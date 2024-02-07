package com.ib.docu.db.dao;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;
import javax.persistence.TypedQuery;

import com.ib.docu.db.dto.MMSDopPol;
import com.ib.system.ActiveUser;
import com.ib.system.db.AbstractDAO;
import com.ib.system.db.JPA;
import com.ib.system.exceptions.DbErrorException;
import com.ib.system.utils.SearchUtils;


/**
 * DAO for {@link MMSDopPol}
 *
 */
public class MMSDopPolDAO extends AbstractDAO<MMSDopPol> {

	
	/** @param user */
	public MMSDopPolDAO(ActiveUser user) {
		super(MMSDopPol.class, user);
	}
	
	
	/**
	 * Заявка за допълнителните полета към обект
	 * @param idObj
	 * @param codeObj
	 * @return
	 */
	@SuppressWarnings({"unchecked" })
	public ArrayList<Object[]> findByIdObjAndCode(Integer idObj,Integer codeObj) throws DbErrorException {

		try {
			
			String sql = " SELECT DP.ID AS A0, DP.ZN_KOD A1, DP.ZN_DATE A2, DP.ZN_STR A3, ED.ID A4, ED.IME_POLE A5, ED.CLASIF A6, ED.TIP_POLE A7, ED.PORED A8, ED.POVT A9 "
					+ " from MMS_DOP_POL DP LEFT OUTER JOIN MMS_ADM_ETAL_DOP ED on (DP.ID_POLE = ED.ID) "
					+ " WHERE DP.ID_OBEKT = :idObekt AND DP.CODE_OBJECT = :codeObject "
					+ " ORDER BY ED.PORED ";
			
			Query query = JPA.getUtil().getEntityManager().createNativeQuery(sql);
			query.setParameter("idObekt", idObj);
			query.setParameter("codeObject", codeObj);

			return (ArrayList<Object[]>) query.getResultList();
		
		} catch (Exception e) {
			throw new DbErrorException("Грешка при търсене на допълнителни полета към обект", e);
		}
	}
		
	/**
	 * Заявка за търсене на записи по id на поле
	 * @param idPole
	 * @return
	 */
	public List<MMSDopPol> findByIdPole(Integer idPole) throws DbErrorException {

		try {
			
           String sql = " select dp from MMSDopPol dp where dp.idPole = :idPole ";
			
			TypedQuery<MMSDopPol> query = getEntityManager().createQuery(sql, MMSDopPol.class)
					.setParameter("idPole", idPole);

			return query.getResultList();
		
		} catch (Exception e) {
			throw new DbErrorException("Грешка при търсене на допълнителни полета по id на поле", e);
		}
	}
	
	
}
