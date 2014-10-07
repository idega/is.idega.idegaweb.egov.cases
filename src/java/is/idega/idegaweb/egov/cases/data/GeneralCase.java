package is.idega.idegaweb.egov.cases.data;


import java.util.Collection;

import com.idega.block.process.data.Case;
import com.idega.core.file.data.ICFile;
import com.idega.data.IDOEntity;
import com.idega.user.data.User;

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

	public void addAttachment(ICFile file);
	
	public Collection<ICFile> getAttachments();
	public void removeAllAttachments();
	public void removeAttachment(ICFile file);

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
	 * @see is.idega.idegaweb.egov.cases.data.GeneralCaseBMPBean#setReference
	 */
	public void setReference(String reference);
	
	/**
	 * 
	 * <p>Checks if case is subscribed by user.</p>
	 * @param user to check, not <code>null</code>;
	 * @return <code>true</code> if subscribed, <code>false</code> otherwise;
	 * @author <a href="mailto:martynas@idega.is">Martynas Stakė</a>
	 */
	public boolean isSubscribed(User user);

}