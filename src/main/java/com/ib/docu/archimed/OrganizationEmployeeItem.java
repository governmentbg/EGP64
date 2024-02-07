package com.ib.docu.archimed;

public class OrganizationEmployeeItem {

	private String	id;
	private String	code;
	private String	name;
	private String	parentUnitId;
	private String	primaryUnitId;
	private Boolean	isInactive;
	private Boolean	canRegister;

	public OrganizationEmployeeItem() {
	}

	public Boolean getCanRegister() {
		return this.canRegister;
	}

	public String getCode() {
		return this.code;
	}

	public String getId() {
		return this.id;
	}

	public Boolean getIsInactive() {
		return this.isInactive;
	}

	public String getName() {
		return this.name;
	}

	public String getParentUnitId() {
		return this.parentUnitId;
	}

	public String getPrimaryUnitId() {
		return this.primaryUnitId;
	}

	public void setCanRegister(Boolean canRegister) {
		this.canRegister = canRegister;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setIsInactive(Boolean isInactive) {
		this.isInactive = isInactive;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setParentUnitId(String parentUnitId) {
		this.parentUnitId = parentUnitId;
	}

	public void setPrimaryUnitId(String primaryUnitId) {
		this.primaryUnitId = primaryUnitId;
	}
}
