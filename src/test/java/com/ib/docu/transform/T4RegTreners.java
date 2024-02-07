package com.ib.docu.transform;

import static com.ib.docu.system.DocuConstants.CODE_CLASSIF_DLAJNOST;
import static com.ib.docu.system.DocuConstants.CODE_CLASSIF_REASON_STATUS_ZAIAVLENIE;
import static com.ib.docu.system.DocuConstants.CODE_CLASSIF_VIDOVE_SPORT;
import static com.ib.docu.system.DocuConstants.CODE_ZNACHENIE_JOURNAL_COACHES;
import static com.ib.docu.system.DocuConstants.CODE_ZNACHENIE_REF_TYPE_FZL;
import static com.ib.docu.system.DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_TREN_KADRI;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
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
 * ММС – миграция на треньорски кадри
 *
 * @author belev
 */
public class T4RegTreners {

	private static final Logger LOGGER = LoggerFactory.getLogger(T4RegTreners.class);

	/**  */
	private static final String REF_MIG_NAME = "mms_coaches"; // mms_coaches

	/** @param args */
	public static void main(String[] args) {
		T4RegTreners t = new T4RegTreners();

		t.validate(JPA.getUtil());

//		t.clear(JPA.getUtil());

		t.transfer(JPA.getUtil());

//		t.regix(JPA.getUtil());

		System.exit(0); // не е ясно защо не терминира ако го няма
	}

	private SimpleDateFormat	sdfDMY	= new SimpleDateFormat("dd.MM.yyyy");
	private SimpleDateFormat	sdfY	= new SimpleDateFormat("yyyy");

	private Map<String, Integer>	vidSportMap;
	private Map<String, Integer>	dlajnostMap;
	private Map<String, Integer>	osnovanieMap;

	// данни, които трябва по време на една итерация на процеса
	private Map<String, Integer> docMap = new HashMap<>(); // key=vh_nom, value=docId

	private Integer	regNom;
	private String	egn;
	private String	lnc;
	private String	nomDoc;

	private Date	regDate;
	private String	rnDocZaiavlenie;
	private Date	dateDocZaiavlenie;
	private String	rnDocResult;
	private Date	dateDocResult;
	private Integer	dlajnost;
	private Integer	reasonVpisvane;
	private Integer	vidSport;
	private String  vidSportText;
	private String	refName;
	private String	ime;
	private String	prez;
	private String	fam;
	private String	tel;
	private String	email;
	private String	dopInfo;
	private Integer	vnositelCode;		// кода на референта в заявлението

	private Set<String> fizLice = new HashSet<>();

	/**  */
	public T4RegTreners() {
		this.vidSportMap = T0Start.findDecodeMap(JPA.getUtil().getEntityManager(), CODE_CLASSIF_VIDOVE_SPORT);
		this.dlajnostMap = T0Start.findDecodeMap(JPA.getUtil().getEntityManager(), CODE_CLASSIF_DLAJNOST);
		this.osnovanieMap = T0Start.findDecodeMap(JPA.getUtil().getEntityManager(), CODE_CLASSIF_REASON_STATUS_ZAIAVLENIE, new String[] { ",", "\\." });

		this.fizLice.add("ФИЗИЧЕСКО ЛИЦЕ");
		this.fizLice.add("ФИЗИЧЕСКО  ЛИЦЕ");
		this.fizLice.add("ФИЗИЧЕСКИ ЛИЦА");
		this.fizLice.add("ФИЗЧЕСКО ЛИЦЕ");
	}

	/** @param jpa */
	public void clear(JPA jpa) {
		LOGGER.info("");
		LOGGER.info("Start clear register data");

		try {
			EntityManager em = jpa.getEntityManager();

			jpa.begin();
			int cnt = em.createNativeQuery("delete from adm_ref_addrs where code_ref in (" //
				+ "select code from adm_referents where mig_login_name = ?1 and code in (select id_object from mms_coaches))") //
				.setParameter(1, REF_MIG_NAME).executeUpdate();
			jpa.commit();
			LOGGER.info("   deleted " + cnt + " rows from table adm_ref_addrs");

			jpa.begin();
			cnt = em.createNativeQuery( //
				"delete from adm_referents where mig_login_name = ?1 and code in (select id_object from mms_coaches)") //
				.setParameter(1, REF_MIG_NAME).executeUpdate();
			jpa.commit();
			LOGGER.info("   deleted " + cnt + " rows from table adm_referents");

			jpa.begin();
			cnt = em.createNativeQuery( // после по това число ще знам кои да изтрия
				"update doc set user_last_mod = -4000 where doc_id in (select id_doc from mms_vpisvane_doc where type_object = ?1)") //
				.setParameter(1, CODE_ZNACHENIE_JOURNAL_COACHES).executeUpdate();
			LOGGER.info("   update " + cnt + " rows from table doc.user_last_mod=-4000");

			cnt = em.createNativeQuery("delete from mms_vpisvane_doc where type_object = ?1") //
				.setParameter(1, CODE_ZNACHENIE_JOURNAL_COACHES).executeUpdate();
			LOGGER.info("   deleted " + cnt + " rows from table mms_vpisvane_doc");

			cnt = em.createNativeQuery("delete from doc_referents where doc_id in (select distinct doc_id from doc where user_last_mod = -4000)").executeUpdate();
			LOGGER.info("   deleted " + cnt + " rows from table doc_referents");

			cnt = em.createNativeQuery("delete from doc where user_last_mod = -4000").executeUpdate();
			LOGGER.info("   deleted " + cnt + " rows from table doc");
			jpa.commit();

			jpa.begin();
			cnt = em.createNativeQuery("delete from mms_vpisvane where type_object = ?1") //
				.setParameter(1, CODE_ZNACHENIE_JOURNAL_COACHES).executeUpdate();
			jpa.commit();
			LOGGER.info("   deleted " + cnt + " rows from table mms_vpisvane");

			jpa.begin();
			cnt = em.createNativeQuery("delete from mms_vid_sport where tip_object = ?1") //
				.setParameter(1, CODE_ZNACHENIE_JOURNAL_COACHES).executeUpdate();
			jpa.commit();
			LOGGER.info("   deleted " + cnt + " rows from table mms_vid_sport");

			jpa.begin();
			cnt = em.createNativeQuery("delete from mms_coaches_diploms").executeUpdate();
			jpa.commit();
			LOGGER.info("   deleted " + cnt + " rows from table mms_coaches_diploms");

			jpa.begin();
			cnt = em.createNativeQuery("delete from mms_coaches").executeUpdate();
			jpa.commit();
			LOGGER.info("   deleted " + cnt + " rows from table mms_coaches");

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
				+ ", ref_grj, fzl_egn, contact_phone, contact_email, web_page, fzl_lnc, nom_doc, mig_login_name, ime, prezime, familia )" //
				+ " values (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9, ?10, ?11, ?12, ?13, ?14, ?15, ?16, ?17, ?18, ?19, ?20)");

			Query coachesQuery = em.createNativeQuery("insert into mms_coaches (" //
				+ " id, id_object, user_reg, date_reg, status, date_status, dop_info, reg_nomer )" //
				+ " values (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8)");

			Query diplomsQuery = em.createNativeQuery("insert into mms_coaches_diploms (" //
				+ " id, id_coaches, reg_nomer, year_issued, uchebno_zavedenie, ucheb_zav_text, dop_info, vid_doc, seria_fabrnom )" //
				+ " values (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9)");

			Query vidSportQuery = em.createNativeQuery("insert into mms_vid_sport (" //
				+ " id, tip_object, id_object, user_reg, date_reg, vid_sport ) " //
				+ "values (?1, ?2, ?3, ?4, ?5, ?6)");

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

			Query checkReferentQuery = em.createNativeQuery( //
				"select ref_id, code, ref_name from adm_referents where ref_type = ?1 and (fzl_egn = ?2 or fzl_lnc = ?3 or upper(nom_doc) = ?4)") //
				.setParameter(1, CODE_ZNACHENIE_REF_TYPE_FZL);

			Query checkCoachesQuery = em.createNativeQuery("select id from mms_coaches where id_object = ?1");

			Query checkVpisvaneQuery = em.createNativeQuery( //
				"select id from mms_vpisvane where type_object = ?1 and id_object = ?2 and vid_sport = ?3 and dlajnost = ?4") //
				.setParameter(1, CODE_ZNACHENIE_JOURNAL_COACHES);

			StringBuilder select = new StringBuilder();
			select.append(" select t.reg_nom, t.ime, t.prez, t.fam "); // 0,1,2,3
			select.append(" , t.sport, t.egn, cast (t.lnc as varchar) lnc, cast (t.l_nom as varchar) l_nom "); // 4,5,6,7
			select.append(" , t.tel, t.email, t.reg_dat, t.rola "); // 8,9,10,11
			select.append(" , t.osn, t.dipl, t.vh_nom, t.vnositel, t.zap_nom_dat, r.code "); // 12,13,14,15,16,17
			select.append(" from reg_treners t ");
			select.append(" left outer join adm_referents r on upper(r.ref_name) = upper(REPLACE(REPLACE(t.vnositel, '„', '\"'), '”', '\"'))");
			select.append(" left outer join mms_sport_obedinenie o on o.id_object = r.code ");
			select.append(" order by 1 ");

			List<Object[]> rows = em.createNativeQuery(select.toString()).getResultList();

			int max = 0; // накря трябва да се оправи брояча с полученото число

			int insertCnt = 0;
			jpa.begin();
			for (Object[] row : rows) {

				setupData(row); // за да се намерят данните !!! МНОГО ВАЖНО !!!

				if (row[0] == null || this.regNom == null) {
					continue;
				}
				if (max < this.regNom.intValue()) {
					max = this.regNom;
				}

				List<Object[]> referent = findReferent(checkReferentQuery);

				if (referent.isEmpty()) { // запис на всичко
					Integer codeRef = saveReferent(referentQuery, em); // adm_referents (6.1)

					Integer coachesId = saveCoaches(coachesQuery, diplomsQuery, row, codeRef, em); // mms_coaches/mms_coaches_diploms
																									// (6.2/6.3)
					saveVidSport(vidSportQuery, coachesId, em); // mms_vid_sport (6.?)

					Integer vpisvaneId = saveVpisvane(vpisvaneQuery, coachesId, em); // mms_vpisvane (6.4)

					Integer docId = saveDoc(docQuery, docQueryUpdate, codeRef, em); // doc (6.5)

					saveVpisvaneDoc(vpisvaneDocQuery, vpisvaneId, docId, coachesId, em); // mms_vpisvane_doc (6.6)

				} else { // допълнителна логика
					Integer codeRef = SearchUtils.asInteger(referent.get(0)[1]);

					// Проверява в базата, има ли треньор с това лице
					List<Object> coaches = checkCoachesQuery.setParameter(1, codeRef).getResultList();

					if (coaches.isEmpty()) { // няма и се изпълняват т. 6.2, 6.3, 6.4, 6.5, 6.6

						Integer coachesId = saveCoaches(coachesQuery, diplomsQuery, row, codeRef, em); // mms_coaches/mms_coaches_diploms
																										// (6.2/6.3)
						saveVidSport(vidSportQuery, coachesId, em); // mms_vid_sport (6.?)

						Integer vpisvaneId = saveVpisvane(vpisvaneQuery, coachesId, em); // mms_vpisvane (6.4)

						Integer docId = saveDoc(docQuery, docQueryUpdate, codeRef, em); // doc (6.5)

						saveVpisvaneDoc(vpisvaneDocQuery, vpisvaneId, docId, coachesId, em); // mms_vpisvane_doc (6.6)

					} else { // Проверява се има ли вписване за него за същата длъжност и вид спорт
						Integer coachesId = SearchUtils.asInteger(coaches.get(0));

						boolean insert;
						if (this.dlajnost == null || this.vidSport == null) {
							insert = true; // няма как да се валидира
						} else {
							List<Object> vpisvane = checkVpisvaneQuery.setParameter(2, coachesId) //
								.setParameter(3, this.vidSport).setParameter(4, this.dlajnost).getResultList();
							insert = vpisvane.isEmpty();
						}

						if (insert) { // няма и се изпълняват т. 6.4, 6.5, 6.6

							Integer vpisvaneId = saveVpisvane(vpisvaneQuery, coachesId, em); // mms_vpisvane (6.4)

							Integer docId = saveDoc(docQuery, docQueryUpdate, codeRef, em); // doc (6.5)

							saveVpisvaneDoc(vpisvaneDocQuery, vpisvaneId, docId, coachesId, em); // mms_vpisvane_doc(6.6)
						}
					}
				}

				insertCnt++;
				if (insertCnt % 100 == 0) {
					LOGGER.info("  " + insertCnt);
				}
			}

			// оправям брояча - трябва да се оправи регистъра но ръчно
//			int cnt = em.createNativeQuery("update sid set next_val = ?1 where object = ?2") //
//				.setParameter(1, max + 1).setParameter(2, "trener").executeUpdate();
//			if (cnt == 0) {
//				em.createNativeQuery("insert into sid (object, next_val) values (?1, ?2)") //
//					.setParameter(1, "trener").setParameter(2, max + 1).executeUpdate();
//			}

			jpa.commit();

			LOGGER.info("  " + rows.size());
			LOGGER.info("Transfer register data - COMPLETE");

		} catch (Exception e) {
			jpa.rollback();

			String s1 = this.egn != null ? " EGN=" + this.egn : "";
			String s2 = this.lnc != null ? " LNC=" + this.lnc : "";
			String s3 = this.nomDoc != null ? " L_NOM=" + this.nomDoc : "";

			LOGGER.error("  ! reg_treners.reg_nom=" + this.regNom + ";" + s1 + s2 + s3 + " ! System ERROR transfer register -> " + e.getMessage(), e);

		} finally {
			jpa.closeConnection();
		}
	}

	/** @param jpa */
	public void validate(JPA jpa) {
		LOGGER.info("");
		LOGGER.info("Start validate register data");

		try {
			EntityManager em = jpa.getEntityManager();

			StringBuilder select = new StringBuilder();
			select.append(" select t.reg_nom, t.ime, t.prez, t.fam "); // 0,1,2,3
			select.append(" , t.sport, t.egn, cast (t.lnc as varchar) lnc, cast (t.l_nom as varchar) l_nom "); // 4,5,6,7
			select.append(" , t.tel, t.email, t.reg_dat, t.rola "); // 8,9,10,11
			select.append(" , t.osn, t.dipl, t.vh_nom, t.vnositel, t.zap_nom_dat, r.code "); // 12,13,14,15,16,17
			select.append(" from reg_treners t ");
			select.append(" left outer join adm_referents r on upper(r.ref_name) = upper(REPLACE(REPLACE(t.vnositel, '„', '\"'), '”', '\"'))");
			select.append(" left outer join mms_sport_obedinenie o on o.id_object = r.code ");
			select.append(" order by 1 ");

			@SuppressWarnings("unchecked")
			List<Object[]> rows = em.createNativeQuery(select.toString()).getResultList();

			long now = System.currentTimeMillis();

			int errcnt = 0;

			for (Object[] row : rows) {
				if (row[0] == null) {
					continue;
				}

				StringBuilder errors = new StringBuilder();
				String prefix = "\n\t";

				String ime1 = SearchUtils.trimToNULL((String) row[1]); // ime
				if (ime1 != null && !T0Start.isBgSpaces(ime1)) {
//					errors.append(prefix + "NOT_BG ime=" + row[1]);
				}
				String prez1 = SearchUtils.trimToNULL((String) row[2]); // prez
				if (prez1 != null && !T0Start.isBgSpaces(prez1.replaceAll(" ", ""))) { // някакъв специален интервал
//					errors.append(prefix + "NOT_BG prez=" + row[2]);
				}
				String fam1 = SearchUtils.trimToNULL((String) row[3]); // fam
				if (fam1 != null && !T0Start.isBgSpaces(fam1)) {
					errors.append(prefix + "NOT_BG fam=" + row[3]);
				}

				String egn1 = SearchUtils.trimToNULL((String) row[5]); // egn
				if (egn1 != null && !"-".equals(egn1)) {

					if (egn1.length() < 10) {
						egn1 = ("0000000000" + egn1).substring(egn1.length());
					}

					if (!ValidationUtils.isValidEGN(egn1)) {
						errors.append(prefix + "NOT_VALID egn=" + row[5]);
					}
				}
				String lnc1 = SearchUtils.trimToNULL((String) row[6]); // lnc
				if (lnc1 != null && !ValidationUtils.isValidLNCH(lnc1)) { // lnc
					errors.append(prefix + "NOT_VALID lnc=" + row[6]);
				}

				String sport = SearchUtils.trimToNULL_Upper((String) row[4]); // sport
				if (sport != null) {
					sport = sport.replaceAll(" ", "");
					if (!this.vidSportMap.containsKey(sport)) {
						errors.append(prefix + "UNKNOWN sport=" + row[4] + "; classif=" + CODE_CLASSIF_VIDOVE_SPORT);
					}
				}

				String tel1 = SearchUtils.trimToNULL((String) row[8]); // tel
				if (tel1 != null) { // трябва долу да се сложи запетая
//					try {
//						Long.parseLong(tel1);
//					} catch (Exception e) {
//						errors.append(prefix + "NOT_VALID tel=" + row[8]);
//					}
				}
				String email1 = SearchUtils.trimToNULL((String) row[9]); // email
				if (email1 != null && !"–".equals(email1) && !"-".equals(email1)) {
					String[] split = email1.replaceAll(",", ";").split(";");
					boolean err = false;
					for (String s : split) {
						if (!ValidationUtils.isEmailValid(s.replaceAll(" ", "").trim())) { // тука е някакъв специален символ
							err = true;
						}
					}
					if (err) {
						errors.append(prefix + "NOT_VALID email=" + row[9]);
					}
				}

				String rola = SearchUtils.trimToNULL_Upper((String) row[11]); // rola
				if (rola != null && rola.indexOf("\r\n") != -1) {
					rola = rola.replace("\r\n", " ");
				}
				if (rola != null && !this.dlajnostMap.containsKey(rola.replaceAll(" ", ""))) {
					errors.append(prefix + "UNKNOWN rola=" + row[11] + "; classif=" + CODE_CLASSIF_DLAJNOST);
				}
				String osn = SearchUtils.trimToNULL_Upper((String) row[12]); // osn
				if (osn != null) {
					osn = osn.replaceAll(" ", "").replaceAll(",", "").replaceAll("\\.", "");
					if (!this.osnovanieMap.containsKey(osn)) {
						errors.append(prefix + "UNKNOWN osn=" + row[12] + "; classif=" + CODE_CLASSIF_REASON_STATUS_ZAIAVLENIE);
					}
				}

				String vhNom = SearchUtils.trimToNULL((String) row[14]); // vh_nom
				if (vhNom != null) {
					if (vhNom.indexOf(".20 г.") != -1) {
						vhNom = vhNom.replace(".20 г.", ".2020 г.");
					}

					vhNom = vhNom.replaceAll("//", "/").replaceAll(",", "").replaceAll(";", "\\.");
					int ind = vhNom.indexOf("г.");
					if (ind != -1) {
						if (ind < vhNom.length() - 2) { // това трябва да дойде като допинфо
						}
						vhNom = vhNom.substring(0, ind);
					}

					try {
						String[] split = vhNom.trim().split("/");

						Date date = null;
						if (split.length == 2) { // номер и дата стандартно
							date = this.sdfDMY.parse(split[1].trim());

						} else if (split.length == 3) { // 14-00-248/3/28.01.2022 г.
							date = this.sdfDMY.parse(split[2].trim());

						} else {
							date = this.sdfDMY.parse(split[1].trim());
							// това трябва да дойде като допинфо
						}
						if (date != null && date.getTime() > now) {
							errors.append(prefix + "AFTER TODAY vh_nom=" + row[14]);
						}
					} catch (Exception e) {
						errors.append(prefix + "NOT_VALID vh_nom=" + row[14]);
					}
				} else {
					errors.append(prefix + "NULL vh_nom");
				}

				String vnositel = SearchUtils.trimToNULL_Upper((String) row[15]); // vnositel
				if (vnositel != null && !this.fizLice.contains(vnositel) && row[17] == null) {
					errors.append(prefix + "UNKNOWN vnositel=" + row[15] + "; adm_referents.ref_name");
				}

				String zapNomDat = SearchUtils.trimToNULL((String) row[16]); // zap_nom_dat
				if (zapNomDat != null) {
					int ind = zapNomDat.indexOf("г.");
					if (ind != -1) {
						if (ind < zapNomDat.length() - 2) { // това трябва да дойде като допинфо
						}
						zapNomDat = zapNomDat.substring(0, ind);
					}
					try {
						String[] split = zapNomDat.trim().split("/");

						Date date = null;
						if (split.length == 1) { // само номер
							//
						} else if (split.length == 2) { // номер и дата
							date = this.sdfDMY.parse(split[1].trim());

						} else { // проблем
							errors.append(prefix + "NOT_VALID zap_nom_dat=" + row[16]);
						}
						if (date != null && date.getTime() > now) {
							errors.append(prefix + "AFTER TODAY zap_nom_dat=" + row[16]);
						}
					} catch (Exception e) { // по голям проблем
						errors.append(prefix + "NOT_VALID zap_nom_dat=" + row[16]);
					}
				} else {
					errors.append(prefix + "NULL zap_nom_dat");
				}

				if (errors.length() > 0) {
					errcnt++;
					String s1 = egn1 != null ? " EGN=" + row[5] : "";
					String s2 = lnc1 != null ? " LNC=" + row[6] : "";
					String s3 = SearchUtils.trimToNULL((String) row[7]) != null ? " L_NOM=" + row[7] : "";

					System.out.println("reg_treners.reg_nom=" + SearchUtils.asInteger(row[0]) + ";" + s1 + s2 + s3 + errors);
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
//		mms_coaches_diploms
// 		mms_coaches

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
		select.append(" select 'seq_mms_coaches_diploms' seq_name, max (id) max_id from mms_coaches_diploms ");
		select.append(" union all ");
		select.append(" select 'seq_mms_coaches' seq_name, max (id) max_id from mms_coaches ");

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
		this.regNom = SearchUtils.asInteger(row[0]);

		this.egn = SearchUtils.trimToNULL((String) row[5]); // egn
		if (this.egn != null && this.egn.length() < 10) {
			this.egn = ("0000000000" + this.egn).substring(this.egn.length());
		}

		this.lnc = SearchUtils.trimToNULL((String) row[6]); // lnc
		this.nomDoc = SearchUtils.trimToNULL((String) row[7]); // l_nom

		StringBuilder refNameSb = new StringBuilder();
		this.ime = SearchUtils.trimToNULL((String) row[1]); // ime
		if (this.ime != null) {
			refNameSb.append(this.ime);
		}
		this.prez = SearchUtils.trimToNULL((String) row[2]); // prez
		if (this.prez != null) {
			if (this.prez.indexOf(" ") != -1) { // някакъв специален интервал
				this.prez = this.prez.replaceAll(" ", "");
			}
			refNameSb.append(" " + this.prez);
		}
		this.fam = SearchUtils.trimToNULL((String) row[3]); // fam
		if (this.fam != null) {
			refNameSb.append(" " + this.fam);
		}
		this.refName = refNameSb.toString().trim();

		String vnositel = SearchUtils.trimToNULL_Upper((String) row[15]); // vnositel

		this.vnositelCode = null;
		if (vnositel != null && this.fizLice.contains(vnositel)) {
			this.vnositelCode = Integer.MIN_VALUE; // по това ще се знае че вносителя е физическото лице
		} else {
			this.vnositelCode = SearchUtils.asInteger(row[17]); // r.code
		}

		this.regDate = row[10] != null ? (Date) row[10] : T0Start.TRANSFER_DATE; // reg_dat

		this.dlajnost = null;
		String rola = SearchUtils.trimToNULL_Upper((String) row[11]); // rola
		if (rola != null && rola.indexOf("\r\n") != -1) {
			rola = rola.replace("\r\n", " ");
		}
		if (rola != null) {
			rola = rola.replaceAll(" ", "");
			this.dlajnost = this.dlajnostMap.get(rola);
		}
		this.reasonVpisvane = null;
		String osn = SearchUtils.trimToNULL_Upper((String) row[12]); // osn
		if (osn != null) {
			osn = osn.replaceAll(" ", "").replaceAll(",", "").replaceAll("\\.", "");
			this.reasonVpisvane = this.osnovanieMap.get(osn);
		}
		this.vidSport = null;
		this.vidSportText = null;
		String sport = SearchUtils.trimToNULL_Upper((String) row[4]); // sport
		if (sport != null) {
			sport = sport.replaceAll(" ", "");
			this.vidSport = this.vidSportMap.get(sport);
		}
		if (this.vidSport == null) {
			this.vidSportText = (String) row[4];
		}

		this.tel = SearchUtils.trimToNULL((String) row[8]); // tel
		if (this.tel != null && this.tel.indexOf(';') != -1) {
			this.tel = this.tel.replaceAll(";", ",");
		}

		this.email = SearchUtils.trimToNULL((String) row[9]); // email
		if ("–".equals(this.email) || "-".equals(this.email)) {
			this.email = null;
		} else if (this.email != null) {
			this.email = this.email.replaceAll(",", ";");
		}

		StringBuilder dopInfoSb = new StringBuilder();

		this.rnDocZaiavlenie = null;
		this.dateDocZaiavlenie = null;
		String vhNom = SearchUtils.trimToNULL((String) row[14]); // vh_nom
		if (vhNom != null) {
			if (vhNom.indexOf(".20 г.") != -1) {
				vhNom = vhNom.replace(".20 г.", ".2020 г.");
			}

			vhNom = vhNom.replaceAll("//", "/").replaceAll(",", "").replaceAll(";", "\\.");
			int ind = vhNom.indexOf("г.");
			if (ind != -1) {
				if (ind < vhNom.length() - 2) {
					dopInfoSb.append(row[14]);
				}
				vhNom = vhNom.substring(0, ind);
			}

			try {
				String[] split = vhNom.trim().split("/");

				if (split.length == 2) { // номер и дата стандартно
					this.rnDocZaiavlenie = split[0].trim();
					this.dateDocZaiavlenie = this.sdfDMY.parse(split[1].trim());

				} else if (split.length == 3) { // 14-00-248/3/28.01.2022 г.
					this.rnDocZaiavlenie = split[0].trim() + "/" + split[1].trim();
					this.dateDocZaiavlenie = this.sdfDMY.parse(split[2].trim());

				} else {
					this.rnDocZaiavlenie = split[0].trim();
					this.dateDocZaiavlenie = this.sdfDMY.parse(split[1].trim());

					if (dopInfoSb.length() == 0) {
						dopInfoSb.append(row[14]);
					}
				}
			} catch (Exception e) {
				this.rnDocZaiavlenie = null;
				this.dateDocZaiavlenie = null;
			}
		}

		this.rnDocResult = null;
		this.dateDocResult = null;
		String zapNomDat = SearchUtils.trimToNULL((String) row[16]); // zap_nom_dat
		if (zapNomDat != null) {
			int ind = zapNomDat.indexOf("г.");
			if (ind != -1) {
				if (ind < zapNomDat.length() - 2) { // това трябва да дойде като допинфо
					if (dopInfoSb.length() > 0) {
						dopInfoSb.append("\r\n");
					}
					dopInfoSb.append("Заповед за вписване: " + row[16]);
				}
				zapNomDat = zapNomDat.substring(0, ind);
			}
			try {
				String[] split = zapNomDat.trim().split("/");
				if (split.length == 1) { // само номер
					this.rnDocResult = split[0].trim();
					this.dateDocResult = this.regDate;

				} else if (split.length == 2) { // номер и дата
					this.rnDocResult = split[0].trim();
					this.dateDocResult = this.sdfDMY.parse(split[1].trim());
				}
			} catch (Exception e) {
				this.rnDocResult = null;
				this.dateDocResult = null;
			}
		}
		this.dopInfo = dopInfoSb.toString().trim();
	}

	/**
	 * Намира данни за лицето по егн/лнч/лном
	 */
	@SuppressWarnings("unchecked")
	private List<Object[]> findReferent(Query query) {
		if (this.egn != null) { // egn
			query.setParameter(2, this.egn);
		} else {
			query.setParameter(2, String.valueOf(Integer.MAX_VALUE));
		}
		if (this.lnc != null) { // lnc
			query.setParameter(3, this.lnc);
		} else {
			query.setParameter(3, String.valueOf(Integer.MAX_VALUE));
		}
		if (this.nomDoc != null) { // l_nom
			query.setParameter(4, this.nomDoc.toUpperCase());
		} else {
			query.setParameter(4, String.valueOf(Integer.MAX_VALUE));
		}

		return query.getResultList();
	}

	/**  */
	private Integer saveCoaches(Query query, Query diplomsQuery, Object[] row, Integer codeRef, EntityManager em) {
		Integer id = T0Start.nextVal("seq_mms_coaches", em);

		query.setParameter(1, id); // id
		query.setParameter(2, codeRef); // id_object
		query.setParameter(3, T0Start.USER); // user_reg
		query.setParameter(4, this.regDate); // date_reg
		query.setParameter(5, DocuConstants.CODE_ZNACHENIE_STATUS_OBEKT_VPISAN); // status
		query.setParameter(6, new TypedParameterValue(StandardBasicTypes.TIMESTAMP, this.dateDocResult)); // date_status
		query.setParameter(7, ""); // dop_info
		query.setParameter(8, this.regNom.toString()); // reg_nomer реално не трябва, но не пречи
		query.executeUpdate();

		String dipl = SearchUtils.trimToNULL((String) row[13]);
		if (dipl != null && !"-".equals(dipl)) {
			Integer diplomId = T0Start.nextVal("seq_mms_coaches_diploms", em);
			diplomsQuery.setParameter(1, diplomId); // id
			diplomsQuery.setParameter(2, id); // id_coaches
			diplomsQuery.setParameter(3, null); // reg_nomer от къде се взима
			diplomsQuery.setParameter(4, new TypedParameterValue(StandardBasicTypes.INTEGER, null)); // year_issued от къде
																										// се взима
			diplomsQuery.setParameter(5, new TypedParameterValue(StandardBasicTypes.INTEGER, null)); // uchebno_zavedenie от
																										// къде се взима
			diplomsQuery.setParameter(6, null); // ucheb_zav_text от къде се взима
			diplomsQuery.setParameter(7, dipl); // dop_info
			diplomsQuery.setParameter(8, new TypedParameterValue(StandardBasicTypes.INTEGER, null)); // vid_doc от къде се
																										// взима
			diplomsQuery.setParameter(9, null); // seria_fabrnom от къде се взима
			diplomsQuery.executeUpdate();
		}

		return id;
	}

	/** */
	private Integer saveDoc(Query query, Query queryUpdate, Integer codeRef, EntityManager em) {
		if (this.rnDocZaiavlenie == null || this.dateDocZaiavlenie == null) {
			return null; // няма как да се запише
		}

		String key = this.rnDocZaiavlenie + "/" + this.sdfY.format(this.dateDocZaiavlenie);
		if (this.docMap.containsKey(key)) {
			Integer docId = this.docMap.get(key);

			queryUpdate.setParameter(1, this.refName).setParameter(2, docId).setParameter(3, "%" + this.refName + "%").executeUpdate();

			return docId; // не се прави нов документ, а се дава по този номер вече записаният
		}

		if (this.vnositelCode == null) { // това означава че няма вносител и трябва да се нулира
			codeRef = null;
		} else if (!this.vnositelCode.equals(Integer.MIN_VALUE)) {
			codeRef = this.vnositelCode; // това означава че вносителя е федерация
		}

		Integer docId = T0Start.nextVal("seq_doc", em);

		query.setParameter(1, docId); // doc_id
		query.setParameter(2, T0Start.REGISTATRURA); // registratura_id
		query.setParameter(3, T0Start.REGISTER); // register_id
		query.setParameter(4, new TypedParameterValue(StandardBasicTypes.INTEGER, codeRef)); // code_ref_corresp
		query.setParameter(5, this.rnDocZaiavlenie); // rn_doc
		query.setParameter(6, ""); // rn_prefix
		query.setParameter(7, new TypedParameterValue(StandardBasicTypes.INTEGER, null)); // rn_pored
		query.setParameter(8, "{" + UUID.randomUUID().toString().toUpperCase() + "}"); // guid
		query.setParameter(9, DocuConstants.CODE_ZNACHENIE_DOC_TYPE_IN); // doc_type
		query.setParameter(10, CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_TREN_KADRI); // doc_vid
		query.setParameter(11, this.dateDocZaiavlenie); // doc_date
		query.setParameter(12, "Заявление за ново вписване на " + this.refName.toString().trim()); // otnosno
		query.setParameter(13, SysConstants.CODE_ZNACHENIE_DA); // valid
		query.setParameter(14, this.dateDocZaiavlenie); // valid_date
		query.setParameter(15, SysConstants.CODE_ZNACHENIE_DA); // processed
		query.setParameter(16, SysConstants.CODE_ZNACHENIE_DA); // free_access
		query.setParameter(17, 0); // count_files
		query.setParameter(18, T0Start.USER); // user_reg
		query.setParameter(19, this.regDate); // date_reg
		query.setParameter(20, SysConstants.CODE_ZNACHENIE_DA); // competence
		query.executeUpdate();

		this.docMap.put(key, docId);

		return docId;
	}

	/**  */
	private Integer saveReferent(Query query, EntityManager em) {
		Integer refId = T0Start.nextVal("seq_adm_referents", em);
		Integer code = T0Start.nextVal("seq_adm_referents_code", em);

		query.setParameter(1, refId); // ref_id
		query.setParameter(2, code); // code
		query.setParameter(3, 0); // code_parent
		query.setParameter(4, CODE_ZNACHENIE_REF_TYPE_FZL); // ref_type
		query.setParameter(5, this.refName); // ref_name
		query.setParameter(6, T0Start.MIN_DATE); // date_ot
		query.setParameter(7, T0Start.MAX_DATE); // date_do
		query.setParameter(8, T0Start.USER); // user_reg
		query.setParameter(9, this.regDate); // date_reg
		query.setParameter(10, 37); // ref_grj
		query.setParameter(11, this.egn); // fzl_egn
		query.setParameter(12, this.tel); // contact_phone
		query.setParameter(13, this.email); // contact_email
		query.setParameter(14, null); // web_page
		query.setParameter(15, this.lnc); // fzl_lnc
		query.setParameter(16, this.nomDoc); // nom_doc
		query.setParameter(17, REF_MIG_NAME); // mig_login_name
		query.setParameter(18, this.ime); // ime
		query.setParameter(19, this.prez); // prezime
		query.setParameter(20, this.fam); // familia
		query.executeUpdate();

		// адресите ще дойдат от regix
		return code;
	}

	/**  */
	private void saveVidSport(Query query, Integer coachesId, EntityManager em) {
		if (this.vidSport == null) {
			return;
		}

		Integer id = T0Start.nextVal("seq_mms_vid_sport", em);

		query.setParameter(1, id); // id
		query.setParameter(2, CODE_ZNACHENIE_JOURNAL_COACHES); // tip_object
		query.setParameter(3, coachesId); // id_object
		query.setParameter(4, T0Start.USER); // user_reg
		query.setParameter(5, this.regDate); // date_reg
		query.setParameter(6, this.vidSport); // vid_sport
		query.executeUpdate();
	}

	/** */
	private Integer saveVpisvane(Query query, Integer coachesId, EntityManager em) {
		Integer id = T0Start.nextVal("seq_mms_vpisvane", em);

		query.setParameter(1, id); // id - ИД на вписване
		query.setParameter(2, CODE_ZNACHENIE_JOURNAL_COACHES); // type_object - Тип на обекта
		query.setParameter(3, coachesId); // id_object - ИД на обекта

		// rn_doc_zaiavlenie - Рег. номер на Заявление за вписване
		query.setParameter(4, this.rnDocZaiavlenie);

		// date_doc_zaiavlenie - Дата на Заявление за вписване
		query.setParameter(5, new TypedParameterValue(StandardBasicTypes.TIMESTAMP, this.dateDocZaiavlenie));

		// status_result_zaiavlenie - Статус на заявление
		query.setParameter(6, DocuConstants.CODE_ZNACHENIE_STATUS_ZAIAVLENIE_VPISAN);

		// reason_result - Основания за статуса на заявление
		query.setParameter(7, new TypedParameterValue(StandardBasicTypes.INTEGER, null));

		// reason_result_text - Основания за статуса на заявление (текст)
		query.setParameter(8, this.vidSportText); // тука ще е за тези, в които не е определен както трябва

		// rn_doc_result - Рег. номер на Заповед за вписване / Заповед за отказ от вписване
		query.setParameter(9, this.rnDocResult);

		// date_doc_result - Дата на Заповед за вписване / Заповед за отказ от вписване
		query.setParameter(10, new TypedParameterValue(StandardBasicTypes.TIMESTAMP, this.dateDocResult));

		// rn_doc_licenz - Рег. номер на лиценз/удостоверение
		query.setParameter(11, this.regNom.toString());

		// date_doc_licenz - Дата на лиценз/удостоверение
		query.setParameter(12, new TypedParameterValue(StandardBasicTypes.TIMESTAMP, this.regDate));

		// status_vpisvane - Статус на вписване
		query.setParameter(13, DocuConstants.CODE_ZNACHENIE_STATUS_REG_VPISAN);

		// reason_vpisvane - Основание за статус на вписване
		query.setParameter(14, new TypedParameterValue(StandardBasicTypes.INTEGER, this.reasonVpisvane));

		// reason_vpisvane_text - Основание за статус на вписване (текст)
		query.setParameter(15, "");

		// rn_doc_vpisvane - Рег. номер на Заповед за прекратяване / Заповед за отнемане и Заповед за заличаване
		query.setParameter(16, "");

		// date_doc_vpisvane - Дата на Заповед за прекратяване / Заповед за отнемане и Заповед за заличаване
		query.setParameter(17, new TypedParameterValue(StandardBasicTypes.TIMESTAMP, null));

		// nachin_poluchavane - Начин на получаване на резултата
		query.setParameter(18, new TypedParameterValue(StandardBasicTypes.INTEGER, null));

		query.setParameter(19, ""); // addr_mail_poluchavane - Адрес/Email

		// date_status_zaiavlenie - Дата на статус на заявлението
		query.setParameter(20, new TypedParameterValue(StandardBasicTypes.TIMESTAMP, this.dateDocZaiavlenie));

		query.setParameter(21, new TypedParameterValue(StandardBasicTypes.INTEGER, this.vidSport)); // vid_sport - Вид спорт
		query.setParameter(22, new TypedParameterValue(StandardBasicTypes.INTEGER, this.dlajnost)); // dlajnost - Длъжност

		query.setParameter(23, this.dopInfo); // dop_info - Допълнителна информация
		query.setParameter(24, T0Start.USER); // user_reg
		query.setParameter(25, this.regDate); // date_reg

		// date_status_vpisvane - Дата на статус на вписването
		query.setParameter(26, new TypedParameterValue(StandardBasicTypes.TIMESTAMP, this.dateDocResult));

		query.executeUpdate();
		return id;
	}

	/** */
	private void saveVpisvaneDoc(Query query, Integer vpisvaneId, Integer docId, Integer coachesId, EntityManager em) {
		if (docId == null) {
			return;
		}
		Integer id = T0Start.nextVal("seq_mms_vpisvane_doc", em);

		query.setParameter(1, id); // id
		query.setParameter(2, vpisvaneId); // id_vpisvane
		query.setParameter(3, docId); // id_doc
		query.setParameter(4, T0Start.USER); // user_reg
		query.setParameter(5, this.regDate); // date_reg
		query.setParameter(6, coachesId); // id_object
		query.setParameter(7, CODE_ZNACHENIE_JOURNAL_COACHES); // type_object
		query.executeUpdate();
	}
}
