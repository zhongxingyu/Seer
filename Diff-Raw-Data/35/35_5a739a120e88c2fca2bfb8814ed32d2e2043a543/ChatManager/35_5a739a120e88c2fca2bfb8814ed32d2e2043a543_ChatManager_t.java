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
 package org.jdesktop.wonderland.modules.textchat.client;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.lang.ref.WeakReference;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import org.jdesktop.wonderland.client.comms.ConnectionFailureException;
 import org.jdesktop.wonderland.client.comms.WonderlandSession;
 import org.jdesktop.wonderland.client.jme.JmeClientMain;
 import org.jdesktop.wonderland.client.login.ServerSessionManager;
 import org.jdesktop.wonderland.client.login.SessionLifecycleListener;
 import org.jdesktop.wonderland.modules.textchat.client.TextChatConnection.TextChatListener;
 
 /**
  * Manages all of the Text Chat windows for the client.
  * 
  * @author Jordan Slott <jslott@dev.java.net>
  */
 public class ChatManager implements TextChatListener {
 
     private static Logger logger = Logger.getLogger(ChatManager.class.getName());
     private WeakReference<ChatUserListJFrame> userListFrameRef = null;
     private Map<String, WeakReference<TextChatJFrame>> textChatFrameRefMap = null;
     private JMenu menu = null;
     private JMenuItem textChatMenuItem = null;
     private JMenuItem userListMenuItem = null;
     private TextChatConnection textChatConnection = null;
     private String localUserName = null;
     private ServerSessionManager loginInfo = null;
     private SessionLifecycleListener sessionListener = null;
 
     /** Constructor */
     public ChatManager(final ServerSessionManager loginInfo) {
         this.loginInfo = loginInfo;
         textChatFrameRefMap = new HashMap();
 
         // Create a new Chat menu underneath the "Tools" menu
         menu = new JMenu("Chat");
 
         // First create the text chat frame and keep a weak reference to it so
         // that it gets garbage collected
         final TextChatJFrame textChatJFrame = new TextChatJFrame();
         final WeakReference<TextChatJFrame> frameRef = new WeakReference(textChatJFrame);
         textChatFrameRefMap.put("", frameRef);
 
         // Add the global text chat frame to the menu item. Listen for when it
         // is selected or de-selected and show/hide the frame as appropriate.
         textChatMenuItem = new JMenuItem("Text Chat All");
         textChatMenuItem.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 JFrame textChatJFrame = frameRef.get();
                if (textChatJFrame.isVisible() == false) {
                    textChatJFrame.setVisible(true);
                }
             }
         });
         textChatMenuItem.setEnabled(false);
         menu.add(textChatMenuItem);
 
         // Add the user list frame to the menu item. Listen for when it is
         // selected or de-selected and show/hide the frame as appropriate.
         userListMenuItem = new JMenuItem("Private Text Chat");
         userListMenuItem.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 JFrame userListJFrame = userListFrameRef.get();
                if (userListJFrame.isVisible() == false) {
                    userListJFrame.setVisible(true);
                }
             }
         });
         userListMenuItem.setEnabled(false);
         menu.add(userListMenuItem);
 
        // Add the Chat menu item to the "Window" menu
         JmeClientMain.getFrame().addToWindowMenu(menu, 2);
 
         // Wait for a primary session to become active. When it does, then
         // we enable the menu items and set the primary sessions on their
         // objects.
         sessionListener = new SessionLifecycleListener() {
             public void sessionCreated(WonderlandSession session) {
                 // Do nothing for now
             }
 
             public void primarySession(WonderlandSession session) {
                 setPrimarySession(session);
             }
         };
         loginInfo.addLifecycleListener(sessionListener);
 
         // XXX Check if we already have primary session, should be handled
         // by addLifecycleListener
         if (loginInfo.getPrimarySession() != null) {
             setPrimarySession(loginInfo.getPrimarySession());
         }
     }
 
     /**
      * Unregister and menus we have created, etc.
      */
     public void unregister() {
         // remove the session listener
         loginInfo.removeLifecycleListener(sessionListener);
 
         // Remove the menu item
         JmeClientMain.getFrame().removeFromWindowMenu(menu);
     }
 
     /**
      * Sets the primary session, when it is made the primary session. This
      * turns on everything: enables the menu items, displays the global chat
      * dialog.
      */
     private void setPrimarySession(WonderlandSession session) {
         // Capture the local user name for later use
         localUserName = session.getUserID().getUsername();
         
         // Create a new custom connection to receive text chats. Register a
         // listener that handles new text messages. Will display them in the
         // window.
         textChatConnection = new TextChatConnection();
         textChatConnection.addTextChatListener(this);
 
         // Open the text chat connection. If unsuccessful, then log an error
         // and return.
         try {
             textChatConnection.connect(session);
         } catch (ConnectionFailureException excp) {
             logger.log(Level.WARNING, "Unable to establish a connection to " +
                     "the chat connection.", excp);
             return;
         }
 
         // Create the user list frame and keep a weak reference to it so that it
         // gets garbage collected
         final ChatUserListJFrame userListJFrame =
                 new ChatUserListJFrame(session.getUserID(), this);
         userListFrameRef = new WeakReference(userListJFrame);
 
         // Otherwise, enable all of the GUI elements. First enable the user list
         // frame by setting its session
         userListJFrame.setPrimarySession(session);
         userListMenuItem.setEnabled(true);
 
         // Next, for the global chat, set its information and make it visible
         // initially.
         TextChatJFrame textChatJFrame = textChatFrameRefMap.get("").get();
         textChatJFrame.setActive(textChatConnection, localUserName, "");
         textChatMenuItem.setEnabled(true);
         textChatMenuItem.setSelected(true);
         textChatJFrame.setVisible(true);
     }
 
     /**
      * Creates a new text chat window, given the remote participants user name
      * and displays it.
      *
      * @param remoteUser The remote participants user name
      */
     public void startChat(String remoteUser) {
         // Do all of this synchronized. This makes sure that multiple text chat
         // window aren't create if a local user clicks to create a new text
         // chat and a message comes in for that remote user.
         synchronized (textChatFrameRefMap) {
             // Check to see if the text chat window already exists. If so, then
             // we do nothing and return.
             WeakReference<TextChatJFrame> ref = textChatFrameRefMap.get(remoteUser);
             if (ref != null) {
                 return;
             }
 
             // Otherwise, create the frame, add it to the map, and display
             TextChatJFrame frame = new TextChatJFrame();
             final String userKey = remoteUser;
             frame.addWindowListener(new WindowAdapter() {
                 @Override
                 public void windowClosing(WindowEvent e) {
                     // Remove from the map which will let it garbage collect
                     synchronized (textChatFrameRefMap) {
                         e.getWindow().dispose();
                         textChatFrameRefMap.remove(userKey);
                     }
                 }
             });
             textChatFrameRefMap.put(remoteUser, new WeakReference(frame));
             frame.setActive(textChatConnection, localUserName, remoteUser);
             frame.setVisible(true);
         }
     }
 
     /**
      * Deactivates the text chat given the remote user's name, if such a frame
      * exists. Displays a message in the window and turns off its GUI.
      *
      * @param remoteUser The remote participants user name
      */
     public void deactivateChat(String remoteUser) {
         // Do all of this synchronized, so that we do not interfere with the
         // code to create chats
         synchronized (textChatFrameRefMap) {
             // Check to see if the text chat window exists. If not, then do
             // nothing.
             WeakReference<TextChatJFrame> ref = textChatFrameRefMap.get(remoteUser);
             if (ref == null) {
                 return;
             }
             TextChatJFrame frame = ref.get();
             frame.deactivate();
         }
     }
 
     /**
      * Re-activates the text chat given the remote user's name, if such a frame
      * exists. Displays a message in the window and turns on its GUI.
      */
     public void reactivateChat(String remoteUser) {
         // Do all of this synchronized, so that we do not interfere with the
         // code to create chats
         synchronized (textChatFrameRefMap) {
             // Check to see if the text chat window exists. If not, then do
             // nothing.
             WeakReference<TextChatJFrame> ref = textChatFrameRefMap.get(remoteUser);
             if (ref == null) {
                 return;
             }
             TextChatJFrame frame = ref.get();
             frame.reactivate();
         }
     }
 
     /**
      * @inheritDoc()
      */
     public void textMessage(String message, String fromUser, String toUser) {
         // Fetch the frame associated with the user. If the "to" user is an
         // empty string, then this is a "global" message and we fetch its
         // frame. It should exist. We always add the message, no matter whether
         // the frame is visible or not.
         if (toUser == null || toUser.equals("") == true) {
             TextChatJFrame frame = textChatFrameRefMap.get("").get();
             frame.appendTextMessage(message, fromUser);
             return;
         }
 
         // Otherwise, the "toUser" is for this specific user. We fetch the
         // frame associated with the "from" user. If it exists (which also
         // means it is visible, then add the message.
         synchronized (textChatFrameRefMap) {
             WeakReference<TextChatJFrame> ref = textChatFrameRefMap.get(fromUser);
             if (ref != null) {
                 TextChatJFrame frame = ref.get();
                 frame.appendTextMessage(message, fromUser);
                 return;
             }
 
             // Finally, we reached here when we have a message from a specific
             // user, but the frame does not exist, and is not visible. So we
             // create it and add to the map and display it.
             TextChatJFrame frame = new TextChatJFrame();
             final String userKey = fromUser;
             frame.addWindowListener(new WindowAdapter() {
                 @Override
                 public void windowClosing(WindowEvent e) {
                     // Remove from the map which will let it garbage collect
                     synchronized (textChatFrameRefMap) {
                         e.getWindow().dispose();
                         textChatFrameRefMap.remove(userKey);
                     }
                 }
             });
             textChatFrameRefMap.put(fromUser, new WeakReference(frame));
             frame.setActive(textChatConnection, toUser, fromUser);
             frame.setVisible(true);
             frame.appendTextMessage(message, fromUser);
         }
     }
 }
