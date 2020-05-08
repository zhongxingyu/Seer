 package edu.brown.cs32.atian.crassus.gui;
 
 
 import java.awt.Color;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.imageio.ImageIO;
 
 import org.jfree.chart.ChartFactory;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.plot.PlotOrientation;
 import org.jfree.chart.plot.XYPlot;
 import org.jfree.chart.renderer.xy.XYItemRenderer;
 import org.jfree.data.time.Day;
 import org.jfree.data.time.Minute;
 import org.jfree.data.time.TimeSeries;
 import org.jfree.data.time.TimeSeriesCollection;
 import edu.brown.cs32.atian.crassus.gui.CantTurnRsOnAfterChartsRetreivedException;
 
 
 public class PlotWrapper implements StockPlot 
 {
 	private String stockName;
 	private TimeFrame timeFrame;
 	private TimeSeriesCollection series = new TimeSeriesCollection();
 	private TimeSeriesCollection rsSeries = new TimeSeriesCollection();
 	private List<Color> seriesColors = new ArrayList<Color>();
 	private List<Color> rsSeriesColors = new ArrayList<Color>();
 	private boolean isRsOn = false;
 	private boolean isRsOnSameChart = false;
 	private boolean primaryChartGenerated = false;
 	private boolean rsChartGenerated = false;
 	private String xAxisTitle = "X";
 	private String yAxisTitle = "Y";
 	private JFreeChart primaryChart;
 	private JFreeChart rsChart;
 	
 	public PlotWrapper(String stockName, TimeFrame timeFrame)
 	{ 
 		this.stockName = stockName; 
 		this.timeFrame = timeFrame;
 	}
 	
 	public void setTimeFrame(TimeFrame timeFrame)
 	{
 		this.timeFrame = timeFrame;
 	}
 	
 	public TimeFrame getTimeFrame()
 	{
 		return timeFrame;
 	}
 	
 	//add a series to the list of series
 	public void addSeries(SeriesWrapper series)
 	{
 		this.series.addSeries(series.getSeries());
 		this.seriesColors.add(series.getColor());
 	}
 	
 	public void addRsSeries(SeriesWrapper series)
 	{
 		this.rsSeries.addSeries(series.getSeries());
 		this.rsSeriesColors.add(series.getColor());
 	}
 
 	//set whether the relative strength chart should be separate from the primary chart
 	@Override
 	public void setRsOnSameChart(boolean isRsOnSameChart) 
 	{
 		this.isRsOnSameChart = isRsOnSameChart;
 	}
 
 	@Override
 	public boolean isRsOn() 
 	{
 		return isRsOnSameChart;
 	}
 
 	@Override
 	public BufferedImage getPrimaryBufferedImage(int width, int height) 
 	{
 		
 		if(primaryChartGenerated)
 		{
 			return primaryChart.createBufferedImage(width, height);
 		}
 		else
 		{
 			primaryChartGenerated = true;
 		}
 		
 		JFreeChart chart;
 		
 		if(isRsOnSameChart)
 		{
 		
 			//fill colors list with all the colors from the stored lists
 			List<Color> chartColors = new ArrayList<Color>(seriesColors);
 			chartColors.addAll(rsSeriesColors);
 			
 			TimeSeriesCollection chartSeries = new TimeSeriesCollection();
 			
 			//fill chartSeries with all neccessary series
 			for(int i = 0; i < series.getSeriesCount(); i++)
 			{
 				chartSeries.addSeries(series.getSeries(i));
 			}
 			
 			for(int i = 0; i < rsSeries.getSeriesCount(); i++)
 			{
 				chartSeries.addSeries(rsSeries.getSeries(i));
 			}
 			
 			chart = generateChart(stockName, chartSeries, chartColors);
 		}
 		else
 		{
 			chart = generateChart(stockName, series, seriesColors);
 		}
 		
 		primaryChart = chart;
 		
 		return chart.createBufferedImage(width, height);
 		
 	}
 
 	@Override
 	public BufferedImage getRsBufferedImage(int width, int height) 
 	{
 		if(!isRsOn || isRsOnSameChart)
 		{
 			throw new Error("This method can only be called if RSI is not on the primary and RSI is set to on.");
 		}
 
 		if(rsChartGenerated)
 		{
 			return rsChart.createBufferedImage(width, height);
 		}
 		else
 		{
 			rsChartGenerated = true;
 		}
 		
 		JFreeChart chart = generateChart("Relative Strength Index", rsSeries, rsSeriesColors);
 		
 		rsChart = chart;
 		
 		return chart.createBufferedImage(width, height);
 	}
 
 	@Override
 	public void setRS(boolean isRsOn) throws CantTurnRsOnAfterChartsRetreivedException
 	{
 		if(!primaryChartGenerated)
 		{
 			this.isRsOn = isRsOn;
 		}
 		else
 		{
 			throw new CantTurnRsOnAfterChartsRetreivedException();
 		}
 	}
 	
 	private JFreeChart generateChart(String title, TimeSeriesCollection series, List<Color> colors)
 	{
 		JFreeChart chart = ChartFactory.createTimeSeriesChart(title, xAxisTitle, yAxisTitle, series, false, false, false);
 		XYPlot plot  = (XYPlot)chart.getPlot();
 		XYItemRenderer ren = plot.getRenderer();
 		
 		for(int i = 0; i < colors.size(); i++)
 		{
 			ren.setSeriesPaint(i, colors.get(i));
 		}
 		
 		return chart;
 	}
 	
 	public void setAxesTitles(String xAxisTitle, String yAxisTitle)
 	{
 		this.xAxisTitle = xAxisTitle;
 		this.yAxisTitle = yAxisTitle;
 	}
 	
 	
 	public static void main(String[] args)
 	{
		PlotWrapper pw = new PlotWrapper("Mike");
 		
 		TimeSeries series1 = new TimeSeries(Math.random());
 		TimeSeries series2 = new TimeSeries(Math.random());
 		
 		for(int i = 0; i < 100; i++)
 		{
 			series1.addOrUpdate(new Day((int)(Math.random()*20+1), (int)(Math.random()*11 + 1),2013), Math.random()*1000.0);
 		}
 		
 		for(int i = 0; i < 100; i++)
 		{
 			series2.addOrUpdate(new Day((int)(Math.random()*20+1), (int)(Math.random()*11 + 1),2013), Math.random()*1000.0);
 		}
 		
 		SeriesWrapper s1 = new SeriesWrapper(series1, Color.RED);
 		SeriesWrapper s2 = new SeriesWrapper(series2, Color.BLUE);
 		
 		pw.addSeries(s1);
 		pw.addSeries(s2);
 		
 		Long start = System.currentTimeMillis();
 		BufferedImage img = pw.getPrimaryBufferedImage(1000, 1000);
 		Long end = System.currentTimeMillis();
 		
 		System.out.println(end - start);
 		
 		start = System.currentTimeMillis();
 		img = pw.getPrimaryBufferedImage(1000, 1000);
 	    end = System.currentTimeMillis();
 	    
 	    System.out.println(end - start);
 		
 	    File outputfile = new File("chart2" + ".png");
 	    try
 	    {	
 
 		ImageIO.write(img, "png", outputfile);
 	    
 	    }
 	    catch(IOException e)
 	    {
 		   
 	    }
 	}
 
 	
 	
 	
 
 }
