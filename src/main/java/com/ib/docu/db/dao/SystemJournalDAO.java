package com.ib.docu.db.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.Query;

import com.ib.docu.system.DocuConstants;
import com.ib.system.db.JPA;
import com.ib.system.exceptions.DbErrorException;

public class SystemJournalDAO {
	
	/**
	 * Търси в журнала всички редове за съответната дата за съответния код на обект
	 * и прави списък с ИД на всички обекти, които за деня са били създадени или редактирани.
	 * Ако обект е бил създаден/редактиран и в съшия ден изтрит, не връщам ИД-то му.
	 * 
	 * @param date
	 * @param codeObject
	 * @return списък с ИД-та на обектите
	 * @throws DbErrorException
	 */
	public List<Integer> getModifiedObjects(Date date, Integer codeObject) throws DbErrorException {
		
		Calendar[] startAndEndMinutes = getStartAndEndMinutes(date);
		
		// Това измъкнва всички записи в журнала за дадения ден,
		// в който има редактирани/чисто нови спортни бекти 
		// [0] id
		// [1] codeObject
		// [2] idObject
		// [3] codeAction
		String sql = "select s.id, s.codeObject, s.idObject, s.codeAction"
				+ " from SystemJournal s"
				+ " where (s.dateAction between :d1 and :d2)"
				+ " and s.codeAction in :actionCodes"
				+ " and s.codeObject = :objectCode";
		
		Query query = JPA.getUtil().getEntityManager().createQuery(sql);
		query.setParameter("d1", startAndEndMinutes[0].getTime());
		query.setParameter("d2", startAndEndMinutes[1].getTime());
		query.setParameter("actionCodes", Arrays.asList(
				DocuConstants.CODE_DEIN_ZAPIS,
				DocuConstants.CODE_DEIN_KOREKCIA,
				DocuConstants.CODE_DEIN_IZTRIVANE));
		query.setParameter("objectCode", codeObject);
		
		List<Object[]> allResults = query.getResultList();
		
		// Това намира редовете, където се е извършило изтриване на обект
		List<Integer> deletedResults = allResults.stream()
				.filter(j -> (Integer) j[3] == DocuConstants.CODE_DEIN_IZTRIVANE)
				.map(j -> (Integer) j[2])
				.collect(Collectors.toList());
		
		// От списъка с всички резултати изтривам изтритите обекти
		List<Object[]> filtered = allResults.stream()
				.filter(j -> !deletedResults.contains((Integer) j[2]))
				.collect(Collectors.toList());
		
		// От всеки ред взима само колоната ИД и ги прави на списък
		Set<Integer> filteredIdsOnly = filtered.stream()
				.map(j -> (Integer) j[2])
				.collect(Collectors.toSet());
	
		ArrayList<Integer> result = new ArrayList<>(filteredIdsOnly);
		result.sort((i1, i2) -> i1.compareTo(i2));
		return result;
		
	}
	
	/**
	 * Търси в журнала всички редове за съответната дата за съответния код на обект
	 * и прави списък с ИД на всички обекти, които за деня са били изтрити.
	 * 
	 * @param date
	 * @param codeObject
	 * @return списък с ИД-та на обектите
	 */
	public List<Integer> getDeletedObjects(Date date, Integer codeObject) {
		Calendar[] startAndEndMinutes = getStartAndEndMinutes(date);
		
		// Това измъкнва всички записи в журнала за дадения ден,
		// в който има затрити спортни бекти 
		// [0] id
		String sql = "select s.id"
				+ " from SystemJournal s"
				+ " where (s.dateAction between :d1 and :d2)"
				+ " and s.codeAction = :actionCode"
				+ " and s.codeObject = :objectCode";
		
		Query query = JPA.getUtil().getEntityManager().createQuery(sql);
		query.setParameter("d1", startAndEndMinutes[0].getTime());
		query.setParameter("d2", startAndEndMinutes[1].getTime());
		query.setParameter("actionCode", DocuConstants.CODE_DEIN_IZTRIVANE);
		query.setParameter("objectCode", codeObject);
		
		List<Integer> allResults = query.getResultList();
		
		
		return allResults;
	}
	
	/**
	 * Връща два календара, които сочат към подадената дата.
	 * [0] е с час 00:00:00:000
	 * [1] е с час 23:59:59:999
	 * 
	 * @param date
	 * @return
	 */
	private Calendar[] getStartAndEndMinutes(Date date) {
		Calendar c1 = Calendar.getInstance();
		c1.setTime(date);
		
		Calendar c2 = Calendar.getInstance();
		c2.setTime(date);
		
		c1.set(Calendar.HOUR_OF_DAY, 0);
		c1.set(Calendar.MINUTE, 0);
		c1.set(Calendar.SECOND, 0);
		c1.set(Calendar.MILLISECOND, 0);
		
		c2.set(Calendar.HOUR_OF_DAY, 23);
		c2.set(Calendar.MINUTE, 59);
		c2.set(Calendar.SECOND, 59);
		c2.set(Calendar.MILLISECOND, 999);
		
		return new Calendar[] { c1, c2 };
	}
}
