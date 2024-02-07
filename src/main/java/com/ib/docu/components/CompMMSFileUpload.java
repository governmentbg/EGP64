package com.ib.docu.components;

import java.io.Serializable;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.el.ELContext;
import javax.el.ValueExpression;
import javax.faces.application.FacesMessage;
import javax.faces.component.FacesComponent;
import javax.faces.component.UINamingContainer;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.mail.internet.MimeUtility;
import javax.servlet.http.HttpServletRequest;

import com.ib.docu.system.DocuConstants;
import com.ib.indexui.system.Constants;
import com.ib.indexui.utils.JSFUtils;
import com.ib.system.BaseSystemData;
import com.ib.system.BaseUserData;
import com.ib.system.SysConstants;
import com.ib.system.db.JPA;
import com.ib.system.db.dao.FilesDAO;
import com.ib.system.db.dto.Files;
import com.ib.system.db.dto.SystemClassif;
import com.ib.system.exceptions.BaseException;
import com.ib.system.exceptions.DbErrorException;
import com.ib.system.utils.DateUtils;
import com.ib.system.utils.SearchUtils;
import com.ib.system.utils.X;

import org.primefaces.PrimeFaces;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@FacesComponent(value="compMMSFileUpload", createTag = true)
public class CompMMSFileUpload<T> extends UINamingContainer implements Serializable{
	
	/**
	 *
	 */
	private static final long serialVersionUID = -4857812625836666643L;

	private static final Logger LOGGER = LoggerFactory.getLogger(CompMMSFileUpload.class);
	
	
	private enum PropertyKeys {
		LSTOBJTMP,
		LISTDELOBJ,
		EDITINDEX,
		EDITNAME,
		TYPELIST,
		FILESELECT
	} 
	
	
	private Files fileSelect;
	

	public  static final  String AUTOSAVE 		 = "autoSave";
	public  static final  String LISTOBJ  		 = "listObj";
	public  static final  String EXTERNALMODE    = "externalMode";
	public  static final  String MOZILLA  		 = "Mozilla";
	public  static final  String APPXDOWNLOAD	 = "application/x-download";
	public  static final  String CONTDISPOSITION = "Content-Disposition";
	public  static final  String BEANMESSAGES	 = "beanMessages";
	public  static final  String UIBEANMESSAGES =  "ui_beanMessages";
	
	public void initRenderComp() {

			try {
				
				setTypeList(getSystemData().getSysClassification(DocuConstants.CODE_CLASSIF_VID_DOC_VPISVANE , new Date(), getLang()));
					
				setFileSelect(new Files());
			} catch (DbErrorException e) {
				LOGGER.error("Грешка при зареждане на видове документи в прикачен файл! ", e);
			}

	}

	
	@SuppressWarnings("unchecked")
	protected void returnValues(List <T> tmpArr, Object hc, String list ){
		if(tmpArr == null){
			tmpArr = new ArrayList<>();
		}
		
		if(getEditIndex() == null){
			tmpArr.add((T) hc);			
		}
		
		setLstObjTmp(tmpArr);	
		ValueExpression expr = getValueExpression(list);
	    ELContext ctx = getFacesContext().getELContext();
	    if(expr != null){
	    	expr.setValue(ctx, tmpArr);
	    }	
	    
	    executeRemoteCmd();
	    initComp();	 
	
	}
	
	/**
	 *  извиква remoteCommnad - ако има такава....
	 */
	private void executeRemoteCmd() {
		String remoteCommnad = (String) getAttributes().get("onComplete");
		if (remoteCommnad != null && !remoteCommnad.equals("")) {
			PrimeFaces.current().executeScript(remoteCommnad);
		}
	}
	
	
	
	public void initComp(){
		setLstObjTmp(null);
		setLstDelObjTmp(null);
	}
	
	/**
	 * Прикачване на файл
	 * @param event
	 */
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
		
		Boolean viewOfficial = (Boolean) getAttributes().get("viewOfficial");
		if(Boolean.TRUE.equals(viewOfficial)) {
			files.setOfficial(SysConstants.CODE_ZNACHENIE_DA);
		}
	
		try {
								
			Boolean autoSave = (Boolean) getAttributes().get(AUTOSAVE);	
			if (Boolean.TRUE.equals(autoSave)) {
				autoSave(files, x);
			} else {
				x.set(files);
			}

			if (x.isPresent()) {
				returnValues (getLstObjTmp(), x.get(), LISTOBJ);	
			}
		
		} catch (BaseException e) {
			LOGGER.error(e.getMessage(), e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,  "Грешка при прикачване на файл!", e.getMessage());
		}
	}
    
	
	/**
	 * Прикачване на файл  - Запис на новоприкачения файл, ако се изисква
	 * @param files
	 * @throws BaseException 
	 */
	private void autoSave(Files files, X<Files> x) throws BaseException {
		
		FilesDAO dao = new FilesDAO(getUserData());

		Integer idObj = (Integer) getAttributes().get("idObj");
		Integer codeObj = (Integer) getAttributes().get("codeObj");
		Boolean externalMode = (Boolean) getAttributes().get(EXTERNALMODE);
		if (Boolean.TRUE.equals(externalMode)) {
			x.set(dao.saveFileObjectRest(files, idObj, codeObj));			
		} else {
			JPA.getUtil().runInTransaction(() -> x.set(dao.saveFileObject(files , idObj, codeObj)));
		}
		
		JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, getMessageResourceString(UIBEANMESSAGES, "general.succesSaveFileMsg") );
					
	}
	
    public void clear(){
    	setLstObjTmp(null);
    }
		
    
	/**
	 * Download selected file
	 *
	 * @param files
	 */
	public void download(Files files) {
		try {
			if (files.getId() != null){
				
				Boolean externalMode = (Boolean) getAttributes().get(EXTERNALMODE);
			//	BaseUserData userData = (BaseUserData) JSFUtils.getManagedBean("userData");
				FilesDAO dao = new FilesDAO(getUserData());	
				
				if (Boolean.TRUE.equals(externalMode)) {
					files = dao.findByIdRest(files.getId());
				} else {
					try {
						files = dao.findById(files.getId());	
					} finally {
						JPA.getUtil().closeConnection();
					}
				}
				if(files.getContent() == null){					
					files.setContent(new byte[0]);
				}
			}
			
//			if (files.getPath() != null && !files.getPath().isEmpty()) { //  file system ????
//				Path path = Paths.get(files.getPath());
//				files.setContent(java.nio.file.Files.readAllBytes(path));
//			}

			FacesContext facesContext = FacesContext.getCurrentInstance();
			ExternalContext externalContext = facesContext.getExternalContext();

			HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();
			String agent = request.getHeader("user-agent");

			String codedfilename = "";

			if (null != agent && (-1 != agent.indexOf("MSIE") || -1 != agent.indexOf(MOZILLA) && -1 != agent.indexOf("rv:11") || -1 != agent.indexOf("Edge"))) {
				codedfilename = URLEncoder.encode(files.getFilename(), "UTF8");
			} else if (null != agent && -1 != agent.indexOf(MOZILLA)) {
				codedfilename = MimeUtility.encodeText(files.getFilename(), "UTF8", "B");
			} else {
				codedfilename = URLEncoder.encode(files.getFilename(), "UTF8");
			}

			externalContext.setResponseHeader("Content-Type", APPXDOWNLOAD);
			externalContext.setResponseHeader("Content-Length", files.getContent().length + "");
			externalContext.setResponseHeader(CONTDISPOSITION, "attachment;filename=\"" + codedfilename + "\"");
			externalContext.getResponseOutputStream().write(files.getContent());

			facesContext.responseComplete();

		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
	}
	
    protected String getMessageResourceString(String bundleName,String key) {
		FacesContext context = FacesContext.getCurrentInstance();
		String text = null;
		ResourceBundle bundle = context.getApplication().getResourceBundle(context, bundleName);
		try {
			text = bundle.getString(key);
		} catch (MissingResourceException e) {
			text = key;
		}
		return text;
	}
 

    public void actionDelete(Object key) {
		List<T> tmpArr = this.getLstObjTmp();
		if(key != null && tmpArr.contains(key)){
			tmpArr.remove(key);
			setLstObjTmp(tmpArr);
			
			ValueExpression expr = getValueExpression(LISTOBJ);
		    ELContext ctx = getFacesContext().getELContext();
		    if(expr != null){
		    	expr.setValue(ctx, tmpArr);
		    }

			if(((Files) key).getId() != null){
		    	if(getAttributes().get(AUTOSAVE) != null && (Boolean)getAttributes().get(AUTOSAVE)){
		    		removeFile((Files)key);
		    		executeRemoteCmd();
		    	} else if(this.getLstDelObjTmp() != null) {
			    	returnValues(this.getLstDelObjTmp(),key,"listDelObj");		
		    	}
		    }
			initComp();
		}
	}

    private void removeFile(Files file){
    	try {
			Boolean externalMode = (Boolean) getAttributes().get(EXTERNALMODE);
			//BaseUserData userData = (BaseUserData) JSFUtils.getManagedBean("userData");
			FilesDAO dao = new FilesDAO(getUserData());	
			
			Integer idObj = (Integer) getAttributes().get("idObj");
			Integer codeObj = (Integer) getAttributes().get("codeObj");
			file.setParrentID(idObj);
			file.setParentObjCode(codeObj);
			if (Boolean.TRUE.equals(externalMode)) {
				dao.deleteFileObjectRest(file);
			} else {
				JPA.getUtil().runInTransaction(() -> dao.deleteFileObject(file));
			}
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, getMessageResourceString(UIBEANMESSAGES, "general.succesDeleteFileMsg") );
			
		} catch (BaseException e) {
			LOGGER.error(e.getMessage(), e);
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
		
	public String titleFile(Files file) {
		StringBuilder title = new StringBuilder();		
		title.append("Име: "+file.getFilename());
		if(Objects.equals(file.getOfficial(),SysConstants.CODE_ZNACHENIE_DA)) {
			title.append(" <p/> За изпращане: ДА ");
		}
		if(Objects.equals(file.getSigned(),SysConstants.CODE_ZNACHENIE_DA)) {
			title.append(" <p/> Ел. подписан: ДА ");
		}
		if(Objects.equals(file.getPersonalData(),SysConstants.CODE_ZNACHENIE_DA)) {
			title.append(" <p/> Лични данни: ДА ");
		}
		if(file.getFilePurpose()!=null) {
			try {
				title.append(" <p/> Предназначение: ");
				title.append(getSystemData().decodeItem(115, file.getFilePurpose(), getLang(), new Date()).trim());
			} catch (DbErrorException e) {
				LOGGER.error("Грешка при зареждане на подробна инфорамция за файл към документ! ", e);
			}
		}
		Boolean showUserUpload = (Boolean) getAttributes().get("showUserUpload");
		if(Boolean.TRUE.equals(showUserUpload) && file.getUploadUser() != null && file.getUploadDate() != null) {
			
			try {
				title.append(" <p/> Прикачил: ");
				title.append(getSystemData().decodeItem(Constants.CODE_CLASSIF_ADMIN_STR, file.getUploadUser(), getLang(), new Date()).trim());
				title.append(" <p/> Дата: ");
				title.append(DateUtils.printDate(file.getUploadDate()));
				
			} catch (DbErrorException e) {
				LOGGER.error("Грешка при зареждане на подробна инфорамция за файл към документ! ", e);
			}
		}
		if(Objects.equals(Constants.CODE_ZNACHENIE_DA, file.getFileCompare())) {
			title.append(" <p/> Маркиран за сравняване ");
		}
		
		return title.toString();
	}
	
	
	
	/**
	 * при маркиране като официален, тип на файла, предназначение....
	 * @param file
	 */
    private void updateFile(Files file ,Boolean onlyFiles){
    	try {
			//Boolean externalMode = (Boolean) getAttributes().get(EXTERNALMODE);
    		Integer idObj = (Integer) getAttributes().get("idObj");
    		Integer codeObj = (Integer) getAttributes().get("codeObj");
    		
    		file.setParrentID(idObj);
    		file.setParentObjCode(codeObj);
			FilesDAO dao = new FilesDAO(getUserData());	
			JPA.getUtil().runInTransaction(() -> dao.updateFileObject(file ,onlyFiles));
		
		} catch (BaseException e) {
			LOGGER.error( e.getMessage(), e);
		}
    }
	 
    public Date getToday(){
		return new Date();
	}
    
    public int getCodeDa() {
    	return SysConstants.CODE_ZNACHENIE_DA;
    }
    
    
//	@SuppressWarnings("unchecked")
	public List<SelectItem> fileTypes(){		
		return new ArrayList<>();
	}
	
	
	@SuppressWarnings("unchecked")
	public List<T> getLstObjTmp() {	
		return (List<T>) getStateHelper().eval(PropertyKeys.LSTOBJTMP, getAttributes().get(LISTOBJ));		
	}
	
	public void setLstObjTmp(List<T> lstObjTmp) {
		getStateHelper().put(PropertyKeys.LSTOBJTMP, lstObjTmp);
	}
	
	@SuppressWarnings("unchecked")
	public List<T> getLstDelObjTmp() {	
		return (List<T>) getStateHelper().eval(PropertyKeys.LISTDELOBJ, getAttributes().get("listDelObj"));		
	}
	
	public void setLstDelObjTmp(List<T> lstObjTmp) {
		getStateHelper().put(PropertyKeys.LISTDELOBJ, lstObjTmp);
	}

	
	public void setEditIndex(Integer editIndex){
		getStateHelper().put(PropertyKeys.EDITINDEX, editIndex);
	}
	
	public Integer getEditIndex(){
		return (Integer) getStateHelper().eval(PropertyKeys.EDITINDEX,null);
	}
	
	public void setEditName(String editName){
		getStateHelper().put(PropertyKeys.EDITNAME, editName);
	}
	
	public String getEditName(){
		return (String) getStateHelper().eval(PropertyKeys.EDITNAME,null);
	}
	
	/** @return the systemData */
	private BaseSystemData	systemData	= null;
	private BaseSystemData getSystemData() {
		if (this.systemData == null) {
			this.systemData = (BaseSystemData) JSFUtils.getManagedBean("systemData");
		}
		return this.systemData;
	}
	
	/** @return the userData */
	private BaseUserData	userData	= null;
	private BaseUserData getUserData() {
		if (this.userData == null) {
			this.userData = (BaseUserData) JSFUtils.getManagedBean("userData");
		}
		return this.userData;
	}
	
	public Integer getLang() {
		return getUserData().getCurrentLang();
	}
	
	/** @return */
	@SuppressWarnings("unchecked")
	public List<SystemClassif> getTypeList() {
		List<SystemClassif> eval = (List<SystemClassif>) getStateHelper().eval(PropertyKeys.TYPELIST, null);
		return eval != null ? eval : new ArrayList<>();
	}

	/** * @param statusList */
	public void setTypeList(List<SystemClassif> typeList) {
		getStateHelper().put(PropertyKeys.TYPELIST, typeList);
	}
	
	public void setFileForDopInfo(Files file) {

		setFileSelect(file);
		
	}
	
	public void actionDopInfo() {
		fileSelect = getFileSelect();
		if(fileSelect!=null && fileSelect.getId()!=null) {
			updateFile(fileSelect ,null);
		}
	}
		
	public Files getFileSelect() {
		return  (Files) getStateHelper().eval(PropertyKeys.FILESELECT,null);
	}


	public void setFileSelect(Files fileSelect) {
		getStateHelper().put(PropertyKeys.FILESELECT, fileSelect);
		this.fileSelect = fileSelect;
	}
	
	/**
	 * Ако е подаден параметър "additionalAction" - да извика и изпълни remoteCommand
	 * Изпълнява функция (от бейна, от който се вика компонентата) при натискане на бутон за допълнително действие
	 */
	public void actionAdditional() {
		String remoteCommnad = (String) getAttributes().get("additionalAction");
		if (remoteCommnad != null && !remoteCommnad.equals("")) {
			PrimeFaces.current().executeScript(remoteCommnad);
		}		
	}
	
	
	
	/**
	 * Избран е файл за преименуване
	 * @param file
	 */
	public void setFileForRename(Files file) {
		setFileSelect(file);
		setEditName(file.getFilename());
	}
	
	/**
	 * преименуване на файл
	 */
    public void changeToNewname(){
    	boolean ok = false;
    	if(!SearchUtils.isEmpty(getEditName())){
    		Matcher matcher = Pattern.compile("^([a-zA-Z0-9а-яА-Я\\s\\._-]+)$").matcher(getEditName()); // + Кирилица
    		//Matcher matcher = Pattern.compile("^([a-zA-Z0-9\\s\\._-]+)$").matcher(getEditName());
    		String extension = null;                    
    		int i = getEditName().lastIndexOf('.');
    		if (i > 0) { 
    		    extension = getEditName().substring(i+1);
    		}    		
	    	//Разширенията да съвпадат!
	    	String fileExtension = getFileSelect().getFilename().substring(getFileSelect().getFilename().lastIndexOf('.')+1);
    		if (matcher.matches() && extension != null && extension.equalsIgnoreCase(fileExtension)) {
	   			getFileSelect().setFilename(getEditName());
	    		if(getFileSelect().getId() != null) {
	    			updateFile(getFileSelect(), true);
	    		}
	    		ok = true;
	        } 
    	} 
    	if(ok) {
    		String  cmdStr = "PF('modalRename').hide();";
			PrimeFaces.current().executeScript(cmdStr);
    	}else {
    		JSFUtils.addMessage("editFileNameError",FacesMessage.SEVERITY_ERROR, getMessageResourceString(UIBEANMESSAGES,"files.invalidFileName"));
    	}
    }
	
}
