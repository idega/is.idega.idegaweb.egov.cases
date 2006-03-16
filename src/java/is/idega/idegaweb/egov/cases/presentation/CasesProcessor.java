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

import com.idega.block.process.data.CaseStatus;
import com.idega.presentation.IWContext;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.Table2;
import com.idega.presentation.TableCell2;
import com.idega.presentation.TableColumn;
import com.idega.presentation.TableColumnGroup;
import com.idega.presentation.TableRow;
import com.idega.presentation.TableRowGroup;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.user.data.User;
import com.idega.util.IWTimestamp;
import com.idega.util.text.Name;


public abstract class CasesProcessor extends CasesBlock {

	protected static final String PARAMETER_ACTION = "cp_prm_action";
	
	protected static final String PARAMETER_CASE_PK = "prm_case_pk";

	protected static final int ACTION_VIEW = 1;
	protected static final int ACTION_PROCESS = 2;
	protected static final int ACTION_SAVE = 3;
	
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
		table.setStyleClass("adminTable");
		
		TableColumnGroup columnGroup = table.createColumnGroup();
		TableColumn column = columnGroup.createColumn();
		column.setSpan(6);
		column = columnGroup.createColumn();
		column.setSpan(1);
		column.setWidth("12");

		Collection cases = getCases(iwc.getCurrentUser());

		TableRowGroup group = table.createHeaderRowGroup();
		TableRow row = group.createRow();
		TableCell2 cell = row.createHeaderCell();
		cell.setStyleClass("firstColumn");
		cell.add(new Text(getResourceBundle().getLocalizedString("case_nr", "Case nr.")));
		
		row.createHeaderCell().add(new Text(getResourceBundle().getLocalizedString("sender", "Sender")));
		row.createHeaderCell().add(new Text(getResourceBundle().getLocalizedString("case_type", "Case type")));
		row.createHeaderCell().add(new Text(getResourceBundle().getLocalizedString("created_date", "Created date")));
		row.createHeaderCell().add(new Text(getResourceBundle().getLocalizedString("status", "Status")));
		row.createHeaderCell().add(new Text(getResourceBundle().getLocalizedString("handler", "Handler")));

		cell = row.createHeaderCell();
		cell.setStyleClass("lastColumn");
		cell.add(new Text(getResourceBundle().getLocalizedString("view", "View")));
		
		group = table.createBodyRowGroup();
		int iRow = 1;
		
		Iterator iter = cases.iterator();
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
			
			cell = row.createCell();
			cell.setStyleClass("firstColumn");
			cell.add(new Text(theCase.getPrimaryKey().toString()));

			row.createCell().add(new Text(new Name(owner.getFirstName(), owner.getMiddleName(), owner.getLastName()).getName(iwc.getCurrentLocale())));
			row.createCell().add(new Text(type.getName()));
			row.createCell().add(new Text(created.getLocaleDateAndTime(iwc.getCurrentLocale(), IWTimestamp.SHORT, IWTimestamp.SHORT)));

			row.createCell().add(new Text(getBusiness().getLocalizedCaseStatusDescription(status, iwc.getCurrentLocale())));
			
			User handler = theCase.getHandledBy();
			if (handler != null) {
				row.createCell().add(new Text(new Name(handler.getFirstName(), handler.getMiddleName(), handler.getLastName()).getName(iwc.getCurrentLocale())));
			}
			else {
				row.createCell().add(new Text("-"));
			}
			
			cell = row.createCell();
			cell.setStyleClass("lastColumn");
			cell.add(getProcessLink(getBundle().getImage("edit.png", getResourceBundle().getLocalizedString("view_case", "View case")), theCase));
			
			if (iRow % 2 == 0) {
				row.setStyleClass("evenRow");
			}
			else {
				row.setStyleClass("oddRow");
			}
		}

		add(table);
	}
	
	private Link getProcessLink(PresentationObject object, GeneralCase theCase) {
		Link process = new Link(object);
		process.addParameter(PARAMETER_CASE_PK, theCase.getPrimaryKey().toString());
		process.addParameter(PARAMETER_ACTION, ACTION_PROCESS);
		
		return process;
	}
	
	protected abstract Collection getCases(User user) throws RemoteException;
	protected abstract void showProcessor(IWContext iwc, Object casePK) throws RemoteException;
	protected abstract void save(IWContext iwc) throws RemoteException;
}