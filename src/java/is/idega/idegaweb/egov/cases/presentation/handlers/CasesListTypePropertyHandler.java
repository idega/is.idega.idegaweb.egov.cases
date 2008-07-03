package is.idega.idegaweb.egov.cases.presentation.handlers;

import is.idega.idegaweb.egov.cases.business.CasesBusiness;
import is.idega.idegaweb.egov.cases.util.CasesConstants;

import java.util.List;

import com.idega.block.process.presentation.UserCases;
import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.core.builder.business.ICBuilderConstants;
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
		
		CasesBusiness casesBusiness = null;
		try {
			casesBusiness = (CasesBusiness) IBOLookup.getServiceInstance(iwc, CasesBusiness.class);
		} catch (IBOLookupException e) {
			e.printStackTrace();
			return caseListTypes;
		}
		
		caseListTypes.addOption(new SelectOption(iwrb.getLocalizedString("open_cases", "Open cases"), getStringFromArray(casesBusiness.getStatusesForOpenCases())));
		caseListTypes.addOption(new SelectOption(iwrb.getLocalizedString("closed_cases", "Closed cases"),
				getStringFromArray(casesBusiness.getStatusesForClosedCases())));
		caseListTypes.addOption(new SelectOption(iwrb.getLocalizedString("my_cases", "My cases"), getStringFromArray(casesBusiness.getStatusesForMyCases())));
		caseListTypes.addOption(new SelectOption(iwrb.getLocalizedString("user_cases", "User cases"), UserCases.TYPE));
		
		caseListTypes.setSelectedElement(stringValue);
		
		return caseListTypes;
	}

	private String getStringFromArray(String[] values) {
		if (values == null || values.length == 0) {
			return String.valueOf(-1);
		}
		
		StringBuilder value = new StringBuilder();
		for (int i = 0; i < values.length; i++) {
			value.append(values[i]);
			
			if ((i + 1) < values.length) {
				value.append(ICBuilderConstants.BUILDER_MODULE_PROPERTY_VALUES_SEPARATOR);
			}
		}
		return value.toString();
	}
	
	public void onUpdate(String[] values, IWContext iwc) {
	}

}
