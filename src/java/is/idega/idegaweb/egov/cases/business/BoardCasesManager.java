package is.idega.idegaweb.egov.cases.business;

import is.idega.idegaweb.egov.cases.presentation.beans.CaseBoardBean;
import is.idega.idegaweb.egov.cases.presentation.beans.CaseBoardTableBean;

import java.util.List;

import com.idega.builder.bean.AdvancedProperty;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWContext;
import com.idega.user.data.User;

public interface BoardCasesManager {
	
	public List<CaseBoardBean> getAllSortedCases(IWContext iwc, IWResourceBundle iwrb, String caseStatus, String processName);
	
	public AdvancedProperty setCaseVariableValue(Integer caseId, String variableName, String value, String role, String backPage);
	
	public String getPageUriForTaskViewer(IWContext iwc);
	
	public String getLinkToTheTaskRedirector(IWContext iwc, String basePage, String caseId, Long processInstanceId, String backPage);
	
	public CaseBoardTableBean getTableData(IWContext iwc, String caseStatus, String processName);
	
	public AdvancedProperty getHandlerInfo(IWContext iwc, User handler);
	
	public String getTaskInstanceIdForTask(Long processInstanceId, String taskName);
	
	public String getLinkToTheTask(IWContext iwc, String caseId, String taskInstanceId, String backPage);
}
