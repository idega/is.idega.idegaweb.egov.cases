package is.idega.idegaweb.egov.cases.presentation;

import is.idega.idegaweb.egov.cases.data.GeneralCase;

import java.util.Collection;
import java.util.List;

import javax.faces.component.UIComponent;

import com.idega.presentation.IWContext;
import com.idega.presentation.text.Link;
import com.idega.user.data.User;

/**
 * 
 * @author <a href="civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.4 $
 *
 * Last modified: $Date: 2008/02/26 15:02:22 $ by $Author: civilis $
 *
 */
public interface CaseHandler {

	public abstract String getBeanIdentifier();
	
	public abstract String getType();
	
	public abstract List<Link> getCaseLinks(GeneralCase theCase);
	
	public abstract UIComponent getView(IWContext iwc, GeneralCase theCase);
	
	public abstract boolean isDisplayedInList(GeneralCase theCase);
	
	public abstract Collection<GeneralCase> getCases(User user, String casesComponentType);
}