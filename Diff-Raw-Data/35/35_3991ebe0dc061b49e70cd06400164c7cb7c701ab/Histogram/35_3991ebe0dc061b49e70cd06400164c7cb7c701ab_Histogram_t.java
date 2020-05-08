 package statistics;
 
 import java.io.Serializable;
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 
 /**
  * A list of bins of values. The number of bins, bin spacing, histogram minimum and maximum are all set once
  * when the histogram is created. addValue(...) increases the count of the appropriate bin, and toString() emits
  * a decently formatted summary. 
  * 
  * @author brendan
  *
  */
 public class Histogram implements Serializable {
 
 	double[] hist;
 	double minValue;
 	double maxValue;
 	double binSpacing;
 	
 	double lessThanMin = 0;
 	double moreThanMax = 0;
 	
 	double currentSum = 0;
 	
 	//We also maintain a running tally of the standard deviation, computing using an algorithm
 	//from the wikipedia page on standard deviation
 	double currentStdev = 0;
 	
 	int count = 0;
 	
 	NumberFormat formatter = new DecimalFormat("0.0###");
 	
 	public Histogram(int bins, double minValue, double binSpacing) {
 		this.binSpacing = binSpacing;
 		this.minValue = minValue;
 		this.maxValue = minValue+bins*binSpacing;
 		hist = new double[bins];
 	}
 	
 	public int getCount() {
 		return count;
 	}
 	
 	public void clear() {
 		for(int i=0; i<hist.length; i++) {
 			hist[i] = 0;
 		}
 		count = 0;
 		moreThanMax = 0;
 		lessThanMin = 0;
 		currentSum = 0;
		currentStdev = 0;
 	}
 	
 	public void addValue(double val) {
 		double prevMean = getMean(); //Needed to compute running stdev
 		count++;
 		if (val<minValue) {
 			lessThanMin++;
 			return;
 		}
 		if (val>=maxValue) {
 			moreThanMax++;
 			return;
 		}
 		
 		currentSum += val; 
 		if (count>1)
 			currentStdev += (val-prevMean)*(val-getMean());
 		hist[ (int)Math.floor( (val-minValue)/(maxValue-minValue)*(double)hist.length ) ]++;
 	}
 	
 	public double getMean() {
 		return currentSum / (double)count;
 	}
 	
 	/**
 	 * This actually returns an exact standard deviation - it's not computed from the bins frequencies; it's kept as a running
 	 * value from all of the values added. 
 	 * @return
 	 */
 	public double getStdev() {
 		return Math.sqrt( currentStdev / (double)count );
 	}
 	
 	public double getBinWidth() {
 		return binSpacing;
 	}
 	
 	public double getFreq(int whichBin) {
 		if (whichBin>=0 && whichBin<hist.length) {
 			return hist[whichBin]/(double)count; 
 		}
 		
 		return Double.NaN;
 	}
 	
 	public String toString() {
 		StringBuilder str = new StringBuilder();
 		if (count == 0) {
 			str.append("(no data collected)");
 		}
 		else {
 			str.append(" < " + minValue + " : " + formatter.format((double)lessThanMin/(double)count) + "\n");
 			for(int i=0; i<hist.length; i++) {
 				str.append(formatter.format(i*binSpacing+minValue) + "\t" + formatter.format(hist[i]/(double)count) + "\n");
 			}
 			str.append(" > " + maxValue + " : " + formatter.format((double)moreThanMax/(double)count) + "\n");
 		}
 		return str.toString();
 	}
 }
