/*
 * $Id$
 * Created on Oct 30, 2005
 *
 * Copyright (C) 2005 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf.
 * Use is subject to license terms.
 */
package is.idega.idegaweb.egov.cases.presentation;

import is.idega.idegaweb.egov.cases.data.CaseType;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Iterator;

import javax.ejb.CreateException;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;

import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.Table2;
import com.idega.presentation.TableCell2;
import com.idega.presentation.TableColumn;
import com.idega.presentation.TableColumnGroup;
import com.idega.presentation.TableRow;
import com.idega.presentation.TableRowGroup;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.HiddenInput;
import com.idega.presentation.ui.Label;
import com.idega.presentation.ui.SubmitButton;
import com.idega.presentation.ui.TextArea;
import com.idega.presentation.ui.TextInput;


public class CaseTypeEditor extends CasesBlock {

	private static final String PARAMETER_ACTION = "cte_prm_action";
	
	private static final String PARAMETER_CASE_TYPE_PK = "prm_case_type_pk";
	private static final String PARAMETER_NAME = "prm_name";
	private static final String PARAMETER_DESCRIPTION = "prm_description";
	private static final String PARAMETER_ORDER = "prm_order";

	private static final int ACTION_VIEW = 1;
	private static final int ACTION_EDIT = 2;
	private static final int ACTION_NEW = 3;
	private static final int ACTION_SAVE = 4;
	private static final int ACTION_DELETE = 5;
	
	protected void present(IWContext iwc) throws Exception {
		switch (parseAction(iwc)) {
			case ACTION_VIEW:
				showList(iwc);
				break;

			case ACTION_EDIT:
				showEditor(iwc, iwc.getParameter(PARAMETER_CASE_TYPE_PK));
				break;

			case ACTION_NEW:
				showEditor(iwc, null);
				break;

			case ACTION_SAVE:
				if (saveType(iwc)) {
					showList(iwc);
				}
				else {
					showEditor(iwc, iwc.getParameter(PARAMETER_CASE_TYPE_PK));
				}
				break;

			case ACTION_DELETE:
				removeType(iwc);
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
		Form form = new Form();
		form.setStyleClass("adminForm");
		
		Table2 table = new Table2();
		table.setWidth("100%");
		table.setCellpadding(0);
		table.setCellspacing(0);
		table.setStyleClass("adminTable");
		table.setStyleClass("ruler");
		
		TableColumnGroup columnGroup = table.createColumnGroup();
		TableColumn column = columnGroup.createColumn();
		column.setSpan(2);
		column = columnGroup.createColumn();
		column.setSpan(2);

		Collection types = getBusiness().getCaseTypes();

		TableRowGroup group = table.createHeaderRowGroup();
		TableRow row = group.createRow();
		TableCell2 cell = row.createHeaderCell();
		cell.setStyleClass("firstColumn");
		cell.add(new Text(getResourceBundle().getLocalizedString("name", "Name")));
		
		row.createHeaderCell().add(new Text(getResourceBundle().getLocalizedString("description", "Description")));
		row.createHeaderCell().add(new Text(getResourceBundle().getLocalizedString("order", "Order")));

		row.createHeaderCell().add(Text.getNonBrakingSpace());
		cell = row.createHeaderCell();
		cell.setStyleClass("lastColumn");
		cell.add(Text.getNonBrakingSpace());
		
		group = table.createBodyRowGroup();
		int iRow = 1;
		
		Iterator iter = types.iterator();
		while (iter.hasNext()) {
			CaseType type = (CaseType) iter.next();
			row = group.createRow();
			if (iRow == 1) {
				row.setStyleClass("firstRow");
			}
			else if (!iter.hasNext()) {
				row.setStyleClass("lastRow");
			}
			
			Link edit = new Link(getBundle().getImage("edit.png", getResourceBundle().getLocalizedString("edit", "Edit")));
			edit.addParameter(PARAMETER_CASE_TYPE_PK, type.getPrimaryKey().toString());
			edit.addParameter(PARAMETER_ACTION, ACTION_EDIT);
			
			Link delete = new Link(getBundle().getImage("delete.png", getResourceBundle().getLocalizedString("delete", "Delete")));
			delete.addParameter(PARAMETER_CASE_TYPE_PK, type.getPrimaryKey().toString());
			delete.addParameter(PARAMETER_ACTION, ACTION_DELETE);

			cell = row.createCell();
			cell.setStyleClass("firstColumn");
			cell.add(new Text(type.getName()));

			row.createCell().add(new Text(type.getDescription() != null ? type.getDescription() : "-"));
			row.createCell().add(new Text(type.getOrder() != -1 ? String.valueOf(type.getOrder()) : "-"));

			row.createCell().add(edit);
			cell = row.createCell();
			cell.setStyleClass("lastColumn");
			cell.add(delete);
			
			if (iRow % 2 == 0) {
				row.setStyleClass("evenRow");
			}
			else {
				row.setStyleClass("oddRow");
			}
		}

		form.add(table);
		
		Layer layer = new Layer();
		layer.setStyleClass("buttonLayer");
		form.add(layer);

		SubmitButton newButton = new SubmitButton(getResourceBundle().getLocalizedString("new_type", "New type"), PARAMETER_ACTION, String.valueOf(ACTION_NEW));
		newButton.setStyleClass("button");
		layer.add(newButton);

		add(form);
	}
	
	private void showEditor(IWContext iwc, Object caseTypePK) throws RemoteException {
		Form form = new Form();
		form.setStyleClass("adminForm");
		
		TextInput name = new TextInput(PARAMETER_NAME);
		name.keepStatusOnAction(true);

		TextArea description = new TextArea(PARAMETER_DESCRIPTION);
		description.setStyleClass("textarea");
		description.keepStatusOnAction(true);
		
		TextInput order = new TextInput(PARAMETER_ORDER);
		order.keepStatusOnAction(true);

		if (caseTypePK != null) {
			try {
				CaseType type = getBusiness().getCaseType(caseTypePK);
				
				name.setContent(type.getName());
				if (type.getDescription() != null) {
					description.setContent(type.getDescription());
				}
				order.setContent(type.getOrder() != -1 ? String.valueOf(type.getOrder()) : "");
				
				form.add(new HiddenInput(PARAMETER_CASE_TYPE_PK, type.getPrimaryKey().toString()));
			}
			catch (FinderException fe) {
				fe.printStackTrace();
			}
		}
		
		Layer section = new Layer(Layer.DIV);
		section.setStyleClass("formSection");
		form.add(section);
		
		Layer layer = new Layer(Layer.DIV);
		layer.setStyleClass("formItem");
		Label label = new Label(getResourceBundle().getLocalizedString("name", "Name"), name);
		layer.add(label);
		layer.add(name);
		section.add(layer);

		layer = new Layer(Layer.DIV);
		layer.setStyleClass("formItem");
		label = new Label(getResourceBundle().getLocalizedString("description", "Description"), description);
		layer.add(label);
		layer.add(description);
		section.add(layer);

		layer = new Layer(Layer.DIV);
		layer.setStyleClass("formItem");
		label = new Label(getResourceBundle().getLocalizedString("order", "Order"), order);
		layer.add(label);
		layer.add(order);
		section.add(layer);

		Layer clear = new Layer(Layer.DIV);
		clear.setStyleClass("Clear");
		section.add(clear);

		layer = new Layer(Layer.DIV);
		layer.setStyleClass("buttonLayer");
		form.add(layer);
		
		SubmitButton cancel = new SubmitButton(getResourceBundle().getLocalizedString("cancel", "Cancel"), PARAMETER_ACTION, String.valueOf(ACTION_VIEW));
		layer.add(cancel);

		SubmitButton save = new SubmitButton(getResourceBundle().getLocalizedString("save", "Save"), PARAMETER_ACTION, String.valueOf(ACTION_SAVE));
		layer.add(save);

		add(form);
	}

	private boolean saveType(IWContext iwc) throws RemoteException {
		Object caseTypePK = iwc.getParameter(PARAMETER_CASE_TYPE_PK);
		String name = iwc.getParameter(PARAMETER_NAME);
		String description = iwc.getParameter(PARAMETER_DESCRIPTION);
		int order = iwc.isParameterSet(PARAMETER_ORDER) ? Integer.parseInt(iwc.getParameter(PARAMETER_ORDER)) : -1;
		
		if (name == null || name.length() == 0) {
			getParentPage().setAlertOnLoad(getResourceBundle().getLocalizedString("case_type.name_not_empty", "You must provide a name for the case type."));
			return false;
		}
		
		try {
			getBusiness().storeCaseType(caseTypePK, name, description, order);
			return true;
		}
		catch (FinderException e) {
			e.printStackTrace();
		}
		catch (CreateException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	private void removeType(IWContext iwc) throws RemoteException {
		String caseTypePK = iwc.getParameter(PARAMETER_CASE_TYPE_PK);
		try {
			getBusiness().removeCaseType(caseTypePK);
		}
		catch (RemoveException re) {
			getParentPage().setAlertOnLoad(getResourceBundle().getLocalizedString("remove_case_type_failed", "You can't remove a case type that already has cases connected to it."));
		}
		catch (FinderException fe) {
			fe.printStackTrace();
		}
	}
}