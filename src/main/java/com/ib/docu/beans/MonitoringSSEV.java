package com.ib.docu.beans;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import com.ib.docu.db.dao.EgovMessagesDAO;
import com.ib.docu.db.dao.LockObjectDAO;
import com.ib.docu.db.dto.EgovMessages;
import com.ib.docu.db.dto.EgovMessagesFiles;
import com.ib.docu.system.DocuConstants;
import com.ib.indexui.customexporter.CustomExpPreProcess;
import com.ib.indexui.pagination.LazyDataModelSQL2Array;
import com.ib.indexui.system.IndexUIbean;
import com.ib.indexui.utils.JSFUtils;
import com.ib.system.db.JPA;
import com.ib.system.db.SelectMetadata;
import com.ib.system.exceptions.BaseException;
import com.ib.system.exceptions.DbErrorException;
import com.ib.system.utils.DateUtils;
import com.ib.system.utils.SearchUtils;

import org.primefaces.PrimeFaces;
import org.primefaces.component.export.PDFOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named (value = "monitoringSSEV")
@ViewScoped
public class MonitoringSSEV extends IndexUIbean  {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9142346787510855379L;
	private static final Logger LOGGER = LoggerFactory.getLogger(MonitoringSSEV.class);
	
	private Integer period;

	private Date dateOt;
	private Date dateDo;
    private String status;
    private String rnDoc;
	
	private Date decodeDate;
	
	private transient EgovMessagesDAO daoEgov;	
	private LazyDataModelSQL2Array msgList;
	
	private List<SelectItem> msgStatusList = new ArrayList<>(); 
	
	private List <Integer> vidDocDost = new ArrayList<>(); // до кои видове документи има достъп потребителят
	
	private Map<String,String> zaiavleniaMap = new HashMap<>(); // видовете заявления и към коя страница трябва да водят
	private List<SelectItem> docVidSelectItem = new ArrayList<>(); // за да се търси по вид документ
	private Integer selectedVidDoc;
	private EgovMessages egovMess;
	
	private List<EgovMessagesFiles> egovFilesList;
	private String statusMess; //заради датата на статуса
	private boolean lockOk; //заради записа в модалния прозорец за разглеждане на заявление
	private String lockFrom; //от кого е заключено
	@PostConstruct
	void initData() {
				
		LOGGER.debug("PostConstruct!!!");								
		daoEgov = new EgovMessagesDAO(getUserData());		
	
		try {
			egovMess = new EgovMessages();
			ArrayList<Object[]> tmpList = daoEgov.createMsgTypesList();
	
			tmpList = daoEgov.createMsgStatusList();
		
			if(tmpList !=null && !tmpList.isEmpty()){
				for(Object[] item:tmpList) {
					if(item != null && item[0]!=null && item[1]!=null){
						msgStatusList.add(new SelectItem( item[0].toString(),item[1].toString()));
					}
				}
			}
			
			status = "DS_WAIT_REGISTRATION";
			
			if(getUserData().hasAccess(DocuConstants.CODE_CLASSIF_DOC_VID,DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_SPORTEN_OBEKT)) {
				vidDocDost.add(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_SPORTEN_OBEKT);
				zaiavleniaMap.put(SearchUtils.asString(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_SPORTEN_OBEKT), "mmsSportObektEdit");
				docVidSelectItem.add(new SelectItem( DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_SPORTEN_OBEKT,
						 getSystemData().decodeItem(DocuConstants.CODE_CLASSIF_DOC_VID, DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_SPORTEN_OBEKT, getCurrentLang(), decodeDate)));
			}
			
			if(getUserData().hasAccess(DocuConstants.CODE_CLASSIF_DOC_VID,DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_SPORTNO_FORM)) {
				vidDocDost.add(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_SPORTNO_FORM);
				zaiavleniaMap.put(SearchUtils.asString(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_SPORTNO_FORM),"mmsSportFormirovanieEdit");
				docVidSelectItem.add(new SelectItem( DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_SPORTNO_FORM,
						 getSystemData().decodeItem(DocuConstants.CODE_CLASSIF_DOC_VID, DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_SPORTNO_FORM, getCurrentLang(), decodeDate)));
			
			}
			
			if(getUserData().hasAccess(DocuConstants.CODE_CLASSIF_DOC_VID,DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_SPORTNO_OBEDINENIE)) {
				vidDocDost.add(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_SPORTNO_OBEDINENIE);
				zaiavleniaMap.put(SearchUtils.asString(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_SPORTNO_OBEDINENIE),"sportObedNew");
				docVidSelectItem.add(new SelectItem( DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_SPORTNO_OBEDINENIE,
						 getSystemData().decodeItem(DocuConstants.CODE_CLASSIF_DOC_VID, DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_SPORTNO_OBEDINENIE, getCurrentLang(), decodeDate)));
			
			}
			
			if(getUserData().hasAccess(DocuConstants.CODE_CLASSIF_DOC_VID,DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_TREN_KADRI)) {
				vidDocDost.add(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_TREN_KADRI);
				zaiavleniaMap.put(SearchUtils.asString(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_TREN_KADRI),"mmsCoachEdit");
				docVidSelectItem.add(new SelectItem( DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_TREN_KADRI,
						 getSystemData().decodeItem(DocuConstants.CODE_CLASSIF_DOC_VID, DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_TREN_KADRI, getCurrentLang(), decodeDate)));
			
			}
			
			if(getUserData().hasAccess(DocuConstants.CODE_CLASSIF_DOC_VID,DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_ZALICH_SPORTEN_OBEKT)) {
				vidDocDost.add(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_ZALICH_SPORTEN_OBEKT);
				zaiavleniaMap.put(SearchUtils.asString(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_ZALICH_SPORTEN_OBEKT),"mmsSportObektEdit");
				docVidSelectItem.add(new SelectItem( DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_ZALICH_SPORTEN_OBEKT,
						 getSystemData().decodeItem(DocuConstants.CODE_CLASSIF_DOC_VID, DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_ZALICH_SPORTEN_OBEKT, getCurrentLang(), decodeDate)));
			
			}
			
			if(getUserData().hasAccess(DocuConstants.CODE_CLASSIF_DOC_VID,DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_ZALICH_SPORTNO_FORM)) {
				vidDocDost.add(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_ZALICH_SPORTNO_FORM);
				zaiavleniaMap.put(SearchUtils.asString(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_ZALICH_SPORTNO_FORM),"mmsSportFormirovanieEdit");
				docVidSelectItem.add(new SelectItem( DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_ZALICH_SPORTNO_FORM,
						 getSystemData().decodeItem(DocuConstants.CODE_CLASSIF_DOC_VID, DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_ZALICH_SPORTNO_FORM, getCurrentLang(), decodeDate)));
			
			}
			
			if(getUserData().hasAccess(DocuConstants.CODE_CLASSIF_DOC_VID,DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_ZALICH_SPORTNO_OBEDINENIE)) {
				vidDocDost.add(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_ZALICH_SPORTNO_OBEDINENIE);
				zaiavleniaMap.put(SearchUtils.asString(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_ZALICH_SPORTNO_OBEDINENIE),"sportObedNew");
				docVidSelectItem.add(new SelectItem( DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_ZALICH_SPORTNO_OBEDINENIE,
						 getSystemData().decodeItem(DocuConstants.CODE_CLASSIF_DOC_VID, DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_ZALICH_SPORTNO_OBEDINENIE, getCurrentLang(), decodeDate)));
			
			}
			
			if(getUserData().hasAccess(DocuConstants.CODE_CLASSIF_DOC_VID,DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_ZALICH_TREN_KADRI)) {
				vidDocDost.add(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_ZALICH_TREN_KADRI);
				zaiavleniaMap.put(SearchUtils.asString(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_ZALICH_TREN_KADRI),"mmsCoachEdit");
				docVidSelectItem.add(new SelectItem( DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_ZALICH_TREN_KADRI,
						 getSystemData().decodeItem(DocuConstants.CODE_CLASSIF_DOC_VID, DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_ZALICH_TREN_KADRI, getCurrentLang(), decodeDate)));
			
			}
			
			if(getUserData().hasAccess(DocuConstants.CODE_CLASSIF_DOC_VID,DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_PROM_SPORTEN_OBEKT)) {
				vidDocDost.add(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_PROM_SPORTEN_OBEKT);
				zaiavleniaMap.put(SearchUtils.asString(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_PROM_SPORTEN_OBEKT),"mmsSportObektEdit");
				docVidSelectItem.add(new SelectItem( DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_PROM_SPORTEN_OBEKT,
						 getSystemData().decodeItem(DocuConstants.CODE_CLASSIF_DOC_VID, DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_PROM_SPORTEN_OBEKT, getCurrentLang(), decodeDate)));
			
			}
			
			if(getUserData().hasAccess(DocuConstants.CODE_CLASSIF_DOC_VID,DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_PROM_SPORTNO_FORM)) {
				vidDocDost.add(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_PROM_SPORTNO_FORM);
				zaiavleniaMap.put(SearchUtils.asString(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_PROM_SPORTNO_FORM),"mmsSportFormirovanieEdit");
				docVidSelectItem.add(new SelectItem( DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_PROM_SPORTNO_FORM,
						 getSystemData().decodeItem(DocuConstants.CODE_CLASSIF_DOC_VID, DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_PROM_SPORTNO_FORM, getCurrentLang(), decodeDate)));
			
			}
			
			if(getUserData().hasAccess(DocuConstants.CODE_CLASSIF_DOC_VID,DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_PROM_SPORTNO_OBEDINENIE)) {
				vidDocDost.add(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_PROM_SPORTNO_OBEDINENIE);
				zaiavleniaMap.put(SearchUtils.asString(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_PROM_SPORTNO_OBEDINENIE),"sportObedNew");
				docVidSelectItem.add(new SelectItem( DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_PROM_SPORTNO_OBEDINENIE,
						 getSystemData().decodeItem(DocuConstants.CODE_CLASSIF_DOC_VID, DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_PROM_SPORTNO_OBEDINENIE, getCurrentLang(), decodeDate)));
			
			}
					
			if(getUserData().hasAccess(DocuConstants.CODE_CLASSIF_DOC_VID,DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_PROM_TREN_KADRI)) {
				vidDocDost.add(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_PROM_TREN_KADRI);
				zaiavleniaMap.put(SearchUtils.asString(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_PROM_TREN_KADRI),"mmsCoachEdit");
				docVidSelectItem.add(new SelectItem( DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_PROM_TREN_KADRI,
						 getSystemData().decodeItem(DocuConstants.CODE_CLASSIF_DOC_VID, DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_PROM_TREN_KADRI, getCurrentLang(), decodeDate)));
			
			}
			
			if(getUserData().hasAccess(DocuConstants.CODE_CLASSIF_DOC_VID,DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_OSK)) {
				vidDocDost.add(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_OSK);
				zaiavleniaMap.put(SearchUtils.asString(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_OSK),"sportObedNew");
				docVidSelectItem.add(new SelectItem( DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_OSK,
						 getSystemData().decodeItem(DocuConstants.CODE_CLASSIF_DOC_VID, DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_OSK, getCurrentLang(), decodeDate)));
			
			}
			
			if(getUserData().hasAccess(DocuConstants.CODE_CLASSIF_DOC_VID,DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_NOSTD)) {
				vidDocDost.add(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_NOSTD);
				zaiavleniaMap.put(SearchUtils.asString(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_NOSTD),"sportObedNew");
				docVidSelectItem.add(new SelectItem( DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_NOSTD,
						 getSystemData().decodeItem(DocuConstants.CODE_CLASSIF_DOC_VID, DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_NOSTD, getCurrentLang(), decodeDate)));
			
			}
			
			if(getUserData().hasAccess(DocuConstants.CODE_CLASSIF_DOC_VID,DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_NOUS)) {
				vidDocDost.add(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_NOUS);
				zaiavleniaMap.put(SearchUtils.asString(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_NOUS),"sportObedNew");
				docVidSelectItem.add(new SelectItem( DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_NOUS,
						 getSystemData().decodeItem(DocuConstants.CODE_CLASSIF_DOC_VID, DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_NOUS, getCurrentLang(), decodeDate)));
			
			}
			
			if(getUserData().hasAccess(DocuConstants.CODE_CLASSIF_DOC_VID,DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_TD)) {
				vidDocDost.add(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_TD);
				zaiavleniaMap.put(SearchUtils.asString(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_TD),"mmsSportFormirovanieEdit");
				docVidSelectItem.add(new SelectItem( DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_TD,
						 getSystemData().decodeItem(DocuConstants.CODE_CLASSIF_DOC_VID, DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_TD, getCurrentLang(), decodeDate)));
			
			}
			
		} catch (DbErrorException e) {
			LOGGER.error("Грешка при зареждане на данните! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
		} finally {
			JPA.getUtil().closeConnection();
		}
		
		actionSearch();
	}

	
	public void actionSearch() {
		
		try {
			SelectMetadata smd = daoEgov.createFilterMonitoringSSEV(status, dateOt, dateDo, vidDocDost,selectedVidDoc,getUserData(),rnDoc);
			String defaultSortColumn = "A0";	
			this.msgList = new LazyDataModelSQL2Array(smd, defaultSortColumn);			
				
		}catch (Exception e) {
			LOGGER.error("Грешка при зареждане данните!", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());	
		}
	}		
	
	
	
	public void actionClearFilter() {
		period = null;
		dateOt = null;
		dateDo = null;
		status = "DS_WAIT_REGISTRATION";
		this.msgList = null;
		selectedVidDoc = null;
		rnDoc = null;
		
	}
	
	public void changePeriod() {
		
    	if (this.period != null) {
			Date[] di;
			di = DateUtils.calculatePeriod(this.period);
			dateOt =  di[0];
			dateDo =  di[1];		
    	} else {
    		dateOt = null;
    		dateDo = null;
		}
    }

	public void changeDate() { 
		this.setPeriod(null);
	}
		
	public String actionGoto (Object[] row) {
		Integer ccevID = ((Number) row[0]).intValue();
		String result = "";
		
		String vidZaiavlenie = SearchUtils.asString(row[1]);
		
		if(vidZaiavlenie!=null) {
			for(String key :zaiavleniaMap.keySet()) {
				if(key.equals(vidZaiavlenie)) {
					//result = zaiavleniaMap.get(key) + ".jsf";
					result = zaiavleniaMap.get(key) + ".xhtml?faces-redirect=true&ccevID=" + ccevID;
					
					break;
				}
		
			}
		}	
		
		return result;
	}
	
	public void actionSelectZaiavlenie(Object[] row) {
		if(row!=null) {
			try {
				// проверка за заключен обект EGOVMESSAGE,ако е заключено,крия бутона за запис,но позволявам разглеждане 
				lockOk = checkForLock(SearchUtils.asInteger(row[0]));
				
				egovMess = daoEgov.findById(SearchUtils.asInteger(row[0]));
				egovFilesList = new EgovMessagesDAO(getUserData()).findFilesByMessage(egovMess.getId());
				statusMess = egovMess.getMsgStatus();
			} catch (DbErrorException e) {
				LOGGER.error("Грешка при работа с базата!", e);
				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());	
			}catch (Exception e) {
				LOGGER.error("Грешка при зареждане данните!", e);
				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());	
			}
		}
	}
	
	/**
	 * Проверка за заключенo заявление
	 * @param idObj
	 * @return
	 */
	private boolean checkForLock(Integer idObj) {
		boolean res = true;
		LockObjectDAO daoL = new LockObjectDAO();		
		try { 
			Object[] obj = daoL.check(getUserData().getUserId(), DocuConstants.CODE_ZNACHENIE_JOURNAL_EGOVMESSAGE, idObj);
			if (obj != null) {
				 res = false;
				 lockFrom = getSystemData().decodeItem(DocuConstants.CODE_CLASSIF_ADMIN_STR, Integer.valueOf(obj[0].toString()), getUserData().getCurrentLang(), new Date())   
						       + " / " + DateUtils.printDate((Date)obj[1]);
				// JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_WARN, "Заявлението е заключено от:", lockFrom);
			}
		} catch (DbErrorException e) {
			LOGGER.error("Грешка при проверка за заключенo заявление! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
		}
		return res;
	}
	
	public void actionSaveStatus() {
		// пак проверявам дали някой не го е заключил
		lockOk = checkForLock(egovMess.getId());
		
		if(!lockOk) {
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_WARN, "Заявлението е заключено от:", lockFrom);
			PrimeFaces.current().executeScript("PF('dialogViewMessage').hide();");
			PrimeFaces.current().executeScript("scrollToErrors()");
		}else {		
			try {
				if(statusMess!=null && !statusMess.equals(egovMess.getMsgStatus())) {
					this.egovMess.setMsgStatusDate(new Date());
				}
				JPA.getUtil().runInTransaction(() -> this.egovMess = daoEgov.save(egovMess));
				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, IndexUIbean.getMessageResourceString(UI_beanMessages, SUCCESSAVEMSG));	
				actionSearch();
				PrimeFaces.current().executeScript("PF('dialogViewMessage').hide();");
				PrimeFaces.current().executeScript("scrollToErrors()");
			} catch (BaseException e) {
				LOGGER.error("Грешка при работа с базата!", e);
				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());	
			}catch (Exception e) {
				LOGGER.error("Грешка при запис!", e);
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
	
	public void postProcessXLS(Object document) {
		
		String title = getMessageResourceString(LABELS, "docWSStatus.titleReport");		  
    	new CustomExpPreProcess().postProcessXLS(document, title, dopInfoReport() , null, null);		
     
	}
	
	public void preProcessPDF(Object document)  {
		try{
			
			String title = getMessageResourceString(LABELS, "docWSStatus.titleReport");		
			new CustomExpPreProcess().preProcessPDF(document, title,  dopInfoReport(), null, null);		
						
		} catch (UnsupportedEncodingException e) {
			LOGGER.error(e.getMessage(),e);			
		} catch (IOException e) {
			LOGGER.error(e.getMessage(),e);			
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
	
	public Object[] dopInfoReport() {
		Object[] dopInf = null;
		dopInf = new Object[2];
		if(dateOt != null && dateDo != null) {
			dopInf[0] = "период: "+DateUtils.printDate(dateOt) + " - "+ DateUtils.printDate(dateDo);
		} 
	
		return dopInf;
	}
	
	public Date getToday() {
    	return new Date();
    }



	public Date getDateOt() {
		return dateOt;
	}

	public void setDateOt(Date dateOt) {
		this.dateOt = dateOt;
	}

	public Date getDateDo() {
		return dateDo;
	}

	public void setDateDo(Date dateDo) {
		this.dateDo = dateDo;
	}

	public LazyDataModelSQL2Array getMsgList() {
		return msgList;
	}

	public void setMsgList(LazyDataModelSQL2Array msgList) {
		this.msgList = msgList;
	}

	public Integer getPeriod() {
		return period;
	}

	public void setPeriod(Integer period) {
		this.period = period;
	}

	public Date getDecodeDate() {
		return decodeDate;
	}

	public void setDecodeDate(Date decodeDate) {
		this.decodeDate = decodeDate;
	}

	public List<SelectItem> getMsgStatusList() {
		return msgStatusList;
		
	}

	public void setMsgStatusList(List<SelectItem> msgStatusList) {
		this.msgStatusList = msgStatusList;
		
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}


	public List<SelectItem> getDocVidSelectItem() {
		return docVidSelectItem;
		
	}


	public void setDocVidSelectItem(List<SelectItem> docVidSelectItem) {
		this.docVidSelectItem = docVidSelectItem;
		
	}


	public Integer getSelectedVidDoc() {
		return selectedVidDoc;
		
	}


	public void setSelectedVidDoc(Integer selectedVidDoc) {
		this.selectedVidDoc = selectedVidDoc;
		
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


	public boolean isLockOk() {
		return lockOk;
		
	}


	public void setLockOk(boolean lockOk) {
		this.lockOk = lockOk;
		
	}


	public String getLockFrom() {
		return lockFrom;
		
	}


	public void setLockFrom(String lockFrom) {
		this.lockFrom = lockFrom;
		
	}


	public String getRnDoc() {
		return rnDoc;
	}


	public void setRnDoc(String rnDoc) {
		this.rnDoc = rnDoc;
	}	
}