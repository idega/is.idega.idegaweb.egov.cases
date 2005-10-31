/*
 * $Id$
 * Created on Oct 31, 2005
 *
 * Copyright (C) 2005 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf.
 * Use is subject to license terms.
 */
package is.idega.idegaweb.egov.cases.presentation;

import is.idega.idegaweb.egov.cases.data.CaseType;
import is.idega.idegaweb.egov.cases.data.GeneralCase;
import javax.ejb.FinderException;
import com.idega.business.IBORuntimeException;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.text.Heading1;
import com.idega.presentation.text.Paragraph;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.GenericButton;
import com.idega.presentation.ui.Label;
import com.idega.user.data.User;
import com.idega.util.IWTimestamp;
import com.idega.util.text.Name;


public class CaseViewer extends CasesBlock {

	protected void present(IWContext iwc) throws Exception {
		Form form = new Form();
		form.setStyleClass("casesForm");
		
		GeneralCase theCase = null;
		try {
			theCase = getBusiness().getGeneralCase(iwc.getParameter(getBusiness().getSelectedCaseParameter()));
			getBusiness().changeCaseStatus(theCase, getBusiness().getCaseStatusInactive().getStatus(), iwc.getCurrentUser(), null);
		}
		catch (FinderException fe) {
			fe.printStackTrace();
			throw new IBORuntimeException(fe);
		}
		CaseType type = theCase.getCaseType();
		User user = getBusiness().getLastModifier(theCase);
		IWTimestamp created = new IWTimestamp(theCase.getCreated());
		
		Layer layer = new Layer(Layer.DIV);
		layer.setStyleClass("elementsLayer");
		form.add(layer);
		
		Heading1 heading = new Heading1(getResourceBundle().getLocalizedString("case_viewer.view_case", "View case"));
		layer.add(heading);
		
		Paragraph caseType = new Paragraph();
		caseType.add(new Text(type.getName()));
		
		Paragraph handler = new Paragraph();
		handler.add(new Text(new Name(user.getFirstName(), user.getMiddleName(), user.getLastName()).getName(iwc.getCurrentLocale(), true)));
		
		Paragraph message = new Paragraph();
		message.add(new Text(theCase.getMessage()));
		
		Paragraph reply = new Paragraph();
		reply.add(new Text(theCase.getReply()));
		
		Paragraph createdDate = new Paragraph();
		createdDate.add(new Text(created.getLocaleDateAndTime(iwc.getCurrentLocale(), IWTimestamp.SHORT, IWTimestamp.SHORT)));
		
		Layer element = new Layer(Layer.DIV);
		element.setStyleClass("formElement");
		Label label = new Label();
		label.setLabel(getResourceBundle().getLocalizedString("case_type", "Case type"));
		element.add(label);
		element.add(caseType);
		layer.add(element);

		element = new Layer(Layer.DIV);
		element.setStyleClass("formElement");
		label = new Label();
		label.setLabel(getResourceBundle().getLocalizedString("created_date", "Created date"));
		element.add(label);
		element.add(createdDate);
		layer.add(element);

		element = new Layer(Layer.DIV);
		element.setStyleClass("formElement");
		label = new Label();
		label.setLabel(getResourceBundle().getLocalizedString("message", "Message"));
		element.add(label);
		element.add(message);
		layer.add(element);

		element = new Layer(Layer.DIV);
		element.setStyleClass("formElement");
		label = new Label();
		label.setLabel(getResourceBundle().getLocalizedString("handler", "Handler"));
		element.add(label);
		element.add(handler);
		layer.add(element);

		element = new Layer(Layer.DIV);
		element.setStyleClass("formElement");
		label = new Label();
		label.setLabel(getResourceBundle().getLocalizedString("reply", "Reply"));
		element.add(label);
		element.add(reply);
		layer.add(element);

		Layer clear = new Layer(Layer.DIV);
		clear.setStyleClass("Clear");
		layer.add(clear);

		if (getHomePage() != null) {
			Layer buttonLayer = new Layer(Layer.DIV);
			buttonLayer.setStyleClass("buttonLayer");
			layer.add(buttonLayer);
			
			GenericButton home = new GenericButton(getResourceBundle().getLocalizedString("my_page", "My page"));
			home.setStyleClass("button");
			home.setPageToOpen(getHomePage());
		}
		
		add(form);
	}
}
