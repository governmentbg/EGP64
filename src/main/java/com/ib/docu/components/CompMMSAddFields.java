package com.ib.docu.components;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.component.FacesComponent;
import javax.faces.component.UINamingContainer;

import com.ib.docu.db.dao.MMSAdmEtalDopDAO;
import com.ib.docu.db.dao.MMSDopPolDAO;
import com.ib.docu.db.dto.MMSAdmEtalDop;
import com.ib.docu.db.dto.MMSDopPol;
import com.ib.docu.system.DocuConstants;
import com.ib.docu.system.SystemData;
import com.ib.docu.system.UserData;
import com.ib.indexui.system.Constants;
import com.ib.indexui.system.IndexUIbean;
import com.ib.indexui.utils.JSFUtils;
import com.ib.system.db.JPA;
import com.ib.system.db.dto.SystemClassif;
import com.ib.system.exceptions.BaseException;
import com.ib.system.exceptions.DbErrorException;
import com.ib.system.exceptions.ObjectInUseException;
import com.ib.system.utils.SearchUtils;

import org.hibernate.JDBCException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** */
@FacesComponent(value = "compMMSAddFields", createTag = true)
public class CompMMSAddFields extends UINamingContainer {
	
	private enum PropertyKeys {
		ETALDOPLIST, DOPPOLLIST, DOPPOL, ETALDOP, SELECTEDCODES, SELECTEDCLASSIFS, IDMANYCLASSIFS
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(CompMMSAddFields.class);
	public static final String	UIBEANMESSAGES = "ui_beanMessages";
	public static final String	BEANMESSAGES = "beanMessages";
	public static final String  MSGPLSINS = "general.pleaseInsert";
	public static final String  ERRDATABASEMSG = "general.errDataBaseMsg";
	public static final String	LABELS = "labels";
	public static final String  SUCCESSAVEMSG = "general.succesSaveMsg";
	private String errMsg = null;
	private String infoMsg = null;
	private UserData userData	= null;
	private Date dateClassif	= null;
		
	public void initCmp() {		
		
		Integer codeObj = (Integer) getAttributes().get("codeObj");	
		Integer idObj = (Integer) getAttributes().get("idObj");
		try {
			setEtalDopList(new MMSAdmEtalDopDAO(getUserData()).findByIdObject(codeObj));
			setDopPolList(new MMSDopPolDAO(getUserData()).findByIdObjAndCode(idObj, codeObj));
		} catch (DbErrorException e) {
			LOGGER.error("Грешка при работа с базата! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
		} catch (Exception e) {
			LOGGER.error("Грешка при работа със системата! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
		}
		
	}

	
	public void actionSelectField(MMSAdmEtalDop row) {
		
		if(row!=null) {
			if(row.getPovt().equals(Constants.CODE_ZNACHENIE_DA)) { //ако е повтараемо,можем да позволим нов ред
				setPol(row);
				if(row.getTipPole()==DocuConstants.CODE_ZNACHENIE_ATTRIB_CLASSIFIKACIONEN) {
					getSavedCodes();
				}
			}else if(getDopPolList()!=null && !getDopPolList().isEmpty()) { //ако не може да се повтаря,проверяваме дали вече е въведено
				boolean isFound = false;
				for(Object[] item: getDopPolList()) {
					if(row.getId().equals(SearchUtils.asInteger( item[4])) ) { //проверяваме по ид
						errMsg = IndexUIbean.getMessageResourceString(BEANMESSAGES, "compMMSAddFields.dubFields") ;	
						setDopPol(new MMSDopPol());
						setEtalDop(new MMSAdmEtalDop());
						isFound = true;
						break;
					}
				}
				
				if(!isFound) { // ако не намерим въвдено
					setPol(row);
				}
			}else { //ако няма въведени полета
				setPol(row);
			}
			
		}
	}
	
	private void setPol(MMSAdmEtalDop row) {
		
//		Integer nomer = 1; //за да зададем пореден номер 
//		if(getDopPolList()!=null && !getDopPolList().isEmpty()) {
//			for(Object[] item: getDopPolList()) {
//				if(SearchUtils.asInteger(item[8])>=nomer) {
//					nomer = SearchUtils.asInteger(item[8])+1;
//				}
//			}
//		}		
		setEtalDop(row);
		setDopPol(new MMSDopPol());
	//	getDopPol().setPored(nomer);
		getDopPol().setCodeObject(row.getCodeObject());
		getDopPol().setIdPole(row.getId());
		Integer idObj = (Integer) getAttributes().get("idObj");		
		getDopPol().setIdObekt(idObj);
	}
	
	private void getSavedCodes() {// кои кодове са били вече въведени при множествен избор от класификация
		setSelectedClassifs(new ArrayList<SystemClassif>());
		setSelectedCodes(new ArrayList<Integer>());
		setIdManyClassifs(new ArrayList<Integer>());
		try {
			for(Object[] item: getDopPolList()) {
				if(item[6]!=null && SearchUtils.asInteger(item[9])==Constants.CODE_ZNACHENIE_DA && getEtalDop().getClasif().equals(SearchUtils.asInteger(item[6]))) {
					getSelectedCodes().add(SearchUtils.asInteger(item[1]));
					getIdManyClassifs().add(SearchUtils.asInteger(item[0]));
					
						SystemClassif sysC = new SystemData().decodeItemLite(getEtalDop().getClasif(), SearchUtils.asInteger(item[1]), getLang(), dateClassif, false);
					    getSelectedClassifs().add(sysC);
					
				}
			}
		} catch (DbErrorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	} 
	
	public void actionEdit(Object[] row) {
		try {
			
			setDopPol(new MMSDopPolDAO(getUserData()).findById(SearchUtils.asInteger(row[0])));
			setEtalDop(new MMSAdmEtalDopDAO(getUserData()).findById(SearchUtils.asInteger(row[4])));
			if(getEtalDop().getPovt().equals(Constants.CODE_ZNACHENIE_DA) && getEtalDop().getTipPole()==DocuConstants.CODE_ZNACHENIE_ATTRIB_CLASSIFIKACIONEN&&
					getEtalDop().getClasif().equals(SearchUtils.asInteger(row[6]))) { 	
					getSavedCodes();
			}
		} catch (DbErrorException e) {
			LOGGER.error("Грешка при работа с базата! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
		} catch (Exception e) {
			LOGGER.error("Грешка при работа със системата! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
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
	
	public void actionSave() {
		
		if(checkData()) {
			return;
		}
        try {
        	MMSDopPolDAO dopPolDao = new MMSDopPolDAO(getUserData());
        	if(getEtalDop().getPovt().equals(Constants.CODE_ZNACHENIE_DA) && getEtalDop().getTipPole()==DocuConstants.CODE_ZNACHENIE_ATTRIB_CLASSIFIKACIONEN) {
        		JPA.getUtil().runInTransaction(() -> { 
    				if(getIdManyClassifs()!=null&& !getIdManyClassifs().isEmpty()){
    					for(Integer item:getIdManyClassifs()){
    						dopPolDao.deleteById(item);
    					}
    				}
    				
    				if(getSelectedCodes()!=null && !getSelectedCodes().isEmpty()) {
    					for(Integer item: getSelectedCodes()) {
    						MMSDopPol dopPolTmp = new MMSDopPol();
    						dopPolTmp.setCodeObject(getDopPol().getCodeObject());
    						dopPolTmp.setIdObekt(getDopPol().getIdObekt());
    						dopPolTmp.setIdPole(getDopPol().getIdPole());
    						dopPolTmp.setZnKod(item);
    						
    						dopPolDao.save(dopPolTmp);
    					}
    				}
    			});
        	}else {   
	        	JPA.getUtil().runInTransaction(() -> setDopPol(dopPolDao.save(getDopPol())));        	
        	}
        	
        	setDopPolList(new MMSDopPolDAO(getUserData()).findByIdObjAndCode(getDopPol().getIdObekt(), getDopPol().getCodeObject()));
			setDopPol(null);	 
			//JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, IndexUIbean.getMessageResourceString(UIBEANMESSAGES, SUCCESSAVEMSG));	
			infoMsg = IndexUIbean.getMessageResourceString(UIBEANMESSAGES, SUCCESSAVEMSG) + "<br/>";	
			setEtalDop(new MMSAdmEtalDop());
			
		} catch (ObjectInUseException  e) {		
			
			LOGGER.error("ObjectInUseException-> {}", e.getMessage());
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());	
		} catch (BaseException e) {	
			
			LOGGER.error("Грешка при запис ! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());	
		} catch (JDBCException e) {
		    e.getSQLException().getNextException().printStackTrace();
		}
	}
	
	private boolean checkData() {
		errMsg =" ";
		boolean save = false;
		//FacesContext context = FacesContext.getCurrentInstance();
		//String clientId = this.getClientId(context);
		if(getEtalDop()==null || getEtalDop().getId()==null) {
			errMsg += IndexUIbean.getMessageResourceString(BEANMESSAGES, "compMMSAddFields.selectField") + "<br/>";	
			save = true;
		}else {
			if(getEtalDop().getTipPole().equals(DocuConstants.CODE_ZNACHENIE_ATTRIB_TEKST)) {
				if( getDopPol().getZnStr()== null|| "".equals(getDopPol().getZnStr())) {
//					JSFUtils.addMessage(clientId + ":teskt", FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(UIBEANMESSAGES, MSGPLSINS, 
//							IndexUIbean.getMessageResourceString(LABELS, "compAddFields.znachenie")));
					errMsg += IndexUIbean.getMessageResourceString(UIBEANMESSAGES, MSGPLSINS, IndexUIbean.getMessageResourceString(LABELS, "compAddFields.znachenie")) + "<br/>";	
					save = true;
				}
			}else if(getEtalDop().getTipPole().equals(DocuConstants.CODE_ZNACHENIE_ATTRIB_DATE)) {
				if( getDopPol().getZnDate()== null) {
//					JSFUtils.addMessage(clientId + ":data", FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(UIBEANMESSAGES, MSGPLSINS, 
//							IndexUIbean.getMessageResourceString(LABELS, "compAddFields.znachenie")));
					errMsg += IndexUIbean.getMessageResourceString(UIBEANMESSAGES, MSGPLSINS, IndexUIbean.getMessageResourceString(LABELS, "compAddFields.znachenie")) + "<br/>";	
					save = true;
				}
			}else if(getEtalDop().getTipPole().equals(DocuConstants.CODE_ZNACHENIE_ATTRIB_DATE_TIME)) {
				if( getDopPol().getZnDate()== null) {
//					JSFUtils.addMessage(clientId + ":dateTime", FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(UIBEANMESSAGES, MSGPLSINS, 
//							IndexUIbean.getMessageResourceString(LABELS, "compAddFields.znachenie")));
					errMsg += IndexUIbean.getMessageResourceString(UIBEANMESSAGES, MSGPLSINS, IndexUIbean.getMessageResourceString(LABELS, "compAddFields.znachenie")) + "<br/>";	
					save = true;
				}
			}else if(getEtalDop().getTipPole().equals(DocuConstants.CODE_ZNACHENIE_ATTRIB_CLASSIFIKACIONEN)) {
				if(getEtalDop().getPovt()==Constants.CODE_ZNACHENIE_NE && getDopPol().getZnKod()== null) {				
//					JSFUtils.addMessage(clientId + ":classif:аutoCompl_input", FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(UIBEANMESSAGES, MSGPLSINS, 
//							IndexUIbean.getMessageResourceString(LABELS, "compAddFields.znachenie")));
					errMsg += IndexUIbean.getMessageResourceString(UIBEANMESSAGES, MSGPLSINS, IndexUIbean.getMessageResourceString(LABELS, "compAddFields.znachenie")) + "<br/>";	
					save = true;
				}
			}else if(getEtalDop().getTipPole().equals(DocuConstants.CODE_ZNACHENIE_ATTRIB_NUMBER)) {
				if( getDopPol().getZnKod()== null) {
//					JSFUtils.addMessage(clientId + ":number", FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(UIBEANMESSAGES, MSGPLSINS, 
//							IndexUIbean.getMessageResourceString(LABELS, "compAddFields.znachenie")));
					errMsg += IndexUIbean.getMessageResourceString(UIBEANMESSAGES, MSGPLSINS, IndexUIbean.getMessageResourceString(LABELS, "compAddFields.znachenie")) + "<br/>";	
					save = true;
				}
			}
			
//			if(getDopPol().getPored()==null) {
//				JSFUtils.addMessage(clientId + ":nomPored", FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(UIBEANMESSAGES, MSGPLSINS, 
//						IndexUIbean.getMessageResourceString(LABELS, "createAddFields.redNum")));
//				errMsg += IndexUIbean.getMessageResourceString(UIBEANMESSAGES, MSGPLSINS, IndexUIbean.getMessageResourceString(LABELS, "createAddFields.redNum")) + "<br/>";	
//				save = true;	
//			}else if(getDopPolList()!=null && !getDopPolList().isEmpty()){
//				for(Object[] item: getDopPolList()) {
//					if((getDopPol().getId()==null || !getDopPol().getId().equals(SearchUtils.asInteger(item[0]))) && getDopPol().getPored().equals(SearchUtils.asInteger( item[8])) ) {
//						JSFUtils.addMessage(clientId + ":nomPored", FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(BEANMESSAGES, "compMMSAddFields.dubPored"));
//						errMsg += IndexUIbean.getMessageResourceString(BEANMESSAGES, "compMMSAddFields.dubPored") ;	
//						save=true;
//						break;
//					}
//				}
//			}
		}
		return save;
	}
	
	public void deleteAll() {
			setDopPol(new MMSDopPol());
			setEtalDop(new MMSAdmEtalDop());
		try {
			
			MMSDopPolDAO dopPolDao = new MMSDopPolDAO(getUserData());
			JPA.getUtil().runInTransaction(() -> { 
				
				for(Object[]item:getDopPolList()) {
					dopPolDao.deleteById(SearchUtils.asInteger(item[0]));
				}
				infoMsg = IndexUIbean.getMessageResourceString(UIBEANMESSAGES, "general.successDeleteMsg") + "<br/>";			
				setDopPolList(new ArrayList<Object[]>());
				
			});
		} catch (BaseException e) {
			LOGGER.error("Грешка при изтриване на допълнителни полета! ", e); 
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(UIBEANMESSAGES, ERRDATABASEMSG), e.getMessage());
		}catch (Exception e) {
			LOGGER.error("Грешка при работа със системата! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
		}
	}

	public void deleteRow(Object [] row) {
		setDopPol(new MMSDopPol());
		setEtalDop(new MMSAdmEtalDop());
		if(row!=null) {
			try {
				JPA.getUtil().runInTransaction(() -> {

					new MMSDopPolDAO(getUserData()).deleteById(SearchUtils.asInteger(row[0]));
					infoMsg = IndexUIbean.getMessageResourceString(UIBEANMESSAGES, "general.successDeleteMsg") + "<br/>";	
					Integer codeObj = (Integer) getAttributes().get("codeObj");	
					Integer idObj = (Integer) getAttributes().get("idObj");		
					setDopPolList(new MMSDopPolDAO(getUserData()).findByIdObjAndCode(idObj, codeObj));
				});
			} catch (BaseException e) {
				LOGGER.error("Грешка при изтриване на поле! ", e); 
				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(UIBEANMESSAGES, ERRDATABASEMSG), e.getMessage());
			}

		}
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
	public List<MMSAdmEtalDop> getEtalDopList() {
		return (List<MMSAdmEtalDop>) getStateHelper().eval(PropertyKeys.ETALDOPLIST, null);		
	}

	public void setEtalDopList(List<MMSAdmEtalDop> etalDopList) {
		getStateHelper().put(PropertyKeys.ETALDOPLIST, etalDopList);		
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<Object[]> getDopPolList() {
		return (ArrayList<Object[]>) getStateHelper().eval(PropertyKeys.DOPPOLLIST, null);		
	}

	public void setDopPolList(ArrayList<Object[]> dopPolList) {
		getStateHelper().put(PropertyKeys.DOPPOLLIST, dopPolList);		
	}
	
	public MMSDopPol getDopPol() {
		return (MMSDopPol) getStateHelper().eval(PropertyKeys.DOPPOL, null);		
	}

	public void setDopPol(MMSDopPol dopPol) {
		getStateHelper().put(PropertyKeys.DOPPOL, dopPol);				
	}
		
	public MMSAdmEtalDop getEtalDop() {
		return (MMSAdmEtalDop) getStateHelper().eval(PropertyKeys.ETALDOP, null);		
	}

	public void setEtalDop(MMSAdmEtalDop etalDop) {
		getStateHelper().put(PropertyKeys.ETALDOP, etalDop);		
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<Integer> getSelectedCodes() {
		return (ArrayList<Integer>) getStateHelper().eval(PropertyKeys.SELECTEDCODES, null);		
	}

	public void setSelectedCodes(ArrayList<Integer> selectedCodes) {
		getStateHelper().put(PropertyKeys.SELECTEDCODES, selectedCodes);		
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<SystemClassif> getSelectedClassifs() {
		return (ArrayList<SystemClassif>) getStateHelper().eval(PropertyKeys.SELECTEDCLASSIFS, null);		
	}

	public void setSelectedClassifs(ArrayList<SystemClassif> selectedClassifs) {
		getStateHelper().put(PropertyKeys.SELECTEDCLASSIFS, selectedClassifs);		
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<Integer> getIdManyClassifs() {
		return (ArrayList<Integer>) getStateHelper().eval(PropertyKeys.IDMANYCLASSIFS, null);		
	}

	public void setIdManyClassifs(ArrayList<Integer> idManyClassifs) {
		getStateHelper().put(PropertyKeys.IDMANYCLASSIFS, idManyClassifs);		
	}
	
//	public Integer getTipPol() {
//		return (Integer) getStateHelper().eval(PropertyKeys.TIPPOL, null);		
//	}
//
//	public void setTipPol(Integer tipPol) {
//		getStateHelper().put(PropertyKeys.TIPPOL, tipPol);		
//	}
//	
//	public Integer getClassif() {
//		return (Integer) getStateHelper().eval(PropertyKeys.CLASSIF, null);		
//	}
//
//	public void setClassif(Integer classif) {
//		getStateHelper().put(PropertyKeys.CLASSIF, classif);		
//	}

	public String getInfoMsg() {
		return infoMsg;
		
	}

	public void setInfoMsg(String infoMsg) {
		this.infoMsg = infoMsg;
		
	}
}