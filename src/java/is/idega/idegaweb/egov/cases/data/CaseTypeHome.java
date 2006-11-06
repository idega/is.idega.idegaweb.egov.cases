package is.idega.idegaweb.egov.cases.data;


import java.util.Collection;
import javax.ejb.CreateException;
import com.idega.data.IDOHome;
import javax.ejb.FinderException;

public interface CaseTypeHome extends IDOHome {

	public CaseType create() throws CreateException;

	public CaseType findByPrimaryKey(Object pk) throws FinderException;

	public Collection findAll() throws FinderException;

	public CaseType findFirstType() throws FinderException;
}