 package org.kalibro;
 
 import static org.junit.Assert.*;
 import static org.kalibro.Granularity.*;
 import static org.kalibro.ProcessState.*;
 
 import java.util.Set;
 
 import org.junit.Before;
 import org.junit.experimental.theories.Theory;
 import org.kalibro.dao.DaoFactory;
 import org.kalibro.dao.ProcessingDao;
 import org.kalibro.tests.AcceptanceTest;
 import org.powermock.reflect.Whitebox;
 
 public class ProcessingAcceptanceTest extends AcceptanceTest {
 
 	private static final String REPOSITORY_NAME = "HelloWorldDirectory";
 
 	private Metric totalCof, cbo, lcom4, sc;
 	private Configuration configuration;
 
 	private Repository repository;
 	private Processing processing;
 
 	@Before
 	public void setUp() {
 		loadConfigurationFixtures();
 		String address = repositoriesDirectory().getAbsolutePath() + "/HelloWorldDirectory/";
 		repository = new Repository(REPOSITORY_NAME, RepositoryType.LOCAL_DIRECTORY, address);
 		repository.setConfiguration(configuration);
 		new Project("Hello World").addRepository(repository);
 	}
 
 	private void loadConfigurationFixtures() {
 		totalCof = loadFixture("total_cof", NativeMetric.class);
 		cbo = loadFixture("cbo", NativeMetric.class);
 		lcom4 = loadFixture("lcom4", NativeMetric.class);
 		sc = loadFixture("sc", CompoundMetric.class);
 		configuration = loadFixture("sc-analizo", Configuration.class);
 	}
 
 	@Theory
 	public void shouldProcessNormally(SupportedDatabase databaseType) throws Exception {
 		resetDatabase(databaseType);
 		assertFalse(Processing.hasProcessing(repository));
 
		long start = process();
		verifyProcessDone(start);
 		verifyResults();
 
 		// should allow changing repository type
 		repository.setType(RepositoryType.GIT);
 		repository.setAddress(repositoriesDirectory().getAbsolutePath() + "/HelloWorldGit/");
		start = process();
		verifyProcessDone(start);
 	}
 
	private void verifyProcessDone(long start) {
 		processing = Processing.lastProcessing(repository);
		assertSorted(start, processing.getDate().getTime(), System.currentTimeMillis());
 		assertProcessingReady();
		long totalTime = System.currentTimeMillis() - start;
 		verifyStateTime(LOADING, totalTime);
 		verifyStateTime(COLLECTING, totalTime);
 		verifyStateTime(ANALYZING, totalTime);
 	}
 
 	private void verifyStateTime(ProcessState state, long totalTime) {
 		Long stateTime = processing.getStateTime(state);
		assertTrue("Time for " + state + ": " + stateTime, 0 < stateTime && stateTime < totalTime);
 	}
 
 	private void verifyResults() {
 		ModuleResult root = processing.getResultsRoot();
 		assertFalse(root.hasParent());
 		verifySoftwareResult(root);
 
 		assertEquals(1, root.getChildren().size());
 		verifyClassResult(root.getChildren().first());
 	}
 
 	private void verifySoftwareResult(ModuleResult root) {
 		assertDeepEquals(new Module(SOFTWARE, REPOSITORY_NAME), root.getModule());
 		assertDoubleEquals(10.0, root.getGrade());
 
 		assertDoubleEquals(1.0, root.getResultFor(totalCof).getValue());
 		assertFalse(root.getResultFor(totalCof).hasRange());
 		verifyClassMetricResults(root);
 	}
 
 	private void verifyClassResult(ModuleResult child) {
 		assertDeepEquals(new Module(CLASS, "HelloWorld"), child.getModule());
 		assertDoubleEquals(10.0, child.getGrade());
 
 		assertFalse(child.hasResultFor(totalCof));
 		verifyClassMetricResults(child);
 	}
 
 	private void verifyClassMetricResults(ModuleResult moduleResult) {
 		assertDoubleEquals(0.0, moduleResult.getResultFor(cbo).getAggregatedValue());
 		assertDoubleEquals(1.0, moduleResult.getResultFor(lcom4).getAggregatedValue());
 		assertDoubleEquals(0.0, moduleResult.getResultFor(sc).getAggregatedValue());
 
 		assertFalse(moduleResult.getResultFor(cbo).hasRange());
 		assertTrue(moduleResult.getResultFor(lcom4).hasRange());
 		assertFalse(moduleResult.getResultFor(sc).hasRange());
 	}
 
 	@Theory
 	public void shouldRetrieveErrorLoading(SupportedDatabase databaseType) throws Exception {
 		resetDatabase(databaseType);
 		repository.setAddress("/invalid/address/");
 		process();
 
 		processing = Processing.lastProcessing(repository);
 		assertEquals(ERROR, processing.getState());
 		assertEquals(LOADING, processing.getStateWhenErrorOcurred());
 		assertNull(processing.getStateTime(COLLECTING));
 		assertNull(processing.getStateTime(ANALYZING));
 		assertNull(processing.getResultsRoot());
 		assertEquals("Error while executing command: cp -ru /invalid/address/ .", processing.getError().getMessage());
 	}
 
 	@Theory
 	public void shouldRetrieveErrorCalculatingCompoundMetric(SupportedDatabase databaseType) throws Exception {
 		resetDatabase(databaseType);
 		configuration.getCompoundMetrics().first().setScript("return cbo == 0 ? null : cbo;");
 		process();
 
 		processing = Processing.lastProcessing(repository);
 		assertProcessingReady();
 
 		ModuleResult root = processing.getResultsRoot();
 		verifyScError(root);
 		verifyScError(root.getChildren().first());
 	}
 
 	private void assertProcessingReady() {
 		if (processing.getState() == ERROR)
 			throw new KalibroException("Error while " + processing.getStateWhenErrorOcurred(), processing.getError());
 		assertEquals(READY, processing.getState());
 	}
 
 	private void verifyScError(ModuleResult moduleResult) {
 		assertTrue(moduleResult.getResultFor(sc).hasError());
 		assertEquals("Error evaluating Javascript for: sc", moduleResult.getResultFor(sc).getError().getMessage());
 	}
 
 	@Theory
 	public void deleteProjectShouldCascadeToProcessings(SupportedDatabase databaseType) throws Exception {
 		resetDatabase(databaseType);
 		repository.setAddress("/invalid/address/");
 		process();
 		assertTrue(Processing.hasProcessing(repository));
 
 		repository.getProject().delete();
 		assertTrue(allProcessings().isEmpty());
 	}
 
 	private Set<Processing> allProcessings() throws Exception {
 		ProcessingDao processingDao = DaoFactory.getProcessingDao();
 		Set<Processing> allProcessings = Whitebox.invokeMethod(processingDao, "all");
 		return allProcessings;
 	}
 
	private long process() throws InterruptedException {
		long start = System.currentTimeMillis();
 		repository.process();
		while (isProcessOngoing())
 			Thread.sleep(2000);
		return start;
 	}
 
 	private boolean isProcessOngoing() {
 		return !Processing.hasProcessing(repository) || Processing.lastProcessingState(repository).isTemporary();
 	}
 }
