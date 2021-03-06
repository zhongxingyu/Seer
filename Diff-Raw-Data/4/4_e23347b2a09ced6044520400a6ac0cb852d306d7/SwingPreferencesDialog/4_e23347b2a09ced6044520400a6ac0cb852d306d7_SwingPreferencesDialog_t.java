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
 
 package com.dmdirc.ui.swing.dialogs.prefs;
 
 import com.dmdirc.ui.swing.components.TextLabel;
 import com.dmdirc.Main;
 import com.dmdirc.config.IdentityManager;
 import com.dmdirc.config.prefs.PreferencesCategory;
 import com.dmdirc.config.prefs.PreferencesManager;
 import com.dmdirc.config.prefs.PreferencesSetting;
 import com.dmdirc.config.prefs.validator.NumericalValidator;
 import com.dmdirc.ui.swing.MainFrame;
 import com.dmdirc.ui.swing.components.ColourChooser;
 import com.dmdirc.ui.swing.components.OptionalColourChooser;
 import com.dmdirc.ui.swing.components.StandardDialog;
 import com.dmdirc.ui.swing.components.TreeScroller;
 import com.dmdirc.ui.swing.components.renderers.MapEntryRenderer;
 import com.dmdirc.ui.swing.components.validating.ValidatingJTextField;
 import static com.dmdirc.ui.swing.UIUtilities.LARGE_BORDER;
 import static com.dmdirc.ui.swing.UIUtilities.SMALL_BORDER;
 
 import java.awt.CardLayout;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JSpinner;
 import javax.swing.JTextField;
 import javax.swing.JTree;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.SpringLayout;
 import javax.swing.WindowConstants;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.TreeSelectionEvent;
 import javax.swing.event.TreeSelectionListener;
 import javax.swing.text.Position;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.TreeCellRenderer;
 import javax.swing.tree.TreeNode;
 import javax.swing.tree.TreePath;
 import javax.swing.tree.TreeSelectionModel;
 
 import net.miginfocom.swing.MigLayout;
 
 /**
  * Allows the user to modify global client preferences.
  */
 public final class SwingPreferencesDialog extends StandardDialog implements
         ActionListener, TreeSelectionListener {
 
     /**
      * A version number for this class. It should be changed whenever the
      * class structure is changed (or anything else that would prevent
      * serialized objects being unserialized with the new class).
      */
     private static final long serialVersionUID = 8;
 
     /** A map of settings to the components used to represent them. */
     private final Map<PreferencesSetting, JComponent> components;
 
     /** Categories in the dialog. */
     private final Map<PreferencesCategory, JPanel> categories;
     
     /** Nodes in the treeview. */
     private final Map<TreeNode, String> nodes;
     
     /** Paths to categories. */
     private final Map<String, PreferencesCategory> paths;
 
     /** Custom panels, not to be laid out automatically. */
     private final List<JPanel> panels;
 
     /** Preferences tab list, used to switch option types. */
     private JTree tabList;
 
     /** Main card layout. */
     private CardLayout cardLayout;
 
     /** Main panel. */
     private JPanel mainPanel;
 
     /** root node. */
     private DefaultMutableTreeNode rootNode;
     
     /** Previously selected category. */
     private PreferencesCategory selected = null;
     
     /** Preferences Manager. */
     private final PreferencesManager manager;
 
     /**
      * Creates a new instance of SwingPreferencesDialog.
      */
     public SwingPreferencesDialog() {
         super((MainFrame) Main.getUI().getMainWindow(), false);
         
         manager = new PreferencesManager();
 
         categories = new HashMap<PreferencesCategory, JPanel>();
         components = new HashMap<PreferencesSetting, JComponent>();
         nodes = new HashMap<TreeNode, String>();
         paths = new HashMap<String, PreferencesCategory>();
 
         panels = new ArrayList<JPanel>();
 
         initComponents();
 
         new TreeScroller(tabList);
         
         addCategories(manager.getCategories());
     }
 
     /**
      * Initialises GUI components.
      */
     private void initComponents() {
         final SpringLayout layout = new SpringLayout();
         final JButton button1 = new JButton();
         final JButton button2 = new JButton();
 
         cardLayout = new CardLayout();
         mainPanel = new JPanel(cardLayout);
 
         rootNode = new DefaultMutableTreeNode("root");
 
         tabList = new JTree(new DefaultTreeModel(rootNode));
         tabList.getSelectionModel().setSelectionMode(
                 TreeSelectionModel.SINGLE_TREE_SELECTION);
         tabList.putClientProperty("JTree.lineStyle", "Angled");
         tabList.setUI(new javax.swing.plaf.metal.MetalTreeUI());
         tabList.setRootVisible(false);
         tabList.setShowsRootHandles(false);
         tabList.setCellRenderer(new PreferencesTreeCellRenderer());
         tabList.addTreeSelectionListener(this);
 
         setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
         getContentPane().setLayout(new GridBagLayout());
         setTitle("Preferences");
         setResizable(false);
 
         mainPanel.setBorder(BorderFactory.createEmptyBorder(LARGE_BORDER,
                 LARGE_BORDER, SMALL_BORDER, LARGE_BORDER));
         tabList.setBorder(BorderFactory.createCompoundBorder(BorderFactory
                 .createEtchedBorder(), BorderFactory.createEmptyBorder(
                 SMALL_BORDER, SMALL_BORDER, SMALL_BORDER, SMALL_BORDER)));
 
         getContentPane().setLayout(layout);
 
         tabList.setPreferredSize(new Dimension(150, 550));
         tabList.setMinimumSize(new Dimension(150, 550));
         setMinimumSize(new Dimension(650, 600));
         setPreferredSize(new Dimension(650, 600));
         setMaximumSize(new Dimension(650, 600));
 
         orderButtons(button1, button2);
 
         getContentPane().add(tabList);
 
         getContentPane().add(mainPanel);
 
         getContentPane().add(Box.createHorizontalGlue());
 
         getContentPane().add(button1);
 
         getContentPane().add(button2);
 
         getOkButton().addActionListener(this);
         getCancelButton().addActionListener(this);
 
         // tab list
         layout.putConstraint(SpringLayout.WEST, tabList, LARGE_BORDER,
                 SpringLayout.WEST, getContentPane());
         layout.putConstraint(SpringLayout.NORTH, tabList, LARGE_BORDER,
                 SpringLayout.NORTH, getContentPane());
         // main panel
         layout.putConstraint(SpringLayout.WEST, mainPanel, SMALL_BORDER,
                 SpringLayout.EAST, tabList);
         layout.putConstraint(SpringLayout.NORTH, mainPanel, SMALL_BORDER,
                 SpringLayout.NORTH, getContentPane());
         // ok button
         layout.putConstraint(SpringLayout.EAST, getRightButton(), -LARGE_BORDER,
                 SpringLayout.EAST, getContentPane());
         layout.putConstraint(SpringLayout.NORTH, getRightButton(), SMALL_BORDER,
                 SpringLayout.SOUTH, mainPanel);
         // cancel button
         layout.putConstraint(SpringLayout.EAST, getLeftButton(), -LARGE_BORDER,
                 SpringLayout.WEST, getRightButton());
         layout.putConstraint(SpringLayout.NORTH, getLeftButton(), SMALL_BORDER,
                 SpringLayout.SOUTH, mainPanel);
         // panel size
         layout.putConstraint(SpringLayout.EAST, getContentPane(), LARGE_BORDER,
                 SpringLayout.EAST, mainPanel);
         layout.putConstraint(SpringLayout.SOUTH, getContentPane(), LARGE_BORDER,
                 SpringLayout.SOUTH, getRightButton());
     }
 
     /**
      * Initialises and adds a component to a panel.
      *
      * @param category The category the setting is being added to
      * @param setting The setting to be used
      */
     private void addComponent(final PreferencesCategory category,
             final PreferencesSetting setting) {
 
         final JLabel label = getLabel(setting);
         final JComponent option = getComponent(setting);
 
         categories.get(category).add(label);
 
         label.setLabelFor(option);
         categories.get(category).add(option, "wrap");
     }
 
     /**
      * Retrieves the title label for the specified setting.
      *
      * @param setting The setting whose label is being requested
      * @return A JLabel with the appropriate text and tooltip
      */
     private JLabel getLabel(final PreferencesSetting setting) {
         final JLabel label = new JLabel(setting.getTitle() + ": ", JLabel.TRAILING);
 
         if (setting.getHelptext().isEmpty()) {
             label.setToolTipText("No help available.");
         } else {
             label.setToolTipText(setting.getHelptext());
         }
 
         return label;
     }
 
     /**
      * Retrieves the component for the specified setting.
      * 
      * @param setting The setting whose component is being requested
      * @return An appropriate JComponent descendant
      */
     private final JComponent getComponent(final PreferencesSetting setting) {
         JComponent option;
 
         switch (setting.getType()) {
             case TEXT:
                 option = new ValidatingJTextField(setting.getValidator());
                 ((ValidatingJTextField) option).setText(setting.getValue());
                 ((ValidatingJTextField) option).addKeyListener(new KeyAdapter() {
                     @Override
                     public void keyTyped(final KeyEvent e) {
                         setting.setValue(((JTextField) e.getSource()).getText());
                     }
                 });
                 break;
             case BOOLEAN:
                 option = new JCheckBox();
                 ((JCheckBox) option).setSelected(Boolean.parseBoolean(setting.getValue()));
                 
                 ((JCheckBox) option).addChangeListener(new ChangeListener(){
                     public void stateChanged(final ChangeEvent e) {
                         setting.setValue(String.valueOf(((JCheckBox) e.getSource()).isSelected()));
                     }
                 });
                 
                 break;
             case MULTICHOICE:
                 option = new JComboBox(setting.getComboOptions().entrySet().toArray());
                 ((JComboBox) option).setRenderer(new MapEntryRenderer());
                 ((JComboBox) option).setEditable(false);
                 
                 for (Map.Entry<String, String> entry : setting.getComboOptions().entrySet()) {
                     if (entry.getKey().equals(setting.getValue())) {
                         ((JComboBox) option).setSelectedItem(entry);
                         break;
                     }
                 }
                 
                 ((JComboBox) option).addActionListener(new ActionListener() {
                     public void actionPerformed(ActionEvent e) {
                         setting.setValue((String) ((Map.Entry)
                                 ((JComboBox) e.getSource()).getSelectedItem()).getKey());
                     }
                 });
                 
                 break;
             case INTEGER:
             case DURATION:
                 try {
                     if (setting.getValidator() instanceof NumericalValidator) {
                         option = new JSpinner(
                                 new SpinnerNumberModel(Integer.parseInt(setting.getValue()),
                                 ((NumericalValidator) setting.getValidator()).getMin(),
                                 ((NumericalValidator) setting.getValidator()).getMax(),
                                 1));
                     } else {
                         option = new JSpinner(new SpinnerNumberModel());
 
                         ((JSpinner) option).setValue(Integer.parseInt(setting.getValue()));
                     }
                 } catch (NumberFormatException ex) {
                         option = new JSpinner(new SpinnerNumberModel());
                 }
                 
                 ((JSpinner) option).addChangeListener(new ChangeListener() {
                     public void stateChanged(ChangeEvent e) {
                         setting.setValue(((JSpinner) e.getSource()).getValue().toString());
                     }
                 });
                 
                 break;
             case COLOUR:
                 option = new ColourChooser(setting.getValue(), true, true);
                 
                 ((ColourChooser) option).addActionListener(new ActionListener() {
                     public void actionPerformed(ActionEvent e) {
                         setting.setValue(((ColourChooser) e.getSource()).getColour());
                     }
                 });
                 
                 break;
             case OPTIONALCOLOUR:
                 final boolean state = setting.getValue() != null && 
                         !setting.getValue().startsWith("false:");
                 final String colour = setting.getValue() == null ? "0" :
                         setting.getValue().substring(1 + setting.getValue().indexOf(':'));
                 
                 option = new OptionalColourChooser(colour, state, true, true);
                 
                 ((OptionalColourChooser) option).addActionListener(new ActionListener() {
                     public void actionPerformed(ActionEvent e) {
                         setting.setValue(
                                 String.valueOf(((OptionalColourChooser) e.getSource()).isEnabled())
                                 + ":" + ((OptionalColourChooser) e.getSource()).getColour());
                     }
                 });
                 
                 break;
             default:
                 throw new IllegalArgumentException(setting.getType() + " is not a valid option");
         }
         
         components.put(setting, option);
         option.setPreferredSize(new Dimension(Short.MAX_VALUE, option.getFont()
         .getSize()));
 
         return option;
     }
     
     /**
      * Adds a new inline category.
      * 
      * @param category The category to be added
      * @param parent The panel to add the category to
      */
     private void addInlineCategory(final PreferencesCategory category,
             final JPanel parent) {
         final JPanel panel = new JPanel(new MigLayout("fillx, gap " + LARGE_BORDER));
         panel.setBorder(BorderFactory.createTitledBorder(category.getTitle()));
         
         categories.put(category, panel);
         
         parent.add(panel, "span, growx, wrap");
 
         initCategory(category, panel, null, "");
     }
 
     /**
      * Adds the specified category to the preferences dialog.
      *
      * @param category The category to be added
      * @param parentNode the parent node of the category
      * @param namePrefix Category name prefix
      */
     private void addCategory(final PreferencesCategory category,
             final DefaultMutableTreeNode parentNode, final String namePrefix) {
         final JPanel panel = new JPanel(new MigLayout("fillx, gap " + LARGE_BORDER));
         final String path = namePrefix + "/" + category.getTitle();
         DefaultMutableTreeNode newNode;
 
         newNode = new DefaultMutableTreeNode(category.getTitle());
 
         categories.put(category, panel);
         nodes.put(newNode, path);
         paths.put(path, category);
         
         mainPanel.add(panel, path);
         ((DefaultTreeModel) tabList.getModel()).insertNodeInto(newNode,
                 parentNode, parentNode.getChildCount());
         tabList.scrollPathToVisible(new TreePath(newNode.getPath()));
         
         initCategory(category, panel, newNode, path);
     }
     
     private void initCategory(final PreferencesCategory category, final JPanel panel,
             final DefaultMutableTreeNode newNode, final String path) {
 
         if (!category.getDescription().isEmpty()) {
             final TextLabel infoLabel = new TextLabel(category.getDescription());
             panel.add(infoLabel, "span, growx, wrap");
         }
 
         for (PreferencesCategory child : category.getSubcats()) {
             if (child.isInline() && category.getInlineBefore()) {
                 addInlineCategory(child, panel);
             } else if (!child.isInline()) {
                 addCategory(child, newNode, path);
             }
         }
 
         if (category.hasObject()) {
             if (!(category.getObject() instanceof JPanel)) {
                 throw new IllegalArgumentException("Custom preferences objects" +
                         " for this UI must extend JPanel.");
             }
 
             panels.add((JPanel) category.getObject());
            categories.get(category).add((JPanel) category.getObject(), "growx");
 
             return;
         }
 
         for (PreferencesSetting setting : category.getSettings()) {
             addComponent(category, setting);
         }
         
         if (!category.getInlineBefore()) {
             for (PreferencesCategory child : category.getSubcats()) {
                 if (child.isInline()) {
                     addInlineCategory(child, panel);
                 }
             }
         }
     }
 
     /**
      * Adds the specified categories to the preferences dialog.
      * 
      * @param categories The categories to be added
      */
     public void addCategories(final Collection<? extends PreferencesCategory> categories) {
         for (PreferencesCategory category : categories) {
             addCategory(category, rootNode, "");
         }
     }
 
     /**
      * Handles the actions for the dialog.
      *
      * @param actionEvent Action event
      */
     public void actionPerformed(final ActionEvent actionEvent) {
         if (selected != null) {
             selected.fireCategoryDeselected();
         }
         
         if (getOkButton().equals(actionEvent.getSource())) {
             if (tabList.getSelectionPath() != null) {
                 final String node = tabList.getSelectionPath().toString();
                 IdentityManager.getConfigIdentity().setOption("dialogstate",
                         "preferences", node.substring(7, node.length() - 1).
                         replaceAll(", ", "->"));
             }
             saveOptions();
             dispose();
         } else if (getCancelButton().equals(actionEvent.getSource())) {
             dispose();
         }
     }
 
     /**
      * Called when the selection in the tree changes.
      *
      * @param selectionEvent list selection event
      */
     public void valueChanged(final TreeSelectionEvent selectionEvent) {
         final String path = nodes.get(((JTree) selectionEvent.getSource())
                 .getSelectionPath().getLastPathComponent());
         
         cardLayout.show(mainPanel, path);
         
         if (selected != null) {
             selected.fireCategoryDeselected();
         }
         
         selected = paths.get(path);
         selected.fireCategorySelected();
     }
 
     /** {@inheritDoc} */
     public void saveOptions() {
         manager.fireSaveListeners();
         
         boolean restart = false;
         
         for (PreferencesSetting setting : components.keySet()) {
             if (setting.save() && setting.isRestartNeeded()) {
                 restart = true;
             }
         }
         
         if (restart) {
             JOptionPane.showMessageDialog((MainFrame) Main.getUI().
                     getMainWindow(), "One or more of the changes you made "
                     + "won't take effect until you restart the client.",
                     "Restart needed", JOptionPane.INFORMATION_MESSAGE);            
         }
     }
 
     /** {@inheritDoc} */
     public void display() {
         final String[] tabName = IdentityManager.getGlobalConfig().
                 getOption("dialogstate", "preferences", "").split("->");
         TreePath path = new TreePath(tabList.getModel().getRoot());
 
         for (String string : tabName) {
             final TreePath treePath = tabList.getNextMatch(string, 0,
                     Position.Bias.Forward);
             if (treePath != null) {
                 final TreeNode node = (TreeNode) treePath.getLastPathComponent();
                 if (node != null) {
                     path = path.pathByAddingChild(node);
                 }
             }
         }
 
         if (path == null || path.getPathCount() <= 1) {
             tabList.setSelectionPath(tabList.getPathForRow(0));
         } else {
             tabList.setSelectionPath(path);
         }
         pack();
         setLocationRelativeTo((MainFrame) Main.getUI().getMainWindow());
         setVisible(true);
     }
 
     /**
      * Preferences tree cell renderer.
      */
     private class PreferencesTreeCellRenderer extends JLabel implements TreeCellRenderer {
 
         /**
          * A version number for this class. It should be changed whenever the class
          * structure is changed (or anything else that would prevent serialized
          * objects being unserialized with the new class).
          */
         private static final long serialVersionUID = 1;
 
         /**
          * Creates a new instance of PreferencesTreeCellRenderer.
          */
         public PreferencesTreeCellRenderer() {
             super();
         }
         /**
          * Configures the renderer based on the passed parameters.
          * @param tree JTree for this renderer.
          * @param value node to be renderered.
          * @param sel whether the node is selected.
          * @param expanded whether the node is expanded.
          * @param leaf whether the node is a leaf.
          * @param row the node's row.
          * @param focused whether the node has focus.
          * @return RendererComponent for this node.
          */
         public final Component getTreeCellRendererComponent(final JTree tree,
                 final Object value, final boolean sel, final boolean expanded,
                 final boolean leaf, final int row, final boolean focused) {
 
             setText(value.toString());
             setBackground(tree.getBackground());
             setForeground(tree.getForeground());
             setOpaque(true);
             setToolTipText(null);
             setBorder(BorderFactory.createEmptyBorder(SMALL_BORDER,
                     SMALL_BORDER, SMALL_BORDER, SMALL_BORDER));
 
             setPreferredSize(new Dimension((int) tabList.getPreferredSize().getWidth() - 20,
                     getFont().getSize() + SMALL_BORDER));
 
             if (sel) {
                 setFont(getFont().deriveFont(Font.BOLD));
             } else {
                 setFont(getFont().deriveFont(Font.PLAIN));
             }
 
             return this;
         }
     }
     
 }
