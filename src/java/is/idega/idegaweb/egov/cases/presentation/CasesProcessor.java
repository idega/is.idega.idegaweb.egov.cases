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

import is.idega.idegaweb.egov.cases.data.CaseType;
import is.idega.idegaweb.egov.cases.data.GeneralCase;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Iterator;
import javax.ejb.FinderException;
import com.idega.block.process.data.CaseStatus;
import com.idega.business.IBORuntimeException;
import com.idega.core.location.data.Commune;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.Table2;
import com.idega.presentation.TableCell2;
import com.idega.presentation.TableColumn;
import com.idega.presentation.TableColumnGroup;
import com.idega.presentation.TableRow;
import com.idega.presentation.TableRowGroup;
import com.idega.presentation.text.Heading1;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Paragraph;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.Label;
import com.idega.presentation.ui.SubmitButton;
import com.idega.presentation.ui.TextArea;
import com.idega.user.data.User;
import com.idega.util.IWTimestamp;
import com.idega.util.text.Name;


public class CasesProcessor extends CasesBlock {

	private static final String PARAMETER_ACTION = "cp_prm_action";
	
	private static final String PARAMETER_CASE_PK = "prm_case_pk";
	private static final String PARAMETER_REPLY = "prm_reply";

	private static final int ACTION_VIEW = 1;
	private static final int ACTION_PROCESS = 2;
	private static final int ACTION_SAVE = 3;
	
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
		}
	}

	private int parseAction(IWContext iwc) {
		if (iwc.isParameterSet(PARAMETER_ACTION)) {
			return Integer.parseInt(iwc.getParameter(PARAMETER_ACTION));
		}
		return ACTION_VIEW;
	}

	private void showList(IWContext iwc) throws RemoteException {
		Table2 table = new Table2();
		table.setWidth("100%");
		table.setCellpadding(0);
		table.setCellspacing(0);
		table.setStyleClass("casesTable");
		
		TableColumnGroup columnGroup = table.createColumnGroup();
		TableColumn column = columnGroup.createColumn();
		column.setSpan(7);
		column = columnGroup.createColumn();
		column.setSpan(1);
		column.setWidth("12");

		Collection groups = getUserBusiness().getUserGroupsDirectlyRelated(iwc.getCurrentUser());
		Collection cases = getBusiness().getCases(groups);

		TableRowGroup group = table.createHeaderRowGroup();
		TableRow row = group.createRow();
		TableCell2 cell = row.createHeaderCell();
		cell.setStyleClass("firstColumn");
		cell.add(new Text(getResourceBundle().getLocalizedString("sender", "Sender")));
		
		row.createHeaderCell().add(new Text(getResourceBundle().getLocalizedString("case_type", "Case type")));
		row.createHeaderCell().add(new Text(getResourceBundle().getLocalizedString("case_number", "Case number")));
		row.createHeaderCell().add(new Text(getResourceBundle().getLocalizedString("created_date", "Created date")));
		row.createHeaderCell().add(new Text(getResourceBundle().getLocalizedString("status", "Status")));
		row.createHeaderCell().add(new Text(getResourceBundle().getLocalizedString("handler", "Handler")));

		row.createHeaderCell().add(Text.getNonBrakingSpace());
		cell = row.createHeaderCell();
		cell.setStyleClass("lastColumn");
		cell.add(Text.getNonBrakingSpace());
		
		group = table.createBodyRowGroup();
		int iRow = 1;
		
		Iterator iter = cases.iterator();
		Commune commune;
		while (iter.hasNext()) {
			GeneralCase theCase = (GeneralCase) iter.next();
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
			
			Link process = new Link(getBundle().getImage("edit.gif", getResourceBundle().getLocalizedString("edit", "Edit")));
			process.addParameter(PARAMETER_CASE_PK, theCase.getPrimaryKey().toString());
			process.addParameter(PARAMETER_ACTION, ACTION_PROCESS);
			
			cell = row.createCell();
			cell.setStyleClass("firstColumn");
			cell.add(new Text(new Name(owner.getFirstName(), owner.getMiddleName(), owner.getLastName()).getName(iwc.getCurrentLocale())));

			row.createCell().add(new Text(type.getName()));
			row.createCell().add(new Text(theCase.getCaseNumber() != null ? theCase.getCaseNumber() : "-"));
			row.createCell().add(new Text(created.getLocaleDateAndTime(iwc.getCurrentLocale(), IWTimestamp.SHORT, IWTimestamp.SHORT)));

			row.createCell().add(new Text(getBusiness().getLocalizedCaseStatusDescription(status, iwc.getCurrentLocale())));
			
			User handler = null;
			if (status.equals(getBusiness().getCaseStatusReady())) {
				handler = getBusiness().getLastModifier(theCase);
				row.createCell().add(new Text(new Name(handler.getFirstName(), handler.getMiddleName(), handler.getLastName()).getName(iwc.getCurrentLocale())));
			}
			if (handler == null) {
				row.createCell().add(new Text("-"));
			}
			
			cell = row.createCell();
			cell.setStyleClass("lastColumn");
			if (status.equals(getBusiness().getCaseStatusOpen())) {
				cell.add(process);
			}
			else {
				cell.add(Text.getNonBrakingSpace());
			}
			
			if (iRow % 2 == 0) {
				row.setStyleClass("evenRow");
			}
			else {
				row.setStyleClass("oddRow");
			}
		}

		add(table);
	}
	
	private void showProcessor(IWContext iwc, Object casePK) throws RemoteException {
		Form form = new Form();
		form.setStyleClass("casesForm");
		form.maintainParameter(PARAMETER_CASE_PK);
		
		GeneralCase theCase = null;
		try {
			theCase = getBusiness().getGeneralCase(casePK);
		}
		catch (FinderException fe) {
			fe.printStackTrace();
			throw new IBORuntimeException(fe);
		}
		CaseType type = theCase.getCaseType();
		User owner = theCase.getOwner();
		IWTimestamp created = new IWTimestamp(theCase.getCreated());
		
		Layer layer = new Layer(Layer.DIV);
		layer.setStyleClass("elementsLayer");
		form.add(layer);
		
		Heading1 heading = new Heading1(getResourceBundle().getLocalizedString("case_processor.handle_case", "Handle case"));
		layer.add(heading);
		
		Paragraph caseType = new Paragraph();
		caseType.add(new Text(type.getName()));
		
		Paragraph sender = new Paragraph();
		sender.add(new Text(new Name(owner.getFirstName(), owner.getMiddleName(), owner.getLastName()).getName(iwc.getCurrentLocale())));
		
		Paragraph message = new Paragraph();
		message.add(new Text(theCase.getMessage()));
		
		Paragraph createdDate = new Paragraph();
		createdDate.add(new Text(created.getLocaleDateAndTime(iwc.getCurrentLocale(), IWTimestamp.SHORT, IWTimestamp.SHORT)));
		
		TextArea reply = new TextArea(PARAMETER_REPLY);
		reply.setStyleClass("textarea");
		reply.keepStatusOnAction(true);
		
		Layer element = new Layer(Layer.DIV);
		layer.setStyleClass("formElement");
		Label label = new Label();
		label.setLabel(getResourceBundle().getLocalizedString("sender", "Sender"));
		element.add(label);
		element.add(sender);
		layer.add(element);

		element = new Layer(Layer.DIV);
		layer.setStyleClass("formElement");
		label = new Label();
		label.setLabel(getResourceBundle().getLocalizedString("case_type", "Case type"));
		element.add(label);
		element.add(caseType);
		layer.add(element);

		element = new Layer(Layer.DIV);
		layer.setStyleClass("formElement");
		label = new Label();
		label.setLabel(getResourceBundle().getLocalizedString("created_date", "Created date"));
		element.add(label);
		element.add(createdDate);
		layer.add(element);

		element = new Layer(Layer.DIV);
		layer.setStyleClass("formElement");
		label = new Label();
		label.setLabel(getResourceBundle().getLocalizedString("message", "Message"));
		element.add(label);
		element.add(message);
		layer.add(element);

		element = new Layer(Layer.DIV);
		element.setStyleClass("formElement");
		label = new Label(getResourceBundle().getLocalizedString("reply", "Reply"), reply);
		element.add(label);
		element.add(reply);
		layer.add(element);

		Layer clear = new Layer(Layer.DIV);
		clear.setStyleClass("Clear");
		layer.add(clear);

		layer = new Layer(Layer.DIV);
		layer.setStyleClass("buttonLayer");
		form.add(layer);
		
		SubmitButton next = (SubmitButton) new SubmitButton(getResourceBundle().getLocalizedString("process", "Process"), PARAMETER_ACTION, String.valueOf(ACTION_SAVE));
		next.setStyleClass("button");
		SubmitButton back = (SubmitButton) new SubmitButton(getResourceBundle().getLocalizedString("back", "Back"), PARAMETER_ACTION, String.valueOf(ACTION_VIEW));
		back.setStyleClass("button");
		layer.add(back);
		layer.add(next);
		
		add(form);
	}
	
	private void save(IWContext iwc) throws RemoteException {
		Object casePK = iwc.getParameter(PARAMETER_CASE_PK);
		String reply = iwc.getParameter(PARAMETER_REPLY);
		
		try {
			getBusiness().handleCase(casePK, iwc.getCurrentUser(), reply);
		}
		catch (FinderException fe) {
			fe.printStackTrace();
		}
	}
}