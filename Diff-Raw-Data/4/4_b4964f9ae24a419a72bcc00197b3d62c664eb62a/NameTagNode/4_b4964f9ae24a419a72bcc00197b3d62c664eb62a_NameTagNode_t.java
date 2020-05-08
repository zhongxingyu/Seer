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
 
 import com.jme.math.Matrix3f;
 import com.jme.math.Vector3f;
 import com.jme.scene.Node;
 import com.jme.scene.Spatial;
 
 import org.jdesktop.mtgame.processor.WorkProcessor.WorkCommit;
 
 import org.jdesktop.wonderland.client.jme.utils.TextLabel2D;
 
 import org.jdesktop.wonderland.client.jme.ClientContextJME;
 
 import java.awt.Color;
 import java.awt.Font;
 
 import java.util.HashMap;
 import java.util.logging.Logger;
 
 /**
  * TODO make this a component
  *
  * @author jprovino
  * @author nsimpson
  */
 public class NameTagNode extends Node {
 
     private static final Logger logger = Logger.getLogger(NameTagNode.class.getName());
 
     public enum EventType {
 
         STARTED_SPEAKING,
         STOPPED_SPEAKING,
         MUTE,
         UNMUTE,
         CHANGE_NAME,
         ENTERED_CONE_OF_SILENCE,
         EXITED_CONE_OF_SILENCE,
         HIDE,
         SMALL_FONT,
         REGULAR_FONT,
         LARGE_FONT
     }
     // colors
     public static final Color SPEAKING_COLOR = Color.RED;
     public static final Color NOT_SPEAKING_COLOR = Color.WHITE;
     public static final Color CONE_OF_SILENCE_COLOR = Color.BLACK;
     private Color foregroundColor = NOT_SPEAKING_COLOR;
     private Color backgroundColor = new Color(0f, 0f, 0f);
     // fonts
     public static final String DEFAULT_FONT_NAME = "SANS_SERIF";
     public static final String DEFAULT_FONT_NAME_TYPE = "PLAIN";
     public static final String DEFAULT_FONT_ALIAS_TYPE = "ITALIC";
     public static final int DEFAULT_FONT_SIZE = 32;
     public static final Font REAL_NAME_FONT =
             fontDecode(DEFAULT_FONT_NAME, DEFAULT_FONT_NAME_TYPE, DEFAULT_FONT_SIZE);
     public static final Font ALIAS_NAME_FONT =
             fontDecode(DEFAULT_FONT_NAME, DEFAULT_FONT_ALIAS_TYPE, DEFAULT_FONT_SIZE);
     private int fontSize = DEFAULT_FONT_SIZE;
     private Font currentFont = REAL_NAME_FONT;
     // name tag heights
     public static final float SMALL_SIZE = 0.2f;
     public static final float REGULAR_SIZE = 0.3f;
     public static final float LARGE_SIZE = 0.5f;
     private float currentHeight = REGULAR_SIZE;
     // status indicators
     public static final String LEFT_MUTE = "[";
     public static final String RIGHT_MUTE = "]";
     public static final String SPEAKING = "...";
     private boolean inConeOfSilence;
     private boolean isSpeaking;
     private boolean isMuted;
     private boolean labelHidden;
     //
     private boolean done;
     private TextLabel2D label = null;
     private final float heightAbove;
     private String name;
     private Spatial q;
     private String usernameAlias;
     private boolean visible;
     private static HashMap<String, NameTagNode> nameTagMap = new HashMap();
 
     private static Font fontDecode(String fontName, String fontType, int fontSize) {
         return Font.decode(fontName + " " + fontType + " " + fontSize);
     }
 
     public NameTagNode(String name, float heightAbove) {
         this.name = name;
         this.heightAbove = heightAbove;
         visible = true;
 
         nameTagMap.put(name, this);
 
         setLabelText(name);
     }
 
     public void done() {
         if (done) {
             return;
         }
 
         done = true;
 
         nameTagMap.remove(name);
 
         detachChild(q);
     }
 
     public static String getDisplayName(String name, boolean isSpeaking, boolean isMuted) {
         if (isMuted) {
             return LEFT_MUTE + name + RIGHT_MUTE;
         }
 
         if (isSpeaking) {
             return name + SPEAKING;
         }
 
         return name;
     }
 
     public static String getUsername(String name) {
         String s = name.replaceAll("\\" + LEFT_MUTE, "");
 
         s = s.replaceAll("\\" + RIGHT_MUTE, "");
 
         return s.replaceAll("\\" + SPEAKING, "");
     }
 
     public void setForegroundColor(Color foregroundColor) {
         this.foregroundColor = foregroundColor;
     }
 
     public Color getForegroundColor() {
         return foregroundColor;
     }
 
     public void setBackgroundColor(Color backgroundColor) {
         this.backgroundColor = backgroundColor;
     }
 
     public Color getBackgroundColor() {
         return backgroundColor;
     }
 
     public void setLabelText(String labelText) {
         this.name = labelText;
     }
 
     public void setFont(Font font) {
         currentFont = font;
     }
 
     public void setFontSize(int fontSize) {
         this.fontSize = fontSize;
     }
 
     public void setHeight(float height) {
         currentHeight = height;
     }
 
     public void setVisible(boolean visible) {
         this.visible = visible;
         if (visible) {
             updateLabel(getDisplayName(name, isSpeaking, isMuted));
         } else {
             removeLabel();
         }
     }
 
     /**
      * Returns whether the name tag is visible. 
      */
     public boolean isVisible() {
         return visible;
     }
 
     public static void setMyNameTag(EventType eventType, String username,
             String usernameAlias) {
 
         NameTagNode nameTag = nameTagMap.get(username);
 
         if (nameTag == null) {
             logger.warning("can't find name tag for " + username);
             return;
         }
 
         nameTag.setNameTag(eventType, username, usernameAlias);
     }
 
     public static void setOtherNameTags(EventType eventType, String username, String usernameAlias) {
         String[] keys = nameTagMap.keySet().toArray(new String[0]);
 
         for (int i = 0; i < keys.length; i++) {
             if (keys[i].equals(username)) {
                 continue;
             }
 
             NameTagNode nameTag = nameTagMap.get(keys[i]);
             logger.fine("set other name tags: " + eventType + ", username: " + username + ", usernameAlias: " + usernameAlias);
             nameTag.setNameTag(eventType, username, usernameAlias);
         }
     }
 
     public void setNameTag(EventType eventType, String username, String alias) {
         setNameTag(eventType, username, alias, foregroundColor, currentFont);
     }
 
     public synchronized void setNameTag(EventType eventType, String username, String alias,
             Color foregroundColor, Font font) {
 
         logger.fine("set name tag: " + eventType + ", username: " + username + ", alias: " + alias + ", color: " + foregroundColor + ", font: " + font);
 
         switch (eventType) {
             case HIDE:
                 labelHidden = true;
                 removeLabel();
                 return;
 
             case SMALL_FONT:
                 labelHidden = false;
                 removeLabel();
                 setHeight(SMALL_SIZE);
                 break;
 
             case REGULAR_FONT:
                 labelHidden = false;
                 removeLabel();
                 setHeight(REGULAR_SIZE);
                 break;
 
             case LARGE_FONT:
                 labelHidden = false;
                 removeLabel();
                 setHeight(LARGE_SIZE);
                 break;
 
             case ENTERED_CONE_OF_SILENCE:
                 inConeOfSilence = true;
                 setForegroundColor(CONE_OF_SILENCE_COLOR);
                 break;
 
             case EXITED_CONE_OF_SILENCE:
                 inConeOfSilence = false;
                 setForegroundColor(NOT_SPEAKING_COLOR);
                 break;
 
             case STARTED_SPEAKING:
                 isSpeaking = true;
                 setForegroundColor(SPEAKING_COLOR);
                 break;
 
             case STOPPED_SPEAKING:
                 isSpeaking = false;
                 setForegroundColor(NOT_SPEAKING_COLOR);
                 break;
 
             case MUTE:
                 isMuted = true;
                 setForegroundColor(NOT_SPEAKING_COLOR);
                 removeLabel();
                 break;
 
             case UNMUTE:
                 isMuted = false;
                 setForegroundColor(NOT_SPEAKING_COLOR);
                 break;
 
             case CHANGE_NAME:
                 removeLabel();
                 usernameAlias = alias;
                 break;
 
             default:
                 logger.warning("unhandled name tag event type: " + eventType);
                 break;
         }
 
        if ((alias != null) && !alias.equals(username)) {
             // displaying an alias
             setFont(ALIAS_NAME_FONT);
            usernameAlias = alias;
             updateLabel(getDisplayName(usernameAlias, isSpeaking, isMuted));
         } else {
             // displaying user name
             setFont(REAL_NAME_FONT);
             updateLabel(getDisplayName(name, isSpeaking, isMuted));
         }
 
         if (foregroundColor != null) {
             setForegroundColor(foregroundColor);
         }
     }
 
     private void removeLabel() {
         if (label != null) {
             detachChild(label);
             label = null;
         }
     }
 
     private void updateLabel(final String displayName) {
         if (labelHidden) {
             return;
         }
         ClientContextJME.getSceneWorker().addWorker(new WorkCommit() {
 
             public void commit() {
                 if (visible) {
                     if (label == null) {
                         label = new TextLabel2D(displayName, foregroundColor, backgroundColor, currentHeight, true, currentFont);
                         label.setLocalTranslation(0, heightAbove, 0);
 
                         Matrix3f rot = new Matrix3f();
                         rot.fromAngleAxis((float) Math.PI, new Vector3f(0f, 1f, 0f));
                         label.setLocalRotation(rot);
 
                         attachChild(label);
                     } else {
                         label.setText(displayName, foregroundColor, backgroundColor);
                     }
                     ClientContextJME.getWorldManager().addToUpdateList(NameTagNode.this);
                 }
             }
         });
     }
 }
