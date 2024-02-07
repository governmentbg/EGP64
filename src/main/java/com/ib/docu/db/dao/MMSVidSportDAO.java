package com.ib.docu.db.dao;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

//import com.ib.docu.db.dto.MMSVidSport;
import com.ib.docu.db.dto.MMSVidSportSC;
import com.ib.system.ActiveUser;
import com.ib.system.db.AbstractDAO;
import com.ib.system.exceptions.DbErrorException;

public class MMSVidSportDAO extends AbstractDAO<MMSVidSportSC>{
	public MMSVidSportDAO(Class<MMSVidSportSC> typeClass, ActiveUser user) {
		super(typeClass, user);
	}
	
	/** 
	 * Tърсене по Тип на обекта
	 * 
	 * @param Integer typeObj
	 * @return
	 * @throws DbErrorException
	 */
	@SuppressWarnings("unchecked")
	public List<MMSVidSportSC> findByTypeObject(Integer typeObj) throws DbErrorException{
		try {
			
			List<MMSVidSportSC> list = createQuery("select mmsC from MMSVidSportSC mmsC where mmsC.tipОbject = ?1").
					setParameter(1, typeObj).getResultList();
			
			if (list.isEmpty()) {
				return null;
			}
			return (List<MMSVidSportSC>)list;
			
		} catch (Exception e) {
			throw new DbErrorException("Грешка при търсене на Вид спорт по тип на обекта!", e);
		}

	}
	
	/**
	 * Връща списък със спортовете по зададени код и ИД на обект.
	 * @param objectType тип нз обекта
	 * @param objectId ид на обекта
	 * @return списък с ИД на спортовете от колоната vid_sport от таблицата mms_vid_sport
	 * @throws DbErrorException
	 */
	public List<Integer> findSportByTypeAndId(Integer objectType, Integer objectId) throws DbErrorException {
		try {
			
			List list = createNativeQuery("select vid_sport from mms_vid_sport where tip_object = :tipObject and id_object = :idObject")
				.setParameter("tipObject", objectType)
				.setParameter("idObject", objectId)
				.getResultList();
			
			if (list.isEmpty()) {
				return null;
			}
			return (List<Integer>) list.stream().map(i -> ((BigInteger) i).intValue()).collect(Collectors.toList());
			
		} catch (Exception e) {
			throw new DbErrorException("Грешка при търсене на Вид спорт по тип и ИД на обекта!", e);
		}
	}
	

}
