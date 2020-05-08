 package com.inktomi.cirrus.forecast;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Provides a mapping in case clients want to use their own icons instead of the provided NDFD ones.
  *
  * Created by mruno on 8/13/13.
  */
 public enum Icon {
     FOG("Fog", "^n?(fg)\\d*$", "http://forecasts.weather.gov/weather/images/fcicons/fg.jpg"),
     SCATTERED_FOG("Scattered Fog", "^n?(sctfg)\\d*$", "http://forecasts.weather.gov/weather/images/fcicons/sctfg.jpg"),
     BROKEN_FOG("Broken Fog", "^n?(bknfg)\\d*$", "http://forecasts.weather.gov/weather/images/fcicons/sctfg.jpg"),
     BLIZZARD("Blizzard", "^n?(blizzard)\\d*$", "http://forecasts.weather.gov/weather/images/fcicons/blizzard.jpg"),
     DUST("Dust", "^n?(du)\\d*$", "http://forecasts.weather.gov/weather/images/fcicons/du.jpg"),
     SMOKE("Smoke", "^n?(fu)\\d*$", "http://forecasts.weather.gov/weather/images/fcicons/fu.jpg"),
     ICE_PELLET("Ice Pellets", "^n?(ip)\\d*$", "http://forecasts.weather.gov/weather/images/fcicons/ip.jpg"),
     SHOWER("Showers", "^n?(shwrs)\\d*$", "http://forecasts.weather.gov/weather/images/fcicons/hi_shwrs.jpg"),
     SHOWERING_RAIN("Showering Rain", "^n?(shra)\\d*$", "http://forecasts.weather.gov/weather/images/fcicons/shra.jpg"),
     RAIN("Rain", "^n?(ra)\\d*$", "http://graphical.weather.gov/xml/xml_fields_icon_weather_conditions.php"),
     SNOW("Snow", "^n?(sn)\\d*$", "http://forecasts.weather.gov/weather/images/fcicons/sn.jpg"),
     RAIN_AND_SNOW("Rain & Snow", "^n?(rasn)\\d*$", "http://graphical.weather.gov/xml/xml_fields_icon_weather_conditions.php"),
     FREEZING_RAIN("Freezing Rain", "^n?(fzra)\\d*$", "http://forecasts.weather.gov/weather/images/fcicons/fzra.jpg"),
     MIXED("Mixed", "^n?(mix)\\d*$", "http://forecasts.weather.gov/weather/images/fcicons/mix.jpg"),
     RAIN_AND_ICE_PELLETS("Rain & Ice Pellets", "^n?(raip)\\d*$", "http://forecasts.weather.gov/weather/images/fcicons/raip.jpg"),
    THUNDERSTORM("Thunderstorms", "^n?(tsra)\\d*$", "http://forecasts.weather.gov/weather/images/fcicons/tsra.jpg"),
     SCATTERED_THUNDERSTORM("Scattered Thunderstorms", "^n?(scttsra)\\d*$", "http://forecasts.weather.gov/weather/images/fcicons/scttsra.jpg"),
     CLEAR("Clear", "^n?(skc)\\d*$", "http://forecasts.weather.gov/weather/images/fcicons/skc.jpg"),
     FEW_CLOUDS("Few Clouds", "^n?(few)\\d*$", "http://forecasts.weather.gov/weather/images/fcicons/few.jpg"),
     SCATTERED_CLOUDS("Scattered Clouds", "^n?(sct)\\d*$", "http://forecasts.weather.gov/weather/images/fcicons/sct.jpg"),
     BROKEN_CLOUDS("Broken Clouds", "^n?(bkn)\\d*$", "http://forecasts.weather.gov/weather/images/fcicons/bkn.jpg"),
     OVERCAST("Overcast", "^n?(ovc)\\d*$", "http://forecasts.weather.gov/weather/images/fcicons/ovc.jpg"),
     HOT("Hot", "^n?(hot)\\d*$", "http://forecasts.weather.gov/weather/images/fcicons/hot.jpg"),
     COLD("Cold", "^n?(cold)\\d*$", "http://forecasts.weather.gov/weather/images/fcicons/cold.jpg"),
     WIND("Windy", "^n?(wind)\\d*$", "http://forecasts.weather.gov/weather/images/fcicons/wind.jpg");
 
     private final String weatherType;
     private final String weatherRegex;
     private final String iconUrl;
 
     Icon(String weatherType, String weatherRegex, String iconUrl) {
         this.weatherType = weatherType;
         this.weatherRegex = weatherRegex;
         this.iconUrl = iconUrl;
     }
 
     public String getWeatherType() {
         return weatherType;
     }
 
     public Pattern getWeatherRegex() {
         return Pattern.compile(weatherRegex);
     }
 
     public String getIconUrl() {
         return iconUrl;
     }
 
     public static Icon getByIconName(String iconName){
         for( Icon condition : Icon.values() ) {
             Matcher matcher = condition.getWeatherRegex().matcher(iconName);
 
             if( matcher.matches() ){
                 return condition;
             }
         }
 
         return null;
     }
}
