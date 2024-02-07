package com.ib.docu.db.dao;

import java.util.List;

import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.docu.db.dto.MMSChlenstvo;
import com.ib.docu.db.dto.MMSSportObedMf;
import com.ib.system.ActiveUser;
import com.ib.system.db.AbstractDAO;
import com.ib.system.db.JPA;


public class MMSSportObedMFDAO extends AbstractDAO<MMSSportObedMf> {

	private static final Logger LOGGER = LoggerFactory.getLogger(MMSSportObedMFDAO.class);
	
	public MMSSportObedMFDAO(ActiveUser user) {
		super(user);
	}
	
	public List<MMSSportObedMf> findByIdSportnoObed(Integer idObed) {
		
		String sql = " select mf from MMSSportObedMf mf where mf.idSportObed = :idObed";
		TypedQuery<MMSSportObedMf> q = JPA.getUtil().getEntityManager().createQuery(sql, MMSSportObedMf.class);
		q.setParameter("idObed", idObed);
		
		return q.getResultList();
	}
	
	public void deleteByIdSpObed(Integer idObed) {
		
		String sql = " delete from MMSSportObedMf mf where mf.idSportObed = :idObed";
		Query q = JPA.getUtil().getEntityManager().createQuery(sql);
		q.setParameter("idObed", idObed);
		
		q.executeUpdate();
	}

	 
}
