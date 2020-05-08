 package com.atex;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class LoadGeneratorBuildAction extends AbstractMetricsAction {
 
 	private static Logger LOG = Logger.getLogger(LoadGeneratorBuildAction.class.getSimpleName());
 	private String abResult;
 	private double timePerRequest;
 	private double requestsPerSecond;
 	private Pattern tprPattern = Pattern.compile(("Time per request:\\s\\s(\\d*[\\.,]\\d*)"));
 	private Pattern rpsPattern = Pattern.compile(("Requests per second:\\s\\s(\\d*[\\.,]\\d*)"));
 	
 	public LoadGeneratorBuildAction(String abResult) {
 		this.abResult = abResult;
 		parseResult();
 	}
 
 	private void parseResult() {
 		try {
 			Matcher matcher = tprPattern.matcher(abResult);
 			matcher.find();
			timePerRequest = Double.parseDouble(matcher.group(1).replace(',', '.'));
 		}
 		catch(Exception e) {
 			LOG.log(Level.WARNING, "Failed to parse time per request from AB result", e);
 			timePerRequest = -1;
 		}
 		try {
 			Matcher matcher = rpsPattern.matcher(abResult);
 			matcher.find();
			requestsPerSecond = Double.parseDouble(matcher.group(1).replace(',', '.'));
 		}
 		catch(Exception e) {
 			LOG.log(Level.WARNING, "Failed to parse requests per second from AB result", e);
 			requestsPerSecond = -1;
 		}
 		
 	}
 
 	public double getTimePerRequest() {
 		return this.timePerRequest;
 	}
 	
 	public double getRequestPerSecond() {
 		return this.requestsPerSecond;
 	}
 }
