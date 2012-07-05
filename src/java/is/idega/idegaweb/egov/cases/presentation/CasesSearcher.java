package is.idega.idegaweb.egov.cases.presentation;

import is.idega.idegaweb.egov.application.IWBundleStarter;
import is.idega.idegaweb.egov.cases.business.CasesEngine;
import is.idega.idegaweb.egov.cases.util.CasesConstants;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.faces.component.UIComponent;

import org.springframework.beans.factory.annotation.Autowired;

import com.idega.block.process.business.CaseBusiness;
import com.idega.block.process.data.CaseStatus;
import com.idega.block.process.presentation.beans.CasesSearchCriteriaBean;
import com.idega.block.process.presentation.beans.GeneralCasesListBuilder;
import com.idega.block.web2.business.JQuery;
import com.idega.block.web2.business.JQueryPlugin;
import com.idega.block.web2.business.Web2Business;
import com.idega.builder.bean.AdvancedProperty;
import com.idega.builder.business.AdvancedPropertyComparator;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.CSSSpacer;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.text.Heading1;
import com.idega.presentation.ui.CheckBox;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.GenericButton;
import com.idega.presentation.ui.HiddenInput;
import com.idega.presentation.ui.IWDatePicker;
import com.idega.presentation.ui.InterfaceObject;
import com.idega.presentation.ui.Label;
import com.idega.presentation.ui.SelectOption;
import com.idega.presentation.ui.TextInput;
import com.idega.util.CoreConstants;
import com.idega.util.CoreUtil;
import com.idega.util.ListUtil;
import com.idega.util.PresentationUtil;
import com.idega.util.StringUtil;
import com.idega.util.expression.ELUtil;

/**
 * Cases searcher. MUST be included in the same page as CasesList!
 * @author <a href="mailto:valdas@idega.com>Valdas Žemaitis</a>
 * Created: 2008.06.27
 * @version '$Revision 1.5'
 * Last modified: 2008.06.27 11:37:14 by: valdas
 */
public class CasesSearcher extends CasesBlock {

	private static final String PARAMETER_PROCESS_ID = "cf_prm_process_id";
	public static final String PARAMETER_CASE_STATUS = "cf_prm_case_status";
	private static final String PARAMETER_CASE_LIST_TYPE = "cf_prm_case_list_type";
	private static final String PARAMETER_CASE_CONTACT = "cf_prm_case_contact";
	private static final String PARAMETER_SORTING_OPTIONS = "cf_prm_sorting_options";

	private String textInputStyleClass = "textinput";
	private String buttonStyleClass = "button";


	private String listType;

	private boolean showAllStatuses;
	private boolean showExportButton = true;

	private String processProperty = null;

	@Autowired
	private CasesEngine casesEngine;
	@Autowired
	private Web2Business web2;
	@Autowired
	private JQuery jQuery;

	protected IWBundle bundle;
	protected IWResourceBundle iwrb;

	protected Layer container, inputsContainer;

	public CasesSearcher() {
		super();
	}

	private Web2Business getWeb2Business() {
		if (web2 == null)
			ELUtil.getInstance().autowire(this);
		return web2;
	}

	private JQuery getJQuery() {
		if (jQuery == null)
			ELUtil.getInstance().autowire(this);
		return jQuery;
	}

	protected void addResources(IWContext iwc) {
		bundle = iwc.getIWMainApplication().getBundle(CasesConstants.IW_BUNDLE_IDENTIFIER);
		iwrb = bundle.getResourceBundle(iwc);

		PresentationUtil.addJavaScriptSourcesLinesToHeader(iwc, Arrays.asList(
				getJQuery().getBundleURIToJQueryLib(),
				getWeb2Business().getBundleUriToHumanizedMessagesScript(),
				getJQuery().getBundleURIToJQueryUILib("1.8.17", "ui.core.js"),
				getJQuery().getBundleURIToJQueryUILib("1.8.17", "ui.widget.js"),
				getJQuery().getBundleURIToJQueryUILib("1.8.17", "ui.mouse.js"),
				getJQuery().getBundleURIToJQueryUILib("1.8.17", "ui.sortable.js"),
				getJQuery().getBundleURIToJQueryPlugin(JQueryPlugin.URL_PARSER),
				bundle.getVirtualPathWithFileNameString(CasesConstants.CASES_LIST_HELPER_JAVA_SCRIPT_FILE),
				CoreConstants.DWR_ENGINE_SCRIPT,
				CoreConstants.DWR_UTIL_SCRIPT,
				"/dwr/interface/CasesEngine.js"
		));

		PresentationUtil.addStyleSheetsToHeader(iwc, Arrays.asList(
				getWeb2Business().getBundleUriToHumanizedMessagesStyleSheet(),
				iwc.getIWMainApplication().getBundle(IWBundleStarter.IW_BUNDLE_IDENTIFIER).getVirtualPathWithFileNameString("style/application.css"),
				bundle.getVirtualPathWithFileNameString("style/case.css"),
				getJQuery().getBundleURIToJQueryUILib("1.8.17/themes/base", "ui.core.css")
		));
	}

	protected Layer getContainer(String styleClass) {
		Layer container = new Layer();
		if (!StringUtil.isEmpty(styleClass)) {
			container.setStyleClass(styleClass);
		}
		return container;
	}

	protected void addHeader() {
		container.add(new Heading1(iwrb.getLocalizedString("search_for_cases", "Search")));
	}

	@Override
	protected void present(IWContext iwc) throws Exception {
		addResources(iwc);

		CasesSearchCriteriaBean searchSettings = null;
		Object o = iwc.getSessionAttribute(GeneralCasesListBuilder.USER_CASES_SEARCH_SETTINGS_ATTRIBUTE);
		if (o instanceof CasesSearchCriteriaBean)
			searchSettings = (CasesSearchCriteriaBean) o;

		container = getContainer("casesSearcherBoxStyleClass");
		add(container);
		addHeader();

		inputsContainer = getContainer("casesSearcherInputsBoxStyleClass");
		container.add(inputsContainer);

		TextInput caseNumber = getTextInput(CaseFinder.PARAMETER_CASE_NUMBER, null, searchSettings == null ? null : searchSettings.getCaseNumber());

		TextInput caseDescription = getTextInput(CaseFinder.PARAMETER_TEXT, null, searchSettings == null ? null : searchSettings.getDescription());

		TextInput name = getTextInput(CaseFinder.PARAMETER_NAME, null, searchSettings == null ? null : searchSettings.getName());

		TextInput personalID = getTextInput(CaseFinder.PARAMETER_PERSONAL_ID, null, searchSettings == null ? null : searchSettings.getPersonalId());

		TextInput contact = getTextInput(PARAMETER_CASE_CONTACT, iwrb.getLocalizedString("cases_search_enter_name_email_or_phone",
				"Contact's name, e-mail or phone number"), searchSettings == null ? null : searchSettings.getContact());

		String showStatisticsLabel = iwrb.getLocalizedString("show_cases_statistics", "Show statistics");
		CheckBox showStatistics = new CheckBox(CaseFinder.PARAMETER_SHOW_STATISTICS);
		showStatistics.setChecked(searchSettings != null && searchSettings.isShowStatistics());
		showStatistics.setTitle(showStatisticsLabel);

		String listType = getListType();
		HiddenInput listTypeInput = new HiddenInput(PARAMETER_CASE_LIST_TYPE, StringUtil.isEmpty(listType) ? CoreConstants.EMPTY : listType);
		inputsContainer.add(listTypeInput);

		//	Case number
		addFormItem(inputsContainer, "caseIdentifier", iwrb.getLocalizedString("case_nr", "Case nr."), caseNumber);

		// Case description
		addFormItem(inputsContainer, "caseDescription", iwrb.getLocalizedString("description", "Description"), caseDescription);

		//	Case name
		addFormItem(inputsContainer, "senderName", iwrb.getLocalizedString("name", "Name"), name);

		//	Case personal id
		addFormItem(inputsContainer, "senderPersonalID", iwrb.getLocalizedString("personal_id", "Personal ID"), personalID);

		//	Case contacts
		addFormItem(inputsContainer, "contact", iwrb.getLocalizedString("contact", "Contact"), contact);

		//	Process
		DropdownMenu processes = getDropdownForProcess(iwc);

		if (this.processProperty == null) {
			addFormItem(inputsContainer, "process", iwrb.getLocalizedString("cases_search_select_process", "Process"), processes);
		} else {
			Layer layer = new Layer();
			layer.setStyleClass("variablesSelectorDropdown");
			inputsContainer.add(layer);
			String action = "CasesListHelper.getProcessDefinitionVariablesByProcessID('"+iwrb.getLocalizedString("loading", "Loading")
					+ "', '"+ this.processProperty +"');";

			if (!CoreUtil.isSingleComponentRenderingProcess(iwc))
				action = "jQuery(window).load(function() {" + action + "});";

			PresentationUtil.addJavaScriptActionOnLoad(iwc, action);
		}

		//	Sorting options
		DropdownMenu sortingOptions = getDropdownForSortingOptions(iwc);
		addFormItem(inputsContainer, "sorting", iwrb.getLocalizedString("cases_search_sorting_optins", "Sorting options"), sortingOptions);

		//	Status
		DropdownMenu statuses = getDropdownForStatus(iwc);
		addFormItem(inputsContainer, "status", iwrb.getLocalizedString("status", "Status"), statuses);

		//	Date range
		Date from = null;
		Date to = null;
		if (searchSettings != null) {
			from = searchSettings.getDateFrom() == null ? null : searchSettings.getDateFrom().getDate();
			to = searchSettings.getDateTo() == null ? null : searchSettings.getDateTo().getDate();
		}
		IWDatePicker dateRange = getDateRange(iwc, "dateRange", from, to);
		addFormItem(inputsContainer, "dateRange", iwrb.getLocalizedString("date_range", "Date range"), dateRange);

		//	Show statistics
		Layer element = getContainer("formItem shortFormItem checkboxFormItem showStatistics");
		inputsContainer.add(element);

		Label label = null;
		label = new Label(showStatisticsLabel, showStatistics);
		element.add(showStatistics);
		element.add(label);

		StringBuilder parameters  = new StringBuilder("['").append(GeneralCasesListBuilder.MAIN_CASES_LIST_CONTAINER_STYLE).append("', '");
		parameters.append(caseNumber.getId()).append("', '").append(name.getId()).append("', '").append(personalID.getId()).append("', '");

		if (this.processProperty == null) {
			parameters.append(processes.getId()).append("', '").append(statuses.getId()).append("', '").append(dateRange.getId()).append("', '");
		} else {
			parameters.append(this.processProperty).append("', '").append(statuses.getId()).append("', '").append(dateRange.getId()).append("', '");
		}

		parameters.append(iwrb.getLocalizedString("searching", "Searching...")).append("', '").append(caseDescription.getId()).append("', '");
		parameters.append(listTypeInput.getId()).append("', '").append(contact.getId()).append("', '").append(CasesConstants.CASES_LIST_GRID_EXPANDER_STYLE_CLASS)
		.append("', '").append(showStatistics.getId()).append("', ").append(isShowAllStatuses()).append("]");
		addCasesFilterButtons(iwc, parameters.toString());
	}

	protected String getSearchAction(String jsParams) {
		return "searchForCases(".concat(jsParams).concat(");");
	}

	protected String getClearAction(String jsParams) {
		return "clearSearchForCases(".concat(jsParams).concat(");");
	}

	protected void addCasesFilterButtons(IWContext iwc, String jsParams) {
		inputsContainer.add(new CSSSpacer());

		Layer buttonsContainer = getContainer("buttonLayer");
		inputsContainer.add(buttonsContainer);

		jsParams = jsParams == null ? CoreConstants.EMPTY : jsParams;
		StringBuilder action = new StringBuilder("registerCasesSearcherBoxActions('").append(inputsContainer.getId()).append("', ")
			.append(StringUtil.isEmpty(jsParams) ? "null" : jsParams).append(");");
		if (!CoreUtil.isSingleComponentRenderingProcess(iwc)) {
			action = new StringBuilder("jQuery(window).load(function() {").append(action.toString()).append("});");
		}
		PresentationUtil.addJavaScriptActionToBody(iwc, action.toString());

		GenericButton searchButton = new GenericButton(iwrb.getLocalizedString("search_for_cases", "Search"));
		searchButton.setTitle(iwrb.getLocalizedString("search_for_cases_by_selected_parameters", "Search for cases by selected parameters"));
		searchButton.setStyleClass(buttonStyleClass);
		searchButton.setStyleClass("seachForCasesButton");
		searchButton.setOnClick(getSearchAction(jsParams));
		buttonsContainer.add(searchButton);

		GenericButton clearSearch = new GenericButton(iwrb.getLocalizedString("clear_search_results", "Clear"));
		clearSearch.setTitle(iwrb.getLocalizedString("clear_all_search_results", "Clear search resutls"));
		clearSearch.setOnClick(getClearAction(jsParams));
		clearSearch.setStyleClass(buttonStyleClass);
		buttonsContainer.add(clearSearch);

		if (isShowExportButton()) {
			GenericButton export = new GenericButton(iwrb.getLocalizedString("export_search_results", "Export"));
			export.setTitle(iwrb.getLocalizedString("export_search_results_to_excel", "Export search results to Excel"));
			export.setOnClick(new StringBuilder("CasesListHelper.exportSearchResults('").append(iwrb.getLocalizedString("exporting", "Exporting..."))
																						.append("');").toString());
			export.setStyleClass(buttonStyleClass);
			buttonsContainer.add(export);
		}
	}

	protected TextInput getTextInput(String name, String toolTip) {
		return getTextInput(name, toolTip, null);
	}

	protected TextInput getTextInput(String name, String toolTip, String value) {
		TextInput input = new TextInput(name);
		input.setStyleClass(textInputStyleClass);

		if (!StringUtil.isEmpty(toolTip))
			input.setTitle(toolTip);
		if (!StringUtil.isEmpty(value))
			input.setValue(value);

		return input;
	}

	protected void fillDropdown(Locale locale, DropdownMenu menu, List<AdvancedProperty> options, AdvancedProperty firstElement, String selectedElement) {
		if (locale == null) {
			locale = Locale.ENGLISH;
		}
		Collections.sort(options, new AdvancedPropertyComparator(locale));

		for (AdvancedProperty option: options) {
			menu.addOption(new SelectOption(option.getValue(), option.getId()));
		}
		if (firstElement != null) {
			menu.addFirstOption(new SelectOption(firstElement.getValue(), firstElement.getId()));
		}

		if (selectedElement != null) {
			menu.setSelectedElement(selectedElement);
		}

		if (ListUtil.isEmpty(options)) {
			menu.setDisabled(true);
		}
	}

	private DropdownMenu getDropdownForSortingOptions(IWContext iwc) {
		DropdownMenu sortingOptions = new DropdownMenu(PARAMETER_SORTING_OPTIONS);
		sortingOptions.setTitle(getResourceBundle().getLocalizedString("cases_searcher_default_sorting_is_by_date", "By default sorting by case's creation date"));
		sortingOptions.setStyleClass("casesSearcherResultsSortingOptionsChooserStyle");

		List<AdvancedProperty> defaultOptions = getCasesEngine().getDefaultSortingOptions(iwc);
		if (ListUtil.isEmpty(defaultOptions)) {
			sortingOptions.addFirstOption(new SelectOption(getResourceBundle().getLocalizedString("cases_searcher_there_are_no_options", "There are no options"),
					String.valueOf(-1)));
			sortingOptions.setDisabled(true);

			return sortingOptions;
		}

		for (AdvancedProperty sortingOption: defaultOptions) {
			SelectOption option = new SelectOption(sortingOption.getValue(), sortingOption.getId());
			option.setStyleClass("defaultCasesSearcherSortingOption");
			sortingOptions.add(option);
		}
		sortingOptions.addFirstOption(new SelectOption(getResourceBundle().getLocalizedString("cases_searcher_select_sorting_option", "Select option"),
				String.valueOf(-1)));

		sortingOptions.setOnChange(new StringBuilder("CasesListHelper.addSelectedSearchResultsSortingOption('").append(sortingOptions.getId()).append("');")
				.toString());

		return sortingOptions;
	}

	private DropdownMenu getDropdownForProcess(IWContext iwc) {
		DropdownMenu menu = new DropdownMenu(PARAMETER_PROCESS_ID);
		menu.setStyleClass("availableVariablesChooserForProcess");
		String selectedProcess = iwc.isParameterSet(PARAMETER_PROCESS_ID) ? iwc.getParameter(PARAMETER_PROCESS_ID) : null;

		List<AdvancedProperty> allProcesses = getCasesEngine().getAvailableProcesses(iwc);

		if (ListUtil.isEmpty(allProcesses)) {
			return menu;
		}

		IWResourceBundle iwrb = getResourceBundle();

		allProcesses.add(0, new AdvancedProperty(CasesConstants.GENERAL_CASES_TYPE, iwrb.getLocalizedString("general_cases", "General cases")));
		Collections.sort(allProcesses, new AdvancedPropertyComparator(iwc.getCurrentLocale()));

		fillDropdown(iwc.getCurrentLocale(), menu, allProcesses, new AdvancedProperty(String.valueOf(-1),
				iwrb.getLocalizedString("cases_search_select_process", "Select process")), selectedProcess);

		menu.setOnChange(new StringBuilder("CasesListHelper.getProcessDefinitionVariablesByIwID('").append(iwrb.getLocalizedString("loading", "Loading..."))
							.append("', '").append(menu.getId()).append("');").toString());

		return menu;
	}

	@SuppressWarnings("unchecked")
	protected DropdownMenu getDropdownForStatus(IWContext iwc) {
		DropdownMenu menu = new DropdownMenu(PARAMETER_CASE_STATUS);
		String selectedStatus = iwc.isParameterSet(PARAMETER_CASE_STATUS) ? iwc.getParameter(PARAMETER_CASE_STATUS) : null;

		CaseBusiness caseBusiness = getCasesBusiness(iwc);
		if (caseBusiness == null) {
			logWarning("CaseBusiness (" + CaseBusiness.class.getName() + ") is unavailable!");
			menu.setDisabled(true);
			return menu;
		}

		Collection<CaseStatus> allStatuses = null;
		try {
			allStatuses = caseBusiness.getCaseStatuses();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		if (ListUtil.isEmpty(allStatuses)) {
			logWarning("There are no statuses available");
			menu.setDisabled(true);
			return menu;
		}

		Locale l = iwc.getCurrentLocale();
		if (l == null) {
			l = Locale.ENGLISH;
		}

		boolean addStatus = true;
		String localizedStatus = null;
		List<AdvancedProperty> statuses = new ArrayList<AdvancedProperty>();
		for (CaseStatus status: allStatuses) {
			addStatus = true;

			try {
				localizedStatus = caseBusiness.getLocalizedCaseStatusDescription(null, status, l);
				if (!showAllStatuses && localizedStatus.equals(status.getStatus())) {
					addStatus = false;
				}

				if (this.getCaseStatusesToShow() != null) {
					if (this.getCaseStatusesToShow().indexOf(status.getStatus()) != -1) {
						addStatus = true;
					}
					else if (!showAllStatuses) {
						addStatus = false;
					}
				}

				if (this.getCaseStatusesToHide() != null) {
					if (this.getCaseStatusesToHide().indexOf(status.getStatus()) != -1) {
						addStatus = false;
					}
					else if (showAllStatuses) {
						addStatus = true;
					}
				}

				if (addStatus) {
					statuses.add(new AdvancedProperty(status.getStatus(), localizedStatus));
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}

		fillDropdown(l, menu, statuses, new AdvancedProperty(String.valueOf(-1), getResourceBundle(iwc).getLocalizedString("select_status", "Select status")),
				selectedStatus);

		return menu;
	}

	protected IWDatePicker getDateRange(IWContext iwc, String name, Date from, Date to) {
		IWDatePicker datePicker = new IWDatePicker(name);

		if (from != null)
			datePicker.setDate(from);
		if (to != null)
			datePicker.setDateTo(to);
		datePicker.setDateRange(true);
		datePicker.setUseCurrentDateIfNotSet(false);

		return datePicker;
	}

	protected Layer addFormItem(Layer layer, String localizedLabelText, InterfaceObject input) {
		return addFormItem(layer, null, localizedLabelText, input);
	}

	protected Layer addFormItem(Layer layer, String styleClass, String localizedLabelText, InterfaceObject input) {
		return addFormItem(layer, styleClass, localizedLabelText, input, null);
	}

	protected Layer addFormItem(Layer layer, String localizedLabelText, InterfaceObject input, List<UIComponent> additionalComponents) {
		return addFormItem(layer, null, localizedLabelText, input, additionalComponents);
	}

	protected Layer addFormItem(Layer layer, String styleClass, String localizedLabelText, InterfaceObject input, List<UIComponent> additionalComponents) {
		Layer element = new Layer(Layer.DIV);
		layer.add(element);
		element.setStyleClass("formItem shortFormItem");

		Label label = null;
		label = new Label(localizedLabelText == null ? CoreConstants.MINUS : localizedLabelText, input);
		element.add(label);
		element.add(input);

		if (!ListUtil.isEmpty(additionalComponents)) {
			for (UIComponent component: additionalComponents) {
				element.add(component);
			}
		}
		return element;
	}

	public String getListType() {
		return listType;
	}

	public void setListType(String listType) {
		this.listType = listType;
	}

	public boolean isShowAllStatuses() {
		return showAllStatuses;
	}

	public void setShowAllStatuses(boolean showAllStatuses) {
		this.showAllStatuses = showAllStatuses;
	}

	public boolean isShowExportButton() {
		return showExportButton;
	}

	public void setShowExportButton(boolean showExportButton) {
		this.showExportButton = showExportButton;
	}

	private CasesEngine getCasesEngine() {
		if (casesEngine == null)
			ELUtil.getInstance().autowire(this);
		return casesEngine;
	}

	public void setCasesEngine(CasesEngine casesEngine) {
		this.casesEngine = casesEngine;
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

	/**
	 * @return the processProperty
	 */
	public String getProcessProperty() {
		return processProperty;
	}

	/**
	 * @param processProperty the processProperty to set
	 */
	public void setProcessProperty(String processProperty) {
		this.processProperty = processProperty;
	}
}