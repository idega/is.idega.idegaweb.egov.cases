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
import is.idega.idegaweb.egov.cases.data.GeneralCase;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Iterator;

import javax.ejb.FinderException;

import com.idega.block.process.data.CaseLog;
import com.idega.block.process.data.CaseStatus;
import com.idega.business.IBORuntimeException;
import com.idega.core.builder.data.ICPage;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.Span;
import com.idega.presentation.text.Heading1;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.Label;
import com.idega.user.data.User;
import com.idega.util.IWTimestamp;
import com.idega.util.text.Name;


public class CaseViewer extends CaseCreator {
	
	private ICPage iHomePage;
	private ICPage iBackPage;

	protected void present(IWContext iwc) {
		try {
			IWResourceBundle iwrb = getResourceBundle(iwc);
			
			Form form = new Form();
			form.setStyleClass("adminForm");
			
			GeneralCase theCase = null;
			try {
				theCase = getCasesBusiness(iwc).getGeneralCase(iwc.getParameter(getCasesBusiness(iwc).getSelectedCaseParameter()));
			}
			catch (FinderException fe) {
				fe.printStackTrace();
				throw new IBORuntimeException(fe);
			}
			CaseCategory category = theCase.getCaseCategory();
			User user = getCasesBusiness(iwc).getLastModifier(theCase);
			IWTimestamp created = new IWTimestamp(theCase.getCreated());
			
			form.add(getHeader(iwrb.getLocalizedString("case_viewer.view_case", "View case")));

			form.add(getPersonInfo(iwc, theCase.getOwner()));
			
			Layer clearLayer = new Layer(Layer.DIV);
			clearLayer.setStyleClass("Clear");
			
			Layer caseType = new Layer(Layer.SPAN);
			caseType.add(new Text(category.getName()));
			
			Layer message = new Layer(Layer.SPAN);
			message.add(new Text(theCase.getMessage()));
			
			Layer createdDate = new Layer(Layer.SPAN);
			createdDate.add(new Text(created.getLocaleDateAndTime(iwc.getCurrentLocale(), IWTimestamp.SHORT, IWTimestamp.SHORT)));
			
			Heading1 heading = new Heading1(iwrb.getLocalizedString("case_overview", "Case overview"));
			heading.setStyleClass("subHeader");
			heading.setStyleClass("topSubHeader");
			form.add(heading);
			
			Layer section = new Layer(Layer.DIV);
			section.setStyleClass("formSection");
			form.add(section);
			
			Layer formItem = new Layer(Layer.DIV);
			Label label = new Label();
			
			formItem.setStyleClass("formItem");
			label = new Label();
			label.setLabel(iwrb.getLocalizedString("case_type", "Case type"));
			formItem.add(label);
			formItem.add(caseType);
			section.add(formItem);
	
			formItem = new Layer(Layer.DIV);
			formItem.setStyleClass("formItem");
			label = new Label();
			label.setLabel(iwrb.getLocalizedString("created_date", "Created date"));
			formItem.add(label);
			formItem.add(createdDate);
			section.add(formItem);
	
			formItem = new Layer(Layer.DIV);
			formItem.setStyleClass("formItem");
			formItem.setStyleClass("informationItem");
			label = new Label();
			label.setLabel(iwrb.getLocalizedString("message", "Message"));
			formItem.add(label);
			formItem.add(message);
			section.add(formItem);
	
			Layer clear = new Layer(Layer.DIV);
			clear.setStyleClass("Clear");
			section.add(clear);
			
			Collection logs = getCasesBusiness(iwc).getCaseLogs(theCase);
			if (!logs.isEmpty()) {
				Iterator iter = logs.iterator();
				while (iter.hasNext()) {
					CaseLog log = (CaseLog) iter.next();
					form.add(getHandlerLayer(iwc, iwrb, log));
				}
			}
			else {
				Layer handler = new Layer(Layer.SPAN);
				if (user != null) {
					handler.add(new Text(new Name(user.getFirstName(), user.getMiddleName(), user.getLastName()).getName(iwc.getCurrentLocale(), true)));
				} else {
					handler.add(new Text(""));
				}
				heading = new Heading1(iwrb.getLocalizedString("handler_overview", "Handler overview"));
				heading.setStyleClass("subHeader");
				form.add(heading);
				
				section = new Layer(Layer.DIV);
				section.setStyleClass("formSection");
				form.add(section);
				
				formItem = new Layer(Layer.DIV);
				formItem.setStyleClass("formItem");
				label = new Label();
				label.setLabel(iwrb.getLocalizedString("handler", "Handler"));
				formItem.add(label);
				formItem.add(handler);
				section.add(formItem);
		
				if (theCase.getReply() != null) {
					Layer reply = new Layer(Layer.SPAN);
					reply.add(new Text(theCase.getReply()));
				
					formItem = new Layer(Layer.DIV);
					formItem.setStyleClass("formItem");
					formItem.setStyleClass("informationItem");
					label = new Label();
					label.setLabel(iwrb.getLocalizedString("reply", "Reply"));
					formItem.add(label);
					formItem.add(reply);
					section.add(formItem);
				}
				
				section.add(clear);
			}
			
			Layer bottom = new Layer(Layer.DIV);
			bottom.setStyleClass("bottom");
			form.add(bottom);

			if (this.iBackPage != null) {
				Link home = getButtonLink(iwrb.getLocalizedString("back", "Back"));
				home.setPage(this.iBackPage);
				bottom.add(home);
			}
			
			if (getHomePage() != null) {
				Link home = getButtonLink(iwrb.getLocalizedString("my_page", "My page"));
				home.setStyleClass("buttonHome");
				home.setPage(getHomePage());
				bottom.add(home);
			}

			add(form);
		}
		catch (RemoteException re) {
			throw new IBORuntimeException(re);
		}
	}
	
	private Layer getHandlerLayer(IWContext iwc, IWResourceBundle iwrb, CaseLog log) throws RemoteException {
		Layer layer = new Layer(Layer.DIV);
		layer.setStyleClass("handlerLayer");
		
		Heading1 heading = new Heading1(iwrb.getLocalizedString("handler_overview", "Handler overview"));
		heading.setStyleClass("subHeader");
		layer.add(heading);
		
		Layer section = new Layer(Layer.DIV);
		section.setStyleClass("formSection");
		layer.add(section);
		
		User user = log.getPerformer();
		IWTimestamp stamp = new IWTimestamp(log.getTimeStamp());
		CaseStatus status = log.getCaseStatusAfter();
		String reply = log.getComment();
		
		Layer formItem = new Layer(Layer.DIV);
		formItem.setStyleClass("formItem");
		Label label = new Label();
		label.setLabel(iwrb.getLocalizedString("handler", "Handler"));
		formItem.add(label);
		formItem.add(new Span(new Text(new Name(user.getFirstName(), user.getMiddleName(), user.getLastName()).getName(iwc.getCurrentLocale(), true))));
		section.add(formItem);

		formItem = new Layer(Layer.DIV);
		formItem.setStyleClass("formItem");
		label = new Label();
		label.setLabel(iwrb.getLocalizedString("timestamp", "Timestamp"));
		formItem.add(label);
		formItem.add(new Span(new Text(stamp.getLocaleDateAndTime(iwc.getCurrentLocale(), IWTimestamp.SHORT, IWTimestamp.SHORT))));
		section.add(formItem);
		
		formItem = new Layer(Layer.DIV);
		formItem.setStyleClass("formItem");
		label = new Label();
		label.setLabel(iwrb.getLocalizedString("status", "Status"));
		formItem.add(label);
		formItem.add(new Span(new Text(getCasesBusiness(iwc).getLocalizedCaseStatusDescription(status, iwc.getCurrentLocale()))));
		section.add(formItem);
		
		formItem = new Layer(Layer.DIV);
		formItem.setStyleClass("formItem");
		formItem.setStyleClass("informationItem");
		label = new Label();
		label.setLabel(iwrb.getLocalizedString("reply", "Reply"));
		formItem.add(label);
		formItem.add(new Span(new Text(reply)));
		section.add(formItem);
		
		Layer clear = new Layer(Layer.DIV);
		clear.setStyleClass("Clear");
		section.add(clear);
		
		return layer;
	}
	
	protected ICPage getHomePage() {
		return this.iHomePage;
	}
	
	public void setHomePage(ICPage page) {
		this.iHomePage = page;
	}

	
	public void setBackPage(ICPage backPage) {
		this.iBackPage = backPage;
	}
}