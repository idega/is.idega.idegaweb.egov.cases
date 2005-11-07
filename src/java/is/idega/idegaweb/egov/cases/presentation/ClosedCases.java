/*
 * $Id$
 * Created on Nov 7, 2005
 *
 * Copyright (C) 2005 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf.
 * Use is subject to license terms.
 */
package is.idega.idegaweb.egov.cases.presentation;

import java.rmi.RemoteException;
import java.util.Collection;
import javax.ejb.FinderException;
import com.idega.presentation.IWContext;
import com.idega.user.data.User;


public class ClosedCases extends CasesProcessor {

	protected Collection getCases(User user) throws RemoteException {
		Collection groups = getUserBusiness().getUserGroupsDirectlyRelated(user);
		return getBusiness().getClosedCases(groups);
	}

	protected void showProcessor(IWContext iwc, Object casePK) throws RemoteException {
	}

	protected void save(IWContext iwc) throws RemoteException {
		Object casePK = iwc.getParameter(PARAMETER_CASE_PK);
		
		try {
			getBusiness().reactivateCase(casePK, iwc.getCurrentUser());
		}
		catch (FinderException fe) {
			fe.printStackTrace();
		}
	}
}
