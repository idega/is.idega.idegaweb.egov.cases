package is.idega.idegaweb.egov.cases.data.dao.impl;

import java.util.List;
import java.util.logging.Level;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.idega.core.persistence.Param;
import com.idega.core.persistence.impl.GenericDaoImpl;

import is.idega.idegaweb.egov.cases.data.bean.TimeRegistrationTask;
import is.idega.idegaweb.egov.cases.data.dao.TimeRegistrationTaskDAO;

@Repository(TimeRegistrationTaskDAO.BEAN_NAME)
@Scope(BeanDefinition.SCOPE_SINGLETON)
@Transactional(readOnly = false)
public class TimeRegistrationTaskDAOImpl extends GenericDaoImpl implements TimeRegistrationTaskDAO {

	@Override
	public TimeRegistrationTask getById(Long id) {
		if (id == null) {
			return null;
		}

		try {
			return getSingleResult(TimeRegistrationTask.QUERY_GET_BY_ID, TimeRegistrationTask.class, new Param(TimeRegistrationTask.PARAM_ID, id));
		} catch (Exception e) {
			getLogger().log(Level.WARNING, "Error getting time registration task by id: " + id, e);
		}

		return null;
	}

	@Override
	@Transactional(readOnly = false)
	public TimeRegistrationTask store(TimeRegistrationTask timeRegistrationTask) {
		if (timeRegistrationTask == null) {
			return null;
		}

		if (timeRegistrationTask.getId() == null) {
			persist(timeRegistrationTask);
		} else {
			merge(timeRegistrationTask);
		}

		return timeRegistrationTask.getId() == null ? null : timeRegistrationTask;
	}

	@Override
	public List<TimeRegistrationTask> getAll() {
		try {
			return getResultList(TimeRegistrationTask.QUERY_GET_ALL, TimeRegistrationTask.class);
		} catch (Exception e) {
			getLogger().log(Level.WARNING, "Error getting all time registration tasks.", e);
		}

		return null;
	}

	@Override
	public List<TimeRegistrationTask> getAllTopLevel() {
		try {
			return getResultList(TimeRegistrationTask.QUERY_GET_ALL_TOP_LEVEL, TimeRegistrationTask.class);
		} catch (Exception e) {
			getLogger().log(Level.WARNING, "Error getting all top level time registration tasks.", e);
		}

		return null;
	}

	@Override
	public List<TimeRegistrationTask> getByParent(TimeRegistrationTask parent) {
		if (parent == null || parent.getId() == null) {
			return null;
		}

		try {
			return getResultList(TimeRegistrationTask.QUERY_GET_BY_PARENT_ID, TimeRegistrationTask.class, new Param(TimeRegistrationTask.PARAM_PARENT_ID, parent.getId()));
		} catch (Exception e) {
			getLogger().log(Level.WARNING, "Error getting time registration task by parent id: " + parent.getId(), e);
		}

		return null;
	}


}