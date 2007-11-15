package is.idega.idegaweb.egov.cases.jbpm;

import is.idega.idegaweb.egov.cases.business.CasesBusiness;
import is.idega.idegaweb.egov.cases.data.GeneralCase;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.context.FacesContext;

import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.jbpm.JbpmConfiguration;
import org.jbpm.JbpmContext;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.taskmgmt.exe.TaskInstance;
import org.w3c.dom.Node;

import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.business.IBORuntimeException;
import com.idega.documentmanager.business.ext.SimpleCaseFormCreateDMIManager;
import com.idega.documentmanager.business.ext.SimpleCaseFormProceedDMIManager;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWMainApplication;
import com.idega.jbpm.business.ProcessManager;
import com.idega.jbpm.exe.VariablesHandler;
import com.idega.presentation.IWContext;
import com.idega.presentation.PresentationObject;
import com.idega.user.business.UserBusiness;
import com.idega.user.data.User;
import com.idega.util.CoreConstants;
import com.idega.util.IWTimestamp;
import com.idega.util.URIUtil;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.3 $
 *
 * Last modified: $Date: 2007/11/15 14:34:36 $ by $Author: civilis $
 */
public class CasesJbpmProcessManager {

	private SessionFactory sessionFactory;
	private JbpmConfiguration jbpmConfiguration;
	private VariablesHandler variablesHandler;
	private ProcessManager jbpmProcessManager;
	
    public ProcessManager getJbpmProcessManager() {
		return jbpmProcessManager;
	}

	public void setJbpmProcessManager(ProcessManager jbpmProcessManager) {
		this.jbpmProcessManager = jbpmProcessManager;
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

	public void processSubmission(String action, Node instance) {

    	Map<String, String> parameters = new URIUtil(action).getParameters();
    	
    	if(parameters.containsKey(SimpleCaseFormCreateDMIManager.type)) {
    		
    		startProcess(parameters, instance);
    		
    	} else if(parameters.containsKey(SimpleCaseFormProceedDMIManager.type)) {
    		
    		proceedProcess(parameters, instance);
    		
    	} else {
    	
    		Logger.getLogger(CasesJbpmProcessManager.class.getName()).log(Level.WARNING, "Couldn't handle submission. No action associated with the submission action: "+action);
    	}
    }
	
	protected void startProcess(Map<String, String> parameters, Node instance) {
		
		Long pdId = Long.parseLong(parameters.get(SimpleCaseFormCreateDMIManager.pdIdParam));
		int userId = Integer.parseInt(parameters.get(SimpleCaseFormCreateDMIManager.userIdParam));
		Long caseCatId = Long.parseLong(parameters.get(SimpleCaseFormCreateDMIManager.caseCategoryIdParam));
		Long caseTypeId = Long.parseLong(parameters.get(SimpleCaseFormCreateDMIManager.caseTypeParam));
		
		SessionFactory sessionFactory = getSessionFactory();
		
		Transaction transaction = sessionFactory.getCurrentSession().getTransaction();
		boolean transactionWasActive = transaction.isActive();
		
		if(!transactionWasActive)
			transaction.begin();
		
		JbpmContext ctx = getJbpmConfiguration().createJbpmContext();
		ctx.setSession(sessionFactory.getCurrentSession());
		
		try {
			
			ProcessDefinition pd = ctx.getGraphSession().getProcessDefinition(pdId);
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
			
			TaskInstance ti = tis.iterator().next();
			
			Map<String, Object> caseData = new HashMap<String, Object>();
			caseData.put(CasesJbpmProcessConstants.caseIdVariableName, genCase.getPrimaryKey().toString());
			caseData.put(CasesJbpmProcessConstants.caseTypeNameVariableName, genCase.getCaseType().getName());
			caseData.put(CasesJbpmProcessConstants.caseCategoryNameVariableName, genCase.getCaseCategory().getName());
			
			IWTimestamp created = new IWTimestamp(genCase.getCreated());
			caseData.put(CasesJbpmProcessConstants.caseCreatedDateVariableName, created.getLocaleDateAndTime(iwc.getCurrentLocale(), IWTimestamp.SHORT, IWTimestamp.SHORT));
			
			getJbpmProcessManager().submitVariables(caseData, ti.getId());
			submitVariablesAndProceedProcess(ti, instance);
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			ctx.close();
			
			if(!transactionWasActive)
				transaction.commit();
		}
	}
	
	protected void submitVariablesAndProceedProcess(TaskInstance ti, Node instance) {
		
    	getVariablesHandler().submit(ti.getId(), instance);
    	
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
	
	protected void proceedProcess(Map<String, String> parameters, Node instance) {
		
		Long piId = Long.parseLong(parameters.get(SimpleCaseFormProceedDMIManager.piIdParam));
		
		SessionFactory sessionFactory = getSessionFactory();
		
		Transaction transaction = sessionFactory.getCurrentSession().getTransaction();
		boolean transactionWasActive = transaction.isActive();
		
		if(!transactionWasActive)
			transaction.begin();
		
		JbpmContext ctx = getJbpmConfiguration().createJbpmContext();
		ctx.setSession(sessionFactory.getCurrentSession());
		
		try {
			ProcessInstance pi = ctx.getProcessInstance(piId);

//			forget about parallel processing for not, using root token
			@SuppressWarnings("unchecked")
			Collection<TaskInstance> tis = pi.getTaskMgmtInstance().getUnfinishedTasks(pi.getRootToken());
			
			if(tis.size() != 1)
				throw new RuntimeException("Fatal: simple cases process definition not correct. First task node comprehends no or more than 1 task . Total: "+tis.size());
			
			//getCasesBusiness(iwc).handleCase((user, caseCatId, caseTypeId, /*attachment pk*/null, "This is simple cases-jbpm-formbuilder integration example.", "type", new Long(pi.getId()).intValue(), /*isPrivate*/false, getCasesBusiness(iwc).getIWResourceBundleForUser(user, iwc, iwma.getBundle(PresentationObject.CORE_IW_BUNDLE_IDENTIFIER)));
 			//now we save variables values in the task and end the and end the task therefore progressing further
			
	    	submitVariablesAndProceedProcess(tis.iterator().next(), instance);
			
		} finally {
			ctx.close();
			
			if(!transactionWasActive)
				transaction.commit();
		}
	}
}