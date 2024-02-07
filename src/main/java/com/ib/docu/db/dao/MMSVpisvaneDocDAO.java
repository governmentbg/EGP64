package com.ib.docu.db.dao;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.docu.db.dto.MMSVpisvaneDoc;
import com.ib.docu.system.DocuConstants;
import com.ib.system.ActiveUser;
import com.ib.system.db.AbstractDAO;
import com.ib.system.db.JPA;
import com.ib.system.db.dao.FilesDAO;
import com.ib.system.db.dto.Files;
import com.ib.system.exceptions.DbErrorException;
import com.ib.system.exceptions.ObjectInUseException;

/**
 * DAO for {@link MMSVpisvaneDoc}
 *
 * @author dessy
 */
public class MMSVpisvaneDocDAO extends AbstractDAO<MMSVpisvaneDoc> {

	/**  */
	private static final Logger LOGGER = LoggerFactory.getLogger(MMSVpisvaneDocDAO.class);

	/** @param user */
	public MMSVpisvaneDocDAO(ActiveUser user) {
		super(MMSVpisvaneDoc.class, user);
	}	
	
	/**
	 * изтрива и данните за док само ако не се използва в други вписвания
	 */
	@Override
	public void delete(MMSVpisvaneDoc entity) throws DbErrorException, ObjectInUseException {
		super.delete(entity);
		
		if (entity.getIdDoc() == null) {
			return; // няма док и няма какво да се прави повече
		}
		
		// това с документите трябва да е след като се изтрие връзката !!!
		
		// doc, files, file_objects
		boolean deleteDoc = false;
		try {
			@SuppressWarnings("unchecked")
			List<Object> rows = createNativeQuery("select id from mms_vpisvane_doc where id_doc = ?1").setParameter(1, entity.getIdDoc()).setMaxResults(1).getResultList();
			deleteDoc = rows.isEmpty();
		} catch (Exception e) {
			throw new DbErrorException("Грешка при търсене на данни за документ!", e);
		}
		
		if (deleteDoc) {
			DocDAO docDao = new DocDAO(getUser());
			FilesDAO filesDao = new FilesDAO(getUser());

			docDao.deleteById(entity.getIdDoc());
			
			List<Files> listFiles = filesDao.selectByFileObject(entity.getIdDoc(), DocuConstants.CODE_ZNACHENIE_JOURNAL_DOC);
			for (Files delme : listFiles) {
				filesDao.deleteFileObject(delme);
			}
		}
	}

	/**
	 * Връща всички документи по ид на вписване
	 *
	 * @param idReg
	 * @return
	 * @throws DbErrorException
	 */
	@SuppressWarnings("unchecked")
	public List<Object[]>  findDocsList(Integer idVpisvane) throws DbErrorException {
		
		try {
			
			String sql = " SELECT VD.ID, VD.ID_DOC, D.DOC_VID, D.DOC_TYPE, D.RN_DOC, D.DOC_DATE, D.OTNOSNO "
					+ " FROM MMS_VPISVANE_DOC VD "
					+ " LEFT OUTER JOIN DOC D ON VD.ID_DOC = D.DOC_ID "
					+ "	WHERE VD.ID_VPISVANE = :IDV "
					+ " ORDER BY VD.ID DESC";			
			
			Query q = JPA.getUtil().getEntityManager().createNativeQuery(sql);
			q.setParameter("IDV", idVpisvane);
			
			return (ArrayList<Object[]>) q.getResultList();
		
		} catch (Exception e) {
			LOGGER.debug("Грешка при извличане на документи по ид на вписване", e);
			throw new DbErrorException("Грешка при извличане на документи по ид на вписване - " + e.getLocalizedMessage(), e);
		}
	}	
	
	/**
	 * Метод за намиране на ид на вписване_док по ид на вписване и ид на документ
	 * 
	 * @param idReg
	 * @return
	 */
	public Integer findByIdReg(Integer idReg, Integer idDoc) {
		
		Query q =  JPA.getUtil().getEntityManager().createQuery(" SELECT VD.id FROM MMSVpisvaneDoc VD WHERE VD.idVpisvane = :idReg AND VD.idDoc = :idDoc ");
		q.setParameter("idReg", idReg);	
		q.setParameter("idDoc", idDoc);
			
		@SuppressWarnings("unchecked")
		List<Integer> list = q.getResultList();
			
			if (list.isEmpty()) {
				return null;
			}
			
			return list.get(0);
		
	}
}
