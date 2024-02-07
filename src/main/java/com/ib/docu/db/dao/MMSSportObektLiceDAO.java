package com.ib.docu.db.dao;

import javax.persistence.Query;

import com.ib.docu.db.dto.MMSSportObekt;
import com.ib.docu.db.dto.MMSSportObektLice;
import com.ib.system.ActiveUser;
import com.ib.system.db.AbstractDAO;
import com.ib.system.db.JPA;
import com.ib.system.exceptions.DbErrorException;

public class MMSSportObektLiceDAO extends AbstractDAO<MMSSportObektLice > {
	
	/** @param user */
	public MMSSportObektLiceDAO(ActiveUser user) {
		super(MMSSportObektLice.class, user);
	}
	
	public MMSSportObektLiceDAO(Class<MMSSportObektLice> typeClass, ActiveUser user) {
		super(typeClass, user);
	}
	
	/**
	 *  Изтриване на лице за връзка от таблица mms_sport_obekt_lice
	 *  @param id - id на записа, който се изтрива
	 * @param idObj -  - ID на лице
	 * @param idSpObekt  - ID на спортен обект
	 * @return
	 */
		public int deleteFromSpObLiceTbl(Integer id, Integer idObj, Integer idSpObekt) throws DbErrorException {
			      if (id == null && idObj == null && idSpObekt == null)  return 0; 
	        
			      Query q = null;  
					if  (id != null) {
					    q =  JPA.getUtil().getEntityManager().createNativeQuery(" delete from mms_sport_obekt_lice  where id = :idz ");
					    q.setParameter("idz", id);
					} else { 
						if (idObj == null) {  // Изтриване на всички лица, свързани със спортния обект
							  q =  JPA.getUtil().getEntityManager().createNativeQuery(" delete from mms_sport_obekt_lice  where id_sport_obekt = :idObekt ");
							  q.setParameter("idObekt", idSpObekt);
						}  else {
							  q =  JPA.getUtil().getEntityManager().createNativeQuery(" delete from mms_sport_obekt_lice  where id_object = :idObj  and id_sport_obekt = :idObekt ");
							  q.setParameter("idObj", idObj);
							  q.setParameter("idObekt", idSpObekt);
						}	  
					}
					
					return q.executeUpdate();
				
		}	   
		
		
}
