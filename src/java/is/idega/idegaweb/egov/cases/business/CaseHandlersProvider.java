package is.idega.idegaweb.egov.cases.business;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import com.idega.block.process.business.CaseManager;
import com.idega.util.CoreConstants;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.4 $
 *
 * Last modified: $Date: 2008/02/26 17:58:28 $ by $Author: civilis $
 */
public class CaseHandlersProvider implements ApplicationListener, ApplicationContextAware {
	
	public static final String beanIdentifier = "casesHandlersProvider";
	
	private ApplicationContext applicationContext;
	private Map<String, String> caseHandlersTypesBeanIdentifiers;
	
	public CaseManager getCaseHandler(String handlerType) {
		
		if(handlerType == null || CoreConstants.EMPTY.equals(handlerType))
			throw new IllegalArgumentException("No or empty handlerType provided");
		
		if(!getCaseHandlersTypesBeanIdentifiers().containsKey(handlerType))
			throw new IllegalArgumentException("No case handler bound to handler type provided: "+handlerType);
		
		String beanIdentifier = getCaseHandlersTypesBeanIdentifiers().get(handlerType);
		return (CaseManager)getApplicationContext().getBean(beanIdentifier);
	}
	
	public List<CaseManager> getCaseHandlers() {
		
		List<CaseManager> handlers = new ArrayList<CaseManager>(caseHandlersTypesBeanIdentifiers.size());
		
		for (String handlerIdentifier : caseHandlersTypesBeanIdentifiers.values()) {
			
			CaseManager handler = (CaseManager)getApplicationContext().getBean(handlerIdentifier);
			
			if(handler != null)
				handlers.add(handler);
		}
		
		return handlers;
	}
	
	public void onApplicationEvent(ApplicationEvent applicationEvent) {
		
		if(applicationEvent instanceof CaseHandlerPluggedInEvent) {
			
			CaseManager caseHandler = ((CaseHandlerPluggedInEvent)applicationEvent).getCaseHandler();
			
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