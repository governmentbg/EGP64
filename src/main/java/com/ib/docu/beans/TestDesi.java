package com.ib.docu.beans;

import static com.ib.docu.system.DocuConstants.CODE_CLASSIF_DOC_VID;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.primefaces.PrimeFaces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.docu.db.dao.DocDAO;
import com.ib.docu.db.dao.EgovMessagesDAO;
import com.ib.docu.db.dao.MMSVpisvaneDAO;
import com.ib.docu.db.dao.MMSVpisvaneDocDAO;
import com.ib.docu.db.dao.ReferentDAO;
import com.ib.docu.db.dto.Doc;
import com.ib.docu.db.dto.EgovMessages;
import com.ib.docu.db.dto.MMSVpisvane;
import com.ib.docu.db.dto.MMSVpisvaneDoc;
import com.ib.docu.db.dto.Referent;
import com.ib.docu.system.DocuClassifAdapter;
import com.ib.docu.system.DocuConstants;
import com.ib.docu.system.UserData;
import com.ib.indexui.system.Constants;
import com.ib.indexui.system.IndexUIbean;
import com.ib.indexui.utils.JSFUtils;
import com.ib.system.SysConstants;
import com.ib.system.db.JPA;
import com.ib.system.exceptions.BaseException;
import com.ib.system.exceptions.DbErrorException;
import com.ib.system.utils.SearchUtils;


@Named(value = "testDesi")
@ViewScoped
public class TestDesi extends IndexUIbean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5369482613470848017L;

	private static final Logger LOGGER = LoggerFactory.getLogger(TestDesi.class);
	
	private Date decodeDate;
	
	private List<MMSVpisvane> regList = new ArrayList<MMSVpisvane>();
	
	private MMSVpisvane vpisvane = new MMSVpisvane();
	
	private Referent ref = new Referent();
	
	/** 
	 * 
	 * 
	 **/
	@PostConstruct
	public void initData() {
		
		LOGGER.debug("PostConstruct!!!");	
		
		try {			
			
			JPA.getUtil().runWithClose(() -> setRegList(new MMSVpisvaneDAO(getUserData()).findRegsListByIdAndType(91, 37)));					
		
		} catch (BaseException e) {
			LOGGER.error("Грешка при зареждане на списъка с вписвания! ", e);
		}
	
	}
	
	public Date getDecodeDate() {
		return decodeDate;
	}

	public void setDecodeDate(Date decodeDate) {
		this.decodeDate = decodeDate;
	}

	public List<MMSVpisvane> getRegList() {
		return regList;
	}

	public void setRegList(List<MMSVpisvane> regList) {
		this.regList = regList;
	}
	
	public MMSVpisvane getVpisvane() {
		return vpisvane;
	}

	public void setVpisvane(MMSVpisvane vpisvane) {
		this.vpisvane = vpisvane;
	}

	public Referent getRef() {
		return ref;
	}

	public void setRef(Referent ref) {
		this.ref = ref;
	}

	//TODO - Метода е за извикване след запис на обектите след извикване от филтъра на Ирена за документи от СЕОС - трябва да се пренесе във всички класове за обектите!
	public void actionSaveDocFromSeos(Integer vidDoc, String rnDoc, Date dateDoc, String otnosno, String egn, String eik, Integer idEgov) {
		
		Doc newDoc = new Doc();		
		MMSVpisvaneDoc vpisvaneDoc = new MMSVpisvaneDoc();
		
		try {
			// настройка по вид документ и регистратура
			Object[] docVidSetting = new DocDAO(getUserData()).findDocSettings(getUserData(UserData.class).getRegistratura(), vidDoc, getSystemData());
			
			if (docVidSetting == null) {
				
				String noSett = getSystemData().decodeItem(CODE_CLASSIF_DOC_VID, vidDoc, getCurrentLang(), new Date());				
				
				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString("beanMessages", "compReg.noDocSettings", noSett));	
				PrimeFaces.current().executeScript("scrollToErrors()");
				return;
			
			} else {
				
				newDoc.setDocVid(vidDoc);
				newDoc.setRnDoc(rnDoc);
				newDoc.setDocDate(dateDoc);
				
				if (egn != null) {
					JPA.getUtil().runWithClose(() -> ref = new ReferentDAO(getUserData()).findByIdent(null, egn, null, DocuConstants.CODE_ZNACHENIE_REF_TYPE_FZL)); 
				} else if (eik != null) {
					JPA.getUtil().runWithClose(() -> ref = new ReferentDAO(getUserData()).findByIdent(eik, null, null, DocuConstants.CODE_ZNACHENIE_REF_TYPE_NFL)); 
				}
				
				newDoc.setCodeRefCorresp(ref.getCode()); 
				
				newDoc.setRegisterId((Integer) docVidSetting[1]);
				boolean createDelo = Objects.equals(docVidSetting[2], SysConstants.CODE_ZNACHENIE_DA);
				
				Integer typeDocByRegister = (Integer) getSystemData().getItemSpecific(DocuConstants.CODE_CLASSIF_REGISTRI, newDoc.getRegisterId(), getCurrentLang(), new Date() , DocuClassifAdapter.REGISTRI_INDEX_DOC_TYPE);
				
				newDoc.setRegistraturaId(getUserData(UserData.class).getRegistratura());
				newDoc.setDocType(typeDocByRegister);
				newDoc.setFreeAccess(Constants.CODE_ZNACHENIE_DA);				
				
				if (SearchUtils.isEmpty(otnosno)) {
					
					newDoc.setOtnosno(getSystemData().decodeItem(DocuConstants.CODE_CLASSIF_DOC_VID, newDoc.getDocVid(), getCurrentLang(), new Date()) + " на " + " ИМЕ НА ОБЕКТА");
				} else {
					
					newDoc.setOtnosno(otnosno); 
				}
				
				boolean saveNewVp = false;
				
				if (vidDoc.equals(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_SPORTEN_OBEKT)
						|| vidDoc.equals(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_SPORTNO_FORM)
						|| vidDoc.equals(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_SPORTNO_OBEDINENIE)
						|| vidDoc.equals(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_TREN_KADRI) ) { 
					
					saveNewVp = true;
				}
				
				if (saveNewVp) {
					vpisvane.setRnDocZaiavlenie(rnDoc);
					vpisvane.setDateDocZaiavlenie(dateDoc);
					vpisvane.setStatusResultZaiavlenie(DocuConstants.CODE_ZNACHENIE_STATUS_ZAIAVLENIE_V_RAZGLEJDANE);
					vpisvane.setDateStatusZaiavlenie(new Date());
					//vpisvane.setIdObject(); // ИД на обекта
					//vpisvane.setTypeObject(); //КОДА на обекта
				
				} else {
					
					JPA.getUtil().runWithClose(() -> {
						
						this.regList = new MMSVpisvaneDAO(getUserData()).findRegsListByIdAndType(91, 1); // КОДА и ИД на обекта
						
						vpisvane = new MMSVpisvaneDAO(getUserData()).findById(this.regList.get(0).getId()); 
						
						//TODO - започвам логики, ако е за заличаване или промяна на обстоятелства - да се запишат в последното вписване данните	
						//vpisvane.setStatusResultZaiavlenie();
						//vpisvane.setDateStatusZaiavlenie(new Date());
					});	
				}
				
				JPA.getUtil().runInTransaction(() -> { 
					
					new MMSVpisvaneDAO(getUserData()).save(vpisvane);
					
					new DocDAO(getUserData()).save(newDoc, createDelo, null, null, getSystemData());
					
					if (newDoc.getId() != null) {
						
						vpisvaneDoc.setIdObject(vpisvane.getIdObject());
						vpisvaneDoc.setTypeObject(vpisvane.getTypeObject()); 
						vpisvaneDoc.setIdVpisvane(vpisvane.getId());
						vpisvaneDoc.setIdDoc(newDoc.getId());
						
						new MMSVpisvaneDocDAO(getUserData()).save(vpisvaneDoc);
					}
					
					//TODO - дали така ще се ъпдейтва статуса в egov_messages - msg_status и msg_status_dat
					
					EgovMessages egovMess = new EgovMessagesDAO(getUserData()).findById(idEgov); 
					
					egovMess.setMsgRn(newDoc.getRnDoc());
					egovMess.setMsgRnDate(newDoc.getDocDate());
					egovMess.setMsgStatus("DS_REGISTERED");	
					egovMess.setMsgStatusDate(new Date());
					
					new EgovMessagesDAO(getUserData()).save(egovMess);
					
				});					
				
			}
		
		} catch (DbErrorException e) {
			LOGGER.error("Грешка при зареждане настройки по вид документ!! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString("ui_beanMessages", ERRDATABASEMSG), e.getMessage());
		
		} catch (BaseException e) {
			LOGGER.error("Грешка при регистриране на вписване", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString("ui_beanMessages", ERRDATABASEMSG));
		}		
		
	}
}