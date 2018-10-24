package is.idega.idegaweb.egov.cases.data.dao;

import java.util.List;

import com.idega.core.persistence.GenericDao;

import is.idega.idegaweb.egov.cases.data.bean.TimeSpentOnCase;

public interface TSOCDAO extends GenericDao {

	public static final String BEAN_NAME = "tsocdao";

	public List<TimeSpentOnCase> getTimeSpentOnCaseList(Integer userId, Integer caseId);

	public List<TimeSpentOnCase> getActiveTimeSpentOnCaseListForUser(Integer userId);

	public List<TimeSpentOnCase> getTimeSpentOnCaseList(Integer caseId);

	public TimeSpentOnCase saveTimeSpentOnCase(TimeSpentOnCase time);

	public void removeTimeSpentOnCase(TimeSpentOnCase time);

	public List<TimeSpentOnCase> getTimeSpentOnCaseList(String caseUuid);

	public void removeTimeSpentOnCase(Long id);
}
