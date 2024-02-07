package com.ib.docu.beans;

import static com.ib.system.SysConstants.CODE_CLASSIF_EKATTE;
import static com.ib.system.SysConstants.CODE_DEFAULT_LANG;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import javax.activation.DataSource;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

// import javax.faces.view.ViewScoped;
import org.omnifaces.cdi.ViewScoped;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
import org.xml.sax.SAXException;

import com.aspose.words.Document;
import com.aspose.words.DocumentBuilder;
import com.aspose.words.License;
import com.aspose.words.SaveFormat;
import com.ib.docu.db.dao.DocDAO;
import com.ib.docu.db.dao.EgovMessagesDAO;
import com.ib.docu.db.dao.LockObjectDAO;
import com.ib.docu.db.dao.MMSCoachesDAO;
import com.ib.docu.db.dao.MMSSportObektDAO;
import com.ib.docu.db.dao.MMSSportObektLiceDAO;
import com.ib.docu.db.dao.MMSVpisvaneDAO;
import com.ib.docu.db.dao.MMSVpisvaneDocDAO;
import com.ib.docu.db.dao.ReferentDAO;
import com.ib.docu.db.dto.Doc;
import com.ib.docu.db.dto.EgovMessages;
import com.ib.docu.db.dto.EgovMessagesCoresp;
import com.ib.docu.db.dto.EgovMessagesFiles;
import com.ib.docu.db.dto.MMSCoaches;
import com.ib.docu.db.dto.MMSSportObekt;
import com.ib.docu.db.dto.MMSSportObektLice;
import com.ib.docu.db.dto.MMSVidSport;
import com.ib.docu.db.dto.MMSVidSportSpOb;
import com.ib.docu.db.dto.MMSVpisvane;
import com.ib.docu.db.dto.MMSVpisvaneDoc;
import com.ib.docu.db.dto.Referent;
import com.ib.docu.db.dto.ReferentAddress;
import com.ib.docu.export.BaseExport;
import com.ib.docu.system.DocuClassifAdapter;
import com.ib.docu.system.DocuConstants;
import com.ib.docu.system.SystemData;
import com.ib.docu.system.UserData;
import com.ib.docu.utils.ParsePdfZaqvlenie;
import com.ib.indexui.pagination.LazyDataModelSQL2Array;
import com.ib.indexui.system.Constants;
import com.ib.indexui.system.IndexUIbean;
import com.ib.indexui.utils.JSFUtils;
import com.ib.system.ActiveUser;
import com.ib.system.SysClassifAdapter;
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
import com.ib.system.utils.SearchUtils;
import com.ib.system.utils.ValidationUtils;
import com.lowagie.text.pdf.Barcode128;

import bg.government.regixclient.RegixClientException;
  
@Named(value = "mmsSportObEdit")
@ViewScoped
public class MMSSportenObektEdit extends IndexUIbean {
	
	private static final long serialVersionUID = 5748270252205977719L;
	private static final Logger LOGGER = LoggerFactory.getLogger(MMSSportFirmirovanieEdit.class);
//	private TimeZone timeZone = TimeZone.getDefault();
	private Date currentDate = null;
	private Integer lang = null;
	private UserData ud;
	
	/** за екраните на проекта IndexUIx */
	public static final String	UI_beanMessages	= "ui_beanMessages";
	/** за екраните на проекта IndexUIx */
	public static final String	UI_LABELS		= "ui_labels";

	/** за конкретната система */
	public static final String	beanMessages	= "beanMessages";
	/** за конкретната система */
	public static final String	LABELS			= "labels";
	/** */
	public static final String	navTexts		= "navTexts";
	private String  messLock = null;         // Съобщение при заключен обект
	
	private MMSSportObektDAO  obDAO;
	private MMSSportObekt   spObekt;
	private MMSSportObektLice   spObektLice;
	private Integer idSpObekt = null;
	private String regNomSpObekt = null;
	private Integer idLiceView = null;
	private String reg_nom_zaiavl;       // За заявление
	private Date date_reg_zaiavl;
	private Integer status_posl_vp;      // За статус на последно вписване
	private Date date_posl_vp;
	private String oldName = null;        // Име на спортния обект при влизане за актуализация
	private String zaglModalProverka = "" ;
	
	private List<Integer> selectedVidSport;
	private String selectedVidSportTxt;
	
	private String headerTxt = null;
	private boolean efrm = false;       // Дали се обработва еформа или се въвеждат данни от заявление
	private boolean inpPdf = false;   // Въведени се данни от PDF
	private int tipZvl = 1;       // Тип подадено заявление  .  1 - за ново вписване;  2 - за промяна на обстоятелства или за заличаване
	private int viewOnly = DocuConstants.CODE_ZNACHENIE_NE;
	private boolean  beginInp = false;      
	private Referent referent = new Referent();
	private Doc doc;
	private LazyDataModelSQL2Array  obList = null;      // Списък на записани спортни обекти със сходни имена
	private List<MMSSportObektLice>  obLicaList = null;   // Списък на лица във връзка с обекта като List от MMSSportObektLice  обекти
	private LazyDataModelSQL2Array  licaList;        // Списък с лицата във връзка с обекта
	private LazyDataModelSQL2Array docsList;      // Списък с всички документи към всички вписвания
	private LazyDataModelSQL2Array regsList;       // Списък с вписвания за обекта
//	private List<MMSVpisvane> regsList = new ArrayList<MMSVpisvane>();
	
	private Integer countryBG = 0; // ще се инициализира в getter-а през системна настройка: delo.countryBG
		
	private MMSVpisvane reg = null;    // Запис за вписване
	private List<MMSVpisvaneDoc>  regDocsList; 
	private List<Files> regFilesList;
			
	private boolean errInit  = false;
	private String errTxt = null;    
	
	private boolean LockOk;
	
//	private Integer vidDocVp;          
	private Integer vidDocVp = null;    // Вид документ за вписване
	private String vidDocVpText;
	
//  Работа с информационен модел
	private String sysNameObject = "";                   // Системно име на обект спортен обект в системния модел;
	
	private boolean ccevNV =true;
	
	// Атрибути от Data Model Спортен обект
	private SysAttrSpec dmRegNom = new SysAttrSpec();
	private SysAttrSpec dmStatus= new SysAttrSpec();
	private SysAttrSpec dmDateStatus= new SysAttrSpec();
	private SysAttrSpec dmName= new SysAttrSpec();
	private SysAttrSpec dmVid= new SysAttrSpec();
//	private SysAttrSpec dmFunkCat = new SysAttrSpec();
	private SysAttrSpec dmVidSport = new SysAttrSpec();
	private SysAttrSpec dmIdentif = new SysAttrSpec();
	private SysAttrSpec dmOpis = new SysAttrSpec();
	private SysAttrSpec dmDopInfo= new SysAttrSpec();
	private SysAttrSpec dmCountry= new SysAttrSpec();
	private SysAttrSpec dmNasMesto = new SysAttrSpec();
	private SysAttrSpec dmAddress= new SysAttrSpec();
	private SysAttrSpec dmEMail= new SysAttrSpec();
	private SysAttrSpec dmTel= new SysAttrSpec();
	private SysAttrSpec dmPostCode= new SysAttrSpec();

	private Referent referent_corresp = null;
	private String  txtCorresp; 
	private String mailText;
	private String subject;
	private boolean willShowMailModal = true;
	private static Properties props=new Properties();
	private static Integer ID_REGISTRATURE = 1;
	private static final String MAILBOX="DEFAULT";
	

	private Date dateRNV;
	private boolean cancelZajavl= false;
	
	private EgovMessages egovMess;
	private EgovMessagesCoresp emcoresp;
	private List<EgovMessagesFiles> egovFilesList;
	private List<SelectItem> msgStatusList = new ArrayList<>();
	private String reasonOtkaz;
	private HashMap<String, String>  msgStatusHM = new HashMap<String, String>();
	private List<EgovMessagesCoresp> emcorespList;
	
	private ArrayList<DataSource> attachedBytes = new ArrayList<DataSource>();
	private ArrayList<Files> uploadFilesList = new ArrayList<Files>();

	
	@PostConstruct
	public void initData() {
		
		ud = getUserData(UserData.class);
		
		// Попълване на данни за полета от информационния модел
		this.errTxt = null;
		this.errInit = false;
		String adres=
		this.zaglModalProverka = getMessageResourceString(LABELS, "spOb.zaglSprPoNaim");
		
		getAttrDataModel();
		if (this.errInit)   return;
			
		actionNew();
		this.lang =  getUserData().getCurrentLang();
		if (this.lang == null) this.lang = Integer.valueOf(1);
		this.countryBG = setCntryBG ();
		
		this.vidDocVp = Integer.valueOf(36);    // Вид документ за вписване
				
		// Прочитанепараметри за настройка на поща
		try {
			this.props = getSystemData(SystemData.class).getMailProp(ID_REGISTRATURE, MAILBOX);
		} catch (DbErrorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	
		this.beginInp = true;     // Първоначално влизане за нов  или актуализация на обект
		
	    obDAO = new MMSSportObektDAO (MMSSportObekt.class, getUserData());
			    
	    String par = null;
	    // Първо проверка дали е извикан от еформи или се въвеждат данни от заявление
//	    par = JSFUtils.getRequestParameter("efrm");     
//	    this.efrm = false;
//	    if  (par != null && !par.trim().isEmpty()) {
//	    	Integer ef =  Integer.valueOf(par.trim()); 
//	    	if (ef != null && (ef.intValue() == 1 ||  ef.intValue() == 0))  this.efrm = true;
//	    }
   	
	    this.efrm = false;
	      
	    par = JSFUtils.getRequestParameter("idObj");
		if (par != null && !par.trim().isEmpty()) {  
					// Актуализация или разглеждане
			this.idSpObekt = Integer.valueOf(par.trim());    // id на избран обект
		   readSpObekt (1) ;   // Прочитане на данни за спортен обект по id и зареждане на списъци
		   if (this.spObekt.getName() != null)  changeName();     // Формиране на списък с намерени обекти с подобни имена 
		   
		
		   
			
			par = JSFUtils.getRequestParameter("viewOnly");
			if (par != null && !par.trim().isEmpty())  
				this.viewOnly = Integer.valueOf( JSFUtils.getRequestParameter("viewOnly"));
					
//				if (this.viewOnly ==DocuConstants.CODE_ZNACHENIE_NE  && this.idSpObekt != null)    // Актуализация
//			    JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, "Моля  направете актуализиращ запис на избрания спортен обект задължително, за да се обнови статуса ако е необходимо!");
			
			   if (this.viewOnly != DocuConstants.CODE_ZNACHENIE_DA) {     // Актуализация
					// Проверка за заключен обект 
					//  ********************************************************************************************************************
					  	// проверка за заключен обект
				        LockOk = true;
						LockOk = checkForLock(Integer.valueOf(DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_OBJECTS), this.spObekt.getId());
						if (LockOk) {
						// отключване на всички обекти за потребителя(userId) и заключване на спортния обект за да не може да се актуализира от друг
							lockObject (Integer.valueOf(DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_OBJECTS), this.spObekt.getId(), Integer.valueOf(1));
							if (this.messLock != null) {     // Грешка при заключване
								this.errTxt = this.messLock;
								this.errInit = true;
								return;
							}
						
						}	else {
							// Съобщение за заключен обект
							this.errTxt = this.messLock;
							this.errInit = true;
							return;
						}
			         //**************************************************************************************************************************************
			   }
			   
			this.beginInp = false;
			
		}  else {
						
			 // Въвеждане на данни от заявление
		    // Проверка за тип заявление
		    par = JSFUtils.getRequestParameter("tipZvl");     
		    if  (par != null && !par.trim().isEmpty()) {    // Тип заявление
		    	Integer tipZ = Integer.valueOf(par.trim()); 
		    	this.tipZvl = tipZ.intValue();           // 1 - Завление за ново вписване;  2 - Заявление за промяна на обстоятелства или за заличаване 
		    	if (this.tipZvl != 1 && this.tipZvl != 2)  this.tipZvl = 1;   // Заявление за ново вписване
		    
		    }

		    this.beginInp = true;
		    headerTxt = ""; 
		    
			if (this.tipZvl == 1)  headerTxt = "Заявление за ново вписване";
			    	else headerTxt = "Заявление за промяна на обстоятелства или за заличаване";

			actionNew ();			

			//TODO - тези параметри ми трябват за метода, при който се записват вписванията и документите, които идват от "Нови заявления" - ДЕСИ!
			if (ValidationUtils.isNotBlank(JSFUtils.getRequestParameter("ccevID")) && ValidationUtils.isNumber(JSFUtils.getRequestParameter("ccevID"))) {
							
				// Тези параметри ми трябват, за да мога да регистрирам документ в нашата система с техните данни
				this.idSSev = Integer.valueOf(JSFUtils.getRequestParameter("ccevID"));
				
				// проверка за заключен обект EGOVMESSAGE
				LockOk = true;
				LockOk = checkForLock(Integer.valueOf(DocuConstants.CODE_ZNACHENIE_JOURNAL_EGOVMESSAGE), this.idSSev);
				if (LockOk) {   // Не е заключен обект  EGOVMESSAGE
					    // заключване обект EGOVMESSAGE
						lockObject(Integer.valueOf(DocuConstants.CODE_ZNACHENIE_JOURNAL_EGOVMESSAGE), this.idSSev, Integer.valueOf(1));
						if (this.messLock != null) {     // Грешка при заключване
							this.errTxt = this.messLock;
							this.errInit = true;
							return;
						}
		
		/*				
					
//					this.vidDoc = Integer.valueOf(JSFUtils.getRequestParameter("vidDoc"));					
//					this.regNom = JSFUtils.getRequestParameter("regNom");
//					this.dataDoc = JSFUtils.getRequestParameter("dataDoc");
//					
//					if (ValidationUtils.isNotBlank(this.dataDoc)) {
//						try {
//							DateFormat df = new SimpleDateFormat("yyy-MM-dd"); 
//							setDateRNV(df.parse(this.dataDoc));
//						} catch (ParseException e) {
//							LOGGER.error("Грешка при конвертиране на стринг в дата", e);
//							JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
//						}	
//					}
//					
					
//					this.otnosno = JSFUtils.getRequestParameter("otnosno");
//					this.egn = JSFUtils.getRequestParameter("egn");
//					this.eik = JSFUtils.getRequestParameter("eik");
					
	
////					actionLoadEgovMessage();
			*/	
						
					this.errInit = false;	
					loadParamsFromZajavl();    // get params from zajavlenie
					if (this.errInit)  return;      // Получена е грешка при зареждане на данни  за заявление
					
					if (this.tipZvl == 1)  this.beginInp = true;     // Първоначално влизане за нов  или актуализация на обект
					else  this.beginInp = false; 
					
										
//					boolean xmlFound=false;
//					for (int i = 0; i < egovFilesList.size(); i++) {
//						if(egovFilesList.get(i).getFilename().endsWith(".xml")) {
//							xmlFound=true;
//							break;
//						}
//					}
//					  
//					if(xmlFound) {
//						//TODO VIKAME NIKI DA OBRABOTI I VARNE DANNI
//						// СЛЕД КАТО НИКИ ГО РАЗПАКЕТИРА МОЖЕ БИ ЩЕ ВИКАМЕ comesFromDAEU метода или поне част от него абе иска се:
//	//					Ако няма ЕИК, това се съобщава на работещия и се променя статуса на заявлението при Ирена на „оставено без последствие“ и се записва грешката в egov_messages;
//	//					pri validnost
//	
//					}  else {
//						//MAI NISHTO NE PRAVIM OT EKRANA AKO RESHAT DA VAVEJDAT AKO LI NE SI IMAT BUTON ZA OTKAZ OT REGISTRACIQ
//					}
					
		//			  this.efrm = true;
					//ТОВА Е СТАРО ЩЕ СЕ ТРИЕ СИГУРНО.
					// TODO - ако това остане да се минава през него - не знам дали ще остане метода actionSaveDocFromSeos() както е сега - заради кореспондента
					//comesFromDAEU(this.idSSev);
		
				//*************************************************************************************************************************	
					//OTIVA DA SE OPITA DA PARSNE PDF AKO IMA TAKAV.
					
					try {
						
						this.spObekt =new ParsePdfZaqvlenie().parseObekt((SystemData) getSystemData(), ud, getCurrentLang(), egovMess, egovFilesList);
						if (this.spObekt !=null && this.spObekt.getNas_mesto() != null && this.spObekt.getCountry() == null)
							this.spObekt.setCountry(this.countryBG);
						 if (this.spObekt != null && this.spObekt.getE_mail() != null) {
							  this.spObekt.setE_mail(this.spObekt.getE_mail().trim());
							  if (this.spObekt.getE_mail().isEmpty() )
								  this.spObekt.setE_mail(null);
							    if (this.spObekt.getE_mail() == null) {
							    	if (emcoresp != null && emcoresp.getEmail() != null)
							    		this.spObekt.setE_mail(emcoresp.getEmail());
							    }
							  else {
								  if (emcoresp == null ) {
									  emcoresp = new EgovMessagesCoresp ();
									  emcoresp.setEmail(null);
								  }
								  if ( emcoresp.getEmail() == null) {
										emcoresp.setEmail(this.spObekt.getE_mail());      // Става e_mail за кореспондент - на този e_mail се изпращат съобщенията от главния екран
							      }
							  }
						  }
						
							try {
								this.inpPdf = true;
								this.spObektLice = null;
								selectedVidSport = new ArrayList<>();
		                         selectedVidSportTxt = "";
		                       if (this.spObekt != null && this.spObekt.getVidSportList() != null)                // Ако има въведени данни за видове спорт
							      getVidSportAStrings(this.spObekt.getVidSportList());
							} catch (DbErrorException e1) {
								JSFUtils.addErrorMessage("Грешка при декодиране на вид спорт при автоматична обработка на заявление! - " + e1.getLocalizedMessage(), e1);
								LOGGER.error(e1.getMessage(), e1);
							}
						
						  
						if (this.spObekt != null && this.spObekt.getParseMessages() != null &&  this.spObekt.getParseMessages().size()>0) {
						
							for (int i = 0; i < this.spObekt.getParseMessages().size(); i++) {
								JSFUtils.addErrorMessage(this.spObekt.getParseMessages().get(i));
							}
							
							
							if (this.spObekt.getParseMessages().size() == 1 && this.spObekt.getParseMessages().get(0).contains("Не е открит пдф")) {
								// (this.spObekt.getParseMessages().get(0).equalsIgnoreCase("Не е открит пдф за извличане на данни!") || this.spObekt.getParseMessages().get(0).equalsIgnoreCase("Не е открит пдф за парсване!") ) 
								// Въвеждане на заявлението от хартия
							   
								if (this.tipZvl == 1) {    // Заявление за вписване
									 this.beginInp = true;
									 this.obLicaList = setLicaVr ();
									 if (this.obLicaList != null && !this.obLicaList.isEmpty())   this.beginInp = false;     // Ако има въведени данни за лица за връзка 
									 this.spObekt.setParseMessages(null);
								} else {       // Заявления за промяна или за замичаване
									// Първо въвеждане на рег. номер за спортен обект
									this.inpPdf = false;
									 this.beginInp = false;
									 this.spObekt = new  MMSSportObekt ();
									 this.idSpObekt = null;
								}
							}
						
						}else {
										
							if (this.spObekt !=  null  && this.spObekt.getId()!=null) {
								this.idSpObekt = this.spObekt.getId();
								 this.obLicaList = setLicaVr ();
								 this.beginInp = false;
								findVpisvAll();
		//						findDocs();	
							} else {                         // Въвеждане на заявлението от хартия
								if (this.spObekt == null)  this.spObekt = new  MMSSportObekt ();
								 this.beginInp = true;
								 this.obLicaList = setLicaVr ();
								 if (this.obLicaList != null && !this.obLicaList.isEmpty())   this.beginInp = false;     // Ако има въведени данни за лица
													
							}
						}
					} catch (ParserConfigurationException e) {
						JSFUtils.addErrorMessage("Грешка при автоматична обработка на заявление! - " + e.getLocalizedMessage() , e);
						LOGGER.error(e.getMessage(), e);
					} catch (DOMException e) {
						JSFUtils.addErrorMessage("Грешка при автоматична обработка на заявление! - " + e.getLocalizedMessage() , e);
						LOGGER.error(e.getMessage(), e);
					} catch (SAXException e) {
						JSFUtils.addErrorMessage("Грешка при автоматична обработка на заявление! - " + e.getLocalizedMessage() , e);
						LOGGER.error(e.getMessage(), e);
					} catch (IOException e) {
						JSFUtils.addErrorMessage("Грешка при автоматична обработка на заявление! - " + e.getLocalizedMessage() , e);
						LOGGER.error(e.getMessage(), e);
					} catch (RegixClientException e) {
						JSFUtils.addErrorMessage("Грешка при автоматична обработка на заявление! - " + e.getLocalizedMessage() , e);
						LOGGER.error(e.getMessage(), e);
					} catch (DatatypeConfigurationException e) {
						JSFUtils.addErrorMessage("Грешка при автоматична обработка на заявление! - " + e.getLocalizedMessage() , e);
						LOGGER.error(e.getMessage(), e);
					} catch (ParseException e) {
						JSFUtils.addErrorMessage("Грешка при автоматична обработка на заявление! - " + e.getLocalizedMessage() , e);
						LOGGER.error(e.getMessage(), e);
				
				    } catch (DbErrorException e) {
				    	JSFUtils.addErrorMessage("Грешка при автоматична обработка на заявление! - " + e.getLocalizedMessage() , e);
						LOGGER.error(e.getMessage(), e);
				    }
					if (null == this.spObekt.getId()) {
//					   this.spObekt.setStatus(DocuConstants.CODE_ZNACHENIE_STATUS_OBEKT_V_RAZGLEJDANE);
//					   this.spObekt.setDateStatus(new Date());
						 this.spObekt.setStatus(null);
						 this.spObekt.setDateStatus(null);
					}   
					
			//********************************************************************************************************************************************************		
					
				}else {    // Обектът EGOVMESSAGE е бил заключен 
	//					setCcevNV(false);
	
				}  
				
//				if (!isCcevNV()) {
//					// Трябва запис в журнала за неподадени идентификатори в ново заявление???
//				}
		       				
			} else {  // !(ValidationUtils.isNotBlank(JSFUtils.getRequestParameter("ccevID")) && ValidationUtils.isNumber(JSFUtils.getRequestParameter("ccevID")))
				// обект EGOVMESSAGE е заключен
				if (this.messLock != null) {     // Грешка при проверка за заключване
					this.errTxt = this.messLock;
					this.errInit = true;
					return;
				}
				
			}
				
		}	

	}	
	
	public boolean getErrMessYes () {
	   if (this.spObekt != null && this.spObekt.getParseMessages() != null) {
		   if (this.spObekt.getParseMessages().size()>0)  return true;
	   }
	   return false;
	}
	
	public void actionNew () {
		this.errInit  = false;
		this.errTxt = null; 
		this.inpPdf = false;
		
		 this.setSpObekt(new MMSSportObekt ());
		 if (this.dmCountry.isActive() && this.dmCountry.isRequired())
		       this.spObekt.setCountry(countryBG);
		 if (this.dmNasMesto.isActive() && this.dmNasMesto.isRequired()) {
		      this.dmCountry.setRequired(true);
			  this.spObekt.setCountry(countryBG);
		 }
		 
		 this.idSpObekt  = null;
		 this.regNomSpObekt = null;
		 this.beginInp = true;   // За първоначално влизане
		 this.reg_nom_zaiavl = null;
		 this.date_reg_zaiavl = null;
		 this.status_posl_vp = null;
		 this.date_posl_vp = null;
		 this.oldName = null;
		 
		 this.setObList(null);
		 this.obLicaList = new  ArrayList<MMSSportObektLice> ();
		
		  this.licaList = null;        // Списък с лицата във връзка с обекта
		  this.docsList = null;      // Списък с всички документи към всички вписвания
		  this.setRegsList(null);    // Списък с вписвания за обекта
			this.selectedVidSport = new ArrayList<>();
			this.selectedVidSportTxt = "";
		 
		 clearSelLice ();
	}
	
	
	//******************************************************************************************************************************************************************************************************
	
	// Обработка на заявления
		public void loadParamsFromZajavl() {//Parametri ot zajavlenie

			try {
				
				setVidDocVpText("");

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
					
					egovFilesList = new EgovMessagesDAO(getUserData()).findFilesByMessage(this.idSSev);
					egovMess = new EgovMessagesDAO(getUserData()).findById(this.idSSev);
						
					emcorespList = (List<EgovMessagesCoresp>) new MMSCoachesDAO(MMSCoaches.class, ActiveUser.DEFAULT).findCorespByIdMessage(this.idSSev);
					
					  
					if(emcorespList != null && emcorespList.size()>0) {
						for (EgovMessagesCoresp item : emcorespList) {
							if(item != null && (ValidationUtils.isNotBlank(item.getEgn()) || ValidationUtils.isNotBlank(item.getIdCard()))) {
								emcoresp = item;
								break;
							}
						}
						
						if (emcoresp==null)
							emcoresp=emcorespList.get(0);
						
						if (emcoresp != null && emcoresp.getEmail() != null) {
							emcoresp.setEmail(emcoresp.getEmail().trim());
							if (emcoresp.getEmail().isEmpty() )  emcoresp.setEmail(null);
						}
						
						// Za vpisvanijata
						this.egn=emcoresp.getEgn();
						this.eik = emcoresp.getBulstat();
				      		
					}
					
								
					if (null!=egovMess) {
						
						if(ValidationUtils.isNotBlank(egovMess.getDocVid()) && ValidationUtils.isNumber(egovMess.getDocVid())) { //Взема Вид заявление - вписване, заличаване, промяна обстоятелства
							this.vidDoc=Integer.valueOf(egovMess.getDocVid());
							this.vidDocVp = this.vidDoc;
							
							switch (this.vidDocVp.intValue()) {

								case DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_SPORTEN_OBEKT: 
									setVidDocVpText("Заявление за вписване");
									this.headerTxt = this.vidDocVpText;
									this.tipZvl = 1;
									break;
								case DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_ZALICH_SPORTEN_OBEKT : 
									setVidDocVpText("Заявление за заличаване");
									this.headerTxt = this.vidDocVpText;
									this.tipZvl = 2;
									break;
								case DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_PROM_SPORTEN_OBEKT: 
									setVidDocVpText("Заявление промяна обстоятелства");
									this.headerTxt = this.vidDocVpText;
									this.tipZvl = 2;
									break;
							}
										
						}  else{
							this.vidDoc = null;
							this.vidDocVp=null;
						}	
						
						// Za vpisvanijata
						this.regNom = egovMess.getDocRn();
						this.dataDoc = egovMess.getDocDate();
						this.dateRNV = this.dataDoc;
						this.otnosno = egovMess.getDocSubject();
						
						if (egovMess.getDocRn() != null)  {
							if (this.headerTxt != null && !this.headerTxt.trim().isEmpty() )  {
								this.headerTxt +=  " : "+ egovMess.getDocRn().trim();
//								this.headerTxt +=  "                   Вх. ном. "+ egovMess.getDocRn().trim();
								if (this.dateRNV != null)
//									this.headerTxt += "     Дата рег.: " + new SimpleDateFormat ("dd.MM.yyyy").format(this.dateRNV);
								   this.headerTxt += "/ " + new SimpleDateFormat ("dd.MM.yyyy").format(this.dateRNV);
							}
							
						}
						
					}
				
				
				/*  ********************************************************
				setVidDocVpText("");
				String tmpVidVpisv=null;
							
				tmpVidVpisv = JSFUtils.getRequestParameter("vidDoc");//Взема Вид заявление - вписване, заличаване, промяна обстоятелства 
				if(ValidationUtils.isNotBlank(tmpVidVpisv) && ValidationUtils.isNumber(tmpVidVpisv)) { 
					this.vidDocVp = Integer.valueOf(tmpVidVpisv);
					
					switch (this.vidDocVp.intValue()) {

						case DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_SPORTEN_OBEKT: 
							setVidDocVpText("Заявление за вписване");
							this.headerTxt = this.vidDocVpText;
							this.tipZvl = 1;
							break;
						case DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_ZALICH_SPORTEN_OBEKT : 
							setVidDocVpText("Заявление за заличаване");
							this.headerTxt = this.vidDocVpText;
							this.tipZvl = 2;
							break;
						case DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_PROM_SPORTEN_OBEKT: 
							setVidDocVpText("Заявление промяна обстоятелства");
							this.headerTxt = this.vidDocVpText;
							this.tipZvl = 2;
							break;
					}
								
				}  else{
					this.vidDocVp=null;
				}
				
						
				this.emcoresp = null;
				
				
				JPA.getUtil().runWithClose(() -> {
					
					ArrayList<Object[]> tmpList = new EgovMessagesDAO(getUserData()).createMsgTypesList();
					
					tmpList = new EgovMessagesDAO(getUserData()).createMsgStatusList();
				
					if(tmpList !=null && !tmpList.isEmpty()){
						for(Object[] item:tmpList) {
							if(item != null && item[0]!=null && item[1]!=null){
								this.msgStatusHM.put(item[0].toString(), item[1].toString());
							}
						}
					}
					
					egovFilesList = new EgovMessagesDAO(getUserData()).findFilesByMessage(this.idSSev);
					egovMess = new EgovMessagesDAO(getUserData()).findById(this.idSSev);
					
					if (egovMess != null && egovMess.getDocRn() != null)  {
						if (this.headerTxt != null && !this.headerTxt.trim().isEmpty() )  {
							this.headerTxt +=  " : "+ egovMess.getDocRn().trim();
//							this.headerTxt +=  "                   Вх. ном. "+ egovMess.getDocRn().trim();
							if (this.dateRNV != null)
//								this.headerTxt += "     Дата рег.: " + new SimpleDateFormat ("dd.MM.yyyy").format(this.dateRNV);
							   this.headerTxt += "/ " + new SimpleDateFormat ("dd.MM.yyyy").format(this.dateRNV);
						}
						
					}
					
					
					emcorespList = (List<EgovMessagesCoresp>) new MMSCoachesDAO(MMSCoaches.class, ActiveUser.DEFAULT).findCorespByIdMessage(this.idSSev);
					
					  
					if(emcorespList != null && emcorespList.size()>0) {
						for (EgovMessagesCoresp item : emcorespList) {
							if(item != null && (ValidationUtils.isNotBlank(item.getEgn()) || ValidationUtils.isNotBlank(item.getIdCard()))) {
								emcoresp = item;
								break;
							}
						}
						
						if (emcoresp==null)
							emcoresp=emcorespList.get(0);
				      		
					}
		
		****************************************************		*/
				
				
				
					
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
	//			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
				this.errTxt = getMessageResourceString(UI_beanMessages, ERRDATABASEMSG) + "- "+  e.getMessage();
				this.errInit = true;
				return;
			} catch (BaseException e) {
				LOGGER.error("Грешка при зареждане на данни от Заявление за вписване!", e);
//				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,"Грешка при зареждане на данни от "+getVidDocVpText()+"!", e.getMessage());
				if (getVidDocVpText() != null && !getVidDocVpText().trim().isEmpty())
				  this.errTxt = "Грешка при зареждане на данни от заявление  "+getVidDocVpText()+"!" + "- "+  e.getMessage();
				else
					this.errTxt = "Грешка при зареждане на данни от заявление !" + "- "+  e.getMessage();
				this.errInit = true;
				return;
			} catch (Exception e) {
				LOGGER.error("Грешка при зареждане данни от Заявление за вписване!", e);
//				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,"Грешка при зареждане на данни от "+getVidDocVpText()+"!", e.getMessage());
				if (getVidDocVpText() != null && !getVidDocVpText().trim().isEmpty())
					  this.errTxt = "Грешка при зареждане на данни от заявление  "+getVidDocVpText()+"!" + "- "+  e.getMessage();
					else
						this.errTxt = "Грешка при зареждане на данни от заявление !" + "- "+  e.getMessage();
				this.errInit = true;
				return;
				
			}	

			
		}
	
	
//	public void actionLoadEgovMessage() {
//		if (this.idSSev!=null) {
//			try {
//				egovMess = new EgovMessagesDAO(getUserData()).findById(this.idSSev);
//				egovFilesList = new EgovMessagesDAO(getUserData()).findFilesByMessage(this.idSSev);
//				ArrayList<Object[]> tmpList = new EgovMessagesDAO(getUserData()).createMsgTypesList();
//				
//				tmpList = new EgovMessagesDAO(getUserData()).createMsgStatusList();
//			
//				if(tmpList !=null && !tmpList.isEmpty()){
//					for(Object[] item:tmpList) {
//						if(item != null && item[0]!=null && item[1]!=null){
//							msgStatusList.add(new SelectItem( item[0].toString(),item[1].toString()));
//						}
//					}
//				}
//			} catch (DbErrorException e) {
//				LOGGER.error("Грешка при работа с базата!", e);
//				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());	
//			}catch (Exception e) {
//				LOGGER.error("Грешка при зареждане данните!", e);
//				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());	
//			}
//		}
//	}
	
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
	
	public void actionOtkazReg() {
		if (this.reasonOtkaz==null || this.reasonOtkaz.trim().isEmpty()) {
			 JSFUtils.addMessage("mmsSObform:otkazText", FacesMessage.SEVERITY_ERROR, 
                     getMessageResourceString(UI_beanMessages, "general.pleaseInsert", "Причина за отказ от регистрация"));
			 return;
		}
		
		//*****************************************************************************************************
		//TODO VIKAM METOD NA DESI ZA PROMQNA NA STATUS.... 
		//*****************************************************************************************************
		changeStatusZaiavl ( this.reasonOtkaz);
		  
	}
	
	public void actionChangeMail () {
		  if (this.spObekt.getE_mail() != null) {
			  this.spObekt.setE_mail(this.spObekt.getE_mail().trim());
			  if (this.spObekt.getE_mail().isEmpty() )  this.spObekt.setE_mail(null);
		  }
	}
	
	public void changeStatusZaiavl (String mess )   {
		
		
		try {
            
		      JPA.getUtil().runWithClose(() -> { 
                  EgovMessages egovMess = new EgovMessagesDAO(getUserData()).findById(this.idSSev);//ccevID - ид. на EgovMessages - Параметър предаван от Ирена
            
      
                //TODO - Сменя статуса на заявлението в egov_messages при отказ за обработка- 
                

                if (null!=egovMess) {

                    egovMess.setMsgStatus("DS_REJECTED");
                    egovMess.setMsgStatusDate(new Date());

                    egovMess.setCommError(mess);// Причината за отказ. Трябва да не е null or ""
                    
                    
                    new EgovMessagesDAO(getUserData()).save(egovMess);
                    this.cancelZajavl= true;
              
                    this.errInit  =true;
           	        this.errTxt = "Отказана е обработката на полученото заявление!"; 
	           	     if (this.headerTxt != null && !this.headerTxt.trim().isEmpty()) {
	           	    	 this.errTxt += " ( " + this.headerTxt + " ) ";
	           	     }
	//           	     this.errTxt += "\r\n";
	           	    this.errTxt += " Причина:  " + mess.trim();		 
                    
                }
                
            });    
            
        
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
	
	public void comesFromDAEU(Integer egovID) {
		
	//  1.	Разпакетиране на въведеното с ЕАУ заявление 
		
			// 2. 	Деловодна регистрация на заявлението чрез система „Архимед“ Това не се прави засега
			
			// 3 Проверка за тип заявление
			//   3.1  - Заявление за вписване  попълва се this.sportObekt  и се изобразява с this.beginInp = true като въвеждане на нов обект
			//         по name на обекта се формира и this.obList  (изпълнява се changeName())
			//      Ако се избере обект от списъка, той се зарежда, като преди изобразяване се изпълнявант методите за азпис на вписвания и документи от Деси
			//       След това ако се налага  се актуализират полетата на this.spObekt със с9тойностите от подаденото заявление
			//   3.2  - Заявление за промяна на обстоятелства или заличаване - първо по рег. номер се търси обект и ако го има се зарежда
			//    Преди изобразяване се изпълнявант методите за азпис на вписвания и документи от Деси
	        //      След това ако се налага  се актуализират полетата на this.spObekt със с9тойностите от подаденото заявление
			//    Ако го няма се формира съобщение за липса - към потребителя
				
		
		try {
			//EgovMessages tmpEgov = new EgovMessagesDAO(getUserData()).findById(egovID);
			
			EgovMessagesCoresp emcoresp = new EgovMessagesDAO(getUserData()).findCorespByIdMessage(egovID);
			if(emcoresp == null) {
				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, "Липсва ЕИК на лицето в заявлението! Моля, изберете ново заявление!");	
				//sendMail("Отговор на подадено заявление", "Липсва ЕИК на лицето в заявлението!");
				return;
			}
			if(emcoresp.getBulstat() != null && !emcoresp.getBulstat().trim().isEmpty()) {
				if( !ValidationUtils.isValidBULSTAT(emcoresp.getBulstat()) ) {
					// 3. 	Валидация на ЕИК. При установяване на грешка се прекратява обработката, като се изпраща известие по мейла на заявителя с подходящата нотификация
					JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, "Грешен ЕИК в заявлението! Моля, изберете ново заявление!");
					//sendMail("Отговор на подадено заявление", "Грешен ЕИК в заявлението! Трябва да изберете ново заявление!");
					return;
				}
				this.referent_corresp = new ReferentDAO(getUserData()).findByIdent(emcoresp.getBulstat(), null, null, DocuConstants.CODE_ZNACHENIE_REF_TYPE_NFL);
				
				if(this.referent_corresp != null) {
					this.txtCorresp = this.referent_corresp.getIme();
					// Зареждане на спортен обект 
//					  this.idSpObekt =idSpOb;    // id на избран обект
//					   readSpObekt (1);
					
					// Пример за обединение 
//					sportObed.setIdObject(Integer.valueOf(referent_corresp.getCode()));
//					sportObed = new MMSsportObedinenieDAO(MMSSportnoObedinenie.class,getUserData()).findByIdObject(sportObed);
//					if(sportObed == null || sportObed.getId() == null) {
//						setTxtCorresp(referent.getIme());
//					}else {
//						if(sportObed != null && sportObed.getId() != null) {
//							mfList	=new MMSSportObedMFDAO(getUserData()).findByIdSportnoObed(sportObed.getId());	
//							actionLoadFormirList();
//							findVpisvane();
//							findDocs();
//						}
//					}
				}else {
					// da potarsi komponentata v regix i ako nameri si minava po normalniq red.
					PrimeFaces.current().executeScript("PF('mCorrD').show();");
				}
					
			} 
			 
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
		 
		
		// 4. Проверка в базата , ако се намери - > отказ от регистрация и мейл до заявителя.
		
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
			attachedBytes.add(new ByteArrayDataSource(upLoadedFile.getContent(), ""));
		}
        try {
               mailer.sent(Content.PLAIN, props, props.getProperty("user.name"), props.getProperty("user.password"),
                            props.getProperty("mail.from"), "Министерство на младежта и спорта", 
                            //referent.getContactEmail(),
                            emcoresp.getEmail() , 
                            //"n.kosev@indexbg.bg", 
                            subject, mailText,
                            attachedBytes);
               JSFUtils.addInfoMessage("Успешно изпращане на съобщението!");
               subject = "";
               mailText = "";
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
	
	//******************************************************************************************************************************************************************************************************
	
	public void changeRegNomer () {
		if (this.regNomSpObekt == null ||  this.regNomSpObekt.trim().isEmpty()) {
			JSFUtils.addMessage("mmsSObform:inpRegN", FacesMessage.SEVERITY_ERROR, "Не е въведен рег. номер за търсене на спортен обект!");
			return;
		}
		this.regNomSpObekt = regNomSpObekt.trim();
	     readSpObekt (2);
	     if (!this.errInit)  {   // Намерен е спортен обект
	    	 
	 		// Проверка за заключен обект 
				//  **********************************************************************************************
				  
			     	// проверка за заключен обект
				    LockOk = true;
					LockOk = checkForLock(Integer.valueOf(DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_OBJECTS), this.spObekt.getId());;
					if (LockOk) {
					// отключване на всички обекти за потребителя(userId) и заключване на спортния обект за да не може да се актуализира от друг
						lockObject (Integer.valueOf(DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_OBJECTS), this.spObekt.getId(), Integer.valueOf(1));
						if (this.messLock != null) {     // Грешка при заключване
							this.errTxt = this.messLock;
							this.errInit = true;
							return;
						}
					
					}	else {
						// Съобщение за заключен обект
						if (this.messLock != null) {     // Грешка при заключване
							this.errTxt = this.messLock;
							this.errInit = true;
							return;
						}	
					}
				    	 
	     }
	     
	     
		 this.viewOnly = 2;
		 this.beginInp = false;
	}
	
	public void editSpObekt (Integer idSpOb)  {
		
		      this.idSpObekt =idSpOb;    // id на избран обект
			
		      readSpObekt (1);
		   if (!this.errInit) {
					  
		   // Проверка за заключен обект 
				//  **********************************************************************************************
				
			     	// проверка за заключен обект
				   LockOk = true;
					LockOk = checkForLock(Integer.valueOf(DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_OBJECTS), this.spObekt.getId());
				
					if (LockOk) {
					// отключване на всички обекти за потребителя(userId) и заключване на спортния обект за да не може да се актуализира от друг
						lockObject (Integer.valueOf(DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_OBJECTS), this.spObekt.getId(), Integer.valueOf(1));
					
						if (this.messLock != null) {     // Грешка при заключване
//							this.errTxt = this.messLock;
//							this.errInit = true;
							 JSFUtils.addGlobalMessage( FacesMessage.SEVERITY_ERROR, this.messLock);
							 this.messLock =  null;
							return;
						}
						
											
					}	else {
						// Съобщение за заключен обект
//						this.errTxt = this.messLock;
//						this.errInit = true;
						if ( this.messLock  != null) {
							 JSFUtils.addGlobalMessage( FacesMessage.SEVERITY_ERROR, this.messLock);
							 this.messLock =  null;
							 if (this.beginInp) actionNew();
							return;
						}	
					}
		
		   }		
					this.viewOnly = 2;
				this.beginInp = false;
						
	}
	
	/**
	 *   Прочитане данни за спортен обект
	 *   
	 * @param prRead - признак как се получават данните - 1 - чрез търсене по зададен id;  2 - по зададен regNom
	 *  при prRead = 1 трябва idSpObekt да съдържа id на обекта  при prRead = 2  this.regNomSpObekt трябва да съдържа regNomer на абекта
	 */
	
	public void readSpObekt (int prRead)  {
		
		if (prRead == 1 )   {
//		this.setSpObekt(JPA.getUtil().getEntityManager().find(MMSSportObekt.class, this.idSpObekt));
				try {
					this.setSpObekt(obDAO.findById(this.idSpObekt));
				} catch (DbErrorException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
//					JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, getMessageResourceString(beanMessages, "spOb.errReadIdObekt") + "-" + e.getLocalizedMessage());
					this.errTxt = getMessageResourceString(beanMessages, "spOb.errReadIdObekt") + "-" + e.getLocalizedMessage();
					this.errInit = true;
					return;
				}
				
				if (this.spObekt == null || this.spObekt.getId() == null) {
					this.errTxt = "В базата липсват записани данни за спортен обект с идентификатор  " + String.valueOf(this.idSpObekt) + " !"  ;
					this.errInit = true;
					return;
				}
		} else {   
			try {
				this.setSpObekt(obDAO.findByRegNom( this.regNomSpObekt ));
			} catch (DbErrorException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
//				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, getMessageResourceString(beanMessages, "spOb.errReadIdObekt") + "-" + e.getLocalizedMessage());
				this.errTxt = getMessageResourceString(beanMessages, "spOb.errReadIdObekt") + "-" + e.getLocalizedMessage();
				this.errInit = true;
				changeStatusZaiavl (this.errTxt ) ;
				return;
			}
			
			if (this.spObekt == null || this.spObekt.getId() == null) {
				this.errTxt = "В базата липсват записани данни за спортен обект с въведения рег. номер =  " + this.regNomSpObekt ;
				this.errInit = true;
				changeStatusZaiavl (this.errTxt ); 
				return;
			}
			
		}
		
		  this.idSpObekt= this.spObekt.getId();
		  if (this.spObekt.getE_mail() != null) {
			    if (this.spObekt.getE_mail().trim().isEmpty() )  this.spObekt.setE_mail(null);
		  }
		  
		  // Зареждане на видове спортове
		  if (this.spObekt.getVidSportList() == null)
			    this.spObekt.setVidSportList(new ArrayList <> ());
		  else this.spObekt.getVidSportList().size();
		
			selectedVidSport = new ArrayList<>();
			selectedVidSportTxt = "";
		     try {
				getVidSportAStrings (this.spObekt.getVidSportList());
			} catch (DbErrorException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		      
							 
				// Зареждане на списъци
		        if (this.efrm)  {    // При вход от ел. форми
		        	// Запис на подадените данни за ново вписване и документи с методите на Деси
		        }
		  
		  
				   this.obLicaList = findLicaVr ();
					findVpisvAll ();
			//		findDocs();  // Документите се зареждат в findVpisvAll ()
		
	}
	
	private void getVidSportAStrings(List<MMSVidSportSpOb> vidSportList) throws DbErrorException {
		selectedVidSport = new ArrayList<>();
		selectedVidSportTxt = "";
		for (int i = 0 ; i < vidSportList.size() ; i++) {
			MMSVidSport tmp = vidSportList.get(i);
			selectedVidSport.add(tmp.getVidSport());
			if (selectedVidSportTxt.length()>0) {
				selectedVidSportTxt+=", "+getSystemData().decodeItem(DocuConstants.CODE_CLASSIF_VIDOVE_SPORT, tmp.getVidSport(), getCurrentLang(), new Date());
			}else {
				selectedVidSportTxt+=getSystemData().decodeItem(DocuConstants.CODE_CLASSIF_VIDOVE_SPORT, tmp.getVidSport(), getCurrentLang(), new Date());
			}
		}
	}
	
	private void makeVidSportStringArrayAsObject() throws NumberFormatException, DbErrorException {
		this.spObekt.getVidSportList().clear();
			
		for (int i = 0; i < selectedVidSport.size(); i++) {
			MMSVidSportSpOb  tmp = new MMSVidSportSpOb();
			tmp.setTipОbject(DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_OBJECTS); // koda na object Sportn object
			tmp.setIdObject(this.spObekt.getId());
			tmp.setVidSport(Integer.valueOf(selectedVidSport.get(i)));
			tmp.setUserReg(getCurrentUserId());
			tmp.setDateReg(new Date());
			this.spObekt.getVidSportList().add(tmp);
		}
	}
	
	public void clearSelLice () {
	
		this.spObektLice = new MMSSportObektLice ();
		this.spObektLice.setIdSportObekt(this.spObekt.getId());
		this.idLiceView = null;     // id на избрано лице за показване на данните му
					
	}	
		
     private void getAttrDataModel() {
      	 
    	 this.sysNameObject = "sport_obekt";      // Системно име за обект  Спортен обект
    	 
		try {
			// Main table sportenObekt
			this.dmRegNom = getSystemData().getModel().getAttrSpec("reg_nomer", this.sysNameObject, getCurrentLang(), null);
			this.dmStatus = getSystemData().getModel().getAttrSpec("status", this.sysNameObject, getCurrentLang(), null);
			this.dmDateStatus = getSystemData().getModel().getAttrSpec("date_status", this.sysNameObject, getCurrentLang(), null);
			this.dmName = getSystemData().getModel().getAttrSpec("name", this.sysNameObject, getCurrentLang(), null);
			this.dmVid = getSystemData().getModel().getAttrSpec("vid", this.sysNameObject, getCurrentLang(), null);
	//		this.dmFunkCat = getSystemData().getModel().getAttrSpec("funk_category", this.sysNameObject, getCurrentLang(), null);
			this.dmVidSport = getSystemData().getModel().getAttrSpec("vid_sport", this.sysNameObject, getCurrentLang(), null);
			this.dmIdentif = getSystemData().getModel().getAttrSpec("identification", this.sysNameObject, getCurrentLang(), null);
			this.dmOpis = getSystemData().getModel().getAttrSpec("opisanie", this.sysNameObject, getCurrentLang(), null);
			this.dmDopInfo = 	getSystemData().getModel().getAttrSpec("dop_info", this.sysNameObject, getCurrentLang(), null);	
			this.dmCountry = getSystemData().getModel().getAttrSpec("country", this.sysNameObject, getCurrentLang(), null);	
			this.dmNasMesto = getSystemData().getModel().getAttrSpec("nas_mesto", this.sysNameObject, getCurrentLang(), null);
			this.dmAddress = getSystemData().getModel().getAttrSpec("sgrada", this.sysNameObject, getCurrentLang(), null);
			this.dmEMail = getSystemData().getModel().getAttrSpec("e_mail", this.sysNameObject, getCurrentLang(), null);
			this.dmTel =   getSystemData().getModel().getAttrSpec("tel", this.sysNameObject, getCurrentLang(), null);
			this.dmPostCode =  getSystemData().getModel().getAttrSpec("post_code", this.sysNameObject, getCurrentLang(), null);
			
					
		} catch (DbErrorException | InvalidParameterException e) {
			LOGGER.error( "Грешка при ЧЕТЕНЕ НА атрибутите на Инф. обект Спортен обект" , e);
			this.errTxt = "Грешка при ЧЕТЕНЕ НА атрибутите на Инф. обект Спортен обект -   " + e.getLocalizedMessage();
			this.errInit = true;
			return;
			
		}
		  
		/*
		 *  От прочетена спецификация  SysAttrSpec spec се проверяват    
		 *  spec.isActive () - дали е активна полето
		 *  spec.isRequired () - дали е задължително
		 *  spec.getValidMetod () - име на JavaScript метод  за валидация  от  IndexUIx- src/main/resources/js/validation.js
		 */
	}
			
	public void changeName () {    // Смяна на име на спортен обект само при първоначална регистрация 
		  this.obList = null;	
		    findObWithEQNames (this.spObekt.getName());
		   if (this.obList == null ) return;
		
	}
	
	public void viewObList () {
          if (this.obList == null ) return;
		
		  // Отваряне на модален за проверка на намерени записи за обекти
			String  cmdStr = "PF('modalProverka').show();";
			PrimeFaces.current().executeScript(cmdStr);	
	}
	
	public boolean eqBG (Integer country) {
		if (country != null && country.intValue() == this.countryBG.intValue())   return true;
		return false;
		
	}
	
	/**
	 * Затваряне на модален  (action за бутон  Х)
	 */
	public void handleCloseDialogProverkaOK () {
		
	}
	
	/**
	 * Затваряне на модален  (action за бутон  Прекъсване въвеждане)
	 */
	public void handleCloseDialogProverka () {
		actionNew ();
	}
	

	public void actionSaveLiceVrazka () {
		
		
		if (this.spObektLice == null || this.spObektLice.getIdSportObekt() == null || (spObektLice.getIdLice() == null && spObektLice.getIme() == null && spObektLice.getPrezime() == null && spObektLice.getFamilia() == null ) || this.spObektLice.getNameLice() == null)	{
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, getMessageResourceString(beanMessages, "spOb.noInfoVr"));
			return;
		}
		if ( this.spObektLice.getTypeVrazka() == null)	{
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, getMessageResourceString(beanMessages, "spOb.noTypeVr"));
			return;
		}
		
		// Проверка дали новото лице не е било записано вече - за лица от Referent
		if (this.spObektLice.getId() == null && this.obLicaList  != null && !this.obLicaList .isEmpty()) {
			
			  for (int i = 0; i < this.obLicaList.size(); i++) {
				  MMSSportObektLice  item =  this.obLicaList.get(i); 
				 if (item.getIdLice() != null && item.getIdLice().intValue() == this.spObektLice.getIdLice().intValue()) {
					 JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, "Избраното лице вече е въведено във връзка със спортния обект!");
					 return;
				 }
					  
			  }
			
		}
		
		this.spObektLice.setDateReg(new Date());
		this.spObektLice.setDateLastMod(new Date());
		this.spObektLice.setUserReg(getUserData().getUserId());
		this.spObektLice.setUserLastMod(getUserData().getUserId());
		
				
		// Запис на лице за връзка
		
		try {
			JPA.getUtil().runInTransaction(() -> this.spObektLice = new MMSSportObektLiceDAO(MMSSportObektLice.class, getUserData()).save(this.spObektLice));
		} catch (DbErrorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, getMessageResourceString(beanMessages, "spOb.errSaveLiceVr") + "-" + e.getLocalizedMessage());
			return;
		} catch (BaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, getMessageResourceString(beanMessages, "spOb.errSaveLiceVr") + "-" + e.getLocalizedMessage());
			return;
		}
		
		JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, "Успешен запис на  връзката с лице! ");
		// Зареждане на списъци
		
	   this.obLicaList = findLicaVr ();
		
		clearSelLice ();
	}
	
  public void actionChangeLiceVrazka () {
		
		
//		if (this.spObektLice == null ||  spObektLice.getIdLice() == null || this.spObektLice.getNameLice() == null)	{
	  if (this.spObektLice == null ||  (spObektLice.getIdLice() == null && spObektLice.getIme() == null && spObektLice.getPrezime() == null && spObektLice.getFamilia() == null)) { 
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, getMessageResourceString(beanMessages, "spOb.noInfoVr"));
			return;
		}
		if ( this.spObektLice.getTypeVrazka() == null)	{
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, getMessageResourceString(beanMessages, "spOb.noTypeVr"));
			return;
		}
		
		// Проверка дали новото лице не е било записано вече
//		if (this.spObektLice.getId() == null && this.obLicaList  != null && !this.obLicaList .isEmpty()) {
//			
//			  for (int i = 0; i < this.obLicaList.size(); i++) {
//				  MMSSportObektLice  item =  this.obLicaList.get(i); 
//				 if (item.getIdLice() != null && item.getIdLice().intValue() == this.spObektLice.getIdLice().intValue()) {
//					 JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, "Избраното лице вече е въведено във връзка със спортния обект!");
//					 return;
//				 }
//					  
//			  }
//			
//		}
		
		this.spObektLice.setDateReg(new Date());
		this.spObektLice.setDateLastMod(new Date());
		this.spObektLice.setUserReg(getUserData().getUserId());
		this.spObektLice.setUserLastMod(getUserData().getUserId());
		
				
		// Запис на лице за връзка
//		
//		try {
//			JPA.getUtil().runInTransaction(() -> this.spObektLice = new MMSSportObektLiceDAO(MMSSportObektLice.class, getUserData()).save(this.spObektLice));
//		} catch (DbErrorException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, getMessageResourceString(beanMessages, "spOb.errSaveLiceVr") + "-" + e.getLocalizedMessage());
//			return;
//		} catch (BaseException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, getMessageResourceString(beanMessages, "spOb.errSaveLiceVr") + "-" + e.getLocalizedMessage());
//			return;
//		}
	
		// Актуализация на запис в  this.obLicaList
		if (this.obLicaList == null)  this.obLicaList = new ArrayList <MMSSportObektLice> ();
		if (this.obLicaList.size() == 0) this.obLicaList.add(this.spObektLice);
		else {
			for (int i = 0; i < this.obLicaList.size(); i++) {
				if (this.obLicaList.get(i).getIdLice() != null) {
					if (this.obLicaList.get(i).getIdLice().intValue() == this.spObektLice.getIdLice().intValue()) {
						this.obLicaList.set(i,  this.spObektLice);
						JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, "Успешна актуализация на  връзката с лице! ");
						break;
					}
				} else {
					String ime = this.obLicaList.get(i).getIme();
					String prez = this.obLicaList.get(i).getPrezime();
					String fam = this.obLicaList.get(i).getFamilia();
					
					if (((ime == null && this.spObektLice.getIme() == null) || (ime != null && ime.equalsIgnoreCase(this.spObektLice.getIme())))  
							&& ((prez == null && this.spObektLice.getPrezime() == null) || (prez != null && prez.equalsIgnoreCase(this.spObektLice.getPrezime()))) 
							&& ((fam == null && this.spObektLice.getFamilia() == null) || (fam != null && fam.equalsIgnoreCase(this.spObektLice.getFamilia()))) 
					)  {
								this.obLicaList.set(i,  this.spObektLice);
								JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, "Успешна актуализация на  връзката с лице! ");
								break;	
					  }
				}
			}
		}
			
		clearSelLice ();
	}
	
	/**
	 *  Запис на всички свързани лица за обект
	 * @param licaL
	 */
	public void  saveAllLica  (List<MMSSportObektLice> licaL) {
		
		if (licaL != null && licaL.size() > 0)  {
			Date d = new Date();
		 for (int i = 0; i < licaL.size(); i++)  {
					
			     MMSSportObektLice  obLice = licaL.get(i);
			        obLice.setId(null);;
			        obLice.setDateReg(d);
					obLice.setDateLastMod(d);
					obLice.setUserReg(getUserData().getUserId());
					obLice.setUserLastMod(getUserData().getUserId());
					
							
					// Запис на лице за връзка
					
					try {
						JPA.getUtil().runInTransaction(() ->  new MMSSportObektLiceDAO(MMSSportObektLice.class, getUserData()).save(obLice));
					} catch (DbErrorException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, getMessageResourceString(beanMessages, "spOb.errSaveLiceVr") + "-" + e.getLocalizedMessage());
						return;
					} catch (BaseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, getMessageResourceString(beanMessages, "spOb.errSaveLiceVr") + "-" + e.getLocalizedMessage());
						return;
					}
					
				
		 }
		}	 
		
	}
		
	// Зареждане данни за вписване
	public void actionEditReg(Integer idReg) {	
			
			try {
	
				if (idReg != null) {
	
					JPA.getUtil().runWithClose(() -> {					
						
						setReg(new MMSVpisvaneDAO(getUserData()).findById(idReg));
	//					setRegDocsList(new MMSVpisvaneDAO(getUserData()).findDocsList(getReg().getId()));
						
						// извличане на файловете от таблица с файловете
						setRegFilesList(new FilesDAO(getUserData()).selectByFileObject(getReg().getId(), getReg().getTypeObject()));
					
					});				
				}			
	
			} catch (BaseException e) {
				LOGGER.error("Грешка при зареждане данните на вписването! ", e);
				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
			}		
		}
	
	public boolean isPravaDelYes () {
		return   getUserData().hasAccess(DocuConstants.CODE_CLASSIF_DEF_PRAVA, DocuConstants.CODE_ZNACHENIE_DEF_PRAVA_DEL_MMS_REG);
	
	}
	
	/**
	 *  Изтриване на спортен обект
	 */
	public void actionDeleteSpObekt () {
		
		try {
			JPA.getUtil().begin();
			this.obDAO.deleteFromRegister(this.spObekt.getId(),  (SystemData)getSystemData()) ;
		
		      JPA.getUtil().commit();

		      JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, "Успешно са изтрити данните за спортния обект!");
		  	actionNew();
		     this.errTxt = null;
			this.errInit = true;
			
		} catch (ObjectInUseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				JPA.getUtil().rollback();
				LOGGER.error("Спортният обект се използува в момента - данните за него не могат да бъдат изтрити! - ", e);
				String s = "";
				if (e.getLocalizedMessage() != null && !e.getLocalizedMessage().trim().isEmpty() ) s += e.getLocalizedMessage();
				else if (e.getMessage() != null ) s += e.getMessage();
				if (e.getCause() != null) {
					if (e.getCause().getLocalizedMessage() != null && !e.getCause().getLocalizedMessage().trim().isEmpty()) s += " - " + e.getCause().getLocalizedMessage();
					else if (e.getCause().getMessage() != null) s += " - " + e.getCause().getMessage();
				}
				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, "Данните за спортния обект не могат да бъдат изтрити!  " + s );
				return;
			
		} catch (DbErrorException  e) {
			e.printStackTrace();
			JPA.getUtil().rollback();
			LOGGER.error("Грешка при изтриване на данни за спортен обект!! ", e);
			String s = "";
			if (e.getLocalizedMessage() != null && !e.getLocalizedMessage().trim().isEmpty() ) s += e.getLocalizedMessage();
			else if (e.getMessage() != null ) s += e.getMessage();
			if (e.getCause() != null) {
				if (e.getCause().getLocalizedMessage() != null && !e.getCause().getLocalizedMessage().trim().isEmpty()) s += " - " + e.getCause().getLocalizedMessage();
				else if (e.getCause().getMessage() != null) s += " - " + e.getCause().getMessage();
			}	
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, "Грешка при изтриване на данни за спортен обект!  " + s );
			return;
		}
		
		
	}
	   
	
	public void generateDoc() {
//		if(doc == null)
//			doc = new Doc();
	
	}
	
	public String actionGoto(Integer idObj) {
			
		String result = "";
		result = "docView.xhtml?faces-redirect=true&idObj=" + idObj;
		return result;
	}
	
	/**
	 * Проверка на входните полета
	 * @return
	 */
	public boolean checkFields () {
		  
			boolean  checkF = true;
//			if (beginInp) {
//				if (this.reg_nom_zaiavl == null || this.reg_nom_zaiavl.trim().isEmpty()) {
//	//				JSFUtils.addMessage("mmsSObform:rZ", FacesMessage.SEVERITY_ERROR, getMessageResourceString(beanMessages, "spOb.noRegNomZ");
//					checkF = false;
//				}	
//				
//				if (this.date_reg_zaiavl == null ) {
//	//				JSFUtils.addMessage("mmsSObform:dZ", FacesMessage.SEVERITY_ERROR, getMessageResourceString(beanMessages, "spOb.noDateZ");
//					checkF = false;
//				}	
//				
//			}	
			
			if (this.dmName.isActive()) {
				if (this.dmName.isRequired() && (this.spObekt.getName()  == null || this.spObekt.getName().trim().isEmpty())) {
					if (this.idSpObekt == null && this.beginInp == true)
					   JSFUtils.addMessage("mmsSObform:nameOb", FacesMessage.SEVERITY_ERROR, getMessageResourceString(beanMessages, "spOb.noName"));
					else
						JSFUtils.addMessage("mmsSObform:nameOb1", FacesMessage.SEVERITY_ERROR, getMessageResourceString(beanMessages, "spOb.noName"));
						
				     checkF = false;
				}	
			} else this.spObekt.setName(null);
			
			if (this.dmVid.isActive()) {
		       if (this.dmVid.isRequired() && this.spObekt.getVidObekt()  == null ) {
					
					JSFUtils.addMessage("mmsSObform:vidOb", FacesMessage.SEVERITY_ERROR, getMessageResourceString(beanMessages, "spOb.noVidOb"));
					checkF = false;
				}	
			} else this.spObekt.setVidObekt(null);
				
//	       if (!(this.idSpObekt == null && !this.beginInp == true)) {   // При първоначална регистрация при първо влизане няма статус
//		       if (this.spObekt.getStatus()  == null ) {
//					JSFUtils.addMessage("mmsSObform:stat", FacesMessage.SEVERITY_ERROR, getMessageResourceString(beanMessages, "spOb.noStatus"));
//					checkF = false;
//		    	   
//		       }
//		       
//		       if (this.spObekt.getDateStatus()  == null ) {
//					JSFUtils.addMessage("mmsSObform:dateStat", FacesMessage.SEVERITY_ERROR, getMessageResourceString(beanMessages, "spOb.noDataSt"));
//					checkF = false;
//		    	   
//		       }
//	       }
	       
//			if (this.getDmFunkCat().isActive()) {
//		       if (this.dmFunkCat.isRequired() && this.spObekt.getFunkCategory()  == null ) {
//		    		if (this.idSpObekt == null && this.beginInp == true)
//					  JSFUtils.addMessage("mmsSObform:spCatOb1", FacesMessage.SEVERITY_ERROR, getMessageResourceString(beanMessages, "spOb.noFCat"));
//		    		else
//		    			JSFUtils.addMessage("mmsSObform:spCatOb", FacesMessage.SEVERITY_ERROR, getMessageResourceString(beanMessages, "spOb.noFCat"));
//					checkF = false;
//		       }
//			}  else  this.spObekt.setFunkCategory(null);
		
			if (this.dmVidSport.isActive()) {
				if (this.dmVidSport.isRequired() && (this.selectedVidSport  == null || this.selectedVidSport.isEmpty())) {
				
						JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, "Не са въведени видове спорт!");
						
				     checkF = false;
				}	
			} 
				
			if (this.dmOpis.isActive()) {
				if (this.dmOpis.isRequired() && (this.spObekt.getOpisanie() == null || this.spObekt.getOpisanie().trim().isEmpty() )) {
					  if (this.beginInp) 
						  JSFUtils.addMessage("mmsSObform:opis0", FacesMessage.SEVERITY_ERROR, "Не е въведено описание!");
					  else	  
						  JSFUtils.addMessage("mmsSObform:opis", FacesMessage.SEVERITY_ERROR, "Не е въведено описание!");
					  checkF = false;
				}
			}  else  this.spObekt.setOpisanie(null);
			
			if (this.dmIdentif.isActive()) {
				if (this.dmIdentif.isRequired() && (this.spObekt.getIdentif() == null || this.spObekt.getIdentif().trim().isEmpty() )) {
					  if (this.beginInp) 
						  JSFUtils.addMessage("mmsSObform:ident0", FacesMessage.SEVERITY_ERROR, "Не е въведена идентификация  !");
					  else	  
						  JSFUtils.addMessage("mmsSObform:ident", FacesMessage.SEVERITY_ERROR, "Не е въведена идентификация!");
					  checkF = false;
				}
			} else  this.spObekt.setIdentif(null);
				
			if (this.dmDopInfo.isActive()) {
				if (this.dmDopInfo.isRequired() && (this.spObekt.getDopInfo() == null || this.spObekt.getDopInfo().trim().isEmpty() )) {
					
						  JSFUtils.addMessage("mmsSObform:dop", FacesMessage.SEVERITY_ERROR, "Не е въведена доп. информация!");
					  checkF = false;
				}
			}  else  this.spObekt.setDopInfo(null);
			
			
			if (this.dmCountry.isActive()) {
				if (this.dmCountry.isRequired()) {
				       if (this.spObekt.getCountry() == null) {
//							JSFUtils.addMessage("mmsSObform:mestoC", FacesMessage.SEVERITY_ERROR, getMessageResourceString(beanMessages, "spOb.noCountry"));
//							checkF = false;
				    	   this.spObekt.setCountry(this.countryBG);
				       } else {
				        				    	   
					       if (this.spObekt.getCountry().intValue() != this.countryBG.intValue() ) {
					    	   if (this.dmNasMesto.isActive() && this.dmNasMesto.isRequired()) {
					    		   JSFUtils.addMessage("mmsSObform:mestoC", FacesMessage.SEVERITY_ERROR, "Задължително се изисква въвеждане на населено място и държавата трябва да бъде България!");
									checkF = false;
					    	   }  else {
						    	   this.spObekt.setNas_mesto(null);
						    	   this.spObekt.setObsht_obl_t(null);
						    	   
					    	   }	   
					       }  else {
//					    	   if (this.dmNasMesto.isActive() && this.dmNasMesto.isRequired())
//							       if (this.spObekt.getNas_mesto() == null) {
//										JSFUtils.addMessage("mmsSObform:mestoC", FacesMessage.SEVERITY_ERROR, getMessageResourceString(beanMessages, "spOb.noNasM"));
//										checkF = false;
//							       }
					       }    
				       }
				}    
			}  else this.spObekt.setCountry(null);
			  
			 if (this.dmNasMesto.isActive()) {
				 // Винагеи задължително въвеждане на населено място, ако е държава България или не е въведена държава
				   if (this.spObekt.getCountry() == null || this.spObekt.getCountry().intValue() ==  this.countryBG.intValue() ) {
					   if (this.spObekt.getNas_mesto() == null) {
							JSFUtils.addMessage("mmsSObform:mestoC", FacesMessage.SEVERITY_ERROR, getMessageResourceString(beanMessages, "spOb.noNasM"));
							checkF = false;
				       }
					   
				   } else  this.spObekt.setNas_mesto(null);   // Ако е зададена друга държава
				 
			 }
			
//			
//			 if (this.dmNasMesto.isActive() && this.dmNasMesto.isRequired()) {
//			       if (this.spObekt.getNas_mesto() == null) {
//			    	   JSFUtils.addMessage("mmsSObform:mestoC", FacesMessage.SEVERITY_ERROR, getMessageResourceString(beanMessages, "spOb.noNasM"));
//			    	   checkF = false;
//	                }
//			 } else if (!this.dmNasMesto.isActive())   this.spObekt.setNas_mesto(null);
			 
				 
			 if (this.dmAddress.isActive()) {
				 if (this.getDmAddress().isRequired() && (this.spObekt.getAdres() == null || this.spObekt.getAdres().trim().isEmpty())) {
					 JSFUtils.addGlobalMessage( FacesMessage.SEVERITY_ERROR, "Не е въведен адрес за спортния обект!");
			    	   checkF = false;
				 }
			 }  else this.spObekt.setAdres(null);
			 
	       if (this.dmEMail.isActive()) {
		       if (this.spObekt.getE_mail () != null && !this.spObekt.getE_mail ().trim().isEmpty()) {
		    	      // Проверка за въведен e_mail
		    	   if (this.dmEMail.getValidMethod() != null) {
				   		try {
							if (!ValidationUtils.invokeValidation(this.dmEMail.getValidMethod(), this.spObekt.getE_mail ().trim())) {
		   
								JSFUtils.addMessage("mmsSObform:mail", FacesMessage.SEVERITY_ERROR, getMessageResourceString(beanMessages, "spOb.errEMail"));
								checkF = false;
							 }
						} catch (InvalidParameterException e) {
							// TODO Auto-generated catch block
							   if (!ValidationUtils.validateText (this.spObekt.getE_mail ().trim(), 4)) {
									
									JSFUtils.addMessage("mmsSObform:mail", FacesMessage.SEVERITY_ERROR, getMessageResourceString(beanMessages, "spOb.errEMail"));
									checkF = false;
					    	   }	
						
						}	   
		    		   
		    	   }  else {
		    	   
				    	   if (!ValidationUtils.validateText (this.spObekt.getE_mail ().trim(), 4)) {
		
								JSFUtils.addMessage("mmsSObform:mail", FacesMessage.SEVERITY_ERROR, getMessageResourceString(beanMessages, "spOb.errEMail"));
								checkF = false;
				    	   }	
		    	   }   

		       }  else if (this.dmEMail.isRequired())  {
		    	   JSFUtils.addMessage("mmsSObform:mail", FacesMessage.SEVERITY_ERROR, "Не е въведен e_mail за спортния обект!");
					checkF = false;
		       }
	       }  else this.spObekt.setE_mail (null);
	       
	  	 if (this.dmTel.isActive()) {
			 if (this.dmTel.isRequired() && (this.spObekt.getTel() == null || this.spObekt.getTel().trim().isEmpty())) {
				 JSFUtils.addGlobalMessage( FacesMessage.SEVERITY_ERROR, "Не са въведени телефони!");
		    	   checkF = false;
			 }
	  	 } else this.spObekt.setTel(null);
	  		 
	  	if (this.dmPostCode.isActive()) {
			 if (this.dmPostCode.isRequired() && (this.spObekt.getPostCode() == null || this.spObekt.getPostCode().trim().isEmpty())) {
				 JSFUtils.addGlobalMessage( FacesMessage.SEVERITY_ERROR, "Не е въведен пощенски код!");
		    	   checkF = false;
			 }	
	  	}   else  this.spObekt.setPostCode(null);
	  	
		return checkF;
	}
 	
	/**
	 * Запис на спортен обект
	 */
	public void actionSave() {
		if( !checkFields() )
			return;
			
		if (this.inpPdf && this.obLicaList != null && this.obLicaList.size() > 0 ) {
			// Проверка за свързани лица
		   for (int i = 0; i < this.obLicaList.size(); i++) {
			   if ( this.obLicaList.get(i).getIdLice() == null && this.obLicaList.get(i).getIme() == null && this.obLicaList.get(i).getPrezime() == null && this.obLicaList.get(i).getFamilia() == null ) {
					JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, getMessageResourceString(beanMessages, "spOb.noInfoVr"));
					return;
			   }
			   if (  this.obLicaList.get(i).getTypeVrazka() == null)	{
					JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, getMessageResourceString(beanMessages, "spOb.noTypeVr"));
					return;
				}
			   
		   }
		}
	

		try {
		
			if (this.beginInp == true && this.idSpObekt == null) {
				// Получаване на рег. номер за  нов спортен обект = това става при първо вписване и не се прави тук
	//			Integer idRegister = Integer.valueOf(660);     // Тестов регистър
//				try {
////					String rN = obDAO.genRnDocByRegister(idRegister);
////					
////					
////					if (rN == null || rN.trim().isEmpty())  {
////						JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, "Грешка при генериране на рег. номер");
////						return;
////					}
//				
//						String rN = new MMSVpisvaneDAO(getUserData()).genRegNomer(DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_OBJECTS,null, this.spObekt.getNas_mesto());
//						if (rN == null || rN.trim().isEmpty())  {
//							JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, "Грешка при генериране на рег. номер -  връща null рег. номер!");
//							return;
//						}
//						
//						this.spObekt.setRegNomer(rN);    
//							
//				} catch (DbErrorException e1) {
//					JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, "Грешка при получаване рег. номер за спортен обект - " + e1.getLocalizedMessage());
//					return;
//				} catch (InvalidParameterException e1) {
//					JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, "Грешка при получаване рег. номер за спортен обект - " + e1.getLocalizedMessage());
//					return;
//				}
				
				// при нов запис трябва да се сетне за статус - в разглеждане и датата на статуса - днешна
//				this.spObekt.setStatus(Integer.valueOf(DocuConstants.CODE_ZNACHENIE_STATUS_OBEKT_V_RAZGLEJDANE));
//				this.spObekt.setDateStatus(this.currentDate);
				this.spObekt.setStatus(null);
				this.spObekt.setDateStatus(null);
				
				
			}
			
			this.spObekt.setFunkCategory(null);
			this.spObekt.getVidSportList().clear();
			makeVidSportStringArrayAsObject();
			
			if (  this.spObekt.getCountry() == null && this.spObekt.getNas_mesto() != null)
					 this.spObekt.setCountry( this.countryBG);   // При зададено населено място държавата е България
		
				JPA.getUtil().runInTransaction(() -> this.spObekt = obDAO.save(this.spObekt));
			} catch (DbErrorException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, getMessageResourceString(beanMessages, "spOb.errSaveSpOb") + "-" + e.getLocalizedMessage());
				return;
			} catch (BaseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, getMessageResourceString(beanMessages, "spOb.errSaveSpOb") + "-" + e.getLocalizedMessage());
				return;
			}
			
		if (this.spObektLice != null && this.spObektLice.getIdLice() != null)
			actionSaveLiceVrazka ();
		else   clearSelLice();
		
		if (this.inpPdf)  {
			// Първи запис след обработен PDF - допълнително се записват лица за връзка
			if (this.idSpObekt == null) {   // Първа регистрация
				this.idSpObekt = this.spObekt.getId();
				if (this.obLicaList != null && this.obLicaList.size() > 0 ) {
					for (int i = 0; i < this.obLicaList.size(); i++) {
						MMSSportObektLice item = this.obLicaList.get(i);
						item.setIdSportObekt(this.idSpObekt);
						this.obLicaList.set(i,  item);
					}
					saveAllLica(this.obLicaList);
				}
			} else {
				removeAllLica (this.spObekt.getId());
				if (this.obLicaList != null && this.obLicaList.size() > 0 ) {
					for (int i = 0; i < this.obLicaList.size(); i++) {
						MMSSportObektLice item = this.obLicaList.get(i);
						item.setIdSportObekt(this.idSpObekt);
						this.obLicaList.set(i,  item);
					}
					saveAllLica(this.obLicaList);
				}
			}
			if (this.obLicaList != null && this.obLicaList.size() > 0 )
			   this.obLicaList = findLicaVr ();                 // С новите id за записите
			clearSelLice ();
			
			this.inpPdf = false;
		}
		
		// success  zapis sporten obekt
	//	JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO,IndexUIbean.getMessageResourceString("ui_beanMessages", SUCCESSAVEMSG));		

		boolean lockYes = true; 
		if (this.beginInp == true) {  
			if (this.idSpObekt == null) {
				// Нова регистрация
				this.idSpObekt = this.spObekt.getId();
//				if (!this.efrm)
//				  JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, "Моля чрез бутона за данни за вписване въведете първото вписване за регистрация и направете актуализиращ запис за въведения спортен обект!");
//								
			} else {
				// Актуализация
//				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, "Моля  направете актуализиращ запис на избания спортен обект задължително, за да се обнови статуса ако е необходимо!");
			}
			
			// Заключване за новия обект
			// **********************************************************************************************************************
			lockObject (Integer.valueOf(DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_OBJECTS), this.spObekt.getId(), Integer.valueOf(1));
		
			if (this.messLock != null) {     // Грешка при заключване на нов обект
				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, this.messLock);
				 lockYes = false;
				this.messLock =  null;
			}
			// **********************************************************************************************************************
			
			
			this.beginInp =false;
		}
		
		//TODO - ако идва от СЕОС след първия запис се вика метода за запис на вписване, документ и промяна на статуса в EgovMessages - ДЕСИ!
		boolean zapZaiavl = true;
		if (this.idSSev != null) {
			zapZaiavl = actionSaveDocFromSeos();
			
			findVpisvAll();
			findDocs();
		}   
		
		if ((this.idSSev == null || zapZaiavl) && lockYes )
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO,IndexUIbean.getMessageResourceString("ui_beanMessages", SUCCESSAVEMSG));	
		else {
			if (lockYes && !zapZaiavl)
			  JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, "Има проблем със запис на заявлението!");
			else
				 JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, "Има проблем със заключване на спортния обект и със запис на заявлението! ");
		}	
		
		
	}
	
	
	public void registerNewVpisvane() {
	
	}
	public void registerNewDoc () {
		
	}
	
    public void findVpisvAll () {
		
		// Прочитане на всички записани вписвания за спортния обект
		try {		
			
			JPA.getUtil().runWithClose(() -> setRegsList(new LazyDataModelSQL2Array((new MMSVpisvaneDAO(getUserData()).findRegsListNativeSMDLast(DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_OBJECTS, spObekt.getId())), " DATE_DOC_ZAIAVLENIE  asc ")));
				
		
		} catch (BaseException e) {
			LOGGER.error("Грешка при зареждане на списъка с вписвания! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getLocalizedMessage());
			return;
		}
		
		// Всички документи
		findDocs();
		
		// Статусът на спортния обект се формира при запис на поредно вписване извън този бин
		
		// Статус за последно вписване
		// Получаване статус и дата статус за последно вписване - за спортен обект се взима статус на вписване 
//		List<MMSVpisvane> vpList = null;
//		vpList = new  MMSVpisvaneDAO(getUserData()).findRegsListMaxDateObr(DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_OBJECTS, spObekt.getId());
//		if (vpList == null || vpList.isEmpty()) {
//			this.status_posl_vp = null;
//			this.date_posl_vp = null;
//		} else {
//			MMSVpisvane vp = vpList.get(0);
//			Integer id1 = vp.getId();
//			this.status_posl_vp = vp.getStatusVpisvane();
//			 this.date_posl_vp = vp.getDateStatusVpisvane();
//			 // Ако  vp.getDateLastMod() != null)  няма null стойности за date_last_mod   и този  първи запис е последно вписване (има сортировка по намаляващи стойности)
//				
//				if (vp.getDateLastMod() == null) {   
//					Date dReg1 =  vp.getDateReg ();
//				    // Всички записи  с null стойности за data_last_mod са подредени отпред - търсим първия запис с ненулева стойност
//					
//					for (int i = 0 ; i <  vpList.size(); i++) {
//						if (vpList.get(i).getDateLastMod() != null) {
//							MMSVpisvane vp1 = vpList.get(i);
//							if ((vp1.getDateLastMod().compareTo(dReg1) > 0) || (vp1.getDateLastMod().compareTo( dReg1) == 0) && vp1.getId().intValue() > id1.intValue()) {
//								// Това ще е действителния запис за последно вписване
//								this.status_posl_vp = vp1.getStatusVpisvane();
//								 this.date_posl_vp = vp1.getDateStatusVpisvane();
//								 break;
//							}
//						}
//					}		
//				}
//		}
		
	//  Зареждане статус за спортен обект - от статуса на вписване
		
//			if (this.status_posl_vp != null) {
//				// Първо актуализация в базата за статус
//				
//				try {
//	    			JPA.getUtil().begin();
//	    			  this.obDAO.updateStatusInSpObekt  (this.spObekt.getId(),this.status_posl_vp,  this.date_posl_vp);
//	    		      JPA.getUtil().commit();
//	    		} catch (BaseException e) {
//	    			JPA.getUtil().rollback();
//	    			LOGGER.error("Грешка при актуализация на статус и дата статус за спортен обект! ", e);
//	    			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, "Грешка при актуализация на статус и дата статус за спортен обект! - " + e.getLocalizedMessage());
//	    			return;
//	    		}
//				
//								
//				this.spObekt.setStatus(this.status_posl_vp);
//				 this.spObekt.setDateStatus(this.date_posl_vp);
//			}
				
    }		
	  
	public void findVpisvAll_old () {
		
		// Прочитане на всички записани вписвания за спортния обект
		try {	
			
			JPA.getUtil().runWithClose(() -> setRegsList(new LazyDataModelSQL2Array((new MMSVpisvaneDAO(getUserData()).findRegsListNativeSMDLast(spObekt.getId(),  DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_OBJECTS)), " ID asc ")));
					
		} catch (BaseException e) {
			LOGGER.error("Грешка при зареждане на списъка с вписвания! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getLocalizedMessage());
			return;
		}
		
		//  При формиране на записване
/*		
       Статус "В разглеждане"
        CODE_ZNACHENIE_STATUS_REG_IN_REVIEW - този статус е, когато се подава заявление за вписване и се въвежда рег. номер и дата на заявлението за вписване в колоните rn_doc_zaiavlenie и date_doc_zaiavlenie;

         Статус "Завършено"
         CODE_ZNACHENIE_STATUS_REG_DONE - този статус е, когато има заповед за вписване и се въвежда рег. номер и дата на заповедта за вписване в колоните rn_doc_result и date_doc_result и също има поле за основание (ако някой изобщо го засяга това поле, освен мен) - reason_result;

		Статус "Отказано"
		CODE_ZNACHENIE_STATUS_REG_DENIED - този статус е, когато има заповед за отказ и се въвежда рег. номер и дата на заповедта за отказ в колоните rn_doc_result и date_doc_result и съответно пак има поле за основание - reason_result;

		Статус "Прекратено" 
		CODE_ZNACHENIE_STATUS_REG_STOPED - този статус е, когато има заповед за прекратяване и се въвежда рег. номер и дата на заповедта за прекратяване в колоните rn_doc_spirane и date_doc_spirane и тук пак има поле за основание - reason_spirane;     

		Статус "Отнето" 
		CODE_ZNACHENIE_STATUS_REG_TAKEN - този статус е, когато има заповед за отнемане и се въвежда рег. номер и дата на заповедта за отнемане в колоните rn_doc_spirane и date_doc_spirane и поле за основание - reason_spirane;     

		Статус "Заличено" 
		CODE_ZNACHENIE_STATUS_REG_DELETED - този статус е, когато има заповед за заличаване и се въвежда рег. номер и дата на заповедта за заличаване в колоните rn_doc_spirane и date_doc_spirane и поле за основание - reason_spirane;     

		И съответно рег. номера и датата на лиценза стоят в колоните rn_doc_licenz и date_doc_licenz;
*/
			
		// Получаване статус и дата статус за последно вписване
//		List<MMSVpisvane> vpList = null;
//		vpList = new  MMSVpisvaneDAO(getUserData()).findRegsListMaxDateObr(DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_OBJECTS, spObekt.getId());
//		if (vpList == null || vpList.isEmpty()) {
//			this.status_posl_vp = null;
//			this.date_posl_vp = null;
//		} else {
//			MMSVpisvane vp = vpList.get(0);
//			Integer id1 = vp.getId();
//			this.status_posl_vp = vp.getStatus();
//			 switch (this.status_posl_vp.intValue()) {
//		    	 case DocuConstants.CODE_ZNACHENIE_STATUS_REG_IN_REVIEW :       // В разглеждане
//		    		 this.date_posl_vp = vp.getDateDocZaiavlenie();
//		       		 break;
//		    	 case DocuConstants.CODE_ZNACHENIE_STATUS_REG_DONE :                  // Регистриран
//		    		 this.date_posl_vp = vp.getDateDocResult();
//		       		 break;
//		    	
//		     	 case DocuConstants.CODE_ZNACHENIE_STATUS_REG_DENIED:      // Отказано
//		     		 this.date_posl_vp = vp.getDateDocResult();
//		    		 break;	 	 	 
//		    		 
//		    	 case DocuConstants.CODE_ZNACHENIE_STATUS_REG_STOPED:    // Прекратено
//		    		 this.date_posl_vp = vp.getDateDocSpirane();
//		    		 break;
//		    	 case DocuConstants.CODE_ZNACHENIE_STATUS_REG_TAKEN:       // Отнето  
//		    		 this.date_posl_vp = vp.getDateDocSpirane();
//		    		 break;	
//		   		    				    		 
//		    	 case DocuConstants.CODE_ZNACHENIE_STATUS_REG_DELETED :             // Заличен
//		    		 this.date_posl_vp = vp.getDateDocSpirane();
//		    		 break;	 
//		    	
//		     } 
//			
//			 // Ако  vp.getDateLastMod() != null)  няма null стойности за date_last_mod   и този  първи запис е последно вписване (има сортировка по намаляващи стойности)
//		
//			if (vp.getDateLastMod() == null) {   
//				Date dReg1 =  vp.getDateReg ();
//			    // Всички записи  с null стойности за data_last_mod са подредени отпред - търсим първия запис с ненулева стойност
//				
//				for (int i = 0 ; i <  vpList.size(); i++) {
//					if (vpList.get(i).getDateLastMod() != null) {
//						MMSVpisvane vp1 = vpList.get(i);
//						if ((vp1.getDateLastMod().compareTo(dReg1) > 0) || (vp1.getDateLastMod().compareTo( dReg1) == 0) && vp1.getId().intValue() > id1.intValue()) {
//							// Това ще е действителния запис за последно вписване
//							this.status_posl_vp = vp1.getStatus();
//						
//							 switch (this.status_posl_vp.intValue()) {
//						    	 case DocuConstants.CODE_ZNACHENIE_STATUS_REG_IN_REVIEW :       // В разглеждане
//						    		 this.date_posl_vp = vp1.getDateDocZaiavlenie();
//						       		 break;
//						    	 case DocuConstants.CODE_ZNACHENIE_STATUS_REG_DONE :                  // Регистриран
//						    		 this.date_posl_vp = vp1.getDateDocResult();
//						       		 break;
//						    	
//						     	 case DocuConstants.CODE_ZNACHENIE_STATUS_REG_DENIED:      // Отказано
//						     		 this.date_posl_vp = vp1.getDateDocResult();
//						    		 break;	 	 	 
//						    		 
//						    	 case DocuConstants.CODE_ZNACHENIE_STATUS_REG_STOPED:    // Прекратено
//						    		 this.date_posl_vp = vp1.getDateDocSpirane();
//						    		 break;
//						    	 case DocuConstants.CODE_ZNACHENIE_STATUS_REG_TAKEN:       // Отнето  
//						    		 this.date_posl_vp = vp1.getDateDocSpirane();
//						    		 break;	
//						   		    				    		 
//						    	 case DocuConstants.CODE_ZNACHENIE_STATUS_REG_DELETED :             // Заличен
//						    		 this.date_posl_vp = vp1.getDateDocSpirane();
//						    		 break;	 
//						    	
//						     } ;
//						}
//						
//						// Ако първата нненулева стойност за dateLastMod е по малка от dateReg на първия запис - той е записът с последно вписване 
//						break;
//					}
//				}
//				
//			};
//		}  
//		
//		//  Зареждане статус за спортен обект - от статуса на вписване
//
//		if (this.status_posl_vp != null) {
//		     switch (this.status_posl_vp.intValue()) {
//		    	 case DocuConstants.CODE_ZNACHENIE_STATUS_REG_IN_REVIEW :       // В разглеждане
//		    		 this.spObekt.setStatus(Integer.valueOf(DocuConstants.CODE_ZNACHENIE_STATUS_REG_IN_REVIEW));
//		    		 break;
//		    	 case DocuConstants.CODE_ZNACHENIE_STATUS_REG_DONE :                  // Регистриран
//		    		 this.spObekt.setStatus(Integer.valueOf(DocuConstants.CODE_ZNACHENIE_STATUS_REG_DONE));
//		    		 break;
//		    		 
//		    	 case DocuConstants.CODE_ZNACHENIE_STATUS_REG_STOPED:    // Прекратено
//		    		 this.spObekt.setStatus(Integer.valueOf(DocuConstants.CODE_ZNACHENIE_STATUS_OBEKT_OTKAZ_REGISTR));  // Отказ от регистрация
//		    		 break;
//		    	 case DocuConstants.CODE_ZNACHENIE_STATUS_REG_TAKEN:       // Отнето  
//		    		 this.spObekt.setStatus(Integer.valueOf(DocuConstants.CODE_ZNACHENIE_STATUS_OBEKT_OTKAZ_REGISTR));
//		    		 break;	
//		    	 case DocuConstants.CODE_ZNACHENIE_STATUS_REG_DENIED:      // Отказано
//		    		 this.spObekt.setStatus(Integer.valueOf(DocuConstants.CODE_ZNACHENIE_STATUS_OBEKT_OTKAZ_REGISTR));
//		    		 break;	 	 
//		    				    		 
//		    	 case DocuConstants.CODE_ZNACHENIE_STATUS_REG_DELETED :             // Заличен
//		    		 this.spObekt.setStatus(Integer.valueOf(DocuConstants.CODE_ZNACHENIE_STATUS_OBEKT_DELETED));
//		    		 break;	 
//		    	
//		     } 
//		     
//		     this.spObekt.setDateStatus(this.date_posl_vp);
//		}    
		
		
		// Всички документи
		findDocs();
		
	}	
	
	private void findDocs() {
		try {
			JPA.getUtil().runWithClose(() -> setDocsList(new LazyDataModelSQL2Array((new MMSVpisvaneDAO(getUserData()).findDocsList(spObekt.getId(),  DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_OBJECTS)), " doc_date asc ")));
		} catch (BaseException e) {
			LOGGER.error("Грешка при зареждане на списъка с документи към  вписвания! ", e);
		//	JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getLocalizedMessage());
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, getMessageResourceString(UI_beanMessages, "so.errFindDoc"));
		}
	}
	
//	private void findLicaVr () {
//		try {
//			JPA.getUtil().runWithClose(() -> setLicaList(new LazyDataModelSQL2Array((new MMSSportObektDAO(getUserData()).findLicaVrSpOb(spObekt.getId())), " id asc ")));
//		} catch (BaseException e) {
//			LOGGER.error("Грешка при зареждане на списъка с лица за връзка със спортен обект ! ", e);
//			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getLocalizedMessage());
//		}
		
//	}
	
	private List <MMSSportObektLice>  findLicaVr ()    {
		
		List <MMSSportObektLice> listL = null;
	
        try {
			 listL =  this.obDAO.findSpObLice(spObekt.getId()); 
			} catch (DbErrorException e) {
			LOGGER.error("Грешка при зареждане на списъка с лица за връзка със спортен обект ! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getLocalizedMessage());
			return null;
		}
			
//		} catch (BaseException e) {
//			LOGGER.error("Грешка при зареждане на списъка с лица за връзка със спортен обект ! ", e);
//			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getLocalizedMessage());
//		}
			 
			 if (listL != null && listL.size() > 0) {
				 // Заместване идентификацията за лицата с актуалните стойности
				 for (int i = 0; i <  listL.size(); i++) {
					 
					 MMSSportObektLice item = listL.get(i);
					 String name= "";
					 if (item.getIdLice() != null)
					     name=  getNameLiceLink (item.getIdLice()) ;
					 else {
						 if (item.getIme()!= null && !item.getIme().trim().isEmpty()) name += item.getIme().trim();
						 if (item.getPrezime()!= null && !item.getPrezime().trim().isEmpty()) name +=  " " + item.getPrezime().trim();
						 if (item.getFamilia()!= null && !item.getFamilia().trim().isEmpty()) name +=  " " + item.getFamilia().trim();
						 name = name.trim();
					 }
					 
	    			 item.setNameLice(name);
					 listL.set(i, item);
				 }
			 }
			 
			 
				 return listL;
		
	}
	
   private List <MMSSportObektLice>  setLicaVr ()    {
		
		List <MMSSportObektLice> listL = null;
		
		    if (this.spObekt != null ) {	 
              if (this.spObekt.getSpObLice() != null ) {
            	  this.spObekt.getSpObLice().size();
            	  if (this.spObekt.getSpObLice().size() > 0) {
            		  listL = new  ArrayList<MMSSportObektLice>() ;
            			
						 // Заместване идентификацията за лицата с актуалните стойности
						 for (int i = 0; i < this.spObekt.getSpObLice().size(); i++) {
							 
							 MMSSportObektLice item = this.spObekt.getSpObLice().get(i);
							 String name= "";
							 if (item.getIdLice() != null)
							     name=  getNameLiceLink (item.getIdLice()) ;
							 else {
								 if (item.getIme()!= null && !item.getIme().trim().isEmpty()) name += item.getIme().trim();
								 if (item.getPrezime()!= null && !item.getPrezime().trim().isEmpty()) name +=  " " + item.getPrezime().trim();
								 if (item.getFamilia()!= null && !item.getFamilia().trim().isEmpty()) name +=  " " + item.getFamilia().trim();
								 name = name.trim();
							 }
			    			 item.setNameLice(name);
							 listL.add(item);
						 }
				 }
               }
              }		    
			 
			 
				 return listL;
				 
	}
	
	
	/**
	 * Търсене на записани спортни обекти с близки имена - като List
	 */
//  private List <MMSSportObekt>  findObWithEQNames (String name) {
//	   if (name == null || name.trim().isEmpty())  return null;
//		
//		List <MMSSportObekt> listL = null;
//	
//		 try {
//			 listL =  this.obDAO. findSpObImena(name); 
//			} catch (DbErrorException e) {
//				LOGGER.error("Грешка при зареждане на списъка с въведени спортни обекти със сходни имена  ! ", e);
//				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getLocalizedMessage());
//				return null;
//			}	
////		} catch (BaseException e) {
////			LOGGER.error("Грешка при зареждане на списъка с лица за връзка със спортен обект ! ", e);
////			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getLocalizedMessage());
////		}
//			 
//			 return listL;
//		
//	}

	/**
	 * Търсене на записани спортни обекти с близки имена  
	 */
	public void   findObWithEQNames (String name) {
		   if (name == null || name.trim().isEmpty()) {
			   this.obList = null;
			   return ;
		   }
			
		   try {
				JPA.getUtil().runWithClose(() -> setObList(new LazyDataModelSQL2Array(obDAO. findSpObListImena(name), " ID asc ")));
			} catch (BaseException e) {
				LOGGER.error("Грешка при извличане на списък с въведени спортни обекти  ", e);
			    JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, "Грешка при извличане на списък с въведени спортни обекти  - " + e.getLocalizedMessage());
			}
			
	}
	
	
	public void editLiceVrazkaFromList (MMSSportObektLice lice) {
		clearSelLice ();
		this.spObektLice.setId(lice.getId());
		this.spObektLice.setIdSportObekt(this.getSpObekt().getId());
		this.spObektLice.setIdLice(lice.getIdLice());;
		
		this.spObektLice.setNameLice(lice.getNameLice());
		this.spObektLice.setIme(lice.getIme());
		this.spObektLice.setPrezime(lice.getPrezime());
		this.spObektLice.setFamilia(lice.getFamilia());
		this.spObektLice.setTypeVrazka(lice.getTypeVrazka ());
		this.spObektLice.setUserReg(lice.getUserReg());
		this.spObektLice.setDateReg(lice.getDateReg());
		this.spObektLice.setUserLastMod(getUserData().getUserId());
		this.spObektLice.setDateLastMod(new Date());
		
	}
	
//	/**
//	 * Получаване идентификация за лице във връзка с спортен обект
//	 * @param idLice -  id за  лицe
//	 *   @return  - name 
//	 */
//	public String  getNameLiceLink (Integer idLice)  {
//		Referent objRef = null;
//		
//		try {
//			 objRef = new ReferentDAO ( getUserData()).findById(idice);
//		} catch (DbErrorException e) {
//			
//			return null;
//		}
//		
//		if (objRef == null)  return null;
//		String name = "";
//		if (objRef.getRefName() != null)  name += objRef.getRefName().trim();
//		if (objRef.getRefType() != null && objRef.getRefType().intValue() == 2)  {      // Физ. лице
//			if (objRef.getFzlEgn() != null && !objRef.getFzlEgn().trim().isEmpty())  name += " (ЕГН: " + objRef.getFzlEgn().trim() + ")";
//			else if  (objRef.getFzlLnc() != null && !objRef.getFzlLnc().trim().isEmpty()) name += " (ЛНЧ: " + objRef.getFzlLnc().trim() + ")";
//		}  else {
//			if (objRef.getNflEik() != null && !objRef.getNflEik().trim().isEmpty())  name += " (ЕИК: " + objRef.getNflEik().trim() + ")";
//		}
//		
//		
//		
//		// Адрес
//		ReferentAddress refAddr = objRef.getAddress();
//		if (refAddr == null) {
//			return name;
//		}
//		String nasM = null, obsht_obl = null;
//		try {
//		     nasM = getSystemData().decodeItem(DocuConstants.CODE_CLASSIF_EKATTE, refAddr.getEkatte(), this.lang, this.currentDate);
//		    obsht_obl =  getSystemData().decodeItemDopInfo(DocuConstants.CODE_CLASSIF_EKATTE, refAddr.getEkatte(), this.lang, this.currentDate);
//		} catch (DbErrorException e)  {
//			return name;
//		}
//			
//		if (nasM != null) {  
//			name += "; " + nasM;
//			if (obsht_obl != null)  name += ", " + obsht_obl;
//		}
//	
//		return name;
//		
//	}
	
	/**
	 * Получаване идентификация за лице във връзка с спортен обект
	 * @param codeLice -  code за  лицe
	 *   @return  - name 
	 */
	public String  getNameLiceLink (Integer codeLice)  {
		if (codeLice == null)  return null;
		
		Referent objRef = null;
		
		try {
			 objRef = new ReferentDAO ( getUserData()).findByCodeRefLast(codeLice);
		} catch (DbErrorException e) {
			
			return null;
		}
		
		if (objRef == null)  return null;
		String name = "";
		if (objRef.getRefName() != null && !objRef.getRefName().trim().isEmpty())  name += objRef.getRefName().trim();
		else {
			
			if (objRef.getRefType() != null && objRef.getRefType().intValue() == 4)  {   // За физ. лице
				String s = "";
				if (objRef.getIme() != null && !objRef.getIme().trim().isEmpty())  s += objRef.getIme().trim();
				if (objRef.getPrezime() != null && !objRef.getPrezime().trim().isEmpty()) s+= " " + objRef.getPrezime().trim();
				if (objRef.getFamilia() != null && !objRef.getFamilia().trim().isEmpty()) s+= " " + objRef.getFamilia().trim();
				s = s.trim();
				if (!s.isEmpty())  name += s;  
			}
		}
		if (objRef.getRefType() != null && objRef.getRefType().intValue() == 4)  {      // Физ. лице = 4; юрид. лице . 3   
			if (objRef.getFzlEgn() != null && !objRef.getFzlEgn().trim().isEmpty())  name += " (ЕГН: " + objRef.getFzlEgn().trim() + ")";
			else if  (objRef.getFzlLnc() != null && !objRef.getFzlLnc().trim().isEmpty()) name += " (ЛНЧ: " + objRef.getFzlLnc().trim() + ")";
		}  else {
			if (objRef.getNflEik() != null && !objRef.getNflEik().trim().isEmpty())  name += " (ЕИК: " + objRef.getNflEik().trim() + ")";
		}
		
		
		
		// Адрес
		ReferentAddress refAddr = objRef.getAddress();
		if (refAddr == null) {
			return name;
		}
		String nasM = null, obsht_obl = null;
		try {
		     nasM = getSystemData().decodeItem(DocuConstants.CODE_CLASSIF_EKATTE, refAddr.getEkatte(), this.lang, this.currentDate);
		    obsht_obl =  getSystemData().decodeItemDopInfo(DocuConstants.CODE_CLASSIF_EKATTE, refAddr.getEkatte(), this.lang, this.currentDate);
		} catch (DbErrorException e)  {
			return name;
		}
			
		if (nasM != null) {  
			name += "; " + nasM;
			if (obsht_obl != null)  name += ", " + obsht_obl;
		}
	
		return name;
		
	}
	
	/**
	 * Получаване идентификация за лице във връзка с спортен обект
	 * @param idLice - id на лице
	 * @return 
	 */
	public Referent getNameLiceLinkPoCode (Integer codeRef)  {
		if (codeRef == null)  return null;
		
			Referent r = null;	
		try {
			r = new ReferentDAO ( getUserData()).findByCodeRefLast(codeRef);
		} catch (DbErrorException e) {
			
			return null;
		}
		
		if (r == null)  return null;
		String name = "";
		if (r.getRefName() != null && !r.getRefName().trim().isEmpty())  name += r.getRefName().trim();
		else {
		
			if (r.getRefType() != null && r.getRefType().intValue() == 4)  {   // За физ. лице
				String s = "";
				if (r.getIme() != null && !r.getIme().trim().isEmpty())  s += r.getIme().trim();
				if (r.getPrezime() != null && !r.getPrezime().trim().isEmpty()) s+= " " + r.getPrezime().trim();
				if (r.getFamilia() != null && !r.getFamilia().trim().isEmpty()) s+= " " + r.getFamilia().trim();
				s = s.trim();
				if (!s.isEmpty())  name += s;  
			}
		}
		
		if (r.getRefType() != null && r.getRefType().intValue() == 4)  {       // Физ. лице = 4; юрид. лице . 3 
			if (r.getFzlEgn() != null && !r.getFzlEgn().trim().isEmpty())  name += " (ЕГН: " + r.getFzlEgn().trim() + ")";
			else if  (r.getFzlLnc() != null && !r.getFzlLnc().trim().isEmpty()) name += " (ЛНЧ: " + r.getFzlLnc().trim() + ")";
		}  else {
			if (r.getNflEik() != null && !r.getNflEik().trim().isEmpty())  name += " (ЕИК: " + r.getNflEik().trim() + ")";
		}
		
		// Адрес
		ReferentAddress refAddr = r.getAddress();
		if (refAddr == null) { 
			r.setDbRefName(name);;
			return r;
		}	
		String nasM = null, obsht_obl = null;
		try {
		     nasM = getSystemData().decodeItem(DocuConstants.CODE_CLASSIF_EKATTE, refAddr.getEkatte(), this.lang, this.currentDate);
		    obsht_obl =  getSystemData().decodeItemDopInfo(DocuConstants.CODE_CLASSIF_EKATTE, refAddr.getEkatte(), this.lang, this.currentDate);
		} catch (DbErrorException e)  {
			r.setDbRefName(name);;
			return r;
		}
			
		if (nasM != null) {
			name += "; " + nasM;
			if (obsht_obl != null)  name += ", " + obsht_obl;
		}
		
		
		r.setDbRefName(name);;
		return r;
		
	}
	
	public void setIdViewLice (MMSSportObektLice lice) {
		 // В idLice  се намира code от таблица adm_referents за лица - за лицата физически июридически  той е уникален - не се поддържа история(
		this.setIdLiceView(Integer.valueOf(lice.getIdLice()));     
		
	}
	
	// Изтриване на свързано лице
	public void  removeLiceFromList(MMSSportObektLice lice) {
       if (this.obLicaList.size() > 0)  	{
    	    // Физическо изтриване от базата
    		try {
    			JPA.getUtil().begin();
    		      new MMSSportObektLiceDAO(MMSSportObektLice.class, getUserData()).deleteFromSpObLiceTbl(lice.getId(), lice.getIdLice(), lice.getIdSportObekt()) ;
    		      JPA.getUtil().commit();
    		} catch (BaseException e) {
    			JPA.getUtil().rollback();
    			LOGGER.error("Грешка при изтриване на връзка със свързано лице! ", e);
    			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, "Грешка при изтриване на връзка със свързано лице от базата! - " + e.getLocalizedMessage());
    			return;
    		}
    	  
    		  if (this.spObektLice != null && this.spObektLice.getId() != null && this.spObektLice.getId().intValue() == lice.getId().intValue())  clearSelLice ();
    		  this.obLicaList.remove(lice);
    		
    		  JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, "Успешно премахване  връзка със свързано лице! ");
       }
	}
	
	/**
	 *  Изтриване на всички свързани лица за спортен обект
	 * @param idSpOb - id на спортен обект
	 */
		public void  removeAllLica(Integer idSpOb) {
	    
	    	    // Физическо изтриване от базата на всички свързани лица за спортен обект
	    		try {
	    			JPA.getUtil().begin();
	    		      new MMSSportObektLiceDAO(MMSSportObektLice.class, getUserData()).deleteFromSpObLiceTbl(null, null, idSpOb) ;
	    		      JPA.getUtil().commit();
	    		} catch (BaseException e) {
	    			JPA.getUtil().rollback();
	    			LOGGER.error("Грешка при изтриване на всички свързани лица за спортния обект! ", e);
	    			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, "Грешка при изтриване на връзка със свързано лице от базата! - " + e.getLocalizedMessage());
	    			return;
	    		}
	    	        
		}
   
	public Integer  setCntryBG () {
		int cBG = 37;   // Код EKATTE за България
	
			try {
				cBG = Integer.parseInt(getSystemData().getSettingsValue("delo.countryBG"));
				if (cBG <= 0)  cBG = 37;
				
			} catch (Exception e) {
				LOGGER.error("Грешка при определяне на код на държава България от настройка: delo.countryBG", e);
				cBG = 37;
			}
	
		
		return  Integer.valueOf(cBG);
	}
	
	public Integer getCountryBG() {
		
		return  this.countryBG;
	
	}

	public void setCountryBG(Integer countryBG) {
		this.countryBG = countryBG;
	}

	/** 
	 *   1 само области; 2 - само общини; 3 - само населени места; без специфики - всикчи
	 * @return
	 */
	@SuppressWarnings({ "unchecked" })
	public Map<Integer, Object> getSpecificsEKATTE() {
//		Map<Integer, Object> eval = (Map<Integer, Object>) getStateHelper().eval(PropertyKeys.EKATTESPEC, null);
//		return eval != null ? eval : Collections.singletonMap(SysClassifAdapter.EKATTE_INDEX_TIP, 3);
		return Collections.singletonMap(SysClassifAdapter.EKATTE_INDEX_TIP, 3);
	}
	
	public void actionChangeVid () {
		
	}
	
	 /**
	    * При смяна на държава - да се нулира полето за ЕКАТЕ
	    */
	   public void  actionChangeCountry() {
		   
		   this.spObekt.setNas_mesto(null);
		   this.spObekt.setObsht_obl_t(null);
		   this.spObekt.setAdres(null);;
		   this.spObekt.setPostCode(null);;
	
	   }
	   
	   public void actionChangeNasMesto () {
		   
		try {
			this.spObekt.setObsht_obl_t(getSystemData().decodeItemDopInfo(DocuConstants.CODE_CLASSIF_EKATTE, this.spObekt.getNas_mesto(), this.lang,this.currentDate));
		} catch (DbErrorException e) {
			// TODO Auto-generated catch block
			this.spObekt.setObsht_obl_t (null);
		}
		   
	   }

	public Date getCurrentDate() {
		if (this.currentDate == null)  this.currentDate = new Date();
		return  this.currentDate;
		
	}

	public void setCurrentDate(Date currentDate) {
		this.currentDate = currentDate;
		
	}

//	public TimeZone getTimeZone() {
//		return timeZone;
//	}
//
//	public void setTimeZone(TimeZone timeZone) {
//		this.timeZone = timeZone;
//	}

	public int getViewOnly() {
		return viewOnly;  
	}

	public void setViewOnly(int viewOnly) {
		this.viewOnly = viewOnly;
	}
		

	public boolean isBeginInp() {
		return beginInp;
	}

	public void setBeginInp(boolean beginInp) {
		this.beginInp = beginInp;
	}

	public MMSSportObekt getSpObekt() {
		return spObekt;
		
	}

	public void setSpObekt(MMSSportObekt spObekt) {
		this.spObekt = spObekt;
		
	}

	public Integer getLang() {
		return lang;
	}

	public void setLang(Integer lang) {
		this.lang = lang;
	}

	public Referent getReferent() {
		return referent;
	}

	public void setReferent(Referent referent) {
		this.referent = referent;
	}

	
	public String getReg_nom_zaiavl() {
		return reg_nom_zaiavl;
		
	}

	public void setReg_nom_zaiavl(String reg_nom_zaiavl) {
		this.reg_nom_zaiavl = reg_nom_zaiavl;
		
	}

	public Date getDate_reg_zaiavl() {
		return date_reg_zaiavl;
		
	}

	public void setDate_reg_zaiavl(Date date_reg_zaiavl) {
		this.date_reg_zaiavl = date_reg_zaiavl;
		
	}

	public LazyDataModelSQL2Array getLicaList() {
		return licaList;
		
	}

	public void setLicaList(LazyDataModelSQL2Array licaList) {
		this.licaList = licaList;
		
	}

	public LazyDataModelSQL2Array getDocsList() {
		return docsList;
		
	}

	public LazyDataModelSQL2Array setDocsList(LazyDataModelSQL2Array docsList) {
		this.docsList = docsList;
		
		return docsList;
	}



	public List<MMSSportObektLice> getObLicaList() {
		return obLicaList;
		
	}

	public void setObLicaList(List<MMSSportObektLice> obLicaList) {
		this.obLicaList = obLicaList;
		
	}

	public MMSSportObektLice getSpObektLice() {
		
		if (this.spObektLice != null &&  this.spObektLice.getNameLice() == null) {
			if (this.spObektLice.getIdLice() != null) {
				// В his.spObektLice.getIdLice()  e code от таблица adm_referents - уникален за физически и юридически лица - за тях не се поддържа история
				Referent r = getNameLiceLinkPoCode (this.spObektLice.getIdLice());   
				if (r != null) {
				   this.spObektLice.setNameLice(r.getDbRefName());   // Получаване  формирана идентификация за лицето
				   this.spObektLice.setIdLice(r.getCode());      // В idLice се поставя code за лицето
				   this.spObektLice.setIdSportObekt(this.spObekt.getId());
				}   
			} else {
				String name = "";
				 if (this.spObektLice.getIme()!= null && !this.spObektLice.getIme().trim().isEmpty()) name += this.spObektLice.getIme().trim();
				 if (this.spObektLice.getPrezime()!= null && !this.spObektLice.getPrezime().trim().isEmpty()) name +=  " " + this.spObektLice.getPrezime().trim();
				 if (this.spObektLice.getFamilia()!= null && !this.spObektLice.getFamilia().trim().isEmpty()) name +=  " " + this.spObektLice.getFamilia().trim();
				 name = name.trim();
				 this.spObektLice.setNameLice(name);
				  this.spObektLice.setIdSportObekt(this.spObekt.getId());
			}
		}
		return spObektLice;
		  
	}
	
	
	public void setSpObektLice(MMSSportObektLice spObektLice) {
	
		this.spObektLice = spObektLice;
		
		
	}

	
	   public MMSSportObektLice    zarSpObektLice( Integer codeRef) {
			
		   MMSSportObektLice lice = new MMSSportObektLice ();
			
				Referent r = getNameLiceLinkPoCode (codeRef);
				if (r != null) {
				  lice.setNameLice(r.getDbRefName());   // Получаване  формирана идентификация за лицето
				   lice.setIdLice(r.getCode());      // Получаване в idLice  на  code за лицето
				  lice.setIdSportObekt(this.spObekt.getId());
				}   
	
	   
			return  lice;
			  
		}
	
	
	public Integer getIdLiceView() {
		return idLiceView;
		  
	}

	public void setIdLiceView(Integer idLiceView) {
		this.idLiceView = idLiceView;
		
	}

	public String getZaglModalProverka() {
		return zaglModalProverka;
		
	}

	public void setZaglModalProverka(String zaglModalProverka) {
		this.zaglModalProverka = zaglModalProverka;
		
	}


	public Integer getStatus_posl_vp() {
		return status_posl_vp;
	}

	public void setStatus_posl_vp(Integer status_posl_vp) {
		this.status_posl_vp = status_posl_vp;
	}

	public Date getDate_posl_vp() {
		return date_posl_vp;
	}

	public void setDate_posl_vp(Date date_posl_vp) {
		this.date_posl_vp = date_posl_vp;
	}

	public MMSVpisvane getReg() {
		return reg;
		
	}

	public void setReg(MMSVpisvane reg) {
		this.reg = reg;
		
	}

	public List<MMSVpisvaneDoc> getRegDocsList() {
		return regDocsList;
		
	}

	public void setRegDocsList(List<MMSVpisvaneDoc> regDocsList) {
		this.regDocsList = regDocsList;
		
	}

	public List<Files> getRegFilesList() {
		return regFilesList;
		
	}

	public void setRegFilesList(List<Files> regFilesList) {
		this.regFilesList = regFilesList;
		
	}

	public boolean isErrInit() {
		return errInit;
		
	}

	public void setErrInit(boolean errInit) {
		this.errInit = errInit;
		
	}

	public String getErrTxt() {
		return errTxt;
	}

	public void setErrTxt(String errTxt) {
		this.errTxt = errTxt;
	}
	
	public Integer getVidDocVp() {
		return vidDocVp;
		
	}

	public void setVidDocVp(Integer vidDocVp) {
		this.vidDocVp = vidDocVp;
		
	}
   
	public LazyDataModelSQL2Array getObList() {
		return obList;
		
	}

	public void setObList(LazyDataModelSQL2Array obList) {
		this.obList = obList;
		
	}

	public LazyDataModelSQL2Array getRegsList() {
		return regsList;
		
	}

	public void setRegsList(LazyDataModelSQL2Array regsList) {
		this.regsList = regsList;
		
	}

	public SysAttrSpec getDmRegNom() {
		return dmRegNom;
	}

	public void setDmRegNom(SysAttrSpec dmRegNom) {
		this.dmRegNom = dmRegNom;
	}

	public SysAttrSpec getDmStatus() {
		return dmStatus;
	}

	public void setDmStatus(SysAttrSpec dmStatus) {
		this.dmStatus = dmStatus;
	}

	public SysAttrSpec getDmDateStatus() {
		return dmDateStatus;
	}

	public void setDmDateStatus(SysAttrSpec dmDateStatus) {
		this.dmDateStatus = dmDateStatus;
	}

	public SysAttrSpec getDmName() {
		return dmName;
	}

	public void setDmName(SysAttrSpec dmName) {
		this.dmName = dmName;
	}

	public SysAttrSpec getDmVid() {
		return dmVid;
	}

	public void setDmVid(SysAttrSpec dmVid) {
		this.dmVid = dmVid;
	}

//	public SysAttrSpec getDmFunkCat() {
//		return dmFunkCat;
//	}
//
//	public void setDmFunkCat(SysAttrSpec dmFunkCat) {
//		this.dmFunkCat = dmFunkCat;
//	}

	
	public SysAttrSpec getDmIdentif() {
		return dmIdentif;
	}

	public SysAttrSpec getDmVidSport() {
		return dmVidSport;
	}

	public void setDmVidSport(SysAttrSpec dmVidSport) {
		this.dmVidSport = dmVidSport;
	}

	public void setDmIdentif(SysAttrSpec dmIdentif) {
		this.dmIdentif = dmIdentif;
	}

	public SysAttrSpec getDmOpis() {
		return dmOpis;
	}

	public void setDmOpis(SysAttrSpec dmOpis) {
		this.dmOpis = dmOpis;
	}

	public SysAttrSpec getDmDopInfo() {
		return dmDopInfo;
	}

	public void setDmDopInfo(SysAttrSpec dmDopInfo) {
		this.dmDopInfo = dmDopInfo;
	}

	public SysAttrSpec getDmCountry() {
		return dmCountry;
	}

	public void setDmCountry(SysAttrSpec dmCountry) {
		this.dmCountry = dmCountry;
	}

	public SysAttrSpec getDmNasMesto() {
		return dmNasMesto;
	}

	public void setDmNasMesto(SysAttrSpec dmNasMesto) {
		this.dmNasMesto = dmNasMesto;
	}

	public SysAttrSpec getDmAddress() {
		return dmAddress;
	}

	public void setDmAddress(SysAttrSpec dmAddress) {
		this.dmAddress = dmAddress;
	}

	public SysAttrSpec getDmEMail() {
		return dmEMail;
	}

	public void setDmEMail(SysAttrSpec dmEMail) {
		this.dmEMail = dmEMail;
	}

	public SysAttrSpec getDmTel() {
		return dmTel;
	}

	public void setDmTel(SysAttrSpec dmTel) {
		this.dmTel = dmTel;
	}

	public SysAttrSpec getDmPostCode() {
		return dmPostCode;
	}

	public void setDmPostCode(SysAttrSpec dmPostCode) {
		this.dmPostCode = dmPostCode;
	}

	public String getHeaderTxt() {
		return headerTxt;
		
	}

	public void setHeaderTxt(String headerTxt) {
		this.headerTxt = headerTxt;
		
	}

	public boolean isEfrm() {
		return efrm;
		
	}

	public void setEfrm(boolean efrm) {
		this.efrm = efrm;
		
	}

	public int getTipZvl() {
		return tipZvl;
	}

	public void setTipZvl(int tipZvl) {
		this.tipZvl = tipZvl;
	}

	public Integer getIdSpObekt() {
		return idSpObekt;
	}

	public void setIdSpObekt(Integer idSpObekt) {
		this.idSpObekt = idSpObekt;
	}

	public String getRegNomSpObekt() {
		return regNomSpObekt;
	}

	public void setRegNomSpObekt(String regNomSpObekt) {
		this.regNomSpObekt = regNomSpObekt;
	}

	/**************************************************** FROM SSEV ****************************************************/	

	private Integer idSSev;
	private Integer vidDoc;
	private String regNom;
	private Date dataDoc;
	private String otnosno;
	private String egn;
	private String eik;
	private Referent ref = new Referent();
	private MMSVpisvane vpisvane = new MMSVpisvane();
	private boolean noVp = false;
	
	// Метода е за извикване след запис на обектите ако са извикани от "Нови заявления"!
	public boolean actionSaveDocFromSeos() {
		
		Doc newDoc = new Doc();	
		MMSVpisvaneDoc vpisvaneDoc = new MMSVpisvaneDoc();	
		
		//DateFormat form = new SimpleDateFormat("yyy-MM-dd HH:mm:ss.S");
		
		boolean saveNewVp = false;
		boolean isDelSpOb = false;      
		noVp = false;		
		 
		try {
			
			JPA.getUtil().runWithClose(() -> {
				
				List<MMSVpisvane> regList = new MMSVpisvaneDAO(getUserData()).findRegsListByIdAndType(this.spObekt.getCodeMainObject(), this.spObekt.getId()); 
				
				
				if (!regList.isEmpty()) {
					vpisvane = new MMSVpisvaneDAO(getUserData()).findById(regList.get(0).getId()); 
				
				} else {						
					noVp = true;					
				}
				 
			});	
			
			if (vidDoc.equals(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_SPORTEN_OBEKT)) {
				
				boolean existActVpis = false;
				
				if (vpisvane.getStatusVpisvane() != null && vpisvane.getStatusVpisvane().equals(DocuConstants.CODE_ZNACHENIE_STATUS_REG_VPISAN)) {

					existActVpis = true;
				
				} else if (vpisvane.getStatusVpisvane() == null && vpisvane.getStatusResultZaiavlenie() != null
						&& (vpisvane.getStatusResultZaiavlenie().equals(DocuConstants.CODE_ZNACHENIE_STATUS_ZAIAVLENIE_V_RAZGLEJDANE)
								|| vpisvane.getStatusResultZaiavlenie().equals(DocuConstants.CODE_ZNACHENIE_STATUS_ZAIAVLENIE_VPISAN))) {

					existActVpis = true;
				}
				
				if (noVp ||	(!noVp && !existActVpis) ) {
					if (noVp)  isDelSpOb = true;    // Ако не се запише първо вписване,със заявлението за вписване  формираният спортен обект ще се изтрива
					saveNewVp = true;
					noVp = false; // за да запише ново вписване, когато отговаря на условията
				
				} else {
				
					saveNewVp = false;
					JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString("beanMessages", "mmsSpObekt.noSaveZaiav"));	
					PrimeFaces.current().executeScript("scrollToErrors()");
					return  false;				
				}
			
			} else {   // Ако заявлението не е за вписване
				
				if (noVp) {
					
					//Ако няма нито едно вписване - съобщение, че няма към кое вписване да се направи заличаване или промяна на обстоятелствата
					if (vidDoc.equals(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_PROM_SPORTEN_OBEKT)) {
						JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString("beanMessages", "mmsSpObekt.noSaveZaiavPrObst"));						
					}
					
					if (vidDoc.equals(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_ZALICH_SPORTEN_OBEKT)) {
						JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString("beanMessages", "mmsSpObekt.noSaveZaiavZalich"));						
					}
					
					PrimeFaces.current().executeScript("scrollToErrors()");
					return false;
					
				} 
			}
			
			if (!noVp) {
			
				// настройка по вид документ и регистратура
				Object[] docVidSetting = new DocDAO(getUserData()).findDocSettings(getUserData(UserData.class).getRegistratura(), this.vidDoc, getSystemData());
				
				if (docVidSetting == null) {
					
					String noSett = getSystemData().decodeItem(DocuConstants.CODE_CLASSIF_DOC_VID, this.vidDoc, getCurrentLang(), new Date());								
					
					JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString("beanMessages", "compReg.noDocSettings", noSett));	
					PrimeFaces.current().executeScript("scrollToErrors()");
					if (isDelSpOb) {
						// Трябва да се изтрие спортният обект, към който няма формирано вписване със заявление за първо вписване
						if (this.spObekt != null && this.spObekt.getId() != null) {
						   JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, "Записаният спортен обект се изтрива!");
						   actionDeleteSpObekt ();
						}   
					}
					return false;
				
				} else { 
					     
					newDoc.setDocVid(this.vidDoc);
					newDoc.setRnDoc(this.regNom);	
					newDoc.setDocDate(this.dataDoc); 
				
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
						vpisvane.setDateDocZaiavlenie(this.dataDoc);
						vpisvane.setStatusResultZaiavlenie(DocuConstants.CODE_ZNACHENIE_STATUS_ZAIAVLENIE_V_RAZGLEJDANE);
						vpisvane.setDateStatusZaiavlenie(new Date());
						vpisvane.setIdObject(this.spObekt.getId()); // ИД на обекта
						vpisvane.setTypeObject(this.spObekt.getCodeMainObject()); //КОДА на обекта
						
						vpisvane.setNachinPoluchavane(this.spObekt.getNachinPoluch());
						vpisvane.setAddrMailPoluchavane(this.spObekt.getNachinPoluchText());
					
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
						
						new MMSVpisvaneDAO(getUserData()).updateStatusReg(DocuConstants.CODE_ZNACHENIE_STATUS_OBEKT_V_RAZGLEJDANE, new Date(), this.spObekt.getId(), DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_OBJECTS);			
						
					});					
					
				}
			}
			
			return true;
		
		} catch (DbErrorException e) {
			LOGGER.error("Грешка при зареждане настройки по вид документ!! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString("ui_beanMessages", ERRDATABASEMSG), e.getMessage());
			if (isDelSpOb) {
				// Трябва да се изтрие спортният обект, към който няма формирано вписване със заявление за първо вписване
				if (this.spObekt != null && this.spObekt.getId() != null) {
				  JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, "Записаният спортен обект се изтрива!");
				  actionDeleteSpObekt ();
				}  
				
			}
			return false;
		} catch (BaseException e) {
			LOGGER.error("Грешка при регистриране на вписване", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString("ui_beanMessages", ERRDATABASEMSG), e.getMessage());
			if (isDelSpOb) {
				// Трябва да се изтрие спортният обект, към който няма формирано вписване със заявление за първо вписване
				if (this.spObekt != null && this.spObekt.getId() != null) {
				   JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, "Записаният спортен обект се изтрива!");
				   actionDeleteSpObekt ();
				}   
			}
               
			return false;
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

	/**
	 * Проверка за заключен  спортен обект
	 * @param objType
	 * @param idObj
	 * @return
	 */
	private boolean checkForLock(Integer objType, Integer idObj) {
		boolean res = true;
		 this.messLock = null;
		LockObjectDAO daoL = new LockObjectDAO();
		if (objType == null)  objType = Integer.valueOf(DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_OBJECTS);
		try { 
			Object[] obj = daoL.check(ud.getUserId(), objType, idObj);
			if (obj != null) {
				 res = false;
				 this.messLock = getSystemData().decodeItem(DocuConstants.CODE_CLASSIF_ADMIN_STR, Integer.valueOf(obj[0].toString()), getUserData().getCurrentLang(), new Date())   
						       + " / " + DateUtils.printDate((Date)obj[1]);
				 this.messLock = "Спортният обект е заключен от:  " + this.messLock;
//				 JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_WARN,  this.messLock);
				 
			}
		} catch (DbErrorException e) {
			if (objType.intValue() == DocuConstants.CODE_ZNACHENIE_JOURNAL_EGOVMESSAGE) {
				LOGGER.error("Грешка при проверка за заключено СЕОС/ССЕВ съобщение! ", e);
				this.messLock = "Грешка при проверка за заключено СЕОС/ССЕВ съобщение - " +  e.getMessage();
		   } else {
				LOGGER.error("Грешка при проверка за заключен спортен обект! ", e);
				this.messLock = "Грешка при проверка за заключен спортен обект - " +  e.getMessage();
		   }	   
			 res = false;
	//		JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
		}
		return res;
	}
	
	/**
	 * Заключване на спортен обект като преди това отключва всички обекти, заключени от потребителя
	 * @param objType
	 * @param idObj
	 * @param unlockTip ако е NULL преди да заключи подадения прави отключване на всички за потребителя, а ако е конкретно число
	 *                  отключва само за подадения тип обкет
	 */
	public void lockObject(Integer objType, Integer idObj, Integer unlockTip) {	
		 this.messLock = null;
		LockObjectDAO daoL = new LockObjectDAO();	
		
		try { 
			 // Заключва само за типа спортен обект
			JPA.getUtil().runInTransaction(() -> 
				daoL.lock(ud.getUserId(), objType, idObj, unlockTip)   
			);
		} catch (BaseException e) {
			if (objType.intValue() == DocuConstants.CODE_ZNACHENIE_JOURNAL_EGOVMESSAGE) {
					LOGGER.error("Грешка при заключване на СЕОС/ССЕВ съобщение! ", e);
					this.messLock = "Грешка при заключване на СЕОС/ССЕВ съобщение - " +  e.getMessage();
			} else {
				LOGGER.error("Грешка при заключване на спортен обект! ", e);
				this.messLock = "Грешка при заключване на спортен обект - " +  e.getMessage();
			}
			
//			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
			 return;
		}			
	}

	
	/**
	 * при излизане от страницата - отключва  спортния обект  и  го освобождава за актуализация от друг потребител
	 */
	@PreDestroy
	public void unlockObject(){
		this.messLock = null;
        if (!ud.isReloadPage()) {
        	LockObjectDAO daoL = new LockObjectDAO();	
        	try { 
	        	
	        	JPA.getUtil().runInTransaction(() -> 
					daoL.unlock(ud.getUserId())
				);
        	} catch (BaseException e) {
    			LOGGER.error("Грешка при отключване на  заключени обекти на страница за спортен обект! ", e);
    			 this.messLock = "Грешка при отключване на  заключени обекти на страница за спортен обект! - " +  e.getMessage();
   // 			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
    		}
        	ud.setPreviousPage(null);
        	
        }          
        ud.setReloadPage(false);
	}
	
	//LM
	public void actionPrint(int envNotice) {
		
//		if(!validatePrint()) {
//			scrollToMessages();
//			return;
//		}
		
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

			BaseExport exp= new BaseExport();
			String rPlik = "";
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

//		clearPostCover();
	}
	
	public Document fillDocShabl243 (com.aspose.words.Document pattern) throws DbErrorException  {
		
		try{
			
			// 1. Данни на Получател 
			
//			if (null != this.getCorespName() && pattern.getRange().getBookmarks().get("poluchatel") !=null){// Name
//				pattern.getRange().getBookmarks().get("poluchatel").setText(this.getCorespName());
//			}	
			
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
			
//	            if (null!= this.getSenderName() && pattern.getRange().getBookmarks().get("podatel") !=null){
//					pattern.getRange().getBookmarks().get("podatel").setText(this.getSenderName());
//				}
//
//	            if (null!=this.getSenderAddress() && pattern.getRange().getBookmarks().get("adresPod") !=null){
//	            	String sendadr=this.getSenderAddress();
//	            	if(null != this.getSenderTel() && !this.getSenderTel().trim().equals("")){
//	    				if(null!=this.getAdr() && !this.getAdr().getAddrCountry().equals(this.getCountryBg())) {
//	    					sendadr+=" phone "+ this.getSenderTel();
//	    				}else{
//	    					sendadr+=" тел. "+ this.getSenderTel();
//	    				}
//	    			}
//	            	
//					pattern.getRange().getBookmarks().get("adresPod").setText(sendadr);
//				}
//	            pattern.getRange().getBookmarks().get("regNom").setText(regNom);
//	            System.out.println();
	            
	            
	            
	            
				//NM
			/*
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
//					 BarcodeQRCode qrCode = new BarcodeQRCode(pkAdressat,1,1,null);
//					Image codeQrImage = qrCode.createAwtImage(Color.BLACK, Color.WHITE);
//					BufferedImage bufferedImage = new BufferedImage(codeQrImage.getWidth(null), codeQrImage.getHeight(null),  BufferedImage.TYPE_INT_RGB);
//			 
//					 DocumentBuilder builder = new DocumentBuilder(pattern);
//					 builder.moveToBookmark("barCod",true,false);
//					 Graphics2D bGr = bufferedImage.createGraphics();
//					 bGr.drawImage(codeQrImage, 0, 0, null);
//					 bGr.dispose();
//					 builder.insertImage(bufferedImage,100, 100); 
				}
				*/			
			
			 	
//		} catch (ObjectNotFoundException e) {
//	 		LOGGER.error(e.getMessage(), e);
//	 		throw new DbErrorException(e.getMessage());
		
		} catch (Exception e) {
			LOGGER.error(e.getMessage(),e);
			throw new DbErrorException(e.getMessage());
		
		} 
		
		
		return pattern;
		
	}

	
	public String CorrDataEnd() {
		String retDat="";
		
		// Corespondent
		
//		if(null != this.getCorespAddress() && !this.getCorespAddress().trim().equals(""))
//			retDat+=this.getCorespAddress();
//		
//		if(null != this.getCorespTel() && !this.getCorespTel().trim().equals("")){
//			if(null!=this.getAdr() && !this.getAdr().getAddrCountry().equals(this.getCountryBg())) {
//				retDat+=" phone "+ this.getCorespTel();
//			}else{
//				retDat+=" тел. "+ this.getCorespTel();
//			}
//		}

//		if(null != this.getCorespObl() && !this.getCorespObl().trim().equals("")) {
//			if (! this.getCorespObl().trim().contains("обл")) {
//				retDat+="\n"+"обл. "+this.getCorespObl().trim();
//			}else {
//				retDat+="\n"+this.getCorespObl().trim();
//			}
//		}
	
//		if(null != this.getCorespPostCode() && !this.getCorespPostCode().trim().equals(""))
//			retDat+="\n"+this.getCorespPostCode();
//		
//		if(null != this.getCorespNM() && !this.getCorespNM().trim().equals(""))
//			retDat+=" "+this.getCorespNM();
//
//		if(null!=this.getAdr() && !this.getAdr().getAddrCountry().equals(this.getCountryBg()) && null != this.getCorespCountry() && !this.getCorespCountry().trim().equals(""))
//			retDat+="\n"+this.getCorespCountry();
//			
//						
//		retDat=infoAdres;
		return retDat;
	}

	public String SenderDataEnd () {
		String retDat="";
		// Sender
		
//		if(null != this.getSenderAddress() && !this.getSenderAddress().trim().equals(""))
//			retDat+=this.getSenderAddress();
//		
//		if(null != this.getSenderTel() && !this.getSenderTel().trim().equals("")){
//			if(null!=this.getAdr() && ! this.getAdr().getAddrCountry().equals(this.getCountryBg())){
//				retDat+=" phone "+ this.getSenderTel();
//			}else{
//				retDat+=" тел. "+ this.getSenderTel();
//			}
//		}

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
	
		/* 
		if(null != this.getSenderNM()) {
			
			}if(null != this.getCorespNM()&& !this.getCorespNM().trim().equals("")) {
				retDat+=" "+this.getSenderNM();
			}
		if(null!=this.getAdr() && !this.getAdr().getAddrCountry().equals(this.getCountryBg()) && null != this.getSenderCountry() && !this.getSenderCountry().trim().equals(""))
			retDat+="\n"+this.getSenderCountry();
		*/
		
		return retDat;
		
	}
	
	public void prepareCorespData() {
		System.out.println();
//		try {
			
			// Corespondents
//			if(null != getRef()) {
//				if(null != this.getRef().getRefName() && !this.getRef().getRefName().trim().equals("")){// Name
//					this.setCorespName(this.getRef().getRefName().trim()); 
//				}
//				if(null != this.getRef().getContactPhone() && !this.getRef().getContactPhone().trim().equals("")){//Tel
//					this.setCorespTel(this.getRef().getContactPhone().trim()); 
//				}
//			}
			
			
			
//			if(null != this.getAdr()) {
//				if(null != this.getAdr().getAddrText() && !this.getAdr().getAddrText().trim().equals("")){// Address
//					this.setCorespAddress(this.getAdr().getAddrText().trim());	
//				}else if (null != this.getAdr().getPostBox() && !this.getAdr().getPostBox().trim().equals("")) {
//					this.setCorespAddress("Пощенска кутия "+this.getAdr().getPostBox().trim());	
//				}
//	
//				if(null != this.getAdr().getPostCode() && !this.getAdr().getPostCode().trim().equals("")){//PostCode - BG
//					this.setCorespPostCode(this.getAdr().getPostCode().trim()); 
//				} 
//				
//				if(null != this.getAdr().getEkatte() && null != this.getRef().getDateReg()){
//					String obstObl = getSystemData().decodeItemLite(CODE_CLASSIF_EKATTE, this.getAdr().getEkatte(), CODE_DEFAULT_LANG, this.getRef().getDateReg(), false).getDopInfo();// Obst and Obl
//					if (null!=obstObl) { 
//						String[] deco = obstObl.split(",");
//						if (deco.length==2 && null!=deco[1]) {
//							this.setCorespObl(deco[1].trim());// Oblast
//						}
//					}
//					
//					//NM
//					this.setCorespNM(getSystemData().decodeItem(CODE_CLASSIF_EKATTE, this.getAdr().getEkatte(), CODE_DEFAULT_LANG, this.getRef().getDateReg()));
//	
//				}

//				if(null != getAdr().getAddrCountry()){//Country
//					this.setCorespCountry(getSystemData().decodeItem(DocuConstants.CODE_CLASSIF_COUNTRIES, this.getAdr().getAddrCountry(), CODE_DEFAULT_LANG, this.getRef().getDateReg())); 
//				}
//			}
			
			
			
//		} catch (BaseException e) {
//			LOGGER.error(getMessageResourceString(beanMessages, "Грешка при вземане на данни за кореспондент!"), e);
//			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
//		}	
		
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

	public List<Integer> getSelectedVidSport() {
		return selectedVidSport;
		
	}

	public void setSelectedVidSport(List<Integer> selectedVidSport) {
		this.selectedVidSport = selectedVidSport;
		
	}

	public String getSelectedVidSportTxt() {
		return selectedVidSportTxt;
		
	}

	public void setSelectedVidSportTxt(String selectedVidSportTxt) {
		this.selectedVidSportTxt = selectedVidSportTxt;
		
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

	public EgovMessages getEgovMess() {
		return egovMess;
		
	}

	public void setEgovMess(EgovMessages egovMess) {
		this.egovMess = egovMess;
		
	}

	public List<EgovMessagesFiles> getEgovFilesList() {
		return egovFilesList;
		
	}

	public void setEgovFilesList(List<EgovMessagesFiles> egovFilesList) {
		this.egovFilesList = egovFilesList;
		
	}

	public List<SelectItem> getMsgStatusList() {
		return msgStatusList;
		
	}

	public void setMsgStatusList(List<SelectItem> msgStatusList) {
		this.msgStatusList = msgStatusList;
		
	}

	public String getReasonOtkaz() {
		return reasonOtkaz;
		
	}

	public void setReasonOtkaz(String reasonOtkaz) {
		this.reasonOtkaz = reasonOtkaz;
		
	}

	public static Properties getProps() {
		return props;
		
	}

	public static void setProps(Properties props) {
		MMSSportenObektEdit.props = props;
		
	}

	public Referent getReferent_corresp() {
		return referent_corresp;
		
	}

	public void setReferent_coresp(Referent referent_corresp) {
		this.referent_corresp = referent_corresp;
		
	}

	public String getTxtCorresp() {
		return txtCorresp;
		
	}

	public void setTxtCorresp(String txtCorresp) {
		this.txtCorresp = txtCorresp;
		
	}


	public Date getDateRNV() {
		return dateRNV;
	}


	public void setDateRNV(Date dateRNV) {
		this.dateRNV = dateRNV;
	}

	public String getVidDocVpText() {
		return vidDocVpText;
		
	}

	public void setVidDocVpText(String vidDocVpText) {
		this.vidDocVpText = vidDocVpText;
		
	}

	public EgovMessagesCoresp getEmcoresp() {
		return emcoresp;
	}

	public void setEmcoresp(EgovMessagesCoresp emcoresp) {
		this.emcoresp = emcoresp;
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

	public void setReferent_corresp(Referent referent_corresp) {
		this.referent_corresp = referent_corresp;
	}

	public boolean isCancelZajavl() {
		return cancelZajavl;
		
	}

	public void setCancelZajavl(boolean cancelZajavl) {
		this.cancelZajavl = cancelZajavl;
		
	}


	public ArrayList<Files> getUploadFilesList() {
		return uploadFilesList;
	}


	public void setUploadFilesList(ArrayList<Files> uploadFilesList) {
		this.uploadFilesList = uploadFilesList;
	}


	public boolean isInpPdf() {
		return inpPdf;
	}


	public void setInpPdf(boolean inpPdf) {
		this.inpPdf = inpPdf;
	}


	public boolean isCcevNV() {
		return ccevNV;
	}


	public void setCcevNV(boolean ccevNV) {
		this.ccevNV = ccevNV;
	}

	
	/**************************************************** END FROM SSEV ****************************************************/  
	
}
