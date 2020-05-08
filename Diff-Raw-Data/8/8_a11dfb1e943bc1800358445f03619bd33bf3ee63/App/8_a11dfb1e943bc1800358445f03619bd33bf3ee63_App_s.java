 package org.zoneproject.extractor.twitterreader;
 
 /*
  * #%L
  * ZONE-TwitterReader
  * %%
  * Copyright (C) 2012 - 2013 ZONE-project
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
 
 import java.util.ArrayList;
 import org.zoneproject.extractor.utils.Database;
 import org.zoneproject.extractor.utils.Item;
 
 /**
  *
  * @author Desclaux Christophe <christophe@zouig.org>
  */
 public class App 
 {
     private static final org.apache.log4j.Logger  logger = org.apache.log4j.Logger.getLogger(App.class);
     public App(){
         String [] tmp = {};
         App.main(tmp);
     }
     public static void main(String args[]){
         ArrayList<Item>  items = new ArrayList<Item>();
         String [] feeds = TwitterApi.getSources();
         logger.info("========= Starting twitter timeline downloading==================");
         
         ArrayList<Item> it = TwitterApi.getFlux(feeds);
         items.addAll(it);
         
         logger.info("========= Cleaning flow with already analysed items==================");
           Database.verifyItemsList(items);
         
         logger.info("========= Printing result items==================");
         for(int i=0; i< items.size();i++)logger.info("\n"+items.get(i));
         
         logger.info("========= saving to database==================");
         Database.addItems(items);
         logger.info("Done");

    System.exit(0);
   }
 }
