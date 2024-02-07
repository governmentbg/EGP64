package com.ib.docu.experimental;

import java.util.Date;

import com.ib.docu.db.dto.Delo;
import com.ib.docu.db.dto.Doc;
import com.ib.docu.db.dto.Task;
import com.ib.docu.system.DocuConstants;
import com.ib.indexui.db.dto.AdmGroup;
import com.ib.indexui.system.Constants;
import com.ib.system.BaseObjectComparator;
import com.ib.system.BaseSystemData;
import com.ib.system.SysConstants;
import com.ib.system.db.JPA;
import com.ib.system.db.dto.SystemJournal;

public class ObjectComparator extends BaseObjectComparator {

	public ObjectComparator(Date oldDate, Date newDate, BaseSystemData sd) {
		super(oldDate, newDate, sd);
		// TODO Auto-generated constructor stub
	}
	
	public ObjectComparator(Date oldDate, Date newDate, BaseSystemData sd, Integer lang) {
		super(oldDate, newDate, sd, lang);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected String formatVal(Object o, String codeClassif, int codeObject, Date dat) {
		//System.out.println("CC=" + codeClassif);
		
		if (codeObject > 0) {
			String ident = null;
			try {				
				Integer id = Integer.parseInt(""+o);
				
				switch(codeObject) {
				  case DocuConstants.CODE_ZNACHENIE_JOURNAL_DELO:
				    Delo delo = JPA.getUtil().getEntityManager().find(Delo.class, id); // new DeloDAO(ActiveUser.DEFAULT).findById(id);
				    if (delo != null) {
				    	ident = delo.getIdentInfo();			    	
				    }
				    break;
				  case DocuConstants.CODE_ZNACHENIE_JOURNAL_DOC:
					    Doc doc = JPA.getUtil().getEntityManager().find(Doc.class, id); // new DocDAO(ActiveUser.DEFAULT).findById(id);
					    if (doc != null) {
					    	ident =  doc.getIdentInfo();			    	
					    }
					    break;
				  case DocuConstants.CODE_ZNACHENIE_JOURNAL_TASK:
					    Task task = JPA.getUtil().getEntityManager().find(Task.class, id);
					    if (task != null) {
					    	ident =  task.getIdentInfo();			    	
					    }
					    break;
				  case Constants.CODE_ZNACHENIE_JOURNAL_GROUPUSER:
					    AdmGroup group = JPA.getUtil().getEntityManager().find(AdmGroup.class, id);
					    if (group != null) {
					    	ident =  group.getIdentInfo();			    	
					    }
					    break;
				  default:
					  break;
				}
				
				if (ident == null) {
					//Правим още един опит през журнала
					SystemJournal j = (SystemJournal) JPA.getUtil().getEntityManager().createQuery("from SystemJournal where codeObject =:co and idObject = :io and codeAction = :ca")
							.setParameter("co", codeObject)
							.setParameter("io", id)
							.setParameter("ca", SysConstants.CODE_DEIN_IZTRIVANE)
							.getSingleResult();
					if (j != null) {
						ident = j.getIdentObject();
					}
				}
				
				if (ident == null) {
					return "Id= " + id;
				}else {
					return ident + "(Id= " +id + ")";
				}
				
			} catch (Exception e) {
				return ident + " (Грешка при идентификация)";
			}

			
		}else {
			if (codeClassif == null || codeClassif.equalsIgnoreCase("none")) {
				return fromatSimpleVal(o);
			}else {
				return decodeVal(o,codeClassif, dat, lang);
			}
		}
		
		
	}
	
	
	
	
}
	


