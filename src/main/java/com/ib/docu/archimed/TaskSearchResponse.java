package com.ib.docu.archimed;

public class TaskSearchResponse {

	private String	guid;
	private Integer	count;

	public TaskSearchResponse() {
	}

	public Integer getCount() {
		return this.count;
	}

	public String getGuid() {
		return this.guid;
	}

	public void setCount(Integer count) {
		this.count = count;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}
}
