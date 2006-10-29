package is.idega.idegaweb.egov.cases.data;


import java.util.Collection;
import javax.ejb.CreateException;
import javax.ejb.FinderException;
import com.idega.data.IDOEntity;
import com.idega.data.IDOFactory;

public class CaseCategoryHomeImpl extends IDOFactory implements CaseCategoryHome {

	public Class getEntityInterfaceClass() {
		return CaseCategory.class;
	}

	public CaseCategory create() throws CreateException {
		return (CaseCategory) super.createIDO();
	}

	public CaseCategory findByPrimaryKey(Object pk) throws FinderException {
		return (CaseCategory) super.findByPrimaryKeyIDO(pk);
	}

	public Collection findAll() throws FinderException {
		IDOEntity entity = this.idoCheckOutPooledEntity();
		Collection ids = ((CaseCategoryBMPBean) entity).ejbFindAll();
		this.idoCheckInPooledEntity(entity);
		return this.getEntityCollectionForPrimaryKeys(ids);
	}

	public Collection findAllTopLevelCategories() throws FinderException {
		IDOEntity entity = this.idoCheckOutPooledEntity();
		Collection ids = ((CaseCategoryBMPBean) entity).ejbFindAllTopLevelCategories();
		this.idoCheckInPooledEntity(entity);
		return this.getEntityCollectionForPrimaryKeys(ids);
	}

	public Collection findAllSubCategories(CaseCategory category) throws FinderException {
		IDOEntity entity = this.idoCheckOutPooledEntity();
		Collection ids = ((CaseCategoryBMPBean) entity).ejbFindAllSubCategories(category);
		this.idoCheckInPooledEntity(entity);
		return this.getEntityCollectionForPrimaryKeys(ids);
	}
}