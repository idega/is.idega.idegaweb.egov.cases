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

import com.idega.block.process.data.CaseStatus;
import com.idega.business.IBORuntimeException;
import com.idega.core.builder.data.ICPage;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.Table2;
import com.idega.presentation.TableCell2;
import com.idega.presentation.TableColumn;
import com.idega.presentation.TableColumnGroup;
import com.idega.presentation.TableRow;
import com.idega.presentation.TableRowGroup;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.CheckBox;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.Label;
import com.idega.presentation.ui.SubmitButton;
import com.idega.presentation.ui.TextArea;
import com.idega.user.data.Group;
import com.idega.user.data.User;
import com.idega.util.IWTimestamp;
import com.idega.util.text.Name;

public abstract class CasesProcessor extends CasesBlock {

	public static final String PARAMETER_ACTION = "cp_prm_action";

	public static final String PARAMETER_CASE_PK = "prm_case_pk";
	protected static final String PARAMETER_REPLY = "prm_reply";
	protected static final String PARAMETER_STATUS = "prm_status";
	protected static final String PARAMETER_USER = "prm_iser";
	protected static final String PARAMETER_MESSAGE = "prm_message";

	protected static final int ACTION_VIEW = 1;
	public static final int ACTION_PROCESS = 2;
	protected static final int ACTION_SAVE = 3;
	protected static final int ACTION_MULTI_PROCESS_FORM = 4;
	protected static final int ACTION_MULTI_PROCESS = 5;
	protected static final int ACTION_ALLOCATION_FORM = 6;

	protected abstract String getBlockID();
	
	private ICPage jbpmProcessViewerPage;

	protected void present(IWContext iwc) throws Exception {
		switch (parseAction(iwc)) {
			case ACTION_VIEW:
				showList(iwc);
				break;

			case ACTION_PROCESS:
				
				showProcessor(iwc, iwc.getParameter(PARAMETER_CASE_PK));
				break;

			case ACTION_SAVE:
				save(iwc);
				showList(iwc);
				break;

			case ACTION_MULTI_PROCESS_FORM:
				showMultiProcessForm(iwc);
				break;

			case ACTION_MULTI_PROCESS:
				multiProcess(iwc);
				showList(iwc);
				break;

			case ACTION_ALLOCATION_FORM:
				showAllocationForm(iwc, iwc.getParameter(PARAMETER_CASE_PK));
				break;
		}
	}

	private int parseAction(IWContext iwc) {
		if (iwc.isParameterSet(PARAMETER_ACTION)) {
			return Integer.parseInt(iwc.getParameter(PARAMETER_ACTION));
		}
		return ACTION_VIEW;
	}

	private void showList(IWContext iwc) throws RemoteException {
		
		Form form = new Form();
		form.addParameter(PARAMETER_ACTION, ACTION_MULTI_PROCESS_FORM);

		Table2 table = new Table2();
		table.setWidth("100%");
		table.setCellpadding(0);
		table.setCellspacing(0);
		table.setStyleClass("adminTable");
		table.setStyleClass("casesTable");
		table.setStyleClass("ruler");
		table.setID(getBlockID());

		TableColumnGroup columnGroup = table.createColumnGroup();
		TableColumn column = columnGroup.createColumn();
		column.setSpan(6);
		column = columnGroup.createColumn();
		column.setSpan(1);
		column.setWidth("12");

		@SuppressWarnings("unchecked")
		Collection<GeneralCase> cases = getCases(iwc.getCurrentUser());

		TableRowGroup group = table.createHeaderRowGroup();
		TableRow row = group.createRow();

		boolean showCheckBoxes = showCheckBox() && getCasesBusiness(iwc).allowAnonymousCases();
		TableCell2 cell = row.createHeaderCell();
		cell.setStyleClass("firstColumn");
		cell.setStyleClass("caseNumber");
		cell.add(new Text(getResourceBundle().getLocalizedString(getPrefix() + "case_nr", "Case nr.")));

		cell = row.createHeaderCell();
		cell.setStyleClass("sender");
		cell.add(new Text(getResourceBundle().getLocalizedString("sender", "Sender")));

		if (getBusiness().useTypes()) {
			cell = row.createHeaderCell();
			cell.setStyleClass("caseType");
			cell.add(new Text(getResourceBundle().getLocalizedString("case_type", "Case type")));
		}

		cell = row.createHeaderCell();
		cell.setStyleClass("createdDate");
		cell.add(new Text(getResourceBundle().getLocalizedString("created_date", "Created date")));

		cell = row.createHeaderCell();
		cell.setStyleClass("status");
		cell.add(new Text(getResourceBundle().getLocalizedString("status", "Status")));

		cell = row.createHeaderCell();
		cell.setStyleClass("handler");
		cell.add(new Text(getResourceBundle().getLocalizedString("handler", "Handler")));

		cell = row.createHeaderCell();
		if (!showCheckBoxes) {
			cell.setStyleClass("lastColumn");
		}
		cell.setStyleClass("view");
		cell.add(new Text(getResourceBundle().getLocalizedString("view", "View")));

		if (showCheckBoxes) {
			cell = row.createHeaderCell();
			cell.setStyleClass("lastColumn");
			cell.setStyleClass("multiHandle");
			cell.add(Text.getNonBrakingSpace());
		}

		group = table.createBodyRowGroup();
		int iRow = 1;

		Iterator<GeneralCase> iter = cases.iterator();
		
		while (iter.hasNext()) {
			
			GeneralCase theCase = iter.next();
			CaseStatus status = theCase.getCaseStatus();
			CaseType type = theCase.getCaseType();
			User owner = theCase.getOwner();
			IWTimestamp created = new IWTimestamp(theCase.getCreated());

			row = group.createRow();
			if (iRow == 1) {
				row.setStyleClass("firstRow");
			}
			else if (!iter.hasNext()) {
				row.setStyleClass("lastRow");
			}
			if (theCase.isPrivate()) {
				row.setStyleClass("isPrivate");
			}
			if (status.equals(getCasesBusiness(iwc).getCaseStatusReview())) {
				row.setStyleClass("isReview");
			}

			cell = row.createCell();
			cell.setStyleClass("firstColumn");
			cell.setStyleClass("caseNumber");
			cell.add(new Text(theCase.getPrimaryKey().toString()));

			cell = row.createCell();
			cell.setStyleClass("sender");
			if (owner != null) {
				cell.add(new Text(new Name(owner.getFirstName(), owner.getMiddleName(), owner.getLastName()).getName(iwc.getCurrentLocale())));
			}
			else {
				cell.add(new Text("-"));
			}

			if (getBusiness().useTypes()) {
				cell = row.createCell();
				cell.setStyleClass("caseType");
				cell.add(new Text(type.getName()));
			}

			cell = row.createCell();
			cell.setStyleClass("createdDate");
			cell.add(new Text(created.getLocaleDateAndTime(iwc.getCurrentLocale(), IWTimestamp.SHORT, IWTimestamp.SHORT)));

			cell = row.createCell();
			cell.setStyleClass("status");
			cell.add(new Text(getBusiness().getLocalizedCaseStatusDescription(theCase, status, iwc.getCurrentLocale())));

			User handler = theCase.getHandledBy();
			cell = row.createCell();
			cell.setStyleClass("handler");
			if (handler != null) {
				cell.add(new Text(new Name(handler.getFirstName(), handler.getMiddleName(), handler.getLastName()).getName(iwc.getCurrentLocale())));
			}
			else {
				cell.add(new Text("-"));
			}

			cell = row.createCell();
			if (!showCheckBoxes) {
				cell.setStyleClass("lastColumn");
			}
			cell.setStyleClass("view");
			
			cell.add(getProcessLink(getBundle().getImage("edit.png", getResourceBundle().getLocalizedString(getPrefix() + "view_case", "View case")), theCase));

			if (showCheckBoxes) {
				CheckBox box = new CheckBox(PARAMETER_CASE_PK, theCase.getPrimaryKey().toString());

				cell = row.createCell();
				cell.setStyleClass("firstColumn");
				cell.setStyleClass("multiHandle");
				cell.add(box);
			}

			if (iRow % 2 == 0) {
				row.setStyleClass("evenRow");
			}
			else {
				row.setStyleClass("oddRow");
			}
		}

		form.add(table);
		form.add(getLegend(iwc));

		if (showCheckBoxes) {
			Layer layer = new Layer();
			layer.setStyleClass("buttonLayer");
			layer.setStyleClass("multiProcessLayer");
			form.add(layer);

			SubmitButton multiProcess = new SubmitButton(getResourceBundle().getLocalizedString("multi_process", "Multi process"), PARAMETER_ACTION, String.valueOf(ACTION_MULTI_PROCESS_FORM));
			multiProcess.setStyleClass("button");
			layer.add(multiProcess);
		}

		add(form);
	}

	protected void showMultiProcessForm(IWContext iwc) throws RemoteException {
		Form form = new Form();
		form.setStyleClass("adminForm");
		form.maintainParameter(PARAMETER_CASE_PK);
		form.addParameter(PARAMETER_ACTION, "");

		Layer section = new Layer(Layer.DIV);
		section.setStyleClass("formSection");
		form.add(section);

		Layer clear = new Layer(Layer.DIV);
		clear.setStyleClass("Clear");

		Layer helpLayer = new Layer(Layer.DIV);
		helpLayer.setStyleClass("helperText");
		helpLayer.add(new Text(getResourceBundle().getLocalizedString("multi_process_help", "Please select a new status for the cases and write in a reply.")));
		section.add(helpLayer);

		DropdownMenu statuses = new DropdownMenu(PARAMETER_STATUS);
		statuses.addMenuElement(getBusiness().getCaseStatusReady().getStatus(), getBusiness().getLocalizedCaseStatusDescription(getBusiness().getCaseStatusReady(), iwc.getCurrentLocale()));
		statuses.addMenuElement(getBusiness().getCaseStatusWaiting().getStatus(), getBusiness().getLocalizedCaseStatusDescription(getBusiness().getCaseStatusWaiting(), iwc.getCurrentLocale()));
		statuses.addMenuElement(getBusiness().getCaseStatusPending().getStatus(), getBusiness().getLocalizedCaseStatusDescription(getBusiness().getCaseStatusPending(), iwc.getCurrentLocale()));

		TextArea reply = new TextArea(PARAMETER_REPLY);
		reply.setStyleClass("textarea");
		reply.keepStatusOnAction(true);

		Layer element = new Layer(Layer.DIV);
		element.setStyleClass("formItem");
		Label label = new Label(getResourceBundle().getLocalizedString("status", "status"), statuses);
		element.add(label);
		element.add(statuses);
		section.add(element);

		element = new Layer(Layer.DIV);
		element.setStyleClass("formItem");
		label = new Label(getResourceBundle().getLocalizedString("reply", "Reply"), reply);
		element.add(label);
		element.add(reply);
		section.add(element);

		section.add(clear);

		Layer bottom = new Layer(Layer.DIV);
		bottom.setStyleClass("bottom");
		form.add(bottom);

		Link back = getButtonLink(getResourceBundle().getLocalizedString("back", "Back"));
		back.setStyleClass("homeButton");
		back.setValueOnClick(PARAMETER_ACTION, String.valueOf(ACTION_VIEW));
		back.setToFormSubmit(form);
		bottom.add(back);

		Link next = getButtonLink(getResourceBundle().getLocalizedString("process", "Process"));
		next.setValueOnClick(PARAMETER_ACTION, String.valueOf(ACTION_MULTI_PROCESS));
		next.setToFormSubmit(form);
		bottom.add(next);

		add(form);
	}

	protected void showAllocationForm(IWContext iwc, Object casePK) throws RemoteException {
		Form form = new Form();
		form.setStyleClass("adminForm");
		form.maintainParameter(PARAMETER_CASE_PK);
		form.addParameter(PARAMETER_ACTION, "");

		Layer section = new Layer(Layer.DIV);
		section.setStyleClass("formSection");
		form.add(section);

		Layer helpLayer = new Layer(Layer.DIV);
		helpLayer.setStyleClass("helperText");
		helpLayer.add(new Text(getResourceBundle().getLocalizedString("allocate_case_help", "Please select the user to allocate the case to and write a message that will be sent to the user selected.")));
		section.add(helpLayer);

		Layer clear = new Layer(Layer.DIV);
		clear.setStyleClass("Clear");

		GeneralCase theCase = null;
		try {
			theCase = getBusiness().getGeneralCase(casePK);
		}
		catch (FinderException fe) {
			fe.printStackTrace();
			throw new IBORuntimeException(fe);
		}
		CaseCategory category = theCase.getCaseCategory();
		Group handlerGroup = category.getHandlerGroup();
		Collection handlers = getUserBusiness().getUsersInGroup(handlerGroup);

		DropdownMenu users = new DropdownMenu(PARAMETER_USER);

		Iterator iter = handlers.iterator();
		while (iter.hasNext()) {
			User handler = (User) iter.next();
			users.addMenuElement(handler.getPrimaryKey().toString(), handler.getName());
		}

		TextArea message = new TextArea(PARAMETER_MESSAGE);
		message.setStyleClass("textarea");
		message.keepStatusOnAction(true);

		Layer element = new Layer(Layer.DIV);
		element.setStyleClass("formItem");
		Label label = new Label(getResourceBundle().getLocalizedString("handler", "Handler"), users);
		element.add(label);
		element.add(users);
		section.add(element);

		element = new Layer(Layer.DIV);
		element.setStyleClass("formItem");
		label = new Label(getResourceBundle().getLocalizedString("allocation_message", "Message"), message);
		element.add(label);
		element.add(message);
		section.add(element);

		section.add(clear);

		Layer bottom = new Layer(Layer.DIV);
		bottom.setStyleClass("bottom");
		form.add(bottom);

		Link back = getButtonLink(getResourceBundle().getLocalizedString("back", "Back"));
		back.setStyleClass("homeButton");
		back.setValueOnClick(PARAMETER_ACTION, String.valueOf(ACTION_PROCESS));
		back.setToFormSubmit(form);
		bottom.add(back);

		Link next = getButtonLink(getResourceBundle().getLocalizedString("allocate", "Allocate"));
		next.setValueOnClick(PARAMETER_ACTION, String.valueOf(ACTION_SAVE));
		next.setToFormSubmit(form);
		bottom.add(next);

		add(form);
	}

	private void multiProcess(IWContext iwc) {
		String[] cases = iwc.getParameterValues(PARAMETER_CASE_PK);
		if (cases != null) {
			for (int i = 0; i < cases.length; i++) {
				try {
					GeneralCase theCase = getCasesBusiness(iwc).getGeneralCase(new Integer(cases[i]));
					CaseCategory category = theCase.getCaseCategory();
					CaseType type = theCase.getCaseType();
					String status = iwc.getParameter(PARAMETER_STATUS);
					String reply = iwc.getParameter(PARAMETER_REPLY);

					getCasesBusiness(iwc).handleCase(theCase, category, type, status, iwc.getCurrentUser(), reply, iwc);
				}
				catch (RemoteException e) {
					throw new IBORuntimeException(e);
				}
				catch (FinderException e) {
					e.printStackTrace();
				}
			}
		}
	}

	protected Link getProcessLink(PresentationObject object, GeneralCase theCase) {
		Link process = new Link("edit");
		
		if(theCase.getJbpmProcessInstanceId() == null || true) {
			process.addParameter(PARAMETER_CASE_PK, theCase.getPrimaryKey().toString());
			process.addParameter(PARAMETER_ACTION, ACTION_PROCESS);
			
		} else {
			
			if(getJbpmProcessViewerPage() == null)
				return process;

			process.setPage(getJbpmProcessViewerPage());
			process.addParameter("processInstanceId", String.valueOf(theCase.getJbpmProcessInstanceId()));
		}

		return process;
	}

	protected abstract Collection getCases(User user) throws RemoteException;

	protected abstract void showProcessor(IWContext iwc, Object casePK) throws RemoteException;

	protected abstract void save(IWContext iwc) throws RemoteException;

	protected abstract boolean showCheckBox();

	public ICPage getJbpmProcessViewerPage() {
		return jbpmProcessViewerPage;
	}

	public void setJbpmProcessViewerPage(ICPage jbpmProcessViewerPage) {
		this.jbpmProcessViewerPage = jbpmProcessViewerPage;
	}
}