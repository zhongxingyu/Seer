 package cubrikproject.tud.likelines.service.impl;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 
 /**
  * The MotionActivityAnalyzer class performs an analysis on converted videos.
  * 
  * @see <a href="http://ieeexplore.ieee.org/xpls/abs_all.jsp?arnumber=927428">MPEG-7 visual motion descriptors</a>
  * @author R. Vliegendhart
  */
 public class MotionActivityAnalyzer {
 	/** path to the binary */
 	private final String motionActivityPath;
 	
 	/** Special output code by the binary for indicating a skipped frame */
 	private final int SKIPPED_FRAME = -1;
 	/** Special output code by the binary for indicating a frame without a motion vector field */
 	private final int NO_MOTION = -2;
 	/** Special output code by the binary for indicating a duplicate frame */
 	private final int DUPLICATE_FRAME = -3;
 	
 	/** Clipping threshold */
 	private final int CLIPPING_THRESHOLD = 32;
 	
 	/**
 	 * Constructs a MotionActivityAnalyzer object.
 	 * @param motionActivityPath Path to motionActivity
 	 */
 	public MotionActivityAnalyzer(String motionActivityPath) {
 		this.motionActivityPath = motionActivityPath;
 	}
 	
 	/**
 	 * Performs motion activity analysis on a video file.
 	 * 
 	 * @param source The video to be analyzed
 	 * @return The results of the analysis
 	 * @throws IOException
 	 */
 	public double[] analyze(String source) throws IOException {
 		final ProcessBuilder pb = new ProcessBuilder(motionActivityPath, source);
 		
 		final Process proc = pb.start();
 		BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
 		String line;
 		
 		// read proc's STDOUT
 		ArrayList<Double> values = new ArrayList<Double>();
 		while ((line = br.readLine()) != null) {
 			values.add(Double.parseDouble(line));
 		}
		
 		double[] scores = processSpecialFrames(values);
 		applyClippingAndNormalize(scores);
 		applyMedianFilter(scores);
 		
 		return scores;
 	}
 
 	/**
 	 * Applies a median filter using a sliding window of 3 elements
 	 * @param scores The results of the analysis
 	 */
 	private void applyMedianFilter(double[] scores) {
 		double[] scores_pre = scores.clone();
 		for (int k = 1; k < scores.length - 1; k++)
 			scores[k] = median(scores_pre[k-1], scores_pre[k], scores_pre[k+1]);
 	}
 
 	/**
 	 * Apply clipping to the extracted motion and normalize.
 	 * 
 	 * @param scores The array to apply the clipping process to
 	 */
 	private void applyClippingAndNormalize(double[] scores) {
 		for (int k = 0; k < scores.length; k++) {
 			if (scores[k] > CLIPPING_THRESHOLD)
 				scores[k] = CLIPPING_THRESHOLD;
 			
 			scores[k] /= CLIPPING_THRESHOLD;
 		}
 	}
 
 	/**
 	 * Post-processes the binary output to deal with special frames.
 	 * 
 	 * @param values Output produced by the binary
 	 * @return An array with special frames (negative values) replaced with correct values
 	 */
 	private double[] processSpecialFrames(ArrayList<Double> values) {
 		double[] scores = new double[values.size()];
 		for (int k = 0; k < scores.length; k++) {
 			double score = scores[k] = values.get(k);
 			
 			if (score == NO_MOTION) {
 				scores[k] = 0;
 			}
 			else if (score == SKIPPED_FRAME || score == DUPLICATE_FRAME) {
 				// Find previous and next non-negative entries
 				int pre = -1;
 				int post = -1;
 				
 				for (int i = k-1; 0 <= i; i--)
 					if (scores[i] >= 0) {
 						pre = i;
 						break;
 					}
 				for (int i = k+1; i < scores.length; i++)
 					if (scores[i] >= 0) {
 						post = i;
 						break;
 					}
 				
 				if (pre == -1 && post == -1)
 					scores[k] = 0;
 				else if (pre == -1)
 					scores[k] = scores[post];
 				else if (post == -1)
 					scores[k] = scores[pre];
 				else
 					scores[k] = 0.5 * (scores[pre] + scores[post]);
 			}
 		}
 		return scores;
 	}
 	
 	/**
 	 * Computes the median of a triplet
 	 * @param a First value
 	 * @param b Second value
 	 * @param c Third value
 	 * @return The median of the given triplet
 	 */
 	private double median(double a, double b, double c) {
 		return Math.max(Math.min(a,b), Math.min(Math.max(a,b),c));
 	}
 }
