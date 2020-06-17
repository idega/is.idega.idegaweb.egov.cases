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

import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.FinderException;

import org.springframework.beans.factory.annotation.Autowired;

import com.idega.block.process.business.CaseManagersProvider;
import com.idega.block.process.business.CasesRetrievalManager;
import com.idega.block.process.business.ProcessConstants;
import com.idega.block.process.data.Case;
import com.idega.block.process.data.CaseCode;
import com.idega.block.process.data.CaseStatus;
import com.idega.block.process.data.CaseStatusHome;
import com.idega.block.process.presentation.beans.CasePresentation;
import com.idega.business.IBORuntimeException;
import com.idega.core.builder.data.ICPage;
import com.idega.core.builder.data.ICPageHome;
import com.idega.data.IDOLookup;
import com.idega.data.IDOLookupException;
import com.idega.idegaweb.IWMainApplication;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.CSSSpacer;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.Table2;
import com.idega.presentation.TableCell2;
import com.idega.presentation.TableColumn;
import com.idega.presentation.TableColumnGroup;
import com.idega.presentation.TableRow;
import com.idega.presentation.TableRowGroup;
import com.idega.presentation.text.Heading1;
import com.idega.presentation.text.Heading2;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.IWDatePicker;
import com.idega.presentation.ui.Label;
import com.idega.presentation.ui.SubmitButton;
import com.idega.presentation.ui.handlers.IWDatePickerHandler;
import com.idega.user.data.User;
import com.idega.util.CoreConstants;
import com.idega.util.CoreUtil;
import com.idega.util.IWTimestamp;
import com.idega.util.ListUtil;
import com.idega.util.StringUtil;
import com.idega.util.datastructures.map.MapUtil;
import com.idega.util.expression.ELUtil;
import com.idega.webface.WFUtil;

import is.idega.idegaweb.egov.application.data.Application;
import is.idega.idegaweb.egov.application.data.ApplicationHome;
import is.idega.idegaweb.egov.cases.business.CasesBusiness;
import is.idega.idegaweb.egov.cases.data.CaseCategory;
import is.idega.idegaweb.egov.cases.data.CaseType;
import is.idega.idegaweb.egov.cases.util.CasesConstants;


public class CasesStatistics extends CasesBlock {

	private static final Logger LOGGER = Logger.getLogger(CasesStatistics.class.getName());

	private static final String PARAMETER_FROM_DATE = "prm_from_date";
	private static final String PARAMETER_TO_DATE = "prm_to_date";

	public static final String UNKOWN_CATEGORY_ID = "case.unkown_category";

	private String visibleStatuses = null;

	private Boolean useStatisticsByCaseType,
					showDateRange = Boolean.TRUE;

	private Collection<CasePresentation> cases;

	private List<Integer> customCategories;
	private List<String> namesOfCustomCategories = Collections.unmodifiableList(Arrays.asList("BPM"));	//	TODO: use constant

	private List<String> availableStatuses;

	private String casesCriteria;
	private String categoriesCriteria;
	private String statusesCriteria;

	@Autowired(required = true)
	private CaseManagersProvider caseManagersProvider;

	@Override
	public void main(IWContext iwc) throws Exception {
		try {
			ELUtil.getInstance().autowire(this);
		} catch(Exception e) {
			LOGGER.log(Level.WARNING, "Unable to autowire Spring bean: " + CaseManagersProvider.class.getName() + ". Will skip BPM cases in statistics");
		}

		super.main(iwc);
	}

	@Override
	protected void present(IWContext iwc) throws Exception {
		boolean useSubCats = super.getCasesBusiness(iwc).useSubCategories();
		boolean useTypes = super.getCasesBusiness(iwc).useTypes();

		IWResourceBundle iwrb = getResourceBundle(iwc);
		Collection<CaseStatus> statuses = null;
		if (visibleStatuses == null) {
			if (ListUtil.isEmpty(cases)) {
				statuses = getCasesBusiness().getCaseStatuses();
			} else {
				//	Will be used statuses provided by cases
				statuses = new ArrayList<CaseStatus>();
				CaseStatus status = null;
				for (CasePresentation theCase: cases) {
					status = theCase.getCaseStatus();
					if (status != null && !statuses.contains(status)) {
						statuses.add(status);
					}
				}
			}
		} else {
			statuses = new ArrayList<CaseStatus>();
			StringTokenizer tok = new StringTokenizer(visibleStatuses, CoreConstants.COMMA);
			while (tok.hasMoreTokens()) {
				String status = tok.nextToken().trim();
				try {
					CaseStatus cStat = getCaseStatusHome().findByPrimaryKey(status);
					statuses.add(cStat);
				} catch (FinderException f) {
					f.printStackTrace();
				}
			}
		}

		String localizedStatus = null;
		Collection<CaseStatus> statusesToUse = new ArrayList<CaseStatus>();
		if (!ListUtil.isEmpty(statuses)) {
			for (CaseStatus status: statuses) {
				if (!statusesToUse.contains(status)) {
					localizedStatus = getCasesBusiness().getLocalizedCaseStatusDescription(null, status, iwc.getCurrentLocale());

					if (!StringUtil.isEmpty(localizedStatus) && !localizedStatus.equals(status.getStatus())) {
						statusesToUse.add(status);
					}
				}
			}
		}

		Form form = new Form();
		form.setID("casesStatistics");
		form.setStyleClass("adminForm");
		add(form);
		boolean addDateIntervalChooser = isShowDateRange();
		if (addDateIntervalChooser) {
			if (CoreUtil.isSingleComponentRenderingProcess(iwc)) {
				try {
					int pageId = iwc.getCurrentIBPageID();
					ICPageHome pageHome = (ICPageHome) IDOLookup.getHome(ICPage.class);
					ICPage page = pageHome.findByPrimaryKey(pageId);
					form.setAction(CoreConstants.PAGES_URI_PREFIX.concat(page.getDefaultPageURI()));
				} catch (Exception e) {
					e.printStackTrace();
					addDateIntervalChooser = false;
				}
			}
		}

		Layer section = new Layer(Layer.DIV);
		section.setStyleClass("formSection");
		form.add(section);

		IWDatePicker from = new IWDatePicker(PARAMETER_FROM_DATE);
		from.setVersion(IWDatePicker.VERSION_1_8_17);
		from.setUseCurrentDateIfNotSet(false);
		from.setShowMonthChange(true);
		from.setShowYearChange(true);
		from.keepStatusOnAction(true);

		IWDatePicker to = new IWDatePicker(PARAMETER_TO_DATE);
		to.setVersion(IWDatePicker.VERSION_1_8_17);
		to.setUseCurrentDateIfNotSet(false);
		to.setShowMonthChange(true);
		to.setShowYearChange(true);
		to.keepStatusOnAction(true);

		if (addDateIntervalChooser) {
			Layer element = new Layer(Layer.DIV);
			element.setStyleClass("formItem");
			Label label = new Label(getResourceBundle().getLocalizedString("cases_fetcher.from_date", "From date"), from);
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
		}

		section = new Layer(Layer.DIV);
		section.setStyleClass("formSection");
		section.setStyleClass("statisticsLayer");
		form.add(section);

		Heading1 heading = new Heading1(iwrb.getLocalizedString("case.statistics", "Case statistics"));
		section.add(heading);

		IWTimestamp fromDate = null;
		if (iwc.isParameterSet(PARAMETER_FROM_DATE)) {
			fromDate = new IWTimestamp(IWDatePickerHandler.getParsedDate(iwc.getParameter(PARAMETER_FROM_DATE), iwc.getCurrentLocale()));
		}

		IWTimestamp toDate = null;
		if (iwc.isParameterSet(PARAMETER_TO_DATE)) {
			toDate = new IWTimestamp(IWDatePickerHandler.getParsedDate(iwc.getParameter(PARAMETER_TO_DATE), iwc.getCurrentLocale()));
		}

		Collection<Result> resultsByCaseCategories = getResults(iwc, useSubCats, -1, true, fromDate, toDate);
		addResults(null, null, null, iwc, iwrb, section, resultsByCaseCategories, statusesToUse, iwrb.getLocalizedString("case.cases_by_category",
				"Cases by category"), useSubCats, false, 0, fromDate, toDate);
		section.add(new CSSSpacer());

		Collection<Result> resultsByUsers = getResultsUsers(iwc, fromDate, toDate);
		addResults(null, null, null, iwc, iwrb, section, resultsByUsers, statusesToUse, iwrb.getLocalizedString("case.cases_by_handler", "Cases by handler"),
				false, false, 0, fromDate, toDate);
		section.add(new CSSSpacer());

		if (useStatisticsByCaseType != null) {
			useTypes = useStatisticsByCaseType;
		}
		if (useTypes) {
			Collection<Result> resultsByCaseTypes = getResultsCode(iwc, fromDate, toDate);
			addResults(null, null, null, iwc, iwrb, section, resultsByCaseTypes, statusesToUse, iwrb.getLocalizedString("case.cases_by_type", "Cases by type"),
					false, false, 0, fromDate, toDate);
			section.add(new CSSSpacer());
		}
	}

	private int addResults(Map<CaseStatus, Integer> totals, Table2 table, TableRowGroup group, IWContext iwc, IWResourceBundle iwrb, Layer section,
			Collection<Result> results, Collection<CaseStatus> statuses, String header, boolean useSubCats, boolean isSubCategory, int iRow, IWTimestamp fromDate, IWTimestamp toDate) {
		if (totals == null) {
			totals = new LinkedHashMap<CaseStatus, Integer>();

			for (Iterator<CaseStatus> statIter = statuses.iterator(); statIter.hasNext();) {
				CaseStatus status = statIter.next();
				totals.put(status, 0);
			}
		}

		if (table == null) {
			Heading2 heading2 = new Heading2(header);
			section.add(heading2);

			table = new Table2();
			table.setWidth("100%");
			table.setCellpadding(0);
			table.setCellspacing(0);
			table.setStyleClass("adminTable");
			table.setStyleClass("ruler");
			section.add(table);

			TableColumnGroup columnGroup = table.createColumnGroup();
			TableColumn column = columnGroup.createColumn();
			column.setSpan(3);
			column = columnGroup.createColumn();
			column.setSpan(2);
			column.setWidth("12");

			group = table.createHeaderRowGroup();
			TableRow row = group.createRow();
			TableCell2 cell = row.createHeaderCell();
			cell.setStyleClass("firstColumn");
			cell.setStyleClass("name");
			cell.add(new Text(getResourceBundle().getLocalizedString("name", "Name")));

			CaseStatus status;
			for (Iterator<CaseStatus> statIter = statuses.iterator(); statIter.hasNext();) {
				status = statIter.next();
				cell = row.createHeaderCell();
				cell.setStyleClass(status.getStatus());
				String statusLabel = null;
				try {
					statusLabel = getCasesBusiness().getLocalizedCaseStatusDescription(null, status, iwc.getCurrentLocale());
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				cell.add(new Text(StringUtil.isEmpty(statusLabel) ? status.getStatus() : statusLabel));
			}

			cell = row.createHeaderCell();
			cell.setStyleClass("total lastColumn");
			cell.add(new Text(getResourceBundle().getLocalizedString("total", "Total")));

			group = table.createBodyRowGroup();
		}

		if (!ListUtil.isEmpty(results)) {
			for (Iterator<Result> iter = results.iterator(); iter.hasNext();) {
				++iRow;
				Result res = iter.next();
				boolean hasSubCats = false;
				Collection<Result> subCats = null;
				if (useSubCats) {
					subCats = getResults(iwc, true, res.getID(), res.isUseDefaultHandlerIfNotFoundResultsProvider(), fromDate, toDate);
					hasSubCats = !ListUtil.isEmpty(subCats);
				}
				addResultToTable(totals, statuses, group, iRow, res, isSubCategory, !hasSubCats);
				if (hasSubCats) {
					iRow = addResults(totals, table, group, iwc, iwrb, section, subCats, statuses, header, useSubCats, true, iRow, fromDate, toDate);
				}
			}
		}

		if (!isSubCategory) {
			group = table.createFooterRowGroup();
			TableRow row = group.createRow();

			TableCell2 cell = row.createCell();
			cell.setStyleClass("total");
			cell.setStyleClass("firstColumn");
			cell.add(new Text(iwrb.getLocalizedString("total", "Total")));

			int total = 0;

			for (Iterator<CaseStatus> caseStatusIter = totals.keySet().iterator(); caseStatusIter.hasNext();) {
				CaseStatus status = caseStatusIter.next();
				Integer statusTotal = totals.get(status);
				total += statusTotal.intValue();

				cell = row.createCell();
				cell.setStyleClass(status.getStatus());
				cell.add(new Text(statusTotal.toString()));
			}

			cell = row.createCell();
			cell.setStyleClass("total");
			cell.setStyleClass("lastColumn");
			cell.add(new Text(String.valueOf(total)));
		}

		return iRow;
	}


	private void addResultToTable(Map<CaseStatus, Integer> totals, Collection<CaseStatus> statuses, TableRowGroup group, int iRow, Result res,
			boolean isSubCategory, boolean showNumbers) {
		TableRow row;
		TableCell2 cell;
		CaseStatus status;
		Map<String, Integer> map = res.getStatusMap();

		row = group.createRow();
		cell = row.createCell();
		cell.add(new Text(res.getName()));
		cell.setStyleClass("firstColumn");
		int totalValue = 0;
		for (Iterator<CaseStatus> statIter = statuses.iterator(); statIter.hasNext();) {
			status = statIter.next();
			Integer value = map.get(status.getStatus());
			int val = 0;
			if (value != null) {
				val = value.intValue();
			}
			totalValue += val;

			cell = row.createCell();
			cell.setStyleClass(status.getStatus());
			if (showNumbers) {
				cell.add(new Text(String.valueOf(val)));
			} else {
				cell.add(Text.getNonBrakingSpace());
			}
			cell.setHorizontalAlignment(Table2.HORIZONTAL_ALIGNMENT_CENTER);

			Integer total = totals.get(status);
			totals.put(status, (total.intValue() + val));
		}

		cell = row.createCell();
		cell.setStyleClass("total");
		cell.setStyleClass("lastColumn");
		if (showNumbers) {
			cell.add(new Text(String.valueOf(totalValue)));
		}
		else {
			cell.add(Text.getNonBrakingSpace());
		}

		if (iRow % 2 == 0) {
			row.setStyleClass("evenRow");
		}
		else {
			row.setStyleClass("oddRow");
		}
		if (isSubCategory) {
			row.setStyleClass("subCategory");
		}
	}

	private Collection<Result> getResults(IWContext iwc, boolean useSubCats, int parentID, boolean useHandlerIfNotFoundCustom, IWTimestamp dateFrom, IWTimestamp dateTo) {
		if (useHandlerIfNotFoundCustom) {
			Handler handler = new CategoryHandler(useSubCats, parentID);
			handler.setDateFrom(dateFrom);
			handler.setDateTo(dateTo);
			return getCasesBusiness().getCasesStatisticsResults(iwc, handler);
		}

		return null;
	}

	private Collection<Result> getResultsUsers(IWContext iwc, IWTimestamp dateFrom, IWTimestamp dateTo) {
		Handler handler = new UserHandler(false, -1);
		handler.setDateFrom(dateFrom);
		handler.setDateTo(dateTo);
		return getCasesBusiness().getCasesStatisticsResults(iwc, handler);
	}

	private Collection<Result> getResultsCode(IWContext iwc, IWTimestamp dateFrom, IWTimestamp dateTo) {
		Handler handler = new CaseTypeHandler(false, -1);
		handler.setDateFrom(dateFrom);
		handler.setDateTo(dateTo);
		return getCasesBusiness().getCasesStatisticsResults(iwc, handler);
	}

	public void setVisibleStatuses(String statuses) {
		this.visibleStatuses = statuses;
	}

	protected CaseStatusHome getCaseStatusHome() {
		try {
			return (CaseStatusHome) IDOLookup.getHome(CaseStatus.class);
		}
		catch (IDOLookupException ile) {
			throw new IBORuntimeException(ile);
		}
	}

	Collection<Result> getCustomCategoryResults(IWContext iwc, int categoryId, Map<String, Integer> statuses, Map<String, List<Integer>> statusesAndCasesIds, IWTimestamp dateFrom, IWTimestamp dateTo) {
		if (getCaseManagersProvider() == null) {
			return null;
		}

		CaseCategory category = null;
		try {
			category = getCaseCategory(categoryId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (category == null || category.isDeleted() || category.isHidden()) {
			return null;
		}

		Set<String> types = new HashSet<>();
		Collection<Case> cases = new ArrayList<Case>();
		try {
			CasesBusiness casesBusiness = getCasesBusiness();
			if (casesBusiness == null) {
				casesBusiness = getCasesBusiness(iwc);
			}

			List<CasesRetrievalManager> caseManagers = getCaseManagersProvider().getCaseManagers();
			if (!ListUtil.isEmpty(caseManagers)) {
				for (CasesRetrievalManager manager: caseManagers) {
					String type = manager.getType();
					if (!StringUtil.isEmpty(type)) {
						types.add(type);
					}
				}
			}
			if (ListUtil.isEmpty(types)) {
				types.add(getCaseManagersProvider().getCaseManager().getType());
			}
			for (String type: types) {
				Collection<Case> casesByType = casesBusiness.getCasesByCriteria(null, category, null, null, null, type);
				if (ListUtil.isEmpty(casesByType)) {
					getLogger().info("No cases found by manager type " + type + " and category " + category);
				} else {
					cases.addAll(casesByType);
				}
			}
		} catch (Exception e) {
			getLogger().log(Level.WARNING, "Error getting cases by manager type(s) " + types + " and category " + category, e);
		}

		Map<String, List<Case>> casesByProcesses = getCasesByProcesses(cases);
		if (casesByProcesses == null || ListUtil.isEmpty(casesByProcesses.keySet())) {
			LOGGER.info("Didn't find any custom category ('"+category+"') cases");
			return null;
		}

		boolean leaveUnlocalizedProcessURL = IWMainApplication.getDefaultIWMainApplication().getSettings().getBoolean("LEAVE_UNLOCALIZED_PROCESS_URL", true);

		String localizedProcessName = null;
		Locale locale = iwc.getCurrentLocale();
		List<Result> results = new ArrayList<Result>();
		String unkownProcess = getResourceBundle(iwc).getLocalizedString("case_statistics.unkown_process", "Unkown process");
		for (String processName: casesByProcesses.keySet()) {
			localizedProcessName = null;

			if (leaveUnlocalizedProcessURL) {
				localizedProcessName = processName;
			} else {
				try {
					localizedProcessName = getCaseManagersProvider().getCaseManager().getProcessName(processName, locale);
				} catch(Exception e) {
					e.printStackTrace();
				}
				//Get localized process name from application
				if (StringUtil.isEmpty(localizedProcessName)) {
					try {
						Collection<Application> apps = null;
						try {
							apps = getApplicationBusiness(iwc != null ? iwc : IWMainApplication.getDefaultIWApplicationContext()).getApplicationHome().findAllByApplicationUrl(processName);
						} catch (Exception e) {}

						if (!ListUtil.isEmpty(apps)) {
							String unlocalizedName = null;
							for (Application app: apps) {
								unlocalizedName = app.getName();
								String localizedName = app.getLocalizedName(locale);
								if (!StringUtil.isEmpty(localizedName)) {
									localizedProcessName = localizedName;
									break;
								}
							}
							if (StringUtil.isEmpty(localizedProcessName) ) {
								localizedProcessName = unlocalizedName;
							}
						}
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
			}

			if (StringUtil.isEmpty(localizedProcessName)) {
				localizedProcessName = unkownProcess;
			}

			Map<String, List<Integer>> statusesAndIds = new HashMap<>();
			results.add(
					new Result(
							-1,
							localizedProcessName,
							getStatusesForCases(
									casesByProcesses.get(processName),
									statuses,
									dateFrom,
									dateTo,
									statusesAndIds
							),
							statusesAndIds,
							false
					)
			);
		}

		return results;
	}

	private Map<String, Integer> getStatusesForCases(List<Case> cases, Map<String, Integer> statusesFromSQL, IWTimestamp dateFrom, IWTimestamp dateTo, Map<String, List<Integer>> statusesAndIds) {
		if (ListUtil.isEmpty(cases) || MapUtil.isEmpty(statusesFromSQL)) {
			return new HashMap<String, Integer>();
		}

		Map<String, Integer> statuses = new HashMap<String, Integer>();

		String statusId = null;
		for (Case theCase: cases) {
			statusId = theCase.getStatus();

			//Do not take into account the case, which status does not exist in the status map from SQL
			if (!statusesFromSQL.containsKey(statusId)) {
				continue;
			}
			//Do not take into account the case, which dates are not in the given date ranges
			if (dateFrom != null && theCase.getCreated() != null && dateFrom.isLaterThan(new IWTimestamp(theCase.getCreated()))) {
				continue;
			}
			if (dateTo != null && theCase.getCreated() != null && dateTo.isEarlierThan(new IWTimestamp(theCase.getCreated()))) {
				continue;
			}

			if (StringUtil.isEmpty(statusId)) {
				LOGGER.warning("There is no status set for case: " + theCase);
			} else {
				Integer count = statuses.get(statusId);
				if (count == null) {
					count = Integer.valueOf(1);
				}
				else {
					count++;
				}
				statuses.put(statusId, count);

				if (statusesAndIds != null) {
					List<Integer> ids = statusesAndIds.get(statusId);
					if (ids == null) {
						ids = new ArrayList<>();
						statusesAndIds.put(statusId, ids);
					}
					ids.add((Integer) theCase.getPrimaryKey());
				}
			}
		}

		return statuses;
	}

	Map<String, Integer> getStatusesForCasesOld(List<Case> cases) {
		if (ListUtil.isEmpty(cases)) {
			return new HashMap<String, Integer>();
		}

		Map<String, Integer> statuses = new HashMap<String, Integer>();

		String statusId = null;
		for (Case theCase: cases) {
			statusId = theCase.getStatus();
			if (StringUtil.isEmpty(statusId)) {
				LOGGER.warning("There is no status set for case: " + theCase);
			}
			else {
				Integer count = statuses.get(statusId);
				if (count == null) {
					count = Integer.valueOf(1);
				}
				else {
					count++;
				}
				statuses.put(statusId, count);
			}
		}

		return statuses;
	}

	private boolean canCaseBeUsedInStatistics(Case theCase) {
		if (ListUtil.isEmpty(cases)) {
			return true;	//	No data set is set
		}

		for (CasePresentation preCase: cases) {
			if (theCase.getPrimaryKey().toString().equals(preCase.getId())) {
				return true;
			}
		}

		return false;
	}

	private Map<String, List<Case>> getCasesByProcesses(Collection<Case> cases) {
		if (ListUtil.isEmpty(cases) || getCaseManagersProvider() == null || getCaseManagersProvider().getCaseManager() == null) {
			return null;
		}

		Map<String, List<Case>> casesByProcesses = new HashMap<String, List<Case>>();

		String processDefinitionName = null;
		for (Case theCase: cases) {
			if (theCase != null && canCaseBeUsedInStatistics(theCase)) {

				//Skip the GENSUPP cases
				CaseCode code = theCase.getCaseCode();
				String caseCode = code == null ? null : code.getCode();
				if (!StringUtil.isEmpty(caseCode) && caseCode.equalsIgnoreCase(ProcessConstants.GENERAL_SUPPORT_CASE_CODE)) {
					continue;
				}

				processDefinitionName = null;
				try {
					processDefinitionName = getCaseManagersProvider().getCaseManager().getProcessDefinitionName(theCase);
				} catch(Exception e) {
					e.printStackTrace();
				}

				//Searching in another way
				if (StringUtil.isEmpty(processDefinitionName)) {
					//By case code
					if (!StringUtil.isEmpty(caseCode)) {
						try {
							ApplicationHome appHome = (ApplicationHome) IDOLookup.getHome(is.idega.idegaweb.egov.application.data.Application.class);
							Collection<is.idega.idegaweb.egov.application.data.Application> apps = appHome.findAllByCaseCode(caseCode);
							if (!ListUtil.isEmpty(apps)) {
								processDefinitionName = apps.iterator().next().getUrl(); //getName();
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}

				if (StringUtil.isEmpty(processDefinitionName)) {
					LOGGER.warning("Unable to get process identifier for case: " + theCase);
				} else {
					List<Case> casesByProcess = casesByProcesses.get(processDefinitionName);
					if (ListUtil.isEmpty(casesByProcess)) {
						casesByProcess = new ArrayList<Case>();
					}
					casesByProcess.add(theCase);
					casesByProcesses.put(processDefinitionName, casesByProcess);
				}
			}
		}

		return casesByProcesses;
	}

	public class Result {

		private int id;
		private boolean useDefaultHandlerIfNotFoundResultsProvider = true;
		private String name = null;
		private Map<String, Integer> statusMap;
		private Map<String, List<Integer>> statusAndCasesIds;

		protected Result(int id, String name, Map<String, Integer> statusMap, Map<String, List<Integer>> statusAndCasesIds) {
			this(id, name, statusMap, statusAndCasesIds, true);
		}

		protected Result(int id, String name, Map<String, Integer> statusMap, Map<String, List<Integer>> statusAndCasesIds, boolean useDefaultHandlerIfNotFoundResultsProvider) {
			this.id = id;
			this.name = name;
			this.statusMap = statusMap;
			this.useDefaultHandlerIfNotFoundResultsProvider = useDefaultHandlerIfNotFoundResultsProvider;
			this.statusAndCasesIds = statusAndCasesIds;
		}

		public String getName() {
			return name;
		}

		public Map<String, Integer> getStatusMap() {
			return statusMap;
		}

		public int getID(){
			return id;
		}

		public boolean isUseDefaultHandlerIfNotFoundResultsProvider() {
			return useDefaultHandlerIfNotFoundResultsProvider;
		}

		public Map<String, List<Integer>> getStatusAndCasesIds() {
			return statusAndCasesIds;
		}

		public void setStatusAndCasesIds(Map<String, List<Integer>> statusAndCasesIds) {
			this.statusAndCasesIds = statusAndCasesIds;
		}

	}

	public abstract class Handler {

		public abstract String getSQL();
		public abstract Collection<Result> getResults(IWContext iwc, ResultSet rs) throws RemoteException, SQLException, FinderException;
		public abstract boolean addResult(IWContext iwc, Collection<Result> results, int prevID, Map<String, Integer> statuses, Map<String, List<Integer>> statusesAndCasesIds) throws RemoteException, FinderException;

		private boolean useSubCats = false;
		private int parentID = -1;
		private IWTimestamp dateFrom;
		private IWTimestamp dateTo;
		private List<String> exceptCaseCodes;

		protected boolean isUseSubCats() {
			return useSubCats;
		}
		protected void setUseSubCats(boolean useSubCats) {
			this.useSubCats = useSubCats;
		}
		protected int getParentID() {
			return parentID;
		}
		protected void setParentID(int parentID) {
			this.parentID = parentID;
		}
		protected IWTimestamp getDateFrom() {
			return dateFrom;
		}
		public void setDateFrom(IWTimestamp dateFrom) {
			this.dateFrom = dateFrom;
		}
		protected IWTimestamp getDateTo() {
			return dateTo;
		}
		public void setDateTo(IWTimestamp dateTo) {
			this.dateTo = dateTo;
		}
		public List<String> getExceptCaseCodes() {
			return exceptCaseCodes;
		}
		public void setExceptCaseCodes(List<String> exceptCaseCodes) {
			this.exceptCaseCodes = exceptCaseCodes;
		}


	}

	//	Cases by categories
	public class CategoryHandler extends Handler {
		private List<Integer> addedCategories = new ArrayList<Integer>();

		public CategoryHandler(boolean useSubCats, int parentID) {
			setUseSubCats(useSubCats);
			setParentID(parentID);
		}

		@Override
		public String getSQL() {
			StringBuilder query = new StringBuilder("select cc.comm_case_category_id, count(c.case_category) as NO_OF_CASES, p.case_status, cc.category_order ")
				.append("from comm_case_category cc left join comm_case c on c.case_category = cc.comm_case_category_id ")
				.append("left join proc_case p on p.proc_case_id = c.comm_case_id ");

			query.append(" where cc.parent_category ");

			if (isUseSubCats() && getParentID() > -1) {
				query.append("= ").append(getParentID());			//	We seek for special category
			} else {
				query.append("is null");							//	We weed ONLY top level categories

				query.append(getCategoriesIdsCriteria());			//	We need the very categories used by PROVIDED cases set
			}

			if (getDateFrom() != null) {
				query.append(getDateFromCriteria(getDateFrom()));
			}
			if (getDateTo() != null) {
				query.append(getDateToCriteria(getDateTo()));
			}

			if (!ListUtil.isEmpty(getExceptCaseCodes())) {
				query.append(getExceptCaseCodesCriteria(getExceptCaseCodes()));
			}

			query.append(" group by cc.comm_case_category_id, cc.category_order, p.case_status order by cc.category_order, cc.comm_case_category_id");

			if (getParentID() == -1) {
				System.out.println("CategoryHandler: " + query.toString());
			}
			return query.toString();
		}

		@Override
		public Collection<Result> getResults(IWContext iwc, ResultSet rs) throws RemoteException, SQLException, FinderException {
			Collection<Result> results = new ArrayList<Result>();
			int previousCaseCategoryId = -1;
			Map<String, Integer> statuses = null;
			Map<String, List<Integer>> statusesAndCasesIds = null;
			while (rs.next()) {
				int categoryId = rs.getInt("comm_case_category_id");
				int count = rs.getInt("NO_OF_CASES");
				String caseStatus = rs.getString("CASE_STATUS");

				if (previousCaseCategoryId != categoryId) {
					if (statuses != null) {
						//	Adding results for previous category
						addResult(iwc, results, previousCaseCategoryId, statuses, statusesAndCasesIds);
					}

					//	New category
					statuses = new HashMap<String, Integer>();
					statusesAndCasesIds = new HashMap<>();
				}

				statuses.put(caseStatus, isValidStatus(caseStatus) ? count : 0);
				previousCaseCategoryId = categoryId;
			}
			if (statuses != null) {
				//	Adding results for LAST category
				addResult(iwc, results, previousCaseCategoryId, statuses, statusesAndCasesIds);
			}

			return results;
		}

		@Override
		public boolean addResult(IWContext iwc, Collection<Result> results, int caseCategoryId, Map<String, Integer> statuses, Map<String, List<Integer>> statusesAndCasesIds)
			throws RemoteException, FinderException {
			if (caseCategoryId < 0) {
				return false;
			}

			if (!addedCategories.contains(caseCategoryId) && isCustomCategory(caseCategoryId)) {
				addedCategories.add(caseCategoryId);
				Collection<Result> customCategoryResults = getCustomCategoryResults(iwc, caseCategoryId, statuses, statusesAndCasesIds, getDateFrom(), getDateTo());
				if (!ListUtil.isEmpty(customCategoryResults)) {
					results.addAll(customCategoryResults);
				}
				return true;
			}

			CaseCategory category = getCaseCategory(caseCategoryId);
			if (category == null || category.isDeleted() || category.isHidden()) {
				return false;
			}
			return addResultToList(getCategoryName(iwc, category), results, caseCategoryId, statuses, statusesAndCasesIds);
		}
	}

	boolean isValidStatus(String caseStatus) {
		if (ListUtil.isEmpty(getAvailableStatuses())) {
			return true;
		}

		return availableStatuses.contains(caseStatus);
	}

	private CaseCategory getTopCategory(CaseCategory category) {
		CaseCategory parentCategory = category.getParent();
		return parentCategory == null ? category : getTopCategory(parentCategory);
	}

	String getCategoryName(IWContext iwc, CaseCategory caseCategory) {
		return caseCategory == null ?	getResourceBundle(iwc).getLocalizedString(UNKOWN_CATEGORY_ID, "Unkown category") :
										caseCategory.getLocalizedCategoryName(iwc.getCurrentLocale());
	}

	CaseCategory getCaseCategory(Object primaryKey) throws RemoteException, FinderException {
		return getCasesBusiness(IWMainApplication.getDefaultIWApplicationContext()).getCaseCategory(primaryKey);
	}

	boolean isCustomCategory(int categoryId) {
		if (!ListUtil.isEmpty(customCategories) && customCategories.contains(Integer.valueOf(categoryId))) {
			return true;
		}

		if (categoryId < 0) {
			return false;
		}

		CaseCategory category = null;
		try {
			category = getCaseCategory(categoryId);
		} catch (Exception e) {
			LOGGER.warning("Unable to get category by: " + categoryId);
		}

		if (category == null || category.isDeleted() || category.isHidden()) {
			return false;
		}

		if (namesOfCustomCategories.contains(category.getName())) {
			if (ListUtil.isEmpty(customCategories)) {
				customCategories = new ArrayList<Integer>();
			}
			customCategories.add(Integer.valueOf(categoryId));
			return true;
		}

		return false;
	}

	//	Cases by user
	public class UserHandler extends Handler {

		public UserHandler(boolean useSubCats, int parentID) {
			setUseSubCats(useSubCats);
			setParentID(parentID);
		}

		@Override
		public String getSQL() {
			StringBuilder query = new StringBuilder("select handler, count(c.comm_case_id) as NO_OF_CASES, p.case_status from comm_case c ")
				.append("left join comm_case_category cc on c.case_category = cc.comm_case_category_id ")
				.append("left join proc_case p on p.proc_case_id = c.comm_case_id, ic_user u where c.handler = u.ic_user_id and c.handler is not null ").append(getCasesIdsCriteria())
				.append(getStatusesIdsCriteria());

			if (getDateFrom() != null) {
				query.append(getDateFromCriteria(getDateFrom()));
			}
			if (getDateTo() != null) {
				query.append(getDateToCriteria(getDateTo()));
			}


			if (!ListUtil.isEmpty(getExceptCaseCodes())) {
				query.append(getExceptCaseCodesCriteria(getExceptCaseCodes()));
			}

			query.append(" group by c.handler, p.case_status, u.display_name order by u.display_name, p.case_status");

			System.out.println("UserHandler: " + query.toString());
			return query.toString();
		}

		@Override
		public Collection<Result> getResults(IWContext iwc, ResultSet rs) throws RemoteException, SQLException, FinderException {
			Collection<Result> results = new ArrayList<Result>();
			int previousUserId = -1;
			Map<String, Integer> statuses = null;
			Map<String, List<Integer>> statusesAndCasesIds = null;
			while (rs.next()) {
				int handlerId = rs.getInt("handler");
				int count = rs.getInt("NO_OF_CASES");
				String caseStatus = rs.getString("case_status");

				if (previousUserId != handlerId) {
					if (statuses != null) {
						//	Adding results for previous user
						addResult(iwc, results, previousUserId, statuses, statusesAndCasesIds);
					}

					//	New user
					statuses = new HashMap<String, Integer>();
					statusesAndCasesIds = new HashMap<>();
				}

				statuses.put(caseStatus, count);
				previousUserId = handlerId;
			}
			if (statuses != null) {
				//	Adding results for LAST user
				addResult(iwc, results, previousUserId, statuses, statusesAndCasesIds);
			}

			return results;
		}

		@Override
		public boolean addResult(IWContext iwc, Collection<Result> results, int userId, Map<String, Integer> statuses, Map<String, List<Integer>> statusesAndCasesIds) throws RemoteException, FinderException {
			String resultName = null;
			if (userId > -1) {
				User user = null;
				if (getUserBusiness() == null) {
					user = getUserBusiness(iwc).getUser(userId);
				} else {
					user = getUserBusiness().getUser(userId);
				}

				resultName = user.getName();
			}
			return addResultToList(resultName, results, userId, statuses, statusesAndCasesIds);
		}

	}

	//	Cases by type
	public class CaseTypeHandler extends Handler {

		public CaseTypeHandler(boolean useSubCats, int parentID) {
			setUseSubCats(useSubCats);
			setParentID(parentID);
		}

		@Override
		public String getSQL() {
			StringBuilder query =  new StringBuilder("select c.case_type, count(c.comm_case_id) as NO_OF_CASES, p.case_status from comm_case c ")
				.append("left join proc_case p on p.proc_case_id = c.comm_case_id where c.case_type = c.case_type ").append(getCasesIdsCriteria())
				.append(getStatusesIdsCriteria());

			if (getDateFrom() != null) {
				query.append(getDateFromCriteria(getDateFrom()));
			}
			if (getDateTo() != null) {
				query.append(getDateToCriteria(getDateTo()));
			}

			if (!ListUtil.isEmpty(getExceptCaseCodes())) {
				query.append(getExceptCaseCodesCriteria(getExceptCaseCodes()));
			}

			query.append(" group by c.case_type, p.case_status order by case_type");

			System.out.println("CaseTypeHandler: " + query.toString());
			return query.toString();
		}

		@Override
		public Collection<Result> getResults(IWContext iwc, ResultSet rs) throws RemoteException, SQLException, FinderException {
			Collection<Result> results = new ArrayList<Result>();
			int prevCaseTypeId = -1;
			Map<String, Integer> statuses = null;
			Map<String, List<Integer>> statusesAndCasesIds = null;
			while (rs.next()) {
				int caseTypeId = rs.getInt("case_type");
				int count = rs.getInt("NO_OF_CASES");
				String caseStatus = rs.getString("case_status");

				if (prevCaseTypeId != caseTypeId) {
					if (statuses != null) {
						//	Adding results for previous case type
						addResult(iwc, results, prevCaseTypeId, statuses, statusesAndCasesIds);
					}

					//	New case type
					statuses = new HashMap<String, Integer>();
					statusesAndCasesIds = new HashMap<>();
				}

				statuses.put(caseStatus, count);
				prevCaseTypeId = caseTypeId;
			}
			if (statuses != null) {
				//	Adding results for LAST case type
				addResult(iwc, results, prevCaseTypeId, statuses, statusesAndCasesIds);
			}

			return results;
		}

		@Override
		public boolean addResult(IWContext iwc, Collection<Result> results, int caseTypeId, Map<String, Integer> statuses, Map<String, List<Integer>> statusesAndCasesIds) throws FinderException, RemoteException {
			String resultName = null;
			if (caseTypeId > -1) {
				CaseType type = null;
				if (getCasesBusiness() == null) {
					type = getCasesBusiness(iwc).getCaseType(caseTypeId);
				} else {
					type = getCasesBusiness().getCaseType(caseTypeId);
				}

				resultName = type.getName();
			}
			return addResultToList(resultName, results, caseTypeId, statuses, statusesAndCasesIds);
		}

	}

	boolean addResultToList(String resultName, Collection<Result> results, int identifier, Map<String, Integer> statuses, Map<String, List<Integer>> statusesAndCasesIds) {
		if (identifier < 0 || StringUtil.isEmpty(resultName)) {
			return false;
		}

		results.add(new Result(identifier, resultName, statuses, statusesAndCasesIds));

		return true;
	}

	private String getExceptCaseCodesCriteria(List<String> exceptCaseCodes) {
		String criteriaSQL = CoreConstants.EMPTY;

		if (!ListUtil.isEmpty(exceptCaseCodes)) {
			StringBuilder caseCodes = new StringBuilder();
			for (String exc : exceptCaseCodes) {
				if (caseCodes.length() > 0) {
					caseCodes.append(CoreConstants.COMMA).append(CoreConstants.SPACE);
				}

				caseCodes.append(CoreConstants.QOUTE_SINGLE_MARK);
				caseCodes.append(exc);
				caseCodes.append(CoreConstants.QOUTE_SINGLE_MARK);
			}

			criteriaSQL = new StringBuilder(" and p.case_code not in (").append(caseCodes.toString()).append(") ").toString();
		}

		return criteriaSQL;
	}

	String getDateFromCriteria(IWTimestamp date) {
		StringBuilder builder = new StringBuilder(" and p.created >= '").append(date.toSQLDateString()).append("'");
		return builder.toString();
	}

	String getDateToCriteria(IWTimestamp date) {
		StringBuilder builder = new StringBuilder(" and p.created <= '").append(date.toSQLDateString()).append("'");
		return builder.toString();
	}

	String getCasesIdsCriteria() {
		if (ListUtil.isEmpty(cases)) {
			return CoreConstants.EMPTY;
		}

		if (casesCriteria != null) {
			return casesCriteria;
		}

		StringBuilder casesIds = new StringBuilder();
		for (Iterator<CasePresentation> casesIter = cases.iterator(); casesIter.hasNext();) {
			casesIds.append(casesIter.next().getId());

			if (casesIter.hasNext()) {
				casesIds.append(CoreConstants.COMMA).append(CoreConstants.SPACE);
			}
		}

		casesCriteria = new StringBuilder(" and c.comm_case_id in (").append(casesIds.toString()).append(") ").toString();
		return casesCriteria;
	}

	String getCategoriesIdsCriteria() {
		if (ListUtil.isEmpty(cases)) {
			return CoreConstants.EMPTY;
		}

		if (categoriesCriteria != null) {
			return categoriesCriteria;
		}

		StringBuilder query = new StringBuilder(" and cc.comm_case_category_id ");

		CaseCategory category = null;
		CaseCategory topCategory = null;
		List<String> categoriesToUse = new ArrayList<String>();
		for (CasePresentation theCase: cases) {
			category = null;

			if (!StringUtil.isEmpty(theCase.getCategoryId())) {
				try {
					category = getCaseCategory(theCase.getCategoryId());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			if (category != null && !category.isDeleted() && !category.isHidden()) {
				topCategory = getTopCategory(category);
				if (!categoriesToUse.contains(topCategory.getPrimaryKey().toString())) {
					categoriesToUse.add(topCategory.getPrimaryKey().toString());
				}
			}
		}

		if (ListUtil.isEmpty(categoriesToUse)) {
			query.append("= -1");					//	No categories needed!!!
		}
		else {
			query.append("in (");
			for (Iterator<String> catIter = categoriesToUse.iterator(); catIter.hasNext();) {
				query.append(catIter.next());

				if (catIter.hasNext()) {
					query.append(CoreConstants.COMMA).append(CoreConstants.SPACE);
				}
			}
			query.append(")");
		}

		categoriesCriteria = query.toString();
		return categoriesCriteria;
	}

	private List<String> getAvailableStatuses() {
		if (ListUtil.isEmpty(cases)) {
			return null;
		}

		if (!ListUtil.isEmpty(availableStatuses)) {
			return availableStatuses;
		}

		String status = null;
		availableStatuses = new ArrayList<String>();
		for (Iterator<CasePresentation> casesIter = cases.iterator(); casesIter.hasNext();) {
			CaseStatus caseStatus = casesIter.next().getCaseStatus();
			status = caseStatus == null ? null : caseStatus.getStatus();

			if (!StringUtil.isEmpty(status) && !availableStatuses.contains(status)) {
				availableStatuses.add(status);
			}
		}

		return availableStatuses;
	}

	String getStatusesIdsCriteria() {
		if (ListUtil.isEmpty(cases)) {
			return CoreConstants.EMPTY;
		}

		if (statusesCriteria != null) {
			return statusesCriteria;
		}

		StringBuilder criteria = new StringBuilder(" and p.case_status ");

		List<String> statusesToUse = getAvailableStatuses();
		if (ListUtil.isEmpty(statusesToUse)) {
			criteria.append("= '-1'");
		}
		else {
			criteria.append("in (");
			for (Iterator<String> statusesIter = statusesToUse.iterator(); statusesIter.hasNext();) {
				criteria.append(CoreConstants.QOUTE_SINGLE_MARK).append(statusesIter.next()).append(CoreConstants.QOUTE_SINGLE_MARK);

				if (statusesIter.hasNext()) {
					criteria.append(CoreConstants.COMMA).append(CoreConstants.SPACE);
				}
			}
			criteria.append(") ");
		}

		statusesCriteria = criteria.toString();
		return statusesCriteria;
	}

	public Collection<CasePresentation> getCases() {
		return cases;
	}

	public void setCases(Collection<CasePresentation> cases) {
		this.cases = cases;
	}

	public CaseManagersProvider getCaseManagersProvider() {
		if (caseManagersProvider == null) {
			caseManagersProvider = WFUtil.getBeanInstance(CaseManagersProvider.beanIdentifier);
		}
		return caseManagersProvider;
	}

	public void setCaseManagersProvider(CaseManagersProvider caseManagersProvider) {
		this.caseManagersProvider = caseManagersProvider;
	}

	@Override
	public String getBundleIdentifier() {
		return CasesConstants.IW_BUNDLE_IDENTIFIER;
	}

	public Boolean getUseStatisticsByCaseType() {
		return useStatisticsByCaseType;
	}

	public void setUseStatisticsByCaseType(Boolean useStatisticsByCaseType) {
		this.useStatisticsByCaseType = useStatisticsByCaseType;
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

	public Boolean isShowDateRange() {
		return showDateRange;
	}

	public void setShowDateRange(Boolean showDateRange) {
		this.showDateRange = showDateRange;
	}




}