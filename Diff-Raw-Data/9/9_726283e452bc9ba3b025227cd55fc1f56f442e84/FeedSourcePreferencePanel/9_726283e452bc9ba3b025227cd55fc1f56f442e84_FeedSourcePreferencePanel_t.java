 /*
  TSAFE Prototype: A decision support tool for air traffic controllers
  Copyright (C) 2003  Gregory D. Dennis
 
  This program is free software; you can redistribute it and/or
  modify it under the terms of the GNU General Public License
  as published by the Free Software Foundation; either version 2
  of the License, or (at your option) any later version.
 
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 
 package tsafe.server.server_gui.preferences;
 
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.GridLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.Reader;
 import java.util.Vector;
 
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSeparator;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.ListSelectionModel;
 
 import tsafe.common_datastructures.TSAFEProperties;
 import tsafe.server.server_gui.feedsourcechooser.FeedSourceChooserDialog;
 import tsafe.server.server_gui.utils.LayoutUtils;
 import tsafe.server.server_gui.utils.table.DefaultSortTableModel;
 import tsafe.server.server_gui.utils.table.JSortTable;
 import tsafe.server.server_gui.utils.table.TableUtils;
 
 /**
  * Allows a user to specify feed source preferences.
  */
 class FeedSourcePreferencePanel extends PreferencePanel {
 
 
     //
     // CONSTANTS
     //
 
     /**
      * The feed source table's column names.
      */
     private final static Vector SOURCES_TABLE_COLUMNS;
     static {
         SOURCES_TABLE_COLUMNS = new Vector();
         SOURCES_TABLE_COLUMNS.add("Click here to sort");
     }
 
     /**
      * The index of the "Sources" table column.
      */
     private final static int COLUMN_SOURCES = 0;
 
     /**
      * The width of the "Sources" table column.
      */
     private final static int COLUMN_WIDTH_SOURCES = LayoutUtils.TABLE_SINGLE_COLUMN_WIDTH;
 
 
 
     //
     // MEMBER VARIABLES
     //
 
     /**
      * The dialog that owns this panel.
      */
     private JDialog parentDialog;
 
     /**
      * The text field displaying the default feed directory.
      */
     private JTextField feedDirTextField;
 
     /**
      * The table containing the list of feed sources.
      */
     private JSortTable feedSourcesTable;
 
     /**
      * The feed source table's model.
      */
     private DefaultSortTableModel feedSourcesTableModel;
 
 
 
     //
     // LAYOUT METHODS
     //
 
     //-------------------------------------------
     /**
      * Constructs a new panel to specify feed source preferences.
      *
      * @param parentDialog      the dialog that owns this panel
      * @param commonButtonPanel the common buttons between all preference panels; assumes
      *                          this argument is not null
      */
     FeedSourcePreferencePanel(JDialog parentDialog, JPanel commonButtonPanel) {
         super();
         GridBagLayout gridbag = new GridBagLayout();
         setLayout(gridbag);
         setBorder(BorderFactory.createEmptyBorder(LayoutUtils.PANEL_BORDERSIZE,
                                                   LayoutUtils.PANEL_BORDERSIZE,
                                                   LayoutUtils.PANEL_BORDERSIZE,
                                                   LayoutUtils.PANEL_BORDERSIZE));       
         this.parentDialog = parentDialog;
 
 
         // Init this panel's contents.  Note that since we are laying out
         // components using GridBagLayout, changes to the layout must be
         // propogated to each of the methods below.  Use "gridy" to 
         // represent the first available vertical gridpoint.
         int gridy = 0;
         MyEventListener mel = new MyEventListener();
         gridy = initFeedSourceTable(gridbag, mel, gridy);
         gridy = initFeedDirOption(gridbag, mel, gridy);
         gridy = addCommonButtonPanel(gridbag, gridy, commonButtonPanel);
 
 
         // Load the current system preferences.
         loadSystemPreferences();
     }
 
 
     //-------------------------------------------
     /**
      * Set up the feed source table and controls.
      *
      * @param gridbag  the gridbag layout object
      * @param mel      the general event listener
      * @param gridy    the first available vertical position in the layout
      */
     private int initFeedSourceTable(GridBagLayout gridbag, MyEventListener mel, int gridy) {
         
         // Set the label.
         JLabel label = new JLabel("Default Feed Sources:");
         label.setHorizontalAlignment(JLabel.LEFT);
         label.setVerticalAlignment(JLabel.BOTTOM);
         GridBagConstraints c = LayoutUtils.makeGridBagConstraints(0, gridy,
                                                                   2, 1,
                                                                   LayoutUtils.LABEL_WEIGHTX, 
                                                                   LayoutUtils.PANEL_B_WEIGHTY_TOPMOST,
                                                                   GridBagConstraints.HORIZONTAL, 
                                                                   GridBagConstraints.SOUTHWEST,
                                                                   new Insets(LayoutUtils.PANEL_B_SPACE_ABOVE_TOPMOST,
                                                                              0,0,0));
         gridbag.setConstraints(label, c);
         add(label);
         gridy++;
 
 
         // Set the instructions.
         JTextArea instructions = new JTextArea("This list of default feed sources will appear every " +
                                                "time you are asked to choose a feed source.", 2, 40);
         instructions.setOpaque(false);
         instructions.setEditable(false);
         instructions.setLineWrap(true);
         instructions.setWrapStyleWord(true);
         c = LayoutUtils.makeGridBagConstraints(0, gridy,
                                                2, 1,
                                                LayoutUtils.TEXTAREA_WEIGHTX, 
                                                LayoutUtils.PANEL_IB_WEIGHTY_BETWEEN,
                                                GridBagConstraints.BOTH, 
                                                GridBagConstraints.WEST,
                                                new Insets(LayoutUtils.PANEL_IB_SPACE_BETWEEN,
                                                           LayoutUtils.PANEL_INDENT_1, 0, 0));
         gridbag.setConstraints(instructions, c);
         add(instructions);
         gridy++;
 
 
         // Create the table.
         feedSourcesTableModel = new DefaultSortTableModel(new Vector(), SOURCES_TABLE_COLUMNS);
         feedSourcesTableModel.setEditable(false);
         feedSourcesTable = new JSortTable(feedSourcesTableModel);
         feedSourcesTable.setToolTipText("Double click on a feed source for an expanded view");
 
         TableUtils.addExpandedCellViewer(feedSourcesTable);
         feedSourcesTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
         feedSourcesTable.setShowGrid(false);
         feedSourcesTable.getColumnModel().getColumn(COLUMN_SOURCES).setPreferredWidth(COLUMN_WIDTH_SOURCES);
         feedSourcesTable.setPreferredScrollableViewportSize(new Dimension(COLUMN_WIDTH_SOURCES,
                                                                           5 * feedSourcesTable.getRowHeight()));
 
         JScrollPane tableScrollPane = new JScrollPane(feedSourcesTable);
         c = LayoutUtils.makeGridBagConstraints(0, gridy,
                                                2, 1,
                                                LayoutUtils.TABLE_WEIGHTX, 
                                                LayoutUtils.TABLE_WEIGHTY,
                                                GridBagConstraints.BOTH, 
                                                GridBagConstraints.CENTER,
                                                new Insets(LayoutUtils.PANEL_IB_SPACE_BETWEEN, 
                                                           LayoutUtils.PANEL_INDENT_1,
                                                           0,0));
         gridbag.setConstraints(tableScrollPane, c);
         add(tableScrollPane);
         gridy++;
         
 
         // Create the button panel.
         JPanel buttonPanel = new JPanel(new GridLayout(1, 2,
                                                        LayoutUtils.BUTTONP_SPACE_HORIZONTAL, 
                                                        LayoutUtils.BUTTONP_SPACE_VERTICAL));
 
         JButton button = new JButton("Add Source");
 		button.setToolTipText("Add a feed source to the list");
 		button.setActionCommand("Add Source");
   		button.addActionListener(mel);
         buttonPanel.add(button);
 
         button = new JButton("Delete Sources");
 		button.setToolTipText("Delete the selected sources from the list");
 		button.setActionCommand("Delete Sources");
   		button.addActionListener(mel);
         buttonPanel.add(button);
 
         c = LayoutUtils.makeGridBagConstraints(0, gridy,
                                                2, 1,
                                                LayoutUtils.BUTTONP_WEIGHTX, 
                                                LayoutUtils.PANEL_B_WEIGHTY_BELOW, 
                                                GridBagConstraints.NONE, 
                                                GridBagConstraints.NORTH,
                                                new Insets(LayoutUtils.BUTTONP_BORDERSIZE +
                                                           LayoutUtils.PANEL_IB_SPACE_BETWEEN_RELATED,
                                                           LayoutUtils.BUTTONP_BORDERSIZE + 
                                                           LayoutUtils.PANEL_INDENT_1,
                                                           LayoutUtils.BUTTONP_BORDERSIZE,
                                                           LayoutUtils.BUTTONP_BORDERSIZE));
         gridbag.setConstraints(buttonPanel, c);
         add(buttonPanel);
         gridy++;
 
 
         return gridy;
     }
 
 
     //-------------------------------------------
     /**
      * Set up the feed directory chooser.
      *
      * @param gridbag  the gridbag layout object
      * @param mel      the general event listener
      * @param gridy    the first available vertical position in the layout
      */
     private int initFeedDirOption(GridBagLayout gridbag, MyEventListener mel, int gridy) {
 
         // Set the label.
         JLabel label = new JLabel("Main Feed Directory:");
         label.setHorizontalAlignment(JLabel.LEFT);
         label.setVerticalAlignment(JLabel.BOTTOM);
         GridBagConstraints c = LayoutUtils.makeGridBagConstraints(0, gridy,
                                                                   2, 1,
                                                                   LayoutUtils.LABEL_WEIGHTX, 
                                                                   LayoutUtils.PANEL_B_WEIGHTY_ABOVE,
                                                                   GridBagConstraints.HORIZONTAL, 
                                                                   GridBagConstraints.SOUTHWEST,
                                                                   new Insets(LayoutUtils.PANEL_B_SPACE_BETWEEN,
                                                                              0,0,0));
         gridbag.setConstraints(label, c);
         add(label);
         gridy++;
         
         
          // Set the instructions.
        JTextArea instructions = new JTextArea("This is the main directory where you store your FIG files. " +
                                                "Anytime you are asked to select a FIG file, you will start " +
                                                "out in this directory.", 3, 40);
         instructions.setOpaque(false);
         instructions.setEditable(false);
         instructions.setLineWrap(true);
         instructions.setWrapStyleWord(true);
         c = LayoutUtils.makeGridBagConstraints(0, gridy,
                                                2, 1,
                                                LayoutUtils.TEXTAREA_WEIGHTX, 
                                                LayoutUtils.PANEL_IB_WEIGHTY_BETWEEN,
                                                GridBagConstraints.BOTH, 
                                                GridBagConstraints.WEST,
                                                new Insets(LayoutUtils.PANEL_IB_SPACE_BETWEEN,
                                                           LayoutUtils.PANEL_INDENT_1, 0, 0));
         gridbag.setConstraints(instructions, c);
         add(instructions);
         gridy++;
 
 
        // Set the directory chooser.
         feedDirTextField = new JTextField(15);
         feedDirTextField.setActionCommand("feedDir");
         feedDirTextField.addActionListener(mel);
         c = LayoutUtils.makeGridBagConstraints(0, gridy,
                                                1, 1,
                                                LayoutUtils.TEXTFIELD_FILE_WEIGHTX,
                                                LayoutUtils.PANEL_B_WEIGHTY_BELOW,
                                                GridBagConstraints.HORIZONTAL, 
                                                GridBagConstraints.NORTHWEST,
                                                new Insets(LayoutUtils.PANEL_IB_SPACE_BETWEEN, 
                                                           LayoutUtils.PANEL_INDENT_1,
                                                           0, 0));
         gridbag.setConstraints(feedDirTextField, c);
         add(feedDirTextField);
         
         JButton button = new JButton("Change");
 		button.setToolTipText("Change the save directory");
 		button.setActionCommand("Change");
   		button.addActionListener(mel);
         c = LayoutUtils.makeGridBagConstraints(1, gridy,
                                                1, 1,
                                                LayoutUtils.BUTTONP_WEIGHTX,
                                                LayoutUtils.PANEL_B_WEIGHTY_BELOW,
                                                GridBagConstraints.NONE, 
                                                GridBagConstraints.NORTHEAST,
                                                new Insets(LayoutUtils.PANEL_IB_SPACE_BETWEEN, 
                                                           LayoutUtils.BUTTONP_BORDERSIZE,
                                                           0, 
                                                           LayoutUtils.BUTTONP_BORDERSIZE));
         gridbag.setConstraints(button, c);
         add(button);
         gridy++;
 
 
         return gridy;
     }
 
 
     //-------------------------------------------
     /**
      * Add the common buttons to this preference panel.
      *
      * @param gridbag           the gridbag layout object
      * @param gridy             the first available vertical position in the layout
      * @param commonButtonPanel the common buttons between all preference panels
      * @return  the first available vertical position in the layout after this method
      *          has finished laying out all of its components
      */
     private int addCommonButtonPanel(GridBagLayout gridbag, int gridy, JPanel commonButtonPanel) {
         
         // Create a visual separator.
         JSeparator separator = new JSeparator();
         GridBagConstraints c = 
             LayoutUtils.makeGridBagConstraints(0, gridy,
                                                2, 1,
                                                LayoutUtils.PANEL_SP_WEIGHTX, 
                                                LayoutUtils.PANEL_SP_WEIGHTY,
                                                GridBagConstraints.HORIZONTAL, 
                                                GridBagConstraints.SOUTH,
                                                new Insets(LayoutUtils.PANEL_SP_SPACE_ABOVE_FINAL_BUTTONP,
                                                           0,0,0));
         gridbag.setConstraints(separator, c);
         add(separator);
         gridy++;
         
 
         c = LayoutUtils.makeGridBagConstraints(0, gridy,
                                                2, 1,
                                                LayoutUtils.BUTTONP_WEIGHTX, 
                                                LayoutUtils.PANEL_B_WEIGHTY_FINAL_BUTTONP,
                                                GridBagConstraints.NONE, 
                                                GridBagConstraints.NORTH,
                                                new Insets(LayoutUtils.BUTTONP_BORDERSIZE +
                                                           LayoutUtils.PANEL_SP_SPACE_BELOW_FINAL_BUTTONP,
                                                           LayoutUtils.BUTTONP_BORDERSIZE,
                                                           LayoutUtils.BUTTONP_BORDERSIZE +
                                                           LayoutUtils.PANEL_B_SPACE_BELOW_BOTTOMMOST,
                                                           LayoutUtils.BUTTONP_BORDERSIZE));
         gridbag.setConstraints(commonButtonPanel, c);
         add(commonButtonPanel);        
         gridy++;
 
            
         return gridy;
     }
 
 
 
 	//
  	// FIELD MAINTENANCE METHODS
 	//
 
     //-------------------------------------------
     /**
      * Set the feed directory.  If the specified directory is
      * null, the directory is set to the empty string.
      *
      * @param newDir  the new directory
      */
     private void setFeedDir(File newDir) {
         if (newDir == null) {
             newDir = new File("");
         }
 
         if (newDir.isFile()) {
             feedDirTextField.setText(newDir.getParent());
         }
         else {
             feedDirTextField.setText(newDir.getPath());
         }
     }
 
 
     //-------------------------------------------
     /**
      * Loads the current system preferences into this panel.
      */
     private void loadSystemPreferences() {
         setFeedDir(TSAFEProperties.getFeedDirectory());
         TableUtils.setDataVector(feedSourcesTableModel, TSAFEProperties.getFeedSources());
     }
 
 
 
 	//
  	// PREFERENCE_PANEL METHODS
 	//
 
     //-------------------------------------------
     void setSystemPreferences() {
         TSAFEProperties.setFeedDirectory(new File(feedDirTextField.getText()));
         TSAFEProperties.setFeedSources(feedSourcesTableModel.getDataVector());
     }
 
 
     //-------------------------------------------
     void savePreferencesAsDefault() {
 
         // Save a copy of the system preferences.
         File originalFeedDir = TSAFEProperties.getFeedDirectory();
         Vector originalFeedSources = TSAFEProperties.getFeedSources();
 
         // Temporarily set the system preferences to reflect the
         // current preference choices and save the system prefrences
         // as the defaults.
         setSystemPreferences();
         TSAFEProperties.saveFeedDirectoryAsDefault();
         TSAFEProperties.saveFeedSourcesAsDefault();
 
         // Now restore the original system preferences.
         TSAFEProperties.setFeedDirectory(originalFeedDir);
         TSAFEProperties.setFeedSources(originalFeedSources);
     }
 
 
     //-------------------------------------------
     void restoreDefaultPreferences() {
 
         // Save a copy of the system preferences.
         File originalFeedDir = TSAFEProperties.getFeedDirectory();
         Vector originalFeedSources = TSAFEProperties.getFeedSources();
 
         // Temporarily set the system preferences to their default
         // values and load them into this panel.
         TSAFEProperties.restoreDefaultFeedDirectory();
         TSAFEProperties.restoreDefaultFeedSources();        
         loadSystemPreferences();
 
         // Now restore the original system preferences.
         TSAFEProperties.setFeedDirectory(originalFeedDir);
         TSAFEProperties.setFeedSources(originalFeedSources);
     }
 
 
 
 
     //
     // INNER CLASSES
     //
 
     /**
      * Listens and responds to any events fired on behalf of the 
      * buttons or lists in this preference panel.
      */
     private class MyEventListener implements ActionListener {
         
         //
         // METHODS
         //
         
         //-------------------------------------------
         /**
          * Responds to any events thrown by this panel's buttons.
          *
          * @param event the ActionEvent
          */
         public void actionPerformed(ActionEvent event) {
             String command = event.getActionCommand();
 
 
             // Feed Source Buttons
 
             if (command.equals("Add Source")) {
                 FeedSourceChooserDialog addSourceDialog = new FeedSourceChooserDialog();
                 addSourceDialog.showDialog((JFrame) parentDialog.getOwner(), "Add a Feed Source");
                 Reader source = addSourceDialog.getSource();
 
                 if (source != null) {
                     Vector newSource = new Vector();
                     newSource.add(source);
                     feedSourcesTableModel.addRow(newSource);
                 }
             }
             
             else if (command.equals("Delete Sources")) {                
                 int[] selectedRows = feedSourcesTable.getSelectedRows();
                 
                 // Remove the row with the highest row index first so
                 // we do not run into "index shifting" errors.
                 for (int i = selectedRows.length - 1; i >= 0; i--) {
                     feedSourcesTableModel.removeRow(selectedRows[i]);
                 }
             }
 
 
             // Feed Dir Button & TextField
 
             // Pop up a file chooser dialog that allows the user to select a directory.
             else if (command.equals("Change")) {                
                 JFileChooser dirChooser = new JFileChooser(TSAFEProperties.getFeedDirectory());
                 dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                 dirChooser.setDialogTitle("Choose a Directory");
                 dirChooser.rescanCurrentDirectory();                
                 dirChooser.setAcceptAllFileFilterUsed(false);
                 int returnVal = dirChooser.showDialog((JFrame) parentDialog.getOwner(), "Choose");
                       
                 if (returnVal == JFileChooser.APPROVE_OPTION) {
                     File directory = dirChooser.getSelectedFile();
                     setFeedDir(directory);
                 }
             }
 
             else if (command.equals("feedDir")) {                
                 File directory = new File(feedDirTextField.getText());
                 setFeedDir(directory);
             }
 
 
             // Default
 
             else {   
                 System.err.println("Unrecognized command: " + command);
             }
         }
 
     } // inner class MyEventListener
 
 }
