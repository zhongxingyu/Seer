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
 package org.jdesktop.wonderland.modules.testcells.client.cell;
 
 import com.jme.renderer.ColorRGBA;
 import org.jdesktop.wonderland.client.cell.*;
 import org.jdesktop.wonderland.common.cell.CellID;
 import org.jdesktop.wonderland.common.cell.state.CellClientState;
 import org.jdesktop.wonderland.common.cell.config.jme.MaterialJME;
 import org.jdesktop.wonderland.modules.testcells.client.jme.cellrenderer.ShapeRenderer;
 import org.jdesktop.wonderland.modules.testcells.common.cell.state.SimpleShapeCellClientState;
 
 /**
  * Simple shape
  * 
  * @author paulby
  */
 public class SimpleShapeCell extends Cell {
 
     protected SimpleShapeCellClientState.Shape shape;
     protected MaterialJME materialJME;
     private float mass;
     private ShapeRenderer shapeRenderer;
 
     /**
      * Mass of zero will result in a static rigid body, non zero will be dynamic
      * @param cellID
      * @param cellCache
      * @param mass
      */
     public SimpleShapeCell(CellID cellID, CellCache cellCache) {
         super(cellID, cellCache);
     }
     
     @Override
     public void setClientState(CellClientState configData) {
         super.setClientState(configData);
         SimpleShapeCellClientState c = (SimpleShapeCellClientState) configData;
         this.shape = c.getShape();
         this.mass = c.getMass();
         this.materialJME = c.getMaterialJME();
//        shapeRenderer.shapeChanged();
//        shapeRenderer.colorChanged();
     }
 
     
     @Override
     protected CellRenderer createCellRenderer(RendererType rendererType) {
         switch(rendererType) {
             case RENDERER_2D :
                 // No 2D Renderer yet
                 break;
             case RENDERER_JME :
                 shapeRenderer= new ShapeRenderer(this);
                 break;                
         }
         
         return shapeRenderer;
     }
 
     public SimpleShapeCellClientState.Shape getShape() {
         return shape;
     }
 
     public float getMass() {
         return mass;
     }
 
     public MaterialJME getMaterialJME() {
         return materialJME;
     }
 }
