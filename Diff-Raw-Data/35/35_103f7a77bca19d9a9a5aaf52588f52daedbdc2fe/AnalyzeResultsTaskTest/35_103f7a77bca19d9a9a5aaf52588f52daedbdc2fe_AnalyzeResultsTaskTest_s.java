 package org.kalibro.core.processing;
 
 import static org.junit.Assert.*;
 
 import java.util.Random;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.kalibro.*;
 import org.kalibro.core.concurrent.Producer;
 import org.kalibro.core.concurrent.Writer;
 import org.kalibro.core.persistence.ConfigurationDatabaseDao;
 import org.kalibro.core.persistence.DatabaseDaoFactory;
 import org.kalibro.core.persistence.ModuleResultDatabaseDao;
 import org.kalibro.tests.UnitTest;
 import org.powermock.core.classloader.annotations.PrepareForTest;
 import org.powermock.modules.junit4.PowerMockRunner;
 
 @RunWith(PowerMockRunner.class)
 @PrepareForTest({AnalyzeResultsTask.class, ModuleResultConfigurer.class})
 public class AnalyzeResultsTaskTest extends UnitTest {
 
 	private static final Long PROCESSING_ID = new Random().nextLong();
 	private static final String REPOSITORY_NAME = "AnalyzeResultsTaskTest repository name";
 
 	private Configuration configurationSnapshot;
 	private ModuleResultDatabaseDao moduleResultDao;
 
 	private ModuleResult softwareResult, classResult;
 
 	private AnalyzeResultsTask analyzeTask;
 
 	@Before
 	public void setUp() throws Exception {
 		mockDatabaseDao();
 		stubModuleResultPreparation();
 		mockStatic(ModuleResultConfigurer.class);
 		analyzeTask = new AnalyzeResultsTask(mockProcessing(), stubProducer());
 	}
 
 	private void mockDatabaseDao() throws Exception {
 		moduleResultDao = mock(ModuleResultDatabaseDao.class);
 		configurationSnapshot = loadFixture("sc", Configuration.class);
 		DatabaseDaoFactory daoFactory = mock(DatabaseDaoFactory.class);
 		ConfigurationDatabaseDao configurationDao = mock(ConfigurationDatabaseDao.class);
 		whenNew(DatabaseDaoFactory.class).withNoArguments().thenReturn(daoFactory);
 		when(daoFactory.createConfigurationDao()).thenReturn(configurationDao);
 		when(daoFactory.createModuleResultDao()).thenReturn(moduleResultDao);
 		when(configurationDao.snapshotFor(PROCESSING_ID)).thenReturn(configurationSnapshot);
 	}
 
 	private void stubModuleResultPreparation() {
		Module softwareModule = new Module(Granularity.SOFTWARE, REPOSITORY_NAME);
		Module classModule = new Module(Granularity.CLASS, "HelloWorld");
		assertEquals(new Module(Granularity.CLASS, "HelloWorld"), classModule);
 		softwareResult = new ModuleResult(null, softwareModule);
 		classResult = new ModuleResult(softwareResult, classModule);
 		when(moduleResultDao.prepareResultFor(softwareModule, PROCESSING_ID)).thenReturn(softwareResult);
 		when(moduleResultDao.prepareResultFor(classModule, PROCESSING_ID)).thenReturn(classResult);
 	}
 
 	private Processing mockProcessing() {
 		Processing processing = mock(Processing.class);
 		Repository repository = mock(Repository.class);
 		when(processing.getId()).thenReturn(PROCESSING_ID);
 		when(processing.getRepository()).thenReturn(repository);
 		when(repository.getName()).thenReturn(REPOSITORY_NAME);
 		return processing;
 	}
 
 	private Producer<NativeModuleResult> stubProducer() {
 		Producer<NativeModuleResult> resultProducer = new Producer<NativeModuleResult>();
 		Writer<NativeModuleResult> writer = resultProducer.createWriter();
		writer.write(loadFixture("HelloWorld-Class", NativeModuleResult.class));
		writer.write(loadFixture("HelloWorld-Software", NativeModuleResult.class));
 		writer.close();
 		return resultProducer;
 	}
 
 	@Test
 	public void shouldAddMetricResults() {
 		NativeMetric lcom4 = loadFixture("lcom4", NativeMetric.class);
 		assertFalse(classResult.hasResultFor(lcom4));
 		analyzeTask.compute();
 
 		assertTrue(softwareResult.hasResultFor(lcom4));
 		MetricResult lcom4Result = classResult.getResultFor(lcom4);
 		assertDoubleEquals(1.0, lcom4Result.getValue());
 		assertTrue(lcom4Result.getDescendantResults().isEmpty());
 	}
 
 	@Test
 	public void shouldAddDescendantResults() {
 		NativeMetric cbo = loadFixture("cbo", NativeMetric.class);
 		assertFalse(softwareResult.hasResultFor(cbo));
 		analyzeTask.compute();
 
 		assertTrue(softwareResult.hasResultFor(cbo));
 		MetricResult cboResult = softwareResult.getResultFor(cbo);
 		assertDoubleEquals(Double.NaN, cboResult.getValue());
 		assertDoubleEquals(0.0, cboResult.getAggregatedValue());
 		assertDeepEquals(list(0.0), cboResult.getDescendantResults());
 	}
 
 	@Test
 	public void shouldConfigureResults() {
 		analyzeTask.compute();
 		verifyStatic(times(2));
 		ModuleResultConfigurer.configure(softwareResult, configurationSnapshot);
 		verifyStatic();
 		ModuleResultConfigurer.configure(classResult, configurationSnapshot);
 	}
 
 	@Test
 	public void shouldSaveResults() {
 		analyzeTask.compute();
 		verify(moduleResultDao, times(2)).save(softwareResult, PROCESSING_ID);
 		verify(moduleResultDao).save(classResult, PROCESSING_ID);
 	}
 
 	@Test
 	public void nextStateShouldBeReady() {
 		assertEquals(ProcessState.READY, analyzeTask.getNextState());
 	}
 }
