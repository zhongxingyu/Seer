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
 package org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer;
 
 import imi.character.CharacterEyes;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.ResourceBundle;
 import java.util.logging.Logger;
 import javax.swing.SwingUtilities;
 import org.jdesktop.wonderland.client.hud.HUD;
 import org.jdesktop.wonderland.client.hud.HUDButton;
 import org.jdesktop.wonderland.client.hud.HUDManagerFactory;
 
 /**
  * A HUD display for avatar gestures
  *
  * @author nsimpson
  */
 public class GestureHUD {
 
     private static final Logger logger = Logger.getLogger(GestureHUD.class.getName());
     private static final ResourceBundle bundle = ResourceBundle.getBundle("org/jdesktop/wonderland/modules/avatarbase/client/resources/Bundle");
     private boolean visible = false;
     private boolean showingGestures = true;
     private Map<String, String> gestureMap = new HashMap();
     private Map<String, HUDButton> buttonMap = new HashMap();
     private HUDButton showGesturesButton;
     private HUD mainHUD;
     // map gestures to column, row locations on gesture HUD
     private String[][] gestures = {
         {"Answer Cell", "0", "4"},
         {"Take Damage", "0", "3"},
         {"Public Speaking", "1", "4"},
         {"Bow", "1", "3"},
         {"Laugh", "2", "4"},
         {"Clap", "2", "3"},
         {"Cheer", "2", "2"},
         {"Left Wink", "2", "1"},
         {"Right Wink", "2", "0"},
         {"Wave", "3", "4"},
         {"Raise Hand", "3", "3"},
         {"Shake Hands", "3", "2"},
         {"Follow", "3", "1"},
         {"Yes", "4", "4"},
         {"No", "4", "3"},};
     private int leftMargin = 20;
     private int bottomMargin = 10;
     private int rowHeight = 30;
     private int columnWidth = 100;
 
     public GestureHUD() {
         setAvatarCharacter(null);
     }
 
     public void setVisible(final boolean visible) {
         SwingUtilities.invokeLater(new Runnable() {
 
             public void run() {
                 if (GestureHUD.this.visible == visible) {
                     return;
                 }
                 if (showGesturesButton == null) {
                     showGesturesButton = mainHUD.createButton("Hide Gestures");
                     showGesturesButton.setDecoratable(false);
                     showGesturesButton.setLocation(leftMargin, bottomMargin);
                     showGesturesButton.addActionListener(new ActionListener() {
 
                         public void actionPerformed(ActionEvent event) {
                             showingGestures = (showGesturesButton.getLabel().equals("Hide Gestures")) ? false : true;
                             showGesturesButton.setLabel(showingGestures ? "Hide Gestures" : "Show Gestures");
                             showGestureButtons(showingGestures);
                         }
                     });
                     mainHUD.addComponent(showGesturesButton);
                 }
                 GestureHUD.this.visible = visible;
                 showGesturesButton.setVisible(visible);
                 showGestureButtons(visible && showingGestures);
             }
         });
     }
 
     public void showGestureButtons(boolean show) {
         for (String gesture : buttonMap.keySet()) {
             HUDButton button = buttonMap.get(gesture);
             button.setVisible(show);
         }
     }
 
     public boolean isVisible() {
         return visible;
     }
 
     public void setAvatarCharacter(final WlAvatarCharacter avatar) {
         SwingUtilities.invokeLater(new Runnable() {
 
             public void run() {
                 if (mainHUD == null) {
                     mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
 
                 }
 
                 // remove existing gesture buttons
                 for (String name : buttonMap.keySet()) {
                     HUDButton button = buttonMap.get(name);
                     button.setVisible(false);
                 // REMIND: really delete button
                 }
                 buttonMap.clear();
                 gestureMap.clear();
 
                 // If we don't have an avatar, then just return
                 if (avatar == null) {
                     return;
                 }
 
                 // Otherwise, figure out which gestures are supported. We want to
                 // remove the "Male_" or "Female_" for now.
                 for (String action : avatar.getAnimationNames()) {
                     String name = action;
                     if (action.startsWith("Male_") == true) {
                         name = name.substring(5);
                     } else if (action.startsWith("Female_") == true) {
                         name = name.substring(7);
                     }
                     // add to a map of user-friendly names to avatar animations
                     // e.g., "Shake Hands" -> "Male_ShakeHands"
                     gestureMap.put(bundle.getString(name), action);
                 }
 
                 // Add the left and right wink
                 gestureMap.put("Left Wink", "LeftWink");
                 gestureMap.put("Right Wink", "RightWink");
 
                 // Create HUD buttons for each of the actions
                 for (String name : gestureMap.keySet()) {
                     int row = 0;
                     int column = 0;
 
                     // find the button row, column position for this gesture
                     for (String[] gesture : gestures) {
                         if (gesture[0].equals(name)) {
                             column = Integer.valueOf(gesture[1]);
                             row = Integer.valueOf(gesture[2]);
                             HUDButton button = mainHUD.createButton(name);
                             button.setDecoratable(false);
                             button.setLocation(leftMargin + column * columnWidth, bottomMargin + row * rowHeight);
                             mainHUD.addComponent(button);
                             buttonMap.put(name, button);
 
                             button.addActionListener(new ActionListener() {
 
                                 public void actionPerformed(ActionEvent event) {
                                     String action = gestureMap.get(event.getActionCommand());
                                     logger.info("playing animation: " + event.getActionCommand());
                                     if (action.equals("LeftWink") == true) {
                                         CharacterEyes eyes = avatar.getEyes();
                                         eyes.wink(false);
                                     } else if (action.equals("RightWink") == true) {
                                         CharacterEyes eyes = avatar.getEyes();
                                         eyes.wink(true);
                                     } else {
                                         avatar.playAnimation(action);
                                     }
                                 }
                             });
                             break;
                         }
                     }
                 }
                 setVisible(true);
             }
         });
     }
 }
