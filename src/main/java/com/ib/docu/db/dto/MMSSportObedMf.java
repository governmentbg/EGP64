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
 * Спортно обединение 
 * @author s.marinov
 *
 */
@Entity
@Table(name = "mms_sport_obed_mf")
public class MMSSportObedMf extends TrackableEntity implements AuditExt{
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 3298619379079466866L;

	@SequenceGenerator(name = "MMSSportObedMf", sequenceName = "seq_mms_sport_obed_mf", allocationSize = 1)
	@Id
	@GeneratedValue(strategy = SEQUENCE, generator = "MMSSportObedMf")
	@Column(name = "id", unique = true, nullable = false)	
	private Integer id;
	
	@Column(name = "id_sport_obed", updatable = false)	
	private Integer idSportObed;

	
	
	@Column(name = "mejd_fed")  
	@JournalAttr(label="mejdFed",defaultText = "Международна федерация", classifID = ""+DocuConstants.CODE_CLASSIF_MMS_MEJD_FED)
	private Integer mejdFed;
	
	@Column(name = "mejd_fed_text")
	@JournalAttr(label = "mejdFedText",defaultText = "Международна федерация текст")
	private String mejdFedText;

	@Column(name = "date_beg")
	@JournalAttr(label = "date_beg", defaultText = "Начална дата", dateMask = "dd.MM.yyyy")
	private Date dateBeg;
	
	@Column(name = "date_end")
	@JournalAttr(label = "date_end", defaultText = "Крайна дата", dateMask = "dd.MM.yyyy")
	private Date dateEnd;

	@Column(name = "reg_nomer")
	@JournalAttr(label = "regNomer",defaultText = "Рег. номер")
	private String regNomer;
	
	@Column(name = "date_doc")
	@JournalAttr(label = "date_doc", defaultText = "Дата на документ", dateMask = "dd.MM.yyyy")
	private Date dateDoc;
  

	/** */
	public MMSSportObedMf() {
		super();
	}

	 

	/** */
	@Override
	public Integer getCodeMainObject() {
		return DocuConstants.CODE_ZNACHENIE_JOURNAL_MEJD_FED;
	}
 
	
	@Override
	public String getIdentInfo() throws DbErrorException {
		return getRegNomer();
	}

	@Override
	public SystemJournal toSystemJournal() throws DbErrorException {
		SystemJournal dj = new  SystemJournal();				
		dj.setCodeObject(getCodeMainObject());
		dj.setIdObject(getId());
		dj.setIdentObject(getIdentInfo());
		return dj;
	}


	 
	public Integer getId() {
		return id;
	}



	public void setId(Integer id) {
		this.id = id;
	}



	public Integer getIdSportObed() {
		return idSportObed;
	}



	public void setIdSportObed(Integer idSportObed) {
		this.idSportObed = idSportObed;
	}



	public Integer getMejdFed() {
		return mejdFed;
	}



	public void setMejdFed(Integer mejdFed) {
		this.mejdFed = mejdFed;
	}



	public Date getDateBeg() {
		return dateBeg;
	}



	public void setDateBeg(Date dateBeg) {
		this.dateBeg = dateBeg;
	}



	public Date getDateEnd() {
		return dateEnd;
	}



	public void setDateEnd(Date dateEnd) {
		this.dateEnd = dateEnd;
	}



	public String getRegNomer() {
		return regNomer;
	}



	public void setRegNomer(String regNomer) {
		this.regNomer = regNomer;
	}



	public Date getDateDoc() {
		return dateDoc;
	}



	public void setDateDoc(Date dateDoc) {
		this.dateDoc = dateDoc;
	}



	public String getMejdFedText() {
		return mejdFedText;
	}



	public void setMejdFedText(String mejdFedText) {
		this.mejdFedText = mejdFedText;
	}


 

	
}