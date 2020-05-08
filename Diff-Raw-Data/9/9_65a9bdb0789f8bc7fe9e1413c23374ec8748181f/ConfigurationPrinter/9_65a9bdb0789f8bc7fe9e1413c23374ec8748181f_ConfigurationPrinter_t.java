 /*
  * Copyright (c) 2012 Jonathan Tyers, Steve Ash
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.github.steveash.typedconfig;
 
 import java.util.Iterator;
 
 import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.tree.ConfigurationNode;
 
 /**
  * @author Steve Ash
  */
 public class ConfigurationPrinter {
 
     public String printToString(HierarchicalConfiguration config) {
         StringBuilder sb = new StringBuilder();

        String name = (config.getRootNode() != null ? config.getRootNode().getName() : "");
        sb.append("Configuration root: ").append(name).append("\n");
         sb.append("----------------------------------\n");
         Iterator<String> keys = config.getKeys();
        while (keys != null && keys.hasNext()) {
             String key = keys.next();
             sb.append("  ");
             sb.append("[").append(key).append("] -> ").append(config.getString(key));
             sb.append("\n");
         }
         return sb.toString();
     }
 }
