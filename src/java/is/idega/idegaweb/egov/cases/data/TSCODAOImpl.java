package is.idega.idegaweb.egov.cases.data;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.idega.core.persistence.Param;
import com.idega.core.persistence.impl.GenericDaoImpl;
import com.idega.presentation.IWContext;

@Scope(BeanDefinition.SCOPE_SINGLETON)
@Repository(TSOCDAO.BEAN_NAME)
@Transactional(readOnly = true)
public class TSCODAOImpl extends GenericDaoImpl implements TSOCDAO {
	@Override
	public List<TimeSpentOnCase> getTimeSpentOnCaseList(Integer userId, Integer caseId) {
		return getResultList(TimeSpentOnCase.getByCaseAndUser, TimeSpentOnCase.class, new Param(TimeSpentOnCase.CaseIdProp, new Long(caseId)),  new Param(TimeSpentOnCase.UserIdProp, userId));
	}

	@Override
	public List<TimeSpentOnCase> getTimeSpentOnCaseList(Integer caseId) {
		return getResultList(TimeSpentOnCase.getByCase, TimeSpentOnCase.class, new Param(TimeSpentOnCase.CaseIdProp, new Long(caseId)));
	}

	@Override
	public List<TimeSpentOnCase> getTimeSpentOnCaseList(String caseUuid) {
		return getResultList(TimeSpentOnCase.getByCaseUuid, TimeSpentOnCase.class, new Param(TimeSpentOnCase.CaseUuidProp, new String(caseUuid)));
	}

	@Override
	@Transactional(readOnly = false)
	public void saveTimeSpentOnCase(TimeSpentOnCase time){
		if (time == null) return;
		if (time.getId()!= null) merge(time);
		else persist(time);
	}

	@Override
	public List<TimeSpentOnCase> getActiveTimeSpentOnCaseListForUser(Integer userId) {
		return getResultList(TimeSpentOnCase.getAllActiveForUser, TimeSpentOnCase.class,  new Param(TimeSpentOnCase.UserIdProp, userId));
	}

	@Override
	@Transactional(readOnly = false)
	public void removeTimeSpentOnCase(TimeSpentOnCase time) {
		if (time == null) return;
		time.setRemovedBy(IWContext.getCurrentInstance().getLoggedInUser());
		time.setRemoved(new Timestamp(System.currentTimeMillis()));
		saveTimeSpentOnCase(time);
	}

	@Override
	@Transactional(readOnly = false)
	public void removeTimeSpentOnCase(Long id) {
		TimeSpentOnCase time = getSingleResultByInlineQuery("FROM TimeSpentOnCase tsoc WHERE tsoc.id = :id", TimeSpentOnCase.class, new Param("id", id));
		removeTimeSpentOnCase(time);
	}
}
