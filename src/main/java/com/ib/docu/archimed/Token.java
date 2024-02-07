package com.ib.docu.archimed;

public class Token {

	private String	accessToken;
	private Long	expiresIn;
	private String	tokenType;

	public Token() {
	}

	public String getAccessToken() {
		return this.accessToken;
	}

	public Long getExpiresIn() {
		return this.expiresIn;
	}

	public String getTokenType() {
		return this.tokenType;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public void setExpiresIn(Long expiresIn) {
		this.expiresIn = expiresIn;
	}

	public void setTokenType(String tokenType) {
		this.tokenType = tokenType;
	}
}
