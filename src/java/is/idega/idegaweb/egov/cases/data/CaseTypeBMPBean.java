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

import java.util.Collection;
import javax.ejb.FinderException;
import com.idega.data.GenericEntity;
import com.idega.data.query.SelectQuery;
import com.idega.data.query.Table;
import com.idega.user.data.Group;


public class CaseTypeBMPBean extends GenericEntity implements CaseType{

	private static final String ENTITY_NAME = "comm_case_type";
	
	private static final String COLUMN_NAME = "name";
	private static final String COLUMN_DESCRIPTION = "description";
	private static final String COLUMN_HANDLER_GROUP = "handler_group";
	private static final String COLUMN_REQUIRES_CASE_NUMBER = "requires_case_number";
	
	public String getEntityName() {
		return ENTITY_NAME;
	}

	public void initializeAttributes() {
		addAttribute(getIDColumnName());

		addAttribute(COLUMN_NAME, "Name", String.class);
		addAttribute(COLUMN_DESCRIPTION, "Description", String.class);
		
		addManyToOneRelationship(COLUMN_HANDLER_GROUP, Group.class);
		setNullable(COLUMN_HANDLER_GROUP, false);
		
		addAttribute(COLUMN_REQUIRES_CASE_NUMBER, "Requires case number", Boolean.class);
	}
	
	//Getters
	public String getName() {
		return getStringColumnValue(COLUMN_NAME);
	}
	
	public String getDescription() {
		return getStringColumnValue(COLUMN_DESCRIPTION);
	}
	
	public Group getHandlerGroup() {
		return (Group) getColumnValue(COLUMN_HANDLER_GROUP);
	}
	
	public boolean requiresCaseNumber() {
		return getBooleanColumnValue(COLUMN_REQUIRES_CASE_NUMBER, false);
	}
	
	//Setters
	public void setName(String name) {
		setColumn(COLUMN_NAME, name);
	}
	
	public void setDescription(String description) {
		setColumn(COLUMN_DESCRIPTION, description);
	}
	
	public void setHandlerGroup(Group group) {
		setColumn(COLUMN_HANDLER_GROUP, group);
	}
	
	public void setHandlerGroup(Object groupPK) {
		setColumn(COLUMN_HANDLER_GROUP, groupPK);
	}
	
	public void setRequiresCaseNumber(boolean requiresCaseNumber) {
		setColumn(COLUMN_REQUIRES_CASE_NUMBER, requiresCaseNumber);
	}
	
	//Finders
	public Collection ejbFindAll() throws FinderException {
		Table table = new Table(this);
		
		SelectQuery query = new SelectQuery(table);
		query.addColumn(table, getIDColumnName(), true);
		
		return idoFindPKsByQuery(query);
	}
}