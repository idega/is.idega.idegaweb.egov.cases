package com.idega.idegaweb.egov.cases.bpm.data;

import java.util.List;

import com.idega.core.persistence.GenericDao;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.1 $
 *
 * Last modified: $Date: 2008/01/30 14:32:16 $ by $Author: civilis $
 */
public interface CasesBPMDAO extends GenericDao {

	public abstract List<CasesBPMBind> getAllCasesJbpmBinds();
	
	public abstract List<Object[]> getSimpleProcessDefinitions();
}