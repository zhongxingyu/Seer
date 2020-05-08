 /*
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
 package net.rptools.maptool.model;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Map.Entry;
 
 import net.rptools.lib.MD5Key;
 import net.rptools.maptool.client.AppPreferences;
 import net.rptools.maptool.client.MapTool;
 import net.rptools.maptool.client.macro.MacroManager;
 import net.rptools.maptool.client.ui.token.BarTokenOverlay;
 import net.rptools.maptool.client.ui.token.ImageTokenOverlay;
 import net.rptools.maptool.client.ui.token.BooleanTokenOverlay;
 import net.rptools.maptool.client.ui.token.MultipleImageBarTokenOverlay;
 import net.rptools.maptool.client.ui.token.SingleImageBarTokenOverlay;
 import net.rptools.maptool.client.ui.token.TwoImageBarTokenOverlay;
 import net.rptools.maptool.model.LookupTable.LookupEntry;
 import net.rptools.parser.ParserException;
 
 /**
  * This object contains {@link Zone}s and {@link Asset}s that make up a campaign.
  * Roughly this is equivalent to multiple tabs that will appear on the client and
  * all of the images that will appear on it (and also campaign macro buttons)
  */
 public class Campaign {
 	
 	public static final String DEFAULT_TOKEN_PROPERTY_TYPE = "Basic";
 	
     private GUID id = new GUID();
     private Map<GUID, Zone> zones = Collections.synchronizedMap(new LinkedHashMap<GUID, Zone>());
     private ExportInfo exportInfo;
     
     private CampaignProperties campaignProperties = new CampaignProperties();
     private transient boolean isBeingSerialized;
 	
 	// campaign macro button properties. these are saved along with the campaign.
 	// as of 1.3b32
 	private List<MacroButtonProperties> macroButtonProperties = new ArrayList<MacroButtonProperties>();
 	// need to have a counter for additions to macroButtonProperties array
 	// otherwise deletions/insertions from/to that array will go out of sync 
 	private int macroButtonLastIndex = 0;
 	
 	// DEPRECATED: As of 1.3b20 these are now in campaignProperties, but are here for backward compatibility
     private Map<String, List<TokenProperty>> tokenTypeMap;
     private List<String> remoteRepositoryList;
 
     private Map<String, Map<GUID, LightSource>> lightSourcesMap;
     
     private Map<String, LookupTable> lookupTableMap;
     
     // DEPRECATED: as of 1.3b19 here to support old serialized versions
     private Map<GUID, LightSource> lightSourceMap;
     
     public Campaign() {
     	macroButtonLastIndex = 0;
 		macroButtonProperties = new ArrayList<MacroButtonProperties>();
 	}
 
     private void checkCampaignPropertyConversion() {
     	
     	if (campaignProperties == null) {
     		campaignProperties = new CampaignProperties();
     	}
     	
     	if (tokenTypeMap != null) {
     		campaignProperties.setTokenTypeMap(tokenTypeMap);
     		tokenTypeMap = null;
     	}
     	if (remoteRepositoryList != null) {
     		campaignProperties.setRemoteRepositoryList(remoteRepositoryList);
     		remoteRepositoryList = null;
     	}
     	if (lightSourcesMap != null) {
     		campaignProperties.setLightSourcesMap(lightSourcesMap);
     		lightSourcesMap = null;
     	}
     	if (lookupTableMap != null) {
     		campaignProperties.setLookupTableMap(lookupTableMap);
     		lookupTableMap = null;
     	}
     }
     
     public List<String> getRemoteRepositoryList() {
     	checkCampaignPropertyConversion(); // TODO: Remove, for compatibility 1.3b19-1.3b20
     	return campaignProperties.getRemoteRepositoryList();
     }
     
     public Campaign (Campaign campaign) {
 
     	zones = Collections.synchronizedMap(new LinkedHashMap<GUID, Zone>());
     	for (Entry<GUID, Zone> entry : campaign.zones.entrySet()) {
     		Zone copy = new Zone(entry.getValue());
     		zones.put(copy.getId(), copy);
     	}
     	campaignProperties = new CampaignProperties(campaign.campaignProperties);
     	macroButtonProperties = new ArrayList<MacroButtonProperties>(campaign.getMacroButtonPropertiesArray());
     }
     
     public GUID getId() {
         return id;
     }
 
     /**
      * This is a workaround to avoid the renderer and the serializer interating on the drawables at the same time
      */
     public boolean isBeingSerialized() {
 		return isBeingSerialized;
 	}
 
 
 
     /**
      * This is a workaround to avoid the renderer and the serializer interating on the drawables at the same time
      */
 	public void setBeingSerialized(boolean isBeingSerialized) {
 		this.isBeingSerialized = isBeingSerialized;
 	}
 
 
 
 	public List<String> getTokenTypes() {
     	List<String> list = new ArrayList<String>(getTokenTypeMap().keySet());
     	Collections.sort(list);
     	return list;
     }
 
 	public List<String> getSightTypes() {
     	List<String> list = new ArrayList<String>(getSightTypeMap().keySet());
     	Collections.sort(list);
     	return list;
     }
 
 	public void setSightTypes(List<SightType> typeList) {
 		checkCampaignPropertyConversion();
 		Map<String, SightType> map = new HashMap<String, SightType>();
 		for (SightType sightType : typeList) {
 			map.put(sightType.getName(), sightType);
 		}
 		campaignProperties.setSightTypeMap(map);
 	}
 
     public List<TokenProperty> getTokenPropertyList(String tokenType) {
     	return getTokenTypeMap().containsKey(tokenType)? getTokenTypeMap().get(tokenType): new ArrayList<TokenProperty>();
     }
     
     public void putTokenType(String name, List<TokenProperty> propertyList) {
     	getTokenTypeMap().put(name, propertyList);
     }
     
     public Map<String, List<TokenProperty>> getTokenTypeMap() {
     	checkCampaignPropertyConversion(); // TODO: Remove, for compatibility 1.3b19-1.3b20
     	return campaignProperties.getTokenTypeMap();
     }
 
     public SightType getSightType(String type) {
     	return getSightTypeMap().get(type != null ? type : campaignProperties.getDefaultSightType());
     }
     
     public Map<String, SightType> getSightTypeMap() {
     	checkCampaignPropertyConversion();
     	return campaignProperties.getSightTypeMap();
     }
     
     public Map<String, LookupTable> getLookupTableMap() {
     	checkCampaignPropertyConversion(); // TODO: Remove, for compatibility 1.3b19-1.3b20
     	return campaignProperties.getLookupTableMap();
     }
     
     public LightSource getLightSource(GUID lightSourceId) {
 
     	for (Map<GUID, LightSource> map : getLightSourcesMap().values()) {
     		if (map.containsKey(lightSourceId)) {
     			return map.get(lightSourceId);
     		}
     	}
     	return null;
     }
 
     public Map<String, Map<GUID, LightSource>> getLightSourcesMap() {
     	checkCampaignPropertyConversion(); // TODO: Remove, for compatibility 1.3b19-1.3b20
     	return campaignProperties.getLightSourcesMap();
     }
     
     public Map<GUID, LightSource> getLightSourceMap(String type) {
     	return getLightSourcesMap().get(type);
     }
     
     public Map<String, BooleanTokenOverlay> getTokenStatesMap() {
         return campaignProperties.getTokenStatesMap();
     }
     
     public Map<String, BarTokenOverlay> getTokenBarsMap() {
         return campaignProperties.getTokenBarsMap();
     }
     
     public void setExportInfo(ExportInfo exportInfo) {
     	this.exportInfo = exportInfo;
     }
     
     public ExportInfo getExportInfo() {
     	return exportInfo;
     }
     
     public void setId(GUID id) {
         this.id = id;
     }
 
     public List<Zone> getZones() {
         return new ArrayList<Zone>(zones.values());
     }
 
     public Zone getZone(GUID id) {
         return zones.get(id);
     }
 
     public void putZone(Zone zone) {
         zones.put(zone.getId(), zone);
     }
 
     public void removeAllZones() {
     	zones.clear();
     }
     
     public void removeZone(GUID id) {
         zones.remove(id);
     }
 
     public boolean containsAsset(Asset asset) {
     	return containsAsset(asset.getId());
     }
     
     public boolean containsAsset(MD5Key key) {
     	
     	for (Zone zone : zones.values()) {
 
     		Set<MD5Key> assetSet = zone.getAllAssetIds();
     		if (assetSet.contains(key)) {
     			return true;
     		}
     	}
     	
     	return false;
     }
 
     public void mergeCampaignProperties(CampaignProperties properties) {
     	properties.mergeInto(campaignProperties);
     }
     
     public void replaceCampaignProperties(CampaignProperties properties) {
     	campaignProperties = new CampaignProperties(properties);
     }
     
     /**
      * Get a copy of the properties.  This is for persistence.  Modification of the properties
      * do not affect this campaign
      */
     public CampaignProperties getCampaignProperties() {
     	return new CampaignProperties(campaignProperties);
     }
 	
 	public List<MacroButtonProperties> getMacroButtonPropertiesArray() {
 		if (macroButtonProperties == null) {
 			// macroButtonProperties is null if you are loading an old campaign file < 1.3b32
 			macroButtonProperties = new ArrayList<MacroButtonProperties>();
 		}
 		return macroButtonProperties;
 	}
 
 	public void saveMacroButtonProperty(MacroButtonProperties properties) {
 		// find the matching property in the array
 		//TODO: hashmap? or equals()? or what?
 		if (!MapTool.getPlayer().isGM()) {
 			MapTool.showError("Only the GM is allowed to make changes to the Campaign Panel.");
 			return;
 		}
 		if(MapTool.isHostingServer() && AppPreferences.getShowMacroUpdateWarning()) {
 			Boolean hideMacroUpdateWarning = MapTool.confirm("Changes to the Campaign Panel will not be sent to other clients until you save and reload this campiagn.\n\n If you would like to hide this message from now on, please select 'Yes', otherwise, please select 'No'.");
 			if(hideMacroUpdateWarning) {
 				AppPreferences.setShowMacroUpdateWarning(false);
 			} else {
 				AppPreferences.setShowMacroUpdateWarning(true);
 			}
 		}
 		for (MacroButtonProperties prop : macroButtonProperties) {
 			if (prop.getIndex() == properties.getIndex()) {
 				prop.setColorKey(properties.getColorKey());
 				prop.setAutoExecute(properties.getAutoExecute());
 				prop.setCommand(properties.getCommand());
 				prop.setHotKey(properties.getHotKey());
 				prop.setIncludeLabel(properties.getIncludeLabel());
 				prop.setApplyToTokens(properties.getApplyToTokens());
 				prop.setLabel(properties.getLabel());
 				prop.setGroup(properties.getGroup());
 				prop.setSortby(properties.getSortby());
 				prop.setFontColorKey(properties.getFontColorKey());
 				prop.setFontSize(properties.getFontSize());
 				prop.setMinWidth(properties.getMinWidth());
 				prop.setMaxWidth(properties.getMaxWidth());
 				MapTool.getFrame().getCampaignPanel().reset();
 				return;
 			}
 		}
 		macroButtonProperties.add(properties);
 		MapTool.getFrame().getCampaignPanel().reset();
 	}
 	
 	public int getMacroButtonNextIndex() {
		for (MacroButtonProperties prop : macroButtonProperties) {
			if (prop.getIndex() > macroButtonLastIndex) {
				macroButtonLastIndex = prop.getIndex();
			}
		}
 		return ++macroButtonLastIndex;
 	}
 	
 	public void deleteMacroButton(MacroButtonProperties properties)	{
 		if (!MapTool.getPlayer().isGM()) {
 			MapTool.showError("Only the GM is allowed to make changes to the Campaign Panel.");
 			return;
 		}
 		MapTool.showInformation("Changes to the Campaign Panel will not be sent to other clients until you save and reload this campiagn.");
 		macroButtonProperties.remove(properties);
 		MapTool.getFrame().getCampaignPanel().reset();
 	}
 
 	public Set<MD5Key> getAllAssetIds() {
 		
 		// Maps (tokens are implicit)
 		Set<MD5Key> assetSet = new HashSet<MD5Key>();
 		for (Zone zone : getZones()) {
 			assetSet.addAll(zone.getAllAssetIds());
 		}
 		
 		// States
 		for (BooleanTokenOverlay overlay : getCampaignProperties().getTokenStatesMap().values()) {
 			if (overlay instanceof ImageTokenOverlay) {
 				assetSet.add(((ImageTokenOverlay)overlay).getAssetId());
 			}
 		}
 
 		// Bars
 		for (BarTokenOverlay overlay : getCampaignProperties().getTokenBarsMap().values()) {
 		    if (overlay instanceof SingleImageBarTokenOverlay) {
                 assetSet.add(((SingleImageBarTokenOverlay)overlay).getAssetId());
 		    } else if (overlay instanceof TwoImageBarTokenOverlay) {
                 assetSet.add(((TwoImageBarTokenOverlay)overlay).getTopAssetId());
                 assetSet.add(((TwoImageBarTokenOverlay)overlay).getBottomAssetId());
             } else if (overlay instanceof MultipleImageBarTokenOverlay) {
                 assetSet.addAll(Arrays.asList(((MultipleImageBarTokenOverlay)overlay).getAssetIds()));
 		    } // endif
 		}
 				
 		// Tables
 		for (LookupTable table : getCampaignProperties().getLookupTableMap().values()) {
 			assetSet.addAll(table.getAllAssetIds());
 		}
 		
 		return assetSet;
 	}
 	
     /** @return Getter for initiativeOwnerPermissions */
     public boolean isInitiativeOwnerPermissions() {
         return campaignProperties.isInitiativeOwnerPermissions();
     }
 
     /** @param initiativeOwnerPermissions Setter for initiativeOwnerPermissions */
     public void setInitiativeOwnerPermissions(boolean initiativeOwnerPermissions) {
         campaignProperties.setInitiativeOwnerPermissions(initiativeOwnerPermissions);
     }
 
     /** @return Getter for characterSheets */
     public Map<String, String> getCharacterSheets() {
         return getCampaignProperties().getCharacterSheets();
     }
 }
