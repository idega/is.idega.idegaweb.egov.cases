package is.idega.idegaweb.egov.cases.presentation.handlers;

import is.idega.idegaweb.egov.cases.util.CasesConstants;

import java.util.List;

import com.idega.block.process.business.CasesRetrievalManager;
import com.idega.core.builder.presentation.ICPropertyHandler;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWContext;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.SelectOption;

public class CasesListTypePropertyHandler implements ICPropertyHandler {

	public List<?> getDefaultHandlerTypes() {
		return null;
	}

	public PresentationObject getHandlerObject(String name, String stringValue, IWContext iwc, boolean oldGenerationHandler, String instanceId, String method) {
		DropdownMenu caseListTypes = new DropdownMenu();
		
		IWResourceBundle iwrb = iwc.getIWMainApplication().getBundle(CasesConstants.IW_BUNDLE_IDENTIFIER).getResourceBundle(iwc);
		
		caseListTypes.addOption(new SelectOption(iwrb.getLocalizedString("select_list_type", "Select list type"), -1));
		
		caseListTypes.addOption(new SelectOption(iwrb.getLocalizedString("open_cases", "Open cases"), CasesRetrievalManager.CASE_LIST_TYPE_OPEN));
		caseListTypes.addOption(new SelectOption(iwrb.getLocalizedString("closed_cases", "Closed cases"), CasesRetrievalManager.CASE_LIST_TYPE_CLOSED));
		caseListTypes.addOption(new SelectOption(iwrb.getLocalizedString("my_cases", "My cases"), CasesRetrievalManager.CASE_LIST_TYPE_MY));
		caseListTypes.addOption(new SelectOption(iwrb.getLocalizedString("user_cases", "User cases"), CasesRetrievalManager.CASE_LIST_TYPE_USER));
		
		caseListTypes.setSelectedElement(stringValue);
		
		return caseListTypes;
	}
	
	public void onUpdate(String[] values, IWContext iwc) {
	}

}