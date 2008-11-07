/*
 * $Id$ Created on Oct 30, 2005
 * 
 * Copyright (C) 2005 Idega Software hf. All Rights Reserved.
 * 
 * This software is the proprietary information of Idega hf. Use is subject to license terms.
 */
package is.idega.idegaweb.egov.cases.presentation;

import is.idega.idegaweb.egov.cases.business.CasesBusiness;
import is.idega.idegaweb.egov.cases.util.CasesConstants;

import java.rmi.RemoteException;

import com.idega.block.process.data.Case;
import com.idega.block.process.data.CaseLog;
import com.idega.block.process.data.CaseStatus;
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
import com.idega.presentation.Span;
import com.idega.presentation.text.DownloadLink;
import com.idega.presentation.text.Heading1;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.ListItem;
import com.idega.presentation.text.Lists;
import com.idega.presentation.text.Paragraph;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.Label;
import com.idega.user.business.NoEmailFoundException;
import com.idega.user.business.NoPhoneFoundException;
import com.idega.user.business.UserBusiness;
import com.idega.user.data.User;
import com.idega.util.CoreConstants;
import com.idega.util.IWTimestamp;
import com.idega.util.PersonalIDFormatter;
import com.idega.util.PresentationUtil;
import com.idega.util.text.Name;

public abstract class CasesBlock extends Block {

	private CasesBusiness business;
	private UserBusiness userBusiness;

	private IWBundle iwb;
	private IWResourceBundle iwrb;

	private ICPage iHomePage;
	private String iType;

	@Override
	public void main(IWContext iwc) throws Exception {
		PresentationUtil.addStyleSheetToHeader(iwc, getBundle(iwc).getVirtualPathWithFileNameString("style/case.css"));
		initialize(iwc);
		present(iwc);
	}

	protected abstract void present(IWContext iwc) throws Exception;

	@Override
	public String getBundleIdentifier() {
		return CasesConstants.IW_BUNDLE_IDENTIFIER;
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
	
	protected Link getDownloadButtonLink(String text, Class mediaWriterClass) {
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

		DownloadLink link = new DownloadLink(all);
		link.setStyleClass("button");
		link.setMediaWriterClass(mediaWriterClass);
		link.setTarget(Link.TARGET_BLANK_WINDOW);

		return link;
	}

	protected Layer getPersonInfo(IWContext iwc, User user) throws RemoteException {
		Layer layer = new Layer(Layer.DIV);
		layer.setStyleClass("info");

		if (user != null) {
			Address address = getUserBusiness(iwc).getUsersMainAddress(user);
			PostalCode postal = null;
			if (address != null) {
				postal = address.getPostalCode();
			}
			Phone phone = null;
			try {
				phone = getUserBusiness(iwc).getUsersHomePhone(user);
			}
			catch (NoPhoneFoundException e) {
				e.printStackTrace();
			}
			Email email = null;
			try {
				email = getUserBusiness(iwc).getUsersMainEmail(user);
			}
			catch (NoEmailFoundException e) {
				e.printStackTrace();
			}

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
			personInfo.setID("phone");
			if (phone != null) {
				personInfo.add(new Text(phone.getNumber()));
			}
			layer.add(personInfo);

			personInfo = new Layer(Layer.DIV);
			personInfo.setStyleClass("personInfo");
			personInfo.setID("postal");
			if (postal != null) {
				personInfo.add(new Text(postal.getPostalAddress()));
			}
			layer.add(personInfo);

			personInfo = new Layer(Layer.DIV);
			personInfo.setStyleClass("personInfo");
			personInfo.setID("email");
			if (email != null) {
				personInfo.add(new Text(email.getEmailAddress()));
			}
			layer.add(personInfo);
		}

		return layer;
	}

	protected Layer getHandlerLayer(IWContext iwc, IWResourceBundle iwrb, Case theCase, CaseLog log) throws RemoteException {
		Layer layer = new Layer(Layer.DIV);
		layer.setStyleClass("handlerLayer");

		Heading1 heading = new Heading1(iwrb.getLocalizedString("handler_overview", "Handler overview"));
		heading.setStyleClass("subHeader");
		layer.add(heading);

		Layer section = new Layer(Layer.DIV);
		section.setStyleClass("formSection");
		layer.add(section);

		User user = log.getPerformer();
		IWTimestamp stamp = new IWTimestamp(log.getTimeStamp());
		CaseStatus status = log.getCaseStatusAfter();
		String reply = log.getComment();

		Layer formItem = new Layer(Layer.DIV);
		formItem.setStyleClass("formItem");
		Label label = new Label();
		label.setLabel(iwrb.getLocalizedString("handler", "Handler"));
		formItem.add(label);
		formItem.add(new Span(new Text(new Name(user.getFirstName(), user.getMiddleName(), user.getLastName()).getName(iwc.getCurrentLocale(), true))));
		section.add(formItem);

		formItem = new Layer(Layer.DIV);
		formItem.setStyleClass("formItem");
		label = new Label();
		label.setLabel(iwrb.getLocalizedString("timestamp", "Timestamp"));
		formItem.add(label);
		formItem.add(new Span(new Text(stamp.getLocaleDateAndTime(iwc.getCurrentLocale(), IWTimestamp.SHORT, IWTimestamp.SHORT))));
		section.add(formItem);

		formItem = new Layer(Layer.DIV);
		formItem.setStyleClass("formItem");
		label = new Label();
		label.setLabel(iwrb.getLocalizedString("status", "Status"));
		formItem.add(label);
		formItem.add(new Span(new Text(getCasesBusiness(iwc).getLocalizedCaseStatusDescription(theCase, status, iwc.getCurrentLocale()))));
		section.add(formItem);

		formItem = new Layer(Layer.DIV);
		formItem.setStyleClass("formItem");
		formItem.setStyleClass("informationItem");
		label = new Label();
		label.setLabel(iwrb.getLocalizedString("reply", "Reply"));
		formItem.add(label);
		formItem.add(new Span(new Text(reply)));
		section.add(formItem);

		Layer clear = new Layer(Layer.DIV);
		clear.setStyleClass("Clear");
		section.add(clear);

		return layer;
	}

	private void initialize(IWContext iwc) {
		setResourceBundle(getResourceBundle(iwc));
		setBundle(getBundle(iwc));
		this.business = getCasesBusiness(iwc);
		this.userBusiness = getUserBusiness(iwc);
	}

	protected String getPrefix() {
		if (getType() != null) {
			return getType() + CoreConstants.DOT;
		}
		else {
			return CoreConstants.EMPTY;
		}
	}

	protected Lists getLegend(IWContext iwc) throws RemoteException {
		Lists list = new Lists();
		list.setStyleClass("legend");

		if (getCasesBusiness(iwc).allowPrivateCases()) {
			ListItem item = new ListItem();
			item.setStyleClass("isPrivate");
			item.add(new Text(this.iwrb.getLocalizedString("legend.is_private", "Is private")));
			list.add(item);
		}

		ListItem item = new ListItem();
		item.setStyleClass("isReview");
		item.add(new Text(this.iwrb.getLocalizedString("legend.is_review", "Is review")));
		list.add(item);

		return list;
	}

	protected Layer getAttentionLayer(String text) {
		Layer layer = new Layer(Layer.DIV);
		layer.setStyleClass("attention");

		Layer imageLayer = new Layer(Layer.DIV);
		imageLayer.setStyleClass("attentionImage");
		layer.add(imageLayer);

		Layer textLayer = new Layer(Layer.DIV);
		textLayer.setStyleClass("attentionText");
		layer.add(textLayer);

		Paragraph paragraph = new Paragraph();
		paragraph.add(new Text(text));
		textLayer.add(paragraph);

		Layer clearLayer = new Layer(Layer.DIV);
		clearLayer.setStyleClass("attentionClear");
		layer.add(clearLayer);

		return layer;
	}

	protected IWBundle getBundle() {
		return this.iwb;
	}

	protected IWResourceBundle getResourceBundle() {
		return this.iwrb;
	}

	protected CasesBusiness getCasesBusiness() {
		return this.business;
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
		return this.userBusiness;
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
		return this.iHomePage;
	}

	public void setHomePage(ICPage page) {
		this.iHomePage = page;
	}

	protected String getType() {
		return this.iType;
	}

	public void setType(String type) {
		this.iType = type;
	}
}