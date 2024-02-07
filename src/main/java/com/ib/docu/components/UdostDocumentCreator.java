package com.ib.docu.components;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.faces.application.FacesMessage;

import org.primefaces.PrimeFaces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aspose.words.Bookmark;
import com.aspose.words.Document;
import com.aspose.words.License;
import com.aspose.words.SaveFormat;
import com.ib.docu.db.dao.DocDAO;
import com.ib.docu.db.dao.MMSCoachesDAO;
import com.ib.docu.db.dao.MMSSportObektDAO;
import com.ib.docu.db.dao.MMSVidSportDAO;
import com.ib.docu.db.dao.MMSVpisvaneDAO;
import com.ib.docu.db.dao.MMSsportFormirovanieDAO;
import com.ib.docu.db.dao.MMSsportObedinenieDAO;
import com.ib.docu.db.dao.ReferentDAO;
import com.ib.docu.db.dto.Doc;
import com.ib.docu.db.dto.DocReferent;
import com.ib.docu.db.dto.MMSCoaches;
import com.ib.docu.db.dto.MMSSportObekt;
import com.ib.docu.db.dto.MMSSportnoObedinenie;
import com.ib.docu.db.dto.MMSVidSportSC;
import com.ib.docu.db.dto.MMSVpisvane;
import com.ib.docu.db.dto.MMSsportFormirovanie;
import com.ib.docu.db.dto.Referent;
import com.ib.docu.system.DocuConstants;
import com.ib.docu.system.SystemData;
import com.ib.docu.system.UserData;
import com.ib.indexui.utils.JSFUtils;
import com.ib.system.db.JPA;
import com.ib.system.db.dao.FilesDAO;
import com.ib.system.db.dto.Files;
import com.ib.system.exceptions.DbErrorException;

/**
 * Извиква се от компонентата CompMMSVpisvane,
 * когато се създаде документ към вписването,
 * и се натисне бутон 'Удостоверителен документ'
 * 
 * @author n.kanev
 *
 */
public class UdostDocumentCreator {

	private static final Logger LOGGER = LoggerFactory.getLogger(UdostDocumentCreator.class);
	
	// Имена на букмаркове в документите
	private final BookmarkObject BOOKMARK_NOMER = 			new BookmarkObject("nomer", "Номер");
	private final BookmarkObject BOOKMARK_REG_NOMER =		new BookmarkObject("regNomer", "Регистрационен номер");
	private final BookmarkObject BOOKMARK_REG_DATA =		new BookmarkObject("regData", "Дата на регистрация");
	private final BookmarkObject BOOKMARK_IZDADEN_NA = 		new BookmarkObject("izdadenNa", "Издаден на");
	private final BookmarkObject BOOKMARK_CHLEN_NA =		new BookmarkObject("chlenNa", "Член на");
	private final BookmarkObject BOOKMARK_STATUT = 			new BookmarkObject("statut", "Статут");
	private final BookmarkObject BOOKMARK_SDRUJENIE =		new BookmarkObject("sdrujenie", "Сдружение");
	private final BookmarkObject BOOKMARK_VID_SPORT = 		new BookmarkObject("vidSport", "Вид спорт");
	private final BookmarkObject BOOKMARK_SEDALISHTE = 		new BookmarkObject("sedalishte", "Седалище");
	private final BookmarkObject BOOKMARK_OBLAST =			new BookmarkObject("oblast", "Област");
	private final BookmarkObject BOOKMARK_OBSHTINA =		new BookmarkObject("obshtina", "Община");
	private final BookmarkObject BOOKMARK_EIK = 			new BookmarkObject("eik", "ЕИК");
	private final BookmarkObject BOOKMARK_OSNOVANIE = 		new BookmarkObject("osnovanie", "Основание");
	private final BookmarkObject BOOKMARK_DATA = 			new BookmarkObject("data", "Дата");
	private final BookmarkObject BOOKMARK_DLAJNOST =		new BookmarkObject("dlajnost", "Длъжност");
	private final BookmarkObject BOOKMARK_PODPISAL =		new BookmarkObject("podpisal", "Име на подписал");
	private final BookmarkObject BOOKMARK_PODPISAL_DLAJN = 	new BookmarkObject("podpisalDlajn", "Длъжност на подписал");
	private final BookmarkObject BOOKMARK_SPORT_SPEC_DEINOST = new BookmarkObject("sportSpecDeinost", "Спорт или Специализирана спортна дейност");
	private final BookmarkObject BOOKMARK_DUBLIKAT = 		new BookmarkObject("dublikat", "Документът е дубликат");
	
	private static final String EMPTY_VALUE = "";
	private static final String SHABLON_FILENAME_CONTAINS_FEDERACIA = "[спортна федерация]";
	private static final String SHABLON_FILENAME_CONTAINS_NOUS = "[ноус]";
	private static final String SHABLON_FILENAME_CONTAINS_OSK = "[оск]";
	private static final String SHABLON_FILENAME_CONTAINS_TURIST = "[туристическо дружество]";
	private static final String SHABLON_FILENAME_CONTAINS_SPORT_KLUB = "[спортен клуб]";
	
	private final UserData userData;
	private final SystemData systemData;
	private final Integer currentLang;
	private final boolean isDublikat;
	private final boolean isCopy;
	
	private Integer idVpisvane;
	private Integer idObject;
	private Integer typeObject;
	private Integer idDoc;
	private Integer vidDoc;
	
	// Няколко помощни променливи, за да се спести извикване на методи
	private String dokumentType;
	private String obektType;
	private String obektName;
	
	private MMSVpisvane vpisvane;
	private Doc doc;
	
	private List<String> nullFields;
	
	public UdostDocumentCreator(UserData userData, SystemData systemData, Integer currentLang, boolean isDublikat, boolean isCopy) {
		this.userData = userData;
		this.systemData = systemData;
		this.currentLang = currentLang;
		this.isDublikat = isDublikat;
		this.isCopy = isCopy;
		this.nullFields = new ArrayList<>();
	}
	
	/**
	 * Методът се вика директно при натискане на бутона 
	 * 'Удостоверителен документ'в екрана с данни на вписване.
	 * @throws Exception 
	 */
	public Files createDokument() throws Exception {
		
		if(this.idVpisvane == null
			|| this.idObject == null
			|| this.typeObject == null
			|| this.idDoc == null
			|| this.vidDoc == null) {
			LOGGER.error("Невалиден параметър");
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, "Невалиден параметър");
			PrimeFaces.current().executeScript("scrollToErrors()");
			return null;
		}
		
		DocDAO docDao = new DocDAO(this.userData);
		MMSVpisvaneDAO vpisvaneDao = new MMSVpisvaneDAO(this.userData);
		
		this.doc = docDao.findById(this.idDoc);
		this.vpisvane = vpisvaneDao.findById(this.idVpisvane);			
		
		this.dokumentType = this.systemData.decodeItem(
				DocuConstants.CODE_CLASSIF_DOC_VID, 
				this.vidDoc, this.currentLang, new Date());
		
		this.obektType = this.systemData.decodeItem(
				DocuConstants.CODE_CLASIF_OBJECTS,
				this.typeObject, this.currentLang, new Date());
		
		// измъкваме си шаблоните за дадения документ
		Object[] settings = docDao.findDocSettings(this.userData.getRegistratura(), vidDoc, this.systemData);
		Integer codeSetting = (Integer) settings[4];
		
		FilesDAO filesDao = new FilesDAO(this.userData);		
		List<Files> shabloni = filesDao.selectByFileObject(codeSetting, DocuConstants.CODE_ZNACHENIE_JOURNAL_DOC_VID_SETT);
		if(shabloni == null || shabloni.isEmpty()) {
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, "Няма зададен шаблон за избрания вид документ.");
			PrimeFaces.current().executeScript("scrollToErrors()");
			return null;	
		}
		Files shablon = null;
		
		// Големия избор на шаблон в зависимост от вида на документа и вида на спортното нещо (обед., формирование, обект, федерация, НОУС...)
		switch(this.vidDoc) {
			
			// ВИД ДОКУМЕНТ - Спортен лиценз или Удостоверение за вписване на НОУС/ОСЦ
			case DocuConstants.CODE_ZNACHENIE_VID_DOC_SPORT_LICENZ : {

				switch(getVidSportnoNeshto()) {
					// ВИД НА ОБЕДИНЕНИЕТО - Спортна федерация
					case DocuConstants.CODE_ZNACHENIE_VID_SPORT_OBEDINENIE_SF : {
						shablon = shabloni.stream().filter(f -> f.getFilename().toUpperCase().contains(SHABLON_FILENAME_CONTAINS_FEDERACIA.toUpperCase())).findFirst().orElse(null);
						break;
					}
					// ВИД НА ОБЕДИНЕНИЕТО - НОУС
					case DocuConstants.CODE_ZNACHENIE_VID_SPORT_OBEDINENIE_NOUS : {
						shablon = shabloni.stream().filter(f -> f.getFilename().toUpperCase().contains(SHABLON_FILENAME_CONTAINS_NOUS.toUpperCase())).findFirst().orElse(null);
						break;
					}
					// ВИД НА ОБЕДИНЕНИЕТО - Обединен спортен клуб
					case DocuConstants.CODE_ZNACHENIE_VID_SPORT_OBEDINENIE_OK : {
						shablon = shabloni.stream().filter(f -> f.getFilename().toUpperCase().contains(SHABLON_FILENAME_CONTAINS_OSK.toUpperCase())).findFirst().orElse(null);
						break;
					}
				}
				
				break;
			}
			// ВИД ДОКУМЕНТ - Удостоверение за регистрация на спортно формирование
			case DocuConstants.CODE_ZNACHENIE_VID_DOC_UDOST_REG_SPORT_FORMIROV : {

				switch(getVidSportnoNeshto()) {
					// ВИД НА ОБЕДИНЕНИЕТО - Спортен клуб
					case DocuConstants.CODE_ZNACHENIE_VID_SPORTNO_FORMIROVANIE_SK : {
						shablon = shabloni.stream().filter(f -> f.getFilename().toUpperCase().contains(SHABLON_FILENAME_CONTAINS_SPORT_KLUB.toUpperCase())).findFirst().orElse(null);
						break;
					}
					// ВИД НА ОБЕДИНЕНИЕТО - Туристическо дружество
					case DocuConstants.CODE_ZNACHENIE_VID_SPORTNO_FORMIROVANIE_TD : {
						shablon = shabloni.stream().filter(f -> f.getFilename().toUpperCase().contains(SHABLON_FILENAME_CONTAINS_TURIST.toUpperCase())).findFirst().orElse(null);
						break;
					}
				}
				break;
			}
			
			case DocuConstants.CODE_ZNACHENIE_VID_DOC_UDOST_REG_SPORT_OBEKT : {
				shablon = filesDao.findById(shabloni.get(0).getId());
				break;
			}
			case DocuConstants.CODE_ZNACHENIE_VID_DOC_UDOST_REG_TREN_KADAR : {
				shablon = filesDao.findById(shabloni.get(0).getId());
				break;
			}
		}
		
		if(shablon != null) {
			shablon = filesDao.findById(shablon.getId()); // за да зареди content
		}
		
		// попълваме шаблона
		Document filledShablon = fillShablon(shablon);
		
		// запис на новия файл към документа		
		Files f = saveFile(shablon.getContentType(), filledShablon, filesDao);
		return f;
	}
	
	/**
	 * Получава празния уърдовски шаблон, стартира Aspose и избира как да го попълни в зависимост от вида на документа.
	 * @param file празен .docx шаблон
	 * @return попълнен шаблон във формата на aspose, готов за записване 
	 * @throws Exception
	 */
	private Document fillShablon(Files file) throws Exception {

		License license = new License();
		String nameLicense = "Aspose.Words.lic";
		InputStream inp = getClass().getClassLoader().getResourceAsStream(nameLicense);
		license.setLicense(inp);
		
		InputStream input = new ByteArrayInputStream(file.getContent());
		Document shablon = new Document(input);
		
		switch(this.vidDoc) {
			case DocuConstants.CODE_ZNACHENIE_VID_DOC_SPORT_LICENZ : {
				
				switch(getVidSportnoNeshto()) {
					// ВИД НА ОБЕДИНЕНИЕТО - Спортна федерация
					case DocuConstants.CODE_ZNACHENIE_VID_SPORT_OBEDINENIE_SF : {
						templateLicenzFederacia.fillTemplate(shablon);
						break;
					}
					// ВИД НА ОБЕДИНЕНИЕТО - НОУС
					case DocuConstants.CODE_ZNACHENIE_VID_SPORT_OBEDINENIE_NOUS : {
						templateUdostoverenieNous.fillTemplate(shablon);
						break;
					}
					// ВИД НА ОБЕДИНЕНИЕТО - Обединен спортен клуб
					case DocuConstants.CODE_ZNACHENIE_VID_SPORT_OBEDINENIE_OK : {
						templateUdostoverenieObedSportenKlub.fillTemplate(shablon);
						break;
					}
				}
			
				break;
			}
			case DocuConstants.CODE_ZNACHENIE_VID_DOC_UDOST_REG_SPORT_FORMIROV : {
				switch(getVidSportnoNeshto()) {
					// ВИД НА ОБЕДИНЕНИЕТО - Спортен клуб
					case DocuConstants.CODE_ZNACHENIE_VID_SPORTNO_FORMIROVANIE_SK : {
						templateUdostoverenieSportenKlub.fillTemplate(shablon);
						break;
					}
					// ВИД НА ОБЕДИНЕНИЕТО - Туристическо дружество
					case DocuConstants.CODE_ZNACHENIE_VID_SPORTNO_FORMIROVANIE_TD : {
						templateUdostoverenieTurist.fillTemplate(shablon);
						break;
					}
				}
				break;
			}
			case DocuConstants.CODE_ZNACHENIE_VID_DOC_UDOST_REG_SPORT_OBEKT : {
				templateSportenObekt.fillTemplate(shablon);
				break;
			}
			case DocuConstants.CODE_ZNACHENIE_VID_DOC_UDOST_REG_TREN_KADAR : {
				templateTrenior.fillTemplate(shablon);
				break;
			}
		}
		
		return shablon;
	}
	
	/**
	 * Самото попълване на букмарковете въс файла се извършва тук. 
	 * В мапа са попълнени по двойки името на букмарк и стойността за попълване.
	 * Също така сетва някои променливи, които се използват по-нататък
	 * при създаването на индекски Files обект. Това спестява повтаряне на 
	 * извиквания на базата и разкодиране.
	 * 
	 * @param shablon празният шаблон за попълване
	 * @param values мап, в който са подредени двойки име на букмарк и съптветна стойност за попълване
	 * @throws Exception
	 */
	private void fillShablon(Document shablon, Map<BookmarkObject, Object> values) throws Exception {
		for(BookmarkObject key : values.keySet()) {
			
			Bookmark bookmark = shablon.getRange().getBookmarks().get(key.getName());
			if(bookmark != null) {
				String value = (String) values.get(key);
				
				if(value != null) {
					value = value.trim();
				}
				else {
					value = EMPTY_VALUE;
					this.nullFields.add(key.getLabel());
				}
				
				bookmark.setText(value);
			}
		}
		
		this.obektName = getPredsedatel();
	}
	
	/**
	 * Записва шаблона като нов файл с разширение DOCX в базата и го прикача към настоящия документ this.docId
	 * 
	 * @param contentType
	 * @param filledShablon
	 * @param filesDao
	 * @return
	 * @throws Exception
	 */
	private Files saveFile(String contentType, Document filledShablon, FilesDAO filesDao) throws Exception {
		
		/*if(!this.nullFields.isEmpty()) {
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, "В удостоверителния документ липсват: " + String.join(", ", this.nullFields));
			PrimeFaces.current().executeScript("scrollToErrors()");
			return null;
		}*/
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		filledShablon.save(outputStream, SaveFormat.DOCX);
		
		Files f = new Files();
		f.setContentType(contentType);
		f.setContent(outputStream.toByteArray());
		f.setFilename(String.format("%s на %s %s%s%s.docx", this.dokumentType, this.obektType, this.obektName, (isDublikat ? "_дубликат" : ""), (isCopy ? "_копие" : "")));
		f.setFileInfo(String.format("%s%s%s", this.dokumentType, (isDublikat ? "_дубликат" : ""), (isCopy ? "_копие" : "")));
		f.setOfficial(DocuConstants.CODE_ZNACHENIE_DA); // по това може после да се види кой от файловете е удостоверителен
		
		JPA.getUtil().runInTransaction(() -> filesDao.saveFileObject(f, this.idDoc, DocuConstants.CODE_ZNACHENIE_JOURNAL_DOC));
		
		return f;
	}

	
	/* 
	 * Логиката на за попълването на различните темплейти. 
	 * Записва се в мап с имената на букмаркове и съответната 
	 * стойност, която се записва на негово място във файла.
	 */
	
	
	@FunctionalInterface
	interface TemplateFiller {
		void fillTemplate(Document document) throws Exception;
	}
	
	/** Указания за попълване на шаблони:
	 * <ul>
	 *   <li>Спортен лиценз (42) за вид спортно обединение Спортна федерация (1)</li>
	 * </ul>
	 */
	private TemplateFiller templateLicenzFederacia = d -> {
		Map<BookmarkObject, Object> templateMap = Stream.of(new Object[][] { 
			 { BOOKMARK_DUBLIKAT, getDublikat() },
			 { BOOKMARK_NOMER, getDocumentRegNo() },
		     { BOOKMARK_IZDADEN_NA, getPredsedatel() },
		     { BOOKMARK_SDRUJENIE, getName()},
		     { BOOKMARK_STATUT, getStatut() },
		     { BOOKMARK_SPORT_SPEC_DEINOST, getSportOrSpecDeinost() },
		     { BOOKMARK_VID_SPORT, getVidSport() },
		     { BOOKMARK_SEDALISHTE, getObektAdres() },
		     { BOOKMARK_EIK, getEik() },
		     { BOOKMARK_DATA, getDocDate() },
		     { BOOKMARK_PODPISAL, getPodpisal() }
		 }).collect(HashMap::new, (m, v) -> m.put((BookmarkObject) v[0], v[1]), HashMap::putAll);

		fillShablon(d, templateMap);
	};
	
	/** Указания за попълване на шаблони:
	 * <ul>
	 *   <li>Спортен лиценз (42) за вид спортно обединение НОУС (3)</li>
	 * </ul>
	 */
	private TemplateFiller templateUdostoverenieNous = d -> {
		Map<BookmarkObject, Object> templateMap = Stream.of(new Object[][] { 
			 { BOOKMARK_DUBLIKAT, getDublikat() },
			 { BOOKMARK_NOMER, getDocumentRegNo() },
		     { BOOKMARK_IZDADEN_NA, getPredsedatel() },
		     { BOOKMARK_STATUT, getStatut() },
		     { BOOKMARK_SEDALISHTE, getObektAdres() },
		     { BOOKMARK_EIK, getEik() },
		     { BOOKMARK_DATA, getDocDate() },
		     { BOOKMARK_PODPISAL, getPodpisal() }
		 }).collect(HashMap::new, (m, v) -> m.put((BookmarkObject) v[0], v[1]), HashMap::putAll);

		fillShablon(d, templateMap);
	};
	
	/** Указания за попълване на шаблони:
	 * <ul>
	 *   <li>Спортен лиценз (42) за вид спортно обединение Обединен спортен клуб (4)</li>
	 * </ul>
	 */
	private TemplateFiller templateUdostoverenieObedSportenKlub = d -> {
		Map<BookmarkObject, Object> templateMap = Stream.of(new Object[][] { 
			 { BOOKMARK_DUBLIKAT, getDublikat() },
			 { BOOKMARK_EIK, getEik() },
			 { BOOKMARK_NOMER, getDocumentRegNo() },
			 { BOOKMARK_OBLAST, "" }, // TODO нямаме област в адреса
		     { BOOKMARK_SDRUJENIE, getName()},
		     { BOOKMARK_SEDALISHTE, getObektAdres() }
		 }).collect(HashMap::new, (m, v) -> m.put((BookmarkObject) v[0], v[1]), HashMap::putAll);

		fillShablon(d, templateMap);
	};
	
	/** Указания за попълване на шаблони:
	 * <ul>
	 *   <li>Удостоверение за регистрация на спортно формирование (43) за вид Спортен клуб (1)</li>
	 * </ul>
	 */
	private TemplateFiller templateUdostoverenieSportenKlub = d -> {
		Map<BookmarkObject, Object> templateMap = Stream.of(new Object[][] { 
			 { BOOKMARK_DUBLIKAT, getDublikat() },
			 { BOOKMARK_NOMER, getDocumentRegNo() },
		     { BOOKMARK_SDRUJENIE, getName()},
		     { BOOKMARK_SEDALISHTE, getObektAdres() },
		     { BOOKMARK_OBSHTINA, "" }, // TODO
			 { BOOKMARK_REG_NOMER, getRegNomer() },
		     { BOOKMARK_REG_DATA, getRegData()},
		     { BOOKMARK_CHLEN_NA, "" } // TODO
		 }).collect(HashMap::new, (m, v) -> m.put((BookmarkObject) v[0], v[1]), HashMap::putAll);

		fillShablon(d, templateMap);
	};
	
	 /** <ul>
	 *   <li>Удостоверение за регистрация на спортно формирование (43) за вид Туристическо дружество (2)</li>
	 * </ul>
	 */
	private TemplateFiller templateUdostoverenieTurist = d -> {
		Map<BookmarkObject, Object> templateMap = Stream.of(new Object[][] { 
			 { BOOKMARK_DUBLIKAT, getDublikat() },
			 { BOOKMARK_NOMER, getDocumentRegNo() },
			 { BOOKMARK_DATA, getDocDate() },
			 { BOOKMARK_SDRUJENIE, getName()},
		     { BOOKMARK_SEDALISHTE, getObektAdres() },
		     { BOOKMARK_OBLAST, "" }, // TODO
		     { BOOKMARK_REG_NOMER, getRegNomer() },
		     { BOOKMARK_REG_DATA, getRegData()},
		     { BOOKMARK_EIK, getEik() }
		 }).collect(HashMap::new, (m, v) -> m.put((BookmarkObject) v[0], v[1]), HashMap::putAll);

		fillShablon(d, templateMap);
	};	

	
	/** Указания за попълване на шаблони:
	 * <ul>
	 *   <li>Удостоверение за регистрация на спортен обект (44)</li>
	 * </ul>
	 */
	private TemplateFiller templateSportenObekt = d -> {
		Map<BookmarkObject, Object> templateMap = Stream.of(new Object[][] { 
		     { BOOKMARK_NOMER, getDocumentRegNo() },
		     { BOOKMARK_IZDADEN_NA, getName() },
		     { BOOKMARK_STATUT, getStatut() },
		     { BOOKMARK_SEDALISHTE, getObektAdres() },
		     { BOOKMARK_DATA, getDocDate() },
		     { BOOKMARK_PODPISAL, getPodpisal() },
		     { BOOKMARK_PODPISAL_DLAJN, getPodpisalDlajnost() },
		     { BOOKMARK_DUBLIKAT, getDublikat() }
		 }).collect(HashMap::new, (m, v) -> m.put((BookmarkObject) v[0], v[1]), HashMap::putAll);

		fillShablon(d, templateMap);
	};
	
	
	/** Указания за попълване на шаблони:
	 * <ul>
	 *   <li>Удостоверение за регистрация на треньорски кадри (45)</li>
	 * </ul>
	 */
	private TemplateFiller templateTrenior = d -> {
		Map<BookmarkObject, Object> templateMap = Stream.of(new Object[][] { 
		     { BOOKMARK_NOMER, getDocumentRegNo() },
		     { BOOKMARK_IZDADEN_NA, getName() },
		     { BOOKMARK_VID_SPORT, getVidSport() },
		     { BOOKMARK_DLAJNOST, getDlajnost() },
		     { BOOKMARK_DATA, getDocDate() },
		     { BOOKMARK_PODPISAL, getPodpisal() },
		     { BOOKMARK_PODPISAL_DLAJN, getPodpisalDlajnost() },
		     { BOOKMARK_SPORT_SPEC_DEINOST, getSportOrSpecDeinost() }
		 }).collect(HashMap::new, (m, v) -> m.put((BookmarkObject) v[0], v[1]), HashMap::putAll);
		
		fillShablon(d, templateMap);
	};
	
	
	/**
	 * Рег. номер на документа.
	 * @return
	 * @throws DbErrorException
	 */
	private String getDocumentRegNo() throws DbErrorException {
		return (this.doc == null) ? null : this.doc.getRnDoc();
	}
	
	private String getRegNomer() {
		return this.vpisvane.getRnDocResult();
	}
	
	private String getRegData() {
		if(this.vpisvane.getDateDocResult() != null) {
			SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy г.");
			return sdf.format(this.vpisvane.getDateDocResult());
		}
		else return "";
	}
	
	/**
	 * Видът спорт.
	 * При треньорските кадри се записва в самото удостоверение,
	 * а в останалите обекти е в таблицата mms_vid_sport.
	 * Ако има няколко спорта, се разделят със запетайки.
	 * @return
	 * @throws DbErrorException
	 */
	private String getVidSport() throws DbErrorException {

		if(this.typeObject.equals(DocuConstants.CODE_ZNACHENIE_JOURNAL_COACHES)) {
			return this.systemData.decodeItem(
					DocuConstants.CODE_CLASSIF_VIDOVE_SPORT, 
					this.vpisvane.getVidSport(), this.currentLang, new Date());
		}
		else {
			MMSVidSportDAO dao = new MMSVidSportDAO(MMSVidSportSC.class, this.userData);
			List<Integer> sportove = dao.findSportByTypeAndId(this.typeObject, this.idObject);
			
			List<String> list = new ArrayList<>();
			if (sportove != null && !sportove.isEmpty()) {
				for(Integer i : sportove) {
					String sport = this.systemData.decodeItem(DocuConstants.CODE_CLASSIF_VIDOVE_SPORT, i, this.currentLang, new Date());
					if(sport != null && !sport.trim().isEmpty()) {
						list.add(sport);
					}
				}
			}

			return String.join(", ", list);
			
		}
		
	}
	
	/**
	 * Длъжността.
	 * @return
	 * @throws DbErrorException
	 */
	private String getDlajnost() throws DbErrorException {
		return this.systemData.decodeItem(
				DocuConstants.CODE_CLASSIF_DLAJNOST, 
				this.vpisvane.getDlajnost(), this.currentLang, new Date());
	}
	
	
	/**
	 * Датата на документа.
	 * @return
	 * @throws DbErrorException
	 */
	private String getDocDate() throws DbErrorException {
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy г.");
		return sdf.format(this.doc.getDocDate());
	}
	
	/**
	 * Адресът на обекта. Има различна логика при различните обекти. 
	 * След като се изчете, се взима само населеното място.
	 * @return
	 * @throws DbErrorException
	 */
	private String getObektAdres() throws DbErrorException {
		String adres = "";
		
		if(this.typeObject.equals(DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORT_OBED)) {
			MMSsportObedinenieDAO dao = new MMSsportObedinenieDAO(MMSSportnoObedinenie.class, this.userData);
			MMSSportnoObedinenie obedinenie = dao.findById(this.idObject);
			String fullAdres = this.systemData.decodeItemDopInfo(DocuConstants.CODE_CLASSIF_REFERENTS, obedinenie.getIdObject(), this.currentLang, new Date());
			adres = getCityFromAdres(fullAdres);
		}
		
		else if(this.typeObject.equals(DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_FORMATIONS)) {
			MMSsportFormirovanieDAO dao = new MMSsportFormirovanieDAO(MMSsportFormirovanie.class, this.userData);
			MMSsportFormirovanie formirovanie = dao.findById(this.idObject);
			ReferentDAO referentDAO = new ReferentDAO(this.userData);
			Referent referent = referentDAO.findByCodeRef(formirovanie.getIdObject());
			String fullAdres = this.systemData.decodeItemDopInfo(DocuConstants.CODE_CLASSIF_REFERENTS, referent.getCode(), this.currentLang, new Date());
			adres = getCityFromAdres(fullAdres);
		}
		
		else if(this.typeObject.equals(DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_OBJECTS)) {
			MMSSportObektDAO dao = new MMSSportObektDAO(MMSSportObekt.class, this.userData);
			MMSSportObekt sportenObekt = dao.findById(this.idObject);
			adres = this.systemData.decodeItem(DocuConstants.CODE_CLASSIF_EKATTE, sportenObekt.getNas_mesto(), this.currentLang, new Date());
		}
		
		return adres;
	}
	
	private String getPredsedatel() throws DbErrorException {

		if(this.typeObject.equals(DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORT_OBED)) {
			MMSsportObedinenieDAO dao = new MMSsportObedinenieDAO(MMSSportnoObedinenie.class, this.userData);
			MMSSportnoObedinenie obedinenie = dao.findById(this.idObject);
			return obedinenie.getPredsedatel();
		}
		
		else if(this.typeObject.equals(DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_FORMATIONS)) {
			MMSsportFormirovanieDAO dao = new MMSsportFormirovanieDAO(MMSsportFormirovanie.class, this.userData);
			MMSsportFormirovanie formirovanie = dao.findById(this.idObject);
			return formirovanie.getPredsedatel();
		}
		
		return null;
	}
	
	/**
	 * Взима само името на населено място, без другите подробности.
	 * @param adres
	 * @return
	 */
	private String getCityFromAdres(String adres) {
		if(adres != null) {
			int i1 = adres.indexOf("гр.");
			if(i1 == -1) {
				i1 = adres.indexOf("с.");
			}
			
			if(i1 != -1) {						
				int i2 = adres.indexOf(", ", i1);
				if(i2 != -1) {
					adres = adres.substring(i1, i2);
				}
				else {
					// има само град или село...
					adres = adres.substring(i1);
				}
			}
		}
		
		return adres;
	}

	/**
	 * Статут на заявлението. Записвам стойност 'Вписан'
	 * @return
	 * @throws DbErrorException
	 */
	private String getStatut() throws DbErrorException {
		return this.systemData.decodeItem(
				DocuConstants.CODE_CLASSIF_STATUS_ZAIAVLENIE, 
				DocuConstants.CODE_ZNACHENIE_STATUS_ZAIAVLENIE_VPISAN, this.currentLang, new Date());
	}
	
	/**
	 * ЕИК на обекта. Има различна логика при различните обекти.
	 * @return
	 * @throws DbErrorException
	 */
	private String getEik() throws DbErrorException {
		String eik = "";
		
		if(this.typeObject.equals(DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORT_OBED)) {
			MMSsportObedinenieDAO dao = new MMSsportObedinenieDAO(MMSSportnoObedinenie.class, this.userData);
			MMSSportnoObedinenie obedinenie = dao.findById(this.idObject);
			ReferentDAO d = new ReferentDAO(this.userData);
			Referent r = d.findByCode(obedinenie.getIdObject(), new Date(), false);
			eik = r.getNflEik();
		}
		
		else if(this.typeObject.equals(DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_FORMATIONS)) {
			MMSsportFormirovanieDAO dao = new MMSsportFormirovanieDAO(MMSsportFormirovanie.class, this.userData);
			MMSsportFormirovanie formirovanie = dao.findById(this.idObject);
			ReferentDAO d = new ReferentDAO(this.userData);
			Referent r = d.findByCode(formirovanie.getIdObject(), new Date(), false);
			eik = r.getNflEik();
		}
		
		return eik;
	}
	
	/**
	 * Името на обекта. Има различна логика при различните обекти.
	 * @return
	 * @throws DbErrorException
	 */
	private String getName() throws DbErrorException {
		String name = "";
		
		if(this.typeObject.equals(DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORT_OBED)) {
			MMSsportObedinenieDAO dao = new MMSsportObedinenieDAO(MMSSportnoObedinenie.class, this.userData);
			MMSSportnoObedinenie obedinenie = dao.findById(this.idObject);
			ReferentDAO d = new ReferentDAO(this.userData);
			Referent r = d.findByCode(obedinenie.getIdObject(), new Date(), false);
			name = r.getRefName();
		}
		
		else if(this.typeObject.equals(DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_FORMATIONS)) {
			MMSsportFormirovanieDAO dao = new MMSsportFormirovanieDAO(MMSsportFormirovanie.class, this.userData);
			MMSsportFormirovanie formirovanie = dao.findById(this.idObject);
			ReferentDAO d = new ReferentDAO(this.userData);
			Referent r = d.findByCode(formirovanie.getIdObject(), new Date(), false);
			name = r.getRefName();
		}
		
		else if(this.typeObject.equals(DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_OBJECTS)) {
			MMSSportObektDAO dao = new MMSSportObektDAO(MMSSportObekt.class, this.userData);
			MMSSportObekt sportenObekt = dao.findById(this.idObject);
			name = sportenObekt.getName();			
		}
		
		else if(this.typeObject.equals(DocuConstants.CODE_ZNACHENIE_JOURNAL_COACHES)) {
			MMSCoachesDAO dao = new MMSCoachesDAO(MMSCoaches.class, this.userData);
			MMSCoaches coach = dao.findById(this.idObject);
			ReferentDAO d = new ReferentDAO(this.userData);
			Referent r = d.findByCode(coach.getIdObject(), new Date(), false);
			name = r.getRefName();
		}
		
		return name;
	}
	
	/**
	 * Кой е подписал документа.
	 * Лицата са записани в списък, но би трябвало да има само един-единствен, затова взимам само първия от списъка.
	 * @return
	 * @throws DbErrorException
	 */
	private String getPodpisal() throws DbErrorException {
		List<DocReferent> listReferent = this.doc.getReferentsSigned();
		String s = null;
		
		if(listReferent != null && listReferent.size() > 0 && listReferent.get(0) != null) {
			s = this.systemData.decodeItem(
					DocuConstants.CODE_CLASSIF_ADMIN_STR,
					listReferent.get(0).getCodeRef(),
					this.currentLang,
					this.doc.getDocDate());
			
			if(s != null) {
				int skoba = s.lastIndexOf("(");
				
				if(skoba >= 0) {
					s = s.substring(0, skoba).trim();
				}
			}
		}
		return s;
	}
	
	private String getPodpisalDlajnost() throws DbErrorException {
		List<DocReferent> listReferent = this.doc.getReferentsSigned();
		String s = null;
		
		if(listReferent != null && listReferent.size() > 0 && listReferent.get(0) != null) {
			s = this.systemData.decodeItem(
					DocuConstants.CODE_CLASSIF_ADMIN_STR,
					listReferent.get(0).getCodeRef(),
					this.currentLang,
					this.doc.getDocDate());
			
			if(s != null) {
				int skoba1 = s.lastIndexOf("(");
				int skoba2 = s.lastIndexOf(")");
				
				if(skoba1 >= 0 && skoba1 < skoba2) {
					s = s.substring(skoba1 + 1, skoba2).trim();
				}
				else {
					s = null;
				}
			}
		}
		return s;
	}
	
	private String getSportOrSpecDeinost() {
		if(this.vpisvane.getVidSport() == null) {
			return "вид спорт";
		}
		
		if(this.vpisvane.getVidSport() == 168
				|| this.vpisvane.getVidSport() == 169
				|| this.vpisvane.getVidSport() ==  170) {
			return "вид специализирана спортна дейност";
		}
		else {
			return "вид спорт";
		}
	}
	
	private String getDublikat() {
		if(this.isDublikat) {
			return "ДУБЛИКАТ";
		}
		else {
			return "";
		}
	}
	
	private Integer getVidSportnoNeshto() throws DbErrorException {
		if(this.typeObject.equals(DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORT_OBED)) {
			MMSsportObedinenieDAO dao = new MMSsportObedinenieDAO(MMSSportnoObedinenie.class, this.userData);
			MMSSportnoObedinenie obedinenie = dao.findById(this.idObject);
			return obedinenie.getVid();
		}
		
		else if(this.typeObject.equals(DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_FORMATIONS)) {
			MMSsportFormirovanieDAO dao = new MMSsportFormirovanieDAO(MMSsportFormirovanie.class, this.userData);
			MMSsportFormirovanie formirovanie = dao.findById(this.idObject);
			return formirovanie.getVid();
		}
		else {
			return null;
		}
	}

	
	private class BookmarkObject {
		private String name;
		private String label;
		
		public BookmarkObject(String name, String label) {
			this.name = name;
			this.label = label;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}
	}
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	
	
	public Integer getIdVpisvane() {
		return idVpisvane;
	}

	public void setIdVpisvane(Integer idVpisvane) {
		this.idVpisvane = idVpisvane;
	}

	public Integer getIdObject() {
		return idObject;
	}

	public void setIdObject(Integer idObject) {
		this.idObject = idObject;
	}

	public Integer getTypeObject() {
		return typeObject;
	}

	public void setTypeObject(Integer typeObject) {
		this.typeObject = typeObject;
	}

	public Integer getIdDoc() {
		return idDoc;
	}

	public void setIdDoc(Integer idDoc) {
		this.idDoc = idDoc;
	}

	public Integer getVidDoc() {
		return vidDoc;
	}

	public void setVidDoc(Integer vidDoc) {
		this.vidDoc = vidDoc;
	}
	
}
