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

import is.idega.idegaweb.egov.cases.business.CasesBusiness;
import is.idega.idegaweb.egov.cases.util.CaseConstants;

import com.idega.block.process.business.CaseCodeManager;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWBundleStartable;
import com.idega.idegaweb.include.GlobalIncludeManager;


public class IWBundleStarter implements IWBundleStartable {

	public void start(IWBundle starterBundle) {
		GlobalIncludeManager.getInstance().addBundleStyleSheet(CaseConstants.IW_BUNDLE_IDENTIFIER, "/style/case.css");
		CaseCodeManager.getInstance().addCaseBusinessForCode(CaseConstants.CASE_CODE_KEY, CasesBusiness.class);
		registerCaseChangeListener(starterBundle);
		
		System.out.println("asdasd ______________________________________");
		CasesViewManager viewManager = CasesViewManager.getInstance(starterBundle.getApplication());
		viewManager.initializeStandardNodes(starterBundle);
	}

	public void stop(IWBundle starterBundle) {
	}
	
	private void registerCaseChangeListener(IWBundle starterBundle) {
	}
}