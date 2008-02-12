package is.idega.idegaweb.egov.cases.data;


import com.idega.core.file.data.ICFile;
import com.idega.block.process.data.Case;
import com.idega.user.data.User;
import com.idega.data.IDOEntity;

public interface GeneralCase extends IDOEntity, Case {

	/**
	 * @see is.idega.idegaweb.egov.cases.data.GeneralCaseBMPBean#getCaseCodeKey
	 */
	public String getCaseCodeKey();

	/**
	 * @see is.idega.idegaweb.egov.cases.data.GeneralCaseBMPBean#getCaseCodeDescription
	 */
	public String getCaseCodeDescription();

	/**
	 * @see is.idega.idegaweb.egov.cases.data.GeneralCaseBMPBean#getMessage
	 */
	public String getMessage();

	/**
	 * @see is.idega.idegaweb.egov.cases.data.GeneralCaseBMPBean#getReply
	 */
	public String getReply();

	/**
	 * @see is.idega.idegaweb.egov.cases.data.GeneralCaseBMPBean#getType
	 */
	public String getType();

	/**
	 * @see is.idega.idegaweb.egov.cases.data.GeneralCaseBMPBean#getCaseCategory
	 */
	public CaseCategory getCaseCategory();

	/**
	 * @see is.idega.idegaweb.egov.cases.data.GeneralCaseBMPBean#getCaseType
	 */
	public CaseType getCaseType();

	/**
	 * @see is.idega.idegaweb.egov.cases.data.GeneralCaseBMPBean#getAttachment
	 */
	public ICFile getAttachment();

	/**
	 * @see is.idega.idegaweb.egov.cases.data.GeneralCaseBMPBean#getHandledBy
	 */
	public User getHandledBy();

	/**
	 * @see is.idega.idegaweb.egov.cases.data.GeneralCaseBMPBean#isPrivate
	 */
	public boolean isPrivate();

	/**
	 * @see is.idega.idegaweb.egov.cases.data.GeneralCaseBMPBean#setMessage
	 */
	public void setMessage(String message);

	/**
	 * @see is.idega.idegaweb.egov.cases.data.GeneralCaseBMPBean#setReply
	 */
	public void setReply(String reply);

	/**
	 * @see is.idega.idegaweb.egov.cases.data.GeneralCaseBMPBean#setType
	 */
	public void setType(String type);

	/**
	 * @see is.idega.idegaweb.egov.cases.data.GeneralCaseBMPBean#setCaseCategory
	 */
	public void setCaseCategory(CaseCategory category);

	/**
	 * @see is.idega.idegaweb.egov.cases.data.GeneralCaseBMPBean#setCaseType
	 */
	public void setCaseType(CaseType type);

	/**
	 * @see is.idega.idegaweb.egov.cases.data.GeneralCaseBMPBean#setAttachment
	 */
	public void setAttachment(ICFile attachment);

	/**
	 * @see is.idega.idegaweb.egov.cases.data.GeneralCaseBMPBean#setHandledBy
	 */
	public void setHandledBy(User handler);

	/**
	 * @see is.idega.idegaweb.egov.cases.data.GeneralCaseBMPBean#setAsPrivate
	 */
	public void setAsPrivate(boolean isPrivate);
	
	public abstract void setCaseHandler(String handler);
	
	public abstract String getCaseHandler();
}