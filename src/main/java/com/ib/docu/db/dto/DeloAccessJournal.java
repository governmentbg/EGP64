package com.ib.docu.db.dto;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.JAXBException;

import com.ib.docu.system.DocuConstants;
import com.ib.system.db.AuditExt;
import com.ib.system.db.JournalAttr;
import com.ib.system.db.dto.SystemJournal;
import com.ib.system.exceptions.DbErrorException;
import com.ib.system.utils.JAXBHelper;

public class DeloAccessJournal implements AuditExt{
	@JournalAttr(label="persons",defaultText = "Изричен достъп")
	private List<DeloAccess> persons=new ArrayList<DeloAccess>();

	private Integer idDelo;
	
	private String identObject;
	
	public List<DeloAccess> getPersons() {
		return persons;
	}

	public void setPersons(List<DeloAccess> persons) {
		this.persons = persons;
	}

	@Override
	public SystemJournal toSystemJournal() throws DbErrorException {
		SystemJournal dj = new SystemJournal();
		dj.setCodeAction(DocuConstants.CODE_DEIN_KOREKCIA);
		dj.setCodeObject(DocuConstants.CODE_ZNACHENIE_JOURNAL_IZR_DOST_DELO);
		dj.setDateAction(new Date());
		dj.setIdentObject(identObject);
		dj.setIdObject(idDelo);
		dj.setJoinedCodeObject1(DocuConstants.CODE_ZNACHENIE_JOURNAL_DELO);
		dj.setJoinedIdObject1(idDelo);
		
		try {
			String objectXml = JAXBHelper.objectToXml(this, true);
			dj.setObjectXml(objectXml);
		} catch (JAXBException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		return dj;
	}

	

	public String getIdentObject() {
		return identObject;
	}

	public void setIdentObject(String identObject) {
		this.identObject = identObject;
	}

	public Integer getIdDelo() {
		return idDelo;
	}

	public void setIdDelo(Integer idDelo) {
		this.idDelo = idDelo;
	}
	
	
}
