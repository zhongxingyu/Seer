 package net.bubbaland.trivia.client;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.GridBagConstraints;
 import java.awt.Shape;
 import java.awt.geom.Ellipse2D;
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import net.bubbaland.trivia.ScoreEntry;
 import net.bubbaland.trivia.Trivia;
 
 import org.jfree.chart.ChartFactory;
 import org.jfree.chart.ChartPanel;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.axis.NumberAxis;
 import org.jfree.chart.axis.NumberTickUnit;
 import org.jfree.chart.labels.StandardXYToolTipGenerator;
 import org.jfree.chart.plot.PlotOrientation;
 import org.jfree.chart.plot.XYPlot;
 import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
 import org.jfree.data.xy.DefaultTableXYDataset;
 import org.jfree.data.xy.XYSeries;
 
 /**
  * A panel with a chart comparing all of the teams' scores.
  * 
  * @author Walter Kolczynski
  * 
  */
 
 public class TeamComparisonPanel extends TriviaPanel {
 
 	private static final long		serialVersionUID	= 0xaa5a61f8f351d0b3L;
 	private static final Color		BACKGROUND_COLOR	= Color.BLACK;
 	private static final float		AXIS_FONT_SIZE		= 16f;
 	private TriviaClient			client;
 	private int						nRounds;
 	private int						lastAnnounced;
 	private ChartPanel				chartPanel;
 	private ArrayList<ScoreEntry[]>	scores;
 
 	public static Shape makeCircle(double radius) {
 		return new Ellipse2D.Double(-radius, -radius, 2 * radius, 2 * radius);
 	}
 
 	public TeamComparisonPanel(TriviaClient client) {
 		super();
 		this.client = client;
 		this.scores = new ArrayList<ScoreEntry[]>(0);
 		this.lastAnnounced = 0;
 		int tryNumber = 0;
 		boolean success;
 		for (success = false; tryNumber < 10 && !success;) {
 			tryNumber++;
 			try {
 				this.nRounds = client.getTrivia().getNRounds();
 				success = true;
 			} catch (final Exception e) {
 				client.log(( new StringBuilder("Couldn't retrieve number of rounds from server (try #") )
 						.append(tryNumber).append(").").toString());
 			}
 		}
 
 		if (!success) {
 			client.disconnected();
 			return;
 		} else {
 			this.chartPanel = null;
 			return;
 		}
 	}
 
 	@Override
 	public synchronized void update(boolean force) {
 		final Trivia trivia = this.client.getTrivia();
 		boolean change;
 		for (change = false; trivia.isAnnounced(this.lastAnnounced + 1); change = true) {
 			this.lastAnnounced++;
 			final ScoreEntry roundStandings[] = trivia.getStandings(this.lastAnnounced);
 			Arrays.sort(roundStandings);
 			this.scores.add(roundStandings);
 		}
 
		if (change) {
 			final int nTeams = this.scores.get(0).length;
 			final DefaultTableXYDataset dataset = new DefaultTableXYDataset();
 			final JFreeChart chart = ChartFactory.createXYLineChart("Team Comparison", "Round", "Point Differential",
 					dataset, PlotOrientation.VERTICAL, false, true, false);
 			final XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) chart.getXYPlot().getRenderer();
 			for (int t = 0; t < nTeams; t++) {
 				final String team = this.scores.get(0)[t].getTeamName();
 				final XYSeries series = new XYSeries(team, true, false);
 				for (int r = 0; r < this.lastAnnounced; r++) {
 					final int ourScore = trivia.getAnnouncedPoints(r + 1);
 					series.add(r + 1, this.scores.get(r)[t].getScore() - ourScore);
 				}
 
 				renderer.setSeriesShapesVisible(t, true);
 				renderer.setSeriesShape(t, makeCircle(2D));
 				if (team.equals(trivia.getTeamName())) {
 					renderer.setSeriesStroke(t, new BasicStroke(3F));
 					renderer.setSeriesPaint(t, Color.WHITE);
 				}
 				dataset.addSeries(series);
 			}
 
 			renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator("{0} Rd {1}: {2}", NumberFormat
 					.getIntegerInstance(), NumberFormat.getIntegerInstance()));
 			final XYPlot plot = chart.getXYPlot();
 			plot.setBackgroundPaint(BACKGROUND_COLOR);
 			final NumberAxis xAxis = (NumberAxis) plot.getDomainAxis();
 			final NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
 			xAxis.setRange(0.5D, this.nRounds + 0.5D);
 			xAxis.setAutoRange(false);
 			xAxis.setTickUnit(new NumberTickUnit(5D));
 			xAxis.setNumberFormatOverride(NumberFormat.getIntegerInstance());
 			xAxis.setLabelFont(xAxis.getLabelFont().deriveFont(AXIS_FONT_SIZE));
 			xAxis.setTickLabelFont(xAxis.getTickLabelFont().deriveFont(AXIS_FONT_SIZE));
 			final DecimalFormat format = new DecimalFormat();
 			format.setPositivePrefix("+");
 			yAxis.setNumberFormatOverride(format);
 			yAxis.setLabelFont(yAxis.getLabelFont().deriveFont(AXIS_FONT_SIZE));
 			yAxis.setTickLabelFont(yAxis.getTickLabelFont().deriveFont(AXIS_FONT_SIZE));
 			if (this.chartPanel != null) {
 				this.remove(this.chartPanel);
 			}
 			this.chartPanel = new ChartPanel(chart);
 			final GridBagConstraints solo = new GridBagConstraints();
 			solo.fill = 1;
 			solo.anchor = 10;
 			solo.weightx = 1.0D;
 			solo.weighty = 1.0D;
 			solo.gridx = 0;
 			solo.gridy = 0;
 			this.add(this.chartPanel, solo);
 		}
 	}
 
 }
