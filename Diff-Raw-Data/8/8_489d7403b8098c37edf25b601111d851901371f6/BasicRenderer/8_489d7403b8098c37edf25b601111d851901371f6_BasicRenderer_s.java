 /**
  * Project Wonderland
  *
  * Copyright (c) 2004-2008, Sun Microsystems, Inc., All Rights Reserved
  *
  * Redistributions in source code form must reproduce the above
  * copyright and this condition.
  *
  * The contents of this file are subject to the GNU General Public
  * License, Version 2 (the "License"); you may not use this file
  * except in compliance with the License. A copy of the License is
  * available at http://www.opensource.org/licenses/gpl-license.php.
  *
  * $Revision$
  * $Date$
  * $State$
  */
 package org.jdesktop.wonderland.client.jme.cellrenderer;
 
 import com.jme.math.Quaternion;
 import com.jme.math.Vector3f;
 import com.jme.scene.Node;
 import java.util.logging.Logger;
 import org.jdesktop.wonderland.client.cell.Cell;
 import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.JMECollisionComponent;
 import org.jdesktop.mtgame.JMECollisionSystem;
 import org.jdesktop.mtgame.NewFrameCondition;
 import org.jdesktop.mtgame.ProcessorArmingCollection;
 import org.jdesktop.mtgame.ProcessorComponent;
 import org.jdesktop.mtgame.RenderComponent;
 import org.jdesktop.mtgame.WorldManager;
 import org.jdesktop.wonderland.client.cell.MovableComponent;
 import org.jdesktop.wonderland.client.jme.ClientContextJME;
 import org.jdesktop.wonderland.common.ExperimentalAPI;
 import org.jdesktop.wonderland.common.cell.CellTransform;
 
 /**
  *
  * Abstract Renderer class that implements CellRendererJME
  * 
  * @author paulby
  */
 @ExperimentalAPI
 public abstract class BasicRenderer implements CellRendererJME {
     
     protected static Logger logger = Logger.getLogger(BasicRenderer.class.getName());
     protected Cell cell;
     protected Entity entity;
     protected Node rootNode;
     protected MoveProcessor moveProcessor = null;
     
     private Vector3f tmpV3f = new Vector3f();
     private Quaternion tmpQuat = new Quaternion();
     
     public BasicRenderer(Cell cell) {
         this.cell = cell;
     }
     
     protected Entity createEntity() {
         Entity ret = new Entity(this.getClass().getName()+"_"+cell.getCellID());
         
         rootNode = createSceneGraph(ret);
         
         if (cell.getComponent(MovableComponent.class)!=null) {
             // Avatars are movable so create a move processor
             moveProcessor = new MoveProcessor(ClientContextJME.getWorldManager(), rootNode);
             ret.addComponent(ProcessorComponent.class, moveProcessor);
         }
         
         RenderComponent rc = ClientContextJME.getWorldManager().getRenderManager().createRenderComponent(rootNode);
         ret.addComponent(RenderComponent.class, rc);
         
         JMECollisionSystem collisionSystem = (JMECollisionSystem) 
                 ClientContextJME.getWorldManager().getCollisionManager().loadCollisionSystem(JMECollisionSystem.class);
         
        JMECollisionComponent cc = collisionSystem.createCollisionComponent(rootNode);
        ret.addComponent(JMECollisionComponent.class, cc);
         
         return ret;        
     }
 
     /**
      * Create the scene graph
      * @return
      */
     protected abstract Node createSceneGraph(Entity entity);
     
     public Entity getEntity() {
         if (entity==null)
             entity = createEntity();
         return entity;
     }
     
     public void cellTransformUpdate(CellTransform cellTransform) {
         if (moveProcessor!=null) {
             moveProcessor.cellMoved(cellTransform);
         }
     }
     
     /**
      * An mtgame ProcessorCompoenent to process cell moves.
      */
     protected class MoveProcessor extends ProcessorComponent {
 
         private CellTransform cellTransform;
         private boolean dirty = false;
         private Node node;
         private WorldManager worldManager;
         
         public MoveProcessor(WorldManager worldManager, Node node) {
             this.node = node;
             this.worldManager = worldManager;
         }
         
         @Override
         public void compute(ProcessorArmingCollection arg0) {
         }
 
         @Override
         public void commit(ProcessorArmingCollection arg0) {
             synchronized(this) {
                 if (dirty) {
                     node.setLocalTranslation(cellTransform.getTranslation(tmpV3f));
                     node.setLocalRotation(cellTransform.getRotation(tmpQuat));
 //                    System.err.println("BasicRenderer.cellMoved "+tmpV3f+" "+tmpQuat);
                     dirty = false;
                     worldManager.addToUpdateList(node);
                 }
             }
         }
 
         @Override
         public void initialize() {
             setArmingCondition(new NewFrameCondition(this));           
         }
 
         /**
          * Notify the MoveProcessor that the cell has moved
          * 
          * @param transform cell transform in world coordinates
          */
         public void cellMoved(CellTransform transform) {
             synchronized(this) {
                 this.cellTransform = transform;
                 dirty = true;
             }
         }
 
 
     }
 }
