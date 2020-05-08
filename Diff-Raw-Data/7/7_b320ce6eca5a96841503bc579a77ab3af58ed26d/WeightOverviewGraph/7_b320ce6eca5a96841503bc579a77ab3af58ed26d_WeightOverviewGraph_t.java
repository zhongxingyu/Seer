 package at.linuxhacker.weightassistant;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 import org.achartengine.ChartFactory;
 import org.achartengine.chart.PointStyle;
 import org.achartengine.model.TimeSeries;
 import org.achartengine.model.XYMultipleSeriesDataset;
 import org.achartengine.renderer.XYMultipleSeriesRenderer;
 import org.achartengine.renderer.XYSeriesRenderer;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Color;
 import android.preference.PreferenceManager;
 
 public class WeightOverviewGraph {
 	private XYMultipleSeriesRenderer renderer;
 	private XYMultipleSeriesDataset dataset;
 	private WeightMeasurmentSeries weightMeasurmentSeries;
 	private WeightMeasurmentSerieStatistics weightMeasurmentSerieStatistics;
 	private List<Double> series2;
 	private List<Double> series3;
 	private int secondGraphOrder = 0;
 	private int thirdGraphOrder = 0;
 	private int minimumMeasuringPoints = 0;
 	private boolean displayOnlyFirstGraph = false;
 
 	public Intent execute(Context context) {
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences( context );
		this.minimumMeasuringPoints  = Integer.parseInt( prefs.getString( "prefMinimumMeasuringPointsForGraph", "15" ) );
 		if ( this.weightMeasurmentSeries.measurmentSeries.size( ) > this.minimumMeasuringPoints ) {
			this.secondGraphOrder = Integer.parseInt( prefs.getString( "prefSecondGraphOrder", "7") );
			this.thirdGraphOrder = Integer.parseInt( prefs.getString( "prefThirdGraphOrder", "14" ) );
 			this.displayOnlyFirstGraph = false;
 		} else {
 			this.secondGraphOrder = 1;
 			this.thirdGraphOrder = 1;
 			this.displayOnlyFirstGraph = true;
 		}
 		
 		this.dataset = new XYMultipleSeriesDataset();
 		this.renderer = new XYMultipleSeriesRenderer( );
 
 		//this.series2 = this.calcAverage( this.secondGraphOrder );
 		//this.series3 = this.calcAverage( this.thirdGraphOrder );
 		this.weightMeasurmentSerieStatistics = new WeightMeasurmentSerieStatistics( 
 				this.weightMeasurmentSeries.measurmentSeries,
 				new int[] { this.secondGraphOrder, this.thirdGraphOrder }  );
 		this.weightMeasurmentSerieStatistics.calcAverageSeries( );
 		
 		setChartSettings( );
 		setSeries( );
 		
 		return ChartFactory.getTimeChartIntent(context, this.dataset,
 				this.renderer, "yyyy-MM-dd");
 	}
 	
 	public void setWeightMeasurmentSeries( WeightMeasurmentSeries weightMeasurmentSeries ) {
 		this.weightMeasurmentSeries = weightMeasurmentSeries;
 	}
 	
 	public void setWeightMeasurmentSerieStatistics( WeightMeasurmentSerieStatistics weightMeasurmentSerieStatistics ) {
 		this.weightMeasurmentSerieStatistics = weightMeasurmentSerieStatistics;
 	}
 	
 	protected void setChartSettings( ) {
 		this.renderer.setChartTitle( "Mein Gewicht" );
 		this.renderer.setXTitle( "Datum" );
 		this.renderer.setYTitle( "Gewicht in kg" );
 		this.renderer.setAxesColor( Color.GRAY );
 		this.renderer.setLabelsColor( Color.LTGRAY );
 		this.renderer.setYLabels(10);
 		this.renderer.setAxisTitleTextSize( 24 );
 		this.renderer.setChartTitleTextSize( 28 );
 		this.renderer.setLabelsTextSize( 23 );
 		this.renderer.setLegendTextSize( 23 );
 		this.renderer.setPointSize(5f);
 		// top, left, button, right
 		this.renderer.setMargins(new int[] { 20, 40, 40, 30 });
 		this.renderer.setZoomButtonsVisible(true);
 	}
 
 	protected void setSeries( ) {
 		TimeSeries timeSeries1 = ( TimeSeries ) new TimeSeries( "Gewicht" );
 		TimeSeries timeSeries2 = ( TimeSeries ) new TimeSeries( this.secondGraphOrder + ". Ordnung" );
 		TimeSeries timeSeries3 = ( TimeSeries ) new TimeSeries( this.thirdGraphOrder + ". Ordnung" );
 
 		int seriesLength = this.weightMeasurmentSerieStatistics.sizeOfStatisticSeries( );
 		for (int i= 0; i < seriesLength; i++ ) {
 			MeasuringPoint point = this.weightMeasurmentSerieStatistics.getMeasuringPoint( i );
 			timeSeries1.add( point.getDate( ), point.getWeight( ) );
 			timeSeries2.add( point.getDate( ), this.weightMeasurmentSerieStatistics.getAverageFor( i, 0 ) );
 			timeSeries3.add( point.getDate( ), this.weightMeasurmentSerieStatistics.getAverageFor( i, 1 ) );
 		}
 		
 		this.dataset.addSeries( timeSeries1 );
 		XYSeriesRenderer r = new XYSeriesRenderer( );
 		r.setColor( Color.CYAN );
 		r.setPointStyle( PointStyle.TRIANGLE );
 		this.renderer.addSeriesRenderer( r );
 
 		this.dataset.addSeries( timeSeries2 );
 		r = new XYSeriesRenderer( );
 		r.setColor( Color.RED );
 		r.setPointStyle( PointStyle.DIAMOND );
 		this.renderer.addSeriesRenderer( r );		
 
 		this.dataset.addSeries( timeSeries3 );
 		r = new XYSeriesRenderer( );
 		r.setColor( Color.YELLOW );
 		r.setPointStyle( PointStyle.TRIANGLE );
 		r.setLineWidth( 5 );
 		this.renderer.addSeriesRenderer( r );		
 		
 	}
 	protected void setSeriesOld( ) {
 		TimeSeries timeSeries1 = ( TimeSeries ) new TimeSeries( "Gewicht" );
 		TimeSeries timeSeries2 = ( TimeSeries ) new TimeSeries( this.secondGraphOrder + ". Ordnung" );
 		TimeSeries timeSeries3 = ( TimeSeries ) new TimeSeries( this.thirdGraphOrder + ". Ordnung" );
 		
 		int length2 = this.series2.size( );
 		int length3 = this.series3.size( );
 		int maxLength = this.weightMeasurmentSeries.measurmentSeries.size( );
 		for ( int i = 0; i < maxLength; i++ ) {
 			int pos2 = length2 - maxLength + i;
 			int pos3 = length3 - maxLength + i;
 			if ( pos3 < 0 || pos2 < 0 ) {
 				continue;
 			}
 			Date time = this.weightMeasurmentSeries.measurmentSeries.get( i ).getDate( );
 			timeSeries1.add( time, this.weightMeasurmentSeries.measurmentSeries.get( i ).getWeight( ) );
 			timeSeries2.add( time, this.series2.get( pos2 ) );
 			timeSeries3.add( time, this.series3.get( pos3 ) );
 		}
 		
 		this.dataset.addSeries( timeSeries1 );
 		XYSeriesRenderer r = new XYSeriesRenderer( );
 		r.setColor( Color.CYAN );
 		r.setPointStyle( PointStyle.TRIANGLE );
 		this.renderer.addSeriesRenderer( r );
 		
 		if ( ! this.displayOnlyFirstGraph ) {
 			this.dataset.addSeries( timeSeries2 );
 			r = new XYSeriesRenderer( );
 			r.setColor( Color.RED );
 			r.setPointStyle( PointStyle.DIAMOND );
 			this.renderer.addSeriesRenderer( r );		
 
 			this.dataset.addSeries( timeSeries3 );
 			r = new XYSeriesRenderer( );
 			r.setColor( Color.YELLOW );
 			r.setPointStyle( PointStyle.TRIANGLE );
 			r.setLineWidth( 5 );
 			this.renderer.addSeriesRenderer( r );
 		}
 	}
 	
 	/*
 	protected List<Double> calcAverage( int order ) {
 		List<Double> series = new ArrayList<Double>( );
 		if ( this.weightMeasurmentSeries.measurmentSeries.size() < order ) {
 			return series;
 		}
 		
 		for ( int i = this.weightMeasurmentSeries.measurmentSeries.size( ) -1; i >= order -1; i-- ) {
 			double average = this.weightMeasurmentSeries.measurmentSeries.get( i ).getWeight( );
 			for ( int j = 1; j < order; j++ ) {
 				average += this.weightMeasurmentSeries.measurmentSeries.get( i - j ).getWeight( );
 			}
 			average = average / order;
 			series.add( average );
 		}
 		Collections.reverse( series );
 		return series;
 	}
 	*/
 }
