/*
 * $Id$
 * Created on Oct 30, 2005
 *
 * Copyright (C) 2005 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf.
 * Use is subject to license terms.
 */
package is.idega.idegaweb.egov.cases.business;

import is.idega.idegaweb.egov.cases.data.CaseType;
import is.idega.idegaweb.egov.cases.data.CaseTypeHome;
import is.idega.idegaweb.egov.cases.data.GeneralCase;
import is.idega.idegaweb.egov.cases.data.GeneralCaseHome;
import is.idega.idegaweb.egov.cases.util.CaseConstants;
import java.rmi.RemoteException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import javax.ejb.CreateException;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;
import se.idega.idegaweb.commune.message.business.CommuneMessageBusiness;
import com.idega.block.process.business.CaseBusiness;
import com.idega.block.process.business.CaseBusinessBean;
import com.idega.block.process.data.Case;
import com.idega.business.IBORuntimeException;
import com.idega.data.IDOLookup;
import com.idega.data.IDOLookupException;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.user.data.User;


public class CasesBusinessBean extends CaseBusinessBean implements CaseBusiness , CasesBusiness{

	protected String getBundleIdentifier() {
		return CaseConstants.IW_BUNDLE_IDENTIFIER;
	}

	private CommuneMessageBusiness getMessageBusiness() {
		try {
			return (CommuneMessageBusiness) this.getServiceInstance(CommuneMessageBusiness.class);
		}
		catch (RemoteException e) {
			throw new IBORuntimeException(e.getMessage());
		}
	}

	private GeneralCaseHome getGeneralCaseHome() {
		try {
			return (GeneralCaseHome) IDOLookup.getHome(GeneralCase.class);
		}
		catch (IDOLookupException ile) {
			throw new IBORuntimeException(ile);
		}
	}
	
	private CaseTypeHome getCaseTypeHome() {
		try {
			return (CaseTypeHome) IDOLookup.getHome(CaseType.class);
		}
		catch (IDOLookupException ile) {
			throw new IBORuntimeException(ile);
		}
	}
	
	public String getLocalizedCaseDescription(Case theCase, Locale locale) {
		try {
			GeneralCase genCase = getGeneralCase(theCase.getPrimaryKey());
			CaseType type = genCase.getCaseType();
			Object[] arguments = { type.getName() };
			
			IWResourceBundle iwrb = getBundle().getResourceBundle(locale);
			return MessageFormat.format(iwrb.getLocalizedString("case_code_key." + theCase.getCode(), theCase.getCode()), arguments);
		}
		catch (FinderException fe) {
			fe.printStackTrace();
			return super.getLocalizedCaseDescription(theCase, locale);
		}
	}

	public GeneralCase getGeneralCase(Object casePK) throws FinderException {
		return getGeneralCaseHome().findByPrimaryKey(new Integer(casePK.toString()));
	}
	
	public Collection getCases(Collection groups) {
		try {
			String[] statuses = { getCaseStatusOpen().getStatus(), getCaseStatusReady().getStatus() };
			return getGeneralCaseHome().findAllByGroupAndStatuses(groups, statuses);
		}
		catch (FinderException fe) {
			fe.printStackTrace();
			return new ArrayList();
		}
	}
	
	public CaseType getCaseType(Object caseTypePK) throws FinderException {
		return getCaseTypeHome().findByPrimaryKey(new Integer(caseTypePK.toString()));
	}
	
	public Collection getCaseTypes() {
		try {
			return getCaseTypeHome().findAll();
		}
		catch (FinderException fe) {
			fe.printStackTrace();
			return new ArrayList();
		}
	}
	
	public void removeCaseType(Object caseTypePK) throws FinderException, RemoveException {
		getCaseType(caseTypePK).remove();
	}
	
	public void storeGeneralCase(User sender, Object caseTypePK, String message, String caseNumber) throws CreateException {
		GeneralCase theCase = getGeneralCaseHome().create();
		CaseType type = null;
		try {
			type = getCaseType(caseTypePK);
		}
		catch (FinderException fe) {
			throw new CreateException("Trying to store a case with case type that has no relation to a group");
		}
		
		theCase.setCaseType(type);
		theCase.setOwner(sender);
		theCase.setHandler(type.getHandlerGroup());
		theCase.setMessage(message);
		theCase.setCaseNumber(caseNumber);
		changeCaseStatus(theCase, getCaseStatusOpen().getStatus(), sender, null);
	}
	
	public void handleCase(Object casePK, User performer, String reply) throws FinderException {
		GeneralCase theCase = getGeneralCase(casePK);
		theCase.setReply(reply);
		
		changeCaseStatus(theCase, getCaseStatusReady().getStatus(), performer, null);
	}
	
	public void storeCaseType(Object caseTypePK, String name, String description, boolean requiresCaseNumber, Object groupPK) throws FinderException, CreateException {
		CaseType type = null;
		if (caseTypePK != null) {
			type = getCaseType(caseTypePK);
		}
		else {
			type = getCaseTypeHome().create();
		}
		
		type.setName(name);
		type.setDescription(description);
		type.setRequiresCaseNumber(requiresCaseNumber);
		type.setHandlerGroup(groupPK);
		type.store();
	}
	
	private void sendMessage(GeneralCase theCase, User receiver, String subject, String body) {
		try {
			getMessageBusiness().createUserMessage(theCase, receiver, subject, body, false);
		}
		catch (RemoteException re) {
			throw new IBORuntimeException(re);
		}
	}

	public boolean canDeleteCase(Case theCase) {
		if (theCase.getCaseStatus().equals(getCaseStatusOpen())) {
			return true;
		}
		return false;
	}
}