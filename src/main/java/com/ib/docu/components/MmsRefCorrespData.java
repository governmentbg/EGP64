package com.ib.docu.components;

import static com.ib.system.utils.SearchUtils.isEmpty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

import javax.el.ELContext;
import javax.el.ValueExpression;
import javax.faces.application.FacesMessage;
import javax.faces.component.FacesComponent;
import javax.faces.component.UINamingContainer;
import javax.faces.context.FacesContext;
import javax.xml.datatype.DatatypeConfigurationException;

import org.primefaces.PrimeFaces;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.ToggleSelectEvent;
import org.primefaces.event.UnselectEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.docu.db.dao.DocDAO;
import com.ib.docu.db.dao.ReferentDAO;
import com.ib.docu.db.dto.Doc;
import com.ib.docu.db.dto.Referent;
import com.ib.docu.db.dto.ReferentAddress;
import com.ib.docu.search.DocSearch;
import com.ib.docu.system.DocuConstants;
import com.ib.docu.system.SystemData;
import com.ib.docu.system.UserData;
import com.ib.docu.utils.RegixUtils;
import com.ib.indexui.pagination.LazyDataModelSQL2Array;
import com.ib.indexui.system.IndexUIbean;
import com.ib.indexui.utils.JSFUtils;
import com.ib.system.SysClassifAdapter;
import com.ib.system.SysConstants;
import com.ib.system.db.JPA;
import com.ib.system.db.SelectMetadata;
import com.ib.system.db.dao.FilesDAO;
import com.ib.system.db.dto.Files;
import com.ib.system.exceptions.BaseException;
import com.ib.system.exceptions.DbErrorException;
import com.ib.system.exceptions.InvalidParameterException;
import com.ib.system.exceptions.ObjectInUseException;
import com.ib.system.utils.SearchUtils;
import com.ib.system.utils.StringUtils;
import com.ib.system.utils.ValidationUtils;

import bg.government.regixclient.RegixClientException;

/** */
@FacesComponent(value = "mmsRefCorrespData", createTag = true)
public class MmsRefCorrespData extends UINamingContainer {
	
	private enum PropertyKeys {
		  REF, SHOWME, EKATTE, EKATTESPEC, DOCSLIST, DOCSEARCH, DOCSELTMP, DOCSEL, SEEPERSONALDATA, REFKORESP, SD, CLEARREGIXDIFF
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(MmsRefCorrespData.class);
	public static final String	UIBEANMESSAGES = "ui_beanMessages";
	public static final String	BEANMESSAGES = "beanMessages";
	public static final String  MSGPLSINS = "general.pleaseInsert";
	public static final String  ERRDATABASEMSG = "general.errDataBaseMsg";
	public static final String  SUCCESSAVEMSG = "general.succesSaveMsg";
	public static final String	OBJECTINUSE		 = "general.objectInUse";
	public static final String	LABELS = "labels";
	private static final String CODEREF = "codeRef";
	private static final String MSGVALIDEIK = "refCorr.msgValidEik";
	private static final String MSGVALIDEGN = "refCorr.msgValidEgn"; 
	private static final String MSGVALIDLNCH = "refCorr.msgValidLnch"; 
	private static final String REFCORRESPMSG1 = "docu.refCorrespMsg1";
	private static final String SUCCESSDELETEMSG = "general.successDeleteMsg";

	private String errMsg = null;
	private SystemData	systemData	= null;
	private UserData	userData	= null;
	private Date			dateClassif	= null;
	private boolean loadedFromRegix=false;
	private TimeZone timeZone = TimeZone.getDefault();
//	private IndexUIbean 	indexUIbean = null;
//	private String 			modalMsg = "";
	
	
	
	private Referent tmpRef;

	private int countryBG; // ще се инициализира в getter-а през системна настройка: delo.countryBG
	
	/// За сега не са включени: 
	//  1. Районите в адреса
	//  2. повече от един адрес
	//  3. имената на латиница
	//  4. гражданството
	//  5. личен номер в ЕС - за физ. лица
	//  6. данъчен номер - за юрид. лица
	
	//  7. Специфичните настройки - сеос,guid и т.н. !!!
	//  8. списък с документи
	 
	
	/**
	 * Данни на лице - актуализация и разглеждане
	 * @return
	 * @throws DbErrorException
	 */
	public void initRefCorresp() {		
		
		//boolean modal = (Boolean) getAttributes().get("modal"); // обработката е в модален диалог (true) или не (false)
		
		setSd(getSystemData());
		
		Integer idR = (Integer) getAttributes().get(CODEREF); 
		if(idR != null) {
			loadRefCorr(idR, null, null, null);
		} else { // нов
			String srchTxt = (String) getAttributes().get("searchTxt"); 
			clearRefCorresp(srchTxt);
			Integer refType = (Integer) getAttributes().get("refType");
			if (refType!=null) {
				tmpRef.setRefType(refType);
			}else {
				tmpRef.setRefType(DocuConstants.CODE_ZNACHENIE_REF_TYPE_NFL);
			}
			
			ValueExpression expr2 = getValueExpression("searchTxt"); //зачиствам текста - искам да се изпозлва само при първото отваряне
			ELContext ctx2 = getFacesContext().getELContext();
			if (expr2 != null) {
				expr2.setValue(ctx2, null);
			}	
			if (srchTxt!=null && !srchTxt.isEmpty()) {
				try {
					
							
								
						
						//REGIX опит за зареждане на данни.
						
						if (ValidationUtils.invokeValidation(getSystemData().getModel().getAttrSpec("fzl_egn", "person", getLang(), null).getValidMethod(), srchTxt)
								|| ValidationUtils.invokeValidation(getSystemData().getModel().getAttrSpec("fzl_lnc", "person", getLang(), null).getValidMethod(), srchTxt)) {
							if (getSystemData().getSettingsValue("REGIX_ESGRAON_ACTIVE")!=null && getSystemData().getSettingsValue("REGIX_ESGRAON_ACTIVE").equals("true")) {
								tmpRef.setRefType(DocuConstants.CODE_ZNACHENIE_REF_TYPE_FZL);
								RegixUtils.loadFizLiceByEgn(tmpRef, srchTxt, true, true, systemData);	
								if (ValidationUtils.isValidLNCH(srchTxt)){
									//default e EGN za tova go smenqm tuk poneje i metoda w regix za sega e edin i sasht.
									tmpRef.setFzlEgn(null);
									tmpRef.setFzlLnc(srchTxt);
									
								}
								loadedFromRegix=true;
							}		
						}else {
							if (ValidationUtils.invokeValidation(getSystemData().getModel().getAttrSpec("nfl_eik", "person", getLang(), null).getValidMethod(), srchTxt)) {
								RegixUtils.loadUridLiceByEik(tmpRef, srchTxt, systemData);
							}
							loadedFromRegix=true;
						}
						
				} catch (DbErrorException e) {
					LOGGER.error("Грешка при работа с базата данни! ", e);
					JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,"Грешка при работа с базата данни! ", e.getMessage());
				} catch (RegixClientException e) {
					LOGGER.error("Грешка при извличане на данни от Regix! ", e);
					JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,"Грешка при извличане на данни от Regix!", e.getMessage());
				} catch (DatatypeConfigurationException e) {
					LOGGER.error("Грешка при извличане на данни от Regix! ", e);
					JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,"Грешка при извличане на данни от Regix!", e.getMessage());
				} catch (InvalidParameterException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}
		
		setSeePersonalData(getUserData().hasAccess(DocuConstants.CODE_CLASSIF_DEF_PRAVA, DocuConstants.CODE_ZNACHENIE_DEF_PRAVA_SEE_PERSONAL_DATA));
		if (getRef()!=null) {
			if (!isSeePersonalData() && getRef().getRefType().equals(DocuConstants.CODE_ZNACHENIE_REF_TYPE_FZL)) { 
				getAttributes().put("readonly", true);
			}	
			setShowMe(true);
			setErrMsg(null);
		}else {
			setShowMe(false);
		}
		
		
		
		LOGGER.debug("initRefCorresp");
	}

	/**
	 *  зарежда данни за корепондент по зададени критерии
	 */
	private boolean loadRefCorr(Integer idR, String eik, String egn, String lnc) {
		boolean bb = true;		
		try {
			setErrMsg(null);
			if(idR != null) {
				JPA.getUtil().runWithClose(() -> tmpRef = new ReferentDAO(getUserData()).findByCode(idR, getDateClassif(), true));
				if (tmpRef==null) {
					tmpRef=new Referent();
					setErrMsg("Не е намерено лице!");
					bb = false;
				}
			} else {	
				JPA.getUtil().runWithClose(() -> tmpRef = new ReferentDAO(getUserData()).findByIdent(eik, egn, lnc, getRef().getRefType())); 
				if(tmpRef != null && !tmpRef.getId().equals(getRef().getId())) {
				    String str1 = (isEmpty(eik) ? " ЕГН" : " ЕИК" );
					setErrMsg(IndexUIbean.getMessageResourceString(BEANMESSAGES, "refCorr.loadByEikEgn", str1)); 
				}else {
					bb = false;
				}
			}			
			if(bb) {
				if(tmpRef != null && tmpRef.getAddress() == null) {
					tmpRef.setAddress(new ReferentAddress());
					tmpRef.getAddress().setAddrType(DocuConstants.CODE_ZNACHENIE_ADDR_TYPE_POSTOQNEN);
				}
				if(tmpRef != null && tmpRef.getAddressKoresp() == null) {
					tmpRef.setAddressKoresp(new ReferentAddress());
					tmpRef.getAddress().setAddrType(DocuConstants.CODE_ZNACHENIE_ADDR_TYPE_CORRESP);
				}
				if (!SearchUtils.isEmpty(tmpRef.getFzlEgn())) {
					if (tmpRef.getFzlBirthDate() == null) { // има егн, но няма дата на раждане
						try { // и се опитваме да изчислим
							tmpRef.setFzlBirthDate(StringUtils.birthdayFromEGN(tmpRef.getFzlEgn()));
						} catch (Exception e) { // няма много какво да направим
							LOGGER.error("Грешка при определяне на дата на раждане по ЕГН", e);
						}
					}
					if (tmpRef.getPol() == null) { // има егн, но няма пол
						try { // и се опитваме да изчислим
							int i = Integer.parseInt(String.valueOf(tmpRef.getFzlEgn().charAt(8)));
							tmpRef.setPol(i % 2 == 0 ? 1 : 2);
							
						} catch (Exception e) { // няма много какво да направим
							LOGGER.error("Грешка при определяне на пол по ЕГН", e);
						}
					}
				}
				setRef(tmpRef);
			}
			
			tmpRef = null;
			
			LOGGER.debug("load initRefCorresp");
		} catch (BaseException e) {
			LOGGER.error("Грешка при зареждане на данни за лице! ", e);
		}
		return bb;
	}
   
   /**
    * Зачиства данните на лице - бутон "нов"
    * 
    */
   public void clearRefCorresp(String srchTxt) {
	    tmpRef = new Referent();
//	    tmpRef.setRefType(DocuConstants.CODE_ZNACHENIE_REF_TYPE_NFL); // юридическо лице
		tmpRef.setDateOt(getDateClassif());
		tmpRef.setAddress(new ReferentAddress());
		tmpRef.getAddress().setAddrType(DocuConstants.CODE_ZNACHENIE_ADDR_TYPE_POSTOQNEN);
		tmpRef.setAddressKoresp(new ReferentAddress());
		tmpRef.setRefName(srchTxt);
		setRef(tmpRef);
		tmpRef.getAddress().setAddrCountry(getCountryBG());
		tmpRef.getAddressKoresp().setAddrCountry(getCountryBG());
		tmpRef.setRefGrj(getCountryBG());
		setDocSelectedAllM(null);
		setDocSelectedTmp(null);
   }
		
	/**
    * смяна на лице - физическо/юридическо  
    */
   public void actionChTypRef() { 
	   getRef().setNflEik(null);	   
	   getRef().setFzlEgn(null);
	   getRef().setFzlLnc(null);
	   
	   getRef().setTaxOfficeNo(null);
	   getRef().setFzlLnEs(null); 
	   LOGGER.debug("actionChTypRef"); 
	   setDocSelectedAllM(null);
	   setDocSelectedTmp(null);
   }
  
   /**
    * При смяна на държава - да се нулира полето за ЕКАТЕ
    */
   public void  actionChangeCountry() {
	   getRef().getAddress().setPostBox(null);
	   getRef().getAddress().setPostCode(null);
	   getRef().getAddress().setEkatte(null);
   }
   
   /**
    * При смяна на държава - да се нулира полето за ЕКАТЕ
    */
   public void  actionChangeCountryKoresp() {
	   getRef().getAddressKoresp().setPostBox(null);
	   getRef().getAddressKoresp().setPostCode(null);
	   getRef().getAddressKoresp().setEkatte(null);
   }
   
   
   /**
    * зарежда лице по зададен еик
    */
	 public void actionLoadByEIK() { 
		//системна настройка - Допуска се дублиране на ЕИК при въвеждане на нефизическо лице  (1- да / 0 - не); по подразбиране - не
		try {
			String	setting = getSystemData().getSettingsValue("delo.allowEikDuplicate"); 	
			if (setting == null || !Objects.equals(Integer.valueOf(setting), SysConstants.CODE_ZNACHENIE_DA)) {
				if (!isEmpty(getRef().getNflEik()) && !ValidationUtils.invokeValidation(getSystemData().getModel().getAttrSpec("nfl_eik", "sport_obedinenie", getLang(), null).getValidMethod(), getRef().getNflEik())) {

					JSFUtils.addMessage(this.getClientId(FacesContext.getCurrentInstance()) + ":eik",
							FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(BEANMESSAGES, MSGVALIDEIK));
					
					setErrMsg(IndexUIbean.getMessageResourceString(BEANMESSAGES, MSGVALIDEIK)); 
				} else {

					loadRefCorr(null, getRef().getNflEik(), null, null);
				}
			}
		
		} catch (DbErrorException e) {
			LOGGER.error(e.getMessage(), e);
			JSFUtils.addErrorMessage(e.getMessage(), e);
		} catch (InvalidParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		LOGGER.debug("actionLoadByEIK");
	 }
	 
	 /**
    * зарежда лице по зададен егн
	 * @throws DbErrorException 
	 * @throws InvalidParameterException 
    */
	 public void actionLoadByEGN() throws InvalidParameterException, DbErrorException {
		// Винаги да се прави проверка за дублирано ЕГН, ако се въвежда физическо лице
		if (!isEmpty(getRef().getFzlEgn()) && !ValidationUtils.invokeValidation(getSystemData().getModel().getAttrSpec("fzl_egn", "person", getLang(), null).getValidMethod(), getRef().getFzlEgn())) {
			
			JSFUtils.addMessage(this.getClientId(FacesContext.getCurrentInstance()) + ":egn",
					FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(BEANMESSAGES, MSGVALIDEGN));
			
			setErrMsg(IndexUIbean.getMessageResourceString(BEANMESSAGES, MSGVALIDEGN)); 
		} else {
			
			loadRefCorr(null, null, getRef().getFzlEgn(), null);
		}
		
		LOGGER.debug("actionLoadByEGN");
	 }
   
	/**
	 * зарежда лице по зададен ЛНЧ
	 * @throws DbErrorException 
	 * @throws InvalidParameterException 
	 */
	public void actionLoadByLNCH() throws InvalidParameterException, DbErrorException {
		// Винаги да се прави проверка за дублирано ЕГН, ако се въвежда физическо лице
		if (!isEmpty(getRef().getFzlLnc()) && !ValidationUtils.invokeValidation(getSystemData().getModel().getAttrSpec("fzl_lnc", "person", getLang(), null).getValidMethod(), getRef().getFzlLnc())) {

			JSFUtils.addMessage(this.getClientId(FacesContext.getCurrentInstance()) + ":lnch",
					FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(BEANMESSAGES, MSGVALIDLNCH));

			setErrMsg(IndexUIbean.getMessageResourceString(BEANMESSAGES, MSGVALIDLNCH));
		} else {

			loadRefCorr(null, null, null, getRef().getFzlLnc());
		}

		LOGGER.debug("actionLoadByLNCH");
	}

   /** 
    * Запис на лице 
    */
   public void actionSave(){ 
	    errMsg = " Моля, въведете задължителната информация!";
	    try { 
			if(checkData()) {
				errMsg = null;
				
					LOGGER.debug("actionSave>>>> ");
					getRef().setLevelNumber(null); // за да може процеса на регикс после да мине през този
					if (getClearRegixDiff()!=null && getClearRegixDiff()) {
						getRef().setRegixDiff(null);
					}
					JPA.getUtil().runInTransaction(() -> this.tmpRef = new ReferentDAO(getUserData()).save(getRef()));
				  
				   getSystemData().mergeReferentsClassif(tmpRef, false );	
									
				   if( tmpRef != null && tmpRef.getCode() != null) {
					   //връща id на избрания лице
					    ValueExpression expr2 = getValueExpression(CODEREF);
						ELContext ctx2 = getFacesContext().getELContext();
						if (expr2 != null) {
							expr2.setValue(ctx2, tmpRef.getCode());
						}	
				   }	
				   
				    // извиква remoteCommnad - ако има такава....
					String remoteCommnad = (String) getAttributes().get("onComplete");
					if (remoteCommnad != null && !"".equals(remoteCommnad)) {
						PrimeFaces.current().executeScript(remoteCommnad);
					}
					
	//				if(tmpRef != null && tmpRef.getAddress() == null) {
	//					tmpRef.setAddress(new ReferentAddress());
	//				}
	//				setRef(tmpRef); // излишно е, ако веднага при запис се затваря модалния, иначе не е....
					
					JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, IndexUIbean.getMessageResourceString(UIBEANMESSAGES, SUCCESSAVEMSG) );
				
			} 
	    } catch (BaseException e) {			
			LOGGER.error("Грешка при запис на лице ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,IndexUIbean.getMessageResourceString(UIBEANMESSAGES, ERRDATABASEMSG));
		}
   }
 

   /**
    * Проверка за валидни данни
    * @return 
 * @throws DbErrorException 
 * @throws InvalidParameterException 
    */
	public boolean checkData() throws InvalidParameterException, DbErrorException {
		boolean flagSave = true;	
		FacesContext context = FacesContext.getCurrentInstance();
	    String clientId = null;	  
	    tmpRef = getRef();
	    
	    
	    if (context != null && tmpRef != null ) {
		   clientId =  this.getClientId(context);		
		   
		   String tmp = tmpRef.getNflEik() == null ? null : tmpRef.getNflEik().trim();  
		   tmpRef.setNflEik(tmp);
		   tmp = tmpRef.getFzlEgn() == null ? null : tmpRef.getFzlEgn().trim();  
		   tmpRef.setFzlEgn(tmp);
		   
		  
		   if (tmpRef.getRefType()==DocuConstants.CODE_ZNACHENIE_REF_TYPE_FZL) {
		    	
			   
			   
			   if (tmpRef.getIme()!=null && !tmpRef.getIme().isEmpty()) {
				   if (ValidationUtils.invokeValidation(getSystemData().getModel().getAttrSpec("ime", "person", getLang(), null).getValidMethod(), tmpRef.getIme())) {
					   tmpRef.setRefName(tmpRef.getIme());
				   }else {
					   flagSave=false;
					   JSFUtils.addMessage(clientId+":imeCorr",FacesMessage.SEVERITY_ERROR, "Невалидно име!");
				   }
				}else {
					if (getSystemData().getModel().getAttrSpec("ime", "person", getLang(), null).isActive() && getSystemData().getModel().getAttrSpec("ime", "person", getLang(), null).isRequired()) {
						flagSave=false;
						JSFUtils.addMessage(clientId+":imeCorr",FacesMessage.SEVERITY_ERROR, "Моля, въведете име!");
					}
				}
				if (tmpRef.getPrezime()!=null && !tmpRef.getPrezime().isEmpty()) {
					if (ValidationUtils.invokeValidation(getSystemData().getModel().getAttrSpec("prezime", "person", getLang(), null).getValidMethod(), tmpRef.getIme())) {
							if (tmpRef.getRefName()!=null) {
								tmpRef.setRefName(tmpRef.getRefName()+" "+tmpRef.getPrezime());
							}else {
								tmpRef.setRefName(tmpRef.getPrezime());
							}
					   }else {
						   flagSave=false;
						   JSFUtils.addMessage(clientId+":prezimeCorr",FacesMessage.SEVERITY_ERROR, "Невалидно презиме!");
					   }
				}else {
					if (getSystemData().getModel().getAttrSpec("prezime", "person", getLang(), null).isActive() && getSystemData().getModel().getAttrSpec("prezime", "person", getLang(), null).isRequired()) {
						flagSave=false;
						JSFUtils.addMessage(clientId+":prezimeCorr",FacesMessage.SEVERITY_ERROR, "Моля, въведете презиме!");
					}
				}
				if (tmpRef.getFamilia()!=null && !tmpRef.getFamilia().isEmpty()) {
					if (ValidationUtils.invokeValidation(getSystemData().getModel().getAttrSpec("familia", "person", getLang(), null).getValidMethod(), tmpRef.getIme())) {
							if (tmpRef.getRefName()!=null) {
								tmpRef.setRefName(tmpRef.getRefName()+" "+tmpRef.getFamilia());
							}else {
								tmpRef.setRefName(tmpRef.getFamilia());
							}
					   }else {
						   flagSave=false;
						   JSFUtils.addMessage(clientId+":familiaCorr",FacesMessage.SEVERITY_ERROR, "Невалидна фамилия!");
					   }
				}else {
					if (getSystemData().getModel().getAttrSpec("familia", "person", getLang(), null).isActive() && getSystemData().getModel().getAttrSpec("familia", "person", getLang(), null).isRequired()) {
						flagSave=false;
						JSFUtils.addMessage(clientId+":familiaCorr",FacesMessage.SEVERITY_ERROR, "Моля, въведете фамилия!");
					}
				}
				
				if (!isEmpty(tmpRef.getFzlEgn())) {
					if (!ValidationUtils.invokeValidation(getSystemData().getModel().getAttrSpec("fzl_egn", "person", getLang(), null).getValidMethod(), tmpRef.getFzlEgn())) {			   
						   JSFUtils.addMessage(clientId+":egn",FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(BEANMESSAGES, MSGVALIDEGN));
						   flagSave = false;
					}
				}else {
					if (getSystemData().getModel().getAttrSpec("fzl_egn", "person", getLang(), null).isActive() && getSystemData().getModel().getAttrSpec("fzl_egn", "person", getLang(), null).isRequired()) {
					   flagSave = false;
					   JSFUtils.addMessage(clientId+":egn",FacesMessage.SEVERITY_ERROR, "Моля, въведете ЕГН!");
					}
				}
				if (!isEmpty(tmpRef.getFzlLnc())) {
					if (!ValidationUtils.invokeValidation(getSystemData().getModel().getAttrSpec("fzl_lnc", "person", getLang(), null).getValidMethod(), tmpRef.getFzlLnc())) {
					   
					   JSFUtils.addMessage(clientId+":lnch",FacesMessage.SEVERITY_ERROR,
								IndexUIbean.getMessageResourceString(BEANMESSAGES, MSGVALIDLNCH));
						flagSave = false;	
					}
				}else {
					if (getSystemData().getModel().getAttrSpec("fzl_lnc", "person", getLang(), null).isActive() && getSystemData().getModel().getAttrSpec("fzl_lnc", "person", getLang(), null).isRequired()) {
						   flagSave = false;
						   JSFUtils.addMessage(clientId+":lnch",FacesMessage.SEVERITY_ERROR, "Моля, въведете ЛНЧ!");
						}
				}
				if (!isEmpty(tmpRef.getNomDoc())) {
					if (!ValidationUtils.invokeValidation(getSystemData().getModel().getAttrSpec("nom_doc", "person", getLang(), null).getValidMethod(), tmpRef.getNomDoc())) {
						
						JSFUtils.addMessage(clientId+":nomDoc",FacesMessage.SEVERITY_ERROR, "Невалиден Номер на документ за самоличност!");
						flagSave = false;	
					}
				}else {
					if (getSystemData().getModel().getAttrSpec("nom_doc", "person", getLang(), null).isActive() && getSystemData().getModel().getAttrSpec("nom_doc", "person", getLang(), null).isRequired()) {
						flagSave = false;
						JSFUtils.addMessage(clientId+":nomDoc",FacesMessage.SEVERITY_ERROR, "Моля, въведете Номер на документ за самоличност!");
					}
				}
				if (getSystemData().getModel().getAttrSpec("ref_grj", "person", getLang(), null).isActive() && getSystemData().getModel().getAttrSpec("ref_grj", "person", getLang(), null).isRequired()) {
				   flagSave = false;
				   JSFUtils.addMessage(clientId+":refGrj",FacesMessage.SEVERITY_ERROR, "Моля, въведете Гражданство!");
				}
				 
				if (getSystemData().getModel().getAttrSpec("fzl_birth_date", "person", getLang(), null).isActive() && getSystemData().getModel().getAttrSpec("fzl_birth_date", "person", getLang(), null).isRequired()) {
				   flagSave = false;
				   JSFUtils.addMessage(clientId+":birthDay",FacesMessage.SEVERITY_ERROR, "Моля, въведете Дата на раждане!");
				}
				
				if (getSystemData().getModel().getAttrSpec("date_smart", "person", getLang(), null).isActive() && getSystemData().getModel().getAttrSpec("date_smart", "person", getLang(), null).isRequired()) {
					flagSave = false;
					JSFUtils.addMessage(clientId+":dateSmart",FacesMessage.SEVERITY_ERROR, "Моля, въведете Дата на смърт!");
				}
				
				
				if (!isEmpty(tmpRef.getRefLatin())) {
					if (!ValidationUtils.invokeValidation(getSystemData().getModel().getAttrSpec("ref_latin", "person", getLang(), null).getValidMethod(), tmpRef.getRefLatin())) {
						   JSFUtils.addMessage(clientId+":nameLatinCorr",FacesMessage.SEVERITY_ERROR, "Невалидни Имена на латиница!");
						   flagSave = false;
					}
				}else {
					if (getSystemData().getModel().getAttrSpec("ref_latin", "person", getLang(), null).isActive() && getSystemData().getModel().getAttrSpec("ref_latin", "person", getLang(), null).isRequired()) {
					   flagSave = false;
					   JSFUtils.addMessage(clientId+":nameLatinCorr",FacesMessage.SEVERITY_ERROR, "Моля, въведете Имена на латиница!");
					}
				}
			}else {
				
				
				//urid lice
				if (getSystemData().getModel().getAttrSpec("ref_name", "person", getLang(), null).isActive() && getSystemData().getModel().getAttrSpec("ref_name", "person", getLang(), null).isRequired()) {
					if(tmpRef.getRefName()==null || isEmpty(tmpRef.getRefName())) {
						JSFUtils.addMessage(clientId+":nameCorr",FacesMessage.SEVERITY_ERROR,
								IndexUIbean.getMessageResourceString(BEANMESSAGES,"general.msgRefCorrName"));
						flagSave = false;	
				   }	
				}else {
					if (ValidationUtils.invokeValidation(getSystemData().getModel().getAttrSpec("ref_name", "person", getLang(), null).getValidMethod(), tmpRef.getIme())) {
						flagSave=false;
						JSFUtils.addMessage(clientId+":nameCorr",FacesMessage.SEVERITY_ERROR, "Невалидно наименование!");
					}
				}
				if (!isEmpty(tmpRef.getNflEik())) {
					if (!ValidationUtils.invokeValidation(getSystemData().getModel().getAttrSpec("nfl_eik", "person", getLang(), null).getValidMethod(), tmpRef.getNflEik())) {
						   JSFUtils.addMessage(clientId+":eik",FacesMessage.SEVERITY_ERROR,IndexUIbean.getMessageResourceString(BEANMESSAGES, MSGVALIDEIK));
						   flagSave = false;
					}
				}else {
					if (getSystemData().getModel().getAttrSpec("nfl_eik", "person", getLang(), null).isActive() && getSystemData().getModel().getAttrSpec("nfl_eik", "person", getLang(), null).isRequired()) {
					   flagSave = false;
					   JSFUtils.addMessage(clientId+":eik",FacesMessage.SEVERITY_ERROR, "Моля, въведете ЕИК!");
					}
				}
				if (getSystemData().getModel().getAttrSpec("ref_grj", "person", getLang(), null).isActive() && getSystemData().getModel().getAttrSpec("ref_grj", "person", getLang(), null).isRequired()) {
				   flagSave = false;
				   JSFUtils.addMessage(clientId+":refGrj",FacesMessage.SEVERITY_ERROR, "Моля, въведете Държава на регистрация!");
				}
				if (getSystemData().getModel().getAttrSpec("polza", "person", getLang(), null).isActive() && getSystemData().getModel().getAttrSpec("polza", "person", getLang(), null).isRequired()) {
					flagSave = false;
					JSFUtils.addMessage(clientId+":polza",FacesMessage.SEVERITY_ERROR, "Моля, въведете Полза!");
				}
				if (getSystemData().getModel().getAttrSpec("predstavitelstvo", "person", getLang(), null).isActive() && getSystemData().getModel().getAttrSpec("predstavitelstvo", "person", getLang(), null).isRequired()) {
					flagSave = false;
					JSFUtils.addMessage(clientId+":predstavitelstvo",FacesMessage.SEVERITY_ERROR, "Моля, въведете Представителство!");
				}
				
				if (!isEmpty(tmpRef.getRefLatin())) {
					if (!ValidationUtils.invokeValidation(getSystemData().getModel().getAttrSpec("ref_latin", "person", getLang(), null).getValidMethod(), tmpRef.getRefLatin())) {
						   JSFUtils.addMessage(clientId+":nameLatinCorr",FacesMessage.SEVERITY_ERROR, "Невалидно Наименование на латиница!");
						   flagSave = false;
					}
				}else {
					if (getSystemData().getModel().getAttrSpec("ref_latin", "person", getLang(), null).isActive() && getSystemData().getModel().getAttrSpec("ref_latin", "person", getLang(), null).isRequired()) {
					   flagSave = false;
					   JSFUtils.addMessage(clientId+":nameLatinCorr",FacesMessage.SEVERITY_ERROR, "Моля, въведете Наименование на латиница!");
					}
				}
				if (!isEmpty(tmpRef.getTaxOfficeNo())) {
					if (!ValidationUtils.invokeValidation(getSystemData().getModel().getAttrSpec("tax_office_no", "person", getLang(), null).getValidMethod(), tmpRef.getTaxOfficeNo())) {
						JSFUtils.addMessage(clientId+":taxOffice",FacesMessage.SEVERITY_ERROR, "Невалиден Данъчен служебен номер!");
						flagSave = false;
					}
				}else {
					if (getSystemData().getModel().getAttrSpec("tax_office_no", "person", getLang(), null).isActive() && getSystemData().getModel().getAttrSpec("tax_office_no", "person", getLang(), null).isRequired()) {
						flagSave = false;
						JSFUtils.addMessage(clientId+":taxOffice",FacesMessage.SEVERITY_ERROR, "Моля, въведете Данъчен служебен номер!");
					}
				}
			}
		   
		   // obshti
		   if (!isEmpty(tmpRef.getContactEmail())) {
			   if (!ValidationUtils.invokeValidation(getSystemData().getModel().getAttrSpec("contact_email", "person", getLang(), null).getValidMethod(), tmpRef.getContactEmail())) {
				   JSFUtils.addMessage(clientId+":contactEmail",FacesMessage.SEVERITY_ERROR,
							IndexUIbean.getMessageResourceString(UIBEANMESSAGES, "general.validE-mail"));
					flagSave = false;	
			   }
		   }else {
			   if (getSystemData().getModel().getAttrSpec("contact_email", "person", getLang(), null).isActive() && getSystemData().getModel().getAttrSpec("contact_email", "person", getLang(), null).isRequired()) {
				   flagSave = false;
				   JSFUtils.addMessage(clientId+":contactEmail",FacesMessage.SEVERITY_ERROR, "Моля, въведете e-mail!");
				}
		   }
		   
		   if (!isEmpty(tmpRef.getContactPhone())) {
			   if (!ValidationUtils.invokeValidation(getSystemData().getModel().getAttrSpec("contact_phone", "person", getLang(), null).getValidMethod(), tmpRef.getContactPhone())) {
				   JSFUtils.addMessage(clientId+":contactPhone",FacesMessage.SEVERITY_ERROR, "Невалиден телефон!");
				   flagSave = false;	
			   }
		   }else {
			   if (getSystemData().getModel().getAttrSpec("contact_phone", "person", getLang(), null).isActive() && getSystemData().getModel().getAttrSpec("contact_phone", "person", getLang(), null).isRequired()) {
				   flagSave = false;
				   JSFUtils.addMessage(clientId+":contactPhone",FacesMessage.SEVERITY_ERROR, "Моля, въведете телефон!");
			   }
		   }
		   
		   if (!isEmpty(tmpRef.getWebPage())) {
			   if (!ValidationUtils.invokeValidation(getSystemData().getModel().getAttrSpec("web_page", "person", getLang(), null).getValidMethod(), tmpRef.getWebPage())) {
				   JSFUtils.addMessage(clientId+":contactWebPage",FacesMessage.SEVERITY_ERROR, "Невалиден Уеб сайт!");
				   flagSave = false;	
			   }
		   }else {
			   if (getSystemData().getModel().getAttrSpec("web_page", "person", getLang(), null).isActive() && getSystemData().getModel().getAttrSpec("web_page", "person", getLang(), null).isRequired()) {
				   flagSave = false;
				   JSFUtils.addMessage(clientId+":contactWebPage",FacesMessage.SEVERITY_ERROR, "Моля, въведете Уеб сайт!");
			   }
		   }
		   if (!isEmpty(tmpRef.getRefInfo())) {
			   if (!ValidationUtils.invokeValidation(getSystemData().getModel().getAttrSpec("ref_info", "person", getLang(), null).getValidMethod(), tmpRef.getRefInfo())) {
				   JSFUtils.addMessage(clientId+":refInfo",FacesMessage.SEVERITY_ERROR, "Невалидна Забележка!");
				   flagSave = false;	
			   }
		   }else {
			   if (getSystemData().getModel().getAttrSpec("ref_info", "person", getLang(), null).isActive() && getSystemData().getModel().getAttrSpec("ref_info", "person", getLang(), null).isRequired()) {
				   flagSave = false;
				   JSFUtils.addMessage(clientId+":refInfo",FacesMessage.SEVERITY_ERROR, "Моля, въведете Забележка!");
			   }
		   }
		   
		   
//		   if(getRef().getAddress() != null && getRef().getAddress().getAddrCountry() != null && getRef().getAddress().getEkatte() == null) {
//			   // за да се запише адреса се изисква да се въведе адрес и/или населено място!!!
//			  //String msgAdrtxt =  "general.msgRefCorrAdrTxt";
//			  if(getRef().getAddress().getAddrCountry().equals(getCountryBG())) {
//				  String msgAdrtxt =  "general.msgRefCorrAdr1";
//				  JSFUtils.addMessage(clientId+":mestoC:аutoCompl",FacesMessage.SEVERITY_ERROR,
//							IndexUIbean.getMessageResourceString(BEANMESSAGES,msgAdrtxt));
//			  }
////			  JSFUtils.addMessage(clientId+":adrTxt",FacesMessage.SEVERITY_ERROR,
////						IndexUIbean.getMessageResourceString(BEANMESSAGES,msgAdrtxt));
//			  
//			  flagSave = false;	
//		   }
		   
//		   //допустимо е- без държава и адрес или само чужда държава
//		   if( getRef().getAddress() != null &&
//				   Integer.valueOf(getCountryBG()).equals(getRef().getAddress().getAddrCountry()) &&
//				   getRef().getAddress().getEkatte() == null) {
//			   // ако e в  България - задължително да се въведе населено място		
//				  String msgAdrtxt =  "general.msgRefCorrAdr1";
//				  JSFUtils.addMessage(clientId+":mestoC:аutoCompl",FacesMessage.SEVERITY_ERROR,
//							IndexUIbean.getMessageResourceString(BEANMESSAGES,msgAdrtxt));
//				  flagSave = false;
//		   }

		   
//		   if (!flagSave && !errEikOrEgn) {			   
//			   errMsg = IndexUIbean.getMessageResourceString(BEANMESSAGES, REFCORRESPMSG1); //" Моля, въведете задължителната информация! ";	
//		   }
	     
	    } else {
		   flagSave = false;
	    }		
		return flagSave;
	}

   
   /** 
    * Изтриване на лице 
    */
   public void actionDelete(){ 
		try {
			LOGGER.debug("actionDelete>>>> ");
						
			JPA.getUtil().runInTransaction(() -> new ReferentDAO(getUserData()).delete(getRef()));
		   
			getSystemData().mergeReferentsClassif(getRef(), true );	
			
		
			ValueExpression expr2 = getValueExpression(CODEREF);
			ELContext ctx2 = getFacesContext().getELContext();
			if (expr2 != null) {
				expr2.setValue(ctx2, null);
			}	
			
		    // извиква remoteCommnad - ако има такава....
			String remoteCommnad = (String) getAttributes().get("onComplete");
			if (remoteCommnad != null && !"".equals(remoteCommnad)) {
				PrimeFaces.current().executeScript(remoteCommnad);
			}
						
		
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO,  IndexUIbean.getMessageResourceString(UIBEANMESSAGES, SUCCESSDELETEMSG) );
		} catch (ObjectInUseException e) {			
			LOGGER.error("Грешка при изтриване на лице! ObjectInUseException = {}", e.getMessage()); 
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
		}catch (BaseException e) {			
			LOGGER.error("Грешка при изтриване на лице ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,IndexUIbean.getMessageResourceString(UIBEANMESSAGES, ERRDATABASEMSG));
		}
		
   }
 
   /** 
    * коригиране данни на лице - изп. се само, ако е в модален прозорец
    * изивква се при затваряне на модалния прозореца (onhide) 
    * 
    */
   public void actionHideModal() {		
	   // за сега няма да се ползва
	   setRef(null);
	   setShowMe(false);
	   LOGGER.debug("actionHideModal>>>> ");
	}
   
   public void actionLoadDocsList() {
	   
//	 	това няма да се използва, тъй като Жоро направи нов метод в DocDAO специално за лицата, за да може да се изкарват всички документи към всички регистратури и да се добави колона с регистратурата на документа - 11.08.2020 г.

//		DocSearch tmpSearch = new DocSearch(null);
//		tmpSearch.setCodeRefCorresp(getRef().getCode());
//		setDocSearch(tmpSearch);
//		if (getRef().getCode() == null) {
//			setDocsList(null);
//		} else {
//			getDocSearch().buildQueryComp(getUserData());
//			setDocsList(new LazyDataModelSQL2Array(getDocSearch(), "a1 desc"));
//		}		
		
	   try {
		   
		   setDocSelectedAllM(null);
		   setDocSelectedTmp(null);
			
		   // списък документи, в които участва лице
			if (getRef().getCode() == null) {
				setDocsList(null);
			
			} else {

				SelectMetadata smd = new DocDAO(getUserData()).createSelectCorrespondentDocs(getRef().getCode());				
				setDocsList(new LazyDataModelSQL2Array(smd, "a1 desc"));
			}
		
	   } catch (DbErrorException e) {
		   LOGGER.error(e.getMessage(), e);
		   JSFUtils.addErrorMessage(e.getMessage(), e);
		}  
   }
   
	public String actionGotoViewDoc(Integer idObj) {
		return "docView.xhtml?faces-redirect=true&idObj=" + idObj;
	}
	
	public String actionGotoEditDoc(Integer idObj) {
		return "docEdit.jsf?faces-redirect=true&idObj=" + idObj;
	}
	
	/**
	 * Множествен избор на документи
	 *
	 * Избира всички редове от текущата страница
	 * @param event
	 */
	  public void onRowSelectAll(ToggleSelectEvent event) {    
    	
		  List<Object[]> tmpL = new ArrayList<>();    	
		  tmpL.addAll(getDocSelectedAllM());
    	
			if (event.isSelected()) {

				for (Object[] obj : getDocSelectedTmp()) {
					if (obj != null && obj.length > 0) {
						boolean bb = true;
						Long l2 = Long.valueOf(obj[0].toString());
						
						for (Object[] j : tmpL) {
							Long l1 = Long.valueOf(j[0].toString());
							if (l1.equals(l2)) {
								bb = false;
								break;
							}
						}
						
						if (bb) {
							tmpL.add(obj);
						}
					}
				}
    	
		  } else {
	    	
			List<Object[]> tmpLPageC = getDocsList().getResult();// rows from current page....

			for (Object[] obj : tmpLPageC) {
				if (obj != null && obj.length > 0) {
					Long l2 = Long.valueOf(obj[0].toString());
					for (Object[] j : tmpL) {
						Long l1 = Long.valueOf(j[0].toString());
						if (l1.equals(l2)) {
							tmpL.remove(j);
							break;
						}
					}
				}
			}	
		}
		
		  setDocSelectedAllM(tmpL);
		  if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("onToggleSelect->>");
		  }
	}
		    
    /** 
     * Select one row
     * @param event
     */
    public void onRowSelect(SelectEvent<?> event) {    	
    	if(event!=null  && event.getObject()!=null) {
    		List<Object[]> tmpList =  getDocSelectedAllM();
    		
    		Object[] obj = (Object[]) event.getObject();
    		boolean bb = true;
    		Integer l2 = Integer.valueOf(obj[0].toString());
			for (Object[] j : tmpList) { 
				Integer l1 = Integer.valueOf(j[0].toString());        			
	    		if(l1.equals(l2)) {
	    			bb = false;
	    			break;
	    		}
	   		}
			if(bb) {
				tmpList.add(obj);
				setDocSelectedAllM(tmpList);   
			}
    	}	    	
    	if (LOGGER.isDebugEnabled()) {
    		LOGGER.debug("1 onRowSelectIil->>{}",getDocSelectedAllM().size());
    	}
    }
		 
		    
    /**
     * unselect one row
     * @param event
     */
    public void onRowUnselect(UnselectEvent<?> event) {
    	if(event!=null  && event.getObject()!=null) {
    		Object[] obj = (Object[]) event.getObject();
    		List<Object[] > tmpL = new ArrayList<>();
    		tmpL.addAll(getDocSelectedAllM());
    		for (Object[] j : tmpL) {
    			Integer l1 = Integer.valueOf(j[0].toString());
    			Integer l2 = Integer.valueOf(obj[0].toString());
	    		if(l1.equals(l2)) {
	    			tmpL.remove(j);
	    			setDocSelectedAllM(tmpL);
	    			break;
	    		}
    		}
    		if (LOGGER.isDebugEnabled()) {
    			LOGGER.debug( "onRowUnselectIil->>{}",getDocSelectedAllM().size());
    		}
    	}
    }

    /**
     * За да се запази селектирането(визуалано на екрана) при преместване от една страница в друга
     */
    public void onPageUpdateSelected(){
    	if (getDocSelectedAllM() != null && !getDocSelectedAllM().isEmpty()) {
    		getDocSelectedTmp().clear();
    		getDocSelectedTmp().addAll(getDocSelectedAllM());
    	}	
    	if (LOGGER.isDebugEnabled()) {
    		LOGGER.debug( " onPageUpdateSelected->>{}",getDocSelectedTmp().size());
    	}
    }    
    
    /**
     * Метод за изтриване на документи, свързани с лице при заличаване на лице
     */
    public void actionDeleteDocs() {
    	
    	try {
    	
			for (Object[] doc : getDocSelectedAllM()) {
				
				Integer docId = SearchUtils.asInteger(doc[0]);

				JPA.getUtil().runInTransaction(() -> {					

					new DocDAO(getUserData()).deleteById(docId);
					
					FilesDAO filesDao = new FilesDAO(getUserData());		
					List<Files> filesList = filesDao.selectByFileObject(docId, DocuConstants.CODE_ZNACHENIE_JOURNAL_DOC); 
					
					if (filesList != null && !filesList.isEmpty()) {
						for (Files f : filesList) {
							filesDao.deleteFileObject(f);	
						}
					}					
				});
				
				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO,  IndexUIbean.getMessageResourceString(UIBEANMESSAGES, SUCCESSDELETEMSG) );				
			} 
			
			actionLoadDocsList();			
    	
    	} catch (ObjectInUseException e) {			
			LOGGER.error("Грешка при изтриване на документа - обекта се използва!", e); 
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(UIBEANMESSAGES, OBJECTINUSE), e.getMessage());
		
    	} catch (BaseException e) {			
			LOGGER.error("Грешка при изтриване на документа - грешка при работа с базата данни!", e);			
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(UIBEANMESSAGES, ERRDATABASEMSG), e.getMessage());
		
    	} finally {
			PrimeFaces.current().executeScript("scrollToErrors()");
		}
    	
    }
    
    /**
     * Метод за изтриване на файлове към документи, за които е отбелязано, че съдържат лична информация при заличаване на лице
     */
    public void actionDeleteFiles() {
    	
    	try {
        	
			for (Object[] doc : getDocSelectedAllM()) {
				
				Integer docId = SearchUtils.asInteger(doc[0]);

				JPA.getUtil().runInTransaction(() -> {	
					
					FilesDAO filesDao = new FilesDAO(getUserData());		
					List<Files> filesList = filesDao.selectByFileObjectDop(docId, DocuConstants.CODE_ZNACHENIE_JOURNAL_DOC); 
					
					if (filesList != null && !filesList.isEmpty()) {
						boolean delFile = false;
						for (Files f : filesList) {
							if(f.getPersonalData() != null && f.getPersonalData().equals(SearchUtils.asInteger(DocuConstants.CODE_ZNACHENIE_DA))) {
								delFile = true;

								f.setParrentID(docId);
								f.setParentObjCode(DocuConstants.CODE_ZNACHENIE_JOURNAL_DOC);
								
								filesDao.deleteFileObject(f);
								
								// ъпдейтване бройката на файловете
								Doc tmpDoc = new Doc();
								tmpDoc.setId(docId); 
								Integer countFiles = SearchUtils.asInteger(doc[8]) - 1;
								new DocDAO(getUserData()).updateCountFiles(tmpDoc, countFiles);
							}
						}
						
						if (delFile) {							
							JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, IndexUIbean.getMessageResourceString(UIBEANMESSAGES, SUCCESSDELETEMSG));
						} else {
							JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, IndexUIbean.getMessageResourceString(BEANMESSAGES, "correspForget.msgNotFileForDelete")); 
						}
					}					
				});
			} 
			
			actionLoadDocsList();
    	
    	} catch (ObjectInUseException e) {			
			LOGGER.error("Грешка при изтриване на файлове, съдържащи лична информация - обекта се използва!", e); 
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(UIBEANMESSAGES, OBJECTINUSE), e.getMessage());
		
    	} catch (BaseException e) {			
			LOGGER.error("Грешка при изтриване на файлове, съдържащи лична информация - грешка при работа с базата данни!", e);			
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(UIBEANMESSAGES, ERRDATABASEMSG), e.getMessage());
    	
    	} finally {
			PrimeFaces.current().executeScript("scrollToErrors()");
		} 
    	
    }   

	/** @return */
	public boolean isShowMe() {
		return (Boolean) getStateHelper().eval(PropertyKeys.SHOWME, false);
	}
	
	/** @param showMe */
	public void setShowMe(boolean showMe) {
		getStateHelper().put(PropertyKeys.SHOWME, showMe);
	}
	
	/** @return */
	public boolean isSeePersonalData() {
		return (Boolean) getStateHelper().eval(PropertyKeys.SEEPERSONALDATA, false);
	}
	/** @param seePersonalData */
	public void setSeePersonalData(boolean seePersonalData) {
		getStateHelper().put(PropertyKeys.SEEPERSONALDATA, seePersonalData);
	}

	/** @return the dateClassif */
	private Date getDateClassif() {
		if (this.dateClassif == null) {
			this.dateClassif = (Date) getAttributes().get("dateClassif");
			if (this.dateClassif == null) {
				this.dateClassif = new Date();
			}
		}
		return this.dateClassif;
	}
	
	private SystemData getSystemData() {
		if (this.systemData == null) {
			this.systemData =  (SystemData) JSFUtils.getManagedBean("systemData");
		}
		return this.systemData;
	}

	/** @return the userData */
	private UserData getUserData() {
		if (this.userData == null) {
			this.userData = (UserData) JSFUtils.getManagedBean("userData");
		}
		return this.userData;
	}
	
	/** 
	 *   1 само области; 2 - само общини; 3 - само населени места; без специфики - всикчи
	 * @return
	 */
	@SuppressWarnings({ "unchecked" })
	public Map<Integer, Object> getSpecificsEKATTE() {
		Map<Integer, Object> eval = (Map<Integer, Object>) getStateHelper().eval(PropertyKeys.EKATTESPEC, null);
		return eval != null ? eval : Collections.singletonMap(SysClassifAdapter.EKATTE_INDEX_TIP, 3);
	}

	/** @return */
	public Integer getLang() {
		return getUserData().getCurrentLang();
	}
	
	/** @return */
	public Date getCurrentDate() {
		return getDateClassif();
	}

//	public String getModalMsg() {
//		return modalMsg;
//	}
//
//
//	public void setModalMsg(String modalMsg) {
//		this.modalMsg = modalMsg;
//	}

	public Referent getRef() {
		return (Referent) getStateHelper().eval(PropertyKeys.REF, null);
	}

	public void setRef(Referent ref) {
		getStateHelper().put(PropertyKeys.REF, ref);
	}
	
	public SystemData getSd() {
		return (SystemData) getStateHelper().eval(PropertyKeys.SD, null);
	}
	
	public void setSd(SystemData sd) {
		getStateHelper().put(PropertyKeys.SD, sd);
	}
	
	public Referent getRefKoresp() {
		return (Referent) getStateHelper().eval(PropertyKeys.REFKORESP, null);
	}
	
	public void setRefKoresp(Referent refKoresp) {
		getStateHelper().put(PropertyKeys.REFKORESP, refKoresp);
	}

	public String getErrMsg() {
		return errMsg;
	}

	public void setErrMsg(String errMsg) {
		this.errMsg = errMsg;
	}
	
	public DocSearch getDocSearch() {
		return (DocSearch) getStateHelper().eval(PropertyKeys.DOCSEARCH, null);
	}

	public void setDocSearch(DocSearch docSearch) {
		getStateHelper().put(PropertyKeys.DOCSEARCH, docSearch);
	}
	
	public LazyDataModelSQL2Array getDocsList() {
		return (LazyDataModelSQL2Array) getStateHelper().eval(PropertyKeys.DOCSLIST, null);
	}

	public void setDocsList(LazyDataModelSQL2Array docsList) {
		getStateHelper().put(PropertyKeys.DOCSLIST, docsList);
	}
	
	@SuppressWarnings("unchecked")
	public List<Object[]> getDocSelectedTmp() {
		List<Object[]> eval = (List<Object[]>) getStateHelper().eval(PropertyKeys.DOCSELTMP, null);
		return eval != null ? eval : new ArrayList<>();		
	}

	public void setDocSelectedTmp(List<Object[]> docSelectedTmp) {
		getStateHelper().put(PropertyKeys.DOCSELTMP, docSelectedTmp);
	}

	@SuppressWarnings("unchecked")
	public List<Object[]> getDocSelectedAllM() {
		List<Object[]> eval = (List<Object[]>) getStateHelper().eval(PropertyKeys.DOCSEL, null);
		return eval != null ? eval : new ArrayList<>();
	}

	public void setDocSelectedAllM(List<Object[]> docSelectedAllM) {
		getStateHelper().put(PropertyKeys.DOCSEL, docSelectedAllM);
	}

	public TimeZone getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(TimeZone timeZone) {
		this.timeZone = timeZone;
	}

	public int getCountryBG() {
		if (this.countryBG == 0) {
			try {
				this.countryBG = Integer.parseInt(getSystemData().getSettingsValue("delo.countryBG"));
			} catch (Exception e) {
				LOGGER.error("Грешка при определяне на код на държава България от настройка: delo.countryBG", e);
			}
		}
		return this.countryBG;
	}

	public boolean isLoadedFromRegix() {
		return loadedFromRegix;
	}

	public void setLoadedFromRegix(boolean loadedFromRegix) {
		this.loadedFromRegix = loadedFromRegix;
	}

	public Boolean getClearRegixDiff() {
		return (Boolean) getStateHelper().eval(PropertyKeys.CLEARREGIXDIFF, null);
	}

	public void setClearRegixDiff(Boolean clearRegixDiff) {
		getStateHelper().put(PropertyKeys.CLEARREGIXDIFF, clearRegixDiff);
	}
}