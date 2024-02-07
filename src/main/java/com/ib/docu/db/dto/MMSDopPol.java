package com.ib.docu.db.dto;

import static javax.persistence.GenerationType.SEQUENCE;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.ib.system.db.TrackableEntity;

/**
 * Допълнителни полета в обектите
 *
 */
@Entity
@Table(name = "mms_dop_pol")
public class MMSDopPol extends TrackableEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2057096495483875936L;

	@SequenceGenerator(name = "MMSDopPol", sequenceName = "SEQ_MMS_DOP_POL", allocationSize = 1)
	@Id
	@GeneratedValue(strategy = SEQUENCE, generator = "MMSDopPol")
	@Column(name = "ID", unique = true, nullable = false)
	private Integer id;

	@Column(name = "CODE_OBJECT", nullable = false)
	private Integer codeObject;
	
	@Column(name = "id_obekt", nullable = false)
	private Integer idObekt;
	
	@Column(name = "ID_POLE", nullable = false)
	private Integer idPole;

	@Column(name = "zn_kod")
	private Integer znKod;
	
	@Column(name = "zn_date")
	private Date znDate;
	
	@Column(name = "zn_str")
	private String znStr;
	

	/** */
	public MMSDopPol() {
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
	 * @return the idObekt
	 */
	public Integer getIdObekt() {
		return idObekt;
	}

	/**
	 * @param idObekt the idObekt to set
	 */
	public void setIdObekt(Integer idObekt) {
		this.idObekt = idObekt;
	}
		
	/**
	 * @return the idPole
	 */
	public Integer getIdPole() {
		return idPole;
	}

	/**
	 * @param idPole the idPole to set
	 */
	public void setIdPole(Integer idPole) {
		this.idPole = idPole;
	}
		
	/**
	 * @return the znKod
	 */
	public Integer getZnKod() {
		return znKod;
	}

	/**
	 * @param znKod the znKod to set
	 */
	public void setZnKod(Integer znKod) {
		this.znKod = znKod;
	}
	
	/**
	 * @return the znDate
	 */
	public Date getZnDate() {
		return znDate;
	}

	/**
	 * @param znDate the znDate to set
	 */
	public void setZnDate(Date znDate) {
		this.znDate = znDate;
	}

	/**
	 * @return the znStr
	 */
	public String getZnStr() {
		return znStr;
	}

	/**
	 * @param znStr the znStr to set
	 */
	public void setZnStr(String znStr) {
		this.znStr = znStr;
	}

	@Override
	public Integer getCodeMainObject() {
		return null;
	}	
	
}