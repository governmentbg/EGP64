package com.ib.docu.db.dto;

import static javax.persistence.GenerationType.SEQUENCE;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.ib.docu.system.DocuConstants;
import com.ib.system.db.JournalAttr;

	/**
	 * Coaches Diploms
	 *
	 * @author IvanT
	 */
	@Entity
	@Table(name = "mms_coaches_diploms")
	public class MMSCoachesDiploms implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = -5497521613451988642L;

		@SequenceGenerator(name = "MMSCoachesDiploms", sequenceName = "seq_mms_coaches_diploms", allocationSize = 1)
		@Id
		@GeneratedValue(strategy = SEQUENCE, generator = "MMSCoachesDiploms")
		@Column(name = "ID", unique = true, nullable = false)
		private Integer id;

		@JournalAttr(label = "idCoaches", defaultText = "Ид на треньор")
		@Column(name = "id_coaches", insertable = false, updatable = false)
		private Integer idCoaches;

		@JournalAttr(label = "regNomer", defaultText = "Рег. номер документ")
		@Column(name = "reg_nomer")
		private String regNomer;

		@JournalAttr(label = "yearIssued", defaultText = "Година издаване")
		@Column(name = "year_issued")
		private Integer yearIssued;
		
		@JournalAttr(label = "uchebnoZavedenie", defaultText = "Институция издала", classifID = ""+DocuConstants.CODE_CLASSIF_UCHEB_ZAVEDENIE)
		@Column(name = "uchebno_zavedenie")
		private Integer uchebnoZavedenie;
		
		@JournalAttr(label = "uchebZavText", defaultText = "Институция издала")
		@Column(name = "ucheb_zav_text")
		private String uchebZavText;
		
		@JournalAttr(label = "dopInfo", defaultText = "Доп. информация")
		@Column(name = "dop_info")
		private String dopInfo;
		
		@JournalAttr(label = "vidDoc", defaultText = "Вид документ", classifID = ""+DocuConstants.CODE_CLASSIF_VID_DOC_OBR_ZENS)
		@Column(name = "vid_doc")
		private Integer vidDoc;
		
		
		@JournalAttr(label = "seriaFnom", defaultText = "Серия фабр.номер документа")
		@Column(name = "seria_fabrnom")
		private String seriaFnom;
		
		

		/** */
		public MMSCoachesDiploms() {
			
		}

		/**
		 * @param id
		 * @param idCoaches
		 * @param regNomer
		 * @param yearIssued
		 * @param uchebnoZavedenie
		 * @param uchebZavText
		 * @param dopInfo
		 * @param vidDoc
		 * @param seriaFnom
		 */
		public MMSCoachesDiploms(Integer id, Integer idCoaches, String regNomer, Integer yearIssued, 
				Integer uchebnoZavedenie, String uchebZavText, String dopInfo, Integer vidDoc, String seriaFnom) {
			this.id = id;
			this.setIdCoaches(idCoaches);
			this.setRegNomer(regNomer);
			this.setYearIssued(yearIssued);
			this.setUchebnoZavedenie(uchebnoZavedenie);
			this.uchebZavText = uchebZavText;
			this.dopInfo = dopInfo;
			this.vidDoc = vidDoc;
			this.seriaFnom = seriaFnom;
		}
		
		public Integer getId() {
			return this.id;
		}

		public Integer getIdCoaches() {
			return idCoaches;
		}

		public void setIdCoaches(Integer idCoaches) {
			this.idCoaches = idCoaches;
		}

		public String getRegNomer() {
			return regNomer;
		}

		public void setRegNomer(String regNomer) {
			this.regNomer = regNomer;
		}

		public Integer getYearIssued() {
			return yearIssued;
		}

		public void setYearIssued(Integer yearIssued) {
			this.yearIssued = yearIssued;
		}

		public Integer getUchebnoZavedenie() {
			return uchebnoZavedenie;
		}

		public void setUchebnoZavedenie(Integer uchebnoZavedenie) {
			this.uchebnoZavedenie = uchebnoZavedenie;
		}

		public String getUchebZavText() {
			return uchebZavText;
		}

		public void setUchebZavText(String uchebZavText) {
			this.uchebZavText = uchebZavText;
		}

		public String getDopInfo() {
			return dopInfo;
		}

		public void setDopInfo(String dopInfo) {
			this.dopInfo = dopInfo;
		}

		public Integer getVidDoc() {
			return vidDoc;
		}

		public void setVidDoc(Integer vidDoc) {
			this.vidDoc = vidDoc;
		}

		public String getSeriaFnom() {
			return seriaFnom;
		}

		public void setSeriaFnom(String seriaFnom) {
			this.seriaFnom = seriaFnom;
		}
	
		
}
