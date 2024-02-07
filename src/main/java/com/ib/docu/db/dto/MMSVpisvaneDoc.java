package com.ib.docu.db.dto;

import static javax.persistence.GenerationType.SEQUENCE;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.ib.docu.system.DocuConstants;
import com.ib.system.db.AuditExt;
import com.ib.system.db.JournalAttr;
import com.ib.system.db.TrackableEntity;
import com.ib.system.db.dto.SystemJournal;
import com.ib.system.exceptions.DbErrorException;

/**
 * Документ на вписване
 *
 * @author dessy
 */
@Entity
@Table(name = "mms_vpisvane_doc")
public class MMSVpisvaneDoc extends TrackableEntity implements AuditExt {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1195856997706862328L;

	@SequenceGenerator(name = "MMSVpisvaneDoc", sequenceName = "SEQ_MMS_VPISVANE_DOC", allocationSize = 1)
	@Id
	@GeneratedValue(strategy = SEQUENCE, generator = "MMSVpisvaneDoc")
	@Column(name = "ID", unique = true, nullable = false)
	private Integer id;
	
	@Column(name = "ID_VPISVANE", updatable = false)
	@JournalAttr(label = "idVpisvane", defaultText = "ИД на вписване")
	private Integer idVpisvane;	
	
	@Column(name = "ID_DOC")
	@JournalAttr(label = "idDoc", defaultText = "ИД на документ")
	private Integer idDoc;	
	
	@Column(name = "ID_OBJECT")
	@JournalAttr(label = "idObject", defaultText = "ИД на обекта")
	private Integer idObject;
	
	@Column(name = "TYPE_OBJECT")
	@JournalAttr(label = "typeObject", defaultText = "Код на обекта")
	private Integer typeObject;
	
	

	/** */
	public MMSVpisvaneDoc() {
		super();
	}	

	/** @return the id */
	@Override
	public Integer getId() {
		return this.id;
	}
	
	/** @param id the id to set */
	public void setId(Integer id) {
		this.id = id;
	}
	
	/**
	 * @return the idVpisvane
	 */
	public Integer getIdVpisvane() {
		return idVpisvane;
	}

	/**
	 * @param idVpisvane the idVpisvane to set
	 */
	public void setIdVpisvane(Integer idVpisvane) {
		this.idVpisvane = idVpisvane;
	}

	/**
	 * @return the idDoc
	 */
	public Integer getIdDoc() {
		return idDoc;
	}

	/**
	 * @param idDoc the idDoc to set
	 */
	public void setIdDoc(Integer idDoc) {
		this.idDoc = idDoc;
	}	

	/**
	 * @return the idObject
	 */
	public Integer getIdObject() {
		return idObject;
	}

	/**
	 * @param idObject the idObject to set
	 */
	public void setIdObject(Integer idObject) {
		this.idObject = idObject;
	}

	/**
	 * @return the typeObject
	 */
	public Integer getTypeObject() {
		return typeObject;
	}

	/**
	 * @param typeObject the typeObject to set
	 */
	public void setTypeObject(Integer typeObject) {
		this.typeObject = typeObject;
	}

	/** */
	@Override
	public Integer getCodeMainObject() {
		return DocuConstants.CODE_ZNACHENIE_JOURNAL_REGISTRATION_DOC;
	}
	
	/** */
	@Override
	public String getIdentInfo() throws DbErrorException {
		return "ИД на вписване: " + this.idVpisvane + ", ИД на док. " + this.idDoc;
	}
	
	/** */
	@Override
	public SystemJournal toSystemJournal() throws DbErrorException {
		SystemJournal journal = new SystemJournal(getCodeMainObject(), getId(), getIdentInfo());

		journal.setJoinedCodeObject1(DocuConstants.CODE_ZNACHENIE_JOURNAL_REGISTRATION);
		journal.setJoinedIdObject1(this.idVpisvane);

		return journal;
	}
}