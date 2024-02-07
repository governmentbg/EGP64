package com.ib.docu.db.dao;

import java.util.List;

import javax.persistence.Query;
import javax.persistence.TypedQuery;

import com.ib.docu.db.dto.MMSChlenstvo;
import com.ib.docu.system.DocuConstants;
import com.ib.system.ActiveUser;
import com.ib.system.db.AbstractDAO;
import com.ib.system.db.JPA;


public class MMSChlenstvoDAO extends AbstractDAO<MMSChlenstvo> {

	public MMSChlenstvoDAO(Class<MMSChlenstvo> typeClass, ActiveUser user) {
		super(typeClass, user);
	}
	
	
	
	public List<MMSChlenstvo> findByIdObject(Integer idObj, Integer typeObj) {
		
		String sql = " select m from MMSChlenstvo m where m.idObject = :idObj and m.typeObject = :typeObj";
		TypedQuery<MMSChlenstvo> q = JPA.getUtil().getEntityManager().createQuery(sql, MMSChlenstvo.class);
		q.setParameter("idObj", idObj);
		q.setParameter("typeObj", typeObj);
		
		return q.getResultList();
	}
	
	public int deleteByIdObject(Integer idObj, Integer typeObj) {
		Query q =  JPA.getUtil().getEntityManager().createQuery(" delete from MMSChlenstvo where idObject = :idObj and typeObject = :typeObj");
		q.setParameter("idObj", idObj);
		q.setParameter("typeObj", typeObj);
		return q.executeUpdate();
	}
	/**
	 * @Svilen
	 * Използва се само за извличане на членствата на формирования във обединение
	 * @param idObed
	 * @return
	 */
	public List<Object[]> findChlenstvaFormirovaniqVObedinenie(Integer idObed) {
		
		StringBuffer SQL = new StringBuffer();
			SQL.append(" SELECT distinct ");
			SQL.append("    c.id, ");
			SQL.append("    f.vid, ");
			SQL.append("    r.ref_name, ");
			SQL.append("    c.date_acceptance, ");
			SQL.append("    c.date_termination, ");
			SQL.append("    f.reg_nomer, ");
			SQL.append("    f.date_reg, ");
			SQL.append("    e.oblast ");
			SQL.append("    , f.id fid ");
			SQL.append(" FROM ");
			SQL.append("    mms_chlenstvo c, ");
			SQL.append("    mms_sport_formirovanie f, ");
			SQL.append("    adm_referents r, ");
			SQL.append("    adm_ref_addrs ra, ");
			SQL.append("    ekatte_att e ");
			SQL.append(" WHERE ");
			SQL.append("    c.id_object=f.id ");
			SQL.append(" AND c.type_object= "+DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_FORMATIONS);
			SQL.append(" AND c.type_vish_object= "+DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORT_OBED);
			SQL.append(" AND c.id_vish_object=:idObed");
			SQL.append(" AND r.code=f.id_object");
			SQL.append(" and ra.code_ref=r.code");
			SQL.append(" and e.ekatte=ra.ekatte");
			

		 Query q = JPA.getUtil().getEntityManager().createNativeQuery(SQL.toString());
		q.setParameter("idObed", idObed);
		System.out.println(SQL.toString());
		return q.getResultList();
	}
	

}
