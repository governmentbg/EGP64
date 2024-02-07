package com.ib.docu.quartz;

import java.io.StringWriter;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.servlet.ServletContext;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.docu.db.dao.SystemJournalDAO;
import com.ib.docu.system.DocuConstants;
import com.ib.docu.system.SystemData;
import com.ib.docu.utils.RegisterExporter;
import com.ib.mms.iscipr.client.IRegisterManagementService;
import com.ib.mms.iscipr.client.ObjectFactory;
import com.ib.mms.iscipr.client.RegisterManagementService;
import com.ib.mms.iscipr.client.RequestDataISCIPR;
import com.ib.mms.iscipr.client.ServiceResultISCIPR;
import com.ib.system.db.JPA;
import com.ib.system.exceptions.BaseException;


public class SendRegistersJob implements Job {

	private static final Logger LOGGER = LoggerFactory.getLogger(SendMailJob.class);

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		
		//КОМЕНТИРАНО Е ЩЕ СЕ ПУСНЕ КОГАТО ВИСКЧО Е ЯСНО И ГОТОВО.
		
//		try {
//			ServletContext servletContext = (ServletContext) context.getScheduler().getContext().get("servletContext");
//			if (servletContext == null) {
//				LOGGER.info("********** servletcontext is null **********");
//				return;
//			}
//
//			SystemData sd = (SystemData) servletContext.getAttribute("systemData");
//
//			// в базата директно да си стоят за да не се налагат промени при деплой
//			
//			
//			URL url = new URL(sd.getSettingsValue("mms.orak.WS.url"));
//
//			RegisterManagementService service1 = new RegisterManagementService(url);
//	        IRegisterManagementService sPort = service1.getBasicHttpBindingIRegisterManagementService();
//			
//			GregorianCalendar c = new GregorianCalendar();
//			c.setTime(new Date());
//			c.add(Calendar.DAY_OF_MONTH, -1);
//			
//			
//			SystemJournalDAO d = new SystemJournalDAO();
//			
//			
//			//tuk sabirame danni za tova kakvo shte prashtame 
//			List<Integer> objectIds1 = d.getModifiedObjects(c.getTime(), DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORT_OBED);
//			//palnim go v podhodqsht vid
//			RequestDataISCIPR r=loadData(objectIds1, sd.getSettingsValue("mms.orak.WS.operationName"));
//			
//			//izprashtame go 
//			ServiceResultISCIPR response=sPort.registerRecordEntry(r);
//	        if (response.isHasError()) {
//	        	LOGGER.error("WS said:  ERRRORRRRR за следните обединения с ид-та:"+objectIds1.toString());	        	
//	        	LOGGER.error("Code: "+response.getErrorCode().getValue());
//	        	LOGGER.error("Message:" +response.getErrorMessage().getValue());
//	        	LOGGER.error("Data: "+response.getData().getValue());
//	        	
//				LOGGER.debug("Успешно изпратихме данни за обединения с ид-та:"+objectIds1.toString());
//			}
//	       
//	        
//	        
//			
//		} catch (Exception e) {
//			LOGGER.error("Възникна грешка при извличане на данни за изпращене!!!", e);
//			throw new JobExecutionException(e);
//		} finally {
//			JPA.getUtil().closeConnection();
//		}

	}
	
	private RequestDataISCIPR loadData(List<Integer> list, String operation) throws BaseException {
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

				ObjectFactory objectFactory = new ObjectFactory();

				r.setOperation(objectFactory.createRequestDataISCIPROperation(operation));
				// zaqvkata e gotova
				r.setArgument(objectFactory.createRequestDataISCIPRArgument(result));
			});

			return r;
		
	}

}
