package is.idega.idegaweb.egov.cases.jbpm;

import is.idega.idegaweb.egov.cases.business.CasesBusiness;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.chiba.xml.xforms.connector.AbstractConnector;
import org.chiba.xml.xforms.connector.SubmissionHandler;
import org.chiba.xml.xforms.core.Submission;
import org.chiba.xml.xforms.exception.XFormsException;
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
import com.idega.documentmanager.util.FormManagerUtil;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWMainApplication;
import com.idega.jbpm.exe.VariablesHandler;
import com.idega.presentation.IWContext;
import com.idega.presentation.PresentationObject;
import com.idega.user.business.UserBusiness;
import com.idega.user.data.User;
import com.idega.util.URIUtil;
import com.idega.webface.WFUtil;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.2 $
 *
 * Last modified: $Date: 2007/10/24 15:28:39 $ by $Author: civilis $
 */
public class SimpleCasesProcessSubmissionHandler extends AbstractConnector implements SubmissionHandler {
    
    /**
     * TODO: write javadoc
     */
	@SuppressWarnings("unchecked")
    public Map submit(Submission submission, Node instance) throws XFormsException {

    	//method - post, replace - none
    	if (!submission.getReplace().equalsIgnoreCase("none"))
            throw new XFormsException("Submission mode '" + submission.getReplace() + "' not supported");
    	
    	if(!submission.getMethod().equalsIgnoreCase("put") && !submission.getMethod().equalsIgnoreCase("post"))
    		throw new XFormsException("Submission method '" + submission.getMethod() + "' not supported");
    	
    	if(submission.getMethod().equalsIgnoreCase("put")) {
    		//update (put)
    		//currently unsupported
    		throw new XFormsException("Submission method '" + submission.getMethod() + "' not yet supported");
    		
    	} else {
    		//insert (post)
    	}
    	
    	String action = submission.getElement().getAttribute(FormManagerUtil.action_att);
    	Map<String, String> parameters = new URIUtil(action).getParameters();
    	
    	if(parameters.containsKey(SimpleCaseFormCreateDMIManager.type)) {
    		
    		processCreateProcess(parameters, instance);
    		
    	} else if(false) {
    		
    	}
    	

    	
    	//VariablesHandler vh = (VariablesHandler)WFUtil.getBeanInstance("process_xforms_variablesHandler");
//    	
//    	TODO: do this somewhere else and in correct way
//    	
//    	vh.submit(Long.parseLong(taskId), instance);
    	
    	return null;
    }
	
	private void processCreateProcess(Map<String, String> parameters, Node instance) {
		
		Long pdId = Long.parseLong(parameters.get(SimpleCaseFormCreateDMIManager.pdIdParam));
		int userId = Integer.parseInt(parameters.get(SimpleCaseFormCreateDMIManager.userIdParam));
		Long caseCatId = Long.parseLong(parameters.get(SimpleCaseFormCreateDMIManager.caseCategoryIdParam));
		Long caseTypeId = Long.parseLong(parameters.get(SimpleCaseFormCreateDMIManager.caseTypeParam));
		
		SessionFactory sessionFactory = (SessionFactory)WFUtil.getBeanInstance("idega_jbpmDSHibernateSessionFactory");
		
		Transaction transaction = sessionFactory.getCurrentSession().getTransaction();
		boolean transactionWasActive = transaction.isActive();
		
		if(!transactionWasActive)
			transaction.begin();
		
		JbpmConfiguration jbmpCfg = (JbpmConfiguration)WFUtil.getBeanInstance("defaultJbpmConfiguration");
		JbpmContext ctx = jbmpCfg.createJbpmContext();
		ctx.setSession(sessionFactory.getCurrentSession());
		
		try {
			
			ProcessDefinition pd = ctx.getGraphSession().getProcessDefinition(pdId);
			ProcessInstance pi = new ProcessInstance(pd);
			
			IWContext iwc = IWContext.getIWContext(FacesContext.getCurrentInstance());
			User user = getUserBusiness(iwc).getUser(userId);
			IWMainApplication iwma = iwc.getApplicationContext().getIWMainApplication();
			
			getCasesBusiness(iwc).storeGeneralCase(user, caseCatId, caseTypeId, /*attachment pk*/null, "This is simple cases-jbpm-formbuilder integration example.", "type", new Long(pi.getId()).intValue(), /*isPrivate*/false, getCasesBusiness(iwc).getIWResourceBundleForUser(user, iwc, iwma.getBundle(PresentationObject.CORE_IW_BUNDLE_IDENTIFIER)));
			
			pi.setStart(new Date());
			
//			moving to 1st task node
			pi.getRootToken().signal();
			
			@SuppressWarnings("unchecked")
			Collection<TaskInstance> tis = pi.getTaskMgmtInstance().getUnfinishedTasks(pi.getRootToken());
			
			if(tis.size() != 1)
				throw new RuntimeException("Fatal: simple cases process definition not correct. First task node comprehends no or more than 1 task . Total: "+tis.size());
			
			
			//now we save variables values in the task and end task therefore progressing further
			
			TaskInstance ti = tis.iterator().next();
			
	    	VariablesHandler vh = (VariablesHandler)WFUtil.getBeanInstance("process_xforms_variablesHandler");
	    	
	    	vh.submit(ti.getId(), instance);
	    	ti.end();
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			ctx.close();
			
			if(!transactionWasActive)
				transaction.commit();
		}
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
}