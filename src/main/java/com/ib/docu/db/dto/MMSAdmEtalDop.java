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
 * Допълнителни полета
 *
 * @author dessy
 */
@Entity
@Table(name = "mms_adm_etal_dop ")
public class MMSAdmEtalDop extends TrackableEntity implements AuditExt {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2057096495483875936L;

	@SequenceGenerator(name = "MMSAdmEtalDop", sequenceName = "SEQ_MMS_ADM_ETAL_DOP", allocationSize = 1)
	@Id
	@GeneratedValue(strategy = SEQUENCE, generator = "MMSAdmEtalDop")
	@Column(name = "ID", unique = true, nullable = false)
	private Integer id;

	@Column(name = "CODE_OBJECT", nullable = false)
	@JournalAttr(label = "codeObject", defaultText = "Код на обекта")
	private Integer codeObject;
	
	@Column(name = "TIP_POLE", nullable = false)
	@JournalAttr(label = "tipPole", defaultText = "Тип на поле")
	private Integer tipPole;
	
	@Column(name = "IME_POLE", nullable = false)
	@JournalAttr(label = "imePole", defaultText = "Име на поле")
	private String imePole;
	
	@Column(name = "CLASIF")
	@JournalAttr(label = "clasif", defaultText = "Класификация")
	private Integer clasif;
	
	@Column(name = "ZAD", nullable = false)
	@JournalAttr(label = "zad", defaultText = "Задължителност на поле")
	private Integer zad;
	
	@Column(name = "POVT", nullable = false)
	@JournalAttr(label = "povt", defaultText = "Повторяемост на поле")
	private Integer povt;
	
	@Column(name = "PORED", nullable = false)
	@JournalAttr(label = "pored", defaultText = "Пореден номер на поле")
	private Integer pored;
	

	/** */
	public MMSAdmEtalDop() {
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
	 * @return the codeObject
	 */
	public Integer getCodeObject() {
		return codeObject;
	}

	/**
	 * @param codeObject the codeObject to set
	 */
	public void setCodeObject(Integer codeObject) {
		this.codeObject = codeObject;
	}
	
	/**
	 * @return the tipPole
	 */
	public Integer getTipPole() {
		return tipPole;
	}

	/**
	 * @param tipPole the tipPole to set
	 */
	public void setTipPole(Integer tipPole) {
		this.tipPole = tipPole;
	}
	
	/**
	 * @return the imePole
	 */
	public String getImePole() {
		return imePole;
	}

	/**
	 * @param imePole the imePole to set
	 */
	public void setImePole(String imePole) {
		this.imePole = imePole;
	}

	/**
	 * @return the clasif
	 */
	public Integer getClasif() {
		return clasif;
	}

	/**
	 * @param clasif the clasif to set
	 */
	public void setClasif(Integer clasif) {
		this.clasif = clasif;
	}

	/**
	 * @return the zad
	 */
	public Integer getZad() {
		return zad;
	}

	/**
	 * @param zad the zad to set
	 */
	public void setZad(Integer zad) {
		this.zad = zad;
	}
	
	/**
	 * @return the povt
	 */
	public Integer getPovt() {
		return povt;
	}
	
	/**
	 * @param povt the povt to set
	 */
	public void setPovt(Integer povt) {
		this.povt = povt;
	}

	/**
	 * @return the pored
	 */
	public Integer getPored() {
		return pored;
	}

	/**
	 * @param pored the pored to set
	 */
	public void setPored(Integer pored) {
		this.pored = pored;
	}

	/** */
	@Override
	public Integer getCodeMainObject() {
		return DocuConstants.CODE_ZNACHENIE_JOURNAL_DOP_POLE;
	}
	
	/** */
	@Override
	public String getIdentInfo() throws DbErrorException {
		return "ИД на поле: " + this.id;
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