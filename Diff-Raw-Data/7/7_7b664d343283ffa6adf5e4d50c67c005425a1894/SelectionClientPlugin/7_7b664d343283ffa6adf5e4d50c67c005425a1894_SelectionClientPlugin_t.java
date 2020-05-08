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
 package org.jdesktop.wonderland.modules.selection.client.cell;
 
 import com.jme.math.Vector3f;
 import com.jme.renderer.ColorRGBA;
 import com.jme.scene.Geometry;
 import com.jme.scene.Node;
 import com.jme.scene.Spatial;
 import java.util.List;
 import java.util.logging.Logger;
 import org.jdesktop.mtgame.Entity;
 import org.jdesktop.mtgame.RenderComponent;
 import org.jdesktop.mtgame.RenderUpdater;
 import org.jdesktop.wonderland.client.BaseClientPlugin;
 import org.jdesktop.wonderland.client.input.Event;
 import org.jdesktop.wonderland.client.input.EventClassListener;
 import org.jdesktop.wonderland.client.jme.ClientContextJME;
 import org.jdesktop.wonderland.client.jme.utils.traverser.ProcessNodeInterface;
 import org.jdesktop.wonderland.client.jme.utils.traverser.TreeScan;
 import org.jdesktop.wonderland.client.scenemanager.SceneManager;
 import org.jdesktop.wonderland.client.scenemanager.event.EnterExitEvent;
 import org.jdesktop.wonderland.common.annotation.Plugin;
 
 /**
  * Registers a plugin to listen for Cell enter/exit events of the mouse (NOT
  * avatar enter/exit events). Highlights the Cells over which the mouse is by
  * making it a bit larger.
  *
  * @author Jordan Slott <jslott@dev.java.net>
  */
 @Plugin
 public class SelectionClientPlugin extends BaseClientPlugin {
 
     private static Logger logger = Logger.getLogger(SelectionClientPlugin.class.getName());
     private HighlighEnterExitListener listener = null;
 
     @Override
     protected void activate() {
         // Register for EnterExitEvent.class with the Scene Manager
         listener = new HighlighEnterExitListener();
         SceneManager.getSceneManager().addSceneListener(listener);
     }
 
     @Override
     protected void deactivate() {
         // Remove the listener for EnterExitEvent.class
         SceneManager.getSceneManager().removeSceneListener(listener);
         listener = null;
     }
 
     /**
      * Listener for Enter/Exit event, makes the Cell a bit larger using the
      * local transform only (since it is just this client that should see
      * it).
      */
     class HighlighEnterExitListener extends EventClassListener {
 
         @Override
         public Class[] eventClassesToConsume() {
             return new Class[] { EnterExitEvent.class };
         }
 
         @Override
         public void commitEvent(Event event) {
            // NOTE: disabled by nsimpson as temporary workaround for bug 239:
            // HUD controls have spurious shadows
            if (true) {
                return;
            }
            // Fetch the Entity from the event, if there is one
             final EnterExitEvent eeEvent = (EnterExitEvent)event;
             List<Entity> entityList = eeEvent.getEntityList();
             if (entityList == null || entityList.isEmpty() == true) {
                 //logger.warning("No entity for event");
                 return;
             }
             Entity entity = entityList.get(0);
             if (entity == null) {
                 //logger.warning("No entity for event " + entity);
                 return;
             }
 
             // Fetch the local transform for the Entity and make the Entity 10%
             // bigger if we are entering the Entity. Store away the original
             // Entity scaling. This assumes that all exit events are transmitted
             // first and always. XXX
             final Node rootNode = getSceneGraphRoot(entity);
             if (rootNode == null) {
                 //logger.warning("No root node for entity " + entity);
                 return;
             }
 
             TreeScan.findNode(rootNode, Geometry.class, new ProcessNodeInterface() {
                 public boolean processNode(final Spatial s) {
                     RenderUpdater updater = new RenderUpdater() {
                         public void update(Object arg0) {
 //                            System.out.println("SETTING GLOW ENABLED " + eeEvent.isEnter() + " ON " + s.toString());
                             s.setGlowEnabled(eeEvent.isEnter());
                             s.setGlowColor(new ColorRGBA(1.0f, 1.0f, 1.0f, 0.5f));
                             s.setGlowScale(new Vector3f(2.0f, 2.0f, 2.0f));
                             ClientContextJME.getWorldManager().addToUpdateList(s);
                         }
                     };
                     ClientContextJME.getWorldManager().addRenderUpdater(updater, null);
                     return true;
                 }
             }, false, false);
         }
 
         /**
          * Returns the scene root for the Entity's scene graph
          *
          * @return The scene graph root Node
          */
         private Node getSceneGraphRoot(Entity entity) {
             RenderComponent cellRC = entity.getComponent(RenderComponent.class);
             if (cellRC == null) {
                 return null;
             }
             return cellRC.getSceneRoot();
         }
     }
 }
