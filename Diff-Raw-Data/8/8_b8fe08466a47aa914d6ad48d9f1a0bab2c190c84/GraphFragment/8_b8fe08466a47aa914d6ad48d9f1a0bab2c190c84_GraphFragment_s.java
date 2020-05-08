 package com.freescale.iastate.hvac.energy;
 
 /**
  * @author Namara
  *
  */
 
 import java.util.Calendar;
 import java.util.Date;
 
 import org.achartengine.ChartFactory;
 import org.achartengine.GraphicalView;
 import org.achartengine.chart.BarChart.Type;
 import org.achartengine.model.XYMultipleSeriesDataset;
 import org.achartengine.model.XYSeries;
 import org.achartengine.renderer.XYMultipleSeriesRenderer;
 import org.achartengine.renderer.XYSeriesRenderer;
 
 import com.freescale.iastate.hvac.R;
 
 import android.app.Fragment;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup.LayoutParams;
 import android.view.ViewGroup;
 import android.widget.LinearLayout;
 import android.widget.Toast;
 
 public class GraphFragment extends Fragment {
 	final int DAY = 1;
 	final int WEEK = 2;
 	final int MONTH = 3;
 	final int YEAR = 4;
 
 	final int MAXKWH = 100;
 
 	LinearLayout graphLayout;
 	LayoutInflater localInflater;
 	ViewGroup localContainer;
 	View view;
 	private int timePeriod;
 	private int barColor;
 	private int axesColor;
 	private int fontColor;
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		localInflater = inflater;
 		localContainer = container;
 		view = inflater.inflate(R.layout.graphs_fragment, container, false);
 
 		timePeriod = WEEK;
 		barColor = Color.WHITE;
 		axesColor = Color.WHITE;
 		fontColor = Color.WHITE;
 
 		GraphicalView graph = ChartFactory.getBarChartView(getActivity(),
 				getGeneratedDataset(timePeriod), getChartRenderer(timePeriod),
 				Type.DEFAULT);
 		graphLayout = (LinearLayout) view
 				.findViewById(R.id.graph_fragment_layout);
 
 		graphLayout.addView(graph, new LayoutParams(LayoutParams.MATCH_PARENT,
 				LayoutParams.MATCH_PARENT));
 
 		return view;
 	}
 
 	/*
 	 * Generates an example data set Currently from 65+/- 17
 	 */
 	private XYMultipleSeriesDataset getGeneratedDataset(int timePeriod) {
 		final int floor = 65;
 		int averageKWH = 0;
		int variation = 0;
 
 		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
 		Calendar c = Calendar.getInstance();
 		int numDataPoints = 0;
 
 		if (timePeriod == DAY) {
 			averageKWH = hourKWH();
			variation = 1;
 			c = Calendar.getInstance();
 			numDataPoints = c.get(Calendar.HOUR_OF_DAY) + 1;
 		} else if (timePeriod == WEEK) {
 			averageKWH = dayKWH();
 			variation = 10;
 			c = Calendar.getInstance();
 			numDataPoints = c.get(Calendar.DAY_OF_WEEK) + 1;
 		} else if (timePeriod == MONTH) {
 			averageKWH = dayKWH();
 			variation = 10;
 			c = Calendar.getInstance();
 			numDataPoints = c.get(Calendar.DATE) + 1;
 		} else if (timePeriod == YEAR) {
 			variation = 15;
 			averageKWH = monthKWH();
 			numDataPoints = 12;
 		}
 
 
 		final String seriesTitle = "Number of kilowatt hours";
 		XYSeries series = new XYSeries(seriesTitle);
 		for (int k = 1; k < numDataPoints; k++) {
 			Double rando = Math.random() * variation;
 			int kWattHr = (int) (Math.floor(rando) + averageKWH);
 			series.add(k, kWattHr);
 		}
 		dataset.addSeries(series);
 
 		return dataset;
 	}
 
 	private XYMultipleSeriesRenderer getChartRenderer(int timePeriod) {
 		ChartSettings s = new ChartSettings();
 		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
 
 		Calendar c = Calendar.getInstance();
 		String chartTitle = "Demo Chart";
 		String xTitle = "";
 		double numBars = 0;
 		double xMin = 0.5;
 		double yMax = 0;
 
 		if (timePeriod == DAY) {
 			chartTitle = "Energy Usage Today";
 			xTitle = "Hours";
 			numBars = 24.5;
			yMax = hourKWH() +  hourKWH();
 
 			renderer.clearXTextLabels();
 
 			String[] hours = { "", "3 AM", "6 AM", "9 AM", "12 PM", "3 PM",
 					"6 PM", "9 PM", "12 AM" };
 
 			for (int i = 0; i < 9; i++) {
 				renderer.addXTextLabel((double) i * 3, hours[i]);
 			}
 			renderer.setXLabels(0);
 		} else if (timePeriod == WEEK) {
 			chartTitle = "Energy Usage This Week";
 			xTitle = "Days";
 			numBars = 7.5;
 			yMax = dayKWH() +  dayKWH()*.4;
 			renderer.clearXTextLabels();
 
 			String[] days = { "Monday", "Tuesday", "Wednesday", "Thursday",
 					"Friday", "Saturday", "Sunday" };
 
 			for (int i = 0; i < 7; i++) {
 				double d = i + 1;
 				renderer.addXTextLabel(d, days[i]);
 			}
 			renderer.setXLabels(0);
 		} else if (timePeriod == MONTH) {
 			chartTitle = "Energy Usage This Month";
 			xTitle = "Days";
 			numBars = c.getActualMaximum(Calendar.DAY_OF_MONTH) + 1;
 			yMax = dayKWH() +  dayKWH()*.4;
 			renderer.setXLabels((int) numBars);
 		} else if (timePeriod == YEAR) {
 			chartTitle = "Energy Usage This Year";
 			xTitle = "Months";
 			yMax = monthKWH() +  monthKWH()*.4;
 			numBars = 12.5;
 
 			renderer.clearXTextLabels();
 
 			String[] days = { "Jan", "Feb", "March", "April", "May", "June",
 					"July", "Aug", "Sept", "Oct", "Nov", "Dec" };
 
 			for (int i = 0; i < 12; i++) {
 				double d = i + 1;
 				renderer.addXTextLabel(d, days[i]);
 			}
 			renderer.setXLabels(0);
 		}
 
 		s.setChartSettings(renderer, chartTitle, xTitle, "kWh", xMin, numBars,
 				0, yMax, axesColor, fontColor);
 		s.setTextSize(renderer, 20, 40, 20);
 		s.setAutoSpacing(renderer);
 
 		XYSeriesRenderer r = new XYSeriesRenderer();
 		r.setColor(barColor);
 		renderer.setYLabelsAngle(-90);
 		renderer.setShowLabels(true);
 		renderer.setShowLegend(false);
 		renderer.addSeriesRenderer(r);
 		return renderer;
 
 	}
 	
 	private int monthKWH(){
 		int avgKWHperYear = 14000;
 		int monthKWH = avgKWHperYear/12;
 		return monthKWH;
 	}
 	
 	private int weekKWH(){
 		int avgKWHperYear = 14000;
 		int weekKWH = avgKWHperYear/52;
 		return weekKWH;
 	}
 	
 	private int dayKWH(){
 		int avgKWHperYear = 14000;
 		int dateKWH = avgKWHperYear/365;
 		return dateKWH;
 		
 	}
 	
 	private int hourKWH(){
 		int avgKWHperYear = 14000;
 		int avgKWHperDay = avgKWHperYear/365;
 		int hourKWH = avgKWHperDay/24;
 		return hourKWH;
 	}
 
 	/**
 	 * @param buttonID
 	 */
 	public void updateGraph(int buttonID) {
 
 		if (buttonID == DAY) {
 			timePeriod = DAY;
 		} else if (buttonID == WEEK) {
 			timePeriod = WEEK;
 		} else if (buttonID == MONTH) {
 			timePeriod = MONTH;
 		} else if (buttonID == YEAR) {
 			timePeriod = YEAR;
 		}
 
 		((ViewGroup) view).removeAllViews();
 		redrawGraph();
 	}
 
 	public void redrawGraph() {
 		GraphicalView graph = ChartFactory.getBarChartView(getActivity(),
 				getGeneratedDataset(timePeriod), getChartRenderer(timePeriod),
 				Type.DEFAULT);
 
 		graphLayout = (LinearLayout) view
 				.findViewById(R.id.graph_fragment_layout);
 
 		((ViewGroup) view).addView(graph, new LinearLayout.LayoutParams(
 				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
 
 	}
 }
