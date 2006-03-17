package is.idega.idegaweb.egov.cases.business;

import is.idega.idegaweb.egov.cases.data.GeneralCase;
import se.idega.idegaweb.commune.care.data.ChildCareApplication;

import com.idega.block.process.business.CaseChangeEvent;
import com.idega.block.process.business.CaseChangeListener;
import com.idega.block.process.business.CaseChangeResult;
import com.idega.block.process.data.Case;

public class OneSystemEventListener implements CaseChangeListener {

	public CaseChangeResult afterCaseChange(CaseChangeEvent event) {
		Case theCase = (Case) event.getCase();
		OneSystemSenderBean bean = new OneSystemSenderBean();

		if (theCase instanceof GeneralCase) {
			GeneralCase genCase = (GeneralCase) theCase;
			bean.setGeneralCase(genCase);
			
		} else if (theCase instanceof ChildCareApplication) {
			ChildCareApplication application = (ChildCareApplication) theCase;
			bean.setChildCareApplication(application);
		}
		
		Thread thread = new Thread(bean);
		thread.setName("OneSystemSenderBean");
		thread.start();
		
		return null;
	}

	public CaseChangeResult beforeCaseChange(CaseChangeEvent event) {
		return null;
	}
}