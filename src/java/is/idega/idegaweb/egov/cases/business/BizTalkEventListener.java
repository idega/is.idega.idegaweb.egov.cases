package is.idega.idegaweb.egov.cases.business;

import is.idega.idegaweb.egov.cases.data.GeneralCase;

import com.idega.block.process.business.CaseChangeEvent;
import com.idega.block.process.business.CaseChangeListener;
import com.idega.block.process.business.CaseChangeResult;

public class BizTalkEventListener implements CaseChangeListener {

	public CaseChangeResult beforeCaseChange(CaseChangeEvent event) {
		return null;
	}

	public CaseChangeResult afterCaseChange(CaseChangeEvent event) {
		BizTalkSenderBean bean = new BizTalkSenderBean();

		GeneralCase genCase = (GeneralCase) event.getCase();
		bean.setGeneralCase(genCase);

		Thread thread = new Thread(bean);
		thread.setName("BizTalkSenderBean");
		thread.start();

		return null;
	}
}