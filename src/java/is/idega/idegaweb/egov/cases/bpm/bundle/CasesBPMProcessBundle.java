package is.idega.idegaweb.egov.cases.bpm.bundle;

import is.idega.idegaweb.egov.cases.util.CaseConstants;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Map.Entry;

import javax.faces.context.FacesContext;

import org.jbpm.graph.def.ProcessDefinition;

import com.idega.documentmanager.business.DocumentManagerFactory;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWMainApplication;
import com.idega.jbpm.def.ProcessBundle;
import com.idega.jbpm.def.ViewResource;
import com.idega.util.CoreConstants;

/**
 * 
 * @author <a href="civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.1 $
 * 
 * Last modified: $Date: 2008/01/27 13:11:23 $ by $Author: civilis $
 * 
 */
public class CasesBPMProcessBundle implements ProcessBundle {

	private static final String propertiesFileName = "bundle.properties";
	private static final String processDefinitionFileName = "processdefinition.xml";
	private static final String formsPath = "forms";
	private static final String XFFileNamePropertyPostfix = ".view.xforms.file_name";

	private IWBundle bundle;
	private List<ViewResource> viewResources;
	private String templateBundleLocationWithinBundle;
	private DocumentManagerFactory documentManagerFactory;

	public DocumentManagerFactory getDocumentManagerFactory() {
		return documentManagerFactory;
	}

	public void setDocumentManagerFactory(
			DocumentManagerFactory documentManagerFactory) {
		this.documentManagerFactory = documentManagerFactory;
	}

	public ProcessDefinition getProcessDefinition() throws IOException {

		String templateBundleLocationWithinBundle = getTemplateBundleLocationWithinBundle();

		if (templateBundleLocationWithinBundle == null)
			throw new IllegalStateException(
					"No templateBundleLocationWithinBundle set");

		InputStream pdIs = getBundle().getResourceInputStream(
				templateBundleLocationWithinBundle + processDefinitionFileName);
		ProcessDefinition pd = ProcessDefinition.parseXmlInputStream(pdIs);
		return pd;
	}

	public List<ViewResource> getViewResources(String taskName)
			throws IOException {

		if (viewResources == null) {

			String templateBundleLocationWithinBundle = getTemplateBundleLocationWithinBundle();

			if (templateBundleLocationWithinBundle == null)
				throw new IllegalStateException(
						"No templateBundleLocationWithinBundle set");

			String formsPathWithin = templateBundleLocationWithinBundle
					+ formsPath;

			InputStream propertiesIs = bundle
					.getResourceInputStream(templateBundleLocationWithinBundle
							+ propertiesFileName);

			Properties properties = new Properties();
			properties.load(propertiesIs);

			for (Entry<Object, Object> entry : properties.entrySet()) {

				if (taskName.equals(entry.getValue())) {

					String taskIdentifier = ((String) entry.getKey())
							.split(CoreConstants.DOT)[0];
					String fileName = properties.getProperty(taskIdentifier
							+ XFFileNamePropertyPostfix);

					CasesBPMBundledFormViewResource resource = new CasesBPMBundledFormViewResource();
					resource.setTaskName(taskName);
					resource.setDocumentManagerFactory(getDocumentManagerFactory());
					String pathWithinBundle = formsPathWithin + fileName;
					resource.setResourceLocation(getBundle(), pathWithinBundle);

					viewResources = new ArrayList<ViewResource>(1);
					viewResources.add(resource);
					break;
				}
			}
		}

		return viewResources;
	}

	public IWBundle getBundle() {

		if (bundle == null) {
			IWMainApplication iwma = IWMainApplication
					.getIWMainApplication(FacesContext.getCurrentInstance());
			bundle = iwma.getBundle(CaseConstants.IW_BUNDLE_IDENTIFIER);
		}
		return bundle;
	}

	public void setBundle(IWBundle bundle) {
		this.bundle = bundle;
	}

	public String getTemplateBundleLocationWithinBundle() {
		return templateBundleLocationWithinBundle;
	}

	public void setTemplateBundleLocationWithinBundle(
			String templateBundleLocationWithinBundle) {
		this.templateBundleLocationWithinBundle = templateBundleLocationWithinBundle;
	}
}