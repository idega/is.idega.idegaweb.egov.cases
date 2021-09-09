package is.idega.idegaweb.egov.cases.media;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
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
import com.idega.presentation.ui.IWDatePicker;
import com.idega.presentation.ui.handlers.IWDatePickerHandler;
import com.idega.user.data.User;
import com.idega.util.ArrayUtil;
import com.idega.util.CoreConstants;
import com.idega.util.FileUtil;
import com.idega.util.IOUtil;
import com.idega.util.IWTimestamp;
import com.idega.util.ListUtil;
import com.idega.util.StringHandler;
import com.idega.util.StringUtil;
import com.idega.util.datastructures.map.MapUtil;
import com.idega.util.expression.ELUtil;
import com.idega.util.text.TextSoap;

import is.idega.idegaweb.egov.cases.business.BoardCasesManager;
import is.idega.idegaweb.egov.cases.presentation.CasesBoardViewer;
import is.idega.idegaweb.egov.cases.presentation.beans.CaseBoardTableBean;
import is.idega.idegaweb.egov.cases.presentation.beans.CaseBoardTableBodyRowBean;
import is.idega.idegaweb.egov.cases.util.CasesConstants;

public class CasesBoardViewerExporter extends DownloadWriter implements MediaWritable {

	private MemoryFileBuffer memory;

	@Autowired
	@Qualifier(BoardCasesManager.BEAN_NAME)
	private BoardCasesManager boardCasesManager;

	@Autowired
	private ExcelExporterService excelExporterService;

	private String casesType;

	private Class<Serializable> type;

	protected BoardCasesManager getBoardCasesManager() {
		if (this.boardCasesManager == null) {
			ELUtil.getInstance().autowire(this);
		}

		return this.boardCasesManager;
	}

	private ExcelExporterService getExcelExporterService() {
		if (this.excelExporterService == null) {
			ELUtil.getInstance().autowire(this);
		}

		return this.excelExporterService;
	}

	@Override
	public String getMimeType() {
		return MimeTypeUtil.MIME_TYPE_EXCEL_2;
	}

	@Override
	public void init(HttpServletRequest req, IWContext iwc) {
		CaseBoardTableBean data = getTableData(iwc);
		if (data == null || !data.isFilledWithData()) {
			return;
		}

		BoardCasesManager manager = getBoardCasesManager();

		HSSFWorkbook workBook = new HSSFWorkbook();
		HSSFSheet sheet = createSheet(workBook, data.getHeaderLabels(), iwc);

		int rowNumber = 1;
		int maxCells = 0;
		for (CaseBoardTableBodyRowBean rowBean: data.getBodyBeans()) {
			HSSFRow row = sheet.createRow(rowNumber++);

			int cellIndex = 0;
			Map<Integer, List<AdvancedProperty>> values = rowBean.getValues();
			for (Integer key: values.keySet()) {
				List<AdvancedProperty> entries = values.get(key);
				if (ListUtil.isEmpty(entries)) {
					continue;
				}

				if (ProcessConstants.FINANCING_OF_THE_TASKS_VARIABLES.contains(entries.get(0).getId())) {
					//	Financing table
					List<Map<String, String>> financingInfo = rowBean.getFinancingInfo();
					if (ListUtil.isEmpty(financingInfo)) {
						financingInfo = new ArrayList<>();
						Map<String, String> emptyValues = new HashMap<>();
						emptyValues.put(CasesBoardViewer.WORK_ITEM, CoreConstants.MINUS);
						emptyValues.put(CasesBoardViewer.ESTIMATED_COST, CoreConstants.MINUS);
						emptyValues.put(CasesBoardViewer.BOARD_SUGGESTION, CoreConstants.MINUS);
						emptyValues.put(CasesBoardViewer.BOARD_DECISION, CoreConstants.MINUS);
						emptyValues.put(CasesBoardViewer.BOARD_PROPOSAL_FOR_GRANT, CoreConstants.MINUS);
						financingInfo.add(emptyValues);
						rowBean.setFinancingInfo(financingInfo);
					}

					cellIndex += 5;
					long estimationTotal = 0;
					long suggestionTotal = 0;
					long proposalTotal = 0;
					long decisionTotal = 0;
					HSSFRow financingTableRow = row;
					for (Iterator<Map<String, String>> infoIter = financingInfo.iterator(); infoIter.hasNext();) {
						Map<String, String> info = infoIter.next();
						int financingTableCellIndex = key;

						HSSFCell cell = financingTableRow.createCell(financingTableCellIndex++, HSSFCell.CELL_TYPE_STRING);
						cell.setCellValue(info.get(CasesBoardViewer.WORK_ITEM));

						cell = financingTableRow.createCell(financingTableCellIndex++, HSSFCell.CELL_TYPE_NUMERIC);
						String estimation = info.get(CasesBoardViewer.ESTIMATED_COST);
						Long estimationNumber = manager.getNumberValue(estimation);
						estimationTotal += estimationNumber;
						cell.setCellValue(estimationNumber);

						cell = financingTableRow.createCell(financingTableCellIndex++, HSSFCell.CELL_TYPE_NUMERIC);
						String proposalForGrant = info.get(CasesBoardViewer.BOARD_PROPOSAL_FOR_GRANT);
						long proposal = manager.getNumberValue(proposalForGrant);
						proposalTotal += proposal;
						cell.setCellValue(proposal);

						cell = financingTableRow.createCell(financingTableCellIndex++, HSSFCell.CELL_TYPE_NUMERIC);
						String suggestion = info.get(CasesBoardViewer.BOARD_SUGGESTION);
						long sugg = manager.getNumberValue(suggestion);
						suggestionTotal += sugg;
						cell.setCellValue(sugg);

						cell = financingTableRow.createCell(financingTableCellIndex, HSSFCell.CELL_TYPE_NUMERIC);
						String decision = info.get(CasesBoardViewer.BOARD_DECISION);
						long dec = manager.getNumberValue(decision);
						decisionTotal += dec;
						cell.setCellValue(dec);

						if (infoIter.hasNext()) {
							financingTableRow = sheet.createRow(rowNumber++);
						}
					}

					//	Empty row
					financingTableRow = sheet.createRow(rowNumber++);

					//	Totals
					financingTableRow = sheet.createRow(rowNumber++);

					int financingTableCellIndex = key;

					HSSFCell cell = financingTableRow.createCell(financingTableCellIndex++, HSSFCell.CELL_TYPE_STRING);
					cell.setCellValue(getIWResourceBundle(iwc).getLocalizedString("total", "Total"));

					cell = financingTableRow.createCell(financingTableCellIndex++, HSSFCell.CELL_TYPE_NUMERIC);
					cell.setCellValue(estimationTotal);

					cell = financingTableRow.createCell(financingTableCellIndex++, HSSFCell.CELL_TYPE_NUMERIC);
					cell.setCellValue(proposalTotal);

					cell = financingTableRow.createCell(financingTableCellIndex++, HSSFCell.CELL_TYPE_NUMERIC);
					cell.setCellValue(suggestionTotal);

					cell = financingTableRow.createCell(financingTableCellIndex++, HSSFCell.CELL_TYPE_NUMERIC);
					cell.setCellValue(decisionTotal);
				} else {
					boolean canSkip = !getBoardCasesManager().hasCustomColumns(uuid);
					if (canSkip) {
						String tmpId = entries.iterator().next().getId();
						canSkip = tmpId.equals(ProcessConstants.BOARD_FINANCING_SUGGESTION) || tmpId.equals(ProcessConstants.BOARD_FINANCING_DECISION);
					}

					if (canSkip) {
						//	Do nothing - it was added already with financing table

					} else {
						//	Simple values
						HSSFCell bodyRowCell = row.createCell(cellIndex);

						for (AdvancedProperty entry: entries) {
							String varName = entry.getId();

							if (manager.isEqual(varName, ProcessConstants.CASE_IDENTIFIER)) {
								bodyRowCell.setCellValue(rowBean.getCaseIdentifier());

							} else if (manager.isEqual(varName, ProcessConstants.HANDLER_IDENTIFIER)) {
								bodyRowCell.setCellValue(getHandlerInfo(iwc, rowBean.getHandler()));

//							} else if (
//									manager.isEqual(varName, CasesBoardViewer.BOARD_SUGGESTION) ||
//									manager.isEqual(varName, CasesBoardViewer.BOARD_DECISION) ||
//									manager.isEqual(varName, CasesBoardViewer.BOARD_PROPOSAL_FOR_GRANT)
//							) {
//
//								String boardValue = entry.getValue();
//								bodyRowCell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
//								Long numberValue = manager.getNumberValue(boardValue);
//								bodyRowCell.setCellValue(numberValue);

							} else {
								bodyRowCell.setCellValue(entry.getValue());
							}
						}

						cellIndex++;
					}
				}
			}
			maxCells = maxCells < cellIndex ? cellIndex : maxCells;
		}

		createCellsWithValues(sheet, null, data.getFooterValues(), rowNumber);
		autosizeSheetColumns(sheet, maxCells);
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

			if (cellStyle != null) {
				cell.setCellStyle(cellStyle);
			}
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

				if (cellStyle != null) {
					cell.setCellStyle(cellStyle);
				}
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

	/**
	 *
	 * @param uuid is id of UI component in page, not <code>null</code>;
	 * @return <code>true</code> if only subscribed cases should be shown,
	 * <code>false</code> otherwise;
	 * @author <a href="mailto:martynas@idega.is">Martynas Stakė</a>
	 */
	protected boolean isSubscribedOnly(String uuid, IWContext iwc) {
		if (StringUtil.isEmpty(uuid)) {
			return Boolean.FALSE;
		}

		Object subscribed = iwc.getSessionAttribute(CasesBoardViewer.PARAMETER_SHOW_ONLY_SUBSCRIBED + uuid);
		Boolean value = null;
		if (subscribed instanceof Boolean) {
			value = (Boolean) subscribed;
		}

		if (value == null || value.equals(Boolean.FALSE)) {
			return Boolean.FALSE;
		} else {
			return Boolean.TRUE;
		}
	}

	protected IWDatePicker getDateRange(IWContext iwc, String name, Date from, Date to) {
		IWDatePicker datePicker = new IWDatePicker(name);
		datePicker.setId(name);
		datePicker.setVersion(IWDatePicker.VERSION_1_8_17);
		datePicker.keepStatusOnAction(true);

		if (from != null) {
			datePicker.setDate(from);
		}
		if (to != null) {
			datePicker.setDateTo(to);
		}
		datePicker.setDateRange(true);
		datePicker.setUseCurrentDateIfNotSet(false);

		return datePicker;
	}

	protected IWTimestamp getTimestampFrom(IWContext iwc) {
		if (iwc.isParameterSet(CasesBoardViewer.PARAMETER_DATE_RANGE)) {
			String dateRangeValue = iwc.getParameter(CasesBoardViewer.PARAMETER_DATE_RANGE);
			String[] dates = dateRangeValue.split(CoreConstants.MINUS);
			if (!ArrayUtil.isEmpty(dates) && dates.length == 2) {
				Locale locale = iwc.getCurrentLocale();
				java.util.Date tmp = IWDatePickerHandler.getParsedDate(dates[0].trim(), locale);
				if (tmp != null) {
					IWTimestamp iwFrom = new IWTimestamp(tmp);
					iwFrom.setTime(0, 0, 0, 0);
					return iwFrom;
				}
			}
		}

		return null;
	}

	protected IWTimestamp getTimestampTo(IWContext iwc) {
		if (iwc.isParameterSet(CasesBoardViewer.PARAMETER_DATE_RANGE)) {
			String dateRangeValue = iwc.getParameter(CasesBoardViewer.PARAMETER_DATE_RANGE);
			String[] dates = dateRangeValue.split(CoreConstants.MINUS);
			if (!ArrayUtil.isEmpty(dates) && dates.length == 2) {
				Locale locale = iwc.getCurrentLocale();
				java.util.Date tmp = IWDatePickerHandler.getParsedDate(dates[1].trim(), locale);
				if (tmp != null) {
					IWTimestamp iwTo = new IWTimestamp(tmp);
					iwTo.setTime(23, 59, 59, 999);
					return iwTo;
				}
			}
		}

		return null;
	}

	protected java.util.Date getDateFrom(IWContext iwc) {
		IWTimestamp timestamp = getTimestampFrom(iwc);
		if (timestamp != null) {
			return timestamp.getTimestamp();
		}

		return null;
	}

	protected java.util.Date getDateTo(IWContext iwc) {
		IWTimestamp timestamp = getTimestampTo(iwc);
		if (timestamp != null) {
			return timestamp.getTimestamp();
		}

		return null;
	}

	protected boolean doFilterByDate(IWContext iwc) {
		return iwc.getApplicationSettings().getBoolean(
				CasesBoardViewer.PROPERTY_SHOW_DATE_FILTER,
				Boolean.FALSE);
	}

	private String uuid = null;

	protected CaseBoardTableBean getTableData(IWContext iwc) {
		if (iwc == null) {
			return null;
		}

		uuid = iwc.getParameter(CasesBoardViewer.PARAMETER_UUID);

		User currentUser = iwc.isLoggedOn() ? iwc.getCurrentUser() : null;

		String casesType = getCasesType();
		return getBoardCasesManager().getTableData(
				currentUser,
				doFilterByDate(iwc) ? getDateFrom(iwc) : null,
				doFilterByDate(iwc) ? getDateTo(iwc) : null,
				Arrays.asList(iwc.getParameter(CasesBoardViewer.CASES_BOARD_VIEWER_CASES_STATUS_PARAMETER).split(CoreConstants.COMMA)),
				iwc.getParameter(CasesBoardViewer.CASES_BOARD_VIEWER_PROCESS_NAME_PARAMETER),
				uuid,
				isSubscribedOnly(iwc.getParameter(CasesBoardViewer.PARAMETER_UUID), iwc),
				Boolean.FALSE,
				null,
				StringUtil.isEmpty(casesType) ? ProcessConstants.BPM_CASE : casesType,
				getType()
		);
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

	protected HSSFSheet createSheet(HSSFWorkbook workBook, Map<Integer, List<AdvancedProperty>> headers, IWContext iwc) {
		if (workBook == null || iwc == null) {
			return null;
		}

		HSSFSheet sheet = workBook.createSheet(TextSoap.encodeToValidExcelSheetName(StringHandler.shortenToLength(
				getIWResourceBundle(iwc).getLocalizedString(
						"cases_board_viewer.cases_board", "Cases board"),
				30)));

		if (!MapUtil.isEmpty(headers)) {
			createHeaders(
					sheet,
					createBigStyle(workBook, createBigFont(workBook)),
					headers,
					0
			);
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
	protected boolean autosizeSheetColumns(HSSFSheet sheet, int nrOfCells) {
		return getExcelExporterService().autosizeSheetColumns(sheet, nrOfCells);
	}

	public String getCasesType() {
		return casesType;
	}

	public void setCasesType(String casesType) {
		this.casesType = casesType;
	}

	public Class<Serializable> getType() {
		return type;
	}

	public void setType(Class<Serializable> type) {
		this.type = type;
	}

}