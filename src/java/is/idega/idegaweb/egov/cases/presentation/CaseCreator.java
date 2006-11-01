/*
 * $Id$
 * Created on Oct 31, 2005
 *
 * Copyright (C) 2005 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf.
 * Use is subject to license terms.
 */
package is.idega.idegaweb.egov.cases.presentation;

import is.idega.idegaweb.egov.application.presentation.ApplicationForm;
import is.idega.idegaweb.egov.cases.business.CaseCategoryCollectionHandler;
import is.idega.idegaweb.egov.cases.business.CasesBusiness;
import is.idega.idegaweb.egov.cases.data.CaseCategory;
import is.idega.idegaweb.egov.cases.data.CaseType;
import is.idega.idegaweb.egov.cases.util.CaseConstants;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.ejb.CreateException;
import javax.ejb.FinderException;

import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.business.IBORuntimeException;
import com.idega.core.accesscontrol.business.NotLoggedOnException;
import com.idega.core.builder.data.ICPage;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.idegaweb.IWUserContext;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.Span;
import com.idega.presentation.remotescripting.RemoteScriptHandler;
import com.idega.presentation.text.Heading1;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.HiddenInput;
import com.idega.presentation.ui.Label;
import com.idega.presentation.ui.TextArea;
import com.idega.presentation.ui.util.SelectorUtility;
import com.idega.user.business.UserSession;
import com.idega.user.data.User;


public class CaseCreator extends ApplicationForm {
	
	private static final String PARAMETER_ACTION = "cc_prm_action";
	
	private static final String PARAMETER_MESSAGE = "prm_message";
	private static final String PARAMETER_CASE_CATEGORY_PK = "prm_case_category_pk";
	private static final String PARAMETER_SUB_CASE_CATEGORY_PK = "prm_sub_case_category_pk";
	private static final String PARAMETER_CASE_TYPE_PK = "prm_case_type_pk";
	
	private static final int ACTION_PHASE_1 = 1;
	private static final int ACTION_OVERVIEW = 2;
	private static final int ACTION_SAVE = 3;
	
	private String iType;

	private IWResourceBundle iwrb;
	private boolean iUseSessionUser = false;
	private boolean iUseAnonymous = false;
	
	private Collection iCategories;
	
	public String getBundleIdentifier() {
		return CaseConstants.IW_BUNDLE_IDENTIFIER;
	}
	
	public String getCaseCode() {
		return CaseConstants.CASE_CODE_KEY;
	}

	protected void present(IWContext iwc) {
		this.iwrb = getResourceBundle(iwc);
		
		try {
			switch (parseAction(iwc)) {
				case ACTION_PHASE_1:
					showPhaseOne(iwc);
					break;
	
				case ACTION_OVERVIEW:
					showOverview(iwc);
					break;
	
				case ACTION_SAVE:
					save(iwc);
					break;
			}
		}
		catch (RemoteException re) {
			throw new IBORuntimeException(re);
		}
	}
	
	private int parseAction(IWContext iwc) {
		if (iwc.isParameterSet(PARAMETER_ACTION)) {
			return Integer.parseInt(iwc.getParameter(PARAMETER_ACTION));
		}
		return ACTION_PHASE_1;
	}

	private void showPhaseOne(IWContext iwc) throws RemoteException {
		Form form = new Form();
		form.setStyleClass("casesForm");
		form.add(new HiddenInput(PARAMETER_ACTION, String.valueOf(ACTION_PHASE_1)));
		
		addErrors(iwc, form);
		
		Heading1 heading = new Heading1(this.iwrb.getLocalizedString(getPrefix() + "application.case_creator", "Case creator"));
		heading.setStyleClass("applicationHeading");
		form.add(heading);
		
		form.add(getPhasesHeader(this.iwrb.getLocalizedString(getPrefix() + "application.enter_new_case", "Enter new case"), 1, 3));

		form.add(getPersonInfo(iwc, getUser(iwc)));
		
		heading = new Heading1(this.iwrb.getLocalizedString(getPrefix() + "case_creator.enter_case", "New case"));
		heading.setStyleClass("subHeader");
		heading.setStyleClass("topSubHeader");
		form.add(heading);
		
		Layer section = new Layer(Layer.DIV);
		section.setStyleClass("formSection");
		form.add(section);
		
		SelectorUtility util = new SelectorUtility();
		DropdownMenu categories = new DropdownMenu(PARAMETER_CASE_CATEGORY_PK);
		categories.addMenuElementFirst("", this.iwrb.getLocalizedString("case_creator.select_category", "Select category"));
		categories.keepStatusOnAction(true);
		categories.setStyleClass("caseCategoryDropdown");
		
		Collection parentCategories = getCasesBusiness(iwc).getCaseCategories();
		Iterator iter = parentCategories.iterator();
		while (iter.hasNext()) {
			CaseCategory element = (CaseCategory) iter.next();
			
			boolean addCategory = false;
			if (iCategories != null) {
				Iterator iterator = iCategories.iterator();
				while (iterator.hasNext()) {
					String categoryPK = (String) iterator.next();
					if (element.getPrimaryKey().toString().equals(categoryPK)) {
						addCategory = true;
					}
				}
			}
			else {
				addCategory = true;
			}
			
			if (addCategory) {
				categories.addMenuElement(element.getPrimaryKey().toString(), element.getName());
			}
		}
		
		DropdownMenu subCategories = new DropdownMenu(PARAMETER_SUB_CASE_CATEGORY_PK);
		boolean addEmptyElement = true;
		if (iwc.isParameterSet(PARAMETER_CASE_CATEGORY_PK)) {
			try {
				CaseCategory category = getCasesBusiness(iwc).getCaseCategory(iwc.getParameter(PARAMETER_CASE_CATEGORY_PK));
				Collection subCats = getCasesBusiness(iwc).getSubCategories(category);
				if (!subCats.isEmpty()) {
					iter = subCats.iterator();
					while (iter.hasNext()) {
						CaseCategory subCategory = (CaseCategory) iter.next();
						subCategories.addMenuElement(subCategory.getPrimaryKey().toString(), subCategory.getName());
					}
				}
				else {
					addEmptyElement = false;
					subCategories.addMenuElement(category.getPrimaryKey().toString(), "");
				}
			}
			catch (FinderException fe) {
				fe.printStackTrace();
			}
		}
		if (addEmptyElement) {
			subCategories.addMenuElementFirst("", this.iwrb.getLocalizedString("case_creator.select_sub_category", "Select sub category"));
		}
		subCategories.keepStatusOnAction(true);
		subCategories.setStyleClass("subCaseCategoryDropdown");
		
		DropdownMenu types = (DropdownMenu) util.getSelectorFromIDOEntities(new DropdownMenu(PARAMETER_CASE_TYPE_PK), getCasesBusiness(iwc).getCaseTypes(), "getName");
		types.addMenuElementFirst("", this.iwrb.getLocalizedString("case_creator.select_type", "Select type"));
		types.keepStatusOnAction(true);
		types.setStyleClass("caseTypeDropdown");
		
		TextArea message = new TextArea(PARAMETER_MESSAGE);
		message.setStyleClass("textarea");
		message.keepStatusOnAction(true);
		
		Layer helpLayer = new Layer(Layer.DIV);
		helpLayer.setStyleClass("helperText");
		helpLayer.add(new Text(this.iwrb.getLocalizedString(getPrefix() + "case_creator.information_text", "Information text here...")));
		section.add(helpLayer);
		
		Layer formItem = new Layer(Layer.DIV);
		formItem.setStyleClass("formItem");
		formItem.setStyleClass("required");
		Label label = new Label(new Span(new Text(this.iwrb.getLocalizedString("case_type", "Case type"))), types);
		formItem.add(label);
		formItem.add(types);
		section.add(formItem);

		formItem = new Layer(Layer.DIV);
		formItem.setStyleClass("formItem");
		formItem.setStyleClass("required");
		label = new Label(new Span(new Text(this.iwrb.getLocalizedString("case_category", "Case category"))), categories);
		formItem.add(label);
		formItem.add(categories);
		section.add(formItem);
		
		if (getCasesBusiness(iwc).useSubCategories()) {
			try {
				RemoteScriptHandler rsh = new RemoteScriptHandler(categories, subCategories);
				rsh.setRemoteScriptCollectionClass(CaseCategoryCollectionHandler.class);
				formItem.add(rsh);
			}
			catch (IllegalAccessException iae) {
				iae.printStackTrace();
			}
			catch (InstantiationException ie) {
				ie.printStackTrace();
			}

			formItem = new Layer(Layer.DIV);
			formItem.setStyleClass("formItem");
			formItem.setStyleClass("required");
			label = new Label(new Span(new Text(this.iwrb.getLocalizedString("sub_case_category", "Sub case category"))), subCategories);
			formItem.add(label);
			formItem.add(subCategories);
			section.add(formItem);
		}

		formItem = new Layer(Layer.DIV);
		formItem.setStyleClass("formItem");
		formItem.setStyleClass("required");
		if (hasError(PARAMETER_MESSAGE)) {
			formItem.setStyleClass("hasError");
		}
		label = new Label(new Span(new Text(this.iwrb.getLocalizedString(getPrefix() + "message", "Message"))), message);
		formItem.add(label);
		formItem.add(message);
		section.add(formItem);

		Layer clear = new Layer(Layer.DIV);
		clear.setStyleClass("Clear");
		section.add(clear);

		Layer bottom = new Layer(Layer.DIV);
		bottom.setStyleClass("bottom");
		form.add(bottom);

		Link next = getButtonLink(this.iwrb.getLocalizedString("next", "Next"));
		next.setValueOnClick(PARAMETER_ACTION, String.valueOf(ACTION_OVERVIEW));
		next.setToFormSubmit(form);
		bottom.add(next);

		add(form);
	}
	
	private void showOverview(IWContext iwc) throws RemoteException {
		if (this.getCasesBusiness(iwc).useSubCategories()) {
			if (!iwc.isParameterSet(PARAMETER_SUB_CASE_CATEGORY_PK)) {
				setError(PARAMETER_CASE_CATEGORY_PK, this.iwrb.getLocalizedString("case_creator.sub_category_empty", "You must select a category"));
			}
		}
		if (!iwc.isParameterSet(PARAMETER_CASE_CATEGORY_PK)) {
			setError(PARAMETER_CASE_CATEGORY_PK, this.iwrb.getLocalizedString("case_creator.category_empty", "You must select a category"));
		}
		if (!iwc.isParameterSet(PARAMETER_CASE_TYPE_PK)) {
			setError(PARAMETER_CASE_TYPE_PK, this.iwrb.getLocalizedString("case_creator.type_empty", "You must select a type"));
		}
		if (!iwc.isParameterSet(PARAMETER_MESSAGE)) {
			setError(PARAMETER_MESSAGE, this.iwrb.getLocalizedString(getPrefix() + "case_creator.message_empty", "You must enter a message"));
		}
		
		if (hasErrors()) {
			showPhaseOne(iwc);
			return;
		}

		Form form = new Form();
		form.setStyleClass("casesForm");
		form.setStyleClass("overview");
		form.addParameter(PARAMETER_ACTION, String.valueOf(ACTION_OVERVIEW));
		form.maintainParameter(PARAMETER_MESSAGE);
		form.maintainParameter(PARAMETER_CASE_TYPE_PK);
		form.maintainParameter(PARAMETER_CASE_CATEGORY_PK);
		form.maintainParameter(PARAMETER_SUB_CASE_CATEGORY_PK);
		
		Heading1 heading = new Heading1(this.iwrb.getLocalizedString(getPrefix() + "application.case_creator", "Case creator"));
		heading.setStyleClass("applicationHeading");
		form.add(heading);
		
		form.add(getPhasesHeader(this.iwrb.getLocalizedString(getPrefix() + "application.overview", "Overview"), 2, 3));

		form.add(getPersonInfo(iwc, getUser(iwc)));
		
		heading = new Heading1(this.iwrb.getLocalizedString(getPrefix() + "case_creator.enter_case_overview", "New case overview"));
		heading.setStyleClass("subHeader");
		heading.setStyleClass("topSubHeader");
		form.add(heading);
		
		Object caseTypePK = iwc.getParameter(PARAMETER_CASE_TYPE_PK);
		Object caseCategoryPK = iwc.getParameter(PARAMETER_CASE_CATEGORY_PK);
		Object subCaseCategoryPK = iwc.getParameter(PARAMETER_SUB_CASE_CATEGORY_PK);
		String message = iwc.getParameter(PARAMETER_MESSAGE);
		
		CaseCategory category = null;
		try {
			category = getCasesBusiness(iwc).getCaseCategory(caseCategoryPK);
		}
		catch (FinderException fe) {
			throw new IBORuntimeException(fe);
		}
		
		CaseCategory subCategory = null;
		if (getCasesBusiness(iwc).useSubCategories()) {
			try {
				subCategory = getCasesBusiness(iwc).getCaseCategory(subCaseCategoryPK);
			}
			catch (FinderException fe) {
				throw new IBORuntimeException(fe);
			}
		}

		CaseType type = null;
		try {
			type = getCasesBusiness(iwc).getCaseType(caseTypePK);
		}
		catch (FinderException fe) {
			throw new IBORuntimeException(fe);
		}

		Layer section = new Layer(Layer.DIV);
		section.setStyleClass("formSection");
		form.add(section);
		
		Layer typeSpan = new Layer(Layer.SPAN);
		typeSpan.add(new Text(type.getName()));
		
		Layer categorySpan = new Layer(Layer.SPAN);
		categorySpan.add(new Text(category.getName()));
		
		Layer messageSpan = new Layer(Layer.SPAN);
		messageSpan.add(new Text(message));
		
		Layer formItem = new Layer(Layer.DIV);
		formItem.setStyleClass("formItem");
		Label label = new Label();
		label.setLabel(this.iwrb.getLocalizedString("case_type", "Case type"));
		formItem.add(label);
		formItem.add(typeSpan);
		section.add(formItem);

		formItem = new Layer(Layer.DIV);
		formItem.setStyleClass("formItem");
		label = new Label();
		label.setLabel(this.iwrb.getLocalizedString("case_category", "Case category"));
		formItem.add(label);
		formItem.add(categorySpan);
		section.add(formItem);

		if (getCasesBusiness(iwc).useSubCategories() && !subCategory.equals(category)) {
			Layer subCategorySpan = new Layer(Layer.SPAN);
			subCategorySpan.add(new Text(subCategory.getName()));
			
			formItem = new Layer(Layer.DIV);
			formItem.setStyleClass("formItem");
			label = new Label();
			label.setLabel(this.iwrb.getLocalizedString("sub_case_category", "Sub case category"));
			formItem.add(label);
			formItem.add(subCategorySpan);
			section.add(formItem);
		}
		
		formItem = new Layer(Layer.DIV);
		formItem.setStyleClass("formItem");
		formItem.setStyleClass("informationItem");
		label = new Label();
		label.setLabel(this.iwrb.getLocalizedString(getPrefix() + "message", "Message"));
		formItem.add(label);
		formItem.add(messageSpan);
		section.add(formItem);

		Layer clear = new Layer(Layer.DIV);
		clear.setStyleClass("Clear");
		section.add(clear);

		Layer bottom = new Layer(Layer.DIV);
		bottom.setStyleClass("bottom");
		form.add(bottom);

		Link next = getButtonLink(this.iwrb.getLocalizedString("send", "Send"));
		next.setValueOnClick(PARAMETER_ACTION, String.valueOf(ACTION_SAVE));
		next.setToFormSubmit(form);
		bottom.add(next);

		Link back = getButtonLink(this.iwrb.getLocalizedString("previous", "Previous"));
		back.setValueOnClick(PARAMETER_ACTION, String.valueOf(ACTION_PHASE_1));
		back.setToFormSubmit(form);
		bottom.add(back);

		add(form);
	}
	
	private void save(IWContext iwc) throws RemoteException {
		String message = iwc.getParameter(PARAMETER_MESSAGE);
		Object caseCategoryPK = iwc.getParameter(PARAMETER_CASE_CATEGORY_PK);
		Object subCaseCategoryPK = iwc.getParameter(PARAMETER_SUB_CASE_CATEGORY_PK);
		Object caseTypePK = iwc.getParameter(PARAMETER_CASE_TYPE_PK);
		
		try {
			getCasesBusiness(iwc).storeGeneralCase(getUser(iwc), getCasesBusiness(iwc).useSubCategories() ? subCaseCategoryPK : caseCategoryPK, caseTypePK, message, getType());

			Heading1 heading = new Heading1(this.iwrb.getLocalizedString(getPrefix() + "application.case_creator", "Case creator"));
			heading.setStyleClass("applicationHeading");
			add(heading);
			
			addPhasesReceipt(iwc, this.iwrb.getLocalizedString(getPrefix() + "case_creator.save_completed", "Application sent"), this.iwrb.getLocalizedString("case_creator.save_completed", "Application sent"), this.iwrb.getLocalizedString("case_creator.save_confirmation", "Your case has been sent and will be processed accordingly."), 3, 3);

			Layer clearLayer = new Layer(Layer.DIV);
			clearLayer.setStyleClass("Clear");
			add(clearLayer);
			
			Layer bottom = new Layer(Layer.DIV);
			bottom.setStyleClass("bottom");
			add(bottom);

			if (!this.iUseAnonymous && iwc.isLoggedOn()) {
				try {
					ICPage page = getUserBusiness(iwc).getHomePageForUser(iwc.getCurrentUser());
					Link link = getButtonLink(this.iwrb.getLocalizedString("my_page", "My page"));
					link.setStyleClass("homeButton");
					link.setPage(page);
					bottom.add(link);
				}
				catch (FinderException fe) {
					fe.printStackTrace();
				}
			}
			else {
				Link link = getButtonLink(this.iwrb.getLocalizedString("close", "Close"));
				link.setStyleClass("homeButton");
				link.setAsCloseLink();
				bottom.add(link);
			}
		}
		catch (CreateException ce) {
			ce.printStackTrace();
			throw new IBORuntimeException(ce);
		}
	}
	
	protected String getPrefix() {
		if (getType() != null) {
			return getType() + ".";
		}
		else {
			return "";
		}
	}

	private User getUser(IWContext iwc) throws RemoteException {
		if (this.iUseAnonymous) {
			return null;
		}
		if (this.iUseSessionUser) {
			return getUserSession(iwc).getUser();
		}
		else {
			try {
				return iwc.getCurrentUser();
			}
			catch (NotLoggedOnException nloe) {
				return null;
			}
		}
	}

	private UserSession getUserSession(IWUserContext iwuc) {
		try {
			return (UserSession) IBOLookup.getSessionInstance(iwuc, UserSession.class);
		}
		catch (IBOLookupException ile) {
			throw new IBORuntimeException(ile);
		}
	}
	
	protected CasesBusiness getCasesBusiness(IWApplicationContext iwac) {
		try {
			return (CasesBusiness) IBOLookup.getServiceInstance(iwac, CasesBusiness.class);
		}
		catch (IBOLookupException ile) {
			throw new IBORuntimeException(ile);
		}
	}	

	public void setUseSessionUser(boolean useSessionUser) {
		this.iUseSessionUser = useSessionUser;
	}
	
	public void setUseAnonymous(boolean useAnonymous) {
		this.iUseAnonymous = useAnonymous;
	}
	
	protected String getType() {
		return this.iType;
	}

	public void setType(String type) {
		this.iType = type;
	}
	
	public void setAllowedCategory(String categoryPK) {
		if (iCategories == null) {
			iCategories = new ArrayList();
		}
		iCategories.add(categoryPK);
	}
}