/**
 * 
 */
package is.idega.idegaweb.egov.cases.business;

public class UserDWR {

	private String userName;
	private String userPersonalID;
	private String userPhone;
	private String userEmail;

	public String getUserName() {
		return this.userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getUserPersonalID() {
		return this.userPersonalID;
	}

	public void setUserPersonalID(String userPersonalID) {
		this.userPersonalID = userPersonalID;
	}

	public String getUserPhone() {
		return this.userPhone;
	}

	public void setUserPhone(String userPhone) {
		this.userPhone = userPhone;
	}

	public String getUserEmail() {
		return this.userEmail;
	}

	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}
}