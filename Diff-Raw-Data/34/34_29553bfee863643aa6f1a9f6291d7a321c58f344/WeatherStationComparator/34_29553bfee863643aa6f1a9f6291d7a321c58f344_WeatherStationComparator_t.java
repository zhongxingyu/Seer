 package no.vindsiden.process.support;
 
 import java.util.Comparator;
 
 import no.vindsiden.weatherstation.WeatherStation;
 
 public class WeatherStationComparator implements Comparator<WeatherStation<?>> {
 
 	@Override
 	public int compare(WeatherStation<?> o1, WeatherStation<?> o2) {
 		if (o1.getWeatherStationId() > o2.getWeatherStationId()) {
 			return 1;
		} else if (o1.getWeatherStationId() < o2.getWeatherStationId()) {
			return -1;
 		}
 		return 0;
 	}
 
 }
