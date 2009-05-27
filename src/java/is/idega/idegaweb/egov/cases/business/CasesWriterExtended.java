/*
 * $Id$
 * Created on Apr 23, 2008
 *
 * Copyright (C) 2008 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf.
 * Use is subject to license terms.
 */
package is.idega.idegaweb.egov.cases.business;

import is.idega.idegaweb.egov.cases.data.CaseCategory;
import is.idega.idegaweb.egov.cases.data.CaseType;
import is.idega.idegaweb.egov.cases.data.GeneralCase;

import java.util.Collection;
import java.util.Iterator;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import com.idega.block.process.data.CaseStatus;
import com.idega.core.file.util.MimeTypeUtil;
import com.idega.io.MemoryFileBuffer;
import com.idega.io.MemoryOutputStream;
import com.idega.presentation.IWContext;
import com.idega.user.data.User;
import com.idega.util.IWTimestamp;
import com.idega.util.PersonalIDFormatter;
import com.idega.util.StringHandler;
import com.idega.util.text.Name;

public class CasesWriterExtended extends CasesWriter {

	@Override
	public MemoryFileBuffer writeXLS(IWContext iwc, Collection cases) throws Exception {
		MemoryFileBuffer buffer = new MemoryFileBuffer();
		MemoryOutputStream mos = new MemoryOutputStream(buffer);

		HSSFWorkbook workbook = new HSSFWorkbook();

		HSSFSheet sheet = workbook.createSheet(StringHandler.shortenToLength(iwrb.getLocalizedString("cases_fetcher.statistics", "Statistics"), 30));
		sheet.setColumnWidth((short) 0, (short) (38 * 256));
		sheet.setColumnWidth((short) 1, (short) (85 * 256));

		HSSFFont font = workbook.createFont();
		font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		font.setFontHeightInPoints((short) 12);

		HSSFCellStyle style = workbook.createCellStyle();
		style.setFont(font);
		HSSFCellStyle style2 = workbook.createCellStyle();
		style2.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		style2.setVerticalAlignment(HSSFCellStyle.VERTICAL_TOP);
		style2.setWrapText(true);
		HSSFCellStyle style3 = workbook.createCellStyle();
		style3.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		style3.setFont(font);

		int cellRow = 0;
		Iterator iter = cases.iterator();
		while (iter.hasNext()) {
			GeneralCase element = (GeneralCase) iter.next();
			CaseCategory category = element.getCaseCategory();
			CaseType type = element.getCaseType();
			CaseStatus status = element.getCaseStatus();
			if (status.equals(getBusiness(iwc).getCaseStatusDeleted())) {
				continue;
			}
			User user = element.getOwner();
			IWTimestamp created = new IWTimestamp(element.getCreated());

			HSSFRow row = sheet.createRow(cellRow++);

			HSSFCell cell = row.createCell((short) 0);
			cell.setCellValue(this.iwrb.getLocalizedString("cases_fetcher.case_id", "Case ID"));
			cell.setCellStyle(style);

			cell = row.createCell((short) 1);
			cell.setCellValue(element.getPrimaryKey().toString());

			row = sheet.createRow(cellRow++);

			cell = row.createCell((short) 0);
			cell.setCellValue(this.iwrb.getLocalizedString("created_date", "Created date"));
			cell.setCellStyle(style);

			cell = row.createCell((short) 1);
			cell.setCellValue(created.getLocaleDateAndTime(locale, IWTimestamp.SHORT, IWTimestamp.SHORT));

			if (user != null) {
				row = sheet.createRow(cellRow++);

				cell = row.createCell((short) 0);
				cell.setCellValue(this.iwrb.getLocalizedString("name", "Name"));
				cell.setCellStyle(style);

				Name name = new Name(user.getFirstName(), user.getMiddleName(), user.getLastName());
				cell = row.createCell((short) 1);
				cell.setCellValue(name.getName(locale));

				row = sheet.createRow(cellRow++);

				cell = row.createCell((short) 0);
				cell.setCellValue(this.iwrb.getLocalizedString("personal_id", "Personal ID"));
				cell.setCellStyle(style);

				cell = row.createCell((short) 1);
				cell.setCellValue(PersonalIDFormatter.format(user.getPersonalID(), locale));
			}

			row = sheet.createRow(cellRow++);

			cell = row.createCell((short) 0);
			cell.setCellValue(this.iwrb.getLocalizedString("case_category", "Case category"));
			cell.setCellStyle(style);

			cell = row.createCell((short) 1);
			cell.setCellValue(category.getLocalizedCategoryName(locale));

			if (getBusiness(iwc).useTypes()) {
				row = sheet.createRow(cellRow++);

				cell = row.createCell((short) 0);
				cell.setCellValue(this.iwrb.getLocalizedString("case_type", "Case type"));
				cell.setCellStyle(style);

				cell = row.createCell((short) 1);
				cell.setCellValue(type.getName());
			}
			
			if (element.getReference() != null) {
				row = sheet.createRow(cellRow++);

				cell = row.createCell((short) 0);
				cell.setCellValue(this.iwrb.getLocalizedString("reference", "Reference"));
				cell.setCellStyle(style);

				cell = row.createCell((short) 1);
				cell.setCellValue(element.getReference());
			}

			row = sheet.createRow(cellRow++);

			cell = row.createCell((short) 0);
			cell.setCellValue(this.iwrb.getLocalizedString("status", "Status"));
			cell.setCellStyle(style);

			cell = row.createCell((short) 1);
			cell.setCellValue(getBusiness(iwc).getLocalizedCaseStatusDescription(element, status, locale));

			row = sheet.createRow(cellRow++);

			cell = row.createCell((short) 0);
			cell.setCellValue(this.iwrb.getLocalizedString("regarding", "Regarding"));
			cell.setCellStyle(style);

			cell = row.createCell((short) 1);
			cell.setCellValue(element.getSubject() != null ? element.getSubject() : "-");

			row = sheet.createRow(cellRow++);
			row = sheet.createRow(cellRow++);

			cell = row.createCell((short) 0);
			cell.setCellValue(this.iwrb.getLocalizedString("message", "Message"));
			cell.setCellStyle(style3);

			cell = row.createCell((short) 1);
			cell.setCellValue(element.getMessage());
			cell.setCellStyle(style2);

			if (element.getReply() != null) {
				row = sheet.createRow(cellRow++);
	
				cell = row.createCell((short) 0);
				cell.setCellValue(this.iwrb.getLocalizedString("reply", "Reply"));
				cell.setCellStyle(style3);
	
				cell = row.createCell((short) 1);
				cell.setCellValue(element.getReply());
				cell.setCellStyle(style2);
			}
		}

		workbook.write(mos);

		buffer.setMimeType(MimeTypeUtil.MIME_TYPE_EXCEL_2);
		return buffer;
	}
}