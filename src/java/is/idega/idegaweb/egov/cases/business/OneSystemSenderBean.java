package is.idega.idegaweb.egov.cases.business;

import is.idega.idegaweb.egov.cases.data.GeneralCase;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URLEncoder;
import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;

import se.idega.idegaweb.commune.care.data.ChildCareApplication;

import com.idega.block.process.business.CaseBusiness;
import com.idega.block.process.data.CaseLog;
import com.idega.business.IBOServiceBean;
import com.idega.core.contact.data.Email;
import com.idega.core.contact.data.Phone;
import com.idega.core.contact.data.PhoneTypeBMPBean;
import com.idega.core.location.data.Address;
import com.idega.core.location.data.PostalCode;
import com.idega.io.MemoryFileBuffer;
import com.idega.io.MemoryOutputStream;
import com.idega.user.data.User;
import com.idega.util.IWTimestamp;
import com.idega.xml.XMLDocument;
import com.idega.xml.XMLElement;
import com.idega.xml.XMLOutput;
import com.lowagie.text.Document;


public class OneSystemSenderBean extends IBOServiceBean implements Runnable {
	protected String URL = "http://one.kerfisveita.is/datareceiver/saveapplication.aspx";

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
	//private static final String XML_FILE_SIZE = "file_size";

	
	private ChildCareApplication application = null;
	
	private GeneralCase genCase = null;

	public void run() {
		System.out.println("running one system thread");
		if (application != null) {
			sendChildCareApplication();
		} else if (genCase != null) {
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
		System.out.println("one system childcare application");
		String xml = createChildcareFile();
		System.out.println("one system childcare file created");		
		sendFile(xml);
		System.out.println("one system childcare file sent");		
	}
	
	private void sendGeneralCase() {
		System.out.println("one system general case");
		String xml = createGeneralCaseFile();
		System.out.println("one system general file created");		
		sendFile(xml);
		System.out.println("one system general file sent");		
	}
	
	private String createGeneralCaseFile() {
		String outputString = "nothing";

		File xmlFile = null;
		try {
			xmlFile = File.createTempFile("testOS", "xml");
			xmlFile = File.createTempFile("testOS", ".xml");

			Document document = new Document();
			MemoryFileBuffer buffer = new MemoryFileBuffer();
			MemoryOutputStream mos = new MemoryOutputStream(buffer);

			XMLDocument doc = new XMLDocument(new XMLElement(XML_CASE));

			Collection col = getCaseBusiness().getCaseLogsByCase(genCase);
			IWTimestamp lastStamp = null;
			if (col != null) {
				Iterator it = col.iterator();
				while (it.hasNext()) {
					CaseLog log = (CaseLog) it.next();
					Timestamp t = log.getTimeStamp();
					if (t != null) {
						IWTimestamp logStamp = new IWTimestamp(t);
						if (lastStamp == null || logStamp.isLaterThan(lastStamp)) {
							lastStamp = logStamp;
						}
					}
				}
			}
			
			XMLElement case_ = doc.getRootElement();
			case_.addContent(XML_ID, "-1");
			case_.addContent(XML_EXTERNAL_ID, ((Integer)genCase.getPrimaryKey()).toString());
			case_.addContent(XML_CREATED, new IWTimestamp(genCase.getCreated()).getDateString("yyyy-MM-dd hh:mm:ss"));
			case_.addContent(XML_CODE, genCase.getCaseType().getName());
			case_.addContent(XML_CATEGORY, ((Integer)genCase.getCaseCategory().getPrimaryKey()).toString());
			if (lastStamp != null) {
				case_.addContent(XML_MODIFIED, lastStamp.getDateString("yyyy-MM-dd hh:mm:ss"));
			} else {
				case_.addContent(XML_MODIFIED, "");				
			}
			case_.addContent(XML_STATUS, genCase.getStatus());
			case_.addContent(XML_SUBJECT, genCase.getSubject());
			case_.addContent(XML_BODY, genCase.getBody());

			
			XMLElement file = new XMLElement(XML_FILE_DATA);
			case_.addContent(file);
			
			/*int length = buffer.buffer().length;
			case_.addContent(XML_FILE_SIZE, Integer.toString(length));
			file.addContent(new XMLCDATA(Base64.encode(buffer.buffer())));*/

			XMLElement owner = new XMLElement(XML_OWNER);
			case_.addContent(owner);

			User uOwner = genCase.getOwner();
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
			
			FileOutputStream out = new FileOutputStream(xmlFile);

			XMLOutput output = new XMLOutput(" ", true);
			output.setLineSeparator(System.getProperty("line.separator"));
			output.setTextNormalize(true);
			output.setEncoding("UTF-8");
			output.output(doc, out);

			out.close();

			try {
				output = new XMLOutput();
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

	private String createChildcareFile() {

		String outputString = "nothing";

		File xmlFile = null;
		try {
			xmlFile = File.createTempFile("testOS", "xml");
			xmlFile = File.createTempFile("testOS", ".xml");

			//Document document = new Document();
			//MemoryFileBuffer buffer = new MemoryFileBuffer();
			//MemoryOutputStream mos = new MemoryOutputStream(buffer);

			XMLDocument doc = new XMLDocument(new XMLElement(XML_CASE));

			Collection col = getCaseBusiness().getCaseLogsByCase(genCase);
			IWTimestamp lastStamp = null;
			if (col != null) {
				Iterator it = col.iterator();
				while (it.hasNext()) {
					CaseLog log = (CaseLog) it.next();
					Timestamp t = log.getTimeStamp();
					if (t != null) {
						IWTimestamp logStamp = new IWTimestamp(t);
						if (lastStamp == null || logStamp.isLaterThan(lastStamp)) {
							lastStamp = logStamp;
						}
					}
				}
			}

			XMLElement case_ = doc.getRootElement();
			case_.addContent(XML_ID, "-1");
			case_.addContent(XML_EXTERNAL_ID, ((Integer)application.getPrimaryKey()).toString());
			case_.addContent(XML_CREATED, new IWTimestamp(application.getCreated()).getDateString("yyyy-MM-dd hh:mm:ss"));
			case_.addContent(XML_CODE, "Leikskólaumsókn");
			case_.addContent(XML_CATEGORY, "9999");
			if (lastStamp != null) {
				case_.addContent(XML_MODIFIED, lastStamp.getDateString("yyyy-MM-dd hh:mm:ss"));
			} else {
				case_.addContent(XML_MODIFIED, "");				
			}
			case_.addContent(XML_STATUS, application.getStatus());
			case_.addContent(XML_SUBJECT, "Leikskólaumsókn");
			
			XMLElement file = new XMLElement(XML_FILE_DATA);
			case_.addContent(file);
			
			/*int length = buffer.buffer().length;
			case_.addContent(XML_FILE_SIZE, Integer.toString(length));
			file.addContent(new XMLCDATA(Base64.encode(buffer.buffer())));*/

			XMLElement owner = new XMLElement(XML_OWNER);
			case_.addContent(owner);

			User uOwner = application.getOwner();
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
			
			FileOutputStream out = new FileOutputStream(xmlFile);

			XMLOutput output = new XMLOutput(" ", true);
			output.setLineSeparator(System.getProperty("line.separator"));
			output.setTextNormalize(true);
			output.setEncoding("UTF-8");
			output.output(doc, out);

			out.close();

			try {
				output = new XMLOutput();
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

	private void sendFile(String xml) {
		PostMethod authpost = new PostMethod(URL);

		try {
			HttpClient client = new HttpClient();
			
			NameValuePair file = new NameValuePair("xmldata", URLEncoder.encode(xml, "ISO-8859-1"));
			authpost.setRequestBody(new NameValuePair[] { file });

			int status = client.executeMethod(authpost);

			if (status == HttpStatus.SC_OK) {
				// System.out.println("Submit complete, response="
				// + authpost.getResponseBodyAsString());
			} else {
				// System.out.println("Submit failed, response="
				// + HttpStatus.getStatusText(status));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			authpost.releaseConnection();
		}
	}
	
	  public CaseBusiness getCaseBusiness() throws RemoteException{
	  		return (CaseBusiness)getServiceInstance(CaseBusiness.class);
	  }
}