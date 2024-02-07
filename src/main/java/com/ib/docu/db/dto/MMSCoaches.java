package com.ib.docu.db.dto;

import static javax.persistence.GenerationType.SEQUENCE;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Where;

import com.ib.docu.system.DocuConstants;
import com.ib.system.db.AuditExt;
import com.ib.system.db.JournalAttr;
import com.ib.system.db.TrackableEntity;
import com.ib.system.db.dto.SystemJournal;
import com.ib.system.exceptions.DbErrorException;

	/**
	 * Coaches
	 *
	 * @author IvanT
	 */
	@Entity
	@Table(name = "mms_coaches")
	public class MMSCoaches extends TrackableEntity implements Serializable, AuditExt {
		/**
		 * 
		 */
		private static final long serialVersionUID = -5497521613451988642L;

		@SequenceGenerator(name = "MMSCoaches", sequenceName = "seq_mms_coaches", allocationSize = 1)
		@Id
		@GeneratedValue(strategy = SEQUENCE, generator = "MMSCoaches")
		@Column(name = "id", unique = true, nullable = false)
		private Integer id;

		@Column(name = "reg_nomer")
		private String regNomer;

		@JournalAttr(label = "idObject", defaultText = "Ид на лицето", classifID = ""+DocuConstants.CODE_CLASSIF_REFERENTS)
		@Column(name = "id_object")
		private Integer idObject;
		
		@JournalAttr(label = "status", defaultText = "Статус на лицето", classifID = ""+DocuConstants.CODE_CLASSIF_STATUS_REGISTRATION)
		@Column(name = "status")
		private Integer status;
		
		@JournalAttr(label = "dateStatus", defaultText = "Дата на статус")
		@Column(name = "date_status")
		private Date dateStatus;

		@Column(name = "dop_info")
		@JournalAttr(label = "dopInfo", defaultText = "Допълнителна информация")
		private String dopInfo;
		
		@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
		@JoinColumn(name = "id_coaches", referencedColumnName = "id" , nullable = false)
		/**
		@JournalAttr(label = "coachesDiploms", defaultText = "Диплома на треньор")
		*/
		private List<MMSCoachesDiploms> coachesDiploms = new ArrayList<>();	// Дипломи на треньор
		
		/**
		@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
		@JoinColumn(name = "id_object", referencedColumnName = "id" , nullable = false)
		@Where(clause = " tip_object =" + DocuConstants.CODE_ZNACHENIE_JOURNAL_COACHES)
		private List<MMSVidSportSC> vidSportList = new ArrayList<>();
		*/
		
		//при парсване на пдф тук се подават съобщения за грешки и проблеми
		@Transient
		private List<String> parseMessages=new ArrayList<String>();
		
		//при парсване на пдф тук се подава вида на спорт който трябва да влезе във Вписването 
		@Transient
		private Integer vidSport=null;
		@Transient
		private Integer dlajnost=null;
		@Transient
		private String mailLice=null;
		@Transient
		private Integer nachinPoluch=null;
		@Transient
		private String nachinPoluchText=null;
		
		
		public List<MMSCoachesDiploms> getCoachesDiploms() {
			return coachesDiploms;
		}

		public void setCoachesDiploms(List<MMSCoachesDiploms> coachesDiploms) {
			this.coachesDiploms = coachesDiploms;
		}


		/** */
		public MMSCoaches() {
			super();
		}

		/**
		 * @param id
		 * @param regNomer
		 * @param idObject
		 */
		public MMSCoaches(Integer id, String regNomer, Integer idObject) {
			this.setId(id);
			this.setRegNomer(regNomer);
			this.setIdObject(idObject);
		}

		/** */
		@Override
		public Integer getCodeMainObject() {
			return DocuConstants.CODE_ZNACHENIE_JOURNAL_COACHES;
		}


		/** */
		@Override
		public Integer getId() {
			return this.id;
		}

		/** */
		@Override
		public SystemJournal toSystemJournal() throws DbErrorException {
			SystemJournal journal = new SystemJournal(getCodeMainObject(), getId(), getIdentInfo());
			return journal;
		}
		
		@Override
		public String getIdentInfo() throws DbErrorException {
			if (idObject!=null) {
				return "Код #" + idObject;
			}else {
				return "";	
			}
			 
		}
		
		public String getRegNomer() {
			return regNomer;
		}

		public void setRegNomer(String regNomer) {
			this.regNomer = regNomer;
		}

		public Integer getIdObject() {
			return idObject;
		}

		public void setIdObject(Integer idObject) {
			this.idObject = idObject;
		}

		public Integer getStatus() {
			return status;
		}

		public void setStatus(Integer status) {
			this.status = status;
		}

		
	/**
		public List<MMSVidSportSC> getVidSportList() {
			return vidSportList;
		}

		public void setVidSportList(List<MMSVidSportSC> vidSportList) {
			this.vidSportList = vidSportList;
		}
		
	*/

		public String getDopInfo() {
			return dopInfo;
		}

		public void setDopInfo(String dopInfo) {
			this.dopInfo = dopInfo;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		public Date getDateStatus() {
			return dateStatus;
		}

		public void setDateStatus(Date dateStatus) {
			this.dateStatus = dateStatus;
		}

		public List<String> getParseMessages() {
			return parseMessages;
		}

		public void setParseMessages(List<String> parseMessages) {
			this.parseMessages = parseMessages;
		}

		public Integer getVidSport() {
			return vidSport;
		}

		public void setVidSport(Integer vidSport) {
			this.vidSport = vidSport;
		}

		public String getMailLice() {
			return mailLice;
		}

		public void setMailLice(String mailLice) {
			this.mailLice = mailLice;
		}

		public Integer getNachinPoluch() {
			return nachinPoluch;
		}

		public void setNachinPoluch(Integer nachinPoluch) {
			this.nachinPoluch = nachinPoluch;
		}

		public String getNachinPoluchText() {
			return nachinPoluchText;
		}

		public void setNachinPoluchText(String nachinPoluchText) {
			this.nachinPoluchText = nachinPoluchText;
		}

		public Integer getDlajnost() {
			return dlajnost;
		}

		public void setDlajnost(Integer dlajnost) {
			this.dlajnost = dlajnost;
		}

		
		
	
	}
