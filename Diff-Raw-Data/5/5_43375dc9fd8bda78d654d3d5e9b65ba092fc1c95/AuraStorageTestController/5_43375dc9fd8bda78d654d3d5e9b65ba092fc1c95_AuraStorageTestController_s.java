 /*
  * Copyright (C) 2012 salesforce.com, inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.auraframework.impl.java.controller;
 
 import java.io.IOException;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.auraframework.Aura;
 import org.auraframework.def.ComponentDef;
 import org.auraframework.instance.Component;
 import org.auraframework.system.Annotations.AuraEnabled;
 import org.auraframework.system.Annotations.Controller;
 import org.auraframework.system.Annotations.Key;
 import org.auraframework.util.json.Json;
 import org.auraframework.util.json.JsonSerializable;
 
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 
 @Controller
 public class AuraStorageTestController {
     public static ConcurrentHashMap<String, Integer> staticCounter = new ConcurrentHashMap<String, Integer>();
 
     @AuraEnabled
     public static Record fetchDataRecord(@Key("testName") String testName) {
         staticCounter.putIfAbsent(testName, 0);
         AuraStorageTestController.Record r = new AuraStorageTestController.Record(staticCounter.get(testName),
                 "StorageController");
         staticCounter.put(testName, new Integer(staticCounter.get(testName).intValue() + 1));
         return r;
     }
 
     @AuraEnabled
     public static void resetCounter(@Key("testName") String testName) {
         if (testName != null) {
             staticCounter.remove(testName);
             return;
         } else {
             for (String s : staticCounter.keySet()) {
                 staticCounter.remove(s);
             }
         }
     }
 
     @AuraEnabled
     public static void setCounter(@Key("testName") String testName, @Key("value") Integer value) {
         staticCounter.put(testName, value);
     }
 
     @AuraEnabled
     public static List<Integer> string(@Key("testName") String testName, @Key("param1") Integer param1) {
         staticCounter.putIfAbsent(testName, 0);
         List<Integer> ret = Lists.newArrayList();
         ret.add(staticCounter.get(testName));
         ret.add(param1);
         staticCounter.put(testName, new Integer(staticCounter.get(testName).intValue() + 1));
         return ret;
 
     }
 
     @AuraEnabled
     public static List<Integer> substring(@Key("testName") String testName, @Key("param1") Integer param1) {
         staticCounter.putIfAbsent(testName, 0);
         List<Integer> ret = Lists.newArrayList();
         ret.add(staticCounter.get(testName));
         ret.add(param1);
         staticCounter.put(testName, new Integer(staticCounter.get(testName).intValue() + 1));
         return ret;
     }
 
     /**
      * Object to represent return value for controller.
      */
     static class Record implements JsonSerializable {
         Integer counterValue;
         Object obj;
 
         Record(Integer counter, Object o) {
             this.counterValue = counter;
             this.obj = o;
         }
 
         public Integer getCounterValue() {
             return counterValue;
         }
 
         public Object getObject() {
             return obj;
         }
 
         @Override
         public void serialize(Json json) throws IOException {
             json.writeMapBegin();
             json.writeMapEntry("Counter", getCounterValue());
             json.writeMapEntry("Data", getObject() == null ? "" : getObject());
             json.writeMapEnd();
         }
     }
 
     @AuraEnabled
     public static List<Component> getBaseball(@Key("testName") String testName) throws Exception {
         List<Component> ret = Lists.newArrayList();
         staticCounter.putIfAbsent(testName, 0);
         Integer currentCount = staticCounter.get(testName);
         // On even count get team and on odd count get players
         if (currentCount.intValue() % 2 == 0) {
             Map<String, Object> attr = Maps.newHashMap();
             attr.put("name", "Giants");
             attr.put("city", "San Francisco");
             Component cmp = Aura.getInstanceService()
                     .getInstance("auraStorageTest:teamFacet", ComponentDef.class, attr);
             ret.add(cmp);
         } else {
             Map<String, Object> attr = Maps.newHashMap();
             attr.put("name", "Posey");
             attr.put("nickName", "Buster");
             Component cmp = Aura.getInstanceService().getInstance("auraStorageTest:playerFacet", ComponentDef.class,
                     attr);
             ret.add(cmp);
             attr.put("name", "PSandavol");
             attr.put("nickName", "Panda");
             cmp = Aura.getInstanceService().getInstance("auraStorageTest:playerFacet", ComponentDef.class, attr);
             ret.add(cmp);
         }
         staticCounter.put(testName, new Integer(currentCount.intValue() + 1));
         return ret;
     }
 
 }
