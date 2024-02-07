package com.ib.docu.beans;


import java.io.IOException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import javax.activation.DataSource;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.faces.view.facelets.FaceletContext;
import javax.inject.Named;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.util.ByteArrayDataSource;

import org.omnifaces.cdi.ViewScoped;
import org.primefaces.PrimeFaces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.docu.db.dao.DocDAO;
import com.ib.docu.db.dao.EgovMessagesDAO;
import com.ib.docu.db.dao.LockObjectDAO;
import com.ib.docu.db.dao.MMSChlenstvoDAO;
import com.ib.docu.db.dao.MMSCoachesDAO;
import com.ib.docu.db.dao.MMSVpisvaneDAO;
import com.ib.docu.db.dao.MMSVpisvaneDocDAO;
import com.ib.docu.db.dao.MMSsportFormirovanieDAO;
import com.ib.docu.db.dao.MMSsportObedinenieDAO;
import com.ib.docu.db.dao.ReferentDAO;
import com.ib.docu.db.dto.Doc;
import com.ib.docu.db.dto.EgovMessages;
import com.ib.docu.db.dto.EgovMessagesCoresp;
import com.ib.docu.db.dto.EgovMessagesFiles;
import com.ib.docu.db.dto.MMSChlenstvo;
import com.ib.docu.db.dto.MMSCoaches;
import com.ib.docu.db.dto.MMSSportnoObedinenie;
import com.ib.docu.db.dto.MMSVidSportSF;
import com.ib.docu.db.dto.MMSVpisvane;
import com.ib.docu.db.dto.MMSVpisvaneDoc;
import com.ib.docu.db.dto.MMSsportFormirovanie;
import com.ib.docu.db.dto.Referent;
import com.ib.docu.system.DocuClassifAdapter;
import com.ib.docu.system.DocuConstants;
import com.ib.docu.system.SystemData;
import com.ib.docu.system.UserData;
import com.ib.docu.utils.ParsePdfZaqvlenie;
import com.ib.indexui.navigation.NavigationDataHolder;
import com.ib.indexui.pagination.LazyDataModelSQL2Array;
import com.ib.indexui.system.Constants;
import com.ib.indexui.system.IndexUIbean;
import com.ib.indexui.utils.JSFUtils;
import com.ib.system.db.JPA;
import com.ib.system.db.dao.FilesDAO;
import com.ib.system.db.dto.Files;
import com.ib.system.exceptions.BaseException;
import com.ib.system.exceptions.DbErrorException;
import com.ib.system.exceptions.InvalidParameterException;
import com.ib.system.mail.Mailer;
import com.ib.system.mail.Mailer.Content;
import com.ib.system.model.SysAttrSpec;
import com.ib.system.utils.DateUtils;
import com.ib.system.utils.SearchUtils;
import com.ib.system.utils.ValidationUtils;

@Named(value = "mmsSF")
@ViewScoped
public class MMSSportFirmirovanieEdit extends IndexUIbean {
	
	private static final long serialVersionUID = 5748270252205977719L;
	private static final Logger LOGGER = LoggerFactory.getLogger(MMSSportFirmirovanieEdit.class);
	private MMSsportFormirovanieDAO mmsSFDAO;
	private MMSsportObedinenieDAO mmsSOdao;
	private ReferentDAO referentDAO;
	private MMSsportFormirovanie mmsSportFormirovanie;
	private Referent referent = new Referent();
	private int viewOnly = DocuConstants.CODE_ZNACHENIE_NE;
	private static final String	UIBEANMESSAGES	= "ui_beanMessages";
	private Doc doc;
	private List<Integer> selectedVidSport;
	private String selectedVidSportTxt;
	private LazyDataModelSQL2Array obedineniaList;
	private LazyDataModelSQL2Array docsList;
	//private LazyDataModelSQL2Array regsList;
	private List<MMSVpisvane> regsList = new ArrayList<MMSVpisvane>();
	private List<MMSChlenstvo> mmsChelnstvoListDeleted = new ArrayList<MMSChlenstvo>();
	private StringBuilder idsSOexclude = new StringBuilder();
	private Integer codeRefCorresp;	
	private Date decodeDate2;
	private String dopInfoAdres;
	private String  txtCorresp;
	private SysAttrSpec sasEik = new SysAttrSpec();
	private SysAttrSpec sasRegNom = new SysAttrSpec();
	private SysAttrSpec sasVidSport = new SysAttrSpec();
	private SysAttrSpec sasVid = new SysAttrSpec();
	private SysAttrSpec sasPredstavitelstvo = new SysAttrSpec();
	private SysAttrSpec sasPredsedatel = new SysAttrSpec();
	private SysAttrSpec sasHighSchool = new SysAttrSpec();
	private SysAttrSpec sasNotes = new SysAttrSpec();
	private SysAttrSpec sasStatus = new SysAttrSpec();
	private SysAttrSpec sasDateStatus = new SysAttrSpec();
	private UserData ud;
	private String idObedinenie = null;
	private String messageFromObedinenie = "";
	private MMSVpisvane lastVpisvane = new MMSVpisvane();
	private String regNomObed = "";
	static Properties props=new Properties();
	private static Integer ID_REGISTRATURE = 1;//1;
	private static final String MAILBOX="DEFAULT";//"DEFAULT";
	private boolean willShowPanels = true;
	private boolean willShowSaveBtns = true;
	private Integer codeRefCorrespChlenstvo;	
	private Date decodeDate2Chlenstvo;
	private String  txtCorrespChlenstvo;
	private Object[] tmpCorespChlenstvo = null;	
	
	private String mailText;
	private String subject;
	private boolean willShowMailModal = true;
	private Date dataDocDate = null;
	private List<EgovMessagesFiles> egovMessFilesList;
	
	private EgovMessages egovMess;
	private List<EgovMessagesFiles> egovFilesList;
	private List<SelectItem> msgStatusList = new ArrayList<>();
	private String reasonOtkaz;
	private List<EgovMessagesCoresp> emcorespList;
	private ArrayList<DataSource> attachedBytes = new ArrayList<DataSource>();
	private String fileName = "";
	private ArrayList<Files> uploadFilesList = new ArrayList<Files>();
	
	
	
	@PostConstruct
	public void initData() {
		try {
			if (JSFUtils.getRequestParameter("idObed")!=null) {
				NavigationDataHolder holder=(NavigationDataHolder) JSFUtils.getManagedBean("navigationSessionDataHolder");
				holder.getPageList().pop();
			}
			if(JSFUtils.getRequestParameter("idObedinenie") != null){
				idObedinenie = JSFUtils.getRequestParameter("idObedinenie");
			}
			ud = getUserData(UserData.class);
			mmsSFDAO = new MMSsportFormirovanieDAO(MMSsportFormirovanie.class, ud);
			mmsSOdao = new MMSsportObedinenieDAO(MMSSportnoObedinenie.class, ud);
			referentDAO = new ReferentDAO(ud);
			props = getSystemData(SystemData.class).getMailProp(ID_REGISTRATURE, MAILBOX);
			actionNew(true);
			loadAttrSpecifications();
			String idMMSFormirovanie = JSFUtils.getRequestParameter("idObj");
			if(idMMSFormirovanie == null || idMMSFormirovanie.trim().isEmpty()) {
				
				if (JSFUtils.getRequestParameter("ccevID") != null  && !"".equals(JSFUtils.getRequestParameter("ccevID"))) {
					
					//Тези параметри ми трябват, за да мога да регистрирам документ в нашата система с техните данни 
					//  ще ги вземаме директно от обекта!!!
					this.idSSev = Integer.valueOf(JSFUtils.getRequestParameter("ccevID"));
					lockDelo(idSSev);					
					actionLoadEgovMessage();
					
					this.vidDoc = Integer.valueOf(egovMess.getDocVid());	
					this.regNom = egovMess.getDocRn();
					this.setDataDocDate(egovMess.getDocDate());
					this.otnosno = egovMess.getDocSubject();
					if(emcoresp != null) {
						this.egn = emcoresp.getEgn();
						this.eik = emcoresp.getBulstat();	
					}
					
					
					if (getDataDoc()!=null) {
							//setDateRNV(this.getDataDoc());
					}

					//PDF PARSE.
					try {
						mmsSportFormirovanie = new ParsePdfZaqvlenie().parseFormirovanie((SystemData) getSystemData(), ud, getCurrentLang(), egovMess, egovFilesList);
						if (mmsSportFormirovanie.getIdObject()==null) {
							this.referent.setContactEmail(mmsSportFormirovanie.getMailLice());
						}
						if(mmsSportFormirovanie.getIdObject() != null) {
							referent = referentDAO.findByCodeRef(mmsSportFormirovanie.getIdObject());
							setCodeRefCorresp(referent.getCode());
						}
						if (mmsSportFormirovanie.getParseMessages().size()>0) {
							for (int i = 0; i < mmsSportFormirovanie.getParseMessages().size(); i++) {
								JSFUtils.addErrorMessage(mmsSportFormirovanie.getParseMessages().get(i));
							}
							if(mmsSportFormirovanie.getId() != null)
								loadallFileds();
						}else {
							loadallFileds();
						}
						if(mmsSportFormirovanie.getVidSportList() != null &&  !mmsSportFormirovanie.getVidSportList().isEmpty())
							getVidSportAStrings(mmsSportFormirovanie.getVidSportList());
					} catch (Exception e) {
						JSFUtils.addErrorMessage("Грешка при автоматична обработка на пдф!", e);
						LOGGER.error(e.getMessage(), e);
					}
					
					if(mmsSportFormirovanie.getId() == null) {
						mmsSportFormirovanie.setStatus(DocuConstants.CODE_ZNACHENIE_STATUS_OBEKT_V_RAZGLEJDANE);
						mmsSportFormirovanie.setDateStatus(new Date());
					}
					return;
				
				} 
				
				actionNew(true);
				//return;
			}else {
				mmsSportFormirovanie = JPA.getUtil().getEntityManager().find(MMSsportFormirovanie.class, Integer.valueOf(idMMSFormirovanie));
				loadallFileds();
			}
			if(referent.getContactEmail() == null || referent.getContactEmail().trim().isEmpty() )
				willShowMailModal = false;
			else
				willShowMailModal = true;
			
			
			FaceletContext faceletContext = (FaceletContext) FacesContext.getCurrentInstance().getAttributes().get(FaceletContext.FACELET_CONTEXT_KEY);
			String param3 = (String) faceletContext.getAttribute("viewOnly"); // 2 - актуализация 1 - разглеждане
			Integer isView=0;
			if(!SearchUtils.isEmpty(param3)) {
				isView = Integer.valueOf(param3);
			}
			boolean fLockOk=true;
			if(isView == 2 && mmsSportFormirovanie.getId()!=null) { 
				// проверка за заключен документ
				fLockOk = checkForLock(mmsSportFormirovanie.getId());
				if (fLockOk) {
					lockDelo(mmsSportFormirovanie.getId());
				// отключване на всички обекти за потребителя(userId) и заключване на док., за да не може да се актуализира от друг
				}				
			}
		} catch(IllegalArgumentException | DbErrorException  e) {
			LOGGER.error("Грешка при ЧЕТЕНЕ НА MMSsportFormirovanie id=  " + JSFUtils.getRequestParameter("idObj"), e);
		} finally {
			JPA.getUtil().closeConnection();			
		}
	}
	
	private void loadAttrSpecifications() {
		
		try {
			sasEik = getSystemData().getModel().getAttrSpec("nfl_eik", "sport_formirovanie", getCurrentLang(), null);
			sasRegNom = getSystemData().getModel().getAttrSpec("reg_nomer", "sport_formirovanie", getCurrentLang(), null);
			sasVidSport = getSystemData().getModel().getAttrSpec("vid_sport", "vid_sport_formirovanie", getCurrentLang(), null);
			sasVid = getSystemData().getModel().getAttrSpec("vid", "sport_formirovanie", getCurrentLang(), null);
			sasPredstavitelstvo = getSystemData().getModel().getAttrSpec("predstavitelstvo", "sport_formirovanie", getCurrentLang(), null);
			sasPredsedatel = getSystemData().getModel().getAttrSpec("predsedatel", "sport_formirovanie", getCurrentLang(), null);
			sasHighSchool = getSystemData().getModel().getAttrSpec("school_name", "sport_formirovanie", getCurrentLang(), null);
			sasNotes = getSystemData().getModel().getAttrSpec("dop_info", "sport_formirovanie", getCurrentLang(), null);
			sasStatus = getSystemData().getModel().getAttrSpec("status", "sport_formirovanie", getCurrentLang(), null);
			sasDateStatus = getSystemData().getModel().getAttrSpec("date_status", "sport_formirovanie", getCurrentLang(), null);
		} catch (DbErrorException | InvalidParameterException e) {
			LOGGER.error( "Грешка при ЧЕТЕНЕ НА атрибутите на  MMSsportFormirovanie id=  " + mmsSportFormirovanie.getId(), e);
		}
 
	}
	
	private void findObedinenia() throws DbErrorException {
		mmsSportFormirovanie.setMmsChlenList( mmsSFDAO.findByIdObject(mmsSportFormirovanie.getId(), DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_FORMATIONS));
		for (int i = 0 ; i < mmsSportFormirovanie.getMmsChlenList().size() ; i++) {
			MMSChlenstvo mmsChlenstvo = mmsSportFormirovanie.getMmsChlenList().get(i);
			if(mmsChlenstvo.getTypeVishObject().equals(DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORT_OBED)) {
				MMSSportnoObedinenie tmpSO = mmsSOdao.findById(mmsChlenstvo.getIdVishObject());
				if(i == 0)
					regNomObed = tmpSO.getRegNomer();

				//Referent referentSO = JPA.getUtil().getEntityManager().find(Referent.class, tmpSO.getIdObject());
				Referent referentSO =  referentDAO.findByCodeRef(tmpSO.getIdObject());
				if(referentSO != null) {
					mmsChlenstvo.setVid(tmpSO.getVid());
					mmsChlenstvo.setNameRef(referentSO.getRefName());
					mmsChlenstvo.setEik(referentSO.getNflEik());
					mmsChlenstvo.setRegNom(tmpSO.getRegNomer());
//					mmsChlenstvo.setDateRegNom(tmpSO.getDateRegDoc());
					idsSOexclude.append(tmpSO.getId()); // tuk si пазя ид-тата на Спортни обединения, за да се редуцира филтъра за търсене на СО при избор на ново членство!
					if(i < mmsSportFormirovanie.getMmsChlenList().size() - 1)
						idsSOexclude.append(" , ");
				}
			}
		}
	}
	
	private void findDocs() {
		try {
			JPA.getUtil().runWithClose(() -> docsList = new LazyDataModelSQL2Array((new MMSVpisvaneDAO(getUserData()).findDocsList(mmsSportFormirovanie.getId(),  DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_FORMATIONS)), " doc_date asc "));
		} catch (BaseException e) {
			LOGGER.error("Грешка при зареждане на списъка с dokumenti kum вписвания! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, getMessageResourceString(UI_beanMessages, "so.errFindDoc"));
		}
	}
	
	public void actionVidSportChange() {
		mmsSportFormirovanie.setVidB(false);
		mmsSportFormirovanie.setVidVoenenB(false);
		try {
			for (int i = 0; i < selectedVidSport.size(); i++) {
				if( getSystemData().matchClassifItems(DocuConstants.CODE_CLASSIF_VIDOVE_SPORT_OLIMP, selectedVidSport.get(i), new Date()))
					mmsSportFormirovanie.setVidB(true);
				if( getSystemData().matchClassifItems(DocuConstants.CODE_CLASSIF_VIDOVE_SPORT_VOEN, selectedVidSport.get(i), new Date()))
					mmsSportFormirovanie.setVidVoenenB(true);
			}
			
		} catch (NumberFormatException e) {
			LOGGER.error("Грешка при форматиране! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,"Грешка при обработка на данни!", e.getMessage());
		} catch (DbErrorException e) {
			LOGGER.error("Грешка при работа с базата данни! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
		}
	}
	
	private void getVidSportAStrings(List<MMSVidSportSF> vidSportList) throws DbErrorException {
		selectedVidSport = new ArrayList<>();
		selectedVidSportTxt = "";
		for (int i = 0 ; i < vidSportList.size() ; i++) {
			MMSVidSportSF tmp = vidSportList.get(i);
			selectedVidSport.add(tmp.getVidSport());
			if (selectedVidSportTxt.length()>0) {
				selectedVidSportTxt+=", "+getSystemData().decodeItem(DocuConstants.CODE_CLASSIF_VIDOVE_SPORT, tmp.getVidSport(), getCurrentLang(), new Date());
			}else {
				selectedVidSportTxt+=getSystemData().decodeItem(DocuConstants.CODE_CLASSIF_VIDOVE_SPORT, tmp.getVidSport(), getCurrentLang(), new Date());
			}
		}
	}
	
	private void makeVidSportStringArrayAsObject() throws NumberFormatException, DbErrorException {
		for (int i = 0; i < selectedVidSport.size(); i++) {
			MMSVidSportSF tmp = new MMSVidSportSF();
			tmp.setTipОbject(DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_FORMATIONS); // koda na object Sportno formirovanie
			tmp.setIdObject(mmsSportFormirovanie.getId());
			tmp.setVidSport(Integer.valueOf(selectedVidSport.get(i)));
			tmp.setUserReg(getCurrentUserId());
			tmp.setDateReg(new Date());
			mmsSportFormirovanie.getVidSportList().add(tmp);
		}
	}
	public void actionSave() {
		if( !checkFormFields() )
			return;
		//if(checkForLock(mmsSportFormirovanie.getId()))
		//	return;
			
		try {
			mmsSportFormirovanie.getVidSportList().clear();
			if(mmsSportFormirovanie.getVid().equals( DocuConstants.CODE_ZNACHENIE_VID_SPORTNO_FORMIROVANIE_SK ))
				makeVidSportStringArrayAsObject();
			/*if(referent == null)
				referent = referentDAO.findByCode(getCodeRefCorresp(), new Date(), false);
			*/
			mmsSportFormirovanie.setIdObject(Integer.valueOf(getCodeRefCorresp()));
			List<MMSChlenstvo> tmpBackUp = mmsSportFormirovanie.getMmsChlenList();
			boolean isNewFormirovanie=false;
			if (this.mmsSportFormirovanie.getId()==null) {
				isNewFormirovanie=true;
			}	
			JPA.getUtil().runInTransaction(() -> { 
				mmsSportFormirovanie = mmsSFDAO.save(mmsSportFormirovanie);
				if(referent != null)
					referent = referentDAO.save(referent);
				
				JPA.getUtil().flush();
				for (MMSChlenstvo mmsChlenstvo : mmsChelnstvoListDeleted) {
					 new MMSChlenstvoDAO(MMSChlenstvo.class, getUserData()).delete(mmsChlenstvo);	
				}
				mmsChelnstvoListDeleted = new ArrayList<MMSChlenstvo>();
				mmsSportFormirovanie.setMmsChlenList(tmpBackUp);
				idsSOexclude = new StringBuilder();
				for(int i = 0 ; i < mmsSportFormirovanie.getMmsChlenList().size() ; i++) {
					MMSChlenstvo tmpChlen = mmsSportFormirovanie.getMmsChlenList().get(i);
					if(tmpChlen.getIdObject() == null) {
						tmpChlen.setIdObject(mmsSportFormirovanie.getId());
					}
					new MMSChlenstvoDAO(MMSChlenstvo.class, getUserData()).save(tmpChlen);
					idsSOexclude.append(tmpChlen.getIdVishObject());
					if(i < mmsSportFormirovanie.getMmsChlenList().size() -1)
						idsSOexclude.append(" , ");
				}
				if(idObedinenie != null)
					mmsSOdao.updateBrChlenove(Integer.valueOf(idObedinenie));
				
			});
			
            if (isNewFormirovanie) {
                lockDelo(this.mmsSportFormirovanie.getId());    
            }

			/*if (!mmsChelnstvoListDeleted.isEmpty()) {
				JPA.getUtil().begin();
				for (MMSChlenstvo mmsChlenstvo : mmsChelnstvoListDeleted) {
					new MMSChlenstvoDAO(MMSChlenstvo.class, getUserData()).delete(mmsChlenstvo);
				}
				JPA.getUtil().commit();
				mmsChelnstvoListDeleted = new ArrayList<MMSChlenstvo>();
			}

			mmsSportFormirovanie.setMmsChlenList(tmpBackUp);
			idsSOexclude = new StringBuilder();
			if(!mmsSportFormirovanie.getMmsChlenList().isEmpty()) {
				JPA.getUtil().begin();
				for(int i = 0 ; i < mmsSportFormirovanie.getMmsChlenList().size() ; i++) {
					MMSChlenstvo tmpChlen = mmsSportFormirovanie.getMmsChlenList().get(i);
					if(tmpChlen.getIdObject() == null) {
						tmpChlen.setIdObject(mmsSportFormirovanie.getId());
					}
					new MMSChlenstvoDAO(MMSChlenstvo.class, getUserData()).save(tmpChlen);
					idsSOexclude.append(tmpChlen.getIdVishObject());
					if(i < mmsSportFormirovanie.getMmsChlenList().size() -1)
						idsSOexclude.append(" , ");
				}
				if(idObedinenie != null)
					mmsSOdao.updateBrChlenove(Integer.valueOf(idObedinenie));
				JPA.getUtil().commit();
			}
			*/
			willShowPanels = true;
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO,IndexUIbean.getMessageResourceString(UIBEANMESSAGES, SUCCESSAVEMSG));
			
			//TODO - ако идва от СЕОС след първия запис се вика метода за запис на вписване, документ и промяна на статуса в EgovMessages
			if (this.idSSev != null) {
				actionSaveDocFromSeos();
				
				findVpisvane();
				findDocs();	
			}
		
		} catch (BaseException e) {
			LOGGER.error("Грешка при запис на Спортно формирование! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,IndexUIbean.getMessageResourceString(UIBEANMESSAGES, ERRDATABASEMSG));
		}
	}
	
	public void checkMultipleSportFederations() {
		
	}
	
	public boolean checkReadyForVpisvane() {
		boolean goVpisvane=true;
		/*if(mmsSportFormirovanie.getMmsChlenList() != null && mmsSportFormirovanie.getMmsChlenList().size() < 7) {
			goVpisvane = false;
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, "Федерацията остава с по-малко от 7 членове и лицензът ѝ следва да се прекрати!");
		}*/
		return goVpisvane;
	}
	
	public void generateDoc() {
		if(doc == null)
			doc = new Doc();
		
		//doc.set
	}
	
	private boolean checkFormFields() {
		boolean willSave = true;
		try {
			if(sasEik.isActive()) {
				if(codeRefCorresp == null && sasEik.isRequired()) {
					willSave = false;
					JSFUtils.addMessage("mmsSFform:refCorrInp", FacesMessage.SEVERITY_ERROR, 
							getMessageResourceString(UI_beanMessages, "general.pleaseInsert", getMessageResourceString(LABELS, "admStruct.eik") + " / " + getMessageResourceString(LABELS, "prilojenia.name")));
				}		
//				if (getTxtCorresp()!=null && !getTxtCorresp().isEmpty()) {
//					if(!ValidationUtils.invokeValidation(sasEik.getValidMethod(), getTxtCorresp())) {
//						JSFUtils.addMessage("mmsSFform:eik", FacesMessage.SEVERITY_ERROR, "Невалидно ЕИК(Наименование)!");
//					}
//				}
			}
			if(sasRegNom.isActive() && sasRegNom.isRequired()) {
				if(mmsSportFormirovanie.getRegNomer() == null || mmsSportFormirovanie.getRegNomer().trim().isEmpty()) {
					willSave = false;
					JSFUtils.addMessage("mmsSFform:regNom", FacesMessage.SEVERITY_ERROR, 
							getMessageResourceString(UI_beanMessages, "general.pleaseInsert", getMessageResourceString(LABELS, "docu.regNom")));
				}
			}
			if(sasVidSport.isActive() && sasVidSport.isRequired()) {
				if(selectedVidSport == null || selectedVidSport.size() == 0) {
					if(mmsSportFormirovanie.getVid() != null ) { 
						if(mmsSportFormirovanie.getVid().intValue() != 2) { // 2 == Туристическо дружество!!!
							willSave = false;
							JSFUtils.addMessage("mmsSFform:multiple", FacesMessage.SEVERITY_ERROR, 
									getMessageResourceString(UI_beanMessages, "general.pleaseInsert", getMessageResourceString(LABELS, "sf.vidSport")));
						}
					}else {
						willSave = false;
						JSFUtils.addMessage("mmsSFform:multiple", FacesMessage.SEVERITY_ERROR, 
								getMessageResourceString(UI_beanMessages, "general.pleaseInsert", getMessageResourceString(LABELS, "sf.vidSport")));
					}
				}
			}
			if(sasVid.isActive() && sasVid.isRequired()) {
				if(mmsSportFormirovanie.getVid() == null) {
					willSave = false;
					JSFUtils.addMessage("mmsSFform:typeSport", FacesMessage.SEVERITY_ERROR, 
							getMessageResourceString(UI_beanMessages, "general.pleaseInsert", getMessageResourceString(LABELS, "docu.vid")));
				}
			}
			/*if(sasPredstavitelstvo.isActive()) {
				if((mmsSportFormirovanie.getPredstavitelstvo() == null || mmsSportFormirovanie.getPredstavitelstvo().trim().isEmpty()) && sasPredstavitelstvo.isRequired()) {
					willSave = false;
					JSFUtils.addMessage("mmsSFform:predsedatelstvo", FacesMessage.SEVERITY_ERROR, 
							getMessageResourceString(UI_beanMessages, "general.pleaseInsert", getMessageResourceString(LABELS, "sf.predstavitelstvo")));
				}
				if (mmsSportFormirovanie.getPredstavitelstvo()!=null && !mmsSportFormirovanie.getPredstavitelstvo().isEmpty()) {
					if(!ValidationUtils.invokeValidation(sasPredstavitelstvo.getValidMethod(), mmsSportFormirovanie.getPredstavitelstvo())) {
						JSFUtils.addMessage("mmsSFform:predsedatelstvo", FacesMessage.SEVERITY_ERROR, "Невалидно представителство!");
						willSave = false;
					}
				}
			}*/
			
			if(sasPredsedatel.isActive() ) {
				if((mmsSportFormirovanie.getPredsedatel() == null || mmsSportFormirovanie.getPredsedatel().trim().isEmpty()) && sasPredsedatel.isRequired()) {
					willSave = false;
					JSFUtils.addMessage("mmsSFform:predsedatel", FacesMessage.SEVERITY_ERROR, 
							getMessageResourceString(UI_beanMessages, "general.pleaseInsert", getMessageResourceString(LABELS, "sf.predsedatel")));
				}
				if (mmsSportFormirovanie.getPredsedatel()!=null && !mmsSportFormirovanie.getPredsedatel().isEmpty()) {
					if(!ValidationUtils.invokeValidation(sasPredsedatel.getValidMethod(), mmsSportFormirovanie.getPredsedatel())) {
						JSFUtils.addMessage("mmsSFform:predsedatel", FacesMessage.SEVERITY_ERROR, "Невалиден председател!");
						willSave = false;
					}
				}
			}
			if(sasHighSchool.isActive()) {
				if((mmsSportFormirovanie.getSchoolName() == null || mmsSportFormirovanie.getSchoolName().trim().isEmpty()) && sasHighSchool.isRequired()) {
					willSave = false;
					JSFUtils.addMessage("mmsSFform:highSchool", FacesMessage.SEVERITY_ERROR, 
							getMessageResourceString(UI_beanMessages, "general.pleaseInsert", getMessageResourceString(LABELS, "sf.highSchool")));
				}
				if (mmsSportFormirovanie.getSchoolName()!=null && !mmsSportFormirovanie.getSchoolName().isEmpty()) {
					if(!ValidationUtils.invokeValidation(sasHighSchool.getValidMethod(), mmsSportFormirovanie.getSchoolName())) {
						JSFUtils.addMessage("mmsSFform:highSchool", FacesMessage.SEVERITY_ERROR,  "Невалидно Висше училище!");
						willSave = false;
					}
				}
			}
			if(sasNotes.isActive()) {
				if((mmsSportFormirovanie.getDopInfo() == null || mmsSportFormirovanie.getDopInfo().trim().isEmpty()) && sasNotes.isRequired()) {
					willSave = false;
					JSFUtils.addMessage("mmsSFform:note", FacesMessage.SEVERITY_ERROR, 
							getMessageResourceString(UI_beanMessages, "general.pleaseInsert", getMessageResourceString(LABELS, "docu.note")));
				}
				if (mmsSportFormirovanie.getDopInfo()!=null && !mmsSportFormirovanie.getDopInfo().isEmpty()) {
					if(!ValidationUtils.invokeValidation(sasHighSchool.getValidMethod(), mmsSportFormirovanie.getDopInfo())) {
						JSFUtils.addMessage("mmsSFform:note", FacesMessage.SEVERITY_ERROR,  "Невалидна Забележка!");
						willSave = false;
					}
				}
			}
			/*if(sasStatus.isActive() && sasStatus.isRequired()) {
				if(mmsSportFormirovanie.getStatus() == null) {
					willSave = false;
					JSFUtils.addMessage("mmsSFform:status", FacesMessage.SEVERITY_ERROR, 
							getMessageResourceString(UI_beanMessages, "general.pleaseInsert", getMessageResourceString(LABELS, "compReg.statusVpisvane")));
				}
			}
			if(sasDateStatus.isActive() && sasDateStatus.isRequired()) {
				if(mmsSportFormirovanie.getDateStatus() == null) {
					willSave = false;
					JSFUtils.addMessage("mmsSFform:dateStatus", FacesMessage.SEVERITY_ERROR, 
							getMessageResourceString(UI_beanMessages, "general.pleaseInsert", getMessageResourceString(LABELS, "docu.statusDate")));
				}
			}
			*/
//			if(mmsSportFormirovanie.getVid() != null && mmsSportFormirovanie.getVid().equals(1)) { // 1 ==  Sporten klub (класификация 505)
//				if (referent.getDbRefName()!=null && !referent.getDbRefName().isEmpty()) {
//					if(!referent.getDbRefName().toLowerCase().contains("клуб")) {
//						willSave = false;
//						JSFUtils.addMessage("mmsSFform:eik", FacesMessage.SEVERITY_ERROR, "Наименованието на спортния клуб трябва да включва думата \"клуб\"!");
//					}
//				}
//			}
		} catch (InvalidParameterException e) {
			LOGGER.error("Грешка при валидиране на спортно обединение! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,"Грешка при валидиране на спортно обединение!", e.getMessage());
		}
		return willSave;
	}
	
	public void actionDelete() {
		try {
			JPA.getUtil().runInTransaction(() -> { 
					mmsSFDAO.deleteFromRegister(mmsSportFormirovanie.getId(), (SystemData) getSystemData());  
			});
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO,  IndexUIbean.getMessageResourceString(UI_beanMessages, "general.successDeleteMsg") );
			actionNew(true);
			txtCorresp = null;
			selectedVidSportTxt = null;
		} catch (BaseException e) {
			JSFUtils.addErrorMessage(getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e);
			LOGGER.error(e.getMessage(), e);
			JPA.getUtil().rollback();
		}
	}
	
	public void actionNew() {
		actionNew(true);
	}
	
	public void actionNew(boolean willLoadNewReferent) {
		mmsSportFormirovanie = new MMSsportFormirovanie();
		selectedVidSport = null;
		if(willLoadNewReferent)
			referent = new Referent(); 
		
		//setRegsList(new LazyDataModelSQL2Array(null, ""));
		setRegsList(new ArrayList<MMSVpisvane>());
		setDocsList(null);
		mmsChelnstvoListDeleted = new ArrayList<>();
		setObedineniaList(null);
		idsSOexclude = new StringBuilder();
		if(willLoadNewReferent) {
			codeRefCorresp = null;
			txtCorresp = null;
		}
//		mmsSportFormirovanie.setStatus(DocuConstants.CODE_ZNACHENIE_STATUS_OBEKT_V_RAZGLEJDANE);
//		mmsSportFormirovanie.setDateStatus(new Date());
		dopInfoAdres = null;
		lastVpisvane = new MMSVpisvane();
		lastVpisvane.setStatusVpisvane(DocuConstants.CODE_ZNACHENIE_STATUS_REG_VPISAN);
		lastVpisvane.setDateStatusVpisvane(new Date());
		if(idObedinenie != null) {
			loadNewChlenstvo();
		}
		mmsSportFormirovanie.setVid(DocuConstants.CODE_ZNACHENIE_VID_SPORTNO_FORMIROVANIE_SK);
	}
	
	private void loadNewChlenstvo() {
		try {
			MMSSportnoObedinenie tmpSO = mmsSOdao.findById(Integer.valueOf(idObedinenie));
			regNomObed = tmpSO.getRegNomer();
			//Referent referentSO = JPA.getUtil().getEntityManager().find(Referent.class, tmpSO.getIdObject());
			Referent referentSO =  referentDAO.findByCodeRef(tmpSO.getIdObject());
			MMSChlenstvo mmsChlenstvo = new MMSChlenstvo();
			mmsChlenstvo.setTypeObject(DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_FORMATIONS);
			mmsChlenstvo.setIdObject(mmsSportFormirovanie.getId());
			mmsChlenstvo.setTypeVishObject(DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORT_OBED);
			mmsChlenstvo.setIdVishObject(Integer.valueOf(idObedinenie));
			mmsChlenstvo.setDateAcceptance(new Date());
			mmsChlenstvo.setVid(tmpSO.getVid());
			if(referentSO != null) {
				mmsChlenstvo.setNameRef(referentSO.getRefName());
				mmsChlenstvo.setEik(referentSO.getNflEik());
				messageFromObedinenie = "Ще добавяте нови Спортни формирования към " + 
						getSystemData().decodeItem(DocuConstants.CODE_CLASSIF_VID_SPORT_OBEDINENIE, tmpSO.getVid(), getCurrentLang(), new Date()) +
						" с Наименование: " + referentSO.getRefName() + " и ЕИК: " + referentSO.getNflEik() ;
			}
			mmsChlenstvo.setRegNom(tmpSO.getRegNomer());
//			mmsChlenstvo.setDateRegNom(tmpSO.getDateRegDoc());
			mmsSportFormirovanie.getMmsChlenList().add(mmsChlenstvo);
			
		} catch (DbErrorException e) {
			LOGGER.error("Не е възможно да се зареди връзката със спортно обединение! Моля, опитайте отново!");
		} finally {
			JPA.getUtil().closeConnection();
		}
	}
	
	public void checkDataRegNom() {
		/*if(isEmpty(referent.getNflEik())) {
			JSFUtils.addMessage("mmsSFform:eik",FacesMessage.SEVERITY_ERROR, getMessageResourceString(beanMessages, "refCorr.msgValidEik"));
			return;
		}
		if(!ValidationUtils.isValidBULSTAT(referent.getNflEik())) {
			JSFUtils.addMessage("mmsSFform:eik",FacesMessage.SEVERITY_ERROR, getMessageResourceString(beanMessages, "refCorr.msgValidEik"));		
		}else {
			
		}*/
		try {
			List<MMSsportFormirovanie> tmp = mmsSFDAO.findByRegNom(mmsSportFormirovanie.getRegNomer());
			if(tmp != null && tmp.size() == 0) {
				actionNew(true);
				JSFUtils.addMessage("mmsSFform:eik",FacesMessage.SEVERITY_INFO, getMessageResourceString(beanMessages, "opis.noResuls"));
				return;
			}
			mmsSportFormirovanie = tmp.get(0);
			if(mmsSportFormirovanie != null && mmsSportFormirovanie.getId() != null)
				loadallFileds();
			/*
			 * else
			 	actionNew();
			 */
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			JSFUtils.addMessage("mmsSFform:eik",FacesMessage.SEVERITY_INFO, getMessageResourceString(beanMessages, "opis.noResuls"));	
		} finally {
			JPA.getUtil().closeConnection();
		}
	}
	
	private void loadallFileds() throws DbErrorException {
		if(mmsSportFormirovanie.getId() != null ) {
			findVpisvane();
			findDocs();
			findObedinenia();
		}
		getVidSportAStrings(mmsSportFormirovanie.getVidSportList());
		if(referent != null && referent.getId() == null) {
			referent = referentDAO.findByCodeRef(mmsSportFormirovanie.getIdObject());
		}
		setCodeRefCorresp(referent.getCode());
		getDopInfoAdres();
		actionVidSportChange();
		//loadAttrSpecifications();
	}
	
	/**
	 *  зарежда данни за корепондент по зададени критерии
	 */
	/*private Referent loadCorresp(Integer idCoresp, Date dateCorr) {
		try {
			if(idCoresp != null) {
				JPA.getUtil().runWithClose(() -> referent = new ReferentDAO(getUserData()).findByCode(idCoresp, dateCorr, true));
			}
		} catch (BaseException e) {
			LOGGER.error("Грешка при зареждане на данни за лице! ", e);
		}

		return referent;
	}*/
	
	
	private EgovMessagesCoresp emcoresp = new EgovMessagesCoresp();
	public void comesFromDAEU(Integer egovID) {
		
		try {
			//EgovMessages tmpEgov = new EgovMessagesDAO(getUserData()).findById(egovID);
			egovMessFilesList = new EgovMessagesDAO(getUserData()).findFilesByMessage(egovID);
			setEmcoresp(mmsSFDAO.findByIdMessage(egovID));
			if(getEmcoresp() == null) {
				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, "Липсва ЕИК на лицето в заявлението! Моля, изберете ново заявление!");	
				//sendMail("Отговор на подадено заявление", "Липсва ЕИК на лицето в заявлението!");
				willShowPanels = false;
				willShowSaveBtns = false;
				return;
			}
			/*if(getEmcoresp().getEmail() == null || getEmcoresp().getEmail().trim().isEmpty() )
				willShowMailModal = false;
			else
				willShowMailModal = true;
			*/if(getEmcoresp().getBulstat() != null && !getEmcoresp().getBulstat().trim().isEmpty()) {
				if( !ValidationUtils.isValidBULSTAT(getEmcoresp().getBulstat()) ) {
					// 3. 	Валидация на ЕИК. При установяване на грешка се прекратява обработката, като се изпраща известие по мейла на заявителя с подходящата нотификация
					JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, "Грешен ЕИК в заявлението! Моля, изберете ново заявление!");
					//sendMail("Отговор на подадено заявление", "Грешен ЕИК в заявлението! Трябва да изберете ново заявление!");
					willShowPanels = false;
					willShowSaveBtns = false;
					return;
				}
				referent = new ReferentDAO(ud).findByIdent(getEmcoresp().getBulstat(), null, null, DocuConstants.CODE_ZNACHENIE_REF_TYPE_NFL);
				mmsSportFormirovanie = mmsSFDAO.findByIdObject(Integer.valueOf(referent.getCode()));
				if(mmsSportFormirovanie == null || mmsSportFormirovanie.getId() == null) {
					if(referent != null) {
						setTxtCorresp(referent.getIme());
					}else {
						actionNew(true);
						
					}
					willShowPanels = false;
				}else {
					if(mmsSportFormirovanie != null && mmsSportFormirovanie.getId() != null) {
						loadallFileds();
						willShowPanels = true;
					}
				}
			}else {
				willShowPanels = false;
				actionNew(true);
			}
			willShowSaveBtns = true;
		} catch (DbErrorException e) {
			LOGGER.error(e.getMessage(), e);
		} /*catch (AddressException e) {
			LOGGER.error(e.getMessage(), e);
		} catch (InvalidParameterException e) {
			LOGGER.error(e.getMessage(), e);
		} catch (MessagingException e) {
			LOGGER.error(e.getMessage(), e);
		} */finally {
			JPA.getUtil().closeConnection();
		}
		
		
		if(viewOnly == 1) {
			
		}else {
			
		}
		
		// 4. Проверка в базата , ако се намери - > отказ от регистрация и мейл до заявителя.
		
	}
	
	public void actionLoadEgovMessage() {
		if (this.idSSev!=null) {
			try {
				
				setEgovMess(new EgovMessagesDAO(getUserData()).findById(this.idSSev));
				emcorespList = (List<EgovMessagesCoresp>) new MMSCoachesDAO(MMSCoaches.class, ud).findCorespByIdMessage(this.idSSev);
				setEgovFilesList(new EgovMessagesDAO(getUserData()).findFilesByMessage(this.idSSev));
				ArrayList<Object[]> tmpList = new EgovMessagesDAO(getUserData()).createMsgTypesList();
				
				tmpList = new EgovMessagesDAO(getUserData()).createMsgStatusList();
			
				if(tmpList !=null && !tmpList.isEmpty()){
					for(Object[] item:tmpList) {
						if(item != null && item[0]!=null && item[1]!=null){
							getMsgStatusList().add(new SelectItem( item[0].toString(),item[1].toString()));
						}
					}
				}
				if(emcorespList != null && emcorespList.size()>0) {
					for (EgovMessagesCoresp item : emcorespList) {
						if(item.getBulstat() != null && !item.getBulstat().trim().isEmpty()) {
							if( !ValidationUtils.isValidBULSTAT(item.getBulstat()) ) {
								emcoresp = item;
								break;
							}
						}
					}
					
					if (emcoresp==null || emcoresp.getId() == null)
						emcoresp=emcorespList.get(0);
					
					// Za vpisvanijata --> samo EIK pri formirovaniqta!!
					this.eik = emcoresp.getBulstat();
					this.egn = null;
	
				}
				if (null!=egovMess) {
					
					if(ValidationUtils.isNotBlank(egovMess.getDocVid()) && ValidationUtils.isNumber(egovMess.getDocVid())) { //Взема Вид заявление - вписване, заличаване, промяна обстоятелства
						this.vidDoc=Integer.valueOf(egovMess.getDocVid());
					}else{
						this.vidDoc=null;
					}
					
					// Za vpisvanijata
					this.regNom = egovMess.getDocRn();
					try {
						dataDoc = new SimpleDateFormat("dd.MM.yyyy").format(egovMess.getDocDate());
					} catch (Exception e) {
						LOGGER.error(e.getMessage());
					} 
					this.otnosno = egovMess.getDocSubject();
					
				}
			} catch (DbErrorException e) {
				LOGGER.error("Грешка при работа с базата!", e);
				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());	
			}catch (Exception e) {
				LOGGER.error("Грешка при зареждане данните!", e);
				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());	
			} 
			
		}
	}
	
	public void download(EgovMessagesFiles file) {
		try {

			if (file.getBlobcontent() != null) {

				String codedfilename = URLEncoder.encode(file.getFilename(), "UTF8");
				FacesContext facesContext = FacesContext.getCurrentInstance();
				ExternalContext externalContext = facesContext.getExternalContext();
				externalContext.setResponseHeader("Content-Type", "application/x-download");
				externalContext.setResponseHeader("Content-Length", file.getBlobcontent().length + "");
				externalContext.setResponseHeader("Content-Disposition", "attachment;filename=\"" + codedfilename + "\"");
				externalContext.getResponseOutputStream().write(file.getBlobcontent());
				facesContext.responseComplete();
			}
		
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, "Грешка при сваляне на файла!: ", e.getMessage());
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, "Грешка при сваляне на файла!: ", e.getMessage());
		}
	}
	
	public void closeModalMail() {
		PrimeFaces.current().executeScript("PF('eMail').hide();");
	}
	
	public void sendMail() {
		boolean sending = true;
		if(subject == null || subject.trim().isEmpty()) {
			JSFUtils.addMessage("mmsSFform:subject", FacesMessage.SEVERITY_ERROR, 
					getMessageResourceString(UI_beanMessages, "general.pleaseInsert", getMessageResourceString(LABELS, "general.otnosno")));
			sending = false;
		}
		if(mailText == null || mailText.trim().isEmpty()) {
			JSFUtils.addMessage("mmsSFform:mailText", FacesMessage.SEVERITY_ERROR, 
					getMessageResourceString(UI_beanMessages, "general.pleaseInsert", getMessageResourceString(LABELS, "general.text")));
			sending = false;
		}
		if(!sending)
			return;
		
		Mailer mailer = new Mailer();
		attachedBytes.clear();
		for (Files upLoadedFile : uploadFilesList) {
			getAttachedBytes().add(new ByteArrayDataSource(upLoadedFile.getContent(), ""));
		}
		
		try {
			mailer.sent(Content.PLAIN, props, props.getProperty("user.name"), props.getProperty("user.password"),
					props.getProperty("mail.from"), "Министерство на младежта и спорта", 
					//referent.getContactEmail(),
					//emcoresp.getEmail() , 
					"n.kosev@indexbg.bg", 
					subject, mailText,
					getAttachedBytes() );
			JSFUtils.addInfoMessage("Успешно изпращане на съобщението!");
			subject = "";
			mailText = "";
			getAttachedBytes().clear();
			uploadFilesList.clear();
			PrimeFaces.current().executeScript("PF('eMail').hide();");
		} catch (AddressException e) {
			LOGGER.error(e.getMessage());
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, "Неуспешно изпращане на съобщението! Грешка в е-мейл адреса!");
		} catch (InvalidParameterException e) {
			LOGGER.error(e.getMessage());
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, "Неуспешно изпращане на съобщението!");
		} catch (MessagingException e) {
			LOGGER.error(e.getMessage());
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, "Неуспешно изпращане на съобщението!");
		}
	}
	
	
	/** При отваряне на модален за Ново членство, се подава ЕИК и се показва списък от СО (Спортни Обединения) за избор!
	 * */
	public void selectNewSO() {
		if(codeRefCorrespChlenstvo == null) {
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, "Не сте избрали лице!");
			return;
		}
		boolean isOnTheList = false;
		try {
			idsSOexclude = new StringBuilder();
			for (int i = 0; i < mmsSportFormirovanie.getMmsChlenList().size() ; i++) {
				MMSChlenstvo mmsChlenstvo = mmsSportFormirovanie.getMmsChlenList().get(i);
				idsSOexclude.append(mmsChlenstvo.getIdVishObject());
				if(codeRefCorrespChlenstvo.equals(mmsChlenstvo.getIdVishObject())) 
					isOnTheList = true;
				if(i < mmsSportFormirovanie.getMmsChlenList().size() -1) 
					idsSOexclude.append(" , ");
			}
			
			//JPA.getUtil().runWithClose(() ->  setObedineniaList(new LazyDataModelSQL2Array( mmsSOdao.findChlenstvoByIdRefferent(codeRefCorrespChlenstvo), true, idsSOexclude.toString() ), " reg_nomer asc ")));
			JPA.getUtil().runWithClose(()->  tmpCorespChlenstvo= mmsSOdao.findChlenstvoByIdRefferent(codeRefCorrespChlenstvo, idsSOexclude.toString())  );
			if(tmpCorespChlenstvo != null)
				registerNewSO((BigInteger)tmpCorespChlenstvo[5], (BigInteger)tmpCorespChlenstvo[0], (String)tmpCorespChlenstvo[1], (String)tmpCorespChlenstvo[2], (String)tmpCorespChlenstvo[3], (Date)tmpCorespChlenstvo[4]);
			else {
				if(isOnTheList)
					JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, "Вече имате въведено това обединение за членство!");
				else
					JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, "Няма намерено обединение за членство!");
			}
		} catch (BaseException e) {
			LOGGER.error("Грешка при зареждане на списъка със спортни обединения! ", e); 
		}
		 
	}
	
	public void registerNewSO(BigInteger idSObedinenie, BigInteger vid, String refName, String nflEik, String regNom, Date regNomDate ) {
		MMSChlenstvo e = new MMSChlenstvo();
		e.setTypeObject(DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_FORMATIONS);
		e.setIdObject(mmsSportFormirovanie.getId());
		e.setTypeVishObject(DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORT_OBED);
		e.setIdVishObject( idSObedinenie.intValue());
		e.setVid(vid.intValue());
		e.setNameRef(refName);
		e.setEik(nflEik);
		e.setDateAcceptance(new Date());
		e.setRegNom(regNom);
		e.setDateReg(regNomDate);
		/*try {
			//e.setDateRegNom(new SimpleDateFormat("yyyy-MM-dd HH:mm:sss").parse(regNomDate));
			
		} catch (ParseException e1) {
			e.setDateRegNom(null);
			LOGGER.error("Грешка при парсването на дата при избор на СО към СФ!" + e1);
		}*/
		
		mmsSportFormirovanie.getMmsChlenList().add(e);
		PrimeFaces.current().executeScript("PF('mCorrSChelnstvo').hide();");
	}
	
	public void removeChlenstvFromList(MMSChlenstvo clen) {
		if(clen.getId() != null)
			mmsChelnstvoListDeleted.add(clen);
		
		mmsSportFormirovanie.getMmsChlenList().remove(clen);
	}
	
	public void findVpisvane() {
		try {		
			/*JPA.getUtil().runWithClose(() -> setRegsList(new LazyDataModelSQL2Array( new MMSVpisvaneDAO(getUserData()).findRegsListNativeSMD(
					DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_FORMATIONS, mmsSportFormirovanie.getId()), " date_reg desc ")));
		*/
			JPA.getUtil().runWithClose(() -> setRegsList(new MMSVpisvaneDAO(getUserData()).findRegsListByIdAndType(
					DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_FORMATIONS, mmsSportFormirovanie.getId())));
					
		} catch (BaseException e) {
			LOGGER.error("Грешка при зареждане на списъка с вписвания! ", e);
		}
	}
	
	public void registerNewDoc() {
		
	}

	public MMSsportFormirovanie getMmsSportFormirovanie() {
		return mmsSportFormirovanie;
	}

	public void setMmsSportFormirovanie(MMSsportFormirovanie mmsSportFormirovanie) {
		this.mmsSportFormirovanie = mmsSportFormirovanie;
	}

	public int getViewOnly() {
		return viewOnly;
	}

	public void setViewOnly(int viewOnly) {
		this.viewOnly = viewOnly;
	}

	public Referent getReferent() {
		return referent;
	}

	public void setReferent(Referent referent) {
		this.referent = referent;
	}

	public List<Integer> getSelectedVidSport() {
		return selectedVidSport;
	}

	public void setSelectedVidSport(List<Integer> selectedVidSport) {
		this.selectedVidSport = selectedVidSport;
		actionVidSportChange();
	}

	public LazyDataModelSQL2Array getDocsList() {
		return docsList;
	}

	public LazyDataModelSQL2Array setDocsList(LazyDataModelSQL2Array docsList) {
		this.docsList = docsList;
		return docsList;
	}

	/*public LazyDataModelSQL2Array getRegsList() {
		return regsList;
	}

	public void setRegsList(LazyDataModelSQL2Array regsList) {
		this.regsList = regsList;
	}*/

	public LazyDataModelSQL2Array getObedineniaList() {
		return obedineniaList;
	}

	public LazyDataModelSQL2Array setObedineniaList(LazyDataModelSQL2Array obedineniaList) {
		this.obedineniaList = obedineniaList;
		return obedineniaList;
	}

	public List<MMSChlenstvo> getMmsChelnstvoListDeleted() {
		return mmsChelnstvoListDeleted;
	}

	public void setMmsChelnstvoListDeleted(List<MMSChlenstvo> mmsChelnstvoListDeleted) {
		this.mmsChelnstvoListDeleted = mmsChelnstvoListDeleted;
	}
	
	public Date getDecodeDate2() {
		return decodeDate2;
	}

	public void setDecodeDate2(Date decodeDate2) {
		this.decodeDate2 = decodeDate2 != null ? new Date(decodeDate2.getTime()) : null;
	}

	public String getDopInfoAdres() {
		if(this.dopInfoAdres == null || this.dopInfoAdres.trim().isEmpty()) {
			loadDopInfoAdres();
		}
		return dopInfoAdres;
	}

	public void setDopInfoAdres(String dopInfoAdres) {
		this.dopInfoAdres = dopInfoAdres;
	}
	/**
	 * зарежда адреса на кореспондента
	 */
	public void loadDopInfoAdres() {
		if(getCodeRefCorresp() != null) {
			// ако нямам права да виждам лини данни
			// заради достъпа до личните данни - в допълнителната информаиця за физическите лица да остане само населеното място!!
			try {				
				this.dopInfoAdres = getSystemData().decodeItemDopInfo(DocuConstants.CODE_CLASSIF_REFERENTS, getCodeRefCorresp(), getCurrentLang(), new Date());
				if(this.dopInfoAdres != null &&
					(int) getSystemData().getItemSpecific(DocuConstants.CODE_CLASSIF_REFERENTS, getCodeRefCorresp() ,  getCurrentLang(), new Date(), DocuClassifAdapter.REFERENTS_INDEX_REF_TYPE) == DocuConstants.CODE_ZNACHENIE_REF_TYPE_NFL) {
				
					if(!getUserData().hasAccess(DocuConstants.CODE_CLASSIF_DEF_PRAVA, DocuConstants.CODE_ZNACHENIE_DEF_PRAVA_SEE_PERSONAL_DATA) ) {
						// да остане само град или село  
						int i1 = this.dopInfoAdres.indexOf("гр.");
						if(i1 == -1) {
							i1 = this.dopInfoAdres.indexOf("с.");
						}
						if(i1 != -1) {						
							int i2 = this.dopInfoAdres.indexOf(", ", i1);
							if(i2 != -1) {
								this.dopInfoAdres = this.dopInfoAdres.substring(i1, i2);
							}else {
								// има само град или село...
								this.dopInfoAdres = this.dopInfoAdres.substring(i1);
							}
						}else {
							this.dopInfoAdres = null;
						}
					}else { // да махна ЕГН, за да остане само адреса
						int i1 = this.dopInfoAdres.indexOf("ЕГН");
						if(i1 != -1) {	
							//има егн
							int i2 = this.dopInfoAdres.indexOf(", ", i1);
							if(i2 != -1) {
								this.dopInfoAdres = this.dopInfoAdres.substring(i2+1);
							}else {
								this.dopInfoAdres = null; // има само егн...
							}
						}
					}
				}			
			} catch (Exception e) {
				LOGGER.error("Грешка при формиране на адрес на кореспонднета за показване в документа! ", e);
			}
			
		}else {
			this.dopInfoAdres = null; 
		}
	}
	
	public void callONCompleteInReferentEdit() {
		clearInfoAdres();
		PrimeFaces.current().executeScript("PF('mCorrD').hide();document.body.scrollTop = 0; document.documentElement.scrollTop = 0;");
	}
	
	public void clearInfoAdres() {
		try {
			referent = new ReferentDAO(ud).findByCode(getCodeRefCorresp(), new Date(), true);
			if(referent != null) {
				if(mmsSportFormirovanie == null || mmsSportFormirovanie.getId() == null) {
					MMSsportFormirovanie tmpFormirovanie = mmsSFDAO.findByIdObject(referent.getCode());
					if (tmpFormirovanie!=null) {
						mmsSportFormirovanie=tmpFormirovanie;
						if(mmsSportFormirovanie != null && mmsSportFormirovanie.getId() != null) {
							loadallFileds();
							if(idObedinenie != null) {
								boolean willAddObedinenie = true;
								for (MMSChlenstvo mmsChlenstvo : mmsSportFormirovanie.getMmsChlenList()) {
									if(mmsChlenstvo.getIdVishObject().equals(Integer.valueOf(idObedinenie)))
										willAddObedinenie = false;
								}
								if(willAddObedinenie)
									loadNewChlenstvo(); 
							}
						}	
					}
					
				}
			}else {
				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, "Не е намерено лицето с подадения ЕИК! Трябва да въведете нов ЕИК!");	
				actionNew(true);
			}
			if(referent.getContactEmail() == null || referent.getContactEmail().trim().isEmpty() )
				willShowMailModal = false;
			else
				willShowMailModal = true;
		} catch (Exception e) {
			 
			LOGGER.error("Грешка при търсене на спортно формирование по ид на рефент!" + e.getMessage());
			
//			JSFUtils.addInfoMessage("Няма намерени Спортни формирования въведени към този ЕИК!");
//			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, "Няма намерени Спортни формирования въведени към този ЕИК!");
		} finally {
			JPA.getUtil().closeConnection();
		}
	}
	
	public String goBackObedinenie() {
		return "sportObedEdit.xhtml?faces-redirect=true&idObj=" + idObedinenie+"&idFormir=true";
	}
	
	public Integer getCodeRefCorresp() {
		return codeRefCorresp;
	}

	public void setCodeRefCorresp(Integer codeRefCorresp) {
		this.codeRefCorresp = codeRefCorresp;
		if(codeRefCorresp != null)
			clearInfoAdres();
	}

	public String getTxtCorresp() {
		return txtCorresp;
	}

	public void setTxtCorresp(String txtCorresp) {
		this.txtCorresp = txtCorresp;
	}

	

	/*
	public List<Object[]> getRegsList() {
		return regsList;
	}
	public void setRegsList(ArrayList<Object[]> regsList) {
		this.regsList = regsList;
	}*/
	public List<MMSVpisvane> getRegsList() {
		return regsList;
	}
	public void setRegsList(List<MMSVpisvane> regsList) {
		this.regsList = regsList;
	}

	public String getIdObedinenie() {
		return idObedinenie;
	}

	public void setIdObedinenie(String idObedinenie) {
		this.idObedinenie = idObedinenie;
	}

	public String getMessageFromObedinenie() {
		return messageFromObedinenie;
	}

	public void setMessageFromObedinenie(String messageFromObedinenie) {
		this.messageFromObedinenie = messageFromObedinenie;
	}

	public MMSVpisvane getLastVpisvane() {
		return lastVpisvane;
	}

	public void setLastVpisvane(MMSVpisvane lastVpisvane) {
		this.lastVpisvane = lastVpisvane;
	}

	public SysAttrSpec getSasRegNom() {
		return sasRegNom;
	}

	public void setSasRegNom(SysAttrSpec sasRegNom) {
		this.sasRegNom = sasRegNom;
	}

	public SysAttrSpec getSasVidSport() {
		return sasVidSport;
	}

	public void setSasVidSport(SysAttrSpec sasVidSport) {
		this.sasVidSport = sasVidSport;
	}

	public SysAttrSpec getSasVid() {
		return sasVid;
	}

	public void setSasVid(SysAttrSpec sasVid) {
		this.sasVid = sasVid;
	}

	public SysAttrSpec getSasPredstavitelstvo() {
		return sasPredstavitelstvo;
	}

	public void setSasPredstavitelstvo(SysAttrSpec sasPredstavitelstvo) {
		this.sasPredstavitelstvo = sasPredstavitelstvo;
	}

	public SysAttrSpec getSasPredsedatel() {
		return sasPredsedatel;
	}

	public void setSasPredsedatel(SysAttrSpec sasPredsedatel) {
		this.sasPredsedatel = sasPredsedatel;
	}

	public SysAttrSpec getSasHighSchool() {
		return sasHighSchool;
	}

	public void setSasHighSchool(SysAttrSpec sasHighSchool) {
		this.sasHighSchool = sasHighSchool;
	}

	public SysAttrSpec getSasNotes() {
		return sasNotes;
	}

	public void setSasNotes(SysAttrSpec sasNotes) {
		this.sasNotes = sasNotes;
	}

	public SysAttrSpec getSasStatus() {
		return sasStatus;
	}

	public void setSasStatus(SysAttrSpec sasStatus) {
		this.sasStatus = sasStatus;
	}

	public SysAttrSpec getSasDateStatus() {
		return sasDateStatus;
	}

	public void setSasDateStatus(SysAttrSpec sasDateStatus) {
		this.sasDateStatus = sasDateStatus;
	}

	public String getRegNomObed() {
		return regNomObed;
	}

	public void setRegNomObed(String regNomObed) {
		this.regNomObed = regNomObed;
	}

	public boolean isWillShowPanels() {
		return willShowPanels;
	}

	public void setWillShowPanels(boolean willShowPanels) {
		this.willShowPanels = willShowPanels;
	}

	public SysAttrSpec getSasEik() {
		return sasEik;
	}

	public void setSasEik(SysAttrSpec sasEik) {
		this.sasEik = sasEik;
	}

	public Integer getCodeRefCorrespChlenstvo() {
		return codeRefCorrespChlenstvo;
	}

	public void setCodeRefCorrespChlenstvo(Integer codeRefCorrespChlenstvo) {
		this.codeRefCorrespChlenstvo = codeRefCorrespChlenstvo;
		if(this.codeRefCorrespChlenstvo != null) {
			selectNewSO();
		}
	}

	public Date getDecodeDate2Chlenstvo() {
		return decodeDate2Chlenstvo;
	}

	public void setDecodeDate2Chlenstvo(Date decodeDate2Chlenstvo) {
		this.decodeDate2Chlenstvo = decodeDate2Chlenstvo;
	}

	public String getTxtCorrespChlenstvo() {
		return txtCorrespChlenstvo;
	}

	public void setTxtCorrespChlenstvo(String txtCorrespChlenstvo) {
		this.txtCorrespChlenstvo = txtCorrespChlenstvo;
	}

	public boolean isWillShowSaveBtns() {
		return willShowSaveBtns;
	}

	public void setWillShowSaveBtns(boolean willShowSaveBtns) {
		this.willShowSaveBtns = willShowSaveBtns;
	}
	
	public void actionOtkazReg() {
		if (this.getReasonOtkaz()==null || this.getReasonOtkaz().isEmpty()) {
			 JSFUtils.addMessage("mmsSFform:otkazText", FacesMessage.SEVERITY_ERROR,  getMessageResourceString(UI_beanMessages, "general.pleaseInsert", "Причина за отказ от регистрация"));
			 return;
		}
		try {
            EgovMessages egovMess = new EgovMessagesDAO(getUserData()).findById(this.idSSev);
            JPA.getUtil().runInTransaction(() -> { 
                if (null!=egovMess) {
                    egovMess.setMsgStatus("DS_REJECTED");
                    egovMess.setMsgStatusDate(new Date());
                    egovMess.setCommError(getReasonOtkaz());
                    new EgovMessagesDAO(getUserData()).save(egovMess);
//                    mmsSportFormirovanie.setStatus(DocuConstants.CODE_ZNACHENIE_STATUS_ZAIAVLENIE_OTKAZANO_VPISVANE);
//                    mmsSportFormirovanie.setDateStatus(egovMess.getMsgStatusDate());
//                    mmsSFDAO.save(mmsSportFormirovanie);
                }
            });  
            JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO,IndexUIbean.getMessageResourceString(UIBEANMESSAGES, SUCCESSAVEMSG));
		} catch (DbErrorException e) {
            LOGGER.error("Грешка при смяна стауса на заявление при отказ!! ", e);
            JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString("ui_beanMessages", ERRDATABASEMSG), e.getMessage());
            JPA.getUtil().rollback();
        } catch (BaseException e) {
            LOGGER.error("Грешка при смяна стауса на заявление при отказ!!", e);
            JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString("ui_beanMessages", ERRDATABASEMSG), e.getMessage());
            JPA.getUtil().rollback();
        }finally {
            JPA.getUtil().closeConnection();
        }
	}

	/**************************************************** FROM SSEV ****************************************************/	

	private Integer idSSev;
	private Integer vidDoc;
	private String regNom;
	private String dataDoc;
	private String otnosno;
	private String egn;
	private String eik;
	private Referent ref = new Referent();
	private MMSVpisvane vpisvane = new MMSVpisvane();
	private boolean noVp = false;
	
	// Метода е за извикване след запис на обектите ако са извикани от "Нови заявления"!
	public void actionSaveDocFromSeos() {
		
		Doc newDoc = new Doc();	
		MMSVpisvaneDoc vpisvaneDoc = new MMSVpisvaneDoc();	
		
		//DateFormat form = new SimpleDateFormat("yyy-MM-dd HH:mm:ss.S"); 
		
		boolean saveNewVp = false;
		
		try {
			
			JPA.getUtil().runWithClose(() -> {
				
				List<MMSVpisvane> regList = new MMSVpisvaneDAO(getUserData()).findRegsListByIdAndType(this.mmsSportFormirovanie.getCodeMainObject(), this.mmsSportFormirovanie.getId()); 
				
				if (!regList.isEmpty()) {
					vpisvane = new MMSVpisvaneDAO(getUserData()).findById(regList.get(0).getId()); 
				
				} else {						
					noVp = true;					
				}
				
			});	
				
			if (vidDoc.equals(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_SPORTNO_FORM)
					|| vidDoc.equals(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_TD) ) {
				
				boolean existActVpis = false;
				
				if (vpisvane.getStatusVpisvane() != null && vpisvane.getStatusVpisvane().equals(DocuConstants.CODE_ZNACHENIE_STATUS_REG_VPISAN)) {

					existActVpis = true;
				
				} else if (vpisvane.getStatusVpisvane() == null && vpisvane.getStatusResultZaiavlenie() != null
						&& (vpisvane.getStatusResultZaiavlenie().equals(DocuConstants.CODE_ZNACHENIE_STATUS_ZAIAVLENIE_V_RAZGLEJDANE)
								|| vpisvane.getStatusResultZaiavlenie().equals(DocuConstants.CODE_ZNACHENIE_STATUS_ZAIAVLENIE_VPISAN))) {

					existActVpis = true;
				}
				
				if (noVp ||	(!noVp && !existActVpis) ) {
				
					saveNewVp = true;
					noVp = false; // за да запише ново вписване, когато отговаря на условията
				
				} else {					
					
					saveNewVp = false;
					JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString("beanMessages", "mmsSpForm.noSaveZaiav"));	
					PrimeFaces.current().executeScript("scrollToErrors()");
					return;
				}
			
			} else {
				
				if (noVp) {
					
					//Ако няма нито едно вписване - съобщение, че няма към кое вписване да се направи заличаване или промяна на обстоятелствата
					if (vidDoc.equals(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_PROM_SPORTNO_FORM)) {
						JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString("beanMessages", "mmsSpForm.noSaveZaiavPrObst"));						
					}
					
					if (vidDoc.equals(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_ZALICH_SPORTNO_FORM)) {
						JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString("beanMessages", "mmsSpForm.noSaveZaiavZalich"));						
					}
					
					PrimeFaces.current().executeScript("scrollToErrors()");
					return;
					
				}
			}
			 
			if (!noVp) {
				
				// настройка по вид документ и регистратура
				Object[] docVidSetting = new DocDAO(getUserData()).findDocSettings(getUserData(UserData.class).getRegistratura(), this.vidDoc, getSystemData());
				
				if (docVidSetting == null) {
					
					String noSett = getSystemData().decodeItem(DocuConstants.CODE_CLASSIF_DOC_VID, this.vidDoc, getCurrentLang(), new Date());								
					
					JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString("beanMessages", "compReg.noDocSettings", noSett));	
					PrimeFaces.current().executeScript("scrollToErrors()");
					return;
				
				} else {
					
					newDoc.setDocVid(this.vidDoc);
					newDoc.setRnDoc(this.regNom);	
					//newDoc.setDocDate(form.parse(this.dataDoc));
					newDoc.setDocDate(new SimpleDateFormat("dd.MM.yyyy").parse(dataDoc));
				
					if (this.egn != null) {
						JPA.getUtil().runWithClose(() -> ref = new ReferentDAO(getUserData()).findByIdent(null, this.egn, null, DocuConstants.CODE_ZNACHENIE_REF_TYPE_FZL)); 
					} else if (this.eik != null) {
						JPA.getUtil().runWithClose(() -> ref = new ReferentDAO(getUserData()).findByIdent(this.eik, null, null, DocuConstants.CODE_ZNACHENIE_REF_TYPE_NFL)); 
					}
					if (ref != null) {
						newDoc.setCodeRefCorresp(ref.getCode()); 
					} 
					
					newDoc.setRegisterId((Integer) docVidSetting[1]);
					boolean createDelo = Objects.equals(docVidSetting[2], Constants.CODE_ZNACHENIE_DA);
					
					Integer typeDocByRegister = (Integer) getSystemData().getItemSpecific(DocuConstants.CODE_CLASSIF_REGISTRI, newDoc.getRegisterId(), getCurrentLang(), new Date() , DocuClassifAdapter.REGISTRI_INDEX_DOC_TYPE);
					
					newDoc.setRegistraturaId(getUserData(UserData.class).getRegistratura());
					newDoc.setDocType(typeDocByRegister);
					newDoc.setFreeAccess(Constants.CODE_ZNACHENIE_DA);
					
					if (newDoc.getDocType().intValue() == DocuConstants.CODE_ZNACHENIE_DOC_TYPE_IN) {  
						newDoc.setProcDef((Integer) docVidSetting[5]);
					
					} else if (newDoc.getDocType().intValue() == DocuConstants.CODE_ZNACHENIE_DOC_TYPE_OWN) {  
						newDoc.setProcDef((Integer) docVidSetting[6]);
					
					} else if (newDoc.getDocType().intValue() == DocuConstants.CODE_ZNACHENIE_DOC_TYPE_WRK) {  
						newDoc.setProcDef((Integer) docVidSetting[7]);
					}
					
					if (SearchUtils.isEmpty(this.otnosno)) {
						
						newDoc.setOtnosno(getSystemData().decodeItem(DocuConstants.CODE_CLASSIF_DOC_VID, newDoc.getDocVid(), getCurrentLang(), new Date()));
					} else {
						
						newDoc.setOtnosno(this.otnosno); 
					}
					
					if (saveNewVp) {
						
						vpisvane = new MMSVpisvane();
						
						vpisvane.setRnDocZaiavlenie(this.regNom);
						//vpisvane.setDateDocZaiavlenie(form.parse(this.dataDoc));
						vpisvane.setDateDocZaiavlenie(new SimpleDateFormat("dd.MM.yyyy").parse(dataDoc));
						vpisvane.setStatusResultZaiavlenie(DocuConstants.CODE_ZNACHENIE_STATUS_ZAIAVLENIE_V_RAZGLEJDANE);
						vpisvane.setDateStatusZaiavlenie(new Date());
						vpisvane.setIdObject(this.mmsSportFormirovanie.getId()); // ИД на обекта
						vpisvane.setTypeObject(this.mmsSportFormirovanie.getCodeMainObject()); //КОДА на обекта				
						vpisvane.setNachinPoluchavane(this.mmsSportFormirovanie.getNachinPoluch());
						vpisvane.setAddrMailPoluchavane(this.mmsSportFormirovanie.getNachinPoluchText());
					} 
					
					JPA.getUtil().runInTransaction(() -> { 
						
						// Търси дали има въведен документ по този рег. номер и дата и ако има - взима неговото ид
						List<Integer> idDocsList = new DocDAO(getUserData()).findDocIdList(newDoc.getRnDoc(), newDoc.getDocDate());
						Integer idDoc = null;
						
						if (!idDocsList.isEmpty()) {
							
							idDoc = idDocsList.get(0);							
						}
						
						if(idDoc == null) {
							// записва се документа при нас
							new DocDAO(getUserData()).save(newDoc, createDelo, null, null, getSystemData());
							idDoc = newDoc.getId();
							
							copyEgovFiles(idDoc);
						} 
						
						// записва се ново вписване, ако е такова заявлението
						if (vpisvane.getId() == null) {
							new MMSVpisvaneDAO(getUserData()).save(vpisvane);
						}
												
						// записваме връзката на документа с вписването
						if (idDoc != null && vpisvane.getId() != null) {
							
							vpisvaneDoc.setIdObject(vpisvane.getIdObject());
							vpisvaneDoc.setTypeObject(vpisvane.getTypeObject()); 
							vpisvaneDoc.setIdVpisvane(vpisvane.getId());
							vpisvaneDoc.setIdDoc(idDoc);
							
							new MMSVpisvaneDocDAO(getUserData()).save(vpisvaneDoc);
						}
						
						//TODO - дали така ще се ъпдейтва статуса в egov_messages - msg_status и msg_status_dat
						
						EgovMessages egovMess = new EgovMessagesDAO(getUserData()).findById(this.idSSev); 
						
						egovMess.setMsgRn(newDoc.getRnDoc());
						egovMess.setMsgRnDate(newDoc.getDocDate());
						egovMess.setMsgStatus("DS_REGISTERED");	
						egovMess.setMsgStatusDate(new Date());
						
						new EgovMessagesDAO(getUserData()).save(egovMess);
						
						new MMSVpisvaneDAO(getUserData()).updateStatusReg(DocuConstants.CODE_ZNACHENIE_STATUS_OBEKT_V_RAZGLEJDANE, new Date(), this.mmsSportFormirovanie.getId(), DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_FORMATIONS);						
						
					});					
					
				}
			
			}
		
		} catch (DbErrorException e) {
			LOGGER.error("Грешка при зареждане настройки по вид документ!! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString("ui_beanMessages", ERRDATABASEMSG), e.getMessage());
		
		} catch (BaseException e) {
			LOGGER.error("Грешка при регистриране на вписване", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString("ui_beanMessages", ERRDATABASEMSG), e.getMessage());
		
		} catch (ParseException e) {
			LOGGER.error("Грешка при конвертиране на стринг в дата", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
		}		
		
	}

	/**
	 * @param idDoc
	 * @throws DbErrorException
	 */
	private void copyEgovFiles(Integer idDoc) throws DbErrorException {
		try {
			FilesDAO filesDao = new FilesDAO(getUserData());
			List<EgovMessagesFiles> egovFiles = new EgovMessagesDAO(getUserData()).findFilesByMessage(this.idSSev);
			for (EgovMessagesFiles egovFile : egovFiles) {
				Files f = new Files();
				
				f.setContent(egovFile.getBlobcontent());
				f.setContentType(egovFile.getMime());
				f.setFilename(egovFile.getFilename());
				
				filesDao.saveFileObject(f, idDoc, DocuConstants.CODE_ZNACHENIE_JOURNAL_DOC);
			}
			
		} catch (Exception e) { // TODO за да не счупим цялата работа само ще се логва за сега!
			LOGGER.error("Грешка при копирана на файловете от ЕГОВ!", e);
		}
	}

	public Integer getIdSSev() {
		return idSSev;
	}

	public void setIdSSev(Integer idSSev) {
		this.idSSev = idSSev;
	}

	public Integer getVidDoc() {
		return vidDoc;
	}

	public void setVidDoc(Integer vidDoc) {
		this.vidDoc = vidDoc;
	}

	public String getRegNom() {
		return regNom;
	}

	public void setRegNom(String regNom) {
		this.regNom = regNom;
	}

	public String getDataDoc() {
		return dataDoc;
	}

	public void setDataDoc(String dataDoc) {
		this.dataDoc = dataDoc;
	}

	public String getOtnosno() {
		return otnosno;
	}

	public void setOtnosno(String otnosno) {
		this.otnosno = otnosno;
	}

	public String getEgn() {
		return egn;
	}

	public void setEgn(String egn) {
		this.egn = egn;
	}

	public String getEik() {
		return eik;
	}

	public void setEik(String eik) {
		this.eik = eik;
	}

	public Referent getRef() {
		return ref;
	}

	public void setRef(Referent ref) {
		this.ref = ref;
	}

	public MMSVpisvane getVpisvane() {
		return vpisvane;
	}

	public void setVpisvane(MMSVpisvane vpisvane) {
		this.vpisvane = vpisvane;
	}

	public boolean isNoVp() {
		return noVp;
	}

	public void setNoVp(boolean noVp) {
		this.noVp = noVp;
	}

	public String getMailText() {
		return mailText;
	}

	public void setMailText(String mailText) {
		this.mailText = mailText;
	}

	public boolean isWillShowMailModal() {
		return willShowMailModal;
	}

	public void setWillShowMailModal(boolean willShowMailModal) {
		this.willShowMailModal = willShowMailModal;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public EgovMessagesCoresp getEmcoresp() {
		return emcoresp;
	}

	public void setEmcoresp(EgovMessagesCoresp emcoresp) {
		this.emcoresp = emcoresp;
	}

	public Date getDataDocDate() {
		return dataDocDate;
	}

	public void setDataDocDate(Date dataDocDate) {
		this.dataDocDate = dataDocDate;
	}

	public String getSelectedVidSportTxt() {
		return selectedVidSportTxt;
	}

	public void setSelectedVidSportTxt(String selectedVidSportTxt) {
		this.selectedVidSportTxt = selectedVidSportTxt;
	}

	public List<EgovMessagesFiles> getEgovMessFilesList() {
		return egovMessFilesList;
	}

	public void setEgovMessFilesList(List<EgovMessagesFiles> egovMessFilesList) {
		this.egovMessFilesList = egovMessFilesList;
	}

	public EgovMessages getEgovMess() {
		return egovMess;
	}

	public void setEgovMess(EgovMessages egovMess) {
		this.egovMess = egovMess;
	}

	public List<SelectItem> getMsgStatusList() {
		return msgStatusList;
	}

	public void setMsgStatusList(List<SelectItem> msgStatusList) {
		this.msgStatusList = msgStatusList;
	}

	public List<EgovMessagesFiles> getEgovFilesList() {
		return egovFilesList;
	}

	public void setEgovFilesList(List<EgovMessagesFiles> egovFilesList) {
		this.egovFilesList = egovFilesList;
	}

	public String getReasonOtkaz() {
		return reasonOtkaz;
	}

	public void setReasonOtkaz(String reasonOtkaz) {
		this.reasonOtkaz = reasonOtkaz;
	}

	/**************************************************** END FROM SSEV ****************************************************/	
	
	
	/**
	 * Проверка за заключенo формирование 
	 * @param idObj
	 * @return
	 */
	private boolean checkForLock(Integer idObj) {
		boolean res = true;
		LockObjectDAO daoL = new LockObjectDAO();		
		try { 
			Object[] obj = daoL.check(ud.getUserId(), DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_FORMATIONS, idObj);
			if (obj != null) {
				 res = false;
				 String msg = getSystemData().decodeItem(DocuConstants.CODE_CLASSIF_ADMIN_STR, Integer.valueOf(obj[0].toString()), getUserData().getCurrentLang(), new Date())   
						       + " / " + DateUtils.printDate((Date)obj[1]);
				 JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_WARN, "Формированието е заключено от:", msg);
			}
		} catch (DbErrorException e) {
			LOGGER.error("Грешка при проверка за заключенo формирование! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
		}
		return res;
	}
	
	/**
	 * Заключване на формирование, като преди това отключва всички обекти, заключени от потребителя
	 * @param idObj
	 */
	public void lockDelo(Integer idObj) {	
		
		LockObjectDAO daoL = new LockObjectDAO();		
		try { 
			JPA.getUtil().runInTransaction(() -> 
				daoL.lock(ud.getUserId(), DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_FORMATIONS, idObj, idObedinenie == null ? null : 1)
			);
		} catch (BaseException e) {
			LOGGER.error("Грешка при заключване на формирование! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
		}			
	}

	
	/**
	 * при излизане от страницата - отключва обекта и да го освобождава за актуализация от друг потребител
	 */
	@PreDestroy
	public void unlockDelo(){
        if (!ud.isReloadPage()) {
        	LockObjectDAO daoL = new LockObjectDAO();	
        	try { 
	        	
	        	JPA.getUtil().runInTransaction(() -> 
					daoL.unlock(ud.getUserId())
				);
        	} catch (BaseException e) {
    			LOGGER.error("Грешка при отключване на формирование! ", e);
    			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
    		}
        	ud.setPreviousPage(null);
        	
        }          
        ud.setReloadPage(false);
	}
	
	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public ArrayList<DataSource> getAttachedBytes() {
		return attachedBytes;
	}

	public void setAttachedBytes(ArrayList<DataSource> attachedBytes) {
		this.attachedBytes = attachedBytes;
	}

	public ArrayList<Files> getUploadFilesList() {
		return uploadFilesList;
	}

	public void setUploadFilesList(ArrayList<Files> uploadFilesList) {
		this.uploadFilesList = uploadFilesList;
	}


}
