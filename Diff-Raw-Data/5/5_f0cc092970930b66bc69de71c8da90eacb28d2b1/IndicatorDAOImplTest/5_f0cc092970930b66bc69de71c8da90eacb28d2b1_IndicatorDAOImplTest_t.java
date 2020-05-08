 package org.ocha.hdx.persistence.dao.currateddata;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import javax.persistence.PersistenceException;
 
 import junit.framework.Assert;
 
 import org.joda.time.DateTime;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.ocha.hdx.IntegrationTestSetUpAndTearDown;
 import org.ocha.hdx.model.DataSerie;
 import org.ocha.hdx.model.validation.ValidationStatus;
 import org.ocha.hdx.persistence.dao.ImportFromCKANDAO;
 import org.ocha.hdx.persistence.entity.ImportFromCKAN;
 import org.ocha.hdx.persistence.entity.curateddata.Entity;
 import org.ocha.hdx.persistence.entity.curateddata.Indicator;
 import org.ocha.hdx.persistence.entity.curateddata.Indicator.Periodicity;
 import org.ocha.hdx.persistence.entity.curateddata.IndicatorType;
 import org.ocha.hdx.persistence.entity.curateddata.IndicatorValue;
 import org.ocha.hdx.persistence.entity.curateddata.Source;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = { "classpath:/ctx-config-test.xml", "classpath:/ctx-core.xml", "classpath:/ctx-dao.xml", "classpath:/ctx-service.xml", "classpath:/ctx-integration-test.xml",
 		"classpath:/ctx-persistence-test.xml" })
 public class IndicatorDAOImplTest {
 
 	@Autowired
 	private IntegrationTestSetUpAndTearDown integrationTestSetUpAndTearDown;
 
 	@Autowired
 	private ImportFromCKANDAO importFromCKANDAO;
 
 	@Autowired
 	private EntityDAO entityDAO;
 
 	@Autowired
 	private EntityTypeDAO entityTypeDAO;
 
 	@Autowired
 	private IndicatorTypeDAO indicatorTypeDAO;
 
 	@Autowired
 	private SourceDAO sourceDAO;
 
 	@Autowired
 	private IndicatorDAO indicatorDAO;
 
 	@Before
 	public void setUp() {
 		integrationTestSetUpAndTearDown.setUp();
 	}
 
 	@After
 	public void tearDown() {
 		integrationTestSetUpAndTearDown.tearDown();
 	}
 
 	@Test
 	public void testListLastIndicators() {
 		final List<Indicator> listLastIndicators = indicatorDAO.listLastIndicators(100);
 		Assert.assertEquals(2, listLastIndicators.size());
 
 		final Entity russia = entityDAO.getEntityByCodeAndType("RUS", "country");
 		final Entity luxembourg = entityDAO.getEntityByCodeAndType("LUX", "country");
 		final List<String> countriesOnlyLuxembourg = new ArrayList<>();
 		countriesOnlyLuxembourg.add("LUX");
 
 		final IndicatorType perCapitaGdp = indicatorTypeDAO.getIndicatorTypeByCode("per-capita-gdp");
 		final IndicatorType pvx040 = indicatorTypeDAO.getIndicatorTypeByCode("PVX040");
 
 		final List<String> indicatorTypesOnlyPerCapita = new ArrayList<>();
 		indicatorTypesOnlyPerCapita.add("per-capita-gdp");
 
 		final Source sourceWB = sourceDAO.getSourceByCode("WB");
 		final Source sourceAcled = sourceDAO.getSourceByCode("acled");
 		final ImportFromCKAN importFromCKAN = importFromCKANDAO.createNewImportRecord("anyResourceId", "anyRevisionId", new Date());
 		final ImportFromCKAN importFromCKAN2 = importFromCKANDAO.createNewImportRecord("anyResourceId", "anyRevisionId", new Date());
 
 		final DateTime dateTime2012 = new DateTime(2012, 1, 1, 0, 0);
 		final Date date2012 = dateTime2012.toDate();
 		final Date date2013 = dateTime2012.plusYears(1).toDate();
 		final Date date2013Feb = dateTime2012.plusYears(1).plusMonths(1).toDate();
 		final Date date2014 = dateTime2012.plusYears(2).toDate();
 
 		try {
 			indicatorDAO.createIndicator(sourceWB, russia, perCapitaGdp, date2013, date2014, Periodicity.YEAR, new IndicatorValue(10000.0), "10000$", ValidationStatus.SUCCESS,
 					"http://www.example.com", importFromCKAN);
 			Assert.fail("Should not be possible to add the same value twice, multiple column constraint not enforced");
 		} catch (final PersistenceException e) {
 			// Expected behavior () caused by a ConstraintViolationException
 		}
 
 		// Should be able to add a very similar indicator, with just another Periodicity
 		indicatorDAO.createIndicator(sourceWB, russia, perCapitaGdp, date2013, date2013Feb, Periodicity.MONTH, new IndicatorValue(10000.0), "10000$", ValidationStatus.SUCCESS,
 				"http://www.example.com", importFromCKAN);
 
 		indicatorDAO.createIndicator(sourceAcled, russia, perCapitaGdp, date2013, date2014, Periodicity.YEAR, new IndicatorValue(9000.0), "9000$", ValidationStatus.SUCCESS, "http://www.example.com",
 				importFromCKAN);
 		Assert.assertEquals(4, indicatorDAO.listLastIndicators(100).size());
 		Assert.assertEquals(1, indicatorDAO.listIndicatorsByPeriodicityAndSourceAndIndicatorType(Periodicity.YEAR, "WB", "per-capita-gdp", null).size());
 		Assert.assertEquals(2, indicatorDAO.listIndicatorsByPeriodicityAndEntityAndIndicatorType(Periodicity.YEAR, "country", "RUS", "per-capita-gdp").size());
 		Assert.assertEquals(1, indicatorDAO.listIndicatorsByYearAndSourceAndIndicatorType(2013, "WB", "per-capita-gdp", null).size());
 		Assert.assertEquals(1, indicatorDAO.listIndicatorsByYearAndSourceAndIndicatorTypes(2013, "WB", indicatorTypesOnlyPerCapita).size());
 
 		indicatorDAO.createIndicator(sourceAcled, luxembourg, perCapitaGdp, date2013, date2014, Periodicity.YEAR, new IndicatorValue(100000.0), "100000$", ValidationStatus.SUCCESS,
 				"http://www.example.com", importFromCKAN);
 		Assert.assertEquals(5, indicatorDAO.listLastIndicators(100).size());
 		Assert.assertEquals(1, indicatorDAO.listIndicatorsByPeriodicityAndSourceAndIndicatorType(Periodicity.YEAR, "WB", "per-capita-gdp", null).size());
 		Assert.assertEquals(2, indicatorDAO.listIndicatorsByPeriodicityAndEntityAndIndicatorType(Periodicity.YEAR, "country", "RUS", "per-capita-gdp").size());
 		Assert.assertEquals(2, indicatorDAO.listIndicatorsByPeriodicityAndSourceAndIndicatorType(Periodicity.YEAR, "acled", "per-capita-gdp", null).size());
 
 		indicatorDAO.createIndicator(sourceAcled, luxembourg, perCapitaGdp, dateTime2012.plusDays(1).toDate(), dateTime2012.plusDays(2).toDate(), Periodicity.DAY, new IndicatorValue(273.97),
 				"237.97$ per day", ValidationStatus.SUCCESS, "http://www.example.com", importFromCKAN);
 
 		Assert.assertEquals(6, indicatorDAO.listLastIndicators(100).size());
 		Assert.assertEquals(1, indicatorDAO.listIndicatorsByPeriodicityAndSourceAndIndicatorType(Periodicity.YEAR, "WB", "per-capita-gdp", null).size());
 		Assert.assertEquals(2, indicatorDAO.listIndicatorsByPeriodicityAndEntityAndIndicatorType(Periodicity.YEAR, "country", "RUS", "per-capita-gdp").size());
 		Assert.assertEquals(2, indicatorDAO.listIndicatorsByPeriodicityAndSourceAndIndicatorType(Periodicity.YEAR, "acled", "per-capita-gdp", null).size());
 
 		indicatorDAO.createIndicator(sourceAcled, luxembourg, perCapitaGdp, dateTime2012.plusDays(2).toDate(), dateTime2012.plusDays(3).toDate(), Periodicity.DAY, new IndicatorValue(273.97),
 				"237.97$ per day", ValidationStatus.SUCCESS, "http://www.example.com", importFromCKAN2);
 
 		Assert.assertEquals(7, indicatorDAO.listLastIndicators(100).size());
 		Assert.assertEquals(1, indicatorDAO.listIndicatorsByPeriodicityAndSourceAndIndicatorType(Periodicity.YEAR, "WB", "per-capita-gdp", null).size());
 		Assert.assertEquals(2, indicatorDAO.listIndicatorsByPeriodicityAndEntityAndIndicatorType(Periodicity.YEAR, "country", "RUS", "per-capita-gdp").size());
 		Assert.assertEquals(1, indicatorDAO.listIndicatorsByYearAndSourceAndIndicatorType(2013, "WB", "per-capita-gdp", null).size());
 
 		indicatorDAO.createIndicator(sourceWB, luxembourg, perCapitaGdp, date2012, date2013, Periodicity.YEAR, new IndicatorValue(273.97), "237.97$ per day", ValidationStatus.SUCCESS,
 				"http://www.example.com", importFromCKAN2);
 
 		Assert.assertEquals(8, indicatorDAO.listLastIndicators(100).size());
 		Assert.assertEquals(2, indicatorDAO.listIndicatorsByPeriodicityAndSourceAndIndicatorType(Periodicity.YEAR, "WB", "per-capita-gdp", null).size());
 		Assert.assertEquals(2, indicatorDAO.listIndicatorsByPeriodicityAndEntityAndIndicatorType(Periodicity.YEAR, "country", "RUS", "per-capita-gdp").size());
 		Assert.assertEquals(1, indicatorDAO.listIndicatorsByYearAndSourceAndIndicatorType(2013, "WB", "per-capita-gdp", null).size());
 		Assert.assertEquals(1, indicatorDAO.listIndicatorsByYearAndSourceAndIndicatorType(2012, "WB", "per-capita-gdp", null).size());
 
 		indicatorDAO.createIndicator(sourceWB, russia, perCapitaGdp, date2012, date2013, Periodicity.YEAR, new IndicatorValue(273.97), "237.97$ per day", ValidationStatus.SUCCESS,
 				"http://www.example.com", importFromCKAN2);
 
 		Assert.assertEquals(9, indicatorDAO.listLastIndicators(100).size());
 		Assert.assertEquals(3, indicatorDAO.listIndicatorsByPeriodicityAndSourceAndIndicatorType(Periodicity.YEAR, "WB", "per-capita-gdp", null).size());
 		Assert.assertEquals(1, indicatorDAO.listIndicatorsByPeriodicityAndSourceAndIndicatorType(Periodicity.YEAR, "WB", "per-capita-gdp", countriesOnlyLuxembourg).size());
 		Assert.assertEquals(3, indicatorDAO.listIndicatorsByPeriodicityAndEntityAndIndicatorType(Periodicity.YEAR, "country", "RUS", "per-capita-gdp").size());
 		Assert.assertEquals(1, indicatorDAO.listIndicatorsByYearAndSourceAndIndicatorType(2013, "WB", "per-capita-gdp", null).size());
 		Assert.assertEquals(2, indicatorDAO.listIndicatorsByYearAndSourceAndIndicatorType(2012, "WB", "per-capita-gdp", null).size());
 
 		Assert.assertEquals(1, indicatorDAO.listIndicatorsByYearAndSourceAndIndicatorTypes(2013, "WB", indicatorTypesOnlyPerCapita).size());
 
 		indicatorDAO.createIndicator(sourceWB, russia, pvx040, date2013, date2014, Periodicity.YEAR, new IndicatorValue(273.97), "237.97$ per day", ValidationStatus.SUCCESS, "http://www.example.com",
 				importFromCKAN2);
 		Assert.assertEquals(1, indicatorDAO.listIndicatorsByYearAndSourceAndIndicatorTypes(2013, "WB", indicatorTypesOnlyPerCapita).size());
 		Assert.assertEquals(1, indicatorDAO.listIndicatorsByYearAndSourceAndIndicatorType(2013, "WB", "per-capita-gdp", null).size());
 		Assert.assertEquals(1, indicatorDAO.listIndicatorsByYearAndSourceAndIndicatorType(2013, "WB", "PVX040", null).size());
 
 		final List<String> indicatorTypes = new ArrayList<>();
 		indicatorTypes.add("per-capita-gdp");
 		indicatorTypes.add("PVX040");
 
 		Assert.assertEquals(2, indicatorDAO.listIndicatorsByYearAndSourceAndIndicatorTypes(2013, "WB", indicatorTypes).size());
 
 		final List<String> sourcesForCapita2013 = indicatorDAO.getExistingSourcesCodesForYearAndIndicatorType(2013, "per-capita-gdp");
 		Assert.assertEquals(2, sourcesForCapita2013.size());
 
 		final List<String> sourcesForCapita = indicatorDAO.getExistingSourcesCodesForIndicatorType("per-capita-gdp");
 		Assert.assertEquals(2, sourcesForCapita.size());
 
 		indicatorDAO.deleteAllIndicatorsFromImport(importFromCKAN.getId());
 
 		Assert.assertEquals(6, indicatorDAO.listLastIndicators(100).size());
 
 	}
 
 	@Test
 	public void testListIndicatorsForCountryOverview() {
 		integrationTestSetUpAndTearDown.setUpDataForCountryOverview();
 
 		{
 			final List<Object[]> listIndicatorsForCountryOverview = indicatorDAO.listIndicatorsForCountryOverview("USA", "FR");
 			Assert.assertEquals(20, listIndicatorsForCountryOverview.size());
 			final Object[] element = listIndicatorsForCountryOverview.get(0);
 			Assert.assertEquals(5, element.length);
 			Assert.assertEquals("CD010", element[0]);
 			Assert.assertEquals("Wikipedia: geography", element[1]);
 			Assert.assertEquals("Url for Usa", element[2].toString());
 			Assert.assertEquals("World Bank", element[4]);
 		}
 
 		{
 			final List<Object[]> listIndicatorsForCountryOverview = indicatorDAO.listIndicatorsForCountryOverview("COL", "FR");
 			Assert.assertEquals(20, listIndicatorsForCountryOverview.size());
 			final Object[] element = listIndicatorsForCountryOverview.get(0);
 			Assert.assertEquals(1, element.length);
 			Assert.assertEquals("CD010", element[0]);
 		}
 
 		integrationTestSetUpAndTearDown.tearDownDataForCountryOverview();
 	}
 
 	@Test
 	public void testListIndicatorsForCountryCrisisHistory() {
 
 		integrationTestSetUpAndTearDown.setUpDataForCountryCrisisHistory();
 
 		final Map<Integer, List<Object[]>> listIndicatorsForCountryCrisisHistory = indicatorDAO.listIndicatorsForCountryCrisisHistory("USA", 2005, 2010, "FR");
 		Assert.assertEquals(6, listIndicatorsForCountryCrisisHistory.size());
 
 		final List<Object[]> results2005 = listIndicatorsForCountryCrisisHistory.get(new Integer(2005));
 		Assert.assertEquals(4, results2005.size());
 		Assert.assertEquals(1, results2005.get(0).length);
 		Assert.assertEquals("CH070", results2005.get(0)[0]);
 
 		final List<Object[]> results2006 = listIndicatorsForCountryCrisisHistory.get(new Integer(2006));
 		Assert.assertEquals(4, results2006.size());
 		Assert.assertEquals(1, results2006.get(0).length);
 		Assert.assertEquals("CH070", results2006.get(0)[0]);
 
 		final List<Object[]> results2007 = listIndicatorsForCountryCrisisHistory.get(new Integer(2007));
 		Assert.assertEquals(4, results2007.size());
 		Assert.assertEquals(1, results2007.get(0).length);
 		Assert.assertEquals("CH070", results2007.get(0)[0]);
 
 		final List<Object[]> results2008 = listIndicatorsForCountryCrisisHistory.get(new Integer(2008));
 		Assert.assertEquals(4, results2008.size());
		Assert.assertEquals(7, results2008.get(0).length);
 		Assert.assertEquals(1, results2008.get(1).length);
 		Assert.assertEquals("CH070", results2008.get(0)[0]);
 		Assert.assertEquals("Number of disasters", results2008.get(0)[1]);
 		Assert.assertEquals("uno", results2008.get(0)[2]);
 		Assert.assertEquals("5.0", results2008.get(0)[3].toString());
 		Assert.assertEquals("emdat", results2008.get(0)[5]);
 
 		final List<Object[]> results2009 = listIndicatorsForCountryCrisisHistory.get(new Integer(2009));
 		Assert.assertEquals(4, results2009.size());
 		Assert.assertEquals(1, results2009.get(0).length);
		Assert.assertEquals(7, results2009.get(1).length);
 		Assert.assertEquals("CH070", results2009.get(0)[0]);
 		Assert.assertEquals("CH080", results2009.get(1)[0]);
 		Assert.assertEquals("People killed in disasters", results2009.get(1)[1]);
 		Assert.assertEquals("uno", results2009.get(1)[2]);
 		Assert.assertEquals("1000.0", results2009.get(1)[3].toString());
 		Assert.assertEquals("emdat", results2008.get(0)[5]);
 
 		integrationTestSetUpAndTearDown.tearDownDataForCountryCrisisHistory();
 
 	}
 
 	@Test
 	public void testGetMinMaxDatesForIndicators() {
 
 		integrationTestSetUpAndTearDown.setUpDataForCountryCrisisHistory();
 
 		final List<DataSerie> dataSeries = new ArrayList<DataSerie>();
 		dataSeries.add(new DataSerie("CH070", "emdat"));
 		dataSeries.add(new DataSerie("CH080", "emdat"));
 
 		final Map<String, Integer> minMaxDatesForCountryIndicators = indicatorDAO.getMinMaxDatesForCountryIndicators("USA", dataSeries);
 		Assert.assertEquals(2, minMaxDatesForCountryIndicators.size());
 		Assert.assertEquals(new Integer(2008), minMaxDatesForCountryIndicators.get("MIN"));
 		Assert.assertEquals(new Integer(2009), minMaxDatesForCountryIndicators.get("MAX"));
 
 		integrationTestSetUpAndTearDown.tearDownDataForCountryCrisisHistory();
 	}
 
 	@Test
 	public void testIndicatorTypeOverview() {
 		integrationTestSetUpAndTearDown.setUpDataForCountryOverview();
 
 		{
 			final List<Object[]> listIndicatorsForCountryOverview = indicatorDAO.listIndicatorsForCountryOverview("USA", "FR");
 			Assert.assertEquals(20, listIndicatorsForCountryOverview.size());
 			final Object[] element = listIndicatorsForCountryOverview.get(0);
 			Assert.assertEquals(5, element.length);
 			Assert.assertEquals("CD010", element[0]);
 			Assert.assertEquals("Wikipedia: geography", element[1]);
 			Assert.assertEquals("Url for Usa", element[2].toString());
 			Assert.assertEquals("World Bank", element[4]);
 		}
 
 		{
 			final List<Object[]> listIndicatorsForCountryOverview = indicatorDAO.listIndicatorsForCountryOverview("COL", "FR");
 			Assert.assertEquals(20, listIndicatorsForCountryOverview.size());
 			final Object[] element = listIndicatorsForCountryOverview.get(0);
 			Assert.assertEquals(1, element.length);
 			Assert.assertEquals("CD010", element[0]);
 		}
 
 		integrationTestSetUpAndTearDown.tearDownDataForCountryOverview();
 	}
 
 }
