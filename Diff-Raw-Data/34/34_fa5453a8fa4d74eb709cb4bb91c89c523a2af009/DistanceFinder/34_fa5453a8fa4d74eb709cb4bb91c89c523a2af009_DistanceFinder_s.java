 package edu.westmont.course;
 
 import java.text.DecimalFormat;
 import java.util.Collection;
 import java.util.Iterator;
 import android.location.Location;
 
 public class DistanceFinder {
 
 	private boolean useMetric = true;
 	final private double imperialConversion = 3.28084;
 	private double totalDistance = 0.0;
 	private double altitudeChange = 0.0;
 	final private DecimalFormat oneDecimal = new DecimalFormat("#,##0.0");
 	final private DecimalFormat noDecimal = new DecimalFormat("#,###");
 	private Location previousLocation = null;
 	private long startTime = 1;
 	private String[] lastString = new String[5];
 
 	public String usingMetricOrImperial(){
 		if (useMetric)
 			return "Metric";
 		return "Imperial";
 	}
 
 	public void changeMeasurement(){
 		useMetric = !useMetric;
 	}
 
	public Double getTotalDistance(){
 		return totalDistance;
 	}
 
 	private void addToAltitudeChange(double current){
 		altitudeChange += Math.abs(previousLocation.getAltitude()-current);
 	}
 
	public Double getTotalAltitudeChange(){
 		if (useMetric) return altitudeChange;
 		return altitudeChange*imperialConversion;
 	}
 
	public Double getAverageSpeed(Long timeMillis, double meters){
		double hours = timeMillis.doubleValue()/3600000;
 		double distance = meters;
 		if (useMetric) distance /= 1000;
 		else distance = (distance*imperialConversion)/5280;
 		if (hours > 0) return distance/hours;
 		return 0.0;
 	}
 
 
	public String getSpeedString(Double speed){
 		String output = oneDecimal.format(speed); 
 		if (useMetric) output += " kph";
 		else output += " mph";
 		return output;
 	}
 
 	public String getTotalDistanceString(){
 		return getDistanceString(totalDistance);
 	}
 
	public String getDistanceString(Double dist){
 		if (useMetric)
 			return formatDistanceString(dist);
 		return formatDistanceString(dist*imperialConversion);
 	}
 
	private String formatDistanceString(Double dist){
 		if ((useMetric) && (dist >= 1000))
 			return oneDecimal.format(dist/1000) + " km";
 		if ((!useMetric) && (dist >= 5280))
 			return oneDecimal.format(dist/5280) + " mi";
 		if (useMetric)
 			return noDecimal.format(dist) + " meters";
 		return noDecimal.format(dist) + " feet";
 	}
 
 	public void reset(){
 		altitudeChange = 0.0;
 		totalDistance = 0.0;
 		previousLocation = null;
 		startTime = 1;
 	}
 
 	public void addDistanceToLocation(Location loc){
 		double currentDistance = 0.0;
 		if (previousLocation != null) {
 			addToAltitudeChange(loc.getAltitude());
 			currentDistance = loc.distanceTo(previousLocation);
 			totalDistance += currentDistance;
 		}
 		else if (previousLocation == null) startTime = loc.getTime();
 		lastString = formatTitleString(loc.getTime(), currentDistance, loc.getAltitude());
 		previousLocation = loc;
 	}
 	
 	public String[] getLastString(){
 		return lastString;
 	}
 
 /*public Double calculateAllDistances(Collection<Location> list){
 		Double distance = 0.0;
 		if (list.size() > 1) {
 			Iterator<Location> iterator = list.iterator();
 			Location previous = iterator.next();
 			while (iterator.hasNext()) {
 				Location current = iterator.next();
 				distance += current.distanceTo(previous);
 				previous = current;
 			}
 		}
 		return distance;
 	}*/
 
 	public Long getElapsedTimeMillis(long currentTime){
 		return currentTime-startTime;
 	}
 
 	public String getElapsedTimeString(long currentTime){
 		Integer time = (Integer) getElapsedTimeMillis(currentTime).intValue()/1000;
 		Integer hours = time/3600;
 		time = time % 3600;
 		Integer minutes = time/60;
 		time = time % 60;
 		return (addAZero(hours) + ":" + addAZero(minutes) + ":" + addAZero(time));
 	}
 
 	private String addAZero(Integer input){
 		String output = "";
 		if (input < 10)
 			output += "0";
 		return (output += input.toString());
 	}
 
 	public String[] formatTitleString(long currentTime, double currentDistance, double currentAltitude){
 		if (previousLocation != null) {
 			return new String[] {"Distance: " + getTotalDistanceString(), 
 					"Time: " + getElapsedTimeString(currentTime),
 					"Speed: " + getSpeedString(getAverageSpeed((currentTime-previousLocation.getTime()), currentDistance)), 
 					"Avg. Speed: " + getSpeedString(getAverageSpeed(getElapsedTimeMillis(currentTime), getTotalDistance())),
 					"Altitude: " + noDecimal.format(currentAltitude)};
 		}
 		return new String[] {"Distance: Start","Time: 0","Speed: 0","Avg. Speed: 0","Altitude: " + noDecimal.format(currentAltitude)};
 	}
 }
