package com.ib.docu.db.dto;

import java.io.Serializable;
import javax.persistence.*;

import com.ib.docu.system.DocuConstants;
import com.ib.system.db.AuditExt;
import com.ib.system.db.JournalAttr;
import com.ib.system.db.TrackableEntity;
import com.ib.system.db.dto.SystemJournal;
import com.ib.system.exceptions.DbErrorException;

/**
 * Entity implementation class for Entity: MMSVidSport
 *
 */
@MappedSuperclass
@DiscriminatorColumn(name = "tip_object", discriminatorType = DiscriminatorType.INTEGER)
public class MMSVidSport extends TrackableEntity implements AuditExt, Serializable {

	
	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator( name = "MMSVidSport", sequenceName = "seq_mms_vid_sport", allocationSize = 1)
	@GeneratedValue( strategy = GenerationType.SEQUENCE, generator = "MMSVidSport")
	@Column(name = "ID")
	private Integer id;
	
	@Column(name = "tip_object")
	@JournalAttr(label = "tipОbject", defaultText = "Код за вид на обект, скойто има връзка")
	private Integer tipОbject;

	@Column(name = "id_object", insertable = false, updatable = false)
	@JournalAttr(label = "idObject", defaultText = "От спортни обединения, формирования и т.н.")
	private Integer idObject;
	
	@Column(name = "vid_sport")
	@JournalAttr(label = "vidSport", defaultText = "Вид спорт", classifID = ""+ DocuConstants.CODE_CLASSIF_VIDOVE_SPORT)
	private Integer vidSport;
	

	
	public MMSVidSport() {
		super();
	}

	@Override
	public Integer getCodeMainObject() {
		return DocuConstants.CODE_ZNACHENIE_JOURNAL_VID_SPORTS;
	}
	@Override
	public SystemJournal toSystemJournal() throws DbErrorException {
		SystemJournal dj = new SystemJournal();
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
	

	public Integer getTipОbject() {
		return tipОbject;
	}

	public void setTipОbject(Integer tipОbject) {
		this.tipОbject = tipОbject;
	}

	public Integer getIdObject() {
		return idObject;
	}

	public void setIdObject(Integer idObject) {
		this.idObject = idObject;
	}

	public Integer getVidSport() {
		return vidSport;
	}

	public void setVidSport(Integer vidSport) {
		this.vidSport = vidSport;
	}
	
   
}
