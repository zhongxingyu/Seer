 package org.kalibro.core.persistence;
 
 import static org.junit.Assert.*;
 
 import java.util.*;
 
 import javax.persistence.TypedQuery;
 
 import org.junit.Test;
 import org.kalibro.Granularity;
 import org.kalibro.MetricResult;
 import org.kalibro.Module;
 import org.kalibro.ModuleResult;
 import org.kalibro.core.persistence.record.DescendantResultRecord;
 import org.kalibro.core.persistence.record.MetricResultRecord;
 import org.powermock.core.classloader.annotations.PrepareForTest;
 
 @PrepareForTest({MetricResultDatabaseDao.class, ModuleResult.class})
 public class MetricResultDatabaseDaoTest extends
 	DatabaseDaoTestCase<MetricResult, MetricResultRecord, MetricResultDatabaseDao> {
 
 	private static final Long ID = new Random().nextLong();
 	private static final Long TIME = new Random().nextLong();
 	private static final Date DATE = new Date(TIME);
 	private static final String METRIC_NAME = "MetricResultDatabaseDaoTest metric name";
 
 	@Test
 	public void shouldGetDescendantResults() {
 		DescendantResultRecord result = mock(DescendantResultRecord.class);
 		TypedQuery<DescendantResultRecord> descendantQuery = mock(TypedQuery.class);
 		doReturn(descendantQuery).when(dao).createQuery(anyString(), eq(DescendantResultRecord.class));
 		when(descendantQuery.getResultList()).thenReturn(list(result));
 		when(result.convert()).thenReturn(42.0);
 
 		assertDeepEquals(list(42.0), dao.descendantResultsOf(ID));
 		verify(descendantQuery).setParameter("metricResultId", ID);
 	}
 
 	@Test
 	public void shouldGetMetricResultsOfModuleResult() {
 		assertDeepEquals(set(entity), dao.metricResultsOf(ID));
 
 		verify(dao).createRecordQuery("metricResult.moduleResult = :moduleResultId");
 		verify(query).setParameter("moduleResultId", ID);
 	}
 
 	@Test
 	public void shouldGetMetricResultHistory() throws Exception {
 		mockModuleResult("org");
 		TypedQuery<Object[]> historyQuery = mock(TypedQuery.class);
 		doReturn(historyQuery).when(dao).createQuery(anyString(), eq(Object[].class));
 		List<Object[]> results = new ArrayList<Object[]>();
 		results.add(new Object[]{TIME, record});
 		when(historyQuery.getResultList()).thenReturn(results);
 
 		SortedMap<Date, MetricResult> history = dao.historyOf(METRIC_NAME, ID);
 		assertEquals(1, history.size());
 		assertDeepEquals(entity, history.get(DATE));
 		verify(historyQuery).setParameter("metricName", METRIC_NAME);
 		verify(historyQuery).setParameter("moduleName", "org");
 		verify(historyQuery).setParameter("moduleResultId", ID);
 	}
 
 	private void mockModuleResult(String moduleName) throws Exception {
 		Module module = new Module(Granularity.PACKAGE, moduleName);
 		ModuleResult moduleResult = mock(ModuleResult.class);
 		ModuleResultDatabaseDao moduleResultDao = mock(ModuleResultDatabaseDao.class);
 		whenNew(ModuleResultDatabaseDao.class).withNoArguments().thenReturn(moduleResultDao);
 		when(moduleResultDao.get(ID)).thenReturn(moduleResult);
 		when(moduleResult.getModule()).thenReturn(module);
 	}
 
 	@Test
 	public void shouldSave() throws Exception {
 		assertSame(entity, dao.save(entity, ID));
 		verifyNew(MetricResultRecord.class).withArguments(entity, ID);
 		verify(dao).save(record);
 	}
 
 	@Test
 	public void shouldCheckExistenceOfMetricResult() {
 		Random random = new Random();
 		Long moduleResultId = random.nextLong();
 		Long configurationId = random.nextLong();
		TypedQuery<Long> countQuery = mock(TypedQuery.class);
		doReturn(countQuery).when(dao).createQuery(anyString(), eq(Long.class));
 
		when(countQuery.getSingleResult()).thenReturn(1L);
 		assertTrue(dao.metricResultExists(moduleResultId, configurationId));
 
		when(countQuery.getSingleResult()).thenReturn(0L);
 		assertFalse(dao.metricResultExists(moduleResultId, configurationId));
 	}
 
 	@Test
 	public void shouldInsertDescendatResults() throws Exception {
 		Random random = new Random();
 		Double value = random.nextDouble();
 		Long moduleResultId = random.nextLong();
 		Long configurationId = random.nextLong();
 		DescendantResultRecord result = mock(DescendantResultRecord.class);
 		whenNew(DescendantResultRecord.class).withArguments(value, moduleResultId, configurationId).thenReturn(result);
 		doNothing().when(dao).saveAll(list(result));
 
 		dao.addDescendantResults(list(value), moduleResultId, configurationId);
 		verifyNew(DescendantResultRecord.class).withArguments(value, moduleResultId, configurationId);
 		verify(dao).saveAll(list(result));
 	}
 }
