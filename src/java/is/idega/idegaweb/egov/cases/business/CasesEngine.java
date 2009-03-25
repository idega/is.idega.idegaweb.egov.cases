package is.idega.idegaweb.egov.cases.business;

import java.util.List;

import com.idega.builder.bean.AdvancedProperty;
import com.idega.presentation.IWContext;

public interface CasesEngine {

	public boolean clearSearchResults(String id);
	
	public AdvancedProperty getExportedSearchResults(String id);
	
	public boolean setCaseSubject(String caseId, String subject);
	
	public List<AdvancedProperty> getDefaultSortingOptions(IWContext iwc);
	
}
