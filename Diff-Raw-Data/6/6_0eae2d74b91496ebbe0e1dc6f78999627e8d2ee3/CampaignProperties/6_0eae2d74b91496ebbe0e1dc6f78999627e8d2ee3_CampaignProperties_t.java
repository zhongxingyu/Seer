 package net.rptools.maptool.model;
 
 import java.awt.Color;
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import net.rptools.maptool.client.ui.token.ColorDotTokenOverlay;
 import net.rptools.maptool.client.ui.token.DiamondTokenOverlay;
 import net.rptools.maptool.client.ui.token.OTokenOverlay;
 import net.rptools.maptool.client.ui.token.ShadedTokenOverlay;
 import net.rptools.maptool.client.ui.token.TokenOverlay;
 import net.rptools.maptool.client.ui.token.TriangleTokenOverlay;
 import net.rptools.maptool.client.ui.token.XTokenOverlay;
 import net.rptools.maptool.client.ui.token.YieldTokenOverlay;
 
 public class CampaignProperties implements Serializable {
 
 	public static final String DEFAULT_TOKEN_PROPERTY_TYPE = "Basic";
 
 	private Map<String, List<TokenProperty>> tokenTypeMap;
     private List<String> remoteRepositoryList;
 
     private Map<String, Map<GUID, LightSource>> lightSourcesMap;
     
     private Map<String, LookupTable> lookupTableMap;
 
     private Map<String, SightType> sightTypeMap;
     
     private String defaultSightType;
     
     private Map<String, TokenOverlay> tokenStates;
     
     public CampaignProperties() {
     	init();
     }
     
     public CampaignProperties(CampaignProperties properties) {
     	tokenTypeMap = new HashMap<String, List<TokenProperty>>(); 
     	for (Entry<String, List<TokenProperty>> entry : properties.tokenTypeMap.entrySet()) {
 
     		List<TokenProperty> typeList = new ArrayList<TokenProperty>();
     		typeList.addAll(properties.tokenTypeMap.get(entry.getKey()));
 
     		tokenTypeMap.put(entry.getKey(), typeList);
     	}
 
     	remoteRepositoryList = new ArrayList<String>(properties.remoteRepositoryList);
     	
 		lookupTableMap = new HashMap<String, LookupTable>();
 		if (properties.lookupTableMap != null) {
 			lookupTableMap.putAll(properties.lookupTableMap);
 		}
 
 		defaultSightType = properties.defaultSightType;
 		sightTypeMap = new HashMap<String, SightType>();
 		if (properties.sightTypeMap != null) {
 			sightTypeMap.putAll(properties.sightTypeMap);
 		}
 
 		// TODO: This doesn't feel right, should we deep copy, or does this do that automatically ?
 		lightSourcesMap = new HashMap<String, Map<GUID, LightSource>>(properties.lightSourcesMap);
 		
 		tokenStates = new HashMap<String, TokenOverlay>();
		
		// TODO: fix for when old campaigns have been loaded into b33+
		if (properties.tokenStates == null || properties.tokenStates.isEmpty()) {
			properties.initTokenStatesMap();
		}
		
 		for (TokenOverlay overlay : properties.tokenStates.values()) {
             overlay = (TokenOverlay)overlay.clone();
             tokenStates.put(overlay.getName(), overlay);
         } // endfor
     }
     
     public void mergeInto(CampaignProperties properties) {
     	
     	if (tokenTypeMap != null) {
     		// This will replace any dups
     		properties.tokenTypeMap.putAll(tokenTypeMap);
     	}
     	
     	if (remoteRepositoryList != null) {
     		// Need to cull out dups
     		for (String repo : properties.remoteRepositoryList) {
     			if (!remoteRepositoryList.contains(repo)) {
     				properties.remoteRepositoryList.add(repo);
     			}
     		}
     	}
     	
     	if (lightSourcesMap != null) {
     		properties.lightSourcesMap.putAll(lightSourcesMap);
     	}
     	
     	if (lookupTableMap != null) {
     		properties.lookupTableMap.putAll(lookupTableMap);
     	}
     	
     	if (sightTypeMap != null) {
     		properties.sightTypeMap.putAll(sightTypeMap);
     	}
     	
     	if (tokenStates != null) {
     	    properties.tokenStates.putAll(tokenStates);
     	}
     }
     
     public Map<String, List<TokenProperty>> getTokenTypeMap() {
     	if (tokenTypeMap == null) {
     		initTokenTypeMap();
     	}
     	return tokenTypeMap;
     }
 
     public Map<String, SightType> getSightTypeMap() {
     	if (sightTypeMap == null) {
     		initSightTypeMap();
     	}
     	return sightTypeMap;
     }
     
     public void setSightTypeMap(Map<String, SightType> map) {
     	sightTypeMap = map;
     }
 
     // TODO: This is for conversion from 1.3b19-1.3b20
     public void setTokenTypeMap(Map<String, List<TokenProperty>> map) {
     	tokenTypeMap = map;
     }
     
     public List<TokenProperty> getTokenPropertyList(String tokenType) {
     	return getTokenTypeMap().get(tokenType);
     }
 
     public List<String> getRemoteRepositoryList() {
     	if (remoteRepositoryList == null) {
     		initRemoteRepositoryList();
     	}
     	return remoteRepositoryList;
     }
 
     public void setRemoteRepositoryList(List<String> list) {
     	remoteRepositoryList = list;
     }
     
     public Map<String, Map<GUID, LightSource>> getLightSourcesMap() {
     	if (lightSourcesMap == null) {
     		initLightSourcesMap();
     	}
     	return lightSourcesMap;
     }
     
     public void setLightSourcesMap(Map<String, Map<GUID, LightSource>> map) {
     	lightSourcesMap = map;
     }
 
     public Map<String, LookupTable> getLookupTableMap() {
     	if (lookupTableMap == null) {
     		initLookupTableMap();
     	}
     	return lookupTableMap;
     }
 
     // TODO: This is for conversion from 1.3b19-1.3b20
     public void setLookupTableMap(Map<String, LookupTable> map) {
     	lookupTableMap = map;
     }
     
     public Map<String, TokenOverlay> getTokenStatesMap() {
         if (tokenStates == null) {
             initTokenStatesMap();
         }
         return tokenStates;
     }
     
     public void setTokenStatesMap(Map<String, TokenOverlay> map) {
         tokenStates = map;
     }
     
     private void init() {
     	initLookupTableMap();
     	initLightSourcesMap();
     	initRemoteRepositoryList();
     	initTokenTypeMap();
     	initSightTypeMap();
         initTokenStatesMap();
     }
 
     private void initLookupTableMap() {
     	if (lookupTableMap != null) {
     		return;
     	}
 		lookupTableMap = new HashMap<String, LookupTable>();
     }
 
     private void initLightSourcesMap() {
     	if (lightSourcesMap != null) {
     		return;
     	}
 		lightSourcesMap = new HashMap<String, Map<GUID, LightSource>>();
 
 		try {
 			Map<String, List<LightSource>> map = LightSource.getDefaultLightSources();
 			for (String key : map.keySet()) {
 				
 	    		Map<GUID, LightSource> lightSourceMap = new LinkedHashMap<GUID, LightSource>();
 	    		for (LightSource source : map.get(key)) {
 	    			lightSourceMap.put(source.getId(), source);
 	    		}
 	    		lightSourcesMap.put(key, lightSourceMap);
 			}
 		} catch (IOException ioe) {
 			ioe.printStackTrace(); 
 		}
     		
     }
 
     private void initRemoteRepositoryList() {
     	if (remoteRepositoryList != null) {
     		return;
     	}
 		remoteRepositoryList = new ArrayList<String>();
 		remoteRepositoryList.add("http://rptools.net/image-indexes/gallery.rpax.gz");
     }
     
     public String getDefaultSightType() {
     	return defaultSightType;
     }
     
     private void initSightTypeMap() {
     	
     	sightTypeMap = new HashMap<String, SightType>();
     	
     	defaultSightType = "Normal";
     	sightTypeMap.put("Normal", new SightType("Normal", 1, null));
     	sightTypeMap.put("Lowlight", new SightType("Lowlight", 2, null));
     	
     	try {
     		sightTypeMap.put("Darkvision", new SightType("Darkvision", 1, LightSource.getDefaultLightSources().get("Generic").get(5)));
 //    		sightTypeMap.put("Darkvision & Lowlight", new SightType("Darkvision", 2, LightSource.getDefaultLightSources().get("Generic").get(4)));
     	} catch (Exception e) {
     		e.printStackTrace();
     	}
     }
     
     private void initTokenTypeMap() {
     	if (tokenTypeMap != null) {
     		return;
     	}
     	
 		tokenTypeMap = new HashMap<String, List<TokenProperty>>();
 
 		List<TokenProperty> list = new ArrayList<TokenProperty>();
     	list.add(new TokenProperty("Strength", "Str"));
     	list.add(new TokenProperty("Dexterity", "Dex"));
     	list.add(new TokenProperty("Constitution", "Con"));
     	list.add(new TokenProperty("Intelligence", "Int"));
     	list.add(new TokenProperty("Wisdom", "Wis"));
     	list.add(new TokenProperty("Charisma", "Char"));
     	list.add(new TokenProperty("HP", true, true, false));
     	list.add(new TokenProperty("AC", true, true, false));
     	list.add(new TokenProperty("Defense", "Def"));
     	list.add(new TokenProperty("Movement", "Mov"));
     	list.add(new TokenProperty("Elevation", "Elv", true, false, false));
     	list.add(new TokenProperty("Description", "Des"));
 
     	tokenTypeMap.put(DEFAULT_TOKEN_PROPERTY_TYPE, list);
     }
     
     private void initTokenStatesMap() {
         tokenStates = new HashMap<String, TokenOverlay>();
         tokenStates.put("Dead", (new XTokenOverlay("Dead", Color.RED, 5)));
         tokenStates.put("Disabled", (new XTokenOverlay("Disabled", Color.GRAY, 5)));
         tokenStates.put("Hidden", (new ShadedTokenOverlay("Hidden", Color.BLACK)));
         tokenStates.put("Prone", (new OTokenOverlay("Prone", Color.BLUE, 5)));
         tokenStates.put("Incapacitated", (new OTokenOverlay("Incapacitated", Color.RED, 5)));
         tokenStates.put("Other", (new ColorDotTokenOverlay("Other", Color.RED, null)));
         tokenStates.put("Other2", (new DiamondTokenOverlay("Other2", Color.RED, 5)));
         tokenStates.put("Other3", (new YieldTokenOverlay("Other3", Color.YELLOW, 5)));
         tokenStates.put("Other4", (new TriangleTokenOverlay("Other4", Color.MAGENTA, 5)));
     }
 }
