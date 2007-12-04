package is.idega.idegaweb.egov.cases.jbpm;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.chiba.xml.xforms.connector.AbstractConnector;
import org.chiba.xml.xforms.connector.SubmissionHandler;
import org.chiba.xml.xforms.core.Submission;
import org.chiba.xml.xforms.exception.XFormsException;
import org.w3c.dom.Node;

import com.idega.documentmanager.util.FormManagerUtil;
import com.idega.util.URIUtil;
import com.idega.webface.WFUtil;

/**
 * TODO: move all this logic to spring bean
 * 
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.6 $
 *
 * Last modified: $Date: 2007/12/04 14:04:20 $ by $Author: civilis $
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
    	
    	CasesJbpmProcessManager submissionBean = (CasesJbpmProcessManager)WFUtil.getBeanInstance("casesJbpmProcessManager");
    	String action = submission.getElement().getAttribute(FormManagerUtil.action_att);
    	Map<String, String> parameters = new URIUtil(action).getParameters();
    	
    	if(parameters.containsKey(CasesJbpmProcessConstants.startProcessActionVariableName)) {
    		submissionBean.startProcess(parameters, instance);
    		
    	} else if(parameters.containsKey(CasesJbpmProcessConstants.proceedProcessActionVariableName)) {
    		submissionBean.proceedProcess(parameters, instance);
    		
    	} else {
    	
    		Logger.getLogger(CasesJbpmProcessManager.class.getName()).log(Level.WARNING, "Couldn't handle submission. No action associated with the submission action: "+action);
    	}

    	return null;
    }
}