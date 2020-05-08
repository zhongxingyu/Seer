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
 
 /**
  *
  * @author Desclaux Christophe <christophe@zouig.org>
  */
 public class App 
 {
     private static final org.apache.log4j.Logger  logger = org.apache.log4j.Logger.getLogger(App.class);
     public static int SIM_DOWNLOADS = 100;
     public App(){
         String [] tmp = {};
         App.main(tmp);
     }
     public static void main( String[] args )
     {
         String [] sources = RSSGetter.getSources();
         DownloadNewsThread[] th = new DownloadNewsThread[SIM_DOWNLOADS];
         for(int i = 0; i < sources.length; i+=SIM_DOWNLOADS){
             
             for(int curSource = i; (curSource < (i+SIM_DOWNLOADS)) && (curSource < sources.length); curSource++){
                 th[curSource-i] = new DownloadNewsThread(sources[curSource]);
                 th[curSource-i].start();
             }
 
             for(int curSource = i; (curSource < (i+SIM_DOWNLOADS)) && (curSource < sources.length); curSource++){
                 try {
                     if(th[curSource-i] == null)continue;
                     th[curSource-i].join();
                     th[curSource-i]=null;
                 } catch (InterruptedException ex) {
                     logger.warn(ex);
                 }
             }
             
            logger.info("["+(i+Math.min(sources.length%SIM_DOWNLOADS,SIM_DOWNLOADS))+"/"+sources.length+"] news annotated");
         }
         logger.info("Done");
     }
 }
