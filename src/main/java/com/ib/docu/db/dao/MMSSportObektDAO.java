package com.ib.docu.db.dao;

import static com.ib.system.SysConstants.CODE_CLASSIF_EKATTE;
import static com.ib.system.SysConstants.CODE_DEFAULT_LANG;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.ParameterMode;
import javax.persistence.Query;
import javax.persistence.StoredProcedureQuery;
import javax.persistence.TypedQuery;

import com.ib.docu.db.dto.MMSSportObekt;
import com.ib.docu.db.dto.MMSSportObektLice;
import com.ib.docu.db.dto.MMSVpisvane;
import com.ib.docu.db.dto.Referent;
import com.ib.docu.system.DocuConstants;
import com.ib.docu.system.SystemData;
import com.ib.system.ActiveUser;
import com.ib.system.BaseSystemData;
import com.ib.system.db.AbstractDAO;
import com.ib.system.db.JPA;
import com.ib.system.db.SelectMetadata;
import com.ib.system.db.dto.SystemClassif;
import com.ib.system.exceptions.DbErrorException;
import com.ib.system.exceptions.ObjectInUseException;
import com.ib.system.utils.DateUtils;
import com.ib.system.utils.SearchUtils;
import com.ib.system.utils.StringUtils;

// За спортен обект

public class MMSSportObektDAO extends AbstractDAO<MMSSportObekt > {
	
	/** @param user */
	public MMSSportObektDAO(ActiveUser user) {
		super(MMSSportObekt.class, user);
	}
	
	public MMSSportObektDAO(Class<MMSSportObekt> typeClass, ActiveUser user) {
		super(typeClass, user);
	}

	
	/**
	 * Изтриване на обект от регистъра
	 * 
	 * @param id
	 * @param sd 
	 * @throws DbErrorException
	 * @throws ObjectInUseException
	 */
	@SuppressWarnings("unchecked")
	public void deleteFromRegister(Integer id, SystemData sd) throws DbErrorException, ObjectInUseException {
		MMSSportObekt entity = findById(id);
		if (entity == null) {
			return;
		}
		
		// mms_vpisvane_doc + mms_vpisvane
		// + doc + files + file_objects - и то само ако дока не се цитира другаде
		MMSVpisvaneDAO vpisvaneDao = new MMSVpisvaneDAO(getUser());
		List<MMSVpisvane> listVpisvane = vpisvaneDao.findRegsListByIdAndType(DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_OBJECTS, entity.getId());
		for (MMSVpisvane delme : listVpisvane) {
			vpisvaneDao.delete(delme);
		}
		
		Set<Integer> setRefCode = new HashSet<>();
		try { 
			// mms_sport_obekt_lice
			List<MMSSportObektLice> listSportObektLice = createQuery("select t from MMSSportObektLice t where t.idSportObekt = ?1")
				.setParameter(1, entity.getId()).getResultList();
			
			MMSSportObektLiceDAO sportObektLiceDao = new MMSSportObektLiceDAO(getUser());
			for (MMSSportObektLice delme : listSportObektLice) {
				setRefCode.add(delme.getIdLice());
				sportObektLiceDao.delete(delme);
			}

		} catch (Exception e) {
			 throw new DbErrorException("Грешка при изтриване на свързани обекти за спортни обекти!", e);
		}
		
		// mms_sport_obekt
		super.delete(entity);

		// adm_referents + adm_ref_addrs (само ако не са цитирани другаде)
		ReferentDAO referentDao = new ReferentDAO(getUser());
		for (Integer refCode : setRefCode) {
			Referent deleted = referentDao.deleteIfNotUsed(refCode);
			if (deleted != null) {
				sd.mergeReferentsClassif(deleted, true);
			}
		}
	}
	
	/** 
	 * Tърсене  на спортен обект по  рег. номер
	 *  
	 * @param Integer nDoc
	 * @return
	 * @throws DbErrorException
	 */
	@SuppressWarnings("unchecked")
	public MMSSportObekt  findByNomDoc(Integer nDoc) throws DbErrorException{
		try {
			
			List<MMSSportObekt> list = createQuery("select sportObekt  from MMSSportObekt  sportObekt  where sportObekt.regNomer = ?1").
					setParameter(1, nDoc).getResultList();
			
			if (list.isEmpty()) {
				return null;
			}
			return (MMSSportObekt)list.get(0);
			
		} catch (Exception e) {
			throw new DbErrorException("Грешка при търсене на спортен обект по Рег.номер! -" + e.getLocalizedMessage(), e);
		}
		
	}
	
	/** 
	 * Tърсене  на спортен обект по  рег. номер
	 *  
	 * @param Integer nDoc
	 * @return
	 * @throws DbErrorException
	 */
	@SuppressWarnings("unchecked")
	public MMSSportObekt  findByRegNom(String  regNom) throws DbErrorException{
		if (regNom == null || regNom.trim().isEmpty())   return null;
		try {
			
			List<MMSSportObekt> list = createQuery("select sportObekt  from MMSSportObekt  sportObekt  where sportObekt.regNomer = ?1").
					setParameter(1, regNom.trim()).getResultList();
			
			if (list.isEmpty()) {
				return null;
			}
			return (MMSSportObekt)list.get(0);
			
		} catch (Exception e) {
			throw new DbErrorException("Грешка при търсене на спортен обект по Рег.номер! -" + e.getLocalizedMessage(), e);
		}
		
	}
	
	/**
	 * Връща всички лица във връзка със спортен обект чрез таблица  mms_sport_obekt_lice
	 *
	 * @param idOb = id на спортния обект 
	 * @return
	 * @throws DbErrorException
	 */
	public SelectMetadata findLicaVrSpOb  (Integer idOb) throws DbErrorException {
	
		Map<String, Object> sqlParams = new HashMap<>();
		StringBuilder sb = new StringBuilder();
		SelectMetadata smd = new SelectMetadata();
		try {
	//		sb.append(" select ID, ID_OBJECT, NAME_LICE, TYPE_VRAZKA  from  mms_sport_obekt_lice  where id_sport_obekt = :idObj ");
			sb.append(" select s_ob_lice  from  mms_sport_obekt_lice  s_ob_lice  where  s_ob_lice.id_sport_obekt = :idObj ");
			
			sqlParams.put("idObj", idOb);
			
			smd.setSql(sb.toString());
			smd.setSqlParameters(sqlParams);
			smd.setSqlCount(" select count(*)  from mms_sport_obekt_lice where id_sport_obekt = :idObj  ");
				
			return smd;
					
		} catch (Exception e) {
			throw new DbErrorException("Грешка при търсене на връзки с лиса за спортен обект по ID на обект ! - "+ e.getLocalizedMessage(), e);
		}
	}	

	/**
	 * Получаване на списък със свързани лица за спортен обект
	 * @param idObj  - ID на спортен обект 
	 * @return
	 */
public List<MMSSportObektLice> findSpObLice(Integer idObj)   throws DbErrorException {
		if (idObj == null)  return null;
	
		try {
			String sql = " select s  from  MMSSportObektLice  s  where  s.idSportObekt = :idObj order by s.id " ;
			TypedQuery<MMSSportObektLice> q = JPA.getUtil().getEntityManager().createQuery(sql, MMSSportObektLice.class);
			q.setParameter("idObj", idObj);
					
			return  q.getResultList();
			
        } catch (Exception e) {
        
			throw new DbErrorException ("Грешка при извличане на списък със свързани лица към спортен обект - " + e.getLocalizedMessage());
			
        }
	
  } 

		
	/**
	 * Получаване на списък със спортни обекти със сходни имена
	 * @param name - въведено име на спортен обект, за което се търсят спортни обекти с подобни имена, 
	 * @return
	 */
   public List<MMSSportObekt> findSpObImena(String name ) throws DbErrorException {
	   if (name == null || name.trim().isEmpty())  return null;
	   
	   List<String> words = StringUtils.divideString(name);
	   String sql2 = "";
	
	   for (int i = 0; i < words.size(); i++) {
			String word = (String) words.get(i);
			sql2 +=" AND UPPER(s.name) LIKE '%" + word.toUpperCase().trim()+"%'";					
		}
	   
	   try { 
				String sql = " select s  from  MMSSportObekt  s  where  1=1 " + sql2 + "  order by s.id " ;
				TypedQuery<MMSSportObekt> q = JPA.getUtil().getEntityManager().createQuery(sql, MMSSportObekt.class);
							
				return q.getResultList();
	
	   } catch (Exception e) {
		   throw new DbErrorException ("Грешка при извличане на списък с въведени спортни обекти със сходни имена - " + e.getLocalizedMessage());
	   }
	}
	  
   
   /**
	 * Получаване на списък със спортни обекти със сходни имена
	 * @param name - въведено име на спортен обект, за което се търсят спортни обекти с подобни имена, 
	 * @return
	
	 * @throws DbErrorException
	 */
	public SelectMetadata  findSpObListImena (String name ) throws DbErrorException {
		   if (name == null || name.trim().isEmpty())  return null;
		   
		   List<String> words = StringUtils.divideString(name);
		   String sql2 = "";
		
		   for (int i = 0; i < words.size(); i++) {
				String word = (String) words.get(i);
				sql2 +=" AND UPPER(name) LIKE '%" + word.toUpperCase().trim()+"%'";					
			}
		
		Map<String, Object> sqlParams = new HashMap<>();
		SelectMetadata smd = new SelectMetadata();
		try {
			
					String sql = " select ID, NAME, VID, FUNK_CATEGORY, COUNTRY, NAS_MESTO, SGRADA, REG_NOMER, DATE_REG DATE_REG      from  mms_sport_obekt   where  1=1 " + sql2 + " " ;
					String sqlcount = " select count(*)  from  mms_sport_obekt    where 1=1 " + sql2 + " " ;
					
			smd.setSql(sql);
			smd.setSqlParameters(sqlParams);
			smd.setSqlCount(sqlcount);
				
			return smd;
					
		} catch (Exception e) {
			 throw new DbErrorException ("Грешка при извличане на списък с въведени спортни обекти със сходни имена - " + e.getLocalizedMessage());
		}
	}	
   
   
   /**
	 * Генериране на регистров номер на документ по регистър
	 *
	 * @param idRegister
	 * @throws DbErrorException
	 */
	public String genRnDocByRegister(Integer idRegister) throws DbErrorException {
		try {
			StoredProcedureQuery storedProcedure = getEntityManager().createStoredProcedureQuery("gen_nom_register") //
				.registerStoredProcedureParameter(0, Integer.class, ParameterMode.IN) //
				.registerStoredProcedureParameter(1, String.class, ParameterMode.OUT) //
				.registerStoredProcedureParameter(2, String.class, ParameterMode.OUT) //
				.registerStoredProcedureParameter(3, Integer.class, ParameterMode.OUT);

			storedProcedure.setParameter(0, idRegister);

			storedProcedure.execute();

//			doc.setRnDoc((String) storedProcedure.getOutputParameterValue(1));     // rnDoc
//			doc.setRnPrefix((String) storedProcedure.getOutputParameterValue(2));   // rnPrefix
//			doc.setRnPored((Integer) storedProcedure.getOutputParameterValue(3)); // rnPored
			
                return (String) storedProcedure.getOutputParameterValue(1);     // regNomer
		} catch (Exception e) {
			throw new DbErrorException("Грешка при генериране на регистров номер на документ по регистър! - " + e.getLocalizedMessage(), e);
		}
		
		
	}
	
	/**
	 * Актуализация на статус и дата статус за спортен обект
	 * @param id   - id на запис за спортен обект
	 * @param status  - нова стойност за статус
	 * @param dStatus - стойност за дата статус
	 * @return
	 * @throws DbErrorException
	 */
	public int updateStatusInSpObekt  (Integer id, Integer status,  Date dStatus)  {
		Query q = null;
		if (id == null) return 0;
						
				    q =  JPA.getUtil().getEntityManager().createNativeQuery(" update  mms_sport_obekt set status =:stat, date_status = :dstat  where id = :idz ");
				    q.setParameter("stat", status);
				    q.setParameter("dstat", dStatus);
				    q.setParameter("idz", id);
				
							
				return q.executeUpdate();
			
	}	
	
	public void updateRegNomSO(Integer idSO, String regNom) throws DbErrorException {

		try {
			
			Query query = createNativeQuery( "update mms_sport_obekt set reg_nomer = :regNom, user_last_mod = :userId, date_last_mod = :date where id = :idSO");
			
			query.setParameter("idSO", idSO);
			query.setParameter("regNom", regNom);
			query.setParameter("userId", getUserId());
			query.setParameter("date", new Date());
			
			query.executeUpdate();
		
		} catch (Exception e) {
			throw new DbErrorException("Грешка при запис на регистров номер на спортен обект!", e);
		}
	}	

public SelectMetadata buildQuery(Integer vid, Integer funkCategory, String name, String regNomer, Integer ekatte //
	, String rnZaiav, Date dateFromZaiav, Date dateToZaiav //
	, Integer status, Date dateFromStatus, Date dateToStatus //
	, BaseSystemData sd
	, Integer statusVpis, Date fromStatusVpis, Date toStatusVpis, Integer statusZaiav, Date fromStatusZaiav, Date toStatusZaiav
	, List<Integer> vidSportList) throws DbErrorException {

	Map<String, Object> params = new HashMap<>();

	StringBuilder select = new StringBuilder();
	StringBuilder from = new StringBuilder();
	StringBuilder where = new StringBuilder();

	select.append(" select distinct o.id a0, o.reg_nomer a1, o.status a2, o.date_status a3 ");
	select.append(" , o.vid a4, o.funk_category a5, o.name a6, o.identification a7, att.ekatte a8 ");
//	select.append(" , v.id a9, v.rn_doc_zaiavlenie a10, v.date_doc_zaiavlenie a11 ");
	select.append(" , null a9, null a10, null a11 ");

//	select.append(" , v.status_vpisvane a12, v.date_status_vpisvane a13, v.status_result_zaiavlenie a14, v.date_status_zaiavlenie a15 ");
	select.append(" , null a12, null a13, null a14, null a15 ");
	select.append(" , att.tvm || ' ' || att.ime a16, att.obstina_ime a17, att.oblast_ime a18 ");
	select.append(" , z.USER_ID a19, z.LOCK_DATE a20 ");

	from.append(" from mms_sport_obekt o ");
	from.append(" left outer join mms_vpisvane v on v.id_object = o.id and v.type_object = :tipObj ");
	from.append(" left outer join ekatte_att att on att.ekatte = o.nas_mesto ");

	where.append(" where 1=1 ");
	params.put("tipObj", DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_OBJECTS);

	if (vid != null) {
		where.append(" and o.vid = :vid ");
		params.put("vid", vid);
	}
	if (funkCategory != null) {
		where.append(" and o.funk_category = :funkCategory ");
		params.put("funkCategory", funkCategory);
	}
	if (vidSportList != null && !vidSportList.isEmpty()) {
		where.append(" and EXISTS (select s.id from mms_vid_sport s where s.tip_object = :tipObj and s.id_object = o.id and s.vid_sport in (:vidSportList)) ");
		params.put("vidSportList", vidSportList);
	}

	String t = SearchUtils.trimToNULL_Upper(name);
	if (t != null) {
		where.append(" and upper(o.name) like :name ");
		params.put("name", "%" + t + "%");
	}

	t = SearchUtils.trimToNULL(regNomer);
	if (t != null) {
		where.append(" and o.reg_nomer = :regNomer ");
		params.put("regNomer", regNomer);
	}

	if (ekatte != null) {
		if (ekatte.intValue() < 100_000) { // търсене по населено място
			where.append(" and att.EKATTE = :ekatte ");
			params.put("ekatte", ekatte);

		} else { // търсене по област или община
			SystemClassif item = sd.decodeItemLite(CODE_CLASSIF_EKATTE, ekatte, CODE_DEFAULT_LANG, null, false);

			if (item != null && item.getCodeExt() != null) {
				String col = null;
				if (item.getCodeExt().length() == 3) { // област
					col = "OBLAST";
				} else if (item.getCodeExt().length() == 5) { // община
					col = "OBSTINA";
				}

				if (col != null) {
					where.append(" and att." + col + " = :codeExt ");
					params.put("codeExt", item.getCodeExt());
				}
			}
		}
	}

	t = SearchUtils.trimToNULL_Upper(rnZaiav);
	if (t != null) {
		where.append(" and upper(v.rn_doc_zaiavlenie) like :rnZaiav ");
		params.put("rnZaiav", "%" + t + "%");
	}
	if (dateFromZaiav != null) {
		where.append(" and v.date_doc_zaiavlenie >= :dateFromZaiav ");
		params.put("dateFromZaiav", DateUtils.startDate(dateFromZaiav));
	}
	if (dateToZaiav != null) {
		where.append(" and v.date_doc_zaiavlenie <= :dateToZaiav ");
		params.put("dateToZaiav", DateUtils.endDate(dateToZaiav));
	}
   
	if (status != null) {
		where.append(" and o.status = :status ");
		params.put("status", status);
	}
	if (dateFromStatus != null) {
		where.append(" and o.date_status >= :dateFromStatus ");
		params.put("dateFromStatus", DateUtils.startDate(dateFromStatus));
	}
	if (dateToStatus != null) {
		where.append(" and o.date_status <= :dateToStatus ");
		params.put("dateToStatus", DateUtils.endDate(dateToStatus));
	}

	if (statusVpis != null) {
		where.append(" and v.status_vpisvane = :statusVpis ");
		params.put("statusVpis", statusVpis);
	}
	if (fromStatusVpis != null) {
		where.append(" and v.date_status_vpisvane >= :fromStatusVpis ");
		params.put("fromStatusVpis", DateUtils.startDate(fromStatusVpis));
	}
	if (toStatusVpis != null) {
		where.append(" and v.date_status_vpisvane <= :toStatusVpis ");
		params.put("toStatusVpis", DateUtils.endDate(toStatusVpis));
	}

	if (statusZaiav != null) {
		where.append(" and v.status_result_zaiavlenie = :statusZaiav ");
		params.put("statusZaiav", statusZaiav);
	}
	if (fromStatusZaiav != null) {
		where.append(" and v.date_status_zaiavlenie >= :fromStatusZaiav ");
		params.put("fromStatusZaiav", DateUtils.startDate(fromStatusZaiav));
	}
	if (toStatusZaiav != null) {
		where.append(" and v.date_status_zaiavlenie <= :toStatusZaiav ");
		params.put("toStatusZaiav", DateUtils.endDate(toStatusZaiav));
	}

	from.append(" left outer join LOCK_OBJECTS z on z.OBJECT_TIP = :zTip and z.OBJECT_ID = o.id and z.USER_ID != :zUser ");
	params.put("zTip", DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_OBJECTS);
	params.put("zUser", getUserId());

	SelectMetadata sm = new SelectMetadata();
	sm.setSqlCount(" select count(distinct o.id) " + from + where);
	sm.setSql(select.toString() + from.toString() + where.toString());
	sm.setSqlParameters(params);

	return sm;
}
}

