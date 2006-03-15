/*
 * $Id$
 * Created on Oct 30, 2005
 *
 * Copyright (C) 2005 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf.
 * Use is subject to license terms.
 */
package is.idega.idegaweb.egov.cases;

import is.idega.idegaweb.egov.cases.business.BizTalkEventListener;
import is.idega.idegaweb.egov.cases.business.CasesBusiness;
import is.idega.idegaweb.egov.cases.business.OneSystemEventListener;
import is.idega.idegaweb.egov.cases.util.CaseConstants;

import com.idega.block.process.business.CaseBusiness;
import com.idega.block.process.business.CaseCodeManager;
import com.idega.business.IBOLookup;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWBundleStartable;
import com.idega.idegaweb.include.GlobalIncludeManager;


public class IWBundleStarter implements IWBundleStartable {

	public void start(IWBundle starterBundle) {
		GlobalIncludeManager.getInstance().addBundleStyleSheet(CaseConstants.IW_BUNDLE_IDENTIFIER, "/style/case.css");
		CaseCodeManager.getInstance().addCaseBusinessForCode(CaseConstants.CASE_CODE_KEY, CasesBusiness.class);
		registerCaseChangeListener(starterBundle);
	}

	public void stop(IWBundle starterBundle) {
	}
	
	private void registerCaseChangeListener(IWBundle starterBundle) {
		IWApplicationContext iwac = starterBundle.getApplication().getIWApplicationContext();
		String sendToOneSystem = iwac.getApplicationSettings().getProperty("WS_SEND_ONE_SYSTEM", "false");
		String sendToBizTalk = iwac.getApplicationSettings().getProperty("WS_SEND_BIZ_TALK", "false");
		CaseBusiness caseBusiness;
		try {
			caseBusiness = (CaseBusiness)IBOLookup.getServiceInstance(iwac,CaseBusiness.class);
			//String choiceCaseCode = SchoolConstants.SCHOOL_CHOICE_CASE_CODE_KEY;
			//registering the event listener on when the schoolchoiceapplication gets status placed
			//@TODO fix the codes
			if (!sendToOneSystem.equals("false")) {
				System.out.println("Adding one system listeners");
				caseBusiness.addCaseChangeListener(new OneSystemEventListener(), CaseConstants.CASE_CODE_KEY);
				caseBusiness.addCaseChangeListener(new OneSystemEventListener(), "MBANBOP");
			}
			if (!sendToBizTalk.equals("false")) {
				System.out.println("Adding biz talk listener");
				caseBusiness.addCaseChangeListener(new BizTalkEventListener(), CaseConstants.CASE_CODE_KEY);
			}
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
