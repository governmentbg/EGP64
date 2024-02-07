package com.ib.docu.ws.signdoc.impl;


import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Date;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.persistence.Query;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aspose.words.Document;
import com.ib.docu.db.dao.DocDAO;
import com.ib.docu.db.dto.Doc;
import com.ib.docu.experimental.Notification;
import com.ib.docu.system.DocuConstants;
import com.ib.docu.system.SystemData;
import com.ib.docu.system.UserData;
import com.ib.docu.ws.signdoc.DeloTransfer;
import com.ib.docu.ws.signdoc.WSFault;
import com.ib.indexui.db.dto.AdmUser;
import com.ib.system.db.JPA;
import com.ib.system.db.dao.FilesDAO;
import com.ib.system.db.dto.Files;
import com.ib.system.utils.SearchUtils;

@WebService(serviceName = "DeloTransferService", endpointInterface = "com.ib.docu.ws.signdoc.DeloTransfer", targetNamespace = "http://wstransfer.delo.indexbg.com/")
public class DeloTransferImpl implements DeloTransfer {
	
	@Resource
	WebServiceContext wsContext;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DeloTransferImpl.class);
	
	
	
	
	public int getServiceVersion() {
		
// за тест изпращане на нотификация за презареждане		
//		ServletContext servletContext = (ServletContext) wsContext.getMessageContext().get(MessageContext.SERVLET_CONTEXT);
//		SystemData sd = (SystemData) servletContext.getAttribute("systemData");
//		
//		if(sd!=null) {
//			System.out.println("ok");
//	
//			
//			try {
//				
//				Notification fake = new Notification(sd);
//			
//				fake.generatеFakeNotif(DocuConstants.CODE_FAKE_NOTIF_RELOAD_FILES_SIGN);
//			
//				fake.getAdresati().add(-1);
//				fake.send();
//				
//
//				System.out.println("ok2");
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		} else {
//			System.out.println("false");
//		}
		
		return 2;
	}

	@SuppressWarnings("unchecked")
	public java.lang.String uploadFile(byte[] fileContent, java.lang.String fileName) throws WSFault {
		LOGGER.debug("filename="+fileName);
		LOGGER.debug("filename.trim().length="+fileName.trim().length());
		
		

		String guid = "";
		if (fileName == null || fileName.trim().length() < 46) {
			LOGGER.debug("Променено име на файл - 1");
			throw new WSFault(3, "Променено име на файл ! Промяната не може да бъде извършена !", null);
		} else {
			
			//Тък ще направим една тъпотия за махна на (1) от именат ана файловете
			int lastPointIndex = fileName.lastIndexOf(".");
			
			if (lastPointIndex > 0) {
				fileName = fileName.replaceAll(" ", "");
				if (fileName.substring(lastPointIndex-2, lastPointIndex-1 ).equals(")")){					
					int firstIndex = fileName.lastIndexOf("(");
					if (firstIndex > 0) {
						String dublString  = fileName.substring(firstIndex, lastPointIndex-2);
						fileName = fileName.replaceAll("\\"+ dublString + "\\)", "");						
					}
				}
				
			}
			LOGGER.debug("After jurking ;) filename="+fileName);
			
			
			guid = fileName.substring(14, 46);
			LOGGER.debug("guid=|"+guid+ "|");
			
			
			if (guid.length() != 32) {
				LOGGER.debug("Променено име на файл - 2");
				throw new WSFault(3, "Променено име на файл ! Промяната не може да бъде извършена !", null);
			}
		}
		LOGGER.debug("guid=" + guid);
		
		int docId = 0 ;
		long countSign = 0;
		int typeDein =  0;	
		try {
			LOGGER.debug("countSign="+fileName.substring(8,10));
			LOGGER.debug("typeDein="+fileName.substring(11,13));
			countSign = Long.parseLong(fileName.substring(8,10) );
			typeDein =  Integer.parseInt(fileName.substring(11,13) );		
			docId = Integer.parseInt(fileName.substring(47, fileName.lastIndexOf(".")));
		} catch (NumberFormatException e2) {
			LOGGER.debug("Името на файла не е форматирано правилно !");
			throw new WSFault(13, "Името на файла не е форматирано правилно ! Промяната не може да бъде извършена !", null);
		}
		
		

		
		MessageContext msgCtxt = wsContext.getMessageContext();
		HttpServletRequest req = (HttpServletRequest) msgCtxt.get(MessageContext.SERVLET_REQUEST);
		String clientIP = req.getRemoteAddr();
		LOGGER.debug("IP=" + clientIP);
	
		//String clientIP = "127.0.0.1";
	
		
		
		

		
		Query sqlQuery = JPA.getUtil().getEntityManager().createNativeQuery("select FILE_ID, EXPORT_TIME, EXPORT_IP  from FILES where EXPORT_GUID = :GGG");
		sqlQuery.setParameter("GGG", guid);

		ArrayList<Object[]> rows = (ArrayList<Object[]>) sqlQuery.getResultList();
		

		if (rows.size() == 0) {
			throw new WSFault(4, "Файлът не е маркиран за редакция ! Промяната не може да бъде извършена !", null);
		}
		
		Object[] row = rows.get(0);
		String fileIp = SearchUtils.asString(row[2]);
		Date datExport = SearchUtils.asDate(row[1]);
		Integer fileId = SearchUtils.asInteger(row[0]);
		
		
		
		if (!clientIP.equals(fileIp)) {
			throw new WSFault(5, "Файлът свален за промяна от друг IP адрес ! Промяната не може да бъде извършена !", null);
		}
		
		if (new Date().getTime() - datExport.getTime() > 1800000) {
			throw new WSFault(6, "Времето за промяна на файла е изтекло ! Промяната не може да бъде извършена !", null);
		}
		
		//Проверка за подпис
		Integer signed = 2;
		long countSignNew = 0;
		try {
			ByteArrayInputStream bis = new ByteArrayInputStream(fileContent);
			Document doc = new Document(bis);
			countSignNew = doc.getDigitalSignatures().getCount();
			if (countSignNew > 0) {
				signed = 1;
			}
		} catch (Exception e1) {
			//Не го разпознава
			signed = 2;
		}
		
		
		
		
		
		
		
		
		try {
			JPA.getUtil().begin();
			
			
			
			Files file =  JPA.getUtil().getEntityManager().find(Files.class, fileId);	
			if (file == null) {
				throw new WSFault(12, "Файла не е намерен ! Промяната не може да бъде извършена !", null);
			}
			
			Integer userId = file.getExportUser();
			
			if (userId == null) {
				throw new WSFault(10, "Файлът не е свален за промяна ! Промяната не може да бъде извършена !", null);
			}
				
			
			AdmUser user = JPA.getUtil().getEntityManager().find(AdmUser.class, userId);
			if (user == null) {
				throw new WSFault(11, "Файлът не е свален за промяна от потребител ! Промяната не може да бъде извършена !", null);
			}
			
			UserData ud = new UserData(userId, user.getUsername(), user.getNames());
			
			
			FilesDAO fdao = new FilesDAO(ud);			
			DocDAO ddao = new DocDAO(ud);
			
			
			
			if (docId > 0) {
				Doc doc = ddao.findById(docId);
				if (doc != null) {
					if (!doc.getDocType().equals(DocuConstants.CODE_ZNACHENIE_DOC_TYPE_WRK)) {
						
						//Махаме официалността на старата връзка

						sqlQuery = JPA.getUtil().getEntityManager().createNativeQuery("update FILE_OBJECTS set OFFICIAL = :DANE where FILE_ID = :FILEID and OBJECT_CODE = :DOCCODE and OBJECT_ID = :DOCID");
						sqlQuery.setParameter("DANE", DocuConstants.CODE_ZNACHENIE_NE);
						sqlQuery.setParameter("FILEID", file.getId());
						sqlQuery.setParameter("DOCCODE", DocuConstants.CODE_ZNACHENIE_JOURNAL_DOC);
						sqlQuery.setParameter("DOCID", doc.getId());
						sqlQuery.executeUpdate();
						
						
						
						Files newFile = new Files();
						
						
						String fn = file.getFilename();
						if (fn.contains(".")) {
							fn = fn.substring(0,fn.lastIndexOf("."))  + "_Official" + fn.substring(fn.lastIndexOf("."));
						}else {
							fn+= "_Official";
						}
						
						
						
											
						newFile.setFilename(fn);
						newFile.setSigned(signed);
						newFile.setPersonalData(file.getPersonalData());
						newFile.setContentType(file.getContentType());
						newFile.setContent(fileContent);
						newFile.setDateReg(new Date());
						newFile.setUserReg(userId);
						
						newFile.setOfficial(DocuConstants.CODE_ZNACHENIE_DA);
						//newFile.setFilePurpose(fileObject.getFilePurpose());
						//newFile.setFileType(fileObject.getFileType());
						newFile.setFileInfo(file.getFileInfo());
												
						fdao.saveFileObject(newFile, docId, DocuConstants.CODE_ZNACHENIE_JOURNAL_DOC);
						
						
						
					}else {
						file.setContent(fileContent);
						file.setSigned(signed);
					}
					
//					if (countSignNew > countSign & userId != null) {
//						//Появил се е нов подпис
//						if (DocuConstants.CODE_WS_DEIN_ZA_SAGL == typeDein) {
//							//За съгласуване е
//							DeloDocSagl sagl = new DeloDocSagl();
//							sagl.setSagl(user.getId_lice());
//							sagl.setSaglDate(new Date());
//							doc.getDeloDocSagl().add(sagl);
//							ddao.save(doc);
//							
//							SQLQuery q = HibernateUtil.currentSession().createSQLQuery("update  MAIL set tip = :tipnew where TIP = :tipold and CODE_OBJECT = :co and ID_OBJECT = :ido and ID_POLUCHATEL = :iduser");
//							q.setParameter("tipnew", Constants.MAIL_TYPE_FORWARD_SAGL_DONE);
//							q.setParameter("tipold", Constants.MAIL_TYPE_FORWARD_SAGL);
//							q.setParameter("co", Constants.CODE_OBJECT_DOC);
//							q.setParameter("ido", doc.getId());
//							q.setParameter("iduser", userId);
//							q.executeUpdate();
//						}else {
//							if (Constants.MAIL_TYPE_FORWARD_PODPIS.longValue() == typeDein) {
//								//За подписване
//								DeloDocPodpis podpis = new DeloDocPodpis();
//								podpis.setPodpis(user.getId_lice());
//								podpis.setPodpisDate(new Date());
//								doc.getDeloDocPodpis().add(podpis);
//								
//								
//								if (doc.getTipDoc().longValue() == Constants.CODE_ZNACHENIE_RABOTEN_DOC) {
//									
//									if (doc.getForReg() == null ||  doc.getForReg().longValue() != Constants.CODE_ZNACHENIE_DA) {
//										doc.setForReg(new Long(Constants.CODE_ZNACHENIE_DA));
//										try {
//											new MailPatterns(sd).generateMailValidateDoc(doc, userId);
//										} catch (Exception e) {
//											LOGGER.error("Грешка при генериане на мейл !", e);
//										}
//									}
//									
//									
//								}
//								
//								
//								ddao.save(doc);
//								
//								SQLQuery q = HibernateUtil.currentSession().createSQLQuery("update  MAIL set tip = :tipnew where TIP = :tipold and CODE_OBJECT = :co and ID_OBJECT = :ido and ID_POLUCHATEL = :iduser");
//								q.setParameter("tipnew", Constants.MAIL_TYPE_FORWARD_PODPIS_DONE);
//								q.setParameter("tipold", Constants.MAIL_TYPE_FORWARD_PODPIS);
//								q.setParameter("co", Constants.CODE_OBJECT_DOC);
//								q.setParameter("ido", doc.getId());
//								q.setParameter("iduser", userId);
//								q.executeUpdate();
//								
//							}
//						}
//					}
					
				}
			}else {
				file.setContent(fileContent);
				file.setSigned(signed);
			}
			
						
			file.setExportGuid(null);
			file.setExportIP(null);
			file.setExportTime(null);
			file.setExportUser(null);
			
			fdao.save(file);

			
			
			
			 sqlQuery = JPA.getUtil().getEntityManager().createNativeQuery("delete from LOCK_OBJECTS where OBJECT_TIP = :TIPO and OBJECT_ID = :IDO");
			 sqlQuery.setParameter("TIPO", DocuConstants.CODE_ZNACHENIE_JOURNAL_FILE);
			 sqlQuery.setParameter("IDO", fileId);
			 sqlQuery.executeUpdate();

			
			JPA.getUtil().commit();

			
			//----- изпращане на съобщение за презареждане ---
			
			ServletContext servletContext = (ServletContext) wsContext.getMessageContext().get(MessageContext.SERVLET_CONTEXT);
			SystemData sd = (SystemData) servletContext.getAttribute("systemData");
			
			if(sd!=null) {
				try {
					
					Notification fake = new Notification(sd);
				
					fake.generatеFakeNotif(DocuConstants.CODE_FAKE_NOTIF_RELOAD_FILES_SIGN);
				
					fake.getAdresati().add(userId);
					fake.send();
					

					System.out.println("ok2");
				} catch (Exception e) {
					LOGGER.error("Грешка при изпращане на съобщение за презареждане на файловете !", e);
				}
			}
			//------------------------------------------------
			
		} catch (Exception e) {
			LOGGER.error("Грешка при запис в базата данни !");
			JPA.getUtil().rollback();
			throw new WSFault(-1, "Грешка при работа с БД ! ", e);

		
		
		} finally {
			JPA.getUtil().closeConnection();
		}

		return "Success";

	
	}
}