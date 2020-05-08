 package trust.model.math;
 
 import trust.model.exceptions.*;
 import java.text.*;
 
 /**
  * Statistics helper class.
  * @author cfournie
  */
 public class Stats {
 	/** Number of levels */
 	protected int n = 0;
 	
 	/**
 	 * Constructor
 	 * @param n Number of levels
 	 */
 	public Stats(int n) {
 		this.n = n;
 	}
 	
 	/**
 	 * Gets the number of levels
 	 * @return levels
 	 */
 	public int getN() {
 		return n;
 	}
 	
 	/**
 	 * Mean for a d that is a pmf
 	 * @return mean
 	 */
 	public double meanPmf()
 	{
 		return 1/this.n;
 	}
 	
 	/**
 	 * Max variance for a d that is a pmf
 	 * @return confidence
 	 */
 	public double confidencePmfMax()
 	{
 		return (1-this.meanPmf());
 	}
 	
 	/**
 	 * Variance for a d that is a pmf
 	 * @param d pmf
 	 * @return variance
 	 */
 	public double variancePmf(double[] d)
 	{
 		double mean = this.meanPmf();
 		
 		double sum = 0;
 		for(int i = 0; i < d.length; i++)
 			sum += Math.pow(d[i] - mean, 2);
 		
 		return sum / (d.length - 1);
 	}
 	
 	/**
 	 * Mean for a d that is not a pmf
 	 * @param d sample
 	 * @return mean
 	 */
 	public double mean(double[] d)
 	{
 		Misc misc = new Misc(this);
 		misc.checkPmf(d);
 		
 		double sum = 0;
 		for(int j = 0; j < d.length; j++)
 			sum += (j+1) * d[j];
 		
 		return sum - 1;
 	}
 	
 	/**
 	 * Variance for a d that is not a pmf
 	 * @param d sample
 	 * @return variance
 	 */
 	public double variance(double[] d)
 	{
 		Misc misc = new Misc(this);
 		misc.checkPmf(d);
 		
 		double mean = this.mean(d);
 		
 		double sum = 0;
 		for(int j = 0; j < d.length; j++)
 			sum += Math.pow(j+1, 2) * d[j];
 		
 		return sum - Math.pow(mean, 2);
 	}
 	
 	/**
 	 * Shows a pmf, its mean and variance on stdout.
 	 * @param d pmf
 	 */
 	public void printPmf(double[] d) 
 	{
 		if (d.length != getN()) {
 			throw new MalformedTupleException(d.length, this);
 		}
 
 		// mean = E(X)
 		// var = E(X^2) - mean^2
 		double mean = 0.0, var = 0.0;
 		for (int j = 0; j < getN(); j++) {
 			mean += j * d[j];
 			var += j * j * d[j];
 		}
 		
 		var -= mean * mean;
 		
		final DecimalFormat fmt = new DecimalFormat("#.###");
 		System.out.print("[");
 		for (int j = 0; j < getN(); j++) {
 			System.out.print(fmt.format(d[j]));
 			if (j != getN() - 1) {
 				System.out.print(" ");
 			}
 		}
 		System.out.println("] mean=" + fmt.format(mean) + " var=" + fmt.format(var));
 	}
 }
