package is.idega.idegaweb.egov.cases.data;


import com.idega.data.IDOException;
import java.util.Collection;
import com.idega.block.process.data.CaseStatus;
import javax.ejb.CreateException;
import com.idega.data.IDOHome;
import javax.ejb.FinderException;
import com.idega.user.data.User;

public interface GeneralCaseHome extends IDOHome {

	public GeneralCase create() throws CreateException;

	public GeneralCase findByPrimaryKey(Object pk) throws FinderException;

	public Collection findAllByGroup(Collection groups) throws FinderException;

	public Collection findAllByGroupAndStatuses(Collection groups, String[] statuses) throws FinderException;

	public Collection findAllByHandler(User handler) throws FinderException;

	public Collection findAllByHandlerAndStatuses(User handler, String[] statuses) throws FinderException;

	public Collection findAllByUsers(Collection users) throws FinderException;

	public Collection findByCriteria(CaseCategory parentCategory, CaseCategory category, CaseType type, CaseStatus status, Boolean anonymous) throws FinderException;
	
	public Collection findByCriteria(CaseCategory parentCategory, CaseCategory category, CaseType type, CaseStatus status, Boolean anonymous, Integer jbpmProcessInstanceId) throws FinderException;

	public int getCountByGroup(Collection groups) throws IDOException;

	public int getCountByGroupAndStatuses(Collection groups, String[] statuses) throws IDOException;
}