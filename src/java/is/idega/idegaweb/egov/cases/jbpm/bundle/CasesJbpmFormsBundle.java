package is.idega.idegaweb.egov.cases.jbpm.bundle;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import javax.faces.context.FacesContext;
import javax.xml.parsers.DocumentBuilder;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.jbpm.JbpmConfiguration;
import org.jbpm.JbpmContext;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.taskmgmt.def.Task;
import org.w3c.dom.Document;

import com.idega.documentmanager.business.DocumentManager;
import com.idega.documentmanager.business.DocumentManagerFactory;
import com.idega.documentmanager.business.PersistenceManager;
import com.idega.documentmanager.component.beans.LocalizedStringBean;
import com.idega.documentmanager.util.FormManagerUtil;
import com.idega.idegaweb.DefaultIWBundle;
import com.idega.idegaweb.IWBundle;
import com.idega.jbpm.data.CasesJbpmBind;
import com.idega.jbpm.def.View;
import com.idega.jbpm.def.ViewFactory;
import com.idega.jbpm.def.ViewToTask;
import com.idega.util.CoreConstants;

/**
 * 
 * @author <a href="civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.1 $
 *
 * Last modified: $Date: 2007/10/30 22:00:02 $ by $Author: civilis $
 *
 */
public class CasesJbpmFormsBundle {

	private DocumentManagerFactory documentManagerFactory;
	private PersistenceManager persistenceManager;
	private ViewFactory viewFactory;
	private JbpmConfiguration jbpmConfiguration;
	private SessionFactory sessionFactory;
	private ViewToTask viewToTaskBinder;
	
	private static final String propertiesFileName = "bundle.properties";
	private static final String processDefinitionFileName = "processdefinition.xml";
	private static final String initTaskNamePropertyKey = "init.task.name";
	
	private ProcessDefinition processDefinition;
	
	public ViewToTask getViewToTaskBinder() {
		return viewToTaskBinder;
	}

	public void setViewToTaskBinder(ViewToTask viewToTaskBinder) {
		this.viewToTaskBinder = viewToTaskBinder;
	}

	public JbpmConfiguration getJbpmConfiguration() {
		return jbpmConfiguration;
	}

	public void setJbpmConfiguration(JbpmConfiguration jbpmConfiguration) {
		this.jbpmConfiguration = jbpmConfiguration;
	}

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
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

	public CasesJbpmFormsBundle() { }
	
	public void createDefinitions(InputStream is) { throw new UnsupportedOperationException("Not implemented yet"); }
	
	public void createDefinitions(FacesContext facesCtx, IWBundle bundle, String templateBundleLocationWithinBundle, String formName, Long caseCategoryId, Long caseTypeId) throws IOException, Exception {
		
		if(formName == null || "".equals(formName))
			throw new NullPointerException("Form name not provided");
			
		Session session = getSessionFactory().getCurrentSession();
		Transaction transaction = session.getTransaction();
		boolean transactionWasActive = transaction.isActive();
		
		if(!transactionWasActive)
			transaction.begin();
		
		JbpmContext ctx = getJbpmConfiguration().createJbpmContext();
		ctx.setSession(session);
		
		try {
//			TODO: cache those input streams
			InputStream pdIs = getResourceInputStream(bundle, templateBundleLocationWithinBundle, processDefinitionFileName);
			ProcessDefinition pd = ProcessDefinition.parseXmlInputStream(pdIs);
			pd.setName(formName);
			
			InputStream propertiesIs = getResourceInputStream(bundle, templateBundleLocationWithinBundle, propertiesFileName);
			
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
			DocumentBuilder builder = FormManagerUtil.getDocumentBuilder();
			
			@SuppressWarnings("unchecked")
			Collection<Task> tasks = pd.getTaskMgmtDefinition().getTasks().values();
			Map<View, Task> viewsAndTaskToBind = new HashMap<View, Task>();
			
			for (Task task : tasks) {
				
				InputStream formIs = getResourceInputStream(bundle, templateBundleLocationWithinBundle+"forms/", task.getName()+".xhtml");
				String formId = loadAndSaveForm(builder, documentManager, formName+" - "+task.getName(), formIs);
			
				View view = getViewFactory().createView();
				view.setViewId(formId);
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

			session.save(bind);
			
			processDefinition = pd;
			
		} catch (Exception e) {
			
			throw e;
			
		} finally {
			
			ctx.close();
			
			if(!transactionWasActive)
				transaction.commit();
		}
	}
	
	public ProcessDefinition getCurrentProcessDefinition() {
		return processDefinition;
	}
	
	private InputStream getResourceInputStream(IWBundle bundle, String pathWithinBundle, String fileName) throws IOException {

		String workspaceDir = System.getProperty(DefaultIWBundle.SYSTEM_BUNDLES_RESOURCE_DIR);
		
		if(workspaceDir != null) {
			
			String bundleInWorkspace = new StringBuilder(workspaceDir).append(CoreConstants.SLASH).append(bundle.getBundleIdentifier()).append(CoreConstants.SLASH).toString();
			return new FileInputStream(bundleInWorkspace + pathWithinBundle + fileName);
		}
						
		return bundle.getResourceInputStream(pathWithinBundle + fileName);
	}
	
	private String loadAndSaveForm(DocumentBuilder builder, DocumentManager documentManager, String formName, InputStream formIs) throws Exception {
		
		Document xformXml = builder.parse(formIs);
		PersistenceManager persistenceManager = getPersistenceManager();
		
		String formId = persistenceManager.generateFormId(formName);
		com.idega.documentmanager.business.Document form = documentManager.openForm(xformXml, formId);
		
		LocalizedStringBean title = form.getFormTitle();
		
		for (Locale titleLocale : title.getLanguagesKeySet())
			title.setString(titleLocale, formName);
		
		form.setFormTitle(title);
		form.save();
		
		return formId;
	}
}