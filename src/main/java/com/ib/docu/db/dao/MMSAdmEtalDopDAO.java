package com.ib.docu.db.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.docu.db.dto.MMSAdmEtalDop;
import com.ib.system.ActiveUser;
import com.ib.system.db.AbstractDAO;
import com.ib.system.exceptions.DbErrorException;

/**
 * DAO for {@link MMSAdmEtalDop}
 *
 * @author dessy
 */
public class MMSAdmEtalDopDAO extends AbstractDAO<MMSAdmEtalDop> {

	EntityManager em;

	/**  */
	private static final Logger LOGGER = LoggerFactory.getLogger(MMSAdmEtalDopDAO.class);

	/** @param user */
	public MMSAdmEtalDopDAO(ActiveUser user) {
		super(MMSAdmEtalDop.class, user);
	}
	
	public List<MMSAdmEtalDop> findByIdObject(Integer codeObject) throws DbErrorException {

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("findByIdObject(codeObject={})", codeObject);
		}
		
		try {
			
			String sql = " select ed from MMSAdmEtalDop ed where ed.codeObject = :codeObject ORDER BY pored ";
			
			TypedQuery<MMSAdmEtalDop> q = getEntityManager().createQuery(sql, MMSAdmEtalDop.class)
					.setParameter("codeObject", codeObject);

			return q.getResultList();
		
		} catch (Exception e) {
			throw new DbErrorException("Грешка при търсене на допълнителни полета към обект", e);
		}
	}
	
}
