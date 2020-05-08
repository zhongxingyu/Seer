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
 package net.rptools.maptool.client;
 
 import java.awt.EventQueue;
 import java.awt.geom.Area;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Set;
 
import antlr.collections.List;

 import net.rptools.clientserver.hessian.AbstractMethodHandler;
 import net.rptools.maptool.client.ui.zone.ZoneRenderer;
 import net.rptools.maptool.client.ui.zone.ZoneRendererFactory;
 import net.rptools.maptool.model.Asset;
 import net.rptools.maptool.model.AssetManager;
 import net.rptools.maptool.model.Campaign;
 import net.rptools.maptool.model.CampaignProperties;
 import net.rptools.maptool.model.GUID;
 import net.rptools.maptool.model.InitiativeList;
 import net.rptools.maptool.model.Label;
 import net.rptools.maptool.model.MacroButtonProperties;
 import net.rptools.maptool.model.Player;
 import net.rptools.maptool.model.Pointer;
 import net.rptools.maptool.model.TextMessage;
 import net.rptools.maptool.model.Token;
 import net.rptools.maptool.model.Zone;
 import net.rptools.maptool.model.ZonePoint;
 import net.rptools.maptool.model.Zone.VisionType;
 import net.rptools.maptool.model.drawing.Drawable;
 import net.rptools.maptool.model.drawing.DrawnElement;
 import net.rptools.maptool.model.drawing.Pen;
 import net.rptools.maptool.server.ServerPolicy;
 import net.rptools.maptool.transfer.AssetChunk;
 import net.rptools.maptool.transfer.AssetConsumer;
 import net.rptools.maptool.transfer.AssetHeader;
 
 
 /**
  * @author drice
  *
  * TODO To change the template for this generated type comment go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 public class ClientMethodHandler extends AbstractMethodHandler {
     
     public ClientMethodHandler() {
     }
 
     public void handleMethod(final String id, final String method, final Object[] parameters) {
         final ClientCommand.COMMAND cmd = Enum.valueOf(ClientCommand.COMMAND.class, method);
         //System.out.println("ClientMethodHandler#handleMethod: " + cmd.name());
 
         EventQueue.invokeLater(new Runnable() {
         	public void run() {
                 GUID zoneGUID;
                 Zone zone;
                 
                 switch (cmd) {
                 case bootPlayer:
         			String playerName = (String) parameters[0];
         			if(MapTool.getPlayer().getName().equals(playerName)) {
         				ServerDisconnectHandler.disconnectExpected = true;
         				AppActions.disconnectFromServer();
         				MapTool.showInformation("You have been booted from the server.");		
         			}
         	
                 	break;
                 case enforceZone:
                 	
                 	zoneGUID = (GUID) parameters[0];
                 	ZoneRenderer renderer = MapTool.getFrame().getZoneRenderer(zoneGUID);
                 	
                 	if (renderer != null && renderer != MapTool.getFrame().getCurrentZoneRenderer() && (renderer.getZone().isVisible() || MapTool.getPlayer().isGM())) {
                     	MapTool.getFrame().setCurrentZoneRenderer(renderer);
                 	}
                 	
                 	break;
                 case clearAllDrawings:
                 	
                 	zoneGUID = (GUID) parameters[0];
                     zone = MapTool.getCampaign().getZone(zoneGUID);
                     zone.getDrawnElements().clear();
                     
                     MapTool.getFrame().refresh();
                 	break;
                 case setZoneHasFoW:
                 	
                 	zoneGUID = (GUID) parameters[0];
                 	boolean hasFog = (Boolean) parameters[1];
                 	
                     zone = MapTool.getCampaign().getZone(zoneGUID);
                     zone.setHasFog(hasFog);
                     
                     // In case we're looking at the zone
                     MapTool.getFrame().refresh();
                 	break;
                 	
                 case exposeFoW:
                 	
                 	zoneGUID = (GUID) parameters[0];
                     Area area = (Area) parameters[1];
 
                     zone = MapTool.getCampaign().getZone(zoneGUID);
                     zone.exposeArea(area);
 
                 	MapTool.getFrame().refresh();
                 	break;
                 	
                 case setFoW:
                 	
                 	zoneGUID = (GUID) parameters[0];
                     area = (Area) parameters[1];
 
                     zone = MapTool.getCampaign().getZone(zoneGUID);
                     zone.setFogArea(area);
 
                 	MapTool.getFrame().refresh();
                 	break;
                 	
                 case hideFoW:
                 	
                 	zoneGUID = (GUID) parameters[0];
                     area = (Area) parameters[1];
 
                     zone = MapTool.getCampaign().getZone(zoneGUID);
                     zone.hideArea(area);
 
                     MapTool.getFrame().refresh();
                 	break;
                 
                 case setCampaign:
                 	Campaign campaign = (Campaign) parameters[0];
                 	MapTool.setCampaign(campaign);
                 	
                 	// Hide the "Connecting" overlay
 					MapTool.getFrame().hideGlassPane();
                     break;
                     
                 case putZone:
                 	zone = (Zone) parameters[0];
                 	MapTool.getCampaign().putZone(zone);
                 	
                 	// TODO: combine this with MapTool.addZone()
                 	renderer = ZoneRendererFactory.newRenderer(zone);
                 	MapTool.getFrame().addZoneRenderer(renderer);
                 	if (MapTool.getFrame().getCurrentZoneRenderer() == null && zone.isVisible()) {
                 		MapTool.getFrame().setCurrentZoneRenderer(renderer);
                 	}
                 	
                     MapTool.getEventDispatcher().fireEvent(MapTool.ZoneEvent.Added, MapTool.getCampaign(), null, zone);
                     break;
                 case removeZone:
                 	zoneGUID = (GUID)parameters[0];
                 	MapTool.getCampaign().removeZone(zoneGUID);
                 	MapTool.getFrame().removeZoneRenderer(MapTool.getFrame().getZoneRenderer(zoneGUID));
                     break;
                 case putAsset:
                     AssetManager.putAsset((Asset) parameters[0]);
                     MapTool.getFrame().getCurrentZoneRenderer().flushDrawableRenderer();
                     MapTool.getFrame().refresh();
                     break;
                 case removeAsset:
                     break;
                 case putToken:
                 	zoneGUID = (GUID) parameters[0];
                 	zone = MapTool.getCampaign().getZone(zoneGUID);
                 	Token token = (Token) parameters[1];
                 	
                 	zone.putToken(token);
                 	
                 	MapTool.getFrame().refresh();
                     break;
                     
                 case putLabel:
                     zoneGUID = (GUID) parameters[0];
                     zone = MapTool.getCampaign().getZone(zoneGUID);
                     Label label = (Label) parameters[1];
                     
                     zone.putLabel(label);
                     
                     MapTool.getFrame().refresh();
                     break;
                 case removeToken:
                     zoneGUID = (GUID) parameters[0];
                     zone = MapTool.getCampaign().getZone(zoneGUID);
                     GUID tokenGUID = (GUID) parameters[1];
 
                     zone.removeToken(tokenGUID);
                     
                     MapTool.getFrame().refresh();
                     break;
                 case removeLabel:
                     zoneGUID = (GUID) parameters[0];
                     zone = MapTool.getCampaign().getZone(zoneGUID);
                     GUID labelGUID = (GUID) parameters[1];
 
                     zone.removeLabel(labelGUID);
                     
                     MapTool.getFrame().refresh();
                     break;
                 case enforceZoneView: 
 
                     zoneGUID = (GUID) parameters[0];
                     int x = (Integer)parameters[1];
                     int y = (Integer)parameters[2];
                     double scale = (Double)parameters[3];
                     
                     renderer = MapTool.getFrame().getZoneRenderer(zoneGUID);
                     if (renderer == null) {
                         return;
                     }
                     
                     renderer.setScale(scale);
                     renderer.centerOn(new ZonePoint(x, y));
 
                     break;
                 case draw:
                 	
                 	zoneGUID = (GUID) parameters[0];
                     Pen pen = (Pen) parameters[1];
                 	Drawable drawable = (Drawable) parameters[2];
 
                 	zone = MapTool.getCampaign().getZone(zoneGUID);
                 	
                 	zone.addDrawable(new DrawnElement(drawable, pen));
                 	
                 	MapTool.getFrame().refresh();
                     break;
                 
                 case undoDraw:
                   zoneGUID = (GUID) parameters[0];
                   GUID drawableId = (GUID)parameters[1];
                   zone = MapTool.getCampaign().getZone(zoneGUID);
                   if (zone == null) {
                 	  return;
                   }
                   
                   zone.removeDrawable(drawableId);
 
         		  if (MapTool.getFrame().getCurrentZoneRenderer().getZone().getId().equals(zoneGUID) && zoneGUID != null) {
         			  MapTool.getFrame().refresh();
         		  }
 
         		  break;
                   
                 case setZoneVisibility: 
 
                 	zoneGUID = (GUID) parameters[0];
                 	boolean visible = (Boolean) parameters[1];
                 	
                 	zone = MapTool.getCampaign().getZone(zoneGUID);
                 	zone.setVisible(visible);
                 	
                 	ZoneRenderer currentRenderer = MapTool.getFrame().getCurrentZoneRenderer();
                 	if (!visible && !MapTool.getPlayer().isGM() && currentRenderer != null && currentRenderer.getZone().getId().equals(zoneGUID)) {
                 		MapTool.getFrame().setCurrentZoneRenderer(null);
                 	}
                 	if (visible && currentRenderer == null) {
                 		currentRenderer = MapTool.getFrame().getZoneRenderer(zoneGUID);
                 		MapTool.getFrame().setCurrentZoneRenderer(currentRenderer);
                 	}
                 	
                 	MapTool.getFrame().getZoneMiniMapPanel().flush();
                 	MapTool.getFrame().refresh();
                 	break;
         		  
                 case setZoneGridSize:
                 	
                 	zoneGUID = (GUID) parameters[0];
                 	int xOffset = ((Integer) parameters[1]).intValue();
                 	int yOffset = ((Integer) parameters[2]).intValue();
                 	int size = ((Integer) parameters[3]).intValue();
                 	int color = ((Integer) parameters[4]).intValue();
                 	
                 	zone = MapTool.getCampaign().getZone(zoneGUID);
                 	zone.getGrid().setSize(size);
                 	zone.getGrid().setOffset(xOffset, yOffset);
                 	zone.setGridColor(color);
                 	
                 	MapTool.getFrame().refresh();
                 	break;
 
                 case playerConnected:
                 	
                 	MapTool.addPlayer((Player) parameters[0]);
                 	MapTool.getFrame().refresh();
                 	break;
 
                 case playerDisconnected:
                 	
                 	MapTool.removePlayer((Player) parameters[0]);
                 	MapTool.getFrame().refresh();
                 	break;
                     
                 case message:
                     TextMessage message = (TextMessage) parameters[0];
                     MapTool.addServerMessage(message);
                 	break;
                     
                 case showPointer:
                 	MapTool.getFrame().getPointerOverlay().addPointer((String) parameters[0], (Pointer) parameters[1]);
                 	MapTool.getFrame().refresh();
                 	break;
                 	
                 case hidePointer:
                 	MapTool.getFrame().getPointerOverlay().removePointer((String) parameters[0]);
                 	MapTool.getFrame().refresh();
                 	break;
                 	
                 case startTokenMove:
         			
         			String playerId = (String) parameters[0];
         			zoneGUID = (GUID) parameters[1];
         			GUID keyToken = (GUID) parameters[2];
         			Set<GUID> selectedSet = (Set<GUID>) parameters[3];
         			
         			renderer = MapTool.getFrame().getZoneRenderer(zoneGUID);
         			renderer.addMoveSelectionSet(playerId, keyToken, selectedSet, true);
         			
                 	break;
                 case stopTokenMove:
 
         			zoneGUID = (GUID) parameters[0];
         			keyToken = (GUID) parameters[1];
         			
         			renderer = MapTool.getFrame().getZoneRenderer(zoneGUID);
         			renderer.removeMoveSelectionSet(keyToken);
 
         			break;
                 case updateTokenMove:
 
         			zoneGUID = (GUID) parameters[0];
         			keyToken = (GUID) parameters[1];
         			
         			x = ((Integer) parameters[2]).intValue();
         			y = ((Integer) parameters[3]).intValue();
         			
         			renderer = MapTool.getFrame().getZoneRenderer(zoneGUID);
         			renderer.updateMoveSelectionSet(keyToken, new ZonePoint(x, y));
 
         			break;
         			
                 case toggleTokenMoveWaypoint:
 
         			zoneGUID = (GUID) parameters[0];
         			keyToken = (GUID) parameters[1];
         			ZonePoint zp = (ZonePoint) parameters[2];
         			
         			renderer = MapTool.getFrame().getZoneRenderer(zoneGUID);
         			renderer.toggleMoveSelectionSetWaypoint(keyToken, zp);
 
         			break;
 
                 case setServerPolicy:
                 	
                 	ServerPolicy policy = (ServerPolicy) parameters[0];
                 	MapTool.setServerPolicy(policy);
                 	break;
                 	
                 case addTopology:
                 	
                 	zoneGUID = (GUID) parameters[0];
                     area = (Area) parameters[1];
 
                     zone = MapTool.getCampaign().getZone(zoneGUID);
                     zone.addTopology(area);
 
                 	MapTool.getFrame().getZoneRenderer(zoneGUID).repaint();
                 	break;
                 	
                 case removeTopology:
                 	
                 	zoneGUID = (GUID) parameters[0];
                     area = (Area) parameters[1];
 
                     zone = MapTool.getCampaign().getZone(zoneGUID);
                     zone.removeTopology(area);
 
                     MapTool.getFrame().getZoneRenderer(zoneGUID).repaint();
                 	break;
                 
                 case startAssetTransfer:
                 	AssetHeader header = (AssetHeader) parameters[0];
                 	MapTool.getAssetTransferManager().addConsumer(new AssetConsumer(AppUtil.getTmpDir(), header));
                 	break;
                 	
                 case updateAssetTransfer:
                 	AssetChunk chunk = (AssetChunk) parameters[0];
                 	
                 	try {
                 		MapTool.getAssetTransferManager().update(chunk);
                 	} catch (IOException ioe) {
                 		// TODO: do something intelligent like clear the transfer manager, and clear the "we're waiting for" flag so that it gets requested again
                 		ioe.printStackTrace();
                 	}
                 	break;
                 case renameZone:
                 	
                 	zoneGUID = (GUID) parameters[0];
                 	String name = (String) parameters[1];
                 	
                 	zone = MapTool.getCampaign().getZone(zoneGUID);
                 	if (zone != null) {
                 		zone.setName(name);
                 	}
                 	
                 	break;
                 case updateCampaign:
                 	CampaignProperties properties = (CampaignProperties) parameters[0];
                 	
                 	MapTool.getCampaign().replaceCampaignProperties(properties);
                 	if (MapTool.getFrame().getCurrentZoneRenderer() != null) {
                 		MapTool.getFrame().getCurrentZoneRenderer().getZoneView().flush();
                 		MapTool.getFrame().getCurrentZoneRenderer().repaint();
                 	}
                 	
                 	AssetManager.updateRepositoryList();
                 	
                 	MapTool.getFrame().getInitiativePanel().setOwnerPermissions(properties.isInitiativeOwnerPermissions());
                 	MapTool.getFrame().getLookupTablePanel().updateView();
                 	break;
                 case movePointer:
                 	String player = (String)parameters[0];
                 	x = (Integer)parameters[1];
                 	y = (Integer)parameters[2];
                 	
                 	Pointer pointer = MapTool.getFrame().getPointerOverlay().getPointer(player);
                 	pointer.setX(x);
                 	pointer.setY(y);
                 	
                 	MapTool.getFrame().refresh();
                 	break;
                 	
                 case updateInitiative:
                     InitiativeList list = (InitiativeList)parameters[0];
                     Boolean ownerPermission = (Boolean)parameters[1];
                     if (list != null) {
                         zone = list.getZone();
                         if (zone == null) return;
                         zone.setInitiativeList(list);
                     } if (ownerPermission != null) {
                         MapTool.getFrame().getInitiativePanel().setOwnerPermissions(ownerPermission.booleanValue());
                     }
                     break;
                     
                 case setUseVision:
                 	zoneGUID = (GUID) parameters[0];
                 	VisionType visionType = (VisionType) parameters[1];
                 	
                 	zone = MapTool.getCampaign().getZone(zoneGUID);
                 	if (zone != null) {
                 		zone.setVisionType(visionType);
                 		if (MapTool.getFrame().getCurrentZoneRenderer() != null) {
                 			MapTool.getFrame().getCurrentZoneRenderer().flushFog();
                 			MapTool.getFrame().getCurrentZoneRenderer().getZoneView().flush();
                 		}
                 		MapTool.getFrame().refresh();
                 	}
                 	break;
                 
                 case updateCampaignMacros:
                 	MapTool.getCampaign().setMacroButtonPropertiesArray(new ArrayList<MacroButtonProperties>((ArrayList<MacroButtonProperties>) parameters[0]));
                 	MapTool.getFrame().getCampaignPanel().reset();
                 	break;
                 	
                 }
         	}
         });
     }
 
 }
