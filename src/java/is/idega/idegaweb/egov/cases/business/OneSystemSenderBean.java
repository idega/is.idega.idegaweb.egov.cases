package is.idega.idegaweb.egov.cases.business;

//import is.idega.block.family.business.FamilyLogic;
import is.idega.block.family.business.FamilyLogic;
import is.idega.block.family.business.NoCustodianFound;
import is.idega.block.family.data.Child;
import is.idega.block.family.data.Custodian;
import is.idega.block.family.data.Relative;
import is.idega.idegaweb.egov.application.business.ApplicationBusiness;
import is.idega.idegaweb.egov.cases.data.GeneralCase;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URLEncoder;
import java.rmi.RemoteException;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.axis.encoding.Base64;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;

import se.idega.idegaweb.commune.care.data.ChildCareApplication;

import com.idega.block.process.business.CaseBusiness;
import com.idega.block.process.data.CaseLog;
import com.idega.block.school.business.SchoolBusiness;
import com.idega.block.school.data.Student;
import com.idega.business.IBOLookupException;
import com.idega.business.IBORuntimeException;
import com.idega.business.IBOServiceBean;
import com.idega.core.contact.data.Email;
import com.idega.core.contact.data.Phone;
import com.idega.core.contact.data.PhoneTypeBMPBean;
import com.idega.core.location.data.Address;
import com.idega.core.location.data.PostalCode;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.io.MemoryFileBuffer;
import com.idega.io.MemoryOutputStream;
import com.idega.user.business.NoEmailFoundException;
import com.idega.user.business.NoPhoneFoundException;
import com.idega.user.business.UserBusiness;
import com.idega.user.data.User;
import com.idega.util.IWTimestamp;
import com.idega.util.LocaleUtil;
import com.idega.util.PersonalIDFormatter;
import com.idega.util.SendMail;
import com.idega.util.text.Name;
import com.idega.xml.XMLCDATA;
import com.idega.xml.XMLDocument;
import com.idega.xml.XMLElement;
import com.idega.xml.XMLOutput;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

public class OneSystemSenderBean extends IBOServiceBean implements Runnable {
	// protected String URL =
	// "http://one.kerfisveita.is/datareceiver/saveapplication.aspx";
	protected String URL = "http://172.29.199.102/datareceiver/saveapplication.aspx";
	
	protected String ONESYSTEM_CASE_SERVICE = "ONESYSTEM_CASESERVICE";


	private static final String XML_CASE = "case";

	private static final String XML_ID = "id";

	private static final String XML_EXTERNAL_ID = "external_id";

	private static final String XML_CREATED = "created";

	private static final String XML_CODE = "code";

	private static final String XML_CATEGORY = "category";

	private static final String XML_MODIFIED = "modified";

	private static final String XML_STATUS = "status";

	private static final String XML_SUBJECT = "subject";

	private static final String XML_BODY = "body";

	private static final String XML_OWNER = "owner";

	private static final String XML_NAME = "name";

	private static final String XML_SSN = "socialsecurity";

	private static final String XML_ADDRESS = "address";

	private static final String XML_CITY = "city";

	private static final String XML_POSTAL_CODE = "postalcode";

	private static final String XML_PHONE = "phone";

	private static final String XML_GSM = "gsm";

	private static final String XML_EMAIL = "email";

	private static final String XML_FILE_DATA = "file_data";

	private static final String XML_FILE_SIZE = "file_size";

	private ChildCareApplication application = null;

	private GeneralCase genCase = null;

	public void run() {
		if (this.application != null) {
			sendChildCareApplication();
		} else if (this.genCase != null) {
			sendGeneralCase();
		}
	}

	public void setChildCareApplication(ChildCareApplication application) {
		this.application = application;
	}

	public void setGeneralCase(GeneralCase genCase) {
		this.genCase = genCase;
	}

	public void setURL(String URL) {
		this.URL = URL;
	}

	private void sendChildCareApplication() {
		String xml = createChildcareFile();
		String ret = sendFile(xml);
		if (ret != null && ret.length() > 36) {
			ret = ret.substring(0, 36);
		}
		this.application.setExternalId(ret);
		this.application.store();
	}

	private void sendGeneralCase() {
		String xml = createGeneralCaseFile();
		String ret = sendFile(xml);
		if (ret != null && ret.length() > 36) {
			ret = ret.substring(0, 36);
		}
		this.genCase.setExternalId(ret);
		this.genCase.store();
	}

	private String createGeneralCaseFile() {
		String outputString = "nothing";

		try {
			XMLDocument doc = new XMLDocument(new XMLElement(XML_CASE));

			Collection col = null;
			try {
				getCaseBusiness().getCaseLogsByCase(this.genCase);
			} catch (Exception e) {
			}

			IWTimestamp lastStamp = null;
			if (col != null) {
				Iterator it = col.iterator();
				while (it.hasNext()) {
					CaseLog log = (CaseLog) it.next();
					Timestamp t = log.getTimeStamp();
					if (t != null) {
						IWTimestamp logStamp = new IWTimestamp(t);
						if (lastStamp == null
								|| logStamp.isLaterThan(lastStamp)) {
							lastStamp = logStamp;
						}
					}
				}
			}

			XMLElement case_ = doc.getRootElement();
			if (this.genCase.getExternalId() != null) {
				case_.addContent(XML_ID, this.genCase.getExternalId());				
			} 
			else {
				case_.addContent(XML_ID, "-1");
			}
			case_.addContent(XML_EXTERNAL_ID, this.genCase.getUniqueId());
			case_.addContent(XML_CREATED, new IWTimestamp(this.genCase.getCreated())
					.getDateString("yyyy-MM-dd hh:mm:ss"));
			case_.addContent(XML_CODE, this.genCase.getCaseType().getName());
			case_.addContent(XML_CATEGORY, ((Integer) this.genCase.getCaseCategory()
					.getPrimaryKey()).toString());
			if (lastStamp != null) {
				case_.addContent(XML_MODIFIED, lastStamp
						.getDateString("yyyy-MM-dd hh:mm:ss"));
			} else {
				case_.addContent(XML_MODIFIED, "");
			}
			case_.addContent(XML_STATUS, this.genCase.getStatus());
			case_.addContent(XML_SUBJECT, this.genCase.getCaseType().getName());
			case_.addContent(XML_BODY, this.genCase.getMessage());

			XMLElement owner = new XMLElement(XML_OWNER);
			case_.addContent(owner);

			User uOwner = this.genCase.getOwner();
			Address address = null;
			PostalCode pCode = null;
			Phone phone = null;
			Phone gsm = null;
			Email email = null;

			if (uOwner.getAddresses() != null) {
				Iterator it = uOwner.getAddresses().iterator();
				if (it.hasNext()) {
					address = (Address) it.next();
					pCode = address.getPostalCode();
				}
			}

			if (uOwner.getPhones() != null) {
				Iterator it = uOwner.getPhones().iterator();
				while (it.hasNext()) {
					Phone p = (Phone) it.next();
					if (p.getPhoneTypeId() == PhoneTypeBMPBean.HOME_PHONE_ID) {
						phone = p;
					} else if (p.getPhoneTypeId() == PhoneTypeBMPBean.MOBILE_PHONE_ID) {
						gsm = p;
					}
				}
			}

			if (uOwner.getEmails() != null) {
				Iterator it = uOwner.getEmails().iterator();
				if (it.hasNext()) {
					email = (Email) it.next();
				}
			}

			owner.addContent(XML_NAME, uOwner.getName());
			owner.addContent(XML_SSN, uOwner.getPersonalID());
			if (address != null) {
				owner.addContent(XML_ADDRESS, address.getStreetAddress());
				owner.addContent(XML_CITY, address.getCity());
			} else {
				owner.addContent(XML_ADDRESS, "");
				owner.addContent(XML_CITY, "");
			}
			if (pCode != null) {
				owner.addContent(XML_POSTAL_CODE, pCode.getPostalCode());
			} else {
				owner.addContent(XML_POSTAL_CODE, "");
			}
			if (phone != null) {
				owner.addContent(XML_PHONE, phone.getNumber());
			} else {
				owner.addContent(XML_PHONE, "");
			}
			if (gsm != null) {
				owner.addContent(XML_GSM, gsm.getNumber());
			} else {
				owner.addContent(XML_GSM, "");
			}
			if (email != null) {
				owner.addContent(XML_EMAIL, email.getEmailAddress());
			} else {
				owner.addContent(XML_EMAIL, "");
			}

			XMLElement file = new XMLElement(XML_FILE_DATA);
			case_.addContent(file);

			/*
			 * Document document = new Document(); MemoryOutputStream mos = new
			 * MemoryOutputStream(buffer);
			 */
			MemoryFileBuffer buffer = new MemoryFileBuffer();

			int length = buffer.buffer().length;
			case_.addContent(XML_FILE_SIZE, Integer.toString(length));
			file.addContent(new XMLCDATA(Base64.encode(buffer.buffer())));

			try {
				XMLOutput output = new XMLOutput();
				output.setLineSeparator(System.getProperty("line.separator"));
				output.setTextNormalize(true);
				output.setEncoding("UTF-8");
				outputString = output.outputString(doc);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return outputString;
	}

	protected String getBundleIdentifier() {
		return "is.idega.idegaweb.egov.childcare";
	}

	private String createChildcareFile() {

		IWBundle iwb = getIWMainApplication().getBundle(getBundleIdentifier());
		IWResourceBundle iwrb = iwb.getResourceBundle(LocaleUtil
				.getIcelandicLocale());

		String outputString = "nothing";

		try {
			XMLDocument doc = new XMLDocument(new XMLElement(XML_CASE));

			Collection col = null;
			try {
				col = getCaseBusiness().getCaseLogsByCase(this.application);
			} catch (Exception e) {
			}

			IWTimestamp lastStamp = null;
			if (col != null) {
				Iterator it = col.iterator();
				while (it.hasNext()) {
					CaseLog log = (CaseLog) it.next();
					Timestamp t = log.getTimeStamp();
					if (t != null) {
						IWTimestamp logStamp = new IWTimestamp(t);
						if (lastStamp == null
								|| logStamp.isLaterThan(lastStamp)) {
							lastStamp = logStamp;
						}
					}
				}
			}

			XMLElement case_ = doc.getRootElement();
			if (this.application.getExternalId() != null) {
				case_.addContent(XML_ID, this.application.getExternalId());				
			}
			else {
				case_.addContent(XML_ID, "-1");
			}
			case_.addContent(XML_EXTERNAL_ID, this.application.getUniqueId());
			case_.addContent(XML_CREATED, new IWTimestamp(this.application
					.getCreated()).getDateString("yyyy-MM-dd hh:mm:ss"));
			case_.addContent(XML_CODE, iwrb.getLocalizedString(
					"childcare_application", "Childcare application"));
			case_.addContent(XML_CATEGORY, "9999");
			if (lastStamp != null) {
				case_.addContent(XML_MODIFIED, lastStamp
						.getDateString("yyyy-MM-dd hh:mm:ss"));
			} else {
				case_.addContent(XML_MODIFIED, "");
			}
			case_.addContent(XML_STATUS, this.application.getStatus());
			case_.addContent(XML_SUBJECT, iwrb.getLocalizedString(
					"childcare_application", "Childcare application"));

			XMLElement owner = new XMLElement(XML_OWNER);
			case_.addContent(owner);

			User uOwner = this.application.getOwner();
			Address address = null;
			PostalCode pCode = null;
			Phone phone = null;
			Phone gsm = null;
			Email email = null;

			if (uOwner.getAddresses() != null) {
				Iterator it = uOwner.getAddresses().iterator();
				if (it.hasNext()) {
					address = (Address) it.next();
					pCode = address.getPostalCode();
				}
			}

			if (uOwner.getPhones() != null) {
				Iterator it = uOwner.getPhones().iterator();
				while (it.hasNext()) {
					Phone p = (Phone) it.next();
					if (p.getPhoneTypeId() == PhoneTypeBMPBean.HOME_PHONE_ID) {
						phone = p;
					} else if (p.getPhoneTypeId() == PhoneTypeBMPBean.MOBILE_PHONE_ID) {
						gsm = p;
					}
				}
			}

			if (uOwner.getEmails() != null) {
				Iterator it = uOwner.getEmails().iterator();
				if (it.hasNext()) {
					email = (Email) it.next();
				}
			}

			owner.addContent(XML_NAME, uOwner.getName());
			owner.addContent(XML_SSN, uOwner.getPersonalID());
			if (address != null) {
				owner.addContent(XML_ADDRESS, address.getStreetAddress());
				owner.addContent(XML_CITY, address.getCity());
			} else {
				owner.addContent(XML_ADDRESS, "");
				owner.addContent(XML_CITY, "");
			}
			if (pCode != null) {
				owner.addContent(XML_POSTAL_CODE, pCode.getPostalCode());
			} else {
				owner.addContent(XML_POSTAL_CODE, "");
			}
			if (phone != null) {
				owner.addContent(XML_PHONE, phone.getNumber());
			} else {
				owner.addContent(XML_PHONE, "");
			}
			if (gsm != null) {
				owner.addContent(XML_GSM, gsm.getNumber());
			} else {
				owner.addContent(XML_GSM, "");
			}
			if (email != null) {
				owner.addContent(XML_EMAIL, email.getEmailAddress());
			} else {
				owner.addContent(XML_EMAIL, "");
			}

			XMLElement file = new XMLElement(XML_FILE_DATA);
			case_.addContent(file);

			MemoryFileBuffer buffer = createPDF();

			if (buffer != null && buffer.buffer() != null) {
				int length = buffer.buffer().length;
				case_.addContent(XML_FILE_SIZE, Integer.toString(length));
				file.addContent(new XMLCDATA(Base64.encode(buffer.buffer())));
			}

			try {
				XMLOutput output = new XMLOutput();
				output.setLineSeparator(System.getProperty("line.separator"));
				output.setTextNormalize(true);
				output.setEncoding("UTF-8");
				outputString = output.outputString(doc);
			} catch (Exception e) {
				e.printStackTrace();
			}

			// messagebox_smtp_mailserver
			File xmlFile = File.createTempFile("testOS", ".xml");

			FileOutputStream out = new FileOutputStream(xmlFile);

			XMLOutput output = new XMLOutput(" ", true);
			output.setLineSeparator(System.getProperty("line.separator"));
			output.setTextNormalize(true);
			output.setEncoding("UTF-8");
			output.output(doc, out);

			out.close();

			String from = getIWApplicationContext().getApplicationSettings().getProperty("messagebox_from_mailaddress");
			String mailserver = getIWApplicationContext().getApplicationSettings().getProperty("messagebox_smtp_mailserver");
			
			System.out.println("from = " + from);
			System.out.println("mailserver = " + mailserver);
			if (outputString == null) {
				System.out.println("outputstring is null");
			}
			if (xmlFile == null) {
				System.out.println("xmlFile is null");
			}
			
			if (from == null) {
				from = "arborg@sunnan3.is";
			}
			
			if (mailserver == null) {
				mailserver = "ns1.anza.is";
			}
			
			SendMail.send(from, "palli@idega.is", null, null, mailserver,
					"test", outputString, xmlFile);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return outputString;
	}

	private String sendFile(String xml) {
		URL = this.getIWApplicationContext().getApplicationSettings().getProperty(ONESYSTEM_CASE_SERVICE, URL);

		PostMethod authpost = new PostMethod(this.URL);

		String ret = "-1";
		try {
			HttpClient client = new HttpClient();

			NameValuePair file = new NameValuePair("xmldata", URLEncoder
					.encode(xml, "ISO-8859-1"));
			authpost.setRequestBody(new NameValuePair[] { file });

			int status = client.executeMethod(authpost);

			if (status == HttpStatus.SC_OK) {
				ret = authpost.getResponseBodyAsString();
				// System.out.println("Submit complete, response="
				// + authpost.getResponseBodyAsString());
			} else {
				ret = "-1";
				// System.out.println("Submit failed, response="
				// + HttpStatus.getStatusText(status));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			authpost.releaseConnection();
		}

		return ret;
	}

	public CaseBusiness getCaseBusiness() throws RemoteException {
		return (CaseBusiness) getServiceInstance(CaseBusiness.class);
	}

	private MemoryFileBuffer createPDF() {
		Font titleFont = new Font(Font.HELVETICA, 14, Font.BOLD);
		Font bigFont = new Font(Font.HELVETICA, 12, Font.NORMAL);
		Font paraFont = new Font(Font.HELVETICA, 11, Font.BOLD);
		Font tagFont = new Font(Font.HELVETICA, 10, Font.BOLD);
		Font textFont = new Font(Font.HELVETICA, 10, Font.NORMAL);

		IWBundle iwb = getIWMainApplication().getBundle(getBundleIdentifier());
		IWResourceBundle iwrb = iwb.getResourceBundle(LocaleUtil
				.getIcelandicLocale());

		try {
			MemoryFileBuffer buffer = new MemoryFileBuffer();
			MemoryOutputStream mos = new MemoryOutputStream(buffer);

			Document document = new Document(PageSize.A4, 50, 50, 50, 50);
			PdfWriter.getInstance(document, mos);
			document.addAuthor("Idegaweb eGov");
			document.addSubject("Case");
			document.open();
			document.newPage();
			String title = iwrb.getLocalizedString("childcare_application",
					"Childcare application");
			Paragraph cTitle = new Paragraph(title, titleFont);
			document.setPageCount(1);
			document.add(cTitle);
			document.add(new Phrase(""));
			PdfPTable table = new PdfPTable(2);
			table.getDefaultCell().setBorder(0);
			User child = this.application.getChild();
			Address address = getUserBusiness().getUsersMainAddress(child);
			PostalCode postal = null;
			if (address != null) {
				postal = address.getPostalCode();
			}

			table.addCell(new Phrase(child.getName(), bigFont));
			table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
			table
					.addCell(new Phrase(PersonalIDFormatter.format(child
							.getPersonalID(), LocaleUtil.getIcelandicLocale()),
							bigFont));
			table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
			if (address != null) {
				table.addCell(new Phrase(address.getStreetAddress(), textFont));
			} else {
				table.addCell("");
			}
			table.addCell("");
			if (postal != null) {
				table.addCell(new Phrase(postal.getPostalAddress(), textFont));
			} else {
				table.addCell(new Phrase(""));
			}
			table.addCell(new Phrase(""));

			table.setWidthPercentage(100);
			document.add(table);

			document.add(new Phrase("\n"));
			document.add(new Phrase(iwrb.getLocalizedString(
					"application.chosen_provider_information",
					"Provider choice information"), paraFont));

			PdfPTable table2 = new PdfPTable(4);
			table2.getDefaultCell().setBorder(0);
			table2.addCell(new Phrase(iwrb.getLocalizedString(
					"application.provider", "Provider")
					+ " " + this.application.getChoiceNumber(), tagFont));
			table2.addCell(new Phrase(this.application.getProvider().getName(),
					textFont));
			table2.addCell(new Phrase(""));
			table2.addCell(new Phrase(""));
			
			if (this.application.getFee() > 0) {
				table2.addCell(new Phrase(""));
				table2.addCell(new Phrase(""));
				table2.addCell(new Phrase(""));
				table2.addCell(new Phrase(""));

				NumberFormat format = NumberFormat.getCurrencyInstance(LocaleUtil.getIcelandicLocale());
				
				table2.addCell(new Phrase(iwrb.getLocalizedString("application.fee", "Fee"), tagFont));
				table2.addCell(new Phrase(format.format(this.application.getFee()), textFont));
				table2.addCell(new Phrase(""));
				table2.addCell(new Phrase(""));
				table2.addCell(new Phrase(""));
				table2.addCell(new Phrase(""));
				table2.addCell(new Phrase(""));
				table2.addCell(new Phrase(""));
			}
			
			table2.addCell(new Phrase(iwrb.getLocalizedString(
					"application.from_date", "From date"), tagFont));
			table2.addCell(new Phrase(
					new IWTimestamp(this.application.getFromDate())
							.getDateString("dd.MM.yyyy"), textFont));
			table2.addCell(new Phrase(""));
			table2.addCell(new Phrase(""));
			table2.addCell(new Phrase(iwrb.getLocalizedString(
					"application.message", "Message"), tagFont));
			table2.addCell(new Phrase(""));
			table2.addCell(new Phrase(""));
			table2.addCell(new Phrase(""));
			table2.getDefaultCell().setColspan(4);
			if (this.application.getMessage() != null) {
				table2.addCell(new Phrase(this.application.getMessage(), textFont));
			} else {
				table2.addCell(new Phrase(""));
			}

			table2.setWidthPercentage(100);
			document.add(table2);

			document.add(new Phrase("\n"));
			document.add(new Phrase(iwrb.getLocalizedString(
					"application.from_to_time_information",
					"From/To time information"), paraFont));
			PdfPTable table3 = new PdfPTable(4);
			table3.getDefaultCell().setBorder(0);
			table3.addCell(new Phrase(iwrb.getLocalizedString(
					"application.from_time", "From time"), tagFont));
			IWTimestamp from = null;
			if (this.application.getFromTime() != null) {
				from = new IWTimestamp(this.application.getFromTime());
			}
			IWTimestamp to = null;
			if (this.application.getToTime() != null) {
				to = new IWTimestamp(this.application.getToTime());
			}

			if (from != null) {
				table3
						.addCell(new Phrase(from.getDateString("hh:mm"),
								textFont));
			} else {
				table3.addCell(new Phrase(""));
			}
			table3.addCell(new Phrase(""));
			table3.addCell(new Phrase(""));
			table3.addCell(new Phrase(iwrb.getLocalizedString(
					"application.to_time", "To time"), tagFont));
			if (to != null) {
				table3.addCell(new Phrase(to.getDateString("hh:mm"), textFont));
			} else {
				table3.addCell(new Phrase(""));
			}
			table3.addCell(new Phrase(""));
			table3.addCell(new Phrase(""));

			table3.setWidthPercentage(100);
			document.add(table3);
		 	
			Child cChild = getMemberFamilyLogic().getChild(child);

			Collection custodians = null;
			try {
				custodians = cChild.getCustodians();
			} catch (NoCustodianFound ncf) {
				custodians = new ArrayList();
			}
			Custodian extraCustodian = cChild.getExtraCustodian();
			if (extraCustodian != null) {
				custodians.add(extraCustodian);
			}

			if (!custodians.isEmpty()) {

				int i = 0;
				Address userAddress[] = new Address[3];
				String userPhone[] = new String[3];
				String userWork[] = new String[3];
				String userMobile[] = new String[3];
				String userEmail[] = new String[3];
				String userNationality[] = new String[3];
				String userMaritalStatus[] = new String[3];
				String relationString[] = new String[3];
				String userName[] = new String[3];
				String userPersonalID[] = new String[3];
				String userStreetName[] = new String[3];
				String userPostalCode[] = new String[3];

				for (int j = 0; j < 3; j++) {
					userPhone[j] = "";
					userWork[j] = "";
					userMobile[j] = "";
					userEmail[j] = "";
					userNationality[j] = "";
					userMaritalStatus[j] = "";
					relationString[j] = "";
					userName[j] = "";
					userPersonalID[j] = "";
					userStreetName[j] = "";
					userPostalCode[j] = "";
				}

				Iterator iter = custodians.iterator();
				while (iter.hasNext() && i < 3) {
					Custodian custodian = (Custodian) iter.next();

					userAddress[i] = getUserBusiness().getUsersMainAddress(
							custodian);
					userNationality[i] = custodian.getNationality() == null ? ""
							: custodian.getNationality();
					userMaritalStatus[i] = custodian.getMaritalStatus();
					userStreetName[i] = userAddress[i].getStreetAddress();
					userPostalCode[i] = userAddress[i].getPostalAddress();

					try {
						Phone phone = getUserBusiness().getUsersHomePhone(
								custodian);
						if (phone != null && phone.getNumber() != null) {
							userPhone[i] = phone.getNumber();
						}
					} catch (NoPhoneFoundException npfe) {
						userPhone[i] = "";
					}

					try {
						Phone phone = getUserBusiness().getUsersWorkPhone(
								custodian);
						if (phone != null && phone.getNumber() != null) {
							userWork[i] = phone.getNumber();
						}
					} catch (NoPhoneFoundException npfe) {
						userWork[i] = "";
					}

					try {
						Phone phone = getUserBusiness().getUsersMobilePhone(
								custodian);
						if (phone != null && phone.getNumber() != null) {
							userMobile[i] = phone.getNumber();
						}
					} catch (NoPhoneFoundException npfe) {
						userMobile[i] = "";
					}

					try {
						Email email = getUserBusiness().getUsersMainEmail(
								custodian);
						if (email != null && email.getEmailAddress() != null) {
							userEmail[i] = email.getEmailAddress();
						}
					} catch (NoEmailFoundException nefe) {
						userEmail[i] = "";
					}

					relationString[i] = iwrb.getLocalizedString("relation."
							+ cChild.getRelation(custodian));

					Name custodianName = new Name(custodian.getFirstName(),
							custodian.getMiddleName(), custodian.getLastName());
					userName[i] = custodianName.getName(LocaleUtil
							.getIcelandicLocale());

					userPersonalID[i] = PersonalIDFormatter.format(custodian
							.getPersonalID(), LocaleUtil.getIcelandicLocale());

					if (userMaritalStatus[i] != null) {
						userMaritalStatus[i] = iwrb
								.getLocalizedString("marital_status."
										+ userMaritalStatus[i]);
					} else {
						userMaritalStatus[i] = "";
					}

					i++;
				}

				document.add(new Phrase("\n"));
				document.add(new Phrase(iwrb.getLocalizedString(
						"application.custodian_information",
						"Custodian information"), paraFont));
				PdfPTable table4 = new PdfPTable(4);
				table4.getDefaultCell().setBorder(0);
				table4.addCell(new Phrase(iwrb.getLocalizedString("relation",
						"Relation"), tagFont));
				table4.addCell(new Phrase(relationString[0], textFont));
				table4.addCell(new Phrase(relationString[1], textFont));
				table4.addCell(new Phrase(relationString[2], textFont));
				table4.addCell(new Phrase(iwrb
						.getLocalizedString("name", "Name"), tagFont));
				table4.addCell(new Phrase(userName[0], textFont));
				table4.addCell(new Phrase(userName[1], textFont));
				table4.addCell(new Phrase(userName[2], textFont));
				table4.addCell(new Phrase(iwrb.getLocalizedString(
						"personal_id", "Personal ID"), tagFont));
				table4.addCell(new Phrase(userPersonalID[0], textFont));
				table4.addCell(new Phrase(userPersonalID[1], textFont));
				table4.addCell(new Phrase(userPersonalID[2], textFont));
				table4.addCell(new Phrase(iwrb.getLocalizedString("address",
						"Address"), tagFont));
				table4.addCell(new Phrase(userStreetName[0],
						textFont));
				table4.addCell(new Phrase(userStreetName[1],
						textFont));
				table4.addCell(new Phrase(userStreetName[2],
						textFont));
				table4.addCell(new Phrase(iwrb.getLocalizedString("zip_code",
						"Zip code"), tagFont));
				table4.addCell(new Phrase(userPostalCode[0],
						textFont));
				table4.addCell(new Phrase(userPostalCode[1],
						textFont));
				table4.addCell(new Phrase(userPostalCode[2],
						textFont));
				table4.addCell(new Phrase(iwrb.getLocalizedString("home_phone",
						"Home phone"), tagFont));
				table4.addCell(new Phrase(userPhone[0], textFont));
				table4.addCell(new Phrase(userPhone[1], textFont));
				table4.addCell(new Phrase(userPhone[2], textFont));
				table4.addCell(new Phrase(iwrb.getLocalizedString("work_phone",
						"Work phone"), tagFont));
				table4.addCell(new Phrase(userWork[0], textFont));
				table4.addCell(new Phrase(userWork[1], textFont));
				table4.addCell(new Phrase(userWork[2], textFont));
				table4.addCell(new Phrase(iwrb.getLocalizedString(
						"mobile_phone", "Mobile phone"), tagFont));
				table4.addCell(new Phrase(userMobile[0], textFont));
				table4.addCell(new Phrase(userMobile[1], textFont));
				table4.addCell(new Phrase(userMobile[2], textFont));
				table4.addCell(new Phrase(iwrb.getLocalizedString("email",
						"E-mail"), tagFont));
				table4.addCell(new Phrase(userEmail[0], textFont));
				table4.addCell(new Phrase(userEmail[1], textFont));
				table4.addCell(new Phrase(userEmail[2], textFont));
				table4.addCell(new Phrase(iwrb.getLocalizedString(
						"nationality", "Nationality"), tagFont));
				table4.addCell(new Phrase(userNationality[0], textFont));
				table4.addCell(new Phrase(userNationality[1], textFont));
				table4.addCell(new Phrase(userNationality[2], textFont));
				table4.addCell(new Phrase(iwrb.getLocalizedString(
						"marital_status", "Marital status"), tagFont));
				table4.addCell(new Phrase(userMaritalStatus[0], textFont));
				table4.addCell(new Phrase(userMaritalStatus[1], textFont));
				table4.addCell(new Phrase(userMaritalStatus[2], textFont));

				table4.setWidthPercentage(100);
				document.add(table4);
			}

			Collection relatives = cChild.getRelatives();
			if (!relatives.isEmpty()) {
				int i = 0;

				String userPhone[] = new String[2];
				String userWork[] = new String[2];
				String userMobile[] = new String[2];
				String userEmail[] = new String[2];
				String relationString[] = new String[2];
				String userName[] = new String[2];
				
				for (int j = 0; j < 2; j++) {
					userPhone[j] = "";
					userWork[j] = "";
					userMobile[j] = "";
					userEmail[j] = "";
					relationString[j] = "";
					userName[j] = "";					
				}

				Iterator iter = relatives.iterator();
				while (iter.hasNext() && i < 2) {
					Relative relative = (Relative) iter.next();

					userName[i] = relative.getName();

					if (relative.getHomePhone() != null) {
						userPhone[i] = relative.getHomePhone();
					} else {
						userPhone[i] = "";
					}

					if (relative.getWorkPhone() != null) {
						userWork[i] = relative.getWorkPhone();
					} else {
						userWork[i] = "";
					}

					if (relative.getMobilePhone() != null) {
						userMobile[i] = relative.getMobilePhone();
					} else {
						userMobile[i] = "";
					}

					if (relative.getEmail() != null) {
						userEmail[i] = relative.getEmail();
					} else {
						userEmail[i] = "";
					}

					relationString[i] = iwrb.getLocalizedString("relation."
							+ relative.getRelation(), "");
					i++;
				}

				document.add(new Phrase("\n"));
				document.add(new Phrase(iwrb.getLocalizedString(
						"application.relative_information",
						"Relative information"), paraFont));
				PdfPTable table5 = new PdfPTable(4);
				table5.getDefaultCell().setBorder(0);
				table5.addCell(new Phrase(iwrb.getLocalizedString("relation",
						"Relation"), tagFont));
				table5.addCell(new Phrase(relationString[0], textFont));
				table5.addCell(new Phrase(relationString[1], textFont));
				table5.addCell(new Phrase(""));
				table5.addCell(new Phrase(iwrb.getLocalizedString("name",
						"Name"), tagFont));
				table5.addCell(new Phrase(userName[0], textFont));
				table5.addCell(new Phrase(userName[1], textFont));
				table5.addCell(new Phrase(""));
				table5.addCell(new Phrase(iwrb.getLocalizedString("home_phone",
						"Home phone"), tagFont));
				table5.addCell(new Phrase(userPhone[0], textFont));
				table5.addCell(new Phrase(userPhone[1], textFont));
				table5.addCell(new Phrase(""));
				table5.addCell(new Phrase(iwrb.getLocalizedString("work_phone",
						"Work phone"), tagFont));
				table5.addCell(new Phrase(userWork[0], textFont));
				table5.addCell(new Phrase(userWork[1], textFont));
				table5.addCell(new Phrase(""));
				table5.addCell(new Phrase(iwrb.getLocalizedString(
						"mobile_phone", "Mobile phone"), tagFont));
				table5.addCell(new Phrase(userMobile[0], textFont));
				table5.addCell(new Phrase(userMobile[1], textFont));
				table5.addCell(new Phrase(""));
				table5.addCell(new Phrase(iwrb.getLocalizedString("email",
						"E-mail"), tagFont));
				table5.addCell(new Phrase(userEmail[0], textFont));
				table5.addCell(new Phrase(userEmail[1], textFont));
				table5.addCell(new Phrase(""));

				table5.setWidthPercentage(100);
				document.add(table5);
			}

			document.newPage();

			boolean hasMultiLanguageHome = cChild.hasMultiLanguageHome();
			String language = cChild.getLanguage();

			Custodian custodian = getMemberFamilyLogic().getCustodian(
					this.application.getOwner());
			boolean hasStudies = custodian.hasStudies();
			String studies = custodian.getStudies();
			Date studyStart = custodian.getStudyStart();
			Date studyEnd = custodian.getStudyEnd();

			Boolean hasGrowthDeviation = cChild.hasGrowthDeviation();
			String growthDeviation = cChild.getGrowthDeviationDetails();
			Boolean hasAllergies = cChild.hasAllergies();
			String allergies = cChild.getAllergiesDetails();
			Student student = getSchoolBusiness().getStudent(child);
			String lastCareProvider = student.getLastProvider();
			boolean canContactLastProvider = student.canContactLastProvider();
			boolean canDisplayImages = student.canDisplayImages();
			String otherInformation = student.getChildCareOtherInformation();
			boolean hasCaretaker = student.hasCaretaker();

			document.add(new Phrase(iwrb.getLocalizedString(
					"child.child_information", "Child information"), paraFont));
			PdfPTable table6 = new PdfPTable(1);
			table6.getDefaultCell().setBorder(0);

			table6.addCell(new Phrase(iwrb.getLocalizedString(
					"child.has_multi_language_home_overview",
					"Has multi language home"), tagFont));
			StringBuffer multiLangBuffer = new StringBuffer(
					getBooleanValue(hasMultiLanguageHome));
			if (hasMultiLanguageHome) {
				if (language != null) {
					multiLangBuffer.append(", ");
					multiLangBuffer.append(language);
				}
			}
			table6.addCell(new Phrase(multiLangBuffer.toString(), textFont));
			table6.addCell(new Phrase(""));

			if (hasStudies) {
				table6.addCell(new Phrase(iwrb.getLocalizedString(
						"custodian.has_studies_overview", "Has studies"),
						tagFont));

				StringBuffer studiesBuffer = new StringBuffer();
				if (studies != null) {
					studiesBuffer.append(studies);
				}
				if (studyStart != null && studyEnd != null) {
					studiesBuffer.append(" (");
					studiesBuffer.append(new IWTimestamp(studyStart)
							.getDateString("dd.MM.yyyy"));
					studiesBuffer.append(" - ");
					studiesBuffer.append(new IWTimestamp(studyEnd)
							.getDateString("dd.MM.yyyy"));
					studiesBuffer.append(")");
				} else if (studyStart != null) {
					studiesBuffer.append(" (");
					studiesBuffer.append(new IWTimestamp(studyStart)
							.getDateString("dd.MM.yyyy"));
					studiesBuffer.append(" - ????)");
				} else if (studyEnd != null) {
					studiesBuffer.append(" (???? - ");
					studiesBuffer.append(new IWTimestamp(studyEnd)
							.getDateString("dd.MM.yyyy"));
					studiesBuffer.append(")");
				}

				table6.addCell(new Phrase(studiesBuffer.toString(), textFont));
				table6.addCell(new Phrase(""));
			}

			table6.addCell(new Phrase(iwrb.getLocalizedString(
					"child.has_growth_deviation_overview",
					"Has growth deviation"), tagFont));
			StringBuffer growthDeviationBuffer = new StringBuffer(
					getBooleanValue(hasGrowthDeviation));
			if (hasGrowthDeviation != null) {
				if (growthDeviation != null) {
					growthDeviationBuffer.append(", ");
					growthDeviationBuffer.append(growthDeviation);
				}
			}
			table6.addCell(new Phrase(growthDeviationBuffer.toString(),
					textFont));
			table6.addCell(new Phrase(""));

			table6.addCell(new Phrase(iwrb.getLocalizedString(
					"child.has_allergies_overview", "Has allergies"), tagFont));
			StringBuffer allergyBuffer = new StringBuffer(
					getBooleanValue(hasAllergies));
			if (hasAllergies != null) {
				if (allergies != null) {
					allergyBuffer.append(", ");
					allergyBuffer.append(allergies);
				}
			}

			table6.addCell(new Phrase(allergyBuffer.toString(), textFont));
			table6.addCell(new Phrase(""));

			table6.addCell(new Phrase(iwrb.getLocalizedString(
					"child.has_caretaker_overview", "Has caretaker"), tagFont));
			table6.addCell(new Phrase(getBooleanValue(hasCaretaker), textFont));
			table6.addCell(new Phrase(""));

			if (lastCareProvider != null) {
				table6.addCell(new Phrase(iwrb.getLocalizedString(
						"child.last_care_provider_overview",
						"Last care provider"), tagFont));
				table6.addCell(new Phrase(lastCareProvider, textFont));
				table6.addCell(new Phrase(""));
			}

			table6.addCell(new Phrase(iwrb.getLocalizedString(
					"child.can_contact_last_care_provider_overview",
					"Can contact last care provider"), tagFont));
			table6.addCell(new Phrase(getBooleanValue(canContactLastProvider),
					textFont));
			table6.addCell(new Phrase(""));

			table6.addCell(new Phrase(iwrb.getLocalizedString(
					"child.can_display_images_overview", "Can display images"),
					tagFont));
			table6.addCell(new Phrase(getBooleanValue(canDisplayImages),
					textFont));
			table6.addCell(new Phrase(""));

			if (otherInformation != null) {
				table6.addCell(new Phrase(iwrb.getLocalizedString(
						"child.other_information", "Other information"),
						tagFont));
				table6.addCell(new Phrase(otherInformation, textFont));
				table6.addCell(new Phrase(""));
			}

			table6.setWidthPercentage(100);
			document.add(table6);

			document.close();
			try {
				mos.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			return buffer;

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return null;
	}

	protected String getBooleanValue(boolean booleanValue) {
		return getBooleanValue(new Boolean(booleanValue));
	}

	protected String getBooleanValue(Boolean booleanValue) {
		IWBundle iwb = getIWMainApplication().getBundle(getBundleIdentifier());
		IWResourceBundle iwrb = iwb.getResourceBundle(LocaleUtil
				.getIcelandicLocale());

		if (booleanValue == null) {
			return iwrb.getLocalizedString("no_answer", "Won't answer");
		} else if (booleanValue.booleanValue()) {
			return iwrb.getLocalizedString("yes", "Yes");
		} else {
			return iwrb.getLocalizedString("no", "No");
		}
	}

	protected ApplicationBusiness getApplicationBusiness() {
		try {
			return (ApplicationBusiness) getServiceInstance(ApplicationBusiness.class);
		} catch (IBOLookupException ile) {
			throw new IBORuntimeException(ile);
		}
	}

	private FamilyLogic getMemberFamilyLogic() {
		try {
			return (FamilyLogic) getServiceInstance(FamilyLogic.class);
		} catch (IBOLookupException ile) {
			throw new IBORuntimeException(ile);
		}
	}

	protected UserBusiness getUserBusiness() {
		try {
			return (UserBusiness) getServiceInstance(UserBusiness.class);
		} catch (IBOLookupException ile) {
			throw new IBORuntimeException(ile);
		}
	}

	protected SchoolBusiness getSchoolBusiness() {
		try {
			return (SchoolBusiness) getServiceInstance(SchoolBusiness.class);
		} catch (IBOLookupException ile) {
			throw new IBORuntimeException(ile);
		}
	}
}