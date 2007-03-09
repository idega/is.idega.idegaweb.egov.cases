/*
 * $Id$ Created on Oct 30, 2005
 * 
 * Copyright (C) 2005 Idega Software hf. All Rights Reserved.
 * 
 * This software is the proprietary information of Idega hf. Use is subject to license terms.
 */
package is.idega.idegaweb.egov.cases.data;

import is.idega.idegaweb.egov.cases.util.CaseConstants;
import java.util.Collection;
import javax.ejb.FinderException;
import com.idega.block.process.data.AbstractCaseBMPBean;
import com.idega.block.process.data.Case;
import com.idega.block.process.data.CaseStatus;
import com.idega.data.IDOException;
import com.idega.data.IDORelationshipException;
import com.idega.data.query.CountColumn;
import com.idega.data.query.InCriteria;
import com.idega.data.query.MatchCriteria;
import com.idega.data.query.Order;
import com.idega.data.query.SelectQuery;
import com.idega.data.query.Table;
import com.idega.user.data.User;

public class GeneralCaseBMPBean extends AbstractCaseBMPBean implements Case, GeneralCase {

	private static final String ENTITY_NAME = "comm_case";

	private static final String COLUMN_MESSAGE = "message";
	private static final String COLUMN_REPLY = "reply";
	private static final String COLUMN_CASE_CATEGORY = "case_category";
	private static final String COLUMN_CASE_TYPE = "case_type";
	private static final String COLUMN_TYPE = "type";
	private static final String COLUMN_HANDLER = "handler";
	private static final String COLUMN_IS_PRIVATE = "is_private";

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.idega.block.process.data.AbstractCaseBMPBean#getCaseCodeKey()
	 */
	public String getCaseCodeKey() {
		return CaseConstants.CASE_CODE_KEY;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.idega.block.process.data.AbstractCaseBMPBean#getCaseCodeDescription()
	 */
	public String getCaseCodeDescription() {
		return "General case";
	}

	public String getEntityName() {
		return ENTITY_NAME;
	}

	public void initializeAttributes() {
		addGeneralCaseRelation();

		addAttribute(COLUMN_MESSAGE, "Message", String.class, 4000);
		addAttribute(COLUMN_REPLY, "Reply", String.class, 4000);
		addAttribute(COLUMN_TYPE, "Type", String.class);
		addAttribute(COLUMN_IS_PRIVATE, "Is private", Boolean.class);

		addManyToOneRelationship(COLUMN_CASE_CATEGORY, CaseCategory.class);
		addManyToOneRelationship(COLUMN_CASE_TYPE, CaseType.class);
		addManyToOneRelationship(COLUMN_HANDLER, User.class);
	}

	// Getters
	public String getMessage() {
		return getStringColumnValue(COLUMN_MESSAGE);
	}

	public String getReply() {
		String reply = getStringColumnValue(COLUMN_REPLY);

		if (reply == null || "".equals(reply)) {
			reply = getBody();
			if (reply == null) {
				reply = "";
			}
		}

		return reply;
	}

	public String getType() {
		return getStringColumnValue(COLUMN_TYPE);
	}

	public CaseCategory getCaseCategory() {
		return (CaseCategory) getColumnValue(COLUMN_CASE_CATEGORY);
	}

	public CaseType getCaseType() {
		return (CaseType) getColumnValue(COLUMN_CASE_TYPE);
	}

	public User getHandledBy() {
		return (User) getColumnValue(COLUMN_HANDLER);
	}

	public boolean isPrivate() {
		return getBooleanColumnValue(COLUMN_IS_PRIVATE, false);
	}

	// Setters
	public void setMessage(String message) {
		setColumn(COLUMN_MESSAGE, message);
	}

	public void setReply(String reply) {
		setColumn(COLUMN_REPLY, reply);
	}

	public void setType(String type) {
		setColumn(COLUMN_TYPE, type);
	}

	public void setCaseCategory(CaseCategory category) {
		setColumn(COLUMN_CASE_CATEGORY, category);
	}

	public void setCaseType(CaseType type) {
		setColumn(COLUMN_CASE_TYPE, type);
	}

	public void setHandledBy(User handler) {
		setColumn(COLUMN_HANDLER, handler);
	}

	public void setAsPrivate(boolean isPrivate) {
		setColumn(COLUMN_IS_PRIVATE, isPrivate);
	}

	// Finders
	public Collection ejbFindAllByGroup(Collection groups) throws FinderException {
		return ejbFindAllByGroupAndStatuses(groups, null);
	}

	public Collection ejbFindAllByGroupAndStatuses(Collection groups, String[] statuses) throws FinderException {
		Table table = new Table(this);
		Table process = new Table(Case.class);

		SelectQuery query = new SelectQuery(table);
		query.addColumn(table, getIDColumnName(), true);
		try {
			query.addJoin(table, process);
		}
		catch (IDORelationshipException e) {
			e.printStackTrace();
			throw new FinderException(e.getMessage());
		}
		query.addCriteria(new InCriteria(process.getColumn(getSQLGeneralCaseHandlerColumnName()), groups));
		if (statuses != null) {
			query.addCriteria(new InCriteria(process.getColumn(getSQLGeneralCaseCaseStatusColumnName()), statuses));
		}

		return idoFindPKsByQuery(query);
	}

	public Collection ejbFindAllByHandler(User handler) throws FinderException {
		return ejbFindAllByHandlerAndStatuses(handler, null);
	}

	public Collection ejbFindAllByHandlerAndStatuses(User handler, String[] statuses) throws FinderException {
		Table table = new Table(this);
		Table process = new Table(Case.class);

		SelectQuery query = new SelectQuery(table);
		query.addColumn(table, getIDColumnName(), true);
		try {
			query.addJoin(table, process);
		}
		catch (IDORelationshipException e) {
			e.printStackTrace();
			throw new FinderException(e.getMessage());
		}
		query.addCriteria(new MatchCriteria(table.getColumn(COLUMN_HANDLER), MatchCriteria.EQUALS, handler));
		if (statuses != null) {
			query.addCriteria(new InCriteria(process.getColumn(getSQLGeneralCaseCaseStatusColumnName()), statuses));
		}

		return idoFindPKsByQuery(query);
	}

	public Collection ejbFindAllByUsers(Collection users) throws FinderException {
		Table table = new Table(this);
		Table process = new Table(Case.class);

		SelectQuery query = new SelectQuery(table);
		query.addColumn(table, getIDColumnName(), true);
		try {
			query.addJoin(table, process);
		}
		catch (IDORelationshipException e) {
			e.printStackTrace();
			throw new FinderException(e.getMessage());
		}
		query.addCriteria(new InCriteria(process.getColumn(getSQLGeneralCaseUserColumnName()), users));

		return idoFindPKsByQuery(query);
	}

	public Collection ejbFindByCriteria(CaseCategory category, CaseType type, CaseStatus status, Boolean anonymous) throws FinderException {
		Table table = new Table(this);
		Table process = new Table(Case.class);

		SelectQuery query = new SelectQuery(table);
		query.addColumn(table, getIDColumnName(), true);
		try {
			query.addJoin(table, process);
		}
		catch (IDORelationshipException e) {
			e.printStackTrace();
			throw new FinderException(e.getMessage());
		}

		if (category != null) {
			query.addCriteria(new MatchCriteria(table.getColumn(COLUMN_CASE_CATEGORY), MatchCriteria.EQUALS, category));
		}
		if (type != null) {
			query.addCriteria(new MatchCriteria(table.getColumn(COLUMN_CASE_TYPE), MatchCriteria.EQUALS, type));
		}
		if (status != null) {
			query.addCriteria(new MatchCriteria(process.getColumn(getSQLGeneralCaseCaseStatusColumnName()), MatchCriteria.EQUALS, status));
		}
		if (anonymous != null) {
			query.addCriteria(new MatchCriteria(process.getColumn(getSQLGeneralCaseUserColumnName()), !anonymous.booleanValue()));
		}

		query.addOrder(new Order(process.getColumn(getSQLGeneralCaseCreatedColumnName()), true));

		return idoFindPKsByQuery(query);
	}

	public int ejbHomeGetCountByGroup(Collection groups) throws IDOException {
		return ejbHomeGetCountByGroupAndStatuses(groups, null);
	}

	public int ejbHomeGetCountByGroupAndStatuses(Collection groups, String[] statuses) throws IDOException {
		Table table = new Table(this);
		Table process = new Table(Case.class);

		SelectQuery query = new SelectQuery(table);
		query.addColumn(new CountColumn(table, getIDColumnName()));
		try {
			query.addJoin(table, process);
		}
		catch (IDORelationshipException e) {
			e.printStackTrace();
			throw new IDOException(e.getMessage());
		}
		query.addCriteria(new InCriteria(process.getColumn(getSQLGeneralCaseCaseStatusColumnName()), groups));
		if (statuses != null) {
			query.addCriteria(new InCriteria(process.getColumn(getSQLGeneralCaseCaseStatusColumnName()), statuses));
		}

		return idoGetNumberOfRecords(query);
	}
}