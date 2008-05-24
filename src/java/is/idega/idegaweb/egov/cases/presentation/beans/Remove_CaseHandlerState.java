package is.idega.idegaweb.egov.cases.presentation.beans;

import java.io.Serializable;

/**
 * 
 * @author <a href="civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.1 $
 *
 * Last modified: $Date: 2008/05/24 10:22:53 $ by $Author: civilis $
 *
 */
public class Remove_CaseHandlerState implements Serializable {

	private static final long serialVersionUID = 7995176049178489211L;

	public static final String beanIdentifier = "caseHandlerState";
		
	private Integer caseId;
	private Boolean showCaseHandler = false;
	
	public Integer getCaseId() {
		return caseId;
	}
	public void setCaseId(Integer caseId) {
		this.caseId = caseId;
	}
	public Boolean getShowCaseHandler() {
		return showCaseHandler;
	}
	public void setShowCaseHandler(Boolean showCaseHandler) {
		this.showCaseHandler = showCaseHandler == null ? false : showCaseHandler;
	}
	public void reset() {
		
		showCaseHandler = false;
		caseId = null;
	}
}