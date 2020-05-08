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
 package org.jdesktop.wonderland.client.jme;
 
 import com.jme.math.Quaternion;
 import com.jme.math.Vector3f;
 import com.jme.scene.CameraNode;
 import org.jdesktop.mtgame.NewFrameCondition;
 import org.jdesktop.mtgame.ProcessorArmingCollection;
 import org.jdesktop.mtgame.WorldManager;
 import org.jdesktop.wonderland.common.cell.CellTransform;
 
 /**
  * A very simplistic third person camera model
  * 
  * @author paulby
  */
 public class ThirdPersonCameraProcessor extends CameraProcessor {
 
     private Quaternion rotation = new Quaternion();
     private Vector3f translation = new Vector3f();
     private Vector3f offset = new Vector3f(0,4,-10);
     private boolean commitRequired = false;
     
     public ThirdPersonCameraProcessor(CameraNode cameraNode, WorldManager wm) {
         super(cameraNode, wm);
     }
     
     @Override
     public void compute(ProcessorArmingCollection arg0) {
     }
 
     @Override
     public void commit(ProcessorArmingCollection arg0) {
         synchronized(this) {
             if (commitRequired) {
                 cameraNode.setLocalRotation(rotation);
                 cameraNode.setLocalTranslation(translation);
                 wm.addToUpdateList(cameraNode);
                 commitRequired = false;
             }
 
         }
     }
 
     @Override
     public void initialize() {
         setArmingCondition(new NewFrameCondition(this));
     }
 
     @Override
     public void viewMoved(CellTransform worldTransform) {
         synchronized(this) {
             translation = worldTransform.getTranslation(translation);
             rotation = worldTransform.getRotation(rotation);
            
             Vector3f cameraTrans = rotation.mult(offset);
            
             translation.addLocal(cameraTrans);
             commitRequired=true;
         }
     }
 
 
 
 }
