package com.ib.docu.archimed;

public class DocumentFile {

	private String	id;
	private String	fileName;
	private String	description;
	private String	contentType;
	private String	dateCreated;
	private String	dateModified;

	private byte[] fileContent;

	public DocumentFile() {
	}

	public String getContentType() {
		return this.contentType;
	}

	public String getDateCreated() {
		return this.dateCreated;
	}

	public String getDateModified() {
		return this.dateModified;
	}

	public String getDescription() {
		return this.description;
	}

	public byte[] getFileContent() {
		return this.fileContent;
	}

	public String getFileName() {
		return this.fileName;
	}

	public String getId() {
		return this.id;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public void setDateCreated(String dateCreated) {
		this.dateCreated = dateCreated;
	}

	public void setDateModified(String dateModified) {
		this.dateModified = dateModified;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setFileContent(byte[] fileContent) {
		this.fileContent = fileContent;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public void setId(String id) {
		this.id = id;
	}
}
