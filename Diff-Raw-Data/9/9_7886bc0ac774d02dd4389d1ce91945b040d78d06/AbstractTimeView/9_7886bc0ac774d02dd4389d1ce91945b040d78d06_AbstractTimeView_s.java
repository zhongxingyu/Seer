 package ch.cern.atlas.apvs.client.ui;
 
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.moxieapps.gwt.highcharts.client.Axis;
 import org.moxieapps.gwt.highcharts.client.AxisTitle;
 import org.moxieapps.gwt.highcharts.client.Chart;
 import org.moxieapps.gwt.highcharts.client.ChartTitle;
 import org.moxieapps.gwt.highcharts.client.Credits;
 import org.moxieapps.gwt.highcharts.client.Exporting;
 import org.moxieapps.gwt.highcharts.client.Legend;
 import org.moxieapps.gwt.highcharts.client.Series;
 import org.moxieapps.gwt.highcharts.client.Style;
 import org.moxieapps.gwt.highcharts.client.ToolTip;
 import org.moxieapps.gwt.highcharts.client.ToolTipData;
 import org.moxieapps.gwt.highcharts.client.ToolTipFormatter;
 import org.moxieapps.gwt.highcharts.client.labels.AxisLabelsData;
 import org.moxieapps.gwt.highcharts.client.labels.AxisLabelsFormatter;
 import org.moxieapps.gwt.highcharts.client.labels.DataLabels;
 import org.moxieapps.gwt.highcharts.client.labels.XAxisLabels;
 import org.moxieapps.gwt.highcharts.client.plotOptions.BarPlotOptions;
 import org.moxieapps.gwt.highcharts.client.plotOptions.LinePlotOptions;
 import org.moxieapps.gwt.highcharts.client.plotOptions.Marker;
 
 import ch.cern.atlas.apvs.client.widget.GlassPanel;
 import ch.cern.atlas.apvs.ptu.shared.PtuClientConstants;
 
 import com.google.gwt.i18n.client.NumberFormat;
 
 public class AbstractTimeView extends GlassPanel {
 
 	private static final int POINT_LIMIT = 200;
	private static final String[] color = { "#4572A7", "#AA4643", "#89A54E",
			"#80699B", "#3D96AE", "#DB843D", "#92A8CD", "#A47D7C", "#B5CA92" };
 	private Map<String, Integer> pointsById;
 	private Map<String, Series> seriesById;
 	private Map<String, String> colorsById;
 
 	protected Chart chart;
 	protected Integer height = null;
 	protected boolean export = true;
 	protected boolean title = true;
 
 	public AbstractTimeView() {
 		super();
 		pointsById = new HashMap<String, Integer>();
 		seriesById = new HashMap<String, Series>();
 		colorsById = new HashMap<String, String>();
 	}
 
 	public Map<String, String> getColors() {
 		return colorsById;
 	}
 
 	protected void removeChart() {
 		if (chart != null) {
 			remove(chart);
 			chart = null;
 			pointsById.clear();
 			seriesById.clear();
 			colorsById.clear();
 		}
 	}
 
 	protected void createChart(String name) {
 		removeChart();
 
 		chart = new Chart()
 				// same as above
 				// FIXME String.format not supported
 				.setColors(
 						// String.format("%s, ", (Object[])color))
						"#4572A7", "#AA4643", "#89A54E", "#80699B", "#3D96AE",
						"#DB843D", "#92A8CD", "#A47D7C", "#B5CA92")
 				.setType(Series.Type.LINE)
 				.setZoomType(Chart.ZoomType.X)
 				.setSizeToMatchContainer()
 //				.setWidth100()
 				.setChartTitle(
 						title ? new ChartTitle().setText(name).setStyle(
 								new Style().setFontSize("12px")) : null)
 				.setMarginRight(10)
 				.setExporting(new Exporting().setEnabled(export))
 				.setBarPlotOptions(
 						new BarPlotOptions().setDataLabels(new DataLabels()
 								.setEnabled(true)))
 				.setLinePlotOptions(
 						new LinePlotOptions().setMarker(
 								new Marker().setEnabled(false))
 								.setShadow(false))
 				.setAnimation(false)
 				.setLegend(new Legend().setEnabled(false))
 				.setCredits(new Credits().setEnabled(false))
 				.setToolTip(
 						new ToolTip().setCrosshairs(true, true).setFormatter(
 								new ToolTipFormatter() {
 									@Override
 									public String format(ToolTipData toolTipData) {
 										return "<b>"
 												+ toolTipData.getSeriesName()
 												+ "</b><br/>"
 												+ getDateTime(toolTipData
 														.getXAsLong())
 												+ "<br/>"
 												+ NumberFormat
 														.getFormat("0.00")
 														.format(toolTipData
 																.getYAsDouble());
 									}
 								}));
 		
 		if (height != null) {
 			chart.setHeight(height);
 		}
 
 		chart.getXAxis().setType(Axis.Type.DATE_TIME).setLabels(
 		// Fix one hour offset in time labels...
 				new XAxisLabels().setFormatter(new AxisLabelsFormatter() {
 
 					@Override
 					public String format(AxisLabelsData axisLabelsData) {
 						return getDateTime(axisLabelsData.getValueAsLong());
 					}
 				}));
 		chart.getXAxis().setAxisTitle(new AxisTitle().setText("Time"));
 
 		chart.getYAxis().setAllowDecimals(true);
 
 		chart.getYAxis().setAxisTitle(new AxisTitle().setText(""));
 	}
 
 	protected void addSeries(String ptuId, String name) {
 		Series series = chart.createSeries().setName(name);
 		pointsById.put(ptuId, 0);
 		seriesById.put(ptuId, series);
 		colorsById.put(ptuId, color[chart.getSeries().length]);
 
 		chart.addSeries(series, true, false);
 	}
 
 	protected void addData(String ptuId, Number[][] data) {
 		Series series = seriesById.get(ptuId);
 		series.setPoints(data != null ? data : new Number[0][2], false);
 		pointsById.put(ptuId, data != null ? data.length : 0);
 	}
 
 	protected void addPoint(String ptuId, long time, Number value) {
 		Series series = seriesById.get(ptuId);
 		if (series == null)
 			return;
 		Integer numberOfPoints = pointsById.get(ptuId);
 		if (numberOfPoints == null)
 			numberOfPoints = 0;
 		boolean shift = numberOfPoints >= POINT_LIMIT;
 		if (!shift) {
 			pointsById.put(ptuId, numberOfPoints + 1);
 		}
 		chart.setLinePlotOptions(new LinePlotOptions().setMarker(new Marker()
 				.setEnabled(!shift)));
 		series.addPoint(time, value, true, shift, true);
 	}
 
 	private String getDateTime(long time) {
 		long now = new Date().getTime();
 		long nextMinute = now + (60 * 1000);
 		long yesterday = now - (24 * 60 * 1000);
 		Date date = new Date(time);
 		if (time > nextMinute) {
 			return "<b>" + PtuClientConstants.timeFormat.format(date) +"<br/>"+PtuClientConstants.dateFormatOnly.format(date)
 					+ "</b>";
 		} else if (time < yesterday) {
 			return "<i>" + PtuClientConstants.timeFormat.format(date) +"<br/>"+PtuClientConstants.dateFormatOnly.format(date)
 					+ "</i>";
 		}
 		return PtuClientConstants.timeFormat.format(date);
 	}
 
 	protected void setUnit(String unit) {
 		if (chart == null) {
 			return;
 		}
 
 		// fix #96 to put unicode in place of &deg; and &micro;
 		unit = unit.replaceAll("\\&deg\\;", "\u00B0");
 		unit = unit.replaceAll("\\&micro\\;", "\u00B5");
 
 		chart.getYAxis().setAxisTitle(new AxisTitle().setText(unit));
 	}
 
 	public void redraw() {
 		if (chart != null) {
 			chart.redraw();
 		}
 	}
 
 }
