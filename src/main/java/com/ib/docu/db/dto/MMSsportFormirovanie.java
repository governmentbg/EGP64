package com.ib.docu.db.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Where;

import com.ib.docu.system.DocuConstants;
import com.ib.indexui.system.Constants;
import com.ib.system.db.AuditExt;
import com.ib.system.db.JournalAttr;
import com.ib.system.db.TrackableEntity;
import com.ib.system.db.dto.SystemJournal;
import com.ib.system.exceptions.DbErrorException;

/**
 * Entity implementation class for Entity: MMSsportFormirovanie
 *
 */
@Entity
@Table(name="mms_sport_formirovanie")
public class MMSsportFormirovanie extends TrackableEntity implements Serializable, AuditExt {

	   
	@Id
	@SequenceGenerator( name = "MMSsportFormirovanie", sequenceName = "seq_mms_sport_formirovanie", allocationSize = 1)
	@GeneratedValue( strategy = GenerationType.SEQUENCE, generator = "MMSsportFormirovanie")
	@Column(name = "ID", unique = true, nullable = false)
	private Integer id;
	
	@Column(name = "type_sport")
	@JournalAttr(label = "typeSport", defaultText = "Олимпийски спортове", classifID = ""+ DocuConstants.CODE_CLASSIF_DANE)
	private Integer typeSport;
	
	@Column(name = "reg_nomer")
	@JournalAttr(label = "regNomer", defaultText = "Рег. номер на Спортно формирование в регистъра")
	private String regNomer;
	
	@Column(name = "id_object")
	@JournalAttr(label = "idObject", defaultText = "От регистъра на лицата")
	private Integer idObject;
	
	@Column(name = "vid")
	@JournalAttr(label = "vid", defaultText = "Видове спорт - много", classifID = ""+ DocuConstants.CODE_CLASSIF_VIDOVE_SPORT)
	private Integer vid;

	@Column(name = "predstavitelstvo")
//	@JournalAttr(label = "predstavitelstvo", defaultText = "Представителство")
	private String predstavitelstvo;
	
	@Column(name = "predsedatel")
	@JournalAttr(label = "predsedatel", defaultText = "Председател")
	private String predsedatel;
	
	
	@Column(name = "school_name")
	@JournalAttr(label = "schoolName", defaultText = "Наименование на висшето училище, в което действа клубът")
	private String schoolName;
	
	@Column(name = "status")
	@JournalAttr(label = "status", defaultText = "Статус на формированието" , classifID = ""+ DocuConstants.CODE_CLASSIF_STATUS_REGISTRATION)
	private Integer status;
	
	@Column(name = "date_status")
	@JournalAttr(label = "dateStatus", defaultText = "Дата на статуса")
	private Date dateStatus;
	
	@Column(name = "dop_info")
	@JournalAttr(label = "dopInfo", defaultText = "Допълнителна информация")
	private String dopInfo;
	
	@Column(name = "univers")
	@JournalAttr(label = "univers", defaultText = "Университетски клубове")
	private Integer univers;
	
	@Column(name = "osnovanie_univers")
	@JournalAttr(label = "osnovanie_univers", defaultText = "Основание за учредяване на университетски клуб")
	private String osnovanieUnivers;
	
	@Column(name = "voenen_sport")  
	@JournalAttr(label="voenenSport",defaultText = "Военноприложни спортове",classifID = ""+DocuConstants.CODE_CLASSIF_DANE)
	private Integer voenenSport;

	/*
	 * @Column(name = "date_reg_sf")
	 * 
	 * @JournalAttr(label = "dateRegSf", defaultText = "") private Date dateRegSf;
	 */
	
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	@JoinColumn(name = "id_object", referencedColumnName = "id" , nullable = false)
	@Where(clause = " tip_object =" + DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_FORMATIONS)
	private List<MMSVidSportSF> vidSportList = new ArrayList<>();
	
	/*
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	@JoinColumn(name = "id_object", referencedColumnName = "id")
	*/
	
	@Transient
	//@JournalAttr(label = "mmsChlenList", defaultText = " Участие в членства ")
	private List<MMSChlenstvo> mmsChlenList = new ArrayList<>(); 
	@Transient
	private boolean vidB;
	@Transient
	private boolean vidVoenenB;
	
	@Transient
	private String mailLice=null;
	@Transient
	private Integer nachinPoluch=null;
	@Transient
	private String nachinPoluchText=null;
	@Transient
	private List<String> parseMessages=new ArrayList<String>();
	
	private static final long serialVersionUID = 1L;

	public MMSsportFormirovanie() {
		super();
	}   
	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}   
	public Integer getTypeSport() {
		return this.typeSport;
	}

	public void setTypeSport(Integer typeSport) {
		this.typeSport = typeSport;
	}   
	public String getRegNomer() {
		return this.regNomer;
	}

	public void setRegNomer(String regNomer) {
		this.regNomer = regNomer;
	}   
	public Integer getIdObject() {
		return this.idObject;
	}

	public void setIdObject(Integer idObject) {
		this.idObject = idObject;
	}   
	public Integer getVid() {
		return this.vid;
	}

	public void setVid(Integer vid) {
		this.vid = vid;
	}   
	public String getPredstavitelstvo() {
		return this.predstavitelstvo;
	}

	public void setPredstavitelstvo(String predstavitelstvo) {
		this.predstavitelstvo = predstavitelstvo;
	}   
	public String getPredsedatel() {
		return this.predsedatel;
	}

	public void setPredsedatel(String predsedatel) {
		this.predsedatel = predsedatel;
	}   

	public String getSchoolName() {
		return this.schoolName;
	}

	public void setSchoolName(String schoolName) {
		this.schoolName = schoolName;
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
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}

	@Override
	public String getIdentInfo() throws DbErrorException {
		return this.regNomer;
	}

	/*
	 * public Date getDateRegSf() { return dateRegSf; } public void
	 * setDateRegSf(Date dateRegSf) { this.dateRegSf = dateRegSf; }
	 */
	public List<MMSChlenstvo> getMmsChlenList() {
		return mmsChlenList;
	}
	public void setMmsChlenList(List<MMSChlenstvo> mmsChlenList) {
		this.mmsChlenList = mmsChlenList;
	}
	public List<MMSVidSportSF> getVidSportList() {
		return vidSportList;
	}
	public void setVidSportList(List<MMSVidSportSF> vidSportList) {
		this.vidSportList = vidSportList;
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
	public boolean isVidB() {
		if(this.typeSport != null && this.typeSport == Constants.CODE_ZNACHENIE_DA)
			vidB = true;
		else
			vidB = false;
		return vidB;
	}
	public void setVidB(boolean vidB) {
		this.vidB = vidB;
		if(vidB)
			this.typeSport = Constants.CODE_ZNACHENIE_DA;
		else
			this.typeSport = Constants.CODE_ZNACHENIE_NE;
	}
	public boolean isVidVoenenB() {
		if(this.voenenSport != null && this.voenenSport == Constants.CODE_ZNACHENIE_DA)
			vidVoenenB = true;
		else
			vidVoenenB = false;
		return vidVoenenB;
	}
	public void setVidVoenenB(boolean vidVoenenB) {
		this.vidVoenenB = vidVoenenB;
		if(vidVoenenB)
			this.voenenSport = Constants.CODE_ZNACHENIE_DA;
		else
			this.voenenSport = Constants.CODE_ZNACHENIE_NE;
	}
	public Integer getUnivers() {
		return univers;
	}
	public void setUnivers(Integer univers) {
		this.univers = univers;
	}
	public boolean getUniversBol() {
		if(univers != null && univers.intValue() == DocuConstants.CODE_ZNACHENIE_DA)
			return true;
		else
			return false;
	}
	public void setUniversBol(boolean universBol) {
		if(universBol)
			univers = DocuConstants.CODE_ZNACHENIE_DA;
		else
			univers = DocuConstants.CODE_ZNACHENIE_NE;
	}
	
	public String getOsnovanieUnivers() {
		return osnovanieUnivers;
	}
	public void setOsnovanieUnivers(String osnovanieUnivers) {
		this.osnovanieUnivers = osnovanieUnivers;
	}
	public List<String> getParseMessages() {
		return parseMessages;
	}
	public void setParseMessages(List<String> parseMessages) {
		this.parseMessages = parseMessages;
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
	
	/** @return the voenenSport */
	public Integer getVoenenSport() {
		return this.voenenSport;
	}
	/** @param voenenSport the voenenSport to set */
	public void setVoenenSport(Integer voenenSport) {
		this.voenenSport = voenenSport;
	}
}
