 package com.abonec.AmoviesParser;
 
 import android.app.Activity;
 import android.content.Context;
 import org.htmlcleaner.HtmlCleaner;
 import org.htmlcleaner.TagNode;
 import org.htmlcleaner.XPatherException;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URL;
 
 /**
  * Created with IntelliJ IDEA.
  * User: abonec
  * Date: 8/28/13
  * Time: 7:11 PM
  * To change this template use File | Settings | File Templates.
  */
 public class AmoviesParser {
     public ProgressUpdate serialProgressCallback;
 
     public interface ProgressUpdate {
         void progressUpdate(int current, int max, AmoviesEntry entry);
     }
 
     HtmlCleaner cleaner;
     URL amoviesUrl;
     int episodesLength;
     private Context context;
     public AmoviesParser(URL htmlPage){
         cleaner = new HtmlCleaner();
         amoviesUrl = htmlPage;
     }
 
     public AmoviesParser(URL htmlPage, Context context) {
         this(htmlPage);
         this.context = context;
     }
 
     void initializeEpisdes(Serial serial){
         try {
             Object[] nodes = cleaner.clean(serial.htmlContent).evaluateXPath("//select[@id=\"series\"]/option");
             for(Object node : nodes) {
                 Serial.SerialEpisode episode = serial.initializeEpisode();
                 TagNode tag = (TagNode) node;
                 episode.vkLink = tag.getAttributeByName("value");
                 episode.title = tag.getText().toString();
             }
         } catch (XPatherException e) {
         }
     }
 
     AmoviesEntry parse() {
         AmoviesEntry entry = getEntry(amoviesUrl);
         parseEntry(entry);
         return entry;
     }
 
     private void parseEntry(AmoviesEntry entry) {
         if(entry.entryType == AmoviesEntry.EntryType.Serial){
             parseSerial((Serial) entry);
         }else{
             parseMovie((Movie) entry);
         }
     }
 
     private Movie parseMovie(Movie movie) {
         TagNode tagNode;
         if((tagNode = getFirstNode(movie.parser, "//div[@class=\"post_text\"]")) != null){
             movie.description = tagNode.getText().toString();
         }
         if((tagNode = getFirstNode(movie.parser, "//article[@class=\"post_full\"]/h1[@class=\"title_d_dot\"]/span")) != null){
             movie.title = tagNode.getText().toString();
         }
         if((tagNode = getFirstNode(movie.parser, "//article[@class=\"post_full\"]/div[@class=\"prev_img\"]/img")) != null){
             movie.posterUrl = tagNode.getAttributeByName("src");
         }
 
        movie.vkLink = getFirstNode(movie.parser, "//li[@class=\"films_if\"]/iframe").getAttributeByName("src");
         try {
             Object[] nodes = movie.parser.evaluateXPath("//article[@class=\"post_full\"]/ul[@class=\"post_info ul_clear\"]/li");
             if (nodes.length > 0){
                 for (Object objectNode : nodes){
                     TagNode node = (TagNode) objectNode;
                     String name = getFirstNode(node, "//strong").getText().toString();
                     String value = getFirstNode(node, "//span").getText().toString();
                     movie.pushProperty(name, value);
                 }
             }
         } catch (XPatherException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
         setMovieVideoLinks(movie);
         return movie;
     }
 
     private void setMovieVideoLinks(Movie movie) {
         Object[] nodes = getResolutionsNodes(getCleaner(movie.vkLink));
         for(Object node : nodes){
             TagNode videoNode = (TagNode) node;
             movie.pushLink(videoNode.getAttributeByName("src"));
         }
     }
 
     private TagNode getFirstNode(TagNode parser, String path){
         Object[] nodes;
         try {
             nodes = parser.evaluateXPath(path);
             if (nodes.length > 0){
                 return (TagNode)nodes[0];
             }
         } catch (XPatherException e) {
         }
         return null;
     }
 
     private Serial parseSerial(Serial serial) {
         initializeEpisdes(serial);
         episodesLength = serial.episodes.size();
         for(Serial.SerialEpisode episode : serial.episodes) {
             TagNode cleaner = getCleaner(episode.vkLink);
             if (cleaner == null) {
                 continue;
             }
             episode.poster = getPoster(cleaner);
             if(episode.poster == null){
                 episode.deprecated = true;
             }
             Object[] nodes = getResolutionsNodes(cleaner);
             for(Object node : nodes){
                 TagNode tagNode = (TagNode) node;
                 episode.pushLink(tagNode.getAttributeByName("src"));
             }
             int currentEpisode = serial.episodes.indexOf(episode);
             serialProgressCallback.progressUpdate(currentEpisode, episodesLength, serial);
 
         }
         return serial;
     }
 
     private AmoviesEntry getEntry(URL amoviesUrl) {
         AmoviesEntry entry = null;
         String content = getContent(amoviesUrl);
         try {
//            cleaner.clean(content).evaluateXPath("//li[@class=\"films_if\"]/iframe")[0].getAttributeByName("src")
             TagNode parser = cleaner.clean(content);
            Object[] film_nodes = parser.evaluateXPath("//li[@class=\"films_if\"]/iframe");
             if(film_nodes.length > 0){
                 entry = new Movie(amoviesUrl, content, parser);
             }else{
                 entry = new Serial(amoviesUrl, content, parser);
             }
         } catch (XPatherException e) {
         }
         return entry;
     }
     private String getContent(URL url) {
         BufferedReader reader;
         StringBuilder stringBuilder = new StringBuilder();
         try {
             reader = new BufferedReader(new InputStreamReader(url.openStream(), "cp1251"));
             String line;
             while((line = reader.readLine()) != null){
                 stringBuilder.append(line);
             }
         } catch (IOException e) {
         }
         return stringBuilder.toString();
     }
 
     private Object[] getResolutionsNodes(TagNode cleaner) {
         try {
             return cleaner.evaluateXPath("//video/source");
         } catch (XPatherException e) {
             return new Object[0];
         }
     }
 
     private TagNode getCleaner(String link){
         try {
             return cleaner.clean(new URL(link));
         } catch (IOException e) {
             return null;
         }
     }
     private String getPoster(TagNode cleaner){
         TagNode videoTag = null;
         try {
             Object[] tags = cleaner.evaluateXPath("//video");
             if(tags.length > 0){
                 videoTag = (TagNode) tags[0];
             } else {
                 return null;
             }
         } catch (XPatherException e) {
             return null;
         }
         return videoTag.getAttributeByName("poster");
     }
 
 }
