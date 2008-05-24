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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.FinderException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import com.idega.block.process.business.CaseManager;
import com.idega.block.process.business.CaseManagersProvider;
import com.idega.block.process.data.CaseStatus;
import com.idega.block.process.presentation.UserCases;
import com.idega.block.process.presentation.beans.CaseManagerState;
import com.idega.block.web2.business.Web2Business;
import com.idega.business.IBORuntimeException;
import com.idega.business.SpringBeanLookup;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.CSSSpacer;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.text.Heading5;
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
import com.idega.util.CoreConstants;
import com.idega.util.CoreUtil;
import com.idega.util.IWTimestamp;
import com.idega.util.PresentationUtil;
import com.idega.util.text.Name;
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
	//public static final int SHOW_CASE_HANDLER = UserCases.SHOW_CASE_HANDLER;
	
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
					
//				case SHOW_CASE_HANDLER:
//					showCaseHandlerView(iwc);
//					break;
				default:
					showList(iwc);
			}
		}
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
	
	/*
	public void showCaseHandlerView(IWContext iwc) {
		
		try {
			GeneralCase theCase = getBusiness().getGeneralCase(iwc.getParameter(PARAMETER_CASE_PK));
			String caseManagerType = theCase.getCaseManagerType();
			
			if(caseManagerType == null || CoreConstants.EMPTY.equals(caseManagerType)) {
				Logger.getLogger(getClassName()).log(Level.SEVERE, "No case handlerType resolved from case, though showCaseHandlerView method was called");
				return;
			}
			
			CaseManager caseManager = getCaseHandlersProvider().getCaseHandler(caseManagerType);
			
			if(caseManager == null) {
				
				Logger.getLogger(getClassName()).log(Level.SEVERE, "No case handler found for case handler type provided: "+caseManagerType);
				return;
			}
			
			CaseManagerState caseManagerState = (CaseManagerState)WFUtil.getBeanInstance(CaseManagerState.beanIdentifier);
			caseManagerState.setCaseId(new Integer(String.valueOf(theCase.getPrimaryKey())));
			caseManagerState.setShowCaseHandler(true);
			caseManagerState.setFullView(true);
			caseManagerState.setInCasesComponent(true);
			
			UIComponent view = caseManager.getView(iwc, theCase);
			getFacets().put(caseManagerFacet, view);
			
		} catch (FinderException fe) {
			fe.printStackTrace();
			throw new IBORuntimeException(fe);
		} catch (RemoteException fe) {
			fe.printStackTrace();
			throw new IBORuntimeException(fe);
		}
	}
	*/
	
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
		if (iwc.isParameterSet(PARAMETER_ACTION)) {
			return Integer.parseInt(iwc.getParameter(PARAMETER_ACTION));
		}
		return ACTION_VIEW;
	}

	private void showList(IWContext iwc) throws RemoteException {
		Form form = new Form();
		form.addParameter(PARAMETER_ACTION, ACTION_MULTI_PROCESS_FORM);
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

			SubmitButton multiProcess = new SubmitButton(getResourceBundle().getLocalizedString("multi_process", "Multi process"), PARAMETER_ACTION, String.valueOf(ACTION_MULTI_PROCESS_FORM));
			multiProcess.setStyleClass("button");
			layer.add(multiProcess);
		}
	}
	
	private void addWeb2Stuff(IWContext iwc, Layer container) {
		Web2Business web2Business = SpringBeanLookup.getInstance().getSpringBean(iwc, Web2Business.class);
		
		List<String> scripts = new ArrayList<String>();
		try {
			scripts.add(web2Business.getBundleURIToJQueryLib());
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		scripts.add(web2Business.getBundleURIToJQGrid());
		IWBundle bundle = getBundle(iwc);
		scripts.add(bundle.getVirtualPathWithFileNameString("javascript/CasesListHelper.js"));
		scripts.add(CoreConstants.DWR_ENGINE_SCRIPT);
		scripts.add(CoreConstants.DWR_UTIL_SCRIPT);
		scripts.add("/dwr/interface/CasesEngine.js");
		scripts.add("/dwr/interface/BPMProcessAssets.js");
	
		List<String> css = new ArrayList<String>();
		css.add(web2Business.getBundleURIToJQGridStyles());
		
		String initAction = "initializeCasesList();";
		if (CoreUtil.isSingleComponentRenderingProcess(iwc)) {
			container.add(PresentationUtil.getJavaScriptSourceLines(scripts));
			container.add(PresentationUtil.getStyleSheetsSourceLines(css));
			container.add(PresentationUtil.getJavaScriptAction(initAction));
		}
		else {
			PresentationUtil.addJavaScriptSourcesLinesToHeader(iwc, scripts);
			PresentationUtil.addStyleSheetsToHeader(iwc, css);
			PresentationUtil.addJavaScriptActionToBody(iwc, "jQuery(document).ready(function() {"+initAction+"});");
		}
	}
	
	private void showNewList(IWContext iwc, Form form, boolean showCheckBoxes) throws RemoteException {
		Layer container = new Layer();
		form.add(container);
		
		IWResourceBundle iwrb = getResourceBundle(iwc);
		
		addWeb2Stuff(iwc, container);
		
		Layer casesContainer = new Layer();
		container.add(casesContainer);
		Layer headers = new Layer();
		casesContainer.add(headers);
		headers.setStyleClass("casesListHeadersContainer");
		String headerItem = "casesListHeadersContainerItem";
		
//		Layer togglerContainer = new Layer();
//		headers.add(togglerContainer);
//		togglerContainer.setStyleClass(headerItem);
//		togglerContainer.setStyleClass("casesListHeaderItemToggler");
//		togglerContainer.add(Text.getNonBrakingSpace(10));

		//	Number
		addLayerToCasesList(headers, new Text(iwrb.getLocalizedString(getPrefix() + "case_nr", "Case nr.")), headerItem, "CaseNumber");
//		Layer numberContainer = new Layer();
//		headers.add(numberContainer);
//		numberContainer.setStyleClass(headerItem);
//		numberContainer.setStyleClass("casesListHeaderItemCaseNumber");
//		numberContainer.add();

		//	Sender
		addLayerToCasesList(headers, new Text(iwrb.getLocalizedString("sender", "Sender")), headerItem, "Sender");
//		Layer senderContainer = new Layer();
//		headers.add(senderContainer);
//		senderContainer.setStyleClass(headerItem);
//		senderContainer.setStyleClass("casesListHeaderItemSender");
//		senderContainer.add(new Text(iwrb.getLocalizedString("sender", "Sender")));
		
		//	Description
		addLayerToCasesList(headers, new Text(iwrb.getLocalizedString("description", "Description")), headerItem, "Description");

		//	Type
//		if (getBusiness().useTypes()) {
//			Layer typeContainer = new Layer();
//			headers.add(typeContainer);
//			typeContainer.setStyleClass(headerItem);
//			typeContainer.setStyleClass("casesListHeaderItemCaseType");
//			typeContainer.add(new Text(iwrb.getLocalizedString("case_type", "Case type")));
//		}

		//	Creation date
		addLayerToCasesList(headers, new Text(iwrb.getLocalizedString("created_date", "Created date")), headerItem, "CreatedDate");
//		Layer creationDateContainer = new Layer();
//		headers.add(creationDateContainer);
//		creationDateContainer.setStyleClass(headerItem);
//		creationDateContainer.setStyleClass("casesListHeaderItemCreatedDate");
//		creationDateContainer.add(new Text(iwrb.getLocalizedString("created_date", "Created date")));

		//	Status
		addLayerToCasesList(headers, new Text(iwrb.getLocalizedString("status", "Status")), headerItem, "Status");
//		Layer statusContainer = new Layer();
//		headers.add(statusContainer);
//		statusContainer.setStyleClass(headerItem);
//		statusContainer.setStyleClass("casesListHeaderItemStatus");
//		statusContainer.add(new Text(iwrb.getLocalizedString("status", "Status")));
		
		//	Toggler - controller
		addLayerToCasesList(headers, new Text(getResourceBundle().getLocalizedString("view", "View")), headerItem, "Toggler");

		//	Handler
//		Layer handlerContainer = new Layer();
//		headers.add(handlerContainer);
//		handlerContainer.setStyleClass(headerItem);
//		handlerContainer.setStyleClass("casesListHeaderItemHandler");
//		handlerContainer.add(new Text(iwrb.getLocalizedString("handler", "Handler")));

		//	Handle case
		if (showCheckBoxes) {
			addLayerToCasesList(headers, Text.getNonBrakingSpace(), headerItem, "MultiHandle");
		}
		
		headers.add(new CSSSpacer());
		
		Collection<GeneralCase> cases = getCases(iwc.getCurrentUser());
		if (cases ==  null || cases.isEmpty()) {
			container.add(new Heading5(iwrb.getLocalizedString("no_case_exist", "There are no cases")));
			return;
		}
		
		Layer casesBodyContainer = new Layer();
		casesContainer.add(casesBodyContainer);
		casesBodyContainer.setStyleClass("casesListBodyContainer");
		
		int rowsCounter = 0;
		Layer caseContainer = null;
		Locale l = iwc.getCurrentLocale();
		String caseContainerStyle = "casesListCaseContainer";
		String bodyItem = "casesListBodyContainerItem";
		for(GeneralCase theCase: cases) {			
			caseContainer = new Layer();
			casesBodyContainer.add(caseContainer);
			caseContainer.setStyleClass(caseContainerStyle);
			
			CaseStatus status = theCase.getCaseStatus();
			User owner = theCase.getOwner();
			IWTimestamp created = new IWTimestamp(theCase.getCreated());
			
			CaseManager caseManager = null;
			if (theCase.getCaseManagerType() != null) {
				caseManager = getCaseHandlersProvider().getCaseHandler(theCase.getCaseManagerType());
			}
//			if (rowsCounter % 2 == 0) {	//	TODO: remove this
//				caseManager = null;
//			}
			
			if (rowsCounter == 0) {
				caseContainer.setStyleClass("firstRow");
			}

			if (theCase.isPrivate()) {
				caseContainer.setStyleClass("isPrivate");
			}
			if (status.equals(getCasesBusiness(iwc).getCaseStatusReview())) {
				caseContainer.setStyleClass("isReview");
			}
			
			//	Number
			Layer numberContainer = addLayerToCasesList(caseContainer, null, bodyItem, "CaseNumber");
			String caseIdentifier = caseManager == null ? theCase.getPrimaryKey().toString() : caseManager.getProcessIdentifier(theCase);
			numberContainer.setStyleClass("firstColumn");
			numberContainer.add(new Text(caseIdentifier));

			//	Sender
			Layer senderContainer = addLayerToCasesList(caseContainer, null, bodyItem, "Sender");
			senderContainer.add(owner == null ? new Text(CoreConstants.MINUS) : new Text(new Name(owner.getFirstName(), owner.getMiddleName(), owner.getLastName()).getName(l)));

			//	Type
//			if (getBusiness().useTypes()) {
//				Layer typeContainer = new Layer();
//				caseContainer.add(typeContainer);
//				typeContainer.setStyleClass(bodyItem);
//				typeContainer.setStyleClass("casesListBodyItemCaseType");
//				typeContainer.add(new Text(type.getName()));
//			}
			
			//	Description
			Layer descriptionContainer = addLayerToCasesList(caseContainer, null, bodyItem, "Description");
			String subject = theCase.getSubject();
			descriptionContainer.add(new Text(subject == null ? CoreConstants.MINUS : subject));

			//	Creation date
			Layer creationDateContainer = addLayerToCasesList(caseContainer, null, bodyItem, "CreationDate");
			creationDateContainer.add(new Text(created.getLocaleDateAndTime(l, IWTimestamp.SHORT, IWTimestamp.SHORT)));

			//	Status
			addLayerToCasesList(caseContainer, new Text(getBusiness().getLocalizedCaseStatusDescription(theCase, status, l)), bodyItem, "Status");
//			statusContainer = new Layer();
//			caseContainer.add(statusContainer);
//			statusContainer.setStyleClass(bodyItem);
//			statusContainer.setStyleClass("casesListBodyItemStatus");
//			statusContainer.add(new Text(getBusiness().getLocalizedCaseStatusDescription(theCase, status, l)));

			//	Controller
			Layer customerView = null;
			UIComponent childForContainer = null;
			if (caseManager == null) {
				childForContainer = getProcessLink(getBundle().getImage("edit.png", getResourceBundle().getLocalizedString(getPrefix() + "view_case", "View case")), theCase);
			}
			else {
				childForContainer = Text.getNonBrakingSpace(10);
			}
			Layer togglerContainer = addLayerToCasesList(caseContainer, childForContainer, caseManager == null ? null : bodyItem, "Toggler");
			if (caseManager != null) {
				togglerContainer.setStyleClass("expand");
				togglerContainer.setMarkupAttribute("caseid", theCase.getPrimaryKey().toString());
				customerView = new Layer();
				togglerContainer.setMarkupAttribute("customerviewid", customerView.getId());
			}
			
			//	Handler
//			User handler = theCase.getHandledBy();
//			handlerContainer = new Layer();
//			caseContainer.add(handlerContainer);
//			handlerContainer.setStyleClass(bodyItem);
//			handlerContainer.setStyleClass("casesListBodyItemHandler");
//			handlerContainer.add(handler == null ? new Text(CoreConstants.MINUS) : new Text(new Name(handler.getFirstName(), handler.getMiddleName(), handler.getLastName()).getName(l)));

			//	Handle case
			if (showCheckBoxes) {
				CheckBox box = new CheckBox(PARAMETER_CASE_PK, theCase.getPrimaryKey().toString());

				Layer multiHandleContainer = addLayerToCasesList(caseContainer, box, bodyItem, "MultiHandle");
				multiHandleContainer.setStyleClass("lastColumn");
			}

			if (rowsCounter % 2 == 0) {
				caseContainer.setStyleClass("evenRow");
			}
			else {
				caseContainer.setStyleClass("oddRow");
			}
			rowsCounter++;
			
			caseContainer.add(new CSSSpacer());
			
			if (customerView != null) {
				caseContainer.add(customerView);
				customerView.setStyleAttribute("display", "none");
			}
		}
		caseContainer.setStyleClass("lastRow");
	}
	
	private Layer addLayerToCasesList(Layer container, UIComponent child, String defaultStyleClass, String suffixForStyleClass) {
		Layer layer = new Layer();
		container.add(layer);
		
		if (defaultStyleClass != null) {
			layer.setStyleClass(defaultStyleClass);
			if (suffixForStyleClass != null) {
				layer.setStyleClass(defaultStyleClass + suffixForStyleClass);
			}
		}
		
		if (child != null) {
			layer.add(child);
		}
		
		return layer;
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
		
		@SuppressWarnings("unchecked")
		Collection<User> handlers = getUserBusiness().getUsersInGroup(handlerGroup);
		DropdownMenu users = new DropdownMenu(PARAMETER_USER);

		for (User handler : handlers)
			users.addMenuElement(handler.getPrimaryKey().toString(), handler.getName());

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
		Link process = new Link(object);
		
		process.addParameter(PARAMETER_CASE_PK, theCase.getPrimaryKey().toString());
		process.addParameter(PARAMETER_ACTION, ACTION_PROCESS);

		return process;
	}
	
	protected Collection<GeneralCase> getCases(User user) throws RemoteException {
		
		List<CaseManager> caseHandlers = getCaseHandlersProvider().getCaseHandlers();
		Collection<GeneralCase> cases = null;
		
		for (CaseManager handler : caseHandlers) {
			
			@SuppressWarnings("unchecked")
			Collection<GeneralCase> cazes = (Collection<GeneralCase>)handler.getCases(user, getCasesProcessorType());
			
			if(cazes != null) {
				
				if(cases == null)
					cases = cazes;
				else
					cases.addAll(cazes);
			}
		}
		
		return cases;
	}

	protected abstract void showProcessor(IWContext iwc, Object casePK) throws RemoteException;
	
	protected abstract String getCasesProcessorType();

	protected abstract void save(IWContext iwc) throws RemoteException;

	protected abstract boolean showCheckBox();
	
	public CaseManagersProvider getCaseHandlersProvider() {
		
		return (CaseManagersProvider)WFUtil.getBeanInstance(CaseManagersProvider.beanIdentifier);
	}
}