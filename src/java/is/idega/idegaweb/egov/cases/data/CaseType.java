package is.idega.idegaweb.egov.cases.data;


import com.idega.data.IDOEntity;

public interface CaseType extends IDOEntity {

	/**
	 * @see is.idega.idegaweb.egov.cases.data.CaseTypeBMPBean#getName
	 */
	public String getName();

	/**
	 * @see is.idega.idegaweb.egov.cases.data.CaseTypeBMPBean#getDescription
	 */
	public String getDescription();

	/**
	 * @see is.idega.idegaweb.egov.cases.data.CaseTypeBMPBean#getOrder
	 */
	public int getOrder();

	/**
	 * @see is.idega.idegaweb.egov.cases.data.CaseTypeBMPBean#setName
	 */
	public void setName(String name);

	/**
	 * @see is.idega.idegaweb.egov.cases.data.CaseTypeBMPBean#setDescription
	 */
	public void setDescription(String description);

	/**
	 * @see is.idega.idegaweb.egov.cases.data.CaseTypeBMPBean#setOrder
	 */
	public void setOrder(int order);
}