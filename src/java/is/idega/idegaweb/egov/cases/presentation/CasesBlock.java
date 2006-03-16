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
import com.idega.core.location.data.Address;
import com.idega.core.location.data.PostalCode;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.Block;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
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

	protected Link getButtonLink(String text) {
		Layer all = new Layer(Layer.SPAN);
		all.setStyleClass("buttonSpan");
		
		Layer left = new Layer(Layer.SPAN);
		left.setStyleClass("left");
		all.add(left);
		
		Layer middle = new Layer(Layer.SPAN);
		middle.setStyleClass("middle");
		middle.add(new Text(text));
		all.add(middle);
		
		Layer right = new Layer(Layer.SPAN);
		right.setStyleClass("right");
		all.add(right);
		
		Link link = new Link(all);
		link.setStyleClass("button");
		
		return link;
	}
	
	protected Layer getPersonInfo(IWContext iwc, User user) throws RemoteException {
		Address address = getUserBusiness(iwc).getUsersMainAddress(user);
		PostalCode postal = null;
		if (address != null) {
			postal = address.getPostalCode();
		}

		Layer layer = new Layer(Layer.DIV);
		layer.setStyleClass("info");
		
		Layer personInfo = new Layer(Layer.DIV);
		personInfo.setStyleClass("personInfo");
		personInfo.setID("name");
		personInfo.add(new Text(user.getName()));
		layer.add(personInfo);
		
		personInfo = new Layer(Layer.DIV);
		personInfo.setStyleClass("personInfo");
		personInfo.setID("personalID");
		personInfo.add(new Text(PersonalIDFormatter.format(user.getPersonalID(), iwc.getCurrentLocale())));
		layer.add(personInfo);
		
		personInfo = new Layer(Layer.DIV);
		personInfo.setStyleClass("personInfo");
		personInfo.setID("address");
		if (address != null) {
			personInfo.add(new Text(address.getStreetAddress()));
		}
		layer.add(personInfo);
		
		personInfo = new Layer(Layer.DIV);
		personInfo.setStyleClass("personInfo");
		personInfo.setID("postal");
		if (postal != null) {
			personInfo.add(new Text(postal.getPostalAddress()));
		}
		layer.add(personInfo);
		
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