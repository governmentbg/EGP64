package com.ib.docu.transform;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.ib.system.db.JPA;
import com.ib.system.utils.DateUtils;
import com.ib.system.utils.SearchUtils;

/**
 * Стартира трансфера
 *
 * @author belev
 */
public class T0Start {

	private static final Pattern	PATTERN_BG_COMMAS_SPACES	= Pattern.compile("^[а-яА-Я-– ,\t]+$");
	private static final Pattern	PATTERN_BG_SPACES			= Pattern.compile("^[а-яА-Я-– ]+$");

	static final Date		TRANSFER_DATE	= DateUtils.parse("05.02.2024");
	static final Integer	REGISTATRURA	= 1;
	static final Integer	REGISTER		= 4;						// Заявления за вписване
	static final Integer	USER			= -1;
	static final Date		MIN_DATE		= DateUtils.systemMinDate();
	static final Date		MAX_DATE		= DateUtils.systemMaxDate();

	/** @param args */
	public static void main(String[] args) {
		T4RegTreners treners = new T4RegTreners();
		treners.clear(JPA.getUtil());

		T3RegSportObj sportObj = new T3RegSportObj();
		sportObj.clear(JPA.getUtil());

		T2RegSportKlTd sportKlTd = new T2RegSportKlTd();
		sportKlTd.clear(JPA.getUtil());

		T2RegSportKl sportKl = new T2RegSportKl();
		sportKl.clear(JPA.getUtil());

		T1RegSportFederationsNostd sportFederationsNostd = new T1RegSportFederationsNostd();
		sportFederationsNostd.clear(JPA.getUtil());

		T1RegSportFederations sportFederations = new T1RegSportFederations();
		sportFederations.clear(JPA.getUtil());

		System.exit(0); // не е ясно защо не терминира ако го няма
	}

	/**
	 * Подговя заявка за правене на SEQUENCE
	 *
	 * @param sequence
	 * @param startFrom
	 * @return
	 */
	static final String createSequenceQuery(String sequence, Number startFrom) {
		String sql = "CREATE SEQUENCE " + sequence + " INCREMENT BY 1 MINVALUE 0 MAXVALUE 2147483647 START WITH " + startFrom + " CACHE 1 NO CYCLE";
		return sql;
	}

	/**
	 * Дава мап за откриване на код по текст UPPER без интервали
	 *
	 * @param em
	 * @param codeClassif
	 * @return
	 */
	static final Map<String, Integer> findDecodeMap(EntityManager em, Integer codeClassif) {
		@SuppressWarnings("unchecked")
		Stream<Object[]> stream = em.createNativeQuery( //
			"select distinct code, tekst from v_system_classif where code_classif = ?1 and lang = 1 order by code") //
			.setParameter(1, codeClassif).getResultStream();

		Map<String, Integer> map = new HashMap<>();
		Iterator<Object[]> iter = stream.iterator();
		while (iter.hasNext()) {
			Object[] row = iter.next();

			String key = (String) row[1];
			int value = ((Number) row[0]).intValue();

			map.put(key.toUpperCase().replaceAll(" ", ""), value);
		}
		stream.close();

		return map;
	}

	/**
	 * Дава мап за откриване на код по текст UPPER без интервали и маха подадените от remove
	 *
	 * @param em
	 * @param codeClassif
	 * @return
	 */
	static final Map<String, Integer> findDecodeMap(EntityManager em, Integer codeClassif, String[] remove) {
		@SuppressWarnings("unchecked")
		Stream<Object[]> stream = em.createNativeQuery( //
			"select distinct code, tekst from v_system_classif where code_classif = ?1 and lang = 1 order by code") //
			.setParameter(1, codeClassif).getResultStream();

		Map<String, Integer> map = new HashMap<>();
		Iterator<Object[]> iter = stream.iterator();
		while (iter.hasNext()) {
			Object[] row = iter.next();

			String key = (String) row[1];
			int value = ((Number) row[0]).intValue();

			key = key.toUpperCase().replaceAll(" ", "");

			for (String r : remove) {
				key = key.replaceAll(r, "");
			}
			map.put(key, value);
		}
		stream.close();

		return map;
	}

	/**
	 * Дава мап за откриване на код по текст UPPER без интервали - за ЕКАТТЕ
	 *
	 * @param em
	 * @return
	 */
	static final Map<String, Integer> findEkatteMap(EntityManager em) {
		@SuppressWarnings("unchecked")
		Stream<Object[]> stream = em.createNativeQuery("select ekatte, tvm, ime, obstina_ime from ekatte_att").getResultStream();

		Map<String, Integer> map = new HashMap<>();
		Iterator<Object[]> iter = stream.iterator();
		while (iter.hasNext()) {
			Object[] row = iter.next();

			String key = "" + row[1] + row[2] + "_" + row[3];
			int value = ((Number) row[0]).intValue();

			map.put(key.toUpperCase().replaceAll(" ", ""), value);
		}
		stream.close();

		return map;
	}

	/**
	 * Дали стринга съдържа само български букви и интервал и запетая !!!!
	 *
	 * @param str
	 * @return
	 */
	static final boolean isBgCommasSpaces(String str) {
		if (str == null) {
			return false;
		}
		return PATTERN_BG_COMMAS_SPACES.matcher(str).matches();
	}

	/**
	 * Дали стринга съдържа само български букви и интервал !!!!
	 *
	 * @param str
	 * @return
	 */
	static final boolean isBgSpaces(String str) {
		if (str == null) {
			return false;
		}
		return PATTERN_BG_SPACES.matcher(str).matches();
	}

	/**
	 * Дава следващ ключ като се използва sequence
	 *
	 * @param seqName
	 * @return
	 */
	static final Integer nextVal(String seqName, EntityManager em) {
		Query query = em.createNativeQuery("SELECT nextval('" + seqName + "')");

		return SearchUtils.asInteger(query.getSingleResult());
	}
}
