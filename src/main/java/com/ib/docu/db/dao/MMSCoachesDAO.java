package com.ib.docu.db.dao;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.docu.db.dto.EgovMessagesCoresp;
import com.ib.docu.db.dto.MMSCoaches;
import com.ib.docu.db.dto.MMSVidSportSC;
import com.ib.docu.db.dto.MMSVpisvane;
import com.ib.docu.db.dto.Referent;
import com.ib.docu.system.DocuConstants;
import com.ib.docu.system.SystemData;
import com.ib.system.ActiveUser;
import com.ib.system.db.AbstractDAO;
import com.ib.system.db.JPA;
import com.ib.system.db.SelectMetadata;
import com.ib.system.exceptions.DbErrorException;
import com.ib.system.exceptions.ObjectInUseException;
import com.ib.system.utils.DateUtils;
import com.ib.system.utils.SearchUtils;

public class MMSCoachesDAO extends AbstractDAO<MMSCoaches>{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MMSCoachesDAO.class);
	public MMSCoachesDAO(Class<MMSCoaches> typeClass, ActiveUser user) {
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
		MMSCoaches entity = findById(id);
		if (entity == null) {
			return;
		}
		try { 
			// mms_vid_sport
			List<MMSVidSportSC> listVidSport = createQuery("select t from MMSVidSportSC t where t.idObject = ?1 and t.tipОbject = ?2")
				.setParameter(1, entity.getId()).setParameter(2, DocuConstants.CODE_ZNACHENIE_JOURNAL_COACHES).getResultList();
			
			MMSVidSportDAO vidSportDao = new MMSVidSportDAO(MMSVidSportSC.class, getUser());
			for (MMSVidSportSC delme : listVidSport) {
				vidSportDao.delete(delme);
			}

		} catch (Exception e) {
			 throw new DbErrorException("Грешка при изтриване на свързани обекти за треньорски кадър!", e);
		}
		
		// mms_vpisvane_doc + mms_vpisvane
		// + doc + files + file_objects - и то само ако дока не се цитира другаде
		MMSVpisvaneDAO vpisvaneDao = new MMSVpisvaneDAO(getUser());
		List<MMSVpisvane> listVpisvane = vpisvaneDao.findRegsListByIdAndType(DocuConstants.CODE_ZNACHENIE_JOURNAL_COACHES, entity.getId());
		for (MMSVpisvane delme : listVpisvane) {
			vpisvaneDao.delete(delme);
		}
		
		// mms_coaches + mms_coaches_diploms
		super.delete(entity);

		// adm_referents + adm_ref_addrs (само ако не са цитирани другаде)
		Referent deleted = new ReferentDAO(getUser()).deleteIfNotUsed(entity.getIdObject());
		if (deleted != null) {
			sd.mergeReferentsClassif(deleted, true);
		}
	}

	/** 
	 * Tърсене по Номер/Рег номер на документ треньор, или code Referent
	 * 
	 * @param Integer nDoc
	 * @param Integer code Referent
	 * @return
	 * @throws DbErrorException
	 */
	@SuppressWarnings("unchecked")
	public Integer findByArg(String nDoc, Integer codeRef) throws DbErrorException{
		try {
			List<Integer> coachIds;
			Query query;
			StringBuilder sql = new StringBuilder("select mmsC.id from MMSCoaches mmsC where ");

			String arg1 = null;
			Integer arg2 = null;
			
			if (null != nDoc) {
				arg1=nDoc;
				sql.append(" mmsC.regNomer = :arg1 ");
			} else if (null!=codeRef){
				arg2 = codeRef;
				sql.append(" mmsC.idObject = :arg2 ");
			}
			if (arg1 == null && arg2 == null) { 
				return null;
			}
			if (null!=arg1) {
				query = createQuery(sql + " ").setParameter("arg1", arg1);
			}else {
				query = createQuery(sql + " ").setParameter("arg2", arg2);
			}
			coachIds = query.getResultList();
			
			if (null==coachIds || coachIds.isEmpty())
				return null;
				
			Integer coachId = (Integer) coachIds.get(0);

			return coachId;
			
						
		} catch (Exception e) {
			throw new DbErrorException("Грешка при търсене на треньор!", e);
		}

	}	
	
	public SelectMetadata buildQuery(String egn, String lnc, String nomDoc, String ime, String prez, String fam //
		, String rnZaiav, Date dateFromZaiav, Date dateToZaiav //
		, Integer status, Date dateFromStatus, Date dateToStatus
		, Integer statusVpis, Date fromStatusVpis, Date toStatusVpis, Integer statusZaiav, Date fromStatusZaiav, Date toStatusZaiav) {

		Map<String, Object> params = new HashMap<>();

		StringBuilder select = new StringBuilder();
		StringBuilder from = new StringBuilder();
		StringBuilder where = new StringBuilder();

		select.append(" select c.id a0, v.rn_doc_licenz a1, c.status a2, c.date_status a3 ");
		select.append(" , r.code a4, r.fzl_egn a5, r.fzl_lnc a6, r.nom_doc a7, r.ref_name a8 ");
		select.append(" , v.id a9, v.vid_sport a10, v.dlajnost a11, v.rn_doc_zaiavlenie a12, v.date_doc_zaiavlenie a13 ");
		
		select.append(" , case when (r.fzl_egn = '') IS FALSE then r.fzl_egn ");
		select.append(" when (r.fzl_lnc = '') IS FALSE then 'ЛНЧ ' || r.fzl_lnc ");
		select.append(" when (r.nom_doc = '') IS FALSE then 'НДС ' || r.nom_doc ");
		select.append(" else null end a14 ");

		select.append(" , r.ime a15, r.prezime a16, r.familia a17 ");

		select.append(" , v.status_vpisvane a18, v.date_status_vpisvane a19, v.status_result_zaiavlenie a20, v.date_status_zaiavlenie a21 ");
		select.append(" , z.USER_ID a22, z.LOCK_DATE a23 ");
		select.append(" , v.rn_doc_result a24, v.reason_vpisvane a25, v.rn_doc_licenz a26 ");

		from.append(" from mms_coaches c ");
		from.append(" inner join adm_referents r on r.code = c.id_object ");
		from.append(" left outer join mms_vpisvane v on v.id_object = c.id and v.type_object = :tipObj ");

		where.append(" where 1=1 ");
		params.put("tipObj", DocuConstants.CODE_ZNACHENIE_JOURNAL_COACHES);

		String t = SearchUtils.trimToNULL(egn);
		if (t != null) {
			where.append(" and r.fzl_egn like :egn ");
			params.put("egn", "%" + t + "%");
		}
		t = SearchUtils.trimToNULL(lnc);
		if (t != null) {
			where.append(" and r.fzl_lnc like :lnc ");
			params.put("lnc", "%" + t + "%");
		}
		t = SearchUtils.trimToNULL(nomDoc);
		if (t != null) {
			where.append(" and r.nom_doc like :nomDoc ");
			params.put("nomDoc", "%" + t + "%");
		}

		t = SearchUtils.trimToNULL_Upper(ime);
		if (t != null) {
			where.append(" and upper(r.ime) like :ime ");
			params.put("ime", "%" + t + "%");
		}
		t = SearchUtils.trimToNULL_Upper(prez);
		if (t != null) {
			where.append(" and upper(r.prezime) like :prez ");
			params.put("prez", "%" + t + "%");
		}
		t = SearchUtils.trimToNULL_Upper(fam);
		if (t != null) {
			where.append(" and upper(r.familia) like :fam ");
			params.put("fam", "%" + t + "%");
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
			where.append(" and c.status = :status ");
			params.put("status", status);
		}
		if (dateFromStatus != null) {
			where.append(" and c.date_status >= :dateFromStatus ");
			params.put("dateFromStatus", DateUtils.startDate(dateFromStatus));
		}
		if (dateToStatus != null) {
			where.append(" and c.date_status <= :dateToStatus ");
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

		from.append(" left outer join LOCK_OBJECTS z on z.OBJECT_TIP = :zTip and z.OBJECT_ID = c.id and z.USER_ID != :zUser ");
		params.put("zTip", DocuConstants.CODE_ZNACHENIE_JOURNAL_COACHES);
		params.put("zUser", getUserId());

		SelectMetadata sm = new SelectMetadata();
		sm.setSqlCount(" select count(*) " + from + where);
		sm.setSql(select.toString() + from.toString() + where.toString());
		sm.setSqlParameters(params);

		return sm;
	}
	
	
	/** 
	 * Tърсене на Coresp from EgovMessagesCoresp по idMessage
	 * 
	 * @param Integer idMessage
	 * @throws DbErrorException
	 */
	public List<EgovMessagesCoresp> findCorespByIdMessage(Integer idMessage) throws DbErrorException {
		
		try {
			@SuppressWarnings("unchecked")
			List<EgovMessagesCoresp> corespList = createQuery(
				"select x from EgovMessagesCoresp x where x.idMessage = ?1 order by x.id")
				.setParameter(1, idMessage).getResultList();

			return corespList;

		} catch (Exception e) {
			throw new DbErrorException("Грешка при търсене на кореспонденти от съобщение!", e);
		}
	}
	
	
	/** Метод за връщане на вписванията към обект
	 * @param codeObject - Код на обект
	 * @param idObject - ИД на обект
	 *  @param vidSport - code vid sportт
	 * @return Списък вписвания
	 * @throws DbErrorException - грешка при работа с БД
	 */
	@SuppressWarnings("unchecked")
	public List<MMSVpisvane> findVpisvListByIdTypeVidSport(Integer typeObject, Integer idObject, Integer vidSport) throws DbErrorException{
		
		try {
			
			String sql = " SELECT V FROM MMSVpisvane V WHERE V.typeObject = :TO AND V.idObject = :IO AND V.vidSport = :VS ORDER BY V.dateStatusZaiavlenie DESC ";	
			
			Query q = JPA.getUtil().getEntityManager().createQuery(sql);
			q.setParameter("TO", typeObject);
			q.setParameter("IO", idObject);
			q.setParameter("VS", vidSport);
			
			return q.getResultList();
		
		} catch (Exception e) {
			LOGGER.debug("Грешка при извличане на списък с вписванията към обект по ид на обект", e);
			throw new DbErrorException("Грешка при извличане на списък с вписванията към обект по ид на обект - " + e.getLocalizedMessage(), e);
		}
	}
	
	/** Метод за връщане на вписванията към обект по подадени параметри
	 * @param typeObject - Код на обекта
	 * @param idObject - ИД на обекта
	 * @param vidSport - вид спорт
	 * @param dlajnost - длъжност
	 * @return
	 * @throws DbErrorException
	 */
	@SuppressWarnings("unchecked")
	public List<MMSVpisvane> findVpisvListByIdTypeVidSportAndDlaj(Integer typeObject, Integer idObject, Integer vidSport, Integer dlajnost) throws DbErrorException{
		
		try {
			
			String sql = " SELECT V FROM MMSVpisvane V WHERE V.typeObject = :TO AND V.idObject = :IO AND V.vidSport = :VS AND V.dlajnost = :D ORDER BY V.dateStatusZaiavlenie DESC ";	
			
			Query q = JPA.getUtil().getEntityManager().createQuery(sql);
			q.setParameter("TO", typeObject);
			q.setParameter("IO", idObject);
			q.setParameter("VS", vidSport);
			q.setParameter("D", dlajnost);
			
			return q.getResultList();
		
		} catch (Exception e) {
			LOGGER.debug("Грешка при извличане на списък с вписванията към обект по ид на обект, вид спорт и длъжност", e);
			throw new DbErrorException("Грешка при извличане на списък с вписванията към обект по ид на обект, вид спорт и длъжност - " + e.getLocalizedMessage(), e);
		}
	}

}
