package is.idega.idegaweb.egov.cases.business;


import is.idega.idegaweb.egov.cases.data.CaseCategory;
import is.idega.idegaweb.egov.cases.data.CaseType;
import is.idega.idegaweb.egov.cases.data.GeneralCase;
import is.idega.idegaweb.egov.cases.data.GeneralCaseHome;

import java.rmi.RemoteException;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.ejb.CreateException;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;

import com.idega.block.process.business.CaseBusiness;
import com.idega.block.process.business.CaseManagersProvider;
import com.idega.block.process.data.Case;
import com.idega.block.process.data.CaseLog;
import com.idega.block.process.data.CaseStatus;
import com.idega.business.IBOService;
import com.idega.core.file.data.ICFile;
import com.idega.idegaweb.IWMainApplication;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.idegaweb.IWUserContext;
import com.idega.presentation.IWContext;
import com.idega.user.data.Group;
import com.idega.user.data.User;
import com.idega.util.IWTimestamp;

public interface CasesBusiness extends IBOService, CaseBusiness {

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#getCasesByCriteria
	 */
	public Collection<Case> getCasesByCriteria(CaseCategory parentCategory, CaseCategory category, CaseType type, CaseStatus status, Date fromDate, Date toDate, Boolean anonymous) throws RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#sendReminder
	 */
	public void sendReminder(GeneralCase theCase, User receiver, User sender, String message, IWContext iwc) throws RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#getAllSubCategories
	 */
	public Map<String, String> getAllSubCategories(String categoryPK, String country) throws RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#getUsers
	 */
	public Map<String, String> getUsers(String categoryPK) throws RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#getUser
	 */
	public UserDWR getUser(String personalID) throws RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#reactivateCase
	 */
	public void reactivateCase(GeneralCase theCase, User performer, IWContext iwc) throws FinderException, RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#getGeneralCaseHome
	 */
	public GeneralCaseHome getGeneralCaseHome() throws RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#getAttachment
	 */
	public ICFile getAttachment(Object attachmentPK) throws RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#getLocalizedCaseDescription
	 */
	@Override
	public String getLocalizedCaseDescription(Case theCase, Locale locale) throws RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#getLocalizedCaseStatusDescription
	 */
	public String getLocalizedCaseStatusDescription(CaseStatus status, Locale locale) throws RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#getLocalizedCaseStatusDescription
	 */
	@Override
	public String getLocalizedCaseStatusDescription(Case theCase, CaseStatus status, Locale locale) throws RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#getGeneralCase
	 */
	public GeneralCase getGeneralCase(Object casePK) throws FinderException, RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#getOpenCases
	 */
	public Collection getOpenCases(Collection groups) throws RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#getOpenCases
	 */
	public Collection getOpenCases(Collection groups, String[] caseHandlers) throws RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#getMyCases
	 */
	public Collection getMyCases(User handler) throws RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#getMyCases
	 */
	public Collection getMyCases(User handler, String[] caseHandlers) throws RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#getClosedCases
	 */
	public Collection getClosedCases(Collection groups) throws RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#getClosedCases
	 */
	public Collection getClosedCases(Collection groups, String[] caseHandlers) throws RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#getCasesByUsers
	 */
	public Collection getCasesByUsers(Collection users) throws RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#getCasesByMessage
	 */
	public Collection getCasesByMessage(String message) throws RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#getCasesByCriteria
	 */
	public Collection getCasesByCriteria(CaseCategory parentCategory, CaseCategory category, CaseType type, CaseStatus status, Boolean anonymous) throws RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#getCasesByCriteria
	 */
	public Collection getCasesByCriteria(CaseCategory parentCategory, CaseCategory category, CaseType type, CaseStatus status, Boolean anonymous, String caseHandler) throws RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#getCaseCategory
	 */
	public CaseCategory getCaseCategory(Object caseCategoryPK) throws FinderException, RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#getAllCaseCategories
	 */
	public Collection getAllCaseCategories() throws RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#getCaseCategoriesByName
	 */
	public Collection<CaseCategory> getCaseCategoriesByName(String name) throws RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#getCaseTypesByName
	 */
	public Collection<CaseType> getCaseTypesByName(String name) throws RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#getCaseCategories
	 */
	public Collection<CaseCategory> getCaseCategories() throws RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#getSubCategories
	 */
	public Collection<CaseCategory> getSubCategories(CaseCategory category) throws RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#getCaseLogs
	 */
	public Collection<CaseLog> getCaseLogs(GeneralCase theCase) throws RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#removeCaseCategory
	 */
	public void removeCaseCategory(Object caseCategoryPK) throws FinderException, RemoveException, RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#getCaseType
	 */
	public CaseType getCaseType(Object caseTypePK) throws FinderException, RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#getCaseTypes
	 */
	public Collection<CaseType> getCaseTypes() throws RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#getFirstAvailableCaseType
	 */
	public CaseType getFirstAvailableCaseType() throws RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#removeCaseType
	 */
	public void removeCaseType(Object caseTypePK) throws FinderException, RemoveException, RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#storeGeneralCase
	 */
	public GeneralCase storeGeneralCase(User sender, Object caseCategoryPK, Object caseTypePK, Object attachmentPK, String regarding, String message, String type, boolean isPrivate, IWResourceBundle iwrb, boolean setType, Timestamp created) throws CreateException, RemoteException, RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#storeGeneralCase
	 */
	public GeneralCase storeGeneralCase(User sender, Object caseCategoryPK, Object caseTypePK, Object attachmentPK, String regarding, String message, String type, String caseManagerType, boolean isPrivate, IWResourceBundle iwrb, boolean setType, Timestamp created) throws CreateException, RemoteException, RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#storeGeneralCase
	 */
	public GeneralCase storeGeneralCase(User sender, Object caseCategoryPK, Object caseTypePK, Object attachmentPK, String regarding, String message, String type, String caseManagerType, boolean isPrivate, IWResourceBundle iwrb, boolean sendMessages, String caseIdentifier, boolean setType, String caseStatusKey, Timestamp created) throws CreateException, RemoteException, RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#storeGeneralCase
	 */
	public GeneralCase storeGeneralCase(GeneralCase theCase, User sender, Object caseCategoryPK, Object caseTypePK, Object attachmentPK, String regarding, String message, String type, String caseManagerType, boolean isPrivate, IWResourceBundle iwrb, boolean sendMessages, String caseIdentifier, boolean setType, Timestamp created) throws CreateException, RemoteException, RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#storeGeneralCase
	 */
	public GeneralCase storeGeneralCase(GeneralCase theCase, User sender, Object caseCategoryPK, Object caseTypePK, Object attachmentPK, String regarding, String message, String type, String caseManagerType, boolean isPrivate, IWResourceBundle iwrb, boolean sendMessages, String caseIdentifier, boolean setType, String caseStatusKey, Timestamp created) throws CreateException, RemoteException, RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#allocateCase
	 */
	public void allocateCase(GeneralCase theCase, Object caseCategoryPK, Object caseTypePK, User user, String message, User performer, IWContext iwc) throws RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#allocateCase
	 */
	public void allocateCase(GeneralCase theCase, User user, String message, User performer, IWContext iwc) throws RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#handleCase
	 */
	public void handleCase(Object casePK, Object caseCategoryPK, Object caseTypePK, String status, User performer, String reply, IWContext iwc) throws FinderException, RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#handleCase
	 */
	public void handleCase(GeneralCase theCase, CaseCategory category, CaseType type, String status, User performer, String reply, IWContext iwc) throws RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#takeCase
	 */
	public void takeCase(Object casePK, User performer, IWContext iwc) throws FinderException, RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#takeCase
	 */
	public void takeCase(GeneralCase theCase, User performer, IWContext iwc) throws RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#takeCase
	 */
	public void takeCase(GeneralCase theCase, User user, IWContext iwc, User performer, boolean hasChanges) throws RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#untakeCase
	 */
	public void untakeCase(GeneralCase theCase) throws RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#takeCase
	 */
	public void takeCase(GeneralCase theCase, User user, IWContext iwc, User performer, boolean hasChanges, boolean sendMessages) throws RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#reactivateCase
	 */
	public void reactivateCase(Object casePK, User performer, IWContext iwc) throws FinderException, RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#reviewCase
	 */
	public void reviewCase(GeneralCase theCase, User performer, IWContext iwc) throws FinderException, RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#reviewCase
	 */
	public void reviewCase(Object casePK, User performer, IWContext iwc) throws FinderException, RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#storeCaseCategory
	 */
	public CaseCategory storeCaseCategory(Object caseCategoryPK, Object parentCaseCategoryPK, String name, String description, Object groupPK, int localeId, int order) throws FinderException, CreateException, RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#storeCaseType
	 */
	public CaseType storeCaseType(Object caseTypePK, String name, String description, int order) throws FinderException, CreateException, RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#canDeleteCase
	 */
	@Override
	public boolean canDeleteCase(Case theCase) throws RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#useSubCategories
	 */
	public boolean useSubCategories() throws RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#useTypes
	 */
	public boolean useTypes() throws RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#allowPrivateCases
	 */
	public boolean allowPrivateCases() throws RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#allowAnonymousCases
	 */
	public boolean allowAnonymousCases() throws RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#allowAttachments
	 */
	public boolean allowAttachments() throws RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#createDefaultCaseTypesForDefaultGroupIfNotExist
	 */
	public Object[] createDefaultCaseTypesForDefaultGroupIfNotExist(String caseCategoryName, String caseCategoryDescription, String caseTypeName, String caseTypeDescription, String caseCategoryHandlersGroupName, String caseCategoryHandlersGroupDescription) throws FinderException, CreateException, RemoteException, RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#getCaseHandlersProvider
	 */
	public CaseManagersProvider getCaseHandlersProvider() throws RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#getCasesByCriteria
	 */
	public Collection<Case> getCasesByCriteria(String caseNumber, String description, String name, String personalId, String[] statuses, IWTimestamp dateFrom, IWTimestamp dateTo, User owner, Collection<Group> groups, boolean simpleCases, boolean notGeneralCases) throws RemoteException;

	public Collection<Integer> getCasesIDsByCriteria(String caseNumber, String description, String name, String personalId, String[] statuses, IWTimestamp dateFrom,
			IWTimestamp dateTo, User owner, Collection<Group> groups, boolean simpleCases, boolean notGeneralCases);

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#getCasesByIds
	 */
	@Override
	public Collection<Case> getCasesByIds(Collection<Integer> ids);

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#getOpenCases
	 */
	public Collection<GeneralCase> getOpenCases(User user, IWMainApplication iwma, IWUserContext iwuc, String[] caseHandlers) throws RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#getClosedCases
	 */
	public Collection<GeneralCase> getClosedCases(User user, IWMainApplication iwma, IWUserContext iwuc, String[] caseHandlers) throws RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#getFilteredProcesslessCases
	 */
	public Collection<Case> getFilteredProcesslessCases(Collection<Integer> ids, boolean notGeneralCases) throws RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#getFilteredProcesslessCasesIds
	 */
	public List<Integer> getFilteredProcesslessCasesIds(Collection<Integer> ids, boolean notGeneralCases) throws RemoteException;

}