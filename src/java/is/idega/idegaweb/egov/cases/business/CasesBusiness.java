/*
 * $Id$
 * Created on Nov 7, 2005
 *
 * Copyright (C) 2005 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf.
 * Use is subject to license terms.
 */
package is.idega.idegaweb.egov.cases.business;

import is.idega.idegaweb.egov.cases.data.CaseCategory;
import is.idega.idegaweb.egov.cases.data.CaseType;
import is.idega.idegaweb.egov.cases.data.GeneralCase;
import java.util.Collection;
import java.util.Locale;
import javax.ejb.CreateException;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;
import com.idega.block.process.business.CaseBusiness;
import com.idega.block.process.data.Case;
import com.idega.business.IBOService;
import com.idega.user.data.User;


/**
 * <p>
 * TODO laddi Describe Type CasesBusiness
 * </p>
 *  Last modified: $Date$ by $Author$
 * 
 * @author <a href="mailto:laddi@idega.com">laddi</a>
 * @version $Revision$
 */
public interface CasesBusiness extends IBOService, CaseBusiness {

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#getLocalizedCaseDescription
	 */
	public String getLocalizedCaseDescription(Case theCase, Locale locale) throws java.rmi.RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#getGeneralCase
	 */
	public GeneralCase getGeneralCase(Object casePK) throws FinderException, java.rmi.RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#getOpenCases
	 */
	public Collection getOpenCases(Collection groups) throws java.rmi.RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#getMyCases
	 */
	public Collection getMyCases(User handler) throws java.rmi.RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#getClosedCases
	 */
	public Collection getClosedCases(Collection groups) throws java.rmi.RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#getCasesByUsers
	 */
	public Collection getCasesByUsers(Collection users) throws java.rmi.RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#getCaseCategory
	 */
	public CaseCategory getCaseCategory(Object caseCategoryPK) throws FinderException, java.rmi.RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#getCaseCategories
	 */
	public Collection getCaseCategories() throws java.rmi.RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#removeCaseCategory
	 */
	public void removeCaseCategory(Object caseCategoryPK) throws FinderException, RemoveException,
			java.rmi.RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#getCaseType
	 */
	public CaseType getCaseType(Object caseTypePK) throws FinderException, java.rmi.RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#getCaseTypes
	 */
	public Collection getCaseTypes() throws java.rmi.RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#removeCaseType
	 */
	public void removeCaseType(Object caseTypePK) throws FinderException, RemoveException, java.rmi.RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#storeGeneralCase
	 */
	public void storeGeneralCase(User sender, Object caseCategoryPK, Object caseTypePK, String message)
			throws CreateException, java.rmi.RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#handleCase
	 */
	public void handleCase(Object casePK, Object caseCategoryPK, Object caseTypePK, String status, User performer,
			String reply, Locale locale) throws FinderException, java.rmi.RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#takeCase
	 */
	public void takeCase(Object casePK, User performer) throws FinderException, java.rmi.RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#reactivateCase
	 */
	public void reactivateCase(Object casePK, User performer) throws FinderException, java.rmi.RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#storeCaseCategory
	 */
	public void storeCaseCategory(Object caseCategoryPK, String name, String description, Object groupPK, int order)
			throws FinderException, CreateException, java.rmi.RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#storeCaseType
	 */
	public void storeCaseType(Object caseTypePK, String name, String description, int order) throws FinderException,
			CreateException, java.rmi.RemoteException;

	/**
	 * @see is.idega.idegaweb.egov.cases.business.CasesBusinessBean#canDeleteCase
	 */
	public boolean canDeleteCase(Case theCase) throws java.rmi.RemoteException;
}
