package com.ib.docu.search;

import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.docu.system.SystemData;
import com.ib.docu.system.UserData;
import com.ib.indexui.pagination.LazyDataModelSQL2Array;
import com.ib.indexui.system.Constants;
import com.ib.system.db.JPA;
import com.ib.system.utils.DateUtils;

/**
 * Тест клас за {@link DocSearch}
 *
 * @author belev
 */
public class TestDocSearch {

	private static final Logger LOGGER = LoggerFactory.getLogger(TestDocSearch.class);

	private static SystemData sd;

	/** @throws Exception */
	@BeforeClass
	public static void setUp() throws Exception {
		JPA.getUtil();
		sd = new SystemData();
		sd.getNameClassification(Constants.CODE_CLASSIF_ADMIN_STR, 1);
	}

	/** */
	@Test
	public void testBuildQueryComp() {
		try {
			DocSearch search = new DocSearch(1);

			search.setRegistraturaId(1);

			search.setRnDoc("Ж-19");

			search.setDocTypeArr(new Integer[] { 1, 2 });
			search.setDocVidList(Arrays.asList(2));

			search.setDocDateFrom(DateUtils.parseFull("01.04.2020 12:00:00"));
			search.setDocDateTo(DateUtils.parseFull("30.05.2020 00:00:00"));
			search.setOtnosno("док");

			search.setNotInDeloId(123);
			search.setNotInDocId(123);

			search.buildQueryComp(new UserData(-1, "", ""));

			LazyDataModelSQL2Array lazy = new LazyDataModelSQL2Array(search, "a0");
			List<Object[]> result = lazy.load(0, lazy.getRowCount(), null, null);

			for (Object[] row : result) {
				LOGGER.info(Arrays.toString(row));
			}
			LOGGER.info("{}", result.size());

		} catch (Exception e) {
			fail(e.getMessage());
			LOGGER.error(e.getMessage(), e);
		}
	}

	/** */
	@Test
	public void testBuildQueryDocList() {
		try {
			Date date = new Date();

			DocSearch searchFull = getDocSearch(date);

			searchFull.buildQueryDocList(new UserData(-1, "", ""), true);

			LazyDataModelSQL2Array lazyFull = new LazyDataModelSQL2Array(searchFull, "a0");
			List<Object[]> result = lazyFull.load(0, lazyFull.getRowCount(), null, null);

			DocSearch searchSQL = getDocSearch(date);
			searchSQL.buildQueryDocList(new UserData(-1, "", ""), true);
			LazyDataModelSQL2Array lazySQL = new LazyDataModelSQL2Array(searchSQL, "a0");

			Assert.assertArrayEquals(result.toArray(),lazySQL.load(0,lazySQL.getRowCount(),null, null).toArray());
			for (Object[] row : result) {
				LOGGER.info(Arrays.toString(row));
				LOGGER.info(sd.decodeItems(Constants.CODE_CLASSIF_ADMIN_STR, (String) row[9], 1, date));
			}
			LOGGER.info("{}", result.size());

		} catch (Exception e) {
			fail(e.getMessage());
			LOGGER.error(e.getMessage(), e);
		}
	}

	private DocSearch getDocSearch(Date date) {
		DocSearch search = new DocSearch(1);

//			search.setRegistraturaId(1);
//			search.setRegisterId(1);
//			search.setDocVidList(Arrays.asList(1));
//			search.setDocTypeArr(new Integer[] { 1, 2 });
//			search.setDocDateFrom(DateUtils.parse("08.01.2020"));
//			search.setDocDateTo(DateUtils.parse("23.01.2020"));
		search.setRnDoc("Зап");
		search.setRnDocEQ(false);

		search.setRegisterIdList(Arrays.asList(1, 2));
		search.setValidArr(new Integer[] { 1 });
		search.setUrgentArr(new Integer[] { 1 });
		search.setUserReg(-1);
		search.setGuid("{CDA79AF7-2BA9-43D9-9A2F-7559D78B36C4}");
		search.setDocName("test");
		search.setDocInfo("забележка");
		search.setOtnosno("нова");
		search.setTehNomer("tehen no");
		search.setTehNomerEQ(true);
		search.setWaitAnswerDateFrom(DateUtils.parse("01.04.2020"));
		search.setWaitAnswerDateTo(date);
		search.setReceiveDateFrom(DateUtils.parse("01.04.2020"));
		search.setReceiveDateTo(date);

//			search.setTaskReferentList(Arrays.asList(-1));
//			search.setTaskDateTo(date);

		search.setDocReferent(2);
		search.setDocReferentRoles(new Integer[] { 1, 2 });
		search.setNameCorresp("козл");
		return search;
	}
}