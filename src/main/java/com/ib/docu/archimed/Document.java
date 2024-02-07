package com.ib.docu.archimed;

import java.util.List;

public class Document {

	public enum ArchiveElevationMode {
		NoAccess, ReadOnly, ReadWrite
	}

	public enum CorrespondenceTypes {
		Internal, Incoming, Outgoing
	}

	public enum DocumentStates {
		Draft, Working, Registered
	}

	private String	id;
	private String	guid;
	private String	parentId;
	private String	description;
	private String	dateCreated;
	private String	dateModified;
	private String	correspondentDescription;
	private String	secondaryCorrespondentID;
	private String	deadline;
	private String	warningDate;
	private String	expiryDate;
	private String	responseDate;
	private String	displayText;
	private String	resolution;
	private String	sendToEmailFromAddress;
	private String	sendToEmailFromName;
	private String	sendToEmailSubjectSuggestion;
	private String	sendToEmailTextSuggestion;
	private String	correspondentUri;

	private DocumentUri						uri;
	private DocumentStates					state;
	private CorrespondenceTypes				correspondenceType;
	private DocumentTypeInfo				type;
	private ActivityInfo					activity;
	private CorrespondentInfo				correspondent;
	private DocumentExternalUri				externalUri;
	private DocumentSupportedActions		supportedActions;
	private CorrespondenceTypes				answerDefaultCorrespondenceType;
	private DocumentTypeItem				answerDefaultDocumentType;
	private ArchiveElevationMode			archiveElevationMode;
	private CorrespondentInfo				secondCorrespondent;
	private ArchiveItem						archive;
	private OrganizationEmployeeItem		employee;
	private List<OrganizationEmployeeItem>	additionalEmployees;
	private FileStorageItem					fileStorage;
	private ProcessItem						process;
	private OrganizationEmployeeItem		registrator;
	private OrganizationEmployeeItem		resolutionPerfomer;
	private OrganizationEmployeeItem		secondEmployee;

	private Boolean	isActive;
	private Boolean	isInactive;
	private Boolean	isAnswered;
	private Boolean	isFinished;
	private Boolean	isExpired;
	private Boolean	isDangerous;
	private Boolean	isFileCase;
	private Boolean	isResoluted;
	private Boolean	isExpectResponse;
	private Boolean	hasSubDocuments;
	private Boolean	isExpectingResponse;
	private Boolean	hasPendingTasks;
	private Boolean	sendToEmailEnabled;
	private Boolean	sendToEmailArchive;
	private Boolean	isWaiting;

	private List<CorrespondentInfo> additionalCorrespondents;

	public Document() {
	}

	public ActivityInfo getActivity() {
		return this.activity;
	}

	public List<CorrespondentInfo> getAdditionalCorrespondents() {
		return this.additionalCorrespondents;
	}

	public List<OrganizationEmployeeItem> getAdditionalEmployees() {
		return this.additionalEmployees;
	}

	public CorrespondenceTypes getAnswerDefaultCorrespondenceType() {
		return this.answerDefaultCorrespondenceType;
	}

	public DocumentTypeItem getAnswerDefaultDocumentType() {
		return this.answerDefaultDocumentType;
	}

	public ArchiveItem getArchive() {
		return this.archive;
	}

	public ArchiveElevationMode getArchiveElevationMode() {
		return this.archiveElevationMode;
	}

	public CorrespondenceTypes getCorrespondenceType() {
		return this.correspondenceType;
	}

	public CorrespondentInfo getCorrespondent() {
		return this.correspondent;
	}

	public String getCorrespondentDescription() {
		return this.correspondentDescription;
	}

	public String getCorrespondentUri() {
		return this.correspondentUri;
	}

	public String getDateCreated() {
		return this.dateCreated;
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

	public String getDisplayText() {
		return this.displayText;
	}

	public OrganizationEmployeeItem getEmployee() {
		return this.employee;
	}

	public String getExpiryDate() {
		return this.expiryDate;
	}

	public DocumentExternalUri getExternalUri() {
		return this.externalUri;
	}

	public FileStorageItem getFileStorage() {
		return this.fileStorage;
	}

	public String getGuid() {
		return this.guid;
	}

	public Boolean getHasPendingTasks() {
		return this.hasPendingTasks;
	}

	public Boolean getHasSubDocuments() {
		return this.hasSubDocuments;
	}

	public String getId() {
		return this.id;
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

	public Boolean getIsExpectingResponse() {
		return this.isExpectingResponse;
	}

	public Boolean getIsExpectResponse() {
		return this.isExpectResponse;
	}

	public Boolean getIsExpired() {
		return this.isExpired;
	}

	public Boolean getIsFileCase() {
		return this.isFileCase;
	}

	public Boolean getIsFinished() {
		return this.isFinished;
	}

	public Boolean getIsInactive() {
		return this.isInactive;
	}

	public Boolean getIsResoluted() {
		return this.isResoluted;
	}

	public Boolean getIsWaiting() {
		return this.isWaiting;
	}

	public String getParentId() {
		return this.parentId;
	}

	public ProcessItem getProcess() {
		return this.process;
	}

	public OrganizationEmployeeItem getRegistrator() {
		return this.registrator;
	}

	public String getResolution() {
		return this.resolution;
	}

	public OrganizationEmployeeItem getResolutionPerfomer() {
		return this.resolutionPerfomer;
	}

	public String getResponseDate() {
		return this.responseDate;
	}

	public String getSecondaryCorrespondentID() {
		return this.secondaryCorrespondentID;
	}

	public CorrespondentInfo getSecondCorrespondent() {
		return this.secondCorrespondent;
	}

	public OrganizationEmployeeItem getSecondEmployee() {
		return this.secondEmployee;
	}

	public Boolean getSendToEmailArchive() {
		return this.sendToEmailArchive;
	}

	public Boolean getSendToEmailEnabled() {
		return this.sendToEmailEnabled;
	}

	public String getSendToEmailFromAddress() {
		return this.sendToEmailFromAddress;
	}

	public String getSendToEmailFromName() {
		return this.sendToEmailFromName;
	}

	public String getSendToEmailSubjectSuggestion() {
		return this.sendToEmailSubjectSuggestion;
	}

	public String getSendToEmailTextSuggestion() {
		return this.sendToEmailTextSuggestion;
	}

	public DocumentStates getState() {
		return this.state;
	}

	public DocumentSupportedActions getSupportedActions() {
		return this.supportedActions;
	}

	public DocumentTypeInfo getType() {
		return this.type;
	}

	public DocumentUri getUri() {
		return this.uri;
	}

	public String getWarningDate() {
		return this.warningDate;
	}

	public boolean hasSubDocuments() {
		return this.hasSubDocuments;
	}

	public void setActivity(ActivityInfo activity) {
		this.activity = activity;
	}

	public void setAdditionalCorrespondents(List<CorrespondentInfo> additionalCorrespondents) {
		this.additionalCorrespondents = additionalCorrespondents;
	}

	public void setAdditionalEmployees(List<OrganizationEmployeeItem> additionalEmployees) {
		this.additionalEmployees = additionalEmployees;
	}

	public void setAnswerDefaultCorrespondenceType(CorrespondenceTypes answerDefaultCorrespondenceType) {
		this.answerDefaultCorrespondenceType = answerDefaultCorrespondenceType;
	}

	public void setAnswerDefaultDocumentType(DocumentTypeItem answerDefaultDocumentType) {
		this.answerDefaultDocumentType = answerDefaultDocumentType;
	}

	public void setArchive(ArchiveItem archive) {
		this.archive = archive;
	}

	public void setArchiveElevationMode(ArchiveElevationMode archiveElevationMode) {
		this.archiveElevationMode = archiveElevationMode;
	}

	public void setCorrespondenceType(CorrespondenceTypes correspondenceType) {
		this.correspondenceType = correspondenceType;
	}

	public void setCorrespondent(CorrespondentInfo correspondent) {
		this.correspondent = correspondent;
	}

	public void setCorrespondentDescription(String correspondentDescription) {
		this.correspondentDescription = correspondentDescription;
	}

	public void setCorrespondentUri(String correspondentUri) {
		this.correspondentUri = correspondentUri;
	}

	public void setDateCreated(String dateCreated) {
		this.dateCreated = dateCreated;
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

	public void setDisplayText(String displayText) {
		this.displayText = displayText;
	}

	public void setEmployee(OrganizationEmployeeItem employee) {
		this.employee = employee;
	}

	public void setExpiryDate(String expiryDate) {
		this.expiryDate = expiryDate;
	}

	public void setExternalUri(DocumentExternalUri externalUri) {
		this.externalUri = externalUri;
	}

	public void setFileStorage(FileStorageItem fileStorage) {
		this.fileStorage = fileStorage;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public void setHasPendingTasks(Boolean hasPendingTasks) {
		this.hasPendingTasks = hasPendingTasks;
	}

	public void setHasSubDocuments(Boolean hasSubDocuments) {
		this.hasSubDocuments = hasSubDocuments;
	}

	public void setId(String id) {
		this.id = id;
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

	public void setIsExpectingResponse(Boolean isExpectingResponse) {
		this.isExpectingResponse = isExpectingResponse;
	}

	public void setIsExpectResponse(Boolean isExpectResponse) {
		this.isExpectResponse = isExpectResponse;
	}

	public void setIsExpired(Boolean isExpired) {
		this.isExpired = isExpired;
	}

	public void setIsFileCase(Boolean isFileCase) {
		this.isFileCase = isFileCase;
	}

	public void setIsFinished(Boolean isFinished) {
		this.isFinished = isFinished;
	}

	public void setIsInactive(Boolean isInactive) {
		this.isInactive = isInactive;
	}

	public void setIsResoluted(Boolean isResoluted) {
		this.isResoluted = isResoluted;
	}

	public void setIsWaiting(Boolean isWaiting) {
		this.isWaiting = isWaiting;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public void setProcess(ProcessItem process) {
		this.process = process;
	}

	public void setRegistrator(OrganizationEmployeeItem registrator) {
		this.registrator = registrator;
	}

	public void setResolution(String resolution) {
		this.resolution = resolution;
	}

	public void setResolutionPerfomer(OrganizationEmployeeItem resolutionPerfomer) {
		this.resolutionPerfomer = resolutionPerfomer;
	}

	public void setResponseDate(String responseDate) {
		this.responseDate = responseDate;
	}

	public void setSecondaryCorrespondentID(String secondaryCorrespondentID) {
		this.secondaryCorrespondentID = secondaryCorrespondentID;
	}

	public void setSecondCorrespondent(CorrespondentInfo secondCorrespondent) {
		this.secondCorrespondent = secondCorrespondent;
	}

	public void setSecondEmployee(OrganizationEmployeeItem secondEmployee) {
		this.secondEmployee = secondEmployee;
	}

	public void setSendToEmailArchive(Boolean sendToEmailArchive) {
		this.sendToEmailArchive = sendToEmailArchive;
	}

	public void setSendToEmailEnabled(Boolean sendToEmailEnabled) {
		this.sendToEmailEnabled = sendToEmailEnabled;
	}

	public void setSendToEmailFromAddress(String sendToEmailFromAddress) {
		this.sendToEmailFromAddress = sendToEmailFromAddress;
	}

	public void setSendToEmailFromName(String sendToEmailFromName) {
		this.sendToEmailFromName = sendToEmailFromName;
	}

	public void setSendToEmailSubjectSuggestion(String sendToEmailSubjectSuggestion) {
		this.sendToEmailSubjectSuggestion = sendToEmailSubjectSuggestion;
	}

	public void setSendToEmailTextSuggestion(String sendToEmailTextSuggestion) {
		this.sendToEmailTextSuggestion = sendToEmailTextSuggestion;
	}

	public void setState(DocumentStates state) {
		this.state = state;
	}

	public void setSupportedActions(DocumentSupportedActions supportedActions) {
		this.supportedActions = supportedActions;
	}

	public void setType(DocumentTypeInfo type) {
		this.type = type;
	}

	public void setUri(DocumentUri uri) {
		this.uri = uri;
	}

	public void setWarningDate(String warningDate) {
		this.warningDate = warningDate;
	}
}
