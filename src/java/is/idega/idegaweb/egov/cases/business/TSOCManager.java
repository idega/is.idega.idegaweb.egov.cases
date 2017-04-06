package is.idega.idegaweb.egov.cases.business;

import com.idega.builder.bean.AdvancedProperty;

public interface TSOCManager {

	void startWorkingOnCase(Integer userId, Integer caseId);

	void stopWorkingOnCase(Integer userId, Integer caseId);

	AdvancedProperty getCurrentState(Integer userId, Integer caseId);

}