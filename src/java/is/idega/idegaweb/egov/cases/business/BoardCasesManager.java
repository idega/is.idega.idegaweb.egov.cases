package is.idega.idegaweb.egov.cases.business;

import is.idega.idegaweb.egov.cases.presentation.beans.CaseBoardBean;

import java.util.List;

import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWContext;

public interface BoardCasesManager {

	public static final String SPRING_BEAN_IDENTIFIER = "boardCasesManagerBean";
	
	public List<CaseBoardBean> getAllSortedCases(IWContext iwc, IWResourceBundle iwrb, String caseStatus);
	
	public boolean setCaseVariableValue(Integer caseId, String variableName, String value);
	
	public String getVariableValueInput(Integer caseId, String variableName, String currentValue);
	
	public String getLinkToTheTask(IWContext iwc, CaseBoardBean boardCase);
	
	public String getGradingSum(IWContext iwc, CaseBoardBean boardCase);
	
}
