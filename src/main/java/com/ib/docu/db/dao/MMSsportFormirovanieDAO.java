package com.ib.docu.db.dao;

import static com.ib.system.SysConstants.CODE_CLASSIF_EKATTE;
import static com.ib.system.SysConstants.CODE_DEFAULT_LANG;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;
import javax.persistence.TypedQuery;

import com.ib.docu.db.dto.EgovMessagesCoresp;
import com.ib.docu.db.dto.MMSChlenstvo;
import com.ib.docu.db.dto.MMSVpisvane;
import com.ib.docu.db.dto.MMSsportFormirovanie;
import com.ib.docu.db.dto.Referent;
import com.ib.docu.system.DocuConstants;
import com.ib.docu.system.SystemData;
import com.ib.system.ActiveUser;
import com.ib.system.BaseSystemData;
import com.ib.system.SysConstants; 
import com.ib.system.db.AbstractDAO;
import com.ib.system.db.DialectConstructor;
import com.ib.system.db.JPA;
import com.ib.system.db.SelectMetadata;
import com.ib.system.db.dto.SystemClassif;
import com.ib.system.exceptions.DbErrorException;
import com.ib.system.exceptions.ObjectInUseException;
import com.ib.system.utils.DateUtils;
import com.ib.system.utils.SearchUtils;


public class MMSsportFormirovanieDAO extends AbstractDAO<MMSsportFormirovanie> {

	public MMSsportFormirovanieDAO(Class<MMSsportFormirovanie> typeClass, ActiveUser user) {
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
		MMSsportFormirovanie entity = findById(id);
		if (entity == null) {
			return;
		}
		try {
			// mms_chlenstvo
			List<MMSChlenstvo> listChlenstvo = createQuery("select t from MMSChlenstvo t where t.idObject = ?1 and t.typeObject = ?2")
				.setParameter(1, entity.getId()).setParameter(2, DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_FORMATIONS).getResultList();
			
			MMSChlenstvoDAO chlenstvoDao = new MMSChlenstvoDAO(MMSChlenstvo.class, getUser());
			for (MMSChlenstvo delme : listChlenstvo) {
				chlenstvoDao.delete(delme);
			}

		} catch (Exception e) {
			 throw new DbErrorException("Грешка при изтриване на свързани обекти за спортно формирование!", e);
		}
		
		// mms_vpisvane_doc + mms_vpisvane
		// + doc + files + file_objects - и то само ако дока не се цитира другаде
		MMSVpisvaneDAO vpisvaneDao = new MMSVpisvaneDAO(getUser());
		List<MMSVpisvane> listVpisvane = vpisvaneDao.findRegsListByIdAndType(DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_FORMATIONS, entity.getId());
		for (MMSVpisvane delme : listVpisvane) {
			vpisvaneDao.delete(delme);
		}
		
		// mms_sport_formirovanie + mms_vid_sport 
		super.delete(entity);

		// adm_referents + adm_ref_addrs (само ако не са цитирани другаде)
		Referent deleted = new ReferentDAO(getUser()).deleteIfNotUsed(entity.getIdObject());
		if (deleted != null) {
			sd.mergeReferentsClassif(deleted, true);
		}
	}
	
	public List<MMSChlenstvo> findByIdObject(Integer idObj, Integer typeObj) {
		
		String sql = " select m from MMSChlenstvo m where m.idObject = :idObj and m.typeObject = :typeObj";
		TypedQuery<MMSChlenstvo> q = JPA.getUtil().getEntityManager().createQuery(sql, MMSChlenstvo.class);
		q.setParameter("idObj", idObj);
		q.setParameter("typeObj", typeObj);
		
		return q.getResultList();
	}
	
	public int deleteByIdObject(Integer idObj, Integer typeObj) {
		Query q =  JPA.getUtil().getEntityManager().createQuery(" delete from MMSChlenstvo where idObject = :idObj and typeObject = :typeObj");
		q.setParameter("idObj", idObj);
		q.setParameter("typeObj", typeObj);
		return q.executeUpdate();
	}
	
	@SuppressWarnings("unchecked")
	public List<MMSsportFormirovanie> findByRegNom(String regNom) {
		Query q = JPA.getUtil().getEntityManager().createQuery("select sf from MMSsportFormirovanie sf where sf.regNomer = :regNom ").setParameter("regNom", regNom);
		return q.getResultList();
	}
	
	public MMSsportFormirovanie findByIdObject(Integer idObject) {
		Query q = JPA.getUtil().getEntityManager().createQuery("select sf from MMSsportFormirovanie sf where sf.idObject = :idObject ").setParameter("idObject", idObject);
		MMSsportFormirovanie r;
		try {
			r = (MMSsportFormirovanie) q.getSingleResult();
		} catch (Exception e) {
			r=null;
		}
		
		return r;
	}
	
	public void updateRegNomSF(Integer idSF, String regNom) throws DbErrorException {

		try {
			
			Query query = createNativeQuery( "update mms_sport_formirovanie set reg_nomer = :regNom, user_last_mod = :userId, date_last_mod = :date where id = :idSF");
			
			query.setParameter("idSF", idSF);
			query.setParameter("regNom", regNom);
			query.setParameter("userId", getUserId());
			query.setParameter("date", new Date());
			
			query.executeUpdate();
		
		} catch (Exception e) {
			throw new DbErrorException("Грешка при запис на регистров номер на спортно формирование!", e);
		}
	}

	public SelectMetadata buildQuery(String eik, String name, Integer ekatte, Integer vidFormirovanie, List<Integer> vidSportList //
		, String rnZaiav, Date dateFromZaiav, Date dateToZaiav //
		, Integer status, Date dateFromStatus, Date dateToStatus //
		, Integer olimp, BaseSystemData sd
		, Integer statusVpis, Date fromStatusVpis, Date toStatusVpis, Integer statusZaiav, Date fromStatusZaiav, Date toStatusZaiav
		, Integer regixDiff
		, Integer univers, Integer voenen) throws DbErrorException {

		// TODO voenen за да работи му трябва колона и да се изчислява - миграция !!!
		
		String dialect = JPA.getUtil().getDbVendorName();

		Map<String, Object> params = new HashMap<>();

		StringBuilder select = new StringBuilder();
		StringBuilder from = new StringBuilder();
		StringBuilder where = new StringBuilder();

		select.append(" select  f.id a0, max( f.reg_nomer) a1, max( f.status) a2, max(f.date_status) a3 ");
		select.append(" , max(r.code) a4, max(r.nfl_eik) a5, max(r.ref_name) a6 ");
//		select.append(" , max(v.id) a7, max(v.rn_doc_zaiavlenie) a8, max(v.date_doc_zaiavlenie) a9 ");
		select.append(" , null a7, null a8, null a9 ");
		select.append(" , max(f.vid) a10, max(f.type_sport) a11, max(att.ekatte) a12, ");
		select.append(" max( " );
		select.append( DialectConstructor.convertToDelimitedString(dialect, "vs.vid_sport",
			"mms_vid_sport vs where vs.tip_object = " + DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_FORMATIONS + " and vs.id_object = f.id", "vs.id") + " ) a13");

//		select.append(" , max(v.status_vpisvane) a14, max(v.date_status_vpisvane) a15, max(v.status_result_zaiavlenie) a16, max(v.date_status_zaiavlenie) a17 ");
		select.append(" , null a14, null a15, null a16, null a17 ");
		select.append(" , max(att.tvm || ' ' || att.ime) a18, max(att.obstina_ime) a19, max(att.oblast_ime) a20 ");
		select.append(" , max(z.USER_ID) a21, max(z.LOCK_DATE) a22 ");
		select.append(" , max(f.univers) a23, max(f.voenen_sport) a24 ");

		from.append(" from mms_sport_formirovanie f ");
		from.append(" inner join adm_referents r on r.code = f.id_object ");
		from.append(" left outer join mms_vpisvane v on v.id_object = f.id and v.type_object = :tipObj ");

		from.append(" left outer join adm_ref_addrs a on a.code_ref = r.code and a.addr_type = " + DocuConstants.CODE_ZNACHENIE_ADDR_TYPE_CORRESP);
		from.append(" left outer join ekatte_att att on att.ekatte = a.ekatte ");

		where.append(" where 1=1 ");
		params.put("tipObj", DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_FORMATIONS);

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

		if (vidFormirovanie != null) {
			where.append(" and f.vid = :vidFormirovanie ");
			params.put("vidFormirovanie", vidFormirovanie);
		}
		if (vidSportList != null && !vidSportList.isEmpty()) {
			where.append(" and EXISTS (select s.id from mms_vid_sport s where s.tip_object = :tipObj and s.id_object = f.id and s.vid_sport in (:vidSportList)) ");
			params.put("vidSportList", vidSportList);
		}
		if (olimp != null) {
			if (olimp.equals(SysConstants.CODE_ZNACHENIE_NE)) {
				where.append(" and (f.type_sport is null or f.type_sport = :olimp) ");
			} else {
				where.append(" and f.type_sport = :olimp ");
			}
			params.put("olimp", olimp);
		}
		if (univers != null) {
			if (univers.equals(SysConstants.CODE_ZNACHENIE_NE)) {
				where.append(" and (f.univers is null or f.univers = :universArg) ");
			} else {
				where.append(" and f.univers = :universArg ");
			}
			params.put("universArg", univers);
		}
		if (voenen != null) {
			if (voenen.equals(SysConstants.CODE_ZNACHENIE_NE)) {
				where.append(" and (f.voenen_sport is null or f.voenen_sport = :voenen) ");
			} else {
				where.append(" and f.voenen_sport = :voenen ");
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
			where.append(" and f.status = :status ");
			params.put("status", status);
		}
		if (dateFromStatus != null) {
			where.append(" and f.date_status >= :dateFromStatus ");
			params.put("dateFromStatus", DateUtils.startDate(dateFromStatus));
		}
		if (dateToStatus != null) {
			where.append(" and f.date_status <= :dateToStatus ");
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

		from.append(" left outer join LOCK_OBJECTS z on z.OBJECT_TIP = :zTip and z.OBJECT_ID = f.id and z.USER_ID != :zUser ");
		params.put("zTip", DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_FORMATIONS);
		params.put("zUser", getUserId());
		
		SelectMetadata sm = new SelectMetadata();
		sm.setSqlCount(" select count(distinct f.id) " + from + where );
		where.append(" group by f.id ");
		sm.setSql(select.toString() + from.toString() + where.toString());
		sm.setSqlParameters(params);

		return sm;
	}
	
	public EgovMessagesCoresp findByIdMessage(Integer idMessage) {
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
