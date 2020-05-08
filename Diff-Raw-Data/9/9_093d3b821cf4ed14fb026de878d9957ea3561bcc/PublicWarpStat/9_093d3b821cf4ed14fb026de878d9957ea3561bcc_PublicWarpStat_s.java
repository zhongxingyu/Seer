 /*
  * Copyright (C) 2012 MineStar.de 
  * 
  * This file is part of FifthElement.
  * 
  * FifthElement is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, version 3 of the License.
  * 
  * FifthElement is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with FifthElement.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.minestar.FifthElement.statistics.warp;
 
 import java.util.Date;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.Queue;
 
 import de.minestar.FifthElement.core.Core;
 import de.minestar.minestarlibrary.database.DatabaseUtils;
 import de.minestar.minestarlibrary.stats.Statistic;
 import de.minestar.minestarlibrary.stats.StatisticType;
 
 public class PublicWarpStat implements Statistic {
 
     private String playerName;
     private String warpName;
     private Date date;
 
     public PublicWarpStat() {
         // EMPTY CONSTRUCTOR FOR REFLECTION ACCESS
     }
 
     public PublicWarpStat(String playerName, String warpName) {
         this.playerName = playerName;
         this.warpName = warpName;
         this.date = new Date();
     }
 
     @Override
     public String getPluginName() {
         return Core.NAME;
     }
 
     @Override
     public String getName() {
        return "WarpPrivate";
     }
 
     @Override
     public LinkedHashMap<String, StatisticType> getHead() {
         LinkedHashMap<String, StatisticType> head = new LinkedHashMap<String, StatisticType>();
 
         head.put("playerName", StatisticType.STRING);
         head.put("warpName", StatisticType.STRING);
         head.put("date", StatisticType.DATETIME);
 
         return head;
     }
 
     @Override
     public Queue<Object> getData() {
         Queue<Object> queue = new LinkedList<Object>();
 
         queue.add(playerName);
         queue.add(warpName);
         queue.add(DatabaseUtils.getDateTimeString(date));
 
         return queue;
     }
 }
