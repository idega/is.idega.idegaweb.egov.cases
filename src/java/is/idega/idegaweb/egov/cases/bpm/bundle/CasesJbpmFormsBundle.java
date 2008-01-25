package is.idega.idegaweb.egov.cases.bpm.bundle;

import java.util.List;

import com.idega.jbpm.def.ProcessBundle;
import com.idega.jbpm.def.View;

/**
 * 
 * @author <a href="civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.1 $
 *
 * Last modified: $Date: 2008/01/25 15:23:56 $ by $Author: civilis $
 *
 */
public class CasesJbpmFormsBundle implements ProcessBundle {

	private Long processDefinitionId;
	private Long bindId;
	
	public Long getProcessDefinitionId() {
		return processDefinitionId;
	}
	void setProcessDefinitionId(Long processDefinitionId) {
		this.processDefinitionId = processDefinitionId;
	}
	
	Long getBindId() {
		return bindId;
	}
	void setBindId(Long bindId) {
		this.bindId = bindId;
	}
	
	public List<View> getBundleViews() {

		throw new UnsupportedOperationException("Not implemented yet");
	}
	
	public void remove() {
		throw new UnsupportedOperationException("Not implemented yet");
	}
}