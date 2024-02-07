package com.ib.docu.beans;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.primefaces.component.export.PDFOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.docu.db.dao.MMSsportFormirovanieDAO;
import com.ib.docu.db.dto.MMSsportFormirovanie;
import com.ib.indexui.customexporter.CustomExpPreProcess;
import com.ib.indexui.pagination.LazyDataModelSQL2Array;
import com.ib.indexui.system.Constants;
import com.ib.indexui.system.IndexUIbean;
import com.ib.indexui.utils.JSFUtils;
import com.ib.system.db.SelectMetadata;
import com.ib.system.exceptions.DbErrorException;
import com.ib.system.utils.DateUtils;
import com.ib.system.utils.ValidationUtils;

@Named(value = "mmsSpFormList")
@ViewScoped
public class MMSSportniFormirovaniaList extends IndexUIbean   {

	/**
	 * Списък със спортни формирования
	 * 
	 */
	private static final long serialVersionUID = 1496621790768476714L;
	private static final Logger LOGGER = LoggerFactory.getLogger(MMSSportniFormirovaniaList.class);
	
	private String eik;
	private String name;
	private Integer ekatte;
	private Integer vidForm;
	private List<Integer> vidSportList;
	private String vidSportTxt;
	private String rnZaiav;
	private Integer periodZaiav = null;
	private Date zaiavFrom;
	private Date zaiavTo;
	private Integer status;
	private Integer periodStatus = null;
	private Date statusFrom;
	private Date statusTo;
	private Integer olimp;
	private String triCheckOlimp = "0";
	private Integer voenen;
	private String triCheckVoenen = "0";
	private Integer univers;
	private String triCheckUnivers = "0";
	
	private Integer statusVpis;
	private Integer periodStVpisvane = null;
	private Date fromStatusVpis;
	private Date toStatusVpis;
	private Integer periodStZaiavlenie = null;
	private Integer statusZaiav;
	private Date fromStatusZaiav;
	private Date toStatusZaiav;
	
	private LazyDataModelSQL2Array spFormList;	
	
	private Date decodeDate;
	private Integer regixDiff;
	
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
	 * Списък с формирования по зададени критерии 
	 * 
	 */
	public void actionSearch() { 
		
		settingOlimpic();
		settingVoenen();
		settingUnivers();
		
		try {			
			
			SelectMetadata smd = new MMSsportFormirovanieDAO(MMSsportFormirovanie.class, getUserData()).buildQuery(eik, name, ekatte, vidForm, vidSportList, rnZaiav, zaiavFrom, zaiavTo,
					status, statusFrom, statusTo, olimp, getSystemData(), statusVpis, fromStatusVpis, toStatusVpis, statusZaiav, fromStatusZaiav, toStatusZaiav, (regixDiff!=null && regixDiff==0) ? null : regixDiff,
							univers, voenen);
			String defaultSortColumn = "a9 desc";
			this.spFormList = new LazyDataModelSQL2Array(smd, defaultSortColumn);
		
		} catch (DbErrorException e) {
			LOGGER.error("Грешка при зареждане на спортни формирования по зададени критерии!", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());			
		}
	} 
	
	private void settingOlimpic() {
		if("0".equals(triCheckOlimp)) {
			this.olimp = null;
		} else if("1".equals(triCheckOlimp)){
			this.olimp = Integer.valueOf(Constants.CODE_ZNACHENIE_DA);
		} else if("2".equals(triCheckOlimp)){
			this.olimp = Integer.valueOf(Constants.CODE_ZNACHENIE_NE);
		}
	}
	private void settingVoenen() {
		if("0".equals(triCheckVoenen)) {
			this.voenen = null;
		} else if("1".equals(triCheckVoenen)){
			this.voenen = Integer.valueOf(Constants.CODE_ZNACHENIE_DA);
		} else if("2".equals(triCheckVoenen)){
			this.voenen = Integer.valueOf(Constants.CODE_ZNACHENIE_NE);
		}
	}
	private void settingUnivers() {
		if("0".equals(triCheckUnivers)) {
			this.univers = null;
		} else if("1".equals(triCheckUnivers)){
			this.univers = Integer.valueOf(Constants.CODE_ZNACHENIE_DA);
		} else if("2".equals(triCheckUnivers)){
			this.univers = Integer.valueOf(Constants.CODE_ZNACHENIE_NE);
		}
	}
	
	public void actionChangeEik() {
		
		if(eik != null && !"".equals(eik) && !ValidationUtils.isValidBULSTAT(eik)) {
			JSFUtils.addMessage("formSFList:eik",FacesMessage.SEVERITY_ERROR, getMessageResourceString(beanMessages, "refCorr.msgValidEik"));
		}
	}
	
	/**
	 * премахва избраните критерии за търсене
	 */
	public void actionClear() {		
		
		this.eik = null;
		this.name = null;
		this.ekatte = null;
		this.vidForm = null;
		this.vidSportList = new ArrayList<>();
		this.vidSportTxt = null;
		this.rnZaiav = null;
		this.periodZaiav = null;
		this.zaiavFrom = null;
		this.zaiavTo = null;
		this.status = null;
		this.periodStatus = null;
		this.statusFrom = null;
		this.statusTo = null;
		this.olimp = null;
		this.triCheckOlimp = "0";
		this.voenen = null;
		this.triCheckVoenen = "0";
		this.univers = null;
		this.triCheckUnivers = "0";
		this.statusVpis = null;
		this.periodStVpisvane = null;
		this.fromStatusVpis = null;
		this.toStatusVpis = null;
		this.periodStZaiavlenie = null;
		this.statusZaiav = null;
		this.fromStatusZaiav = null;
		this.toStatusZaiav = null;
		
		this.spFormList = null;
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
			return "mmsSportFormirovanieEdit.xhtml?faces-redirect=true&idObj=" + idObj;
		} else {
			return "mmsSportFormirovanieView.xhtml?faces-redirect=true&idObj=" + idObj;
		}
		
	}

	public String getEik() {
		return eik;
	}

	public void setEik(String eik) {
		this.eik = eik;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getEkatte() {
		return ekatte;
	}

	public void setEkatte(Integer ekatte) {
		this.ekatte = ekatte;
	}	

	public Integer getVidForm() {
		return vidForm;
	}

	public void setVidForm(Integer vidForm) {
		this.vidForm = vidForm;
	}

	public List<Integer> getVidSportList() {
		return vidSportList;
	}

	public void setVidSportList(List<Integer> vidSportList) {
		this.vidSportList = vidSportList;
	}

	public String getVidSportTxt() {
		return vidSportTxt;
	}

	public void setVidSportTxt(String vidSportTxt) {
		this.vidSportTxt = vidSportTxt;
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

	public Integer getOlimp() {
		return olimp;
	}

	public void setOlimp(Integer olimp) {
		this.olimp = olimp;
	}	

	public String getTriCheckOlimp() {
		return triCheckOlimp;
	}

	public void setTriCheckOlimp(String triCheckOlimp) {
		this.triCheckOlimp = triCheckOlimp;
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

	public LazyDataModelSQL2Array getSpFormList() {
		return spFormList;
	}

	public void setSpFormList(LazyDataModelSQL2Array spFormList) {
		this.spFormList = spFormList;
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
	public void postProcessXLS(Object spForm) {
		
		String title = getMessageResourceString(LABELS, "mmsSpFormList.reportTitle");		  
    	new CustomExpPreProcess().postProcessXLS(spForm, title, dopInfoReport(), null, null);		
     
	}
	
	/**
	 * за експорт в pdf - добавя заглавие и дата на изготвяне на справката
	 */
	public void preProcessPDF(Object spForm)  {
		
		try {
			
			String title = getMessageResourceString(LABELS, "mmsSpFormList.reportTitle");		
			new CustomExpPreProcess().preProcessPDF(spForm, title, dopInfoReport(), null, null);		
						
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

	public Integer getRegixDiff() {
		return regixDiff;
	}

	public void setRegixDiff(Integer regixDiff) {
		this.regixDiff = regixDiff;
	}

	public Integer getUnivers() {
		return univers;
	}

	public void setUnivers(Integer univers) {
		this.univers = univers;
	}

	public String getTriCheckUnivers() {
		return triCheckUnivers;
	}

	public void setTriCheckUnivers(String triCheckUnivers) {
		this.triCheckUnivers = triCheckUnivers;
	}

	public Integer getVoenen() {
		return voenen;
	}

	public void setVoenen(Integer voenen) {
		this.voenen = voenen;
	}

	public String getTriCheckVoenen() {
		return triCheckVoenen;
	}

	public void setTriCheckVoenen(String triCheckVoenen) {
		this.triCheckVoenen = triCheckVoenen;
	}

}