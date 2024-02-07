package com.ib.docu.archimed;

public class DocumentExternalUri {

	private String	number;
	private String	date;

	public DocumentExternalUri() {
	}

	public DocumentExternalUri(String number, String date) {
		this.number = number;
		this.date = date;
	}

	public String getDate() {
		return this.date;
	}

	public String getNumber() {
		return this.number;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public void setNumber(String number) {
		this.number = number;
	}
}
