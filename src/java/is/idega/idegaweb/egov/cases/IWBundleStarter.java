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
import is.idega.idegaweb.egov.cases.util.CasesConstants;

import com.idega.block.process.business.CaseCodeManager;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWBundleStartable;


public class IWBundleStarter implements IWBundleStartable {

	public void start(IWBundle starterBundle) {
		CaseCodeManager.getInstance().addCaseBusinessForCode(CasesConstants.CASE_CODE_KEY, CasesBusiness.class);
		registerCaseChangeListener(starterBundle);
	}

	public void stop(IWBundle starterBundle) {
	}
	
	private void registerCaseChangeListener(IWBundle starterBundle) {
	}
}