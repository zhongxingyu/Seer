 package gov.usgs.cida.coastalhazards.sld;
 
 import com.sun.jersey.api.view.Viewable;
 import gov.usgs.cida.coastalhazards.gson.GsonUtil;
 import gov.usgs.cida.coastalhazards.model.Item;
 import java.util.EnumMap;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import javax.ws.rs.core.Response;
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.lang.StringUtils;
 
 /**
  *
  * @author Jordan Walker <jiwalker@usgs.gov>
  */
 public class SLDGenerator {
 	protected static final String style = "cch";
 	protected static final int STROKE_WIDTH = 3;
 	protected static final int STROKE_OPACITY = 1;
 
 	protected final Item item;
 	protected final Integer ribbon;
 	protected final SLDConfig config;
 	
 	protected static final EnumMap<Item.Type,Map<String, SLDConfig>> generatorMap;
 	static {
 		EnumMap<Item.Type,Map<String, SLDConfig>> gmap = new EnumMap<Item.Type,Map<String, SLDConfig>>(Item.Type.class);
 		
 		Map<String, SLDConfig> stormsMap = new HashMap<String, SLDConfig>();
 		sideEffectMapPut(stormsMap, Pcoi.pcoi);
 		sideEffectMapPut(stormsMap, Extreme.extreme);
 		sideEffectMapPut(stormsMap, DuneHeight.duneCrest);
 		sideEffectMapPut(stormsMap, DuneHeight.duneToe);
 		sideEffectMapPut(stormsMap, MeanWaterLevel.mean);
 		gmap.put(Item.Type.storms, stormsMap);
 		
 		Map<String, SLDConfig> vulnerability = new HashMap<String, SLDConfig>();
 		sideEffectMapPut(vulnerability, BayesianCVI.bayes);
 		sideEffectMapPut(vulnerability, OldSchoolCVI.oldSchool);
 		gmap.put(Item.Type.vulnerability, vulnerability);
 		
 		Map<String, SLDConfig> historical = new HashMap<String, SLDConfig>();
 		sideEffectMapPut(historical, Shorelines.shorelines);
 		sideEffectMapPut(historical, Rates.rates);
 		gmap.put(Item.Type.historical, historical);
 		
 		Map<String, SLDConfig> mixed = new HashMap<String, SLDConfig>();
 		gmap.put(Item.Type.mixed, mixed);
 		
 		generatorMap = gmap;
 	}
 	protected static void sideEffectMapPut(Map<String, SLDConfig> map, SLDConfig conf) {
 		for (String attr : conf.attrs) {
 			map.put(attr, conf);
 		}
 	}
 	
 	public static SLDGenerator getGenerator(Item item, Integer ribbon) {
 		SLDGenerator generator = null;
         
        if (ribbon == null) {
            ribbon = 0;
        }
		
 		try {
 			Item.Type itemDotType = item.getType();
 			String itemAttribute = item.getAttr();
 			
 			Map<String, SLDConfig> typeLookup = generatorMap.get(itemDotType);
 			SLDConfig conf = typeLookup.get(StringUtils.upperCase(itemAttribute));
 			
 			if (null != conf) {
 				generator = new SLDGenerator(item, ribbon, conf);
 			}
 		} catch (Exception e) {
 			//Aint nothin to do here.
 		}
 		
 		if (null == generator) {
 			throw new IllegalArgumentException("Type not found");
 		}
 		
 		return generator;
 	}
 	
 	public static boolean isValidAttr(SLDConfig config, String attr) {
 		return ArrayUtils.contains(config.getAttrs(), attr.toUpperCase());
 	}
 	
 
 	/**
 	 * Use SLDGenerator.getGenerator(item, ribbon) instead.
 	 * @param item
 	 * @param ribbon
 	 * @param config 
 	 */
 	protected SLDGenerator(Item item, Integer ribbon, SLDConfig config) {
 		this.item = item;
 		this.ribbon = ribbon;
 		this.config = config;
 	}
 
 	public Response generateSLD() {
 		return Response.ok(new Viewable(this.config.getJspPath(), this)).build();
 	}
 
 	public Response generateSLDInfo() {
 		Map<String, Object> sldInfo = new LinkedHashMap<String, Object>();
 		sldInfo.put("title", this.item.getSummary().getTiny().getText());
 		sldInfo.put("units", this.config.getUnits());
 		sldInfo.put("style", this.config.getStyle());
 		sldInfo.put("bins", this.config.getBins());
 		String toJson = GsonUtil.getDefault().toJson(sldInfo, HashMap.class);
 		return Response.ok(toJson).build();
 	}
 
 	public String[] getAttrs() {
 		return this.config.attrs;
 	}
 
 	public String getId() {
 		return this.item.fetchWmsService().getLayers();
 	}
 
 	public String getStyle() {
 		return style;
 	}
 
 	public Integer getRibbon() {
 		return this.ribbon;
 	}
 
 	public int getStrokeWidth() {
 		return STROKE_WIDTH;
 	}
 
 	public int getStrokeOpacity() {
 		return STROKE_OPACITY;
 	}
 
 	public String getAttr() {
 		return this.item.getAttr();
 	}
 
 	public float[] getThresholds() {
 		return this.config.getThresholds();
 	}
 
 	public String[] getColors() {
 		return this.config.getColors();
 	}
 
 	public int getBinCount() {
 		return this.config.getColors().length;
 	}
 	
 	public int[] getScales() {
 		return this.config.getScales();
 	}
 	
 	public int getScaleCount() {
 		return this.config.getScales().length;
 	}
 }
