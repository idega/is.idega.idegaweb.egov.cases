/*
 * $Id$
 * Created on Oct 31, 2005
 *
 * Copyright (C) 2005 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf.
 * Use is subject to license terms.
 */
package is.idega.idegaweb.egov.cases.business;

import com.idega.business.IBOHome;


/**
 * <p>
 * TODO laddi Describe Type CasesBusinessHome
 * </p>
 *  Last modified: $Date$ by $Author$
 * 
 * @author <a href="mailto:laddi@idega.com">laddi</a>
 * @version $Revision$
 */
public interface CasesBusinessHome extends IBOHome {

	public CasesBusiness create() throws javax.ejb.CreateException, java.rmi.RemoteException;
}
