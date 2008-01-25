package com.idega.idegaweb.egov.cases.bpm.data;

import java.util.List;

import com.idega.core.persistence.impl.GenericDaoImpl;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.1 $
 *
 * Last modified: $Date: 2008/01/25 15:23:55 $ by $Author: civilis $
 */
public class CasesJbpmDaoImpl extends GenericDaoImpl implements CasesJbpmDao {

	public List<CasesJbpmBind> getAllCasesJbpmBinds() {

		@SuppressWarnings("unchecked")
		List<CasesJbpmBind> binds = getEntityManager().createNamedQuery(CasesJbpmBind.SIMPLE_CASES_GET_ALL_QUERY_NAME).getResultList();

		return binds;
	}
	
	public List<Object[]> getSimpleProcessDefinitions() {
		
		@SuppressWarnings("unchecked")
		List<Object[]> casesProcesses = getEntityManager().createNamedQuery(CasesJbpmBind.SIMPLE_CASES_PROCESSES_DEFINITIONS_QUERY_NAME)
		.getResultList();
		
		return casesProcesses;
	}
}