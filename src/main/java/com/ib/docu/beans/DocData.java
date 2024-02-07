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
import java.io.Serializable;
import java.net.URLEncoder;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.faces.view.facelets.FaceletContext;
import javax.inject.Named;
import javax.mail.MessagingException;
import javax.mail.internet.MimeUtility;
import javax.persistence.Query;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jsoup.Jsoup;
import org.omnifaces.cdi.ViewScoped;
import org.primefaces.PrimeFaces;
import org.primefaces.event.TabChangeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aspose.words.Document;
import com.aspose.words.DocumentBuilder;
import com.aspose.words.License;
import com.aspose.words.SaveFormat;
import com.ib.docu.components.CompAccess;
import com.ib.docu.db.dao.DocDAO;
import com.ib.docu.db.dao.DocDocDAO;
import com.ib.docu.db.dao.EgovMessagesDAO;
import com.ib.docu.db.dao.LockObjectDAO;
import com.ib.docu.db.dao.RegistraturaDAO;
import com.ib.docu.db.dao.TaskDAO;
import com.ib.docu.db.dto.Delo;
import com.ib.docu.db.dto.DeloDoc;
import com.ib.docu.db.dto.Doc;
import com.ib.docu.db.dto.DocAccess;
import com.ib.docu.db.dto.DocDopdata;
import com.ib.docu.db.dto.DocReferent;
import com.ib.docu.db.dto.EgovMessages;
import com.ib.docu.db.dto.EgovMessagesFiles;
import com.ib.docu.db.dto.Referent;
import com.ib.docu.db.dto.ReferentAddress;
import com.ib.docu.db.dto.Registratura;
import com.ib.docu.db.dto.Task;
import com.ib.docu.experimental.Notification;
import com.ib.docu.export.BaseExport;
import com.ib.docu.search.DeloSearch;
import com.ib.docu.search.DocSearch;
import com.ib.docu.search.TaskSearch;
import com.ib.docu.system.DocuClassifAdapter;
import com.ib.docu.system.DocuConstants;
import com.ib.docu.system.SystemData;
import com.ib.docu.system.UserData;
import com.ib.indexui.navigation.Navigation;
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
import com.ib.system.exceptions.ObjectInUseException;
import com.ib.system.exceptions.ObjectNotFoundException;
import com.ib.system.exceptions.UnexpectedResultException;
import com.ib.system.mail.Mailer;
import com.ib.system.mail.MyMessage;
import com.ib.system.utils.DateUtils;
import com.ib.system.utils.SearchUtils;
import com.ib.system.utils.SignatureUtils;
import com.ib.system.utils.VerifySignature;
import com.ib.system.utils.X;
import com.lowagie.text.pdf.Barcode128;

/**
 * Актуализация на документ
 *
 * @author rosi
 */
@Named
@ViewScoped
public class DocData   extends IndexUIbean  implements Serializable {

	/**  */
	private static final long serialVersionUID = 8191901936895268740L;

	private static final Logger LOGGER = LoggerFactory.getLogger(DocData.class);
	public  static final String DOCFORMTABS      = "docForm:tabsDoc";
	public  static final String	OBJECTINUSE		 = "general.objectInUse";
	public  static final String	MSGVALIDDATES	 = "docu.validDates";
	public  static final  String TXTREJECTED 	 = "docu.txtRejected";
	public  static final  String TXTREJECTEDID   = "docForm:tabsDoc:txtReject";
	
	/**
	 * за актуализация;
	 */
	public  static final int UPDATE_DOC = 0; 	
	/**
	 * за рег. като офицален;
	 */
	public  static final int REG_WRK_DOC = 1; 
	/**
	 * за приемане от друга регистратура
	 */
	public  static final int REG_OTHER_R = 2; 	
	/**
	 * регистриране от е-мейл
	 */
	public  static final int REG_FROM_MAIL = 3; 
	/**
	 * регистриране от СЕОС или ССЕВ
	 */
	public static final int REG_FROM_EGOV= 4; 
	public static final String S_SEOS = "S_SEOS";

	/**
	 * Статуси на съобщения - СЕОС, 
	 * @author rosi
	 *
	 */
	private enum EgovStatusType {
		DS_REJECTED,   
	    DS_REGISTERED,
	    DS_WAIT_REGISTRATION
	    //, DS_STOPPED,DS_CLOSED,DS_NOT_FOUND, DS_ALREADY_RECEIVED
	}
	
	private Doc document;
	private transient DocDAO	dao;
	private SystemData sd;	
	private UserData ud;
	/**
	 * true -  автоматично генериране на номер
	 */
	private boolean avtomNo = true;
	/**
	 * true - да се забрани достъпа до бутона за автоматичното генериране на номер
	 */
	private boolean avtomNoDisabled = false;          
	
	/**
	 *  свободен достъп - false; ограничен достъп = true
	 */
	private boolean limitedAccessCh = false;  	
	/**
	 * true - да се създаде преписка
	 */
	private boolean createPrep = false;     
	private boolean createPrepOld = false; // използвам го, ако се натисне бутон "нов документ със запазване на данни"  
	/**
	 * true - обработен; false - необработен
	 */
	private boolean processedCh = false;   		 
		
	private Date decodeDate = new Date();
		
	private List<SelectItem>	 docTypeList;	
	private List<SystemClassif>  scTopicList; // заради autocomplete
	
	private List<SystemClassif> classifProceduri; 
	
	private transient Map<Integer, Object> specificsProc;  
	private boolean enableProc;
	private boolean notFinishedProc;

	/**
	 * за списъка с допустими регистри, в зависимост от типа документ
	 */
	
	private transient Map<Integer, Object> specificsRegister;  
	/**
	 *  дали списъкът с регистри да се филтрира в зависимост от правата на потребителя
	 */
	private boolean regUserFiltered;  				 
	/**
	 * списък файлове към документа
	 */
	private List<Files> filesList;
	
	/**
	 * шаблони към вид документ
	 */
	private List<Files> templatesList;
	/**
	 * id от таблицата с настройки по вид документ 
	 */
	private Integer docSettingId;
	
	/**
	 * характер на спец. документ, ако има такъв посочен в описание на характеристиките на документа 
	 */
	private Integer docSettingHar;
	/**
	 * Таб - участници, само, ако е посочено в характеристиките на вид документ
	 */
	private String membersTab;
	/**
	 * ще бъде true само, ако при актуализация на док. е бил сменен вида на документа и предипния вид е имал таб "Участници", в който вече е било записано нещо!
	 * това е нужно, за да може да бъдат изтрити на записа на документа 
	 */
	private boolean membersTabForDel; 
	


	/**
	 * брой официални файлове за изпращане
	 */
	private int countOfficalFiles; 
	private String  txtCorresp;
	/**
	 * преписка, в която се влага документа при ръчно писане на рег. номера - за нов документ
	 */
	private DeloDoc deloDocPrep; 					
	/**
	 * избор на преписка при ръчно въвеждане на номер
	 */

	private transient Object[] selectedDelo;					
	
	/**
	 * Задачи към документ
	 */
	private String rnFullDoc; 
	private LazyDataModelSQL2Array tasksList;
	private Integer idTask;
	private String srokPattern;
	
	/**
	 * "За запознаване" - индивидуални задачи от тип резолюция
	 */
	private List<SystemClassif> rezolExecClassif;
	
	private transient Map<Integer, Object> specificsAdm;	
	private Task rezolTask;
	
	private transient Object[] codeExtCheck;
	
	/**
	 * Указания - "за запознаване" - изричен достъп до документ + указания за нотификацията
	 */
	private String noteAccess;
	
	/**
	 * За работните документи - Готов за регистриране като официален
	 */
	private boolean readyForOfficial= false;   
	private List<SelectItem>	dopRegistraturiList;	

	// Ако за вида док. има посочена регистртура къде да бъде рег. официалния - тя се зарежда в - forRegId
	// Ако за вида док., няма изрично посочена регистртура - зарежда се текущата.
	// Ако потребителя има допълнителни регистртури - излиза списък за избор
	// За регистрация - остава последното избрано!
	
	/**
	 * рег.ном./дата на протокол за унищожаване на документ
	 */
	private String rnFullProtocol;
	
	
	/**
	 * рег.ном./дата на свързания с текущия документ - работен/офицален/от другата регистратура
	 */
	private String rnFullDocOther;
	private boolean fromOtherReg; // ако е от друга регистратура - да скрия линка за разглеждане
	

	private String rnFullDocEdit; 


	/**
	 * Приемане на документи от друга регистратура - id на движението, с което е изпратен документа
	 */
	private Integer sourceRegDvijId;
	
	private transient Object[] dvijData;
	private Properties propMail;
    private Long messUID;
    private String selectMailBox;
    private String messFromRef;
    private String messSubject;

	/**
	 * за какво действие е извикан екрана
	 */
	private int flagFW;
	private int isView;
		
	private int viewBtnProcessed;  // Бутонът "Обработен" -  за входящ документ да се вижда само ако служителят, който работи (userAccess) има роля деловодител, а за работен документ - деловодител или автор на документа. За собствен документ бутонът не се вижда.
	
	// да проверя, ако отворя за редакция документ, който е в конкретен регистър, а нямам право да въвеждам в този регистър
    //     (има ограничение в правата) - какво се случва!!!! 
	
	/**
	 * Свързан с документ по техен номер
	 */

	private transient List<Object[]> selectedDocsTn; //избрани документи
	private int searchFlagTn=0; // за да предоврати минаването през searchTehNomer два пъти, ако след въвеждане на номер на док. веднага се натисне лупата
	private int tnRez = 0;      // бр. резултати при търсенен по техен номер
	
	/**
	 * настройки на системата
	 */
	private boolean nastrWithEkz;
	private boolean scanModuleExist;
	
	/**
	 * Отказ от регистрация - причина за отказ
	 */
	private String textReject;
	

	/**
	 * true - да се изпрати по компетентност
	 */
	private boolean competence;     	
	
	/**
	 * съобщение от СЕОС
	 */
	private EgovMessages egovMess;
	
	/**
	 * справка за достъп
	 */
	private LazyDataModelSQL2Array docAccessList;
	
	/**
	 * да се вижда ли поле "Кореспондент" в собствени документи
	 */
	private boolean showCoresp;
	
	/**
	 * Допуска се въвеждане на съгласувал и подписал в работен документ
	 */
	private boolean editReferentsWRK;
	
	/**
	 * Допуска се въвеждане на начин на подписване за собсвен и  работен документ
	 */
	private Integer showSignMethod;
	
	/**
	 * рег. номер и дата на документа за подпис
	 */
	private String rnFullDocSign;
	
	/**
	 * В зависимост от  типа на кореспондента и правото на потребителя дали да вижда лични данни или не  - какво да се вижда в полето адрес
	 *  
	 */
	private String dopInfoAdres;
	
	/** */
	@PostConstruct
	void initData() {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("PostConstruct!" );
		}
		try {
			
			long t = System.currentTimeMillis(); //test
			
			
			ud = getUserData(UserData.class);					
			sd = (SystemData) getSystemData();
			dao = new DocDAO(getUserData());
			
			setCountryBulg ();     // Код EKATTE и име на държава България
			
			// общосистемни настройки
			allSystemSettings();
			
			// филтрира се в зависимост от правата на потребителя - само зa нов документ
			this.docTypeList = createItemsList(true,  DocuConstants.CODE_CLASSIF_DOC_TYPE, this.decodeDate, true);
			// деловодителите да могат да въвеждат работни документи само в тяхната основна регистртура!
			Integer mainRegId = (Integer)sd.getItemSpecific(DocuConstants.CODE_CLASSIF_ADMIN_STR, ud.getUserId(), DocuConstants.CODE_DEFAULT_LANG, new Date(), DocuClassifAdapter.ADM_STRUCT_INDEX_REGISTRATURA);
			if(!ud.getRegistratura().equals(mainRegId)) {
				this.docTypeList  = this.docTypeList.stream()
				.filter(item -> (Integer)item.getValue() != DocuConstants.CODE_ZNACHENIE_DOC_TYPE_WRK)
				.collect(Collectors.toList());
			}
			
			String paramEgov = JSFUtils.getRequestParameter("idEgov");
			String param2 = JSFUtils.getRequestParameter("fw");	// flag от къде идва - 0 -за актуализация; 1 - за рег. като офицален; 2- от друга рег; 3- e-mail
			flagFW = UPDATE_DOC; // актуализация на док.
			if(!SearchUtils.isEmpty(param2)) {
				flagFW = Integer.valueOf(param2);
			}else if(!SearchUtils.isEmpty(paramEgov)){
				flagFW = REG_FROM_EGOV;
			}
			
	
			//проверка дали потребителя има права за въвеждане на необходимите типове документи - може да се получи, ако правата му не са зададени правилно! 
			boolean flagA = (flagFW == REG_WRK_DOC || flagFW == REG_OTHER_R ) && checkAccessTypeDoc( DocuConstants.CODE_ZNACHENIE_DOC_TYPE_OWN);
			if(flagFW == REG_FROM_MAIL  && checkAccessTypeDoc( DocuConstants.CODE_ZNACHENIE_DOC_TYPE_IN)) {
				// рег. от е-маил
				initDocMail();
			} else if (flagFW == UPDATE_DOC || flagA){
				initDoc();	
			} else if (flagFW == REG_FROM_EGOV  && checkAccessTypeDoc( DocuConstants.CODE_ZNACHENIE_DOC_TYPE_IN)) {
				initDocEgovMsg(Integer.valueOf(paramEgov));	
			} 

			if(document != null) {
				docRegSettings(); // настройки на регистратура
			}
			
			selectedDelo = null;		
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("ALL Doc inited: {}ms.", System.currentTimeMillis() - t);
			}
		} catch (DbErrorException | UnexpectedResultException e) {
			LOGGER.error("Грешка при зареждане на данни за документ! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
		} 
	}
	
	private boolean checkAccessTypeDoc( Integer typeDoc) {
		boolean flagA = false;  
		if(docTypeList != null && docTypeList.size()<3) {
			for  (SelectItem si: docTypeList) {
				if(Objects.equals(si.getValue(), typeDoc)){
					flagA = true;
				}
			}
		} else {
			flagA = true;
		}
		return flagA;
	}
	
	
	
	
	/**
	 * Зарежда данни за документ, ако е нов документ, за актуализация, за приеман от др. регистратура, рег. на офицален от работен 
	 * @return
	 */
	private void initDoc() {
	
		propMail = null;
		String param = JSFUtils.getRequestParameter("idObj");
		if ( SearchUtils.isEmpty(param)){
			actionNewDocument(false, false);	
		} else {
			initDocObj(param);
		}		
		
	}
	
	/**
	 * зарежда данни за обект документ, подаден е idObj
	 * @param paramIdObj
	 */
	private void initDocObj(String paramIdObj) {
		boolean fLockOk = true;
		
		FaceletContext faceletContext = (FaceletContext) FacesContext.getCurrentInstance().getAttributes().get(FaceletContext.FACELET_CONTEXT_KEY);
		String param3 = (String) faceletContext.getAttribute("isView"); // 0 - актуализациял 1 - разглеждане
		Integer	docId = Integer.valueOf(paramIdObj);				
		isView = !SearchUtils.isEmpty(param3) ? Integer.valueOf(param3) : 0;
		
		if(isView == 0) { 
			Integer idLock = docId;
			Integer codeObj = DocuConstants.CODE_ZNACHENIE_JOURNAL_DOC;
			
			if(flagFW == REG_OTHER_R ) {
				param3 = JSFUtils.getRequestParameter("idDvig");	
				this.sourceRegDvijId = Integer.valueOf(param3);	//id на движението,за да се коригират данните в него! 
				idLock = this.sourceRegDvijId;
				codeObj = DocuConstants.CODE_ZNACHENIE_JOURNAL_DOC_DVIJ;
			}
			// проверка за заключен док. + закл. на текущия обект.
			fLockOk = checkAndLockDoc(idLock, codeObj);
		}
		
		//За сега няма да викам различен метода за зареждане на док при разгледжане. 
		if (fLockOk) {
			if(flagFW == UPDATE_DOC) {
				fLockOk = loadDocumentEdit(docId); // зарежда данните за документа за актуализация или разглеждане
			}else if(flagFW == REG_WRK_DOC) {
				loadDocFromOtherDoc(docId, REG_WRK_DOC); // зарежда данните на работен документ за регистриране като официален
			}else if(flagFW == REG_OTHER_R ) {
				loadDocFromOtherDoc(docId, REG_OTHER_R ); // зарежда данните на  документ зa приемане от друга регистратура
			}
			if(fLockOk) {
				// спецификите за списъка с регистри - зависимост от типа документ
				specTypeDocProc(document.getDocType());
				
			}
			//  журналира отварянето на обекта ///// Ще го правим ли????
		}
		
   		if(isView == 1 && fLockOk) {
   			viewMode();
   			// **********************************
   			// Инициализация на данни за печат на етикети/оратни разписки
   			    clearPostCover();
   			    setObjParams();
   			    if (this.prInpPar)  {       // view на документ от bean за обекти
   			    	 setSenderForMS();
   			    	 prepareCorespData();
   			    	 prepareSenderData();
   			    }
   			
   			//***********************************
   			
   		}
	}
	
	/**
	 * Регистриране на док. от е-маил
	 * @return
	 */
	private boolean  initDocMail() {
		boolean fLockOk = true;
		MyMessage mailMsg = (MyMessage)JSFUtils.getFlashScopeValue("mailMessage");
		propMail = (Properties)JSFUtils.getFlashScopeValue("prop");
		selectMailBox = (String)JSFUtils.getFlashScopeValue("selectMailBox");
		if(mailMsg != null) {
			messUID =  Long.valueOf(mailMsg.getMessUID());			
			// проверка за заключване + закл. на текущия обект.
			fLockOk = checkAndLockDoc(messUID.intValue(), DocuConstants.CODE_ZNACHENIE_JOURNAL_MAILBOX);
			if(fLockOk) {			
				actionNewDocument(false, false);		
				document.setDocType(DocuConstants.CODE_ZNACHENIE_DOC_TYPE_IN);
				// спецификите за списъка с регистри - зависимост от типа документ
				specTypeDocProc(document.getDocType());
				
				document.setReceiveDate(mailMsg.getReceivedDate());
				document.setReceiveMethod(DocuConstants.CODE_ZNACHENIE_PREDAVANE_EMAIL);
				messFromRef = mailMsg.getFrom() ;
				document.setReceivedBy(messFromRef);// + " \n" + mailMsg.getFromName());			
				messSubject = mailMsg.getSubject(); // трябва ми за отговора..
						
				String noHTMLString="";
				String body = mailMsg.getBody();
				if(body != null){
					noHTMLString= Jsoup.parse(body).text();
				}
				document.setOtnosno(messSubject + " \n\n" + noHTMLString);
				
	  		    // файлове от мейла... 
				filesList = new ArrayList<>();
				if(mailMsg.getAttachements() != null && !mailMsg.getAttachements().isEmpty()) {
					Iterator<String> it = mailMsg.getAttachements().keySet().iterator();
					Files newFile;
					while (it.hasNext()) {
						String keyVar = it.next();
						newFile =  loadFilesFromMsg(keyVar, "application/x-download", mailMsg.getAttachements().get(keyVar));
						filesList.add(newFile);	
					}
				}		
				
				document.setFromMail(messUID.toString()+""+selectMailBox.trim().toUpperCase()); //необходимо е, защото от работния плот се прави проверка дали вече този е-мейл не региструран
			}
			
		}
		return fLockOk;
	}
	 

	
	
	/**
	 * Зарежда в новия документ файловете от емайл, сеос, ссев и прави проверка дали са подписани
	 * @param fileName
	 * @param fileCType
	 * @param content
	 * @return
	 */
	private Files loadFilesFromMsg(String fileName, String fileCType, byte[] content) {
		
		Files newFile = new Files();
		newFile.setFilename(fileName);
		newFile.setContentType(fileCType);
		newFile.setContent(content);
		newFile.setOfficial(DocuConstants.CODE_ZNACHENIE_DA);
								
		boolean bb = verifySignFile(fileName, content);
		
		if(bb) {
			newFile.setSigned(Constants.CODE_ZNACHENIE_DA);
		} else {
			newFile.setSigned(Constants.CODE_ZNACHENIE_NE);
		}
	
		return newFile;
	}
	
	
    
	/**
	 * проверка за ел. подпис за файл, който идва от емйл, сеос, ссев
	 * @param filename
	 * @param cont
	 * @throws IOException 
	 */
	private boolean verifySignFile(String filename, byte[] content )  {
		
		boolean bb = false;
		try {
			
			Set<X509Certificate> trust = getSystemData().getTrustedCerts();
			
			if(filename.endsWith(".docx")) {			
				List<VerifySignature> rez = new SignatureUtils().verifyWordExcelSignatures(content, trust);
				if(rez!=null && !rez.isEmpty()) {
					bb = true;
				} 
			} else if(filename.endsWith(".pdf")) {			
				List<VerifySignature> rez = new SignatureUtils().verifyPDFSignatures(content, trust);
				if(rez!=null && !rez.isEmpty()) {
					bb = true;
				}
			}
			
		} catch (UnexpectedResultException e) {
			LOGGER.error(e.getMessage(), e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,  "Неочаквана Грешка при извличане данни за сертификатите на файл!", e.getMessage());
		}
		
		return bb;
	}
	
	
	
	/** 
	 * регистриране на документи от СЕОС или ССЕВ
	 * @param idEgov - id на съобщение, изпратено през СЕОС или ССЕВ
	 * @return
	 */
	private boolean initDocEgovMsg(Integer idEgov) {
		boolean fLockOk = checkAndLockDoc(idEgov, DocuConstants.CODE_ZNACHENIE_JOURNAL_EGOVMESSAGE);	
		// проверка за заключване + закл. на текущия обект - съобщението от СЕОС
		if(fLockOk) {		
			try {
				actionNewDocument(false, false);
				
				JPA.getUtil().runWithClose(() -> {
					EgovMessagesDAO daoEgov = new EgovMessagesDAO(getUserData());
					egovMess = daoEgov.findById(idEgov); //TODO xml ??? da se mahne!!
					// още една проверка за статуса на съобщениеото  - трябва да е "Чака регистрация" - само тогава се допуска да продължи отварянето на екрана за регистрация на документ
					if (Objects.equals(egovMess.getMsgStatus(), EgovStatusType.DS_WAIT_REGISTRATION.toString())) {
						//файлове от съобщението... 
						loadFilesEgovMess(idEgov, daoEgov);
					}else {
						String txt = "Статус: " +egovMess.getMsgStatus();
						if(egovMess.getMsgRn() != null && egovMess.getMsgRnDate() != null) {
							txt = "Регистрирано под номер: "+egovMess.getMsgRn() + "/" + DateUtils.printDate(egovMess.getMsgRnDate());
						}						
						JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_WARN,getMessageResourceString(LABELS, "docu.egovMsgErrSt"),txt);
						egovMess = null; // това може да се случи, ако деловодител 1 си е отворил списъкът със съобщения и дълго нищо не прав, а междувременно деловодител 2 е обработил съобщението и едва тогава делов. 1 решава да рег. същото съобшение...
					}
				});
				
				if(egovMess != null) {
					initDocEgovMsg1();
				} else {
					document = null;
				}
			} catch (BaseException e) {
				LOGGER.error("Грешка при зареждане на съобщение от СЕОС/ССЕВ! ", e);
				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
			}	
		}
		return fLockOk;
	}

	/**
	 * зарежда данните от egоv съобщението в обект документ 
	 * @throws DbErrorException
	 */
	private void initDocEgovMsg1() throws DbErrorException{
		document.setDocType(DocuConstants.CODE_ZNACHENIE_DOC_TYPE_IN);
		// спецификите за списъка с регистри - зависимост от типа документ  и списък процедури
		specTypeDocProc(document.getDocType());
		
		document.setGuid(egovMess.getDocGuid().toUpperCase());
		document.setReceiveDate(egovMess.getMsgDate());
		if(Objects.equals(S_SEOS, egovMess.getSource())){
			document.setReceiveMethod(DocuConstants.CODE_ZNACHENIE_PREDAVANE_SEOS); 
		}else {
			document.setReceiveMethod(DocuConstants.CODE_ZNACHENIE_PREDAVANE_SSEV); 
		}
		if(!SearchUtils.isEmpty(egovMess.getDocSubject())) {
			document.setOtnosno(egovMess.getDocSubject().replaceAll("\r", ""));
		}
		
		// зарежда вид док., ако го намери и доп. информация 
		String docInfo = docEgovMsgVD();  
		document.setDocInfo(docInfo);
		//търси кореспондента по ЕИК
		String idvaOt = docEgovMsgCoresp(); 
		if(!SearchUtils.isEmpty(idvaOt)) {
			document.setReceivedBy(idvaOt);
		}
		// техен номер/дата
		docEgovMsgTN();					
		document.setCountFiles(filesList.size());
	}
	
	
	/**
	 * Зарежда вид документ, ако го намери
	 * зарежда доп.информация
	 * @return
	 * @throws DbErrorException
	 */
	private String docEgovMsgVD() throws DbErrorException {
		StringBuilder docInfo = new StringBuilder();
		if (!SearchUtils.isEmpty(egovMess.getDocVid())){	
			List<SystemClassif> res = sd.getItemsByTekst(DocuConstants.CODE_CLASSIF_DOC_VID, egovMess.getDocVid().trim(),getCurrentLang(), new Date());
			if(res != null && !res.isEmpty()){
				SystemClassif sc = res.get(0);
				document.setDocVid(sc.getCode()); // ако успея да го намеря в класификацията....
			} else{
				docInfo.append("Вид документ: " + egovMess.getDocVid()+"\n");
			}
		}
		if(Objects.equals(S_SEOS, egovMess.getSource()) && 
				  !SearchUtils.isEmpty(egovMess.getDocComment()) && !"N/A".equals(egovMess.getDocComment())) {
			docInfo.append(egovMess.getDocComment()); 
		}else if(!Objects.equals(S_SEOS, egovMess.getSource()) &&
				 !SearchUtils.isEmpty(egovMess.getMsgXml()) ) {
			// това е съдържанието от ССЕВ - като string!!
			docInfo.append(egovMess.getMsgXml()); 
		}
		return docInfo.toString();
	}
	
	/**
	 * Търси кореспондента по ЕИК
	 * @return
	 * @throws DbErrorException
	 */
	private String docEgovMsgCoresp() throws DbErrorException {
		boolean cc = true;
		String idvaOt = "";
		if (!SearchUtils.isEmpty(egovMess.getSenderEik())){
			List<SystemClassif> sender = sd.getItemsByCodeExt(DocuConstants.CODE_CLASSIF_REFERENTS, egovMess.getSenderEik(), getCurrentLang(), new Date());
			if(sender != null && !sender.isEmpty()){
				SystemClassif sc = sender.get(0);
				document.setCodeRefCorresp(sc.getCode()); // ако успея да го намеря в класификацията....
				cc = false;
			}	
		} 
		if (cc && !SearchUtils.isEmpty(egovMess.getSenderName())){
			String eikStr = "";
			if(!SearchUtils.isEmpty(egovMess.getSenderEik())) {
				eikStr = " ЕИК:" +egovMess.getSenderEik();
			}
			idvaOt ="Идва от: " + egovMess.getSenderName()+eikStr+"\n";
		}
		return idvaOt;
	}
	
	/**
	 * EgovMsg - техен номер/дата
	 */
	private void docEgovMsgTN() {
		if (!SearchUtils.isEmpty(egovMess.getDocRn())){							
			document.setTehNomer(egovMess.getDocRn());
			document.setTehDate(egovMess.getDocDate());
			//търсене по техен номер като включа, дата и кореспондент, ако е намерен! 
			searchTehNomerSql(egovMess.getDocRn(), egovMess.getDocDate(), document.getCodeRefCorresp());
			
		}else if (!SearchUtils.isEmpty(egovMess.getDocUriReg())){
			document.setTehNomer(egovMess.getDocUriReg() + "egovMess"+egovMess.getDocUriBtch());
			document.setTehDate(egovMess.getDocDate());
		}
	}
	
	/**
	 * Файлове към съобщения от СЕОС/ССЕВ
	 * @param idEgov
	 * @param daoEgov
	 */
	private void loadFilesEgovMess(Integer idEgov, EgovMessagesDAO daoEgov) {
		//  само ако има файлове към документа
		try {			
			List<EgovMessagesFiles> lstEgovFiles = 	daoEgov.findFilesByMessage(idEgov);
			if(lstEgovFiles != null && !lstEgovFiles.isEmpty() ) {
				Files newFile = null;  // GUID и останалите полета ????
				for(EgovMessagesFiles egovF: lstEgovFiles) { 
					newFile =  loadFilesFromMsg(egovF.getFilename(), egovF.getMime(), egovF.getBlobcontent());
					filesList.add(newFile);	
				}
				
			} else {
				String msg="За съобщение с id= "+idEgov+", липсват прикачени файлове!"; 
				LOGGER.error(msg);
			}
			
		} catch (BaseException e) {
			LOGGER.error("Грешка при извличане на файловете към съобщение от СЕОС/ССЕВ! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
		}
	}
	
	/**
	 * проверка за заключен документ / движение / съобщение от е-мейл
	 * заключване на съответния обект  
	 * @param idLock
	 * @param codeObj
	 * @return true - OK 
	 */
	private boolean checkAndLockDoc(Integer idLock, Integer codeObj) {	
		// проверка за заключен документ / движение / съобщение от е-мейл
		boolean fLockOk = checkForLockDoc(idLock, codeObj);
		if (fLockOk) {	
			// заключване на док., за да не може да се актуализира от друг и отключване на всички други обекти за потребителя(userId) и 
			// при рег. на официални от работни -  заключвам раб.,  Така ще избегнем дублиране, ако се работи едновременно
			// при приемане на док. от друга рег. - заключвам движението, с което е предаден документа
			// при рег. от е-мейл - заключва се messId, с код на обект CODE_ZNACHENIE_JOURNAL_MAILBOX
			// при рег. от СЕОС - заключва се id на съобщението, обект CODE_ZNACHENIE_JOURNAL_EGOVMESSAGE
			lockDoc(idLock, codeObj);					
		}
		return fLockOk;
	}
	
	
	
	/**
	 *  общосистемни настройки
	 */
	private void allSystemSettings() {
		try {
			nastrWithEkz = false;
			String param1 = sd.getSettingsValue("delo.docWithExemplars"); // да се работи с екземпляри
			if(Objects.equals(param1,String.valueOf(DocuConstants.CODE_ZNACHENIE_DA))) {
				nastrWithEkz = true;
			}
			scanModuleExist = false;
			param1 = sd.getSettingsValue("system.scanModuleExist"); // 	Системата има модул за сканиране на документи. 0-няма, 1-има
			if(Objects.equals(param1,String.valueOf(DocuConstants.CODE_ZNACHENIE_DA))) {
				scanModuleExist = true;
			}
			
		 } catch (DbErrorException e) {
			 LOGGER.error("Грешка при извличане на системни настройки! ", e);
			 JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
		}
	}


	
	/**
	 * Проверка за заключен документ 
	 * @param idObj
	 * @return true - ОК (док. не е заключен)
	 */
	private boolean checkForLockDoc(Integer idObj, Integer codeObj) {
		boolean res = true;	
		try { 
			Object[] lockObj =  new LockObjectDAO().check(ud.getUserId(), codeObj, idObj);
			if (lockObj != null) {
				 res = false;
				 String msg = getSystemData().decodeItem(DocuConstants.CODE_CLASSIF_ADMIN_STR, Integer.valueOf(lockObj[0].toString()), getUserData().getCurrentLang(), new Date())   
						       + " / " + DateUtils.printDateFull((Date)lockObj[1]);
				 JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_WARN,getMessageResourceString(LABELS, "docu.docLocked"), msg);
			}
		} catch (DbErrorException e) {
			LOGGER.error("Грешка при проверка за заключен документ! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
		}
		return res;
	}
	
	/**
	 * Заключване на документ, като преди това отключва всички обекти, заключени от потребителя
	 * @param idObj
	 */
	public void lockDoc(Integer idObj, Integer codeObj) {	
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("lockDoc! {}", ud.getPreviousPage() );
		}
		LockObjectDAO daoL = new LockObjectDAO();		
		try { 
			JPA.getUtil().runInTransaction(() -> 
				daoL.lock(ud.getUserId(), codeObj, idObj, null)
			);
		} catch (BaseException e) {
			LOGGER.error("Грешка при заключване на документ! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
		} 		
	}

	
	/**
	 * при излизане от страницата - отключва обекта и да го освобождава за актуализация от друг потребител
	 */
	@PreDestroy
	public void unlockDoc(){
        if (!ud.isReloadPage()) {
        	unlockAll(true);
        	ud.setPreviousPage(null);
        }          
        ud.setReloadPage(false);
	}
	
	
	/**
	 * отключва всички обекти на потребителя - при излизане от страницата или при натискане на бутон "Нов"
	 */
	private void unlockAll(boolean all) {
		LockObjectDAO daoL = new LockObjectDAO();		
		try { 
			if (all) {
				JPA.getUtil().runInTransaction(() -> 
					daoL.unlock(ud.getUserId())
			);
			} else {
				JPA.getUtil().runInTransaction(() -> 
					daoL.unlock(getUserData().getUserId(), DocuConstants.CODE_ZNACHENIE_JOURNAL_TASK)
				);
			}
		} catch (BaseException e) {
			LOGGER.error("Грешка при отключване на документ! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
		}
	}
	
	/**
	 *  Зарежда данните на документа за актуализация или разглеждане
	 *   
	 * @param docId
	 * @return
	 */
	private boolean loadDocumentEdit(Integer docId) {
		boolean flagOk = true;
		try {
			regUserFiltered = isView == 0;
			JPA.getUtil().runWithClose(() -> {
				document = this.dao.findById(docId);				
				if(document != null && this.dao.hasDocAccess(document, regUserFiltered, getSystemData())) {	//проверка за достъп до документа
					loadFilesList(document.getId(), UPDATE_DOC ); // 	load files
				} else {
					document = null;
				}
			});
			
		    if (document == null) {
		    	flagOk = false; // потребителят няма достъп до документа
		       	JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_WARN,getMessageResourceString(LABELS, "docu.docAccessDenied"));
		    } else {
		    	rnFullDocEdit = DocDAO.formRnDoc(this.document.getRnDoc(), this.document.getPoredDelo());
		    	actionChangeDocVid(false); // извлича настройките по вид документ
		    	setLimitedAccessCh(!Objects.equals(document.getFreeAccess(), DocuConstants.CODE_ZNACHENIE_DA));
		    	docProcessed(Objects.equals(document.getProcessed(), DocuConstants.CODE_ZNACHENIE_DA));
				docCompetence(); // компетентност
				
				docWorkOffId();// връзка офицален - работен; официален - друга регистратура
				
				// протокол за унищожение
				if(document.getProtocolData() != null) {
					rnFullProtocol = "  ПУ "+ document.getProtocolData()[1] + "/" + DateUtils.printDate((Date)document.getProtocolData()[2]) ;
					// ако има пореден номер - той е слепен предварително
				}
				
				deloDocPrep = new DeloDoc();
				//за значения от класификации, които трябва да се разкодират към датата на документа
				this.setDecodeDate(document.getDocDate());
							
				btnObraboten(false, false); // да се вижда ли бутона "Обработен"
				
				//зарежда референтите към документ
				loadReferentsData(UPDATE_DOC);
					
				// ако има техен номер - да се провери има ли други док. със същия тех.номер
				if(!SearchUtils.isEmpty(document.getTehNomer())) {
					searchTehNomerSql(document.getTehNomer(), null, null);
				}
				
				// тематики
				loadScTopicList();
				
				// зарежда допълнителните регистратури за раб. док. - зависи от правата на потребителя 
				loadDopRegistraturiList();
				if(document.getId() != null) {
					String param1 = sd.getSettingsValue("delo.journalOpenDeloDoc"); // да се журналира ли отварянето
					if(Objects.equals(param1,String.valueOf(DocuConstants.CODE_ZNACHENIE_DA))) {
						// запис в журнала, че док. е отоврен
						JPA.getUtil().runInTransaction(() -> this.dao.saveAudit(document, SysConstants.CODE_DEIN_OPEN));	
					}
				}
				
		    }
		} catch (BaseException e) {
			LOGGER.error("Грешка при зареждане данните на документа! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
		}
		return flagOk;
	}
	

	/**
	 * връзка офицален - работен; официален - друга регистратура
	 * @throws DbErrorException 
	 */
	private void docWorkOffId() throws DbErrorException {
		readyForOfficial = false;
		boolean tvdWrk = Objects.equals(document.getDocType(), DocuConstants.CODE_ZNACHENIE_DOC_TYPE_WRK);
		if(document.getWorkOffId() != null) {	
			Integer reg = Integer.valueOf((document.getWorkOffData()[3]).toString());
			String tvd  = ""; 
			String fromRegistratura = "";
			if(tvdWrk) {
				// в работен док. сме
				tvd  = getMessageResourceString(LABELS, "docu.officDoc");
			} else if(Objects.equals(reg, document.getRegistraturaId())) {
				// в официален, регистриран от работен
				tvd = getMessageResourceString(LABELS, "docu.wrkDoc"); 
				setFromOtherReg(false);
			} else { // от друга регистратура 
				fromRegistratura = " "+ getSystemData().decodeItem(DocuConstants.CODE_CLASSIF_REGISTRATURI, reg, getUserData().getCurrentLang(), new Date());
				setFromOtherReg(Objects.equals(document.getDocType(), DocuConstants.CODE_ZNACHENIE_DOC_TYPE_IN)); // ако е входящ - линка се вижда до техен номер, ако е собствен - тогава занчие, че е рег. на официален от работен! 
			}
			
			rnFullDocOther = tvd +" " +document.getWorkOffData()[1] + "/" + DateUtils.printDate((Date)document.getWorkOffData()[2]) + fromRegistratura+"  ";
			// ако има пореден номер - той е слепен предварително 
		} else if(tvdWrk &&  document.getForRegId() != null ) {
			readyForOfficial = true;
		}
	}
	
	/**
	 * зарежда допълнителните регистратури - само за раб. док, който не е рег. като официален
	 * филтрира се в зависимост от правата на потребителя
	 * @throws DbErrorException
	 * @throws UnexpectedResultException
	 */
	private void loadDopRegistraturiList() throws DbErrorException, UnexpectedResultException {
		if(this.dopRegistraturiList == null && 
			Objects.equals(document.getDocType(), DocuConstants.CODE_ZNACHENIE_DOC_TYPE_WRK) &&
			document.getWorkOffId() == null) {
			// контролата за избор на доп.регистратура да се види амо, ако резултатът този списък е > 1
			// Допълнителни регистратури за заявка за извеждане на документи - включва и основната!!		
			this.dopRegistraturiList = createItemsList(true,  DocuConstants.CODE_CLASSIF_REGISTRATURI_REQDOC, new Date(), true); 
			if(document.getPreForRegId()==null) {
				document.setPreForRegId(getUserData(UserData.class).getRegistratura()); // текуща регистртурата
			}
		}
	}
		
	
	/**
	 * разглеждане на работен/офицален док.; протокол за унищожение
	 * @param i
	 * @return
	 */
	public String actionGotoViewDoc(int i) {
		Integer idObj = null;
		if(i==1) {
			idObj = this.document.getWorkOffId(); // работен - офицален 
		} else{
			idObj = Integer.valueOf(this.document.getProtocolData()[0].toString()); //протокол за унищожение
		}
		return "docView.xhtml?faces-redirect=true&idObj=" + idObj;
	}
	
	
	/**
	 * Дали да се вижда бутона "обработен"
	 * @param newDoc
	 * @param fSave true - извиква се от actionSave
	 */
	private void btnObraboten(boolean newDoc, boolean fSave) {
		
		if(fSave && !newDoc  && createPrep ) {
			// за да се обнови таб "Вложен в преписки"
			DocDataPrep beanTabPrep = (DocDataPrep) JSFUtils.getManagedBean("docDataPrep");
			if(beanTabPrep != null){ 
				beanTabPrep.setRnFullDoc(null);
			}
		}
		
		if (!Objects.equals(document.getDocType(), DocuConstants.CODE_ZNACHENIE_DOC_TYPE_OWN) && ud.isDelovoditel()){
	    	// за работен и входящ, ако съм в роля деловодител 
	    	viewBtnProcessed ++;
	    	if(newDoc) {
	    		docProcessed(Objects.equals(document.getProcessed(), DocuConstants.CODE_ZNACHENIE_DA)); // ако има процедура и след запис е създадена задача
	    	}
	    }	else {
	    	viewBtnProcessed = 0;
	    }
	}
	
	/**
	 * зарежда референтите към документ - без входящи
	 * @param flag - 0(актуализаиця); 1(рег. на офиц. от раб.); 2(от друга регистратура)
	 */
	private void loadReferentsData(int flag) {
		if(!Objects.equals(document.getDocType(), DocuConstants.CODE_ZNACHENIE_DOC_TYPE_IN)) {
			// само за собствени документи
			loadNameReferentsList(document.getReferentsAuthor(), flag,  DocuConstants.CODE_ZNACHENIE_DOC_REF_ROLE_AUTHOR);
			loadNameReferentsList(document.getReferentsAgreed(), flag,  DocuConstants.CODE_ZNACHENIE_DOC_REF_ROLE_AGREED);
			loadNameReferentsList(document.getReferentsSigned(), flag,  DocuConstants.CODE_ZNACHENIE_DOC_REF_ROLE_SIGNED);
		}
	}
	
	
	/**	
	 * Извлича списъка с файлове към документ
	 * @param wrkDocId - id  на документ
	 * @param @param flag - 0(актуализаиця); 1(рег. на офиц. от раб.); 2(от друга регистратура)
	 */
	private void loadFilesList(Integer wrkDocId, Integer flag ) {
	
		try {				
			FilesDAO daoF = new FilesDAO(getUserData());		
			filesList = daoF.selectByFileObjectDop(wrkDocId, DocuConstants.CODE_ZNACHENIE_JOURNAL_DOC); // използва се този метод за зареждане на файлове, за да се заредят доп. полета - лични дании, официален док и т.н.
		
			if (flag != UPDATE_DOC && filesList != null && !filesList.isEmpty() ) {
				onlyMarkFiles();
			}
		
		} catch (BaseException e) {
			LOGGER.error("Грешка при извличане на файловете към документ! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
		}	
				
	}
	
	/**
	 * да се извлекат само файловете, маркирани като официални
	 */
	private void onlyMarkFiles() {
		List<Files> fList = new ArrayList<>();
		for (Files f : filesList) {
			// да се вземат само маркираните като официални от работния документ или при рег. от други регистратура
			if(Objects.equals(f.getOfficial(),DocuConstants.CODE_ZNACHENIE_DA)) {
				fList.add(f);
			}
		}
		filesList.clear();
		if(!fList.isEmpty()) {
			filesList.addAll(fList);
		}
	}
	
	/**
	 * зарежда тематиките на документа
	 */
	private void loadScTopicList(){
		List<SystemClassif> tmpLst = new ArrayList<>();		
		if(document.getTopicList() != null) {
			for( Integer item : document.getTopicList()) {
				String tekst = "";
				SystemClassif scItem = new SystemClassif();
				try {
					scItem.setCodeClassif(DocuConstants.CODE_CLASSIF_DOC_TOPIC);
					scItem.setCode(item);
					tekst = getSystemData().decodeItem(DocuConstants.CODE_CLASSIF_DOC_TOPIC, item, getUserData().getCurrentLang(), new Date());		
					scItem.setTekst(tekst);
					tmpLst.add(scItem);
				} catch (DbErrorException e) {
					LOGGER.error("Грешка при зареждане на тематики на документ! ", e);
				}		
			}				
		}
		setScTopicList(tmpLst); // тематики
	}

	
	/**
	 * 
	 * @param wrkDocId:
	 * id на предаден от друга рег. документ, който ще бъде регистриран като официален
	 * id на работен документ, който ще бъде регистриран като официален
	 * @param fw: 1(рег. на офиц. от раб.); 2(от друга регистратура)
	 */	
	public void loadDocFromOtherDoc(Integer wrkDocId, int fw) {
		try {
			regUserFiltered = true;
			
			JPA.getUtil().runWithClose(() -> {
				document = this.dao.findById(wrkDocId);
				// load files 
				loadFilesList(wrkDocId,fw);
				if(this.sourceRegDvijId != null) {
					dvijData = dao.findDocDvijData(this.sourceRegDvijId);
				}
			});
							
			// формирам инф. за показване на екрана: рег.номер/дата на предадения документ + регистратура, от която идва 
			String fromRegistratura ="";
			if(fw == REG_OTHER_R ) {
				fromRegistratura =  " "+getSystemData().decodeItem(DocuConstants.CODE_CLASSIF_REGISTRATURI, document.getRegistraturaId(), getUserData().getCurrentLang(), new Date());
				setFromOtherReg(true);				
			}
			rnFullDocOther = DocDAO.formRnDocDate(this.document.getRnDoc(), this.document.getDocDate(), this.document.getPoredDelo())+fromRegistratura;
			
			// прехвърлям id на док. източник в поле за връзка workOffId 	
			document.setWorkOffId(wrkDocId);
			// да нулирам ид-то
			document.setId(null);
			// общи настр. за екрана - нов документ - да сменя типа на документа, дата на рег. и др.
			if(fw == REG_WRK_DOC) {
				//зарежда референтите (автор/подписал/съгалсувал) към документ и нулира id на връзката
				loadReferentsData(REG_OTHER_R );
				// типа е "собствен" и смяната на тип документ е забранена
				newDocSettings(DocuConstants.CODE_ZNACHENIE_DOC_TYPE_OWN, false);
				document.setDocInfo(null); 
			} else if (fw == REG_OTHER_R ) {
				loadFromOtherDocR(fromRegistratura);	
			}
				
			//зачиства специфичини полета в документа- когато от док. източник, трябва да направя нов официален
			clearDocSpecData();
			
			// зачиства данните за преписката, ако има такава
			clearDeloDocLink();
			deloDocPrep = new DeloDoc();
			
			// 	настройки на регистртура - нов документ 
			newDocRegSettings();
            
			// нулирам регистъра - освен, ако не е ясно по подразбиране кой е регистъра... 
			document.setRegisterId(null);
			
			// да взема настройките по вид документ, ако има такива	
			nastrDocType(document.getDocType());
		    actionChangeDocVid(true);
		    document.setValid(DocuConstants.CODE_CLASSIF_DOC_VALID_ACTUAL);
			
		} catch (DbErrorException e) {
			LOGGER.error("Грешка при зареждане данните на раб. док. при регистриране на официален! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
		} catch (BaseException e) {
			LOGGER.error("Грешка при зареждане данните на раб. док. при регистриране на официален!", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
		}
	}
	
	/**
	 * зарежда данните за док. - идва от друга регистртура
	 * @param fromRegistratura
	 */
	private void loadFromOtherDocR(String fromRegistratura) {
		String tmpDopInfo = "";	
		if(dvijData != null) {
			if(dvijData[1]!=null) {
				fromRegistratura += "; Изпратен на "+ dvijData[1].toString();
			}
			if(dvijData[2]!=null) {
				tmpDopInfo = dvijData[2].toString()+"\r\n";						
			}
			if(dvijData[3]!=null) {
				tmpDopInfo += "Да се върне до:" +DateUtils.printDate((Date)dvijData[3]);
			}
		}
		if(SearchUtils.isEmpty(tmpDopInfo)) {
			tmpDopInfo = null;
		}
		document.setDocInfo(tmpDopInfo);
		document.setTehNomer(this.document.getRnDoc());
		document.setTehDate(this.document.getDocDate());
		// типа е "входящ" и  смяната на тип документ е забранена
		newDocSettings(DocuConstants.CODE_ZNACHENIE_DOC_TYPE_IN, false);
		document.setReceiveMethod(DocuConstants.CODE_ZNACHENIE_PREDAVANE_DRUGA_REGISTRATURA);
		document.setReceiveDate(new Date());
		document.setReceivedBy(fromRegistratura);
		// да се провери има ли други док. със същия тех.номер
		searchTehNomerSql(document.getTehNomer(), null, null);
		// новият документ е "входящ" - да се зачистят данните за автор/подписал/съгласувал, които идват от изпратения от другата рег. документ (ако има такива)
		document.setReferentsAuthor(null);
		document.setReferentsAgreed(null);
		document.setReferentsSigned(null);
	}
	
	/**
	 * Зарежда(разкодира) имената на референтите в списъците - автор, подписал, съгласувал
	 * @param listRef
	 * @param flag - 0(актуализаиця); 1(рег. на офиц. от раб.) 
	 */
	private void loadNameReferentsList(List<DocReferent> listRef, int flag, int role) {
		if (listRef == null || listRef.isEmpty()) {
			return;
		}
		for( DocReferent drItem: listRef ) {
			String tekst = "";
			try {				
				tekst = getSystemData().decodeItem(DocuConstants.CODE_CLASSIF_ADMIN_STR, drItem.getCodeRef(), getUserData().getCurrentLang(), document.getDocDate());
				drItem.setTekst(tekst);
				if(flag != UPDATE_DOC) {
					//ако е рег. на официален от работен и др.... 
					drItem.setId(null);
					drItem.setDocId(null);
					drItem.setUserLastMod(null);
					drItem.setDateLastMod(null);
				}else if ( role == DocuConstants.CODE_ZNACHENIE_DOC_REF_ROLE_AUTHOR &&
						Objects.equals(document.getDocType(), DocuConstants.CODE_ZNACHENIE_DOC_TYPE_WRK) &&
						ud.getUserAccess().equals(drItem.getCodeRef())) {
					viewBtnProcessed ++; // за работен документ, ако съм автор - да видя бутон "обработен" - ще се види само при актуализация!
				}
			} catch (DbErrorException e) {
				LOGGER.error("Грешка при зареждане на референти (автор, подписал, съгласувал)! ", e);
				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
			}		
		}
	}
	
	
	
	/** Нов документ
	 *  flag = true - ако документа се отваря през точка от менюто "Нов документ със запазване на данни"
	 *  flag2 = true - натиснат е бутон Нов вътре в екрана
	 * @return
	 */
	public void actionNewDocument(boolean flagS, boolean flag2 ) {
		Integer typeD = DocuConstants.CODE_ZNACHENIE_DOC_TYPE_IN;
		if(docTypeList != null && docTypeList.size()<3) {
			typeD = (Integer) docTypeList.get(0).getValue();
		}
	
		Doc docC = null;
		if(flagS) { 
			//запомняне на данни от предишния док. //типа се помни винаги 
			docC = document;
		} 
		if(flag2) {
			flagFW = UPDATE_DOC; 
			typeD = document.getDocType();
		}
		
		this.document = new Doc();	
		rnFullDocEdit = null;
		rnFullDoc = null;
		rnFullDocOther = null;
		rnFullProtocol = null;
		sourceRegDvijId = null; 
		viewBtnProcessed = 0;	
		setScTopicList(new ArrayList<>()); // тематики
		// общи настр. за екрана - нов документ
		newDocSettings(typeD, flagS);
		if(flagS) { 
			createPrep = createPrepOld;
		} 
		// настройки в зависимост от типа на документа
		nastrDocType(typeD);	
	    // да позволи избор на процедура 
		enableProc();
		// зачиства данните за преписката
		clearDeloDocLink();
		
		// 	настройки на регистртура - нов документ
		newDocRegSettings();
	
		// зачиства данни за връзка по техен номер
		selectedDocsTn = null;
		//само за работния
		readyForOfficial = false;
		
		filesList = new ArrayList<>();
		
		// "за резолюция"
		rezolTask = new Task();	
		rezolExecClassif = new ArrayList<>();
		tnRez = 0; // тех.ном.
		
		// прехвъляне на запомнените данни от предишния док.
		// вид, относно, спешност, регистър, бр. листа - за всички документи 
	
		// за вх. документи -  връзка с док., идва от, кореспондент, насочването на документа
		//  - за собствените - връзка с док., идва от, кореспондент, подписал, съгласувал, автор	

		if(docC != null) {
			document.setDocVid(docC.getDocVid());  		
			document.setRegisterId(docC.getRegisterId());
			document.setOtnosno(docC.getOtnosno());
			document.setUrgent(docC.getUrgent());
			document.setCountSheets(docC.getCountSheets());
		}
		
		
		if (flagFW != REG_FROM_MAIL && flagFW != REG_FROM_EGOV) {
			unlockAll(true); // да се отключи предишния документ, но да не маха от UserData previousPage
		}
		if(flagS) {
			ud.setReloadPage(false); //при натискане на бутон "нов документ" - вътре в екрана
		}
		document.setValid(DocuConstants.CODE_CLASSIF_DOC_VALID_ACTUAL);
	}
	
	
	/**
	 * общи настр. за екрана - нов документ
	 * @param typeDoc - тип на документ по подразбиране
	 * @param newDocCopy - true - със запазване на данни от предишен док.
	 */
	private void newDocSettings(Integer typeDoc, boolean newDocCopy) {
		this.setDecodeDate(new Date());	
		this.document.setRegistraturaId(getUserData(UserData.class).getRegistratura());		
		this.document.setDocDate(new Date());
		this.document.setDocType(typeDoc);
		regUserFiltered = true;
		rnFullDoc = null;
		processedCh = false;
		competence = false;
		createPrep = false;
		if(!newDocCopy) {
			this.document.setCountOriginals(1); // поподразбиране - 1 екземпляр
			avtomNoDisabled = false;
			templatesList = null;
			docSettingId = null;
			docSettingHar = null;
			membersTab = null;
		}
	
	}
		
   /**
    * настройки в зависимост от типа на документа 
    * @param typeDoc - тип на док.
    */
   private void nastrDocType(Integer typeDoc) {
	// да се филтрира списъка с регистри в зависимост от типа на документа 
	    document.setRegisterId(null);
	    avtomNoDisabled =  false;
	    avtomNo = true;
	    document.setDocType(typeDoc);
	    
	 // спецификите за списъка с регистри - зависимост от типа документ и списък процедури
	  
	    classifProceduri = null;
	   	document.setProcDef(null);
       	document.setProcExeStat(null); 
		specTypeDocProc(typeDoc);
		
		if(document.getId() == null && showSignMethod != null) {
			document.setSignMethod(showSignMethod);
		}else {
			document.setSignMethod(null);
		}
			
	    // за нов работен - да се зареди по подразбиране автор - userSave
	    if(document.getId() == null && Objects.equals(typeDoc, DocuConstants.CODE_ZNACHENIE_DOC_TYPE_WRK)) {
			try {
				DocReferent drItem = new DocReferent();
				drItem.setCodeRef(ud.getUserSave());	
				drItem.setRoleRef(DocuConstants.CODE_ZNACHENIE_DOC_REF_ROLE_AUTHOR);
				String tekst = getSystemData().decodeItem(DocuConstants.CODE_CLASSIF_ADMIN_STR, drItem.getCodeRef(), getUserData().getCurrentLang(), document.getDocDate());
				drItem.setTekst(tekst);
				List<DocReferent> tmp = new ArrayList<>();
				tmp.add(drItem);
				document.setReferentsAuthor(tmp);
				loadDopRegistraturiList();
			} catch (DbErrorException | UnexpectedResultException e) {
				LOGGER.error("Грешка при зареждане в работен документ на автор по подразбиране или списък с допълнителни регистртури!", e);	
			}
	    }else {
	    	dopRegistraturiList = null;
	    }
   }
   
   /**
    * 1. Спец. по тип документ
    * 2. специфики и списък с процедури
    * @param typeDoc
    */
    private void specTypeDocProc(Integer typeDoc){
    	if(specificsRegister == null) {
			specificsRegister = new HashMap<>();
			specificsRegister.put(DocuClassifAdapter.REGISTRI_INDEX_REGISTRATURA, Optional.of(getUserData(UserData.class).getRegistratura())); //?? трябва ли да го има
			specificsRegister.put(DocuClassifAdapter.REGISTRI_INDEX_VALID, SysConstants.CODE_ZNACHENIE_DA);
    	}
    	if (Objects.equals(typeDoc, DocuConstants.CODE_ZNACHENIE_DOC_TYPE_WRK)) { // изрично само за работни
    		specificsRegister.put(DocuClassifAdapter.REGISTRI_INDEX_DOC_TYPE, typeDoc);
    		
    	} else { // за конкретния тип док + тези регистри , за които не е зададен тип документ
    		specificsRegister.put(DocuClassifAdapter.REGISTRI_INDEX_DOC_TYPE, Optional.of(typeDoc));
    	}   
    	enableProc();
    	specProcedure();
    }
    
    /**
     * специфики и списък с процедури
     */
    private void specProcedure() {
    	try {
    		if(classifProceduri == null && enableProc) { 		
				specificsProc = new HashMap<>();
				specificsProc.put(DocuClassifAdapter.PROCEDURI_INDEX_REGISTRATURA, getUserData(UserData.class).getRegistratura()); 
				specificsProc.put(DocuClassifAdapter.PROCEDURI_INDEX_STATUS, DocuConstants.CODE_ZNACHENIE_PROC_DEF_STAT_ACTIVE);
				specificsProc.put(DocuClassifAdapter.PROCEDURI_INDEX_DOC_TYPE, this.document.getDocType());
				classifProceduri = getSystemData().queryClassification(DocuConstants.CODE_CLASSIF_PROCEDURI, null, new Date(), getCurrentLang(), specificsProc);
			}
		} catch (DbErrorException e) {
			LOGGER.error("Грешка при зареждане на списък с процедури!", e);	
		}
    	
    }
     
    /**
     * Определя дали да се разреши избор на процедура
     */
    private void enableProc() {
    	//дали разрешава стартиране на процедура в зависимост от стауса й, ако вече док. е стартиран по процедура,
    	// notFinishedProc=true - раб. по нея не е приключила и не се позвлоява стартиране на друга проц. по документа - бутона "Избор на нова процедура" е скрит
    	notFinishedProc = Objects.equals(DocuConstants.CODE_ZNACHENIE_PROC_STAT_WAIT,document.getProcExeStat()) ||
    					  Objects.equals(DocuConstants.CODE_ZNACHENIE_PROC_STAT_EXE, document.getProcExeStat());
    	
    	enableProc = document.getProcExeStat() == null; 
    	// за нов документ - винаги трябва да се вижда контролата за избор на процедура    	
    }
    
    /**
     * Натиснат е бутона "Нова процедура", който се вижда само, ако enableProc=false;
     */
    public void actionNewProcedure() {
    	enableProc = true;
    	notFinishedProc = false; 
       	document.setProcDef(null);
       	document.setProcExeStat(null); // за да мине записa!
    	specProcedure();
    }
    
    /**
     * Извежда съобщение, ако е избрана процедура
     */	
    public void changeProcedure() {
    	if(document.getProcDef() != null) {
    		JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, getMessageResourceString(beanMessages, "docu.startProcedure") );
    	}
    }
    /**
     * Настройки на регистрaтура - без значение дали е за нов документ или е актуализация
     */
    private void  docRegSettings() {
 	  try {
 		    //В собствен документ да се поддържа за кого се отнася, т.е да се вижда ли полето "Кореспондент" или да не се вижда
 			Integer s1 = ((SystemData) getSystemData()).getRegistraturaSetting(document.getRegistraturaId(), DocuConstants.CODE_ZNACHENIE_REISTRATURA_SETTINGS_8);
 			if(Objects.equals(s1,  DocuConstants.CODE_ZNACHENIE_DA)) {
 				showCoresp = true;
 			}else {
 				showCoresp = false;
 			}
 			//Допуска се ръчно въвеждане на съгласувал и подписал в работен документ
 			s1 = ((SystemData) getSystemData()).getRegistraturaSetting(document.getRegistraturaId(), DocuConstants.CODE_ZNACHENIE_REISTRATURA_SETTINGS_4);
 			if(Objects.equals(s1,  DocuConstants.CODE_ZNACHENIE_DA)) {
 				editReferentsWRK = true;
 			}else {
 				editReferentsWRK = false;
 			}
 			
 			//Изпълнител на задача може да бъде от друга регистратура - тук се използва за "бързите" задачи от тип резолюция
			s1 = sd.getRegistraturaSetting(ud.getRegistratura(), DocuConstants.CODE_ZNACHENIE_REISTRATURA_SETTINGS_16);
			if(Objects.equals(s1,  DocuConstants.CODE_ZNACHENIE_NE)) {
				Object[] codeExt = new Object[] {DocuClassifAdapter.ADM_STRUCT_INDEX_REGISTRATURA, ud.getRegistratura().toString(), IndexUIbean.getMessageResourceString(beanMessages,"task.msgCodeExt")};
				setCodeExtCheck(codeExt);
			}else {
				setCodeExtCheck(null);
			}
			
			//да се позволи въвеждане на начин на подписване на официалните документи
			showSignMethod = null;
			s1 = ((SystemData) getSystemData()).getRegistraturaSetting(document.getRegistraturaId(), DocuConstants.CODE_ZNACHENIE_REISTRATURA_SETTINGS_17);
 			if(s1 != null) {
 				showSignMethod = s1;
 				if(s1!= 0  && document.getId() == null) {
 					document.setSignMethod(s1);
 				} 				
 			}
 			
 		
 		} catch (DbErrorException e) {
 			LOGGER.error("Грешка при извичане на настройки на регистратура: {} ", document.getRegistraturaId()+" ! ", e);
 			
 		}
    }
    
   
    
	/**
	 * настройки на регистртура - нов документ
	 */
	private void newDocRegSettings() {
		try {
			avtomNo = true; 
			// Код на значение "Включен по подразбиране чек-бокс за генериране на рег. номер на документ" класификация "Настройки на регистратура" 151
			Integer s1 = sd.getRegistraturaSetting(document.getRegistraturaId(), DocuConstants.CODE_ZNACHENIE_REISTRATURA_SETTINGS_1);
			if( Objects.equals(s1,  DocuConstants.CODE_ZNACHENIE_NE)) {
				avtomNo = false; 
			}
			
			this.document.setFreeAccess(DocuConstants.CODE_ZNACHENIE_NE);
			limitedAccessCh = true;
			// Код на значение "Документи и преписки по подразбиране са с ограничен достъп" класификация "Настройки на регистратура" 151
			s1 = sd.getRegistraturaSetting(document.getRegistraturaId(), DocuConstants.CODE_ZNACHENIE_REISTRATURA_SETTINGS_12);
			if( Objects.equals(s1,  DocuConstants.CODE_ZNACHENIE_NE)) {
				limitedAccessCh = false; 
				this.document.setFreeAccess(DocuConstants.CODE_ZNACHENIE_DA);
			}
			
			// ? В собствен документ да се поддържа за кого се отнася (да се вижда кореспондента)
			
		} catch (DbErrorException e) {
			LOGGER.error("Грешка при извичане на настройка CODE_ZNACHENIE_REISTRATURA_SETTINGS_1 на регистратура: {} ", document.getRegistraturaId()+" ! ", e);
		}
	}
	
	/**
	 * зачиства специфичини полета в документа- когато от раб. док., трябва да направя нов официален 
	 */
	private void clearDocSpecData(){

		// да нулирам рег. номер и дата
		document.setRnDoc(null);
		document.setRnPored(null);
		document.setRnPrefix(null);
		document.setDeloIncluded(false);
		document.setWorkOffData(null);
		document.setDocAccess(null);
		document.setHistory(null);
		document.setUserLastMod(null);
		document.setDateLastMod(null);
		document.setGuid(null);
		if(this.flagFW == REG_OTHER_R || this.flagFW  == REG_WRK_DOC) {
			// да нулирам полетата за статус, валидност и т.н.
			document.setStatus(null);
			document.setStatusDate(null);
			document.setValid(null);
			document.setValidDate(null);
			//document.setDocInfo(null); // забележката да не се прехвърля! В нея се записва доп. информация от движението при пререг. от друга рег.!
		}
		processedCh = false;
		readyForOfficial = false;
	}
	
	/** Зарежда данните на документа за разглеждане - от журнала 
	 * 
	 * @param docId
	 * @return
	 */
//	private boolean loadDocumentView(Integer docId) {
//		boolean bb = false;
//		
//		regUserFiltered = false; // ако е за разглеждане - да списъкът с регистри да не се филтрира в зависимост от правата на потребителя
//
//		return bb;
//	}
		
	/**
	 * Запис на документ
	 */
	public void actionSave() {
	
		//Integer alg  = (Integer) getSystemData().getItemSpecific(DocuConstants.CODE_CLASSIF_REGISTRI, document.getRegisterId(), getUserData().getCurrentLang(), new Date(), DocuClassifAdapter.REGISTRI_INDEX_ALG);
		//  проверки дали избрания тип док. и регистър са съвместими - повреме на записа - излиза съобщение 
		
		if(checkDataDoc()) { 			
			try {
				
				documentParamsAPC();
				
				// файловете за нов документ - да се запишат с него
				// ако е редакция на документ - се записват при upload
				boolean newDoc = this.document.getId() == null;
				
				//TODO Трябва да се измисли първо как ще се индексират прикачените файлове
//				List<String> ocrDocs = this.document.getOcr(this.filesList);
			
				saveDoc(newDoc);
				  			 				
				clearDeloDocLink(); // махам връзката, ако се натисне втори път запис;  само за нов документ deloDocPrep.getId() би могло да е != null
				createPrepOld = createPrep; // ако ще се запазват данни за док. "Нов док. със запазване на данни"
				if(newDoc || createPrep) {
					rnFullDocEdit = DocDAO.formRnDoc(this.document.getRnDoc(), this.document.getPoredDelo());
				}
				
				btnObraboten(newDoc, true);			
				createPrep = false;
				
			    saveVrazkiTn(); // запис на връзките - от техен номер
				
				if(newDoc) {// само за нов документ	
					
					// заключване на док.
					lockDoc(this.document.getId(), DocuConstants.CODE_ZNACHENIE_JOURNAL_DOC);
					
					if(flagFW == REG_FROM_MAIL ) {
						// 1.мейла да се премести в друга папка					
						// 2. да се върне отговор! 						
						createReturnMail(true);		        
					} else if(flagFW == REG_FROM_EGOV && egovMess != null) {
						egovMess = null;
					}
					
					flagFW = UPDATE_DOC;
				}
				// да обнови статуса на процедурата
				enableProc();	
				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, getMessageResourceString(UI_beanMessages, SUCCESSAVEMSG) );		
			} catch (ObjectInUseException e) {
				LOGGER.error("Грешка при запис на документа! ObjectInUseException "); 
				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
			} catch (BaseException e) {			
				LOGGER.error("Грешка при запис на документа! BaseException", e);				
				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,  getMessageResourceString(UI_beanMessages, ERRDATABASEMSG),e.getMessage());
			} catch (Exception e) {
				LOGGER.error("Грешка при запис на документа! ", e);					
				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,  getMessageResourceString(UI_beanMessages, ERRDATABASEMSG),e.getMessage());
			}
		}
	}
	
	/**
	 * зарежда параметрите на документа - access(достъп), processed(обработен), competence(по компетентност)
	 */
	private void documentParamsAPC(){
		if(limitedAccessCh) {
			this.document.setFreeAccess(DocuConstants.CODE_ZNACHENIE_NE); //ограничен достъп
		}else {
			this.document.setFreeAccess(DocuConstants.CODE_ZNACHENIE_DA); // свободен достъп (общодостъпен)
		}	
		
		if(processedCh) {
			this.document.setProcessed(DocuConstants.CODE_ZNACHENIE_DA);
		}else {
			this.document.setProcessed(DocuConstants.CODE_ZNACHENIE_NE);
		}	
		
		if(!competence){
			this.document.setCompetence(DocuConstants.CODE_ZNACHENIE_COMPETENCE_OUR);
			this.document.setCompetenceText(null);
		} else if(!Objects.equals(this.document.getCompetence(), DocuConstants.CODE_ZNACHENIE_COMPETENCE_SENT)) {
			this.document.setCompetence(DocuConstants.CODE_ZNACHENIE_COMPETENCE_FOR_SEND);
		}
	}
	
	/**
	 * извиква метода за запис на документ и файловете в обща транзакция
	 * @param newDoc
	 * @throws BaseException
	 */
	private void saveDoc(boolean newDoc) throws BaseException {
		JPA.getUtil().runInTransaction(() -> { 
			document.setCountFiles(filesList == null ? 0 : filesList.size());
			
			this.document = this.dao.save(document, createPrep, deloDocPrep.getDeloId(), sourceRegDvijId, getSystemData());

			if (newDoc && filesList != null && !filesList.isEmpty()) {
				// при регистриране на офицален от работен!!!
				// да направи връзка към  новия документ
				FilesDAO filesDao = new FilesDAO(getUserData());
				for (Files f : filesList) {
					filesDao.saveFileObject(f, this.document.getId(), DocuConstants.CODE_ZNACHENIE_JOURNAL_DOC);
				}
			}	
			
			if(membersTabForDel) {
				 //имало е таб участници за предишния вид. док., а сега няма
				 //проверка дали преди това е имало въведени вече участници - ако да - трябва да се изтрият на записа на документа
				 int brMem = dao.findDocMembersCount(document.getId());
				 if(brMem > 0) {
					 dao.deleteDocMembers(document.getId());
					 DocDataMembers bean = (DocDataMembers) JSFUtils.getManagedBean("docDataMembers");
					 if(bean != null) {
						 bean.setRnFullDoc(null); // за да е сигурно, че ако пак този таб е видим - ще се зареди всичко наново!
					 }
				 }
			}
			
			if(egovMess != null) {
				// 1. да се направи update на съобщението в table EGOV_MESSAGES
				// 2. само за СЕОС съобщенията - да се изпрати отговор, че е регистрирано!
				createReturnEGOV(true);
				//TDDO - запис на файловете!!!!
			}
		});
	}

	
	
	/**
	 * проверка за задължителни полета при запис на документ
	 */
	public boolean checkDataDoc() {
		boolean flagSave =  checkDates();
		
		if(!avtomNo && SearchUtils.isEmpty(document.getRnDoc())) {
			JSFUtils.addMessage(DOCFORMTABS+":regN",FacesMessage.SEVERITY_ERROR,getMessageResourceString(UI_beanMessages,MSGPLSINS,getMessageResourceString(LABELS, "repDoc.regnom")));
			flagSave = false;
		}
		
		if(document.getDocVid() == null) {
			JSFUtils.addMessage(DOCFORMTABS+":dVid:аutoCompl",FacesMessage.SEVERITY_ERROR,getMessageResourceString(UI_beanMessages,MSGPLSINS,getMessageResourceString(LABELS, "docu.vid")));
			flagSave = false;
		}else if ( Objects.equals(document.getDocType(), DocuConstants.CODE_ZNACHENIE_DOC_TYPE_OWN)) {
			// за собствените - полето "Обработен" не се вижда, но да има стойност "ДА"
			document.setProcessed(DocuConstants.CODE_ZNACHENIE_DA);
		}
		
		if(document.getRegisterId() == null) {
			JSFUtils.addMessage(DOCFORMTABS+":registerId:аutoCompl",FacesMessage.SEVERITY_ERROR,getMessageResourceString(UI_beanMessages,MSGPLSINS,getMessageResourceString(LABELS, "docu.register")));
			flagSave = false;
		}
		
		if(SearchUtils.isEmpty(document.getOtnosno())) {
			JSFUtils.addMessage(DOCFORMTABS+":otnosno",FacesMessage.SEVERITY_ERROR,getMessageResourceString(UI_beanMessages,MSGPLSINS,getMessageResourceString(LABELS, "docu.otnosno")));
			flagSave = false;
		}
		
		
		// проверка за валиден номер - в метода за запис в дао...
		
		return flagSave ;
	}

	/**
	 * Проверка за валидни дати в документа
	 * @return
	 */
	private boolean checkDates() {
		int flagDatesOk = 0;// всичко е ок
		
		Date docDate = DateUtils.startDate(document.getDocDate());
		if(docDate == null || docDate.after(DateUtils.startDate(new Date()))) {
			JSFUtils.addMessage(DOCFORMTABS+":regDat",FacesMessage.SEVERITY_ERROR,getMessageResourceString(UI_beanMessages,MSGPLSINS,getMessageResourceString(LABELS, "docu.docDate")));
			flagDatesOk ++;
		} else { 
			//дата на техен номер - преди дата на документа
			Date tmpDate = DateUtils.startDate(document.getTehDate());
			flagDatesOk += checkDatesA(tmpDate, docDate, ":tehDat", "docu.datTehNom");
		
			// дата на получаване - преди дата на документа
			tmpDate = DateUtils.startDate(document.getReceiveDate());
			flagDatesOk += checkDatesA(tmpDate, docDate, ":receiveDat", "docu.receiveDate");
						
			// дата на валидност - след дата на документа
			tmpDate = DateUtils.startDate(document.getValidDate());
			if(tmpDate == null || document.getDocType().equals(DocuConstants.CODE_ZNACHENIE_DOC_TYPE_WRK)) {
				document.setValidDate(docDate);
			} else {
				flagDatesOk += checkDatesB(tmpDate, docDate, ":validDat", "docu.validDate");
			}
			// дата на статус на обработка - след дата на документа
			tmpDate = DateUtils.startDate(document.getStatusDate());
			flagDatesOk += checkDatesB(tmpDate, docDate, ":statusDat", "docu.statusDate");

			// дата за връщане на отговор - след дата на документа
			tmpDate = DateUtils.startDate(document.getWaitAnswerDate());
			flagDatesOk += checkDatesB(tmpDate, docDate, ":replayDat", "docu.answerDate");

		}
		return flagDatesOk == 0;
	}
	
	
	/**
	 *  проверка за валидност на дата - преди дата на документа
	 * @param tmpDate
	 * @param docDate
	 * @param idComp
	 * @param msg
	 * @return
	 */
	private int checkDatesA(Date tmpDate, Date docDate, String idComp, String msg) {
		int bb = 0;
		if(tmpDate != null && tmpDate.after(docDate)){
			JSFUtils.addMessage(DOCFORMTABS+idComp,FacesMessage.SEVERITY_ERROR,getMessageResourceString(beanMessages,MSGVALIDDATES,getMessageResourceString(LABELS, msg)));
			bb++;
		}
		return bb;
	}
	
	
	/**
	 * проверка за валидност на дата - след дата на документа
	 * @param tmpDate
	 * @param docDate
	 * @param idComp
	 * @param msg
	 * @return
	 */
	private int checkDatesB(Date tmpDate, Date docDate, String idComp, String msg) {
		int bb = 0;
		if(tmpDate != null && tmpDate.before(docDate)){
			JSFUtils.addMessage(DOCFORMTABS+idComp,FacesMessage.SEVERITY_ERROR,getMessageResourceString(beanMessages,MSGVALIDDATES,getMessageResourceString(LABELS, msg)));
			bb++;
		}
		return bb;
	}
	
	/**
	 * Изтриване на деловоден документ
	 * права на потребителя за изтриване
	 * 
	 */
	public void actionDelete() {
		try {
	
			JPA.getUtil().runInTransaction(() ->  {
				this.dao.deleteById(document.getId());
				
				if (filesList != null && !filesList.isEmpty()) {
				
					FilesDAO filesDao = new FilesDAO(getUserData());
					for (Files f : filesList) {
						filesDao.deleteFileObject(f);	
					}
				}
			}); 
		
			this.dao.notifDocDelete(document, sd);

			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO,  IndexUIbean.getMessageResourceString(UI_beanMessages, "general.successDeleteMsg") );
			actionNewDocument(false, true);
			
			createPrep = false;
		} catch (ObjectInUseException e) {
			// ако инициира преписка и има други връзки 
			LOGGER.error("Грешка при изтриване на документа!", e); 
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,getMessageResourceString(UI_beanMessages, OBJECTINUSE), e.getMessage());
		} catch (BaseException e) {			
			LOGGER.error("Грешка при изтриване на документа!", e);			
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
		} 
	}
	
	/**
	 * "За резолюция" - нови индивидуални задачи от тип "резолюция"
	 */
	public void actionTaskRezol() {
	//		String path = DOCFORMTABS+":lstIzpR:dialogButtonM";
	//		String  cmdStr = "document.getElementById('"+path+"').click()";
		if(rezolTask == null ) {
			// да се запазят стойностите,само ако задачата е нова и е натиснат бутон "потвърждение" - иначе се възприема като отказ
			rezolTask = new Task();   
			rezolExecClassif = new ArrayList<>();
		}
		String  cmdStr = "PF('modalRezol').show();";
		PrimeFaces.current().executeScript(cmdStr);	
	}
	
	/**
	 * "За резолюция" бутон потвърждение от модалния - индивидуални задачи от тип "резолюция"
	 */
	public void actionSaveRezol() {
		if(rezolTask.getCodeExecs() != null && !rezolTask.getCodeExecs().isEmpty()) {
			saveRezol(); //  да записва веднага - бутона "за резолюция" се показва само за вече записани документи
			String  cmdStr = "PF('modalRezol').hide();";
			PrimeFaces.current().executeScript(cmdStr);
			
			docProcessed(Objects.equals(document.getProcessed(), DocuConstants.CODE_ZNACHENIE_DA)); 
		} else {	
			JSFUtils.addMessage(DOCFORMTABS+":lstIzpR:autoComplM",FacesMessage.SEVERITY_ERROR,getMessageResourceString(UI_beanMessages,MSGPLSINS, getMessageResourceString(LABELS, "task.rezolExec")));						
		}
	}
	
	/**
	 * "За резолюция" - запис на индивидуални задачи от тип "резолюция" - без срок
	 */
	public void  saveRezol() {		

		rezolTask.setAssignDate(new Date());
		rezolTask.setStatusDate(new Date());
		rezolTask.setStatus(DocuConstants.CODE_ZNACHENIE_TASK_STATUS_NEIZP); // неизпълнена 
		rezolTask.setDocId(this.document.getId());
		rezolTask.setRegistraturaId(ud.getRegistratura());
		rezolTask.setTaskType(DocuConstants.CODE_ZNACHENIE_TASK_TYPE_REZOL); // за резолюция code-1
		rezolTask.setDocRequired(DocuConstants.CODE_ZNACHENIE_NE); // изисква ли се документ при приключване на задачата
		rezolTask.setCodeAssign(ud.getUserSave());  //  за възложител по подразбиране да е текущия потребител или този делегирани права!!!
		
	    try {
			JPA.getUtil().runInTransaction(() -> rezolTask = new TaskDAO(getUserData()).save(rezolTask, true, document, sd));
					
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, getMessageResourceString(beanMessages, "docu.msgSaveRezol") );
			
			rezolTask = new Task();	
			rezolExecClassif = new ArrayList<>();		
		} catch (BaseException e) {			
			LOGGER.error("Грешка при запис на резолюция (задача)! ", e);			
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,  getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
		}	
			
	}
	
	
	
	
	
	/**
	 * Шаблони за вида документ 
	 */
	public void actionDocTemplate() {
		if(docSettingId != null &&  templatesList == null) {
			try {
				JPA.getUtil().runWithClose(() -> {
					FilesDAO daoF = new FilesDAO(getUserData());		
					templatesList = daoF.selectByFileObject(docSettingId, DocuConstants.CODE_ZNACHENIE_JOURNAL_DOC_VID_SETT); 
				 
				});
			} catch (BaseException e) {
				LOGGER.error("Грешка при зареждане на шаблони на документи ", e);
				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
			}
		}
		
		if (templatesList != null) {
			if(templatesList.size() > 1) {
				String  cmdStr = "PF('modalDocTml').show();";
				PrimeFaces.current().executeScript(cmdStr);
			}else if( templatesList.size() == 1) {
				//ако само един файл - да не отваря модалния				
				loadFilesFromTemplate(templatesList.get(0));
			}
		}
	}
	
	/**
	 * извлича шаблона и директно го записва в документа
	 * @param ftmpl
	 */
	public void loadFilesFromTemplate(Files ftmpl) {	
		Files newFile = new Files();
		FilesDAO dao = new FilesDAO(getUserData());	
		try {		
			ftmpl = dao.findById(ftmpl.getId());	
			if(ftmpl != null) { 
				if( ftmpl.getContent() == null){					
					ftmpl.setContent(new byte[0]);
				}
				
				newFile.setFilename(ftmpl.getFilename());
				newFile.setContentType(ftmpl.getContentType());
				newFile.setContent(ftmpl.getContent());
				newFile.setOfficial(DocuConstants.CODE_ZNACHENIE_DA);
				filesList.add(newFile);
				
				if(document.getId() != null) {
					JPA.getUtil().runInTransaction(() -> dao.saveFileObject(newFile, document.getId(), DocuConstants.CODE_ZNACHENIE_JOURNAL_DOC));
					JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, getMessageResourceString(UI_beanMessages, "general.succesSaveFileMsg") );
					actionChangeFiles();
				}
			}
		} catch (BaseException e) {
			LOGGER.error("Грешка при извличане на шаблон на документ! ", e);
		} finally {
			JPA.getUtil().closeConnection();
		}
	
	}
	
	
	/**
	 * "За запознаване" - изричен достъп + нотификация
	 */
	public void actionDocAccess() {
		try {
			if (document.getDocAccess()==null || document.getDocAccess().isEmpty()) {
				JPA.getUtil().runWithClose(() -> this.dao.loadDocAccess(document));
			}
			((CompAccess)FacesContext.getCurrentInstance().getViewRoot().findComponent(DOCFORMTABS).findComponent("docAccessComp")).initAutoCompl();
		
		} catch (BaseException e) {
			JSFUtils.addErrorMessage(getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e);
			LOGGER.error(e.getMessage(), e);
		}
		String  cmdStr = "PF('modalAccess').show();";
		PrimeFaces.current().executeScript(cmdStr);
	}

	/**
	 * Изричен достъп - справка за достъп
	 */
	public void actionFillDocAccessList() {

		try {
			
			SelectMetadata sm = dao.createSelectDocAccessList(document.getId(), sd);
			docAccessList = new LazyDataModelSQL2Array(sm, "A2, A1");
		} catch (DbErrorException e) {
			JSFUtils.addErrorMessage(getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e);
			LOGGER.error(e.getMessage(), e);
		}

		String  cmdStr = "PF('modalAccessSpr').show();";
		PrimeFaces.current().executeScript(cmdStr);
	}
	
   /** "Изричен достъп" - запис на целия документ 
    * 
    */
   public void actionConfirmAccess() {
	    int flagOk = 2;
	    // указания при насочване на документ	
		boolean noteNotEmpty = !SearchUtils.isEmpty(noteAccess); //&&	document.getDocAccess()!=null && !document.getDocAccess().isEmpty() ;
		for(DocAccess item: document.getDocAccess() ) {
			if(item.getId() == null && noteNotEmpty) { // само нови да добави указанията
				item.setNote(noteAccess);
				flagOk = 0;
			}else if(item.getId() == null && !noteNotEmpty) {
				flagOk = 1;
				break;
			}
		}

	    if(flagOk == 1){
	    	JSFUtils.addMessage(DOCFORMTABS+":noteAcc",FacesMessage.SEVERITY_ERROR,getMessageResourceString(UI_beanMessages,MSGPLSINS, getMessageResourceString(LABELS, "docu.msgZaZapoznavane") ));
	    }else if( flagOk == 2 && noteNotEmpty)	{
	    	// има указания, няма лица.... docAccessComp:tblDeloList_head
	    	JSFUtils.addMessage(DOCFORMTABS+":docAccessComp:tblDeloList",FacesMessage.SEVERITY_ERROR,getMessageResourceString(UI_beanMessages,MSGPLSINS, getMessageResourceString(LABELS, "docu.msgZaZapoznavane2") ));
		}else if(flagOk == 0 || (flagOk == 2 && !noteNotEmpty)) { 
			String  cmdStr = "PF('modalAccess').hide();";
			PrimeFaces.current().executeScript(cmdStr);
			actionSave(); // да запиша целия документ - ако има нови или изтрити - заради достъпа!!
			noteAccess = null;
		}
   }
	   
	
   /**При промяна на типа документ
    * @return
    */
   public void actionChangeDocType(ValueChangeEvent event) {
	   Integer newTypeDoc = (Integer)event.getNewValue();
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("actionChangeDocType= {}", newTypeDoc);
		}
	    nastrDocType(newTypeDoc);
	    actionChangeDocVid(true);	    	    
   }

   
   /**Изключва автоматичното генериране на номера
    * 
    */
   public void actionChangeAvtomNo() {
	   if(this.avtomNo) {
		   this.document.setRnDoc(null); 
	   }	
 	   // да се махне и връзката с преписката!!!
	   clearDeloDocLink();
   }
   
  
   /**
    * ДА / НЕ -  потвърждание за смяна на състоянието "Обработен / Необработен"
    * @param bProcessed - новото състояние на бутона
    * @param f - 1- "Да"
    */
   public void actionConfirmProcessed(boolean bProcessed, int f) {
	   if(f==DocuConstants.CODE_ZNACHENIE_DA && document.getDocType().equals(DocuConstants.CODE_ZNACHENIE_DOC_TYPE_WRK)) { 
		   //ако потвърдят - да док. да стане "Не е готов за регистрация"
		   //ако е маркирано "Готов за регистрация", документа винаги е "Необработен"
		   //обработен става след регистрирането на работния като официален
		   this.document.setForRegId(null);
		   this.readyForOfficial = false;
	   } else if (f==DocuConstants.CODE_ZNACHENIE_NE){ // ne
		   docProcessed(!bProcessed);
	   }
	   
   }
   
   /**
    * Обработен/не обработен
    * @param bProcessed
    */
   private void docProcessed( boolean bProcessed) {
	   if(bProcessed) {
		   processedCh = true;
	   }else {
		   processedCh = false;
	   }
   }

   /**
    * Компетентност
    */
   private void docCompetence() {
	   boolean bCompetence = document.getCompetence() == null || Objects.equals(document.getCompetence(), DocuConstants.CODE_ZNACHENIE_COMPETENCE_OUR); // наша комп. или ако е null
	   if(bCompetence) {
		   competence = false;
	   }else {
		   competence = true; // за изпращане
	   }
   }
   
  /**
   * Премахва отлагането на документа в преписката - при ръчно изписване на номера на документа
   */
   public void clearDeloDocLink() {
	  selectedDelo = null;
	  deloDocPrep = new DeloDoc();
	  deloDocPrep.setDelo(new Delo());
   }
   
	/**
	* Ръчно въвеждане на номер на документ
    * Търсене на преписка с въведения номер - при излизане от полето
     * @param rnEQ  - true- пълно съвпадение на номера
	 */
   public void actionSearchRnDelo(boolean rnEQ) {
	   clearDeloDocLink();	 
	   if(!SearchUtils.isEmpty(document.getRnDoc()) || !rnEQ) {
		   selectedDelo =  searchRnDelo( document.getRnDoc(),  "mDeloS",  rnEQ, new Date());
	   }
	}
   
   /**
    * Търси преписка по номер - ръчно въвеждане на номера - бутон "Търси"
    */
   public void actionSearchRnDeloBtn() {
	   if(selectedDelo == null) { // ако е намерена вече преписка да не се пуска пак търсенето
		   actionSearchRnDelo(false);
	    }
   }
   /**
    * Търсене на преписка по номер
    * @param rnDelo
    * @param varModal
    * @param rnEQ
    * @param nastr
    * @param inpDate
    * @return
    */
   private Object[] searchRnDelo(String rnDelo, String varModal, boolean rnEQ, Date inpDate) {
	   Object[] sDelo = null;
	   rnDelo  =  SearchUtils.trimToNULL_Upper(rnDelo);
       DeloSearch  tmp = new DeloSearch(document.getRegistraturaId());
       tmp.setUseDost(false); // да не се ограничава достъпа!! За сега
	   tmp.setRnDelo(rnDelo);
	   tmp.setRnDeloEQ(rnEQ);
	   tmp.buildQueryComp(getUserData());
	
	   LazyDataModelSQL2Array lazy =   new LazyDataModelSQL2Array(tmp, "a1 desc");
	   if(lazy.getRowCount() == 0 && rnEQ) {
		  
		   clearDeloDocLink(); 
		   //	LOGGER.debug("Не е намерена преписка с посочения номер!");
		   
	   } else if(lazy.getRowCount() == 1 && rnEQ) { // само при пълно съвпадение на номера
		   
		   List<Object[]> result = lazy.load(0, lazy.getRowCount(), null, null);
		   sDelo = new Object[8];
		   if(result != null) {
			    sDelo =  result.get(0);		
			   	loadDataFromDeloS(sDelo, inpDate); // документ - в преписка
			   
		   }	
		   
			LOGGER.debug("Намерена е само една преписка с този рег. номер - данните да се заредят без да излиза модалния");
		   
	   } else {
		   sDelo = new Object[8];		
		   String  cmdStr = "PF('"+varModal+"').show();";
		   PrimeFaces.current().executeScript(cmdStr);
	   }
	  return sDelo;		  
   }
   
   
   /**
    * Зарежда данните за избраната преписка 
    * nastr = true - ръчно въвеждане на номера на документа
    * 0]-DELO_ID<br>
	* [1]-RN_DELO<br>
	* [2]-DELO_DATE<br>
	* [3]-STATUS<br>
	* [4]-DELO_NAME<br>
	* [5]-INIT_DOC_ID<br>
	* [6]-REGISTER_ID<br>
    */
   private void loadDataFromDeloS(Object[] sDelo, Date inpDate) {	  
	    deloDocPrep = new DeloDoc();
	    deloDocPrep.setDelo(new Delo());	    
		if(sDelo[0] != null) {
			deloDocPrep.setDeloId(Integer.valueOf(sDelo[0].toString())); 
		}
		
		Date datd = (Date)sDelo[2];
		if(datd != null ){
			deloDocPrep.getDelo().setDeloDate(datd);
		}
		
		if(inpDate == null) {
			inpDate = new Date();
		}
		deloDocPrep.setInputDate(inpDate);
		deloDocPrep.getDelo().setStatus(Integer.valueOf(sDelo[3].toString()));
		
		String tmpstr = (String)sDelo[1];
		deloDocPrep.getDelo().setRnDelo(tmpstr);	
		

		deloDocPrep.setTomNomer(1);  // по подразбиране
		deloDocPrep.setEkzNomer(1);  //по подразбиране- 1-ви екз. ; раздела се зарежда при записа - за входящи - офицаилен, за собств. и раб. - вътршен
		
	
		// извиква се през полета за ръчно въвеждане на номер на документ
		 document.setRnDoc(tmpstr);
		// иницииращ документ- регистър 	
		
		if(sDelo[6] != null ){
			Integer initDocReg = Integer.valueOf(sDelo[6].toString());
			if( getUserData().hasAccess(DocuConstants.CODE_CLASSIF_REGISTRI, initDocReg)) {
				//само, ако има право да въвежда в този регистър
				document.setRegisterId(initDocReg); // винаги сменям регисъра, ако е върнат....
				actionChangeRegister();// да извлека настройките на регистъра
			}
		}	
		
		// "При влагане на нов документ в преписка, в него да се копира „относно“ на последния документ от преписката" - nastrojka???
		//  Как ще се използва??? ТODO
   	    //  относно - наименование на преписката  
		tmpstr = (String)sDelo[4];
		if(!SearchUtils.isEmpty(tmpstr)  ){
		   if (!SearchUtils.isEmpty(document.getOtnosno())) {
			   tmpstr += "\n"+ document.getOtnosno();
		   }
		   document.setOtnosno(tmpstr);
		}
		
		try {
			String msg =  deloDocPrep.getDelo().getRnDelo() +" / "+ DateUtils.printDate(deloDocPrep.getDelo().getDeloDate()) +
							"; "+  getSystemData().decodeItem(DocuConstants.CODE_CLASSIF_DELO_STATUS, deloDocPrep.getDelo().getStatus(), getUserData().getCurrentLang(), new Date());
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, getMessageResourceString(beanMessages, "docu.addDeloMsg1", msg) );
		} catch (DbErrorException e) {
			LOGGER.error("Грешка при разкодиране на статус на преписка ! ", e);
		}
		
		// ако е от минала година???? Съобщение? -   настройка на регистратура
	}
   
   
	/**
	 * Затваряне на модалния за избор на преписка - ръчно въвеждане на номера
	 */
   public void actionHideModalDelo() {
	   if(selectedDelo != null && selectedDelo[0] != null) {
		   // да заредя полетата
		   loadDataFromDeloS(selectedDelo,  null); // ръчно въвеждане на рег. номер
		
	   } else {
		   selectedDelo = null;
	   }
   }
   
   
  
   /**
    * Извлича настройките по вид документ
    * @param flagCh = true - при промяна на вида документ; false = зареждане за актуализация
    */
   public void actionChangeDocVid(boolean flagCh) {	
	   // За нов документ  - Да променям ли регистъра, ако предварително е избран - да!
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(" actionChangeDocVid= {}", document.getDocVid());
		}
	    createPrep = false; 
	    docSettingId = null;
	    templatesList = null;
		try {						
		   Integer oldDocSettingHar = docSettingHar; 
		   docSettingHar = null;
		   membersTabForDel = membersTab != null ? true : false; // true - имало е таб "Участници" преди смяната на вида документ
		   membersTab = null;
		   Object[] docSettings = dao.findDocSettings(document.getRegistraturaId(),document.getDocVid(),getSystemData());
		   if(docSettings != null) {
 			    // само за нов док.
			    docSettingsForNewDoc(docSettings);
			    
				// регистратура по подразб. за рег. на офц док. от работен 
				if(docSettings[3] != null && 
				   flagCh && Objects.equals(document.getDocType(), DocuConstants.CODE_ZNACHENIE_DOC_TYPE_WRK) && document.getWorkOffId() == null){
					//само, ако е работен и вече не е регистриран!! 
					this.document.setForRegId(null); // регистртура, в която да се регистрира официалния
					this.readyForOfficial = false;
					document.setPreForRegId(Integer.valueOf(docSettings[3].toString())); 
				}
				
				// ИД-то на настройката само ако има шаблони!
				if(docSettings[4] != null) { 
					docSettingId = Integer.valueOf(docSettings[4].toString());
				}

				// Характеристики на специализиран документ 
				docSettingsDopData(docSettings,  oldDocSettingHar,  flagCh);
			
				// Име на таба участници
				if(docSettings[9] != null) { 
					membersTab = docSettings[9].toString();
					membersTabForDel = false;
				}
		   }
		} catch (DbErrorException e) {
			LOGGER.error("Грешка при промяна на вид документ)! ", e);
		}	
   }
   
   /**
    * Настройки по вид док. - само, за нов докуменът
    * @param docSettings
    * @throws DbErrorException
    */
   private void  docSettingsForNewDoc(Object[] docSettings) throws DbErrorException{
		if(document.getId() == null) {  		
			if(docSettings[1] != null ) {
				// При актуализация да не се променя регистъра. Това ще става през дейност "Пререгистрация"
				Integer nReg = Integer.valueOf(docSettings[1].toString());
				loadRegNewDoc(nReg);					
			}				
			// зарежда процедура по подразбиране, ако има такава
			loadProcedure(docSettings);
			// да създава преписка 
			if( Objects.equals(docSettings[2], DocuConstants.CODE_ZNACHENIE_DA)) {
				createPrep = true;
			}
		}
   }

   /**
    *  Характеристики на специализиран документ 
    * @param docSettings
    * @param oldDocSettingHar
    * @param flagCh
    */
   private void docSettingsDopData(Object[] docSettings, Integer oldDocSettingHar, boolean flagCh) {
		// Характеристики на специализиран документ 
		if(docSettings[8] != null) {
			document.setDopdata(new DocDopdata());
			docSettingHar = Integer.valueOf(docSettings[8].toString());
			boolean bb  = flagCh ? Objects.equals(docSettingHar, oldDocSettingHar) : true;
			if(document.getId() != null && bb ) {
				try {
					JPA.getUtil().runWithClose(() -> {
						DocDopdata dopData = this.dao.findDocDopdata(document.getId());
						if(dopData != null) {
							document.setDopdata(dopData);
						}
					});
				} catch (BaseException e) {
					LOGGER.error("Грешка при извличане допълнителните данни за специализиран документ! ", e);
					JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
				}
			}
		}else {	//DocDopData - да се нулира, ако за вида документ не е указано, че е специализиран!
			document.setDopdata(null);
		}
   }
   
   /**
    * при смяна на вид документ 
    * само за нов документ. При актуализация да не се променя регистъра. Това ще става през дейност "Пререгистрация"
    * @param nReg
    * @throws DbErrorException
    */
   private void loadRegNewDoc(Integer nReg) throws DbErrorException {
		if( getUserData().hasAccess(DocuConstants.CODE_CLASSIF_REGISTRI, nReg)) {
			// само, ако има право да въвежда в този регистър
			Integer docTypeReg  = (Integer) getSystemData().getItemSpecific(DocuConstants.CODE_CLASSIF_REGISTRI, nReg, getUserData().getCurrentLang(), new Date(), DocuClassifAdapter.REGISTRI_INDEX_DOC_TYPE);
			if((docTypeReg == null && 
					!Objects.equals(document.getDocType(),DocuConstants.CODE_ZNACHENIE_DOC_TYPE_WRK)) 
					|| Objects.equals(docTypeReg, document.getDocType())) { // само, ако регистъра е за вх. и собствени (БЕЗ работните) или за избрания тип документ
					document.setRegisterId(nReg);//REGISTER_ID
			}
		}
   }
   
 /**
  * Зарежда процедура по подразбиране, ако има такава за вида документ
  * @param docSettings
  */
   private void loadProcedure(Object[] docSettings) {
	   if(docSettings[5]!=null && document.getDocType().equals(DocuConstants.CODE_ZNACHENIE_DOC_TYPE_IN)){
		   document.setProcDef(Integer.valueOf(docSettings[5].toString()));
	   }else if(docSettings[6]!=null && document.getDocType().equals(DocuConstants.CODE_ZNACHENIE_DOC_TYPE_OWN)){
		   document.setProcDef(Integer.valueOf(docSettings[6].toString()));
	   }else if(docSettings[7]!=null && document.getDocType().equals(DocuConstants.CODE_ZNACHENIE_DOC_TYPE_WRK)){
		   document.setProcDef(Integer.valueOf(docSettings[7].toString()));
	   }else {
		   document.setProcDef(null);
	   }
   }
   
   /**
    * работен документ - при смяна на "Готов за регистриране като официален"
    */
   public void actionChangeReadyFO() { 
	   if(this.readyForOfficial) {
		   try {	
				if(this.document.getPreForRegId() == null) { //тази проверка е само да се презастраховам, не би трявало да влиза тук, ако всичко е ОК 
				   // няма избрана предварително рег. - проверяваме настройките
				   // настройки по вид документ  - ако върне регистратура за регистрация - вземам нея
				   Object[] docSettings = dao.findDocSettings(document.getRegistraturaId(),document.getDocVid(),getSystemData());
				   if(docSettings != null && docSettings[3] != null) {
					  this.document.setPreForRegId(Integer.valueOf(docSettings[3].toString()));				
				   }  else {
					  this.document.setPreForRegId(getUserData(UserData.class).getRegistratura()); // текуща регистртурата);   
				   }
				} 			
               	// ако  полето ForRegId != null => док. е готов за рег. като официален 
               	this.document.setForRegId(this.document.getPreForRegId());
 			    this.processedCh = false; //раб. документ да се маркира като "Необработен"
		   } catch (DbErrorException e) {
				LOGGER.error("Грешка при извличане на настройки по вид документ)! ", e);
		   } 
			 
	   } else {
		   // Не е готов;
		   this.document.setForRegId(null);   
	   }	 
   }


   /**При промяна на регистъра 
    * 
    */
   public void actionChangeRegister() {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(" actionChangeRegister= {}", document.getRegisterId());
		}
		Integer alg = null;
		avtomNoDisabled =  false;
		try {
			 if(document.getRegisterId() != null) {
				 alg  = (Integer) getSystemData().getItemSpecific(DocuConstants.CODE_CLASSIF_REGISTRI, document.getRegisterId(), getUserData().getCurrentLang(), new Date(), DocuClassifAdapter.REGISTRI_INDEX_ALG);
			 }
			 if(alg != null && alg.equals(DocuConstants.CODE_ZNACHENIE_ALG_FREE)) {
				 this.avtomNo = false; // да се забрани автом. генер. на номера! Да се прави проверка за въведен номер, ако алгоритъмът е "произволен рег.номер"
				 avtomNoDisabled =  true;
			 }else if(SearchUtils.isEmpty(document.getRnDoc())){
				 this.avtomNo = true; // да се промени според регистъра, само ако вече няма нищо въведено в полето за номер на документ 
			 }
		} catch (DbErrorException e) {
			LOGGER.error("Грешка при промяна на регистъра на деловоден документ )! ", e);
		}						

   }
 
   /**При промяна на файловете
    * обновяване на броя файловете - само за стар документ
    * При добавяне или изтриване на файл - нотификация
    */
   @SuppressWarnings("unchecked")
   public void actionChangeFiles() {
	    if(document.getId() != null) {  
			try {
				JPA.getUtil().runInTransaction(() -> { 
					Integer countFiles = (filesList == null ) ? 0 : filesList.size(); 
					dao.updateCountFiles(document,  countFiles);
					
					try {
						Query q = JPA.getUtil().getEntityManager().createNativeQuery("select CODE_REF from DOC_ACCESS_ALL where doc_id = :IDD"); //TODO - da se premesti ot tuk...
						q.setParameter("IDD", document.getId());
						
						ArrayList<Object> all = (ArrayList<Object>) q.getResultList();
						ArrayList<Integer> allI = new ArrayList<> ();
						for (Object obj : all) {
							allI.add(SearchUtils.asInteger(obj));
						}
						
						Notification notif = new Notification(((UserData)getUserData()).getUserAccess(), null
								, DocuConstants.CODE_ZNACHENIE_NOTIFF_EVENTS_FILE_CHANGE, DocuConstants.CODE_ZNACHENIE_NOTIF_ROLIA_ALL_DOST, getSd());
							notif.setDoc(document);
							notif.setAdresati(allI);
							notif.send();
					} catch (Exception e) {
						LOGGER.error("Грешка при изпращане на нотификация за променено файлово съдържание");
						throw new DbErrorException("Грешка при изпращане на нотификация за променено файлово съдържание", e);
					}
					
				});
			} catch (BaseException e) {
				LOGGER.error("Грешка при обновяване броя на файлове в документа! ", e);			
				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,  getMessageResourceString(UI_beanMessages, ERRDATABASEMSG),e.getMessage());
			} 
	    }
   }
   
   private Integer reloadFile;
   
   public Integer getReloadFile() {
		return reloadFile;
	}
   
   
   //метода се извиква от компонентата за сканиране след успешно извършен запис на файл
   public void setReloadFile(Integer reloadFile) {
		
	//	System.out.println("setReloadFile ->"+reloadFile);
		
		if(reloadFile!=null) {
			reloadDocDataFile(); 
			actionChangeFiles();
		}
		
		this.reloadFile = reloadFile;
	}
   
   
   /**
    * Избор на задача от списъка - ид на зад. да се подаде на комп. taskData
    */
    public void actionSelectTask(Object[] row) {	  	 
		idTask = Integer.valueOf(row[0].toString());	    
    }
  


	/**
	 * търсене по техен номер - бутон 
	 */
	public void actionSearchDocBtnTn() {
		if(SearchUtils.isEmpty(this.document.getTehNomer())) {		
			actionCancelRelTn(true);
		} else if(searchFlagTn == 0){
			searchTehNomer(this.document.getTehNomer());
		}else {
			searchFlagTn = 0;
		}
	}
	
	
	/**
	 * Търсене по по техен номер
	 * извиква се
	 * 1. при въвеждане / промяна на техен номер
	 * 2. при първоначално зареждане на входящ док., ако има нещо в полето техен номер	 * 
	 * @param tehNomer
	 * @param tehDate
	 * @param coresp
	 */
	private void searchTehNomerSql(String tehNomer, Date tehDate, Integer coresp) {
		
		if (tehNomer == null) { 
			tehNomer = "";
		}
		
		DocSearch tmp = new DocSearch(getUserData(UserData.class).getRegistratura());
		tmp.setTehNomer(tehNomer);
		tmp.setDocDateTo(tehDate);
		tmp.setDocDateFrom(tehDate);
		tmp.setCodeRefCorresp(coresp);
		tmp.setTehNomerEQ(true);
		tmp.setMarkRelDocId(this.document.getId()); //Ако има вече създадена връзка - само да са маркирани. Не искам да се скриват, за да се видят всички док. които имат същия техен номер.
		// Но трябва да се изключи подадения!!! 
		//tmp.setUseDost(true);
		
		tmp.buildQueryComp(getUserData());
		LazyDataModelSQL2Array lazy = new LazyDataModelSQL2Array(tmp, "a1 desc");
		this.tnRez =  lazy.getRowCount(); // бутонът с лупата ще се виждам само, ако има намеренео нещо
		
	}
	
	/**
	 * Търсене по техен номер
	 */
	private Object[] searchTehNomer(String tehNomer) {
		
		Object[] sDoc = null;
		searchTehNomerSql(tehNomer, null, null);
		if (this.tnRez == 0 ) {			
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Не е намерен документ с посочения номер!");
			}
			JSFUtils.addMessage(DOCFORMTABS+":btnDocIn",FacesMessage.SEVERITY_INFO,	getMessageResourceString(LABELS, "docu.tehNMsgNotFound"));
			
		} else  {	
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Намерени са  документ! Дори да е един - да се отвори таблицата");
			}
			sDoc = new Object[5];
			String dialogWidgetVar = "PF('docTehNVar').show();";
			PrimeFaces.current().executeScript(dialogWidgetVar);
			searchFlagTn++;
		}		
		return sDoc;
	}
	
	/**
	 * премахва все още незаписани връзки 
	 */
	public void actionCancelRelTn(boolean clrRez) {
		selectedDocsTn.clear();	
		searchFlagTn=0;
		if(clrRez) {
			tnRez = 0;
		}
	}	
	

   /**
    * Рефрешва списъка със задачи при запис и изтриване от компонентата
    */
    public void actionRefreshTaskList() {
	   getTasksList().loadCountSql();
    }


	/**
	 *  запис на връзките - от техен номер
	 */
     public void saveVrazkiTn() {

		boolean saveVr = this.selectedDocsTn != null && !this.selectedDocsTn.isEmpty();
		try {
			if (saveVr) {
				JPA.getUtil().runInTransaction(() -> { 
					// запис на връзките - от техен номер
					DocDocDAO vrazkiDAO = new DocDocDAO(getUserData());
					Integer idObj = null;
					for (Object[] obj : this.selectedDocsTn) {
						idObj = SearchUtils.asInteger(obj[0]);
						vrazkiDAO.save(null, DocuConstants.CODE_ZNACHENIE_DOC_REL_TYPE_VRAZKA, this.document.getId(), idObj);
					}
					selectedDocsTn.clear();
				});
		
				//Задава параметър за рефрешване на списъка със свързани документи при запис на връзки по 'техен номер'
				DocDataVrazki docDataVrBean = (DocDataVrazki) JSFUtils.getManagedBean("docDataVrazki");
		 		if(	docDataVrBean != null) {
		 			docDataVrBean.setRefreshList(true);
		 		}
				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, getMessageResourceString(beanMessages, "docu.SuccesMsgVrTn") );
			}
		} catch (BaseException e) {
			LOGGER.error("Грешка при запис на връзки по техен номер! ", e);				
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,  getMessageResourceString(UI_beanMessages, ERRDATABASEMSG),e.getMessage());
		}			
		
     }
     
     
	/**
	 * търсене по техен номер - при скриване на модалния
	 */
	public void onHideModalTehN() {
		searchFlagTn = 0;
	}

	/**
	 * отказ от регистрация
	 */
	public void actionReject() {
		boolean rejectOk = false;
		if (flagFW == REG_OTHER_R ) {
			rejectOk = rejectOtherR(); //при рег. от други регистратури 
		} else if (flagFW == REG_FROM_MAIL ) {
			rejectOk = rejectFromMail(); //при регистриране от e-mail
		}else if (flagFW == REG_FROM_EGOV ) {
			rejectOk = rejectFromEGOV(); //при регистриране от СЕОС или ССЕВ
		}
		if(rejectOk) {
			Navigation navHolder = new Navigation();
			navHolder.goBack();   //връща към предходната страница
		}
	}
	
	/**
	 * Отказ от регистрация  - друга регистратура
	 */
	private boolean rejectOtherR() {
		boolean rejectOk = false;
		if( SearchUtils.isEmpty(textReject)) {
			JSFUtils.addMessage(TXTREJECTEDID,FacesMessage.SEVERITY_ERROR,getMessageResourceString(UI_beanMessages,MSGPLSINS,getMessageResourceString(LABELS, TXTREJECTED)));
		} else {
			// save reject
		  try {
				JPA.getUtil().runInTransaction(() -> this.dao.rejectDocAcceptance(this.sourceRegDvijId, textReject, ud.getRegistratura(), sd));
						
			//	JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, getMessageResourceString(UI_beanMessages, SUCCESSAVEMSG) );
				rejectOk = true;
		  } catch (BaseException e) {			
				LOGGER.error("Грешка при отказ от регистрация на документ! ", e);			
				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,  getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
		  }	
					
		}
		return rejectOk;
	}
	
	/**
	 * Отказ от регистрация - рег. на док. от е-маил
	 */
	private boolean rejectFromMail() {
		boolean rejectOk = false;
		if( SearchUtils.isEmpty(textReject)) {
			JSFUtils.addMessage(TXTREJECTEDID,FacesMessage.SEVERITY_ERROR,getMessageResourceString(UI_beanMessages,MSGPLSINS,getMessageResourceString(LABELS, TXTREJECTED)));
		} else {
			createReturnMail(false);
			rejectOk = true;
		}	
		return rejectOk;
	}
	
	/**
	 * Формира и изпраща е-мейл за отговор
	 * Премества мейлите в папка "Inbox.registered" или  в "Inbox.rejected"
	 * @param registred - true : регистриран, false - отказ
	 */
	private void createReturnMail(boolean registred) {
		Mailer mm = new Mailer();
		try {
			String subject = "FW:"+ (messSubject!=null?messSubject:"") ;
			StringBuilder body = new StringBuilder();
			if(registred) {
				String bodyPlainText = mailBodyTextPlain();
				body.append(bodyPlainText);
			}else {
				body.append(" " +getSystemData().decodeItemDopInfo(DocuConstants.CODE_CLASSIF_REGISTRATURI,ud.getRegistratura(), ud.getCurrentLang(), new Date())); // име на организация
				body.append(" \n  Отказ от регистрация ");
				body.append(" \n  Дата на отказ: " + DateUtils.printDate(new Date()));
				body.append(" \n  Причина за отказ: " + textReject);
			}
			mm.forward(this.propMail, this.messUID, this.propMail.getProperty("mail.folder.read", getSystemData().getSettingsValue("mail.folder.read")), subject, body.toString());
			//mm.sendHTMLMail(propMail, messFromRef, subject, body.toString(), null);
			
			if(registred) {
				mm.moveMailUIDRegistred(this.propMail, this.messUID, sd);
			}else {
				mm.moveMailUIDRejected(this.propMail, this.messUID, sd);	
			}
						
		} catch (DbErrorException | MessagingException e) {
			LOGGER.error("Грешка при формиране на е-мейл и връщане на е-мейл към подателя! ", e); 
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,getMessageResourceString(UI_beanMessages, OBJECTINUSE), e.getMessage());
		} 
	}
	
	/**
	 * Формира текста на съобщението, което се връща по е-майл или чрез еВръчване (ССЕВ), при успешно регистриран документ
	 * @return
	 * @throws DbErrorException 
	 */
	private String mailBodyTextPlain() throws DbErrorException {
		StringBuilder tmp= new StringBuilder();
		String orgName = sd.decodeItemDopInfo(DocuConstants.CODE_CLASSIF_REGISTRATURI, document.getRegistraturaId(), getCurrentLang(), new Date());
		String regN = this.document.getRnDoc() + "/" + DateUtils.printDate(this.document.getDocDate());
		String param = sd.getSettingsValue("emailReplayMsg");
		if(SearchUtils.isEmpty(param)) {
			// формира текст по подразбиране, ако липсва настройка
			tmp.append(orgName);
			tmp.append("\n  Вашият документ е регистриран успешно! ");
			tmp.append("\n  Уникален регистрационен № на документа/материала  ");
			tmp.append(regN);
		}else {
			param = param.replace("<br/>","\n");
			param = param.replace("$regnom$", regN+" ");
			param = param.replace("$admSlujba$", orgName +" ");
			param = param.replace("$anot$", document.getOtnosno()+" ");
			String tmplat = "";
			if(param.contains("$transliterate$")) {
				param = param.replace("$transliterate$", "");
				tmplat = com.ib.system.utils.StringUtils.transliterate(param);			
			}
			tmp.append(param);
			tmp.append(tmplat);
			
//		<br/>Вашият документ е регистриран успешно!<br/> $admSlujba$<br/> Уникален регистрационен № на документа/материала $regnom$<br/> Относно: $anot$ <br/><br/> $transliterate$
		}		
		return tmp.toString();
	}
	
	
	
	

	/**
	 * Отказ от регистрация - рег. на док. от СЕОС или ССЕВ
	 */
	private boolean rejectFromEGOV() {
		boolean rejectOk = false;
		if( SearchUtils.isEmpty(textReject)) {
			JSFUtils.addMessage(DOCFORMTABS+":txtReject",FacesMessage.SEVERITY_ERROR,getMessageResourceString(UI_beanMessages,MSGPLSINS,getMessageResourceString(LABELS, TXTREJECTED)));
		} else {			
			try {
				JPA.getUtil().runInTransaction(() -> 
					//  да се направи update на съобщението в table EGOV_MESSAGES
					// Да се върне съобщение за отказ
					createReturnEGOV(false)			
				);
			} catch (BaseException e) {
				LOGGER.error("Грешка при формиране на отказ от регистрация - СЕОС или ССЕВ! ", e); 
			}
			rejectOk = true;
		}	
		return rejectOk;
	}
	

	/**
	 * update на статуса на съобщението в table EGOV_MESSAGES
	 * Формира и изпраща съобщение през СЕОС за отговор 
	 * @param registred - true : регистриран, false - отказ
	 */
	private void createReturnEGOV(boolean registred) {
		try {
			if(registred) {
				egovMess.setMsgRn(document.getRnDoc());
				egovMess.setMsgRnDate(document.getDocDate());
				egovMess.setMsgStatus(EgovStatusType.DS_REGISTERED.toString());		
				
			}else {			
				egovMess.setMsgRn(null);
				egovMess.setMsgRnDate(null);
				egovMess.setMsgStatus(EgovStatusType.DS_REJECTED.toString());
				egovMess.setPrichina(textReject);
			}
			egovMess.setMsgStatusDate(new Date());
			EgovMessagesDAO daoEgov = new EgovMessagesDAO(getUserData());
			// запис на промяната на статуса в таблица Egov_Messages
			egovMess = daoEgov.save(egovMess);		
			
			// Отговор 
			if(Objects.equals(S_SEOS, egovMess.getSource())) {
				createReturnMsgEGOV(registred, daoEgov);
			}else if(Objects.equals("S_EDELIVERY", egovMess.getSource())){ 
				createReturnMsgЕDelivery(daoEgov);
			}
		
		} catch (DbErrorException  e) {
			LOGGER.error("Грешка при формиране на отговор - egovMsg! ", e); 
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,getMessageResourceString(UI_beanMessages, OBJECTINUSE), e.getMessage());
		} 
	}
	
	/**
	 * формира съобщение отговор -  за СЕОС
	 * @param registred - true : регистриран, false - отказ
	 * @throws DbErrorException 
	 */
	private void createReturnMsgEGOV(boolean registred, EgovMessagesDAO daoEgov) throws DbErrorException {
		if(registred) { 
			daoEgov.saveStatusResponseRegisteredMessage(egovMess, document.getRnDoc(), document.getDocDate(), ud);			
		}else {
			daoEgov.saveStatusResponseOtkazMessage(egovMess, textReject, ud);			
		}
	}
	

	/**
	 * формира съобщение отговор за успешна регистрация -  за еDelivery (ССЕВ)
	 * @throws DbErrorException 
	 */
	private void createReturnMsgЕDelivery(EgovMessagesDAO daoEgov) throws DbErrorException {
		String bodyTextPlain = mailBodyTextPlain();
		String subject = getMessageResourceString(beanMessages, "docu.confSucRegDoc"); 		
		daoEgov.saveDeliverySuccessMess(egovMess, document.getRnDoc(), document.getDocDate(), document.getDocVid(), bodyTextPlain, subject, sd, ud);
	}
					
		
	
   /**
    * При смяна на таб
    */

    public void onTabChange(TabChangeEvent<?> event) {
	   	if(event != null) {
	   		if (LOGGER.isDebugEnabled()) {
	   			LOGGER.debug("onTabChange Active Tab: {}", event.getTab().getId());
	   		}
			rnFullDoc = DocDAO.formRnDocDate(this.document.getRnDoc(), this.document.getDocDate(), this.document.getPoredDelo());
			String activeTab =  event.getTab().getId();
			if(activeTab.equals("tabTasks")) {
				getSrokPattern();
				// списък задачи към документ
				TaskSearch tmpTs = new TaskSearch(document.getRegistraturaId()); 
				tmpTs.setDocId(document.getId());			
				tmpTs.buildQueryTasksInDoc();
				setTasksList(new LazyDataModelSQL2Array(tmpTs, "a1 asc"));
				
			} else if (activeTab.equals("tabMain")) {
				docProcessed(Objects.equals(document.getProcessed(), DocuConstants.CODE_ZNACHENIE_DA)); // ако се върне от задача или движения  и е променено
				docCompetence();
			} else if (activeTab.equals("tabDvig")) {
				countOfficialFiles();								
			}
			
			
			if(isView == 1) {
	   			viewMode();
	   		} else	if(!activeTab.equals("tabTasks") && idTask != null) {// && tasksList != null && tasksList.getRowCount() > 0) {
				idTask = null;
				unlockAll(false);//Да отключа задачите към документа 
			}
			
	   	}
   }
	
    /**
     * брой файлове, маркирани като официални
     */
   private void countOfficialFiles() {
	   countOfficalFiles = 0;
	   if ( filesList != null && !filesList.isEmpty()) {
			// да преброя колко са официални за изпращане
			for (Files f : filesList) {
				if(Objects.equals(f.getOfficial(), DocuConstants.CODE_ZNACHENIE_DA)) {
					countOfficalFiles  ++;
				}
			}
	   }
   }
    
   /**
    * Да се виждат ли часове и минути в срока на задачите 
    */
   private void loadSrokPattern() {
	   	// да се виждат ли часове и минути в срока на задачата
		// взема се настройкaт на регистратурата на потребителя - за сега, в списъка,  ще се определя само от текущата регистртура!
	   
		try {
			Integer s1 = ((SystemData) getSystemData()).getRegistraturaSetting(document.getRegistraturaId(), DocuConstants.CODE_ZNACHENIE_REISTRATURA_SETTINGS_15);
			if(Objects.equals(s1,  DocuConstants.CODE_ZNACHENIE_DA)) {
				setSrokPattern("dd.MM.yyyy HH:mm");
			}else {
				setSrokPattern("dd.MM.yyyy");
			}
		} catch (DbErrorException e) {
			LOGGER.error("Грешка при извичане на настройка CODE_ZNACHENIE_REISTRATURA_SETTINGS_15 на регистратура: {} ", document.getRegistraturaId()+" ! ", e);
		}
	  
   }
   
   /**
    * Режим - разглеждане на документ
    * @param activeTab
    */
   private void viewMode() {

	    String  cmdStr;  

		// 1. забранявам всички инпутполета
		cmdStr = "$(':input').attr('readonly','readonly')";
		PrimeFaces.current().executeScript(cmdStr);
		
   }
   
	/**
	 * подскзака в списъка със задачи - мнение при приключване + коментар
	 * @param comment
	 * @param opinion
	 * @return
	 */
	public String titleInfoTask(Integer opinion, String comment ) {
		StringBuilder title = new StringBuilder();
		if(opinion != null) {
			try {
				String opinionTxt=getSystemData().decodeItem(DocuConstants.CODE_CLASSIF_TASK_OPINION, opinion, getUserData().getCurrentLang(), new Date());
				title.append(getMessageResourceString(LABELS, "docu.modalRefMnenie")+": " + opinionTxt+ "; ");
			} catch (DbErrorException e) {
				LOGGER.error("Грешка при зареждане на данни за документ! ", e);
			}
		}
		if(!SearchUtils.isEmpty(comment)) {
			title.append(comment); //getMessageResourceString(LABELS, "tasks.comment")+": "
		}
		return title.toString();
	}
	
	
	public Doc getDocument() {		
		return document;
	}

	public void setDocument(Doc document) {
		this.document = document;
	}

	/** @return the docTypeList */
	public List<SelectItem> getDocTypeList() {
		return this.docTypeList;
	}

	public Date getDecodeDate() {
		return decodeDate;
	}

	public void setDecodeDate(Date decodeDate) {
		this.decodeDate = decodeDate;
	}

	public boolean isAvtomNo() {
		return avtomNo;
	}

	public void setAvtomNo(boolean avtomNo) {
		this.avtomNo = avtomNo;
	}


	public boolean isCreatePrep() {
		return createPrep;
	}

	public void setCreatePrep(boolean createPrep) {
		this.createPrep = createPrep;
	}


	public boolean isProcessedCh() {
		return processedCh;
	}

	public void setProcessedCh(boolean processedCh) {
		this.processedCh = processedCh;
	}

	

	
	public DocDAO getDao() {
		return dao;
	}

	public void setDao(DocDAO dao) {
		this.dao = dao;
	}
	

	public List<Files> getFilesList() {
		return filesList;
	}


	public void setFilesList(List<Files> filesList) {
		this.filesList = filesList;
	}




	public String getTxtCorresp() {
		return txtCorresp;
	}


	public void setTxtCorresp(String txtCorresp) {
		this.txtCorresp = txtCorresp;
	}


	public boolean isAvtomNoDisabled() {
		return avtomNoDisabled;
	}


	public void setAvtomNoDisabled(boolean avtomNoDisabled) {
		this.avtomNoDisabled = avtomNoDisabled;
	}


	public Map<Integer, Object> getSpecificsRegister() {
		return specificsRegister;
	}


	public void setSpecificsRegister(Map<Integer, Object> specificsRegister) {
		this.specificsRegister = specificsRegister;
	}


	public boolean isRegUserFiltered() {
		return regUserFiltered;
	}


	public void setRegUserFiltered(boolean regUserFiltered) {
		this.regUserFiltered = regUserFiltered;
	}


	public Object[] getSelectedDelo() {
		return selectedDelo;
	}


	public void setSelectedDelo(Object[] selectedDelo) {
		this.selectedDelo = selectedDelo;
	}


	public String getRnFullDoc() {
		return rnFullDoc;
	}


	public void setRnFullDoc(String rnFullDoc) {
		this.rnFullDoc = rnFullDoc;
	}

	
	public DeloDoc getDeloDocPrep() {
		return deloDocPrep;
	}


	public void setDeloDocPrep(DeloDoc deloDocPrep) {
		this.deloDocPrep = deloDocPrep;
	}




	public LazyDataModelSQL2Array getTasksList() {
		return tasksList;
	}


	public void setTasksList(LazyDataModelSQL2Array tasksList) {
		this.tasksList = tasksList;
	}


	public Integer getIdTask() {
		return idTask;
	}


	public void setIdTask(Integer idTask) {
		this.idTask = idTask;
	}


	public SystemData getSd() {
		return sd;
	}


	public void setSd(SystemData sd) {
		this.sd = sd;
	}


	public boolean isReadyForOfficial() {
		return readyForOfficial;
	}


	public void setReadyForOfficial(boolean readyForOfficial) {
		this.readyForOfficial = readyForOfficial;
	}

	
	public List<SelectItem> getDopRegistraturiList() {
		return dopRegistraturiList;
	}


	public void setDopRegistraturiList(List<SelectItem> dopRegistraturiList) {
		this.dopRegistraturiList = dopRegistraturiList;
	}



	public String getRnFullDocOther() {
		return rnFullDocOther;
	}

		
	public void setRnFullDocOther(String rnFullDocOther) {
		this.rnFullDocOther = rnFullDocOther;
	}


	public int getFlagFW() {
		return flagFW;
	}


	public void setFlagFW(int flagFW) {
		this.flagFW = flagFW;
	}


	public int getIsView() {
		return isView;
	}


	public void setIsView(int isView) {
		this.isView = isView;
	}


	public UserData getUd() {
		return ud;
	}


	public void setUd(UserData ud) {
		this.ud = ud;
	}


	public int getViewBtnProcessed() {
		return viewBtnProcessed;
	}


	public void setViewBtnProcessed(int viewBtnProcessed) {
		this.viewBtnProcessed = viewBtnProcessed;
	}

	public Map<Integer, Object> getSpecificsAdm() {
		if(specificsAdm == null) {
			Object[][] obj = {{DocuClassifAdapter.ADM_STRUCT_INDEX_REF_TYPE, X.of(DocuConstants.CODE_ZNACHENIE_REF_TYPE_EMPL)},};
			specificsAdm = Stream.of(obj).collect(Collectors.toMap(data -> (Integer) data[0], data ->  data[1]));  // X.of() -> така ще дава само служители през аутокомплете, а в дървото ще е цялата
		}
		return specificsAdm;
	}

	public void setSpecificsAdm(Map<Integer, Object> specificsAdm) {
		this.specificsAdm = specificsAdm;
	}
	
	public List<SystemClassif> getRezolExecClassif() {
		return rezolExecClassif;
	}


	public void setRezolExecClassif(List<SystemClassif> rezolExecClassif) {
		this.rezolExecClassif = rezolExecClassif;
	}



	public Task getRezolTask() {
		return rezolTask;
	}


	public void setRezolTask(Task rezolTask) {
		this.rezolTask = rezolTask;
	}


	public List<Object[]> getSelectedDocsTn() {
		return selectedDocsTn;
	}


	public void setSelectedDocsTn(List<Object[]> selectedDocsTn) {
		this.selectedDocsTn = selectedDocsTn;
	}





	public String getNoteAccess() {
		return noteAccess;
	}


	public void setNoteAccess(String noteAccess) {
		this.noteAccess = noteAccess;
	}


	public int getSearchFlagTn() {
		return searchFlagTn;
	}


	public void setSearchFlagTn(int searchFlagTn) {
		this.searchFlagTn = searchFlagTn;
	}


	public int getTnRez() {
		return tnRez;
	}


	public void setTnRez(int tnRez) {
		this.tnRez = tnRez;
	}


	public boolean isLimitedAccessCh() {
		return limitedAccessCh;
	}


	public void setLimitedAccessCh(boolean limitedAccessCh) {
		this.limitedAccessCh = limitedAccessCh;
	}


	public boolean isNastrWithEkz() {
		return nastrWithEkz;
	}


	public void setNastrWithEkz(boolean nastrWithEkz) {
		this.nastrWithEkz = nastrWithEkz;
	}

	public Integer getSourceRegDvijId() {
		return sourceRegDvijId;
	}


	public void setSourceRegDvijId(Integer sourceRegDvijId) {
		this.sourceRegDvijId = sourceRegDvijId;
	}


	public String getTextReject() {
		return textReject;
	}


	public void setTextReject(String textReject) {
		this.textReject = textReject;
	}


	public Properties getPropMail() {
		return propMail;
	}


	public void setPropMail(Properties propMail) {
		this.propMail = propMail;
	}


	public Long getMessUID() {
		return messUID;
	}


	public void setMessUID(Long messUID) {
		this.messUID = messUID;
	}


	public String getMessSubject() {
		return messSubject;
	}


	public void setMessSubject(String messSubject) {
		this.messSubject = messSubject;
	}


	public String getSelectMailBox() {
		return selectMailBox;
	}


	public void setSelectMailBox(String selectMailBox) {
		this.selectMailBox = selectMailBox;
	}


	public String getMessFromRef() {
		return messFromRef;
	}


	public void setMessFromRef(String messFromRef) {
		this.messFromRef = messFromRef;
	}


	public Date getCurrentDate() {
		return new Date();
	}


	public String getRnFullProtocol() {
		return rnFullProtocol;
	}


	public void setRnFullProtocol(String rnFullProtocol) {
		this.rnFullProtocol = rnFullProtocol;
	}


	public int getCountOfficalFiles() {
		return countOfficalFiles;
	}


	public void setCountOfficalFiles(int countOfficalFiles) {
		this.countOfficalFiles = countOfficalFiles;
	}


	public boolean isFromOtherReg() {
		return fromOtherReg;
	}


	public void setFromOtherReg(boolean fromOtherReg) {
		this.fromOtherReg = fromOtherReg;
	}

	public boolean isCompetence() {
		return competence;
	}

	public void setCompetence(boolean competence) {
		this.competence = competence;
	}

	
	public int getUpdateDoc() {
		return UPDATE_DOC; 
	}
	
	
	public  int getRegOtherR() {
		return  REG_OTHER_R; 
	}


	public  int getRegFromMail() {
		return REG_FROM_MAIL; 
	}
	
	public  int getRegFromEgov() {
		return REG_FROM_EGOV; 
	}
	
	public void reloadDocDataFile() {
		
		if(document!=null && document.getId()!=null) {
			try {
				JPA.getUtil().runWithClose(() -> {
					if(this.dao.hasDocAccess(document, regUserFiltered, getSystemData())) {	//проверка за достъп до документа
						loadFilesList(document.getId(), UPDATE_DOC); // 	load files
					} 
				});
			} catch (BaseException e) {
				LOGGER.error("Грешка при зареждане на файлове след подписване! ", e);
				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
			}
		}
		
	}

	public boolean isCreatePrepOld() {
		return createPrepOld;
	}

	public void setCreatePrepOld(boolean createPrepOld) {
		this.createPrepOld = createPrepOld;
	}

	public boolean isScanModuleExist() {
		return scanModuleExist;
	}

	public void setScanModuleExist(boolean scanModuleExist) {
		this.scanModuleExist = scanModuleExist;
	}

	public EgovMessages getEgovMess() {
		return egovMess;
	}

	public void setEgovMess(EgovMessages egovMess) {
		this.egovMess = egovMess;
	}

	public List<SystemClassif> getScTopicList() {
		return scTopicList;
	}

	public void setScTopicList(List<SystemClassif> scTopicList) {
		this.scTopicList = scTopicList;
	}
	
	/**
	 * Разглеждане на документ
	 * При отваряне в нов таб, като title на таба, да излезе номера и дата на документа
	 * @return
	 */
	public String getRnFullViewDoc() {
		if (document != null) {
			return DocDAO.formRnDocDate(this.document.getRnDoc(), this.document.getDocDate(), this.document.getPoredDelo());
		} else {
			return "Документ";
		}
	}

	public LazyDataModelSQL2Array getDocAccessList() {
		return docAccessList;
	}

	public void setDocAccessList(LazyDataModelSQL2Array docAccessList) {
		this.docAccessList = docAccessList;
	}

	public String getSrokPattern() {
		if(srokPattern == null && document != null) {
			loadSrokPattern();
		}
		return srokPattern;
	}

	public void setSrokPattern(String srokPattern) {
		this.srokPattern = srokPattern;
	}

	public boolean isShowCoresp() {
		return showCoresp;
	}

	public void setShowCoresp(boolean showCoresp) {
		this.showCoresp = showCoresp;
	}

	public boolean isEditReferentsWRK() {
		return editReferentsWRK;
	}

	public void setEditReferentsWRK(boolean editReferentsWRK) {
		this.editReferentsWRK = editReferentsWRK;
	}

	public Integer getDocSettingId() {
		return docSettingId;
	}

	public void setDocSettingId(Integer docSettingId) {
		this.docSettingId = docSettingId;
	}

	public List<Files> getTemplatesList() {
		return templatesList;
	}

	public void setTemplatesList(List<Files> templatesList) {
		this.templatesList = templatesList;
	}

	public Object[] getCodeExtCheck() {
		return codeExtCheck;
	}

	public void setCodeExtCheck(Object[] codeExtCheck) {
		this.codeExtCheck = codeExtCheck;
	}

	public List<SystemClassif> getClassifProceduri() {
		return classifProceduri;
	}

	public void setClassifProceduri(List<SystemClassif> classifProceduri) {
		this.classifProceduri = classifProceduri;
	}

	public Map<Integer, Object> getSpecificsProc() {
		return specificsProc;
	}

	public void setSpecificsProc(Map<Integer, Object> specificsProc) {
		this.specificsProc = specificsProc;
	}

	public boolean isEnableProc() {
		return enableProc;
	}

	public void setEnableProc(boolean enableProc) {
		this.enableProc = enableProc;
	}

	public boolean isNotFinishedProc() {
		return notFinishedProc;
	}

	public void setNotFinishedProc(boolean notFinishedProc) {
		this.notFinishedProc = notFinishedProc;
	}
	
	/**
	 * брой на редове в полето относно 
	 * @return
	 */
	public int rowsOtnosno() {
		int rows = Objects.equals(document.getDocType(),DocuConstants.CODE_ZNACHENIE_DOC_TYPE_IN) ? 16 : 12;
		boolean bb = isView == 1 ? document.getProcDef() == null :  document.getProcDef() == null && (classifProceduri == null || classifProceduri.isEmpty());
		if(bb ) {
			rows = rows-3;
		}
		return rows;
	}
	

	/* 
	 * Известие за доставка Обратна разписка /кореспонденти шаблон 04.2018  - ivanc
	*/
		/*public void izvestieDostav()  {
			
					
			try{
				
				// 1. Зарежда лиценза за работа с MS Word documents.
				
				License license = new License();
				String nameLic="Aspose.Words.lic";
				
				InputStream inp = getClass().getClassLoader().getResourceAsStream(nameLic);
				license.setLicense(inp);
				
				// 2. Чете файл-шаблон от БД

				Files fileShabl = new FilesDAO(getUserData()).findById(Integer.valueOf(-111));

				// 3. Създава празен MS Word документ от шаблона 
				
				Document docEmptyShablon = new Document(new ByteArrayInputStream(fileShabl.getContent()));

				
				// 4. Създава попълнен документ от шаблона
				Document docFilledShablon = null;
	
				docFilledShablon = new FillDocShablon().fillDocShabl243 (document, docEmptyShablon, sd, ud);
			
				ByteArrayOutputStream dstStream = new ByteArrayOutputStream();
				docFilledShablon.save(dstStream, SaveFormat.DOCX);
				byte [] bytearray = null;
				bytearray = dstStream.toByteArray();
				// 5. Създава файла от създадения MS Word документ и го показва
				if (bytearray !=null){ 
					String fileName = "Izvestie_Dostavka";
					
					SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
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
			
		}*/
		
	/**
	 * За журнала! 
	 * @return
	 */
	public String getRnFullDocAudit() {
		//Не изпозлвам rnFullDoc, защото може да се обърка отварянето на табовете....
		String rnAudit = null;
		if(document != null && document.getId() != null) {
			rnAudit = "Документ: "+DocDAO.formRnDocDate(this.document.getRnDoc(), this.document.getDocDate(), this.document.getPoredDelo());
		}
		return rnAudit;
	}

	public Integer getDocSettingHar() {
		return docSettingHar;
	}

	public void setDocSettingHar(Integer docSettingHar) {
		this.docSettingHar = docSettingHar;
	}


	public String getMembersTab() {
		return membersTab;
	}

	public void setMembersTab(String membersTab) {
		this.membersTab = membersTab;
	}


	public boolean isMembersTabForDel() {
		return membersTabForDel;
	}

	public void setMembersTabForDel(boolean membersTabForDel) {
		this.membersTabForDel = membersTabForDel;
	}

	public String getRnFullDocSign() {
		
		if(document.getRnDoc()!=null && document.getDocDate()!=null) {
			rnFullDocSign = document.getRnDoc() + "/" + DateUtils.printDate(document.getDocDate());
		} else {
			rnFullDocSign = "-1";
		}
		return rnFullDocSign;
	}

	public void setRnFullDocSign(String rnFullDocSign) {
		this.rnFullDocSign = rnFullDocSign;
	}

	public String getDopInfoAdres() {
		if(this.dopInfoAdres == null) {
			loadDopInfoAdres();
		}
		return dopInfoAdres;
	}

	public void setDopInfoAdres(String dopInfoAdres) {
		this.dopInfoAdres = dopInfoAdres;
	}

	/**
	 * да зачисти информацията за полето адрес на кореспонднет
	 */
	public void clearInfoAdres() {
		if(isView != 1) {
			this.dopInfoAdres = null;
		}
	}
	
	/**
	 * зарежда адреса на кореспондента
	 */
	public void loadDopInfoAdres() {
		if(document.getCodeRefCorresp() != null) {
			// ако нямам права да виждам лини данни
			// заради достъпа до личните данни - в допълнителната информаиця за физическите лица да остане само населеното място!!
			try {				
				this.dopInfoAdres = sd.decodeItemDopInfo(DocuConstants.CODE_CLASSIF_REFERENTS, document.getCodeRefCorresp(), getCurrentLang(), new Date());
				if(this.dopInfoAdres != null &&
					(int) sd.getItemSpecific(DocuConstants.CODE_CLASSIF_REFERENTS, document.getCodeRefCorresp() ,  getCurrentLang(), new Date(), DocuClassifAdapter.REFERENTS_INDEX_REF_TYPE) == DocuConstants.CODE_ZNACHENIE_REF_TYPE_FZL) {
				
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

	public Integer getShowSignMethod() {
		return showSignMethod;
	}

	public void setShowSignMethod(Integer showSignMethod) {
		this.showSignMethod = showSignMethod;
	}

	public String getRnFullDocEdit() {
		return rnFullDocEdit;
	}

	public void setRnFullDocEdit(String rnFullDocEdit) {
		this.rnFullDocEdit = rnFullDocEdit;
	}

	//************************************************************************************************************************************************************************************************
	
	// Print envelopes / Deliver Notice
	private ReferentAddress adr=null;
	private Referent ref=null;	
	private Integer idRegistratura = null;
	@SuppressWarnings("unused")
	private  Integer codeObject = null;              // Код на съответния обект - спортен обект - 97, спортно формирование, спортно обединение, треньори 
	private String headerDialog = null;
	public static final String	LABELS			= "labels";
	
		private Integer formatPlik=null;
		private boolean recommended = false;
		private String corespName=null;
		private String corespTel=null;
		private String corespAddress=null;
		private String corespPostCode=null; 
		private String corespPBox=null;
		private String corespObl=null;
		private String corespNM=null;
		private String corespCountry=null;
		
		private String corespNamePar=null;
		private String corespTelPar=null;
		private String corespAddressPar=null;
		private String corespPostCodePar=null; 
		private String corespOblPar=null;
		private String corespNMPar=null;
		
		private String senderName=null;
		private String senderTel=null;
		private String senderAddress=null;
		private String senderPostCode=null; 
		private String senderPBox=null;
		private String senderObl=null;
		private String senderNM=null;
		private String senderCountry=null;
		
		private String senderNameMS=null;
		private String senderTelMS=null;
		private String senderAddressMS=null;
		private String senderPostCodeMS=null; 
		private String senderOblMS=null;
		private String senderNMMS=null;
		private boolean prInpPar = false;
		
		private Integer countryBg=null;
		private String countryBgStr = null;
		private String identDoc = null;         // За идентификация на формата за обратна разписка
		
		
		/**
		 * Данни по подразбиране за подател . Министерство на младежта и спорта
		 */
		public void setSenderForMS () {
			this.senderNameMS= "Министерство на младежта и спорта";
			this.senderTelMS=null;
			this.senderAddressMS= "бул. 'Васил Левски'  №25";
			this.senderPostCodeMS="1142"; 
			this.senderOblMS= "общ. Столична, обл. София (столица)";
			this.senderNMMS= "гр. София";
			
		}
		
		// Държава България
		public void setCountryBulg () {
			   this.countryBg = null;
				int cBG = 37;   // Код EKATTE за България
				
				try {
					cBG = Integer.parseInt(getSystemData().getSettingsValue("delo.countryBG"));
					if (cBG <= 0)  cBG = 37;
					
				} catch (Exception e) {
					LOGGER.error("Грешка при определяне на код на държава България от настройка: delo.countryBG", e);
					cBG = 37;
				}
			   
				 this.countryBg = Integer.valueOf(cBG);
				
			
			   try {
					this.countryBgStr= getSystemData().decodeItem(DocuConstants.CODE_CLASSIF_COUNTRIES, this.countryBg, getUserData().getCurrentLang(), new Date());
				} catch (DbErrorException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					this.countryBgStr = null;
				}
		}
		
		/**
		 *  Входни параметри, свързани с печат на еттикети
		 */
		public void setObjParams ( ) {
		    
			this.prInpPar = false;
			
			String s = null;
			s = JSFUtils.getRequestParameter("codeObj");
			if (s != null && !s.trim().isEmpty()) {
				this.codeObject = Integer.valueOf(s.trim());
				this.prInpPar = true;
			}
			
			s = JSFUtils.getRequestParameter("nameObj");
			if (s != null && !s.trim().isEmpty()) {
				this.corespNamePar = s.trim();
				this.prInpPar = true;
			}
			
			s = JSFUtils.getRequestParameter("addressObj");
			if (s != null && !s.trim().isEmpty()) {
				this.corespAddressPar = s.trim();
				this.prInpPar = true;
			}
			
			s = JSFUtils.getRequestParameter("oblastObj");
			if (s != null && !s.trim().isEmpty()) {
				this.corespOblPar = s.trim();
				this.prInpPar = true;
			}
			
			s = JSFUtils.getRequestParameter("nasMObj");
			if (s != null && !s.trim().isEmpty()) {
				this.corespNMPar = s.trim();
				this.prInpPar = true;
			}
			
			s = JSFUtils.getRequestParameter("postCodeObj");
			if (s != null && !s.trim().isEmpty()) {
				this.corespPostCodePar = s.trim();
				this.prInpPar = true;
			}
			
			s = JSFUtils.getRequestParameter("telObj");
			if (s != null && !s.trim().isEmpty()) {
				this.corespTelPar = s.trim();
				this.prInpPar = true;
			}
			
			
		}
		
		public void clearPostCover() {
			
			this.setFormatPlik(null);
			this.setRecommended(false);
			this.setCorespName(null);
			this.setCorespTel(null);
			this.setCorespAddress(null);
			this.setCorespPostCode(null); 
			this.setCorespPBox(null);
			this.setCorespObl(null);
			this.setCorespNM(null);
			this.setSenderName(null);
			this.setSenderTel(null);
			this.setSenderAddress(null);
			this.setSenderPostCode(null); 
			this.setSenderPBox(null);
			this.setSenderObl(null);
			this.setSenderNM(null);
			this.setCorespCountry(null);
			this.setSenderCountry(null);
			

		}
		
	
		
		// Формат С6/5 за етикети
		public void actionSetFormatP () {
			this.formatPlik  = Integer.valueOf(2);
			if ((this.codeObject != null && this.codeObject.intValue() == 97) && this.document.getDocVid() != null
					&& (this.document.getDocVid().intValue() == 44 || (this.document.getDocVid().intValue() >= 55 &&  this.document.getDocVid().intValue() <= 62))  ) {
				this.headerDialog = getMessageResourceString(LABELS, "docDatDvij.envPrint") + "/" + getMessageResourceString(LABELS, "docData.deliverNotFT");
			} else
				this.headerDialog = getMessageResourceString(LABELS, "docDatDvij.envPrint");
		
		}
		
		/**
		 * Данни за получател
		 */
		public void prepareCorespData() {
			
//			try {
//				
//				// Corespondents
//				if(null != getRef()) {
//					if(null != this.getRef().getRefName() && !this.getRef().getRefName().trim().equals("")){// Name
//						this.setCorespName(this.getRef().getRefName().trim()); 
//					}
//					if(null != this.getRef().getContactPhone() && !this.getRef().getContactPhone().trim().equals("")){//Tel
//						this.setCorespTel(this.getRef().getContactPhone().trim()); 
//					}
//				}
//				
//				
//				if(null != this.getAdr()) {
//					if(null != this.getAdr().getAddrText() && !this.getAdr().getAddrText().trim().equals("")){// Address
//						this.setCorespAddress(this.getAdr().getAddrText().trim());	
//					}else if (null != this.getAdr().getPostBox() && !this.getAdr().getPostBox().trim().equals("")) {
//						this.setCorespAddress("Пощенска кутия "+this.getAdr().getPostBox().trim());	
//					}
//		
//					if(null != this.getAdr().getPostCode() && !this.getAdr().getPostCode().trim().equals("")){//PostCode - BG
//						this.setCorespPostCode(this.getAdr().getPostCode().trim()); 
//					} 
//					
//					if(null != this.getAdr().getEkatte() && null != this.getRef().getDateReg()){
//						String obstObl = getSystemData().decodeItemLite(CODE_CLASSIF_EKATTE, this.getAdr().getEkatte(), CODE_DEFAULT_LANG, this.getRef().getDateReg(), false).getDopInfo();// Obst and Obl
//						if (null!=obstObl) { 
//							String[] deco = obstObl.split(",");
//							if (deco.length==2 && null!=deco[1]) {
//								this.setCorespObl(deco[1].trim());// Oblast
//							}
//						}
//						
//						//NM
//						this.setCorespNM(getSystemData().decodeItem(CODE_CLASSIF_EKATTE, this.getAdr().getEkatte(), CODE_DEFAULT_LANG, this.getRef().getDateReg()));
//		
//					}
//
//					if(null != getAdr().getAddrCountry()){//Country
//						this.setCorespCountry(getSystemData().decodeItem(DocuConstants.CODE_CLASSIF_COUNTRIES, this.getAdr().getAddrCountry(), CODE_DEFAULT_LANG, this.getRef().getDateReg())); 
//					}
//				}
//				
//				
//				
//			} catch (BaseException e) {
//				LOGGER.error(getMessageResourceString(beanMessages, "Грешка при вземане на данни за кореспондент!"), e);
//				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
//			}	
			
			this.corespName=this.corespNamePar;
			this.corespTel=this.corespTelPar;
			this.corespAddress=this.corespAddressPar;
			this.corespPostCode=this.corespPostCodePar; 
			this.corespPBox=null;
			this.corespObl=this.corespOblPar;
			this.corespNM=this.corespNMPar;
		
			this.corespCountry = this.countryBgStr;
			
			
		}
		
	
		/**
		 *  Данни за подател
		 */
		public void prepareSenderData() {
//			Registratura registratura=null;
//			try {
//				
//				this.setIdRegistratura(((UserData)getUserData()).getRegistratura());
//				
//				if (null!=this.getIdRegistratura()) {
//					registratura = new RegistraturaDAO(getUserData()).findById(this.getIdRegistratura());
//					if (null!=registratura) {
//					
//						if (null!=registratura.getAddress() && ! registratura.getAddress().trim().equals("")) {
//							this.setSenderAddress(registratura.getAddress().trim());
//						}else if (null != registratura.getPostBox() && !registratura.getPostBox().trim().equals("")) {
//							this.setSenderAddress("Пощенска кутия "+registratura.getPostBox().trim());	
//						}
//						
//						
//						if (null!=registratura.getOrgName() && ! registratura.getOrgName().trim().equals("")) {
//							this.setSenderName(registratura.getOrgName().trim());
//						}
//						
//						if (null!=registratura.getContacts() && ! registratura.getContacts().trim().equals("")) {
//							this.setSenderTel(registratura.getContacts().trim());
//						}
//						
//						if(null!=registratura.getPostCode() && !registratura.getPostCode().trim().equals("")){
//							this.setSenderPostCode(registratura.getPostCode().trim());
//						}
//						
//						if(null != registratura.getEkatte()){
//							String obstOblS = getSystemData().decodeItemLite(CODE_CLASSIF_EKATTE, registratura.getEkatte(), CODE_DEFAULT_LANG, new Date(), false).getDopInfo();// Obst and Obl
//							if (null!=obstOblS) { 
//								String[] deco = obstOblS.split(",");
//								if (deco.length==2 && null!=deco[1]) {
//									this.setSenderObl(deco[1].trim());// Oblast
//								}
//							}
//							
//							//NM
//							this.setSenderNM(getSystemData().decodeItem(CODE_CLASSIF_EKATTE, registratura.getEkatte(), CODE_DEFAULT_LANG, new Date()));
//			
//						}
//					}
//					
//				}
//			
//			} catch (BaseException e) {
//				LOGGER.error(getMessageResourceString(beanMessages, "Грешка при вземане на данни за подател!"), e);
//				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
//			}	
			
			this.senderName=this.senderNameMS;
			this.senderTel=this.senderTelMS;
			this.senderAddress=this.senderAddressMS;
			this.senderPostCode=this.senderPostCodeMS; 
			this.senderPBox=null;
			this.senderObl=this.senderOblMS;
			this.senderNM=this.senderNMMS;
		
			this.senderCountry = this.countryBgStr;
					
		}

		/**
		 * Проверки за полета
		 * @return
		 */
		public boolean validatePrint() {
			boolean err=true;
			
			if(null == this.getCorespName() || this.getCorespName().trim().equals("")){
				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, "Въведете получател!");
				err=false;
			}
			
			if(null == this.getSenderName() || this.getSenderName().trim().equals("")){
				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, "Въведете подател!");
				err=false;
			}
			
			if(null == this.getCorespAddress() || this.getCorespAddress().trim().equals("")){
				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, "Въведете Адрес/До поискване на получател!");
				err=false;
			}
			
			if(null == this.getSenderAddress() || this.getSenderAddress().trim().equals("")){
				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, "Въведете Адрес  на подател!");
				err=false;
			}
					
			if(null == this.getCorespNM() || this.getCorespNM().trim().equals("")){
				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, "Въведете нас.место на получател!");
				err=false;
			}
			
			if(null == this.getSenderNM() || this.getSenderNM().trim().equals("")){
				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, "Въведете нас.место на подател!");
				err=false;
			}
			
			if (null==this.getAdr() || null==getAdr().getAddrCountry() || (this.getAdr().getAddrCountry().equals(this.getCountryBg()) || null==getAdr().getAddrCountry())) {
				
				if(null == this.getSenderPostCode() || this.getSenderPostCode().trim().equals("")){
					JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, "Въведете пощ.код на подател!");
					err=false;
				}else if(this.getSenderPostCode().trim().length()!=4) {
					JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, "Пощ.код на подател трябва да има 4 цифри!");
					err=false;
				}
				
				if(null == this.getCorespPostCode() || this.getCorespPostCode().trim().equals("")){
					JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, "Въведете пощ.код на получател!");
					err=false;
				}else if(this.getCorespPostCode().trim().length()!=4) {
					JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, "Пощ.код на получател трябва да има 4 цифри!");
					err=false;
				}
				
				if(null == this.getCorespObl() || this.getCorespObl().trim().equals("")){
					JSFUtils.addGlobalMessage( FacesMessage.SEVERITY_ERROR, "Въведете Област на получател!");
					err=false;
				}
				
				if(null == this.getSenderObl() || this.getSenderObl().trim().equals("")){
					JSFUtils.addGlobalMessage( FacesMessage.SEVERITY_ERROR, "Въведете Област на подател!");
					err=false;
				}
				
			}
			
			if (null!=this.getAdr() && !this.getAdr().getAddrCountry().equals(this.getCountryBg())) {
				
				if(null == this.getCorespCountry() || this.getCorespCountry().trim().equals("")){
					JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, "Въведете държава на получател!");
					err=false;
				}
				
				if(null == this.getSenderCountry() || this.getSenderCountry().trim().equals("")){
					JSFUtils.addGlobalMessage( FacesMessage.SEVERITY_ERROR, "Въведете държава на подател!");
					err=false;
				}
				
			}
			
			return err;
		}
		
		/**
		 *  Формиране печат
		 * @param envNotice - 1 - печат на етикети
		 */
		public void actionPrint(int envNotice) {
			
			if(!validatePrint()) {
				scrollToMessages();
				return;
			}
			
			String regN="";
			SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
			    	
	    	if (null!=this.document.getRnDoc())
	    		regN +=this.document.getRnDoc().trim();
	    	if (null!=this.document.getDocDate())
	    		regN +="/"+sdf.format(this.document.getDocDate());
			
			
			
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
			    		rPlik = getSystemData().decodeItemLite(DocuConstants.CODE_CLASSIF_POST_ENVELOPS, this.getFormatPlik(), CODE_DEFAULT_LANG, new Date(), false).getDopInfo();
			    	}

					exp.printPlikCorespondent(rPlik, this.getCorespName(), correspData, this.getSenderName(), senderData, regN, this.isRecommended());

				
			    }catch (DbErrorException e) {
			        LOGGER.error("Грешка при отпечатване на плик!",e);
			        JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, "Грешка при отпечатване на плик!", e.getMessage());
			    }
			}else {//Печат Обратна разписка
				
				try{
					
					// 1. Зарежда лиценза за работа с MS Word documents.
					
					License license = new License();
					String nameLic="Aspose.Words.lic";
					InputStream inp = getClass().getClassLoader().getResourceAsStream(nameLic);
					license.setLicense(inp);
					
					this.identDoc = "";
					if (this.document.getRnDoc() != null)
						this.identDoc += this.document.getRnDoc().trim();
					if (this.document.getDocDate() != null) 
						this.identDoc += "/" + new SimpleDateFormat ("dd.MM.yyyy").format(this.document.getDocDate()) + " г." ;
					
					Document docEmptyShablon = null; 
					// 2. Чете файл-шаблон и създава празен Aspose Document за попълване 
//					String namIzv="/resources/docs/"+"Известие доставка 243.docx"
					String namIzv="/resources/docs/"+"ИЗВЕСТИЕ ЗА ДОСТАВЯНЕ.docx";
					if (this.document.getDocVid() != null)  {
						int vid =  this.document.getDocVid().intValue();
                    		switch (vid) {
                    			// Удостоверения за регистрация
                    			case 43:
                    			case 44:
                    			case 45:	
 //                   				namIzv="/resources/docs/"+"Известие доставка 243_udo.docx";
                    				this.identDoc +=  "  У – 7777, СВСУ /СУСУ";
                    			    break;
                    			 // Писмо    
                    			case 55:
  //                  				namIzv="/resources/docs/"+"Известие доставка 243_pismo.docx";
                    				this.identDoc +=  "  (п), СВСУ /СУСУ";
                    			    break;
                    			 // Заповеди   
                    			case 56:
                    			case 57:
                    			case 58:
                    			case 59:
                    			case 60:
                    			case 61:
                    			case 62:	
 //                   				namIzv="/resources/docs/"+"Известие доставка 243_zap.docx";
                    				this.identDoc +=  "  (з), СВСУ /СУСУ";
                    			    break;
                    			 // Заявление за вписване
                    			case 34:
                    			case 35:
                    			case 36:
                    			case 37:
                    			//  Заявление за заличаване	
                    			case 38:
                    			case 39:
                    			case 40:
                    			case 41:
                    			//  Заявление за промяна обстоятелства
                    			case 51:
                    			case 52:
                    			case 53:
                    			case 54:   
                    				      break;
                    			// Уведомления
                    			case 46:
                    			case 47:
                    			case 48:
                    			case 49:   
                    			case 50: 	
                    				      break;		      
                    			    
                    			default:
                    				break;
                    		
                    		}
							
					}
					
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
	 		        
//			        docEmptyShablon = new Document(new ByteArrayInputStream(baR));
			     
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


		}
		
		
		/**
		 * Вика функцията scrollToErrors на страницата, за да се скролне екранът към съобщенията за грешка.
		 * Сложено е, защото иначе съобщенията може да са извън видимия екран и user изобшо да не разбере,
		 * че е излязла грешка, и каква.
		 */
		private void scrollToMessages() {
			PrimeFaces.current().executeScript("scrollToErrors()");
		}
		
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
				
							
			
			return retDat;
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

			if(null != this.getSenderObl() && !this.getSenderObl().trim().equals("")){
				if (! this.getSenderObl().trim().contains("обл")) {
					retDat+="\n"+"обл. "+this.getSenderObl().trim();
				}else {
					retDat+="\n"+this.getSenderObl().trim();
				}
			}

			if(null != this.getSenderPostCode() && !this.getSenderPostCode().trim().equals(""))
				retDat+="\n"+this.getSenderPostCode();
			
			if(null != this.getSenderNM() && !this.getCorespNM().trim().equals(""))
				retDat+=" "+this.getSenderNM();

			if(null!=this.getAdr() && !this.getAdr().getAddrCountry().equals(this.getCountryBg()) && null != this.getSenderCountry() && !this.getSenderCountry().trim().equals(""))
				retDat+="\n"+this.getSenderCountry();
			
			return retDat;
			
		}
		
		public Document fillDocShabl243 (com.aspose.words.Document pattern) throws DbErrorException  {
			
			try{
				
				// 1. Данни на Получател 
				
				if (null != this.getCorespName() && pattern.getRange().getBookmarks().get("poluchatel") !=null){// Name
					pattern.getRange().getBookmarks().get("poluchatel").setText(this.getCorespName());
				}	
				
				if (null!=this.getCorespAddress() && pattern.getRange().getBookmarks().get("adres") !=null){//Addres
					String coradr=this.getCorespAddress();
					if(null != this.getCorespTel() && !this.getCorespTel().trim().equals("")){
						if(null!=this.getAdr() && !this.getAdr().getAddrCountry().equals(this.getCountryBg())) {
							coradr+=" \r\n                                              phone "+ this.getCorespTel();
						}else{
							coradr+="  \r\n                                              тел. "+ this.getCorespTel();
						}
					}
					
					pattern.getRange().getBookmarks().get("adres").setText(coradr);
				}
										
				if (null != this.getCorespNM() && pattern.getRange().getBookmarks().get("nasMesto") !=null){
					pattern.getRange().getBookmarks().get("nasMesto").setText(this.getCorespNM());
				}
					
				if (null != this.getCorespPostCode()){
					String pkAdressat=this.getCorespPostCode().trim();
					if (pattern.getRange().getBookmarks().get("pk1") !=null)
						pattern.getRange().getBookmarks().get("pk1").setText(pkAdressat.substring(0, 1));
					if (pattern.getRange().getBookmarks().get("pk2") !=null)
						pattern.getRange().getBookmarks().get("pk2").setText(pkAdressat.substring(1, 2));
					if (pattern.getRange().getBookmarks().get("pk3") !=null)
						pattern.getRange().getBookmarks().get("pk3").setText(pkAdressat.substring(2, 3));
					if (pattern.getRange().getBookmarks().get("pk4") !=null)
						pattern.getRange().getBookmarks().get("pk4").setText(pkAdressat.substring(3, 4));
					if (pattern.getRange().getBookmarks().get("pk") !=null)
						pattern.getRange().getBookmarks().get("pk").setText(pkAdressat);
				}

				 pattern.getRange().getBookmarks().get("identDoc").setText(this.identDoc);
				
				// 2. Данни на адресанта(подател) - от регистратурата
				
		            if (null!= this.getSenderName() && pattern.getRange().getBookmarks().get("podatel") !=null){
						pattern.getRange().getBookmarks().get("podatel").setText(this.getSenderName());
					}

		            if (null!=this.getSenderAddress() && pattern.getRange().getBookmarks().get("adresPod") !=null){
		            	String sendadr=this.getSenderAddress();
//		            	if(null != this.getSenderTel() && !this.getSenderTel().trim().equals("")){
//		    				if(null!=this.getAdr() && !this.getAdr().getAddrCountry().equals(this.getCountryBg())) {
//		    					sendadr+="\r\n           phone "+ this.getSenderTel();
//		    				}else{
//		    					sendadr+="\r\n            тел. "+ this.getSenderTel();
//		    				}
//		    			}
		            	
						pattern.getRange().getBookmarks().get("adresPod").setText(sendadr);
					}
					

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
//							barcode.setAltText("Пощенски код");
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
		

		

		public Integer getFormatPlik() {
			return formatPlik;
		}

		public void setFormatPlik(Integer formatPlik) {
			this.formatPlik = formatPlik;
		}

		public boolean isRecommended() {
			return recommended;
		}

		public void setRecommended(boolean recommended) {
			this.recommended = recommended;
		}

		public String getCorespName() {
			return corespName;
		}

		public void setCorespName(String corespName) {
			this.corespName = corespName;
		}

		public String getCorespTel() {
			return corespTel;
		}

		public void setCorespTel(String corespTel) {
			this.corespTel = corespTel;
		}

		public String getCorespAddress() {
			return corespAddress;
		}

		public void setCorespAddress(String corespAddress) {
			this.corespAddress = corespAddress;
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

		public String getCorespCountry() {
			return corespCountry;
		}

		public void setCorespCountry(String corespCountry) {
			this.corespCountry = corespCountry;
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

		public String getSenderCountry() {
			return senderCountry;
		}

		public void setSenderCountry(String senderCountry) {
			this.senderCountry = senderCountry;
		}

		public Integer getCountryBg() {
			return countryBg;
		}

		public void setCountryBg(Integer countryBg) {
			this.countryBg = countryBg;
		}

		public Referent getRef() {
			return ref;
			
		}

		public void setRef(Referent ref) {
			this.ref = ref;
			
		}

		public ReferentAddress getAdr() {
			return adr;
			
		}

		public void setAdr(ReferentAddress adr) {
			this.adr = adr;
			
		}

		public Integer getIdRegistratura() {
			return idRegistratura;
			
		}

		public void setIdRegistratura(Integer idRegistratura) {
			this.idRegistratura = idRegistratura;
			
		}

		public boolean isPrInpPar() {
			return prInpPar;
			
		}

		public void setPrInpPar(boolean prInpPar) {
			this.prInpPar = prInpPar;
			
		}

		public Integer getCodeObject() {
			return codeObject;
		}

		public void setCodeObject(Integer codeObject) {
			this.codeObject = codeObject;
		}

		public String getHeaderDialog() {
			return headerDialog;
		}

		public void setHeaderDialog(String headerDialog) {
			this.headerDialog = headerDialog;
		}
	
	
	//************************************************************************************************************************************************************************************************
	
	
}