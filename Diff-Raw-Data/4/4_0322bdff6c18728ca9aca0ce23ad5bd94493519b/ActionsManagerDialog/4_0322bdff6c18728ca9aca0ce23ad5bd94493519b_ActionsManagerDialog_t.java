 /*
  * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes
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
 
 package com.dmdirc.addons.ui_swing.dialogs.actionsmanager;
 
 import com.dmdirc.actions.Action;
 import com.dmdirc.actions.ActionGroup;
 import com.dmdirc.actions.ActionManager;
 import com.dmdirc.actions.CoreActionType;
 import com.dmdirc.actions.interfaces.ActionType;
 import com.dmdirc.addons.ui_swing.Apple;
 import com.dmdirc.addons.ui_swing.MainFrame;
 import com.dmdirc.addons.ui_swing.SwingController;
 import com.dmdirc.addons.ui_swing.components.ListScroller;
 import com.dmdirc.addons.ui_swing.components.SortedListModel;
 import com.dmdirc.addons.ui_swing.components.frames.AppleJFrame;
 import com.dmdirc.addons.ui_swing.components.renderers.ActionGroupListCellRenderer;
 import com.dmdirc.addons.ui_swing.components.text.TextLabel;
 import com.dmdirc.addons.ui_swing.components.validating.NoDuplicatesInListValidator;
 import com.dmdirc.addons.ui_swing.dialogs.StandardDialog;
 import com.dmdirc.addons.ui_swing.dialogs.StandardInputDialog;
 import com.dmdirc.addons.ui_swing.dialogs.StandardQuestionDialog;
 import com.dmdirc.config.IdentityManager;
 import com.dmdirc.util.validators.FileNameValidator;
 import com.dmdirc.util.validators.ValidatorChain;
 
 import java.awt.Window;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import javax.swing.BorderFactory;
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.ListSelectionModel;
 import javax.swing.UIManager;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import net.miginfocom.swing.MigLayout;
 
 /**
  * Allows the user to manage actions.
  */
 public final class ActionsManagerDialog extends StandardDialog implements
         ActionListener, com.dmdirc.interfaces.ActionListener,
         ListSelectionListener {
 
     /**
      * A version number for this class. It should be changed whenever the class
      * structure is changed (or anything else that would prevent serialized
      * objects being unserialized with the new class).
      */
     private static final long serialVersionUID = 1;
     /** Previously created instance of ActionsManagerDialog. */
     private static volatile ActionsManagerDialog me;
     /** Info label. */
     private TextLabel infoLabel;
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
     /** Group panel. */
     private JPanel groupPanel;
     /** Are we saving? */
     private final AtomicBoolean saving = new AtomicBoolean(false);
     /** Duplicate action group validator. */
     private ValidatorChain<String> validator;
 
     /** 
      * Creates a new instance of ActionsManagerDialog.
      */
     @SuppressWarnings("unchecked")
     private ActionsManagerDialog(final Window parentWindow,
             final SwingController controller) {
         super(Apple.isAppleUI() ? new AppleJFrame((MainFrame) parentWindow,
                 controller) : null, ModalityType.MODELESS);
 
         initComponents();
         validator = new ValidatorChain<String>(
                 new ActionGroupNoDuplicatesInListValidator(groups,
                 (DefaultListModel) groups.getModel()), new FileNameValidator());
         addListeners();
         layoutGroupPanel();
         layoutComponents();
 
         setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
         setTitle("Actions Manager");
         setResizable(false);
     }
 
     /** 
      * Creates the dialog if one doesn't exist, and displays it. 
      * 
      * @param parentWindow Parent window
      * @param controller Swing controller
      */
     public static void showActionsManagerDialog(final Window parentWindow,
             final SwingController controller) {
         getActionsManagerDialog(parentWindow, controller);
 
         me.setIconImages(parentWindow.getIconImages());
         me.display();
         me.requestFocusInWindow();
 
         final int selected = IdentityManager.getGlobalConfig().
                 getOptionInt("dialogstate", "actionsmanagerdialog");
         if (selected >= 0 && selected < me.groups.getModel().getSize()) {
             me.groups.setSelectedIndex(selected);
             me.changeActiveGroup((ActionGroup) me.groups.getModel().getElementAt(
                     selected));
         }
     }
 
     /**
      * Returns the current instance of the ActionsManagerDialog.
      *
      * @param parentWindow Parent window
      * @param controller Swing controller
      *
      * @return The current ActionsManagerDialog instance
      */
     public static ActionsManagerDialog getActionsManagerDialog(
             final Window parentWindow,
             final SwingController controller) {
         synchronized (ActionsManagerDialog.class) {
             if (me == null) {
                 me = new ActionsManagerDialog(parentWindow, controller);
             } else {
                 me.reloadGroups();
             }
         }
 
         return me;
     }
 
     /**
      * Initialises the components.
      */
     private void initComponents() {
         orderButtons(new JButton(), new JButton());
         infoLabel = new TextLabel("Actions allow you to make DMDirc"
                 + " intelligently respond to various events.  Action groups are "
                 + "there for you to organise groups, add or remove them "
                 + "to suit your needs.");
         groups = new JList(new SortedListModel<ActionGroup>(
                 new ActionGroupNameComparator()));
         actions = new ActionsGroupPanel(this, null);
         info = new ActionGroupInformationPanel(null);
         settings = new HashMap<ActionGroup, ActionGroupSettingsPanel>();
         activeSettings = new ActionGroupSettingsPanel(null, this);
         settings.put(null, activeSettings);
         add = new JButton("Add");
         edit = new JButton("Edit");
         delete = new JButton("Delete");
         groupPanel = new JPanel();
         groupPanel.setName("Groups");
 
         groupPanel.setBorder(BorderFactory.createTitledBorder(UIManager.
                 getBorder("TitledBorder.border"),
                 "Groups"));
         info.setBorder(BorderFactory.createTitledBorder(UIManager.getBorder(
                 "TitledBorder.border"),
                 "Information"));
         actions.setBorder(BorderFactory.createTitledBorder(UIManager.getBorder(
                 "TitledBorder.border"),
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
         getCancelButton().addActionListener(this);
         add.addActionListener(this);
         edit.addActionListener(this);
         delete.addActionListener(this);
         groups.getSelectionModel().addListSelectionListener(this);
         ActionManager.addListener(this, CoreActionType.ACTION_CREATED);
         ActionManager.addListener(this, CoreActionType.ACTION_UPDATED);
         ActionManager.addListener(this, CoreActionType.ACTION_DELETED);
     }
 
     /**
      * Lays out the group panel.
      */
     private void layoutGroupPanel() {
         groupPanel.setLayout(new MigLayout("fill, wrap 1"));
 
         groupPanel.add(new JScrollPane(groups), "growy, pushy, w 150!");
         groupPanel.add(add, "sgx button, w 150!");
         groupPanel.add(edit, "sgx button, w 150!");
         groupPanel.add(delete, "sgx button, w 150!");
     }
 
     /**
      * Lays out the components.
      */
     private void layoutComponents() {
 
         getContentPane().setLayout(new MigLayout(
                 "fill, wrap 2, hidemode 3, wmax 800"));
 
         getContentPane().add(infoLabel, "spanx 2, growx");
         if (info.isVisible() && activeSettings.isVisible()) {
             getContentPane().add(groupPanel, "growy, pushy, spany 3");
         } else if (info.isVisible() || activeSettings.isVisible()) {
             getContentPane().add(groupPanel, "growy, pushy, spany 2");
         } else {
             getContentPane().add(groupPanel, "growy, pushy");
         }
         getContentPane().add(info, "growx, pushx");
         getContentPane().add(actions, "grow, push");
         getContentPane().add(activeSettings, "growx, pushx");
         getContentPane().add(getOkButton(), "skip, right, sgx button");
     }
 
     /**
      * Reloads the action groups.
      */
     private void reloadGroups() {
         reloadGroups(null);
     }
 
     /**
      * Reloads the action groups.
      * 
      * @param selectedGroup Newly selected group
      */
     private void reloadGroups(final ActionGroup selectedGroup) {
         ((DefaultListModel) groups.getModel()).clear();
         for (ActionGroup group : ActionManager.getGroups().values()) {
             ((DefaultListModel) groups.getModel()).addElement(group);
         }
         groups.setSelectedValue(selectedGroup, true);
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
             final ActionGroupSettingsPanel currentSettings =
                     new ActionGroupSettingsPanel(group, this);
             settings.put(group, currentSettings);
             currentSettings.setBorder(BorderFactory.createTitledBorder(
                     UIManager.getBorder("TitledBorder.border"), "Settings"));
         }
         activeSettings = settings.get(group);
 
         info.setVisible(info.shouldDisplay());
         activeSettings.setVisible(activeSettings.shouldDisplay());
 
         getContentPane().setVisible(false);
         getContentPane().removeAll();
         layoutComponents();
         validate();
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
         } else if ((e.getSource() == getOkButton() || e.getSource()
                == getCancelButton()) && !saving.getAndSet(true)) {
             for (ActionGroupSettingsPanel loopSettings : settings.values()) {
                 loopSettings.save();
             }
             IdentityManager.getConfigIdentity().setOption("dialogstate",
                     "actionsmanagerdialog", groups.getSelectedIndex());
             dispose();
        }
     }
 
     /**
      * Prompts then adds an action group.
      */
     private void addGroup() {
         final int index = groups.getSelectedIndex();
         groups.getSelectionModel().clearSelection();
         final StandardInputDialog inputDialog = new StandardInputDialog(this,
                 ModalityType.DOCUMENT_MODAL, "New action group",
                 "Please enter the name of the new action group", validator) {
 
             /**
              * A version number for this class. It should be changed whenever the class
              * structure is changed (or anything else that would prevent serialized
              * objects being unserialized with the new class).
              */
             private static final long serialVersionUID = 1;
 
             /** {@inheritDoc} */
             @Override
             public boolean save() {
                 if (!saving.getAndSet(true)) {
                     groups.setSelectedIndex(index);
                     if (getText() == null || getText().isEmpty() && !ActionManager.
                             getGroups().
                             containsKey(getText())) {
                         return false;
                     } else {
                         final ActionGroup group =
                                 ActionManager.makeGroup(getText());
                         reloadGroups(group);
                         return true;
                     }
                 }
                 return false;
             }
 
             /** {@inheritDoc} */
             @Override
             public void cancelled() {
                 groups.setSelectedIndex(index);
             }
         };
         inputDialog.display(this);
     }
 
     /**
      * Prompts then edits an action group.
      */
     private void editGroup() {
         final String oldName =
                 ((ActionGroup) groups.getSelectedValue()).getName();
         final StandardInputDialog inputDialog = new StandardInputDialog(this,
                 ModalityType.DOCUMENT_MODAL, "Edit action group",
                 "Please enter the new name of the action group", validator) {
 
             /**
              * A version number for this class. It should be changed whenever the class
              * structure is changed (or anything eloh blese that would prevent serialized
              * objects being unserialized with the new class).
              */
             private static final long serialVersionUID = 1;
 
             /** {@inheritDoc} */
             @Override
             public boolean save() {
                 if (!saving.getAndSet(true)) {
                     if (getText() == null || getText().isEmpty()) {
                         return false;
                     } else {
                         ActionManager.renameGroup(oldName, getText());
                         reloadGroups();
                         return true;
                     }
                 }
                 return false;
             }
 
             /** {@inheritDoc} */
             @Override
             public void cancelled() {
                 //Ignore
             }
         };
         inputDialog.setText(oldName);
         inputDialog.display(this);
     }
 
     /**
      * Prompts then deletes an action group.
      */
     private void delGroup() {
         final String group =
                 ((ActionGroup) groups.getSelectedValue()).getName();
         new StandardQuestionDialog(this,
                 ModalityType.APPLICATION_MODAL,
                 "Confirm deletion",
                 "Are you sure you wish to delete the '" + group
                 + "' group and all actions within it?") {
 
             /**
              * A version number for this class. It should be changed whenever the class
              * structure is changed (or anything else that would prevent serialized
              * objects being unserialized with the new class).
              */
             private static final long serialVersionUID = 1;
 
             /** {@inheritDoc} */
             @Override
             public boolean save() {
                 int location =
                         ((DefaultListModel) groups.getModel()).indexOf(
                         ActionManager.getGroup(group));
                 ActionManager.removeGroup(group);
                 reloadGroups();
                 if (groups.getModel().getSize() == 0) {
                     location = -1;
                 } else if (location >= groups.getModel().getSize()) {
                     location = groups.getModel().getSize();
                 } else if (location <= 0) {
                     location = 0;
                 }
                 groups.setSelectedIndex(location);
                 return true;
             }
 
             /** {@inheritDoc} */
             @Override
             public void cancelled() {
                 return;
             }
         }.display();
     }
 
     /** {@inheritDoc} */
     @Override
     public void valueChanged(final ListSelectionEvent e) {
         if (e.getValueIsAdjusting()) {
             return;
         }
 
         changeActiveGroup((ActionGroup) groups.getSelectedValue());
         if (groups.getSelectedIndex() == -1 || !((ActionGroup) groups.
                 getSelectedValue()).isDelible()) {
             edit.setEnabled(false);
             delete.setEnabled(false);
         } else {
             edit.setEnabled(true);
             delete.setEnabled(true);
         }
     }
 
     /** {@inheritDoc} */
     @Override
     public void processEvent(final ActionType type, final StringBuffer format,
             final Object... arguments) {
         if (groups.getSelectedValue() == null) {
             return;
         }
         if (type.equals(CoreActionType.ACTION_CREATED) || type.equals(
                 CoreActionType.ACTION_UPDATED)) {
             final Action action = (Action) arguments[0];
             if (action.getGroup().equals(((ActionGroup) groups.getSelectedValue()).
                     getName())) {
                 actions.actionChanged(action);
             }
         } else {
             if (arguments[0].equals(((ActionGroup) groups.getSelectedValue()).
                     getName())) {
                 actions.actionDeleted((String) arguments[1]);
             }
         }
     }
 
     /** {@inheritDoc} */
     @Override
     public void dispose() {
         if (me == null) {
             return;
         }
         synchronized (me) {
             super.dispose();
             me = null;
         }
     }
 }
 
 /**
  * No duplicates list validator, overriden to work with action groups.
  */
 class ActionGroupNoDuplicatesInListValidator extends NoDuplicatesInListValidator {
 
     /**
      * Creates a new validator.
      *
      * @param list List
      * @param model Model to validate
      */
     public ActionGroupNoDuplicatesInListValidator(final JList list,
             final DefaultListModel model) {
         super(true, list, model);
     }
 
     /**
      * Creates a new validator.
      *
      * @param list List
      * @param caseSensitive Case sensitive check?
      * @param model Model to validate
      */
     public ActionGroupNoDuplicatesInListValidator(final boolean caseSensitive,
             final JList list, final DefaultListModel model) {
         super(caseSensitive, list, model);
     }
     
     /** {@inheritDoc} */
     @Override
     public String listValueToString(final Object object) {
         return ((ActionGroup) object).getName();
     }
 
     /** {@inheritDoc} */
     @Override
     public int indexOfString(final String string) {
         final String value = caseSensitive ? string : string.toLowerCase();
         int index = -1;
         for (int i = 0; i < model.getSize(); i++) {
             if (((ActionGroup) model.get(i)).getName().equals(value)) {
                 index = i;
                 break;
             }
         }
         return index;
     }
 }
