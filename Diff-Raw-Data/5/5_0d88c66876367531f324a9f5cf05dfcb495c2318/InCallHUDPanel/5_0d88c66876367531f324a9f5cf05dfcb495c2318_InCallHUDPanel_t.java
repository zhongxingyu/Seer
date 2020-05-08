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
 package org.jdesktop.wonderland.modules.audiomanager.client;
 
 import org.jdesktop.wonderland.modules.audiomanager.common.messages.audio.EndCallMessage;
 
 import org.jdesktop.wonderland.modules.audiomanager.common.messages.voicechat.VoiceChatDialOutMessage;
 import org.jdesktop.wonderland.modules.audiomanager.common.messages.voicechat.VoiceChatHoldMessage;
 import org.jdesktop.wonderland.modules.audiomanager.common.messages.voicechat.VoiceChatJoinMessage;
 import org.jdesktop.wonderland.modules.audiomanager.common.messages.voicechat.VoiceChatLeaveMessage;
 import org.jdesktop.wonderland.modules.audiomanager.common.messages.voicechat.VoiceChatMessage;
 import org.jdesktop.wonderland.modules.audiomanager.common.messages.voicechat.VoiceChatMessage.ChatType;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import java.util.logging.Logger;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Font;
 
 import javax.swing.DefaultListCellRenderer;
 import javax.swing.DefaultListModel;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.ListCellRenderer;
 
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 
 import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManager;
 import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManagerFactory;
 import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManagerListener;
 import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManagerListener.ChangeType;
 
 import org.jdesktop.wonderland.client.softphone.SoftphoneControl;
 import org.jdesktop.wonderland.client.softphone.SoftphoneControlImpl;
 
 import org.jdesktop.wonderland.common.auth.WonderlandIdentity;
 
 import org.jdesktop.wonderland.modules.presencemanager.common.PresenceInfo;
 
 import org.jdesktop.wonderland.client.comms.WonderlandSession;
 
 import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.NameTagNode;
 
 import org.jdesktop.wonderland.client.hud.CompassLayout.Layout;
 import org.jdesktop.wonderland.client.hud.HUD;
 import org.jdesktop.wonderland.client.hud.HUDComponent;
 
 import org.jdesktop.wonderland.client.hud.HUDEvent;
 import org.jdesktop.wonderland.client.hud.HUDEvent.HUDEventType;
 import org.jdesktop.wonderland.client.hud.HUDEventListener;
 import org.jdesktop.wonderland.client.hud.HUDManagerFactory;
 
 import org.jdesktop.wonderland.modules.audiomanager.common.VolumeUtil;
 
 import org.jdesktop.wonderland.client.softphone.SoftphoneControlImpl;
 
 /**
  *
  * @author  jp
  */
 public class InCallHUDPanel extends javax.swing.JPanel implements PresenceManagerListener,
         MemberChangeListener, DisconnectListener {
 
     private static final Logger logger = Logger.getLogger(InCallHUDPanel.class.getName());
     private AudioManagerClient client;
     private WonderlandSession session;
     private PresenceManager pm;
     private PresenceInfo myPresenceInfo;
     private PresenceInfo caller;
     private DefaultListModel userListModel;
     private String group;
     private ChatType chatType;
     private static int groupNumber;
     private static HashMap<String, InCallHUDPanel> inCallHUDPanelMap = new HashMap();
     private HUDComponent inCallHUDComponent;
 
     private AddHUDPanel addHUDPanel;
     private HUDComponent addHUDComponent;
 
     private boolean personalPhone;
 
     /** Creates new form InCallHUDPanel */
     public InCallHUDPanel() {
         initComponents();
     }
 
     public InCallHUDPanel(AudioManagerClient client, WonderlandSession session,
             PresenceInfo myPresenceInfo, PresenceInfo caller) {
 
         this(client, session, myPresenceInfo, caller, null);
     }
 
     public InCallHUDPanel(AudioManagerClient client, WonderlandSession session,
             PresenceInfo myPresenceInfo, PresenceInfo caller, String group) {
 
         this.client = client;
         this.session = session;
         this.myPresenceInfo = myPresenceInfo;
         this.caller = caller;
 
         initComponents();
 
         userListModel = new DefaultListModel();
         userList.setModel(userListModel);
         userList.setCellRenderer(new UserListCellRenderer());
 
 	members.add(myPresenceInfo);
 
         if (caller.equals(myPresenceInfo) == false) {
             members.add(caller);
             addToUserList(caller);
         }
 
         hangupButton.setEnabled(false);
 
         pm = PresenceManagerFactory.getPresenceManager(session);
 
         pm.addPresenceManagerListener(this);
 
	pm.setMute(myPresenceInfo, true);

         client.addDisconnectListener(this);
 
         if (group == null) {
             group = caller.userID.getUsername() + "-" + groupNumber++;
         }
 
         this.group = group;
 
         inCallHUDPanelMap.put(group, this);
 
         client.addMemberChangeListener(group, this);
 
 	privacyDescription.setText(VoiceChatMessage.PRIVATE_DESCRIPTION);
 
         setVisible(true);
     }
 
     public void setAddPanel(AddHUDPanel addHUDPanel,
 	    HUDComponent addHUDComponent) {
 
         this.addHUDPanel = addHUDPanel;
 	this.addHUDComponent = addHUDComponent;
     }
 
     public void setAddHUDPanel(AddHUDPanel addHUDPanel,
 	    HUDComponent addHUDComponent) {
 
         this.addHUDPanel = addHUDPanel;
         this.addHUDComponent = addHUDComponent;
     }
 
     public void setClosed() {
 	holdHUDPanel = null;
 	holdHUDComponent = null;
 
 	inCallHUDComponent.setClosed();
     }
 
     public void setHUDComponent(HUDComponent inCallHUDComponent) {
         this.inCallHUDComponent = inCallHUDComponent;
 
 	inCallHUDComponent.setName("Call In Progress");
 
         inCallHUDComponent.addEventListener(new HUDEventListener() {
 
 	    public void HUDObjectChanged(HUDEvent e) {
                 if (e.getEventType().equals(HUDEventType.CLOSED)) {
 		    leave();
                 }
             }
         });
     }
 
     public void callUser(String name, String number) {
 	personalPhone = true;
 
         session.send(client, new VoiceChatJoinMessage(group, myPresenceInfo,
                 new PresenceInfo[0], ChatType.PRIVATE));
 
         SoftphoneControl sc = SoftphoneControlImpl.getInstance();
 
         String callID = sc.getCallID();
 
         PresenceInfo info = new PresenceInfo(null, null, new WonderlandIdentity(name, name, null), callID);
 
 	pm.addPresenceInfo(info);
 
         addToUserList(info);
         session.send(client, new VoiceChatDialOutMessage(group, callID, ChatType.PRIVATE, info, number));
     }
 
     public void inviteUsers(ArrayList<PresenceInfo> usersToInvite) {
         inviteUsers(usersToInvite, secretRadioButton.isSelected());
     }
 
     public void inviteUsers(ArrayList<PresenceInfo> usersToInvite, boolean isSecretChat) {
         for (PresenceInfo info : usersToInvite) {
             addToUserList(info);
             invitedMembers.add(info);
         }
 
         if (isSecretChat) {
             secretRadioButton.setSelected(true);
         } else {
             privateRadioButton.setSelected(true);
         }
 
         session.send(client, new VoiceChatJoinMessage(group, myPresenceInfo,
             usersToInvite.toArray(new PresenceInfo[0]),
             isSecretChat ? ChatType.SECRET : ChatType.PRIVATE));
     }
 
     public PresenceInfo getCaller() {
         return caller;
     }
 
     public String getGroup() {
         return group;
     }
 
     public HUDComponent getHUDComponent() {
         return inCallHUDComponent;
     }
 
     public static InCallHUDPanel getInCallHUDPanel(String group) {
         return inCallHUDPanelMap.get(group);
     }
 
     private void addElement(final String name) {
         java.awt.EventQueue.invokeLater(new Runnable() {
             public void run() {
                 userListModel.removeElement(name);
                 userListModel.addElement(name);
             }
         });
     }
 
     private void removeElement(final String name) {
         java.awt.EventQueue.invokeLater(new Runnable() {
             public void run() {
                 userListModel.removeElement(name);
             }
         });
     }
 
     private void addToUserList(PresenceInfo info) {
         removeFromUserList(info);
 
         String name = NameTagNode.getDisplayName(info.usernameAlias,
                 info.isSpeaking, info.isMuted);
 
         addElement(name);
     }
 
     private void removeFromUserList(PresenceInfo info) {
         String name = NameTagNode.getDisplayName(info.usernameAlias, false, false);
         removeElement(name);
 
         name = NameTagNode.getDisplayName(info.usernameAlias, false, true);
         removeElement(name);
 
         name = NameTagNode.getDisplayName(info.usernameAlias, true, false);
         removeElement(name);
     }
 
     public void presenceInfoChanged(PresenceInfo presenceInfo, ChangeType type) {
         removeFromUserList(presenceInfo);
 
         if (members.contains(presenceInfo) == false &&
                 invitedMembers.contains(presenceInfo) == false) {
 
 	    //System.out.println("PI Change not a member: " + type + " " + presenceInfo);
             return;
         }
 
 	//System.out.println("PI Change for member: " + type + " " + presenceInfo);
 
         if (type.equals(ChangeType.USER_REMOVED)) {
 	    if (presenceInfo.clientID == null) {
 		if (personalPhone && members.size() == 1) {
 		    leave();
 		}
 	    }
 	} else {
             addToUserList(presenceInfo);
         }
     }
 
     private ArrayList<PresenceInfo> members = new ArrayList();
     private ArrayList<PresenceInfo> invitedMembers = new ArrayList();
 
     public void setMemberList(PresenceInfo[] memberList) {
     }
 
     public void memberChange(PresenceInfo member, boolean added) {
 	//System.out.println("MemberChange " + added + " " + member);
 
         invitedMembers.remove(member);
 
         if (added == true) {
 	    if (members.contains(member) == false) {
                 members.add(member);
 	    } 
 
             addToUserList(member);
             return;
         }
 
         synchronized (members) {
             members.remove(member);
         }
 
         removeFromUserList(member);
 
 	if (personalPhone && members.size() == 1) {
 	    leave();
 	}
     }
 
     public void disconnected() {
         inCallHUDPanelMap.remove(group);
         inCallHUDComponent.setClosed();
     }
 
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         buttonGroup1 = new javax.swing.ButtonGroup();
         jScrollPane1 = new javax.swing.JScrollPane();
         userList = new javax.swing.JList();
         secretRadioButton = new javax.swing.JRadioButton();
         privateRadioButton = new javax.swing.JRadioButton();
         leaveButton = new javax.swing.JButton();
         hangupButton = new javax.swing.JButton();
         holdButton = new javax.swing.JButton();
         speakerPhoneRadioButton = new javax.swing.JRadioButton();
         privacyDescription = new javax.swing.JLabel();
         addButton = new javax.swing.JButton();
 
         setRequestFocusEnabled(false);
 
         userList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
             public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                 userListValueChanged(evt);
             }
         });
         jScrollPane1.setViewportView(userList);
 
         buttonGroup1.add(secretRadioButton);
         secretRadioButton.setFont(secretRadioButton.getFont());
         secretRadioButton.setText("Secret");
         secretRadioButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 secretRadioButtonActionPerformed(evt);
             }
         });
 
         buttonGroup1.add(privateRadioButton);
         privateRadioButton.setFont(privateRadioButton.getFont());
         privateRadioButton.setSelected(true);
         privateRadioButton.setText("Private");
         privateRadioButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 privateRadioButtonActionPerformed(evt);
             }
         });
 
         leaveButton.setText("Leave");
         leaveButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 leaveButtonActionPerformed(evt);
             }
         });
 
         hangupButton.setText("Hang up");
         hangupButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 hangupButtonActionPerformed(evt);
             }
         });
 
         holdButton.setText("Hold");
         holdButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 holdButtonActionPerformed(evt);
             }
         });
 
         speakerPhoneRadioButton.setText("SpeakerPhone");
         speakerPhoneRadioButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 speakerPhoneRadioButtonActionPerformed(evt);
             }
         });
 
         privacyDescription.setFont(new java.awt.Font("DejaVu Sans", 0, 12));
 
         addButton.setText("Add...");
         addButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 addButtonActionPerformed(evt);
             }
         });
 
         org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
         this.setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(layout.createSequentialGroup()
                 .addContainerGap()
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(layout.createSequentialGroup()
                         .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                             .add(addButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                             .add(holdButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 68, Short.MAX_VALUE))
                         .add(18, 18, 18)
                         .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                             .add(hangupButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                             .add(leaveButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 90, Short.MAX_VALUE)))
                     .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                         .add(org.jdesktop.layout.GroupLayout.LEADING, jScrollPane1)
                         .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                             .add(privateRadioButton)
                             .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                             .add(secretRadioButton)
                             .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                             .add(speakerPhoneRadioButton))
                         .add(privacyDescription, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 235, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                 .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(layout.createSequentialGroup()
                 .addContainerGap()
                 .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 152, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .add(7, 7, 7)
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(privateRadioButton)
                     .add(secretRadioButton)
                     .add(speakerPhoneRadioButton))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(privacyDescription, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 14, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .add(12, 12, 12)
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(addButton)
                     .add(hangupButton))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(holdButton)
                     .add(leaveButton))
                 .addContainerGap())
         );
     }// </editor-fold>//GEN-END:initComponents
 
 private void userListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_userListValueChanged
     setEnableHangupButton();
 }//GEN-LAST:event_userListValueChanged
 
 private void leaveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_leaveButtonActionPerformed
     leave();
 }//GEN-LAST:event_leaveButtonActionPerformed
 
 private void hangupButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hangupButtonActionPerformed
     ArrayList<PresenceInfo> membersInfo = getSelectedMembers();
 
     for (PresenceInfo info : membersInfo) {
         session.send(client, new EndCallMessage(info.callID, "Terminated with malice"));
     }
 }//GEN-LAST:event_hangupButtonActionPerformed
 
     private HoldHUDPanel holdHUDPanel;
     private HUDComponent holdHUDComponent;
     private boolean onHold = false;
 
 private void holdButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_holdButtonActionPerformed
     onHold = !onHold;
 
     hold(onHold);
 }//GEN-LAST:event_holdButtonActionPerformed
 
 private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
     if (addHUDPanel != null) {
         addHUDComponent.setVisible(true);
         return;
     }
 
     addHUDPanel = new AddHUDPanel(client, session, myPresenceInfo, this);
 
     HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
     addHUDComponent = mainHUD.createComponent(addHUDPanel);
     addHUDComponent.setName("Phone");
     addHUDPanel.setHUDComponent(addHUDComponent);
 
     //System.out.println("Call in progress x,y " + inCallHUDComponent.getX() + ", " + inCallHUDComponent.getY()
     //    + " width " + inCallHUDComponent.getWidth() + " height " + inCallHUDComponent.getHeight()
     //    + " Call x,y " + (inCallHUDComponent.getX() + inCallHUDComponent.getWidth())
     //    + ", " + (inCallHUDComponent.getY() + inCallHUDComponent.getHeight() - addHUDComponent.getHeight()));
 
     mainHUD.addComponent(addHUDComponent);
     addHUDComponent.addEventListener(new HUDEventListener() {
 
         public void HUDObjectChanged(HUDEvent e) {
             if (e.getEventType().equals(HUDEventType.CLOSED)) {
 		addHUDPanel = null;
 		addHUDComponent = null;
             }
         }
     });
 
     addHUDComponent.setVisible(true);
     addHUDComponent.setLocation(inCallHUDComponent.getX() + inCallHUDComponent.getWidth(),
             inCallHUDComponent.getY() + inCallHUDComponent.getHeight() - addHUDComponent.getHeight());
 
     PropertyChangeListener plistener = new PropertyChangeListener() {
 
         public void propertyChange(PropertyChangeEvent pe) {
             if (pe.getPropertyName().equals("ok") || pe.getPropertyName().equals("cancel")) {
             }
         }
     };
     addHUDPanel.addPropertyChangeListener(plistener);
     addHUDComponent.setVisible(true);
 }//GEN-LAST:event_addButtonActionPerformed
 
 private void speakerPhoneRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_speakerPhoneRadioButtonActionPerformed
     privacyDescription.setText(VoiceChatMessage.PUBLIC_DESCRIPTION);
     changePrivacy(ChatType.PUBLIC);
 
     pm.setMute(myPresenceInfo, SoftphoneControlImpl.getInstance().isMuted());
 }//GEN-LAST:event_speakerPhoneRadioButtonActionPerformed
 
 private void secretRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_secretRadioButtonActionPerformed
     privacyDescription.setText(VoiceChatMessage.SECRET_DESCRIPTION);
     changePrivacy(ChatType.SECRET);
     pm.setMute(myPresenceInfo, true);
 }//GEN-LAST:event_secretRadioButtonActionPerformed
 
 private void privateRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_privateRadioButtonActionPerformed
     privacyDescription.setText(VoiceChatMessage.PRIVATE_DESCRIPTION);
     changePrivacy(ChatType.PRIVATE);
     pm.setMute(myPresenceInfo, true);
 }//GEN-LAST:event_privateRadioButtonActionPerformed
 
     private void changePrivacy(ChatType chatType) {
         ArrayList<PresenceInfo> membersInfo = getSelectedMembers();
 
         if (membersInfo.contains(myPresenceInfo) == false) {
             session.send(client, new VoiceChatJoinMessage(group, myPresenceInfo, new PresenceInfo[0], chatType));
         }
 
         for (PresenceInfo info : membersInfo) {
             /*
              * You can only select yourself or outworlders
              */
             if (info.clientID != null) {
                 continue;
             }
             session.send(client, new VoiceChatJoinMessage(group, info, new PresenceInfo[0], chatType));
         }
     }
 
     private void hold(boolean onHold) {
         if (holdHUDPanel == null) {
             if (onHold == false) {
                 return;
             }
 
             holdHUDPanel = new HoldHUDPanel(client, session, group, this, myPresenceInfo);
 
             HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
             holdHUDComponent = mainHUD.createComponent(holdHUDPanel);
             holdHUDComponent.setPreferredLocation(Layout.SOUTHWEST);
 
 	    holdHUDPanel.setHUDComponent(holdHUDComponent);
 
             mainHUD.addComponent(holdHUDComponent);
             holdHUDComponent.addEventListener(new HUDEventListener() {
 
                 public void HUDObjectChanged(HUDEvent e) {
                     if (e.getEventType().equals(HUDEventType.DISAPPEARED)) {
                     }
                 }
             });
 
             PropertyChangeListener plistener = new PropertyChangeListener() {
 
                 public void propertyChange(PropertyChangeEvent pe) {
                     if (pe.getPropertyName().equals("ok") || pe.getPropertyName().equals("cancel")) {
                         holdHUDComponent.setVisible(false);
                     }
                 }
             };
             holdHUDPanel.addPropertyChangeListener(plistener);
         }
 
         holdHUDComponent.setVisible(onHold);
 
         inCallHUDComponent.setVisible(!onHold);
         setHold(onHold, 1);
     }
 
     public void setHold(boolean onHold, double volume) {
         this.onHold = onHold;
 
         try {
             session.send(client, new VoiceChatHoldMessage(group, myPresenceInfo, onHold,
                     VolumeUtil.getServerVolume(volume)));
 
             if (onHold == false) {
                 holdOtherCalls();
             }
 
             inCallHUDComponent.setVisible(!onHold);
             holdHUDComponent.setVisible(onHold);
         } catch (IllegalStateException e) {
             leave();
         }
     }
 
     public void holdOtherCalls() {
         InCallHUDPanel[] inCallHUDPanels = inCallHUDPanelMap.values().toArray(new InCallHUDPanel[0]);
 
         for (int i = 0; i < inCallHUDPanels.length; i++) {
             if (inCallHUDPanels[i] == this) {
                 continue;
             }
 
             inCallHUDPanels[i].hold(true);
         }
     }
 
     private ArrayList<PresenceInfo> getSelectedMembers() {
         Object[] selectedValues = userList.getSelectedValues();
 
         ArrayList<PresenceInfo> membersInfo = new ArrayList();
 
         for (int i = 0; i < selectedValues.length; i++) {
             String usernameAlias = NameTagNode.getUsername((String) selectedValues[i]);
 
             PresenceInfo info = pm.getAliasPresenceInfo(usernameAlias);
 
             if (info == null) {
                 logger.warning("No presence info for " + (String) selectedValues[i]);
                 continue;
             }
 
             membersInfo.add(info);
         }
 
         return membersInfo;
     }
 
     private void setEnableHangupButton() {
 	hangupButton.setEnabled(false);
 
         ArrayList<PresenceInfo> membersInfo = getSelectedMembers();
 
 	if (membersInfo.size() == 0) {
 	    return;
 	}
 
         for (PresenceInfo info : membersInfo) {
             /*
              * You can only select outworlders
              */
             if (info.clientID != null) {
                 return;
             }
         }
 
         hangupButton.setEnabled(true);
     }
 
     private void leave() {
         session.send(client, new VoiceChatLeaveMessage(group, myPresenceInfo));
         inCallHUDComponent.setVisible(false);
         inCallHUDPanelMap.remove(group);
     }
 
     private class UserListCellRenderer implements ListCellRenderer {
 
         protected DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();
         private Font font = new Font("SansSerif", Font.PLAIN, 13);
 
         public Component getListCellRendererComponent(JList list, Object value, int index,
                 boolean isSelected, boolean cellHasFocus) {
 
             JLabel renderer = (JLabel) defaultRenderer.getListCellRendererComponent(list, value, index,
                     isSelected, cellHasFocus);
 
             String usernameAlias = NameTagNode.getUsername((String) value);
 
             PresenceInfo info = pm.getAliasPresenceInfo(usernameAlias);
 
             if (info == null) {
                 logger.warning("No presence info for " + usernameAlias);
                 return renderer;
             }
 
             if (members.contains(info)) {
                 renderer.setFont(font);
                 renderer.setForeground(Color.BLACK);
             } else {
                 renderer.setFont(font);
                 renderer.setForeground(Color.BLUE);
             }
             return renderer;
         }
     }
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton addButton;
     private javax.swing.ButtonGroup buttonGroup1;
     private javax.swing.JButton hangupButton;
     private javax.swing.JButton holdButton;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JButton leaveButton;
     private javax.swing.JLabel privacyDescription;
     private javax.swing.JRadioButton privateRadioButton;
     private javax.swing.JRadioButton secretRadioButton;
     private javax.swing.JRadioButton speakerPhoneRadioButton;
     private javax.swing.JList userList;
     // End of variables declaration//GEN-END:variables
 }
