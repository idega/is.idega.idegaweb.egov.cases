package is.idega.idegaweb.egov.cases.jbpm.bundle;

import org.hibernate.SessionFactory;
import org.jbpm.JbpmConfiguration;

import com.idega.documentmanager.business.DocumentManagerFactory;
import com.idega.documentmanager.business.PersistenceManager;
import com.idega.jbpm.def.ViewFactory;
import com.idega.jbpm.def.ViewToTask;

/**
 * 
 * @author <a href="civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.1 $
 *
 * Last modified: $Date: 2007/10/30 22:00:02 $ by $Author: civilis $
 *
 */
public class CasesJbpmFormsBundleFactory {
	
	private DocumentManagerFactory documentManagerFactory;
	private PersistenceManager persistenceManager;
	private ViewFactory viewFactory;
	private JbpmConfiguration jbpmConfiguration;
	private SessionFactory sessionFactory;
	private ViewToTask viewToTaskBinder;
	
	public CasesJbpmFormsBundle newCasesJbpmFormsBundle() {
		
		CasesJbpmFormsBundle bundle = new CasesJbpmFormsBundle();
		bundle.setDocumentManagerFactory(getDocumentManagerFactory());
		bundle.setPersistenceManager(getPersistenceManager());
		bundle.setViewFactory(getViewFactory());
		bundle.setJbpmConfiguration(getJbpmConfiguration());
		bundle.setSessionFactory(getSessionFactory());
		bundle.setViewToTaskBinder(getViewToTaskBinder());
		
		return bundle;
	}

	public DocumentManagerFactory getDocumentManagerFactory() {
		return documentManagerFactory;
	}

	public void setDocumentManagerFactory(
			DocumentManagerFactory documentManagerFactory) {
		this.documentManagerFactory = documentManagerFactory;
	}

	public PersistenceManager getPersistenceManager() {
		return persistenceManager;
	}

	public void setPersistenceManager(PersistenceManager persistenceManager) {
		this.persistenceManager = persistenceManager;
	}

	public ViewFactory getViewFactory() {
		return viewFactory;
	}

	public void setViewFactory(ViewFactory viewFactory) {
		this.viewFactory = viewFactory;
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

	public ViewToTask getViewToTaskBinder() {
		return viewToTaskBinder;
	}

	public void setViewToTaskBinder(ViewToTask viewToTaskBinder) {
		this.viewToTaskBinder = viewToTaskBinder;
	}
}