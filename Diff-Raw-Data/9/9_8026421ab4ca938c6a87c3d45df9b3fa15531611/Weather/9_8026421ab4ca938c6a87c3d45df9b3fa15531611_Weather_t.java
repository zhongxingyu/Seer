 package com.fear_airsoft.json;
 
 import java.io.Serializable;
 import java.util.List;
 
 class Weather implements Serializable{
     private String date;
     private String precipMM;
     private String tempMaxC;
     private String tempMinC;
     private List<WeatherIcon> weatherIconUrl;
     private String windspeedKmph;
 
     public boolean equals(Weather other) {
    System.out.println( date.equals(other.date));
    System.out.println(precipMM.equals(other.precipMM));
    System.out.println(tempMinC.equals(other.tempMinC));
    System.out.println(tempMaxC.equals(other.tempMaxC));
    System.out.println(weatherIconUrl.equals(other.weatherIconUrl));
    System.out.println(windspeedKmph.equals(other.windspeedKmph));
    
    
    
         return date.equals(other.date)
             && precipMM.equals(other.precipMM)
             && tempMinC.equals(other.tempMinC)
             && tempMaxC.equals(other.tempMaxC)
             && weatherIconUrl.equals(other.weatherIconUrl)
             && windspeedKmph.equals(other.windspeedKmph);
     }
     
     public String toString() {
         String str = "<"+this.getClass().getName()+":";
         str+="date="+date;
         str+="precipMM="+precipMM;
         str+="tempMaxC="+tempMaxC;
         str+="tempMinC="+tempMinC;
         str+="weatherIconUrl="+weatherIconUrl;
         str+="windspeedKmph="+windspeedKmph;
         str+=">";
         return str;
     }
 
 
     public String getDate() {
         return date;
     }
 
     public void setDate(String date) {
         this.date = date;
     }
 
     public String getPrecipMM() {
         return precipMM;
     }
 
     public void setPrecipMM(String precipMM) {
         this.precipMM = precipMM;
     }
 
     public String getTempMaxC() {
         return tempMaxC;
     }
 
     public void setTempMaxC(String tempMaxC) {
         this.tempMaxC = tempMaxC;
     }
 
     public String getTempMinC() {
         return tempMinC;
     }
 
     public void setTempMinC(String tempMinC) {
         this.tempMinC = tempMinC;
     }
 
     public List<WeatherIcon> getWeatherIconUrl() {
         return weatherIconUrl;
     }
 
     public void setWeatherIconUrl(List<WeatherIcon> weatherIconUrl) {
         this.weatherIconUrl = weatherIconUrl;
     }
 
     public String getWindspeedKmph() {
         return windspeedKmph;
     }
 
     public void setWindspeedKmph(String windspeedKmph) {
         this.windspeedKmph = windspeedKmph;
     }
 }
