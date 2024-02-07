package com.ib.docu.utils;

import static com.ib.system.utils.SearchUtils.asInteger;
import static com.ib.system.utils.SearchUtils.asLong;
import static com.ib.system.utils.SearchUtils.asString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.docu.db.dao.DocDAO;
import com.ib.docu.system.DocuConstants;
import com.ib.docu.system.SystemData;
import com.ib.docu.system.UserData;
import com.ib.indexui.system.Constants;
import com.ib.system.db.DialectConstructor;
import com.ib.system.db.JPA;
import com.ib.system.db.SelectMetadata;
import com.ib.system.exceptions.DbErrorException;
import com.ib.system.exceptions.InvalidParameterException;

public class DashboardUtils {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DashboardUtils.class);
	
	
	
	
	/**
	 * Изчислява бройки за dashboard-a, свързани със задачите
	 *
	 * @param userId - потребител
	 * @param map - Мап със бройки и ключ опцията (виж константите горе)
	 * @param isAssigner -  за възложител
	 * @param isConroller -  за контролиращ
	 * @param isExcecutor -  за изпълнител
	 * @param daysToEnd - дни до изтичането
	 * @return
	 * @throws DbErrorException
	 */
	@SuppressWarnings("unchecked")
	public void calculateTasksSection(Integer userId, Map<Long, String> map, boolean isAssigner, boolean isConroller, boolean isExcecutor, Integer daysToEnd ,List<Integer> statusList) throws DbErrorException {
		
		
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTime(new Date());
		gc.add(Calendar.DAY_OF_YEAR, daysToEnd);
		Date dat = gc.getTime();
		
		try {
			if (!isAssigner && !isConroller && !isExcecutor) {
				return;
			}
			
			String union = "";
			StringBuilder sql =new StringBuilder("");
			if (isAssigner) {
				sql.append(union);
				sql.append("select count(TASK_ID) cnt , "+DocuConstants.CODE_ZNACHENIE_DASHBOARD_ASSIGN+" as tip from TASK where CODE_ASSIGN = :id and STATUS in :statusList and TASK_TYPE not in :typeList");
				union = " UNION ";
			}
			
			if (isConroller) {
				sql.append(union);
				sql.append("select count(TASK_ID) cnt , "+DocuConstants.CODE_ZNACHENIE_DASHBOARD_CONTROL+" as tip from TASK where CODE_CONTROL = :id and STATUS in :statusList and TASK_TYPE not in :typeList");
				union = " UNION ";
			}
			
			if (isExcecutor) {
				sql.append(union);
				sql.append("select count(TASK.TASK_ID), "+DocuConstants.CODE_ZNACHENIE_DASHBOARD_EXEC+" as tip from TASK join TASK_REFERENTS on TASK_REFERENTS.TASK_ID = TASK.TASK_ID where TASK_REFERENTS.CODE_REF = :id and STATUS in :statusList and TASK_TYPE not in :typeList");
				union = " UNION ";
			}
			
			sql.append(union);
			sql.append("select count(TASK.task_id), "+DocuConstants.CODE_ZNACHENIE_DASHBOARD_GOING_LATE+" as tip from TASK join TASK_REFERENTS on TASK_REFERENTS.TASK_ID = TASK.TASK_ID where TASK_REFERENTS.CODE_REF = :id and STATUS in :statusList and SROK_DATE <= :DAT AND SROK_DATE>= :DAT1  and TASK_TYPE not in :typeList");
			
			sql.append(union);
			sql.append("select count(TASK.task_id), "+DocuConstants.CODE_ZNACHENIE_DASHBOARD_LATE+" as tip from TASK join TASK_REFERENTS on TASK_REFERENTS.TASK_ID = TASK.TASK_ID where TASK_REFERENTS.CODE_REF = :id and STATUS in :statusList and SROK_DATE <= :DAT1  and TASK_TYPE not in :typeList");
			
			Query query = JPA.getUtil().getEntityManager().createNativeQuery(sql.toString());
			query.setParameter("statusList", statusList);
			
			
			query.setParameter("typeList",Arrays.asList(DocuConstants.CODE_ZNACHENIE_TASK_TYPE_REZOL , DocuConstants.CODE_ZNACHENIE_TASK_TYPE_SAGL ,DocuConstants.CODE_ZNACHENIE_TASK_TYPE_PODPIS));
						
			query.setParameter("id", userId);
			query.setParameter("DAT", dat);
			query.setParameter("DAT1", new Date());
			
			ArrayList<Object[]> rows = (ArrayList<Object[]>) query.getResultList();
			
			Integer count = 0; 
			for (Object[] row : rows) {
				
				map.put(asLong(row[1]), asString(row[0]));
				
				//Неизпълнени задачи - да се преименува на "Мои неизпълнени задачи" , бройката да включва само "неизпълнени задачи, на които съм изпълнител"
				if(asLong(row[1]).intValue() == DocuConstants.CODE_ZNACHENIE_DASHBOARD_EXEC) {
					count =  asInteger(row[0]);
				}
				
//				Integer br = asInteger(row[0]);
//				if(br!=null) {
//					count += br;
//				}		
			}
			
			map.put(Long.valueOf(DocuConstants.CODE_ZNACHENIE_DASHBOARD_TASK), count.toString());
			
		} catch (Exception e) {
			LOGGER.error("Грешки при извличане на бройки за задачи !", e);
			throw new DbErrorException("Грешка при търсене на брой за задачи !", e);
		}
		
		
	}
	
	/**
	 * Изгражда заявката за dashboard-a, свързани със задачите базиран на calculateTasksSection метода
	 *	
	 * [0]-TASK_ID<br>
	 * [1]-RN_TASK<br>
	 * [2]-TASK_TYPE<br>
	 * [3]-STATUS<br>
	 * [4]-SROK_DATE<br>
	 * [5]-ASSIGN_DATE<br>
	 * [6]-COMMENTS<br>
	 * [7]-STATUS_USER_ID<br>
	 * [8]-CODE_ASSIGN<br>
	 * [9]-CODE_EXECS-кодовете на изпълнителите с разделител ',' (5,2,20)
	 * [10]-TASK_INFO<br>
	 * [11]-DOC_ID<br>
	 * [12]-RN_DOC<br>
	 * [13]-DOC_DATE<br>
	 * [14]-DOC_VID<br>
	 * [15]-DOC_TYPE<br>
	 * 
	 * @param key - идентификатор за условията по-които ще се изгради завяката
	 * @param userId - потребител
	 * @param daysToEnd - дни до изтичането
	 * @param statusList - списък със статуси които се водят за активни
	 * @return
	 * @throws InvalidParameterException
	 */
	public SelectMetadata sqlTasksSection(int key ,Integer userId,  Integer daysToEnd ,List<Integer> statusList ,String textSearch, boolean fullEq) throws InvalidParameterException {
		
		
		Date datToday = new Date();
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTime(new Date());
		gc.set(Calendar.HOUR, 0);
		gc.set(Calendar.MINUTE, 0);
		gc.set(Calendar.SECOND, 0);
		gc.set(Calendar.MILLISECOND, 0);
		datToday = gc.getTime();
		
		if (userId == null) {
			throw new InvalidParameterException("Невалидни параметри");
		}
		
		String dialect = JPA.getUtil().getDbVendorName();
		
		Map<String, Object> params = new HashMap<>();
		
		StringBuilder select = new StringBuilder();
		StringBuilder from = new StringBuilder();
		StringBuilder where = new StringBuilder();

		try {
			
			select.append(" select t.TASK_ID a0, t.RN_TASK a1, t.TASK_TYPE a2, t.STATUS a3, t.SROK_DATE a4, t.ASSIGN_DATE a5, t.STATUS_COMMENTS a6, t.STATUS_USER_ID a7, t.CODE_ASSIGN a8, ");
			select.append(DialectConstructor.convertToDelimitedString(dialect, "tr.CODE_REF", "TASK_REFERENTS tr where tr.TASK_ID = t.TASK_ID", "tr.ROLE_REF, tr.ID") + " a9, ");
			
			select.append(DialectConstructor.limitBigString(dialect, "t.TASK_INFO", 150) + " a10, ");
			
			select.append(" d.DOC_ID a11, "+DocDAO.formRnDocSelect("d.", dialect)+" a12, d.DOC_DATE a13, d.DOC_VID a14 , d.DOC_TYPE a15 ,d.CODE_REF_CORRESP a16,");
			select.append(" CASE WHEN d.DOC_TYPE = 1 THEN null ELSE "); 
			select.append(DialectConstructor.convertToDelimitedString(dialect, "DR.CODE_REF", "DOC_REFERENTS DR where DR.DOC_ID = d.DOC_ID and DR.ROLE_REF = "+DocuConstants.CODE_ZNACHENIE_DOC_REF_ROLE_AUTHOR	, "DR.PORED"));
			select.append(" END a17 , D.URGENT a18 ,");
			select.append(DialectConstructor.limitBigString(dialect, "d.OTNOSNO", 150) + " a19 ");
			
			from.append(" from TASK t  left outer join DOC d on d.DOC_ID = t.DOC_ID ");
			
			params.put("id",userId);
			params.put("statusList",statusList);
			
			switch(key) {
			
				case DocuConstants.CODE_ZNACHENIE_DASHBOARD_ASSIGN:
					where.append(" where t.CODE_ASSIGN = :id and t.STATUS in (:statusList) and t.TASK_TYPE not in (:typeList) ");
					params.put("typeList",Arrays.asList(DocuConstants.CODE_ZNACHENIE_TASK_TYPE_REZOL , DocuConstants.CODE_ZNACHENIE_TASK_TYPE_SAGL ,DocuConstants.CODE_ZNACHENIE_TASK_TYPE_PODPIS));
				break;
				
				case DocuConstants.CODE_ZNACHENIE_DASHBOARD_CONTROL:
					where.append(" where t.CODE_CONTROL = :id and t.STATUS in (:statusList) and t.TASK_TYPE not in (:typeList) ");
					params.put("typeList",Arrays.asList(DocuConstants.CODE_ZNACHENIE_TASK_TYPE_REZOL , DocuConstants.CODE_ZNACHENIE_TASK_TYPE_SAGL ,DocuConstants.CODE_ZNACHENIE_TASK_TYPE_PODPIS));
				break;
				
				case DocuConstants.CODE_ZNACHENIE_DASHBOARD_EXEC:
					from.append(" join TASK_REFERENTS on TASK_REFERENTS.TASK_ID = t.TASK_ID ");
					where.append(" where TASK_REFERENTS.CODE_REF = :id and t.STATUS in (:statusList) and t.TASK_TYPE not in (:typeList) ");
					params.put("typeList",Arrays.asList(DocuConstants.CODE_ZNACHENIE_TASK_TYPE_REZOL , DocuConstants.CODE_ZNACHENIE_TASK_TYPE_SAGL ,DocuConstants.CODE_ZNACHENIE_TASK_TYPE_PODPIS));
				break;
				
				case DocuConstants.CODE_ZNACHENIE_DASHBOARD_GOING_LATE:
					if (daysToEnd == null) {
						throw new InvalidParameterException("Невалидни параметри");
					}
					
					from.append(" join TASK_REFERENTS on TASK_REFERENTS.TASK_ID = t.TASK_ID ");
					where.append(" where TASK_REFERENTS.CODE_REF = :id and t.STATUS in (:statusList) and SROK_DATE <= :DAT AND SROK_DATE>= :DAT1 and t.TASK_TYPE not in (:typeList)");
					
					
					gc.setTime(new Date());
					gc.add(Calendar.DAY_OF_YEAR, daysToEnd);
					Date dat = gc.getTime();
					
					params.put("DAT",dat);
					params.put("DAT1",new Date());
					params.put("typeList",Arrays.asList(DocuConstants.CODE_ZNACHENIE_TASK_TYPE_REZOL , DocuConstants.CODE_ZNACHENIE_TASK_TYPE_SAGL ,DocuConstants.CODE_ZNACHENIE_TASK_TYPE_PODPIS));
				break;
				
				case DocuConstants.CODE_ZNACHENIE_DASHBOARD_LATE:
					from.append(" join TASK_REFERENTS on TASK_REFERENTS.TASK_ID = t.TASK_ID ");
					where.append(" where TASK_REFERENTS.CODE_REF = :id and t.STATUS in (:statusList) and SROK_DATE <= :DAT1 and t.TASK_TYPE not in (:typeList)");
					params.put("DAT1",datToday);
					params.put("typeList",Arrays.asList(DocuConstants.CODE_ZNACHENIE_TASK_TYPE_REZOL , DocuConstants.CODE_ZNACHENIE_TASK_TYPE_SAGL ,DocuConstants.CODE_ZNACHENIE_TASK_TYPE_PODPIS));
				break;
			}
			
			
			if(textSearch!=null && !textSearch.trim().isEmpty()) {
				
				if(fullEq) {
					where .append(" AND ( upper(d.RN_DOC) =:textSearch OR upper(t.RN_TASK) =:textSearch) ");
					params.put("textSearch",textSearch.trim().toUpperCase());
				} else {
					where .append(" AND ( upper(d.RN_DOC) LIKE :textSearch OR upper(t.RN_TASK) LIKE :textSearch) ");
					params.put("textSearch","%"+textSearch.trim().toUpperCase()+"%");
				}
			}
			
			SelectMetadata sm = new SelectMetadata();
			
			sm.setSqlCount(" select count(*) " + from.toString() + where.toString());
			sm.setSql(select.toString() + from.toString() + where.toString());
			sm.setSqlParameters(params);
			
			return sm;
		} catch (Exception e) {
			LOGGER.error("Грешки при изграждане на slq за задачи раб. плот !", e);
			throw new InvalidParameterException("Грешки при изграждане на slq за задачи раб. плот !");
		}
		
		
	}
	
	/**
	 * Изчислява бройки за dashboard-a, свързани с документи
	 *
	 * @param userId - потребител
	 * @param map - Мап със бройки и ключ опцията (виж константите горе)	
	 * @return
	 * @throws DbErrorException
	 */
	@SuppressWarnings("unchecked")
	public void calculateDocSection(UserData ud, Map<Long, String> map ,List<Integer> statusList) throws DbErrorException {
		
		try {
			Integer count = 0; //Документи - да се преименува на "Документи за обработка" , бройката да включва: " за резолюция" + "за съгласуване" + "за подпис" MANTIS 14301
			
			Query query = JPA.getUtil().getEntityManager().createNativeQuery("select count(TASK_TYPE) cnt, TASK_TYPE from TASK join TASK_REFERENTS on TASK_REFERENTS.TASK_ID = TASK.TASK_ID where TASK_REFERENTS.CODE_REF = :id and STATUS in :STAT and TASK_TYPE in :typeList group by TASK_TYPE");
			query.setParameter("STAT", statusList);
			query.setParameter("id", ud.getUserAccess());
			query.setParameter("typeList",Arrays.asList(DocuConstants.CODE_ZNACHENIE_TASK_TYPE_REZOL , DocuConstants.CODE_ZNACHENIE_TASK_TYPE_SAGL ,DocuConstants.CODE_ZNACHENIE_TASK_TYPE_PODPIS));
			
			ArrayList<Object[]> rows = (ArrayList<Object[]>) query.getResultList();
			map.put(Long.valueOf(DocuConstants.CODE_ZNACHENIE_DASHBOARD_FOR_REZOL), "0");
			map.put(Long.valueOf(DocuConstants.CODE_ZNACHENIE_DASHBOARD_FOR_SAGL),  "0");
			map.put(Long.valueOf(DocuConstants.CODE_ZNACHENIE_DASHBOARD_FOR_PODPIS),"0");			
			
			Integer br;
			for (Object[] row : rows) {
				if (asInteger(row[1]) == DocuConstants.CODE_ZNACHENIE_TASK_TYPE_REZOL) {
					map.put(Long.valueOf(DocuConstants.CODE_ZNACHENIE_DASHBOARD_FOR_REZOL), asString(row[0]));
					
					br = asInteger(row[0]);
					if(br!=null) {
						count += br;
					}
				}
				
				if (asInteger(row[1]) == DocuConstants.CODE_ZNACHENIE_TASK_TYPE_SAGL) {
					map.put(Long.valueOf(DocuConstants.CODE_ZNACHENIE_DASHBOARD_FOR_SAGL), asString(row[0]));
					br = asInteger(row[0]);
					if(br!=null) {
						count += br;
					}
				}
				
				if (asInteger(row[1]) == DocuConstants.CODE_ZNACHENIE_TASK_TYPE_PODPIS) {
					map.put(Long.valueOf(DocuConstants.CODE_ZNACHENIE_DASHBOARD_FOR_PODPIS), asString(row[0]));
					br = asInteger(row[0]);
					if(br!=null) {
						count += br;
					}
				}
			}
			
			if (ud.getRegistratura() == null) { // за мигрирани потребители може да няма регистратура и това ще гърми
				br = 0;
			} else {
			query = JPA.getUtil().getEntityManager().createNativeQuery("select count(D.DOC_ID) from doc D where WORK_OFF_ID is null  and FOR_REG_ID is null and PROCESSED = :process and DOC_TYPE=:dType and REGISTRATURA_ID = :regId "
					+ " and exists (select dr.ID from DOC_REFERENTS dr where dr.DOC_ID = D.DOC_ID and dr.CODE_REF = :id and dr.ROLE_REF = :roleRef "
					+ " union all"
					+ " select tr.ID from TASK t inner join TASK_REFERENTS tr on tr.TASK_ID = t.TASK_ID  where t.DOC_ID = D.DOC_ID and tr.CODE_REF = :id and t.TASK_TYPE in (:taskType) )");	
			
			query.setParameter("id", ud.getUserAccess());
			query.setParameter("process", DocuConstants.CODE_ZNACHENIE_NE);
			query.setParameter("dType", DocuConstants.CODE_ZNACHENIE_DOC_TYPE_WRK);
			query.setParameter("roleRef", DocuConstants.CODE_ZNACHENIE_DOC_REF_ROLE_AUTHOR);
//			query.setParameter("stat",statusList); // не се искат статусите на задачи
			query.setParameter("taskType",Arrays.asList(DocuConstants.CODE_ZNACHENIE_TASK_TYPE_SAGL,DocuConstants.CODE_ZNACHENIE_TASK_TYPE_PODPIS));
			query.setParameter("regId",ud.getRegistratura());
			
			br = asInteger(query.getSingleResult());
			}
			map.put(Long.valueOf(DocuConstants.CODE_ZNACHENIE_DASHBOARD_NONREG), asString(br));
			
//			if(br!=null) {
//				count += br;
//			}
//			
			//--- za zapoznawane
			
			query = JPA.getUtil().getEntityManager().createNativeQuery(" select count(D.DOC_ID)  from DOC D join USER_NOTIFICATIONS on D.DOC_ID = USER_NOTIFICATIONS.OBJECT_ID   where USER_ID = :ID and MESSAGE_TYPE = :MT and READ = :RD ");
			query.setParameter("ID", ud.getUserAccess());
			query.setParameter("MT", DocuConstants.CODE_ZNACHENIE_NOTIFF_EVENTS_DOC_ACCESS);
			query.setParameter("RD", DocuConstants.CODE_ZNACHENIE_NE);
			
			br = asInteger(query.getSingleResult());
			map.put(Long.valueOf(DocuConstants.CODE_ZNACHENIE_DASHBOARD_ZA_ZAPOZNAVANE), asString(br));
//			if(br!=null) {
//				count += br;
//			}
			//---------------
			map.put(Long.valueOf(DocuConstants.CODE_ZNACHENIE_DASHBOARD_TASK_DOC), count.toString());
			
		} catch (Exception e) {
			LOGGER.error("Грешки при извличане на бройки за документи !", e);
			throw new DbErrorException("Грешка при търсене на брой за документи", e);
		}
	}
	
	
	public SelectMetadata sqlDocSection(int key ,UserData ud ,List<Integer> statusList ,String textSearch,boolean fullEq) throws InvalidParameterException {
		
		String dialect = JPA.getUtil().getDbVendorName();
		
		Map<String, Object> params = new HashMap<>();
		
		StringBuilder select = new StringBuilder();
		StringBuilder from = new StringBuilder();
		StringBuilder where = new StringBuilder();
		
		params.put("id",ud.getUserAccess());
				
		select.append(" select D.DOC_ID a0, "+DocDAO.formRnDocSelect("D.", dialect)+" a1 , D.DOC_DATE a2,  D.DOC_VID a3, D.DOC_TYPE a4, ");
		select.append(DialectConstructor.limitBigString(dialect, "D.OTNOSNO", 300) + " a5, ");
		select.append(" D.CODE_REF_CORRESP a6 ");
		
		if(key != DocuConstants.CODE_ZNACHENIE_DASHBOARD_NONREG) {
			select.append(", CASE WHEN D.DOC_TYPE = 1 THEN null ELSE "); 
			select.append(DialectConstructor.convertToDelimitedString(dialect, "DR.CODE_REF", "DOC_REFERENTS DR where DR.DOC_ID = D.DOC_ID and DR.ROLE_REF = "+DocuConstants.CODE_ZNACHENIE_DOC_REF_ROLE_AUTHOR	, "DR.PORED"));
			select.append(" END a7 ");
			
		} 
		
		from  .append(" from TASK T join TASK_REFERENTS TR on TR.TASK_ID = T.TASK_ID ");
		from  .append(" join DOC D on D.DOC_ID = T.DOC_ID "); 
		where .append(" where TR.CODE_REF = :id and T.STATUS in (:stat) and T.TASK_TYPE = :taskType");
		
		if(textSearch!=null && !textSearch.trim().isEmpty()) {
			if(fullEq) {
				where .append(" AND (upper(D.RN_DOC) LIKE :textSearch OR upper(T.RN_TASK)  =:textSearch) ");
				params.put("textSearch",textSearch.trim().toUpperCase());
			} else {	
				where .append(" AND (upper(D.RN_DOC) LIKE :textSearch OR upper(T.RN_TASK)  LIKE :textSearch) ");
				params.put("textSearch","%"+textSearch.trim().toUpperCase()+"%");
			}
		}
		
		switch (key) {
			
			case DocuConstants.CODE_ZNACHENIE_DASHBOARD_FOR_REZOL:  //За резолюция
				select.append(" ,T.TASK_ID a8, T.RN_TASK a9, T.SROK_DATE a10, T.CODE_ASSIGN a11 , D.COMPETENCE a12 , D.URGENT a13 "); // полета от задачите
				params.put("taskType",DocuConstants.CODE_ZNACHENIE_TASK_TYPE_REZOL);
				params.put("stat",statusList);
			break;
			
			case DocuConstants.CODE_ZNACHENIE_DASHBOARD_FOR_SAGL:   //За съгласуване
				select.append(" ,T.TASK_ID a8, T.RN_TASK a9, T.SROK_DATE a10, T.CODE_ASSIGN a11 , D.URGENT a12 , D.SIGN_METHOD a13 ");// полета от задачите
				params.put("taskType",DocuConstants.CODE_ZNACHENIE_TASK_TYPE_SAGL);
				params.put("stat",statusList);
			break;
			
			case DocuConstants.CODE_ZNACHENIE_DASHBOARD_FOR_PODPIS: //За подпис
				select.append(" ,T.TASK_ID a8, T.RN_TASK a9, T.SROK_DATE a10, T.CODE_ASSIGN a11, D.URGENT a12 , D.SIGN_METHOD a13 "); // полета от задачите
				params.put("taskType",DocuConstants.CODE_ZNACHENIE_TASK_TYPE_PODPIS);
				params.put("stat",statusList);
			break;
			
			
			
			case DocuConstants.CODE_ZNACHENIE_DASHBOARD_NONREG:     //Нерегистрирани работни
				
				select.append(" , D.URGENT a7 "); 
				
				from   = new StringBuilder("");
				where  = new StringBuilder();
				
				
				from  .append(" from DOC D ");
				//where .append(" where D.USER_REG = :id and D.WORK_OFF_ID is null and D.FOR_REG_ID is null and D.PROCESSED = :prcess and D.DOC_TYPE=:dType");
				
				where .append(" where D.WORK_OFF_ID is null and D.FOR_REG_ID is null and D.PROCESSED = :prcess and D.DOC_TYPE=:dType and REGISTRATURA_ID = :regId");
				where .append(" and exists (select dr.ID from DOC_REFERENTS dr where dr.DOC_ID = D.DOC_ID and dr.CODE_REF = :id and dr.ROLE_REF = :roleRef "); 
				where .append(" union all ");
				where .append(" select tr.ID from TASK t inner join TASK_REFERENTS tr on tr.TASK_ID = t.TASK_ID  where t.DOC_ID = D.DOC_ID and tr.CODE_REF = :id and t.TASK_TYPE in (:taskType) ");
				where .append( " ) ");
				
				
				params.put("prcess",DocuConstants.CODE_ZNACHENIE_NE);
				params.put("dType", DocuConstants.CODE_ZNACHENIE_DOC_TYPE_WRK);
				params.put("roleRef", DocuConstants.CODE_ZNACHENIE_DOC_REF_ROLE_AUTHOR);
//				params.put("stat",statusList); // не се искат статусите на задачи
				params.put("taskType",Arrays.asList(DocuConstants.CODE_ZNACHENIE_TASK_TYPE_SAGL,DocuConstants.CODE_ZNACHENIE_TASK_TYPE_PODPIS));
				params.put("regId",ud.getRegistratura() != null ? ud.getRegistratura() : Integer.MIN_VALUE); // за мигрирани потребители може да няма регистратура и това ще гърми
				
				if(textSearch!=null && !textSearch.isEmpty()) {
					
					if(fullEq) {
						where .append(" AND upper(D.RN_DOC) =:textSearch ");
						params.put("textSearch",textSearch.trim().toUpperCase());
					} else {
						where .append(" AND upper(D.RN_DOC) LIKE :textSearch ");
						params.put("textSearch","%"+textSearch.trim().toUpperCase()+"%");
					}
				}
			break;
		}
		
		
		
		SelectMetadata sm = new SelectMetadata();
		
		sm.setSqlCount(" select count(*) " + from.toString() + where.toString());
		sm.setSql(select.toString() + from.toString() + where.toString());
		sm.setSqlParameters(params);
		
		return sm;
	}
	
	
	public SelectMetadata sqlDocZapoznavane(Integer userId ,String textSearch ,boolean fullEq) throws InvalidParameterException {
		
		String dialect = JPA.getUtil().getDbVendorName();
		
		Map<String, Object> params = new HashMap<>();
		
		StringBuilder select = new StringBuilder();
		StringBuilder from = new StringBuilder();
		StringBuilder where = new StringBuilder();
		
		params.put("ID",userId);
		params.put("MT",DocuConstants.CODE_ZNACHENIE_NOTIFF_EVENTS_DOC_ACCESS);
		params.put("RD",DocuConstants.CODE_ZNACHENIE_NE);
				
		select.append(" select D.DOC_ID a0, "+DocDAO.formRnDocSelect("D.", dialect)+" a1 , D.DOC_DATE a2,  D.DOC_VID a3, D.DOC_TYPE a4, ");
		select.append(DialectConstructor.limitBigString(dialect, "D.OTNOSNO", 300) + " a5, ");
		select.append(" D.CODE_REF_CORRESP a6, ");
		select.append(" CASE WHEN D.DOC_TYPE = 1 THEN null ELSE "); 
		select.append(DialectConstructor.convertToDelimitedString(dialect, "DR.CODE_REF", "DOC_REFERENTS DR where DR.DOC_ID = D.DOC_ID and DR.ROLE_REF = "+DocuConstants.CODE_ZNACHENIE_DOC_REF_ROLE_AUTHOR	, "DR.PORED"));
		select.append(" END a7, ");
				
//		if (dialect.indexOf("ORACLE") != -1) {
//			select.append(" TO_CHAR(USER_NOTIFICATIONS.DETAILS) a8 ");
//		} else {
			select.append(" USER_NOTIFICATIONS.DETAILS a8 ");
//		}	
		select.append(" , USER_NOTIFICATIONS.ID a9 ");
		
		select.append(" ,  D.REGISTRATURA_ID b10 ");
		
		select.append(" , D.URGENT c11 "); 
		
		from  .append(" from DOC D join USER_NOTIFICATIONS on D.DOC_ID = USER_NOTIFICATIONS.OBJECT_ID ");
		
		where .append(" where USER_ID = :ID and MESSAGE_TYPE = :MT and READ = :RD ");
		
		
		
		if(textSearch!=null && !textSearch.isEmpty()) {
			if(fullEq) {
				where .append(" AND upper(D.RN_DOC) =:textSearch ");
				params.put("textSearch",textSearch.trim().toUpperCase());
			} else {
				where .append(" AND upper(D.RN_DOC) LIKE :textSearch ");
				params.put("textSearch","%"+textSearch.trim().toUpperCase()+"%");
			}
		}
		
		
		SelectMetadata sm = new SelectMetadata();
		
		sm.setSqlCount(" select count(*) " + from.toString() + where.toString());
		sm.setSql(select.toString() + from.toString() + where.toString());
		sm.setSqlParameters(params);
		
		return sm;
	}
	
	
	
	
	public SelectMetadata sqlDelovodSection(int key, Integer idRegistrature ,String textSearch, boolean fullEq) throws InvalidParameterException {
		
		String dialect = JPA.getUtil().getDbVendorName();
		
		Map<String, Object> params = new HashMap<>();
		
		StringBuilder select = new StringBuilder();
		StringBuilder from = new StringBuilder();
		StringBuilder where = new StringBuilder();
		
				
		select.append(" select D.DOC_ID a0, "+DocDAO.formRnDocSelect("D.", dialect)+" a1 , D.DOC_DATE a2,  D.DOC_VID a3, D.DOC_TYPE a4, ");
		select.append(DialectConstructor.limitBigString(dialect, "D.OTNOSNO", 300) + " a5, ");
		select.append(" D.CODE_REF_CORRESP a6, ");
		select.append(" CASE WHEN D.DOC_TYPE = 1 THEN null ELSE "); 
		select.append(DialectConstructor.convertToDelimitedString(dialect, "DR.CODE_REF", "DOC_REFERENTS DR where DR.DOC_ID = D.DOC_ID and DR.ROLE_REF = "+DocuConstants.CODE_ZNACHENIE_DOC_REF_ROLE_AUTHOR	, "DR.PORED"));
		select.append(" END a7 ");
		
		from  .append(" from DOC D ");
		
		
		switch (key) {
			
			case DocuConstants.CODE_ZNACHENIE_DASHBOARD_DOC_REG_OF:  //Регистриране от работни
				select.append(" , D.URGENT a8 , D.SIGN_METHOD a9 "); 
				where .append(" where D.DOC_TYPE =  :TIP and D.WORK_OFF_ID is null and D.FOR_REG_ID = :IDR");
				params.put("TIP",DocuConstants.CODE_ZNACHENIE_DOC_TYPE_WRK);
				params.put("IDR",idRegistrature);
			break;
			
			case DocuConstants.CODE_ZNACHENIE_DASHBOARD_DOC_DIF_REG:   //От друга регистратура	
				select.append(" , doc_dvij.id as a8, doc_dvij.DVIJ_TEXT а9 ,other.REGISTRATURA_ID a10 , D.URGENT a11");
				from.append(" join doc_dvij on d.doc_id = doc_dvij.doc_id ");
				from.append(" left outer join DOC other on other.DOC_ID = doc_dvij.DOC_ID ");
				where .append(" where doc_dvij.FOR_REG_ID = :IDR and doc_dvij.status = :STAT and D.REGISTRATURA_ID <> :IDR");
				params.put("STAT",DocuConstants.DS_WAIT_REGISTRATION);
				params.put("IDR",idRegistrature);
			break;
			
			case DocuConstants.CODE_ZNACHENIE_DASHBOARD_DOC_NAS: //Входящи за насочване	
				select.append(" , D.URGENT a8 "); 
				where .append(" where DOC_TYPE = :TIP and PROCESSED = :DANE and REGISTRATURA_ID = :IDR");
				params.put("TIP",DocuConstants.CODE_ZNACHENIE_DOC_TYPE_IN);
				params.put("DANE",DocuConstants.CODE_ZNACHENIE_NE);
				params.put("IDR",idRegistrature);
			break;	
			
			
			case DocuConstants.CODE_ZNACHENIE_DASHBOARD_DOC_COMPETENCE: //по компетентност		
				select.append(" , D.COMPETENCE_TEXT a8 , D.URGENT a9 ");
				where .append(" where D.COMPETENCE = :COMP and REGISTRATURA_ID = :IDR ");
				params.put("COMP",DocuConstants.CODE_ZNACHENIE_COMPETENCE_FOR_SEND);
				params.put("IDR",idRegistrature);
				
			break;
			
		}
		
		if(textSearch!=null && !textSearch.trim().isEmpty()) {
			if(fullEq) {
				where .append(" AND upper(D.RN_DOC) =:textSearch ");
				params.put("textSearch",textSearch.trim().toUpperCase());
			} else {
				where .append(" AND upper(D.RN_DOC) LIKE :textSearch ");
				params.put("textSearch","%"+textSearch.trim().toUpperCase()+"%");
			}
		}
		
		SelectMetadata sm = new SelectMetadata();
		
		sm.setSqlCount(" select count(*) " + from.toString() + where.toString());
		sm.setSql(select.toString() + from.toString() + where.toString());
		sm.setSqlParameters(params);
		
		return sm;
	}
	
	
	
	
	
	
	
	/**
	 * Извлича отностно на дукомент 
	 *
	 * @param idDoc - идентификатор на документ
	 *
	 * @return
	 * @throws DbErrorException
	 */
	public String decodeDocOtnostno(Integer idDoc) throws DbErrorException  {
		try {
			String dialect = JPA.getUtil().getDbVendorName();
			
			String otnosno = " D.OTNOSNO ";
			
			if (dialect.indexOf("ORACLE") != -1) {
				 otnosno = " TO_CHAR(D.OTNOSNO) ";
				
			}
			
			Query query = JPA.getUtil().getEntityManager().createNativeQuery("SELECT "+otnosno+" FROM DOC D WHERE D.DOC_ID =:idDoc");			
			query.setParameter("idDoc", idDoc);
			
			return (String) query.getSingleResult();
			
		} catch (Exception e) {
			LOGGER.error("Грешки при извличане на отностно на документ !", e);
			throw new DbErrorException("Грешки при извличане на отностно на документ!", e);
		}
		
	}
	
	/**
	 * Търсене на док,дело,задача по номер спрямо правата. Дава резултат от вида:<br>
	 * [0]-OBJ_ID<br>
	 * [1]-RN_OBJ<br>
	 * [2]-OBJ_DATE<br>
	 * [3]-TIP - 1-док,2-дело,3-задача<br>
	 * [4]-EDIT_MODE = 1-актуализация,0-преглед<br>
	 * [5]-TASK_DOC_ID - ID на документа в задачата<br>
	 * [6]-OBJ_TYPE тип/вид обект, който се разкодира с класификациите
	 * 				(CODE_CLASSIF_DOC_TYPE = 129; CODE_CLASSIF_DELO_TYPE = 130; CODE_CLASSIF_TASK_VID = 105)<br>
	 * @param rn
	 * @param userData
	 * @return
	 * @throws DbErrorException
	 */
	@SuppressWarnings("unchecked")
	public List<Object[]> searchObjectsByRN(String rn, UserData userData) throws DbErrorException {
		final boolean menuDocEdit 	= userData.hasAccess(Constants.CODE_CLASSIF_MENU, 65);
		final boolean menuDeloEdit 	= userData.hasAccess(Constants.CODE_CLASSIF_MENU, 50);
		final boolean menuTaskEdit 	= userData.hasAccess(Constants.CODE_CLASSIF_MENU, 86);
		
		final boolean fullView 			= userData.hasAccess(DocuConstants.CODE_CLASSIF_DEF_PRAVA, DocuConstants.CODE_ZNACHENIE_DEF_PRAVA_FULL_VIEW);
		final boolean fullDocDeloEdit 	= userData.hasAccess(DocuConstants.CODE_CLASSIF_DEF_PRAVA, DocuConstants.CODE_ZNACHENIE_DEF_PRAVA_DOC_DELO_FULL_EDIT);
		final boolean fullTaskEdit 		= userData.hasAccess(DocuConstants.CODE_CLASSIF_DEF_PRAVA, DocuConstants.CODE_ZNACHENIE_DEF_PRAVA_TASK_FULL_EDIT);

		// ако има пълен достъп, значи има достъп за ПРЕГЛЕД до всички документи и дела в позволените регистратури
		Set<Integer> objaccessDocDelo = (fullView || fullDocDeloEdit) 
				&& userData.getAccessValues().containsKey(DocuConstants.CODE_CLASSIF_REGISTRATURI_OBJACCESS) 
				? userData.getAccessValues().get(DocuConstants.CODE_CLASSIF_REGISTRATURI_OBJACCESS).keySet() 
				: null;
		
		StringBuilder sql = new StringBuilder();
		

		boolean addRegistraturaParam = false;
		
		// ДОКУМЕНТИ tip=1
		sql.append(" select distinct d.DOC_ID OBJ_ID, d.RN_DOC RN_OBJ, d.DOC_DATE OBJ_DATE, 1 TIP ");
	
		if (menuDocEdit) { // има достъп до меню актуализация
			sql.append(" , case when d.REGISTRATURA_ID = " + userData.getRegistratura());
		
			if (!fullDocDeloEdit) {
				sql.append(" and (daden.CODE_REF in ("+userData.getUserAccess()+","+userData.getZveno()+")) ");
			}
			sql.append(" then 1 else 0 end EDIT_MODE ");
			
		} else { // само преглед може, без значение какво друго ще се определи
			sql.append(" , 0 EDIT_MODE ");
		}
		sql.append(" , -100 TASK_DOC_ID, d.DOC_TYPE OBJ_TYPE, d.PORED_DELO from DOC d ");
		
		sql.append(" left outer join DOC_ACCESS_ALL daden on daden.DOC_ID = d.DOC_ID and ");
		if (userData.getAccessZvenoList() == null) {
			sql.append(" daden.CODE_REF in (:userAccess,:zveno) "); // личен достъп за човека
		} else { // личен + на подчинените защото е шеф
			sql.append(" ( daden.CODE_REF in (:userAccess,:zveno) or daden.CODE_STRUCT in ("+userData.getAccessZvenoList()+")) ");
		}
		
		if (userData.isDocAccessDenied()) { // има маркирано че е с отнет и трябва да се изключат тези документи
			sql.append(" left outer join DOC_ACCESS otnet on otnet.DOC_ID = d.DOC_ID and otnet.CODE_REF = :userAccess and otnet.EXCLUDE = 1 ");
		}
		
		sql.append(" where upper(d.RN_DOC) = :rn and ( daden.DOC_ID is not null ");
		if (objaccessDocDelo != null) { // + пълен достъп за в регистратури като ако са в други ще се определи за преглед
			sql.append(" or d.REGISTRATURA_ID in (:objaccessDocDelo)) ");
		} else { // или само oбщодостъпни в регистратурата
			addRegistraturaParam = true;
			sql.append(" or (d.FREE_ACCESS = 1 and d.REGISTRATURA_ID = :registratura)) "); 
		}
		if (userData.isDocAccessDenied()) {
			sql.append(" and otnet.ID is null ");
		}		
		sql.append(" union all ");
		
		
		String poredDelo = "POSTGRESQL".equals(JPA.getUtil().getDbVendorName()) ? "cast(null as int8)" : "null";

		// ПРЕПИСКИ tip=2
		sql.append(" select distinct d.DELO_ID OBJ_ID, d.RN_DELO RN_OBJ, d.DELO_DATE OBJ_DATE, 2 TIP ");
		if (menuDeloEdit) { // има достъп до меню актуализация
			sql.append(" , case when d.REGISTRATURA_ID = " + userData.getRegistratura());
		
			if (!fullDocDeloEdit) {
				sql.append(" and (daden.CODE_REF in ("+userData.getUserAccess()+","+userData.getZveno()+")) ");
			}
			sql.append(" then 1 else 0 end EDIT_MODE ");
			
		} else { // само преглед може, без значение какво друго ще се определи
			sql.append(" , 0 EDIT_MODE ");
		}
		sql.append(" , -100 TASK_DOC_ID, d.DELO_TYPE OBJ_TYPE, "+poredDelo+" PORED_DELO from DELO d ");
		
		sql.append(" left outer join DELO_ACCESS_ALL daden on daden.DELO_ID = d.DELO_ID and ");
		if (userData.getAccessZvenoList() == null) {
			sql.append(" daden.CODE_REF in (:userAccess,:zveno) "); // личен достъп за човека
		} else { // личен + на подчинените защото е шеф
			sql.append(" ( daden.CODE_REF in (:userAccess,:zveno) or daden.CODE_STRUCT in ("+userData.getAccessZvenoList()+")) ");
		}
		
		if (userData.isDeloAccessDenied()) { // има маркирано че е с отнет и трябва да се изключат тези преписки
			sql.append(" left outer join DELO_ACCESS otnet on otnet.DELO_ID = d.DELO_ID and otnet.CODE_REF = :userAccess and otnet.EXCLUDE = 1 ");
		}
		
		sql.append(" where upper(d.RN_DELO) = :rn and ( daden.DELO_ID is not null ");
		if (objaccessDocDelo != null) { // + пълен достъп в регистратури като ако са в други ще се определи за преглед
			sql.append(" or d.REGISTRATURA_ID in (:objaccessDocDelo)) ");
		} else { // или само oбщодостъпни в регистратурата
			addRegistraturaParam = true;
			sql.append(" or (d.FREE_ACCESS = 1 and d.REGISTRATURA_ID = :registratura)) "); 
		}
		if (userData.isDeloAccessDenied()) {
			sql.append(" and otnet.ID is null ");
		}		
		sql.append(" union all ");
		

		// TODO не е реализирано:
		// - преглед на задачите в позволените му регистратури за достъп до обекти. ако има DEF_PRAVA_FULL_VIEW или PRAVA_TASK_FULL_EDIT
		// - преглед на задачи на мои подчинени ако е шеф, което е при поискване с чекбокс
		
		// ЗАДАЧИ tip=3
		sql.append(" select t.TASK_ID OBJ_ID, t.RN_TASK RN_OBJ, t.ASSIGN_DATE OBJ_DATE, 3 TIP ");
		if (menuTaskEdit) {
			sql.append(" , 1 EDIT_MODE ");
		} else { // няма достъп до актуализация без значение какво друго има
			sql.append(" , 0 EDIT_MODE ");
		}
		sql.append(" , t.DOC_ID TASK_DOC_ID, t.TASK_TYPE OBJ_TYPE, "+poredDelo+" PORED_DELO from TASK t ");
		sql.append(" where upper(t.RN_TASK) = :rn ");
		sql.append(" and ( t.USER_REG = :userReg or t.CODE_ASSIGN = :userAccess or t.CODE_CONTROL = :userAccess ");
		sql.append(" or EXISTS (select tr.ID from TASK_REFERENTS tr where tr.TASK_ID = t.TASK_ID and tr.CODE_REF = :userAccess) ");
		if (fullTaskEdit) { // пълен достъп за актуализация в регистратурата
			addRegistraturaParam = true;
			sql.append(" or t.REGISTRATURA_ID = :registratura ");
		}
		sql.append(" ) ");
		
		
		
		sql.append(" order by 4, 1 desc ");
		

		List<Object[]> result = null;
		try {
			Query query = JPA.getUtil().getEntityManager().createNativeQuery(sql.toString())
				.setParameter("rn", rn.trim().toUpperCase())
				.setParameter("userAccess", userData.getUserAccess())
				.setParameter("userReg", userData.getUserId())
				.setParameter("zveno", userData.getZveno());
			if (addRegistraturaParam) {
				query.setParameter("registratura", userData.getRegistratura());
			}
			if (objaccessDocDelo != null) {
				query.setParameter("objaccessDocDelo", objaccessDocDelo);
			}

			result = query.getResultList();
			
			if (result.isEmpty()) {
				return result;
			}
			
			Map<Integer, Boolean> allowedEditDocTypes = userData.getAccessValues().get(DocuConstants.CODE_CLASSIF_DOC_TYPE);
			if (!fullDocDeloEdit && menuDocEdit && allowedEditDocTypes != null && !allowedEditDocTypes.isEmpty()) {
				// само ако няма пълен достъп и има досъп до меню актуализация, но има зададен достъп до конкретни типове документи
				// ако в резултата има такъв тип трябва да се смени безусловно на преглед
				for (Object[] row : result) {
					if (((Number)row[3]).intValue() == 1 
						&& !userData.hasAccess(DocuConstants.CODE_CLASSIF_DOC_TYPE, ((Number)row[6]).intValue())) {
						row[4] = 0; // само преглед може за този тип документ
					}
				}
			}
			 
//			if (SystemData.isDocPoredDeloGen()) { // TODO това сглобяване прави, така че след избор се сменя въведеното
//				for (Object[] row : result) {
//					if (row[7] != null) {
//						row[1] = row[1] + "#" + row[7];
//					}
//				}
//			}
		} catch (Exception e) {
			throw new DbErrorException("Грешка при Търсене на док,дело,задача по номер", e);
		}
		return result;
	}
}
