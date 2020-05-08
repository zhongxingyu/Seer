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
 package org.jdesktop.wonderland.modules.simplewhiteboard.client;
 
 import java.awt.Point;
 import java.math.BigInteger;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.logging.Logger;
 import org.jdesktop.wonderland.client.cell.CellCache;
 import org.jdesktop.wonderland.client.cell.ChannelComponent;
 import org.jdesktop.wonderland.common.cell.CellID;
 import org.jdesktop.wonderland.common.cell.config.CellConfig;
 import org.jdesktop.wonderland.modules.appbase.client.AppType;
 import org.jdesktop.wonderland.modules.appbase.client.App2DCell;
 import org.jdesktop.wonderland.modules.simplewhiteboard.common.WhiteboardCellConfig;
 import org.jdesktop.wonderland.modules.simplewhiteboard.common.WhiteboardCompoundCellMessage;
 import org.jdesktop.wonderland.modules.simplewhiteboard.common.WhiteboardAction.Action;
 import org.jdesktop.wonderland.modules.simplewhiteboard.common.WhiteboardCommand.Command;
 import org.jdesktop.wonderland.modules.simplewhiteboard.common.WhiteboardTypeName;
 import org.jdesktop.wonderland.common.ExperimentalAPI;
 
 /**
  * Client Cell for a whiteboard shared application.
  *
  * @author nsimpson,deronj
  */
 
 @ExperimentalAPI
 public class WhiteboardCell extends App2DCell {
     
     /** The logger used by this class */
     private static final Logger logger = Logger.getLogger(WhiteboardCell.class.getName());
     
     /** The (singleton) window created by the whiteboard app */
     private WhiteboardWindow whiteboardWin;
 
     /** The cell config message received from the server cell */
     private WhiteboardCellConfig config;
     
     /** The communications component used to communicate with the server */
     private WhiteboardComponent commComponent;
 
     /**
      * Create an instance of WhiteboardCell.
      *
      * @param cellID The ID of the cell.
      * @param cellCache the cell cache which instantiated, and owns, this cell.
      */
     public WhiteboardCell (CellID cellID, CellCache cellCache) {
         super(cellID, cellCache);
         addComponent(new ChannelComponent(this));
 	commComponent = new WhiteboardComponent(this);
         addComponent(commComponent);
     }
     
     /** 
      * {@inheritDoc}
      */
     public AppType getAppType () {
 	return new WhiteboardAppType();
     }
 
     /**
      * Initialize the whiteboard with parameters from the server.
      *
      * @param configData the config data to initialize the cell with
      */
     public void configure (CellConfig configData) {
	System.err.println("************* Enter WhiteboardCell.configure");
 
         config = (WhiteboardCellConfig)configData;
         setApp(new WhiteboardApp(getAppType(), config.getPreferredWidth(), config.getPreferredHeight(),
 				 config.getPixelScale(), commComponent));
 
 	// Associate the app with this cell (must be done before making it visible)
 	app.setCell(this);
 
 	// Get the window the app created
 	whiteboardWin = ((WhiteboardApp)app).getWindow();
 
 	// Make the app window visible
 	((WhiteboardApp)app).setVisible(true);
 
 	// Note: we used to force a sync here. But in the new implementation we will
 	// perform the sync when the cell status becomes BOUNDS.
     }
     
     /**
      * Process the actions in a compound message
      *
      * @param msg a compound message
      */
     void processMessage(WhiteboardCompoundCellMessage msg) {
         switch (msg.getAction()) {
             case SET_TOOL:
                 whiteboardWin.setTool(msg.getTool());
                 break;
             case SET_COLOR:
                 whiteboardWin.setPenColor(msg.getColor());
                 break;
             case MOVE_TO:
             case DRAG_TO:
                 LinkedList<Point> positions = msg.getPositions();
                 Iterator<Point> iter = positions.iterator();
                 
                 while (iter.hasNext()) {
                     Point position = iter.next();
                     if (msg.getAction() == Action.MOVE_TO) {
                         whiteboardWin.moveTo(position);
                     } else if (msg.getAction() == Action.DRAG_TO) {
                         whiteboardWin.dragTo(position);
                     }
                 }
                 break;
             case EXECUTE_COMMAND:
                 if (msg.getCommand() == Command.ERASE) {
                     whiteboardWin.erase();
                 }
         }
     }
 
     /**
      * Returns the client ID of this cell's session.
      */
     BigInteger getClientID () {
 	return getCellCache().getSession().getID();
     }
 }
