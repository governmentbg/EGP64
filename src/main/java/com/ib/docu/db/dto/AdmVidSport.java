package com.ib.docu.db.dto;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "adm_vid_sport")
public class AdmVidSport implements Serializable {

	private static final long serialVersionUID = -3466321045764045778L;

	@Id
	@Column(name = "vid_sport", unique = true, nullable = false)
	private Integer vidSport;
	
	@Column(name = "olimp", nullable = false)
	private Integer olimp;
	
	@Column(name = "voenen", nullable = false)
	private Integer voenen;
	
	public AdmVidSport() {
		
	}

	public Integer getVidSport() {
		return vidSport;
	}

	public void setVidSport(Integer vidSport) {
		this.vidSport = vidSport;
	}

	public Integer getOlimp() {
		return olimp;
	}

	public void setOlimp(Integer olimp) {
		this.olimp = olimp;
	}

	public Integer getVoenen() {
		return voenen;
	}

	public void setVoenen(Integer voenen) {
		this.voenen = voenen;
	}
	
}
