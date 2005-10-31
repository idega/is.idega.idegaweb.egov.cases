/*
 * $Id$
 * Created on Oct 31, 2005
 *
 * Copyright (C) 2005 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf.
 * Use is subject to license terms.
 */
package is.idega.idegaweb.egov.cases.data;

import java.util.Collection;
import javax.ejb.FinderException;
import com.idega.data.IDOException;
import com.idega.data.IDOHome;


/**
 * <p>
 * TODO laddi Describe Type GeneralCaseHome
 * </p>
 *  Last modified: $Date$ by $Author$
 * 
 * @author <a href="mailto:laddi@idega.com">laddi</a>
 * @version $Revision$
 */
public interface GeneralCaseHome extends IDOHome {

	public GeneralCase create() throws javax.ejb.CreateException;

	public GeneralCase findByPrimaryKey(Object pk) throws javax.ejb.FinderException;

	/**
	 * @see is.idega.idegaweb.egov.cases.data.GeneralCaseBMPBean#ejbFindAllByGroup
	 */
	public Collection findAllByGroup(Collection groups) throws FinderException;

	/**
	 * @see is.idega.idegaweb.egov.cases.data.GeneralCaseBMPBean#ejbFindAllByGroupAndStatuses
	 */
	public Collection findAllByGroupAndStatuses(Collection groups, String[] statuses) throws FinderException;

	/**
	 * @see is.idega.idegaweb.egov.cases.data.GeneralCaseBMPBean#ejbHomeGetCountByGroup
	 */
	public int getCountByGroup(Collection groups) throws IDOException;

	/**
	 * @see is.idega.idegaweb.egov.cases.data.GeneralCaseBMPBean#ejbHomeGetCountByGroupAndStatuses
	 */
	public int getCountByGroupAndStatuses(Collection groups, String[] statuses) throws IDOException;
}
