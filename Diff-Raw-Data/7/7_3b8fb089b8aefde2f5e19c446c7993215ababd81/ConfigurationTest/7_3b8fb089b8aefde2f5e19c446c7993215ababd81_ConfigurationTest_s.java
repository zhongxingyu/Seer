 package org.kalibro;
 
 import static org.junit.Assert.*;
 
 import java.io.File;
 import java.util.SortedSet;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.kalibro.core.abstractentity.AbstractEntity;
 import org.kalibro.core.concurrent.VoidTask;
 import org.kalibro.dao.ConfigurationDao;
 import org.kalibro.dao.DaoFactory;
 import org.kalibro.dao.MetricConfigurationDao;
 import org.kalibro.tests.UnitTest;
 import org.powermock.core.classloader.annotations.PrepareForTest;
 import org.powermock.modules.junit4.PowerMockRunner;
 import org.powermock.reflect.Whitebox;
 
 @RunWith(PowerMockRunner.class)
 @PrepareForTest({AbstractEntity.class, DaoFactory.class})
 public class ConfigurationTest extends UnitTest {
 
 	private ConfigurationDao dao;
 
 	private CompoundMetric sc;
 	private NativeMetric cbo, lcom4;
 	private Configuration configuration;
 
 	@Before
 	public void setUp() {
 		dao = mock(ConfigurationDao.class);
 		mockStatic(DaoFactory.class);
 		when(DaoFactory.getConfigurationDao()).thenReturn(dao);
 		sc = loadFixture("sc", CompoundMetric.class);
 		cbo = loadFixture("cbo", NativeMetric.class);
 		lcom4 = loadFixture("lcom4", NativeMetric.class);
 		configuration = loadFixture("sc", Configuration.class);
 	}
 
 	@Test
 	public void shouldImportFromFile() throws Exception {
 		File file = mock(File.class);
 		mockStatic(AbstractEntity.class);
 		when(AbstractEntity.class, "importFrom", file, Configuration.class).thenReturn(configuration);
 
 		assertSame(configuration, Configuration.importFrom(file));
 		verifyPrivate(AbstractEntity.class).invoke("importFrom", file, Configuration.class);
 	}
 
 	@Test
 	public void shouldGetAllConfigurations() {
 		SortedSet<Configuration> configurations = mock(SortedSet.class);
 		when(dao.all()).thenReturn(configurations);
 		assertSame(configurations, Configuration.all());
 	}
 
 	@Test
 	public void shouldSortByName() {
 		assertSorted(withName("A"), withName("B"), withName("C"), withName("X"), withName("Y"), withName("Z"));
 	}
 
 	@Test
 	public void shouldIdentifyByName() {
 		assertEquals(configuration, withName(configuration.getName()));
 	}
 
 	private Configuration withName(String name) {
 		return new Configuration(name);
 	}
 
 	@Test
 	public void checkConstruction() {
 		configuration = new Configuration();
 		assertFalse(configuration.hasId());
 		assertEquals("New configuration", configuration.getName());
 		assertEquals("", configuration.getDescription());
 		assertTrue(configuration.getMetricConfigurations().isEmpty());
 	}
 
 	@Test
 	public void shouldSetConfigurationOnMetricConfigurations() {
 		MetricConfiguration metricConfiguration = mock(MetricConfiguration.class);
 		configuration.setMetricConfigurations(asSortedSet(metricConfiguration));
 		assertDeepEquals(asSet(metricConfiguration), configuration.getMetricConfigurations());
 		verify(metricConfiguration).setConfiguration(configuration);
 	}
 
 	@Test
 	public void shouldSetMetricConfigurationsWithoutTouchingThem() {
 		// required for lazy loading
 		SortedSet<MetricConfiguration> metricConfigurations = mock(SortedSet.class);
 		configuration.setMetricConfigurations(metricConfigurations);
 		verifyZeroInteractions(metricConfigurations);
 	}
 
 	@Test
 	public void shouldAddMetricConfigurationIfItDoesNotConflictWithExistingOnes() {
 		configuration.addMetricConfiguration(new MetricConfiguration());
 		assertThat(new VoidTask() {
 
 			@Override
 			protected void perform() throws Throwable {
 				configuration.addMetricConfiguration(new MetricConfiguration(new CompoundMetric("sc")));
 			}
 		}).throwsException().withMessage("Metric with code 'sc' already exists in the configuration.");
 	}
 
 	@Test
 	public void shouldRemoveMetricConfiguration() {
 		MetricConfiguration metricConfiguration = mock(MetricConfiguration.class);
 		SortedSet<MetricConfiguration> metricConfigurations = spy(asSortedSet(metricConfiguration));
 		configuration.setMetricConfigurations(metricConfigurations);
 
 		configuration.removeMetricConfiguration(metricConfiguration);
 		verify(metricConfigurations).remove(metricConfiguration);
 		verify(metricConfiguration).setConfiguration(null);
 	}
 
 	@Test
 	public void shouldGetCompoundMetrics() {
 		assertDeepEquals(asSet(sc), configuration.getCompoundMetrics());
 	}
 
 	@Test
 	public void shouldRetrieveNativeMetricsPerBaseTool() {
 		BaseTool baseTool = loadFixture("inexistent", BaseTool.class);
 		assertDeepEquals(asMap(baseTool, asSet(cbo, lcom4)), configuration.getNativeMetrics());
 	}
 
 	@Test
 	public void shouldAnswerIfContainsMetric() {
 		assertTrue(configuration.containsMetric(cbo));
 		assertTrue(configuration.containsMetric(lcom4));
 		assertTrue(configuration.containsMetric(sc));
 		assertFalse(configuration.containsMetric(new CompoundMetric()));
 	}
 
 	@Test
 	public void shouldGetConfigurationForMetric() {
 		assertDeepEquals(cbo, configuration.getConfigurationFor(cbo).getMetric());
 		assertDeepEquals(lcom4, configuration.getConfigurationFor(lcom4).getMetric());
 		assertDeepEquals(sc, configuration.getConfigurationFor(sc).getMetric());
 		assertThat(getConfigurationFor(new CompoundMetric())).throwsException()
 			.withMessage("No configuration found for metric: New metric");
 	}
 
 	private VoidTask getConfigurationFor(final Metric metric) {
 		return new VoidTask() {
 
 			@Override
 			protected void perform() {
 				configuration.getConfigurationFor(metric);
 			}
 		};
 	}
 
 	@Test
 	public void shouldValidateScripts() {
		MetricConfiguration scConfiguration = new MetricConfiguration(sc);
		configuration.addMetricConfiguration(scConfiguration);
 		configuration.validateScripts();
 
 		sc.setScript("return null;");
		assertThat(validateScripts()).throwsException()
			.withMessage("Error evaluating Javascript for: structuralComplexity");
 	}
 
 	private VoidTask validateScripts() {
 		return new VoidTask() {
 
 			@Override
 			protected void perform() throws Exception {
 				configuration.validateScripts();
 			}
 		};
 	}
 
 	@Test
 	public void shouldRequiredNameToSave() {
 		configuration.setName(" ");
 		assertThat(save()).throwsException().withMessage("Configuration requires name.");
 	}
 
 	private VoidTask save() {
 		return new VoidTask() {
 
 			@Override
 			protected void perform() {
 				configuration.save();
 			}
 		};
 	}
 
 	@Test
 	public void shouldUpdateIdAndMetricConfigurationsOnSave() {
 		Long id = mock(Long.class);
 		MetricConfiguration metricConfiguration = mockMetricConfiguration(id);
 		when(dao.save(configuration)).thenReturn(id);
 
 		assertFalse(configuration.hasId());
 		configuration.save();
 		assertSame(id, configuration.getId());
 		assertDeepEquals(asSet(metricConfiguration), configuration.getMetricConfigurations());
 	}
 
 	private MetricConfiguration mockMetricConfiguration(Long id) {
 		MetricConfiguration metricConfiguration = mock(MetricConfiguration.class);
 		MetricConfigurationDao metricConfigurationDao = mock(MetricConfigurationDao.class);
 		when(DaoFactory.getMetricConfigurationDao()).thenReturn(metricConfigurationDao);
 		when(metricConfigurationDao.metricConfigurationsOf(id)).thenReturn(asSortedSet(metricConfiguration));
 		return metricConfiguration;
 	}
 
 	@Test
 	public void shouldDeleteIfHasId() {
 		assertFalse(configuration.hasId());
 		configuration.delete();
 		verify(dao, never()).delete(any(Long.class));
 
 		Whitebox.setInternalState(configuration, "id", 42L);
 		assertTrue(configuration.hasId());
 
 		configuration.delete();
 		verify(dao).delete(42L);
 		assertFalse(configuration.hasId());
 	}
 
 	@Test
 	public void shouldNotifyMetricConfigurationsOfDeletion() {
 		MetricConfiguration metricConfiguration = mock(MetricConfiguration.class);
 		configuration.setMetricConfigurations(asSortedSet(metricConfiguration));
 
 		configuration.delete();
 		verify(metricConfiguration).deleted();
 	}
 
 	@Test
 	public void toStringShouldBeName() {
 		assertEquals(configuration.getName(), "" + configuration);
 	}
 }
