package is.idega.idegaweb.egov.cases.jbpm.actionhandlers;

import org.jbpm.graph.def.ActionHandler;
import org.jbpm.graph.exe.ExecutionContext;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.1 $
 *
 * Last modified: $Date: 2007/10/30 22:00:03 $ by $Author: civilis $
 */
public class CasesAllocationHandler implements ActionHandler {

	private static final long serialVersionUID = -6527613958449076385L;

	public void execute(ExecutionContext ctx) throws Exception {

		String var = (String)ctx.getVariable("var");
	}
}