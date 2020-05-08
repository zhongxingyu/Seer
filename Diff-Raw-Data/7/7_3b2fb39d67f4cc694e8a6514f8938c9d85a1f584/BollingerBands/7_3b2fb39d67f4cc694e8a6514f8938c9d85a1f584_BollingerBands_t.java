 package edu.brown.cs32.atian.crassus.backend;
 
 import java.util.*;
 
 import edu.brown.cs32.atian.crassus.gui.StockPlot;
 
 /**
  * @author atian
  * 
  * Bollinger Bands can be used to measure price action volatility.
  * 
  * Typical value of period is 20 days with bands 2 * standard deviations
  * above and below the moving average.
  *
  */
 public class BollingerBands implements Indicator {
 
 	private List<IndicatorDatum> middleBand;
 	private List<IndicatorDatum> lowerBand;
 	private List<IndicatorDatum> upperBand;
 	private int period;
 	private List<StockTimeFrameData> data;
 	private int bandWidth;
 	
	public BollingerBands(List<StockTimeFrameData> data, int period, int bandWidth) {
 		this.data = data;
		this.period = period;
		this.bandWidth = bandWidth;
 		middleBand = new ArrayList<IndicatorDatum>();
 		upperBand = new ArrayList<IndicatorDatum>();
 		lowerBand = new ArrayList<IndicatorDatum>();
 		refresh(data);
 	}
 	
 	/**
 	 * Creates new Bollinger Band stock event.
 	 * 
 	 * @param period	int period of SMA
 	 * @param k			int multiplier of standard deviation; determines
 	 * 					width of the bands.
 	 */
 	public BollingerBands(int period, int bandWidth) {
 		this.period = period;
 		this.lowerBand = new ArrayList<IndicatorDatum>();
 		this.upperBand = new ArrayList<IndicatorDatum>();
 		this.bandWidth = bandWidth;
 	}
 	
 	/**
 	 * Calculates the standard deviation given start and end index of data.
 	 * 
 	 * @param startIndex						int start index
 	 * @param endIndex							int end index
 	 * @param movingAvg							double moving average
 	 * @return									double standard deviation of given close values
 	 * @throws ArrayIndexOutOfBoundsException	if array index of data is out of bounds
 	 */
 	private double calcStdDev(int startIndex, int endIndex, double movingAvg) 
 			throws ArrayIndexOutOfBoundsException {
 		
 		double sum = 0;
 		for (int i = startIndex; i <= endIndex; i++) {
 			double diff = data.get(i).getClose() - movingAvg;
 			sum +=  (diff * diff);
 		}
 		
 		return Math.sqrt(sum / (endIndex - startIndex + 1));
 	}
 	
 	/**
 	 * Calculates the SMA from start index to end index of data
 	 * inclusively.
 	 * 
 	 * @param startIndex	int start index 
 	 * @param endIndex		int end index
 	 * @return				double simple moving average of given close values
 	 * @throws ArrayIndexOutOfBoundsException	if array index of data is out of bounds
 	 */
 	private double calcSMA(int startIndex, int endIndex) throws ArrayIndexOutOfBoundsException {
 		
 		double sum = 0;
 		for (int i = startIndex; i <= endIndex; i++) {
 			sum += data.get(i).getClose();
 		}
 		
 		return sum / (endIndex - startIndex + 1);
 	}
 
 	@Override
 	public void addToPlot(StockPlot stockPlot) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	/**
 	 * Updates the Bollinger Bands.
 	 */
 	private void updateBollingerBands() {
 		for (int i = 0; (i + period - 1) < data.size(); i++) {
 			double avg = calcSMA(i, i + period - 1);
 			double stdDev = calcStdDev(i, i + period - 1, avg);
 			
 			if (i == 0) {																// for first 20 days in list (oldest 20 days)
 				for (int j = 0; j < 19; j++) {
 					middleBand.add(new IndicatorDatum(data.get(j).getTime(), avg));		// add 19 times for previous 19 days
 				}
 			}
 			
 			middleBand.add(new IndicatorDatum(data.get(i + period - 1).getTime(), avg));
 			upperBand.add(new IndicatorDatum(data.get(i + period - 1).getTime(), avg + (bandWidth * stdDev)));
 			lowerBand.add(new IndicatorDatum(data.get(i + period - 1).getTime(), avg - (bandWidth * stdDev)));
 		}
 	}
 
 	@Override
 	public void refresh(List<StockTimeFrameData> data) {
 		this.data = data;
 		updateBollingerBands();
 	}
 }
