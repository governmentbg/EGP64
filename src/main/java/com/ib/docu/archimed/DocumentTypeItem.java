package com.ib.docu.archimed;

public class DocumentTypeItem {

	private String	id;
	private String	code;
	private String	name;
	private Boolean	isInactive;

	public DocumentTypeItem() {
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

}
