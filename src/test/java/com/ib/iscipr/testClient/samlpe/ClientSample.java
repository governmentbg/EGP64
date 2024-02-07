package com.ib.iscipr.testClient.samlpe;

import java.io.StringWriter;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.Test;

import com.ib.docu.db.dao.MMSSportObedMFDAO;
import com.ib.docu.db.dao.MMSVpisvaneDAO;
import com.ib.docu.db.dao.MMSsportObedinenieDAO;
import com.ib.docu.db.dao.ReferentDAO;
import com.ib.docu.db.dto.MMSSportObedMf;
import com.ib.docu.db.dto.MMSSportnoObedinenie;
import com.ib.docu.db.dto.MMSVpisvane;
import com.ib.docu.db.dto.Referent;
import com.ib.docu.system.DocuConstants;
import com.ib.docu.system.SystemData;
import com.ib.docu.utils.RegisterExporter;
import com.ib.iscipr.testClient.IRegisterManagementService;
import com.ib.iscipr.testClient.ObjectFactory;
import com.ib.iscipr.testClient.RegisterManagementService;
import com.ib.iscipr.testClient.RequestDataISCIPR;
import com.ib.iscipr.testClient.ServiceResultISCIPR;
import com.ib.iscipr.testClient.samlpe.SportUnionsRequest.SportUnionsRecord;
import com.ib.system.ActiveUser;
import com.ib.system.db.JPA;
import com.ib.system.exceptions.BaseException;
 


public class ClientSample {
	/**
	externalId				ИД на спортно обединение;
	sportUnionType			Вид на спортното обединение;
	sportUnionRegNumber		Рег. номер на спортно обединение;
	licenceNumber			Номер на лиценз;
	regNumberEntryOrder		Рег. номер на заповедта за вписване;
	entryOrderDate			Дата на заповедта за вписване;
	unionName				Наименование;
	unionLocation			Седалище – населено място 
	unionAddress			адрес на управление;
	unionStatus				Статус
	unionStatusDate			Дата на статус
	sport					Вид спорт;
	presenter				Представляващ;
	addressCorespondant		Адрес на референт
	telephone				Телефони;
	emails					Имейли;
	eik						ЕИК;
	webPage					Web-страница;
	foreignData				международни спортни организации в които членува;
	cancelationOrder		Заповед за прекратяване/отнемане на лиценз;
	cancelDate"				Дата на отнемане
	 * @throws DatatypeConfigurationException 
	 * @throws BaseException 
	 * @throws MalformedURLException 

    **/

	@Test
	public void testWsCreateEdit() throws JAXBException, DatatypeConfigurationException, BaseException, MalformedURLException {
	        System.out.println("***********************");
	        System.out.println("Create Web Service Client...");
	        RegisterManagementService service1 = new RegisterManagementService();
	       
	        System.out.println("Create Web Service...");
	        IRegisterManagementService port1 = service1.getBasicHttpBindingIRegisterManagementService();
	        System.out.println("Call Web Service Operation...");
	        
	   
//	        Service.create(new URL("http://corporate.orak365.net/SmartRegistryAPI/RegisterManagementService.svc?singleWsdl"),new QName("http://iscipr.egov.bg/", "RegisterManagementService"))
//	        .createDispatch(new QName("http://iscipr.egov.bg/", "BasicHttpBinding_IRegisterManagementService"), IRegisterManagementService.class, Service.Mode.MESSAGE)
//	        .getRequestContext().put("javax.xml.ws.client.connectionTimeout", "360000");
	       
	        //RequestDataISCIPR r=fillDataSingleEntry(118); //118
	        
	        List<Integer> list= new ArrayList<Integer>();
	        for (int i = 0; i < 150; i++) {
	        	list.add(i+1);	
			}
			
			
			RequestDataISCIPR r=fillDataMultyEntry(list);
 
			
			
	        ObjectFactory of=new ObjectFactory();
//	        CallContext callContext=new CallContext();
//	        callContext.setEmployeeNames(of.createCallContextEmployeeNames("Imena Slujitel"));
//	        callContext.setServiceURI(of.createCallContextEmployeeNames("незная"));
//	        callContext.setServiceType(of.createCallContextEmployeeNames("незная"));
//	        callContext.setEmployeeIdentifier(of.createCallContextEmployeeNames("1"));
//	        callContext.setEmployeePosition(of.createCallContextEmployeeNames("незная"));
//	        callContext.setLawReason(of.createCallContextEmployeeNames("незная"));
//	        callContext.setRemark(of.createCallContextEmployeeNames("незная"));
//	        callContext.setAdministrationName(of.createCallContextEmployeeNames("незная"));
//	        
//	        
//	        
//	        r.setCallContext(new ObjectFactory().createCallContext(callContext));
	        
	        ServiceResultISCIPR response=port1.registerRecordEntry(r);
	        if (response.isHasError()) {
	        	System.out.println("Server said:  ERRRORRRRR:");
			}
	        System.out.println("Server said: ");
	        System.out.println("Code: "+response.getErrorCode().getValue());
	        System.out.println("Message:" +response.getErrorMessage().getValue());
	        System.out.println("Data: "+response.getData().getValue());
	        
	        System.out.println("***********************");
	        System.out.println("Call Over!");
	}
	


	@Test
	public void testWsDeleteFirst100Ids() throws JAXBException, DatatypeConfigurationException, BaseException, MalformedURLException {
	
//		Service								service;
//		IRegisterManagementService	sPort;
		URL url = new URL("http://corporate.orak365.net/SmartRegistryAPI/RegisterManagementService.svc?singleWsdl");
//		QName qname = new QName("http://iscipr.egov.bg/", "RegisterManagementService");
////		http://wsmf.delo.indexbg.com
//		service = Service.create(url, qname);
//		
//	       
//        System.out.println("Create Web Service...");
//		// Hello helloPort = service.getHelloPort();
//        service = (RegisterManagementService) service;
//        sPort=service.getPort(IRegisterManagementService.class);
	       
        
        RegisterManagementService service1 = new RegisterManagementService(url);
        IRegisterManagementService sPort = service1.getBasicHttpBindingIRegisterManagementService();
	        
	       
	        RequestDataISCIPR r=new RequestDataISCIPR();
 
	        com.ib.iscipr.testClient.samlpe.ObjectFactory oFactoryClient=new com.ib.iscipr.testClient.samlpe.ObjectFactory();
	        SportUnionsRequest sr=new SportUnionsRequest();
	        
	        for (int i = 0; i < 100; i++) {
	        	SportUnionsRecord record=oFactoryClient.createSportUnionsRequestSportUnionsRecord();
	        	record.setExternalId(""+(i+1));	
	        	sr.getSportUnionsRecord().add(record);
			}
	        
	        StringWriter writer = new StringWriter();

	    	
			try {
				Marshaller marshaller = JAXBContext.newInstance(SportUnionsRequest.class).createMarshaller();
			
			
				marshaller.marshal(sr, writer);
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			String result = writer.toString();
	    	
			
			System.out.println(result);
	        
			
	        ObjectFactory of=new ObjectFactory();
	        r.setOperation(of.createRequestDataISCIPROperation("SPORT.SportUnions.SportEntry"));
	        // zaqvkata e gotova
	        r.setArgument(of.createRequestDataISCIPRArgument(result));
	        
	        ServiceResultISCIPR response=sPort.registerRecordRemove(r);
	        
	        if (response.isHasError()) {
	        	System.out.println("Server said:  ERRRORRRRR:");
			}
	        System.out.println("***********************");
	        System.out.println("Code: "+response.getErrorCode().getValue());
	        System.out.println("Message:" +response.getErrorMessage().getValue());
	        System.out.println("Data: "+response.getData().getValue());
	        
	        System.out.println("***********************");
	        System.out.println("Call Over!");
	}
	
	
	
	private static XMLGregorianCalendar toGregorianCalendar(Date date) throws DatatypeConfigurationException  {
		if (date == null) {
			return null;
		}
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(date);

		return DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);
	}
	
	@Test
	public void fillDataCall() {
		fillDataSingleEntry(118);
	}
	
	 
	public RequestDataISCIPR fillDataSingleEntry(int id){
		RequestDataISCIPR r=new RequestDataISCIPR();
		
		try {
			
			JPA.getUtil().runWithClose(() -> {
			ObjectFactory objectFactory=new ObjectFactory();
		       
			com.ib.iscipr.testClient.samlpe.ObjectFactory oFactoryClient=new com.ib.iscipr.testClient.samlpe.ObjectFactory();
			SimpleDateFormat sdf=new SimpleDateFormat("dd.MM.yyyy");
			SystemData sd=new SystemData();
			
	        MMSSportnoObedinenie obedinenie=new MMSsportObedinenieDAO(MMSSportnoObedinenie.class, ActiveUser.DEFAULT).findById(id);
	        Referent referent=new ReferentDAO(ActiveUser.DEFAULT).findByCodeRef(obedinenie.getIdObject());
	        List<MMSSportObedMf> mfList	=new MMSSportObedMFDAO(ActiveUser.DEFAULT).findByIdSportnoObed(obedinenie.getId());
	        String mf="";
	        for (int i = 0; i < mfList.size(); i++) {
	        	mf+=sd.decodeItem(DocuConstants.CODE_CLASSIF_MMS_MEJD_FED, mfList.get(i).getMejdFed(), 1, new Date())
	        			+ " от: "+sdf.format(mfList.get(i).getDateBeg());
	        	if(mfList.get(i).getDateEnd()!=null) {
	        		mf+=" до: "+sdf.format(mfList.get(i).getDateEnd());
	        	}
	        	mf+="; ";
			}
	        
	        List<MMSVpisvane> vpisList = new MMSVpisvaneDAO(ActiveUser.DEFAULT).findRegsList(DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORT_OBED, obedinenie.getId());
	        int maxIdindex=-1;
	        for (int i = 0; i < vpisList.size(); i++) {
				if (maxIdindex<0) {
					maxIdindex=i;
				}else{
					if (vpisList.get(maxIdindex).getId()<vpisList.get(i).getId()) {
						maxIdindex=i;
					}
				};
			}
	        MMSVpisvane vpisvane=vpisList.get(maxIdindex);
	        
	        
			String selectedVidSportTxt="";
			for (int i = 0 ; i < obedinenie.getVidSportList().size() ; i++) {
				if (selectedVidSportTxt.length()>0) {
					selectedVidSportTxt+=", "+sd.decodeItem(DocuConstants.CODE_CLASSIF_VIDOVE_SPORT, obedinenie.getVidSportList().get(i).getVidSport(), 1, new Date());
				}else {
					selectedVidSportTxt+=sd.decodeItem(DocuConstants.CODE_CLASSIF_VIDOVE_SPORT, obedinenie.getVidSportList().get(i).getVidSport(), 1, new Date());
				}
			}
	        
	        
	        
	      
	        
	        // palnim dannite 
	        SportUnionsRecord record=oFactoryClient.createSportUnionsRequestSportUnionsRecord();
	        record.setExternalId(obedinenie.getId().toString());
	        record.setSportUnionType(sd.decodeItem(DocuConstants.CODE_CLASSIF_VID_SPORT_OBEDINENIE, obedinenie.getVid(), 1, new Date()));  
	        record.setSportUnionRegNumber(obedinenie.getRegNomer());
	      
	        
	        
	        
	        record.setUnionName(referent.getRefName());  

	        record.setUnionLocation(sd.decodeItem(DocuConstants.CODE_CLASSIF_EKATTE, referent.getAddress().getEkatte(), 1, new Date()));  
	        
	        record.setUnionAddress(referent.getAddress().getAddrText());  
	        record.setUnionStatus(sd.decodeItem(DocuConstants.CODE_CLASSIF_STATUS_OBEKT, obedinenie.getStatus(), 1, new Date()));  
	        try {
				record.setUnionStatusDate(toGregorianCalendar(obedinenie.getDateStatus()));
			} catch (DatatypeConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        record.setPresenter(obedinenie.getPredstavitelstvo());
	        record.setSport(selectedVidSportTxt);
	        record.setTelephone(referent.getContactPhone()); //
	        record.setEmails(referent.getContactEmail());
	        record.setEIK(new BigInteger(referent.getNflEik()));
	        record.setWebPage(referent.getWebPage());
	        
	        record.setForeignData(mf); 
	        
	        if (vpisvane.getStatusVpisvane()==1) {
	        	record.setLicenceNumber(vpisvane.getRnDocLicenz()); 
		        record.setRegNumberEntryOrder(vpisvane.getRnDocVpisvane());  
		        try {
					record.setEntryOrderDate(toGregorianCalendar(vpisvane.getDateDocVpisvane()));
				} catch (DatatypeConfigurationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}  
			}else {
				if (vpisvane.getStatusVpisvane()==2 || vpisvane.getStatusVpisvane()==3) {
					record.setCancelationOrder(vpisvane.getRnDocVpisvane());
			        try {
						record.setCancelDate(toGregorianCalendar(vpisvane.getDateDocVpisvane()));
					} catch (DatatypeConfigurationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}		
				}
			}
	        
	        
	        
	         
	        
	        //dobavqme gi v request classa
	        SportUnionsRequest sr=new SportUnionsRequest();
	        sr.getSportUnionsRecord().add(record);
	        
	        
	        //ot klasa pravim xml
		        
			StringWriter writer = new StringWriter();

	    	
			try {
				Marshaller marshaller = JAXBContext.newInstance(SportUnionsRequest.class).createMarshaller();
			
			
				marshaller.marshal(sr, writer);
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			String result = writer.toString();
	    	
			
			System.out.println(result);
			
			//slagame go v argument
//			Argument argument=new Argument();
////	        argument.setAny(objectFactory.createAnyType(result));
//	        argument.setAny(result);
//	        
	        r.setOperation(objectFactory.createRequestDataISCIPROperation("SPORT.SportUnions.SportEntry"));
	        // zaqvkata e gotova
	        r.setArgument(objectFactory.createRequestDataISCIPRArgument(result));
	         
			});
		
		} catch (BaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
		 return r;
	}
	
	@Test
	public void fillDataMultyCheck() throws BaseException {
		List<Integer> list = new ArrayList<Integer>();
		for (int i = 0; i < 10; i++) {
			list.add(i + 1);
		}
		fillDataMultyEntry(list);
	}
	
	 
	
	@Test
	public RequestDataISCIPR fillDataMultyEntry(List<Integer> list) throws BaseException {
		RequestDataISCIPR r = new RequestDataISCIPR();
		
		JPA.getUtil().runWithClose(() -> {
			List<com.ib.mms.iscipr.client.xsd.SportUnionsRequest.SportUnionsRecord> selectObedinenia = new RegisterExporter()
					.selectObedinenia(new SystemData(), list);

			com.ib.mms.iscipr.client.xsd.SportUnionsRequest sr = new com.ib.mms.iscipr.client.xsd.SportUnionsRequest();
			sr.getSportUnionsRecord().addAll(selectObedinenia);

			// ot klasa pravim xml

			StringWriter writer = new StringWriter();

			try {
				Marshaller marshaller = JAXBContext.newInstance(com.ib.mms.iscipr.client.xsd.SportUnionsRequest.class)
						.createMarshaller();

				marshaller.marshal(sr, writer);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			String result = writer.toString();

			System.out.println(result);
			ObjectFactory objectFactory = new ObjectFactory();

			r.setOperation(objectFactory.createRequestDataISCIPROperation("SPORT.SportUnions.SportEntry"));
			// zaqvkata e gotova
			r.setArgument(objectFactory.createRequestDataISCIPRArgument(result));
		});

		return r;
	}
	
}
