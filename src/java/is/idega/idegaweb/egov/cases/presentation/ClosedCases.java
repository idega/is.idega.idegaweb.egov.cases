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
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.text.Heading1;
import com.idega.presentation.text.Paragraph;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.Label;
import com.idega.presentation.ui.SubmitButton;
import com.idega.user.data.User;
import com.idega.util.IWTimestamp;
import com.idega.util.text.Name;


public class ClosedCases extends CasesProcessor {

	protected Collection getCases(User user) throws RemoteException {
		Collection groups = getUserBusiness().getUserGroupsDirectlyRelated(user);
		return getBusiness().getClosedCases(groups);
	}

	protected void showProcessor(IWContext iwc, Object casePK) throws RemoteException {
		Form form = new Form();
		form.setStyleClass("casesForm");
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
		
		Layer layer = new Layer(Layer.DIV);
		layer.setStyleClass("elementsLayer");
		form.add(layer);
		
		Heading1 heading = new Heading1(getResourceBundle().getLocalizedString("case_processor.handle_case", "Handle case"));
		layer.add(heading);
		
		Paragraph caseType = new Paragraph();
		caseType.add(new Text(type.getName()));
		
		Paragraph caseCategory = new Paragraph();
		caseCategory.add(new Text(category.getName()));
		
		Paragraph sender = new Paragraph();
		sender.add(new Text(new Name(owner.getFirstName(), owner.getMiddleName(), owner.getLastName()).getName(iwc.getCurrentLocale())));
		
		Paragraph message = new Paragraph();
		message.add(new Text(theCase.getMessage()));
		
		Paragraph reply = new Paragraph();
		reply.add(new Text(theCase.getReply()));
		
		Paragraph handler = new Paragraph();
		handler.add(new Text(theCase.getHandledBy().getName()));
		
		Paragraph createdDate = new Paragraph();
		createdDate.add(new Text(created.getLocaleDateAndTime(iwc.getCurrentLocale(), IWTimestamp.SHORT, IWTimestamp.SHORT)));
		
		Layer element = new Layer(Layer.DIV);
		layer.setStyleClass("formElement");
		Label label = new Label();
		label.setLabel(getResourceBundle().getLocalizedString("sender", "Sender"));
		element.add(label);
		element.add(sender);
		layer.add(element);

		element = new Layer(Layer.DIV);
		layer.setStyleClass("formElement");
		label = new Label();
		label.setLabel(getResourceBundle().getLocalizedString("case_type", "Case type"));
		element.add(label);
		element.add(caseType);
		layer.add(element);

		element = new Layer(Layer.DIV);
		layer.setStyleClass("formElement");
		label = new Label();
		label.setLabel(getResourceBundle().getLocalizedString("case_category", "Case category"));
		element.add(label);
		element.add(caseCategory);
		layer.add(element);

		element = new Layer(Layer.DIV);
		layer.setStyleClass("formElement");
		label = new Label();
		label.setLabel(getResourceBundle().getLocalizedString("created_date", "Created date"));
		element.add(label);
		element.add(createdDate);
		layer.add(element);

		element = new Layer(Layer.DIV);
		layer.setStyleClass("formElement");
		label = new Label();
		label.setLabel(getResourceBundle().getLocalizedString("message", "Message"));
		element.add(label);
		element.add(message);
		layer.add(element);

		element = new Layer(Layer.DIV);
		layer.setStyleClass("formElement");
		label = new Label();
		label.setLabel(getResourceBundle().getLocalizedString("handler", "Handler"));
		element.add(label);
		element.add(handler);
		layer.add(element);

		element = new Layer(Layer.DIV);
		layer.setStyleClass("formElement");
		label = new Label();
		label.setLabel(getResourceBundle().getLocalizedString("reply", "Reply"));
		element.add(label);
		element.add(reply);
		layer.add(element);

		Layer clear = new Layer(Layer.DIV);
		clear.setStyleClass("Clear");
		layer.add(clear);

		layer = new Layer(Layer.DIV);
		layer.setStyleClass("buttonLayer");
		form.add(layer);
		
		SubmitButton next = (SubmitButton) new SubmitButton(getResourceBundle().getLocalizedString("reactivate_case", "Reactivate case"), PARAMETER_ACTION, String.valueOf(ACTION_SAVE));
		next.setStyleClass("button");
		SubmitButton back = (SubmitButton) new SubmitButton(getResourceBundle().getLocalizedString("back", "Back"), PARAMETER_ACTION, String.valueOf(ACTION_VIEW));
		back.setStyleClass("button");
		layer.add(back);
		layer.add(next);
		
		add(form);
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
