/*
 * $Id$
 * Created on Nov 7, 2005
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import javax.ejb.FinderException;
import com.idega.block.process.data.CaseStatus;
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
import com.idega.presentation.ui.TextInput;
import com.idega.user.data.User;
import com.idega.util.IWTimestamp;
import com.idega.util.text.Name;


public class CaseFinder extends CasesBlock {

	private static final String PARAMETER_ACTION = "cf_prm_action";
	private static final String PARAMETER_CASE_NUMBER = "cf_prm_case_number";
	private static final String PARAMETER_NAME = "cf_prm_name";
	private static final String PARAMETER_PERSONAL_ID = "cf_prm_personal_id";

	private static final int ACTION_SEARCH = 1;
	private static final int ACTION_RESULTS = 2;
	private static final int ACTION_HANDLE = 3;

	protected void present(IWContext iwc) throws Exception {
		switch (parseAction(iwc)) {
			case ACTION_SEARCH:
				showSearchForm(iwc);
				break;

			case ACTION_RESULTS:
				showResults(iwc);
				break;

			case ACTION_HANDLE:
				showProcessor(iwc);
				break;
		}
	}
	
	private int parseAction(IWContext iwc) {
		if (iwc.isParameterSet(PARAMETER_ACTION)) {
			return Integer.parseInt(iwc.getParameter(PARAMETER_ACTION));
		}
		return ACTION_SEARCH;
	}


	private void showSearchForm(IWContext iwc) {
		Form form = new Form();
		form.setStyleClass("casesForm");
		
		Layer layer = new Layer(Layer.DIV);
		layer.setStyleClass("infoLayer");
		form.add(layer);
		
		Heading1 heading = new Heading1(getResourceBundle().getLocalizedString("case_finder.information_heading", "Information"));
		layer.add(heading);
		
		Paragraph paragraph = new Paragraph();
		paragraph.add(new Text(getResourceBundle().getLocalizedString("case_finder.information_text", "Information text here...")));
		layer.add(paragraph);
		
		TextInput caseNumber = new TextInput(PARAMETER_CASE_NUMBER);
		caseNumber.setStyleClass("textinput");
		TextInput name = new TextInput(PARAMETER_NAME);
		name.setStyleClass("textinput");
		TextInput personalID = new TextInput(PARAMETER_PERSONAL_ID);
		personalID.setStyleClass("textinput");
		
		caseNumber.setToDisableOnWhenNotEmpty(name);
		caseNumber.setToDisableOnWhenNotEmpty(personalID);
		name.setToDisableOnWhenNotEmpty(caseNumber);
		name.setToDisableOnWhenNotEmpty(personalID);
		personalID.setToDisableOnWhenNotEmpty(caseNumber);
		personalID.setToDisableOnWhenNotEmpty(name);
		
		Layer element = new Layer(Layer.DIV);
		element.setStyleClass("formElement");
		Label label = new Label(getResourceBundle().getLocalizedString("case_number", "Case number"), caseNumber);
		element.add(label);
		element.add(caseNumber);
		layer.add(element);

		element = new Layer(Layer.DIV);
		element.setStyleClass("formElement");
		label = new Label(getResourceBundle().getLocalizedString("name", "Name"), name);
		element.add(label);
		element.add(name);
		layer.add(element);

		element = new Layer(Layer.DIV);
		element.setStyleClass("formElement");
		label = new Label(getResourceBundle().getLocalizedString("personal_id", "Personal ID"), personalID);
		element.add(label);
		element.add(personalID);
		layer.add(element);

		layer = new Layer(Layer.DIV);
		layer.setStyleClass("buttonLayer");
		form.add(layer);
		
		SubmitButton next = (SubmitButton) new SubmitButton(getResourceBundle().getLocalizedString("next", "Next"), PARAMETER_ACTION, String.valueOf(ACTION_RESULTS));
		next.setStyleClass("button");
		layer.add(next);

		add(form);
	}
	
	private void showResults(IWContext iwc) throws RemoteException {
		Form form = new Form();
		form.setStyleClass("casesForm");
		
		Collection cases = new ArrayList();
		if (iwc.isParameterSet(PARAMETER_CASE_NUMBER)) {
			try {
				GeneralCase theCase = getBusiness().getGeneralCase(iwc.getParameter(PARAMETER_CASE_NUMBER));
				cases.add(theCase);
			}
			catch (FinderException fe) {
				//Nothing found
			}
		}
		else if (iwc.isParameterSet(PARAMETER_NAME)) {
			try {
				Collection users = getUserBusiness().getUserHome().findUsersBySearchCondition(iwc.getParameter(PARAMETER_NAME), false);
				cases.add(getBusiness().getCasesByUsers(users));
			}
			catch (FinderException fe) {
				fe.printStackTrace();
			}
		}
		else if (iwc.isParameterSet(PARAMETER_PERSONAL_ID)) {
			try {
				Collection users = getUserBusiness().getUserHome().findUsersBySearchCondition(iwc.getParameter(PARAMETER_PERSONAL_ID), false);
				cases.add(getBusiness().getCasesByUsers(users));
			}
			catch (FinderException fe) {
				fe.printStackTrace();
			}
		}
		
		if (cases.isEmpty()) {
			Heading1 heading = new Heading1(getResourceBundle().getLocalizedString("search_results.nothing_found", "Nothing found"));
			heading.setStyleClass("errorHeading");
			form.add(heading);
		}
		else {
			Heading1 heading = new Heading1(getResourceBundle().getLocalizedString("search_results.results", "Results"));
			heading.setStyleClass("resultHeading");
			form.add(heading);

			Table2 table = new Table2();
			table.setWidth("100%");
			table.setCellpadding(0);
			table.setCellspacing(0);
			table.setStyleClass("casesTable");
			
			TableColumnGroup columnGroup = table.createColumnGroup();
			TableColumn column = columnGroup.createColumn();
			column.setSpan(6);
			column = columnGroup.createColumn();
			column.setSpan(1);
			column.setWidth("12");

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
				process.addParameter(PARAMETER_CASE_NUMBER, theCase.getPrimaryKey().toString());
				process.addParameter(PARAMETER_ACTION, ACTION_HANDLE);
				
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
	
			form.add(table);
		}
		
		Layer layer = new Layer(Layer.DIV);
		layer.setStyleClass("buttonLayer");
		form.add(layer);
		
		SubmitButton back = (SubmitButton) new SubmitButton(getResourceBundle().getLocalizedString("back", "Back"), PARAMETER_ACTION, String.valueOf(ACTION_SEARCH));
		back.setStyleClass("button");
		layer.add(back);
		
		add(form);
	}
	
	private void showProcessor(IWContext iwc) {
		
	}
}