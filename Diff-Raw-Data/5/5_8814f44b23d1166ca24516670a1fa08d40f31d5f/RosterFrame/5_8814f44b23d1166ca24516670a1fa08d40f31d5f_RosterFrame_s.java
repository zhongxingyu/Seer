 /*
  * RosterFrame.java
  * Copyright (C) 2001, 2002 Klaus Rennecke.
  */
 
 package net.sourceforge.fraglets.yaelp;
 
 import com.jclark.xml.sax.CommentDriver;
 import com.jclark.xsl.sax.Destination;
 import com.jclark.xsl.sax.FileDestination;
 import com.jclark.xsl.sax.OutputMethodHandlerImpl;
 import com.jclark.xsl.sax.XSLProcessor;
 import com.jclark.xsl.sax.XSLProcessorImpl;
 import java.util.Comparator;
 import org.xml.sax.ErrorHandler;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.xml.sax.SAXParseException;
 import java.util.HashMap;
 import javax.swing.JFileChooser;
 import java.util.Properties;
 import java.util.Iterator;
 import java.util.Map;
 
 /** This class implements a simple GUI to invoke the log recognizer and
  * display the results.
  *
  * <p>This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * <p>This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * <p>You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
  *
  * @author marion@users.sourceforge.net
 * @version $Revision: 1.12 $
  */
 public class RosterFrame extends javax.swing.JFrame {
     /** The file chooser map used to select files to parse and export.
      */
     protected HashMap chooserMap;
     /** The last chooser category used. */
     protected String lastChooser;
     /** The table model. */
     protected RosterTableModel model = new RosterTableModel();
     /** The recognizer used to parse the input files.
      */    
     protected Recognizer recognizer = new Recognizer();
     /** The current avatar filter, if any.
      */    
     protected AvatarFilter avatarFilter;
     /** The property editor. */
     protected PropertyEditor propertyEditor;
     
     protected String aboutText;
     
     protected StyleSelection styleSelection
         = new StyleSelection(getResource("roster.xsl"));
     
     /** Client property key for filter property. */
     public static final String FILTER_PROPERTY = "RosterFrame.filter";
     /** Client property key for style property. */
     public static final String STYLE_PROPERTY = "RosterFrame.style";
     
     
     public static class StyleSelection extends javax.swing.AbstractAction {
         private java.net.URL selectedStyle;
         public StyleSelection(java.net.URL initialSelection) {
             super("select style");
             this.selectedStyle = initialSelection;
         }
         public java.net.URL getSelectedStyle() {
             return selectedStyle;
         }
         public void setSelectedStyle(java.net.URL newStyle) {
             selectedStyle = newStyle;
         }
         public void actionPerformed(java.awt.event.ActionEvent ev) {
             Object source = ev.getSource();
             if (source instanceof javax.swing.JComponent) {
                 Object value = ((javax.swing.JComponent)source)
                     .getClientProperty(STYLE_PROPERTY);
                 if (value instanceof java.net.URL) {
                     selectedStyle = (java.net.URL)value;
                 }
             }
         }
     }
     
     /** Creates new form RosterFrame */
     public RosterFrame() {
         initComponents();
         // initialize sorting
         doNameOrder();
         rosterTable.getTableHeader().addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent ev) {
                 if (isPopupTrigger(ev)) {
                     contextMenu.show((java.awt.Component)ev.getSource(),
                                      ev.getX(), ev.getY());
                 } else {
                     javax.swing.table.JTableHeader header = rosterTable.getTableHeader();
                     int index = header.columnAtPoint(ev.getPoint());
                     if (index >= 0) {
                         doOrder(rosterTable.getColumnModel().getColumn(index)
                                 .getIdentifier().toString());
                     }
                 }
             }
         });
         rosterTable.setSelectionMode
             (javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
         rosterTable.setDefaultRenderer(Object.class, new AvatarCellRenderer());
 
         // attach filters
         mainFilterItem.putClientProperty(FILTER_PROPERTY,
             new AvatarFilter.Main());
         level1FilterItem.putClientProperty(FILTER_PROPERTY,
             new AvatarFilter.Level(1));
         defaultFilterItem.putClientProperty(FILTER_PROPERTY,
             new AvatarFilter.Guild("Mad Wanderer"));
         alliedFilterItem.putClientProperty(FILTER_PROPERTY,
             new AvatarFilter.Guild("Sovereign Storm"));
         friendFilterItem.putClientProperty(FILTER_PROPERTY,
             new AvatarFilter.Guild("Veterans Legion"));
         
         // attach styles
         defaultStyleItem.putClientProperty(STYLE_PROPERTY,
             getResource("roster.xsl"));
         mwStyleItem.putClientProperty(STYLE_PROPERTY,
             getResource("roster_mw.xsl"));
         ssStyleItem.putClientProperty(STYLE_PROPERTY,
             getResource("roster_ss.xsl"));
         vlStyleItem.putClientProperty(STYLE_PROPERTY,
             getResource("roster_vl.xsl"));
         
         // HACK: Since the Netbeans 3.2 IDE drops all button
         // group assignments on CVS checkin, we do it here.
         // Likewise, we can't set the button name after the action
         // so the text would get mangled.
         styleButtonGroup.add(defaultStyleItem);
         defaultStyleItem.addActionListener(styleSelection);
         styleButtonGroup.add(mwStyleItem);
         mwStyleItem.addActionListener(styleSelection);
         styleButtonGroup.add(ssStyleItem);
         ssStyleItem.addActionListener(styleSelection);
         styleButtonGroup.add(vlStyleItem);
         vlStyleItem.addActionListener(styleSelection);
         
         // set editors
         Avatar.CHANGE.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
             public void propertyChange(java.beans.PropertyChangeEvent ev) {
                 if (ev.getPropertyName().equals(Avatar.class.getName())) {
                     updateEditor(ev.getNewValue());
                 }
             }
         });
         initEditors();
     }
 
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     private void initComponents() {//GEN-BEGIN:initComponents
         styleButtonGroup = new javax.swing.ButtonGroup();
         contextMenu = new javax.swing.JPopupMenu();
         propertyMenuItem = new javax.swing.JMenuItem();
         jSeparator3 = new javax.swing.JSeparator();
         newEntryItem = new javax.swing.JMenuItem();
         hideMenuItem = new javax.swing.JMenuItem();
         jSeparator1 = new javax.swing.JSeparator();
         dingMenuItem = new javax.swing.JMenuItem();
         dohMenuItem = new javax.swing.JMenuItem();
         tableScroll = new javax.swing.JScrollPane();
         rosterTable = new javax.swing.JTable();
         status = new javax.swing.JTextField();
         menuBar = new javax.swing.JMenuBar();
         fileMenu = new javax.swing.JMenu();
         parseFileItem = new javax.swing.JMenuItem();
         separator1 = new javax.swing.JSeparator();
         exportTableItem = new javax.swing.JMenuItem();
         exportXMLItem = new javax.swing.JMenuItem();
         exportHTMLItem = new javax.swing.JMenuItem();
         separator2 = new javax.swing.JSeparator();
         quitItem = new javax.swing.JMenuItem();
         filterMenu = new javax.swing.JMenu();
         noFilterItem = new javax.swing.JMenuItem();
         jSeparator2 = new javax.swing.JSeparator();
         mainFilterItem = new javax.swing.JCheckBoxMenuItem();
         level1FilterItem = new javax.swing.JCheckBoxMenuItem();
         separator3 = new javax.swing.JSeparator();
         defaultFilterItem = new javax.swing.JCheckBoxMenuItem();
         alliedFilterItem = new javax.swing.JCheckBoxMenuItem();
         friendFilterItem = new javax.swing.JCheckBoxMenuItem();
         separator4 = new javax.swing.JSeparator();
         newFilterMenu = new javax.swing.JMenu();
         newGuildFilterItem = new javax.swing.JMenuItem();
         levelFilterItem = new javax.swing.JMenuItem();
         classFilterItem = new javax.swing.JMenuItem();
         cultureFilterItem = new javax.swing.JMenuItem();
         afterFilterItem = new javax.swing.JMenuItem();
         styleMenu = new javax.swing.JMenu();
         defaultStyleItem = new javax.swing.JRadioButtonMenuItem();
         separator5 = new javax.swing.JSeparator();
         mwStyleItem = new javax.swing.JRadioButtonMenuItem();
         ssStyleItem = new javax.swing.JRadioButtonMenuItem();
         vlStyleItem = new javax.swing.JRadioButtonMenuItem();
         separator6 = new javax.swing.JSeparator();
         newStyleMenu = new javax.swing.JMenu();
         styleWizardItem = new javax.swing.JMenuItem();
         styleFileItem = new javax.swing.JMenuItem();
         optionsMenu = new javax.swing.JMenu();
         displayHTMLItem = new javax.swing.JCheckBoxMenuItem();
         helpMenu = new javax.swing.JMenu();
         aboutItem = new javax.swing.JMenuItem();
         licenseItem = new javax.swing.JMenuItem();
         xpLicenseItem = new javax.swing.JMenuItem();
         xtLicenseItem = new javax.swing.JMenuItem();
 
         contextMenu.setLabel("Edit");
         propertyMenuItem.setText("Properties ...");
         propertyMenuItem.setToolTipText("Show extra avatar properties.");
         propertyMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 propertyMenuItemActionPerformed(evt);
             }
         });
 
         contextMenu.add(propertyMenuItem);
         contextMenu.add(jSeparator3);
         newEntryItem.setText("new entry");
         newEntryItem.setToolTipText("Create a new entry.");
         newEntryItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 newEntryItemActionPerformed(evt);
             }
         });
 
         contextMenu.add(newEntryItem);
         hideMenuItem.setText("hide selected");
         hideMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 hideMenuItemActionPerformed(evt);
             }
         });
 
         contextMenu.add(hideMenuItem);
         contextMenu.add(jSeparator1);
         dingMenuItem.setText("DING!");
         dingMenuItem.setToolTipText("Increase level by one.");
         dingMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 dingMenuItemActionPerformed(evt);
             }
         });
 
         contextMenu.add(dingMenuItem);
         dohMenuItem.setText("DOH :(");
         dohMenuItem.setToolTipText("Decrease level by one.");
         dohMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 dohMenuItemActionPerformed(evt);
             }
         });
 
         contextMenu.add(dohMenuItem);
 
         setTitle("YAELP log file parser");
         setIconImage(getToolkit().getImage(getClass().getResource("logo.gif")));
         addWindowListener(new java.awt.event.WindowAdapter() {
             public void windowClosing(java.awt.event.WindowEvent evt) {
                 exitForm(evt);
             }
         });
 
         rosterTable.setModel(model);
         rosterTable.setPreferredScrollableViewportSize(new java.awt.Dimension(650, 200));
         rosterTable.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 rosterTableMouseClicked(evt);
             }
         });
 
         tableScroll.setViewportView(rosterTable);
 
         getContentPane().add(tableScroll, java.awt.BorderLayout.CENTER);
 
         status.setBackground(java.awt.Color.lightGray);
         status.setEditable(false);
         status.setText("Parse some files to add entries.");
         status.setToolTipText("");
         getContentPane().add(status, java.awt.BorderLayout.SOUTH);
 
         fileMenu.setText(java.util.ResourceBundle.getBundle("net/sourceforge/fraglets/yaelp/YaelpResources").getString("fileMenu.text"));
         parseFileItem.setText(java.util.ResourceBundle.getBundle("net/sourceforge/fraglets/yaelp/YaelpResources").getString("parseFileItem.text"));
         parseFileItem.setToolTipText(java.util.ResourceBundle.getBundle("net/sourceforge/fraglets/yaelp/YaelpResources").getString("parseFileItem.tooltip"));
         parseFileItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 parseFileItemActionPerformed(evt);
             }
         });
 
         fileMenu.add(parseFileItem);
         fileMenu.add(separator1);
         exportTableItem.setText(java.util.ResourceBundle.getBundle("net/sourceforge/fraglets/yaelp/YaelpResources").getString("exportTableItem.text"));
         exportTableItem.setToolTipText(java.util.ResourceBundle.getBundle("net/sourceforge/fraglets/yaelp/YaelpResources").getString("exportTableItem.tooltip"));
         exportTableItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 exportTableItemActionPerformed(evt);
             }
         });
 
         fileMenu.add(exportTableItem);
         exportXMLItem.setText(java.util.ResourceBundle.getBundle("net/sourceforge/fraglets/yaelp/YaelpResources").getString("exportXMLItem.text"));
         exportXMLItem.setToolTipText(java.util.ResourceBundle.getBundle("net/sourceforge/fraglets/yaelp/YaelpResources").getString("exportXMLItem.tooltip"));
         exportXMLItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 exportXMLItemActionPerformed(evt);
             }
         });
 
         fileMenu.add(exportXMLItem);
         exportHTMLItem.setText(java.util.ResourceBundle.getBundle("net/sourceforge/fraglets/yaelp/YaelpResources").getString("exportHTMLItem.text"));
         exportHTMLItem.setToolTipText(java.util.ResourceBundle.getBundle("net/sourceforge/fraglets/yaelp/YaelpResources").getString("exportHTMLItem.tooltip"));
         exportHTMLItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 exportHTMLItemActionPerformed(evt);
             }
         });
 
         fileMenu.add(exportHTMLItem);
         fileMenu.add(separator2);
         quitItem.setText(java.util.ResourceBundle.getBundle("net/sourceforge/fraglets/yaelp/YaelpResources").getString("quitItem.text"));
         quitItem.setToolTipText(java.util.ResourceBundle.getBundle("net/sourceforge/fraglets/yaelp/YaelpResources").getString("quitItem.tooltip"));
         quitItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 quitItemActionPerformed(evt);
             }
         });
 
         fileMenu.add(quitItem);
         menuBar.add(fileMenu);
         filterMenu.setText(java.util.ResourceBundle.getBundle("net/sourceforge/fraglets/yaelp/YaelpResources").getString("filterMenu.text"));
         filterMenu.setToolTipText(java.util.ResourceBundle.getBundle("net/sourceforge/fraglets/yaelp/YaelpResources").getString("filterMenu.tooltip"));
         noFilterItem.setText("no filter");
         noFilterItem.setToolTipText("Disable all filters.");
         noFilterItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 noFilterItemActionPerformed(evt);
             }
         });
 
         filterMenu.add(noFilterItem);
         filterMenu.add(jSeparator2);
         mainFilterItem.setText(java.util.ResourceBundle.getBundle("net/sourceforge/fraglets/yaelp/YaelpResources").getString("mainFilterItem.text"));
         mainFilterItem.setToolTipText(java.util.ResourceBundle.getBundle("net/sourceforge/fraglets/yaelp/YaelpResources").getString("mainFilterItem.tooltip"));
         mainFilterItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 filterItemActionPerformed(evt);
             }
         });
 
         filterMenu.add(mainFilterItem);
         level1FilterItem.setText(java.util.ResourceBundle.getBundle("net/sourceforge/fraglets/yaelp/YaelpResources").getString("level1FilterItem.text"));
         level1FilterItem.setToolTipText(java.util.ResourceBundle.getBundle("net/sourceforge/fraglets/yaelp/YaelpResources").getString("level1FilterItem.tooltip"));
         level1FilterItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 filterItemActionPerformed(evt);
             }
         });
 
         filterMenu.add(level1FilterItem);
         filterMenu.add(separator3);
         defaultFilterItem.setText(java.util.ResourceBundle.getBundle("net/sourceforge/fraglets/yaelp/YaelpResources").getString("defaultFilterItem.text"));
         defaultFilterItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 filterItemActionPerformed(evt);
             }
         });
 
         filterMenu.add(defaultFilterItem);
         alliedFilterItem.setText(java.util.ResourceBundle.getBundle("net/sourceforge/fraglets/yaelp/YaelpResources").getString("alliedFilterItem.text"));
         alliedFilterItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 filterItemActionPerformed(evt);
             }
         });
 
         filterMenu.add(alliedFilterItem);
         friendFilterItem.setText(java.util.ResourceBundle.getBundle("net/sourceforge/fraglets/yaelp/YaelpResources").getString("friendFilterItem.text"));
         friendFilterItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 filterItemActionPerformed(evt);
             }
         });
 
         filterMenu.add(friendFilterItem);
         filterMenu.add(separator4);
         newFilterMenu.setText(java.util.ResourceBundle.getBundle("net/sourceforge/fraglets/yaelp/YaelpResources").getString("newFilterMenu.text"));
         newGuildFilterItem.setText(java.util.ResourceBundle.getBundle("net/sourceforge/fraglets/yaelp/YaelpResources").getString("newGuildFilterItem.text"));
         newGuildFilterItem.setToolTipText(java.util.ResourceBundle.getBundle("net/sourceforge/fraglets/yaelp/YaelpResources").getString("newGuildFilterItem.tooltip"));
         newGuildFilterItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 newGuildFilterItemActionPerformed(evt);
             }
         });
 
         newFilterMenu.add(newGuildFilterItem);
         levelFilterItem.setText(java.util.ResourceBundle.getBundle("net/sourceforge/fraglets/yaelp/YaelpResources").getString("levelFilterItem.text"));
         levelFilterItem.setToolTipText(java.util.ResourceBundle.getBundle("net/sourceforge/fraglets/yaelp/YaelpResources").getString("levelFilterItem.tooltip"));
         levelFilterItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 levelFilterItemActionPerformed(evt);
             }
         });
 
         newFilterMenu.add(levelFilterItem);
         classFilterItem.setText(java.util.ResourceBundle.getBundle("net/sourceforge/fraglets/yaelp/YaelpResources").getString("classFilterItem.text"));
         classFilterItem.setToolTipText(java.util.ResourceBundle.getBundle("net/sourceforge/fraglets/yaelp/YaelpResources").getString("classFilterItem.tooltip"));
         classFilterItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 classFilterItemActionPerformed(evt);
             }
         });
 
         newFilterMenu.add(classFilterItem);
         cultureFilterItem.setText(java.util.ResourceBundle.getBundle("net/sourceforge/fraglets/yaelp/YaelpResources").getString("cultureFilterItem.text"));
         cultureFilterItem.setToolTipText(java.util.ResourceBundle.getBundle("net/sourceforge/fraglets/yaelp/YaelpResources").getString("cultureFilterItem.tooltip"));
         cultureFilterItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cultureFilterItemActionPerformed(evt);
             }
         });
 
         newFilterMenu.add(cultureFilterItem);
         afterFilterItem.setText(java.util.ResourceBundle.getBundle("net/sourceforge/fraglets/yaelp/YaelpResources").getString("afterFilterItem.text"));
         afterFilterItem.setToolTipText(java.util.ResourceBundle.getBundle("net/sourceforge/fraglets/yaelp/YaelpResources").getString("afterFilterItem.tooltip"));
         afterFilterItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 afterFilterItemActionPerformed(evt);
             }
         });
 
         newFilterMenu.add(afterFilterItem);
         filterMenu.add(newFilterMenu);
         menuBar.add(filterMenu);
         styleMenu.setText(java.util.ResourceBundle.getBundle("net/sourceforge/fraglets/yaelp/YaelpResources").getString("styleMenu.text"));
         styleMenu.setToolTipText(java.util.ResourceBundle.getBundle("net/sourceforge/fraglets/yaelp/YaelpResources").getString("styleMenu.tooltip"));
         defaultStyleItem.setSelected(true);
         defaultStyleItem.setText(java.util.ResourceBundle.getBundle("net/sourceforge/fraglets/yaelp/YaelpResources").getString("defaultStyleItem.text"));
         defaultStyleItem.setToolTipText(java.util.ResourceBundle.getBundle("net/sourceforge/fraglets/yaelp/YaelpResources").getString("defaultStyleItem.tooltip"));
         styleMenu.add(defaultStyleItem);
         styleMenu.add(separator5);
         mwStyleItem.setText(java.util.ResourceBundle.getBundle("net/sourceforge/fraglets/yaelp/YaelpResources").getString("mwStyleItem.text"));
         mwStyleItem.setToolTipText(java.util.ResourceBundle.getBundle("net/sourceforge/fraglets/yaelp/YaelpResources").getString("mwStyleItem.tooltip"));
         styleMenu.add(mwStyleItem);
         ssStyleItem.setText(java.util.ResourceBundle.getBundle("net/sourceforge/fraglets/yaelp/YaelpResources").getString("ssStyleItem.text"));
         ssStyleItem.setToolTipText(java.util.ResourceBundle.getBundle("net/sourceforge/fraglets/yaelp/YaelpResources").getString("ssStyleItem.tooltip"));
         styleMenu.add(ssStyleItem);
         vlStyleItem.setText(java.util.ResourceBundle.getBundle("net/sourceforge/fraglets/yaelp/YaelpResources").getString("vlStyleItem.text"));
         vlStyleItem.setToolTipText(java.util.ResourceBundle.getBundle("net/sourceforge/fraglets/yaelp/YaelpResources").getString("vlStyleItem.tooltip"));
         styleMenu.add(vlStyleItem);
         styleMenu.add(separator6);
         newStyleMenu.setText(java.util.ResourceBundle.getBundle("net/sourceforge/fraglets/yaelp/YaelpResources").getString("newStyleMenu.text"));
         styleWizardItem.setText(java.util.ResourceBundle.getBundle("net/sourceforge/fraglets/yaelp/YaelpResources").getString("styleWizardItem.text"));
         styleWizardItem.setToolTipText(java.util.ResourceBundle.getBundle("net/sourceforge/fraglets/yaelp/YaelpResources").getString("styleWizardItem.tooltip"));
         styleWizardItem.setEnabled(false);
         newStyleMenu.add(styleWizardItem);
         styleFileItem.setText(java.util.ResourceBundle.getBundle("net/sourceforge/fraglets/yaelp/YaelpResources").getString("styleFileItem.text"));
         styleFileItem.setToolTipText(java.util.ResourceBundle.getBundle("net/sourceforge/fraglets/yaelp/YaelpResources").getString("styleFileItem.tooltip"));
         styleFileItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 styleFileItemActionPerformed(evt);
             }
         });
 
         newStyleMenu.add(styleFileItem);
         styleMenu.add(newStyleMenu);
         menuBar.add(styleMenu);
         optionsMenu.setText(java.util.ResourceBundle.getBundle("net/sourceforge/fraglets/yaelp/YaelpResources").getString("optionsMenu.text"));
         displayHTMLItem.setText(java.util.ResourceBundle.getBundle("net/sourceforge/fraglets/yaelp/YaelpResources").getString("displayHTMLItem.text"));
         displayHTMLItem.setToolTipText(java.util.ResourceBundle.getBundle("net/sourceforge/fraglets/yaelp/YaelpResources").getString("displayHTMLItem.tooltip"));
         optionsMenu.add(displayHTMLItem);
         menuBar.add(optionsMenu);
         helpMenu.setText(java.util.ResourceBundle.getBundle("net/sourceforge/fraglets/yaelp/YaelpResources").getString("helpMenu.text"));
         aboutItem.setText(java.util.ResourceBundle.getBundle("net/sourceforge/fraglets/yaelp/YaelpResources").getString("aboutItem.text"));
         aboutItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 aboutItemActionPerformed(evt);
             }
         });
 
         helpMenu.add(aboutItem);
         licenseItem.setText(java.util.ResourceBundle.getBundle("net/sourceforge/fraglets/yaelp/YaelpResources").getString("licenseItem.text"));
         licenseItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 licenseItemActionPerformed(evt);
             }
         });
 
         helpMenu.add(licenseItem);
         xpLicenseItem.setText(java.util.ResourceBundle.getBundle("net/sourceforge/fraglets/yaelp/YaelpResources").getString("xpLicenseItem.text"));
         xpLicenseItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 xpLicenseItemActionPerformed(evt);
             }
         });
 
         helpMenu.add(xpLicenseItem);
         xtLicenseItem.setText(java.util.ResourceBundle.getBundle("net/sourceforge/fraglets/yaelp/YaelpResources").getString("xtLicenseItem.text"));
         xtLicenseItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 xtLicenseItemActionPerformed(evt);
             }
         });
 
         helpMenu.add(xtLicenseItem);
         menuBar.add(helpMenu);
         setJMenuBar(menuBar);
 
         pack();
     }//GEN-END:initComponents
 
     private void propertyMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_propertyMenuItemActionPerformed
         // Add your handling code here:
         int row = rosterTable.getSelectedRow();
         if (row < 0) {
             return; // nothing selected
         }
         if (propertyEditor == null) {
             propertyEditor = new PropertyEditor(this, true);
         }
         propertyEditor.setAvatar(model.getAvatar(row));
         propertyEditor.show();
     }//GEN-LAST:event_propertyMenuItemActionPerformed
 
     private void dohMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dohMenuItemActionPerformed
         int rows[] = rosterTable.getSelectedRows();
         for (int i = 0; i < rows.length; i++) {
             Avatar avatar = model.getAvatar(rows[i]);
             int level = avatar.getLevel();
             if (level > 1) {
                 avatar.setLevel(level - 1);
                 model.fireTableCellUpdated(rows[i], 3); // HACK
             }
         }
     }//GEN-LAST:event_dohMenuItemActionPerformed
 
     private void dingMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dingMenuItemActionPerformed
         int rows[] = rosterTable.getSelectedRows();
         for (int i = 0; i < rows.length; i++) {
             Avatar avatar = model.getAvatar(rows[i]);
             int level = avatar.getLevel();
             if (level > 0) {
                 avatar.setLevel(level + 1);
                 model.fireTableCellUpdated(rows[i], 3); // HACK
             }
         }
     }//GEN-LAST:event_dingMenuItemActionPerformed
 
     private void afterFilterItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_afterFilterItemActionPerformed
         DateInput input;
         int row = rosterTable.getSelectedRow();
         if (row >= 0) {
             input = new DateInput(model.getAvatar(row).getTimestamp());
         } else {
             input = new DateInput();
         }
         input.setLabel("After Date: ");
         int result = javax.swing.JOptionPane.showConfirmDialog
             (this, input, "New Timestamp Filter",
              javax.swing.JOptionPane.OK_CANCEL_OPTION,
              javax.swing.JOptionPane.QUESTION_MESSAGE);
         if (result == javax.swing.JOptionPane.OK_OPTION) {
             AvatarFilter filter =
                 new AvatarFilter.Time(input.getTime(), false);
             doNewFilter(filter.toString(), filter);
         }
     }//GEN-LAST:event_afterFilterItemActionPerformed
 
     private void noFilterItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_noFilterItemActionPerformed
         int scan = filterMenu.getItemCount();
         while (--scan >= 0) {
             javax.swing.JMenuItem item = filterMenu.getItem(scan);
             if (item != null && item.isSelected()) {
                 item.setSelected(false);
             }
         }
         refreshFilter();
     }//GEN-LAST:event_noFilterItemActionPerformed
 
     private void newEntryItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newEntryItemActionPerformed
         String name = javax.swing.JOptionPane.showInputDialog
             (this, "Character name", "New Entry",
              javax.swing.JOptionPane.QUESTION_MESSAGE);
         fixBackingStore();
         if (name != null && name.length() > 0) {
             Avatar avatar = recognizer.updateAvatar
                 (0, name, 0, null, null, null, null);
             if (avatarFilter != null && !avatarFilter.accept(avatar)) {
                 // hidden by filter
                 javax.swing.JOptionPane.showMessageDialog
                     (this, "New entry is hidden, disabling all filters.",
                      "New Entry", javax.swing.JOptionPane.INFORMATION_MESSAGE);
                 noFilterItemActionPerformed(evt);
             } else if (avatar.getTimestamp() == 0) {
                 // really a new entry, will have to refresh roster
                 avatar.setTimestamp(System.currentTimeMillis());
                 setRoster(recognizer.getAvatars(), recognizer.getLines());
             }
             // search position in current roster
             Avatar roster[] = model.getRoster();
             int index = java.util.Arrays
                 .binarySearch(roster, avatar, model.getOrder());
             if (index < 0) {
                 doException(new RuntimeException("new entry not ound in roster"));
             } else {
                 rosterTable.setRowSelectionInterval(index, index);
                 rosterTable.scrollRectToVisible(rosterTable.getCellRect(index, 0, true));
             }
         }
     }//GEN-LAST:event_newEntryItemActionPerformed
 
     private void hideMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hideMenuItemActionPerformed
         // find the set filter menu entry
         AvatarFilter.Set setFilter = null;
         javax.swing.JMenuItem filterItem = null;
         int scan = filterMenu.getItemCount();
         while (--scan >= 0) {
             javax.swing.JMenuItem item = filterMenu.getItem(scan);
             if (item != null) {
                 Object filter = item.getClientProperty(FILTER_PROPERTY);
                 if (filter instanceof AvatarFilter.Not) {
                     filter = ((AvatarFilter.Not)filter).getFilter();
                 }
                 if (filter instanceof AvatarFilter.Set) {
                     setFilter = (AvatarFilter.Set)filter;
                     filterItem = item;
                     if (!item.isSelected()) {
                         setFilter.getSet().clear();
                     }
                     break; // found
                 }
             }
         }
         if (setFilter == null) {
             setFilter = new AvatarFilter.Set();
         }
         int rows[] = rosterTable.getSelectedRows();
         for (int i = 0; i < rows.length; i++) {
             setFilter.add(model.getAvatar(rows[i]));
         }
         if (rosterTable.isEditing()) {
             rosterTable.getCellEditor().cancelCellEditing();
         }
         if (filterItem != null) {
             filterItem.setSelected(true);
             refreshFilter();
         } else {
             doNewFilter("hide selection", new AvatarFilter.Not(setFilter));
         }
     }//GEN-LAST:event_hideMenuItemActionPerformed
 
     private void rosterTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_rosterTableMouseClicked
         if (isPopupTrigger(evt)) {
             propertyMenuItem.setEnabled(rosterTable.getSelectedRow() >= 0);
             contextMenu.show(rosterTable, evt.getX(), evt.getY());
         }
     }//GEN-LAST:event_rosterTableMouseClicked
 
     private void cultureFilterItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cultureFilterItemActionPerformed
         Object values[] = Avatar.Culture.getValues().toArray();
         java.util.Arrays.sort(values, Avatar.Culture.getComparator());
         Object input = javax.swing.JOptionPane.showInputDialog
             (this, "Culture", "New Culture Filter", javax.swing.JOptionPane.QUESTION_MESSAGE,
              null, values, null);
         if (input != null) {
             Avatar.Culture culture = (Avatar.Culture)input;
             doNewFilter(culture.getName(), new AvatarFilter.Culture(culture));
         }
     }//GEN-LAST:event_cultureFilterItemActionPerformed
 
     private void classFilterItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_classFilterItemActionPerformed
         Object values[] = Avatar.Class.getValues().toArray();
         java.util.Arrays.sort(values, Avatar.Class.getComparator());
         Object input = javax.swing.JOptionPane.showInputDialog
             (this, "Class", "New Class Filter", javax.swing.JOptionPane.QUESTION_MESSAGE,
              null, values, null);
         if (input != null) {
             Avatar.Class clazz = (Avatar.Class)input;
             doNewFilter(clazz.getName(), new AvatarFilter.Class(clazz));
         }
     }//GEN-LAST:event_classFilterItemActionPerformed
 
     private void levelFilterItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_levelFilterItemActionPerformed
         String input = javax.swing.JOptionPane.showInputDialog(this, "Minimum Level");
         if (input != null) try {
             int level = Integer.parseInt(input);
             doNewFilter("min level "+level, new AvatarFilter.Level(level));
         } catch (NumberFormatException ex) {
             ex.printStackTrace();
             doException(new NumberFormatException("invalid number: "+ex.getMessage()));
         }
     }//GEN-LAST:event_levelFilterItemActionPerformed
 
     private void newGuildFilterItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newGuildFilterItemActionPerformed
         Object values[] = Avatar.Guild.getValues().toArray();
         java.util.Arrays.sort(values, Avatar.Guild.getComparator());
         Object input = javax.swing.JOptionPane.showInputDialog
             (this, "Guild", "New Guild Filter", javax.swing.JOptionPane.QUESTION_MESSAGE,
              null, values, null);
         if (input != null) {
             Avatar.Guild guild = (Avatar.Guild)input;
             doNewFilter(guild.getName(), new AvatarFilter.Guild(guild));
         }
     }//GEN-LAST:event_newGuildFilterItemActionPerformed
 
     private void styleFileItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_styleFileItemActionPerformed
         JFileChooser chooser = getChooser("xsl");
         chooser.setApproveButtonText("Use Style");
         chooser.setDialogTitle("Use New Style");
         chooser.setFileSelectionMode(chooser.FILES_ONLY);
         chooser.setMultiSelectionEnabled(false);
         chooser.setSelectedFile(null);
         chooser.resetChoosableFileFilters();
         chooser.addChoosableFileFilter(new javax.swing.filechooser.FileFilter() {
             public boolean accept(java.io.File file) {
                 return file.isDirectory() ||
                     file.getName().endsWith(".xsl");
             }
             public String getDescription() {
                 return "XSL Style Files (*.xsl)";
             }
         });
         if (chooser.showOpenDialog(this) == chooser.APPROVE_OPTION) {
             fixBackingStore();
             doNewStyle(chooser.getSelectedFile());
         } else {
             fixBackingStore();
         }
     }//GEN-LAST:event_styleFileItemActionPerformed
 
     private void xtLicenseItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_xtLicenseItemActionPerformed
         doShowDocument("XSL Transformation License", "copying_xt.txt");
     }//GEN-LAST:event_xtLicenseItemActionPerformed
 
     private void xpLicenseItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_xpLicenseItemActionPerformed
         doShowDocument("XML Parser License", "copying_xp.txt");
     }//GEN-LAST:event_xpLicenseItemActionPerformed
 
     private void filterItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filterItemActionPerformed
         refreshFilter();
     }//GEN-LAST:event_filterItemActionPerformed
 
     private void exportHTMLItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportHTMLItemActionPerformed
         JFileChooser chooser = getChooser("xml");
         chooser.setApproveButtonText("Export");
         chooser.setDialogTitle("Export HTML");
         chooser.setFileSelectionMode(chooser.FILES_ONLY);
         chooser.setMultiSelectionEnabled(false);
         chooser.setSelectedFile(null);
         chooser.resetChoosableFileFilters();
         chooser.addChoosableFileFilter(new javax.swing.filechooser.FileFilter() {
             public boolean accept(java.io.File file) {
                 return file.isDirectory() || file.getName().endsWith(".html");
             }
             public String getDescription() {
                 return "HTML Files (*.html)";
             }
         });
         if (chooser.showSaveDialog(this) == chooser.APPROVE_OPTION) {
             fixBackingStore();
             doExportHTML(chooser.getSelectedFile());
         } else {
             fixBackingStore();
         }
     }//GEN-LAST:event_exportHTMLItemActionPerformed
 
     private void exportXMLItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportXMLItemActionPerformed
         JFileChooser chooser = getChooser("xml");
         chooser.setApproveButtonText("Export");
         chooser.setDialogTitle("Export XML");
         chooser.setFileSelectionMode(chooser.FILES_ONLY);
         chooser.setMultiSelectionEnabled(false);
         chooser.setSelectedFile(null);
         chooser.resetChoosableFileFilters();
         chooser.addChoosableFileFilter(new javax.swing.filechooser.FileFilter() {
             public boolean accept(java.io.File file) {
                 return file.isDirectory() || file.getName().endsWith(".xml");
             }
             public String getDescription() {
                 return "XML Files (*.xml)";
             }
         });
         if (chooser.showSaveDialog(this) == chooser.APPROVE_OPTION) {
             fixBackingStore();
             doExportXML(chooser.getSelectedFile());
         } else {
             fixBackingStore();
         }
     }//GEN-LAST:event_exportXMLItemActionPerformed
 
     private void licenseItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_licenseItemActionPerformed
         // Add your handling code here:
         doShowDocument("YAELP License", "gpl.txt");
     }//GEN-LAST:event_licenseItemActionPerformed
 
     private void aboutItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutItemActionPerformed
         javax.swing.JOptionPane.showMessageDialog
             (RosterFrame.this, getAboutText(), "About YAELP", javax.swing.JOptionPane.INFORMATION_MESSAGE);
         fixBackingStore();
     }//GEN-LAST:event_aboutItemActionPerformed
 
     private void exportTableItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportTableItemActionPerformed
         JFileChooser chooser = getChooser("xml");
         chooser.setApproveButtonText("Export");
         chooser.setDialogTitle("Export table");
         chooser.setFileSelectionMode(chooser.FILES_ONLY);
         chooser.setMultiSelectionEnabled(false);
         chooser.setSelectedFile(null);
         chooser.resetChoosableFileFilters();
         chooser.addChoosableFileFilter(new javax.swing.filechooser.FileFilter() {
             public boolean accept(java.io.File file) {
                 return file.isDirectory() ||
                     file.getName().endsWith(".csv") ||
                     file.getName().endsWith(".txt");
             }
             public String getDescription() {
                 return "Table Files (*.csv, *.txt)";
             }
         });
         if (chooser.showSaveDialog(this) == chooser.APPROVE_OPTION) {
             fixBackingStore();
             doExport(chooser.getSelectedFile());
         } else {
             fixBackingStore();
         }
     }//GEN-LAST:event_exportTableItemActionPerformed
 
     private void quitItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_quitItemActionPerformed
         System.exit(0);
     }//GEN-LAST:event_quitItemActionPerformed
 
     private void parseFileItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_parseFileItemActionPerformed
         JFileChooser chooser = getChooser("txt");
         chooser.setApproveButtonText("Parse");
         chooser.setDialogTitle("Parse files");
         chooser.setFileSelectionMode(chooser.FILES_ONLY);
         chooser.setMultiSelectionEnabled(true);
         chooser.setSelectedFile(null);
         chooser.resetChoosableFileFilters();
         chooser.addChoosableFileFilter(new javax.swing.filechooser.FileFilter() {
             public boolean accept(java.io.File file) {
                 return file.isDirectory() ||
                     file.getName().endsWith(".txt") ||
                     file.getName().endsWith(".txt.gz");
             }
             public String getDescription() {
                 return "Log Files (*.txt, *.txt.gz)";
             }
         });
         if (chooser.showOpenDialog(this) == chooser.APPROVE_OPTION) {
             fixBackingStore();
             doParse(chooser.getSelectedFiles());
         } else {
             fixBackingStore();
         }
     }//GEN-LAST:event_parseFileItemActionPerformed
 
     /** Exit the Application */
     private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
         System.exit(0);
     }//GEN-LAST:event_exitForm
 
     /** Refresh the filter expression based on the current filter
      * selections.
      */    
     protected void refreshFilter() {
         avatarFilter = null;
         int scan;
         // or-connect all spot filters
         AvatarFilter guildFilter = null;
         AvatarFilter classFilter = null;
         AvatarFilter cultureFilter = null;
         scan = filterMenu.getItemCount();
         while (--scan >= 0) {
             javax.swing.JMenuItem item = filterMenu.getItem(scan);
             if (item != null && item.isSelected()) {
                 Object filter = item.getClientProperty(FILTER_PROPERTY);
                 if (filter instanceof AvatarFilter.Guild) {
                     guildFilter = appendFilterOr(guildFilter, (AvatarFilter.Guild)filter);
                 } else if (filter instanceof AvatarFilter.Class) {
                     classFilter = appendFilterOr(classFilter, (AvatarFilter.Class)filter);
                 } else if (filter instanceof AvatarFilter.Culture) {
                     cultureFilter = appendFilterOr(cultureFilter, (AvatarFilter.Culture)filter);
                 } else if (filter instanceof AvatarFilter) {
                     avatarFilter = appendFilterAnd(avatarFilter, (AvatarFilter)filter);
                 } else {
                     System.err.println("unrecognized filter: "+filter);
                 }
             }
         }
         // and-connect the results
         avatarFilter = appendFilterAnd(avatarFilter, guildFilter);
         avatarFilter = appendFilterAnd(avatarFilter, classFilter);
         avatarFilter = appendFilterAnd(avatarFilter, cultureFilter);
         setRoster(recognizer.getAvatars(), recognizer.getLines());
     }
     
     /** Initialize editors for enumerated values. */
     protected void initEditors() {
         javax.swing.border.LineBorder blackBorder =
             new javax.swing.border.LineBorder(java.awt.Color.black);
         Class editingClass[] = {
             Avatar.Class.class,
             Avatar.Culture.class,
             Avatar.Guild.class,
             Avatar.Zone.class,
         };
         
         for (int i = 0; i < editingClass.length; i++) {
             javax.swing.JComboBox selection = new javax.swing.JComboBox();
             selection.setBorder(blackBorder);
             selection.setEditable(true);
             rosterTable.setDefaultEditor(editingClass[i],
                 new javax.swing.DefaultCellEditor(selection));
         }
     }
     
     /** Update the editor for the new instance. */
     protected void updateEditor(Object newInstance) {
         javax.swing.table.TableCellEditor editor = rosterTable
             .getDefaultEditor(newInstance.getClass());
         java.awt.Component editorComponent = editor
             .getTableCellEditorComponent(rosterTable, newInstance, false, 0, 0);
         javax.swing.DefaultComboBoxModel editorModel =
             (javax.swing.DefaultComboBoxModel)
             ((javax.swing.JComboBox)editorComponent).getModel();
         if (newInstance instanceof Comparable) {
             Comparable c = (Comparable)newInstance;
             int end = editorModel.getSize();
             int scan = 0;
             while (scan < end && c.compareTo(editorModel.getElementAt(scan)) > 0)
                 scan++;
             editorModel.insertElementAt(newInstance, scan);
         } else {
             editorModel.addElement(newInstance);
         }
     }
     
     protected static AvatarFilter appendFilterOr(AvatarFilter list, AvatarFilter filter) {
         if (list == null) {
             return filter;
         } else if (filter == null) {
             return list;
         } else {
             return new AvatarFilter.Or(list, filter);
         }
     }
     
     protected static AvatarFilter appendFilterAnd(AvatarFilter list, AvatarFilter filter) {
         if (list == null) {
             return filter;
         } else if (filter == null) {
             return list;
         } else {
             return new AvatarFilter.And(list, filter);
         }
     }
     
     protected void fixBackingStore() {
         // hack to fix dialog backing store bug
         javax.swing.RepaintManager.currentManager(this)
             .markCompletelyDirty(getRootPane());
     }
     
     /** Initialize the roster table with a new list of avatars.
      * @param avatars the avatars to be displayed in the roster
      * @param lines lines parsed to create the provided list
      */    
     protected void setRoster(Avatar avatars[], int lines) {
         int hidden = 0;
         if (avatarFilter != null) {
             int end = avatars.length;
             int i0 = 0;
             int i1 = 0;
             while (i0 < end) {
                 if (avatarFilter.accept(avatars[i0])) {
                     if (i1 < i0) {
                         avatars[i1] = avatars[i0];
                     }
                     i1++;
                 }
                 i0++;
             }
             if (i1 < end) {
                 Avatar shrink[] = new Avatar[i1];
                 System.arraycopy(avatars, 0, shrink, 0, i1);
                 avatars = shrink;
             }
             hidden = i0 - i1;
         }
         
         if (model == null) {
             model = new RosterTableModel(avatars);
             rosterTable.setModel(model);
         } else {
             model.setRoster(avatars);
         }
         status.setText(
             model.getRowCount() + " characters, " +
             (hidden>0 ? hidden + " hidden, " : "") +
             lines + " lines parsed");
     }
     
     protected void doNameOrder() {
         // sort by name
         model.setOrder(new java.util.Comparator() {
             public boolean equals(Object other) {
                 return other != null &&
                 other.getClass() == this.getClass();
             }
             public int compare(Object a, Object b) {
                 return ((Avatar)a).getName()
                 .compareTo(((Avatar)b).getName());
             }
         });
     }
     
     protected void doClassOrder() {
         // sort by class
         model.setOrder(new java.util.Comparator() {
             public boolean equals(Object other) {
                 return other != null &&
                 other.getClass() == this.getClass();
             }
             public int compare(Object a, Object b) {
                 return stringValue(((Avatar)a).getClazz())
                 .compareTo(stringValue(((Avatar)b).getClazz()));
             }
         });
     }
     
     protected void doCultureOrder() {
         // sort by culture
         model.setOrder(new java.util.Comparator() {
             public boolean equals(Object other) {
                 return other != null &&
                 other.getClass() == this.getClass();
             }
             public int compare(Object a, Object b) {
                 return stringValue(((Avatar)a).getCulture())
                 .compareTo(stringValue(((Avatar)b).getCulture()));
             }
         });
     }
     
     protected void doLevelOrder() {
         // sort by level
         model.setOrder(new java.util.Comparator() {
             public boolean equals(Object other) {
                 return other != null &&
                 other.getClass() == this.getClass();
             }
             public int compare(Object a, Object b) {
                 int delta = ((Avatar)a).getLevel() - ((Avatar)b).getLevel();
                 if (delta < 0) {
                     return 1;
                 } else if (delta > 0) {
                     return -1;
                 } else {
                     return 0;
                 }
             }
         });
     }
     
     protected void doTimeOrder() {
         // sort by time
         model.setOrder(new java.util.Comparator() {
             public boolean equals(Object other) {
                 return other != null &&
                 other.getClass() == this.getClass();
             }
             public int compare(Object a, Object b) {
                 long delta = ((Avatar)a).getTimestamp() - ((Avatar)b).getTimestamp();
                 if (delta < 0) {
                     return 1;
                 } else if (delta > 0) {
                     return -1;
                 } else {
                     return 0;
                 }
             }
         });
     }
     
     protected void doOrder(String order) {
         order = order.toLowerCase(); // HACK
         if (order.equals("name")) {
             doNameOrder();
         } else if (order.equals("class")) {
             doClassOrder();
         } else if (order.equals("culture")) {
             doCultureOrder();
         } else if (order.equals("level")) {
             doLevelOrder();
         } else if (order.equals("time")) {
             doTimeOrder();
         }
     }
     /** Parse a list of files for who lines.
      * @param files array of files to parse
      */    
     protected void doParse(java.io.File files[]) {
         try {
             for (int i = 0; i < files.length; i++) {
                 EqlogParser.parseFile(files[i], recognizer);
             }
             setRoster(recognizer.getAvatars(), recognizer.getLines());
         } catch (Exception ex) {
             ex.printStackTrace();
             doException(ex);
         }
     }
     
     protected void doShowDocument(String title, String resource) {
         doShowDocument(title, getClass().getResource(resource));
     }
     
     protected void doShowDocument(String title, java.net.URL text) {
         try {
             javax.swing.JEditorPane editorPane =
                 new javax.swing.JEditorPane(text);
             editorPane.setEditable(false);
             javax.swing.JScrollPane scrollPane =
                 new javax.swing.JScrollPane(editorPane);
             scrollPane.setPreferredSize(new java.awt.Dimension(550, 250));
             javax.swing.JOptionPane.showMessageDialog
                 (this, scrollPane, title, javax.swing.JOptionPane.INFORMATION_MESSAGE);
             fixBackingStore();
         } catch (java.io.IOException ex) {
             ex.printStackTrace();
             doException(ex);
         }
     }
 
     /** Export the current roster to a file.
      * @param file file to write the exported roster into
      */    
     protected void doExport(java.io.File file) {
         if (!doWriteConfirmation(file)) {
             return; // nothing selected, canceled.
         }
         try {
             java.io.FileOutputStream fos = new java.io.FileOutputStream(file);
             java.io.PrintStream out = new java.io.PrintStream(fos);
             
             // create header
             javax.swing.table.TableColumnModel header = rosterTable.getColumnModel();
             int cols = header.getColumnCount();
             for (int i = 0; i < cols; i++) {
                 if (i > 0) {
                     out.print(", ");
                 }
                 out.print(quote(header.getColumn(i).getHeaderValue().toString()));
             }
             out.println();
             
             // create table contents
             javax.swing.table.TableModel model = rosterTable.getModel();
             int rows = model.getRowCount();
             for (int j = 0; j < rows; j++) {
                 for (int i = 0; i < cols; i++) {
                     if (i > 0) {
                         out.print(", ");
                     }
                     Object value = model.getValueAt(j, i);
                     if (value != null) {
                         out.print(quote(value.toString()));
                     } else {
                         out.print("\"\"");
                     }
                 }
                 out.println();
             }
             
             out.close();
             fos.close();
         } catch (Exception ex) {
             ex.printStackTrace();
             doException(ex);
         }
     }
     
     protected void doNewFilter(String name, AvatarFilter filter) {
         javax.swing.JCheckBoxMenuItem menuItem =
             new javax.swing.JCheckBoxMenuItem(name, true);
         menuItem.putClientProperty(FILTER_PROPERTY, filter);
         menuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 refreshFilter();
             }
         });
         filterMenu.insert(menuItem, filterMenu.getItemCount()-1);
         refreshFilter();
     }
     
     protected void doNewStyle(java.io.File file) {
         if (file == null) {
             return; // canceled
         }
         if (file.exists()) try {
             final java.net.URL styleURL =
                 new java.net.URL("file:///"+file.getAbsolutePath());
             javax.swing.JRadioButtonMenuItem menuItem =
                 new javax.swing.JRadioButtonMenuItem(file.getName());
             menuItem.addActionListener(styleSelection);
             styleButtonGroup.add(menuItem);
             styleMenu.insert(menuItem, styleMenu.getItemCount()-1);
             menuItem.setSelected(true);
             styleSelection.setSelectedStyle(styleURL);
         } catch (Exception ex) {
             ex.printStackTrace();
             doException(ex);
         } else {
             javax.swing.JOptionPane.showMessageDialog(this, "File does not exist");
         }
     }
     
     /** Export the current roster to an XML file.
      * @param file file to write the exported roster into
      */    
     protected void doExportXML(java.io.File file) {
         if (!doWriteConfirmation(file)) {
             return; // nothing selected, canceled.
         }
         try {
             java.io.FileWriter out = new java.io.FileWriter(file);
             writeXML(out);
             out.close();
         } catch (Exception ex) {
             ex.printStackTrace();
             doException(ex);
         }
     }
     
     protected XSLProcessor processor;
     protected OutputMethodHandlerImpl handler;
     protected java.net.URL loadedStyle;
     
     /** Export the current roster to a HTML file.
      * @param file file to write the exported roster into
      */    
     protected void doExportHTML(java.io.File file) {
         if (!doWriteConfirmation(file)) {
             return; // nothing selected, canceled.
         }
         try {
             if (processor == null) {
                 processor = new XSLProcessorImpl();
                 processor.setParser(new CommentDriver());
                 processor.setErrorHandler(new ErrorHandler() {
                     public void warning(SAXParseException e) {
                         doException(e);
                     }
                     public void error(SAXParseException e) {
                         doException(e);
                     }
                     public void fatalError(SAXParseException e) throws SAXException {
                         throw e;
                     }
                 });
                 handler = new OutputMethodHandlerImpl(processor);
                 processor.setOutputMethodHandler(handler);
             }
             java.net.URL style = styleSelection.getSelectedStyle();
             if (loadedStyle == null || !loadedStyle.equals(style)) {
                 java.io.InputStream xsl = style.openStream();
                 processor.loadStylesheet(new InputSource(xsl));
                 xsl.close();
                 loadedStyle = style;
             }
             Destination out = new FileDestination(file);
             handler.setDestination(out);
             final java.io.PipedWriter pw = new java.io.PipedWriter();
             new Thread() {
                 public void run() {
                     try {
                         writeXML(pw);
                         pw.close();
                     } catch (java.io.IOException ex) {
                         ex.printStackTrace();
                     }
                 }
             }.start();
             processor.parse(new InputSource(new java.io.PipedReader(pw)));
             
             if (displayHTMLItem.isSelected()) {
                 doShowDocument("HTML Export Result",
                     new java.net.URL("file:///"+file.getAbsolutePath()));
             }
         } catch (Exception ex) {
             ex.printStackTrace();
             doException(ex);
         }
     }
     
     protected void writeXML(java.io.Writer out) throws java.io.IOException {
         java.io.PrintWriter pw = new java.io.PrintWriter(out);
         pw.println("<roster title=\"Roster\">"); // FIXME: allow setting title
         // create tag names
         javax.swing.table.TableColumnModel header = rosterTable.getColumnModel();
         int cols = header.getColumnCount();
         String tags[] = new String[cols];
         pw.println(" <heading>");
         for (int i = 0; i < cols; i++) {
             // HACK: we don't set identifiers explicitly yet,
             // so we use lower case header values.
             javax.swing.table.TableColumn column = header.getColumn(i);
             tags[i] = column.getIdentifier().toString().toLowerCase();
             pw.print("  <column id=\"");
             pw.print(tags[i]);
             pw.print("\">");
             pw.print(column.getHeaderValue());
             pw.println("</column>");
         }
         pw.println(" </heading>");
         
         // create XML
         int rows = rosterTable.getRowCount();
         for (int j = 0; j < rows; j++) {
             Avatar avatar = model.getAvatar(j);
             pw.print(" <avatar time=\"");
             pw.print(avatar.getTimestamp());
             pw.println("\">");
             for (int i = 0; i < cols; i++) {
                 pw.print("  <");
                 pw.print(tags[i]);
                 Object value = rosterTable.getValueAt(j, i);
                 if (value != null) {
                     pw.print(">");
                     pw.print(pcdata(value.toString()));
                     pw.print("</");
                     pw.print(tags[i]);
                     pw.println(">");
                 } else {
                     pw.println("/>");
                 }
             }
             Iterator i = avatar.getProperties();
             if (i != null) {
                 while (i.hasNext()) {
                     Map.Entry entry = (Map.Entry)i.next();
                     Avatar.TimestampEntry value =
                         (Avatar.TimestampEntry)entry.getValue();
                     pw.print("  <property name=\"");
                     pw.print(entry.getKey());
                     pw.print("\" value=\"");
                     pw.print(quote(value.toString(), "&<>\""));
                     pw.print("\" time=\"");
                     pw.print(value.timestamp);
                     pw.println("\"/>");
                 }
             }
             pw.println(" </avatar>");
         }
         pw.println("</roster>");
         pw.flush();
     }
     
     protected boolean doWriteConfirmation(java.io.File file) {
         if (file == null) {
             return false; // already canceled
         } else if (file.exists()) {
             return javax.swing.JOptionPane.OK_OPTION ==
                 javax.swing.JOptionPane.showConfirmDialog
                     (this, "Overwrite "+file+"?", "Overwrite Confirmation",
                      javax.swing.JOptionPane.OK_CANCEL_OPTION);
         }
         return true;
     }
     
     protected void doException(Throwable ex) {
         String message = ex.getLocalizedMessage();
         if (message == null) {
             message = ex.toString();
         }
         if (ex instanceof SAXParseException) {
             SAXParseException spe = (SAXParseException)ex;
             message += "(" + spe.getSystemId()
                 + " line " + spe.getLineNumber() + ")";
         }
         javax.swing.JOptionPane.showMessageDialog
         (RosterFrame.this, message, "Exception", javax.swing.JOptionPane.ERROR_MESSAGE);
         fixBackingStore();
     }
     
     /** Fix for the broken MouseEvent.isPopupTrigger(). */
     public static boolean isPopupTrigger(java.awt.event.MouseEvent evt) {
         return evt.isPopupTrigger() ||
             (evt.getModifiers() & evt.BUTTON3_MASK) > 0;
     }
     
     protected javax.swing.JFileChooser getChooser(String category) {
         if (chooserMap == null) {
             chooserMap = new HashMap();
         }
         JFileChooser chooser = (JFileChooser)chooserMap.get(category);
         if (chooser == null) {
             chooser = new javax.swing.JFileChooser();
             chooserMap.put(category, chooser);
             if (category.equals("txt")) {
                 String alternatives[] = {
                     "C:/Program Files/EverQuest",
                     "C:/Programme/EverQuest",
                 };
                 for (int i = 0; i < alternatives.length; i++) {
                     java.io.File eq = new java.io.File(alternatives[i]);
                     if (eq.exists() && eq.isDirectory()) {
                         chooser.setCurrentDirectory(eq);
                         break;
                     }
                 }
             } else if (lastChooser != null) {
                 chooser.setCurrentDirectory(((JFileChooser)chooserMap
                     .get(lastChooser)).getCurrentDirectory());
             }
         }
         lastChooser = category;
         return chooser;
     }
     
     protected static java.net.URL getResource(String name) {
         return RosterFrame.class.getResource(name);
     }
     
     public String getAboutText() {
         if (aboutText == null) try {
             //StringBuffer buffer = new StringBuffer("\n");
             java.io.InputStream stream =
             getClass().getResource("gpl.txt").openStream();
             stream.skip(15816);             // HACK
             byte text[] = new byte[720];    // HACK
             int count = 0;
             while (count < text.length) {
                 int n = stream.read(text, count, text.length-count);
                 if (n > 0) {
                     count += n;
                 } else if (n == -1) {
                     break;
                 }
             }
             aboutText =
            "YAELP log file parser, $Revision: 1.12 $.\n"+
             "Copyright  2001 Klaus Rennecke.\n"+
             "XML parser Copyright  1997, 1998 James Clark.\n"+
             "XSL transformation Copyright  1998, 1999 James Clark.\n"+
             "\n"+
             new String(text);
         } catch (java.io.IOException ex) {
             aboutText = "<ERROR: text missing>";
             ex.printStackTrace();
             doException(ex);
         }
         return aboutText;
     }
     
     /** Quote a string, escaping quotes in the original string in the process.
      * @param str the string to quote
      * @return the quoted string
      */
     public static String quote(String str) {
         StringBuffer buffer = new StringBuffer();
         buffer.append('"');
         if (str.indexOf('"') >= 0) {
             java.util.StringTokenizer tok = new java.util.StringTokenizer(str, "\"", true);
             while (tok.hasMoreTokens()) {
                 String token = tok.nextToken();
                 if (token.equals("\"")) {
                     buffer.append("\\\"");
                 } else {
                     buffer.append(token);
                 }
             }
         } else {
             buffer.append(str);
         }
         buffer.append('"');
         return buffer.toString();
     }
     
     /** Quote a string, ready to use in a #PCDATA section of XML
      * @param str the string to quote
      * @return the quoted string
      */
     public static String pcdata(String str) {
         return quote(str, "&<>");
     }
     
     /** Quote a string, ready to use in a #PCDATA section of XML
      * @param str the string to quote
      * @param delim the delimiters to quote in the string
      * @return the quoted string
      */
     public static String quote(String str, String delim) {
         StringBuffer buffer = new StringBuffer();
         java.util.StringTokenizer tok = new java.util.StringTokenizer(str, delim, true);
         while (tok.hasMoreTokens()) {
             String token = tok.nextToken();
             if (token.length() == 1) {
                 switch(token.charAt(0)) {
                     case '<':
                         buffer.append("&lt;");
                         break;
                     case '>':
                         buffer.append("&gt;");
                         break;
                     case '&':
                         buffer.append("&amp;");
                         break;
                     case '"':
                         buffer.append("&quot;");
                         break;
                     default:
                         buffer.append(token);
                 }
             } else {
                 buffer.append(token);
             }
         }
         return buffer.toString();
     }
     
     public static String stringValue(Object o) {
         if (o == null) {
             return "";
         } else {
             return o.toString();
         }
     }
     
     /** Start the application with a RosterFrame.
      * @param args the command line arguments
      */
     public static void main(String args[]) {
         try {
             javax.swing.UIManager.setLookAndFeel
                 (javax.swing.UIManager.getSystemLookAndFeelClassName());
         } catch (Exception ex) {
             // oh well ...
         }
         RosterFrame frame = new RosterFrame();
         System.setOut(new java.io.PrintStream
         (new DocumentStream(frame.status)));
         frame.show();
     }
     
     
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.ButtonGroup styleButtonGroup;
     private javax.swing.JMenu fileMenu;
     private javax.swing.JMenuItem cultureFilterItem;
     private javax.swing.JSeparator jSeparator3;
     private javax.swing.JMenu filterMenu;
     private javax.swing.JSeparator jSeparator2;
     private javax.swing.JSeparator jSeparator1;
     private javax.swing.JMenuItem exportXMLItem;
     private javax.swing.JMenuItem hideMenuItem;
     private javax.swing.JMenuItem licenseItem;
     private javax.swing.JTextField status;
     private javax.swing.JSeparator separator6;
     private javax.swing.JSeparator separator5;
     private javax.swing.JMenu newStyleMenu;
     private javax.swing.JMenuItem dohMenuItem;
     private javax.swing.JSeparator separator4;
     private javax.swing.JSeparator separator3;
     private javax.swing.JSeparator separator2;
     private javax.swing.JSeparator separator1;
     private javax.swing.JCheckBoxMenuItem friendFilterItem;
     private javax.swing.JMenuItem newEntryItem;
     private javax.swing.JRadioButtonMenuItem ssStyleItem;
     private javax.swing.JMenuItem xpLicenseItem;
     private javax.swing.JRadioButtonMenuItem mwStyleItem;
     private javax.swing.JMenuItem xtLicenseItem;
     private javax.swing.JMenuItem levelFilterItem;
     private javax.swing.JRadioButtonMenuItem vlStyleItem;
     private javax.swing.JMenuItem noFilterItem;
     private javax.swing.JCheckBoxMenuItem defaultFilterItem;
     private javax.swing.JCheckBoxMenuItem mainFilterItem;
     private javax.swing.JMenuItem classFilterItem;
     private javax.swing.JMenu helpMenu;
     private javax.swing.JMenuItem aboutItem;
     private javax.swing.JMenuItem newGuildFilterItem;
     private javax.swing.JScrollPane tableScroll;
     private javax.swing.JCheckBoxMenuItem level1FilterItem;
     private javax.swing.JMenu newFilterMenu;
     private javax.swing.JMenuBar menuBar;
     private javax.swing.JMenuItem dingMenuItem;
     private javax.swing.JMenu styleMenu;
     private javax.swing.JMenuItem styleWizardItem;
     private javax.swing.JPopupMenu contextMenu;
     private javax.swing.JMenuItem exportTableItem;
     private javax.swing.JMenuItem afterFilterItem;
     private javax.swing.JRadioButtonMenuItem defaultStyleItem;
     private javax.swing.JCheckBoxMenuItem displayHTMLItem;
     private javax.swing.JMenu optionsMenu;
     private javax.swing.JMenuItem exportHTMLItem;
     private javax.swing.JCheckBoxMenuItem alliedFilterItem;
     private javax.swing.JMenuItem propertyMenuItem;
     private javax.swing.JTable rosterTable;
     private javax.swing.JMenuItem parseFileItem;
     private javax.swing.JMenuItem quitItem;
     private javax.swing.JMenuItem styleFileItem;
     // End of variables declaration//GEN-END:variables
 
 }
