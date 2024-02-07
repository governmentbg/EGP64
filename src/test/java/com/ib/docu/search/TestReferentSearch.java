package com.ib.docu.search;

import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.docu.system.SystemData;
import com.ib.indexui.pagination.LazyDataModelSQL2Array;

/**
 * Тест клас за {@link ReferentSearch}
 *
 * @author belev
 */
public class TestReferentSearch {

	private static SystemData sd;

	private static final Logger LOGGER = LoggerFactory.getLogger(TestReferentSearch.class);

	/***/
	@BeforeClass
	public static void setUp() {
		sd = new SystemData();
	}

	/** */
	@Test
	public void testBuildQuery() {
		try {

			ReferentSearch search = new ReferentSearch();
			search.setRefName("ИНДЕКС БЪЛГАРИЯ ООД");
			search.setExactly(true);
			search.setRefType(3);
			search.setCountry(37);
			search.setEikEgn("885544634");
			search.setEkatte(68134);

			search.calcEkatte(sd); // за да може при въведена област или община да се определи какво да се сложи в селекта
			search.buildQuery();

			LazyDataModelSQL2Array lazy = new LazyDataModelSQL2Array(search, "REF_NAME");
			List<Object[]> result = lazy.load(0, 10, null, null);

			for (Object[] objects : result) {
				for (int i = 0; i < objects.length; i++) {
					LOGGER.info(objects[i] + "\t");
				}
				LOGGER.info("");
			}

		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			fail(e.getMessage());
		}
	}

	/** */
	@Test
	public void testExtendedUserSearch() {
		try {
			ExtendedUserSearch search = new ExtendedUserSearch();

			search.setRegistratura(1);
			search.setBusinessRole(1);

			search.buildQueryUserList();

			LazyDataModelSQL2Array lazy = new LazyDataModelSQL2Array(search, "user_id");
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
}