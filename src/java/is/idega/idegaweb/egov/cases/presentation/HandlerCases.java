/**
 * @(#)HandlerCases.java    1.0.0 1:56:21 PM
 *
 * Idega Software hf. Source Code Licence Agreement x
 *
 * This agreement, made this 10th of February 2006 by and between
 * Idega Software hf., a business formed and operating under laws
 * of Iceland, having its principal place of business in Reykjavik,
 * Iceland, hereinafter after referred to as "Manufacturer" and Agura
 * IT hereinafter referred to as "Licensee".
 * 1.  License Grant: Upon completion of this agreement, the source
 *     code that may be made available according to the documentation for
 *     a particular software product (Software) from Manufacturer
 *     (Source Code) shall be provided to Licensee, provided that
 *     (1) funds have been received for payment of the License for Software and
 *     (2) the appropriate License has been purchased as stated in the
 *     documentation for Software. As used in this License Agreement,
 *     Licensee shall also mean the individual using or installing
 *     the source code together with any individual or entity, including
 *     but not limited to your employer, on whose behalf you are acting
 *     in using or installing the Source Code. By completing this agreement,
 *     Licensee agrees to be bound by the terms and conditions of this Source
 *     Code License Agreement. This Source Code License Agreement shall
 *     be an extension of the Software License Agreement for the associated
 *     product. No additional amendment or modification shall be made
 *     to this Agreement except in writing signed by Licensee and
 *     Manufacturer. This Agreement is effective indefinitely and once
 *     completed, cannot be terminated. Manufacturer hereby grants to
 *     Licensee a non-transferable, worldwide license during the term of
 *     this Agreement to use the Source Code for the associated product
 *     purchased. In the event the Software License Agreement to the
 *     associated product is terminated; (1) Licensee's rights to use
 *     the Source Code are revoked and (2) Licensee shall destroy all
 *     copies of the Source Code including any Source Code used in
 *     Licensee's applications.
 * 2.  License Limitations
 *     2.1 Licensee may not resell, rent, lease or distribute the
 *         Source Code alone, it shall only be distributed as a
 *         compiled component of an application.
 *     2.2 Licensee shall protect and keep secure all Source Code
 *         provided by this this Source Code License Agreement.
 *         All Source Code provided by this Agreement that is used
 *         with an application that is distributed or accessible outside
 *         Licensee's organization (including use from the Internet),
 *         must be protected to the extent that it cannot be easily
 *         extracted or decompiled.
 *     2.3 The Licensee shall not resell, rent, lease or distribute
 *         the products created from the Source Code in any way that
 *         would compete with Idega Software.
 *     2.4 Manufacturer's copyright notices may not be removed from
 *         the Source Code.
 *     2.5 All modifications on the source code by Licencee must
 *         be submitted to or provided to Manufacturer.
 * 3.  Copyright: Manufacturer's source code is copyrighted and contains
 *     proprietary information. Licensee shall not distribute or
 *     reveal the Source Code to anyone other than the software
 *     developers of Licensee's organization. Licensee may be held
 *     legally responsible for any infringement of intellectual property
 *     rights that is caused or encouraged by Licensee's failure to abide
 *     by the terms of this Agreement. Licensee may make copies of the
 *     Source Code provided the copyright and trademark notices are
 *     reproduced in their entirety on the copy. Manufacturer reserves
 *     all rights not specifically granted to Licensee.
 *
 * 4.  Warranty & Risks: Although efforts have been made to assure that the
 *     Source Code is correct, reliable, date compliant, and technically
 *     accurate, the Source Code is licensed to Licensee as is and without
 *     warranties as to performance of merchantability, fitness for a
 *     particular purpose or use, or any other warranties whether
 *     expressed or implied. Licensee's organization and all users
 *     of the source code assume all risks when using it. The manufacturers,
 *     distributors and resellers of the Source Code shall not be liable
 *     for any consequential, incidental, punitive or special damages
 *     arising out of the use of or inability to use the source code or
 *     the provision of or failure to provide support services, even if we
 *     have been advised of the possibility of such damages. In any case,
 *     the entire liability under any provision of this agreement shall be
 *     limited to the greater of the amount actually paid by Licensee for the
 *     Software or 5.00 USD. No returns will be provided for the associated
 *     License that was purchased to become eligible to receive the Source
 *     Code after Licensee receives the source code.
 */
package is.idega.idegaweb.egov.cases.presentation;

import is.idega.idegaweb.egov.cases.business.CaseWriter;
import is.idega.idegaweb.egov.cases.data.CaseCategory;
import is.idega.idegaweb.egov.cases.data.CaseType;
import is.idega.idegaweb.egov.cases.data.GeneralCase;

import java.rmi.RemoteException;
import java.util.Collection;

import javax.ejb.FinderException;

import org.springframework.beans.factory.annotation.Autowired;

import com.idega.block.process.business.CasesRetrievalManager;
import com.idega.block.process.data.CaseLog;
import com.idega.block.process.presentation.UserCases;
import com.idega.block.web2.business.JQuery;
import com.idega.business.IBORuntimeException;
import com.idega.core.file.data.ICFile;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.Span;
import com.idega.presentation.text.Heading1;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.HiddenInput;
import com.idega.presentation.ui.Label;
import com.idega.presentation.ui.TextArea;
import com.idega.presentation.ui.util.SelectorUtility;
import com.idega.user.data.User;
import com.idega.util.CoreConstants;
import com.idega.util.IWTimestamp;
import com.idega.util.PresentationUtil;
import com.idega.util.expression.ELUtil;
import com.idega.util.text.TextSoap;

/**
 * <p>Cases viewer for current handler only, show cases that only
 * current handler manages.</p>
 * <p>You can report about problems to:
 * <a href="mailto:martynas@idega.is">Martynas Stakė</a></p>
 *
 * @version 1.0.0 Jan 17, 2013
 * @author <a href="mailto:martynas@idega.is">Martynas Stakė</a>
 */
public class HandlerCases extends CasesProcessor {

	@Autowired
	private JQuery jQuery;

	private JQuery getJQuery() {
		if (jQuery == null) {
			ELUtil.getInstance().autowire(this);
		}
		return jQuery;
	}

	@Override
	protected void present(IWContext iwc) throws Exception {
		ELUtil.getInstance().autowire(this);
		super.display(iwc);
	}

	@Override
	public boolean showCheckBox() {
		return false;
	}

	@Override
	public boolean showCheckBoxes() {
		return false;
	}

	@Override
	public String getCasesProcessorType() {
		return CasesRetrievalManager.CASE_LIST_TYPE_HANDLER;
	}

	@Override
	protected String getBlockID() {
		return "handlerCases";
	}

	@Override
	protected void showProcessor(IWContext iwc, Object casePK) throws RemoteException {
		PresentationUtil.addJavaScriptSourceLineToHeader(iwc, getJQuery().getBundleURIToJQueryLib());
		PresentationUtil.addJavaScriptSourceLineToHeader(iwc, CoreConstants.DWR_ENGINE_SCRIPT);
		PresentationUtil.addJavaScriptSourceLineToHeader(iwc, CoreConstants.DWR_UTIL_SCRIPT);
		PresentationUtil.addJavaScriptSourceLineToHeader(iwc, "/dwr/interface/CasesBusiness.js");
		PresentationUtil.addJavaScriptSourceLineToHeader(iwc, getBundle().getVirtualPathWithFileNameString("javascript/egov_cases.js"));

		Form form = new Form();
		form.setStyleClass("adminForm");
		form.setStyleClass("overview");
		form.maintainParameter(PARAMETER_CASE_PK);
		form.addParameter(UserCases.PARAMETER_ACTION, "");

		HiddenInput localeInput = new HiddenInput("current_locale", iwc.getCurrentLocale().getCountry().toLowerCase());
		localeInput.setID("casesLocale");
		form.add(localeInput);

		boolean useSubCategories = getCasesBusiness(iwc).useSubCategories();

		GeneralCase theCase = null;
		try {
			theCase = getCasesBusiness().getGeneralCase(casePK);
		}
		catch (FinderException fe) {
			fe.printStackTrace();
			throw new IBORuntimeException(fe);
		}
		CaseCategory category = theCase.getCaseCategory();
		CaseCategory parentCategory = category.getParent();
		CaseType type = theCase.getCaseType();
		ICFile attachment = theCase.getAttachment();
		User owner = theCase.getOwner();
		IWTimestamp created = new IWTimestamp(theCase.getCreated());

		form.add(getPersonInfo(iwc, owner));

		Heading1 heading = new Heading1(getResourceBundle().getLocalizedString(getPrefix() + "case_overview", "Case overview"));
		heading.setStyleClass("subHeader");
		heading.setStyleClass("topSubHeader");
		form.add(heading);

		Layer section = new Layer(Layer.DIV);
		section.setStyleClass("formSection");
		form.add(section);

		if (theCase.isPrivate()) {
			section.add(getAttentionLayer(getResourceBundle().getLocalizedString(getPrefix() + "case.is_private", "The sender wishes that this case be handled as confidential.")));
		}

		Layer clear = new Layer(Layer.DIV);
		clear.setStyleClass("Clear");

		SelectorUtility util = new SelectorUtility();
		DropdownMenu categories = (DropdownMenu) util.getSelectorFromIDOEntities(new DropdownMenu(PARAMETER_CASE_CATEGORY_PK), getCasesBusiness().getCaseCategories(), "getName");
		categories.setID("casesParentCategory");
		categories.keepStatusOnAction(true);
		categories.setSelectedElement(parentCategory != null ? parentCategory.getPrimaryKey().toString() : category.getPrimaryKey().toString());
		categories.setStyleClass("caseCategoryDropdown");

		DropdownMenu subCategories = new DropdownMenu(PARAMETER_SUB_CASE_CATEGORY_PK);
		subCategories.setID("casesSubCategory");
		subCategories.keepStatusOnAction(true);
		subCategories.setSelectedElement(category.getPrimaryKey().toString());
		subCategories.setStyleClass("subCaseCategoryDropdown");

		if (parentCategory != null) {
			@SuppressWarnings("unchecked")
			Collection<CaseCategory> collection = getCasesBusiness(iwc).getSubCategories(parentCategory);

			if (collection.isEmpty())
				subCategories.addMenuElement(category.getPrimaryKey().toString(), getResourceBundle().getLocalizedString("case_creator.no_sub_category", "no sub category"));
			else
				for (CaseCategory subCategory : collection)
					subCategories.addMenuElement(subCategory.getPrimaryKey().toString(), subCategory.getLocalizedCategoryName(iwc.getCurrentLocale()));
		}

		DropdownMenu types = (DropdownMenu) util.getSelectorFromIDOEntities(new DropdownMenu(PARAMETER_CASE_TYPE_PK), getCasesBusiness().getCaseTypes(), "getName");
		types.keepStatusOnAction(true);
		types.setSelectedElement(type.getPrimaryKey().toString());
		types.setStyleClass("caseTypeDropdown");

		HiddenInput hiddenType = new HiddenInput(PARAMETER_CASE_TYPE_PK, type.getPrimaryKey().toString());

		DropdownMenu statuses = new DropdownMenu(PARAMETER_STATUS);
		statuses.addMenuElement(getCasesBusiness().getCaseStatusPending().getStatus(), getCasesBusiness().getLocalizedCaseStatusDescription(theCase, getCasesBusiness().getCaseStatusPending(), iwc.getCurrentLocale()));
		statuses.addMenuElement(getCasesBusiness().getCaseStatusWaiting().getStatus(), getCasesBusiness().getLocalizedCaseStatusDescription(theCase, getCasesBusiness().getCaseStatusWaiting(), iwc.getCurrentLocale()));
		statuses.addMenuElement(getCasesBusiness().getCaseStatusReady().getStatus(), getCasesBusiness().getLocalizedCaseStatusDescription(theCase, getCasesBusiness().getCaseStatusReady(), iwc.getCurrentLocale()));
		statuses.setSelectedElement(theCase.getStatus());
		statuses.setStyleClass("caseStatusDropdown");

		Layer message = new Layer(Layer.SPAN);
		if (theCase.getMessage() != null) {
			message.add(new Text(TextSoap.formatText(theCase.getMessage())));
		}
		else {
			message.add(Text.getNonBrakingSpace());
		}

		Layer createdDate = new Layer(Layer.SPAN);
		createdDate.add(new Text(created.getLocaleDateAndTime(iwc.getCurrentLocale(), IWTimestamp.SHORT, IWTimestamp.SHORT)));

		TextArea reply = new TextArea(PARAMETER_REPLY);
		reply.setStyleClass("textarea");
		reply.keepStatusOnAction(true);

		Layer formItem = new Layer(Layer.DIV);
		formItem.setStyleClass("formItem");
		Label label = new Label();
		label.setLabel(getResourceBundle().getLocalizedString("case_nr", "Case nr"));
		formItem.add(label);
		formItem.add(new Span(new Text(theCase.getPrimaryKey().toString())));
		section.add(formItem);

		if (getCasesBusiness().useTypes()) {
			Layer element = new Layer(Layer.DIV);
			element.setStyleClass("formItem");
			label = new Label(getResourceBundle().getLocalizedString("case_type", "Case type"), types);
			element.add(label);
			element.add(types);
			section.add(element);
		}
		else {
			form.add(hiddenType);
		}

		Layer element = new Layer(Layer.DIV);
		element.setStyleClass("formItem");
		label = new Label(getResourceBundle().getLocalizedString("case_category", "Case category"), categories);
		element.add(label);
		element.add(categories);
		section.add(element);

		if (useSubCategories) {
			element = new Layer(Layer.DIV);
			element.setStyleClass("formItem");
			label = new Label(getResourceBundle().getLocalizedString("sub_case_category", "Sub case category"), subCategories);
			element.add(label);
			element.add(subCategories);
			section.add(element);
		}

		element = new Layer(Layer.DIV);
		element.setStyleClass("formItem");
		label = new Label();
		label.setLabel(getResourceBundle().getLocalizedString("created_date", "Created date"));
		element.add(label);
		element.add(createdDate);
		section.add(element);

		if (attachment != null) {
			Link link = new Link(new Text(attachment.getName()));
			link.setFile(attachment);
			link.setTarget(Link.TARGET_BLANK_WINDOW);

			Layer attachmentSpan = new Layer(Layer.SPAN);
			attachmentSpan.add(link);

			element = new Layer(Layer.DIV);
			element.setStyleClass("formItem");
			label = new Label();
			label.setLabel(getResourceBundle().getLocalizedString("attachment", "Attachment"));
			element.add(label);
			element.add(attachmentSpan);
			section.add(element);
		}

		if (theCase.getSubject() != null) {
			formItem = new Layer(Layer.DIV);
			formItem.setStyleClass("formItem");
			label = new Label();
			label.setLabel(getResourceBundle().getLocalizedString("regarding", "Regarding"));
			formItem.add(label);
			formItem.add(new Span(new Text(theCase.getSubject())));
			section.add(formItem);
		}

		element = new Layer(Layer.DIV);
		element.setStyleClass("formItem");
		element.setStyleClass("informationItem");
		label = new Label();
		label.setLabel(getResourceBundle().getLocalizedString(getPrefix() + "message", "Message"));
		element.add(label);
		element.add(message);
		section.add(element);

		section.add(clear);

		heading = new Heading1(getResourceBundle().getLocalizedString("handler_overview", "Handler overview"));
		heading.setStyleClass("subHeader");
		form.add(heading);

		section = new Layer(Layer.DIV);
		section.setStyleClass("formSection");
		form.add(section);

		element = new Layer(Layer.DIV);
		element.setStyleClass("formItem");
		label = new Label(getResourceBundle().getLocalizedString("status", "status"), statuses);
		element.add(label);
		element.add(statuses);
		section.add(element);

		element = new Layer(Layer.DIV);
		element.setStyleClass("formItem");
		label = new Label(getResourceBundle().getLocalizedString("reply", "Reply"), reply);
		element.add(label);
		element.add(reply);
		section.add(element);

		section.add(clear);

		@SuppressWarnings("unchecked")
		Collection<CaseLog> logs = getCasesBusiness(iwc).getCaseLogs(theCase);
		if (!logs.isEmpty()) {
			for (CaseLog log : logs) {
				form.add(getHandlerLayer(iwc, this.getResourceBundle(), theCase, log));
			}
		}

		Layer bottom = new Layer(Layer.DIV);
		bottom.setStyleClass("bottom");
		form.add(bottom);

		Link back = getButtonLink(getResourceBundle().getLocalizedString("back", "Back"));
		back.setStyleClass("homeButton");
		back.setValueOnClick(UserCases.PARAMETER_ACTION, String.valueOf(ACTION_VIEW));
		back.setToFormSubmit(form);
		bottom.add(back);

		Link pdf = getDownloadButtonLink(getResourceBundle().getLocalizedString("fetch_pdf", "Fetch PDF"), CaseWriter.class);
		pdf.addParameter(getCasesBusiness().getSelectedCaseParameter(), theCase.getPrimaryKey().toString());
		bottom.add(pdf);

		Link next = getButtonLink(getResourceBundle().getLocalizedString("process", "Process"));
		next.setValueOnClick(UserCases.PARAMETER_ACTION, String.valueOf(ACTION_SAVE));
		next.setToFormSubmit(form);
		bottom.add(next);

		add(form);
	}

	@Override
	protected void save(IWContext iwc) throws RemoteException {
		Object casePK = iwc.getParameter(PARAMETER_CASE_PK);
		Object caseCategoryPK = iwc.getParameter(PARAMETER_CASE_CATEGORY_PK);
		Object subCaseCategoryPK = iwc.getParameter(PARAMETER_SUB_CASE_CATEGORY_PK);
		Object caseTypePK = iwc.getParameter(PARAMETER_CASE_TYPE_PK);
		String status = iwc.getParameter(PARAMETER_STATUS);
		String reply = iwc.getParameter(PARAMETER_REPLY);

		try {
			getCasesBusiness().handleCase(casePK, subCaseCategoryPK != null ? subCaseCategoryPK : caseCategoryPK, caseTypePK, status, iwc.getCurrentUser(), reply, iwc);
		}
		catch (FinderException fe) {
			fe.printStackTrace();
		}
	}

	@Override
	protected void initializeTableSorter(IWContext iwc) throws RemoteException {
		StringBuilder buffer = new StringBuilder();
		buffer.append("jQuery(window).load(function() { jQuery('#").append(getBlockID()).append("').tablesorter( { headers: { ")
			.append((getCasesBusiness().useTypes() ? 3 : 2)).append(": { sorter: false }, ").append((getCasesBusiness().useTypes() ? 6 : 5))
			.append(": { sorter: false}").append((showCheckBox() ? ", " + (getCasesBusiness().useTypes() ? 7 : 6) + ": { sorter: false}" : ""))
			.append("}, sortList: [[0,0]] } ); } );");
		PresentationUtil.addJavaScriptActionToBody(iwc, buffer.toString());
	}
}
