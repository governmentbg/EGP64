package com.ib.docu.transform;

import static com.ib.docu.system.DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORT_OBED;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.hibernate.jpa.TypedParameterValue;
import org.hibernate.type.StandardBasicTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.docu.db.dao.MMSCoachesDAO;
import com.ib.docu.db.dao.MMSSportObektDAO;
import com.ib.docu.db.dao.MMSsportFormirovanieDAO;
import com.ib.docu.db.dao.MMSsportObedinenieDAO;
import com.ib.docu.db.dao.ReferentDAO;
import com.ib.docu.db.dto.MMSCoaches;
import com.ib.docu.db.dto.MMSSportObekt;
import com.ib.docu.db.dto.MMSSportnoObedinenie;
import com.ib.docu.db.dto.MMSsportFormirovanie;
import com.ib.docu.db.dto.Referent;
import com.ib.docu.system.DocuConstants;
import com.ib.system.ActiveUser;
import com.ib.system.SysConstants;
import com.ib.system.db.AbstractDAO;
import com.ib.system.db.JPA;
import com.ib.system.db.PersistentEntity;
import com.ib.system.utils.SearchUtils;
import com.ib.system.utils.StringUtils;
import com.ib.system.utils.ValidationUtils;

/**
 * Някакви, които са общи за всички и ще се пускат като крайни действия.
 *
 * @author belev
 */
public class T5Final {

	private static final Logger LOGGER = LoggerFactory.getLogger(T5Final.class);

	/** @param args */
	public static void main(String[] args) {
		// 0. да се генерира удост. док за всчики които трябва и нямат
		int register = 5;
		generateUdostDoc(DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORT_OBED //
				, DocuConstants.CODE_ZNACHENIE_VID_DOC_SPORT_LICENZ, "Спортен лиценз или Удостоверение за вписване на НОУС/ОСЦ", register);

		generateUdostDoc(DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_FORMATIONS //
				, DocuConstants.CODE_ZNACHENIE_VID_DOC_UDOST_REG_SPORT_FORMIROV, "Удостоверение за регистрация на спортно формирование", register);

		generateUdostDoc(DocuConstants.CODE_ZNACHENIE_JOURNAL_COACHES //
				, DocuConstants.CODE_ZNACHENIE_VID_DOC_UDOST_REG_TREN_KADAR, "Удостоверение за регистрация на треньорски кадри", register);

		// 1. Изчисляване на дата на раждане и пол за лицата, които имат валидно ЕГН
		calcDataByEGN();

		// FIXME Журналирането на текущуте данни за момента го задържам и да се види как да се запали REGIX
		
//		// 2. Журналира данните за всички НФЛ, за да има после как да се сравняват след корекциите на регикс
//		journalReferent(DocuConstants.CODE_ZNACHENIE_REF_TYPE_NFL);
//
//		// 3. Журналира данните за всички ФЗЛ, за да има после как да се сравняват след корекциите на регикс
//		journalReferent(DocuConstants.CODE_ZNACHENIE_REF_TYPE_FZL);
//
//		// 4. обединения
//		journalObjects("mms_sport_obedinenie", DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORT_OBED //
//				, new MMSsportObedinenieDAO(MMSSportnoObedinenie.class, ActiveUser.DEFAULT));
//
//		// 5. формирования
//		journalObjects("mms_sport_formirovanie", DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_FORMATIONS //
//				, new MMSsportFormirovanieDAO(MMSsportFormirovanie.class, ActiveUser.DEFAULT));
//
//		// 6. спортни обекти
//		journalObjects("mms_sport_obekt", DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_OBJECTS //
//				, new MMSSportObektDAO(MMSSportObekt.class, ActiveUser.DEFAULT));
//
//		// 6. трен.кадри
//		journalObjects("mms_sport_obekt", DocuConstants.CODE_ZNACHENIE_JOURNAL_COACHES //
//				, new MMSCoachesDAO(MMSCoaches.class, ActiveUser.DEFAULT));
	}

	static void calcDataByEGN() {
		String egn = null;
		try {
			@SuppressWarnings("unchecked")
			List<Object[]> list = JPA.getUtil().getEntityManager().createNativeQuery( //
					"select ref_id, fzl_egn, fzl_pol, fzl_birth_date from adm_referents where fzl_egn is not null and fzl_egn <> ''") //
					.getResultList();

			Query query = JPA.getUtil().getEntityManager().createNativeQuery( //
					"update adm_referents set fzl_pol = ?1, fzl_birth_date = ?2 where ref_id = ?3");

			int cnt = 0;
			JPA.getUtil().begin();

			for (Object[] row : list) {
				egn = SearchUtils.trimToNULL((String) row[1]);

				if (!ValidationUtils.isValidEGN(egn)) {
					LOGGER.warn("NOT VALID EGN={}", egn);
					continue;
				}
				int i = Integer.parseInt(String.valueOf(egn.charAt(8)));
				Integer pol = i % 2 == 0 ? 1 : 2;
				Date date = StringUtils.birthdayFromEGN(egn);

				query.setParameter(1, pol);
				query.setParameter(2, date);
				query.setParameter(3, row[0]);

				query.executeUpdate();
				cnt++;
			}
			JPA.getUtil().commit();

			LOGGER.info("Calculate {} fzl_birth_date & fzl_pol", cnt);

		} catch (Exception e) {
			LOGGER.error("Грешка при изчисляване на дата на раждане и пол по ЕГН=" + egn, e);
		} finally {
			JPA.getUtil().closeConnection();
		}
	}

	static void generateUdostDoc(int typeObj, int docVid, String docVidText, int register) {
		String objTable;
		if (typeObj == DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORT_OBED) {
			objTable = "mms_sport_obedinenie";
		} else if (typeObj == DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_FORMATIONS) {
			objTable = "mms_sport_formirovanie";
		} else if (typeObj == DocuConstants.CODE_ZNACHENIE_JOURNAL_COACHES) {
			objTable = "mms_coaches";
		} else {
			objTable = "error";
		}

		try {
			EntityManager em = JPA.getUtil().getEntityManager();

			Query query = em.createNativeQuery("insert into doc (" //
					+ " doc_id, registratura_id, register_id, code_ref_corresp, rn_doc, rn_prefix, rn_pored, guid, doc_type, doc_vid" //
					+ ", doc_date, otnosno, valid, valid_date, processed, free_access, count_files, user_reg, date_reg, competence )" //
					+ " values (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9, ?10, ?11, ?12, ?13, ?14, ?15, ?16, ?17, ?18, ?19, ?20)");

			Query vpisvaneDocQuery = em.createNativeQuery("insert into mms_vpisvane_doc (" //
					+ " id, id_vpisvane, id_doc, user_reg, date_reg, id_object, type_object ) " //
					+ "values (?1, ?2, ?3, ?4, ?5, ?6, ?7)");

			StringBuilder sql = new StringBuilder();
			sql.append(" select distinct v.id, v.rn_doc_licenz, v.date_doc_licenz, r.ref_name, obj.id obj_id ");
			sql.append(" from mms_vpisvane v ");
			sql.append(" inner join " + objTable + " obj on obj.id = v.id_object ");
			sql.append(" inner join adm_referents r on r.code = obj.id_object ");
			sql.append(" where v.rn_doc_licenz is not null and v.date_doc_licenz is not null ");
			sql.append(" and v.type_object = ?1 ");
			sql.append(" and not exists (select 1 from mms_vpisvane_doc vd inner join doc d on d.doc_id = vd.id_doc and d.doc_vid = ?2 where vd.id_vpisvane = v.id) ");
			sql.append(" order by 1 ");

			@SuppressWarnings("unchecked")
			List<Object[]> rows = em.createNativeQuery(sql.toString()) //
					.setParameter(1, typeObj).setParameter(2, docVid) //
					.getResultList();

			JPA.getUtil().begin();

			for (Object[] row : rows) {
				Integer docId = T0Start.nextVal("seq_doc", em);

				query.setParameter(1, docId); // doc_id
				query.setParameter(2, T0Start.REGISTATRURA); // registratura_id
				query.setParameter(3, register); // register_id
				query.setParameter(4, new TypedParameterValue(StandardBasicTypes.INTEGER, null)); // code_ref_corresp
				query.setParameter(5, row[1]); // rn_doc
				query.setParameter(6, ""); // rn_prefix
				query.setParameter(7, new TypedParameterValue(StandardBasicTypes.INTEGER, null)); // rn_pored
				query.setParameter(8, "{" + UUID.randomUUID().toString().toUpperCase() + "}"); // guid
				query.setParameter(9, DocuConstants.CODE_ZNACHENIE_DOC_TYPE_OWN); // doc_type
				query.setParameter(10, docVid); // doc_vid
				query.setParameter(11, row[2]); // doc_date

				query.setParameter(12, docVidText + " на " + row[3]); // otnosno
				query.setParameter(13, SysConstants.CODE_ZNACHENIE_DA); // valid
				query.setParameter(14, row[2]); // valid_date
				query.setParameter(15, SysConstants.CODE_ZNACHENIE_DA); // processed
				query.setParameter(16, SysConstants.CODE_ZNACHENIE_DA); // free_access
				query.setParameter(17, 0); // count_files
				query.setParameter(18, T0Start.USER); // user_reg
				query.setParameter(19, row[2]); // date_reg
				query.setParameter(20, SysConstants.CODE_ZNACHENIE_DA); // competence
				query.executeUpdate();

				//
				Integer id = T0Start.nextVal("seq_mms_vpisvane_doc", em);

				vpisvaneDocQuery.setParameter(1, id); // id
				vpisvaneDocQuery.setParameter(2, row[0]); // id_vpisvane
				vpisvaneDocQuery.setParameter(3, docId); // id_doc
				vpisvaneDocQuery.setParameter(4, T0Start.USER); // user_reg
				vpisvaneDocQuery.setParameter(5, row[2]); // date_reg
				vpisvaneDocQuery.setParameter(6, row[4]); // id_object
				vpisvaneDocQuery.setParameter(7, typeObj); // type_object
				vpisvaneDocQuery.executeUpdate();

			}
			JPA.getUtil().commit();

		} catch (Exception e) {
			JPA.getUtil().rollback();
			LOGGER.error("Грешка при генериране н УД typeObj=" + typeObj + " с docVid=" + docVid, e);
		} finally {
			JPA.getUtil().closeConnection();
		}
	}

	@SuppressWarnings("unchecked")
	static <E extends PersistentEntity> void journalObjects(String tableName, Integer codeObject, AbstractDAO<E> dao) {
		Integer id = null;
		try {
			StringBuilder sql = new StringBuilder();
			sql.append(" select distinct obj.id ");
			sql.append(" from " + tableName + " obj ");
			sql.append(" left outer join system_journal j on j.id_object = obj.id and j.code_object = " + codeObject + " ");
			sql.append(" where j.id is null "); // идеята е да няма вече журнал за това
			sql.append(" order by 1 ");

			List<Object> codes = JPA.getUtil().getEntityManager().createNativeQuery( //
					sql.toString()).getResultList();
			JPA.getUtil().closeConnection();

			Date now = new Date();

			int cnt = 0;
			for (Object x : codes) {
				id = ((Number) x).intValue();

				PersistentEntity entity = dao.findById(id);
				if (entity == null) {
					continue;
				}

				entity.setDateLastMod(now);

				JPA.getUtil().begin();
				dao.save((E) entity);
				JPA.getUtil().commit();
				JPA.getUtil().closeConnection();

				cnt++;

				if (cnt % 50 == 0) {
					LOGGER.info("{}", cnt);
				}
			}
			LOGGER.info("Journal {} {} (code_object={}) ", cnt, tableName, codeObject);

		} catch (Exception e) {
			LOGGER.error("Грешка при журналиране на " + tableName + " с ИД=" + id, e);
		} finally {
			JPA.getUtil().closeConnection();
		}
	}

	static void journalReferent(Integer refType) {
		Integer code = null;
		try {
			StringBuilder sql = new StringBuilder();
			sql.append(" select distinct r.code ");
			sql.append(" from adm_referents r ");
			sql.append(" left outer join system_journal j on j.id_object = r.code and j.code_object = 52 ");
			sql.append(" where r.ref_type = " + refType + " and j.id is null "); // идеята е да няма вече журнал за това
			sql.append(" order by 1 ");

			@SuppressWarnings("unchecked")
			List<Object> codes = JPA.getUtil().getEntityManager().createNativeQuery( //
					sql.toString()).getResultList();
			JPA.getUtil().closeConnection();

			ReferentDAO dao = new ReferentDAO(ActiveUser.DEFAULT);
			Date now = new Date();

			int cnt = 0;
			for (Object x : codes) {
				code = ((Number) x).intValue();

				Referent referent = dao.findByCodeRef(code);
				if (referent == null) {
					continue;
				}

				referent.setDateLastMod(now);

				JPA.getUtil().begin();
				dao.save(referent);
				JPA.getUtil().commit();
				JPA.getUtil().closeConnection();

				cnt++;

				if (cnt % 50 == 0) {
					LOGGER.info("{}", cnt);
				}
			}
			LOGGER.info("Journal {} Referents (ref_type={}) ", cnt, refType);

		} catch (Exception e) {
			LOGGER.error("Грешка при журналиране на ЛИЦЕ с КОД=" + code, e);
		} finally {
			JPA.getUtil().closeConnection();
		}
	}
}
