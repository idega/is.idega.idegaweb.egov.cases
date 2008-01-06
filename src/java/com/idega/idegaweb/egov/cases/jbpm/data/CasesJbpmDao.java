package com.idega.idegaweb.egov.cases.jbpm.data;

import java.util.List;

import com.idega.core.persistence.GenericDao;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.1 $
 *
 * Last modified: $Date: 2008/01/06 17:00:21 $ by $Author: civilis $
 */
public interface CasesJbpmDao extends GenericDao {

	public abstract List<CasesJbpmBind> getAllCasesJbpmBinds();
	
	public abstract List<Object[]> getSimpleProcessDefinitions();
}