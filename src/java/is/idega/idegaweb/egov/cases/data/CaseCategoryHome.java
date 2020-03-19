package is.idega.idegaweb.egov.cases.data;


import java.util.Collection;

import javax.ejb.CreateException;
import javax.ejb.FinderException;

import com.idega.data.IDOHome;

public interface CaseCategoryHome extends IDOHome {
	public CaseCategory create() throws CreateException;

	public CaseCategory findByPrimaryKey(Object pk) throws FinderException;

	public Collection findAll() throws FinderException;

	public Collection findAllTopLevelCategories() throws FinderException;

	public Collection findAllTopLevelCategoriesForAdmins();

	public Collection findAllSubCategories(CaseCategory category) throws FinderException;

	public Collection<CaseCategory> findByName(String typeName) throws FinderException;

	public Collection<CaseCategory> findByGroupId(Integer groupId) throws FinderException;

}