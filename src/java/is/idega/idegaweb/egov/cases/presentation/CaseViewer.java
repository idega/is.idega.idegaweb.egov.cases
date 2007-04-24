/*
 * $Id$ Created on Oct 31, 2005
 * 
 * Copyright (C) 2005 Idega Software hf. All Rights Reserved.
 * 
 * This software is the proprietary information of Idega hf. Use is subject to license terms.
 */
package is.idega.idegaweb.egov.cases.presentation;

import is.idega.idegaweb.egov.cases.data.CaseCategory;
import is.idega.idegaweb.egov.cases.data.CaseType;
import is.idega.idegaweb.egov.cases.data.GeneralCase;

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
import com.idega.idegaweb.IWResourceBundle;
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
import com.idega.util.text.Name;

public class CaseViewer extends CaseCreator {

	public static final String PARAMETER_ACTION = "cp_prm_action";

	public static final String PARAMETER_ACTION_REACTIVATE = "prm_action_reactivate";
	public static final String PARAMETER_ACTION_REVIEW = "prm_action_review";
	public static final String PARAMETER_CASE_PK = "prm_case_pk";

	protected static final int ACTION_SAVE = 1;

	private ICPage iHomePage;
	private ICPage iBackPage;

	protected void present(IWContext iwc) {
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

			if (getCasesBusiness(iwc).useTypes()) {
				Layer formItem = new Layer(Layer.DIV);
				formItem.setStyleClass("formItem");
				Label label = new Label();
				label.setLabel(iwrb.getLocalizedString("case_type", "Case type"));
				formItem.add(label);
				formItem.add(caseType);
				section.add(formItem);
			}

			if (parentCategory != null) {
				Layer parentCaseCategory = new Layer(Layer.SPAN);
				parentCaseCategory.add(new Text(parentCategory.getLocalizedCategoryName(iwc.getCurrentLocale())));

				Layer formItem = new Layer(Layer.DIV);
				formItem.setStyleClass("formItem");
				Label label = new Label();
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
				Layer formItem = new Layer(Layer.DIV);
				formItem.setStyleClass("formItem");
				Label label = new Label();
				label.setLabel(iwrb.getLocalizedString("case_category", "Case category"));
				formItem.add(label);
				formItem.add(caseCategory);
				section.add(formItem);
			}

			Layer formItem = new Layer(Layer.DIV);
			formItem.setStyleClass("formItem");
			Label label = new Label();
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

			Collection logs = getCasesBusiness(iwc).getCaseLogs(theCase);
			if (!logs.isEmpty()) {
				Iterator iter = logs.iterator();
				while (iter.hasNext()) {
					CaseLog log = (CaseLog) iter.next();
					form.add(getHandlerLayer(iwc, iwrb, theCase, log));
				}
			}
			else {
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