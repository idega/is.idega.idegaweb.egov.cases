package is.idega.idegaweb.egov.cases.data;

import java.util.List;

import com.idega.core.persistence.GenericDao;

public interface TSOCDAO extends GenericDao {

	public static final String BEAN_NAME = "tsocdao";

	public List<TimeSpentOnCase> getTimeSpentOnCaseList(Integer userId, Integer caseId);

	public void saveTimeSpentOnCase(TimeSpentOnCase time);
}
