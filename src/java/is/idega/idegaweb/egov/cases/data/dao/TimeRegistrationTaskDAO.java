package is.idega.idegaweb.egov.cases.data.dao;

import java.util.List;

import com.idega.core.persistence.GenericDao;

import is.idega.idegaweb.egov.cases.data.bean.TimeRegistrationTask;

public interface TimeRegistrationTaskDAO extends GenericDao {

	static final String BEAN_NAME = "timeRegistrationTaskDAO";

	public TimeRegistrationTask getById(Long id);

	public TimeRegistrationTask store(TimeRegistrationTask timeRegistrationTask);

	public List<TimeRegistrationTask> getAll();

	public List<TimeRegistrationTask> getAllTopLevel();

	public List<TimeRegistrationTask> getByParent(TimeRegistrationTask parent);


}
