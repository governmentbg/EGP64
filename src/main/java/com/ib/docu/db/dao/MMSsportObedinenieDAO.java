package com.ib.docu.db.dao;

import static com.ib.docu.system.DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_FORMATIONS;
import static com.ib.docu.system.DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORT_OBED;
import static com.ib.system.SysConstants.CODE_CLASSIF_EKATTE;
import static com.ib.system.SysConstants.CODE_DEFAULT_LANG;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.docu.db.dto.MMSChlenstvo;
import com.ib.docu.db.dto.MMSSportObedMf;
import com.ib.docu.db.dto.MMSSportnoObedinenie;
import com.ib.docu.db.dto.MMSVpisvane;
import com.ib.docu.db.dto.MMSsportFormirovanie;
import com.ib.docu.db.dto.Referent;
import com.ib.docu.db.dto.Task;
import com.ib.docu.system.DocuConstants;
import com.ib.docu.system.SystemData;
import com.ib.indexui.system.Constants;
import com.ib.system.ActiveUser;
import com.ib.system.BaseSystemData;
import com.ib.system.SysConstants;
import com.ib.system.db.AbstractDAO;
import com.ib.system.db.DialectConstructor;
import com.ib.system.db.JPA;
import com.ib.system.db.SelectMetadata;
import com.ib.system.db.dto.SystemClassif;
import com.ib.system.exceptions.DbErrorException;
import com.ib.system.exceptions.InvalidParameterException;
import com.ib.system.exceptions.ObjectInUseException;
import com.ib.system.utils.DateUtils;
import com.ib.system.utils.SearchUtils;


public class MMSsportObedinenieDAO extends AbstractDAO<MMSSportnoObedinenie > {

	private static final Logger LOGGER = LoggerFactory.getLogger(MMSsportObedinenieDAO.class);
	
	public MMSsportObedinenieDAO(Class<MMSSportnoObedinenie> typeClass, ActiveUser user) {
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
		MMSSportnoObedinenie entity = findById(id);
		if (entity == null) {
			return;
		}
		try {
			// mms_chlenstvo
			List<MMSChlenstvo> listChlenstvo = createQuery("select t from MMSChlenstvo t where t.idVishObject = ?1 and t.typeVishObject = ?2")
				.setParameter(1, entity.getId()).setParameter(2, DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORT_OBED).getResultList();

			MMSChlenstvoDAO chlenstvoDao = new MMSChlenstvoDAO(MMSChlenstvo.class, getUser());
			for (MMSChlenstvo delme : listChlenstvo) {
				chlenstvoDao.delete(delme);
			}

			// mms_sport_obed_mf
			List<MMSSportObedMf> listSportObedMf = createQuery("select t from MMSSportObedMf t where t.idSportObed = ?1")
				.setParameter(1, entity.getId()).getResultList();

			MMSSportObedMFDAO sportObedMFDao = new MMSSportObedMFDAO(getUser());
			for (MMSSportObedMf delme : listSportObedMf) {
				sportObedMFDao.delete(delme);
			}

		} catch (Exception e) {
			 throw new DbErrorException("Грешка при изтриване на свързани обекти за спортно обединени!", e);
		}
		
		// mms_vpisvane_doc + mms_vpisvane
		// + doc + files + file_objects - и то само ако дока не се цитира другаде
		MMSVpisvaneDAO vpisvaneDao = new MMSVpisvaneDAO(getUser());
		List<MMSVpisvane> listVpisvane = vpisvaneDao.findRegsListByIdAndType(DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORT_OBED, entity.getId());
		for (MMSVpisvane delme : listVpisvane) {
			vpisvaneDao.delete(delme);
		}
		
		// mms_sport_obedinenie + mms_vid_sport
		super.delete(entity);

		// adm_referents + adm_ref_addrs (само ако не са цитирани другаде)
		Referent deleted = new ReferentDAO(getUser()).deleteIfNotUsed(entity.getIdObject());
		if (deleted != null) {
			sd.mergeReferentsClassif(deleted, true);
		}
	}


	/** Методът търси членства на спортни обединения по ид на Лице(Референт)
	 * @param idObject = id -to на референта(лицето) намерено по ЕИК.
	 * @return списък с обединения, които членуват в структури.
	 * @throws DbErrorException 
	 *  */
	public SelectMetadata findChlenstvoByIdRefferent(Integer idObject, boolean forFilter, String idSOargs) throws DbErrorException{
		SelectMetadata smd = new SelectMetadata();
		Map<String, Object> sqlParams = new HashMap<>();
		if(idSOargs == null  || idSOargs.trim().isEmpty())
			idSOargs = "0" ;
		try {
			
			String sql = " select mso.vid , ar.ref_name , ar.nfl_eik , mc.date_acceptance , mc.date_termination , mso.reg_nomer , mso.date_reg ,  mso.id  "
					+ "from mms_sport_obedinenie mso , mms_chlenstvo mc, adm_referents ar  where mso.id_object = ar.code and mso.id_object = mc.id_vish_object and mc.type_vish_object = :typeObj "
					+ "and mso.id_object = :id_obj and mso.id not in ( " + idSOargs + " )  ";			
			
			if(forFilter) {
				sql = " select mso.vid , ar.ref_name , mso.reg_nomer , mso.date_reg ,  mso.id from mms_sport_obedinenie mso, adm_referents ar  "
						+ "where mso.id_object = ar.code and mso.id_object = :id_obj  and mso.id not in ( " + idSOargs + " )    ";
			} 
			
			if(! forFilter) {
				sqlParams.put("typeObj", DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORT_OBED);
			}
			sqlParams.put("id_obj", idObject);
			smd.setSql(sql);
			smd.setSqlCount(" select count(1) from  mms_sport_obedinenie mso , mms_chlenstvo mc "
					+ "where mso.id_object = mc.id_vish_object and mc.type_vish_object = :typeObj and mso.id_object = :id_obj  and mso.id not in ( " + idSOargs + " )   ");
			if(forFilter)
				smd.setSqlCount(" select count(1) from mms_sport_obedinenie mso, adm_referents ar  where mso.id_object = ar.code and mso.id_object = :id_obj  and mso.id not in ( " + idSOargs + " ) ");
			
			smd.setSqlParameters(sqlParams);
			return smd;
		
		} catch (Exception e) {
			LOGGER.debug("Грешка при извличане на членствата на спортно обединение ", e);
			throw new DbErrorException("Грешка при извличане на членствата на спортно обединение", e);
		}
	}
	
	public Object[] findChlenstvoByIdRefferent(Integer idObject, String idSOargs) throws DbErrorException {
		Object[] rez = null;
		String sql = "";
		if(idSOargs == null  || idSOargs.trim().isEmpty())
			idSOargs = "0" ;
		try {
			sql = " select mso.vid , ar.ref_name, ar.nfl_eik , mso.reg_nomer , mso.date_reg ,  mso.id from mms_sport_obedinenie mso, adm_referents ar  "
					+ "where mso.id_object = ar.code and mso.id_object = :id_obj  and mso.id not in ( " + idSOargs + " )    ";
			Query q = createNativeQuery(sql);
			q.setParameter("id_obj", idObject);
			if(q.getResultList().size() > 0)
				rez = (Object[]) q.getResultList().get(0);
		} catch(Exception e) {
			LOGGER.debug("Грешка при извличане на членствата на спортно обединение ", e);
			throw new DbErrorException("Грешка при извличане на членствата на спортно обединение", e);
			
		}
		
		return rez;
	}
	
	public MMSSportnoObedinenie findByIdObject(MMSSportnoObedinenie spOb) {
		Query q = JPA.getUtil().getEntityManager().createQuery("select sо from MMSSportnoObedinenie sо where sо.idObject = :idObject ").setParameter("idObject", spOb.getIdObject());
		MMSSportnoObedinenie so=null;
		try {
			so = (MMSSportnoObedinenie) q.getSingleResult();
		} catch (NoResultException e) {
			// nishto ne pravim prosto prodaljavame tova e v slu4aite kogato vavejdame neshto novo 			
		}
		if (so!=null) {
			spOb=so;
		}
		return spOb;
	}
	
	public void updateBrChlenove (Integer idObedinenie) {
		StringBuffer SQL = new StringBuffer();
			SQL.append("UPDATE ");
			SQL.append("    mms_sport_obedinenie ");
			SQL.append(" SET ");
			SQL.append("    br_chlenove = ");
			SQL.append("    ( ");
			SQL.append("        SELECT ");
			SQL.append("            COUNT (c.id_object) ");
			SQL.append("        FROM ");
			SQL.append("            mms_chlenstvo c ");
			SQL.append("        WHERE ");
			SQL.append("            c.id_vish_object=:idObedinenie ");
			SQL.append("        AND c.type_vish_object = "+DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORT_OBED+")");
			
			JPA.getUtil().getEntityManager().createNativeQuery(SQL.toString()).setParameter("idObedinenie", idObedinenie).executeUpdate();
			
	}
	
	
	public void updateRegNomSO(Integer idSO, String regNom) throws DbErrorException {

		try {
			
			Query query = createNativeQuery( "update mms_sport_obedinenie set reg_nomer = :regNom, user_last_mod = :userId, date_last_mod = :date where id = :idSO");
			
			query.setParameter("idSO", idSO);
			query.setParameter("regNom", regNom);
			query.setParameter("userId", getUserId());
			query.setParameter("date", new Date());
			
			query.executeUpdate();
		
		} catch (Exception e) {
			throw new DbErrorException("Грешка при запис на регистров номер на спортно обединение!", e);
		}
	}	
	
	public SelectMetadata buildQuery(String eik, String name, Integer ekatte, Integer vidObedinenie, List<Integer> vidSportList //
		, String rnZaiav, Date dateFromZaiav, Date dateToZaiav //
		, Integer status, Date dateFromStatus, Date dateToStatus //
		, Integer olimp, BaseSystemData sd
		, Integer statusVpis, Date fromStatusVpis, Date toStatusVpis, Integer statusZaiav, Date fromStatusZaiav, Date toStatusZaiav
		, Integer voenen
		, Integer regixDiff) throws DbErrorException {

		String dialect = JPA.getUtil().getDbVendorName();

		Map<String, Object> params = new HashMap<>();

		StringBuilder select = new StringBuilder();
		StringBuilder from = new StringBuilder();
		StringBuilder where = new StringBuilder();

		select.append(" select distinct o.id a0, o.reg_nomer a1, o.status a2, o.date_status a3 ");
		select.append(" , r.code a4, r.nfl_eik a5, r.ref_name a6 ");
//		select.append(" , v.id a7, v.rn_doc_zaiavlenie a8, v.date_doc_zaiavlenie a9 ");
		select.append(" , null a7, null a8, null a9 ");
		select.append(" , o.vid a10, o.type_sport a11, att.ekatte a12, ");
		select.append(DialectConstructor.convertToDelimitedString(dialect, "vs.vid_sport",
			"mms_vid_sport vs where vs.tip_object = " + DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORT_OBED + " and vs.id_object = o.id", "vs.id") + " a13");

//		select.append(" , v.status_vpisvane a14, v.date_status_vpisvane a15, v.status_result_zaiavlenie a16, v.date_status_zaiavlenie a17 ");
		select.append(" , null a14, null a15, null a16, null a17 ");
		select.append(" , z.USER_ID a18, z.LOCK_DATE a19 ");
		select.append(" , o.voenen_sport a20 ");
		
		from.append(" from mms_sport_obedinenie o ");
		from.append(" inner join adm_referents r on r.code = o.id_object ");
		from.append(" left outer join mms_vpisvane v on v.id_object = o.id and v.type_object = :tipObj ");

		from.append(" left outer join adm_ref_addrs a on a.code_ref = r.code and a.addr_type = " + DocuConstants.CODE_ZNACHENIE_ADDR_TYPE_CORRESP);
		from.append(" left outer join ekatte_att att on att.ekatte = a.ekatte ");

		where.append(" where 1=1 ");
		params.put("tipObj", DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORT_OBED);

		String t = SearchUtils.trimToNULL(eik);
		if (t != null) {
			where.append(" and r.nfl_eik like :eik ");
			params.put("eik", "%" + t + "%");
		}
		t = SearchUtils.trimToNULL_Upper(name);
		if (t != null) {
			where.append(" and upper(r.ref_name) like :name ");
			params.put("name", "%" + t + "%");
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

		if (vidObedinenie != null) {
			where.append(" and o.vid = :vidObedinenie ");
			params.put("vidObedinenie", vidObedinenie);
		}
		if (vidSportList != null && !vidSportList.isEmpty()) {
			where.append(" and EXISTS (select s.id from mms_vid_sport s where s.tip_object = :tipObj and s.id_object = o.id and s.vid_sport in (:vidSportList)) ");
			params.put("vidSportList", vidSportList);
		}
		if (olimp != null) {
			if (olimp.equals(SysConstants.CODE_ZNACHENIE_NE)) {
				where.append(" and (o.type_sport is null or o.type_sport = :olimp) ");
			} else {
				where.append(" and o.type_sport = :olimp ");
			}
			params.put("olimp", olimp);
		}
		if (voenen != null) {
			if (voenen.equals(SysConstants.CODE_ZNACHENIE_NE)) {
				where.append(" and (o.voenen_sport is null or o.voenen_sport = :voenen) ");
			} else {
				where.append(" and o.voenen_sport = :voenen ");
			}
			params.put("voenen", voenen);
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

		if (regixDiff != null) {
			if (regixDiff.equals(SysConstants.CODE_ZNACHENIE_NE)) {
				where.append(" and (r.regix_diff is null or r.regix_diff = '') ");
			} else {
				where.append(" and (r.regix_diff is not null and r.regix_diff <> '') ");
			}
		}

		from.append(" left outer join LOCK_OBJECTS z on z.OBJECT_TIP = :zTip and z.OBJECT_ID = o.id and z.USER_ID != :zUser ");
		params.put("zTip", DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORT_OBED);
		params.put("zUser", getUserId());

		SelectMetadata sm = new SelectMetadata();
		sm.setSqlCount(" select count(distinct o.id) " + from + where);
		sm.setSql(select.toString() + from.toString() + where.toString());
		sm.setSqlParameters(params);

		return sm;
	}
	/**
	 * Метода проверява дали няма въведено обединение за даден вид спорт.
	 * Отнася се само за спортните федерации
	 * 
	 * @param idObed - ако правим проверката когато сме в актуализация на дадено обединение (за нова тук е null)
	 * @param vidSportList - списък с избраните видове спорт
	 * @return връща текст еик-та на вече въведени обединения разделени със запетая
	 */
	public String checkForDuplicateVidSportObedinenie(Integer idObed, List<Integer> vidSportList) {
		String eikFound="";
		List<Object> rez = null;
		StringBuffer SQL = new StringBuffer();
			SQL.append("SELECT DISTINCT ");
			SQL.append("    r.nfl_eik ");
			SQL.append(" FROM ");
			SQL.append("    mms_sport_obedinenie so, ");
			SQL.append("    adm_referents r, ");
			SQL.append("    mms_vid_sport v ");
			SQL.append(" WHERE ");
			SQL.append("    r.code=so.id_object ");
			SQL.append(" AND so.id=v.id_object ");
			SQL.append(" AND v.tip_object= "+DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORT_OBED);
			SQL.append(" AND so.status="+DocuConstants.CODE_ZNACHENIE_STATUS_OBEKT_VPISAN);
			
			SQL.append(" AND so.vid="+DocuConstants.CODE_ZNACHENIE_VID_SPORT_OBEDINENIE_SF);
			if (idObed!=null) {
				SQL.append(" AND so.id<>"+idObed);
			}
			
			if (vidSportList!=null) {
				String vidString = vidSportList.toString();
				vidString = vidString.replace('[', '(').replace(']', ')');
				
				SQL.append(" AND v.vid_sport IN "+vidString);	
			}else {
				SQL.append(" AND v.vid_sport IN "
						+ "(select v1.vid_sport from mms_vid_sport v1 where v1.id_object="+idObed+" "
								+ " and v1.tip_object="+DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORT_OBED
								+ " and v1.vid_sport<>"+DocuConstants.CODE_ZNACHENIE_MNOGOSPORTOV+")");	
			}
			
			
			
			
			
		Query q = createNativeQuery(SQL.toString());
		
		rez = (ArrayList<Object>) q.getResultList();
		if (rez!=null) {
			eikFound=rez.toString();
		}				
		return eikFound;
	}
	
	
	/**
	 * Метод за генериране на задача при заличаване на спортно обединение
	 * 
	 * @param obedId
	 * @param zapZalDate
	 * @param systemData
	 * @return
	 * @throws DbErrorException
	 */
	@SuppressWarnings("unchecked")
	public Task saveTaskZalichavane(Integer obedId, Date zapZalDate, SystemData systemData) throws DbErrorException {
		if (zapZalDate == null) {
			zapZalDate = new Date();
		}
		GregorianCalendar srok = new GregorianCalendar();
		srok.setTime(zapZalDate);
		srok.add(Calendar.MONTH, 6);
		
		StringBuilder info = new StringBuilder();
		info.append("Заличаване на спортни клубове, членове на спортна федерация ");
		try {
			StringBuilder sql = new StringBuilder();
			sql.append(" select r.ref_name, r.nfl_eik ");
			sql.append(" from mms_sport_obedinenie o ");
			sql.append(" inner join adm_referents r on r.code = o.id_object ");
			sql.append(" where o.id = :obedId ");
			
			List<Object[]> rows = createNativeQuery(sql.toString()).setParameter("obedId", obedId).getResultList();
			if (rows.isEmpty()) {
				info.append("с ИД=" + obedId); // за всеки случай, ако има кофти дани
			} else {
				info.append(rows.get(0)[0] + " " + rows.get(0)[1]);
			}

		} catch (Exception e) {
			throw new DbErrorException("Грешка при търсене на Наименование/ЕИК на спортно обединение.", e);
		}
		info.append(", заличена на: " + DateUtils.printDate(zapZalDate) + "г." );
		
		List<Object> userIdList;
		try {
			StringBuilder sql = new StringBuilder();
			sql.append(" select ur.USER_ID from ADM_USER_ROLES ur where ur.CODE_CLASSIF = :codeClassif and ur.CODE_ROLE = :codeRole ");
			sql.append(" union ");
			sql.append(" select ug.USER_ID from ADM_USER_GROUP ug inner join ADM_GROUP_ROLES gr on gr.GROUP_ID = ug.GROUP_ID where gr.CODE_CLASSIF = :codeClassif and gr.CODE_ROLE = :codeRole ");
			
			userIdList = createNativeQuery(sql.toString())
				.setParameter("codeClassif", DocuConstants.CODE_CLASSIF_MENU).setParameter("codeRole", DocuConstants.CODE_ZNACHENIE_MENU_SP_FORMIR_UPD)
				.getResultList();
			
		} catch (Exception e) {
			throw new DbErrorException("Грешка при търсене на потребители с достъп до 'Актуализация на спортни формирования'.", e);
		}
		if (userIdList.isEmpty()) { // ако няма никой в тази роля - давам задачата на този, който работи
			userIdList.add(getUserId());
		}
		
		Task task = new Task();
		
		task.setRegistraturaId(1);
		task.setTaskType(DocuConstants.CODE_ZNACHENIE_TASK_TYPE_DEFAULT);
		task.setStatus(DocuConstants.CODE_ZNACHENIE_TASK_STATUS_NEIZP);
		task.setDocRequired(SysConstants.CODE_ZNACHENIE_NE);
		
		task.setTaskInfo(info.toString());
		task.setCodeAssign(getUserId());
		task.setAssignDate(zapZalDate);
		task.setSrokDate(srok.getTime());
		
		task.setCodeExecs(new ArrayList<>());
		for (Object userId : userIdList) {
			task.getCodeExecs().add(((Number)userId).intValue());
		}
		
		task = new TaskDAO(getUser()).save(task, null, systemData);
		return task;
	}
	
	/**
	 * Метод за заличаване на спортни формирования с параметър: Id на спортно обединение
	 * 
	 * @param obedId
	 * @throws DbErrorException
	 * @throws InvalidParameterException
	 * @return [0] броя на заличените формирования, [1] броя на затворените членства
	 */
	@SuppressWarnings("unchecked")
	public int[] zalichavaneSportFormir(Integer obedId) throws DbErrorException, InvalidParameterException {
		
//			Взема се рег. номера и датата на заповедта за заличаване на спортното обединение от последното вписване на спортното обединение;
		Object[] vpisvaneData = null; // данни за последното вписване за обединението
		try {
			StringBuilder sql = new StringBuilder();
			sql.append(" select o.status, v.status_vpisvane, v.rn_doc_vpisvane, v.date_doc_vpisvane ");
			sql.append(" from mms_sport_obedinenie o ");
			sql.append(" inner join mms_vpisvane v on v.id_object = o.id and v.type_object = :sportObed ");
			sql.append(" where o.id = :obedId ");
			sql.append(" order by v.id desc ");
			
			List<Object[]> rows = createNativeQuery(sql.toString())
				.setParameter("sportObed", CODE_ZNACHENIE_JOURNAL_SPORT_OBED).setParameter("obedId", obedId)
				.setMaxResults(1).getResultList();
			if (!rows.isEmpty()) {
				vpisvaneData = rows.get(0);
			}
		} catch (Exception e) {
			throw new DbErrorException("Грешка при търсене на данни за вписване на спортното обединение.", e);
		}
		if (vpisvaneData == null) {
			throw new InvalidParameterException("Не са открити данни за вписване на спортното обединение.");
		}
		if (((Number)vpisvaneData[0]).intValue() != DocuConstants.CODE_ZNACHENIE_STATUS_OBEKT_ZALICHEN) {
			throw new InvalidParameterException("Спортното обединение не е в статус 'Заличен'.");
		}
		if (((Number)vpisvaneData[1]).intValue() != DocuConstants.CODE_ZNACHENIE_STATUS_REG_ZALICHENO_VPISVANE) {
			throw new InvalidParameterException("Вписването на спортното обединение не е в статус 'Заличено вписване'.");
		}
		if (vpisvaneData[3] == null) {
			vpisvaneData[3] = new Date();
//			throw new InvalidParameterException("Не е въведена дата на заповедта за заличаване на спортното обединение.");
		}

//			Четат се последователно спортните формирования от таблица mms_chlenstvo по ID на спортното обединение;
		List<Object[]> formirovaniaData;
		try {
			StringBuilder sql = new StringBuilder();
			sql.append(" select f.id, max(v.id) vpisvane_id "); // ще се прави промяна в последното вписване
			sql.append(" , max(c.id) c_id "); // членството, което подлежи на промяна
			sql.append(" , max(c2.id) c2_id "); // ако има нещо тук значи има други активни членства и формир. не се пипа
			sql.append(" , f.status "); // статуса на формир.
			sql.append(" from mms_chlenstvo c ");
			sql.append(" inner join mms_sport_formirovanie f on f.id = c.id_object and c.type_object = :sportForm ");
			sql.append(" left outer join mms_vpisvane v on v.id_object = f.id and v.type_object = :sportForm ");
			sql.append(" left outer join mms_chlenstvo c2 on c2.type_vish_object = :sportObed and c2.id_vish_object != :obedId and c2.type_object = :sportForm and c2.id_object = c.id_object ");
			sql.append("												and c2.date_termination is null "); // другите членства, да не се затворени
			sql.append(" where c.type_vish_object = :sportObed and c.id_vish_object = :obedId ");
			sql.append(" and c.date_termination is null "); // и текущото му членство да не е затворено
			sql.append(" group by f.id, f.status ");
			
			formirovaniaData = createNativeQuery(sql.toString())
				.setParameter("sportObed", CODE_ZNACHENIE_JOURNAL_SPORT_OBED).setParameter("obedId", obedId)
				.setParameter("sportForm", CODE_ZNACHENIE_JOURNAL_SPORTS_FORMATIONS)
				.getResultList();
			
		} catch (Exception e) {
			throw new DbErrorException("Грешка при търсене на спортни формирования, които са членове на обединението.", e);
		}
		if (formirovaniaData.isEmpty()) {
			return new int [2]; // няма какво да се прави
		}
		
//			Ако поредният клуб не е член на друго спортно обединение, във вписването му се сменява статуса на „заличен“, с дата на статуса today();
//			Въвежда се във вписването рег. номер и дата на заповедта за заличаване на спортното обединение.
		MMSsportFormirovanieDAO formirovanieDao = new MMSsportFormirovanieDAO(MMSsportFormirovanie.class, getUser());
		MMSVpisvaneDAO vpisvaneDao = new MMSVpisvaneDAO(getUser());
		MMSChlenstvoDAO chlenstvoDao = new MMSChlenstvoDAO(MMSChlenstvo.class, getUser());

		int i1 = 0;
		int i2 = 0;
		for (Object[] row : formirovaniaData) {
			boolean zalichen = row[4] != null && ((Number)row[4]).intValue() == DocuConstants.CODE_ZNACHENIE_STATUS_OBEKT_ZALICHEN;
			
			if (!zalichen && row[3] == null) { // да не е заличено формир и само ако няма други активни членства
				MMSsportFormirovanie formirovanie = formirovanieDao.findById(((Number)row[0]).intValue());
				
				formirovanie.setStatus(DocuConstants.CODE_ZNACHENIE_STATUS_OBEKT_ZALICHEN);
				formirovanie.setDateStatus(new Date());
				formirovanieDao.save(formirovanie);
				i1++;

				if (row[1] != null) { // по някаква причина ако няма вписване няма какво да правим
					MMSVpisvane vpisvane = vpisvaneDao.findById(((Number)row[1]).intValue());
					
					vpisvane.setStatusVpisvane(DocuConstants.CODE_ZNACHENIE_STATUS_REG_ZALICHENO_VPISVANE);
					vpisvane.setDateStatusVpisvane(new Date());
					vpisvane.setRnDocVpisvane((String) vpisvaneData[2]);
					vpisvane.setDateDocVpisvane((Date) vpisvaneData[3]);
					vpisvaneDao.save(vpisvane);
				}
			}

			MMSChlenstvo chlenstvo = chlenstvoDao.findById(((Number)row[2]).intValue());
			chlenstvo.setDateTermination((Date) vpisvaneData[3]);
			chlenstvoDao.save(chlenstvo);
			i2++;
		}
		return new int[] {i1, i2};
	}
}
