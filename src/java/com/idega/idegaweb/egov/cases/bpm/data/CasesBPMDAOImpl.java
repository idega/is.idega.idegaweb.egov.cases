package com.idega.idegaweb.egov.cases.bpm.data;

import java.util.List;

import com.idega.core.persistence.impl.GenericDaoImpl;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.1 $
 *
 * Last modified: $Date: 2008/01/30 14:32:16 $ by $Author: civilis $
 */
public class CasesBPMDAOImpl extends GenericDaoImpl implements CasesBPMDAO {

	public List<CasesBPMBind> getAllCasesJbpmBinds() {

		@SuppressWarnings("unchecked")
		List<CasesBPMBind> binds = getEntityManager().createNamedQuery(CasesBPMBind.SIMPLE_CASES_GET_ALL_QUERY_NAME).getResultList();

		return binds;
	}
	
	public List<Object[]> getSimpleProcessDefinitions() {
		
		@SuppressWarnings("unchecked")
		List<Object[]> casesProcesses = getEntityManager().createNamedQuery(CasesBPMBind.SIMPLE_CASES_PROCESSES_DEFINITIONS_QUERY_NAME)
		.getResultList();
		
		return casesProcesses;
	}
}