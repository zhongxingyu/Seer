 package br.com.while42.argentum.indicators;
 
 import br.com.while42.argentum.model.TimeSeries;
 
 public class SimpleMovingAverage implements Indicator {
 
 	private int sizeWindow = 3;	
 	private Indicator indicator;
 	
 	public SimpleMovingAverage(Indicator indicator) {
 		this.indicator = indicator;
 	}
 
 	@Override
 	public double calcule(int position, TimeSeries serie) {
 		double sum = 0.0;
 		for (int i = position; i > position - sizeWindow; i--) {
 			sum += indicator.calcule(i, serie);
 		}
 
 		return sum / sizeWindow;
 	}
 
 	@Override
 	public String toString() {
		return "Simple Moving Average (" + indicator + ")";
 	}
 }
