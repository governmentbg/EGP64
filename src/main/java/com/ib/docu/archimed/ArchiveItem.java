package com.ib.docu.archimed;

public class ArchiveItem {
	private String	id;
	private String	number;
	private String	date;

	public ArchiveItem() {
	}

	public String getDate() {
		return this.date;
	}

	public String getId() {
		return this.id;
	}

	public String getNumber() {
		return this.number;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setNumber(String number) {
		this.number = number;
	}

}
