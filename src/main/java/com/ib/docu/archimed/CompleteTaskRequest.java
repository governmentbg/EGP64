package com.ib.docu.archimed;

public class CompleteTaskRequest {

	private CompleteTaskPayload actionPayload;

	public CompleteTaskRequest() {
	}

	public CompleteTaskPayload getActionPayload() {
		return this.actionPayload;
	}

	public void setActionPayload(CompleteTaskPayload actionPayload) {
		this.actionPayload = actionPayload;
	}
}
