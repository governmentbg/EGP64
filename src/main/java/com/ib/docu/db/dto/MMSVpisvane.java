package com.ib.docu.db.dto;

import static javax.persistence.GenerationType.SEQUENCE;

import java.util.Date;

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
 * Вписване
 *
 * @author dessy
 */
@Entity
@Table(name = "mms_vpisvane")
public class MMSVpisvane extends TrackableEntity implements AuditExt {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1521753336890703450L;

	@SequenceGenerator(name = "MMSVpisvane", sequenceName = "SEQ_MMS_VPISVANE", allocationSize = 1)
	@Id
	@GeneratedValue(strategy = SEQUENCE, generator = "MMSVpisvane")
	@Column(name = "ID", unique = true, nullable = false)
	private Integer id;

	@Column(name = "TYPE_OBJECT")
	@JournalAttr(label = "typeObject", defaultText = "Тип на обекта")
	private Integer typeObject;
	
	@Column(name = "ID_OBJECT")
	@JournalAttr(label = "idObject", defaultText = "ИД на обекта")
	private Integer idObject;
	
	@Column(name = "RN_DOC_ZAIAVLENIE")
	@JournalAttr(label = "rnDocZaiavlenie", defaultText = "Рег. номер на документ за заявление")
	private String rnDocZaiavlenie;
	
	@Column(name = "DATE_DOC_ZAIAVLENIE")
	@JournalAttr(label="dateDocZaiavlenie",defaultText = "Дата на документ за заявление", dateMask = "dd.MM.yyyy HH:mm:ss")
	private Date dateDocZaiavlenie;
	
	@Column(name = "STATUS_RESULT_ZAIAVLENIE")
	@JournalAttr(label = "statusResultZaiavlenie", defaultText = "Статус на заявление", classifID = "" )
	private Integer statusResultZaiavlenie;
	
	@Column(name = "REASON_RESULT")
	@JournalAttr(label = "reasonResult", defaultText = "Основание за вписване или отказ", classifID = "" )
	private Integer reasonResult;
	
	@Column(name = "REASON_RESULT_TEXT")
	@JournalAttr(label = "reasonResultText", defaultText = "Основание за вписване или отказ - текст")
	private String reasonResultText;

	@Column(name = "RN_DOC_RESULT")
	@JournalAttr(label = "rnDocResult", defaultText = "Рег. номер на документ за вписване или отказ")
	private String rnDocResult;
	
	@Column(name = "DATE_DOC_RESULT")
	@JournalAttr(label="dateDocResult",defaultText = "Дата на документ за вписване или отказ", dateMask = "dd.MM.yyyy HH:mm:ss")
	private Date dateDocResult;	
	
	@Column(name = "RN_DOC_LICENZ")
	@JournalAttr(label = "rnDocLicenz", defaultText = "Рег. номер на документ за лиценза")
	private String rnDocLicenz;
	
	@Column(name = "DATE_DOC_LICENZ")
	@JournalAttr(label="dateDocLicenz",defaultText = "Дата на документ за лиценза", dateMask = "dd.MM.yyyy HH:mm:ss")
	private Date dateDocLicenz;	
	
	@Column(name = "STATUS_VPISVANE")
	@JournalAttr(label = "statusVpisvane", defaultText = "Статус на вписване", classifID = "" )
	private Integer statusVpisvane;
	
	@Column(name = "REASON_VPISVANE")
	@JournalAttr(label = "reasonVpisvane", defaultText = "Основание за прекратяване/отнемане/заличаване", classifID = "" )
	private Integer reasonVpisvane;
	
	@Column(name = "REASON_VPISVANE_TEXT")
	@JournalAttr(label = "reasonVpisvaneText", defaultText = "Основание за за прекратяване/отнемане/заличаване - текст")
	private String reasonVpisvaneText;
	
	@Column(name = "RN_DOC_VPISVANE")
	@JournalAttr(label = "rnDocVpisvane", defaultText = "Рег. номер на документ за прекратяване, отнемане, заличаване")
	private String rnDocVpisvane;
	
	@Column(name = "DATE_DOC_VPISVANE")
	@JournalAttr(label="dateDocVpisvane",defaultText = "Дата на документ за прекратяване, отнемане, заличаване", dateMask = "dd.MM.yyyy HH:mm:ss")
	private Date dateDocVpisvane;

	@Column(name = "NACHIN_POLUCHAVANE")
	@JournalAttr(label = "nachinPoluchavane", defaultText = "Начин на получаване", classifID = "" + DocuConstants.CODE_CLASSIF_DVIJ_METHOD)
	private Integer nachinPoluchavane;
	
	@Column(name = "ADDR_MAIL_POLUCHAVANE")
	@JournalAttr(label = "addrMailPoluchavane", defaultText = "Адрес или мейл на получаване")
	private String addrMailPoluchavane;
	
	@Column(name = "DATE_STATUS_ZAIAVLENIE")
	@JournalAttr(label="dateStatusZaiavlenie",defaultText = "Дата на статуса на заявление", dateMask = "dd.MM.yyyy HH:mm:ss")
	private Date dateStatusZaiavlenie;
	
	@Column(name = "VID_SPORT")
	@JournalAttr(label = "vidSport", defaultText = "Вид спорт", classifID = "" + DocuConstants.CODE_CLASSIF_VIDOVE_SPORT)
	private Integer vidSport;
	
	@Column(name = "DLAJNOST")
	@JournalAttr(label = "dlajnost", defaultText = "Длажност", classifID = "" + DocuConstants.CODE_CLASSIF_DLAJNOST)
	private Integer dlajnost;	
	
	@Column(name = "DOP_INFO")
	@JournalAttr(label = "dopInfo", defaultText = "Доп. инфо")
	private String dopInfo;
	
	@Column(name = "DATE_STATUS_VPISVANE")
	@JournalAttr(label="dateStatusVpisvane",defaultText = "Дата на статуса  на вписване", dateMask = "dd.MM.yyyy HH:mm:ss")
	private Date dateStatusVpisvane;
	
	@Column(name = "DATE_DOC_RECEIVE")
	@JournalAttr(label="dateDocReceive",defaultText = "Дата на получаване на документа", dateMask = "dd.MM.yyyy HH:mm:ss")
	private Date dateDocReceive;
	

	/** */
	public MMSVpisvane() {
		super();
	}		
	
	/**
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Integer id) {
		this.id = id;
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
	 * @return the rnDocZaiavlenie
	 */
	public String getRnDocZaiavlenie() {
		return rnDocZaiavlenie;
	}

	/**
	 * @param rnDocZaiavlenie the rnDocZaiavlenie to set
	 */
	public void setRnDocZaiavlenie(String rnDocZaiavlenie) {
		this.rnDocZaiavlenie = rnDocZaiavlenie;
	}

	/**
	 * @return the dateDocZaiavlenie
	 */
	public Date getDateDocZaiavlenie() {
		return dateDocZaiavlenie;
	}

	/**
	 * @param dateDocZaiavlenie the dateDocZaiavlenie to set
	 */
	public void setDateDocZaiavlenie(Date dateDocZaiavlenie) {
		this.dateDocZaiavlenie = dateDocZaiavlenie;
	}

	/**
	 * @return the statusResultZaiavlenie
	 */
	public Integer getStatusResultZaiavlenie() {
		return statusResultZaiavlenie;
	}

	/**
	 * @param statusResultZaiavlenie the statusResultZaiavlenie to set
	 */
	public void setStatusResultZaiavlenie(Integer statusResultZaiavlenie) {
		this.statusResultZaiavlenie = statusResultZaiavlenie;
	}

	/**
	 * @return the reasonResult
	 */
	public Integer getReasonResult() {
		return reasonResult;
	}

	/**
	 * @param reasonResult the reasonResult to set
	 */
	public void setReasonResult(Integer reasonResult) {
		this.reasonResult = reasonResult;
	}

	/**
	 * @return the reasonResultText
	 */
	public String getReasonResultText() {
		return reasonResultText;
	}

	/**
	 * @param reasonResultText the reasonResultText to set
	 */
	public void setReasonResultText(String reasonResultText) {
		this.reasonResultText = reasonResultText;
	}

	/**
	 * @return the rnDocResult
	 */
	public String getRnDocResult() {
		return rnDocResult;
	}

	/**
	 * @param rnDocResult the rnDocResult to set
	 */
	public void setRnDocResult(String rnDocResult) {
		this.rnDocResult = rnDocResult;
	}

	/**
	 * @return the dateDocResult
	 */
	public Date getDateDocResult() {
		return dateDocResult;
	}

	/**
	 * @param dateDocResult the dateDocResult to set
	 */
	public void setDateDocResult(Date dateDocResult) {
		this.dateDocResult = dateDocResult;
	}

	/**
	 * @return the rnDocLicenz
	 */
	public String getRnDocLicenz() {
		return rnDocLicenz;
	}

	/**
	 * @param rnDocLicenz the rnDocLicenz to set
	 */
	public void setRnDocLicenz(String rnDocLicenz) {
		this.rnDocLicenz = rnDocLicenz;
	}

	/**
	 * @return the dateDocLicenz
	 */
	public Date getDateDocLicenz() {
		return dateDocLicenz;
	}

	/**
	 * @param dateDocLicenz the dateDocLicenz to set
	 */
	public void setDateDocLicenz(Date dateDocLicenz) {
		this.dateDocLicenz = dateDocLicenz;
	}

	/**
	 * @return the statusVpisvane
	 */
	public Integer getStatusVpisvane() {
		return statusVpisvane;
	}

	/**
	 * @param statusVpisvane the statusVpisvane to set
	 */
	public void setStatusVpisvane(Integer statusVpisvane) {
		this.statusVpisvane = statusVpisvane;
	}

	/**
	 * @return the reasonVpisvane
	 */
	public Integer getReasonVpisvane() {
		return reasonVpisvane;
	}

	/**
	 * @param reasonVpisvane the reasonVpisvane to set
	 */
	public void setReasonVpisvane(Integer reasonVpisvane) {
		this.reasonVpisvane = reasonVpisvane;
	}

	/**
	 * @return the reasonVpisvaneText
	 */
	public String getReasonVpisvaneText() {
		return reasonVpisvaneText;
	}

	/**
	 * @param reasonVpisvaneText the reasonVpisvaneText to set
	 */
	public void setReasonVpisvaneText(String reasonVpisvaneText) {
		this.reasonVpisvaneText = reasonVpisvaneText;
	}

	/**
	 * @return the rnDocVpisvane
	 */
	public String getRnDocVpisvane() {
		return rnDocVpisvane;
	}

	/**
	 * @param rnDocVpisvane the rnDocVpisvane to set
	 */
	public void setRnDocVpisvane(String rnDocVpisvane) {
		this.rnDocVpisvane = rnDocVpisvane;
	}

	/**
	 * @return the dateDocVpisvane
	 */
	public Date getDateDocVpisvane() {
		return dateDocVpisvane;
	}

	/**
	 * @param dateDocVpisvane the dateDocVpisvane to set
	 */
	public void setDateDocVpisvane(Date dateDocVpisvane) {
		this.dateDocVpisvane = dateDocVpisvane;
	}

	/**
	 * @return the nachinPoluchavane
	 */
	public Integer getNachinPoluchavane() {
		return nachinPoluchavane;
	}

	/**
	 * @param nachinPoluchavane the nachinPoluchavane to set
	 */
	public void setNachinPoluchavane(Integer nachinPoluchavane) {
		this.nachinPoluchavane = nachinPoluchavane;
	}

	/**
	 * @return the addrMailPoluchavane
	 */
	public String getAddrMailPoluchavane() {
		return addrMailPoluchavane;
	}

	/**
	 * @param addrMailPoluchavane the addrMailPoluchavane to set
	 */
	public void setAddrMailPoluchavane(String addrMailPoluchavane) {
		this.addrMailPoluchavane = addrMailPoluchavane;
	}

	/**
	 * @return the dateStatusZaiavlenie
	 */
	public Date getDateStatusZaiavlenie() {
		return dateStatusZaiavlenie;
	}

	/**
	 * @param dateStatusZaiavlenie the dateStatusZaiavlenie to set
	 */
	public void setDateStatusZaiavlenie(Date dateStatusZaiavlenie) {
		this.dateStatusZaiavlenie = dateStatusZaiavlenie;
	}

	/**
	 * @return the vidSport
	 */
	public Integer getVidSport() {
		return vidSport;
	}

	/**
	 * @param vidSport the vidSport to set
	 */
	public void setVidSport(Integer vidSport) {
		this.vidSport = vidSport;
	}

	/**
	 * @return the dlajnost
	 */
	public Integer getDlajnost() {
		return dlajnost;
	}

	/**
	 * @param dlajnost the dlajnost to set
	 */
	public void setDlajnost(Integer dlajnost) {
		this.dlajnost = dlajnost;
	}

	/**
	 * @return the dopInfo
	 */
	public String getDopInfo() {
		return dopInfo;
	}

	/**
	 * @param dopInfo the dopInfo to set
	 */
	public void setDopInfo(String dopInfo) {
		this.dopInfo = dopInfo;
	}	

	/**
	 * @return the dateStatusVpisvane
	 */
	public Date getDateStatusVpisvane() {
		return dateStatusVpisvane;
	}

	/**
	 * @param dateStatusVpisvane the dateStatusVpisvane to set
	 */
	public void setDateStatusVpisvane(Date dateStatusVpisvane) {
		this.dateStatusVpisvane = dateStatusVpisvane;
	}

	/**
	 * @return the dateDocReceive
	 */
	public Date getDateDocReceive() {
		return dateDocReceive;
	}

	/**
	 * @param dateDocReceive the dateDocReceive to set
	 */
	public void setDateDocReceive(Date dateDocReceive) {
		this.dateDocReceive = dateDocReceive;
	}

	/** */
	@Override
	public Integer getCodeMainObject() {
		return DocuConstants.CODE_ZNACHENIE_JOURNAL_REGISTRATION;
	}
	
	/** */
	@Override
	public String getIdentInfo() throws DbErrorException {
		return "ИД на вписване: " + this.id + " със статус " + this.statusResultZaiavlenie;
	}

	/** */
	@Override
	public SystemJournal toSystemJournal() throws DbErrorException {
		SystemJournal dj = new SystemJournal();
		dj.setCodeObject(getCodeMainObject());
		dj.setIdObject(getId());
		dj.setIdentObject(getIdentInfo());
		return dj;
	}
}