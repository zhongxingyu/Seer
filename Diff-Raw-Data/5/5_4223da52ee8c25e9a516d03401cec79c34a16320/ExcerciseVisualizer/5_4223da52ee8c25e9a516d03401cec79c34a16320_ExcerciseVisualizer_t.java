 package tu.modgeh.intfiresim;
 
 
 import java.awt.Color;
 import java.awt.Shape;
 import java.awt.geom.Path2D;
 import java.util.ArrayList;
 import java.util.List;
 import javax.swing.JFrame;
 import javax.swing.border.EmptyBorder;
 import org.jfree.chart.*;
 import org.jfree.chart.axis.NumberAxis;
 import org.jfree.chart.axis.ValueAxis;
 import org.jfree.chart.plot.XYPlot;
 import org.jfree.chart.renderer.xy.DefaultXYItemRenderer;
 import org.jfree.chart.renderer.xy.XYItemRenderer;
 import org.jfree.data.xy.XYSeries;
 import org.jfree.data.xy.XYSeriesCollection;
 
 public class ExcerciseVisualizer {
 
 	public ExcerciseVisualizer() {
 	}
 
 	public static class CrossShape extends Path2D.Double {
 
 		public CrossShape(double crossRadius) {
 
 			moveTo( crossRadius,  crossRadius);
 			lineTo(-crossRadius, -crossRadius);
 			moveTo(-crossRadius,  crossRadius);
 			lineTo( crossRadius, -crossRadius);
 		}
 		public CrossShape() {
 			this(3.0);
 		}
 	}
 
 	public static class SpikeShape extends Path2D.Double {
 
 		private SpikeShape(double height, double offset) {
 
 			moveTo(0.0, -offset);
 			lineTo(0.0, -offset -height);
 		}
 		public SpikeShape(double height) {
 			this(height, 0.0);
 		}
 		public SpikeShape() {
 			this(100.0);
 		}
 	}
 
 	private static XYItemRenderer createRenderer() {
 
 		DefaultXYItemRenderer renderer = null;
 
 		renderer = new DefaultXYItemRenderer();
 
 		renderer.setDrawSeriesLineAsPath(false);
 		renderer.setBaseItemLabelsVisible(false);
 		renderer.setBaseLinesVisible(true);
 		renderer.setBaseShapesVisible(true);
 		renderer.setBaseShapesFilled(false);
 
 		renderer.setAutoPopulateSeriesFillPaint(false);
 		renderer.setAutoPopulateSeriesOutlinePaint(false);
 		renderer.setAutoPopulateSeriesOutlineStroke(false);
 		renderer.setAutoPopulateSeriesPaint(false);
 		renderer.setAutoPopulateSeriesShape(false);
 		renderer.setAutoPopulateSeriesStroke(false);
 
 		Shape spikeShape = new SpikeShape();
 		Shape defaultShape = renderer.getBaseShape();
 
 		List<Color> colors = new ArrayList<Color>();
 		colors.add(Color.BLUE);
 		colors.add(Color.RED);
 		colors.add(Color.GREEN);
 
 		// spikes series
 		for (int i = 0; i < 100; i++) {
 			int series = i * 2;
 			renderer.setSeriesLinesVisible(series, false);
 			renderer.setSeriesVisible(series, true);
 			renderer.setSeriesShape(series, spikeShape);
 			renderer.setLegendShape(series, defaultShape);
 			renderer.setSeriesPaint(series, colors.get(i % colors.size()));
 			renderer.setSeriesFillPaint(series, colors.get(i % colors.size()));
 			renderer.setSeriesOutlinePaint(series, colors.get(i % colors.size()));
 		}
 
 		// membrane potential series
 		for (int i = 0; i < 100; i++) {
 			int series = i * 2 + 1;
 			renderer.setSeriesShapesVisible(series, false);
 			renderer.setSeriesVisible(series, true);
 			renderer.setSeriesVisibleInLegend(series, false);
 			renderer.setSeriesPaint(series, renderer.getSeriesPaint(series - 1));
 			renderer.setSeriesFillPaint(series, renderer.getSeriesFillPaint(series - 1));
 			renderer.setSeriesOutlinePaint(series, renderer.getSeriesOutlinePaint(series - 1));
 		}
 
 		return renderer;
 	}
 
 	private static ValueAxis createDomainAxis() {
 
 		NumberAxis axis = null;
 
 		axis = new NumberAxis();
 		axis.setLowerBound(-0.05);
 		axis.setUpperBound(1.05);
 
 		return axis;
 	}
 
 	private static ValueAxis createRangeAxis(double maxValue) {
 
 		NumberAxis axis = null;
 
 		axis = new NumberAxis();
 		axis.setAutoRange(true);
 
 		return axis;
 	}
 
 	public static ChartPanel createCanvas(List<Integrator> integrators, double simulationTime) {
 
 		ChartPanel panel = null;
 
 		XYSeriesCollection dataCollection = new XYSeriesCollection();
 
 		double maxValue = 0.0;
 		for (Integrator integrator : integrators) {
 			maxValue = Math.max(maxValue, integrator.getThreasholdPotential());
 			IntegrationTimeSeriesCreator dataGenerator = new IntegrationTimeSeriesCreator(integrator, simulationTime);
 			XYSeries spikes = dataGenerator.getSpikes();
 			spikes.setKey("i: " + integrator.getCurrent() + "A / spike-rate: " + (spikes.getItemCount() / simulationTime));
 			XYSeries membranePotential = dataGenerator.getMembranePotential();
 			dataCollection.addSeries(spikes);
 			dataCollection.addSeries(membranePotential);
 		}
 
 		ValueAxis rangeAxis = createRangeAxis(maxValue);
 		ValueAxis domainAxis = createDomainAxis();
 
 		XYPlot plot = new XYPlot(dataCollection, domainAxis, rangeAxis, createRenderer());
 
 		plot.setDomainCrosshairVisible(true);
 
 		plot.setDomainPannable(true);
 		plot.setRangePannable(true);
 
 		JFreeChart chart = new JFreeChart(plot);
 		panel = new ChartPanel(chart);
 
 		return panel;
 	}
 
     public void run() {
 
 		final JFrame mainFrame = new JFrame("6.a) Spike-rates on different noise-currents");
 		mainFrame.setSize(800, 600);
 
 		List<Integrator> integrators = new ArrayList<Integrator>();
 
 		Integrator integrator_5nA = new Integrator();
 		integrator_5nA.setCurrent(0.5E-9);
 		integrators.add(integrator_5nA);
 
 		Integrator integrator_7nA = new Integrator();
 		integrator_7nA.setCurrent(0.7E-9);
 		integrators.add(integrator_7nA);
 
 		Integrator integrator_9nA = new Integrator();
 		integrator_9nA.setCurrent(0.9E-9);
 		integrators.add(integrator_9nA);
 
 		ChartPanel canvas = createCanvas(integrators, 100.0);
 		canvas.setBorder(new EmptyBorder(10, 10, 10, 10));
 
 		mainFrame.getContentPane().add(canvas);
 
		java.awt.EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
                 mainFrame.setVisible(true);
             }
         });
     }
 }
