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

import org.springframework.beans.factory.annotation.Autowired;

import com.idega.block.process.business.CaseManagersProvider;
import com.idega.block.process.presentation.UICasesList;
import com.idega.block.process.presentation.UserCases;
import com.idega.block.process.presentation.beans.CaseManagerState;
import com.idega.block.process.presentation.beans.GeneralCaseManagerViewBuilder;
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
import com.idega.util.CoreConstants;
import com.idega.util.PresentationUtil;
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

	public static final int ACTION_VIEW = UserCases.ACTION_VIEW;
	public static final int ACTION_PROCESS = 2;
	public static final int ACTION_SAVE = 3;
	protected static final int ACTION_MULTI_PROCESS_FORM = 4;
	protected static final int ACTION_MULTI_PROCESS = 5;
	protected static final int ACTION_ALLOCATION_FORM = 6;
	
	@Autowired private CaseManagersProvider caseManagersProvider;
	
	private static final String caseManagerFacet = "caseManager";

	private int pageSize = 20;
	
	private int page = 1;
	
	private boolean showCaseNumberColumn = true;
	private boolean showCreationTimeInDateColumn = true;
	
	private String caseStatusesToHide;
	private String caseStatusesToShow;
	
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
	
	protected int parseAction(IWContext iwc) {
		if (iwc.isParameterSet(UserCases.PARAMETER_ACTION)) {
			return Integer.parseInt(iwc.getParameter(UserCases.PARAMETER_ACTION));
		}
		return ACTION_VIEW;
	}
	
	protected void showList(IWContext iwc) throws RemoteException {
		Layer topLayer = new Layer();
		Form form = new Form();
		form.addParameter(UserCases.PARAMETER_ACTION, ACTION_MULTI_PROCESS_FORM);
		boolean showCheckBoxes = showCheckBox() && getCasesBusiness(iwc).allowAnonymousCases();
		topLayer.add(form);
		
		add(topLayer);
				
		UICasesList list = (UICasesList)iwc.getApplication().createComponent(UICasesList.COMPONENT_TYPE);
		list.setType(getCasesProcessorType());
		list.setShowCheckBoxes(showCheckBoxes);
		list.setUsePDFDownloadColumn(isUsePDFDownloadColumn());
		list.setAllowPDFSigning(isAllowPDFSigning());
		list.setShowStatistics(isShowStatistics());
		list.setHideEmptySection(isHideEmptySection());
		list.setPageSize(getPageSize());
		list.setPage(getPage());
		list.setComponentId(topLayer.getId());
		list.setInstanceId(getBuilderService(iwc).getInstanceId(this));
		list.setShowCaseNumberColumn(isShowCaseNumberColumn());
		list.setShowCreationTimeInDateColumn(isShowCreationTimeInDateColumn());
		list.setCaseStatusesToHide(getCaseStatusesToHide());
		list.setCaseStatusesToShow(getCaseStatusesToShow());
		
		form.add(list);
				
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

		PresentationUtil.addJavaScriptSourceLineToHeader(iwc, CoreConstants.DWR_ENGINE_SCRIPT);
		PresentationUtil.addJavaScriptSourceLineToHeader(iwc, CoreConstants.DWR_UTIL_SCRIPT);
		PresentationUtil.addJavaScriptSourceLineToHeader(iwc, "/dwr/interface/CasesBusiness.js");
		PresentationUtil.addJavaScriptSourceLineToHeader(iwc, getBundle().getVirtualPathWithFileNameString("js/navigation.js"));

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
		users.setID(PARAMETER_USER);

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
	
			
	public abstract boolean isUsePDFDownloadColumn();

	public abstract void setUsePDFDownloadColumn(boolean usePDFDownloadColumn);

	public abstract boolean isAllowPDFSigning();

	public abstract void setAllowPDFSigning(boolean allowPDFSigning);
	
	public abstract boolean isShowStatistics();
	
	public abstract void setShowStatistics(boolean showStatistics);
	
	public abstract boolean isHideEmptySection();
	
	public abstract void setHideEmptySection(boolean hideEmptySection);

	protected abstract void showProcessor(IWContext iwc, Object casePK) throws RemoteException;
	
	protected abstract String getCasesProcessorType();

	protected abstract void save(IWContext iwc) throws RemoteException;

	protected abstract boolean showCheckBox();
	
	protected abstract void initializeTableSorter(IWContext iwc) throws RemoteException;

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public CaseManagersProvider getCaseManagersProvider() {
		return caseManagersProvider;
	}

	public void setCaseManagersProvider(CaseManagersProvider caseManagersProvider) {
		this.caseManagersProvider = caseManagersProvider;
	}

	public boolean isShowCaseNumberColumn() {
		return showCaseNumberColumn;
	}

	public void setShowCaseNumberColumn(boolean showCaseNumberColumn) {
		this.showCaseNumberColumn = showCaseNumberColumn;
	}

	public boolean isShowCreationTimeInDateColumn() {
		return showCreationTimeInDateColumn;
	}

	public void setShowCreationTimeInDateColumn(boolean showCreationTimeInDateColumn) {
		this.showCreationTimeInDateColumn = showCreationTimeInDateColumn;
	}

	public String getCaseStatusesToHide() {
		return caseStatusesToHide;
	}

	public void setCaseStatusesToHide(String caseStatusesToHide) {
		this.caseStatusesToHide = caseStatusesToHide;
	}

	public String getCaseStatusesToShow() {
		return caseStatusesToShow;
	}

	public void setCaseStatusesToShow(String caseStatusesToShow) {
		this.caseStatusesToShow = caseStatusesToShow;
	}
}