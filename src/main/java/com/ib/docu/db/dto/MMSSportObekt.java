package com.ib.docu.db.dto;

import static javax.persistence.GenerationType.SEQUENCE;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

import com.ib.docu.system.DocuConstants;
import com.ib.system.db.AuditExt;
import com.ib.system.db.JournalAttr;
import com.ib.system.db.TrackableEntity;
import com.ib.system.db.dto.SystemJournal;
import com.ib.system.exceptions.DbErrorException;

import org.hibernate.annotations.Where;

/**
 * Entity
 * Спортен обект 
 *
 */
@Entity
@Table(name = "mms_sport_obekt")
public class MMSSportObekt extends TrackableEntity implements AuditExt{
	
	/**
	 *       
	 */
	private static final long serialVersionUID = 3298619379079466866L;
		
	@SequenceGenerator(name = "MMSSportObekt", sequenceName = "seq_mms_sport_obekt", allocationSize = 1)
	@Id
	@GeneratedValue(strategy = SEQUENCE, generator = "MMSSportObekt")
	@Column(name = "id", unique = true, nullable = false)	
	private Integer id;
	 
	
	@Column(name = "vid")
	@JournalAttr(label = "vidObekt",defaultText = "Вид спортен обект"  ,classifID = ""+DocuConstants.CODE_CLASSIF_VID_SPORTEN_OBEKT)
	private Integer vidObekt;
	
	@Column(name = "name")
	@JournalAttr(label = "name",defaultText = "Име на спортен обект")
	private String name;
	
	@Column(name = "reg_nomer")
	@JournalAttr(label = "regNomer",defaultText = "Рег. номер")
	private String  regNomer;
		   
	@Column(name = "funk_category")
	@JournalAttr(label = "funkCategory",defaultText = "Функц. категория",classifID = ""+DocuConstants.CODE_CLASSIF_FUNC_CATEGORIA_SPORTEN_OBEKT)
	private Integer funkCategory;
	
	@Column(name = "identification")
	@JournalAttr(label = "identif",defaultText = "Идентификация")
	private String identif;
	
	@Column(name = "opisanie")
	@JournalAttr(label = "opisanie",defaultText = "Описание за спортен обект")
	private String opisanie;
	
	@Column(name = "country")
	@JournalAttr(label = "country",defaultText = "Държава" ,classifID = ""+DocuConstants. CODE_CLASSIF_COUNTRIES)
	private Integer country;
	
//	@Column(name = "oblast")
//	@JournalAttr(label = "oblast",defaultText = "Област")
//	private Integer oblast;
//	
//	@Column(name = "obshtina")
//	@JournalAttr(label = "obshtina",defaultText = "Община")
//	private Integer obshtina;
	
	@Column(name = "nas_mesto")
	@JournalAttr(label = "nas_mesto",defaultText = "Нас. место")
	private Integer nas_mesto;
	
	@Column(name = "sgrada")
	@JournalAttr(label = "adres",defaultText = "Адрес")
	private String adres;
	
	@Column(name = "post_code")
	@JournalAttr(label = "postCode",defaultText = "Пощ. код")
	private String postCode;
	
	@Column(name = "e_mail")
	@JournalAttr(label = "e_mail",defaultText = "Електр. поща")
	private String e_mail;
	
	@Column(name = "tel")
	@JournalAttr(label = "tel",defaultText = "Телефони")
	private String tel;
	
	@Column(name = "status")  
	@JournalAttr(label="status",defaultText = "Статус на спортен обект",classifID = ""+DocuConstants.CODE_CLASSIF_STATUS_REGISTRATION)
	private Integer status;
	
	@Column(name = "date_status")  
	@JournalAttr(label="dateStatus",defaultText = "Дата статус на спортен обект", dateMask = "dd.MM.yyyy HH:mm:ss")
	private Date dateStatus;
	
	
	@Column(name = "dop_info")
	@JournalAttr(label = "dopInfo",defaultText = "Допълн. инф.")
	private String dopInfo;
	  
	// Видове спорт за спортен обект
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	@JoinColumn(name = "id_object", referencedColumnName = "id" , nullable = false)
	@Where(clause = " tip_object =" + DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_OBJECTS)
	private List<MMSVidSportSpOb> vidSportList = new ArrayList<>();
	
	@Transient
	private String  obsht_obl_t;            // Тук се записва работно община, област
	
	// Съобщения за грешки при парсване на пдф
	@Transient
	private List<String> parseMessages=new ArrayList<String>();
	@Transient
	private Integer nachinPoluch=null;
	@Transient
	private String nachinPoluchText=null;
	
	//при парсване на пдф тук се подават лицата (idLice и typeVrazka)
	@Transient
	private List<MMSSportObektLice> spObLice=new ArrayList<MMSSportObektLice>();
	
	/** */  
	public MMSSportObekt() {
		super();
	}
	  
	
	/** */
	@Override
	public Integer getCodeMainObject() {
		return DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_OBJECTS	;
	}
 
	
	
	@Override
	public SystemJournal toSystemJournal() throws DbErrorException {
		SystemJournal dj = new  SystemJournal();				
		dj.setCodeObject(getCodeMainObject());
		dj.setIdObject(getId());
		dj.setIdentObject(getIdentInfo());
		return dj;
	}


	 
	@Override
	public String getIdentInfo() throws DbErrorException {
		StringBuilder ident = new StringBuilder();

		if (this.name != null) {
			ident.append(this.name);
		}
		if (this.regNomer != null) {
			if (ident.length() > 0) {
				ident.append(" /");
			}
			ident.append(this.regNomer);
		}
		return ident.toString();
	}


	public Integer getId() {
		return id;
	}



	public void setId(Integer id) {
		this.id = id;
	}

	
	public Integer getVidObekt() {
		return vidObekt;
	}


	public void setVidObekt(Integer vidObekt) {
		this.vidObekt = vidObekt;
	}

    
	
	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}



	public String getRegNomer() {
		return regNomer;
	}


	public void setRegNomer(String regNomer) {
		this.regNomer = regNomer;
	}


	public Integer getCountry() {
		return country;
	}


	public void setCountry(Integer country) {
		this.country = country;
	}


	public Integer getFunkCategory() {
		return funkCategory;
	}


	public void setFunkCategory(Integer funkCategory) {
		this.funkCategory = funkCategory;
	}


	public String getIdentif() {
		return identif;
	}


	public void setIdentif(String identif) {
		this.identif = identif;
	}


//	public Integer getCountry() {
//		return country;
//	}
//
//
//	public void setCountry(Integer country) {
//		this.country = country;
//	}


//	public Integer getOblast() {
//		return oblast;
//	}
//
//
//	public void setOblast(Integer oblast) {
//		this.oblast = oblast;
//	}
//
//
//	public Integer getObshtina() {
//		return obshtina;
//	}
//
//
//	public void setObshtina(Integer obshtina) {
//		this.obshtina = obshtina;
//	}

	

	public Integer getNas_mesto() {
		return nas_mesto;
	}


	public String getObsht_obl_t() {
		return obsht_obl_t;
	}


	public void setObsht_obl_t(String obsht_obl_t) {
		this.obsht_obl_t = obsht_obl_t;
	}


	public void setNas_mesto(Integer nas_mesto) {
		this.nas_mesto = nas_mesto;
	}


	public String getAdres() {
		return adres;
	}


	public void setAdres(String adres) {
		this.adres = adres;
	}


	public String getPostCode() {
		return postCode;
	}


	public void setPostCode(String postCode) {
		this.postCode = postCode;
	}


	
	public String getE_mail() {
		return e_mail;
	}


	public void setE_mail(String e_mail) {
		this.e_mail = e_mail;
	}

	

	public String getTel() {
		return tel;
	}


	public void setTel(String tel) {
		this.tel = tel;
	}


	public Integer getStatus() {
		return status;
	}


	public void setStatus(Integer status) {
		this.status = status;
	}


	public Date getDateStatus() {
		return dateStatus;
	}


	public void setDateStatus(Date dateStatus) {
		this.dateStatus = dateStatus;
	}


	public String getDopInfo() {
		return dopInfo;
	}


	public void setDopInfo(String dopInfo) {
		this.dopInfo = dopInfo;
	}


	public String getOpisanie() {
		return opisanie;
	}


	public void setOpisanie(String opisanie) {
		this.opisanie = opisanie;
	}


	public List<MMSVidSportSpOb> getVidSportList() {
		return vidSportList;
	}


	public void setVidSportList(List<MMSVidSportSpOb> vidSportList) {
		this.vidSportList = vidSportList;
	}


	public List<String> getParseMessages() {
		return parseMessages;
	}


	public void setParseMessages(List<String> parseMessages) {
		this.parseMessages = parseMessages;
	}


	public List<MMSSportObektLice> getSpObLice() {
		return spObLice;
	}


	public void setSpObLice(List<MMSSportObektLice> spObLice) {
		this.spObLice = spObLice;
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


	
	
//	public Integer getOblast() {
//		return oblast;
//		
//	}
//
//
//	public void setOblast(Integer oblast) {
//		this.oblast = oblast;
//		
//	}
//
//
//	public Integer getObshtina() {
//		return obshtina;
//		
//	}
//
//
//	public void setObshtina(Integer obshtina) {
//		this.obshtina = obshtina;
//		
//	}


	
	
	
//	public List<MMSSportObektLice> getSportObektLica() {
//		return sportObektLica;
//	}
//
//
//	public void setSportObektLica(List<MMSSportObektLice> sportObektLica) {
//		this.sportObektLica = sportObektLica;
//	}




	/** Зададени лица собственици/наематели на спортния обект */
//	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
//	@JournalAttr(label =  "MMSSportenObektLice", defaultText = "Собственици/наематели")
//	@JoinColumn(name = "ID_SPORT_OBEKT", referencedColumnName = "ID", nullable = false)
//	private List<MMSSportObektLice> sportObektLica;
	
		
}

