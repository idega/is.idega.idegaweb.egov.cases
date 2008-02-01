package is.idega.idegaweb.egov.cases;

import is.idega.idegaweb.egov.cases.util.CaseConstants;

import java.util.ArrayList;
import java.util.Collection;
import javax.faces.context.FacesContext;


import com.idega.core.accesscontrol.business.StandardRoles;
import com.idega.core.view.ApplicationViewNode;
import com.idega.core.view.DefaultViewNode;
import com.idega.core.view.ViewManager;
import com.idega.core.view.ViewNode;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWMainApplication;
import com.idega.repository.data.Singleton;

/**
 * 
 * @author <a href="civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.1 $
 *
 * Last modified: $Date: 2008/02/01 12:20:03 $ by $Author: civilis $
 *
 */
public class CasesViewManager implements Singleton  {

	private static final String VIEW_MANAGER_KEY = "iw_casesviewmanager";
	private static final String VIEW_MANAGER_ID = "Cases";
	
	private ViewNode rootNode;
	private IWMainApplication iwma;
	
	private CasesViewManager(IWMainApplication iwma){
		
		this.iwma = iwma;
	}

	public static synchronized CasesViewManager getInstance(IWMainApplication iwma) {
		CasesViewManager viewManager = (CasesViewManager)iwma.getAttribute(VIEW_MANAGER_KEY);
		
		if(viewManager == null) {
			viewManager = new CasesViewManager(iwma);
			iwma.setAttribute(VIEW_MANAGER_KEY, viewManager);
	    }
	    return viewManager;
	}	
	
	public static CasesViewManager getInstance(FacesContext context) {
		return getInstance(IWMainApplication.getIWMainApplication(context));
	}
	
	public ViewManager getViewManager() {
		return ViewManager.getInstance(iwma);
	}
	
	
	public ViewNode getContentNode() {
		IWBundle iwb = iwma.getBundle(CaseConstants.IW_BUNDLE_IDENTIFIER);
		
		if(rootNode == null)
			rootNode = initalizeContentNode(iwb);
		
		return rootNode;
	}
	
	public ViewNode initalizeContentNode(IWBundle bundle) {
		
		ViewNode root = getViewManager().getWorkspaceRoot();
		DefaultViewNode node = new ApplicationViewNode(VIEW_MANAGER_ID, root);
		Collection<String> roles = new ArrayList<String>();
		roles.add(StandardRoles.ROLE_KEY_BUILDER);
		node.setAuthorizedRoles(roles);
		
		node.setFaceletUri(bundle.getFaceletURI("ViewNodeCreateProcess.xhtml"));
		rootNode = node;
		return rootNode;
	}
	
	public void initializeStandardNodes(IWBundle bundle){
		/*ViewNode contentNode = */initalizeContentNode(bundle);
		
		/*
		DefaultViewNode node = new DefaultViewNode(VIEW_MANAGER_ID, contentNode);
		node.setFaceletUri(bundle.getFaceletURI("processDefUpload.xhtml"));
		node.setName(VIEW_MANAGER_ID);
		node.setVisibleInMenus(true);
		
		node = new DefaultViewNode("xforms_workflow", contentNode);
		node.setFaceletUri("/idegaweb/bundles/com.idega.formbuilder.bundle/facelets/xformsWorkflow.xhtml");
		node.setName("XForms Workflow");
		node.setVisibleInMenus(true);
		
		node = new DefaultViewNode("processMgmntMockup", contentNode);
		node.setFaceletUri(bundle.getFaceletURI("processMgmtMockup.xhtml"));
		node.setName("Process Mgmnt Mockup");
		node.setVisibleInMenus(true);
		
		node = new DefaultViewNode("simpleCasesProcess", contentNode);
		node.setFaceletUri(bundle.getFaceletURI("SimpleCasesProcess.xhtml"));
		node.setName("Simple cases process");
		node.setVisibleInMenus(true);
		
		node = new DefaultViewNode("nestCasesProcess", contentNode);
		node.setFaceletUri(bundle.getFaceletURI("NestCasesProcess.xhtml"));
		node.setName("Nest cases process");
		node.setVisibleInMenus(true);
		*/
	}
}