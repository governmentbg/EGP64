package com.ib.docu.db.dao;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ib.docu.db.dto.MMSChlenstvo;
import com.ib.docu.db.dto.MMSCoaches;
import com.ib.docu.db.dto.MMSSportnoObedinenie;
import com.ib.docu.db.dto.MMSVidSport;
import com.ib.docu.db.dto.MMSVidSportSC;
import com.ib.docu.db.dto.MMSVidSportSF;
import com.ib.docu.db.dto.MMSVidSportSO;
import com.ib.docu.db.dto.MMSVpisvane;
import com.ib.docu.db.dto.MMSsportFormirovanie;
import com.ib.docu.system.DocuConstants;
import com.ib.docu.system.SystemData;
import com.ib.indexui.pagination.LazyDataModelSQL2Array;
import com.ib.system.ActiveUser;
import com.ib.system.db.JPA;
import com.ib.system.exceptions.BaseException;
import com.ib.system.exceptions.DbErrorException;
import com.ib.system.utils.DateUtils;

public class TestMMSsportFormirovanie {
	
	private static SystemData sd;
	private static MMSsportFormirovanieDAO mmsSFDAO;
	private static MMSVpisvaneDAO mmsVpisDAO;
	private static MMSChlenstvoDAO mmsChelDAO;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		sd = new SystemData();
		mmsSFDAO = new MMSsportFormirovanieDAO(MMSsportFormirovanie.class, ActiveUser.DEFAULT);
		mmsVpisDAO = new MMSVpisvaneDAO(ActiveUser.DEFAULT);
		mmsChelDAO =  new MMSChlenstvoDAO(MMSChlenstvo.class, ActiveUser.DEFAULT);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void test() {
		try {
			
			//Referent ref = JPA.getUtil().getEntityManager().find(Referent.class, Integer.valueOf(1471));
			//System.out.println(ref.getAddress());
			
			//MMSsportFormirovanie tmp = JPA.getUtil().getEntityManager().find(MMSsportFormirovanie.class, Integer.valueOf(1));
			//System.out.println(tmp.getMmsChlenList().size());
			//MMSsportFormirovanie tmp = mmsSFDAO.findById(Integer.valueOf(1));
			MMSsportFormirovanie tmp = mmsSFDAO.findByIdObject(Integer.valueOf(1947));
			System.out.println(tmp);
			//tmp.getVidSportList().remove(0);
			/*
			MMSVidSport e = new MMSVidSport();
			e.setTipОbject(DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_FORMATIONS);
			e.setIdObject(Integer.valueOf(1));
			e.setVidSport(Integer.valueOf(2));
			e.setUserReg(Integer.valueOf(-1));
			e.setDateReg(new Date());
			tmp.getVidSportList().add(e);
			
			JPA.getUtil().runInTransaction(() ->  mmsSFDAO.save(tmp));
			System.out.println(tmp.getPredsedatel());
			*/
			/*SelectMetadata tmp = mmsVpisDAO.findDocsList(1, DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_FORMATIONS);
			System.out.println(tmp.getSql());
			LazyDataModelSQL2Array tmpL = new LazyDataModelSQL2Array(tmp, "");
			System.out.println(tmpL.getRowCount());
			System.out.println(tmpL.getResult().size());
			/*for (Object[] objects : tmp) {
				for (int i = 0; i < objects.length; i++) {
					Object object = objects[i];
					System.out.println(object);
				}
				
			}*/
			
			
		} catch (Exception e) {
			
			e.printStackTrace();
		}
	}
	
	@Test
	public void testFindChlenstvo() {
		LazyDataModelSQL2Array tmp;
		try {
			tmp = new LazyDataModelSQL2Array( new MMSsportObedinenieDAO(MMSSportnoObedinenie.class, ActiveUser.DEFAULT).findChlenstvoByIdRefferent(1171, true, null) , " reg_nomer asc " );
			System.out.println(tmp.getRowCount());
		} catch (DbErrorException e) {
			e.printStackTrace();
		}
		
	}
	
	@Test
	public void testMMSSFDAO() {
		List<MMSChlenstvo> tmp = mmsSFDAO.findByIdObject(1, DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_FORMATIONS);
		System.out.println("broi zapisi -> " + tmp.size());
		
		try {
			JPA.getUtil().runInTransaction(() -> System.out.println(" iztriti: " +  mmsSFDAO.deleteByIdObject(1, DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_FORMATIONS)));
		} catch (BaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	@Test
	public void testSaveChlenstvo() {
		try {
			MMSChlenstvo e = new MMSChlenstvo();
			e = JPA.getUtil().getEntityManager().find(MMSChlenstvo.class, Integer.valueOf(1));
			System.out.println(e);
			if(e == null)
				e = new MMSChlenstvo();
			e.setTypeObject(96);
			e.setIdObject(1);
			e.setTypeVishObject(DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORT_OBED);
			e.setIdVishObject(1);
			e.setUserReg(-1);
			e.setDateReg(new Date());
		
			JPA.getUtil().begin();
			e = mmsChelDAO.save(e);
			JPA.getUtil().commit();
			System.out.println(e.getId());
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	@Test
	public void testVidSportEntity() {
		try {
			MMSsportFormirovanie tmp = mmsSFDAO.findById(Integer.valueOf(1));
			System.out.println("broj vid Sport: " + tmp.getVidSportList().size());
			
			/*MMSVidSportSF e = new MMSVidSportSF();
			e.setTipОbject(DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_FORMATIONS);
			e.setIdObject(Integer.valueOf(1));
			e.setVidSport(Integer.valueOf(2));
			e.setUserReg(Integer.valueOf(-1));
			e.setDateReg(new Date());
			tmp.getVidSportList().add(e);
			
			JPA.getUtil().runInTransaction(() ->  mmsSFDAO.save(tmp));
			System.out.println(tmp.getPredsedatel());*/
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testSaveVidSportEntityFromSO() {
		try {
			//MMSsportFormirovanie tmp = mmsSFDAO.findById(Integer.valueOf(1));
			MMSSportnoObedinenie tmp = new MMSsportObedinenieDAO(MMSSportnoObedinenie.class, ActiveUser.DEFAULT).findById(Integer.valueOf(1));
			System.out.println(tmp.getVidSportList().size());
			
			MMSVidSportSO e = new MMSVidSportSO();
			e.setTipОbject(DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORT_OBED);
			e.setIdObject(Integer.valueOf(1));
			e.setVidSport(Integer.valueOf(1));
			e.setUserReg(Integer.valueOf(-1));
			e.setDateReg(new Date());
			tmp.getVidSportList().add(e);
			
			JPA.getUtil().runInTransaction(() ->  new MMSsportObedinenieDAO(MMSSportnoObedinenie.class, ActiveUser.DEFAULT).save(tmp));
			System.out.println(tmp.getPredsedatel());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testSaveVidSportCoaches() {
		try {
			MMSCoaches tmp = new MMSCoachesDAO(MMSCoaches.class, ActiveUser.DEFAULT).findById(Integer.valueOf(1));
//			System.out.println(tmp.getVidSportList().size());
			
			MMSVidSportSC e = new MMSVidSportSC();
			e.setTipОbject(DocuConstants.CODE_ZNACHENIE_JOURNAL_COACHES);
			e.setIdObject(Integer.valueOf(1));
			e.setVidSport(Integer.valueOf(2));
			e.setUserReg(Integer.valueOf(-1));
			e.setDateReg(new Date());
//			tmp.getVidSportList().add(e);
			
			JPA.getUtil().runInTransaction(() ->  new MMSCoachesDAO(MMSCoaches.class, ActiveUser.DEFAULT).save(tmp));
			System.out.println(tmp.getId());
			
		} catch (DbErrorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BaseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	
	LazyDataModelSQL2Array tmp = null;

	List<MMSVpisvane> tmpV = new ArrayList<MMSVpisvane>();
	@Test
	public void findVpisvane() {
		
		try {		
			/*JPA.getUtil().runWithClose(() -> tmp = new LazyDataModelSQL2Array( new MMSVpisvaneDAO(ActiveUser.DEFAULT).findRegsListNativeSMD(
					DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_FORMATIONS, Integer.valueOf(1)), " RN_DOC_ZAIAVLENIE asc "));
			//tmpVpis = Arrays.sort( tmp.getResult(), Comparator.comparing(null));
			System.out.println(tmp.getResult().size() );
			for (int i = 0 ; i < tmp.getResult().size() ; i ++) {
				Object[] tmpO = tmp.getResult().get(i);
				for (int j = 0; j < tmpO.length; j++) {
					System.out.println(tmpO[j]);
				}
			}*/
			JPA.getUtil().runWithClose(() -> tmpV =  new MMSVpisvaneDAO(ActiveUser.DEFAULT).findRegsList(DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_FORMATIONS, Integer.valueOf(1)));
			for (MMSVpisvane vp : tmpV) {
				System.out.println("id:" + vp.getId() +"rnDoc:" + vp.getRnDocLicenz() + "dateReg:" + vp.getDateReg());
			}
			MMSVpisvane lastVpisvane = tmpV.get(tmpV.size() - 1);
			if(lastVpisvane.getRnDocResult() != null && !lastVpisvane.getRnDocResult().trim().isEmpty())
				System.out.println("otkaz");
			if(lastVpisvane.getRnDocZaiavlenie() != null && !lastVpisvane.getRnDocZaiavlenie().trim().isEmpty())
				System.out.println("регистриран");
		} catch (BaseException e) {
			e.printStackTrace();
		}
	}
	
	List<MMSsportFormirovanie> tmpSF = new ArrayList<MMSsportFormirovanie>();
	
	@Test
	public void testfindByRegNom() {
		try {
			JPA.getUtil().runWithClose(() -> tmpSF =  mmsSFDAO.findByRegNom("101-018"));
			if(tmpSF.size() == 0)
				System.out.println("praznoQ");
		} catch (BaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (MMSsportFormirovanie vp : tmpSF) {
			System.out.println("id:" + vp.getId() +"rnDoc:" + vp.getRegNomer() + "dateReg:" + vp.getDateReg());
		}
		
	}
	
	
	@Test
	public void testParseDate() {
		try {
		String regNomDate = "2019-06-05 00:00:00.0";
		Date date1=new SimpleDateFormat("yyyy-MM-dd HH:mm:sss").parse(regNomDate);  
	    System.out.println(regNomDate+"\t"+date1);  
		} catch( java.text.ParseException e) {
			e.printStackTrace();
		}
	}
}
