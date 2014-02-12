package is.idega.idegaweb.egov.cases.bean;

import java.io.Serializable;

public class CasesExportParams implements Serializable {

	private static final long serialVersionUID = -4099540752887950292L;

	private Long processDefinitionId;

	private String id, status, dateFrom, dateTo;

	public Long getProcessDefinitionId() {
		return processDefinitionId;
	}

	public void setProcessDefinitionId(Long processDefinitionId) {
		this.processDefinitionId = processDefinitionId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getDateFrom() {
		return dateFrom;
	}

	public void setDateFrom(String dateFrom) {
		this.dateFrom = dateFrom;
	}

	public String getDateTo() {
		return dateTo;
	}

	public void setDateTo(String dateTo) {
		this.dateTo = dateTo;
	}

	@Override
	public String toString() {
		return "Proc. def. ID: " + getProcessDefinitionId() + ", ID: " + getId() + ", status: " + getStatus() + ", from: " + getDateFrom() + ", to: " +
				getDateTo();
	}

}