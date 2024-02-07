package com.ib.docu.beans;

import java.io.IOException;
import java.io.Serializable;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import javax.activation.DataSource;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.faces.view.facelets.FaceletContext;
import javax.inject.Named;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.util.ByteArrayDataSource;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.parsers.ParserConfigurationException;

import org.omnifaces.cdi.ViewScoped;
import org.primefaces.PrimeFaces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
import org.xml.sax.SAXException;

import com.ib.docu.db.dao.DocDAO;
import com.ib.docu.db.dao.EgovMessagesDAO;
import com.ib.docu.db.dao.LockObjectDAO;
import com.ib.docu.db.dao.MMSChlenstvoDAO;
import com.ib.docu.db.dao.MMSSportObedMFDAO;
import com.ib.docu.db.dao.MMSVpisvaneDAO;
import com.ib.docu.db.dao.MMSVpisvaneDocDAO;
import com.ib.docu.db.dao.MMSsportObedinenieDAO;
import com.ib.docu.db.dao.ReferentDAO;
import com.ib.docu.db.dto.Doc;
import com.ib.docu.db.dto.EgovMessages;
import com.ib.docu.db.dto.EgovMessagesCoresp;
import com.ib.docu.db.dto.EgovMessagesFiles;
import com.ib.docu.db.dto.MMSChlenstvo;
import com.ib.docu.db.dto.MMSSportObedMf;
import com.ib.docu.db.dto.MMSSportnoObedinenie;
import com.ib.docu.db.dto.MMSVidSportSO;
import com.ib.docu.db.dto.MMSVpisvane;
import com.ib.docu.db.dto.MMSVpisvaneDoc;
import com.ib.docu.db.dto.Referent;
import com.ib.docu.system.DocuClassifAdapter;
import com.ib.docu.system.DocuConstants;
import com.ib.docu.system.SystemData;
import com.ib.docu.system.UserData;
import com.ib.docu.utils.ParsePdfZaqvlenie;
import com.ib.indexui.navigation.NavigationDataHolder;
import com.ib.indexui.pagination.LazyDataModelSQL2Array;
import com.ib.indexui.system.Constants;
import com.ib.indexui.system.IndexUIbean;
import com.ib.indexui.utils.JSFUtils;
import com.ib.system.BaseSystemData;
import com.ib.system.db.JPA;
import com.ib.system.db.dao.FilesDAO;
import com.ib.system.db.dto.Files;
import com.ib.system.exceptions.BaseException;
import com.ib.system.exceptions.DbErrorException;
import com.ib.system.exceptions.InvalidParameterException;
import com.ib.system.exceptions.ObjectInUseException;
import com.ib.system.mail.Mailer;
import com.ib.system.mail.Mailer.Content;
import com.ib.system.utils.DateUtils;
import com.ib.system.utils.SearchUtils;
import com.ib.system.utils.ValidationUtils;

import bg.government.regixclient.RegixClientException;




/**
 * Въвеждане и актуализация на Преписка/Дело
 * 
 * @author s.marinov
 *
 */
@Named
@ViewScoped
public class SportObedEdit   extends IndexUIbean  implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2934740148410006322L;
	private static final Logger LOGGER = LoggerFactory.getLogger(SportObedEdit.class);
	public static final String  DELOFORM = "deloForm";
	public static final String  MSGPLSINS = "general.pleaseInsert";
	public static final String  ERRDATABASEMSG = "general.errDataBaseMsg";
	public static final String  SUCCESSAVEMSG = "general.succesSaveMsg";
	public static final String  deloMaxBrSheetsDefault = "delo.deloMaxBrSheetsDefault";
	
	
	private MMSSportnoObedinenie sportObed=new MMSSportnoObedinenie();
	
	
	private List<Integer> selectedVidSport = new ArrayList<>();
	private String selectedVidSportTxt;
	
	private Date decodeDate = new Date();
	private String  txtCorresp; 
	
	private LazyDataModelSQL2Array docsList;
	
	private MMSSportObedMf currentMf=new MMSSportObedMf();
	private boolean editingMf=false;
	
	private List<MMSSportObedMf> mfList=new ArrayList<MMSSportObedMf>();
	private List<Object[]> sportFormList=new ArrayList<Object[]>();
	
	private List<MMSVpisvane> regsList=new ArrayList<MMSVpisvane>(); 
	private BaseSystemData sd;
	
	private String dopInfoAdres;
	private Referent referent=new Referent();
	private String mailText;
	private String subject;
	private boolean willShowMailModal = true;
	private static Properties props=new Properties();
	private static Integer ID_REGISTRATURE = 1;//1;
	private static final String MAILBOX="DEFAULT";//"DEFAULT";
	private String vidDocVpisvText;
	private Date dateRNV;
	
	
	private EgovMessages egovMess;
	private EgovMessagesCoresp egovCoresp;
	private List<EgovMessagesFiles> egovFilesList;
	private List<SelectItem> msgStatusList = new ArrayList<>();
	private String reasonOtkaz;
	
	private UserData ud;
	
	private ArrayList<DataSource> attachedBytes = new ArrayList<DataSource>();
	private ArrayList<Files> uploadFilesList = new ArrayList<Files>();
	private Integer idFormir=null;
	
	/** */
	@PostConstruct
	void initData() {
		if (JSFUtils.getRequestParameter("idFormir")!=null) {
			NavigationDataHolder holder=(NavigationDataHolder) JSFUtils.getManagedBean("navigationSessionDataHolder");
			holder.getPageList().pop();
		}
		if (JSFUtils.getRequestParameter("idFormirovanie")!=null) {
			idFormir=Integer.valueOf(JSFUtils.getRequestParameter("idFormirovanie"));
		}
		
		
		LOGGER.debug("!!! PostConstruct ObedBean !!!");
		setUd(getUserData(UserData.class));
		try {
			props = getSystemData(SystemData.class).getMailProp(ID_REGISTRATURE, MAILBOX);
			boolean fLockOk=true;
			FaceletContext faceletContext = (FaceletContext) FacesContext.getCurrentInstance().getAttributes().get(FaceletContext.FACELET_CONTEXT_KEY);
			String param3 = (String) faceletContext.getAttribute("isView"); // 0 - актуализациял 1 - разглеждане
			Integer isView=0;
			if(!SearchUtils.isEmpty(param3)) {
				isView = Integer.valueOf(param3);
			}
			String param = JSFUtils.getRequestParameter("idObj");
			Integer obedId = null;
			if (param != null && !param.isEmpty()){
				obedId = Integer.valueOf(param);
			}
			 
			
	        if(isView == 0 && obedId!=null) { 
	            // проверка за заключен документ
	            fLockOk = checkForLock(obedId);
	            if (fLockOk) {
	                lockObed(obedId);
	            // отключване на всички обекти за потребителя(userId) и заключване на док., за да не може да се актуализира от друг
	            }                
	        }
	        
	        if (fLockOk) {
				
			
				JPA.getUtil().runWithClose(() -> {
					sd=getSystemData();
					getVidSportAStrings(sportObed.getVidSportList());
					loadSportObed();
					if (sportObed.getId()!=null) {
						mfList	=new MMSSportObedMFDAO(getUserData()).findByIdSportnoObed(sportObed.getId());	
						actionLoadFormirList();
						findVpisvane();
						findDocs();
					} else {
						
						if (JSFUtils.getRequestParameter("ccevID") != null  && !"".equals(JSFUtils.getRequestParameter("ccevID"))) {
							
							// Тези параметри ми трябват, за да мога да регистрирам документ в нашата система с техните данни
							this.idSSev = Integer.valueOf(JSFUtils.getRequestParameter("ccevID"));
							boolean lockEgov=true;
							if (idSSev!=null && checkForLock(idSSev)) {
								if (lockEgov) {
									lockZaiavl(idSSev);
								}
							}							
							
							actionLoadEgovMessage();
							
							
							this.vidDoc = Integer.valueOf(egovMess.getDocVid());	
							 
							this.regNom = egovMess.getDocRn();
							this.setDataDoc(egovMess.getDocDate());
							this.otnosno = egovMess.getDocSubject();
							if(egovCoresp!=null) {
								this.egn = egovCoresp.getEgn();
								this.eik = egovCoresp.getBulstat();	
							}
							
							
							if (getDataDoc()!=null) {
									setDateRNV(this.getDataDoc());
							}
							
							
							//OTIVA DA SE OPITA DA PARSNE PDF AKO IMA TAKAV.
							try {
								
								sportObed=new ParsePdfZaqvlenie().parseObedinenie((SystemData) getSystemData(), ud, getCurrentLang(), egovMess, egovFilesList);
								boolean lockObed=true;
								if (sportObed!=null && sportObed.getId()!=null) {
						            // проверка за заключен документ
									lockObed = checkForLock(sportObed.getId());
						            if (lockObed) {
						                lockObed(sportObed.getId());
						            // отключване на всички обекти за потребителя(userId) и заключване на док., за да не може да се актуализира от друг
						            }                
								}
								if (lockObed) {
									if (sportObed.getIdObject()==null) {
										this.referent.setContactEmail(sportObed.getMailLice());
									}else {
										this.referent=new ReferentDAO(getUserData()).findByCodeRef(sportObed.getIdObject());
									}
									if (sportObed.getParseMessages().size()>0) {
										for (int i = 0; i < sportObed.getParseMessages().size(); i++) {
											JSFUtils.addErrorMessage(sportObed.getParseMessages().get(i));
										}										
									}
									if (sportObed!=null) {
										getVidSportAStrings(sportObed.getVidSportList());										
									}
									
									if (sportObed.getId()!=null) {											
										mfList	=new MMSSportObedMFDAO(getUserData()).findByIdSportnoObed(sportObed.getId());

										actionLoadFormirList();
										findVpisvane();
										findDocs();	
									}
								}else {
									actionNew();
								}
							} catch (ParserConfigurationException e) {
								JSFUtils.addErrorMessage("Грешка при автоматична обработка на пдф!", e);
								LOGGER.error(e.getMessage(), e);
							} catch (DOMException e) {
								JSFUtils.addErrorMessage("Грешка при автоматична обработка на пдф!", e);
								LOGGER.error(e.getMessage(), e);
							} catch (SAXException e) {
								JSFUtils.addErrorMessage("Грешка при автоматична обработка на пдф!", e);
								LOGGER.error(e.getMessage(), e);
							} catch (IOException e) {
								JSFUtils.addErrorMessage("Грешка при автоматична обработка на пдф!", e);
								LOGGER.error(e.getMessage(), e);
							} catch (RegixClientException e) {
								JSFUtils.addErrorMessage("Грешка при автоматична обработка на пдф!", e);
								LOGGER.error(e.getMessage(), e);
							} catch (DatatypeConfigurationException e) {
								JSFUtils.addErrorMessage("Грешка при автоматична обработка на пдф!", e);
								LOGGER.error(e.getMessage(), e);
							} catch (ParseException e) {
								JSFUtils.addErrorMessage("Грешка при автоматична обработка на пдф!", e);
								LOGGER.error(e.getMessage(), e);
							}  
							 
						}		
						
//						if (sportObed.getId()==null) {
//							sportObed.setStatus(DocuConstants.CODE_ZNACHENIE_STATUS_OBEKT_V_RAZGLEJDANE);
//							sportObed.setDateStatus(new Date());	
//						}
//						
						
						
						
					}
					
				});
	        }
		} catch (BaseException e) {
			JSFUtils.addErrorMessage(getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e);
			LOGGER.error(e.getMessage(), e);
		}
		
	}
	
	public String actionGoBackFormir() {
		return "mmsSportFormirovanieEdit.xhtml?faces-redirect=true&idObj=" + idFormir+"&idObed=true";
	}
	 
	
	public void actionLoadEgovMessage() {
		if (this.idSSev!=null) {
			try {
				egovMess = new EgovMessagesDAO(getUserData()).findById(this.idSSev);
				egovFilesList = new EgovMessagesDAO(getUserData()).findFilesByMessage(this.idSSev);
				
				egovCoresp=new EgovMessagesDAO(getUserData()).findCorespByIdMessage(this.idSSev);
				
				ArrayList<Object[]> tmpList = new EgovMessagesDAO(getUserData()).createMsgTypesList();
				
				tmpList = new EgovMessagesDAO(getUserData()).createMsgStatusList();
			
				if(tmpList !=null && !tmpList.isEmpty()){
					for(Object[] item:tmpList) {
						if(item != null && item[0]!=null && item[1]!=null){
							msgStatusList.add(new SelectItem( item[0].toString(),item[1].toString()));
						}
					}
				}
			} catch (DbErrorException e) {
				LOGGER.error("Грешка при работа с базата!", e);
				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());	
			}catch (Exception e) {
				LOGGER.error("Грешка при зареждане данните!", e);
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
	
	public void actionOtkazReg() {
		if (this.reasonOtkaz==null || this.reasonOtkaz.isEmpty()) {
			 JSFUtils.addMessage("sportObedForm:otkazText", FacesMessage.SEVERITY_ERROR, 
                     getMessageResourceString(UI_beanMessages, "general.pleaseInsert", "Причина за отказ от регистрация!"));
			 return;
		}
		try {
            
            EgovMessages egovMess = new EgovMessagesDAO(getUserData()).findById(this.idSSev);
            JPA.getUtil().runInTransaction(() -> { 
                if (null!=egovMess) {

                    egovMess.setMsgStatus("DS_REJECTED");
                    egovMess.setMsgStatusDate(new Date());

                    egovMess.setCommError(reasonOtkaz);
                    
                    
                    new EgovMessagesDAO(getUserData()).save(egovMess);
                    JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, getMessageResourceString(UI_beanMessages, SUCCESSAVEMSG));
                    PrimeFaces.current().executeScript("PF('otkazReg').hide();");
                }
                
            });    
            
        
		} catch (DbErrorException e) {
            LOGGER.error("Грешка при смяна стауса на заявление при отказ!! ", e);
            JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString("ui_beanMessages", ERRDATABASEMSG), e.getMessage());
            JPA.getUtil().rollback();
        } catch (BaseException e) {
            LOGGER.error("Грешка при смяна стауса на заявление при отказ!!", e);
            JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString("ui_beanMessages", ERRDATABASEMSG), e.getMessage());
            JPA.getUtil().rollback();
        }finally {
            JPA.getUtil().closeConnection();
        }


		
	}
	
	public void comesFromDAEU(Integer egovID) {
		
		try {
			//EgovMessages tmpEgov = new EgovMessagesDAO(getUserData()).findById(egovID);
			
			EgovMessagesCoresp emcoresp = new EgovMessagesDAO(getUserData()).findCorespByIdMessage(egovID);
			if(emcoresp == null) {
				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, "Липсва ЕИК на лицето в заявлението! Моля, изберете ново заявление!");	
				//sendMail("Отговор на подадено заявление", "Липсва ЕИК на лицето в заявлението!");
				return;
			}
			if(emcoresp.getBulstat() != null && !emcoresp.getBulstat().trim().isEmpty()) {
				if( !ValidationUtils.isValidBULSTAT(emcoresp.getBulstat()) ) {
					// 3. 	Валидация на ЕИК. При установяване на грешка се прекратява обработката, като се изпраща известие по мейла на заявителя с подходящата нотификация
					JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, "Грешен ЕИК в заявлението! Моля, изберете ново заявление!");
					//sendMail("Отговор на подадено заявление", "Грешен ЕИК в заявлението! Трябва да изберете ново заявление!");
					return;
				}
				Referent referent = new ReferentDAO(getUserData()).findByIdent(emcoresp.getBulstat(), null, null, DocuConstants.CODE_ZNACHENIE_REF_TYPE_NFL);
				
				if(referent != null) {
					sportObed.setIdObject(Integer.valueOf(referent.getCode()));
					sportObed = new MMSsportObedinenieDAO(MMSSportnoObedinenie.class,getUserData()).findByIdObject(sportObed);
					if(sportObed == null || sportObed.getId() == null) {
						setTxtCorresp(referent.getIme());
					}else {
						if(sportObed != null && sportObed.getId() != null) {
							mfList	=new MMSSportObedMFDAO(getUserData()).findByIdSportnoObed(sportObed.getId());	
							actionLoadFormirList();
							findVpisvane();
							findDocs();
						}
					}
				}else {
					// da potarsi komponentata v regix i ako nameri si minava po normalniq red.
					PrimeFaces.current().executeScript("PF('mCorrD').show();");
				}
					
			} 
			 
		} catch (DbErrorException e) {
			LOGGER.error(e.getMessage(), e);
		} /*catch (AddressException e) {
			LOGGER.error(e.getMessage(), e);
		} catch (InvalidParameterException e) {
			LOGGER.error(e.getMessage(), e);
		} catch (MessagingException e) {
			LOGGER.error(e.getMessage(), e);
		} */finally {
			JPA.getUtil().closeConnection();
		}
		
		
		 
		
		// 4. Проверка в базата , ако се намери - > отказ от регистрация и мейл до заявителя.
		
	}
	
	public void actionSave() {
		 
		if (validate()) {
			boolean isNewObed=false;
			if (this.sportObed.getId()==null) {
				isNewObed=true;
			}
			try {
			
				JPA.getUtil().runInTransaction(() -> {
					
					sportObed=new MMSsportObedinenieDAO(MMSSportnoObedinenie.class, getUserData()).save(sportObed);
					JPA.getUtil().flush();
					if (idSSev!=null && sportObed.getMejdFedList().size()>0) {
						if(this.sportObed.getId()!=null) {
							new MMSSportObedMFDAO(getUserData()).deleteByIdSpObed(this.sportObed.getId());	
						}						
						
	                	// ako ima neshto tuk to e doshlo ot xml-a i go zapisvam ra4no.
	                	for (int i = 0; i < sportObed.getMejdFedList().size(); i++) {
	                		sportObed.getMejdFedList().get(i).setIdSportObed(this.sportObed.getId());	                		
	    					
	    					new MMSSportObedMFDAO(getUserData()).save(sportObed.getMejdFedList().get(i));
	    					
	    									
	    				}
	                	sportObed.getMejdFedList().clear();
	                	
	            		mfList	=new MMSSportObedMFDAO(getUserData()).findByIdSportnoObed(sportObed.getId());	    
	                	
					}
					 
				});
				
				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, getMessageResourceString(UI_beanMessages, SUCCESSAVEMSG));
				
				this.referent=new ReferentDAO(getUserData()).findById(sportObed.getId());
				
				//TODO - ако идва от СЕОС след първия запис се вика метода за запис на вписване, документ и промяна на статуса в EgovMessages
				if (this.idSSev != null) {
					if (this.sportObed.getId()==null) {
						actionSaveDocFromSeos();						
					}

					
					findVpisvane();
					findDocs();
				}
				
                if (isNewObed) {
                    lockObed(this.sportObed.getId());    
                }
                
                
                
				
			} catch (BaseException e) {			
				JSFUtils.addErrorMessage(getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e);
				LOGGER.error(e.getMessage(), e);
				JPA.getUtil().rollback();
			}finally {
				JPA.getUtil().closeConnection();
			}
		}
		
	}
	
	/**
	 * Заключване на дело, като преди това отключва всички обекти, заключени от потребителя
	 * @param idObj
	 */
	public void lockObed(Integer idObj) {	
		
		LockObjectDAO daoL = new LockObjectDAO();		
		try { 
			JPA.getUtil().runInTransaction(() -> 
				daoL.lock(getUd().getUserId(), DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORT_OBED, idObj, null)
			);
		} catch (BaseException e) {
			LOGGER.error("Грешка при заключване на документ! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
		}			
	}
	/**
	 * Заключване на дело, като преди това отключва всички обекти, заключени от потребителя
	 * @param idObj
	 */
	public void lockZaiavl(Integer idObj) {	
		
		LockObjectDAO daoL = new LockObjectDAO();		
		try { 
			JPA.getUtil().runInTransaction(() -> 
			daoL.lock(getUd().getUserId(), DocuConstants.CODE_ZNACHENIE_JOURNAL_EGOVMESSAGE, idObj, null)
					);
		} catch (BaseException e) {
			LOGGER.error("Грешка при заключване на документ! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
		}			
	}
	
	/**
	 * Проверка за заключен документ 
	 * @param idObj
	 * @return
	 */
	private boolean checkForLock(Integer idObj) {
		boolean res = true;
		LockObjectDAO daoL = new LockObjectDAO();		
		try { 
			Object[] obj = daoL.check(getUserData().getUserId(), DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORT_OBED, idObj);
			if (obj != null) {
				 res = false;
				 String msg = getSystemData().decodeItem(DocuConstants.CODE_CLASSIF_ADMIN_STR, Integer.valueOf(obj[0].toString()), getUserData().getCurrentLang(), new Date())   
						       + " / " + DateUtils.printDate((Date)obj[1]);
				 JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_WARN,getMessageResourceString(beanMessages, "obed.lockedObed"), msg);
			}
		} catch (DbErrorException e) {
			LOGGER.error("Грешка при проверка за заключена преписка! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
		}
		return res;
	}
	
	/**
	 * при излизане от страницата - отключва обекта и да го освобождава за актуализация от друг потребител
	 */
	@PreDestroy
	public void unlockObed(){
        if (!getUd().isReloadPage()) {
        	LockObjectDAO daoL = new LockObjectDAO();	
        	try { 
	        	
	        	JPA.getUtil().runInTransaction(() -> 
					daoL.unlock(getUserData().getUserId())
				);
        	} catch (BaseException e) {
    			LOGGER.error("Грешка при отключване на документ! ", e);
    			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
    		}
        	getUd().setPreviousPage(null);
        	
        }          
        getUd().setReloadPage(false);
	}
	 
	
	public void actionCheckExistEik() {
		try {
			
			JPA.getUtil().runWithClose(() -> {
				if (sportObed.getIdObject()!=null) {
					sportObed = new MMSsportObedinenieDAO(MMSSportnoObedinenie.class, getUserData()).findByIdObject(sportObed);
				}
				if (sportObed.getId()!=null) {
					getVidSportAStrings(sportObed.getVidSportList());
					mfList	=new MMSSportObedMFDAO(getUserData()).findByIdSportnoObed(sportObed.getId());	
					actionLoadFormirList();
					findVpisvane();
					findDocs();
				}
				actionCheckOneYear();
				
			});
		} catch (BaseException e) {
			JSFUtils.addErrorMessage(getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e);
			LOGGER.error(e.getMessage(), e);
		}
		
	}
	
	public boolean actionCheckOneYear() {
		GregorianCalendar gc=new GregorianCalendar();
		if (sportObed.getDateStatus()!=null) {
			gc.setTime(sportObed.getDateStatus());
			gc.add(Calendar.YEAR, 1);
		}
		Date datReg=new Date();
		if (egovMess!=null && egovMess.getDocDate()!=null) {
			datReg=egovMess.getDocDate();
		}
		if (sportObed.getVid()!=null && sportObed.getStatus()!=null && sportObed.getVid()==DocuConstants.CODE_ZNACHENIE_VID_SPORT_OBEDINENIE_SF
				&& sportObed.getStatus()==DocuConstants.CODE_ZNACHENIE_STATUS_OBEKT_OTKAZAN
				&& sportObed.getDateStatus()!=null
				&& gc.getTime().after(datReg)) {
			// ako e federaciq i e s otkazan licenz, gledame 1 godina dali e minala za da gi pusnem na vtoro vpisvane.
			
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_WARN, "Нямате право да вписвате тази федерация преди: "+new SimpleDateFormat("dd.MM.yyyy").format(gc.getTime()));
			return false;
		}
		return true;
	}
	
	public void actionNew() {
		selectedVidSport = null;
		setDocsList(null);
		setRegsList(new ArrayList<MMSVpisvane>());
		this.sportObed=new MMSSportnoObedinenie();
		this.referent=new Referent();
		txtCorresp=null;
		dopInfoAdres=null;
		selectedVidSportTxt=null;

		this.vidDoc = null;	
		 
		this.regNom = null;
		this.setDataDoc(null);
		this.otnosno = null;
		
		this.egn = null;
		this.eik = null;	
		this.idSSev=null;
		
	}
	
	public void actionDelete() {
		try {
			JPA.getUtil().runInTransaction(() -> {
				
				new MMSsportObedinenieDAO(MMSSportnoObedinenie.class, getUserData()).deleteFromRegister(sportObed.getId(),(SystemData) getSd());
				 
			});
			getSd().reloadClassif(DocuConstants.CODE_ZNACHENIE_JOURNAL_REFERENT, false, false);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO,  IndexUIbean.getMessageResourceString(UI_beanMessages, "general.successDeleteMsg") );
			actionNew();
		} catch (BaseException e) {
			JSFUtils.addErrorMessage(getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e);
			LOGGER.error(e.getMessage(), e);
			JPA.getUtil().rollback();
		}
	}
	
	public void loadSportObed() throws DbErrorException {
		String param = JSFUtils.getRequestParameter("idObj");
		Integer obedId = null;
		if (param != null && !param.isEmpty()){
			obedId = Integer.valueOf(param);
		}
		if (obedId!=null) {
			sportObed=	new MMSsportObedinenieDAO(MMSSportnoObedinenie.class, getUserData()).findById(obedId);
			if (sportObed==null) {
				sportObed=new MMSSportnoObedinenie();
			}
			getVidSportAStrings(sportObed.getVidSportList());
			this.referent=new ReferentDAO(getUserData()).findByCodeRef(sportObed.getIdObject());
		}
		actionCheckOneYear();
	}
	
	public boolean validate() {
		boolean save =true;
		
		
		try {
			if (getSystemData().getModel().getAttrSpec("nfl_eik", "sport_obedinenie", getCurrentLang(), null).isActive()) {
				if (getSystemData().getModel().getAttrSpec("nfl_eik", "sport_obedinenie", getCurrentLang(), null).isRequired()) {
					if (sportObed.getIdObject()==null) {
									save = false;
									JSFUtils.addMessage("mmsSFform:eik", FacesMessage.SEVERITY_ERROR, 
											getMessageResourceString(UI_beanMessages, "general.pleaseInsert", "юридическо лице"));
					}		
				}
				if (getTxtCorresp()!=null && !getTxtCorresp().isEmpty() && sportObed.getIdObject()==null) {
					if(!ValidationUtils.invokeValidation(getSystemData().getModel().getAttrSpec("nfl_eik", "sport_obedinenie", getCurrentLang(), null).getValidMethod(), getTxtCorresp())) {
						save = false;
						JSFUtils.addMessage("mmsSFform:eik", FacesMessage.SEVERITY_ERROR, 
								"Невалидно ЕИК!");
					}
				}
			}
			if (sportObed.getVid()!=null && sportObed.getVid()==DocuConstants.CODE_ZNACHENIE_VID_SPORT_OBEDINENIE_OK) {
				if (sportObed.getIdObject()!=null) {
					String name =getSystemData().decodeItem(DocuConstants.CODE_CLASSIF_REFERENTS, sportObed.getIdObject(), getCurrentLang(), new Date());
					if (name==null || !name.toLowerCase().contains("обединен спортен клуб")) {
						save = false;
						JSFUtils.addMessage("mmsSFform:eik", FacesMessage.SEVERITY_ERROR, 
								"Наименованието трябва да съдържа думите \"обединен спортен клуб\"!");
					}
				}
			}
			
			
			if (getSystemData().getModel().getAttrSpec("vid", "sport_obedinenie", getCurrentLang(), null).isActive() 
					&& getSystemData().getModel().getAttrSpec("vid", "sport_obedinenie", getCurrentLang(), null).isRequired()) {
				if(sportObed.getVid() == null) {
					save = false;
					JSFUtils.addMessage("mmsSFform:vid", FacesMessage.SEVERITY_ERROR, 
							getMessageResourceString(UI_beanMessages, "general.pleaseInsert", "Вид Обединение"));
				}		
			}
			
			if (getSystemData().getModel().getAttrSpec("status", "sport_obedinenie", getCurrentLang(), null).isActive() 
					&& getSystemData().getModel().getAttrSpec("status", "sport_obedinenie", getCurrentLang(), null).isRequired()) {
				if(sportObed.getStatus() == null) {
					save = false;
					JSFUtils.addMessage("mmsSFform:status", FacesMessage.SEVERITY_ERROR, 
							getMessageResourceString(UI_beanMessages, "general.pleaseInsert", "Статус"));
				}		
			}
			if (sportObed.getVid()!=null && sportObed.getVid()==DocuConstants.CODE_ZNACHENIE_VID_SPORT_OBEDINENIE_SF) {
				//ako e federaciq
				if (sportObed.getStatus()!=null && sportObed.getStatus()==DocuConstants.CODE_ZNACHENIE_STATUS_OBEKT_VPISAN) {
					if (selectedVidSport!=null && selectedVidSport.size()>0) {
						boolean skip=false;
						for (int i = 0; i < selectedVidSport.size(); i++) {
							if (selectedVidSport.get(i)==DocuConstants.CODE_ZNACHENIE_MNOGOSPORTOV) {
								skip=true;
								break;
							}
						}
						if (!skip) {
							String s=new MMSsportObedinenieDAO(MMSSportnoObedinenie.class, getUserData()).checkForDuplicateVidSportObedinenie(sportObed.getId(), selectedVidSport);
							s=s.replace("[", "").replace("]", "");
							if (s!=null && !s.trim().isEmpty()) {
								save = false;
								JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,"Има въведени обединения с този/тези вид спорт! ЕИК: "+s);
							}	
						}
					}
				}
				
			}
			if (sportObed.getVid()!=null && sportObed.getVid()==DocuConstants.CODE_ZNACHENIE_VID_SPORT_OBEDINENIE_SF) {
				if (getSystemData().getModel().getAttrSpec("vid_sport", "sport_obedinenie", getCurrentLang(), null).isActive() 
						&& getSystemData().getModel().getAttrSpec("vid_sport", "sport_obedinenie", getCurrentLang(), null).isRequired()) {
					if((selectedVidSport == null || selectedVidSport.size() == 0) && sportObed.getVidSportText()==null || sportObed.getVidSportText().trim().isEmpty()) {
						save = false;
						JSFUtils.addMessage("mmsSFform:multipleVidSport", FacesMessage.SEVERITY_ERROR, 
								getMessageResourceString(UI_beanMessages, "general.pleaseInsert", "Вид спорт"));
					}else {
						if (this.sportObed.getStatus()!=null && this.sportObed.getStatus()==DocuConstants.CODE_ZNACHENIE_STATUS_OBEKT_VPISAN) {
							//ako e vpisan iskame ot klasifikaciqta zadalje.
							if(selectedVidSport == null || selectedVidSport.size() == 0) {
								save = false;
								JSFUtils.addMessage("mmsSFform:multipleVidSport", FacesMessage.SEVERITY_ERROR, 
										getMessageResourceString(UI_beanMessages, "general.pleaseInsert", "Вид спорт от класификацията"));
							}
						}
					}
				}	
			}
			
			
			if (getSystemData().getModel().getAttrSpec("br_chlenove", "sport_obedinenie", getCurrentLang(), null).isActive() 
					&& getSystemData().getModel().getAttrSpec("br_chlenove", "sport_obedinenie", getCurrentLang(), null).isRequired()) {
				if(sportObed.getBrChlenove() == null) {
					save = false;
					JSFUtils.addMessage("mmsSFform:brChlenove", FacesMessage.SEVERITY_ERROR, 
							getMessageResourceString(UI_beanMessages, "general.pleaseInsert", "Брой членуващи"));
				}		
			}
			
						
			if (getSystemData().getModel().getAttrSpec("predsedatel", "sport_obedinenie", getCurrentLang(), null).isActive()) {
				if (getSystemData().getModel().getAttrSpec("predsedatel", "sport_obedinenie", getCurrentLang(), null).isRequired()) {
					if(sportObed.getPredsedatel() == null && sportObed.getPredsedatel().isEmpty()) {
									save = false;
									JSFUtils.addMessage("mmsSFform:predsedatel", FacesMessage.SEVERITY_ERROR, 
											getMessageResourceString(UI_beanMessages, "general.pleaseInsert", "Председател"));
					}		
				}
				if (sportObed.getPredsedatel()!=null && !sportObed.getPredsedatel().isEmpty()) {
					
					if(!ValidationUtils.invokeValidation(getSystemData().getModel().getAttrSpec("predsedatel", "sport_obedinenie", getCurrentLang(), null).getValidMethod(), sportObed.getPredsedatel())) {
						save = false;
						JSFUtils.addMessage("mmsSFform:predsedatel", FacesMessage.SEVERITY_ERROR, 
								"Името на председателя трябва да е на кирилица!");
					}
				}
			}
			
			if (getSystemData().getModel().getAttrSpec("gen_sek_direktor", "sport_obedinenie", getCurrentLang(), null).isActive()) {
				if (getSystemData().getModel().getAttrSpec("gen_sek_direktor", "sport_obedinenie", getCurrentLang(), null).isRequired()) {
					if(sportObed.getGenSekDirektor() == null && sportObed.getGenSekDirektor().isEmpty()) {
									save = false;
									JSFUtils.addMessage("mmsSFform:genSekDirektor", FacesMessage.SEVERITY_ERROR, 
											getMessageResourceString(UI_beanMessages, "general.pleaseInsert", "Ген. секретар/ Изп. директор"));
					}		
				}
				if (sportObed.getGenSekDirektor()!=null && !sportObed.getGenSekDirektor().isEmpty()) {
					if(!ValidationUtils.invokeValidation(getSystemData().getModel().getAttrSpec("gen_sek_direktor", "sport_obedinenie", getCurrentLang(), null).getValidMethod(), sportObed.getGenSekDirektor())) {
						save = false;
						JSFUtils.addMessage("mmsSFform:genSekDirektor", FacesMessage.SEVERITY_ERROR, 
								"Невалиден Ген. секретар/ Изп. директор!");
					}
				}
			}
			
			if (getSystemData().getModel().getAttrSpec("dop_info", "sport_obedinenie", getCurrentLang(), null).isActive()) {
				if (getSystemData().getModel().getAttrSpec("dop_info", "sport_obedinenie", getCurrentLang(), null).isRequired()) {
					if(sportObed.getDopInfo() == null && sportObed.getDopInfo().isEmpty()) {
						save = false;
						JSFUtils.addMessage("mmsSFform:note", FacesMessage.SEVERITY_ERROR, 
								getMessageResourceString(UI_beanMessages, "general.pleaseInsert", "Забележка"));
					}		
				}
				if (sportObed.getGenSekDirektor()!=null && !sportObed.getGenSekDirektor().isEmpty()) {
					ValidationUtils.invokeValidation(getSystemData().getModel().getAttrSpec("dop_info", "sport_obedinenie", getCurrentLang(), null).getValidMethod(), sportObed.getDopInfo());
					if(!ValidationUtils.invokeValidation(getSystemData().getModel().getAttrSpec("dop_info", "sport_obedinenie", getCurrentLang(), null).getValidMethod(), sportObed.getDopInfo())) {
						save = false;
						JSFUtils.addMessage("mmsSFform:note", FacesMessage.SEVERITY_ERROR, 
								"Невалидна забележка!");
					}
				}
			}
			save=actionCheckOneYear();
			
			
			
			
			
		} catch (DbErrorException  e) {
			LOGGER.error("Грешка при работа с базата данни! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
		} catch (InvalidParameterException e) {
			LOGGER.error("Грешка при валидиране на спортно обединение! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,"Грешка при валидиране на спортно обединение!", e.getMessage());
		}
				
		
		
		
		return save;
	}
	
	
	
	public void actionCloseChlenstvoFormir() {
		try {
			
			JPA.getUtil().runInTransaction(() -> {
				
				int[] rez= new MMSsportObedinenieDAO(MMSSportnoObedinenie.class,getUserData()).zalichavaneSportFormir(this.sportObed.getId()); 
				
				 if (rez!=null) {
					if(rez[0]>0) {
						JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, "Заличени формирования: "+rez[0]);
					}
					if(rez[1]>0) {
						JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, "Затворени членства: "+rez[1]);
					}
					if (rez[0] == 0 && rez[1] == 0) {
						JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, "Не са намерени формирования, които подлежат на заличаване.");
					}
				}
			});
			
			
			actionLoadFormirList();
		} catch (InvalidParameterException  e) {
			JSFUtils.addErrorMessage(e.getMessage());
			LOGGER.error(e.getMessage(), e);
		} catch (BaseException e) {			
			JSFUtils.addErrorMessage(getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e);
			LOGGER.error(e.getMessage(), e);
		}
		
	}

	public String actionCheckBeforeChangeVpisvane() {
		boolean hasAll=true;
		//ako e vpisan iskame ot klasifikaciqta zadalje.
		if (sportObed.getVid()!=null && sportObed.getVid()==DocuConstants.CODE_ZNACHENIE_VID_SPORT_OBEDINENIE_SF) {
			if(selectedVidSport == null || selectedVidSport.size() == 0) {
				hasAll= false;
				JSFUtils.addMessage("mmsSFform:multipleVidSport", FacesMessage.SEVERITY_ERROR, 
						getMessageResourceString(UI_beanMessages, "general.pleaseInsert", "Вид спорт от класификацията"));
			}
		}
//		for (int i = 0; i < mfList.size(); i++) {
//			if (mfList.get(i).getMejdFed()==null) {
//				hasAll=false;
//				JSFUtils.addMessage("mmsSFform:mejdFed", FacesMessage.SEVERITY_ERROR, 
//						getMessageResourceString(UI_beanMessages, "general.pleaseInsert", "международна федерация от класификацията"));
//				break;
//			}	
//		}
		
		if (hasAll) {
			return "mmsVpisvaneEdit.jsf";
		}else {
			return "";
		}
	}
	
	public void closeModalMail() {
		PrimeFaces.current().executeScript("PF('eMail').hide();");
	}
	
	public void sendMail() {
        boolean sending = true;
        if(subject == null || subject.trim().isEmpty()) {
               JSFUtils.addMessage("mmsSFform:subject", FacesMessage.SEVERITY_ERROR, 
                            getMessageResourceString(UI_beanMessages, "general.pleaseInsert", getMessageResourceString(LABELS, "general.otnosno")));
               sending = false;
        }
        if(mailText == null || mailText.trim().isEmpty()) {
               JSFUtils.addMessage("mmsSFform:mailText", FacesMessage.SEVERITY_ERROR, 
                            getMessageResourceString(UI_beanMessages, "general.pleaseInsert", getMessageResourceString(LABELS, "general.text")));
               sending = false;
        }
        if(!sending)
               return;
        
        Mailer mailer = new Mailer();
        attachedBytes.clear();
		for (Files upLoadedFile : uploadFilesList) {
			attachedBytes.add(new ByteArrayDataSource(upLoadedFile.getContent(), ""));
		}
        try {
               mailer.sent(Content.PLAIN, props, props.getProperty("user.name"), props.getProperty("user.password"),
                            props.getProperty("mail.from"), "Министерство на младежта и спорта", 
                            referent.getContactEmail(),
                            //emcoresp.getEmail() , 
                            //"n.kosev@indexbg.bg", 
                            subject, mailText,
                            attachedBytes);
               JSFUtils.addInfoMessage("Успешно изпращане на съобщението!");
               subject = "";
               mailText = "";
               attachedBytes.clear();
  			   uploadFilesList.clear();
               PrimeFaces.current().executeScript("PF('eMail').hide();");
        } catch (AddressException e) {
               LOGGER.error(e.getMessage());
               JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, "Неуспешно изпращане на съобщението! Грешка в е-мейл адреса!");
        } catch (InvalidParameterException e) {
               LOGGER.error(e.getMessage());
               JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, "Неуспешно изпращане на съобщението!");
        } catch (MessagingException e) {
               LOGGER.error(e.getMessage());
               JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, "Неуспешно изпращане на съобщението!");
        }
  }

	public boolean checkReadyForVpisvane() {
		boolean goVpisvane=true;
		if (sportObed.getVid()!=null && sportObed.getVid()==DocuConstants.CODE_ZNACHENIE_VID_SPORT_OBEDINENIE_SF) {
			if (sportFormList.size()<7) {
				goVpisvane=false;
			}
			if (goVpisvane) {
				HashMap<String, Boolean> mapOblasti=new HashMap<String, Boolean>();
				for (int i = 0; i < sportFormList.size(); i++) {
					mapOblasti.put(""+sportFormList.get(i)[7],true);
				}	
				if (mapOblasti.size()<3) {
					goVpisvane=false;
				}
			}
		}
		if (sportObed.getVid()!=null && sportObed.getVid()==DocuConstants.CODE_ZNACHENIE_VID_SPORT_OBEDINENIE_OK) {
			if (sportFormList.size()<3) {
				goVpisvane=false;
			}
			if (goVpisvane) {
				HashMap<String, Boolean> mapOblasti=new HashMap<String, Boolean>();
				for (int i = 0; i < sportFormList.size(); i++) {
					mapOblasti.put(""+sportFormList.get(i)[7],true);
				}	
				if (mapOblasti.size()>1) {
					goVpisvane=false;
				}
			}
		}
		
		
		return goVpisvane;
	}
	
	/*************** chlenstvo M federacia ****************/
	
	public void actionNewChlenstvoMf() {
		this.currentMf=new MMSSportObedMf();
		editingMf=false;
	}
	
	public void actionEditChlenstvoMf(MMSSportObedMf mf) {
		currentMf=mf;
		editingMf= true;
	}
	
	public void actionSaveChlenstvoMf() {
		try {
			
			boolean save=true;
			if (currentMf.getMejdFed()==null && (currentMf.getMejdFedText()==null || currentMf.getMejdFedText().trim().isEmpty())) {
				save =false;
				JSFUtils.addMessage("mmsSFform:mejdFed", FacesMessage.SEVERITY_ERROR, 
						getMessageResourceString(UI_beanMessages, "general.pleaseInsert", "международна федерация"));
			}else {
				if (this.sportObed.getStatus()!=null && this.sportObed.getStatus()==DocuConstants.CODE_ZNACHENIE_STATUS_OBEKT_VPISAN) {
					if (currentMf.getMejdFed()==null) {
						save =false;
						JSFUtils.addMessage("mmsSFform:mejdFed", FacesMessage.SEVERITY_ERROR, 
								getMessageResourceString(UI_beanMessages, "general.pleaseInsert", "международна федерация от класификацията"));
					}
				}
			}
			
			if (currentMf.getDateBeg()==null) {
				save =false;
				JSFUtils.addMessage("mmsSFform:dateBegMf", FacesMessage.SEVERITY_ERROR, 
						getMessageResourceString(UI_beanMessages, "general.pleaseInsert", "начална дата"));
			}
			if (save) {
				JPA.getUtil().runInTransaction(() -> {
					
					this.currentMf.setIdSportObed(this.sportObed.getId());
					
					new MMSSportObedMFDAO(getUserData()).save(currentMf);
					 
				});
				if (editingMf) {
					for (int i = 0; i < mfList.size(); i++) {
						if (mfList.get(i).getId()!=null && currentMf.getId()!=null && mfList.get(i).getId()==currentMf.getId()) {
							mfList.set(i, currentMf);
							break;
						}
					}
				}else {
					mfList.add(currentMf);	
				}
				
				String  cmdStr = "PF('chlenstvoFed').hide();";
				PrimeFaces.current().executeScript(cmdStr);
				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, getMessageResourceString(UI_beanMessages, SUCCESSAVEMSG));
			}
			
			
			
		} catch (BaseException e) {			
			JSFUtils.addErrorMessage(getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e);
			LOGGER.error(e.getMessage(), e);
		}
	}
	
	public void removeChlenstvFromList(MMSSportObedMf mf) {
		try {
			this.mfList.remove(mf);
			JPA.getUtil().runInTransaction(() -> { 
				
				new MMSSportObedMFDAO(getUserData()).delete(mf);
			
			});
			
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO,  "Успешно изтриване!" );
			
		} catch (BaseException e) {
			JSFUtils.addErrorMessage(getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e);
			LOGGER.error(e.getMessage(), e);
		}
	}
	
	/***************** krai chlenstvo federacia *****************/
	
	/****************** chlenstvo formirovanie *************/
	
	public void actionLoadFormirList() {
		if (this.sportObed.getId()!=null) {
			try {
				JPA.getUtil().runWithClose(() -> this.sportFormList=new MMSChlenstvoDAO(MMSChlenstvo.class, getUserData()).findChlenstvaFormirovaniqVObedinenie(this.sportObed.getId()));
			} catch (BaseException e) {
				JSFUtils.addErrorMessage(getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e);
				LOGGER.error(e.getMessage(), e);
			}
				
		}
		
	}
	public String actionViewFormir(Integer idObj) {
		return "mmsSportFormirovanieView.xhtml?faces-redirect=true&idObj="+idObj;
	}
	public String actionNewChlenstvoFormir() {
		return "mmsSportFormirovanieEdit.jsf?faces-redirect=true&idObedinenie="+this.sportObed.getId();
	}
	
	public void removeChlenstvoFormirList(Object[] mf) {
		try {
			this.sportFormList.remove(mf);
			JPA.getUtil().begin();
			 	
				new MMSChlenstvoDAO(MMSChlenstvo.class,getUserData()).deleteById(SearchUtils.asInteger(mf[0]));
			
				new MMSsportObedinenieDAO(MMSSportnoObedinenie.class, getUserData()).updateBrChlenove(this.sportObed.getId());
				sportObed.setBrChlenove(sportFormList.size());
			JPA.getUtil().commit();
			
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO,  "Успешно изтриване!" );
			
		} catch (BaseException e) {
			JSFUtils.addErrorMessage(getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e);
			LOGGER.error(e.getMessage(), e);
		}finally {
			JPA.getUtil().closeConnection();
		}
	}
	
	
	/*************** krai chlenstvo formirovanie *****************/
	
	public void actionVidSportChange() {
		this.sportObed.setTypeSportBool(false);
		this.sportObed.setVoenenSportBool(false);
		try {
			for (int i = 0; i < selectedVidSport.size(); i++) {
				if (getSystemData().matchClassifItems(DocuConstants.CODE_CLASSIF_VIDOVE_SPORT_OLIMP , selectedVidSport.get(i), new Date())) {
					this.sportObed.setTypeSportBool(true);				
				}
				if (getSystemData().matchClassifItems(DocuConstants.CODE_CLASSIF_VIDOVE_SPORT_VOEN , selectedVidSport.get(i), new Date())) {
					this.sportObed.setVoenenSportBool(true);					
				}				
			}
			sportObed.getVidSportList().clear();
			makeVidSportStringArrayAsObject();
			
			if (selectedVidSport!=null && selectedVidSport.size()>0) {
				String s=new MMSsportObedinenieDAO(MMSSportnoObedinenie.class, getUserData()).checkForDuplicateVidSportObedinenie(sportObed.getId(), selectedVidSport);
				s=s.replace("[", "").replace("]", "");
				if (s!=null && !s.trim().isEmpty()) {
					JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_WARN,"Има въведени обединения с този/тези вид спорт! ЕИК: "+s);
				}	
			}
			
		} catch (NumberFormatException e) {
			LOGGER.error("Грешка при форматиране! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,"Грешка при обработка на данни!", e.getMessage());
		} catch (DbErrorException e) {
			LOGGER.error("Грешка при работа с базата данни! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
		}
	}

	
	
	private void getVidSportAStrings(List<MMSVidSportSO> vidSportList) throws DbErrorException {
		selectedVidSport = new ArrayList<Integer>();
		selectedVidSportTxt="";
		for (int i = 0 ; i < vidSportList.size() ; i++) {
			selectedVidSport.add(vidSportList.get(i).getVidSport());
			if (selectedVidSportTxt.length()>0) {
				selectedVidSportTxt+=", "+getSd().decodeItem(DocuConstants.CODE_CLASSIF_VIDOVE_SPORT, vidSportList.get(i).getVidSport(), getCurrentLang(), new Date());
			}else {
				selectedVidSportTxt+=getSd().decodeItem(DocuConstants.CODE_CLASSIF_VIDOVE_SPORT, vidSportList.get(i).getVidSport(), getCurrentLang(), new Date());
			}
		}
	}
	
	private void makeVidSportStringArrayAsObject() {
		for (int i = 0; i < selectedVidSport.size(); i++) {
			MMSVidSportSO tmp = new MMSVidSportSO();
			tmp.setTipОbject(DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORT_OBED); // koda na object Sportno обед
			tmp.setIdObject(sportObed.getId());
			tmp.setVidSport(Integer.valueOf(selectedVidSport.get(i)));
			tmp.setUserReg(getCurrentUserId());
			tmp.setDateReg(new Date());
			sportObed.getVidSportList().add(tmp);
		}
		
	}
	/********************** ВПИСВАНИЯ ************************/
	
	public void findVpisvane() {
		try {		
			JPA.getUtil().runWithClose(() -> setRegsList( new MMSVpisvaneDAO(getUserData()).findRegsListByIdAndType(DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORT_OBED, this.sportObed.getId())));
		
		} catch (BaseException e) {
			LOGGER.error("Грешка при зареждане на списъка с вписвания! ", e);
		}
	}
	
	
	private void findDocs() {
		try {
			JPA.getUtil().runWithClose(() -> docsList = new LazyDataModelSQL2Array((new MMSVpisvaneDAO(getUserData()).findDocsList(sportObed.getId(),  DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORT_OBED)), " doc_date asc "));
		} catch (BaseException e) {
			LOGGER.error("Грешка при зареждане на списъка с dokumenti kum вписвания! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, getMessageResourceString(UI_beanMessages, "so.errFindDoc"));
		}
	}
	
	
	/*****************************************/
	
	/**
	 * зарежда адреса на кореспондента
	 */
	public void loadDopInfoAdres() {
		if(sportObed.getIdObject() != null) {
			// ако нямам права да виждам лини данни
			// заради достъпа до личните данни - в допълнителната информаиця за физическите лица да остане само населеното място!!
			try {				
				this.dopInfoAdres = getSystemData().decodeItemDopInfo(DocuConstants.CODE_CLASSIF_REFERENTS, sportObed.getIdObject(), getCurrentLang(), new Date());
				if(this.dopInfoAdres != null &&
					(int) getSystemData().getItemSpecific(DocuConstants.CODE_CLASSIF_REFERENTS, sportObed.getIdObject() ,  getCurrentLang(), new Date(), DocuClassifAdapter.REFERENTS_INDEX_REF_TYPE) == DocuConstants.CODE_ZNACHENIE_REF_TYPE_NFL) {
				
					if(!getUserData().hasAccess(DocuConstants.CODE_CLASSIF_DEF_PRAVA, DocuConstants.CODE_ZNACHENIE_DEF_PRAVA_SEE_PERSONAL_DATA) ) {
						// да остане само град или село  
						int i1 = this.dopInfoAdres.indexOf("гр.");
						if(i1 == -1) {
							i1 = this.dopInfoAdres.indexOf("с.");
						}
						if(i1 != -1) {						
							int i2 = this.dopInfoAdres.indexOf(", ", i1);
							if(i2 != -1) {
								this.dopInfoAdres = this.dopInfoAdres.substring(i1, i2);
							}else {
								// има само град или село...
								this.dopInfoAdres = this.dopInfoAdres.substring(i1);
							}
						}else {
							this.dopInfoAdres = null;
						}
					}else { // да махна ЕГН, за да остане само адреса
						int i1 = this.dopInfoAdres.indexOf("ЕГН");
						if(i1 != -1) {	
							//има егн
							int i2 = this.dopInfoAdres.indexOf(", ", i1);
							if(i2 != -1) {
								this.dopInfoAdres = this.dopInfoAdres.substring(i2+1);
							}else {
								this.dopInfoAdres = null; // има само егн...
							}
						}
					}
				}			
			} catch (Exception e) {
				LOGGER.error("Грешка при формиране на адрес на кореспонднета за показване в документа! ", e);
			}
			
		}else {
			this.dopInfoAdres = null; 
		}
	}
	
	public void clearInfoAdres() {
		this.dopInfoAdres = null; 
	}
	
	public MMSSportnoObedinenie getSportObed() {
		return sportObed;
	}

	public void setSportObed(MMSSportnoObedinenie sportObed) {
		this.sportObed = sportObed;
	}

	public Date getDecodeDate() {
		return decodeDate;
	}

	public void setDecodeDate(Date decodeDate) {
		this.decodeDate = decodeDate;
	}

	public String getTxtCorresp() {
		return txtCorresp;
	}

	public void setTxtCorresp(String txtCorresp) {
		this.txtCorresp = txtCorresp;
	}

	public LazyDataModelSQL2Array getDocsList() {
		return docsList;
	}

	public void setDocsList(LazyDataModelSQL2Array docsList) {
		this.docsList = docsList;
	}

	public List<Integer> getSelectedVidSport() {
		return selectedVidSport;
	}

	public void setSelectedVidSport(List<Integer> selectedVidSport) {
		
		this.selectedVidSport = selectedVidSport;
		actionVidSportChange();
	}

	public MMSSportObedMf getCurrentMf() {
		return currentMf;
	}

	public void setCurrentMf(MMSSportObedMf currentMf) {
		this.currentMf = currentMf;
	}

	public List<MMSSportObedMf> getMfList() {
		return mfList;
	}

	public void setMfList(List<MMSSportObedMf> mfList) {
		this.mfList = mfList;
	}

	public List<MMSVpisvane> getRegsList() {
		return regsList;
	}

	public void setRegsList(List<MMSVpisvane> list) {
		this.regsList = list;
	}

	public List<Object[]> getSportFormList() {
		return sportFormList;
	}

	public void setSportFormList(List<Object[]> sportFormList) {
		this.sportFormList = sportFormList;
	}

	public BaseSystemData getSd() {
		return sd;
	}

	public void setSd(BaseSystemData sd) {
		this.sd = sd;
	}

	public boolean isEditingMf() {
		return editingMf;
	}

	public void setEditingMf(boolean editingMf) {
		this.editingMf = editingMf;
	}

	public String getDopInfoAdres() {
		if(this.dopInfoAdres == null || this.dopInfoAdres.trim().isEmpty()) {
			loadDopInfoAdres();
		}
		return dopInfoAdres;
	}

	public void setDopInfoAdres(String dopInfoAdres) {
		this.dopInfoAdres = dopInfoAdres;
	}
	
	public String getSelectedVidSportTxt() {
		return selectedVidSportTxt;
	}

	public void setSelectedVidSportTxt(String selectedVidSportTxt) {
		this.selectedVidSportTxt = selectedVidSportTxt;
	}

	
	/**************************************************** FROM SSEV ****************************************************/	

	private Integer idSSev;
	private Integer vidDoc;
	private String regNom;
	private Date dataDoc;
	private String otnosno;
	private String egn;
	private String eik;
	private Referent ref = new Referent();
	private MMSVpisvane vpisvane = new MMSVpisvane();
	private boolean noVp = false;
	
	// Метода е за извикване след запис на обектите ако са извикани от "Нови заявления"!
	public void actionSaveDocFromSeos() {
		
		Doc newDoc = new Doc();	
		MMSVpisvaneDoc vpisvaneDoc = new MMSVpisvaneDoc();
		
		boolean saveNewVp = false;
		
		try {
			
			JPA.getUtil().runWithClose(() -> {
				
				List<MMSVpisvane> regList = new MMSVpisvaneDAO(getUserData()).findRegsListByIdAndType(this.sportObed.getCodeMainObject(), this.sportObed.getId()); 
				
				if (!regList.isEmpty()) {
					vpisvane = new MMSVpisvaneDAO(getUserData()).findById(regList.get(0).getId()); 
				
				} else {						
					noVp = true;					
				}
				
			});	
			
			if (vidDoc.equals(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_SPORTNO_OBEDINENIE)
					|| vidDoc.equals(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_OSK)
					|| vidDoc.equals(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_NOSTD)
					|| vidDoc.equals(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_NOUS) ) { 
				
				boolean existActVpis = false;
				
				if (vpisvane.getStatusVpisvane() != null && vpisvane.getStatusVpisvane().equals(DocuConstants.CODE_ZNACHENIE_STATUS_REG_VPISAN)) {

					existActVpis = true;
				
				} else if (vpisvane.getStatusVpisvane() == null && vpisvane.getStatusResultZaiavlenie() != null
						&& (vpisvane.getStatusResultZaiavlenie().equals(DocuConstants.CODE_ZNACHENIE_STATUS_ZAIAVLENIE_V_RAZGLEJDANE)
								|| vpisvane.getStatusResultZaiavlenie().equals(DocuConstants.CODE_ZNACHENIE_STATUS_ZAIAVLENIE_VPISAN))) {

					existActVpis = true;
				}
				
				if (noVp ||	(!noVp && !existActVpis) ) {
					
					saveNewVp = true;
					noVp = false; // за да запише ново вписване, когато отговаря на условията
				
				} else {
				
					saveNewVp = false;
					JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString("beanMessages", "mmsSpObed.noSaveZaiav"));	
					PrimeFaces.current().executeScript("scrollToErrors()");
					return;				
				}
			
			} else {
				
				if (noVp) {
					
					//Ако няма нито едно вписване - съобщение, че няма към кое вписване да се направи заличаване или промяна на обстоятелствата
					if (vidDoc.equals(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_PROM_SPORTNO_OBEDINENIE)) {
						JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString("beanMessages", "mmsSpObed.noSaveZaiavPrObst"));						
					}
					
					if (vidDoc.equals(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_ZALICH_SPORTNO_OBEDINENIE)) {
						JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString("beanMessages", "mmsSpObed.noSaveZaiavZalich"));						
					}
					
					PrimeFaces.current().executeScript("scrollToErrors()");
					return;
					
				} 
			}
			
			if (!noVp) {
			
				// настройка по вид документ и регистратура
				Object[] docVidSetting = new DocDAO(getUserData()).findDocSettings(getUserData(UserData.class).getRegistratura(), this.vidDoc, getSystemData());
				
				if (docVidSetting == null) {
					
					String noSett = getSystemData().decodeItem(DocuConstants.CODE_CLASSIF_DOC_VID, this.vidDoc, getCurrentLang(), new Date());								
					
					JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString("beanMessages", "compReg.noDocSettings", noSett));	
					PrimeFaces.current().executeScript("scrollToErrors()");
					return;
				
				} else {
					
					newDoc.setDocVid(this.vidDoc);
					newDoc.setRnDoc(this.regNom);	
					newDoc.setDocDate(this.getDataDoc()); 
				
					if (this.egn != null) {
						JPA.getUtil().runWithClose(() -> ref = new ReferentDAO(getUserData()).findByIdent(null, this.egn, null, DocuConstants.CODE_ZNACHENIE_REF_TYPE_FZL)); 
					} else if (this.eik != null) {
						JPA.getUtil().runWithClose(() -> ref = new ReferentDAO(getUserData()).findByIdent(this.eik, null, null, DocuConstants.CODE_ZNACHENIE_REF_TYPE_NFL)); 
					}
					if (ref != null) {
						newDoc.setCodeRefCorresp(ref.getCode()); 
					} 
					
					newDoc.setRegisterId((Integer) docVidSetting[1]);
					boolean createObed = Objects.equals(docVidSetting[2], Constants.CODE_ZNACHENIE_DA);
					
					Integer typeDocByRegister = (Integer) getSystemData().getItemSpecific(DocuConstants.CODE_CLASSIF_REGISTRI, newDoc.getRegisterId(), getCurrentLang(), new Date() , DocuClassifAdapter.REGISTRI_INDEX_DOC_TYPE);
					
					newDoc.setRegistraturaId(getUserData(UserData.class).getRegistratura());
					newDoc.setDocType(typeDocByRegister);
					newDoc.setFreeAccess(Constants.CODE_ZNACHENIE_DA);	
					
					if (newDoc.getDocType().intValue() == DocuConstants.CODE_ZNACHENIE_DOC_TYPE_IN) {  
						newDoc.setProcDef((Integer) docVidSetting[5]);
					
					} else if (newDoc.getDocType().intValue() == DocuConstants.CODE_ZNACHENIE_DOC_TYPE_OWN) {  
						newDoc.setProcDef((Integer) docVidSetting[6]);
					
					} else if (newDoc.getDocType().intValue() == DocuConstants.CODE_ZNACHENIE_DOC_TYPE_WRK) {  
						newDoc.setProcDef((Integer) docVidSetting[7]);
					}
					
					if (SearchUtils.isEmpty(this.otnosno)) {
						
						newDoc.setOtnosno(getSystemData().decodeItem(DocuConstants.CODE_CLASSIF_DOC_VID, newDoc.getDocVid(), getCurrentLang(), new Date()));
					} else {
						
						newDoc.setOtnosno(this.otnosno); 
					}
					
					if (saveNewVp) {
						
						vpisvane = new MMSVpisvane();
						
						vpisvane.setRnDocZaiavlenie(this.regNom);
						vpisvane.setDateDocZaiavlenie(this.getDataDoc());
						vpisvane.setStatusResultZaiavlenie(DocuConstants.CODE_ZNACHENIE_STATUS_ZAIAVLENIE_V_RAZGLEJDANE);
						vpisvane.setDateStatusZaiavlenie(new Date());
						vpisvane.setIdObject(this.sportObed.getId()); // ИД на обекта
						vpisvane.setTypeObject(this.sportObed.getCodeMainObject()); //КОДА на обекта
						vpisvane.setNachinPoluchavane(this.sportObed.getNachinPoluch());
						vpisvane.setAddrMailPoluchavane(this.sportObed.getNachinPoluchText());
					
					} 
					
					JPA.getUtil().runInTransaction(() -> { 
						
						List<Integer> idDocsList = new DocDAO(getUserData()).findDocIdList(newDoc.getRnDoc(), newDoc.getDocDate());
						Integer idDoc = null;
						
						if (!idDocsList.isEmpty()) {
							
							idDoc = idDocsList.get(0);							
						}
						
						if(idDoc == null) {
							// записва се документа при нас
							new DocDAO(getUserData()).save(newDoc, createObed, null, null, getSystemData());
							idDoc = newDoc.getId();
							
							copyEgovFiles(idDoc);
						} 
						
						// заисва се ново вписване, ако е такова заявлението
						if (vpisvane.getId() == null) {
							new MMSVpisvaneDAO(getUserData()).save(vpisvane);
						}
												
						// записваме връзката на документа с вписването
						if (idDoc != null && vpisvane.getId() != null) {
							
							vpisvaneDoc.setIdObject(vpisvane.getIdObject());
							vpisvaneDoc.setTypeObject(vpisvane.getTypeObject()); 
							vpisvaneDoc.setIdVpisvane(vpisvane.getId());
							vpisvaneDoc.setIdDoc(idDoc);
							
							new MMSVpisvaneDocDAO(getUserData()).save(vpisvaneDoc);
						}
						
						//TODO - дали така ще се ъпдейтва статуса в egov_messages - msg_status и msg_status_dat
						
						EgovMessages egovMess = new EgovMessagesDAO(getUserData()).findById(this.idSSev); 
						
						egovMess.setMsgRn(newDoc.getRnDoc());
						egovMess.setMsgRnDate(newDoc.getDocDate());
						egovMess.setMsgStatus("DS_REGISTERED");	
						egovMess.setMsgStatusDate(new Date());
						
						new EgovMessagesDAO(getUserData()).save(egovMess);
						
						new MMSVpisvaneDAO(getUserData()).updateStatusReg(DocuConstants.CODE_ZNACHENIE_STATUS_OBEKT_V_RAZGLEJDANE, new Date(), this.sportObed.getId(), DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORT_OBED);						
					});					
					
				}
			}
		
		} catch (ObjectInUseException e) {
			LOGGER.error("Грешка при запис на документ! ObjectInUseException = {}", e.getMessage());
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_WARN, e.getMessage());
		
		} catch (DbErrorException e) {
			LOGGER.error("Грешка при зареждане настройки по вид документ!! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString("ui_beanMessages", ERRDATABASEMSG), e.getMessage());
		
		} catch (BaseException e) {
			LOGGER.error("Грешка при регистриране на вписване", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString("ui_beanMessages", ERRDATABASEMSG), e.getMessage());
		
		}  		
		
	}

	/**
	 * @param idDoc
	 * @throws DbErrorException
	 */
	private void copyEgovFiles(Integer idDoc) throws DbErrorException {
		try {
			FilesDAO filesDao = new FilesDAO(getUserData());
			List<EgovMessagesFiles> egovFiles = new EgovMessagesDAO(getUserData()).findFilesByMessage(this.idSSev);
			for (EgovMessagesFiles egovFile : egovFiles) {
				Files f = new Files();
				
				f.setContent(egovFile.getBlobcontent());
				f.setContentType(egovFile.getMime());
				f.setFilename(egovFile.getFilename());
				
				filesDao.saveFileObject(f, idDoc, DocuConstants.CODE_ZNACHENIE_JOURNAL_DOC);
			}
			
		} catch (Exception e) { // TODO за да не счупим цялата работа само ще се логва за сега!
			LOGGER.error("Грешка при копирана на файловете от ЕГОВ!", e);
		}
	}

	public Integer getIdSSev() {
		return idSSev;
	}

	public void setIdSSev(Integer idSSev) {
		this.idSSev = idSSev;
	}

	public Integer getVidDoc() {
		return vidDoc;
	}

	public void setVidDoc(Integer vidDoc) {
		this.vidDoc = vidDoc;
	}

	public String getRegNom() {
		return regNom;
	}

	public void setRegNom(String regNom) {
		this.regNom = regNom;
	}

	 

	public String getOtnosno() {
		return otnosno;
	}

	public void setOtnosno(String otnosno) {
		this.otnosno = otnosno;
	}

	public String getEgn() {
		return egn;
	}

	public void setEgn(String egn) {
		this.egn = egn;
	}

	public String getEik() {
		return eik;
	}

	public void setEik(String eik) {
		this.eik = eik;
	}

	public Referent getRef() {
		return ref;
	}

	public void setRef(Referent ref) {
		this.ref = ref;
	}

	public MMSVpisvane getVpisvane() {
		return vpisvane;
	}

	public void setVpisvane(MMSVpisvane vpisvane) {
		this.vpisvane = vpisvane;
	}

	public boolean isNoVp() {
		return noVp;
	}

	public void setNoVp(boolean noVp) {
		this.noVp = noVp;
	}

	public String getMailText() {
		return mailText;
	}

	public void setMailText(String mailText) {
		this.mailText = mailText;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public boolean isWillShowMailModal() {
		return willShowMailModal;
	}

	public void setWillShowMailModal(boolean willShowMailModal) {
		this.willShowMailModal = willShowMailModal;
	}

	public static Properties getProps() {
		return props;
	}

	public static void setProps(Properties props) {
		SportObedEdit.props = props;
	}

	public Referent getReferent() {
		return referent;
	}

	public void setReferent(Referent referent) {
		this.referent = referent;
	}

	public String getVidDocVpisvText() {
		return vidDocVpisvText;
	}

	public void setVidDocVpisvText(String vidDocVpisvText) {
		this.vidDocVpisvText = vidDocVpisvText;
	}

	public Date getDateRNV() {
		return dateRNV;
	}

	public void setDateRNV(Date dateRNV) {
		this.dateRNV = dateRNV;
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

	public List<SelectItem> getMsgStatusList() {
		return msgStatusList;
	}

	public void setMsgStatusList(List<SelectItem> msgStatusList) {
		this.msgStatusList = msgStatusList;
	}

	public String getReasonOtkaz() {
		return reasonOtkaz;
	}

	public void setReasonOtkaz(String reasonOtkaz) {
		this.reasonOtkaz = reasonOtkaz;
	}

	public UserData getUd() {
		return ud;
	}

	public void setUd(UserData ud) {
		this.ud = ud;
	}
	
	/**************************************************** END FROM SSEV ****************************************************/
	
	public String actionGoToFormirovanie() {
			return "mmsSportFormirovanieEdit.xhtml?idObedinenie=" + sportObed.getId();
	}

	public void setDataDoc(Date dataDoc) {
		this.dataDoc = dataDoc;
	}

	public Date getDataDoc() {
		return dataDoc;
	}

	public EgovMessagesCoresp getEgovCoresp() {
		return egovCoresp;
	}

	public void setEgovCoresp(EgovMessagesCoresp egovCoresp) {
		this.egovCoresp = egovCoresp;
	}

	public ArrayList<Files> getUploadFilesList() {
		return uploadFilesList;
	}

	public void setUploadFilesList(ArrayList<Files> uploadFilesList) {
		this.uploadFilesList = uploadFilesList;
	}

	public Integer getIdFormir() {
		return idFormir;
	}

	public void setIdFormir(Integer idFormir) {
		this.idFormir = idFormir;
	}
}