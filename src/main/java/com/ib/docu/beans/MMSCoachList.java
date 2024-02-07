package com.ib.docu.beans;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.primefaces.component.export.PDFOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.docu.db.dao.MMSCoachesDAO;
import com.ib.docu.db.dto.MMSCoaches;
import com.ib.indexui.customexporter.CustomExpPreProcess;
import com.ib.indexui.pagination.LazyDataModelSQL2Array;
import com.ib.indexui.system.IndexUIbean;
import com.ib.indexui.utils.JSFUtils;
import com.ib.system.db.SelectMetadata;
import com.ib.system.utils.DateUtils;
import com.ib.system.utils.ValidationUtils;

@Named(value = "mmsCL")
@ViewScoped
public class MMSCoachList extends IndexUIbean   {

	/**
	 * Списък със спортни специалисти
	 * 
	 */
	private static final long serialVersionUID = -3291826591408971572L;
	private static final Logger LOGGER = LoggerFactory.getLogger(MMSCoachList.class);
	
	private String egn;
	private String lnch;
	private String nomDoc;
	private String ime;
	private String prezime;
	private String familia;
	private String rnZaiav;
	private Integer periodZaiav = null;
	private Date zaiavFrom;
	private Date zaiavTo;
	private Integer status;
	private Integer periodStatus = null;
	private Date statusFrom;
	private Date statusTo;
	
	private Integer statusVpis;
	private Integer periodStVpisvane = null;
	private Date fromStatusVpis;
	private Date toStatusVpis;
	private Integer periodStZaiavlenie = null;
	private Integer statusZaiav;
	private Date fromStatusZaiav;
	private Date toStatusZaiav;
	
	private LazyDataModelSQL2Array coachesList;	
	
	private Date decodeDate;
	
	/** 
	 * 
	 * 
	 **/
	@PostConstruct
	public void initData() {
		
		LOGGER.debug("PostConstruct!!!");
		
		actionClear();
		actionSearch();
	}
	
	/** 
	 * Списък със спортни специалисти по зададени критерии 
	 * 
	 */
	public void actionSearch(){
		
		SelectMetadata smd = new MMSCoachesDAO(MMSCoaches.class, getUserData()).buildQuery(egn, lnch, nomDoc, ime, prezime, familia, rnZaiav, zaiavFrom, zaiavTo, 
				status, statusFrom, statusTo, statusVpis, fromStatusVpis, toStatusVpis, statusZaiav, fromStatusZaiav, toStatusZaiav);

		String defaultSortColumn = "a13 desc";
		this.coachesList = new LazyDataModelSQL2Array(smd, defaultSortColumn);			
	} 
	
	public void actionChangeEgn() {
		
		if(egn != null && !"".equals(egn) && !ValidationUtils.isValidEGN(egn)) {
			JSFUtils.addMessage("formCoachList:egn",FacesMessage.SEVERITY_ERROR, getMessageResourceString(beanMessages, "refCorr.msgValidEgn"));
		}
	}
	
	public void actionChangeLnch() {
		
		if(lnch != null && !"".equals(lnch) && !ValidationUtils.isValidEGN(lnch)) {
			JSFUtils.addMessage("formCoachList:lnch",FacesMessage.SEVERITY_ERROR, getMessageResourceString(beanMessages, "refCorr.msgValidLnch"));
		}
	}
	
	/**
	 * премахва избраните критерии за търсене
	 */
	public void actionClear() {		
		
		egn = null;
		lnch = null;
		nomDoc = null;
		ime = null;
		prezime = null;
		familia = null;
		rnZaiav = null;
		periodZaiav = null;
		zaiavFrom = null;
		zaiavTo = null;
		status = null;
		periodStatus = null;
		statusFrom = null;
		statusTo = null;
		statusVpis = null;
		periodStVpisvane = null;
		fromStatusVpis = null;
		toStatusVpis = null;
		periodStZaiavlenie = null;
		statusZaiav = null;
		fromStatusZaiav = null;
		toStatusZaiav = null;
		
		this.coachesList = null;
	}
	
	/** Методи за смяна на датите при избор на период за търсене.
	 * 
	 * 
	 */
	public void changePeriodZaiav() {
		
    	if (this.periodZaiav != null) {
			Date[] di;
			di = DateUtils.calculatePeriod(this.periodZaiav);
			this.zaiavFrom = di[0];
			this.zaiavTo = di[1];		
    	} else {
    		this.zaiavFrom = null;
    		this.zaiavTo = null;			
		}
    }
	
	public void changeDateZaiav() { 
		this.setPeriodZaiav(null);
	}	
	
	public void changePeriodStatus() {
		
    	if (this.periodStatus != null) {
			Date[] di;
			di = DateUtils.calculatePeriod(this.periodStatus);
			this.statusFrom = di[0];
			this.statusTo = di[1];		
    	} else {
    		this.statusFrom = null;
    		this.statusTo = null;			
		}
    }
	
	public void changeDateStatus() { 
		this.setPeriodStatus(null);
	}	
	
	public void changePeriodStVpisvane() {
		
    	if (this.periodStVpisvane != null) {
			Date[] di;
			di = DateUtils.calculatePeriod(this.periodStVpisvane);
			this.fromStatusVpis = di[0];
			this.toStatusVpis = di[1];		
    	} else {
    		this.fromStatusVpis = null;
    		this.toStatusVpis = null;			
		}
    }
	
	public void changeDateStVpisvane() { 
		this.setPeriodStVpisvane(null); 
	}	
	
	public void changePeriodStZaiavlenie() {
		
    	if (this.periodStZaiavlenie != null) {
			Date[] di;
			di = DateUtils.calculatePeriod(this.periodStZaiavlenie);
			this.fromStatusZaiav = di[0];
			this.toStatusZaiav = di[1];		
    	} else {
    		this.fromStatusZaiav = null;
    		this.toStatusZaiav = null;			
		}
    }
	
	public void changeDateStZaiavlenie() { 
		this.setPeriodStZaiavlenie(null); 
	}	

	public String actionGoto(Integer idObj, int num) {
		
		if (num == 1) {
			return "mmsCoachEdit.xhtml?faces-redirect=true&idObj=" + idObj;
		} else {
			return "mmsCoachView.xhtml?faces-redirect=true&idObj=" + idObj;
		}
		
	}

	public String getEgn() {
		return egn;
	}

	public void setEgn(String egn) {
		this.egn = egn;
	}

	public String getLnch() {
		return lnch;
	}

	public void setLnch(String lnch) {
		this.lnch = lnch;
	}

	public String getNomDoc() {
		return nomDoc;
	}

	public void setNomDoc(String nomDoc) {
		this.nomDoc = nomDoc;
	}

	public String getIme() {
		return ime;
	}

	public void setIme(String ime) {
		this.ime = ime;
	}

	public String getPrezime() {
		return prezime;
	}

	public void setPrezime(String prezime) {
		this.prezime = prezime;
	}

	public String getFamilia() {
		return familia;
	}

	public void setFamilia(String familia) {
		this.familia = familia;
	}

	public String getRnZaiav() {
		return rnZaiav;
	}

	public void setRnZaiav(String rnZaiav) {
		this.rnZaiav = rnZaiav;
	}

	public Integer getPeriodZaiav() {
		return periodZaiav;
	}

	public void setPeriodZaiav(Integer periodZaiav) {
		this.periodZaiav = periodZaiav;
	}

	public Date getZaiavFrom() {
		return zaiavFrom;
	}

	public void setZaiavFrom(Date zaiavFrom) {
		this.zaiavFrom = zaiavFrom;
	}

	public Date getZaiavTo() {
		return zaiavTo;
	}

	public void setZaiavTo(Date zaiavTo) {
		this.zaiavTo = zaiavTo;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Integer getPeriodStatus() {
		return periodStatus;
	}

	public void setPeriodStatus(Integer periodStatus) {
		this.periodStatus = periodStatus;
	}

	public Date getStatusFrom() {
		return statusFrom;
	}

	public void setStatusFrom(Date statusFrom) {
		this.statusFrom = statusFrom;
	}

	public Date getStatusTo() {
		return statusTo;
	}

	public void setStatusTo(Date statusTo) {
		this.statusTo = statusTo;
	}

	public Integer getStatusVpis() {
		return statusVpis;
	}

	public void setStatusVpis(Integer statusVpis) {
		this.statusVpis = statusVpis;
	}

	public Integer getPeriodStVpisvane() {
		return periodStVpisvane;
	}

	public void setPeriodStVpisvane(Integer periodStVpisvane) {
		this.periodStVpisvane = periodStVpisvane;
	}

	public Date getFromStatusVpis() {
		return fromStatusVpis;
	}

	public void setFromStatusVpis(Date fromStatusVpis) {
		this.fromStatusVpis = fromStatusVpis;
	}

	public Date getToStatusVpis() {
		return toStatusVpis;
	}

	public void setToStatusVpis(Date toStatusVpis) {
		this.toStatusVpis = toStatusVpis;
	}

	public Integer getPeriodStZaiavlenie() {
		return periodStZaiavlenie;
	}

	public void setPeriodStZaiavlenie(Integer periodStZaiavlenie) {
		this.periodStZaiavlenie = periodStZaiavlenie;
	}

	public Integer getStatusZaiav() {
		return statusZaiav;
	}

	public void setStatusZaiav(Integer statusZaiav) {
		this.statusZaiav = statusZaiav;
	}

	public Date getFromStatusZaiav() {
		return fromStatusZaiav;
	}

	public void setFromStatusZaiav(Date fromStatusZaiav) {
		this.fromStatusZaiav = fromStatusZaiav;
	}

	public Date getToStatusZaiav() {
		return toStatusZaiav;
	}

	public void setToStatusZaiav(Date toStatusZaiav) {
		this.toStatusZaiav = toStatusZaiav;
	}

	public LazyDataModelSQL2Array getCoachesList() {
		return coachesList;
	}

	public void setCoachesList(LazyDataModelSQL2Array coachesList) {
		this.coachesList = coachesList;
	}

	public Date getDecodeDate() {
		return new Date(decodeDate.getTime()) ;
	}

	public void setDecodeDate(Date decodeDate) {
		this.decodeDate = decodeDate != null ? new Date(decodeDate.getTime()) : null;
	}
	
	/**
	 * за експорт в excel - добавя заглавие и дата на изготвяне на справката и др.
	 */
	public void postProcessXLS(Object coach) {
		
		String title = getMessageResourceString(LABELS, "mmsCL.reportTitle");		  
    	new CustomExpPreProcess().postProcessXLS(coach, title, dopInfoReport(), null, null);		
     
	}

	/**
	 * за експорт в pdf - добавя заглавие и дата на изготвяне на справката
	 */
	public void preProcessPDF(Object coach)  {
		
		try {
			
			String title = getMessageResourceString(LABELS, "mmsCL.reportTitle");		
			new CustomExpPreProcess().preProcessPDF(coach, title, dopInfoReport(), null, null);		
						
		} catch (UnsupportedEncodingException e) {
			LOGGER.error(e.getMessage(),e);			
		} catch (IOException e) {
			LOGGER.error(e.getMessage(),e);			
		} 
	}
	
	/**
	 * за експорт в pdf 
	 * @return
	 */
	public PDFOptions pdfOptions() {
		PDFOptions pdfOpt = new CustomExpPreProcess().pdfOptions(null, null, null);
        return pdfOpt;
	}
	
	/**
	 * подзаглавие за екпсорта 
	 */
	public Object[] dopInfoReport() {
		
		Object[] dopInf = null;
		dopInf = new Object[3];
		
		if(this.zaiavFrom != null && this.zaiavTo != null) {
			dopInf[0] = "период на подаване на заявлението: "+ DateUtils.printDate(this.zaiavFrom) + " - "+ DateUtils.printDate(this.zaiavTo);
		} 
		
		if(this.statusFrom != null && this.statusTo != null) {			
			dopInf[1] = "период на статус: "+ DateUtils.printDate(this.statusFrom) + " - "+ DateUtils.printDate(this.statusTo);
		} 
	
		return dopInf;
	}

}