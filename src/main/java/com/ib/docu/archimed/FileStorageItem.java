package com.ib.docu.archimed;

public class FileStorageItem {

	private String	id;
	private String	parentId;
	private String	code;
	private String	name;
	private Boolean	isInactive;
	private Boolean	isGroup;

	public FileStorageItem() {
	}

	public String getCode() {
		return this.code;
	}

	public String getId() {
		return this.id;
	}

	public Boolean getIsGroup() {
		return this.isGroup;
	}

	public Boolean getIsInactive() {
		return this.isInactive;
	}

	public String getName() {
		return this.name;
	}

	public String getParentId() {
		return this.parentId;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setIsGroup(Boolean isGroup) {
		this.isGroup = isGroup;
	}

	public void setIsInactive(Boolean isInactive) {
		this.isInactive = isInactive;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

}
