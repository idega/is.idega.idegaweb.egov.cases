package is.idega.idegaweb.egov.cases.business;


import org.springframework.context.ApplicationEvent;

import com.idega.block.process.business.CaseManager;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.2 $
 *
 * Last modified: $Date: 2008/02/26 17:58:27 $ by $Author: civilis $
 */
public class CaseHandlerPluggedInEvent extends ApplicationEvent {

	private static final long serialVersionUID = 2033689951503691347L;
	private CaseManager caseHandler;

	public CaseHandlerPluggedInEvent(Object source) {
        super(source);
        
        caseHandler = (CaseManager)source;
    }

	public CaseManager getCaseHandler() {
		return caseHandler;
	}
}