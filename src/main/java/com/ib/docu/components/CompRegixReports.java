package com.ib.docu.components;

import static com.ib.system.utils.SearchUtils.isEmpty;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.component.FacesComponent;
import javax.faces.component.UINamingContainer;
import javax.faces.model.SelectItem;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.docu.system.SystemData;
import com.ib.docu.system.UserData;
import com.ib.indexui.system.IndexUIbean;
import com.ib.indexui.utils.JSFUtils;
import com.ib.system.exceptions.DbErrorException;
import com.ib.system.utils.DateUtils;
import com.ib.system.utils.StringUtils;
import com.ib.system.utils.ValidationUtils;

import bg.government.regixclient.RegixClient;
import bg.government.regixclient.RegixClientException;
import bg.government.regixclient.requests.av.tr.ActualStateRequestType;
import bg.government.regixclient.requests.av.tr.ActualStateResponseType;
import bg.government.regixclient.requests.av.tr.CompanyParticipationType.Company;
import bg.government.regixclient.requests.av.tr.DetailType;
import bg.government.regixclient.requests.av.tr.SearchParticipationInCompaniesRequestType;
import bg.government.regixclient.requests.av.tr.SearchParticipationInCompaniesResponseType;
import bg.government.regixclient.requests.av.tr.TROperation;
import bg.government.regixclient.requests.grao.GraoOperation;
import bg.government.regixclient.requests.grao.nbd.PersonDataRequestType;
import bg.government.regixclient.requests.grao.nbd.PersonDataResponseType;
import bg.government.regixclient.requests.grao.pna.PermanentAddressRequestType;
import bg.government.regixclient.requests.grao.pna.PermanentAddressResponseType;
import bg.government.regixclient.requests.grao.pna.TemporaryAddressRequestType;
import bg.government.regixclient.requests.grao.pna.TemporaryAddressResponseType;
import bg.government.regixclient.requests.napoo.DocumentsByStudentResponse;
import bg.government.regixclient.requests.napoo.NapooOperation;
import bg.government.regixclient.requests.napoo.StudentDocumentRequestType;
import bg.government.regixclient.requests.nra.EikTypeTypeRequest;
import bg.government.regixclient.requests.nra.IdentityTypeRequest;
import bg.government.regixclient.requests.nra.NraOperation;
import bg.government.regixclient.requests.nra.ObligationRequest;
import bg.government.regixclient.requests.nra.ObligationResponse;
import bg.government.regixclient.requests.rdso.CertifiedDocumentsSearchType;
import bg.government.regixclient.requests.rdso.CertifiedDocumentsType;
import bg.government.regixclient.requests.rdso.DiplomaDocumentsType;
import bg.government.regixclient.requests.rdso.DiplomaSearchType;
import bg.government.regixclient.requests.rdso.IdentifierType;
import bg.government.regixclient.requests.rdso.RdsoOperation;

/** */
@FacesComponent(value = "compRegixReports", createTag = true)
public class CompRegixReports extends UINamingContainer {
	
	private enum PropertyKeys {
		 VIDSPR,SPRLIST, EGNEIK, RESULT, IDENT, PRAG, DOCNUM, IDENTLIST
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(CompRegixReports.class);
	public static final String	UIBEANMESSAGES = "ui_beanMessages";
	public static final String	BEANMESSAGES = "beanMessages";
	public static final String  MSGPLSINS = "general.pleaseInsert";
	public static final String  ERRDATABASEMSG = "general.errDataBaseMsg";
	public static final String	LABELS = "labels";
	private static final String MSGVALIDEIK = "refCorr.msgValidEik";
	private static final String MSGVALIDEGN = "refCorr.msgValidEgn"; 
	private static final String B_COLON_SPACE = ":</b> "; 
	private static final String B_OPEN = "<b>"; 
	private static final String BR_BR = "<br/><br/>"; 
	private static final String BR = "<br/>"; 
	private static final String FLOOR ="regixReport.floor";
	private static final String DOCU_LOGIN ="docu.login";

	private SystemData systemData = null;
	private String errMsg = null;
	private UserData userData	= null;
	private Date dateClassif	= null;
	private StringBuilder sb;
	private boolean disableSearch = false;
	RegixClient client;
	
	
	public void initCmp() {		
		
		//boolean modal = (Boolean) getAttributes().get("modal"); // обработката е в модален диалог (true) или не (false)
		setSprList(new ArrayList<>());
		
		SelectItem item = new SelectItem(1, IndexUIbean.getMessageResourceString(LABELS, "compRegix.spravkaTR") );
		getSprList().add(item);	
		item = new SelectItem(2,IndexUIbean.getMessageResourceString(LABELS, "comRegix.sprEgnTR"));
		getSprList().add(item);	
		
		try {
			if ("true".equals(getSystemData().getSettingsValue("REGIX_ESGRAON_ACTIVE"))) {
				// само тогава трябва да се вижда справка: Справка по ЕГН в Национална база „Население“
				item = new SelectItem(3,IndexUIbean.getMessageResourceString(LABELS, "compRegix.sprNaselenie"));
				getSprList().add(item);	
			}
		} catch (DbErrorException e) {
			LOGGER.error("Грешка при работа с базата! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
		}
			
		item = new SelectItem(4,IndexUIbean.getMessageResourceString(LABELS, "compRegix.sprZL"));
		getSprList().add(item);
//20.04.2023 до тази справка няма достъп. добавят се две нови
//		item = new SelectItem(5,IndexUIbean.getMessageResourceString(LABELS, "compRegix.sprMON"));
//		getSprList().add(item);		
			
		item = new SelectItem(5,IndexUIbean.getMessageResourceString(LABELS, "compRegix.sprDiploma"));
		getSprList().add(item);		
		
		item = new SelectItem(6,IndexUIbean.getMessageResourceString(LABELS, "compRegix.sprDocs"));
		getSprList().add(item);	
	}
	
	public void actionSearch() {
		
		sb= new StringBuilder();
		errMsg = "";
		if(getVidSpr()!=null) {
			if(getVidSpr().equals(1)) {
				
				if(getEgnEik()==null || "".equals(getEgnEik())) {
					errMsg += IndexUIbean.getMessageResourceString(BEANMESSAGES, MSGVALIDEIK)+ "<br/>";
//					JSFUtils.addMessage(this.getClientId(FacesContext.getCurrentInstance()) + ":eik",
//							FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(BEANMESSAGES, MSGVALIDEIK));
					//JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(BEANMESSAGES, MSGVALIDEIK));
				}else if(!ValidationUtils.isValidBULSTAT(getEgnEik())){
					errMsg += IndexUIbean.getMessageResourceString(BEANMESSAGES, MSGVALIDEIK) + "<br/>";
				}else {
					actionSearchActualState();
				}
			}else if(getVidSpr().equals(2)){
				if(getEgnEik()==null || "".equals(getEgnEik())) {
					errMsg += IndexUIbean.getMessageResourceString(BEANMESSAGES, "refCorr.msgValidEgn")+ "<br/>";
//					JSFUtils.addMessage(this.getClientId(FacesContext.getCurrentInstance()) + ":egn",
//							FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(BEANMESSAGES, "refCorr.msgValidEgn"));
					//JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(BEANMESSAGES, "refCorr.msgValidEgn"));
				}else if(!ValidationUtils.isValidEGN(getEgnEik())){
					errMsg += IndexUIbean.getMessageResourceString(BEANMESSAGES, MSGVALIDEGN) + "<br/>";
				}else {
					actionSearchParticipationInCompanies();
				}
			}else if(getVidSpr().equals(3)){
				if(getEgnEik()==null || "".equals(getEgnEik())) {
					errMsg += IndexUIbean.getMessageResourceString(BEANMESSAGES, "refCorr.msgValidEgn")+ "<br/>";
//					JSFUtils.addMessage(this.getClientId(FacesContext.getCurrentInstance()) + ":egn",
//							FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(BEANMESSAGES, "refCorr.msgValidEgn"));
					//JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(BEANMESSAGES, "refCorr.msgValidEgn"));
				}else if(!ValidationUtils.isValidEGN(getEgnEik())){
					errMsg += IndexUIbean.getMessageResourceString(BEANMESSAGES, MSGVALIDEGN) + "<br/>";
				}else {					
					actionSearchPersonData();
				}
			}else if(getVidSpr().equals(4)&& !checkDataSprObligations()){						
					actionSearchObligation();
			}else if(getVidSpr().equals(5) && !checkDataSprSrednoObrazovanie()){
							
				searchDiploma();
				
			}else if(getVidSpr().equals(6) && !checkDataSprIzdDoc()){
							
				searchDocLice();			
			}
		}else {
			errMsg += IndexUIbean.getMessageResourceString(BEANMESSAGES, "compRegix.izborSpr");
//			JSFUtils.addMessage(this.getClientId(FacesContext.getCurrentInstance()) + ":vidSpr:аutoCompl_input",
//					FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(BEANMESSAGES, "compRegix.izborSpr"));
			//JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(BEANMESSAGES, "compRegix.izborSpr"));	
		}
	}
		//проверка за  Справка за наличие/ липса на задължения
		private boolean checkDataSprObligations() {
			
			boolean flag = false;
			if(getEgnEik()==null ||  "".equals(getEgnEik())){
				errMsg += IndexUIbean.getMessageResourceString(BEANMESSAGES, "compRegix.intertEgnEikLnch")+ "<br/>";
				//JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(BEANMESSAGES, "compRegix.insertEgnLnch"));
				flag = true;
			}else {
				if("1".equals(getIdent()) && !ValidationUtils.isValidEGN(getEgnEik())) {					
					errMsg += IndexUIbean.getMessageResourceString(BEANMESSAGES, MSGVALIDEGN) + "<br/>";
					flag = true;
				}else if("2".equals(getIdent())&& !ValidationUtils.isValidLNCH(getEgnEik())) {
					errMsg +=IndexUIbean.getMessageResourceString(BEANMESSAGES, "refCorr.msgValidLnch")+ "<br/>";	
					flag = true;
				}else if("3".equals(getIdent())&& !ValidationUtils.isValidBULSTAT(getEgnEik())) {
					errMsg += IndexUIbean.getMessageResourceString(BEANMESSAGES, MSGVALIDEIK) + "<br/>";
					flag = true;
				}
			}
				return flag;
	   }
			
	//проверка за  Справка за диплома за средно образование на определено лице
	private boolean checkDataSprSrednoObrazovanie() {
		
		boolean flag = false;
		if(getEgnEik()==null ||  "".equals(getEgnEik())){
			errMsg += IndexUIbean.getMessageResourceString(BEANMESSAGES, "compRegix.insertEgnLnch")+ "<br/>";
			//JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(BEANMESSAGES, "compRegix.insertEgnLnch"));
			flag = true;
		}else {
			if("1".equals(getIdent()) && !ValidationUtils.isValidEGN(getEgnEik())) {					
				errMsg += IndexUIbean.getMessageResourceString(BEANMESSAGES, MSGVALIDEGN) + "<br/>";
				flag = true;
			}else if("2".equals(getIdent())&& !ValidationUtils.isValidLNCH(getEgnEik())) {
				errMsg +=IndexUIbean.getMessageResourceString(BEANMESSAGES, "refCorr.msgValidLnch")+ "<br/>";	
				flag = true;
			}
		}
		
		if(getDocNum()==null || "".equals(getDocNum())) {
			errMsg += IndexUIbean.getMessageResourceString(BEANMESSAGES, "compRegix.insertDocNum")+ "<br/>";
			//JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(BEANMESSAGES, "compRegix.insertDocNum"));
			flag = true;						
		}else if ( !StringUtils.isNumeric(getDocNum())){ //за тази справка се иска само цифровата част на номера на документа
			errMsg += IndexUIbean.getMessageResourceString(BEANMESSAGES, "compRegix.onlyDig")+ "<br/>";
			//JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(BEANMESSAGES, "compRegix.onlyDig"));
			flag = true;
		}
		return flag;
	}
	
	//проверка за Справка за издаден документ на лице по подаден идентификатор и идентификационен (или регистрационен) номер
		private boolean checkDataSprIzdDoc() {
			
			boolean flag = false;
			if(getEgnEik()==null ||  "".equals(getEgnEik())){
				errMsg += IndexUIbean.getMessageResourceString(BEANMESSAGES, "compRegix.insertEgnLnch")+ "<br/>";
				//JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(BEANMESSAGES, "compRegix.insertEgnLnch"));
				flag = true;
			}else {
				if("1".equals(getIdent()) && !ValidationUtils.isValidEGN(getEgnEik())) {					
					errMsg += IndexUIbean.getMessageResourceString(BEANMESSAGES, MSGVALIDEGN) + "<br/>";
					flag = true;
				}else if("2".equals(getIdent())&& !ValidationUtils.isValidLNCH(getEgnEik())) {
					errMsg +=IndexUIbean.getMessageResourceString(BEANMESSAGES, "refCorr.msgValidLnch")+ "<br/>";
					flag = true;
				}
			}
			
			if(getDocNum()==null || "".equals(getDocNum())) {	
				errMsg += IndexUIbean.getMessageResourceString(BEANMESSAGES, "compRegix.insertRegNom")+ "<br/>";
				//JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(BEANMESSAGES, "compRegix.insertRegNom"));
				flag = true;						
			}
			return flag;
		}
		
	private void actionSearchActualState() {
		try {
			client = getSystemData().getRegixClient();
			ActualStateRequestType request = new ActualStateRequestType();
			request.setUIC(getEgnEik());

			ActualStateResponseType response = (ActualStateResponseType) this.client.executeOperation(TROperation.GET_ACTUAL_STATE, request);
			if (response != null) {
				
				if ( null!=response.getStatus()) {	
					
					sb.append(B_OPEN).append(IndexUIbean.getMessageResourceString(LABELS, "regixReport.statusPartida")).append(B_COLON_SPACE).append(response.getStatus()).append(BR_BR);				
					
				}
				
				sb.append(B_OPEN).append(IndexUIbean.getMessageResourceString(LABELS, "regixReport.mainCirc")).append("</b><br/><br/>");
				
				if(response.getCompany()!=null&&!"".equals(response.getCompany())) {
					sb.append(B_OPEN).append(IndexUIbean.getMessageResourceString(LABELS, "regixReport.firma")).append(B_COLON_SPACE).append(response.getCompany()).append(BR);	
				}
				if(response.getLegalForm()!=null) {
					sb.append(B_OPEN).append(IndexUIbean.getMessageResourceString(LABELS, "regixReport.pravnaForma")).append(B_COLON_SPACE).append(response.getLegalForm().getLegalFormName()).append(BR);	
				}
				
				if(response.getTransliteration()!=null) {
					sb.append(B_OPEN).append(IndexUIbean.getMessageResourceString(LABELS, "regixReport.latinNameFirma")).append(B_COLON_SPACE).append(response.getTransliteration()).append(BR);	
				}
				
				//Седалище и адрес на управление
				if(response.getSeat()!=null) {
					if(response.getSeat().getAddress()!=null) {
						
						sb.append(B_OPEN).append(IndexUIbean.getMessageResourceString(LABELS, "regixReport.sedalishte")).append(B_COLON_SPACE);
						
						if(response.getSeat().getAddress().getCountry()!=null) {
							sb.append(", ").append(response.getSeat().getAddress().getCountry());
						}
						if(response.getSeat().getAddress().getDistrict()!=null) {
							sb.append(", ").append(IndexUIbean.getMessageResourceString(LABELS,"global.oblast")).append(" ").append(response.getSeat().getAddress().getDistrict());
						}
						if(response.getSeat().getAddress().getMunicipality()!=null) {
							sb.append(", ").append(IndexUIbean.getMessageResourceString(LABELS,"regixReport.obshtina")).append(" ").append(response.getSeat().getAddress().getMunicipality());
						}
						
						if(response.getSeat().getAddress().getSettlement()!=null &&!"".equals(response.getSeat().getAddress().getSettlement())) {
							sb.append(", ").append(IndexUIbean.getMessageResourceString(LABELS,"regixReport.nasMiasto")).append(" ").append(response.getSeat().getAddress().getSettlement());
						}
						
						if(response.getSeat().getAddress().getArea()!=null &&"".equals(response.getSeat().getAddress().getArea())) {
							sb.append(", ").append(IndexUIbean.getMessageResourceString(LABELS,"regixReport.raion")).append(" ").append(response.getSeat().getAddress().getArea());
						}
						if(response.getSeat().getAddress().getForeignPlace()!=null && !"".equals(response.getSeat().getAddress().getForeignPlace())) {
							sb.append(", ").append(response.getSeat().getAddress().getForeignPlace());
						}
						
						if(response.getSeat().getAddress().getHousingEstate()!=null && !"".equals(response.getSeat().getAddress().getHousingEstate())) {
							sb.append(", ").append(IndexUIbean.getMessageResourceString(LABELS,"regixReport.housingEstate")).append(" ").append(response.getSeat().getAddress().getHousingEstate());
						}
						
						if(response.getSeat().getAddress().getStreet()!=null && !"".equals(response.getSeat().getAddress().getStreet())) {
							sb.append(", ").append(IndexUIbean.getMessageResourceString(LABELS,"regixReport.ulitsa")).append(" ").append(response.getSeat().getAddress().getStreet());
						}
						
						if(response.getSeat().getAddress().getStreetNumber()!=null && !"".equals(response.getSeat().getAddress().getStreetNumber())) {
							sb.append(", ").append(IndexUIbean.getMessageResourceString(LABELS,"procDefList.nomProc")).append(" ").append(response.getSeat().getAddress().getStreetNumber());
						}
						
						if(response.getSeat().getAddress().getBlock()!=null && !"".equals(response.getSeat().getAddress().getBlock())) {
							sb.append(", ").append(IndexUIbean.getMessageResourceString(LABELS,"regixReport.blok")).append(" ").append(response.getSeat().getAddress().getBlock());
						}
						
						if(response.getSeat().getAddress().getEntrance()!=null && !"".equals(response.getSeat().getAddress().getEntrance())) {
							sb.append(", ").append(IndexUIbean.getMessageResourceString(LABELS,DOCU_LOGIN)).append(" ").append(response.getSeat().getAddress().getEntrance());
						}
						
						if(response.getSeat().getAddress().getFloor()!=null && !"".equals(response.getSeat().getAddress().getFloor())) {
							sb.append(", ").append(IndexUIbean.getMessageResourceString(LABELS,FLOOR)).append(" ").append(response.getSeat().getAddress().getFloor());
						}
						
						if(response.getSeat().getAddress().getApartment()!=null && !"".equals(response.getSeat().getAddress().getApartment())) {
							sb.append(", ").append(IndexUIbean.getMessageResourceString(LABELS,"regixReport.apartment")).append(" ").append(response.getSeat().getAddress().getApartment());
						}
						
					}
									
					if(response.getSeat().getContacts()!=null){
						if(response.getSeat().getContacts().getPhone()!=null && !"".equals(response.getSeat().getContacts().getPhone())) {
							sb.append(", ").append(IndexUIbean.getMessageResourceString(LABELS,"admStruct.telefon")).append(" ").append(response.getSeat().getContacts().getPhone());
						}
						
						if(response.getSeat().getContacts().getFax()!=null && !"".equals(response.getSeat().getContacts().getFax())) {
							sb.append(", ").append(IndexUIbean.getMessageResourceString(LABELS,"regixReport.fax")).append(" ").append(response.getSeat().getContacts().getFax());
						}
						
						if(response.getSeat().getContacts().getEMail()!=null && !"".equals(response.getSeat().getContacts().getEMail())) {
							sb.append(", ").append(IndexUIbean.getMessageResourceString(LABELS,"regixReport.email")).append(" ").append(response.getSeat().getContacts().getEMail());
						}
						
						if(response.getSeat().getContacts().getURL()!=null && !"".equals(response.getSeat().getContacts().getURL())) {
							sb.append(", ").append(IndexUIbean.getMessageResourceString(LABELS,"regixReport.url")).append(" ").append(response.getSeat().getContacts().getURL());
						}
					}
				}
				
				//Адрес за кореспонденция
				if(response.getSeatForCorrespondence()!=null) {
					
						sb.append(BR);
						
						sb.append(B_OPEN).append(IndexUIbean.getMessageResourceString(LABELS, "regixReport.adressCoresp")).append(B_COLON_SPACE);
						
						if(response.getSeatForCorrespondence().getCountry()!=null) {
							sb.append(", ").append(response.getSeatForCorrespondence().getCountry());
						}
						if(response.getSeatForCorrespondence().getDistrict()!=null) {
							sb.append(", ").append(IndexUIbean.getMessageResourceString(LABELS,"global.oblast")).append(" ").append(response.getSeatForCorrespondence().getDistrict());
						}
						if(response.getSeatForCorrespondence().getMunicipality()!=null) {
							sb.append(", ").append(IndexUIbean.getMessageResourceString(LABELS,"regixReport.obshtina")).append(" ").append(response.getSeatForCorrespondence().getMunicipality());
						}
						
						if(response.getSeatForCorrespondence().getSettlement()!=null &&!"".equals(response.getSeatForCorrespondence().getSettlement())) {
							sb.append(", ").append(IndexUIbean.getMessageResourceString(LABELS,"regixReport.nasMiasto")).append(" ").append(response.getSeatForCorrespondence().getSettlement());
						}
						
						if(response.getSeatForCorrespondence().getArea()!=null &&"".equals(response.getSeatForCorrespondence().getArea())) {
							sb.append(", ").append(IndexUIbean.getMessageResourceString(LABELS,"regixReport.raion")).append(" ").append(response.getSeatForCorrespondence().getArea());
						}
						if(response.getSeatForCorrespondence().getForeignPlace()!=null && !"".equals(response.getSeatForCorrespondence().getForeignPlace())) {
							sb.append(", ").append(response.getSeatForCorrespondence().getForeignPlace());
						}
						
						if(response.getSeatForCorrespondence().getHousingEstate()!=null && !"".equals(response.getSeatForCorrespondence().getHousingEstate())) {
							sb.append(", ").append(IndexUIbean.getMessageResourceString(LABELS,"regixReport.housingEstate")).append(" ").append(response.getSeatForCorrespondence().getHousingEstate());
						}
						
						if(response.getSeatForCorrespondence().getStreet()!=null && !"".equals(response.getSeatForCorrespondence().getStreet())) {
							sb.append(", ").append(IndexUIbean.getMessageResourceString(LABELS,"regixReport.ulitsa")).append(" ").append(response.getSeatForCorrespondence().getStreet());
						}
						
						if(response.getSeatForCorrespondence().getStreetNumber()!=null && !"".equals(response.getSeatForCorrespondence().getStreetNumber())) {
							sb.append(", ").append(IndexUIbean.getMessageResourceString(LABELS,"procDefList.nomProc")).append(" ").append(response.getSeatForCorrespondence().getStreetNumber());
						}
						
						if(response.getSeatForCorrespondence().getBlock()!=null && !"".equals(response.getSeatForCorrespondence().getBlock())) {
							sb.append(", ").append(IndexUIbean.getMessageResourceString(LABELS,"regixReport.blok")).append(" ").append(response.getSeatForCorrespondence().getBlock());
						}
						
						if(response.getSeatForCorrespondence().getEntrance()!=null && !"".equals(response.getSeatForCorrespondence().getEntrance())) {
							sb.append(", ").append(IndexUIbean.getMessageResourceString(LABELS,DOCU_LOGIN)).append(" ").append(response.getSeatForCorrespondence().getEntrance());
						}
						
						if(response.getSeatForCorrespondence().getFloor()!=null && !"".equals(response.getSeatForCorrespondence().getFloor())) {
							sb.append(", ").append(IndexUIbean.getMessageResourceString(LABELS,FLOOR)).append(" ").append(response.getSeatForCorrespondence().getFloor());
						}
						
						if(response.getSeatForCorrespondence().getApartment()!=null && !"".equals(response.getSeatForCorrespondence().getApartment())) {
							sb.append(", ").append(IndexUIbean.getMessageResourceString(LABELS,"regixReport.apartment")).append(" ").append(response.getSeatForCorrespondence().getApartment());
						}

							
				}
				
				if(response.getSubjectOfActivity()!=null) {
					
					sb.append(BR);
									
					if(response.getSubjectOfActivity().getSubject()!=null) {
						sb.append(B_OPEN).append(IndexUIbean.getMessageResourceString(LABELS, "regixReport.predmetDeinost")).append(B_COLON_SPACE).append(response.getSubjectOfActivity().getSubject()).append(BR);
					}
					
					if(response.getSubjectOfActivityNKID().getNKIDname()!=null) {
						sb.append(B_OPEN).append(IndexUIbean.getMessageResourceString(LABELS, "regixReport.deinost")).append(B_COLON_SPACE).append(response.getSubjectOfActivityNKID().getNKIDname()).append(BR);
					}
					
					if(response.getWayOfManagement()!=null) {
						sb.append(B_OPEN).append(IndexUIbean.getMessageResourceString(LABELS, "regixReport.wayOfManag")).append(B_COLON_SPACE).append(response.getWayOfManagement()).append(BR);
					}
					
					if(response.getWayOfRepresentation()!=null) {
						sb.append(B_OPEN).append(IndexUIbean.getMessageResourceString(LABELS, "regixReport.nachinPredst")).append(B_COLON_SPACE).append(response.getWayOfRepresentation()).append(BR);
					}
					
					if (response.getDetails() != null 
							&& response.getDetails().getDetail() != null && !response.getDetails().getDetail().isEmpty()) {
						
						sb.append(B_OPEN).append(IndexUIbean.getMessageResourceString(LABELS, "regixReport.predst")).append(B_COLON_SPACE);
						for (DetailType dt : response.getDetails().getDetail()) {
							if ("10а".equalsIgnoreCase(dt.getFieldCode()) && dt.getSubject() != null) {
								sb.append(dt.getSubject().getName()).append(BR);
								
								break;
							}
						}
						sb.append(BR);
					}
					
					
					sb.append(B_OPEN).append(IndexUIbean.getMessageResourceString(LABELS, "regixReport.savet")).append(B_COLON_SPACE);
					if(response.getBoardOfDirectorsMandate()!=null ) {
						
						if(response.getBoardOfDirectorsMandate().getType()!=null) { 
							sb.append(response.getBoardOfDirectorsMandate().getType());
						}
						if(response.getBoardOfDirectorsMandate().getMandateValue()!=null) {
							sb.append(" ").append(response.getBoardOfDirectorsMandate().getMandateValue());
						}
						
						sb.append(BR);
					}
					
				}
				
				
					
				sb.append(BR).append(B_OPEN).append(IndexUIbean.getMessageResourceString(LABELS, "regixReport.kapital")).append("</b><br/> ");
					
				if(response.getFunds()!=null && response.getFunds().getValue()!=null) {
					sb.append(B_OPEN).append(IndexUIbean.getMessageResourceString(LABELS, "regixReport.razmer")).append(B_COLON_SPACE).append(response.getFunds().getValue()).append(" ");
					if(response.getFunds().getEuro()!=null) {
						sb.append(response.getFunds().getEuro()).append(BR);
					}						
				}
					
				if(response.getDepositedFunds().getValue()!=null && response.getDepositedFunds().getValue()!=null) {
					sb.append(B_OPEN).append(IndexUIbean.getMessageResourceString(LABELS, "regixReport.vnesenKapital")).append(B_COLON_SPACE).append(response.getDepositedFunds().getValue()).append(" ");
					if(response.getDepositedFunds().getEuro()!=null) {
						sb.append(response.getDepositedFunds().getEuro()).append(BR);
					}
				}
									
//					if(response.getShares()!=null) {
//						sb.append(B_OPEN).append(IndexUIbean.getMessageResourceString(LABELS, "regixReport.akcii")).append(B_COLON_SPACE).append(response.getShares()).append(" ")
//						.append(response.getDepositedFunds().getValue()).append(BR);
//			}
				
				setResult(sb.toString());	
			}else {
				//JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, IndexUIbean.getMessageResourceString(BEANMESSAGES, "compRegix.noData"));
				errMsg = IndexUIbean.getMessageResourceString(BEANMESSAGES, "compRegix.noData");
			}
			   
		} catch (RegixClientException e) {
			LOGGER.error("Грешка при зареждане на Regix client! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
		} catch (DbErrorException e) {
			LOGGER.error("Грешка при работа с базата! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
		}
	}
	
	private void actionSearchParticipationInCompanies(){
		try {
			client = getSystemData().getRegixClient();
			SearchParticipationInCompaniesRequestType request = new SearchParticipationInCompaniesRequestType();
			request.setEGN(getEgnEik());

			SearchParticipationInCompaniesResponseType response = (SearchParticipationInCompaniesResponseType) this.client.executeOperation(TROperation.PERSON_IN_COMPANIES_SEARCH, request);
			
			if(response!=null &&response.isIsFound()) {
				
				if(response.getPersonInformation()!=null) {
					sb.append(B_OPEN).append(IndexUIbean.getMessageResourceString(LABELS, "regixReport.personData")).append(":</b><br/>")
					.append(response.getPersonInformation().getName()).append("<br/><br/>");
				}
														
				if(response.getCompanyParticipation()!=null&&response.getCompanyParticipation().getCompany()!=null) {
					sb.append("<table border=\"1\"<tr><td><b>").append(IndexUIbean.getMessageResourceString(LABELS, "regixReport.tardovskoDr")).
					append("</b></td><td><b>").append(IndexUIbean.getMessageResourceString(LABELS, "admStruct.eik")).append("</b></td><td><b>").append(IndexUIbean.getMessageResourceString(LABELS, "regixReport.role"))
					.append("</b></td></tr>");
					for(Company item:response.getCompanyParticipation().getCompany()) {
						sb.append("<tr><td>").append(item.getCompanyName()).append(" ").append("</td><td>").append(item.getEIK()).append(" ")
						.append("</td><td>").append(item.getFields().getField()).append(" ").append("</td></tr>");
					}
					sb.append("</table>");
				}
			}else {
				//JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, IndexUIbean.getMessageResourceString(BEANMESSAGES, "compRegix.noData"));	
				errMsg = IndexUIbean.getMessageResourceString(BEANMESSAGES, "compRegix.noData");
			}
			setResult(sb.toString());
		}  catch (RegixClientException e) {
			LOGGER.error("Грешка при зареждане на Regix client! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
		} catch (DbErrorException e) {
			LOGGER.error("Грешка при работа с базата! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
		}
	}
	
	private void actionSearchPersonData() {
		try {
			client = getSystemData().getRegixClient();
			PersonDataRequestType request = new PersonDataRequestType();
			request.setEGN(getEgnEik());
			PersonDataResponseType response = (PersonDataResponseType) client.executeOperation(GraoOperation.PERSON_DATA_SEARCH, request);
			if (response != null) {
				
				if ( null!=response.getPersonNames()) {	
					
					sb.append(B_OPEN).append(IndexUIbean.getMessageResourceString(LABELS, "admStruct.names")).append(B_COLON_SPACE).append(response.getPersonNames().getFirstName()).append(" ")
					.append(response.getPersonNames().getSurName()).append(" ")
					.append(response.getPersonNames().getFamilyName()).append(BR);				
					
				}
				if(response.getAlias()!=null&&!"".equals(response.getAlias())) {
					sb.append(B_OPEN).append(IndexUIbean.getMessageResourceString(LABELS, "regixReport.psevdonim")).append(B_COLON_SPACE).append(response.getAlias()).append(BR);	
				}
				
				if ( null!=response.getLatinNames()) {	
					
					sb.append(B_OPEN).append(IndexUIbean.getMessageResourceString(LABELS, "refCorr.nameLatinFL")).append(B_COLON_SPACE).append(response.getLatinNames().getFirstName()).append(" ")
					.append(response.getLatinNames().getSurName()).append(" ")
					.append(response.getLatinNames().getFamilyName()).append(BR);				
					
				}
			
				if(response.getGender().getGenderName()!=null) {
					sb.append(B_OPEN).append(IndexUIbean.getMessageResourceString(LABELS, "regixReport.gender")).append(B_COLON_SPACE).append(response.getGender().getGenderName()).append(BR);
				}
				
				if(response.getBirthDate()!=null) { 
					sb.append(B_OPEN).append(IndexUIbean.getMessageResourceString(LABELS, "regixReport.birthDate")).append(B_COLON_SPACE).append(DateUtils.printDate(DateUtils.toDate(response.getBirthDate()))).append(BR);
				}
				

				if(response.getPlaceBirth()!=null) { 
					sb.append(B_OPEN).append(IndexUIbean.getMessageResourceString(LABELS, "regixReport.placeBirth")).append(B_COLON_SPACE).append(response.getPlaceBirth()).append(BR);
				}
				
				if(response.getNationality()!=null) { 
					sb.append(B_OPEN).append(IndexUIbean.getMessageResourceString(LABELS, "regixReport.nationality")).append(B_COLON_SPACE).append(response.getNationality().getNationalityName()).append(BR);
				}

				if(response.getDeathDate()!=null) {
					sb.append(B_OPEN).append(IndexUIbean.getMessageResourceString(LABELS, "regixReport.death")).append(B_COLON_SPACE).append(DateUtils.printDate(DateUtils.toDate(response.getDeathDate()))).append("<br/><br/>");
				}
								
				
				PermanentAddressRequestType requestPermAdr = new PermanentAddressRequestType();
				requestPermAdr.setEGN(getEgnEik());
				requestPermAdr.setSearchDate(toGregorianCalendar(new Date()));
				PermanentAddressResponseType responsePermAdr = (PermanentAddressResponseType) client.executeOperation(GraoOperation.PERMANENT_ADDRESS_SEARCH, requestPermAdr);
				if(responsePermAdr != null) {
					sb.append(B_OPEN).append(IndexUIbean.getMessageResourceString(LABELS, "regixReport.permanentAdress")).append(B_COLON_SPACE).append(BR);
					if(responsePermAdr.getDistrictName()!=null) {
						sb.append(B_OPEN).append(IndexUIbean.getMessageResourceString(LABELS,"global.oblast")).append(B_COLON_SPACE).append(responsePermAdr.getDistrictName()).append(BR);
					}
					if(responsePermAdr.getMunicipalityName()!=null) {
						sb.append(B_OPEN).append(IndexUIbean.getMessageResourceString(LABELS,"regixReport.obshtina")).append(B_COLON_SPACE).append(responsePermAdr.getMunicipalityName()).append(BR);
					}
					
					if(responsePermAdr.getSettlementName()!=null) {
						sb.append(B_OPEN).append(IndexUIbean.getMessageResourceString(LABELS,"regixReport.nasMiasto")).append(B_COLON_SPACE).append(responsePermAdr.getSettlementName()).append(BR);
					}
					
					if(responsePermAdr.getCityArea()!=null) {
						sb.append(B_OPEN).append(IndexUIbean.getMessageResourceString(LABELS,"regixReport.raion")).append(B_COLON_SPACE).append(responsePermAdr.getCityArea()).append(BR);
					}
					if(responsePermAdr.getCityArea()!=null) {
						sb.append(B_OPEN).append(IndexUIbean.getMessageResourceString(LABELS,"dvijenie.adres")).append(B_COLON_SPACE);
					}
					
					if(responsePermAdr.getLocationName()!=null && !"".equals(responsePermAdr.getLocationName())) {
						sb.append(responsePermAdr.getLocationName()).append(" ");
					}
					
					if(responsePermAdr.getBuildingNumber()!=null) {
						sb.append(IndexUIbean.getMessageResourceString(LABELS,"tasks.nomer")).append(": ").append(responsePermAdr.getBuildingNumber()).append(" ");
					}
					
					if(responsePermAdr.getEntrance()!=null) {
						sb.append(IndexUIbean.getMessageResourceString(LABELS,DOCU_LOGIN)).append(": ").append(responsePermAdr.getEntrance()).append(" ");
					}
					
					if(responsePermAdr.getFloor()!=null) {
						sb.append(IndexUIbean.getMessageResourceString(LABELS,FLOOR)).append(": ").append(responsePermAdr.getFloor()).append(" ");
					}
					
					if(responsePermAdr.getApartment()!=null) {
						sb.append(IndexUIbean.getMessageResourceString(LABELS,"regixReport.apartment")).append(": ").append(responsePermAdr.getApartment()).append(BR);
					}
					
					if(responsePermAdr.getFromDate()!=null) {
						sb.append(B_OPEN).append(IndexUIbean.getMessageResourceString(LABELS,"regixReport.fromDate")).append(B_COLON_SPACE).append(DateUtils.printDate(DateUtils.toDate(responsePermAdr.getFromDate()))).append("<br/><br/>");
					}
				} 
				
				
				TemporaryAddressRequestType requestTempAdr = new TemporaryAddressRequestType();
				requestTempAdr.setEGN(getEgnEik());
				requestTempAdr.setSearchDate(toGregorianCalendar(new Date()));
				TemporaryAddressResponseType responseTempAdr = (TemporaryAddressResponseType) client.executeOperation(GraoOperation.TEMPORARY_ADDRESS_SEARCH, requestTempAdr);
				if(responseTempAdr != null) {
					sb.append(B_OPEN).append(IndexUIbean.getMessageResourceString(LABELS, "regixReport.tempAdr")).append(B_COLON_SPACE).append(BR);
					if(responseTempAdr.getDistrictName()!=null) {
						sb.append(B_OPEN).append(IndexUIbean.getMessageResourceString(LABELS,"global.oblast")).append(B_COLON_SPACE).append(responseTempAdr.getDistrictName()).append(BR);
					}
					if(responseTempAdr.getMunicipalityName()!=null) {
						sb.append(B_OPEN).append(IndexUIbean.getMessageResourceString(LABELS,"regixReport.obshtina")).append(B_COLON_SPACE).append(responseTempAdr.getMunicipalityName()).append(BR);
					}
					
					if(responseTempAdr.getSettlementName()!=null) {
						sb.append(B_OPEN).append(IndexUIbean.getMessageResourceString(LABELS,"regixReport.nasMiasto")).append(B_COLON_SPACE).append(responseTempAdr.getSettlementName()).append(BR);
					}
					
					if(responseTempAdr.getCityArea()!=null) {
						sb.append(B_OPEN).append(IndexUIbean.getMessageResourceString(LABELS,"regixReport.raion")).append(B_COLON_SPACE).append(responseTempAdr.getCityArea()).append(BR);
					}
					if(responseTempAdr.getCityArea()!=null) {
						sb.append(B_OPEN).append(IndexUIbean.getMessageResourceString(LABELS,"dvijenie.adres")).append(B_COLON_SPACE);
					}
					
					if(responseTempAdr.getLocationName()!=null && !"".equals(responseTempAdr.getLocationName())) {
						sb.append(responseTempAdr.getLocationName()).append(" ");
					}
					
					if(responseTempAdr.getBuildingNumber()!=null) {
						sb.append(IndexUIbean.getMessageResourceString(LABELS,"tasks.nomer")).append(": ").append(responseTempAdr.getBuildingNumber()).append(" ");
					}
					
					if(responseTempAdr.getEntrance()!=null) {
						sb.append(IndexUIbean.getMessageResourceString(LABELS,DOCU_LOGIN)).append(": ").append(responseTempAdr.getEntrance()).append(" ");
					}
					
					if(responseTempAdr.getFloor()!=null) {
						sb.append(IndexUIbean.getMessageResourceString(LABELS,FLOOR)).append(": ").append(responseTempAdr.getFloor()).append(" ");
					}
					
					if(responseTempAdr.getApartment()!=null) {
						sb.append(IndexUIbean.getMessageResourceString(LABELS,"regixReport.apartment")).append(": ").append(responseTempAdr.getApartment()).append(BR);
					}
					
					if(responseTempAdr.getFromDate()!=null) {
						sb.append(B_OPEN).append(IndexUIbean.getMessageResourceString(LABELS,"regixReport.fromDate")).append(B_COLON_SPACE).append(DateUtils.printDate(DateUtils.toDate(responseTempAdr.getFromDate()))).append(BR);
					}
				} 
				
				
				
				setResult(sb.toString());
				
			}else{			
				//JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, IndexUIbean.getMessageResourceString(BEANMESSAGES, "compRegix.noData"));
				errMsg = IndexUIbean.getMessageResourceString(BEANMESSAGES, "compRegix.noData");
			}
		} catch (RegixClientException e) {
			LOGGER.error("Грешка при зареждане на Regix client! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
		} catch (DbErrorException e) {
			LOGGER.error("Грешка при работа с базата! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
		} catch (DatatypeConfigurationException e) {
			LOGGER.error("Грешка при! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
		}
	}
	
	//Справка за наличие/ липса на задължения
	private void actionSearchObligation() {
		try {
			client = getSystemData().getRegixClient();
			ObligationRequest request = new ObligationRequest();

			IdentityTypeRequest identityTypeRequest = new IdentityTypeRequest();
			identityTypeRequest.setID(getEgnEik());
			//String ident="";
			if("1".equals(getIdent())) {					
				identityTypeRequest.setTYPE(EikTypeTypeRequest.EGN);
				//ident = IndexUIbean.getMessageResourceString(LABELS,"admStruct.egn");
			}else if("2".equals(getIdent())) {
				identityTypeRequest.setTYPE(EikTypeTypeRequest.LNC);
				//ident = IndexUIbean.getMessageResourceString(LABELS,"mmsCoach.lnch");
			}else if("3".equals(getIdent())){
				identityTypeRequest.setTYPE(EikTypeTypeRequest.BULSTAT);
				//ident = IndexUIbean.getMessageResourceString(LABELS,"admStruct.eik");
			}
			
			request.setIdentity(identityTypeRequest);
			request.setThreshold(getPrag()); 
			ObligationResponse response = (ObligationResponse) this.client.executeOperation(NraOperation.GET_OBLIGATED_PERSONS, request);
			
			if(response!=null && response.getStatus()!=null){
				if(response.getStatus().getCode()==0) {//  Code - Код Възможни стойности: 0 - OK 1 - XML валидационна грешка 2 - Невалиден ЕИК 99 - Друго					
					sb.append(IndexUIbean.getMessageResourceString(LABELS,"regixReport.liceto")).append(" ").append(response.getName()).append(" ");
					if(response.getObligationStatus()!=null) {
						  switch (response.getObligationStatus()) {
					            case PRESENT:
					            	sb.append(IndexUIbean.getMessageResourceString(LABELS,"regixReport.ima"));
					                break;
					                    
					            case ABSENT:
					            	sb.append(IndexUIbean.getMessageResourceString(LABELS,"regixReport.niama"));
					                break;
						  }
						
						sb.append(" ").append(IndexUIbean.getMessageResourceString(LABELS,"regixReport.oblMessage"));
					}
					
					if(getPrag()!=null) {
						sb.append(" ").append(getPrag()).append(" ");
					}else {
						setPrag(0);
						sb.append(" 0 ");
					}
					sb.append(IndexUIbean.getMessageResourceString(LABELS,"regixReport.lv"));
																
					setResult(sb.toString());
				}else {
					setResult(response.getStatus().getMessage());
				}
			}else{
				errMsg = IndexUIbean.getMessageResourceString(BEANMESSAGES, "compRegix.noData");
				//JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, IndexUIbean.getMessageResourceString(BEANMESSAGES, "compRegix.noData"));	
			}
		} catch (RegixClientException e) {
			LOGGER.error("Грешка при зареждане на Regix client! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
		} catch (DbErrorException e) {
			LOGGER.error("Грешка при работа с базата! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
		}
	}
	
	//Справка по ЕГН за документ за образование в системата на МОН
    private void searchCertifiedDocuments() {
    	try {
			client = getSystemData().getRegixClient();
			CertifiedDocumentsSearchType request = new CertifiedDocumentsSearchType();
			request.setDocumentNumber(getDocNum());
			if("1".equals(getIdent())) {					
				request.setIDType(IdentifierType.EGN);			
			}else if("2".equals(getIdent())) {
				request.setIDType(IdentifierType.LN_CH);				
			}else if("4".equals(getIdent())) {
				request.setIDType(IdentifierType.IDN);				
			}
					
			request.setStudentID(getEgnEik()); 

			CertifiedDocumentsType response = (CertifiedDocumentsType) this.client.executeOperation(RdsoOperation.GET_CERTIFIED_DIPLOMA_INFO, request);
			
			if(response!=null&&response.getCertifiedDocument()!=null) {
				for(int i=0;i<response.getCertifiedDocument().size();i++) { 
					
					sb.append(IndexUIbean.getMessageResourceString(LABELS,"docu.document")).append(" ").append(i+1).append(" ").append(IndexUIbean.getMessageResourceString(LABELS,"regixReport.ot"))
					.append(" ").append(response.getCertifiedDocument().size());
					
					sb.append("<table border=\"1\"<tr><td><b>").append(IndexUIbean.getMessageResourceString(LABELS, "regixReport.diplomaIdent")).
					append("</b></td><td>");
					
					if(response.getCertifiedDocument().get(i).getIntID()!=null) {
						sb.append(response.getCertifiedDocument().get(i).getIntID());
					}
					
					sb.append("</td></tr><tr><td><b>").append(IndexUIbean.getMessageResourceString(LABELS, "admStruct.names")).append("</b></td><td>");
					
					if(response.getCertifiedDocument().get(i).getVcName1()!=null) {
						sb.append(response.getCertifiedDocument().get(i).getVcName1()).append(" ");
					}
					
					if(response.getCertifiedDocument().get(i).getVcName2()!=null) {
						sb.append(response.getCertifiedDocument().get(i).getVcName2()).append(" ");
					}
					
					if(response.getCertifiedDocument().get(i).getVcName3()!=null) {
						sb.append(response.getCertifiedDocument().get(i).getVcName3());	
					}
					
					sb.append("</td></tr><tr><td><b>").append(IndexUIbean.getMessageResourceString(LABELS, "regixReport.decumentCode")).append("</b></td><td>");
					
					if(response.getCertifiedDocument().get(i).getIntDocumentType()!=null) {
						sb.append(response.getCertifiedDocument().get(i).getIntDocumentType());
					}
										
					sb.append("</td></tr><tr><td><b>").append(IndexUIbean.getMessageResourceString(LABELS, "regixReport.naimDoc")).append("</b></td><td>");
					
					if(response.getCertifiedDocument().get(i).getVcDocumentName()!=null) {
						sb.append(response.getCertifiedDocument().get(i).getVcDocumentName());
					}
					
					sb.append("</td></tr><tr><td><b>").append(IndexUIbean.getMessageResourceString(LABELS, "regixReport.seriaDoc")).append("</b></td><td>");
					
					if(response.getCertifiedDocument().get(i).getVcPrnSer()!=null) {
						sb.append(response.getCertifiedDocument().get(i).getVcPrnSer());
					}				
					sb.append("</td></tr><tr><td><b>").append(IndexUIbean.getMessageResourceString(LABELS, "regixReport.nomerDoc")).append("</b></td><td>");
					
					if(response.getCertifiedDocument().get(i).getVcPrnNo()!=null) {
						sb.append(response.getCertifiedDocument().get(i).getVcPrnNo());
					}
										
					sb.append("</td></tr><tr><td><b>").append(IndexUIbean.getMessageResourceString(LABELS, "regixReport.regNom")).append("</b></td><td>");
					if(response.getCertifiedDocument().get(i).getVcRegNo()!=null) {
						sb.append(response.getCertifiedDocument().get(i).getVcRegNo());
					}
					
					sb.append("</td></tr><tr><td><b>").append(IndexUIbean.getMessageResourceString(LABELS, "regixReport.dataZaverka")).append("</b></td><td>");
					if(response.getCertifiedDocument().get(i).getDtCertDate()!=null) {
						sb.append(DateUtils.printDate(DateUtils.toDate(response.getCertifiedDocument().get(i).getDtCertDate())));
					}
					sb.append("</td></tr></table><br/><br/>");
				}
							
				setResult(sb.toString());
			}else{
				errMsg = IndexUIbean.getMessageResourceString(BEANMESSAGES, "compRegix.noData");
				//JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, IndexUIbean.getMessageResourceString(LABELS, "regixReport.noData"));	
			}
		}  catch (RegixClientException e) {
			LOGGER.error("Грешка при зареждане на Regix client! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
		} catch (DbErrorException e) {
			LOGGER.error("Грешка при работа с базата! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
		}
    }
    
    
  // Справка за диплома за средно образование на определено лице 
    private void searchDiploma() {
    	try {
			client = getSystemData().getRegixClient();
			DiplomaSearchType request = new DiplomaSearchType();
			request.setDocumentNumber(getDocNum());
			if("1".equals(getIdent())) {					
				request.setIDType(IdentifierType.EGN);			
			}else if("2".equals(getIdent())) {
				request.setIDType(IdentifierType.LN_CH);				
			}else if("4".equals(getIdent())) {
				request.setIDType(IdentifierType.IDN);				
			}
					
			request.setStudentID(getEgnEik()); 

			DiplomaDocumentsType response = (DiplomaDocumentsType) this.client.executeOperation(RdsoOperation.GET_DIPLOMA_INFO, request);
			
			if(response!=null && response.getDiplomaDocument()!=null && !response.getDiplomaDocument().isEmpty()) {
				for(int i=0;i<response.getDiplomaDocument().size();i++) { 
					
					sb.append(IndexUIbean.getMessageResourceString(LABELS,"docu.document")).append(" ").append(i+1).append(" ").append(IndexUIbean.getMessageResourceString(LABELS,"regixReport.ot"))
					.append(" ").append(response.getDiplomaDocument().size());
					
					sb.append("<table border=\"1\"<tr><td><b>").append(IndexUIbean.getMessageResourceString(LABELS, "compRegix.indentUchenik")).
					append("</b></td><td>");
					
					if(response.getDiplomaDocument().get(i).getIntStudentID ()!=null) {
						sb.append(response.getDiplomaDocument().get(i).getIntStudentID());
					}
					
					sb.append("</td></tr><tr><td><b>").append(IndexUIbean.getMessageResourceString(LABELS, "admStruct.names")).append("</b></td><td>");
					
					if(response.getDiplomaDocument().get(i).getVcName1()!=null) {
						sb.append(response.getDiplomaDocument().get(i).getVcName1()).append(" ");
					}
					
					if(response.getDiplomaDocument().get(i).getVcName2()!=null) {
						sb.append(response.getDiplomaDocument().get(i).getVcName2()).append(" ");
					}
					
					if(response.getDiplomaDocument().get(i).getVcName3()!=null) {
						sb.append(response.getDiplomaDocument().get(i).getVcName3());	
					}
					
					sb.append("</td></tr>");
										
					sb.append("<tr><td><b>").append(IndexUIbean.getMessageResourceString(LABELS, "regixReport.naimDoc")).append("</b></td><td>");
					
					if(response.getDiplomaDocument().get(i).getVcDocumentName()!=null) {
						sb.append(response.getDiplomaDocument().get(i).getVcDocumentName());
					}
					
					sb.append("</td></tr>");
					
					//Година на завършване
					sb.append("<tr><td><b>").append(IndexUIbean.getMessageResourceString(LABELS, "compRegix.yearGraduated")).append("</b></td><td>");
					
					if(response.getDiplomaDocument().get(i).getIntYearGraduated ()!=null) {
						sb.append(response.getDiplomaDocument().get(i).getIntYearGraduated());
					}										
					sb.append("</td></tr>");
					
					//Дата на издаване на документа
					sb.append("<tr><td><b>").append(IndexUIbean.getMessageResourceString(LABELS, "compRegix.dataizdDoc")).append("</b></td><td>");
					
					if(response.getDiplomaDocument().get(i).getDtRegDate ()!=null) {
						sb.append(DateUtils.printDate(DateUtils.toDate(response.getDiplomaDocument().get(i).getDtRegDate())));
					}										
					sb.append("</td></tr>");
					
					//Код на завършена специалност 
					sb.append("<tr><td><b>").append(IndexUIbean.getMessageResourceString(LABELS, "compRegix.codeSpec")).append("</b></td><td>");
					
					if(response.getDiplomaDocument().get(i).getIntVETSpeciality()!=null) {
						sb.append(response.getDiplomaDocument().get(i).getIntVETSpeciality());
					}										
					sb.append("</td></tr>");
					
					//Наименование на специалност 
					sb.append("<tr><td><b>").append(IndexUIbean.getMessageResourceString(LABELS, "compRegix.naimSpec")).append("</b></td><td>");
					
					if(response.getDiplomaDocument().get(i).getVcVETSpecialityName()!=null) {
						sb.append(response.getDiplomaDocument().get(i).getVcVETSpecialityName());
					}										
					sb.append("</td></tr>");
					
					//Код на придобита степен на професионална квалификация
					sb.append("<tr><td><b>").append(IndexUIbean.getMessageResourceString(LABELS, "compRegix.kodProfKv")).append("</b></td><td>");
					
					if(response.getDiplomaDocument().get(i).getIntVETLevel()!=null) {
						sb.append(response.getDiplomaDocument().get(i).getIntVETLevel());
					}										
					sb.append("</td></tr>");
					
					//Код на степен на професионална квалификация
					sb.append("<tr><td><b>").append(IndexUIbean.getMessageResourceString(LABELS, "compRegix.codeStepen")).append("</b></td><td>");
					
					if(response.getDiplomaDocument().get(i).getVcVETLevelName()!=null) {
						sb.append(response.getDiplomaDocument().get(i).getVcVETLevelName());
					}										
					sb.append("</td></tr>");
					
					//Код на професионално направление 
					sb.append("<tr><td><b>").append(IndexUIbean.getMessageResourceString(LABELS, "compRegix.codeProfNapr")).append("</b></td><td>");
					
					if(response.getDiplomaDocument().get(i).getIntVETGroupIdent()!=null) {
						sb.append(response.getDiplomaDocument().get(i).getIntVETGroupIdent());
					}										
					sb.append("</td></tr>");
					
					
					//Наименование на професионално направление
					sb.append("<tr><td><b>").append(IndexUIbean.getMessageResourceString(LABELS, "compRegix.naimProfNapr")).append("</b></td><td>");
					
					if(response.getDiplomaDocument().get(i).getVcEducAreaName ()!=null) {
						sb.append(response.getDiplomaDocument().get(i).getVcEducAreaName());
					}										
					sb.append("</td></tr>");
					
					sb.append("</td></tr></table><br/><br/>");
				}
							
				setResult(sb.toString());
			}else{
				errMsg = IndexUIbean.getMessageResourceString(BEANMESSAGES, "compRegix.noData");
				//JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, IndexUIbean.getMessageResourceString(BEANMESSAGES, "compRegix.noData"));		
			}
		}  catch (RegixClientException e) {
			LOGGER.error("Грешка при зареждане на Regix client! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
		} catch (DbErrorException e) {
			LOGGER.error("Грешка при работа с базата! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
		}
    }
    
 // Справка за издаден документ на лице по подаден идентификатор и идентификационен (или регистрационен) номер
    private void searchDocLice() {
    	try {
			client = getSystemData().getRegixClient();
			StudentDocumentRequestType request = new StudentDocumentRequestType();
			request.setDocumentRegistrationNumber(getDocNum());				
			request.setStudentIdentifier (getEgnEik()); 

			DocumentsByStudentResponse response = (DocumentsByStudentResponse) this.client.executeOperation(NapooOperation.GET_STUDENT_DOCUMENT, request);
			
			if(response!=null &&  response.getStudentDocument()!=null && !response.getStudentDocument().isEmpty()) {
				for(int i=0;i<response.getStudentDocument().size();i++) { 
					
					sb.append(IndexUIbean.getMessageResourceString(LABELS,"docu.document")).append(" ").append(i+1).append(" ").append(IndexUIbean.getMessageResourceString(LABELS,"regixReport.ot"))
					.append(" ").append(response.getStudentDocument().size());
					
					sb.append("<table border=\"1\"<tr><td><b>").append(IndexUIbean.getMessageResourceString(LABELS, "compRegix.indentUchenik")).
					append("</b></td><td>");
					
					if(response.getStudentDocument().get(i).getStudentIdentifier()!=null) {
						sb.append(response.getStudentDocument().get(i).getStudentIdentifier());
					}
					
					sb.append("</td></tr><tr><td><b>").append(IndexUIbean.getMessageResourceString(LABELS, "admStruct.names")).append("</b></td><td>");
					
					if(response.getStudentDocument().get(i).getFirstName()!=null) {
						sb.append(response.getStudentDocument().get(i).getFirstName()).append(" ");
					}
					
					if(response.getStudentDocument().get(i).getMiddleName()!=null) {
						sb.append(response.getStudentDocument().get(i).getMiddleName()).append(" ");
					}
					
					if(response.getStudentDocument().get(i).getLastName()!=null) {
						sb.append(response.getStudentDocument().get(i).getLastName());	
					}
					
					sb.append("</td></tr>");
					
					//Идентификатор на документ на курсист в регистъра
					sb.append("<tr><td><b>").append(IndexUIbean.getMessageResourceString(LABELS, "compRegix.identDoc")).append("</b></td><td>");					
					if(response.getStudentDocument().get(i).getLicenceNumber()!=null) {
						sb.append(response.getStudentDocument().get(i).getLicenceNumber());
					}					
					sb.append("</td></tr>");
					
					//Наименование на център за професионално обучение
					sb.append("<tr><td><b>").append(IndexUIbean.getMessageResourceString(LABELS, "compRegix.naimCenter")).append("</b></td><td>");					
					if(response.getStudentDocument().get(i).getProfessionalEduCenter()!=null) {
						sb.append(response.getStudentDocument().get(i).getProfessionalEduCenter());
					}					
					sb.append("</td></tr>");
					
					//Населено място на център за професионално обучение
					sb.append("<tr><td><b>").append(IndexUIbean.getMessageResourceString(LABELS, "compRegix.centerLocation")).append("</b></td><td>");					
					if(response.getStudentDocument().get(i).getProfessionalEduCenterLocation()!=null) {
						sb.append(response.getStudentDocument().get(i).getProfessionalEduCenterLocation());
					}				
					sb.append("</td></tr>");
					
					//Уникален идентификатор на вида на документа
					sb.append("<tr><td><b>").append(IndexUIbean.getMessageResourceString(LABELS, "compRegix.docVidIdent")).append("</b></td><td>");					
					if(response.getStudentDocument().get(i).getDocumentTypeID()!=null) {
						sb.append(response.getStudentDocument().get(i).getDocumentTypeID());
					}				
					sb.append("</td></tr>");
					
					//Вид на документа
					sb.append("<tr><td><b>").append(IndexUIbean.getMessageResourceString(LABELS, "mmsCoach.vidDoc")).append("</b></td><td>");
					
					if(response.getStudentDocument().get(i).getDocumentType()!=null) {
						sb.append(response.getStudentDocument().get(i).getDocumentType());
					}										
					sb.append("</td></tr>");
					
					//Уникален идентификатор на вида на обучението
					sb.append("<tr><td><b>").append(IndexUIbean.getMessageResourceString(LABELS, "compRegix.identEducation")).append("</b></td><td>");
					
					if(response.getStudentDocument().get(i).getEducationTypeID()!=null) {
						sb.append(response.getStudentDocument().get(i).getEducationTypeID());
					}										
					sb.append("</td></tr>");
					
					//Вид на обучението
					sb.append("<tr><td><b>").append(IndexUIbean.getMessageResourceString(LABELS, "compRegix.vidEducation")).append("</b></td><td>");
					
					if(response.getStudentDocument().get(i).getEducationType()!=null) {
						sb.append(response.getStudentDocument().get(i).getEducationType());
					}										
					sb.append("</td></tr>");
					
					//Код и наименование на професията
					sb.append("<tr><td><b>").append(IndexUIbean.getMessageResourceString(LABELS, "compRegix.codeNaimProf")).append("</b></td><td>");
					
					if(response.getStudentDocument().get(i).getProfessionCodeAndName()!=null) {
						sb.append(response.getStudentDocument().get(i).getProfessionCodeAndName());
					}										
					sb.append("</td></tr>");
					
					//Код и наименование на специалността
					sb.append("<tr><td><b>").append(IndexUIbean.getMessageResourceString(LABELS, "compRegix.codeNaimSpec")).append("</b></td><td>");
					
					if(response.getStudentDocument().get(i).getSubjectCodeAndName()!=null) {
						sb.append(response.getStudentDocument().get(i).getSubjectCodeAndName());
					}										
					sb.append("</td></tr>");
					
					//Придобита степен на професионална квалификация
					sb.append("<tr><td><b>").append(IndexUIbean.getMessageResourceString(LABELS, "compRegix.stepen")).append("</b></td><td>");
					
					if(response.getStudentDocument().get(i).getQualificationDegree()!=null) {
						sb.append(response.getStudentDocument().get(i).getQualificationDegree());
					}										
					sb.append("</td></tr>");
					
					//Година на завършване на курсиста
					sb.append("<tr><td><b>").append(IndexUIbean.getMessageResourceString(LABELS, "compRegix.yearGraduationStudent")).append("</b></td><td>");
					
					if(response.getStudentDocument().get(i).getGraduationYear()!=null) {
						sb.append(response.getStudentDocument().get(i).getGraduationYear());
					}										
					sb.append("</td></tr>");
					
					//Серия на документа
					sb.append("<tr><td><b>").append(IndexUIbean.getMessageResourceString(LABELS, "compRegix.seriaDoc")).append("</b></td><td>");
					
					if(response.getStudentDocument().get(i).getDocumentSeries()!=null) {
						sb.append(response.getStudentDocument().get(i).getDocumentSeries());
					}										
					sb.append("</td></tr>");
					
					//Сериен номер на документа
					sb.append("<tr><td><b>").append(IndexUIbean.getMessageResourceString(LABELS, "compRegix.serienNomerDoc")).append("</b></td><td>");
					
					if(response.getStudentDocument().get(i).getDocumentSerialNumber()!=null) {
						sb.append(response.getStudentDocument().get(i).getDocumentSerialNumber());
					}										
					sb.append("</td></tr>");
					
					//Регистрационен номер на документа
					sb.append("<tr><td><b>").append(IndexUIbean.getMessageResourceString(LABELS, "compRegix.regoNomDoc")).append("</b></td><td>");
					
					if(response.getStudentDocument().get(i).getDocumentRegistrationNumber()!=null) {
						sb.append(response.getStudentDocument().get(i).getDocumentRegistrationNumber());
					}										
					sb.append("</td></tr>");
					
					//Дата на издаване на документа
					sb.append("<tr><td><b>").append(IndexUIbean.getMessageResourceString(LABELS, "compRegix.dataizdDoc")).append("</b></td><td>");
					
					if(response.getStudentDocument().get(i).getDocumentIssueDate ()!=null) {
						sb.append(DateUtils.printDate(DateUtils.toDate(response.getStudentDocument().get(i).getDocumentIssueDate())));
					}										
					sb.append("</td></tr>");
					
								
					sb.append("</td></tr></table><br/><br/>");
				}
							
				setResult(sb.toString());
			}else{
				errMsg = IndexUIbean.getMessageResourceString(BEANMESSAGES, "compRegix.noData");
				//JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, IndexUIbean.getMessageResourceString(BEANMESSAGES, "compRegix.noData"));	
			}
		}  catch (RegixClientException e) {
			LOGGER.error("Грешка при зареждане на Regix client! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
		} catch (DbErrorException e) {
			LOGGER.error("Грешка при работа с базата! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
		}
    }
    
	public void changeSpr() {
		setEgnEik("");
		setResult("");
		setPrag(0); 
		setIdent("");
		setDocNum("");
		errMsg = "";
		if(getVidSpr().equals(4)||getVidSpr().equals(5)||getVidSpr().equals(6)) {
			populateIdentList();
		}
	}
	
	public void changeIdent() {
		setEgnEik("");
		errMsg = "";
	}
	
	private void populateIdentList() {
		
		setIdentList(new ArrayList<>());
		SelectItem  item = new SelectItem(1, IndexUIbean.getMessageResourceString(LABELS, "admStruct.egn") );
		getIdentList().add(item);	
		item = new SelectItem(2,IndexUIbean.getMessageResourceString(LABELS, "mmsCoach.lnch"));
		getIdentList().add(item);	
		if(getVidSpr().equals(4)) {
			item = new SelectItem(3,IndexUIbean.getMessageResourceString(LABELS, "admStruct.eik"));
			getIdentList().add(item);	
		} 
		if(getVidSpr().equals(5)) {
			item = new SelectItem(4,IndexUIbean.getMessageResourceString(LABELS, "regixReport.slujNom"));
			getIdentList().add(item);
		}
		
		if(getVidSpr().equals(6)) {
			item = new SelectItem(4,IndexUIbean.getMessageResourceString(LABELS, "compRegix.drugIdent"));
			getIdentList().add(item);
		}
	}
	
	
	public void changeSlujNom() {
		errMsg = "";
		if(getEgnEik()!=null && !isEmpty(getEgnEik())){
			disableSearch = false;
		}else {
			disableSearch = true;
		}
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
	
	
	/** @return the userData */
	private UserData getUserData() {
		if (this.userData == null) {
			this.userData = (UserData) JSFUtils.getManagedBean("userData");
		}
		return this.userData;
	}
	
	private SystemData getSystemData() {
		if (this.systemData == null) {
			this.systemData =  (SystemData) JSFUtils.getManagedBean("systemData");
		}
		return this.systemData;
	}
	
	/**
	 * @param date
	 * @return
	 * @throws DatatypeConfigurationException
	 */
	private static XMLGregorianCalendar toGregorianCalendar(Date date) throws DatatypeConfigurationException {
		if (date == null) {
			return null;
		}
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(date);

		return DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);
	}
	
	/** @return */
	public Integer getLang() {
		return getUserData().getCurrentLang();
	}
	
	/** @return */
	public Date getCurrentDate() {
		return getDateClassif();
	}

	public String getErrMsg() {
		return errMsg;
	}

	public void setErrMsg(String errMsg) {
		this.errMsg = errMsg;
	}

	@SuppressWarnings("unchecked")
	public List<SelectItem> getSprList() {
		return (List<SelectItem>) getStateHelper().eval(PropertyKeys.SPRLIST, null);		
	}

	public void setSprList(List<SelectItem> sprList) {
		getStateHelper().put(PropertyKeys.SPRLIST, sprList);		
	}
	
	@SuppressWarnings("unchecked")
	public List<SelectItem> getIdentList() {
		return (List<SelectItem>) getStateHelper().eval(PropertyKeys.IDENTLIST, null);		
	}

	public void setIdentList(List<SelectItem> identList) {
		getStateHelper().put(PropertyKeys.IDENTLIST, identList);		
	}

	public Integer getVidSpr() {
		return (Integer) getStateHelper().eval(PropertyKeys.VIDSPR, null);		
	}

	public void setVidSpr(Integer vidSpr) {
		getStateHelper().put(PropertyKeys.VIDSPR, vidSpr);		
	}

	public String getEgnEik() {
		return (String) getStateHelper().eval(PropertyKeys.EGNEIK, null);	
	}

	public void setEgnEik(String egnEik) {
		getStateHelper().put(PropertyKeys.EGNEIK, egnEik);			
	}
	
	public String getIdent() {
		return (String) getStateHelper().eval(PropertyKeys.IDENT, null);	
	}

	public void setIdent(String ident) {
		getStateHelper().put(PropertyKeys.IDENT, ident);			
	}
	
	public Integer getPrag() {
		return (Integer) getStateHelper().eval(PropertyKeys.PRAG, null);	
	}

	public void setPrag(Integer prag) {
		getStateHelper().put(PropertyKeys.PRAG, prag);			
	}

	public String getResult() {
		return (String) getStateHelper().eval(PropertyKeys.RESULT, null);		
	}

	public void setResult(String result) {
		getStateHelper().put(PropertyKeys.RESULT, result);		
	}
	
	public String getDocNum() {
		return (String) getStateHelper().eval(PropertyKeys.DOCNUM, null);		
	}

	public void setDocNum(String docNum) {
		getStateHelper().put(PropertyKeys.DOCNUM, docNum);		
	}
	
	public boolean isDisableSearch() {
		return disableSearch;		
	}

	public void setDisableSearch(boolean disableSearch) {
		this.disableSearch = disableSearch;		
	}
	
}