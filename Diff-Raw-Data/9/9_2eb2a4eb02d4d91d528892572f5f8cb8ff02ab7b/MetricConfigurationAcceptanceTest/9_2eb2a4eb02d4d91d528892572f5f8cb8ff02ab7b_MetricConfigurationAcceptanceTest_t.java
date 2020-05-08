 package org.kalibro;
 
 import static org.junit.Assert.*;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.experimental.theories.Theory;
 import org.kalibro.core.concurrent.VoidTask;
 import org.kalibro.tests.AcceptanceTest;
 
 public class MetricConfigurationAcceptanceTest extends AcceptanceTest {
 
 	private MetricConfiguration metricConfiguration;
 	private Configuration configuration;
 
 	@Before
 	public void setUp() {
 		configuration = loadFixture("sc", Configuration.class);
 	}
 
 	@After
 	public void tearDown() {
 		configuration.delete();
 	}
 
 	@Theory
 	public void testCrud(SupportedDatabase databaseType) {
 		changeDatabase(databaseType);
 		configuration.save();
 		metricConfiguration = configuration.getMetricConfigurations().first();
 
 		assertSaved();
 
 		metricConfiguration.setWeight(0.0);
 		assertDifferentFromSaved();
 
 		metricConfiguration.save();
 		assertSaved();
 
 		metricConfiguration.delete();
 		assertFalse(configuration.getMetricConfigurations().contains(metricConfiguration));
 		assertFalse(Configuration.all().first().getMetricConfigurations().contains(metricConfiguration));
 	}
 
 	private void assertSaved() {
 		assertDeepEquals(metricConfiguration, Configuration.all().first().getMetricConfigurations().first());
 	}
 
 	private void assertDifferentFromSaved() {
 		MetricConfiguration saved = Configuration.all().first().getMetricConfigurations().first();
 		assertFalse(metricConfiguration.deepEquals(saved));
 	}
 
 	@Test
 	public void metricConfigurationIsRequiredToBeInConfiguration() {
		assertThat(saveNew()).throwsException().withMessage("Metric is not in any configuration.");
 	}
 
 	private VoidTask saveNew() {
 		return new VoidTask() {
 
 			@Override
 			protected void perform() {
 				new MetricConfiguration().save();
 			}
 		};
 	}
 
 	@Test
 	public void shouldNotSetConflictingCode() {
 		metricConfiguration = configuration.getMetricConfigurations().first();
 		assertThat(new VoidTask() {
 
 			@Override
 			protected void perform() throws Throwable {
 				metricConfiguration.setCode("loc");
 			}
 		}).throwsException().withMessage("Metric configuration with code loc already exists in the configuration.");
 		assertEquals("acc", metricConfiguration.getCode());
 	}
 }
