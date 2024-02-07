package com.ib.docu.utils;

import static com.ib.docu.system.DocuConstants.CODE_CLASSIF_DLAJNOST;
import static com.ib.docu.system.DocuConstants.CODE_CLASSIF_MMS_MEJD_FED;
import static com.ib.docu.system.DocuConstants.CODE_CLASSIF_VIDOVE_SPORT;
import static com.ib.docu.system.DocuConstants.CODE_CLASSIF_VID_SPORTEN_OBEKT;
import static com.ib.docu.system.DocuConstants.CODE_CLASSIF_VID_SPORTNO_FORMIROVANIE;
import static com.ib.docu.system.DocuConstants.CODE_CLASSIF_VID_SPORT_OBEDINENIE;
import static com.ib.docu.system.DocuConstants.CODE_ZNACHENIE_ADDR_TYPE_CORRESP;
import static com.ib.docu.system.DocuConstants.CODE_ZNACHENIE_JOURNAL_COACHES;
import static com.ib.docu.system.DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_FORMATIONS;
import static com.ib.docu.system.DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_OBJECTS;
import static com.ib.docu.system.DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORT_OBED;

import java.io.FileOutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.persistence.Query;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import com.ib.docu.system.DocuConstants;
import com.ib.docu.system.SystemData;
import com.ib.mms.iscipr.client.xsd.SportUnionsRequest.SportUnionsRecord;
import com.ib.system.SysConstants;
import com.ib.system.db.DialectConstructor;
import com.ib.system.db.JPA;
import com.ib.system.exceptions.DbErrorException;
import com.ib.system.utils.DateUtils;
import com.ib.system.utils.SearchUtils;

/**
 * Експорт на публични регистри
 *
 * @author belev
 */
public class RegisterExporter {

	private static final String	DELIMITER	= "\t";
	private static final String	NEW_LINE	= "\r\n";

	/** @param args */
	public static void main(String[] args) {
		SystemData sd = new SystemData();

		RegisterExporter exporter = new RegisterExporter();

		try {
//			List<SportUnionsRecord> obedinenia = exporter.selectObedinenia(sd, null);
//			String csvObedinenia = csvFromListArray(obedinenia);
//			try (FileOutputStream outputStream = new FileOutputStream("mms_sport_obedinenie.csv")) {
//				byte[] strToBytes = csvObedinenia.getBytes(StandardCharsets.UTF_8);
//				outputStream.write(strToBytes);
//			}

//			List<String[]> formirovania = exporter.selectFormirovania(sd, null, true);
//			String csvFormirovania = csvFromListArray(formirovania);
//			try (FileOutputStream outputStream = new FileOutputStream("mms_sport_formirovanie.csv")) {
//				byte[] strToBytes = csvFormirovania.getBytes(StandardCharsets.UTF_8);
//				outputStream.write(strToBytes);
//			}
//
//			List<String[]> chlenstva = exporter.selectChlenstva(sd, null, true);
//			String csvChlenstva = csvFromListArray(chlenstva);
//			try (FileOutputStream outputStream = new FileOutputStream("mms_chlenstvo.csv")) {
//				byte[] strToBytes = csvChlenstva.getBytes(StandardCharsets.UTF_8);
//				outputStream.write(strToBytes);
//			}
//
//			List<String[]> sportniObekti = exporter.selectSportniObekti(sd, null, true);
//			String csvSportniObekti = csvFromListArray(sportniObekti);
//			try (FileOutputStream outputStream = new FileOutputStream("mms_sport_obekt.csv")) {
//				byte[] strToBytes = csvSportniObekti.getBytes(StandardCharsets.UTF_8);
//				outputStream.write(strToBytes);
//			}

			List<String[]> trenKadri = exporter.selectTrenKadri(sd, null, true);
			String csvTrenKadri = csvFromListArray(trenKadri);
			try (FileOutputStream outputStream = new FileOutputStream("mms_coaches.csv")) {
				byte[] strToBytes = csvTrenKadri.getBytes(StandardCharsets.UTF_8);
				outputStream.write(strToBytes);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static String csvFromListArray(List<String[]> input) {
		if (input == null || input.isEmpty()) {
			return "";
		}
		StringBuilder sb = new StringBuilder();

		for (String[] row : input) {

			for (int i = 0; i < row.length; i++) {
				sb.append(row[i]);

				if (i < row.length - 1) {
					sb.append(DELIMITER);
				}
			}

			sb.append(NEW_LINE);
		}
		return sb.toString();
	}

	/**
	 * За Членство на спортни формирования в спортни обединения <br>
	 * [0] = "ИД на спортно формирование"; <br>
	 * [1] = "ИД на спортно обединение"; <br>
	 * [2] = "Дата на приемане"; <br>
	 * [3] = "Дата на изключване";
	 *
	 * @param sd
	 * @param selectedList само избрани ИД-а, ако е NULL или празно връща всички
	 * @param addHeader
	 * @return
	 * @throws DbErrorException
	 */
	@SuppressWarnings("unchecked")
	public List<String[]> selectChlenstva(SystemData sd, List<Integer> selectedList, boolean addHeader) throws DbErrorException {
		List<Object[]> rows;
		try {
			StringBuilder sql = new StringBuilder();
			sql.append(" select id, id_object, id_vish_object, date_acceptance, date_termination ");
			sql.append(" from mms_chlenstvo ");
			sql.append(" where type_object = :typeObject and type_vish_object = :typeVishObject ");
			if (selectedList != null && !selectedList.isEmpty()) {
				sql.append(" and id in (:selectedList) ");
			}
			sql.append(" order by 1 ");

			Query query = JPA.getUtil().getEntityManager().createNativeQuery(sql.toString()) //
				.setParameter("typeObject", CODE_ZNACHENIE_JOURNAL_SPORTS_FORMATIONS).setParameter("typeVishObject", CODE_ZNACHENIE_JOURNAL_SPORT_OBED);

			if (selectedList != null && !selectedList.isEmpty()) {
				query.setParameter("selectedList", selectedList);
			}
			rows = query.getResultList();

		} catch (Exception e) {
			throw new DbErrorException("Грешка при извличане на данни за Членство на спортни формирования в спортни обединения!", e);
		} finally {
			JPA.getUtil().closeConnection();
		}

		List<String[]> result = new ArrayList<>();

		if (addHeader) {
			String[] h = new String[4];

			h[0] = "ИД на спортно формирование";
			h[1] = "ИД на спортно обединение";
			h[2] = "Дата на приемане";
			h[3] = "Дата на изключване";

			result.add(h);
		}

		for (Object[] row : rows) {
			String[] s = new String[4];

			s[0] = String.valueOf(row[1]); // id_object
			s[1] = String.valueOf(row[2]); // id_vish_object
			s[2] = getDate(row[3]); // date_acceptance
			s[3] = getDate(row[4]); // date_termination

			result.add(s);
		}
		return result;
	}

	/**
	 * За Спортни формирования <br>
	 * [0] = "ИД на спортното формирование"; <br>
	 * [1] = "Вид на спортното формирование"; <br>
	 * [2] = "Рег. номер на формированието"; <br>
	 * [3] = "Рег. номер на удостоверителен документ"; <br>
	 * [4] = "Дата на удостоверителен документ"; <br>
	 * [5] = "Рег. номер на заповедта за вписване"; <br>
	 * [6] = "Дата на вписване"; <br>
	 * [7] = "Наименование"; <br>
	 * [8] = "ЕИК"; <br>
	 * [9] = "Вид спорт"; <br>
	 * [10] = "Седалище"; <br>
	 * [11] = "Представляващ"; <br>
	 * [12] = "Телефони"; <br>
	 * [13] = "Имейли"; <br>
	 * [14] = "Web-страница"; <br>
	 * [15] = "Дата на заличаване"; <br>
	 * [16] = "Мотиви";
	 *
	 * @param sd
	 * @param selectedList само избрани ИД-а, ако е NULL или празно връща всички
	 * @param addHeader
	 * @return
	 * @throws DbErrorException
	 */
	@SuppressWarnings("unchecked")
	public List<String[]> selectFormirovania(SystemData sd, List<Integer> selectedList, boolean addHeader) throws DbErrorException {
		List<Object[]> rows;
		try {
			String dialect = JPA.getUtil().getDbVendorName();

			StringBuilder sql = new StringBuilder();

			sql.append(" select f.id, f.vid, f.reg_nomer ");
			sql.append(" , v.rn_doc_licenz, v.date_doc_licenz, v.rn_doc_result, v.date_doc_result ");
			sql.append(" , r.ref_name, a.ekatte, a.addr_text, r.nfl_eik ");
			sql.append(" , " + DialectConstructor.convertToDelimitedString(dialect, "vs.vid_sport",
				"mms_vid_sport vs where vs.tip_object = " + CODE_ZNACHENIE_JOURNAL_SPORTS_FORMATIONS + " and vs.id_object = f.id", "vs.id") + " vid_sport ");
			sql.append(" , r.predstavitelstvo ");
			sql.append(" , r.contact_phone, r.contact_email, r.web_page ");
			sql.append(" , v.reason_vpisvane_text ");
			sql.append(" , v.rn_doc_vpisvane, v.date_doc_vpisvane ");
			sql.append(" from mms_sport_formirovanie f ");
			sql.append(" inner join adm_referents r on r.code = f.id_object ");
			sql.append(" left outer join adm_ref_addrs a on a.code_ref = r.code and a.addr_type = :addrType ");
			sql.append(" inner join mms_vpisvane v on v.id_object = f.id and v.type_object = :typeObject ");
			if (selectedList != null && !selectedList.isEmpty()) {
				sql.append(" where f.id in (:selectedList) ");
			}
			sql.append(" order by 1 ");

			Query query = JPA.getUtil().getEntityManager().createNativeQuery(sql.toString()) //
				.setParameter("typeObject", CODE_ZNACHENIE_JOURNAL_SPORTS_FORMATIONS).setParameter("addrType", CODE_ZNACHENIE_ADDR_TYPE_CORRESP);

			if (selectedList != null && !selectedList.isEmpty()) {
				query.setParameter("selectedList", selectedList);
			}
			rows = query.getResultList();

		} catch (Exception e) {
			throw new DbErrorException("Грешка при извличане на данни за Спортни формирования!", e);
		} finally {
			JPA.getUtil().closeConnection();
		}

		List<String[]> result = new ArrayList<>();

		if (addHeader) {
			String[] h = new String[17];

			h[0] = "ИД на спортното формирование";
			h[1] = "Вид на спортното формирование";
			h[2] = "Рег. номер на формированието";
			h[3] = "Рег. номер на удостоверителен документ";
			h[4] = "Дата на удостоверителен документ";
			h[5] = "Рег. номер на заповедта за вписване";
			h[6] = "Дата на вписване";
			h[7] = "Наименование";
			h[8] = "ЕИК";
			h[9] = "Вид спорт";
			h[10] = "Седалище";
			h[11] = "Представляващ";
			h[12] = "Телефони";
			h[13] = "Имейли";
			h[14] = "Web-страница";
			h[15] = "Дата на заличаване";
			h[16] = "Мотиви";

			result.add(h);
		}

		for (Object[] row : rows) {
			String[] s = new String[17];

			s[0] = String.valueOf(row[0]); // id
			s[1] = decode(sd, CODE_CLASSIF_VID_SPORTNO_FORMIROVANIE, row[1]); // vid
			s[2] = getString(row[2]); // reg_nomer
			s[3] = getString(row[3]); // rn_doc_licenz
			s[4] = getDate(row[4]); // date_doc_licenz
			s[5] = getString(row[5]); // rn_doc_result
			s[6] = getDate(row[6]); // date_doc_result
			s[7] = getString(row[7]); // ref_name
			s[8] = getString(row[10]); // nfl_eik
			s[9] = decodeMulti(sd, CODE_CLASSIF_VIDOVE_SPORT, row[11]); // vid_sport

			String location = decode(sd, SysConstants.CODE_CLASSIF_EKATTE, row[8]); // ekatte
			if ("".equals(location)) {
				s[10] = getString(row[9]); // addr_text
			} else {
				s[10] = location + ", " + getString(row[9]); // addr_text
			}

			s[11] = getString(row[12]); // predstavitelstvo
			s[12] = getString(row[13]); // contact_phone
			s[13] = getString(row[14]); // contact_email
			s[14] = getString(row[15]); // web_page
			s[15] = getDate(row[18]); // date_doc_vpisvane
			s[16] = getString(row[16]); // reason_vpisvane_text

			result.add(s);
		}
		return result;
	}

	/**
	 * За Спортни обединения <br>
	 * [0] = "ИД на спортно обединение"; <br>
	 * [1] = "Вид на спортното обединение"; <br>
	 * [2] = "Рег. номер на спортно обединение"; <br>
	 * [3] = "Номер на лиценз"; <br>
	 * [4] = "Дата на лиценз"; <br>
	 * [5] = "Рег. номер на заповедта за вписване"; <br>
	 * [6] = "Дата на заповедта за вписване"; <br>
	 * [7] = "Наименование"; <br>
	 * [8] = "Седалище"; <br>
	 * [9] = "adres"; <br>
	 * [10] = "ЕИК"; <br>
	 * [11] = "Вид спорт"; <br>
	 * [12] = "Представляващ"; <br>
	 * [13] = "Телефони"; <br>
	 * [14] = "Имейли"; <br>
	 * [15] = "Web-страница"; <br>
	 * [16] = "Международни спортни организации в които членува"; <br>
	 * [17] = "Заповед за прекратяване/отнемане на лиценз"; <br>
	 * [18] = "Дата на отнемане";
	 * [19] = "Статус";
	 * [20] = "Дата на статус";
	 * [21] = "Статус на вписване";
	 *
 
	 *
	 * @param sd
	 * @param selectedList само избрани ИД-а, ако е NULL или празно връща празен списък
	 * @param addHeader
	 * @return
	 * @throws DbErrorException
	 */
	@SuppressWarnings("unchecked")
	public List<SportUnionsRecord> selectObedinenia(SystemData sd, List<Integer> selectedList) throws DbErrorException {
		List<Object[]> rows;

		try {
			if (selectedList==null || selectedList.size()==0) {
				rows=new ArrayList<Object[]>();
			}else {
				String dialect = JPA.getUtil().getDbVendorName();
	
				StringBuilder sql = new StringBuilder();
	
				sql.append(" select o.id, o.vid, o.reg_nomer ");
				sql.append(" , v.rn_doc_licenz, v.date_doc_licenz, v.rn_doc_result, v.date_doc_result ");
				sql.append(" , r.ref_name, a.ekatte, a.addr_text, r.nfl_eik ");
				sql.append(" , " + DialectConstructor.convertToDelimitedString(dialect, "vs.vid_sport",
					"mms_vid_sport vs where vs.tip_object = " + CODE_ZNACHENIE_JOURNAL_SPORT_OBED + " and vs.id_object = o.id", "vs.id") + " vid_sport ");
				sql.append(" , r.predstavitelstvo ");
				sql.append(" , r.contact_phone, r.contact_email, r.web_page ");
				sql.append(" , " + DialectConstructor.convertToDelimitedString(dialect, "mf.mejd_fed", "mms_sport_obed_mf mf where mf.id_sport_obed = o.id", "mf.id") + " mejd ");
				sql.append(" , v.rn_doc_vpisvane, v.date_doc_vpisvane, o.status, o.date_status, v.status_vpisvane ");
				sql.append(" from mms_sport_obedinenie o ");
				sql.append(" inner join adm_referents r on r.code = o.id_object ");
				sql.append(" left outer join adm_ref_addrs a on a.code_ref = r.code and a.addr_type = :addrType ");
				sql.append(" inner join mms_vpisvane v on v.id = (select max (v1.id) from mms_vpisvane v1 where v1.id_object = o.id AND v1.type_object = :typeObject) ");
				if (selectedList != null && !selectedList.isEmpty()) {
					sql.append(" where o.id in (:selectedList) ");
				}
				sql.append(" order by o.id ");
				 System.out.println(sql.toString());
				Query query = JPA.getUtil().getEntityManager().createNativeQuery(sql.toString()) //
					.setParameter("typeObject", CODE_ZNACHENIE_JOURNAL_SPORT_OBED).setParameter("addrType", CODE_ZNACHENIE_ADDR_TYPE_CORRESP);
	
				if (selectedList != null && !selectedList.isEmpty()) {
					query.setParameter("selectedList", selectedList);
				}
				rows = query.getResultList();
			}

		} catch (Exception e) {
			throw new DbErrorException("Грешка при извличане на данни за Спортни обединения!", e);
		} finally {
			JPA.getUtil().closeConnection();
		}

		List<SportUnionsRecord> result = new ArrayList<SportUnionsRecord>();

		for (Object[] row : rows) {
			SportUnionsRecord record=new SportUnionsRecord();

			record.setExternalId(String.valueOf(row[0])); // id
			record.setSportUnionType(decode(sd, CODE_CLASSIF_VID_SPORT_OBEDINENIE, row[1])); // vid
			record.setSportUnionRegNumber(getString(row[2])); // reg_nomer
			
			record.setUnionName(getString(row[7]));  //ref_name

	       
	        
	         
	        

	        String location = decode(sd, SysConstants.CODE_CLASSIF_EKATTE, row[8]); // ekatte
	        record.setUnionLocation(location);  
	        
			record.setUnionAddress(getString(row[9]));  // addr_text
			
			record.setUnionStatus(decode(sd, DocuConstants.CODE_CLASSIF_STATUS_OBEKT, row[19]));
			if (row[20]!=null) {
				try {
					record.setUnionStatusDate(toGregorianCalendar((Date)row[20]));
				} catch (DatatypeConfigurationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
			}
			
			record.setSport(decodeMulti(sd, CODE_CLASSIF_VIDOVE_SPORT, row[11]));
			record.setPresenter( getString(row[12]));
	        record.setTelephone(getString(row[13])); //
	        record.setEmails(getString(row[14]));
	        record.setEIK(new BigInteger(getString(row[10])));
	        record.setWebPage(getString(row[15]));
	        
	        record.setForeignData(decodeMulti(sd, CODE_CLASSIF_MMS_MEJD_FED, row[16])); 
	        
			
	       
			 if ((""+row[21]).equals("1")) {
				 record.setLicenceNumber(getString(row[3])); // rn_doc_licenz
			     record.setRegNumberEntryOrder(getString(row[17]));  
			        try {
			        	record.setEntryOrderDate(toGregorianCalendar((Date)row[18]));// date_doc_vpisvane
					} catch (DatatypeConfigurationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}  
				}else {
					if ((""+row[21]).equals("2") || (""+row[21]).equals("3")) {
						record.setCancelationOrder(getString(row[17]));
				        try {
							record.setCancelDate(toGregorianCalendar((Date)row[18]));
						} catch (DatatypeConfigurationException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}		
					}
				}
			
			result.add(record);
		}
		return result;
	}
	
	private static XMLGregorianCalendar toGregorianCalendar(Date date) throws DatatypeConfigurationException  {
		if (date == null) {
			return null;
		}
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(date);

		return DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);
	}

	/**
	 * За Спортни обекти <br>
	 * [0] = "ИД на спортен обект"; <br>
	 * [1] = "Вид на обекта"; <br>
	 * [2] = "Регистрационен номер"; <br>
	 * [3] = "Наименование"; <br>
	 * [4] = "Местонахождение"; <br>
	 * [5] = "Телефони"; <br>
	 * [6] = "Имейли"; <br>
	 * [7] = "Web-страница"; TODO няма такова колона в БД
	 *
	 * @param sd
	 * @param selectedList само избрани ИД-а, ако е NULL или празно връща всички
	 * @param addHeader
	 * @return
	 * @throws DbErrorException
	 */
	@SuppressWarnings("unchecked")
	public List<String[]> selectSportniObekti(SystemData sd, List<Integer> selectedList, boolean addHeader) throws DbErrorException {
		List<Object[]> rows;
		try {
			StringBuilder sql = new StringBuilder();
			sql.append(" select o.id, o.vid, o.reg_nomer, o.name ");
			sql.append(" , o.nas_mesto, o.identification ");
			sql.append(" , o.tel, o.e_mail ");
			sql.append(" from mms_sport_obekt o ");
			sql.append(" inner join mms_vpisvane v on v.id_object = o.id and v.type_object = :typeObject ");
			if (selectedList != null && !selectedList.isEmpty()) {
				sql.append(" where o.id in (:selectedList) ");
			}
			sql.append(" order by 1 ");

			Query query = JPA.getUtil().getEntityManager().createNativeQuery(sql.toString()) //
				.setParameter("typeObject", CODE_ZNACHENIE_JOURNAL_SPORTS_OBJECTS);

			if (selectedList != null && !selectedList.isEmpty()) {
				query.setParameter("selectedList", selectedList);
			}
			rows = query.getResultList();

		} catch (Exception e) {
			throw new DbErrorException("Грешка при извличане на данни за Спортни обекти!", e);
		} finally {
			JPA.getUtil().closeConnection();
		}

		List<String[]> result = new ArrayList<>();

		if (addHeader) {
			String[] h = new String[8];

			h[0] = "ИД на спортен обект";
			h[1] = "Вид на обекта";
			h[2] = "Регистрационен номер";
			h[3] = "Наименование";
			h[4] = "Местонахождение";
			h[5] = "Телефони";
			h[6] = "Имейли";
			h[7] = "Web-страница";

			result.add(h);
		}

		for (Object[] row : rows) {
			String[] s = new String[8];

			s[0] = String.valueOf(row[0]); // id
			s[1] = decode(sd, CODE_CLASSIF_VID_SPORTEN_OBEKT, row[1]); // vid
			s[2] = getString(row[2]); // reg_nomer
			s[3] = getString(row[3]); // name

			String location = decode(sd, SysConstants.CODE_CLASSIF_EKATTE, row[4]); // nas_mesto
			if ("".equals(location)) {
				s[4] = getString(row[5]); // identification
			} else {
				s[4] = location + ", " + getString(row[5]); // identification
			}

			s[5] = getString(row[6]); // tel
			s[6] = getString(row[7]); // e_mail
			s[7] = ""; // ???

			result.add(s);
		}
		return result;
	}

	/**
	 * За Треньорски кадри <br>
	 * [0] = "ИД на лицето"; <br>
	 * [1] = "Име"; <br>
	 * [2] = "Презиме"; <br>
	 * [3] = "Фамилия"; <br>
	 * [4] = "Рег. номер"; <br>
	 * [5] = "Дата на вписване"; <br>
	 * [6] = "Вид спорт"; <br>
	 * [7] = "Длъжност";
	 *
	 * @param sd
	 * @param selectedList само избрани ИД-а, ако е NULL или празно връща всички
	 * @param addHeader
	 * @return
	 * @throws DbErrorException
	 */
	@SuppressWarnings("unchecked")
	public List<String[]> selectTrenKadri(SystemData sd, List<Integer> selectedList, boolean addHeader) throws DbErrorException {
		List<Object[]> rows;
		try {
			StringBuilder sql = new StringBuilder();
			sql.append(" select c.id, r.ime, r.prezime, r.familia, v.rn_doc_licenz ");
			sql.append(" , v.rn_doc_result, v.date_doc_result ");
			sql.append(" , v.vid_sport, v.dlajnost ");
			sql.append(" from mms_coaches c ");
			sql.append(" inner join adm_referents r on r.code = c.id_object and r.date_smart is null ");
			sql.append(" inner join mms_vpisvane v on v.id_object = c.id and v.type_object = :typeObject ");
			if (selectedList != null && !selectedList.isEmpty()) {
				sql.append(" where c.id in (:selectedList) ");
			}
			sql.append(" order by 1 ");

			Query query = JPA.getUtil().getEntityManager().createNativeQuery(sql.toString()) //
				.setParameter("typeObject", CODE_ZNACHENIE_JOURNAL_COACHES);

			if (selectedList != null && !selectedList.isEmpty()) {
				query.setParameter("selectedList", selectedList);
			}
			rows = query.getResultList();

		} catch (Exception e) {
			throw new DbErrorException("Грешка при извличане на данни за Треньорски кадри!", e);
		} finally {
			JPA.getUtil().closeConnection();
		}

		List<String[]> result = new ArrayList<>();

		if (addHeader) {
			String[] h = new String[8];

			h[0] = "ИД на лицето";
			h[1] = "Име";
			h[2] = "Презиме";
			h[3] = "Фамилия";
			h[4] = "Рег. номер";
			h[5] = "Дата на вписване";
			h[6] = "Вид спорт";
			h[7] = "Длъжност";

			result.add(h);
		}

		for (Object[] row : rows) {
			String[] s = new String[8];

			s[0] = String.valueOf(row[0]); // code
			s[1] = getString(row[1]); // ime
			s[2] = getString(row[2]); // prezime
			s[3] = getString(row[3]); // familia
			s[4] = getString(row[4]); // rn_doc_licenz
			s[5] = getDate(row[6]); // date_doc_result
			s[6] = decode(sd, CODE_CLASSIF_VIDOVE_SPORT, row[7]); // vid_sport
			s[7] = decode(sd, CODE_CLASSIF_DLAJNOST, row[8]); // dlajnost

			result.add(s);
		}
		return result;
	}

	private String decode(SystemData sd, Integer classif, Object object) throws DbErrorException {
		if (object == null) {
			return "";
		}
		int code = ((Number) object).intValue();
		String string = sd.decodeItem(classif, code, SysConstants.CODE_LANG_BG, null);
		if (string == null || string.startsWith("Ненамерено значение")) {
			return "";
		}
		return string;
	}

	private String decodeMulti(SystemData sd, Integer classif, Object object) throws DbErrorException {
		String codes = SearchUtils.trimToNULL((String) object);
		if (codes == null) {
			return "";
		}
		return sd.decodeItems(classif, codes, SysConstants.CODE_LANG_BG, null);
	}

	private String getDate(Object object) {
		if (object == null) {
			return "";
		}
		Date date = (Date) object;
		return DateUtils.printDate(date);
	}

	private String getString(Object object) {
		String string = SearchUtils.trimToNULL((String) object);
		if (string == null) {
			return "";
		}

		if (string.indexOf(DELIMITER) != -1) {
			string = string.replace(DELIMITER, " ");
		}
		if (string.indexOf(NEW_LINE) != -1) {
			string = string.replace(NEW_LINE, ", ");
		}
		if (string.indexOf("  ") != -1) {
			string = string.replace("  ", " ");
		}
		return string;
	}
}
