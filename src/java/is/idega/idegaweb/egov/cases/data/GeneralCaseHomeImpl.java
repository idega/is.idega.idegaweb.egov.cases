package is.idega.idegaweb.egov.cases.data;


import com.idega.data.IDOException;
import java.util.Collection;
import com.idega.block.process.data.CaseStatus;
import javax.ejb.CreateException;
import javax.ejb.FinderException;
import java.sql.Date;
import com.idega.user.data.User;
import com.idega.data.IDOEntity;
import com.idega.data.IDOFactory;

public class GeneralCaseHomeImpl extends IDOFactory implements GeneralCaseHome {

	public Class getEntityInterfaceClass() {
		return GeneralCase.class;
	}

	public GeneralCase create() throws CreateException {
		return (GeneralCase) super.createIDO();
	}

	public GeneralCase findByPrimaryKey(Object pk) throws FinderException {
		return (GeneralCase) super.findByPrimaryKeyIDO(pk);
	}

	public Collection findAllByGroup(Collection groups) throws FinderException {
		IDOEntity entity = this.idoCheckOutPooledEntity();
		Collection ids = ((GeneralCaseBMPBean) entity).ejbFindAllByGroup(groups);
		this.idoCheckInPooledEntity(entity);
		return this.getEntityCollectionForPrimaryKeys(ids);
	}

	public Collection findAllByGroupAndStatuses(Collection groups, String[] statuses) throws FinderException {
		IDOEntity entity = this.idoCheckOutPooledEntity();
		Collection ids = ((GeneralCaseBMPBean) entity).ejbFindAllByGroupAndStatuses(groups, statuses);
		this.idoCheckInPooledEntity(entity);
		return this.getEntityCollectionForPrimaryKeys(ids);
	}

	public Collection findAllByHandler(User handler) throws FinderException {
		IDOEntity entity = this.idoCheckOutPooledEntity();
		Collection ids = ((GeneralCaseBMPBean) entity).ejbFindAllByHandler(handler);
		this.idoCheckInPooledEntity(entity);
		return this.getEntityCollectionForPrimaryKeys(ids);
	}

	public Collection findAllByHandlerAndStatuses(User handler, String[] statuses) throws FinderException {
		IDOEntity entity = this.idoCheckOutPooledEntity();
		Collection ids = ((GeneralCaseBMPBean) entity).ejbFindAllByHandlerAndStatuses(handler, statuses);
		this.idoCheckInPooledEntity(entity);
		return this.getEntityCollectionForPrimaryKeys(ids);
	}

	public Collection findAllByUsers(Collection users) throws FinderException {
		IDOEntity entity = this.idoCheckOutPooledEntity();
		Collection ids = ((GeneralCaseBMPBean) entity).ejbFindAllByUsers(users);
		this.idoCheckInPooledEntity(entity);
		return this.getEntityCollectionForPrimaryKeys(ids);
	}

	public Collection findByCriteria(CaseCategory parentCategory, CaseCategory category, CaseType type, CaseStatus status, Date fromDate, Date toDate, Boolean anonymous) throws FinderException {
		IDOEntity entity = this.idoCheckOutPooledEntity();
		Collection ids = ((GeneralCaseBMPBean) entity).ejbFindByCriteria(parentCategory, category, type, status, fromDate, toDate, anonymous);
		this.idoCheckInPooledEntity(entity);
		return this.getEntityCollectionForPrimaryKeys(ids);
	}

	public int getCountByGroup(Collection groups) throws IDOException {
		IDOEntity entity = this.idoCheckOutPooledEntity();
		int theReturn = ((GeneralCaseBMPBean) entity).ejbHomeGetCountByGroup(groups);
		this.idoCheckInPooledEntity(entity);
		return theReturn;
	}

	public int getCountByGroupAndStatuses(Collection groups, String[] statuses) throws IDOException {
		IDOEntity entity = this.idoCheckOutPooledEntity();
		int theReturn = ((GeneralCaseBMPBean) entity).ejbHomeGetCountByGroupAndStatuses(groups, statuses);
		this.idoCheckInPooledEntity(entity);
		return theReturn;
	}
}