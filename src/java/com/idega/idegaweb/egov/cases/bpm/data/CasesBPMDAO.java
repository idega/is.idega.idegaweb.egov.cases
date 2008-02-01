package com.idega.idegaweb.egov.cases.bpm.data;

import java.util.List;

import com.idega.core.persistence.GenericDao;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.2 $
 *
 * Last modified: $Date: 2008/02/01 12:20:03 $ by $Author: civilis $
 */
public interface CasesBPMDAO extends GenericDao {

	public abstract List<CasesBPMBind> getAllCasesJbpmBinds();
	
	public abstract List<Object[]> getCasesProcessDefinitions();
}