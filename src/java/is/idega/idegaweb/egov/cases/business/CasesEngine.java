package is.idega.idegaweb.egov.cases.business;

import java.util.List;

import org.jdom.Document;

import com.idega.block.process.business.CasesRetrievalManager;
import com.idega.block.process.presentation.beans.CasePresentation;
import com.idega.block.process.presentation.beans.CasesSearchCriteriaBean;
import com.idega.builder.bean.AdvancedProperty;
import com.idega.core.business.DefaultSpringBean;
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

	public boolean setCasesPagerAttributes(int page, int pageSize);

	/**
	 * <p>Method for getting available processes in simplified form.</p>
	 * @param iwc {@link IWContext}.
	 * @return {@link List} of {@link AdvancedProperty}(
	 * {@link CasesRetrievalManager#getLatestProcessDefinitionIdByProcessName(String)},
	 * {@link CasesRetrievalManager#getProcessName(String, java.util.Locale)}
	 * ).
	 */
	public List<AdvancedProperty> getAvailableProcesses(IWContext iwc);

	/**
	 * <p>Check if there is Spring bean registered in cache with such name,
	 * which has value <code>true</code>. If not registered in cache, it checks
	 * if exist and registers answer to cache. Cache is found in
	 * {@link DefaultSpringBean#getCache(String)} method.</p>
	 * @param beanName {@link String} representation of bean name to check.
	 * @return UI class name if there is Spring bean with given name, null otherwise.
	 */
	public String isResolverExist(String beanName);

	public AdvancedProperty getExportedCases(String instanceId, String uri);

	public boolean showCaseAssets();

}
