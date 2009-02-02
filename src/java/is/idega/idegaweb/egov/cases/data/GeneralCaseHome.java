package is.idega.idegaweb.egov.cases.data;


import java.sql.Date;
import java.util.Collection;
import javax.ejb.CreateException;
import javax.ejb.FinderException;

import com.idega.block.process.data.Case;
import com.idega.block.process.data.CaseStatus;
import com.idega.data.IDOException;
import com.idega.data.IDOHome;
import com.idega.user.data.Group;
import com.idega.user.data.User;
import com.idega.util.IWTimestamp;

public interface GeneralCaseHome extends IDOHome {

	public GeneralCase create() throws CreateException;

	public GeneralCase findByPrimaryKey(Object pk) throws FinderException;

	public Collection findAllByGroup(Collection groups) throws FinderException;

	/**
	 * 
	 * @param groups
	 * @param statuses
	 * @param caseHandlers - if caseHandlers is null, then it is not added to criteria list, but if it's empty, then the criteria is considered to be IS NULL
	 * @return
	 * @throws FinderException
	 */
	public Collection findAllByGroupAndStatuses(Collection groups, String[] statuses, String[] caseHandlers) throws FinderException;
	
	public Collection findAllByHandler(User handler) throws FinderException;

	/**
	 * 
	 * @param handler
	 * @param statuses
	 * @param caseHandlers - if caseHandlers is null, then it is not added to criteria list, but if it's empty, then the criteria is considered to be IS NULL
	 * @return
	 * @throws FinderException
	 */
	public Collection findAllByHandlerAndStatuses(User handler, String[] statuses, String[] caseHandlers) throws FinderException;

	public Collection findAllByUsers(Collection users) throws FinderException;
	
	public Collection findAllByMessage(String message) throws FinderException;

	public Collection findByCriteria(CaseCategory parentCategory, CaseCategory category, CaseType type, CaseStatus status, Date fromDate, Date toDate, Boolean anonymous) throws FinderException;
			
	public Collection findByCriteria(CaseCategory parentCategory, CaseCategory category, CaseType type, CaseStatus status, Boolean anonymous) throws FinderException;
	
	public Collection findByCriteria(CaseCategory parentCategory, CaseCategory category, CaseType type, CaseStatus status, Boolean anonymous, String caseHandler) throws FinderException;

	public int getCountByGroup(Collection groups) throws IDOException;

	public int getCountByGroupAndStatuses(Collection groups, String[] statuses) throws IDOException;
	
	public abstract Collection<GeneralCase> findAllByIds(Collection<Integer> ids) throws FinderException;
	
	public Collection<Case> getCasesByCriteria(String caseNumber, String description, Collection<String> owners, String[] statuses,
			IWTimestamp dateFrom, IWTimestamp dateTo, User owner, Collection<Group> groups, boolean simpleCases) throws FinderException;
	
	public Collection<Case> getCasesByIds(Collection<Integer> ids) throws FinderException;
}