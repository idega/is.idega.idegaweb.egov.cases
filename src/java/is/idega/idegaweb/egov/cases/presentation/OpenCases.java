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

import is.idega.idegaweb.egov.cases.data.CaseCategory;
import is.idega.idegaweb.egov.cases.data.CaseType;
import is.idega.idegaweb.egov.cases.data.GeneralCase;

import java.rmi.RemoteException;
import java.util.Collection;

import javax.ejb.FinderException;

import com.idega.business.IBORuntimeException;
import com.idega.core.builder.data.ICPage;
import com.idega.event.IWPageEventListener;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.text.Heading1;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.Label;
import com.idega.user.data.User;
import com.idega.util.IWTimestamp;


public class OpenCases extends CasesProcessor implements IWPageEventListener {

	private ICPage iMyCasesPage;
	
	protected String getBlockID() {
		return "openCases";
	}

	public boolean actionPerformed(IWContext iwc) {
		if (iwc.isParameterSet(PARAMETER_CASE_PK)) {
			Object casePK = iwc.getParameter(PARAMETER_CASE_PK);
			
			try {
				getCasesBusiness(iwc).takeCase(casePK, iwc.getCurrentUser());
				return true;
			}
			catch (RemoteException re) {
				throw new IBORuntimeException(re);
			}
			catch (FinderException fe) {
				fe.printStackTrace();
			}
		}
		return false;
	}

	protected Collection getCases(User user) throws RemoteException {
		Collection groups = getUserBusiness().getUserGroupsDirectlyRelated(user);
		return getBusiness().getOpenCases(groups);
	}
	
	protected void showProcessor(IWContext iwc, Object casePK) throws RemoteException {
		Form form = new Form();
		form.setStyleClass("adminForm");
		form.setEventListener(this.getClass());
		if (iMyCasesPage != null) {
			form.setPageToSubmitTo(iMyCasesPage);
		}
		form.addParameter(PARAMETER_ACTION, "");
		form.maintainParameter(PARAMETER_CASE_PK);
		
		GeneralCase theCase = null;
		try {
			theCase = getBusiness().getGeneralCase(casePK);
		}
		catch (FinderException fe) {
			fe.printStackTrace();
			throw new IBORuntimeException(fe);
		}
		CaseCategory category = theCase.getCaseCategory();
		CaseType type = theCase.getCaseType();
		User owner = theCase.getOwner();
		IWTimestamp created = new IWTimestamp(theCase.getCreated());
		
		form.add(getPersonInfo(iwc, owner));
		
		Heading1 heading = new Heading1(getResourceBundle().getLocalizedString("case_overview", "Case overview"));
		heading.setStyleClass("subHeader");
		heading.setStyleClass("topSubHeader");
		form.add(heading);
		
		Layer section = new Layer(Layer.DIV);
		section.setStyleClass("formSection");
		form.add(section);
		
		Layer caseType = new Layer(Layer.SPAN);
		caseType.add(new Text(type.getName()));
		
		Layer caseCategory = new Layer(Layer.SPAN);
		caseCategory.add(new Text(category.getName()));
		
		Layer message = new Layer(Layer.SPAN);
		message.add(new Text(theCase.getMessage()));
		
		Layer createdDate = new Layer(Layer.SPAN);
		createdDate.add(new Text(created.getLocaleDateAndTime(iwc.getCurrentLocale(), IWTimestamp.SHORT, IWTimestamp.SHORT)));
		
		Layer element = new Layer(Layer.DIV);
		element.setStyleClass("formItem");
		Label label = new Label();
		label.setLabel(getResourceBundle().getLocalizedString("case_type", "Case type"));
		element.add(label);
		element.add(caseType);
		section.add(element);

		element = new Layer(Layer.DIV);
		element.setStyleClass("formItem");
		label = new Label();
		label.setLabel(getResourceBundle().getLocalizedString("case_category", "Case category"));
		element.add(label);
		element.add(caseCategory);
		section.add(element);

		element = new Layer(Layer.DIV);
		element.setStyleClass("formItem");
		label = new Label();
		label.setLabel(getResourceBundle().getLocalizedString("created_date", "Created date"));
		element.add(label);
		element.add(createdDate);
		section.add(element);

		element = new Layer(Layer.DIV);
		element.setStyleClass("formItem");
		label = new Label();
		label.setLabel(getResourceBundle().getLocalizedString("message", "Message"));
		element.add(label);
		element.add(message);
		section.add(element);

		Layer clear = new Layer(Layer.DIV);
		clear.setStyleClass("Clear");
		section.add(clear);

		Layer bottom = new Layer(Layer.DIV);
		bottom.setStyleClass("bottom");
		form.add(bottom);

		Link next = getButtonLink(theCase.getCaseStatus().equals(getBusiness().getCaseStatusPending()) ?  getResourceBundle().getLocalizedString("take_over_case", "Take over case") : getResourceBundle().getLocalizedString("take_case", "Take case"));
		next.setValueOnClick(PARAMETER_ACTION, String.valueOf(ACTION_PROCESS));
		next.setToFormSubmit(form);
		bottom.add(next);

		Link back = getButtonLink(getResourceBundle().getLocalizedString("back", "Back"));
		back.setValueOnClick(PARAMETER_ACTION, String.valueOf(ACTION_VIEW));
		back.setToFormSubmit(form);
		bottom.add(back);

		add(form);
	}

	protected void save(IWContext iwc) throws RemoteException {
	}

	
	public void setMyCasesPage(ICPage myCasesPage) {
		iMyCasesPage = myCasesPage;
	}
}