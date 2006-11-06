package is.idega.idegaweb.egov.cases.data;


import java.util.Collection;
import javax.ejb.CreateException;
import javax.ejb.FinderException;
import com.idega.data.IDOEntity;
import com.idega.data.IDOFactory;

public class CaseTypeHomeImpl extends IDOFactory implements CaseTypeHome {

	public Class getEntityInterfaceClass() {
		return CaseType.class;
	}

	public CaseType create() throws CreateException {
		return (CaseType) super.createIDO();
	}

	public CaseType findByPrimaryKey(Object pk) throws FinderException {
		return (CaseType) super.findByPrimaryKeyIDO(pk);
	}

	public Collection findAll() throws FinderException {
		IDOEntity entity = this.idoCheckOutPooledEntity();
		Collection ids = ((CaseTypeBMPBean) entity).ejbFindAll();
		this.idoCheckInPooledEntity(entity);
		return this.getEntityCollectionForPrimaryKeys(ids);
	}

	public CaseType findFirstType() throws FinderException {
		IDOEntity entity = this.idoCheckOutPooledEntity();
		Object pk = ((CaseTypeBMPBean) entity).ejbFindFirstType();
		this.idoCheckInPooledEntity(entity);
		return this.findByPrimaryKey(pk);
	}
}