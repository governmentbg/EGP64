package com.ib.docu.system;

import static com.ib.indexui.system.Constants.CODE_CLASSIF_ADMIN_STR;
import static com.ib.indexui.system.Constants.CODE_CLASSIF_REFERENTS;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.persistence.Query;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.indexui.system.Constants;
import com.ib.system.SysConstants;
import com.ib.system.db.JPA;
import com.ib.system.db.dto.SystemClassif;
import com.ib.system.exceptions.BaseException;
import com.ib.system.exceptions.DbErrorException;
import com.ib.system.utils.DateUtils;
import com.ib.system.utils.SearchUtils;

/**  */
public class TestSystemData {

	private static final Logger LOGGER = LoggerFactory.getLogger(TestSystemData.class);

	private static SystemData sd;

	/** @throws Exception */
	@BeforeClass
	public static void setUp() throws Exception {
		sd = new SystemData();
		sd.getSysClassification(DocuConstants.CODE_CLASSIF_REGISTRI, null, 1);
	}

	private int nextVal(String seqName) throws DbErrorException {
		try {
			Query query = JPA.getUtil().getEntityManager().createNativeQuery("SELECT nextval('" + seqName + "') ");

			return SearchUtils.asInteger(query.getSingleResult());
		} catch (Exception e) {
			throw new DbErrorException(e);
		}
	}

//	@Test
	public void testSaveEgov() {
		List<String[]> result = new ArrayList<>();

		try (InputStream resource = new FileInputStream("D:\\Projects\\MMS\\нови_pdf-и\\opis.txt"); //
			InputStreamReader isr = new InputStreamReader(resource, StandardCharsets.UTF_8); //
			BufferedReader reader = new BufferedReader(isr)) {

			String line = null;
			while ((line = reader.readLine()) != null) {
				result.add(line.split("\t"));
			}
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
			
		try {
			for (String[] row : result) {
			
			JPA.getUtil().begin();

			int msgId = nextVal("SEQ_EGOV_MESSAGES");
			int filesId = nextVal("SEQ_EGOV_MESSAGES_FILES");
			int correspId = nextVal("SEQ_EGOV_MESSAGES_CORESP");
			
			String filename = row[0]; // "1201003ZVLNv01-Заявление за заличаване на спортен обект_sign.pdf";
			String msgDocRn = row[1]; // "15-40-03";
			Date msgDocRnDat = DateUtils.parse(row[2]); // ("03.05.2023");
			String docVid = row[3]; // "38";
			
			Path path = Paths.get("D:\\Projects\\MMS\\нови_pdf-и\\" + filename);
			byte[] blob = Files.readAllBytes(path);
			
			Query msg = JPA.getUtil().getEntityManager().createNativeQuery("INSERT INTO EGOV_MESSAGES"
					+ "(ID, MSG_GUID, SENDER_GUID, SENDER_NAME, RECIPIENT_GUID, RECIPIENT_NAME"
					+ ", MSG_TYPE, MSG_DAT, MSG_STATUS, MSG_STATUS_DAT, MSG_INOUT, MSG_VERSION, MSG_RN, MSG_RN_DAT"
					+ ", DOC_GUID, DOC_DAT, DOC_RN, DOC_VID, DOC_SUBJECT, DOC_COMMENT, COMM_STATUS"
					+ ", SENDER_EIK, RECIPIENT_EIK, SOURCE)"
					+ " VALUES(?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9, ?10, ?11, ?12, ?13, ?14, ?15, ?16, ?17, ?18, ?19, ?20, ?21, ?22, ?23, ?24)");
			msg.setParameter(1, msgId); // ID
			msg.setParameter(2, "{" + UUID.randomUUID().toString().toUpperCase() + "}"); // MSG_GUID
			msg.setParameter(3, 1554); // SENDER_GUID
			msg.setParameter(4, "Камелия Томова"); // SENDER_NAME
			msg.setParameter(5, "{77EE2AC9-4719-4063-9A52-781B78032EFA}"); // RECIPIENT_GUID
			msg.setParameter(6, "Министерство на младежта и спорта"); // RECIPIENT_NAME
			msg.setParameter(7, "MSG_DocumentRegistrationRequest"); // MSG_TYPE
			msg.setParameter(8, new Date()); // MSG_DAT
			msg.setParameter(9, "DS_WAIT_REGISTRATION"); // MSG_STATUS
			msg.setParameter(10, new Date()); // MSG_STATUS_DAT
			msg.setParameter(11, 1); // MSG_INOUT
			msg.setParameter(12, "1.0"); // MSG_VERSION
			msg.setParameter(13, msgDocRn); // MSG_RN
			msg.setParameter(14, msgDocRnDat); // MSG_RN_DAT
			msg.setParameter(15, "{" + UUID.randomUUID().toString().toUpperCase() + "}"); // DOC_GUID
			msg.setParameter(16, msgDocRnDat); // DOC_DAT
			msg.setParameter(17, msgDocRn); // DOC_RN
			msg.setParameter(18, docVid); // DOC_VID
			msg.setParameter(19, "Тестов - НОВ - 2"); // DOC_SUBJECT
			msg.setParameter(20, ""); // DOC_COMMENT
			msg.setParameter(21, 3); // COMM_STATUS
			msg.setParameter(22, ""); // SENDER_EIK
			msg.setParameter(23, "175745920"); // RECIPIENT_EIK
			msg.setParameter(24, "S_ARCHIMED"); // SOURCE
			msg.executeUpdate();
			
			
			Query files = JPA.getUtil().getEntityManager().createNativeQuery(
					"INSERT INTO EGOV_MESSAGES_FILES(ID, ID_MESSAGE, FILENAME, MIME, BLOBCONTENT) VALUES(?1, ?2, ?3, ?4, ?5)");
			files.setParameter(1, filesId); // ID
			files.setParameter(2, msgId); // ID_MESSAGE
			files.setParameter(3, filename); // FILENAME
			files.setParameter(4, "application/pdf"); // MIME
			files.setParameter(5, blob); // BLOBCONTENT
			files.executeUpdate();
			
			
			Query corresp = JPA.getUtil().getEntityManager().createNativeQuery(
					"INSERT INTO EGOV_MESSAGES_CORESP(ID, ID_MESSAGE, IME, EGN, BULSTAT, CITY, ADRES, PK, EMAIL, DOP_INFO)"
					+ " VALUES(?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9, ?10)");
			corresp.setParameter(1, correspId); // ID
			corresp.setParameter(2, msgId); // ID_MESSAGE
			corresp.setParameter(3, "Index Ltd - E_MAIL: nikolaikosev@mail.bg\r\nЕИК: 204167785"); // IME
			corresp.setParameter(4, null); // EGN
			corresp.setParameter(5, null); // BULSTAT
			corresp.setParameter(6, "София"); // CITY
			corresp.setParameter(7, null); // ADRES
			corresp.setParameter(8, null); // PK
			corresp.setParameter(9, "nikolaikosev@mail.bg"); // EMAIL
			corresp.setParameter(10, "Code: 11-04\r\nCorrespondent: 204167785"); // DOP_INFO
			
			JPA.getUtil().commit();
			
			}

		} catch (Exception e) {
			JPA.getUtil().rollback();
			
			e.printStackTrace();
		} finally {
			JPA.getUtil().closeConnection();
		}
	}
	
	/** */
//	@Test
	public void testClassifAdmStruct() { // CODE_CLASSIF_ADMIN_STR само административната структура ЙЕРАРХИЧНА
		try {
			List<SystemClassif> list = sd.queryClassification(CODE_CLASSIF_ADMIN_STR, "", new Date(), 1);
			LOGGER.info("АДМ.СТРУКТУРА");
			for (SystemClassif item : list) {
				LOGGER.info("\t" + item.getTekst() + " --> " + item.getDopInfo());
			}

			LOGGER.info("РАЗКОДИРАНЕ");
			LOGGER.info("\t" + sd.decodeItem(CODE_CLASSIF_ADMIN_STR, 6, 1, new Date()));

			LOGGER.info("СПЕЦИФИКИ");
			SystemClassif item = sd.decodeItemLite(CODE_CLASSIF_ADMIN_STR, 6, 1, new Date(), true);

			LOGGER.info("\tREF_TYPE=" + item.getSpecifics()[DocuClassifAdapter.ADM_STRUCT_INDEX_REF_TYPE]);
			LOGGER.info("\tREGISTRATURA=" + item.getSpecifics()[DocuClassifAdapter.ADM_STRUCT_INDEX_REGISTRATURA]);
			LOGGER.info("\tCONTACT_EMAIL=" + item.getSpecifics()[DocuClassifAdapter.ADM_STRUCT_INDEX_CONTACT_EMAIL]);

		} catch (Exception e) {
			fail(e.getMessage());
			LOGGER.error(e.getMessage(), e);
		}
	}

	/** */
	@Test
	public void testClassifEmplReplaces() {
		try {
//			List<SystemClassif> list = sd.getSysClassification(Constants.CODE_CLASSIF_EMPL_REPLACES, new Date(), 1);
//			for (SystemClassif item : list) {
//				LOGGER.info("\t" + item.getTekst());
//			}

			SystemClassif zamestnik = sd.decodeItemLite(Constants.CODE_CLASSIF_EMPL_REPLACES, 6, 1, new Date(), false);

			if (zamestnik != null) { // има заместник към подадената дата

				LOGGER.info(zamestnik.getTekst());
				LOGGER.info("код на заместник={}", zamestnik.getCodeExt());
			}

		} catch (Exception e) {
			fail(e.getMessage());
			LOGGER.error(e.getMessage(), e);
		}
	}

	/** */
//	@Test
	public void testClassifGroupEmployees() {
		// Това е за групи служители. По подбен начин може да се вземе всичко необходимо и за групи кореспонденти

		try {
			Date date = new Date();

			List<SystemClassif> allGroups = sd.getSysClassification(DocuConstants.CODE_CLASSIF_GROUP_EMPL, date, SysConstants.CODE_DEFAULT_LANG);

			LOGGER.info("---------------------------------------------------");
			LOGGER.info("Всички групи служители без значение регистратурата");
			for (SystemClassif group : allGroups) {
				LOGGER.info("\t" + group.getCode() + "-" + group.getTekst());
			}
			Integer registratura = 1; // в класификациите за групи служители има специфика по регистратура!
			Map<Integer, Object> specRegistratura = Collections.singletonMap(DocuClassifAdapter.REG_GROUP_INDEX_REGISTRATURA, registratura);
			List<SystemClassif> groupsByRegistratura = sd.queryClassification(DocuConstants.CODE_CLASSIF_GROUP_EMPL, null, date, SysConstants.CODE_DEFAULT_LANG, specRegistratura);

			LOGGER.info("---------------------------------------------------");
			LOGGER.info("Всички групи служители за регистратура 1");
			for (SystemClassif group : groupsByRegistratura) {
				LOGGER.info("\t" + group.getCode() + "-" + group.getTekst());
			}

			if (!groupsByRegistratura.isEmpty()) {
				Integer selectedGroup = groupsByRegistratura.get(0).getCode();

				// в групата има специфика на всички служители (кодовете с разделител ',')
				String emplCodes = (String) sd.getItemSpecific(DocuConstants.CODE_CLASSIF_GROUP_EMPL, selectedGroup, SysConstants.CODE_DEFAULT_LANG, date, DocuClassifAdapter.REG_GROUP_INDEX_MEMBERS);
				if (emplCodes != null) { // защото може и да е празна
					String[] codes = emplCodes.split(",");

					LOGGER.info("---------------------------------------------------");
					LOGGER.info("Всички служители в групата");
					for (String code : codes) {

						// взимам елемента заедно със спецификите
						SystemClassif empl = sd.decodeItemLite(Constants.CODE_CLASSIF_ADMIN_STR, Integer.valueOf(code), SysConstants.CODE_DEFAULT_LANG, date, true);

						LOGGER.info("CODE={}", empl.getCode());
						LOGGER.info("\tNAME={}", empl.getTekst());
						LOGGER.info("\tDOP_INFO={}", empl.getDopInfo());
						LOGGER.info("\tEMAIL={}", empl.getSpecifics()[DocuClassifAdapter.ADM_STRUCT_INDEX_CONTACT_EMAIL]);

						// по codeParent имам достъп до звеното, а като се разкодира и до името му
						LOGGER.info("\tZVENO_CODE={}", empl.getCodeParent());
						LOGGER.info("\tZVENO_NAME={}", sd.decodeItem(Constants.CODE_CLASSIF_ADMIN_STR, empl.getCodeParent(), SysConstants.CODE_DEFAULT_LANG, date));
					}
				}
			}

		} catch (Exception e) {
			fail(e.getMessage());
			LOGGER.error(e.getMessage(), e);
		}
	}

	/** */
//	@Test
	public void testClassifReferents() { // CODE_CLASSIF_REFERENTS може да се използва за търсене и разкодиране
		try {
			// търсене само в кореспонденти - специфика REFERENTS_INDEX_CORRESPONDENT=1
			List<SystemClassif> list = sd.queryClassification(CODE_CLASSIF_REFERENTS, "", new Date(), 1);
			LOGGER.info("КОРЕСПОНДЕНТИ");
			for (SystemClassif item : list) {
				LOGGER.info("\t" + item.getCode() + "-" + item.getTekst());
			}

			if (!list.isEmpty()) {
				LOGGER.info("РАЗКОДИРАНЕ");
				LOGGER.info("\t" + sd.decodeItem(CODE_CLASSIF_REFERENTS, list.get(0).getCode(), 1, new Date()));

				LOGGER.info("СПЕЦИФИКИ");
				SystemClassif item = sd.decodeItemLite(CODE_CLASSIF_REFERENTS, list.get(0).getCode(), 1, new Date(), true);

				System.out.println(item.getSpecifics()[0]);
				System.out.println(item.getSpecifics()[1]);
				System.out.println(item.getSpecifics()[2]);
			}

		} catch (Exception e) {
			fail(e.getMessage());
			LOGGER.error(e.getMessage(), e);
		}
	}

	/** */
//	@Test
	public void testClassifRegistri() {
		try {
			List<SystemClassif> list = new ArrayList<>();
			Map<Integer, Object> map = Collections.singletonMap(DocuClassifAdapter.REGISTRI_INDEX_DOC_TYPE, Optional.of(1));

			long t1 = System.currentTimeMillis();
			for (int i = 0; i < 100000; i++) {

				list = sd.queryClassification(DocuConstants.CODE_CLASSIF_REGISTRI, "индекс", null, 1, map);
			}
			long t2 = System.currentTimeMillis();

			for (SystemClassif item : list) {
				LOGGER.info("\t" + item.getTekst());
			}

			LOGGER.info("{}", t2 - t1);

		} catch (Exception e) {
			fail(e.getMessage());
			LOGGER.error(e.getMessage(), e);
		}
	}
	
	
//	@Test
	public void testNotifMap() {
		
		try {
			boolean result = sd.checkUserNotifSettings(-1, 11);
			System.out.println("Result is: " + result);
			
		} catch (DbErrorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail();
		}
		
	}
	
	
	
	
	
	
}