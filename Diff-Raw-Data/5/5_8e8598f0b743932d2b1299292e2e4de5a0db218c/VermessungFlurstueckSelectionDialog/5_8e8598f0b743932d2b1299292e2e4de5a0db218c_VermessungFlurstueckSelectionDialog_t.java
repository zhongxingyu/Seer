 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 /*
  * FlurstueckSelectionDialoge.java
  *
  * Created on 13.12.2010, 11:02:41
  */
 package de.cismet.cids.custom.objecteditors.wunda_blau;
 
 import Sirius.server.middleware.types.LightweightMetaObject;
 import Sirius.server.middleware.types.MetaObject;
 
 import org.openide.util.NbBundle;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.HeadlessException;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ExecutionException;
 
 import javax.swing.ComboBoxModel;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.DefaultListCellRenderer;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JTextField;
 import javax.swing.ListCellRenderer;
 import javax.swing.SwingWorker;
 
 import de.cismet.cids.custom.objectrenderer.utils.AlphanumComparator;
 import de.cismet.cids.custom.objectrenderer.utils.CidsBeanSupport;
 import de.cismet.cids.custom.objectrenderer.utils.ObjectRendererUtils;
 import de.cismet.cids.custom.objectrenderer.utils.VermessungFlurstueckFinder;
 
 import de.cismet.cids.dynamics.CidsBean;
 
 import de.cismet.tools.CismetThreadPool;
 
import de.cismet.tools.gui.StaticSwingTools;

 /**
  * DOCUMENT ME!
  *
  * @author   stefan
  * @version  $Revision$, $Date$
  */
 public class VermessungFlurstueckSelectionDialog extends javax.swing.JDialog {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(
             VermessungFlurstueckSelectionDialog.class);
     private static final ComboBoxModel WAIT_MODEL = new DefaultComboBoxModel(new String[] { "Wird geladen..." });
     private static final DefaultComboBoxModel NO_SELECTION_MODEL = new DefaultComboBoxModel(new Object[] {});
     private static final String CB_EDITED_ACTION_COMMAND = "comboBoxEdited";
 
     //~ Instance fields --------------------------------------------------------
 
     private List<CidsBean> currentListToAdd;
     private final boolean usedInEditor;
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton btnApply;
     private javax.swing.JButton btnCancel;
     private javax.swing.JButton btnOK;
     private javax.swing.JComboBox cboFlur;
     private javax.swing.JComboBox cboFlurstueck;
     private javax.swing.JComboBox cboGemarkung;
     private javax.swing.JComboBox cmbVeraenderungsart;
     private javax.swing.JLabel lblFlur;
     private javax.swing.JLabel lblFlurstueck;
     private javax.swing.JLabel lblGemarkung;
     private javax.swing.JLabel lblGemarkungsname;
     private javax.swing.JLabel lblVeraenderungsart;
     private javax.swing.JPanel pnlContainer;
     private javax.swing.JPanel pnlControls;
     private javax.swing.JSeparator sepControls;
     // End of variables declaration//GEN-END:variables
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new FlurstueckSelectionDialoge object.
      */
     public VermessungFlurstueckSelectionDialog() {
         this(true);
     }
 
     /**
      * Creates new form FlurstueckSelectionDialoge.
      *
      * @param  usedInEditor  DOCUMENT ME!
      */
     public VermessungFlurstueckSelectionDialog(final boolean usedInEditor) {
         this.usedInEditor = usedInEditor;
         setTitle("Bitte Flurstück auswählen");
         initComponents();
         setSize(419, 144);
 
         final ListCellRenderer lcr = new ListCellRenderer() {
 
                 DefaultListCellRenderer defaultListCellRenderer = new DefaultListCellRenderer();
 
                 @Override
                 public Component getListCellRendererComponent(final JList list,
                         final Object value,
                         final int index,
                         final boolean isSelected,
                         final boolean cellHasFocus) {
                     final JLabel result = (JLabel)defaultListCellRenderer.getListCellRendererComponent(
                             list,
                             value,
                             index,
                             isSelected,
                             cellHasFocus);
 
                     if (value instanceof LightweightMetaObject) {
                         final LightweightMetaObject metaObject = (LightweightMetaObject)value;
 
                         result.setText(
                             String.valueOf(metaObject.getLWAttribute(VermessungFlurstueckFinder.FLURSTUECK_GEMARKUNG))
                                     + " - "
                                     + String.valueOf(
                                         metaObject.getLWAttribute(VermessungFlurstueckFinder.GEMARKUNG_NAME)));
                     }
 
                     return result;
                 }
             };
 
         cboGemarkung.setRenderer(lcr);
 
         CismetThreadPool.execute(new AbstractFlurstueckComboModelWorker(cboGemarkung, true) {
 
                 @Override
                 protected ComboBoxModel doInBackground() throws Exception {
                     return new DefaultComboBoxModel(VermessungFlurstueckFinder.getLWGemarkungen());
                 }
 
                 @Override
                 protected void done() {
                     super.done();
                     cboGemarkung.setSelectedIndex(0);
 
                     cboGemarkung.requestFocusInWindow();
                     ObjectRendererUtils.selectAllTextInEditableCombobox(cboGemarkung);
                 }
             });
 
         CismetThreadPool.execute(new AbstractFlurstueckComboModelWorker(cmbVeraenderungsart, false) {
 
                 @Override
                 protected ComboBoxModel doInBackground() throws Exception {
                     final DefaultComboBoxModel result = new DefaultComboBoxModel(
                             VermessungFlurstueckFinder.getVeraenderungsarten());
 
                     if (!usedInEditor) {
                         result.insertElementAt("Alle", 0);
                     }
 
                     return result;
                 }
 
                 @Override
                 protected void done() {
                     super.done();
                     cmbVeraenderungsart.setSelectedIndex(0);
                 }
             });
     }
 
     //~ Methods ----------------------------------------------------------------
 
     @Override
     public void setVisible(final boolean b) {
         checkOkEnableState();
         super.setVisible(b);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  currentListToAdd  DOCUMENT ME!
      */
     public void setCurrentListToAdd(final List<CidsBean> currentListToAdd) {
         this.currentListToAdd = currentListToAdd;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public List<CidsBean> getCurrentListToAdd() {
         return currentListToAdd;
     }
 
     /**
      * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
      * content of this method is always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
         java.awt.GridBagConstraints gridBagConstraints;
 
         pnlContainer = new javax.swing.JPanel();
         cboGemarkung = new javax.swing.JComboBox();
         pnlControls = new javax.swing.JPanel();
         btnCancel = new javax.swing.JButton();
         btnOK = new javax.swing.JButton();
         btnApply = new javax.swing.JButton();
         cboFlur = new javax.swing.JComboBox(NO_SELECTION_MODEL);
         cboFlurstueck = new javax.swing.JComboBox(NO_SELECTION_MODEL);
         lblGemarkung = new javax.swing.JLabel();
         lblFlur = new javax.swing.JLabel();
         lblFlurstueck = new javax.swing.JLabel();
         lblGemarkungsname = new javax.swing.JLabel();
         sepControls = new javax.swing.JSeparator();
         lblVeraenderungsart = new javax.swing.JLabel();
         cmbVeraenderungsart = new javax.swing.JComboBox();
 
         setMinimumSize(new java.awt.Dimension(419, 154));
 
         pnlContainer.setMaximumSize(new java.awt.Dimension(250, 180));
         pnlContainer.setMinimumSize(new java.awt.Dimension(250, 180));
         pnlContainer.setPreferredSize(new java.awt.Dimension(250, 180));
         pnlContainer.setLayout(new java.awt.GridBagLayout());
 
         cboGemarkung.setEditable(true);
         cboGemarkung.setMaximumSize(new java.awt.Dimension(100, 18));
         cboGemarkung.setMinimumSize(new java.awt.Dimension(100, 18));
         cboGemarkung.setPreferredSize(new java.awt.Dimension(100, 18));
         cboGemarkung.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     cboGemarkungActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.weightx = 2.0;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         pnlContainer.add(cboGemarkung, gridBagConstraints);
 
         pnlControls.setLayout(new java.awt.GridBagLayout());
 
         btnCancel.setText("Abbrechen");
         btnCancel.setToolTipText("Eingaben nicht übernehmen und Dialog schliessen");
         btnCancel.setFocusPainted(false);
         btnCancel.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     btnCancelActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 2;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         pnlControls.add(btnCancel, gridBagConstraints);
 
         btnOK.setText("OK");
         btnOK.setToolTipText("Eingaben übernehmen und Dialog schliessen");
         btnOK.setFocusPainted(false);
         btnOK.setMaximumSize(new java.awt.Dimension(85, 23));
         btnOK.setMinimumSize(new java.awt.Dimension(85, 23));
         btnOK.setPreferredSize(new java.awt.Dimension(85, 23));
         btnOK.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     btnOKActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         pnlControls.add(btnOK, gridBagConstraints);
 
         btnApply.setText("Übernehmen");
         btnApply.setToolTipText("Eingaben übernehmen und Dialog geöffnet lassen");
         btnApply.setFocusPainted(false);
         btnApply.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     btnApplyActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         pnlControls.add(btnApply, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 6;
         gridBagConstraints.gridwidth = 4;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
         gridBagConstraints.weightx = 2.0;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         pnlContainer.add(pnlControls, gridBagConstraints);
 
         cboFlur.setEditable(true);
         cboFlur.setEnabled(false);
         cboFlur.setMaximumSize(new java.awt.Dimension(100, 18));
         cboFlur.setMinimumSize(new java.awt.Dimension(100, 18));
         cboFlur.setPreferredSize(new java.awt.Dimension(100, 18));
         cboFlur.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     cboFlurActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 0);
         pnlContainer.add(cboFlur, gridBagConstraints);
 
         cboFlurstueck.setEditable(true);
         cboFlurstueck.setEnabled(false);
         cboFlurstueck.setMaximumSize(new java.awt.Dimension(100, 18));
         cboFlurstueck.setMinimumSize(new java.awt.Dimension(100, 18));
         cboFlurstueck.setPreferredSize(new java.awt.Dimension(100, 18));
         cboFlurstueck.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     cboFlurstueckActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 2;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         pnlContainer.add(cboFlurstueck, gridBagConstraints);
 
         lblGemarkung.setText("Gemarkung");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.weightx = 2.0;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         pnlContainer.add(lblGemarkung, gridBagConstraints);
 
         lblFlur.setText("Flur");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         pnlContainer.add(lblFlur, gridBagConstraints);
 
         lblFlurstueck.setText("Flurstück");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 2;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         pnlContainer.add(lblFlurstueck, gridBagConstraints);
 
         lblGemarkungsname.setText(" ");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         pnlContainer.add(lblGemarkungsname, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 5;
         gridBagConstraints.gridwidth = 4;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
         pnlContainer.add(sepControls, gridBagConstraints);
 
         lblVeraenderungsart.setText("Veränderungsart");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 2;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         pnlContainer.add(lblVeraenderungsart, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 2;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         pnlContainer.add(cmbVeraenderungsart, gridBagConstraints);
 
         getContentPane().add(pnlContainer, java.awt.BorderLayout.CENTER);
     } // </editor-fold>//GEN-END:initComponents
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void cboGemarkungActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cboGemarkungActionPerformed
         final Object selection = cboGemarkung.getSelectedItem();
 
         cboFlurstueck.setEnabled(false);
         btnOK.setEnabled(false);
 
         if (selection instanceof LightweightMetaObject) {
             final LightweightMetaObject metaObject = (LightweightMetaObject)selection;
             final String gemarkung = String.valueOf(selection);
 
             CismetThreadPool.execute(new AbstractFlurstueckComboModelWorker(
                     cboFlur,
                     CB_EDITED_ACTION_COMMAND.equals(evt.getActionCommand())) {
 
                     @Override
                     protected ComboBoxModel doInBackground() throws Exception {
                         return new DefaultComboBoxModel(VermessungFlurstueckFinder.getLWFlure(gemarkung));
                     }
                 });
 
             final String gemarkungsname = String.valueOf(metaObject.getLWAttribute(
                         VermessungFlurstueckFinder.GEMARKUNG_NAME));
             lblGemarkungsname.setText("(" + gemarkungsname + ")");
             cboGemarkung.getEditor().getEditorComponent().setBackground(Color.WHITE);
         } else {
             final int foundBeanIndex = ObjectRendererUtils.findComboBoxItemForString(
                     cboGemarkung,
                     String.valueOf(selection));
 
             if (foundBeanIndex < 0) {
                 if (usedInEditor) {
                     cboFlur.setModel(new DefaultComboBoxModel());
 
                     try {
                         Integer.parseInt(String.valueOf(selection));
                         cboGemarkung.getEditor().getEditorComponent().setBackground(Color.YELLOW);
                         cboFlur.setEnabled(true);
                         if (CB_EDITED_ACTION_COMMAND.equals(evt.getActionCommand())) {
                             cboFlur.requestFocusInWindow();
                         }
                     } catch (NumberFormatException notANumberEx) {
                         if (LOG.isDebugEnabled()) {
                             LOG.debug(selection + " is not a number!", notANumberEx);
                         }
 
                         cboFlur.setEnabled(false);
                         cboGemarkung.getEditor().getEditorComponent().setBackground(Color.RED);
                         lblGemarkungsname.setText("(Ist keine Zahl)");
                     }
 
                     lblGemarkungsname.setText(" ");
                 } else {
                     cboGemarkung.getEditor().getEditorComponent().setBackground(Color.RED);
                     cboFlur.setEnabled(false);
                 }
             } else {
                 cboGemarkung.setSelectedIndex(foundBeanIndex);
                 cboFlur.getEditor().getEditorComponent().setBackground(Color.WHITE);
                 cboFlurstueck.getEditor().getEditorComponent().setBackground(Color.WHITE);
             }
         }
 
         checkOkEnableState();
     } //GEN-LAST:event_cboGemarkungActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnCancelActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnCancelActionPerformed
         setVisible(false);
         cancelHook();
     }                                                                             //GEN-LAST:event_btnCancelActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnOKActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnOKActionPerformed
         apply(false);
     }                                                                         //GEN-LAST:event_btnOKActionPerformed
 
     /**
      * DOCUMENT ME!
      */
     public void okHook() {
     }
 
     /**
      * DOCUMENT ME!
      */
     public void cancelHook() {
     }
 
     /**
      * DOCUMENT ME!
      */
     private void checkOkEnableState() {
         btnOK.setEnabled(cboFlurstueck.getSelectedItem() instanceof MetaObject);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void cboFlurActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cboFlurActionPerformed
         final Object selection = cboFlur.getSelectedItem();
 
         if (selection instanceof MetaObject) {
             final String gemarkung = String.valueOf(cboGemarkung.getSelectedItem());
             final StringBuffer flur = new StringBuffer(String.valueOf(cboFlur.getSelectedItem()));
 
             while (flur.length() < 3) {
                 flur.insert(0, 0);
             }
 
             btnOK.setEnabled(false);
             cboFlur.getEditor().getEditorComponent().setBackground(Color.WHITE);
 
             CismetThreadPool.execute(new AbstractFlurstueckComboModelWorker(
                     cboFlurstueck,
                     CB_EDITED_ACTION_COMMAND.equals(evt.getActionCommand())) {
 
                     @Override
                     protected ComboBoxModel doInBackground() throws Exception {
                         return new DefaultComboBoxModel(
                                 VermessungFlurstueckFinder.getLWFurstuecksZaehlerNenner(gemarkung, flur.toString()));
                     }
                 });
         } else {
             String userInput = String.valueOf(selection);
 
             while (userInput.length() < 3) {
                 userInput = "0" + userInput;
             }
 
             final int foundBeanIndex = ObjectRendererUtils.findComboBoxItemForString(cboFlur, userInput);
             if (foundBeanIndex < 0) {
                 if (usedInEditor) {
                     cboFlur.getEditor().getEditorComponent().setBackground(Color.YELLOW);
                     cboFlurstueck.setModel(new DefaultComboBoxModel());
                     cboFlurstueck.setEnabled(true);
 
                     if (CB_EDITED_ACTION_COMMAND.equals(evt.getActionCommand())) {
                         cboFlurstueck.requestFocusInWindow();
                         cboFlurstueck.setSelectedIndex(0);
                     }
                 } else {
                     cboFlur.getEditor().getEditorComponent().setBackground(Color.RED);
                     cboFlurstueck.setModel(new DefaultComboBoxModel());
                     cboFlurstueck.setEnabled(false);
                 }
             } else {
                 cboFlur.setSelectedIndex(foundBeanIndex);
                 cboFlurstueck.getEditor().getEditorComponent().setBackground(Color.WHITE);
             }
         }
 
         checkOkEnableState();
     } //GEN-LAST:event_cboFlurActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void cboFlurstueckActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cboFlurstueckActionPerformed
         btnOK.setEnabled(checkFlurstueckSelectionComplete());
 
         if (CB_EDITED_ACTION_COMMAND.equals(evt.getActionCommand())) {
             btnOK.requestFocusInWindow();
         }
 
         final Component editor = cboFlurstueck.getEditor().getEditorComponent();
 
         if (cboFlurstueck.getSelectedItem() instanceof MetaObject) {
             editor.setBackground(Color.WHITE);
         } else {
             String flurstueck = String.valueOf(cboFlurstueck.getSelectedItem());
             if (!flurstueck.contains("/")) {
                 flurstueck += "/0";
 
                 if (editor instanceof JTextField) {
                     ((JTextField)editor).setText(flurstueck);
                 }
             }
 
             final int foundBeanIndex = ObjectRendererUtils.findComboBoxItemForString(cboFlurstueck, flurstueck);
             if (foundBeanIndex < 0) {
                 if (usedInEditor) {
                     cboFlurstueck.getEditor().getEditorComponent().setBackground(Color.YELLOW);
                 } else {
                     cboFlurstueck.getEditor().getEditorComponent().setBackground(Color.RED);
                 }
             } else {
                 cboFlurstueck.setSelectedIndex(foundBeanIndex);
             }
         }
     } //GEN-LAST:event_cboFlurstueckActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnApplyActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnApplyActionPerformed
         apply(true);
     }                                                                            //GEN-LAST:event_btnApplyActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private boolean checkFlurstueckSelectionComplete() {
         if (cboFlur.isEnabled() && cboFlurstueck.isEnabled()) {
             final Object flur = cboFlur.getSelectedItem();
             final Object flurstueck = cboFlurstueck.getSelectedItem();
 
             if ((flur != null) && (flurstueck != null)) {
                 if (usedInEditor || (flurstueck instanceof MetaObject)) {
                     if ((flur.toString().length() > 0) && (flurstueck.toString().length() > 0)) {
                         return true;
                     }
                 }
             }
         }
 
         return false;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   zaehlerNenner  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private CidsBean landParcelBeanFromComboBoxes(final String zaehlerNenner) {
         int result = JOptionPane.YES_OPTION;
 
         try {
             final Map<String, Object> newLandParcelProperties = new HashMap<String, Object>();
             final String gemarkung = String.valueOf(cboGemarkung.getSelectedItem());
             final String flur = String.valueOf(cboFlur.getSelectedItem());
 
             if (flur.length() != 3) {
                 result = JOptionPane.showConfirmDialog(
                        StaticSwingTools.getParentFrame(this),
                         "Das neue Flurstück entspricht nicht der Namenskonvention: Flur sollte dreistellig sein (mit führenden Nullen, z.B. 007). Datensatz trotzdem abspeichern?",
                         "Warnung: Format",
                         JOptionPane.YES_NO_OPTION,
                         JOptionPane.WARNING_MESSAGE);
             }
 
             if (result == JOptionPane.YES_OPTION) {
                 final String[] zaehlerNennerParts = zaehlerNenner.split("/");
                 final String zaehler = zaehlerNennerParts[0];
                 String nenner = "0";
                 if (zaehlerNennerParts.length == 2) {
                     nenner = zaehlerNennerParts[1];
                 }
 
                 final MetaObject gemarkungMetaObject = VermessungFlurstueckFinder.getLWGemarkung(Integer.valueOf(
                             gemarkung));
                 if ((gemarkungMetaObject != null) && (gemarkungMetaObject.getBean() != null)) {
                     newLandParcelProperties.put(
                         VermessungFlurstueckFinder.FLURSTUECK_GEMARKUNG,
                         gemarkungMetaObject.getBean());
                 } else {
                     LOG.error("Gemarkung '" + gemarkung
                                 + "' could not be found in teh cids system. Can't add this flurstueck.");
                     return null;
                 }
 
                 newLandParcelProperties.put(VermessungFlurstueckFinder.FLURSTUECK_FLUR, flur);
                 newLandParcelProperties.put(VermessungFlurstueckFinder.FLURSTUECK_ZAEHLER, zaehler);
                 newLandParcelProperties.put(VermessungFlurstueckFinder.FLURSTUECK_NENNER, nenner);
 
                 // the following code tries to avoid the creation of multiple entries for the same landparcel. however,
                 // there *might* be a chance that a historic landparcel is created multiple times when more then one
                 // client creates the same parcel at the "same time".
                 final MetaObject[] searchResult = VermessungFlurstueckFinder.getLWLandparcel(
                         gemarkung,
                         flur,
                         zaehler,
                         nenner);
                 if ((searchResult != null) && (searchResult.length > 0)) {
                     return searchResult[0].getBean();
                 } else {
                     return CidsBeanSupport.createNewCidsBeanFromTableName(
                             VermessungFlurstueckFinder.FLURSTUECK_KICKER_TABLE_NAME,
                             newLandParcelProperties);
                 }
             }
         } catch (Exception ex) {
             LOG.error("Could not find or create the landparcel corresponding to user's input.", ex);
         }
 
         return null;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   visible  DOCUMENT ME!
      *
      * @throws  HeadlessException  DOCUMENT ME!
      */
     private void apply(final boolean visible) throws HeadlessException {
         final Object flurstueck = cboFlurstueck.getSelectedItem();
         final Object veraenderungsart = cmbVeraenderungsart.getSelectedItem();
 
         CidsBean flurstueckBean = null;
         if (flurstueck instanceof LightweightMetaObject) {
             flurstueckBean = ((LightweightMetaObject)flurstueck).getBean();
         } else if ((flurstueck instanceof String) && usedInEditor) {
             final int result = JOptionPane.showConfirmDialog(
                     this,
                     "Das Flurstück befindet sich nicht im Datenbestand der aktuellen Flurstücke. Soll es als historisch angelegt werden?",
                     "Historisches Flurstück anlegen",
                     JOptionPane.YES_NO_OPTION);
 
             if (result == JOptionPane.YES_OPTION) {
                 flurstueckBean = landParcelBeanFromComboBoxes(flurstueck.toString());
 
                 if (MetaObject.NEW == flurstueckBean.getMetaObject().getStatus()) {
                     try {
                         flurstueckBean = flurstueckBean.persist();
                     } catch (Exception ex) {
                         LOG.error("Could not persist new flurstueck.", ex);
                         flurstueckBean = null;
                     }
                 }
             }
         }
 
         CidsBean veraenderungsartBean = null;
         if (veraenderungsart instanceof LightweightMetaObject) {
             veraenderungsartBean = ((LightweightMetaObject)veraenderungsart).getBean();
         }
 
         CidsBean flurstuecksvermessung = null;
         // If the dialog is not used in the editor - thus is used in the window search - it's OK to have a
         // veraenderungsartBean which is null
         if ((flurstueckBean != null) && (!usedInEditor || (veraenderungsartBean != null))) {
             final Map<String, Object> properties = new HashMap<String, Object>();
             properties.put(VermessungFlurstueckFinder.VERMESSUNG_FLURSTUECKSVERMESSUNG_FLURSTUECK, flurstueckBean);
             if (veraenderungsartBean != null) {
                 properties.put(
                     VermessungFlurstueckFinder.VERMESSUNG_FLURSTUECKSVERMESSUNG_VERMESSUNGSART,
                     veraenderungsartBean);
             }
 
             try {
                 flurstuecksvermessung = CidsBeanSupport.createNewCidsBeanFromTableName(
                         VermessungFlurstueckFinder.VERMESSUNG_FLURSTUECKSVERMESSUNG_TABLE_NAME,
                         properties);
             } catch (Exception ex) {
                 LOG.error("Could not add new flurstueck or flurstuecksvermessung.", ex);
             }
         }
 
         if ((flurstuecksvermessung != null) && (currentListToAdd != null)) {
             final int position = Collections.binarySearch(
                     currentListToAdd,
                     flurstuecksvermessung,
                     AlphanumComparator.getInstance());
 
             if (position < 0) {
                 currentListToAdd.add(-position - 1, flurstuecksvermessung);
             } else {
                 JOptionPane.showMessageDialog(
                     this,
                     NbBundle.getMessage(
                         VermessungFlurstueckSelectionDialog.class,
                         "VermessungFlurstueckSelectionDialog.apply(boolean).itemAlreadyExists.message"),
                     NbBundle.getMessage(
                         VermessungFlurstueckSelectionDialog.class,
                         "VermessungFlurstueckSelectionDialog.apply(boolean).itemAlreadyExists.title"),
                     JOptionPane.WARNING_MESSAGE);
                 return;
             }
         }
 
         okHook();
 
         setVisible(visible);
     }
 
     //~ Inner Classes ----------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     abstract class AbstractFlurstueckComboModelWorker extends SwingWorker<ComboBoxModel, Void> {
 
         //~ Instance fields ----------------------------------------------------
 
         private final JComboBox box;
         private final boolean switchToBox;
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new AbstractFlurstueckComboModelWorker object.
          *
          * @param  box          DOCUMENT ME!
          * @param  switchToBox  DOCUMENT ME!
          */
         public AbstractFlurstueckComboModelWorker(final JComboBox box, final boolean switchToBox) {
             this.box = box;
             this.switchToBox = switchToBox;
             box.setVisible(true);
             box.setEnabled(false);
             box.setModel(WAIT_MODEL);
         }
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         protected void done() {
             try {
                 box.setModel(get());
                 if (switchToBox) {
                     box.requestFocus();
                 }
             } catch (InterruptedException ex) {
                 if (LOG.isDebugEnabled()) {
                     LOG.debug("There was an interruption while loading the values.", ex);
                 }
             } catch (ExecutionException ex) {
                 LOG.error("An error occurred while loading the values.", ex);
             } finally {
                 box.setEnabled(true);
                 box.setSelectedIndex(0);
                 ObjectRendererUtils.selectAllTextInEditableCombobox(box);
 
                 checkOkEnableState();
             }
         }
     }
 }
