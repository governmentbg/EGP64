package com.ib.docu.utils;

import static com.ib.docu.system.DocuConstants.CODE_ZNACHENIE_REF_TYPE_FZL;
//import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.ib.docu.db.dao.MMSCoachesDAO;
import com.ib.docu.db.dao.MMSSportObektDAO;
import com.ib.docu.db.dao.MMSVpisvaneDAO;
import com.ib.docu.db.dao.MMSsportFormirovanieDAO;
import com.ib.docu.db.dao.MMSsportObedinenieDAO;
import com.ib.docu.db.dao.ReferentDAO;
import com.ib.docu.db.dto.EgovMessages;
import com.ib.docu.db.dto.EgovMessagesFiles;
import com.ib.docu.db.dto.MMSChlenstvo;
import com.ib.docu.db.dto.MMSCoaches;
import com.ib.docu.db.dto.MMSCoachesDiploms;
import com.ib.docu.db.dto.MMSSportObedMf;
import com.ib.docu.db.dto.MMSSportObekt;
import com.ib.docu.db.dto.MMSSportObektLice;
import com.ib.docu.db.dto.MMSSportnoObedinenie;
import com.ib.docu.db.dto.MMSVidSportSF;
import com.ib.docu.db.dto.MMSVidSportSO;
import com.ib.docu.db.dto.MMSVidSportSpOb;
import com.ib.docu.db.dto.MMSVpisvane;
import com.ib.docu.db.dto.MMSsportFormirovanie;
import com.ib.docu.db.dto.Referent;
import com.ib.docu.db.dto.ReferentAddress;
import com.ib.docu.system.DocuConstants;
import com.ib.docu.system.SystemData;
import com.ib.system.ActiveUser;
import com.ib.system.db.JPA;
import com.ib.system.exceptions.DbErrorException;
import com.ib.system.utils.ValidationUtils;

import bg.government.regixclient.RegixClientException;

public class ParsePdfZaqvlenie {

	/**
	 * 
	 * @param fileContent
	 * @param sd
	 * @return String[0] - кода на услугата
	 * 		   String[1] - еик (само когато е заличаване на обединение/формирование, във останалите случаи не се извлича)
	 * 		   null -  когато нямаме код на услугата във файл-а (не е пдф който можем да обработим обикновенно)
	 * @throws ParserConfigurationException
	 * @throws DbErrorException 
	 * @throws IOException 
	 * @throws SAXException 
	 */
	public String[] getCodeAndEIK(byte[] fileContent, SystemData sd, Integer lang) throws ParserConfigurationException, DbErrorException, SAXException, IOException {
		String[] s=new String[2];
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		
		DocumentBuilder db = dbf.newDocumentBuilder();
		com.aspose.pdf.facades.Form form = new com.aspose.pdf.facades.Form();
		form.bindPdf(new ByteArrayInputStream(fileContent));
		
		//кода на услугата 
		if (form.getDocument()==null || form.getDocument().getInfo()==null || form.getDocument().getInfo().getTitle()==null) {
			form.close();
			return null;
		}
		s[0]=form.getDocument().getInfo().getTitle().substring(0, form.getDocument().getInfo().getTitle().indexOf("ZVLN"))+"ZVLN";
		
		
		//ако вида е заличаване на обед/формиров тогава търсим ЕИК
		if (sd.getItemsByCodeExt(DocuConstants.CODE_CLASSIF_DOC_VID, form.getDocument().getInfo().getTitle(), lang, new Date()).size()>0 
				&& sd.getItemsByCodeExt(DocuConstants.CODE_CLASSIF_DOC_VID, form.getDocument().getInfo().getTitle(), lang, new Date()).get(0).getCode()==DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_ZALICH_SPORTNO_OBEDINENIE) {
			
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			 
			form.exportXml(out);
			ByteArrayInputStream  is=new ByteArrayInputStream(out.toByteArray());
			Document doc = db.parse(is);
			doc.getDocumentElement().normalize();
			NodeList nodeList = doc.getElementsByTagName("Zvln");
			
			// nodeList is not iterable, so we are using for loop
			for (int itr = 0; itr < nodeList.getLength(); itr++) {
				Node node = nodeList.item(itr);

				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) node;
					for (int i = 0; i < eElement.getElementsByTagName("nflEik").getLength(); i++) {
						if (eElement.getElementsByTagName("nflEik").item(i).getParentNode().getNodeName().equals("Main")) {
							s[1]=eElement.getElementsByTagName("nflEik").item(i).getTextContent();
							 
								form.close();
								return s;
						} 
					}
				}
			}
			
		}
		
		
		form.close();
		
		return s;
	}
	
	public MMSSportnoObedinenie parseObedinenie(SystemData sd, ActiveUser ac, Integer lang, EgovMessages mess, List<EgovMessagesFiles> files) throws DOMException, SAXException, IOException, ParserConfigurationException, DbErrorException, RegixClientException, DatatypeConfigurationException, ParseException  {
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		
		DocumentBuilder db = dbf.newDocumentBuilder();
		
		boolean isNew=true;
		boolean isDelete=false;
		boolean pdfFound=false;
		
		MMSSportnoObedinenie spObed = new MMSSportnoObedinenie();
		Integer vid=null;
		com.aspose.pdf.facades.Form form = new com.aspose.pdf.facades.Form();
		/******************** LOCAL TESTING ***********************/
//		String path = "C:\\Users\\s.marinov\\Desktop\\MMS\\testPdf\\New100323\\Promqna\\";
//
//		form.bindPdf(path + "037003ZVLNv01-Заявление за промяна на обстоятелства на спортно обединение - НОСТД_SIGNED.pdf");
//		pdfFound=true;
//		isNew=false;
//		isDelete=false;
		/*********************LOCAL TESTING END *************************/
				
		/*********************FOR PRODUCTION  *************************/
		
		if (mess!=null && mess.getDocVid()!=null) {
			try {
				Integer vidZ=Integer.parseInt(mess.getDocVid());
				if (vidZ.equals(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_SPORTNO_OBEDINENIE)) {
					isNew=true;
					isDelete=false;
				}
				if (vidZ.equals(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_PROM_SPORTNO_OBEDINENIE)) {
					isNew=false;
					isDelete=false;
				}
				if (vidZ.equals(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_ZALICH_SPORTNO_OBEDINENIE)) {
					isNew=false;
					isDelete=true;
				}
				
			} catch (NumberFormatException e) {
				//не е код от класификацията прескачаме всичко
				pdfFound=false;
				spObed.getParseMessages().add("Не е открит пдф за извличане на данни!");
				form.close();
				return spObed;
			}
//			//очакваме да има пдф отиваме да го търсим
//			 pdfFound=true;
		}
		
		for (int i = 0; i < files.size(); i++) {
			if (files.get(i).getMime()!=null && files.get(i).getMime().contains("application/pdf")) {
				
				form.bindPdf(new ByteArrayInputStream(files.get(i).getBlobcontent()));
				//tarsim file koito shte parsvame
				if (form.getDocument()==null || form.getDocument().getInfo()==null || form.getDocument().getInfo().getTitle()==null) {
					pdfFound=false;
				}else {
					if (form.getDocument().getInfo().getTitle().contains("ZVLN")) {
						//razkodirame vida
						for (int j = 0; j < sd.getSysClassification(DocuConstants.CODE_CLASSIF_VID_SPORT_OBEDINENIE, new Date(), lang).size(); j++) {
							if (sd.getSysClassification(DocuConstants.CODE_CLASSIF_VID_SPORT_OBEDINENIE, new Date(), lang).get(j).getCodeExt().equals(form.getDocument().getInfo().getTitle().substring(0, form.getDocument().getInfo().getTitle().indexOf("ZVLN"))+"ZVLN")) {
								vid=sd.getSysClassification(DocuConstants.CODE_CLASSIF_VID_SPORT_OBEDINENIE, new Date(), lang).get(j).getCode();
							}
						}
						pdfFound=true;
						break;
					}	
				}				
			}
		}
		
		
		if (!pdfFound) {
			spObed.getParseMessages().add("Не е открит пдф за извличане на данни!");
			form.close();
			return spObed;
		}
		/********************** FOR PRODUCTION END ********************************/
 
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		// Export data
		form.exportXml(out);
		ByteArrayInputStream  is=new ByteArrayInputStream( out.toByteArray() );
		Document doc = db.parse(is);
		
		doc.getDocumentElement().normalize();
		
		String mail="";
		
		Referent ref=new Referent();
		NodeList nodeList = doc.getElementsByTagName("Zvln");
		
		// nodeList is not iterable, so we are using for loop
		for (int itr = 0; itr < nodeList.getLength(); itr++) {
			Node node = nodeList.item(itr);
//						System.out.println("\nNode Name :" + node.getNodeName());
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) node;
				
				if (eElement.getElementsByTagName("contactEmail").item(0)!=null && !eElement.getElementsByTagName("contactEmail").item(0).getTextContent().trim().isEmpty()) {
					mail=eElement.getElementsByTagName("contactEmail").item(0).getTextContent();
				}
				
				String mainEik=null;
				List<String[]> otherEikList=new ArrayList<String[]>();
				for (int i = 0; i < eElement.getElementsByTagName("nflEik").getLength(); i++) {
					if (eElement.getElementsByTagName("nflEik").item(i).getParentNode().getNodeName().equals("Main")) {
						mainEik=eElement.getElementsByTagName("nflEik").item(i).getTextContent();
						if(!ValidationUtils.isValidBULSTAT(mainEik)) {
							spObed.getParseMessages().add("Невалидно ЕИК: "+mainEik);
							spObed.setMailLice(mail);
							form.close();
							return spObed;
						}
					}else {
						String [] s=new String [2];
						s[0]=eElement.getElementsByTagName("nflEik").item(i).getTextContent();
						if(!ValidationUtils.isValidBULSTAT(eElement.getElementsByTagName("nflEik").item(i).getTextContent())) {
							spObed.getParseMessages().add("Невалидно ЕИК: "+eElement.getElementsByTagName("nflEik").item(i).getTextContent());
							spObed.setMailLice(mail);
							form.close();
							return spObed;
						}
						Node childNode = eElement.getElementsByTagName("nflEik").item(i);  
						while( childNode.getNextSibling()!=null ){          
					        childNode = childNode.getNextSibling();         
					        if (childNode.getNodeType() == Node.ELEMENT_NODE) {         
					            Element childElement = (Element) childNode;             
					            s[1]=childElement.getTextContent();     
					            break;
					        }       
					    }
						
						otherEikList.add(s);
					}
						
				}
				
				//do tuk i 3-te vida vpis, promqna i zalichavane trqbwa da imat nflEIK - ina4e nqma smisal natam.
				
				if (mainEik==null) {
					spObed.getParseMessages().add("Не е откритo ЕИК във пдф-а!");
					spObed.setMailLice(mail);
					form.close();
					return spObed;
				}
				
				
				ref=findReferent(ref, mainEik, null, null, sd, eElement, lang, ac,true, isDelete);
				if (ref==null) {
					spObed.getParseMessages().add("Не е откритo юрид. лице с ЕИК:"+mainEik);	
					spObed.setMailLice(mail);
					form.close();
					return spObed;
				}
				
				//tarsim dali ima obekt s toq referent v bazata ve4e
				spObed.setIdObject(ref.getCode());
				spObed=new MMSsportObedinenieDAO(MMSSportnoObedinenie.class, ac).findByIdObject(spObed);
				
				if (isNew) {
					if (spObed!=null && spObed.getId()!=null) {
						GregorianCalendar gc=new GregorianCalendar();
						if (spObed.getDateStatus()!=null) {
							gc.setTime(spObed.getDateStatus());
							gc.add(Calendar.YEAR, 1);
						}
						
						if (spObed.getVid()==DocuConstants.CODE_ZNACHENIE_VID_SPORT_OBEDINENIE_SF
								&& spObed.getStatus()==DocuConstants.CODE_ZNACHENIE_STATUS_OBEKT_OTKAZAN
								&& spObed.getDateStatus()!=null
								&& gc.getTime().after(mess.getDocDate())) {
							// ako e federaciq i e s otkazan licenz, gledame 1 godina dali e minala za da gi pusnem na vtoro vpisvane.
							
							spObed.getParseMessages().add("Нямате право да вписвате тази федерация преди: "+new SimpleDateFormat("dd.MM.yyyy").format(gc.getTime()));
							form.close();
							return spObed;
						}else {
							spObed.getParseMessages().add("Опитвате се да впишете обединение коeто съществува!");
							spObed.setMailLice(mail);
							form.close();
							return spObed;
						}
					}
					spObed=new MMSSportnoObedinenie();
					//nov e slagame mu ref i prodaljavame s drugite danni
					spObed.setIdObject(ref.getCode());
					if (spObed.getPredstavitelstvo()==null) {
						spObed.setPredstavitelstvo(ref.getPredstavitelstvo());	
					}
					
				}else {
					if (spObed==null || spObed.getId()==null) {
						spObed=new MMSSportnoObedinenie();
						spObed.getParseMessages().add("Не е открито обединение!");
						spObed.setMailLice(mail);
						form.close();
						return spObed;
					}else {
						// от последните бележки мейл 17.05 - т.12 (Даниела и Киро след разговор се разбра, намери ли се нещо в базата промяна или изтриване прескачаме четенето натам от пдф)
						//коментираме тези 2 реда ако искаме да се парсва пдф-а при промяна на обстоятелства
						form.close();
						return spObed;
					}
				}
				
				 spObed.setVid(vid);
				
				//vidove sport
				List<Integer> vidSportL=new ArrayList<Integer>();
				if (!isNew) {
					// promqna shte trqbva da gi vzemem parvo
					for (int i = 0; i < spObed.getVidSportList().size(); i++) {
						vidSportL.add(spObed.getVidSportList().get(i).getVidSport());
					}
				}
				findVidSportList(vidSportL, eElement, sd, lang, isNew);
				spObed.getVidSportList().clear();
				for (int i = 0; i < vidSportL.size(); i++) {
					MMSVidSportSO tmp = new MMSVidSportSO();
					tmp.setTipОbject(DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORT_OBED); // koda na object Sportno formirovanie
					tmp.setIdObject(spObed.getId());
					tmp.setVidSport(vidSportL.get(i));
					tmp.setUserReg(ac.getUserId());
					tmp.setDateReg(new Date());
					spObed.getVidSportList().add(tmp);
				}
				
				if (eElement.getElementsByTagName("predsedatel").item(0)!=null && !eElement.getElementsByTagName("predsedatel").item(0).getTextContent().trim().isEmpty()) {
					spObed.setPredsedatel(eElement.getElementsByTagName("predsedatel").item(0).getTextContent());	
				}
				
				if (eElement.getElementsByTagName("dopInfo").item(0)!=null && !eElement.getElementsByTagName("dopInfo").item(0).getTextContent().trim().isEmpty()) {
					spObed.setDopInfo(eElement.getElementsByTagName("dopInfo").item(0).getTextContent());
				}
				
				if (eElement.getElementsByTagName("genSekDirektor").item(0)!=null && !eElement.getElementsByTagName("genSekDirektor").item(0).getTextContent().trim().isEmpty()) {
					spObed.setGenSekDirektor(eElement.getElementsByTagName("genSekDirektor").item(0).getTextContent());
				}
				
		/****************** nachin na poluchavane ***************/		
				if (eElement.getElementsByTagName("nachinPost").item(0)!=null && !eElement.getElementsByTagName("nachinPost").item(0).getTextContent().trim().isEmpty()) {
					if (eElement.getElementsByTagName("nachinPost").item(0).getTextContent().equals("1")) {
						spObed.setNachinPoluch(DocuConstants.CODE_ZNACHENIE_PREDAVANE_POSHTA);
					}					
				}
				if (eElement.getElementsByTagName("nachinCCEB").item(0)!=null && !eElement.getElementsByTagName("nachinCCEB").item(0).getTextContent().trim().isEmpty()) {
					if (eElement.getElementsByTagName("nachinCCEB").item(0).getTextContent().equals("1")) {
						spObed.setNachinPoluch(DocuConstants.CODE_ZNACHENIE_PREDAVANE_SSEV);
					}					
				}
				if (eElement.getElementsByTagName("nachinByhand").item(0)!=null && !eElement.getElementsByTagName("nachinByhand").item(0).getTextContent().trim().isEmpty()) {
					if (eElement.getElementsByTagName("nachinByhand").item(0).getTextContent().equals("1")) {
						spObed.setNachinPoluch(DocuConstants.CODE_ZNACHENIE_PREDAVANE_NA_RAKA);
					}					
				}
				if (eElement.getElementsByTagName("nachinEmail").item(0)!=null && !eElement.getElementsByTagName("nachinEmail").item(0).getTextContent().trim().isEmpty()) {
					if (eElement.getElementsByTagName("nachinEmail").item(0).getTextContent().equals("1")) {
						spObed.setNachinPoluch(DocuConstants.CODE_ZNACHENIE_PREDAVANE_EMAIL);
					}					
				}
				if (eElement.getElementsByTagName("nachinDrug").item(0)!=null && !eElement.getElementsByTagName("nachinDrug").item(0).getTextContent().trim().isEmpty()) {
					if (eElement.getElementsByTagName("nachinDrug").item(0).getTextContent().equals("1")) {
						spObed.setNachinPoluch(DocuConstants.CODE_ZNACHENIE_PREDAVANE_DRUG);
					}					
				}
				for (int i = 0; i < eElement.getElementsByTagName("nachinPoluchavane").item(0).getChildNodes().getLength(); i++) {
					Node childNode = eElement.getElementsByTagName("nachinPoluchavane").item(0).getChildNodes().item(i);
					while( childNode.getNextSibling()!=null ){          
				        childNode = childNode.getNextSibling();  
				        if (childNode.getNodeType() == Node.ELEMENT_NODE) {         
				            Element childElement = (Element) childNode; 
				            if (childElement.getTagName().equals("text")) {
				            	spObed.setNachinPoluchText(childElement.getTextContent());
				            	break;
				            }
				        }
					}
				}
/********************** krai nachin na poluchavane **************/
				
				
				for (int i = 0; i < eElement.getElementsByTagName("sportObedMf").getLength(); i++) {
					Node childNode = eElement.getElementsByTagName("sportObedMf").item(i);
					
					MMSSportObedMf mmsSportObedMf = new MMSSportObedMf();
					mmsSportObedMf.setMejdFed(decodeByClassif(DocuConstants.CODE_CLASSIF_MMS_MEJD_FED, eElement.getElementsByTagName("sportObedMf").item(i).getTextContent(), lang, sd));;
					if (mmsSportObedMf.getMejdFed()!=null) {
						while( childNode.getNextSibling()!=null ){          
					        childNode = childNode.getNextSibling();  
					        boolean add=false;
					        if (childNode.getNodeType() == Node.ELEMENT_NODE) {         
					            Element childElement = (Element) childNode; 
					            if (childElement.getTagName().equals("dateBeg") && !childElement.getTextContent().isBlank()) {
					            	SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
					            	mmsSportObedMf.setDateBeg(sdf.parse(childElement.getTextContent()));				            	
								}
					            if (isNew) {
					            	//novo e direktno dobavqme
									add=true;
								}else {
									//promqna e gledame kakva e promqnata
						            if (childElement.getTagName().equals("action") && childElement.getTextContent()!=null) {
						            	if (childElement.getTextContent().equals("Добави")) {
											add=true;
										}				            	
						            }		
								}
						
								if (add) {
									//ako deistvieto e dobavqne						
									spObed.getMejdFedList().add(mmsSportObedMf);
								}else {
									//mahame go
									for (int j = 0; j < spObed.getMejdFedList().size(); j++) {
										if (spObed.getMejdFedList().get(i).getMejdFed()!=null 
												&& mmsSportObedMf.getMejdFed()!=null 
												&& spObed.getMejdFedList().get(i).getMejdFed()==mmsSportObedMf.getMejdFed()) {
											spObed.getMejdFedList().remove(i);
											break;
										}
									}
								}
					        }       
					    }
					}
					
				}
				
				
			}
		}
		 
		form.close();
		return spObed;
	}
	
	public MMSsportFormirovanie parseFormirovanie(SystemData sd, ActiveUser ac, Integer lang, EgovMessages mess, List<EgovMessagesFiles> files) throws DOMException, SAXException, IOException, ParserConfigurationException, DbErrorException, RegixClientException, DatatypeConfigurationException, ParseException{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		
		DocumentBuilder db = dbf.newDocumentBuilder();
		
		
		boolean isNew=true;
		boolean isDelete=false;
		
		
		com.aspose.pdf.facades.Form form = new com.aspose.pdf.facades.Form();
		
		MMSsportFormirovanie fo=new MMSsportFormirovanie();
		Integer vid=null;
		 
		boolean pdfFound=false;
		
		if (mess!=null && mess.getDocVid()!=null) {
			try {
				Integer vidZ=Integer.parseInt(mess.getDocVid());
				if (vidZ.equals(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_SPORTNO_FORM)) {
					isNew=true;
					isDelete=false;
				}
				if (vidZ.equals(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_PROM_SPORTNO_FORM)) {
					isNew=false;
					isDelete=false;
				}
				if (vidZ.equals(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_ZALICH_SPORTNO_FORM)) {
					isNew=false;
					isDelete=true;
				}
			} catch (NumberFormatException e) {
				//не е код от класификацията прескачаме всичко
				pdfFound=false;
				fo.getParseMessages().add("Не е открит пдф за извличане на данни!");
				form.close();
				return fo;
			}
			//очакваме да има пдф отиваме да го търсим
//			 pdfFound=true;
		}
		
		for (int i = 0; i < files.size(); i++) {
			if (files.get(i).getMime().contains("application/pdf")) {
				
				form.bindPdf(new ByteArrayInputStream(files.get(i).getBlobcontent()));
				//tarsim file koito shte parsvame
				if (form.getDocument()==null || form.getDocument().getInfo()==null || form.getDocument().getInfo().getTitle()==null) {
					pdfFound=false;
				}else {
					if (form.getDocument().getInfo().getTitle().contains("ZVLN")) {
						//razkodirame vida
						for (int j = 0; j < sd.getSysClassification(DocuConstants.CODE_CLASSIF_VID_SPORTNO_FORMIROVANIE, new Date(), lang).size(); j++) {
							if (sd.getSysClassification(DocuConstants.CODE_CLASSIF_VID_SPORTNO_FORMIROVANIE, new Date(), lang).get(j).getCodeExt().equals(form.getDocument().getInfo().getTitle().substring(0, form.getDocument().getInfo().getTitle().indexOf("ZVLN"))+"ZVLN")) {
								vid=sd.getSysClassification(DocuConstants.CODE_CLASSIF_VID_SPORTNO_FORMIROVANIE, new Date(), lang).get(j).getCode();
							}
						}
						pdfFound=true;
						break;
					}	
				}	
			}
		}
		
		
		if (!pdfFound) {
			fo.getParseMessages().add("Не е открит пдф за извличане на данни!");
			form.close();
			return fo;
		}
		
		
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		// Export data
		form.exportXml(out);
		ByteArrayInputStream  is=new ByteArrayInputStream( out.toByteArray() );
		Document doc = db.parse(is);
		
		String mail="";
 
		
		doc.getDocumentElement().normalize();
		
		
		Referent ref=new Referent();
		NodeList nodeList = doc.getElementsByTagName("Zvln");
		
		// nodeList is not iterable, so we are using for loop
		for (int itr = 0; itr < nodeList.getLength(); itr++) {
			Node node = nodeList.item(itr);
//						System.out.println("\nNode Name :" + node.getNodeName());
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) node;
				
				if (eElement.getElementsByTagName("contactEmail").item(0)!=null && !eElement.getElementsByTagName("contactEmail").item(0).getTextContent().trim().isEmpty()) {
					mail=eElement.getElementsByTagName("contactEmail").item(0).getTextContent();
				}
				
				String mainEik=null;
				List<String[]> otherEikList=new ArrayList<String[]>();
				for (int i = 0; i < eElement.getElementsByTagName("nflEik").getLength(); i++) {
					if (eElement.getElementsByTagName("nflEik").item(i).getParentNode().getNodeName().equals("Main")) {
						mainEik=eElement.getElementsByTagName("nflEik").item(i).getTextContent();
						if(!ValidationUtils.isValidBULSTAT(mainEik)) {
							fo.getParseMessages().add("Невалидно ЕИК: "+mainEik);
							fo.setMailLice(mail);
							form.close();
							return fo;
						}
					}else {
						if (!eElement.getElementsByTagName("nflEik").item(i).getTextContent().isBlank()) {
							String [] s=new String [2];
							s[0]=eElement.getElementsByTagName("nflEik").item(i).getTextContent();
							if(!ValidationUtils.isValidBULSTAT(eElement.getElementsByTagName("nflEik").item(i).getTextContent())) {
								fo.getParseMessages().add("Невалидно ЕИК: "+eElement.getElementsByTagName("nflEik").item(i).getTextContent());
								fo.setMailLice(mail);
								form.close();
								return fo;
							}
							Node childNode = eElement.getElementsByTagName("nflEik").item(i);  
							while( childNode.getNextSibling()!=null ){          
						        childNode = childNode.getNextSibling();         
						        if (childNode.getNodeType() == Node.ELEMENT_NODE) {         
						            Element childElement = (Element) childNode;             
						            s[1]=childElement.getTextContent();     
						            break;
						        }       
						    }
							
							otherEikList.add(s);
						}
					}
						
				}
				
				if (mainEik==null) {
					fo.getParseMessages().add("Не е откритo ЕИК във пдф-а!");	
					fo.setMailLice(mail);
					form.close();
					return fo;
				}
				
				
				
				
				
				//  Нещо в което да се сложат на спортно обединение междФед, че се записват отделно.
				
				 
				ref=findReferent(ref, mainEik, null, null, sd, eElement, lang, ac,true, isDelete);
				if (ref==null) {
					fo.getParseMessages().add("Не е откритo юрид. лице с ЕИК:"+mainEik);	
					fo.setMailLice(mail);
					form.close();
					return fo;
				}
				//tarsim dali ima obekt s toq referent v bazata ve4e
				fo=new MMSsportFormirovanieDAO(MMSsportFormirovanie.class, ac).findByIdObject(ref.getCode());
				
				
				if (isNew) {
					if (fo!=null && fo.getId()!=null) {
						fo.getParseMessages().add("Опитвате се да впишете формирование което съществува!");
						fo.setMailLice(mail);
						form.close();
						return fo;	
					}
					fo=new MMSsportFormirovanie();
					//nov e slagame mu ref i prodaljavame s drugite danni
					fo.setIdObject(ref.getCode());	
					if (fo.getPredstavitelstvo()==null) {
						fo.setPredstavitelstvo(ref.getPredstavitelstvo());	
					}
					
				}else {
					if (fo==null || fo.getId()==null) {
						fo=new MMSsportFormirovanie();
						fo.getParseMessages().add("Не е открито формирование!");
						fo.setMailLice(mail);
						form.close();
						return fo;
					}else {
						// от последните бележки мейл 17.05 - т.12 (Даниела и Киро след разговор се разбра, намери ли се нещо в базата промяна или изтриване прескачаме четенето натам от пдф)
						//коментираме тези 2 реда ако искаме да се парсва пдф-а при промяна на обстоятелства
						form.close();
						return fo;
					}
				}
				fo.setVid(vid);
				  
				
				//vidove sport
				List<Integer> vidSportL=new ArrayList<Integer>();
				if (!isNew) {
					// promqna shte trqbva da gi vzemem parvo
					for (int i = 0; i < fo.getVidSportList().size(); i++) {
						vidSportL.add(fo.getVidSportList().get(i).getVidSport());
					}
				}
				findVidSportList(vidSportL, eElement, sd, lang, isNew);
				fo.getVidSportList().clear();
				for (int i = 0; i < vidSportL.size(); i++) {
					MMSVidSportSF tmp = new MMSVidSportSF();
					tmp.setTipОbject(DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_FORMATIONS); // koda na object Sportno formirovanie
					tmp.setIdObject(fo.getId());
					tmp.setVidSport(vidSportL.get(i));
					tmp.setUserReg(ac.getUserId());
					tmp.setDateReg(new Date());
					fo.getVidSportList().add(tmp);
				}
				if (eElement.getElementsByTagName("predsedatel").item(0)!=null && !eElement.getElementsByTagName("predsedatel").item(0).getTextContent().trim().isEmpty()) {
					fo.setPredsedatel(eElement.getElementsByTagName("predsedatel").item(0).getTextContent());	
				}
				if (eElement.getElementsByTagName("univers").item(0)!=null && !eElement.getElementsByTagName("univers").item(0).getTextContent().trim().isEmpty()) {
					fo.setUnivers(Integer.valueOf(eElement.getElementsByTagName("univers").item(0).getTextContent()));	
				}
				if (eElement.getElementsByTagName("osnovanieUnivers").item(0)!=null && !eElement.getElementsByTagName("osnovanieUnivers").item(0).getTextContent().trim().isEmpty()) {
					fo.setOsnovanieUnivers(eElement.getElementsByTagName("osnovanieUnivers").item(0).getTextContent());	
				}
				if (eElement.getElementsByTagName("schoolName").item(0)!=null && !eElement.getElementsByTagName("schoolName").item(0).getTextContent().trim().isEmpty()) {
					fo.setSchoolName(eElement.getElementsByTagName("schoolName").item(0).getTextContent());	
				}
				if (eElement.getElementsByTagName("dopInfo").item(0)!=null && !eElement.getElementsByTagName("dopInfo").item(0).getTextContent().trim().isEmpty()) {
					fo.setDopInfo(eElement.getElementsByTagName("dopInfo").item(0).getTextContent());
				}
				/****************** nachin na poluchavane ***************/		
				if (eElement.getElementsByTagName("nachinPost").item(0)!=null && !eElement.getElementsByTagName("nachinPost").item(0).getTextContent().trim().isEmpty()) {
					if (eElement.getElementsByTagName("nachinPost").item(0).getTextContent().equals("1")) {
						fo.setNachinPoluch(DocuConstants.CODE_ZNACHENIE_PREDAVANE_POSHTA);
					}					
				}
				if (eElement.getElementsByTagName("nachinCCEB").item(0)!=null && !eElement.getElementsByTagName("nachinCCEB").item(0).getTextContent().trim().isEmpty()) {
					if (eElement.getElementsByTagName("nachinCCEB").item(0).getTextContent().equals("1")) {
						fo.setNachinPoluch(DocuConstants.CODE_ZNACHENIE_PREDAVANE_SSEV);
					}					
				}
				if (eElement.getElementsByTagName("nachinByhand").item(0)!=null && !eElement.getElementsByTagName("nachinByhand").item(0).getTextContent().trim().isEmpty()) {
					if (eElement.getElementsByTagName("nachinByhand").item(0).getTextContent().equals("1")) {
						fo.setNachinPoluch(DocuConstants.CODE_ZNACHENIE_PREDAVANE_NA_RAKA);
					}					
				}
				if (eElement.getElementsByTagName("nachinEmail").item(0)!=null && !eElement.getElementsByTagName("nachinEmail").item(0).getTextContent().trim().isEmpty()) {
					if (eElement.getElementsByTagName("nachinEmail").item(0).getTextContent().equals("1")) {
						fo.setNachinPoluch(DocuConstants.CODE_ZNACHENIE_PREDAVANE_EMAIL);
					}					
				}
				if (eElement.getElementsByTagName("nachinDrug").item(0)!=null && !eElement.getElementsByTagName("nachinDrug").item(0).getTextContent().trim().isEmpty()) {
					if (eElement.getElementsByTagName("nachinDrug").item(0).getTextContent().equals("1")) {
						fo.setNachinPoluch(DocuConstants.CODE_ZNACHENIE_PREDAVANE_DRUG);
					}					
				}
				for (int i = 0; i < eElement.getElementsByTagName("nachinPoluchavane").item(0).getChildNodes().getLength(); i++) {
					Node childNode = eElement.getElementsByTagName("nachinPoluchavane").item(0).getChildNodes().item(i);
					while( childNode.getNextSibling()!=null ){          
				        childNode = childNode.getNextSibling();  
				        if (childNode.getNodeType() == Node.ELEMENT_NODE) {         
				            Element childElement = (Element) childNode; 
				            if (childElement.getTagName().equals("text")) {
				            	fo.setNachinPoluchText(childElement.getTextContent());
				            	break;
				            }
				        }
					}
				}
/********************** krai nachin na poluchavane **************/
				
				//chlenstvo v sportni obed.
				for (int i = 0; i < otherEikList.size(); i++) {
					Referent referentSO=new Referent();
					 
					referentSO=findReferent(referentSO, otherEikList.get(i)[0], null, null, sd, eElement, lang, ac,false, isDelete);
					if (referentSO!=null && referentSO.getCode()!=null) {
						// imame referent shte se ma4im da namerim Obedinenie.
						MMSSportnoObedinenie tmpSO =new MMSSportnoObedinenie();
						tmpSO.setIdObject(referentSO.getCode());
						tmpSO=new MMSsportObedinenieDAO(MMSSportnoObedinenie.class, ac).findByIdObject(tmpSO);
						if (tmpSO!=null && tmpSO.getId()!=null) {
							MMSChlenstvo mmsChlenstvo = new MMSChlenstvo();
							mmsChlenstvo.setTypeObject(DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_FORMATIONS);
							mmsChlenstvo.setIdObject(fo.getId());
							mmsChlenstvo.setTypeVishObject(DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORT_OBED);
							mmsChlenstvo.setIdVishObject(Integer.valueOf(tmpSO.getId()));
							
							SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
							mmsChlenstvo.setDateAcceptance(sdf.parse(otherEikList.get(i)[1]));
							
							mmsChlenstvo.setVid(tmpSO.getVid());
							mmsChlenstvo.setNameRef(referentSO.getRefName());
							mmsChlenstvo.setEik(referentSO.getNflEik());
							mmsChlenstvo.setRegNom(tmpSO.getRegNomer());
							
							fo.getMmsChlenList().add(mmsChlenstvo);
						}else {
							fo.getParseMessages().add("Не е намерено обединение за: "+otherEikList.get(i)[0]+"!");
							fo.setMailLice(mail);
						}
					}else {
						fo.getParseMessages().add("Не е открито юридическо лице за: "+otherEikList.get(i)[0]);
						fo.setMailLice(mail);
					}
					
					
				}
				
				 
				
				 
				
			}
		}
		form.close();
		return fo;
	}
	
	public MMSSportObekt parseObekt(SystemData sd, ActiveUser ac, Integer lang, EgovMessages mess, List<EgovMessagesFiles> files) throws DOMException, SAXException, IOException, ParserConfigurationException, DbErrorException, RegixClientException, DatatypeConfigurationException, ParseException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		
		DocumentBuilder db = dbf.newDocumentBuilder();
		
		
		boolean isNew=true;
		boolean isDelete=false;
		Integer vidZ = null;           // Вид заявление
		String docRn = mess.getDocRn();     // rnDoc
		if (docRn != null) docRn = docRn.trim();
		if (docRn.isEmpty()) docRn = null;
		
		
		
		com.aspose.pdf.facades.Form form = new com.aspose.pdf.facades.Form();
		
		MMSSportObekt ob=new MMSSportObekt();
		
		
		 
		boolean pdfFound=false;
	
		
		if (mess!=null && mess.getDocVid()!=null) {
			try {
				Integer vid=Integer.parseInt(mess.getDocVid());
				if (vid.equals(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_SPORTEN_OBEKT)) {
					isNew=true;
					isDelete=false;
				}
				if (vid.equals(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_PROM_SPORTEN_OBEKT)) {
					isNew=false;
					isDelete=false;
					
				}
				if (vid.equals(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_ZALICH_SPORTEN_OBEKT)) {
					isNew=false;
					isDelete=true;
				
				}
				vidZ = vid;
				
			} catch (NumberFormatException e) {
				//не е код от класификацията прескачаме всичко
				pdfFound=false;
				ob.getParseMessages().add("Не е открит пдф за извличане на данни!");
				form.close();
				return ob;
			}
			//очакваме да има пдф отиваме да го търсим
//			 pdfFound=true;
		}
		
		for (int i = 0; i < files.size(); i++) {
			if (files.get(i).getMime().contains("application/pdf")) {
				
				form.bindPdf(new ByteArrayInputStream(files.get(i).getBlobcontent()));
				//tarsim file koito shte parsvame
				if (form.getDocument()==null || form.getDocument().getInfo()==null || form.getDocument().getInfo().getTitle()==null) {
					pdfFound=false;
				}else {
					if (form.getDocument().getInfo().getTitle().contains("ZVLN")) {
						pdfFound=true;
						break;
					}	
				}	
			}
		}
		
		
		if (!pdfFound) {
			ob.getParseMessages().add("Не е открит пдф за извличане на данни!");
			form.close();
			return ob;
		}
		
		
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		// Export data
		form.exportXml(out);
		ByteArrayInputStream  is=new ByteArrayInputStream( out.toByteArray() );
		Document doc = db.parse(is);
		
		
		doc.getDocumentElement().normalize();
		
		
//		Referent ref=new Referent();
		NodeList nodeList = doc.getElementsByTagName("Zvln");
		
		String regNomer=null;
		String mail="";
		
		
		
		// nodeList is not iterable, so we are using for loop
		for (int itr = 0; itr < nodeList.getLength(); itr++) {
			Node node = nodeList.item(itr);
//						System.out.println("\nNode Name :" + node.getNodeName());
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) node;
				
				if (eElement.getElementsByTagName("contactEmail").item(0)!=null && !eElement.getElementsByTagName("contactEmail").item(0).getTextContent().trim().isEmpty()) {
					mail=eElement.getElementsByTagName("contactEmail").item(0).getTextContent();
				}
				   
				// Проверка дали вече е създаден спортен обект със това заявление за вписване
				if (vidZ.equals(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_SPORTEN_OBEKT)) {
					Integer idSpOb = null;
					try {
						idSpOb =   new MMSVpisvaneDAO(null).checkForZaiavlVp ( DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_OBJECTS, docRn, vidZ );  
					} catch (DbErrorException e) {
						ob=new MMSSportObekt();
						ob.getParseMessages().add("Грешка при проверка за повторно въвеждане на заявление за вписване с рег. номер = '" + docRn  + "'  !");
						ob.setE_mail(mail);
						form.close();
						return ob;
												
					}
					
					if (idSpOb== null || idSpOb.intValue() > 0) {
						ob=new MMSSportObekt();
						if (idSpOb== null)
						  ob.getParseMessages().add("Опит  за повторно въвеждане на заявление за вписване с рег. номер = '" + docRn  + "' !");
						 else
							  ob.getParseMessages().add("Опит  за повторно въвеждане на заявление за вписване с рег. номер = '" + docRn  + "' ! Има спортен обект с ID = " + idSpOb + " , вписан с това заявление! ");	  
						ob.setE_mail(mail);
						form.close();
						return ob;
					}
							
					
				}
				
				if (!isNew || isDelete) {     // Другите два типа заявления - за промяна на обстоятелства или за заличаване
					 
					// по рег номер търсим при промяна и заличаване
					if (eElement.getElementsByTagName("regNomer").item(0)!=null && !eElement.getElementsByTagName("regNomer").item(0).getTextContent().trim().isEmpty()) {
						regNomer=eElement.getElementsByTagName("regNomer").item(0).getTextContent();	
					}
					if (regNomer==null) {
						ob.getParseMessages().add("Не е открит рег. номер за спортен обект, задължителен за този тип заявление!");
						ob.setE_mail(mail);
						form.close();
						return ob;
					}else {
						ob=new MMSSportObektDAO(ac).findByRegNom(regNomer);
						if (ob==null) {
							//ne e namren
							ob=new MMSSportObekt();
							ob.getParseMessages().add("Не е открит спортен обект с указания рег. номер = '"+ regNomer +"' !");
							ob.setE_mail(mail);
							form.close();
							return ob;
						}else {
							if (ob.getStatus() == null || (ob.getStatus().intValue() == DocuConstants.CODE_ZNACHENIE_STATUS_OBEKT_V_RAZGLEJDANE
									|| ob.getStatus().intValue() == DocuConstants.CODE_ZNACHENIE_STATUS_OBEKT_OTKAZAN) ) {
								ob=new MMSSportObekt();
								ob.getParseMessages().add("Открит е спортен обект с указания рег. номер = '" + regNomer + "', който още не е вписан!");
								ob.setE_mail(mail);
								form.close();
								return ob;
							}
														
							// Проверка за този спортен обект дали вече не е записано същото заявление от Архимед по тип и рег. номер	
							if (docRn != null) {
								 boolean prDocsYes = false;
								try {
									prDocsYes =   new MMSVpisvaneDAO(null).checkDocsYes (ob.getId(), DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_OBJECTS, docRn, vidZ );  
								} catch (DbErrorException e) {
									ob=new MMSSportObekt();
									ob.getParseMessages().add("Грешка при проверка за повторно въвеждане на заявление с рег. номер = '" + docRn  + "' за  указания спортен обект с рег. номер = '" + regNomer + "' !");
									ob.setE_mail(mail);
									form.close();
									return ob;
															
								}
								if (prDocsYes) {
									ob=new MMSSportObekt();
									ob.getParseMessages().add("Опит  за повторно въвеждане на заявление с рег. номер = '" + docRn  + "' за  указания спортен обект с рег. номер = '" + regNomer + "' !");
									ob.setE_mail(mail);
									form.close();
									return ob;
								}
								
							}
							
							ob.setSpObLice(new MMSSportObektDAO(ac).findSpObLice(ob.getId())); 
							// nameili sme
							if (isDelete) {
								//za iztrivane e ne ni trqbva da parsvame ostanalite danni
								form.close();
								return ob;
								
							}
							if (!isNew) {
								// от последните бележки мейл 17.05 - т.12 (Даниела и Киро след разговор се разбра, намери ли се нещо в базата промяна или изтриване прескачаме четенето натам от пдф)
								//коментираме тези 2 реда ако искаме да се парсва пдф-а при промяна на обстоятелства
								form.close();
								return ob;
							}
						}
					}
					
				}
				
				
				
				if (eElement.getElementsByTagName("name").item(0)!=null && !eElement.getElementsByTagName("name").item(0).getTextContent().trim().isEmpty()) {
					ob.setName(eElement.getElementsByTagName("name").item(0).getTextContent());	
				}
				if (eElement.getElementsByTagName("vid").item(0)!=null && !eElement.getElementsByTagName("vid").item(0).getTextContent().trim().isEmpty()) {
					ob.setVidObekt(decodeByClassif(DocuConstants.CODE_CLASSIF_VID_SPORTEN_OBEKT, eElement.getElementsByTagName("vid").item(0).getTextContent(), lang, sd));
				}
				/****************** nachin na poluchavane ***************/		
				if (eElement.getElementsByTagName("nachinPost").item(0)!=null && !eElement.getElementsByTagName("nachinPost").item(0).getTextContent().trim().isEmpty()) {
					if (eElement.getElementsByTagName("nachinPost").item(0).getTextContent().equals("1")) {
						ob.setNachinPoluch(DocuConstants.CODE_ZNACHENIE_PREDAVANE_POSHTA);
					}					
				}
				if (eElement.getElementsByTagName("nachinCCEB").item(0)!=null && !eElement.getElementsByTagName("nachinCCEB").item(0).getTextContent().trim().isEmpty()) {
					if (eElement.getElementsByTagName("nachinCCEB").item(0).getTextContent().equals("1")) {
						ob.setNachinPoluch(DocuConstants.CODE_ZNACHENIE_PREDAVANE_SSEV);
					}					
				}
				if (eElement.getElementsByTagName("nachinByhand").item(0)!=null && !eElement.getElementsByTagName("nachinByhand").item(0).getTextContent().trim().isEmpty()) {
					if (eElement.getElementsByTagName("nachinByhand").item(0).getTextContent().equals("1")) {
						ob.setNachinPoluch(DocuConstants.CODE_ZNACHENIE_PREDAVANE_NA_RAKA);
					}					
				}
				if (eElement.getElementsByTagName("nachinEmail").item(0)!=null && !eElement.getElementsByTagName("nachinEmail").item(0).getTextContent().trim().isEmpty()) {
					if (eElement.getElementsByTagName("nachinEmail").item(0).getTextContent().equals("1")) {
						ob.setNachinPoluch(DocuConstants.CODE_ZNACHENIE_PREDAVANE_EMAIL);
					}					
				}
				if (eElement.getElementsByTagName("nachinDrug").item(0)!=null && !eElement.getElementsByTagName("nachinDrug").item(0).getTextContent().trim().isEmpty()) {
					if (eElement.getElementsByTagName("nachinDrug").item(0).getTextContent().equals("1")) {
						ob.setNachinPoluch(DocuConstants.CODE_ZNACHENIE_PREDAVANE_DRUG);
					}					
				}
				for (int i = 0; i < eElement.getElementsByTagName("nachinPoluchavane").item(0).getChildNodes().getLength(); i++) {
					Node childNode = eElement.getElementsByTagName("nachinPoluchavane").item(0).getChildNodes().item(i);
					while( childNode.getNextSibling()!=null ){          
				        childNode = childNode.getNextSibling();  
				        if (childNode.getNodeType() == Node.ELEMENT_NODE) {         
				            Element childElement = (Element) childNode; 
				            if (childElement.getTagName().equals("text")) {
				            	ob.setNachinPoluchText(childElement.getTextContent());
				            	break;
				            }
				        }
					}
				}
				/********************** krai nachin na poluchavane **************/
				
				//vidove sport
				List<Integer> vidSportL=new ArrayList<Integer>();
				if (!isNew) {
					// promqna shte trqbva da gi vzemem parvo
					for (int i = 0; i < ob.getVidSportList().size(); i++) {
						vidSportL.add(ob.getVidSportList().get(i).getVidSport());
					}
				}
				findVidSportList(vidSportL, eElement, sd, lang, isNew);
				ob.getVidSportList().clear();
				for (int i = 0; i < vidSportL.size(); i++) {
					MMSVidSportSpOb tmp = new MMSVidSportSpOb();
					tmp.setTipОbject(DocuConstants.CODE_ZNACHENIE_JOURNAL_SPORTS_OBJECTS); // koda na object Sportno formirovanie
					tmp.setIdObject(ob.getId());
					tmp.setVidSport(vidSportL.get(i));
					tmp.setUserReg(ac.getUserId());
					tmp.setDateReg(new Date());
					ob.getVidSportList().add(tmp);
				}
				
				if (eElement.getElementsByTagName("opisanie").item(0)!=null && !eElement.getElementsByTagName("opisanie").item(0).getTextContent().trim().isEmpty()) {
					ob.setOpisanie(eElement.getElementsByTagName("opisanie").item(0).getTextContent());	
				}
				if (eElement.getElementsByTagName("identification").item(0)!=null && !eElement.getElementsByTagName("identification").item(0).getTextContent().trim().isEmpty()) {
					ob.setIdentif(eElement.getElementsByTagName("identification").item(0).getTextContent());
				}

				
									
				for (int i = 0; i < eElement.getElementsByTagName("ime").getLength(); i++) {	
					Node childNode = eElement.getElementsByTagName("ime").item(i);
					
					String egn=null;
					
					MMSSportObektLice l=new MMSSportObektLice();
					Referent ref=new Referent();
					boolean add=false;
					
					if (childNode!=null && !childNode.getTextContent().isBlank()) {
		            	ref.setIme(childNode.getTextContent());
		            	ref.setRefName(childNode.getTextContent());
		            	add=true;
		            }
					
					 
					while( childNode.getNextSibling()!=null ){          
				        childNode = childNode.getNextSibling();         
				        if (childNode.getNodeType() == Node.ELEMENT_NODE) {         
				            Element childElement = (Element) childNode; 
				            if (childElement.getTagName().equals("fzl_egn")) {
				            	egn=childElement.getTextContent();				            	
				            }
				            if (childElement.getTagName().equals("type_vrazka")) {
				            	l.setTypeVrazka(decodeByClassif(DocuConstants.CODE_CLASSIF_VRAZKA_LICE_SPORTEN_OBEKT, childElement.getTextContent(), lang, sd));
				            	if (l.getTypeVrazka() == null && childElement.getTextContent() != null && !childElement.getTextContent().trim().isEmpty()) {
				            		
				            		if (childElement.getTextContent().equalsIgnoreCase("Собственик"))
				            			l.setTypeVrazka(Integer.valueOf(DocuConstants.CODE_ZNACHENIE_TYPE_VR_SOBSTV));
				            		else if (childElement.getTextContent().equalsIgnoreCase("Наемател"))
				            			l.setTypeVrazka(Integer.valueOf(DocuConstants.CODE_ZNACHENIE_TYPE_VR_NAEM));
				            	}
				            	add=true;
							}
				            if (childElement.getTagName().equals("prezime")) {
				            	ref.setPrezime(childElement.getTextContent());
				            	if (ref.getRefName()==null || ref.getRefName().isBlank()) {
				            		ref.setRefName(childElement.getTextContent());
								}else {
									ref.setRefName(ref.getRefName()+" "+childElement.getTextContent());
								}
				            	add=true;
				            }
				            if (childElement.getTagName().equals("familia")) {
				            	ref.setFamilia(childElement.getTextContent());
				            	if (ref.getRefName()==null || ref.getRefName().isBlank()) {
				            		ref.setRefName(childElement.getTextContent());
								}else {
									ref.setRefName(ref.getRefName()+" "+childElement.getTextContent());
								}
				            	add=true;
				            }
				        }       
					}
					
					
					if (egn!=null && !egn.isBlank()) {
				//		l=new MMSSportObektLice();
						if(!ValidationUtils.isValidEGN(egn) && !ValidationUtils.isValidLNCH(egn) ) {
							  ob.getParseMessages().add("Невалидно ЕГН/ЛНЧ за лице във връзка със спортен обект: "+egn);
							  ob.setE_mail(mail);
						}else {
							if(ValidationUtils.isValidLNCH(egn)) {
							   ref=findReferent(ref, null, null, egn,  sd, eElement, lang, ac,false, isDelete);
							}else {
							   ref=findReferent(ref, null, egn, null,  sd, eElement, lang, ac,false, isDelete);
							}
							if (ref!=null && ref.getCode()!=null) {
								add=true;
								l.setIdLice(ref.getCode());
							}else {
								ob.getParseMessages().add("Не е откритo лице с ЕГН: "+egn);
								ob.setE_mail(mail);
							}
						}
					} else {
						if (add) {
							if ((ref.getIme() == null || (ref.getIme() != null && ref.getIme().trim().isEmpty()) )
								&& (ref.getPrezime() == null || (ref.getPrezime() != null && ref.getPrezime().trim().isEmpty()) )
								&& (ref.getFamilia() == null || (ref.getFamilia() != null && ref.getFamilia().trim().isEmpty()) )
								)   add = false;   // Няма лице за връзка
									
						}
					}
					
					
					if (add) {
						if (ref.getCode()==null) {
							try {
								JPA.getUtil().begin();
								ref.setRefType(CODE_ZNACHENIE_REF_TYPE_FZL);
								ref = new ReferentDAO(ac).save(ref);
								
								JPA.getUtil().commit();
								l.setIdLice(ref.getCode());
							} catch (DbErrorException e) {
								e.printStackTrace();
								JPA.getUtil().rollback();
							}
						}
						//ako deistvieto e dobavqne						
						ob.getSpObLice().add(l);	
					}
					
				}
				
				
				// addressKoresp
				if (eElement.getElementsByTagName("cboSettl").item(0)!=null && !eElement.getElementsByTagName("cboSettl").item(0).getTextContent().trim().isEmpty()
						&& eElement.getElementsByTagName("cboObsht").item(0)!=null && !eElement.getElementsByTagName("cboObsht").item(0).getTextContent().trim().isEmpty()
						&& eElement.getElementsByTagName("cboObl").item(0)!=null && !eElement.getElementsByTagName("cboObl").item(0).getTextContent().trim().isEmpty()
						) {
					       String oblName = eElement.getElementsByTagName("cboObl").item(0).getTextContent().trim();
					          if (ValidationUtils.isNumber(oblName)) {
					        	  // Прочетен е код от колона abc в таблица ekatte_oblasti
					        	  oblName = new ReferentDAO(ActiveUser.DEFAULT).findEkatteOblName(Integer.valueOf(oblName));
					          }
					Integer ek=	new ReferentDAO(ActiveUser.DEFAULT).findEkatteByEkatteNames(
						//	eElement.getElementsByTagName("cboObl").item(0).getTextContent(),
							oblName,
							eElement.getElementsByTagName("cboObsht").item(0).getTextContent(),
							eElement.getElementsByTagName("cboSettl").item(0).getTextContent());
					 
					if (ek!=null) {
						ob.setNas_mesto(ek);
					}
				}
				if (eElement.getElementsByTagName("postCode").item(0)!=null && !eElement.getElementsByTagName("postCode").item(0).getTextContent().trim().isEmpty()) {
					ob.setPostCode(eElement.getElementsByTagName("postCode").item(0).getTextContent());	
				} 
				if (eElement.getElementsByTagName("addrText").item(0)!=null && !eElement.getElementsByTagName("addrText").item(0).getTextContent().trim().isEmpty()) {
					ob.setAdres(eElement.getElementsByTagName("addrText").item(0).getTextContent());
				} 
				if (eElement.getElementsByTagName("contactPhone").item(0)!=null && !eElement.getElementsByTagName("contactPhone").item(0).getTextContent().trim().isEmpty()) {
					ob.setTel(eElement.getElementsByTagName("contactPhone").item(0).getTextContent());
				} 
				if (eElement.getElementsByTagName("contactEmail").item(0)!=null && !eElement.getElementsByTagName("contactEmail").item(0).getTextContent().trim().isEmpty()) {
					ob.setE_mail(eElement.getElementsByTagName("contactEmail").item(0).getTextContent());
				}
				 
				if (eElement.getElementsByTagName("dopInfo").item(0)!=null && !eElement.getElementsByTagName("dopInfo").item(0).getTextContent().trim().isEmpty()) {
					ob.setDopInfo(eElement.getElementsByTagName("dopInfo").item(0).getTextContent());
				} 
				 
				
			}
			
			
		}
		form.close();
		return ob;
	}
	
	
	public MMSCoaches parseTrener(SystemData sd, ActiveUser ac, Integer lang, EgovMessages mess, List<EgovMessagesFiles> files) throws DOMException, SAXException, IOException, ParserConfigurationException, DbErrorException, RegixClientException, DatatypeConfigurationException, ParseException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		
		DocumentBuilder db = dbf.newDocumentBuilder();
		
		boolean isNew=true;
		boolean isDelete=false;
		
		
		com.aspose.pdf.facades.Form form = new com.aspose.pdf.facades.Form();
		
		MMSCoaches co=new MMSCoaches();
		
		
		 
		boolean pdfFound=false;
		
		if (mess!=null && mess.getDocVid()!=null) {
			try {
				Integer vid=Integer.parseInt(mess.getDocVid());
				if (vid.equals(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_VPISVANE_TREN_KADRI)) {
					isNew=true;
					isDelete=false;
				}
				if (vid.equals(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_PROM_TREN_KADRI)) {
					isNew=false;
					isDelete=false;
				}
				if (vid.equals(DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_ZALICH_TREN_KADRI)) {
					isNew=false;
					isDelete=true;
				}
				
			} catch (NumberFormatException e) {
				//не е код от класификацията прескачаме всичко
				pdfFound=false;
				co.getParseMessages().add("Не е открит пдф за извличане на данни!");
				form.close();
				return co;
			}
			//очакваме да има пдф отиваме да го търсим
//			 pdfFound=true;
		}
		
		
		for (int i = 0; i < files.size(); i++) {
			if (files.get(i).getMime().contains("application/pdf")) {
				
				form.bindPdf(new ByteArrayInputStream(files.get(i).getBlobcontent()));
				//tarsim file koito shte parsvame
				if (form.getDocument()==null || form.getDocument().getInfo()==null || form.getDocument().getInfo().getTitle()==null) {
					pdfFound=false;
				}else {
					if (form.getDocument().getInfo().getTitle().contains("ZVLN")) {
						pdfFound=true;
						break;
					}	
				}	
			}
		}
		
		
		if (!pdfFound) {
			co.getParseMessages().add("Не е открит пдф за извличане на данни!");
			form.close();
			return co;
		}
		
		
		
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		// Export data
		form.exportXml(out);
		ByteArrayInputStream  is=new ByteArrayInputStream( out.toByteArray() );
		Document doc = db.parse(is);
	 
		
		doc.getDocumentElement().normalize();
		
		String mail="";
		
		Referent ref=new Referent();
		NodeList nodeList = doc.getElementsByTagName("Zvln");
		
		// nodeList is not iterable, so we are using for loop
		for (int itr = 0; itr < nodeList.getLength(); itr++) {
			Node node = nodeList.item(itr);
//						System.out.println("\nNode Name :" + node.getNodeName());
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) node;
				
				if (eElement.getElementsByTagName("contactEmail").item(0)!=null && !eElement.getElementsByTagName("contactEmail").item(0).getTextContent().trim().isEmpty()) {
					mail=eElement.getElementsByTagName("contactEmail").item(0).getTextContent();
				}
				
				String mainEgn=null;
				if (eElement.getElementsByTagName("fzl_egn").item(0)!=null && !eElement.getElementsByTagName("fzl_egn").item(0).getTextContent().trim().isEmpty()) {
					mainEgn=eElement.getElementsByTagName("fzl_egn").item(0).getTextContent();
					if(!ValidationUtils.isValidEGN(mainEgn)) {
						co.getParseMessages().add("Невалидно ЕГН/ЛНЧ: "+mainEgn);
						co.setMailLice(mail);
						form.close();
						return co;
					}
				} 
						
				
				if (mainEgn==null) {
					co.getParseMessages().add("Не е откритo ЕГН/ЛНЧ във пдф-а!");	
					co.setMailLice(mail);
					form.close();
					return co;
				}
				
			
				 
				ref=findReferent(ref, null, mainEgn, null, sd, eElement, lang, ac,true, isDelete);
				
				if (ref==null) {
					co.getParseMessages().add("Не е откритo лице с ЕГН/ЛНЧ: "+ mainEgn);	
					co.setMailLice(mail);
					form.close();
					return co;
				}
				
				Integer codeVidSport=null;
				
				for (int i = 0; i < eElement.getElementsByTagName("vid_sport").getLength(); i++) {
					if (eElement.getElementsByTagName("vid_sport").item(i)!=null && !eElement.getElementsByTagName("vid_sport").item(i).getTextContent().trim().isEmpty()) {
						codeVidSport=decodeByClassif(DocuConstants.CODE_CLASSIF_VIDOVE_SPORT, eElement.getElementsByTagName("vid_sport").item(i).getTextContent(), lang, sd);
						 if (codeVidSport!=null) {
							break;
						}
					}
				}
				
				if(null==codeVidSport) {
					for (int i = 0; i < eElement.getElementsByTagName("vidSportList").getLength(); i++) {
						if (eElement.getElementsByTagName("vidSportList").item(i)!=null && !eElement.getElementsByTagName("vidSportList").item(i).getTextContent().trim().isEmpty()) {
							codeVidSport=decodeByClassif(DocuConstants.CODE_CLASSIF_VIDOVE_SPORT, eElement.getElementsByTagName("vidSportList").item(i).getTextContent(), lang, sd);
							 if (codeVidSport!=null) {
								break;
							}
						}
					}
					//taq glupost e shtoto ima razlika v tova dali e za vpisvane ili zali4avane.
					if(null==codeVidSport) {
						co.getParseMessages().add("В обработваното заявление за треньор, не е указан вид спорт. Задължителен параметър!");
						co.setMailLice(mail);
						form.close();
						return co;	
					}
				}
				 
				 
				//tarsim dali ima obekt s toq referent v bazata ve4e
				Integer coachId = new MMSCoachesDAO(MMSCoaches.class, ac).findByArg(null, ref.getCode());
				boolean returnAfterDiplom=false;
				boolean vpisvane=false;
				if (null!=coachId) {
					co=new MMSCoachesDAO(MMSCoaches.class, ac).findById(coachId); //tarsim dali ima obekt s toq referent v bazata ve4e
					co.getCoachesDiploms().size();
					List<MMSVpisvane> vpisv = new ArrayList<MMSVpisvane>(); // Tarsim ima li vpisvane za trener po coachId + codeVidSport
					vpisv = new MMSCoachesDAO(MMSCoaches.class, ac).findVpisvListByIdTypeVidSport(DocuConstants.CODE_ZNACHENIE_JOURNAL_COACHES, coachId, codeVidSport);
					if(null!=vpisv && !vpisv.isEmpty()) {
						vpisvane = true;
						co.setDlajnost(vpisv.get(0).getDlajnost());
					}	
				}
						
				if (isNew) {
					if (co!=null && co.getId()!=null) {
						if(vpisvane) {
							co.getParseMessages().add("Опитвате се да впишете треньор, който съществува с посочения в заявлението вид спорт: "+sd.decodeItem(DocuConstants.CODE_CLASSIF_VIDOVE_SPORT, codeVidSport, lang, new Date()) +"!");
							co.setVidSport(codeVidSport);
							co.setMailLice(mail);
							form.close();
							return co;	
						}else {
							co.setMailLice(mail); //Za trener v bazata Vpisvane za nov vid sport
							co.setVidSport(codeVidSport);
							co.setIdObject(ref.getCode());
							
							returnAfterDiplom=true;
							
						}
					}else {
						co=new MMSCoaches();// Nov zapis v bazata
						co.setMailLice(mail);
						co.setVidSport(codeVidSport);
						//nov e slagame mu ref i prodaljavame s drugite danni
						co.setIdObject(ref.getCode());
					}
				}else {
					if (co==null || co.getId()==null || ! vpisvane) {
						co=new MMSCoaches();
						co.getParseMessages().add("Не е открит вписан треньор с посочения в заявлението вид спорт: "+sd.decodeItem(DocuConstants.CODE_CLASSIF_VIDOVE_SPORT, codeVidSport, lang, new Date())+"!");
						co.setMailLice(mail);
						form.close();
						return co;
					}else {
						// от последните бележки мейл 17.05 - т.12 (Даниела и Киро след разговор се разбра, намери ли се нещо в базата промяна или изтриване прескачаме четенето натам от пдф)
						//коментираме тези 2 реда ако искаме да се парсва пдф-а при промяна на обстоятелства
						//Izvarshvame promyana obstojatelstvata i zali4avane
						co.setMailLice(mail);
						co.setVidSport(codeVidSport);
						form.close();
						return co;
					}
				}
						
				for (int i = 0; i < eElement.getElementsByTagName("vid_doc").getLength(); i++) {
					Node childNode = eElement.getElementsByTagName("vid_doc").item(i);
					
					MMSCoachesDiploms diplom = new MMSCoachesDiploms();
					diplom.setVidDoc(decodeByClassif(DocuConstants.CODE_CLASSIF_VID_DOC_OBR_ZENS, eElement.getElementsByTagName("vid_doc").item(i).getTextContent(), lang, sd));;
					boolean add=false;
					while( childNode.getNextSibling()!=null ){          
				        childNode = childNode.getNextSibling();         
				        if (childNode.getNodeType() == Node.ELEMENT_NODE) {         
				            Element childElement = (Element) childNode;			            
				            
				            if (childElement.getTagName().equals("reg_nomer") && childElement.getTextContent()!=null && !childElement.getTextContent().isBlank()) {
				            	diplom.setRegNomer(childElement.getTextContent());
							}
				            if (childElement.getTagName().equals("seria_fabrnom") && childElement.getTextContent()!=null && !childElement.getTextContent().isBlank()) {
				            	diplom.setSeriaFnom(childElement.getTextContent());
				            }
				            if (childElement.getTagName().equals("year_issued") && childElement.getTextContent()!=null && !childElement.getTextContent().isBlank()) {
				            	diplom.setYearIssued(Integer.valueOf(childElement.getTextContent()));
				            }
				            if (childElement.getTagName().equals("ucheb_zav_text") && childElement.getTextContent()!=null && !childElement.getTextContent().isBlank()) {
				            	diplom.setUchebnoZavedenie(decodeByClassif(DocuConstants.CODE_CLASSIF_UCHEB_ZAVEDENIE, childElement.getTextContent(), lang, sd));
				            	if (diplom.getUchebnoZavedenie()==null) {
									diplom.setUchebZavText(childElement.getTextContent());
								}
				            }
				            if (childElement.getTagName().equals("dop_info") && childElement.getTextContent()!=null && !childElement.getTextContent().isBlank()) {
				            	diplom.setDopInfo(childElement.getTextContent());
				            }
				            if (isNew) {
				            	//novo e direktno dobavqme
								add=true;
							}else {
								//promqna e gledame kakva e promqnata
					            if (childElement.getTagName().equals("action") && childElement.getTextContent()!=null) {
					            	if (childElement.getTextContent().equals("Добави")) {
										add=true;
									}				            	
					            }		
							}
				        }       
				    }
					
					if (add) {
						//ako deistvieto e dobavqne						
						co.getCoachesDiploms().add(diplom);	
					}else {
						//mahame go
						for (int j = 0; j < co.getCoachesDiploms().size(); j++) {
							if (co.getCoachesDiploms().get(i).getRegNomer()!=null && diplom.getRegNomer()!=null && co.getCoachesDiploms().get(i).getRegNomer().equals(diplom.getRegNomer())) {
								co.getCoachesDiploms().remove(i);
								break;
							}
						}
					}
					
				}
			
				// tova pri vtoro zaqvlenie za vpisvane kogato ve4e imame trener ama iskame da mu dobavim diplomi.
				if (returnAfterDiplom) {
					form.close();
					return co;
				}
				
				co.setVidSport(codeVidSport);
				if (eElement.getElementsByTagName("dopInfo").item(0)!=null && !eElement.getElementsByTagName("dopInfo").item(0).getTextContent().trim().isEmpty()) {
					co.setDopInfo(eElement.getElementsByTagName("dopInfo").item(0).getTextContent());
				} 
				/****************** nachin na poluchavane ***************/		
				if (eElement.getElementsByTagName("nachinPost").item(0)!=null && !eElement.getElementsByTagName("nachinPost").item(0).getTextContent().trim().isEmpty()) {
					if (eElement.getElementsByTagName("nachinPost").item(0).getTextContent().equals("1")) {
						co.setNachinPoluch(DocuConstants.CODE_ZNACHENIE_PREDAVANE_POSHTA);
					}					
				}
				if (eElement.getElementsByTagName("nachinCCEB").item(0)!=null && !eElement.getElementsByTagName("nachinCCEB").item(0).getTextContent().trim().isEmpty()) {
					if (eElement.getElementsByTagName("nachinCCEB").item(0).getTextContent().equals("1")) {
						co.setNachinPoluch(DocuConstants.CODE_ZNACHENIE_PREDAVANE_SSEV);
					}					
				}
				if (eElement.getElementsByTagName("nachinByhand").item(0)!=null && !eElement.getElementsByTagName("nachinByhand").item(0).getTextContent().trim().isEmpty()) {
					if (eElement.getElementsByTagName("nachinByhand").item(0).getTextContent().equals("1")) {
						co.setNachinPoluch(DocuConstants.CODE_ZNACHENIE_PREDAVANE_NA_RAKA);
					}					
				}
				if (eElement.getElementsByTagName("nachinEmail").item(0)!=null && !eElement.getElementsByTagName("nachinEmail").item(0).getTextContent().trim().isEmpty()) {
					if (eElement.getElementsByTagName("nachinEmail").item(0).getTextContent().equals("1")) {
						co.setNachinPoluch(DocuConstants.CODE_ZNACHENIE_PREDAVANE_EMAIL);
					}					
				}
				if (eElement.getElementsByTagName("nachinDrug").item(0)!=null && !eElement.getElementsByTagName("nachinDrug").item(0).getTextContent().trim().isEmpty()) {
					if (eElement.getElementsByTagName("nachinDrug").item(0).getTextContent().equals("1")) {
						co.setNachinPoluch(DocuConstants.CODE_ZNACHENIE_PREDAVANE_DRUG);
					}					
				}
				for (int i = 0; i < eElement.getElementsByTagName("nachinPoluchavane").item(0).getChildNodes().getLength(); i++) {
					Node childNode = eElement.getElementsByTagName("nachinPoluchavane").item(0).getChildNodes().item(i);
					while( childNode.getNextSibling()!=null ){          
				        childNode = childNode.getNextSibling();  
				        if (childNode.getNodeType() == Node.ELEMENT_NODE) {         
				            Element childElement = (Element) childNode; 
				            if (childElement.getTagName().equals("text")) {
				            	co.setNachinPoluchText(childElement.getTextContent());
				            	break;
				            }
				        }
					}
				}
/********************** krai nachin na poluchavane **************/
				 
				
			}
		}
		form.close();
		return co;
	}
	
	
	
	
	public Integer decodeByClassif(Integer codeClassif, String textString, Integer lang, SystemData sd) throws DbErrorException {
		for (int j = 0; j < sd.getSysClassification(codeClassif, new Date(), lang).size(); j++) {
			if (sd.getSysClassification(codeClassif, new Date(), lang).get(j).getTekst().toLowerCase().equals(textString.toLowerCase())) {
				return sd.getSysClassification(codeClassif, new Date(), lang).get(j).getCode();
			}
		}
		return null;
	}
	
	public void findVidSportList(List<Integer> vidSportL,Element eElement, SystemData sd, Integer lang, Boolean isNew) throws DbErrorException, DOMException {
		
		if (eElement.getElementsByTagName("vidSportList").item(0)!=null && !eElement.getElementsByTagName("vidSportList").item(0).getTextContent().trim().isEmpty()) {
			for (int i = 0; i < eElement.getElementsByTagName("vidSportList").getLength(); i++) {
				Node childNode = eElement.getElementsByTagName("vidSportList").item(i);
				
				
				
				for (int j = 0; j < sd.getSysClassification(DocuConstants.CODE_CLASSIF_VIDOVE_SPORT, new Date(), lang).size(); j++) {
					if (sd.getSysClassification(DocuConstants.CODE_CLASSIF_VIDOVE_SPORT, new Date(), lang).get(j).getTekst().equals(eElement.getElementsByTagName("vidSportList").item(i).getTextContent())) {
						// namerili sme go 
						//ako e nov obekt direktno dobavqme
						if (isNew) {
							vidSportL.add(sd.getSysClassification(DocuConstants.CODE_CLASSIF_VIDOVE_SPORT, new Date(), lang).get(j).getCode());	
						}else {
							
							// zna4i sme v promqna gledame kakva e i togava pravim nujnoto
							while( childNode.getNextSibling()!=null ){          
						        childNode = childNode.getNextSibling();         
						        if (childNode.getNodeType() == Node.ELEMENT_NODE) {         
						            Element childElement = (Element) childNode; 
						            if (childElement.getTagName().equals("action")) {
						            	if (childElement.getTextContent()!=null) {
											if (childElement.getTextContent().equals("Добави")) {
												vidSportL.add(sd.getSysClassification(DocuConstants.CODE_CLASSIF_VIDOVE_SPORT, new Date(), lang).get(j).getCode());
												break;
											}
											if (childElement.getTextContent().equals("Премахни") || childElement.getTextContent().equals("Изтрий")) {
												for (int l = 0; l < vidSportL.size(); l++) {
													if (vidSportL.get(l)==sd.getSysClassification(DocuConstants.CODE_CLASSIF_VIDOVE_SPORT, new Date(), lang).get(j).getCode()) {
														vidSportL.remove(l);
														break;
													}
												}
											}
										}							            	
									}
						        }       
							}
						}
						
						break;
					}
				}				
			}
		}
	}
 
	public Referent findReferent(Referent ref, String eik, String egn, String lnch, SystemData sd,Element eElement, Integer lang, ActiveUser ac, boolean setKorespAddress, boolean zaqvlDelete) throws DbErrorException, RegixClientException, DatatypeConfigurationException {
		ref=new ReferentDAO(ac).findByIdent(eik, egn, lnch, null);	
		
		//ako e iztrivane vrashtame kakvoto sme namerili bilo to i null
		if (zaqvlDelete) {
			return ref;
		}
		boolean save=false;
		 
		if (ref!=null && ref.getCode()!=null) {
			//slagame pak novite danni za korespondenciq
			if (setKorespAddress) {
				ref=setKorespAdressData(ref, sd, eElement, lang);
				save=true;
			}
		}else {
			ref=new Referent();
			if (eik !=null || (sd.getSettingsValue("REGIX_ESGRAON_ACTIVE")!=null && sd.getSettingsValue("REGIX_ESGRAON_ACTIVE").equals("true"))) {
				// Hodim i tarsim v REGIX
					if (eik!=null) {
						
						RegixUtils.loadUridLiceByEik(ref, eik, sd);
						ref.setRefType(DocuConstants.CODE_ZNACHENIE_REF_TYPE_NFL);
					}else {
						RegixUtils.loadFizLiceByEgn(ref, egn, true, true, sd);
						ref.setRefType(CODE_ZNACHENIE_REF_TYPE_FZL);
					}
				if (ref!=null && ref.getRefName()!=null && !ref.getRefName().trim().isEmpty()) {
	
					// kogato idva ot REGIX ne zamenqme adres za koresp. ako kajat da go smenqme razkomentirasht tuk tezi 3 reda.

					//namerili sme go shte go zapishem ama sled kato mu slojim dannite za adres ot zaqvlenieto.
	//				if (setKorespAddress) {
	//					ref=setKorespAdressData(ref, sd, eElement, lang);
	//				}
					save=true;
				}else {
					//regix ne e върнал фирма очевидно е просто валидно еик.
					return null;
				}
			}else {
				// po EGN sme, no ne sme hodili v REGIX за това правим нов референт с данните от пдф-а
				ref.setRefType(CODE_ZNACHENIE_REF_TYPE_FZL);
				 ref.setFzlEgn(egn);
				 String refName="";
				if (eElement!=null && eElement.getElementsByTagName("ime").item(0)!=null && eElement.getElementsByTagName("ime").item(0)!=null && !eElement.getElementsByTagName("ime").item(0).getTextContent().trim().isEmpty()) {
					ref.setIme(eElement.getElementsByTagName("ime").item(0).getTextContent());
					refName=ref.getIme();
				}
				if (eElement!=null && eElement.getElementsByTagName("prezime").item(0)!=null && eElement.getElementsByTagName("prezime").item(0)!=null && !eElement.getElementsByTagName("prezime").item(0).getTextContent().trim().isEmpty()) {
					ref.setPrezime(eElement.getElementsByTagName("prezime").item(0).getTextContent());
					if (refName.length()>0) {
						refName+=" "+ref.getPrezime();	
					}
				}
				if (eElement!=null && eElement.getElementsByTagName("familia").item(0)!=null && eElement.getElementsByTagName("familia").item(0)!=null && !eElement.getElementsByTagName("familia").item(0).getTextContent().trim().isEmpty()) {
					ref.setFamilia(eElement.getElementsByTagName("familia").item(0).getTextContent());
					if (refName.length()>0) {
						refName+=" "+ref.getFamilia();	
					}
				}
				ref.setRefName(refName);
				if (eElement!=null && eElement.getElementsByTagName("doctNumber").item(0)!=null && eElement.getElementsByTagName("doctNumber").item(0)!=null && !eElement.getElementsByTagName("doctNumber").item(0).getTextContent().trim().isEmpty()) {
					ref.setNomDoc(eElement.getElementsByTagName("doctNumber").item(0).getTextContent());
				}
				ref=setKorespAdressData(ref, sd, eElement, lang);
				ref.setAddress(ref.getAddressKoresp());
				save=true;
			}
		}
		
		
		if (eElement!=null && eElement.getElementsByTagName("polza").item(0)!=null && eElement.getElementsByTagName("polza").item(0)!=null && !eElement.getElementsByTagName("polza").item(0).getTextContent().trim().isEmpty()) {
			ref.setPolza(decodeByClassif(DocuConstants.CODE_CLASSIF_MMS_POLZA, eElement.getElementsByTagName("polza").item(0).getTextContent(), lang, sd));	
		}
		
		if (save) {
			//zapisvame go nov ili ne ima promqna eventualno v adresnite danni.
			try {
				JPA.getUtil().begin();
				
				ref = new ReferentDAO(ac).save(ref);
				
				JPA.getUtil().commit();
			} catch (DbErrorException e) {
				e.printStackTrace();
				JPA.getUtil().rollback();
			}
			sd.mergeReferentsClassif(ref, false );	
		}
		return ref;
		
	}

	public Referent setKorespAdressData(Referent ref, SystemData sd, Element eElement, Integer lang) throws DbErrorException, DOMException {
		if(ref.getAddressKoresp() == null) {
			ref.setAddressKoresp(new ReferentAddress());
			ref.getAddressKoresp().setAddrType(DocuConstants.CODE_ZNACHENIE_ADDR_TYPE_CORRESP);
			ref.getAddressKoresp().setAddrCountry(Integer.parseInt(sd.getSettingsValue("delo.countryBG")));
		}
		if (eElement.getElementsByTagName("cboSettl").item(0)!=null && !eElement.getElementsByTagName("cboSettl").item(0).getTextContent().trim().isEmpty()
				&& eElement.getElementsByTagName("cboObsht").item(0)!=null && !eElement.getElementsByTagName("cboObsht").item(0).getTextContent().trim().isEmpty()
				&&	eElement.getElementsByTagName("cboObl").item(0)!=null && !eElement.getElementsByTagName("cboObl").item(0).getTextContent().trim().isEmpty()) {
			
			
			Integer ek=	new ReferentDAO(ActiveUser.DEFAULT).findEkatteByEkatteNames(
					eElement.getElementsByTagName("cboObl").item(0).getTextContent(),
					eElement.getElementsByTagName("cboObsht").item(0).getTextContent(),
					eElement.getElementsByTagName("cboSettl").item(0).getTextContent());
			 
			if (ek!=null) {
				ref.getAddressKoresp().setEkatte(ek);
			}
		}
		if (eElement.getElementsByTagName("postCode").item(0)!=null && !eElement.getElementsByTagName("postCode").item(0).getTextContent().trim().isEmpty()) {
			ref.getAddressKoresp().setPostCode(eElement.getElementsByTagName("postCode").item(0).getTextContent());	
		} 
		if (eElement.getElementsByTagName("addrText").item(0)!=null && !eElement.getElementsByTagName("addrText").item(0).getTextContent().trim().isEmpty()) {
			ref.getAddressKoresp().setAddrText(eElement.getElementsByTagName("addrText").item(0).getTextContent());
		} 
		if (eElement.getElementsByTagName("contactPhone").item(0)!=null && !eElement.getElementsByTagName("contactPhone").item(0).getTextContent().trim().isEmpty()) {
			ref.setContactPhone(eElement.getElementsByTagName("contactPhone").item(0).getTextContent());
		} 
		if (eElement.getElementsByTagName("contactEmail").item(0)!=null && !eElement.getElementsByTagName("contactEmail").item(0).getTextContent().trim().isEmpty()) {
			ref.setContactEmail(eElement.getElementsByTagName("contactEmail").item(0).getTextContent());
		} 
		if (eElement.getElementsByTagName("webPage").item(0)!=null && !eElement.getElementsByTagName("webPage").item(0).getTextContent().trim().isEmpty()) {
			ref.setWebPage(eElement.getElementsByTagName("webPage").item(0).getTextContent());
		} 
		return ref;
		
	}
}
