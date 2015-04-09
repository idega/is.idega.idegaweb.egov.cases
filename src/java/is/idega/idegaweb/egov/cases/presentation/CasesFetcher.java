/*
 * Created on 9.5.2004
 */
package is.idega.idegaweb.egov.cases.presentation;

import is.idega.idegaweb.egov.cases.business.CasesWriter;
import is.idega.idegaweb.egov.cases.data.CaseCategory;
import is.idega.idegaweb.egov.cases.data.CaseType;
import is.idega.idegaweb.egov.cases.data.GeneralCase;
import is.idega.idegaweb.egov.cases.data.GeneralCaseHome;

import java.rmi.RemoteException;
import java.sql.Date;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import javax.ejb.FinderException;

import com.idega.block.process.data.Case;
import com.idega.block.process.data.CaseStatus;
import com.idega.block.web2.business.Web2Business;
import com.idega.business.IBORuntimeException;
import com.idega.core.builder.data.ICPage;
import com.idega.data.IDOLookup;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.Table2;
import com.idega.presentation.TableCell2;
import com.idega.presentation.TableRow;
import com.idega.presentation.TableRowGroup;
import com.idega.presentation.text.DownloadLink;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.HiddenInput;
import com.idega.presentation.ui.IWDatePicker;
import com.idega.presentation.ui.Label;
import com.idega.presentation.ui.SubmitButton;
import com.idega.presentation.ui.handlers.IWDatePickerHandler;
import com.idega.presentation.ui.util.SelectorUtility;
import com.idega.user.data.Group;
import com.idega.user.data.User;
import com.idega.util.CoreConstants;
import com.idega.util.IWTimestamp;
import com.idega.util.ListUtil;
import com.idega.util.PersonalIDFormatter;
import com.idega.util.PresentationUtil;
import com.idega.util.expression.ELUtil;
import com.idega.util.text.Name;

/**
 * @author laddi
 */
public class CasesFetcher extends CasesBlock {

	private static final String PARAMETER_CASE_CATEGORY = "prm_case_category";
	private static final String PARAMETER_SUB_CASE_CATEGORY = "prm_sub_case_category";
	private static final String PARAMETER_CASE_TYPE = "prm_case_type";
	private static final String PARAMETER_CASE_STATUS = "prm_instruments";
	private static final String PARAMETER_ANONYMOUS = "prm_anonymous";
	private static final String PARAMETER_SHOW_RESULTS = "prm_show_results";
	private static final String PARAMETER_FROM_DATE = "prm_from_date";
	private static final String PARAMETER_TO_DATE = "prm_to_date";

	private CaseCategory parentCategory;
	private CaseCategory category;
	private CaseType type;
	private CaseStatus status;
	private Boolean anonymous;
	private Date fromDate;
	private Date toDate;

	private Collection<Case> cases;

	private ICPage iPage;

	private void parse(IWContext iwc) throws RemoteException {
		if (iwc.isParameterSet(PARAMETER_CASE_CATEGORY)) {
			try {
				parentCategory = getCasesBusiness(iwc).getCaseCategory(iwc.getParameter(PARAMETER_CASE_CATEGORY));
			}
			catch (FinderException fe) {
				fe.printStackTrace();
			}
		}

		if (iwc.isParameterSet(PARAMETER_SUB_CASE_CATEGORY)) {
			try {
				category = getCasesBusiness(iwc).getCaseCategory(iwc.getParameter(PARAMETER_SUB_CASE_CATEGORY));
			}
			catch (FinderException fe) {
				fe.printStackTrace();
			}
		}

		if (iwc.isParameterSet(PARAMETER_CASE_TYPE)) {
			try {
				type = getCasesBusiness(iwc).getCaseType(iwc.getParameter(PARAMETER_CASE_TYPE));
			}
			catch (FinderException fe) {
				fe.printStackTrace();
			}
		}

		if (iwc.isParameterSet(PARAMETER_CASE_STATUS)) {
			status = getCasesBusiness(iwc).getCaseStatus(iwc.getParameter(PARAMETER_CASE_STATUS));
		}

		if (iwc.isParameterSet(PARAMETER_ANONYMOUS)) {
			anonymous = new Boolean(iwc.getParameter(PARAMETER_ANONYMOUS));
		}

		if (iwc.isParameterSet(PARAMETER_FROM_DATE)) {
			fromDate = new IWTimestamp(IWDatePickerHandler.getParsedDate(iwc.getParameter(PARAMETER_FROM_DATE), iwc.getCurrentLocale())).getDate();
		}

		if (iwc.isParameterSet(PARAMETER_TO_DATE)) {
			toDate = new IWTimestamp(IWDatePickerHandler.getParsedDate(iwc.getParameter(PARAMETER_TO_DATE), iwc.getCurrentLocale())).getDate();
		}

		if (iwc.isParameterSet(PARAMETER_SHOW_RESULTS)) {
			cases = getCasesBusiness(iwc).getCasesByCriteria(parentCategory, category, type, status, fromDate, toDate, anonymous);
			if (ListUtil.isEmpty(cases)) {
				getLogger().warning("Did not find cases by parameters: parent category: " + parentCategory + ", category: " + category + ", type: " + type + ", status: " + status
						+ ", from date: " + fromDate + ", to date: " +  toDate + ", anonymous: " + anonymous);
			} else {
				getLogger().info("Found cases: " + cases + " (total: " + cases.size() + ") by parameters: parent category: " + parentCategory + ", category: " + category + ", type: " + type + ", status: " + status
						+ ", from date: " + fromDate + ", to date: " +  toDate + ", anonymous: " + anonymous);
			}
		} else {
			getLogger().warning("No cases to show: parameter to show results is not set");
		}
	}

	@Override
	protected void present(IWContext iwc) {
		try {
			parse(iwc);

			Web2Business business = ELUtil.getInstance().getBean(Web2Business.class);
			PresentationUtil.addJavaScriptSourceLineToHeader(iwc, business.getBundleURIToJQueryLib());
			PresentationUtil.addJavaScriptSourceLineToHeader(iwc, CoreConstants.DWR_ENGINE_SCRIPT);
			PresentationUtil.addJavaScriptSourceLineToHeader(iwc, CoreConstants.DWR_UTIL_SCRIPT);
			PresentationUtil.addJavaScriptSourceLineToHeader(iwc, "/dwr/interface/CasesBusiness.js");
			PresentationUtil.addJavaScriptSourceLineToHeader(iwc, business.getBundleURIToScriptsFolder() + "tablesorter/jquery.metadata.js");
			PresentationUtil.addJavaScriptSourceLineToHeader(iwc, business.getBundleURIToScriptsFolder() + "tablesorter/jquery.tablesorter.min.js");
			PresentationUtil.addJavaScriptSourceLineToHeader(iwc, getBundle().getVirtualPathWithFileNameString("javascript/egov_cases.js"));
			PresentationUtil.addJavaScriptSourceLineToHeader(iwc, getBundle().getVirtualPathWithFileNameString("javascript/tablesorter.js"));

			Form form = new Form();
			form.setID("casesFetcher");
			form.setStyleClass("adminForm");
			form.addParameter(PARAMETER_SHOW_RESULTS, Boolean.TRUE.toString());

			HiddenInput localeInput = new HiddenInput("current_locale", iwc.getCurrentLocale().getCountry().toLowerCase());
			localeInput.setID("casesLocale");
			form.add(localeInput);

			Layer section = new Layer(Layer.DIV);
			section.setStyleClass("formSection");
			form.add(section);

			Layer helpLayer = new Layer(Layer.DIV);
			helpLayer.setStyleClass("helperText");
			helpLayer.add(new Text(getResourceBundle().getLocalizedString("cases_fetcher.help_text", "Select your criteria and click 'fetch'.  The results will be displayed below.")));
			section.add(helpLayer);

			SelectorUtility util = new SelectorUtility();
			DropdownMenu categories = (DropdownMenu) util.getSelectorFromIDOEntities(new DropdownMenu(PARAMETER_CASE_CATEGORY), getCasesBusiness().getCaseCategories(), "getName");
			categories.setID("casesParentCategory");
			categories.keepStatusOnAction(true);
			categories.setStyleClass("caseCategoryDropdown");
			categories.setMenuElementFirst("", "");

			DropdownMenu subCategories = new DropdownMenu(PARAMETER_SUB_CASE_CATEGORY);
			subCategories.setID("casesSubCategory");
			subCategories.keepStatusOnAction(true);
			subCategories.setStyleClass("subCaseCategoryDropdown");

			if (parentCategory != null) {
				Collection<CaseCategory> collection = getCasesBusiness(iwc).getSubCategories(parentCategory);
				if (ListUtil.isEmpty(collection)) {
					subCategories.addMenuElement(parentCategory.getPrimaryKey().toString(), getResourceBundle().getLocalizedString("case_creator.no_sub_category", "no sub category"));
				}
				else {
					for (Iterator<CaseCategory> iter = collection.iterator(); iter.hasNext();) {
						CaseCategory subCategory = iter.next();
						subCategories.addMenuElement(subCategory.getPrimaryKey().toString(), subCategory.getLocalizedCategoryName(iwc.getCurrentLocale()));
					}
					subCategories.setMenuElementFirst("", getResourceBundle().getLocalizedString("case_creator.select_sub_category", "Select sub category"));
				}
			}

			DropdownMenu types = (DropdownMenu) util.getSelectorFromIDOEntities(new DropdownMenu(PARAMETER_CASE_TYPE), getCasesBusiness().getCaseTypes(), "getName");
			types.keepStatusOnAction(true);
			types.setStyleClass("caseTypeDropdown");
			types.setMenuElementFirst("", "");

			DropdownMenu statuses = new DropdownMenu(PARAMETER_CASE_STATUS);
			statuses.addMenuElement(getCasesBusiness().getCaseStatusOpen().getStatus(), getCasesBusiness().getLocalizedCaseStatusDescription(getCasesBusiness().getCaseStatusOpen(), iwc.getCurrentLocale()));
			statuses.addMenuElement(getCasesBusiness().getCaseStatusPending().getStatus(), getCasesBusiness().getLocalizedCaseStatusDescription(getCasesBusiness().getCaseStatusPending(), iwc.getCurrentLocale()));
			statuses.addMenuElement(getCasesBusiness().getCaseStatusWaiting().getStatus(), getCasesBusiness().getLocalizedCaseStatusDescription(getCasesBusiness().getCaseStatusWaiting(), iwc.getCurrentLocale()));
			statuses.addMenuElement(getCasesBusiness().getCaseStatusReady().getStatus(), getCasesBusiness().getLocalizedCaseStatusDescription(getCasesBusiness().getCaseStatusReady(), iwc.getCurrentLocale()));
			statuses.keepStatusOnAction(true);
			statuses.setStyleClass("caseStatusDropdown");
			statuses.setMenuElementFirst("", "");

			DropdownMenu anonymous = new DropdownMenu(PARAMETER_ANONYMOUS);
			anonymous.addMenuElement("", getResourceBundle().getLocalizedString("cases_fetcher.both", "Both"));
			anonymous.addMenuElement(Boolean.TRUE.toString(), getResourceBundle().getLocalizedString("cases_fetcher.yes", "Yes"));
			anonymous.addMenuElement(Boolean.FALSE.toString(), getResourceBundle().getLocalizedString("cases_fetcher.no", "No"));
			anonymous.keepStatusOnAction(true);
			anonymous.setStyleClass("anonymousDropdown");

			IWDatePicker from = new IWDatePicker(PARAMETER_FROM_DATE);
			from.setVersion(IWDatePicker.VERSION_1_8_17);
			from.setUseCurrentDateIfNotSet(false);
			from.keepStatusOnAction(true);

			IWDatePicker to = new IWDatePicker(PARAMETER_TO_DATE);
			to.setVersion(IWDatePicker.VERSION_1_8_17);
			to.setUseCurrentDateIfNotSet(false);
			to.keepStatusOnAction(true);

			Layer element = new Layer(Layer.DIV);
			element.setStyleClass("formItem");
			Label label = new Label(getResourceBundle().getLocalizedString("case_category", "Case category"), categories);
			element.add(label);
			element.add(categories);
			section.add(element);

			if (getCasesBusiness().useSubCategories()) {
				element = new Layer(Layer.DIV);
				element.setStyleClass("formItem");
				label = new Label(getResourceBundle().getLocalizedString("sub_case_category", "Sub case category"), subCategories);
				element.add(label);
				element.add(subCategories);
				section.add(element);
			}

			if (getCasesBusiness().useTypes()) {
				element = new Layer(Layer.DIV);
				element.setStyleClass("formItem");
				label = new Label(getResourceBundle().getLocalizedString("case_type", "Case type"), types);
				element.add(label);
				element.add(types);
				section.add(element);
			}

			element = new Layer(Layer.DIV);
			element.setStyleClass("formItem");
			label = new Label(getResourceBundle().getLocalizedString("status", "status"), statuses);
			element.add(label);
			element.add(statuses);
			section.add(element);

			element = new Layer(Layer.DIV);
			element.setStyleClass("formItem");
			label = new Label(getResourceBundle().getLocalizedString("cases_fetcher.show_anonymous", "Show anonymous"), anonymous);
			element.add(label);
			element.add(anonymous);
			section.add(element);

			element = new Layer(Layer.DIV);
			element.setStyleClass("formItem");
			label = new Label(getResourceBundle().getLocalizedString("cases_fetcher.from_date", "From date"), from);
			element.add(label);
			element.add(from);
			section.add(element);

			element = new Layer(Layer.DIV);
			element.setStyleClass("formItem");
			label = new Label(getResourceBundle().getLocalizedString("cases_fetcher.to_date", "To date"), to);
			element.add(label);
			element.add(to);
			section.add(element);

			SubmitButton fetch = new SubmitButton(getResourceBundle().getLocalizedString("cases_fetcher.fetch", "Fetch"));
			fetch.setStyleClass("indentedButton");
			fetch.setStyleClass("button");
			element = new Layer(Layer.DIV);
			element.setStyleClass("formItem");
			element.add(fetch);
			section.add(element);

			Layer clearLayer = new Layer(Layer.DIV);
			clearLayer.setStyleClass("Clear");
			section.add(clearLayer);

			if (cases != null) {
				getLogger().info("Have to print cases: " + cases);
				form.add(getPrintouts(iwc));
				form.add(getFileTable(iwc));
			} else {
				getLogger().warning("No printing cases");
			}

			add(form);
		}
		catch (RemoteException re) {
			throw new IBORuntimeException(re);
		}
	}

	private Table2 getFileTable(IWContext iwc) throws RemoteException {
		Table2 table = new Table2();
		table.setWidth("100%");
		table.setCellpadding(0);
		table.setCellspacing(0);
		table.setStyleClass("ruler");
		table.setStyleClass("adminTable");
//		table.setID("casesFetcher");

		TableRowGroup group = table.createHeaderRowGroup();
		TableRow row = group.createRow();
		TableCell2 cell = row.createHeaderCell();
		cell.setStyleClass("firstColumn");
		cell.setStyleClass("caseID");
		cell.add(new Text(getResourceBundle().getLocalizedString("cases_fetcher.case_id", "Case ID")));

		cell = row.createHeaderCell();
		cell.setStyleClass("createdDate");
		cell.add(new Text(getResourceBundle().getLocalizedString("created_date", "Created date")));

		cell = row.createHeaderCell();
		cell.setStyleClass("name");
		cell.add(new Text(getResourceBundle().getLocalizedString("name", "Name")));

		cell = row.createHeaderCell();
		cell.setStyleClass("personalID");
		cell.add(new Text(getResourceBundle().getLocalizedString("personal_id", "Personal ID")));

		cell = row.createHeaderCell();
		cell.setStyleClass("category");
		cell.add(new Text(getResourceBundle().getLocalizedString("case_category", "Category")));

		if (getCasesBusiness(iwc).useTypes()) {
			cell = row.createHeaderCell();
			cell.setStyleClass("type");
			cell.add(new Text(getResourceBundle().getLocalizedString("case_type", "Type")));
		}

		cell = row.createHeaderCell();
		cell.setStyleClass("status");
		if (getResponsePage() == null) {
			cell.setStyleClass("lastColumn");
		}
		cell.add(new Text(getResourceBundle().getLocalizedString("status", "Status")));

		if (getResponsePage() != null) {
			cell = row.createHeaderCell();
			cell.setStyleClass("view");
			cell.setStyleClass("lastColumn");
			cell.add(Text.getNonBrakingSpace());
		}

		group = table.createBodyRowGroup();
		int iRow = 1;

		User currentUser = iwc.isLoggedOn() ? iwc.getCurrentUser() : null;
		if (currentUser == null) {
			getLogger().warning("User must be logged on");
			return table;
		}

		boolean superAdmin = iwc.isSuperAdmin();

		if (ListUtil.isEmpty(cases)) {
			getLogger().info("No cases to show");
			return table;
		}

		GeneralCaseHome generalCaseHome = (GeneralCaseHome) IDOLookup.getHome(GeneralCase.class);
		for (Iterator<Case> iter = cases.iterator(); iter.hasNext();) {
			Case object = iter.next();
			if (object == null) {
				continue;
			}

			GeneralCase theCase = null;
			if (object instanceof GeneralCase) {
				theCase = (GeneralCase) object;
			} else {
				try {
					theCase = generalCaseHome.findByPrimaryKey(object.getId());
				} catch (FinderException e) {}
			}

			if (theCase == null) {
				getLogger().warning("Failed to resolve general case by ID: " + object.getId());
				continue;
			}

			CaseCategory category = theCase.getCaseCategory();
			if (!superAdmin && category != null) {
				Group handlerGroup = category.getHandlerGroup();
				if (handlerGroup != null && !currentUser.hasRelationTo(handlerGroup)) {
					getLogger().warning(currentUser + " (ID: " + currentUser.getId() + ") can not see case (ID: " + theCase.getId() + ") from category " + category.getName() + " (ID: " + category.getPrimaryKey() + ")");
					continue;
				}
			}

			CaseType type = theCase.getCaseType();

			CaseStatus status = theCase.getCaseStatus();
			if (status.equals(getCasesBusiness().getCaseStatusDeleted())) {
				getLogger().info("Case (ID: " + theCase.getId() + ") is deleted, skipping it");
				continue;
			}

			User user = theCase.getOwner();
			IWTimestamp created = new IWTimestamp(theCase.getCreated());
			row = group.createRow();

			if (iRow % 2 == 0) {
				row.setStyleClass("evenRow");
			}
			else {
				row.setStyleClass("oddRow");
			}

			cell = row.createCell();
			cell.setStyleClass("firstColumn");
			cell.setStyleClass("caseID");
			if (getResponsePage() != null) {
				Link link = new Link(new Text(theCase.getPrimaryKey().toString()));
				link.addParameter(getCasesBusiness(iwc).getSelectedCaseParameter(), theCase.getPrimaryKey().toString());
				link.setPage(getResponsePage());
				cell.add(link);
			}
			else {
				cell.add(new Text(theCase.getPrimaryKey().toString()));
			}

			cell = row.createCell();
			cell.setStyleClass("createdDate");
			cell.add(new Text(created.getLocaleDateAndTime(iwc.getCurrentLocale(), IWTimestamp.SHORT, IWTimestamp.SHORT)));

			if (user != null) {
				Name name = new Name(user.getFirstName(), user.getMiddleName(), user.getLastName());
				cell = row.createCell();
				cell.setStyleClass("name");
				cell.add(new Text(name.getName(iwc.getCurrentLocale())));

				cell = row.createCell();
				cell.setStyleClass("personalID");
				cell.add(new Text(PersonalIDFormatter.format(user.getPersonalID(), iwc.getCurrentLocale())));
			}
			else {
				cell = row.createCell();
				cell.setStyleClass("name");
				cell.add(Text.getNonBrakingSpace());

				cell = row.createCell();
				cell.setStyleClass("personalID");
				cell.add(Text.getNonBrakingSpace());
			}

			cell = row.createCell();
			cell.setStyleClass("category");
			if (category != null) {
				cell.add(new Text(category.getLocalizedCategoryName(iwc.getCurrentLocale())));
			}
			else {
				cell.add(Text.getNonBrakingSpace());
			}

			if (getCasesBusiness(iwc).useTypes()) {
				cell = row.createCell();
				cell.setStyleClass("type");
				cell.add(new Text(type.getName()));
			}

			cell = row.createCell();
			cell.setStyleClass("status");
			if (getResponsePage() == null) {
				cell.setStyleClass("lastColumn");
			}
			cell.add(new Text(getCasesBusiness(iwc).getLocalizedCaseStatusDescription(theCase, status, iwc.getCurrentLocale())));

			if (getResponsePage() != null) {
				Link view = new Link(getBundle().getImage("edit.png", getResourceBundle().getLocalizedString("view_case", "View case")));
				view.addParameter(getCasesBusiness(iwc).getSelectedCaseParameter(), theCase.getPrimaryKey().toString());
				view.setPage(getResponsePage());

				cell = row.createCell();
				cell.setStyleClass("view");
				cell.setStyleClass("lastColumn");
				cell.add(view);
			}

			iRow++;
		}

		return table;
	}

	public Layer getPrintouts(IWContext iwc) throws RemoteException {
		Layer layer = new Layer(Layer.DIV);
		layer.setStyleClass("printIcons");

		DownloadLink link = new DownloadLink(getBundle().getImage("xls.gif"));
		link.setStyleClass("xls");
		link.setTarget(Link.TARGET_NEW_WINDOW);
		link.setMediaWriterClass(CasesWriter.class);
		if (parentCategory != null) {
			link.addParameter(CasesWriter.PARAMETER_CASE_CATEGORY, parentCategory.getPrimaryKey().toString());
		}
		if (category != null) {
			link.addParameter(CasesWriter.PARAMETER_SUB_CASE_CATEGORY, category.getPrimaryKey().toString());
		}
		if (type != null) {
			link.addParameter(CasesWriter.PARAMETER_CASE_TYPE, type.getPrimaryKey().toString());
		}
		if (status != null) {
			link.addParameter(CasesWriter.PARAMETER_CASE_STATUS, status.getPrimaryKey().toString());
		}
		if (fromDate != null) {
			link.addParameter(CasesWriter.PARAMETER_FROM_DATE, fromDate.toString());
		}
		if (toDate != null) {
			link.addParameter(CasesWriter.PARAMETER_TO_DATE, toDate.toString());
		}
		if (anonymous != null) {
			link.addParameter(CasesWriter.PARAMETER_ANONYMOUS, anonymous.toString());
		}

		layer.add(link);

		return layer;
	}

	public void setResponsePage(ICPage page) {
		this.iPage = page;
	}

	private ICPage getResponsePage() {
		return this.iPage;
	}

	@Override
	public String getCasesProcessorType() {
		return null;
	}

	@Override
	public Map<Object, Object> getUserCasesPageMap() {
		return null;
	}

	@Override
	public boolean showCheckBox() {
		return false;
	}

	@Override
	public boolean showCheckBoxes() {
		return false;
	}
}