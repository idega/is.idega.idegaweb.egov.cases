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
import org.springframework.beans.factory.annotation.Autowired;

import com.idega.block.process.business.ProcessConstants;
import com.idega.builder.bean.AdvancedProperty;
import com.idega.core.file.util.MimeTypeUtil;
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
import com.idega.util.StringHandler;
import com.idega.util.expression.ELUtil;

public class CasesBoardViewerExporter extends DownloadWriter implements MediaWritable {
	
private MemoryFileBuffer memory;
	
	@Autowired
	private BoardCasesManager boardCasesManager;

	@Override
	public String getMimeType() {
		return MimeTypeUtil.MIME_TYPE_EXCEL_2;
	}

	@Override
	public void init(HttpServletRequest req, IWContext iwc) {
		ELUtil.getInstance().autowire(this);
		
		String processName = iwc.getParameter(CasesBoardViewer.CASES_BOARD_VIEWER_PROCESS_NAME_PARAMETER);
		String caseStatus = iwc.getParameter(CasesBoardViewer.CASES_BOARD_VIEWER_CASES_STATUS_PARAMETER);
		String uuid = iwc.getParameter(CasesBoardViewer.PARAMETER_UUID);
		
		CaseBoardTableBean data = boardCasesManager.getTableData(iwc, caseStatus, processName, uuid);
		if (data == null || !data.isFilledWithData()) {
			return;
		}
		
		IWResourceBundle iwrb = iwc.getIWMainApplication().getBundle(CasesConstants.IW_BUNDLE_IDENTIFIER).getResourceBundle(iwc);
		
		memory = new MemoryFileBuffer();
		OutputStream streamOut = new MemoryOutputStream(memory);
		HSSFWorkbook workBook = new HSSFWorkbook();

		HSSFFont bigFont = workBook.createFont();
		bigFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		bigFont.setFontHeightInPoints((short) 13);
		HSSFCellStyle bigStyle = workBook.createCellStyle();
		bigStyle.setFont(bigFont);
		
		HSSFSheet sheet = workBook.createSheet(StringHandler.shortenToLength(iwrb.getLocalizedString("cases_board_viewer.cases_board", "Cases board"), 30));
		int rowNumber = 0;
		createHeaders(sheet, bigStyle, data.getHeaderLabels(), rowNumber);
		rowNumber++;
		for (CaseBoardTableBodyRowBean rowBean: data.getBodyBeans()) {
			HSSFRow row = sheet.createRow(rowNumber);
			
			short index = 0;
			Map<Integer, AdvancedProperty> values = rowBean.getValues();
			for (Integer key: values.keySet()) {
				HSSFCell bodyRowCell = row.createCell(index);
				AdvancedProperty entry = values.get(key);
				
				if (boardCasesManager.isColumnOfDomain(entry.getId(), CasesBoardViewer.CASE_FIELDS.get(5).getId())) {
					bodyRowCell.setCellValue(rowBean.getCaseIdentifier());
				} else if (boardCasesManager.isColumnOfDomain(entry.getId(), ProcessConstants.HANDLER_IDENTIFIER)) {
					bodyRowCell.setCellValue(getHandlerInfo(iwc, rowBean.getHandler()));
				} else {
					bodyRowCell.setCellValue(entry.getValue());
				}
				
				index++;
			}

			rowNumber++;
		}
		
		createHeaders(sheet, null, data.getFooterValues(), rowNumber);
		
		try {
			workBook.write(streamOut);
		} catch (Exception e) {
			Logger.getLogger(CasesBoardViewer.class.getName()).log(Level.SEVERE, "Error writing cases board to Excel!", e);
			return;
		} finally {
			IOUtil.closeOutputStream(streamOut);
		}
		
		memory.setMimeType(MimeTypeUtil.MIME_TYPE_EXCEL_2);
		setAsDownload(iwc, new StringBuilder(iwrb.getLocalizedString("cases_board_viewer.exported_data", "Exported cases board")).append(".xls").toString(),
				memory.length());
	}
	
	private String getHandlerInfo(IWContext iwc, User handler) {
		AdvancedProperty info = null;
		try {
			info = boardCasesManager.getHandlerInfo(iwc, handler);
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		if (info == null) {
			return CoreConstants.EMPTY;
		}
		
		return info.getId();
	}
	
	private void createHeaders(HSSFSheet sheet, HSSFCellStyle cellStyle, List<String> labels, int rowNumber) {
		HSSFRow row = sheet.createRow(rowNumber);
		
		short cellIndex = 0;
		for (String label: labels) {
			HSSFCell cell = row.createCell(cellIndex++);
			cell.setCellValue(label);
			
			if (cellStyle != null)
				cell.setCellStyle(cellStyle);
		}
	}
	
	private void createHeaders(HSSFSheet sheet, HSSFCellStyle cellStyle, Map<Integer, AdvancedProperty> headers, int rowNumber) {
		HSSFRow row = sheet.createRow(rowNumber);
		
		short cellIndex = 0;
		for (Integer key: headers.keySet()) {
			HSSFCell cell = row.createCell(cellIndex++);
			cell.setCellValue(headers.get(key).getValue());
			
			if (cellStyle != null)
				cell.setCellStyle(cellStyle);
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

}