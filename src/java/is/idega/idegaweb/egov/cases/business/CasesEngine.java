package is.idega.idegaweb.egov.cases.business;

import java.util.List;

import com.idega.builder.bean.AdvancedProperty;
import com.idega.presentation.IWContext;

public interface CasesEngine {

	public abstract boolean clearSearchResults(String id);
	
	public abstract AdvancedProperty getExportedSearchResults(String id);
	
	public abstract boolean setCaseSubject(String caseId, String subject);
	
	public abstract List<AdvancedProperty> getDefaultSortingOptions(IWContext iwc);
	
}
