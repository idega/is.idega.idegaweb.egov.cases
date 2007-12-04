package is.idega.idegaweb.egov.cases.jbpm.form;

import is.idega.idegaweb.egov.cases.business.CasesBusiness;
import is.idega.idegaweb.egov.cases.jbpm.CasesJbpmProcessConstants;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.jbpm.JbpmConfiguration;
import org.jbpm.JbpmContext;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.exe.Token;
import org.jbpm.taskmgmt.def.Task;
import org.jbpm.taskmgmt.exe.TaskInstance;

import com.idega.block.form.process.XFormsView;
import com.idega.block.process.data.CaseStatus;
import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.business.IBORuntimeException;
import com.idega.documentmanager.business.DocumentManagerFactory;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.jbpm.data.CasesJbpmBind;
import com.idega.jbpm.def.View;
import com.idega.jbpm.def.ViewToTask;
import com.idega.jbpm.exe.VariablesHandler;
import com.idega.jbpm.exe.ViewManager;
import com.idega.user.business.GroupBusiness;
import com.idega.user.business.UserBusiness;
import com.idega.util.CoreConstants;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.7 $
 *
 * Last modified: $Date: 2007/12/04 14:04:36 $ by $Author: civilis $
 */
public class CasesJbpmFormManager implements ViewManager {

	private JbpmConfiguration jbpmConfiguration;
	private SessionFactory sessionFactory;
	private DocumentManagerFactory documentManagerFactory;
	private ViewToTask viewToTaskBinder;
	private VariablesHandler variablesHandler;
	
	public VariablesHandler getVariablesHandler() {
		return variablesHandler;
	}

	public void setVariablesHandler(VariablesHandler variablesHandler) {
		this.variablesHandler = variablesHandler;
	}
	
	public View loadInitView(FacesContext context, Long processDefinitionId, int initiatorId) {
		
		Session session = getSessionFactory().getCurrentSession();
		
		Transaction transaction = session.getTransaction();
		boolean transactionWasActive = transaction.isActive();
		
		if(!transactionWasActive)
			transaction.begin();
		
		JbpmContext ctx = getJbpmConfiguration().createJbpmContext();
		ctx.setSession(session);
		
		try {
			CasesJbpmBind bind = (CasesJbpmBind)session.load(CasesJbpmBind.class, processDefinitionId);
			
			String initTaskName = bind.getInitTaskName();
			
			if(initTaskName == null || CoreConstants.EMPTY.equals(initTaskName))
				throw new NullPointerException("Init task name not found on CasesJbpmBind.");
			
			ProcessDefinition pd = ctx.getGraphSession().getProcessDefinition(processDefinitionId);
			Task initTask = pd.getTaskMgmtDefinition().getTask(initTaskName);
			
			View view = getViewToTaskBinder().getView(initTask.getId());
			
			Map<String, String> parameters = new HashMap<String, String>(4);
			
			parameters.put(CasesJbpmProcessConstants.userIdActionVariableName, String.valueOf(initiatorId));
			parameters.put(CasesJbpmProcessConstants.processDefinitionIdActionVariableName, String.valueOf(initiatorId));
			parameters.put(CasesJbpmProcessConstants.caseCategoryIdActionVariableName, String.valueOf(bind.getCasesCategoryId()));
			parameters.put(CasesJbpmProcessConstants.caseTypeActionVariableName, String.valueOf(bind.getCasesTypeId()));
			parameters.put(CasesJbpmProcessConstants.startProcessActionVariableName, "1");
			
			((XFormsView)view).addParameters(parameters);
			
			return view;
		
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
			
		} finally {
			
			ctx.close();
			
			if(!transactionWasActive)
				transaction.commit();
		}
	}
	
	protected CaseStatus getInitCaseStatus(IWApplicationContext iwac) {
		
		try {
			return getCasesBusiness(iwac).getCaseStatusWaiting();
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public View loadTaskInstanceView(FacesContext context, Long taskInstanceId) {
		
		Session session = getSessionFactory().getCurrentSession();
		
		Transaction transaction = session.getTransaction();
		boolean transactionWasActive = transaction.isActive();
		
		if(!transactionWasActive)
			transaction.begin();
		
		JbpmContext ctx = getJbpmConfiguration().createJbpmContext();
		ctx.setSession(session);
		
		try {
			TaskInstance taskInstance = ctx.getTaskInstance(taskInstanceId);
			
			View view = getViewToTaskBinder().getView(taskInstance.getTask().getId());
			view.populate(getVariablesHandler().populateVariables(taskInstance.getId()));
			
			return view;
		
		} catch(RuntimeException e) {
			throw e;
		} catch(Exception e) {
			throw new RuntimeException(e);
		} finally {
			ctx.close();
			
			if(!transactionWasActive)
				transaction.commit();
		}
	}
	
	/*
	 public org.w3c.dom.Document loadInstanceForm(FacesContext context, Long processInstanceId) {
		
		Session session = getSessionFactory().getCurrentSession();
		
		Transaction transaction = session.getTransaction();
		boolean transactionWasActive = transaction.isActive();
		
		if(!transactionWasActive)
			transaction.begin();
		
		JbpmContext ctx = getJbpmConfiguration().createJbpmContext();
		ctx.setSession(session);
		
		try {
			ProcessInstance pi = ctx.getProcessInstance(processInstanceId);
			
			@SuppressWarnings("unchecked")
			Collection<TaskInstance> tis = pi.getTaskMgmtInstance().getUnfinishedTasks(pi.getRootToken());
			
			if(tis.size() != 1)
				throw new RuntimeException("Fatal: simple cases process definition not correct. Node comprehends no or more than 1 task . Total: "+tis.size()+". Token: "+pi.getRootToken());
			
			TaskInstance ti = tis.iterator().next();
			
			View view = getViewToTaskBinder().getView(ti.getTask().getId());
			String formId = view.getViewId();
			
			DocumentManager documentManager = getDocumentManagerFactory().newDocumentManager(context);
			Document form = documentManager.openForm(formId);
			
			SimpleCaseFormProceedDMIManager metaInfManager = new SimpleCaseFormProceedDMIManager();
			form.setMetaInformationManager(metaInfManager);
			
			SimpleCaseFormProceedMetaInf metaInf = new SimpleCaseFormProceedMetaInf();
			metaInf.setProcessInstanceId(String.valueOf(processInstanceId));
			metaInfManager.update(metaInf);
			
			getVariablesHandler().populate(ti.getId(), form.getSubmissionInstanceElement());
			
			return form.getXformsDocument();
			
		} catch (Exception e) {
			throw new RuntimeException(e);
			
		} finally {
			
			ctx.close();
			
			if(!transactionWasActive)
				transaction.commit();
		}
	}
	*/
	
	public View loadProcessInstanceView(FacesContext context, Token token) {
		
		Session session = getSessionFactory().getCurrentSession();
		
		Transaction transaction = session.getTransaction();
		boolean transactionWasActive = transaction.isActive();
		
		if(!transactionWasActive)
			transaction.begin();
		
		JbpmContext ctx = getJbpmConfiguration().createJbpmContext();
		ctx.setSession(session);
		
		try {
			
			@SuppressWarnings("unchecked")
			Collection<TaskInstance> tis = token.getProcessInstance().getTaskMgmtInstance().getUnfinishedTasks(token);
			
			if(tis.size() != 1)
				throw new RuntimeException("Fatal: simple cases process definition not correct. Node comprehends no or more than 1 task . Total: "+tis.size()+". Token: "+token);
			
			TaskInstance taskInstance = tis.iterator().next();
			
			View view = getViewToTaskBinder().getView(taskInstance.getTask().getId());
			
			Map<String, String> parameters = new HashMap<String, String>(1);
			parameters.put(CasesJbpmProcessConstants.processInstanceIdActionVariableName, String.valueOf(token.getProcessInstance().getId()));
			parameters.put(CasesJbpmProcessConstants.proceedProcessActionVariableName, "1");
			
			((XFormsView)view).addParameters(parameters);
			
			view.populate(getVariablesHandler().populateVariables(taskInstance.getId()));
			
			return view;
			
		} catch (Exception e) {
			throw new RuntimeException(e);
			
		} finally {
			
			ctx.close();
			
			if(!transactionWasActive)
				transaction.commit();
		}
	}
	
	/*
	public org.w3c.dom.Document loadProcessViewForm(FacesContext context, Long processInstanceId, int viewerId) {
		
//		roles - by view type
//		1 - owner 2 - case handler 3 - other
//		checking in this order. 3 - other should be resolved in the same way task view bindings are resolved.
		
		Session session = getSessionFactory().getCurrentSession();
		
		Transaction transaction = session.getTransaction();
		boolean transactionWasActive = transaction.isActive();
		
		if(!transactionWasActive)
			transaction.begin();
		
		JbpmContext ctx = getJbpmConfiguration().createJbpmContext();
		ctx.setSession(session);
		
		try {
			ProcessInstance pi = ctx.getProcessInstance(processInstanceId);
			Long processDefinitionId = pi.getProcessDefinition().getId();
			
			IWContext iwc = IWContext.getIWContext(FacesContext.getCurrentInstance());
			
			User viewer = getUserBusiness(iwc).getUser(viewerId);
			
			if(viewer == null)
				throw new RuntimeException("userId provided not correct - no User found. userId provided: "+viewerId);
			
			CasesBusiness casesBusiness = getCasesBusiness(iwc);
			
			@SuppressWarnings("unchecked")
			Collection<GeneralCase> genCases = casesBusiness.getCasesByCriteria(null, null, null, null, null, processInstanceId.intValue());
			
			if(genCases.isEmpty())
				throw new RuntimeException("No case found by processInstanceId provided: "+processInstanceId);
			
			GeneralCase genCase = genCases.iterator().next();
			
			User owner = genCase.getOwner();
			String formId;
			
			if(viewer.equals(owner)) {
//				viewer is owner
				ProcessViewByActor processView = ProcessViewByActor.getByViewerType(session, ProcessViewByActor.VIEWER_TYPE_OWNER, XFormsView.VIEW_TYPE, processDefinitionId);
				formId = processView.getViewIdentifier();
				
			} else {
				
				@SuppressWarnings("unchecked")
				Collection<User> handlers = getGroupBusiness(iwc).getUsers(genCase.getHandler());
				
				if(handlers.contains(viewer)) {
//					viewer is status handler
					ProcessViewByActor processView = ProcessViewByActor.getByViewerType(session, ProcessViewByActor.VIEWER_TYPE_CASE_HANDLERS, XFormsView.VIEW_TYPE, processDefinitionId);
					formId = processView.getViewIdentifier();
					
				} else {
//					viewer is someone from another group
//					TODO: implement this
					throw new IllegalStateException("User isn't case owner, and doesn't belong to case handlers group. This situation is not implemented yet");
				}
			}
			
			DocumentManager documentManager = getDocumentManagerFactory().newDocumentManager(context);
			Document form = documentManager.openForm(formId);
			
			getVariablesHandler().populateFromProcess(processInstanceId, form.getSubmissionInstanceElement());
			
			return form.getXformsDocument();
		
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
			
		} finally {
			
			ctx.close();
			
			if(!transactionWasActive)
				transaction.commit();
		}
	}
	*/

	public JbpmConfiguration getJbpmConfiguration() {
		return jbpmConfiguration;
	}

	public void setJbpmConfiguration(JbpmConfiguration jbpmConfiguration) {
		this.jbpmConfiguration = jbpmConfiguration;
	}

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public DocumentManagerFactory getDocumentManagerFactory() {
		return documentManagerFactory;
	}

	public void setDocumentManagerFactory(
			DocumentManagerFactory documentManagerFactory) {
		this.documentManagerFactory = documentManagerFactory;
	}

	public ViewToTask getViewToTaskBinder() {
		return viewToTaskBinder;
	}

	public void setViewToTaskBinder(ViewToTask viewToTaskBinder) {
		this.viewToTaskBinder = viewToTaskBinder;
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
	
	protected GroupBusiness getGroupBusiness(IWApplicationContext iwac) {
		try {
			return (GroupBusiness) IBOLookup.getServiceInstance(iwac, GroupBusiness.class);
		}
		catch (IBOLookupException ile) {
			throw new IBORuntimeException(ile);
		}
	}
}