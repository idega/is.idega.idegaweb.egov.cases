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

import com.idega.business.IBOHomeImpl;


/**
 * <p>
 * TODO laddi Describe Type CasesBusinessHomeImpl
 * </p>
 *  Last modified: $Date$ by $Author$
 * 
 * @author <a href="mailto:laddi@idega.com">laddi</a>
 * @version $Revision$
 */
public class CasesBusinessHomeImpl extends IBOHomeImpl implements CasesBusinessHome {

	protected Class getBeanInterfaceClass() {
		return CasesBusiness.class;
	}

	public CasesBusiness create() throws javax.ejb.CreateException {
		return (CasesBusiness) super.createIBO();
	}
}
