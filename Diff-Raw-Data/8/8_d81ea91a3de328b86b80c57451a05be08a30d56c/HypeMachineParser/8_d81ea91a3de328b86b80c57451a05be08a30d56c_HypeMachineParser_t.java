 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package data;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import org.apache.http.Header;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EntityUtils;
 import org.w3c.dom.Document;
 /**
  *
  * @author fzakaria
  */
 public class HypeMachineParser {
     private Document _dom = null;
     private static HypeMachineParser _instance = null;
     private String _url = "";
     
     private HypeMachineParser()
     {
     }
     
     public static HypeMachineParser getInstance()
     {
         if (_instance == null)
         {
             _instance = new HypeMachineParser();
         }
         return _instance;
     }
     
     public String getURL()
     {
         return _url;
     }
     
     public void setURL(String URL)
     {
         this._url = URL;
     }
     
     public ArrayList<Song> collectSongs(String URL, int pageNumber)
     {
         this.setURL(URL);
         return collectSongs(pageNumber);
     }
     
     public ArrayList<Song> collectSongs(int pageNumber)
     {
         ArrayList<Song> songs = new ArrayList<Song>();
         HttpClient httpclient = new DefaultHttpClient();
         
         String urlLink = this._url + "/" + pageNumber + "?ax=1&ts=" + (new Date()).getTime();
         
         HttpGet httpget = new HttpGet(urlLink);
         HttpResponse response = null;
         try {
             response = httpclient.execute(httpget);
             Header cookieHeader = response.getFirstHeader("Set-Cookie");
             String cookie = cookieHeader.getValue();
             HttpEntity entity = response.getEntity();
             if (entity != null)
             {
                 String HTMLfile = EntityUtils.toString(entity);
                 
                 //System.out.println(HTMLfile);
                 
                Pattern keyPattern = Pattern.compile("\s+key:\s*\'(.+)\'");
                Pattern idPattern = Pattern.compile("\s+id:\s*\'(.+)\'");
                Pattern titlePattern = Pattern.compile("\s+song:\s*\'(.+)\'");
                Pattern artistPattern = Pattern.compile("\s+artist:\s*\'(.+)\'");
                 
                 Matcher keyMatcher = keyPattern.matcher(HTMLfile);
                 Matcher idMatcher = idPattern.matcher(HTMLfile);
                 Matcher titleMatcher = titlePattern.matcher(HTMLfile);
                 Matcher artistMatcher = artistPattern.matcher(HTMLfile);
                 
                 String currentKey = "";
                 String currentID = "";
                 String currentTitle = "";
                 String currentArtist = "";
                 
                 while (idMatcher.find())
                 {
                     keyMatcher.find();
                     titleMatcher.find();
                     artistMatcher.find();
                     
                     currentKey = keyMatcher.group();
                     currentID = idMatcher.group();
                     currentTitle = titleMatcher.group();
                     currentArtist = artistMatcher.group();
                     
                     Song newSong = new Song(currentID, currentKey, currentTitle, currentArtist);
                     newSong.setCookie(cookie);
                     
                     //System.out.println(newSong);
                     
                     songs.add(newSong);
                 }
                 
             }
         } catch (IOException ex) {
             Logger.getLogger(HypeMachineParser.class.getName()).log(Level.SEVERE, null, ex);
             return songs;
         }
         
         return songs;
     }
     
     
 }
