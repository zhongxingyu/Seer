 package org.zoneproject.extractor.rssreader;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import org.zoneproject.extractor.utils.Database;
 import org.zoneproject.extractor.utils.Item;
 import org.zoneproject.extractor.utils.Prop;
 import org.zoneproject.extractor.utils.VirtuosoDatabase;
 
 
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
 public class DownloadNewsThread extends Thread  {
     private String source;
     private static final org.apache.log4j.Logger  logger = org.apache.log4j.Logger.getLogger(App.class);
 
     public DownloadNewsThread(String source) {
       this.source = source;
     }
     public void run() {
         try {Thread.currentThread().sleep(5000);} catch (InterruptedException ex1) {}
        logger.info("Add ExtractArticlesContent for item: "+source);
         
         //Starting rss downloading
         ArrayList<Item> items = RSSGetter.getFlux(source);
 
         //Cleaning flow with already analysed items
         Database.verifyItemsList(items);
 
         //Printing result items
         for(int i=0; i< items.size();i++)logger.info("\n"+items.get(i));
         
         //saving to 4Store database
         Database.addItems(items);     
   }
 }
