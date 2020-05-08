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
 public class DownloadLastsNewsThread extends Thread  {
     private static final org.apache.log4j.Logger  logger = org.apache.log4j.Logger.getLogger(App.class);
 
     public DownloadLastsNewsThread() {
     }
     public void run() {
         String lastRss = "";
         String [] sources;
         while(true){
             sources = RSSGetter.getLastsSources(50);
             for(String cur: sources){
                 if(cur.equals(lastRss)){
                     break;
                 }
                 logger.info("QUICKANNOTATE:"+cur);
                 new DownloadNewsThread(cur).start();
             }
            if(sources.length>0){
                lastRss = sources[0];
            }
             try{Thread.currentThread().sleep(1000);}catch(Exception ie){}
         }
   }
 }
