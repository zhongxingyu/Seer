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
 package org.jdesktop.wonderland.modules.palette.client;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.lang.ref.WeakReference;
 import java.util.logging.Logger;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.SwingUtilities;
 import org.jdesktop.wonderland.client.BaseClientPlugin;
 import org.jdesktop.wonderland.client.cell.Cell;
 import org.jdesktop.wonderland.client.cell.CellEditChannelConnection;
 import org.jdesktop.wonderland.client.comms.WonderlandSession;
 import org.jdesktop.wonderland.client.contextmenu.ContextMenuActionListener;
 import org.jdesktop.wonderland.client.contextmenu.ContextMenuItemEvent;
 import org.jdesktop.wonderland.client.contextmenu.ContextMenuItem;
 import org.jdesktop.wonderland.client.contextmenu.SimpleContextMenuItem;
 import org.jdesktop.wonderland.client.contextmenu.annotation.ContextMenuFactory;
 import org.jdesktop.wonderland.client.contextmenu.spi.ContextMenuFactorySPI;
 import org.jdesktop.wonderland.client.jme.JmeClientMain;
 import org.jdesktop.wonderland.client.jme.dnd.DragAndDropManager;
 import org.jdesktop.wonderland.client.jme.dnd.spi.DataFlavorHandlerSPI;
 import org.jdesktop.wonderland.client.login.LoginManager;
 import org.jdesktop.wonderland.client.login.ServerSessionManager;
 import org.jdesktop.wonderland.client.scenemanager.event.ContextEvent;
 import org.jdesktop.wonderland.common.annotation.Plugin;
 import org.jdesktop.wonderland.common.cell.CellEditConnectionType;
 import org.jdesktop.wonderland.common.cell.messages.CellDuplicateMessage;
 import org.jdesktop.wonderland.common.cell.security.ChildrenAction;
 import org.jdesktop.wonderland.common.cell.security.ModifyAction;
 import org.jdesktop.wonderland.modules.palette.client.dnd.CellPaletteDataFlavorHandler;
 import org.jdesktop.wonderland.modules.security.client.SecurityComponent;
 import org.jdesktop.wonderland.client.cell.utils.CellUtils;
 import org.jdesktop.wonderland.client.hud.CompassLayout.Layout;
 import org.jdesktop.wonderland.client.hud.HUD;
 import org.jdesktop.wonderland.client.hud.HUDComponent;
 import org.jdesktop.wonderland.client.hud.HUDManagerFactory;
 
 /**
  * Client-size plugin for the cell palette.
  * 
  * @author Jordan Slott <jslott@dev.java.net>
  */
 @Plugin
 @ContextMenuFactory
 public class PaletteClientPlugin extends BaseClientPlugin
         implements ContextMenuFactorySPI
 {
     private static Logger logger = Logger.getLogger(PaletteClientPlugin.class.getName());
 
     /* The single instance of the cell palette dialog */
     private WeakReference<CellPalette> cellPaletteFrameRef = null;
 
     /* The single instance of the Affordance HUD Panel */
     private static HUDCellPalette hudCellPalette = null;
     private static HUDComponent paletteHUD = null;
 
     /* The menu item to add to the menu */
     private JMenuItem paletteMI;
     private JMenuItem paletteHUDMI;
 
     /* The dnd flavor handler we activate */
     private final DataFlavorHandlerSPI dndHandler =
             new CellPaletteDataFlavorHandler();
 
     @Override
     public void initialize(ServerSessionManager loginInfo) {
         // Create the Palette menu and the Cell submenu and dialog that lets
         // users create new cells.  The menu will be added when our server
         // becomes primary.
         paletteMI = new JMenuItem("Component...");
         paletteMI.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 CellPalette cellPaletteFrame;
                 if (cellPaletteFrameRef == null || cellPaletteFrameRef.get() == null) {
                     cellPaletteFrame = new CellPalette();
                     cellPaletteFrameRef = new WeakReference(cellPaletteFrame);
                 }
                 else {
                     cellPaletteFrame = cellPaletteFrameRef.get();
                 }
                 
                 if (cellPaletteFrame.isVisible() == false) {
                     cellPaletteFrame.setVisible(true);
                 }
                 cellPaletteFrame.toFront();
             }
         });
 
         // Add the Palette menu and the Cell submenu and dialog that lets users
         // create new cells.
         paletteHUDMI = new JMenuItem("Shortcuts");
         paletteHUDMI.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 // Display the HUD Panel. We need to call HUD methods
                 // on a thread OTHER than the AWT Event Thread.
                 new Thread() {
                     @Override
                     public void run() {
                         if (paletteHUD == null) {
                             createHUD();
                         }
                         paletteHUD.setVisible(true);
                     }
                 }.start();
             }
         });
 
         super.initialize(loginInfo);
     }
 
     /**
      * Creates the affordance HUD frame.
      *
      * NOTE: This method should NOT be called on the AWT Event Thread.
      */
     private void createHUD() {
         HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
 
         // create affordances Swing control
         hudCellPalette = new HUDCellPalette();
 
         // create HUD control
         paletteHUD = mainHUD.createComponent(hudCellPalette);
         paletteHUD.setName("Shortcuts");
         paletteHUD.setPreferredLocation(Layout.NORTHEAST);
 
         // add affordances HUD panel to main HUD
         mainHUD.addComponent(paletteHUD);
     }
 
     /**
      * Notification that our server is now the the primary server
      */
     @Override
     protected void activate() {
         // activate
         JmeClientMain.getFrame().addToInsertMenu(paletteMI, 0);
         JmeClientMain.getFrame().addToWindowMenu(paletteHUDMI, 6);
 
         // Register the handler for CellServerState flavors with the system-wide
         // drag and drop manager. When the preview icon is dragged from the Cell
         // Palette this handler creates an instance of the cell in the world.
         DragAndDropManager dndManager = DragAndDropManager.getDragAndDropManager();
         dndManager.registerDataFlavorHandler(dndHandler);
     }
 
     @Override
     protected void deactivate() {
         // deactivate
         JmeClientMain.getFrame().removeFromInsertMenu(paletteMI);
         JmeClientMain.getFrame().removeFromWindowMenu(paletteHUDMI);
 
         DragAndDropManager dndManager = DragAndDropManager.getDragAndDropManager();
         dndManager.unregisterDataFlavorHandler(dndHandler);
     }
 
     /**
      * @inheritDoc()
      */
     public ContextMenuItem[] getContextMenuItems(ContextEvent event) {
         final SimpleContextMenuItem deleteItem = 
                 new SimpleContextMenuItem("Delete", null, new DeleteListener());
 
         final SimpleContextMenuItem duplicateItem =
                 new SimpleContextMenuItem("Duplicate", null, new DuplicateListener());
 
         // find the security component for both this cell and it's parent,
         // if any
         final Cell cell = event.getPrimaryCell();
         final SecurityComponent sc = cell.getComponent(SecurityComponent.class);
         final SecurityComponent psc;
         if (cell.getParent() != null) {
             psc = cell.getParent().getComponent(SecurityComponent.class);
         } else {
             psc = null;
         }
 
         // see if we can check security locally, or if we have to make a
         // remote request
         if ((sc == null || sc.hasPermissions()) &&
             (psc == null || psc.hasPermissions()))
         {
             duplicateItem.setEnabled(canDuplicate(psc));
             deleteItem.setEnabled(canDelete(sc, psc));
         } else {
             Thread t = new Thread(new Runnable() {
                 public void run() {
                     duplicateItem.setEnabled(canDuplicate(psc));
                     duplicateItem.setLabel("Duplicate");
                     duplicateItem.fireMenuItemRepaintListeners();
                     deleteItem.setEnabled(canDelete(sc, psc));
                     deleteItem.setLabel("Delete");
                     deleteItem.fireMenuItemRepaintListeners();
                 }
             }, "Cell palette security check");
             t.start();
 
             // wait for a little bit to see if the check comes back
             // quickly
             try {
                 t.join(250);
             } catch (InterruptedException ie) {}
 
             if (!t.isAlive()) {
                 duplicateItem.setEnabled(false);
                 duplicateItem.setLabel("Duplicate (checking)");
                 deleteItem.setEnabled(false);
                 deleteItem.setLabel("Delete (checking)");
             }
         }
 
         return new ContextMenuItem[] {
             deleteItem,
             duplicateItem,
         };
     }
 
     private boolean canDuplicate(SecurityComponent psc) {
         if (psc == null) {
             return true;
         }
 
         try {
             ChildrenAction ca = new ChildrenAction();
             return psc.getPermission(ca);
         } catch (InterruptedException ie) {
             // shouldn't happen, since we check above
             return true;
         }
     }
 
     private boolean canDelete(SecurityComponent sc, SecurityComponent psc) {
         boolean out = true;
         if (sc == null && psc == null) {
             return out;
         }
 
         try {
             ModifyAction ma = new ModifyAction();
             ChildrenAction ca = new ChildrenAction();
 
             if (sc != null) {
                 out = sc.getPermission(ma);
             }
             if (out && psc != null) {
                 out = psc.getPermission(ca);
             }
         } catch (InterruptedException ie) {
             // shouldn't happen, since we check above
         }
 
         return out;
     }
 
     /**
      * Listener for the "Delete" context menu item
      */
     private class DeleteListener implements ContextMenuActionListener {
 
         public void actionPerformed(ContextMenuItemEvent event) {
             // Display a confirmation dialog to make sure we really want to
             // delete the cell.
             Cell cell = event.getCell();
             int result = JOptionPane.showConfirmDialog(
                     JmeClientMain.getFrame().getFrame(),
                     "Are you sure you wish to delete cell " + cell.getName(),
                     "Confirm Delete",
                     JOptionPane.YES_NO_OPTION);

            // Note that pressing the Esc key results in -1, so we must check
            // for that too (CLOSED_OPTION)
            if (result == JOptionPane.NO_OPTION || result == JOptionPane.CLOSED_OPTION) {
                 return;
             }
 
             CellUtils.deleteCell(cell);
         }
     }
 
     /**
      * Listener for the "Duplicate" context menu item
      */
     private class DuplicateListener implements ContextMenuActionListener {
 
         public void actionPerformed(ContextMenuItemEvent event) {
             // Create a new name for the cell, based upon the old name.
             Cell cell = event.getCell();
             String cellName = cell.getName() + " Copy";
 
             // If we want to delete, send a message to the server as such
             WonderlandSession session = LoginManager.getPrimary().getPrimarySession();
             CellEditChannelConnection connection = (CellEditChannelConnection)
                     session.getConnection(CellEditConnectionType.CLIENT_TYPE);
             CellDuplicateMessage msg = new CellDuplicateMessage(cell.getCellID(), cellName);
             connection.send(msg);
 
             // Really should receive an OK/Error response from the server!
         }
     }
 }
