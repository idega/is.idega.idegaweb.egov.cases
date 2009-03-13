package is.idega.idegaweb.egov.cases.presentation;

import is.idega.idegaweb.egov.cases.business.BoardCasesManager;
import is.idega.idegaweb.egov.cases.media.CasesBoardViewerExporter;
import is.idega.idegaweb.egov.cases.presentation.beans.CaseBoardBean;
import is.idega.idegaweb.egov.cases.util.CasesConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.faces.context.FacesContext;

import org.springframework.beans.factory.annotation.Autowired;

import com.idega.block.web2.business.JQueryUIType;
import com.idega.block.web2.business.Web2Business;
import com.idega.builder.bean.AdvancedProperty;
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
import com.idega.presentation.TableHeaderRowGroup;
import com.idega.presentation.TableRow;
import com.idega.presentation.text.Heading3;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.GenericButton;
import com.idega.presentation.ui.HiddenInput;
import com.idega.util.CoreConstants;
import com.idega.util.CoreUtil;
import com.idega.util.ListUtil;
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
	
	@Autowired
	private BoardCasesManager boardCasesManager;
	
	@Autowired
	private Web2Business web2;
	
	private String caseStatus;
	private String role;
	
	@Override
	protected void initializeComponent(FacesContext context) {
		IWContext iwc = IWContext.getIWContext(context);
		
		if (!iwc.isLoggedOn()) {
			LOGGER.warning("User must be logged to see cases!");
		}
		if (!StringUtil.isEmpty(role) && !iwc.hasRole(role)) {
			LOGGER.warning("User must have role '" + role + "' to see cases!");
		}
		
		ELUtil.getInstance().autowire(this);
		
		IWBundle bundle = getBundle(context, CasesConstants.IW_BUNDLE_IDENTIFIER);
		IWResourceBundle iwrb = bundle.getResourceBundle(iwc);
		
		PresentationUtil.addJavaScriptSourceLineToHeader(iwc, web2.getBundleURIToJQueryLib());
		PresentationUtil.addJavaScriptSourceLineToHeader(iwc, web2.getBundleUriToHumanizedMessagesScript());
		PresentationUtil.addJavaScriptSourceLineToHeader(iwc, web2.getBundleURIToJQueryUILib(JQueryUIType.UI_EDITABLE));
		PresentationUtil.addJavaScriptSourceLineToHeader(iwc, CoreConstants.DWR_ENGINE_SCRIPT);
		PresentationUtil.addJavaScriptSourceLineToHeader(iwc, CoreConstants.DWR_UTIL_SCRIPT);
		PresentationUtil.addJavaScriptSourceLineToHeader(iwc, "/dwr/interface/BoardCasesManager.js");
		PresentationUtil.addJavaScriptSourceLineToHeader(iwc, bundle.getVirtualPathWithFileNameString("javascript/CasesBoardHelper.js"));
		PresentationUtil.addStyleSheetToHeader(iwc, web2.getBundleUriToHumanizedMessagesStyleSheet());
		PresentationUtil.addStyleSheetToHeader(iwc, bundle.getVirtualPathWithFileNameString("style/case.css"));
		
		Layer container = new Layer();
		getChildren().add(container);
		container.setStyleClass("casesBoardViewerContainer");
		
		addCasesTable(container, iwc, iwrb);
		
		container.add(new CSSSpacer());
		
		addButtons(container, iwc, iwrb);
		
		String initAction = new StringBuilder("CasesBoardHelper.initializeBoardCases({savingMessage: '")
			.append(iwrb.getLocalizedString("case_board_viewer.saving_case_variable", "Saving...")).append("', select: '")
			.append(iwrb.getLocalizedString("case_board_viewer.select_value", "Select")).append("'});").toString();
		if (!CoreUtil.isSingleComponentRenderingProcess(iwc)) {
			initAction = new StringBuilder("jQuery(document).ready(function() {").append(initAction).append("});").toString();
		}
		PresentationUtil.addJavaScriptActionToBody(iwc, initAction);
	}
	
	private void addCasesTable(Layer container, IWContext iwc, IWResourceBundle iwrb) {
		List<CaseBoardBean> boardCases = boardCasesManager.getAllSortedCases(iwc, iwrb, caseStatus);
		if (ListUtil.isEmpty(boardCases)) {
			getChildren().add(new Heading3(iwrb.getLocalizedString("cases_board_viewer.no_cases_found", "There are no cases!")));
			return;
		}
		
		Table2 table = new Table2();
		container.add(table);
		table.setStyleClass("casesBoardViewerTable");
		
		TableHeaderRowGroup header = table.createHeaderRowGroup();
		TableRow headerRow = header.createRow();
		for (String headerLabel: getTableHeaders(iwrb)) {
			headerRow.createHeaderCell().add(new Text(headerLabel));
		}

		double grantAmountSuggestionTotal = 0;
		double boardAmountTotal = 0;
		TableBodyRowGroup body = table.createBodyRowGroup();
		for (CaseBoardBean caseBoard: boardCases) {
			TableRow row = body.createRow();
			
			row.getChildren().add(new HiddenInput("casesBoardViewerTableEditableCellCaseId", caseBoard.getCaseId()));
			
			int index = 0;
			List<String> allValues = caseBoard.getAllValues();
			for (String value: allValues) {
				TableCell2 bodyRowCell = row.createCell();
				
				if (index == 2) {
					//	Link to grading task
					bodyRowCell.add(new Link(caseBoard.getCaseIdentifier(), boardCasesManager.getLinkToTheTask(iwc, caseBoard)));
				}
				else {
					bodyRowCell.add(new Text(value));
				}
				
				if (index == allValues.size() - 3) {
					//	Calculating grant amount suggestions
					grantAmountSuggestionTotal += caseBoard.getGrantAmountSuggestion();
				} else if (index == allValues.size() - 2) {
					//	Calculating board amounts
					boardAmountTotal += caseBoard.getBoardAmount();
				}
				
				if (index == 7) {
					//	SUMs for grading variables
					bodyRowCell.add(new Text(boardCasesManager.getGradingSum(iwc, caseBoard)));
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
		}
		
		TableFooterRowGroup footer = table.createFooterRowGroup();
		TableRow footerRow = footer.createRow();
		for (int i = 0; i < CASE_FIELDS.size(); i++) {
			TableCell2 footerCell = footerRow.createCell();
			
			if (i == CASE_FIELDS.size() - 4) {
				//	SUMs label
				footerCell.add(new Text(new StringBuilder(iwrb.getLocalizedString("case_board_viewer.total_sum", "Total")).append(CoreConstants.COLON)
						.toString()));
			} else if (i == CASE_FIELDS.size() - 3) {
				//	Grant amount suggestions
				footerCell.add(new Text(String.valueOf(grantAmountSuggestionTotal)));
			} else if (i == CASE_FIELDS.size() - 2) {
				//	Board amount
				footerCell.add(new Text(String.valueOf(boardAmountTotal)));
			}
		}
		
		container.add(new HiddenInput(new StringBuilder("casesBoardViewerTableEditableCell").append(EDITABLE_FIELD_TYPE_DROPDOWN).append("VariableName")
				.toString(), CASE_FIELDS.get(8).getId()));
		container.add(new HiddenInput(new StringBuilder("casesBoardViewerTableEditableCell").append(EDITABLE_FIELD_TYPE_TEXT_INPUT).append("VariableName")
				.toString(), CASE_FIELDS.get(11).getId()));
		container.add(new HiddenInput(new StringBuilder("casesBoardViewerTableEditableCell").append(EDITABLE_FIELD_TYPE_TEXT_AREA).append("VariableName")
				.toString(), CASE_FIELDS.get(12).getId()));
	}
	
	private void makeCellEditable(TableCell2 cell, String type) {
		cell.setStyleClass(new StringBuilder("casesBoardViewerTableEditableCell").append(type).toString());
	}
	
	private void addButtons(Layer container, IWContext iwc, IWResourceBundle iwrb) {
		Layer buttonsContainer = new Layer();
		container.add(buttonsContainer);
		buttonsContainer.setStyleClass("casesBoardViewerContainer");
		
		GenericButton exportToExcel = new GenericButton(iwrb.getLocalizedString("cases_board_viewer.export_cases_list_to_excel", "Export to Excel"));
		//buttonsContainer.add(exportToExcel);	//	TODO: finish up!
		exportToExcel.setOnClick(new StringBuilder("humanMsg.displayMsg('").append(iwrb.getLocalizedString("cases_board_viewer.exporting_cases_list_to_excel",
				"Exporting to Excel")).append("');window.location.href='").append(getUriToExcelExporter(iwc)).append("';").toString());
	}
	
	private String getUriToExcelExporter(IWContext iwc) {
		StringBuilder uri = new StringBuilder(iwc.getIWMainApplication().getMediaServletURI()).append("?").append(MediaWritable.PRM_WRITABLE_CLASS)
			.append("=").append(IWMainApplication.getEncryptedClassName(CasesBoardViewerExporter.class));
		
		if (!StringUtil.isEmpty(caseStatus)) {
			uri.append("&").append(CASES_BOARD_VIEWER_CASES_STATUS_PARAMETER).append("=").append(caseStatus);
		}
		
		return uri.toString();
	}
	
	private List<String> getTableHeaders(IWResourceBundle iwrb) {
		String prefix = "case_board_viewer.";
		List<String> headers = new ArrayList<String>(CASE_FIELDS.size());
		for (AdvancedProperty header: CASE_FIELDS) {
			headers.add(iwrb.getLocalizedString(new StringBuilder(prefix).append(header.getId()).toString(), header.getValue()));
		}
		return headers;
	}

	public String getCaseStatus() {
		return caseStatus;
	}

	public void setCaseStatus(String caseStatus) {
		this.caseStatus = caseStatus;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

}
