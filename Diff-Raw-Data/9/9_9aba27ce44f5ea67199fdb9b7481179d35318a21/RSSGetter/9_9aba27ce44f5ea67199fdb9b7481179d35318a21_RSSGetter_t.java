 /**
  * class used to parse rss feed
  */
 package org.zoneproject.extractor.rssreader;
 
 import com.sun.syndication.feed.synd.SyndEntry;
 import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
 import com.sun.syndication.io.SyndFeedInput;
 import com.sun.syndication.io.XmlReader;
 import java.io.File;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.zoneproject.extractor.utils.Item;
 
 /**
  *
  * @author Desclaux Christophe <christophe@zouig.org>
  */
 public class RSSGetter {
     /**
      * transform a list of rss feeds links to a list of items
      * @param urls pointing to the rss feeds
      * @return the list of items
      */
     public static ArrayList<Item> getFlux(String[] urls) {
         ArrayList<Item> result = new ArrayList<Item>();
         try {
             for(int i=0; i < urls.length;i++){
                 result.addAll(RSSGetter.getFlux(new XmlReader(new URL(urls[i]))));
                 
             }
         } catch (Exception ex) {
             Logger.getLogger(RSSGetter.class.getName()).log(Level.SEVERE, null, ex);
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
             return RSSGetter.getFlux(new XmlReader(new URL(url)));
         } catch (Exception ex) {
             Logger.getLogger(RSSGetter.class.getName()).log(Level.SEVERE, null, ex);
         }
         return null;
     }
     
     /**
      * get the list of items contained in a rss feed file
      * @param file
      * @return 
      */
     public static ArrayList<Item> getFlux(File file) {
         try {
             return getFlux(new XmlReader(file));
         } catch (Exception ex) {
             Logger.getLogger(RSSGetter.class.getName()).log(Level.SEVERE, null, ex);
         }
         return null;
     }
     
     private static ArrayList<Item> getFlux(XmlReader reader){
         ArrayList<Item> items = new ArrayList<Item>();
         
         SyndFeedInput sfi = new SyndFeedInput();
         try {
             SyndFeed feed = sfi.build(reader);
             List entries = new ArrayList();
             entries = feed.getEntries();
 
             for (int i = 0; i < entries.size(); i++){
                 SyndEntry entry = (SyndEntry)entries.get(i);
                 
                 //create item
                 Item cur = new Item(entry);
                 
                 //add item to list
                 items.add(cur);
             }
         }
        catch(IllegalArgumentException e) {
            System.out.println("RSS Feed "+e+" not working");
        }
        catch(FeedException e) {
            System.out.println("RSS Feed "+e+" not working");
         }
         return items;
     }
     
     
     public static void main(String[] args){
         
         String fileURI = "http://europe1.fr.feedsportal.com/c/32376/f/546041/index.rss";
         ArrayList result = RSSGetter.getFlux(fileURI);
         System.out.println(result);
     }
 }
