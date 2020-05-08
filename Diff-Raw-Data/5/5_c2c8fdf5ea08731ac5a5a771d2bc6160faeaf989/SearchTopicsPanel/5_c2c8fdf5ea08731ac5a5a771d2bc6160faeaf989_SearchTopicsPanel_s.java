 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 /*
  * SearchTopicsPanel.java
  *
  * Created on 30.11.2011, 13:33:09
  */
 package de.cismet.cismap.navigatorplugin.metasearch;
 
 import org.apache.log4j.Logger;
 
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.Insets;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.swing.Box;
 import javax.swing.JCheckBox;
 import javax.swing.JLabel;
 
 /**
  * DOCUMENT ME!
  *
  * @author   jweintraut
  * @version  $Revision$, $Date$
  */
 public class SearchTopicsPanel extends javax.swing.JPanel {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final Logger LOG = Logger.getLogger(SearchTopicsPanel.class);
 
     //~ Instance fields --------------------------------------------------------
 
     private Set<SearchTopic> searchTopics;
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.Box.Filler gluFiller;
     private org.jdesktop.swingx.JXHyperlink hypSelectAll;
     private org.jdesktop.swingx.JXHyperlink hypSelectNone;
     private javax.swing.JLabel lblSelection;
     private javax.swing.JLabel lblSeparator;
     private javax.swing.JPanel pnlControls;
     // End of variables declaration//GEN-END:variables
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates new form SearchTopicsPanel.
      */
     public SearchTopicsPanel() {
         searchTopics = new HashSet<SearchTopic>();
         initComponents();
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * Using this method you can specify which search topics are to be displayed by this panel.
      * setSearchTopics(Collection<SearchTopic>) creates the UI elements and layouts them.
      *
      * @param  searchTopics  The search topics to display.
      */
     public void setSearchTopics(final Collection<SearchTopic> searchTopics) {
         if ((searchTopics == null) || searchTopics.isEmpty()) {
             return;
         }
 
         this.searchTopics.clear();
         removeAll();
         if (LOG.isDebugEnabled()) {
             LOG.debug("Setting search topics: " + searchTopics);
         }
 
         int maxRowWidth = 0;
         int rowHeight = 0;
 
        final Insets insetsIcon = new Insets(0, 5, 0, 2);
        final Insets insetsCheckbox = new Insets(0, 2, 0, 5);
         final SearchTopic[] searchTopicsArray = searchTopics.toArray(new SearchTopic[0]);
         for (int i = 0; i < searchTopicsArray.length; i++) {
             final SearchTopic searchTopic = searchTopicsArray[i];
 
             if (!this.searchTopics.add(searchTopic)) {
                 if (LOG.isDebugEnabled()) {
                     LOG.debug("Search topic '" + searchTopic.getName() + " - " + searchTopic.getDescription()
                                 + "' couldn't be added. Maybe it's defined twice.");
                 }
                 continue;
             }
 
             final JLabel lblIcon = new JLabel(searchTopic.getIcon());
             final JCheckBox chkSearchTopic = new JCheckBox(searchTopic.getName());
 
             lblIcon.setToolTipText(searchTopic.getDescription());
             chkSearchTopic.setBackground(getBackground());
             chkSearchTopic.setToolTipText(searchTopic.getDescription());
             chkSearchTopic.setSelected(searchTopic.isSelected());
 
             final ItemListener listener = new ItemListener() {
 
                     @Override
                     public void itemStateChanged(final ItemEvent e) {
                         searchTopic.setSelected(!searchTopic.isSelected());
                     }
                 };
             chkSearchTopic.addItemListener(listener);
             searchTopic.addPropertyChangeListener(new PropertyChangeListener() {
 
                     @Override
                     public void propertyChange(final PropertyChangeEvent evt) {
                         boolean newValue = false;
 
                         if (("selected".equalsIgnoreCase(evt.getPropertyName())
                                         || "SwingSelectedKey".equalsIgnoreCase(evt.getPropertyName()))
                                     && (evt.getNewValue() instanceof Boolean)) {
                             newValue = (Boolean)evt.getNewValue();
                         } else {
                             LOG.warn("Caught a propertyChangeEvent '" + evt + "' which could not be interpreted.");
                             return;
                         }
 
                         if (chkSearchTopic.isSelected() ^ newValue) {
                             // If a SearchTopic is changed "outside" (e. g. via GeoSearch menu), this has to be
                             // reflected here. The item listener has to be removed since it would be invoked while
                             // setting chkSearchTopic's new value.
                             chkSearchTopic.removeItemListener(listener);
                             chkSearchTopic.setSelected(newValue);
                             chkSearchTopic.addItemListener(listener);
                         }
                     }
                 });
 
             GridBagConstraints constraints = new GridBagConstraints();
             constraints.anchor = GridBagConstraints.LINE_START;
             constraints.gridx = 0;
             constraints.gridy = this.searchTopics.size() - 1;
 
             if (i == 0) {
                 constraints.insets = new Insets(5, 7, 0, 2);
             } else if (i == (searchTopicsArray.length - 1)) {
                 constraints.insets = new Insets(0, 7, 5, 2);
             } else {
                 constraints.insets = insetsIcon;
             }
 
             add(lblIcon, constraints);
 
             constraints = new GridBagConstraints();
             constraints.anchor = GridBagConstraints.LINE_START;
             constraints.fill = GridBagConstraints.HORIZONTAL;
             constraints.gridx = 1;
             constraints.gridy = this.searchTopics.size() - 1;
 
             if (i == 0) {
                 constraints.insets = new Insets(5, 2, 0, 7);
             } else if (i == (searchTopicsArray.length - 1)) {
                 constraints.insets = new Insets(0, 2, 5, 7);
             } else {
                 constraints.insets = insetsCheckbox;
             }
 
             constraints.weightx = 1.0;
             add(chkSearchTopic, constraints);
 
             final int rowWidth = lblIcon.getWidth() + chkSearchTopic.getWidth() + 10;
             if (rowWidth > maxRowWidth) {
                 maxRowWidth = rowWidth;
             }
 
             rowHeight = chkSearchTopic.getHeight();
 
             if (LOG.isDebugEnabled()) {
                 LOG.debug("Added '" + searchTopic.getName() + " - " + searchTopic.getDescription() + "' on position "
                             + (this.searchTopics.size() - 1));
             }
         }
 
         setMinimumSize(new Dimension(maxRowWidth, 20 * rowHeight));
 
         final Component gluFiller = Box.createGlue();
         GridBagConstraints constraints = new GridBagConstraints();
         constraints.fill = GridBagConstraints.BOTH;
         constraints.gridx = 0;
         constraints.gridy = this.searchTopics.size();
         constraints.gridwidth = 2;
         constraints.weighty = 1.0;
         add(gluFiller, constraints);
 
         constraints = new GridBagConstraints();
         constraints.fill = GridBagConstraints.HORIZONTAL;
         constraints.gridx = 0;
         constraints.gridy = this.searchTopics.size() + 1;
         constraints.gridwidth = 2;
         constraints.weightx = 1.0;
         add(pnlControls, constraints);
 
         revalidate();
     }
 
     /**
      * Adds the given ItemListener to every checkbox representing a search topic.
      *
      * @param  itemListener  The ItemListener to add.
      */
     public void registerItemListener(final ItemListener itemListener) {
         for (final Component component : getComponents()) {
             if (component instanceof JCheckBox) {
                 final JCheckBox checkbox = (JCheckBox)component;
                 checkbox.addItemListener(itemListener);
             }
         }
     }
 
     /**
      * Removes the given ItemListener from every checkbox representing a search topic.
      *
      * @param  itemListener  The ItemListener to remove.
      */
     public void unregisterItemListener(final ItemListener itemListener) {
         for (final Component component : getComponents()) {
             if (component instanceof JCheckBox) {
                 final JCheckBox checkbox = (JCheckBox)component;
                 checkbox.removeItemListener(itemListener);
             }
         }
     }
 
     /**
      * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
      * content of this method is always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
         java.awt.GridBagConstraints gridBagConstraints;
 
         pnlControls = new javax.swing.JPanel();
         lblSelection = new javax.swing.JLabel();
         hypSelectAll = new org.jdesktop.swingx.JXHyperlink();
         lblSeparator = new javax.swing.JLabel();
         hypSelectNone = new org.jdesktop.swingx.JXHyperlink();
         gluFiller = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0),
                 new java.awt.Dimension(0, 0),
                 new java.awt.Dimension(32767, 0));
 
         pnlControls.setBackground(javax.swing.UIManager.getDefaults().getColor("List.background"));
         pnlControls.setLayout(new java.awt.GridBagLayout());
 
         lblSelection.setIcon(new javax.swing.ImageIcon(
                 getClass().getResource(
                     "/de/cismet/cismap/navigatorplugin/metasearch/SearchTopicsPanel_lblSelection.png"))); // NOI18N
         lblSelection.setText(org.openide.util.NbBundle.getMessage(
                 SearchTopicsPanel.class,
                 "SearchTopicsPanel.lblSelection.text"));                                                  // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
         gridBagConstraints.insets = new java.awt.Insets(0, 7, 0, 0);
         pnlControls.add(lblSelection, gridBagConstraints);
 
         hypSelectAll.setText(org.openide.util.NbBundle.getMessage(
                 SearchTopicsPanel.class,
                 "SearchTopicsPanel.hypSelectAll.text"));        // NOI18N
         hypSelectAll.setToolTipText(org.openide.util.NbBundle.getMessage(
                 SearchTopicsPanel.class,
                 "SearchTopicsPanel.hypSelectAll.toolTipText")); // NOI18N
         hypSelectAll.setFocusPainted(false);
         hypSelectAll.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         hypSelectAll.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     hypSelectAllActionPerformed(evt);
                 }
             });
         pnlControls.add(hypSelectAll, new java.awt.GridBagConstraints());
 
         lblSeparator.setText(org.openide.util.NbBundle.getMessage(
                 SearchTopicsPanel.class,
                 "SearchTopicsPanel.lblSeparator.text")); // NOI18N
         pnlControls.add(lblSeparator, new java.awt.GridBagConstraints());
 
         hypSelectNone.setText(org.openide.util.NbBundle.getMessage(
                 SearchTopicsPanel.class,
                 "SearchTopicsPanel.hypSelectNone.text"));        // NOI18N
         hypSelectNone.setToolTipText(org.openide.util.NbBundle.getMessage(
                 SearchTopicsPanel.class,
                 "SearchTopicsPanel.hypSelectNone.toolTipText")); // NOI18N
         hypSelectNone.setFocusPainted(false);
         hypSelectNone.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         hypSelectNone.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     hypSelectNoneActionPerformed(evt);
                 }
             });
         pnlControls.add(hypSelectNone, new java.awt.GridBagConstraints());
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.weightx = 1.0;
         pnlControls.add(gluFiller, gridBagConstraints);
 
         setBackground(javax.swing.UIManager.getDefaults().getColor("List.background"));
         setLayout(new java.awt.GridBagLayout());
     } // </editor-fold>//GEN-END:initComponents
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void hypSelectAllActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_hypSelectAllActionPerformed
         for (final SearchTopic searchTopic : searchTopics) {
             searchTopic.setSelected(true);
         }
     }                                                                                //GEN-LAST:event_hypSelectAllActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void hypSelectNoneActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_hypSelectNoneActionPerformed
         for (final SearchTopic searchTopic : searchTopics) {
             searchTopic.setSelected(false);
         }
     }                                                                                 //GEN-LAST:event_hypSelectNoneActionPerformed
 }
