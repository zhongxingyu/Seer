 /*
  * Copyright (c) 2006-2008 Chris Smith, Shane Mc Cormack, Gregory Holmes
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 
 package com.dmdirc.ui.swing.dialogs.actionsmanager;
 
 import com.dmdirc.Main;
 import com.dmdirc.actions.ActionGroup;
 import com.dmdirc.actions.ActionManager;
 import com.dmdirc.actions.CoreActionType;
 import com.dmdirc.actions.interfaces.ActionType;
 import com.dmdirc.config.prefs.validator.RegexStringValidator;
 import com.dmdirc.ui.swing.components.JWrappingLabel;
 import com.dmdirc.ui.swing.MainFrame;
 import com.dmdirc.ui.swing.SwingController;
 import com.dmdirc.ui.swing.components.ListScroller;
 import com.dmdirc.ui.swing.components.StandardDialog;
 import com.dmdirc.ui.swing.components.StandardInputDialog;
 import com.dmdirc.ui.swing.components.renderers.ActionGroupListCellRenderer;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.swing.BorderFactory;
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.ListSelectionModel;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import net.miginfocom.swing.MigLayout;
 
 /**
  * Allows the user to manage actions.
  */
 public final class ActionsManagerDialog extends StandardDialog implements ActionListener,
         ListSelectionListener, com.dmdirc.interfaces.ActionListener {
 
     /**
      * A version number for this class. It should be changed whenever the class
      * structure is changed (or anything else that would prevent serialized
      * objects being unserialized with the new class).
      */
     private static final long serialVersionUID = 1;
     /** Previously created instance of ActionsManagerDialog. */
     private static ActionsManagerDialog me;
     /** Info label. */
     private JWrappingLabel infoLabel;
     /** Group list. */
     private JList groups;
     /** Add button. */
     private JButton add;
     /** Edit button. */
     private JButton edit;
     /** Delete button. */
     private JButton delete;
     /** Info panel. */
     private ActionGroupInformationPanel info;
     /** Actions panel. */
     private ActionsGroupPanel actions;
     /** Settings panels. */
     private Map<ActionGroup, ActionGroupSettingsPanel> settings;
     /** Active s panel. */
     private ActionGroupSettingsPanel activeSettings;
    /** Filename regex. */
    private static final String FILENAME_REGEX = "[A-Za-z0-9 ]+";
     /** Group panel. */
     private JPanel groupPanel;
 
     /** Creates a new instance of ActionsManagerDialog. */
     private ActionsManagerDialog() {
         super((MainFrame) Main.getUI().getMainWindow(), false);
 
         initComponents();
         addListeners();
         layoutGroupPanel();
         layoutComponents();
 
         setDefaultCloseOperation(DISPOSE_ON_CLOSE);
         setTitle("DMDirc: Action Manager");
         setResizable(false);
     }
 
     /** Creates the dialog if one doesn't exist, and displays it. */
     public static synchronized void showActionsManagerDialog() {
         me = getActionsManagerDialog();
 
         me.pack();
         me.setLocationRelativeTo((MainFrame) Main.getUI().getMainWindow());
         me.setVisible(true);
         me.requestFocus();
     }
 
     /**
      * Returns the current instance of the ActionsManagerDialog.
      *
      * @return The current ActionsManagerDialog instance
      */
     public static synchronized ActionsManagerDialog getActionsManagerDialog() {
         if (me == null) {
             me = new ActionsManagerDialog();
         } else {
             me.reloadGroups();
         }
 
         return me;
     }
 
     /**
      * Initialises the components.
      */
     private void initComponents() {
         orderButtons(new JButton(), new JButton());
        infoLabel = new JWrappingLabel("Actions allow you to automate many " +
                "aspects of DMDirc, they alow you to intelligently respond " +
                "to different events.");
         groups = new JList(new DefaultListModel());
         actions = new ActionsGroupPanel(null);
         info = new ActionGroupInformationPanel(null);
         settings = new HashMap<ActionGroup, ActionGroupSettingsPanel>();
         activeSettings = new ActionGroupSettingsPanel(null);
         settings.put(null, activeSettings);
         add = new JButton("Add");
         edit = new JButton("Edit");
         delete = new JButton("Delete");
         groupPanel = new JPanel();
        
         groupPanel.setBorder(BorderFactory.createTitledBorder(groupPanel.getBorder(),
                 "Groups"));
         info.setBorder(BorderFactory.createTitledBorder(info.getBorder(),
                 "Information"));
         actions.setBorder(BorderFactory.createTitledBorder(actions.getBorder(),
                 "Actions"));
 
         groups.setCellRenderer(new ActionGroupListCellRenderer());
         groups.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         edit.setEnabled(false);
         delete.setEnabled(false);
 
         info.setVisible(false);
         activeSettings.setVisible(false);
        
         new ListScroller(groups);
 
         reloadGroups();
     }
 
     /**
      * Adds listeners.
      */
     private void addListeners() {
         getOkButton().addActionListener(this);
         add.addActionListener(this);
         edit.addActionListener(this);
         delete.addActionListener(this);
         groups.getSelectionModel().addListSelectionListener(this);
         ActionManager.addListener(this, CoreActionType.ACTION_CREATED);
         ActionManager.addListener(this, CoreActionType.ACTION_UPDATED);
     }
    
     /**
      * Lays out the group panel.
      */
     private void layoutGroupPanel() {
         groupPanel.setLayout(new MigLayout("fill, wrap 1"));
 
         groupPanel.add(new JScrollPane(groups), "growy, w 150!");
         groupPanel.add(add, "sgx button, w 150!");
         groupPanel.add(edit, "sgx button, w 150!");
         groupPanel.add(delete, "sgx button, w 150!");
     }
 
     /**
      * Lays out the components.
      */
     private void layoutComponents() {
 
         getContentPane().setLayout(new MigLayout("fill, wrap 2, hidemode 3"));
 
         getContentPane().add(infoLabel, "spanx 2");
         if (info.isVisible() && activeSettings.isVisible()) {
             getContentPane().add(groupPanel, "growy, spany 3");
         } else if (info.isVisible() || activeSettings.isVisible()) {
             getContentPane().add(groupPanel, "growy, spany 2");
         } else {
             getContentPane().add(groupPanel, "growy");
         }
         getContentPane().add(info, "growx");
         getContentPane().add(actions, "grow");
         getContentPane().add(activeSettings, "growx");
         getContentPane().add(getRightButton(), "skip, right, sgx button");
     }
 
     /**
      * Reloads the action groups.
      */
     private void reloadGroups() {
         ((DefaultListModel) groups.getModel()).clear();
         for (ActionGroup group : ActionManager.getGroups().values()) {
             ((DefaultListModel) groups.getModel()).addElement(group);
         }
     }
 
     /**
      * Changes the active group.
     * 
      * @param group New group
      */
     private void changeActiveGroup(final ActionGroup group) {
         info.setActionGroup(group);
         actions.setActionGroup(group);
         if (!settings.containsKey(group)) {
             final ActionGroupSettingsPanel currentSettings = new ActionGroupSettingsPanel(group);
             settings.put(group, currentSettings);
             currentSettings.setBorder(BorderFactory.createTitledBorder(currentSettings.getBorder(),
                 "Settings"));
         }
         activeSettings = settings.get(group);
        
         info.setVisible(info.shouldDisplay());
         activeSettings.setVisible(activeSettings.shouldDisplay());
        
         getContentPane().setVisible(false);
         getContentPane().removeAll();
         layoutComponents();
         getContentPane().setVisible(true);
     }
 
    /** 
      * {@inheritDoc}
     * 
      * @param e Action event
      */
     @Override
     public void actionPerformed(final ActionEvent e) {
         if (e.getSource() == add) {
             addGroup();
         } else if (e.getSource() == edit) {
             editGroup();
         } else if (e.getSource() == delete) {
             delGroup();
         } else if (e.getSource() == getOkButton()) {
             for (ActionGroupSettingsPanel loopSettings : settings.values()) {
                 loopSettings.save();
             }
             dispose();
         }
     }
 
     /**
      * Prompts then adds an action group.
      */
     private void addGroup() {
         final StandardInputDialog inputDialog = new StandardInputDialog(SwingController.getMainFrame(), false,
                 "New action group",
                 "Please enter the name of the new action group",
                 new RegexStringValidator(FILENAME_REGEX,
                 "Must be a valid filename")) {
 
             /**
              * A version number for this class. It should be changed whenever the class
              * structure is changed (or anything else that would prevent serialized
              * objects being unserialized with the new class).
              */
             private static final long serialVersionUID = 1;
 
             /** {@inheritDoc} */
             @Override
             public boolean save() {
                 if (getText() == null || getText().isEmpty()) {
                     return false;
                 } else {
                     ActionManager.makeGroup(getText());
                     reloadGroups();
                     return true;
                 }
             }
 
             /** {@inheritDoc} */
             @Override
             public void cancelled() {
             //Ignore
             }
             };
         inputDialog.pack();
         inputDialog.setLocationRelativeTo(this);
         inputDialog.setVisible(true);
     }
 
     /**
      * Prompts then edits an action group.
      */
     private void editGroup() {
         final String oldName =
                 ((ActionGroup) groups.getSelectedValue()).getName();
         final StandardInputDialog inputDialog = new StandardInputDialog(SwingController.getMainFrame(), false,
                 "Edit action group",
                 "Please enter the new name of the action group",
                 new RegexStringValidator(FILENAME_REGEX,
                 "Must be a valid filename")) {
 
             /**
              * A version number for this class. It should be changed whenever the class
              * structure is changed (or anything eloh blese that would prevent serialized
              * objects being unserialized with the new class).
              */
             private static final long serialVersionUID = 1;
 
             /** {@inheritDoc} */
             @Override
             public boolean save() {
                 if (getText() == null || getText().isEmpty()) {
                     return false;
                 } else {
                     ActionManager.renameGroup(oldName, getText());
                     reloadGroups();
                     return true;
                 }
             }
 
             /** {@inheritDoc} */
             @Override
             public void cancelled() {
             //Ignore
             }
             };
         inputDialog.pack();
         inputDialog.setLocationRelativeTo(this);
         inputDialog.setText(oldName);
         inputDialog.setVisible(true);
     }
 
     /**
      * Prompts then deletes an action group.
      */
     private void delGroup() {
         final String group =
                 ((ActionGroup) groups.getSelectedValue()).getName();
         final int response = JOptionPane.showConfirmDialog(this,
                 "Are you sure you wish to delete the '" + group +
                 "' group and all actions within it?",
                 "Confirm deletion", JOptionPane.YES_NO_OPTION);
         if (response == JOptionPane.YES_OPTION) {
             ActionManager.removeGroup(group);
             reloadGroups();
         }
     }
 
     /** {@inheritDoc} */
     @Override
     public void valueChanged(final ListSelectionEvent e) {
         if (e.getValueIsAdjusting()) {
             return;
         }
 
         if (groups.getSelectedIndex() == -1) {
             edit.setEnabled(false);
             delete.setEnabled(false);
         } else {
             edit.setEnabled(true);
             delete.setEnabled(true);
         }
         changeActiveGroup((ActionGroup) groups.getSelectedValue());
     }
 
     /** {@inheritDoc} */
     @Override
     public void dispose() {
         synchronized (me) {
             super.dispose();
             me = null;
         }
     }
 
     /** {@inheritDoc} */
     @Override
     public void processEvent(final ActionType type, final StringBuffer format,
             final Object... arguments) {
         reloadGroups();
     }
 }
