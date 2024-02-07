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

import org.hibernate.annotations.Where;

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
@Table(name = "mms_sport_obedinenie")
public class MMSSportnoObedinenie extends TrackableEntity implements AuditExt{
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 3298619379079466866L;

	@SequenceGenerator(name = "MMSSportnoObedinenie", sequenceName = "seq_mms_sport_obedninenie", allocationSize = 1)
	@Id
	@GeneratedValue(strategy = SEQUENCE, generator = "MMSSportnoObedinenie")
	@Column(name = "id", unique = true, nullable = false)	
	private Integer id;

	@Column(name = "type_sport")  
	@JournalAttr(label="typeSport",defaultText = "Олимпийски спортове",classifID = ""+DocuConstants.CODE_CLASSIF_DANE)
	private Integer typeSport;
	
	@Column(name = "voenen_sport")  
	@JournalAttr(label="voenenSport",defaultText = "Военноприложни спортове",classifID = ""+DocuConstants.CODE_CLASSIF_DANE)
	private Integer voenenSport;
	 

	@Column(name = "reg_nomer")
	@JournalAttr(label = "regNomer",defaultText = "Рег. номер")
	private String regNomer;
 
	@Column(name = "id_object")
	@JournalAttr(label="idObject",defaultText = "Ид на юридическо лице")
	private Integer idObject;
	
	@Column(name = "vid")  
	@JournalAttr(label="vid",defaultText = "Вид обединение",classifID = ""+DocuConstants.CODE_CLASSIF_VID_SPORT_OBEDINENIE)
	private Integer vid;
	
	@Column(name = "predstavitelstvo")
//	@JournalAttr(label = "predstavitelstvo",defaultText = "Представителство")
	private String predstavitelstvo;
	
	@Column(name = "predsedatel")
	@JournalAttr(label = "predsedatel",defaultText = "Председател")
	private String predsedatel;
	
	@Column(name = "gen_sek_direktor")
	@JournalAttr(label = "gen_sek_direktor",defaultText = "Ген. секретар/ Изп. директор")
	private String genSekDirektor;
	
	@Column(name = "br_chlenove")
	@JournalAttr(label="brChlenove",defaultText = "Брой членуващи в обединението")
	private Integer brChlenove;
	
	@Column(name = "status")  
	@JournalAttr(label="status",defaultText = "Статус на обединение",classifID = ""+DocuConstants.CODE_CLASSIF_STATUS_REGISTRATION)
	private Integer status;
	 
	@Column(name = "date_status")
	@JournalAttr(label = "dateStatus", defaultText = "Дата на статуса")
	private Date dateStatus;
	
	@Column(name = "dop_info")
	@JournalAttr(label = "dopInfo", defaultText = "Допълнителна информация")
	private String dopInfo;
	
	@Column(name = "vid_sport_text")
	@JournalAttr(label = "vidSportText", defaultText = "Вид спорт текст")
	private String vidSportText;
	
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	@JoinColumn(name = "id_object", referencedColumnName = "id" , nullable = false)
	@Where (clause = "tip_object="+ DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORT_OBED)
	private List<MMSVidSportSO> vidSportList = new ArrayList<>();

	@Transient
	private String mailLice=null;
	@Transient
	private Integer nachinPoluch=null;
	@Transient
	private String nachinPoluchText=null;
	@Transient
	private List<MMSSportObedMf> mejdFedList=new ArrayList<MMSSportObedMf>();
	@Transient
	private List<String> parseMessages=new ArrayList<String>();
	
	
	/** */
	public MMSSportnoObedinenie() {
		super();
	}

	 

	/** */
	@Override
	public Integer getCodeMainObject() {
		return DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORT_OBED;
	}
 
	
	@Override
	public String getIdentInfo() throws DbErrorException {
		if (regNomer!=null) {
			return regNomer.toString();
		}else {
			return "";	
		}
		 
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



	public Integer getTypeSport() {
		return typeSport;
	}



	public void setTypeSport(Integer typeSport) {
		this.typeSport = typeSport;
	}
	
	public boolean getTypeSportBool() {
		if (typeSport!=null && typeSport==DocuConstants.CODE_ZNACHENIE_DA) {
			return true;
		}else {
			return false;
		}
	}
	
	public void setTypeSportBool(boolean typeSport) {
		if (typeSport) {
			this.typeSport=DocuConstants.CODE_ZNACHENIE_DA;
		}else {
			this.typeSport=DocuConstants.CODE_ZNACHENIE_NE;
		}
	}
	


	public String getRegNomer() {
		return regNomer;
	}



	public void setRegNomer(String regNomer) {
		this.regNomer = regNomer;
	}



	public Integer getVid() {
		return vid;
	}



	public void setVid(Integer vid) {
		this.vid = vid;
	}



	public String getPredstavitelstvo() {
		return predstavitelstvo;
	}



	public void setPredstavitelstvo(String predstavitelstvo) {
		this.predstavitelstvo = predstavitelstvo;
	}



	public String getPredsedatel() {
		return predsedatel;
	}



	public void setPredsedatel(String predsedatel) {
		this.predsedatel = predsedatel;
	}



	public String getGenSekDirektor() {
		return genSekDirektor;
	}



	public void setGenSekDirektor(String genSekDirektor) {
		this.genSekDirektor = genSekDirektor;
	}



	public Integer getBrChlenove() {
		return brChlenove;
	}



	public void setBrChlenove(Integer brChlenove) {
		this.brChlenove = brChlenove;
	}



	public Integer getStatus() {
		return status;
	}



	public void setStatus(Integer status) {
		this.status = status;
	}



	public Integer getIdObject() {
		return idObject;
	}



	public void setIdObject(Integer idObject) {
		this.idObject = idObject;
	}


 



	public List<MMSVidSportSO> getVidSportList() {
		return vidSportList;
	}



	public void setVidSportList(List<MMSVidSportSO> vidSportList) {
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



	public Integer getVoenenSport() {
		return voenenSport;
	}



	public void setVoenenSport(Integer voenenSport) {
		this.voenenSport = voenenSport;
	}
	
	public boolean getVoenenSportBool() {
		if (voenenSport!=null && voenenSport==DocuConstants.CODE_ZNACHENIE_DA) {
			return true;
		}else {
			return false;
		}
	}
	
	public void setVoenenSportBool(boolean voenenSport) {
		if (voenenSport) {
			this.voenenSport=DocuConstants.CODE_ZNACHENIE_DA;
		}else {
			this.voenenSport=DocuConstants.CODE_ZNACHENIE_NE;
		}
	}



	public String getVidSportText() {
		return vidSportText;
	}



	public void setVidSportText(String vidSportText) {
		this.vidSportText = vidSportText;
	}



	public List<MMSSportObedMf> getMejdFedList() {
		return mejdFedList;
	}



	public void setMejdFedList(List<MMSSportObedMf> mejdFedList) {
		this.mejdFedList = mejdFedList;
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

	
}