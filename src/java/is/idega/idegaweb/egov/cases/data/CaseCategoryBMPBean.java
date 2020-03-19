/*
 * $Id$
 * Created on Oct 30, 2005
 *
 * Copyright (C) 2005 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf.
 * Use is subject to license terms.
 */
package is.idega.idegaweb.egov.cases.data;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;

import javax.ejb.FinderException;
import javax.ejb.RemoveException;

import com.idega.block.text.data.LocalizedText;
import com.idega.core.localisation.business.ICLocaleBusiness;
import com.idega.data.GenericEntity;
import com.idega.data.IDORelationshipException;
import com.idega.data.query.Column;
import com.idega.data.query.Criteria;
import com.idega.data.query.MatchCriteria;
import com.idega.data.query.OR;
import com.idega.data.query.SelectQuery;
import com.idega.data.query.Table;
import com.idega.user.data.Group;

public class CaseCategoryBMPBean extends GenericEntity implements CaseCategory{

	private static final String ENTITY_NAME = "comm_case_category";

	private static final String COLUMN_NAME = "name";
	private static final String COLUMN_DESCRIPTION = "description";
	private static final String COLUMN_HANDLER_GROUP = "handler_group";
	private static final String COLUMN_ORDER = "category_order";
	private static final String COLUMN_PARENT_CATEGORY = "parent_category";
	private static final String COLUMN_DELETED = "deleted";
	public static final String COLUMN_HIDDEN = "hidden";

	@Override
	public String getEntityName() {
		return ENTITY_NAME;
	}

	@Override
	public void initializeAttributes() {
		addAttribute(getIDColumnName());

		addAttribute(COLUMN_NAME, "Name", String.class);
		addAttribute(COLUMN_DESCRIPTION, "Description", String.class, 4000);
		addAttribute(COLUMN_ORDER, "Order", Integer.class);
		addAttribute(COLUMN_DELETED, "Deleted", Boolean.class);
		addAttribute(COLUMN_HIDDEN, "hidden", Boolean.class);

		addManyToOneRelationship(COLUMN_HANDLER_GROUP, Group.class);
		setNullable(COLUMN_HANDLER_GROUP, false);

		addManyToOneRelationship(COLUMN_PARENT_CATEGORY, CaseCategory.class);

		//localization
		addManyToManyRelationShip(LocalizedText.class);
	}

	//Getters
	@Override
	public String getName() {
		return getStringColumnValue(COLUMN_NAME);
	}

	@Override
	public String getDescription() {
		return getStringColumnValue(COLUMN_DESCRIPTION);
	}

	@Override
	public Group getHandlerGroup() {
		return (Group) getColumnValue(COLUMN_HANDLER_GROUP);
	}

	@Override
	public int getOrder() {
		return getIntColumnValue(COLUMN_ORDER);
	}

	@Override
	public CaseCategory getParent() {
		return (CaseCategory) getColumnValue(COLUMN_PARENT_CATEGORY);
	}

	//Setters
	@Override
	public void setName(String name) {
		setColumn(COLUMN_NAME, name);
	}

	@Override
	public void setDescription(String description) {
		setColumn(COLUMN_DESCRIPTION, description);
	}

	@Override
	public void setHandlerGroup(Group group) {
		setColumn(COLUMN_HANDLER_GROUP, group);
	}

	@Override
	public void setHandlerGroup(Object groupPK) {
		setColumn(COLUMN_HANDLER_GROUP, groupPK);
	}

	@Override
	public void setOrder(int order) {
		setColumn(COLUMN_ORDER, order);
	}

	@Override
	public void setParent(CaseCategory category) {
		setColumn(COLUMN_PARENT_CATEGORY, category);
	}

	//Finders
	public Collection ejbFindAll() throws FinderException {
		Table table = new Table(this);

		SelectQuery query = new SelectQuery(table);
		query.addColumn(table, getIDColumnName());
		query.addOrder(table, COLUMN_ORDER, true);
		addNotDeletedCriteria(table, query);

		return idoFindPKsByQuery(query);
	}

	public Collection ejbFindAllTopLevelCategories() throws FinderException {
		Table table = new Table(this);

		SelectQuery query = new SelectQuery(table);
		query.addColumn(table, getIDColumnName());
		query.addCriteria(new MatchCriteria(table.getColumn(COLUMN_PARENT_CATEGORY)));
		query.addOrder(table, COLUMN_ORDER, true);
		addNotDeletedCriteria(table, query);

		return idoFindPKsByQuery(query);
	}
	public Collection ejbFindAllTopLevelCategoriesForAdmins() throws FinderException {
		Table table = new Table(this);

		SelectQuery query = new SelectQuery(table);
		query.addColumn(table, getIDColumnName());
		query.addCriteria(new MatchCriteria(table.getColumn(COLUMN_PARENT_CATEGORY)));
		query.addOrder(table, COLUMN_ORDER, true);
		Criteria isNull = new MatchCriteria(new Column(table, COLUMN_DELETED), MatchCriteria.IS, MatchCriteria.NULL);
		Criteria notEquals = new MatchCriteria(new Column(table, COLUMN_DELETED), MatchCriteria.NOTEQUALS, true);
		query.addCriteria(new OR(isNull, notEquals));

		return idoFindPKsByQuery(query);
	}

	public Collection ejbFindAllByName(String categoryName) throws FinderException {
		Table table = new Table(this);

		SelectQuery query = new SelectQuery(table);
		query.addColumn(table, getIDColumnName());
		query.addCriteria(new MatchCriteria(table.getColumn(COLUMN_NAME), MatchCriteria.EQUALS, categoryName));
		addNotDeletedCriteria(table, query);

		return idoFindPKsByQuery(query);
	}

	public Collection<?> ejbFindByGroupId(Integer groupId) throws FinderException {
		Table table = new Table(this);

		SelectQuery query = new SelectQuery(table);
		query.addColumn(table, getIDColumnName());
		query.addCriteria(new MatchCriteria(table.getColumn(COLUMN_HANDLER_GROUP), MatchCriteria.EQUALS, groupId));
		addNotDeletedCriteria(table, query);

		return idoFindPKsByQuery(query);
	}

	public Collection ejbFindAllSubCategories(CaseCategory category) throws FinderException {
		Table table = new Table(this);

		SelectQuery query = new SelectQuery(table);
		query.addColumn(table, getIDColumnName());
		query.addCriteria(new MatchCriteria(table.getColumn(COLUMN_PARENT_CATEGORY), MatchCriteria.EQUALS, category));
		addNotDeletedCriteria(table, query);
		query.addOrder(table, COLUMN_ORDER, true);
		return idoFindPKsByQuery(query);
	}

	private void addNotDeletedCriteria(Table table, SelectQuery query) {
//		Deleted
		Criteria isNull = new MatchCriteria(new Column(table, COLUMN_DELETED), MatchCriteria.IS, MatchCriteria.NULL);
		Criteria notEquals = new MatchCriteria(new Column(table, COLUMN_DELETED), MatchCriteria.NOTEQUALS, true);
		query.addCriteria(new OR(isNull, notEquals));
//		Hidden
		isNull = new MatchCriteria(new Column(table, COLUMN_HIDDEN), MatchCriteria.IS, MatchCriteria.NULL);
		notEquals = new MatchCriteria(new Column(table, COLUMN_HIDDEN), MatchCriteria.NOTEQUALS, true);
		query.addCriteria(new OR(isNull, notEquals));
	}

	@Override
	public String getLocalizedCategoryName(Locale locale){
		LocalizedText text = getLocalizedText(locale);

		if(text!=null){
			return text.getHeadline();
		}
		else{
			return this.getName();
		}
	}

	@Override
	public String getLocalizedCategoryDescription(Locale locale){
		LocalizedText text = getLocalizedText(locale);

		if(text!=null){
			return text.getBody();
		}
		else{
			return this.getDescription();
		}
	}

	@Override
	public void addLocalization(LocalizedText localizedText) throws SQLException{
		this.addTo(localizedText);
	}

	@Override
	public LocalizedText getLocalizedText(int icLocaleId){
		Collection locales = null;
		LocalizedText text = null;

		try {
			locales = idoGetRelatedEntities(LocalizedText.class);
		} catch (IDORelationshipException e) {
			return null;
		}


		for (Iterator iter = locales.iterator(); iter.hasNext();) {
			text = (LocalizedText) iter.next();
			if(text.getLocaleId()==icLocaleId){
				return text;
			}
		}

		return null;
	}


	@Override
	public LocalizedText getLocalizedText(Locale locale){
		return getLocalizedText(ICLocaleBusiness.getLocaleId(locale));
	}

	/* (non-Javadoc)
	 * @see com.idega.data.GenericEntity#remove()
	 */
	@Override
	public void remove() throws RemoveException {
		//get rid of localizations first
		Collection locales = null;
		LocalizedText text = null;



		try {
			locales = idoGetRelatedEntities(LocalizedText.class);
		} catch (IDORelationshipException e) {

		}


		for (Iterator iter = locales.iterator(); iter.hasNext();) {
			text = (LocalizedText) iter.next();
			try {
				this.removeFrom(text);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			text.remove();
		}


		super.remove();
	}

	@Override
	public boolean isDeleted() {
		return getBooleanColumnValue(COLUMN_DELETED);
	}

	@Override
	public void setDeleted(boolean deleted) {
		setValue(COLUMN_DELETED, deleted);
	}

	@Override
	public boolean isHidden(){
		return getBooleanColumnValue(COLUMN_HIDDEN);
	}

	@Override
	public void setHidden(boolean hidden){
		setValue(COLUMN_HIDDEN, hidden);
	}

}