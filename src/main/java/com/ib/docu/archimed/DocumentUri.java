package com.ib.docu.archimed;

public class DocumentUri {

	private String	date;
	private String	number;
	private Integer	serialNo;

	public DocumentUri() {
	}

	public String getDate() {
		return this.date;
	}

	public String getNumber() {
		return this.number;
	}

	public Integer getSerialNo() {
		return this.serialNo;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public void setSerialNo(Integer serialNo) {
		this.serialNo = serialNo;
	}
}
