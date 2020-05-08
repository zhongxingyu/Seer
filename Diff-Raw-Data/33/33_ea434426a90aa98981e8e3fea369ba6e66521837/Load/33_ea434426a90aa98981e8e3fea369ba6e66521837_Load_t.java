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
 import org.syphr.mythtv.proto.types.LoadCategory;
 
 public class Load
 {
     private final Map<LoadCategory, Double> map;
 
     public Load(Pair<LoadCategory, Double>... loads)
     {
         map = new EnumMap<LoadCategory, Double>(LoadCategory.class);
 
         for (Pair<LoadCategory, Double> load : loads)
         {
            map.put(load.getLeftElement(), load.getRightElement());
         }
     }
 
     public double getLoad(LoadCategory category)
     {
         Double load = map.get(category);
         if (load == null)
         {
             return -1;
         }
 
         return load;
     }
 
     @Override
     public String toString()
     {
         StringBuilder builder = new StringBuilder();
         builder.append("Load [map=");
         builder.append(map);
         builder.append("]");
         return builder.toString();
     }
 }
