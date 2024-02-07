package com.ib.docu.db.dto;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Where;

import com.ib.docu.system.DocuConstants;

@Entity
@Table(name="mms_vid_sport")
@DiscriminatorValue("" + DocuConstants.CODE_ZNACHENIE_JOURNAL_COACHES)
public class MMSVidSportSC extends MMSVidSport{
	

}
