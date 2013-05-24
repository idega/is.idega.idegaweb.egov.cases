package is.idega.idegaweb.egov.cases.media;

import is.idega.idegaweb.egov.cases.business.BoardCasesManager;
import is.idega.idegaweb.egov.cases.presentation.CasesBoardViewer;
import is.idega.idegaweb.egov.cases.presentation.beans.CaseBoardTableBean;
import is.idega.idegaweb.egov.cases.presentation.beans.CaseBoardTableBodyRowBean;
import is.idega.idegaweb.egov.cases.util.CasesConstants;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.idega.block.process.business.ProcessConstants;
import com.idega.builder.bean.AdvancedProperty;
import com.idega.core.file.util.MimeTypeUtil;
import com.idega.idegaweb.IWMainApplication;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.io.DownloadWriter;
import com.idega.io.MediaWritable;
import com.idega.io.MemoryFileBuffer;
import com.idega.io.MemoryOutputStream;
import com.idega.presentation.IWContext;
import com.idega.user.data.User;
import com.idega.util.CoreConstants;
import com.idega.util.FileUtil;
import com.idega.util.IOUtil;
import com.idega.util.ListUtil;
import com.idega.util.StringHandler;
import com.idega.util.datastructures.map.MapUtil;
import com.idega.util.expression.ELUtil;

public class CasesBoardViewerExporter extends DownloadWriter implements MediaWritable {

	private MemoryFileBuffer memory;

	@Autowired
	@Qualifier(BoardCasesManager.BEAN_NAME)
	private BoardCasesManager boardCasesManager;

	protected BoardCasesManager getBoardCasesManager() {
		if (this.boardCasesManager == null) {
			ELUtil.getInstance().autowire(this);
		}

		return this.boardCasesManager;
	}

	@Override
	public String getMimeType() {
		return MimeTypeUtil.MIME_TYPE_EXCEL_2;
	}

	@Override
	public void init(HttpServletRequest req, IWContext iwc) {
		CaseBoardTableBean data = getTableData(iwc);
		if (data == null || !data.isFilledWithData())
			return;

		HSSFWorkbook workBook = new HSSFWorkbook();
		HSSFSheet sheet = createSheet(workBook, data.getHeaderLabels(), iwc);

		int rowNumber = 1;
		for (CaseBoardTableBodyRowBean rowBean: data.getBodyBeans()) {
			HSSFRow row = sheet.createRow(rowNumber++);

			int cellIndex = 0;
			Map<Integer, List<AdvancedProperty>> values = rowBean.getValues();
			for (Integer key: values.keySet()) {
				List<AdvancedProperty> entries = values.get(key);
				if (ListUtil.isEmpty(entries))
					continue;

				if (ProcessConstants.FINANCING_OF_THE_TASKS.equals(entries.get(0).getId())) {
					//	Financing table
					List<Map<String, String>> financingInfo = rowBean.getFinancingInfo();
					if (ListUtil.isEmpty(financingInfo)) {
						financingInfo = new ArrayList<Map<String,String>>();
						Map<String, String> emptyValues = new HashMap<String, String>();
						emptyValues.put(CasesBoardViewer.WORK_ITEM, CoreConstants.MINUS);
						emptyValues.put(CasesBoardViewer.ESTIMATED_COST, CoreConstants.MINUS);
						emptyValues.put(CasesBoardViewer.BOARD_SUGGESTION, CoreConstants.MINUS);
						emptyValues.put(CasesBoardViewer.BOARD_DECISION, CoreConstants.MINUS);
						financingInfo.add(emptyValues);
						rowBean.setFinancingInfo(financingInfo);
					}

					cellIndex += 4;
					long estimationTotal = 0;
					long suggestionTotal = 0;
					long decisionTotal = 0;
					HSSFRow financingTableRow = row;
					for (Iterator<Map<String, String>> infoIter = financingInfo.iterator(); infoIter.hasNext();) {
						Map<String, String> info = infoIter.next();
						int financingTableCellIndex = key;

						HSSFCell cell = financingTableRow.createCell(financingTableCellIndex++, HSSFCell.CELL_TYPE_STRING);
						cell.setCellValue(info.get(CasesBoardViewer.WORK_ITEM));

						cell = financingTableRow.createCell(financingTableCellIndex++, HSSFCell.CELL_TYPE_NUMERIC);
						String estimation = info.get(CasesBoardViewer.ESTIMATED_COST);
						estimationTotal += getBoardCasesManager().getNumberValue(estimation);
						cell.setCellValue(estimation);

						cell = financingTableRow.createCell(financingTableCellIndex++, HSSFCell.CELL_TYPE_NUMERIC);
						String suggestion = info.get(CasesBoardViewer.BOARD_SUGGESTION);
						long sugg = getBoardCasesManager().getNumberValue(suggestion);
						suggestionTotal += sugg;
						cell.setCellValue(String.valueOf(sugg));

						cell = financingTableRow.createCell(financingTableCellIndex, HSSFCell.CELL_TYPE_NUMERIC);
						String decision = info.get(CasesBoardViewer.BOARD_DECISION);
						long dec = getBoardCasesManager().getNumberValue(decision);
						decisionTotal += dec;
						cell.setCellValue(String.valueOf(dec));

						if (infoIter.hasNext())
							financingTableRow = sheet.createRow(rowNumber++);
					}

					//	Empty row
					financingTableRow = sheet.createRow(rowNumber++);

					//	Totals
					financingTableRow = sheet.createRow(rowNumber++);

					int financingTableCellIndex = key;

					HSSFCell cell = financingTableRow.createCell(financingTableCellIndex++, HSSFCell.CELL_TYPE_STRING);
					cell.setCellValue(getIWResourceBundle(iwc).getLocalizedString("total", "Total"));

					cell = financingTableRow.createCell(financingTableCellIndex++, HSSFCell.CELL_TYPE_NUMERIC);
					cell.setCellValue(String.valueOf(estimationTotal));

					cell = financingTableRow.createCell(financingTableCellIndex++, HSSFCell.CELL_TYPE_NUMERIC);
					cell.setCellValue(String.valueOf(suggestionTotal));

					cell = financingTableRow.createCell(financingTableCellIndex++, HSSFCell.CELL_TYPE_NUMERIC);
					cell.setCellValue((String.valueOf(decisionTotal)));
				} else {
					//	Simple values
					HSSFCell bodyRowCell = row.createCell(cellIndex);

					for (AdvancedProperty entry: entries) {
						String varName = entry.getId();

						if (getBoardCasesManager().isColumnOfDomain(varName, ProcessConstants.CASE_IDENTIFIER)) {
							bodyRowCell.setCellValue(rowBean.getCaseIdentifier());

						} else if (getBoardCasesManager().isColumnOfDomain(varName, ProcessConstants.HANDLER_IDENTIFIER)) {
							bodyRowCell.setCellValue(getHandlerInfo(iwc, rowBean.getHandler()));

						} else if (getBoardCasesManager().isColumnOfDomain(varName, CasesBoardViewer.BOARD_SUGGESTION) ||
								getBoardCasesManager().isColumnOfDomain(varName, CasesBoardViewer.BOARD_DECISION)) {

							String boardValue = entry.getValue();
							Logger.getLogger(getClass().getName()).info("Handling board value '" + boardValue + "' for variable '" + varName + "'");
							bodyRowCell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
							Long numberValue = getBoardCasesManager().getNumberValue(boardValue);
							bodyRowCell.setCellValue(String.valueOf(numberValue));

						} else {
							bodyRowCell.setCellValue(entry.getValue());
						}
					}

					cellIndex++;
				}
			}
		}

		createCellsWithValues(sheet, null, data.getFooterValues(), rowNumber);
		autosizeSheetColumns(sheet);
		write(createOutputStream(), workBook, iwc);
	}

	protected String getHandlerInfo(IWContext iwc, User handler) {
		AdvancedProperty info = null;
		try {
			info = getBoardCasesManager().getHandlerInfo(iwc, handler);
		} catch(Exception e) {
			e.printStackTrace();
		}

		if (info == null) {
			return CoreConstants.EMPTY;
		}

		return info.getId();
	}

	protected void createCellsWithValues(HSSFSheet sheet, HSSFCellStyle cellStyle, List<String> labels, int rowNumber) {
		HSSFRow row = sheet.createRow(rowNumber);

		int cellIndex = 0;
		for (String label: labels) {
			HSSFCell cell = row.createCell(cellIndex++);
			cell.setCellValue(label);

			if (cellStyle != null)
				cell.setCellStyle(cellStyle);
		}
	}

	protected void createHeaders(HSSFSheet sheet, HSSFCellStyle cellStyle, Map<Integer, List<AdvancedProperty>> headers, int rowNumber) {
		HSSFRow row = sheet.createRow(rowNumber);

		int cellIndex = 0;
		for (Integer key: headers.keySet()) {
			List<AdvancedProperty> labels = headers.get(key);
			for (AdvancedProperty label: labels) {
				HSSFCell cell = row.createCell(cellIndex++);
				cell.setCellValue(label.getValue());

				if (cellStyle != null)
					cell.setCellStyle(cellStyle);
			}
		}
	}

	@Override
	public void writeTo(OutputStream streamOut) throws IOException {
		InputStream streamIn = new ByteArrayInputStream(memory.buffer());
		FileUtil.streamToOutputStream(streamIn, streamOut);

		streamOut.flush();
		streamOut.close();
		streamIn.close();
	}

	protected IWResourceBundle getIWResourceBundle(IWContext iwc) {
		if (iwc == null) {
			return null;
		}

		IWMainApplication iwma = iwc.getIWMainApplication();
		if (iwma == null) {
			return null;
		}

		com.idega.idegaweb.IWBundle iwb = iwma.getBundle(
				CasesConstants.IW_BUNDLE_IDENTIFIER);
		if (iwb == null) {
			return null;
		}

		return iwb.getResourceBundle(iwc);
	}

	protected CaseBoardTableBean getTableData(IWContext iwc) {
		if (iwc == null) {
			return null;
		}

		return getBoardCasesManager().getTableData(
				iwc,
				Arrays.asList(iwc.getParameter(CasesBoardViewer.CASES_BOARD_VIEWER_CASES_STATUS_PARAMETER).split(CoreConstants.COMMA)),
				iwc.getParameter(CasesBoardViewer.CASES_BOARD_VIEWER_PROCESS_NAME_PARAMETER),
				iwc.getParameter(CasesBoardViewer.PARAMETER_UUID));
	}

	protected OutputStream createOutputStream() {
		memory = new MemoryFileBuffer();
		return new MemoryOutputStream(memory);
	}

	protected HSSFFont createBigFont(HSSFWorkbook workBook) {
		if (workBook == null) {
			return null;
		}

		HSSFFont bigFont = workBook.createFont();
		bigFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		bigFont.setFontHeightInPoints((short) 13);
		return bigFont;
	}

	protected HSSFCellStyle createBigStyle(HSSFWorkbook workBook, HSSFFont font) {
		if (workBook == null) {
			return null;
		}

		HSSFCellStyle bigStyle = workBook.createCellStyle();

		if (font != null) {
			bigStyle.setFont(font);
		}

		return bigStyle;
	}

	protected HSSFSheet createSheet(HSSFWorkbook workBook,
			Map<Integer, List<AdvancedProperty>> headers, IWContext iwc) {
		if (workBook == null || iwc == null) {
			return null;
		}

		HSSFSheet sheet = workBook.createSheet(StringHandler.shortenToLength(
				getIWResourceBundle(iwc).getLocalizedString(
						"cases_board_viewer.cases_board", "Cases board"),
				30));

		if (!MapUtil.isEmpty(headers)) {
			createHeaders(
					sheet,
					createBigStyle(workBook, createBigFont(workBook)),
					headers,
					0);
		}

		return sheet;
	}

	protected boolean write(OutputStream streamOut, HSSFWorkbook workBook, IWContext iwc) {
		try {
			workBook.write(streamOut);
		} catch (Exception e) {
			Logger.getLogger(CasesBoardViewer.class.getName()).log(Level.SEVERE, "Error writing cases board to Excel!", e);
			return false;
		} finally {
			IOUtil.closeOutputStream(streamOut);
		}

		memory.setMimeType(MimeTypeUtil.MIME_TYPE_EXCEL_2);
		setAsDownload(iwc,
				new StringBuilder(getIWResourceBundle(iwc).getLocalizedString(
						"cases_board_viewer.exported_data", "Exported cases board"
						)).append(".xls").toString(),
				memory.length());

		return true;
	}

	/**
	 *
	 * <p>Automatically fits content to each column in sheet.</p>
	 * @param sheet to format;
	 * @return <code>true</code> if formatted, false otherwise;
	 * @author <a href="mailto:martynas@idega.com">Martynas Stakė</a>
	 */
	protected boolean autosizeSheetColumns(HSSFSheet sheet) {
		if (sheet == null) {
			return false;
		}

		for (int i = 0; i < sheet.getLastRowNum(); i++) {
			HSSFRow row = sheet.getRow(i);
			int cell = 0;
			for (Iterator<Cell> cellsIter = row.cellIterator(); cellsIter.hasNext();) {
				cellsIter.next();

				if (row.getCell(cell) != null)
					sheet.autoSizeColumn(cell);
				cell++;
			}
		}

		return true;
	}
}