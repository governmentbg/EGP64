package com.ib.docu.transform;

import static com.ib.docu.system.DocuConstants.CODE_CLASSIF_FUNC_CATEGORIA_SPORTEN_OBEKT;
import static com.ib.docu.system.DocuConstants.CODE_CLASSIF_VID_SPORTEN_OBEKT;
import static com.ib.docu.system.DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_OBJECTS;
import static com.ib.docu.system.DocuConstants.CODE_ZNACHENIE_STATUS_REG_VPISAN;
import static com.ib.docu.system.DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_SPORTEN_OBEKT;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

/**
 * ММС – миграция на спортни обекти
 *
 * @author belev
 */
public class T3RegSportObj {

	private static final Logger LOGGER = LoggerFactory.getLogger(T3RegSportObj.class);

	/**  */
	private static final String REF_MIG_NAME = "mms_sport_obekt_lice"; // mms_sport_obekt_lice

	/** @param args */
	public static void main(String[] args) {
		T3RegSportObj t = new T3RegSportObj();

		t.validate(JPA.getUtil());

//		t.clear(JPA.getUtil());

		t.transfer(JPA.getUtil());

//		t.regix(JPA.getUtil());

		System.exit(0); // не е ясно защо не терминира ако го няма
	}

	private SimpleDateFormat sdfY = new SimpleDateFormat("yyyy");

	private Map<String, Integer>	vidObektMap;
	private Map<String, Integer>	katObektMap;

	/** код на община + име на населено място -> код на населено място */
	private Map<String, Integer>		ekatteMap		= new HashMap<>();	// key=obst.ekatte_att.ime, key=att.ekatte
	/** тука ще са само уникалните имена, със всички кодове по екатте */
	private Map<String, List<Integer>>	ekatteMapAtt	= new HashMap<>();	// key=att.ime, key=list(att.ekatte)
	/** тука ще са само уникалните имена+твм, със всички кодове по екатте */
	private Map<String, List<Integer>>	ekatteMapTvmAtt	= new HashMap<>();	// key=att.tvm+att.ime, key=list(att.ekatte)

	// данни, които трябва по време на една итерация на процеса
	private Map<String, Integer> docMap = new HashMap<>(); // key=vh_nom, value=docId

	private Date	createdDate;
	private Integer	idSo;
	private String	nomSo;
	private String	name;
	private Integer	ekatte;
	private String	identKadast;
	private String	opisanie;
	private String	dopInfo;
	private Integer	vidCode;
	private Integer	katCode;

	private String	postCode;
	private String	sgrada;

	private String rnDoc;

	/**  */
	public T3RegSportObj() {
		this.vidObektMap = T0Start.findDecodeMap(JPA.getUtil().getEntityManager(), CODE_CLASSIF_VID_SPORTEN_OBEKT);
		this.katObektMap = T0Start.findDecodeMap(JPA.getUtil().getEntityManager(), CODE_CLASSIF_FUNC_CATEGORIA_SPORTEN_OBEKT);

		fillEkatteMaps();
	}

	/** @param jpa */
	public void clear(JPA jpa) {
		LOGGER.info("");
		LOGGER.info("Start clear register data");

		try {
			EntityManager em = jpa.getEntityManager();

			jpa.begin();
			int cnt = em.createNativeQuery("delete from adm_ref_addrs where code_ref in (" //
				+ "select code from adm_referents where mig_login_name = ?1 and code in (select id_object from mms_sport_obekt_lice))") //
				.setParameter(1, REF_MIG_NAME).executeUpdate();
			jpa.commit();
			LOGGER.info("   deleted " + cnt + " rows from table adm_ref_addrs");

			jpa.begin();
			cnt = em.createNativeQuery( //
				"delete from adm_referents where mig_login_name = ?1 and code in (select id_object from mms_sport_obekt_lice)") //
				.setParameter(1, REF_MIG_NAME).executeUpdate();
			jpa.commit();
			LOGGER.info("   deleted " + cnt + " rows from table adm_referents");

			jpa.begin();
			cnt = em.createNativeQuery("delete from mms_sport_obekt_lice").executeUpdate();
			jpa.commit();
			LOGGER.info("   deleted " + cnt + " rows from table mms_sport_obekt_lice");

			jpa.begin();
			cnt = em.createNativeQuery( // после по това число ще знам кои да изтрия
				"update doc set user_last_mod = -3000 where doc_id in (select id_doc from mms_vpisvane_doc where type_object = ?1)") //
				.setParameter(1, CODE_ZNACHENIE_JOURNAL_SPORTS_OBJECTS).executeUpdate();
			LOGGER.info("   update " + cnt + " rows from table doc.user_last_mod=-3000");

			cnt = em.createNativeQuery("delete from mms_vpisvane_doc where type_object = ?1") //
				.setParameter(1, CODE_ZNACHENIE_JOURNAL_SPORTS_OBJECTS).executeUpdate();
			LOGGER.info("   deleted " + cnt + " rows from table mms_vpisvane_doc");

			cnt = em.createNativeQuery("delete from doc_referents where doc_id in (select distinct doc_id from doc where user_last_mod = -3000)").executeUpdate();
			LOGGER.info("   deleted " + cnt + " rows from table doc_referents");

			cnt = em.createNativeQuery("delete from doc where user_last_mod = -3000").executeUpdate();
			LOGGER.info("   deleted " + cnt + " rows from table doc");
			jpa.commit();

			jpa.begin();
			cnt = em.createNativeQuery("delete from mms_vpisvane where type_object = ?1") //
				.setParameter(1, CODE_ZNACHENIE_JOURNAL_SPORTS_OBJECTS).executeUpdate();
			jpa.commit();
			LOGGER.info("   deleted " + cnt + " rows from table mms_vpisvane");

			jpa.begin();
			cnt = em.createNativeQuery("delete from mms_sport_obekt").executeUpdate();
			jpa.commit();
			LOGGER.info("   deleted " + cnt + " rows from table mms_sport_obekt");

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

			Query obektQuery = em.createNativeQuery("insert into mms_sport_obekt (" //
				+ " id, vid, funk_category, status, identification, sgrada, post_code, user_reg, date_reg, date_status" //
				+ ", name, opisanie, e_mail, tel, dop_info, country, reg_nomer, nas_mesto ) " //
				+ " values (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9, ?10, ?11, ?12, ?13, ?14, ?15, ?16, ?17, ?18)");

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

			Query checkObektQuery = em.createNativeQuery("select id from mms_sport_obekt where reg_nomer = ?1");

			Query checkVpisvaneQuery = em.createNativeQuery("select id from mms_vpisvane where type_object = ?1 and id_object = ?2") //
				.setParameter(1, CODE_ZNACHENIE_JOURNAL_SPORTS_OBJECTS);

			StringBuilder select = new StringBuilder();
			select.append(" select 0 obj, o.id, o.nom_so, o.tip, o.vid, o.kat, o.name_big "); // 0,1,2,3,4,5,6
			select.append(" , o.ident_kadast, o.opisanie, o.created_dat, o.prava "); // 7,8,9,10
			select.append(" , o.reg_dat, o.status, o.vid_sobst "); // 11,12,13
			select.append(" , a.post_code, a.nas_masto, a.address "); // 14,15,16
			select.append(" , o.statut, o.statut_info "); // 17,18
			select.append(" from reg_sport_obj o ");
			select.append(" left outer join reg_sport_obj_adres a on a.object_id = o.id ");
			select.append(" order by 1, 2 ");

			List<Object[]> rows = em.createNativeQuery(select.toString()).getResultList();

			Map<Integer, Integer> maxSport = new HashMap<>(); // за броячие
			Map<Integer, Integer> maxTur = new HashMap<>(); // за броячие

			int insertCnt = 0;
			jpa.begin();
			for (Object[] row : rows) {

				setupData(row); // за да се намерят данните !!! МНОГО ВАЖНО !!!

				if (this.idSo == null) {
					continue;
				}

				String checkNom;
				if (this.nomSo != null) {
					checkNom = this.nomSo; // по това ще се гледа за запис на нов обект

					try { // има номер и трябва да се вземе макс за брояча
						String[] split = this.nomSo.split("-");

						int key = Integer.parseInt(split[1]);
						int current = Integer.parseInt(split[2]);

						Map<Integer, Integer> max = maxSport;
						if ("Обект за социален туризъм".equalsIgnoreCase((String) row[3])) {
							max = maxTur;
						}
						Integer maxVal = max.get(key);

						if (maxVal == null || maxVal.intValue() < current) {
							maxVal = current;
						}
						max.put(key, maxVal);

					} catch (Exception e) {
						LOGGER.error("  ! nom_so=" + this.nomSo + " ! ERROR parseInt ", e);
					}

				} else {
					checkNom = "123456789-987654321"; // за да не открие нищо и винаги ще е нов
				}

				// Проверява в базата, има ли регистриран спортен обект с този номер
				List<Object> obekt = checkObektQuery.setParameter(1, checkNom).getResultList();

				if (obekt.isEmpty()) { // няма и се изпълняват т. 4.2, 4.3, 4.4, 4.5, 4.6

					Integer obektId = saveObekt(obektQuery, em); // mms_sport_obekt (4.2)

					Integer vpisvaneId = saveVpisvane(vpisvaneQuery, obektId, em); // mms_vpisvane (4.4)

					Integer docId = saveDoc(docQuery, null, em); // doc (4.5)

					saveVpisvaneDoc(vpisvaneDocQuery, vpisvaneId, docId, obektId, em); // mms_vpisvane_doc (4.6)

				} else { // Проверява се има ли вписване за този обект

					Integer obektId = SearchUtils.asInteger(obekt.get(0));

					List<Object> vpisvane = checkVpisvaneQuery.setParameter(2, obektId).getResultList();
					if (vpisvane.isEmpty()) { // няма и се изпълняват т. 4.4, 4.5, 4.6

						Integer vpisvaneId = saveVpisvane(vpisvaneQuery, obektId, em); // mms_vpisvane (4.4)

						Integer docId = saveDoc(docQuery, null, em); // doc (4.5)

						saveVpisvaneDoc(vpisvaneDocQuery, vpisvaneId, docId, obektId, em); // mms_vpisvane_doc (4.6)
					}
				}

				insertCnt++;
				if (insertCnt % 100 == 0) {
					LOGGER.info("  " + insertCnt);
				}
			}

			// оправям брояча
			for (Entry<Integer, Integer> entry : maxSport.entrySet()) { // оправям броячите
				String key = "obekt." + entry.getKey();

				int cnt = em.createNativeQuery("update sid set next_val = ?1 where object = ?2") //
					.setParameter(1, entry.getValue().intValue() + 1).setParameter(2, key).executeUpdate();
				if (cnt == 0) {
					em.createNativeQuery("insert into sid (object, next_val) values (?1, ?2)") //
						.setParameter(1, key).setParameter(2, entry.getValue().intValue() + 1).executeUpdate();
				}
			}

			// оправям брояча
			for (Entry<Integer, Integer> entry : maxTur.entrySet()) { // оправям броячите
				String key = "obekt.t." + entry.getKey();

				int cnt = em.createNativeQuery("update sid set next_val = ?1 where object = ?2") //
					.setParameter(1, entry.getValue().intValue() + 1).setParameter(2, key).executeUpdate();
				if (cnt == 0) {
					em.createNativeQuery("insert into sid (object, next_val) values (?1, ?2)") //
						.setParameter(1, key).setParameter(2, entry.getValue().intValue() + 1).executeUpdate();
				}
			}

			jpa.commit();

			LOGGER.info("  " + rows.size());
			LOGGER.info("Transfer register data - COMPLETE");

		} catch (Exception e) {
			jpa.rollback();

			LOGGER.error("  ! ID=" + this.idSo + " ! System ERROR transfer register -> " + e.getMessage(), e);

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
			select.append(" select 0 obj, o.id, o.nom_so, o.tip, o.vid, o.kat, o.name_big "); // 0,1,2,3,4,5,6
			select.append(" , a.post_code, a.nas_masto, a.address ");// 7,8,9
			select.append(" from reg_sport_obj o ");
			select.append(" left outer join reg_sport_obj_adres a on a.object_id = o.id ");
//			select.append(" where o.tip <> 'Обект за социален туризъм' "); // !!! Обект за социален туризъм !!!
			select.append(" order by 1, 2 ");

			@SuppressWarnings("unchecked")
			List<Object[]> rows = em.createNativeQuery(select.toString()).getResultList();

			Set<String> unique = new HashSet<>();
			int errcnt = 0;

			for (Object[] row : rows) {
				String tablename = "reg_sport_obj";

				StringBuilder errors = new StringBuilder();
				String prefix = "\n\t";

				String vid = SearchUtils.trimToNULL_Upper((String) row[4]); // vid
				if (vid != null && !this.vidObektMap.containsKey(vid.replaceAll(" ", ""))) {
					errors.append(prefix + "UNKNOWN vid=" + row[4] + "; classif=" + CODE_CLASSIF_VID_SPORTEN_OBEKT);
				}

				String kat = SearchUtils.trimToNULL_Upper((String) row[5]); // kat
				if (kat != null && !this.katObektMap.containsKey(kat.replaceAll(" ", ""))) {
					errors.append(prefix + "UNKNOWN kat=" + row[5] + "; classif=" + CODE_CLASSIF_FUNC_CATEGORIA_SPORTEN_OBEKT);
				}

				String name1 = SearchUtils.trimToNULL((String) row[6]); // name
				if (name1 == null) {
					errors.append(prefix + "NULL name");
				}

				String nomSo1 = SearchUtils.trimToNULL((String) row[2]); // nom_so
				if (nomSo1 != null) { // има и без номера
					if (unique.contains(nomSo1)) {
						errors.append(prefix + "DUPLICATE nom_so=" + row[2]);
					} else {
						unique.add(nomSo1);
					}
				}

				Integer ekatte1 = null;

				String city = SearchUtils.trimToNULL_Upper((String) row[8]); // nas_masto
				if (city != null) {
					if (city.startsWith("СЕЛО ")) {
						city = "С." + city.substring(5, city.length());
					}
					if (city.startsWith("ГРАД ")) {
						city = "ГР." + city.substring(5, city.length());
					}
					city = city.replaceAll(" ", "");

					List<Integer> tempTvm = this.ekatteMapTvmAtt.get(city);
					if (tempTvm != null && tempTvm.size() == 1) {
						ekatte1 = tempTvm.get(0); // само един и сме ОК
					}

					if (ekatte1 == null) {
						if (city.indexOf("ГР.") != -1) {
							city = city.replace("ГР.", "");
						}
						if (city.indexOf("С.") != -1) {
							city = city.replace("С.", "");
						}

						List<Integer> temp = this.ekatteMapAtt.get(city);
						if (temp != null && temp.size() == 1) {
							ekatte1 = temp.get(0); // само един и сме ОК
						}
					}
				}

				if (ekatte1 == null && nomSo1 != null) { // тука по номера трябва все пак да излезе нещо
					int obstEkatte = 0;
					try {
						obstEkatte = Integer.valueOf(nomSo1.split("-")[1]);
					} catch (Exception e) { // няма какво да се направи
					}

					if (city != null) {
						ekatte1 = this.ekatteMap.get(obstEkatte + "_" + city);
					}

					if (ekatte1 == null) {
						ekatte1 = obstEkatte;
					}
				}

				if (ekatte1 == null) {
//					errors.append(prefix + "UNKNOWN city=" + row[8]);
				}

				if (errors.length() > 0) {
					errcnt++;
					System.out.println(tablename + ".id=" + SearchUtils.asInteger(row[1]) + errors);
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

//		mms_sport_obekt
//		mms_sport_obekt_lice
//		adm_ref_addrs
//		adm_referents
//		mms_vpisvane_doc
//		doc
//		mms_vpisvane

		StringBuilder select = new StringBuilder();
		select.append(" select 'seq_mms_sport_obekt' seq_name, max (id) max_id from mms_sport_obekt ");
		select.append(" union all ");
		select.append(" select 'seq_mms_sport_obekt_lice' seq_name, max (id) max_id from mms_sport_obekt_lice ");
		select.append(" union all ");
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
		this.createdDate = (Date) row[9]; // created_dat
		if (this.createdDate == null) {
			this.createdDate = T0Start.TRANSFER_DATE;
		}

		this.idSo = ((Number) row[1]).intValue(); // id
		this.nomSo = SearchUtils.trimToNULL((String) row[2]); // nom_so

		if (this.nomSo != null) {
			this.rnDoc = this.nomSo;
		} else {
			this.rnDoc = String.valueOf(this.idSo);
		}

		calcEkatte(row);

		this.postCode = SearchUtils.trimToNULL((String) row[14]); // post_code

		if (this.ekatte != null) { // населеното място е ясно и ни трябва само адреса
			this.sgrada = SearchUtils.trimToNULL((String) row[16]); // address

		} else { // ще се сглоби от техните данни и няма да имаме код на населено място
			this.sgrada = SearchUtils.trimToNULL((String) row[15]); // nas_masto

			String t = SearchUtils.trimToNULL((String) row[16]); // address
			if (t != null) {
				if (this.sgrada == null) {
					this.sgrada = t;
				} else {
					if (t.indexOf(this.sgrada) != -1) {
						// няма какво да се добавя защото ще се дублира
					} else {
						this.sgrada = this.sgrada + ", " + t;
					}
				}
			}
		}

		this.name = SearchUtils.trimToNULL((String) row[6]); // name
		if (this.name == null) {
			this.name = this.rnDoc;
		}

		this.identKadast = SearchUtils.trimToNULL((String) row[7]); // ident_kadast
		this.opisanie = SearchUtils.trimToNULL((String) row[8]); // opisanie

		this.vidCode = null;
		String vid = SearchUtils.trimToNULL_Upper((String) row[4]); // vid
		if (vid != null) {
			this.vidCode = this.vidObektMap.get(vid.replaceAll(" ", ""));
		} else {
			this.vidCode = 28; // константа !!!
		}
		this.katCode = null;
		String kat = SearchUtils.trimToNULL_Upper((String) row[5]); // kat
		if (kat != null) {
			this.katCode = this.katObektMap.get(kat.replaceAll(" ", ""));
		}

		StringBuilder dopInfoSb = new StringBuilder();
		String prava = SearchUtils.trimToNULL((String) row[10]); // prava
		if (prava != null) {
			dopInfoSb.append("Предоставени права: " + prava);
		}
		String vidSobst = SearchUtils.trimToNULL((String) row[13]); // vid_sobst
		if (vidSobst != null) {
			if (dopInfoSb.length() > 0) {
				dopInfoSb.append("\r\n");
			}
			dopInfoSb.append("Вид на собствеността: " + vidSobst);
		}
		String statut = SearchUtils.trimToNULL((String) row[17]); // statut
		if (statut != null) {
			if (dopInfoSb.length() > 0) {
				dopInfoSb.append("\r\n");
			}
			dopInfoSb.append("Статут: " + statut);
		}
		String statutInfo = SearchUtils.trimToNULL((String) row[18]); // statut_info
		if (statutInfo != null) {
			if (dopInfoSb.length() > 0) {
				dopInfoSb.append("\r\n");
			}
			dopInfoSb.append("Информация: " + statutInfo);
		}

		this.dopInfo = dopInfoSb.toString();
	}

	private void calcEkatte(Object[] row) {
		this.ekatte = null;

		String city = SearchUtils.trimToNULL_Upper((String) row[15]); // nas_masto
		if (city != null) {
			if (city.startsWith("СЕЛО ")) {
				city = "С." + city.substring(5, city.length());
			}
			if (city.startsWith("ГРАД ")) {
				city = "ГР." + city.substring(5, city.length());
			}
			city = city.replaceAll(" ", "");

			List<Integer> tempTvm = this.ekatteMapTvmAtt.get(city);
			if (tempTvm != null && tempTvm.size() == 1) {
				this.ekatte = tempTvm.get(0); // само един и сме ОК
			}

			if (this.ekatte == null) {
				if (city.indexOf("ГР.") != -1) {
					city = city.replace("ГР.", "");
				}
				if (city.indexOf("С.") != -1) {
					city = city.replace("С.", "");
				}

				List<Integer> temp = this.ekatteMapAtt.get(city);
				if (temp != null && temp.size() == 1) {
					this.ekatte = temp.get(0); // само един и сме ОК
				}
			}
		}

		if (this.ekatte == null && this.nomSo != null) { // тука по номера трябва все пак да излезе нещо
			int obstEkatte = 0;
			try {
				obstEkatte = Integer.valueOf(this.nomSo.split("-")[1]);
			} catch (Exception e) { // няма какво да се направи
			}

			if (city != null) {
				this.ekatte = this.ekatteMap.get(obstEkatte + "_" + city);
			}

			if (this.ekatte == null) {
				this.ekatte = obstEkatte;
			}
		}
	}

	private void fillEkatteMaps() {
		@SuppressWarnings("unchecked")
		Stream<Object[]> stream = JPA.getUtil().getEntityManager().createNativeQuery( //
			"select obst.ekatte obst_ekatte, att.ime att_ime, att.ekatte, att.tvm" //
				+ " from ekatte_att att inner join ekatte_obstini obst on obst.obstina = att.obstina")
			.getResultStream();

		this.ekatteMap.clear();
		this.ekatteMapAtt.clear();
		this.ekatteMapTvmAtt.clear();

		Iterator<Object[]> iter = stream.iterator();
		while (iter.hasNext()) {
			Object[] row = iter.next();

			int value = ((Number) row[2]).intValue();

			String attIme = ((String) row[1]).toUpperCase().replaceAll(" ", "");
			String tvmAttIme = ((String) row[3]).toUpperCase() + attIme;

			String key = ((Number) row[0]).intValue() + "_" + attIme;

			this.ekatteMap.put(key, value);

			List<Integer> temp = this.ekatteMapAtt.get(attIme);
			if (temp == null) {
				temp = new ArrayList<>();
				this.ekatteMapAtt.put(attIme, temp);
			}
			temp.add(value);

			List<Integer> tempTvm = this.ekatteMapTvmAtt.get(tvmAttIme);
			if (tempTvm == null) {
				tempTvm = new ArrayList<>();
				this.ekatteMapTvmAtt.put(tvmAttIme, tempTvm);
			}
			tempTvm.add(value);
		}
		stream.close();
	}

	/** */
	private Integer saveDoc(Query query, Integer codeRef, EntityManager em) {
		if (this.rnDoc == null || this.createdDate == null) {
			return null; // няма как да се запише
		}

		String key = this.rnDoc + "/" + this.sdfY.format(this.createdDate);
		if (this.docMap.containsKey(key)) {
			return this.docMap.get(key); // не се прави нов документ, а се дава по този номер вече записаният
		}

		Integer docId = T0Start.nextVal("seq_doc", em);

		query.setParameter(1, docId); // doc_id
		query.setParameter(2, T0Start.REGISTATRURA); // registratura_id
		query.setParameter(3, T0Start.REGISTER); // register_id
		query.setParameter(4, new TypedParameterValue(StandardBasicTypes.INTEGER, codeRef)); // code_ref_corresp
		query.setParameter(5, this.rnDoc); // rn_doc
		query.setParameter(6, ""); // rn_prefix
		query.setParameter(7, new TypedParameterValue(StandardBasicTypes.INTEGER, null)); // rn_pored
		query.setParameter(8, "{" + UUID.randomUUID().toString().toUpperCase() + "}"); // guid
		query.setParameter(9, DocuConstants.CODE_ZNACHENIE_DOC_TYPE_IN); // doc_type
		query.setParameter(10, CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_SPORTEN_OBEKT); // doc_vid
		query.setParameter(11, this.createdDate); // doc_date
		query.setParameter(12, "Заявление за ново вписване на " + this.name); // otnosno
		query.setParameter(13, SysConstants.CODE_ZNACHENIE_DA); // valid
		query.setParameter(14, this.createdDate); // valid_date
		query.setParameter(15, SysConstants.CODE_ZNACHENIE_DA); // processed
		query.setParameter(16, SysConstants.CODE_ZNACHENIE_DA); // free_access
		query.setParameter(17, 0); // count_files
		query.setParameter(18, T0Start.USER); // user_reg
		query.setParameter(19, this.createdDate); // date_reg
		query.setParameter(20, SysConstants.CODE_ZNACHENIE_DA); // competence
		query.executeUpdate();

		this.docMap.put(key, docId);

		return docId;
	}

	/**  */
	private Integer saveObekt(Query query, EntityManager em) {
		Integer id = T0Start.nextVal("seq_mms_sport_obekt", em);

		Integer statusObekt = DocuConstants.CODE_ZNACHENIE_STATUS_OBEKT_VPISAN;
		Date dateStatus = this.createdDate;
		if (this.nomSo == null) {
			statusObekt = null;
			dateStatus = null;
		}

		query.setParameter(1, id); // id
		query.setParameter(2, new TypedParameterValue(StandardBasicTypes.INTEGER, this.vidCode)); // vid
		query.setParameter(3, new TypedParameterValue(StandardBasicTypes.INTEGER, this.katCode)); // funk_category
		query.setParameter(4, new TypedParameterValue(StandardBasicTypes.INTEGER, statusObekt)); // status
		query.setParameter(5, this.identKadast); // identification
		query.setParameter(6, this.sgrada); // sgrada
		query.setParameter(7, this.postCode); // post_code
		query.setParameter(8, T0Start.USER); // user_reg
		query.setParameter(9, this.createdDate); // date_reg
		query.setParameter(10, new TypedParameterValue(StandardBasicTypes.TIMESTAMP, dateStatus)); // date_status
		query.setParameter(11, this.name); // name
		query.setParameter(12, this.opisanie); // opisanie
		query.setParameter(13, ""); // e_mail от къде се взима
		query.setParameter(14, ""); // tel от къде се взима
		query.setParameter(15, this.dopInfo); // dop_info
		query.setParameter(16, 37); // country
		query.setParameter(17, this.nomSo); // reg_nomer
		query.setParameter(18, new TypedParameterValue(StandardBasicTypes.INTEGER, this.ekatte)); // nas_mesto
		query.executeUpdate();

		return id;
	}

	/** */
	private Integer saveVpisvane(Query query, Integer obektId, EntityManager em) {
		Integer id = T0Start.nextVal("seq_mms_vpisvane", em);

		int statusZaiavlenie = DocuConstants.CODE_ZNACHENIE_STATUS_ZAIAVLENIE_VPISAN;
		Integer statusVpisvane = CODE_ZNACHENIE_STATUS_REG_VPISAN;
		
		if (this.nomSo == null) { // щом обекта няма номер значи е така става
			statusZaiavlenie = DocuConstants.CODE_ZNACHENIE_STATUS_ZAIAVLENIE_V_RAZGLEJDANE;
			statusVpisvane = null;
		}

		query.setParameter(1, id); // id - ИД на вписване
		query.setParameter(2, CODE_ZNACHENIE_JOURNAL_SPORTS_OBJECTS); // type_object - Тип на обекта
		query.setParameter(3, obektId); // id_object - ИД на обекта

		// rn_doc_zaiavlenie - Рег. номер на Заявление за вписване
		query.setParameter(4, this.rnDoc);

		// date_doc_zaiavlenie - Дата на Заявление за вписване
		query.setParameter(5, this.createdDate);

		// status_result_zaiavlenie - Статус на заявление
		query.setParameter(6, statusZaiavlenie);

		// reason_result - Основания за статуса на заявление
		query.setParameter(7, new TypedParameterValue(StandardBasicTypes.INTEGER, null));

		// reason_result_text - Основания за статуса на заявление (текст)
		query.setParameter(8, "");

		// rn_doc_result - Рег. номер на Заповед за вписване / Заповед за отказ от вписване
		query.setParameter(9, this.nomSo); // ако няма номер няма да има и заповед за отказ

		// date_doc_result - Дата на Заповед за вписване / Заповед за отказ от вписване
		Date dateDocResult = this.nomSo != null ? this.createdDate : null;
		query.setParameter(10, new TypedParameterValue(StandardBasicTypes.TIMESTAMP, dateDocResult));

		// rn_doc_licenz - Рег. номер на лиценз/удостоверение
		query.setParameter(11, "");

		// date_doc_licenz - Дата на лиценз/удостоверение
		query.setParameter(12, new TypedParameterValue(StandardBasicTypes.TIMESTAMP, null));

		// status_vpisvane - Статус на вписване
		query.setParameter(13, new TypedParameterValue(StandardBasicTypes.INTEGER, statusVpisvane));

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
		query.setParameter(20, this.createdDate);

		query.setParameter(21, new TypedParameterValue(StandardBasicTypes.INTEGER, null)); // vid_sport - Вид спорт
		query.setParameter(22, new TypedParameterValue(StandardBasicTypes.INTEGER, null)); // dlajnost - Длъжност

		query.setParameter(23, ""); // dop_info - Допълнителна информация
		query.setParameter(24, T0Start.USER); // user_reg
		query.setParameter(25, this.createdDate); // date_reg

		// date_status_vpisvane - Дата на статус на вписването
		Date dateStatusVpisvane = statusVpisvane != null ? this.createdDate : null;
		query.setParameter(26, new TypedParameterValue(StandardBasicTypes.TIMESTAMP, dateStatusVpisvane));

		query.executeUpdate();
		return id;
	}

	/** */
	private void saveVpisvaneDoc(Query query, Integer vpisvaneId, Integer docId, Integer obektId, EntityManager em) {
		if (docId == null) {
			return;
		}
		Integer id = T0Start.nextVal("seq_mms_vpisvane_doc", em);

		query.setParameter(1, id); // id
		query.setParameter(2, vpisvaneId); // id_vpisvane
		query.setParameter(3, docId); // id_doc
		query.setParameter(4, T0Start.USER); // user_reg
		query.setParameter(5, this.createdDate); // date_reg
		query.setParameter(6, obektId); // id_object
		query.setParameter(7, CODE_ZNACHENIE_JOURNAL_SPORTS_OBJECTS); // type_object
		query.executeUpdate();
	}
}
