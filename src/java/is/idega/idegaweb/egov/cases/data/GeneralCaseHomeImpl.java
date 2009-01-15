package is.idega.idegaweb.egov.cases.data;


import java.sql.Date;
import java.util.Collection;
import javax.ejb.CreateException;
import javax.ejb.FinderException;

import com.idega.block.process.data.Case;
import com.idega.block.process.data.CaseStatus;
import com.idega.data.IDOEntity;
import com.idega.data.IDOException;
import com.idega.data.IDOFactory;
import com.idega.user.data.Group;
import com.idega.user.data.User;
import com.idega.util.IWTimestamp;

public class GeneralCaseHomeImpl extends IDOFactory implements GeneralCaseHome {

	@Override
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

	/**
	 * 
	 * @param groups
	 * @param statuses
	 * @param caseHandlers - if caseHandlers is null, then it is not added to criteria list, but if it's empty, then the criteria is considered to be IS NULL
	 * @return
	 * @throws FinderException
	 */
	public Collection findAllByGroupAndStatuses(Collection groups, String[] statuses, String[] caseHandlers) throws FinderException {
		IDOEntity entity = this.idoCheckOutPooledEntity();
		Collection ids = ((GeneralCaseBMPBean) entity).ejbFindAllByGroupAndStatuses(groups, statuses, caseHandlers);
		this.idoCheckInPooledEntity(entity);
		return this.getEntityCollectionForPrimaryKeys(ids);
	}

	public Collection findAllByHandler(User handler) throws FinderException {
		IDOEntity entity = this.idoCheckOutPooledEntity();
		Collection ids = ((GeneralCaseBMPBean) entity).ejbFindAllByHandler(handler);
		this.idoCheckInPooledEntity(entity);
		return this.getEntityCollectionForPrimaryKeys(ids);
	}

	/**
	 * 
	 * @param handler
	 * @param statuses
	 * @param caseHandlers - if caseHandlers is null, then it is not added to criteria list, but if it's empty, then the criteria is considered to be IS NULL
	 * @return
	 * @throws FinderException
	 */
	public Collection findAllByHandlerAndStatuses(User handler, String[] statuses, String[] caseHandlers) throws FinderException {
		IDOEntity entity = this.idoCheckOutPooledEntity();
		Collection ids = ((GeneralCaseBMPBean) entity).ejbFindAllByHandlerAndStatuses(handler, statuses, caseHandlers);
		this.idoCheckInPooledEntity(entity);
		return this.getEntityCollectionForPrimaryKeys(ids);
	}

	public Collection findAllByUsers(Collection users) throws FinderException {
		IDOEntity entity = this.idoCheckOutPooledEntity();
		Collection ids = ((GeneralCaseBMPBean) entity).ejbFindAllByUsers(users);
		this.idoCheckInPooledEntity(entity);
		return this.getEntityCollectionForPrimaryKeys(ids);
	}
	
	public Collection<GeneralCase> findAllByIds(Collection<Integer> ids) throws FinderException {
		IDOEntity entity = this.idoCheckOutPooledEntity();
		Collection resultIds = ((GeneralCaseBMPBean) entity).ejbFindAllByIds(ids);
		this.idoCheckInPooledEntity(entity);
		return this.getEntityCollectionForPrimaryKeys(resultIds);
	}
	
	public Collection findAllByMessage(String message) throws FinderException {
		IDOEntity entity = this.idoCheckOutPooledEntity();
		Collection ids = ((GeneralCaseBMPBean) entity)
				.ejbFindAllByMessage(message);
		this.idoCheckInPooledEntity(entity);
		return this.getEntityCollectionForPrimaryKeys(ids);
	}
	
	public Collection findByCriteria(CaseCategory parentCategory, CaseCategory category, CaseType type, CaseStatus status, Boolean anonymous) throws FinderException {
		return findByCriteria(parentCategory, category, type, status, anonymous, null);
	}
	
	public Collection findByCriteria(CaseCategory parentCategory, CaseCategory category, CaseType type, CaseStatus status, Date fromDate, Date toDate, Boolean anonymous) throws FinderException {
		IDOEntity entity = this.idoCheckOutPooledEntity();
		Collection ids = ((GeneralCaseBMPBean) entity).ejbFindByCriteria(
				parentCategory, category, type, status, fromDate, toDate,
				anonymous);
		this.idoCheckInPooledEntity(entity);
		return this.getEntityCollectionForPrimaryKeys(ids);
	}
	
	public Collection findByCriteria(CaseCategory parentCategory, CaseCategory category, CaseType type, CaseStatus status, Boolean anonymous, String caseHandler) throws FinderException {
		IDOEntity entity = this.idoCheckOutPooledEntity();
		Collection ids = ((GeneralCaseBMPBean) entity).ejbFindByCriteria(parentCategory, category, type, status, anonymous, caseHandler);
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
	
	public Collection<Case> getCasesByCriteria(String caseNumber, String description, Collection<String> owners, String[] statuses,
			IWTimestamp dateFrom, IWTimestamp dateTo, User owner, Collection<Group> groups, boolean simpleCases) throws FinderException {
		IDOEntity entity = this.idoCheckOutPooledEntity();
		Collection ids = ((GeneralCaseBMPBean) entity).ejbFindByCriteria(caseNumber, description, owners, statuses, dateFrom, dateTo, owner, groups, simpleCases);
		this.idoCheckInPooledEntity(entity);
		return this.getEntityCollectionForPrimaryKeys(ids);
	}

	public Collection<Case> getCasesByIds(Collection<Integer> ids) throws FinderException {
		IDOEntity entity = this.idoCheckOutPooledEntity();
		this.idoCheckInPooledEntity(entity);
		return this.getEntityCollectionForPrimaryKeys(ids);
	}
}