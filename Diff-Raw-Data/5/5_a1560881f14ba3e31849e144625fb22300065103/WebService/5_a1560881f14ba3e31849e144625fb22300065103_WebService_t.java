 package com.shopzilla.ucla.cs130.seotool.team2.service;
 
 import com.shopzilla.ucla.cs130.seotool.team2.model.*;
 //import com.google.api.services.customsearch.*;
 import java.lang.String;
 import java.net.*;
 import java.io.*;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 public class WebService {
    private static final int numResults = 3;
    private static final String key = "AIzaSyB8JAz0MHfwz7s5e5Nv8jf-Ku_WlZbrpPM";
    //private static final String bizSearchID = "013100502047583691894:1dyk11jghmi";
    private static final String liveSearchID = "013036536707430787589:_pqjad5hr1a";
    //private static final String shopzillaSearchID = "013100502047583691894:9ncazeorv5y";
    // method for them to call
    public static WebPage[] service(String query, String targetsite){
       //Jonathan's code here
       
       
       WebPage[] pages = new WebPage[numResults + 1];
       for(int i = 0; i < numResults + 1; i++){
          pages[i] = new WebPage();
       }
       boolean done = false;
      
       try{
          String line;
          StringBuilder build = new StringBuilder();
          // form the url for the google query
          URL url = new URL("https://www.googleapis.com/customsearch/v1?key="
             + key +"&cx=" + liveSearchID + "&q="
             + query+ "&alt=json");
          
          // create the connection
          HttpURLConnection conn = (HttpURLConnection) url.openConnection();
          conn.setRequestMethod("GET");
          conn.setRequestProperty("Accept",  "application/json");
          
          // grab the reply
          BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
          while((line = br.readLine()) != null){
             build.append(line);
          }
          
          // close the reader
          br.close();
          
          // create the JSONObject
          JSONObject results = new JSONObject(build.toString());
          
          // get the JSON element items
          JSONArray items = results.getJSONArray("items");
          int size = items.length();
          
          // array to store the links
          int j = 1;
          JSONObject temp;
          
          for(int i = 0; i < size; i++){
             // grab each element in the array
             temp = items.getJSONObject(i);
             String tempurl = temp.getString("link");
             
             // check if the link belongs to amazon or ebay
             if (tempurl.contains("amazon") || 
                 tempurl.contains("ebay")){
                continue;
             }
             else if(tempurl.contains(targetsite)){
                pages[0].set_url(tempurl);
                pages[0].set_rank(i+1);
                done = true;
                continue;
             }
             
             // grab the link and store into the array of links
             pages[j].set_url(tempurl);
             pages[j].set_rank(i+1);
             // links size counter
             j++;
             if (j == numResults + 1){
                break;
             }
          }
          
          // ------now get the target link link------------
          if(!done){
             /*
          // get a new StringBuilder so garbage isn't collected
             build = new StringBuilder();
          
             if(targetsite.contains("bizrate")){
                url = new URL("https://www.googleapis.com/customsearch/v1?key="
                   + key +"&cx=" + bizSearchID + "&q="
                   + query+ "&alt=json");
             }
             else{
                url = new URL("https://www.googleapis.com/customsearch/v1?key="
                   + key +"&cx=" + shopzillaSearchID + "&q="
                   + query+ "&alt=json");
             }
             
             // create the connection
             conn = (HttpURLConnection) url.openConnection();
             conn.setRequestMethod("GET");
             conn.setRequestProperty("Accept",  "application/json");
             
          // grab the reply
             br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
             while((line = br.readLine()) != null){
                build.append(line);
             }
             
             // close the reader
             br.close();
             
             // create the JSONObject
             results = new JSONObject(build.toString());
             
             // get the JSON element items
             items = results.getJSONArray("items");
             temp = items.getJSONObject(0);
             pages[0].set_url(temp.getString("link"));
             */
             if(targetsite.contains("bizrate")){
               String targeturl = "http://www.bizrate.com/classify?search_box=1&keyword=" + query;
                pages[0].set_url(targeturl);
             }
             else if(targetsite.contains("shopzilla")){
               String targeturl = "http://www.shopzilla.com/search?seach_box=1&sfsk=0&cat_id=1&keyword=" + query;
                pages[0].set_url(targeturl);
             }
          }
          // works on local machine up to here
          
       } catch (IOException e){
          System.err.println("Error during REST invocation of API!");
          System.err.println("Exception Thrown: " + e.getMessage());
          e.printStackTrace();
       } catch (JSONException e){
          System.err.println("Error during JSON construction!");
          System.err.println("Exception Thrown: " +e.getMessage());
          e.printStackTrace();
       }
       
       
       
    //---------------------------------------------------------------------------------
       //Albert's code here
       /*
        * add handling of cases where <100% of the pages are GET-able
        * add handling of more than top 3 pages
        * 
        */
       
       try {
     	  for(int i = 0; i < pages.length; i++) {
     		  
     		  //setup connection
     		  URL url = new URL(pages[i].get_url());
     		  HttpURLConnection conn = (HttpURLConnection)url.openConnection();
     		  conn.setRequestMethod("GET");
     		  BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
     		  
     		  String content = "";
     		  String temp;
     		  
     		  //read the contents of the page
     		  while( (temp = rd.readLine()) != null) {
     			  content += temp;
     		  }
     		  
     		  //close buffered reader
     		  rd.close();
     		  
     		  //fill our the WebPage object with content, keyword, and size
     		  pages[i].set_content(content);
     		  pages[i].set_keyword(query);
     		  pages[i].set_size(content.length());
     		  //add ranking, however need to clarify which ranking it is.
     		  
     	  }
       } catch (Exception e) { //refine the possible error messages
     	  System.err.println("Error during webpage crawling");
     	  e.printStackTrace();
       }
       
       
       return pages;
    }
 
 }
