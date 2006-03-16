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

import is.idega.idegaweb.egov.cases.data.CaseCategory;
import is.idega.idegaweb.egov.cases.data.CaseCategoryHome;
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
import com.idega.user.data.Group;
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
	
	private CaseCategoryHome getCaseCategoryHome() {
		try {
			return (CaseCategoryHome) IDOLookup.getHome(CaseCategory.class);
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
			CaseCategory type = genCase.getCaseCategory();
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
	
	public Collection getOpenCases(Collection groups) {
		try {
			String[] statuses = { getCaseStatusOpen().getStatus() };
			return getGeneralCaseHome().findAllByGroupAndStatuses(groups, statuses);
		}
		catch (FinderException fe) {
			fe.printStackTrace();
			return new ArrayList();
		}
	}
	
	public Collection getMyCases(User handler) {
		try {
			String[] statuses = { getCaseStatusPending().getStatus(), getCaseStatusWaiting().getStatus() };
			return getGeneralCaseHome().findAllByHandlerAndStatuses(handler, statuses);
		}
		catch (FinderException fe) {
			fe.printStackTrace();
			return new ArrayList();
		}
	}
	
	public Collection getClosedCases(Collection groups) {
		try {
			String[] statuses = { getCaseStatusInactive().getStatus(), getCaseStatusReady().getStatus() };
			return getGeneralCaseHome().findAllByGroupAndStatuses(groups, statuses);
		}
		catch (FinderException fe) {
			fe.printStackTrace();
			return new ArrayList();
		}
	}
	
	public Collection getCasesByUsers(Collection users) {
		try {
			return getGeneralCaseHome().findAllByUsers(users);
		}
		catch (FinderException fe) {
			fe.printStackTrace();
			return new ArrayList();
		}
	}
	
	public CaseCategory getCaseCategory(Object caseCategoryPK) throws FinderException {
		return getCaseCategoryHome().findByPrimaryKey(new Integer(caseCategoryPK.toString()));
	}
	
	public Collection getCaseCategories() {
		try {
			return getCaseCategoryHome().findAll();
		}
		catch (FinderException fe) {
			fe.printStackTrace();
			return new ArrayList();
		}
	}
	
	public void removeCaseCategory(Object caseCategoryPK) throws FinderException, RemoveException {
		getCaseCategory(caseCategoryPK).remove();
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
	
	public void storeGeneralCase(User sender, Object caseCategoryPK, Object caseTypePK, String message) throws CreateException {
		GeneralCase theCase = getGeneralCaseHome().create();
		CaseCategory category = null;
		try {
			category = getCaseCategory(caseCategoryPK);
		}
		catch (FinderException fe) {
			throw new CreateException("Trying to store a case with case category that has no relation to a group");
		}
		CaseType type = null;
		try {
			type = getCaseType(caseTypePK);
		}
		catch (FinderException fe) {
			throw new CreateException("Trying to store a case with case type that has does not exist");
		}
		
		theCase.setCaseCategory(category);
		theCase.setCaseType(type);
		theCase.setOwner(sender);
		theCase.setHandler(category.getHandlerGroup());
		theCase.setMessage(message);
		changeCaseStatus(theCase, getCaseStatusOpen().getStatus(), sender, (Group)null);
	}
	
	public void handleCase(Object casePK, Object caseCategoryPK, Object caseTypePK, String status, User performer, String reply, Locale locale) throws FinderException {
		GeneralCase theCase = getGeneralCase(casePK);
		theCase.setReply(reply);
		
		CaseCategory category = getCaseCategory(caseCategoryPK);
		theCase.setCaseCategory(category);
		CaseType type = getCaseType(caseTypePK);
		theCase.setCaseType(type);
		
		changeCaseStatus(theCase, status, performer, (Group)null);
		
		Object[] arguments = { theCase.getCaseCategory().getName(), theCase.getCaseType().getName(), performer.getName(), reply, getLocalizedCaseStatusDescription(getCaseStatus(status), locale) };
		String subject = getLocalizedString("case_handled_subject", "Your case has been handled");
		String body = MessageFormat.format(getLocalizedString("case_handled_body", "Your case with category {0} and type {1} has been handled by {2}.  The reply was as follows:\n\n{3}"), arguments);
		
		sendMessage(theCase, theCase.getOwner(), performer, subject, body);
	}
	
	public void takeCase(Object casePK, User performer) throws FinderException {
		GeneralCase theCase = getGeneralCase(casePK);
		theCase.setHandledBy(performer);
		
		changeCaseStatus(theCase, getCaseStatusPending().getStatus(), performer, (Group)null);
		
		Object[] arguments = { theCase.getCaseCategory().getName(), theCase.getCaseType().getName(), performer.getName() };
		String subject = getLocalizedString("case_taken_subject", "Your case has been taken");
		String body = MessageFormat.format(getLocalizedString("case_taken_body", "Your case with category {0} and type {1} has been put into process by {2}"), arguments);
		
		sendMessage(theCase, theCase.getOwner(), performer, subject, body);
	}
	
	public void reactivateCase(Object casePK, User performer) throws FinderException {
		GeneralCase theCase = getGeneralCase(casePK);
		
		changeCaseStatus(theCase, getCaseStatusPending().getStatus(), performer, (Group)null);
		
		Object[] arguments = { theCase.getCaseCategory().getName(), theCase.getCaseType().getName(), performer.getName() };
		String subject = getLocalizedString("case_reactivated_subject", "Your case has been reactivated");
		String body = MessageFormat.format(getLocalizedString("case_reactivated_body", "Your case with category {0} and type {1} has been reactivated by {2}"), arguments);
		
		sendMessage(theCase, theCase.getOwner(), performer, subject, body);
	}
	
	public void storeCaseCategory(Object caseCategoryPK, String name, String description, Object groupPK, int order) throws FinderException, CreateException {
		CaseCategory category = null;
		if (caseCategoryPK != null) {
			category = getCaseCategory(caseCategoryPK);
		}
		else {
			category = getCaseCategoryHome().create();
		}
		
		category.setName(name);
		category.setDescription(description);
		category.setHandlerGroup(groupPK);
		if (order != -1) {
			category.setOrder(order);
		}
		category.store();
	}
	
	public void storeCaseType(Object caseTypePK, String name, String description, int order) throws FinderException, CreateException {
		CaseType type = null;
		if (caseTypePK != null) {
			type = getCaseType(caseTypePK);
		}
		else {
			type = getCaseTypeHome().create();
		}
		
		type.setName(name);
		type.setDescription(description);
		if (order != -1) {
			type.setOrder(order);
		}
		type.store();
	}
	
	private void sendMessage(GeneralCase theCase, User receiver, User sender, String subject, String body) {
		try {
			getMessageBusiness().createUserMessage(theCase, receiver, sender, subject, body, false);
		}
		catch (RemoteException re) {
			throw new IBORuntimeException(re);
		}
	}

	public boolean canDeleteCase(Case theCase) {
		return false;
	}
}