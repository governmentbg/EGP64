package com.ib.docu.db.dto;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.ib.docu.system.DocuConstants;

/**
 * Entity implementation class for class: MMSVidSport
 *
 */
@Entity
@Table(name="mms_vid_sport")
@DiscriminatorValue("" + DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORT_OBED)
public class MMSVidSportSO extends MMSVidSport  {

	
   
}
