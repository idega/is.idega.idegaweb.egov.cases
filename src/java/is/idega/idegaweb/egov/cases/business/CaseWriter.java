/*
 * $Id$
 * Created on Dec 6, 2007
 *
 * Copyright (C) 2007 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf.
 * Use is subject to license terms.
 */
package is.idega.idegaweb.egov.cases.business;

import is.idega.idegaweb.egov.cases.data.CaseCategory;
import is.idega.idegaweb.egov.cases.data.CaseType;
import is.idega.idegaweb.egov.cases.data.GeneralCase;
import is.idega.idegaweb.egov.cases.util.CaseConstants;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.business.IBORuntimeException;
import com.idega.core.contact.data.Email;
import com.idega.core.contact.data.Phone;
import com.idega.core.location.data.Address;
import com.idega.core.location.data.PostalCode;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.io.DownloadWriter;
import com.idega.io.MediaWritable;
import com.idega.io.MemoryFileBuffer;
import com.idega.io.MemoryInputStream;
import com.idega.io.MemoryOutputStream;
import com.idega.presentation.IWContext;
import com.idega.user.business.NoEmailFoundException;
import com.idega.user.business.NoPhoneFoundException;
import com.idega.user.business.UserBusiness;
import com.idega.user.data.User;
import com.idega.util.IWTimestamp;
import com.idega.util.PersonalIDFormatter;
import com.idega.util.text.Name;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

public class CaseWriter extends DownloadWriter implements MediaWritable {

	private MemoryFileBuffer buffer = null;
	private Locale locale;
	private IWResourceBundle iwrb;
	private GeneralCase theCase;

	public void init(HttpServletRequest req, IWContext iwc) {
		try {
			this.locale = iwc.getApplicationSettings().getApplicationLocale();
			this.iwrb = iwc.getIWMainApplication().getBundle(CaseConstants.IW_BUNDLE_IDENTIFIER).getResourceBundle(this.locale);

			if (iwc.isParameterSet(getCasesBusiness(iwc).getSelectedCaseParameter())) {
				this.theCase = getCasesBusiness(iwc).getGeneralCase(iwc.getParameter(getCasesBusiness(iwc).getSelectedCaseParameter()));
			}

			this.buffer = writePDF(iwc);
			setAsDownload(iwc, "case.pdf", this.buffer.length());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getMimeType() {
		if (this.buffer != null) {
			return this.buffer.getMimeType();
		}
		return super.getMimeType();
	}

	public void writeTo(OutputStream out) throws IOException {
		if (this.buffer != null) {
			MemoryInputStream mis = new MemoryInputStream(this.buffer);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			while (mis.available() > 0) {
				baos.write(mis.read());
			}
			baos.writeTo(out);
		}
		else {
			System.err.println("buffer is null");
		}
	}

	protected MemoryFileBuffer writePDF(IWContext iwc) {
		Font titleFont = new Font(Font.HELVETICA, 14, Font.BOLD);
		Font labelFont = new Font(Font.HELVETICA, 11, Font.BOLD);
		Font textFont = new Font(Font.HELVETICA, 11, Font.NORMAL);

		try {
			MemoryFileBuffer buffer = new MemoryFileBuffer();
			MemoryOutputStream mos = new MemoryOutputStream(buffer);

			Document document = new Document(PageSize.A4, 50, 50, 50, 50);
			PdfWriter.getInstance(document, mos);
			document.addAuthor("Idegaweb eGov");
			document.addSubject("Case");
			document.open();
			document.newPage();

			String title = iwrb.getLocalizedString("case_overview", "Case overview");
			Paragraph cTitle = new Paragraph(title, titleFont);
			cTitle.setSpacingAfter(24);
			document.setPageCount(1);
			document.add(cTitle);

			int[] widths = { 25, 75 };
			PdfPTable table = new PdfPTable(2);
			table.setWidths(widths);
			table.getDefaultCell().setBorder(0);
			table.getDefaultCell().setPaddingBottom(8);

			CaseCategory category = theCase.getCaseCategory();
			CaseCategory parentCategory = category.getParent();
			CaseType type = theCase.getCaseType();
			User user = theCase.getOwner();
			Address address = user != null ? getUserBusiness(iwc).getUsersMainAddress(user) : null;
			PostalCode postal = null;
			if (address != null) {
				postal = address.getPostalCode();
			}
			Phone phone = null;
			if (user != null) {
				try {
					phone = getUserBusiness(iwc).getUsersHomePhone(user);
				}
				catch (NoPhoneFoundException e) {
					//No phone found...
				}
			}
			Email email = null;
			if (user != null) {
				try {
					email = getUserBusiness(iwc).getUsersMainEmail(user);
				}
				catch (NoEmailFoundException e) {
					//No email found...
				}
			}

			IWTimestamp created = new IWTimestamp(theCase.getCreated());

			if (user != null) {
				table.addCell(new Phrase(iwrb.getLocalizedString("name", "Name"), labelFont));
				table.addCell(new Phrase(new Name(user.getFirstName(), user.getMiddleName(), user.getLastName()).getName(locale), textFont));

				table.addCell(new Phrase(iwrb.getLocalizedString("personal_id", "Personal ID"), labelFont));
				table.addCell(new Phrase(PersonalIDFormatter.format(user.getPersonalID(), locale), textFont));

				table.addCell(new Phrase(iwrb.getLocalizedString("address", "Address"), labelFont));
				table.addCell(new Phrase(address != null ? address.getStreetAddress() : "-", textFont));

				table.addCell(new Phrase(iwrb.getLocalizedString("zip_code", "Postal code"), labelFont));
				table.addCell(new Phrase(postal != null ? postal.getPostalAddress() : "-", textFont));

				table.addCell(new Phrase(iwrb.getLocalizedString("home_phone", "Home phone"), labelFont));
				table.addCell(new Phrase(phone != null ? phone.getNumber() : "-", textFont));

				table.addCell(new Phrase(iwrb.getLocalizedString("email", "Email"), labelFont));
				table.addCell(new Phrase(email != null ? email.getEmailAddress() : "-", textFont));

				table.addCell(new Phrase(""));
				table.addCell(new Phrase(""));
			}

			table.addCell(new Phrase(iwrb.getLocalizedString("case_nr", "Case nr."), labelFont));
			table.addCell(new Phrase(theCase.getPrimaryKey().toString(), textFont));

			if (getCasesBusiness(iwc).useTypes()) {
				table.addCell(new Phrase(iwrb.getLocalizedString("case_type", "Case type"), labelFont));
				table.addCell(new Phrase(type.getName(), textFont));
			}

			if (parentCategory != null) {
				table.addCell(new Phrase(iwrb.getLocalizedString("case_category", "Case category"), labelFont));
				table.addCell(new Phrase(parentCategory.getLocalizedCategoryName(locale), textFont));

				table.addCell(new Phrase(iwrb.getLocalizedString("sub_case_category", "Case category"), labelFont));
				table.addCell(new Phrase(category.getLocalizedCategoryName(locale), textFont));
			}
			else {
				table.addCell(new Phrase(iwrb.getLocalizedString("case_category", "Case category"), labelFont));
				table.addCell(new Phrase(category.getLocalizedCategoryName(locale), textFont));
			}

			table.addCell(new Phrase(iwrb.getLocalizedString("created_date", "Created date"), labelFont));
			table.addCell(new Phrase(created.getLocaleDateAndTime(locale, IWTimestamp.SHORT, IWTimestamp.SHORT), textFont));

			if (theCase.getSubject() != null) {
				table.addCell(new Phrase(iwrb.getLocalizedString("created_date", "Created date"), labelFont));
				table.addCell(new Phrase(theCase.getSubject(), textFont));
			}

			table.addCell(new Phrase(iwrb.getLocalizedString("message", "Message"), labelFont));
			table.addCell(new Phrase(theCase.getMessage(), textFont));

			table.setWidthPercentage(100);
			document.add(table);

			document.close();
			try {
				mos.close();
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}

			buffer.setMimeType("application/pdf");
			return buffer;

		}
		catch (Exception ex) {
			ex.printStackTrace();
		}

		return null;
	}

	protected CasesBusiness getCasesBusiness(IWApplicationContext iwac) {
		try {
			return (CasesBusiness) IBOLookup.getServiceInstance(iwac, CasesBusiness.class);
		}
		catch (RemoteException e) {
			throw new IBORuntimeException(e.getMessage());
		}
	}

	protected UserBusiness getUserBusiness(IWApplicationContext iwac) {
		try {
			return (UserBusiness) IBOLookup.getServiceInstance(iwac, UserBusiness.class);
		}
		catch (IBOLookupException ile) {
			throw new IBORuntimeException(ile);
		}
	}
}