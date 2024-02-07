package com.ib.docu.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.inject.Named;

import org.omnifaces.cdi.ViewScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.docu.db.dto.AdmVidSport;
import com.ib.docu.system.DocuConstants;
import com.ib.indexui.system.IndexUIbean;
import com.ib.indexui.utils.JSFUtils;
import com.ib.system.db.JPA;
import com.ib.system.db.dto.SystemClassif;
import com.ib.system.exceptions.BaseException;
import com.ib.system.exceptions.DbErrorException;
import com.ib.system.exceptions.ObjectInUseException;

/**
 * Олимпийски и военно-приложни спортове
 *
 * @author n.kanev
 */
@Named
@ViewScoped
public class OlympicSports extends IndexUIbean implements Serializable {
	
	private static final long serialVersionUID = 5516034921666546732L;
	private static final Logger LOGGER = LoggerFactory.getLogger(OlympicSports.class);
	
	private List<Sport> sports;
	
	// Класификацията със спортовете
	private List<SystemClassif> sportsClassif;
	
	// Записите в таблицата adm_vid_sport
	private List<AdmVidSport> admVidSport;	

	@PostConstruct
	public void init() {
		this.sportsClassif = new ArrayList<>();
		
		try {
			// извличат се значенията от таблиците
			this.sportsClassif = getSystemData().getSysClassification(DocuConstants.CODE_CLASSIF_VIDOVE_SPORT, new Date(), getCurrentLang());
			this.sportsClassif.sort((s1, s2) -> s1.getTekst().compareToIgnoreCase(s2.getTekst()));
			this.admVidSport = JPA.getUtil().getEntityManager().createQuery("from AdmVidSport", AdmVidSport.class).getResultList();
			
			// двата списъка се комбинират в обекти от тип Sport, които по удобен начин пазят информацията 
			// и за чекбоксовете ползват boolean вместо 1 / 2. Чекбоксовете не поддържат converter, затова се прави по този начин.
			this.sports = new ArrayList<>();
			this.sportsClassif.stream().forEach(s -> {
				Sport sport = new Sport();
				sport.setSport(s);
				
				AdmVidSport vidSport = this.admVidSport.stream().filter(v -> v.getVidSport().equals(s.getCode())).findFirst().orElseGet(() -> null);
				
				if(vidSport != null) {
					sport.setOlympic(vidSport.getOlimp() != null && vidSport.getOlimp() == DocuConstants.CODE_ZNACHENIE_DA);
					sport.setVoenen(vidSport.getVoenen() != null && vidSport.getVoenen() == DocuConstants.CODE_ZNACHENIE_DA);
				}
				else {
					sport.setOlympic(false);
					sport.setVoenen(false);
				}
				
				sports.add(sport);
			});
			
		} catch (DbErrorException e) {
			LOGGER.error(getMessageResourceString(beanMessages, "general.errorClassif"), e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
		}
		
		// Изтриват се ненужните редове тук, за да не се показват в таблицата.
		deleteMissingSports();
	
	}
	
	/**
	 * Отговаря за следния проблем:
	 * В класификацията 'Видове спорт' е имало спорт 'Бягане с яйце в лъжица' с ид 101.
	 * Бил му е въведен запис в таблицата 'adm_vid_sport', където пише, че е олимпийски.
	 * После спортът е бил затрит от класификацията. Редът в 'adm_vid_sport' обаче остава.
	 * 
	 * Този метод намира подобни висящи редове, които сочат към затрити спортове, и ги трие.
	 */
	private void deleteMissingSports() {	
		try {
			Set<Integer> sportsCodes = this.sportsClassif.stream().mapToInt(s -> s.getCode()).boxed().collect(Collectors.toSet());
			Set<AdmVidSport> admSportsToDelete = this.admVidSport.stream().filter(s -> !sportsCodes.contains(s.getVidSport())).collect(Collectors.toSet());

			JPA.getUtil().runInTransaction(() -> {
				for(AdmVidSport s : admSportsToDelete) {
					LOGGER.debug("Изтриване на adm_vid_sport с код: " + s.getVidSport());
					JPA.getUtil().getEntityManager().remove(s);
				}
			});
			
		}
		catch(NullPointerException e) {
			LOGGER.error(e.getMessage(), e);
		}
		catch(ObjectInUseException | DbErrorException e) {
			LOGGER.error(e.getMessage(), e);
		}
		catch(BaseException e) {
			LOGGER.error(e.getMessage(), e);
		}
	}
	
	/**
	 * За да не се прави ъпдейт без необходимост, тук се отбелязва на кой ред е бил кликнат чекбокс.
	 * После се обновяват смо кликнатите.
	 * 
	 * @param index индексът на реда, който е бил кликнат
	 */
	public void onCheckboxClick(int index) {
		this.sports.get(index).setAltered(true);
	}
	
	/**
	 * Кликнат е 'Запис'
	 * 
	 */
	public void actionSave() {
		List<AdmVidSport> admSportsToPersist = new ArrayList<>();
		List<AdmVidSport> admSportsToDelete = new ArrayList<>();
		
		for(Sport sport : this.sports) {
			// ъпдейтват се само спортовете, чиито чекбоксове са били кликани
			if(sport.altered) {
				
				// Може още да няма запис в таблицата за конкретния спорт. 
				// В този случай не променяме съществуващ AdmVisSport, а създаваме нов запис.
				AdmVidSport alteredAdmSport = this.admVidSport.stream().filter(s -> s.getVidSport().equals(sport.sport.getCode())).findFirst().orElseGet(() -> null);
				if(alteredAdmSport == null) {
					alteredAdmSport = new AdmVidSport();
					alteredAdmSport.setVidSport(sport.sport.getCode());
				}
				
				int isOlymp, isVoenen;
				isOlymp = (sport.olympic) 
						? DocuConstants.CODE_ZNACHENIE_DA 
						: DocuConstants.CODE_ZNACHENIE_NE;
				
				isVoenen = (sport.voenen) 
						? DocuConstants.CODE_ZNACHENIE_DA 
						: DocuConstants.CODE_ZNACHENIE_NE;
				
				alteredAdmSport.setOlimp(isOlymp);
				alteredAdmSport.setVoenen(isVoenen);
				
				if(alteredAdmSport.getOlimp().equals(DocuConstants.CODE_ZNACHENIE_NE) && alteredAdmSport.getVoenen().equals(DocuConstants.CODE_ZNACHENIE_NE)) {
					admSportsToDelete.add(alteredAdmSport);
				}
				else {
					admSportsToPersist.add(alteredAdmSport);
				}
			}
		}
		
		// Запис на обектите. Ако е нов, даваме persist. Ако променяме съществуващ - merge.
		// Ако в обекта има два пъти НЕ, го трия от таблицата.
		try {
			JPA.getUtil().runInTransaction(() -> {
				for(AdmVidSport admSport : admSportsToPersist) {
					AdmVidSport s = JPA.getUtil().getEntityManager().find(AdmVidSport.class, admSport.getVidSport());
					
					if(s == null) {
						JPA.getUtil().getEntityManager().persist(admSport);
					}
					else {
						JPA.getUtil().getEntityManager().merge(admSport);
					}
				}
				
				for(AdmVidSport admSport : admSportsToDelete) {
					AdmVidSport s = JPA.getUtil().getEntityManager().find(AdmVidSport.class, admSport.getVidSport());
					
					if(s != null) {
						JPA.getUtil().getEntityManager().remove(s);
					}
				}
			});
			
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, getMessageResourceString(UI_beanMessages, "general.succesSaveMsg"));
			
			// рефреш на класификацията с новите данни
			getSystemData().reloadClassif(DocuConstants.CODE_CLASSIF_VIDOVE_SPORT_OLIMP, false, false);
		}
		catch(ObjectInUseException | DbErrorException e) {
			LOGGER.error(e.getMessage(), e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, getMessageResourceString(beanMessages, "general.errorZapis"));
		}
		catch(BaseException e) {
			LOGGER.error(e.getMessage(), e);
		}

	}
	
	/**
	 * 
	 * Този обект служи, за да пази на едно място спорта, хем двата критерия като тип boolean.
	 * Чекбоксовете не поддържат converter в html-a (въпреки че излиза като атрибут), 
	 * затова става много сложно да прикачиш стойност 1 / 2 (ДА/НЕ) към чекбокс.
	 * Този клас решава проблема, въпреки че се налага в началото и при запис да се сглобява и чете.
	 *
	 */
	public class Sport {
		private SystemClassif sport;
		private boolean olympic;
		private boolean voenen;
		private boolean altered;
		
		public Sport() { }

		public SystemClassif getSport() {
			return sport;
		}

		public void setSport(SystemClassif sport) {
			this.sport = sport;
		}

		public boolean getOlympic() {
			return olympic;
		}

		public void setOlympic(boolean olympic) {
			this.olympic = olympic;
		}

		public boolean getVoenen() {
			return voenen;
		}

		public void setVoenen(boolean voenen) {
			this.voenen = voenen;
		}

		public boolean isAltered() {
			return altered;
		}

		public void setAltered(boolean altered) {
			this.altered = altered;
		}
	}
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */	
	

	public List<SystemClassif> getSportsClassif() {
		return sportsClassif;
	}

	public void setSportsClassif(List<SystemClassif> sportsClassif) {
		this.sportsClassif = sportsClassif;
	}

	public List<Sport> getSports() {
		return sports;
	}

	public void setSports(List<Sport> sports) {
		this.sports = sports;
	}
	
}
