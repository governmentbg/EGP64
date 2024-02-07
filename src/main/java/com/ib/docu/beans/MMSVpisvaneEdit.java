package com.ib.docu.beans;

import java.io.Serializable;
import java.util.Date;

import javax.annotation.PostConstruct;

import javax.inject.Named;

import org.omnifaces.cdi.ViewScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.docu.system.DocuConstants;
import com.ib.indexui.system.IndexUIbean;
import com.ib.indexui.utils.JSFUtils;

@Named(value = "mmsVpisvaneEdit")
@ViewScoped
public class MMSVpisvaneEdit extends IndexUIbean  implements Serializable {	
	
	/**
	 * Актуализация на вписване
	 * 
	 */
	private static final long serialVersionUID = -6555886524967146446L;
	private static final Logger LOGGER = LoggerFactory.getLogger(MMSVpisvaneEdit.class);
	
	private static final String ID_REG = "idReg";
	private static final String ID_OBJ = "idObject";
	private static final String CODE_OBJ = "codeObject";
	private static final String NAME_OBJ = "nameObject";
	private static final String VID_DOC = "vidDoc";
	private static final String REG_NOM = "regNomer";
	private static final String REG_NOM_OBED = "regNomerObed";
	private static final String EKATTE = "ekatte";
	private static final String VID_OBJ = "vidObject";
	private static final String READONLY = "readonly";
	private static final String NACHIN_POLUCH = "nachinPoluch";
	private static final String DOP_INFO_NACHIN_POLUCH = "dopInfoNachinPoluch";
	
	private Integer idReg;	
	private Integer idObject; 
	private Integer codeObject; 		
	private String nameObject;
	private Integer vidDoc;
	private String regNomer;
	private String regNomerObed;
	private Integer ekatte;
	private Integer vidObject;
	private boolean readonly;
	private String view;
	private Integer nachinPoluch;	
	private String dopInfoNachinPoluch;
	
	private Date decodeDate = new Date();	
	
	/** 
	 * 
	 * 
	 **/
	@PostConstruct
	public void initData() {
		
		LOGGER.debug("PostConstruct!!!");	
		
		if (JSFUtils.getRequestParameter(ID_REG) != null && !JSFUtils.getRequestParameter(ID_REG).isEmpty()) {
			this.idReg = Integer.valueOf(JSFUtils.getRequestParameter(ID_REG));
		}	
		
		if (JSFUtils.getRequestParameter(ID_OBJ) != null && !JSFUtils.getRequestParameter(ID_OBJ).isEmpty()) {
			this.idObject = Integer.valueOf(JSFUtils.getRequestParameter(ID_OBJ));
		}	
		
		if (JSFUtils.getRequestParameter(CODE_OBJ) != null && !JSFUtils.getRequestParameter(CODE_OBJ).isEmpty()) {
			this.codeObject = Integer.valueOf(JSFUtils.getRequestParameter(CODE_OBJ));
		}	
		
		if (JSFUtils.getRequestParameter(NAME_OBJ) != null && !JSFUtils.getRequestParameter(NAME_OBJ).isEmpty()) {
			this.nameObject = JSFUtils.getRequestParameter(NAME_OBJ);
		}	
		
		if (JSFUtils.getRequestParameter(VID_DOC) != null && !JSFUtils.getRequestParameter(VID_DOC).isEmpty()) {
			this.vidDoc = Integer.valueOf(JSFUtils.getRequestParameter(VID_DOC));
		}	
		
		if (JSFUtils.getRequestParameter(REG_NOM) != null && !JSFUtils.getRequestParameter(REG_NOM).isEmpty()) {
			this.regNomer = JSFUtils.getRequestParameter(REG_NOM);
		}
		
		if (JSFUtils.getRequestParameter(REG_NOM_OBED) != null && !JSFUtils.getRequestParameter(REG_NOM_OBED).isEmpty()) {
			this.regNomerObed = JSFUtils.getRequestParameter(REG_NOM_OBED);
		}
		
		if (JSFUtils.getRequestParameter(EKATTE) != null && !JSFUtils.getRequestParameter(EKATTE).isEmpty()) {
			this.ekatte = Integer.valueOf(JSFUtils.getRequestParameter(EKATTE));
		}
		
		if (JSFUtils.getRequestParameter(VID_OBJ) != null && !JSFUtils.getRequestParameter(VID_OBJ).isEmpty()) {
			this.vidObject = Integer.valueOf(JSFUtils.getRequestParameter(VID_OBJ));
		}
		
		if (JSFUtils.getRequestParameter(READONLY) != null && !JSFUtils.getRequestParameter(READONLY).isEmpty()) {
			this.view = JSFUtils.getRequestParameter(READONLY);
			if (this.view.trim().equals("true")) {
				this.readonly = true;
			} else {
				this.readonly = false;
			}
		}
		
		if (JSFUtils.getRequestParameter(NACHIN_POLUCH) != null && !JSFUtils.getRequestParameter(NACHIN_POLUCH).isEmpty()) {
			this.nachinPoluch = Integer.valueOf(JSFUtils.getRequestParameter(NACHIN_POLUCH));
		}	
		
		if (JSFUtils.getRequestParameter(DOP_INFO_NACHIN_POLUCH) != null && !JSFUtils.getRequestParameter(DOP_INFO_NACHIN_POLUCH).isEmpty()) {
			this.dopInfoNachinPoluch = JSFUtils.getRequestParameter(DOP_INFO_NACHIN_POLUCH);
		}	
	
	}
	
	public String actionGoBack() {
		
		if (this.codeObject != null && this.codeObject.equals(DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORT_OBED)) {
			return "sportObedEdit.xhtml?faces-redirect=true&idObj=" + idObject;		
		}
		
		if (this.codeObject != null && this.codeObject.equals(DocuConstants.CODE_ZNACHENIE_JOURNAL_COACHES)) {
			return "mmsCoachEdit.xhtml?faces-redirect=true&idObj=" + idObject;			
		}
		
		if (this.codeObject != null && this.codeObject.equals(DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_FORMATIONS)) {
			return "mmsSportFormirovanieEdit.xhtml?faces-redirect=true&idObj=" + idObject;				
		}
		
		if (this.codeObject != null && this.codeObject.equals(DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_OBJECTS)) {
			return "mmsSportObektEdit.xhtml?faces-redirect=true&idObj=" + idObject;					
		}
		
		return "";
	}
	

	public Integer getIdReg() {
		return idReg;
	}

	public void setIdReg(Integer idReg) {
		this.idReg = idReg;
	}
	
	public Integer getIdObject() {
		return idObject;
	}

	public void setIdObject(Integer idObject) {
		this.idObject = idObject;
	}

	public Integer getCodeObject() {
		return codeObject;
	}

	public void setCodeObject(Integer codeObject) {
		this.codeObject = codeObject;
	}

	public String getNameObject() {
		return nameObject;
	}

	public void setNameObject(String nameObject) {
		this.nameObject = nameObject;
	}

	public Integer getVidDoc() {
		return vidDoc;
	}

	public void setVidDoc(Integer vidDoc) {
		this.vidDoc = vidDoc;
	}

	public String getRegNomer() {
		return regNomer;
	}

	public void setRegNomer(String regNomer) {
		this.regNomer = regNomer;
	}

	public String getRegNomerObed() {
		return regNomerObed;
	}

	public void setRegNomerObed(String regNomerObed) {
		this.regNomerObed = regNomerObed;
	}
	
	public Integer getEkatte() {
		return ekatte;
	}

	public void setEkatte(Integer ekatte) {
		this.ekatte = ekatte;
	}

	/**
	 * @return the vidObject
	 */
	public Integer getVidObject() {
		return vidObject;
	}

	/**
	 * @param vidObject the vidObject to set
	 */
	public void setVidObject(Integer vidObject) {
		this.vidObject = vidObject;
	}

	public boolean isReadonly() {
		return readonly;
	}

	public void setReadonly(boolean readonly) {
		this.readonly = readonly;
	}

	public String getView() {
		return view;
	}

	public void setView(String view) {
		this.view = view;
	}

	public Integer getNachinPoluch() {
		return nachinPoluch;
	}

	public void setNachinPoluch(Integer nachinPoluch) {
		this.nachinPoluch = nachinPoluch;
	}

	public String getDopInfoNachinPoluch() {
		return dopInfoNachinPoluch;
	}

	public void setDopInfoNachinPoluch(String dopInfoNachinPoluch) {
		this.dopInfoNachinPoluch = dopInfoNachinPoluch;
	}

	public Date getDecodeDate() {
		return new Date(decodeDate.getTime()) ;
	}

	public void setDecodeDate(Date decodeDate) {
		this.decodeDate = decodeDate != null ? new Date(decodeDate.getTime()) : null;
	}
	
}