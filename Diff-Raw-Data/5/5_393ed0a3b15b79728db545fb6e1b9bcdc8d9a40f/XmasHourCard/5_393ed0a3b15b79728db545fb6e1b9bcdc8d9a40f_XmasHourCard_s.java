 package com.david.bgwfanspaid.xmas.cards;
 
 import android.content.Context;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.AsyncTask;
 import android.os.StrictMode;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.david.bgwfanspaid.JSONWeatherParser;
 import com.david.bgwfanspaid.R;
 import com.david.bgwfanspaid.Weather;
 import com.david.bgwfanspaid.WeatherHttpClient;
 import com.david.bgwfanspaid.io.network.responses.INetworkResponse;
 import com.david.bgwfanspaid.io.network.responses.NetworkResponse;
 import com.david.bgwfanspaid.io.toolbox.NetworkServiceTask;
 import com.david.bgwfanspaid.io.v2.network.services.ForecastService;
 import com.david.bgwfanspaid.io.v2.transfer.LatLng;
 import com.fima.cardsui.objects.Card;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONException;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.xml.sax.SAXException;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.text.DecimalFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.TimeZone;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 /**
  * Created by david.hodge on 11/10/13.
  */
 public class XmasHourCard extends Card {
 
     class MyWeather {
 
         public String conditiontext;
         public String conditiondate;
         public String conditioncode;
         public String sunrise;
         public String sunset;
 
         public String toString() {
 
             return sunset;
 //            return "It's currently " + conditiondate + (char) 0x00B0 + "\nand " + conditiontext;
 
         }
     }
     static final String baseURL = "http://weather.yahooapis.com/forecastrss?w=12767253";
 
     private TextView weatherView;
     private TextView currentTemp;
     private TextView sunset;
     LatLng locThis;
 
     public XmasHourCard(String title) {
         super(title);
     }
 
     private Document convertStringToDocument(String src) {
         Document dest = null;
 
         DocumentBuilderFactory dbFactory =
                 DocumentBuilderFactory.newInstance();
         DocumentBuilder parser;
 
         try {
             parser = dbFactory.newDocumentBuilder();
             dest = parser.parse(new ByteArrayInputStream(src.getBytes()));
         } catch (ParserConfigurationException e1) {
             e1.printStackTrace();
         } catch (SAXException e) {
             e.printStackTrace();
             String unavil = ("unavilable");
         } catch (IOException e) {
             e.printStackTrace();
         }
 
         return dest;
     }
 
 
     @Override
     public View getCardContent(final Context context) {
         View view = LayoutInflater.from(context).inflate(R.layout.hos_info_card, null);
         String city = "Williamsburg,VA&units=imperial";
 
         StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
         StrictMode.setThreadPolicy(policy);
 
         weatherView = (TextView) view.findViewById(R.id.weather);
         currentTemp = (TextView) view.findViewById(R.id.cur_temp_text);
         sunset = (TextView) view.findViewById(R.id.sunsetText);
 
         Date cDate = new Date();
         String fDate = new SimpleDateFormat("yyyy-MM-dd").format(cDate);
 
         ((TextView) view.findViewById(R.id.title)).setText(title);
 
         //Fixed this area for midly easier readability
         TextView t = (TextView) view.findViewById(R.id.hours);
         if((fDate.equals("2013-11-24") || fDate.equals("2013-12-01") || fDate.equals("2013-12-08") || fDate.equals("2013-12-15") || fDate.equals("2013-12-24"))){
             t.setText("Hours: 3pm - 9pm");
 
         } else if(fDate.equals("2013-11-22") || fDate.equals("2013-11-23") || fDate.equals("2013-11-29") || fDate.equals("2013-11-30") || fDate.equals("2013-12-06") || fDate.equals("2013-12-07") || fDate.equals("2013-12-13") || fDate.equals("2013-12-14") || fDate.equals("2013-12-16") || fDate.equals("2013-12-17") || fDate.equals("2013-12-18") || fDate.equals("2013-12-19") || fDate.equals("2013-12-20") || fDate.equals("2013-12-21") || fDate.equals("2013-12-22") || fDate.equals("2013-12-23") || fDate.equals("2013-12-26")
                 || fDate.equals("2013-12-27") || fDate.equals("2013-12-28") || fDate.equals("2013-12-29") || fDate.equals("2013-12-30") || fDate.equals("2013-12-31")){
             t.setText("Hours: 3pm - 10pm");
 
         } else{
             t.setText("Hours: Closed");
         }
 
 
         if(isNetworkAvailable(context))
         {
 
             ForecastService.Request request1 = ForecastService.Request.newBuilder("9127a75d40728c2aeab21957e38a5a62")
                     .setLatitude(37.234027)
                     .setLongitude(-76.646109)
                     .build();
 
             new NetworkServiceTask() {
 
                 @Override
                 protected void onPostExecute( INetworkResponse network ) {
                     if ( network == null || network.getStatus() == NetworkResponse.Status.FAIL ) {
                         Toast.makeText(context, "FORECAST ERROR", Toast.LENGTH_SHORT).show();
 
                         return;
                     }
 
                     ForecastService.Response response = ( ForecastService.Response ) network;
 
 //                sunset.setText(response.getForecast().getCurrently().getSummary());
                     weatherView.setText(response.getForecast().getMinutely().getSummary());
 //                    sunset.setText(response.getForecast().getCurrently().getSummary());
                     long itemPos = Math.round(response.getForecast().getCurrently().getTemperature());
                     DecimalFormat df = new DecimalFormat("###.#");
                     String itemPosString = df.format(itemPos);
                     currentTemp.setText(itemPosString + (char) 0x00B0);
 //                    currentTemp.setText(Double.toString(Math.ceil(response.getForecast().getCurrently().getTemperature())) + (char) 0x00B0);
                 }
             }.execute( request1 );
 
 //            JSONWeatherTask task = new JSONWeatherTask();
 //            task.execute(new String[]{city});
 
             String weatherString = QueryYahooWeather();
             Document weatherDoc = convertStringToDocument(weatherString);
 
             MyWeather weatherResult = parseWeather(weatherDoc);
             sunset.setText("Sunset: " + weatherResult.toString());
         }
 
         return view;
     }
 
 
     public boolean isNetworkAvailable(Context ctx)
     {
         ConnectivityManager cm = (ConnectivityManager)ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
         NetworkInfo netInfo = cm.getActiveNetworkInfo();
         if (netInfo != null && netInfo.isConnectedOrConnecting()&& cm.getActiveNetworkInfo().isAvailable()&& cm.getActiveNetworkInfo().isConnected())
         {
             return true;
         }
         else
         {
             return false;
         }
     }
 
     public class JSONWeatherTask extends AsyncTask<String, Void, Weather> {
         @Override
         protected Weather doInBackground(String... params) {
             Weather weather = new Weather();
             String data = ( (new WeatherHttpClient()).getWeatherData(params[0]));
 
             try {
                 weather = JSONWeatherParser.getWeather(data);
             } catch (JSONException e) {
                 e.printStackTrace();
                 currentTemp.setText("N/A");
                 weatherView.setText("Unavailable due to network error");
             }
             return weather;
         }
 
 
         @Override
         protected void onPostExecute(Weather weather) {
             super.onPostExecute(weather);
 
//            weatherView.setText(weather.currentCondition.getCondition() + "(" + weather.currentCondition.getDescr() + ")");
 
 //			currentTemp.setText((Double.toString(Math.round(weather.temperature.getTemp())) + (char) 0x00B0 ));
             Date d = new Date(weather.location.getSunset());
             SimpleDateFormat f = new SimpleDateFormat("hh:mm aa");
             f.setTimeZone(TimeZone.getTimeZone("EST"));
             String s = f.format(d);
 //            sunset.setText("Sunrise: " + s);
 
             /**	cityText.setText(weather.location.getCity() + "," + weather.location.getCountry());
              condDescr.setText(weather.currentCondition.getCondition() + "(" + weather.currentCondition.getDescr() + ")");
              temp.setText("" + Math.round((weather.temperature.getTemp() - 275.15)) + "�C");
              hum.setText("" + weather.currentCondition.getHumidity() + "%");
              press.setText("" + weather.currentCondition.getPressure() + " hPa");
              windSpeed.setText("" + weather.wind.getSpeed() + " mps");
              windDeg.setText("" + weather.wind.getDeg() + "�");    **/
 
         }
     }
 
     private MyWeather parseWeather(Document srcDoc) {
 
         MyWeather myWeather = new MyWeather();
 
         //<yweather:condition text="Fair" code="33" temp="60" date="Fri, 23 Mar 2012 8:49 pm EDT"/>
         Node conditionNode = srcDoc.getElementsByTagName("yweather:condition").item(0);
         Node astronomyNode = srcDoc.getElementsByTagName("yweather:astronomy").item(0);
         myWeather.conditiontext = conditionNode.getAttributes()
                 .getNamedItem("text")
                 .getNodeValue()
                 .toString();
         myWeather.conditiondate = conditionNode.getAttributes()
                 .getNamedItem("temp")
                 .getNodeValue()
                 .toString();
         myWeather.conditioncode = conditionNode.getAttributes()
                 .getNamedItem("code")
                 .getNodeValue()
                 .toString();
 
         myWeather.sunrise = astronomyNode.getAttributes()
                 .getNamedItem("sunrise")
                 .getNodeValue()
                 .toString();
 
         myWeather.sunset = astronomyNode.getAttributes()
                 .getNamedItem("sunset")
                 .getNodeValue()
                 .toString();
         return myWeather;
     }
 
     private String QueryYahooWeather() {
 
         String qResult = "";
         String queryString = "http://weather.yahooapis.com/forecastrss?w=12767253";
 
         HttpClient httpClient = new DefaultHttpClient();
         HttpGet httpGet = new HttpGet(queryString);
 
         try {
             HttpEntity httpEntity = httpClient.execute(httpGet).getEntity();
 
             if (httpEntity != null) {
                 InputStream inputStream = httpEntity.getContent();
                 Reader in = new InputStreamReader(inputStream);
                 BufferedReader bufferedreader = new BufferedReader(in);
                 StringBuilder stringBuilder = new StringBuilder();
 
                 String stringReadLine = null;
 
                 while ((stringReadLine = bufferedreader.readLine()) != null) {
                     stringBuilder.append(stringReadLine + "\n");
                 }
 
                 qResult = stringBuilder.toString();
             }
 
         } catch (ClientProtocolException e) {
             e.printStackTrace();
             // Toast.makeText(TodayCard.this,
             //     e.toString(), Toast.LENGTH_LONG).show();
         } catch (IOException e) {
             e.printStackTrace();
             // Toast.makeText(TodayCard.this,
             //     e.toString(), Toast.LENGTH_LONG).show();
         }
 
         return qResult;
     }
 }
