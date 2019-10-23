/*
 * $Id$ Created on Oct 30, 2005
 *
 * Copyright (C) 2005 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf. Use is subject to license terms.
 */
package is.idega.idegaweb.egov.cases.data;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import javax.ejb.FinderException;

import com.idega.block.process.data.AbstractCaseBMPBean;
import com.idega.block.process.data.Case;
import com.idega.block.process.data.CaseBMPBean;
import com.idega.block.process.data.CaseStatus;
import com.idega.core.file.data.ICFile;
import com.idega.data.IDOException;
import com.idega.data.IDORelationshipException;
import com.idega.data.query.BetweenCriteria;
import com.idega.data.query.Column;
import com.idega.data.query.CountColumn;
import com.idega.data.query.Criteria;
import com.idega.data.query.InCriteria;
import com.idega.data.query.MatchCriteria;
import com.idega.data.query.OR;
import com.idega.data.query.Order;
import com.idega.data.query.SelectQuery;
import com.idega.data.query.Table;
import com.idega.data.query.range.DateRange;
import com.idega.user.data.Group;
import com.idega.user.data.User;
import com.idega.util.CoreUtil;
import com.idega.util.IWTimestamp;
import com.idega.util.ListUtil;
import com.idega.util.StringUtil;

import is.idega.idegaweb.egov.cases.util.CasesConstants;

public class GeneralCaseBMPBean extends AbstractCaseBMPBean implements Case, GeneralCase {

	private static final long serialVersionUID = 1213681239602561355L;

	public static final String ENTITY_NAME = "comm_case";

	private static final String COLUMN_MESSAGE = "message";
	private static final String COLUMN_REPLY = "reply";
	private static final String COLUMN_CASE_CATEGORY = "case_category";
	private static final String COLUMN_CASE_TYPE = "case_type";
	private static final String COLUMN_FILE = "ic_file_id";
	private static final String RELATION_FILES = ENTITY_NAME + "_ic_file";
	private static final String COLUMN_TYPE = "type";
	public static final String COLUMN_HANDLER = "handler";
	private static final String COLUMN_IS_PRIVATE = "is_private";
	private static final String COLUMN_IS_ANONYMOUS = "is_anonymous";
	private static final String COLUMN_PRIORITY = "priority";
	private static final String COLUMN_TITLE = "title";
	private static final String COLUMN_WANT_REPLY = "want_reply";
	private static final String COLUMN_WANT_REPLY_EMAIL = "want_reply_email";
	private static final String COLUMN_WANT_REPLY_PHONE = "want_reply_phone";
	private static final String COLUMN_CASE_SUBSCRIBERS = ENTITY_NAME + "_subscribers";

	private static final String COLUMN_REFERENCE = "reference";

	/*
	 * (non-Javadoc)
	 *
	 * @see com.idega.block.process.data.AbstractCaseBMPBean#getCaseCodeKey()
	 */
	@Override
	public String getCaseCodeKey() {
		return CasesConstants.CASE_CODE_KEY;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.idega.block.process.data.AbstractCaseBMPBean#getCaseCodeDescription()
	 */
	@Override
	public String getCaseCodeDescription() {
		return "General case";
	}

	@Override
	public String getEntityName() {
		return ENTITY_NAME;
	}

	@Override
	public void initializeAttributes() {
		addGeneralCaseRelation();

		addAttribute(COLUMN_MESSAGE, "Message", String.class, 4000);
		addAttribute(COLUMN_REPLY, "Reply", String.class, 4000);
		addAttribute(COLUMN_TYPE, "Type", String.class);
		addAttribute(COLUMN_IS_PRIVATE, "Is private", Boolean.class);
		addAttribute(COLUMN_IS_ANONYMOUS, "Is anonymous", Boolean.class);
		addAttribute(COLUMN_PRIORITY, "Priority", String.class);
		addAttribute(COLUMN_TITLE, "Title", String.class);
		addAttribute(COLUMN_WANT_REPLY, "Want reply", String.class);
		addAttribute(COLUMN_WANT_REPLY_EMAIL, "Email reply", String.class);
		addAttribute(COLUMN_WANT_REPLY_PHONE, "Phone reply", String.class);

		addManyToOneRelationship(COLUMN_CASE_CATEGORY, CaseCategory.class);
		addManyToOneRelationship(COLUMN_CASE_TYPE, CaseType.class);
		addManyToOneRelationship(COLUMN_FILE, ICFile.class);
		addManyToOneRelationship(COLUMN_HANDLER, User.class);
		addManyToManyRelationShip(User.class, COLUMN_CASE_SUBSCRIBERS);
		addManyToManyRelationShip(ICFile.class, RELATION_FILES);
		getEntityDefinition().setBeanCachingActiveByDefault(true, 1000);

		addAttribute(COLUMN_REFERENCE, "Reference", String.class);

		super.initializeAttributes();
	}

	@Override
	public void removeAllAttachments(){
		try{
			idoRemoveFrom(ICFile.class);
		}catch (Exception e) {
			getLogger().log(Level.WARNING, "Failed removing files from case " + getPrimaryKey(), e);
		}
	}
	@Override
	public void removeAttachment(ICFile file){
		try{
			idoRemoveFrom(file);
		}catch (Exception e) {
			getLogger().log(Level.WARNING, "Failed removing file " + file + " from case " + getPrimaryKey(), e);
		}
	}
	@Override
	public void addAttachment(ICFile file){
		try{
			idoAddTo(file);
		}catch (Exception e) {
			getLogger().log(Level.WARNING, "Failed adding file " + file + " to case " + getPrimaryKey(), e);
		}
	}

	@Override
	public Collection<ICFile> getAttachments(){
		try {
			return idoGetRelatedEntities(ICFile.class);
		} catch (IDORelationshipException e) {
			getLogger().log(Level.WARNING, "Failed getting files of case " + getPrimaryKey(), e);
		}
		return Collections.emptyList();
	}

	// Getters
	@Override
	public String getMessage() {
		return getStringColumnValue(COLUMN_MESSAGE);
	}

	@Override
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

	@Override
	public String getType() {
		return getStringColumnValue(COLUMN_TYPE);
	}

	@Override
	public CaseCategory getCaseCategory() {
		return (CaseCategory) getColumnValue(COLUMN_CASE_CATEGORY);
	}

	@Override
	public CaseType getCaseType() {
		return (CaseType) getColumnValue(COLUMN_CASE_TYPE);
	}

	public ICFile getAttachment() {
		return (ICFile) getColumnValue(COLUMN_FILE);
	}

	@Override
	public User getHandledBy() {
		return (User) getColumnValue(COLUMN_HANDLER);
	}

	@Override
	public boolean isPrivate() {
		return getBooleanColumnValue(COLUMN_IS_PRIVATE, false);
	}

	@Override
	public boolean isAnonymous() {
		return getBooleanColumnValue(COLUMN_IS_ANONYMOUS, false);
	}

	@Override
	public String getPriority() {
		return getStringColumnValue(COLUMN_PRIORITY);
	}

	@Override
	public String getTitle() {
		return getStringColumnValue(COLUMN_TITLE);
	}

	@Override
	public String getWantReply() {
		return getStringColumnValue(COLUMN_WANT_REPLY);
	}

	@Override
	public String getWantReplyEmail() {
		return getStringColumnValue(COLUMN_WANT_REPLY_EMAIL);
	}

	@Override
	public String getWantReplyPhone() {
		return getStringColumnValue(COLUMN_WANT_REPLY_PHONE);
	}

	@Override
	public String getReference() {
		return getStringColumnValue(COLUMN_REFERENCE);
	}

	// Setters
	@Override
	public void setMessage(String message) {
		setColumn(COLUMN_MESSAGE, message);
	}

	@Override
	public void setReply(String reply) {
		setColumn(COLUMN_REPLY, reply);
	}

	@Override
	public void setType(String type) {
		setColumn(COLUMN_TYPE, type);
	}

	@Override
	public void setCaseCategory(CaseCategory category) {
		setColumn(COLUMN_CASE_CATEGORY, category);
	}

	@Override
	public void setCaseType(CaseType type) {
		setColumn(COLUMN_CASE_TYPE, type);
	}

	public void setAttachment(ICFile attachment) {
		setColumn(COLUMN_FILE, attachment);
	}

	@Override
	public void setHandledBy(User handler) {
		setColumn(COLUMN_HANDLER, handler);
	}

	@Override
	public void setAsPrivate(boolean isPrivate) {
		setColumn(COLUMN_IS_PRIVATE, isPrivate);
	}

	@Override
	public void setAsAnonymous(boolean isAnonymous) {
		setColumn(COLUMN_IS_ANONYMOUS, isAnonymous);
	}

	@Override
	public void setPriority(String priority) {
		setColumn(COLUMN_PRIORITY, priority);
	}

	@Override
	public void setTitle(String title) {
		setColumn(COLUMN_TITLE, title);
	}

	@Override
	public void setWantReply(String wantReply) {
		setColumn(COLUMN_WANT_REPLY, wantReply);
	}

	@Override
	public void setWantReplyEmail(String wantReplyEmail) {
		setColumn(COLUMN_WANT_REPLY_EMAIL, wantReplyEmail);
	}

	@Override
	public void setWantReplyPhone(String wantReplyPhone) {
		setColumn(COLUMN_WANT_REPLY_PHONE, wantReplyPhone);
	}

	@Override
	public void setReference(String reference) {
		setColumn(COLUMN_REFERENCE, reference);
	}

	// Finders
	public Collection ejbFindAllByGroup(Collection groups) throws FinderException {
		return ejbFindAllByGroupAndStatuses(groups, null, null);
	}

	/**
	 *
	 * @param groups
	 * @param statuses
	 * @param caseManagerType - if caseHandlers is null, then it is not added to criteria list, but if it's empty, then the criteria is considered to be IS NULL
	 * @return
	 * @throws FinderException
	 */
	public Collection ejbFindAllByGroupAndStatuses(Collection groups, String[] statuses, String[] caseManagerType) throws FinderException {
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
		if (groups != null) {
			query.addCriteria(new InCriteria(process.getColumn(getSQLGeneralCaseHandlerColumnName()), groups));
		}
		if (statuses != null) {
			query.addCriteria(new InCriteria(process.getColumn(getSQLGeneralCaseCaseStatusColumnName()), statuses));
		}

		if (caseManagerType != null) {

			if(caseManagerType.length == 0) {

				query.addCriteria(new MatchCriteria(process.getColumn(getSQLGeneralCaseCaseManagerTypeColumnName())));

			} else {

				query.addCriteria(new InCriteria(process.getColumn(getSQLGeneralCaseCaseManagerTypeColumnName()), caseManagerType));
			}
		}

		log(Level.INFO, query.toString());
		return idoFindPKsByQuery(query);
	}

	public Collection ejbFindAllByHandler(User handler) throws FinderException {
		return ejbFindAllByHandlerAndStatuses(handler, null, null);
	}

	/**
	 *
	 * @param handler
	 * @param statuses
	 * @param caseManagerType - if caseHandlers is null, then it is not added to criteria list, but if it's empty, then the criteria is considered to be IS NULL
	 * @return
	 * @throws FinderException
	 */
	public Collection ejbFindAllByHandlerAndStatuses(User handler, String[] statuses, String[] caseManagerType) throws FinderException {
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

		if (caseManagerType != null) {

			if(caseManagerType.length == 0) {

				query.addCriteria(new MatchCriteria(process.getColumn(getSQLGeneralCaseCaseManagerTypeColumnName())));

			} else {

				query.addCriteria(new InCriteria(process.getColumn(getSQLGeneralCaseCaseManagerTypeColumnName()), caseManagerType));
			}
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

	public Collection ejbFindAllByMessage(String message) throws FinderException {
		Table table = new Table(this);

		SelectQuery query = new SelectQuery(table);
		query.addColumn(table, getIDColumnName(), true);
		query.addCriteria(new MatchCriteria(table.getColumn(COLUMN_MESSAGE), MatchCriteria.LIKE, "%" + message + "%"));

		return idoFindPKsByQuery(query);
	}

	public Collection ejbFindByCriteria(CaseCategory parentCategory, CaseCategory category, CaseType type, CaseStatus status, Boolean anonymous) throws FinderException {
		return ejbFindByCriteria(parentCategory, category, type, status, anonymous, null);
	}

	public Collection ejbFindByCriteria(CaseCategory parentCategory, CaseCategory category, CaseType type, CaseStatus status, Date fromDate, Date toDate, Boolean anonymous) throws FinderException {
		Table table = new Table(this);
		Table process = new Table(Case.class);
		Table categories = new Table(CaseCategory.class);

		SelectQuery query = new SelectQuery(table);
		query.addColumn(table, getIDColumnName());
		try {
			query.addJoin(table, process);
		}
		catch (IDORelationshipException e) {
			e.printStackTrace();
			throw new FinderException(e.getMessage());
		}

		if (parentCategory != null) {
			if (category == null) {
				try {
					query.addJoin(table, categories);
				}
				catch (IDORelationshipException e) {
					e.printStackTrace();
					throw new FinderException(e.getMessage());
				}
				query.addCriteria(new MatchCriteria(categories.getColumn("parent_category"), MatchCriteria.EQUALS, parentCategory));
			}
			else {
				query.addCriteria(new MatchCriteria(table.getColumn(COLUMN_CASE_CATEGORY), MatchCriteria.EQUALS, category));
			}
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
		if (fromDate != null) {
			query.addCriteria(new MatchCriteria(process.getColumn(getSQLGeneralCaseCreatedColumnName()), MatchCriteria.GREATEREQUAL, fromDate));
		}
		if (toDate != null) {
			query.addCriteria(new MatchCriteria(process.getColumn(getSQLGeneralCaseCreatedColumnName()), MatchCriteria.LESSEQUAL, toDate));
		}

		query.addOrder(new Order(process.getColumn(getSQLGeneralCaseCreatedColumnName()), true));

		boolean measureSQL = CoreUtil.isSQLMeasurementOn();
		long start = measureSQL ? System.currentTimeMillis() : 0;
		try {
			return idoFindPKsByQuery(query);
		} finally {
			if (measureSQL) {
				CoreUtil.doDebugSQL(start, System.currentTimeMillis(), query.toString());
			}
		}
	}

	public Collection ejbFindByCriteria(CaseCategory parentCategory, CaseCategory category, CaseType type, CaseStatus status, Boolean anonymous, String caseManagerType) throws FinderException {

		Table table = new Table(this);
		Table process = new Table(Case.class);
		Table categories = new Table(CaseCategory.class);

		SelectQuery query = new SelectQuery(table);
		query.addColumn(table, getIDColumnName());
		try {
			query.addJoin(table, process);
		}
		catch (IDORelationshipException e) {
			e.printStackTrace();
			throw new FinderException(e.getMessage());
		}

		if (parentCategory == null) {
			if (category != null) {
				query.addCriteria(new MatchCriteria(table.getColumn(COLUMN_CASE_CATEGORY), MatchCriteria.EQUALS, category));
			}
		}
		else if (category == null) {
			try {
				query.addJoin(table, categories);
			}
			catch (IDORelationshipException e) {
				e.printStackTrace();
				throw new FinderException(e.getMessage());
			}
			query.addCriteria(new MatchCriteria(categories.getColumn("parent_category"), MatchCriteria.EQUALS, parentCategory));
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
		if (caseManagerType != null) {
			query.addCriteria(new MatchCriteria(process.getColumn(getSQLGeneralCaseCaseManagerTypeColumnName()), MatchCriteria.EQUALS, caseManagerType));
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

	public Collection<Integer> ejbFindByCriteria(
			String caseNumber,
			String description,
			Collection<String> owners,
			String[] statuses,
			IWTimestamp dateFrom,
			IWTimestamp dateTo,
			User owner,
			Collection<Group> groups,
			boolean simpleCases
	) throws FinderException {
		return ejbFindByCriteria(caseNumber, description, owners, statuses, dateFrom, dateTo, owner, groups, simpleCases, null, null, null);
	}

	public Collection<Integer> ejbFindByCriteria(
			String caseNumber,
			String description,
			Collection<String> owners,
			String[] statuses,
			IWTimestamp dateFrom,
			IWTimestamp dateTo,
			User owner,
			Collection<Group> groups,
			boolean simpleCases,
			Boolean withHandler,
			List<Integer> exceptOwnersIds,
			String caseCode
	) throws FinderException {

		Table generalCasesTable = new Table(this);
		Table casesTable = new Table(Case.class);

		SelectQuery query = new SelectQuery(generalCasesTable);
		query.addColumn(generalCasesTable.getColumn(getIDColumnName()));
		try {
			query.addJoin(generalCasesTable, casesTable);
		}
		catch (IDORelationshipException e) {
			e.printStackTrace();
			throw new FinderException(e.getMessage());
		}

		if (owner != null) {
			query.addCriteria(new MatchCriteria(casesTable.getColumn(CaseBMPBean.COLUMN_USER), MatchCriteria.EQUALS, owner.getId()));
		}
		if (!ListUtil.isEmpty(groups)) {
			List<String> groupsIds = new ArrayList<String>(groups.size());
			for (Group group: groups) {
				groupsIds.add(group.getId());
			}

			Criteria handlersGroupsCriteria = new InCriteria(casesTable.getColumn(getSQLGeneralCaseHandlerColumnName()), groupsIds);
			Criteria handlersCriteria = new InCriteria(generalCasesTable.getColumn(COLUMN_HANDLER), groupsIds);
			query.addCriteria(new OR(handlersGroupsCriteria, handlersCriteria));
		}
		if (caseNumber != null) {
			Column column = new Column(casesTable, casesTable.getColumn(CaseBMPBean.COLUMN_CASE_IDENTIFIER).getName());
			column.setPrefix("lower(");
			column.setPostfix(")");
			query.addCriteria(new MatchCriteria(column, MatchCriteria.LIKE, true, caseNumber));
		}
		if (description != null) {
			Column column = casesTable.getColumn(CaseBMPBean.COLUMN_CASE_SUBJECT);
			column.setPrefix("lower(");
			column.setPostfix(")");
			query.addCriteria(new MatchCriteria(column, MatchCriteria.LIKE, true, description));
		}
		if (!ListUtil.isEmpty(owners) && owner == null) {
			query.addCriteria(new InCriteria(casesTable.getColumn(CaseBMPBean.COLUMN_USER), owners));
		}
		if (statuses != null && statuses.length > 0) {
			query.addCriteria(new InCriteria(casesTable.getColumn(getSQLGeneralCaseCaseStatusColumnName()), statuses));
		}
		if (dateFrom != null && dateTo != null) {
			query.addCriteria(new BetweenCriteria(casesTable.getColumn(CaseBMPBean.COLUMN_CREATED), new DateRange(dateFrom.getDate(), dateTo.getDate())));
		} else {
			if (dateFrom != null) {
				query.addCriteria(new MatchCriteria(casesTable.getColumn(CaseBMPBean.COLUMN_CREATED), MatchCriteria.GREATEREQUAL, dateFrom.getDate()));
			}
			if (dateTo != null) {
				query.addCriteria(new MatchCriteria(casesTable.getColumn(CaseBMPBean.COLUMN_CREATED), MatchCriteria.LESSEQUAL, dateTo.getDate()));
			}
		}
		if (simpleCases) {
			query.addCriteria(new MatchCriteria(casesTable.getColumn(CaseBMPBean.COLUMN_CASE_MANAGER_TYPE), MatchCriteria.IS, MatchCriteria.NULL));
		}
		if (withHandler != null) {
			if (withHandler.booleanValue()) {
				query.addCriteria(new MatchCriteria(generalCasesTable.getColumn(COLUMN_HANDLER), MatchCriteria.ISNOT, MatchCriteria.NULL));
			} else {
				query.addCriteria(new MatchCriteria(generalCasesTable.getColumn(COLUMN_HANDLER), MatchCriteria.IS, MatchCriteria.NULL));
			}
		}

		if (!ListUtil.isEmpty(exceptOwnersIds)) {
			query.addCriteria(new InCriteria(casesTable.getColumn(CaseBMPBean.COLUMN_USER), exceptOwnersIds, true));
		}

		if (!StringUtil.isEmpty(caseCode)) {
			query.addCriteria(new MatchCriteria(casesTable.getColumn(CaseBMPBean.COLUMN_CASE_CODE), MatchCriteria.EQUALS, caseCode));
		}

		query.addGroupByColumn(generalCasesTable.getColumn(getIDColumnName()));

		boolean measureSQL = CoreUtil.isSQLMeasurementOn();
		long start = measureSQL ? System.currentTimeMillis() : 0;
		try {
			return idoFindPKsByQuery(query);
		} finally {
			if (measureSQL) {
				CoreUtil.doDebugSQL(start, System.currentTimeMillis(), query.toString());
			}
		}
	}

	public Collection ejbFindAllByIds(Collection<Integer> ids) throws FinderException {
		Table generalCasesTable = new Table(this);
		Table casesTable = new Table(Case.class);

		SelectQuery query = new SelectQuery(generalCasesTable);
		String[] columnNames = getColumnNames();
		for (String columnName: columnNames) {
			query.addColumn(generalCasesTable, columnName);
		}
		try {
			query.addJoin(generalCasesTable, casesTable);
		} catch (IDORelationshipException e) {
			throw new FinderException("Could not add join: " + e);
		}
		if (!ListUtil.isEmpty(ids)) {
			query.addCriteria(new InCriteria(generalCasesTable.getColumn(getIDColumnName()), ids));
		}
		query.addOrder(casesTable, CaseBMPBean.COLUMN_CREATED, false);
		return idoFindPKsByQuery(query);
	}

	public Collection<Integer> getCasesWithONeToOneAttachments(int start,int max) throws FinderException{
		Table generalCasesTable = new Table(this);

		SelectQuery query = new SelectQuery(generalCasesTable);
		query.addColumn(generalCasesTable, getIDColumnName());
		query.addCriteria(new MatchCriteria(generalCasesTable, COLUMN_FILE, MatchCriteria.ISNOT, MatchCriteria.NULL));
		return idoFindPKsByQuery(query, max, start);
	}
	/*
	 * (non-Javadoc)
	 * @see is.idega.idegaweb.egov.cases.data.GeneralCase#isSubscribed(com.idega.user.data.User)
	 */
	@Override
	public boolean isSubscribed(User user) {
		if (user == null) {
			return Boolean.FALSE;
		}

		Collection<User> subscribers = getSubscribers();
		if (!ListUtil.isEmpty(subscribers)) {
			for (User subscriber : subscribers) {
				if (subscriber.equals(user)) {
					return Boolean.TRUE;
				}
			}
		}

		return Boolean.FALSE;
	}
}