 package com.fear_airsoft.json;
 
 import java.util.*;
 import javax.servlet.*;
 import javax.servlet.http.*;
 import java.util.logging.*;
 import java.io.*;
 import java.net.*;
 import com.google.appengine.api.memcache.*;
 import com.google.gson.Gson;
 
 public class JsonServletJogo extends HttpServlet{
   private static final long serialVersionUID = 1L;
   private static final Logger logger = Logger.getLogger(JsonServletJogo.class.getName());
   private static final String tempoUrl="http://free.worldweatheronline.com/feed/weather.ashx?format=json&num_of_days=5&key=795c730da3133229131502";
   
  
  @Override
   public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
     resp.setHeader("Access-Control-Allow-Origin", "*");
     resp.setContentType("application/json");
     resp.setCharacterEncoding("UTF-8");
     Jogo[] jogos = getJogo();
     if(jogos!=null&&jogos.length>0){
       Jogo jogo = jogos[0];
       jogo.tempo = getTempo(jogo.getCampo().getLat(), jogo.getCampo().getLng(), printDate(jogo.ano,jogo.mes,jogo.dia));
     }
 	String result = new Gson().toJson(jogos);
     PrintWriter out = resp.getWriter();
     out.write(result);
     out.close();
   }
   
   @Override
   protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{ 
     resp.setHeader("Access-Control-Allow-Origin", "*");
     resp.setHeader("Access-Control-Allow-Methods", "GET");
   }
   
   String printDate(int ano, int mes, int dia){
 	  return ""+ano+"-"+print2digits(mes)+"-"+print2digits(dia);
   }
   
   String print2digits(int num){
 	  String str = ""+num;
	  return str.substring(str.length()-2);
   }
   
   Jogo[] getJogo(){
      return parseJogo(executeGet(JsonServletPublishedData.publishedDataUrl+"jogo"));
   }
   
   Jogo[] parseJogo(String content){
      Gson gson = new Gson();
      return (Jogo[]) gson.fromJson(content, Jogo[].class);
   }
   
  Weather getTempo(double lat, double lng, String data){
     MemcacheService cache = prepareCacheService();
     Weather result=getTempoFromCache(lat,lng,data,cache);
     if(result==null){
       result=parseTempoData(executeGet(tempoUrl+"&q="+lat+","+lng), data);
       cache.put(getTempoCacheKey(lat, lng, data), result, Expiration.byDeltaMillis(3600000));
     }
     return result;
   }
   
   MemcacheService prepareCacheService(){
     MemcacheService cache = MemcacheServiceFactory.getMemcacheService();
     cache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.FINER));
     return cache;
   }
   
   String getTempoCacheKey(String lat, String lng, String data){
     return lat+","+lng+","+data;
   }
   
   Weather getTempoFromCache(String lat, String lng, String data, MemcacheService cache){
     String key = getTempoCacheKey(lat, lng, data);
     return (Weather)cache.get(key);
   }
   
   Tempo parseTempo(String content){
     Gson gson = new Gson();
     return (Tempo) gson.fromJson(content, Tempo.class);
   }
   
   Weather parseTempoData(String content, String data){
    Tempo dataJson = parseTempo(content);
     Weather foundWeather=null;
     List<Weather> weatherList =  dataJson.getData().getWeather();
     for (Weather weather : weatherList){
       String weatherData = weather.getDate();
       if(data.equals(weatherData)){
         foundWeather=weather;
         break;
       }
     }
 	return foundWeather;
   }
   
   String executeGet(String targetURL){
     URL url;
     HttpURLConnection connection = null;  
     try {
       url = new URL(targetURL);
       connection = (HttpURLConnection)url.openConnection();
       connection.setRequestMethod("GET");
       connection.setUseCaches(true);
       BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
       StringBuffer response = new StringBuffer();
       String inputLine;
       while((inputLine = in.readLine())!=null) 
         response.append(inputLine);
       in.close();
       return response.toString();
     } catch (Exception e) {
       logger.throwing(JsonServletJogo.class.getName(),"executeGet",e);
       return null;
     } finally {
       if(connection != null) {
         connection.disconnect(); 
       }
     }
   }
 }
