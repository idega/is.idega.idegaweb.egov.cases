package is.idega.idegaweb.egov.cases.business;

import is.idega.idegaweb.egov.cases.presentation.beans.CaseBoardBean;
import is.idega.idegaweb.egov.cases.presentation.beans.CaseBoardTableBean;

import java.util.List;
import java.util.Map;

import com.idega.builder.bean.AdvancedProperty;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWContext;
import com.idega.user.data.User;

public interface BoardCasesManager {

	public List<CaseBoardBean> getAllSortedCases(IWContext iwc, IWResourceBundle iwrb, String caseStatus, String processName, String uuid);

	public CaseBoardTableBean getTableData(IWContext iwc, String caseStatus, String processName, String uuid);

	public AdvancedProperty getHandlerInfo(IWContext iwc, User handler);

	public String getLinkToTheTaskRedirector(IWContext iwc, String basePage, String caseId, Long processInstanceId, String backPage, String taskName);

	public List<AdvancedProperty> getAvailableVariables(String processName);

	public Map<Integer, List<AdvancedProperty>> getColumns(IWResourceBundle iwrb, String uuid);

	public List<String> getCustomColumns(String uuid);

	public boolean isColumnOfDomain(String currentColumn, String columnOfDomain);

	public int getIndexOfColumn(String column, String uuid);

	public Long getNumberValue(String value);

}