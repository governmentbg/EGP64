package com.ib.docu.beans;

import java.io.Serializable;
import java.util.Calendar;
import java.util.List;

import javax.inject.Named;

import org.omnifaces.cdi.ViewScoped;

import com.ib.docu.db.dao.SystemJournalDAO;
import com.ib.docu.system.DocuConstants;
import com.ib.docu.system.SystemData;
import com.ib.docu.utils.RegisterExporter;
import com.ib.indexui.system.IndexUIbean;
import com.ib.mms.iscipr.client.xsd.SportUnionsRequest.SportUnionsRecord;
import com.ib.system.exceptions.DbErrorException;

@Named
@ViewScoped
public class TestRegisterExporter extends IndexUIbean implements Serializable {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5741670506486510271L;
	

	public void test() throws DbErrorException {
		
		SystemJournalDAO d = new SystemJournalDAO();
		
		Calendar c = Calendar.getInstance();
		c.set(Calendar.DAY_OF_MONTH, 7);
		c.set(Calendar.MONTH, Calendar.SEPTEMBER);
		c.set(Calendar.YEAR, 2022);
		
		List<Integer> objectIds1 = d.getModifiedObjects(c.getTime(), DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORT_OBED);
		List<Integer> objectIds2 = d.getModifiedObjects(c.getTime(), DocuConstants.CODE_ZNACHENIE_JOURNAL_COACHES);
		List<Integer> objectIds3 = d.getModifiedObjects(c.getTime(), DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_FORMATIONS);
		List<Integer> objectIds4 = d.getModifiedObjects(c.getTime(), DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_OBJECTS);
		
		List<Integer> deletedObjectIds1 = d.getDeletedObjects(c.getTime(), DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORT_OBED);
		List<Integer> deletedObjectIds2 = d.getDeletedObjects(c.getTime(), DocuConstants.CODE_ZNACHENIE_JOURNAL_COACHES);
		List<Integer> deletedObjectIds3 = d.getDeletedObjects(c.getTime(), DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_FORMATIONS);
		List<Integer> deletedObjectIds4 = d.getDeletedObjects(c.getTime(), DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_OBJECTS);
		
		RegisterExporter exporter = new RegisterExporter();
		List<SportUnionsRecord> listObedinenia = exporter.selectObedinenia((SystemData) getSystemData(), objectIds1);
		List<String[]> listTreniori = exporter.selectTrenKadri((SystemData) getSystemData(), objectIds2, false);
		List<String[]> listFormiravania = exporter.selectFormirovania((SystemData) getSystemData(), objectIds3, false);
		List<String[]> listSportniObekti = exporter.selectSportniObekti((SystemData) getSystemData(), objectIds4, false);
		
		
		// TODO да се пращат към уеб услугата
		
	}

}
