 /*
  * Copyright 2012 Sebastian Koppehel
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package de.bastisoft.ogre.gui;
 
 import java.awt.Component;
 import java.awt.Frame;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import javax.swing.DefaultListCellRenderer;
 import javax.swing.Icon;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.ListCellRenderer;
 
 import de.bastisoft.util.swing.SwingUtils;
 
 class ServerChoicePanel extends JPanel {
 
     private static final String RES_PREFIX              = "server.choice.";
     
     private static final String RES_LABEL               = RES_PREFIX + "label";
     private static final String RES_ADD_TOOLTIP         = RES_PREFIX + "add.tooltip";
     private static final String RES_DEL_TOOLTIP         = RES_PREFIX + "delete.tooltip";
     private static final String RES_EDIT_TOOLTIP        = RES_PREFIX + "edit.tooltip";
     private static final String RES_DEFAULT_NAME        = RES_PREFIX + "default.name";
     private static final String RES_CONFIRM_DEL_TITLE   = RES_PREFIX + "confirm.delete.dialog.title";
     private static final String RES_CONFIRM_DEL_MESSAGE = RES_PREFIX + "confirm.delete.dialog.message";
     
     private Frame parentFrame;
     private List<Server> servers;
     private ServerSelection selection;
     
     private JComboBox<Server> combo;
     private JButton addButton;
     private JButton delButton;
     private JButton editButton;
     
     private ServerSettingsDialog settingsDialog;
     
     private boolean autoUpdate;
     
     ServerChoicePanel(Frame parentFrame, ServerSelection selection) {
         this.parentFrame = parentFrame;
         this.selection = selection;
         servers = new ArrayList<>();
         
         makeWidgets();
         makeActions();
     }
     
     private void makeWidgets() {
         setLayout(new GridBagLayout());
         
         JLabel label = SwingUtils.makeLabel(Resources.label(RES_LABEL));
         
         combo = new JComboBox<>();
         combo.setRenderer(new ServerRenderer(combo.getRenderer()));
         label.setLabelFor(combo);
         
         
         // Icons for the buttons
         
         Icon addIcon = Resources.icon("add");
         Icon delIcon = Resources.icon("delete");
         Icon editIcon = Resources.icon("edit");
         
         
         // Buttons
         
         addButton = makeButton(addIcon, RES_ADD_TOOLTIP);
         delButton = makeButton(delIcon, RES_DEL_TOOLTIP);
         editButton = makeButton(editIcon, RES_EDIT_TOOLTIP);
         
         
         GridBagConstraints c = new GridBagConstraints();
         c.gridy = 0;
         c.gridwidth = 3;
         c.weightx = 1;
         c.fill = GridBagConstraints.HORIZONTAL;
         c.anchor = GridBagConstraints.BASELINE_LEADING;
         add(label, c);
         
         c.gridy++;
         c.anchor = GridBagConstraints.NORTH;
         c.insets = new Insets(5, 0, 0, 0);
         add(combo, c);
         
         c.gridy++;
         c.gridwidth = 1;
         c.fill = GridBagConstraints.VERTICAL;
         c.anchor = GridBagConstraints.FIRST_LINE_END;
         c.insets = new Insets(10, 0, 0, 0);
         add(addButton, c);
         
         c.weightx = 0;
         c.insets = new Insets(10, 5, 0, 0);
         add(delButton, c);
         add(editButton, c);
     }
     
     private JButton makeButton(Icon icon, String resource) {
         JButton button = new JButton();
         button.setIcon(icon);
         button.setToolTipText(Resources.string(resource));
         button.setMargin(new Insets(2, 2, 2, 2));
         return button;
     }
     
     private void makeActions() {
         combo.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 // Do not propagate server selection while we are filling the list
                 if (!autoUpdate)
                     selection.setSelected((Server) combo.getSelectedItem());
             }
         });
         
         addButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 addServer();
             }
         });
         
         delButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 deleteServer();
             }
         });
         
         editButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 showSettings();
             }
         });
     }
     
     private void showSettings() {
         Server selected = (Server) combo.getSelectedItem();
         if (selected == null)
             return;
         
         if (settingsDialog == null)
             settingsDialog = new ServerSettingsDialog(parentFrame);
         
         String oldName = selected.serverSettings.name;
         if (settingsDialog.run(selected.serverSettings)) {
             selected.serverSettings = settingsDialog.getServerSettings();
             if (!selected.serverSettings.name.equals(oldName))
                 combo.repaint();
         }
     }
     
     private void addServer() {
         int num = 0;
         boolean taken;
         String name;
         do {
             num++;
             name = Resources.string(RES_DEFAULT_NAME) + " " + num;
             taken = false;
             for (Server srv : servers)
                 if (name.equals(srv.serverSettings.name)) {
                     taken = true;
                     break;
                 }
         }
         while (taken && num < Integer.MAX_VALUE);
         
         ServerSettings settings = new ServerSettings(name);
         
         if (settingsDialog == null)
             settingsDialog = new ServerSettingsDialog(parentFrame);
         
         if (settingsDialog.run(settings)) {
             Server srv = new Server(settingsDialog.getServerSettings(), new QueryInputs());
             servers.add(srv);
             combo.addItem(srv);
             combo.setSelectedItem(srv);
             updateButtons();
         }
     }
     
     private void deleteServer() {
         Server selected = (Server) combo.getSelectedItem();
         if (selected == null)
             return;
         
         String message = MessageFormat.format(Resources.string(RES_CONFIRM_DEL_MESSAGE), selected.serverSettings.name);
         String title = Resources.string(RES_CONFIRM_DEL_TITLE);
         int answer = JOptionPane.showConfirmDialog(parentFrame, message, title, JOptionPane.YES_NO_OPTION);
         
         if (answer == JOptionPane.YES_OPTION) {
             servers.remove(selected);
             combo.removeItem(selected);
             updateButtons();
         }
     }
     
     private void updateButtons() {
         boolean enabled = getServers().size() > 0;
         delButton.setEnabled(enabled);
         editButton.setEnabled(enabled);
         combo.setEnabled(enabled);
     }
     
     List<Server> getServers() {
         return Collections.unmodifiableList(servers);
     }
     
     void setServers(List<Server> servers) {
         this.servers = new ArrayList<>(servers);
         
         autoUpdate = true;
         combo.removeAllItems();
         for (Server server : servers)
             combo.addItem(server);
         autoUpdate = false;
         
         if (servers.size() > 0) {
             int selectedIndex = servers.indexOf(selection.getSelected());
             combo.setSelectedIndex(Math.max(selectedIndex, 0));
         }
         
         updateButtons();
     }
     
     private class ServerRenderer implements ListCellRenderer<Server> {
         
         private ListCellRenderer<? super Server> orig;
         private DefaultListCellRenderer def;
 
         ServerRenderer(ListCellRenderer<? super Server> renderer) {
             if (renderer instanceof JLabel)
                 orig = renderer;
             else
                 def = new DefaultListCellRenderer();
         }
         
         @Override
         public Component getListCellRendererComponent(
                 JList<? extends Server> list,
                 Server value,
                 int index,
                 boolean isSelected,
                 boolean cellHasFocus) {
             
             JLabel label;
             
             if (orig != null) {
                 orig.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                 label = (JLabel) orig;
             }
             else {
                 def.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                 label = def;
             }
             
            label.setText(value.serverSettings.name);
             return label;
         }
         
     }
     
 }
