package com.ib.docu.beans;
import static com.ib.system.SysConstants.CODE_CLASSIF_EKATTE;
import static com.ib.system.SysConstants.CODE_DEFAULT_LANG;

import static com.ib.system.utils.SearchUtils.isEmpty;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import javax.activation.DataSource;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import org.omnifaces.cdi.ViewScoped;
import javax.faces.view.facelets.FaceletContext;
import javax.inject.Named;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.MimeUtility;
import javax.mail.util.ByteArrayDataSource;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.parsers.ParserConfigurationException;
import org.primefaces.PrimeFaces;
import org.primefaces.component.export.PDFOptions;
import org.primefaces.component.row.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
import org.xml.sax.SAXException;

import com.aspose.words.BookmarkCollection;
import com.aspose.words.Document;
import com.aspose.words.DocumentBuilder;
import com.aspose.words.License;
import com.aspose.words.SaveFormat;
import com.ib.docu.db.dao.DocDAO;
import com.ib.docu.db.dao.EgovMessagesDAO;
import com.ib.docu.db.dao.LockObjectDAO;
import com.ib.docu.db.dao.MMSCoachesDAO;
import com.ib.docu.db.dao.MMSVpisvaneDAO;
import com.ib.docu.db.dao.MMSVpisvaneDocDAO;
import com.ib.docu.db.dao.ReferentDAO;
import com.ib.docu.db.dao.RegistraturaDAO;
import com.ib.docu.db.dto.Doc;
import com.ib.docu.db.dto.EgovMessages;
import com.ib.docu.db.dto.EgovMessagesCoresp;
import com.ib.docu.db.dto.EgovMessagesFiles;
import com.ib.docu.db.dto.MMSCoaches;
import com.ib.docu.db.dto.MMSCoachesDiploms;
import com.ib.docu.db.dto.MMSVpisvane;
import com.ib.docu.db.dto.MMSVpisvaneDoc;
import com.ib.docu.db.dto.Referent;
import com.ib.docu.db.dto.ReferentAddress;
import com.ib.docu.db.dto.Registratura;
import com.ib.docu.export.BaseExport;
import com.ib.docu.system.DocuClassifAdapter;
import com.ib.docu.system.DocuConstants;
import com.ib.docu.system.SystemData;
import com.ib.docu.system.UserData;
import com.ib.docu.utils.ParsePdfZaqvlenie;
import com.ib.indexui.customexporter.CustomExpPreProcess;
import com.ib.indexui.pagination.LazyDataModelSQL2Array;
import com.ib.indexui.system.Constants;
import com.ib.indexui.system.IndexUIbean;
import com.ib.indexui.utils.JSFUtils;
import com.ib.system.ActiveUser;
import com.ib.system.SysConstants;
import com.ib.system.db.JPA;
import com.ib.system.db.dao.FilesDAO;
import com.ib.system.db.dto.Files;
import com.ib.system.exceptions.BaseException;
import com.ib.system.exceptions.DbErrorException;
import com.ib.system.exceptions.InvalidParameterException;
import com.ib.system.exceptions.ObjectInUseException;
import com.ib.system.exceptions.ObjectNotFoundException;
import com.ib.system.mail.Mailer;
import com.ib.system.mail.Mailer.Content;
import com.ib.system.model.SysAttrSpec;
import com.ib.system.utils.DateUtils;
import com.ib.system.utils.ValidationUtils;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import com.lowagie.text.pdf.Barcode128;

import bg.government.regixclient.RegixClientException;

@Named(value = "mmsCoach")
@ViewScoped
public class MmsCoach  extends IndexUIbean{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4738857535160458091L;
	/**
	 * 
	 */
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MmsCoach.class);
	private transient MMSCoachesDAO mmsCoachesDAO;
	private transient MMSVpisvaneDAO mmsVpisvaneDAO;
	private transient MMSCoaches mmsCoaches;
	private transient MMSCoachesDiploms mmsDiplom;
	private Referent referent;
	private ReferentAddress refAdress;
	private String infoAdres;
	private SystemData sd;	
	private UserData ud;
	public static final String  COACHFORM = "mmsCoachform";
	private static final String	UIBEANMESSAGES	= "ui_beanMessages";
	private Date decodeDate = new Date();
	private String txtCorrMCI;
	private String egnF=null;
	private String nomLK=null;

	private Integer coachId=null;
	private Integer codeRef=null;
	private Integer vidDocVpisv=null;
	private String vidDocVpisvText;
	private Date dateRNV;
	private transient List<MMSVpisvane> regsListCV;
	private transient LazyDataModelSQL2Array docsListCD;
	
	private int indR;
	private MMSVpisvane lastVpisvane;
	
//	private boolean ccevNV =true;
	
	// Атрибути от Data Model Треньори
	private SysAttrSpec maRegNom = new SysAttrSpec();
	private SysAttrSpec maStatus= new SysAttrSpec();
	private SysAttrSpec maDateStatus= new SysAttrSpec();
	private SysAttrSpec maDopInfo= new SysAttrSpec();
	
	private SysAttrSpec maAddress= new SysAttrSpec();
	private SysAttrSpec maRefNameD= new SysAttrSpec();
	private SysAttrSpec maLnc= new SysAttrSpec();
	private SysAttrSpec maNomDoc= new SysAttrSpec();
	
	// Атрибути от Data Model Треньори - дипломи
	private SysAttrSpec maRegNomD = new SysAttrSpec();
	private SysAttrSpec maYearIssued= new SysAttrSpec();
	private SysAttrSpec maUchZaved= new SysAttrSpec();
	private SysAttrSpec maUchebZavText= new SysAttrSpec();
	private SysAttrSpec maDopInfoD= new SysAttrSpec();
	private SysAttrSpec maVidDocD= new SysAttrSpec();
	private SysAttrSpec maSeriaFabrNomD= new SysAttrSpec();
	// Email
	private String mailText;
	private String subject;
	private static Properties props=new Properties();
	private static Integer ID_REGISTRATURE = 1;
	private static final String MAILBOX="DEFAULT";
	private String reasonCancel;
	private boolean cancelZajavl= false;
	
	private EgovMessages egovMess;
	private EgovMessagesCoresp emcoresp;
	private List<EgovMessagesFiles> egovMessFilesList;
	private HashMap<String, String>  msgStatusHM = new HashMap<String, String>();
	private List<EgovMessagesCoresp> emcorespList;
	private String selOptZajavl=null;
	private boolean LockOk;
	private String viewNe;
	private Integer vidSportN;
//	private boolean saveCD=false;
	private boolean lockSave=false;
	
	private ArrayList<DataSource> attachedBytes = new ArrayList<DataSource>();
	private ArrayList<Files> uploadFilesList = new ArrayList<Files>();

	//ЛМ
	private String names;
	
	// Print envelopes / Deliver Notice
	private ReferentAddress adr=null;
//	private Referent ref=null;//Ima go na 1849 red!!! 
	private Integer formatPlik=null;
	private boolean recommended = false;
	private String corespName=null;
	private String corespTel=null;
	private String corespAddress=null;
	private String corespPostCode=null; 
	private String corespPBox=null;
	private String corespObl=null;
	private String corespNM=null;
	
	private String senderName=null;
	private String senderTel=null;
	private String senderAddress=null;
	private String senderPostCode=null; 
	private String senderPBox=null;
	private String senderObl=null;
	private String senderNM=null;
	private Integer countryBg=null;
	private String corespCountry=null;
	private String senderCountry=null;
	private Integer idRegistratura = null;

	
	
	@PostConstruct
	public void initData() {
	try {
		Integer a = getUserData(UserData.class).getRegistratura();
		mmsCoachesDAO = new MMSCoachesDAO(MMSCoaches.class, ActiveUser.DEFAULT);
		mmsVpisvaneDAO = new MMSVpisvaneDAO(ActiveUser.DEFAULT);

		props = getSystemData(SystemData.class).getMailProp(ID_REGISTRATURE, MAILBOX);
		setUd(getUserData(UserData.class));					
		setSd((SystemData) getSystemData());
		
		getAttrDataModel();
		actionNew();
		
		String idCoach = JSFUtils.getRequestParameter("idObj");		
		
		FaceletContext faceletContext = (FaceletContext) FacesContext.getCurrentInstance().getAttributes().get(FaceletContext.FACELET_CONTEXT_KEY);
		viewNe = (String) faceletContext.getAttribute("viewOnly");

		
		if (ValidationUtils.isNotBlank(idCoach) && ValidationUtils.isNumber(idCoach)) { //Актуализация/Разглеждане от Регистри на менюто
			loadCoachByIdCoach(Integer.valueOf(idCoach));

		} else if (ValidationUtils.isNotBlank(JSFUtils.getRequestParameter("ccevID")) && ValidationUtils.isNumber(JSFUtils.getRequestParameter("ccevID"))) {	
			// Тези параметри ми трябват, за да мога да регистрирам документ в нашата система с техните данни
				this.idSSev = Integer.valueOf(JSFUtils.getRequestParameter("ccevID"));
				
				loadParamsFromZajavl();// get params from Emessages
				
				// проверка за заключен обект EGOVMESSAGE
				LockOk = checkForLock(Integer.valueOf(DocuConstants.CODE_ZNACHENIE_JOURNAL_EGOVMESSAGE), this.idSSev);

				if (LockOk) {// заключване обект EGOVMESSAGE
					lockObjects(Integer.valueOf(DocuConstants.CODE_ZNACHENIE_JOURNAL_EGOVMESSAGE), this.idSSev, 1);

					//OTIVA DA SE OPITA DA PARSNE PDF AKO IMA TAKAV.
					try {
						
						this.mmsCoaches=new ParsePdfZaqvlenie().parseTrener((SystemData) getSystemData(), ud, getCurrentLang(), egovMess, egovMessFilesList);
						
						if (this.mmsCoaches.getId()==null) {
//							this.mmsCoaches.setStatus(DocuConstants.CODE_ZNACHENIE_STATUS_OBEKT_V_RAZGLEJDANE);
//							this.mmsCoaches.setDateStatus(new Date());
							this.mmsCoaches.setStatus(null);
							this.mmsCoaches.setDateStatus(null);
						}
						
						if (this.mmsCoaches.getParseMessages().size()>0) {
							if (this.mmsCoaches.getParseMessages().size() == 1 && this.mmsCoaches.getParseMessages().get(0).contains("Опитвате се да впишете треньор, който съществува с посочения в заявлението вид спорт" ) ) {
								JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, this.mmsCoaches.getParseMessages().get(0));
								this.mmsCoaches.setParseMessages(null);
							} else {
								for (int i = 0; i < this.mmsCoaches.getParseMessages().size(); i++) {
									JSFUtils.addErrorMessage(this.mmsCoaches.getParseMessages().get(i));
								}
							}	
						}
						if (null!=this.mmsCoaches && null!=this.mmsCoaches.getIdObject()) {	
							findReferentByCode(this.mmsCoaches.getIdObject());
						}
						setVidSportN(mmsCoaches.getVidSport());
			
						if (ValidationUtils.isNotBlank(this.mmsCoaches.getMailLice()))
							referent.setContactEmail(this.mmsCoaches.getMailLice());
			
						if (ValidationUtils.isNotBlank(this.mmsCoaches.getMailLice())) {
							
							selOptZajavl="2";	// от пдф файл 
							
							if (null==this.mmsCoaches.getIdObject()) {
								this.lockSave=true;
								return;
							}
								
							if (null!=this.mmsCoaches.getVidSport()) {
								this.vidSportN = this.mmsCoaches.getVidSport();
							}else {
								this.lockSave=true;
								return;
							}
							//if (null==this.mmsCoaches.getDlajnost()) {
								//LOGGER.error("Няма определенa длъжност за лицето! Задължителен параметър!");
								//JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, "Няма определенa длъжност за лицето! Задължителен параметър!")  ;
								//actionNew();
								//return;
							//}

							
//							if (null!=this.mmsCoaches && null!=this.mmsCoaches.getIdObject()) {	
//								setCodeRef(this.mmsCoaches.getIdObject());
//							}
							
						}else {
							selOptZajavl="1";	//Без пдф файл
							
						}
						
						
					} catch (ParserConfigurationException e) {
						JSFUtils.addErrorMessage("Грешка при автоматична обработка на пдф!", e);
						LOGGER.error(e.getMessage(), e);
					} catch (DOMException e) {
						JSFUtils.addErrorMessage("Грешка при автоматична обработка на пдф!", e);
						LOGGER.error(e.getMessage(), e);
					} catch (SAXException e) {
						JSFUtils.addErrorMessage("Грешка при автоматична обработка на пдф!", e);
						LOGGER.error(e.getMessage(), e);
					} catch (IOException e) {
						JSFUtils.addErrorMessage("Грешка при автоматична обработка на пдф!", e);
						LOGGER.error(e.getMessage(), e);
					} catch (RegixClientException e) {
						JSFUtils.addErrorMessage("Грешка при автоматична обработка на пдф!", e);
						LOGGER.error(e.getMessage(), e);
					} catch (DatatypeConfigurationException e) {
						JSFUtils.addErrorMessage("Грешка при автоматична обработка на пдф!", e);
						LOGGER.error(e.getMessage(), e);
					} catch (ParseException e) {
						JSFUtils.addErrorMessage("Грешка при автоматична обработка на пдф!", e);
						LOGGER.error(e.getMessage(), e);
					} catch (Exception e) {
						JSFUtils.addErrorMessage("Грешка при автоматична обработка на пдф!", e);
						LOGGER.error(e.getMessage(), e);
					} 
					
				}else {
					return;
				}

		}else{
//			setCcevNV(false);
//			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, "Липсва Идентификатор на обработваното заявление. Моля, изберете ново заявление!");
		}

		} catch(IllegalArgumentException | DbErrorException  e) {
			LOGGER.error("Грешка при ЧЕТЕНЕ НА MMSCoaches id=  " + JSFUtils.getRequestParameter("idObj"), e);
		} 
		//LM
	
//		if (!isCcevNV()) {
			// Трябва запис в журнала за неподадени идентификатори в ново заявление???
//		}
	}
	
	public void actionNew() {

		coachId=null;
		codeRef=null;
		setTxtCorrMCI("");
		egnF="";
		infoAdres="";
		this.mmsCoaches = new MMSCoaches();
//		this.mmsCoaches.setStatus(DocuConstants.CODE_ZNACHENIE_STATUS_OBEKT_V_RAZGLEJDANE);
//		this.mmsCoaches.setDateStatus(new Date());
		this.mmsCoaches.setCoachesDiploms(new ArrayList<MMSCoachesDiploms>());
		setRegsListCV(new ArrayList<MMSVpisvane>());
		referent = new Referent();
		refAdress = new ReferentAddress();
		this.mmsDiplom = new MMSCoachesDiploms();
		this.lastVpisvane = new MMSVpisvane();
		this.setReasonCancel(null); 
		this.docsListCD = null;
		this.vidSportN = null;
		if (null==this.idSSev) {
			egovMess = new EgovMessages();
			emcoresp = new EgovMessagesCoresp();
			msgStatusHM = new HashMap<String, String>();
		}
		names="";
		corespName="";
	}
	
	
	private void getAttrDataModel() {
		
		try {
			// Main table coaches
			maRegNom = getSystemData().getModel().getAttrSpec("reg_nomer", "coaches", getCurrentLang(), null);
			maStatus=getSystemData().getModel().getAttrSpec("status", "coaches", getCurrentLang(), null);
			maDateStatus=getSystemData().getModel().getAttrSpec("date_status", "coaches", getCurrentLang(), null);
			setMaDopInfo(getSystemData().getModel().getAttrSpec("dop_info", "coaches", getCurrentLang(), null));
			
			maRefNameD = getSystemData().getModel().getAttrSpec("ref_name", "coaches", getCurrentLang(), null);

			maAddress= getSystemData().getModel().getAttrSpec("ref_addrs", "coaches", getCurrentLang(), null);
			maNomDoc= getSystemData().getModel().getAttrSpec("nom_doc", "coaches", getCurrentLang(), null);
			maLnc = getSystemData().getModel().getAttrSpec("fzl_lnc", "coaches", getCurrentLang(), null);
			
			// Join table coaches-diploms
			setMaRegNomD(getSystemData().getModel().getAttrSpec("reg_nomer", "coaches_diploms", getCurrentLang(), null));
			setMaYearIssued(getSystemData().getModel().getAttrSpec("year_issued", "coaches_diploms", getCurrentLang(), null));
			setMaUchZaved(getSystemData().getModel().getAttrSpec("uchebno_zavedenie", "coaches_diploms", getCurrentLang(), null));
			setMaUchebZavText(getSystemData().getModel().getAttrSpec("ucheb_zav_text", "coaches_diploms", getCurrentLang(), null));
			setMaDopInfoD(getSystemData().getModel().getAttrSpec("dop_info", "coaches_diploms", getCurrentLang(), null));
			setMaVidDocD(getSystemData().getModel().getAttrSpec("vid_doc", "coaches_diploms", getCurrentLang(), null));
			setMaSeriaFabrNomD(getSystemData().getModel().getAttrSpec("seria_fabrnom", "coaches_diploms", getCurrentLang(), null));
	
		} catch (DbErrorException | InvalidParameterException e) {
			LOGGER.error( "Грешка при ЧЕТЕНЕ НА атрибутите на Инф. обект Треньори/Дипломи" , e);
		}
		  
	}
	

	/**
	 *  Зарежда данните на инф. Обект за актуализация или разглеждане
	 *   
	 * @param idCoach
	 * @return
	 */
	public void loadCoachByIdCoach(Integer idCoach) {
		
		if (null!=idCoach) {
			// Lock
			if(ValidationUtils.isNotBlank(viewNe) && viewNe.equals("2")) { 
				// проверка за заключен документ
				LockOk = checkForLock(Integer.valueOf(DocuConstants.CODE_ZNACHENIE_JOURNAL_COACHES), idCoach);
				if (LockOk) {
					lockObjects(Integer.valueOf(DocuConstants.CODE_ZNACHENIE_JOURNAL_COACHES), idCoach, 1);
				}else {
					return;
				}
			}
		}
	
		try {
			
			
			
			JPA.getUtil().runWithClose(() -> {// Треньор + Дипломи
				this.mmsCoaches = mmsCoachesDAO.findById(idCoach);	
				if (null!=this.mmsCoaches && null!=this.mmsCoaches.getId()) {
	
					this.mmsCoaches.getCoachesDiploms().size();
					if(null!=this.mmsCoaches && null!=this.mmsCoaches.getIdObject() && null==referent.getId())
						findReferentByCode(this.mmsCoaches.getIdObject());
					
					findVpisvaneCV();
					
					findDocs();
					
				}else {
					LOGGER.error("В базата липсват записани данни за треньор с идентификатор  " + idCoach + " !");
					JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, "В базата липсват записани данни за треньор с идентификатор  " + idCoach + " !")  ;
					return;
				}
			});

			
			
			
					   
		} catch (BaseException e) {
			LOGGER.error("Грешка при зареждане данните на треньор! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
		} catch (Exception e) {
			LOGGER.error("Грешка при зареждане данните на треньор!", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
		}
	}
	
	// От Компонета Вписване
	public void findVpisvaneCV() {
		
		setRegsListCV(new ArrayList<MMSVpisvane>(0));
		try {
			
			JPA.getUtil().runWithClose(() -> {// Вписвания
				regsListCV = mmsVpisvaneDAO.findRegsListByIdAndType(DocuConstants.CODE_ZNACHENIE_JOURNAL_COACHES, this.mmsCoaches.getId());
			});

		} catch (BaseException e) {
			LOGGER.error("Грешка при зареждане данните за вписване! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
		
		} catch (Exception e) {
			LOGGER.error("Грешка при четене и обработка на вписване id=  " + this.mmsCoaches.getId(), e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,"Грешка при четене и обработка на вписване id=  " + this.mmsCoaches.getId(), e.getMessage());
			
		}
	}

	// От Documents
	private void findDocs() {
		try {
			// Документи
			JPA.getUtil().runWithClose(() -> docsListCD=new LazyDataModelSQL2Array((new MMSVpisvaneDAO(getUserData()).findDocsList(this.mmsCoaches.getId(),  DocuConstants.CODE_ZNACHENIE_JOURNAL_COACHES)), " doc_date asc "));
			if (null!=docsListCD) 
				docsListCD.getRowCount();
		
		} catch (BaseException e) {
			LOGGER.error("Грешка при зареждане на документи към вписвания! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, "Грешка при четене на документи към вписване id=  " + this.mmsCoaches.getId(), e.getMessage());
		}catch (Exception e) {
			LOGGER.error("Грешка при четене и обработка на вписване id=  " + this.mmsCoaches.getId(), e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,"Грешка при четене на документи към вписване id=  " + this.mmsCoaches.getId(), e.getMessage());
			
		}
	}
	
		
	public void actionSave() {
		
		if (! checkDataCoach())
			return;
		
		boolean zapZ = true;
		try {
			
			JPA.getUtil().runInTransaction(() -> { this.mmsCoaches = mmsCoachesDAO.save(this.mmsCoaches);	});// Save Coaches with Diploms +Vid Sports
			
			if (null!=this.mmsCoaches.getCoachesDiploms())
				this.mmsCoaches.getCoachesDiploms().size();

		//	JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO,IndexUIbean.getMessageResourceString(UIBEANMESSAGES, SUCCESSAVEMSG));

			// Lock
			if(ValidationUtils.isNotBlank(viewNe) && viewNe.equals("2") && null!=this.mmsCoaches.getId()) { 
				
				
				// проверка за заключен документ
				LockOk = checkForLock(Integer.valueOf(DocuConstants.CODE_ZNACHENIE_JOURNAL_COACHES), this.mmsCoaches.getId());
				if (LockOk) {
					lockObjects(Integer.valueOf(DocuConstants.CODE_ZNACHENIE_JOURNAL_COACHES), this.mmsCoaches.getId(), 1);
				}	
				
			}
			
			//TODO - ако идва от СЕОС след първия запис се вика метода за запис на вписване, документ и промяна на статуса в EgovMessages
			if (this.idSSev != null) {//selOptZajavl.equals("2") -> Параметър Само 1 треньор на заявление	/ selOptZajavl.equals("1") -> Параметър Има и други треньори в заявлението	!!!!!!				
				if (null!=this.vidSportN)
					this.mmsCoaches.setVidSport(this.vidSportN);
				
			     zapZ = 	actionSaveDocFromSeos();

				// Да зареди вписванията и документите, като запише нови
				findVpisvaneCV(); 				
				findDocs();
//				if (selOptZajavl.equals("1")) {
//					actionNew();
//				}else {
//					this.idSSev = null;
//				}
			}
			
			if (zapZ)  
				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO,IndexUIbean.getMessageResourceString(UIBEANMESSAGES, SUCCESSAVEMSG));
			else
				throw new Exception();
			
		} catch (ObjectInUseException e) {
			LOGGER.error("Грешка при запис/изтриване на треньор от базата данни!", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
			JPA.getUtil().rollback();
		} catch (DbErrorException e) {
			LOGGER.error(e.getMessage(), e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());	
			JPA.getUtil().rollback();
		} catch (BaseException e) {			
			LOGGER.error("Грешка при запис/изтриване на треньор! ", e);	
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
			JPA.getUtil().rollback();
		} catch (Exception e) {
			LOGGER.error("Грешка при запис/изтриване на треньор!", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
			JPA.getUtil().rollback();
		}finally {
			JPA.getUtil().closeConnection();
		}
	}
	
	
	public void actionDelete() {
		try {
			JPA.getUtil().runInTransaction(() -> {
				
				mmsCoachesDAO.deleteFromRegister(this.mmsCoaches.getId(), getSd());
				 
			});
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO,  IndexUIbean.getMessageResourceString(UI_beanMessages, "general.successDeleteMsg") );
			actionNew();
		} catch (BaseException e) {
			JSFUtils.addErrorMessage(getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e);
			LOGGER.error(e.getMessage(), e);
			JPA.getUtil().rollback();
		}finally {
			JPA.getUtil().closeConnection();
		}
	}
	
	
	/**
	 * проверка за задължителни полета при запис на Треньор
	 * @throws DbErrorException 
	 */
	public boolean checkDataCoach() {
		boolean flagSave = true;	
		if(maRefNameD.isActive() && maRefNameD.isRequired()) {
			if(this.mmsCoaches.getIdObject()==null) {
				JSFUtils.addMessage(COACHFORM+":refCorrCI",FacesMessage.SEVERITY_ERROR,getMessageResourceString(beanMessages, "mmsCoach.noLice"));
				flagSave = false;
			}
		}
//		if(maRegNom.isActive() && maRegNom.isRequired()) {
//			if(this.mmsCoaches.getRegNomer() == null) {
//				JSFUtils.addMessage(COACHFORM+":idND",FacesMessage.SEVERITY_ERROR,getMessageResourceString(UI_beanMessages,"general.pleaseInsert", getMessageResourceString(beanMessages, "mmsCoach.regNomCoach")));
//				flagSave = false;
//			}
//		}

//		if(getMaStatus().isActive() && getMaStatus().isRequired()) {
//			if(this.mmsCoaches.getStatus() == null) {
//				JSFUtils.addMessage(COACHFORM+":idStatusCV",FacesMessage.SEVERITY_ERROR,getMessageResourceString(UI_beanMessages,"general.pleaseInsert","Статус вписване"));
//				flagSave = false;
//			}
//		}
//		if(getMaDateStatus().isActive() && getMaDateStatus().isRequired()) {
//			if(this.mmsCoaches.getDateStatus() == null) {
//				JSFUtils.addMessage(COACHFORM+":idDateStatusCV",FacesMessage.SEVERITY_ERROR,getMessageResourceString(UI_beanMessages,"general.pleaseInsert","Дата статус вписване"));
//				flagSave = false;
//			}
//		}


		if(getMaDopInfo().isActive() && getMaDopInfo().isRequired()) {
			if(this.mmsCoaches.getDopInfo() == null || this.mmsCoaches.getDopInfo().trim().isEmpty()) {
				JSFUtils.addMessage(COACHFORM+":idDopInfoC",FacesMessage.SEVERITY_ERROR,getMessageResourceString(UI_beanMessages,"general.pleaseInsert","Допълнителна информация"));
				flagSave = false;
			}
		}
		
		if (null==this.vidSportN && this.idSSev != null) {
			LOGGER.error("Няма определен вид спорт за лицето! Задължителен параметър!");
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, "Няма определен вид спорт за лицето! Задължителен параметър!")  ;
			flagSave = false;
		}
//			if (null==this.mmsCoaches.getDlajnost() && selOptZajavl=="1") {
//				LOGGER.error("Няма определенa длъжност за лицето! Задължителен параметър!");
//				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, "Няма определенa длъжност за лицето! Задължителен параметър!")  ;
//				flagSave = false;
//			}

		return flagSave;
	}
	
	// Обработка на заявления
	public void loadParamsFromZajavl() {//Parametri ot zajavlenie

		try {
			setVidDocVpisvText("");

			egovMess = new EgovMessages();
			emcoresp = new EgovMessagesCoresp();
			
			
			JPA.getUtil().runWithClose(() -> {
				
				ArrayList<Object[]> tmpList = new EgovMessagesDAO(getUserData()).createMsgTypesList();
				
				tmpList = new EgovMessagesDAO(getUserData()).createMsgStatusList();
			
				if(tmpList !=null && !tmpList.isEmpty()){
					for(Object[] item:tmpList) {
						if(item != null && item[0]!=null && item[1]!=null){
							msgStatusHM.put(item[0].toString(), item[1].toString());
						}
					}
				}
				
				egovMessFilesList = new EgovMessagesDAO(getUserData()).findFilesByMessage(this.idSSev);
				egovMess = new EgovMessagesDAO(getUserData()).findById(this.idSSev);
				
				emcorespList = (List<EgovMessagesCoresp>) mmsCoachesDAO.findCorespByIdMessage(this.idSSev);
				
				
				if(emcorespList != null && emcorespList.size()>0) {
					for (EgovMessagesCoresp item : emcorespList) {
						if(item != null && (ValidationUtils.isNotBlank(item.getEgn()) || ValidationUtils.isNotBlank(item.getIdCard()))) {
							emcoresp = item;
							break;
						}
					}
					
					if (emcoresp==null)
						emcoresp=emcorespList.get(0);
					
					// Za vpisvanijata
					this.egn=emcoresp.getEgn();
					this.eik = emcoresp.getBulstat();
			
	
				}
				
				if (null!=egovMess) {
					
					if(ValidationUtils.isNotBlank(egovMess.getDocVid()) && ValidationUtils.isNumber(egovMess.getDocVid())) { //Взема Вид заявление - вписване, заличаване, промяна обстоятелства
						this.vidDoc=Integer.valueOf(egovMess.getDocVid());

						
						switch (this.vidDoc.intValue()) {
	
							case DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_TREN_KADRI: 
								setVidDocVpisvText("Заявление за вписване");
								break;
							case DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_ZALICH_TREN_KADRI: 
								setVidDocVpisvText("Заявление за заличаване");
								break;
							case DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_PROM_TREN_KADRI: 
								setVidDocVpisvText("Заявление промяна обстоятелства");
								break;
						}
	
					}else{
						this.vidDoc=null;
					}
					
					// Za vpisvanijata
					this.regNom = egovMess.getDocRn();
					this.dataDoc = egovMess.getDocDate();
					setDateRNV(this.dataDoc);
					this.otnosno = egovMess.getDocSubject();
					
				}
				
				
				/*
				
				Integer validEGN =null;
				Integer validLNC =null;
				
				
				if(emcoresp == null) {
					LOGGER.error("Липсват ЕГН/ЛНЧ/Номер документ за самоличност на лицето от "+getVidDocVpisvText()+"! Моля, изберете ново заявление!");
					JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, "Липсва ЕГН/ЛНЧ/Номер документ за самоличност на лицето от "+getVidDocVpisvText()+"! Моля, изберете ново заявление!");
					setCcevNV(false);
					// Тук се изпълнява метод за смяна на статуса на заявлението в на „оставено без последствие“ 
				}else {
					if(ValidationUtils.isNotBlank(emcoresp.getEgn()) && ValidationUtils.isValidEGN(emcoresp.getEgn())){//ЕГН
						validEGN=1;
						findReferentByEGN(emcoresp.getEgn());
						
						if (null==referent) {
							setTxtCorrMCI(emcoresp.getEgn());
							PrimeFaces.current().executeScript("PF('mCorrCNE').show();");
						}
						
					}else if (ValidationUtils.isNotBlank(emcoresp.getEgn()) && ValidationUtils.isValidLNCH(emcoresp.getEgn())){//ЛНЧ
						validLNC =1;
						findReferentByLNCH(emcoresp.getEgn());
						
						if (null==referent) {
							setTxtCorrMCI(emcoresp.getEgn());
							PrimeFaces.current().executeScript("PF('mCorrCNE').show();");
						}
						
					}else if (ValidationUtils.isNotBlank(emcoresp.getIdCard())){ //Док.самоличност
						referent.setNomDoc(emcoresp.getIdCard());
						findReferentByLK();
					}
					
					
					
					if (null==referent || referent.getCode()==null) {
						if (null==validEGN) {
							if(!ValidationUtils.isNotBlank(emcoresp.getEgn())) {
								JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, "Липсва ЕГН/ЛНЧ на лицето от "+getVidDocVpisvText()+"!");
							}else {		
								JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, "Невалидно ЕГН/ЛНЧ на лицето от "+getVidDocVpisvText()+"!");
							}
						}	
						
						
						JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, "Моля, изберете ново заявление!");
						setCcevNV(false);
						
						// Тук се изпълнява метод за смяна на статуса на заявлението в на „оставено без последствие“ 
					}
		
				}
				
				*/

				
			});
		
			
		} catch (DbErrorException e) {
			LOGGER.error(e.getMessage(), e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
		} catch (BaseException e) {
			LOGGER.error("Грешка при зареждане на данни от Заявление за вписване!", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,"Грешка при зареждане на данни от "+getVidDocVpisvText()+"!", e.getMessage());
			return;
		} catch (Exception e) {
			LOGGER.error("Грешка при зареждане данни от Заявление за вписване!", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,"Грешка при зареждане на данни от "+getVidDocVpisvText()+"!", e.getMessage());
			return;
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
	
	
	public void cancelZajavl() {
		
		if (!ValidationUtils.isNotBlank(getReasonCancel())) {
			JSFUtils.addMessage(COACHFORM+":idReason",FacesMessage.SEVERITY_ERROR,"Моля, Въведете мотивите за отказ обработката на заявлението!");
			cancelZajavl=false;
		}else {
		
			if (null!=getEgovMess()) {
		
				try {
				
					
					JPA.getUtil().runInTransaction(() -> { 
			
						//TODO - Сменя статуса на заявлението в egov_messages при отказ за обработка- 

							getEgovMess().setMsgStatus("DS_REJECTED");
							getEgovMess().setMsgStatusDate(new Date());

							getEgovMess().setCommError(getReasonCancel());
							
							
							new EgovMessagesDAO(getUserData()).save(egovMess);
							cancelZajavl=true;
	
					});	
					
					JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO,"Успешна смяна стауса на заявление при отказ!");
					
				
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
		}
	}
	
	
	public void selectOneManyCoaches(String selOpt) {
		
		if (null==this.vidSportN && this.idSSev != null) {
			LOGGER.error("Няма определен вид спорт за лицето! Задължителен параметър!");
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, "Няма определен вид спорт за лицето! Задължителен параметър!")  ;
			return;
		}	
		selOptZajavl=selOpt;
		actionSave();

	}

	public void closeModalMail() {
		PrimeFaces.current().executeScript("PF('eMail').hide();");
	}
	
	public void sendMail() {
        boolean sending = true;
        if(getSubject() == null || getSubject().trim().isEmpty()) {
               JSFUtils.addMessage("mmsCoachform:subject", FacesMessage.SEVERITY_ERROR,
            		   getMessageResourceString(UI_beanMessages, "general.pleaseInsert", getMessageResourceString(LABELS, "general.otnosno")));
               sending = false;
        }
        if(getMailText() == null || getMailText().trim().isEmpty()) {
               JSFUtils.addMessage("mmsCoachform:mailText", FacesMessage.SEVERITY_ERROR,
                        getMessageResourceString(UI_beanMessages, "general.pleaseInsert", getMessageResourceString(LABELS, "general.text")));
               sending = false;
        }
        if(!sending)
               return;
        Mailer mailer = new Mailer();
        attachedBytes.clear();
		for (Files upLoadedFile : uploadFilesList) {
			attachedBytes.add(new ByteArrayDataSource(upLoadedFile.getContent(), ""));
		}
        try {
        		mailer.sent(Content.PLAIN, getProps(), getProps().getProperty("user.name"), getProps().getProperty("user.password"),
                       getProps().getProperty("mail.from"), "Министерство на младежта и спорта",
                       referent.getContactEmail(), getSubject(), getMailText(), attachedBytes);

               JSFUtils.addInfoMessage("Успешно изпращане на съобщението!");
               setSubject("");
               setMailText("");
                attachedBytes.clear();
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
	



	public MMSCoaches getMmsCoaches() {
		return mmsCoaches;
	}

	public void setMmsCoaches(MMSCoaches mmsCoaches) {
		this.mmsCoaches = mmsCoaches;
	}

	public String getInfoAdres() {
		return infoAdres;

	}

	public void setInfoAdres(String infoAdres) {
		this.infoAdres = infoAdres;
	}
	

	public Referent getReferent() {
		return referent;
	}

	public void setReferent(Referent referent) {
		this.referent = referent;
	}

	public ReferentAddress getRefAdress() {
		return refAdress;
	}

	public void setRefAdress(ReferentAddress refAdress) {
		this.refAdress = refAdress;
	}
	
	public void loadCoachByCodeRef(Integer codRef) {
		try {
			JPA.getUtil().runWithClose(() -> {

				coachId = mmsCoachesDAO.findByArg(null,codRef);	
				this.mmsCoaches.setIdObject(codRef);
				
				if (null!=coachId) {
					loadCoachByIdCoach(coachId);
				
				}else {
					findReferentByCode(codeRef);
					if (null!=referent.getCode())
						loadInfoAdres(referent.getCode());
				}
				
			});
		
		} catch (BaseException e) {
			LOGGER.error("Грешка при зареждане данните на треньор! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
		}catch (Exception e) {
			LOGGER.error("Грешка при зареждане данните на треньор!", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
		}
		
		
	}
	
	
	public void findReferentByCode(Integer idR) throws DbErrorException {
		
		try {
			
			if(idR != null) {
				JPA.getUtil().runWithClose(() -> referent = new ReferentDAO(getUserData()).findByCodeRef(idR));
			}else {
				return;
			}
			
			if(referent != null) {
				this.mmsCoaches.setIdObject(referent.getCode());
//				egnF=referent.getFzlEgn();
//				lnchF=referent.getFzlLnc();
				codeRef = referent.getCode();
				txtCorrMCI=referent.getRefName();
				if (null!=referent.getCode())
					loadInfoAdres(referent.getCode());
			}

			LOGGER.debug("load initRefCorresp");
		} catch (BaseException e) {
			LOGGER.error("Грешка при зареждане на данни за лице! ", e);
		}catch (Exception e) {
			LOGGER.error("Грешка при зареждане данните на треньор!", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
		}

	}
	
	// Търсене на лици по док. самоличност
	public void findReferentByLK() {
		
		if(!ValidationUtils.isNotBlank(referent.getNomDoc())) {
//			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, "Невалиден Номер документ за самоличност на лицето!");
			return;
		}
		try {

			JPA.getUtil().runWithClose(() -> {
				List<Object[]> refer = new ReferentDAO(getUserData()).findByNomDoc(referent.getNomDoc());
				
				
				if(null==refer || refer.size()<1) {
					JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, "Няма намерено лице!");
					return;
				}
				
				if (null!=Integer.valueOf(refer.get(0)[0].toString())) { 
					codeRef=Integer.valueOf(refer.get(0)[0].toString());
					findReferentByCode(codeRef);
					if (null!=referent.getCode())
						loadInfoAdres(referent.getCode());
					loadCoachByCodeRef(codeRef);
				}else {
					JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, "Няма намерено физ. лице по документ за самоличност!");
				}
				
			});
				
		} catch (BaseException e) {
			LOGGER.error("Грешка при зареждане на данни за лице! ", e);
		}catch (Exception e) {
			LOGGER.error("Грешка при зареждане на данни за лице!", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
		}
		
	}
	

	
	public void findReferentByEGN(String egnF) {
		
		try {
			
			referent = new ReferentDAO(getUserData()).findByIdent(null, egnF, null, DocuConstants.CODE_ZNACHENIE_REF_TYPE_FZL);
			if(referent != null) {
				if (null!=referent.getCode())
					loadInfoAdres(referent.getCode());
				setCodeRef(referent.getCode());
				setTxtCorrMCI(referent.getRefName());
				this.mmsCoaches.setIdObject(referent.getCode());
				coachId = mmsCoachesDAO.findByArg(null, referent.getCode());
				if (null!=coachId)
					loadCoachByIdCoach(coachId);
				
				
			}
					
			} catch (DbErrorException e) {
				LOGGER.error(e.getMessage(), e);
				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());	
			} catch (Exception e) {
				LOGGER.error("Грешка при зареждане данните на физическо лице!", e);
				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
			}
		
	}
	

	public void findReferentByLNCH(String lnchF) {
			
		try {
			
			referent = new ReferentDAO(getUserData()).findByIdent(null, null, lnchF, DocuConstants.CODE_ZNACHENIE_REF_TYPE_FZL);
			
			if(referent != null) {
				if (null!=referent.getCode())
					loadInfoAdres(referent.getCode());
				setCodeRef(referent.getCode());
				setTxtCorrMCI(referent.getRefName());
				this.mmsCoaches.setIdObject(referent.getCode());
				coachId = mmsCoachesDAO.findByArg(null, referent.getCode());
				if (null!=coachId)
					loadCoachByIdCoach(coachId);
			}

		} catch (DbErrorException e) {
			LOGGER.error(e.getMessage(), e);
			JSFUtils.addMessage("mmsCoachform:idLNC",FacesMessage.SEVERITY_ERROR, getMessageResourceString(beanMessages, "opis.noResuls"));	
		} catch (Exception e) {
			LOGGER.error("Грешка при зареждане данните на лице", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
		}
	
	}
	
	
	
	/**
	 * зарежда адреса на треньора
	 */
	public void loadInfoAdres(Integer cRef) {

			// заради достъпа до личните данни - в допълнителната информаиця за физическите лица да остане само населеното място!!
			try {				
				infoAdres = getSd().decodeItemDopInfo(DocuConstants.CODE_CLASSIF_REFERENTS, cRef, getCurrentLang(), new Date());
				if(infoAdres != null &&
					(int) getSd().getItemSpecific(DocuConstants.CODE_CLASSIF_REFERENTS, cRef,  getCurrentLang(), decodeDate, DocuClassifAdapter.REFERENTS_INDEX_REF_TYPE) == DocuConstants.CODE_ZNACHENIE_REF_TYPE_FZL) {
				
					if(!getUserData().hasAccess(DocuConstants.CODE_CLASSIF_DEF_PRAVA, DocuConstants.CODE_ZNACHENIE_DEF_PRAVA_SEE_PERSONAL_DATA) ) {
						// да остане само град или село  
						int i1 = infoAdres.indexOf("гр.");
						if(i1 == -1) {
							i1 = infoAdres.indexOf("с.");
						}
						if(i1 != -1) {						
							int i2 = infoAdres.indexOf(", ", i1);
							if(i2 != -1) {
								infoAdres = infoAdres.substring(i1, i2);
							}else {
								// има само град или село...
								infoAdres = infoAdres.substring(i1);
							}
						}else {
							infoAdres = null;
						}
					}else { // да махна ЕГН, за да остане само адреса
						int i1 = infoAdres.indexOf("ЕГН");
						if(i1 != -1) {	
							//има егн
							int i2 = infoAdres.indexOf(", ", i1);
							if(i2 != -1) {
								infoAdres = infoAdres.substring(i2+1);
							}else {
								infoAdres = null; // има само егн...
							}
						}
					}
				}	
				
				// Ново изискване на КК добавяне на тел.+ email	
				if (null==infoAdres) {
					JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO,"За български физически и юридически лица задължително трябва да е въведен адрес за тях в таблица за кореспонденти!");
					infoAdres="";
				}	
				infoAdres+=" /";		
				if (ValidationUtils.isNotBlank(referent.getContactPhone()))
					infoAdres += " тел. "+referent.getContactPhone();
				
				if (ValidationUtils.isNotBlank(this.mmsCoaches.getMailLice())) {
					infoAdres += " email "+this.mmsCoaches.getMailLice();
					referent.setContactEmail(this.mmsCoaches.getMailLice());
				}else {
					if (ValidationUtils.isNotBlank(referent.getContactEmail()))
						infoAdres += " email "+referent.getContactEmail();
				}
				//********
		        if (referent.getContactEmail() != null && referent.getContactEmail().trim().isEmpty()) referent.setContactEmail(null);
			
			
			} catch (Exception e) {
				LOGGER.error("Грешка при формиране на адрес на треньора за показване! ", e);
			}
		
	}
	
	public MMSCoachesDiploms getMmsDiplom() {
		return mmsDiplom;
	}

	public void setMmsDiplom(MMSCoachesDiploms mmsDiplom) {
		this.mmsDiplom = mmsDiplom;
	}
	
	
	/**
	 * проверка за задължителни полета при запис на Треньор - диплома
	 * 
	 */
	public boolean checkCoachDipl() {
		boolean flagSave = true;	
		try {
			
			if(getMaRegNomD().isActive() && getMaRegNomD().isRequired()) {
				if(this.mmsDiplom.getRegNomer() == null || this.mmsDiplom.getRegNomer().trim().isEmpty()) {
					JSFUtils.addMessage("mmsCoachform:idRND",FacesMessage.SEVERITY_ERROR,"Въведете Рег.№ на документа");
					flagSave = false;
				}
				
				
			}
			
			if(getMaYearIssued().isActive()) {
				if(null!=this.mmsDiplom.getYearIssued()) {
					if (! ValidationUtils.isNumber((this.mmsDiplom.getYearIssued().toString())) || this.mmsDiplom.getYearIssued()<1){
						JSFUtils.addMessage("mmsCoachform:idYear",FacesMessage.SEVERITY_ERROR,"Годината на издаване трябва да бъде положително число");
						this.mmsDiplom.setYearIssued(null);
						flagSave = false;
					}
				}else if (getMaYearIssued().isRequired()){
					JSFUtils.addMessage("mmsCoachform:idYear",FacesMessage.SEVERITY_ERROR,"Въведете Година на издаване");
					flagSave = false;
				}
			}
			
			if((getMaUchZaved().isActive() && getMaUchZaved().isRequired()) || (getMaUchebZavText().isActive() && getMaUchebZavText().isRequired()) ) {
				if(null==this.mmsDiplom.getUchebnoZavedenie() && (null==this.mmsDiplom.getUchebZavText() || isEmpty(this.mmsDiplom.getUchebZavText()))) {
					JSFUtils.addMessage("mmsCoachform:idUZ",FacesMessage.SEVERITY_ERROR,"Въведете Институция издала или Институция издала - текст");
					flagSave = false;
				}
				
				if(null!=this.mmsDiplom.getUchebnoZavedenie() && (null!=this.mmsDiplom.getUchebZavText() && !isEmpty(this.mmsDiplom.getUchebZavText()))) {
					JSFUtils.addMessage("mmsCoachform:idUZ",FacesMessage.SEVERITY_ERROR,"Въведете само една Институция издала или Институция издала - текст");
					flagSave = false;
				}
			}
			
			if(getMaVidDocD().isActive() && getMaVidDocD().isRequired()) {
				if(null==this.mmsDiplom.getVidDoc()) {
					JSFUtils.addMessage("mmsCoachform:idVidObrZenz",FacesMessage.SEVERITY_ERROR,"Въведете Вид документ за образователен ценз");
					flagSave = false;
				}
			}
			
			if(getMaDopInfoD().isActive() && getMaDopInfoD().isRequired()) {
				if(null==this.mmsDiplom.getDopInfo()) {
					JSFUtils.addMessage("mmsCoachform:idDopD",FacesMessage.SEVERITY_ERROR,"Въведете Допълнителна информация");
					flagSave = false;
				}
			}
			
			if(getMaSeriaFabrNomD().isActive() && getMaSeriaFabrNomD().isRequired()) {
				if(null==this.mmsDiplom.getSeriaFnom()) {
					JSFUtils.addMessage("mmsCoachform:idSFND",FacesMessage.SEVERITY_ERROR,"Въведете Серия Фабр. номер");
					flagSave = false;
				}
			}

			
			if (this.mmsCoaches.getCoachesDiploms().size()<1)
				return flagSave;
			
			if (flagSave) {	
				for (int i = 0; i < this.mmsCoaches.getCoachesDiploms().size(); i++) {
					
					if (i != this.indR&&this.mmsCoaches.getCoachesDiploms().get(i).getRegNomer().toUpperCase().equals(this.mmsDiplom.getRegNomer().toUpperCase()) 
						&& this.mmsCoaches.getCoachesDiploms().get(i).getYearIssued().equals(this.mmsDiplom.getYearIssued())) {
						JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,"Не се допуска дублиране на Документи за завършено образование и придобита квалификация с еднакви Рег.номер и дата на издаване!");
						flagSave = false;
					}	
				}
			}
			
		}catch (Exception e) {
			LOGGER.error("Грешка при добавяне/редактиране на Документ за завършено образование и придобита квалификация!", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
		}
		
		return flagSave;
	}
	
	
	public void newDiploma() {// Въвеждане на нова диплома
		this.mmsDiplom = new MMSCoachesDiploms();
		this.mmsDiplom.setIdCoaches(this.mmsCoaches.getId());
		this.indR = -1;
	}
	
	public void addDipl() {
		
		if (! checkCoachDipl())
			return;
		
		if (null==this.mmsDiplom.getIdCoaches())
			this.mmsDiplom.setIdCoaches(this.mmsCoaches.getId());
			
		if (-1==this.indR){
			this.mmsCoaches.getCoachesDiploms().add(this.mmsDiplom); 
		}else{
			this.mmsCoaches.getCoachesDiploms().set(this.indR, this.mmsDiplom); 
		}

	
		String  jsDC = "PF('coachDiplNew').hide();";
		PrimeFaces.current().executeScript(jsDC);
	}
	
	public void removeDiplom(MMSCoachesDiploms item) {
		this.mmsCoaches.getCoachesDiploms().remove(item);
	}
	
	public void editDiploma(int ind){
		this.mmsDiplom=new MMSCoachesDiploms();
		this.indR = ind;
		this.mmsDiplom=this.mmsCoaches.getCoachesDiploms().get(this.indR);
	}
	
	
	
	// Check for lock objects
	/**
	 * Проверка за заключена дейност "Актуализация на регистър Треньори" 
	 * @param Integer objType
	 * @param Integer objId
	 * @return
	 */
	private boolean checkForLock(Integer objType, Integer objId) {
		boolean res = true;
		LockObjectDAO daoL = new LockObjectDAO();		
		try { 
			Object[] obj = daoL.check(ud.getUserId(), objType, objId);
			if (obj != null) {
				 res = false;
				 
				 String msg = getSystemData().decodeItem(DocuConstants.CODE_CLASSIF_ADMIN_STR, Integer.valueOf(obj[0].toString()), getUserData().getCurrentLang(), new Date())   
						       + " / " + DateUtils.printDate((Date)obj[1]);
				 JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_WARN,getMessageResourceString(LABELS, "docu.admStrLocked"), msg);
			}
		} catch (DbErrorException e) {
			LOGGER.error("Грешка при проверка за заключена страница! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
		}
		return res;
	}
	
	
	/**
	 * Заключване на регистър треньори, като преди това отключва всички обекти, заключени от потребителя
	 * @param idObj
	 * @param Integer objId
	 * @param unlockTip
	 */
	public void lockObjects(Integer objType, Integer objId, Integer unlockTip) {	
		
		LockObjectDAO daoL = new LockObjectDAO();		
		try { 
			JPA.getUtil().runInTransaction(() -> 
				daoL.lock(ud.getUserId(), objType, objId, unlockTip)
			);
		} catch (BaseException e) {
			LOGGER.error("Грешка при заключване на Треньори! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
		}			
	}
	/**
	 * при излизане от страницата - отключва обекта и да го освобождава за актуализация от друг потребител
	 */
	
	@PreDestroy
	public void unlockObjects(){
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
	
	
	

	public Date getDecodeDate() {
		return decodeDate;
	}

	public void setDecodeDate(Date decodeDate) {
		this.decodeDate = decodeDate != null ? new Date(decodeDate.getTime()) : null;
	}

	public String getTxtCorrMCI() {
		return txtCorrMCI;
	}

	public void setTxtCorrMCI(String txtCorrMCI) {
		this.txtCorrMCI = txtCorrMCI;
	}
	
	public String getEgnF() {
		return egnF;
	}

	public void setEgnF(String egnF) {
		this.egnF = egnF;
	}

	
	public Integer getCoachId() {
		return coachId;
	}

	public void setCoachId(Integer coachId) {
		this.coachId = coachId;
	}

	
	public Integer getCodeRef() {
		return codeRef;
	}

	public void setCodeRef(Integer codeRef) {
		this.codeRef = codeRef;
		if (null!=codeRef) 
			loadCoachByCodeRef(codeRef);
		
	}

	public UserData getUd() {
		return ud;
	}

	public void setUd(UserData ud) {
		this.ud = ud;
	}


	

	public LazyDataModelSQL2Array getDocsListCD() {
		return docsListCD;
	}

	public LazyDataModelSQL2Array setDocsListCD(LazyDataModelSQL2Array docsListCD) {
		this.docsListCD = docsListCD;
		return docsListCD;
	}

	
	public int getIndR() { 
		 return indR; 
	}
	 
	 public void setIndR(int indR) {
		 this.indR = indR; 
	}

	public List<MMSVpisvane> getRegsListCV() {
		return regsListCV;
	}

	public void setRegsListCV(List<MMSVpisvane> regsListCV) {
		this.regsListCV = regsListCV;
	}
	
/******************************** EXPORTS **********************************/
	
	/**
	 * за експорт в excel - добавя заглавие и дата на изготвяне на справката и др.
	 */
	public void postProcessXLS(Object document) {
		
		Object[] dopInfoT=getReportTitles();
		if (null==dopInfoT)
			return;
		String title = "";
		if (null!=dopInfoT[0])
			title += dopInfoT[0].toString();	
		
		Object[] dopInfo=new Object[2];
		if (null!=dopInfoT[1])
			dopInfo[0]=dopInfoT[1];
		if (null!=dopInfoT[2])
			dopInfo[1]=dopInfoT[2];
			
		new CustomExpPreProcess().postProcessXLS(document, title, dopInfo, null, null);	
     
	}

	/**
	 * за експорт в pdf - добавя заглавие и дата на изготвяне на справката 
	 */
	public void preProcessPDF(Object document) {
		
		Object[] dopInfoT=getReportTitles();

		if (null==dopInfoT)
			return;
		String title = "";
		if (null!=dopInfoT[0])
			title += dopInfoT[0].toString();	
		
		Object[] dopInfo=new Object[2];
		
		if (null!=dopInfoT[1])
			dopInfo[0]=dopInfoT[1];
		if (null!=dopInfoT[2])
			dopInfo[1]=dopInfoT[2];
		
		try{
			
			new CustomExpPreProcess().preProcessPDF(document, title, dopInfo, null, null);		
						
		} catch (IOException e) {
			LOGGER.error(e.getMessage(),e);	
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, "Грешка при експорт на справка");
		}
	}
	

	/**
	 * за експорт в pdf 
	 * @return
	 */
	public PDFOptions pdfOptions() {
		PDFOptions pdfOpt = new CustomExpPreProcess().pdfOptions(null, null, null);
        return pdfOpt;
	}
	
	
	public Object[] getReportTitles() {
		
		Object[] dopInfoT=new Object[3];
		
			String title = "Треньор"+": "+ referent.getRefName();
			dopInfoT[0]=title;
			
		//	if (null!=referent.getRefName())
		//		dopInfoT[1]="Треньор"+": "+ referent.getRefName();
		 	
		
		return dopInfoT;
	}

	public MMSVpisvane getLastVpisvane() {
		return lastVpisvane;
	}

	public void setLastVpisvane(MMSVpisvane lastVpisvane) {
		this.lastVpisvane = lastVpisvane;
	}

	public SysAttrSpec getMaRegNom() {
		return maRegNom;
	}

	public void setMaRegNom(SysAttrSpec maRegNom) {
		this.maRegNom = maRegNom;
	}

	public SysAttrSpec getMaStatus() {
		return maStatus;
	}

	public void setMaStatus(SysAttrSpec maStatus) {
		this.maStatus = maStatus;
	}

	public SysAttrSpec getMaDateStatus() {
		return maDateStatus;
	}

	public void setMaDateStatus(SysAttrSpec maDateStatus) {
		this.maDateStatus = maDateStatus;
	}

	public SysAttrSpec getMaDopInfo() {
		return maDopInfo;
	}

	public void setMaDopInfo(SysAttrSpec maDopInfo) {
		this.maDopInfo = maDopInfo;
	}

	public SysAttrSpec getMaRegNomD() {
		return maRegNomD;
	}

	public void setMaRegNomD(SysAttrSpec maRegNomD) {
		this.maRegNomD = maRegNomD;
	}

	public SysAttrSpec getMaYearIssued() {
		return maYearIssued;
	}

	public void setMaYearIssued(SysAttrSpec maYearIssued) {
		this.maYearIssued = maYearIssued;
	}

	public SysAttrSpec getMaUchZaved() {
		return maUchZaved;
	}

	public void setMaUchZaved(SysAttrSpec maUchZaved) {
		this.maUchZaved = maUchZaved;
	}

	public SysAttrSpec getMaUchebZavText() {
		return maUchebZavText;
	}

	public void setMaUchebZavText(SysAttrSpec maUchebZavText) {
		this.maUchebZavText = maUchebZavText;
	}

	public SysAttrSpec getMaDopInfoD() {
		return maDopInfoD;
	}

	public void setMaDopInfoD(SysAttrSpec maDopInfoD) {
		this.maDopInfoD = maDopInfoD;
	}

	public SysAttrSpec getMaVidDocD() {
		return maVidDocD;
	}

	public void setMaVidDocD(SysAttrSpec maVidDocD) {
		this.maVidDocD = maVidDocD;
	}

	public SysAttrSpec getMaSeriaFabrNomD() {
		return maSeriaFabrNomD;
	}

	public void setMaSeriaFabrNomD(SysAttrSpec maSeriaFabrNomD) {
		this.maSeriaFabrNomD = maSeriaFabrNomD;
	}

	public SysAttrSpec getMaRefNameD() {
		return maRefNameD;
	}

	public void setMaRefNameD(SysAttrSpec maRefNameD) {
		this.maRefNameD = maRefNameD;
	}

	public String getNomLK() {
		return nomLK;
	}

	public void setNomLK(String nomLK) {
		this.nomLK = nomLK;
	}

	public SysAttrSpec getMaAddress() {
		return maAddress;
	}

	public void setMaAddress(SysAttrSpec maAddress) {
		this.maAddress = maAddress;
	}

	public SysAttrSpec getMaNomDoc() {
		return maNomDoc;
	}

	public void setMaNomDoc(SysAttrSpec maNomDoc) {
		this.maNomDoc = maNomDoc;
	}

	public SysAttrSpec getMaLnc() {
		return maLnc;
	}

	public void setMaLnc(SysAttrSpec maLnc) {
		this.maLnc = maLnc;
	}

	public SystemData getSd() {
		return sd;
	}

	public void setSd(SystemData sd) {
		this.sd = sd;
	}


	public Integer getVidDocVpisv() {
		return vidDocVpisv;
	}

	public void setVidDocVpisv(Integer vidDocVpisv) {
		this.vidDocVpisv = vidDocVpisv;
	}

	public EgovMessagesCoresp getEmcoresp() {
		return emcoresp;
	}

	public void setEmcoresp(EgovMessagesCoresp emcoresp) {
		this.emcoresp = emcoresp;
	}
	public Date getDateRNV() {
		return dateRNV;
	}

	public void setDateRNV(Date dateRNV) {
		this.dateRNV = dateRNV;
	}
	
	public List<EgovMessagesFiles> getEgovMessFilesList() {
		return egovMessFilesList;
	}

	public void setEgovMessFilesList(List<EgovMessagesFiles> egovMessFilesList) {
		this.egovMessFilesList = egovMessFilesList;
	}
	
	public String getMailText() {
		return mailText;
	}

	public void setMailText(String mailText) {
		this.mailText = mailText;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public static Properties getProps() {
		return props;
	}

	public static void setProps(Properties props) {
		MmsCoach.props = props;
	}
	
	/*
	 * public boolean isCcevNV() { return ccevNV; }
	 * 
	 * public void setCcevNV(boolean ccevNV) { this.ccevNV = ccevNV; }
	 */

	public String getVidDocVpisvText() {
		return vidDocVpisvText;
	}

	public void setVidDocVpisvText(String vidDocVpisvText) {
		this.vidDocVpisvText = vidDocVpisvText;
	}

	public String getReasonCancel() {
		return reasonCancel;
	}

	public void setReasonCancel(String reasonCancel) {
		this.reasonCancel = reasonCancel;
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

public boolean isCancelZajavl() {
	return cancelZajavl;
}

public void setCancelZajavl(boolean cancelZajavl) {
	this.cancelZajavl = cancelZajavl;
}


public EgovMessages getEgovMess() {
	return egovMess;
}

public void setEgovMess(EgovMessages egovMess) {
	this.egovMess = egovMess;
}

public HashMap<String, String> getMsgStatusHM() {
	return msgStatusHM;
}

public void setMsgStatusHM(HashMap<String, String> msgStatusHM) {
	this.msgStatusHM = msgStatusHM;
}
	
public List<EgovMessagesCoresp> getEmcorespList() {
	return emcorespList;
}

public void setEmcorespList(List<EgovMessagesCoresp> emcorespList) {
	this.emcorespList = emcorespList;
}

public String getSelOptZajavl() {
	return selOptZajavl;
}

public void setSelOptZajavl(String selOptZajavl) {
	this.selOptZajavl = selOptZajavl;
}

	/**************************************************** FROM SSEV ****************************************************/	
	
	private Integer idSSev = null;
	private Integer vidDoc = null;
	private String regNom = null;
	private Date dataDoc = null;
	private String otnosno = null;
	private String egn = null;
	private String eik = null;
	private Referent ref = new Referent();
	private MMSVpisvane vpisvane = new MMSVpisvane();
	private boolean noVp = false;
	
	// Метода е за извикване след запис на обектите ако са извикани от "Нови заявления"!
	public boolean actionSaveDocFromSeos() {
		
		Doc newDoc = new Doc();	
		MMSVpisvaneDoc vpisvaneDoc = new MMSVpisvaneDoc();	
		
		DateFormat form = new SimpleDateFormat("yyy-MM-dd HH:mm:ss.S"); 
		
		boolean saveNewVp = false;		
		
		try {
			
			JPA.getUtil().runWithClose(() -> {
	
				// зареждам списъка с вписвания към треньорския кадър
//				List<MMSVpisvane> regList = new MMSVpisvaneDAO(getUserData()).findRegsListByIdAndType(this.mmsCoaches.getCodeMainObject(), this.mmsCoaches.getId());
				List<MMSVpisvane> regList = new MMSCoachesDAO(MMSCoaches.class, getUserData()).findVpisvListByIdTypeVidSport(this.mmsCoaches.getCodeMainObject(), this.mmsCoaches.getId(), this.mmsCoaches.getVidSport());
				if (!regList.isEmpty()) { // списъка не е празен
					vpisvane = new MMSVpisvaneDAO(getUserData()).findById(regList.get(0).getId()); // зареждам последното вписване 
				} else {						
					noVp = true; // няма вписвания					
				}
			});
				
			if (vidDoc.equals(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_TREN_KADRI)) { // ако заявлението е за вписване
				
				if (vpisvane.getStatusResultZaiavlenie() != null && vpisvane.getStatusResultZaiavlenie().equals(DocuConstants.CODE_ZNACHENIE_STATUS_ZAIAVLENIE_V_RAZGLEJDANE) ) {// проверява се, ако има вече записано вписване със статус на заявлението в разглеждане - да не записва пак ново вписване (това е, ако няколко пъти натискат бутона за запис)
						// този ред се маха по искане на Киро след онлайн среща от 30.08.2023г. - трябва да се позволява колкото искат вписвания за треньор с един и същи вид спорт дори и да е вписан вече - щяло само длъжността да е различна, която се въвежда от вписванията
//						|| (vpisvane.getStatusVpisvane() != null && vpisvane.getStatusVpisvane().equals(DocuConstants.CODE_ZNACHENIE_STATUS_REG_VPISAN)) ) { // проверява се, и ако има вече записано вписване със статус на вписването - вписан - да не записва пак ново вписване за този треньор за същия спорт (това се добави, заради проверката за вписвания по треньор и вид спорт - 18.05.2023); 
					
					saveNewVp = false; // няма да се записва ново вписване					
					JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString("beanMessages", "mmsCoach.noSaveZaiav"));	
					PrimeFaces.current().executeScript("scrollToErrors()");
					return false;	
				
				} else {
				
					saveNewVp = true; // ще се записва ново вписване
					noVp = false;
				}
			}
			
			if (noVp) { //Ако няма нито едно вписване - съобщение, че няма към кое вписване да се направи заличаване или промяна на обстоятелствата				
				
				if (vidDoc.equals(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_PROM_TREN_KADRI)) {
					JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString("beanMessages", "mmsCoach.noSaveZaiavPrObst"));						
				}
				
				if (vidDoc.equals(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_ZALICH_TREN_KADRI)) {
					JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString("beanMessages", "mmsCoach.noSaveZaiavZalich"));						
				}
				
				PrimeFaces.current().executeScript("scrollToErrors()");
				return false;
			
			} else { //Има вписване - продължава се за запис
			
				// настройка по вид документ и регистратура
				Object[] docVidSetting = new DocDAO(getUserData()).findDocSettings(getUserData(UserData.class).getRegistratura(), this.vidDoc, getSystemData());
				
				if (docVidSetting == null) { // Ако няма настройки по вид документ - да изкара съобщение
					
					String noSett = getSystemData().decodeItem(DocuConstants.CODE_CLASSIF_DOC_VID, this.vidDoc, getCurrentLang(), new Date());								
					
					JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString("beanMessages", "compReg.noDocSettings", noSett));	
					PrimeFaces.current().executeScript("scrollToErrors()");
					return false;
				
				} else { // има настройки - сет-ва ги на новия документ
					
					newDoc.setDocVid(this.vidDoc);
					newDoc.setRnDoc(this.regNom);	
					newDoc.setDocDate(this.dataDoc); 
		
					// ref.getCode() идва от parsvane pdf
					if (null!=this.mmsCoaches && null!=this.mmsCoaches.getIdObject()) {
						newDoc.setCodeRefCorresp(this.mmsCoaches.getIdObject()); 
					}
					
					
					newDoc.setRegisterId((Integer) docVidSetting[1]);
					boolean createDelo = Objects.equals(docVidSetting[2], Constants.CODE_ZNACHENIE_DA);
					
					Integer typeDocByRegister = (Integer) getSystemData().getItemSpecific(DocuConstants.CODE_CLASSIF_REGISTRI, newDoc.getRegisterId(), getCurrentLang(), new Date() , DocuClassifAdapter.REGISTRI_INDEX_DOC_TYPE);
					
					newDoc.setRegistraturaId(getUserData(UserData.class).getRegistratura());
					newDoc.setDocType(typeDocByRegister);
					newDoc.setFreeAccess(Constants.CODE_ZNACHENIE_DA);	
					
					// ако има декларирани процедури - стартират се с документа
					if (newDoc.getDocType().intValue() == DocuConstants.CODE_ZNACHENIE_DOC_TYPE_IN) {  
						newDoc.setProcDef((Integer) docVidSetting[5]);
					
					} else if (newDoc.getDocType().intValue() == DocuConstants.CODE_ZNACHENIE_DOC_TYPE_OWN) {  
						newDoc.setProcDef((Integer) docVidSetting[6]);
					
					} else if (newDoc.getDocType().intValue() == DocuConstants.CODE_ZNACHENIE_DOC_TYPE_WRK) {  
						newDoc.setProcDef((Integer) docVidSetting[7]);
					}
					
					if (ValidationUtils.isNotBlank(this.otnosno) && ! this.otnosno.trim().equals("null")) {						
						newDoc.setOtnosno(this.otnosno); 
					} else {						
						newDoc.setOtnosno(getSystemData().decodeItem(DocuConstants.CODE_CLASSIF_DOC_VID, newDoc.getDocVid(), getCurrentLang(), new Date()));
					}
					
					if (saveNewVp) { // сет-ва данните за ново вписване
						
						vpisvane = new MMSVpisvane();
						
						vpisvane.setRnDocZaiavlenie(this.regNom);
						vpisvane.setDateDocZaiavlenie(this.dataDoc);
						vpisvane.setStatusResultZaiavlenie(DocuConstants.CODE_ZNACHENIE_STATUS_ZAIAVLENIE_V_RAZGLEJDANE);
						vpisvane.setDateStatusZaiavlenie(new Date());
						vpisvane.setIdObject(this.mmsCoaches.getId()); // ИД на обекта
						vpisvane.setTypeObject(this.mmsCoaches.getCodeMainObject()); //КОДА на обекта	
						if (null!=this.mmsCoaches.getVidSport())
							vpisvane.setVidSport(this.mmsCoaches.getVidSport());
				//		if (null!=this.mmsCoaches.getDlajnost())
				//			vpisvane.setDlajnost(this.mmsCoaches.getDlajnost());
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
						
						// заисва се ново вписване, ако е такова заявлението
						if (vpisvane.getId() == null) {
							new MMSVpisvaneDAO(getUserData()).save(vpisvane);
						}
												
						// записваме връзката на документа с вписването
						if (idDoc != null && vpisvane.getId() != null) {
							
							if (checkExistDocInDocsList(idDoc)) { // проверка дали този документ вече го няма в списъка с документи към това вписване и ако го няма - тогава го записва
								vpisvaneDoc.setIdObject(vpisvane.getIdObject());
								vpisvaneDoc.setTypeObject(vpisvane.getTypeObject()); 
								vpisvaneDoc.setIdVpisvane(vpisvane.getId());
								vpisvaneDoc.setIdDoc(idDoc);
								
								new MMSVpisvaneDocDAO(getUserData()).save(vpisvaneDoc);
							}							
						}
						
						//TODO - дали така ще се ъпдейтва статуса в egov_messages - msg_status и msg_status_dat
	//					selOptZajavl.equals("2") // Параметър Само 1 треньор на заявленеие	!!!!!!!!
	//					selOptZajavl.equals("1") // Параметър Има и друг треньор на заявленеие	!!!!!!
						
						// Касае само заявленията за вписване!!!Този параметър ми показва, че заявлението ще бъде само за 1 треньор и тогава трябва да отида да ъпдейтна статуса egov_messages
						// ако се използва за повече треньори заявлениетео - не трябва да му се сменя статуса!!!
						if (null==selOptZajavl || selOptZajavl.equals("2")) {  
							
							EgovMessages egovMess = new EgovMessagesDAO(getUserData()).findById(this.idSSev); 
							
							egovMess.setMsgRn(newDoc.getRnDoc());
							egovMess.setMsgRnDate(newDoc.getDocDate());
							egovMess.setMsgStatus("DS_REGISTERED");	
							egovMess.setMsgStatusDate(new Date());
							
							new EgovMessagesDAO(getUserData()).save(egovMess);
						}
						
					});	
				}
			}
		
		} catch (DbErrorException e) {
			LOGGER.error("Грешка при зареждане настройки по вид документ!! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString("ui_beanMessages", ERRDATABASEMSG), e.getMessage());
		     return false;
		} catch (BaseException e) {
			LOGGER.error("Грешка при регистриране на вписване", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString("ui_beanMessages", ERRDATABASEMSG), e.getMessage());
		    return false;
		} /*catch (ParseException e) {
			LOGGER.error("Грешка при конвертиране на стринг в дата", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
		}*/		
		  
		return true;
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
	
	public Date getDataDoc() {
		return dataDoc;
	}
	
	public void setDataDoc(Date dataDoc) {
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
	
	private boolean checkExistDocInDocsList( Integer idDoc) {
		
		boolean saveNewDoc = false;
		
		try {
			
			List<Object[]> docsList = new MMSVpisvaneDocDAO(getUserData()).findDocsList(vpisvane.getId());			
			
			if (!docsList.isEmpty()) {
				
				for (Object[] tmpDoc : docsList) {
					
					int idDocDoc = ((BigInteger) tmpDoc[1]).intValue();
					
					if(idDocDoc == idDoc.intValue()) {
						saveNewDoc = false;
						break;
					
					} else {
						saveNewDoc = true;
					}								
				}
			
			} else {
				saveNewDoc = true;
			}
					
		} catch (DbErrorException e) {
			LOGGER.error("Грешка при зарежданесписъка с документи към вписване!!! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString("ui_beanMessages", ERRDATABASEMSG), e.getMessage());		
		}		
		
		return saveNewDoc;
	}

	public ArrayList<Files> getUploadFilesList() {
		return uploadFilesList;
	}

	public void setUploadFilesList(ArrayList<Files> uploadFilesList) {
		this.uploadFilesList = uploadFilesList;
	}
	
	/**************************************************** END FROM SSEV ****************************************************/			
				
		
	
	public void findByRegNom() {// Търсене на Обект по Рег. номер от регистър
		if (null!=this.mmsCoaches.getRegNomer() && ! this.mmsCoaches.getRegNomer().trim().isEmpty()) {
			try {
				
				JPA.getUtil().runWithClose(() -> {
					coachId = mmsCoachesDAO.findByArg(this.mmsCoaches.getRegNomer(), null);
					if (null!=coachId)
						loadCoachByIdCoach(Integer.valueOf(coachId));
				});
			   
			} catch (BaseException e) {
				LOGGER.error("Грешка при зареждане данните на треньор! ", e);
				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
			}catch (Exception e) {
				LOGGER.error("Грешка при зареждане данните на треньор!", e);
				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
			}
			
		}

	}
	//LM
	public void actionPrint(int envNotice) {
		
		if(!validatePrint()) {
			scrollToMessages();
			return;
		}
		
		String regN="";
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
		// От къде рег. номера да взема
//    	if (null!=this.document.getRnDoc())
//    		regN +=this.document.getRnDoc().trim();
//    	if (null!=this.document.getDocDate())
//    		regN +="/"+sdf.format(this.document.getDocDate());
//	
//		String param = JSFUtils.getRequestParameter("idObj");
//		Integer	docId = Integer.valueOf(envNotice);				

		if (envNotice==1) {//Печат пощ. плик
			String correspData="";
			String senderData="";
			
			correspData = CorrDataEnd();
			senderData = SenderDataEnd();

			// Print
			try {
				BaseExport exp= new BaseExport();
				String rPlik = "";
				
		    	if(null!=this.getFormatPlik()){
		    		rPlik = getSystemData().decodeItemLite(DocuConstants.CODE_CLASSIF_POST_ENVELOPS, this.getFormatPlik(),SysConstants.CODE_DEFAULT_LANG, new Date(), false).getDopInfo();
		    	}

				exp.printPlikCorespondent(rPlik, this.corespName, correspData, this.getSenderName(), senderData, regN, this.isRecommended());

			
		    }catch (DbErrorException e) {
		        LOGGER.error("Грешка при отпечатване на плик!",e);
		        JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, "Грешка при отпечатване на плик!", e.getMessage());
		    }
		}
		else {//Печат Обратна разписка
			
			try{
				
				// 1. Зарежда лиценза за работа с MS Word documents.
				
				License license = new License();
				String nameLic="Aspose.Words.lic";
				InputStream inp = getClass().getClassLoader().getResourceAsStream(nameLic);
				license.setLicense(inp);
				
				Document docEmptyShablon = null; 
				// 2. Чете файл-шаблон и създава празен Aspose Document за попълване 
				String namIzv="/resources/docs/"+"Известие доставка 243.docx";
				ServletContext context_ = (ServletContext)FacesContext.getCurrentInstance().getExternalContext().getContext();
				FileInputStream  fis=null;
				fis = new FileInputStream(context_.getRealPath("")+namIzv);
		       /* int size = fis.available();
		        byte[] baR = new byte[size];
		        fis.read(baR);
		        fis.close();*/
 		        if (null==fis || fis.available()==0) {
					JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, "Грешка при четене на файл шаблон!");
					return;
				}
 		       docEmptyShablon = new Document((InputStream)fis);
 		        
//		        docEmptyShablon = new Document(new ByteArrayInputStream(baR));
		     
		        // 2. Или чете файл-шаблон от БД и създава празен Asoose Document за попълване
		        /*Files fileShabl = new FilesDAO(getUserData()).findById(Integer.valueOf(-111));
				if (null==fileShabl || null==fileShabl.getContent()) {
					JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, "Грешка при четене на файл шаблон от БД!");
					return;
				}
				docEmptyShablon = new Document(new ByteArrayInputStream(fileShabl.getContent()));*/
				
				// 4. Създава попълнен документ от шаблона
				Document docFilledShablon = null;
	
				docFilledShablon = fillDocShabl243(docEmptyShablon);
			
				ByteArrayOutputStream dstStream = new ByteArrayOutputStream();
				docFilledShablon.save(dstStream, SaveFormat.DOCX);
				byte [] bytearray = null;
				bytearray = dstStream.toByteArray();
				// 5. Създава файла от създадения MS Word документ и го показва
				if (bytearray !=null){ 
					String fileName = "Izvestie_Dostavka";
					
					fileName =  fileName.split("\\.")[0] + "_" + sdf.format(new Date())+".docx";
				
					//	Показва попълнения шаблон		
					FacesContext ctx = FacesContext.getCurrentInstance();
					HttpServletResponse response = (HttpServletResponse) ctx.getExternalContext().getResponse();
					HttpServletRequest request = (HttpServletRequest) ctx.getExternalContext().getRequest();
		
					String agent = request.getHeader("USER-AGENT");
					if (null != agent && -1 != agent.indexOf("MSIE")) {
						String codedfilename = URLEncoder.encode(fileName,
								"UTF8");
						response.setContentType("application/x-download");
						response.setHeader("Content-Disposition",
								"attachment;filename=" + codedfilename);
					} else if (null != agent && -1 != agent.indexOf("Mozilla")) {
						String codedfilename = MimeUtility.encodeText(fileName,
								"UTF8", "B");
						response.setContentType("application/x-download");
						response.setHeader("Content-Disposition",
								"attachment;filename=" + codedfilename);
					} else {
						response.setContentType("application/x-download");
						response.setHeader("Content-Disposition",
								"attachment;filename=" + fileName);
					}
		
					ServletOutputStream out = null;
					out = response.getOutputStream();
					if (bytearray != null)
						out.write(bytearray);
		
					out.flush();
					out.close();
		
					ctx.responseComplete();
				}
		
			 } catch (DbErrorException e) {
				LOGGER.error(e.getMessage(), e);
				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
			 } catch (Exception e) {
					LOGGER.error(e.getMessage(), e);
					JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,"Грешка при четене на файл шаблон!", e.getMessage());
			}
			
			
		}

		clearPostCover();
	}
	public void prepareCorespDataCopy() {
		
		try {
			
			// Corespondents
			if(null != getRef()) {
				if(null != this.getRef().getRefName() && !this.getRef().getRefName().trim().equals("")){// Name
					this.setCorespName(this.getRef().getRefName().trim()); 
				}
				if(null != this.getRef().getContactPhone() && !this.getRef().getContactPhone().trim().equals("")){//Tel
					this.setCorespTel(this.getRef().getContactPhone().trim()); 
				}
				
			}
			
			
			if(null != this.getAdr()) {
				if(null != this.getAdr().getAddrText() && !this.getAdr().getAddrText().trim().equals("")){// Address
					this.setCorespAddress(this.getAdr().getAddrText().trim());	
				}else if (null != this.getAdr().getPostBox() && !this.getAdr().getPostBox().trim().equals("")) {
					this.setCorespAddress("Пощенска кутия "+this.getAdr().getPostBox().trim());	
				}
	
				if(null != this.getAdr().getPostCode() && !this.getAdr().getPostCode().trim().equals("")){//PostCode - BG
					this.setCorespPostCode(this.getAdr().getPostCode().trim()); 
				} 
				
				if(null != this.getAdr().getEkatte() && null != this.getRef().getDateReg()){
					String obstObl = getSystemData().decodeItemLite(CODE_CLASSIF_EKATTE, this.getAdr().getEkatte(), CODE_DEFAULT_LANG, this.getRef().getDateReg(), false).getDopInfo();// Obst and Obl
					if (null!=obstObl) { 
						String[] deco = obstObl.split(",");
						if (deco.length==2 && null!=deco[1]) {
							this.setCorespObl(deco[1].trim());// Oblast
						}
					}
					
					//NM
					this.setCorespNM(getSystemData().decodeItem(CODE_CLASSIF_EKATTE, this.getAdr().getEkatte(), CODE_DEFAULT_LANG, this.getRef().getDateReg()));
	
				}

				if(null != getAdr().getAddrCountry()){//Country
					this.setCorespCountry(getSystemData().decodeItem(DocuConstants.CODE_CLASSIF_COUNTRIES, this.getAdr().getAddrCountry(), CODE_DEFAULT_LANG, this.getRef().getDateReg())); 
				}
			}
			
			
			
		} catch (BaseException e) {
			LOGGER.error(getMessageResourceString(beanMessages, "Грешка при вземане на данни за кореспондент!"), e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
		}	
		
	}

	
	public void prepareCorespData() {
		
		try {
			
			// Corespondents
			if(null != getRef()) {
				if(null != this.getRef().getRefName() && !this.getRef().getRefName().trim().equals("")){// Name
					this.setCorespName(this.getRef().getRefName().trim()); 
				}
				if(null != this.getRef().getContactPhone() && !this.getRef().getContactPhone().trim().equals("")){//Tel
					this.setCorespTel(this.getRef().getContactPhone().trim()); 
				}
			}
			if (null==this.getCorespName()||"".equals(this.getCorespName())) this.getCorespName();
	
//			" phone "+ this.getCorespTel();
			
			
			
			if(null != this.getAdr()) {
				if(null != this.getAdr().getAddrText() && !this.getAdr().getAddrText().trim().equals("")){// Address
					this.setCorespAddress(this.getAdr().getAddrText().trim());	
				}else if (null != this.getAdr().getPostBox() && !this.getAdr().getPostBox().trim().equals("")) {
					this.setCorespAddress("Пощенска кутия "+this.getAdr().getPostBox().trim());	
				}
	
				if(null != this.getAdr().getPostCode() && !this.getAdr().getPostCode().trim().equals("")){//PostCode - BG
					this.setCorespPostCode(this.getAdr().getPostCode().trim()); 
				} 
				
				if(null != this.getAdr().getEkatte() && null != this.getRef().getDateReg()){
					String obstObl = getSystemData().decodeItemLite(CODE_CLASSIF_EKATTE, this.getAdr().getEkatte(), CODE_DEFAULT_LANG, this.getRef().getDateReg(), false).getDopInfo();// Obst and Obl
					if (null!=obstObl) { 
						String[] deco = obstObl.split(",");
						if (deco.length==2 && null!=deco[1]) {
							this.setCorespObl(deco[1].trim());// Oblast
						}
					}
					
					//NM
					this.setCorespNM(getSystemData().decodeItem(CODE_CLASSIF_EKATTE, this.getAdr().getEkatte(), CODE_DEFAULT_LANG, this.getRef().getDateReg()));
	
				}

				if(null != getAdr().getAddrCountry()){//Country
					this.setCorespCountry(getSystemData().decodeItem(DocuConstants.CODE_CLASSIF_COUNTRIES, this.getAdr().getAddrCountry(), CODE_DEFAULT_LANG, this.getRef().getDateReg())); 
				}
			}
			
			
			
		} catch (BaseException e) {
			LOGGER.error(getMessageResourceString(beanMessages, "Грешка при вземане на данни за кореспондент!"), e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
		}	
		
	}

	
	public String SenderDataEnd () {
		String retDat="";
		// Sender
		
		if(null != this.getSenderAddress() && !this.getSenderAddress().trim().equals(""))
			retDat+=this.getSenderAddress();
		
		if(null != this.getSenderTel() && !this.getSenderTel().trim().equals("")){
			if(null!=this.getAdr() && ! this.getAdr().getAddrCountry().equals(this.getCountryBg())){
				retDat+=" phone "+ this.getSenderTel();
			}else{
				retDat+=" тел. "+ this.getSenderTel();
			}
		}

//		if(null != this.getSenderObl() && !this.getSenderObl().trim().equals("")){
//			if (! this.getSenderObl().trim().contains("обл")) {
//				retDat+="\n"+"обл. "+this.getSenderObl().trim();
//			}else {
//				retDat+="\n"+this.getSenderObl().trim();
//			}
//		}
//
//		if(null != this.getSenderPostCode() && !this.getSenderPostCode().trim().equals(""))
//			retDat+="\n"+this.getSenderPostCode();
		
		if(null != this.getSenderNM()) {
			
			}if(null != this.getCorespNM()&& !this.getCorespNM().trim().equals("")) {
				retDat+=" "+this.getSenderNM();
			}
		if(null!=this.getAdr() && !this.getAdr().getAddrCountry().equals(this.getCountryBg()) && null != this.getSenderCountry() && !this.getSenderCountry().trim().equals(""))
			retDat+="\n"+this.getSenderCountry();
		
		return retDat;
		
	}
	
//	private boolean isRecommended() {
//		// TODO Auto-generated method stub
//		return false;
//	}

	public String CorrDataEnd() {
		String retDat="";
		
		// Corespondent
		
		if(null != this.getCorespAddress() && !this.getCorespAddress().trim().equals(""))
			retDat+=this.getCorespAddress();
		
		if(null != this.getCorespTel() && !this.getCorespTel().trim().equals("")){
			if(null!=this.getAdr() && !this.getAdr().getAddrCountry().equals(this.getCountryBg())) {
				retDat+=" phone "+ this.getCorespTel();
			}else{
				retDat+=" тел. "+ this.getCorespTel();
			}
		}

		if(null != this.getCorespObl() && !this.getCorespObl().trim().equals("")) {
			if (! this.getCorespObl().trim().contains("обл")) {
				retDat+="\n"+"обл. "+this.getCorespObl().trim();
			}else {
				retDat+="\n"+this.getCorespObl().trim();
			}
		}
	
		if(null != this.getCorespPostCode() && !this.getCorespPostCode().trim().equals(""))
			retDat+="\n"+this.getCorespPostCode();
		
		if(null != this.getCorespNM() && !this.getCorespNM().trim().equals(""))
			retDat+=" "+this.getCorespNM();

		if(null!=this.getAdr() && !this.getAdr().getAddrCountry().equals(this.getCountryBg()) && null != this.getCorespCountry() && !this.getCorespCountry().trim().equals(""))
			retDat+="\n"+this.getCorespCountry();
			
						
		retDat=infoAdres;
		return retDat;
	}

	
	public Document fillDocShabl243 (com.aspose.words.Document pattern) throws DbErrorException  {
		
		try{
			
			// 1. Данни на Получател 
			
			if (null != this.getCorespName() && pattern.getRange().getBookmarks().get("poluchatel") !=null){// Name
				pattern.getRange().getBookmarks().get("poluchatel").setText(this.getCorespName());
			}	
			
//			if (null!=this.getCorespAddress() && pattern.getRange().getBookmarks().get("adres") !=null){//Addres
//				String coradr=this.getCorespAddress();
//				if(null != this.getCorespTel() && !this.getCorespTel().trim().equals("")){
//					if(null!=this.getAdr() && !this.getAdr().getAddrCountry().equals(this.getCountryBg())) {
//						coradr+=" phone "+ this.getCorespTel();
//					}else{
//						coradr+=" тел. "+ this.getCorespTel();
//					}
//				}
//				
//				pattern.getRange().getBookmarks().get("adres").setText(coradr);
//			}
									
//			if (null != this.getCorespNM() && pattern.getRange().getBookmarks().get("nasMesto") !=null){
//				pattern.getRange().getBookmarks().get("nasMesto").setText(this.getCorespNM());
//			}
				
//			if (null != this.getCorespPostCode()){
//				String pkAdressat=this.getCorespPostCode().trim();
//				if (pattern.getRange().getBookmarks().get("pk1") !=null)
//					pattern.getRange().getBookmarks().get("pk1").setText(pkAdressat.substring(0, 1));
//				if (pattern.getRange().getBookmarks().get("pk2") !=null)
//					pattern.getRange().getBookmarks().get("pk2").setText(pkAdressat.substring(1, 2));
//				if (pattern.getRange().getBookmarks().get("pk3") !=null)
//					pattern.getRange().getBookmarks().get("pk3").setText(pkAdressat.substring(2, 3));
//				if (pattern.getRange().getBookmarks().get("pk4") !=null)
//					pattern.getRange().getBookmarks().get("pk4").setText(pkAdressat.substring(3, 4));
//				if (pattern.getRange().getBookmarks().get("pk") !=null)
//					pattern.getRange().getBookmarks().get("pk").setText(pkAdressat);
//			}

			
			// 2. Данни на адресанта(подател) - от регистратурата
			
	            if (null!= this.getSenderName() && pattern.getRange().getBookmarks().get("podatel") !=null){
					pattern.getRange().getBookmarks().get("podatel").setText(this.getSenderName());
				}

	            if (null!=this.getSenderAddress() && pattern.getRange().getBookmarks().get("adresPod") !=null){
	            	String sendadr=this.getSenderAddress();
	            	if(null != this.getSenderTel() && !this.getSenderTel().trim().equals("")){
	    				if(null!=this.getAdr() && !this.getAdr().getAddrCountry().equals(this.getCountryBg())) {
	    					sendadr+=" phone "+ this.getSenderTel();
	    				}else{
	    					sendadr+=" тел. "+ this.getSenderTel();
	    				}
	    			}
	            	
					pattern.getRange().getBookmarks().get("adresPod").setText(sendadr);
				}
	            pattern.getRange().getBookmarks().get("regNom").setText(regNom);
	            System.out.println();
	            
	            
	            
	            
				//NM
				if (null!=this.getSenderNM() && pattern.getRange().getBookmarks().get("nasMestoPod") !=null)
					pattern.getRange().getBookmarks().get("nasMestoPod").setText(this.getSenderNM());

				if (null!=this.getSenderObl() && pattern.getRange().getBookmarks().get("oblPod") !=null)
					pattern.getRange().getBookmarks().get("oblPod").setText(this.getSenderObl());
				

				
				if(null!=this.getSenderPostCode()){
					String pkAdressant=this.getSenderPostCode().trim();
					if (pattern.getRange().getBookmarks().get("pkp1") !=null)
						pattern.getRange().getBookmarks().get("pkp1").setText(pkAdressant.substring(0, 1));
					if (pattern.getRange().getBookmarks().get("pkp2") !=null)
						pattern.getRange().getBookmarks().get("pkp2").setText(pkAdressant.substring(1, 2));
					if (pattern.getRange().getBookmarks().get("pkp3") !=null)
						pattern.getRange().getBookmarks().get("pkp3").setText(pkAdressant.substring(2, 3));
					if (pattern.getRange().getBookmarks().get("pkp4") !=null)
						pattern.getRange().getBookmarks().get("pkp4").setText(pkAdressant.substring(3, 4));
					if (pattern.getRange().getBookmarks().get("pkp") !=null)
						pattern.getRange().getBookmarks().get("pkp").setText(pkAdressant);
					
					
					if (null!=pattern.getRange().getBookmarks().get("barCod")){
						Barcode128 barcode = new Barcode128();
						barcode.setCodeType(com.lowagie.text.pdf.Barcode128.POSTNET);
						barcode.setCode(this.getCorespPostCode());
//						barcode.setAltText("Пощенски код");
						barcode.setGenerateChecksum(true);
						Image code128Image = barcode.createAwtImage(Color.BLACK, Color.WHITE);

						DocumentBuilder builder = new DocumentBuilder(pattern);
						builder.moveToBookmark("barCod",true,false);
						BufferedImage bufferedImage = new BufferedImage(code128Image.getWidth(null), code128Image.getHeight(null),  BufferedImage.TYPE_INT_ARGB);
						Graphics2D bGr = bufferedImage.createGraphics();
						bGr.drawImage(code128Image, 0, 0, null);
						bGr.dispose();
						builder.insertImage(bufferedImage,120,80);
						
					}
						
					// Създаване на баркод
					// Insert QR BarCode 
					/*BarcodeQRCode qrCode = new BarcodeQRCode(pkAdressat,1,1,null);
					Image codeQrImage = qrCode.createAwtImage(Color.BLACK, Color.WHITE);
					BufferedImage bufferedImage = new BufferedImage(codeQrImage.getWidth(null), codeQrImage.getHeight(null),  BufferedImage.TYPE_INT_RGB);
			 
					 DocumentBuilder builder = new DocumentBuilder(pattern);
					 builder.moveToBookmark("barCod",true,false);
					 Graphics2D bGr = bufferedImage.createGraphics();
					 bGr.drawImage(codeQrImage, 0, 0, null);
					 bGr.dispose();
					 builder.insertImage(bufferedImage,100, 100); */
				}
							
			
			 	
		} catch (ObjectNotFoundException e) {
	 		LOGGER.error(e.getMessage(), e);
	 		throw new DbErrorException(e.getMessage());
		
		} catch (Exception e) {
			LOGGER.error(e.getMessage(),e);
			throw new DbErrorException(e.getMessage());
		
		} 
		return pattern;
		
	}


	public void clearPostCover() {
		
		this.setFormatPlik(null);
//		this.setRecommended(false);
//		this.setCorespName(null);
//		this.setCorespTel(null);
//		this.setCorespAddress(null);
//		this.setCorespPostCode(null); 
//		this.setCorespPBox(null);
//		this.setCorespObl(null);
//		this.setCorespNM(null);
//		this.setSenderName(null);
//		this.setSenderTel(null);
//		this.setSenderAddress(null);
//		this.setSenderPostCode(null); 
//		this.setSenderPBox(null);
//		this.setSenderObl(null);
//		this.setSenderNM(null);
//		this.setCorespCountry(null);
//		this.setSenderCountry(null);
//		this.setIdRegistratura(null);

	}
	
	public boolean validatePrint() {
		boolean err=true;

		
		
		
		return err;
	}
	/**
	 * Вика функцията scrollToErrors на страницата, за да се скролне екранът към съобщенията за грешка.
	 * Сложено е, защото иначе съобщенията може да са извън видимия екран и user изобшо да не разбере,
	 * че е излязла грешка, и каква.
	 */
	private void scrollToMessages() {
		PrimeFaces.current().executeScript("scrollToErrors()");
	}

	
	/*
	 * public boolean isSaveCD() { return saveCD; }
	 * 
	 * public void setSaveCD(boolean saveCD) { this.saveCD = saveCD; }
	 */

	public Integer getVidSportN() {
		return vidSportN;
	}

	public void setVidSportN(Integer vidSportN) {
		this.vidSportN = vidSportN;
	}

	public boolean isLockSave() {
		return lockSave;
	}

	public void setLockSave(boolean lockSave) {
		this.lockSave = lockSave;
	}
	//ЛМ
	public Integer getFormatPlik() {
		return formatPlik;
	}

	public void setFormatPlik(Integer formatPlik) {
		this.formatPlik = formatPlik;
	}

	public void prepareSenderData() {
		Registratura registratura=null;
		try {
			
			this.setIdRegistratura(((UserData)getUserData()).getRegistratura());
			
			if (null!=this.getIdRegistratura()) {
				registratura = new RegistraturaDAO(getUserData()).findById(this.getIdRegistratura());
				if (null!=registratura) {
				
					if (null!=registratura.getAddress() && ! registratura.getAddress().trim().equals("")) {
						this.setSenderAddress(registratura.getAddress().trim());
					}else if (null != registratura.getPostBox() && !registratura.getPostBox().trim().equals("")) {
						this.setSenderAddress("Пощенска кутия "+registratura.getPostBox().trim());	
					}
					
					
					if (null!=registratura.getOrgName() && ! registratura.getOrgName().trim().equals("")) {
						this.setSenderName(registratura.getOrgName().trim());
					}
					
					if (null!=registratura.getContacts() && ! registratura.getContacts().trim().equals("")) {
						this.setSenderTel(registratura.getContacts().trim());
					}
					
					if(null!=registratura.getPostCode() && !registratura.getPostCode().trim().equals("")){
						this.setSenderPostCode(registratura.getPostCode().trim());
					}
					
					if(null != registratura.getEkatte()){
						String obstOblS = getSystemData().decodeItemLite(CODE_CLASSIF_EKATTE, registratura.getEkatte(), CODE_DEFAULT_LANG, new Date(), false).getDopInfo();// Obst and Obl
						if (null!=obstOblS) { 
							String[] deco = obstOblS.split(",");
							if (deco.length==2 && null!=deco[1]) {
								this.setSenderObl(deco[1].trim());// Oblast
							}
						}
						
						//NM
						this.setSenderNM(getSystemData().decodeItem(CODE_CLASSIF_EKATTE, registratura.getEkatte(), CODE_DEFAULT_LANG, new Date()));
		
					}
				}
				
			}
		
		} catch (BaseException e) {
			LOGGER.error(getMessageResourceString(beanMessages, "Грешка при вземане на данни за подател!"), e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
		}	
		
		
	}

	public void setNames(String names) {
		this.names = names;
	}

	public ReferentAddress getAdr() {
		return adr;
	}

	public void setAdr(ReferentAddress adr) {
		this.adr = adr;
	}

	public String getCorespTel() {
		return corespTel;
	}

	public void setCorespTel(String corespTel) {
		this.corespTel = corespTel;
	}

	public String getCorespPostCode() {
		return corespPostCode;
	}

	public void setCorespPostCode(String corespPostCode) {
		this.corespPostCode = corespPostCode;
	}

	public String getCorespPBox() {
		return corespPBox;
	}

	public void setCorespPBox(String corespPBox) {
		this.corespPBox = corespPBox;
	}

	public String getCorespObl() {
		return corespObl;
	}

	public void setCorespObl(String corespObl) {
		this.corespObl = corespObl;
	}

	public String getCorespNM() {
		return corespNM;
	}

	public void setCorespNM(String corespNM) {
		this.corespNM = corespNM;
	}

	public String getSenderName() {
		return senderName;
	}

	public void setSenderName(String senderName) {
		this.senderName = senderName;
	}

	public String getSenderTel() {
		return senderTel;
	}

	public void setSenderTel(String senderTel) {
		this.senderTel = senderTel;
	}

	public String getSenderAddress() {
		return senderAddress;
	}

	public void setSenderAddress(String senderAddress) {
		this.senderAddress = senderAddress;
	}

	public String getSenderPostCode() {
		return senderPostCode;
	}

	public void setSenderPostCode(String senderPostCode) {
		this.senderPostCode = senderPostCode;
	}

	public String getSenderPBox() {
		return senderPBox;
	}

	public void setSenderPBox(String senderPBox) {
		this.senderPBox = senderPBox;
	}

	public String getSenderObl() {
		return senderObl;
	}

	public void setSenderObl(String senderObl) {
		this.senderObl = senderObl;
	}

	public String getSenderNM() {
		return senderNM;
	}

	public void setSenderNM(String senderNM) {
		this.senderNM = senderNM;
	}

	public Integer getCountryBg() {
		return countryBg;
	}

	public void setCountryBg(Integer countryBg) {
		this.countryBg = countryBg;
	}

	public String getSenderCountry() {
		return senderCountry;
	}

	public void setSenderCountry(String senderCountry) {
		this.senderCountry = senderCountry;
	}

	public Integer getIdRegistratura() {
		return idRegistratura;
	}

	public void setIdRegistratura(Integer idRegistratura) {
		this.idRegistratura = idRegistratura;
	}


	public void setCorespName(String corespName) {
		this.corespName = corespName;
	}

	public void setCorespAddress(String corespAddress) {
		this.corespAddress = corespAddress;
	}

	public String getCorespName() {
		if (corespName.equals("")){
			corespName+=referent.getIme()+" ";
			corespName+=referent.getPrezime()+" ";
			corespName+=referent.getFamilia()+" ";
			
		}

		return corespName;
	}

	public boolean isRecommended() {
		return recommended;
	}

	public void setRecommended(boolean recommended) {
		this.recommended = recommended;
	}

	public String getCorespAddress() {
		return corespAddress;
	}

	public String getCorespCountry() {
		return corespCountry;
	}

	public void setCorespCountry(String corespCountry) {
		this.corespCountry = corespCountry;
	}
	/* 
	 * Отпечатване на плик/Обратна разписка  - //
	 */

	public String preparePostCoverNotice(int index) {		
		clearPostCover();
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

		int a = index;
		Object[] red= docsListCD.getRowData();
		Object[] dopred=new Object[12];
		dopred[0]=red[0];//Вид на документа от DocuConstants.CODE_CLASSIF_DOC_VID
		dopred[1]=red[1];//РегНомер
		dopred[2]=red[2];//Дата на рег
		dopred[3]=red[3];//Относно
		dopred[8]=red[8];//Статус
		dopred[9]=red[9];//Дата Статус
		dopred[10]=red[10];//ВИд спорт
		regNom=(String) red[1];//РегНомер
		BigInteger bi=(BigInteger) red[0];//Вид на документа от DocuConstants.CODE_CLASSIF_DOC_VID
		vidDoc=bi.intValue();
		dataDoc=(Date) red[2];//Дата на рег
		if (vidDoc==DocuConstants.CODE_ZNACHENIE_VID_DOC_UDOST_REG_TREN_KADAR) regNom="У-"+regNom+"/"+sdf.format(dataDoc);;
		if (vidDoc==DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_TREN_KADRI) regNom=regNom+"(з)";
//		if (vidDoc==DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_TREN_KADRI) regNom=regNom+"(з)";
		System.out.println();
		try {
			
			this.setCountryBg(Integer.parseInt(getSystemData().getSettingsValue("delo.countryBG"))); 	
			
//			if(forSend.getCodeRef() != null){ // Взема адреса на кореспондента
//				this.setRef(new ReferentDAO(getUserData()).findByCode(forSend.getCodeRef(), forSend.getStatusDate(), true));
//				this.setAdr(getRef().getAddress());
//			}else {
//				this.setRef(null);
//				this.setAdr(null);
//				
//				if (null!=forSend.getDvijText() && ! forSend.getDvijText().trim().equals(""))
//					this.setCorespName(forSend.getDvijText().trim());
//			}
			
			
			
			// Data Corespondent
			prepareCorespData();
			
			// Data Sender
			prepareSenderData(); // from user Registratura
		
	
		} catch (BaseException e) {
			LOGGER.error(getMessageResourceString(beanMessages, "Грешка при вземане на адреса!"), e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
		}
		
		
		return null;
	}

	
}
