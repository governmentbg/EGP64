package com.ib.docu.db.dao;

import static com.ib.system.utils.SearchUtils.asInteger;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.ParameterMode;
import javax.persistence.Query;
import javax.persistence.StoredProcedureQuery;

import org.hibernate.jpa.TypedParameterValue;
import org.hibernate.type.StandardBasicTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.docu.db.dto.MMSChlenstvo;
import com.ib.docu.db.dto.MMSVpisvane;
import com.ib.docu.db.dto.MMSVpisvaneDoc;
import com.ib.docu.system.DocuConstants;
import com.ib.system.ActiveUser;
import com.ib.system.db.AbstractDAO;
import com.ib.system.db.JPA;
import com.ib.system.db.SelectMetadata;
import com.ib.system.exceptions.DbErrorException;
import com.ib.system.exceptions.InvalidParameterException;
import com.ib.system.exceptions.ObjectInUseException;
import com.ib.system.utils.SearchUtils;

/**
 * DAO for {@link MMSVpisvane}
 *
 * @author dessy
 */
public class MMSVpisvaneDAO extends AbstractDAO<MMSVpisvane> {

	/**  */
	private static final Logger LOGGER = LoggerFactory.getLogger(MMSVpisvaneDAO.class);

	/** @param user */
	public MMSVpisvaneDAO(ActiveUser user) {
		super(MMSVpisvane.class, user);
	}
	
	/**
	 * Изтрива mms_vpisvane + mms_vpisvane_doc и данните за док само ако не се използва в други вписвания
	 * 
	 * @param entity
	 * @throws DbErrorException
	 * @throws ObjectInUseException
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void delete(MMSVpisvane entity) throws DbErrorException, ObjectInUseException {
//		Set<Integer> docIdSet = new HashSet<>();
		try {
			// mms_vpisvane_doc
			List<MMSVpisvaneDoc> listVpisvaneDoc = createQuery("select t from MMSVpisvaneDoc t where t.idVpisvane = ?1")
				.setParameter(1, entity.getId()).getResultList();
			
			MMSVpisvaneDocDAO daoVpisvaneDocDAO = new MMSVpisvaneDocDAO(getUser());
			for (MMSVpisvaneDoc t : listVpisvaneDoc) {
//				docIdSet.add(t.getIdDoc());
				
				daoVpisvaneDocDAO.delete(t);
			}
		} catch (Exception e) {
			 throw new DbErrorException("Грешка при изтриване на свързани обекти за вписване!", e);
		}
		
		// mms_vpisvane
		super.delete(entity);
		
//		if (docIdSet.isEmpty()) {
//			return;
//		}
//		// това с документите трябва да е след като се изтрие вписването !!!
//		
//		// doc, files, file_objects
//		DocDAO docDao = new DocDAO(getUser());
//		FilesDAO filesDao = new FilesDAO(getUser());
//		for (Integer docId : docIdSet) {
//			try {
//				List<Object> rows = createNativeQuery("select id from mms_vpisvane_doc where id_doc = ?1").setParameter(1, docId).setMaxResults(1).getResultList();
//				if (!rows.isEmpty()) {
//					continue; // няма как да се трие този док
//				}
//			} catch (Exception e) {
//				throw new DbErrorException("Грешка при търсене на данни за документ!", e);
//			}
//			docDao.deleteById(docId);
//			
//			List<Files> listFiles = filesDao.selectByFileObject(docId, DocuConstants.CODE_ZNACHENIE_JOURNAL_DOC);
//			for (Files delme : listFiles) {
//				filesDao.deleteFileObject(delme);
//			}
//		}
	}

	/** Метод за връщане на вписванията към обект
	 * @param codeObject - Код на обект
	 * @param idObject - ИД на обект
	 * @return Списък вписвания
	 * @throws DbErrorException - грешка при работа с БД
	 */
	@SuppressWarnings("unchecked")
	public List<MMSVpisvane> findRegsListByIdAndType(Integer typeObject, Integer idObject) throws DbErrorException{
		
		try {
			
			String sql = " SELECT V FROM MMSVpisvane V WHERE V.typeObject = :TO AND V.idObject = :IO ORDER BY V.dateStatusZaiavlenie DESC ";	
			
			Query q = JPA.getUtil().getEntityManager().createQuery(sql);
			q.setParameter("TO", typeObject);
			q.setParameter("IO", idObject);
			
			return q.getResultList();
		
		} catch (Exception e) {
			LOGGER.debug("Грешка при извличане на списък с вписванията към обект по ид на обект", e);
			throw new DbErrorException("Грешка при извличане на списък с вписванията към обект по ид на обект - " + e.getLocalizedMessage(), e);
		}
	}	
	
	/**
	 * Връща всички документи от таблица mms_vpisvane_doc по ид на обект и код на обект
	 * 
	 * допълнително за треньорите </br>
	 * [8]-status_result_zaiavlenie - тегли се само за заявл. за вписване </br>
	 * [9]-date_status_zaiavlenie - тегли се само за заявл. за вписване </br> 
	 * [10]-vid_sport - тегли се само за удост. док </br>
	 * [11]-dlajnost - тегли се само за удост. док </br>
	 * 
	 * допълнително за спортните обекти </br>
	 * [8]-status_result_zaiavlenie - тегли се само за заявл. за вписване </br>
	 * [9]-date_status_zaiavlenie - тегли се само за заявл. за вписване </br> 
	 * 
	 * @param idObj = id на обекта, за който има вписване
	 * @param codeObj = код на обекта, за който има вписване( например DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_FORMATIONS, DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_OBJECTS и т.н.)
	 * @return
	 * @throws DbErrorException
	 */
	public SelectMetadata findDocsList(Integer idObj, Integer codeObj) throws DbErrorException {
		Map<String, Object> params = new HashMap<>();
		StringBuilder select = new StringBuilder();
		StringBuilder from = new StringBuilder();
		StringBuilder where = new StringBuilder();
		
		try {
			select.append(" select d.doc_vid, d.rn_doc, d.doc_date, d.otnosno, d.doc_id, d.status, d.status_date ");
			select.append(" , vp.id a7 ");
			
			// новите колони в момента ще се теглят само за треньорите
			if (codeObj != null && codeObj.equals(DocuConstants.CODE_ZNACHENIE_JOURNAL_COACHES)) {
				select.append(" , case when d.doc_type = 1 and d.rn_doc = vp.rn_doc_zaiavlenie then vp.status_result_zaiavlenie else null end a8 ");
				select.append(" , case when d.doc_type = 1 and d.rn_doc = vp.rn_doc_zaiavlenie then vp.date_status_zaiavlenie else null end a9 ");
			
				int t = DocuConstants.CODE_ZNACHENIE_JOURNAL_COACHES; // да не разтегли много нещата в селекта
				select.append(" , case when vpd.type_object = "+t+" and d.doc_type = 2 then vp.vid_sport else null end a10 ");
				select.append(" , case when vpd.type_object = "+t+" and d.doc_type = 2 then vp.dlajnost else null end a11 ");
			}

			// иска се и това за спортните обекти
			if (codeObj != null && codeObj.equals(DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_OBJECTS)) {
				select.append(" , case when d.doc_type = 1 and d.rn_doc = vp.rn_doc_zaiavlenie then vp.status_result_zaiavlenie else null end a8 ");
				select.append(" , case when d.doc_type = 1 and d.rn_doc = vp.rn_doc_zaiavlenie then vp.date_status_zaiavlenie else null end a9 ");
			}
			
			from.append(" from doc d ");
			from.append(" inner join mms_vpisvane_doc vpd on vpd.id_doc = d.doc_id ");
			from.append(" inner join mms_vpisvane vp on vp.id = vpd.id_vpisvane ");

			where.append(" where vpd.id_object = :idObject and vpd.type_object = :typeObj ");
			   
			params.put("idObject", idObj);
			params.put("typeObj", codeObj);

			SelectMetadata smd = new SelectMetadata();
			
			smd.setSql("" + select + from + where);
			smd.setSqlParameters(params);
			smd.setSqlCount(" select count(*) " + from + where);
			
			return smd;
			
		} catch (Exception e) {
			throw new DbErrorException("Грешка при търсене на документи по ид на обект и код на обект! - " + e.getLocalizedMessage(), e);
		}
	}	
	
	/** Метод за връщане на вписванията към обект
	 * @param codeObject - Код на обект
	 * @param idObject - ИД на обект
	 * @return Списък вписвания
	 * @throws DbErrorException - грешка при работа с БД
	 */
	public SelectMetadata findRegsListNativeSMD(Integer typeObject, Integer idObject) throws DbErrorException{
		Map<String, Object> sqlParams = new HashMap<>();
		SelectMetadata smd = new SelectMetadata();
		try {
			
			String sql = " SELECT  V.ID, V.RN_DOC_ZAIAVLENIE, V.DATE_DOC_ZAIAVLENIE, V.STATUS, V.reason_result,  V.RN_DOC_LICENZ,  V.DATE_DOC_LICENZ, "
					+ " V.rn_doc_spirane, V.date_doc_spirane "
					+ " FROM MMS_VPISVANE V "
					+ "	WHERE V.TYPE_OBJECT = :TO AND V.ID_OBJECT = :IO";			
			
			sqlParams.put("TO", typeObject);
			sqlParams.put("IO", idObject);
			smd.setSql(sql);
			smd.setSqlCount(" select count(1) FROM MMS_VPISVANE V WHERE V.TYPE_OBJECT = :TO AND V.ID_OBJECT = :IO " );
			smd.setSqlParameters(sqlParams);
			
			return smd;
		
		} catch (Exception e) {
			LOGGER.debug("Грешка при извличане на вписванията към обект", e);
			throw new DbErrorException("Грешка при извличане на вписванията към обект - " + e.getLocalizedMessage(), e);
		}
	}
	
	/** Метод за връщане на вписванията към обект
	 * @param codeObject - Код на обект
	 * @param idObject - ИД на обект
	 * @return Списък вписвания
	 * @throws DbErrorException - грешка при работа с БД
	 */
	public SelectMetadata findRegsListNativeSMDNew(Integer typeObject, Integer idObject) throws DbErrorException{
		Map<String, Object> sqlParams = new HashMap<>();
		SelectMetadata smd = new SelectMetadata();
		try {
			
			String sql = " SELECT  V.ID, V.RN_DOC_ZAIAVLENIE, V.DATE_DOC_ZAIAVLENIE, V.STATUS, V.RN_DOC_LICENZ,  V.DATE_DOC_LICENZ, "
					+ " V.RN_DOC_RESULT, V.DATE_DOC_RESULT, V.RN_DOC_SPIRANE, V.DATE_DOC_SPIRANE, V.VID_SPORT, V.DLAJNOST  "
					+ " FROM MMS_VPISVANE V "
					+ "	WHERE V.TYPE_OBJECT = :TO AND V.ID_OBJECT = :IO";			
			
			sqlParams.put("TO", typeObject);
			sqlParams.put("IO", idObject);
			smd.setSql(sql);
			smd.setSqlCount(" select count(1) FROM MMS_VPISVANE V WHERE V.TYPE_OBJECT = :TO AND V.ID_OBJECT = :IO " );
			smd.setSqlParameters(sqlParams);
			
			return smd;
		
		} catch (Exception e) {
			LOGGER.debug("Грешка при извличане на вписванията към обект", e);
			throw new DbErrorException("Грешка при извличане на вписванията към обект - " + e.getLocalizedMessage(), e);
		}
	}
	
	/** Метод за връщане на вписванията към обект
	 * @param codeObject - Код на обект
	 * @param idObject - ИД на обект
	 * @return Списък вписвания
	 * @throws DbErrorException - грешка при работа с БД
	 */
	public SelectMetadata findRegsListNativeSMDLast(Integer typeObject, Integer idObject) throws DbErrorException{
		Map<String, Object> sqlParams = new HashMap<>();
		SelectMetadata smd = new SelectMetadata();
		try {
			
			String sql = " SELECT  V.ID, V.RN_DOC_ZAIAVLENIE, V.DATE_DOC_ZAIAVLENIE, V.STATUS_RESULT_ZAIAVLENIE, V.DATE_STATUS_ZAIAVLENIE,  V.REASON_RESULT, V.REASON_RESULT_TEXT, V.RN_DOC_RESULT, V.DATE_DOC_RESULT,  V.RN_DOC_LICENZ,  V.DATE_DOC_LICENZ, "
					+ " V.RN_DOC_VPISVANE, V.DATE_DOC_VPISVANE, V.STATUS_VPISVANE,  V.DATE_STATUS_VPISVANE, V.REASON_VPISVANE, V.REASON_VPISVANE_TEXT,  V.VID_SPORT, V.DLAJNOST, V.NACHIN_POLUCHAVANE, V.ADDR_MAIL_POLUCHAVANE  "
					+ " FROM MMS_VPISVANE V "
					+ "	WHERE V.TYPE_OBJECT = :TO AND V.ID_OBJECT = :IO";			
			
			sqlParams.put("TO", typeObject);
			sqlParams.put("IO", idObject);
			smd.setSql(sql);
			smd.setSqlCount(" select count(1) FROM MMS_VPISVANE V WHERE V.TYPE_OBJECT = :TO AND V.ID_OBJECT = :IO " );
			smd.setSqlParameters(sqlParams);
			
			return smd;
		
		} catch (Exception e) {
			LOGGER.debug("Грешка при извличане на вписванията към обект", e);
			throw new DbErrorException("Грешка при извличане на вписванията към обект - " + e.getLocalizedMessage(), e);
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<MMSVpisvane> findRegsList(Integer typeObj, Integer idObj){
		String sql = "SELECT  V FROM MMSVpisvane V WHERE V.typeObject = :TO AND V.idObject = :IO order by V.dateDocZaiavlenie desc";
		Query query = createQuery(sql).setParameter("TO", typeObj).setParameter("IO", idObj);
		return query.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<MMSVpisvane> findRegsListMaxDateObr(Integer typeObj, Integer idObj){
		String sql = "SELECT  V FROM MMSVpisvane V WHERE V.typeObject = :TO AND V.idObject = :IO order by V.dateLastMod Desc, V.dateReg Desc, V.id Desc ";
		Query query = createQuery(sql).setParameter("TO", typeObj).setParameter("IO", idObj);
		return query.getResultList();
	}
	
	
	/**
	 * Генерира рег.номер за подаден обект
	 * 
	 * @param typeObject от класисикацията за обекта, за който се иска номер
	 * @param regNomerObedinenie дава се само ако се генерира за формирование и това е рег.номера на обединението.
	 * @param ekatte 
	 * @return
	 * @throws DbErrorException
	 * @throws InvalidParameterException 
	 */
	public String genRegNomer(int typeObject, String regNomerObedinenie, Integer ekatte, Integer vidObject) throws DbErrorException, InvalidParameterException {
		String obekt;
		if (typeObject == DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORT_OBED) {
			if (vidObject == null) {
				vidObject = DocuConstants.CODE_ZNACHENIE_VID_SPORT_OBEDINENIE_SF;
			}
			if (vidObject.intValue() == DocuConstants.CODE_ZNACHENIE_VID_SPORT_OBEDINENIE_NOSTD) {
				obekt = "obed_nostd";
			} else if (vidObject.intValue() == DocuConstants.CODE_ZNACHENIE_VID_SPORT_OBEDINENIE_OK) {
				obekt = "obed_osk";
			} else { // тука остават Спортна федерация и НОУС
				obekt = "obedinenie";
			}

		} else if (typeObject == DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_FORMATIONS) {
			regNomerObedinenie = SearchUtils.trimToNULL(regNomerObedinenie);
			
			if (vidObject != null && vidObject.intValue() == DocuConstants.CODE_ZNACHENIE_VID_SPORTNO_FORMIROVANIE_TD) {
				obekt = "turist_druj";
			} else if (regNomerObedinenie == null) {
				obekt = "formir.000-000";
			} else if (regNomerObedinenie.indexOf('-') == -1) {
				throw new InvalidParameterException("Невалиден номер на спортно обединение : " + regNomerObedinenie);
			} else {
				obekt = "formir." + regNomerObedinenie;
			}

		} else if (typeObject == DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_OBJECTS) {
			if (ekatte == null) {
				obekt = "obekt.00000";
			} else {
				
				@SuppressWarnings("unchecked")
				List<Object> list = createNativeQuery(
					" select obst.ekatte from ekatte_att att inner join ekatte_obstini obst on obst.obstina = att.obstina "
					+ " where att.ekatte = ?1 order by att.date_ot desc ")
					.setParameter(1, ekatte).getResultList();
				if (!list.isEmpty()) {
					ekatte = ((Number)list.get(0)).intValue();
				}
				obekt = "obekt." + ekatte;
			}
		} else {
			throw new InvalidParameterException("Невалиден typeObject=" + typeObject);
		}

		try {
			StoredProcedureQuery storedProcedure = getEntityManager().createStoredProcedureQuery("gen_nom_mms") //
				.registerStoredProcedureParameter(0, String.class, ParameterMode.IN) //
				.registerStoredProcedureParameter(1, Integer.class, ParameterMode.OUT); //

			storedProcedure.setParameter(0, obekt);

			storedProcedure.execute();

			Integer gen = (Integer) storedProcedure.getOutputParameterValue(1);

			String result;
			
			if (typeObject == DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORT_OBED) {
				if (vidObject.intValue() == DocuConstants.CODE_ZNACHENIE_VID_SPORT_OBEDINENIE_NOSTD) {
					result = String.valueOf(gen);
				} else {
					result = String.format("%03d", gen) + "-000";
				}
				
			} else if (typeObject == DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_FORMATIONS) {
				
				if (vidObject != null && vidObject.intValue() == DocuConstants.CODE_ZNACHENIE_VID_SPORTNO_FORMIROVANIE_TD) {
					result = String.valueOf(gen);
				} else if (regNomerObedinenie == null) {
					result = "000-" + String.format("%03d", gen);
				} else {
					result = regNomerObedinenie.substring(0, regNomerObedinenie.indexOf('-')) + "-" + String.format("%03d", gen);
				}
				
			} else if (typeObject == DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_OBJECTS) {

				if (ekatte == null) {
					result = "С-00000-" + String.format("%03d", gen);
				} else {
					result = "С-" + String.format("%05d", ekatte) + "-" + String.format("%03d", gen);
				}

			} else {
				throw new InvalidParameterException("Невалиден typeObject=" + typeObject);
			}
			return result;
			
		} catch (Exception e) {
			throw new DbErrorException("Грешка при генериране на регистров номер на обект!", e);
		}
	}
	
	public void updateStatusReg(Integer statusInt, Date dateStatusDate, Integer idRegister, int typeObject) throws DbErrorException {

		try {
			TypedParameterValue status = new TypedParameterValue(StandardBasicTypes.INTEGER, statusInt);
			TypedParameterValue dateStatus = new TypedParameterValue(StandardBasicTypes.TIMESTAMP, dateStatusDate);
			
			if (typeObject == DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORT_OBED) {
				Query query = createNativeQuery( "update mms_sport_obedinenie set status = :status, date_status = :dateStatus, user_last_mod = :userId, date_last_mod = :dateLast where id = :idRegister" )
						.setParameter("status", status).setParameter("dateStatus", dateStatus).setParameter("idRegister", idRegister).setParameter("userId", getUserId()).setParameter("dateLast", new Date());
				
				query.executeUpdate();
				
				if (statusInt != null && statusInt.equals(DocuConstants.CODE_ZNACHENIE_STATUS_OBEKT_ZALICHEN)) {
//					При заличаване на федерация да и се затварят автоматично всички членства, като се постави „дата до“ = today().
					@SuppressWarnings("unchecked")
					List<MMSChlenstvo> list = createQuery("select x from MMSChlenstvo x where x.idVishObject = ?1 and x.typeVishObject = ?2 and x.dateTermination is null")
							.setParameter(1, idRegister).setParameter(2, typeObject).getResultList();
					for (MMSChlenstvo x : list) {
						x.setDateTermination(dateStatusDate);
						getEntityManager().merge(x);
					}
				}
				
			} else if (typeObject == DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_FORMATIONS) {
				Query query = createNativeQuery( "update mms_sport_formirovanie set status = :status, date_status = :dateStatus, user_last_mod = :userId, date_last_mod = :dateLast where id = :idRegister" )
						.setParameter("status", status).setParameter("dateStatus", dateStatus).setParameter("idRegister", idRegister).setParameter("userId", getUserId()).setParameter("dateLast", new Date());
				
				query.executeUpdate();
				
				if (statusInt != null && statusInt.equals(DocuConstants.CODE_ZNACHENIE_STATUS_OBEKT_ZALICHEN)) {
//					При заличаване на спортен клуб да му се затварят автоматично всички членства, като се постави „дата до“ = today().
					@SuppressWarnings("unchecked")
					List<MMSChlenstvo> list = createQuery("select x from MMSChlenstvo x where x.idObject = ?1 and x.typeObject = ?2 and x.dateTermination is null")
							.setParameter(1, idRegister).setParameter(2, typeObject).getResultList();
					for (MMSChlenstvo x : list) {
						x.setDateTermination(dateStatusDate);
						getEntityManager().merge(x);
					}
				}
				
			} else if (typeObject == DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_OBJECTS) {
				Query query = createNativeQuery( "update mms_sport_obekt set status = :status, date_status = :dateStatus, user_last_mod = :userId, date_last_mod = :dateLast where id = :idRegister" )
						.setParameter("status", status).setParameter("dateStatus", dateStatus).setParameter("idRegister", idRegister).setParameter("userId", getUserId()).setParameter("dateLast", new Date());
				
				query.executeUpdate();
			
			} else if (typeObject == DocuConstants.CODE_ZNACHENIE_JOURNAL_COACHES) {
				Query query = createNativeQuery( "update mms_coaches set status = :status, date_status = :dateStatus, user_last_mod = :userId, date_last_mod = :dateLast where id = :idRegister" )
						.setParameter("status", status).setParameter("dateStatus", dateStatus).setParameter("idRegister", idRegister).setParameter("userId", getUserId()).setParameter("dateLast", new Date());
				
				query.executeUpdate();
			}
		
		} catch (Exception e) {
			throw new DbErrorException("Грешка при смяна на статус в регистрите!", e);
		}
	}
	
	/**
	 * Метод за намиране на решението или статуса по ид на вписване
	 * 
	 * @param idReg
	 * @return
	 */
	public Object[] findStatusiByIdReg(Integer idReg) {
		
		Query q =  JPA.getUtil().getEntityManager().createQuery(" SELECT V.statusResultZaiavlenie, V.statusVpisvane FROM MMSVpisvane V WHERE V.id = :idReg ");
		
		q.setParameter("idReg", idReg);	
		return (Object[]) q.getSingleResult();
	}
	

	/**
	 *   Дали за съответния обект има записан документ от заявление он Архимед с указания рег. номер
	 * @param idObj    -  id на обект
	 * @param codeObj - код обект
	 * @param rnDoc - рег. номер
	 * @param vidDoc - вид документ
	 * @return
	 * @throws DbErrorException
	 */
	public boolean checkDocsYes (Integer idObj, Integer codeObj, String rnDoc, Integer vidDoc) throws DbErrorException {
		try {
			 Query query = createNativeQuery(" select count(*) cnt  from doc where doc_id  in ( "
				+ " select vpd.id_doc from mms_vpisvane_doc vpd where vpd.id_object = :idObject and vpd.type_object = :typeObj  "
				+ " )  and doc.rn_doc = :rnDoc and doc.doc_vid = :docVid and doc.registratura_id = 1 and doc.register_id = 2 and doc.doc_type = 1 ")
				.setParameter("idObject", idObj)
				.setParameter("typeObj", codeObj) 
				.setParameter("rnDoc", rnDoc)
				.setParameter("docVid", vidDoc);
			
			@SuppressWarnings("unchecked")
			Integer br =   asInteger(query.getSingleResult());
			if (br == null || br.intValue() <= 0) return false;
			return true;
			
			
			} catch (Exception e) {
			   throw new DbErrorException("Грешка при търсене на документи за обект! - " + e.getLocalizedMessage(), e);
			}
   }	
	
	/**
	 * Ттърсене на ID на  обект, записан със заявление за вписване (проверка за наличие на такъв  обект)
	 * @param codeObj - код  обект
	 * @param rnDoc - рег. номер на заявлението за вписване
	 * @param vidDoc - вид документ (заявление за вписване)
	 * @return
	 * @throws DbErrorException
	 */
	public Integer checkForZaiavlVp ( Integer codeObj, String rnDoc, Integer vidDoc) throws DbErrorException {
		try {
			 Query query = createNativeQuery(" select vpd.id_object  from doc,  mms_vpisvane_doc vpd where doc.doc_id  = vpd.id_doc "
				+ "  and vpd.type_object = :typeObj  "
				+ "  and doc.rn_doc = :rnDoc and doc.doc_vid = :docVid and doc.registratura_id = 1 and doc.register_id = 2 and doc.doc_type = 1 ")
				.setParameter("typeObj", codeObj) 
				.setParameter("rnDoc", rnDoc)
				.setParameter("docVid", vidDoc);
			
			@SuppressWarnings("unchecked")
			List<Object> list = query.getResultList();
			if (list == null || list.isEmpty())   return Integer.valueOf(-1);
			Integer idObj =   asInteger(list.get(0));
			return idObj;
			
			
			} catch (Exception e) {
			   throw new DbErrorException("Грешка при търсене на ID на спортен обект, записан със заявление за вписване ! - " + e.getLocalizedMessage(), e);
			}
   }		
}
