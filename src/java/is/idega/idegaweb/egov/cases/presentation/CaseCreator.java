/*
 * $Id$ Created on Oct 31, 2005
 * 
 * Copyright (C) 2005 Idega Software hf. All Rights Reserved.
 * 
 * This software is the proprietary information of Idega hf. Use is subject to license terms.
 */
package is.idega.idegaweb.egov.cases.presentation;

import is.idega.idegaweb.egov.application.presentation.ApplicationForm;
import is.idega.idegaweb.egov.cases.business.CasesBusiness;
import is.idega.idegaweb.egov.cases.data.CaseCategory;
import is.idega.idegaweb.egov.cases.data.CaseType;
import is.idega.idegaweb.egov.cases.data.GeneralCase;
import is.idega.idegaweb.egov.cases.util.CasesConstants;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.ejb.CreateException;
import javax.ejb.FinderException;

import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.business.IBORuntimeException;
import com.idega.core.accesscontrol.business.NotLoggedOnException;
import com.idega.core.builder.data.ICPage;
import com.idega.core.file.data.ICFile;
import com.idega.core.file.data.ICFileHome;
import com.idega.data.IDOLookup;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.idegaweb.IWUserContext;
import com.idega.io.UploadFile;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.Script;
import com.idega.presentation.Span;
import com.idega.presentation.text.Heading1;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Paragraph;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.CheckBox;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.FileInput;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.HiddenInput;
import com.idega.presentation.ui.Label;
import com.idega.presentation.ui.TextArea;
import com.idega.presentation.ui.TextInput;
import com.idega.presentation.ui.util.SelectorUtility;
import com.idega.user.business.UserBusiness;
import com.idega.user.business.UserSession;
import com.idega.user.data.User;
import com.idega.util.CoreConstants;
import com.idega.util.EmailValidator;
import com.idega.util.FileUtil;
import com.idega.util.PersonalIDFormatter;
import com.idega.util.PresentationUtil;
import com.idega.util.text.Name;
import com.idega.util.text.SocialSecurityNumber;

public class CaseCreator extends ApplicationForm {

	protected static final String PARAMETER_ACTION = "cc_prm_action";

	protected static final String PARAMETER_REGARDING = "prm_regarding";
	protected static final String PARAMETER_MESSAGE = "prm_message";
	protected static final String PARAMETER_CASE_CATEGORY_PK = "prm_case_category_pk";
	protected static final String PARAMETER_HIDE_OTHERS = "hide_others";
	protected static final String PARAMETER_SUB_CASE_CATEGORY_PK = "prm_sub_case_category_pk";
	protected static final String PARAMETER_CASE_TYPE_PK = "prm_case_type_pk";
	protected static final String PARAMETER_ATTACHMENT_PK = "prm_file_pk";
	protected static final String PARAMETER_PRIVATE = "prm_private";
	
	private static final String PARAMETER_NAME = "prm_name";
	private static final String PARAMETER_PERSONAL_ID = "prm_personal_id";
	private static final String PARAMETER_EMAIL = "prm_email";
	private static final String PARAMETER_PHONE = "prm_phone";
	private static final String PARAMETER_REFERENCE = "prm_reference";

	protected static final int ACTION_PHASE_1 = 1;
	protected static final int ACTION_OVERVIEW = 2;
	protected static final int ACTION_SAVE = 3;

	protected String iType;

	protected IWResourceBundle iwrb;
	protected boolean iUseSessionUser = false;
	protected boolean iUseAnonymous = false;
	protected boolean iShowSenderInputs = false;
	protected boolean iShowRegarding = true;

	protected Collection iCategories;

	@Override
	public String getBundleIdentifier() {
		return CasesConstants.IW_BUNDLE_IDENTIFIER;
	}

	@Override
	public String getCaseCode() {
		return CasesConstants.CASE_CODE_KEY;
	}

	@Override
	protected void present(IWContext iwc) {
		PresentationUtil.addStyleSheetToHeader(iwc, getBundle(iwc).getVirtualPathWithFileNameString("style/case.css"));
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

	protected void showPhaseOne(IWContext iwc) throws RemoteException {
		User user = getUser(iwc);
		Locale locale = iwc.getCurrentLocale();
		boolean hideOtherCategories = "true".equalsIgnoreCase(iwc.getParameter(PARAMETER_HIDE_OTHERS));

		CaseCategory category = null;
		if (iwc.isParameterSet(PARAMETER_CASE_CATEGORY_PK)) {
			try {
				category = getCasesBusiness(iwc).getCaseCategory(iwc.getParameter(PARAMETER_CASE_CATEGORY_PK));
			}
			catch (FinderException fe) {
				fe.printStackTrace();
			}
		}

		CaseCategory subCategory = null;
		if (getCasesBusiness(iwc).useSubCategories() && iwc.isParameterSet(PARAMETER_SUB_CASE_CATEGORY_PK)) {
			try {
				subCategory = getCasesBusiness(iwc).getCaseCategory(iwc.getParameter(PARAMETER_SUB_CASE_CATEGORY_PK));
			}
			catch (FinderException fe) {
				throw new IBORuntimeException(fe);
			}
		}

		Form form = new Form();
		form.setStyleClass("casesForm");
		form.add(new HiddenInput(PARAMETER_ACTION, String.valueOf(ACTION_PHASE_1)));

		addErrors(iwc, form);

		String headingText = this.iwrb.getLocalizedString(getPrefix() + (this.iUseAnonymous ? "anonymous_application.case_creator" : "application.case_creator"), "Case creator");
		if (category != null) {
			headingText += " - " + category.getLocalizedCategoryName(locale);
		}

		Heading1 heading = new Heading1(headingText);
		heading.setStyleClass("applicationHeading");
		form.add(heading);

		form.add(getPhasesHeader(this.iwrb.getLocalizedString(getPrefix() + "application.enter_new_case", "Enter new case"), 1, 3));

		Layer contents = new Layer();
		contents.setStyleClass("formContents");
		form.add(contents);
		
		contents.add(getPersonInfo(iwc, user));

		heading = new Heading1(this.iwrb.getLocalizedString(getPrefix() + "case_creator.enter_case", "New case"));
		heading.setStyleClass("subHeader");
		heading.setStyleClass("topSubHeader");
		contents.add(heading);

		Layer section = new Layer(Layer.DIV);
		section.setStyleClass("formSection");
		contents.add(section);

		SelectorUtility util = new SelectorUtility();
		DropdownMenu categories = new DropdownMenu(PARAMETER_CASE_CATEGORY_PK);
		categories.keepStatusOnAction(true);
		categories.setStyleClass("caseCategoryDropdown");

		if (category != null && hideOtherCategories) {
			form.add(new HiddenInput(PARAMETER_HIDE_OTHERS, "true"));
			categories.addMenuElement(category.getPrimaryKey().toString(), category.getLocalizedCategoryName(locale));
			categories.setSelectedElement(category.getPrimaryKey().toString());
		}
		else {
			categories.addMenuElementFirst("", this.iwrb.getLocalizedString("case_creator.select_category", "Select category"));
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
					String primaryKey = element.getPrimaryKey().toString();
					categories.addMenuElement(primaryKey, element.getLocalizedCategoryName(locale));
					if (category != null && category.getPrimaryKey().equals(primaryKey)) {
						categories.setSelectedElement(primaryKey);
					}
				}
			}
		}
		categories.setToSubmit();

		DropdownMenu subCategories = new DropdownMenu(PARAMETER_SUB_CASE_CATEGORY_PK);
		boolean addEmptyElement = true;
		if (category != null) {
			Collection subCats = getCasesBusiness(iwc).getSubCategories(category);
			if (!subCats.isEmpty()) {
				Iterator iter = subCats.iterator();
				while (iter.hasNext()) {
					CaseCategory subCat = (CaseCategory) iter.next();
					subCategories.addMenuElement(subCat.getPrimaryKey().toString(), subCat.getLocalizedCategoryName(locale));
				}
			}
			else {
				addEmptyElement = false;
				subCategories.addMenuElement(category.getPrimaryKey().toString(), iwrb.getLocalizedString("case_creator.no_sub_category", "no sub category"));
			}
		}
		if (addEmptyElement) {
			subCategories.addMenuElementFirst("", this.iwrb.getLocalizedString("case_creator.select_sub_category", "Select sub category"));
		}
		subCategories.keepStatusOnAction(true);
		subCategories.setToSubmit();
		subCategories.setStyleClass("subCaseCategoryDropdown");

		DropdownMenu types = (DropdownMenu) util.getSelectorFromIDOEntities(new DropdownMenu(PARAMETER_CASE_TYPE_PK), getCasesBusiness(iwc).getCaseTypes(), "getName");
		types.addMenuElementFirst("", this.iwrb.getLocalizedString("case_creator.select_type", "Select type"));
		types.keepStatusOnAction(true);
		types.setStyleClass("caseTypeDropdown");

		CaseType firstType = getCasesBusiness(iwc).getFirstAvailableCaseType();
		HiddenInput hiddenType = new HiddenInput(PARAMETER_CASE_TYPE_PK, firstType != null ? firstType.getPrimaryKey().toString() : "");
		
		TextInput regarding = new TextInput(PARAMETER_REGARDING);
		regarding.keepStatusOnAction(true);
		
		TextArea message = new TextArea(PARAMETER_MESSAGE);
		message.setStyleClass("textarea");
		// message.keepStatusOnAction(true);
		String messageText = getMessageParameterValue(iwc);
		if (messageText != null) {
			message.setContent(messageText);
		}

		Layer helpLayer = new Layer(Layer.DIV);
		helpLayer.setStyleClass("helperText");
		String helperText = this.iwrb.getLocalizedString(getPrefix() + "case_creator.information_text", "Information text here...");
		// If the category has a description use it, subcategories override!
		String tempHelperText = null; // so we don't make useless calls for localized texts from the db!
		if (subCategory != null && (tempHelperText = subCategory.getLocalizedCategoryDescription(locale)) != null && !"".equals(tempHelperText)) {
			helperText = tempHelperText;
		}
		else if (category != null && (tempHelperText = category.getLocalizedCategoryDescription(locale)) != null && !"".equals(tempHelperText)) {
			helperText = tempHelperText;
		}

		helpLayer.add(new Text(helperText));
		section.add(helpLayer);

		if (this.iUseAnonymous && !iShowSenderInputs) {
			Layer helpLayerExtra = new Layer(Layer.DIV);
			helpLayerExtra.setStyleClass("helperTextExtra");
			helpLayerExtra.add(new Text(this.iwrb.getLocalizedString(getPrefix() + "case_creator.information_text_extra", "Please note that we can only answer notifications from registered users due to the fact that anonymous notifications do not include any information about the sender.")));
			helpLayer.add(helpLayerExtra);
		}

		if (getCasesBusiness(iwc).useTypes()) {
			Layer formItem = new Layer(Layer.DIV);
			formItem.setStyleClass("formItem");
			formItem.setStyleClass("required");
			if (hasError(PARAMETER_CASE_TYPE_PK)) {
				formItem.setStyleClass("hasError");
			}
			Label label = new Label(new Span(new Text(this.iwrb.getLocalizedString("case_type", "Case type"))), types);
			formItem.add(label);
			formItem.add(types);
			section.add(formItem);
		}
		else {
			form.add(hiddenType);
		}

		Layer formItem = new Layer(Layer.DIV);
		formItem.setStyleClass("formItem");
		formItem.setStyleClass("required");
		if (hasError(PARAMETER_CASE_CATEGORY_PK)) {
			formItem.setStyleClass("hasError");
		}
		Label label = new Label(new Span(new Text(this.iwrb.getLocalizedString("case_category", "Case category"))), categories);
		formItem.add(label);
		formItem.add(categories);
		section.add(formItem);

		if (getCasesBusiness(iwc).useSubCategories()) {
			formItem = new Layer(Layer.DIV);
			formItem.setStyleClass("formItem");
			formItem.setStyleClass("required");
			if (hasError(PARAMETER_SUB_CASE_CATEGORY_PK)) {
				formItem.setStyleClass("hasError");
			}
			label = new Label(new Span(new Text(this.iwrb.getLocalizedString("sub_case_category", "Sub case category"))), subCategories);
			formItem.add(label);
			formItem.add(subCategories);
			section.add(formItem);
		}

		if (getCasesBusiness(iwc).allowAttachments()) {
			FileInput file = new FileInput();
			file.keepStatusOnAction(true);

			formItem = new Layer(Layer.DIV);
			formItem.setStyleClass("formItem");
			formItem.setID("attachment");
			label = new Label(this.iwrb.getLocalizedString("attachment", "Attachment"), file);
			formItem.add(label);
			formItem.add(file);
			section.add(formItem);
		}

		if (this.iShowRegarding) {
			formItem = new Layer(Layer.DIV);
			formItem.setStyleClass("formItem");
			formItem.setStyleClass("required");
			if (hasError(PARAMETER_REGARDING)) {
				formItem.setStyleClass("hasError");
			}
			label = new Label(new Span(new Text(this.iwrb.getLocalizedString(getPrefix() + "regarding", "Regarding"))), regarding);
			formItem.add(label);
			formItem.add(regarding);
			section.add(formItem);
		}
		
		if (this.iShowSenderInputs) {
			TextInput reference = new TextInput(PARAMETER_REFERENCE);
			reference.keepStatusOnAction(true);

			formItem = new Layer(Layer.DIV);
			formItem.setStyleClass("formItem");
			label = new Label(new Span(new Text(this.iwrb.getLocalizedString("reference", "Reference"))), reference);
			formItem.add(label);
			formItem.add(reference);
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

		if (getCasesBusiness(iwc).allowPrivateCases() && !iUseAnonymous) {
			heading = new Heading1(this.iwrb.getLocalizedString(getPrefix() + "case_creator.private_case_info", "Private case information"));
			heading.setStyleClass("subHeader");
			form.add(heading);

			section = new Layer(Layer.DIV);
			section.setStyleClass("formSection");
			form.add(section);

			CheckBox isPrivate = new CheckBox(PARAMETER_PRIVATE, Boolean.TRUE.toString());
			isPrivate.setStyleClass("checkbox");
			isPrivate.keepStatusOnAction(true);

			Paragraph paragraph = new Paragraph();
			paragraph.setStyleClass("privateText");
			paragraph.add(new Text(this.iwrb.getLocalizedString(getPrefix() + "case_creator.private_text", "If you would like your case to be handled confidentially please check the checkbox here below.")));
			section.add(paragraph);

			formItem = new Layer(Layer.DIV);
			formItem.setStyleClass("formItem");
			formItem.setStyleClass("radioButtonItem");
			label = new Label(new Span(new Text(this.iwrb.getLocalizedString(getPrefix() + "case_creator.request_private_handling", "I request for my case to be handled confidentially"))), isPrivate);
			formItem.add(isPrivate);
			formItem.add(label);
			section.add(formItem);

			section.add(clear);
		}
		
		if (iShowSenderInputs) {
			List scripts = new ArrayList();
			scripts.add("/dwr/interface/CasesBusiness.js");
			scripts.add(CoreConstants.DWR_ENGINE_SCRIPT);
			scripts.add(CoreConstants.DWR_UTIL_SCRIPT);
			PresentationUtil.addJavaScriptSourcesLinesToHeader(iwc, scripts);

			StringBuffer script = new StringBuffer();
			script.append("function readUser() {\n\tvar id = dwr.util.getValue(\"" + PARAMETER_PERSONAL_ID + "\");\n\tCasesBusiness.getUser(id, fillUser);\n}");

			StringBuffer script2 = new StringBuffer();
			script2.append("function fillUser(auser) {\n\tdwr.util.setValues(auser);\n}");

			Script formScript = new Script();
			formScript.addFunction("readUser", script.toString());
			formScript.addFunction("fillUser", script2.toString());
			form.add(formScript);

			heading = new Heading1(this.iwrb.getLocalizedString(getPrefix() + "case_creator.sender_info", "Sender information"));
			heading.setStyleClass("subHeader");
			form.add(heading);

			section = new Layer(Layer.DIV);
			section.setStyleClass("formSection");
			form.add(section);

			TextInput personalID = new TextInput(PARAMETER_PERSONAL_ID);
			personalID.setMaxlength(10);
			personalID.keepStatusOnAction(true);
			personalID.setOnKeyUp("readUser();");
			personalID.setOnChange("readUser();");
			
			formItem = new Layer(Layer.DIV);
			formItem.setStyleClass("formItem");
			formItem.setStyleClass("required");
			if (hasError(PARAMETER_PERSONAL_ID)) {
				formItem.setStyleClass("hasError");
			}
			label = new Label(new Span(new Text(this.iwrb.getLocalizedString("personal_id", "Personal ID"))), personalID);
			formItem.add(label);
			formItem.add(personalID);
			section.add(formItem);
			

			TextInput name = new TextInput(PARAMETER_NAME);
			name.setID("userName");
			if (iwc.isParameterSet(PARAMETER_PERSONAL_ID)) {
				try {
					User sender = getUserBusiness(iwc).getUser(iwc.getParameter(PARAMETER_PERSONAL_ID));
					name.setContent(new Name(sender.getFirstName(), sender.getMiddleName(), sender.getLastName()).getName());
				}
				catch (FinderException fe) {
					log(fe);
				}
			}
			name.keepStatusOnAction(true);
			name.setDisabled(true);
			
			formItem = new Layer(Layer.DIV);
			formItem.setStyleClass("formItem");
			formItem.setStyleClass("required");
			if (hasError(PARAMETER_NAME)) {
				formItem.setStyleClass("hasError");
			}
			label = new Label(new Span(new Text(this.iwrb.getLocalizedString("name", "Name"))), name);
			formItem.add(label);
			formItem.add(name);
			section.add(formItem);

			TextInput email = new TextInput(PARAMETER_EMAIL);
			email.setID("userEmail");
			email.keepStatusOnAction(true);
			
			formItem = new Layer(Layer.DIV);
			formItem.setStyleClass("formItem");
			formItem.setStyleClass("required");
			if (hasError(PARAMETER_EMAIL)) {
				formItem.setStyleClass("hasError");
			}
			label = new Label(new Span(new Text(this.iwrb.getLocalizedString("email", "Email"))), email);
			formItem.add(label);
			formItem.add(email);
			section.add(formItem);

			TextInput phone = new TextInput(PARAMETER_PHONE);
			phone.setID("userPhone");
			phone.keepStatusOnAction(true);
			
			formItem = new Layer(Layer.DIV);
			formItem.setStyleClass("formItem");
			formItem.setStyleClass("required");
			if (hasError(PARAMETER_PHONE)) {
				formItem.setStyleClass("hasError");
			}
			label = new Label(new Span(new Text(this.iwrb.getLocalizedString("phone", "Phone"))), phone);
			formItem.add(label);
			formItem.add(phone);
			section.add(formItem);
		}

		Layer bottom = new Layer(Layer.DIV);
		bottom.setStyleClass("bottom");
		form.add(bottom);

		Link next = getButtonLink(this.iwrb.getLocalizedString("next", "Next"));
		next.setValueOnClick(PARAMETER_ACTION, String.valueOf(ACTION_OVERVIEW));
		next.setToFormSubmit(form);
		bottom.add(next);

		add(form);
	}

	protected void showOverview(IWContext iwc) throws RemoteException {
		Locale locale = iwc.getCurrentLocale();

		Object caseTypePK = iwc.getParameter(PARAMETER_CASE_TYPE_PK);
		Object caseCategoryPK = iwc.getParameter(PARAMETER_CASE_CATEGORY_PK);
		Object subCaseCategoryPK = iwc.getParameter(PARAMETER_SUB_CASE_CATEGORY_PK);
		String regarding = iwc.getParameter(PARAMETER_REGARDING);
		String message = getMessageParameterValue(iwc);

		ICFile attachment = null;
		UploadFile uploadFile = iwc.getUploadedFile();
		if (uploadFile != null && uploadFile.getName() != null && uploadFile.getName().length() > 0) {
			try {
				FileInputStream input = new FileInputStream(uploadFile.getRealPath());

				attachment = ((ICFileHome) IDOLookup.getHome(ICFile.class)).create();
				attachment.setName(uploadFile.getName());
				attachment.setMimeType(uploadFile.getMimeType());
				attachment.setFileValue(input);
				attachment.setFileSize((int) uploadFile.getSize());
				attachment.store();

				uploadFile.setId(((Integer) attachment.getPrimaryKey()).intValue());
				try {
					FileUtil.delete(uploadFile);
				}
				catch (Exception ex) {
					System.err.println("MediaBusiness: deleting the temporary file at " + uploadFile.getRealPath() + " failed.");
				}
			}
			catch (RemoteException e) {
				e.printStackTrace(System.err);
				uploadFile.setId(-1);
			}
			catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			catch (CreateException ce) {
				ce.printStackTrace();
			}
		}

		CaseCategory category = null;
		if (caseCategoryPK != null && !"".equals(caseCategoryPK)) {
			try {
				category = getCasesBusiness(iwc).getCaseCategory(caseCategoryPK);
			}
			catch (FinderException fe) {
				throw new IBORuntimeException(fe);
			}
		}

		CaseCategory subCategory = null;
		if (getCasesBusiness(iwc).useSubCategories() && subCaseCategoryPK != null && !"".equals(subCaseCategoryPK)) {
			try {
				subCategory = getCasesBusiness(iwc).getCaseCategory(subCaseCategoryPK);
			}
			catch (FinderException fe) {
				throw new IBORuntimeException(fe);
			}
		}

		CaseType type = null;
		if (caseTypePK != null && !"".equals(caseTypePK)) {
			try {
				type = getCasesBusiness(iwc).getCaseType(caseTypePK);
			}
			catch (FinderException fe) {
				throw new IBORuntimeException(fe);
			}
		}

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
		if (this.iShowRegarding && !iwc.isParameterSet(PARAMETER_REGARDING)) {
			setError(PARAMETER_REGARDING, this.iwrb.getLocalizedString(getPrefix() + "case_creator.regarding_empty", "You must enter what the case is regarding"));
		}
		if (!iwc.isParameterSet(PARAMETER_MESSAGE)) {
			setError(PARAMETER_MESSAGE, this.iwrb.getLocalizedString(getPrefix() + "case_creator.message_empty", "You must enter a message"));
		}
		
		if (iShowSenderInputs) {
			if (!iwc.isParameterSet(PARAMETER_PERSONAL_ID)) {
				setError(PARAMETER_PERSONAL_ID, iwrb.getLocalizedString("case_creator.personal_id_empty", "You must enter personal ID"));
			}
			else if (!SocialSecurityNumber.isValidSocialSecurityNumber(iwc.getParameter(PARAMETER_PERSONAL_ID), iwc.getCurrentLocale())) {
				setError(PARAMETER_PERSONAL_ID, iwrb.getLocalizedString("case_creator.personal_id_invalid", "You must enter a valid personal ID"));
			}
			else {
				String personalID = iwc.getParameter(PARAMETER_PERSONAL_ID);
				try {
					getUserBusiness(iwc).getUser(personalID);
				}
				catch (FinderException fe) {
					setError(PARAMETER_PERSONAL_ID, iwrb.getLocalizedString("case_creator.no_user_found", "No user found with supplied personal ID"));
				}
			}
			if (!iwc.isParameterSet(PARAMETER_EMAIL)) {
				setError(PARAMETER_EMAIL, iwrb.getLocalizedString("case_creator.email_empty", "You must enter email"));
			}
			else if (!EmailValidator.getInstance().validateEmail(iwc.getParameter(PARAMETER_EMAIL))) {
				setError(PARAMETER_EMAIL, iwrb.getLocalizedString("case_creator.email_invalid", "You must enter a valid email"));
			}
			if (!iwc.isParameterSet(PARAMETER_PHONE)) {
				setError(PARAMETER_PHONE, iwrb.getLocalizedString("case_creator.phone_empty", "You must enter phone"));
			}
		}

		if (hasErrors()) {
			showPhaseOne(iwc);
			return;
		}

		User user = getUser(iwc);

		Form form = new Form();
		form.setStyleClass("casesForm");
		form.setStyleClass("overview");
		form.addParameter(PARAMETER_ACTION, String.valueOf(ACTION_OVERVIEW));

		// form.maintainParameter(PARAMETER_MESSAGE);
		// cannot use from maintainParameter here because the message can contain html letters like < > and that isn't encoded in the form
		form.maintainParameter(PARAMETER_CASE_TYPE_PK);
		form.maintainParameter(PARAMETER_CASE_CATEGORY_PK);
		form.maintainParameter(PARAMETER_SUB_CASE_CATEGORY_PK);
		form.maintainParameter(PARAMETER_PRIVATE);
		form.maintainParameter(PARAMETER_REGARDING);
		form.maintainParameter(PARAMETER_NAME);
		form.maintainParameter(PARAMETER_PERSONAL_ID);
		form.maintainParameter(PARAMETER_EMAIL);
		form.maintainParameter(PARAMETER_PHONE);
		form.maintainParameter(PARAMETER_REFERENCE);
		if (attachment != null) {
			form.add(new HiddenInput(PARAMETER_ATTACHMENT_PK, attachment.getPrimaryKey().toString()));
		}

		String headingText = this.iwrb.getLocalizedString(getPrefix() + (this.iUseAnonymous ? "anonymous_application.case_creator" : "application.case_creator"), "Case creator");
		if (category != null) {
			headingText += " - " + category.getLocalizedCategoryName(locale);
		}
		Heading1 heading = new Heading1(this.iwrb.getLocalizedString(getPrefix() + "application.case_creator", "Case creator"));
		heading.setStyleClass("applicationHeading");
		form.add(heading);

		form.add(getPhasesHeader(this.iwrb.getLocalizedString(getPrefix() + "application.overview", "Overview"), 2, 3));

		form.add(getPersonInfo(iwc, user));

		heading = new Heading1(this.iwrb.getLocalizedString(getPrefix() + "case_creator.enter_case_overview", "New case overview"));
		heading.setStyleClass("subHeader");
		heading.setStyleClass("topSubHeader");
		form.add(heading);

		Layer section = new Layer(Layer.DIV);
		section.setStyleClass("formSection");
		form.add(section);

		Layer typeSpan = new Layer(Layer.SPAN);
		typeSpan.add(new Text(type.getName()));

		Layer categorySpan = new Layer(Layer.SPAN);
		categorySpan.add(new Text(category.getLocalizedCategoryName(locale)));

		Span regardingSpan = new Span(new Text(regarding));

		Layer messageSpan = new Layer(Layer.SPAN);
		messageSpan.add(new Text(message));

		if (getCasesBusiness(iwc).useTypes()) {
			Layer formItem = new Layer(Layer.DIV);
			formItem.setStyleClass("formItem");
			Label label = new Label();
			label.setLabel(this.iwrb.getLocalizedString("case_type", "Case type"));
			formItem.add(label);
			formItem.add(typeSpan);
			section.add(formItem);
		}

		Layer formItem = new Layer(Layer.DIV);
		formItem.setStyleClass("formItem");
		Label label = new Label();
		label.setLabel(this.iwrb.getLocalizedString("case_category", "Case category"));
		formItem.add(label);
		formItem.add(categorySpan);
		section.add(formItem);

		if (getCasesBusiness(iwc).useSubCategories() && !subCategory.equals(category)) {
			Layer subCategorySpan = new Layer(Layer.SPAN);
			subCategorySpan.add(new Text(subCategory.getLocalizedCategoryName(locale)));

			formItem = new Layer(Layer.DIV);
			formItem.setStyleClass("formItem");
			label = new Label();
			label.setLabel(this.iwrb.getLocalizedString("sub_case_category", "Sub case category"));
			formItem.add(label);
			formItem.add(subCategorySpan);
			section.add(formItem);
		}

		if (attachment != null) {
			Link link = new Link(new Text(attachment.getName()));
			link.setFile(attachment);
			link.setTarget(Link.TARGET_BLANK_WINDOW);

			Layer attachmentSpan = new Layer(Layer.SPAN);
			attachmentSpan.add(link);

			formItem = new Layer(Layer.DIV);
			formItem.setStyleClass("formItem");
			label = new Label();
			label.setLabel(this.iwrb.getLocalizedString("attachment", "Attachment"));
			formItem.add(label);
			formItem.add(attachmentSpan);
			section.add(formItem);
		}

		if (this.iShowRegarding) {
			formItem = new Layer(Layer.DIV);
			formItem.setStyleClass("formItem");
			label = new Label();
			label.setLabel(this.iwrb.getLocalizedString(getPrefix() + "regarding", "Regarding"));
			formItem.add(label);
			formItem.add(regardingSpan);
			section.add(formItem);
		}

		if (iwc.isParameterSet(PARAMETER_REFERENCE)) {
			formItem = new Layer(Layer.DIV);
			formItem.setStyleClass("formItem");
			formItem.setStyleClass("informationItem");
			label = new Label();
			label.setLabel(this.iwrb.getLocalizedString("reference", "Reference"));
			formItem.add(label);
			formItem.add(new Span(new Text(iwc.getParameter(PARAMETER_REFERENCE))));
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
		
		if (iShowSenderInputs) {
			heading = new Heading1(this.iwrb.getLocalizedString("case_creator.sender_overview", "Sender overview"));
			heading.setStyleClass("subHeader");
			form.add(heading);

			section = new Layer(Layer.DIV);
			section.setStyleClass("formSection");
			form.add(section);
			
			formItem = new Layer(Layer.DIV);
			formItem.setStyleClass("formItem");
			formItem.setStyleClass("informationItem");
			label = new Label();
			label.setLabel(this.iwrb.getLocalizedString("personal_id", "Personal ID"));
			formItem.add(label);
			formItem.add(new Span(new Text(PersonalIDFormatter.format(iwc.getParameter(PARAMETER_PERSONAL_ID), iwc.getCurrentLocale()))));
			section.add(formItem);

			try {
				User sender = getUserBusiness(iwc).getUser(iwc.getParameter(PARAMETER_PERSONAL_ID));
				Name name = new Name(sender.getFirstName(), sender.getMiddleName(), sender.getLastName());

				formItem = new Layer(Layer.DIV);
				formItem.setStyleClass("formItem");
				formItem.setStyleClass("informationItem");
				label = new Label();
				label.setLabel(this.iwrb.getLocalizedString("name", "Name"));
				formItem.add(label);
				formItem.add(new Span(new Text(name.getName(iwc.getCurrentLocale()))));
				section.add(formItem);
			}
			catch (FinderException fe) {
				log(fe);
			}
			
			formItem = new Layer(Layer.DIV);
			formItem.setStyleClass("formItem");
			formItem.setStyleClass("informationItem");
			label = new Label();
			label.setLabel(this.iwrb.getLocalizedString("email", "Email"));
			formItem.add(label);
			formItem.add(new Span(new Text(iwc.getParameter(PARAMETER_EMAIL))));
			section.add(formItem);
			
			formItem = new Layer(Layer.DIV);
			formItem.setStyleClass("formItem");
			formItem.setStyleClass("informationItem");
			label = new Label();
			label.setLabel(this.iwrb.getLocalizedString("phone", "phone"));
			formItem.add(label);
			formItem.add(new Span(new Text(iwc.getParameter(PARAMETER_PHONE))));
			section.add(formItem);
		}

		Layer bottom = new Layer(Layer.DIV);
		bottom.setStyleClass("bottom");
		form.add(bottom);

		Link back = getButtonLink(this.iwrb.getLocalizedString("previous", "Previous"));
		back.setStyleClass("homeButton");
		back.setValueOnClick(PARAMETER_ACTION, String.valueOf(ACTION_PHASE_1));
		back.setToFormSubmit(form);
		bottom.add(back);

		Link next = getButtonLink(this.iwrb.getLocalizedString("send", "Send"));
		next.setValueOnClick(PARAMETER_ACTION, String.valueOf(ACTION_SAVE));
		next.setToFormSubmit(form);
		bottom.add(next);

		add(form);
	}

	protected void save(IWContext iwc) throws RemoteException {
		String regarding = iwc.getParameter(PARAMETER_REGARDING);
		String message = getMessageParameterValue(iwc);
		iwc.removeSessionAttribute(PARAMETER_MESSAGE);

		Object caseCategoryPK = iwc.getParameter(PARAMETER_CASE_CATEGORY_PK);
		Object subCaseCategoryPK = iwc.getParameter(PARAMETER_SUB_CASE_CATEGORY_PK);
		Object caseTypePK = iwc.getParameter(PARAMETER_CASE_TYPE_PK);
		Object attachmentPK = iwc.getParameter(PARAMETER_ATTACHMENT_PK);
		boolean isPrivate = iwc.isParameterSet(PARAMETER_PRIVATE);
		Locale locale = iwc.getCurrentLocale();
		
		String personalID = iwc.getParameter(PARAMETER_PERSONAL_ID);
		String email = iwc.getParameter(PARAMETER_EMAIL);
		String phone = iwc.getParameter(PARAMETER_PHONE);
		String reference = iwc.getParameter(PARAMETER_REFERENCE);

		CaseCategory category = null;
		if (caseCategoryPK != null && !"".equals(caseCategoryPK)) {
			try {
				category = getCasesBusiness(iwc).getCaseCategory(caseCategoryPK);
			}
			catch (FinderException fe) {
				throw new IBORuntimeException(fe);
			}
		}

		try {
			User user = getUser(iwc);
			if (personalID != null) {
				try {
					user = getUserBusiness(iwc).getUser(personalID);
				}
				catch (FinderException fe) {
					log(fe);
				}
			}
			GeneralCase theCase = getCasesBusiness(iwc).storeGeneralCase(user, getCasesBusiness(iwc).useSubCategories() ? subCaseCategoryPK : caseCategoryPK, caseTypePK, attachmentPK, regarding, message, iShowSenderInputs ? null : getType(), isPrivate, getCasesBusiness(iwc).getIWResourceBundleForUser(user, iwc, this.getBundle(iwc)));
			if (iShowSenderInputs) {
				theCase.setReference(reference);
				theCase.store();

				if (user != null) {
					getUserBusiness(iwc).updateUserMail(user, email);
					getUserBusiness(iwc).updateUserHomePhone(user, phone);
				}
			}
			
			String headingText = this.iwrb.getLocalizedString(getPrefix() + (this.iUseAnonymous ? "anonymous_application.case_creator" : "application.case_creator"), "Case creator");
			if (category != null) {
				headingText += " - " + category.getLocalizedCategoryName(locale);
			}
			Heading1 heading = new Heading1(this.iwrb.getLocalizedString(getPrefix() + "application.case_creator", "Case creator"));
			heading.setStyleClass("applicationHeading");
			add(heading);

			addPhasesReceipt(iwc, this.iwrb.getLocalizedString(getPrefix() + "case_creator.save_completed", "Application sent"), this.iwrb.getLocalizedString(getPrefix() + "case_creator.save_completed", "Application sent"), user != null ? this.iwrb.getLocalizedString(getPrefix() + "case_creator.save_confirmation", "Your case has been sent and will be processed accordingly.") : this.iwrb.getLocalizedString(getPrefix() + "anonymous_case_creator.save_confirmation", "Your case has been sent and will be processed accordingly."), 3, 3);

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

	/**
	 * @param iwc
	 * @return
	 */
	protected String getMessageParameterValue(IWContext iwc) {
		String message = iwc.getParameter(PARAMETER_MESSAGE);

		if (message == null) {
			message = (String) iwc.getSessionAttribute(PARAMETER_MESSAGE);
		}
		else {
			iwc.setSessionAttribute(PARAMETER_MESSAGE, message);
		}

		return message;
	}

	protected String getPrefix() {
		if (getType() != null) {
			return getType() + ".";
		}
		else {
			return "";
		}
	}

	protected User getUser(IWContext iwc) throws RemoteException {
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

	protected UserBusiness getUserBusiness(IWApplicationContext iwac) {
		try {
			return (UserBusiness) IBOLookup.getServiceInstance(iwac, UserBusiness.class);
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

	public void setShowSenderInputs(boolean showSenderInputs) {
		this.iShowSenderInputs = showSenderInputs;
	}

	public void setShowRegarding(boolean showRegarding) {
		this.iShowRegarding = showRegarding;
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