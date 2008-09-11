/*
 * $Id$ Created on Nov 7, 2005
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

import java.rmi.RemoteException;

import javax.ejb.FinderException;

import com.idega.block.process.presentation.UserCases;
import com.idega.business.IBORuntimeException;
import com.idega.core.file.data.ICFile;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.Span;
import com.idega.presentation.text.Heading1;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.Label;
import com.idega.user.data.User;
import com.idega.util.IWTimestamp;

public class ClosedCases extends CasesProcessor {
	
	public static final String TYPE = "ClosedCases";

	protected String getBlockID() {
		return "closedCases";
	}

//	protected Collection<GeneralCase> getCases(User user) {
//		
//		Collection<GeneralCase> cases = super.getCases(user);
//		Collection<GeneralCase> closedCases = getClosedCases(user, getIWApplicationContext().getIWMainApplication(), getIWUserContext(), getUserBusiness(), getCasesBusiness(), null);
//		
//		if(cases != null)
//			closedCases.addAll(cases);
//		
//		return closedCases;
//	}

	protected void showProcessor(IWContext iwc, Object casePK) throws RemoteException {
		Form form = new Form();
		form.setStyleClass("adminForm");
		form.setStyleClass("overview");
		form.addParameter(UserCases.PARAMETER_ACTION, "");
		form.maintainParameter(PARAMETER_CASE_PK);

		GeneralCase theCase = null;
		try {
			theCase = getCasesBusiness().getGeneralCase(casePK);
		}
		catch (FinderException fe) {
			fe.printStackTrace();
			throw new IBORuntimeException(fe);
		}
		CaseCategory category = theCase.getCaseCategory();
		CaseCategory parentCategory = category.getParent();
		CaseType type = theCase.getCaseType();
		ICFile attachment = theCase.getAttachment();
		User owner = theCase.getOwner();
		IWTimestamp created = new IWTimestamp(theCase.getCreated());

		form.add(getPersonInfo(iwc, owner));

		Heading1 heading = new Heading1(getResourceBundle().getLocalizedString(getPrefix() + "case_overview", "Case overview"));
		heading.setStyleClass("subHeader");
		heading.setStyleClass("topSubHeader");
		form.add(heading);

		Layer layer = new Layer(Layer.DIV);
		layer.setStyleClass("formSection");
		form.add(layer);

		if (theCase.isPrivate()) {
			layer.add(getAttentionLayer(getResourceBundle().getLocalizedString(getPrefix() + "case.is_private", "The sender wishes that this case be handled as confidential.")));
		}

		Layer caseType = new Layer(Layer.SPAN);
		caseType.add(new Text(type.getName()));

		Layer caseCategory = new Layer(Layer.SPAN);
		caseCategory.add(new Text(category.getLocalizedCategoryName(iwc.getCurrentLocale())));

		Layer message = new Layer(Layer.SPAN);
		message.add(new Text(theCase.getMessage()));

		Layer reply = new Layer(Layer.SPAN);
		reply.add(new Text(theCase.getReply()));

		Layer handler = new Layer(Layer.SPAN);
		if (theCase.getHandledBy() != null) {
			handler.add(new Text(theCase.getHandledBy().getName()));
		}
		else {
			handler.add(new Text("-"));
		}

		Layer createdDate = new Layer(Layer.SPAN);
		createdDate.add(new Text(created.getLocaleDateAndTime(iwc.getCurrentLocale(), IWTimestamp.SHORT, IWTimestamp.SHORT)));

		Layer formItem = new Layer(Layer.DIV);
		formItem.setStyleClass("formItem");
		Label label = new Label();
		label.setLabel(getResourceBundle().getLocalizedString("case_nr", "Case nr"));
		formItem.add(label);
		formItem.add(new Span(new Text(theCase.getPrimaryKey().toString())));
		layer.add(formItem);
		
		if (getCasesBusiness().useTypes()) {
			Layer element = new Layer(Layer.DIV);
			element.setStyleClass("formItem");
			label = new Label();
			label.setLabel(getResourceBundle().getLocalizedString("case_type", "Case type"));
			element.add(label);
			element.add(caseType);
			layer.add(element);
		}

		if (parentCategory != null) {
			Layer parentCaseCategory = new Layer(Layer.SPAN);
			parentCaseCategory.add(new Text(parentCategory.getLocalizedCategoryName(iwc.getCurrentLocale())));

			Layer element = new Layer(Layer.DIV);
			element.setStyleClass("formItem");
			label = new Label();
			label.setLabel(getResourceBundle().getLocalizedString("case_category", "Case category"));
			element.add(label);
			element.add(parentCaseCategory);
			layer.add(element);

			element = new Layer(Layer.DIV);
			element.setStyleClass("formItem");
			label = new Label();
			label.setLabel(getResourceBundle().getLocalizedString("sub_case_category", "Sub case category"));
			element.add(label);
			element.add(caseCategory);
			layer.add(element);
		}
		else {
			Layer element = new Layer(Layer.DIV);
			element.setStyleClass("formItem");
			label = new Label();
			label.setLabel(getResourceBundle().getLocalizedString("case_category", "Case category"));
			element.add(label);
			element.add(caseCategory);
			layer.add(element);
		}

		Layer element = new Layer(Layer.DIV);
		element.setStyleClass("formItem");
		label = new Label();
		label.setLabel(getResourceBundle().getLocalizedString("created_date", "Created date"));
		element.add(label);
		element.add(createdDate);
		layer.add(element);

		if (attachment != null) {
			Link link = new Link(new Text(attachment.getName()));
			link.setFile(attachment);
			link.setTarget(Link.TARGET_BLANK_WINDOW);

			Layer attachmentSpan = new Layer(Layer.SPAN);
			attachmentSpan.add(link);

			element = new Layer(Layer.DIV);
			element.setStyleClass("formItem");
			label = new Label();
			label.setLabel(getResourceBundle().getLocalizedString("attachment", "Attachment"));
			element.add(label);
			element.add(attachmentSpan);
			layer.add(element);
		}

		element = new Layer(Layer.DIV);
		element.setStyleClass("formItem");
		element.setStyleClass("informationItem");
		label = new Label();
		label.setLabel(getResourceBundle().getLocalizedString(getPrefix() + "message", "Message"));
		element.add(label);
		element.add(message);
		layer.add(element);

		Layer clear = new Layer(Layer.DIV);
		clear.setStyleClass("Clear");
		layer.add(clear);

		heading = new Heading1(getResourceBundle().getLocalizedString("handle_overview", "Handle overview"));
		heading.setStyleClass("subHeader");
		form.add(heading);

		layer = new Layer(Layer.DIV);
		layer.setStyleClass("formSection");
		form.add(layer);

		element = new Layer(Layer.DIV);
		element.setStyleClass("formItem");
		label = new Label();
		label.setLabel(getResourceBundle().getLocalizedString("handler", "Handler"));
		element.add(label);
		element.add(handler);
		layer.add(element);

		element = new Layer(Layer.DIV);
		element.setStyleClass("formItem");
		element.setStyleClass("informationItem");
		label = new Label();
		label.setLabel(getResourceBundle().getLocalizedString("reply", "Reply"));
		element.add(label);
		element.add(reply);
		layer.add(element);

		layer.add(clear);

		Layer bottom = new Layer(Layer.DIV);
		bottom.setStyleClass("bottom");
		form.add(bottom);

		Link back = getButtonLink(getResourceBundle().getLocalizedString("back", "Back"));
		back.setStyleClass("homeButton");
		back.setValueOnClick(UserCases.PARAMETER_ACTION, String.valueOf(ACTION_VIEW));
		back.setToFormSubmit(form);
		bottom.add(back);
		
		Link pdf = getDownloadButtonLink(getResourceBundle().getLocalizedString("fetch_pdf", "Fetch PDF"), CaseWriter.class);
		pdf.addParameter(getCasesBusiness().getSelectedCaseParameter(), theCase.getPrimaryKey().toString());
		bottom.add(pdf);

		Link next = getButtonLink(getResourceBundle().getLocalizedString(getPrefix() + "reactivate_case", "Reactivate case"));
		next.setValueOnClick(UserCases.PARAMETER_ACTION, String.valueOf(ACTION_SAVE));
		next.setToFormSubmit(form);
		bottom.add(next);

		add(form);
	}

	protected void save(IWContext iwc) throws RemoteException {
		Object casePK = iwc.getParameter(PARAMETER_CASE_PK);

		try {
			getCasesBusiness().reactivateCase(casePK, iwc.getCurrentUser(), iwc);
		}
		catch (FinderException fe) {
			fe.printStackTrace();
		}
	}

	protected boolean showCheckBox() {
		return false;
	}
	
	protected void initializeTableSorter(IWContext iwc) throws RemoteException {
		StringBuffer buffer = new StringBuffer();
		buffer.append("$(document).ready(function() { $('#" + getBlockID() + "').tablesorter( { headers: { " + (getCasesBusiness().useTypes() ? 3 : 2) + ": { sorter: false }, " + (getCasesBusiness().useTypes() ? 6 : 5) + ": { sorter: false}" + (showCheckBox() ? ", " + (getCasesBusiness().useTypes() ? 7 : 6) + ": { sorter: false}" : "") + "}, sortList: [[0,1]] } ); } );");

		super.getParentPage().getAssociatedScript().addFunction("tableSorter", buffer.toString());
	}
	
	@Override
	protected String getCasesProcessorType() {
		return TYPE;
	}
	
//	@SuppressWarnings("unchecked")
//	public static Collection<GeneralCase> getClosedCases(User user, IWMainApplication iwma, IWUserContext iwuc, UserBusiness userBusiness, CasesBusiness casesBusiness, String[] caseHandlers) {
//		
//		try {
//			boolean isCaseSuperAdmin = iwma.getAccessController().hasRole(CasesConstants.ROLE_CASES_SUPER_ADMIN, iwuc);
//			
//			Collection groups = userBusiness.getUserGroupsDirectlyRelated(user);
//			Collection<GeneralCase> closedCases;
//			
//			if(caseHandlers == null) {
//			
//				closedCases = casesBusiness.getClosedCases(!isCaseSuperAdmin ? groups : null);
//			} else {
//				
//				closedCases = casesBusiness.getClosedCases(!isCaseSuperAdmin ? groups : null, caseHandlers);
//			}
//			return closedCases;
//			
//		} catch (RemoteException e) {
//			throw new IBORuntimeException(e);
//		}
//	}
}