/*
 * $Id$
 * Created on Nov 7, 2005
 *
 * Copyright (C) 2005 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf.
 * Use is subject to license terms.
 */
package is.idega.idegaweb.egov.cases.data;

import com.idega.block.process.data.Case;
import com.idega.data.IDOEntity;
import com.idega.user.data.User;


/**
 * <p>
 * TODO laddi Describe Type GeneralCase
 * </p>
 *  Last modified: $Date$ by $Author$
 * 
 * @author <a href="mailto:laddi@idega.com">laddi</a>
 * @version $Revision$
 */
public interface GeneralCase extends IDOEntity, Case {

	/**
	 * @see is.idega.idegaweb.egov.cases.data.GeneralCaseBMPBean#getCaseCodeKey
	 */
	public String getCaseCodeKey();

	/**
	 * @see is.idega.idegaweb.egov.cases.data.GeneralCaseBMPBean#getCaseCodeDescription
	 */
	public String getCaseCodeDescription();

	/**
	 * @see is.idega.idegaweb.egov.cases.data.GeneralCaseBMPBean#getMessage
	 */
	public String getMessage();

	/**
	 * @see is.idega.idegaweb.egov.cases.data.GeneralCaseBMPBean#getReply
	 */
	public String getReply();

	/**
	 * @see is.idega.idegaweb.egov.cases.data.GeneralCaseBMPBean#getCaseCategory
	 */
	public CaseCategory getCaseCategory();

	/**
	 * @see is.idega.idegaweb.egov.cases.data.GeneralCaseBMPBean#getCaseType
	 */
	public CaseType getCaseType();

	/**
	 * @see is.idega.idegaweb.egov.cases.data.GeneralCaseBMPBean#getHandledBy
	 */
	public User getHandledBy();

	/**
	 * @see is.idega.idegaweb.egov.cases.data.GeneralCaseBMPBean#setMessage
	 */
	public void setMessage(String message);

	/**
	 * @see is.idega.idegaweb.egov.cases.data.GeneralCaseBMPBean#setReply
	 */
	public void setReply(String reply);

	/**
	 * @see is.idega.idegaweb.egov.cases.data.GeneralCaseBMPBean#setCaseCategory
	 */
	public void setCaseCategory(CaseCategory category);

	/**
	 * @see is.idega.idegaweb.egov.cases.data.GeneralCaseBMPBean#setCaseType
	 */
	public void setCaseType(CaseType type);

	/**
	 * @see is.idega.idegaweb.egov.cases.data.GeneralCaseBMPBean#setHandledBy
	 */
	public void setHandledBy(User handler);
}
