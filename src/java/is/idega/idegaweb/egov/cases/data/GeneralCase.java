package is.idega.idegaweb.egov.cases.data;


import com.idega.core.file.data.ICFile;
import com.idega.data.IDOAddRelationshipException;
import java.util.Collection;
import com.idega.block.process.data.Case;
import com.idega.user.data.User;
import com.idega.data.IDORemoveRelationshipException;
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
	 * @see is.idega.idegaweb.egov.cases.data.GeneralCaseBMPBean#isAnonymous
	 */
	public boolean isAnonymous();

	/**
	 * @see is.idega.idegaweb.egov.cases.data.GeneralCaseBMPBean#getPriority
	 */
	public String getPriority();

	/**
	 * @see is.idega.idegaweb.egov.cases.data.GeneralCaseBMPBean#getTitle
	 */
	public String getTitle();

	/**
	 * @see is.idega.idegaweb.egov.cases.data.GeneralCaseBMPBean#getWantReply
	 */
	public String getWantReply();

	/**
	 * @see is.idega.idegaweb.egov.cases.data.GeneralCaseBMPBean#getWantReplyEmail
	 */
	public String getWantReplyEmail();

	/**
	 * @see is.idega.idegaweb.egov.cases.data.GeneralCaseBMPBean#getWantReplyPhone
	 */
	public String getWantReplyPhone();

	/**
	 * @see is.idega.idegaweb.egov.cases.data.GeneralCaseBMPBean#getName
	 */
	public String getName();

	/**
	 * @see is.idega.idegaweb.egov.cases.data.GeneralCaseBMPBean#getPersonalID
	 */
	public String getPersonalID();

	/**
	 * @see is.idega.idegaweb.egov.cases.data.GeneralCaseBMPBean#getEmail
	 */
	public String getEmail();

	/**
	 * @see is.idega.idegaweb.egov.cases.data.GeneralCaseBMPBean#getPhone
	 */
	public String getPhone();

	/**
	 * @see is.idega.idegaweb.egov.cases.data.GeneralCaseBMPBean#getReference
	 */
	public String getReference();

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

	/**
	 * @see is.idega.idegaweb.egov.cases.data.GeneralCaseBMPBean#setAsAnonymous
	 */
	public void setAsAnonymous(boolean isAnonymous);

	/**
	 * @see is.idega.idegaweb.egov.cases.data.GeneralCaseBMPBean#setPriority
	 */
	public void setPriority(String priority);

	/**
	 * @see is.idega.idegaweb.egov.cases.data.GeneralCaseBMPBean#setTitle
	 */
	public void setTitle(String title);

	/**
	 * @see is.idega.idegaweb.egov.cases.data.GeneralCaseBMPBean#setWantReply
	 */
	public void setWantReply(String wantReply);

	/**
	 * @see is.idega.idegaweb.egov.cases.data.GeneralCaseBMPBean#setWantReplyEmail
	 */
	public void setWantReplyEmail(String wantReplyEmail);

	/**
	 * @see is.idega.idegaweb.egov.cases.data.GeneralCaseBMPBean#setWantReplyPhone
	 */
	public void setWantReplyPhone(String wantReplyPhone);

	/**
	 * @see is.idega.idegaweb.egov.cases.data.GeneralCaseBMPBean#setName
	 */
	public void setName(String name);

	/**
	 * @see is.idega.idegaweb.egov.cases.data.GeneralCaseBMPBean#setPersonalID
	 */
	public void setPersonalID(String personalID);

	/**
	 * @see is.idega.idegaweb.egov.cases.data.GeneralCaseBMPBean#setEmail
	 */
	public void setEmail(String email);

	/**
	 * @see is.idega.idegaweb.egov.cases.data.GeneralCaseBMPBean#setPhone
	 */
	public void setPhone(String phone);

	/**
	 * @see is.idega.idegaweb.egov.cases.data.GeneralCaseBMPBean#setReference
	 */
	public void setReference(String reference);

	/**
	 * @see is.idega.idegaweb.egov.cases.data.GeneralCaseBMPBean#addSubscriber
	 */
	public void addSubscriber(User subscriber) throws IDOAddRelationshipException;

	/**
	 * @see is.idega.idegaweb.egov.cases.data.GeneralCaseBMPBean#getSubscribers
	 */
	public Collection<User> getSubscribers();

	/**
	 * @see is.idega.idegaweb.egov.cases.data.GeneralCaseBMPBean#removeSubscriber
	 */
	public void removeSubscriber(User subscriber) throws IDORemoveRelationshipException;
}