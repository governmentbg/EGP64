package com.ib.docu.db.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.docu.db.dto.DocDvij;
import com.ib.docu.system.DocuClassifAdapter;
import com.ib.docu.system.DocuConstants;
import com.ib.docu.system.SystemData;
import com.ib.docu.utils.DocDostUtils;
import com.ib.indexui.system.Constants;
import com.ib.system.ActiveUser;
import com.ib.system.SysConstants;
import com.ib.system.db.AbstractDAO;
import com.ib.system.db.JPA;
import com.ib.system.db.dto.SystemJournal;
import com.ib.system.exceptions.DbErrorException;
import com.ib.system.exceptions.ObjectInUseException;
import com.ib.system.utils.DateUtils;
import com.ib.system.utils.SearchUtils;

/**
 * DAO for {@link DocDvij}
 *
 * @author belev
 */
public class DocDvijDAO extends AbstractDAO<DocDvij> {

	/**  */
	private static final Logger LOGGER = LoggerFactory.getLogger(DocDvijDAO.class);
	
	/** @param user */
	public DocDvijDAO(ActiveUser user) {
		super(DocDvij.class, user);
	}
	
	/** */
	@Override
	public DocDvij findById(Object id) throws DbErrorException {
		DocDvij dvij = super.findById(id);
		if (dvij != null) {
			dvij.setDbCodeRef(dvij.getCodeRef());
		}
		return dvij;
	}
	
	@SuppressWarnings("unchecked")
	public List<DocDvij> getDocDvij(Integer docId) throws DbErrorException{
		// TODO ако се направи на пагинатион и на отваряне да се вика findById ще е по добре
		try {
			List<DocDvij> list = JPA.getUtil().getEntityManager().createQuery("from DocDvij where docId = :idd order by dvijDate").setParameter("idd", docId).getResultList();
			if (list.isEmpty()) {
				return list;
			}
			for (DocDvij dvij : list) { // не е много добре така, но не се вика финдБъИд на отваряне
				dvij.setDbCodeRef(dvij.getCodeRef());
			}
			return list;
		} catch (Exception e) {
			LOGGER.error("Грешка при извличане на движения на документ !");
			throw new DbErrorException("Грешка при извличане на движения на документ !",e);
		}
		
	}
	
	
	public DocDvij save(DocDvij dvij, SystemData systemData, Integer docRegId) throws DbErrorException {
		
		boolean isNew = false;
		if (dvij.getId() == null) {
			//Ново движение		
			if (dvij.getDvijMethod() != null) {				
				isNew = true;
				switch (dvij.getDvijMethod()) {
				
					case DocuConstants.CODE_ZNACHENIE_PREDAVANE_EMAIL:  
						dvij.setStatus(DocuConstants.DS_WAIT_SENDING);
						dvij.setStatusDate(new Date());
						break;
					
					case DocuConstants.CODE_ZNACHENIE_PREDAVANE_SSEV:  
						dvij.setStatus(DocuConstants.DS_WAIT_SENDING);
						dvij.setStatusDate(new Date());
						break;
					
					case DocuConstants.CODE_ZNACHENIE_PREDAVANE_SEOS:  
						dvij.setStatus(DocuConstants.DS_WAIT_SENDING);
						dvij.setStatusDate(new Date());
						break;					
					default : 
						dvij.setStatus(DocuConstants.DS_SENT);
						dvij.setStatusDate(new Date());						
				}
				
				if (dvij.getCodeRef() != null && docRegId != null) {
					Integer regId = (Integer) systemData.getItemSpecific(DocuConstants.CODE_CLASSIF_ADMIN_STR, dvij.getCodeRef(), DocuConstants.CODE_DEFAULT_LANG, dvij.getDvijDate(), DocuClassifAdapter.ADM_STRUCT_INDEX_REGISTRATURA);			
					if (regId != null && !regId.equals(docRegId) && dvij.getDvijMethod( ).equals(DocuConstants.CODE_ZNACHENIE_PREDAVANE_DRUGA_REGISTRATURA)){
						dvij.setStatus(DocuConstants.DS_WAIT_REGISTRATION);
						dvij.setForRegid(regId);
					}
				}
			}
		}		
		Integer oldCodeRef = dvij.getDbCodeRef(); // мерге го замазва
		
		dvij =  super.save(dvij);
		
		if (dvij.getCodeRef() != null // само ако има
			&& (isNew || !dvij.getCodeRef().equals(oldCodeRef)) // нов или промяна
			&& !Objects.equals(dvij.getDvijMethod(), DocuConstants.CODE_ZNACHENIE_PREDAVANE_DRUGA_REGISTRATURA) // без на друга регистратура
			&& systemData.matchClassifItems(Constants.CODE_CLASSIF_ADMIN_STR, dvij.getCodeRef(), dvij.getDvijDate())) { // само ако е от АДМ 

			Set<Integer> forInsert = Collections.singleton(dvij.getCodeRef());
			
			Set<Integer> forDelete = null;
			if (oldCodeRef != null) {
				forDelete = Collections.singleton(oldCodeRef);
			}

			new DocDostUtils().addRemoveDocDost(dvij.getDocId(), dvij.getCodeMainObject(), dvij.getId(), forDelete, forInsert , systemData);
		}
		dvij.setDbCodeRef(dvij.getCodeRef());

		return dvij;
	}
	
	
	/**
	 * @param dvij
	 * @param systemData
	 * @return ако има смяна в компететност на док връща новата стойност, иначе NULL
	 * @throws DbErrorException
	 * @throws ObjectInUseException
	 */
	public Integer delete(DocDvij dvij, SystemData systemData) throws DbErrorException, ObjectInUseException {
		
		if (dvij == null) {
			return null;
		}
		
		
		super.delete(dvij);
		
		
		if (dvij.getCodeRef() != null
			&& systemData.matchClassifItems(Constants.CODE_CLASSIF_ADMIN_STR, dvij.getCodeRef(), dvij.getDvijDate())) { // от АДМ
			
			Set<Integer> forDelete = Collections.singleton(dvij.getCodeRef());
			new DocDostUtils().addRemoveDocDost(dvij.getDocId(), dvij.getCodeMainObject(), dvij.getId(), forDelete, null , systemData);
		}
		Integer newCompetence = null;
		try {
			//Проверка компетентност
			Query q = JPA.getUtil().getEntityManager().createNativeQuery("select COMPETENCE, DVIJ_DATE, DOC.RN_DOC, DOC.DOC_DATE, DOC.PORED_DELO from DOC left outer join DOC_DVIJ on DOC.DOC_ID = DOC_DVIJ.DOC_ID where DOC.DOC_ID = :IDD");
			q.setParameter("IDD",dvij.getDocId());
			@SuppressWarnings("unchecked")
			ArrayList<Object[]> result = (ArrayList<Object[]>) q.getResultList();
			if (result.size() > 0) {
				Integer comp = SearchUtils.asInteger(result.get(0)[0]);
				if (result.get(0)[1] == null && comp != null && comp.equals(DocuConstants.CODE_ZNACHENIE_COMPETENCE_SENT) ) {
					//Няма значение с е изпратено по компетентност
					q = JPA.getUtil().getEntityManager().createNativeQuery("UPDATE DOC SET COMPETENCE = :COMP WHERE DOC_ID = :IDD");
					q.setParameter("COMP", DocuConstants.CODE_ZNACHENIE_COMPETENCE_FOR_SEND);
					q.setParameter("IDD",dvij.getDocId());
					q.executeUpdate();
					
					newCompetence = DocuConstants.CODE_ZNACHENIE_COMPETENCE_FOR_SEND;
					
					String docIdent = DocDAO.formRnDocDate(result.get(0)[2], result.get(0)[3], result.get(0)[4]);
					SystemJournal journal = new SystemJournal(DocuConstants.CODE_ZNACHENIE_JOURNAL_DOC, dvij.getDocId()
						, "Изтрито е предаване (Id="+dvij.getId()+") на документ "+docIdent+", с което е бил изпратен по компетентост и документът е върнат в състояние \"за изпращане по компетентност\"");

					journal.setCodeAction(SysConstants.CODE_DEIN_SYS_OKA);
					journal.setDateAction(new Date());
					journal.setIdUser(getUserId());

					saveAudit(journal);
				}
			}
			
		}catch (Exception e) {
			LOGGER.error("Грешка при изтриване на движение на документ !");
			throw new DbErrorException("Грешка при изтриване на движение на документ !",e);
		}
		
		return newCompetence;
	}
	
	/**
	 * при изтриване на движението трябва да се изтрие и достъп до каквото то е дало
	 */
	@Override
	protected void remove(DocDvij entity) throws DbErrorException, ObjectInUseException {
		try {
			// DOC_ACCESS_ALL
			if (entity.getCodeRef() != null) {
				int deleted = createNativeQuery("delete from DOC_ACCESS_ALL where OBJECT_CODE = ?1 and OBJECT_ID = ?2")
					.setParameter(1, DocuConstants.CODE_ZNACHENIE_JOURNAL_DOC_DVIJ).setParameter(2, entity.getId()).executeUpdate();
				LOGGER.debug("Изтрити са {} реда от DOC_ACCESS_ALL за движение с ID={}", deleted, entity.getId());
			}

		} catch (Exception e) {
			throw new DbErrorException("Грешка при изтриване на свързани обекти на движение!", e);
		}

		super.remove(entity);
	}

	/**
	 * Проверява дали документ вече е изпратен към друга регистратура със статус 'Чака регистрация' или 'Регистриран';
	 * @param docId - ИД на документа
	 * @param foreignRegId - ИД на другата регистратура
	 * @return true, ако е намерено друго предаване
	 * @throws DbErrorException 
	 */
	public boolean alreadySentToForeignReg(Integer docId, Integer foreignRegId) throws DbErrorException {
		String sql = "select count(d) from DocDvij d where dvijMethod = :dvijMethod and docId = :docId and forRegid = :regId and (status = :wait or status = :registered)";
		
		try {
			Query query = JPA.getUtil().getEntityManager().createQuery(sql)
				.setParameter("dvijMethod", DocuConstants.CODE_ZNACHENIE_PREDAVANE_DRUGA_REGISTRATURA)
				.setParameter("docId", docId)
				.setParameter("regId", foreignRegId)
				.setParameter("wait", DocuConstants.DS_WAIT_REGISTRATION)
				.setParameter("registered", DocuConstants.DS_REGISTERED);
		
			Long count =  (Long) query.getSingleResult();
			return (count != null && count > 0);
		} catch (Exception e) {
			LOGGER.error("Грешка при извличане на движения на документ !");
			throw new DbErrorException("Грешка при извличане на движения на документ !", e);
		}
		
	}
}