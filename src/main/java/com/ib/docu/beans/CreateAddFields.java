package com.ib.docu.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.model.SelectItem;
import javax.inject.Named;

import org.omnifaces.cdi.ViewScoped;
import org.primefaces.PrimeFaces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.docu.db.dao.MMSAdmEtalDopDAO;
import com.ib.docu.db.dao.MMSDopPolDAO;
import com.ib.docu.db.dto.MMSAdmEtalDop;
import com.ib.docu.db.dto.MMSDopPol;
import com.ib.docu.system.DocuConstants;
import com.ib.indexui.system.Constants;
import com.ib.indexui.system.IndexUIbean;
import com.ib.indexui.utils.JSFUtils;
import com.ib.system.db.JPA;
import com.ib.system.exceptions.BaseException;
import com.ib.system.exceptions.DbErrorException;

@Named(value = "createAddFields")
@ViewScoped
public class CreateAddFields extends IndexUIbean implements Serializable {			
	
	/**
	 * Въвеждане / актуализация на допълнителни полета към спортните регистри
	 * 
	 */
	private static final long serialVersionUID = 7333285442491891584L;
	private static final Logger LOGGER = LoggerFactory.getLogger(CreateAddFields.class);
	
	
	private static final String FORM_ADD_FIELDS = "formAddFields";
	
	private List<SelectItem> objectsList;
	private List<SelectItem> tipeList;
	private transient MMSAdmEtalDopDAO admDAO;
	private MMSAdmEtalDop admDop;
	
	private List<MMSAdmEtalDop> admEtalDopList;	
	private Integer object;
	//private String errMsg = null;
	
	
	

	/** 
	 * 
	 * 
	 **/
	@PostConstruct
	public void initData() {
		
		LOGGER.debug("PostConstruct - CreateAddFields!!!");
			
		this.admDAO = new MMSAdmEtalDopDAO(getUserData());
		
		this.objectsList = new ArrayList<>();
		this.tipeList = new ArrayList<>();
		this.admEtalDopList = new ArrayList<>();
		this.admDop = new MMSAdmEtalDop();
		
			
			try {
				
				this.objectsList.add(new SelectItem(DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORT_OBED,getSystemData().decodeItem(Integer.valueOf(2), 
						DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORT_OBED, getUserData().getCurrentLang(), new Date())));			
		
				this.objectsList.add(new SelectItem(DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_FORMATIONS,getSystemData().decodeItem(Integer.valueOf(2), 
						DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_FORMATIONS, getUserData().getCurrentLang(), new Date())));
				
				this.objectsList.add(new SelectItem(DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_OBJECTS,getSystemData().decodeItem(Integer.valueOf(2), 
						DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_OBJECTS, getUserData().getCurrentLang(), new Date())));
			
				this.objectsList.add(new SelectItem(DocuConstants.CODE_ZNACHENIE_JOURNAL_COACHES,getSystemData().decodeItem(Integer.valueOf(2), 
						DocuConstants.CODE_ZNACHENIE_JOURNAL_COACHES, getUserData().getCurrentLang(), new Date())));
				
				this.objectsList.add(new SelectItem(DocuConstants.CODE_ZNACHENIE_JOURNAL_TASK,getSystemData().decodeItem(Integer.valueOf(2), 
						DocuConstants.CODE_ZNACHENIE_JOURNAL_TASK, getUserData().getCurrentLang(), new Date())));			
			
				this.tipeList.add(new SelectItem(Constants.CODE_ZNACHENIE_ATTRIB_TEKST,getSystemData().decodeItem(Constants.CODE_CLASIF_IM_ELEMENT_TYPES, 
						Constants.CODE_ZNACHENIE_ATTRIB_TEKST, getUserData().getCurrentLang(), new Date())));
				
				this.tipeList.add(new SelectItem(Constants.CODE_ZNACHENIE_ATTRIB_NUMBER ,getSystemData().decodeItem(Constants.CODE_CLASIF_IM_ELEMENT_TYPES, 
						Constants.CODE_ZNACHENIE_ATTRIB_NUMBER , getUserData().getCurrentLang(), new Date())));	
				
				this.tipeList.add(new SelectItem(Constants.CODE_ZNACHENIE_ATTRIB_DATE,getSystemData().decodeItem(Constants.CODE_CLASIF_IM_ELEMENT_TYPES, 
						Constants.CODE_ZNACHENIE_ATTRIB_DATE, getUserData().getCurrentLang(), new Date())));
			
				this.tipeList.add(new SelectItem(Constants.CODE_ZNACHENIE_ATTRIB_DATE_TIME,getSystemData().decodeItem(Constants.CODE_CLASIF_IM_ELEMENT_TYPES, 
						Constants.CODE_ZNACHENIE_ATTRIB_DATE_TIME, getUserData().getCurrentLang(), new Date())));
			
				this.tipeList.add(new SelectItem(Constants.CODE_ZNACHENIE_ATTRIB_CLASSIFIKACIONEN,getSystemData().decodeItem(Constants.CODE_CLASIF_IM_ELEMENT_TYPES, 
						Constants.CODE_ZNACHENIE_ATTRIB_CLASSIFIKACIONEN, getUserData().getCurrentLang(), new Date())));
		
			} catch (DbErrorException e) {
				LOGGER.error("Грешка при зареждане на списъците с обекти и тип полета! ", e);
				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
			}
			
		actionSearchList();		
	}
	
	public void actionSearchList() {

		try {

			JPA.getUtil().runWithClose(() -> this.admEtalDopList = this.admDAO.findByIdObject(object)); // това ти е списъка за таблицата - трябва да го заредиш, след като избереш обекта
		
		} catch (BaseException e) {
			LOGGER.error("Грешка при зареждане на списъка с допълнителни полета към обект! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
		}
	}
	
	private boolean checkData() {

		boolean save = false;
		
		if(this.object == null) {
			JSFUtils.addMessage(FORM_ADD_FIELDS + ":object", FacesMessage.SEVERITY_ERROR,
					getMessageResourceString(UI_beanMessages, MSGPLSINS, getMessageResourceString(LABELS, "createAddFields.object")));
			save = true;
		} else {
			
				
			if(admDop.getTipPole() == null) {
				JSFUtils.addMessage(FORM_ADD_FIELDS + ":tipe", FacesMessage.SEVERITY_ERROR,
						getMessageResourceString(UI_beanMessages, MSGPLSINS, getMessageResourceString(LABELS, "createAddFields.tipe")));
				save = true;
			}
			
			if(admDop.getImePole() == null || "".equals(admDop.getImePole())) {
				JSFUtils.addMessage(FORM_ADD_FIELDS + ":name", FacesMessage.SEVERITY_ERROR,
						getMessageResourceString(UI_beanMessages, MSGPLSINS, getMessageResourceString(LABELS, "createAddFields.name")));
				save = true;
			
			} else {
                   for (int i = 0; i < admEtalDopList.size(); i++) {
					
					String imePole = admEtalDopList.get(i).getImePole();
					Integer idPole = admEtalDopList.get(i).getId();
					
					if ((admDop.getId() == null || admDop.getId() != idPole) && admDop.getImePole().trim().equals(imePole.trim())) {
						JSFUtils.addMessage(FORM_ADD_FIELDS + ":name", FacesMessage.SEVERITY_ERROR,
								getMessageResourceString(beanMessages, "createAddFields.existNamePole"));
						save = true;
						break;
					}
				} 		
				}	
			
			if(admDop.getClasif() == null 
					&& (admDop.getTipPole() != null && admDop.getTipPole().equals(Integer.valueOf(Constants.CODE_ZNACHENIE_ATTRIB_CLASSIFIKACIONEN)))) {
				JSFUtils.addMessage(FORM_ADD_FIELDS + ":clasif", FacesMessage.SEVERITY_ERROR,
						getMessageResourceString(UI_beanMessages, MSGPLSINS, getMessageResourceString(LABELS, "createAddFields.clasif")));
				save = true;
			}
			
			if(admDop.getZad() == null) {
				JSFUtils.addMessage(FORM_ADD_FIELDS + ":rec", FacesMessage.SEVERITY_ERROR,
						getMessageResourceString(UI_beanMessages, MSGPLSINS, getMessageResourceString(LABELS, "createAddFields.rec")));
				save = true;
			}
			
			if(admDop.getPovt() == null) {
				JSFUtils.addMessage(FORM_ADD_FIELDS + ":rep", FacesMessage.SEVERITY_ERROR,
						getMessageResourceString(UI_beanMessages, MSGPLSINS, getMessageResourceString(LABELS, "createAddFields.rep")));
				save = true;
			}
			
			if(admDop.getPored() == null) {
				JSFUtils.addMessage(FORM_ADD_FIELDS + ":basic", FacesMessage.SEVERITY_ERROR,
						getMessageResourceString(UI_beanMessages, MSGPLSINS, getMessageResourceString(LABELS, "createAddFields.redNum")));
				save = true;
			
			} else { // ако поредния номер не е празен се прави проверка за повтарящ се номер
				
				if(this.admEtalDopList !=null && !this.admEtalDopList.isEmpty()) { // ако има списък с полета трябва да се обиколи списъка 
					for(MMSAdmEtalDop rowDopPole: this.admEtalDopList) { //обиколя се списъка 
						// ако ид е нулл ИЛИ ид-то е различно от това в списъка И поредния номер от страницата е равен на поредния номер от ред в списъка - изкарва съобщение да се смени поредния номер
						if ((admDop.getId() == null || admDop.getId() != rowDopPole.getId()) &&  admDop.getPored().equals(rowDopPole.getPored())) { 
							JSFUtils.addMessage(FORM_ADD_FIELDS + ":basic", FacesMessage.SEVERITY_ERROR, 
									getMessageResourceString(beanMessages, "createAddFields.existPoredNum"));
							save = true;
							break;						
						}
					}
				}
			}
		}
		
		return save;	
	}	
	
	
	public void actionNew() {		
		
		this.admDop = new MMSAdmEtalDop();
		
		setPored(); 
	}
	
	private void setPored() {
		
		Integer pored = 1; //винаги задава пореден номер 1 - ако е няма нито един запис - да се сет-не 1
		
		if(this.admEtalDopList !=null && !this.admEtalDopList.isEmpty()) { // ако има списък с полета трябва да се обиколи списъка 
			
			for(MMSAdmEtalDop rowDopPole: this.admEtalDopList) { //обиколя се списъка и се провери до кой номер е поредния номер
				
				if(rowDopPole.getPored() >= pored) { // ако поредния номер от реда в списъка е по-голям или равен на pored
					pored = rowDopPole.getPored() + 1; // увеличава се променливата с 1 и се продължава така докато стигне до последния свободен номер
				}
			}
		}	
		
		this.admDop.setPored(pored); // накра се сет-ва на поредния номер този номер, до който е стигнал при проверката			
	}
	
	public void actionEdit(Integer idObj) {	
		
		try {
			
			if (idObj != null) {
				
				JPA.getUtil().runWithClose(() -> this.admDop = admDAO.findById(idObj));
			}			

		} catch (BaseException e) {
			LOGGER.error("Грешка при редактиране! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
		}		
		
	}

	public void actionSave() {
		
		if(checkData()) {
			return;
		}
		
		try {
			
			this.admDop.setCodeObject(this.object); 
			
			JPA.getUtil().runInTransaction(() ->  this.admDop = admDAO.save(this.admDop)); // запис на доп. поле
		
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, getMessageResourceString(UI_beanMessages, SUCCESSAVEMSG));
			
			String dialogWidgetVar = "PF('dlg2').hide();"; // така се затваря автоматично модалния след успешен запис, но ако има грешки трябва да стои отворен на екрана
			PrimeFaces.current().executeScript(dialogWidgetVar);
			
			actionSearchList();	//презареждане на списъка с допълнителни полета, за да се добави и новото			
			
		} catch (BaseException e) {			
			LOGGER.error("Грешка при запис на допълнително поле! ", e);	
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
		}	
		
	}
		
	public void actionDelete() {
       try {			
			
			JPA.getUtil().runInTransaction(() -> {
				
				List<MMSDopPol> dopPolList = new MMSDopPolDAO(getUserData()).findByIdPole(admDop.getId());
				
				if(!dopPolList.isEmpty()) { 
					
					JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, getMessageResourceString(beanMessages, "createAddF.noDel"));
					
				} else { 
					
					admDAO.delete(this.admDop);
					JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, getMessageResourceString(UI_beanMessages, "general.successDeleteMsg"));
					
					String dialogWidgetVar = "PF('dlg2').hide();"; // така се затваря автоматично модалния след успешно изтриване
					PrimeFaces.current().executeScript(dialogWidgetVar);
				}			
			});
			
			actionSearchList();

		} catch (BaseException e) {
			LOGGER.error("Грешка при изтриване! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
		}	
	}
	
	public MMSAdmEtalDop getAdmDop() {
		return admDop;
	}

	public void setAdmDop(MMSAdmEtalDop mmsAdmEtalDop) {
		this.admDop = mmsAdmEtalDop;
	}

	public List<SelectItem> getObjectsList() {
		return objectsList;
	}

	public void setObjectsList(List<SelectItem> objectsList) {
		this.objectsList = objectsList;
	}

	public List<SelectItem> getTipeList() {
		return tipeList;
	}

	public void setTipeList(List<SelectItem> tipeList) {
		this.tipeList = tipeList;
	}


	public List<MMSAdmEtalDop> getAdmEtalDopList() {
		return admEtalDopList;
	}

	public void setAdmEtalDopList(List<MMSAdmEtalDop> admEtalDopList) {
		this.admEtalDopList = admEtalDopList;
	}
	
	public Integer getObject() {
		return object;
	}

	public void setObject(Integer object) {
		this.object = object;
	}
	
}