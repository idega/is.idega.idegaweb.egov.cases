package is.idega.idegaweb.egov.cases.data;


import java.util.Collection;

import javax.ejb.CreateException;
import javax.ejb.FinderException;

import com.idega.data.IDOEntity;
import com.idega.data.IDOFactory;

public class CaseTypeHomeImpl extends IDOFactory implements CaseTypeHome {

	@Override
	public Class getEntityInterfaceClass() {
		return CaseType.class;
	}

	@Override
	public CaseType create() throws CreateException {
		return (CaseType) super.createIDO();
	}

	@Override
	public CaseType findByPrimaryKey(Object pk) throws FinderException {
		return (CaseType) super.findByPrimaryKeyIDO(pk);
	}

	@Override
	public Collection findAll() throws FinderException {
		IDOEntity entity = this.idoCheckOutPooledEntity();
		Collection ids = ((CaseTypeBMPBean) entity).ejbFindAll();
		this.idoCheckInPooledEntity(entity);
		return this.getEntityCollectionForPrimaryKeys(ids);
	}

	@Override
	public CaseType findFirstType() throws FinderException {
		IDOEntity entity = this.idoCheckOutPooledEntity();
		Object pk = ((CaseTypeBMPBean) entity).ejbFindFirstType();
		this.idoCheckInPooledEntity(entity);
		return this.findByPrimaryKey(pk);
	}

	@Override
	public Collection<CaseType> findByName(String typeName) throws FinderException {
		IDOEntity entity = this.idoCheckOutPooledEntity();
		Collection ids = ((CaseTypeBMPBean) entity).ejbFindAllByName(typeName);
		this.idoCheckInPooledEntity(entity);
		return this.getEntityCollectionForPrimaryKeys(ids);
	}
}