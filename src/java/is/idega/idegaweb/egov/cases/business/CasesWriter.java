/*
 * $Id$ Created on Dec 19, 2006
 *
 * Copyright (C) 2006 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf. Use is subject to license terms.
 */
package is.idega.idegaweb.egov.cases.business;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Date;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;

import javax.ejb.FinderException;
import javax.servlet.http.HttpServletRequest;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.VerticalAlignment;

import com.idega.block.process.data.Case;
import com.idega.block.process.data.CaseLog;
import com.idega.block.process.data.CaseStatus;
import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.business.IBORuntimeException;
import com.idega.core.file.util.MimeTypeUtil;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.io.DownloadWriter;
import com.idega.io.MediaWritable;
import com.idega.io.MemoryFileBuffer;
import com.idega.io.MemoryInputStream;
import com.idega.io.MemoryOutputStream;
import com.idega.presentation.IWContext;
import com.idega.user.data.Group;
import com.idega.user.data.User;
import com.idega.util.CoreConstants;
import com.idega.util.IOUtil;
import com.idega.util.IWTimestamp;
import com.idega.util.PersonalIDFormatter;
import com.idega.util.StringHandler;
import com.idega.util.text.Name;
import com.idega.util.text.TextSoap;

import is.idega.idegaweb.egov.cases.data.CaseCategory;
import is.idega.idegaweb.egov.cases.data.CaseType;
import is.idega.idegaweb.egov.cases.data.GeneralCase;
import is.idega.idegaweb.egov.cases.util.CasesConstants;

public class CasesWriter extends DownloadWriter implements MediaWritable {

	protected MemoryFileBuffer buffer = null;
	protected Locale locale;
	protected IWResourceBundle iwrb;

	public static final String PARAMETER_CASE_CATEGORY = "prm_case_category";
	public static final String PARAMETER_SUB_CASE_CATEGORY = "prm_sub_case_category";
	public static final String PARAMETER_CASE_TYPE = "prm_case_type";
	public static final String PARAMETER_CASE_STATUS = "prm_case_status";
	public static final String PARAMETER_ANONYMOUS = "prm_anonymous";
	public static final String PARAMETER_FROM_DATE = "prm_from_date";
	public static final String PARAMETER_TO_DATE = "prm_to_date";

	public CasesWriter() {
	}

	@Override
	public void init(HttpServletRequest req, IWContext iwc) {
		if (iwc == null || !iwc.isLoggedOn()) {
			return;
		}

		try {
			this.locale = iwc.getApplicationSettings().getApplicationLocale();
			this.iwrb = iwc.getIWMainApplication().getBundle(CasesConstants.IW_BUNDLE_IDENTIFIER).getResourceBundle(this.locale);

			CaseCategory category = null;
			if (iwc.isParameterSet(PARAMETER_CASE_CATEGORY)) {
				try {
					category = getBusiness(iwc).getCaseCategory(iwc.getParameter(PARAMETER_CASE_CATEGORY));
				}
				catch (FinderException fe) {
					fe.printStackTrace();
				}
			}

			CaseCategory subCategory = null;
			if (iwc.isParameterSet(PARAMETER_SUB_CASE_CATEGORY)) {
				try {
					subCategory = getBusiness(iwc).getCaseCategory(iwc.getParameter(PARAMETER_SUB_CASE_CATEGORY));
				}
				catch (FinderException fe) {
					fe.printStackTrace();
				}
			}

			CaseType type = null;
			if (iwc.isParameterSet(PARAMETER_CASE_TYPE)) {
				try {
					type = getBusiness(iwc).getCaseType(iwc.getParameter(PARAMETER_CASE_TYPE));
				}
				catch (FinderException fe) {
					fe.printStackTrace();
				}
			}

			CaseStatus status = null;
			if (iwc.isParameterSet(PARAMETER_CASE_STATUS)) {
				status = getBusiness(iwc).getCaseStatus(iwc.getParameter(PARAMETER_CASE_STATUS));
			}

			Boolean anonymous = null;
			if (iwc.isParameterSet(PARAMETER_ANONYMOUS)) {
				anonymous = new Boolean(iwc.getParameter(PARAMETER_ANONYMOUS));
			}

			Date fromDate = null;
			if (iwc.isParameterSet(PARAMETER_FROM_DATE)) {
				fromDate = new IWTimestamp(iwc.getParameter(PARAMETER_FROM_DATE)).getDate();
			}

			Date toDate = null;
			if (iwc.isParameterSet(PARAMETER_TO_DATE)) {
				toDate = new IWTimestamp(iwc.getParameter(PARAMETER_TO_DATE)).getDate();
			}

			Collection<Case> cases = getBusiness(iwc).getCasesByCriteria(category, subCategory, type, status, fromDate, toDate, anonymous);

			this.buffer = writeXLS(iwc, cases);
			setAsDownload(iwc, "cases.xls", this.buffer.length());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getMimeType() {
		if (this.buffer != null) {
			return this.buffer.getMimeType();
		}
		return super.getMimeType();
	}

	@Override
	public void writeTo(IWContext iwc, OutputStream out) throws IOException {
		if (this.buffer != null) {
			MemoryInputStream mis = new MemoryInputStream(this.buffer);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			while (mis.available() > 0) {
				baos.write(mis.read());
			}
			baos.writeTo(out);
			IOUtil.close(mis);
		}
		else {
			System.err.println("buffer is null");
		}
	}

	public MemoryFileBuffer writeXLS(IWContext iwc, Collection<Case> cases) throws Exception {
		MemoryFileBuffer buffer = new MemoryFileBuffer();
		MemoryOutputStream mos = new MemoryOutputStream(buffer);

		HSSFWorkbook workbook = new HSSFWorkbook();

		short cellColumn = 0;
		HSSFSheet sheet = workbook.createSheet(TextSoap.encodeToValidExcelSheetName(StringHandler.shortenToLength(iwrb.getLocalizedString("cases_fetcher.statistics", "Statistics"), 30)));
		sheet.setColumnWidth(cellColumn++, (short) (8 * 256));
		sheet.setColumnWidth(cellColumn++, (short) (14 * 256));
		sheet.setColumnWidth(cellColumn++, (short) (30 * 256));
		sheet.setColumnWidth(cellColumn++, (short) (14 * 256));
		sheet.setColumnWidth(cellColumn++, (short) (18 * 256));
		if (getBusiness(iwc).useTypes()) {
			sheet.setColumnWidth(cellColumn++, (short) (14 * 256));
		}
		sheet.setColumnWidth(cellColumn++, (short) (14 * 256));
		sheet.setColumnWidth(cellColumn++, (short) (30 * 256));
		sheet.setColumnWidth(cellColumn++, (short) (50 * 256));

		HSSFFont font = workbook.createFont();
		font.setBold(true);
		font.setFontHeightInPoints((short) 12);
		HSSFCellStyle style = workbook.createCellStyle();
		style.setFont(font);

		HSSFCellStyle style2 = workbook.createCellStyle();
		style2.setVerticalAlignment(VerticalAlignment.TOP);
		style2.setWrapText(true);

		int cellRow = 0;
		cellColumn = 0;
		HSSFRow row = sheet.createRow(cellRow++);

		HSSFCell cell = row.createCell(cellColumn++);
		cell.setCellValue(this.iwrb.getLocalizedString("cases_fetcher.case_id", "Case ID"));
		cell.setCellStyle(style);

		cell = row.createCell(cellColumn++);
		cell.setCellValue(this.iwrb.getLocalizedString("created_date", "Created date"));
		cell.setCellStyle(style);

		cell = row.createCell(cellColumn++);
		cell.setCellValue(this.iwrb.getLocalizedString("name", "Name"));
		cell.setCellStyle(style);

		cell = row.createCell(cellColumn++);
		cell.setCellValue(this.iwrb.getLocalizedString("personal_id", "Personal ID"));
		cell.setCellStyle(style);

		cell = row.createCell(cellColumn++);
		cell.setCellValue(this.iwrb.getLocalizedString("case_category", "Case category"));
		cell.setCellStyle(style);

		if (getBusiness(iwc).useTypes()) {
			cell = row.createCell(cellColumn++);
			cell.setCellValue(this.iwrb.getLocalizedString("case_type", "Case type"));
			cell.setCellStyle(style);
		}

		cell = row.createCell(cellColumn++);
		cell.setCellValue(this.iwrb.getLocalizedString("reference", "Reference"));
		cell.setCellStyle(style);

		cell = row.createCell(cellColumn++);
		cell.setCellValue(this.iwrb.getLocalizedString("status", "Status"));
		cell.setCellStyle(style);

		cell = row.createCell(cellColumn++);
		cell.setCellValue(this.iwrb.getLocalizedString("regarding", "Regarding"));
		cell.setCellStyle(style);

		cell = row.createCell(cellColumn++);
		cell.setCellValue(this.iwrb.getLocalizedString("message", "Message"));
		cell.setCellStyle(style);

		cell = row.createCell(cellColumn++);
		cell.setCellValue(this.iwrb.getLocalizedString("reply", "Reply"));
		cell.setCellStyle(style);

		User currentUser = iwc.getCurrentUser();

		for (Iterator<Case> iter = cases.iterator(); iter.hasNext();) {
			Case theCase = iter.next();
			if (!(theCase instanceof GeneralCase)) {
				continue;
			}

			GeneralCase element = (GeneralCase) theCase;
			CaseCategory category = element.getCaseCategory();
			if (category != null) {
				Group handlerGroup = category.getHandlerGroup();
				if (handlerGroup != null && !currentUser.hasRelationTo(handlerGroup)) {
					continue;
				}
			}
			CaseType type = element.getCaseType();
			CaseStatus status = element.getCaseStatus();
			if (status != null && status.equals(getBusiness(iwc).getCaseStatusDeleted())) {
				continue;
			}
			User user = element.getOwner();
			IWTimestamp created = new IWTimestamp(element.getCreated());

			row = sheet.createRow(cellRow++);
			cellColumn = 0;

			cell = row.createCell(cellColumn++);
			cell.setCellValue(element.getPrimaryKey().toString());
			cell.setCellStyle(style2);

			cell = row.createCell(cellColumn++);
			cell.setCellValue(created.getLocaleDateAndTime(locale, IWTimestamp.SHORT, IWTimestamp.SHORT));
			cell.setCellStyle(style2);

			if (user != null) {
				Name name = new Name(user.getFirstName(), user.getMiddleName(), user.getLastName());
				cell = row.createCell(cellColumn++);
				cell.setCellValue(name.getName(locale));
				cell.setCellStyle(style2);

				cell = row.createCell(cellColumn++);
				cell.setCellValue(PersonalIDFormatter.format(user.getPersonalID(), locale));
				cell.setCellStyle(style2);
			}
			else {
				cell = row.createCell(cellColumn++);
				cell.setCellValue("");
				cell = row.createCell(cellColumn++);
				cell.setCellValue("");
			}

			cell = row.createCell(cellColumn++);
			cell.setCellValue(category == null ? CoreConstants.EMPTY : category.getLocalizedCategoryName(locale));
			cell.setCellStyle(style2);

			if (type != null && getBusiness(iwc).useTypes()) {
				cell = row.createCell(cellColumn++);
				cell.setCellValue(type.getName());
				cell.setCellStyle(style2);
			}

			cell = row.createCell(cellColumn++);
			cell.setCellValue(element.getReference() != null ? element.getReference() : "");
			cell.setCellStyle(style2);

			cell = row.createCell(cellColumn++);
			cell.setCellValue(status == null ? CoreConstants.MINUS : getBusiness(iwc).getLocalizedCaseStatusDescription(element, status, locale));
			cell.setCellStyle(style2);

			cell = row.createCell(cellColumn++);
			cell.setCellValue(element.getSubject() != null ? element.getSubject() : "-");
			cell.setCellStyle(style2);

			cell = row.createCell(cellColumn++);
			cell.setCellValue(element.getMessage());
			cell.setCellStyle(style2);

			Collection<CaseLog> logs = getBusiness(iwc).getCaseLogs(element);
			if (!logs.isEmpty()) {
				for (CaseLog log : logs) {
					cell = row.createCell(cellColumn++);
					cell.setCellValue(log.getComment());
					cell.setCellStyle(style2);
				}
			}
			else if (element.getReply() != null) {
				cell = row.createCell(cellColumn++);
				cell.setCellValue(element.getReply());
				cell.setCellStyle(style2);
			}
		}

		workbook.write(mos);
		workbook.close();

		buffer.setMimeType(MimeTypeUtil.MIME_TYPE_EXCEL_2);
		return buffer;
	}

	protected CasesBusiness getBusiness(IWApplicationContext iwac) {
		try {
			return IBOLookup.getServiceInstance(iwac, CasesBusiness.class);
		}
		catch (IBOLookupException ile) {
			throw new IBORuntimeException(ile);
		}
	}
}