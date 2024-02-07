package com.ib.docu.quartz;

import static com.ib.docu.system.DocuConstants.CODE_ZNACHENIE_REF_TYPE_FZL;
import static com.ib.docu.system.DocuConstants.CODE_ZNACHENIE_REF_TYPE_NFL;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.persistence.Query;
import javax.servlet.ServletContext;
import javax.xml.datatype.DatatypeConfigurationException;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.docu.db.dao.ReferentDAO;
import com.ib.docu.db.dto.Referent;
import com.ib.docu.system.SystemData;
import com.ib.docu.utils.RegixUtils;
import com.ib.indexui.system.Constants;
import com.ib.system.ActiveUser;
import com.ib.system.db.JPA;
import com.ib.system.exceptions.DbErrorException;
import com.ib.system.utils.DateUtils;

import bg.government.regixclient.RegixClientException;

/**
 * „Back-ground метод за актуализация на данни на юридически и физически лица“
 *
 * @author belev
 */
@DisallowConcurrentExecution
public class RegixSyncJob implements Job {

	private static final Logger LOGGER = LoggerFactory.getLogger(RegixSyncJob.class);

	/** @param args */
	public static void main(String[] args) {
		RegixSyncJob job = new RegixSyncJob();
		try {
			job.execute(null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

	/** */
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		try {
			SystemData systemData;

			if (context != null) {
				ServletContext servletContext = (ServletContext) context.getScheduler().getContext().get("servletContext");
				if (servletContext == null) {
					LOGGER.error("********** ServletContext is null **********");
					return;
				}
				systemData = (SystemData) servletContext.getAttribute("systemData");

			} else { // унит тестове явно
				LOGGER.error("********** JobExecutionContext is null **********");
				systemData = new SystemData();
			}

			String setting = systemData.getSettingsValue("regix.sync.job.count");
			if (setting == null || Integer.parseInt(setting) == 0) {
				LOGGER.info("SWITCHED OFF by setting 'regix.sync.job.count'");
				return;
			}
			LOGGER.info("regix.sync.job.on={}", setting);

			long days30 = 30 * 24 * 60 * 60 * (long) 1000;
			Date date = new Date(System.currentTimeMillis() - days30);

			List<Integer> refTypes = new ArrayList<>();
			refTypes.add(CODE_ZNACHENIE_REF_TYPE_NFL);
			if ("true".equals(systemData.getSettingsValue("REGIX_ESGRAON_ACTIVE"))) {
				refTypes.add(CODE_ZNACHENIE_REF_TYPE_FZL);
			}

			StringBuilder sql = new StringBuilder();
			sql.append(" select distinct r.ref_id, r.code, r.ref_type, r.nfl_eik, r.fzl_egn ");
			sql.append(" , case when obed.id is null then 0 else 1 end obed "); // 5
			sql.append(" , case when formir.id is null then 0 else 1 end formir "); // 6
			sql.append(" , case when coach.id is null then 0 else 1 end coach "); // 7
			sql.append(" , case when lice.id is null then 0 else 1 end lice "); // 8
			sql.append(" from adm_referents r ");
			sql.append(" left outer join mms_sport_obedinenie obed on obed.id_object = r.code ");
			sql.append(" left outer join mms_sport_formirovanie formir on formir.id_object = r.code ");
			sql.append(" left outer join mms_coaches coach on coach.id_object = r.code ");
			sql.append(" left outer join mms_sport_obekt_lice lice on lice.id_object = r.code ");
			sql.append(" where r.ref_type in (?1) ");
			sql.append(" and (obed.id is not null or formir.id is not null or coach.id is not null or lice.id is not null) ");
			sql.append(" and (r.date_last_mod is null or r.date_last_mod <= ?2) "); // никога или последна промяна преди 30дни
			sql.append(" order by r.ref_id ");

			@SuppressWarnings("unchecked")
			List<Object[]> rows = JPA.getUtil().getEntityManager().createNativeQuery(sql.toString()) //
					.setParameter(1, refTypes) //
					.setParameter(2, date).setMaxResults(Integer.parseInt(setting)).getResultList();

			LOGGER.info("Found {} to Sync by RegIX", rows.size());

			ReferentDAO dao = new ReferentDAO(ActiveUser.DEFAULT);
			Date now = new Date();

			for (Object[] row : rows) {
				int code = ((Number) row[1]).intValue();

				Referent referent = dao.findByCode(code, now, true);
				JPA.getUtil().closeConnection(); // иначе като е сетват разни неща в обекта ще се направи мерге!!!

				syncReferent(referent, systemData); // това си е в отделно бегин/комит/ролбак
			}

			// след промените да се рефрешне и класификацията
			systemData.reloadClassif(Constants.CODE_CLASSIF_REFERENTS, false, true);

		} catch (Exception e) {
			LOGGER.error("Грешка при Aктуализация на данни на юридически и физически лица.", e);

		} finally {
			JPA.getUtil().closeConnection();
		}
	}

	/**  */
	void syncReferent(Referent referent, SystemData systemData) throws DbErrorException, RegixClientException, DatatypeConfigurationException {
		if (referent.getRefType() == null) {
			return;
		}
		boolean changed = false;

		LinkedHashMap<String, Object[]> diff = new LinkedHashMap<>();

		if (CODE_ZNACHENIE_REF_TYPE_FZL == referent.getRefType().intValue()) {
			changed = RegixUtils.loadFizLiceByEgn(referent, referent.getFzlEgn(), true, true, systemData);

			diff.put("Разлика", new Object[] { "старо име", "ново име" }); // TODO тука не е направено да се връщат разликите

		} else if (CODE_ZNACHENIE_REF_TYPE_NFL == referent.getRefType().intValue()) {
			changed = RegixUtils.loadUridLiceByEik(referent, referent.getNflEik(), systemData, diff);

		} else {
			LOGGER.error("unknown ReferentType={}", referent.getRefType());
		}

		StringBuilder text = new StringBuilder();

		if (changed) { // тука пробваме за всеки ако мине записа
			for (Entry<String, Object[]> entry : diff.entrySet()) {
				text.append("<i>" + entry.getKey() + ":</i> <b>" + getText(entry.getValue()[0]) + "</b> се променя на <b>" + getText(entry.getValue()[1]) + "</b></br>");
			}

			text.append("</br>");
			text.append("<i>Време на проверка:</i> " + DateUtils.printDateFull(new Date()));
		}
		setCheked(referent, text);
	}

	private Object getText(Object o) {
		if (o instanceof String) {
			String s = (String) o;
			return s.trim().length() > 0 ? s : "''";
		}
		return o != null ? o : "''";
	}

	private void setCheked(Referent referent, StringBuilder diffText) {
		try { // трябва да се каже, че е проверен, за да може при следващо минаване да се пропусне
			Query query = JPA.getUtil().getEntityManager().createNativeQuery( //
					"update adm_referents set user_last_mod = -1, date_last_mod = ?1, regix_diff = ?2 where ref_id = ?3");

			query.setParameter(1, new Date());
			query.setParameter(2, diffText == null || diffText.length() == 0 ? null : diffText.toString());
			query.setParameter(3, referent.getId());

			JPA.getUtil().begin();
			query.executeUpdate();
			JPA.getUtil().commit();

			LOGGER.info("ref_id={}; ref_code={}; checked; diff_text={}", referent.getId(), referent.getCode(), diffText);

		} catch (Exception e) {
			JPA.getUtil().rollback();

			LOGGER.error("Грешка при запис на НФЛ/ФЗЛ.", e);
		}
	}
}
