package is.idega.idegaweb.egov.cases.presentation;

import is.idega.idegaweb.egov.cases.business.BoardCasesManager;
import is.idega.idegaweb.egov.cases.media.CasesBoardViewerExporter;
import is.idega.idegaweb.egov.cases.presentation.beans.CaseBoardTableBean;
import is.idega.idegaweb.egov.cases.presentation.beans.CaseBoardTableBodyRowBean;
import is.idega.idegaweb.egov.cases.util.CasesConstants;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.faces.context.FacesContext;

import org.springframework.beans.factory.annotation.Autowired;

import com.idega.block.web2.business.JQueryPlugin;
import com.idega.block.web2.business.JQueryUIType;
import com.idega.block.web2.business.Web2Business;
import com.idega.builder.bean.AdvancedProperty;
import com.idega.builder.business.BuilderLogicWrapper;
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
import com.idega.util.CoreConstants;
import com.idega.util.CoreUtil;
import com.idega.util.PresentationUtil;
import com.idega.util.StringUtil;
import com.idega.util.expression.ELUtil;

public class CasesBoardViewer extends IWBaseComponent {

	private static final Logger LOGGER = Logger.getLogger(CasesBoardViewer.class.getName());
	
	private static final String EDITABLE_FIELD_TYPE_TEXT_INPUT = "textinput";
	private static final String EDITABLE_FIELD_TYPE_TEXT_AREA = "textarea";
	private static final String EDITABLE_FIELD_TYPE_DROPDOWN = "select";
	
	public static final List<AdvancedProperty> CASE_FIELDS = Collections.unmodifiableList(Arrays.asList(
		new AdvancedProperty("string_ownerFullName", "Applicant"),						//	0
		new AdvancedProperty("string_ownerPostCode", "Zip"),							//	1
		new AdvancedProperty("string_caseIdentifier", "Case nr."),						//	2
		new AdvancedProperty("string_caseDescription", "Description"),					//	3
		
		new AdvancedProperty("string_ownerTotalCost", "Total cost"),					//	4
		new AdvancedProperty("string_ownerGrantAmount", "Applied amount"),				//	5
		
		new AdvancedProperty("string_ownerBusinessConcept", "In a nutshell"),			//	6
		
		new AdvancedProperty("sum_all_grades", "Grade"),								//	7
		
		new AdvancedProperty("string_ownerProjectLead", "Category"),					//	8,	EDITABLE, select
		
		new AdvancedProperty("string_ownerGrade", "Comment"),							//	9
		new AdvancedProperty("string_ownerGradeComment", "Grant amount suggestion"),	//	10
		new AdvancedProperty("string_ownerGrantAmauntValue", "Board amount"),			//	11,	EDITABLE, text input
		new AdvancedProperty("string_ownerAnswer", "Restrictions")						//	12, EDITABLE, text area
	));
	
	public static final String CASES_BOARD_VIEWER_CASES_STATUS_PARAMETER = "casesBoardViewerCasesStatusParameter";
	public static final String CASES_BOARD_VIEWER_PROCESS_NAME_PARAMETER = "casesBoardViewerProcessNameParameter";
	
	@Autowired
	private BoardCasesManager boardCasesManager;
	
	@Autowired
	private Web2Business web2;
	
	@Autowired
	private BuilderLogicWrapper builderLogicWrapper;
	
	private String caseStatus;
	private String roleKey;
	private String processName;
	
	@Override
	protected void initializeComponent(FacesContext context) {
		IWContext iwc = IWContext.getIWContext(context);
		
		if (!iwc.isLoggedOn()) {
			LOGGER.warning("User must be logged to see cases!");
		}
		if (!StringUtil.isEmpty(roleKey) && !iwc.hasRole(roleKey)) {
			LOGGER.warning("User must have role '" + roleKey + "' to see cases!");
		}
		
		ELUtil.getInstance().autowire(this);
		
		IWBundle bundle = getBundle(context, CasesConstants.IW_BUNDLE_IDENTIFIER);
		IWResourceBundle iwrb = bundle.getResourceBundle(iwc);
		
		PresentationUtil.addJavaScriptSourcesLinesToHeader(iwc, Arrays.asList(
			web2.getBundleURIToJQueryLib(),
			web2.getBundleUriToHumanizedMessagesScript(),
			web2.getBundleURIToJQueryUILib(JQueryUIType.UI_EDITABLE),
			web2.getBundleURIToJQueryPlugin(JQueryPlugin.TABLE_SORTER),
			CoreConstants.DWR_ENGINE_SCRIPT,
			CoreConstants.DWR_UTIL_SCRIPT,
			"/dwr/interface/BoardCasesManager.js",
			bundle.getVirtualPathWithFileNameString("javascript/CasesBoardHelper.js")
		));
		PresentationUtil.addStyleSheetsToHeader(iwc, Arrays.asList(
			web2.getBundleUriToHumanizedMessagesStyleSheet(),
			bundle.getVirtualPathWithFileNameString("style/case.css")
		));
		
		Layer container = new Layer();
		getChildren().add(container);
		container.setStyleClass("casesBoardViewerContainer");
		
		addCasesTable(container, iwc, iwrb);
		
		container.add(new CSSSpacer());
		
		addButtons(container, iwc, iwrb);
		
		String initAction = new StringBuilder("CasesBoardHelper.initializeBoardCases({savingMessage: '")
			.append(iwrb.getLocalizedString("case_board_viewer.saving_case_variable", "Saving...")).append("', remove: '")
			.append(iwrb.getLocalizedString("case_board_viewer.remove_value", "Remove")).append("', edit: '")
			.append(iwrb.getLocalizedString("case_board_viewer.edit_value", "Edit")).append("'});").toString();
		if (!CoreUtil.isSingleComponentRenderingProcess(iwc)) {
			initAction = new StringBuilder("jQuery(document).ready(function() {").append(initAction).append("});").toString();
		}
		PresentationUtil.addJavaScriptActionToBody(iwc, initAction);
	}
	
	private void addCasesTable(Layer container, IWContext iwc, IWResourceBundle iwrb) {
		CaseBoardTableBean data = boardCasesManager.getTableData(iwc, caseStatus, processName);
		if (data == null || !data.isFilledWithData()) {
			getChildren().add(new Heading3(data.getErrorMessage()));
			return;
		}
		
		Table2 table = new Table2();
		container.add(table);
		table.setStyleClass("casesBoardViewerTable");
		
		TableHeaderRowGroup header = table.createHeaderRowGroup();
		TableRow headerRow = header.createRow();
		int index = 0;
		for (String headerLabel: data.getHeaderLabels()) {
			TableHeaderCell headerCell = headerRow.createHeaderCell();
			headerCell.add(new Text(headerLabel));
			
			if (index == 6 || index == 12) {
				headerCell.setStyleClass("casesBoardViewerTableWiderCell");
			}
			
			index++;
		}

		int rowsIndex = 0;
		TableBodyRowGroup body = table.createBodyRowGroup();
		body.setStyleClass("casesBoardViewerBodyRows");
		String taskViewerPage = boardCasesManager.getPageUriForTaskViewer(iwc);
		for (CaseBoardTableBodyRowBean rowBean: data.getBodyBeans()) {
			TableRow row = body.createRow();
			row.setId(rowBean.getId());
			row.setStyleClass(rowsIndex % 2 == 0 ? "even" : "odd");
			
			index = 0;
			for (String value: rowBean.getValues()) {
				TableCell2 bodyRowCell = row.createCell();
				
				if (index == 2) {
					//	Link to grading task
					bodyRowCell.add(new Link(rowBean.getCaseIdentifier(), boardCasesManager.getLinkToTheTask(iwc, rowBean.getCaseId(), taskViewerPage)));
				}
				else {
					bodyRowCell.add(new Text(value));
				}
				
				//	Editable fields
				if (index == 8) {
					makeCellEditable(bodyRowCell, EDITABLE_FIELD_TYPE_DROPDOWN);
				}
				if (index == 11) {
					makeCellEditable(bodyRowCell, EDITABLE_FIELD_TYPE_TEXT_INPUT);
				}
				if (index == 12) {
					makeCellEditable(bodyRowCell, EDITABLE_FIELD_TYPE_TEXT_AREA);
				}
				
				index++;
			}
			
			rowsIndex++;
		}
		
		index = 0;
		String totalBoardAmountCellId = null;
		TableFooterRowGroup footer = table.createFooterRowGroup();
		TableRow footerRow = footer.createRow();
		for (String footerLabel: data.getFooterValues()) {
			TableCell2 footerCell = footerRow.createCell();
			footerCell.add(new Text(footerLabel));
			
			if (CASE_FIELDS.size() - 2 == index) {
				totalBoardAmountCellId = footerCell.getId();
			}
			
			index++;
		}
		
		container.add(new HiddenInput(new StringBuilder("casesBoardViewerTableEditableCell").append(EDITABLE_FIELD_TYPE_DROPDOWN).append("VariableName")
				.toString(), CASE_FIELDS.get(8).getId()));
		container.add(new HiddenInput(new StringBuilder("casesBoardViewerTableEditableCell").append(EDITABLE_FIELD_TYPE_TEXT_INPUT).append("VariableName")
				.toString(), CASE_FIELDS.get(11).getId()));
		container.add(new HiddenInput(new StringBuilder("casesBoardViewerTableEditableCell").append(EDITABLE_FIELD_TYPE_TEXT_AREA).append("VariableName")
				.toString(), CASE_FIELDS.get(12).getId()));
		container.add(new HiddenInput("casesBoardViewerTableRoleKey", StringUtil.isEmpty(roleKey) ? CoreConstants.EMPTY : roleKey));
		
		container.add(new HiddenInput("casesBoardViewerTableUniqueIdKey", builderLogicWrapper.getBuilderService(iwc).getInstanceId(this)));
		container.add(new HiddenInput("casesBoardViewerTableContainerKey", container.getId()));
		container.add(new HiddenInput("casesBoardViewerTableTotalBoardAmountCellIdKey", totalBoardAmountCellId));
		
		String initAction = new StringBuilder("jQuery('#").append(table.getId()).append("').tablesorter();").toString();
		if (!CoreUtil.isSingleComponentRenderingProcess(iwc)) {
			initAction = new StringBuilder("jQuery(window).load(function() {").append(initAction).append("});").toString();
		}
		PresentationUtil.addJavaScriptActionToBody(iwc, initAction);
	}
	
	private void makeCellEditable(TableCell2 cell, String type) {
		cell.setStyleClass(new StringBuilder("casesBoardViewerTableEditableCell").append(type).toString());
	}
	
	private void addButtons(Layer container, IWContext iwc, IWResourceBundle iwrb) {
		Layer buttonsContainer = new Layer();
		container.add(buttonsContainer);
		buttonsContainer.setStyleClass("casesBoardViewerContainer");
		
		GenericButton exportToExcel = new GenericButton(iwrb.getLocalizedString("cases_board_viewer.export_cases_list_to_excel", "Export to Excel"));
		buttonsContainer.add(exportToExcel);
		exportToExcel.setOnClick(new StringBuilder("humanMsg.displayMsg('").append(iwrb.getLocalizedString("cases_board_viewer.exporting_cases_list_to_excel",
				"Exporting to Excel")).append("');window.location.href='").append(getUriToExcelExporter(iwc)).append("';").toString());
	}
	
	private String getUriToExcelExporter(IWContext iwc) {
		StringBuilder uri = new StringBuilder(iwc.getIWMainApplication().getMediaServletURI()).append("?").append(MediaWritable.PRM_WRITABLE_CLASS)
			.append("=").append(IWMainApplication.getEncryptedClassName(CasesBoardViewerExporter.class));
		
		if (!StringUtil.isEmpty(caseStatus)) {
			uri.append("&").append(CASES_BOARD_VIEWER_CASES_STATUS_PARAMETER).append("=").append(caseStatus);
		}
		if (!StringUtil.isEmpty(processName)) {
			uri.append("&").append(CASES_BOARD_VIEWER_PROCESS_NAME_PARAMETER).append("=").append(processName);
		}
		
		return uri.toString();
	}

	public String getCaseStatus() {
		return caseStatus;
	}

	public void setCaseStatus(String caseStatus) {
		this.caseStatus = caseStatus;
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

}
