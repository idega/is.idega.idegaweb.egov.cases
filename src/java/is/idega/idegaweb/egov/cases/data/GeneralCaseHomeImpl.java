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

import java.util.Collection;
import javax.ejb.FinderException;
import com.idega.data.IDOException;
import com.idega.data.IDOFactory;
import com.idega.user.data.User;


/**
 * <p>
 * TODO laddi Describe Type GeneralCaseHomeImpl
 * </p>
 *  Last modified: $Date$ by $Author$
 * 
 * @author <a href="mailto:laddi@idega.com">laddi</a>
 * @version $Revision$
 */
public class GeneralCaseHomeImpl extends IDOFactory implements GeneralCaseHome {

	protected Class getEntityInterfaceClass() {
		return GeneralCase.class;
	}

	public GeneralCase create() throws javax.ejb.CreateException {
		return (GeneralCase) super.createIDO();
	}

	public GeneralCase findByPrimaryKey(Object pk) throws javax.ejb.FinderException {
		return (GeneralCase) super.findByPrimaryKeyIDO(pk);
	}

	public Collection findAllByGroup(Collection groups) throws FinderException {
		com.idega.data.IDOEntity entity = this.idoCheckOutPooledEntity();
		java.util.Collection ids = ((GeneralCaseBMPBean) entity).ejbFindAllByGroup(groups);
		this.idoCheckInPooledEntity(entity);
		return this.getEntityCollectionForPrimaryKeys(ids);
	}

	public Collection findAllByGroupAndStatuses(Collection groups, String[] statuses) throws FinderException {
		com.idega.data.IDOEntity entity = this.idoCheckOutPooledEntity();
		java.util.Collection ids = ((GeneralCaseBMPBean) entity).ejbFindAllByGroupAndStatuses(groups, statuses);
		this.idoCheckInPooledEntity(entity);
		return this.getEntityCollectionForPrimaryKeys(ids);
	}

	public Collection findAllByHandler(User handler) throws FinderException {
		com.idega.data.IDOEntity entity = this.idoCheckOutPooledEntity();
		java.util.Collection ids = ((GeneralCaseBMPBean) entity).ejbFindAllByHandler(handler);
		this.idoCheckInPooledEntity(entity);
		return this.getEntityCollectionForPrimaryKeys(ids);
	}

	public Collection findAllByHandlerAndStatuses(User handler, String[] statuses) throws FinderException {
		com.idega.data.IDOEntity entity = this.idoCheckOutPooledEntity();
		java.util.Collection ids = ((GeneralCaseBMPBean) entity).ejbFindAllByHandlerAndStatuses(handler, statuses);
		this.idoCheckInPooledEntity(entity);
		return this.getEntityCollectionForPrimaryKeys(ids);
	}

	public Collection findAllByUsers(Collection users) throws FinderException {
		com.idega.data.IDOEntity entity = this.idoCheckOutPooledEntity();
		java.util.Collection ids = ((GeneralCaseBMPBean) entity).ejbFindAllByUsers(users);
		this.idoCheckInPooledEntity(entity);
		return this.getEntityCollectionForPrimaryKeys(ids);
	}

	public int getCountByGroup(Collection groups) throws IDOException {
		com.idega.data.IDOEntity entity = this.idoCheckOutPooledEntity();
		int theReturn = ((GeneralCaseBMPBean) entity).ejbHomeGetCountByGroup(groups);
		this.idoCheckInPooledEntity(entity);
		return theReturn;
	}

	public int getCountByGroupAndStatuses(Collection groups, String[] statuses) throws IDOException {
		com.idega.data.IDOEntity entity = this.idoCheckOutPooledEntity();
		int theReturn = ((GeneralCaseBMPBean) entity).ejbHomeGetCountByGroupAndStatuses(groups, statuses);
		this.idoCheckInPooledEntity(entity);
		return theReturn;
	}
}
