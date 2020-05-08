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
 package org.jdesktop.wonderland.client.jme;
 
 import java.awt.Canvas;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.ResourceBundle;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.ToolTipManager;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import org.jdesktop.mtgame.WorldManager;
 import org.jdesktop.wonderland.client.help.HelpSystem;
 import org.jdesktop.wonderland.common.LogControl;
 import java.util.logging.Logger;
 import javax.swing.ButtonGroup;
 import javax.swing.JCheckBoxMenuItem;
 import javax.swing.JFrame;
 import javax.swing.JRadioButtonMenuItem;
 import javax.swing.SwingUtilities;
 import org.jdesktop.mtgame.FrameRateListener;
 import org.jdesktop.wonderland.client.hud.CompassLayout.Layout;
 import org.jdesktop.wonderland.client.hud.HUD;
 import org.jdesktop.wonderland.client.hud.HUDComponent;
 import org.jdesktop.wonderland.client.hud.HUDManagerFactory;
 import org.jdesktop.wonderland.client.jme.dnd.DragAndDropManager;
 import org.jdesktop.wonderland.client.jme.utils.GUIUtils;
 
 /**
  * The Main JFrame for the wonderland jme client
  * 
  * @author  paulby
  * @author Ronny Standtke <ronny.standtke@fhnw.ch>
  */
 public class MainFrameImpl extends JFrame implements MainFrame {
 
     private static final Logger LOGGER =
             Logger.getLogger(MainFrameImpl.class.getName());
     private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
             "org/jdesktop/wonderland/client/jme/resources/bundle");
     private JMenuItem logoutMI;
     private JMenuItem exitMI;
     private ButtonGroup cameraButtonGroup = new ButtonGroup();
     private JRadioButtonMenuItem firstPersonRB;
     private JRadioButtonMenuItem thirdPersonRB;
     private JRadioButtonMenuItem frontPersonRB;
     private final Map<JMenuItem, Integer> menuWeights =
             new HashMap<JMenuItem, Integer>();
     private JMenu frameRateMenu;
     private int desiredFrameRate = 30;
     private FrameRateListener frameRateListener = null;
     private JMenuItem fpsMI;
     private Chart chart;
     private HUDComponent fpsComponent;
     private WorldManager wm;
 
     static {
         new LogControl(MainFrameImpl.class,
                 "/org/jdesktop/wonderland/client/jme/resources/" +
                 "logging.properties");
     }
     // variables for the location field
     private String serverURL;
     private ServerURLListener serverListener;
 
     /** Creates new form MainFrame */
     public MainFrameImpl(WorldManager wm, int width, int height) {
         this.wm = wm;
 
 
         GUIUtils.initLookAndFeel();
         JPopupMenu.setDefaultLightWeightPopupEnabled(false);
         ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
 
         initComponents();
         initMenus();
 
         setTitle(BUNDLE.getString("Wonderland"));
         centerPanel.setMinimumSize(new Dimension(width, height));
         centerPanel.setPreferredSize(new Dimension(width, height));
 
         // Register the main panel with the drag-and-drop manager
         DragAndDropManager.getDragAndDropManager().setDropTarget(centerPanel);
 
         serverField.getDocument().addDocumentListener(new DocumentListener() {
 
             public void insertUpdate(DocumentEvent e) {
                 updateGoButton();
             }
 
             public void removeUpdate(DocumentEvent e) {
                 updateGoButton();
             }
 
             public void changedUpdate(DocumentEvent e) {
                 updateGoButton();
             }
         });
 
         pack();
     }
 
     private void initMenus() {
         SwingUtilities.invokeLater(new Runnable() {
 
             public void run() {
                 // File menu
                 // Log out
                 logoutMI = new JMenuItem(BUNDLE.getString("Log out"));
                 logoutMI.addActionListener(new ActionListener() {
 
                     public void actionPerformed(ActionEvent evt) {
                         logoutMIActionPerformed(evt);
                     }
                 });
                 addToFileMenu(logoutMI, 2);
 
                 // Exit
                 exitMI = new JMenuItem(BUNDLE.getString("Exit"));
                 exitMI.addActionListener(new ActionListener() {
 
                     public void actionPerformed(ActionEvent evt) {
                         exitMIActionPerformed(evt);
                     }
                 });
                 addToFileMenu(exitMI, 3);
 
                 // View menu
                 firstPersonRB = new JRadioButtonMenuItem(
                         BUNDLE.getString("First Person Camera"));
                 firstPersonRB.addActionListener(new ActionListener() {
 
                     public void actionPerformed(ActionEvent evt) {
                         cameraChangedActionPerformed(evt);
                     }
                 });
                 addToViewMenu(firstPersonRB, 0);
                 cameraButtonGroup.add(firstPersonRB);
 
                 thirdPersonRB = new JRadioButtonMenuItem(
                         BUNDLE.getString("Third Person Camera"));
                 thirdPersonRB.setSelected(true);
                 thirdPersonRB.addActionListener(new ActionListener() {
 
                     public void actionPerformed(ActionEvent evt) {
                         cameraChangedActionPerformed(evt);
                     }
                 });
                 addToViewMenu(thirdPersonRB, 1);
                 cameraButtonGroup.add(thirdPersonRB);
 
                 frontPersonRB = new JRadioButtonMenuItem(
                         BUNDLE.getString("Front Camera"));
                 frontPersonRB.setToolTipText(
                         BUNDLE.getString("Front_Camera_Tooltip"));
                 frontPersonRB.addActionListener(new ActionListener() {
 
                     public void actionPerformed(ActionEvent evt) {
                         cameraChangedActionPerformed(evt);
                     }
                 });
                 addToViewMenu(frontPersonRB, 2);
                 cameraButtonGroup.add(frontPersonRB);
 
                 // Frame Rate menu
                 frameRateMenu = new JMenu(BUNDLE.getString("Max Frame Rate"));
 
                 JMenuItem fps15 = new JCheckBoxMenuItem(
                         BUNDLE.getString("15 fps"));
                 JMenuItem fps30 = new JCheckBoxMenuItem(
                         BUNDLE.getString("30 fps (default)"));
                 JMenuItem fps60 = new JCheckBoxMenuItem(
                         BUNDLE.getString("60 fps"));
                 JMenuItem fps120 = new JCheckBoxMenuItem(
                         BUNDLE.getString("120 fps"));
                 JMenuItem fps200 = new JCheckBoxMenuItem(
                         BUNDLE.getString("200 fps"));
 
                 frameRateMenu.add(fps15);
                 frameRateMenu.add(fps30);
                 frameRateMenu.add(fps60);
                 frameRateMenu.add(fps120);
                 frameRateMenu.add(fps200);
 
                 addToViewMenu(frameRateMenu, 5);
 
                 fps15.addActionListener(new ActionListener() {
 
                     public void actionPerformed(ActionEvent evt) {
                         frameRateActionPerformed(evt);
                     }
                 });
                 fps30.addActionListener(new ActionListener() {
 
                     public void actionPerformed(ActionEvent evt) {
                         frameRateActionPerformed(evt);
                     }
                 });
                 fps60.addActionListener(new ActionListener() {
 
                     public void actionPerformed(ActionEvent evt) {
                         frameRateActionPerformed(evt);
                     }
                 });
                 fps120.addActionListener(new ActionListener() {
 
                     public void actionPerformed(ActionEvent evt) {
                         frameRateActionPerformed(evt);
                     }
                 });
                 fps200.addActionListener(new ActionListener() {
 
                     public void actionPerformed(ActionEvent evt) {
                         frameRateActionPerformed(evt);
                     }
                 });
 
                 // frame rate meter
                 fpsMI = new JCheckBoxMenuItem(BUNDLE.getString("FPS_Meter"));
                 fpsMI.addActionListener(new ActionListener() {
 
                     public void actionPerformed(ActionEvent evt) {
                         if ((fpsComponent == null) ||
                                 !fpsComponent.isVisible()) {
                             showFPSMeter(true);
                         } else {
                             showFPSMeter(false);
                         }
                     }
                 });
 
                 addToWindowMenu(fpsMI, -1);
 
                 // Help menu
                 HelpSystem helpSystem = new HelpSystem();
                 JMenu helpMenu = helpSystem.getHelpJMenu();
                 mainMenuBar.add(helpMenu);
             }
         });
     }
 
     private void logoutMIActionPerformed(ActionEvent evt) {
         if (serverListener != null) {
             serverListener.logout();
         }
 
         serverURL = null;
         updateGoButton();
     }
 
     private void exitMIActionPerformed(ActionEvent evt) {
         ClientContextJME.getWorldManager().shutdown();
         System.exit(0);
     }
 
     private void cameraChangedActionPerformed(ActionEvent evt) {
         ViewManager viewManager = ClientContextJME.getViewManager();
         if (evt.getSource() == firstPersonRB) {
             viewManager.setCameraController(
                     new FirstPersonCameraProcessor());
         } else if (evt.getSource() == thirdPersonRB) {
             viewManager.setCameraController(
                     new ThirdPersonCameraProcessor());
         } else if (evt.getSource() == frontPersonRB) {
             viewManager.setCameraController(
                     new FrontHackPersonCameraProcessor());
         }
 
     }
 
     private void frameRateActionPerformed(ActionEvent evt) {
         JMenuItem mi = (JMenuItem) evt.getSource();
         String[] fpsString = mi.getText().split(" ");
         int fps = Integer.valueOf(fpsString[0]);
         LOGGER.info("maximum fps: " + fps);
         setDesiredFrameRate(fps);
     }
 
     public void setDesiredFrameRate(int desiredFrameRate) {
         this.desiredFrameRate = desiredFrameRate;
 
         for (int i = 0; i < frameRateMenu.getItemCount(); i++) {
             JMenuItem item = frameRateMenu.getItem(i);
             String[] fpsString = item.getText().split(" ");
             int fps = Integer.valueOf(fpsString[0]);
             if (fps == desiredFrameRate) {
                 item.setSelected(true);
             } else {
                 item.setSelected(false);
             }
         }
         wm.getRenderManager().setDesiredFrameRate(desiredFrameRate);
 
         removeFrameRateListener(frameRateListener);
         frameRateListener = addFrameRateListener(desiredFrameRate);
 
         if (chart != null) {
             chart.setMaxValue(desiredFrameRate);
         }
     }
 
     /**
      * updates the Go! button
      */
     public void updateGoButton() {
         String cur = serverField.getText();
         goButton.setEnabled(
                 (cur != null) && (cur.length() > 0) && !cur.equals(serverURL));
     }
 
     /**
      * Return the JME frame
      * @return the frame
      */
     public JFrame getFrame() {
         return this;
     }
 
     /**
      * Returns the canvas of the frame.
      * @return the canvas of the frame.
      */
     public Canvas getCanvas() {
         return ViewManager.getViewManager().getCanvas();
     }
 
     /**
      * Returns the panel of the frame in which the 3D canvas resides.
      * @return the panel of the frame in which the 3D canvas resides.
      */
     public JPanel getCanvas3DPanel() {
         return centerPanel;
     }
 
     /**
      * {@inheritDoc}
      */
     public void addToMenu(JMenu menu, JMenuItem menuItem, int weight) {
         if (weight < 0) {
             weight = Integer.MAX_VALUE;
         }
 
         final int weightFinal = weight;
         final JMenu menuFinal = menu;
         final JMenuItem menuItemFinal = menuItem;
 
         LOGGER.fine(menuFinal.getText() + " menu: inserting [" +
                 menuItemFinal.getText() + "] with weight: " + weight);
 
         SwingUtilities.invokeLater(new Runnable() {
 
             public void run() {
                 // find the index of the first menu item with a higher weight or
                 // the same weight and later in the alphabet
                 int index = 0;
                 for (index = 0; index < menuFinal.getItemCount(); index++) {
                     JMenuItem curItem = menuFinal.getItem(index);
                     int curWeight = menuWeights.get(curItem);
                     if (curWeight > weightFinal) {
                         break;
                     } else if (curWeight == weightFinal) {
                         String currentItemName = curItem.getName();
                         if (currentItemName == null) {
                             break;
                         }
 
                         String name = menuItemFinal.getName();
                         if ((name != null) &&
                                 name.compareTo(currentItemName) > 0) {
                             break;
                         }
                     }
                 }
 
                 // add the item at the right place
                 menuFinal.insert(menuItemFinal, index);
 
                 // remember the menu's weight
                 menuWeights.put(menuItemFinal, weightFinal);
             }
         });
     }
 
     /**
      * Remove the given menu item from a menu
      * @param menu the menu to remove from
      * @param item the item to remove
      */
     public void removeFromMenu(JMenu menu, JMenuItem item) {
         menu.remove(item);
         menuWeights.remove(item);
     }
 
     /**
      * {@inheritDoc}
      */
     public void addToFileMenu(JMenuItem menuItem) {
         addToMenu(fileMenu, menuItem, -1);
     }
 
     /**
      * {@inheritDoc}
      */
     public void addToFileMenu(JMenuItem menuItem, int index) {
         addToMenu(fileMenu, menuItem, index);
     }
 
     /**
      * {@inheritDoc}
      */
     public void removeFromFileMenu(JMenuItem menuItem) {
         removeFromMenu(fileMenu, menuItem);
     }
 
     /**
      * {@inheritDoc}
      */
     public void addToEditMenu(JMenuItem menuItem) {
         addToMenu(editMenu, menuItem, -1);
     }
 
     /**
      * {@inheritDoc}
      */
     public void addToEditMenu(JMenuItem menuItem, int index) {
         addToMenu(editMenu, menuItem, index);
     }
 
     /**
      * {@inheritDoc}
      */
     public void removeFromEditMenu(JMenuItem menuItem) {
         removeFromMenu(editMenu, menuItem);
     }
 
     /**
      * {@inheritDoc}
      */
     public void addToViewMenu(JMenuItem menuItem) {
         addToMenu(viewMenu, menuItem, -1);
     }
 
     /**
      * {@inheritDoc}
      */
     public void addToViewMenu(JMenuItem menuItem, int index) {
         addToMenu(viewMenu, menuItem, index);
     }
 
     /**
      *
      * @param menuItem
      */
     public void addToViewMenuCameraGroup(JRadioButtonMenuItem menuItem) {
     }
 
     /**
      * {@inheritDoc}
      */
     public void removeFromViewMenu(JMenuItem menuItem) {
         removeFromMenu(viewMenu, menuItem);
     }
 
     /**
      * {@inheritDoc}
      */
     public void addToInsertMenu(JMenuItem menuItem) {
         addToMenu(insertMenu, menuItem, -1);
     }
 
     /**
      * {@inheritDoc}
      */
     public void addToInsertMenu(JMenuItem menuItem, int index) {
         addToMenu(insertMenu, menuItem, index);
     }
 
     /**
      * {@inheritDoc}
      */
     public void removeFromInsertMenu(JMenuItem menuItem) {
         removeFromMenu(insertMenu, menuItem);
     }
 
     /**
      * {@inheritDoc}
      */
     public void addToToolsMenu(JMenuItem menuItem) {
         addToMenu(toolsMenu, menuItem, -1);
     }
 
     /**
      * {@inheritDoc}
      */
     public void addToToolsMenu(JMenuItem menuItem, int index) {
         addToMenu(toolsMenu, menuItem, index);
     }
 
     /**
      * {@inheritDoc}
      */
     public void removeFromToolsMenu(JMenuItem menuItem) {
         removeFromMenu(toolsMenu, menuItem);
     }
 
     /**
      * {@inheritDoc}
      */
     public void addToPlacemarksMenu(JMenuItem menuItem) {
         addToMenu(placemarksMenu, menuItem, -1);
     }
 
     /**
      * {@inheritDoc}
      */
     public void addToPlacemarksMenu(JMenuItem menuItem, int index) {
         addToMenu(placemarksMenu, menuItem, index);
     }
 
     /**
      * {@inheritDoc}
      */
     public void removeFromPlacemarksMenu(JMenuItem menuItem) {
         removeFromMenu(placemarksMenu, menuItem);
     }
 
     /**
      * {@inheritDoc}
      */
     public void addToWindowMenu(JMenuItem menuItem) {
         addToMenu(windowMenu, menuItem, -1);
     }
 
     /**
      * {@inheritDoc}
      */
     public void addToWindowMenu(JMenuItem menuItem, int index) {
         addToMenu(windowMenu, menuItem, index);
     }
 
     /**
      * {@inheritDoc}
      */
     public void removeFromWindowMenu(JMenuItem menuItem) {
         removeFromMenu(windowMenu, menuItem);
     }
 
     /**
      * Set the server URL in the location field
      * @param serverURL the server URL to set
      */
     public void setServerURL(String serverURL) {
         this.serverURL = serverURL;
         serverField.setText(serverURL);
     }
 
     public void connected(boolean connected) {
         //showFPSMeter(connected);
     }
 
     /**
      * Add a camera menu item to the end of the View menu.
      *
      * @param cameraMenuItem
      */
     public void addToCameraChoices(JRadioButtonMenuItem cameraMenuItem) {
         addToCameraChoices(cameraMenuItem, -1);
     }
 
     /**
      * Add a camera menu item to the View menu at the specified index, where -1
      * adds to the end of the menu
      *
      * @param cameraMenuItem
      */
     public void addToCameraChoices(
             JRadioButtonMenuItem cameraMenuItem, int index) {
         final int indexFinal = index;
         final JRadioButtonMenuItem itemFinal = cameraMenuItem;
         SwingUtilities.invokeLater(new Runnable() {
 
             public void run() {
                 addToViewMenu(itemFinal, indexFinal);
                 cameraButtonGroup.add(itemFinal);
             }
         });
     }
 
     /**
      * Removes the specified camera choice
      * @param menuItem
      */
     public void removeFromCameraChoices(JRadioButtonMenuItem menuItem) {
         cameraButtonGroup.remove(menuItem);
         removeFromViewMenu(menuItem);
     }
 
     public void showFPSMeter(boolean visible) {
         if (visible) {
             if (chart == null) {
                 // display FPS meter
                 HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
 
                 // create fps Swing control
                 chart = new Chart(BUNDLE.getString("fps:"));
                 chart.setSampleSize(200);
                 chart.setMaxValue(30);
                 chart.setPreferredSize(new Dimension(200, 34));
 
                 // create HUD control panel
                 fpsComponent = mainHUD.createComponent(chart);
                 fpsComponent.setDecoratable(false);
                 fpsComponent.setPreferredLocation(Layout.SOUTHEAST);
 
                 // add HUD control panel to HUD
                 mainHUD.addComponent(fpsComponent);
 
                 removeFrameRateListener(frameRateListener);
                 frameRateListener = addFrameRateListener(desiredFrameRate);
             }
         } else {
             removeFrameRateListener(frameRateListener);
         }
         fpsComponent.setVisible(visible);
         fpsMI.setSelected(visible);
     }
 
     public FrameRateListener addFrameRateListener(int frameRate) {
         FrameRateListener listener = new FrameRateListener() {
 
             public void currentFramerate(float fps) {
                 if (chart != null) {
                     chart.setValue(fps);
                 }
             }
         };
         ClientContextJME.getWorldManager().getRenderManager().
                 setFrameRateListener(listener, frameRate);
 
         return listener;
     }
 
     public void removeFrameRateListener(FrameRateListener listener) {
         if (listener != null) {
             ClientContextJME.getWorldManager().getRenderManager().
                     setFrameRateListener(null, desiredFrameRate);
             frameRateListener = null;
         }
     }
 
     public void addServerURLListener(ServerURLListener listener) {
         serverListener = listener;
     }
 
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         serverPanel = new javax.swing.JPanel();
         serverLabel = new javax.swing.JLabel();
         serverField = new javax.swing.JTextField();
         goButton = new javax.swing.JButton();
         centerPanel = new javax.swing.JPanel();
         mainMenuBar = new javax.swing.JMenuBar();
         fileMenu = new javax.swing.JMenu();
         editMenu = new javax.swing.JMenu();
         viewMenu = new javax.swing.JMenu();
         insertMenu = new javax.swing.JMenu();
         placemarksMenu = new javax.swing.JMenu();
         toolsMenu = new javax.swing.JMenu();
         windowMenu = new javax.swing.JMenu();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
         addWindowListener(new java.awt.event.WindowAdapter() {
             public void windowClosing(java.awt.event.WindowEvent evt) {
                 formWindowClosing(evt);
             }
         });
 
         serverPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
         serverPanel.setPreferredSize(new java.awt.Dimension(692, 35));
 
         java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/jdesktop/wonderland/client/jme/resources/bundle"); // NOI18N
         serverLabel.setText(bundle.getString("Location:")); // NOI18N
 
         serverField.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 serverFieldActionPerformed(evt);
             }
         });
 
         goButton.setText(bundle.getString("Go!")); // NOI18N
         goButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 goButtonActionPerformed(evt);
             }
         });
 
         org.jdesktop.layout.GroupLayout serverPanelLayout = new org.jdesktop.layout.GroupLayout(serverPanel);
         serverPanel.setLayout(serverPanelLayout);
         serverPanelLayout.setHorizontalGroup(
             serverPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(serverPanelLayout.createSequentialGroup()
                 .add(serverLabel)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(serverField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 549, Short.MAX_VALUE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(goButton))
         );
         serverPanelLayout.setVerticalGroup(
             serverPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(serverPanelLayout.createSequentialGroup()
                 .add(serverPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(serverLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 29, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(serverPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                         .add(serverField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 29, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                         .add(goButton)))
                 .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         getContentPane().add(serverPanel, java.awt.BorderLayout.NORTH);
 
         centerPanel.setLayout(new java.awt.BorderLayout());
         getContentPane().add(centerPanel, java.awt.BorderLayout.CENTER);
 
         fileMenu.setText(bundle.getString("File")); // NOI18N
         mainMenuBar.add(fileMenu);
 
         editMenu.setText(bundle.getString("Edit")); // NOI18N
         mainMenuBar.add(editMenu);
 
         java.util.ResourceBundle bundle1 = java.util.ResourceBundle.getBundle("org/jdesktop/wonderland/client/jme/Bundle"); // NOI18N
         viewMenu.setText(bundle1.getString("MainFrameImpl.viewMenu.text")); // NOI18N
         mainMenuBar.add(viewMenu);
 
         insertMenu.setText(bundle1.getString("MainFrameImpl.insertMenu.text")); // NOI18N
         mainMenuBar.add(insertMenu);
 
         placemarksMenu.setText(bundle1.getString("MainFrameImpl.placemarksMenu.text")); // NOI18N
         mainMenuBar.add(placemarksMenu);
 
         toolsMenu.setText(bundle.getString("Tools")); // NOI18N
         mainMenuBar.add(toolsMenu);
 
         windowMenu.setText(bundle1.getString("MainFrameImpl.windowMenu.text")); // NOI18N
         mainMenuBar.add(windowMenu);
 
         setJMenuBar(mainMenuBar);
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
 private void serverFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_serverFieldActionPerformed
     String serverText = serverField.getText();
     if (serverText != null && serverText.equals("") == false) {
         goButton.doClick();
     }
 }//GEN-LAST:event_serverFieldActionPerformed
 
 private void goButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_goButtonActionPerformed
     LOGGER.info("[MainFrameImp] GO! " + serverField.getText());
 
     if (serverListener != null) {
         serverListener.serverURLChanged(serverField.getText());
     }
 }//GEN-LAST:event_goButtonActionPerformed
 
 private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
     ClientContextJME.getWorldManager().shutdown();
 }//GEN-LAST:event_formWindowClosing
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JPanel centerPanel;
     private javax.swing.JMenu editMenu;
     private javax.swing.JMenu fileMenu;
     private javax.swing.JButton goButton;
     private javax.swing.JMenu insertMenu;
     private javax.swing.JMenuBar mainMenuBar;
     private javax.swing.JMenu placemarksMenu;
     private javax.swing.JTextField serverField;
     private javax.swing.JLabel serverLabel;
     private javax.swing.JPanel serverPanel;
     private javax.swing.JMenu toolsMenu;
     private javax.swing.JMenu viewMenu;
     private javax.swing.JMenu windowMenu;
     // End of variables declaration//GEN-END:variables
 }
