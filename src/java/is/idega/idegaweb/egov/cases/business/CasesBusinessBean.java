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
import java.util.Iterator;
import java.util.Locale;

import javax.ejb.CreateException;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;

import se.idega.idegaweb.commune.message.business.CommuneMessageBusiness;

import com.idega.block.process.business.CaseBusiness;
import com.idega.block.process.business.CaseBusinessBean;
import com.idega.block.process.data.Case;
import com.idega.block.process.data.CaseLog;
import com.idega.block.process.data.CaseStatus;
import com.idega.business.IBORuntimeException;
import com.idega.data.IDOLookup;
import com.idega.data.IDOLookupException;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.user.business.UserBusiness;
import com.idega.user.data.Group;
import com.idega.user.data.User;
import com.idega.util.text.Name;


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

	private UserBusiness getUserBusiness() {
		try {
			return (UserBusiness) this.getServiceInstance(UserBusiness.class);
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
			return MessageFormat.format(iwrb.getLocalizedString((genCase.getType() != null ? genCase.getType() + "." : "") + "case_code_key." + theCase.getCode(), theCase.getCode()), arguments);
		}
		catch (FinderException fe) {
			fe.printStackTrace();
			return super.getLocalizedCaseDescription(theCase, locale);
		}
	}

	public String getLocalizedCaseStatusDescription(Case theCase, CaseStatus status, Locale locale) {
		try {
			GeneralCase genCase = getGeneralCase(theCase.getPrimaryKey());
			IWResourceBundle iwrb = getBundle().getResourceBundle(locale);
			return iwrb.getLocalizedString((genCase.getType() != null ? genCase.getType() + "." : "") + "case_status_key." + theCase.getStatus(), theCase.getStatus());
		}
		catch (FinderException fe) {
			fe.printStackTrace();
			return super.getLocalizedCaseStatusDescription(theCase, status, locale);
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
	
	public Collection getAllCaseCategories() {
		try {
			return getCaseCategoryHome().findAll();
		}
		catch (FinderException fe) {
			fe.printStackTrace();
			return new ArrayList();
		}
	}
	
	public Collection getCaseCategories() {
		try {
			return getCaseCategoryHome().findAllTopLevelCategories();
		}
		catch (FinderException fe) {
			fe.printStackTrace();
			return new ArrayList();
		}
	}
	
	public Collection getSubCategories(CaseCategory category) {
		try {
			return getCaseCategoryHome().findAllSubCategories(category);
		}
		catch (FinderException fe) {
			fe.printStackTrace();
			return new ArrayList();
		}
	}
	
	public Collection getCaseLogs(GeneralCase theCase) {
		try {
			Collection logs = getCaseLogsByCase(theCase);
			Collection returner = new ArrayList(logs);
			User owner = theCase.getOwner();
			
			Iterator iter = logs.iterator();
			while (iter.hasNext()) {
				CaseLog log = (CaseLog) iter.next();
				if (log.getPerformer().equals(owner)) {
					returner.remove(log);
				}
				else if (log.getComment() == null || log.getComment().length() == 0) {
					returner.remove(log);
				}
			}
			
			return returner;
		}
		catch (FinderException e) {
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
	
	public void storeGeneralCase(User sender, Object caseCategoryPK, Object caseTypePK, String message, String type) throws CreateException {
		GeneralCase theCase = getGeneralCaseHome().create();
		CaseCategory category = null;
		try {
			category = getCaseCategory(caseCategoryPK);
		}
		catch (FinderException fe) {
			throw new CreateException("Trying to store a case with case category that has no relation to a group");
		}
		CaseType caseType = null;
		try {
			caseType = getCaseType(caseTypePK);
		}
		catch (FinderException fe) {
			throw new CreateException("Trying to store a case with case type that does not exist");
		}
		
		Group handlerGroup = category.getHandlerGroup();
		
		theCase.setCaseCategory(category);
		theCase.setCaseType(caseType);
		theCase.setOwner(sender);
		theCase.setHandler(handlerGroup);
		theCase.setMessage(message);
		theCase.setType(type);
		changeCaseStatus(theCase, getCaseStatusOpen().getStatus(), sender, (Group)null);
		
		try {
			Name name = new Name(sender.getFirstName(), sender.getMiddleName(), sender.getLastName());
			Object[] arguments = { name.getName(getIWApplicationContext().getApplicationSettings().getDefaultLocale()), theCase.getCaseCategory().getName(), message };
			String subject = getLocalizedString("case_sent_subject", "A new case sent in");
			String body = MessageFormat.format(getLocalizedString("case_sent_body", "A new case has been sent in by {0} in case category {1}. \n\nThe case is as follows:\n{2}"), arguments);
			
			Collection handlers = getUserBusiness().getUsersInGroup(handlerGroup);
			Iterator iter = handlers.iterator();
			while (iter.hasNext()) {
				User handler = (User) iter.next();
				sendMessage(theCase, handler, sender, subject, body);
			}
			
			if (sender != null) {
				Object[] arguments2 = { theCase.getCaseCategory().getName() };
				subject = getLocalizedString("case_sent_confirmation_subject", "A new case sent in");
				body = MessageFormat.format(getLocalizedString("case_sent_confirmation_body", "Your case with case category {0} has been received and will be processed."), arguments2);
				
				sendMessage(theCase, sender, null, subject, body);
			}
		}
		catch (RemoteException e) {
			throw new IBORuntimeException(e);
		}
	}
	
	public void handleCase(Object casePK, Object caseCategoryPK, Object caseTypePK, String status, User performer, String reply, Locale locale) throws FinderException {
		GeneralCase theCase = getGeneralCase(casePK);
		theCase.setReply(reply);
		
		CaseCategory category = getCaseCategory(caseCategoryPK);
		theCase.setCaseCategory(category);
		CaseType type = getCaseType(caseTypePK);
		theCase.setCaseType(type);
		
		changeCaseStatus(theCase, status, reply, performer, (Group)null, true);
		
		User owner = theCase.getOwner();
		if (owner != null) {
			Object[] arguments = { theCase.getCaseCategory().getName(), theCase.getCaseType().getName(), performer.getName(), reply, getLocalizedCaseStatusDescription(theCase, getCaseStatus(status), locale) };
			String subject = getLocalizedString("case_handled_subject", "Your case has been handled");
			String body = MessageFormat.format(getLocalizedString("case_handled_body", "Your case with category {0} and type {1} has been handled by {2}.  The reply was as follows:\n\n{3}"), arguments);
			
			sendMessage(theCase, owner, performer, subject, body);
		}
	}
	
	public void takeCase(Object casePK, User performer) throws FinderException {
		GeneralCase theCase = getGeneralCase(casePK);
		theCase.setHandledBy(performer);
		
		changeCaseStatus(theCase, getCaseStatusPending().getStatus(), performer, (Group)null);
		
		User owner = theCase.getOwner();
		if (owner != null) {
			Object[] arguments = { theCase.getCaseCategory().getName(), theCase.getCaseType().getName(), performer.getName() };
			String subject = getLocalizedString("case_taken_subject", "Your case has been taken");
			String body = MessageFormat.format(getLocalizedString("case_taken_body", "Your case with category {0} and type {1} has been put into process by {2}"), arguments);
			
			sendMessage(theCase, owner, performer, subject, body);
		}
	}
	
	public void reactivateCase(Object casePK, User performer) throws FinderException {
		GeneralCase theCase = getGeneralCase(casePK);
		theCase.setHandledBy(performer);
		
		changeCaseStatus(theCase, getCaseStatusPending().getStatus(), performer, (Group)null);
		
		User owner = theCase.getOwner();
		if (owner != null) {
			Object[] arguments = { theCase.getCaseCategory().getName(), theCase.getCaseType().getName(), performer.getName() };
			String subject = getLocalizedString("case_reactivated_subject", "Your case has been reactivated");
			String body = MessageFormat.format(getLocalizedString("case_reactivated_body", "Your case with category {0} and type {1} has been reactivated by {2}"), arguments);
			
			sendMessage(theCase, owner, performer, subject, body);
		}
	}
	
	public void storeCaseCategory(Object caseCategoryPK, Object parentCaseCategoryPK, String name, String description, Object groupPK, int order) throws FinderException, CreateException {
		CaseCategory category = null;
		if (caseCategoryPK != null) {
			category = getCaseCategory(caseCategoryPK);
		}
		else {
			category = getCaseCategoryHome().create();
		}
		
		CaseCategory parentCategory = null;
		if (parentCaseCategoryPK != null) {
			parentCategory = getCaseCategory(parentCaseCategoryPK);
		}
		
		category.setName(name);
		category.setDescription(description);
		category.setParent(parentCategory);
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
	
	public boolean useSubCategories() {
		return getIWApplicationContext().getApplicationSettings().getBoolean(CaseConstants.PROPERTY_USE_SUB_CATEGORIES, false);
	}
}