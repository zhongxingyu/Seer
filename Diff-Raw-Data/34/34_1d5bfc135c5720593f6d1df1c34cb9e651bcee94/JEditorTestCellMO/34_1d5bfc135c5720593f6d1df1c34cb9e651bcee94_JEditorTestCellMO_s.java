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
 package org.jdesktop.wonderland.modules.jeditortest.server;
 
 import com.jme.math.Vector2f;
 import org.jdesktop.wonderland.common.ExperimentalAPI;
 import org.jdesktop.wonderland.common.cell.ClientCapabilities;
 import org.jdesktop.wonderland.common.cell.state.CellClientState;
 import org.jdesktop.wonderland.common.cell.state.CellServerState;
 import org.jdesktop.wonderland.modules.jeditortest.common.JEditorTestCellClientState;
 import org.jdesktop.wonderland.modules.appbase.server.cell.App2DCellMO;
 import org.jdesktop.wonderland.server.comms.WonderlandClientID;
 
 /**
  * A server cell associated with a JEditor test.
  *
  * @author nsimpson,deronj
  */
 
 @ExperimentalAPI
 public class JEditorTestCellMO extends App2DCellMO {
 
     /** The preferred width (from the WFS file) */
     private int preferredWidth;
 
     /** The preferred height (from the WFS file) */
     private int preferredHeight;
 
     /** Default constructor, used when the cell is created via WFS */
     public JEditorTestCellMO() {
         super();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected String getClientCellClassName(WonderlandClientID clientID, ClientCapabilities capabilities) {
         return "org.jdesktop.wonderland.modules.jeditortest.client.JEditorTestCell";
     }
 
     /** 
      * {@inheritDoc}
      */
     @Override
     protected CellClientState getClientState (CellClientState cellClientState, WonderlandClientID clientID, ClientCapabilities capabilities) {
         if (cellClientState == null) {
             cellClientState = new JEditorTestCellClientState(pixelScale);
         }
         ((JEditorTestCellClientState)cellClientState).setPreferredWidth(preferredWidth);
         ((JEditorTestCellClientState)cellClientState).setPreferredHeight(preferredHeight);
         return super.getClientState(cellClientState, clientID, capabilities);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void setServerState(CellServerState serverState) {
 	super.setServerState(serverState);
 
 	JEditorTestCellServerState state = (JEditorTestCellServerState) serverState;
 	preferredWidth = state.getPreferredWidth();
 	preferredHeight = state.getPreferredHeight();
 	pixelScale = new Vector2f(state.getPixelScaleX(), state.getPixelScaleY());
     }
 }
