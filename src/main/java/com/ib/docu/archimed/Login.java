package com.ib.docu.archimed;

public class Login {

	private String	profileGuid;
	private String	productName;
	private String	userName;
	private String	password;

	public Login() {
	}

	public String getPassword() {
		return this.password;
	}

	public String getProductName() {
		return this.productName;
	}

	public String getProfileGuid() {
		return this.profileGuid;
	}

	public String getUserName() {
		return this.userName;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public void setProfileGuid(String profileGuid) {
		this.profileGuid = profileGuid;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}
}
