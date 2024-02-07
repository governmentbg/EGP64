package com.ib.docu.components;

import java.math.BigInteger;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

import javax.annotation.PreDestroy;
import javax.el.ELContext;
import javax.el.ValueExpression;
import javax.faces.application.FacesMessage;
import javax.faces.component.FacesComponent;
import javax.faces.component.UINamingContainer;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.mail.internet.MimeUtility;
import javax.servlet.http.HttpServletRequest;

import org.primefaces.PrimeFaces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.docu.db.dao.DocDAO;
import com.ib.docu.db.dao.LockObjectDAO;
import com.ib.docu.db.dao.MMSCoachesDAO;
import com.ib.docu.db.dao.MMSSportObektDAO;
import com.ib.docu.db.dao.MMSVpisvaneDAO;
import com.ib.docu.db.dao.MMSVpisvaneDocDAO;
import com.ib.docu.db.dao.MMSsportFormirovanieDAO;
import com.ib.docu.db.dao.MMSsportObedinenieDAO;
import com.ib.docu.db.dao.ProcExeDAO;
import com.ib.docu.db.dto.Doc;
import com.ib.docu.db.dto.DocReferent;
import com.ib.docu.db.dto.MMSCoaches;
import com.ib.docu.db.dto.MMSSportObekt;
import com.ib.docu.db.dto.MMSSportnoObedinenie;
import com.ib.docu.db.dto.MMSVpisvane;
import com.ib.docu.db.dto.MMSVpisvaneDoc;
import com.ib.docu.db.dto.MMSsportFormirovanie;
import com.ib.docu.db.dto.ProcExe;
import com.ib.docu.system.DocuClassifAdapter;
import com.ib.docu.system.DocuConstants;
import com.ib.docu.system.SystemData;
import com.ib.docu.system.UserData;
import com.ib.indexui.pagination.LazyDataModelSQL2Array;
import com.ib.indexui.system.Constants;
import com.ib.indexui.system.IndexUIbean;
import com.ib.indexui.utils.JSFUtils;
import com.ib.system.SysConstants;
import com.ib.system.db.JPA;
import com.ib.system.db.SelectMetadata;
import com.ib.system.db.dao.FilesDAO;
import com.ib.system.db.dto.Files;
import com.ib.system.db.dto.SystemClassif;
import com.ib.system.exceptions.BaseException;
import com.ib.system.exceptions.DbErrorException;
import com.ib.system.exceptions.InvalidParameterException;
import com.ib.system.exceptions.ObjectInUseException;
import com.ib.system.utils.DateUtils;
import com.ib.system.utils.SearchUtils;
import com.ib.system.utils.ValidationUtils;

/** */
@FacesComponent(value = "compMMSVpisvane", createTag = true)
public class CompMMSVpisvane extends UINamingContainer {
	
	private enum PropertyKeys {
		REG, 
		REGDOC, 
		DOC, 
		FILESLIST, 
		SHOWME, 
		SHOWDATAFORDOC, 
		CHANGESTATUSZAIAV, 
		CHANGESTATUSVPIS, 
		VIEWBTNUDOSTDOC, 
		DOCSLIST, 
		CHECKUDOSTDOC, 
		VIEWCHECKUDOSTDOC, 
		VIEWBTNNEWFILE,
		RESHENIALIST,
		STATUSILIST,
		SASTOIANIEZAIAV,
		STATUSVPISVANE,
		AVTONOMNO,
		AVTONOMNODISABLED,
		REASONZAIAVLENIALIST,
		REASONVPISVANELIST,
		REFERENTSSIGNED,
		RERADONLYDATALICENZ		
	}
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CompMMSVpisvane.class);
	
	public static final String	UIBEANMESSAGES = "ui_beanMessages";
	public static final String	BEANMESSAGES = "beanMessages";
	public static final String  MSGPLSINS = "general.pleaseInsert";
	public static final String  ERRDATABASEMSG = "general.errDataBaseMsg";
	public static final String  SUCCESSAVEMSG = "general.succesSaveMsg";
	private static final String SUCCESSDELETEMSG = "general.successDeleteMsg";
	public static final String	OBJECTINUSE		 = "general.objectInUse";
	public static final String	LABELS = "labels";	
	
	private String errMsg = null;
	private UserData userData = null;
	private Date dateClassif = null;
	private SystemData systemData = null;
	private TimeZone timeZone = TimeZone.getDefault();
	
	private Integer idReg;
	private Integer idObject;
	private Integer codeObject;	
	private String nameObject;
	private Integer vidDoc;
	private String regNomer;
	private String regNomerObed;
	private Integer ekatte;
	private Integer vidObject;
	
	private MMSVpisvane tmpReg;
	
	private String typeObj = null;
	
	private boolean createDelo;
	
	private LazyDataModelSQL2Array etapExeList;	
	
	private List<MMSVpisvane> regList;
	
	private Integer nachinPoluch;	
	private String dopInfoNachinPoluch;
	
	//private boolean avtomNo = true;        			 // true -  автоматично генериране на номер
	//private boolean avtomNoDisabled = false;         // true - да се забрани достъпа до бутона за автоматичното генериране на номер
	
	private int unlockObj;
	
	private Doc docCopy;
	
	/**
	 * Търсене на вписвания - инциализира компонентата   <f:event type="preRenderComponent" listener="#{cc.initRegsComp()}" />
	 */	
	public void initRegsComp() {
		
		this.idReg = (Integer) getAttributes().get("idReg"); 
		this.idObject = (Integer) getAttributes().get("idObject"); 
		this.codeObject = (Integer) getAttributes().get("codeObject"); 		
		this.nameObject = (String) getAttributes().get("nameObject");
		this.vidDoc =  (Integer) getAttributes().get("vidDoc"); 
		this.regNomer = (String) getAttributes().get("regNomer");
		this.regNomerObed = (String) getAttributes().get("regNomerObed");
		this.ekatte = (Integer) getAttributes().get("ekatte"); 
		this.vidObject = (Integer) getAttributes().get("vidObject"); 		
		this.nachinPoluch = (Integer) getAttributes().get("nachinPoluch");
		this.dopInfoNachinPoluch = (String) getAttributes().get("dopInfoNachinPoluch");
		
		if (this.idReg != null) {
			actionNew();
			actionEditReg(this.idReg);
//			getReg().setNachinPoluchavane(this.nachinPoluch); 
//			getReg().setAddrMailPoluchavane(this.dopInfoNachinPoluch); 
		} else {
			actionNew();
		}
		
		setShowMe(true);
		setErrMsg(null);
		
		if (this.codeObject != null && this.codeObject.equals(DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORT_OBED)) {
			setTypeObj("Спортно обединение");			
		}
		
		if (this.codeObject != null && this.codeObject.equals(DocuConstants.CODE_ZNACHENIE_JOURNAL_COACHES)) {
			setTypeObj("Треньорски кадър");			
		}
		
		if (this.codeObject != null && this.codeObject.equals(DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_FORMATIONS)) {
			setTypeObj("Спортно формирование");			
		}
		
		if (this.codeObject != null && this.codeObject.equals(DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_OBJECTS)) {
			setTypeObj("Спортен обект");			
		}
		
		LOGGER.debug("initDocsComp!!!");		
	}
	
	/**
	 * Зачиства данните за вписване - бутон "нов"
	 * 
	 */
	public void actionNew() {
		
		tmpReg = new MMSVpisvane();
		tmpReg.setIdObject(this.idObject);
		tmpReg.setTypeObject(this.codeObject);		
		tmpReg.setStatusResultZaiavlenie(DocuConstants.CODE_ZNACHENIE_STATUS_ZAIAVLENIE_V_RAZGLEJDANE); 
		tmpReg.setDateStatusZaiavlenie(getCurrentDate()); 
		setReg(tmpReg);	
		
		setRegDoc(new MMSVpisvaneDoc());		
		getRegDoc().setIdObject(this.idObject);
		getRegDoc().setTypeObject(this.codeObject);  
		
		setDocsList(new ArrayList<>());
		
		setShowDataForDoc(false);
		
		setReasonZaiavleniaList(new ArrayList<>());
		setReasonVpisvaneList(new ArrayList<>());
		
		setChangeStatusZaiav(false);
		setChangeStatusVpis(false);
		setViewCheckUdostDoc(false);
		setReadOnlyDataLicenz(false);
	}
	
	public void actionLoadStatusiList() {
		
		setResheniaList(new ArrayList<>());
		setStatusiList(new ArrayList<>());
		
		try {
		
			List<SystemClassif> itemsReshenie = getSystemData().getSysClassification(DocuConstants.CODE_CLASSIF_STATUS_ZAIAVLENIE_NO_V_RAZGLEJDANE, new Date(), getLang());
			List<SystemClassif> itemsStatus = getSystemData().getSysClassification(DocuConstants.CODE_CLASSIF_STATUS_VPISVANE_NO_VPISAN, new Date(), getLang());
						
			for (SystemClassif item : itemsReshenie) {
				getResheniaList().add(new SelectItem(item.getCode(), item.getTekst()));				
			}
			
			Collections.sort(getResheniaList(), compatator);
			
			for (SystemClassif item : itemsStatus) {
				getStatusiList().add(new SelectItem(item.getCode(), item.getTekst()));				
			}
			
			Collections.sort(getStatusiList(), compatator);
		
		} catch (DbErrorException e) {
			LOGGER.error("Грешка при зареждане на логически списъци за статус на заявление и статус на вписване!!! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(UIBEANMESSAGES, ERRDATABASEMSG), e.getMessage());		
		}
		
	}
	
	/**
	 * Зарежда данните на вписването
	 * 
	 */
	public void actionEditReg(Integer idReg) {	
		
		boolean fLockOk = true;
		
		this.unlockObj = 0;
		
		if(JSFUtils.getFlashScopeValue("unlock") != null){
			this.unlockObj = (int) JSFUtils.getFlashScopeValue("unlock");
		}
		
		try {
			
			JPA.getUtil().runWithClose(() -> {					
				
				tmpReg = new MMSVpisvaneDAO(getUserData()).findById(idReg);	
				 
				setDocsList(new MMSVpisvaneDocDAO(getUserData()).findDocsList(idReg));
				
				setSastoianieZaiav(tmpReg.getStatusResultZaiavlenie());
				setStatusVpisvane(tmpReg.getStatusVpisvane());
			});	
			
			// проверка за заключен обект
			fLockOk = checkForLock(getReg().getIdObject());
			
			if (fLockOk) {
				// проверка за достъп
				lock(getReg().getIdObject());
				// отключване на всички обекти за потребителя(userId) и заключване на обекта, за да не може да се актуализира от друг
			}
			
			setReg(tmpReg);
			
//			tmpReg = null;
			
			actionLoadStatusiList();
			
			actionSearchEtapExeList();
			
			actionChangeStatusZaiavlenie(true);
			actionChangeStatusVpisvane(true);
			
			actionForCheckUD(true);
			
			loadNameReferentsList(getDoc().getReferentsSigned());

		} catch (BaseException e) {
			LOGGER.error("Грешка при зареждане данните на вписването! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
		}		
	}
	
	public void actionForCheckUD(boolean fromEdit) {
		
		actionNewDoc();
		setShowDataForDoc(false);
		
		if (getReg().getTypeObject().equals(DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORT_OBED)) {
			this.vidDoc = Integer.valueOf(DocuConstants.CODE_ZNACHENIE_VID_DOC_SPORT_LICENZ);
		
		} else if (getReg().getTypeObject().equals(DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_FORMATIONS)) {
			this.vidDoc = Integer.valueOf(DocuConstants.CODE_ZNACHENIE_VID_DOC_UDOST_REG_SPORT_FORMIROV);
			
		} else if (getReg().getTypeObject().equals(DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_OBJECTS)) {
			this.vidDoc = Integer.valueOf(DocuConstants.CODE_ZNACHENIE_VID_DOC_UDOST_REG_SPORT_OBEKT);
		
		} else if (getReg().getTypeObject().equals(DocuConstants.CODE_ZNACHENIE_JOURNAL_COACHES)) {
			this.vidDoc = Integer.valueOf(DocuConstants.CODE_ZNACHENIE_VID_DOC_UDOST_REG_TREN_KADAR);			
		}
		
		getDoc().setDocVid(vidDoc);
		System.out.println(getReg().getStatusVpisvane()); 
		System.out.println(getReg().getStatusResultZaiavlenie()); 
		
		if(getReg().getStatusVpisvane() == null && (getReg().getStatusResultZaiavlenie() != null && getReg().getStatusResultZaiavlenie().equals(DocuConstants.CODE_ZNACHENIE_STATUS_ZAIAVLENIE_VPISAN))) {
			getReg().setStatusVpisvane(DocuConstants.CODE_ZNACHENIE_STATUS_REG_VPISAN);	
		}
		if (getReg().getStatusVpisvane() != null && getReg().getStatusVpisvane().equals(DocuConstants.CODE_ZNACHENIE_STATUS_REG_VPISAN)) {
			if (actionSetDocSettings(this.vidDoc) && isViewCheckUdostDoc()) {
				setViewCheckUdostDoc(true);
				if (fromEdit) {
					if (!ValidationUtils.isNotBlank(getReg().getRnDocLicenz())) {  
						setCheckUdostDoc(true);
					} else {
						setCheckUdostDoc(false);
					}
				} else {					
					setCheckUdostDoc(true);
				}
			} else {
				setViewCheckUdostDoc(false);
				setReadOnlyDataLicenz(true);
			}
		}
	
		setViewBtnUdostDoc(false);
		
		if (!getDocsList().isEmpty()) { 
			for (Object[] doc : getDocsList()) {
				
				int vidDoc = ((BigInteger) doc[2]).intValue();
				
				if (vidDoc == DocuConstants.CODE_ZNACHENIE_VID_DOC_SPORT_LICENZ
						|| vidDoc == DocuConstants.CODE_ZNACHENIE_VID_DOC_UDOST_REG_SPORT_FORMIROV
						|| vidDoc == DocuConstants.CODE_ZNACHENIE_VID_DOC_UDOST_REG_SPORT_OBEKT
						|| vidDoc == DocuConstants.CODE_ZNACHENIE_VID_DOC_UDOST_REG_TREN_KADAR) {
					
					setViewBtnUdostDoc(true);
					setViewCheckUdostDoc(false);
					setReadOnlyDataLicenz(true);
					break;
				} else {
					setViewBtnUdostDoc(false);
					setViewCheckUdostDoc(true);
					setReadOnlyDataLicenz(false);
				}
			}
		}
		
	}
	
	/**
	 * Проверка за валидни данни
	 * 
	 * @return flagSave
	 */
	public boolean checkData() {

		boolean flagSave = true;
		FacesContext context = FacesContext.getCurrentInstance();
		String clientId = null;
				
		if (context != null && getReg() != null) {
			clientId = this.getClientId(context);				
					
			if (getReg().getStatusResultZaiavlenie() == null) {
				JSFUtils.addMessage(clientId + ":statusZaiavlenie:аutoCompl", FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(UIBEANMESSAGES, MSGPLSINS, 
						IndexUIbean.getMessageResourceString(LABELS, "compReg.decision")));
				errMsg = IndexUIbean.getMessageResourceString(UIBEANMESSAGES, MSGPLSINS, IndexUIbean.getMessageResourceString(LABELS, "compReg.statusZaiavlenie")) + "<br/>";	
				flagSave = false;	
			}
			
			if (getReg().getStatusResultZaiavlenie() == Integer.valueOf(DocuConstants.CODE_ZNACHENIE_STATUS_ZAIAVLENIE_VPISAN)) {
				
				if (!isCheckUdostDoc()) { 
					
					if (!ValidationUtils.isNotBlank(getReg().getRnDocLicenz())) {
						JSFUtils.addMessage(clientId + ":regNumL", FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(UIBEANMESSAGES, MSGPLSINS, 
								IndexUIbean.getMessageResourceString(LABELS, "compReg.nomUdostDoc")));
						errMsg = IndexUIbean.getMessageResourceString(UIBEANMESSAGES, MSGPLSINS, IndexUIbean.getMessageResourceString(LABELS, "compReg.nomUdostDoc")) + "<br/>";	
						flagSave = false;	
					}
					
					if (getReg().getDateDocLicenz() == null) {
						JSFUtils.addMessage(clientId + ":datDocL", FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(UIBEANMESSAGES, MSGPLSINS, 
								IndexUIbean.getMessageResourceString(LABELS, "compReg.dateUdostDoc")));
						errMsg = IndexUIbean.getMessageResourceString(UIBEANMESSAGES, MSGPLSINS, IndexUIbean.getMessageResourceString(LABELS, "compReg.dateUdostDoc")) + "<br/>";	
						flagSave = false;	
					}					
				}
				
				if (getReferentsSigned() != null && getReferentsSigned().isEmpty()) {
					JSFUtils.addMessage(clientId + ":signUd:аutoCompl", FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(UIBEANMESSAGES, MSGPLSINS, 
							IndexUIbean.getMessageResourceString(LABELS, "compReg.signatureDoc")));
					errMsg = IndexUIbean.getMessageResourceString(UIBEANMESSAGES, MSGPLSINS, IndexUIbean.getMessageResourceString(LABELS, "compReg.signatureDoc")) + "<br/>";	
					flagSave = false;
				}		
				
			}
			
			if (getReg().getTypeObject().equals(DocuConstants.CODE_ZNACHENIE_JOURNAL_COACHES)) {
				
				if (getReg().getVidSport() == null) {
					JSFUtils.addMessage(clientId + ":vidSport:аutoCompl", FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(UIBEANMESSAGES, MSGPLSINS, 
							IndexUIbean.getMessageResourceString(LABELS, "compReg.forVidSport")));
					errMsg = IndexUIbean.getMessageResourceString(UIBEANMESSAGES, MSGPLSINS, IndexUIbean.getMessageResourceString(LABELS, "compReg.forVidSport")) + "<br/>";	
					flagSave = false;					
				} 
				// Тази проверка вече е на 450-ти ред - 31.08.2023
//				else {
//					if (actionChangeVidSport()) {
//						errMsg = IndexUIbean.getMessageResourceString(BEANMESSAGES, "compReg.existCoachForVidSport") + "<br/>";	
//						flagSave = false;
//					}
//				}
								
				if (getReg().getDlajnost() == null
					&& (getReg().getStatusResultZaiavlenie() != null && getReg().getStatusResultZaiavlenie().equals(DocuConstants.CODE_ZNACHENIE_STATUS_ZAIAVLENIE_VPISAN))) { // КК поиска да сложа проверка само, ако статуса е вписан, тогава да иска задължително въвеждане на длъжност - 10.05.2023 г. 
					JSFUtils.addMessage(clientId + ":dlajnost", FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(UIBEANMESSAGES, MSGPLSINS, 
							IndexUIbean.getMessageResourceString(LABELS, "regGrSluj.position")));
					errMsg = IndexUIbean.getMessageResourceString(UIBEANMESSAGES, MSGPLSINS, IndexUIbean.getMessageResourceString(LABELS, "regGrSluj.position")) + "<br/>";	
					flagSave = false;	
				}
				
				if (getReg().getVidSport() != null && getReg().getDlajnost() != null) {
					if (actionChangeVidSport()) {
						errMsg = IndexUIbean.getMessageResourceString(BEANMESSAGES, "compReg.existCoachForVidSport") + "<br/>";	
						flagSave = false;
					}
				}
				
			}
			
			if (getReg().getNachinPoluchavane() != null) {
				
				if (getReg().getNachinPoluchavane().equals(DocuConstants.CODE_ZNACHENIE_PREDAVANE_EMAIL) 
					&& !ValidationUtils.isNotBlank(getReg().getAddrMailPoluchavane())) {
				JSFUtils.addMessage(clientId + ":addrMail", FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(UIBEANMESSAGES, MSGPLSINS, 
						IndexUIbean.getMessageResourceString(LABELS, "dvijenie.email")));
				errMsg = IndexUIbean.getMessageResourceString(UIBEANMESSAGES, MSGPLSINS, IndexUIbean.getMessageResourceString(LABELS, "dvijenie.email")) + "<br/>";	
				flagSave = false;					
				}
				
				if (getReg().getNachinPoluchavane().equals(DocuConstants.CODE_ZNACHENIE_PREDAVANE_POSHTA) 
						&& !ValidationUtils.isNotBlank(getReg().getAddrMailPoluchavane())) {
					JSFUtils.addMessage(clientId + ":addrMail", FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(UIBEANMESSAGES, MSGPLSINS, 
							IndexUIbean.getMessageResourceString(LABELS, "dvijenie.adres")));
					errMsg = IndexUIbean.getMessageResourceString(UIBEANMESSAGES, MSGPLSINS, IndexUIbean.getMessageResourceString(LABELS, "dvijenie.adres")) + "<br/>";	
					flagSave = false;					
				}
			}
			
			//Метода на Свилен за проверка на обединението - "Има въведени обединения с този/тези вид спорт! ЕИК: " + s); и не се допуска промяна на статуса на вписан 
			if (getReg().getTypeObject().equals(DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORT_OBED) 
					&& getReg().getStatusResultZaiavlenie() != null && getReg().getStatusResultZaiavlenie().equals(DocuConstants.CODE_ZNACHENIE_STATUS_ZAIAVLENIE_VPISAN) 
					&& getReg().getStatusVpisvane() != null && getReg().getStatusVpisvane().equals(DocuConstants.CODE_ZNACHENIE_STATUS_REG_VPISAN))  {
				
				String s = new MMSsportObedinenieDAO(MMSSportnoObedinenie.class, getUserData()).checkForDuplicateVidSportObedinenie(getReg().getIdObject(), null);
				s = s.replace("[", "").replace("]", "");
				if (s != null && !s.trim().isEmpty()) {
					JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString (BEANMESSAGES, "compReg.noVpisanoObed", s));
					errMsg = IndexUIbean.getMessageResourceString (BEANMESSAGES, "compReg.noVpisanoObed", s) + "<br/>";	
					flagSave = false;
				}
			}		
		}
		
		return flagSave;
	}	
	
	/**
	 * Запис на вписване
	 * 
	 */
	public void actionSave() {
	
		LOGGER.debug("actionSave >>> ");
		
		if(getReg().getStatusVpisvane() == null && (getReg().getStatusResultZaiavlenie() != null && getReg().getStatusResultZaiavlenie().equals(DocuConstants.CODE_ZNACHENIE_STATUS_ZAIAVLENIE_VPISAN))) {
			getReg().setStatusVpisvane(DocuConstants.CODE_ZNACHENIE_STATUS_REG_VPISAN);	
		}
	
		if (checkData()) {
			errMsg = null;
			
			try {
				
				tmpReg = getReg();
				
				JPA.getUtil().runInTransaction(() -> {
										
					if (tmpReg.getStatusResultZaiavlenie().equals(DocuConstants.CODE_ZNACHENIE_STATUS_ZAIAVLENIE_VPISAN)) {

						String regNomer = actionCreateRegNumForObject();
						
						if (regNomer != null) {
							
							if (tmpReg.getTypeObject().equals(DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORT_OBED)) {				
								new MMSsportObedinenieDAO(MMSSportnoObedinenie.class, getUserData()).updateRegNomSO(getReg().getIdObject(), regNomer);
							}
							
							// Това се коментира, тъй като вече няма да се слага рег. номер на треньора, а ще взима рег. номера на удостов. документ за всяко вписване - 31.08.2023 г.
//							if (tmpReg.getTypeObject().equals(DocuConstants.CODE_ZNACHENIE_JOURNAL_COACHES)) {
//								new MMSCoachesDAO(MMSCoaches.class, getUserData()).updateRegNomCoache(getReg().getIdObject(), regNomer);		
//							}
							
							if (tmpReg.getTypeObject().equals(DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_FORMATIONS)) {
								new MMSsportFormirovanieDAO(MMSsportFormirovanie.class, getUserData()).updateRegNomSF(getReg().getIdObject(), regNomer);	
							}
							
							if (tmpReg.getTypeObject().equals(DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_OBJECTS)) {
								new MMSSportObektDAO(MMSSportObekt.class, getUserData()).updateRegNomSO(getReg().getIdObject(), regNomer);													
							}							
						}
						
//						if (isChangeStatusZaiav()) {
//							if(tmpReg.getStatusVpisvane() == null) {
//								tmpReg.setStatusVpisvane(DocuConstants.CODE_ZNACHENIE_STATUS_REG_VPISAN);	
//								tmpReg.setDateStatusVpisvane(new Date());
//								setChangeStatusVpis(true); 							
//							}
//							
//							if (tmpReg.getDateDocLicenz() == null) {
//								tmpReg.setDateDocLicenz(getReg().getDateStatusZaiavlenie()); 
//							}
//						}
					}
					
					tmpReg = new MMSVpisvaneDAO(getUserData()).save(getReg());
					
					actionUpdateStatusObjects(tmpReg);
					
					setStatusVpisvane(getReg().getStatusVpisvane());
					setChangeStatusZaiav(false);
					setChangeStatusVpis(false);							
				});
				
				// TODO Ако е попълнен рег.номер и дата на лиценз ИЛИ е маркирано да се генерирара автоматично УД И чекбокса за автоматично генериране се вижда - 
				// тогава ще се минава мрез метода за автоматично генериране на УД - трябва да е само веднъж - да проверя!!!
				if (((ValidationUtils.isNotBlank(tmpReg.getRnDocLicenz()) &&  tmpReg.getDateDocLicenz() != null) 
						|| isCheckUdostDoc()) 
						&& isViewCheckUdostDoc()
						&& tmpReg.getStatusResultZaiavlenie().equals(DocuConstants.CODE_ZNACHENIE_STATUS_ZAIAVLENIE_VPISAN)) {
					 actionGenerateUdostDoc();
					 
					 if(ValidationUtils.isNotBlank(getReg().getRnDocLicenz())) {
						 tmpReg.setRnDocLicenz(getReg().getRnDocLicenz());
						 tmpReg.setDateDocLicenz(getReg().getDateDocLicenz()); 
						 setReadOnlyDataLicenz(true);
					 }
					 
					 actionForCheckUD(false);
					 
				} else {
					JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, IndexUIbean.getMessageResourceString(UIBEANMESSAGES, SUCCESSAVEMSG));	
					
				}
				
				 if(tmpReg != null && tmpReg.getId() != null) {
					   //връща id на избраното вписване
					    ValueExpression expr2 = getValueExpression("idReg");
						ELContext ctx2 = getFacesContext().getELContext();
						if (expr2 != null) {
							expr2.setValue(ctx2, tmpReg.getId());
						}	
				   }	
				
				// извиква remoteCommnad - ако има такава....
				String remoteCommnad = (String) getAttributes().get("onComplete");
				if (remoteCommnad != null && !"".equals(remoteCommnad)) {
					PrimeFaces.current().executeScript(remoteCommnad);
				}
			
				PrimeFaces.current().executeScript("scrollToErrors()");
				
				setReg(tmpReg);
				
				setSastoianieZaiav(getReg().getStatusResultZaiavlenie());
				setStatusVpisvane(getReg().getStatusVpisvane());
			
			} catch (BaseException e) {
				LOGGER.error("Грешка при запис на вписване ", e);
				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(UIBEANMESSAGES, ERRDATABASEMSG));
			}			
		}
	
	}
	
	public void actionUpdateStatusObjects(MMSVpisvane tmpReg1) {
		
		try {
			
//			JPA.getUtil().runInTransaction(() -> {
				
//				if (isChangeStatusZaiav() || isChangeStatusVpis()) {
			
					if (isChangeStatusZaiav() && tmpReg1.getStatusResultZaiavlenie().equals(DocuConstants.CODE_ZNACHENIE_STATUS_ZAIAVLENIE_VPISAN)) {
						if(tmpReg1.getStatusVpisvane() == null) {
							tmpReg1.setStatusVpisvane(DocuConstants.CODE_ZNACHENIE_STATUS_REG_VPISAN);	
							tmpReg1.setDateStatusVpisvane(new Date());
							setChangeStatusVpis(true); 							
						}
						
						if (tmpReg1.getDateDocLicenz() == null) {
							tmpReg1.setDateDocLicenz(new Date()); 
							 //tmpReg1.setDateDocLicenz(getReg().getDateStatusZaiavlenie()); // това го коментирам, след обсъждане с Даниела Д. (04.09.2023), тй като ако не я променят  сетването на тази дата е безумна!	
						}
					}
				
					Integer status = null;
					Date dateStatus = new Date();
					boolean changeStatus = false;
					
					System.out.println(">>> " + tmpReg1.getStatusVpisvane()); 
					System.out.println("<<< " +getStatusVpisvane()); 					
					
					if (tmpReg1.getStatusVpisvane() != null  && tmpReg1.getStatusVpisvane().equals(DocuConstants.CODE_ZNACHENIE_STATUS_REG_VPISAN) && !tmpReg1.getStatusVpisvane().equals(getStatusVpisvane())) {
						status = Integer.valueOf(DocuConstants.CODE_ZNACHENIE_STATUS_OBEKT_VPISAN);	
						changeStatus = true;
						dateStatus = tmpReg1.getDateStatusVpisvane();
					}
					
					if (tmpReg1.getStatusResultZaiavlenie().equals(DocuConstants.CODE_ZNACHENIE_STATUS_ZAIAVLENIE_V_RAZGLEJDANE)) {
						status = null;	
						changeStatus = true;
						dateStatus = null;
					}						
					
					if (tmpReg1.getTypeObject().equals(DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORT_OBED) 
							|| tmpReg1.getTypeObject().equals(DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_FORMATIONS)
							|| tmpReg1.getTypeObject().equals(DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_OBJECTS)) {
						
						
						if (tmpReg1.getStatusResultZaiavlenie().equals(DocuConstants.CODE_ZNACHENIE_STATUS_ZAIAVLENIE_OTKAZANO_VPISVANE) && !tmpReg1.getStatusResultZaiavlenie().equals(getSastoianieZaiav())) {
							status = Integer.valueOf(DocuConstants.CODE_ZNACHENIE_STATUS_OBEKT_OTKAZAN);
							changeStatus = true;
							dateStatus = tmpReg1.getDateStatusZaiavlenie();
						}
						
						if (tmpReg1.getStatusVpisvane() != null && (tmpReg1.getStatusVpisvane().equals(DocuConstants.CODE_ZNACHENIE_STATUS_REG_PREKRATENO_VPISVANE) 
								|| tmpReg1.getStatusVpisvane().equals(DocuConstants.CODE_ZNACHENIE_STATUS_REG_OTNETO_VPISVANE)
								|| tmpReg1.getStatusVpisvane().equals(DocuConstants.CODE_ZNACHENIE_STATUS_REG_ZALICHENO_VPISVANE))
								&& !tmpReg1.getStatusVpisvane().equals(getStatusVpisvane())) {
							status = Integer.valueOf(DocuConstants.CODE_ZNACHENIE_STATUS_OBEKT_ZALICHEN);
							changeStatus = true;
							dateStatus = tmpReg1.getDateStatusVpisvane();
						}
					
					} else if (tmpReg1.getTypeObject().equals(DocuConstants.CODE_ZNACHENIE_JOURNAL_COACHES)) {
						
						if ((!tmpReg1.getStatusResultZaiavlenie().equals(DocuConstants.CODE_ZNACHENIE_STATUS_ZAIAVLENIE_VPISAN) 
								|| !tmpReg1.getStatusResultZaiavlenie().equals(DocuConstants.CODE_ZNACHENIE_STATUS_ZAIAVLENIE_V_RAZGLEJDANE))
								&& (tmpReg1.getStatusVpisvane() != null && !tmpReg1.getStatusVpisvane().equals(DocuConstants.CODE_ZNACHENIE_STATUS_REG_VPISAN))) {
						
							if(!checkVpisvaneActiveForCoaches()) { // няма нито едно вписване със статус Вписан и може да се промени статуса
								
								if (tmpReg1.getStatusResultZaiavlenie().equals(DocuConstants.CODE_ZNACHENIE_STATUS_ZAIAVLENIE_OTKAZANO_VPISVANE) && !tmpReg1.getStatusResultZaiavlenie().equals(getSastoianieZaiav())) {
									status = Integer.valueOf(DocuConstants.CODE_ZNACHENIE_STATUS_OBEKT_OTKAZAN);
									changeStatus = true;
									dateStatus = tmpReg1.getDateStatusZaiavlenie();
								}
								
								if (tmpReg1.getStatusVpisvane() != null && (tmpReg1.getStatusVpisvane().equals(DocuConstants.CODE_ZNACHENIE_STATUS_REG_PREKRATENO_VPISVANE) 
										|| tmpReg1.getStatusVpisvane().equals(DocuConstants.CODE_ZNACHENIE_STATUS_REG_OTNETO_VPISVANE)
										|| tmpReg1.getStatusVpisvane().equals(DocuConstants.CODE_ZNACHENIE_STATUS_REG_ZALICHENO_VPISVANE))
										&& !tmpReg1.getStatusVpisvane().equals(getStatusVpisvane())) {
									status = Integer.valueOf(DocuConstants.CODE_ZNACHENIE_STATUS_OBEKT_ZALICHEN);	
									changeStatus = true;
									dateStatus = tmpReg1.getDateStatusVpisvane();
								}								
								
							} else {
								
								changeStatus = false;
							}
						}
					}
				
					if(changeStatus) {
						
						new MMSVpisvaneDAO(getUserData()).updateStatusReg(status, dateStatus, tmpReg1.getIdObject(), tmpReg1.getTypeObject().intValue());
					}
//				}
				
//			});
					
			if (isChangeStatusVpis()) {
				setStatusVpisvane(getReg().getStatusVpisvane()); 
			}
		
		} catch (DbErrorException e) {
			LOGGER.error("Грешка при ъпдейтване статуса на регистрите ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(UIBEANMESSAGES, ERRDATABASEMSG));
		}	
		
	}
	
	/**
	 * Зарежда списъци за основания, свързани със смяна на статуса на заявлението
	 * 
	 */
	public void actionChangeStatusZaiavlenie(boolean fromEdit) {
		
		setReasonZaiavleniaList(new ArrayList<>());
		
		try {
			
			//actionLoadStatusiList();
		
			if (getReg().getStatusResultZaiavlenie() != null) {				 
				
				if (getReg().getTypeObject().equals(DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORT_OBED)) {
					
					List<SystemClassif> tmpReasonZaiavleniaLst = getSystemData().getClassifByListVod(DocuConstants.CODE_LIST_REASON_STATUS_ZAIAVLENIE_SPORTNO_OBEDINENIE, getReg().getStatusResultZaiavlenie(), getLang(),  getDateClassif());
		    		List<SelectItem> items = new ArrayList<>(tmpReasonZaiavleniaLst.size());
					for (SystemClassif reason : tmpReasonZaiavleniaLst) {
						items.add(new SelectItem(reason.getCode(), reason.getTekst()));	
					}		
					setReasonZaiavleniaList(items);
				}	
				
				if (getReg().getTypeObject().equals(DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_FORMATIONS)) {
					
					List<SystemClassif> tmpReasonZaiavleniaLst = getSystemData().getClassifByListVod(DocuConstants.CODE_LIST_REASON_STATUS_ZAIAVLENIE_SPORTNO_FORMIROVANIE, getReg().getStatusResultZaiavlenie(), getLang(),  getDateClassif());
		    		List<SelectItem> items = new ArrayList<>(tmpReasonZaiavleniaLst.size());
					for (SystemClassif reason : tmpReasonZaiavleniaLst) {
						items.add(new SelectItem(reason.getCode(), reason.getTekst()));	
					}		
					setReasonZaiavleniaList(items);
				}	
		
				if (getReg().getTypeObject().equals(DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_OBJECTS)) {					

					List<SystemClassif> tmpReasonZaiavleniaLst = getSystemData().getClassifByListVod(DocuConstants.CODE_LIST_REASON_STATUS_ZAIAVLENIE_SPORTEN_OBEKT, getReg().getStatusResultZaiavlenie(), getLang(),  getDateClassif());
		    		List<SelectItem> items = new ArrayList<>(tmpReasonZaiavleniaLst.size());
					for (SystemClassif reason : tmpReasonZaiavleniaLst) {
						items.add(new SelectItem(reason.getCode(), reason.getTekst()));	
					}		
					setReasonZaiavleniaList(items);
				}
				
				if (getReg().getTypeObject().equals(DocuConstants.CODE_ZNACHENIE_JOURNAL_COACHES)) {
					
					List<SystemClassif> tmpReasonZaiavleniaLst = getSystemData().getClassifByListVod(DocuConstants.CODE_LIST_REASON_STATUS_ZAIAVLENIE_COACH, getReg().getStatusResultZaiavlenie(), getLang(),  getDateClassif());
		    		List<SelectItem> items = new ArrayList<>(tmpReasonZaiavleniaLst.size());
					for (SystemClassif reason : tmpReasonZaiavleniaLst) {
						items.add(new SelectItem(reason.getCode(), reason.getTekst()));	
					}		
					setReasonZaiavleniaList(items);
				}
				
				if (getReg().getDateStatusZaiavlenie() == null && getReg().getStatusResultZaiavlenie().equals(DocuConstants.CODE_ZNACHENIE_STATUS_ZAIAVLENIE_V_RAZGLEJDANE)){
					if (getReg().getDateDocZaiavlenie() != null) {
						getReg().setDateStatusZaiavlenie(getReg().getDateDocZaiavlenie()); 
					}  else {
						getReg().setDateStatusZaiavlenie(new Date());
					}					
				}
				
				if (getReg().getStatusResultZaiavlenie().equals(DocuConstants.CODE_ZNACHENIE_STATUS_ZAIAVLENIE_VPISAN)) {
					if(getReg().getStatusVpisvane() == null) { 
						getReg().setStatusVpisvane(DocuConstants.CODE_ZNACHENIE_STATUS_REG_VPISAN);	
						getReg().setDateStatusVpisvane(new Date());
						setChangeStatusVpis(true); 
						
//						JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_WARN, IndexUIbean.getMessageResourceString(BEANMESSAGES, "compReg.messForInsertDoc"));	
//						PrimeFaces.current().executeScript("scrollToErrors()");
					}
					
					if (getReg().getDateDocLicenz() == null) {
						getReg().setDateDocLicenz(new Date()); 
						//getReg().setDateDocLicenz(getReg().getDateStatusZaiavlenie()); // това го коментирам, след обсъждане с Даниела Д. (04.09.2023), тй като ако не я променят  сетването на тази дата е безумна!	
					}
				}
				
				if (!fromEdit) {
					setChangeStatusZaiav(true);
					getReg().setRnDocResult(null);
					getReg().setDateDocResult(null);
					actionForCheckUD(false);
				}
			}
		
		} catch (DbErrorException e) {
			LOGGER.error("Грешка при зареждане на логически списък за основания за статус на заявление!!! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(UIBEANMESSAGES, ERRDATABASEMSG), e.getMessage());		
		}
		
	}
	
	/**
	 * Зарежда списъци за основания, свързани със смяна на статуса на вписването
	 * 
	 */
	public void actionChangeStatusVpisvane(boolean fromEdit) {
		
		setReasonVpisvaneList(new ArrayList<>());
		
		//Може ли да е като смени статуса на вписването и да се показва в реда за съобщенията.
		//"След като изпълните "Запис", моля, въведете документ за промяна на статуса в секция "Документи към вписването"
		
		try {
			
			//actionLoadStatusiList();
			
			if (getReg().getStatusVpisvane() != null) {
				
				if (getReg().getTypeObject().equals(DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORT_OBED)) {
					
					List<SystemClassif> reasonVpisvaneLst = getSystemData().getClassifByListVod(DocuConstants.CODE_LIST_REASON_STATUS_VPISVANE_SPORTNO_OBEDINENIE, getReg().getStatusResultZaiavlenie(), getLang(),  getDateClassif());
		    		List<SelectItem> items = new ArrayList<>(reasonVpisvaneLst.size());
					for (SystemClassif reason : reasonVpisvaneLst) {
						items.add(new SelectItem(reason.getCode(), reason.getTekst()));	
					}		
					setReasonVpisvaneList(items); 										
				}	
				
				if (getReg().getTypeObject().equals(DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_FORMATIONS)) {					

					List<SystemClassif> reasonVpisvaneLst = getSystemData().getClassifByListVod(DocuConstants.CODE_LIST_REASON_STATUS_VPISVANE_SPORTNO_FORMIROVANIE, getReg().getStatusResultZaiavlenie(), getLang(),  getDateClassif());
		    		List<SelectItem> items = new ArrayList<>(reasonVpisvaneLst.size());
					for (SystemClassif reason : reasonVpisvaneLst) {
						items.add(new SelectItem(reason.getCode(), reason.getTekst()));	
					}		
					setReasonVpisvaneList(items); 
				}	
				
				if (getReg().getTypeObject().equals(DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_OBJECTS)) {

					List<SystemClassif> reasonVpisvaneLst = getSystemData().getClassifByListVod(DocuConstants.CODE_LIST_REASON_STATUS_VPISVANE_SPORTEN_OBEKT, getReg().getStatusResultZaiavlenie(), getLang(),  getDateClassif());
		    		List<SelectItem> items = new ArrayList<>(reasonVpisvaneLst.size());
					for (SystemClassif reason : reasonVpisvaneLst) {
						items.add(new SelectItem(reason.getCode(), reason.getTekst()));	
					}		
					setReasonVpisvaneList(items);
				}
				
				if (getReg().getTypeObject().equals(DocuConstants.CODE_ZNACHENIE_JOURNAL_COACHES)) {					

					List<SystemClassif> reasonVpisvaneLst = getSystemData().getClassifByListVod(DocuConstants.CODE_LIST_REASON_STATUS_VPISVANE_COACH, getReg().getStatusResultZaiavlenie(), getLang(),  getDateClassif());
		    		List<SelectItem> items = new ArrayList<>(reasonVpisvaneLst.size());
					for (SystemClassif reason : reasonVpisvaneLst) {
						items.add(new SelectItem(reason.getCode(), reason.getTekst()));	
					}		
					setReasonVpisvaneList(items); 
				}
				
				if (getReg().getDateStatusVpisvane() == null && getReg().getStatusVpisvane().equals(DocuConstants.CODE_ZNACHENIE_STATUS_REG_VPISAN)) {
					getReg().setDateStatusVpisvane(new Date()); 
				}
				
				if (!fromEdit) {
					setChangeStatusVpis(true); 
					getReg().setDateStatusVpisvane(new Date()); 
					getReg().setRnDocVpisvane(null);
					getReg().setDateDocVpisvane(null); 
					actionForCheckUD(false);
					
//					JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_WARN, IndexUIbean.getMessageResourceString(BEANMESSAGES, "compReg.messForInsertDoc"));	
//					PrimeFaces.current().executeScript("scrollToErrors()");
				}
			}
		
		} catch (DbErrorException e) {
			LOGGER.error("Грешка при зареждане на логически списък за основания за статус на вписване!!! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(UIBEANMESSAGES, ERRDATABASEMSG), e.getMessage());		
		}
		
	}
	
	/**
	 * Връща генериран регистров номер, ако е подаден празен от регистрите
	 * 
	 * @return registrovNomer
	 */
	public String actionCreateRegNumForObject() {
		
		String registrovNomer = null;
		this.regNomer = (String) getAttributes().get("regNomer");
		this.regNomerObed = (String) getAttributes().get("regNomerObed");
		this.ekatte = (Integer) getAttributes().get("ekatte");
		this.vidObject = (Integer) getAttributes().get("vidObject");
		
		try {
			
			if (getReg().getTypeObject().equals(DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORT_OBED)
					&& ((!ValidationUtils.isNotBlank(this.regNomer) || (this.regNomer.trim().equals("null")))) ) {	 			
				registrovNomer = new MMSVpisvaneDAO(getUserData()).genRegNomer(DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORT_OBED, null, null, this.vidObject);				
			}
			
			// Това се коментира, тъй като вече няма да се слага рег. номер на треньора, а ще взима рег. номера на удостов. документ за всяко вписване - 31.08.2023 г.
//			if (getReg().getTypeObject().equals(DocuConstants.CODE_ZNACHENIE_JOURNAL_COACHES) 
//					&& ((!ValidationUtils.isNotBlank(this.regNomer) || (this.regNomer.trim().equals("null")))) ) {
//				registrovNomer = new MMSVpisvaneDAO(getUserData()).genRegNomer(DocuConstants.CODE_ZNACHENIE_JOURNAL_COACHES, null, null);				
//			}
			
			if (getReg().getTypeObject().equals(DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_FORMATIONS) 
					&& ((!ValidationUtils.isNotBlank(this.regNomer) || (this.regNomer.trim().equals("null")))) ) {
				registrovNomer = new MMSVpisvaneDAO(getUserData()).genRegNomer(DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_FORMATIONS, this.regNomerObed, null, this.vidObject);		
			}
			
			if (getReg().getTypeObject().equals(DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_OBJECTS) 
					&& ((!ValidationUtils.isNotBlank(this.regNomer) || (this.regNomer.trim().equals("null")))) ) {
				registrovNomer = new MMSVpisvaneDAO(getUserData()).genRegNomer(DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_OBJECTS, null, ekatte, this.vidObject);							
			}	
		
		} catch (DbErrorException e) {
			LOGGER.error("Грешка при генериране на регистров номер на обект!!! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(UIBEANMESSAGES, ERRDATABASEMSG), e.getMessage());
		
		} catch (InvalidParameterException e) {
			LOGGER.error("Невалиден номер на спортно обединение!!! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(UIBEANMESSAGES, "general.invalidParameter"), e.getMessage());		
		}		
		
		return registrovNomer;		
	}
	
	/**
	 * Проверява дали има поне едно вписване за треньор със статус вписан
	 * 
	 * @return activeVpisvane
	 */
	private boolean checkVpisvaneActiveForCoaches() {
		
		boolean activeVpisvane = false;
		regList = new ArrayList<>();
		
		try {
			
			regList = new MMSVpisvaneDAO(getUserData()).findRegsListByIdAndType(getReg().getTypeObject(), getReg().getIdObject());

			if (!regList.isEmpty() && regList.size() > 1) {
				
				for (MMSVpisvane tmpVpisvane : regList) {
					// КК иска, ако има поне едно вписване със статус вписан, тогава статуса да не се променя на треньора, 
					// тъй като може да бъде заличен или отказан за конкретния спорт, но за другите спортове си остава вписан
					if (tmpVpisvane.getStatusVpisvane() != null && tmpVpisvane.getStatusVpisvane().equals(DocuConstants.CODE_ZNACHENIE_STATUS_REG_VPISAN)) {
						activeVpisvane = true;
						break;
					}								
				}			
			}
		
		} catch (BaseException e) {
			LOGGER.error("Грешка при търсене списък с вписвания към треньорски кадър ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(UIBEANMESSAGES, ERRDATABASEMSG));
		}
		
		return activeVpisvane;
	}
	
	public boolean actionChangeVidSport() {
		
		boolean existVsCoach = false;
		List<MMSVpisvane> vpisvaneList = new ArrayList<>();
		
		FacesContext context = FacesContext.getCurrentInstance();
		String clientId = this.getClientId(context);	
		
		try {
			
			vpisvaneList = new MMSCoachesDAO(MMSCoaches.class, getUserData()).findVpisvListByIdTypeVidSportAndDlaj(getReg().getTypeObject(), getReg().getIdObject(), getReg().getVidSport(), getReg().getDlajnost());

			if (vpisvaneList != null && !vpisvaneList.isEmpty()) {
				
				for (MMSVpisvane tmpVpisvane : vpisvaneList) {	
					// слагам проверка и намереното вписване статуса на заявлнеието ако е в разглеждане или вписан - да не позволява този спорт и тази длъжност, но да позволява при отказано вписванес
					if (!tmpVpisvane.getId().equals(getReg().getId())
							&& tmpVpisvane.getStatusResultZaiavlenie() != null 
							&& (tmpVpisvane.getStatusResultZaiavlenie().equals(Integer.valueOf(DocuConstants.CODE_ZNACHENIE_STATUS_ZAIAVLENIE_V_RAZGLEJDANE)) 
									|| tmpVpisvane.getStatusVpisvane().equals(Integer.valueOf(DocuConstants.CODE_ZNACHENIE_STATUS_REG_VPISAN))))  {																	
						existVsCoach = true;
						JSFUtils.addMessage(clientId + ":dlajnost", FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(BEANMESSAGES, "compReg.existCoachForVidSport"));	
						PrimeFaces.current().executeScript("scrollToErrors()");	
						break;												
					}
				}
			}
		
		} catch (BaseException e) {
			LOGGER.error("Грешка при търсене списък с вписвания към треньорски кадър ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(UIBEANMESSAGES, ERRDATABASEMSG));
		}
		
		return existVsCoach;

	}
	
	
	/**
	 * Изтриване на вписване
	 * 
	 */
	public void actionDelete() {

		LOGGER.debug("actionDelete >>> ");

		try {
			
			tmpReg = getReg();
			
			JPA.getUtil().runInTransaction(() -> { 
				new MMSVpisvaneDAO(getUserData()).deleteById(getReg().getId());
				
				new MMSVpisvaneDAO(getUserData()).updateStatusReg(DocuConstants.CODE_ZNACHENIE_STATUS_OBEKT_V_RAZGLEJDANE, new Date(), tmpReg.getIdObject(), tmpReg.getTypeObject().intValue());
				
				tmpReg = null;
				actionNew();		
			});
			
			ValueExpression expr2 = getValueExpression("idReg");
			ELContext ctx2 = getFacesContext().getELContext();
			if (expr2 != null) {
				expr2.setValue(ctx2, null);
			}

			// извиква remoteCommnad - ако има такава....
			String remoteCommnad = (String) getAttributes().get("onComplete");
			if (remoteCommnad != null && !"".equals(remoteCommnad)) {
				PrimeFaces.current().executeScript(remoteCommnad);
			}			

			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, IndexUIbean.getMessageResourceString(UIBEANMESSAGES, SUCCESSDELETEMSG));
			PrimeFaces.current().executeScript("scrollToErrors()");
		
		} catch (ObjectInUseException e) {
			LOGGER.error("Грешка при изтриване на вписване! ObjectInUseException = {}", e.getMessage());
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
		
		} catch (BaseException e) {
			LOGGER.error("Грешка при изтриване на вписване! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(UIBEANMESSAGES, ERRDATABASEMSG));
		}
	}	
	
	/**
	 * Търси да зареди етапите по процедурата за изпълнение, ако има такава към заявлението
	 * 
	 */
	public void actionSearchEtapExeList() {				
		
		try {
			
			JPA.getUtil().runWithClose(() -> { 
				
				List<Integer> idDocsList = new DocDAO(getUserData()).findDocIdList(getReg().getRnDocZaiavlenie(), getReg().getDateDocZaiavlenie());
				
				if (!idDocsList.isEmpty()) {
					
					Integer exeId = new ProcExeDAO(getUserData()).findExeIdByDoc(idDocsList.get(0)); 
					
					if (exeId != null) {
						
						ProcExe proc = new ProcExeDAO(getUserData()).findById(exeId);
						
						SelectMetadata smd = new ProcExeDAO(getUserData()).createSelectEtapExeList(exeId, proc.getDefId());
						String defaultSortColumn = "a0";
						this.etapExeList = new LazyDataModelSQL2Array(smd, defaultSortColumn);
					}
				}
			});
						
		
		} catch (BaseException e) { 
			LOGGER.error("Грешка при търсене на етапи на процедура за документ! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(UIBEANMESSAGES, ERRDATABASEMSG), e.getMessage());
		}	
	}
	
	/**
	 * Зачиства данните за документа - бутон "нов"
	 * 
	 */
	public void actionNewDoc() {	
		
		setShowDataForDoc(true);
		
		setDoc(new Doc());
		//getDoc().setDocVid((Integer) getAttributes().get("vidDoc"));	// КК каза да се махне документа по подразбиране
		//actionChangeVidDoc();		
		getDoc().setDocDate(getDateClassif());		

		getDoc().setReferentsAuthor(new ArrayList<>());
		getDoc().setReferentsSigned(new ArrayList<>());

		setRegDoc(new MMSVpisvaneDoc());		
		getRegDoc().setIdObject(getReg().getIdObject());
		getRegDoc().setTypeObject(getReg().getTypeObject()); 
		getRegDoc().setIdVpisvane(getReg().getId());
		
		setFilesList(new ArrayList<>());
		
		this.createDelo = false;	
		setViewBtnUdostDoc(false);
		setViewBtnViewFile(true);
		setAvtomNo(true); 
	}
	
	/**
	 * Зарежда данните за документа
	 * 
	 */
	public void actionEditDoc() {
		
		String idDoc = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("idDoc");
		
		setShowDataForDoc(true);
		
		try {
			
			JPA.getUtil().runWithClose(() -> {
				
				setDoc(new DocDAO(getUserData()).findById(Integer.valueOf(idDoc)));					
				
				Integer idRegDoc = new MMSVpisvaneDocDAO(getUserData()).findByIdReg(getReg().getId(), getDoc().getId());
				setRegDoc(new MMSVpisvaneDocDAO(getUserData()).findById(idRegDoc)); 

				// извличане на файловете от таблица с файловете
				setFilesList(new FilesDAO(getUserData()).selectByFileObjectDop(getDoc().getId(), DocuConstants.CODE_ZNACHENIE_JOURNAL_DOC));
				
				actionCheckExistFileForUD();
			});
			
			loadReferentsData();
			
			//System.out.println("getStatusVpisvane >>> " + getStatusVpisvane());
			
		} catch (BaseException e) {
			LOGGER.error("Грешка при зареждане данните на документа! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(UIBEANMESSAGES, ERRDATABASEMSG), e.getMessage());
		}
	}
	
	/**
	 * Зарежда по вид документ настройки
	 * 
	 */
	public void actionChangeVidDoc() {

		try {

			if (getDoc().getDocVid() != null) {
				
				// ако са удостовер. документи или лиценз  
				if (getDoc().getDocVid().equals(DocuConstants.CODE_ZNACHENIE_VID_DOC_SPORT_LICENZ)
					|| getDoc().getDocVid().equals(DocuConstants.CODE_ZNACHENIE_VID_DOC_UDOST_REG_SPORT_FORMIROV)
					|| getDoc().getDocVid().equals(DocuConstants.CODE_ZNACHENIE_VID_DOC_UDOST_REG_SPORT_OBEKT)
					|| getDoc().getDocVid().equals(DocuConstants.CODE_ZNACHENIE_VID_DOC_UDOST_REG_TREN_KADAR)) {

				if (getReg().getStatusResultZaiavlenie() == null) {
						JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(BEANMESSAGES, "compReg.noStatusZaiav"));
						PrimeFaces.current().executeScript("scrollToErrors()");
					}
				}				
				
				if (!getDocsList().isEmpty()) { 
					if (existLicenzOrUdostDoc()) {
						JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(BEANMESSAGES, "compReg.existLicenzOrUdostDoc"));	
						PrimeFaces.current().executeScript("scrollToErrors()");
					}
				}
				
				getDoc().setOtnosno(getSystemData().decodeItem(DocuConstants.CODE_CLASSIF_DOC_VID, getDoc().getDocVid(), getLang(),  getDateClassif()) + " на " + "'" + (String) getAttributes().get("nameObject") + "'");
				
				actionSetDocSettings(getDoc().getDocVid()); 
				
				actionCheckRegNumForDoc(getDoc().getRnDoc());				
			
			} else {
				getDoc().setOtnosno(null);
				getDoc().setDocType(null);
			}
			
		} catch (DbErrorException e) {
			LOGGER.error("Грешка при разкодиране на вид документ!! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(UIBEANMESSAGES, ERRDATABASEMSG), e.getMessage());
		}	
	}
	
	/**
	 * Зарежда по характеристиките на документа по вид документ
	 * 
	 * @param vidDoc
	 * @return validSettings
	 */
	private boolean actionSetDocSettings(Integer vidDoc) {
		
		boolean validSettings = false;
		
		try {
			// настройка по вид документ и регистратура
			Object[] docVidSetting = new DocDAO(getUserData()).findDocSettings(getUserData().getRegistratura(), vidDoc, getSystemData());
			
			if (docVidSetting == null) {
				
				String noSett = getSystemData().decodeItem(DocuConstants.CODE_CLASSIF_DOC_VID, vidDoc, getLang(),  getDateClassif());				
				validSettings = false;	
				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(BEANMESSAGES, "compReg.noDocSettings", noSett));	
				PrimeFaces.current().executeScript("scrollToErrors()");
			
			} else {
				
				getDoc().setRegisterId((Integer) docVidSetting[1]);
				this.createDelo = Objects.equals(docVidSetting[2], SysConstants.CODE_ZNACHENIE_DA);
				if ((Integer) docVidSetting[4] != null) {
					setViewBtnUdostDoc(true);
				} else {
					setViewBtnUdostDoc(false);
				}
				
				Integer typeDocByRegister = (Integer) getSystemData().getItemSpecific(DocuConstants.CODE_CLASSIF_REGISTRI, getDoc().getRegisterId(), getLang(), getDateClassif(), DocuClassifAdapter.REGISTRI_INDEX_DOC_TYPE);
				
				getDoc().setRegistraturaId(getUserData().getRegistratura());
				getDoc().setDocType(typeDocByRegister);
				getDoc().setFreeAccess(Constants.CODE_ZNACHENIE_DA);
				
				Integer alg = null;
				setAvtomNoDisabled(false); 
				//this.avtomNoDisabled = false;

				if (getDoc().getRegisterId() != null) {
					alg = (Integer) getSystemData().getItemSpecific(DocuConstants.CODE_CLASSIF_REGISTRI, getDoc().getRegisterId(), getLang(), new Date(), DocuClassifAdapter.REGISTRI_INDEX_ALG);
				}

				if (alg != null && alg.equals(DocuConstants.CODE_ZNACHENIE_ALG_FREE)) {
					setAvtomNo(false); 
					setAvtomNoDisabled(true); 
					
//					this.avtomNo = false; // да се забрани автом. генер. на номера! Да се прави проверка за въведен номер, ако алгоритъмът е "произволен рег.номер"
//					this.avtomNoDisabled = true;
					setViewCheckUdostDoc(false); // да не се показва чек-бокса за автоматично генериране на удостов. документ и да се пише на ръка рег. номера
				
				} else if (SearchUtils.isEmpty(getDoc().getRnDoc())) {
					setAvtomNo(true); 
					
					//this.avtomNo = true; // да се промени според регистъра, само ако вече няма нищо въведено в полето за номер на документ
					setViewCheckUdostDoc(true); // да се показва чек-бокса за автоматично генериране на удостов. документ
				}
				
				validSettings = true;
			}
		
		} catch (DbErrorException e) {
			LOGGER.error("Грешка при зареждане настройки по вид документ!! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(UIBEANMESSAGES, ERRDATABASEMSG), e.getMessage());
		}
		
		return validSettings;	
	}
	
	/**
	 * Зарежда(разкодира) имената на референтите в списъците - изготвил, подписал
	 * @param listRef
	 * 
	 */
	private void loadNameReferentsList(List<DocReferent> listRef) {
		
		if (listRef == null || listRef.isEmpty()) {
			return;
		}
		
		for(DocReferent drItem: listRef ) {
			
			String tekst = "";
			
			try {				
				tekst = getSystemData().decodeItem(DocuConstants.CODE_CLASSIF_ADMIN_STR, drItem.getCodeRef(), getLang(), getDoc().getDocDate());
				drItem.setTekst(tekst);				
				
			} catch (DbErrorException e) {
				LOGGER.error("Грешка при зареждане на референти (автор, подписал, съгласувал)! ", e);
				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(UIBEANMESSAGES, ERRDATABASEMSG), e.getMessage());
			}		
		}
	}
	
	/**
	 * зарежда референтите към документ
	 * 
	 */
	private void loadReferentsData() {
		
		loadNameReferentsList(getDoc().getReferentsAuthor());
		loadNameReferentsList(getDoc().getReferentsSigned());		
	}	
	
	/**
	 * Валидира данните за документа
	 * 
	 * @return flagSave
	 */
	private boolean checkDataForDoc() {
		
		boolean flagSave = true;		
		FacesContext context = FacesContext.getCurrentInstance();
		String clientId = null;
				
		if (context != null && getDoc() != null) {
			clientId = this.getClientId(context);
			
			if(!isAvtomNo() && SearchUtils.isEmpty(getDoc().getRnDoc())) {
				JSFUtils.addMessage(clientId + ":regN", FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString (UIBEANMESSAGES, MSGPLSINS, 
						IndexUIbean.getMessageResourceString(LABELS, "docu.regNom")));
				errMsg = IndexUIbean.getMessageResourceString(UIBEANMESSAGES, MSGPLSINS, IndexUIbean.getMessageResourceString(LABELS, "docu.regNom")) + "<br/>";	
				flagSave = false;
			}
			
			if (getDoc().getDocVid() == null) {
				JSFUtils.addMessage(clientId + ":dVid:аutoCompl", FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(UIBEANMESSAGES, MSGPLSINS, 
						IndexUIbean.getMessageResourceString(LABELS, "docu.vid")));
				errMsg = IndexUIbean.getMessageResourceString(UIBEANMESSAGES, MSGPLSINS, IndexUIbean.getMessageResourceString(LABELS, "docu.vid")) + "<br/>";	
				flagSave = false;	
			
			} else {

				if(!actionSetDocSettings(getDoc().getDocVid())) {
					
					try {
						
						String noSett = getSystemData().decodeItem(DocuConstants.CODE_CLASSIF_DOC_VID, getDoc().getDocVid(), getLang(),  getDateClassif());
						
						errMsg = IndexUIbean.getMessageResourceString(BEANMESSAGES, "compReg.noDocSettings", noSett) + "<br/>";	
						
					} catch (DbErrorException e) {
						LOGGER.error("Грешка при разкодиране на вид документ!! ", e);
						JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(UIBEANMESSAGES, ERRDATABASEMSG), e.getMessage());
					}		
					
					flagSave = false;						
				}
				
				// ако са удостовер. документи или лиценз - да запише автоматично рег. номер и дата на удост. документ
				if (getDoc().getDocVid().equals(DocuConstants.CODE_ZNACHENIE_VID_DOC_SPORT_LICENZ)
					|| getDoc().getDocVid().equals(DocuConstants.CODE_ZNACHENIE_VID_DOC_UDOST_REG_SPORT_FORMIROV) 
					|| getDoc().getDocVid().equals(DocuConstants.CODE_ZNACHENIE_VID_DOC_UDOST_REG_SPORT_OBEKT) 
					|| getDoc().getDocVid().equals(DocuConstants.CODE_ZNACHENIE_VID_DOC_UDOST_REG_TREN_KADAR)) {
					
					if (getReg().getStatusResultZaiavlenie() == null) {						
						JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(BEANMESSAGES, "compReg.noStatusZaiav"));
						flagSave = false;	
					}
				}
			}
			
			if(SearchUtils.isEmpty(getDoc().getOtnosno())) {
				JSFUtils.addMessage(clientId + ":otnosno",FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(UIBEANMESSAGES, MSGPLSINS, 
						IndexUIbean.getMessageResourceString(LABELS, "docu.otnosno")));
				errMsg = IndexUIbean.getMessageResourceString(UIBEANMESSAGES, MSGPLSINS, IndexUIbean.getMessageResourceString(LABELS, "docu.otnosno")) + "<br/>";	
				flagSave = false;	
			}
			
			if (!getDocsList().isEmpty()) { 
				if (existLicenzOrUdostDoc()) {
					JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(BEANMESSAGES, "compReg.existLicenzOrUdostDoc"));
					errMsg = IndexUIbean.getMessageResourceString(BEANMESSAGES, "compReg.existLicenzOrUdostDoc") + "<br/>";	
					flagSave = false;	
				}
			}
			
		}
		
		return flagSave;		
	}	
	
	/**
	 * Записва документа
	 * 
	 */
	public void actionSaveDoc(boolean fromUD) {
		
		if(!checkDataForDoc()) {
			PrimeFaces.current().executeScript("scrollToErrors()");
			return;
		}
		
		try {
			
			 if(!isAvtomNo()) {
				String prefix = getSystemData().decodeItemDopInfo(DocuConstants.CODE_CLASSIF_REGISTRI, getDoc().getRegisterId(), getLang(), new Date());
				
				if (prefix != null) {				
					getDoc().setRnPrefix(prefix);
					getDoc().setRnDoc(getDoc().getRnPrefix() + "-"+ getDoc().getRnDoc());
				}				
			 }
			 
			 if (fromUD) {
				 getDoc().setReferentsSigned(getReferentsSigned()); 
			 }
			
			
			JPA.getUtil().runInTransaction(() -> { 
				
				getDoc().setCountFiles(getFilesList() == null ? 0 : getFilesList().size());
				
				setDoc(new DocDAO(getUserData()).save(getDoc(), this.createDelo, null, null, getSystemData()));
				
				if (getDoc().getId() != null) {
					
					getRegDoc().setIdDoc(getDoc().getId());
					
					setRegDoc(new MMSVpisvaneDocDAO(getUserData()).save(getRegDoc()));
				}				
				
				// ако са удостовер. документи или лиценз - да запише автоматично рег. номер и дата на удост. документ
				if (getDoc().getDocVid().equals(DocuConstants.CODE_ZNACHENIE_VID_DOC_SPORT_LICENZ)
					|| getDoc().getDocVid().equals(DocuConstants.CODE_ZNACHENIE_VID_DOC_UDOST_REG_SPORT_FORMIROV) 
					|| getDoc().getDocVid().equals(DocuConstants.CODE_ZNACHENIE_VID_DOC_UDOST_REG_SPORT_OBEKT) 
					|| getDoc().getDocVid().equals(DocuConstants.CODE_ZNACHENIE_VID_DOC_UDOST_REG_TREN_KADAR)) {
					
						getReg().setRnDocLicenz(getDoc().getRnDoc());
						getReg().setDateDocLicenz(getDoc().getDocDate()); 
					
				}
				
				if (getReg().getStatusResultZaiavlenie() != null) {
				
					if ((getDoc().getDocVid().equals(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAPOVED_VPISVANE) && getReg().getStatusResultZaiavlenie().equals(DocuConstants.CODE_ZNACHENIE_STATUS_ZAIAVLENIE_VPISAN)) 
						|| (getDoc().getDocVid().equals(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAPOVED_OTKAZANO_VPISVANE) && getReg().getStatusResultZaiavlenie().equals(DocuConstants.CODE_ZNACHENIE_STATUS_ZAIAVLENIE_VPISAN))
						|| (getDoc().getDocVid().equals(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAPOVED_OSTAVIANE_BEZ_POSLEDSTVIA) && getReg().getStatusResultZaiavlenie().equals(DocuConstants.CODE_ZNACHENIE_STATUS_ZAIAVLENIE_OSTAVENO_BEZ_POSLEDSTVIE))
						|| (getDoc().getDocVid().equals(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAPOVED_PREKRATENO_PROIZVODSTVO) && getReg().getStatusResultZaiavlenie().equals(DocuConstants.CODE_ZNACHENIE_STATUS_ZAIAVLENIE_PREKRATENO_PROIZVODSTVO))) {
					
						getReg().setRnDocResult(getDoc().getRnDoc());
						getReg().setDateDocResult(getDoc().getDocDate());
						getReg().setDateStatusZaiavlenie(getDoc().getDocDate());
					}				
				}
				
				if (getReg().getStatusVpisvane() != null) {
					
					if ((getDoc().getDocVid().equals(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAPOVED_ZALICHAVANE) && getReg().getStatusVpisvane().equals(DocuConstants.CODE_ZNACHENIE_STATUS_REG_ZALICHENO_VPISVANE))
							|| (getDoc().getDocVid().equals(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAPOVED_PREKRATIAVANE) && getReg().getStatusVpisvane().equals(DocuConstants.CODE_ZNACHENIE_STATUS_REG_PREKRATENO_VPISVANE) )
							|| (getDoc().getDocVid().equals(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAPOVED_OTNEMANE) && getReg().getStatusVpisvane().equals(DocuConstants.CODE_ZNACHENIE_STATUS_REG_OTNETO_VPISVANE))) {
						
						getReg().setRnDocVpisvane(getDoc().getRnDoc());
						getReg().setDateDocVpisvane(getDoc().getDocDate());
						getReg().setDateStatusVpisvane(getDoc().getDocDate());
						
						if (getReg().getTypeObject().equals(DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORT_OBED) 
								&& getReg().getStatusVpisvane().equals(DocuConstants.CODE_ZNACHENIE_STATUS_REG_ZALICHENO_VPISVANE)) {
							
							 new MMSsportObedinenieDAO(MMSSportnoObedinenie.class, getUserData()).saveTaskZalichavane(getReg().getIdObject(), getDoc().getDocDate(), getSystemData());						
						}											
					}
				}
				
				getReg().setStatusVpisvane(getStatusVpisvane());
				setReg(new MMSVpisvaneDAO(getUserData()).save(getReg()));
				
				if (getReg().getStatusResultZaiavlenie() != null) {
					actionUpdateStatusObjects(getReg());
				}
				
				setDocsList(new MMSVpisvaneDocDAO(getUserData()).findDocsList(getReg().getId()));
			
			});	
			
			actionForCheckUD(false);
			
			if (!fromUD) {
				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, IndexUIbean.getMessageResourceString(UIBEANMESSAGES, SUCCESSAVEMSG));
				PrimeFaces.current().executeScript("scrollToErrors()");
			} else {
				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, IndexUIbean.getMessageResourceString(UIBEANMESSAGES, SUCCESSAVEMSG));
				setShowDataForDoc(false);
			}
			
			
		} catch (BaseException e) {
			LOGGER.error("Грешка при запис на документа! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());		
		} 			
		
	}
	
	/**
	 * Изтрива документа
	 * 
	 */
	public void actionDeleteDoc() {
		
		LOGGER.debug("actionDelete >>> ");

		try {
			
			Integer vidDoc = getDoc().getDocVid();
			
			JPA.getUtil().runInTransaction(() -> {
				
				new MMSVpisvaneDocDAO(getUserData()).deleteById(getRegDoc().getId());
				
				actionNewDoc();
				setShowDataForDoc(false);
				
				//setDocsList(new MMSVpisvaneDocDAO(getUserData()).findDocsList(getReg().getId()));
				
				// ако са удостовер. документи или лиценз - да запише автоматично рег. номер и дата на удост. документ
				if (vidDoc.equals(DocuConstants.CODE_ZNACHENIE_VID_DOC_SPORT_LICENZ)
					|| vidDoc.equals(DocuConstants.CODE_ZNACHENIE_VID_DOC_UDOST_REG_SPORT_FORMIROV) 
					|| vidDoc.equals(DocuConstants.CODE_ZNACHENIE_VID_DOC_UDOST_REG_SPORT_OBEKT) 
					|| vidDoc.equals(DocuConstants.CODE_ZNACHENIE_VID_DOC_UDOST_REG_TREN_KADAR)) {
					
					// зачиствам полетата за УД
					getReg().setRnDocLicenz(null);
					getReg().setDateDocLicenz(null);
					getReg().setStatusVpisvane(getStatusVpisvane());
					//getReg().setDateDocLicenz(getReg().getDateStatusZaiavlenie()); // това го коментирам, след обсъждане с Даниела Д. (04.09.2023), тй като ако не я променят  сетването на тази дата е безумна!	
					
					setReg(new MMSVpisvaneDAO(getUserData()).save(getReg()));
					
					//след запис на вписването сетвам днешна дата на датата на УД
					getReg().setDateDocLicenz(new Date());
				}
			
			});	

			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, IndexUIbean.getMessageResourceString(UIBEANMESSAGES, SUCCESSDELETEMSG));
			PrimeFaces.current().executeScript("scrollToErrors()");	
			
			actionEditReg(getReg().getId()); 
		
		} catch (ObjectInUseException e) {
			LOGGER.error("Грешка при изтриване на документ! ObjectInUseException = {}", e.getMessage());
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
			PrimeFaces.current().executeScript("scrollToErrors()");
		
		} catch (BaseException e) {
			LOGGER.error("Грешка при изтриване на документ! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(UIBEANMESSAGES, ERRDATABASEMSG));
			PrimeFaces.current().executeScript("scrollToErrors()");
		}
		
	}
	
	/**
	 * Изключва автоматичното генериране на номера
	 * 
	 */
	public void actionChangeAvtomNo() {
	   if(isAvtomNo()) {
		   this.getDoc().setRnDoc(null); 
	   }
	}
	
	/**
	 * Включва и изключва автоматичното генериране на удостоверителния документ
	 * 
	 */
	public void actionCheckUdostDoc() {
		
		if (isCheckUdostDoc()) {
			getReg().setRnDocLicenz(null);
			if (getReg().getDateDocLicenz() == null) {
				getReg().setDateDocLicenz(new Date()); 
			}
		}		
	}
	
	/**
	 * Проверява дали съществува вече лиценз или удостоверителен документ към това вписване, тъй като трябва да бъде само един за вписване
	 * 
	 * @return exist
	 */
	public boolean existLicenzOrUdostDoc() {
		
		boolean exist = false;
		
		for (Object[] doc : getDocsList()) {

			int vidDoc = ((BigInteger) doc[2]).intValue();

			// ако има вече въведен удостовер. документи или лиценз - да не допуска друг такъв вид да се запише
			if ( (getDoc().getId() == null)
				&& (getDoc().getDocVid() == DocuConstants.CODE_ZNACHENIE_VID_DOC_SPORT_LICENZ
					|| getDoc().getDocVid() == DocuConstants.CODE_ZNACHENIE_VID_DOC_UDOST_REG_SPORT_FORMIROV
					|| getDoc().getDocVid() == DocuConstants.CODE_ZNACHENIE_VID_DOC_UDOST_REG_SPORT_OBEKT
					|| getDoc().getDocVid() == DocuConstants.CODE_ZNACHENIE_VID_DOC_UDOST_REG_TREN_KADAR)
				&& (vidDoc == DocuConstants.CODE_ZNACHENIE_VID_DOC_SPORT_LICENZ
					|| vidDoc == DocuConstants.CODE_ZNACHENIE_VID_DOC_UDOST_REG_SPORT_FORMIROV
					|| vidDoc == DocuConstants.CODE_ZNACHENIE_VID_DOC_UDOST_REG_SPORT_OBEKT
					|| vidDoc == DocuConstants.CODE_ZNACHENIE_VID_DOC_UDOST_REG_TREN_KADAR) ) {
				
				exist = true;				
				break;
			}
		}
		
		return exist;  
	}
	
	/**
	 * Автоматично генерира удостоверителен документ
	 * 
	 */
	public void actionGenerateUdostDoc() {
		
		try {
		
		if (isViewCheckUdostDoc() && !isCheckUdostDoc()) {
			
			getDoc().setRnDoc(getReg().getRnDocLicenz()); 
			getDoc().setDateReg(getReg().getDateDocLicenz()); 
		}
			
			getDoc().setDocDate(getReg().getDateDocLicenz());  
			
			getDoc().setOtnosno(getSystemData().decodeItem(DocuConstants.CODE_CLASSIF_DOC_VID, getDoc().getDocVid(), getLang(),  getDateClassif()) + " на " + "'" + (String) getAttributes().get("nameObject") + "'");
			
			actionSaveDoc(true);
			
			if (getDoc().getId() != null) {
				setViewCheckUdostDoc(false);				
			} else {
				getDoc().setRnDoc(null);
			}
			
			setViewBtnUdostDoc(true);
		
		//createUdostDokument(getDoc().getId(), getDoc().getDocVid());		
		
		} catch (BaseException e) {
			LOGGER.error("Грешка при разкодиране на вид документ!! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(UIBEANMESSAGES, ERRDATABASEMSG), e.getMessage());		
		}
		
	}	
	
	/**
	 * На бутон удостоверителен документ се проверява
	 * ако няма генериран файл се вика метода за генериране от шаблона, а ако има - вика се файла за сваляне
	 * 
	 */
	public void actionUdostovDocum() {
		
		try {
		
			if (isViewBtnUdostDoc()) {
				
				List<Object[]> tmpLDocList = new ArrayList<>();
			
				if (!getDocsList().isEmpty()) { 
					for (Object[] doc : getDocsList()) {
						
						int vidDoc = ((BigInteger) doc[2]).intValue();						
						
						if (vidDoc == DocuConstants.CODE_ZNACHENIE_VID_DOC_SPORT_LICENZ
								|| vidDoc == DocuConstants.CODE_ZNACHENIE_VID_DOC_UDOST_REG_SPORT_FORMIROV
								|| vidDoc == DocuConstants.CODE_ZNACHENIE_VID_DOC_UDOST_REG_SPORT_OBEKT
								|| vidDoc == DocuConstants.CODE_ZNACHENIE_VID_DOC_UDOST_REG_TREN_KADAR) {	
							
							tmpLDocList.add(doc);
						}
					}
					
					if (!tmpLDocList.isEmpty()) { 
						
						for (Object[] tmpDoc : tmpLDocList) {
							
							int idDoc = ((BigInteger) tmpDoc[1]).intValue();
							int vidDoc = ((BigInteger) tmpDoc[2]).intValue();
							Date datDoc = ((Date)tmpDoc[5]);
							
							if (tmpLDocList.size() == 1) {
								
								JPA.getUtil().runWithClose(() -> setFilesList(new FilesDAO(getUserData()).selectByFileObjectDop(Integer.valueOf(idDoc), DocuConstants.CODE_ZNACHENIE_JOURNAL_DOC)));
								
								if(!getFilesList().isEmpty()) {								
									if(getFilesList().get(0).getOfficial() != null && getFilesList().get(0).getOfficial().equals(DocuConstants.CODE_ZNACHENIE_DA)) {
										download(getFilesList().get(0));	 						
									} 						
									break;
								} else {
									createUdostDokument(Integer.valueOf(idDoc), Integer.valueOf(vidDoc), false, false); 
									setShowDataForDoc(false);
									break;
								}	
								
							} else if (tmpLDocList.size() > 1) {
								
								Integer docID = actionSelectLastUD(idDoc, datDoc);
								
								if (Integer.valueOf(idDoc).equals(docID)) {
									
									JPA.getUtil().runWithClose(() -> setFilesList(new FilesDAO(getUserData()).selectByFileObjectDop(Integer.valueOf(idDoc), DocuConstants.CODE_ZNACHENIE_JOURNAL_DOC)));
									
									if(!getFilesList().isEmpty()) {								
										if(getFilesList().get(0).getOfficial() != null && getFilesList().get(0).getOfficial().equals(DocuConstants.CODE_ZNACHENIE_DA)) {
											download(getFilesList().get(0));	 						
										} 						
										break;
									} else {
										createUdostDokument(Integer.valueOf(idDoc), Integer.valueOf(vidDoc), false, false); 
										setShowDataForDoc(false);
										break;
									}
								}
							}
						}						
					}
				}
			}
			
		} catch (BaseException e) {
			LOGGER.error("Грешка при зареждане данните за удостоверителен документ! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
		}
		
	}
	
	public Integer actionSelectLastUD(int idDoc, Date dateDoc) {
		
		Integer docID = null; 
		
		if (!getDocsList().isEmpty()) { 
			
			for (Object[] doc : getDocsList()) {
				
				int idD = ((BigInteger) doc[1]).intValue();
				Date datD = ((Date)doc[5]);
				
				if (idDoc != idD && dateDoc.after(datD)) {
					docID = idDoc;
					break;
				}
			}
		}		
		
		return docID;
	}

	/**
	 * 
	 * Генерира нов уърдовски файл от шаблона и го записва към документа this.getDoc()
	 */
	public void createUdostDokument(Integer idDoc, Integer vidDoc, boolean isDublikat, boolean isCopy) {
		
		UdostDocumentCreator creator = new UdostDocumentCreator(getUserData(), getSystemData(), getLang(), isDublikat, isCopy);
		creator.setIdVpisvane(getReg().getId());
		creator.setIdObject(getReg().getIdObject());
		creator.setTypeObject(getReg().getTypeObject());
		creator.setIdDoc(idDoc);
		creator.setVidDoc(vidDoc);		
		
		try {
			// Новосъздаденият файл
			Files f = creator.createDokument();
			
			// добавен е файла към документа
			if (f != null) {					
				
				// извличане на файловете от таблица с файловете
				JPA.getUtil().runWithClose(() ->  setFilesList(new FilesDAO(getUserData()).selectByFileObjectDop(idDoc, DocuConstants.CODE_ZNACHENIE_JOURNAL_DOC)));
				
				download(f);
				
				setViewBtnUdostDoc(true);
				setViewCheckUdostDoc(false);
				setShowDataForDoc(false);
				
				actionCheckExistFileForUD();
			}
			
		} catch (DbErrorException e) {
			LOGGER.error("Грешка при търсене на шаблони към вида документ! ", e);
			JSFUtils.addErrorMessage(IndexUIbean.getMessageResourceString("ui_beanMessages", ERRDATABASEMSG), e);
		}
		catch (BaseException e) {
			LOGGER.error("Грешка при запис на удостоверителния документ! ", e);
			JSFUtils.addErrorMessage(IndexUIbean.getMessageResourceString("ui_beanMessages", ERRDATABASEMSG), e);
		}
		catch(Exception e) {
			LOGGER.error("Грешка при попълването на удостоверителен документ! ", e);
			JSFUtils.addErrorMessage(IndexUIbean.getMessageResourceString("ui_beanMessages", "general.formatExc"), e);
		}
		
	}
	
	/**
	 * Download selected file
	 *
	 * @param files
	 */	
	public void download(Files files) {
		
		try {
			
			if (files.getId() != null){
			
				FilesDAO dao = new FilesDAO(getUserData());					
			
				try {
					
					files = dao.findById(files.getId());	
				
				} finally {
					JPA.getUtil().closeConnection();
				}
				
				if(files.getContent() == null){					
					files.setContent(new byte[0]);
				}
			}

			FacesContext facesContext = FacesContext.getCurrentInstance();
			ExternalContext externalContext = facesContext.getExternalContext();

			HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();
			String agent = request.getHeader("user-agent");

			String codedfilename = "";

			if (null != agent && (-1 != agent.indexOf("MSIE") || -1 != agent.indexOf("Mozilla") && -1 != agent.indexOf("rv:11") || -1 != agent.indexOf("Edge"))) {
				codedfilename = URLEncoder.encode(files.getFilename(), "UTF8");
			} else if (null != agent && -1 != agent.indexOf("Mozilla")) {
				codedfilename = MimeUtility.encodeText(files.getFilename(), "UTF8", "B");
			} else {
				codedfilename = URLEncoder.encode(files.getFilename(), "UTF8");
			}

			externalContext.setResponseHeader("Content-Type", "application/x-download");
			externalContext.setResponseHeader("Content-Length", files.getContent().length + "");
			externalContext.setResponseHeader("Content-Disposition", "attachment;filename=\"" + codedfilename + "\"");
			externalContext.getResponseOutputStream().write(files.getContent());

			facesContext.responseComplete();

		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
	}
	
	public void actionCheckExistFileForUD() {
		
		if (getDoc().getDocVid().equals(DocuConstants.CODE_ZNACHENIE_VID_DOC_SPORT_LICENZ)
				|| getDoc().getDocVid().equals(DocuConstants.CODE_ZNACHENIE_VID_DOC_UDOST_REG_SPORT_FORMIROV) 
				|| getDoc().getDocVid().equals(DocuConstants.CODE_ZNACHENIE_VID_DOC_UDOST_REG_SPORT_OBEKT) 
				|| getDoc().getDocVid().equals(DocuConstants.CODE_ZNACHENIE_VID_DOC_UDOST_REG_TREN_KADAR)) {
			
			if (getFilesList() == null || getFilesList().size() == 0) {
				setViewBtnViewFile(true);
			} else {
				setViewBtnViewFile(false);
			}
		}
	}
	
	public void actionCreateUDWithSameRegNum(Integer idDoc, Integer vidDoc) {
		
		createUdostDokument(idDoc, vidDoc, false, true);

		JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, IndexUIbean.getMessageResourceString(UIBEANMESSAGES, SUCCESSAVEMSG));
		PrimeFaces.current().executeScript("scrollToErrors()");
		setShowDataForDoc(false);
	}
	
	public void actionCreateUDWithNewRegNum(Integer idDoc) {
		
		this.docCopy = new Doc();

		try {			

			JPA.getUtil().runWithClose(() -> this.docCopy = new DocDAO(getUserData()).findById(Integer.valueOf(idDoc)));
			
			this.docCopy.setId(null);
			this.docCopy.setRnDoc(null);
			this.docCopy.setDocDate(new Date());
			
			String prefix = getSystemData().decodeItemDopInfo(DocuConstants.CODE_CLASSIF_REGISTRI, this.docCopy.getRegisterId(), getLang(), new Date());
			
			this.docCopy.setRnPrefix(prefix);
			
			JPA.getUtil().runInTransaction(() -> { 		
				
				this.docCopy.setCountFiles(getFilesList() == null ? 0 : getFilesList().size());
				
				this.docCopy = new DocDAO(getUserData()).save(this.docCopy, this.createDelo, null, null, getSystemData());
				
				if (this.docCopy.getId() != null) {
					
					setRegDoc(new MMSVpisvaneDoc());		
					getRegDoc().setIdObject(getReg().getIdObject());
					getRegDoc().setTypeObject(getReg().getTypeObject()); 
					getRegDoc().setIdVpisvane(getReg().getId());
					
					getRegDoc().setIdDoc(this.docCopy.getId());
					
					setRegDoc(new MMSVpisvaneDocDAO(getUserData()).save(getRegDoc()));
				}
				
				setDocsList(new MMSVpisvaneDocDAO(getUserData()).findDocsList(getReg().getId()));
				
				 getReg().setRnDocLicenz(this.docCopy.getRnDoc());
				 getReg().setDateDocLicenz(this.docCopy.getDocDate());
				 getReg().setStatusVpisvane(getStatusVpisvane());
				 
				 setReg(new MMSVpisvaneDAO(getUserData()).save(getReg()));				 
				 
				 JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, IndexUIbean.getMessageResourceString(UIBEANMESSAGES, SUCCESSAVEMSG));
				 PrimeFaces.current().executeScript("scrollToErrors()");
				 setShowDataForDoc(false);
					
			});
		
		} catch (DbErrorException e) {
			LOGGER.error("Грешка при запис на нов УД със същия регистров номер! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(UIBEANMESSAGES, ERRDATABASEMSG));
			PrimeFaces.current().executeScript("scrollToErrors()");
		
		} catch (BaseException e) {
			LOGGER.error("Грешка при запис на нов УД със същия регистров номер! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(UIBEANMESSAGES, ERRDATABASEMSG), e.getMessage());
			PrimeFaces.current().executeScript("scrollToErrors()");
		}	
		
	}
	
	public void actionCreateUDDuplicate(Integer idDoc, Integer vidDoc) {
			
		createUdostDokument(idDoc, vidDoc, true, false);
		
		JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, IndexUIbean.getMessageResourceString(UIBEANMESSAGES, SUCCESSAVEMSG));
		PrimeFaces.current().executeScript("scrollToErrors()");
		setShowDataForDoc(false);
		
	}
	
	public void actionChangeNachinPoluchavane() {
		
		if (ValidationUtils.isNotBlank(getReg().getAddrMailPoluchavane())) {
			getReg().setAddrMailPoluchavane(null);			
		}
	}
	
	public void actionSendSSEV() {
		
	}
	
	public void closeSendMail() {
		PrimeFaces.current().executeScript("PF('wvSendMail#{cc.clientId}').hide();");
	}
	
	public void actionDeleteStatusZaiavlenie() {
		
		getReg().setStatusResultZaiavlenie(DocuConstants.CODE_ZNACHENIE_STATUS_ZAIAVLENIE_V_RAZGLEJDANE);
		setChangeStatusZaiav(true); 
		getReg().setDateStatusZaiavlenie(getCurrentDate());
		getReg().setReasonResult(null);
		getReg().setReasonResultText(null);
		
		try {

			JPA.getUtil().runInTransaction(() -> {

				if (ValidationUtils.isNotBlank(getReg().getRnDocResult()) && getReg().getDateDocResult() != null) {
					
					List<Integer> idDocsList = new DocDAO(getUserData()).findDocIdList(getReg().getRnDocResult(), getReg().getDateDocResult());

					if (!idDocsList.isEmpty()) {

						Integer docId = idDocsList.get(0);

						if (docId != null) {
							
							for (Object[] doc : getDocsList()) {
							
								int idVpisDoc = ((BigInteger) doc[0]).intValue();
								int idDoc = ((BigInteger) doc[1]).intValue();
								
								if(docId.equals(idDoc)) {
									// delete документа/заповедта
									new MMSVpisvaneDocDAO(getUserData()).deleteById(idVpisDoc);
								}								
							}
						}
					}

					getReg().setRnDocResult(null);
					getReg().setDateDocResult(null);
				}
				
				setDocsList(new MMSVpisvaneDocDAO(getUserData()).findDocsList(getReg().getId()));

			});
			
			actionDeleteStatusVpisvane(true); 
			
			// запис на вписване - с всички условия при смяна на статус
			actionSave();

		} catch (BaseException e) {
			LOGGER.error("Грешка при изтриване на решението! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(UIBEANMESSAGES, ERRDATABASEMSG), e.getMessage());
		}
				
	}
	
	public void actionDeleteStatusVpisvane(boolean fromDelStZaiav) {
		
		if (fromDelStZaiav) { 
			getReg().setStatusVpisvane(null);
			getReg().setDateStatusVpisvane(null); 
		
		} else {		
			getReg().setStatusVpisvane(DocuConstants.CODE_ZNACHENIE_STATUS_REG_VPISAN);
			setChangeStatusVpis(true); 
			getReg().setDateStatusVpisvane(new Date()); 
			getReg().setReasonVpisvane(null);
			getReg().setReasonVpisvaneText(null);
		}
		
		try {

			JPA.getUtil().runInTransaction(() -> {

				if (ValidationUtils.isNotBlank(getReg().getRnDocVpisvane()) && getReg().getDateDocVpisvane() != null) {
					
					List<Integer> idDocsList = new DocDAO(getUserData()).findDocIdList(getReg().getRnDocVpisvane(), getReg().getDateDocVpisvane());

					if (!idDocsList.isEmpty()) {

						Integer docId = idDocsList.get(0);

						if (docId != null) {
							
							for (Object[] doc : getDocsList()) {
							
								int idVpisDoc = ((BigInteger) doc[0]).intValue();
								int idDoc = ((BigInteger) doc[1]).intValue();
								
								if(docId.equals(idDoc)) {
									// delete документа/заповедта
									new MMSVpisvaneDocDAO(getUserData()).deleteById(idVpisDoc);
								}								
							}
						}
					}

					getReg().setRnDocVpisvane(null);
					getReg().setDateDocVpisvane(null);		
				}
				
				setDocsList(new MMSVpisvaneDocDAO(getUserData()).findDocsList(getReg().getId()));

			});
			
			if (!fromDelStZaiav) { 
				// запис на вписване - с всички условия при смяна на статус
				actionSave();
			}			

		} catch (BaseException e) {
			LOGGER.error("Грешка при изтриване на статус! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(UIBEANMESSAGES, ERRDATABASEMSG), e.getMessage());
		}
		
	}
	
	public void actionCheckExistZapByRegNumAndData() {
		
		try {
				
			if (!getDocsList().isEmpty()) {
				
				for (Object[] doc : getDocsList()) {

					int vidDoc = ((BigInteger) doc[2]).intValue();
					String rnDoc = (String) doc[4];
					String datDoc = SearchUtils.asString(doc[5]);

					SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.S");
					Date date = dt.parse(datDoc);

					if (vidDoc == DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAPOVED_VPISVANE
							|| vidDoc == DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAPOVED_OTKAZANO_VPISVANE
							|| vidDoc == DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAPOVED_OSTAVIANE_BEZ_POSLEDSTVIA
							|| vidDoc == DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAPOVED_PREKRATENO_PROIZVODSTVO) {

						if (ValidationUtils.isNotBlank(getReg().getRnDocResult()) && !getReg().getRnDocResult().trim().equals(rnDoc) 
								&& getReg().getDateDocResult() != null  && !getReg().getDateDocResult().equals(date)) {
							JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(BEANMESSAGES, "compReg.messForDiffRegNumZap"));
							PrimeFaces.current().executeScript("scrollToErrors()");

							break;
						}
					}

					if (vidDoc == DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAPOVED_ZALICHAVANE
							|| vidDoc == DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAPOVED_PREKRATIAVANE
							|| vidDoc == DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAPOVED_OTNEMANE) {

						if (ValidationUtils.isNotBlank(getReg().getRnDocVpisvane()) && !getReg().getRnDocVpisvane().trim().equals(rnDoc) 
								&& getReg().getDateDocVpisvane() != null  && !getReg().getDateDocVpisvane().equals(date)) {						
							JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(BEANMESSAGES, "compReg.messForDiffRegNumZap"));
							PrimeFaces.current().executeScript("scrollToErrors()");

							break;
						}
					}
				}
			}
		
		} catch (ParseException e) {
			LOGGER.error("Грешка при конвертиране на стринг в дата", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
		}	
		
	}
	
	public void actionCheckRegNumForDoc(String rnDoc) {
		
		if (ValidationUtils.isNotBlank(rnDoc) && getDoc().getDocVid() != null
				&& (getDoc().getDocVid().intValue() == DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAPOVED_VPISVANE
				|| getDoc().getDocVid().intValue() == DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAPOVED_OTKAZANO_VPISVANE
				|| getDoc().getDocVid().intValue() == DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAPOVED_OSTAVIANE_BEZ_POSLEDSTVIA
				|| getDoc().getDocVid().intValue() == DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAPOVED_PREKRATENO_PROIZVODSTVO
				|| getDoc().getDocVid().intValue() == DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAPOVED_ZALICHAVANE
				|| getDoc().getDocVid().intValue() == DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAPOVED_PREKRATIAVANE
				|| getDoc().getDocVid().intValue() == DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAPOVED_OTNEMANE) ) {
			
			actionSetDocSettings(getDoc().getDocVid());
			
			try {
				
				String prefix = getSystemData().decodeItemDopInfo(DocuConstants.CODE_CLASSIF_REGISTRI, getDoc().getRegisterId(), getLang(), new Date());
				
				getDoc().setRnPrefix(prefix);
				getDoc().setRnDoc(getDoc().getRnPrefix() + "-"+ rnDoc);
				
				String errorRnDoc = new DocDAO(getUserData()).validateRnDoc(getDoc(), null, getSystemData(), false, false);
				
				if (errorRnDoc != null) {
					throw new ObjectInUseException(errorRnDoc);
				
				} else {
					
					if (ValidationUtils.isNotBlank(getReg().getRnDocResult()) && !getReg().getRnDocResult().trim().equals(getDoc().getRnDoc()) 
							&& getReg().getDateDocResult() != null  && !getReg().getDateDocResult().equals(getDoc().getDateReg())) {
						JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(BEANMESSAGES, "compReg.messForDiffRegNumZap"));
						PrimeFaces.current().executeScript("scrollToErrors()");
						return;
					}
					
				}

			} catch (ObjectInUseException e) {
				LOGGER.error("Грешка при зареждане характеристики на документ! ObjectInUseException = {}", e.getMessage());
				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
				PrimeFaces.current().executeScript("scrollToErrors()");
			
			} catch (DbErrorException e) {
				LOGGER.error("Грешка при зареждане характеристики на документ! ", e);
				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(UIBEANMESSAGES, ERRDATABASEMSG));
				PrimeFaces.current().executeScript("scrollToErrors()");
			} finally {
				JPA.getUtil().closeConnection();
			}
			
		}
	}
	
	public MMSVpisvane getReg() {
		return (MMSVpisvane) getStateHelper().eval(PropertyKeys.REG, null);
	}

	public void setReg(MMSVpisvane reg) {
		getStateHelper().put(PropertyKeys.REG, reg);
	}
	
	public MMSVpisvaneDoc getRegDoc() {
		return (MMSVpisvaneDoc) getStateHelper().eval(PropertyKeys.REGDOC, null);
	}

	public void setRegDoc(MMSVpisvaneDoc regDoc) {
		getStateHelper().put(PropertyKeys.REGDOC, regDoc);
	}
	
	public Doc getDoc() {
		return (Doc) getStateHelper().eval(PropertyKeys.DOC, null);
	}

	public void setDoc(Doc Doc) {
		getStateHelper().put(PropertyKeys.DOC, Doc);
	}	
	
	@SuppressWarnings("unchecked")
	public List<Files> getFilesList() {
		return (List<Files>) getStateHelper().eval(PropertyKeys.FILESLIST, null);
	}

	public void setFilesList(List<Files> objDocsList) {
		getStateHelper().put(PropertyKeys.FILESLIST, objDocsList);
	}
	
	@SuppressWarnings("unchecked")
	public List<Object[]> getDocsList() {
		return (List<Object[]>) getStateHelper().eval(PropertyKeys.DOCSLIST, null);
	}

	public void setDocsList(List<Object[]> docsList) {
		getStateHelper().put(PropertyKeys.DOCSLIST, docsList);
	}
	
	public boolean isShowMe() {
		return (Boolean) getStateHelper().eval(PropertyKeys.SHOWME, false);
	}
	
	public void setShowMe(boolean showMe) {
		getStateHelper().put(PropertyKeys.SHOWME, showMe);
	}	
	
	public boolean isShowDataForDoc() {
		return (Boolean) getStateHelper().eval(PropertyKeys.SHOWDATAFORDOC, false);
	}

	public void setShowDataForDoc(boolean showDataForDoc) {
		getStateHelper().put(PropertyKeys.SHOWDATAFORDOC, showDataForDoc);
	}
	
	public boolean isChangeStatusZaiav() {
		return (Boolean) getStateHelper().eval(PropertyKeys.CHANGESTATUSZAIAV, false);
	}

	public void setChangeStatusZaiav(boolean changeStatusZaiav) {
		getStateHelper().put(PropertyKeys.CHANGESTATUSZAIAV, changeStatusZaiav);
	}
	
	public boolean isChangeStatusVpis() {
		return (Boolean) getStateHelper().eval(PropertyKeys.CHANGESTATUSVPIS, false);
	}

	public void setChangeStatusVpis(boolean changeStatusVpis) {
		getStateHelper().put(PropertyKeys.CHANGESTATUSVPIS, changeStatusVpis);
	}
	
	public boolean isViewBtnUdostDoc() {
		return (Boolean) getStateHelper().eval(PropertyKeys.VIEWBTNUDOSTDOC, false);
	}

	public void setViewBtnUdostDoc(boolean viewBtnUdostDoc) {
		getStateHelper().put(PropertyKeys.VIEWBTNUDOSTDOC, viewBtnUdostDoc);
	}
	
	public boolean isCheckUdostDoc() {
		return (Boolean) getStateHelper().eval(PropertyKeys.CHECKUDOSTDOC, false);
	}

	public void setCheckUdostDoc(boolean checkUdostDoc) {
		getStateHelper().put(PropertyKeys.CHECKUDOSTDOC, checkUdostDoc);
	}
	
	public boolean isViewCheckUdostDoc() {
		return (Boolean) getStateHelper().eval(PropertyKeys.VIEWCHECKUDOSTDOC, false);
	}

	public void setViewCheckUdostDoc(boolean viewcheckUdostDoc) {
		getStateHelper().put(PropertyKeys.VIEWCHECKUDOSTDOC, viewcheckUdostDoc);
	}
	
	public boolean isViewBtnViewFile() {
		return (Boolean) getStateHelper().eval(PropertyKeys.VIEWBTNNEWFILE, false);
	}

	public void setViewBtnViewFile(boolean viewBtnViewFile) {
		getStateHelper().put(PropertyKeys.VIEWBTNNEWFILE, viewBtnViewFile);
	}
	
	@SuppressWarnings("unchecked")
	public List<SelectItem> getResheniaList() {
		return (List<SelectItem>) getStateHelper().eval(PropertyKeys.RESHENIALIST, null);
	}

	public void setResheniaList(List<SelectItem> resheniaList) {
		getStateHelper().put(PropertyKeys.RESHENIALIST, resheniaList);
	}

	@SuppressWarnings("unchecked")
	public List<SelectItem> getStatusiList() {
		return (List<SelectItem>) getStateHelper().eval(PropertyKeys.STATUSILIST, null);
	}

	public void setStatusiList(List<SelectItem> statusiList) {
		getStateHelper().put(PropertyKeys.STATUSILIST, statusiList);
	}
	
	public Integer getSastoianieZaiav() {
		return (Integer) getStateHelper().eval(PropertyKeys.SASTOIANIEZAIAV, null);
	}

	public void setSastoianieZaiav(Integer sastoianieZaiav) {
		getStateHelper().put(PropertyKeys.SASTOIANIEZAIAV, sastoianieZaiav);
	}
	
	public Integer getStatusVpisvane() {
		return (Integer) getStateHelper().eval(PropertyKeys.STATUSVPISVANE, null);
	}

	public void setStatusVpisvane(Integer statusVpisvane) {
		getStateHelper().put(PropertyKeys.STATUSVPISVANE, statusVpisvane);
	}
	
	public boolean isAvtomNo() {
		return (Boolean) getStateHelper().eval(PropertyKeys.AVTONOMNO, false);
	}

	public void setAvtomNo(boolean avtomNo) {
		getStateHelper().put(PropertyKeys.AVTONOMNO, avtomNo);
	}

	public boolean isAvtomNoDisabled() {
		return (Boolean) getStateHelper().eval(PropertyKeys.AVTONOMNODISABLED, false);
	}

	public void setAvtomNoDisabled(boolean avtomNoDisabled) {
		getStateHelper().put(PropertyKeys.AVTONOMNODISABLED, avtomNoDisabled);
	}
	
	@SuppressWarnings("unchecked")
	public List<SelectItem> getReasonZaiavleniaList() {
		return (List<SelectItem>) getStateHelper().eval(PropertyKeys.REASONZAIAVLENIALIST, null);
	}

	public void setReasonZaiavleniaList(List<SelectItem> reasonZaiavleniaList) {
		getStateHelper().put(PropertyKeys.REASONZAIAVLENIALIST, reasonZaiavleniaList);
	}

	@SuppressWarnings("unchecked")
	public List<SelectItem> getReasonVpisvaneList() {
		return (List<SelectItem>) getStateHelper().eval(PropertyKeys.REASONVPISVANELIST, null);
	}

	public void setReasonVpisvaneList(List<SelectItem> reasonVpisvaneList) {
		getStateHelper().put(PropertyKeys.REASONVPISVANELIST, reasonVpisvaneList);
	}
	
	@SuppressWarnings("unchecked")
	public List<DocReferent> getReferentsSigned() {
		return (List<DocReferent>) getStateHelper().eval(PropertyKeys.REFERENTSSIGNED, null);
	}
	
	public void setReferentsSigned(List<DocReferent> referentsSigned) {
		getStateHelper().put(PropertyKeys.REFERENTSSIGNED, referentsSigned);
	}
	
	public boolean isReadOnlyDataLicenz() {
		return (Boolean) getStateHelper().eval(PropertyKeys.RERADONLYDATALICENZ, false);
	}

	public void setReadOnlyDataLicenz(boolean readOnlyDataLicenz) {
		getStateHelper().put(PropertyKeys.RERADONLYDATALICENZ, readOnlyDataLicenz);
	}	
	
	public String getErrMsg() {
		return errMsg;
	}

	public void setErrMsg(String errMsg) {
		this.errMsg = errMsg;
	}	
	
	/** @return the userData */
	private UserData getUserData() {
		if (this.userData == null) {
			this.userData = (UserData) JSFUtils.getManagedBean("userData");
		}
		return this.userData;
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
	
	public TimeZone getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(TimeZone timeZone) {
		this.timeZone = timeZone;
	}
	
	/** @return */
	public Date getCurrentDate() {
		return getDateClassif();
	}
	
	/** @return */
	public Integer getLang() {
		return getUserData().getCurrentLang();
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

	public Integer getVidObject() {
		return vidObject;
	}

	public void setVidObject(Integer vidObject) {
		this.vidObject = vidObject;
	}

	public MMSVpisvane getTmpReg() {
		return tmpReg;
	}

	public void setTmpReg(MMSVpisvane tmpReg) {
		this.tmpReg = tmpReg;
	}

	public String getTypeObj() {
		return typeObj;
	}

	public void setTypeObj(String typeObj) {
		this.typeObj = typeObj;
	}

	public boolean isCreateDelo() {
		return createDelo;
	}

	public void setCreateDelo(boolean createDelo) {
		this.createDelo = createDelo;
	}

	public LazyDataModelSQL2Array getEtapExeList() {
		return etapExeList;
	}

	public void setEtapExeList(LazyDataModelSQL2Array etapExeList) {
		this.etapExeList = etapExeList;
	}
		
	public List<MMSVpisvane> getRegList() {
		return regList;
	}

	public void setRegList(List<MMSVpisvane> regList) {
		this.regList = regList;
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

	public int getUnlockObj() {
		return unlockObj;
	}

	public void setUnlockObj(int unlockObj) {
		this.unlockObj = unlockObj;
	}
	
/******************************************************* LOCK & UNLOCK *******************************************************/	
	
	/**
	 * @return the docCopy
	 */
	public Doc getDocCopy() {
		return docCopy;
	}

	/**
	 * @param docCopy the docCopy to set
	 */
	public void setDocCopy(Doc docCopy) {
		this.docCopy = docCopy;
	}

	/**
	 * Проверка за заключен обект
	 * @param idObj
	 * @return
	 */
	private boolean checkForLock(Integer idObj) {
		boolean res = true;
		LockObjectDAO daoL = new LockObjectDAO();		
		
		try { 
			
			Object[] obj = daoL.check(getUserData().getUserId(), getReg().getTypeObject(), idObj);
			
			if (obj != null) {
				 res = false;
				 String msg = getSystemData().decodeItem(DocuConstants.CODE_CLASSIF_ADMIN_STR, Integer.valueOf(obj[0].toString()), getUserData().getCurrentLang(), new Date())   
						       + " / " + DateUtils.printDate((Date)obj[1]);
				 
				 JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_WARN, IndexUIbean.getMessageResourceString(LABELS, "compVpisvane.lock"), msg);
			}
		
		} catch (DbErrorException e) {
			
			LOGGER.error("Грешка при проверка за заключена процедура! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(UIBEANMESSAGES, ERRDATABASEMSG), e.getMessage());
		}
		return res;
	}
	
	/**
	 * Заключване на процедура, като преди това отключва всички обекти, заключени от потребителя
	 * @param idObj
	 */
	public void lock(Integer idObj) {	
		LOGGER.info("lockProc! = {}", ((UserData) getUserData()).getPreviousPage());		
		LockObjectDAO daoL = new LockObjectDAO();		
		
		try { 
			
			JPA.getUtil().runInTransaction(() ->  daoL.lock(getUserData().getUserId(), getReg().getTypeObject(), idObj, null));
		
		} catch (DbErrorException e) {
			LOGGER.error("Грешка при заключване на процедура! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(UIBEANMESSAGES, ERRDATABASEMSG), e.getMessage());
		
		} catch (BaseException e) {
			LOGGER.error("Грешка при заключване на процедура! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(UIBEANMESSAGES, ERRDATABASEMSG), e.getMessage());
		}			
	}

	
	/**
	 * при излизане от страницата - отключва обекта и да го освобождава за актуализация от друг потребител
	 */
	@PreDestroy
	public void unlock(){		
		
		if (!getDocsList().isEmpty()) { 
			
			Object[] statusiFromDB = new MMSVpisvaneDAO(getUserData()).findStatusiByIdReg(getReg().getId());
			
			int reshenie = ((BigInteger)statusiFromDB[0]).intValue();
			int statusVpisvane = ((BigInteger)statusiFromDB[1]).intValue();
				
			boolean flagSave = false;
			int vidDoc = ((BigInteger) getDocsList().get(0)[2]).intValue();
    
			if (reshenie != DocuConstants.CODE_ZNACHENIE_STATUS_ZAIAVLENIE_VPISAN && vidDoc == DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAPOVED_VPISVANE) {
				getReg().setStatusResultZaiavlenie(DocuConstants.CODE_ZNACHENIE_STATUS_ZAIAVLENIE_VPISAN);
				flagSave = true;
			
			} else if (reshenie != DocuConstants.CODE_ZNACHENIE_STATUS_ZAIAVLENIE_OTKAZANO_VPISVANE && vidDoc == DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAPOVED_OTKAZANO_VPISVANE) {
				getReg().setStatusResultZaiavlenie( DocuConstants.CODE_ZNACHENIE_STATUS_ZAIAVLENIE_OTKAZANO_VPISVANE);
				flagSave = true;
			
			} else if (reshenie != DocuConstants.CODE_ZNACHENIE_STATUS_ZAIAVLENIE_OSTAVENO_BEZ_POSLEDSTVIE && vidDoc == DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAPOVED_OSTAVIANE_BEZ_POSLEDSTVIA) {
				getReg().setStatusResultZaiavlenie(DocuConstants.CODE_ZNACHENIE_STATUS_ZAIAVLENIE_OSTAVENO_BEZ_POSLEDSTVIE);
				flagSave = true;
			
			} else if (reshenie != DocuConstants.CODE_ZNACHENIE_STATUS_ZAIAVLENIE_PREKRATENO_PROIZVODSTVO && vidDoc == DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAPOVED_PREKRATENO_PROIZVODSTVO) {
				getReg().setStatusResultZaiavlenie(DocuConstants.CODE_ZNACHENIE_STATUS_ZAIAVLENIE_PREKRATENO_PROIZVODSTVO);
				flagSave = true;
			
			} else if (statusVpisvane != DocuConstants.CODE_ZNACHENIE_STATUS_REG_ZALICHENO_VPISVANE && vidDoc == DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAPOVED_ZALICHAVANE) {
				getReg().setStatusVpisvane(DocuConstants.CODE_ZNACHENIE_STATUS_REG_ZALICHENO_VPISVANE);
				flagSave = true;

			} else if (statusVpisvane != DocuConstants.CODE_ZNACHENIE_STATUS_REG_PREKRATENO_VPISVANE && vidDoc == DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAPOVED_PREKRATIAVANE) {
				getReg().setStatusVpisvane(DocuConstants.CODE_ZNACHENIE_STATUS_REG_PREKRATENO_VPISVANE);
				flagSave = true;

			} else if (statusVpisvane != DocuConstants.CODE_ZNACHENIE_STATUS_REG_OTNETO_VPISVANE && vidDoc == DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAPOVED_OTNEMANE) {
				getReg().setStatusVpisvane(DocuConstants.CODE_ZNACHENIE_STATUS_REG_OTNETO_VPISVANE);
				flagSave = true;
			}
			
			if (flagSave) {
				
				actionSave();
			}			
		}
		
		if (!((UserData) getUserData()).isReloadPage()) {
        	LOGGER.info("unlockData! = {}", ((UserData) getUserData()).getPreviousPage() );        	
			
			if(this.unlockObj != 1 ){ 
	        	unlockAll(true);
	        	((UserData) getUserData()).setPreviousPage(null);
			}
        }          
        ((UserData) getUserData()).setReloadPage(false); 
	}
	
	
	/**
	 * отключва всички обекти на потребителя - при излизане от страницата или при натискане на бутон "Нов"
	 */
	private void unlockAll(boolean all) {
		LOGGER.info("unlockProc! = {}", ((UserData) getUserData()).getPreviousPage());
		LockObjectDAO daoL = new LockObjectDAO();		
		
		try { 
			if (all) {
				JPA.getUtil().runInTransaction(() ->  daoL.unlock(getUserData().getUserId()));
			} 
		
		} catch (DbErrorException e) {
			LOGGER.error("Грешка при отключване на процедура! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(UIBEANMESSAGES, ERRDATABASEMSG), e.getMessage());
		
		} catch (BaseException e) {
			LOGGER.error("Грешка при отключване на процедура! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(UIBEANMESSAGES, ERRDATABASEMSG), e.getMessage());
		}
	}
	
	/**************************************************** END LOCK & UNLOCK ****************************************************/
	
	transient Comparator<SelectItem> compatator = new Comparator<SelectItem>() {
		public int compare(SelectItem s1, SelectItem s2) {
			return (s1.getLabel().toUpperCase().compareTo(s2.getLabel().toUpperCase()));
		}
	};
}
