package is.idega.idegaweb.egov.cases.presentation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.idega.block.process.business.ProcessConstants;
import com.idega.builder.bean.AdvancedProperty;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.Block;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.text.Heading4;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.GenericButton;
import com.idega.presentation.ui.SelectionBox;
import com.idega.presentation.ui.SelectionDoubleBox;
import com.idega.util.ListUtil;
import com.idega.util.expression.ELUtil;

import is.idega.idegaweb.egov.cases.business.BoardCasesManager;
import is.idega.idegaweb.egov.cases.util.CasesConstants;

public class CasesBoardViewCustomizer extends Block {

	public static final String FINANCING_TABLE_COLUMN = "financing_table_column";

	@Autowired
	@Qualifier(BoardCasesManager.BEAN_NAME)
	private BoardCasesManager boardCaseManager;

	@Override
	public void main(IWContext iwc) {
		ELUtil.getInstance().autowire(this);

		Form container = new Form();
		add(container);

		IWBundle bundle = getBundle(iwc);
		IWResourceBundle iwrb = bundle.getResourceBundle(iwc);

		Heading4 helpText = new Heading4(iwrb.getLocalizedString("you_can_customize_and_re_order_table",
				"You can customize and re-order the columns of the table"));
		container.add(helpText);

		String processName = iwc.getParameter(CasesBoardViewer.PARAMETER_PROCESS_NAME);
		String uuid = iwc.getParameter(CasesBoardViewer.PARAMETER_UUID);

		SelectionDoubleBox box = new SelectionDoubleBox("availableVariables", iwrb.getLocalizedString("available_columns", "Available columns"),
				iwrb.getLocalizedString("selected_columns", "Selected columns"));
		container.add(box);
		box.setEnableOrdering(true);

		SelectionBox selectedColumns = box.getRightBox();
		List<String> keys = new ArrayList<>();
		Map<Integer, List<AdvancedProperty>> currentColumns = boardCaseManager.getColumns(uuid, ProcessConstants.BPM_CASE);
		for (Integer key: currentColumns.keySet()) {
			List<AdvancedProperty> columns = currentColumns.get(key);
			for (AdvancedProperty column: columns) {
				if (CasesBoardViewer.WORK_ITEM.equals(column.getId())) {
					selectedColumns.addMenuElement(FINANCING_TABLE_COLUMN, iwrb.getLocalizedString("financing_table", "Financing table"));
					keys.add(FINANCING_TABLE_COLUMN);

				} else if (
						CasesBoardViewer.ESTIMATED_COST.equals(column.getId()) ||
						CasesBoardViewer.BOARD_SUGGESTION.equals(column.getId()) ||
						CasesBoardViewer.BOARD_DECISION.equals(column.getId())
				) {
					continue;
				} else {
					selectedColumns.addMenuElement(column.getId(), column.getValue());
					keys.add(column.getId());
				}
			}
		}

		SelectionBox options = box.getLeftBox();
		Map<Integer, List<AdvancedProperty>> defaultColumns = boardCaseManager.getColumns(null, ProcessConstants.BPM_CASE);
		for (Integer key: defaultColumns.keySet()) {
			List<AdvancedProperty> columns = defaultColumns.get(key);
			for (AdvancedProperty defaultColumn: columns) {
				String columnKey = defaultColumn.getId();
				if (
						keys.contains(columnKey) ||
						CasesBoardViewer.WORK_ITEM.equals(columnKey) ||
						CasesBoardViewer.ESTIMATED_COST.equals(columnKey) ||
						CasesBoardViewer.BOARD_SUGGESTION.equals(columnKey) ||
						CasesBoardViewer.BOARD_DECISION.equals(columnKey)
				) {
					continue;
				}

				options.addMenuElement(columnKey, defaultColumn.getValue());
				keys.add(columnKey);
			}
		}
		List<AdvancedProperty> availableColumns = boardCaseManager.getAvailableVariables(processName, ProcessConstants.BPM_CASE);
		if (!ListUtil.isEmpty(availableColumns)) {
			for (AdvancedProperty column: availableColumns) {
				String variableName = column.getId().split("@")[0];
				if (!keys.contains(variableName))
					options.addMenuElement(variableName, column.getValue());
			}
		}

		Layer buttons = new Layer();
		buttons.setStyleClass("casesBoardViewCustomizerButtons");
		container.add(buttons);

		GenericButton save = new GenericButton(iwrb.getLocalizedString("save", "Save"));
		save.setOnClick("CasesBoardHelper.saveCustomizedColumns('" + iwrb.getLocalizedString("saving", "Saving...") + "', '" +
				selectedColumns.getId() + "', '" + uuid + "');");
		buttons.add(save);

		GenericButton reset = new GenericButton(iwrb.getLocalizedString("reset", "Reset"));
		reset.setOnClick("CasesBoardHelper.resetCustomizedColumns('" + iwrb.getLocalizedString("reseting", "Resetting...") + "', '" + uuid + "');");
		buttons.add(reset);
	}

	@Override
	public String getBundleIdentifier() {
		return CasesConstants.IW_BUNDLE_IDENTIFIER;
	}
}