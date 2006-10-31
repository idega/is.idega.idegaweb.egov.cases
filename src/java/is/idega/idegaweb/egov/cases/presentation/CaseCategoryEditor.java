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
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.HiddenInput;
import com.idega.presentation.ui.Label;
import com.idega.presentation.ui.SubmitButton;
import com.idega.presentation.ui.TextArea;
import com.idega.presentation.ui.TextInput;
import com.idega.presentation.ui.util.SelectorUtility;
import com.idega.user.data.Group;
import com.idega.user.presentation.GroupChooser;


public class CaseCategoryEditor extends CasesBlock {

	private static final String PARAMETER_ACTION = "cce_prm_action";
	
	private static final String PARAMETER_CASE_CATEGORY_PK = "prm_case_category_pk";
	private static final String PARAMETER_PARENT_CASE_CATEGORY_PK = "prm_parent_case_category_pk";
	private static final String PARAMETER_NAME = "prm_name";
	private static final String PARAMETER_DESCRIPTION = "prm_description";
	private static final String PARAMETER_GROUP = "prm_group";
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
		form.setStyleClass("adminForm");
		form.setID("caseCategoryEditor");
		
		Table2 table = new Table2();
		table.setWidth("100%");
		table.setCellpadding(0);
		table.setCellspacing(0);
		table.setStyleClass("adminTable");
		table.setStyleClass("ruler");
		
		TableColumnGroup columnGroup = table.createColumnGroup();
		TableColumn column = columnGroup.createColumn();
		column.setSpan(3);
		column = columnGroup.createColumn();
		column.setSpan(2);
		column.setWidth("12");

		Collection categories = getBusiness().getAllCaseCategories();

		TableRowGroup group = table.createHeaderRowGroup();
		TableRow row = group.createRow();
		TableCell2 cell = row.createHeaderCell();
		cell.setStyleClass("firstColumn");
		cell.setStyleClass("name");
		cell.add(new Text(getResourceBundle().getLocalizedString(getPrefix() + "name", "Name")));
		
		cell = row.createHeaderCell();
		cell.setStyleClass("description");
		cell.add(new Text(getResourceBundle().getLocalizedString(getPrefix() + "description", "Description")));
		
		if (getCasesBusiness(iwc).useSubCategories()) {
			cell = row.createHeaderCell();
			cell.setStyleClass("parentCategory");
			cell.add(new Text(getResourceBundle().getLocalizedString(getPrefix() + "parent_category", "Parent category")));
		}
		
		cell = row.createHeaderCell();
		cell.setStyleClass("handlerGroup");
		cell.add(new Text(getResourceBundle().getLocalizedString(getPrefix() + "handler_group", "Handler group")));
		
		cell = row.createHeaderCell();
		cell.setStyleClass("order");
		cell.add(new Text(getResourceBundle().getLocalizedString(getPrefix() + "order", "Order")));

		cell = row.createHeaderCell();
		cell.setStyleClass("edit");
		cell.add(new Text(getResourceBundle().getLocalizedString("edit", "Edit")));

		cell = row.createHeaderCell();
		cell.setStyleClass("lastColumn");
		cell.setStyleClass("delete");
		cell.add(new Text(getResourceBundle().getLocalizedString("delete", "Delete")));
		
		group = table.createBodyRowGroup();
		int iRow = 1;
		
		Iterator iter = categories.iterator();
		while (iter.hasNext()) {
			CaseCategory category = (CaseCategory) iter.next();
			row = group.createRow();
			if (iRow == 1) {
				row.setStyleClass("firstRow");
			}
			else if (!iter.hasNext()) {
				row.setStyleClass("lastRow");
			}
			
			CaseCategory parentCategory = category.getParent();
			
			Link edit = new Link(getBundle().getImage("edit.png", getResourceBundle().getLocalizedString(getPrefix() + "edit_category", "Edit")));
			edit.addParameter(PARAMETER_CASE_CATEGORY_PK, category.getPrimaryKey().toString());
			edit.addParameter(PARAMETER_ACTION, ACTION_EDIT);
			
			Link delete = new Link(getBundle().getImage("delete.png", getResourceBundle().getLocalizedString(getPrefix() + "delete_category", "Delete")));
			delete.addParameter(PARAMETER_CASE_CATEGORY_PK, category.getPrimaryKey().toString());
			delete.setClickConfirmation(getResourceBundle().getLocalizedString(getPrefix() + "delete_confirmation", "Are you sure you want to delete this case category?"));
			delete.addParameter(PARAMETER_ACTION, ACTION_DELETE);

			cell = row.createCell();
			cell.setStyleClass("firstColumn");
			cell.setStyleClass("name");
			cell.add(new Text(category.getName()));

			cell = row.createCell();
			cell.setStyleClass("description");
			cell.add(new Text(category.getDescription() != null ? category.getDescription() : "-"));
			
			if (getCasesBusiness(iwc).useSubCategories()) {
				cell = row.createCell();
				cell.setStyleClass("parentCategory");
				cell.add(new Text(parentCategory != null ? parentCategory.getName() : "-"));
			}
			
			cell = row.createCell();
			cell.setStyleClass("handlerGroup");
			cell.add(new Text(category.getHandlerGroup().getName()));
			
			cell = row.createCell();
			cell.setStyleClass("order");
			cell.add(new Text(category.getOrder() != -1 ? String.valueOf(category.getOrder()) : "-"));

			row.createCell().add(edit);
			cell = row.createCell();
			cell.setStyleClass("lastColumn");
			cell.setStyleClass("edit");
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

		SubmitButton newButton = new SubmitButton(getResourceBundle().getLocalizedString(getPrefix() + "new_category", "New category"), PARAMETER_ACTION, String.valueOf(ACTION_NEW));
		newButton.setStyleClass("button");
		layer.add(newButton);

		add(form);
	}
	
	private void showEditor(IWContext iwc, Object caseCategoryPK) throws RemoteException {
		Form form = new Form();
		form.setStyleClass("adminForm");
		
		Layer section = new Layer(Layer.DIV);
		section.setStyleClass("formSection");
		form.add(section);
		
		TextInput name = new TextInput(PARAMETER_NAME);
		name.keepStatusOnAction(true);

		TextArea description = new TextArea(PARAMETER_DESCRIPTION);
		description.setStyleClass("textarea");
		description.keepStatusOnAction(true);
		
		GroupChooser chooser = new GroupChooser(PARAMETER_GROUP);
		
		SelectorUtility util = new SelectorUtility();
		DropdownMenu parentCategory = (DropdownMenu) util.getSelectorFromIDOEntities(new DropdownMenu(PARAMETER_PARENT_CASE_CATEGORY_PK), getCasesBusiness(iwc).getCaseCategories(), "getName");
		parentCategory.addMenuElementFirst("", "");
		
		TextInput order = new TextInput(PARAMETER_ORDER);
		order.keepStatusOnAction(true);

		if (caseCategoryPK != null) {
			try {
				CaseCategory category = getBusiness().getCaseCategory(caseCategoryPK);
				CaseCategory parent = category.getParent();
				Group group = category.getHandlerGroup();
				
				name.setContent(category.getName());
				if (category.getDescription() != null) {
					description.setContent(category.getDescription());
				}
				chooser.setSelectedGroup(group.getPrimaryKey().toString(), group.getName());
				order.setContent(category.getOrder() != -1 ? String.valueOf(category.getOrder()) : "");
				if (parent != null) {
					parentCategory.setSelectedElement(parent.getPrimaryKey().toString());
				}
				
				form.add(new HiddenInput(PARAMETER_CASE_CATEGORY_PK, category.getPrimaryKey().toString()));
			}
			catch (FinderException fe) {
				fe.printStackTrace();
			}
		}
		
		Layer layer = new Layer(Layer.DIV);
		layer.setStyleClass("formItem");
		Label label = new Label(getResourceBundle().getLocalizedString(getPrefix() + "name", "Name"), name);
		layer.add(label);
		layer.add(name);
		section.add(layer);

		layer = new Layer(Layer.DIV);
		layer.setStyleClass("formItem");
		label = new Label(getResourceBundle().getLocalizedString(getPrefix() + "description", "Description"), description);
		layer.add(label);
		layer.add(description);
		section.add(layer);

		if (getCasesBusiness(iwc).useSubCategories()) {
			layer = new Layer(Layer.DIV);
			layer.setStyleClass("formItem");
			label = new Label(getResourceBundle().getLocalizedString(getPrefix() + "parent_category", "Parent category"), parentCategory);
			layer.add(label);
			layer.add(parentCategory);
			section.add(layer);
		}
		
		layer = new Layer(Layer.DIV);
		layer.setStyleClass("formItem");
		label = new Label();
		label.setLabel(getResourceBundle().getLocalizedString(getPrefix() + "handler_group", "Handler group"));
		layer.add(label);
		layer.add(chooser);
		section.add(layer);

		layer = new Layer(Layer.DIV);
		layer.setStyleClass("formItem");
		label = new Label(getResourceBundle().getLocalizedString(getPrefix() + "order", "Order"), order);
		layer.add(label);
		layer.add(order);
		section.add(layer);

		Layer clear = new Layer(Layer.DIV);
		clear.setStyleClass("Clear");
		section.add(clear);

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
		Object parentCaseCategoryPK = iwc.getParameter(PARAMETER_PARENT_CASE_CATEGORY_PK);
		String name = iwc.getParameter(PARAMETER_NAME);
		String description = iwc.getParameter(PARAMETER_DESCRIPTION);
		String groupPK = iwc.getParameter(PARAMETER_GROUP);
		int order = iwc.isParameterSet(PARAMETER_ORDER) ? Integer.parseInt(iwc.getParameter(PARAMETER_ORDER)) : -1;
		
		if (name == null || name.length() == 0) {
			getParentPage().setAlertOnLoad(getResourceBundle().getLocalizedString(getPrefix() + "case_category.name_not_empty", "You must provide a name for the case category."));
			return false;
		}
		if (groupPK == null || groupPK.length() == 0) {
			getParentPage().setAlertOnLoad(getResourceBundle().getLocalizedString(getPrefix() + "case_category.group_not_empty", "You must select a handler group for the case category."));
			return false;
		}
		if (groupPK.indexOf("_") != -1) {
			groupPK = groupPK.substring(groupPK.indexOf("_") + 1);
		}
		
		try {
			getBusiness().storeCaseCategory(caseCategoryPK, parentCaseCategoryPK, name, description, groupPK, order);
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
			getParentPage().setAlertOnLoad(getResourceBundle().getLocalizedString(getPrefix() + "remove_case_category_failed", "You can't remove a case category that already has cases connected to it."));
		}
		catch (FinderException fe) {
			fe.printStackTrace();
		}
	}
}