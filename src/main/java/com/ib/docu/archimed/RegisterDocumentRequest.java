package com.ib.docu.archimed;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.ib.docu.archimed.Document.CorrespondenceTypes;
import com.ib.docu.archimed.Document.DocumentStates;

@JsonInclude(value = Include.NON_NULL)
public class RegisterDocumentRequest {

	private String	registratorId;
	private String	registrationDate;
	private String	parentId;
	private String	documentTypeId;
	private String	activityId;
	private String	correspondentId;
	private String	correspondentDescription;
	private String	description;
	private String	deadline;
	private String	additionalText;
	private String	secondaryCorrespondentId;
	private String	processDefinitionRevisionID;

	private CorrespondenceTypes	correspondenceType;
	private DocumentStates		state;
	private DocumentExternalUri	externalUri;

	private Boolean	expectingResponse;
	private Boolean	createArchive;

	private String taskId;

	public RegisterDocumentRequest() {
	}

	public String getActivityId() {
		return this.activityId;
	}

	public String getAdditionalText() {
		return this.additionalText;
	}

	public CorrespondenceTypes getCorrespondenceType() {
		return this.correspondenceType;
	}

	public String getCorrespondentDescription() {
		return this.correspondentDescription;
	}

	public String getCorrespondentId() {
		return this.correspondentId;
	}

	public Boolean getCreateArchive() {
		return this.createArchive;
	}

	public String getDeadline() {
		return this.deadline;
	}

	public String getDescription() {
		return this.description;
	}

	public String getDocumentTypeId() {
		return this.documentTypeId;
	}

	public Boolean getExpectingResponse() {
		return this.expectingResponse;
	}

	public DocumentExternalUri getExternalUri() {
		return this.externalUri;
	}

	public String getParentId() {
		return this.parentId;
	}

	public String getProcessDefinitionRevisionID() {
		return this.processDefinitionRevisionID;
	}

	public String getRegistrationDate() {
		return this.registrationDate;
	}

	public String getRegistratorId() {
		return this.registratorId;
	}

	public String getSecondaryCorrespondentId() {
		return this.secondaryCorrespondentId;
	}

	public DocumentStates getState() {
		return this.state;
	}

	public String getTaskId() {
		return this.taskId;
	}

	public void setActivityId(String activityId) {
		this.activityId = activityId;
	}

	public void setAdditionalText(String additionalText) {
		this.additionalText = additionalText;
	}

	public void setCorrespondenceType(CorrespondenceTypes correspondenceType) {
		this.correspondenceType = correspondenceType;
	}

	public void setCorrespondentDescription(String correspondentDescription) {
		this.correspondentDescription = correspondentDescription;
	}

	public void setCorrespondentId(String correspondentId) {
		this.correspondentId = correspondentId;
	}

	public void setCreateArchive(Boolean createArchive) {
		this.createArchive = createArchive;
	}

	public void setDeadline(String deadline) {
		this.deadline = deadline;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setDocumentTypeId(String documentTypeId) {
		this.documentTypeId = documentTypeId;
	}

	public void setExpectingResponse(Boolean expectingResponse) {
		this.expectingResponse = expectingResponse;
	}

	public void setExternalUri(DocumentExternalUri externalUri) {
		this.externalUri = externalUri;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public void setProcessDefinitionRevisionID(String processDefinitionRevisionID) {
		this.processDefinitionRevisionID = processDefinitionRevisionID;
	}

	public void setRegistrationDate(String registrationDate) {
		this.registrationDate = registrationDate;
	}

	public void setRegistratorId(String registratorId) {
		this.registratorId = registratorId;
	}

	public void setSecondaryCorrespondentId(String secondaryCorrespondentId) {
		this.secondaryCorrespondentId = secondaryCorrespondentId;
	}

	public void setState(DocumentStates state) {
		this.state = state;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

}
