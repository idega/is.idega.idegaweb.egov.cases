/*
 * $Id$
 * Created on Oct 18, 2006
 *
 * Copyright (C) 2006 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf.
 * Use is subject to license terms.
 */
package is.idega.idegaweb.egov.cases.business;

import is.idega.idegaweb.egov.cases.data.CaseCategory;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.ejb.FinderException;

import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.business.IBORuntimeException;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.presentation.IWContext;
import com.idega.presentation.remotescripting.RemoteScriptCollection;
import com.idega.presentation.remotescripting.RemoteScriptHandler;
import com.idega.presentation.remotescripting.RemoteScriptingResults;
import com.idega.user.business.UserBusiness;
import com.idega.user.data.Group;
import com.idega.user.data.User;

public class CaseHandlerCollectionHandler implements RemoteScriptCollection {

	public RemoteScriptingResults getResults(IWContext iwc) {
		String sourceName = iwc.getParameter(RemoteScriptHandler.PARAMETER_SOURCE_PARAMETER_NAME);
		String sourceID = iwc.getParameter(sourceName);

		Collection ids = new ArrayList();
		Collection names = new ArrayList();

		try {
			CaseCategory category = getBusiness(iwc).getCaseCategory(sourceID);
			Group handlerGroup = category.getHandlerGroup();

			Collection handlers = getUserBusiness(iwc).getUsersInGroup(handlerGroup);
			if (!handlers.isEmpty()) {
				Iterator iter = handlers.iterator();
				while (iter.hasNext()) {
					User handler = (User) iter.next();
					ids.add(handler.getPrimaryKey().toString());
					names.add(handler.getName());
				}
			}
		}
		catch (RemoteException e) {
			e.printStackTrace();
		}
		catch (FinderException e) {
			e.printStackTrace();
		}

		RemoteScriptingResults rsr = new RemoteScriptingResults(RemoteScriptHandler.getLayerName(sourceName, "id"), ids);
		rsr.addLayer(RemoteScriptHandler.getLayerName(sourceName, "name"), names);

		return rsr;
	}

	private CasesBusiness getBusiness(IWApplicationContext iwac) {
		try {
			return (CasesBusiness) IBOLookup.getServiceInstance(iwac, CasesBusiness.class);
		}
		catch (IBOLookupException ile) {
			throw new IBORuntimeException(ile);
		}
	}

	private UserBusiness getUserBusiness(IWApplicationContext iwac) {
		try {
			return (UserBusiness) IBOLookup.getServiceInstance(iwac, UserBusiness.class);
		}
		catch (IBOLookupException ile) {
			throw new IBORuntimeException(ile);
		}
	}
}