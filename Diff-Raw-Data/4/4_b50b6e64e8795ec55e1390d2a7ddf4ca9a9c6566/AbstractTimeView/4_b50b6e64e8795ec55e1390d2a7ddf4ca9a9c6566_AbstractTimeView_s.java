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
 
 import ch.cern.atlas.apvs.client.ClientFactory;
 
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.i18n.client.NumberFormat;
 import com.google.gwt.user.client.ui.SimplePanel;
 
 public class AbstractTimeView extends SimplePanel {
 
 	protected static final String[] color = { "#4572A7", "#AA4643", "#89A54E",
 			"#80699B", "#3D96AE", "#DB843D", "#92A8CD", "#A47D7C", "#B5CA92" };
 	protected static final int pointLimit = 200;
 	protected ClientFactory clientFactory;
 	protected Chart chart;
 	protected Map<String, Integer> pointsById;
 	protected Map<String, Series> seriesById;
 	protected Map<String, String> colorsById;
 	protected int height = 300;
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
 				.setWidth100()
 				.setHeight(height)
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
 												+ DateTimeFormat
 														.getFormat(
 																"yyyy-MM-dd HH:mm:ss")
 														.format(new Date(
 																toolTipData
 																		.getXAsLong()))
 												+ "<br/>"
 												+ NumberFormat
 														.getFormat("0.00")
 														.format(toolTipData
 																.getYAsDouble());
 									}
 								}));
 
 		chart.getXAxis().setType(Axis.Type.DATE_TIME).setLabels(
 		// Fix one hour offset in time labels...
 				new XAxisLabels().setFormatter(new AxisLabelsFormatter() {
 
 					@Override
 					public String format(AxisLabelsData axisLabelsData) {
 						Date date = new Date(axisLabelsData.getValueAsLong());
 						@SuppressWarnings("deprecation")
 						String pattern = date.getSeconds() == 0 ? "HH:mm"
 								: "HH:mm:ss";
 						return DateTimeFormat.getFormat(pattern).format(date);
 					}
 				}));
 		chart.getXAxis().setAxisTitle(new AxisTitle().setText("Time"));
 
 		chart.getYAxis().setAllowDecimals(true);
 		
 		chart.getYAxis().setAxisTitle(new AxisTitle().setText(""));
 	}
 
 	protected void setUnit(String unit) {
 		if (chart == null) {
 			return;
 		}
 		
		// fix #96 to put unicode in place of &deg;
 		unit = unit.replaceAll("\\&deg\\;", "\u00B0");
 
 		chart.getYAxis().setAxisTitle(new AxisTitle().setText(unit));
 	}
 
 	public void redraw() {
 		if (chart != null) {
 			chart.redraw();
 		}
 	}
 
 }
