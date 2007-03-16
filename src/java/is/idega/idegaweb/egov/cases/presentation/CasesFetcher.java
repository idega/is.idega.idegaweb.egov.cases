/*
 * Created on 9.5.2004
 */
package is.idega.idegaweb.egov.cases.presentation;

import is.idega.idegaweb.egov.cases.business.CaseCategoryCollectionHandler;
import is.idega.idegaweb.egov.cases.business.CasesWriter;
import is.idega.idegaweb.egov.cases.data.CaseCategory;
import is.idega.idegaweb.egov.cases.data.CaseType;
import is.idega.idegaweb.egov.cases.data.GeneralCase;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Iterator;

import javax.ejb.FinderException;

import com.idega.block.process.data.CaseStatus;
import com.idega.business.IBORuntimeException;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.Table2;
import com.idega.presentation.TableCell2;
import com.idega.presentation.TableRow;
import com.idega.presentation.TableRowGroup;
import com.idega.presentation.remotescripting.RemoteScriptHandler;
import com.idega.presentation.text.DownloadLink;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.Label;
import com.idega.presentation.ui.SubmitButton;
import com.idega.presentation.ui.util.SelectorUtility;
import com.idega.user.data.User;
import com.idega.util.IWTimestamp;
import com.idega.util.PersonalIDFormatter;
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

	private CaseCategory parentCategory;
	private CaseCategory category;
	private CaseType type;
	private CaseStatus status;
	private Boolean anonymous;

	private Collection cases;

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
				category = getCasesBusiness(iwc).getCaseCategory(iwc.getParameter(PARAMETER_CASE_CATEGORY));
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

		if (iwc.isParameterSet(PARAMETER_SHOW_RESULTS)) {
			cases = getCasesBusiness(iwc).getCasesByCriteria(parentCategory, category, type, status, anonymous);
		}
	}

	protected void present(IWContext iwc) {
		try {
			parse(iwc);

			Form form = new Form();
			form.setID("casesFetcher");
			form.setStyleClass("adminForm");
			form.addParameter(PARAMETER_SHOW_RESULTS, Boolean.TRUE.toString());

			Layer section = new Layer(Layer.DIV);
			section.setStyleClass("formSection");
			form.add(section);

			Layer helpLayer = new Layer(Layer.DIV);
			helpLayer.setStyleClass("helperText");
			helpLayer.add(new Text(getResourceBundle().getLocalizedString("cases_fetcher.help_text", "Select your criteria and click 'fetch'.  The results will be displayed below.")));
			section.add(helpLayer);

			SelectorUtility util = new SelectorUtility();
			DropdownMenu categories = (DropdownMenu) util.getSelectorFromIDOEntities(new DropdownMenu(PARAMETER_CASE_CATEGORY), getBusiness().getCaseCategories(), "getName");
			categories.keepStatusOnAction(true);
			categories.setStyleClass("caseCategoryDropdown");
			categories.setMenuElementFirst("", "");

			DropdownMenu subCategories = new DropdownMenu(PARAMETER_SUB_CASE_CATEGORY);
			subCategories.keepStatusOnAction(true);
			subCategories.setStyleClass("subCaseCategoryDropdown");

			if (parentCategory != null) {
				Collection collection = getCasesBusiness(iwc).getSubCategories(parentCategory);
				if (collection.isEmpty()) {
					subCategories.addMenuElement(category.getPrimaryKey().toString(), getResourceBundle().getLocalizedString("case_creator.no_sub_category", "no sub category"));
				}
				else {
					Iterator iter = collection.iterator();
					while (iter.hasNext()) {
						CaseCategory subCategory = (CaseCategory) iter.next();
						subCategories.addMenuElement(subCategory.getPrimaryKey().toString(), subCategory.getLocalizedCategoryName(iwc.getCurrentLocale()));
					}
				}
				subCategories.setMenuElementFirst("", "");
			}

			DropdownMenu types = (DropdownMenu) util.getSelectorFromIDOEntities(new DropdownMenu(PARAMETER_CASE_TYPE), getBusiness().getCaseTypes(), "getName");
			types.keepStatusOnAction(true);
			types.setStyleClass("caseTypeDropdown");
			types.setMenuElementFirst("", "");

			DropdownMenu statuses = new DropdownMenu(PARAMETER_CASE_STATUS);
			statuses.addMenuElement(getBusiness().getCaseStatusOpen().getStatus(), getBusiness().getLocalizedCaseStatusDescription(getBusiness().getCaseStatusOpen(), iwc.getCurrentLocale()));
			statuses.addMenuElement(getBusiness().getCaseStatusPending().getStatus(), getBusiness().getLocalizedCaseStatusDescription(getBusiness().getCaseStatusPending(), iwc.getCurrentLocale()));
			statuses.addMenuElement(getBusiness().getCaseStatusWaiting().getStatus(), getBusiness().getLocalizedCaseStatusDescription(getBusiness().getCaseStatusWaiting(), iwc.getCurrentLocale()));
			statuses.addMenuElement(getBusiness().getCaseStatusReady().getStatus(), getBusiness().getLocalizedCaseStatusDescription(getBusiness().getCaseStatusReady(), iwc.getCurrentLocale()));
			statuses.keepStatusOnAction(true);
			statuses.setStyleClass("caseStatusDropdown");
			statuses.setMenuElementFirst("", "");

			DropdownMenu anonymous = new DropdownMenu(PARAMETER_ANONYMOUS);
			anonymous.addMenuElement("", "");
			anonymous.addMenuElement(Boolean.TRUE.toString(), getResourceBundle().getLocalizedString("cases_fetcher.yes", "Yes"));
			anonymous.addMenuElement(Boolean.TRUE.toString(), getResourceBundle().getLocalizedString("cases_fetcher.no", "No"));
			anonymous.keepStatusOnAction(true);
			anonymous.setStyleClass("anonymousDropdown");

			Layer element = new Layer(Layer.DIV);
			element.setStyleClass("formItem");
			Label label = new Label(getResourceBundle().getLocalizedString("case_category", "Case category"), categories);
			element.add(label);
			element.add(categories);
			section.add(element);

			if (getBusiness().useSubCategories()) {
				try {
					RemoteScriptHandler rsh = new RemoteScriptHandler(categories, subCategories);
					rsh.setRemoteScriptCollectionClass(CaseCategoryCollectionHandler.class);
					element.add(rsh);
				}
				catch (IllegalAccessException iae) {
					iae.printStackTrace();
				}
				catch (InstantiationException ie) {
					ie.printStackTrace();
				}

				element = new Layer(Layer.DIV);
				element.setStyleClass("formItem");
				label = new Label(getResourceBundle().getLocalizedString("sub_case_category", "Sub case category"), subCategories);
				element.add(label);
				element.add(subCategories);
				section.add(element);
			}

			if (getBusiness().useTypes()) {
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

			if (cases != null && cases.size() > 0) {
				form.add(getPrintouts(iwc));
				form.add(getFileTable(iwc));
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
		cell.setStyleClass("lastColumn");
		cell.add(new Text(getResourceBundle().getLocalizedString("status", "Status")));

		group = table.createBodyRowGroup();
		int iRow = 1;

		Iterator iter = cases.iterator();
		while (iter.hasNext()) {
			GeneralCase theCase = (GeneralCase) iter.next();
			CaseCategory category = theCase.getCaseCategory();
			CaseType type = theCase.getCaseType();
			CaseStatus status = theCase.getCaseStatus();
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
			cell.add(new Text(theCase.getPrimaryKey().toString()));

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
			cell.add(new Text(category.getLocalizedCategoryName(iwc.getCurrentLocale())));

			if (getCasesBusiness(iwc).useTypes()) {
				cell = row.createCell();
				cell.setStyleClass("type");
				cell.add(new Text(type.getName()));
			}

			cell = row.createCell();
			cell.setStyleClass("status");
			cell.setStyleClass("lastColumn");
			cell.add(new Text(getCasesBusiness(iwc).getLocalizedCaseStatusDescription(theCase, status, iwc.getCurrentLocale())));

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
		if (anonymous != null) {
			link.addParameter(CasesWriter.PARAMETER_ANONYMOUS, anonymous.toString());
		}

		layer.add(link);

		return layer;
	}
}