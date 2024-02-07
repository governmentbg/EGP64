package com.ib.docu.beans;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.model.SelectItem;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.docu.db.dao.EgovMessagesDAO;
import com.ib.docu.system.DocuConstants;
import com.ib.indexui.system.IndexUIbean;
import com.ib.indexui.utils.JSFUtils;
import com.ib.system.db.JPA;
import com.ib.system.db.dao.FilesDAO;
import com.ib.system.db.dto.Files;
import com.ib.system.exceptions.BaseException;
import com.ib.system.exceptions.DbErrorException;
import com.ib.system.exceptions.ObjectInUseException;
import com.ib.system.utils.X;

@Named
@ViewScoped
public class UploadZaqvlenieBean extends IndexUIbean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7767572564756526112L;
	private static final Logger LOGGER = LoggerFactory.getLogger(UploadZaqvlenieBean.class);

	private List<Files> filesList;
	private List<SelectItem> docVidSelectItem = new ArrayList<>(); // за да се търси по вид документ
	private Integer selectedVidDoc;
	private String lastFileName;
	FilesDAO filesDao;

	@PostConstruct
	void initData() {
		Date decodeDate=new Date();
		try {
				docVidSelectItem.add(new SelectItem( DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_SPORTEN_OBEKT,
						 getSystemData().decodeItem(DocuConstants.CODE_CLASSIF_DOC_VID, DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_SPORTEN_OBEKT, getCurrentLang(), decodeDate)));
				docVidSelectItem.add(new SelectItem( DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_SPORTNO_FORM,
						 getSystemData().decodeItem(DocuConstants.CODE_CLASSIF_DOC_VID, DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_SPORTNO_FORM, getCurrentLang(), decodeDate)));
				docVidSelectItem.add(new SelectItem( DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_SPORTNO_OBEDINENIE,
						 getSystemData().decodeItem(DocuConstants.CODE_CLASSIF_DOC_VID, DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_SPORTNO_OBEDINENIE, getCurrentLang(), decodeDate)));
				docVidSelectItem.add(new SelectItem( DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_TREN_KADRI,
						 getSystemData().decodeItem(DocuConstants.CODE_CLASSIF_DOC_VID, DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_TREN_KADRI, getCurrentLang(), decodeDate)));
				docVidSelectItem.add(new SelectItem( DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_ZALICH_SPORTEN_OBEKT,
						 getSystemData().decodeItem(DocuConstants.CODE_CLASSIF_DOC_VID, DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_ZALICH_SPORTEN_OBEKT, getCurrentLang(), decodeDate)));
				docVidSelectItem.add(new SelectItem( DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_ZALICH_SPORTNO_FORM,
						 getSystemData().decodeItem(DocuConstants.CODE_CLASSIF_DOC_VID, DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_ZALICH_SPORTNO_FORM, getCurrentLang(), decodeDate)));
				docVidSelectItem.add(new SelectItem( DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_ZALICH_SPORTNO_OBEDINENIE,
						 getSystemData().decodeItem(DocuConstants.CODE_CLASSIF_DOC_VID, DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_ZALICH_SPORTNO_OBEDINENIE, getCurrentLang(), decodeDate)));
				docVidSelectItem.add(new SelectItem( DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_ZALICH_TREN_KADRI,
						 getSystemData().decodeItem(DocuConstants.CODE_CLASSIF_DOC_VID, DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_ZALICH_TREN_KADRI, getCurrentLang(), decodeDate)));
				docVidSelectItem.add(new SelectItem( DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_PROM_SPORTEN_OBEKT,
						 getSystemData().decodeItem(DocuConstants.CODE_CLASSIF_DOC_VID, DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_PROM_SPORTEN_OBEKT, getCurrentLang(), decodeDate)));
				docVidSelectItem.add(new SelectItem( DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_PROM_SPORTNO_FORM,
						 getSystemData().decodeItem(DocuConstants.CODE_CLASSIF_DOC_VID, DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_PROM_SPORTNO_FORM, getCurrentLang(), decodeDate)));
				docVidSelectItem.add(new SelectItem( DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_PROM_SPORTNO_OBEDINENIE,
						 getSystemData().decodeItem(DocuConstants.CODE_CLASSIF_DOC_VID, DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_PROM_SPORTNO_OBEDINENIE, getCurrentLang(), decodeDate)));
				docVidSelectItem.add(new SelectItem( DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_PROM_TREN_KADRI,
						 getSystemData().decodeItem(DocuConstants.CODE_CLASSIF_DOC_VID, DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_PROM_TREN_KADRI, getCurrentLang(), decodeDate)));
				docVidSelectItem.add(new SelectItem( DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_OSK,
						 getSystemData().decodeItem(DocuConstants.CODE_CLASSIF_DOC_VID, DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_OSK, getCurrentLang(), decodeDate)));
				docVidSelectItem.add(new SelectItem( DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_NOSTD,
						 getSystemData().decodeItem(DocuConstants.CODE_CLASSIF_DOC_VID, DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_NOSTD, getCurrentLang(), decodeDate)));
				docVidSelectItem.add(new SelectItem( DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_NOUS,
						 getSystemData().decodeItem(DocuConstants.CODE_CLASSIF_DOC_VID, DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_NOUS, getCurrentLang(), decodeDate)));
				docVidSelectItem.add(new SelectItem( DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_TD,
						 getSystemData().decodeItem(DocuConstants.CODE_CLASSIF_DOC_VID, DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_TD, getCurrentLang(), decodeDate)));
			
		} catch (DbErrorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void changeVidDoc() {
		System.err.println(selectedVidDoc);
	}
	
	public void listenerPrime(FileUploadEvent event)  {
		UploadedFile item = event.getFile();
		String filename = item.getFileName();
		
		if(isISO88591Encoded(filename)){
			filename = new String(filename.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
		}
		
		X<Files> x = X.empty();
		
		Files files = new Files();
		files.setFilename(filename);
		files.setContentType(item.getContentType());
		files.setContent(item.getContent());
		
		lastFileName=null;
		try {
			JPA.getUtil().runInTransaction(() -> { 
				//vpObj.setCountFiles(filesList == null ? 0 : filesList.size());
			
				lastFileName=new EgovMessagesDAO(getUserData()).loadZaiavlenieTest(files, selectedVidDoc, new Date(), getSystemData());
				
			});
			
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
		if (lastFileName!=null) {
			lastFileName="Последно прикачен файл: "+filename+" рег. №: "+lastFileName;	
		}
		
	
		 
	}
	
	/**
	 * Само за wildfly за да не изкарва името на файла на маймуница
	 * @param text
	 * @return
	 */
	private boolean isISO88591Encoded(String text) {
	    String checked = new String(text.getBytes( StandardCharsets.ISO_8859_1), StandardCharsets.ISO_8859_1);
		return checked.equals(text);

	}


	public List<Files> getFilesList() {
		return filesList;
	}

	public void setFilesList(List<Files> filesList) {
		this.filesList = filesList;
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
	public String getLastFileName() {
		return lastFileName;
	}
	public void setLastFileName(String lastFileName) {
		this.lastFileName = lastFileName;
	}

}