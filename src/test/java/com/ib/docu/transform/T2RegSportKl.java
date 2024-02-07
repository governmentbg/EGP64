package com.ib.docu.transform;

import static com.ib.docu.system.DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_FORMATIONS;
import static com.ib.docu.system.DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORT_OBED;
import static com.ib.docu.system.DocuConstants.CODE_ZNACHENIE_REF_TYPE_NFL;
import static com.ib.docu.system.DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_SPORTNO_FORM;
import static com.ib.docu.system.DocuConstants.CODE_ZNACHENIE_VID_SPORTNO_FORMIROVANIE_SK;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

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
 * ММС – миграция на спортни формирования
 *
 * @author belev
 */
public class T2RegSportKl {

	private static final Logger LOGGER = LoggerFactory.getLogger(T2RegSportKl.class);

	/**  */
	private static final String REF_MIG_NAME = "mms_sport_formirovanie"; // mms_sport_formirovanie

	/** @param args */
	public static void main(String[] args) {
		T2RegSportKl t = new T2RegSportKl();

		t.validate(JPA.getUtil());

//		t.clear(JPA.getUtil());

		t.transfer(JPA.getUtil());

//		t.regix(JPA.getUtil());

		System.exit(0); // не е ясно защо не терминира ако го няма
	}

	private SimpleDateFormat	sdfDMY	= new SimpleDateFormat("dd.MM.yyyy");
	private SimpleDateFormat	sdfMY	= new SimpleDateFormat("MM.yyyy");
	private SimpleDateFormat	sdfY	= new SimpleDateFormat("yyyy");

	private Map<String, Integer> ekatteMap;

	// данни, които трябва по време на една итерация на процеса
	private Map<String, Integer> docMap = new HashMap<>(); // key=vh_nom, value=docId

	private Date	regDatSk;
	private String	regNomSk;
	private Integer	typeSport;

	private String	eik;
	private String	refName;
	private String	email;
	private Integer	polzaCode;
	private String	chairman;
	private String	reprezent;

	private String	licenz;
	private Date	licenzDate;

	private Date	dateAcceptance;
	private Date	dataТermination;
	private Integer	statusVpisvane;
	private Date	statusVpisvaneDate;

	private String reasonVpisvaneText;

	private String dopInfo;

	/**  */
	public T2RegSportKl() {
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
				+ "select code from adm_referents where mig_login_name = ?1 and code in (select id_object from mms_sport_formirovanie))") //
				.setParameter(1, REF_MIG_NAME).executeUpdate();
			jpa.commit();
			LOGGER.info("   deleted " + cnt + " rows from table adm_ref_addrs");

			jpa.begin();
			cnt = em.createNativeQuery( //
				"delete from adm_referents where mig_login_name = ?1 and code in (select id_object from mms_sport_formirovanie)") //
				.setParameter(1, REF_MIG_NAME).executeUpdate();
			jpa.commit();
			LOGGER.info("   deleted " + cnt + " rows from table adm_referents");

			jpa.begin();
			cnt = em.createNativeQuery("delete from mms_chlenstvo where type_object = ?1") //
				.setParameter(1, CODE_ZNACHENIE_JOURNAL_SPORTS_FORMATIONS).executeUpdate();
			jpa.commit();
			LOGGER.info("   deleted " + cnt + " rows from table mms_chlenstvo");

			jpa.begin();
			cnt = em.createNativeQuery( // после по това число ще знам кои да изтрия
				"update doc set user_last_mod = -2000 where doc_id in (select id_doc from mms_vpisvane_doc where type_object = ?1)") //
				.setParameter(1, CODE_ZNACHENIE_JOURNAL_SPORTS_FORMATIONS).executeUpdate();
			LOGGER.info("   update " + cnt + " rows from table doc.user_last_mod=-2000");

			cnt = em.createNativeQuery("delete from mms_vpisvane_doc where type_object = ?1") //
				.setParameter(1, CODE_ZNACHENIE_JOURNAL_SPORTS_FORMATIONS).executeUpdate();
			LOGGER.info("   deleted " + cnt + " rows from table mms_vpisvane_doc");

			cnt = em.createNativeQuery("delete from doc_referents where doc_id in (select distinct doc_id from doc where user_last_mod = -2000)").executeUpdate();
			LOGGER.info("   deleted " + cnt + " rows from table doc_referents");

			cnt = em.createNativeQuery("delete from doc where user_last_mod = -2000").executeUpdate();
			LOGGER.info("   deleted " + cnt + " rows from table doc");
			jpa.commit();

			jpa.begin();
			cnt = em.createNativeQuery("delete from mms_vpisvane where type_object = ?1") //
				.setParameter(1, CODE_ZNACHENIE_JOURNAL_SPORTS_FORMATIONS).executeUpdate();
			jpa.commit();
			LOGGER.info("   deleted " + cnt + " rows from table mms_vpisvane");

			jpa.begin();
			cnt = em.createNativeQuery("delete from mms_vid_sport where tip_object = ?1") //
				.setParameter(1, CODE_ZNACHENIE_JOURNAL_SPORTS_FORMATIONS).executeUpdate();
			jpa.commit();
			LOGGER.info("   deleted " + cnt + " rows from table mms_vid_sport");

			jpa.begin();
			cnt = em.createNativeQuery("delete from mms_sport_formirovanie").executeUpdate();
			jpa.commit();
			LOGGER.info("   deleted " + cnt + " rows from table mms_sport_formirovanie");

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

			// key=reg_nom_sk, value=[reg_nom_sk, priet_osk, reg_dat, osk_id]
			Map<String, Object[]> chlenstvoMap = new HashMap<>(); // за другите членства
			Stream<Object[]> stream = em.createNativeQuery( //
				"select ch.reg_nom_sk, ch.priet_osk, ch.reg_dat, osk.id osk_id from reg_sport_osk_chlenstvo ch inner join mms_sport_obedinenie osk on osk.reg_nomer = ch.reg_nom_osk") //
				.getResultStream();
			Iterator<Object[]> iter = stream.iterator();
			while (iter.hasNext()) {
				Object[] row = iter.next();
				String key = SearchUtils.trimToNULL((String) row[0]); // reg_nom_sk
				if (key != null) {
					chlenstvoMap.put(key, row);
				}
			}
			stream.close();

			Query referentQuery = em.createNativeQuery("insert into adm_referents (" //
				+ " ref_id, code, code_parent, ref_type, ref_name, date_ot, date_do, user_reg, date_reg" //
				+ ", ref_grj, nfl_eik, contact_phone, contact_email, web_page, polza, ref_latin, mig_login_name, predstavitelstvo )" //
				+ " values (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9, ?10, ?11, ?12, ?13, ?14, ?15, ?16, ?17, ?18)");

			Query addrQuery = em.createNativeQuery("insert into adm_ref_addrs (" //
				+ " addr_id, code_ref, addr_type, addr_country, addr_text, post_code, post_box, ekatte, raion, user_reg, date_reg )" //
				+ " values (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9, ?10, ?11)");

			Query formirovanieQuery = em.createNativeQuery("insert into mms_sport_formirovanie (" //
				+ " id, type_sport, reg_nomer, id_object , vid, predstavitelstvo, predsedatel" //
				+ ", school_name, user_reg, date_reg, status, date_status, dop_info )" //
				+ " values (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9, ?10, ?11, ?12, ?13)");

			Query chlenstvoQuery = em.createNativeQuery("insert into mms_chlenstvo (" //
				+ " id, type_object, id_object, type_vish_object, id_vish_object, date_acceptance, date_termination, user_reg, date_reg )" //
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

			Query docQueryUpdate = em.createNativeQuery("update doc set otnosno = otnosno || '; ' || ?1 where doc_id = ?2" //
				+ " and otnosno not like ?3"); // за да не дублира имената

			Query vpisvaneDocQuery = em.createNativeQuery("insert into mms_vpisvane_doc (" //
				+ " id, id_vpisvane, id_doc, user_reg, date_reg, id_object, type_object ) " //
				+ "values (?1, ?2, ?3, ?4, ?5, ?6, ?7)");

			Query checkReferentQuery = em.createNativeQuery("select ref_id, code, ref_name from adm_referents where ref_type = ?1 and nfl_eik = ?2") //
				.setParameter(1, CODE_ZNACHENIE_REF_TYPE_NFL);

			Query checkFormirovanieQuery = em.createNativeQuery("select id from mms_sport_formirovanie where id_object = ?1 and reg_nomer = ?2");

			Query checkVpisvaneQuery = em.createNativeQuery("select id from mms_vpisvane where type_object = ?1 and id_object = ?2") //
				.setParameter(1, CODE_ZNACHENIE_JOURNAL_SPORTS_FORMATIONS);

			StringBuilder select = new StringBuilder();
			select.append(" select 2 kl, kl.reg_nom_sk, kl.reg_dat_sk, kl.reg_nom, kl.eik, kl.priet "); // 0,1,2,3,4,5
			select.append(" , kl.chairman, kl.reprezent, kl.mem_osk, kl.polza, kl.udost_nom, kl.email "); // 6,7,8,9,10,11
			select.append(" , kl.name_sk, kl.head, kl.pk, kl.adres, kl.obst, kl.obl, kl.tel, kl.web, kl.notes "); // 12,13,14,15,16,17,18,19,20
			select.append(" , kl.data_end, kl.motivi, kl.polza_tr, kl.in_tr, kl.project, kl.bs, ob.id ob_id, ob.type_sport "); // 21,22,23,24,25,26,27,28
			select.append(" from reg_sport_kl kl");
			select.append(" left outer join mms_sport_obedinenie ob on ob.reg_nomer = kl.reg_nom ");
			select.append(" union all ");
			select.append(
				" select 1 kl, kl.reg_nom_sk, kl.dat_reg_sk reg_dat_sk, kl.reg_nom, cast (kl.eik as varchar) eik, kl.priet, kl.chairman, kl.reprezent, kl.mem_osk, kl.polza, kl.udost_nom, kl.email ");
			select.append(
				" , kl.name_sk, kl.head, kl.pk, kl.adres, kl.obst, kl.obl, kl.tel, kl.web, kl.notes, kl.data_end, kl.motivi, kl.polza_tr, kl.in_tr, kl.project, kl.bs, ob.id ob_id, ob.type_sport ");
			select.append(" from reg_sport_kl_zalicheni kl ");
			select.append(" left outer join mms_sport_obedinenie ob on ob.reg_nomer = kl.reg_nom ");
			select.append(" order by 1, 2 ");

			List<Object[]> rows = em.createNativeQuery(select.toString()).getResultList();

			Map<String, Integer> max = new HashMap<>(); // за броячие

			int insertCnt = 0;
			jpa.begin();
			for (Object[] row : rows) {
				setupData(row); // за да се намерят данните !!! МНОГО ВАЖНО !!!

				if (this.eik == null || this.regNomSk == null) {
					continue;
				}
				List<Object[]> referent = checkReferentQuery.setParameter(2, this.eik).getResultList();

				String regNomFed = SearchUtils.trimToNULL((String) row[3]); // reg_nom
				try {
					int current = Integer.parseInt(this.regNomSk.split("-")[1]);
					Integer maxVal = max.get(regNomFed);

					if (maxVal == null || maxVal.intValue() < current) {
						maxVal = current;
					}
					max.put(regNomFed, maxVal);

				} catch (Exception e) {
					LOGGER.error("  ! reg_nom_sk=" + this.regNomSk + " ! ERROR parseInt ", e);
				}

				if (referent.isEmpty()) { // запис на всичко
					Integer codeRef = saveReferent(referentQuery, addrQuery, row, em); // adm_referents (4.1)

					Integer formirovanieId = saveFormirovanie(formirovanieQuery, codeRef, em); // mms_sport_formirovanie (4.2)

					saveChlenstvo(chlenstvoQuery, row, formirovanieId, em); // mms_chlenstvo (4.3)
					if (chlenstvoMap.containsKey(this.regNomSk)) {
						saveChlenstvoOsk(chlenstvoQuery, chlenstvoMap.get(this.regNomSk), formirovanieId, em);
					}

					Integer vpisvaneId = saveVpisvane(vpisvaneQuery, formirovanieId, em); // mms_vpisvane (4.4)

					Integer docId = saveDoc(docQuery, docQueryUpdate, codeRef, em); // doc (4.5)

					saveVpisvaneDoc(vpisvaneDocQuery, vpisvaneId, docId, formirovanieId, em); // mms_vpisvane_doc (4.6)

				} else { // допълнителна логика
					Integer codeRef = SearchUtils.asInteger(referent.get(0)[1]);

					// Проверява в базата, има ли спортно формирование с този номер за това лице
					List<Object> formirovanie = checkFormirovanieQuery.setParameter(1, codeRef).setParameter(2, this.regNomSk).getResultList();

					if (formirovanie.isEmpty()) { // няма и се изпълняват т. 4.2, 4.3, 4.4, 4.5, 4.6

						Integer formirovanieId = saveFormirovanie(formirovanieQuery, codeRef, em); // mms_sport_formirovanie (4.2)

						saveChlenstvo(chlenstvoQuery, row, formirovanieId, em); // mms_chlenstvo (4.3)
						if (chlenstvoMap.containsKey(this.regNomSk)) {
							saveChlenstvoOsk(chlenstvoQuery, chlenstvoMap.get(this.regNomSk), formirovanieId, em);
						}

						Integer vpisvaneId = saveVpisvane(vpisvaneQuery, formirovanieId, em); // mms_vpisvane (4.4)

						Integer docId = saveDoc(docQuery, docQueryUpdate, codeRef, em); // doc (4.5)

						saveVpisvaneDoc(vpisvaneDocQuery, vpisvaneId, docId, formirovanieId, em); // mms_vpisvane_doc (4.6)

					} else { // Проверява се има ли вписване за това формирование
						Integer formirovanieId = SearchUtils.asInteger(formirovanie.get(0));

						List<Object> vpisvane = checkVpisvaneQuery.setParameter(2, formirovanieId).getResultList();
						if (vpisvane.isEmpty()) { // няма и се изпълняват т. 4.4, 4.5, 4.6

							Integer vpisvaneId = saveVpisvane(vpisvaneQuery, formirovanieId, em); // mms_vpisvane (4.4)

							Integer docId = saveDoc(docQuery, docQueryUpdate, codeRef, em); // doc (4.5)

							saveVpisvaneDoc(vpisvaneDocQuery, vpisvaneId, docId, formirovanieId, em); // mms_vpisvane_doc (4.6)
						}
					}
				}

				insertCnt++;
				if (insertCnt % 100 == 0) {
					LOGGER.info("  " + insertCnt);
				}
			}

			for (Entry<String, Integer> entry : max.entrySet()) { // оправям броячите
				String key = "formir." + entry.getKey();

				int cnt = em.createNativeQuery("update sid set next_val = ?1 where object = ?2") //
					.setParameter(1, entry.getValue().intValue() + 1).setParameter(2, key).executeUpdate();
				if (cnt == 0) {
					em.createNativeQuery("insert into sid (object, next_val) values (?1, ?2)") //
						.setParameter(1, key).setParameter(2, entry.getValue().intValue() + 1).executeUpdate();
				}
			}

			// трябва да се оправи вид спорт на всички формирования като се вземе от обединението в което е член
			StringBuilder insertVidSport = new StringBuilder();
			insertVidSport.append(" insert into mms_vid_sport (id, tip_object, id_object, user_reg, date_reg, vid_sport) ");
			insertVidSport.append(" select nextval('seq_mms_vid_sport'), c.type_object, c.id_object, s.user_reg, s.date_reg, s.vid_sport ");
			insertVidSport.append(" from mms_sport_obedinenie o ");
			insertVidSport.append(" inner join mms_vid_sport s on s.id_object = o.id ");
			insertVidSport.append(" inner join mms_chlenstvo c on c.id_vish_object = o.id ");
			insertVidSport.append(" where s.tip_object = :tipObed and c.type_vish_object = :tipObed and c.type_object = :tipForm ");
			em.createNativeQuery(insertVidSport.toString()) //
				.setParameter("tipObed", CODE_ZNACHENIE_JOURNAL_SPORT_OBED) //
				.setParameter("tipForm", CODE_ZNACHENIE_JOURNAL_SPORTS_FORMATIONS) //
				.executeUpdate();

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

//		priet=учредител -  да не се търси членство във федерация, а да се пропуска търсенето на федерация
//		Трябва преди записа на клуба да се търси, има ли го вече и ако го има, да се пропусне записа.

		try {
			EntityManager em = jpa.getEntityManager();

			StringBuilder select = new StringBuilder();
			select.append(" select 2 kl, kl.reg_nom_sk, kl.reg_dat_sk, kl.reg_nom, kl.eik, kl.priet "); // 0,1,2,3,4,5
			select.append(" , kl.chairman, kl.reprezent, kl.mem_osk, kl.polza, kl.udost_nom, kl.email, ob.id ob_id "); // 6,7,8,9,10,11,12
			select.append(" from reg_sport_kl kl ");
			select.append(" left outer join mms_sport_obedinenie ob on ob.reg_nomer = kl.reg_nom ");
			select.append(" union all ");
			select.append(
				" select 1 kl, kl.reg_nom_sk, kl.dat_reg_sk reg_dat_sk, kl.reg_nom, cast (kl.eik as varchar) eik, kl.priet, kl.chairman, kl.reprezent, kl.mem_osk, kl.polza, kl.udost_nom, kl.email, ob.id ob_id ");
			select.append(" from reg_sport_kl_zalicheni kl ");
			select.append(" left outer join mms_sport_obedinenie ob on ob.reg_nomer = kl.reg_nom ");
			select.append(" order by 1, 2 ");

			@SuppressWarnings("unchecked")
			List<Object[]> rows = em.createNativeQuery(select.toString()).getResultList();

			long now = System.currentTimeMillis();

			Set<String> unique = new HashSet<>();
			int errcnt = 0;

			for (Object[] row : rows) {
				String tablename = ((Number) row[0]).intValue() == 1 ? "reg_sport_kl_zalicheni" : "reg_sport_kl";

				StringBuilder errors = new StringBuilder();
				String prefix = "\n\t";

				String regNomSk1 = SearchUtils.trimToNULL((String) row[1]); // reg_nom_sk
				if (regNomSk1 == null) {
					errors.append(prefix + "NULL reg_nom_sk");
				} else {
					if (unique.contains(regNomSk1)) {
						errors.append(prefix + "DUPLICATE reg_nom_sk=" + row[1]);
					} else {
						unique.add(regNomSk1);
					}
				}

				if (row[2] == null) { // reg_dat_sk
					errors.append(prefix + "NULL reg_dat_sk");
				} else {
					if (((Date) row[2]).getTime() > now) {
						errors.append(prefix + "AFTER TODAY reg_dat_sk=" + row[2]);
					}
				}

				String regNom = SearchUtils.trimToNULL((String) row[3]);
				if (regNom == null) {
					errors.append(prefix + "NULL reg_nom");
				} else {
					if (row[12] == null) {
						errors.append(prefix + "MISSING mms_sport_obedinenie.reg_nomer");
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

				String priet = SearchUtils.trimToNULL((String) row[5]); // priet
				if (priet != null && !"учредител".equalsIgnoreCase(priet)) {
					int indexOf = priet.indexOf(' ');
					String val = indexOf == -1 ? priet : priet.substring(0, indexOf);
					try {
						Integer.parseInt(val); // ако е само година е ОК
					} catch (Exception e) {
						Date date = null;
						try {
							date = this.sdfDMY.parse(val);
						} catch (Exception e2) {
							try {
								date = this.sdfMY.parse(val);
							} catch (Exception e3) {
								errors.append(prefix + "NOT_VALID priet=" + row[5]);
							}
						}
						if (date != null && date.getTime() > now) {
							errors.append(prefix + "AFTER TODAY priet=" + row[5]);
						}
					}
				}

				String chairman1 = SearchUtils.trimToNULL((String) row[6]); // chairman
				if (chairman1 != null && !T0Start.isBgCommasSpaces(chairman1)) {
					errors.append(prefix + "NOT_BG_COMMA chairman=" + row[6]);
				}

				String reprezent1 = SearchUtils.trimToNULL((String) row[7]); // reprezent
				if (reprezent1 != null && !T0Start.isBgCommasSpaces(reprezent1)) {
					errors.append(prefix + "NOT_BG_COMMA reprezent=" + row[7]);
				}

				String polza = SearchUtils.trimToNULL_Upper((String) row[9]); // polza
				if (polza != null && !"ОБЩЕСТВЕНА".equals(polza) && !"ЧАСТНА".equals(polza) && !"ТЗ".equals(polza)) {
					errors.append(prefix + "NOT_VALID polza=" + row[9]);
				}

				String udostNom = SearchUtils.trimToNULL((String) row[10]); // udost_nom
				if (udostNom == null) {
					errors.append(prefix + "NULL udost_nom");
				} else {
					String[] split = udostNom.split("/");
					Date date = null;
					try {
						date = this.sdfDMY.parse(split[1]);
					} catch (Exception e) {
						errors.append(prefix + "NOT_VALID udost_nom=" + row[10]);
					}
					if (date != null && date.getTime() > now) {
						errors.append(prefix + "AFTER TODAY udost_nom=" + row[10]);
					}
				}

				String email1 = SearchUtils.trimToNULL((String) row[11]); // email
				if (email1 != null) {
					String[] split = email1.replaceAll(",", ";").split(";");
					boolean err = false;
					for (String s : split) {
						if (!ValidationUtils.isEmailValid(s.replaceAll(" ", "").trim())) { // тука е някакъв специален символ
							err = true;
						}
					}
					if (err) {
						errors.append(prefix + "NOT_VALID email=" + row[11]);
					}
				}

				if (errors.length() > 0) {
					errcnt++;
					System.out.println(tablename + ".reg_nom_sk=" + row[1] + "; EIK=" + row[4] + errors);
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
//		mms_sport_formirovanie
//		mms_chlenstvo
//		mms_vid_sport

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
		select.append(" select 'seq_mms_sport_formirovanie' seq_name, max (id) max_id from mms_sport_formirovanie ");
		select.append(" union all ");
		select.append(" select 'seq_mms_chlenstvo' seq_name, max (id) max_id from mms_chlenstvo ");
		select.append(" union all ");
		select.append(" select 'seq_mms_vid_sport' seq_name, max (id) max_id from mms_vid_sport ");

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
		this.regDatSk = (Date) row[2]; // reg_dat_sk
		if (this.regDatSk == null) {
			this.regDatSk = T0Start.TRANSFER_DATE;
		}
		this.regNomSk = SearchUtils.trimToNULL((String) row[1]); // reg_nom_sk

		this.typeSport = SearchUtils.asInteger(row[28]); // type_sport - от обединението

		this.eik = SearchUtils.trimToNULL((String) row[4]); // eik
		if (this.eik != null && this.eik.length() < 9) {
			this.eik = ("000000000" + this.eik).substring(this.eik.length());
		}

		this.refName = SearchUtils.trimToNULL((String) row[12]); // name_sk
		if (this.refName == null) {
			this.refName = (String) row[4]; // eik
		}
		this.email = SearchUtils.trimToNULL((String) row[11]); // email
		if (this.email != null) {
			this.email = this.email.replaceAll(",", ";");
		}
		this.polzaCode = null;
		String polza = SearchUtils.trimToNULL_Upper((String) row[9]); // polza
		if (polza != null && !"ТЗ".equals(polza)) {
			this.polzaCode = "ОБЩЕСТВЕНА".equals(polza) ? 2 : 1;
		}

		this.chairman = SearchUtils.trimToNULL((String) row[6]); // chairman
		this.reprezent = SearchUtils.trimToNULL((String) row[7]); // reprezent

		String udostNom = SearchUtils.trimToNULL((String) row[10]); // udost_nom
		this.licenz = null;
		this.licenzDate = null;
		if (udostNom != null) {
			String[] split = udostNom.split("/");
			try {
				this.licenz = split[0];
				this.licenzDate = this.sdfDMY.parse(split[1]);
			} catch (Exception e) {
				this.licenz = udostNom;
			}
		}

		this.dateAcceptance = null;
		String priet = SearchUtils.trimToNULL((String) row[5]); // priet
		if ("учредител".equalsIgnoreCase(priet)) {
			this.dateAcceptance = this.regDatSk;

		} else if (priet != null) {
			int indexOf = priet.indexOf(' ');
			String val = indexOf == -1 ? priet : priet.substring(0, indexOf);
			if (val.length() > 10) {
				val = val.substring(0, 10);
			}
			try {
				this.dateAcceptance = this.sdfDMY.parse("01.01." + Integer.parseInt(val));
			} catch (Exception e) {
				try {
					this.dateAcceptance = this.sdfDMY.parse(val);
				} catch (Exception e2) {
					try {
						this.dateAcceptance = this.sdfMY.parse(val);
					} catch (Exception e3) {
						this.dateAcceptance = this.regDatSk;
					}
				}
			}
		}

		this.statusVpisvane = null;
		this.statusVpisvaneDate = null;
		this.dataТermination = (Date) row[21]; // data_end

		StringBuilder dopInfoSb = new StringBuilder();
		String notes = SearchUtils.trimToNULL((String) row[20]); // notes
		if (notes != null) {
			dopInfoSb.append(notes);
		}
		String project = SearchUtils.trimToNULL((String) row[25]); // project
		if (project != null) {
			if (dopInfoSb.length() > 0) {
				dopInfoSb.append(" ");
			}
			dopInfoSb.append("Проект № " + project);
		}
		this.dopInfo = dopInfoSb.toString();

		if (((Number) row[0]).intValue() == 1) { // 1=reg_sport_kl_zalicheni
			this.statusVpisvaneDate = this.dataТermination;
			this.statusVpisvane = DocuConstants.CODE_ZNACHENIE_STATUS_REG_ZALICHENO_VPISVANE;
		} else {
			this.statusVpisvaneDate = this.regDatSk;
			this.statusVpisvane = DocuConstants.CODE_ZNACHENIE_STATUS_REG_VPISAN;
		}

		if (this.statusVpisvaneDate == null) {
			this.statusVpisvaneDate = this.regDatSk;
		}

		this.reasonVpisvaneText = SearchUtils.trimToNULL((String) row[22]); // motivi
	}

	/** */
	private Integer saveChlenstvo(Query query, Object[] row, Integer formirovanieId, EntityManager em) {
		if (row[27] == null) { // ob_id - тука стои ИД на обединението
			return null; // няма как да се запише
		}

		Integer id = T0Start.nextVal("seq_mms_chlenstvo", em);

		query.setParameter(1, id); // id
		query.setParameter(2, CODE_ZNACHENIE_JOURNAL_SPORTS_FORMATIONS); // type_object
		query.setParameter(3, formirovanieId); // id_object
		query.setParameter(4, CODE_ZNACHENIE_JOURNAL_SPORT_OBED); // type_vish_object
		query.setParameter(5, row[27]); // id_vish_object
		query.setParameter(6, new TypedParameterValue(StandardBasicTypes.TIMESTAMP, this.dateAcceptance)); // date_acceptance
		query.setParameter(7, new TypedParameterValue(StandardBasicTypes.TIMESTAMP, this.dataТermination)); // date_termination
		query.setParameter(8, T0Start.USER); // user_reg
		query.setParameter(9, this.regDatSk); // date_reg
		query.executeUpdate();

		return id;
	}

	/** key=reg_nom_sk, value=[reg_nom_sk, priet_osk, reg_dat, osk_id] */
	private Integer saveChlenstvoOsk(Query query, Object[] row, Integer formirovanieId, EntityManager em) {
		Integer id = T0Start.nextVal("seq_mms_chlenstvo", em);

		Date regDat = (Date) row[2]; // reg_dat
		if (regDat == null) {
			regDat = T0Start.TRANSFER_DATE;
		}
		Date prietOsk = (Date) row[1]; // priet_osk;
		if (prietOsk == null) {
			prietOsk = regDat;
		}

		query.setParameter(1, id); // id
		query.setParameter(2, CODE_ZNACHENIE_JOURNAL_SPORTS_FORMATIONS); // type_object
		query.setParameter(3, formirovanieId); // id_object
		query.setParameter(4, CODE_ZNACHENIE_JOURNAL_SPORT_OBED); // type_vish_object
		query.setParameter(5, row[3]); // id_vish_object
		query.setParameter(6, new TypedParameterValue(StandardBasicTypes.TIMESTAMP, prietOsk)); // date_acceptance
		query.setParameter(7, new TypedParameterValue(StandardBasicTypes.TIMESTAMP, null)); // date_termination
		query.setParameter(8, T0Start.USER); // user_reg
		query.setParameter(9, regDat); // date_reg
		query.executeUpdate();

		return id;
	}

	/** */
	private Integer saveDoc(Query query, Query queryUpdate, Integer codeRef, EntityManager em) {
		if (this.regNomSk == null || this.regDatSk == null) {
			return null; // няма как да се запише
		}

		String key = this.regNomSk + "/" + this.sdfY.format(this.regDatSk);
		if (this.docMap.containsKey(key)) {
			Integer docId = this.docMap.get(key);

			queryUpdate.setParameter(1, this.refName).setParameter(2, docId).setParameter(3, "%" + this.refName + "%").executeUpdate();

			return docId; // не се прави нов документ, а се дава по този номер вече записаният
		}

		Integer docId = T0Start.nextVal("seq_doc", em);

		query.setParameter(1, docId); // doc_id
		query.setParameter(2, T0Start.REGISTATRURA); // registratura_id
		query.setParameter(3, T0Start.REGISTER); // register_id
		query.setParameter(4, codeRef); // code_ref_corresp
		query.setParameter(5, this.regNomSk); // rn_doc
		query.setParameter(6, ""); // rn_prefix
		query.setParameter(7, new TypedParameterValue(StandardBasicTypes.INTEGER, null)); // rn_pored
		query.setParameter(8, "{" + UUID.randomUUID().toString().toUpperCase() + "}"); // guid
		query.setParameter(9, DocuConstants.CODE_ZNACHENIE_DOC_TYPE_IN); // doc_type
		query.setParameter(10, CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_SPORTNO_FORM); // doc_vid
		query.setParameter(11, this.regDatSk); // doc_date
		query.setParameter(12, "Заявление за ново вписване на " + this.refName); // otnosno
		query.setParameter(13, SysConstants.CODE_ZNACHENIE_DA); // valid
		query.setParameter(14, this.regDatSk); // valid_date
		query.setParameter(15, SysConstants.CODE_ZNACHENIE_DA); // processed
		query.setParameter(16, SysConstants.CODE_ZNACHENIE_DA); // free_access
		query.setParameter(17, 0); // count_files
		query.setParameter(18, T0Start.USER); // user_reg
		query.setParameter(19, this.regDatSk); // date_reg
		query.setParameter(20, SysConstants.CODE_ZNACHENIE_DA); // competence
		query.executeUpdate();

		this.docMap.put(key, docId);

		return docId;
	}

	/**  */
	private Integer saveFormirovanie(Query query, Integer codeRef, EntityManager em) {
		Integer id = T0Start.nextVal("seq_mms_sport_formirovanie", em);

		int statusObekt = DocuConstants.CODE_ZNACHENIE_STATUS_OBEKT_VPISAN;
		if (this.statusVpisvane != null //
			&& this.statusVpisvane.intValue() == DocuConstants.CODE_ZNACHENIE_STATUS_REG_ZALICHENO_VPISVANE) {
			statusObekt = DocuConstants.CODE_ZNACHENIE_STATUS_OBEKT_ZALICHEN;
		}

		query.setParameter(1, id); // id
		query.setParameter(2, new TypedParameterValue(StandardBasicTypes.INTEGER, this.typeSport)); // type_sport
		query.setParameter(3, this.regNomSk); // reg_nomer
		query.setParameter(4, codeRef); // id_object
		query.setParameter(5, CODE_ZNACHENIE_VID_SPORTNO_FORMIROVANIE_SK); // vid
		query.setParameter(6, this.reprezent); // predstavitelstvo
		query.setParameter(7, this.chairman); // predsedatel
		query.setParameter(8, null); // school_name
		query.setParameter(9, T0Start.USER); // user_reg
		query.setParameter(10, this.regDatSk); // date_reg
		query.setParameter(11, statusObekt); // status
		query.setParameter(12, this.regDatSk); // date_status
		query.setParameter(13, ""); // dop_info
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
		query.setParameter(9, this.regDatSk); // date_reg
		query.setParameter(10, 37); // ref_grj
		query.setParameter(11, this.eik); // nfl_eik
		query.setParameter(12, row[18]); // contact_phone
		query.setParameter(13, this.email); // contact_email
		query.setParameter(14, row[19]); // web_page
		query.setParameter(15, new TypedParameterValue(StandardBasicTypes.INTEGER, this.polzaCode)); // polza
		query.setParameter(16, null); // ref_latin
		query.setParameter(17, REF_MIG_NAME); // mig_login_name
		query.setParameter(18, this.reprezent);
		query.executeUpdate();

		String address = SearchUtils.trimToNULL((String) row[15]); // adres
		if (address != null) {
			Integer addrId = T0Start.nextVal("seq_adm_ref_addrs", em);

			// head-13 , obst-16
			String key = (row[13] + "_" + row[16]).toUpperCase().replaceAll(" ", "");
			Integer ekatte = this.ekatteMap.get(key);
			if (ekatte == null) {
				if ("гр. София".equalsIgnoreCase((String) row[13])) {
					ekatte = 68134;
				} else if ("гр. Пловдив".equalsIgnoreCase((String) row[13])) {
					ekatte = 56784;
				} else if ("с. Мусомища".equalsIgnoreCase((String) row[13])) {
					ekatte = 49432;
				} else if ("гр. Сливен,".equalsIgnoreCase((String) row[13])) {
					ekatte = 67338;
				} else {
					LOGGER.warn("  ! eik=" + this.eik + " uknown EKATTE: " + row[13] + " " + row[16]);
				}
			}

			String postCode = null;
			if (row[14] != null) { // pk
				postCode = "" + SearchUtils.asInteger(row[14]);
			}

			addrQuery.setParameter(1, addrId); // addr_id
			addrQuery.setParameter(2, code); // code_ref
			addrQuery.setParameter(3, DocuConstants.CODE_ZNACHENIE_ADDR_TYPE_CORRESP); // addr_type
			addrQuery.setParameter(4, 37); // addr_country
			addrQuery.setParameter(5, address); // addr_text
			addrQuery.setParameter(6, postCode); // post_code
			addrQuery.setParameter(7, ""); // post_box
			addrQuery.setParameter(8, new TypedParameterValue(StandardBasicTypes.INTEGER, ekatte)); // ekatte
			addrQuery.setParameter(9, new TypedParameterValue(StandardBasicTypes.INTEGER, null)); // raion
			addrQuery.setParameter(10, T0Start.USER); // user_reg
			addrQuery.setParameter(11, this.regDatSk); // date_reg
			addrQuery.executeUpdate();
		}

		return code;
	}

	/** */
	private Integer saveVpisvane(Query query, Integer formirovanieId, EntityManager em) {
		Integer id = T0Start.nextVal("seq_mms_vpisvane", em);

		query.setParameter(1, id); // id - ИД на вписване
		query.setParameter(2, CODE_ZNACHENIE_JOURNAL_SPORTS_FORMATIONS); // type_object - Тип на обекта
		query.setParameter(3, formirovanieId); // id_object - ИД на обекта

		// rn_doc_zaiavlenie - Рег. номер на Заявление за вписване
		query.setParameter(4, this.regNomSk);

		// date_doc_zaiavlenie - Дата на Заявление за вписване
		query.setParameter(5, this.regDatSk);

		// status_result_zaiavlenie - Статус на заявление
		query.setParameter(6, DocuConstants.CODE_ZNACHENIE_STATUS_ZAIAVLENIE_VPISAN);

		// reason_result - Основания за статуса на заявление
		query.setParameter(7, new TypedParameterValue(StandardBasicTypes.INTEGER, null));

		// reason_result_text - Основания за статуса на заявление (текст)
		query.setParameter(8, "");

		// rn_doc_result - Рег. номер на Заповед за вписване / Заповед за отказ от вписване
		query.setParameter(9, this.regNomSk);

		// date_doc_result - Дата на Заповед за вписване / Заповед за отказ от вписване
		query.setParameter(10, this.regDatSk);

		// rn_doc_licenz - Рег. номер на лиценз/удостоверение
		query.setParameter(11, this.licenz);

		// date_doc_licenz - Дата на лиценз/удостоверение
		query.setParameter(12, new TypedParameterValue(StandardBasicTypes.TIMESTAMP, this.licenzDate));

		// status_vpisvane - Статус на вписване
		query.setParameter(13, this.statusVpisvane);

		// reason_vpisvane - Основание за статус на вписване
		query.setParameter(14, new TypedParameterValue(StandardBasicTypes.INTEGER, null));

		// reason_vpisvane_text - Основание за статус на вписване (текст)
		query.setParameter(15, this.reasonVpisvaneText);

		// rn_doc_vpisvane - Рег. номер на Заповед за прекратяване / Заповед за отнемане и Заповед за заличаване
		query.setParameter(16, "");

		// date_doc_vpisvane - Дата на Заповед за прекратяване / аповед за отнемане и Заповед за заличаване
		Date dateDocVpisvane = null;
		if (this.statusVpisvane.intValue() == DocuConstants.CODE_ZNACHENIE_STATUS_REG_ZALICHENO_VPISVANE) {
			dateDocVpisvane = this.dataТermination;
		}
		query.setParameter(17, new TypedParameterValue(StandardBasicTypes.TIMESTAMP, dateDocVpisvane));

		// nachin_poluchavane - Начин на получаване на резултата
		query.setParameter(18, new TypedParameterValue(StandardBasicTypes.INTEGER, null));

		// addr_mail_poluchavane - Адрес/Email
		query.setParameter(19, "");

		// date_status_zaiavlenie - Дата на статус на заявлението
		query.setParameter(20, this.regDatSk);

		query.setParameter(21, new TypedParameterValue(StandardBasicTypes.INTEGER, null)); // vid_sport - Вид спорт
		query.setParameter(22, new TypedParameterValue(StandardBasicTypes.INTEGER, null)); // dlajnost - Длъжност

		query.setParameter(23, this.dopInfo.toString()); // dop_info - Допълнителна информация
		query.setParameter(24, T0Start.USER); // user_reg
		query.setParameter(25, this.regDatSk); // date_reg

		// date_status_vpisvane - Дата на статус на вписването
		query.setParameter(26, this.statusVpisvaneDate);

		query.executeUpdate();
		return id;
	}

	/** */
	private void saveVpisvaneDoc(Query query, Integer vpisvaneId, Integer docId, Integer formirovanieId, EntityManager em) {
		if (docId == null) {
			return;
		}
		Integer id = T0Start.nextVal("seq_mms_vpisvane_doc", em);

		query.setParameter(1, id); // id
		query.setParameter(2, vpisvaneId); // id_vpisvane
		query.setParameter(3, docId); // id_doc
		query.setParameter(4, T0Start.USER); // user_reg
		query.setParameter(5, this.regDatSk); // date_reg
		query.setParameter(6, formirovanieId); // id_object
		query.setParameter(7, CODE_ZNACHENIE_JOURNAL_SPORTS_FORMATIONS); // type_object
		query.executeUpdate();
	}
}
