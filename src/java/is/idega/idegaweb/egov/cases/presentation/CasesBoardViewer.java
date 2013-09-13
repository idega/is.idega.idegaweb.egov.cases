package is.idega.idegaweb.egov.cases.presentation;

import is.idega.idegaweb.egov.cases.business.BoardCasesManager;
import is.idega.idegaweb.egov.cases.media.CasesBoardViewerExporter;
import is.idega.idegaweb.egov.cases.presentation.beans.CaseBoardTableBean;
import is.idega.idegaweb.egov.cases.presentation.beans.CaseBoardTableBodyRowBean;
import is.idega.idegaweb.egov.cases.util.CasesConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.idega.block.process.business.CaseBusiness;
import com.idega.block.process.business.ProcessConstants;
import com.idega.block.web2.business.JQuery;
import com.idega.block.web2.business.JQueryPlugin;
import com.idega.block.web2.business.Web2Business;
import com.idega.builder.bean.AdvancedProperty;
import com.idega.builder.business.BuilderLogicWrapper;
import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.core.idgenerator.business.UUIDGenerator;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWMainApplication;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.io.MediaWritable;
import com.idega.presentation.CSSSpacer;
import com.idega.presentation.IWBaseComponent;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.Table2;
import com.idega.presentation.TableBodyRowGroup;
import com.idega.presentation.TableCell2;
import com.idega.presentation.TableFooterRowGroup;
import com.idega.presentation.TableHeaderCell;
import com.idega.presentation.TableHeaderRowGroup;
import com.idega.presentation.TableRow;
import com.idega.presentation.text.Heading3;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.GenericButton;
import com.idega.presentation.ui.HiddenInput;
import com.idega.presentation.ui.PrintButton;
import com.idega.user.data.User;
import com.idega.util.CoreConstants;
import com.idega.util.CoreUtil;
import com.idega.util.ListUtil;
import com.idega.util.PresentationUtil;
import com.idega.util.StringUtil;
import com.idega.util.URIUtil;
import com.idega.util.expression.ELUtil;

public class CasesBoardViewer extends IWBaseComponent {

	private static final Logger LOGGER = Logger.getLogger(CasesBoardViewer.class.getName());

	public static final String	PARAMETER_PROCESS_NAME = "prmProcessName",
								PARAMETER_UUID = "uuid",
								PARAMETER_CUSTOM_COLUMNS = "customCasesBoardViewerColumns";

	private static final String EDITABLE_FIELD_TYPE_TEXT_INPUT = "textinput",
								EDITABLE_FIELD_TYPE_TEXT_AREA = "textarea",
								EDITABLE_FIELD_TYPE_DROPDOWN = "select";


	public static final String	WORK_ITEM = "work_item",
								ESTIMATED_COST = "estimated_cost",
								BOARD_SUGGESTION = ProcessConstants.BOARD_FINANCING_SUGGESTION,
								BOARD_DECISION = ProcessConstants.BOARD_FINANCING_DECISION,
								BOARD_PROPOSAL_FOR_GRANT = "proposal_for_grant";

	public static final List<AdvancedProperty> CASE_FIELDS = Collections.unmodifiableList(Arrays.asList(
		new AdvancedProperty("string_ownerFullName", "Applicant"),									//	0
		new AdvancedProperty("string_ownerKennitala", "Personal ID"),								//	1
		new AdvancedProperty("string_ownerAddress", "Address"),										//	2
		new AdvancedProperty("string_ownerPostCode", "Zip"),										//	3
		new AdvancedProperty("string_ownerMunicipality", "Municipality"),							//	4
		new AdvancedProperty(ProcessConstants.CASE_IDENTIFIER, "Case nr."),							//	5
		new AdvancedProperty(ProcessConstants.CASE_DESCRIPTION, "Description"),						//	6

		new AdvancedProperty("string_ownerTotalCost", "Total cost"),								//	7
		new AdvancedProperty(CasesConstants.APPLIED_GRANT_AMOUNT_VARIABLE, "Applied amount"),		//	8

		new AdvancedProperty("string_ownerBusinessConcept", "In a nutshell"),						//	9

		new AdvancedProperty("sum_all_negative_grades", "Negative grade"),							//	10
		new AdvancedProperty("sum_all_grades", "Grade"),											//	11

		new AdvancedProperty("string_ownerProjectLead", "Category"),								//	12,	EDITABLE, select

		new AdvancedProperty(ProcessConstants.FINANCING_OF_THE_TASKS, "Financing of the tasks"),	//	13, table of 4 columns

		new AdvancedProperty("string_ownerGrade", "Comment"),										//	14
		new AdvancedProperty("string_ownerAnswer", "Restrictions")									//	15, EDITABLE, text area
	));

	private static Map<String, String> EDITABLE_FIELDS;
	static {
		EDITABLE_FIELDS = new HashMap<String, String>();
		EDITABLE_FIELDS.put(CASE_FIELDS.get(12).getId(), EDITABLE_FIELD_TYPE_DROPDOWN);

		EDITABLE_FIELDS.put(ProcessConstants.BOARD_FINANCING_SUGGESTION, EDITABLE_FIELD_TYPE_TEXT_INPUT);
		EDITABLE_FIELDS.put(ProcessConstants.BOARD_FINANCING_DECISION, EDITABLE_FIELD_TYPE_TEXT_INPUT);

		EDITABLE_FIELDS.put(CASE_FIELDS.get(15).getId(), EDITABLE_FIELD_TYPE_TEXT_AREA);
	}

	public static final String CASES_BOARD_VIEWER_CASES_STATUS_PARAMETER = "casesBoardViewerCasesStatusParameter";
	public static final String CASES_BOARD_VIEWER_PROCESS_NAME_PARAMETER = "casesBoardViewerProcessNameParameter";
	public static final String CASES_BOARD_VIEWER_REQUIRED_COLUMNS = "casesBoardViewerRequiredColumns";

	@Autowired
	@Qualifier(BoardCasesManager.BEAN_NAME)
	private BoardCasesManager boardCasesManager;

	@Autowired
	private Web2Business web2;

	@Autowired
	private JQuery jQuery;

	@Autowired
	private BuilderLogicWrapper builderLogicWrapper;

	protected String	caseStatus,
					roleKey,
					processName,
					taskName = "Grading",
					currentPageUri,
					uuid;

	private boolean useCurrentPageAsBackPageFromTaskViewer = Boolean.TRUE;

	@Override
	protected void initializeComponent(FacesContext context) {
		
//		TODO: this is for testing with default theme
//		Layer style = new Layer();
//		add(style);
//		style.add("<style>div{overflow:auto;}</style>");
///////////////////////////////////////////////////////////
		
		IWContext iwc = IWContext.getIWContext(context);

		if (!iwc.isLoggedOn()) {
			LOGGER.warning("User must be logged to see cases!");
			return;
		}
		if (!StringUtil.isEmpty(roleKey) && !iwc.hasRole(roleKey)) {
			LOGGER.warning("User must have role '" + roleKey + "' to see cases!");
			return;
		}

		uuid = getBuilderLogicWrapper().getBuilderService(iwc).getInstanceId(this);
		if (StringUtil.isEmpty(uuid))
			uuid = CoreConstants.MINUS;

		IWBundle bundle = getBundle(context, CasesConstants.IW_BUNDLE_IDENTIFIER);
		IWResourceBundle iwrb = bundle.getResourceBundle(iwc);

		PresentationUtil.addJavaScriptSourcesLinesToHeader(iwc, Arrays.asList(
			jQuery.getBundleURIToJQueryLib(),
			web2.getBundleUriToHumanizedMessagesScript(),
			jQuery.getBundleURIToJQueryPlugin(JQueryPlugin.EDITABLE),
			jQuery.getBundleURIToJQueryPlugin(JQueryPlugin.TABLE_SORTER),
			CoreConstants.DWR_ENGINE_SCRIPT,
			CoreConstants.DWR_UTIL_SCRIPT,
			"/dwr/interface/BoardCasesManager.js",
			bundle.getVirtualPathWithFileNameString("javascript/CasesBoardHelper.js")
		));
		PresentationUtil.addJavaScriptSourcesLinesToHeader(iwc, web2.getBundleURIsToFancyBoxScriptFiles());
		PresentationUtil.addStyleSheetsToHeader(iwc, Arrays.asList(
			web2.getBundleUriToHumanizedMessagesStyleSheet(),
			bundle.getVirtualPathWithFileNameString("style/case.css"),
			web2.getBundleURIToFancyBoxStyleFile()
		));

		Layer container = new Layer();
		getChildren().add(container);
		container.setStyleClass("casesBoardViewerContainer");

		if (!addCasesTable(container, iwc, iwrb)) {
			return;
		}

		container.add(new CSSSpacer());

		addButtons(container, iwc, iwrb);

		String initAction = new StringBuilder("CasesBoardHelper.initializeBoardCases({savingMessage: '")
			.append(iwrb.getLocalizedString("case_board_viewer.saving_case_variable", "Saving...")).append("', remove: '")
			.append(iwrb.getLocalizedString("case_board_viewer.remove_value", "Remove")).append("', edit: '")
			.append(iwrb.getLocalizedString("case_board_viewer.edit_value", "Edit")).append("', loading: '")
			.append(iwrb.getLocalizedString("case_board_viewer.loading", "Loading...")).append("', enterNumericValue: '")
			.append(iwrb.getLocalizedString("case_board_viewer.enter_numeric_value", "Invalid value! Make sure entered value is numeric only."))
			.append("', errorSaving: '").append(iwrb.getLocalizedString("case_board_viewer.error_saving_value", "Error occurred while saving!"))
			.append("'});").toString();
		if (!CoreUtil.isSingleComponentRenderingProcess(iwc)) {
			initAction = new StringBuilder("jQuery(document).ready(function() {").append(initAction).append("});").toString();
		}
		PresentationUtil.addJavaScriptActionToBody(iwc, initAction);
	}

	private boolean addCasesTable(Layer container, IWContext iwc, IWResourceBundle iwrb) {
		CaseBoardTableBean data = getBoardCasesManager().getTableData(iwc, 
				getCaseStatuses(), processName, uuid);

		if (data == null || !data.isFilledWithData()) {
			getChildren().add(new Heading3(data.getErrorMessage()));
			return false;
		}

		Table2 table = new Table2();
		container.add(table);
		table.setStyleClass("casesBoardViewerTable");

		TableHeaderRowGroup headerGroup = table.createHeaderRowGroup();
		TableRow headerRow = headerGroup.createRow();
		Map<Integer, List<AdvancedProperty>> headers = data.getHeaderLabels();
		ArrayList<String> ids = new ArrayList<String>(headers.size());
		for (Integer key: headers.keySet()) {
			List<AdvancedProperty> headerLabels = headers.get(key);
			for (AdvancedProperty header: headerLabels) {
				TableHeaderCell headerCell = headerRow.createHeaderCell();
				headerCell.add(new Text(header.getValue()));
				headerCell.setStyleClass(header.getId());
				ids.add(header.getId());
				if (getBoardCasesManager().isColumnOfDomain(header.getId(), CASE_FIELDS.get(9).getId()) ||
					getBoardCasesManager().isColumnOfDomain(header.getId(), CASE_FIELDS.get(14).getId()))
					headerCell.setStyleClass("casesBoardViewerTableWiderCell");
				
			}
		}

		int rowsIndex = 0;
		Link linkToTask = null;
		TableBodyRowGroup body = table.createBodyRowGroup();
		body.setStyleClass("casesBoardViewerBodyRows");
		for (CaseBoardTableBodyRowBean rowBean: data.getBodyBeans()) {
			TableRow row = body.createRow();
			row.setId(rowBean.getId());
			row.setStyleClass(rowsIndex % 2 == 0 ? "even" : "odd");

			Map<Integer, List<AdvancedProperty>> values = rowBean.getValues();
			for (Integer key: values.keySet()) {
				List<AdvancedProperty> entries = values.get(key);
				if (ListUtil.isEmpty(entries))
					continue;

				if (ProcessConstants.FINANCING_OF_THE_TASKS.equals(entries.get(0).getId())) {
					//	Financing table
					List<Map<String, String>> financingInfo = rowBean.getFinancingInfo();
					if (ListUtil.isEmpty(financingInfo)) {
						financingInfo = new ArrayList<Map<String,String>>();
						Map<String, String> emptyValues = new HashMap<String, String>();
						emptyValues.put(WORK_ITEM, CoreConstants.MINUS);
						emptyValues.put(ESTIMATED_COST, CoreConstants.MINUS);
						emptyValues.put(BOARD_SUGGESTION, CoreConstants.MINUS);
						emptyValues.put(BOARD_DECISION, CoreConstants.MINUS);
						emptyValues.put(BOARD_PROPOSAL_FOR_GRANT, CoreConstants.MINUS);
						financingInfo.add(emptyValues);
						rowBean.setFinancingInfo(financingInfo);
					}

					int index = 0;
					long estimationTotal = 0;
					long suggestionTotal = 0;
					long proposalTotal = 0;
					long decisionTotal = 0;
					TableRow financingTableRow = row;
					List<TableCell2> suggestionCells = new ArrayList<TableCell2>();
					List<TableCell2> decisionCells = new ArrayList<TableCell2>();
					for (Iterator<Map<String, String>> infoIter = financingInfo.iterator(); infoIter.hasNext();) {
						Map<String, String> info = infoIter.next();

						TableCell2 cell = financingTableRow.createCell();
						cell.add(new Text(info.get(WORK_ITEM)));

						cell = financingTableRow.createCell();
						String estimation = info.get(ESTIMATED_COST);
						estimationTotal += getBoardCasesManager().getNumberValue(estimation);
						cell.add(new Text(estimation));
						
						cell = financingTableRow.createCell();
						String proposal = info.get(BOARD_PROPOSAL_FOR_GRANT);
						proposalTotal += getBoardCasesManager().getNumberValue(proposal);
						cell.add(new Text(proposal));

						TableCell2 suggestionCell = financingTableRow.createCell();
						String suggestion = info.get(BOARD_SUGGESTION);
						long sugg = getBoardCasesManager().getNumberValue(suggestion);
						suggestionTotal += sugg;
						suggestionCell.add(new Text(String.valueOf(sugg)));
						
						if (!getCaseBusiness().isCaseClosed(rowBean.getCaseIdentifier())) {
							makeCellEditable(suggestionCell, EDITABLE_FIELD_TYPE_TEXT_INPUT);
						}
						
						suggestionCell.setStyleClass(BOARD_SUGGESTION);
						suggestionCell.setMarkupAttribute("task_index", index);
						suggestionCell.setMarkupAttribute("total_values", financingInfo.size());
						suggestionCells.add(suggestionCell);

						TableCell2 decisionCell = financingTableRow.createCell();
						String decision = info.get(BOARD_DECISION);
						long dec = getBoardCasesManager().getNumberValue(decision);
						decisionTotal += dec;
						decisionCell.add(new Text(String.valueOf(dec)));
						
						if (!getCaseBusiness().isCaseClosed(rowBean.getCaseIdentifier())) {
							makeCellEditable(decisionCell, EDITABLE_FIELD_TYPE_TEXT_INPUT);
						}
						
						decisionCell.setStyleClass(BOARD_DECISION);
						decisionCell.setMarkupAttribute("task_index", index);
						decisionCell.setMarkupAttribute("total_values", financingInfo.size());
						decisionCells.add(decisionCell);

						if (infoIter.hasNext()) {
							financingTableRow = body.createRow();
							financingTableRow.setStyleClass("childRow");
							financingTableRow.setId(rowBean.getId().concat(CoreConstants.UNDER).concat(UUIDGenerator.getInstance().generateId()));
						}
						index++;
					}

					//	Totals
					financingTableRow = body.createRow();
					financingTableRow.setStyleClass("childRow");
					for (int i = 0; i < 4; i++)
						financingTableRow.createCell().add(new Text(CoreConstants.SPACE));

					financingTableRow = body.createRow();
					financingTableRow.setStyleClass("childRow");

					financingTableRow.createCell().add(new Text(iwrb.getLocalizedString("total", "Total")));
					financingTableRow.createCell().add(new Text(String.valueOf(estimationTotal)));

					TableCell2 localProposalTotalCell = financingTableRow.createCell();
					localProposalTotalCell.add(new Text(String.valueOf(proposalTotal)));
					String proposalTotalCellId  = localProposalTotalCell.getId();
					for (TableCell2 suggestionCell: suggestionCells)
						suggestionCell.setMarkupAttribute("local_total", proposalTotalCellId);
					
					TableCell2 localSuggestionTotalCell = financingTableRow.createCell();
					localSuggestionTotalCell.add(new Text(String.valueOf(suggestionTotal)));
					String suggestionTotalCellId  = localSuggestionTotalCell.getId();
					for (TableCell2 suggestionCell: suggestionCells)
						suggestionCell.setMarkupAttribute("local_total", suggestionTotalCellId);

					TableCell2 localDecisionTotalCell = financingTableRow.createCell();
					localDecisionTotalCell.add(new Text(String.valueOf(decisionTotal)));
					String decisionTotalCellId = localDecisionTotalCell.getId();
					for (TableCell2 decisionCell: decisionCells)
						decisionCell.setMarkupAttribute("local_total", decisionTotalCellId);

				//	"Simple" values
				} else {
					TableCell2 bodyRowCell = row.createCell();
					List<Map<String, String>> financingInfo = rowBean.getFinancingInfo();
					int rowSpan = getRowSpan(financingInfo);
					bodyRowCell.setRowSpan(rowSpan);

					for (AdvancedProperty entry: entries) {
						if (getBoardCasesManager().isColumnOfDomain(entry.getId(), CASE_FIELDS.get(5).getId())) {
							//	Link to grading task
							linkToTask = new Link(rowBean.getCaseIdentifier(), getLinkToTheTask(iwc, rowBean));
							linkToTask.setStyleClass("casesBoardViewerTableLinkToTaskStyle");
							linkToTask.getId();
							bodyRowCell.add(linkToTask);
						} else if (getBoardCasesManager().isColumnOfDomain(entry.getId(), ProcessConstants.HANDLER_IDENTIFIER)) {
							//	E-mail link to handler
							bodyRowCell.add(getHandlerInfo(iwc, rowBean.getHandler()));
						} else if (!StringUtil.isEmpty(entry.getValue())) {
							bodyRowCell.setStyleClass(entry.getId());
							bodyRowCell.add(new Text(entry.getValue()));
						} else {
							bodyRowCell.add(new Text(CoreConstants.MINUS));
						}

						//	Editable fields
						String editableType = EDITABLE_FIELDS.get(entry.getId());
						if (!StringUtil.isEmpty(editableType) && !getCaseBusiness().isCaseClosed(rowBean.getCaseIdentifier()))
							makeCellEditable(bodyRowCell, editableType);
					}
				}
			}

			rowsIndex++;
		}

		int index = 0;
		String boardSuggestionTotalCellId = null;
		String boardDecisionTotalCellId = null;
		TableFooterRowGroup footer = table.createFooterRowGroup();
		TableRow footerRow = footer.createRow();
		int totalBoardSuggestionCellIndex = getBoardCasesManager().getIndexOfColumn(ProcessConstants.BOARD_FINANCING_SUGGESTION, uuid) - 1;
		int totalBoardDecisionCellIndex = getBoardCasesManager().getIndexOfColumn(ProcessConstants.BOARD_FINANCING_DECISION, uuid) - 1;
		for (String footerLabel: data.getFooterValues()) {
			TableCell2 footerCell = footerRow.createCell();
			footerCell.add(new Text(footerLabel));
			footerCell.setStyleClass(ids.get(index));

			if (totalBoardDecisionCellIndex == index)
				boardDecisionTotalCellId = footerCell.getId();
			else if (totalBoardSuggestionCellIndex == index)
				boardSuggestionTotalCellId = footerCell.getId();

			index++;
		}

		container.add(new HiddenInput(new StringBuilder("casesBoardViewerTableEditableCell").append(EDITABLE_FIELD_TYPE_DROPDOWN)
				.append("VariableName").toString(), CASE_FIELDS.get(12).getId()));

		container.add(new HiddenInput(new StringBuilder("casesBoardViewerTableEditableCell").append(EDITABLE_FIELD_TYPE_TEXT_INPUT)
				.append(ProcessConstants.BOARD_FINANCING_SUGGESTION).toString(), ProcessConstants.BOARD_FINANCING_SUGGESTION));
		container.add(new HiddenInput(new StringBuilder("casesBoardViewerTableEditableCell").append(EDITABLE_FIELD_TYPE_TEXT_INPUT)
				.append(ProcessConstants.BOARD_FINANCING_DECISION).toString(),	ProcessConstants.BOARD_FINANCING_DECISION));

		container.add(new HiddenInput(new StringBuilder("casesBoardViewerTableEditableCell").append(EDITABLE_FIELD_TYPE_TEXT_AREA)
				.append("VariableName").toString(), CASE_FIELDS.get(15).getId()));

		container.add(new HiddenInput("casesBoardViewerTableRoleKey", StringUtil.isEmpty(roleKey) ? CoreConstants.EMPTY : roleKey));

		container.add(new HiddenInput("casesBoardViewerTableUniqueIdKey", getBuilderLogicWrapper().getBuilderService(iwc).getInstanceId(this)));
		container.add(new HiddenInput("casesBoardViewerTableContainerKey", container.getId()));
		container.add(new HiddenInput("casesBoardViewerTableTotalBoardSuggestionCellIdKey", boardSuggestionTotalCellId));
		container.add(new HiddenInput("casesBoardViewerTableTotalBoardAmountCellIdKey", boardDecisionTotalCellId));
		if (useCurrentPageAsBackPageFromTaskViewer)
			container.add(new HiddenInput("casesBoardViewerTableSpecialBackPageFromTaskViewer", getCurrentPageUri(iwc)));

		String initAction = new StringBuilder("jQuery('#").append(table.getId()).append("').tablesorter({cssChildRow: 'childRow'});").toString();
		if (!CoreUtil.isSingleComponentRenderingProcess(iwc))
			initAction = new StringBuilder("jQuery(window).load(function() {").append(initAction).append("});").toString();
		PresentationUtil.addJavaScriptActionToBody(iwc, initAction);

		return true;
	}

	protected int getRowSpan(List<Map<String, String>> financingInfo) {
		if (ListUtil.isEmpty(financingInfo)) {
			return 1;
		}
		
		return ListUtil.isEmpty(financingInfo) ? 3 : (financingInfo.size() + 2);
	}

	private UIComponent getHandlerInfo(IWContext iwc, User handler) {
		AdvancedProperty info = null;
		try {
			info = getBoardCasesManager().getHandlerInfo(iwc, handler);
		} catch(Exception e) {
			LOGGER.log(Level.WARNING, "Error getting handler info for user: " + handler);
		}

		if (info == null) {
			return new Text(CoreConstants.EMPTY);
		}

		if (StringUtil.isEmpty(info.getValue())) {
			return new Text(info.getId());
		}

		Link mailTo = new Link(info.getId(), info.getValue());
		mailTo.setSessionId(false);
		return mailTo;
	}

	private String getLinkToTheTask(IWContext iwc, CaseBoardTableBodyRowBean rowBean) {
		String uri = null;
		try {
			String basePage = getCurrentPageUri(iwc);
			uri = getBoardCasesManager().getLinkToTheTaskRedirector(iwc, basePage, rowBean.getCaseId(), rowBean.getProcessInstanceId(),
					useCurrentPageAsBackPageFromTaskViewer ? basePage : null, getTaskName());
		} catch(Exception e) {
			LOGGER.log(Level.WARNING, "Error getting uri to the task for process: " + rowBean.getProcessInstanceId());
		}
		return StringUtil.isEmpty(uri) ? iwc.getRequestURI() : uri;
	}
		
	protected CaseBusiness getCaseBusiness() {
		try {
			return IBOLookup.getServiceInstance(CoreUtil.getIWContext(), CaseBusiness.class);
		} catch (IBOLookupException e) {
			LOGGER.log(Level.WARNING, "Unable to get " + CaseBusiness.class + 
					" bean.", e);
		}
		
		return null;
		
	}
	
	private void makeCellEditable(TableCell2 cell, String type) {
		cell.setStyleClass(new StringBuilder("casesBoardViewerTableEditableCell").append(type).toString());
	}

	protected void addButtons(Layer container, IWContext iwc, IWResourceBundle iwrb) {
		Layer buttonsContainer = new Layer();
		container.add(buttonsContainer);
		buttonsContainer.setStyleClass("casesBoardViewerButtonsContainer");

		//	Export to excel
		GenericButton exportToExcel = new GenericButton(iwrb.getLocalizedString("cases_board_viewer.export_cases_list_to_excel", "Export to Excel"));
		buttonsContainer.add(exportToExcel);
		exportToExcel.setOnClick(new StringBuilder("humanMsg.displayMsg('")
			.append(iwrb.getLocalizedString("cases_board_viewer.exporting_cases_list_to_excel",	"Exporting to Excel"))
			.append("');window.location.href='").append(getUriToExcelExporter(iwc)).append("';").toString());

		//	Print
		PrintButton printList = new PrintButton(iwrb.getLocalizedString("cases_board_viewer.print_list", "Print"));
		buttonsContainer.add(printList);

		//	Customize
		GenericButton customize = new GenericButton(iwrb.getLocalizedString("cases_board_viewer.customize_columns", "Customize"));
		buttonsContainer.add(customize);

		Link customizeWindow = new Link(CasesBoardViewCustomizer.class);
		customizeWindow.setStyleClass("casesBoardViewCustomizer");
		customizeWindow.setStyleAttribute("display", "none");
		customizeWindow.setMarkupAttribute("data-fancybox-type", "ajax");
		buttonsContainer.add(customizeWindow);

		String link = getBuilderLogicWrapper().getBuilderService(iwc).getUriToObject(CasesBoardViewCustomizer.class, Arrays.asList(
				new AdvancedProperty(PARAMETER_PROCESS_NAME, processName),
				new AdvancedProperty(PARAMETER_UUID, uuid)
		));
		customize.setOnClick("CasesBoardHelper.openCustomizeWindow('" + customizeWindow.getId() + "', '" + link + "');");
	}

	protected String getUriToExcelExporter(IWContext iwc) {
		URIUtil uri = new URIUtil(iwc.getIWMainApplication().getMediaServletURI());

		uri.setParameter(MediaWritable.PRM_WRITABLE_CLASS, IWMainApplication.getEncryptedClassName(CasesBoardViewerExporter.class));

		if (!StringUtil.isEmpty(getCaseStatus()))
			uri.setParameter(CASES_BOARD_VIEWER_CASES_STATUS_PARAMETER, getCaseStatus());
		if (!StringUtil.isEmpty(processName))
			uri.setParameter(CASES_BOARD_VIEWER_PROCESS_NAME_PARAMETER, processName);
		uri.setParameter(PARAMETER_UUID, uuid);

		return uri.getUri();
	}

	private String getCurrentPageUri(IWContext iwc) {
		if (StringUtil.isEmpty(currentPageUri)) {
			if (CoreUtil.isSingleComponentRenderingProcess(iwc)) {
				try {
					currentPageUri = getBuilderLogicWrapper().getBuilderService(iwc).getCurrentPageURI(iwc);
				} catch(Exception e) {
					LOGGER.log(Level.WARNING, "Error getting current page's uri!", e);
				}
			}
			if (StringUtil.isEmpty(currentPageUri)) {
				currentPageUri = iwc.getRequestURI();
			}
		}
		return currentPageUri;
	}

	public List<String> getCaseStatuses() {
		if (StringUtil.isEmpty(getCaseStatus())) {
			return null;
		}
		
		return Arrays.asList(getCaseStatus().split(CoreConstants.COMMA));
	}
	
	public String getCaseStatus() {
		if (this.caseStatus == null) {
			return null;
		}
		
		return caseStatus.replaceAll("\\s", CoreConstants.EMPTY);
	}

	public void setCaseStatus(String caseStatus) {
		this.caseStatus = caseStatus.replaceAll("\\s", CoreConstants.EMPTY);
	}

	public String getRoleKey() {
		return roleKey;
	}

	public void setRoleKey(String roleKey) {
		this.roleKey = roleKey;
	}

	public String getProcessName() {
		return processName;
	}

	public void setProcessName(String processName) {
		this.processName = processName;
	}

	public BoardCasesManager getBoardCasesManager() {
		if (boardCasesManager == null) {
			ELUtil.getInstance().autowire(this);
		}
		return boardCasesManager;
	}

	public void setBoardCasesManager(BoardCasesManager boardCasesManager) {
		this.boardCasesManager = boardCasesManager;
	}

	public BuilderLogicWrapper getBuilderLogicWrapper() {
		if (builderLogicWrapper == null)
			ELUtil.getInstance().autowire(this);
		return builderLogicWrapper;
	}

	public void setBuilderLogicWrapper(BuilderLogicWrapper builderLogicWrapper) {
		this.builderLogicWrapper = builderLogicWrapper;
	}

	public boolean isUseCurrentPageAsBackPageFromTaskViewer() {
		return useCurrentPageAsBackPageFromTaskViewer;
	}

	public void setUseCurrentPageAsBackPageFromTaskViewer(boolean useCurrentPageAsBackPageFromTaskViewer) {
		this.useCurrentPageAsBackPageFromTaskViewer = useCurrentPageAsBackPageFromTaskViewer;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

}