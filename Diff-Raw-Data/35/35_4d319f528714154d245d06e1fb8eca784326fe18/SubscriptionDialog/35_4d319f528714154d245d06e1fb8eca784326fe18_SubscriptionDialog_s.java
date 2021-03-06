 /**
  * $Revision$
  * $Date$
  *
  * Copyright (C) 1999-2005 Jive Software. All rights reserved.
  * This software is the proprietary information of Jive Software. Use is subject to license terms.
  */
 
 package org.jivesoftware.spark.ui;
 
 import org.jivesoftware.resource.Res;
 import org.jivesoftware.resource.SparkRes;
 import org.jivesoftware.smack.Roster;
 import org.jivesoftware.smack.RosterEntry;
 import org.jivesoftware.smack.RosterGroup;
 import org.jivesoftware.smack.XMPPException;
 import org.jivesoftware.smack.packet.Presence;
 import org.jivesoftware.smack.packet.RosterPacket;
 import org.jivesoftware.smack.util.StringUtils;
 import org.jivesoftware.spark.SparkManager;
 import org.jivesoftware.spark.UserManager;
 import org.jivesoftware.spark.component.RolloverButton;
 import org.jivesoftware.spark.component.TitlePanel;
 import org.jivesoftware.spark.component.borders.ComponentTitledBorder;
 import org.jivesoftware.spark.util.ModelUtil;
 import org.jivesoftware.spark.util.ResourceUtils;
 import org.jivesoftware.spark.util.log.Log;
 import org.jivesoftware.sparkimpl.plugin.gateways.transports.Transport;
 import org.jivesoftware.sparkimpl.plugin.gateways.transports.TransportUtils;
 
 import javax.swing.BorderFactory;
 import javax.swing.Icon;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 import java.awt.Frame;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 /**
  * SubscriptionDialog handles all subscription requests.
  *
  * @author Derek DeMoro
  */
 public class SubscriptionDialog {
 
     private TitlePanel messageLabel;
 
     private final RolloverButton acceptButton = new RolloverButton();
     private final RolloverButton viewInfoButton = new RolloverButton();
     private final RolloverButton denyButton = new RolloverButton();
     private final JPanel mainPanel;
 
     private final JCheckBox rosterBox = new JCheckBox();
 
     private final JLabel nicknameLabel = new JLabel();
     private final JTextField nicknameField = new JTextField();
 
     private final JLabel groupLabel = new JLabel();
     private final JComboBox groupBox = new JComboBox();
 
     private JLabel usernameLabel = new JLabel();
     private JLabel usernameLabelValue = new JLabel();
 
     private JFrame dialog;
 
     private String jid;
 
 
     public SubscriptionDialog() {
         mainPanel = new JPanel();
 
         mainPanel.setLayout(new GridBagLayout());
 
         // Add Roster Addition
         final JPanel rosterPanel = new JPanel();
         rosterPanel.setLayout(new GridBagLayout());
 
         // Add ResourceUtils
         ResourceUtils.resLabel(usernameLabel, nicknameField, Res.getString("label.username") + ":");
         ResourceUtils.resLabel(nicknameLabel, nicknameField, Res.getString("label.nickname") + ":");
         ResourceUtils.resLabel(groupLabel, groupBox, Res.getString("label.group") + ":");
 
 
         rosterBox.setText("Add user to your roster");
         groupBox.setEditable(true);
 
         rosterBox.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent actionEvent) {
                 nicknameField.setEnabled(rosterBox.isSelected());
                 groupBox.setEnabled(rosterBox.isSelected());
             }
         });
 
         rosterBox.setSelected(true);
 
 
         ComponentTitledBorder componentBorder = new ComponentTitledBorder(rosterBox, rosterPanel, BorderFactory.createEtchedBorder());
 
 

         rosterPanel.add(usernameLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
         rosterPanel.add(usernameLabelValue, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
 
 
         rosterPanel.add(nicknameLabel, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
         rosterPanel.add(nicknameField, new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
 
         rosterPanel.add(groupLabel, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
         rosterPanel.add(groupBox, new GridBagConstraints(1, 3, 1, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
         rosterPanel.add(new JLabel(), new GridBagConstraints(1, 4, 1, 1, 1.0, 1.0, GridBagConstraints.SOUTH, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
 
         // Add Roster Panel to mainPanel
         mainPanel.add(rosterPanel, new GridBagConstraints(2, 1, 5, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
         rosterPanel.setBorder(componentBorder);
 
         // Add Buttons
         ResourceUtils.resButton(acceptButton, Res.getString("button.accept"));
         ResourceUtils.resButton(viewInfoButton, Res.getString("button.profile"));
         ResourceUtils.resButton(denyButton, Res.getString("button.deny"));
 
        mainPanel.add(acceptButton, new GridBagConstraints(3, 2, 1, 1, 1.0, 0.0, GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
        mainPanel.add(viewInfoButton, new GridBagConstraints(4, 2, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
        mainPanel.add(denyButton, new GridBagConstraints(5, 2, 1, 1, 0.0, 1.0, GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
 
         // Set Group Box
         for (ContactGroup group : SparkManager.getWorkspace().getContactList().getContactGroups()) {
             if (!group.isOfflineGroup() && !"Unfiled".equalsIgnoreCase(group.getGroupName()) && !group.isSharedGroup()) {
                 groupBox.addItem(group.getGroupName());
             }
         }
 
         groupBox.setEditable(true);
 
         if (groupBox.getItemCount() == 0) {
             groupBox.addItem("Friends");
         }
 
         if (groupBox.getItemCount() > 0) {
             groupBox.setSelectedIndex(0);
         }
     }
 
     public void invoke(final String jid) {
         this.jid = jid;
 
         final Roster roster = SparkManager.getConnection().getRoster();
 
         // If User is already in roster, do not show.
         RosterEntry entry = roster.getEntry(jid);
         if (entry != null && entry.getType() == RosterPacket.ItemType.TO) {
             Presence response = new Presence(Presence.Type.subscribed);
             response.setTo(jid);
 
             SparkManager.getConnection().sendPacket(response);
             return;
         }
 
         String message = Res.getString("message.approve.subscription", UserManager.unescapeJID(jid));
         Transport transport = TransportUtils.getTransport(StringUtils.parseServer(jid));
         Icon icon = null;
         if (transport != null) {
             icon = transport.getIcon();
         }
 
         messageLabel = new TitlePanel("", message, icon, true);
 
         // Add Message Label
         mainPanel.add(messageLabel, new GridBagConstraints(0, 0, 6, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
 
 
         String username = StringUtils.parseName(UserManager.unescapeJID(jid));
         usernameLabelValue.setText(UserManager.unescapeJID(jid));
         nicknameField.setText(username);
 
         acceptButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 if (!rosterBox.isSelected()) {
                     Presence response = new Presence(Presence.Type.subscribed);
                     response.setTo(jid);
                     SparkManager.getConnection().sendPacket(response);
                     dialog.dispose();
                     return;
                 }
 
                 boolean addEntry = addEntry();
                 if (addEntry) {
                     Presence response = new Presence(Presence.Type.subscribed);
                     response.setTo(jid);
                     SparkManager.getConnection().sendPacket(response);
                 }
                 else {
                     dialog.dispose();
                 }
             }
         });
 
         denyButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 // Send subscribed
                 Presence response = new Presence(Presence.Type.unsubscribe);
                 response.setTo(jid);
                 SparkManager.getConnection().sendPacket(response);
 
                 dialog.dispose();
             }
         });
 
         viewInfoButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 SparkManager.getVCardManager().viewProfile(jid, mainPanel);
             }
         });
 
        dialog = new JFrame("Subscription Request");
 
 
         dialog.setIconImage(SparkRes.getImageIcon(SparkRes.MAIN_IMAGE).getImage());
         dialog.getContentPane().add(mainPanel);
         dialog.pack();
        dialog.setSize(400, 250);
         dialog.setLocationRelativeTo(SparkManager.getMainWindow());
 
 
         if (SparkManager.getMainWindow().isFocused()) {
             dialog.setState(Frame.NORMAL);
             dialog.setVisible(true);
         }
         else if (!SparkManager.getMainWindow().isVisible() || !SparkManager.getMainWindow().isFocused()) {
            dialog.setFocusableWindowState(false);
            dialog.setState(Frame.ICONIFIED);
             dialog.setVisible(true);
            SparkManager.getAlertManager().flashWindowStopOnFocus(dialog);
             dialog.setFocusableWindowState(true);
         }
     }
 
 
     private boolean addEntry() {
         String errorMessage = Res.getString("title.error");
         String nickname = nicknameField.getText();
         String group = (String)groupBox.getSelectedItem();
 
 
         ContactGroup contactGroup = SparkManager.getWorkspace().getContactList().getContactGroup(group);
         boolean isSharedGroup = contactGroup != null && contactGroup.isSharedGroup();
 
         if (isSharedGroup) {
             errorMessage = Res.getString("message.cannot.add.contact.to.shared.group");
         }
         else if (!ModelUtil.hasLength(nickname)) {
             errorMessage = Res.getString("message.specify.contact.jid");
         }
         else if (!ModelUtil.hasLength(group)) {
             errorMessage = Res.getString("message.specify.group");
         }
         else if (ModelUtil.hasLength(nickname) && ModelUtil.hasLength(group) && !isSharedGroup) {
             addEntry(jid, nickname, group);
             dialog.setVisible(false);
             return true;
         }
 
         JOptionPane.showMessageDialog(dialog, errorMessage, Res.getString("title.error"), JOptionPane.ERROR_MESSAGE);
         return false;
     }
 
 
     /**
      * Adds a new entry to the users Roster.
      *
      * @param jid      the jid.
      * @param nickname the nickname.
      * @param group    the contact group.
      * @return the new RosterEntry.
      */
     public RosterEntry addEntry(String jid, String nickname, String group) {
         String[] groups = {group};
 
         Roster roster = SparkManager.getConnection().getRoster();
         RosterEntry userEntry = roster.getEntry(jid);
 
         boolean isSubscribed = true;
         if (userEntry != null) {
             isSubscribed = userEntry.getGroups().size() == 0;
         }
 
         if (isSubscribed) {
             try {
                 roster.createEntry(jid, nickname, new String[]{group});
             }
             catch (XMPPException e) {
                 Log.error("Unable to add new entry " + jid, e);
             }
             return roster.getEntry(jid);
         }
 
 
         try {
             RosterGroup rosterGroup = roster.getGroup(group);
             if (rosterGroup == null) {
                 rosterGroup = roster.createGroup(group);
             }
 
             if (userEntry == null) {
                 roster.createEntry(jid, nickname, groups);
                 userEntry = roster.getEntry(jid);
             }
             else {
                 userEntry.setName(nickname);
                 rosterGroup.addEntry(userEntry);
             }
 
             userEntry = roster.getEntry(jid);
         }
         catch (XMPPException ex) {
             Log.error(ex);
         }
         return userEntry;
     }
 }
