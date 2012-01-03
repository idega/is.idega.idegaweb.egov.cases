package is.idega.idegaweb.egov.cases.presentation.beans;

import java.util.Map;

import com.idega.builder.bean.AdvancedProperty;
import com.idega.user.data.User;

public class CaseBoardTableBodyRowBean {

	private String id;
	private String caseId;
	private String caseIdentifier;
	
	private Long processInstanceId;
	
	private User handler;
	
	public CaseBoardTableBodyRowBean(String caseId, Long processInstanceId) {
		this.caseId = caseId;
		this.processInstanceId = processInstanceId;
	}
	
	private Map<Integer, AdvancedProperty> values;

	public Map<Integer, AdvancedProperty> getValues() {
		return values;
	}
	public void setValues(Map<Integer, AdvancedProperty> values) {
		this.values = values;
	}
	public String getCaseId() {
		return caseId;
	}
	public void setCaseId(String caseId) {
		this.caseId = caseId;
	}
	public String getCaseIdentifier() {
		return caseIdentifier;
	}
	public void setCaseIdentifier(String caseIdentifier) {
		this.caseIdentifier = caseIdentifier;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Long getProcessInstanceId() {
		return processInstanceId;
	}
	public void setProcessInstanceId(Long processInstanceId) {
		this.processInstanceId = processInstanceId;
	}
	public User getHandler() {
		return handler;
	}
	public void setHandler(User handler) {
		this.handler = handler;
	}
	
}