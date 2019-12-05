/*
 * $Id$ Created on Oct 30, 2005
 *
 * Copyright (C) 2005 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf. Use is subject to license terms.
 */
package is.idega.idegaweb.egov.cases.business;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import javax.ejb.CreateException;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;
import javax.faces.context.FacesContext;

import com.idega.block.process.business.CaseBusiness;
import com.idega.block.process.business.CaseBusinessBean;
import com.idega.block.process.business.CaseManagersProvider;
import com.idega.block.process.business.ProcessConstants;
import com.idega.block.process.data.Case;
import com.idega.block.process.data.CaseBMPBean;
import com.idega.block.process.data.CaseStatus;
import com.idega.block.text.data.LocalizedText;
import com.idega.block.text.data.LocalizedTextHome;
import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.business.IBORuntimeException;
import com.idega.core.contact.data.Email;
import com.idega.core.contact.data.Phone;
import com.idega.core.file.data.ICFile;
import com.idega.core.file.data.ICFileHome;
import com.idega.core.localisation.business.ICLocaleBusiness;
import com.idega.data.IDOLookup;
import com.idega.data.IDOLookupException;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWMainApplication;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.idegaweb.IWUserContext;
import com.idega.presentation.IWContext;
import com.idega.user.business.GroupBusiness;
import com.idega.user.business.NoEmailFoundException;
import com.idega.user.business.NoPhoneFoundException;
import com.idega.user.business.UserBusiness;
import com.idega.user.data.Group;
import com.idega.user.data.User;
import com.idega.util.ArrayUtil;
import com.idega.util.CoreConstants;
import com.idega.util.IWTimestamp;
import com.idega.util.ListUtil;
import com.idega.util.StringHandler;
import com.idega.util.StringUtil;
import com.idega.util.database.ConnectionBroker;
import com.idega.util.text.Name;
import com.idega.webface.WFUtil;

import is.idega.idegaweb.egov.cases.data.CaseCategory;
import is.idega.idegaweb.egov.cases.data.CaseCategoryHome;
import is.idega.idegaweb.egov.cases.data.CaseType;
import is.idega.idegaweb.egov.cases.data.CaseTypeHome;
import is.idega.idegaweb.egov.cases.data.GeneralCase;
import is.idega.idegaweb.egov.cases.data.GeneralCaseHome;
import is.idega.idegaweb.egov.cases.presentation.CasesStatistics;
import is.idega.idegaweb.egov.cases.presentation.CasesStatistics.Result;
import is.idega.idegaweb.egov.cases.util.CasesConstants;
import is.idega.idegaweb.egov.message.business.CommuneMessageBusiness;

public class CasesBusinessBean extends CaseBusinessBean implements CaseBusiness, CasesBusiness {

	private static final long serialVersionUID = -1807320113180412800L;

	@Override
	protected String getBundleIdentifier() {
		return CasesConstants.IW_BUNDLE_IDENTIFIER;
	}

	@Override
	public Collection getCasesByCriteria(CaseCategory parentCategory, CaseCategory category, CaseType type, CaseStatus status, Date fromDate, Date toDate, Boolean anonymous) {
		try {
			return getGeneralCaseHome().findByCriteria(parentCategory, category, type, status, fromDate, toDate, anonymous);
		} catch (FinderException fe) {
			fe.printStackTrace();
			return new ArrayList();
		}
	}

	@Override
	public void sendReminder(GeneralCase theCase, User receiver, User sender, String message, IWContext iwc) {
		IWResourceBundle iwrb = this.getIWResourceBundleForUser(receiver, iwc);

		Object[] args = { theCase.getPrimaryKey().toString(), sender.getName(), message };
		String subject = iwrb.getLocalizedString("case_reminder_subject", "You have received a reminder for a case");
		String body = MessageFormat.format(iwrb.getLocalizedString("case_reminder_body", "{1} has sent you a reminder for case nr. {0} with the following message:\n{2}"), args);

		sendMessage(theCase, receiver, sender, subject, body);
	}

	@Override
	public Map<String, String> getAllSubCategories(String categoryPK, String country) {
		Map<String, String> map = new LinkedHashMap<String, String>();
		Locale locale = new Locale(country, country.toUpperCase());

		if (categoryPK != null && categoryPK.length() > 0 && Integer.parseInt(categoryPK) > -1) {
			CaseCategory category = null;
			try {
				category = getCaseCategory(categoryPK);
			} catch (FinderException e) {
				e.printStackTrace();
			}

			if (category != null) {
				Collection coll = getSubCategories(category);

				if (!coll.isEmpty()) {
					map.put("", getLocalizedString("case_creator.select_sub_category", "Select sub category", locale));

					Iterator iter = coll.iterator();
					while (iter.hasNext()) {
						CaseCategory subCategory = (CaseCategory) iter.next();
						map.put(subCategory.getPrimaryKey().toString(), subCategory.getName());
					}
				} else {
					map.put(categoryPK, getLocalizedString("case_creator.no_sub_category", "no sub category", locale));
				}
			}
		}
		return map;
	}

	@Override
	public Map<String, String> getUsers(String categoryPK) {
		try {
			Map<String, String> map = new LinkedHashMap<String, String>();

			if (categoryPK != null && categoryPK.length() > 0 && Integer.parseInt(categoryPK) > -1) {
				CaseCategory category = null;
				try {
					category = getCaseCategory(categoryPK);
				} catch (FinderException e) {
					e.printStackTrace();
				}

				if (category != null) {
					Group handlerGroup = category.getHandlerGroup();

					Collection<User> handlers = getUserBusiness().getUsersInGroup(handlerGroup);
					if (!handlers.isEmpty()) {
						Iterator<User> iter = handlers.iterator();
						while (iter.hasNext()) {
							User handler = iter.next();
							map.put(handler.getPrimaryKey().toString(), handler.getName());
						}
					}
				}
			}
			return map;
		} catch (RemoteException re) {
			throw new IBORuntimeException(re);
		}
	}

	@Override
	public UserDWR getUser(String personalID) {
		try {
			User user = getUserBusiness().getUser(personalID);

			UserDWR dwr = new UserDWR();
			dwr.setUserPersonalID(personalID);
			dwr.setUserName(new Name(user.getFirstName(), user.getMiddleName(), user.getLastName()).getName());

			try {
				Phone homePhone = getUserBusiness().getUsersHomePhone(user);
				dwr.setUserPhone(homePhone.getNumber());
			}
			catch (NoPhoneFoundException e) {
				// No phone found...
			}

			try {
				Email email = getUserBusiness().getUsersMainEmail(user);
				dwr.setUserEmail(email.getEmailAddress());
			}
			catch (NoEmailFoundException e) {
				// No email found...
			}

			return dwr;
		}
		catch (RemoteException re) {
			throw new IBORuntimeException(re);
		}
		catch (FinderException e) {
			return new UserDWR();
		}
	}

	@Override
	public void reactivateCase(GeneralCase theCase, User performer, IWContext iwc) throws FinderException {
		theCase.setHandledBy(performer);

		changeCaseStatus(theCase, getCaseStatusPending().getStatus(), performer, (Group) null);

		User owner = theCase.getOwner();
		if (owner != null) {
			IWResourceBundle iwrb = this.getIWResourceBundleForUser(owner, iwc);

			String prefix = theCase.getType() != null ? theCase.getType() + "." : "";
			Object[] arguments = { theCase.getCaseCategory().getLocalizedCategoryName(iwrb.getLocale()), theCase.getCaseType().getName(), performer.getName() };
			String subject = iwrb.getLocalizedString(prefix + "case_reactivated_subject", "Your case has been reactivated");
			String body = MessageFormat.format(iwrb.getLocalizedString(prefix + "case_reactivated_body", "Your case with category {0} and type {1} has been reactivated by {2}"), arguments);

			sendMessage(theCase, owner, performer, subject, body);
		}
	}

	private CommuneMessageBusiness getMessageBusiness() {
		try {
			return this.getServiceInstance(CommuneMessageBusiness.class);
		} catch (RemoteException e) {
			throw new IBORuntimeException(e.getMessage());
		}
	}

	private UserBusiness getUserBusiness() {
		try {
			return this.getServiceInstance(UserBusiness.class);
		} catch (RemoteException e) {
			throw new IBORuntimeException(e.getMessage());
		}
	}

	@Override
	public GeneralCaseHome getGeneralCaseHome() {
		try {
			return (GeneralCaseHome) IDOLookup.getHome(GeneralCase.class);
		} catch (IDOLookupException ile) {
			throw new IBORuntimeException(ile);
		}
	}

	private CaseCategoryHome getCaseCategoryHome() {
		try {
			return (CaseCategoryHome) IDOLookup.getHome(CaseCategory.class);
		} catch (IDOLookupException ile) {
			throw new IBORuntimeException(ile);
		}
	}

	private CaseTypeHome getCaseTypeHome() {
		try {
			return (CaseTypeHome) IDOLookup.getHome(CaseType.class);
		} catch (IDOLookupException ile) {
			throw new IBORuntimeException(ile);
		}
	}

	@Override
	public ICFile getAttachment(Object attachmentPK) {
		try {
			return ((ICFileHome) IDOLookup.getHome(ICFile.class)).findByPrimaryKey(attachmentPK);
		} catch (IDOLookupException e) {
			e.printStackTrace();
		} catch (FinderException e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public String getLocalizedCaseDescription(Case theCase, Locale locale) {
		try {
			String code = theCase.getCode();
			String typeOrCodeKey = null;

			Object[] arguments = null;
			if (theCase instanceof GeneralCase) {
				GeneralCase genCase = getGeneralCase(theCase.getPrimaryKey());
				CaseCategory type = genCase.getCaseCategory();
				arguments = new Object [] {type.getLocalizedCategoryName(locale)};
				typeOrCodeKey = genCase.getType();
			}

			IWResourceBundle iwrb = getBundle().getResourceBundle(locale);
			String allKey = (StringUtil.isEmpty(typeOrCodeKey) ? CoreConstants.EMPTY : CoreConstants.DOT) + "case_code_key." + code;
			String localizedDescription = iwrb.getLocalizedString(allKey, code);
			if (StringUtil.isEmpty(localizedDescription) || localizedDescription.equals(code)) {
				localizedDescription = super.getLocalizedString(allKey, code, locale, super.getBundleIdentifier());
			}
			if (StringUtil.isEmpty(localizedDescription) || localizedDescription.equals(code)) {
				localizedDescription = super.getLocalizedCaseDescription(theCase, locale);
			}
			return MessageFormat.format(localizedDescription, arguments);
		} catch (FinderException fe) {
			fe.printStackTrace();
			return super.getLocalizedCaseDescription(theCase, locale);
		}
	}

	@Override
	public String getLocalizedCaseStatusDescription(CaseStatus status, Locale locale) {
		return super.getLocalizedCaseStatusDescription(null, status, locale);
	}

	@Override
	public String getLocalizedCaseStatusDescription(Case theCase, CaseStatus status, Locale locale) {
		return getLocalizedCaseStatusDescription(theCase, status, locale, getBundleIdentifier());
	}

	@Override
	public String getLocalizedCaseStatusDescription(Case theCase, CaseStatus status, Locale locale, String bundleIdentifier) {
		try {
			String statusKey = status.getStatus();
			String key = ProcessConstants.CASE_STATUS_KEY + CoreConstants.DOT + statusKey;

			String typeOrCodeKey = null;
			if (theCase instanceof GeneralCase) {
				GeneralCase genCase = (GeneralCase) theCase;
				typeOrCodeKey = genCase.getType() == null ? CoreConstants.EMPTY : genCase.getType() + CoreConstants.DOT;
			}

			IWResourceBundle iwrb = getIWMainApplication().getBundle(bundleIdentifier).getResourceBundle(locale);
			if (theCase == null) {
				return iwrb.getLocalizedString(key, statusKey);
			}
			return iwrb.getLocalizedString((StringUtil.isEmpty(typeOrCodeKey) ? CoreConstants.EMPTY : typeOrCodeKey) + key, statusKey);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return theCase.getStatus();
	}

	@Override
	public GeneralCase getGeneralCase(Object casePK) throws FinderException {
		return getGeneralCaseHome().findByPrimaryKey(new Integer(casePK.toString()));
	}

	@Override
	public Collection getOpenCases(Collection groups) {

		return getOpenCases(groups, new String[] {});
	}

	@Override
	public Collection getOpenCases(Collection groups, String[] caseHandlers) {

		try {
			String[] statuses = getStatusesForOpenCases();
			return getGeneralCaseHome().findAllByGroupAndStatuses(groups, statuses, caseHandlers);
		} catch (FinderException fe) {
			fe.printStackTrace();
			return new ArrayList();
		}
	}

	@Override
	public Collection getMyCases(User handler) {

		return getMyCases(handler, new String[] {});
	}

	@Override
	public Collection getMyCases(User handler, String[] caseHandlers) {
		try {
			String[] statuses = getStatusesForMyCases();
			return getGeneralCaseHome().findAllByHandlerAndStatuses(handler, statuses, caseHandlers);
		} catch (FinderException fe) {
			fe.printStackTrace();
			return new ArrayList();
		}
	}

	@Override
	public Collection getClosedCases(Collection groups) {

		return getClosedCases(groups, new String[] {});
	}

	@Override
	public Collection getClosedCases(Collection groups, String[] caseHandlers) {
		try {
			String[] statuses = getStatusesForClosedCases();
			return getGeneralCaseHome().findAllByGroupAndStatuses(groups, statuses, caseHandlers);
		} catch (FinderException fe) {
			fe.printStackTrace();
			return new ArrayList();
		}
	}

	@Override
	public Collection getCasesByUsers(Collection users) {
		try {
			return getGeneralCaseHome().findAllByUsers(users);
		} catch (FinderException fe) {
			fe.printStackTrace();
			return new ArrayList();
		}
	}

	@Override
	public Collection getCasesByMessage(String message) {
		try {
			return getGeneralCaseHome().findAllByMessage(message);
		} catch (FinderException fe) {
			fe.printStackTrace();
			return new ArrayList();
		}
	}

	@Override
	public Collection getCasesByCriteria(CaseCategory parentCategory, CaseCategory category, CaseType type, CaseStatus status, Boolean anonymous) {
		return getCasesByCriteria(parentCategory, category, type, status, anonymous, null);
	}

	@Override
	public Collection getCasesByCriteria(CaseCategory parentCategory, CaseCategory category, CaseType type, CaseStatus status, Boolean anonymous, String caseHandler) {
		try {
			return getGeneralCaseHome().findByCriteria(parentCategory, category, type, status, anonymous, caseHandler);
		} catch (FinderException fe) {
			fe.printStackTrace();
			return new ArrayList();
		}
	}

	@Override
	public CaseCategory getCaseCategory(Object caseCategoryPK) throws FinderException {
		return getCaseCategoryHome().findByPrimaryKey(new Integer(caseCategoryPK.toString()));
	}

	@Override
	public Collection getAllCaseCategories() {
		try {
			return getCaseCategoryHome().findAll();
		} catch (FinderException fe) {
			fe.printStackTrace();
			return new ArrayList();
		}
	}

	@Override
	public Collection<CaseCategory> getCaseCategoriesByName(String name) {
		try {
			return getCaseCategoryHome().findByName(name);
		} catch (FinderException fe) {
			fe.printStackTrace();
			return new ArrayList<CaseCategory>(0);
		}
	}

	@Override
	public Collection<CaseType> getCaseTypesByName(String name) {
		try {
			return getCaseTypeHome().findByName(name);
		} catch (FinderException fe) {
			fe.printStackTrace();
			return new ArrayList<CaseType>(0);
		}
	}

	@Override
	public Collection getCaseCategories() {
		try {
			return getCaseCategoryHome().findAllTopLevelCategories();
		} catch (FinderException fe) {
			fe.printStackTrace();
			return new ArrayList();
		}
	}
	@Override
	public Collection<CaseCategory> getCaseCategoriesForAdmins(){
		return getCaseCategoryHome().findAllTopLevelCategoriesForAdmins();
	}

	@Override
	public Collection getSubCategories(CaseCategory category) {
		try {
			return getCaseCategoryHome().findAllSubCategories(category);
		} catch (FinderException fe) {
			fe.printStackTrace();
			return new ArrayList();
		}
	}

	@Override
	public Collection getCaseLogs(GeneralCase theCase) {
		try {
			Collection logs = getCaseLogsByCase(theCase);

			return logs;
		} catch (FinderException e) {
			return new ArrayList();
		}
	}

	@Override
	public void removeCaseCategory(Object caseCategoryPK) throws FinderException, RemoveException {
		CaseCategory caseCategory = getCaseCategory(caseCategoryPK);
		caseCategory.setDeleted(Boolean.TRUE);
		caseCategory.store();
	}

	@Override
	public CaseType getCaseType(Object caseTypePK) throws FinderException {
		Integer id = null;
		if (caseTypePK instanceof Number) {
			id = ((Number) caseTypePK).intValue();
		} else if (caseTypePK != null) {
			String tmp = caseTypePK.toString();
			if (StringHandler.isNumeric(tmp)) {
				id = Integer.valueOf(tmp);
			}
		}
		return id == null ? null : getCaseTypeHome().findByPrimaryKey(id);
	}

	@Override
	public Collection getCaseTypes() {
		try {
			return getCaseTypeHome().findAll();
		} catch (FinderException fe) {
			fe.printStackTrace();
			return new ArrayList();
		}
	}

	@Override
	public CaseType getFirstAvailableCaseType() {
		try {
			return getCaseTypeHome().findFirstType();
		} catch (FinderException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void removeCaseType(Object caseTypePK) throws FinderException, RemoveException {
		getCaseType(caseTypePK).remove();
	}

	@Override
	public GeneralCase storeGeneralCase(User sender, Object caseCategoryPK, Object caseTypePK, Object attachmentPK, String regarding, String message, String type, boolean isPrivate, IWResourceBundle iwrb, boolean setType, Timestamp created) throws CreateException, RemoteException {
		return storeGeneralCase(sender, caseCategoryPK, caseTypePK, attachmentPK, regarding, message, type, null, isPrivate, iwrb, setType, created);
	}
	@Override
	public GeneralCase storeGeneralCase(User sender, Object caseCategoryPK, Object caseTypePK, Collection<Object> attachmentPKs, String regarding, String message, String type, boolean isPrivate, IWResourceBundle iwrb, boolean setType, Timestamp created) throws CreateException, RemoteException {
		GeneralCase theCase = getGeneralCaseHome().create();
		return storeGeneralCase(theCase, sender, caseCategoryPK, caseTypePK, attachmentPKs, regarding, message, type, null, isPrivate, iwrb, true, null, setType, null, created);
	}

	@Override
	public GeneralCase storeGeneralCase(User sender, Object caseCategoryPK, Object caseTypePK, Object attachmentPK, String regarding, String message, String type, String caseManagerType, boolean isPrivate, IWResourceBundle iwrb, boolean setType, Timestamp created) throws CreateException, RemoteException {
		return storeGeneralCase(sender, caseCategoryPK, caseTypePK, attachmentPK, regarding, message, type, caseManagerType, isPrivate, iwrb, true, null, setType, null, created);
	}

	@Override
	public GeneralCase storeGeneralCase(User sender, Object caseCategoryPK, Object caseTypePK, Object attachmentPK, String regarding, String message, String type, String caseManagerType, boolean isPrivate, IWResourceBundle iwrb, boolean sendMessages, String caseIdentifier, boolean setType, String caseStatusKey, Timestamp created) throws CreateException, RemoteException {
		GeneralCase theCase = getGeneralCaseHome().create();
		return storeGeneralCase(theCase, sender, caseCategoryPK, caseTypePK, attachmentPK, regarding, message, type, caseManagerType, isPrivate, iwrb, sendMessages, caseIdentifier, setType, caseStatusKey, created);
	}

	/**
	 * The iwrb is the users preferred locale
	 */
	@Override
	public GeneralCase storeGeneralCase(GeneralCase theCase, User sender, Object caseCategoryPK, Object caseTypePK, Object attachmentPK, String regarding, String message, String type, String caseManagerType, boolean isPrivate, IWResourceBundle iwrb, boolean sendMessages, String caseIdentifier, boolean setType, Timestamp created) throws CreateException, RemoteException {
		return storeGeneralCase(theCase, sender, caseCategoryPK, caseTypePK, attachmentPK, regarding, message, type, caseManagerType, isPrivate, iwrb, sendMessages, caseIdentifier, setType, CaseBMPBean.CASE_STATUS_OPEN_KEY, created);
	}


	@Override
	public GeneralCase storeGeneralCase(GeneralCase theCase, User sender, Object caseCategoryPK, Object caseTypePK, Object attachmentPK, String regarding, String message, String type, String caseManagerType, boolean isPrivate, IWResourceBundle iwrb, boolean sendMessages, String caseIdentifier, boolean setType, String caseStatusKey, Timestamp created) throws CreateException, RemoteException {
		Collection<Object> attachmentPKs;
		if(attachmentPK == null){
			attachmentPKs = Collections.emptyList();
		}else{
			attachmentPKs = new ArrayList<Object>(1);
			attachmentPKs.add(attachmentPK);
		}
		return storeGeneralCase(theCase, sender, caseCategoryPK, caseTypePK, attachmentPKs, regarding, message, type, caseManagerType, isPrivate, iwrb, sendMessages, caseIdentifier, setType, caseStatusKey, created);
	}
	public GeneralCase storeGeneralCase(GeneralCase theCase, User sender, Object caseCategoryPK, Object caseTypePK, Collection<Object> attachmentPKs, String regarding, String message, String type, String caseManagerType, boolean isPrivate, IWResourceBundle iwrb, boolean sendMessages, String caseIdentifier, boolean setType, String caseStatusKey, Timestamp created) throws CreateException, RemoteException {
		Locale locale = iwrb.getLocale();
		// TODO use users preferred language!!

		if (caseStatusKey == null) {
			caseStatusKey = getCaseStatusOpenString();
		}

		CaseCategory category = null;
		try {
			category = getCaseCategory(caseCategoryPK);
		} catch (FinderException fe) {
			throw new CreateException("Trying to store a case with case category that has no relation to a group");
		}
		CaseType caseType = null;
		try {
			caseType = getCaseType(caseTypePK);
		} catch (FinderException fe) {
			throw new CreateException("Trying to store a case with case type that does not exist");
		}

		Group handlerGroup = category.getHandlerGroup();

		theCase.setCaseCategory(category);
		theCase.setCaseType(caseType);
		theCase.setOwner(sender);
		theCase.setHandler(handlerGroup);
		theCase.setSubject(regarding);
		theCase.setMessage(message);

		if (setType) {
			theCase.setType(type);
		}
		if (created != null) {
			theCase.setCreated(created);
		}
		theCase.setAsPrivate(isPrivate);
		theCase.setCaseIdentifier(caseIdentifier);

		if (caseManagerType != null) {
			theCase.setCaseManagerType(caseManagerType);
		}

		changeCaseStatus(theCase, caseStatusKey, sender, (Group) null);

		// Adding related entities
		if(theCase.getPrimaryKey() == null){
			theCase.store();
		}
		if(ListUtil.isEmpty(attachmentPKs)){
			theCase.removeAllAttachments();
		}else{
			ICFileHome icFileHome = (ICFileHome) IDOLookup.getHome(ICFile.class);
			Collection<ICFile> attachments;
			try{
				 attachments = icFileHome.getEntityCollectionForPrimaryKeys(attachmentPKs);
			}catch (Exception e) {
				attachments = Collections.emptyList();
			}
			for(ICFile attachment : attachments){
				theCase.addAttachment(attachment);
			}
		}
		if (sendMessages) {
			try {
				sendMessages(theCase, sender, handlerGroup, iwrb, locale, caseManagerType, message);
			} catch (Exception e) {
				throw new IBORuntimeException(e);
			}
		}

		return theCase;
	}

	@Override
	public void sendMessages(GeneralCase theCase, User sender, Group handlerGroup, IWResourceBundle iwrb, Locale locale, String type, String message) throws Exception {
		String prefix = (type != null ? type + CoreConstants.DOT : CoreConstants.EMPTY);

		String subject = iwrb.getLocalizedString(prefix + "case_sent_subject", "A new case sent in");
		String body = null;
		if (sender != null) {
			Name name = new Name(sender.getFirstName(), sender.getMiddleName(), sender.getLastName());

			Object[] arguments = { name.getName(locale), theCase.getCaseCategory().getLocalizedCategoryName(locale), message };
			body = MessageFormat.format(iwrb.getLocalizedString(prefix + "case_sent_body", "A new case has been sent in by {0} in case category {1}. \n\nThe case is as follows:\n{2}"), arguments);

			// sendMessageToUsersAdminAndCompany(sender, subject, body, theCase);
			User moderator = getUserBusiness().getModeratorForUser(sender);
			if (moderator != null) {
				sendMessage(theCase, moderator, sender, subject, body);
			}

		} else {
			Object[] arguments = { iwrb.getLocalizedString("anonymous", "Anonymous"), theCase.getCaseCategory().getLocalizedCategoryName(locale), message };
			body = MessageFormat.format(iwrb.getLocalizedString(prefix + "anonymous_case_sent_body", "An anonymous case has been sent in case category {1}. \n\nThe case is as follows:\n{2}"), arguments);
		}

		Collection<User> handlers = getUserBusiness().getUsersInGroup(handlerGroup);
		Iterator<User> iter = handlers.iterator();
		while (iter.hasNext()) {
			User handler = iter.next();
			sendMessage(theCase, handler, sender, subject, body);
		}

		if (sender != null) {
			Object[] arguments2 = { theCase.getCaseCategory().getLocalizedCategoryName(locale) };
			subject = iwrb.getLocalizedString(prefix + "case_sent_confirmation_subject", "A new case sent in");
			body = MessageFormat.format(iwrb.getLocalizedString(prefix + "case_sent_confirmation_body", "Your case with case category {0} has been received and will be processed."), arguments2);

			sendMessage(theCase, sender, null, subject, body);
		}
	}

	@Override
	public void allocateCase(GeneralCase theCase, Object caseCategoryPK, Object caseTypePK, User user, String message, User performer, IWContext iwc) {
		boolean hasChanges = false;
		try {
			CaseCategory category = caseCategoryPK != null ? getCaseCategory(caseCategoryPK) : null;
			Group handlerGroup = category != null ? category.getHandlerGroup() : null;
			if (category != null && !category.equals(theCase.getCaseCategory())) {
				theCase.setCaseCategory(category);
				theCase.setHandler(handlerGroup);
				hasChanges = true;
			}

			CaseType type = caseTypePK != null ? getCaseType(caseTypePK) : null;
			if (type != null && !theCase.getCaseType().equals(type)) {
				theCase.setCaseType(type);
				hasChanges = true;
			}

			if (hasChanges) {
				theCase.store();
			}
		} catch (FinderException fe) {
			fe.printStackTrace();
		}

		takeCase(theCase, user, iwc, performer, hasChanges);

		Name name = new Name(performer.getFirstName(), performer.getMiddleName(), performer.getLastName());
		Object[] arguments = { name.getName(iwc.getCurrentLocale()), theCase.getCaseCategory().getLocalizedCategoryName(iwc.getApplicationSettings().getDefaultLocale()), theCase.getPrimaryKey().toString(), message };

		String subject = getLocalizedString("case_allocation_subject", "A case has been allocated to you", iwc.getApplicationSettings().getDefaultLocale());
		String body = MessageFormat.format(getLocalizedString("case_allocation_body", "{0} has allocated case nr. {2} in the category {1} to you with the following message:\n{3}", iwc.getApplicationSettings().getDefaultLocale()), arguments);
		sendMessage(theCase, user, performer, subject, body);
	}

	@Override
	public void allocateCase(GeneralCase theCase, User user, String message, User performer, IWContext iwc) {
		takeCase(theCase, user, iwc);

		Name name = new Name(performer.getFirstName(), performer.getMiddleName(), performer.getLastName());
		Object[] arguments = { name.getName(iwc.getCurrentLocale()), theCase.getCaseCategory().getLocalizedCategoryName(iwc.getApplicationSettings().getDefaultLocale()), theCase.getPrimaryKey().toString(), message };

		String subject = getLocalizedString("case_allocation_subject", "A case has been allocated to you", iwc.getApplicationSettings().getDefaultLocale());
		String body = MessageFormat.format(getLocalizedString("case_allocation_body", "{0} has allocated case nr. {2} in the category {1} to you with the following message:\n{3}", iwc.getApplicationSettings().getDefaultLocale()), arguments);
		sendMessage(theCase, user, performer, subject, body);
	}

	@Override
	public void handleCase(Object casePK, Object caseCategoryPK, Object caseTypePK, String status, User performer, String reply, IWContext iwc) throws FinderException {
		GeneralCase theCase = getGeneralCase(casePK);
		CaseCategory category = getCaseCategory(caseCategoryPK);
		CaseType type = getCaseType(caseTypePK);

		handleCase(theCase, category, type, status, performer, reply, iwc);
	}

	@Override
	public void handleCase(GeneralCase theCase, CaseCategory category, CaseType type, String status, User performer, String reply, IWContext iwc) {
		handleCase(theCase, category, category == null ? null : category.getHandlerGroup(), type, status, performer, reply, iwc, true);
	}

	@Override
	public void handleCase(GeneralCase theCase, CaseCategory category, Group handlerGroup, CaseType type, String status, User performer, String reply, IWContext iwc, boolean changeStatus) {
		theCase.setReply(reply);

		boolean isSameCategory = category == null ? false: category.equals(theCase.getCaseCategory());
		if (category != null) {
			theCase.setCaseCategory(category);
		}

		handlerGroup = handlerGroup == null ?
				category == null ? null : category.getHandlerGroup() :
				handlerGroup;
		boolean isInGroup = handlerGroup == null ? false: performer.hasRelationTo(handlerGroup);
		theCase.setHandler(handlerGroup);

		if (!isInGroup) {
			theCase.setHandledBy(iwc.isLoggedOn() ? iwc.getCurrentUser() : null);
			status = getCaseStatusOpen().getStatus();
		} else {
			theCase.setHandledBy(performer);
		}

		if (!isSameCategory) {
			String prefix = (theCase.getType() != null ? theCase.getType() + "." : "");
			User sender = theCase.getOwner();

			String subject = getLocalizedString(prefix + "case_sent_subject", "A new case sent in", iwc.getApplicationSettings().getDefaultLocale());
			String body = null;
			if (sender != null) {
				Name name = new Name(sender.getFirstName(), sender.getMiddleName(), sender.getLastName());

				Object[] arguments = { name.getName(iwc.getCurrentLocale()), theCase.getCaseCategory().getLocalizedCategoryName(iwc.getApplicationSettings().getDefaultLocale()), theCase.getMessage() };
				body = MessageFormat.format(getLocalizedString(prefix + "case_sent_body", "A new case has been sent in by {0} in case category {1}. \n\nThe case is as follows:\n{2}", iwc.getApplicationSettings().getDefaultLocale()), arguments);
			} else {
				Object[] arguments = { getLocalizedString("anonymous", "Anonymous", iwc.getApplicationSettings().getDefaultLocale()), theCase.getCaseCategory().getLocalizedCategoryName(iwc.getApplicationSettings().getDefaultLocale()), theCase.getMessage() };
				body = MessageFormat.format(getLocalizedString(prefix + "anonymous_case_sent_body", "An anonymous case has been sent in case category {1}. \n\nThe case is as follows:\n{2}", iwc.getApplicationSettings().getDefaultLocale()), arguments);
			}

			try {
				Collection<User> handlers = Collections.emptyList();
				if (handlerGroup != null) {
					handlers = getUserBusiness().getUsersInGroup(handlerGroup);
				}
				Iterator<User> iter = handlers.iterator();
				while (iter.hasNext()) {
					User handler = iter.next();
					sendMessage(theCase, handler, sender, subject, body);
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}

		theCase.setCaseType(type);

		if (changeStatus) {
			changeCaseStatus(theCase, status, reply, performer, (Group) null, true);
		}

		User owner = theCase.getOwner();
		if (owner != null && isInGroup) {
			IWResourceBundle iwrb = getIWResourceBundleForUser(owner, iwc);
			Locale locale = iwrb.getLocale();
			String prefix = theCase.getType() != null ? theCase.getType() + "." : "";

			Object[] arguments = { theCase.getCaseCategory().getLocalizedCategoryName(locale), theCase.getCaseType().getName(), performer.getName(), reply, getLocalizedCaseStatusDescription(theCase, getCaseStatus(status), locale) };
			String subject = getLocalizedString(prefix + "case_handled_subject", "Your case has been handled", locale);
			String body = MessageFormat.format(getLocalizedString(prefix + "case_handled_body", "Your case with category {0} and type {1} has been handled by {2}.  The reply was as follows:\n\n{3}", locale), arguments);

			sendMessage(theCase, owner, performer, subject, body);

			User moderator = getUserBusiness().getModeratorForUser(owner);
			if (moderator != null) {
				// sending replay to users admin too.
				Name name = new Name( theCase.getCreator().getFirstName(),  theCase.getCreator().getMiddleName(),  theCase.getCreator().getLastName());
				Locale localeAdmin = iwrb.getLocale();
				String prefixAdmin = theCase.getType() != null ? theCase.getType() + "." : "";
				Object[] arguments2 = { theCase.getCaseCategory().getLocalizedCategoryName(localeAdmin), theCase.getCaseType().getName(), performer.getName(), reply, getLocalizedCaseStatusDescription(theCase, getCaseStatus(status), localeAdmin), name.getName(iwc.getCurrentLocale()) };
				String subjectAdmin = getLocalizedString(prefixAdmin + "case_handled_subject_for_admin", "Users case has been handled", localeAdmin);
				String bodyAdmin = MessageFormat.format(getLocalizedString(prefixAdmin + "case_handled_body_for_admin", "{4} case with category {0} and type {1} has been handled by {2}.  The reply was as follows:\n\n{3}", localeAdmin), arguments2);

				sendMessage(theCase, moderator, owner, subjectAdmin, bodyAdmin);
			}
		}
	}

	@Override
	public void takeCase(Object casePK, User performer, IWContext iwc) throws FinderException {
		GeneralCase theCase = getGeneralCase(casePK);
		takeCase(theCase, performer, iwc);
	}
	
	@Override
	public void takeCase(Object casePK, User performer, IWContext iwc, String messageSubject, String messageBody) throws FinderException,
			RemoteException {
		GeneralCase theCase = getGeneralCase(casePK);
		takeCase(theCase, performer, iwc, messageSubject, messageBody);
	}

	@Override
	public void takeCase(GeneralCase theCase, User performer, IWContext iwc) {
		takeCase(theCase, performer, iwc, null, null);
	}
	
	@Override
	public void takeCase(GeneralCase theCase, User performer, IWContext iwc, String messageSubject, String messageBody) {
		theCase.setHandledBy(performer);

		changeCaseStatus(theCase, getCaseStatusPending().getStatus(), performer, (Group) null);

		User owner = theCase.getOwner();

		if (owner != null) {
			if (StringUtil.isEmpty(messageSubject) && StringUtil.isEmpty(messageBody)) {
				IWResourceBundle iwrb = this.getIWResourceBundleForUser(owner, iwc);
				
				String prefix = theCase.getType() != null ? theCase.getType() + "." : "";
				Object[] arguments = { theCase.getCaseCategory().getLocalizedCategoryName(iwrb.getLocale()), theCase.getCaseType().getName(), performer.getName() };
				String subject = iwrb.getLocalizedString(prefix + "case_taken_subject", "Your case has been taken");
				String body = MessageFormat.format(iwrb.getLocalizedString(prefix + "case_taken_body", "Your case with category {0} and type {1} has been put into process by {2}"), arguments);

				sendMessage(theCase, owner, performer, subject, body);
			} else {
				sendMessage(theCase, owner, performer, messageSubject, messageBody);
			}
		}
	}

	@Override
	public void takeCase(GeneralCase theCase, User user, IWContext iwc, User performer, boolean hasChanges) {

		takeCase(theCase, user, iwc, performer, hasChanges, true);
	}

	@Override
	public void untakeCase(GeneralCase theCase) {

		theCase.setHandledBy(null);
		theCase.store();
	}

	@Override
	public void takeCase(GeneralCase theCase, User user, IWContext iwc, User performer, boolean hasChanges, boolean sendMessages) {
		String comment = getLocalizedString("case_handler_set", "Case handler set", iwc.getApplicationSettings().getDefaultLocale()) + ": " + new Name(user.getFirstName(), user.getMiddleName(), user.getLastName()).getName(iwc.getApplicationSettings().getDefaultLocale());

		theCase.setHandledBy(user);
		changeCaseStatus(theCase, getCaseStatusPending().getStatus(), comment, performer, (Group) null);

		if (sendMessages) {

			User owner = theCase.getOwner();

			IWResourceBundle iwrb = this.getIWResourceBundleForUser(owner, iwc);

			if (owner != null) {
				String prefix = theCase.getType() != null ? theCase.getType() + "." : "";

				if (hasChanges) {
					Name name = new Name(performer.getFirstName(), performer.getMiddleName(), performer.getLastName());
					Object[] arguments2 = { name.getName(iwc.getCurrentLocale()), theCase.getCaseCategory().getLocalizedCategoryName(iwc.getApplicationSettings().getDefaultLocale()), theCase.getPrimaryKey().toString() };

					String subject = getLocalizedString(prefix + "case_changed_subject", "Your case has been changed", iwc.getApplicationSettings().getDefaultLocale());
					String body = MessageFormat.format(getLocalizedString(prefix + "case_changed_body", "{0} has changed case nr. {2} to the category {1}", iwc.getApplicationSettings().getDefaultLocale()), arguments2);
					sendMessage(theCase, owner, performer, subject, body);
				}

				Object[] arguments = { theCase.getCaseCategory().getLocalizedCategoryName(iwrb.getLocale()), theCase.getCaseType().getName(), user.getName() };
				String subject = iwrb.getLocalizedString(prefix + "case_taken_subject", "Your case has been taken");
				String body = MessageFormat.format(iwrb.getLocalizedString(prefix + "case_taken_body", "Your case with category {0} and type {1} has been put into process by {2}"), arguments);

				sendMessage(theCase, owner, user, subject, body);
			}
		}
	}

	@Override
	public void reactivateCase(Object casePK, User performer, IWContext iwc) throws FinderException {
		GeneralCase theCase = getGeneralCase(casePK);
		User owner = theCase.getOwner();
		if (owner == null || (owner != null && !owner.equals(performer))) {
			theCase.setHandledBy(performer);
		}

		changeCaseStatus(theCase, getCaseStatusPending().getStatus(), performer, (Group) null);

		if (owner != null) {
			IWResourceBundle iwrb = this.getIWResourceBundleForUser(owner, iwc);

			String prefix = theCase.getType() != null ? theCase.getType() + "." : "";
			Object[] arguments = { theCase.getCaseCategory().getLocalizedCategoryName(iwrb.getLocale()), theCase.getCaseType().getName(), performer.getName() };
			String subject = iwrb.getLocalizedString(prefix + "case_reactivated_subject", "Your case has been reactivated");
			String body = MessageFormat.format(iwrb.getLocalizedString(prefix + "case_reactivated_body", "Your case with category {0} and type {1} has been reactivated by {2}"), arguments);

			sendMessage(theCase, owner, performer, subject, body);

			// sending replay to users admin too.
			Name name = new Name(performer.getFirstName(), performer.getMiddleName(), performer.getLastName());
			Object[] arguments2 = { theCase.getCaseCategory().getLocalizedCategoryName(iwrb.getLocale()), theCase.getCaseType().getName(), performer.getName(), name.getName(iwc.getCurrentLocale()) };
			subject = iwrb.getLocalizedString(prefix + "case_reactivated_subject_for_admin", "Users case has been reactivated");
			body = MessageFormat.format(iwrb.getLocalizedString(prefix + "case_reactivated_body_for_admin", "{3} case  with category {0} and type {1} has been reactivated by {2}"), arguments2);

			User moderator = getUserBusiness().getModeratorForUser(owner);
			if (moderator != null) {
				sendMessage(theCase, moderator, owner, subject, body);
			}
		}
	}

	@Override
	public void reviewCase(GeneralCase theCase, User performer, IWContext iwc) throws FinderException {
		CaseCategory category = theCase.getCaseCategory();
		Group handlerGroup = category.getHandlerGroup();

		changeCaseStatus(theCase, getCaseStatusReview().getStatus(), performer, (Group) null);

		User owner = theCase.getOwner();
		try {
			String prefix = theCase.getType() != null ? theCase.getType() + "." : "";
			IWResourceBundle iwrb = this.getIWResourceBundleForUser(owner, iwc);

			Name name = new Name(owner.getFirstName(), owner.getMiddleName(), owner.getLastName());
			Object[] arguments = { name.getName(iwrb.getLocale()), theCase.getCaseCategory().getLocalizedCategoryName(iwrb.getLocale()), theCase.getMessage() };
			String subject = iwrb.getLocalizedString(prefix + "case_review_handler_subject", "A case sent for review");
			String body = MessageFormat.format(iwrb.getLocalizedString(prefix + "case_review_handler_body", "A case has been sent in for review by {0} in case category {1}. \n\nThe case is as follows:\n{2}"), arguments);

			Collection handlers = getUserBusiness().getUsersInGroup(handlerGroup);
			Iterator iter = handlers.iterator();
			while (iter.hasNext()) {
				User handler = (User) iter.next();
				sendMessage(theCase, handler, owner, subject, body);
			}

			if (owner != null) {
				Object[] args = { theCase.getCaseCategory().getLocalizedCategoryName(iwrb.getLocale()), theCase.getCaseType().getName(), performer.getName() };
				subject = iwrb.getLocalizedString(prefix + "case_review_subject", "Your case has been sent for review");
				body = MessageFormat.format(iwrb.getLocalizedString(prefix + "case_review_body", "Your case with category {0} and type {1} has been sent for review"), args);

				sendMessage(theCase, owner, performer, subject, body);
			}
		} catch (RemoteException e) {
			throw new IBORuntimeException(e);
		}
	}

	@Override
	public void reviewCase(Object casePK, User performer, IWContext iwc) throws FinderException {
		GeneralCase theCase = getGeneralCase(casePK);
		reviewCase(theCase, performer, iwc);
	}

	@Override
	public CaseCategory storeCaseCategory(Object caseCategoryPK, Object parentCaseCategoryPK, String name, String description, Object groupPK, int localeId, int order) throws FinderException, CreateException {
		CaseCategory category = null;
		boolean isDefaultLocale = ICLocaleBusiness.getLocaleId(this.getDefaultLocale()) == localeId;

		if (caseCategoryPK != null) {
			category = getCaseCategory(caseCategoryPK);
		} else {
			category = getCaseCategoryHome().create();
		}

		CaseCategory parentCategory = null;

		if (parentCaseCategoryPK != null) {
			parentCategory = getCaseCategory(parentCaseCategoryPK);
		}

		if (category.getName() == null || isDefaultLocale) {
			category.setName(name);
		}

		if (category.getDescription() == null || isDefaultLocale) {
			category.setDescription(description);
		}

		// watch out for endless nesting
		if (parentCaseCategoryPK != null && !parentCaseCategoryPK.equals(caseCategoryPK)) {
			category.setParent(parentCategory);
		}

		if (parentCaseCategoryPK == null) {
			category.setParent(null);
		}

		category.setHandlerGroup(groupPK);
		if (order != -1) {
			category.setOrder(order);
		}

		category.store();

		// localization

		LocalizedText locText = category.getLocalizedText(localeId);
		if (locText == null) {
			locText = ((LocalizedTextHome) com.idega.data.IDOLookup.getHomeLegacy(LocalizedText.class)).createLegacy();
		}

		locText.setHeadline(name);
		locText.setBody(description);
		locText.setLocaleId(localeId);
		locText.store();

		try {
			category.addLocalization(locText);
		} catch (SQLException e) {
			// error usually means the text is already added
			// e.printStackTrace();
			// throw new CreateException("Failed to add localization, the message was : "+e.getMessage());
		}

		return category;

	}

	@Override
	public CaseType storeCaseType(Object caseTypePK, String name, String description, int order) throws FinderException, CreateException {
		CaseType type;
		if (caseTypePK != null) {
			type = getCaseType(caseTypePK);
		} else {
			type = getCaseTypeHome().create();
		}

		type.setName(name);
		type.setDescription(description);
		if (order != -1) {
			type.setOrder(order);
		}
		type.store();

		return type;
	}

	private void sendMessage(GeneralCase theCase, User receiver, User sender, String subject, String body) {
		try {
			boolean sendLetter = getIWMainApplication().getSettings().getBoolean("case.send_letter_about_new_case", false);
			getMessageBusiness().createUserMessage(theCase, receiver, sender, subject, body, sendLetter);
		} catch (RemoteException re) {
			throw new IBORuntimeException(re);
		}
	}

	@Override
	public boolean canDeleteCase(Case theCase) {
		return false;
	}

	@Override
	public boolean useSubCategories() {
		return getIWApplicationContext().getApplicationSettings().getBoolean(CasesConstants.PROPERTY_USE_SUB_CATEGORIES, false);
	}

	@Override
	public boolean useTypes() {
		return getIWApplicationContext().getApplicationSettings().getBoolean(CasesConstants.PROPERTY_USE_TYPES, true);
	}

	@Override
	public boolean allowPrivateCases() {
		return getIWApplicationContext().getApplicationSettings().getBoolean(CasesConstants.PROPERTY_ALLOW_PRIVATE_CASES, false);
	}

	@Override
	public boolean allowAnonymousCases() {
		return getIWApplicationContext().getApplicationSettings().getBoolean(CasesConstants.PROPERTY_ALLOW_ANONYMOUS_CASES, false);
	}

	@Override
	public boolean allowAttachments() {
		return getIWApplicationContext().getApplicationSettings().getBoolean(CasesConstants.PROPERTY_ALLOW_ATTACHMENTS, false);
	}

	@Override
	public Object[] createDefaultCaseTypesForDefaultGroupIfNotExist(String caseCategoryName, String caseCategoryDescription, String caseTypeName, String caseTypeDescription, String caseCategoryHandlersGroupName, String caseCategoryHandlersGroupDescription) throws FinderException, CreateException, RemoteException {

		Collection<CaseCategory> caseCategories = getCaseCategoriesByName(caseCategoryName);
		Collection<CaseType> caseTypes = getCaseTypesByName(caseTypeName);

		CaseCategory caseCategory;
		CaseType caseType;

		if (caseCategories == null || caseCategories.isEmpty()) {

			GroupBusiness groupBusiness = getGroupBusiness();

			Collection<Group> caseHandlersGroups = groupBusiness.getGroupsByGroupName(caseCategoryHandlersGroupName);
			Group caseHandlersGroup;

			if (caseHandlersGroups == null || caseHandlersGroups.isEmpty()) {

				caseHandlersGroup = groupBusiness.createGroup(caseCategoryHandlersGroupName, caseCategoryHandlersGroupDescription);
			} else {
				caseHandlersGroup = caseHandlersGroups.iterator().next();
			}

			int localeId = ICLocaleBusiness.getLocaleId(new Locale("en"));
			caseCategory = storeCaseCategory(null, null, caseCategoryName, caseCategoryDescription, caseHandlersGroup, localeId, -1);
		} else {
			caseCategory = caseCategories.iterator().next();
		}

		if (caseTypes == null || caseTypes.isEmpty()) {

			caseType = storeCaseType(null, caseTypeName, caseTypeDescription, -1);

		} else {
			caseType = caseTypes.iterator().next();
		}

		return new Object[] { caseCategory, caseType };
	}

	@Override
	protected GroupBusiness getGroupBusiness() {

		try {
			FacesContext fctx = FacesContext.getCurrentInstance();
			IWApplicationContext iwac;

			if (fctx == null) {
				iwac = IWMainApplication.getDefaultIWApplicationContext();
			} else {
				iwac = IWMainApplication.getIWMainApplication(fctx).getIWApplicationContext();
			}

			return IBOLookup.getServiceInstance(iwac, GroupBusiness.class);
		} catch (IBOLookupException ile) {
			throw new IBORuntimeException(ile);
		}
	}

	@Override
	public CaseManagersProvider getCaseHandlersProvider() {
		return WFUtil.getBeanInstance(CaseManagersProvider.beanIdentifier);
	}

	@Override
	public Collection<Integer> getCasesIDsByCriteria(String caseNumber, String description, String name, String personalId, String[] statuses, IWTimestamp dateFrom,
			IWTimestamp dateTo, User owner, Collection<Group> groups, boolean simpleCases, boolean notGeneralCases) {
		return getCasesIDsByCriteria(caseNumber, description, name, personalId, statuses, dateFrom, dateTo, owner, groups, simpleCases, notGeneralCases, null, null);
	}

	@Override
	public Collection<Integer> getCasesIDsByCriteria(String caseNumber, String description, String name, String personalId, String[] statuses, IWTimestamp dateFrom,
			IWTimestamp dateTo, User owner, Collection<Group> groups, boolean simpleCases, boolean notGeneralCases, Boolean withHandler, List<Integer> exceptOwnersIds) {
		return getCasesIDsByCriteria(caseNumber, description, name, personalId, statuses, dateFrom, dateTo, owner, groups, simpleCases, notGeneralCases, withHandler, exceptOwnersIds, null);
	}

	@Override
	public Collection<Integer> getCasesIDsByCriteria(
			String caseNumber,
			String description,
			String name,
			String personalId,
			String[] statuses,
			IWTimestamp dateFrom,
			IWTimestamp dateTo,
			User owner,
			Collection<Group> groups,
			boolean simpleCases,
			boolean notGeneralCases,
			Boolean withHandler,
			List<Integer> exceptOwnersIds,
			String caseCode
	) {
		Collection<User> owners = null;
		if (!StringUtil.isEmpty(personalId)) {
			Collection<User> caseOwners = null;
			try {
				caseOwners = getUserHome().findAllByPersonalID(personalId);
			} catch (FinderException e) {
				e.printStackTrace();
			}
			if (ListUtil.isEmpty(caseOwners)) {
				return null;
			}

			owners = new ArrayList<User>(caseOwners);
		}
		if (!StringUtil.isEmpty(name)) {
			Collection<User> usersByName = getUserBusiness().getUsersByName(name);
			if (ListUtil.isEmpty(usersByName)) {
				return null;
			}

			if (ListUtil.isEmpty(owners)) {
				owners = new ArrayList<User>(usersByName);
			} else {
				for (User userByName: usersByName) {
					if (!owners.contains(userByName)) {
						owners.add(userByName);
					}
				}
			}
		}

		Collection<String> ownersIds = null;
		if (personalId != null || name != null) {
			if (ListUtil.isEmpty(owners)) {
				return null;
			}
			ownersIds = new ArrayList<String>(owners.size());
			for (User caseOwner : owners) {
				ownersIds.add(caseOwner.getId());
			}
		}

		statuses = ArrayUtil.isEmpty(statuses) ? null : statuses;
		if (!ArrayUtil.isEmpty(statuses)) {
			Set<String> validStatuses = new HashSet<>();
			for (String status: statuses) {
				if (!StringUtil.isEmpty(status) && !StringUtil.isEmpty(status.trim())) {
					validStatuses.add(status.trim());
				}
			}
			statuses = ArrayUtil.convertListToArray(validStatuses);
		}

		if (notGeneralCases) {
			try {
				return getCaseHome().findIDsByCriteria(caseNumber, description, ownersIds, statuses, dateFrom, dateTo, owner, groups, simpleCases, withHandler, exceptOwnersIds, caseCode);
			} catch (Exception e) {
				log(Level.SEVERE, "Error getting cases by criteria: case number: " + caseNumber + ", description: " + description + ", owners IDs: " +
						ownersIds + ", statuses: " + statuses + ", date from: " + dateFrom + ", date to: " + dateTo + ", owner: " + owner + ", groups: " +
						groups + ", simple cases: " + simpleCases, e);
			}
		} else {
			try {
				return getGeneralCaseHome().getCasesIDsByCriteria(caseNumber, description, ownersIds, statuses, dateFrom, dateTo, owner, groups, simpleCases, withHandler, exceptOwnersIds, caseCode);
			} catch (Exception e) {
				log(Level.SEVERE, "Error getting cases by criteria: case number: " + caseNumber + ", description: " + description + ", owners IDs: " +
						ownersIds + ", statuses: " + statuses + ", date from: " + dateFrom + ", date to: " + dateTo + ", owner: " + owner + ", groups: " +
						groups + ", simple cases: " + simpleCases, e);
			}
		}

		return null;
	}

	@Override
	public Collection<Case> getCasesByCriteria(String caseNumber, String description, String name, String personalId, String[] statuses, IWTimestamp dateFrom,
			IWTimestamp dateTo, User owner, Collection<Group> groups, boolean simpleCases, boolean notGeneralCases) {

		return getCasesByCriteria(caseNumber, description, name, personalId, statuses, dateFrom, dateTo, owner, groups, simpleCases, notGeneralCases, null, null);
	}

	@Override
	public Collection<Case> getCasesByCriteria(String caseNumber, String description, String name, String personalId, String[] statuses, IWTimestamp dateFrom,
			IWTimestamp dateTo, User owner, Collection<Group> groups, boolean simpleCases, boolean notGeneralCases, Boolean withHandler, List<Integer> exceptOwnersIds) {
		return getCasesByCriteria(caseNumber, description, name, personalId, statuses, dateFrom, dateTo, owner, groups, simpleCases, notGeneralCases, withHandler, exceptOwnersIds, null);
	}

	@Override
	public Collection<Case> getCasesByCriteria(
			String caseNumber,
			String description,
			String name,
			String personalId,
			String[] statuses,
			IWTimestamp dateFrom,
			IWTimestamp dateTo,
			User owner,
			Collection<Group> groups,
			boolean simpleCases,
			boolean notGeneralCases,
			Boolean withHandler,
			List<Integer> exceptOwnersIds,
			String caseCode
	) {
		Collection<Integer> ids = getCasesIDsByCriteria(caseNumber, description, name, personalId, statuses, dateFrom, dateTo, owner, groups, simpleCases,
				notGeneralCases, withHandler, exceptOwnersIds, caseCode);
		try {
			return getCaseHome().findAllByIds(ids);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Collection<Case> getCasesByIds(Collection<Integer> ids) {
		if (ids == null || ids.isEmpty()) {
			return null;
		}

		String errorMessage = "Error getting cases by ids: " + ids;
		boolean reTry = false;
		try {
			return getGeneralCaseHome().getCasesByIds(ids);
		} catch (FinderException e) {
			reTry = true;
		} catch (Exception e) {
			log(Level.SEVERE, errorMessage, e);
		}
		if (reTry) {
			try {
				return getCaseHome().getCasesByIds(ids);
			} catch (Exception e) {
				log(Level.SEVERE, errorMessage, e);
			}
		}

		return null;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Collection<GeneralCase> getOpenCases(User user, IWMainApplication iwma, IWUserContext iwuc, String[] caseHandlers) {

		try {
			boolean isCaseSuperAdmin = iwma.getAccessController().hasRole(CasesConstants.ROLE_CASES_SUPER_ADMIN, iwuc);

			Collection<GeneralCase> openCases;

			if(caseHandlers == null) {

				openCases = getOpenCases(!isCaseSuperAdmin ? getUserBusiness().getUserGroupsDirectlyRelated(user) : null);
			} else {

				openCases = getOpenCases(!isCaseSuperAdmin ? getUserBusiness().getUserGroupsDirectlyRelated(user) : null, caseHandlers);
			}
			return openCases;

		} catch (RemoteException e) {
			throw new IBORuntimeException(e);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public Collection<GeneralCase> getClosedCases(User user, IWMainApplication iwma, IWUserContext iwuc, String[] caseHandlers) {

		try {
			boolean isCaseSuperAdmin = iwma.getAccessController().hasRole(CasesConstants.ROLE_CASES_SUPER_ADMIN, iwuc);

			Collection<GeneralCase> closedCases;

			if(caseHandlers == null) {

				closedCases = getClosedCases(!isCaseSuperAdmin ? getUserBusiness().getUserGroupsDirectlyRelated(user) : null);
			} else {

				closedCases = getClosedCases(!isCaseSuperAdmin ? getUserBusiness().getUserGroupsDirectlyRelated(user) : null, caseHandlers);
			}
			return closedCases;

		} catch (RemoteException e) {
			throw new IBORuntimeException(e);
		}
	}

	@Override
	public Collection<Case> getFilteredProcesslessCases(Collection<Integer> ids, boolean notGeneralCases) {
		if (ListUtil.isEmpty(ids)) {
			return null;
		}

		Collection<Case> cases = null;
		if (notGeneralCases) {
			try {
				cases = getCaseHome().findAllByIds(ids);
			} catch (FinderException e) {
				e.printStackTrace();
			}
		}
		else {
			Collection<GeneralCase> generalCases = null;
			try {
				generalCases = getGeneralCaseHome().findAllByIds(ids);
			} catch (FinderException e) {
				e.printStackTrace();
			}
			if (ListUtil.isEmpty(generalCases)) {
				return null;
			}

			cases = new ArrayList<Case>(generalCases);
		}
		if (ListUtil.isEmpty(cases)) {
			return null;
		}

		List<Case> filteredCases = new ArrayList<Case>();
		for (Case casse: cases) {
			if (StringUtil.isEmpty(casse.getCaseManagerType())) {
				filteredCases.add(casse);
			}
		}

		return filteredCases;
	}

	@Override
	public List<Integer> getFilteredProcesslessCasesIds(Collection<Integer> ids, boolean notGeneralCases) {
		Collection<Case> filteredCases = getFilteredProcesslessCases(ids, notGeneralCases);
		if (ListUtil.isEmpty(filteredCases)) {
			return null;
		}

		Integer id = null;
		List<Integer> filteredIds = new ArrayList<Integer>();
		for (Case casse: filteredCases) {
			id = null;
			try {
				id = Integer.valueOf(casse.getId());
			} catch(NumberFormatException e) {
				e.printStackTrace();
			}

			if (id != null && !filteredIds.contains(id)) {
				filteredIds.add(id);
			}
		}

		return filteredIds;
	}

	@Override
	public Collection<CasesStatistics.Result> getCasesStatisticsResults(IWContext iwc, CasesStatistics.Handler handler) {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;

		Collection<Result> results = new ArrayList<Result>();
		try {
			//CoreUtil.clearAllCaches();

			conn = ConnectionBroker.getConnection();
			stmt = conn.createStatement();

			rs = stmt.executeQuery(handler.getSQL());

			results.addAll(handler.getResults(iwc, rs));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (conn != null) {
				ConnectionBroker.freeConnection(conn);
			}
		}

		return results;
	}

}