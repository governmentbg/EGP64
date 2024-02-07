package com.ib.docu.archimed;

import com.ib.docu.archimed.Document.CorrespondenceTypes;
import com.ib.docu.archimed.Document.DocumentStates;

public class CompleteTaskPayload {

	private ActionType type;

	private CorrespondenceTypes	correspondenceType;
	private String				documentTypeId;
	private String				activityId;
	private String				correspondentId;
	private DocumentStates		state;

//	private String		taskId;
//	private String		answeredById;
//	private String		text;

	public CompleteTaskPayload() {
	}

	public String getActivityId() {
		return this.activityId;
	}

	public CorrespondenceTypes getCorrespondenceType() {
		return this.correspondenceType;
	}

	public String getCorrespondentId() {
		return this.correspondentId;
	}

	public String getDocumentTypeId() {
		return this.documentTypeId;
	}

	public DocumentStates getState() {
		return this.state;
	}

	public ActionType getType() {
		return this.type;
	}

	public void setActivityId(String activityId) {
		this.activityId = activityId;
	}

	public void setCorrespondenceType(CorrespondenceTypes correspondenceType) {
		this.correspondenceType = correspondenceType;
	}

	public void setCorrespondentId(String correspondentId) {
		this.correspondentId = correspondentId;
	}

	public void setDocumentTypeId(String documentTypeId) {
		this.documentTypeId = documentTypeId;
	}

	public void setState(DocumentStates state) {
		this.state = state;
	}

	public void setType(ActionType type) {
		this.type = type;
	}

}
