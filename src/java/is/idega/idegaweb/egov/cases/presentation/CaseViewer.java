/*
 * $Id$ Created on Oct 31, 2005
 * 
 * Copyright (C) 2005 Idega Software hf. All Rights Reserved.
 * 
 * This software is the proprietary information of Idega hf. Use is subject to license terms.
 */
package is.idega.idegaweb.egov.cases.presentation;

import is.idega.idegaweb.egov.cases.business.CaseWriter;
import is.idega.idegaweb.egov.cases.data.CaseCategory;
import is.idega.idegaweb.egov.cases.data.CaseType;
import is.idega.idegaweb.egov.cases.data.GeneralCase;
import com.idega.block.process.business.CaseConstants;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Iterator;

import javax.ejb.FinderException;

import com.idega.block.process.data.Case;
import com.idega.block.process.data.CaseLog;
import com.idega.block.process.data.CaseStatus;
import com.idega.business.IBORuntimeException;
import com.idega.core.builder.data.ICPage;
import com.idega.core.file.data.ICFile;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.Span;
import com.idega.presentation.text.DownloadLink;
import com.idega.presentation.text.Heading1;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.HiddenInput;
import com.idega.presentation.ui.Label;
import com.idega.presentation.ui.TextArea;
import com.idega.presentation.ui.util.SelectorUtility;
import com.idega.user.data.Group;
import com.idega.user.data.User;
import com.idega.util.IWTimestamp;
import com.idega.util.PresentationUtil;
import com.idega.util.text.Name;

public class CaseViewer extends CaseCreator {

	public static final String PARAMETER_ACTION = "cp_prm_action";

	public static final String PARAMETER_ACTION_REACTIVATE = "prm_action_reactivate";
	public static final String PARAMETER_ACTION_REVIEW = "prm_action_review";
	public static final String PARAMETER_CASE_PK = "prm_case_pk";
	private static final String PARAMETER_USER = "prm_user";

	private static final int ACTION_VIEW = 1;
	private static final int ACTION_SEND_REMINDER = 2;
	private static final int ACTION_SEND = 3;
	private static final int ACTION_ALLOCATION_FORM = 4;
	private static final int ACTION_ALLOCATE = 5;

	private ICPage iHomePage;
	private ICPage iBackPage;

	@Override
	protected void present(IWContext iwc) {
		PresentationUtil.addStyleSheetToHeader(iwc, getBundle(iwc).getVirtualPathWithFileNameString("style/case.css"));
		
		try {
			if (iwc.isParameterSet(PARAMETER_CASE_PK)) {
				if (iwc.isParameterSet(PARAMETER_ACTION_REACTIVATE)) {
					try {
						getCasesBusiness(iwc).reactivateCase(iwc.getParameter(PARAMETER_CASE_PK), iwc.getCurrentUser(), iwc);
					}
					catch (FinderException e) {
						e.printStackTrace();
					}
				}
				else if (iwc.isParameterSet(PARAMETER_ACTION_REVIEW)) {
					try {
						getCasesBusiness(iwc).reviewCase(iwc.getParameter(PARAMETER_CASE_PK), iwc.getCurrentUser(), iwc);
					}
					catch (FinderException e) {
						e.printStackTrace();
					}
				}
			}

			IWResourceBundle iwrb = getResourceBundle(iwc);

			Form form = new Form();
			form.setStyleClass("adminForm");
			form.setStyleClass("overview");

			GeneralCase theCase = null;
			try {
				String casePK = iwc.getParameter(getCasesBusiness(iwc).getSelectedCaseParameter());
				if (casePK == null || "".equals(casePK)) {
					casePK = iwc.getParameter(PARAMETER_CASE_PK);
				}

				if (casePK == null) {
					add("No case selected...");
					return;
				}

				theCase = getCasesBusiness(iwc).getGeneralCase(casePK);
			}
			catch (FinderException fe) {
				fe.printStackTrace();
				throw new IBORuntimeException(fe);
			}
			
			if(theCase.getCaseManagerType() != null) {
				
//				TODO: show bpm view (where is this used)
				return;
			}
			
			CaseCategory category = theCase.getCaseCategory();
			CaseCategory parentCategory = category.getParent();
			CaseStatus status = theCase.getCaseStatus();
			CaseType type = theCase.getCaseType();
			ICFile attachment = theCase.getAttachment();
			User user = getCasesBusiness(iwc).getLastModifier(theCase);
			User owner = theCase.getOwner();
			if (user != null && user.equals(owner)) {
				user = null;
			}
			IWTimestamp created = new IWTimestamp(theCase.getCreated());

			form.add(getHeader(iwrb.getLocalizedString(getPrefix() + "case_viewer.view_case", "View case")));

			form.add(getPersonInfo(iwc, theCase.getOwner()));

			Layer clearLayer = new Layer(Layer.DIV);
			clearLayer.setStyleClass("Clear");

			Layer caseType = new Layer(Layer.SPAN);
			caseType.add(new Text(type.getName()));

			Layer caseCategory = new Layer(Layer.SPAN);
			caseCategory.add(new Text(category.getLocalizedCategoryName(iwc.getCurrentLocale())));

			Layer message = new Layer(Layer.SPAN);
			message.add(new Text(theCase.getMessage()));

			Layer createdDate = new Layer(Layer.SPAN);
			createdDate.add(new Text(created.getLocaleDateAndTime(iwc.getCurrentLocale(), IWTimestamp.SHORT, IWTimestamp.SHORT)));

			Heading1 heading = new Heading1(iwrb.getLocalizedString(getPrefix() + "case_overview", "Case overview"));
			heading.setStyleClass("subHeader");
			heading.setStyleClass("topSubHeader");
			form.add(heading);

			Layer section = new Layer(Layer.DIV);
			section.setStyleClass("formSection");
			form.add(section);

			if (theCase.isPrivate()) {
				section.add(getAttentionLayer(iwrb.getLocalizedString(getPrefix() + "case.is_private", "The sender wishes that this case be handled as confidential.")));
			}
			
			Layer formItem = new Layer(Layer.DIV);
			formItem.setStyleClass("formItem");
			Label label = new Label();
			label.setLabel(iwrb.getLocalizedString(getPrefix() + "case_nr", "Case nr"));
			formItem.add(label);
			formItem.add(new Span(new Text(theCase.getPrimaryKey().toString())));
			section.add(formItem);

			if (getCasesBusiness(iwc).useTypes()) {
				formItem = new Layer(Layer.DIV);
				formItem.setStyleClass("formItem");
				label = new Label();
				label.setLabel(iwrb.getLocalizedString("case_type", "Case type"));
				formItem.add(label);
				formItem.add(caseType);
				section.add(formItem);
			}

			if (parentCategory != null) {
				Layer parentCaseCategory = new Layer(Layer.SPAN);
				parentCaseCategory.add(new Text(parentCategory.getLocalizedCategoryName(iwc.getCurrentLocale())));

				formItem = new Layer(Layer.DIV);
				formItem.setStyleClass("formItem");
				label = new Label();
				label.setLabel(iwrb.getLocalizedString("case_category", "Case category"));
				formItem.add(label);
				formItem.add(parentCaseCategory);
				section.add(formItem);

				formItem = new Layer(Layer.DIV);
				formItem.setStyleClass("formItem");
				label = new Label();
				label.setLabel(iwrb.getLocalizedString("sub_case_category", "Case category"));
				formItem.add(label);
				formItem.add(caseCategory);
				section.add(formItem);
			}
			else {
				formItem = new Layer(Layer.DIV);
				formItem.setStyleClass("formItem");
				label = new Label();
				label.setLabel(iwrb.getLocalizedString("case_category", "Case category"));
				formItem.add(label);
				formItem.add(caseCategory);
				section.add(formItem);
			}

			formItem = new Layer(Layer.DIV);
			formItem.setStyleClass("formItem");
			label = new Label();
			label.setLabel(iwrb.getLocalizedString("created_date", "Created date"));
			formItem.add(label);
			formItem.add(createdDate);
			section.add(formItem);

			if (attachment != null) {
				Link link = new Link(new Text(attachment.getName()));
				link.setFile(attachment);
				link.setTarget(Link.TARGET_BLANK_WINDOW);

				Layer attachmentSpan = new Layer(Layer.SPAN);
				attachmentSpan.add(link);

				formItem = new Layer(Layer.DIV);
				formItem.setStyleClass("formItem");
				label = new Label();
				label.setLabel(iwrb.getLocalizedString("attachment", "Attachment"));
				formItem.add(label);
				formItem.add(attachmentSpan);
				section.add(formItem);
			}
			
			if (theCase.getSubject() != null) {
				formItem = new Layer(Layer.DIV);
				formItem.setStyleClass("formItem");
				label = new Label();
				label.setLabel(iwrb.getLocalizedString(getPrefix() + "regarding", "Regarding"));
				formItem.add(label);
				formItem.add(new Span(new Text(theCase.getSubject())));
				section.add(formItem);
			}

			formItem = new Layer(Layer.DIV);
			formItem.setStyleClass("formItem");
			formItem.setStyleClass("informationItem");
			label = new Label();
			label.setLabel(iwrb.getLocalizedString(getPrefix() + "message", "Message"));
			formItem.add(label);
			formItem.add(message);
			section.add(formItem);

			Layer clear = new Layer(Layer.DIV);
			clear.setStyleClass("Clear");
			section.add(clear);

			@SuppressWarnings("unchecked")
			Collection<CaseLog> logs = getCasesBusiness(iwc).getCaseLogs(theCase);
			
			if (!logs.isEmpty()) {
				for (CaseLog log : logs)
					form.add(getHandlerLayer(iwc, iwrb, theCase, log));
			} else {
				Layer handler = new Layer(Layer.SPAN);
				if (user != null) {
					handler.add(new Text(new Name(user.getFirstName(), user.getMiddleName(), user.getLastName()).getName(iwc.getCurrentLocale(), true)));
				}
				else {
					handler.add(new Text(""));
				}
				heading = new Heading1(iwrb.getLocalizedString("handler_overview", "Handler overview"));
				heading.setStyleClass("subHeader");
				form.add(heading);

				section = new Layer(Layer.DIV);
				section.setStyleClass("formSection");
				form.add(section);

				formItem = new Layer(Layer.DIV);
				formItem.setStyleClass("formItem");
				label = new Label();
				label.setLabel(iwrb.getLocalizedString("handler", "Handler"));
				formItem.add(label);
				formItem.add(handler);
				section.add(formItem);

				formItem = new Layer(Layer.DIV);
				formItem.setStyleClass("formItem");
				label = new Label();
				label.setLabel(iwrb.getLocalizedString("status", "Status"));
				formItem.add(label);
				formItem.add(new Span(new Text(getCasesBusiness(iwc).getLocalizedCaseStatusDescription(theCase, status, iwc.getCurrentLocale()))));
				section.add(formItem);

				if (theCase.getReply() != null && theCase.getReply().length() > 0) {
					Layer reply = new Layer(Layer.SPAN);
					reply.add(new Text(theCase.getReply()));

					formItem = new Layer(Layer.DIV);
					formItem.setStyleClass("formItem");
					formItem.setStyleClass("informationItem");
					label = new Label();
					label.setLabel(iwrb.getLocalizedString("reply", "Reply"));
					formItem.add(label);
					formItem.add(reply);
					section.add(formItem);
				}

				section.add(clear);
			}

			Layer bottom = new Layer(Layer.DIV);
			bottom.setStyleClass("bottom");
			form.add(bottom);

			if (this.iBackPage != null) {
				Link home = getButtonLink(iwrb.getLocalizedString("back", "Back"));
				home.setStyleClass("buttonHome");
				home.setPage(this.iBackPage);
				bottom.add(home);
			}

			if (getHomePage() != null) {
				Link home = getButtonLink(iwrb.getLocalizedString("my_page", "My page"));
				home.setStyleClass("buttonHome");
				home.setPage(getHomePage());
				bottom.add(home);
			}
			
			if (!iwc.getCurrentUser().equals(owner)) {
				Link pdf = getDownloadButtonLink(iwrb.getLocalizedString("fetch_pdf", "Fetch PDF"), CaseWriter.class);
				pdf.addParameter(getCasesBusiness(iwc).getSelectedCaseParameter(), theCase.getPrimaryKey().toString());
				bottom.add(pdf);
			}

			if (status.equals(getCasesBusiness(iwc).getCaseStatusInactive()) || status.equals(getCasesBusiness(iwc).getCaseStatusReady())) {
				Link next = getButtonLink(iwrb.getLocalizedString(getPrefix() + "reactivate_case", "Reactivate case"));
				next.addParameter(PARAMETER_ACTION_REACTIVATE, Boolean.TRUE.toString());
				next.addParameter(PARAMETER_CASE_PK, theCase.getPrimaryKey().toString());
				next.maintainParameter(iwc.getParameter(getCasesBusiness(iwc).getSelectedCaseParameter()), iwc);
				bottom.add(next);
			}
			else if (iwc.getCurrentUser().equals(owner) && status.equals(getCasesBusiness(iwc).getCaseStatusOpen())) {
				Link next = getButtonLink(iwrb.getLocalizedString(getPrefix() + "review_case", "Review case"));
				next.addParameter(PARAMETER_ACTION_REVIEW, Boolean.TRUE.toString());
				next.addParameter(PARAMETER_CASE_PK, theCase.getPrimaryKey().toString());
				next.maintainParameter(iwc.getParameter(getCasesBusiness(iwc).getSelectedCaseParameter()), iwc);
				bottom.add(next);
			}
			else if (iwc.getAccessController().hasRole(CaseConstants.ROLE_CASES_SUPER_ADMIN, iwc) && (status.equals(getCasesBusiness(iwc).getCaseStatusPending()) || status.equals(getCasesBusiness(iwc).getCaseStatusWaiting()))) {
				Link sendReminder = getButtonLink(iwrb.getLocalizedString("send_reminder", "Send reminder"));
				sendReminder.addParameter(PARAMETER_ACTION, String.valueOf(ACTION_SEND_REMINDER));
				sendReminder.addParameter(getCasesBusiness(iwc).getSelectedCaseParameter(), theCase.getPrimaryKey().toString());
				bottom.add(sendReminder);

				Link allocate = getButtonLink(iwrb.getLocalizedString(getPrefix() + "allocate_case", "Allocate case"));
				allocate.addParameter(PARAMETER_ACTION, String.valueOf(ACTION_ALLOCATION_FORM));
				allocate.addParameter(getCasesBusiness(iwc).getSelectedCaseParameter(), theCase.getPrimaryKey().toString());
				bottom.add(allocate);
			}

			add(form);
		}
		catch (RemoteException re) {
			throw new IBORuntimeException(re);
		}
	}

	private Layer getHandlerLayer(IWContext iwc, IWResourceBundle iwrb, Case theCase, CaseLog log) throws RemoteException {
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
	
	protected void showReminderForm(IWContext iwc, GeneralCase theCase) throws RemoteException {
		Form form = new Form();
		form.setStyleClass("adminForm");
		form.maintainParameter(getCasesBusiness(iwc).getSelectedCaseParameter());
		form.addParameter(PARAMETER_ACTION, String.valueOf(ACTION_VIEW));

		IWResourceBundle iwrb = getResourceBundle(iwc);

		form.add(getHeader(iwrb.getLocalizedString("case_viewer.send_reminder", "Send reminder")));
		form.add(getPersonInfo(iwc, theCase.getOwner()));

		Layer section = new Layer(Layer.DIV);
		section.setStyleClass("formSection");
		form.add(section);

		Layer clear = new Layer(Layer.DIV);
		clear.setStyleClass("Clear");

		TextArea message = new TextArea(PARAMETER_MESSAGE);
		message.setStyleClass("textarea");
		message.keepStatusOnAction(true);

		Layer element = new Layer(Layer.DIV);
		element.setStyleClass("formItem");
		Label label = new Label(iwrb.getLocalizedString("allocation_message", "Message"), message);
		element.add(label);
		element.add(message);
		section.add(element);

		section.add(clear);

		Layer bottom = new Layer(Layer.DIV);
		bottom.setStyleClass("bottom");
		form.add(bottom);

		Link back = getButtonLink(iwrb.getLocalizedString("back", "Back"));
		back.setStyleClass("homeButton");
		back.setValueOnClick(PARAMETER_ACTION, String.valueOf(ACTION_VIEW));
		back.setToFormSubmit(form);
		bottom.add(back);

		Link next = getButtonLink(iwrb.getLocalizedString("send_reminder", "Send reminder"));
		next.setValueOnClick(PARAMETER_ACTION, String.valueOf(ACTION_SEND));
		next.setToFormSubmit(form);
		bottom.add(next);

		add(form);
	}

	protected void showAllocationForm(IWContext iwc, GeneralCase theCase) throws RemoteException {
		IWBundle iwb = getBundle(iwc);
		IWResourceBundle iwrb = getResourceBundle(iwc);

		Form form = new Form();
		form.setStyleClass("adminForm");
		form.maintainParameter(getCasesBusiness(iwc).getSelectedCaseParameter());
		form.addParameter(PARAMETER_ACTION, ACTION_VIEW);

		boolean useSubCategories = getCasesBusiness(iwc).useSubCategories();

		super.getParentPage().addJavascriptURL("/dwr/interface/CasesDWRUtil.js");
		super.getParentPage().addJavascriptURL("/dwr/engine.js");
		super.getParentPage().addJavascriptURL("/dwr/util.js");
		super.getParentPage().addJavascriptURL(iwb.getResourcesVirtualPath() + "/js/navigation.js");

		Layer section = new Layer(Layer.DIV);
		section.setStyleClass("formSection");
		form.add(section);

		Layer helpLayer = new Layer(Layer.DIV);
		helpLayer.setStyleClass("helperText");
		helpLayer.add(new Text(iwrb.getLocalizedString("allocate_case_help", "Please select the user to allocate the case to and write a message that will be sent to the user selected.")));
		section.add(helpLayer);

		Layer clear = new Layer(Layer.DIV);
		clear.setStyleClass("Clear");

		CaseCategory category = theCase.getCaseCategory();
		CaseCategory parentCategory = category.getParent();
		CaseType type = theCase.getCaseType();
		Group handlerGroup = category.getHandlerGroup();
		Collection handlers = getUserBusiness(iwc).getUsersInGroup(handlerGroup);

		SelectorUtility util = new SelectorUtility();
		DropdownMenu categories = (DropdownMenu) util.getSelectorFromIDOEntities(new DropdownMenu(PARAMETER_CASE_CATEGORY_PK), getCasesBusiness(iwc).getCaseCategories(), "getName");
		categories.setID(PARAMETER_CASE_CATEGORY_PK);
		categories.setSelectedElement(parentCategory != null ? parentCategory.getPrimaryKey().toString() : category.getPrimaryKey().toString());
		categories.setStyleClass("caseCategoryDropdown");
		if (useSubCategories) {
			categories.setOnChange("changeSubCategories('" + PARAMETER_CASE_CATEGORY_PK + "', '" + iwc.getCurrentLocale().getCountry() + "')");
		}
		categories.setOnChange("changeUsers('" + PARAMETER_CASE_CATEGORY_PK + "')");

		DropdownMenu subCategories = new DropdownMenu(PARAMETER_SUB_CASE_CATEGORY_PK);
		subCategories.setID(PARAMETER_SUB_CASE_CATEGORY_PK);
		subCategories.setSelectedElement(category.getPrimaryKey().toString());
		subCategories.setStyleClass("subCaseCategoryDropdown");
		subCategories.setOnChange("changeUsers('" + PARAMETER_SUB_CASE_CATEGORY_PK + "')");

		Collection collection = getCasesBusiness(iwc).getSubCategories(parentCategory != null ? parentCategory : category);
		if (collection.isEmpty()) {
			subCategories.addMenuElement(category.getPrimaryKey().toString(), iwrb.getLocalizedString("case_creator.no_sub_category", "no sub category"));
		}
		else {
			subCategories.addMenuElement(category.getPrimaryKey().toString(), iwrb.getLocalizedString("case_creator.select_sub_category", "Select sub category"));
			Iterator iter = collection.iterator();
			while (iter.hasNext()) {
				CaseCategory subCategory = (CaseCategory) iter.next();
				subCategories.addMenuElement(subCategory.getPrimaryKey().toString(), subCategory.getLocalizedCategoryName(iwc.getCurrentLocale()));
			}
		}

		DropdownMenu types = (DropdownMenu) util.getSelectorFromIDOEntities(new DropdownMenu(PARAMETER_CASE_TYPE_PK), getCasesBusiness(iwc).getCaseTypes(), "getName");
		types.keepStatusOnAction(true);
		types.setSelectedElement(type.getPrimaryKey().toString());
		types.setStyleClass("caseTypeDropdown");

		HiddenInput hiddenType = new HiddenInput(PARAMETER_CASE_TYPE_PK, type.getPrimaryKey().toString());

		DropdownMenu users = new DropdownMenu(PARAMETER_USER);
		users.setID(PARAMETER_USER);

		Iterator iter = handlers.iterator();
		while (iter.hasNext()) {
			User handler = (User) iter.next();
			users.addMenuElement(handler.getPrimaryKey().toString(), handler.getName());
		}

		TextArea message = new TextArea(PARAMETER_MESSAGE);
		message.setStyleClass("textarea");
		message.keepStatusOnAction(true);

		if (getCasesBusiness(iwc).useTypes()) {
			Layer element = new Layer(Layer.DIV);
			element.setStyleClass("formItem");
			Label label = new Label(iwrb.getLocalizedString("case_type", "Case type"), types);
			element.add(label);
			element.add(types);
			section.add(element);
		}
		else {
			form.add(hiddenType);
		}

		Layer element = new Layer(Layer.DIV);
		element.setStyleClass("formItem");
		Label label = new Label(iwrb.getLocalizedString("case_category", "Case category"), categories);
		element.add(label);
		element.add(categories);
		section.add(element);

		if (useSubCategories) {
			element = new Layer(Layer.DIV);
			element.setStyleClass("formItem");
			label = new Label(iwrb.getLocalizedString("sub_case_category", "Sub case category"), subCategories);
			element.add(label);
			element.add(subCategories);
			section.add(element);
		}

		element = new Layer(Layer.DIV);
		element.setStyleClass("formItem");
		label = new Label(iwrb.getLocalizedString("handler", "Handler"), users);
		element.add(label);
		element.add(users);
		section.add(element);

		element = new Layer(Layer.DIV);
		element.setStyleClass("formItem");
		label = new Label(iwrb.getLocalizedString("allocation_message", "Message"), message);
		element.add(label);
		element.add(message);
		section.add(element);

		section.add(clear);

		Layer bottom = new Layer(Layer.DIV);
		bottom.setStyleClass("bottom");
		form.add(bottom);

		Link back = getButtonLink(iwrb.getLocalizedString("back", "Back"));
		back.setStyleClass("homeButton");
		back.setValueOnClick(PARAMETER_ACTION, String.valueOf(ACTION_VIEW));
		back.setToFormSubmit(form);
		bottom.add(back);

		Link next = getButtonLink(iwrb.getLocalizedString("allocate", "Allocate"));
		next.setValueOnClick(PARAMETER_ACTION, String.valueOf(ACTION_ALLOCATE));
		next.setToFormSubmit(form);
		bottom.add(next);

		add(form);
	}

	protected void sendReminder(IWContext iwc, GeneralCase theCase) throws RemoteException {
		String message = iwc.getParameter(PARAMETER_MESSAGE);
		User user = getCasesBusiness(iwc).getLastModifier(theCase);

		getCasesBusiness(iwc).sendReminder(theCase, user, iwc.getCurrentUser(), message, iwc);

		IWResourceBundle iwrb = getResourceBundle(iwc);

		addReceipt(iwc, iwrb.getLocalizedString("case_view.reminder_sent", "Reminder sent"), iwrb.getLocalizedString("case_view.reminder_sent_subject", "A reminder was sent"), iwrb.getLocalizedString("case_viewer.reminder_sent_body", "You have successfully sent a reminder for completion of the case."));

		Layer clearLayer = new Layer(Layer.DIV);
		clearLayer.setStyleClass("Clear");
		add(clearLayer);

		Layer bottom = new Layer(Layer.DIV);
		bottom.setStyleClass("bottom");
		add(bottom);

		Link link = getButtonLink(iwrb.getLocalizedString("back", "Back"));
		link.addParameter(getCasesBusiness(iwc).getSelectedCaseParameter(), theCase.getPrimaryKey().toString());
		link.setStyleClass("homeButton");
		bottom.add(link);
	}

	protected void allocate(IWContext iwc, GeneralCase theCase) throws RemoteException {
		Object caseCategoryPK = iwc.isParameterSet(PARAMETER_CASE_CATEGORY_PK) ? iwc.getParameter(PARAMETER_CASE_CATEGORY_PK) : null;
		Object subCaseCategoryPK = iwc.isParameterSet(PARAMETER_SUB_CASE_CATEGORY_PK) ? iwc.getParameter(PARAMETER_SUB_CASE_CATEGORY_PK) : null;
		Object caseTypePK = iwc.isParameterSet(PARAMETER_CASE_TYPE_PK) ? iwc.getParameter(PARAMETER_CASE_TYPE_PK) : null;
		try {
			Object userPK = iwc.getParameter(PARAMETER_USER);
			String message = iwc.getParameter(PARAMETER_MESSAGE);

			User user = getUserBusiness(iwc).getUser(new Integer(userPK.toString()));

			getCasesBusiness(iwc).allocateCase(theCase, subCaseCategoryPK != null ? subCaseCategoryPK : caseCategoryPK, caseTypePK, user, message, iwc.getCurrentUser(), iwc);
		}
		catch (RemoteException e) {
			throw new IBORuntimeException(e);
		}

		IWResourceBundle iwrb = getResourceBundle(iwc);

		addReceipt(iwc, iwrb.getLocalizedString("case_viewer.reallocation_completed", "Allocation completed"), iwrb.getLocalizedString("case_viewer.reallocation_completed_subject", "Allocation completed"), iwrb.getLocalizedString("case_viewer.reallocation_completed_body", "You have successfully allocated the case."));

		Layer clearLayer = new Layer(Layer.DIV);
		clearLayer.setStyleClass("Clear");
		add(clearLayer);

		Layer bottom = new Layer(Layer.DIV);
		bottom.setStyleClass("bottom");
		add(bottom);

		Link link = getButtonLink(iwrb.getLocalizedString("back", "Back"));
		link.addParameter(getCasesBusiness(iwc).getSelectedCaseParameter(), theCase.getPrimaryKey().toString());
		link.setStyleClass("homeButton");
		bottom.add(link);
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

	protected ICPage getHomePage() {
		return this.iHomePage;
	}

	public void setHomePage(ICPage page) {
		this.iHomePage = page;
	}

	public void setBackPage(ICPage backPage) {
		this.iBackPage = backPage;
	}
}