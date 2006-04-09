package is.idega.idegaweb.egov.cases.business;

import is.idega.idegaweb.egov.cases.data.GeneralCase;

import java.net.URL;
import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Iterator;

import com.idega.block.process.business.CaseBusiness;
import com.idega.block.process.data.CaseLog;
import com.idega.block.process.wsclient.Case_NewCase_SoapPortLocator;
import com.idega.block.process.wsclient.Case_NewCase_SoapPortSoap_PortType;
import com.idega.block.process.wsclient.Case_request;
import com.idega.block.process.wsclient.Case_requestItem;
import com.idega.block.process.wsclient.Case_requestOwner;
import com.idega.block.process.wsclient.Case_response;
import com.idega.business.IBOServiceBean;
import com.idega.core.contact.data.Email;
import com.idega.core.contact.data.Phone;
import com.idega.core.contact.data.PhoneTypeBMPBean;
import com.idega.core.location.business.CommuneBusiness;
import com.idega.core.location.data.Address;
import com.idega.core.location.data.Commune;
import com.idega.core.location.data.PostalCode;
import com.idega.user.data.User;
import com.idega.util.IWTimestamp;

public class BizTalkSenderBean extends IBOServiceBean implements Runnable {
	protected String endpoint = "http://10.30.11.55/Case/Case_NewCase_SoapPort.asmx?op=NewCase";

	private GeneralCase genCase = null;

	public void run() {
		if (this.genCase != null) {			
			sendGeneralCase();
		}
	}

	private void sendGeneralCase() {
		try {
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

			Collection col = getCaseBusiness().getCaseLogsByCase(this.genCase);
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
			
			Case_NewCase_SoapPortLocator locator = new Case_NewCase_SoapPortLocator();
			Case_NewCase_SoapPortSoap_PortType port = locator
					.getCase_NewCase_SoapPortSoap(new URL(this.endpoint));
			Case_request request = new Case_request();
			if (this.genCase.getExternalId() != null) {
				request.setCase_id(this.genCase.getExternalId());
			} else {
				request.setCase_id("-1");				
			}
			request.setCode("GENERAL");
			request.setCreated(new IWTimestamp(this.genCase.getCreated()).getDateString(
					"dd-MM-yyyy hh:mm:ss"));
			request.setBody(this.genCase.getMessage());
			System.out.println("case id = " + this.genCase.getPrimaryKey().toString());
			System.out.println("case unique id = " + this.genCase.getUniqueId());
			request.setExternal_case_id(this.genCase.getUniqueId());
			Commune defaultCommune = getCommuneBusiness().getDefaultCommune();
			request.setSf_id(Integer.parseInt(defaultCommune.getCommuneCode()));// husavik = 6100, hveragerdi = 8716
			request.setStatus(this.genCase.getStatus());
			request.setSubject(this.genCase.getCaseType().getName());
			Case_requestOwner owner = new Case_requestOwner();
			owner.setAddress(address.getStreetAddress());
			owner.setCase_role("owner");
			if (address != null) {
				owner.setCity(address.getCity());
			} else {
				owner.setCity("");				
			}
			if (email != null) {
				owner.setEmail(email.getEmailAddress());
			} else {
				owner.setEmail("");				
			}
			if (gsm != null) {
				owner.setGsm(gsm.getNumber());
			} else {
				owner.setGsm("");				
			}
			if (uOwner != null) {
				owner.setName(uOwner.getName());
				owner.setSocialsecurity(uOwner.getPersonalID());
			} else {
				owner.setName("");				
				owner.setSocialsecurity("");
			}
			if (phone != null) {
				owner.setPhone(phone.getNumber());
			} else {
				owner.setPhone("");				
			}
			if (pCode != null) {
				owner.setPostalcode(pCode.getPostalCode());
			} else {
				owner.setPostalcode("");				
			}
			request.setOwner(owner);
			
			Case_requestItem[] items = new Case_requestItem[2];
			items[0] = new Case_requestItem("CASE_TYPE", this.genCase.getCaseType().getName());
			items[1] = new Case_requestItem("CASE_CATEGORY", this.genCase.getCaseCategory().getName());
			
			request.setMetadata(items);
			
			Case_response response = port.newCase(request);
			String external = response.getExternal_case_id();
			if (external != null && external.length() > 36) {
				external = external.substring(0, 36);
			}
			this.genCase.setExternalId(response.getExternal_case_id());
			this.genCase.store();
			//System.out.println("external id = " + response.getExternal_case_id());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setGeneralCase(GeneralCase genCase) {
		this.genCase = genCase;
	}

	public void setEndPoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public CaseBusiness getCaseBusiness() throws RemoteException {
		return (CaseBusiness) getServiceInstance(CaseBusiness.class);
	}
	
	public CommuneBusiness getCommuneBusiness() throws RemoteException {
		return (CommuneBusiness) getServiceInstance(CommuneBusiness.class);
	}
}