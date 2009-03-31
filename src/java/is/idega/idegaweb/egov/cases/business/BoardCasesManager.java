package is.idega.idegaweb.egov.cases.business;

import is.idega.idegaweb.egov.cases.presentation.beans.CaseBoardBean;
import is.idega.idegaweb.egov.cases.presentation.beans.CaseBoardTableBean;

import java.util.List;

import com.idega.builder.bean.AdvancedProperty;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWContext;

public interface BoardCasesManager {
	
	public List<CaseBoardBean> getAllSortedCases(IWContext iwc, IWResourceBundle iwrb, String caseStatus, String processName);
	
	public AdvancedProperty setCaseVariableValue(Integer caseId, String variableName, String value, String role, String backPage);
	
	public String getPageUriForTaskViewer(IWContext iwc);
	
	public String getLinkToTheTask(IWContext iwc, String caseId, String basePage, String backPage);
	
	public String getGradingSum(IWContext iwc, CaseBoardBean boardCase);
	
	public CaseBoardTableBean getTableData(IWContext iwc, String caseStatus, String processName);
	
	public AdvancedProperty getHandlerInfo(IWContext iwc, String userId);
	
}
