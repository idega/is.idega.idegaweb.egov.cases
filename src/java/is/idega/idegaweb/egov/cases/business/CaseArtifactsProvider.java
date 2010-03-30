package is.idega.idegaweb.egov.cases.business;

import java.io.Serializable;

public interface CaseArtifactsProvider {

	public <T extends Serializable> T getVariableValue(Object caseId, String variableName);
	
}
