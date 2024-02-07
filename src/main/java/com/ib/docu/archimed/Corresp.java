package com.ib.docu.archimed;

public class Corresp {

	public enum CorrespTypes {
		Firm, Correspondent, CorrespondentGroup, Department, Employee, Role, Pool, Bank
	}

	public enum DataProtectionModes {
		None, Restriction, Obliteration
	}

	private String	id;
	private String	code;
	private String	name;
	private String	groupId;
	private String	description;
	private String	responsiblePerson;
	private String	customerNumber;
	private String	vatNumber;
	private String	position;
	private String	email;
	private String	postalCode;
	private String	locality;
	private String	phoneNumber;
	private String	faxNumber;

	private CorrespIdentifier	identifier;
	private CorrespTypes		type;
	private DataProtectionModes	dataProtectionMode;

	private Boolean	isInactive;
	private Boolean	canUpdate;
	private Boolean	canAddMembers;
	private Boolean	isJuridicalPerson;

	public Corresp() {
	}

	public Boolean getCanAddMembers() {
		return this.canAddMembers;
	}

	public Boolean getCanUpdate() {
		return this.canUpdate;
	}

	public String getCode() {
		return this.code;
	}

	public String getCustomerNumber() {
		return this.customerNumber;
	}

	public DataProtectionModes getDataProtectionMode() {
		return this.dataProtectionMode;
	}

	public String getDescription() {
		return this.description;
	}

	public String getEmail() {
		return this.email;
	}

	public String getFaxNumber() {
		return this.faxNumber;
	}

	public String getGroupId() {
		return this.groupId;
	}

	public String getId() {
		return this.id;
	}

	public CorrespIdentifier getIdentifier() {
		return this.identifier;
	}

	public Boolean getIsInactive() {
		return this.isInactive;
	}

	public Boolean getIsJuridicalPerson() {
		return this.isJuridicalPerson;
	}

	public String getLocality() {
		return this.locality;
	}

	public String getName() {
		return this.name;
	}

	public String getPhoneNumber() {
		return this.phoneNumber;
	}

	public String getPosition() {
		return this.position;
	}

	public String getPostalCode() {
		return this.postalCode;
	}

	public String getResponsiblePerson() {
		return this.responsiblePerson;
	}

	public CorrespTypes getType() {
		return this.type;
	}

	public String getVatNumber() {
		return this.vatNumber;
	}

	public void setCanAddMembers(Boolean canAddMembers) {
		this.canAddMembers = canAddMembers;
	}

	public void setCanUpdate(Boolean canUpdate) {
		this.canUpdate = canUpdate;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public void setCustomerNumber(String customerNumber) {
		this.customerNumber = customerNumber;
	}

	public void setDataProtectionMode(DataProtectionModes dataProtectionMode) {
		this.dataProtectionMode = dataProtectionMode;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setFaxNumber(String faxNumber) {
		this.faxNumber = faxNumber;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setIdentifier(CorrespIdentifier identifier) {
		this.identifier = identifier;
	}

	public void setIsInactive(Boolean isInactive) {
		this.isInactive = isInactive;
	}

	public void setIsJuridicalPerson(Boolean isJuridicalPerson) {
		this.isJuridicalPerson = isJuridicalPerson;
	}

	public void setLocality(String locality) {
		this.locality = locality;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	public void setResponsiblePerson(String responsiblePerson) {
		this.responsiblePerson = responsiblePerson;
	}

	public void setType(CorrespTypes type) {
		this.type = type;
	}

	public void setVatNumber(String vatNumber) {
		this.vatNumber = vatNumber;
	}
}
