 
 /**
  * User: pizuricv
  */
 package com.ai.myplugin;
 
 import com.ai.bayes.plugins.BNSensorPlugin;
 import com.ai.bayes.scenario.TestResult;
 import com.ai.util.resource.TestSessionContext;
 import net.xeoh.plugins.base.annotations.PluginImplementation;
 
 import java.io.*;
 import java.net.*;
 import java.util.Properties;
 
 @PluginImplementation
 public class WeatherSensor implements BNSensorPlugin{
     //TODO use JSON parsing later, need a tiny library for this
     String city;
     static final String TEMP = "temperature";
     static final String HUMIDITY = "humidity";
     static final String WEATHER = "weather";
     static final String OPTION = "option";
     static final String CITY = "city";
     static final String server = "http://api.openweathermap.org/";
 
     //default option
     Properties property = new Properties();
 
     String [] weatherStates = {"Clouds", "Clear", "Rain",
             "Storm", "Snow", "Fog", "Mist" , "Drizzle",
             "Smoke", "Dust", "Tropical Storm", "Hot", "Cold" ,
             "Windy", "Hail"};
     String [] humidityStates = {"Low", "Normal", "High"};
     String [] tempStates = {"Freezing", "Cold", "Mild", "Warm", "Heat"};
 
     @Override
     public String[] getRequiredProperties() {
         return new String[] {"City", "Option"};
     }
 
     @Override
     public void setProperty(String string, Object obj) {
         if(string.equalsIgnoreCase(OPTION)) {
             if(!obj.toString().equalsIgnoreCase(TEMP) && !obj.toString().equalsIgnoreCase(HUMIDITY)
                     && !obj.toString().equalsIgnoreCase(WEATHER)){
                 throw new RuntimeException("Property "+ obj + " not in the required settings");
             }
             property.put(OPTION, obj);
         } else if(string.equalsIgnoreCase(CITY)) {
             city = (String) obj;
         } else {
             throw new RuntimeException("Property "+ string + " not in the required settings");
         }
     }
 
     @Override
     public Object getProperty(String string) {
         return property.get(string);
     }
 
     @Override
     public String getDescription() {
         return "Weather information";
     }
 
     @Override
     public TestResult execute(TestSessionContext testSessionContext) {
         if(city == null){
             throw new RuntimeException("City not defined");
         }
 
         URL url;
         boolean testSuccess = true;
 
         try {
             url = new URL(server+ "data/2.5/find?q="+ city+ "&mode=json&units=metric&cnt=0");
         } catch (MalformedURLException e) {
             System.err.println(e.getLocalizedMessage());
             throw new RuntimeException(e);
         }
         HttpURLConnection conn = null;
         try {
             conn = (HttpURLConnection) url.openConnection();
         } catch (IOException e) {
             e.printStackTrace();
             testSuccess = false;
         }
         assert conn != null;
         try {
             conn.setRequestMethod("GET");
         } catch (ProtocolException e) {
             e.printStackTrace();
             testSuccess = false;
         }
 
         BufferedReader rd = null;
         try {
             rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
         } catch (IOException e) {
             e.printStackTrace();
             testSuccess = false;
         }
         String inputLine;
         StringBuffer stringBuffer = new StringBuffer();
 
         assert rd != null;
         try {
             while ((inputLine = rd.readLine()) != null){
                 stringBuffer.append(inputLine);
             }
         } catch (IOException e) {
             e.printStackTrace();
             testSuccess = false;
         }
         conn.disconnect();
         try {
             rd.close();
         } catch (IOException e) {
             e.printStackTrace();
         }
 
 /*        JSONParser parser=new JSONParser();
 
         Object obj = parser.parse(stringBuffer.toString());*/
         final String stringToParse = stringBuffer.toString();
         System.out.println(stringToParse);
         int indexTemp = stringToParse.indexOf("temp");
         String tempString = stringToParse.substring(indexTemp + 6 );
         int index1 = tempString.indexOf(",") == -1? Integer.MAX_VALUE : tempString.indexOf(",");
         int index2 = tempString.indexOf("},") == -1? Integer.MAX_VALUE : tempString.indexOf("},");
         String temperatureString = tempString.substring(0, Math.min(index1, index2));
 
         int indexHumidity = stringToParse.indexOf("humidity");
         String tempHumidity = stringToParse.substring(indexHumidity + 10);
         index1 = tempHumidity.indexOf(",") == -1? Integer.MAX_VALUE : tempHumidity.indexOf(",");
         index2 = tempHumidity.indexOf("},") == -1? Integer.MAX_VALUE : tempHumidity.indexOf("},");
         String humidityString = tempHumidity.substring(0, Math.min(index1, index2));
 
         final int temp = (int)Math.round(Double.parseDouble(temperatureString));
         final int humidity = Integer.parseInt(humidityString);
 
         //get weather ID
         int indexWeather = stringToParse.indexOf("weather\":[{\"id\"");
         String tempWeather = stringToParse.substring(indexWeather + 16 );
         index1 = tempWeather.indexOf(",") == -1? Integer.MAX_VALUE : tempWeather.indexOf(",");
         index2 = tempWeather.indexOf("},") == -1? Integer.MAX_VALUE : tempWeather.indexOf("},");
         final int weatherID = Integer.parseInt(tempWeather.substring(0, Math.min(index1, index2)));
 
 
         final boolean finalTestSuccess = testSuccess;
         return new TestResult() {
             @Override
             public boolean isSuccess() {
                 return finalTestSuccess;
             }
 
             @Override
             public String getName() {
                 return "Weather result";
             }
 
             @Override
             public String getObserverState() {
                 if(property.get(OPTION).equals(TEMP)){
                     return mapTemperature();
                 } else if(property.get(OPTION).equals(WEATHER)){
                     return mapWeather();
                 } else {
                     return mapHumidity();
                 }
             }
 
             private String mapWeather() {
                 //String [] weatherStates = {"Clouds", "Clear", "Rain", "Storm", "Snow", "Fog"};
                 //http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
                 if(weatherID < 300){
                     return "Storm";
                 } else if(weatherID < 400){
                     return "Drizzle";
                 } else if(weatherID < 600){
                     return "Rain";
                 } else if(weatherID < 700){
                     return "Snow";
                 } else if(weatherID == 701){
                     return "Mist";
                 } else if(weatherID == 711){
                     return "Smoke";
                 } else if(weatherID == 721){
                     return "Haze";
                 } else if(weatherID == 731){
                     return "Dust";
                 } else if(weatherID == 741){
                     return "Fog";
                 } else if(weatherID == 800){
                     return "Clear";
                 } else if(weatherID < 900){
                     return "Clouds";
                 } else if(weatherID == 900){
                     return "Tornado";
                 } else if(weatherID == 901){
                     return "Tropical Storm";
                 } else if(weatherID == 902){
                     return "Cold";
                 } else if(weatherID == 903){
                     return "Hot";
                 } else if(weatherID == 904){
                     return "Windy";
                 }  else if(weatherID == 9035){
                     return "Hail";
                 }
                 return "Extreme";
 
             }
 
             private String mapHumidity() {
                 //    String [] humidityStates = {"Low", "Normal", "High"};
                 System.out.println("Map humidity "+humidity);
                 if(humidity < 70) {
                     return "Low";
                 } else if(humidity < 90) {
                     return "Normal";
                 }
                 return "High";
             }
 
             private String mapTemperature() {
                 System.out.println("Map temperature "+temp);
                 if(temp < 0) {
                     return "Freezing";
                 }  else if(temp < 8) {
                     return "Cold";
                 } else if(temp < 15) {
                     return "Mild";
                 }  else if(temp < 25) {
                     return "Warm";
                 }
                 return "Heat";
             };
         };
     }
 
     @Override
     public String getName() {
         return "Weather result";
     }
 
     @Override
     public String[] getSupportedStates() {
         if(TEMP.equals(property.get(OPTION))){
             return tempStates;
         } else if(WEATHER.equals(property.get(OPTION))){
             return weatherStates;
        } else if(HUMIDITY.equals(property.get(OPTION))){
             return humidityStates;
        } else {
            return new String[]{};
         }
     }
 
     public static void main(String[] args){
         WeatherSensor weatherSensor = new WeatherSensor();
         weatherSensor.setProperty("option", WeatherSensor.HUMIDITY);
         weatherSensor.setProperty("city", "Gent");
         TestResult testResult = weatherSensor.execute(null);
         System.out.println(testResult.getObserverState());
 
         weatherSensor.setProperty("option", WeatherSensor.WEATHER);
         System.out.println(testResult.getObserverState());
 
         weatherSensor.setProperty("option", WeatherSensor.TEMP);
         weatherSensor.setProperty("city", "London");
         testResult = weatherSensor.execute(null);
         System.out.println(testResult.getObserverState());
 
 
 
         weatherSensor.setProperty("option", WeatherSensor.WEATHER);
         weatherSensor.setProperty("city", "Sidney");
         testResult = weatherSensor.execute(null);
         System.out.println(testResult.getObserverState());
 
         weatherSensor.setProperty("city", "Bangalore");
         testResult = weatherSensor.execute(null);
         System.out.println(testResult.getObserverState());
 
         weatherSensor.setProperty("city", "Chennai");
         testResult = weatherSensor.execute(null);
         System.out.println(testResult.getObserverState());
 
         weatherSensor.setProperty("city", "Moscow");
         testResult = weatherSensor.execute(null);
         System.out.println(testResult.getObserverState());
 
         weatherSensor.setProperty("city", "Belgrade");
         testResult = weatherSensor.execute(null);
         System.out.println(testResult.getObserverState());
 
         weatherSensor.setProperty("city", "Split");
         testResult = weatherSensor.execute(null);
         System.out.println(testResult.getObserverState());
     }
 }
