package com.ib.docu.transform;

import static com.ib.docu.system.DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORT_OBED;
import static com.ib.docu.system.DocuConstants.CODE_ZNACHENIE_REF_TYPE_NFL;
import static com.ib.docu.system.DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_NOSTD;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.hibernate.jpa.TypedParameterValue;
import org.hibernate.type.StandardBasicTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.docu.system.DocuConstants;
import com.ib.system.SysConstants;
import com.ib.system.db.JPA;
import com.ib.system.utils.SearchUtils;
import com.ib.system.utils.ValidationUtils;

/**
 * ММС – миграция на спортни обединения НОСТ
 *
 * @author belev
 */
public class T1RegSportFederationsNostd {

	private static final Logger LOGGER = LoggerFactory.getLogger(T1RegSportFederationsNostd.class);

	/**  */
	private static final String REF_MIG_NAME = "reg_nostd"; // reg_nostd

	/** @param args */
	public static void main(String[] args) {
		T1RegSportFederationsNostd t = new T1RegSportFederationsNostd();

		t.validate(JPA.getUtil());

//		t.clear(JPA.getUtil());

		t.transfer(JPA.getUtil());

//		t.regix(JPA.getUtil());

		System.exit(0); // не е ясно защо не терминира ако го няма
	}

	private Map<String, Integer>	ekatteMap;

	private String	eik;
	private String	regNom;
	private String	zap;

	private Date	splicDate;
	private String	splicNom;

	private String	chairman;
	private String	director;
	private String	reprezent;
	private Integer	polzaCode;
	private String	refName;
	private String	email;

	private Integer	statusVpisvane;

	/**  */
	public T1RegSportFederationsNostd() {
		this.ekatteMap = T0Start.findEkatteMap(JPA.getUtil().getEntityManager());
	}

	/** @param jpa */
	public void clear(JPA jpa) {
		LOGGER.info("");
		LOGGER.info("Start clear register data");

		try {
			EntityManager em = jpa.getEntityManager();

			jpa.begin();
			int cnt = em.createNativeQuery("delete from adm_ref_addrs where code_ref in (" //
				+ "select code from adm_referents where mig_login_name = ?1 and code in (select id_object from mms_sport_obedinenie))") //
				.setParameter(1, REF_MIG_NAME).executeUpdate();
			jpa.commit();
			LOGGER.info("   deleted " + cnt + " rows from table adm_ref_addrs");

			jpa.begin();
			cnt = em.createNativeQuery( //
				"delete from adm_referents where mig_login_name = ?1 and code in (select id_object from mms_sport_obedinenie)") //
				.setParameter(1, REF_MIG_NAME).executeUpdate();
			jpa.commit();
			LOGGER.info("   deleted " + cnt + " rows from table adm_referents");
			
			LOGGER.info("Clear register data - COMPLETE");

			// !!! оправям и броячите за изтритите таблици
			recreateSequences(jpa);

		} catch (Exception e) {
			jpa.rollback();

			LOGGER.error("System ERROR clear register! -> " + e.getMessage(), e);
		} finally {
			jpa.closeConnection();
		}
	}

	/** @param jpa */
	public void regix(JPA jpa) {
		// regix
	}

	/** @param jpa */
	@SuppressWarnings("unchecked")
	public void transfer(JPA jpa) {
		LOGGER.info("");
		LOGGER.info("Start transfer register data");

		try {
			EntityManager em = jpa.getEntityManager();

			Query referentQuery = em.createNativeQuery("insert into adm_referents (" //
				+ " ref_id, code, code_parent, ref_type, ref_name, date_ot, date_do, user_reg, date_reg" //
				+ ", ref_grj, nfl_eik, contact_phone, contact_email, web_page, polza, ref_latin, mig_login_name, predstavitelstvo )" //
				+ " values (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9, ?10, ?11, ?12, ?13, ?14, ?15, ?16, ?17, ?18)");

			Query addrQuery = em.createNativeQuery("insert into adm_ref_addrs (" //
				+ " addr_id, code_ref, addr_type, addr_country, addr_text, post_code, post_box, ekatte, raion, user_reg, date_reg )" //
				+ " values (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9, ?10, ?11)");

			Query obedinenieQuery = em.createNativeQuery("insert into mms_sport_obedinenie (" //
				+ " id, type_sport, reg_nomer, id_object, vid, predstavitelstvo, predsedatel, gen_sek_direktor, br_chlenove" //
				+ ", user_reg, date_reg, status, date_status, dop_info )" //
				+ " values (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9, ?10, ?11, ?12, ?13, ?14)");

			Query obedinenieMfQuery = em.createNativeQuery("insert into mms_sport_obed_mf (" //
				+ " id, id_sport_obed, mejd_fed, date_beg, date_end, reg_nomer, date_doc, user_reg, date_reg )" //
				+ " values (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9)");

			Query vpisvaneQuery = em.createNativeQuery("insert into mms_vpisvane (" //
				+ " id, type_object, id_object, rn_doc_zaiavlenie, date_doc_zaiavlenie, status_result_zaiavlenie" //
				+ ", reason_result, reason_result_text, rn_doc_result, date_doc_result, rn_doc_licenz, date_doc_licenz" //
				+ ", status_vpisvane, reason_vpisvane, reason_vpisvane_text, rn_doc_vpisvane, date_doc_vpisvane" //
				+ ", nachin_poluchavane, addr_mail_poluchavane, date_status_zaiavlenie, vid_sport, dlajnost, dop_info" //
				+ ", user_reg, date_reg, date_status_vpisvane )" //
				+ " values (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9, ?10, ?11, ?12, ?13, ?14, ?15, ?16, ?17, ?18, ?19" //
				+ ", ?20, ?21, ?22, ?23, ?24, ?25, ?26)");

			Query docQuery = em.createNativeQuery("insert into doc (" //
				+ " doc_id, registratura_id, register_id, code_ref_corresp, rn_doc, rn_prefix, rn_pored, guid, doc_type, doc_vid" //
				+ ", doc_date, otnosno, valid, valid_date, processed, free_access, count_files, user_reg, date_reg, competence )" //
				+ " values (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9, ?10, ?11, ?12, ?13, ?14, ?15, ?16, ?17, ?18, ?19, ?20)");

			Query vpisvaneDocQuery = em.createNativeQuery("insert into mms_vpisvane_doc (" //
				+ " id, id_vpisvane, id_doc, user_reg, date_reg, id_object, type_object ) " //
				+ "values (?1, ?2, ?3, ?4, ?5, ?6, ?7)");

			Query checkReferentQuery = em.createNativeQuery("select ref_id, code, ref_name from adm_referents where ref_type = ?1 and nfl_eik = ?2") //
				.setParameter(1, CODE_ZNACHENIE_REF_TYPE_NFL);

			Query checkObedinenieQuery = em.createNativeQuery("select id from mms_sport_obedinenie where id_object = ?1 and reg_nomer = ?2");

			Query checkVpisvaneQuery = em.createNativeQuery("select id from mms_vpisvane where type_object = ?1 and id_object = ?2") //
				.setParameter(1, CODE_ZNACHENIE_JOURNAL_SPORT_OBED);

			StringBuilder select = new StringBuilder();
			select.append(" select reg_nom id, zap_nom, reg_nom || '' splic_nom, dat_izd, eik, reg_nom || '' reg_nom, '' chairman "); // 0,1,2,3,4,5,6
			select.append(" , '' director, represent, '' sports, email, '' mso, '' polza_tr "); // 7,8,9,10,11,12
			select.append(" , name, '1000' pk, head, '-' adres, '22' obl, 'Столична' obst, tel, web_adr "); // 13,14,15,16,17,18,19,20
			select.append(" from reg_nostd ");
			select.append(" order by 1 ");

			List<Object[]> rows = em.createNativeQuery(select.toString()).getResultList();

			jpa.begin();
			for (Object[] row : rows) {

				setupData(row); // за да се намерят данните !!! МНОГО ВАЖНО !!!

				if (this.eik == null || this.regNom == null) {
					continue;
				}

				List<Object[]> referent = checkReferentQuery.setParameter(2, this.eik).getResultList();

				if (referent.isEmpty()) { // запис на всичко
					Integer codeRef = saveReferent(referentQuery, addrQuery, row, em); // adm_referents (4.1)

					Integer obedinenieId = saveObedinenie(obedinenieQuery, obedinenieMfQuery, row, codeRef, em); // mms_sport_obedinenie
																													// (4.2)
					Integer vpisvaneId = saveVpisvane(vpisvaneQuery, obedinenieId, em); // mms_vpisvane (4.4)

					Integer docId = saveDoc(docQuery, codeRef, em); // doc (4.5)

					saveVpisvaneDoc(vpisvaneDocQuery, vpisvaneId, docId, obedinenieId, em); // mms_vpisvane_doc (4.6)

				} else { // допълнителна логика
					Integer codeRef = SearchUtils.asInteger(referent.get(0)[1]);

					// Проверява в базата, има ли регистрирано спортно обединение с този номер за това лице
					List<Object> obedinenie = checkObedinenieQuery.setParameter(1, codeRef).setParameter(2, this.regNom).getResultList();

					if (obedinenie.isEmpty()) { // няма и се изпълняват т. 4.2, 4.3, 4.4, 4.5, 4.6

						Integer obedinenieId = saveObedinenie(obedinenieQuery, obedinenieMfQuery, row, codeRef, em); // mms_sport_obedinenie
																														// (4.2)
						Integer vpisvaneId = saveVpisvane(vpisvaneQuery, obedinenieId, em); // mms_vpisvane (4.4)

						Integer docId = saveDoc(docQuery, codeRef, em); // doc (4.5)

						saveVpisvaneDoc(vpisvaneDocQuery, vpisvaneId, docId, obedinenieId, em); // mms_vpisvane_doc (4.6)

					} else { // Проверява се има ли вписване за това обединение

						Integer obedinenieId = SearchUtils.asInteger(obedinenie.get(0));

						List<Object> vpisvane = checkVpisvaneQuery.setParameter(2, obedinenieId).getResultList();
						if (vpisvane.isEmpty()) { // няма и се изпълняват т. 4.4, 4.5, 4.6

							Integer vpisvaneId = saveVpisvane(vpisvaneQuery, obedinenieId, em); // mms_vpisvane (4.4)

							Integer docId = saveDoc(docQuery, codeRef, em); // doc (4.5)

							saveVpisvaneDoc(vpisvaneDocQuery, vpisvaneId, docId, obedinenieId, em); // mms_vpisvane_doc (4.6)
						}
					}
				}
			}

			jpa.commit();

			LOGGER.info("  " + rows.size());
			LOGGER.info("Transfer register data - COMPLETE");

		} catch (Exception e) {
			jpa.rollback();

			LOGGER.error("  ! EIK=" + this.eik + " ! System ERROR transfer register -> " + e.getMessage(), e);

		} finally {
			jpa.closeConnection();
		}
	}

	/** @param jpa */
	public void validate(JPA jpa) {
		LOGGER.info("");
		LOGGER.info("Start validate register data");
		System.out.println();

		try {
			EntityManager em = jpa.getEntityManager();

			StringBuilder select = new StringBuilder();
			select.append(" select reg_nom id, zap_nom, reg_nom || '' splic_nom, dat_izd, eik, reg_nom || '' reg_nom, '' chairman "); // 0,1,2,3,4,5,6
			select.append(" , '' director, represent, '' sports, email, '' mso, '' polza_tr "); // 7,8,9,10,11,12,13
			select.append(" from reg_nostd ");
			select.append(" order by 1 ");

			@SuppressWarnings("unchecked")
			List<Object[]> rows = em.createNativeQuery(select.toString()).getResultList();

			long now = System.currentTimeMillis();

			Set<String> unique = new HashSet<>();
			int errcnt = 0;

			for (Object[] row : rows) {
				String tablename = "reg_nostd";

				StringBuilder errors = new StringBuilder();
				String prefix = "\n\t";

				String zap1 = SearchUtils.trimToNULL((String) row[1]); // zap_nom
				if (zap1 == null) {
					errors.append(prefix + "NULL zap_nom");
				}

				String splicNom1 = SearchUtils.trimToNULL((String) row[2]); // reg_nom
				if (splicNom1 == null) {
					errors.append(prefix + "NULL reg_nom");
				}

				if (row[3] == null) { // dat_izd
					errors.append(prefix + "NULL dat_izd");
				} else {
					if (((Date) row[3]).getTime() > now) {
						errors.append(prefix + "AFTER TODAY dat_izd=" + row[3]);
					}
				}

				String eik1 = SearchUtils.trimToNULL((String) row[4]); // eik
				if (eik1 != null) {
					if (eik1.length() < 9) {
						eik1 = ("000000000" + eik1).substring(eik1.length());
					}
					if (!ValidationUtils.isValidBULSTAT(eik1)) {
						errors.append(prefix + "NOT_VALID eik=" + row[4]);
					}
				}

				String regNom1 = SearchUtils.trimToNULL((String) row[5]); // reg_nom
				if (regNom1 == null) {
					errors.append(prefix + "NULL reg_nom");
				} else {
					if (unique.contains(regNom1)) {
						errors.append(prefix + "DUPLICATE reg_nom=" + row[5]);
					} else {
						unique.add(regNom1);
					}
				}

				String reprezent1 = SearchUtils.trimToNULL((String) row[8]); // represent
				if (reprezent1 != null && !T0Start.isBgCommasSpaces(reprezent1)) {
					errors.append(prefix + "NOT_BG_COMMA represent=" + row[8]);
				}

				String email1 = SearchUtils.trimToNULL((String) row[10]); // email
				if (email1 != null) {
					String[] split = email1.replaceAll(",", ";").split(";");
					boolean err = false;
					for (String s : split) {
						if (!ValidationUtils.isEmailValid(s.replaceAll(" ", "").trim())) { // тука е някакъв специален символ
							err = true;
						}
					}
					if (err) {
						errors.append(prefix + "NOT_VALID email=" + row[10]);
					}
				}

				if (errors.length() > 0) {
					errcnt++;
					System.out.println(tablename + ".reg_nom=" + row[5] + "; EIK=" + row[4] + errors);
					System.out.println();
				}
			}

			LOGGER.info(errcnt + " errors from " + rows.size());
			LOGGER.info("Validate register data - COMPLETE");

		} catch (Exception e) {
			LOGGER.error("System ERROR validate register! -> " + e.getMessage(), e);

		} finally {
			jpa.closeConnection();
		}
	}

	/** */
	void recreateSequences(JPA jpa) {
		LOGGER.info("");
		LOGGER.info("Recreate SEQUENCEs");

//		adm_ref_addrs
//		adm_referents
//		doc
//		mms_vpisvane_doc
//		mms_vpisvane
//		mms_vid_sport
//		mms_sport_obedinenie
// 		mms_sport_obed_mf

		StringBuilder select = new StringBuilder();
		select.append(" select 'seq_adm_ref_addrs' seq_name, max (addr_id) max_id from adm_ref_addrs ");
		select.append(" union all ");
		select.append(" select 'seq_adm_referents' seq_name, max (ref_id) max_id from adm_referents ");
		select.append(" union all ");
		select.append(" select 'seq_adm_referents_code' seq_name, max (code) max_id from adm_referents ");
		select.append(" union all ");
		select.append(" select 'seq_doc' seq_name, max (doc_id) max_id from doc ");
		select.append(" union all ");
		select.append(" select 'seq_mms_vpisvane_doc' seq_name, max (id) max_id from mms_vpisvane_doc ");
		select.append(" union all ");
		select.append(" select 'seq_mms_vpisvane' seq_name, max (id) max_id from mms_vpisvane ");
		select.append(" union all ");
		select.append(" select 'seq_mms_vid_sport' seq_name, max (id) max_id from mms_vid_sport ");
		select.append(" union all ");
		select.append(" select 'seq_mms_sport_obedninenie' seq_name, max (id) max_id from mms_sport_obedinenie ");
		select.append(" union all ");
		select.append(" select 'seq_mms_sport_obed_mf' seq_name, max (id) max_id from mms_sport_obed_mf ");

		@SuppressWarnings("unchecked")
		List<Object[]> rows = jpa.getEntityManager().createNativeQuery(select.toString()).getResultList();

		for (Object[] row : rows) {
			String seqName = (String) row[0];

			int seqVal = row[1] == null ? 1 : ((Number) row[1]).intValue() + 1;
			if (seqVal < 1) { // ако има някакви отрицателни ще объркат схемата
				seqVal = 1;
			}
			try {
				jpa.begin();
				jpa.getEntityManager().createNativeQuery("DROP SEQUENCE " + seqName).executeUpdate();
				jpa.commit();
				LOGGER.info("   drop " + seqName + " -success. --> ");

			} catch (Exception e) { // най вероятно е няма
				jpa.rollback();
				LOGGER.warn(e.getMessage());
			}

			String create = T0Start.createSequenceQuery(seqName, seqVal);
			try {
				jpa.begin();
				jpa.getEntityManager().createNativeQuery(create).executeUpdate();
				jpa.commit();
				LOGGER.info("create " + seqName + " -success. " + seqVal);

			} catch (Exception e) {
				jpa.rollback();
				LOGGER.warn(e.getMessage());
			}
		}
		LOGGER.info("Recreate SEQUENCEs - COMPLETE");
	}

	/**
	 * Тука ще се напрвят на параметри на класа разните данни от резултата
	 */
	void setupData(Object[] row) {
		this.splicDate = (Date) row[3]; // splic_dat
		if (this.splicDate == null) {
			this.splicDate = T0Start.TRANSFER_DATE;
		}

		this.eik = SearchUtils.trimToNULL((String) row[4]); // eik;
		if (this.eik != null && this.eik.length() < 9) {
			this.eik = ("000000000" + this.eik).substring(this.eik.length());
		}

		this.regNom = SearchUtils.trimToNULL((String) row[5]); // reg_nom
		this.zap = SearchUtils.trimToNULL((String) row[1]); // zap
		this.splicNom = SearchUtils.trimToNULL((String) row[2]); // splic_nom

		this.chairman = SearchUtils.trimToNULL((String) row[6]); // chairman
		this.director = SearchUtils.trimToNULL((String) row[7]); // director
		this.reprezent = SearchUtils.trimToNULL((String) row[8]); // reprezent

		this.polzaCode = null;
		String polzaTr = SearchUtils.trimToNULL_Upper((String) row[12]); // polza_tr
		if (polzaTr != null) {
			this.polzaCode = "ДА".equals(polzaTr) ? 2 : 1;
		}
		this.refName = SearchUtils.trimToNULL((String) row[13]); // name
		if (this.refName == null) {
			this.refName = this.eik; // eik
		}
		this.email = SearchUtils.trimToNULL((String) row[10]); // email
		if (this.email != null) {
			this.email = this.email.replaceAll(",", ";");
		}

		this.statusVpisvane = DocuConstants.CODE_ZNACHENIE_STATUS_REG_VPISAN;
	}

	/** */
	private Integer saveDoc(Query query, Integer codeRef, EntityManager em) {
		Integer docId = T0Start.nextVal("seq_doc", em);

		query.setParameter(1, docId); // doc_id
		query.setParameter(2, T0Start.REGISTATRURA); // registratura_id
		query.setParameter(3, T0Start.REGISTER); // register_id
		query.setParameter(4, codeRef); // code_ref_corresp
		query.setParameter(5, this.regNom); // rn_doc
		query.setParameter(6, ""); // rn_prefix
		query.setParameter(7, new TypedParameterValue(StandardBasicTypes.INTEGER, null)); // rn_pored
		query.setParameter(8, "{" + UUID.randomUUID().toString().toUpperCase() + "}"); // guid
		query.setParameter(9, DocuConstants.CODE_ZNACHENIE_DOC_TYPE_IN); // doc_type
		query.setParameter(10, CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_NOSTD); // doc_vid
		query.setParameter(11, this.splicDate); // doc_date
		query.setParameter(12, "Заявление за ново вписване на " + this.refName); // otnosno
		query.setParameter(13, SysConstants.CODE_ZNACHENIE_DA); // valid
		query.setParameter(14, this.splicDate); // valid_date
		query.setParameter(15, SysConstants.CODE_ZNACHENIE_DA); // processed
		query.setParameter(16, SysConstants.CODE_ZNACHENIE_DA); // free_access
		query.setParameter(17, 0); // count_files
		query.setParameter(18, T0Start.USER); // user_reg
		query.setParameter(19, this.splicDate); // date_reg
		query.setParameter(20, SysConstants.CODE_ZNACHENIE_DA); // competence
		query.executeUpdate();

		return docId;
	}

	/** */
	private Integer saveObedinenie(Query query, Query queryMf, Object[] row, Integer codeRef, EntityManager em) {
		Integer id = T0Start.nextVal("seq_mms_sport_obedninenie", em);

		int statusObekt = DocuConstants.CODE_ZNACHENIE_STATUS_OBEKT_VPISAN;
		if (this.statusVpisvane != null //
			&& this.statusVpisvane.intValue() == DocuConstants.CODE_ZNACHENIE_STATUS_REG_OTNETO_VPISVANE) {
			statusObekt = DocuConstants.CODE_ZNACHENIE_STATUS_OBEKT_ZALICHEN;
		}

		query.setParameter(1, id); // id
		query.setParameter(2, new TypedParameterValue(StandardBasicTypes.INTEGER, null)); // type_sport
		query.setParameter(3, this.regNom); // reg_nomer
		query.setParameter(4, codeRef); // id_object
		query.setParameter(5, DocuConstants.CODE_ZNACHENIE_VID_SPORT_OBEDINENIE_NOSTD); // vid
		query.setParameter(6, this.reprezent); // predstavitelstvo
		query.setParameter(7, this.chairman); // predsedatel
		query.setParameter(8, this.director); // gen_sek_direktor
		query.setParameter(9, new TypedParameterValue(StandardBasicTypes.INTEGER, null)); // br_chlenove
		query.setParameter(10, T0Start.USER); // user_reg
		query.setParameter(11, this.splicDate); // date_reg
		query.setParameter(12, statusObekt); // status
		query.setParameter(13, this.splicDate); // date_status
		query.setParameter(14, ""); // dop_info
		query.executeUpdate();

		return id;
	}

	/**  */
	private Integer saveReferent(Query query, Query addrQuery, Object[] row, EntityManager em) {
		Integer refId = T0Start.nextVal("seq_adm_referents", em);
		Integer code = T0Start.nextVal("seq_adm_referents_code", em);

		query.setParameter(1, refId); // ref_id
		query.setParameter(2, code); // code
		query.setParameter(3, 0); // code_parent
		query.setParameter(4, CODE_ZNACHENIE_REF_TYPE_NFL); // ref_type
		query.setParameter(5, this.refName); // ref_name
		query.setParameter(6, T0Start.MIN_DATE); // date_ot
		query.setParameter(7, T0Start.MAX_DATE); // date_do
		query.setParameter(8, T0Start.USER); // user_reg
		query.setParameter(9, this.splicDate); // date_reg
		query.setParameter(10, 37); // ref_grj
		query.setParameter(11, this.eik); // nfl_eik
		query.setParameter(12, row[19]); // contact_phone
		query.setParameter(13, this.email); // contact_email
		query.setParameter(14, row[20]); // web_page
		query.setParameter(15, new TypedParameterValue(StandardBasicTypes.INTEGER, this.polzaCode)); // polza
		query.setParameter(16, null); // ref_latin
		query.setParameter(17, REF_MIG_NAME); // mig_login_name
		query.setParameter(18, this.reprezent);
		query.executeUpdate();

		String address = SearchUtils.trimToNULL((String) row[16]); // adres
		if (address != null) {
			Integer addrId = T0Start.nextVal("seq_adm_ref_addrs", em);

			// head-15 , obst-18
			String key = (row[15] + "_" + row[18]).toUpperCase().replaceAll(" ", "");
			Integer ekatte = this.ekatteMap.get(key);
			if (ekatte == null) {
				if ("гр. София".equalsIgnoreCase((String) row[15])) {
					ekatte = 68134;
				} else {
					LOGGER.warn("  ! eik=" + this.eik + " uknown EKATTE: " + row[15] + " " + row[18]);
				}
			}

			addrQuery.setParameter(1, addrId); // addr_id
			addrQuery.setParameter(2, code); // code_ref
			addrQuery.setParameter(3, DocuConstants.CODE_ZNACHENIE_ADDR_TYPE_CORRESP); // addr_type
			addrQuery.setParameter(4, 37); // addr_country
			addrQuery.setParameter(5, address); // addr_text
			addrQuery.setParameter(6, row[14]); // post_code/ pk
			addrQuery.setParameter(7, ""); // post_box
			addrQuery.setParameter(8, new TypedParameterValue(StandardBasicTypes.INTEGER, ekatte)); // ekatte
			addrQuery.setParameter(9, new TypedParameterValue(StandardBasicTypes.INTEGER, null)); // raion
			addrQuery.setParameter(10, T0Start.USER); // user_reg
			addrQuery.setParameter(11, this.splicDate); // date_reg
			addrQuery.executeUpdate();
		}

		return code;
	}

	/** */
	private Integer saveVpisvane(Query query, Integer obedinenieId, EntityManager em) {
		Integer id = T0Start.nextVal("seq_mms_vpisvane", em);

		query.setParameter(1, id); // id - ИД на вписване
		query.setParameter(2, CODE_ZNACHENIE_JOURNAL_SPORT_OBED); // type_object - Тип на обекта
		query.setParameter(3, obedinenieId); // id_object - ИД на обекта

		// rn_doc_zaiavlenie - Рег. номер на Заявление за вписване
		query.setParameter(4, this.regNom);

		// date_doc_zaiavlenie - Дата на Заявление за вписване
		query.setParameter(5, this.splicDate);

		// status_result_zaiavlenie - Статус на заявление
		query.setParameter(6, DocuConstants.CODE_ZNACHENIE_STATUS_ZAIAVLENIE_VPISAN);

		// reason_result - Основания за статуса на заявление
		query.setParameter(7, new TypedParameterValue(StandardBasicTypes.INTEGER, null));

		// reason_result_text - Основания за статуса на заявление (текст)
		query.setParameter(8, "");

		// rn_doc_result - Рег. номер на Заповед за вписване / Заповед за отказ от вписване
		query.setParameter(9, this.zap);

		// date_doc_result - Дата на Заповед за вписване / Заповед за отказ от вписване
		query.setParameter(10, this.splicDate);

		// rn_doc_licenz - Рег. номер на лиценз/удостоверение
		query.setParameter(11, this.splicNom);

		// date_doc_licenz - Дата на лиценз/удостоверение
		query.setParameter(12, this.splicDate);

		// status_vpisvane - Статус на вписване
		query.setParameter(13, this.statusVpisvane);

		// reason_vpisvane - Основание за статус на вписване
		query.setParameter(14, new TypedParameterValue(StandardBasicTypes.INTEGER, null));

		// reason_vpisvane_text - Основание за статус на вписване (текст)
		query.setParameter(15, "");

		// rn_doc_vpisvane - Рег. номер на Заповед за прекратяване / Заповед за отнемане и Заповед за заличаване
		query.setParameter(16, "");

		// date_doc_vpisvane - Дата на Заповед за прекратяване / Заповед за отнемане и Заповед за заличаване
		query.setParameter(17, new TypedParameterValue(StandardBasicTypes.TIMESTAMP, null));

		// nachin_poluchavane - Начин на получаване на резултата
		query.setParameter(18, new TypedParameterValue(StandardBasicTypes.INTEGER, null));

		// addr_mail_poluchavane - Адрес/Email
		query.setParameter(19, "");

		// date_status_zaiavlenie - Дата на статус на заявлението
		query.setParameter(20, this.splicDate);

		query.setParameter(21, new TypedParameterValue(StandardBasicTypes.INTEGER, null)); // vid_sport - Вид спорт
		query.setParameter(22, new TypedParameterValue(StandardBasicTypes.INTEGER, null)); // dlajnost - Длъжност

		query.setParameter(23, ""); // dop_info - Допълнителна информация
		query.setParameter(24, T0Start.USER); // user_reg
		query.setParameter(25, this.splicDate); // date_reg

		// date_status_vpisvane - Дата на статус на вписването
		query.setParameter(26, this.splicDate);

		query.executeUpdate();
		return id;
	}

	/** */
	private Integer saveVpisvaneDoc(Query query, Integer vpisvaneId, Integer docId, Integer obedinenieId, EntityManager em) {
		Integer id = T0Start.nextVal("seq_mms_vpisvane_doc", em);

		query.setParameter(1, id); // id
		query.setParameter(2, vpisvaneId); // id_vpisvane
		query.setParameter(3, docId); // id_doc
		query.setParameter(4, T0Start.USER); // user_reg
		query.setParameter(5, this.splicDate); // date_reg
		query.setParameter(6, obedinenieId); // id_object
		query.setParameter(7, CODE_ZNACHENIE_JOURNAL_SPORT_OBED); // type_object
		query.executeUpdate();

		return id;
	}
}
