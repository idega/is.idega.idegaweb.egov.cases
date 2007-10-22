package is.idega.idegaweb.egov.cases.jbpm;

import is.idega.idegaweb.egov.cases.business.CasesBusiness;
import is.idega.idegaweb.egov.cases.data.CaseCategory;
import is.idega.idegaweb.egov.cases.data.CaseType;
import is.idega.idegaweb.egov.cases.util.CaseConstants;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.xml.parsers.DocumentBuilder;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.jbpm.JbpmConfiguration;
import org.jbpm.JbpmContext;
import org.jbpm.graph.def.ProcessDefinition;
import org.w3c.dom.Document;

import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.business.IBORuntimeException;
import com.idega.documentmanager.business.DocumentManager;
import com.idega.documentmanager.business.DocumentManagerFactory;
import com.idega.documentmanager.business.PersistenceManager;
import com.idega.documentmanager.component.beans.LocalizedStringBean;
import com.idega.documentmanager.util.FormManagerUtil;
import com.idega.idegaweb.DefaultIWBundle;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWMainApplication;
import com.idega.jbpm.data.CasesJbpmBind;
import com.idega.jbpm.def.View;
import com.idega.jbpm.def.ViewFactory;
import com.idega.jbpm.def.ViewToTask;

/**
 * 
 * @author <a href="civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.3 $
 *
 * Last modified: $Date: 2007/10/22 15:39:00 $ by $Author: civilis $
 *
 */
public class SimpleCasesProcessManager {
	
	private JbpmConfiguration jbpmConfiguration;
	private SessionFactory sessionFactory;
	private String formName;
	private String message;
	private DocumentManagerFactory documentManagerFactory;
	private PersistenceManager persistenceManager;
	private ViewFactory viewFactory;
	private ViewToTask viewToTaskBinder;
	private String caseCategory;
	private String caseType;
	private String processDefinitionId;
	private String processInstanceId;
	
	private String processDefinitionTemplateLocation;
	private String createRequestFormTemplateLocation;
	private String createResponseFormTemplateLocation;
	
	private List<SelectItem> casesTypes = new ArrayList<SelectItem>();
	private List<SelectItem> casesCategories = new ArrayList<SelectItem>();
	private List<SelectItem> casesProcessesDefinitions = new ArrayList<SelectItem>();

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

	public String getFormName() {
		return formName;
	}

	public void setFormName(String formName) {
		this.formName = formName;
	}

	public JbpmConfiguration getJbpmConfiguration() {
		return jbpmConfiguration;
	}

	public void setJbpmConfiguration(JbpmConfiguration jbpmConfiguration) {
		this.jbpmConfiguration = jbpmConfiguration;
	}
	
	public String createNewSimpleProcess() {
		
		if(getFormName() == null || getFormName().equals("")) {
		
			setMessage("Form name not set");
			return null;
		}
			
		Session session = getSessionFactory().getCurrentSession();
		Transaction transaction = session.getTransaction();
		boolean transactionWasActive = transaction.isActive();
		
		if(!transactionWasActive)
			transaction.begin();
		
		JbpmContext ctx = getJbpmConfiguration().createJbpmContext();
		ctx.setSession(session);
		
		try {
			FacesContext facesCtx = FacesContext.getCurrentInstance();
			IWMainApplication iwma = IWMainApplication.getIWMainApplication(facesCtx);
			
			InputStream pdIs = getResourceInputStream(iwma, getProcessDefinitionTemplateLocation());
			InputStream createReqFormIs = getResourceInputStream(iwma, getCreateRequestFormTemplateLocation());
			InputStream createResFormIs = getResourceInputStream(iwma, getCreateResponseFormTemplateLocation());
			
			ProcessDefinition pd = ProcessDefinition.parseXmlInputStream(pdIs);
			
//			TODO: create transaction rollback here. if any of deployings fails, rollback everything.
			pd.setName(getFormName()+" Process");
			ctx.getGraphSession().deployProcessDefinition(pd);
			
			DocumentManager documentManager = getDocumentManagerFactory().newDocumentManager(facesCtx);
			DocumentBuilder builder = FormManagerUtil.getDocumentBuilder();
			
			String[] values1 = loadAndSaveForm(builder, documentManager, getFormName()+" request", createReqFormIs);
			String[] values2 = loadAndSaveForm(builder, documentManager, getFormName()+" response", createResFormIs);
			
			String createReqFormId = values1[0];
			String createResFormId = values2[0];
			String createReqTaskName = values1[1];
			String createResTaskName = values2[1];
			
			View view = getViewFactory().createView();
			view.setViewId(createReqFormId);
			
//			bind create request
			getViewToTaskBinder().bind(view, pd.getTaskMgmtDefinition().getTask(createReqTaskName));
			
			view = getViewFactory().createView();
			view.setViewId(createResFormId);
			
//			bind create response
			getViewToTaskBinder().bind(view, pd.getTaskMgmtDefinition().getTask(createResTaskName));
			
			CasesJbpmBind bind = new CasesJbpmBind();
			bind.setCasesCategoryId(Long.parseLong(getCaseCategory()));
			bind.setCasesTypeId(Long.parseLong(getCaseType()));
			bind.setProcDefId(pd.getId());
			bind.setInitTaskName(createReqTaskName);
			
			session.save(bind);
			
		} catch (IOException e) {
			setMessage("IO Exception occured");
			e.printStackTrace();
		} catch (Exception e) {
			setMessage("Exception occured");
			e.printStackTrace();
		} finally {
			
			ctx.close();
			
			if(!transactionWasActive)
				transaction.commit();
		}
		
		return null;
	}
	
	private String[] loadAndSaveForm(DocumentBuilder builder, DocumentManager documentManager, String formName, InputStream formIs) throws Exception {
		
		Document xformXml = builder.parse(formIs);
		PersistenceManager persistenceManager = getPersistenceManager();
		
		String formId = persistenceManager.generateFormId(formName);
		com.idega.documentmanager.business.Document form = documentManager.openForm(xformXml, formId);
		
		LocalizedStringBean title = form.getFormTitle();
		
		String taskName = title.getString(new Locale("en"));
		
		for (Locale titleLocale : title.getLanguagesKeySet())
			title.setString(titleLocale, formName);
		
		form.setFormTitle(title);
		form.save();
		
		return new String[] {formId, taskName};
	}

	public String getMessage() {
		return message == null ? "" : message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	private InputStream getResourceInputStream(IWMainApplication iwma, String pathWithinBundle) throws IOException {

		IWBundle bundle = iwma.getBundle(CaseConstants.IW_BUNDLE_IDENTIFIER);
		
		String workspaceDir = System.getProperty(DefaultIWBundle.SYSTEM_BUNDLES_RESOURCE_DIR);
		
		if(workspaceDir != null) {
			
			String bundleInWorkspace = new StringBuilder(workspaceDir).append("/").append(CaseConstants.IW_BUNDLE_IDENTIFIER).append("/").toString();
			return new FileInputStream(bundleInWorkspace + pathWithinBundle);
		}
						
		return bundle.getResourceInputStream(pathWithinBundle);
	}

	public String getProcessDefinitionTemplateLocation() {
		return processDefinitionTemplateLocation;
	}
	
	public void setProcessDefinitionTemplateLocation(
			String processDefinitionTemplateLocation) {
		this.processDefinitionTemplateLocation = processDefinitionTemplateLocation;
	}

	public String getCreateRequestFormTemplateLocation() {
		return createRequestFormTemplateLocation;
	}

	public void setCreateRequestFormTemplateLocation(
			String createRequestFormTemplateLocation) {
		this.createRequestFormTemplateLocation = createRequestFormTemplateLocation;
	}

	public String getCreateResponseFormTemplateLocation() {
		return createResponseFormTemplateLocation;
	}

	public void setCreateResponseFormTemplateLocation(
			String createResponseFormTemplateLocation) {
		this.createResponseFormTemplateLocation = createResponseFormTemplateLocation;
	}

	public ViewFactory getViewFactory() {
		return viewFactory;
	}

	public void setViewFactory(ViewFactory viewFactory) {
		this.viewFactory = viewFactory;
	}

	public ViewToTask getViewToTaskBinder() {
		return viewToTaskBinder;
	}

	public void setViewToTaskBinder(ViewToTask viewToTaskBinder) {
		this.viewToTaskBinder = viewToTaskBinder;
	}

	public List<SelectItem> getCasesTypes() {
		
		casesTypes.clear();
		
		try {
			
			@SuppressWarnings("unchecked")
			Collection<CaseType> types = getCasesBusiness(IWMainApplication.getIWMainApplication(FacesContext.getCurrentInstance()).getIWApplicationContext())
			.getCaseTypes();
			
			for (CaseType caseType : types) {
				
				SelectItem item = new SelectItem();
				
//				it is done in the same manner (toString for primary key), so anyway.. :\ 
				item.setValue(caseType.getPrimaryKey().toString());
				item.setLabel(caseType.getName());
				casesTypes.add(item);
			}
			
			return casesTypes;
			
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}

	public void setCasesTypes(List<SelectItem> casesTypes) {
		this.casesTypes = casesTypes;
	}

	public List<SelectItem> getCasesCategories() {
		
		casesCategories.clear();
		
		try {
			
			@SuppressWarnings("unchecked")
			Collection<CaseCategory> categories = getCasesBusiness(IWMainApplication.getIWMainApplication(FacesContext.getCurrentInstance()).getIWApplicationContext())
			.getCaseCategories();
			
			for (CaseCategory caseCategory : categories) {
				
				SelectItem item = new SelectItem();
				
//				it is done in the same manner (toString for primary key), so anyway.. :\ 
				item.setValue(caseCategory.getPrimaryKey().toString());
				item.setLabel(caseCategory.getName());
				casesCategories.add(item);
			}
			
			return casesCategories;
			
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}

	public void setCasesCategories(List<SelectItem> casesCategories) {
		this.casesCategories = casesCategories;
	}
	
	protected CasesBusiness getCasesBusiness(IWApplicationContext iwac) {
		try {
			return (CasesBusiness) IBOLookup.getServiceInstance(iwac, CasesBusiness.class);
		}
		catch (IBOLookupException ile) {
			throw new IBORuntimeException(ile);
		}
	}

	public String getCaseCategory() {
		return caseCategory;
	}

	public void setCaseCategory(String caseCategory) {
		this.caseCategory = caseCategory;
	}

	public String getCaseType() {
		return caseType;
	}

	public void setCaseType(String caseType) {
		this.caseType = caseType;
	}

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	private void addDefaultSelectItem(List<SelectItem> selectItems) {
		
		SelectItem item = new SelectItem();
		
		item.setValue("");
		item.setLabel("No selection");
		
		selectItems.add(item);
	}
	
	public List<SelectItem> getSimpleCasesProcessesDefinitions() {

		casesProcessesDefinitions.clear();
		addDefaultSelectItem(casesProcessesDefinitions);
		
		Session session = getSessionFactory().getCurrentSession();
		Transaction transaction = session.getTransaction();
		boolean transactionWasActive = transaction.isActive();
		
		if(!transactionWasActive)
			transaction.begin();
		
		try {
			
			//query from CasesJbpmBind
			@SuppressWarnings("unchecked")			
			List<Object[]> casesProcesses = session.getNamedQuery("casesJbpmBind.simpleCasesProcessesDefinitionsQuery").list();
			
			if(casesProcesses == null)
				return casesProcessesDefinitions;
			
			for (Object[] idAndName : casesProcesses) {
				
				SelectItem item = new SelectItem();
				
				item.setValue(String.valueOf(idAndName[0]));
				item.setLabel((String)idAndName[1]);
				casesProcessesDefinitions.add(item);
			}
			
			return casesProcessesDefinitions;
			
		} catch (Exception e) {
			setMessage("Exception occured");
			e.printStackTrace();
			casesProcessesDefinitions.clear();
			return casesProcessesDefinitions;
			
		} finally {
		
			if(!transactionWasActive)
				transaction.commit();
		}
	}

	public String getProcessDefinitionId() {
		return processDefinitionId;
	}

	public void setProcessDefinitionId(String processDefinitionId) {
		this.processDefinitionId = processDefinitionId;
	}

	public String getProcessInstanceId() {
		return processInstanceId;
	}

	public void setProcessInstanceId(String processInstanceId) {
		this.processInstanceId = processInstanceId;
	}
}