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
 package org.jdesktop.wonderland.modules.testcells.server.cell;
 
 import org.jdesktop.wonderland.server.cell.*;
 import com.jme.bounding.BoundingBox;
 import com.jme.math.Vector3f;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
 import org.jdesktop.wonderland.common.cell.setup.BasicCellSetup;
 import org.jdesktop.wonderland.common.ExperimentalAPI;
 import org.jdesktop.wonderland.common.cell.CellTransform;
 import org.jdesktop.wonderland.common.cell.ClientCapabilities;
 import org.jdesktop.wonderland.common.cell.config.CellConfig;
 import org.jdesktop.wonderland.modules.testcells.common.cell.config.SimpleShapeConfig;
 
 
 /**
  * Simple test for cell dragging.
  *
  * @author paulby
  */
 @ExperimentalAPI
 public class DisappearTestMO extends SimpleShapeCellMO {
     
     /** Default constructor, used when cell is created via WFS */
     public DisappearTestMO () {
         this(new Vector3f(), 50);
     }
 
     public DisappearTestMO (Vector3f center, float size) {
         super(center, size);
         addComponent(new ChannelComponentMO(this));
         addComponent(new MovableComponentMO(this));
     }
     
     @Override 
    protected String getClientCellClassName(WonderlandClientID clientID, ClientCapabilities capabilities) {
         return "org.jdesktop.wonderland.modules.testcells.client.cell.DisappearTest";
     }
 
     @Override
     public void setupCell(BasicCellSetup setup) {
         super.setupCell(setup);
     }
 
     @Override
     public void reconfigureCell(BasicCellSetup setup) {
         super.reconfigureCell(setup);
         setupCell(setup);
     }
 }
