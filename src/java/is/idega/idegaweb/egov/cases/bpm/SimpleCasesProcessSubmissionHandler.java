package is.idega.idegaweb.egov.cases.bpm;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.chiba.xml.xforms.connector.AbstractConnector;
import org.chiba.xml.xforms.connector.SubmissionHandler;
import org.chiba.xml.xforms.core.Submission;
import org.chiba.xml.xforms.exception.XFormsException;
import org.w3c.dom.Node;

import com.idega.documentmanager.util.FormManagerUtil;
import com.idega.jbpm.exe.ProcessConstants;
import com.idega.util.URIUtil;
import com.idega.webface.WFUtil;

/**
 * TODO: move all this logic to spring bean
 * 
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.1 $
 *
 * Last modified: $Date: 2008/01/25 15:23:55 $ by $Author: civilis $
 */
public class SimpleCasesProcessSubmissionHandler extends AbstractConnector implements SubmissionHandler {
    
	@SuppressWarnings("unchecked")
    public Map submit(Submission submission, Node submissionInstance) throws XFormsException {
		
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
    	
    	com.idega.jbpm.exe.BPMFactory bpmFactory = (com.idega.jbpm.exe.BPMFactory)WFUtil.getBeanInstance("bpmFactory");
    	String action = submission.getElement().getAttribute(FormManagerUtil.action_att);
    	Map<String, String> parameters = new URIUtil(action).getParameters();
    	
    	if(parameters.containsKey(ProcessConstants.PROCESS_DEFINITION_ID)) {
    		//bpmFactory.startProcess(parameters, submissionInstance);
    		
    	} else if(parameters.containsKey(ProcessConstants.TASK_INSTANCE_ID)) {
    		//bpmFactory.submitTaskInstance(parameters, submissionInstance);
    		
    	} else {
    	
    		Logger.getLogger(CasesBpmProcessManager.class.getName()).log(Level.WARNING, "Couldn't handle submission. No action associated with the submission action: "+action);
    	}

    	return null;
    }
}