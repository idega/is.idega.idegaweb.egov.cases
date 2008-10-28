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

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.FinderException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import com.idega.block.process.presentation.UserCases;
import com.idega.block.process.presentation.beans.CaseManagerState;
import com.idega.block.process.presentation.beans.GeneralCaseManagerViewBuilder;
import com.idega.block.process.presentation.beans.GeneralCasesListBuilder;
import com.idega.business.IBORuntimeException;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.HiddenInput;
import com.idega.presentation.ui.Label;
import com.idega.presentation.ui.SubmitButton;
import com.idega.presentation.ui.TextArea;
import com.idega.presentation.ui.util.SelectorUtility;
import com.idega.user.data.Group;
import com.idega.user.data.User;
import com.idega.webface.WFUtil;

public abstract class CasesProcessor extends CasesBlock {
	
	public static final String PARAMETER_ACTION = "cp_prm_action";
	
	public static final String PARAMETER_CASE_PK = UserCases.PARAMETER_CASE_PK;
	protected static final String PARAMETER_CASE_CATEGORY_PK = "prm_case_category_pk";
	protected static final String PARAMETER_SUB_CASE_CATEGORY_PK = "prm_sub_case_category_pk";
	protected static final String PARAMETER_CASE_TYPE_PK = "prm_case_type_pk";
	protected static final String PARAMETER_REPLY = "prm_reply";
	protected static final String PARAMETER_STATUS = "prm_status";
	protected static final String PARAMETER_USER = "prm_user";
	protected static final String PARAMETER_MESSAGE = "prm_message";

	protected static final int ACTION_VIEW = UserCases.ACTION_VIEW;
	public static final int ACTION_PROCESS = 2;
	protected static final int ACTION_SAVE = 3;
	protected static final int ACTION_MULTI_PROCESS_FORM = 4;
	protected static final int ACTION_MULTI_PROCESS = 5;
	protected static final int ACTION_ALLOCATION_FORM = 6;
	
	private static final String caseManagerFacet = "caseManager";

	protected abstract String getBlockID();
	
	@Override
	protected void present(IWContext iwc) throws Exception {
	}
	
	protected void display(IWContext iwc) throws Exception {

		CaseManagerState caseHandlerState = (CaseManagerState)WFUtil.getBeanInstance(CaseManagerState.beanIdentifier);
		
		if(!caseHandlerState.getShowCaseHandler()) {
			
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
					
				case UserCases.ACTION_CASE_MANAGER_VIEW:
					showCaseManagerView(iwc);
					break;
				
				default:
					showList(iwc);
			}
		}
	}
	
	private void showCaseManagerView(IWContext iwc) {

		UIComponent view = null;

		GeneralCaseManagerViewBuilder viewBuilder = (GeneralCaseManagerViewBuilder) WFUtil.getBeanInstance(GeneralCaseManagerViewBuilder.SPRING_BEAN_IDENTIFIER);
		try {
			view = viewBuilder.getCaseManagerView(iwc, getCasesProcessorType());
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		if (view == null) {
			return;
		}
		
		add(view);
	}
	
	@Override
	public void encodeBegin(FacesContext fc) throws IOException {
		super.encodeBegin(fc);
		
		try {
			display(IWContext.getIWContext(fc));
		
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			Logger.getLogger(getClassName()).log(Level.SEVERE, "Exception while displaying CasesProcessor", e);
		}
	}
	
	@Override
	public void encodeChildren(FacesContext context) throws IOException {
		super.encodeChildren(context);
		
		CaseManagerState caseHandlerState = (CaseManagerState)WFUtil.getBeanInstance(CaseManagerState.beanIdentifier);
		
		if(caseHandlerState.getShowCaseHandler()) {
			
			UIComponent facet = getFacet(caseManagerFacet);
			renderChild(context, facet);
		}
	}
	
	private int parseAction(IWContext iwc) {
		if (iwc.isParameterSet(UserCases.PARAMETER_ACTION)) {
			return Integer.parseInt(iwc.getParameter(UserCases.PARAMETER_ACTION));
		}
		return ACTION_VIEW;
	}

	private void showList(IWContext iwc) throws RemoteException {
		Form form = new Form();
		form.addParameter(UserCases.PARAMETER_ACTION, ACTION_MULTI_PROCESS_FORM);
//
		boolean showCheckBoxes = showCheckBox() && getCasesBusiness(iwc).allowAnonymousCases();
//		
//		Table2 table = new Table2();
//		table.setWidth("100%");
//		table.setCellpadding(0);
//		table.setCellspacing(0);
//		table.setStyleClass("adminTable");
//		table.setStyleClass("casesTable");
//		table.setStyleClass("ruler");
//		table.setID(getBlockID());
//
//		TableColumnGroup columnGroup = table.createColumnGroup();
//		TableColumn column = columnGroup.createColumn();
//		column.setSpan(6);
//		column = columnGroup.createColumn();
//		column.setSpan(1);
//		column.setWidth("12");
//
//		Collection<GeneralCase> cases = getCases(iwc.getCurrentUser());
//
//		TableRowGroup group = table.createHeaderRowGroup();
//		TableRow row = group.createRow();
//
//		
//		TableCell2 cell = row.createHeaderCell();
//		cell.setStyleClass("firstColumn");
//		cell.setStyleClass("caseNumber");
//		cell.add(new Text(getResourceBundle().getLocalizedString(getPrefix() + "case_nr", "Case nr.")));
//
//		cell = row.createHeaderCell();
//		cell.setStyleClass("sender");
//		cell.add(new Text(getResourceBundle().getLocalizedString("sender", "Sender")));
//
//		if (getBusiness().useTypes()) {
//			cell = row.createHeaderCell();
//			cell.setStyleClass("caseType");
//			cell.add(new Text(getResourceBundle().getLocalizedString("case_type", "Case type")));
//		}
//
//		cell = row.createHeaderCell();
//		cell.setStyleClass("createdDate");
//		cell.add(new Text(getResourceBundle().getLocalizedString("created_date", "Created date")));
//
//		cell = row.createHeaderCell();
//		cell.setStyleClass("status");
//		cell.add(new Text(getResourceBundle().getLocalizedString("status", "Status")));
//
//		cell = row.createHeaderCell();
//		cell.setStyleClass("handler");
//		cell.add(new Text(getResourceBundle().getLocalizedString("handler", "Handler")));
//
//		cell = row.createHeaderCell();
//		if (!showCheckBoxes) {
//			cell.setStyleClass("lastColumn");
//		}
//		cell.setStyleClass("view");
//		cell.add(new Text(getResourceBundle().getLocalizedString("view", "View")));
//
//		if (showCheckBoxes) {
//			cell = row.createHeaderCell();
//			cell.setStyleClass("lastColumn");
//			cell.setStyleClass("multiHandle");
//			cell.add(Text.getNonBrakingSpace());
//		}
//
//		group = table.createBodyRowGroup();
//		int iRow = 1;
//
//		Layer customerViewContainer = new Layer();
//		
//		for (Iterator<GeneralCase> iter = cases.iterator(); iter.hasNext();) {
//			
//			GeneralCase theCase = iter.next();
//			CaseStatus status = theCase.getCaseStatus();
//			CaseType type = theCase.getCaseType();
//			User owner = theCase.getOwner();
//			IWTimestamp created = new IWTimestamp(theCase.getCreated());
//			
//			CaseManager caseManager;
//			
//			if(theCase.getCaseManagerType() != null)
//				caseManager = getCaseHandlersProvider().getCaseHandler(theCase.getCaseManagerType());
//			else 
//				caseManager = null;
//
//			row = group.createRow();
//			if (iRow == 1) {
//				row.setStyleClass("firstRow");
//			}
//			else if (!iter.hasNext()) {
//				row.setStyleClass("lastRow");
//			}
//			if (theCase.isPrivate()) {
//				row.setStyleClass("isPrivate");
//			}
//			if (status.equals(getCasesBusiness(iwc).getCaseStatusReview())) {
//				row.setStyleClass("isReview");
//			}
//
//			cell = row.createCell();
//			cell.setStyleClass("firstColumn");
//			cell.setStyleClass("caseNumber");
//			
//			String caseIdentifier;
//			
//			if(caseManager != null)
//				caseIdentifier = caseManager.getProcessIdentifier(theCase);
//			else
//				caseIdentifier = null;
//			
//			if(caseIdentifier != null)
//				cell.add(new Text(caseIdentifier));
//			else
//				cell.add(new Text(theCase.getPrimaryKey().toString()));
//
//			cell = row.createCell();
//			cell.setStyleClass("sender");
//			if (owner != null) {
//				cell.add(new Text(new Name(owner.getFirstName(), owner.getMiddleName(), owner.getLastName()).getName(iwc.getCurrentLocale())));
//			}
//			else {
//				cell.add(new Text("-"));
//			}
//
//			if (getBusiness().useTypes()) {
//				cell = row.createCell();
//				cell.setStyleClass("caseType");
//				cell.add(new Text(type.getName()));
//			}
//
//			cell = row.createCell();
//			cell.setStyleClass("createdDate");
//			cell.add(new Text(created.getLocaleDateAndTime(iwc.getCurrentLocale(), IWTimestamp.SHORT, IWTimestamp.SHORT)));
//
//			cell = row.createCell();
//			cell.setStyleClass("status");
//			cell.add(new Text(getBusiness().getLocalizedCaseStatusDescription(theCase, status, iwc.getCurrentLocale())));
//
//			User handler = theCase.getHandledBy();
//			cell = row.createCell();
//			cell.setStyleClass("handler");
//			if (handler != null) {
//				cell.add(new Text(new Name(handler.getFirstName(), handler.getMiddleName(), handler.getLastName()).getName(iwc.getCurrentLocale())));
//			}
//			else {
//				cell.add(new Text("-"));
//			}
//
//			cell = row.createCell();
//			if (!showCheckBoxes) {
//				cell.setStyleClass("lastColumn");
//			}
//			cell.setStyleClass("view");
//			
//			if(caseManager == null) {
//			
//				cell.add(getProcessLink(getBundle().getImage("edit.png", getResourceBundle().getLocalizedString(getPrefix() + "view_case", "View case")), theCase));
//				
//			} else {
//				
//				List<Link> links = caseManager.getCaseLinks(theCase, getCasesProcessorType());
//				
//				if(links != null)
//					for (Link link : links)
//						cell.add(link);
//			}
//
//			if (showCheckBoxes) {
//				CheckBox box = new CheckBox(PARAMETER_CASE_PK, theCase.getPrimaryKey().toString());
//
//				cell = row.createCell();
//				cell.setStyleClass("firstColumn");
//				cell.setStyleClass("multiHandle");
//				cell.add(box);
//			}
//
//			if (iRow % 2 == 0) {
//				row.setStyleClass("evenRow");
//			}
//			else {
//				row.setStyleClass("oddRow");
//			}
//		}
//
//		form.add(table);
//		form.add(getLegend(iwc));
//
//		if (showCheckBoxes) {
//			Layer layer = new Layer();
//			layer.setStyleClass("buttonLayer");
//			layer.setStyleClass("multiProcessLayer");
//			form.add(layer);
//
//			SubmitButton multiProcess = new SubmitButton(getResourceBundle().getLocalizedString("multi_process", "Multi process"), PARAMETER_ACTION, String.valueOf(ACTION_MULTI_PROCESS_FORM));
//			multiProcess.setStyleClass("button");
//			layer.add(multiProcess);
//		}

		add(form);
		
		showNewList(iwc, form, showCheckBoxes);
		
		form.add(getLegend(iwc));
		
		if (showCheckBoxes) {
			Layer layer = new Layer();
			layer.setStyleClass("buttonLayer");
			layer.setStyleClass("multiProcessLayer");
			form.add(layer);

			SubmitButton multiProcess = new SubmitButton(getResourceBundle().getLocalizedString("multi_process", "Multi process"), UserCases.PARAMETER_ACTION, String.valueOf(ACTION_MULTI_PROCESS_FORM));
			multiProcess.setStyleClass("button");
			layer.add(multiProcess);
		}
	}
	
	private void showNewList(IWContext iwc, Form form, boolean showCheckBoxes) throws RemoteException {
		GeneralCasesListBuilder listBuilder = (GeneralCasesListBuilder)WFUtil.getBeanInstance(iwc, GeneralCasesListBuilder.SPRING_BEAN_IDENTIFIER);
		form.add(listBuilder.getCasesList(iwc, getCases(iwc.getCurrentUser()), getCasesProcessorType(), showCheckBoxes, isUsePDFDownloadColumn(),
				isAllowPDFSigning(), isShowStatistics()));
	}

	protected void showMultiProcessForm(IWContext iwc) throws RemoteException {
		Form form = new Form();
		form.setStyleClass("adminForm");
		form.maintainParameter(PARAMETER_CASE_PK);
		form.addParameter(UserCases.PARAMETER_ACTION, "");

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
		statuses.addMenuElement(getCasesBusiness().getCaseStatusReady().getStatus(), getCasesBusiness().getLocalizedCaseStatusDescription(getCasesBusiness().getCaseStatusReady(), iwc.getCurrentLocale()));
		statuses.addMenuElement(getCasesBusiness().getCaseStatusWaiting().getStatus(), getCasesBusiness().getLocalizedCaseStatusDescription(getCasesBusiness().getCaseStatusWaiting(), iwc.getCurrentLocale()));
		statuses.addMenuElement(getCasesBusiness().getCaseStatusPending().getStatus(), getCasesBusiness().getLocalizedCaseStatusDescription(getCasesBusiness().getCaseStatusPending(), iwc.getCurrentLocale()));

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
		back.setValueOnClick(UserCases.PARAMETER_ACTION, String.valueOf(ACTION_VIEW));
		back.setToFormSubmit(form);
		bottom.add(back);

		Link next = getButtonLink(getResourceBundle().getLocalizedString("process", "Process"));
		next.setValueOnClick(UserCases.PARAMETER_ACTION, String.valueOf(ACTION_MULTI_PROCESS));
		next.setToFormSubmit(form);
		bottom.add(next);

		add(form);
	}

	protected void showAllocationForm(IWContext iwc, Object casePK) throws RemoteException {
		Form form = new Form();
		form.setStyleClass("adminForm");
		form.maintainParameter(PARAMETER_CASE_PK);
		form.addParameter(UserCases.PARAMETER_ACTION, "");
		
		boolean useSubCategories = getCasesBusiness(iwc).useSubCategories();

		super.getParentPage().addJavascriptURL("/dwr/interface/CasesDWRUtil.js");
		super.getParentPage().addJavascriptURL("/dwr/engine.js");
		super.getParentPage().addJavascriptURL("/dwr/util.js");
		super.getParentPage().addJavascriptURL(getBundle().getResourcesVirtualPath() + "/js/navigation.js");

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
			theCase = getCasesBusiness().getGeneralCase(casePK);
		}
		catch (FinderException fe) {
			fe.printStackTrace();
			throw new IBORuntimeException(fe);
		}
		CaseCategory category = theCase.getCaseCategory();
		CaseCategory parentCategory = category.getParent();
		CaseType type = theCase.getCaseType();
		Group handlerGroup = category.getHandlerGroup();

		SelectorUtility util = new SelectorUtility();
		DropdownMenu categories = (DropdownMenu) util.getSelectorFromIDOEntities(new DropdownMenu(PARAMETER_CASE_CATEGORY_PK), getCasesBusiness().getCaseCategories(), "getName");
		categories.setID(PARAMETER_CASE_CATEGORY_PK);
		categories.setSelectedElement(parentCategory != null ? parentCategory.getPrimaryKey().toString() : category.getPrimaryKey().toString());
		categories.setStyleClass("caseCategoryDropdown");
		if (useSubCategories) {
			categories.setOnChange("changeSubCategories('" + PARAMETER_CASE_CATEGORY_PK + "', '" + iwc.getCurrentLocale().getCountry() + "')");
		}
		categories.setOnChange("changeUsers('" + PARAMETER_CASE_CATEGORY_PK + "')");

		DropdownMenu subCategories = new DropdownMenu(PARAMETER_SUB_CASE_CATEGORY_PK);
		subCategories.setID(PARAMETER_SUB_CASE_CATEGORY_PK);
		subCategories.setSelectedElement(category.getPrimaryKey().toString());
		subCategories.setStyleClass("subCaseCategoryDropdown");
		subCategories.setOnChange("changeUsers('" + PARAMETER_SUB_CASE_CATEGORY_PK + "')");

		@SuppressWarnings("unchecked")
		Collection<CaseCategory> collection = getCasesBusiness(iwc).getSubCategories(parentCategory != null ? parentCategory : category);
		if (collection.isEmpty()) {
			subCategories.addMenuElement(category.getPrimaryKey().toString(), getResourceBundle().getLocalizedString("case_creator.no_sub_category", "no sub category"));
		}
		else {
			subCategories.addMenuElement(category.getPrimaryKey().toString(), getResourceBundle().getLocalizedString("case_creator.select_sub_category", "Select sub category"));
			Iterator<CaseCategory> iter = collection.iterator();
			while (iter.hasNext()) {
				CaseCategory subCategory = iter.next();
				subCategories.addMenuElement(subCategory.getPrimaryKey().toString(), subCategory.getLocalizedCategoryName(iwc.getCurrentLocale()));
			}
		}

		DropdownMenu types = (DropdownMenu) util.getSelectorFromIDOEntities(new DropdownMenu(PARAMETER_CASE_TYPE_PK), getCasesBusiness().getCaseTypes(), "getName");
		types.keepStatusOnAction(true);
		types.setSelectedElement(type.getPrimaryKey().toString());
		types.setStyleClass("caseTypeDropdown");
		
		HiddenInput hiddenType = new HiddenInput(PARAMETER_CASE_TYPE_PK, type.getPrimaryKey().toString());
		
		@SuppressWarnings("unchecked")
		Collection<User> handlers = getUserBusiness().getUsersInGroup(handlerGroup);
		DropdownMenu users = new DropdownMenu(PARAMETER_USER);

		for (User handler : handlers)
			users.addMenuElement(handler.getPrimaryKey().toString(), handler.getName());

		TextArea message = new TextArea(PARAMETER_MESSAGE);
		message.setStyleClass("textarea");
		message.keepStatusOnAction(true);
		
		if (getCasesBusiness().useTypes()) {
			Layer element = new Layer(Layer.DIV);
			element.setStyleClass("formItem");
			Label label = new Label(getResourceBundle().getLocalizedString("case_type", "Case type"), types);
			element.add(label);
			element.add(types);
			section.add(element);
		}
		else {
			form.add(hiddenType);
		}

		Layer element = new Layer(Layer.DIV);
		element.setStyleClass("formItem");
		Label label = new Label(getResourceBundle().getLocalizedString("case_category", "Case category"), categories);
		element.add(label);
		element.add(categories);
		section.add(element);

		if (useSubCategories) {
			element = new Layer(Layer.DIV);
			element.setStyleClass("formItem");
			label = new Label(getResourceBundle().getLocalizedString("sub_case_category", "Sub case category"), subCategories);
			element.add(label);
			element.add(subCategories);
			section.add(element);
		}

		element = new Layer(Layer.DIV);
		element.setStyleClass("formItem");
		label = new Label(getResourceBundle().getLocalizedString("handler", "Handler"), users);
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
		back.setValueOnClick(UserCases.PARAMETER_ACTION, String.valueOf(ACTION_PROCESS));
		back.setToFormSubmit(form);
		bottom.add(back);

		Link next = getButtonLink(getResourceBundle().getLocalizedString("allocate", "Allocate"));
		next.setValueOnClick(UserCases.PARAMETER_ACTION, String.valueOf(ACTION_SAVE));
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
	
//	private Link getProcessLink(PresentationObject object, GeneralCase theCase) {
//		Link process = new Link(object);
//		process.addParameter(PARAMETER_CASE_PK, theCase.getPrimaryKey().toString());
//		process.addParameter(PARAMETER_ACTION, ACTION_PROCESS);
//
//		return process;
//	}
	
	protected Collection<GeneralCase> getCases(User user) {
		
		return getCasesBusiness().getCasesForUser(user, getCasesProcessorType());
	}
	
	public abstract boolean isUsePDFDownloadColumn();

	public abstract void setUsePDFDownloadColumn(boolean usePDFDownloadColumn);

	public abstract boolean isAllowPDFSigning();

	public abstract void setAllowPDFSigning(boolean allowPDFSigning);
	
	public abstract boolean isShowStatistics();
	
	public abstract void setShowStatistics(boolean showStatistics);

	protected abstract void showProcessor(IWContext iwc, Object casePK) throws RemoteException;
	
	protected abstract String getCasesProcessorType();

	protected abstract void save(IWContext iwc) throws RemoteException;

	protected abstract boolean showCheckBox();
	
	protected abstract void initializeTableSorter(IWContext iwc) throws RemoteException;
}