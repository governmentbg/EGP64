package com.ib.docu.db.dto;

import static javax.persistence.GenerationType.SEQUENCE;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.ib.docu.system.DocuConstants;
import com.ib.system.db.AuditExt;
import com.ib.system.db.JournalAttr;
import com.ib.system.db.TrackableEntity;
import com.ib.system.db.dto.SystemJournal;
import com.ib.system.exceptions.DbErrorException;

	/**
	 * MMSChlenstvo
	 *
	 * @author kosev
	 */
	@Entity
	@Table(name = "mms_chlenstvo")
	public class MMSChlenstvo extends TrackableEntity implements Serializable, AuditExt {
		/**
		 * 
		 */
		private static final long serialVersionUID = -5497521613451988642L;

		@SequenceGenerator(name = "MMSchlenstvo", sequenceName = "seq_mms_chlenstvo", allocationSize = 1)
		@Id
		@GeneratedValue(strategy = SEQUENCE, generator = "MMSchlenstvo")
		@Column(name = "ID", unique = true, nullable = false)
		private Integer id;

		// Системна класификация 2 - Информационни обекти (за журналиране)
		@JournalAttr(label = "typeObject", defaultText = "Тип на обекта", classifID = "" + DocuConstants.CODE_NOTIF_STATUS_NEPROCHETENA)
		@Column(name = "type_object")
		private Integer typeObject;

		@JournalAttr(label = "idObject", defaultText = "Ид на обекта")
		@Column(name = "id_object")
		private Integer idObject;
		
		// Системна класификация 2 - Информационни обекти (за журналиране)
		@JournalAttr(label = "typeVishObject", defaultText = "Тип на висшестоящ обект", classifID = ""+DocuConstants.CODE_NOTIF_STATUS_NEPROCHETENA)
		@Column(name = "type_vish_object")
		private Integer typeVishObject;
		
		@JournalAttr(label = "idVishObject", defaultText = "Ид на висшестоящ обект")
		@Column(name = "id_vish_object")
		private Integer idVishObject;

		
		@Column(name = "date_acceptance")
		@JournalAttr(label = "dateAcceptance", defaultText = "Дата на приемане")
		private Date dateAcceptance;
		
		@Column(name = "date_termination")
		@JournalAttr(label = "dateTermination", defaultText = "Дата на прекратяване на членството")
		private Date dateTermination;
		
		@Transient
		private Integer vid;
		@Transient
		private String nameRef;
		@Transient
		private String eik;
		@Transient
		private String regNom;
		@Transient
		private Date dateRegNom;
		

		/** */
		public MMSChlenstvo() {
			super();
		}

		/**
		 * @param id
		 * @param idObject
		 */
		public MMSChlenstvo(Integer id, Integer idObject) {
			this.id = id;
			this.setIdObject(idObject);
		}

		/** */
		@Override
		public Integer getId() {
			return this.id;
		}

		@Override
		public Integer getCodeMainObject() {
			return DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_FORMATIONS;
		}
		@Override
		public SystemJournal toSystemJournal() throws DbErrorException {
			SystemJournal dj = new SystemJournal();
			dj.setCodeObject(getCodeMainObject());
			dj.setIdObject(getId());
			dj.setIdentObject(getIdentInfo());
			return dj;
		}

		public Integer getIdObject() {
			return idObject;
		}

		public void setIdObject(Integer idObject) {
			this.idObject = idObject;
		}

		public Integer getTypeObject() {
			return typeObject;
		}

		public void setTypeObject(Integer typeObject) {
			this.typeObject = typeObject;
		}

		public Date getDateAcceptance() {
			return dateAcceptance;
		}

		public void setDateAcceptance(Date dateAcceptance) {
			this.dateAcceptance = dateAcceptance;
		}

		public Date getDateTermination() {
			return dateTermination;
		}

		public void setDateTermination(Date dateTermination) {
			this.dateTermination = dateTermination;
		}

		public Integer getTypeVishObject() {
			return typeVishObject;
		}

		public void setTypeVishObject(Integer typeVishObject) {
			this.typeVishObject = typeVishObject;
		}

		public Integer getIdVishObject() {
			return idVishObject;
		}

		public void setIdVishObject(Integer idVishObject) {
			this.idVishObject = idVishObject;
		}

		public Integer getVid() {
			return vid;
		}

		public void setVid(Integer vid) {
			this.vid = vid;
		}

		public String getNameRef() {
			return nameRef;
		}

		public void setNameRef(String nameRef) {
			this.nameRef = nameRef;
		}

		public String getEik() {
			return eik;
		}

		public void setEik(String eik) {
			this.eik = eik;
		}

		public String getRegNom() {
			return regNom;
		}

		public void setRegNom(String regNom) {
			this.regNom = regNom;
		}

		public Date getDateRegNom() {
			return dateRegNom;
		}

		public void setDateRegNom(Date dateRegNom) {
			this.dateRegNom = dateRegNom;
		}

	}
