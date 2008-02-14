package is.idega.idegaweb.egov.cases.presentation;

import is.idega.idegaweb.egov.cases.data.GeneralCase;

import java.util.List;

import javax.faces.component.UIComponent;

import com.idega.presentation.text.Link;

/**
 * 
 * @author <a href="civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.2 $
 *
 * Last modified: $Date: 2008/02/14 15:51:00 $ by $Author: civilis $
 *
 */
public interface CaseHandler {

	public String getBeanIdentifier();
	
	public String getType();
	
	public abstract List<Link> getCaseLinks(GeneralCase theCase);
	
	public abstract UIComponent getView(GeneralCase theCase);
}