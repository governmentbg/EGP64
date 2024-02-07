package com.ib.docu.components;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.el.ELContext;
import javax.el.ValueExpression;
import javax.faces.application.FacesMessage;
import javax.faces.component.FacesComponent;
import javax.faces.component.UINamingContainer;

import org.apache.commons.lang.StringUtils;
import org.primefaces.PrimeFaces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.docu.db.dto.Referent;
import com.ib.docu.system.DocuConstants;
import com.ib.docu.system.SystemData;
import com.ib.docu.system.UserData;
import com.ib.docu.utils.RegixUtils;
import com.ib.indexui.utils.JSFUtils;
import com.ib.system.db.dto.SystemClassif;
import com.ib.system.exceptions.DbErrorException;
import com.ib.system.exceptions.InvalidParameterException;
import com.ib.system.utils.ValidationUtils;

/** */
@FacesComponent(value = "mmsSelectReferents", createTag = true)
public class MmsSelectReferents extends UINamingContainer {
	
	private enum PropertyKeys {
		  SEARCHWORD, SHOWME, CODE
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(MmsSelectReferents.class);
	public static final String	BEANMESSAGES	= "beanMessages";


	private SystemData	systemData	= null;
	private UserData	userData	= null;
	private Date		dateClassif	= null;
	
	
	
	/**
	 * Инициалиира комп. в зависимост от типа на реферeнта
	 * @return
	 * @throws DbErrorException
	 */
	public void initRefComp() {	
		
		if (getAttributes().get("selectedCode")!=null) {
			try {
				
				setSearchWord(getSystemData().decodeItem(DocuConstants.CODE_CLASSIF_REFERENTS, (Integer)getAttributes().get("selectedCode"), getUserData().getCurrentLang(), getDateClassif()));

			} catch (DbErrorException e) {
				LOGGER.error("Грешка при работа с базата данни! ", e);
				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,"Грешка при работа с базата данни! ", e.getMessage());
			}		
		}
	}
 
	 

	/**
	 * При натискане на бутон - "Потвърждение" - модалния за избор от дървото
	 */
	public void actionConfirm() {
		if (getCode()!=null) {
			ValueExpression expr2 = getValueExpression("searchWord");
			ELContext ctx2 = getFacesContext().getELContext();
			if (expr2 != null) {
				expr2.setValue(ctx2, getSearchWord());
			}	
		 
			ValueExpression exprForCode = getValueExpression("selectedCode");
			if (exprForCode != null) {
				exprForCode.setValue(ctx2, getCode()); // по този начин знам, че е ново значение...
			}
			
			// извиква remoteCommnad - ако има такава....
			String remoteCommnad = (String) getAttributes().get("onComplete");
			if (remoteCommnad != null && !remoteCommnad.equals("")) {
				PrimeFaces.current().executeScript(remoteCommnad);
			}
		}
	}
	
  

	        
	/**
	 * Търсене на класификация по текст
	 *
	 * @throws DbErrorException
	 * @throws InvalidParameterException 
	 */
	public void search() throws DbErrorException, InvalidParameterException {
		 
		 
		String typeSearch = (String) getAttributes().get("typeSearch");
		boolean goSearch=false;
		 
		if (typeSearch.equals("EGN")) {
			if (getSearchWord() != null && getSearchWord().length() > 0) {
				
				if (ValidationUtils.invokeValidation(getSystemData().getModel().getAttrSpec("fzl_egn", "person", getLang(), null).getValidMethod(), getSearchWord())
						|| ValidationUtils.invokeValidation(getSystemData().getModel().getAttrSpec("fzl_lnc", "person", getLang(), null).getValidMethod(), getSearchWord())) {
					goSearch=true;
				}else {
					JSFUtils.addErrorMessage("Въведеното ЕГН/ЛНЧ е невалидно!");
				}
			}
		}else {
			if (typeSearch.equals("EIK")) {
				if (getSearchWord() != null && getSearchWord().length() > 0) {
					if (ValidationUtils.invokeValidation(getSystemData().getModel().getAttrSpec("nfl_eik", "person", getLang(), null).getValidMethod(), getSearchWord())) {
						goSearch=true;
					}else {
						JSFUtils.addErrorMessage("Въведеното ЕИК е невалидно!");
					}
				}
			}
		}
		
		
		if (goSearch) {
		
			 
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Searching for classif with: {}", getSearchWord());
				}
	
				try {
					List<SystemClassif> classifList = getSystemData().getSysClassification(DocuConstants.CODE_CLASSIF_REFERENTS, getDateClassif(),  getUserData().getCurrentLang()); 
					
					boolean found=false;
					
					if (getSearchWord() != null && getSearchWord().length() > 0) {
						for (int i = 0; i < classifList.size(); i++) {
							if (classifList.get(i).getTekst().toLowerCase().contains(getSearchWord().toLowerCase())) {
								setSearchWord(classifList.get(i).getTekst());
								setCode(classifList.get(i).getCode());
								found=true;
								break;
							}
						}
					}
					if (found) {
						actionConfirm();	
					}else {
//						Referent tmpRef=new Referent();
//						//REGIX and open modalCoresp
//						if (typeSearch.equals("EGN")) {
//							
//							tmpRef.setRefType(DocuConstants.CODE_ZNACHENIE_REF_TYPE_FZL);
//							RegixUtils.loadFizLiceByEgn(tmpRef, getSearchWord(), true, true, systemData);	
//						}else {
//							if (typeSearch.equals("EIK")) {
//								RegixUtils.loadUridLiceByEik(tmpRef, getSearchWord(), systemData);
//							}
//						}
//						if (tmpRef.getRefName()!=null) {
							ValueExpression expr2 = getValueExpression("searchWord");
							ELContext ctx2 = getFacesContext().getELContext();
							if (expr2 != null) {
								expr2.setValue(ctx2, getSearchWord());
							}
							
							String remoteCompCoresp = (String) getAttributes().get("corespCompCommand");
							if (remoteCompCoresp != null && !remoteCompCoresp.equals("")) {
								PrimeFaces.current().executeScript(remoteCompCoresp);
							}	
//						}else {
//							JSFUtils.addErrorMessage("Не са намерени данни за това лице!");
//						}
						
					}
					
					
					
				} catch (Exception e) {
					throw new DbErrorException(e);
				}
			} 
	}

	
	
	/** @return */
	private Integer getCodeClassif() {
		return  (Integer)getAttributes().get("codeClassif");
	}
	



	/** @return */
	public Date getCurrentDate() {
		return getDateClassif();
	}


	/** @return */
	public Integer getLang() {
		return getUserData().getCurrentLang();
	}


	/** @return */
	public String getSearchWord() {
		return (String) getStateHelper().eval(PropertyKeys.SEARCHWORD, "");
	}


	/** @return */

	/** @return */
	public Integer getCode() {
		Integer eval = (Integer) getStateHelper().eval(PropertyKeys.CODE, null);
		return eval;
	}

	/** @return */
	public boolean isShowMe() {
		return (Boolean) getStateHelper().eval(PropertyKeys.SHOWME, false);
	}
	


	/** @param searchWord */
	public void setSearchWord(String searchWord) {
		getStateHelper().put(PropertyKeys.SEARCHWORD, searchWord.trim());
	}


	/** @param showMe */
	public void setShowMe(boolean showMe) {
		getStateHelper().put(PropertyKeys.SHOWME, showMe);
	}


	/** @param tempCodes */
	public void setCode(Integer tempCodes) {
		getStateHelper().put(PropertyKeys.CODE, tempCodes);
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

	/** @return the systemData */
	private SystemData getSystemData() {
		if (this.systemData == null) {
			this.systemData = (SystemData) JSFUtils.getManagedBean("systemData");
		}
		return this.systemData;
	}

	/** @return the userData */
	public UserData getUserData() {
		if (this.userData == null) {
			this.userData = (UserData) JSFUtils.getManagedBean("userData");
		}
		return this.userData;
	}

	
}