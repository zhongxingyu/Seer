 package com.chips.xmlhandler;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.EnumMap;
 import java.util.List;
 
 import org.xml.sax.SAXException;
 
 import com.chips.datarecord.FoodRecord;
 import com.chips.xmlhandler.TagStateEnum.FoodTagState;
 
 public class FoodHandler extends SAXHandler<FoodTagState> {
    private static final int MAX_RESULTS = 30;
     
     public FoodHandler() {
         foodRecords = new ArrayList<FoodRecord>();
         
         buffers = new EnumMap<FoodTagState, StringBuilder>(
                 FoodTagState.class);
         values = Arrays.asList(FoodTagState.values());
     }
 
     @Override
     protected FoodTagState valueOf(String s) {
         return FoodTagState.valueOf(normalize(s));
     }
 
     public void endElement(String namespaceURI, String localName, String qName)
             throws SAXException {
         if (localName.equals("food") && foodRecords.size() < MAX_RESULTS) {
             FoodRecord newFood = new FoodRecord(
                     getString(FoodTagState.ID),
                     getString(FoodTagState.NAME),
                     getString(FoodTagState.CALORIES),
                     getString(FoodTagState.CARBS),
                     getString(FoodTagState.FAT),
                     getString(FoodTagState.PROTEIN));
 
             foodRecords.add(newFood);
 
             clearTagBuffers();
         }
     }
     
     public List<FoodRecord> getFoodRecords() {
         return foodRecords;
     }
     
     private List<FoodRecord> foodRecords;
 }
