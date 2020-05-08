 /*
  * Copyright 2011 Gregory P. Moyer
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.syphr.mythtv.proto.data;
 
 import java.util.EnumMap;
 import java.util.Map;
 
 import org.apache.commons.lang3.Pair;
 import org.syphr.mythtv.proto.types.MemStatCategory;
 
 public class MemStats
 {
     private final Map<MemStatCategory, Integer> map;
 
     public MemStats(Pair<MemStatCategory, Integer>... memStats)
     {
         map = new EnumMap<MemStatCategory, Integer>(MemStatCategory.class);
 
         for (Pair<MemStatCategory, Integer> memStat : memStats)
         {
            map.put(memStat.left, memStat.right);
         }
     }
 
     public int getMemStat(MemStatCategory category)
     {
         Integer memStat = map.get(category);
         if (memStat == null)
         {
             return -1;
         }
 
         return memStat;
     }
 
     @Override
     public String toString()
     {
         StringBuilder builder = new StringBuilder();
         builder.append("MemStats [map=");
         builder.append(map);
         builder.append("]");
         return builder.toString();
     }
 }
