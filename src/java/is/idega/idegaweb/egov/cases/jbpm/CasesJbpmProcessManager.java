package is.idega.idegaweb.egov.cases.jbpm;

import is.idega.idegaweb.egov.cases.business.CasesBusiness;
import is.idega.idegaweb.egov.cases.data.GeneralCase;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.jbpm.JbpmConfiguration;
import org.jbpm.JbpmContext;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.taskmgmt.exe.TaskInstance;

import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.business.IBORuntimeException;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWMainApplication;
import com.idega.jbpm.exe.Converter;
import com.idega.jbpm.exe.ProcessConstants;
import com.idega.jbpm.exe.VariablesHandler;
import com.idega.jbpm.exe.ViewManager;
import com.idega.presentation.IWContext;
import com.idega.presentation.PresentationObject;
import com.idega.user.business.UserBusiness;
import com.idega.user.data.User;
import com.idega.util.CoreConstants;
import com.idega.util.IWTimestamp;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.6 $
 *
 * Last modified: $Date: 2007/12/05 10:36:15 $ by $Author: civilis $
 */
public class CasesJbpmProcessManager implements com.idega.jbpm.exe.Process {

	private SessionFactory sessionFactory;
	private JbpmConfiguration jbpmConfiguration;
	private VariablesHandler variablesHandler;
	private ViewManager viewManager;
	private Converter converter;
	
	public Converter getConverter() {
		return converter;
	}

	public void setConverter(Converter converter) {
		this.converter = converter;
	}

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public JbpmConfiguration getJbpmConfiguration() {
		return jbpmConfiguration;
	}

	public void setJbpmConfiguration(JbpmConfiguration jbpmConfiguration) {
		this.jbpmConfiguration = jbpmConfiguration;
	}

	public VariablesHandler getVariablesHandler() {
		return variablesHandler;
	}

	public void setVariablesHandler(VariablesHandler variablesHandler) {
		this.variablesHandler = variablesHandler;
	}

	public void startProcess(Map<String, String> parameters, Object submissionData) {
		
		Long processDefinitionId = Long.parseLong(parameters.get(ProcessConstants.PROCESS_DEFINITION_ID));
		int userId = Integer.parseInt(parameters.get(CasesJbpmProcessConstants.userIdActionVariableName));
		Long caseCatId = Long.parseLong(parameters.get(CasesJbpmProcessConstants.caseCategoryIdActionVariableName));
		Long caseTypeId = Long.parseLong(parameters.get(CasesJbpmProcessConstants.caseTypeActionVariableName));
		
		SessionFactory sessionFactory = getSessionFactory();
		
		Transaction transaction = sessionFactory.getCurrentSession().getTransaction();
		boolean transactionWasActive = transaction.isActive();
		
		if(!transactionWasActive)
			transaction.begin();
		
		JbpmContext ctx = getJbpmConfiguration().createJbpmContext();
		ctx.setSession(sessionFactory.getCurrentSession());
		
		try {
			
			ProcessDefinition pd = ctx.getGraphSession().getProcessDefinition(processDefinitionId);
			ProcessInstance pi = new ProcessInstance(pd);
			
			IWContext iwc = IWContext.getIWContext(FacesContext.getCurrentInstance());
			User user = getUserBusiness(iwc).getUser(userId);
			IWMainApplication iwma = iwc.getApplicationContext().getIWMainApplication();
			
			GeneralCase genCase = getCasesBusiness(iwc).storeGeneralCase(user, caseCatId, caseTypeId, /*attachment pk*/null, "This is simple cases-jbpm-formbuilder integration example.", "type", new Long(pi.getId()).intValue(), /*isPrivate*/false, getCasesBusiness(iwc).getIWResourceBundleForUser(user, iwc, iwma.getBundle(PresentationObject.CORE_IW_BUNDLE_IDENTIFIER)));
			
			pi.setStart(new Date());
			
//			moving to 1st task node
			pi.getRootToken().signal();
			
			@SuppressWarnings("unchecked")
			Collection<TaskInstance> tis = pi.getTaskMgmtInstance().getUnfinishedTasks(pi.getRootToken());
			
			if(tis.size() != 1)
				throw new RuntimeException("Fatal: simple cases process definition not correct. First task node comprehends no or more than 1 task . Total: "+tis.size());
			
			TaskInstance taskInstance = tis.iterator().next();
			
			Map<String, Object> caseData = new HashMap<String, Object>();
			caseData.put(CasesJbpmProcessConstants.caseIdVariableName, genCase.getPrimaryKey().toString());
			caseData.put(CasesJbpmProcessConstants.caseTypeNameVariableName, genCase.getCaseType().getName());
			caseData.put(CasesJbpmProcessConstants.caseCategoryNameVariableName, genCase.getCaseCategory().getName());
			caseData.put(CasesJbpmProcessConstants.caseStatusVariableName, genCase.getCaseStatus().getStatus());
			
			IWTimestamp created = new IWTimestamp(genCase.getCreated());
			caseData.put(CasesJbpmProcessConstants.caseCreatedDateVariableName, created.getLocaleDateAndTime(iwc.getCurrentLocale(), IWTimestamp.SHORT, IWTimestamp.SHORT));
			
			getVariablesHandler().submitVariables(caseData, taskInstance.getId());
			submitVariablesAndProceedProcess(taskInstance, submissionData);
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			ctx.close();
			
			if(!transactionWasActive)
				transaction.commit();
		}
	}
	
	protected void submitVariablesAndProceedProcess(TaskInstance ti, Object instance) {
		
		getVariablesHandler().submitVariables(getConverter().convert(instance), ti.getId());
    	
    	String actionTaken = (String)ti.getVariable(CasesJbpmProcessConstants.actionTakenVariableName);
    	
    	if(actionTaken != null && !CoreConstants.EMPTY.equals(actionTaken) && false)
    		ti.end(actionTaken);
    	else
    		ti.end();
	}
	
	protected CasesBusiness getCasesBusiness(IWApplicationContext iwac) {
		try {
			return (CasesBusiness) IBOLookup.getServiceInstance(iwac, CasesBusiness.class);
		}
		catch (IBOLookupException ile) {
			throw new IBORuntimeException(ile);
		}
	}
	
	protected UserBusiness getUserBusiness(IWApplicationContext iwac) {
		try {
			return (UserBusiness) IBOLookup.getServiceInstance(iwac, UserBusiness.class);
		}
		catch (IBOLookupException ile) {
			throw new IBORuntimeException(ile);
		}
	}
	
	public void submitTaskInstance(Map<String, String> parameters, Object submissionData) {
		
		Long taskInstanceId = Long.parseLong(parameters.get(ProcessConstants.TASK_INSTANCE_ID));
		
		SessionFactory sessionFactory = getSessionFactory();
		
		Transaction transaction = sessionFactory.getCurrentSession().getTransaction();
		boolean transactionWasActive = transaction.isActive();
		
		if(!transactionWasActive)
			transaction.begin();
		
		JbpmContext ctx = getJbpmConfiguration().createJbpmContext();
		ctx.setSession(sessionFactory.getCurrentSession());
		
		try {
			TaskInstance taskInstance = ctx.getTaskInstance(taskInstanceId);
	    	submitVariablesAndProceedProcess(taskInstance, submissionData);
			
		} finally {
			ctx.close();
			
			if(!transactionWasActive)
				transaction.commit();
		}
	}
	
	public void setViewManager(ViewManager viewManager) {
		this.viewManager = viewManager;
	}

	public ViewManager getViewManager() {
		return viewManager;
	}
}