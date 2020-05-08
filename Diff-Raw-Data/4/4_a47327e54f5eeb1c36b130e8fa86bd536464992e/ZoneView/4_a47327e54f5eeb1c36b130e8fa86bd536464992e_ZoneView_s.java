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
 package net.rptools.maptool.client.ui.zone;
 
 import java.awt.Point;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.Area;
 import java.awt.geom.Ellipse2D;
 import java.awt.geom.Rectangle2D;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import net.rptools.maptool.client.AppUtil;
 import net.rptools.maptool.client.MapTool;
 import net.rptools.maptool.client.ui.zone.vbl.AreaTree;
 import net.rptools.maptool.model.AttachedLightSource;
 import net.rptools.maptool.model.Direction;
 import net.rptools.maptool.model.GUID;
 import net.rptools.maptool.model.Light;
 import net.rptools.maptool.model.LightSource;
 import net.rptools.maptool.model.ModelChangeEvent;
 import net.rptools.maptool.model.ModelChangeListener;
 import net.rptools.maptool.model.SightType;
 import net.rptools.maptool.model.Token;
 import net.rptools.maptool.model.Zone;
 
 public class ZoneView implements ModelChangeListener {
 
 	private Zone zone;
 
 	// VISION
 	private Map<GUID, Area> tokenVisibleAreaCache = new HashMap<GUID, Area>();
     private Map<GUID, Area> tokenVisionCache = new HashMap<GUID, Area>();
     private Map<GUID, Map<String, Area>> lightSourceCache = new HashMap<GUID, Map<String, Area>>();
     private Map<LightSource.Type, Set<GUID>> lightSourceMap = new HashMap<LightSource.Type, Set<GUID>>();
     private Map<GUID, Map<String,Set<DrawableLight>>> drawableLightCache = new HashMap<GUID, Map<String, Set<DrawableLight>>>();
     private Map<GUID, Map<String, Set<Area>>> brightLightCache = new HashMap<GUID, Map<String, Set<Area>>>();
     private Map<PlayerView, VisibleAreaMeta> visibleAreaMap = new HashMap<PlayerView, VisibleAreaMeta>();
     private AreaData topologyAreaData;
     private AreaTree topology;
     
 	public ZoneView(Zone zone) {
 		
 		this.zone = zone;
 		
 		findLightSources();
 		
 		zone.addModelChangeListener(this);
 	}
 
 	public Area getVisibleArea(PlayerView view) {
 		calculateVisibleArea(view);
 		return visibleAreaMap.get(view).visibleArea;
 	}
 	
 	public boolean isUsingVision() {
 		return zone.getVisionType() != Zone.VisionType.OFF;
 	}
 	
 	public AreaTree getTopology() {
 		if (topology == null) {
 			topology = new AreaTree(zone.getTopology());
 		}
 		return topology;
 	}
 
     public AreaData getTopologyAreaData() {
     	if (topologyAreaData == null) {
     		topologyAreaData = new AreaData(zone.getTopology());
     		topologyAreaData.digest();
     	}
     	return topologyAreaData;
     }
     
     public Area getLightSourceArea(Token token, Token lightSourceToken) {
     	
     	// Cached ?
     	Map<String, Area> areaBySightMap = lightSourceCache.get(lightSourceToken.getId());
     	if (areaBySightMap != null) {
 
     		Area lightSourceArea = areaBySightMap.get(token.getSightType());
     		if (lightSourceArea != null) {
     			return lightSourceArea;
     		}
     	} else {
     		areaBySightMap = new HashMap<String, Area>();
     		lightSourceCache.put(lightSourceToken.getId(), areaBySightMap);
     	}
     	
     	// Calculate
 		Area area = new Area();
 		for (AttachedLightSource attachedLightSource : lightSourceToken.getLightSources()) {
 			
 			LightSource lightSource = MapTool.getCampaign().getLightSource(attachedLightSource.getLightSourceId());
 			if (lightSource == null) {
 				continue;
 			}
 
 	        SightType sight = MapTool.getCampaign().getSightType(token.getSightType());
 			Area visibleArea = calculateLightSourceArea(lightSource, lightSourceToken, sight, attachedLightSource.getDirection());
 
 			// I don't like the NORMAL check here, it doesn't feel right, the API needs to change to support
 			// getting arbitrary light source types, but that's not a simple change
 			if (visibleArea != null && lightSource.getType() == LightSource.Type.NORMAL) {
 				area.add(visibleArea);
 			}
 		}
     	
 		// Cache
 		areaBySightMap.put(token.getSightType(), area);
 		
 		return area;
     }
 	
     private Area calculateLightSourceArea(LightSource lightSource, Token lightSourceToken, SightType sight, Direction direction) {
     	
     	if (sight == null) {
     		return null;
     	}
     	
         Point p = FogUtil.calculateVisionCenter(lightSourceToken, zone);
         Area lightSourceArea = lightSource.getArea(lightSourceToken, zone, direction);
         
     	// Calculate exposed area
         // TODO: This won't work with directed light, need to add an anchor or something
         if (sight.getMultiplier() != 1) {
         	lightSourceArea.transform(AffineTransform.getScaleInstance(sight.getMultiplier(), sight.getMultiplier()));
         }
 
 		Area visibleArea = FogUtil.calculateVisibility(p.x, p.y, lightSourceArea, getTopology());
 
 		if (visibleArea == null) {
 			return null;
 		}
 
 		// Keep track of colored light
         Set<DrawableLight> lightSet = new HashSet<DrawableLight>();
         Set<Area> brightLightSet = new HashSet<Area>();
         for (Light light : lightSource.getLightList()) {
         	
         	Area lightArea = lightSource.getArea(lightSourceToken, zone, direction, light);
             if (sight.getMultiplier() != 1) {
             	lightArea.transform(AffineTransform.getScaleInstance(sight.getMultiplier(), sight.getMultiplier()));
             }
 
             lightArea.transform(AffineTransform.getTranslateInstance(p.x, p.y));
             lightArea.intersect(visibleArea);
 
             if (light.getPaint() != null) {
             	lightSet.add(new DrawableLight(lightSource.getType(), light.getPaint(), lightArea));
             } else {
             	brightLightSet.add(lightArea);
             }
         }
 
         Map<String, Set<DrawableLight>> lightMap = drawableLightCache.get(lightSourceToken.getId());
         if (lightMap == null) {
         	lightMap = new HashMap<String, Set<DrawableLight>>();
         	drawableLightCache.put(lightSourceToken.getId(), lightMap);
         }
         lightMap.put(sight.getName(), lightSet);
 	        
         Map<String, Set<Area>> brightLightMap = brightLightCache.get(lightSourceToken.getId());
         if (brightLightMap == null) {
         	brightLightMap = new HashMap<String, Set<Area>>();
         	brightLightCache.put(lightSourceToken.getId(), brightLightMap);
         }
         brightLightMap.put(sight.getName(), brightLightSet);
         
 		return visibleArea;
     }
     
 	public Area getVisibleArea(Token token) {
 
 		// Sanity
 		if (token == null || !token.getHasSight()) {
 			return null;
 		}
 		// Cache ?
 		Area tokenVisibleArea = tokenVisionCache.get(token.getId());
 		if (tokenVisibleArea != null) {
 			return tokenVisibleArea;
 		}
 
 		// Combine the player visible area with the available light sources
 		tokenVisibleArea = tokenVisibleAreaCache.get(token.getId());
 		if (tokenVisibleArea == null) {
 			
 	        Point p = FogUtil.calculateVisionCenter(token, zone);
 	        int visionDistance = zone.getTokenVisionInPixels();
 	        Area visibleArea = new Area(new Ellipse2D.Double(-visionDistance, -visionDistance, visionDistance*2, visionDistance*2));
 
 	        tokenVisibleArea = FogUtil.calculateVisibility(p.x, p.y, visibleArea, getTopology());
 			
 			tokenVisibleAreaCache.put(token.getId(), tokenVisibleArea);
 		}
 
         // Combine in the visible light areas
         if (tokenVisibleArea != null && zone.getVisionType() == Zone.VisionType.NIGHT) {
         
         	Rectangle2D origBounds = tokenVisibleArea.getBounds();
         	
     		// Combine all light sources that might intersect our vision
         	List<Area> intersects = new LinkedList<Area>();
         	List<Token> lightSourceTokens = new ArrayList<Token>();
         	
         	if (lightSourceMap.get(LightSource.Type.NORMAL) != null) {
 	    		for (GUID lightSourceTokenId : lightSourceMap.get(LightSource.Type.NORMAL)) {
 	    			
 	    			Token lightSourceToken = zone.getToken(lightSourceTokenId);
 	    			if (lightSourceToken != null) {
 	    				lightSourceTokens.add(lightSourceToken);
 	    			}
 	    		}
         	}
         	if (token.hasLightSources() && !lightSourceTokens.contains(token)) {
         		// This accounts for temporary tokens (such as during an Expose Last Path)
         		lightSourceTokens.add(token);
         	}
     		for (Token lightSourceToken : lightSourceTokens) {
     			
     			Area lightArea = getLightSourceArea(token, lightSourceToken);
 
     			if (origBounds.intersects(lightArea.getBounds2D())) {
             		Area intersection = new Area(tokenVisibleArea);
             		intersection.intersect(lightArea);
                 	intersects.add(intersection);
             	}
     		}
         	
             // Check for personal vision
             SightType sight = MapTool.getCampaign().getSightType(token.getSightType());
             if (sight != null && sight.hasPersonalLightSource()) {
     			Area lightArea = calculateLightSourceArea(sight.getPersonalLightSource(), token, sight, Direction.CENTER);
     			if (lightArea != null) {
             		Area intersection = new Area(tokenVisibleArea);
             		intersection.intersect(lightArea);
                 	intersects.add(intersection);
     			}
             }
 
 			while (intersects.size() > 1) {
 				
 				Area a1 = intersects.remove(0);
 				Area a2 = intersects.remove(0);
 				
 				a1.add(a2);
 				intersects.add(a1);
 			}
 
             tokenVisibleArea = intersects.size() > 0 ? intersects.get(0) : new Area();
         }
         
         tokenVisionCache.put(token.getId(), tokenVisibleArea);
 		
 		return tokenVisibleArea;
 	}
 
 	public List<DrawableLight> getLights(LightSource.Type type) {
 		
 		List<DrawableLight> lightList = new LinkedList<DrawableLight>();
 		if (lightSourceMap.get(type) != null) {
 
 			for (GUID lightSourceToken : lightSourceMap.get(type)) {
 				Token token = zone.getToken(lightSourceToken);
 				if (token == null) {
 					continue;
 				}
 				
 		        Point p = FogUtil.calculateVisionCenter(token, zone);
 
 		        for (AttachedLightSource als : token.getLightSources()) {
 					LightSource lightSource = MapTool.getCampaign().getLightSource(als.getLightSourceId());
 					if (lightSource == null) {
 						continue;
 					}
 
 					if (lightSource.getType() == type) {
 						// This needs to be cached somehow
 				        Area lightSourceArea = lightSource.getArea(token, zone, Direction.CENTER);
 						Area visibleArea = FogUtil.calculateVisibility(p.x, p.y, lightSourceArea, getTopology());
 
 						for (Light light : lightSource.getLightList()) {
 							lightList.add(new DrawableLight(type, light.getPaint(), visibleArea));
 						}
 					}
 				}
 			}
 		}
 		
 		return lightList;
 	}
 	
 	private void findLightSources() {
 		
 		lightSourceMap.clear();
 		
 		for (Token token : zone.getAllTokens()) {
 			if (token.hasLightSources() && token.isVisible()) {
 				for (AttachedLightSource als : token.getLightSources()) {
 					
 					LightSource lightSource = MapTool.getCampaign().getLightSource(als.getLightSourceId());
 					if (lightSource == null) {
 						continue;
 					}
 					
 					Set<GUID> lightSet = lightSourceMap.get(lightSource.getType());
 					if (lightSet == null) {
 						lightSet = new HashSet<GUID>();
 						lightSourceMap.put(lightSource.getType(), lightSet);
 					}
 					lightSet.add(token.getId());
 				}
 			}
 		}
 	}
 	
 	public Set<DrawableLight> getDrawableLights() {
 		Set<DrawableLight> lightSet = new HashSet<DrawableLight>();
 
 		for (Map<String, Set<DrawableLight>> map : drawableLightCache.values()) {
 			for (Set<DrawableLight> set : map.values()) {
 				lightSet.addAll(set);
 			}
 		}
 		
 		return lightSet;
 	}
 	
 	public Set<Area> getBrightLights() {
 		Set<Area> lightSet = new HashSet<Area>();
 
 		for (Map<String, Set<Area>> map : brightLightCache.values()) {
 			for (Set<Area> set : map.values()) {
 				lightSet.addAll(set);
 			}
 		}
 		
 		return lightSet;
 	}
 	
 	public void flush() {
 		tokenVisibleAreaCache.clear();
 		tokenVisionCache.clear();
 		lightSourceCache.clear();
 		visibleAreaMap.clear();
 		drawableLightCache.clear();
 		brightLightCache.clear();
 	}
 	
     public void flush(Token token) {
     	boolean hadLightSource = lightSourceCache.get(token.getId()) != null;
     	
         tokenVisionCache.remove(token.getId());
         tokenVisibleAreaCache.remove(token.getId());
         lightSourceCache.remove(token.getId());
         drawableLightCache.remove(token.getId());
         brightLightCache.remove(token.getId());
         visibleAreaMap.clear();
         
         if (hadLightSource || token.hasLightSources()) {
         	// Have to recalculate all token vision
         	tokenVisionCache.clear();
         }
         if (token.getHasSight()) {
             visibleAreaMap.clear();
         }
         
 //        // TODO: This fixes a bug with changing vision type, I don't like it though, it needs to be optimized back out
 //        lightSourceCache.clear();
     }
 
     private void calculateVisibleArea(PlayerView view) {
 
     	if (visibleAreaMap.get(view) != null) {
     		return;
     	}
 
     	// Cache it
     	VisibleAreaMeta meta = new VisibleAreaMeta();
     	meta.visibleArea = new Area();
 
     	visibleAreaMap.put(view, meta);
     	
     	// Calculate it
         for (Token token : zone.getAllTokens()) {
 
             if (!token.getHasSight ()) {
             	continue;
             }
                 
             // Don't bother if it's not visible
             if (!view.isGMView() && !token.isVisible()) {
         		continue;
             }
 
             // Permission
             if (MapTool.getServerPolicy().isUseIndividualViews()) {
             	if (!AppUtil.playerOwns(token)) {
             		continue;
             	}
             } else {
             	// Party members only, unless you are the GM
             	if (token.getType() != Token.Type.PC && !view.isGMView()) {
             		continue;
             	}
             }
 
             Area tokenVision = getVisibleArea(token);	                
             if (tokenVision != null) {
 
                 meta.visibleArea.add(tokenVision);
             }
         }
     }
     
     ////
 	// MODEL CHANGE LISTENER
 	public void modelChanged(ModelChangeEvent event) {
 		
 		Object evt = event.getEvent();
 		if (event.getModel() instanceof Zone) {
 			
             if (evt == Zone.Event.TOPOLOGY_CHANGED) {
                 tokenVisionCache.clear();
                 lightSourceCache.clear();
                 visibleAreaMap.clear();
                 topologyAreaData = null;
                 topology = null;
                 tokenVisibleAreaCache.clear();
             }
             if (evt == Zone.Event.TOKEN_CHANGED || evt == Zone.Event.TOKEN_REMOVED) {
             	flush((Token)event.getArg());
             }
             if (evt == Zone.Event.TOKEN_ADDED || evt == Zone.Event.TOKEN_CHANGED) {
             	Token token = (Token) event.getArg();
             	if (token.hasLightSources() && token.isVisible()) {
             		for(AttachedLightSource als : token.getLightSources()) {
             			LightSource lightSource = MapTool.getCampaign().getLightSource(als.getLightSourceId());
             			if (lightSource == null) {
             				continue;
             			}
             			Set<GUID> lightSet = lightSourceMap.get(lightSource.getType());
             			if (lightSet == null) {
             				lightSet = new HashSet<GUID>();
             				lightSourceMap.put(lightSource.getType(), lightSet);
             			}
             			lightSet.add(token.getId());
             		}
             	} else {
             		for(AttachedLightSource als : token.getLightSources()) {
             			LightSource lightSource = MapTool.getCampaign().getLightSource(als.getLightSourceId());
             			if (lightSource == null) {
             				continue;
             			}
             			Set<GUID> lightSet = lightSourceMap.get(lightSource.getType());
             			if (lightSet != null) {
             				lightSet.remove(token.getId());
             			}
             		}
             	}
             	
             	if (token.getHasSight()) {
                     visibleAreaMap.clear();
             	}
             }
             if (evt == Zone.Event.TOKEN_REMOVED) {
             	Token token = (Token) event.getArg();
         		for(AttachedLightSource als : token.getLightSources()) {
         			LightSource lightSource = MapTool.getCampaign().getLightSource(als.getLightSourceId());
         			if (lightSource == null) {
         				continue;
         			}
         			Set<GUID> lightSet = lightSourceMap.get(lightSource.getType());
         			if (lightSet != null) {
         				lightSet.remove(token.getId());
         			}
         		}
             }
             
 		}
 	}
 	
 	private static class VisibleAreaMeta {
 		
 		Area visibleArea;
 		
 	}
 	
 }
