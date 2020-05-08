 package cubrikproject.tud.likelines.service;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.URLEncoder;
 
 import com.google.gson.JsonObject;
 
 import cubrikproject.tud.likelines.util.Ajax;
 import cubrikproject.tud.likelines.util.Peaks;
 
 /**
  * The proxy class to talk with a LikeLines server.
  * 
  * @author R. Vliegendhart
  */
 public class LikeLinesService {
 
 	/** The LikeLines server URL with a trailing slash */
 	private final String serverUrl;
 
 	private static final String METHOD_AGGREGATE = "aggregate";
 	
 	/** Default peak detection delta */
 	public final double DEFAULT_PEAK_DELTA = 0.1;
 	
 	/**
 	 * Constructs a proxy for a LikeLines server.
 	 * 
 	 * @param url
 	 *            The address pointing to the LikeLines server
 	 * @throws MalformedURLException When the server URL is not well-formed.
 	 */
 	public LikeLinesService(String url) throws MalformedURLException {
 		serverUrl = ensureTrailingSlash(url);
 	}
 
 	/**
 	 * Makes sure the input string ends with a trailing slash. If not, it is
 	 * added to the string.
 	 * 
 	 * @param input
 	 *            Input string
 	 * @return String with a trailing slash added if it did not end with a
 	 *         slash.
 	 */
 	private String ensureTrailingSlash(String input) {
 		return input.charAt(input.length() - 1) != '/' ? input + '/' : input;
 	}
 
 	/**
 	 * Constructs the URL for a method and its parameters.
 	 * 
 	 * @param method
 	 *            The method to be invoked on the LikeLines server
 	 * @param paramsAndValues
 	 *            A list of named parameters and corresponding values
 	 * @return The URL for the given method set with the given parameters
 	 */
 	String constructUrl(String method, String... paramsAndValues) {
 		assert paramsAndValues.length % 2 == 0 : "paramsAndValues should contain even number of values";
 
 		final StringBuilder url = new StringBuilder(serverUrl);
 		url.append(method);
 
 		char delim = '?';
 		try {
 			for (int i = 0; i < paramsAndValues.length; i += 2) {
 				final String param = paramsAndValues[i];
 				final String value = paramsAndValues[i + 1];
 				url.append(delim);
 				url.append(URLEncoder.encode(param, "UTF-8"));
 				url.append('=');
 				url.append(URLEncoder.encode(value, "UTF-8"));
 				delim = '&';
 			}
 
 		} catch (UnsupportedEncodingException e) {
 			assert false : "UTF-8 should be supported";
 		}
 		return url.toString();
 	}
 
 	/**
 	 * Aggregate user interaction sessions for a given video.
 	 * 
 	 * @param videoId Video ID for which interaction sessions need to be aggregated. Format is "YouTube:<i>videoId</i>" for YouTube videos.
 	 * @return An aggregation of user interaction sessions for the given video.
 	 * @throws IOException
 	 */
 	public Aggregate aggregate(String videoId) throws IOException {
 		String url = constructUrl(METHOD_AGGREGATE, "videoId", videoId);
 		
 		System.out.println(url);
 		try {
 			JsonObject aggregation = Ajax.getJSON(url).getAsJsonObject();
 			Aggregate agg = new Aggregate(aggregation);
 			return agg;
 		}
 		catch (MalformedURLException e) {
 			assert false : "Constructed URL should not be malformed since base server URL is valid";
 		}
 		return null; /* should not reach */
 	}
 
 	
 	/**
 	 * Computes the top N key frames for a queried video and returns the
 	 * time-codes of these key frames.
 	 * 
 	 * @param N The (maximum) number of time-codes to be returned 
 	 * @param videoId The video ID. For YouTube videos: YouTube:video_id.
 	 * @return At most N time-codes.
 	 * @throws IOException
 	 */
 	public double[] getNKeyFrames(int N, String videoId) throws IOException {
 		Aggregate agg = aggregate(videoId);
 		return getNKeyFrames(N, agg);
 	}
 	
 	/**
 	 * Computes the top N key frames for a queried video and returns the
 	 * time-codes of these key frames.
 	 * 
 	 * @param N The (maximum) number of time-codes to be returned 
 	 * @param aggregate A previously retrieved Aggregate object for a video.
 	 * @return At most N time-codes.
 	 * @throws IOException
 	 */
 	public double[] getNKeyFrames(int N, Aggregate aggregate) {
 		double[] heatmap = aggregate.heatmap(aggregate.durationEstimate);
 		Peaks peaks = Peaks.extract(heatmap, null, DEFAULT_PEAK_DELTA);
 		
 		// TODO: Filter and sort peaks
 		System.out.println(">> TODO: First version of getNKeyFrames!");
		double[] timecodes = new double[peaks.peaks.size()];
		for (int i = 0; i < Math.min(N, timecodes.length); i++) {
 			timecodes[i] = peaks.peaks.get(i).x;
 		}
 		return timecodes;
 	}
 
 }
