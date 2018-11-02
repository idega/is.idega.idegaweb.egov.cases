package is.idega.idegaweb.egov.cases.business;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.idega.builder.bean.AdvancedProperty;
import com.idega.user.dao.UserDAO;
import com.idega.user.data.bean.User;
import com.idega.util.ListUtil;
import com.idega.util.expression.ELUtil;

import is.idega.idegaweb.egov.cases.data.bean.TimeSpentOnCase;
import is.idega.idegaweb.egov.cases.data.dao.TSOCDAO;

@Scope(BeanDefinition.SCOPE_SINGLETON)
@Service(TimeSpentOnCaseManager.BEAN_NAME)
public class TimeSpentOnCaseManager implements TSOCManager {

	public static final String BEAN_NAME = "TimeSpentOnCaseManager";

	@Autowired
	private TSOCDAO tsocdao;

	@Autowired
	private UserDAO userDao;

	private UserDAO getUserDAO() {
		if (userDao == null) ELUtil.getInstance().autowire(this);
		return userDao;
	}

	private TSOCDAO getTsocdao() {
		if (tsocdao == null) ELUtil.getInstance().autowire(this);
		return tsocdao;
	}

	@Override
	public void stopWorkingOnAllCases(Integer userId) {
		List<TimeSpentOnCase> tsocList = getTsocdao().getActiveTimeSpentOnCaseListForUser(userId);
		for (TimeSpentOnCase tsoc: tsocList){
			stopWorkingOnCase(userId, new Integer(tsoc.getBpmCase().toString()));
		}
	}

	/* (non-Javadoc)
	 * @see is.idega.idegaweb.egov.bpm.business.TSOCManager#startWorkingOnCase(java.lang.Integer, java.lang.Integer)
	 */
	@Override
	public Long startWorkingOnCase(Integer userId, Integer caseId) {
		stopWorkingOnAllCases(userId);
		List<TimeSpentOnCase> tsocList = getTsocdao().getTimeSpentOnCaseList(userId, caseId);
		Long currentDuration = 0L;
		if (ListUtil.isEmpty(tsocList)){
			TimeSpentOnCase tsoc = new TimeSpentOnCase();
			tsoc.setBpmCase(new Long(caseId.longValue()));
			User user = getUserDAO().getUser(userId);
			tsoc.setUser(user);
			tsoc.setStart(new Timestamp(new java.util.Date().getTime()));
			getTsocdao().saveTimeSpentOnCase(tsoc);
		} else {
			for (int i = 0; i < tsocList.size(); i++){
				if (i != tsocList.size()-1){
					TimeSpentOnCase tsoc = tsocList.get(i);
					if (tsoc.getEnd()==null){
						tsoc.setEnd(tsocList.get(i+1).getStart());
						tsoc.setDuration(tsoc.getEnd().getTime() - tsoc.getStart().getTime());
						getTsocdao().saveTimeSpentOnCase(tsoc);
					}
					currentDuration += tsoc.getDuration();
				} else {
					TimeSpentOnCase tsoc = tsocList.get(i);
					if (tsoc.getEnd()==null){
						currentDuration += (new java.util.Date().getTime()-tsoc.getStart().getTime());
					} else {
						tsoc = new TimeSpentOnCase();
						tsoc.setBpmCase(new Long(caseId.longValue()));
						User user = getUserDAO().getUser(userId);
						tsoc.setUser(user);
						tsoc.setStart(new Timestamp(new java.util.Date().getTime()));
						getTsocdao().saveTimeSpentOnCase(tsoc);
					}
				}
			}
		}
		return getCurrentState(caseId);
		//return currentDuration;
	}

	/* (non-Javadoc)
	 * @see is.idega.idegaweb.egov.bpm.business.TSOCManager#stopWorkingOnCase(java.lang.Integer, java.lang.Integer)
	 */
	@Override
	public void stopWorkingOnCase(Integer userId, Integer caseId) {
		List<TimeSpentOnCase> tsocList = getTsocdao().getTimeSpentOnCaseList(userId, caseId);
		if (ListUtil.isEmpty(tsocList)){

		} else {
			for (int i = 0; i < tsocList.size(); i++){
				if (i != tsocList.size()-1){
					TimeSpentOnCase tsoc = tsocList.get(i);
					if (tsoc.getEnd()==null){
						tsoc.setEnd(tsocList.get(i+1).getStart());
						tsoc.setDuration(tsoc.getEnd().getTime() - tsoc.getStart().getTime());
						getTsocdao().saveTimeSpentOnCase(tsoc);
					}
				} else {
					TimeSpentOnCase tsoc = tsocList.get(i);
					tsoc.setEnd(new Timestamp(new java.util.Date().getTime()));
					tsoc.setDuration(tsoc.getEnd().getTime() - tsoc.getStart().getTime());
					getTsocdao().saveTimeSpentOnCase(tsoc);
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see is.idega.idegaweb.egov.bpm.business.TSOCManager#getCurrentState(java.lang.Integer, java.lang.Integer)
	 */
	@Override
	public AdvancedProperty getCurrentState(Integer userId, Integer caseId){
		List<TimeSpentOnCase> tsocList = getTsocdao().getTimeSpentOnCaseList(userId, caseId);
		Long currentDuration = 0L;
		Boolean isActive = Boolean.FALSE;
		if (ListUtil.isEmpty(tsocList)){

		} else {
			for (int i = 0; i < tsocList.size(); i++){
				if (i != tsocList.size()-1){
					TimeSpentOnCase tsoc = tsocList.get(i);
					if (tsoc.getEnd()==null){
						tsoc.setEnd(tsocList.get(i+1).getStart());
						tsoc.setDuration(tsoc.getEnd().getTime() - tsoc.getStart().getTime());
						getTsocdao().saveTimeSpentOnCase(tsoc);
					}
					currentDuration += tsoc.getDuration();
				} else {
					TimeSpentOnCase tsoc = tsocList.get(i);
					if (tsoc.getEnd()==null){
						isActive = Boolean.TRUE;
						currentDuration += (new java.util.Date().getTime() - tsoc.getStart().getTime());
					} else {
						currentDuration += tsoc.getDuration();
					}
				}
			}
		}
		return new AdvancedProperty(currentDuration, isActive);
	}

	@Override
	public Long getCurrentState(Integer caseId){
		List<TimeSpentOnCase> tsocList = getTsocdao().getTimeSpentOnCaseList(caseId);
		Long currentDuration = 0L;
		if (ListUtil.isEmpty(tsocList)){

		} else {
			for (int i = 0; i < tsocList.size(); i++){
				TimeSpentOnCase tsoc = tsocList.get(i);
				if (tsoc.getEnd()==null){
					currentDuration += (new java.util.Date().getTime() - tsoc.getStart().getTime());
				} else {
					currentDuration += tsoc.getDuration();
				}
			}
		}
		return currentDuration;
	}

}
