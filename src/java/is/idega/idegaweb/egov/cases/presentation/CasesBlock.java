/*
 * $Id$
 * Created on Oct 30, 2005
 *
 * Copyright (C) 2005 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf.
 * Use is subject to license terms.
 */
package is.idega.idegaweb.egov.cases.presentation;

import is.idega.idegaweb.egov.cases.business.CasesBusiness;
import is.idega.idegaweb.egov.cases.util.CaseConstants;
import java.rmi.RemoteException;
import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.business.IBORuntimeException;
import com.idega.core.builder.data.ICPage;
import com.idega.core.contact.data.Email;
import com.idega.core.contact.data.Phone;
import com.idega.core.location.data.Address;
import com.idega.core.location.data.PostalCode;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.Block;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.text.Heading1;
import com.idega.presentation.text.Text;
import com.idega.user.business.NoEmailFoundException;
import com.idega.user.business.NoPhoneFoundException;
import com.idega.user.business.UserBusiness;
import com.idega.user.data.User;
import com.idega.util.PersonalIDFormatter;


public abstract class CasesBlock extends Block {

	private CasesBusiness business;
	private UserBusiness userBusiness;
	
	private IWBundle iwb;
	private IWResourceBundle iwrb;
	
	private ICPage iHomePage;

	public void main(IWContext iwc) throws Exception {
		initialize(iwc);
		present(iwc);
	}

	protected abstract void present(IWContext iwc) throws Exception;

	public String getBundleIdentifier() {
		return CaseConstants.IW_BUNDLE_IDENTIFIER;
	}

	protected Layer getPersonInfo(IWContext iwc, User user) throws RemoteException {
		Layer layer = new Layer();
		layer.setID("personInfo");
		
		Address address = getUserBusiness().getUsersMainAddress(user);
		PostalCode postal = null;
		if (address != null) {
			postal = address.getPostalCode();
		}
		Phone phone = null;
		try {
			phone = getUserBusiness().getUsersHomePhone(user);
		}
		catch (NoPhoneFoundException npfe) {
			phone = null;
		}
		Phone mobilePhone = null;
		try {
			mobilePhone = getUserBusiness().getUsersMobilePhone(user);
		}
		catch (NoPhoneFoundException npfe) {
			mobilePhone = null;
		}
		Email email = null;
		try {
			email = getUserBusiness().getUsersMainEmail(user);
		}
		catch (NoEmailFoundException nefe) {
			email = null;
		}
		
		Layer formElement = new Layer(Layer.DIV);
		formElement.setStyleClass("personInfoItem");
		Heading1 heading = new Heading1(getResourceBundle().getLocalizedString("name", "Name"));
		Text text = new Text(user.getName());
		formElement.add(heading);
		formElement.add(text);
		layer.add(formElement);
		
		formElement = new Layer(Layer.DIV);
		formElement.setStyleClass("personInfoItem");
		heading = new Heading1(getResourceBundle().getLocalizedString("personal_id", "Personal ID"));
		text = new Text(PersonalIDFormatter.format(user.getPersonalID(), iwc.getCurrentLocale()));
		formElement.add(heading);
		formElement.add(text);
		layer.add(formElement);
		
		if (phone != null && phone.getNumber() != null) {
			formElement = new Layer(Layer.DIV);
			formElement.setStyleClass("personInfoItem");
			heading = new Heading1(getResourceBundle().getLocalizedString("home_phone", "Home phone"));
			text = new Text(phone.getNumber());
			formElement.add(heading);
			formElement.add(text);
			layer.add(formElement);
		}
	
		formElement = new Layer(Layer.DIV);
		formElement.setStyleClass("personInfoItem");
		heading = new Heading1(getResourceBundle().getLocalizedString("address", "Address"));
		if (address != null) {
			text = new Text(address.getStreetAddress());
		}
		else {
			text = new Text("-");
		}
		formElement.add(heading);
		formElement.add(text);
		layer.add(formElement);
		
		formElement = new Layer(Layer.DIV);
		formElement.setStyleClass("personInfoItem");
		heading = new Heading1(getResourceBundle().getLocalizedString("zip_code", "Postal code"));
		if (postal != null) {
			text = new Text(postal.getPostalCode());
		}
		else {
			text = new Text("-");
		}
		formElement.add(heading);
		formElement.add(text);
		layer.add(formElement);
		
		formElement = new Layer(Layer.DIV);
		formElement.setStyleClass("personInfoItem");
		heading = new Heading1(getResourceBundle().getLocalizedString("zip_city", "City"));
		if (postal != null) {
			text = new Text(postal.getName());
		}
		else {
			text = new Text("-");
		}
		formElement.add(heading);
		formElement.add(text);
		layer.add(formElement);
		
		if (mobilePhone != null && mobilePhone.getNumber() != null) {
			formElement = new Layer(Layer.DIV);
			formElement.setStyleClass("personInfoItem");
			heading = new Heading1(getResourceBundle().getLocalizedString("mobile_phone", "Mobile phone"));
			text = new Text(mobilePhone.getNumber());
			formElement.add(heading);
			formElement.add(text);
			layer.add(formElement);
		}
	
		if (email != null && email.getEmailAddress() != null) {
			formElement = new Layer(Layer.DIV);
			formElement.setStyleClass("personInfoItem");
			heading = new Heading1(getResourceBundle().getLocalizedString("email", "E-mail"));
			text = new Text(email.getEmailAddress());
			formElement.add(heading);
			formElement.add(text);
			layer.add(formElement);
		}
		
		Layer clear = new Layer(Layer.DIV);
		clear.setStyleClass("Clear");
		layer.add(clear);
		
		return layer;
	}

	private void initialize(IWContext iwc) {
		setResourceBundle(getResourceBundle(iwc));
		setBundle(getBundle(iwc));
		business = getCasesBusiness(iwc);
		userBusiness = getUserBusiness(iwc);
	}
	
	protected IWBundle getBundle() {
		return iwb;
	}
	
	protected IWResourceBundle getResourceBundle() {
		return iwrb;
	}
	
	protected CasesBusiness getBusiness() {
		return business;
	}

	protected CasesBusiness getCasesBusiness(IWApplicationContext iwac) {
		try {
			return (CasesBusiness) IBOLookup.getServiceInstance(iwac, CasesBusiness.class);
		}
		catch (IBOLookupException ile) {
			throw new IBORuntimeException(ile);
		}
	}
	
	protected UserBusiness getUserBusiness() {
		return userBusiness;
	}
	
	private UserBusiness getUserBusiness(IWApplicationContext iwac) {
		try {
			return (UserBusiness) IBOLookup.getServiceInstance(iwac, UserBusiness.class);
		}
		catch (IBOLookupException ile) {
			throw new IBORuntimeException(ile);
		}
	}
	private void setBundle(IWBundle iwb) {
		this.iwb = iwb;
	}
	
	private void setResourceBundle(IWResourceBundle iwrb) {
		this.iwrb = iwrb;
	}

	protected ICPage getHomePage() {
		return iHomePage;
	}
	
	public void setHomePage(ICPage page) {
		iHomePage = page;
	}
}