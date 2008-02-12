package is.idega.idegaweb.egov.cases.business;

import is.idega.idegaweb.egov.cases.presentation.CaseHandler;

import org.springframework.context.ApplicationEvent;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.1 $
 *
 * Last modified: $Date: 2008/02/12 14:36:11 $ by $Author: civilis $
 */
public class CaseHandlerPluggedInEvent extends ApplicationEvent {

	private static final long serialVersionUID = 2033689951503691347L;
	private CaseHandler caseHandler;

	public CaseHandlerPluggedInEvent(Object source) {
        super(source);
        
        caseHandler = (CaseHandler)source;
    }

	public CaseHandler getCaseHandler() {
		return caseHandler;
	}
}