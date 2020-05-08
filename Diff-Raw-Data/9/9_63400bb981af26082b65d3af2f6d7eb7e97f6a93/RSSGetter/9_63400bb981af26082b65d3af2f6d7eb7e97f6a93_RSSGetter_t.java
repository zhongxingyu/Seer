 /**
  * class used to parse rss feed
  */
 package org.zoneproject.extractor.rssreader;
 
 /*
  * #%L
  * ZONE-RSSreader
  * %%
  * Copyright (C) 2012 ZONE-project
  * %%
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * #L%
  */
 
 import com.hp.hpl.jena.query.QuerySolution;
 import com.hp.hpl.jena.query.ResultSet;
 import com.sun.syndication.feed.synd.SyndEnclosureImpl;
 import com.sun.syndication.feed.synd.SyndEntry;
 import com.sun.syndication.feed.synd.SyndFeed;
 import com.sun.syndication.io.FeedException;
 import com.sun.syndication.io.SyndFeedInput;
 import com.sun.syndication.io.XmlReader;
 import java.io.File;
 import java.io.IOException;
 import java.net.URI;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.jdom.Element;
 import org.zoneproject.extractor.utils.Database;
 import org.zoneproject.extractor.utils.Item;
 import org.zoneproject.extractor.utils.Prop;
 import org.zoneproject.extractor.utils.ZoneOntology;
 
 /**
  *
  * @author Desclaux Christophe <christophe@zouig.org>
  */
 public class RSSGetter {
     private static final org.apache.log4j.Logger  logger = org.apache.log4j.Logger.getLogger(RSSGetter.class);
     
     /**
      * transform a list of rss feeds links to a list of items
      * @param urls pointing to the rss feeds
      * @return the list of items
      */
     public static ArrayList<Item> getFlux(String[] urls) {
         ArrayList<Item> result = new ArrayList<Item>();
         for(int i=0; i < urls.length;i++){
             String curUri = urls[i];
             logger.info("RSS Feed "+curUri);
             try{
                 URLConnection urlConnection = new URL(curUri).openConnection();
                 urlConnection.setConnectTimeout(10000);
                 XmlReader flux = new XmlReader(urlConnection);
                 result.addAll(RSSGetter.getFlux(curUri,flux));
                 
             } catch (IllegalArgumentException ex) {
                 logger.warn("RSS Feed "+curUri+" not working"+ex);
                 Database.addAnnotation(curUri, new Prop(ZoneOntology.SOURCES_OFFLINE, "true"), ZoneOntology.GRAPH_SOURCES);
             } catch (FeedException ex) {
                 logger.warn("RSS Feed "+curUri+" not working"+ex);
                 Database.addAnnotation(curUri, new Prop(ZoneOntology.SOURCES_OFFLINE, "true"), ZoneOntology.GRAPH_SOURCES);
             } catch (java.net.UnknownHostException ex) {
                 logger.warn("RSS Feed "+curUri+" is offline");
                 Database.addAnnotation(curUri, new Prop(ZoneOntology.SOURCES_OFFLINE, "true"), ZoneOntology.GRAPH_SOURCES);
             } catch (IOException ex) {
                 logger.warn("RSS Feed "+curUri+" is offline");
                 Database.addAnnotation(curUri, new Prop(ZoneOntology.SOURCES_OFFLINE, "true"), ZoneOntology.GRAPH_SOURCES);
             }
         }
         return result;
     }
     
     /**
      * get the list of items contained in a rss feed link
      * @param url
      * @return the list of items
      */
     public static ArrayList<Item> getFlux(String url) {
         try {
             return RSSGetter.getFlux(url,new XmlReader(new URL(url)));
         } catch (Exception ex) {
             Database.addAnnotation(url, new Prop(ZoneOntology.SOURCES_OFFLINE, "true"), ZoneOntology.GRAPH_SOURCES);
             Logger.getLogger(RSSGetter.class.getName()).log(Level.SEVERE, null, ex);
         }
        return new ArrayList<Item>();
     }
     
     /**
      * get the list of items contained in a rss feed file
      * @param file
      * @return 
      */
     public static ArrayList<Item> getFlux(File file) {
         try {
             return getFlux(file.getAbsoluteFile().toString(),new XmlReader(file));
         } catch (Exception ex) {
             Logger.getLogger(RSSGetter.class.getName()).log(Level.SEVERE, null, ex);
         }
         return null;
     }
     
     private static ArrayList<Item> getFlux(String source, XmlReader reader) throws IllegalArgumentException, FeedException{
         ArrayList<Item> items = new ArrayList<Item>();
         
         SyndFeedInput sfi = new SyndFeedInput();
         SyndFeed feed = sfi.build(reader);
         List entries = new ArrayList();
         entries = feed.getEntries();
 
         for (int i = 0; i < entries.size(); i++){
             SyndEntry entry = (SyndEntry)entries.get(i);
 
             //we clean bad Uris
             String uri = entry.getLink();
             uri = uri.replace("\t", "");
             uri = uri.replace("\n", "");
             while(uri.startsWith(" ")) {
                 uri = uri.substring(1);
             }
             while(uri.endsWith(" ")) {
                 uri = uri.substring(0,uri.length()-1);
             }
             //catch if the uri is local
             if(uri.startsWith("/")){
                 uri = "http://"+URI.create(source).getHost()+""+uri;
             }
             if(uri.equals("")) {
                 continue;
             }
 
             //create item
             String description = "";
             if(entry.getDescription() != null){
                 description = entry.getDescription().getValue();
             }
             if(((List)entry.getForeignMarkup()).size() > 0){
                 int biggestVal = 0;
                 String biggestUrl = null;
                 for(Object o: (List)entry.getForeignMarkup()){
                     Element e = (Element)o;
                     if(!e.getName().contains("thumb"))continue;
                     int size = Integer.valueOf(e.getAttributeValue("width"));
                     if(size > biggestVal){
                         biggestUrl = e.getAttributeValue("url");
                         biggestVal = size;
                     }
                 }
                 if(biggestUrl != null){
                     SyndEnclosureImpl attr = new SyndEnclosureImpl();
                     attr.setUrl(biggestUrl);
                     entry.getEnclosures().add(attr);
                 }
             }
             Item cur = new Item(source, uri.toString(),entry.getTitle(),description,entry.getPublishedDate(),entry.getEnclosures());
 
             //add item to list
             items.add(cur);
         }
         return items;
     }
     
     public static String [] getSources(){
         String query = "SELECT *  WHERE {"
                 + "?uri rdf:type <"+ZoneOntology.SOURCES_TYPE+">."
                 + "FILTER (!bif:exists ((select (1) where { ?uri <http://zone-project.org/model/sources#offline> \"true\" } )))"
                 + "FILTER (!bif:exists ((select (1) where { ?uri rdf:type <http://zone-project.org/model/sources#twitter> } )))"
                 + "}";
         ResultSet res = Database.runSPARQLRequest(query, ZoneOntology.GRAPH_SOURCES);
         ArrayList<String> sources = new ArrayList<String>();
         while (res.hasNext()) {
             QuerySolution r = res.nextSolution();
             sources.add(r.get("?uri").toString());
         }
         return sources.toArray(new String[sources.size()]);
     }
 
     public static void main(String[] args){
         
         String fileURI = "http://feeds.bbci.co.uk/news/world/rss.xml";
         ArrayList<Item> result = RSSGetter.getFlux(fileURI);
         for(Item i: result){
             logger.info(i);
         }
     }
 }
