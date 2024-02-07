package com.ib.docu.components;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.faces.application.FacesMessage;
import javax.faces.component.FacesComponent;
import javax.faces.component.UINamingContainer;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.primefaces.PrimeFaces;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.ToggleSelectEvent;
import org.primefaces.event.UnselectEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.docu.db.dao.DeloDAO;
import com.ib.docu.db.dao.DeloDocDAO;
import com.ib.docu.db.dao.TaskDAO;
import com.ib.docu.db.dto.Delo;
import com.ib.docu.db.dto.DeloAccess;
import com.ib.docu.db.dto.Task;
import com.ib.docu.system.SystemData;
import com.ib.docu.system.UserData;
import com.ib.indexui.pagination.LazyDataModelSQL2Array;
import com.ib.indexui.system.Constants;
import com.ib.indexui.system.IndexUIbean;
import com.ib.indexui.utils.JSFUtils;
import com.ib.system.db.JPA;
import com.ib.system.db.SelectMetadata;
import com.ib.system.exceptions.BaseException;
import com.ib.system.exceptions.DbErrorException;


/** Достъп до преписки през задача. Само, ако потребителия има дефинитивно право: "Право да дава достъп до преписка през задача" */

@FacesComponent(value = "compTaskAccessDelo", createTag = true)
public class CompTaskAccessDelo extends UINamingContainer  {
	
	
	private enum PropertyKeys {
		 PREPLIST,  DELOSEL, DELOSELTMP, LICALIST,  LICASEL
	}


	private static final Logger LOGGER = LoggerFactory.getLogger(CompTaskAccessDelo.class);
	public static final String	UI_BEANMESSAGES	= "ui_beanMessages";
	public static final String	BEANMESSAGES	= "beanMessages";
	public static final String  ERRDATABASEMSG 	= "general.errDataBaseMsg";
	public static final String  SUCCESSAVEMSG 	= "general.succesSaveMsg";
	

	private TimeZone timeZone = TimeZone.getDefault();

	private SystemData	systemData	= null;
	private UserData	userData	= null;

	
	private Integer[] licaSelected;

	private Task tmpTask;	

	public void initAccessDelo() {
		setPrepList(null);
		setLicaList(null);
		setDeloSelectedTmp(null);
		setLicaSelected(null);
		setDeloSelectedAllM(null);
	}
	
   /**
    * @throws DbErrorException 
	* 
	*/
	public void actionAccessDelo() throws DbErrorException {
		Integer idDoc = (Integer) getAttributes().get("idDoc");  
		Integer idTask = (Integer) getAttributes().get("idTask");
		
		if(idDoc != null) {
			//1. зареждам списъка с преписки към документа
			DeloDocDAO	deloDocDao = new DeloDocDAO(getUserData());			
			SelectMetadata sm = deloDocDao.createSelectDeloListByDoc(idDoc); // списък преписки, в които е вложен документа
			setPrepList( new LazyDataModelSQL2Array(sm, "a0"));	
			
			//1. зареждам  списък с лица: възложител, контролиращ, изпълнители от задачата
			Integer codeAssign = (Integer) getAttributes().get("codeAssign");  
			Integer codeCtrl = (Integer) getAttributes().get("codeCtrl");  
			@SuppressWarnings("unchecked")
			List<Integer>  listExec = (List<Integer>) getAttributes().get("listExec");  
			tmpTask = new Task();
			try {
				boolean ok = true;
				if(codeAssign != null  && listExec != null) {
					tmpTask.setCodeAssign(codeAssign);
					tmpTask.setCodeControl(codeCtrl); // не е задължителен
					tmpTask.setCodeExecs(listExec);
				}else if(idTask != null) {
					// зареждам задачата по ид
					JPA.getUtil().runWithClose(() -> {
						tmpTask = new TaskDAO(getUserData()).findById(idTask);
						}
					);
				}else {
					ok = false;
				}
				if(ok) {
					List<Integer> tmpList = new ArrayList<>();
					tmpList.add(tmpTask.getCodeAssign());
					if(tmpTask.getCodeControl() != null && !tmpList.contains(tmpTask.getCodeControl())) {
						tmpList.add(tmpTask.getCodeControl());
					}			
					for (Integer i: tmpTask.getCodeExecs()) {
						if(!tmpList.contains(i)) {
							tmpList.add(i);
						}
					}
					
					List<SelectItem> items = new ArrayList<>(tmpList.size());
					for (Integer x : tmpList) {
						String tekst = getSystemData().decodeItem(Constants.CODE_CLASSIF_ADMIN_STR, x, getUserData().getCurrentLang(), new Date());		
						items.add(new SelectItem(x, tekst));				
					}	
					setLicaList(items);
				
					String dialogWidgetVar = "PF('accessDeloVar').show();";
					PrimeFaces.current().executeScript(dialogWidgetVar);
				} else {
					LOGGER.error("Подадени са невалидни параметри при зареждане на списъка с преписки към документа  - задача,  изричен достъп до преписки! ");
				}
			} catch (BaseException e) {
				LOGGER.error("Грешка при зареждане на списъка с преписки към документа  - задача,  изричен достъп до преписки! ", e);
			}
		}
	}

	/*
	 * Множествен избор на дело/преписка
	 */
		 
	/**
	 * маркиране на всички редове от текуща страница
	 * @param tmpL
	 * @param selTmp
	 */
	private void  rowSelectAll(List<Object[]> tmpL, List<Object[]> selTmp) {
		for (Object[] obj : selTmp) {
			if(obj != null && obj.length > 0) {
				boolean bb = true;
				Long l2 = Long.valueOf(obj[0].toString());
				for (Object[] j : tmpL) { 
	    			Long l1 = Long.valueOf(j[0].toString());        			
		    		if(l1.equals(l2)) {    	    			
		    			bb = false;
		    			break;
		    		}
	    		}
				if(bb) {
					tmpL.add(obj);
				}
			}
		} 
	}
		
	/**
	 * Размракиране на всички редове от текуща страница
	 * @param tmpL
	 * @param listRez
	 */
	private void  rowUnselectAll(List<Object[]> tmpL, List<Object[]> tmpLPageC) {
		
		for (Object[] obj : tmpLPageC) {
			if(obj != null && obj.length > 0) {
				Long l2 = Long.valueOf(obj[0].toString());
				for (Object[] j : tmpL) { 
	    			Long l1 = Long.valueOf(j[0].toString());        			
		    		if(l1.equals(l2)) {    	    			
		    			tmpL.remove(j);
		    			break;
		    		}	
	    		}
			}
		}    	
	}
		
    

    /**
     * избор на ред от списъка
     * @param tmpList
     * @param obj
     * @return
     */
    private boolean rowSelect(List<Object[]> tmpList, Object[] obj  ) {
		boolean bb = true;	
		Integer l2 = Integer.valueOf(obj[0].toString());
		for (Object[] j : tmpList) { 
			Integer l1 = Integer.valueOf(j[0].toString());        			
    		if(l1.equals(l2)) {
    			bb = false;
    			break;
    		}
   		}
		return bb;
    }
   
    /**
     * размаркиране  на ред от списъка
     * @param tmpList
     * @param obj
     * @return
     */
    private boolean rowUnselect(List<Object[]> tmpList, Object[] obj  ) {
		boolean bb = false;		
		Integer l2 = Integer.valueOf(obj[0].toString());
		for (Object[] j : tmpList) {
			Integer l1 = Integer.valueOf(j[0].toString());
    		if(l1.equals(l2)) {
    			tmpList.remove(j);
    			bb = true;
    			break;
    		}
		}
		return bb;
    }
    
    /**
	 * преписки
	 * Избира всички редове от текущата страница 
	 * @param event
	 */
	 public void onRowSelectAllDelo(ToggleSelectEvent event) {    
    	List<Object[]> tmpL = new ArrayList<>();    	
    	tmpL.addAll(getDeloSelectedAllM());    	    	
    	if(event.isSelected()) {
    		rowSelectAll(tmpL, getDeloSelectedTmp());   		
    	}else {
    	   // rows from current page....   	
    		rowUnselectAll(tmpL, getPrepList().getResult()); 			
		}
		setDeloSelectedAllM(tmpL);	    	
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("onToggleSelect->>");
		}
	}
	 

	  /** 
     * избор на ред от списъка - преписки 
     * @param event
     */
   	public void onRowSelectDelo(SelectEvent<?> event) {    	
    	if(event!=null  && event.getObject()!=null) {
    		List<Object[]> tmpList =  getDeloSelectedAllM();
    		Object[] obj = (Object[]) event.getObject();			
			if(rowSelect(tmpList, obj)) {
				tmpList.add(obj);
				setDeloSelectedAllM(tmpList);   
			}
    	}	
    	if (LOGGER.isDebugEnabled()) {
    		LOGGER.debug("1 onRowSelectDelo->>{}",getDeloSelectedAllM().size());
    	}
    }
	

    /**
     * unselect one row - преписки
     * @param event
     */
	public void onRowUnselectDelo(UnselectEvent<?>  event) {
    	if(event!=null  && event.getObject()!=null) {
    		Object[] obj = (Object[]) event.getObject();
    		List<Object[] > tmpL = new ArrayList<>();    		
    		tmpL.addAll(getDeloSelectedAllM());
    
    		if(rowUnselect(tmpL, obj ) ) {
    			setDeloSelectedAllM(tmpL);
    		}
    		
    		if (LOGGER.isDebugEnabled()) {
    			LOGGER.debug( "onRowUnselectIil->>{}",getDeloSelectedAllM().size());
    		}
    	}
    }
    

		  
    /**
     * Преписки
     * За да се запази селектирането(визуалано на екрана) при преместване от една страница в друга
     */
    public void   onPageUpdateSelectedDelo(){
    	if (getDeloSelectedAllM() != null && !getDeloSelectedAllM().isEmpty()) {
    		getDeloSelectedTmp().clear();
    		getDeloSelectedTmp().addAll(getDeloSelectedAllM());
    	}	    	
    	if (LOGGER.isDebugEnabled()) {
    		LOGGER.debug( " onPageUpdateSelected->>{}", getDeloSelectedTmp().size());
    	}
    }
    

	private Delo deloTmp;

	/**
	 * Запис на изричен достъп до преписки
	 * 
	 */
	public void actionSaveAccess() {
		if(checkAccessDelo()) {
			try {
				boolean bb = false;
				DeloDAO	deloDao = new DeloDAO(getUserData());
			
					//2. записвам достъпа до преписките
					for(Object[] delo: getDeloSelectedAllM() ) {	
						JPA.getUtil().runWithClose(() -> 
							deloTmp = deloDao.taskAccessFindDelo(Integer.valueOf(delo[4].toString()))
						);
						bb = licaToDeloAccess(deloTmp);
						if(bb) {
							JPA.getUtil().runInTransaction(() -> 
							 	deloDao.taskAccessSaveDelo(deloTmp, getSystemData())
							); 
						}
					}
					
					JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, IndexUIbean.getMessageResourceString(BEANMESSAGES, "task.msgSuccessAccess"));
				
				setDeloSelectedAllM(null);
				setLicaSelected(null);
			} catch (BaseException e) {
				LOGGER.error("Грешка при запис на изричен достъп до преписки! ", e);
				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,IndexUIbean.getMessageResourceString(UI_BEANMESSAGES, ERRDATABASEMSG), e.getMessage());
			}
			String dialogWidgetVar = "PF('accessDeloVar').hide();";
			PrimeFaces.current().executeScript(dialogWidgetVar);
		}	
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug( " actionSaveAccess->{}", getDeloSelectedAllM().size());
		}  
	}
	
	/**
	 * запис на достъп до преписки
	 * проверка за валидни данни  
	 * @return
	 */
	private boolean checkAccessDelo() {
		boolean saveOk = true;
		FacesContext context = FacesContext.getCurrentInstance();
	    String clientId = null;	  
	    if (context != null ) { 
		    clientId =  this.getClientId(context);		    
			if(getDeloSelectedAllM() == null || getDeloSelectedAllM().isEmpty()) {
				saveOk = false;
				 JSFUtils.addMessage(clientId+":tblDeloList",FacesMessage.SEVERITY_ERROR,
							IndexUIbean.getMessageResourceString(BEANMESSAGES,"task.msgAccessDelo")); 
			}			
			if(getLicaSelected() == null || getLicaSelected().length==0) {
				saveOk = false;			
				 JSFUtils.addMessage(clientId+":lica",FacesMessage.SEVERITY_ERROR,
							IndexUIbean.getMessageResourceString(BEANMESSAGES,"task.msgAccessLica")); 
			}
	    }else {
	    	saveOk = false;
	    }
	    return saveOk;
	}
	
	/**
	 * добавя лицата, които имат достъп към конкретната преписка
	 * @param delo
	 * @return
	 */
	private boolean licaToDeloAccess(Delo delo) {
		boolean bb = false;
		if(delo != null) {
			DeloAccess da;	
			List<DeloAccess> licaDA = delo.getDeloAccess();		
			for (Integer idRef : getLicaSelected() ) {
				bb = true;
				for(DeloAccess da1: licaDA) {
					if(da1.getCodeRef().equals(idRef)) {						
						bb = false;
					}
				}
				if(bb) {
					da = new DeloAccess();
					da.setCodeRef(idRef);
					da.setDeloId(delo.getId());
					licaDA.add(da);					
				}
			}		
		}
		return bb;
	}
	
	
	private SystemData getSystemData() {
		if (this.systemData == null) {
			this.systemData =  (SystemData) JSFUtils.getManagedBean("systemData");
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



	public TimeZone getTimeZone() {
		return timeZone;
	}


	public void setTimeZone(TimeZone timeZone) {
		this.timeZone = timeZone;
	}

	
	public Date getCurrentDate() {
		return new Date();
	}



	public LazyDataModelSQL2Array getPrepList() {
		return (LazyDataModelSQL2Array) getStateHelper().eval(PropertyKeys.PREPLIST, null);
	}


	public void setPrepList(LazyDataModelSQL2Array prepList) {
		getStateHelper().put(PropertyKeys.PREPLIST, prepList);
	}

	/** @return */
	@SuppressWarnings("unchecked")
	public List<SelectItem> getLicaList() {
		List<SelectItem> eval = (List<SelectItem>) getStateHelper().eval(PropertyKeys.LICALIST, null);
		return eval != null ? eval : new ArrayList<>();
	}

	/** * @param statusList */
	public void setLicaList(List<SelectItem> licaList) {
		getStateHelper().put(PropertyKeys.LICALIST, licaList);
	}



	@SuppressWarnings("unchecked")
	public List<Object[]> getDeloSelectedAllM() {
		List<Object[]> eval = (List<Object[]>) getStateHelper().eval(PropertyKeys.DELOSEL, null);
		return eval != null ? eval : new ArrayList<>();
	}


	public void setDeloSelectedAllM(List<Object[]> deloSelectedAllM) {
		getStateHelper().put(PropertyKeys.DELOSEL, deloSelectedAllM);
	}

	@SuppressWarnings("unchecked")
	public List<Object[]> getDeloSelectedTmp() {
		List<Object[]> eval = (List<Object[]>) getStateHelper().eval(PropertyKeys.DELOSELTMP, null);
		return eval != null ? eval : new ArrayList<>();		
	}


	public void setDeloSelectedTmp(List<Object[]> deloSelectedTmp) {
		getStateHelper().put(PropertyKeys.DELOSELTMP, deloSelectedTmp);
	}

	
	public Integer[] getLicaSelected() {
		return licaSelected;
	
	}

	public void setLicaSelected(Integer[] licaSelected) {
		this.licaSelected = licaSelected;
	}

	/** @return */
	public Integer getLang() {
		return getUserData().getCurrentLang();
	}
	
	
	
}