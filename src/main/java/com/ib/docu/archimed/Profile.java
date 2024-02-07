package com.ib.docu.archimed;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Profile {

	private String	guid;
	private String	name;
	private Boolean	isEnabled;

	public Profile() {
	}

	/** @return the guid */
	public String getGuid() {
		return this.guid;
	}

	/** @return the isEnabled */
	public Boolean getIsEnabled() {
		return this.isEnabled;
	}

	/** @return the name */
	public String getName() {
		return this.name;
	}

	/** @param guid the guid to set */
	public void setGuid(String guid) {
		this.guid = guid;
	}

	/** @param isEnabled the isEnabled to set */
	public void setIsEnabled(Boolean isEnabled) {
		this.isEnabled = isEnabled;
	}

	/** @param name the name to set */
	public void setName(String name) {
		this.name = name;
	}
}
