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

import is.idega.idegaweb.egov.cases.data.CaseCategory;
import is.idega.idegaweb.egov.cases.data.CaseType;
import java.rmi.RemoteException;
import javax.ejb.CreateException;
import javax.ejb.FinderException;
import com.idega.business.IBORuntimeException;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.text.Heading1;
import com.idega.presentation.text.Paragraph;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.GenericButton;
import com.idega.presentation.ui.Label;
import com.idega.presentation.ui.SubmitButton;
import com.idega.presentation.ui.TextArea;
import com.idega.presentation.ui.util.SelectorUtility;


public class CaseCreator extends CasesBlock {
	
	private static final String PARAMETER_ACTION = "cc_prm_action";
	
	private static final String PARAMETER_MESSAGE = "prm_message";
	private static final String PARAMETER_CASE_CATEGORY_PK = "prm_case_category_pk";
	private static final String PARAMETER_CASE_TYPE_PK = "prm_case_type_pk";
	
	private static final int ACTION_PHASE_1 = 1;
	private static final int ACTION_PHASE_2 = 2;
	private static final int ACTION_OVERVIEW = 3;
	private static final int ACTION_SAVE = 4;

	protected void present(IWContext iwc) throws Exception {
		switch (parseAction(iwc)) {
			case ACTION_PHASE_1:
				showPhaseOne(iwc);
				break;

			case ACTION_PHASE_2:
				showPhaseTwo(iwc);
				break;

			case ACTION_OVERVIEW:
				showOverview(iwc);
				break;

			case ACTION_SAVE:
				save(iwc);
				break;
		}
	}
	
	private int parseAction(IWContext iwc) {
		if (iwc.isParameterSet(PARAMETER_ACTION)) {
			return Integer.parseInt(iwc.getParameter(PARAMETER_ACTION));
		}
		return ACTION_PHASE_1;
	}

	private void showPhaseOne(IWContext iwc) throws RemoteException {
		Form form = new Form();
		form.setStyleClass("casesForm");
		form.maintainParameter(PARAMETER_MESSAGE);
		form.maintainParameter(PARAMETER_CASE_TYPE_PK);
		form.maintainParameter(PARAMETER_CASE_CATEGORY_PK);
		
		Layer layer = new Layer(Layer.DIV);
		layer.setStyleClass("infoLayer");
		form.add(layer);
		
		Heading1 heading = new Heading1(getResourceBundle().getLocalizedString("case_creator.information_heading", "Information"));
		layer.add(heading);
		
		Paragraph paragraph = new Paragraph();
		paragraph.add(new Text(getResourceBundle().getLocalizedString("case_creator.information_text", "Information text here...")));
		layer.add(paragraph);
		
		layer = new Layer(Layer.DIV);
		layer.setStyleClass("buttonLayer");
		form.add(layer);
		
		SubmitButton next = (SubmitButton) new SubmitButton(getResourceBundle().getLocalizedString("next", "Next"), PARAMETER_ACTION, String.valueOf(ACTION_PHASE_2));
		next.setStyleClass("button");
		layer.add(next);

		add(form);
	}
	
	private void showPhaseTwo(IWContext iwc) throws RemoteException {
		Form form = new Form();
		form.setStyleClass("casesForm");
		
		form.add(getPersonInfo(iwc, iwc.getCurrentUser()));
		
		Layer layer = new Layer(Layer.DIV);
		layer.setStyleClass("elementsLayer");
		form.add(layer);
		
		Heading1 heading = new Heading1(getResourceBundle().getLocalizedString("case_creator.enter_case", "New case"));
		layer.add(heading);
		
		SelectorUtility util = new SelectorUtility();
		DropdownMenu categories = (DropdownMenu) util.getSelectorFromIDOEntities(new DropdownMenu(PARAMETER_CASE_CATEGORY_PK), getBusiness().getCaseCategories(), "getName");
		categories.keepStatusOnAction(true);
		
		DropdownMenu types = (DropdownMenu) util.getSelectorFromIDOEntities(new DropdownMenu(PARAMETER_CASE_TYPE_PK), getBusiness().getCaseTypes(), "getName");
		types.keepStatusOnAction(true);
		
		TextArea message = new TextArea(PARAMETER_MESSAGE);
		message.setStyleClass("textarea");
		message.keepStatusOnAction(true);
		
		Layer element = new Layer(Layer.DIV);
		layer.setStyleClass("formElement");
		Label label = new Label(getResourceBundle().getLocalizedString("case_type", "Case type"), types);
		element.add(label);
		element.add(types);
		layer.add(element);

		element = new Layer(Layer.DIV);
		layer.setStyleClass("formElement");
		label = new Label(getResourceBundle().getLocalizedString("case_category", "Case category"), categories);
		element.add(label);
		element.add(categories);
		layer.add(element);

		element = new Layer(Layer.DIV);
		element.setStyleClass("formElement");
		label = new Label(getResourceBundle().getLocalizedString("message", "Message"), message);
		element.add(label);
		element.add(message);
		layer.add(element);

		Layer clear = new Layer(Layer.DIV);
		clear.setStyleClass("Clear");
		layer.add(clear);

		layer = new Layer(Layer.DIV);
		layer.setStyleClass("buttonLayer");
		form.add(layer);
		
		SubmitButton next = (SubmitButton) new SubmitButton(getResourceBundle().getLocalizedString("next", "Next"), PARAMETER_ACTION, String.valueOf(ACTION_OVERVIEW));
		next.setStyleClass("button");
		SubmitButton back = (SubmitButton) new SubmitButton(getResourceBundle().getLocalizedString("back", "Back"), PARAMETER_ACTION, String.valueOf(ACTION_PHASE_1));
		back.setStyleClass("button");
		layer.add(back);
		layer.add(next);
		
		add(form);
	}
	
	private void showOverview(IWContext iwc) throws RemoteException {
		Form form = new Form();
		form.setStyleClass("casesForm");
		form.maintainParameter(PARAMETER_MESSAGE);
		form.maintainParameter(PARAMETER_CASE_TYPE_PK);
		form.maintainParameter(PARAMETER_CASE_CATEGORY_PK);
		
		form.add(getPersonInfo(iwc, iwc.getCurrentUser()));
		
		String message = iwc.getParameter(PARAMETER_MESSAGE);
		if (message == null || message.length() == 0) {
			getParentPage().setAlertOnLoad(getResourceBundle().getLocalizedString("case_creator.message_empty", "You must enter a message"));
			showPhaseTwo(iwc);
			return;
		}
		Object caseCategoryPK = iwc.getParameter(PARAMETER_CASE_CATEGORY_PK);
		CaseCategory category = null;
		try {
			category = getBusiness().getCaseCategory(caseCategoryPK);
		}
		catch (FinderException fe) {
			throw new IBORuntimeException(fe);
		}
		Object caseTypePK = iwc.getParameter(PARAMETER_CASE_TYPE_PK);
		CaseType type = null;
		try {
			type = getBusiness().getCaseType(caseTypePK);
		}
		catch (FinderException fe) {
			throw new IBORuntimeException(fe);
		}

		Layer layer = new Layer(Layer.DIV);
		layer.setStyleClass("elementsLayer");
		form.add(layer);
		
		Heading1 heading = new Heading1(getResourceBundle().getLocalizedString("case_creator.enter_case", "New case"));
		layer.add(heading);
		
		Paragraph typeSpan = new Paragraph();
		typeSpan.add(new Text(type.getName()));
		
		Paragraph categorySpan = new Paragraph();
		categorySpan.add(new Text(category.getName()));
		
		Paragraph messageSpan = new Paragraph();
		messageSpan.add(new Text(message));
		
		Layer element = new Layer(Layer.DIV);
		layer.setStyleClass("formElement");
		Label label = new Label();
		label.setLabel(getResourceBundle().getLocalizedString("case_type", "Case type"));
		element.add(label);
		element.add(typeSpan);
		layer.add(element);

		element = new Layer(Layer.DIV);
		layer.setStyleClass("formElement");
		label = new Label();
		label.setLabel(getResourceBundle().getLocalizedString("case_category", "Case category"));
		element.add(label);
		element.add(categorySpan);
		layer.add(element);

		element = new Layer(Layer.DIV);
		element.setStyleClass("formElement");
		label = new Label();
		label.setLabel(getResourceBundle().getLocalizedString("message", "Message"));
		element.add(label);
		element.add(messageSpan);
		layer.add(element);

		Layer clear = new Layer(Layer.DIV);
		clear.setStyleClass("Clear");
		layer.add(clear);

		layer = new Layer(Layer.DIV);
		layer.setStyleClass("buttonLayer");
		form.add(layer);
		
		SubmitButton send = (SubmitButton) new SubmitButton(getResourceBundle().getLocalizedString("send", "Send"), PARAMETER_ACTION, String.valueOf(ACTION_SAVE));
		send.setStyleClass("button");
		SubmitButton back = (SubmitButton) new SubmitButton(getResourceBundle().getLocalizedString("back", "Back"), PARAMETER_ACTION, String.valueOf(ACTION_PHASE_2));
		back.setStyleClass("button");
		layer.add(back);
		layer.add(send);
		
		add(form);
	}
	
	private void save(IWContext iwc) throws RemoteException {
		String message = iwc.getParameter(PARAMETER_MESSAGE);
		Object caseCategoryPK = iwc.getParameter(PARAMETER_CASE_CATEGORY_PK);
		Object caseTypePK = iwc.getParameter(PARAMETER_CASE_TYPE_PK);
		
		try {
			getBusiness().storeGeneralCase(iwc.getCurrentUser(), caseCategoryPK, caseTypePK, message);

			Layer layer = new Layer(Layer.DIV);
			layer.setID("elementsLayer");
			add(layer);
			
			layer.add(new Heading1(getResourceBundle().getLocalizedString("case_creator.save_completed", "Application sent")));

			Paragraph paragraph = new Paragraph();
			paragraph.add(new Text(getResourceBundle().getLocalizedString("case_creator.save_confirmation", "Your case has been sent and will be processed accordingly.")));
			layer.add(paragraph);
			
			if (getHomePage() != null) {
				Layer buttonLayer = new Layer(Layer.DIV);
				buttonLayer.setStyleClass("buttonLayer");
				layer.add(buttonLayer);
				
				GenericButton home = new GenericButton(getResourceBundle().getLocalizedString("my_page", "My page"));
				home.setStyleClass("button");
				home.setPageToOpen(getHomePage());
			}
		}
		catch (CreateException ce) {
			ce.printStackTrace();
			throw new IBORuntimeException(ce);
		}
	}
}