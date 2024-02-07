package com.ib.docu.db.dao;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.hibernate.HibernateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.docu.archimed.Corresp;
import com.ib.docu.archimed.Document;
import com.ib.docu.archimed.DocumentFile;
import com.ib.docu.db.dto.Doc;
import com.ib.docu.db.dto.DocDvij;
import com.ib.docu.db.dto.EgovMessages;
import com.ib.docu.db.dto.EgovMessagesCoresp;
import com.ib.docu.db.dto.EgovMessagesFiles;
import com.ib.docu.db.dto.Referent;
import com.ib.docu.db.dto.Registratura;
import com.ib.docu.seos.MessageType;
import com.ib.docu.system.DocuConstants;
import com.ib.docu.system.SystemData;
import com.ib.docu.system.UserData;
import com.ib.system.ActiveUser;
import com.ib.system.BaseSystemData;
import com.ib.system.BaseUserData;
import com.ib.system.db.AbstractDAO;
import com.ib.system.db.DialectConstructor;
import com.ib.system.db.JPA;
import com.ib.system.db.SelectMetadata;
import com.ib.system.db.dao.FilesDAO;
import com.ib.system.db.dto.Files;
import com.ib.system.db.dto.SystemClassif;
import com.ib.system.exceptions.DbErrorException;
import com.ib.system.exceptions.ObjectInUseException;
import com.ib.system.utils.DateUtils;
import com.ib.system.utils.SearchUtils;



public class EgovMessagesDAO extends AbstractDAO<EgovMessages> {

	
	private static final Logger LOGGER = LoggerFactory.getLogger(EgovMessagesDAO.class);
	
	/** @param user */
	public EgovMessagesDAO(ActiveUser user) {
		super(EgovMessages.class, user);
	}
	
	public String loadZaiavlenieTest(Files f, Integer docVid, Date msgDocRnDat, BaseSystemData sd) throws DbErrorException, ObjectInUseException {
		Doc doc = new Doc();

		doc.setRegisterId(6); // Тест регистър входящи документи (18-00)
		doc.setDocDate(msgDocRnDat);
		doc.setDocVid(docVid);
		
		new DocDAO(getUser()).generateRnDoc(doc, sd);

		try {
			int msgId = nextVal("SEQ_EGOV_MESSAGES");
			int filesId = nextVal("SEQ_EGOV_MESSAGES_FILES");
			int correspId = nextVal("SEQ_EGOV_MESSAGES_CORESP");
			
			String filename = f.getFilename();
			byte[] blob = f.getContent();
			
			Query msg = JPA.getUtil().getEntityManager().createNativeQuery("INSERT INTO EGOV_MESSAGES"
					+ "(ID, MSG_GUID, SENDER_GUID, SENDER_NAME, RECIPIENT_GUID, RECIPIENT_NAME"
					+ ", MSG_TYPE, MSG_DAT, MSG_STATUS, MSG_STATUS_DAT, MSG_INOUT, MSG_VERSION, MSG_RN, MSG_RN_DAT"
					+ ", DOC_GUID, DOC_DAT, DOC_RN, DOC_VID, DOC_SUBJECT, DOC_COMMENT, COMM_STATUS"
					+ ", SENDER_EIK, RECIPIENT_EIK, SOURCE)"
					+ " VALUES(?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9, ?10, ?11, ?12, ?13, ?14, ?15, ?16, ?17, ?18, ?19, ?20, ?21, ?22, ?23, ?24)");
			msg.setParameter(1, msgId); // ID
			msg.setParameter(2, "{" + UUID.randomUUID().toString().toUpperCase() + "}"); // MSG_GUID
			msg.setParameter(3, 1554); // SENDER_GUID
			msg.setParameter(4, "ТЕСТ"); // SENDER_NAME
			msg.setParameter(5, "{77EE2AC9-4719-4063-9A52-781B78032EFA}"); // RECIPIENT_GUID
			msg.setParameter(6, "Министерство на младежта и спорта"); // RECIPIENT_NAME
			msg.setParameter(7, "MSG_DocumentRegistrationRequest"); // MSG_TYPE
			msg.setParameter(8, new Date()); // MSG_DAT
			msg.setParameter(9, "DS_WAIT_REGISTRATION"); // MSG_STATUS
			msg.setParameter(10, new Date()); // MSG_STATUS_DAT
			msg.setParameter(11, 1); // MSG_INOUT
			msg.setParameter(12, "1.0"); // MSG_VERSION
			msg.setParameter(13, doc.getRnDoc()); // MSG_RN
			msg.setParameter(14, msgDocRnDat); // MSG_RN_DAT
			msg.setParameter(15, "{" + UUID.randomUUID().toString().toUpperCase() + "}"); // DOC_GUID
			msg.setParameter(16, msgDocRnDat); // DOC_DAT
			msg.setParameter(17, doc.getRnDoc()); // DOC_RN
			msg.setParameter(18, docVid); // DOC_VID
			msg.setParameter(19, filename); // DOC_SUBJECT
			msg.setParameter(20, ""); // DOC_COMMENT
			msg.setParameter(21, 3); // COMM_STATUS
			msg.setParameter(22, ""); // SENDER_EIK
			msg.setParameter(23, "175745920"); // RECIPIENT_EIK
			msg.setParameter(24, "S_ARCHIMED"); // SOURCE
			msg.executeUpdate();
			
			
			Query files = JPA.getUtil().getEntityManager().createNativeQuery(
					"INSERT INTO EGOV_MESSAGES_FILES(ID, ID_MESSAGE, FILENAME, MIME, BLOBCONTENT) VALUES(?1, ?2, ?3, ?4, ?5)");
			files.setParameter(1, filesId); // ID
			files.setParameter(2, msgId); // ID_MESSAGE
			files.setParameter(3, filename); // FILENAME
			files.setParameter(4, f.getContentType()); // MIME
			files.setParameter(5, blob); // BLOBCONTENT
			files.executeUpdate();
			
			
			Query corresp = JPA.getUtil().getEntityManager().createNativeQuery(
					"INSERT INTO EGOV_MESSAGES_CORESP(ID, ID_MESSAGE, IME, EGN, BULSTAT, CITY, ADRES, PK, EMAIL, DOP_INFO)"
					+ " VALUES(?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9, ?10)");
			corresp.setParameter(1, correspId); // ID
			corresp.setParameter(2, msgId); // ID_MESSAGE
			corresp.setParameter(3, "ТЕСТ"); // IME
			corresp.setParameter(4, null); // EGN
			corresp.setParameter(5, null); // BULSTAT
			corresp.setParameter(6, "София"); // CITY
			corresp.setParameter(7, null); // ADRES
			corresp.setParameter(8, null); // PK
			corresp.setParameter(9, "test@mail.bg"); // EMAIL
			corresp.setParameter(10, "Correspondent: 123456789"); // DOP_INFO
			corresp.executeUpdate();
			
		} catch (Exception e) {
			throw new DbErrorException("Грешка при запис на съобщение в EGOV_MESSAGES.", e);
		} 
		return doc.getRnDoc();
	}
	
	
	/** Изгражда sql за филтъра на чакащите за регистрация документи
	 * @param guidRegistraturа - guid на регистратура 
	 * @return SelectMetadata
	 */
	public SelectMetadata createFilterEgovMessages(String guidSeos, String guidSSEV, String tehNom ,boolean fullEq){
		
		SelectMetadata smd = new SelectMetadata();
		String dialect = JPA.getUtil().getDbVendorName().toUpperCase();
		
		StringBuilder select = new StringBuilder();
		StringBuilder from = new StringBuilder();
		StringBuilder where = new StringBuilder();
		Map<String, Object> params = new HashMap<String, Object>();
		
		
				
		if (guidSeos == null && guidSSEV == null) {		
			return null;
		}
		
		ArrayList<String> guids = new ArrayList<String>();
		if (guidSeos != null) {
			guids.add(guidSeos.toUpperCase());
		}
		if (guidSSEV != null) {
			guids.add(guidSSEV.toUpperCase());
		}
				
		select.append(" SELECT DISTINCT EM.ID A1ID, EM.SENDER_NAME A2SENDERNAME, EM.MSG_REG_DAT A3MSGREGDAT, ");
		select.append(DialectConstructor.limitBigString(dialect, "EM.DOC_SUBJECT", 300) + " A4DOCSUBJECT, "); // това заменя долното
//		if (dialect.indexOf("ORACLE") != -1){
//			select.append(" dbms_lob.substr( EM.DOC_SUBJECT, 300, 1 ) || CASE  WHEN dbms_lob.getlength(EM.DOC_SUBJECT)>300  THEN '...'  END A4DOCSUBJECT,");    		
//    		
//    	}else{
//    		select.append(" EM.DOC_SUBJECT A4DOCSUBJECT, ") ;
//    	} 
		
		select.append(" EM.DOC_SROK A5DOCSROK, LOCK_OBJECTS.USER_ID A6LOCK, EM.DOC_GUID A7GUID,   EGOV_NOMENKLATURI.DESCRIPTION A8DESC, ") ;
		select.append(" EM.DOC_RN A9, EM.DOC_DAT A10 " ) ;
	    
		from.append("	FROM EGOV_MESSAGES EM");
		from.append(" 		LEFT OUTER JOIN EGOV_NOMENKLATURI on EM.SOURCE = EGOV_NOMENKLATURI.STATUS_TEKST ");
		from.append(" 		LEFT OUTER JOIN LOCK_OBJECTS ON EM.ID = LOCK_OBJECTS.OBJECT_ID AND  LOCK_OBJECTS.OBJECT_TIP = :TIP AND LOCK_OBJECTS.USER_ID <> :USERID") ;
		
		where.append(" WHERE EM.MSG_TYPE = 'MSG_DocumentRegistrationRequest' AND EM.MSG_STATUS = 'DS_WAIT_REGISTRATION'  AND EM.MSG_INOUT = 1  AND ");
		
		if (guids.size() == 2) {
			where.append("upper(EM.RECIPIENT_GUID) in (:GUIDS)");
		}else {
			where.append("upper(EM.RECIPIENT_GUID) = :GUIDS");
		}
				
				
		
		
		if (tehNom != null && !tehNom.trim().isEmpty()) {
			if(fullEq) {
				if (!tehNom.contains("-")) {
					where.append(" and em.doc_rn = '"+tehNom.trim()+"'");
				}else {
					String[] parts = tehNom.split("-");
					if (parts.length == 2) {
						where.append(" and em.doc_rn = '"+tehNom.trim()+"' or (em.doc_uri_reg = '" + parts[0].trim()+ "' and em.doc_uri_batch = '"+parts[1].trim()+"' ) ");
					}else {
						where.append(" and em.doc_rn = '"+tehNom.trim()+"'");
					}
				}
			
			} else {
				where .append(" AND upper(em.doc_rn) LIKE '%"+tehNom.trim().toUpperCase()+"%' ");
				
			}
		}
		
		
		params.put("TIP", DocuConstants.CODE_ZNACHENIE_JOURNAL_EGOVMESSAGE);
		params.put("USERID", ((UserData) getUser()).getUserAccess());
		
		if (guids.size() == 2) {
			params.put("GUIDS", guids);
		}else {
			params.put("GUIDS", guids.get(0));
		}
		
		
		smd.setSqlParameters(params);
		smd.setSql(select.toString() + from.toString() + where.toString());
		smd.setSqlCount(" select count(distinct EM.ID) " + from.toString() + where.toString());
		
		return smd;
	}
	
	/**
	 * @param idMessage
	 * @return
	 * @throws DbErrorException
	 */
	public List<EgovMessagesFiles> findFilesByMessage(Integer idMessage) throws DbErrorException {
		if (idMessage == null) {
			return new ArrayList<>();
		}
		
		try {
			@SuppressWarnings("unchecked")
			List<EgovMessagesFiles> files = createQuery(
				"select x from EgovMessagesFiles x where x.idMessage = ?1")
				.setParameter(1, idMessage).getResultList();
			
			return files;

		} catch (Exception e) {
			throw new DbErrorException("Грешка при търсене на файлове за съобщение!", e);
		}
	}
	
	
	/** Връща съобщение по guid
	 * @param guid - guid на съобщение
	 * @return
	 * @throws DbErrorException
	 */
	@SuppressWarnings("unchecked")
	public EgovMessages findMessageByGuid(String guid) throws DbErrorException {
		if (guid == null) {
			return  null;
		}
		
		try {			
			List<EgovMessages> messages = createQuery("from EgovMessages where msgGuid = :guid")
				.setParameter("guid", guid).getResultList();
			
			if (messages.size() > 0) {
				return messages.get(0);
			}else {
				return null;
			}

		} catch (Exception e) {
			throw new DbErrorException("Грешка при търсене на съобщение!", e);
		}
	}
	
	
	
	
	
	/** за справка Статус пакети за обмен СЕОС / ССЕВ
	*/
	@SuppressWarnings("unchecked")
	public ArrayList<Object[]>  createMsgTypesList() throws DbErrorException {
		
		try {		
			Query q = createNativeQuery("select STATUS_TEKST, DESCRIPTION from EGOV_NOMENKLATURI where STATUS_TEKST like 'MSG_%' order by DESCRIPTION");
			
			return (ArrayList<Object[]>) q.getResultList();
		} catch (Exception e) {
			throw new DbErrorException("Грешка при зареждане на тип на съобщения сеос!", e);
		}		
	}
	
	

	@SuppressWarnings("unchecked")
	public ArrayList<Object[]> createMsgStatusList() throws  DbErrorException{

		try {		
			Query q = createNativeQuery("select STATUS_TEKST, DESCRIPTION from EGOV_NOMENKLATURI where STATUS_TEKST like 'DS_%' order by DESCRIPTION");
			
			return (ArrayList<Object[]>) q.getResultList();
		} catch (Exception e) {
			throw new DbErrorException("Грешка при търсене на статус на съобщения сеос!", e);
		}	
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<Object[]> createCommStatusList() throws  DbErrorException{
		
		try {		
			Query q = createNativeQuery("select STATUS, DESCRIPTION from EGOV_NOMENKLATURI where status is not null order by status");
			
			return (ArrayList<Object[]>) q.getResultList();
		} catch (Exception e) {
			throw new DbErrorException("Грешка при търсене на статус на изпращане сеос!", e);
		}
		
	}
	

	@SuppressWarnings("unchecked")
	/**
	 * Връща Guid на организацията по подадено id
	 * @return
	 * @throws DbErrorException
	 */
	public String findEgovOrgGuidById(Integer id) throws DbErrorException {
		
		try {			
			List<String> org = createNativeQuery("SELECT GUID from EGOV_ORGANISATIONS where id = :IDOrg")
				.setParameter("IDOrg", id).getResultList();
			
			if (org.size() > 0) {
				return org.get(0);
			}else {
				return null;
			}

		} catch (Exception e) {
			throw new DbErrorException("Грешка при търсене на EGOV_ORGANISATIONS по id!", e);
		}		
	}
	
	@SuppressWarnings("unchecked")
	/**
	 * Връща Guid на организацията от EDELIVERY_ORGANISATIONS по подадено id
	 * @return
	 * @throws DbErrorException
	 */
	public String findEgovDeliveryOrgGuidById(Integer id) throws DbErrorException {
		
		try {			
			List<String> org = createNativeQuery("SELECT GUID from EDELIVERY_ORGANISATIONS where id = :IDOrg")
				.setParameter("IDOrg", id).getResultList();
			
			if (org.size() > 0) {
				return org.get(0);
			}else {
				return null;
			}

		} catch (Exception e) {
			throw new DbErrorException("Грешка при търсене на EDELIVERY_ORGANISATIONS по id!", e);
		}		
	}
	
	
	
	/**
	 * Заявка за справка "Статус пакети за обмен СЕОС / ССЕВ"
	 * @param sender
	 * @param recepient
	 * @param msgType
	 * @param docStatus
	 * @param sentStatus
	 * @param inOut
	 * @param dateOt
	 * @param dateDo
	 * @param source
	 * @return
	 */
	public SelectMetadata createFilterMsgSQL(String sender, String recepient, String msgType, String docStatus, Integer sentStatus, Integer inOut, Date dateOt, Date dateDo, String source, String nameSender,String nameRecepient) {
		
		String dialect = JPA.getUtil().getDbVendorName();
		
		Map<String, Object> params = new HashMap<>();
		StringBuilder whereClause = new  StringBuilder("");
		ArrayList<String> uslovia = new ArrayList<String>();
		
		String selectClause = "SELECT EGOV_MESSAGES.ID 	A0, "+
							"	MSG_TYPE			A1, " + 
				 			"  	nom3.DESCRIPTION    A2, " +	
						    " 	MSG_DAT 			A3, " +				
						    " 	SENDER_NAME 		A4, " +
						    " 	RECIPIENT_NAME 		A5, " +
						    " 	DOC_RN 		  		A6, " +
						    "  	DOC_DAT 			A7, " +						    
						    "  	MSG_RN 		 		A8, " +
						    "  	MSG_RN_DAT 			A9, " +
						    DialectConstructor.limitBigString(dialect, "DOC_SUBJECT", 300) +" A10, " +
						    " 	nom2.DESCRIPTION 	A11, " +
						    "  	nom1.DESCRIPTION    A12, " +						    
						    "  	COMM_ERRROR  		A13, " +
						    "  	PRICHINA  		    A14 ";
		
		String fromClause = " FROM " +
						    " EGOV_MESSAGES join EGOV_NOMENKLATURI nom1 on EGOV_MESSAGES.COMM_STATUS = nom1.STATUS " +
						    "               join EGOV_NOMENKLATURI nom2 on EGOV_MESSAGES.MSG_STATUS = nom2.STATUS_TEKST "+
						    "				join EGOV_NOMENKLATURI nom3 on EGOV_MESSAGES.MSG_TYPE = nom3.STATUS_TEKST ";
		
		
		if (!SearchUtils.isEmpty(sender)){
			uslovia.add("SENDER_GUID = :sender ");
			params.put("sender", sender);
		}
		
		if (!SearchUtils.isEmpty(recepient)){
			uslovia.add("RECIPIENT_GUID = :recepient ");
			params.put("recepient", recepient);
		}
		
		if (!SearchUtils.isEmpty(msgType)){
			uslovia.add("MSG_TYPE = :msgType ");
			params.put("msgType", msgType);
		}
		
		if (!SearchUtils.isEmpty(docStatus)){
			uslovia.add("MSG_STATUS = :docStatus ");
			params.put("docStatus", docStatus);
		}		
		
		if (sentStatus != null){
			uslovia.add("COMM_STATUS = :sentStatus " );
			params.put("sentStatus", sentStatus);
		}
		
		if (inOut != null){
			uslovia.add("MSG_INOUT =:inOut " );
			params.put("inOut", inOut);
		}
		
		if (dateOt != null) {
			uslovia.add("MSG_DAT >= :dateOt ");
			params.put("dateOt", DateUtils.startDate(dateOt));
		}

		if (dateDo != null) {
			uslovia.add("MSG_DAT <= :dateDo");
			params.put("dateDo", DateUtils.endDate(dateDo));
		}
		
		if (!SearchUtils.isEmpty(source)) {
			uslovia.add("SOURCE = :source ");
			params.put("source", source);
		}
		
		if (!SearchUtils.isEmpty(nameSender)) {
			uslovia.add("UPPER(TRIM(SENDER_NAME)) LIKE '%"+nameSender.trim().toUpperCase() + "%'");
		}
		
		if (!SearchUtils.isEmpty(nameRecepient)) {
			uslovia.add("UPPER(TRIM(RECIPIENT_NAME)) LIKE '%"+ nameRecepient.trim().toUpperCase()+ "%'");
		}
		
			
		if (!uslovia.isEmpty()) {
			whereClause.append(" WHERE ");
			for (int i = 0; i < uslovia.size(); i++) {
				whereClause.append(uslovia.get(i));
				if (i != (uslovia.size() - 1)) {
					whereClause.append(" AND ");
				}
			}
		}
		
		SelectMetadata sm = new SelectMetadata();

		sm.setSqlCount(" select count(*) " + fromClause + whereClause.toString());

		sm.setSql(selectClause + fromClause + whereClause.toString());
		sm.setSqlParameters(params);
		
		return sm;
	}
	
	/**
	 * Заявка за "Мониторинг на съобщения от ССЕВ"
	 * @param status
	 * @param dateOt
	 * @param dateDo
	 * @param vidDocList
	 * @return
	 */
	public SelectMetadata createFilterMonitoringSSEV(String status, Date dateOt, Date dateDo, List <Integer> vidDocDost, Integer vidDoc,BaseUserData userData, String rnDoc) {
			
		Map<String, Object> params = new HashMap<>();
		StringBuilder whereClause = new  StringBuilder("");
		ArrayList<String> uslovia = new ArrayList<String>();
		
		String selectClause = " SELECT em.id  A0, "+
				"	em.doc_vid				  A1, " + 
	 			"  	em.msg_dat    			  A2, " +	
			    "   em.doc_rn           	  A3, " +
			    "   em.doc_dat      		  A4, " +
			    "   nom.description        	  A5, " +
			    "   em.doc_subject      	  A6,  " +
			    // временно,докато се уточни кой кореспондент ще се взима. Махам го,защото вече не го подавам към екраните на обектите
//			    "   (SELECT emc.egn from egov_messages_coresp emc where emc.id in( select min(id) from egov_messages_coresp) AND emc.id_message = em.id) A7, " +
//			    "   (SELECT emc.bulstat from egov_messages_coresp emc where emc.id in( select min(id) from egov_messages_coresp) AND emc.id_message = em.id) A8, " +
			    "   em.msg_status A9, " +
			    "	z.USER_ID A10," +
			    "   z.LOCK_DATE A11 ";
				   

		String fromClause = " FROM EGOV_MESSAGES em "
		+ "join EGOV_NOMENKLATURI nom on em.msg_status = nom.STATUS_TEKST left outer join LOCK_OBJECTS z on z.OBJECT_TIP = :zTip and z.OBJECT_ID = em.id and z.USER_ID != :zUser ";
		
				
		uslovia.add("em.SOURCE = :source");
		params.put("source", "S_ARCHIMED");
		
		params.put("zTip", DocuConstants.CODE_ZNACHENIE_JOURNAL_EGOVMESSAGE);
		params.put("zUser", userData.getUserId());
			
	
		if(vidDoc!=null) {
			uslovia.add("em.doc_vid = :vidDoc " );
			params.put("vidDoc", SearchUtils.asString(vidDoc));
		}else if(vidDocDost!=null) {
			
			List <String> vidDocDostTmp = new ArrayList<String>();
			for(Integer item: vidDocDost) {
				vidDocDostTmp.add(SearchUtils.asString(item));
			}
			uslovia.add("em.doc_vid in (:vid)");
			params.put("vid", vidDocDostTmp);
		}
			
		if (status != null){
			uslovia.add("em.msg_status = :status " );
			params.put("status", status);
		}
		
		if (rnDoc != null && !"".equals(rnDoc)){
			uslovia.add("em.doc_rn = :rnDoc " );
			params.put("rnDoc", rnDoc);
		}
		
		if (dateOt != null) {
			uslovia.add("DOC_DAT >= :dateOt ");
			params.put("dateOt", DateUtils.startDate(dateOt));
		}

		if (dateDo != null) {
			uslovia.add("DOC_DAT <= :dateDo");
			params.put("dateDo", DateUtils.endDate(dateDo));
		}
							
		if (!uslovia.isEmpty()) {
			whereClause.append(" WHERE ");
			for (int i = 0; i < uslovia.size(); i++) {
				whereClause.append(uslovia.get(i));
				if (i != (uslovia.size() - 1)) {
					whereClause.append(" AND ");
				}
			}
		}
		
		SelectMetadata sm = new SelectMetadata();

		sm.setSqlCount(" select count(*) " + fromClause + whereClause.toString());

		sm.setSql(selectClause + fromClause + whereClause.toString());
		sm.setSqlParameters(params);
		
		return sm;
	}
	
	/** Създава и записва съобщение от тип MSG_DocumentRegistrationRequest

	 * @return 
	 * @throws DbErrorException 
	 */
	public void saveRegistrationRequestMessage(DocDvij dvij, SystemData sd) throws DbErrorException{
		
		EgovMessages mess = new EgovMessages();
		ArrayList<EgovMessagesFiles> files = new ArrayList<EgovMessagesFiles>();
		ArrayList<EgovMessagesCoresp> corespondenti = new ArrayList<EgovMessagesCoresp>();
		
		FilesDAO fdao = new FilesDAO(ActiveUser.DEFAULT);
		
		
		//Doc doc1 = new DocDAO(ActiveUser.DEFAULT).findById();		
		Object[] docData = new DocDAO(ActiveUser.DEFAULT).findDocDataForSeos(dvij.getDocId());
		
		
		SystemClassif item = sd.decodeItemLite(DocuConstants.CODE_CLASSIF_DOC_PREDAVANE_STATUS, DocuConstants.DS_WAIT_SENDING, DocuConstants.CODE_DEFAULT_LANG, new Date(), false);
		String status = item.getCodeExt();
		
		//ИНИЦИАЛИЗАЦИЯ
		mess.setMsgInOut(2); //Изходящо		
		mess.setMsgRegDate(new Date());
		mess.setMsgRn(null);
		mess.setMsgRnDate(null);		
		mess.setMsgStatus(status);		
		mess.setMsgStatusDate(new Date());
		mess.setMsgType(MessageType.MSG_DOCUMENT_REGISTRATION_REQUEST.value());
		mess.setMsgUrgent(1);
		mess.setMsgDate(new Date());
		mess.setMsgGuid("{"+java.util.UUID.randomUUID().toString().toUpperCase()+"}");
		
		mess.setRecepientEik(dvij.getUchastnikIdent());
		mess.setRecepientGuid(dvij.getUchastnikGuid());
		mess.setRecepientName(dvij.getUchastnikName());
		
		
		mess.setSenderGuid(SearchUtils.asString(docData[6]));
		mess.setSenderEik (SearchUtils.asString(docData[8]));
		mess.setSenderName(SearchUtils.asString(docData[9]));
				
		
		//Основни данни за документа
		mess.setMsgComment("N/A");
		mess.setDocComment (null);
		mess.setDocNasochen(null);
		
		String vidDoc = sd.decodeItem(DocuConstants.CODE_CLASSIF_DOC_VID, SearchUtils.asInteger(docData[1]), DocuConstants.CODE_DEFAULT_LANG, SearchUtils.asDate(docData[4]));				
		if (vidDoc == null || vidDoc.trim().isEmpty()) {
			vidDoc = "Писмо";
		}
		mess.setDocVid(vidDoc);
		mess.setDocSubject (SearchUtils.asString(docData[0]));
		mess.setDocSrok(null);
		mess.setDocGuid(SearchUtils.asString(docData[2]));
		mess.setDocRn(SearchUtils.asString(docData[3]));
		mess.setDocDate(SearchUtils.asDate(docData[4]));
		mess.setCommStatus(1);
		mess.setSource("S_SEOS");
		//
		
		if (docData[5] != null) {
			
			Referent coresp = new ReferentDAO(ActiveUser.DEFAULT).findByCode(SearchUtils.asInteger(docData[5]), SearchUtils.asDate(docData[4]), false );
			
			
			EgovMessagesCoresp tek = new EgovMessagesCoresp();
			if (coresp.getAddress() != null) {
				if (coresp.getAddress().getEkatte() != null){
					
					String  mesto = sd.decodeItem(DocuConstants.CODE_CLASSIF_EKATTE, coresp.getAddress().getEkatte(), DocuConstants.CODE_DEFAULT_LANG, new Date());
					
					if (mesto != null){
						tek.setCity(mesto);
					}else{
						tek.setCity("Няма въведено!");
					}
						
					
				}else{
					tek.setCity("Няма въведено!");
				}
				
				tek.setAdres(coresp.getAddress().getAddrText());
				tek.setPk(coresp.getAddress().getPostCode());
			}
			
			
			
			tek.setBulstat(coresp.getNflEik());				
			tek.setDopInfo(coresp.getRefInfo());
			tek.setEgn(coresp.getFzlEgn());
			tek.setEmail(coresp.getContactEmail());
			tek.setIme(coresp.getRefName());
			tek.setPhone(coresp.getContactPhone());
			
			corespondenti.add(tek);
			
			
		}	
		
		
		// Взимат се само файловете, които са маркирани за изпращане
		String SELECT_BY_FILE_OBJECT_SQL =
				"select f"
				+ " from Files f"
				+ " inner join FileObject fo on fo.fileId = f.id"
				+ " where fo.objectId = :objectIdArg and fo.objectCode = :objectCodeArg and fo.official = :isOfficial";

		TypedQuery<Files> typedQuery = getEntityManager().createQuery(SELECT_BY_FILE_OBJECT_SQL, Files.class);
		typedQuery.setParameter("objectIdArg", dvij.getDocId());
		typedQuery.setParameter("objectCodeArg", DocuConstants.CODE_ZNACHENIE_JOURNAL_DOC);
		typedQuery.setParameter("isOfficial", DocuConstants.CODE_ZNACHENIE_DA);
		List<Files> filesList = typedQuery.getResultList();
		
		if (filesList != null && filesList.size() > 0) {
			
			for (Files att : filesList){
				
				EgovMessagesFiles file = new EgovMessagesFiles();
				file.setFilename(att.getFilename());		        
		        file.setMime(att.getContentType());
		        
		        if (att.getContent() == null) {
		        	att = fdao.findById(att.getId()) ;
		        }
		        
		        file.setBlobcontent(att.getContent());
		        
		        files.add(file);
			}
			
		}
			
		
		
		try {
			
			
			
			save(mess);
			
			if (corespondenti != null && corespondenti.size() == 1) { 
				EgovMessagesCoresp egovCoresp = corespondenti.get(0);
				egovCoresp.setIdMessage(mess.getId());
				JPA.getUtil().getEntityManager().persist(egovCoresp);
			}
			
			
			for (EgovMessagesFiles att : files){				
				att.setIdMessage(mess.getId());				
		        JPA.getUtil().getEntityManager().persist(att);
			}
			
			
			JPA.getUtil().getEntityManager().createNativeQuery("update DOC_DVIJ set MESSAGE_ID = :mid where ID = :IDD").setParameter("mid", mess.getId()).setParameter("IDD", dvij.getId()).executeUpdate();
			
			
		
		}catch (Exception e) {
			LOGGER.error("Грешка изграждане на СЕОС съобщение!", e);			
			throw new DbErrorException("Грешка изграждане на СЕОС съобщение!", e);
		}
		
		
		
		
	}
	
	
	
	
	
	@SuppressWarnings("unchecked")
	/*
	 * Проверка за вече регистриран докумет с този GUID
	 */
	public Object[] isDblGuid(String guid, Integer idRegistratura)
			throws DbErrorException {

		try {

			String sqlString = " select DOC_ID,  RN_DOC,  DOC_DATE FROM DOC where  GUID = :GUIDC and REGISTRATURA_ID = :IDR";

			
			Query q = JPA.getUtil().getEntityManager().createNativeQuery(sqlString);				
			
			q.setParameter("GUIDC", guid);
			q.setParameter("IDR", idRegistratura);
			
			ArrayList<Object[]> rez = (ArrayList<Object[]>) q.getResultList();

			if (rez.size() > 0) {
				return rez.get(0);

			} else {
				return null;
			}

		} catch (HibernateException e) {

			throw new DbErrorException(
					"Грешка при проверка за дублиран GUID !");

		}

	}
	
	
	/** Създава и записва съобщение за искане на нов статус на движението/изпращането

	 * @return 
	 * @throws DbErrorException 
	 */
	public void saveStatusRequestMessage(DocDvij dvij, SystemData sd) throws DbErrorException{
		
			EgovMessages incommingMess = null;
		
			if (dvij.getMessageId() == null) {
				throw new DbErrorException("Движението няма записанo id на съобщение !");
			}else {
				incommingMess = findById(dvij.getMessageId());
				if (incommingMess == null) {
					throw new DbErrorException("Не се намира изпратено съобщение по id= "+dvij.getMessageId()+" !");
				}
			}
			
		
			
			EgovMessages mess = new EgovMessages();
			
			
			
			//ИНИЦИАЛИЗАЦИЯ
			mess.setMsgInOut(1);		
			mess.setMsgRegDate(new Date());
			mess.setMsgRn(null);
			mess.setMsgRnDate(null);			
			mess.setMsgStatusDate(new Date());
			mess.setMsgType(MessageType.MSG_DOCUMENT_STATUS_REQUEST.value());
			mess.setMsgUrgent(1);
			mess.setCommStatus(1);
			mess.setMsgStatus("DS_WAIT_SENDING");
			
			mess.setMsgDate(new Date());
			mess.setMsgGuid("{"+java.util.UUID.randomUUID().toString().toUpperCase()+"}");
			
			mess.setRecepientGuid(incommingMess.getRecepientGuid());
			mess.setRecepientEik(incommingMess.getRecepientEik());
			mess.setRecepientName(incommingMess.getRecepientName());
						
			mess.setSenderGuid(incommingMess.getSenderGuid());			
			mess.setSenderEik(incommingMess.getSenderEik());
			mess.setSenderName(incommingMess.getSenderName());
			
			mess.setDocGuid(incommingMess.getDocGuid());
			mess.setDocDate(incommingMess.getDocDate());
			mess.setDocUriBtch(incommingMess.getDocUriBtch());
			mess.setDocUriReg(incommingMess.getDocUriReg());
			mess.setDocRn(incommingMess.getDocRn());
			
			mess.setSource("S_SEOS");
				
			save(mess);
				
					
	}
	
	
	/** Изпраща съобщението отново
	 * @param idMessage
	 * @throws DbErrorException
	 */
	public void resetOutgoingMessages(Integer messageId) throws DbErrorException{

		try {

			
			
			JPA.getUtil().getEntityManager().createNativeQuery("update EGOV_MESSAGES set COMM_STATUS = 1, COMM_ERRROR = null where ID  = :mId").setParameter("mId", messageId).executeUpdate();
			
			
		} catch (Exception e) {
			throw new DbErrorException("Грешка при ресет на изходящо съобщение !", e);
		}
	}
	
	
	
	/** Записва отказ на съобщението - за СЕОС
	 * @param incommingMess - съобщението, което се отказва
	 * @param prichina - причина за отказа
	 * @param ud
	 * @throws DbErrorException
	 */
	public void saveStatusResponseOtkazMessage(EgovMessages incommingMess, String prichina, UserData ud) throws DbErrorException{
		
		if (incommingMess == null) {
			throw new DbErrorException("Съобщението, на което трябва да се отговори е null");
		}
		
		
		EgovMessages mess = new EgovMessages();
		
		mess.setCommError(null);
		mess.setCommStatus(1);		
		
		mess.setDocGuid(incommingMess.getDocGuid());
		
		mess.setMsgDate(new Date());
		mess.setMsgGuid("{"+java.util.UUID.randomUUID().toString().toUpperCase()+"}");
		mess.setMsgInOut(2);
		mess.setMsgRegDate(new Date());
		mess.setMsgStatusDate(new Date());
		mess.setMsgType(MessageType.MSG_DOCUMENT_STATUS_RESPONSE.value());
		mess.setMsgUrgent(1);
		mess.setMsgVersion("1");
		mess.setMsgXml(null); 
		mess.setMsgComment("Отказан от " +  ud.getLiceNames());
		mess.setMsgStatus("DS_REJECTED");
		mess.setPrichina(prichina);
			
		
		mess.setRecepientName(incommingMess.getSenderName());
		mess.setRecepientEik(incommingMess.getSenderEik());
		mess.setRecepientGuid(incommingMess.getSenderGuid());
		
		mess.setSenderEik(incommingMess.getRecepientEik());
		mess.setSenderGuid(incommingMess.getRecepientGuid());
		mess.setSenderName(incommingMess.getRecepientName());
		
		mess.setUserCreated(ud.getUserId());
		
		mess.setSource("S_SEOS");
		
		save(mess);
		
	}

	/**
	 * Записва отговор на съобщението, че е регистрирано - за СЕОС
	 * @param incommingMess - на което трябва да се отговори 
	 * @param rnDoc - номера на регистрирания в деловодството документ
	 * @param datDoc -дата да деловоден док.
	 * @param ud
	 * @throws DbErrorException
	 */
	public void saveStatusResponseRegisteredMessage(EgovMessages incommingMess, String rnDoc, Date datDoc, UserData ud) throws DbErrorException{
		
		if (incommingMess == null) {
			throw new DbErrorException("Съобщението, на което трябва да се отговори е null");
		}
		
		
		EgovMessages mess = new EgovMessages();
		
		mess.setCommError(null);
		mess.setCommStatus(1);		
		
		mess.setDocGuid(incommingMess.getDocGuid());
		
		mess.setMsgDate(new Date());
		mess.setMsgGuid("{"+java.util.UUID.randomUUID().toString().toUpperCase()+"}");
		mess.setMsgInOut(2);
		mess.setMsgRegDate(new Date());
		mess.setMsgStatusDate(new Date());
		mess.setMsgType(MessageType.MSG_DOCUMENT_STATUS_RESPONSE.value());
		mess.setMsgUrgent(1);
		mess.setMsgVersion("1");
		mess.setMsgXml(null); 
		mess.setMsgComment("регистриран от " +  ud.getLiceNames());
		mess.setMsgStatus("DS_REGISTERED");
		mess.setDocRn(rnDoc);
		mess.setDocDate(datDoc);
			
		
		mess.setRecepientName(incommingMess.getSenderName());
		mess.setRecepientEik(incommingMess.getSenderEik());
		mess.setRecepientGuid(incommingMess.getSenderGuid());
		
		mess.setSenderEik(incommingMess.getRecepientEik());
		mess.setSenderGuid(incommingMess.getRecepientGuid());
		mess.setSenderName(incommingMess.getRecepientName());
		
		mess.setUserCreated(ud.getUserId());
		
		mess.setSource("S_SEOS");
		
		save(mess);
		
	}
	
	
	/**Търси свързани документи дошли по ел. връчване
	 * @param docId ид на документ
	 * @param guid гуид на регистратурата във връчването
	 * @return
	 * @throws DbErrorException
	 */
	@SuppressWarnings("unchecked")
	public List<Object[]> getAllDocCoresps(Long docId, String guid) throws DbErrorException {
		
		if (guid == null) {
			return new ArrayList<Object[]>();
		}
		
		
		String sqlString = 	" select msg_guid, SENDER_NAME, doc.RN_DOC, doc.DOC_DATE from EGOV_MESSAGES, doc, DOC_DOC where EGOV_MESSAGES.DOC_GUID = doc.GUID and doc.doc_id = doc_doc.doc_id1 and doc_doc.doc_id2 = :IDD and EGOV_MESSAGES.SOURCE = :SRC and upper(RECIPIENT_GUID) = :GUID" + 
				" union " + 
						" select msg_guid, SENDER_NAME, doc.RN_DOC, doc.DOC_DATE from EGOV_MESSAGES, doc, DOC_DOC where EGOV_MESSAGES.DOC_GUID = doc.GUID and doc.doc_id = doc_doc.doc_id2 and doc_doc.doc_id1 = :IDD and EGOV_MESSAGES.SOURCE = :SRC and upper(RECIPIENT_GUID) = :GUID" + 
				" union " + 
						" select EGOV_MESSAGES.MSG_GUID, SENDER_NAME, doc.RN_DOC, doc.DOC_DATE from doc, EGOV_MESSAGES, DELO_DOC where doc.GUID = EGOV_MESSAGES.DOC_GUID and doc.DOC_ID = DELO_DOC.INPUT_DOC_ID   and  DELO_ID in ( select DELO_ID from DELO_DOC where INPUT_DOC_ID = :IDD ) and doc.DOC_ID <> :IDD  and EGOV_MESSAGES.SOURCE = :SRC and upper(RECIPIENT_GUID) = :GUID";
		
		try {

			
			
			Query query = JPA.getUtil().getEntityManager().createNativeQuery(sqlString);
			
			query.setParameter("IDD", docId);
			query.setParameter("SRC", "S_EDELIVERY");
			query.setParameter("GUID", guid.trim().toUpperCase());
			
			return query.getResultList();
			
			
		} catch (HibernateException e) {
			throw new DbErrorException("Грешка търсене на кореспонденти !", e);
		}
		
		
		
	}

	
	
	/** Създава и записва обект от за връчваето в БД	
	 * @throws DbErrorException 
	 */
	public void saveNewSSEVMessage(DocDvij dvij, SystemData sd) throws DbErrorException {
		
		
		ArrayList<EgovMessagesFiles> files = new ArrayList<EgovMessagesFiles>();
		FilesDAO fdao = new FilesDAO(ActiveUser.DEFAULT);
		
		
		Object[] docData = new DocDAO(ActiveUser.DEFAULT).findDocDataForSeos(dvij.getDocId());
		
		
		//ИНИЦИАЛИЗАЦИЯ
		EgovMessages mess = new EgovMessages();
		
		mess.setSenderGuid(SearchUtils.asString(docData[7]));
		mess.setSenderEik (SearchUtils.asString(docData[8]));
		mess.setSenderName(SearchUtils.asString(docData[9]));
		
		mess.setRecepientType(dvij.getUchastnikType());
		mess.setRecepientName(dvij.getUchastnikName());
		if (mess.getRecepientType().equals("LegalPerson") || mess.getRecepientType().equals("Institution") || mess.getRecepientType().equals("Person")) {
			mess.setRecepientEik(dvij.getUchastnikIdent());
		}else {
			if (mess.getRecepientType().equals("Reply")){
				mess.setReplyIdent(dvij.getUchastnikIdent());
			}
		}
		
		
		String vidDoc = sd.decodeItem(DocuConstants.CODE_CLASSIF_DOC_VID, SearchUtils.asInteger(docData[1]), DocuConstants.CODE_DEFAULT_LANG, SearchUtils.asDate(docData[4]));				
		if (vidDoc == null || vidDoc.trim().isEmpty()) {
			vidDoc = "Писмо";
		}
		mess.setDocVid(vidDoc);
		
		
		
		
		
		mess.setMsgType("MSG_DocumentRegistrationRequest");
		mess.setMsgDate(new Date());
		mess.setMsgStatus("DS_WAIT_SENDING");
		mess.setMsgStatusDate(new Date());
		mess.setDocGuid(SearchUtils.asString(docData[2]));
		mess.setDocDate(new Date());		
		mess.setDocSubject (SearchUtils.asString(docData[0]));
		
		mess.setMsgXml(null);
		mess.setCommStatus(1);
		mess.setSource("S_EDELIVERY");
		mess.setMsgInOut(2);		
		mess.setDocRn(SearchUtils.asString(docData[3]));
		mess.setDocDate(SearchUtils.asDate(docData[4]));
		
		// Взимат се само файловете, които са маркирани за изпращане
		String SELECT_BY_FILE_OBJECT_SQL =
				"select f"
				+ " from Files f"
				+ " inner join FileObject fo on fo.fileId = f.id"
				+ " where fo.objectId = :objectIdArg and fo.objectCode = :objectCodeArg and fo.official = :isOfficial";

		TypedQuery<Files> typedQuery = getEntityManager().createQuery(SELECT_BY_FILE_OBJECT_SQL, Files.class);
		typedQuery.setParameter("objectIdArg", dvij.getDocId());
		typedQuery.setParameter("objectCodeArg", DocuConstants.CODE_ZNACHENIE_JOURNAL_DOC);
		typedQuery.setParameter("isOfficial", DocuConstants.CODE_ZNACHENIE_DA);
		List<Files> filesList = typedQuery.getResultList();

		if (filesList != null && filesList.size() > 0) {
			
			for (Files att : filesList){
				
				EgovMessagesFiles file = new EgovMessagesFiles();
				file.setFilename(att.getFilename());		        
		        file.setMime(att.getContentType());
		        
		        if (att.getContent() == null) {
		        	att = fdao.findById(att.getId()) ;
		        }
		        
		        file.setBlobcontent(att.getContent());
		        
		        files.add(file);
			}
			
		}
			
		
		
		try {
			
			
			
			save(mess);
			
			for (EgovMessagesFiles att : files){				
				att.setIdMessage(mess.getId());				
		        JPA.getUtil().getEntityManager().persist(att);
			}
			
			
			JPA.getUtil().getEntityManager().createNativeQuery("update DOC_DVIJ set MESSAGE_ID = :mid where ID = :IDD").setParameter("mid", mess.getId()).setParameter("IDD", dvij.getId()).executeUpdate();
			
			
		
		}catch (Exception e) {
			LOGGER.error("Грешка изграждане на ССЕВ съобщение!", e);			
			throw new DbErrorException("Грешка изграждане на ССЕВ съобщение!", e);
		}
		
	}
	
	
	/** Извлича получената грешка от съобщение 
	 * @param messageId - ид на съобщение
	 *
	 * @return грешката
	 * @throws DbErrorException
	 */
	
	public String getMessageError(Integer messageId) throws DbErrorException {
		
		if (messageId == null) {
			return null;
		}
		
		
		String sqlString = 	"select COMM_ERRROR from EGOV_MESSAGES where id = :MID";
		
		try {

			
			
			Query query = JPA.getUtil().getEntityManager().createNativeQuery(sqlString);
			
			query.setParameter("MID", messageId);
			
			
			return SearchUtils.asString(query.getSingleResult());
			
			
		} catch (HibernateException e) {
			throw new DbErrorException("Грешка търсене на кореспонденти !", e);
		}
		
		
		
	}
	
	/**
	 * Записва съобщение за успешна регистрация по еDelivery	
	 * @param incommingMess - на което трябва да се отговори 
	 * @param rnDoc -  номера на регистрирания в деловодството документ
	 * @param datDoc - дата да деловоден док.
	 * @param vidDoc - вида да деловоден док.
	 * @param bodyTextPlain - текста на самото съобщение
	 * @param subject 
	 * @param sd
	 * @param ud
	 * @throws DbErrorException
	 */
	public void saveDeliverySuccessMess(EgovMessages incommingMess, String rnDoc, Date datDoc, Integer vidDoc, String bodyTextPlain, String subject, SystemData sd, UserData ud) throws DbErrorException {

		if (incommingMess == null) {
			throw new DbErrorException("Съобщението, на което трябва да се отговори е null");
		}

		EgovMessages mess = new EgovMessages();		
		
		mess.setSenderEik(incommingMess.getRecepientEik());
		mess.setSenderGuid(incommingMess.getRecepientGuid());
		mess.setSenderName(incommingMess.getRecepientName());
		
		mess.setRecepientName("Unknown");
		mess.setRecepientEik("Unknown");
		
		mess.setRecepientType("Reply");
		mess.setReplyIdent(incommingMess.getMsgGuid());
					
		mess.setMsgType("MSG_DocumentRegistrationRequest");
		mess.setMsgDate(new Date());
		mess.setMsgStatus("DS_WAIT_SENDING");
		mess.setMsgStatusDate(new Date());
		mess.setDocDate(new Date());		
		mess.setDocGuid(incommingMess.getDocGuid());			
		mess.setCommStatus(1);
		mess.setSource("S_EDELIVERY");
		mess.setMsgInOut(2);		
		mess.setDocRn(rnDoc);
		mess.setDocDate(datDoc);
		String vidDocStr = null;
		try {
			vidDocStr = sd.decodeItem(DocuConstants.CODE_CLASSIF_DOC_VID, vidDoc, DocuConstants.CODE_DEFAULT_LANG, new Date());
		} catch (DbErrorException e) {
			vidDocStr = "Писмо";
		}
		if (vidDocStr == null || vidDocStr.trim().isEmpty()) {
			vidDocStr = "Писмо";
		}
		mess.setDocVid(vidDocStr);
		mess.setMsgXml(bodyTextPlain);
		mess.setDocSubject(subject);
		
		mess.setUserCreated(ud.getUserId());

		save(mess);
	}
	
	public void saveFromArchimed(Document document, Integer docVid, Corresp corresp1, Corresp corresp2, List<Corresp> correspOthers, List<DocumentFile> files, SystemData sd) throws DbErrorException {
		
		
		EgovMessages mess = new EgovMessages();
		
		
		Corresp first = corresp1;
		if (first == null) {
			first = corresp2;
		}
		if (first == null && correspOthers.size() > 0) {
			first = correspOthers.get(0);
		}
		
		
		
		mess.setDocSubject(document.getDescription());
		
		mess.setMsgGuid("{"+java.util.UUID.randomUUID().toString().toUpperCase()+"}");
		
		if (document.getUri() != null) {
			
			mess.setDocRn(document.getUri().getNumber());
			if (document.getUri().getDate() != null ) {				
					try {
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
						mess.setDocDate(sdf.parse(document.getUri().getDate()));
					} catch (Exception e) {
						LOGGER.error("Грешка при конвертиране на дата на документ от Аркимед!",e);
					}
			}	
		}
		
		if (docVid == null) {
			if (document.getType() != null) {
				mess.setDocVid(document.getType().getName());
			}
		}else {
			mess.setDocVid(""+docVid);
		}
		
		
			
		mess.setDocGuid(document.getGuid());	
		
		if (first != null) {
			
			if (first.getVatNumber() != null  && !first.getVatNumber().isEmpty()) {
				mess.setSenderEik(first.getVatNumber());
			}else {
				if (first.getIdentifier() != null) {
					mess.setSenderEik(getUnitName());
				}else {
					
					mess.setSenderEik("N/A");
				}
			}
						
			mess.setSenderGuid(first.getId());
			mess.setSenderName(first.getName());
		}
		
		
		List<Registratura> allRegs = new RegistraturaDAO(getUser()).findAll();
		if (allRegs.size() > 0) {
			mess.setRecepientEik(allRegs.get(0).getOrgEik());
			mess.setRecepientGuid(allRegs.get(0).getGuidSeos());
			mess.setRecepientName(allRegs.get(0).getOrgName());
		}
		
		
		if (document.getDeadline() != null) {
			try {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
				mess.setDocSrok(sdf.parse(document.getDeadline()));
				
			} catch (Exception e) {
				LOGGER.error("Грешка при конвертиране на срок на документ от Аркимед!",e);
			}
		}
		
		
		mess.setMsgType("MSG_DocumentRegistrationRequest");
		mess.setMsgInOut(1);
		mess.setMsgStatus("DS_WAIT_REGISTRATION");
		mess.setMsgStatusDate(new Date());
		mess.setMsgVersion("1.0");
		mess.setSource("S_ARCHIMED");
		mess.setCommStatus(3);
		
		
		
		//Тук не знам какво
		mess.setMsgUrgent(null);
		mess.setDocComment(null);
		mess.setDocNasochen(null);
		
		
		
		mess = save(mess);
		
		
		//Кореспонденти
		if (corresp1 != null) {
			EgovMessagesCoresp c = creteEgovCorrespFromArchimed(corresp1);
			if (!SearchUtils.isEmpty(document.getCorrespondentDescription())) {
				c.setIme(c.getIme() + " - " + document.getCorrespondentDescription());
			}
			c.setIdMessage(mess.getId());
			
			JPA.getUtil().getEntityManager().persist(c);
		}
		
		if (corresp2 != null) {
			
			EgovMessagesCoresp c = creteEgovCorrespFromArchimed(corresp2);
			c.setIdMessage(mess.getId());
			
			JPA.getUtil().getEntityManager().persist(c);
		}
		
		
		if (correspOthers != null) {
			for (Corresp correspOther : correspOthers) {
				EgovMessagesCoresp c = creteEgovCorrespFromArchimed(correspOther);
				c.setIdMessage(mess.getId());
				
				JPA.getUtil().getEntityManager().persist(c);
			}
		}
		
		
		
		if (files != null) {
			for (DocumentFile file : files) {
				EgovMessagesFiles f = new EgovMessagesFiles();
				f.setBlobcontent(file.getFileContent());
				f.setFilename(file.getFileName());
				f.setIdMessage(mess.getId());
				f.setMime(file.getContentType());
				
				JPA.getUtil().getEntityManager().persist(f);
			}
		}
		
		
		
		
		
		
		
		
		
		
		
	}
	
	
	private EgovMessagesCoresp creteEgovCorrespFromArchimed(Corresp corresp) {
		
		if (corresp != null) {
			EgovMessagesCoresp c = new EgovMessagesCoresp();
			
			//c.setIdMessage(mess.getId());
			
			c.setAdres(corresp.getDescription());
			c.setBulstat(corresp.getVatNumber());			
			c.setCity(corresp.getLocality());
			c.setDopInfo("Code: " + corresp.getCode());			
			c.setEmail(corresp.getEmail());
			//c.setIdCard(getUnitName())
			c.setIme(corresp.getName());
			//c.setMobilePhone(getUnitName())
			c.setPhone(corresp.getPhoneNumber());
			c.setPk(corresp.getPostalCode());
			
			if (corresp.getIdentifier() != null && corresp.getType() != null && corresp.getType().name() != null) {
				if (corresp.getType().name().equals("Pfn")) {
					c.setEgn(corresp.getIdentifier().getValue());
				}else if (corresp.getType().name().equals("Uic")) {
					c.setBulstat(corresp.getIdentifier().getValue());
				}else {
					c.setDopInfo(c.getDopInfo() + "\r\n" + corresp.getType().name() + ": " + corresp.getIdentifier().getValue());
				}
			}
			
			return c;
			
		}else{
			return null;
		}
		
	}
	
	public EgovMessagesCoresp findCorespByIdMessage(Integer idMessage) {
		EgovMessagesCoresp resultCoresp = null;
		Query q = JPA.getUtil().getEntityManager().createQuery("select sf from EgovMessagesCoresp sf where sf.idMessage = :idMessage ").setParameter("idMessage", idMessage);
		if(q.getResultList() != null && q.getResultList().size() > 0) {
			for(int i = 0; i < q.getResultList().size() ; i++) {
				EgovMessagesCoresp tmpCoresp = (EgovMessagesCoresp) q.getResultList().get(i);
				if(tmpCoresp != null && tmpCoresp.getBulstat() != null) {
					resultCoresp = (EgovMessagesCoresp) q.getResultList().get(i);		
				}
			}
		}
		return resultCoresp;
	}
	
	
	
	
}
