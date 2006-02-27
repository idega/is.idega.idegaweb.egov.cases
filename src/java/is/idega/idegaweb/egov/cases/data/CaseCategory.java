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
import com.idega.user.data.Group;


/**
 * <p>
 * TODO laddi Describe Type CaseCategory
 * </p>
 *  Last modified: $Date$ by $Author$
 * 
 * @author <a href="mailto:laddi@idega.com">laddi</a>
 * @version $Revision$
 */
public interface CaseCategory extends IDOEntity {

	/**
	 * @see is.idega.idegaweb.egov.cases.data.CaseCategoryBMPBean#getName
	 */
	public String getName();

	/**
	 * @see is.idega.idegaweb.egov.cases.data.CaseCategoryBMPBean#getDescription
	 */
	public String getDescription();

	/**
	 * @see is.idega.idegaweb.egov.cases.data.CaseCategoryBMPBean#getHandlerGroup
	 */
	public Group getHandlerGroup();

	/**
	 * @see is.idega.idegaweb.egov.cases.data.CaseCategoryBMPBean#getOrder
	 */
	public int getOrder();

	/**
	 * @see is.idega.idegaweb.egov.cases.data.CaseCategoryBMPBean#setName
	 */
	public void setName(String name);

	/**
	 * @see is.idega.idegaweb.egov.cases.data.CaseCategoryBMPBean#setDescription
	 */
	public void setDescription(String description);

	/**
	 * @see is.idega.idegaweb.egov.cases.data.CaseCategoryBMPBean#setHandlerGroup
	 */
	public void setHandlerGroup(Group group);

	/**
	 * @see is.idega.idegaweb.egov.cases.data.CaseCategoryBMPBean#setHandlerGroup
	 */
	public void setHandlerGroup(Object groupPK);

	/**
	 * @see is.idega.idegaweb.egov.cases.data.CaseCategoryBMPBean#setOrder
	 */
	public void setOrder(int order);
}