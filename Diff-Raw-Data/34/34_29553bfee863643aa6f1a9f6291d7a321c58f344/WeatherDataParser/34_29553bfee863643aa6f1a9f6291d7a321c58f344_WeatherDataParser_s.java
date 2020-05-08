 package no.vindsiden.parser;
 
 import java.io.IOException;
 
 import no.vindsiden.vindsiden.Measurement;
 import no.vindsiden.weatherstation.WeatherStation;
 
 
 
 /**
  * @author Erik Mohn - mohn.erik@gmail.com
  */
 public abstract class WeatherDataParser {
 
 	private WeatherStation<?> weatherStation;
 	
 	public abstract Measurement fetchMeasurement() throws IOException;
	
	public WeatherDataParser initialize(WeatherStation<?> weatherStation) {
		this.weatherStation = weatherStation;
		return this;
	}
 
 	public void setWeatherStation(WeatherStation<?> weatherStation) {
 		this.weatherStation =  weatherStation;
 	}
 
 	public WeatherStation<?> getWeatherStation() {
 		return weatherStation;
 	}
 	
 }
