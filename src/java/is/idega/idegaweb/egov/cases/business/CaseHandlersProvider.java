package is.idega.idegaweb.egov.cases.business;

import is.idega.idegaweb.egov.cases.presentation.CaseHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import com.idega.util.CoreConstants;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.2 $
 *
 * Last modified: $Date: 2008/02/14 15:51:00 $ by $Author: civilis $
 */
public class CaseHandlersProvider implements ApplicationListener, ApplicationContextAware {
	
	public static final String beanIdentifier = "casesHandlersProvider";
	
	private ApplicationContext applicationContext;
	private Map<String, String> caseHandlersTypesBeanIdentifiers;
	
	public CaseHandler getCaseHandler(String handlerType) {
		
		if(handlerType == null || CoreConstants.EMPTY.equals(handlerType))
			throw new IllegalArgumentException("No or empty handlerType provided");
		
		if(!getCaseHandlersTypesBeanIdentifiers().containsKey(handlerType))
			throw new IllegalArgumentException("No case handler bound to handler type provided: "+handlerType);
		
		String beanIdentifier = getCaseHandlersTypesBeanIdentifiers().get(handlerType);
		return (CaseHandler)getApplicationContext().getBean(beanIdentifier);
	}
	
	public void onApplicationEvent(ApplicationEvent applicationEvent) {
		
		if(applicationEvent instanceof CaseHandlerPluggedInEvent) {
			
			CaseHandler caseHandler = ((CaseHandlerPluggedInEvent)applicationEvent).getCaseHandler();
			
			String beanIdentifier = caseHandler.getBeanIdentifier();
			
			if(beanIdentifier != null) {
				
				getCaseHandlersTypesBeanIdentifiers().put(caseHandler.getType(), beanIdentifier);
			} else {
				Logger.getLogger(getClass().getName()).log(Level.WARNING, "No bean identifier provided for case handler. Skipping. Class name: "+caseHandler.getClass().getName());
			}
		}
	}

	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
	
	protected ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	protected Map<String, String> getCaseHandlersTypesBeanIdentifiers() {
		
		if(caseHandlersTypesBeanIdentifiers == null)
			caseHandlersTypesBeanIdentifiers = new HashMap<String, String>();
		
		return caseHandlersTypesBeanIdentifiers;
	}
}