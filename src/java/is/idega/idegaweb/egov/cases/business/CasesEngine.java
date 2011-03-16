package is.idega.idegaweb.egov.cases.business;

import java.util.List;

import org.jdom.Document;

import com.idega.block.process.presentation.beans.CasePresentation;
import com.idega.block.process.presentation.beans.CasesSearchCriteriaBean;
import com.idega.builder.bean.AdvancedProperty;
import com.idega.presentation.IWContext;
import com.idega.presentation.paging.PagedDataCollection;

public interface CasesEngine {

	public abstract boolean clearSearchResults(String id);
	
	public abstract AdvancedProperty getExportedSearchResults(String id);
	
	public abstract boolean setCaseSubject(String caseId, String subject);
	
	public abstract List<AdvancedProperty> getDefaultSortingOptions(IWContext iwc);
	
	public PagedDataCollection<CasePresentation> getCasesByQuery(CasesSearchCriteriaBean criteriaBean);
	
	public Document getRenderedCasesByQuery(CasesSearchCriteriaBean criteriaBean);
	
	public String getCaseStatus(Long processInstanceId);
}
