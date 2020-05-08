 /**
  * Project Wonderland
  *
  * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
  *
  * Redistributions in source code form must reproduce the above
  * copyright and this condition.
  *
  * The contents of this file are subject to the GNU General Public
  * License, Version 2 (the "License"); you may not use this file
  * except in compliance with the License. A copy of the License is
  * available at http://www.opensource.org/licenses/gpl-license.php.
  *
  * Sun designates this particular file as subject to the "Classpath" 
  * exception as provided by Sun in the License file that accompanied 
  * this code.
  */
 package org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer;
 
 import java.net.MalformedURLException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.jdesktop.wonderland.client.jme.cellrenderer.*;
 import com.jme.math.Quaternion;
 import com.jme.math.Vector3f;
 import com.jme.scene.Node;
 import imi.character.CharacterAttributes;
 import imi.character.CharacterMotionListener;
 import imi.character.avatar.Avatar;
 import imi.character.avatar.AvatarContext.TriggerNames;
 import imi.character.avatar.FemaleAvatarAttributes;
 import imi.character.avatar.MaleAvatarAttributes;
 import imi.character.statemachine.GameContextListener;
 import imi.scene.PMatrix;
 import imi.scene.processors.JSceneEventProcessor;
 import imi.utils.PMathUtils;
 import imi.utils.input.AvatarControlScheme;
 import java.io.File;
 import java.net.URL;
 import javax.swing.JFrame;
 import javax.swing.SwingUtilities;
 import org.jdesktop.mtgame.Entity;
 import org.jdesktop.mtgame.RenderComponent;
 import org.jdesktop.mtgame.WorldManager;
 import org.jdesktop.wonderland.client.cell.Cell;
 import org.jdesktop.wonderland.client.jme.ClientContextJME;
 import org.jdesktop.wonderland.common.ExperimentalAPI;
 import org.jdesktop.wonderland.common.cell.CellTransform;
 import org.jdesktop.wonderland.client.ClientContext;
 import org.jdesktop.wonderland.client.cell.MovableAvatarComponent;
 import org.jdesktop.wonderland.client.cell.MovableComponent;
 import org.jdesktop.wonderland.client.cell.view.AvatarCell;
 import org.jdesktop.wonderland.client.cell.view.ViewCell;
 import org.jdesktop.wonderland.client.comms.WonderlandSession;
 import org.jdesktop.wonderland.client.input.Event;
 import org.jdesktop.wonderland.client.input.EventClassListener;
 import org.jdesktop.wonderland.client.jme.AvatarControls.AvatarActionTrigger;
 import org.jdesktop.wonderland.client.jme.AvatarControls.AvatarInputSelector;
 import org.jdesktop.wonderland.client.login.LoginManager;
 import org.jdesktop.wonderland.client.login.ServerSessionManager;
 
 /**
  * Renderer for Avatars, using the new avatar system
  * 
  * @author paulby
  */
 @ExperimentalAPI
 public class AvatarImiJME extends BasicRenderer implements AvatarInputSelector, AvatarActionTrigger {
 
     private WlAvatarCharacter avatarCharacter=null;
     private boolean selectedForInput = false;
 
 //    private AvatarRendererChangeRequestEvent.AvatarQuality quality = AvatarRendererChangeRequestEvent.AvatarQuality.High;
 
     private CharacterMotionListener characterMotionListener;
     private GameContextListener gameContextListener;
 
     private int currentTrigger = -1;
     private boolean currentPressed = false;
 
     private float positionMinDistanceForPull    = 0.1f;
     private float positionMaxDistanceForPull    = 3.0f;
 
     public AvatarImiJME(Cell cell) {
         super(cell);
         assert(cell!=null);
         final Cell c = cell;
 
         characterMotionListener = new CharacterMotionListener() {
                 public void transformUpdate(Vector3f translation, PMatrix rotation) {
                    ((MovableAvatarComponent)c.getComponent(MovableComponent.class)).localMoveRequest(new CellTransform(rotation.getRotation(), translation));
                 }
             };
 
         // This info will be sent to the other clients to animate the avatar
         gameContextListener = new GameContextListener() {
                 public void trigger(boolean pressed, int trigger, Vector3f translation, Quaternion rotation) {
                     synchronized(this) {
                         currentTrigger = trigger;
                         currentPressed = pressed;
                     }
                    ((MovableAvatarComponent)c.getComponent(MovableComponent.class)).localMoveRequest(new CellTransform(rotation, translation), trigger, pressed, null);
                 }
 
             };
 
         ClientContext.getInputManager().addGlobalEventListener(new EventClassListener() {
             private Class[] consumeClasses = new Class[] { AvatarRendererChangeRequestEvent.class };
 
             @Override
             public Class[] eventClassesToConsume () {
                 return consumeClasses;
             }
 
             @Override
             public void computeEvent(Event evtIn) {
                 System.err.println("TODO - GOT EVENT "+evtIn);
             }
         });
     }
 
     @Override
     protected Entity createEntity() {
         avatarCharacter = (WlAvatarCharacter) createAvatarEntities(ClientContextJME.getWorldManager());
 
         RenderComponent rc = (RenderComponent) avatarCharacter.getComponent(RenderComponent.class);
         if (rc!=null)
             addDefaultComponents(avatarCharacter, rc.getSceneRoot());
         else
             logger.warning("NO RenderComponent for Avatar");
 
         // Remove the entity, it will be added when the cell status changes
         ClientContextJME.getWorldManager().removeEntity(avatarCharacter);
 
         return avatarCharacter;
     }
 
     void changeAvatar(WlAvatarCharacter newAvatar) {
         WorldManager wm = ClientContextJME.getWorldManager();
         wm.removeEntity(avatarCharacter);
 
         avatarCharacter = newAvatar;
 
         RenderComponent rc = (RenderComponent) avatarCharacter.getComponent(RenderComponent.class);
         if (rc!=null)
             addDefaultComponents(avatarCharacter, rc.getSceneRoot());
         else
             logger.warning("NO RenderComponent for Avatar");
 
         wm.addEntity(avatarCharacter);
         avatarCharacter.selectForInput();
     }
 
     @Override
     protected void addRenderState(Node node) {
         // Nothing to do
     }
 
     @Override
     public void cellTransformUpdate(CellTransform transform) {
         // Don't call super, we don't use a MoveProcessor for avatars
 
         if (!selectedForInput && avatarCharacter!=null) {
             Vector3f pos = transform.getTranslation(null);
             Vector3f dir = new Vector3f(0,0,-1);
             dir = transform.transform(dir);
             PMatrix local = avatarCharacter.getController().getModelInstance().getTransform().getLocalMatrix(true);
             Vector3f currentPosition = local.getTranslation();
             float currentDistance = currentPosition.distance(pos);
             if ( currentDistance < positionMaxDistanceForPull )
                 pos.set(currentPosition);
             PMatrix look = PMathUtils.lookAt(pos.add(dir), pos, Vector3f.UNIT_Y);
             avatarCharacter.getModelInst().getTransform().getLocalMatrix(true).set(look);
         }
 
     }
     
     protected Entity createAvatarEntities(WorldManager wm) {
         PMatrix origin = new PMatrix();
         CellTransform transform = cell.getLocalTransform();
         origin.setTranslation(transform.getTranslation(null));
         origin.setRotation(transform.getRotation(null));
 
         String name = ((ViewCell)cell).getIdentity().getUsername();
 
         CharacterAttributes attributes;
         if (name.contains("icole") || name.contains("iriam"))
             attributes = new FemaleAvatarAttributes(name, false);
         else
             attributes = new MaleAvatarAttributes(name, false);
 
         // Set the base URL
         WonderlandSession session = cell.getCellCache().getSession();
         ServerSessionManager manager = LoginManager.find(session);
         String serverHostAndPort = manager.getServerNameAndPort();
         String baseURL = "wla://avatarbase@"+serverHostAndPort+"/";
         attributes.setBaseURL(baseURL);
 
         URL avatarConfigURL = ((AvatarCell)cell).getAvatarConfigURL();
//        System.err.println("AvatarConfigURL "+avatarConfigURL);
 
         // Force to null for testing
 //        avatarConfigURL = null;
 
         // Create the character, but don't add the entity to wm
         // TODO this will change to take the config
         if (avatarConfigURL==null) {
             File defaultConfig = AvatarConfigFrame.getDefaultAvatarConfigFile();
             if (defaultConfig.exists()) {
                 try {
                     avatarCharacter = new WlAvatarCharacter(defaultConfig.toURI().toURL(), ClientContextJME.getWorldManager(), baseURL);
                 } catch (MalformedURLException ex) {
                     avatarCharacter = null;
                     Logger.getLogger(AvatarImiJME.class.getName()).log(Level.SEVERE, "Error loading default config file"+defaultConfig, ex);
                 }
             }
 
             if (avatarCharacter == null) {
                 avatarCharacter = new WlAvatarCharacter(attributes, wm);
                 SwingUtilities.invokeLater(new Runnable() {
 
                     public void run() {
                         JFrame f = new AvatarConfigFrame(AvatarImiJME.this);
                         f.setVisible(true);
                     }
                 });
             }
         } else {
             avatarCharacter = new WlAvatarCharacter(avatarConfigURL, wm, "wla://avatarbase@"+serverHostAndPort+"/");
         }
         avatarCharacter.getModelInst().getTransform().getLocalMatrix(true).set(origin);
 
 //        JScene jscene = avatar.getJScene();
 //        jscene.renderToggle();      // both renderers
 //        jscene.renderToggle();      // jme renderer only
 //        jscene.setRenderPRendererMesh(true);  // Force pRenderer to be instantiated
 //        jscene.toggleRenderPRendererMesh();   // turn off mesh
 //        jscene.toggleRenderBoundingVolume();  // turn off bounds
 
 //        wm.removeEntity(avatarCharacter);
 
         return avatarCharacter;
     }
 
     @Override
     protected Node createSceneGraph(Entity entity) {
         // Nothing to do here
         return null;
     }
 
     /**
      * Returns the WlAvatarCharacter object for this renderer. The WlAvatarCharacter
      * provides the control points in the avatar system.
      * @return
      */
     public WlAvatarCharacter getAvatarCharacter() {
         return avatarCharacter;
     }
 
     public void selectForInput(boolean selected) {
         WorldManager wm = ClientContextJME.getWorldManager();
         ((WlAvatarContext)avatarCharacter.getContext()).getSteering().setEnable(false);
         AvatarControlScheme control = (AvatarControlScheme)((JSceneEventProcessor)wm.getUserData(JSceneEventProcessor.class)).setDefault(new AvatarControlScheme(avatarCharacter));
         avatarCharacter.selectForInput();
         control.getAvatarTeam().add(avatarCharacter);
         selectedForInput = selected;
 
         if (selected) {
             // Listen for avatar movement and update the cell
             avatarCharacter.getController().addCharacterMotionListener(characterMotionListener);
 
             // Listen for game context changes
             avatarCharacter.getContext().addGameContextListener(gameContextListener);
         } else {
             avatarCharacter.getController().removeCharacterMotionListener(characterMotionListener);
             avatarCharacter.getContext().removeGameContextListener(gameContextListener);
         }
 
     }
 
     public void trigger(int trigger, boolean pressed) {
         if (!selectedForInput && avatarCharacter!=null) {
             // Sync to avoid concurrent updates of currentTrigger and currentPressed
             synchronized(this) {
                 if (currentTrigger==trigger && currentPressed==pressed)
                     return;
 
                 if (pressed)
                     avatarCharacter.getContext().triggerPressed(trigger);
                 else
                     avatarCharacter.getContext().triggerReleased(trigger);
                 currentTrigger = trigger;
                 currentPressed = pressed;
             }
         }
     }
 
 
 }
