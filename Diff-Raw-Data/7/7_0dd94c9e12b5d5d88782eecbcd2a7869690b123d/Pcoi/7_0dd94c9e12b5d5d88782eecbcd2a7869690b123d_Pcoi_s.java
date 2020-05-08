 package gov.usgs.cida.coastalhazards.sld;
 
 import com.google.gson.Gson;
 import com.sun.jersey.api.view.Viewable;
 import gov.usgs.cida.coastalhazards.model.Item;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import javax.ws.rs.core.Response;
 
 /**
  *
  * @author Jordan Walker <jiwalker@usgs.gov>
  */
 public final class Pcoi extends SLDGenerator {
 
     private static final String[] attrs = {
         "PCOL1",
         "PCOL2",
         "PCOL3",
         "PCOL4",
         "PCOL5",
         "POVR1",
         "POVR2",
         "POVR3",
         "POVR4",
         "POVR5",
         "PIND1",
         "PIND2",
         "PIND3",
         "PIND4",
         "PIND5",
         "PCOL",
         "POVR",
         "PIND"
     };
     private static final int STROKE_WIDTH = 3;
     private static final int STROKE_OPACITY = 1;
     private static final float[] thresholds = {10.0f, 25.0f, 50.0f, 75.0f, 90.0f};
     private static final String[] colors = {"#FFFFFF", "#FFE6E6", "#FFCCCD", "#FF9C95", "#FF574A", "#FF0000"};
     
     public Pcoi(Item item) {
         super(item);
     }
     
     @Override
     public String[] getAttrs() {
         return attrs;
     }
     
     @Override
     public Response generateSLD() {
         return Response.ok(new Viewable("/bins_line.jsp", this)).build();
     }
     
     @Override
     public Response generateSLDInfo() {
         Map<String, Object> sldInfo = new LinkedHashMap<String, Object>();
         sldInfo.put("title", item.getSummary().getTiny().getText());
         sldInfo.put("units", "%");
         sldInfo.put("style", getStyle());
         List<Map<String,Object>> bins = new ArrayList<Map<String,Object>>();
         for (int i=0; i<getBinCount(); i++) {
             Map<String, Object> binMap = new LinkedHashMap<String,Object>();
            binMap.put("lowerBound", getThresholds()[i]);
             if (i+1 < getBinCount()) {
                binMap.put("upperBound", getThresholds()[i+1]);
             }
             binMap.put("color", getColors()[i]);
             bins.add(binMap);
         }
         sldInfo.put("bins", bins);
         String toJson = new Gson().toJson(sldInfo, HashMap.class);
         return Response.ok(toJson).build();
     }
     
     public String getId() {
         return item.getWmsService().getLayers();
     }
 
     public String getAttr() {
         return item.getAttr();
     }
 
     public float[] getThresholds() {
         return thresholds;
     }
 
     public String[] getColors() {
         return colors;
     }
     
     public int getBinCount() {
         return colors.length;
     }
     
     @Override
     public String getStyle() {
         return style;
     }
 
     public int getSTROKE_WIDTH() {
         return STROKE_WIDTH;
     }
 
     public int getSTROKE_OPACITY() {
         return STROKE_OPACITY;
     }
 }
