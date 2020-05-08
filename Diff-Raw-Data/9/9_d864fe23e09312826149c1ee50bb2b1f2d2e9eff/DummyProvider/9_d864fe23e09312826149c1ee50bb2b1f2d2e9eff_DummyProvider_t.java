 /**
  * 
  */
 package weazzer.weather;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.net.*;
 import java.io.*;
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 
 /**
  * The Class DummyProvider.
  * 
  * @author cosmin, filip
  */
 public class DummyProvider implements WeatherProvider {
 
 	private final String SERVER  = "http://api.wunderground.com";
 	private final String API_KEY = "bf4be9ffc282fa45";
 	
 	/** The measurement unit. */
 	MeasurementUnit measurementUnit = MeasurementUnit.Celsius;
 
 	/** The location. */
 	String location = "Bucharest";
 
 	/**
 	 * Instantiates a new dummy provider.
 	 */
 	public DummyProvider() {
 		// TODO Auto-generated constructor stub
 	}
 
 	/**
 	 * Makes an HTTP GET request and returns the response as a string
 	 */	
 	private static String sendGetRequest(String location)
 	{
 		String result = null;
 		try
 		{
 			URL url = new URL(location);
 			URLConnection conn = url.openConnection();
 			
 			// Get the response
 			BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
 			StringBuffer sb = new StringBuffer();
 			String line;
 			while ((line = rd.readLine()) != null) {
 				sb.append(line);
 			}
 			rd.close();
 			result = sb.toString();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return result;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see weazzer.weather.WeatherProvider#getCurrentWeather()
 	 */
 	public ArrayList<WeatherData> getCurrentWeather() {
 
 		ArrayList<WeatherData> weatherList = new ArrayList<WeatherData>();
 		for (int i = 1; i < 5; i++) {
 			WeatherData wd = new WeatherData();
 
 			wd.humidity = i * 0.8f;
 			wd.temperature = i * 2f;
 			wd.weatherCondition = i != 2 ? "Sunny" : "Rain";
 			wd.when = i == 1 ? "Now" : i == 2 ? "In 2H" : i == 3 ? "In 5H"
 					: "In 10H";
 			wd.windSpeed = i * 2f;
 			wd.icon = i != 2 ? "sunny" : "rain";
 			wd.feelsLike = wd.temperature + 1;
 			weatherList.add(wd);
 		}
 
 		return weatherList;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see weazzer.weather.WeatherProvider#getWeatherForecast(int)
 	 */
 	public ArrayList<WeatherForecast> getWeatherForecast(int daysCount) {	
 
 		try {
 			String url = 
 					String.format(
 						"%s/api/%s/forecast7day/q/%s.json", 
 						SERVER, API_KEY, location
 					);
 			String response = sendGetRequest(url);
 			JSONObject json = new JSONObject(response);
 			JSONArray forecastData = 
 				json.getJSONObject("forecast")
 					.getJSONObject("simpleforecast")
 					.getJSONArray("forecastday");
 			
 			ArrayList<WeatherForecast> wf = new ArrayList<WeatherForecast>();
 			for (int i = 0; i < daysCount && i < forecastData.length(); i++) {
 				JSONObject day = forecastData.getJSONObject(i);
 
 				WeatherForecast forecast = new WeatherForecast();
 				forecast.forecastDate = new GregorianCalendar();
 				forecast.forecastDate.add(Calendar.DAY_OF_MONTH, i);
				forecast.tempMax = Float.parseFloat(day.getJSONObject("high").getString("celsius"));
				forecast.tempMin = Float.parseFloat(day.getJSONObject("low").getString("celsius"));
 				forecast.weatherCondition = day.getString("conditions");
 				forecast.windDirection = day.getJSONObject("avewind").getString("dir");
 				forecast.windSpeed = Float.parseFloat(day.getJSONObject("avewind").getString("kph")); // TODO: or miles?
 				forecast.icon = day.getString("icon");
 
 				wf.add(forecast);
 			}
 			return wf;
 		}
 		catch (Exception ex) {
 			System.out.println(ex);
 		}
 		
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see weazzer.weather.WeatherProvider#getMeasurementUnit()
 	 */
 	public MeasurementUnit getMeasurementUnit() {
 		return measurementUnit;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see weazzer.weather.WeatherProvider#setMeasurementUnit()
 	 */
 	public void setMeasurementUnit(MeasurementUnit value) {
 		measurementUnit = value;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see weazzer.weather.WeatherProvider#getSelectedLocation()
 	 */
 	public WeatherLocation getSelectedLocation() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * weazzer.weather.WeatherProvider#getSuggestedLocation(java.lang.String,
 	 * java.lang.String)
 	 */
 	public ArrayList<WeatherLocation> getSuggestedLocation(String country,
 			String city) {
 		// TODO Auto-generated method stub
 		ArrayList<WeatherLocation> locations = new ArrayList<WeatherLocation>();
 		locations.add(new WeatherLocation("Pitesti", "Romania"));
 		locations.add(new WeatherLocation("Bucuresti", "Romania"));
 
 		return locations;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * weazzer.weather.WeatherProvider#setLocation(weazzer.weather.WeatherLocation
 	 * )
 	 */
 	public void setLocation(WeatherLocation location) {
 		// TODO Auto-generated method stub
 
 	}
 
 }
