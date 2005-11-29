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

import is.idega.idegaweb.egov.cases.data.CaseCategory;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Iterator;
import javax.ejb.CreateException;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;
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
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.HiddenInput;
import com.idega.presentation.ui.Label;
import com.idega.presentation.ui.SubmitButton;
import com.idega.presentation.ui.TextArea;
import com.idega.presentation.ui.TextInput;
import com.idega.user.data.Group;
import com.idega.user.presentation.GroupChooser;


public class CaseCategoryEditor extends CasesBlock {

	private static final String PARAMETER_ACTION = "cce_prm_action";
	
	private static final String PARAMETER_CASE_CATEGORY_PK = "prm_case_category_pk";
	private static final String PARAMETER_NAME = "prm_name";
	private static final String PARAMETER_DESCRIPTION = "prm_description";
	private static final String PARAMETER_GROUP = "prm_group";

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
				showEditor(iwc, iwc.getParameter(PARAMETER_CASE_CATEGORY_PK));
				break;

			case ACTION_NEW:
				showEditor(iwc, null);
				break;

			case ACTION_SAVE:
				if (saveCategory(iwc)) {
					showList(iwc);
				}
				else {
					showEditor(iwc, iwc.getParameter(PARAMETER_CASE_CATEGORY_PK));
				}
				break;

			case ACTION_DELETE:
				removeCategory(iwc);
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
		form.setStyleClass("casesForm");
		
		Table2 table = new Table2();
		table.setWidth("100%");
		table.setCellpadding(0);
		table.setCellspacing(0);
		table.setStyleClass("casesTable");
		
		TableColumnGroup columnGroup = table.createColumnGroup();
		TableColumn column = columnGroup.createColumn();
		column.setSpan(3);
		column = columnGroup.createColumn();
		column.setSpan(2);
		column.setWidth("12");

		Collection categories = getBusiness().getCaseCategories();

		TableRowGroup group = table.createHeaderRowGroup();
		TableRow row = group.createRow();
		TableCell2 cell = row.createHeaderCell();
		cell.setStyleClass("firstColumn");
		cell.add(new Text(getResourceBundle().getLocalizedString("name", "Name")));
		
		row.createHeaderCell().add(new Text(getResourceBundle().getLocalizedString("description", "Description")));
		row.createHeaderCell().add(new Text(getResourceBundle().getLocalizedString("handler_group", "Handler group")));

		row.createHeaderCell().add(Text.getNonBrakingSpace());
		cell = row.createHeaderCell();
		cell.setStyleClass("lastColumn");
		cell.add(Text.getNonBrakingSpace());
		
		group = table.createBodyRowGroup();
		int iRow = 1;
		
		Iterator iter = categories.iterator();
		Commune commune;
		while (iter.hasNext()) {
			CaseCategory category = (CaseCategory) iter.next();
			row = group.createRow();
			if (iRow == 1) {
				row.setStyleClass("firstRow");
			}
			else if (!iter.hasNext()) {
				row.setStyleClass("lastRow");
			}
			
			Link edit = new Link(getBundle().getImage("edit.gif", getResourceBundle().getLocalizedString("edit", "Edit")));
			edit.addParameter(PARAMETER_CASE_CATEGORY_PK, category.getPrimaryKey().toString());
			edit.addParameter(PARAMETER_ACTION, ACTION_EDIT);
			
			Link delete = new Link(getBundle().getImage("delete.gif", getResourceBundle().getLocalizedString("delete", "Delete")));
			delete.addParameter(PARAMETER_CASE_CATEGORY_PK, category.getPrimaryKey().toString());
			delete.addParameter(PARAMETER_ACTION, ACTION_DELETE);

			cell = row.createCell();
			cell.setStyleClass("firstColumn");
			cell.add(new Text(category.getName()));

			row.createCell().add(new Text(category.getDescription() != null ? category.getDescription() : "-"));
			row.createCell().add(new Text(category.getHandlerGroup().getName()));

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

		SubmitButton newButton = new SubmitButton(getResourceBundle().getLocalizedString("new_category", "New category"), PARAMETER_ACTION, String.valueOf(ACTION_NEW));
		newButton.setStyleClass("button");
		layer.add(newButton);

		add(form);
	}
	
	private void showEditor(IWContext iwc, Object caseCategoryPK) throws RemoteException {
		Form form = new Form();
		form.setStyleClass("casesForm");
		
		Heading1 heading = new Heading1(getResourceBundle().getLocalizedString("new_edit_case_category", "New/Edit case category"));
		form.add(heading);
		
		TextInput name = new TextInput(PARAMETER_NAME);
		name.keepStatusOnAction(true);

		TextArea description = new TextArea(PARAMETER_DESCRIPTION);
		description.setStyleClass("textarea");
		description.keepStatusOnAction(true);
		
		GroupChooser chooser = new GroupChooser(PARAMETER_GROUP);
		
		if (caseCategoryPK != null) {
			try {
				CaseCategory category = getBusiness().getCaseCategory(caseCategoryPK);
				Group group = category.getHandlerGroup();
				
				name.setContent(category.getName());
				if (category.getDescription() != null) {
					description.setContent(category.getDescription());
				}
				chooser.setSelectedGroup(group.getPrimaryKey().toString(), group.getName());
				
				form.add(new HiddenInput(PARAMETER_CASE_CATEGORY_PK, category.getPrimaryKey().toString()));
			}
			catch (FinderException fe) {
				fe.printStackTrace();
			}
		}
		
		Layer layer = new Layer(Layer.DIV);
		layer.setStyleClass("formElement");
		Label label = new Label(getResourceBundle().getLocalizedString("name", "Name"), name);
		layer.add(label);
		layer.add(name);
		form.add(layer);

		layer = new Layer(Layer.DIV);
		layer.setStyleClass("formElement");
		label = new Label(getResourceBundle().getLocalizedString("description", "Description"), description);
		layer.add(label);
		layer.add(description);
		form.add(layer);

		layer = new Layer(Layer.DIV);
		layer.setStyleClass("formElement");
		label = new Label();
		label.setLabel(getResourceBundle().getLocalizedString("handler_group", "Handler group"));
		layer.add(label);
		layer.add(chooser);
		form.add(layer);

		Layer clear = new Layer(Layer.DIV);
		clear.setStyleClass("Clear");
		form.add(clear);

		layer = new Layer(Layer.DIV);
		layer.setStyleClass("buttonLayer");
		form.add(layer);
		
		SubmitButton save = new SubmitButton(getResourceBundle().getLocalizedString("save", "Save"), PARAMETER_ACTION, String.valueOf(ACTION_SAVE));
		save.setStyleClass("button");
		SubmitButton cancel = new SubmitButton(getResourceBundle().getLocalizedString("cancel", "Cancel"), PARAMETER_ACTION, String.valueOf(ACTION_VIEW));
		cancel.setStyleClass("button");
		layer.add(cancel);
		layer.add(save);

		add(form);
	}

	private boolean saveCategory(IWContext iwc) throws RemoteException {
		Object caseCategoryPK = iwc.getParameter(PARAMETER_CASE_CATEGORY_PK);
		String name = iwc.getParameter(PARAMETER_NAME);
		String description = iwc.getParameter(PARAMETER_DESCRIPTION);
		String groupPK = iwc.getParameter(PARAMETER_GROUP);
		
		if (name == null || name.length() == 0) {
			getParentPage().setAlertOnLoad(getResourceBundle().getLocalizedString("case_category.name_not_empty", "You must provide a name for the case category."));
			return false;
		}
		if (groupPK == null || groupPK.length() == 0) {
			getParentPage().setAlertOnLoad(getResourceBundle().getLocalizedString("case_category.group_not_empty", "You must select a handler group for the case category."));
			return false;
		}
		if (groupPK.indexOf("_") != -1) {
			groupPK = groupPK.substring(groupPK.indexOf("_") + 1);
		}
		
		try {
			getBusiness().storeCaseCategory(caseCategoryPK, name, description, groupPK);
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
	
	private void removeCategory(IWContext iwc) throws RemoteException {
		String caseCategoryPK = iwc.getParameter(PARAMETER_CASE_CATEGORY_PK);
		try {
			getBusiness().removeCaseCategory(caseCategoryPK);
		}
		catch (RemoveException re) {
			getParentPage().setAlertOnLoad(getResourceBundle().getLocalizedString("remove_case_category_failed", "You can't remove a case category that already has cases connected to it."));
		}
		catch (FinderException fe) {
			fe.printStackTrace();
		}
	}
}