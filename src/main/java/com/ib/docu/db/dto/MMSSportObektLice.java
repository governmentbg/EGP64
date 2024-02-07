package com.ib.docu.db.dto;
import static javax.persistence.GenerationType.SEQUENCE;

import java.io.Serializable;	

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
 * MMSSportObektLice Entity
 *  Връзка на Спортен обект със собственици/наематели  
 */
@Entity
@Table(name = "mms_sport_obekt_lice")

public class MMSSportObektLice extends TrackableEntity  implements   AuditExt  {
  
	private static final long serialVersionUID = -545631164407428931L;

	@SequenceGenerator(name = "MMSSportObektLice", sequenceName = "seq_mms_sport_obekt_lice", allocationSize = 1)
	@Id
	@GeneratedValue(strategy = SEQUENCE, generator = "MMSSportObektLice")
	@Column(name = "ID")
//	@JournalAttr(label = "id", defaultText = "Системен идентификатор", isId = "true")
	private Integer id; // id
	
	@Column(name = "ID_SPORT_OBEKT")
	@JournalAttr(label = "idSportObekt",defaultText = "ID на спортен обект")
     private Integer idSportObekt;
	  
	@Column(name = "ID_OBJECT")  
	@JournalAttr(label = "idLice",defaultText = "ID на лице за връзка")
     private Integer idLice;                       // Тук се записва стойността на поле code от таблица adm_referents  с лицата
   	
	@JournalAttr(label = "ime", defaultText = "Име")
	@Column(name = "ime")
	private String ime;
	
	@JournalAttr(label = "prezime", defaultText = "Презиме")
	@Column(name = "prezime")
	private String prezime;
	
	@JournalAttr(label = "familia", defaultText = "Фамилия")
	@Column(name = "familia")
	private String familia;
	
	@Column(name = "TYPE_VRAZKA")
	@JournalAttr(label = "typeVrazka",defaultText = "Тип на връзка", classifID = ""+DocuConstants.CODE_CLASSIF_VRAZKA_LICE_SPORTEN_OBEKT)
     private Integer typeVrazka;
	
	@Transient
     private String  nameLice;      //  Тук се записва съчетание на име за лице и ЕИК/ЕГН и адрес
	

	/**  */
	public MMSSportObektLice() {
		super();
	}


	public Integer getIdSportObekt() {
		return idSportObekt;
	}

	public void setIdSportObekt(Integer idSportObekt) {
		this.idSportObekt = idSportObekt;
	}

	public Integer getIdLice() {
		return idLice;
	}

	public void setIdLice(Integer idLice) {
		this.idLice = idLice;
	}
	
	
	public String getNameLice() {
		return nameLice;
	}


	public void setNameLice(String nameLice) {
		this.nameLice = nameLice;
	}


	public Integer getTypeVrazka() {
		return typeVrazka;
	}

	public void setTypeVrazka(Integer typeVrazka) {
		this.typeVrazka = typeVrazka;
	}
	
	

	public String getIme() {
		return ime;
	}


	public void setIme(String ime) {
		this.ime = ime;
	}


	public String getPrezime() {
		return prezime;
	}


	public void setPrezime(String prezime) {
		this.prezime = prezime;
	}


	public String getFamilia() {
		return familia;
	}


	public void setFamilia(String familia) {
		this.familia = familia;
	}


	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

//	@Override
//	public Object getId() {
//		// TODO Auto-generated method stub
//		return null;
//	}
	     
	@Override
	public Integer getCodeMainObject() {
		// TODO Auto-generated method stub
		return DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORT_OBJECT_LICE;	
	}


	@Override
	public SystemJournal toSystemJournal() throws DbErrorException {
		// TODO Auto-generated method stub
		SystemJournal dj = new  SystemJournal();				
		dj.setCodeObject(getCodeMainObject());
		dj.setIdObject(getIdSportObekt());
		dj.setIdentObject("idLiceVrazka=" + getIdLice() + ",  typeVrazka=  " + getTypeVrazka());
		return dj;
	}
	
	
	
}

