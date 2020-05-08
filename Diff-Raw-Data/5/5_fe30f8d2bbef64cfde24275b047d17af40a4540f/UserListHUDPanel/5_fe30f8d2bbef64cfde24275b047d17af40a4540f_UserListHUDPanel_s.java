 /*
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
 
 /*
  * UserListHUDPanel.java
  *
  * Created on Jun 16, 2009, 11:57:10 AM
  */
 package org.jdesktop.wonderland.modules.audiomanager.client;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Font;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.logging.Logger;
 import javax.swing.DefaultListModel;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.ListCellRenderer;
 import javax.swing.DefaultListCellRenderer;
 import javax.swing.ImageIcon;
 import org.jdesktop.wonderland.client.cell.Cell;
 import org.jdesktop.wonderland.client.cell.ChannelComponent;
 import org.jdesktop.wonderland.client.hud.CompassLayout.Layout;
 import org.jdesktop.wonderland.client.hud.HUD;
 import org.jdesktop.wonderland.client.hud.HUDComponent;
 import org.jdesktop.wonderland.client.hud.HUDComponentEvent;
 import org.jdesktop.wonderland.client.hud.HUDComponentEvent.ComponentEventType;
 import org.jdesktop.wonderland.client.hud.HUDComponentListener;
 import org.jdesktop.wonderland.client.hud.HUDManagerFactory;
 import org.jdesktop.wonderland.client.softphone.SoftphoneControlImpl;
 import org.jdesktop.wonderland.common.cell.CellID;
 import org.jdesktop.wonderland.modules.audiomanager.common.VolumeUtil;
 import org.jdesktop.wonderland.modules.audiomanager.common.messages.AudioVolumeMessage;
 import org.jdesktop.wonderland.modules.audiomanager.common.messages.ChangeUsernameAliasMessage;
 import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.NameTagNode;
 import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManager;
 import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManagerListener;
 import org.jdesktop.wonderland.modules.presencemanager.common.PresenceInfo;
 
 import org.jdesktop.wonderland.client.comms.WonderlandSession;
 import org.jdesktop.wonderland.client.softphone.SoftphoneListener;
 
 /**
  *
  * @author nsimpson
  */
 public class UserListHUDPanel extends javax.swing.JPanel implements PresenceManagerListener,
         VolumeChangeListener, UsernameAliasChangeListener {
 
     private static final Logger logger = Logger.getLogger(UserListHUDPanel.class.getName());
     private Cell cell;
     private ChannelComponent channelComp;
     private PresenceManager pm;
     private PresenceInfo presenceInfo;
     private Map<PresenceInfo, HUDComponent> changeNameMap = Collections.synchronizedMap(new HashMap<PresenceInfo, HUDComponent>());
     private ConcurrentHashMap<String, String> usernameMap = new ConcurrentHashMap<String, String>();
     private HUDComponent namePropertiesHUDComponent;
     private String[] selection = null;
     private DefaultListModel userListModel;
     private ArrayList<PresenceInfo> usersInRange = new ArrayList();
     private int outOfRangeIndex = 0;
     private Font inRangeFont = new Font("SansSerif", Font.PLAIN, 13);
     private Font outOfRangeFont = new Font("SansSerif", Font.PLAIN, 13);
     private ImageIcon mutedIcon;
     private ImageIcon unmutedIcon;
     private ImageIcon upIcon;
     private ImageIcon downIcon;
     private AudioManagerClient client;
     private WonderlandSession session;
     private HUDComponent userListHUDComponent;
 
     public UserListHUDPanel(AudioManagerClient client, WonderlandSession session,
             PresenceManager pm, Cell cell) {
 
         this.client = client;
         this.session = session;
 
         this.pm = pm;
         this.cell = cell;
 
         initComponents();
 
         mutedIcon = new ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/audiomanager/client/resources/UserListMuteOn24x24.png"));
         unmutedIcon = new ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/audiomanager/client/resources/UserListMuteOff24x24.png"));
         upIcon = new ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/audiomanager/client/resources/upArrow23x10.png"));
         downIcon = new ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/audiomanager/client/resources/downArrow23x10.png"));
 
         userListModel = new DefaultListModel();
         userList.setModel(userListModel);
         userList.setCellRenderer(new UserListCellRenderer());
 
         textChatButton.setEnabled(false);
         voiceChatButton.setEnabled(false);
 
         channelComp = cell.getComponent(ChannelComponent.class);
 
         pm.addPresenceManagerListener(this);
         presenceInfo = pm.getPresenceInfo(cell.getCellID());
 
         if (presenceInfo == null) {
             logger.warning("no presence info for cell " + cell.getCellID());
             return;
         }
         controlPanel.setVisible(false);
         volumeSlider.setEnabled(false);
         editButton.setEnabled(false);
         propertiesButton.setEnabled(true);
 
         SoftphoneControlImpl sc = SoftphoneControlImpl.getInstance();
         sc.addSoftphoneListener(new SoftphoneListener() {
 
             public void softphoneVisible(boolean isVisible) {
             }
 
             public void softphoneMuted(boolean muted) {
                 updateMuteButton();
             }
 
             public void softphoneConnected(boolean connected) {
             }
 
             public void softphoneExited() {
             }
 
             public void microphoneGainTooHigh() {
             }
         });
     }
 
     public void setHUDComponent(HUDComponent userListHUDComponent) {
         this.userListHUDComponent = userListHUDComponent;
     }
 
     public void changeUsernameAlias(PresenceInfo info) {
         session.send(client, new ChangeUsernameAliasMessage(info.cellID, info));
     }
 
     public void updateMuteButton() {
         SoftphoneControlImpl sc = SoftphoneControlImpl.getInstance();
 
         if (sc.isMuted()) {
             muteButton.setIcon(mutedIcon);
         } else {
             muteButton.setIcon(unmutedIcon);
         }
     }
 
     public void done() {
         setVisible(false);
     }
 
     private boolean isMe(PresenceInfo info) {
         if ((presenceInfo == null) && (cell != null) && (pm != null)) {
             presenceInfo = pm.getPresenceInfo(cell.getCellID());
         }
         return ((presenceInfo != null) && presenceInfo.equals(info));
     }
 
     private boolean isInRange(PresenceInfo info) {
         return isMe(info) || (usersInRange.contains(info));
     }
 
     public synchronized void setUserList() {
         PresenceInfo[] presenceInfoList = pm.getAllUsers();
 
         for (int i = 0; i < presenceInfoList.length; i++) {
             PresenceInfo info = presenceInfoList[i];
             if (info.callID == null) {
                 // It's a virtual player, skip it.
                 continue;
             }
 
             String username = info.userID.getUsername();
             String displayName = NameTagNode.getDisplayName(info.usernameAlias, info.isSpeaking,
                     info.isMuted);
 
             boolean inRange = isInRange(info);
             //displayName = (inRange ? "\u25B8 " : "") + displayName;
 
             int desiredPosition;
             boolean newUser = !usernameMap.containsKey(username);
 
             if (newUser) {
                 if (isMe(info)) {
                     // this user always goes at the top of the list
                     desiredPosition = 0;
                 } else {
                     // some other user
                     if (inRange) {
                         // new in range user
                         // put them at the bottom of the in range list
                         desiredPosition = outOfRangeIndex;
                     } else {
                         // new out of range user
                         // add them to the end of the out of range list (bottom of user list)
                         desiredPosition = userListModel.size();
                     }
                 }
                 logger.finest("inserting new user: " + username + " at position: " + desiredPosition);
                 if (inRange) {
                     outOfRangeIndex++;
                 }
 
                 usernameMap.put(username, displayName);
                 userListModel.insertElementAt(displayName, desiredPosition);
             } else {
                 // existing user
                 // update entry in list model
                 String oldName = usernameMap.get(username);
                 int currentIndex = userListModel.indexOf(usernameMap.get(username));
                 boolean wasInRange = currentIndex < outOfRangeIndex;
 
                 if (inRange != wasInRange) {
                     boolean reselect = userList.isSelectedIndex(currentIndex);
                     logger.finest("user in range: " + username + ": " + wasInRange + " -> " + inRange);
                     logger.finest("removing: " + username + " at " + currentIndex);
                     userListModel.removeElementAt(currentIndex);
 
                     if (wasInRange) {
                         // user has gone out of range
                         // add them to the end of the out of range list (at the bottom of user list)
                         outOfRangeIndex--;
                         desiredPosition = outOfRangeIndex;
                         logger.finest("inserting out of range: " + username + " at " + desiredPosition);
                     } else {
                         // user has come in range
                         // put them at the bottom of the in range list
                         desiredPosition = outOfRangeIndex;
                         outOfRangeIndex++;
                         logger.finest("inserting in range: " + username + " at " + desiredPosition);
                     }
                     userListModel.insertElementAt(displayName, desiredPosition);
                     if (reselect) {
                         userList.setSelectedIndex(desiredPosition);
                     }
                 } else {
                     // user range didn't change
                     desiredPosition = currentIndex;
                 }
                 // update name
                 if (!displayName.equals(oldName)) {
                     logger.finest("name change: " + oldName + " -> " + displayName);
                     usernameMap.replace(username, displayName);
                     userListModel.setElementAt(displayName, desiredPosition);
                 }
             }
         }
 
         // search for removed users
         Iterator<String> iter = usernameMap.keySet().iterator();
         while (iter.hasNext()) {
             // for each user previously displayed...
             String username = (String) iter.next();
             boolean found = false;
 
             // check if user is in current presence list
             for (int i = 0; i < presenceInfoList.length; i++) {
                 PresenceInfo info = presenceInfoList[i];
                 if (username.equals(info.userID.getUsername())) {
                     found = true;
                     break;
                 }
             }
             if (!found) {
                 // user is no longer present, remove them from the user list
                 logger.finest("removing user: " + username);
                 int index = userListModel.indexOf(usernameMap.get(username));
                 boolean wasInRange = index < outOfRangeIndex;
 
                 userListModel.removeElement(usernameMap.get(username));
                 usernameMap.remove(username);
                 if (wasInRange) {
                     outOfRangeIndex--;
                 }
             }
         }
         //logger.finest("map: " + usernameMap);
         //logger.finest("out of range index: " + outOfRangeIndex);
     }
 
     private class UserListCellRenderer implements ListCellRenderer {
 
         protected DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();
 
         public Component getListCellRendererComponent(JList list, Object value, int index,
                 boolean isSelected, boolean cellHasFocus) {
             JLabel renderer = (JLabel) defaultRenderer.getListCellRendererComponent(list, value, index,
                     isSelected, cellHasFocus);
             if (index < outOfRangeIndex) {
                 renderer.setFont(inRangeFont);
                 renderer.setForeground(Color.BLUE);
             } else {
                 renderer.setFont(outOfRangeFont);
                 renderer.setForeground(Color.BLACK);
             }
             return renderer;
         }
     }
 
     public void presenceInfoChanged(PresenceInfo info, ChangeType type) {
         if (type.equals(ChangeType.USER_IN_RANGE)) {
             logger.fine("user in range:  " + info);
             usersInRange.add(info);
         } else if (type.equals(ChangeType.USER_OUT_OF_RANGE)) {
             logger.fine("user out of range:  " + info);
             usersInRange.remove(info);
         }
 
         setUserList();
     }
 
     public void usernameAliasChanged(PresenceInfo info) {
         setUserList();
     }
 
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         controlPanel = new javax.swing.JPanel();
         muteButton = new javax.swing.JButton();
         textChatButton = new javax.swing.JButton();
         voiceChatButton = new javax.swing.JButton();
         phoneButton = new javax.swing.JButton();
         editButton = new javax.swing.JButton();
         propertiesButton = new javax.swing.JButton();
         volumeLabel = new javax.swing.JLabel();
         volumeSlider = new javax.swing.JSlider();
         userListScrollPane = new javax.swing.JScrollPane();
         userList = new javax.swing.JList();
         jPanel1 = new javax.swing.JPanel();
         panelToggleButton = new javax.swing.JButton();
 
         setPreferredSize(new java.awt.Dimension(179, 300));
 
         muteButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/audiomanager/client/resources/UserListMuteOff24x24.png"))); // NOI18N
         muteButton.setToolTipText("Mute");
         muteButton.setBorderPainted(false);
         muteButton.setMargin(new java.awt.Insets(0, -4, 0, -4));
         muteButton.setMaximumSize(new java.awt.Dimension(24, 24));
         muteButton.setMinimumSize(new java.awt.Dimension(24, 24));
         muteButton.setPreferredSize(new java.awt.Dimension(24, 24));
         muteButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 muteButtonActionPerformed(evt);
             }
         });
 
         textChatButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/audiomanager/client/resources/UserListChatText24x24.png"))); // NOI18N
         textChatButton.setToolTipText("Text Chat");
         textChatButton.setBorderPainted(false);
         textChatButton.setMargin(new java.awt.Insets(0, -4, 0, -4));
         textChatButton.setMaximumSize(new java.awt.Dimension(24, 24));
         textChatButton.setMinimumSize(new java.awt.Dimension(24, 24));
         textChatButton.setPreferredSize(new java.awt.Dimension(24, 24));
         textChatButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 textChatButtonActionPerformed(evt);
             }
         });
 
         voiceChatButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/audiomanager/client/resources/UserListChatVoice24x24.png"))); // NOI18N
         voiceChatButton.setToolTipText("Voice Chat");
         voiceChatButton.setMaximumSize(new java.awt.Dimension(24, 24));
         voiceChatButton.setMinimumSize(new java.awt.Dimension(24, 24));
         voiceChatButton.setPreferredSize(new java.awt.Dimension(24, 24));
         voiceChatButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 voiceChatButtonActionPerformed(evt);
             }
         });
 
         phoneButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/audiomanager/client/resources/UserListPhone24x24.png"))); // NOI18N
         phoneButton.setToolTipText("Properties");
         phoneButton.setMaximumSize(new java.awt.Dimension(24, 24));
         phoneButton.setMinimumSize(new java.awt.Dimension(24, 24));
         phoneButton.setPreferredSize(new java.awt.Dimension(24, 24));
         phoneButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 phoneButtonActionPerformed(evt);
             }
         });
 
         editButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/audiomanager/client/resources/UserListEdit24x24.png"))); // NOI18N
         editButton.setToolTipText("Edit");
         editButton.setMaximumSize(new java.awt.Dimension(24, 24));
         editButton.setMinimumSize(new java.awt.Dimension(24, 24));
         editButton.setPreferredSize(new java.awt.Dimension(24, 24));
         editButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 editButtonActionPerformed(evt);
             }
         });
 
         propertiesButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/audiomanager/client/resources/UserListProperties24x24.png"))); // NOI18N
         propertiesButton.setToolTipText("Properties");
         propertiesButton.setMaximumSize(new java.awt.Dimension(24, 24));
         propertiesButton.setMinimumSize(new java.awt.Dimension(24, 24));
         propertiesButton.setPreferredSize(new java.awt.Dimension(24, 24));
         propertiesButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 propertiesButtonActionPerformed(evt);
             }
         });
 
         volumeLabel.setText("Volume for:");
 
         volumeSlider.setMajorTickSpacing(1);
         volumeSlider.setMaximum(10);
         volumeSlider.setMinorTickSpacing(1);
         volumeSlider.setPaintLabels(true);
         volumeSlider.setPaintTicks(true);
         volumeSlider.setSnapToTicks(true);
         volumeSlider.setValue(5);
         volumeSlider.setMinimumSize(new java.awt.Dimension(36, 85));
         volumeSlider.addChangeListener(new javax.swing.event.ChangeListener() {
             public void stateChanged(javax.swing.event.ChangeEvent evt) {
                 volumeSliderStateChanged(evt);
             }
         });
 
         org.jdesktop.layout.GroupLayout controlPanelLayout = new org.jdesktop.layout.GroupLayout(controlPanel);
         controlPanel.setLayout(controlPanelLayout);
         controlPanelLayout.setHorizontalGroup(
             controlPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(controlPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .add(controlPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(volumeLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 159, Short.MAX_VALUE)
                     .add(volumeSlider, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 159, Short.MAX_VALUE)
                     .add(controlPanelLayout.createSequentialGroup()
                         .add(muteButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                         .add(3, 3, 3)
                         .add(textChatButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                         .add(3, 3, 3)
                         .add(voiceChatButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                         .add(3, 3, 3)
                         .add(phoneButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                         .add(3, 3, 3)
                         .add(editButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                         .add(3, 3, 3)
                        .add(propertiesButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                 .addContainerGap())
         );
         controlPanelLayout.setVerticalGroup(
             controlPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(controlPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .add(controlPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(textChatButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(muteButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(voiceChatButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(editButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(propertiesButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(phoneButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(volumeLabel)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(volumeSlider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 40, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         userListScrollPane.setPreferredSize(new java.awt.Dimension(260, 300));
 
         userList.setFont(new java.awt.Font("SansSerif", 0, 12));
         userList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
             public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                 userListValueChanged(evt);
             }
         });
         userListScrollPane.setViewportView(userList);
 
         jPanel1.setMaximumSize(new java.awt.Dimension(32767, 17));
         jPanel1.setMinimumSize(new java.awt.Dimension(100, 17));
         jPanel1.setPreferredSize(new java.awt.Dimension(164, 17));
 
         panelToggleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/audiomanager/client/resources/upArrow23x10.png"))); // NOI18N
         panelToggleButton.setBorder(null);
         panelToggleButton.setMaximumSize(new java.awt.Dimension(63, 14));
         panelToggleButton.setMinimumSize(new java.awt.Dimension(63, 14));
         panelToggleButton.setPreferredSize(new java.awt.Dimension(63, 14));
         panelToggleButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 panelToggleButtonActionPerformed(evt);
             }
         });
 
         org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
         jPanel1.setLayout(jPanel1Layout);
         jPanel1Layout.setHorizontalGroup(
             jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                 .addContainerGap(151, Short.MAX_VALUE)
                 .add(panelToggleButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 28, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
         );
         jPanel1Layout.setVerticalGroup(
             jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(panelToggleButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
         );
 
         org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
         this.setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(userListScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
             .add(org.jdesktop.layout.GroupLayout.TRAILING, controlPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
             .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(layout.createSequentialGroup()
                 .add(userListScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                 .add(0, 0, 0)
                 .add(controlPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 101, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 14, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
         );
     }// </editor-fold>//GEN-END:initComponents
 
     private void editButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editButtonActionPerformed
         HUDComponent changeNameHUDComponent = changeNameMap.get(presenceInfo);
 
         if (changeNameHUDComponent == null) {
             ChangeNameHUDPanel changeNameHUDPanel = new ChangeNameHUDPanel(this, pm, presenceInfo);
             HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
             final HUDComponent comp = mainHUD.createComponent(changeNameHUDPanel);
             comp.setPreferredLocation(Layout.NORTH);
             mainHUD.addComponent(comp);
             changeNameMap.put(presenceInfo, comp);
 
             PropertyChangeListener plistener = new PropertyChangeListener() {
 
                 public void propertyChange(PropertyChangeEvent pe) {
                     if (pe.getPropertyName().equals("ok") || pe.getPropertyName().equals("cancel")) {
                         comp.setVisible(false);
                     }
                 }
             };
             changeNameHUDPanel.addPropertyChangeListener(plistener);
             changeNameHUDComponent = comp;
         }
 
         changeNameHUDComponent.setVisible(true);
 }//GEN-LAST:event_editButtonActionPerformed
 
     private void propertiesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_propertiesButtonActionPerformed
         if (namePropertiesHUDComponent == null) {
             NamePropertiesHUDPanel namePropertiesHUDPanel = new NamePropertiesHUDPanel(presenceInfo);
             HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
             namePropertiesHUDComponent = mainHUD.createComponent(namePropertiesHUDPanel);
             namePropertiesHUDComponent.setPreferredLocation(Layout.NORTH);
             mainHUD.addComponent(namePropertiesHUDComponent);
 
             PropertyChangeListener plistener = new PropertyChangeListener() {
 
                 public void propertyChange(PropertyChangeEvent pe) {
                     if (pe.getPropertyName().equals("ok") || pe.getPropertyName().equals("cancel")) {
                         namePropertiesHUDComponent.setVisible(false);
                     }
                 }
             };
             namePropertiesHUDPanel.addPropertyChangeListener(plistener);
         }
 
         namePropertiesHUDComponent.setVisible(true);
 }//GEN-LAST:event_propertiesButtonActionPerformed
     private ConcurrentHashMap<PresenceInfo, Integer> volumeChangeMap = new ConcurrentHashMap();
 
     private void volumeSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_volumeSliderStateChanged
         javax.swing.JSlider source = (javax.swing.JSlider) evt.getSource();
         int volume = source.getValue();
 
         Object[] selectedValues = userList.getSelectedValues();
 
         if (selectedValues.length > 0) {
             for (int i = 0; i < selectedValues.length; i++) {
                 String username = NameTagNode.getUsername((String) selectedValues[i]);
 
                 PresenceInfo[] info = pm.getAliasPresenceInfo(username);
 
                 if (info == null) {
                     logger.warning("no PresenceInfo for " + username);
                     System.out.println("no PresenceInfo for " + username);
                     continue;
                 }
                 logger.info("changing volume for " + username + " to: " + volume);
                 PresenceInfo pi = info[0];
                 volumeChanged(pi.cellID, pi.callID, volume);
                 volumeChangeMap.put(pi, new Integer(volume));
             }
         }
 }//GEN-LAST:event_volumeSliderStateChanged
 
     public void volumeChanged(CellID cellID, String otherCallID, int volume) {
         SoftphoneControlImpl sc = SoftphoneControlImpl.getInstance();
 
         session.send(client, new AudioVolumeMessage(cellID, sc.getCallID(), otherCallID,
                 VolumeUtil.getServerVolume(volume)));
     }
 
     private void userListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_userListValueChanged
         Object[] selectedValues = userList.getSelectedValues();
 
         if (selectedValues.length == 0) {
             editButton.setEnabled(false);
             volumeLabel.setText("Private volume");
             volumeSlider.setEnabled(false);
             controlPanel.setVisible(false);
             textChatButton.setEnabled(false);
             voiceChatButton.setEnabled(false);
             panelToggleButton.setIcon(upIcon);
         } else if (selectedValues.length == 1) {
             // one user (self or someone else)
             controlPanel.setVisible(true);
             volumeSlider.setEnabled(true);
             panelToggleButton.setIcon(downIcon);
 
             String username = NameTagNode.getUsername((String) selectedValues[0]);
 
             PresenceInfo[] info = pm.getAliasPresenceInfo(username);
 
             if (info == null) {
                 logger.warning("no PresenceInfo for " + username);
                 editButton.setEnabled(false);
                 return;
             }
 
             if (isMe(info[0])) {
                 // this user
                 volumeLabel.setText("Master volume for " + username);
                 editButton.setEnabled(true);
                 textChatButton.setEnabled(false);
                 voiceChatButton.setEnabled(false);
             } else {
                 // another user
                 volumeLabel.setText("Private volume for " + username);
                 editButton.setEnabled(false);
                 textChatButton.setEnabled(true);
                 voiceChatButton.setEnabled(true);
             }
 
             if (presenceInfo != null) {
                 Integer v = volumeChangeMap.get(presenceInfo);
 
                 if (v != null) {
                     volumeSlider.setValue(v.intValue());
                 }
             }
         } else {
             // multiple users
             volumeLabel.setText("Private volume for " + selectedValues.length + " users");
             volumeSlider.setEnabled(true);
             volumeSlider.setValue(5);
             textChatButton.setEnabled(true);
             voiceChatButton.setEnabled(true);
             panelToggleButton.setIcon(downIcon);
         }
 }//GEN-LAST:event_userListValueChanged
 
 private void textChatButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textChatButtonActionPerformed
 }//GEN-LAST:event_textChatButtonActionPerformed
 
 private void voiceChatButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_voiceChatButtonActionPerformed
     ArrayList<PresenceInfo> usersToInvite = new ArrayList();
 
     Object[] selectedValues = userList.getSelectedValues();
 
     if (selectedValues.length > 0) {
         for (int i = 0; i < selectedValues.length; i++) {
             String username = NameTagNode.getUsername((String) selectedValues[i]);
 
             PresenceInfo[] info = pm.getAliasPresenceInfo(username);
 
             if (info == null) {
                 System.out.println("no PresenceInfo for " + username);
                 continue;
             }
 
             if (info[0].equals(presenceInfo)) {
                 /*
                  * I'm the caller and will be added automatically
                  */
                 continue;
             }
 
             usersToInvite.add(info[0]);
         }
     }
 
     InCallHUDPanel inCallHUDPanel =
 	new InCallHUDPanel(client, session, presenceInfo, presenceInfo);
 
     inCallHUDPanel.inviteUsers(usersToInvite);
 
     HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
     final HUDComponent inCallHUDComponent = mainHUD.createComponent(inCallHUDPanel);
 
     inCallHUDPanel.setHUDComponent(inCallHUDComponent);
 
     mainHUD.addComponent(inCallHUDComponent);
     inCallHUDComponent.addComponentListener(new HUDComponentListener() {
 
         public void HUDComponentChanged(HUDComponentEvent e) {
             if (e.getEventType().equals(ComponentEventType.DISAPPEARED)) {
             }
         }
     });
 
     PropertyChangeListener plistener = new PropertyChangeListener() {
 
         public void propertyChange(PropertyChangeEvent pe) {
             if (pe.getPropertyName().equals("ok") || pe.getPropertyName().equals("cancel")) {
                 inCallHUDComponent.setVisible(false);
             }
         }
     };
 
     inCallHUDPanel.addPropertyChangeListener(plistener);
     inCallHUDComponent.setVisible(true);
 
     //System.out.println("U x,y " + userListHUDComponent.getX() + ", " + userListHUDComponent.getY()
     //    + " width " + userListHUDComponent.getWidth() + " height " + userListHUDComponent.getHeight()
     //    + " I x,y " + (userListHUDComponent.getX() + userListHUDComponent.getWidth())
     //    + ", " + (userListHUDComponent.getY() + userListHUDComponent.getHeight() - inCallHUDComponent.getHeight()));
 
     inCallHUDComponent.setLocation(userListHUDComponent.getX() + userListHUDComponent.getWidth(),
             userListHUDComponent.getY() + userListHUDComponent.getHeight() - inCallHUDComponent.getHeight());
 }//GEN-LAST:event_voiceChatButtonActionPerformed
 
 private void muteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_muteButtonActionPerformed
     SoftphoneControlImpl sc = SoftphoneControlImpl.getInstance();
     sc.mute(!sc.isMuted());
 }//GEN-LAST:event_muteButtonActionPerformed
 
 private void panelToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_panelToggleButtonActionPerformed
     controlPanel.setVisible(!controlPanel.isVisible());
     if (controlPanel.isVisible()){
         panelToggleButton.setIcon(downIcon);
     } else {
         panelToggleButton.setIcon(upIcon);
     }
 }//GEN-LAST:event_panelToggleButtonActionPerformed
 
 private void phoneButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_phoneButtonActionPerformed
     // TODO add your handling code here:
 }//GEN-LAST:event_phoneButtonActionPerformed
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JPanel controlPanel;
     private javax.swing.JButton editButton;
     private javax.swing.JPanel jPanel1;
     private javax.swing.JButton muteButton;
     private javax.swing.JButton panelToggleButton;
     private javax.swing.JButton phoneButton;
     private javax.swing.JButton propertiesButton;
     private javax.swing.JButton textChatButton;
     private javax.swing.JList userList;
     private javax.swing.JScrollPane userListScrollPane;
     private javax.swing.JButton voiceChatButton;
     private javax.swing.JLabel volumeLabel;
     private javax.swing.JSlider volumeSlider;
     // End of variables declaration//GEN-END:variables
 }
