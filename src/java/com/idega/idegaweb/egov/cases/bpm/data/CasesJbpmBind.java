package com.idega.idegaweb.egov.cases.bpm.data;


import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.1 $
 *
 * Last modified: $Date: 2008/01/25 15:23:55 $ by $Author: civilis $
 */
@Entity
@Table(name="CASES_JBPM_BINDINGS")
@NamedQueries(
		{
			@NamedQuery(name="casesJbpmBind.simpleCasesProcessesDefinitionsQuery", query="select pd.id, pd.name from org.jbpm.graph.def.ProcessDefinition pd, CasesJbpmBind cb where pd.id = cb.procDefId"),
			@NamedQuery(name="casesJbpmBind.getAllQuery", query="from CasesJbpmBind")
		}
)
public class CasesJbpmBind implements Serializable {
	
	private static final long serialVersionUID = -3222584305636229751L;
	
	public static final String SIMPLE_CASES_PROCESSES_DEFINITIONS_QUERY_NAME = "casesJbpmBind.simpleCasesProcessesDefinitionsQuery";
	public static final String SIMPLE_CASES_GET_ALL_QUERY_NAME = "casesJbpmBind.getAllQuery";

	@Id
	@Column(name="process_definition_id")
    private Long procDefId;
	
	@Column(name="cases_category_id")
	private Long casesCategoryId;
	
	@Column(name="cases_type_id")
	private Long casesTypeId;
	
	@Column(name="init_task_name")
	private String initTaskName;
	
	public Long getCasesCategoryId() {
		return casesCategoryId;
	}

	public void setCasesCategoryId(Long casesCategoryId) {
		this.casesCategoryId = casesCategoryId;
	}

	public Long getCasesTypeId() {
		return casesTypeId;
	}

	public void setCasesTypeId(Long casesTypeId) {
		this.casesTypeId = casesTypeId;
	}

	public CasesJbpmBind() { }

	public Long getProcDefId() {
		return procDefId;
	}

	public void setProcDefId(Long procDefId) {
		this.procDefId = procDefId;
	}

	public String getInitTaskName() {
		return initTaskName;
	}

	public void setInitTaskName(String initTaskName) {
		this.initTaskName = initTaskName;
	}
}