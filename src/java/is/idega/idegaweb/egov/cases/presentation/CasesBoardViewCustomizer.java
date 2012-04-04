package is.idega.idegaweb.egov.cases.presentation;

import is.idega.idegaweb.egov.cases.business.BoardCasesManager;
import is.idega.idegaweb.egov.cases.util.CasesConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

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

public class CasesBoardViewCustomizer extends Block {

	@Autowired
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
		List<String> keys = new ArrayList<String>();
		Map<Integer, List<AdvancedProperty>> currentColumns = boardCaseManager.getColumns(iwrb, uuid);
		for (Integer key: currentColumns.keySet()) {
			List<AdvancedProperty> columns = currentColumns.get(key);
			for (AdvancedProperty column: columns) {
				selectedColumns.addMenuElement(column.getId(), column.getValue());
				keys.add(column.getId());
			}
		}

		SelectionBox options = box.getLeftBox();
		Map<Integer, List<AdvancedProperty>> defaultColumns = boardCaseManager.getColumns(iwrb, null);
		for (Integer key: defaultColumns.keySet()) {
			List<AdvancedProperty> columns = defaultColumns.get(key);
			for (AdvancedProperty defaultColumn: columns) {
				String columnKey = defaultColumn.getId();
				if (keys.contains(columnKey))
					continue;

				options.addMenuElement(columnKey, defaultColumn.getValue());
				keys.add(columnKey);
			}
		}
		List<AdvancedProperty> availableColumns = boardCaseManager.getAvailableVariables(processName);
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