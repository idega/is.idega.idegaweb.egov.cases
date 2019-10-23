package is.idega.idegaweb.egov.cases.data;

import java.sql.Date;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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

	private static final long serialVersionUID = -7651342927028274155L;

	@Override
	public Class<GeneralCase> getEntityInterfaceClass() {
		return GeneralCase.class;
	}

	@Override
	public GeneralCase create() throws CreateException {
		return (GeneralCase) super.createIDO();
	}

	@Override
	public GeneralCase findByPrimaryKey(Object pk) throws FinderException {
		return (GeneralCase) super.findByPrimaryKeyIDO(pk);
	}

	@Override
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
	@Override
	public Collection findAllByGroupAndStatuses(Collection groups, String[] statuses, String[] caseHandlers) throws FinderException {
		IDOEntity entity = this.idoCheckOutPooledEntity();
		Collection ids = ((GeneralCaseBMPBean) entity).ejbFindAllByGroupAndStatuses(groups, statuses, caseHandlers);
		this.idoCheckInPooledEntity(entity);
		return this.getEntityCollectionForPrimaryKeys(ids);
	}

	@Override
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
	@Override
	public Collection findAllByHandlerAndStatuses(User handler, String[] statuses, String[] caseHandlers) throws FinderException {
		IDOEntity entity = this.idoCheckOutPooledEntity();
		Collection ids = ((GeneralCaseBMPBean) entity).ejbFindAllByHandlerAndStatuses(handler, statuses, caseHandlers);
		this.idoCheckInPooledEntity(entity);
		return this.getEntityCollectionForPrimaryKeys(ids);
	}

	@Override
	public Collection findAllByUsers(Collection users) throws FinderException {
		IDOEntity entity = this.idoCheckOutPooledEntity();
		Collection ids = ((GeneralCaseBMPBean) entity).ejbFindAllByUsers(users);
		this.idoCheckInPooledEntity(entity);
		return this.getEntityCollectionForPrimaryKeys(ids);
	}

	@Override
	public Collection<GeneralCase> findAllByIds(Collection<Integer> ids) throws FinderException {
		IDOEntity entity = this.idoCheckOutPooledEntity();
		Collection resultIds = ((GeneralCaseBMPBean) entity).ejbFindAllByIds(ids);
		this.idoCheckInPooledEntity(entity);
		return this.getEntityCollectionForPrimaryKeys(resultIds);
	}

	@Override
	public Collection findAllByMessage(String message) throws FinderException {
		IDOEntity entity = this.idoCheckOutPooledEntity();
		Collection ids = ((GeneralCaseBMPBean) entity)
				.ejbFindAllByMessage(message);
		this.idoCheckInPooledEntity(entity);
		return this.getEntityCollectionForPrimaryKeys(ids);
	}

	@Override
	public Collection findByCriteria(CaseCategory parentCategory, CaseCategory category, CaseType type, CaseStatus status, Boolean anonymous) throws FinderException {
		return findByCriteria(parentCategory, category, type, status, anonymous, null);
	}

	@Override
	public Collection findByCriteria(CaseCategory parentCategory, CaseCategory category, CaseType type, CaseStatus status, Date fromDate, Date toDate, Boolean anonymous) throws FinderException {
		IDOEntity entity = this.idoCheckOutPooledEntity();
		Collection ids = ((GeneralCaseBMPBean) entity).ejbFindByCriteria(
				parentCategory, category, type, status, fromDate, toDate,
				anonymous);
		this.idoCheckInPooledEntity(entity);
		return this.getEntityCollectionForPrimaryKeys(ids);
	}

	@Override
	public Collection findByCriteria(CaseCategory parentCategory, CaseCategory category, CaseType type, CaseStatus status, Boolean anonymous, String caseHandler) throws FinderException {
		IDOEntity entity = this.idoCheckOutPooledEntity();
		Collection ids = ((GeneralCaseBMPBean) entity).ejbFindByCriteria(parentCategory, category, type, status, anonymous, caseHandler);
		this.idoCheckInPooledEntity(entity);
		return this.getEntityCollectionForPrimaryKeys(ids);
	}

	@Override
	public int getCountByGroup(Collection groups) throws IDOException {
		IDOEntity entity = this.idoCheckOutPooledEntity();
		int theReturn = ((GeneralCaseBMPBean) entity).ejbHomeGetCountByGroup(groups);
		this.idoCheckInPooledEntity(entity);
		return theReturn;
	}

	@Override
	public int getCountByGroupAndStatuses(Collection groups, String[] statuses) throws IDOException {
		IDOEntity entity = this.idoCheckOutPooledEntity();
		int theReturn = ((GeneralCaseBMPBean) entity).ejbHomeGetCountByGroupAndStatuses(groups, statuses);
		this.idoCheckInPooledEntity(entity);
		return theReturn;
	}

	@Override
	public Collection<Integer> getCasesIDsByCriteria(String caseNumber,
			String description, Collection<String> owners, String[] statuses,
			IWTimestamp dateFrom, IWTimestamp dateTo, User owner,
			Collection<Group> groups, boolean simpleCases)
			throws FinderException {

		IDOEntity entity = this.idoCheckOutPooledEntity();
		Collection<Integer> ids = ((GeneralCaseBMPBean) entity).ejbFindByCriteria(caseNumber, description, owners, statuses, dateFrom, dateTo, owner, groups, simpleCases);
		this.idoCheckInPooledEntity(entity);
		return ids;
	}

	@Override
	public Collection<Integer> getCasesIDsByCriteria(String caseNumber,
			String description, Collection<String> owners, String[] statuses,
			IWTimestamp dateFrom, IWTimestamp dateTo, User owner,
			Collection<Group> groups, boolean simpleCases, Boolean withHandler,
			List<Integer> exceptOwnersIds
	) throws FinderException {
		return getCasesIDsByCriteria(caseNumber, description, owners, statuses, dateFrom, dateTo, owner, groups, simpleCases, withHandler, exceptOwnersIds, null);
	}

	@Override
	public Collection<Integer> getCasesIDsByCriteria(String caseNumber, String description, Collection<String> owners, String[] statuses,
			IWTimestamp dateFrom, IWTimestamp dateTo, User owner, Collection<Group> groups, boolean simpleCases, Boolean withHandler, List<Integer> exceptOwnersIds, String caseCode) throws FinderException {
		IDOEntity entity = this.idoCheckOutPooledEntity();
		Collection<Integer> ids = ((GeneralCaseBMPBean) entity).ejbFindByCriteria(caseNumber, description, owners, statuses, dateFrom, dateTo, owner, groups, simpleCases, withHandler, exceptOwnersIds, caseCode);
		this.idoCheckInPooledEntity(entity);
		return ids;
	}

	@Override
	public Collection<Case> getCasesByCriteria(String caseNumber, String description, Collection<String> owners, String[] statuses,
			IWTimestamp dateFrom, IWTimestamp dateTo, User owner, Collection<Group> groups, boolean simpleCases) throws FinderException {

		Collection<Integer> ids = getCasesIDsByCriteria(caseNumber, description, owners, statuses, dateFrom, dateTo, owner, groups, simpleCases);
		return this.getEntityCollectionForPrimaryKeys(ids);
	}

	@Override
	public Collection<Case> getCasesByIds(Collection<Integer> ids) throws FinderException {
		IDOEntity entity = this.idoCheckOutPooledEntity();
		this.idoCheckInPooledEntity(entity);
		return this.getEntityCollectionForPrimaryKeys(ids);
	}

	private Logger getLogger(){
		return Logger.getLogger(GeneralCaseHomeImpl.class.getName());
	}
	@Override
	public Collection<Case> getCasesWithONeToOneAttachments(int start,int max){
		try {
			IDOEntity entity = this.idoCheckOutPooledEntity();
			Collection<Integer> ids = ((GeneralCaseBMPBean) entity).getCasesWithONeToOneAttachments(start, max);
			this.idoCheckInPooledEntity(entity);
			return this.getEntityCollectionForPrimaryKeys(ids);
		} catch (FinderException e) {
			getLogger().log(Level.WARNING, "Failed getting attachments", e);
		}
		return Collections.emptyList();
	}
}