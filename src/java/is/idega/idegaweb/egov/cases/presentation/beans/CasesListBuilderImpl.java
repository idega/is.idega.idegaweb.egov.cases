package is.idega.idegaweb.egov.cases.presentation.beans;

import is.idega.idegaweb.egov.cases.business.CasesBusiness;
import is.idega.idegaweb.egov.cases.data.GeneralCase;
import is.idega.idegaweb.egov.cases.presentation.CasesProcessor;
import is.idega.idegaweb.egov.cases.presentation.MyCases;
import is.idega.idegaweb.egov.cases.presentation.OpenCases;
import is.idega.idegaweb.egov.cases.util.CasesConstants;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.EJBException;
import javax.faces.component.UIComponent;

import org.directwebremoting.WebContext;
import org.directwebremoting.WebContextFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.idega.block.process.business.CaseBusiness;
import com.idega.block.process.business.CaseCodeManager;
import com.idega.block.process.business.CaseManager;
import com.idega.block.process.business.CaseManagersProvider;
import com.idega.block.process.data.Case;
import com.idega.block.process.data.CaseStatus;
import com.idega.block.process.presentation.UserCases;
import com.idega.block.process.presentation.beans.GeneralCasesListBuilder;
import com.idega.block.process.util.CaseComparator;
import com.idega.block.web2.business.JQueryUIType;
import com.idega.block.web2.business.Web2Business;
import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.core.accesscontrol.business.CredentialBusiness;
import com.idega.core.builder.data.ICPage;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.CSSSpacer;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.Layer;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.text.Heading3;
import com.idega.presentation.text.Heading5;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.CheckBox;
import com.idega.user.data.User;
import com.idega.util.CoreConstants;
import com.idega.util.CoreUtil;
import com.idega.util.IWTimestamp;
import com.idega.util.PresentationUtil;
import com.idega.util.text.Name;
import com.idega.webface.WFUtil;

@Scope("singleton")
@Service(GeneralCasesListBuilder.SPRING_BEAN_IDENTIFIER)
public class CasesListBuilderImpl implements GeneralCasesListBuilder {
	
	private CaseManagersProvider caseManagersProvider;
	
	private String caseContainerStyle = "casesListCaseContainer";
	private String bodyItem = "casesListBodyContainerItem";
	private String oldBodyItem = "old_" + bodyItem;
	private String lastRowStyle = "lastRow";
	
	private String caseIdParName = "caseid";
	
	private Layer createHeader(IWContext iwc, Layer container, int totalCases, boolean showCheckBoxes, boolean searchResults) {
		IWResourceBundle iwrb = getResourceBundle(iwc);
		if (totalCases < 1) {
			if (searchResults) {
				container.add(new Heading5(iwrb.getLocalizedString("no_cases_found", "No cases were found by your query!")));
			}
			else {
				container.add(new Heading5(iwrb.getLocalizedString("no_case_exist", "There are no cases")));
			}
			return container;
		}
		
		String caseId = iwc.getParameter(CasesProcessor.PARAMETER_CASE_PK);
		addResources(caseId, iwc, getBundle(iwc));
		
		if (searchResults) {
			StringBuilder message = new StringBuilder(iwrb.getLocalizedString("search_for_cases_results", "Your search results")).append(CoreConstants.SPACE);
			message.append("(").append(totalCases).append("):");
			container.add(new Heading5(message.toString()));
		}
		
		Layer casesContainer = new Layer();
		container.add(casesContainer);
		Layer headers = new Layer();
		casesContainer.add(headers);
		headers.setStyleClass("casesListHeadersContainer");
		String headerItem = "casesListHeadersContainerItem";

		//	Number
		addLayerToCasesList(headers, new Text(iwrb.getLocalizedString("case_nr", "Case nr.")), headerItem, "CaseNumber");

		//	Sender
		addLayerToCasesList(headers, new Text(iwrb.getLocalizedString("sender", "Sender")), headerItem, "Sender");
		
		//	Description
		addLayerToCasesList(headers, new Text(iwrb.getLocalizedString("description", "Description")), headerItem, "Description");

		//	Creation date
		addLayerToCasesList(headers, new Text(iwrb.getLocalizedString("created_date", "Created date")), headerItem, "CreatedDate");

		//	Status
		addLayerToCasesList(headers, new Text(iwrb.getLocalizedString("status", "Status")), headerItem, "Status");
		
		//	Toggler - controller
		addLayerToCasesList(headers, new Text(iwrb.getLocalizedString("view", "View")), headerItem, "Toggler");

		//	Handle case
		if (showCheckBoxes) {
			addLayerToCasesList(headers, Text.getNonBrakingSpace(), headerItem, "MultiHandle");
		}
		
		headers.add(new CSSSpacer());
		
		return casesContainer;
	}
	
	private Layer createBody(Layer casesContainer) {
		Layer casesBodyContainer = new Layer();
		casesContainer.add(casesBodyContainer);
		casesBodyContainer.setStyleClass("casesListBodyContainer");
		return casesBodyContainer;
	}
	
	private void prepareCellToBeGridExpander(Layer layer, String caseId, String gridViewerId) {
		layer.setStyleClass("casesListGridExpanderStyleClass");
		layer.setMarkupAttribute(caseIdParName, caseId);
		layer.setMarkupAttribute("customerviewid", gridViewerId);
	}
	
	@SuppressWarnings("unchecked")
	private Layer addRowToCasesList(IWContext iwc, Layer casesBodyContainer, Case theCase, CaseStatus caseStatusReview, Locale l, boolean showCheckBoxes,
			boolean isPrivate, boolean isUserList, int rowsCounter, Map pages, boolean addCredentialsToExernalUrls, String emailAddress, boolean descriptionIsEditable) {
		Layer caseContainer = new Layer();
		casesBodyContainer.add(caseContainer);
		caseContainer.setStyleClass(caseContainerStyle);
		
		CaseStatus status = theCase.getCaseStatus();
		User owner = theCase.getOwner();
		IWTimestamp created = new IWTimestamp(theCase.getCreated());
		
		CaseManager caseManager = null;
		if (theCase.getCaseManagerType() != null) {
			caseManager = getCasesBusiness(iwc).getCaseHandlersProvider().getCaseManager(theCase.getCaseManagerType());
		}
		
		if (rowsCounter == 0) {
			caseContainer.setStyleClass("firstRow");
		}

		if (isPrivate) {
			caseContainer.setStyleClass("isPrivate");
		}
		if (status != null && caseStatusReview != null) {
			if (status.equals(caseStatusReview)) {
				caseContainer.setStyleClass("isReview");
			}
		}
		
		boolean notGeneralCase = !(theCase instanceof GeneralCase);
		CaseBusiness caseBusiness = null;
		if (notGeneralCase) {
			try {
				caseBusiness = CaseCodeManager.getInstance().getCaseBusinessOrDefault(theCase.getCaseCode(), iwc);
			} catch (IBOLookupException e) {
				e.printStackTrace();
			}
		}
		
		//	Number
		Layer numberContainer = addLayerToCasesList(caseContainer, null, bodyItem, "CaseNumber");
		
		boolean caseIdentifierFromCaseManager = false;
		
		String caseIdentifier = caseManager == null ? theCase.getPrimaryKey().toString() : caseManager.getProcessIdentifier(theCase);
		
		if(caseManager != null) {
			caseIdentifier = caseManager.getProcessIdentifier(theCase);
			
			if(caseIdentifier == null) {
			
				caseIdentifier = theCase.getPrimaryKey().toString();
			} else {
				caseIdentifierFromCaseManager = true;
			}
		} else {
			
			caseIdentifier = theCase.getPrimaryKey().toString();
		}
		
		numberContainer.setStyleClass("firstColumn");
		if (caseManager == null) {
			numberContainer.add(caseIdentifier);
		} else {
			
			if(caseIdentifierFromCaseManager) {
			
				IWBundle bundle = getBundle(iwc);
				IWResourceBundle iwrb = getResourceBundle(iwc);
				Link sendEmail = new Link(bundle.getImage("images/email.png", iwrb.getLocalizedString("send_email", "Send e-mail")),
						getEmailAddressMailtoFormattedWithSubject(emailAddress, caseIdentifier));
				numberContainer.add(sendEmail);
				numberContainer.add(Text.getNonBrakingSpace());
			}
			
			numberContainer.add(caseIdentifier);
		}
		showCheckBoxes = caseManager == null ? showCheckBoxes : false;
		
		Layer customerView = null;
		String caseId = theCase.getPrimaryKey().toString();
		String gridViewerId = null;
		if (caseManager != null) {
			customerView = new Layer();
			gridViewerId = customerView.getId();
		}
		
		if (caseManager != null) {
			prepareCellToBeGridExpander(numberContainer, caseId, gridViewerId);
		}

		//	Sender
		Layer senderContainer = addLayerToCasesList(caseContainer, null, bodyItem, "Sender");
		senderContainer.add(owner == null ? new Text(CoreConstants.MINUS) : new Text(new Name(owner.getFirstName(), owner.getMiddleName(),
				owner.getLastName()).getName(l)));
		if (caseManager != null) {
			prepareCellToBeGridExpander(senderContainer, caseId, gridViewerId);
		}
		
		//	Description
		Layer descriptionContainer = addLayerToCasesList(caseContainer, null, bodyItem, "Description");
		if (descriptionIsEditable) {
			descriptionContainer.setStyleClass("casesListBodyItemIsEditable");
			descriptionContainer.setMarkupAttribute(caseIdParName, caseId);
		}
		String subject = null;
		if (notGeneralCase) {
			try {
				subject = caseBusiness.getCaseSubject(theCase, l);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		else {
			subject = theCase.getSubject();
		}
		if (subject != null && subject.length() > 100) {
			subject = new StringBuilder(subject.substring(0, 100)).append(CoreConstants.DOT).append(CoreConstants.DOT).append(CoreConstants.DOT).toString();
		}
		descriptionContainer.add(new Text(subject == null ? CoreConstants.MINUS : subject));

		//	Creation date
		Layer creationDateContainer = addLayerToCasesList(caseContainer, null, bodyItem, "CreationDate");
		creationDateContainer.add(new Text(created.getLocaleDateAndTime(l, IWTimestamp.SHORT, IWTimestamp.SHORT)));
		if (caseManager != null) {
			prepareCellToBeGridExpander(creationDateContainer, caseId, gridViewerId);
		}

		//	Status
		String localizedStatus = null;
		if (theCase instanceof GeneralCase) {
			try {
				localizedStatus = getCasesBusiness(iwc).getLocalizedCaseStatusDescription(theCase, status, l);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		else {
			try {
				localizedStatus = caseBusiness.getLocalizedCaseStatusDescription(theCase, status, l);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		Layer statusContainer = addLayerToCasesList(caseContainer, new Text(localizedStatus == null ? CoreConstants.MINUS : localizedStatus), bodyItem, "Status");
		if (caseManager != null) {
			prepareCellToBeGridExpander(statusContainer, caseId, gridViewerId);
		}
		
		//	Controller
		UIComponent childForContainer = null;
		if (caseManager == null) {
			Image view = getBundle(iwc).getImage("edit.png", getResourceBundle(iwc).getLocalizedString("view_case", "View case"));
			if (isUserList) {
				childForContainer = getLinkToViewUserCase(iwc, theCase, caseBusiness, view, pages, theCase.getCode(), status, addCredentialsToExernalUrls);
			}
			else {
				childForContainer = getProcessLink(iwc, view, theCase);
			}
		}
		else {
			childForContainer = Text.getNonBrakingSpace(10);
		}
		Layer togglerContainer = addLayerToCasesList(caseContainer, childForContainer, caseManager == null ? oldBodyItem : bodyItem, "Toggler");
		if (caseManager != null) {
			togglerContainer.setStyleClass("expand");
			togglerContainer.setMarkupAttribute("changeimage", "true");
			prepareCellToBeGridExpander(togglerContainer, caseId, gridViewerId);
		}
		
		//	Handle case
		if (showCheckBoxes) {
			CheckBox box = new CheckBox(CasesProcessor.PARAMETER_CASE_PK, theCase.getPrimaryKey().toString());

			Layer multiHandleContainer = addLayerToCasesList(caseContainer, box, bodyItem, "MultiHandle");
			multiHandleContainer.setStyleClass("lastColumn");
		}

		if (rowsCounter % 2 == 0) {
			caseContainer.setStyleClass("evenRow");
		}
		else {
			caseContainer.setStyleClass("oddRow");
		}
		
		caseContainer.add(new CSSSpacer());
		
		if (customerView != null) {
			caseContainer.add(customerView);
			customerView.setStyleAttribute("display", "none");
		}
	
		return caseContainer;
	}
	
	private String getEmailAddressMailtoFormattedWithSubject(String emailAddress, String subject) {
		if (emailAddress == null || CoreConstants.EMPTY.equals(emailAddress)) {
			return subject;
		}
		
		return new StringBuilder("mailto:").append(emailAddress).append("?subject=(").append(subject).append(")").toString();
	} 
	
	@SuppressWarnings("unchecked")
	private List<Case> getSortedCases(Collection cases) {
		if (cases == null || cases.isEmpty()) {
			return new ArrayList<Case>(0);
		}
		List<Case> casesInList = new ArrayList<Case>(cases);
		Collections.sort(casesInList, new CaseComparator());
		return casesInList;
	}
	
	private boolean isDescriptionEditable(String casesType, boolean isAdmin) {
		boolean descriptionIsEditable = OpenCases.TYPE.equals(casesType);
		if (!descriptionIsEditable) {
			descriptionIsEditable = MyCases.TYPE.equals(casesType) && isAdmin;
		}
		return descriptionIsEditable;
	}
	
	private Layer getCasesListContainer(boolean searchResults) {
		Layer container = new Layer();
		container.setStyleClass(GeneralCasesListBuilder.MAIN_CASES_LIST_CONTAINER_STYLE);
		if (searchResults) {
			container.setMarkupAttribute("searchresult", Boolean.TRUE.toString());
		}
		return container;
	}
	
	@SuppressWarnings("unchecked")
	public UIComponent getCasesList(IWContext iwc, Collection cases, String casesType, boolean showCheckBoxes) {		
		List<Case> casesInList = getSortedCases(cases);
		
		String emailAddress = getDefaultEmail(iwc);
		
		boolean descriptionIsEditable = isDescriptionEditable(casesType, iwc.isSuperAdmin());
		
		boolean searchResults = CasesConstants.CASE_LIST_TYPE_SEARCH_RESULTS.equals(casesType);
		Layer container = getCasesListContainer(searchResults);

		IWResourceBundle iwrb = getResourceBundle(iwc);
		CasesBusiness casesBusiness = getCasesBusiness(iwc);
		if (casesBusiness == null) {
			container.add(new Heading3(iwrb.getLocalizedString("cases_list.can_not_get_cases_list", "Sorry, error occurred - can not generate cases list.")));
			return container;
		}
		
		int totalCases = (casesInList == null || casesInList.isEmpty()) ? 0 : casesInList.size();
		
		Layer casesContainer = createHeader(iwc, container, totalCases, showCheckBoxes, searchResults);
		
		if (totalCases < 1) {
			return container;
		}
		
		Layer casesBodyContainer = createBody(casesContainer);
		
		int rowsCounter = 0;
		Layer caseContainer = null;
		Locale l = iwc.getCurrentLocale();
		CaseStatus caseStatusReview = null;
		try {
			caseStatusReview = casesBusiness.getCaseStatusReview();
		} catch (RemoteException e) {
			e.printStackTrace();
		}

		GeneralCase genCase = null;
		for (Object o: casesInList) {
			if (o instanceof GeneralCase) {
				genCase = (GeneralCase) o;
				caseContainer = addRowToCasesList(iwc, casesBodyContainer, genCase, caseStatusReview, l, showCheckBoxes, genCase.isPrivate(), false,
						rowsCounter, null, false, emailAddress, descriptionIsEditable);
			}
			else if (o instanceof Case) {
				caseContainer = addRowToCasesList(iwc, casesBodyContainer, (Case) o, caseStatusReview, l, showCheckBoxes, false, false, rowsCounter, null,
						false, emailAddress, descriptionIsEditable);
			}
			rowsCounter++;
		}
		if (caseContainer != null) {
			caseContainer.setStyleClass(lastRowStyle);
		}
		
		return container;
	}
	
	//	TODO: test this
	@SuppressWarnings("unchecked")
	public UIComponent getUserCasesList(IWContext iwc, Collection<Case> cases, Map pages, String casesType, boolean addCredentialsToExernalUrls) {
		List<Case> casesInList = getSortedCases(cases);
		
		String emailAddress = getDefaultEmail(iwc); 
		
		boolean descriptionIsEditable = isDescriptionEditable(casesType, iwc.isSuperAdmin());
		
		boolean searchResults = CasesConstants.CASE_LIST_TYPE_SEARCH_RESULTS.equals(casesType);
		Layer container = getCasesListContainer(searchResults);

		IWResourceBundle iwrb = getResourceBundle(iwc);
		CasesBusiness casesBusiness = getCasesBusiness(iwc);
		if (casesBusiness == null) {
			container.add(new Heading3(iwrb.getLocalizedString("cases_list.can_not_get_cases_list", "Sorry, error occurred - can not generate cases list.")));
			return container;
		}
		
		int totalCases = (casesInList == null || casesInList.isEmpty()) ? 0 : casesInList.size();
		
		Layer casesContainer = createHeader(iwc, container, totalCases, false, searchResults);
		
		if (totalCases < 1) {
			return container;
		}
		
		Layer casesBodyContainer = createBody(casesContainer);
		
		int rowsCounter = 0;
		Layer caseContainer = null;
		Locale l = iwc.getCurrentLocale();
		CaseStatus caseStatusReview = null;
		try {
			caseStatusReview = casesBusiness.getCaseStatusReview();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		for (Case theCase: casesInList) {			
			caseContainer = addRowToCasesList(iwc, casesBodyContainer, theCase, caseStatusReview, l, false, false, true, rowsCounter, pages,
					addCredentialsToExernalUrls, emailAddress, descriptionIsEditable);
			rowsCounter++;
		}
		caseContainer.setStyleClass(lastRowStyle);

		return container;
	}
	
	private String getDefaultEmail(IWContext iwc) {
		try {
			return iwc.getApplicationSettings().getProperty(CoreConstants.PROP_SYSTEM_ACCOUNT);
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private Link getLinkToViewUserCase(IWContext iwc, Case theCase, CaseBusiness caseBusiness, Image viewCaseImage, Map pages, String caseCode, CaseStatus caseStatus,
			boolean addCredentialsToExernalUrls) {
		if (caseBusiness == null) {
			try {
				caseBusiness = (CaseBusiness) IBOLookup.getServiceInstance(iwc, CaseBusiness.class);
			} catch (IBOLookupException e) {
				e.printStackTrace();
			}
		}
		if (caseBusiness == null) {
			return null;
		}
		
		ICPage page = getPage(pages, caseCode, caseStatus == null ? null : caseStatus.getStatus());
		String caseUrl = null;
		try {
			caseUrl = caseBusiness.getUrl(theCase);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		if (page != null) {
			Link link = new Link(viewCaseImage);
			link.setStyleClass("caseEdit");
			link.setToolTip(getToolTipForLink(iwc));
			
			Class<?> eventListener = null;
			try {
				eventListener = caseBusiness.getEventListener();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			if (eventListener != null) {
				link.setEventListener(eventListener);
			}
			Map parameters = null;
			try {
				parameters = caseBusiness.getCaseParameters(theCase);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			if (parameters != null) {
				link.setParameter(parameters);
			}
			
			try {
				link.addParameter(caseBusiness.getSelectedCaseParameter(), theCase.getPrimaryKey().toString());
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (EJBException e) {
				e.printStackTrace();
			}
			link.setPage(page);
			return link;
		}
		else if (caseUrl != null) {
			Link link = new Link(viewCaseImage, caseUrl);
			link.setStyleClass("caseEdit");
			link.setToolTip(getResourceBundle(iwc).getLocalizedString("view_case", "View case"));
			if (addCredentialsToExernalUrls) {
				try {
					getCredentialBusiness(iwc).addCredentialsToLink(link, iwc);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
			return link;
		}
		
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private ICPage getPage(Map pages, String caseCode, String caseStatus) {
		if (pages == null) {
			return null;
		}
		
		try {
			Object object = pages.get(caseCode);
			if (object instanceof ICPage) {
				return (ICPage) object;
			}
			else if (object instanceof Map) {
				Map statusMap = (Map) object;
				return (ICPage) statusMap.get(caseStatus);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	private String getToolTipForLink(IWContext iwc) {
		return getResourceBundle(iwc).getLocalizedString("view_case", "View case");
	}
	
	private Link getProcessLink(IWContext iwc, PresentationObject object, Case theCase) {
		Link process = new Link(object);
		
		WebContext webContext = WebContextFactory.get();
		if (webContext != null) {
			process.setURL(webContext.getCurrentPage());
		}
		
		process.setToolTip(getToolTipForLink(iwc));
		
		process.addParameter(CasesProcessor.PARAMETER_CASE_PK, theCase.getPrimaryKey().toString());
		process.addParameter(UserCases.PARAMETER_ACTION, CasesProcessor.ACTION_PROCESS);
		
		process.setStyleClass("old_casesListBodyContainerItemLink");

		return process;
	}
	
	private void addResources(String caseId, IWContext iwc, IWBundle bundle) {
		Web2Business web2Business = WFUtil.getBeanInstance(iwc, Web2Business.SPRING_BEAN_IDENTIFIER);
		
		List<String> scripts = new ArrayList<String>();
		scripts.add(web2Business.getBundleURIToJQueryLib());
		scripts.add(web2Business.getBundleURIToJQueryUILib(JQueryUIType.UI_EDITABLE));
		scripts.add(bundle.getVirtualPathWithFileNameString(CasesConstants.CASES_LIST_HELPER_JAVA_SCRIPT_FILE));
		scripts.add(CoreConstants.DWR_ENGINE_SCRIPT);
		scripts.add("/dwr/interface/CasesEngine.js");
		
		if (caseId == null || CoreConstants.EMPTY.equals(caseId)) {
			caseId = iwc.getParameter(CasesProcessor.PARAMETER_CASE_PK + "_id");
		}
		StringBuilder action = new StringBuilder("initializeCasesList(");
		if (caseId == null || CoreConstants.EMPTY.equals(action)) {
			action.append("null");
		}
		else {
			action.append("'").append(caseId).append("'");
		}
		action.append(", [");
		
		//	Localizations: array as parameter
		IWResourceBundle iwrb = getResourceBundle(iwc);
		
		action.append("'").append(iwrb.getLocalizedString("click_to_edit", "Click to edit...")).append("'");
		action.append(", '").append(iwrb.getLocalizedString("error_occurred_confirm_to_reload_page", "Oops! Error occurred. Reloading current page might help to avoid it. Do you want to reload current page?")).append("'");
		action.append(", '").append(iwrb.getLocalizedString("loading", "Loading...")).append("'");
		
		action.append("], false);");
		
		if (!CoreUtil.isSingleComponentRenderingProcess(iwc)) {
			action = new StringBuilder("jQuery(window).load(function() {").append(action.toString()).append("});");
		}
		
		//	Adding resources
		PresentationUtil.addJavaScriptSourcesLinesToHeader(iwc, scripts);
		PresentationUtil.addJavaScriptActionToBody(iwc, action.toString());
	}
	
	private CasesBusiness getCasesBusiness(IWApplicationContext iwac) {
		try {
			return (CasesBusiness) IBOLookup.getServiceInstance(iwac, CasesBusiness.class);
		} catch (IBOLookupException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	private CredentialBusiness getCredentialBusiness(IWApplicationContext iwac) {
		
		try {
			return (CredentialBusiness) IBOLookup.getServiceInstance(iwac, CredentialBusiness.class);
		}
		catch (IBOLookupException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	private IWBundle getBundle(IWContext iwc) {
		return iwc.getIWMainApplication().getBundle(CasesConstants.IW_BUNDLE_IDENTIFIER);
	}
	
	private IWResourceBundle getResourceBundle(IWContext iwc) {
		return getBundle(iwc).getResourceBundle(iwc);
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

	public UIComponent getCaseManagerView(IWContext iwc, Integer caseId) {
		
		try {
			Case theCase = getCasesBusiness(iwc).getCase(caseId);
			
			CaseManager caseManager;
			
			if(theCase.getCaseManagerType() != null)
				caseManager = getCaseManagersProvider().getCaseManager(theCase.getCaseManagerType());
			else 
				caseManager = null;
			
			if(caseManager != null) {
				
				UIComponent caseAssets = caseManager.getView(iwc, theCase);
				
				if(caseAssets != null)
					return caseAssets;
				else
					Logger.getLogger(getClass().getName()).log(Level.WARNING, "No case assets component resolved from case manager: "+caseManager.getType()+" by case pk: "+theCase.getPrimaryKey().toString());
			} else
				Logger.getLogger(getClass().getName()).log(Level.WARNING, "No case manager resolved by type="+theCase.getCaseManagerType());
			
		} catch (Exception e) {
			Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Exception while resolving case manager view", e);
		}
		
		return null;
	}

	public CaseManagersProvider getCaseManagersProvider() {
		return caseManagersProvider;
	}

	@Autowired
	public void setCaseManagersProvider(CaseManagersProvider caseManagersProvider) {
		this.caseManagersProvider = caseManagersProvider;
	}
	
}