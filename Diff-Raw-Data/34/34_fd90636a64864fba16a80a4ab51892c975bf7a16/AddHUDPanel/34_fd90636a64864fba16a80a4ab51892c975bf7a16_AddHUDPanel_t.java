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
 
 import org.jdesktop.wonderland.modules.audiomanager.common.VolumeUtil;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Point;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.beans.PropertyChangeEvent;
 
 import javax.swing.JFrame;
 import javax.swing.JSlider;
 
 import javax.swing.event.ChangeListener;
 
 import java.util.logging.Logger;
 
 import java.util.ArrayList;
 
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 
 import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManager;
 import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManagerFactory;
 import org.jdesktop.wonderland.modules.presencemanager.common.PresenceInfo;
 
 import org.jdesktop.wonderland.modules.audiomanager.common.messages.voicechat.VoiceChatHoldMessage;
 import org.jdesktop.wonderland.modules.audiomanager.common.messages.voicechat.VoiceChatLeaveMessage;
 import org.jdesktop.wonderland.modules.audiomanager.common.messages.voicechat.VoiceChatMessage.ChatType;
 
 import org.jdesktop.wonderland.client.comms.WonderlandSession;
 
 import org.jdesktop.wonderland.client.hud.CompassLayout.Layout;
 import org.jdesktop.wonderland.client.hud.CompassLayout.Layout;
 
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
     private static ArrayList<AddHUDPanel> addHUDPanelList = new ArrayList();
     private PropertyChangeSupport listeners;
     private HUDComponent addHUDComponent;
     private int normalHeight = 0;
 
     public AddHUDPanel() {
         initComponents();
         setMode(Mode.INITIATE);
     }
 
     public AddHUDPanel(AudioManagerClient client, WonderlandSession session,
             PresenceInfo myPresenceInfo, PresenceInfo caller) {
 
         this(client, session, myPresenceInfo, caller, null, Mode.INITIATE);
     }
 
     public AddHUDPanel(AudioManagerClient client, WonderlandSession session,
             PresenceInfo myPresenceInfo, PresenceInfo caller, String group, Mode mode) {
 
         this.client = client;
         this.session = session;
         this.myPresenceInfo = myPresenceInfo;
         this.caller = caller;
 
         if (group == null) {
             group = caller.userID.getUsername() + "-" + groupNumber++;
         }
 
         this.group = group;
 
 	//System.out.println("NEW HUD For " + group);
 
         initComponents();
 
         setMode(mode);
 
 	setEnabledInviteButton();
 	setEnabledActionButton();
 
         pm = PresenceManagerFactory.getPresenceManager(session);
 
 	addHUDPanelList.add(this);
 	
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
 
     public void setPreferredLocation(Layout location) {
 	if (addHUDPanelList.size() == 1) {
 	    addHUDComponent.setPreferredLocation(location);
 	    return;
 	}
 
 	setLocation(0, 0);
     }
 
     public void setLocation(int x, int y) {
         AddHUDPanel[] addHUDPanels = addHUDPanelList.toArray(new AddHUDPanel[0]);
 
         for (AddHUDPanel addHUDPanel : addHUDPanelList) {
             if (addHUDPanel == this) {
                 continue;
 	    }
 
 	    HUDComponent addHUDComponent = addHUDPanel.getHUDComponent();
 
 	    Point p = addHUDComponent.getLocation();
 
 	    //System.out.println("x " + x + " y " + y + " Location " + p + " width " + addHUDComponent.getWidth());
 
 	    if (p.getX() >= x) {
 		x = (int) (p.getX() + addHUDComponent.getWidth());
 		y = (int) p.getY();
 	    }	    
 	}
 
 	addHUDComponent.setLocation(x, y);
     }
 
     public void setPhoneType() {
         addTypePanel.setPhoneType();
         showAddPhonePanel(true, true);
         addInitiateButtonPanel.setEnabledActionButton(false);
 	addInitiateButtonPanel.setActionButtonText("Call");
         userMode = false;
     }
 
     public void inviteUsers(ArrayList<PresenceInfo> usersToInvite) {
         addUserPanel.inviteUsers(usersToInvite);
         setMode(Mode.IN_PROGRESS);
     }
 
     public void setClosed() {
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
 
     public Mode getMode() {
 	return mode;
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
         	    setEnabledInviteButton();
                     userMode = true;
                 }
             });
             addTypePanel.addPhoneModeListener(new ActionListener() {
 
                 public void actionPerformed(ActionEvent e) {
                     showAddPhonePanel(true, (mode != Mode.ADD));
                     addInitiateButtonPanel.setActionButtonText("Call");
 		    setEnabledActionButton();
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
		caller, group, Mode.ADD);
 
 	    addUserPanel.addUserListSelectionListener(new javax.swing.event.ListSelectionListener() {
                 public void valueChanged(javax.swing.event.ListSelectionEvent e) {
                     addUserListValueChanged(e);
                 }
             });
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
 
 	    addPhoneUserPanel.addNameTextActionListener(new java.awt.event.ActionListener() {
                 public void actionPerformed(java.awt.event.ActionEvent evt) {
                     phoneTextActionPerformed(evt);
                 }
             });
 
 	    addPhoneUserPanel.addPhoneTextActionListener(new java.awt.event.ActionListener() {
                 public void actionPerformed(java.awt.event.ActionEvent e) {
                     phoneTextActionPerformed(e);
                 }
             });
 
 	    addPhoneUserPanel.addNameTextKeyReleasedListener(new java.awt.event.KeyAdapter() {
                 public void keyReleased(java.awt.event.KeyEvent e) {
                     setEnabledActionButton();
                 }
             });
 	    addPhoneUserPanel.addPhoneTextKeyReleasedListener(new java.awt.event.KeyAdapter() {
                 public void keyReleased(java.awt.event.KeyEvent e) {
                     setEnabledActionButton();
                 }
             });
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
 		    takeOffHold();
                 }
             });
 
             holdPanel.addVolumeSliderChangeListener(new ChangeListener() {
 
 		public void stateChanged(javax.swing.event.ChangeEvent evt) {
 		    JSlider holdVolumeSlider = (JSlider) evt.getSource();
 
 		    setHoldVolume(holdVolumeSlider.getValue());
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
                     actionButtonActionPerformed();
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
 
     private void actionButtonActionPerformed() {
         if (userMode) {
             addUserPanel.inviteUsers();
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
 
 	    if (mode.equals(Mode.ADD)) {
 	        addHUDComponent.setVisible(false);
 	        addHUDComponent.setClosed();
 	    }
         }
 
 	if (mode.equals(Mode.INITIATE)) {
             setMode(Mode.IN_PROGRESS);
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
                     setMode(Mode.HOLD);
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
 
     private HUDComponent addModeAddHUDComponent;
 
     private void addButtonActionPerformed(ActionEvent e) {
         AddHUDPanel addHUDPanel =
                 new AddHUDPanel(client, session, myPresenceInfo, myPresenceInfo, group, Mode.ADD);
 
         HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
         addModeAddHUDComponent = mainHUD.createComponent(addHUDPanel);
         addHUDPanel.setHUDComponent(addModeAddHUDComponent);
 
         addModeAddHUDComponent.setName("Add to Voice Chat");
 
         addHUDPanel.setPreferredLocation(Layout.EAST);
         
         mainHUD.addComponent(addModeAddHUDComponent);
 
 	inProgressButtonPanel.setEnabledAddButton(false);
 
         addModeAddHUDComponent.addEventListener(new HUDEventListener() {
 
             public void HUDObjectChanged(HUDEvent e) {
                 if (e.getEventType().equals(HUDEventType.DISAPPEARED)) {
 		    inProgressButtonPanel.setEnabledAddButton(true);
                 }
             }
         });
 
         PropertyChangeListener plistener = new PropertyChangeListener() {
 
             public void propertyChange(PropertyChangeEvent pe) {
                 if (pe.getPropertyName().equals("ok") || pe.getPropertyName().equals("cancel")) {
                     addModeAddHUDComponent.setVisible(false);
                 }
             }
         };
 
         addHUDPanel.addPropertyChangeListener(plistener);
         addModeAddHUDComponent.setVisible(true);
     }
 
     private void leave() {
         session.send(client, new VoiceChatLeaveMessage(group, myPresenceInfo));
         addHUDComponent.setVisible(false);
         addHUDPanelList.remove(this);
 
 	if (addModeAddHUDComponent != null) {
 	    addModeAddHUDComponent.setVisible(false);
 	}
 
 	client.getWlAvatarCharacter().stop();
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
 	holdOtherCalls();
     }
 
     private void setHoldMode() {
         clearPanel();
         showHoldPanel(true);
 
 	int volume = holdPanel.getHoldVolume();
 
 	try {
             session.send(client, new VoiceChatHoldMessage(group, myPresenceInfo, true,
                 VolumeUtil.getServerVolume(volume)));
         } catch (IllegalStateException e) {
             leave();
         }
     }
 
     private void setHoldVolume(int volume) {
 	try {
             session.send(client, new VoiceChatHoldMessage(group, myPresenceInfo, true,
                 VolumeUtil.getServerVolume(volume)));
         } catch (IllegalStateException e) {
             leave();
         }
     }
     
     private void holdOtherCalls() {
         for (AddHUDPanel addHUDPanel : addHUDPanelList) {
             if (addHUDPanel == this) {
                 continue;
             }
 
 	    if (addHUDPanel.getMode().equals(Mode.IN_PROGRESS)) {
                 addHUDPanel.setMode(Mode.HOLD);
 	    }
         }
     }
 
     private void takeOffHold() {
 	try {
             session.send(client, new VoiceChatHoldMessage(group, myPresenceInfo, false, 1));
 	    setMode(Mode.IN_PROGRESS);
         } catch (IllegalStateException e) {
             leave();
         }
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
 
     private void addUserListValueChanged(javax.swing.event.ListSelectionEvent e) {
 	ArrayList<PresenceInfo> selectedValues = addUserPanel.getSelectedValues();
 
 	setEnabledInviteButton();
 	setEnabledActionButton();
 
 	if (inProgressButtonPanel == null) {
 	    return;
 	}
 
 	for (PresenceInfo info: selectedValues) {
 	    if (info.clientID != null) {
 		if (inProgressButtonPanel != null) {
 		    inProgressButtonPanel.setEnabledHangUpButton(false);
 		}
 		return;
 	    }
 	}
 
 	inProgressButtonPanel.setEnabledHangUpButton(true);
     }
 
     private void phoneTextActionPerformed(java.awt.event.ActionEvent e) {
 	if (addPhoneUserPanel.getPhoneName().length() > 0 &&
 	        addPhoneUserPanel.getPhoneNumber().length() > 0) {
 
 	    actionButtonActionPerformed();
 	}
     }
 
     private void setEnabledInviteButton() {
 	if (addInitiateButtonPanel == null) {
 	    return;
 	}
 
 	addInitiateButtonPanel.setEnabledActionButton(addUserPanel.getSelectedValues().size() > 0);
     }
 
     private void setEnabledActionButton() {
 	if (addInitiateButtonPanel == null || addPhoneUserPanel == null) {
 	    return;
 	}
 
 	boolean isEnabled = addPhoneUserPanel.getPhoneName().length() > 0 &&
 	    addPhoneUserPanel.getPhoneNumber().length() > 0;
 
         addInitiateButtonPanel.setEnabledActionButton(isEnabled);
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
