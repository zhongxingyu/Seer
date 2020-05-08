 package org.jfree.chart.editor;
 
 import java.awt.Component;
 import java.awt.ComponentOrientation;
 
 import javax.swing.JPanel;
 
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 
 import org.jfree.chart.ChartFactory;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.plot.PlotOrientation;
 import org.jfree.data.category.CategoryDataset;
 import org.jfree.data.general.DatasetUtilities;
 
 public class DefaultChartEditorTests extends TestCase {
 
 	JFreeChart chart;
 
 	/**
 	 * Returns the tests as a test suite.
 	 * 
 	 * @return The test suite.
 	 */
 	public static Test suite() {
 		return new TestSuite(DefaultChartEditorTests.class);
 	}
 
 	/**
 	 * Constructs a new set of tests.
 	 * 
 	 * @param name
 	 *            the name of the tests.
 	 */
 	public DefaultChartEditorTests(String name) {
 		super(name);
 	}
 
 	/**
 	 * Common test setup.
 	 */
 	protected void setUp() {
 		this.chart = createBarChart();
 	}
 
 	public void testDefaultChartEditor() {
 
 		DefaultChartEditor editor = new DefaultChartEditor(chart);
 
 		assertNotNull(editor.getTitleEditor());
 		assertEquals("Bar Chart Title", editor.getTitleEditor().getTitleText());
 		assertNotNull(editor.getPlotEditor());
 		assertTrue(editor.getAntiAlias());
 		assertNotNull(editor.getBackgroundPaint());
 
 		Component components[] = editor.getComponents();
 		assertEquals(1, components.length);
 		Component component = components[0];
 
 		assertTrue(component instanceof JPanel);
 		assertTrue(0.5 == component.getAlignmentX());
 		assertTrue(0.5 == component.getAlignmentY());
 
 		assertEquals(-1118482, component.getBackground().getRGB());
 		assertTrue(0.0 == component.getBounds().getHeight());
 		assertTrue(0.0 == component.getBounds().getWidth());
 
 		assertTrue(0.0 == component.getHeight());
 		assertTrue(0.0 == component.getWidth());
 
 		assertNull(component.getName());
 		assertTrue(0 == component.getSize().getHeight());
 		assertTrue(0 == component.getSize().getWidth());
 		assertFalse(ComponentOrientation.LEFT_TO_RIGHT.equals(component
 				.getComponentOrientation()));
 	}
 
 	public void testUpdateChart() {
 
 		DefaultChartEditor editor = new DefaultChartEditor(chart);
 		assertNotNull(editor.getTitleEditor());
 		assertEquals("Bar Chart Title", editor.getTitleEditor().getTitleText());
 
 		editor.updateChart(createBarChart2());
 		assertNotNull(editor.getTitleEditor());
		assertEquals("Bar Chart 2 Title", editor.getTitleEditor()
 				.getTitleText());
		assertFalse(editor.getAntiAlias());
 
 	}
 
 	private static JFreeChart createBarChart() {
 
 		// create a dataset...
 		Number[][] data = new Integer[][] {
 				{ new Integer(-3), new Integer(-2) },
 				{ new Integer(-1), new Integer(1) },
 				{ new Integer(2), new Integer(3) } };
 
 		CategoryDataset dataset = DatasetUtilities.createCategoryDataset("S",
 				"C", data);
 
 		// create the chart...
 		JFreeChart chart = ChartFactory.createBarChart("Bar Chart Title",
 				"Domain", "Range", dataset, PlotOrientation.HORIZONTAL, true, // include
 																				// legend
 				true, true);
 		chart.setAntiAlias(true);
 		return chart;
 	}
 
 	private static JFreeChart createBarChart2() {
 
 		// create a dataset...
 		Number[][] data = new Integer[][] {
 				{ new Integer(-3), new Integer(-2) },
 				{ new Integer(-1), new Integer(1) },
 				{ new Integer(2), new Integer(3) } };
 
 		CategoryDataset dataset = DatasetUtilities.createCategoryDataset("S",
 				"C", data);
 
 		// create the chart...
 		JFreeChart chart = ChartFactory.createBarChart("Bar Chart 2 Title",
 				"Domain", "Range", dataset, PlotOrientation.HORIZONTAL, true, // include
 																				// legend
 				true, true);
 		chart.setAntiAlias(false);
 		return chart;
 	}
 }
