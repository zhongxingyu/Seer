 package no.vindsiden.parser.impl;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import no.vindsiden.parser.WeatherDataParser;
 import no.vindsiden.parser.impl.support.WindDirection;
 import no.vindsiden.vindsiden.Measurement;
 import no.vindsiden.weatherstation.WeatherStation;
 import no.vindsiden.weatherstation.impl.WeatherDisplayWeatherStation;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 
 public class WeatherDisplayDataParser extends WeatherDataParser {
 
 	private Document document;
 	private URL url;
 	private Measurement measurement;
 	
 	public WeatherDisplayDataParser(WeatherStation<WeatherDisplayDataParser> weatherStation)  {
 		setWeatherStation(weatherStation);
 	}
 	
 	@Override
 	public Measurement fetchMeasurement() throws IOException {	
 		initializeURLLocation();
 		fetchDocument();
 		parseMeasurement();
 		return measurement;
 	}
 	
 	private void parseMeasurement() {
 		measurement = new Measurement();
 		String temperatureString = document.getElementById("ajaxtemp").text();
		measurement.setStationID(getWeatherStation().getWeatherStationId());
 		measurement.setTemperature1(parseDoubleText(temperatureString.substring(0, temperatureString.length() - 2)));	
 		measurement.setDirectionAvg(WindDirection.getWindDirectionFromString(document.getElementById("ajaxwinddir").text()));
 		measurement.setWindMin(parseDoubleText(document.getElementById("ajaxwind").text()));
 		measurement.setWindAvg(parseDoubleText(document.getElementById("ajaxwind").text()));
 		String windGust = document.getElementById("ajaxgust").text();
 		measurement.setWindMax(parseDoubleText(windGust.substring(0, windGust.length() - 4)));
 	}
 
 	private Double parseDoubleText(String text) {
 		return Double.parseDouble(text.replace(",", "."));
 	}
 	
 	private void initializeURLLocation() throws MalformedURLException {
 		url = new URL(((WeatherDisplayWeatherStation) getWeatherStation()).getUrl());
 	}
 	
 	private void fetchDocument() throws IOException {
		document = Jsoup.parse(url.openStream(), "CP1252", ((WeatherDisplayWeatherStation) getWeatherStation()).getUrl());
 	}
 }
