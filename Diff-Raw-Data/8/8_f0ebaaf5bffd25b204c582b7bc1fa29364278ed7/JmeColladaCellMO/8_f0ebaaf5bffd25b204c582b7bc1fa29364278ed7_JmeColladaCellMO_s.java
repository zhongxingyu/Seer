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
 package org.jdesktop.wonderland.modules.jmecolladaloader.server.cell;
 
 import org.jdesktop.wonderland.server.cell.*;
 import com.jme.bounding.BoundingBox;
 import com.jme.bounding.BoundingVolume;
 import com.jme.math.Quaternion;
 import com.jme.math.Vector3f;
 import org.jdesktop.wonderland.common.ExperimentalAPI;
 import org.jdesktop.wonderland.common.cell.CellTransform;
 import org.jdesktop.wonderland.common.cell.ClientCapabilities;
 import org.jdesktop.wonderland.common.cell.state.CellClientState;
 import org.jdesktop.wonderland.modules.jmecolladaloader.common.cell.state.JmeColladaCellClientState;
 import org.jdesktop.wonderland.common.cell.state.CellServerState;
 import org.jdesktop.wonderland.common.cell.state.PositionComponentServerState;
 import org.jdesktop.wonderland.modules.jmecolladaloader.common.cell.state.JmeColladaCellServerState;
 import org.jdesktop.wonderland.server.comms.WonderlandClientID;
 
 
 /**
  * A cell for collada models
  * @author paulby
  */
 @ExperimentalAPI
 public class JmeColladaCellMO extends CellMO { 
     
     /* The unique model URI */
     private String modelURI = null;
     private Vector3f geometryTranslation = new Vector3f();
     private Quaternion geometryRotation = new Quaternion();
     	
     /** Default constructor, used when cell is created via WFS */
     public JmeColladaCellMO() {
     }
     
     public JmeColladaCellMO(Vector3f center, float size, String modelURI, Vector3f geometryTranslation, Quaternion geometryRotation) {
         super(new BoundingBox(new Vector3f(), size, size, size), new CellTransform(null, center));
         this.modelURI = modelURI;
         this.geometryRotation = geometryRotation;
         this.geometryTranslation = geometryTranslation;
     }
 
     public JmeColladaCellMO(Vector3f center, float size, String modelURI) {
         this(center, size,  modelURI, null, null);
     }
     
     public JmeColladaCellMO(BoundingVolume bounds, CellTransform transform) {
         super(bounds, transform);
     }
     
     @Override protected String getClientCellClassName(WonderlandClientID clientID, ClientCapabilities capabilities) {
         return "org.jdesktop.wonderland.modules.jmecolladaloader.client.cell.JmeColladaCell";
     }
 
     @Override
     public CellClientState getClientState(CellClientState cellClientState, WonderlandClientID clientID, ClientCapabilities capabilities) {
         if (cellClientState == null) {
           cellClientState = new JmeColladaCellClientState();
         }
         ((JmeColladaCellClientState)cellClientState).setModelURI(modelURI);
         ((JmeColladaCellClientState)cellClientState).setGeometryTranslation(geometryTranslation);
         ((JmeColladaCellClientState)cellClientState).setGeometryRotation(geometryRotation);
 
         return super.getClientState(cellClientState, clientID, capabilities);
     }
 
     @Override
     public void setServerState(CellServerState setup) {
         super.setServerState(setup);
         this.modelURI = ((JmeColladaCellServerState)setup).getModel();
 
         // Override model URI if there is a property set for it
        String model = setup.getProperty("model");
        if (model != null) {
            this.modelURI = model;
        }
         
         JmeColladaCellServerState jccss = (JmeColladaCellServerState)setup;
         if (jccss.getGeometryTranslation() != null) {
             this.geometryTranslation = new Vector3f(
                     (float) jccss.getGeometryTranslation().x,
                     (float) jccss.getGeometryTranslation().y,
                     (float) jccss.getGeometryTranslation().z);
         }
 
         if (jccss.getGeometryRotation() != null) {
             this.geometryRotation = new Quaternion().fromAngleAxis(
                     (float) jccss.getGeometryRotation().angle,
                     new Vector3f(
                     (float) jccss.getGeometryRotation().x,
                     (float) jccss.getGeometryRotation().y,
                     (float) jccss.getGeometryRotation().z));
         }
     }
 
     @Override
     public CellServerState getServerState(CellServerState state) {
         JmeColladaCellServerState ret;
         if (state==null) {
             ret = new JmeColladaCellServerState();
         } else {
             ret = (JmeColladaCellServerState) state;
         }
 
         System.err.println("**************************************");
         System.err.println(modelURI);
 
         ret.setModel(modelURI);
         if (geometryTranslation != null) {
             ret.setGeometryTranslation(new PositionComponentServerState.Origin(geometryTranslation));
         }
         if (geometryRotation != null) {
             ret.setGeometryRotation(new PositionComponentServerState.Rotation(geometryRotation));
         }
         return super.getServerState(ret);
     }
 }
