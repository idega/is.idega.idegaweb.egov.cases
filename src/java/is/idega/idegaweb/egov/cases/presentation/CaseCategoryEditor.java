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
import java.util.Locale;
import java.util.Map;

import javax.ejb.CreateException;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;

import com.idega.block.text.business.TextFinder;
import com.idega.core.localisation.business.ICLocaleBusiness;
import com.idega.core.localisation.presentation.ICLocalePresentation;
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
import com.idega.presentation.ui.SelectOption;
import com.idega.presentation.ui.SubmitButton;
import com.idega.presentation.ui.TextArea;
import com.idega.presentation.ui.TextInput;
import com.idega.user.data.Group;
import com.idega.user.presentation.GroupChooser;
import com.idega.util.PresentationUtil;


public class CaseCategoryEditor extends CasesBlock {

	private static final String PARAMETER_ACTION = "cce_prm_action";
	
	private static final String PARAMETER_CASE_CATEGORY_PK = "prm_case_category_pk";
	private static final String PARAMETER_PARENT_CASE_CATEGORY_PK = "prm_parent_case_category_pk";
	private static final String PARAMETER_LOCALE_ID = "prm_localeid";
	private static final String PARAMETER_NAME = "prm_name";
	private static final String PARAMETER_DESCRIPTION = "prm_description";
	private static final String PARAMETER_GROUP = "prm_group";
	private static final String PARAMETER_ORDER = "prm_order";

	private static final int ACTION_VIEW = 1;
	private static final int ACTION_EDIT = 2;
	private static final int ACTION_NEW = 3;
	private static final int ACTION_SAVE = 4;
	private static final int ACTION_DELETE = 5;

	@Override
	protected void present(IWContext iwc) throws Exception {
			
		switch (parseAction(iwc)) {
			case ACTION_VIEW:
				iwc.removeSessionAttribute(PARAMETER_CASE_CATEGORY_PK);
				showList(iwc);
				break;
			case ACTION_NEW:
				iwc.removeSessionAttribute(PARAMETER_CASE_CATEGORY_PK);
				showEditor(iwc, null);
				break;
			case ACTION_SAVE:
				if (!saveCategory(iwc)) {
					add(new Text(this.getResourceBundle(iwc).getLocalizedString("failed_to_save","FAILED TO SAVE THE CATEGORY!")));
				}
				Object pk = getCaseCategoryPrimaryKey(iwc);				
				showEditor(iwc,pk);
				
				break;
			case ACTION_EDIT:
				pk = getCaseCategoryPrimaryKey(iwc);				
				showEditor(iwc, pk);
				break;
			case ACTION_DELETE:
				iwc.removeSessionAttribute(PARAMETER_CASE_CATEGORY_PK);
				removeCategory(iwc);
				showList(iwc);
				break;
		}
	}

	/**
	 * @param iwc
	 * @return
	 */
	private Object getCaseCategoryPrimaryKey(IWContext iwc) {
		Object pk = iwc.getParameter(PARAMETER_CASE_CATEGORY_PK);
		if(pk!=null){
			iwc.setSessionAttribute(PARAMETER_CASE_CATEGORY_PK, pk);
		}
		else{
			pk = iwc.getSessionAttribute(PARAMETER_CASE_CATEGORY_PK);
		}
		return pk;
	}

	private int parseAction(IWContext iwc) {
		int actionInt = ACTION_VIEW;
		
		if (iwc.isParameterSet(PARAMETER_ACTION)) {
			actionInt = Integer.parseInt(iwc.getParameter(PARAMETER_ACTION));
					
			if(actionInt==ACTION_EDIT || actionInt==ACTION_SAVE){
				iwc.setSessionAttribute(PARAMETER_ACTION,new Integer(ACTION_EDIT));
			}
			else{
				iwc.removeSessionAttribute(PARAMETER_ACTION);
			}
		}
		else{
			Integer action = (Integer) iwc.getSessionAttribute(PARAMETER_ACTION);
			if(action!=null){
				actionInt = action.intValue();
			}
		}
		
	
		
		return actionInt;
	}

	private void showList(IWContext iwc) throws RemoteException {
		Locale locale = iwc.getCurrentLocale();
		
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

		Collection categories = getCasesBusiness().getCaseCategories();

		TableRowGroup group = table.createHeaderRowGroup();
		TableRow row = group.createRow();
		TableCell2 cell = row.createHeaderCell();
		cell.setStyleClass("firstColumn");
		cell.setStyleClass("name");
		cell.add(new Text(getResourceBundle().getLocalizedString("name", "Name")));
		
		cell = row.createHeaderCell();
		cell.setStyleClass("description");
		cell.add(new Text(getResourceBundle().getLocalizedString("description", "Description")));
		
		cell = row.createHeaderCell();
		cell.setStyleClass("handlerGroup");
		cell.add(new Text(getResourceBundle().getLocalizedString("handler_group", "Handler group")));
		
		cell = row.createHeaderCell();
		cell.setStyleClass("order");
		cell.add(new Text(getResourceBundle().getLocalizedString("order", "Order")));

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
			addCategoryToTable(group, category, iRow++, !iter.hasNext(), false, locale);
			
			if (getCasesBusiness().useSubCategories()) {
				Collection subCategories = getCasesBusiness().getSubCategories(category);
				Iterator iterator = subCategories.iterator();
				
				int iSubRow = 1;
				while (iterator.hasNext()) {
					category = (CaseCategory) iterator.next();
					addCategoryToTable(group, category, iSubRow++, !iterator.hasNext(), true, locale);
				}
			}
		}

		form.add(table);
		
		Layer layer = new Layer();
		layer.setStyleClass("buttonLayer");
		layer.setStyleClass("newButtonLayer");
		form.add(layer);

		SubmitButton newButton = new SubmitButton(getResourceBundle().getLocalizedString("new_category", "New category"), PARAMETER_ACTION, String.valueOf(ACTION_NEW));
		newButton.setStyleClass("button");
		layer.add(newButton);

		add(form);
	}
	
	private void addCategoryToTable(TableRowGroup group, CaseCategory category, int iRow, boolean lastEntry, boolean isSubCategory,Locale locale) {
		TableRow row = group.createRow();
		if (iRow == 1) {
			row.setStyleClass("firstRow");
		}
		else if (lastEntry) {
			row.setStyleClass("lastRow");
		}
		if (isSubCategory) {
			row.setStyleClass("subCategory");
		}
		
		Link edit = new Link(getBundle().getImage("edit.png", getResourceBundle().getLocalizedString("edit_category", "Edit")));
		edit.addParameter(PARAMETER_CASE_CATEGORY_PK, category.getPrimaryKey().toString());
		edit.addParameter(PARAMETER_ACTION, ACTION_EDIT);
		
		Link delete = new Link(getBundle().getImage("delete.png", getResourceBundle().getLocalizedString("delete_category", "Delete")));
		delete.addParameter(PARAMETER_CASE_CATEGORY_PK, category.getPrimaryKey().toString());
		delete.setClickConfirmation(getResourceBundle().getLocalizedString("delete_confirmation", "Are you sure you want to delete this case category?"));
		delete.addParameter(PARAMETER_ACTION, ACTION_DELETE);

		TableCell2 cell = row.createCell();
		cell.setStyleClass("firstColumn");
		cell.setStyleClass("name");
		cell.add(new Text(category.getLocalizedCategoryName(locale)));

		cell = row.createCell();
		cell.setStyleClass("description");
		String description = category.getLocalizedCategoryDescription(locale);
		cell.add(new Text( description != null ? description : "-"));
		
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
	
	private void showEditor(IWContext iwc, Object caseCategoryPK) throws RemoteException {
		Form form = new Form();
		form.setStyleClass("adminForm");
		
		Layer section = new Layer(Layer.DIV);
		section.setStyleClass("formSection");
		form.add(section);
		
		Layer helpLayer = new Layer(Layer.DIV);
		helpLayer.setStyleClass("helperText");
		helpLayer.add(new Text(getResourceBundle().getLocalizedString("category_editor.help", "Help for creating/editing categories.")));
		section.add(helpLayer);
		
		Locale locale = iwc.getCurrentLocale();
		String localeId = iwc.getParameter(PARAMETER_LOCALE_ID);
		DropdownMenu localeDrop = ICLocalePresentation.getLocaleDropdownIdKeyed(PARAMETER_LOCALE_ID);
		localeDrop.setToSubmit();
		if(localeId==null){
			localeId = Integer.toString(TextFinder.getLocaleId(locale));	
		}
		else{
			locale = ICLocaleBusiness.getLocale(Integer.parseInt(localeId));
		}
		
		localeDrop.setSelectedElement(localeId);
		
		TextInput name = new TextInput(PARAMETER_NAME);

		TextArea description = new TextArea(PARAMETER_DESCRIPTION);
		description.setStyleClass("textarea");

		
		GroupChooser chooser = new GroupChooser(PARAMETER_GROUP);
		
		DropdownMenu parentCategory = getCaseCategoriesDropdownMenu(getCasesBusiness(iwc).getCaseCategories(),PARAMETER_PARENT_CASE_CATEGORY_PK,locale);			
		parentCategory.addMenuElementFirst("", "");
		
		TextInput order = new TextInput(PARAMETER_ORDER);

		if (caseCategoryPK != null) {
			try {
				CaseCategory category = getCasesBusiness().getCaseCategory(caseCategoryPK);
				CaseCategory parent = category.getParent();
				Group group = category.getHandlerGroup();
				
				name.setContent(category.getLocalizedCategoryName(locale));
				String descriptionText = category.getLocalizedCategoryDescription(locale);
				if (descriptionText != null) {
					description.setContent(descriptionText);
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
		Label label = new Label(getResourceBundle().getLocalizedString("language", "Language"), name);
		layer.add(label);
		layer.add(localeDrop);
		section.add(layer);
		
		layer = new Layer(Layer.DIV);
		layer.setStyleClass("formItem");
		label = new Label(getResourceBundle().getLocalizedString("name", "Name"), name);
		layer.add(label);
		layer.add(name);
		section.add(layer);

		layer = new Layer(Layer.DIV);
		layer.setStyleClass("formItem");
		label = new Label(getResourceBundle().getLocalizedString("description", "Description"), description);
		layer.add(label);
		layer.add(description);
		section.add(layer);

		if (getCasesBusiness(iwc).useSubCategories()) {
			layer = new Layer(Layer.DIV);
			layer.setStyleClass("formItem");
			label = new Label(getResourceBundle().getLocalizedString("parent_category", "Parent category"), parentCategory);
			layer.add(label);
			layer.add(parentCategory);
			section.add(layer);
		}
		
		layer = new Layer(Layer.DIV);
		layer.setStyleClass("formItem");
		label = new Label();
		label.setLabel(getResourceBundle().getLocalizedString("handler_group", "Handler group"));
		layer.add(label);
		layer.add(chooser);
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
		
		SubmitButton save = new SubmitButton(getResourceBundle().getLocalizedString("save", "Save"), PARAMETER_ACTION, String.valueOf(ACTION_SAVE));
		save.setStyleClass("button");
		SubmitButton cancel = new SubmitButton(getResourceBundle().getLocalizedString("back_to_list", "Back to list"), PARAMETER_ACTION, String.valueOf(ACTION_VIEW));
		cancel.setStyleClass("button");
		layer.add(cancel);
		layer.add(save);	

		add(form);
	}

	
	protected DropdownMenu getCaseCategoriesDropdownMenu(Collection caseCategories, String key, Locale locale) {
		DropdownMenu menu = new DropdownMenu(key);
		if (caseCategories != null) {
			Iterator iter = caseCategories.iterator();
			while (iter.hasNext()) {
				CaseCategory category = (CaseCategory) iter.next();
				SelectOption option = new SelectOption(category.getPrimaryKey().toString());
				String value = category.getLocalizedCategoryName(locale);
				option.setName(value + " ("+category.getName()+")");
				menu.addOption(option);
			}
		}
		
		return menu;
	}

	private boolean saveCategory(IWContext iwc) throws RemoteException {
		Object caseCategoryPK = iwc.getParameter(PARAMETER_CASE_CATEGORY_PK);
		Object parentCaseCategoryPK = iwc.isParameterSet(PARAMETER_PARENT_CASE_CATEGORY_PK) ? iwc.getParameter(PARAMETER_PARENT_CASE_CATEGORY_PK) : null;
		String name = iwc.getParameter(PARAMETER_NAME);
		String description = iwc.getParameter(PARAMETER_DESCRIPTION);
		String groupPK = iwc.getParameter(PARAMETER_GROUP);
		int localeId = Integer.parseInt(iwc.getParameter(PARAMETER_LOCALE_ID));
		
		
		int order = iwc.isParameterSet(PARAMETER_ORDER) ? Integer.parseInt(iwc.getParameter(PARAMETER_ORDER)) : -1;
		
		if (name == null || name.length() == 0) {
			PresentationUtil.addJavascriptAlertOnLoad(iwc, getResourceBundle().getLocalizedString("case_category.name_not_empty", "You must provide a name for the case category."));
			return false;
		}
		if (groupPK == null || groupPK.length() == 0) {
			PresentationUtil.addJavascriptAlertOnLoad(iwc, getResourceBundle().getLocalizedString("case_category.group_not_empty", "You must select a handler group for the case category."));
			return false;
		}
		if (groupPK.indexOf("_") != -1) {
			groupPK = groupPK.substring(groupPK.indexOf("_") + 1);
		}
		
		try {
			CaseCategory category = getCasesBusiness().storeCaseCategory(caseCategoryPK, parentCaseCategoryPK, name, description, groupPK, localeId, order);
			iwc.setSessionAttribute(PARAMETER_CASE_CATEGORY_PK,category.getPrimaryKey());
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
			getCasesBusiness().removeCaseCategory(caseCategoryPK);
		}
		catch (RemoveException re) {
			PresentationUtil.addJavascriptAlertOnLoad(iwc, getResourceBundle().getLocalizedString("remove_case_category_failed", "You can't remove a case category that already has cases connected to it."));
		}
		catch (FinderException fe) {
			fe.printStackTrace();
		}
	}
	
	@Override
	public String getCasesProcessorType() {
		return null;
	}

	@Override
	public Map<Object, Object> getUserCasesPageMap() {
		return null;
	}

	@Override
	public boolean showCheckBox() {
		return false;
	}

	@Override
	public boolean showCheckBoxes() {
		return false;
	}
}