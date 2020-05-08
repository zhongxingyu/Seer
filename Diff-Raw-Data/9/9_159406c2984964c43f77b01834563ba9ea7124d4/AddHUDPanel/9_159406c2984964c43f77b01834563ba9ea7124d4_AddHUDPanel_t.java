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
 package org.jdesktop.wonderland.modules.audiomanager.client.voicechat;
 
 import org.jdesktop.wonderland.modules.audiomanager.client.AudioManagerClient;
 import org.jdesktop.wonderland.modules.audiomanager.client.DisconnectListener;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.beans.PropertyChangeEvent;
 
 import java.util.logging.Logger;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 
 import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManager;
 import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManagerFactory;
 import org.jdesktop.wonderland.modules.presencemanager.common.PresenceInfo;
 
 import org.jdesktop.wonderland.modules.audiomanager.common.messages.voicechat.VoiceChatLeaveMessage;
 import org.jdesktop.wonderland.modules.audiomanager.common.messages.voicechat.VoiceChatMessage.ChatType;
 
 import org.jdesktop.wonderland.client.comms.WonderlandSession;
 
 import org.jdesktop.wonderland.client.hud.HUD;
 import org.jdesktop.wonderland.client.hud.HUDComponent;
 import org.jdesktop.wonderland.client.hud.HUDEvent;
 import org.jdesktop.wonderland.client.hud.HUDEvent.HUDEventType;
 import org.jdesktop.wonderland.client.hud.HUDEventListener;
 import org.jdesktop.wonderland.client.hud.HUDManagerFactory;
 
 /**
  *
  * @author nsimpson
  */
 public class AddHUDPanel extends javax.swing.JPanel implements DisconnectListener {
 
     public enum Mode {
 
         ADD, INITIATE, IN_PROGRESS, HOLD
     };
     public Mode mode = Mode.ADD;
     private AddTypePanel addTypePanel;
     private AddUserPanel addUserPanel;
     private AddPhoneUserPanel addPhoneUserPanel;
     private HoldPanel holdPanel;
     private AddInitiateButtonPanel addInitiateButtonPanel;
     private InProgressButtonPanel inProgressButtonPanel;
     private static final Logger logger = Logger.getLogger(AddHUDPanel.class.getName());
     private AudioManagerClient client;
     private WonderlandSession session;
     private PresenceManager pm;
     private PresenceInfo myPresenceInfo;
     private PresenceInfo caller;
     private String group;
     private ChatType chatType;
     private static int groupNumber;
     private static HashMap<String, AddHUDPanel> addHUDPanelMap = new HashMap();
     private PropertyChangeSupport listeners;
     private HUDComponent addHUDComponent;
     private int normalHeight = 0;
 
     public AddHUDPanel() {
         initComponents();
         setMode(Mode.INITIATE);
     }
 
     public AddHUDPanel(AudioManagerClient client, WonderlandSession session,
             PresenceInfo myPresenceInfo, PresenceInfo caller) {
 
         this(client, session, myPresenceInfo, caller, null);
     }
 
     public AddHUDPanel(AudioManagerClient client, WonderlandSession session,
             PresenceInfo myPresenceInfo, PresenceInfo caller, String group) {
 
         this.client = client;
         this.session = session;
         this.myPresenceInfo = myPresenceInfo;
         this.caller = caller;
 
         if (group == null) {
             group = caller.userID.getUsername() + "-" + groupNumber++;
         }
 
         this.group = group;
 
         initComponents();
 
         setMode(Mode.INITIATE);
 
         //hangupButton.setEnabled(false);
 
         pm = PresenceManagerFactory.getPresenceManager(session);
 
         addHUDPanelMap.put(group, this);
 
         client.addDisconnectListener(this);
     }
 
     public void setHUDComponent(HUDComponent addHUDComponent) {
         this.addHUDComponent = addHUDComponent;
 
         addHUDComponent.addEventListener(new HUDEventListener() {
 
             public void HUDObjectChanged(HUDEvent e) {
                 if (e.getEventType().equals(HUDEventType.CLOSED)) {
                     leave();
                 }
             }
         });
     }
 
     public HUDComponent getHUDComponent() {
         return addHUDComponent;
     }
 
     public static AddHUDPanel getAddHUDPanel(String group) {
         return addHUDPanelMap.get(group);
     }
 
     public void setPhoneType() {
         addTypePanel.setType(true);
         showAddPhonePanel(true, true);
         addInitiateButtonPanel.setActionButtonText("Call");
         userMode = false;
     }
 
     public void inviteUsers(ArrayList<PresenceInfo> usersToInvite) {
         addUserPanel.inviteUsers(usersToInvite);
         setMode(Mode.IN_PROGRESS);
     }
 
     public void setClosed() {
         //holdHUDPanel = null;
         //holdHUDComponent = null;
 
         addHUDComponent.setClosed();
     }
 
     public void disconnected() {
         java.awt.EventQueue.invokeLater(new Runnable() {
 
             public void run() {
                 addHUDComponent.setVisible(false);
             }
         });
     }
 
     /**
      * Adds a bound property listener to the dialog
      * @param listener a listener for dialog events
      */
     @Override
     public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
         if (listeners == null) {
             listeners = new PropertyChangeSupport(this);
         }
         listeners.addPropertyChangeListener(listener);
     }
 
     /**
      * Removes a bound property listener from the dialog
      * @param listener the listener to remove
      */
     @Override
     public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
         if (listeners != null) {
             listeners.removePropertyChangeListener(listener);
         }
     }
 
     public void setMode(Mode mode) {
         this.mode = mode;
         switch (mode) {
             case INITIATE:
                 setInitiateMode();
                 break;
             case ADD:
                 setAddUserMode();
                 break;
             case IN_PROGRESS:
                 setInProgressMode();
                 break;
             case HOLD:
                 setHoldMode();
                 break;
         }
     }
     private boolean userMode = true;
 
     private void showAddType(boolean show) {
         if (addTypePanel == null) {
             addTypePanel = new AddTypePanel();
             addTypePanel.addUserModeListener(new ActionListener() {
 
                 public void actionPerformed(ActionEvent e) {
                     showAddUserPanel(true, (mode != Mode.ADD));
                     addInitiateButtonPanel.setActionButtonText("Invite");
                     userMode = true;
                 }
             });
             addTypePanel.addPhoneModeListener(new ActionListener() {
 
                 public void actionPerformed(ActionEvent e) {
                     showAddPhonePanel(true, (mode != Mode.ADD));
                     addInitiateButtonPanel.setActionButtonText("Call");
                     userMode = false;
                 }
             });
         }
         addTypePanel.setVisible(show);
         if (show) {
             add(addTypePanel, BorderLayout.NORTH);
         }
     }
 
     private void showAddUserPanel(boolean showPanel, boolean showPrivacy) {
         if (addPhoneUserPanel != null) {
             addPhoneUserPanel.setVisible(false);
         }
 
         if (addUserPanel == null) {
             addUserPanel = new AddUserPanel(client, session, myPresenceInfo,
                     caller, group);
         }
 
         addUserPanel.setVisible(showPanel, mode);
 
         if (showPanel) {
             add(addUserPanel, BorderLayout.CENTER);
         }
 
         addUserPanel.showPrivacyPanel(showPrivacy);
     }
 
     private void showAddPhonePanel(boolean showPanel, boolean showPrivacy) {
         if (addUserPanel != null) {
             addUserPanel.setVisible(false);
         }
 
         if (addPhoneUserPanel == null) {
             addPhoneUserPanel = new AddPhoneUserPanel();
         }
 
         addPhoneUserPanel.setVisible(showPanel);
 
         if (showPanel) {
             add(addPhoneUserPanel, BorderLayout.CENTER);
         }
         addPhoneUserPanel.showPrivacyPanel(showPrivacy);
     }
 
     private void showHoldPanel(boolean showPanel) {
         if (holdPanel == null) {
             holdPanel = new HoldPanel();
             holdPanel.addHoldListener(new ActionListener() {
 
                 public void actionPerformed(ActionEvent e) {
                     setInProgressMode();
                 }
             });
         }
         holdPanel.setVisible(showPanel);
         if (showPanel) {
             add(holdPanel, BorderLayout.NORTH);
             if (normalHeight == 0) {
                 normalHeight = addHUDComponent.getHeight();
             }
             addHUDComponent.setHeight(holdPanel.getPreferredSize().height);
         }
     }
 
     private void showInitiateButtons(boolean show) {
         if (addInitiateButtonPanel == null) {
             addInitiateButtonPanel = new AddInitiateButtonPanel();
 
             addInitiateButtonPanel.addActionButtonListener(new ActionListener() {
 
                 public void actionPerformed(ActionEvent e) {
                     actionButtonActionPerformed(e);
                 }
             });
 
             addInitiateButtonPanel.addCancelButtonListener(new ActionListener() {
 
                 public void actionPerformed(ActionEvent e) {
                     cancelButtonActionPerformed(e);
                 }
             });
         }
         addInitiateButtonPanel.setVisible(show);
         if (show) {
             add(addInitiateButtonPanel, BorderLayout.SOUTH);
         }
     }
 
     private void actionButtonActionPerformed(ActionEvent e) {
         if (userMode) {
             addUserPanel.inviteUsers();
             setMode(Mode.IN_PROGRESS);
         } else {
             /*
              * Phone Mode
              */
             String name = addPhoneUserPanel.getPhoneName();
 
             PresenceInfo[] info = pm.getAllUsers();
 
             for (int i = 0; i < info.length; i++) {
                 if (info[i].usernameAlias.equals(name) ||
                         info[i].userID.getUsername().equals(name)) {
 
                     addPhoneUserPanel.setStatusMessage("Name is already being used!");
                     return;
                 }
             }
 
             addUserPanel.callUser(name, addPhoneUserPanel.getPhoneNumber());
         }
     }
 
     private void cancelButtonActionPerformed(ActionEvent e) {
         addHUDComponent.setVisible(false);
     }
 
     private void showInProgressButtons(boolean show) {
         if (inProgressButtonPanel == null) {
             inProgressButtonPanel = new InProgressButtonPanel();
 
             inProgressButtonPanel.addAddButtonListener(new ActionListener() {
 
                 public void actionPerformed(ActionEvent e) {
                     addButtonActionPerformed(e);
                 }
             });
 
             inProgressButtonPanel.addHangUpButtonListener(new ActionListener() {
 
                 public void actionPerformed(ActionEvent e) {
                     hangup(e);
                 }
             });
 
             inProgressButtonPanel.addHoldButtonListener(new ActionListener() {
 
                 public void actionPerformed(ActionEvent e) {
                     setHoldMode();
                 }
             });
             inProgressButtonPanel.addLeaveButtonListener(new ActionListener() {
 
                 public void actionPerformed(ActionEvent e) {
                     leave();
                 }
             });
         }
         inProgressButtonPanel.setVisible(show);
         if (show) {
             add(inProgressButtonPanel, BorderLayout.SOUTH);
         }
     }
 
     private void addButtonActionPerformed(ActionEvent e) {
         AddHUDPanel addHUDPanel =
                 new AddHUDPanel(client, session, myPresenceInfo, myPresenceInfo, group);
 
         addHUDPanel.setMode(Mode.ADD);
 
         HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
         final HUDComponent addHUDComponent = mainHUD.createComponent(addHUDPanel);
 
         addHUDPanel.setHUDComponent(addHUDComponent);
 
         mainHUD.addComponent(addHUDComponent);
         addHUDComponent.addEventListener(new HUDEventListener() {
 
             public void HUDObjectChanged(HUDEvent e) {
                 if (e.getEventType().equals(HUDEventType.DISAPPEARED)) {
                 }
             }
         });
 
         PropertyChangeListener plistener = new PropertyChangeListener() {
 
             public void propertyChange(PropertyChangeEvent pe) {
                 if (pe.getPropertyName().equals("ok") || pe.getPropertyName().equals("cancel")) {
                     addHUDComponent.setVisible(false);
                 }
             }
         };
 
         addHUDPanel.addPropertyChangeListener(plistener);
         addHUDComponent.setVisible(true);
     }
 
     private void leave() {
         session.send(client, new VoiceChatLeaveMessage(group, myPresenceInfo));
         addHUDComponent.setVisible(false);
         addHUDPanelMap.remove(group);
     }
 
     private void hangup(ActionEvent e) {
         addUserPanel.hangup();
     }
 
     private void setInitiateMode() {
         clearPanel();
         showAddType(true);
         showAddUserPanel(true, true);
         showInitiateButtons(true);
     }
 
     private void setAddUserMode() {
         clearPanel();
         showAddType(true);
         showAddUserPanel(true, false);
         showInitiateButtons(true);
     }
 
     private void setInProgressMode() {
         clearPanel();
         showAddUserPanel(true, true);
         showInProgressButtons(true);
     }
 
     public void setHoldMode() {
         clearPanel();
         showHoldPanel(true);
     }
 
     private void clearPanel() {
         Component[] components = getComponents();
         for (int c = 0; c < components.length; c++) {
             components[c].setVisible(false);
         }
         validate();
         if ((normalHeight > 0) && (addHUDComponent != null)) {
             // restore dialog to the normal height if was in HOLD mode
            addHUDComponent.setHeight(normalHeight);
         }
     }
 
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         addTypeButtonGroup = new javax.swing.ButtonGroup();
 
         setPreferredSize(new java.awt.Dimension(295, 220));
         setLayout(new java.awt.BorderLayout());
     }// </editor-fold>//GEN-END:initComponents
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.ButtonGroup addTypeButtonGroup;
     // End of variables declaration//GEN-END:variables
 }
