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

import javax.ejb.FinderException;

import com.idega.block.process.data.CaseStatus;
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
import com.idega.util.database.ConnectionBroker;


public class CasesStatistics extends CasesBlock {

	protected void present(IWContext iwc) throws Exception {
		
		boolean useSubCats = super.getCasesBusiness(iwc).useSubCategories();
		boolean useTypes = super.getCasesBusiness(iwc).useTypes();
		
		IWResourceBundle iwrb = getResourceBundle(iwc);
		Collection statuses = getBusiness().getCaseStatuses();

		Layer section = new Layer(Layer.DIV);
		section.setStyleClass("formSection");
		section.setStyleClass("statisticsLayer");
		add(section);

		Layer clearLayer = new Layer(Layer.DIV);
		clearLayer.setStyleClass("Clear");
		
		Heading1 heading = new Heading1(iwrb.getLocalizedString("case.statistics", "Case statistics"));
		section.add(heading);

		Collection coll = getResults(iwc, useSubCats, -1);
		addResults(null, null, iwc, iwrb, section, coll, statuses, iwrb.getLocalizedString("case.cases_by_category", "Cases by category"), useSubCats, false, 0);
		section.add(clearLayer);

		coll = getResultsUsers(iwc);
		addResults(null, null, iwc, iwrb, section, coll, statuses, iwrb.getLocalizedString("case.cases_by_handler", "Cases by handler"), false, false, 0);
		section.add(clearLayer);
		
		if (useTypes) {
			coll = getResultsCode(iwc);
			addResults(null, null, iwc, iwrb, section, coll, statuses, iwrb.getLocalizedString("case.cases_by_type", "Cases by type"), false, false, 0);
			section.add(clearLayer);
		}
		
	}

	private int addResults(Table2 table, TableRowGroup group, IWContext iwc, IWResourceBundle iwrb, Layer section, Collection coll, Collection statuses, String header, boolean useSubCats, boolean isSubCategory, int iRow) {

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
			
			Iterator statIter = statuses.iterator();
			CaseStatus status;
			while (statIter.hasNext()) {
				status = (CaseStatus) statIter.next();
				cell = row.createHeaderCell();
				cell.setStyleClass(status.getStatus());
				cell.add(new Text(iwrb.getLocalizedString("case_status_key."+status.getStatus(), status.getStatus())));
			}
			
			group = table.createBodyRowGroup();
		}
		
		Iterator iter = coll.iterator();
		
		while (iter.hasNext()) {
			++iRow;
			Result res = (Result) iter.next();
			addResultToTable(statuses, group, iRow, res, isSubCategory);
			if (useSubCats) {
				Collection subCats = getResults(iwc, true, res.getID());
				iRow = addResults(table, group, iwc, iwrb, section, subCats, statuses, header, useSubCats, true, iRow);
			}
		}
		
		return iRow;
	}


	private void addResultToTable(Collection statuses, TableRowGroup group, int iRow, Result res, boolean isSubCategory) {
		TableRow row;
		TableCell2 cell;
		Iterator statIter;
		CaseStatus status;
		HashMap map = res.getStatusMap();
		statIter = statuses.iterator();

		row = group.createRow();
		cell = row.createCell();
		cell.add(new Text(res.getName()));
		while (statIter.hasNext()) {
			status = (CaseStatus) statIter.next();
			Integer value = (Integer) map.get(status.getStatus());
			int val = 0;
			if (value != null) {
				val = value.intValue();
			}
			cell = row.createCell();
			cell.setStyleClass(status.getStatus());
			cell.add(new Text(String.valueOf(val)));
			cell.setHorizontalAlignment(Table2.HORIZONTAL_ALIGNMENT_CENTER);
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
	
	
	private Collection getResults(IWContext iwc, boolean useSubCats, int parentID) {
		Handler handler = new CategoryHandler();
		return getResults(iwc, handler, useSubCats, parentID);
	}
	
	private Collection getResultsUsers(IWContext iwc) {
		Handler handler = new UserHandler();
		return getResults(iwc, handler, false, -1);
	}
	
	private Collection getResultsCode(IWContext iwc) {
		Handler handler = new CaseTypeHandler();
		return getResults(iwc, handler, false, -1);
	}

	private Collection getResults(IWContext iwc, Handler handler, boolean useSubCats, int parentID) {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;

		Collection results = new ArrayList();
		try {
			conn = ConnectionBroker.getConnection();
			stmt = conn.createStatement();

			rs = stmt.executeQuery(handler.getSQL(useSubCats, parentID));
			
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
		private HashMap statusMap;
		
		public Result(int id, String name, HashMap statusMap) {
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
		
		public HashMap getStatusMap() {
			return statusMap;
		}
		
		public int getID(){
			return id;
		}
	}
	
	private interface Handler {
		public String getSQL(boolean useSubCats, int parentID);
		public Collection getResults(IWContext iwc, ResultSet rs) throws RemoteException, SQLException, FinderException ;
	}
	
	private class CategoryHandler implements Handler{
		
		public String getSQL(boolean useSubCats, int parentID) {
			StringBuffer buff = new StringBuffer("select cc.comm_case_category_id, count(c.case_category) as NO_OF_CASES, p.case_status, cc.category_order ")
					.append("from comm_case_category cc ")
					.append("left join comm_case c on c.case_category = cc.comm_case_category_id ")
					.append("left join proc_case p on p.proc_case_id = c.comm_case_id ");
					if (useSubCats && parentID > -1) {
						buff.append("where cc.parent_category = ").append(parentID);
					} else {
						buff.append("where cc.parent_category is null");
					}
					buff.append(" group by cc.comm_case_category_id, cc.category_order, p.case_status ")
					.append("ORDER BY COMM_CASE_CATEGORY_ID, cc.category_order");
			
			return buff.toString();
		}

		public Collection getResults(IWContext iwc, ResultSet rs) throws RemoteException, SQLException, FinderException {
			Collection results = new ArrayList();
			int prevID = -1;
			HashMap statuses = new HashMap();
			while (rs.next()) {
				int catID = rs.getInt("comm_case_category_id");
				int count = rs.getInt("NO_OF_CASES");
				String caseStatus = rs.getString("CASE_STATUS");
				if (prevID != catID) {
					addResult(iwc, results, prevID, statuses);
					// NEW CATEGORY
					statuses = new HashMap();
				}
				statuses.put(caseStatus, new Integer(count));

				prevID = catID;
				
			}
			// ADDING THE LAST ONE
			addResult(iwc, results, prevID, statuses);
			
			return results;
		}

		private void addResult(IWContext iwc, Collection results, int prevID, HashMap statuses) throws FinderException, RemoteException {
			if (prevID > -1) {
				CaseCategory cat = getCasesBusiness(iwc).getCaseCategory(new Integer(prevID));
				Result res = new Result(prevID, cat.getName(), statuses);
				results.add(res);
			}
		}
	
	}

	private class UserHandler implements Handler{
		
		public String getSQL(boolean useSubCats, int parentID) {
			return "select handler, count(c.comm_case_id) as NO_OF_CASES, p.case_status " +
					"from comm_case c " +
					"left join comm_case_category cc on c.case_category = cc.comm_case_category_id " +
					"left join proc_case p on p.proc_case_id = c.comm_case_id " +
					"where c.handler is not null " +
					"group by c.handler, p.case_status";
		}

		public Collection getResults(IWContext iwc, ResultSet rs) throws RemoteException, SQLException, FinderException {
			Collection results = new ArrayList();
			int prevID = -1;
			HashMap statuses = new HashMap();
			while (rs.next()) {
				int handID = rs.getInt("handler");
				int count = rs.getInt("NO_OF_CASES");
				String caseStatus = rs.getString("CASE_STATUS");
				if (prevID != handID) {
					addResult(iwc, results, prevID, statuses);
					// NEW CATEGORY
					statuses = new HashMap();
				}
				statuses.put(caseStatus, new Integer(count));

				prevID = handID;
				
			}
			// ADDING THE LAST ONE
			addResult(iwc, results, prevID, statuses);
			
			return results;
		}

		private void addResult(IWContext iwc, Collection results, int prevID, HashMap statuses) throws FinderException, RemoteException {
			if (prevID > -1) {
				User user = getUserBusiness().getUser(prevID);
				Result res = new Result(prevID, user.getName(), statuses);
				results.add(res);
			}
		}
	
	}

	private class CaseTypeHandler implements Handler{
		
		public String getSQL(boolean useSubCats, int parentID) {
			return "select c.case_type, count(c.comm_case_id) as NO_OF_CASES, p.case_status " +
					"from comm_case c " +
					"left join proc_case p on p.proc_case_id = c.comm_case_id " +
					"group by p.case_status, c.case_type " +
					"order by case_type";
		}

		public Collection getResults(IWContext iwc, ResultSet rs) throws RemoteException, SQLException, FinderException {
			Collection results = new ArrayList();
			int prevID = -1;
			HashMap statuses = new HashMap();
			while (rs.next()) {
				int handID = rs.getInt("case_type");
				int count = rs.getInt("NO_OF_CASES");
				String caseStatus = rs.getString("CASE_STATUS");
				if (prevID != handID) {
					addResult(iwc, results, prevID, statuses);
					// NEW CATEGORY
					statuses = new HashMap();
				}
				statuses.put(caseStatus, new Integer(count));

				prevID = handID;
				
			}
			// ADDING THE LAST ONE
			addResult(iwc, results, prevID, statuses);
			
			return results;
		}

		private void addResult(IWContext iwc, Collection results, int prevID, HashMap statuses) throws FinderException, RemoteException {
			if (prevID > -1) {
				CaseType type = getBusiness().getCaseType(new Integer(prevID));
				Result res = new Result(prevID, type.getName(), statuses);
				results.add(res);
			}
		}
	
	}
	
}