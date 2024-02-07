package com.ib.docu.beans;


import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.primefaces.model.diagram.Connection;
import org.primefaces.model.diagram.DefaultDiagramModel;
import org.primefaces.model.diagram.DiagramModel;
import org.primefaces.model.diagram.Element;
import org.primefaces.model.diagram.connector.FlowChartConnector;
import org.primefaces.model.diagram.endpoint.BlankEndPoint;
import org.primefaces.model.diagram.endpoint.EndPoint;
import org.primefaces.model.diagram.endpoint.EndPointAnchor;
import org.primefaces.model.diagram.overlay.ArrowOverlay;
import org.primefaces.model.diagram.overlay.LabelOverlay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.docu.db.dao.ProcDefDAO;
import com.ib.docu.db.dto.ProcDefEtap;
import com.ib.indexui.system.IndexUIbean;
import com.ib.indexui.utils.JSFUtils;
import com.ib.system.db.JPA;
import com.ib.system.exceptions.DbErrorException;
import com.ib.system.utils.SearchUtils;



@Named
@ViewScoped
public class TestDiagram extends IndexUIbean implements Serializable {

	private static final Logger LOGGER = LoggerFactory.getLogger(TestDiagram.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = 4985013225316883670L;
	
	
	private DefaultDiagramModel model;

    @PostConstruct
    public void init() {
    	
    	Integer idProc = Integer.valueOf(JSFUtils.getRequestParameter("idProc"));	
    	
    	
    	generateModel(idProc);
    	


    }
    
    private void generateModel(Integer id) {
    	 
    	if(id==null) return;
    	 
    	 model  = new DefaultDiagramModel();
         model.setMaxConnections(-1);

         FlowChartConnector connector = new FlowChartConnector();
         connector.setPaintStyle("{stroke:'#C7B097',strokeWidth:2}");
         model.setDefaultConnector(connector);
         
    	try {
    		List<ProcDefEtap> listEtapi = new ProcDefDAO(getUserData()).selectDefEtapList(id, null);
			
    		int i =0;
			for(ProcDefEtap etap: listEtapi) {
				model.addElement(createElement(etap,i));
				i++;
			}
			
			//връзки 
			for(Element el:model.getElements()) {
				
				ProcDefEtap etap = (ProcDefEtap) el.getData();
				if(checkIf(etap)) {
					//TODO
					
					//ptri da
					createConnectionData(etap.getNextOk(),  el ,1, 0, "Да");
					
					
					//pri ne
					createConnectionData(etap.getNextNot(),  el ,2, 3, "Не");
					
					//pri opcionalen
					createConnectionData(etap.getNextOptional(),  el ,3, 3, "Опционален");
					
				} else {
					createConnectionData(etap.getNextOk(),  el ,1, 0, null);
				}
				
				
			}
			
			
		} catch (DbErrorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			JPA.getUtil().closeConnection();
		}
    	
    }
    
    private void createConnectionData(String next, Element el ,int fromPos, int toPos, String label) {
    	next = SearchUtils.trimToNULL(next);
    	if(next!=null) {
	    	String[] nextList = next.split(",");
			for(int j=0; j<nextList.length;j++) {
				Element nextEl = findElModel( Integer.valueOf(nextList[j]));
				if(nextEl!=null) {
					if(label!=null && label.equals("Не") && checkConect(el ,nextEl)) {
						model.connect(createConnectionModel(el.getEndPoints().get(fromPos), nextEl.getEndPoints().get(2), label));
					} else {
						model.connect(createConnectionModel(el.getEndPoints().get(fromPos), nextEl.getEndPoints().get(toPos), label));
					}
				}
			}
    	}
    }
    
    private boolean checkConect(Element el ,Element elNext) {
    	String nomerE = ((ProcDefEtap) el.getData()).getNomer().toString();
    	
    	ProcDefEtap e2 = (ProcDefEtap) elNext.getData();
		String aa = SearchUtils.trimToNULL(e2.getNextOk());
		//System.out.println("checkConect-- Nomer -> "+nomerE);
		//System.out.println("checkConect-- list -> "+aa);
		if(aa!=null) {
			String [] aaList = aa.split(",");
			
			for(String aas: aaList) {
				
				if(aas.equals(nomerE)) { //System.out.println("checkConect---> ok");
					return true;
				}
			}
		}
	//	System.out.println("checkConect---> no");
		return false;
    }
    
    private Element findElModel(Integer nomerEtap) {
    	
    	for(Element el:model.getElements()) {
			
			ProcDefEtap etap = (ProcDefEtap) el.getData();
			
			if(etap.getNomer().equals(nomerEtap)) {
				return el;
			}
    	}
			
    	return null;
    }
    
    
    private Element createElement(ProcDefEtap pEtap ,int pored) {
    	Element el = new Element(pEtap, "20em", (pored*10)+"em");
    	
    	el.addEndPoint(new BlankEndPoint(EndPointAnchor.TOP));
    	el.addEndPoint(new BlankEndPoint(EndPointAnchor.BOTTOM));
    	el.addEndPoint(new BlankEndPoint(EndPointAnchor.RIGHT));
    	el.addEndPoint(new BlankEndPoint(EndPointAnchor.LEFT));
    	el.addEndPoint(new BlankEndPoint(EndPointAnchor.TOP_RIGHT));
    	el.addEndPoint(new BlankEndPoint(EndPointAnchor.BOTTOM_RIGHT));
    	
    	if(checkIf(pEtap)) {
    		el.setStyleClass("ui-diagram-success");
    	} else {
    		// проверка за краен етап (от него не излизат връзки)
    		
    		if( SearchUtils.trimToNULL(pEtap.getNextNot()) == null &&
    				SearchUtils.trimToNULL(pEtap.getNextOk()) == null &&
    				SearchUtils.trimToNULL(pEtap.getNextOptional()) == null) {
    			el.setStyleClass("ui-diagram-end");
    			
    		}
    		
    	}
    	
    	return el;
    }
    
    private boolean checkIf(ProcDefEtap pEtap) {
    	
    	String nextNot = SearchUtils.trimToNULL(pEtap.getNextNot());
		String nextOptional = SearchUtils.trimToNULL(pEtap.getNextOptional());
		
		return (nextNot != null || nextOptional != null );
    }
    
    
    private Connection createConnectionModel(EndPoint from, EndPoint to, String label) {
        Connection conn = new Connection(from, to);
        conn.getOverlays().add(new ArrowOverlay(20, 20, 1, 1));

        if (label != null) {
            conn.getOverlays().add(new LabelOverlay(label, "flow-label", 0.5));
        }

        return conn;
    }
	
    public DiagramModel getModel() {
        return model;
    }
}