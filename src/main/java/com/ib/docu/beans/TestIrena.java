package com.ib.docu.beans;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import com.ib.docu.db.dao.DocDAO;
import com.ib.docu.db.dao.MMSDopPolDAO;
import com.ib.docu.db.dto.Doc;
import com.ib.docu.db.dto.MMSDopPol;
import com.ib.docu.system.DocuConstants;
import com.ib.indexui.system.IndexUIbean;
import com.ib.indexui.utils.JSFUtils;
import com.ib.system.db.JPA;
import com.ib.system.db.dao.FilesDAO;
import com.ib.system.db.dto.Files;
import com.ib.system.exceptions.BaseException;
import com.ib.system.exceptions.ObjectInUseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named
@ViewScoped
public class TestIrena extends IndexUIbean  implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7767572564756526112L;
	private static final Logger LOGGER = LoggerFactory.getLogger(TestIrena.class);
	
	private Date decodeDate = new Date();
	private transient DocDAO docDao;
	private Doc doc;
	
	private List<Files> filesList;
	
	FilesDAO filesDao;
	private MMSDopPol dopPol = new MMSDopPol();
	
	@PostConstruct
	void initData() {
		
		
		LOGGER.debug("PostConstruct!!!");	
		docDao = new DocDAO(getUserData());
		filesDao = new FilesDAO(getUserData());

				try {
					JPA.getUtil().runWithClose( () -> doc = docDao.findById(Integer.valueOf(2006)));
					JPA.getUtil().runWithClose( () -> this.filesList = filesDao.selectByFileObjectDop(this.doc.getId(), DocuConstants.CODE_ZNACHENIE_JOURNAL_DOC));
					
				} catch (BaseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			
	}					
	
	public void actionTestDopPol() {
		dopPol = new MMSDopPol();
	
		dopPol.setCodeObject(-1);
		dopPol.setIdObekt(-1);
		//dopPol.setPored(-1);
		dopPol.setZnKod(-1);
		dopPol.setIdPole(21);
		MMSDopPolDAO dopPolDao = new MMSDopPolDAO(getUserData());
		
		try {
			
			JPA.getUtil().runInTransaction(() -> dopPol = dopPolDao.save(dopPol));
		
			
		} catch (ObjectInUseException  e) {		
			
			LOGGER.error("ObjectInUseException-> {}", e.getMessage());
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());	
		} catch (BaseException e) {	
			
			LOGGER.error("Грешка при запис ! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());	
		}
	}
	
	public Date getDecodeDate() {
		return decodeDate;
	}

	public void setDecodeDate(Date decodeDate) {
		this.decodeDate = decodeDate;
	}

	public List<Files> getFilesList() {
		return filesList;
	}

	public void setFilesList(List<Files> filesList) {
		this.filesList = filesList;
	}

	public Doc getDoc() {
		return doc;
		
	}

	public Doc setDoc(Doc doc) {
		this.doc = doc;
		
		return doc;
	}
}