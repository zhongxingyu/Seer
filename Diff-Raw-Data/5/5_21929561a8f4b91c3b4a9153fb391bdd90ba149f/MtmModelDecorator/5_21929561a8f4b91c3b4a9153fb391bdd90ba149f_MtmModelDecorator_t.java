 package com.sl.xmlparser.model.decorator;
 
 import com.sl.xmlparser.config.Configuration;
 import com.sl.xmlparser.model.ProjectModel;
 import java.math.BigDecimal;
 import java.net.URI;
 import java.util.Arrays;
 import java.util.EnumMap;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 public class MtmModelDecorator {
 
     private enum FloorConfiguration {
 
         groundFloor,
         groundFloorWithAttic,
         storied
     }
     private static final int SHORT_DESCRIPTION_MAX_LEN = 200;
     private static final Map<String, String> categoryMapping = new HashMap<String, String>();
     private static final Map<Integer, String> usableSpaceMapping = new LinkedHashMap<Integer, String>();
     private static final Map<String[], String> floorConfigurationMapping = new HashMap<String[], String>();
     private static final Map<Integer, String> garageMapping = new HashMap<Integer, String>();
     private static final Map<String, String> technologyMapping = new HashMap<String, String>();
     private static final Map<String, String> roofTypeMapping = new HashMap<String, String>();
     private static final Map<Integer, String> minimumPlotMappingMapping = new LinkedHashMap<Integer, String>();
 
     static {
         categoryMapping.put("jednorodzinny_z_uzytkowym_poddaszem", "Projekty domów z poddaszem użytkowym");
         categoryMapping.put("jednorodzinny_dwukondygnacyjny", "Projekty domów piętrowych");
         categoryMapping.put("jednorodzinny_parterowy", "Projekty domów parterowych");
        categoryMapping.put("dwurodzinny_dwupokoleniowy", "Projekty domów dwurodzinnych");
        categoryMapping.put("w_zabudowie_blizniaczej", "Projekty domów w zabudowie bliźniaczej");
        categoryMapping.put("letniskowy", "Projekty domów letniskowych");
 
         usableSpaceMapping.put(100, "do 100m2");
         usableSpaceMapping.put(130, "od 100m2 - 130m2");
         usableSpaceMapping.put(160, "od 130m2 - 160m2");
         usableSpaceMapping.put(9999, "powyżej 160m2");
         usableSpaceMapping.put(99999, "Possible error");
 
         floorConfigurationMapping.put(new String[]{"parter"}, "Parterowe");
         floorConfigurationMapping.put(new String[]{"parter", "poddasze"}, "Parterowe z poddaszem");
        floorConfigurationMapping.put(new String[]{"piwnica", "parter", "poddasze"}, "Parterowe z poddaszem");
         floorConfigurationMapping.put(new String[]{"parter", "pietro1"}, "Piętrowe");
 
         garageMapping.put(0, "Brak");
         garageMapping.put(1, "Jednostanowiskowy");
         garageMapping.put(2, "Dwustanowiskowy");
 
         technologyMapping.put("murowany", "Tradycyjna");
         technologyMapping.put("szkieletowy", "Szkieletowe");
         technologyMapping.put("bale", "Z bali");
 
         roofTypeMapping.put("jednospadowy", "Jednospadowy");
         roofTypeMapping.put("dwuspadowy", "Dwuspadowy");
         roofTypeMapping.put("wielospadowy", "Wielospadowy");
         roofTypeMapping.put("płaski", "Płaski");
 
         minimumPlotMappingMapping.put(12, "do 12m");
         minimumPlotMappingMapping.put(14, "do 14m");
         minimumPlotMappingMapping.put(16, "do 16m");
         minimumPlotMappingMapping.put(18, "do 18m");
         minimumPlotMappingMapping.put(20, "do 20m");
         minimumPlotMappingMapping.put(30, "do 30m");
         minimumPlotMappingMapping.put(1000, "powyżej 30");
     }
 
     public static ProjectModel decorateModel(final ProjectModel model, final Configuration configuration) {
         return new ProjectModel() {
             {
                 model.setActive(configuration.getActive());
                 model.setAmount(configuration.getAmountConstant());
             }
 
             @Override
             public String getProjectName() {
                 return model.getProjectName();
             }
 
             @Override
             public void setProjectName(String projectName) {
                 model.setProjectName(projectName.trim());
             }
 
             @Override
             public String getFullDescription() {
                 return model.getFullDescription();
             }
 
             @Override
             public void setFullDescription(String fullDescription) {
                 model.setFullDescription(fullDescription.trim());
             }
 
             @Override
             public String getShortDescription() {
                 return model.getShortDescription();
             }
 
             @Override
             public String getCategory() {
                 return model.getCategory();
             }
 
             @Override
             public String getCategoryForSearcher() {
                 return model.getCategoryForSearcher();
             }
 
             @Override
             public void setCategoryForSearcher(String categoryForSearcher) {
                 model.setCategoryForSearcher(categoryForSearcher);
             }
 
             @Override
             public BigDecimal getPriceWithVat() {
                 return model.getPriceWithVat();
             }
 
             @Override
             public void setPriceWithVat(String priceWithVat) {
                 model.setPriceWithVat(priceWithVat);
             }
 
             @Override
             public Integer getAmount() {
                 return model.getAmount();
             }
 
             @Override
             public void setAmount(Integer Amount) {
                 model.setAmount(Amount);
             }
 
             @Override
             public List<String> getImgMain() {
                 return model.getImgMain();
             }
 
             @Override
             public void addImgMain(String imgMain) {
                 model.addImgMain(imgMain);
             }
 
             @Override
             public List<String> getImgProjection() {
                 return model.getImgProjection();
             }
 
             @Override
             public void addImgProjection(String imgProjection) {
                 model.addImgProjection(imgProjection);
             }
 
             @Override
             public List<String> getImgElevation() {
                 return model.getImgElevation();
             }
 
             @Override
             public void addImgElevation(String imgElevation) {
                 model.addImgElevation(imgElevation);
             }
 
             @Override
             public List<String> getImgLocation() {
                 return model.getImgLocation();
             }
 
             @Override
             public void addImgLocation(String imgLocation) {
                 model.addImgLocation(imgLocation);
             }
 
             @Override
             public Integer getActive() {
                 return model.getActive();
             }
 
             @Override
             public void setActive(Integer active) {
                 model.setActive(active);
             }
 
             @Override
             public BigDecimal getUsableSpace() {
                 return model.getUsableSpace();
             }
 
             @Override
             public void setUsableSpace(String usableSpace) {
                 model.setUsableSpace(usableSpace);
                 model.setUsableSpace2(usableSpace);
                 if (model.getUsableSpace() != null) {
                     int extractedUsableSpace2 = model.getUsableSpace().intValue();
                     for (int compareValue : usableSpaceMapping.keySet()) {
                         //this will be LinkedHash Set, we know the order
                         if (extractedUsableSpace2 < compareValue) {
                             model.setUsableSpace2(usableSpaceMapping.get(compareValue));
                             break;
                         }
                     }
                 }
             }
 
             @Override
             public String getUsableSpace2() {
                 return model.getUsableSpace2();
             }
 
             @Override
             public void setUsableSpace2(String usableSpace2) {
                 model.setUsableSpace2(usableSpace2);
             }
 
             @Override
             public String getFloors() {
                 return model.getFloors();
             }
 
             @Override
             public void setFloors(String floors) {
                 model.setFloors(floors);
                 model.setFloors2(floors);
                 if (floors != null && !floors.isEmpty()) {
                     String[] splittedFloors = floors.trim().split(",");
 
                     boolean found = false;
 
                     for (String[] floorKeys : floorConfigurationMapping.keySet()) {
                         if (Arrays.equals(floorKeys, splittedFloors)) {
                             model.setFloors(floorConfigurationMapping.get(floorKeys));
                             model.setFloors2(floorConfigurationMapping.get(floorKeys));
                             found = true;
                             break;
                         }
                     }
 
                     if (!found) {
                         model.setFloors(Arrays.toString(splittedFloors));
                         model.setFloors2(Arrays.toString(splittedFloors));
                     }
                 }
             }
 
             @Override
             public String getFloors2() {
                 return model.getFloors2();
             }
 
             @Override
             public void setFloors2(String floors2) {
                 model.setFloors2(floors2);
             }
 
             @Override
             public String getGarage() {
                 return model.getGarage();
             }
 
             @Override
             public void setGarage(String garage) {
                 model.setGarage(garage);
                 model.setGarage2(garage);
                 if (garage != null && !garage.isEmpty()) {
                     Integer numberOfGarages = Integer.valueOf(garage);
                     model.setGarage(garageMapping.get(numberOfGarages));
                     model.setGarage2(garageMapping.get(numberOfGarages));
                 }
             }
 
             @Override
             public String getGarage2() {
                 return model.getGarage2();
             }
 
             @Override
             public void setGarage2(String garage2) {
                 model.setGarage2(garage2);
             }
 
             @Override
             public String getTechnology() {
                 return model.getTechnology();
             }
             
             @Override
             public String getTechnology2() {
                 return model.getTechnology2();
             }
 
             @Override
             public void setTechnology(String technology) {
                 model.setTechnology(technology);
                 model.setTechnology2(technology);
                 if (technology != null && !technology.isEmpty()) {
                     model.setTechnology(technologyMapping.get(technology));
                     model.setTechnology2(technologyMapping.get(technology));
                 }
             }
 
             @Override
             public String getRoofType() {
                 return model.getRoofType();
             }
 
             @Override
             public void setRoofType(String roofType) {
                 model.setRoofType(roofType);
                 model.setRoofType2(roofType);
                 if (roofType != null && !roofType.isEmpty()) {
                     model.setRoofType(roofTypeMapping.get(roofType));
                     model.setRoofType2(roofTypeMapping.get(roofType));
                 }
             }
 
             @Override
             public String getRoofType2() {
                 return model.getRoofType2();
             }
 
             @Override
             public void setRoofType2(String roofType2) {
                 model.setRoofType2(roofType2);
             }
 
             @Override
             public String getMinimumPlot() {
                 return model.getMinimumPlot();
             }
 
             @Override
             public void setMinimumPlot(String minimumPlot) {
                 model.setMinimumPlot(minimumPlot);
                 model.setMinimumPlot2(minimumPlot);
                 if (minimumPlot != null && !minimumPlot.isEmpty()) {
                     String[] splittedPlot = minimumPlot.replaceAll("[a-zA-Z&&[^x]]","").trim().split("\\s*x\\s*");
                     if (splittedPlot.length == 2) {
                         int a = Integer.valueOf(new BigDecimal(splittedPlot[0]).intValue());
                         int b = Integer.valueOf(new BigDecimal(splittedPlot[1]).intValue());
                         int c = Math.max(a, b);
 
                         for (int compareValue : minimumPlotMappingMapping.keySet()) {
                             //this will be LinkedHash Set, we know the order
                             if (c < compareValue) {
                                 model.setMinimumPlot2(minimumPlotMappingMapping.get(compareValue));
                                 break;
                             }
                         }
                     }
 
                     model.setMinimumPlot(minimumPlot);
                 }
             }
 
             @Override
             public String getMinimumPlot2() {
                 return model.getMinimumPlot2();
             }
 
             @Override
             public void setMinimumPlot2(String minimumPlot2) {
                 model.setMinimumPlot2(minimumPlot2);
             }
 
             @Override
             public String getBuildInArea() {
                 return model.getBuildInArea();
             }
 
             @Override
             public void setBuildInArea(String buildInArea) {
                 model.setBuildInArea(buildInArea);
             }
 
             @Override
             public String getVolume() {
                 return model.getVolume();
             }
 
             @Override
             public void setVolume(String volume) {
                 model.setVolume(volume);
             }
 
             @Override
             public String getBuildingHeight() {
                 return model.getBuildingHeight();
             }
 
             @Override
             public void setBuildingHeight(String buildingHeight) {
                 model.setBuildingHeight(buildingHeight);
             }
 
             @Override
             public String getSlopeOfTheRoof() {
                 return model.getSlopeOfTheRoof();
             }
 
             @Override
             public void setSlopeOfTheRoof(String slopeOfTheRoof) {
                 model.setSlopeOfTheRoof(slopeOfTheRoof);
             }
 
             @Override
             public String getBasement() {
                 return model.getBasement();
             }
 
             @Override
             public void setBasement(String basement) {
                 model.setBasement(basement);
             }
 
             @Override
             public void setShortDescription(String shortDescription) {
                 if (shortDescription.length() > SHORT_DESCRIPTION_MAX_LEN) {
                     shortDescription = shortDescription.trim().substring(0, SHORT_DESCRIPTION_MAX_LEN);
                 }
                 model.setShortDescription(shortDescription);
             }
 
             @Override
             public void setCategory(String category) {
                 if (categoryMapping.containsKey(category)) {
                     category = categoryMapping.get(category);
                 }
                 model.setCategory(category);
                 model.setCategoryForSearcher(category);
             }
 
             @Override
             public String toString() {
                 return model.toString();
             }
 
             @Override
             public int hashCode() {
                 return model.hashCode();
             }
 
             @Override
             public boolean equals(Object obj) {
                 return model.equals(obj);
             }
         };
     }
 }
