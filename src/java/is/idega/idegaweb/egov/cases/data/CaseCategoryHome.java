package is.idega.idegaweb.egov.cases.data;


import java.util.Collection;
import javax.ejb.CreateException;
import com.idega.data.IDOHome;
import javax.ejb.FinderException;

public interface CaseCategoryHome extends IDOHome {

	public CaseCategory create() throws CreateException;

	public CaseCategory findByPrimaryKey(Object pk) throws FinderException;

	public Collection findAll() throws FinderException;

	public Collection findAllTopLevelCategories() throws FinderException;

	public Collection findAllSubCategories(CaseCategory category) throws FinderException;
}