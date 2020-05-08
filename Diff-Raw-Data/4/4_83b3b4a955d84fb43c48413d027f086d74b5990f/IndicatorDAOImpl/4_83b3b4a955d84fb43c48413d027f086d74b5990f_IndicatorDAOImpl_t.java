 package org.ocha.hdx.persistence.dao.currateddata;
 
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.persistence.EntityManager;
 import javax.persistence.NoResultException;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Query;
 import javax.persistence.TypedQuery;
 
 import org.joda.time.DateTime;
 import org.ocha.hdx.importer.TimeRange;
 import org.ocha.hdx.model.validation.ValidationStatus;
 import org.ocha.hdx.persistence.entity.ImportFromCKAN;
 import org.ocha.hdx.persistence.entity.curateddata.Entity;
 import org.ocha.hdx.persistence.entity.curateddata.Indicator;
 import org.ocha.hdx.persistence.entity.curateddata.Indicator.Periodicity;
 import org.ocha.hdx.persistence.entity.curateddata.IndicatorType;
 import org.ocha.hdx.persistence.entity.curateddata.IndicatorValue;
 import org.ocha.hdx.persistence.entity.curateddata.Source;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.transaction.annotation.Transactional;
 
 public class IndicatorDAOImpl implements IndicatorDAO {
 
 	private static Logger logger = LoggerFactory.getLogger(IndicatorDAOImpl.class);
 
 	@PersistenceContext
 	private EntityManager em;
 
 	/* *************** */
 	/* CRUD operations */
 	/* *************** */
 
 	/**
 	 * Create indicator.
 	 */
 	@Override
 	@Transactional
 	public void createIndicator(final Source source, final Entity entity, final IndicatorType type, final Date start, final Date end, final Periodicity periodicity, final IndicatorValue value,
 			final String initialValue, final ValidationStatus validationStatus, final String sourceLink, final ImportFromCKAN importFromCKAN) {
 
 		final Indicator indicator = new Indicator();
 		indicator.setSource(source);
 		indicator.setEntity(entity);
 		indicator.setType(type);
 		indicator.setStart(start);
 		indicator.setEnd(end);
 		indicator.setPeriodicity(periodicity);
 		indicator.setValue(value);
 		indicator.setInitialValue(initialValue);
 		indicator.setValidationStatus(validationStatus);
 		indicator.setSourceLink(sourceLink);
 		indicator.setImportFromCKAN(importFromCKAN);
 		em.persist(indicator);
 	}
 
 	/**
 	 * Delete an indicator.
 	 */
 	@Override
 	@Transactional
 	public void deleteIndicator(final long indicatorId) {
 		em.createQuery("DELETE FROM Indicator i WHERE i.id = :indicatorId").setParameter("indicatorId", indicatorId).executeUpdate();
 
 	}
 
 	/**
 	 * Delete some indicators.
 	 * 
 	 * @author Dan TODO: It could delete all indicators in one transaction
 	 */
 	@Override
 	@Transactional
 	public void deleteIndicators(final List<Long> indList) {
 		for (final Long ind : indList) {
 			deleteIndicator(ind);
 		}
 	}
 
 	/**
 	 * Delete all indicators.
 	 */
 	@Override
 	@Transactional
 	public void deleteAllIndicators() {
 		em.createQuery("DELETE FROM Indicator").executeUpdate();
 	}
 
 	/**
 	 * Delete all indicators for a given import.
 	 */
 	@Override
 	@Transactional
 	public void deleteAllIndicatorsFromImport(final long importId) {
 		em.createQuery("DELETE FROM Indicator i WHERE i.importFromCKAN.id = :importId").setParameter("importId", importId).executeUpdate();
 
 	}
 
 	/* Lists */
 
 	@Override
 	public List<Indicator> listLastIndicators(final int limit) {
 		final TypedQuery<Indicator> query = em.createQuery("SELECT i FROM Indicator i ORDER BY i.id DESC", Indicator.class).setMaxResults(limit);
 		return query.getResultList();
 	}
 
 	@Override
 	public List<Indicator> listIndicatorsByPeriodicityAndSourceAndIndicatorType(final Periodicity periodicity, final String sourceCode, final String indicatorTypeCode, final List<String> countryCodes) {
 		final StringBuilder sb = new StringBuilder();
 		sb.append("SELECT i FROM Indicator i WHERE i.periodicity = :periodicity AND i.source.code = :source AND i.type.code = :indicatorType ");
 		if ((countryCodes != null) && !countryCodes.isEmpty()) {
 			sb.append(" AND i.entity.type.code = 'country' AND i.entity.code IN :countryCodes ");
 		}
 		sb.append(" ORDER BY i.start, i.entity.type.code, i.entity.code");
 		final TypedQuery<Indicator> query = em.createQuery(sb.toString(), Indicator.class).setParameter("periodicity", periodicity).setParameter("source", sourceCode)
 				.setParameter("indicatorType", indicatorTypeCode);
 
 		if ((countryCodes != null) && !countryCodes.isEmpty()) {
 			query.setParameter("countryCodes", countryCodes);
 		}
 
 		return query.getResultList();
 	}
 
 	@Override
 	public List<Indicator> listIndicatorsByPeriodicityAndEntityAndIndicatorType(final Periodicity periodicity, final String entityType, final String entityCode, final String indicatorTypeCode) {
 		final TypedQuery<Indicator> query = em
 				.createQuery(
 						"SELECT i FROM Indicator i WHERE i.periodicity = :periodicity AND i.entity.type.code = :entityType AND i.entity.code = :entityCode AND i.type.code = :indicatorType ORDER BY i.start, i.source.code",
 						Indicator.class).setParameter("periodicity", periodicity).setParameter("entityType", entityType).setParameter("entityCode", entityCode)
 				.setParameter("indicatorType", indicatorTypeCode);
 
 		return query.getResultList();
 	}
 
 	@Override
 	public List<Indicator> listIndicatorsByYearAndSourceAndIndicatorType(final int year, final String sourceCode, final String indicatorTypeCode, final List<String> countryCodes) {
 		final TimeRange timeRange = new TimeRange(year);
 		final StringBuilder sb = new StringBuilder();
 		sb.append("SELECT i FROM Indicator i WHERE i.periodicity = :periodicity AND i.source.code = :source AND i.type.code = :indicatorType AND i.start = :start ");
 		if ((countryCodes != null) && !countryCodes.isEmpty()) {
 			sb.append(" AND i.entity.type.code = 'country' AND i.entity.code IN :countryCodes ");
 		}
 		sb.append(" ORDER BY i.entity.type.code, i.entity.code ");
 		final TypedQuery<Indicator> query = em.createQuery(sb.toString(), Indicator.class).setParameter("periodicity", Periodicity.YEAR).setParameter("start", timeRange.getStart())
 				.setParameter("source", sourceCode).setParameter("indicatorType", indicatorTypeCode);
 
 		if ((countryCodes != null) && !countryCodes.isEmpty()) {
 			query.setParameter("countryCodes", countryCodes);
 		}
 		return query.getResultList();
 	}
 
 	@Override
 	public List<Indicator> listIndicatorsByYearAndSourceAndIndicatorTypes(final int year, final String sourceCode, final List<String> indicatorTypeCodes) {
 		final TimeRange timeRange = new TimeRange(year);
 		final TypedQuery<Indicator> query = em
 				.createQuery(
 						"SELECT i FROM Indicator i WHERE i.periodicity = :periodicity AND i.source.code = :source AND i.type.code IN :indicatorTypes AND i.start = :start ORDER BY i.entity.type.code, i.entity.code, i.type.code",
 						Indicator.class).setParameter("periodicity", Periodicity.YEAR).setParameter("start", timeRange.getStart()).setParameter("source", sourceCode)
 				.setParameter("indicatorTypes", indicatorTypeCodes);
 
 		return query.getResultList();
 	}
 
 	/* ******* */
 	/* Reports */
 	/* ******* */
 
 	/* Country reports */
 
 	/*
 	 * List of indicators for a country - overview.
 	 */
 	@Override
 	public List<Object[]> listIndicatorsForCountryOverview(final String countryCode, final String languageCode) {
 		// List of indicators relevant for country - overview. TODO Externalize ?
		final String[] indicatorsList = new String[] { "CD010", "CD030", "CD050", "CD060", "CD070", "CD080", "CD090", "CG020", "CG030", "CG060", "CG070", "CG080", "CG100", "CG120", "CG140", "CG150",
				"CG260", "CG290", "_m49-name", "_unterm:ISO Country alpha-2-code" };
 
 		final List<Object[]> result = new ArrayList<Object[]>();
 		for (final String indicator : indicatorsList) {
 			// TODO i18n
 			final Query query = em
 					.createQuery(
 							"SELECT i.type.code, i.type.name.defaultValue, i.value, i.importFromCKAN.timestamp, i.source.name.defaultValue from Indicator i WHERE i.entity.type.code = :isCountry AND i.type.code = :code AND i.entity.code = :countryCode")
 					.setParameter("isCountry", "country").setParameter("code", indicator).setParameter("countryCode", countryCode).setMaxResults(1);
 			Object[] queryResult = null;
 			try {
 				queryResult = (Object[]) query.getSingleResult();
 			} catch (final NoResultException e) {
 				// It is possible that no value exists for the given indicator.
 				// So we just put the indicator in the result.
 				queryResult = new Object[] { indicator };
 			}
 			result.add(queryResult);
 		}
 		return result;
 	}
 
 	/*
 	 * List of indicators for a country - crisis history.
 	 */
 	@Override
 	public Map<Integer, List<Object[]>> listIndicatorsForCountryCrisisHistory(final String countryCode, final int fromYear, final int toYear, final String languageCode) {
 
 		// List of indicators relevant for country - crisis history. Each indicator type has an associated source here TODO Externalize ?
 		final String[] indicatorsList = new String[] { "CH070", "CH080", "CH090", "CH100" };
 		final String[] sourcesList = new String[] { "emdat", "emdat", "emdat", "emdat" };
 
 		return listIndicatorsForCountry(countryCode, fromYear, toYear, indicatorsList, sourcesList, languageCode, Periodicity.YEAR);
 	}
 
 	/*
 	 * List of indicators for a country - socio-economic.
 	 */
 	@Override
 	public Map<Integer, List<Object[]>> listIndicatorsForCountrySocioEconomic(final String countryCode, final int fromYear, final int toYear, final String languageCode) {
 		// List of indicators relevant for country - socio-economic. Each indicator type has an associated source here TODO Externalize ?
 		final String[] indicatorsList = new String[] { "PSE030", "PSE090", "PSE120", "PSE130", "PSP080", "PSE140", "PSE150", "PSE200", "PSP010", "PSP060", "PSP090", "PSP110" };
 		final String[] sourcesList = new String[] { "world-bank", "world-bank", "world-bank", "world-bank", "esa-unpd-wpp2012", "world-bank", "world-bank", "world-bank", "esa-unpd-wpp2012",
 				"world-bank", "world-bank", "world-bank" };
 
 		return listIndicatorsForCountry(countryCode, fromYear, toYear, indicatorsList, sourcesList, languageCode, Periodicity.YEAR);
 	}
 
 	/*
 	 * List of indicators for a country - vulnerability.
 	 */
 	@Override
 	public Map<Integer, List<Object[]>> listIndicatorsForCountryVulnerability(final String countryCode, final int fromYear, final int toYear, final String languageCode) {
 		// List of indicators relevant for country - vulnerability. Each indicator type has an associated source here TODO Externalize ?
 		final String[] indicatorsList = new String[] { "PVE130", "PVF020", "PVH140", "PVL040", "PVN010", "PVW010", "PVW040" };
 		final String[] sourcesList = new String[] { "mdgs", "faostat3", "mdgs", "world-bank", "fao-foodsec", "mdgs", "mdgs" };
 
 		return listIndicatorsForCountry(countryCode, fromYear, toYear, indicatorsList, sourcesList, languageCode, Periodicity.YEAR);
 	}
 
 	/*
 	 * List of indicators for a country - capacity.
 	 */
 	@Override
 	public Map<Integer, List<Object[]>> listIndicatorsForCountryCapacity(final String countryCode, final int fromYear, final int toYear, final String languageCode) {
 		// List of indicators relevant for country - capacity. Each indicator type has an associated source here TODO Externalize ?
 		final String[] indicatorsList = new String[] { "PCX051", "PCX080", "PCX090", "PCX100" };
 		final String[] sourcesList = new String[] { "mdgs", "mdgs", "world-bank", "world-bank" };
 
 		return listIndicatorsForCountry(countryCode, fromYear, toYear, indicatorsList, sourcesList, languageCode, Periodicity.YEAR);
 	}
 
 	/*
 	 * List of indicators for a country - other.
 	 */
 	@Override
 	public Map<Integer, List<Object[]>> listIndicatorsForCountryOther(final String countryCode, final int fromYear, final int toYear, final String languageCode) {
 		// List of indicators relevant for country - other. Each indicator type has an associated source here TODO Externalize ?
 		final String[] indicatorsList = new String[] { "_Children 1 year old immunized against measles, percentage", "_emdat:no_homeless", "_emdat:no_injured", "_emdat:total_affected",
 				"_GNI, PPP (current international $)", "_Internet users per 100 inhabitants", "_Land area (sq. km)", "_Net ODA received per capita (current US$)", "_Number of infant deaths",
 				"_Population, total", "_Population undernourished, millions", "_Population undernourished, percentage", "_reliefweb_Humanitarian_Bulletin", "_reliefweb_Humanitarian_Dashboard",
 				"_reliefweb_Humanitarian_Snapshot", "_reliefweb_Infographic", "_reliefweb_Key_Messages", "_reliefweb_Other", "_reliefweb_Press_Release", "_reliefweb_Press_Review",
 				"_reliefweb_Reference_Map", "_reliefweb_Situation_Report", "_reliefweb_Statement/Speech", "_reliefweb_Thematic_Map" };
 		final String[] sourcesList = new String[] { "mdgs", "emdat", "emdat", "emdat", "world-bank", "mdgs", "world-bank", "world-bank", "world-bank", "world-bank", "mdgs", "mdgs", "reliefweb-api",
 				"reliefweb-api", "reliefweb-api", "reliefweb-api", "reliefweb-api", "reliefweb-api", "reliefweb-api", "reliefweb-api", "reliefweb-api", "reliefweb-api", "reliefweb-api",
 				"reliefweb-api" };
 
 		return listIndicatorsForCountry(countryCode, fromYear, toYear, indicatorsList, sourcesList, languageCode, Periodicity.YEAR);
 	}
 
 	/*
 	 * List of indicators for a country - 5-years data.
 	 */
 	@Override
 	public Map<Integer, List<Object[]>> list5YearsIndicatorsForCountry(final String countryCode, final int fromYear, final int toYear, final String languageCode) {
 		final String[] indicatorsList = new String[] { "_WPP2012_MORT_F02_CRUDE_DEATH_RATE", "PSP050", "PVH010", "PVH050", "PVH100", "PVH150" };
 		final String[] sourcesList = new String[] { "esa-unpd-wpp2012", "esa-unpd-wpp2012", "esa-unpd-wpp2012", "esa-unpd-wpp2012", "esa-unpd-wpp2012", "esa-unpd-wpp2012" };
 
 		return listIndicatorsForCountry(countryCode, fromYear, toYear, indicatorsList, sourcesList, languageCode, Periodicity.FIVE_YEARS);
 	}
 
 	/*
 	 * Generic method to get indicators for a given country.
 	 */
 	private Map<Integer, List<Object[]>> listIndicatorsForCountry(final String countryCode, final int fromYear, final int toYear, final String[] indicatorsList, final String[] sourcesList,
 			final String languageCode, final Periodicity periodicity) {
 		int fromYear_ = fromYear;
 		int toYear_ = toYear;
 
 		// Convention : if fromYear = 0 => we take the earliest data available
 		Map<String, Integer> minMax = null;
 		if (0 == fromYear) {
 			// Find the earliest year available
 			minMax = getMinMaxDatesForCountryIndicators(countryCode, indicatorsList, sourcesList);
 			fromYear_ = minMax.get("MIN");
 		}
 
 		// Convention : if toYear = 0 => we take the latest data available
 		if (0 == toYear) {
 			// Find the latest year available
 			if (null == minMax) {
 				minMax = getMinMaxDatesForCountryIndicators(countryCode, indicatorsList, sourcesList);
 			}
 			toYear_ = minMax.get("MAX");
 		}
 
 		final Map<Integer, List<Object[]>> result = new HashMap<>();
 		for (int year = fromYear_; year <= toYear_; year++) {
 			final DateTime yearAsDate = new DateTime(year, 1, 1, 0, 0);
 			final List<Object[]> yearResult = new ArrayList<Object[]>();
 			for (int i = 0; i < indicatorsList.length; i++) {
 				final String indicator = indicatorsList[i];
 				final String sourceCode = sourcesList[i];
 				// FIXME this should be possible to run only one query per indicator, with several results, one per year. Only if there is one and only one result per year, no updates
 				final Query query = em
 						.createQuery(
 								"SELECT i.type.code, i.type.name.defaultValue, i.type.unit.name.defaultValue, i.value, i.importFromCKAN.timestamp, i.source.name.defaultValue from Indicator i WHERE i.entity.type.code = :isCountry AND i.type.code = :code AND i.source.code = :sourceCode "
 										+ "AND i.entity.code = :countryCode AND i.periodicity = :periodicity and i.start = :start").setParameter("isCountry", "country")
 						.setParameter("code", indicator).setParameter("sourceCode", sourceCode).setParameter("countryCode", countryCode).setParameter("periodicity", periodicity)
 						.setParameter("start", yearAsDate.toDate());
 				Object[] queryResult = null;
 				try {
 					queryResult = (Object[]) query.getSingleResult();
 				} catch (final NoResultException e) {
 					// It is possible that no value exists for the given indicator.
 					// So we just put the indicator in the result.
 					queryResult = new Object[] { indicator };
 				}
 				yearResult.add(queryResult);
 			}
 			result.put(year, yearResult);
 		}
 		return result;
 	}
 
 	/*
 	 * For a given country and list of indicators, find the earliest and latest years of information available.
 	 */
 	@Override
 	public Map<String, Integer> getMinMaxDatesForCountryIndicators(final String countryCode, final String[] indicatorsList, final String[] sourcesList) {
 		String query_ = "SELECT MIN(i.start), MAX(i.start) from Indicator i WHERE i.entity.type.code = :isCountry AND i.entity.code = :countryCode AND i.periodicity = :periodicity AND (";
 		for (int i = 0; i < indicatorsList.length; i++) {
 			query_ += "(i.source.code = :sourceCode_" + i + " AND i.type.code = :code_" + i + ")";
 			if (i < (indicatorsList.length - 1)) {
 				query_ += " OR ";
 			}
 		}
 		query_ += ")";
 		logger.debug("Min-max dates query : " + query_);
 
 		final Query query = em.createQuery(query_);
 
 		query.setParameter("isCountry", "country");
 		query.setParameter("countryCode", countryCode);
 		query.setParameter("periodicity", Periodicity.YEAR);
 
 		for (int i = 0; i < indicatorsList.length; i++) {
 			final String indicator = indicatorsList[i];
 			final String sourceCode = sourcesList[i];
 			query.setParameter("code_" + i, indicator);
 			query.setParameter("sourceCode_" + i, sourceCode);
 		}
 		Object[] queryResult = null;
 		queryResult = (Object[]) query.getSingleResult();
 
 		final Map<String, Integer> result = new HashMap<String, Integer>();
 
 		final Timestamp min = (Timestamp) queryResult[0];
 		final Timestamp max = (Timestamp) queryResult[1];
 
 		final Calendar minCal = new GregorianCalendar();
 		final Calendar maxCal = new GregorianCalendar();
 
 		if (null != min) {
 			minCal.setTimeInMillis(min.getTime());
 			result.put("MIN", minCal.get(Calendar.YEAR));
 		} else {
 			result.put("MIN", 0);
 		}
 		if (null != max) {
 			maxCal.setTimeInMillis(max.getTime());
 			result.put("MAX", maxCal.get(Calendar.YEAR));
 		} else {
 			result.put("MAX", 0);
 		}
 		return result;
 	}
 
 	/* Indicator reports */
 
 	/*
 	 * Indicator - overview.
 	 */
 	@Override
 	public Object[] getIndicatorTypeOverview(final String indicatorTypeCode, final String sourceCode, final String languageCode) {
 		// TODO i18n
 		final Query query = em.createQuery("SELECT i.type.code, i.type.name.defaultValue, i.source.code, i.value, i.source.name.defaultValue from Indicator i WHERE i.type.code = :code and ")
 				.setParameter("code", indicatorTypeCode).setMaxResults(1);
 		Object[] queryResult = null;
 		try {
 			queryResult = (Object[]) query.getSingleResult();
 		} catch (final NoResultException e) {
 			// It is possible that no value exists for the given indicator.
 			// So we just put the indicator in the result.
 			queryResult = new Object[] { indicatorTypeCode };
 		}
 		return queryResult;
 	}
 
 	/* ********* */
 	/* Utilities */
 	/* ********* */
 
 	@Override
 	public List<String> getExistingSourcesCodesForYearAndIndicatorType(final int year, final String indicatorTypeCode) {
 		final TimeRange timerange = new TimeRange(Integer.valueOf(year).toString());
 		final TypedQuery<String> query = em
 				.createQuery("SELECT DISTINCT(i.source.code) FROM Indicator i Where i.start = :start AND i.end = :end AND i.periodicity = :periodicity AND i.type.code = :indicatorTypeCode",
 						String.class).setParameter("start", timerange.getStart()).setParameter("end", timerange.getEnd()).setParameter("periodicity", timerange.getPeriodicity())
 				.setParameter("indicatorTypeCode", indicatorTypeCode);
 		return query.getResultList();
 	}
 
 	@Override
 	public List<String> getExistingSourcesCodesForIndicatorType(final String indicatorTypeCode) {
 		final TypedQuery<String> query = em.createQuery("SELECT DISTINCT(i.source.code) FROM Indicator i Where i.type.code = :indicatorTypeCode", String.class).setParameter("indicatorTypeCode",
 				indicatorTypeCode);
 		return query.getResultList();
 	}
 
 }
