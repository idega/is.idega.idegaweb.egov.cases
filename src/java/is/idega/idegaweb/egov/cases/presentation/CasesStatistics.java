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
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.ejb.FinderException;

import com.idega.block.process.data.Case;
import com.idega.block.process.data.CaseStatus;
import com.idega.block.process.data.CaseStatusHome;
import com.idega.business.IBORuntimeException;
import com.idega.data.IDOLookup;
import com.idega.data.IDOLookupException;
import com.idega.idegaweb.IWResourceBundle;
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
import com.idega.user.data.User;
import com.idega.util.CoreConstants;
import com.idega.util.ListUtil;
import com.idega.util.database.ConnectionBroker;


public class CasesStatistics extends CasesBlock {
	
	private String visibleStatuses = null;
	
	private Collection<Case> cases;

	@SuppressWarnings("unchecked")
	@Override
	protected void present(IWContext iwc) throws Exception {
		
		boolean useSubCats = super.getCasesBusiness(iwc).useSubCategories();
		boolean useTypes = super.getCasesBusiness(iwc).useTypes();
		
		IWResourceBundle iwrb = getResourceBundle(iwc);
		Collection<CaseStatus> statuses = null;
		if (visibleStatuses == null) {
			statuses = getCasesBusiness().getCaseStatuses();
		} else {
			statuses = new ArrayList<CaseStatus>();
			StringTokenizer tok = new StringTokenizer(visibleStatuses, ",");
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

		Layer section = new Layer(Layer.DIV);
		section.setStyleClass("formSection");
		section.setStyleClass("statisticsLayer");
		add(section);

		Layer clearLayer = new Layer(Layer.DIV);
		clearLayer.setStyleClass("Clear");
		
		Heading1 heading = new Heading1(iwrb.getLocalizedString("case.statistics", "Case statistics"));
		section.add(heading);

		Collection<Result> resultsByCaseCategories = getResults(iwc, useSubCats, -1);
		addResults(null, null, null, iwc, iwrb, section, resultsByCaseCategories, statuses, iwrb.getLocalizedString("case.cases_by_category", "Cases by category"),
				useSubCats, false, 0);
		section.add(clearLayer);

		Collection<Result> resultsByUsers = getResultsUsers(iwc);
		addResults(null, null, null, iwc, iwrb, section, resultsByUsers, statuses, iwrb.getLocalizedString("case.cases_by_handler", "Cases by handler"), false,
				false, 0);
		section.add(clearLayer);
		
		if (useTypes) {
			Collection<Result> resultsByCaseTypes = getResultsCode(iwc);
			addResults(null, null, null, iwc, iwrb, section, resultsByCaseTypes, statuses, iwrb.getLocalizedString("case.cases_by_type", "Cases by type"), false,
					false, 0);
			section.add(clearLayer);
		}
	}

	private int addResults(Map<CaseStatus, Integer> totals, Table2 table, TableRowGroup group, IWContext iwc, IWResourceBundle iwrb, Layer section,
			Collection<Result> results, Collection<CaseStatus> statuses, String header, boolean useSubCats, boolean isSubCategory, int iRow) {
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
				cell.add(new Text(iwrb.getLocalizedString("case_status_key."+status.getStatus(), status.getStatus())));
			}
			cell.setStyleClass("lastColumn");

			group = table.createBodyRowGroup();
		}
		
		for (Iterator<Result> iter = results.iterator(); iter.hasNext();) {
			++iRow;
			Result res = iter.next();
			boolean hasSubCats = false;
			Collection<Result> subCats = null;
			if (useSubCats) {
				subCats = getResults(iwc, true, res.getID());
				hasSubCats = subCats != null && !subCats.isEmpty();
			}
			addResultToTable(totals, statuses, group, iRow, res, isSubCategory, !hasSubCats);
			if (hasSubCats) {
				iRow = addResults(totals, table, group, iwc, iwrb, section, subCats, statuses, header, useSubCats, true, iRow);
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
	
	private Collection<Result> getResults(IWContext iwc, boolean useSubCats, int parentID) {
		Handler handler = new CategoryHandler(useSubCats, parentID);
		return getResults(iwc, handler);
	}
	
	private Collection<Result> getResultsUsers(IWContext iwc) {
		Handler handler = new UserHandler(false, -1);
		return getResults(iwc, handler);
	}
	
	private Collection<Result> getResultsCode(IWContext iwc) {
		Handler handler = new CaseTypeHandler(false, -1);
		return getResults(iwc, handler);
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
	
	private Collection<Result> getResults(IWContext iwc, Handler handler) {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;

		Collection<Result> results = new ArrayList<Result>();
		try {
			conn = ConnectionBroker.getConnection();
			stmt = conn.createStatement();

			rs = stmt.executeQuery(handler.getSQL());
			
			results.addAll(handler.getResults(iwc, rs));

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (conn != null) {
				ConnectionBroker.freeConnection(conn);
			}
		}
		
		return results;
	}
	
	private class Result {
	
		private int id;
		private String name = null;
		private int count = 0;
		private Map<String, Integer> statusMap;
		
		public Result(int id, String name, Map<String, Integer> statusMap) {
			this.id = id;
			this.name = name;
			this.statusMap = statusMap;
		}
		
		public String getName() {
			return name;
		}
		
		public int getCount() {
			return count;
		}
		
		public Map<String, Integer> getStatusMap() {
			return statusMap;
		}
		
		public int getID(){
			return id;
		}
	}
	
	private abstract class Handler {
		public abstract String getSQL();
		public abstract Collection<Result> getResults(IWContext iwc, ResultSet rs) throws RemoteException, SQLException, FinderException;
		public abstract boolean addResult(IWContext iwc, Collection<Result> results, int prevID, Map<String, Integer> statuses)
										throws RemoteException, FinderException;
		
		private boolean useSubCats = false;
		private int parentID = -1;
		
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
	}
	
	protected class CategoryHandler extends Handler {
		public CategoryHandler(boolean useSubCats, int parentID) {
			setUseSubCats(useSubCats);
			setParentID(parentID);
		}
		
		@Override
		public String getSQL() {
			StringBuilder query = new StringBuilder("select cc.comm_case_category_id, count(c.case_category) as NO_OF_CASES, p.case_status, cc.category_order ")
				.append("from comm_case_category cc left join comm_case c on c.case_category = cc.comm_case_category_id ")
				.append("left join proc_case p on p.proc_case_id = c.comm_case_id where cc.parent_category ");
				
			if (isUseSubCats() && getParentID() > -1) {
				query.append("= ").append(getParentID());
			} else {
				query.append("is null");
			}
			
			query.append(getCasesIdsCriteria(false)).append("group by cc.comm_case_category_id, cc.category_order, p.case_status ")
			.append("ORDER BY cc.category_order, COMM_CASE_CATEGORY_ID");
			
			return query.toString();
		}

		@Override
		public Collection<Result> getResults(IWContext iwc, ResultSet rs) throws RemoteException, SQLException, FinderException {
			Collection<Result> results = new ArrayList<Result>();
			int previousCaseCategoryId = -1;
			Map<String, Integer> statuses = new HashMap<String, Integer>();
			while (rs.next()) {
				int categoryId = rs.getInt("comm_case_category_id");
				int count = rs.getInt("NO_OF_CASES");
				String caseStatus = rs.getString("CASE_STATUS");
				
				if (previousCaseCategoryId != categoryId) {
					statuses.put(caseStatus, count);
					addResult(iwc, results, categoryId, statuses);
					
					statuses = new HashMap<String, Integer>();
				}
				
				previousCaseCategoryId = categoryId;
			}
			
			return results;
		}

		@Override
		public boolean addResult(IWContext iwc, Collection<Result> results, int caseCategoryId, Map<String, Integer> statuses)
			throws RemoteException, FinderException {
			String resultName = null;
			if (caseCategoryId > -1) {
				CaseCategory cat = getCasesBusiness(iwc).getCaseCategory(caseCategoryId);
				resultName = cat.getLocalizedCategoryName(iwc.getCurrentLocale());
			}
			return addResultToList(resultName, results, caseCategoryId, statuses);
		}
	}

	protected class UserHandler extends Handler {
		public UserHandler(boolean useSubCats, int parentID) {
			setUseSubCats(useSubCats);
			setParentID(parentID);
		}
		
		@Override
		public String getSQL() {
			return new StringBuilder("select handler, count(c.comm_case_id) as NO_OF_CASES, p.case_status from comm_case c ")
						.append("left join comm_case_category cc on c.case_category = cc.comm_case_category_id ")
						.append("left join proc_case p on p.proc_case_id = c.comm_case_id where c.handler is not null ").append(getCasesIdsCriteria(false))
						.append("group by c.handler, p.case_status").toString();
		}

		@Override
		public Collection<Result> getResults(IWContext iwc, ResultSet rs) throws RemoteException, SQLException, FinderException {
			Collection<Result> results = new ArrayList<Result>();
			int previousUserId = -1;
			Map<String, Integer> statuses = new HashMap<String, Integer>();
			while (rs.next()) {
				int handlerId = rs.getInt("handler");
				int count = rs.getInt("NO_OF_CASES");
				String caseStatus = rs.getString("CASE_STATUS");
				
				if (previousUserId != handlerId) {
					statuses.put(caseStatus, count);
					addResult(iwc, results, handlerId, statuses);

					statuses = new HashMap<String, Integer>();
				}
				
				previousUserId = handlerId;
			}
			
			return results;
		}

		@Override
		public boolean addResult(IWContext iwc, Collection<Result> results, int userId, Map<String, Integer> statuses) throws RemoteException, FinderException {
			String resultName = null;
			if (userId > -1) {
				User user = getUserBusiness().getUser(userId);
				resultName = user.getName();
			}
			return addResultToList(resultName, results, userId, statuses);
		}

	}
	
	protected class CaseTypeHandler extends Handler {
		public CaseTypeHandler(boolean useSubCats, int parentID) {
			setUseSubCats(useSubCats);
			setParentID(parentID);
		}
		
		@Override
		public String getSQL() {
			return new StringBuilder("select c.case_type, count(c.comm_case_id) as NO_OF_CASES, p.case_status from comm_case c ")
					.append("left join proc_case p on p.proc_case_id = c.comm_case_id ").append(getCasesIdsCriteria(true))
					.append("group by c.case_type, p.case_status order by case_type").toString();
		}

		@Override
		public Collection<Result> getResults(IWContext iwc, ResultSet rs) throws RemoteException, SQLException, FinderException {
			Collection<Result> results = new ArrayList<Result>();
			int prevCaseTypeId = -1;
			Map<String, Integer> statuses = new HashMap<String, Integer>();
			while (rs.next()) {
				int caseTypeId = rs.getInt("case_type");
				int count = rs.getInt("NO_OF_CASES");
				String caseStatus = rs.getString("case_status");
				
				if (prevCaseTypeId != caseTypeId) {
					statuses.put(caseStatus, count);
					addResult(iwc, results, caseTypeId, statuses);
					
					statuses = new HashMap<String, Integer>();
				}
				
				prevCaseTypeId = caseTypeId;
			}
			
			return results;
		}

		@Override
		public boolean addResult(IWContext iwc, Collection<Result> results, int caseTypeId, Map<String, Integer> statuses)
			throws FinderException, RemoteException {			
			String resultName = null;
			if (caseTypeId > -1) {
				CaseType type = getCasesBusiness().getCaseType(caseTypeId);
				resultName = type.getName();
			}
			return addResultToList(resultName, results, caseTypeId, statuses);
		}
	
	}
	
	private boolean addResultToList(String resultName, Collection<Result> results, int identifier, Map<String, Integer> statuses) {
		if (identifier < 0) {
			return false;
		}
		
		results.add(new Result(identifier, resultName, statuses));

		return true;
	}
	
	private String getCasesIdsCriteria(boolean addWhere) {
		if (ListUtil.isEmpty(cases)) {
			return CoreConstants.EMPTY;
		}
		
		StringBuilder casesIds = new StringBuilder();
		for (Iterator<Case> casesIter = cases.iterator(); casesIter.hasNext();) {
			casesIds.append(casesIter.next().getId());
			
			if (casesIter.hasNext()) {
				casesIds.append(CoreConstants.COMMA).append(CoreConstants.SPACE);
			}
		}
		
		return new StringBuilder(addWhere ? " where" : " and").append(" c.comm_case_id in (").append(casesIds.toString()).append(") ").toString();
	}

	public Collection<Case> getCases() {
		return cases;
	}

	public void setCases(Collection<Case> cases) {
		this.cases = cases;
	}
	
}