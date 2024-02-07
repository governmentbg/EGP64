package com.ib.docu.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.aspose.pdf.ImageFormat;
import com.aspose.pdf.TextAbsorber;
import com.aspose.pdf.XImage;
import com.ib.docu.db.dto.EgovMessages;
import com.ib.docu.db.dto.EgovMessagesFiles;
import com.ib.docu.db.dto.MMSCoaches;
import com.ib.docu.db.dto.MMSSportObekt;
import com.ib.docu.db.dto.MMSSportObektLice;
import com.ib.docu.db.dto.MMSSportnoObedinenie;
import com.ib.docu.db.dto.MMSsportFormirovanie;
import com.ib.docu.system.SystemData;
import com.ib.system.ActiveUser;
import com.ib.system.db.JPA;
import com.ib.system.exceptions.BaseException;
import com.ib.system.exceptions.DbErrorException;

import bg.government.regixclient.RegixClientException;

public class TestPdfParse {

	@Test
	public void readInfoFromPdf() throws ParserConfigurationException, DbErrorException, DOMException, SAXException, IOException {
		String path = "C:\\Users\\s.marinov\\Desktop\\MMS\\testPdf\\AAA\\";
		// Open document
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		
		DocumentBuilder db = dbf.newDocumentBuilder();
		
		com.aspose.pdf.facades.Form form = new com.aspose.pdf.facades.Form();
		
		form.bindPdf(path + "NOUS.pdf");
		//кода на услугата 
		if (form.getDocument()==null || form.getDocument().getInfo()==null || form.getDocument().getInfo().getTitle()==null) {
			form.close();
		}
		String[] s=new String[2];
		s[0]=form.getDocument().getInfo().getTitle().substring(0, form.getDocument().getInfo().getTitle().indexOf("ZVLN"))+"ZVLN";
		
		
		//ако вида е заличаване на обед/формиров тогава търсим ЕИК
//		if (go && new SystemData().getItemsByCodeExt(DocuConstants.CODE_CLASSIF_DOC_VID, form.getDocument().getInfo().getTitle(), 1, new Date()).get(0).getCode()==DocuConstants.CODE_ZNACHENIE_VID_DOC_ZAIAVL_ZALICH_SPORTNO_OBEDINENIE) {
			
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
						} 
					}
				}
			}
			
//		}
		
		
		form.close();
		System.out.println(s[0]);
		System.out.println(s[1]);
	}
	
	@Test
	public void parsePdf() {
		String path = "C:\\Users\\s.marinov\\Desktop\\MMS\\testPdf\\AAA\\";
		String imeFile="trenerAdres";
		// Open document
		com.aspose.pdf.facades.Form form = new com.aspose.pdf.facades.Form();
		form.bindPdf(path + imeFile+".pdf");
		System.out.println(form.getAttachmentName());
		System.out.println(form.getDocument().getInfo().getTitle());
		
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			 
			
			FileOutputStream xmlOutputStream;
			
			xmlOutputStream = new FileOutputStream(path + imeFile+".xml");
			form.exportXml(xmlOutputStream);
			System.out.println(form.getDocument().getForm().getFields()[1]);
			//exportXml(xmlOutputStream);
			
			xmlOutputStream.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

		// Close the document
		form.close();

	}
	@Test
	public void readPdfAsStream() throws IOException {
		
		String path = "D:\\VaskoPdf\\";
		String imeFile="502-ac3b5ae9-11f8-49d8-b696-4250fbdeec02-ZVLN";
		// Open document
		com.aspose.pdf.Document pdfDocument = new com.aspose.pdf.Document(path + imeFile+".pdf");
	
		// Extract a particular image

for (int i = 1; i < 2195; i++) {
	System.out.println(         pdfDocument.getPages().get_Item(1).getContents().get_Item(i));	
}

//        String outputLogName = path + "ua-20.xml";
//
//
//        Boolean isValid = pdfDocument.validate(outputLogName, PdfFormat.PDF_UA_1);
//
//        System.out.println(isValid);
		
	}

	@Test
	public void unmarshalXML() {
		String path = "C:\\Users\\s.marinov\\Desktop\\MMS\\testPdf\\";

		try {
			// Create XML file.
			FileInputStream is = new FileInputStream(path + "input1312.xml");

			JAXBContext jc = JAXBContext.newInstance(Form1.class);
			Unmarshaller u = jc.createUnmarshaller();

			Form1 sportnoObedinenie = (Form1) u.unmarshal(is);

			System.out.println(sportnoObedinenie.getZvln().getZvlnDklr().getMain().getVidOrNflEikOrAddressKoresp()
					.get(0).getValue());

			// Close file stream
//		        xmlOutputStream.close();

		} catch (IOException e) {

			e.printStackTrace();
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	@Test
	public void testReadXML() {
		try {
			String path = "C:\\Users\\s.marinov\\Desktop\\MMS\\testPdf\\AAA\\";
			// creating a constructor of file class and parsing an XML file
			File file = new File(path + "obektV.xml");
			// an instance of factory that gives a document builder
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			// an instance of builder to parse the specified xml file
			DocumentBuilder db = dbf.newDocumentBuilder();

			
			//GETTIGN IT FROM DB
			/**
			EgovMessagesFiles file=new EgovMessagesFiles();
			com.aspose.pdf.facades.Form form = new com.aspose.pdf.facades.Form();
			List<EgovMessagesFiles> files = new EgovMessagesDAO(ActiveUser.DEFAULT).findFilesByMessage(300042);
			for (int i = 0; i < files.size(); i++) {
				if (files.get(i).getMime().contains("application/pdf")) {
					file=files.get(i);
					form.bindPdf(new ByteArrayInputStream(file.getBlobcontent()));
					
				}
			}
			ByteArrayOutputStream out = new ByteArrayOutputStream();
				// Export data
				form.exportXml(out);
				 ByteArrayInputStream  is=new ByteArrayInputStream( out.toByteArray() );
			Document doc = db.parse(is);
			**/
			
			//ot diska za testove e po barzo
			Document doc = db.parse(file);
			
			doc.getDocumentElement().normalize();
			
//			System.out.println("Root element: " + doc.getDocumentElement().getNodeName());
			
			
			NodeList nodeList = doc.getElementsByTagName("Zvln");
			// nodeList is not iterable, so we are using for loop
			for (int itr = 0; itr < nodeList.getLength(); itr++) {
				Node node = nodeList.item(itr);
//				System.out.println("\nNode Name :" + node.getNodeName());
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) node;
					
//					readElements(eElement);
//					System.out.println("txtEDeliveryDestination: " + eElement.getElementsByTagName("txtEDeliveryDestination").item(0).getTextContent());
//					System.out.println(
//							"First Name: " + eElement.getElementsByTagName("zaiavitel").item(0).getTextContent());
//					System.out.println("predsedatel: " + eElement.getElementsByTagName("predsedatel").item(0).getTextContent());
//					System.out.println("addrText: " + eElement.getElementsByTagName("addrText").item(0).getTextContent());
//					System.out.println("nflEik: " + eElement.getElementsByTagName("nflEik").item(0).getTextContent());
					
					
					
					String mainEik=null;
					List<String> otherEikList=new ArrayList<String>();
					for (int i = 0; i < eElement.getElementsByTagName("nflEik").getLength(); i++) {
						if (eElement.getElementsByTagName("nflEik").item(i).getParentNode().getNodeName().equals("Main")) {
							mainEik=eElement.getElementsByTagName("nflEik").item(i).getTextContent();
						}else {
							otherEikList.add(eElement.getElementsByTagName("nflEik").item(i).getTextContent());

							Node childNode = eElement.getElementsByTagName("nflEik").item(i);  
							while( childNode.getNextSibling()!=null ){          
						        childNode = childNode.getNextSibling();         
						        if (childNode.getNodeType() == Node.ELEMENT_NODE) {         
						            Element childElement = (Element) childNode;             
						            System.out.println("NODE num:-" + childElement.getTagName());
						            break;
						        }       
						    }
						}
							
					}
					for (int i = 0; i < eElement.getElementsByTagName("ime").getLength(); i++) {	
						Node childNode = eElement.getElementsByTagName("ime").item(i);
						System.out.println("************"+childNode.getNodeName());
						String egn=null;
						
						MMSSportObektLice l=new MMSSportObektLice();
						boolean add=false;
						 
						while( childNode.getNextSibling()!=null ){          
					        childNode = childNode.getNextSibling();    
					        System.out.println("----------"+childNode.getNodeName());
						}
					}
					
					MMSsportFormirovanie fo=new MMSsportFormirovanie();
					
					
					
					//TODO Валидации на ЕИК
					
//					Referent ref=new ReferentDAO(ActiveUser.DEFAULT).findByIdent(mainEik, null, null, null);
					
//					if (ref!=null && ref.getCode()!=null) {
//						//tarsim dali go nqma v bazata ve4e
//						//TODO ACTIVEUSER-a
//						fo=new MMSsportFormirovanieDAO(MMSsportFormirovanie.class, ActiveUser.DEFAULT).findByIdObject(ref.getCode());
//						if (fo!=null && fo.getId()!=null) {
//							//namerili sme go return predpolagam 
//							return fo;
//						}
//						
//					}else {
//						
//					}
//					
//					if (ref==null || ref.getCode()==null) {
//						// Hodim i tarsim v REGIX
//						RegixUtils.loadUridLiceByEik(ref, mainEik, systemData);
//					}
					
//					fo.setPredsedatel(eElement.getElementsByTagName("predsedatel").item(0).getTextContent());
					
					
//					if (eElement.getElementsByTagName("nachinPost").item(0)!=null && !eElement.getElementsByTagName("nachinPost").item(0).getTextContent().trim().isEmpty()) {
//						if (eElement.getElementsByTagName("nachinPost").item(0).getTextContent().equals("1")) {
//							System.out.println("nachinPost");
//						}					
//					}
//					if (eElement.getElementsByTagName("nachinCCEB").item(0)!=null && !eElement.getElementsByTagName("nachinCCEB").item(0).getTextContent().trim().isEmpty()) {
//						if (eElement.getElementsByTagName("nachinCCEB").item(0).getTextContent().equals("1")) {
//							System.out.println("nachinCCEB");
//						}					
//					}
//					if (eElement.getElementsByTagName("nachinByhand").item(0)!=null && !eElement.getElementsByTagName("nachinByhand").item(0).getTextContent().trim().isEmpty()) {
//						if (eElement.getElementsByTagName("nachinByhand").item(0).getTextContent().equals("1")) {
//							System.out.println("nachinByhand");
//						}					
//					}
//					if (eElement.getElementsByTagName("nachinEmail").item(0)!=null && !eElement.getElementsByTagName("nachinEmail").item(0).getTextContent().trim().isEmpty()) {
//						if (eElement.getElementsByTagName("nachinEmail").item(0).getTextContent().equals("1")) {
//							System.out.println("nachinEmail");
//						}					
//					}
//					if (eElement.getElementsByTagName("nachinDrug").item(0)!=null && !eElement.getElementsByTagName("nachinDrug").item(0).getTextContent().trim().isEmpty()) {
//						if (eElement.getElementsByTagName("nachinDrug").item(0).getTextContent().equals("1")) {
//							System.out.println("nachinDrug");
//						}					
//					}
//					for (int i = 0; i < eElement.getElementsByTagName("nachinPoluchavane").item(0).getChildNodes().getLength(); i++) {
//						Node childNode = eElement.getElementsByTagName("nachinPoluchavane").item(0).getChildNodes().item(i);
//						while( childNode.getNextSibling()!=null ){          
//					        childNode = childNode.getNextSibling();  
//					        if (childNode.getNodeType() == Node.ELEMENT_NODE) {         
//					            Element childElement = (Element) childNode; 
//					            if (childElement.getTagName().equals("text")) {
//					            	System.out.println(childElement.getTextContent());
//					            }
//					        }
//						}
//					}
//				
//					for (int i = 0; i < eElement.getElementsByTagName("vid_sport").getLength(); i++) {
//						System.out.println("***********"+eElement.getElementsByTagName("vid_sport").item(i).getTextContent());
////						if (eElement.getElementsByTagName("vid_sport").item(0)!=null && !eElement.getElementsByTagName("vid_sport").item(0).getTextContent().trim().isEmpty()) {
////							System.out.println(eElement.getElementsByTagName("vid_sport").item(i).getTextContent());
////							codeVidSport=decodeByClassif(DocuConstants.CODE_CLASSIF_VIDOVE_SPORT, eElement.getElementsByTagName("vid_sport").item(i).getTextContent(), lang, sd);
////							 if (codeVidSport!=null) {
////								break;
////							}
////						}
//					}
				}
			}
			 
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			JPA.getUtil().closeConnection();
		}
	}
	
	public void readElements(Element eElement) {
		if (eElement.getChildNodes().getLength()>0) {
			for (int itr = 0; itr < eElement.getChildNodes().getLength(); itr++) {
				if (eElement.getChildNodes().item(itr).getChildNodes().getLength()>0) {
					readElements((Element)eElement.getChildNodes().item(itr));
				}else {
//					System.out.println(eElement.getTagName()+": "+ eElement.getChildNodes().item(itr).getNodeValue());
				}
			}
		}
	}
	@Test
	public void testObed() {
		try {
			
			EgovMessages mess=new EgovMessages();
			mess.setDocVid("36");
			List<EgovMessagesFiles> files=new ArrayList<EgovMessagesFiles>();
			
			String path = "C:\\Users\\s.marinov\\Desktop\\MMS\\testPdf\\AAA\\";
			// creating a constructor of file class and parsing an XML file
			File file = new File(path + "fedMail.pdf");
			EgovMessagesFiles f=new EgovMessagesFiles();
			f.setMime("application/pdf");
			FileInputStream in=new FileInputStream(file);
			f.setBlobcontent(in.readAllBytes());
			files.add(f);
			
			MMSSportnoObedinenie o=	new ParsePdfZaqvlenie().parseObedinenie(new SystemData(), ActiveUser.DEFAULT, 1, mess,files);
			
			in.close();
			System.out.println(o.getDopInfo());
			if (o.getParseMessages().size()>0) {
				for (int i = 0; i < o.getParseMessages().size(); i++) {
					System.out.println("ERROR: " + o.getParseMessages().get(i));
				}
			}
			
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RegixClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DatatypeConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@Test
	public void testObekt() {
		try {
			
			EgovMessages mess=new EgovMessages();
			mess.setDocVid("36");
			List<EgovMessagesFiles> files=new ArrayList<EgovMessagesFiles>();
			
			String path = "C:\\Users\\s.marinov\\Desktop\\MMS\\testPdf\\New100323\\Vpisvane\\";
			// creating a constructor of file class and parsing an XML file
			File file = new File(path + "1201001ZVLNv01-Заявление за вписване на спортен обект_signed.pdf");
			EgovMessagesFiles f=new EgovMessagesFiles();
			f.setMime("application/pdf");
			FileInputStream in=new FileInputStream(file);
			f.setBlobcontent(in.readAllBytes());
			files.add(f);
			
			MMSSportObekt o=	new ParsePdfZaqvlenie().parseObekt(new SystemData(), ActiveUser.DEFAULT, 1, mess,files);
			
//			in.close();
			System.out.println(o.getSpObLice().get(0).getTypeVrazka());
			if (o.getParseMessages().size()>0) {
				for (int i = 0; i < o.getParseMessages().size(); i++) {
					System.out.println("ERROR: " + o.getParseMessages().get(i));
				}
			}
			
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RegixClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DatatypeConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@Test
	public void testTrener() {
		try {
			
			EgovMessages mess=new EgovMessages();
			mess.setDocVid("54");
			List<EgovMessagesFiles> files=new ArrayList<EgovMessagesFiles>();
			
			String path = "C:\\Users\\s.marinov\\Desktop\\MMS\\testPdf\\AAA\\";
			// creating a constructor of file class and parsing an XML file
			File file = new File(path + "vpisTrener.pdf");
			EgovMessagesFiles f=new EgovMessagesFiles();
			f.setMime("application/pdf");
			FileInputStream in=new FileInputStream(file);
			f.setBlobcontent(in.readAllBytes());
			files.add(f);
			
			MMSCoaches o=	new ParsePdfZaqvlenie().parseTrener(new SystemData(), ActiveUser.DEFAULT, 1, mess,files);
			
			in.close();
			System.out.println("Вид спорт: "+o.getVidSport());
			if (o.getParseMessages().size()>0) {
				for (int i = 0; i < o.getParseMessages().size(); i++) {
					System.out.println("ERROR: " + o.getParseMessages().get(i));
				}
			}
			System.out.println("Nachninpoluch: "+o.getNachinPoluch());
			System.out.println("Nachninpoluch Text: "+o.getNachinPoluchText());
			
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RegixClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DatatypeConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
