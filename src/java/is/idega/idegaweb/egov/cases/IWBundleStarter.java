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

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.idega.block.process.business.CaseCodeManager;
import com.idega.block.process.data.Case;
import com.idega.data.IDOLookup;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWBundleStartable;
import com.idega.idegaweb.IWMainApplication;
import com.idega.idegaweb.IWMainApplicationSettings;
import com.idega.util.ListUtil;

import is.idega.idegaweb.egov.cases.business.CasesBusiness;
import is.idega.idegaweb.egov.cases.data.GeneralCase;
import is.idega.idegaweb.egov.cases.data.GeneralCaseBMPBean;
import is.idega.idegaweb.egov.cases.data.GeneralCaseHome;
import is.idega.idegaweb.egov.cases.util.CasesConstants;


public class IWBundleStarter implements IWBundleStartable {

	@Override
	public void start(IWBundle starterBundle) {
		CaseCodeManager.getInstance().addCaseBusinessForCode(CasesConstants.CASE_CODE_KEY, CasesBusiness.class);
		registerCaseChangeListener(starterBundle);
		refactorGeneralCaseTableAttachments(starterBundle);
	}

	@Override
	public void stop(IWBundle starterBundle) {
	}

	private void registerCaseChangeListener(IWBundle starterBundle) {
	}
	private Logger getLogger(){
		return Logger.getLogger(IWBundleStarter.class.getName());
	}
	private void refactorGeneralCaseTableAttachments(IWBundle starterBundle){
		try{
			IWMainApplication iwma = starterBundle.getApplication();
			IWMainApplicationSettings settings = iwma.getSettings();
			if(settings.getBoolean("refactored_cases_mtm", false)){
				return;
			}
			getLogger().info("Refactoring generalCases for many to many relation");
			GeneralCaseHome generalCaseHome = (GeneralCaseHome) IDOLookup.getHome(GeneralCase.class);
			int max = 2000;
			int count = 0;
			for(Collection<Case> cases = generalCaseHome.getCasesWithONeToOneAttachments(0, max);
					!ListUtil.isEmpty(cases);
					cases = generalCaseHome.getCasesWithONeToOneAttachments(0, max)){
				for(Case c : cases){
					GeneralCaseBMPBean generalCase = (GeneralCaseBMPBean) c;
					generalCase.addAttachment(generalCase.getAttachment());
					generalCase.setAttachment(null);
					generalCase.store();
				}
				count = count + max;
				getLogger().info("Refactored " + count + " cases");
			}
			settings.setProperty("refactored_cases_mtm", "true");
			getLogger().info("Refactored generalCases for many to many relation");
		}catch (Exception e) {
			getLogger().log(Level.WARNING, "Failed refactoring database for many to many case attachments", e);
		}
	}
}