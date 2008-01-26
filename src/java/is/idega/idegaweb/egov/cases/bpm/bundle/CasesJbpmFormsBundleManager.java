package is.idega.idegaweb.egov.cases.bpm.bundle;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import javax.faces.context.FacesContext;
import javax.xml.parsers.DocumentBuilder;

import org.jbpm.JbpmContext;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.taskmgmt.def.Task;
import org.w3c.dom.Document;

import com.idega.block.form.process.XFormsView;
import com.idega.documentmanager.business.DocumentManager;
import com.idega.documentmanager.business.DocumentManagerFactory;
import com.idega.documentmanager.business.PersistenceManager;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.egov.cases.bpm.data.CasesJbpmBind;
import com.idega.idegaweb.egov.cases.bpm.data.CasesJbpmDao;
import com.idega.jbpm.IdegaJbpmContext;

import com.idega.jbpm.data.ProcessViewByActor;
import com.idega.jbpm.def.ProcessBundle;
import com.idega.jbpm.def.ProcessBundleManager;
import com.idega.jbpm.def.View;
import com.idega.jbpm.def.ViewFactory;
import com.idega.jbpm.def.ViewToTask;
import com.idega.util.CoreConstants;
import com.idega.util.xml.XmlUtil;

/**
 * 
 * @author <a href="civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.2 $
 *
 * Last modified: $Date: 2008/01/26 09:45:37 $ by $Author: civilis $
 *
 */
public class CasesJbpmFormsBundleManager implements ProcessBundleManager {

	private DocumentManagerFactory documentManagerFactory;
	private PersistenceManager persistenceManager;
	private ViewFactory viewFactory;
	private ViewToTask viewToTaskBinder;
	private CasesJbpmDao casesJbpmDao;
	private IdegaJbpmContext idegaJbpmContext;
	
	private static final String propertiesFileName = "bundle.properties";
	private static final String processDefinitionFileName = "processdefinition.xml";
	private static final String initTaskNamePropertyKey = "init.task.name";
	private static final String processViewCaseOwnerPropertyKey = "processView.formName.caseOwner";
	private static final String processViewCaseHandlersPropertyKey = "processView.formName.caseHandlers";
	
	public static final String caseCategoryIdParameter = "caseCategoryId";
	public static final String caseTypeIdParameter = "caseTypeId";
	
	private ProcessDefinition processDefinition;
	
	public ViewToTask getViewToTaskBinder() {
		return viewToTaskBinder;
	}

	public void setViewToTaskBinder(ViewToTask viewToTaskBinder) {
		this.viewToTaskBinder = viewToTaskBinder;
	}

	public ViewFactory getViewFactory() {
		return viewFactory;
	}

	public void setViewFactory(ViewFactory viewFactory) {
		this.viewFactory = viewFactory;
	}

	public PersistenceManager getPersistenceManager() {
		return persistenceManager;
	}

	public void setPersistenceManager(PersistenceManager persistenceManager) {
		this.persistenceManager = persistenceManager;
	}

	public DocumentManagerFactory getDocumentManagerFactory() {
		return documentManagerFactory;
	}

	public void setDocumentManagerFactory(
			DocumentManagerFactory documentManagerFactory) {
		this.documentManagerFactory = documentManagerFactory;
	}

	public CasesJbpmFormsBundleManager() { }
	
	protected ProcessBundle createDefinitions(FacesContext facesCtx, IWBundle bundle, String templateBundleLocationWithinBundle, String formName, Long caseCategoryId, Long caseTypeId) throws IOException, Exception {
		
		if(formName == null || CoreConstants.EMPTY.equals(formName))
			throw new NullPointerException("Form name not provided");
			
		JbpmContext ctx = getIdegaJbpmContext().createJbpmContext();
		
		try {
//			TODO: cache those input streams
			InputStream pdIs = bundle.getResourceInputStream(templateBundleLocationWithinBundle+processDefinitionFileName);
			ProcessDefinition pd = ProcessDefinition.parseXmlInputStream(pdIs);
			pd.setName(formName);
			
			InputStream propertiesIs = bundle.getResourceInputStream(templateBundleLocationWithinBundle+propertiesFileName);
			
			Properties properties = new Properties();
			properties.load(propertiesIs);
			
			String initTaskName = properties.getProperty(initTaskNamePropertyKey);
			
			if(pd.getTaskMgmtDefinition().getTask(initTaskName) == null)
				throw new IllegalArgumentException(
						new StringBuilder("No, or wrong (no such task found in process definition) property defined in ")
						.append(propertiesFileName)
						.append(" for property key ")
						.append(initTaskNamePropertyKey)
						.append(". Value=")
						.append(initTaskName)
						.toString()
				);
			
			DocumentManager documentManager = getDocumentManagerFactory().newDocumentManager(facesCtx);
			DocumentBuilder builder = XmlUtil.getDocumentBuilder();
			
			@SuppressWarnings("unchecked")
			Collection<Task> tasks = pd.getTaskMgmtDefinition().getTasks().values();
			Map<View, Task> viewsAndTaskToBind = new HashMap<View, Task>();
			
			for (Task task : tasks) {
				
				InputStream formIs = bundle.getResourceInputStream(new StringBuilder(templateBundleLocationWithinBundle).append("forms/").append(task.getName().toLowerCase().replaceAll(CoreConstants.SPACE, CoreConstants.UNDER)).append(".xhtml").toString());
				String formId = loadAndSaveForm(builder, documentManager, formIs);
			
				View view = getViewFactory().getView(formId, true);
				viewsAndTaskToBind.put(view, task);
			}
			
			ctx.getGraphSession().deployProcessDefinition(pd);
			
			for (Entry<View, Task> entry : viewsAndTaskToBind.entrySet())
				getViewToTaskBinder().bind(entry.getKey(), entry.getValue());
			
			CasesJbpmBind bind = new CasesJbpmBind();
			bind.setCasesCategoryId(caseCategoryId);
			bind.setCasesTypeId(caseTypeId);
			bind.setProcDefId(pd.getId());
			bind.setInitTaskName(initTaskName);
			
			CasesJbpmFormsBundle processBundle = createBundle();
			processBundle.setProcessDefinitionId(pd.getId());
			processBundle.setBindId(bind.getProcDefId());

			getCasesJbpmDao().persist(bind);
			
			loadAndStoreProcessViewForms(properties, bundle, templateBundleLocationWithinBundle, documentManager, pd.getId());
			
			processDefinition = pd;
			
			return processBundle;
			
		} finally {
			
			ctx.close();
		}
	}
	
	protected CasesJbpmFormsBundle createBundle() {
		
		CasesJbpmFormsBundle bundle = new CasesJbpmFormsBundle();
//		set resources
		
		return bundle;
	}
	
	public ProcessBundle createBundle(FacesContext facesCtx, IWBundle bundle,
			String templateBundleLocationWithinBundle, String formName,
			Object parameters) throws IOException {

		@SuppressWarnings("unchecked")
		Map<String, String> params = (Map<String, String>)parameters;

		String caseCategoryId = params.get(caseCategoryIdParameter);
		String caseTypeId = params.get(caseTypeIdParameter);
		
		if(caseCategoryId == null || CoreConstants.EMPTY.equals(caseCategoryId) || caseTypeId == null || CoreConstants.EMPTY.equals(caseTypeId))
			throw new IllegalArgumentException(new StringBuilder("Either not provided: \ncaseCategoryId: ").append(caseCategoryId).append(CoreConstants.NEWLINE).append("caseTypeId: ").append(caseTypeId).toString());
		
		try {
			return createDefinitions(facesCtx, bundle, templateBundleLocationWithinBundle, formName, Long.parseLong(caseCategoryId), Long.parseLong(caseTypeId));
		
		} catch (IOException e) {
			throw e;
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public ProcessBundle loadBundle(long processDefinitionId) {
		
		CasesJbpmBind bind = getCasesJbpmDao().find(CasesJbpmBind.class, processDefinitionId);
		
		if(bind == null)
			throw new RuntimeException("No cases jbpm bind for process definition id provided: "+processDefinitionId);
		
		CasesJbpmFormsBundle processBundle = createBundle();
		processBundle.setProcessDefinitionId(processDefinitionId);
		processBundle.setBindId(bind.getProcDefId());
		
		return processBundle;
	}
	
	public List<ProcessBundle> getBundles() {
		
		List<ProcessBundle> bundles = new ArrayList<ProcessBundle>();
		
		for (CasesJbpmBind casesJbpmBind : getCasesJbpmDao().getAllCasesJbpmBinds()) {
			
			CasesJbpmFormsBundle processBundle = createBundle();
			processBundle.setProcessDefinitionId(casesJbpmBind.getProcDefId());
			processBundle.setBindId(casesJbpmBind.getProcDefId());
		}
		
		return bundles;
	}
	
	private void loadAndStoreProcessViewForms(Properties properties, IWBundle bundle, String templateBundleLocationWithinBundle, DocumentManager documentManager, Long processDefinitionId) throws Exception {

		DocumentBuilder builder = XmlUtil.getDocumentBuilder();
		
		InputStream formIs;
		String formId;
		ProcessViewByActor processView;

//		case owner form name
		String formName = properties.getProperty(processViewCaseOwnerPropertyKey);
		
		if(formName != null) {
		
			formIs = bundle.getResourceInputStream(new StringBuilder(templateBundleLocationWithinBundle).append("forms/").append(formName).append(".xhtml").toString());
			formId = loadAndSaveForm(builder, documentManager, formIs);
			
			processView = new ProcessViewByActor();
			processView.setProcessDefinitionId(processDefinitionId);
			processView.setViewerType(ProcessViewByActor.VIEWER_TYPE_OWNER);
			processView.setViewType(XFormsView.VIEW_TYPE);
			processView.setViewIdentifier(formId);
			
			getCasesJbpmDao().persist(processView);
		}

//		case handlers form name
		formName = properties.getProperty(processViewCaseHandlersPropertyKey);
		
		if(formName != null) {
		
			formIs = bundle.getResourceInputStream(new StringBuilder(templateBundleLocationWithinBundle).append("forms/").append(formName).append(".xhtml").toString());
			formId = loadAndSaveForm(builder, documentManager, formIs);
			
			processView = new ProcessViewByActor();
			processView.setProcessDefinitionId(processDefinitionId);
			processView.setViewerType(ProcessViewByActor.VIEWER_TYPE_CASE_HANDLERS);
			processView.setViewType(XFormsView.VIEW_TYPE);
			processView.setViewIdentifier(formId);
			
			getCasesJbpmDao().persist(processView);
		}
	}
	
	public ProcessDefinition getCurrentProcessDefinition() {
		return processDefinition;
	}
	
//	private InputStream getResourceInputStream(IWBundle bundle, String pathWithinBundle, String fileName) throws IOException {
//
//		String workspaceDir = System.getProperty(DefaultIWBundle.SYSTEM_BUNDLES_RESOURCE_DIR);
//		
//		if(workspaceDir != null) {
//			
//			String bundleInWorkspace = new StringBuilder(workspaceDir).append(CoreConstants.SLASH).append(bundle.getBundleIdentifier()).append(CoreConstants.SLASH).toString();
//			return new FileInputStream(bundleInWorkspace + pathWithinBundle + fileName);
//		}
//						
//		return bundle.getResourceInputStream(pathWithinBundle + fileName);
//	}
	
	private String loadAndSaveForm(DocumentBuilder builder, DocumentManager documentManager, InputStream formIs) throws Exception {
		
		Document xformXml = builder.parse(formIs);
		com.idega.documentmanager.business.Document form = documentManager.openFormAndGenerateId(xformXml);
		form.save();
		
		return form.getId();
	}

	public IdegaJbpmContext getIdegaJbpmContext() {
		return idegaJbpmContext;
	}

	public void setIdegaJbpmContext(IdegaJbpmContext idegaJbpmContext) {
		this.idegaJbpmContext = idegaJbpmContext;
	}

	public CasesJbpmDao getCasesJbpmDao() {
		return casesJbpmDao;
	}

	public void setCasesJbpmDao(CasesJbpmDao casesJbpmDao) {
		this.casesJbpmDao = casesJbpmDao;
	}
}