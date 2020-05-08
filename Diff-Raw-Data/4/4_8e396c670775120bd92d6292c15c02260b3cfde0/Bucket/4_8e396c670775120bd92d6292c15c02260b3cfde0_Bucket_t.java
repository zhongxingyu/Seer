 package ch.ethz.mlmq.log_analyzer;
 
 import java.math.BigDecimal;
 import java.math.BigInteger;
import java.math.MathContext;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.math.stat.descriptive.rank.Percentile;
 import org.apache.commons.math.util.FastMath;
 
 public class Bucket {
 
 	private Map<Integer, Integer> values = new HashMap<>();
 	private double[] primitiveValuesCache;
 
 	/**
 	 * Timestamp of the first measurement assigned to this bucket
 	 */
 	private long startTimestamp;
 	private Percentile percentileCache;
 	private List<Integer> cachedDurations;
 
 	public Bucket() {
 		this.startTimestamp = Long.MAX_VALUE;
 	}
 
 	public Bucket(long startTimestamp) {
 		this.startTimestamp = startTimestamp;
 	}
 
 	public int count() {
 		int totalCount = 0;
 		for (Integer i : values.keySet()) {
 			totalCount += values.get(i);
 		}
 		return totalCount;
 	}
 
 	private List<Integer> getDurations() {
 		if (cachedDurations != null)
 			return cachedDurations;
 
 		cachedDurations = new ArrayList<>();
 		cachedDurations.addAll(values.keySet());
 		Collections.sort(cachedDurations);
 		return cachedDurations;
 	}
 
 	public void addValue(int duration) {
 		clearCache();
 
 		if (!values.containsKey(duration))
 			values.put(duration, 0);
 
 		values.put(duration, values.get(duration) + 1);
 	}
 
 	private void clearCache() {
 		primitiveValuesCache = null;
 		percentileCache = null;
 		cachedDurations = null;
 	}
 
 	public void addTimestamp(long timestamp) {
 		startTimestamp = Math.min(startTimestamp, timestamp);
 	}
 
 	public double mean() {
 		if (noValues())
 			return 0;
 
 		BigInteger b = new BigInteger("0");
 		for (Integer i : values.keySet()) {
 			b = b.add(BigInteger.valueOf(values.get(i)).multiply(BigInteger.valueOf(i)));
 		}
 		BigDecimal d = new BigDecimal(b);
 
		return d.divide(BigDecimal.valueOf(count()), MathContext.DECIMAL128).doubleValue();
 	}
 
 	private boolean noValues() {
 		return values.isEmpty();
 	}
 
 	public double variance() {
 		if (noValues() || count() == 1)
 			return 0;
 
 		// variance = sum((x_i - mean)^2) / (n - 1)
 		double mean = mean();
 
 		double accum = 0.0;
 		double xiMinusMean = 0.0;
 		double accum2 = 0.0;
 		int count = 0;
 		int times;
 
 		for (Integer i : values.keySet()) {
 			// currentMean += values.get(i) / (double) i; => leads to precision loss
 
 			times = values.get(i);
 			count += values.get(i);
 			xiMinusMean = i - mean;
 			accum += (xiMinusMean * xiMinusMean) * times;
 			accum2 += xiMinusMean * times;
 		}
 		double len = count;
 		return (accum - (accum2 * accum2 / len)) / (len - 1.0);
 	}
 
 	public double stddev() {
 		return Math.sqrt(variance());
 	}
 
 	public double percentile(double percentile) {
 		if (noValues())
 			return 0;
 
 		List<Integer> durations = getDurations();
 		int count = count();
 		double pos = percentile * (count + 1) / 100;
 		double fpos = FastMath.floor(pos);
 		int intPos = (int) fpos;
 
 		int currentPos = 0;
 		for (Integer i : durations) {
 			currentPos += values.get(i);
 			if (currentPos >= intPos) {
 				return i;
 			}
 		}
 		return max();
 	}
 
 	public double min() {
 		if (noValues())
 			return 0;
 		int i = getDurations().get(0);
 		return i;
 	}
 
 	public double max() {
 		if (noValues())
 			return 0;
 		int i = getDurations().get(getDurations().size() - 1);
 		return i;
 	}
 
 	public long getTime() {
 		return startTimestamp;
 	}
 
 	public double median() {
 		return percentile(50.0);
 	}
 }
