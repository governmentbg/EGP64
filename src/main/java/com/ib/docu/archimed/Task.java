package com.ib.docu.archimed;

public class Task {

	public enum TaskResponseType {
		Text, DocumentRegistration, Resolution, Signing, Coordination, Approve, Attitude, Classification, Completion, ArchiveImport
	}

	public enum TaskState {
		Initiated, Accepted, Closed
	}

	private String	id;
	private String	documentId;
	private String	registrationDate;
	private String	dateModified;
	private String	date;
	private String	startDate;
	private String	answerDate;
	private String	deadline;
	private String	description;
	private String	initiatorId;
	private String	assignedEmployeeId;
	private String	performerId;
	private String	registratorId;

	private TaskResponseType	responseType;
	private TaskState			state;

	private Boolean	canPerformerDoAction;
	private Boolean	canAnswer;
	private Boolean	isActive;
	private Boolean	isAnswered;
	private Boolean	isExpired;
	private Boolean	isDangerous;
	private Boolean	isWaiting;
	private Boolean	isDeleted;

	public Task() {
	}

	public String getAnswerDate() {
		return this.answerDate;
	}

	public String getAssignedEmployeeId() {
		return this.assignedEmployeeId;
	}

	public Boolean getCanAnswer() {
		return this.canAnswer;
	}

	public Boolean getCanPerformerDoAction() {
		return this.canPerformerDoAction;
	}

	public String getDate() {
		return this.date;
	}

	public String getDateModified() {
		return this.dateModified;
	}

	public String getDeadline() {
		return this.deadline;
	}

	public String getDescription() {
		return this.description;
	}

	public String getDocumentId() {
		return this.documentId;
	}

	public String getId() {
		return this.id;
	}

	public String getInitiatorId() {
		return this.initiatorId;
	}

	public Boolean getIsActive() {
		return this.isActive;
	}

	public Boolean getIsAnswered() {
		return this.isAnswered;
	}

	public Boolean getIsDangerous() {
		return this.isDangerous;
	}

	public Boolean getIsDeleted() {
		return this.isDeleted;
	}

	public Boolean getIsExpired() {
		return this.isExpired;
	}

	public Boolean getIsWaiting() {
		return this.isWaiting;
	}

	public String getPerformerId() {
		return this.performerId;
	}

	public String getRegistrationDate() {
		return this.registrationDate;
	}

	public String getRegistratorId() {
		return this.registratorId;
	}

	public TaskResponseType getResponseType() {
		return this.responseType;
	}

	public String getStartDate() {
		return this.startDate;
	}

	public TaskState getState() {
		return this.state;
	}

	public void setAnswerDate(String answerDate) {
		this.answerDate = answerDate;
	}

	public void setAssignedEmployeeId(String assignedEmployeeId) {
		this.assignedEmployeeId = assignedEmployeeId;
	}

	public void setCanAnswer(Boolean canAnswer) {
		this.canAnswer = canAnswer;
	}

	public void setCanPerformerDoAction(Boolean canPerformerDoAction) {
		this.canPerformerDoAction = canPerformerDoAction;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public void setDateModified(String dateModified) {
		this.dateModified = dateModified;
	}

	public void setDeadline(String deadline) {
		this.deadline = deadline;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setDocumentId(String documentId) {
		this.documentId = documentId;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setInitiatorId(String initiatorId) {
		this.initiatorId = initiatorId;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

	public void setIsAnswered(Boolean isAnswered) {
		this.isAnswered = isAnswered;
	}

	public void setIsDangerous(Boolean isDangerous) {
		this.isDangerous = isDangerous;
	}

	public void setIsDeleted(Boolean isDeleted) {
		this.isDeleted = isDeleted;
	}

	public void setIsExpired(Boolean isExpired) {
		this.isExpired = isExpired;
	}

	public void setIsWaiting(Boolean isWaiting) {
		this.isWaiting = isWaiting;
	}

	public void setPerformerId(String performerId) {
		this.performerId = performerId;
	}

	public void setRegistrationDate(String registrationDate) {
		this.registrationDate = registrationDate;
	}

	public void setRegistratorId(String registratorId) {
		this.registratorId = registratorId;
	}

	public void setResponseType(TaskResponseType responseType) {
		this.responseType = responseType;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public void setState(TaskState state) {
		this.state = state;
	}
}
