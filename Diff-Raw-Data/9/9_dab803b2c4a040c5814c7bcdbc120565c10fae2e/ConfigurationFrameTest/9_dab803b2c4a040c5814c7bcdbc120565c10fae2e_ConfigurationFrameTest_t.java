 package org.kalibro.desktop.configuration;
 
 import static org.junit.Assert.*;
 import static org.mockito.Matchers.*;
 
 import java.beans.PropertyVetoException;
 
 import javax.swing.JPanel;
 import javax.swing.JTable;
 import javax.swing.WindowConstants;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.kalibro.KalibroTestCase;
 import org.kalibro.core.concurrent.Task;
 import org.kalibro.core.model.Configuration;
 import org.kalibro.core.model.ConfigurationFixtures;
 import org.kalibro.core.model.MetricConfiguration;
 import org.kalibro.desktop.ComponentFinder;
 import org.kalibro.desktop.swingextension.Button;
 import org.kalibro.desktop.swingextension.panel.CardStackPanel;
 import org.mockito.Mockito;
 import org.mockito.internal.util.reflection.Whitebox;
 import org.powermock.api.mockito.PowerMockito;
 import org.powermock.core.classloader.annotations.PowerMockIgnore;
 import org.powermock.core.classloader.annotations.PrepareOnlyThisForTest;
 import org.powermock.modules.junit4.PowerMockRunner;
 
 @RunWith(PowerMockRunner.class)
 @PowerMockIgnore("javax.*")
 @PrepareOnlyThisForTest(ConfigurationFrame.class)
 public class ConfigurationFrameTest extends KalibroTestCase {
 
 	private Configuration configuration;
 	private ConfigurationPanel panel;
 	private MetricConfigurationController metricConfigurationController;
 
 	private ConfigurationFrame frame;
 	private ComponentFinder finder;
 
 	@Before
 	public void setUp() throws Exception {
 		configuration = ConfigurationFixtures.simpleConfiguration();
 		mockPanel();
 		mockMetricConfigurationController();
 		frame = new ConfigurationFrame(configuration);
 		finder = new ComponentFinder(frame);
 	}
 
 	private void mockPanel() throws Exception {
 		panel = PowerMockito.spy(new ConfigurationPanel());
 		PowerMockito.whenNew(ConfigurationPanel.class).withNoArguments().thenReturn(panel);
 	}
 
 	private void mockMetricConfigurationController() throws Exception {
 		metricConfigurationController = PowerMockito.mock(MetricConfigurationController.class);
 		PowerMockito.whenNew(MetricConfigurationController.class).withArguments(eq(configuration), any())
 			.thenReturn(metricConfigurationController);
 	}
 
 	@Test(timeout = UNIT_TIMEOUT)
 	public void titleShouldHaveConfigurationName() {
 		assertEquals(configuration.getName() + " - Configuration", frame.getTitle());
 	}
 
 	@Test(timeout = UNIT_TIMEOUT)
 	public void shouldDoNothingOnCloseByDefault() {
 		assertEquals(WindowConstants.DO_NOTHING_ON_CLOSE, frame.getDefaultCloseOperation());
 	}
 
 	@Test(timeout = UNIT_TIMEOUT)
 	public void shouldBeResizable() {
 		assertTrue(frame.isResizable());
 	}
 
 	@Test(timeout = UNIT_TIMEOUT)
 	public void shouldBeClosable() {
 		assertTrue(frame.isClosable());
 	}
 
 	@Test(timeout = UNIT_TIMEOUT)
 	public void shouldBeMaximizable() {
 		assertTrue(frame.isMaximizable());
 	}
 
 	@Test(timeout = UNIT_TIMEOUT)
 	public void shouldBeIconifiable() {
 		assertTrue(frame.isIconifiable());
 	}
 
 	@Test(timeout = UNIT_TIMEOUT)
 	public void shouldShowConfigurationOnPanel() {
 		Mockito.verify(panel).set(configuration);
 	}
 
 	@Test(timeout = UNIT_TIMEOUT)
 	public void shouldListenToMetricConfigurationsPanel() {
 		Mockito.verify(panel).addMetricConfigurationsListener(frame);
 	}
 
 	@Test(timeout = UNIT_TIMEOUT)
 	public void shouldAddMetricConfiguration() {
 		Configuration newConfiguration = mockPanelGet();
 		finder.find("add", Button.class).doClick();
 
 		Mockito.verify(panel).get();
		Mockito.verify(metricConfigurationController).addMetricConfiguration();
 		assertSame(newConfiguration, frame.getConfiguration());
 	}
 
 	@Test(timeout = UNIT_TIMEOUT)
 	public void shouldEditMetricConfiguration() {
 		Configuration newConfiguration = mockPanelGet();
 		finder.find("metricConfigurations", JTable.class).getSelectionModel().setSelectionInterval(0, 0);
 		finder.find("edit", Button.class).doClick();
 
 		MetricConfiguration metricConfiguration = configuration.getMetricConfigurations().iterator().next();
 		Mockito.verify(panel).get();
 		Mockito.verify(metricConfigurationController).edit(metricConfiguration);
 		assertSame(newConfiguration, frame.getConfiguration());
 	}
 
 	private Configuration mockPanelGet() {
 		Configuration newConfiguration = new Configuration();
 		newConfiguration.setName(configuration.getName());
 		PowerMockito.when(panel.get()).thenReturn(newConfiguration);
 		return newConfiguration;
 	}
 
 	@Test(timeout = UNIT_TIMEOUT)
 	public void shouldRefreshConfigurationWhenPanelIsRemovedFromCardStack() {
 		CardStackPanel cardStack = (CardStackPanel) Whitebox.getInternalState(frame, "cardStack");
 		cardStack.push(new JPanel());
 
 		Mockito.reset(panel);
 		cardStack.pop();
 		Mockito.verify(panel).set(configuration);
 	}
 
 	@Test(timeout = UNIT_TIMEOUT)
 	public void shouldSelectHidingPropertyVetoException() throws Exception {
 		frame = PowerMockito.spy(frame);
 		frame.select();
 		Mockito.verify(frame).setSelected(true);
 
 		PowerMockito.doThrow(new PropertyVetoException("", null)).when(frame).setSelected(true);
 		checkException(new Task() {
 
 			@Override
 			public void perform() {
 				frame.select();
 			}
 		}, IllegalStateException.class, "java.beans.PropertyVetoException: ", PropertyVetoException.class);
 	}
 }
