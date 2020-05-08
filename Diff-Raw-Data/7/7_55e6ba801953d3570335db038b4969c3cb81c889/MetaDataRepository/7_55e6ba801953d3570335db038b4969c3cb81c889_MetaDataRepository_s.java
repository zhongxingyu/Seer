 package com.servicenow.bigdata.metadata;
 import java.io.FileInputStream;
 import java.util.*;
 import java.io.IOException;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class MetaDataRepository {
 
     public static final String REPO_MAP_NAME = "repository";
     private static final Logger logger = LoggerFactory.getLogger(MetaDataRepository.class);
 
     // repositoryMap stores the map for all NamedPersistentMap including itself
     private static NamedPersistentMap repositoryMap;
     private static HashMap<String, NamedPersistentMap> allMaps = new HashMap<>();
 
     static { // init repository
         try {
            XMLConfigLoader xcl = new XMLConfigLoader();
            String hbaseURL = xcl.get("HBaseURL");
 
             // make sure hbase has the schema
             //HbaseAccess.init(hbaseURL);
             AsynchbaseAccess.init(hbaseURL);
 
             repositoryMap = new NamedPersistentMap(REPO_MAP_NAME);
         } catch (Exception e) {
             logger.error("Cannot initialize MetaDataRepository");
         }
     }
 
     public static boolean mapExists(String mapIDName) {
         return allMaps.containsKey(mapIDName);
     }
 
     public static void addMap(String mapIDName) throws IOException {
         NamedPersistentMap map = new NamedPersistentMap(mapIDName);
         allMaps.put(mapIDName, map);
     }
 
     public static NamedPersistentMap getMap(String mapIDName) throws IOException {
         if(mapExists(mapIDName) == false) {
             addMap(mapIDName);
         }
         return allMaps.get(mapIDName);
     }
 
     public static final void main(String[] args) {
         try {
             MetaDataRepository.getMap("namespace").addKey("usageStats");
             MetaDataRepository.getMap("namespace-metric").addKeyMetric("usageStats", "usageStats.instance01.select-count");
             MetaDataRepository.getMap("metric-" + "user").addKeyMetric("usageStats.instance01.select-count", "eason.hu");
             MetaDataRepository.getMap("metric-" + "table").addKeyMetric("usageStats.instance01.select-count", "table01");
             MetaDataRepository.getMap("metric-" + "app").addKeyMetric("usageStats.instance01.select-count", "app01");
             MetaDataRepository.getMap("metric-" + "user").addKeyMetric("usageStats.instance01.select-count", "eason.hu");
             MetaDataRepository.getMap("metric-" + "table").addKeyMetric("usageStats.instance01.select-count", "table02");
             MetaDataRepository.getMap("metric-" + "app").addKeyMetric("usageStats.instance01.select-count", "app02");
             MetaDataRepository.getMap("metric-" + "user").addKeyMetric("usageStats.instance01.select-count", "eason.hu");
             MetaDataRepository.getMap("metric-" + "table").addKeyMetric("usageStats.instance01.select-count", "table03");
             MetaDataRepository.getMap("metric-" + "app").addKeyMetric("usageStats.instance01.select-count", "app03");
 
             Collection<String> c = MetaDataRepository.getMap("metric-" + "table").getMetrics("usageStats.instance01.select-count");
             for(String k : c) {
                 System.out.println(k);
             }
 
             System.exit(0);
 
         } catch (Exception e) {
             e.printStackTrace();
         }
 
     }
 
 }
