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

import com.idega.data.IDOEntity;


/**
 * <p>
 * TODO laddi Describe Type CaseType
 * </p>
 *  Last modified: $Date$ by $Author$
 * 
 * @author <a href="mailto:laddi@idega.com">laddi</a>
 * @version $Revision$
 */
public interface CaseType extends IDOEntity {

	/**
	 * @see is.idega.idegaweb.egov.cases.data.CaseTypeBMPBean#getName
	 */
	public String getName();

	/**
	 * @see is.idega.idegaweb.egov.cases.data.CaseTypeBMPBean#getDescription
	 */
	public String getDescription();

	/**
	 * @see is.idega.idegaweb.egov.cases.data.CaseTypeBMPBean#setName
	 */
	public void setName(String name);

	/**
	 * @see is.idega.idegaweb.egov.cases.data.CaseTypeBMPBean#setDescription
	 */
	public void setDescription(String description);
}
