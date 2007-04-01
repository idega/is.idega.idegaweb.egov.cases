package is.idega.idegaweb.egov.cases.data;


import java.sql.SQLException;
import java.util.Locale;

import com.idega.block.text.data.LocalizedText;
import com.idega.data.IDOEntity;
import com.idega.user.data.Group;

public interface CaseCategory extends IDOEntity {
	/**
	 * @see is.idega.idegaweb.egov.cases.data.CaseCategoryBMPBean#getName
	 */
	public String getName();

	/**
	 * @see is.idega.idegaweb.egov.cases.data.CaseCategoryBMPBean#getDescription
	 */
	public String getDescription();

	/**
	 * @see is.idega.idegaweb.egov.cases.data.CaseCategoryBMPBean#getHandlerGroup
	 */
	public Group getHandlerGroup();

	/**
	 * @see is.idega.idegaweb.egov.cases.data.CaseCategoryBMPBean#getOrder
	 */
	public int getOrder();

	/**
	 * @see is.idega.idegaweb.egov.cases.data.CaseCategoryBMPBean#getParent
	 */
	public CaseCategory getParent();

	/**
	 * @see is.idega.idegaweb.egov.cases.data.CaseCategoryBMPBean#setName
	 */
	public void setName(String name);

	/**
	 * @see is.idega.idegaweb.egov.cases.data.CaseCategoryBMPBean#setDescription
	 */
	public void setDescription(String description);

	/**
	 * @see is.idega.idegaweb.egov.cases.data.CaseCategoryBMPBean#setHandlerGroup
	 */
	public void setHandlerGroup(Group group);

	/**
	 * @see is.idega.idegaweb.egov.cases.data.CaseCategoryBMPBean#setHandlerGroup
	 */
	public void setHandlerGroup(Object groupPK);

	/**
	 * @see is.idega.idegaweb.egov.cases.data.CaseCategoryBMPBean#setOrder
	 */
	public void setOrder(int order);

	/**
	 * @see is.idega.idegaweb.egov.cases.data.CaseCategoryBMPBean#setParent
	 */
	public void setParent(CaseCategory category);

	/**
	 * @see is.idega.idegaweb.egov.cases.data.CaseCategoryBMPBean#getLocalizedCategoryName
	 */
	public String getLocalizedCategoryName(Locale locale);

	/**
	 * @see is.idega.idegaweb.egov.cases.data.CaseCategoryBMPBean#getLocalizedCategoryDescription
	 */
	public String getLocalizedCategoryDescription(Locale locale);

	/**
	 * @see is.idega.idegaweb.egov.cases.data.CaseCategoryBMPBean#addLocalization
	 */
	public void addLocalization(LocalizedText localizedText) throws SQLException;

	/**
	 * @see is.idega.idegaweb.egov.cases.data.CaseCategoryBMPBean#getLocalizedText
	 */
	public LocalizedText getLocalizedText(int icLocaleId);

	/**
	 * @see is.idega.idegaweb.egov.cases.data.CaseCategoryBMPBean#getLocalizedText
	 */
	public LocalizedText getLocalizedText(Locale locale);
}