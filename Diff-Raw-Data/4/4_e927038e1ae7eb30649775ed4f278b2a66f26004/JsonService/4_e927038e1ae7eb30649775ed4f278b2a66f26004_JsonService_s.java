 package com.tsupryk.service.service;
 
 import com.tsupryk.api.JsonReadException;
 import com.tsupryk.repository.api.IJsonRepository;
 import com.tsupryk.service.api.IJsonService;
 import org.codehaus.jackson.JsonParser;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 import java.io.IOException;
 import java.util.*;
 
 /**
  * The Class JsonService.
  * Date: 06.09.13
  */
 @Service
 public class JsonService implements IJsonService {
 
     @Autowired
     private IJsonRepository jsonRepository;
 
     @Autowired
     private ObjectMapper jacksonMarshaller;
 
     /**
      * Constants for json, acceptable in JS.
      */
     private static final String DATA = "data";
     private static final String LABEL = "label";
     private static final String COLOR = "color";
 
     /**
      * Constants from raw json file.
      */
     private static final String JSON_CONST_EPAM = "EPAM";
     private static final String JSON_CONST_YEAR = "Year";
 
     @Override
     public Object getGlobalData() {
         Map<String, Object> rawData = getDataMap();
         Map<String, Object> datasets = performToGlobalData(rawData);
         return datasets;
     }
 
     @Override
     public Object getCitiesData() {
         Map<String, Object> rawData = getDataMap();
         Collection<Map<String, Object>> datasets = performToCitiesData(rawData);
         return datasets;
     }
 
     private Map<String, Object> performToGlobalData(Map<String, Object> rawData) {
         Map<String, Object> datasets = new HashMap<>();
         datasets.put(DATA, new ArrayList<List<Double>>());
         datasets.put(LABEL, "Epam");
         datasets.put(COLOR, 0);
 
         Map<String, Object> all = (Map<String, Object>) rawData.get(JSON_CONST_EPAM);
 
         for (Object yearName : all.keySet()) {
             String yearNameString = (String) yearName;
             String year = yearNameString.replace(JSON_CONST_YEAR, "");
 
             Map<String, Object> towns = (Map<String, Object>) all.get(yearNameString);
             double[] yearValues = new double[]{0.0, 0.0, 0.0, 0.0};
 
             for (String townName : towns.keySet()) {
                 List<Integer> townValuesList = (List<Integer>) towns.get(townName);
                 for (int i = 0; i<townValuesList.size(); i++) {
                     yearValues[i] += Double.valueOf(townValuesList.get(i));
                 }
             }
             List<List<Double>> data = (List<List<Double>>) datasets.get(DATA);
             for (int i = 0; i < yearValues.length; i++) {
                 List<Double> ld = new ArrayList<>();
                 ld.add(Integer.valueOf(year) + (0.25 * i));
                 ld.add(yearValues[i]);
                data.add(ld);
             }
         }
         return datasets;
     }
 
     private Map<String, Object> getDataMap() {
         Map<String, Object> rawData = null;
         try {
             String data = jsonRepository.getData();
             jacksonMarshaller.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
             rawData = jacksonMarshaller.readValue(data, HashMap.class);
         } catch (IOException e) {
             throw new JsonReadException(e);
         }
         return rawData;
     }
 
     private Collection<Map<String, Object>> performToCitiesData(Map<String, Object> data) {
         Map<String, Map<String, Object>> datasets = new HashMap<>();
         Map<String, Object> all = (Map<String, Object>) data.get(JSON_CONST_EPAM);
         int citiesCount = 0;
 
         for (Object yearName : all.keySet()) {
             String yearNameString = (String) yearName;
             String year = yearNameString.replace(JSON_CONST_YEAR, "");
 
             Map<String, Object> towns = (Map<String, Object>) all.get(yearNameString);
 
             for (String townName : towns.keySet()) {
                 // init fields
                 if (datasets.get(townName) == null) {
                     Map<String, Object> townMap = new HashMap<>();
                     townMap.put(DATA, new ArrayList<List<Double>>());
                     townMap.put(LABEL, townName);
                     townMap.put(COLOR, citiesCount++);
                     datasets.put(townName, townMap);
                 }
                 List<Integer> townValuesList = (List<Integer>) towns.get(townName);
                 for (int i = 0; i < townValuesList.size(); i++) {
                     List<Double> point = new ArrayList<>();
                     Integer y = new Integer(year);
                     point.add(y + (0.25 * i));
                     point.add(Double.valueOf(townValuesList.get(i)));
                     List<List<Double>> townData1 = (List<List<Double>>) datasets.get(townName).get(DATA);
                     townData1.add(point);
                 }
             }
         }
         return datasets.values();
     }
 }
