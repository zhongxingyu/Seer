 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 /*
  *  Copyright (C) 2010 thorsten
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 /*
  * DetailPanel.java
  *
  * Created on 24.11.2010, 20:42:43
  */
 package de.cismet.verdis.gui;
 
 import Sirius.server.middleware.types.MetaObject;
 
 import com.vividsolutions.jts.geom.Geometry;
 
 import edu.umd.cs.piccolo.PCanvas;
 
 import org.jdesktop.beansbinding.Converter;
 
 import org.openide.util.Exceptions;
 
 import java.awt.Component;
 import java.awt.event.ActionEvent;
 
 import java.util.Collection;
 import java.util.concurrent.ExecutionException;
 import java.util.regex.Pattern;
 
 import javax.swing.*;
 import javax.swing.event.HyperlinkEvent;
 import javax.swing.event.HyperlinkListener;
 
 import de.cismet.cids.custom.util.BindingValidationSupport;
 
 import de.cismet.cids.dynamics.CidsBean;
 import de.cismet.cids.dynamics.CidsBeanStore;
 
 import de.cismet.cids.editors.DefaultBindableReferenceCombo;
 import de.cismet.cids.editors.converters.SqlDateToStringConverter;
 
 import de.cismet.tools.CismetThreadPool;
 
import de.cismet.tools.gui.StaticSwingTools;

 import de.cismet.validation.Validator;
 import de.cismet.validation.ValidatorHelper;
 import de.cismet.validation.ValidatorState;
 import de.cismet.validation.ValidatorStateImpl;
 
 import de.cismet.validation.display.EmbeddedValidatorDisplay;
 
 import de.cismet.validation.validator.CidsBeanValidator;
 
 import de.cismet.verdis.CidsAppBackend;
 import de.cismet.verdis.EditModeListener;
 
 import de.cismet.verdis.commons.constants.FrontPropertyConstants;
 import de.cismet.verdis.commons.constants.FrontinfoPropertyConstants;
 import de.cismet.verdis.commons.constants.GeomPropertyConstants;
 import de.cismet.verdis.commons.constants.StrassePropertyConstants;
 import de.cismet.verdis.commons.constants.VerdisMetaClassConstants;
 
 /**
  * DOCUMENT ME!
  *
  * @author   thorsten
  * @version  $Revision$, $Date$
  */
 public class WDSRDetailsPanel extends javax.swing.JPanel implements CidsBeanStore, EditModeListener, HyperlinkListener {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final transient org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(
             WDSRDetailsPanel.class);
     private static final ComboBoxModel WAIT_MODEL = new DefaultComboBoxModel(new String[] { "Wird geladen..." });
     private static final ListCellRenderer WAIT_RENDERER = new DefaultListCellRenderer() {
 
             @Override
             public Component getListCellRendererComponent(final JList list,
                     final Object value,
                     final int index,
                     final boolean isSelected,
                     final boolean cellHasFocus) {
                 super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                 if (list.getModel() == WAIT_MODEL) {
                     setText(list.getModel().getElementAt(0).toString());
                 }
                 return this;
             }
         };
 
     //~ Instance fields --------------------------------------------------------
 
     private CidsBean frontBean = null;
     private final Validator bindingValidator;
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private de.cismet.cismap.commons.gui.SimpleBackgroundedJPanel bpanWdsrDetails;
     private javax.swing.JComboBox cboLageSR;
     private javax.swing.JComboBox cboLageWD;
     private javax.swing.JComboBox cboSR;
     private javax.swing.JComboBox cboSRFromLage;
     private javax.swing.JComboBox cboStrasse;
     private javax.swing.JComboBox cboWD;
     private javax.swing.JComboBox cboWDFromLage;
     private javax.swing.JEditorPane edtQuer;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JLabel jLabel2;
     private javax.swing.JLabel jLabel5;
     private javax.swing.JPanel jPanel1;
     private javax.swing.JSeparator jSeparator1;
     private javax.swing.JSeparator jSeparator2;
     private javax.swing.JSeparator jSeparator3;
     private javax.swing.JLabel labLageSR;
     private javax.swing.JLabel labLageWD;
     private javax.swing.JLabel lblBemSR;
     private javax.swing.JLabel lblBemWD;
     private javax.swing.JLabel lblErfassungsdatumDesc;
     private javax.swing.JLabel lblErfassungsdatumDesc1;
     private javax.swing.JLabel lblLaengeGR;
     private javax.swing.JLabel lblLaengeKorr;
     private javax.swing.JLabel lblLastEditorDescr;
     private javax.swing.JLabel lblNummer;
     private javax.swing.JLabel lblQuerverweise;
     private javax.swing.JLabel lblVeranlagungWD;
     private javax.swing.JScrollPane scpBemSR;
     private javax.swing.JScrollPane scpBemWD;
     private javax.swing.JScrollPane scpQuer;
     private javax.swing.JTextField txtBearbeitetDurch;
     private javax.swing.JTextArea txtBemSR;
     private javax.swing.JTextArea txtBemWD;
     private javax.swing.JTextField txtErfassungsdatum;
     private javax.swing.JTextField txtLaengeGrafik;
     private javax.swing.JTextField txtLaengeKorrektur;
     private javax.swing.JTextField txtNummer;
     private javax.swing.JTextField txtVeranlagungSR;
     private javax.swing.JTextField txtVeranlagungWD;
     private org.jdesktop.beansbinding.BindingGroup bindingGroup;
     // End of variables declaration//GEN-END:variables
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates new form DetailPanel.
      */
     public WDSRDetailsPanel() {
         initComponents();
 
         bindingValidator = BindingValidationSupport.attachBindingValidationToAllTargets(bindingGroup);
 
         cboStrasse.setModel(WAIT_MODEL);
         cboStrasse.setRenderer(WAIT_RENDERER);
         cboLageSR.setModel(WAIT_MODEL);
         cboLageSR.setRenderer(WAIT_RENDERER);
         cboLageWD.setModel(WAIT_MODEL);
         cboLageWD.setRenderer(WAIT_RENDERER);
 
         ((DefaultBindableReferenceCombo)cboStrasse).setMetaClass(CidsAppBackend.getInstance().getVerdisMetaClass(
                 VerdisMetaClassConstants.MC_STRASSE));
         ((DefaultBindableReferenceCombo)cboSR).setMetaClass(CidsAppBackend.getInstance().getVerdisMetaClass(
                 VerdisMetaClassConstants.MC_STRASSENREINIGUNG));
         ((DefaultBindableReferenceCombo)cboWD).setMetaClass(CidsAppBackend.getInstance().getVerdisMetaClass(
                 VerdisMetaClassConstants.MC_WINTERDIENST));
 
         cboLageSR.setRenderer(new DefaultListCellRenderer() {
 
                 @Override
                 public Component getListCellRendererComponent(final JList list,
                         final Object value,
                         final int index,
                         final boolean isSelected,
                         final boolean cellHasFocus) {
                     super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                     if (value == null) {
                         setText("manuelle Auswahl der Straßenreinigung");
                     } else if (value instanceof CidsBean) {
                         final CidsBean bean = (CidsBean)value;
                         final String strasse = (String)bean.getProperty("strasse.name");
                         final String bem = (String)bean.getProperty("sr_bem");
                         final String key = (String)bean.getProperty("sr_klasse.key");
                         setText(((bem == null) ? strasse : bem) + " (" + key + ")");
                     }
                     return this;
                 }
             });
         cboLageWD.setRenderer(new DefaultListCellRenderer() {
 
                 @Override
                 public Component getListCellRendererComponent(final JList list,
                         final Object value,
                         final int index,
                         final boolean isSelected,
                         final boolean cellHasFocus) {
                     super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                     if (value == null) {
                         setText("manuelle Auswahl des Winterdienstes");
                     } else if (value instanceof CidsBean) {
                         final CidsBean bean = (CidsBean)value;
                         final String strasse = (String)bean.getProperty("strasse.name");
                         final String bem = (String)bean.getProperty("wd_bem");
                         final String key = (String)bean.getProperty("wd_prio.key");
                         setText(((bem == null) ? strasse : bem) + " (" + key + ")");
                     }
                     return this;
                 }
             });
 
         cboSR.setRenderer(new DefaultListCellRenderer() {
 
                 @Override
                 public Component getListCellRendererComponent(final JList list,
                         final Object value,
                         final int index,
                         final boolean isSelected,
                         final boolean cellHasFocus) {
                     super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                     if (value instanceof CidsBean) {
                         setText((String)((CidsBean)value).getProperty("name"));
                     }
                     return this;
                 }
             });
 
         cboWD.setRenderer(new DefaultListCellRenderer() {
 
                 @Override
                 public Component getListCellRendererComponent(final JList list,
                         final Object value,
                         final int index,
                         final boolean isSelected,
                         final boolean cellHasFocus) {
                     super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                     if (value instanceof CidsBean) {
                         setText((String)((CidsBean)value).getProperty("name"));
                     }
                     return this;
                 }
             });
 
        StaticSwingTools.decorateWithFixedAutoCompleteDecorator(cboStrasse);
        final JTextField txt = (JTextField)cboStrasse.getEditor().getEditorComponent();
        txt.setOpaque(false);
         edtQuer.addHyperlinkListener(this);
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public Validator getValidator() {
         // nur BindingValidator notwendig, der TabellenValidator validiert schon alle beans
         return bindingValidator;
     }
 
     /**
      * DOCUMENT ME!
      */
     private void attachBeanValidators() {
         ValidatorHelper.removeAllNoBindingValidatorFromDisplay(txtNummer);
         getValidatorNummer(frontBean).attachDisplay(EmbeddedValidatorDisplay.getEmbeddedDisplayFor(txtNummer));
         ValidatorHelper.removeAllNoBindingValidatorFromDisplay(txtLaengeGrafik);
         getValidatorLaengeGrafik(frontBean).attachDisplay(EmbeddedValidatorDisplay.getEmbeddedDisplayFor(
                 txtLaengeGrafik));
         ValidatorHelper.removeAllNoBindingValidatorFromDisplay(txtLaengeKorrektur);
         getValidatorLaengeKorrektur(frontBean).attachDisplay(EmbeddedValidatorDisplay.getEmbeddedDisplayFor(
                 txtLaengeKorrektur));
         ValidatorHelper.removeAllNoBindingValidatorFromDisplay(txtErfassungsdatum);
         getValidatorDatumErfassung(frontBean).attachDisplay(EmbeddedValidatorDisplay.getEmbeddedDisplayFor(
                 txtErfassungsdatum));
         ValidatorHelper.removeAllNoBindingValidatorFromDisplay(txtVeranlagungWD);
         getValidatorVeranlagungWD(frontBean).attachDisplay(EmbeddedValidatorDisplay.getEmbeddedDisplayFor(
                 txtVeranlagungWD));
         ValidatorHelper.removeAllNoBindingValidatorFromDisplay(txtVeranlagungSR);
         getValidatorVeranlagungSR(frontBean).attachDisplay(EmbeddedValidatorDisplay.getEmbeddedDisplayFor(
                 txtVeranlagungSR));
     }
 
     /**
      * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
      * content of this method is always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
         java.awt.GridBagConstraints gridBagConstraints;
         bindingGroup = new org.jdesktop.beansbinding.BindingGroup();
 
         bpanWdsrDetails = new de.cismet.cismap.commons.gui.SimpleBackgroundedJPanel();
         lblNummer = new javax.swing.JLabel();
         txtNummer = new javax.swing.JTextField();
         lblLaengeGR = new javax.swing.JLabel();
         txtLaengeGrafik = new javax.swing.JTextField();
         lblLaengeKorr = new javax.swing.JLabel();
         txtLaengeKorrektur = new javax.swing.JTextField();
         lblLastEditorDescr = new javax.swing.JLabel();
         cboStrasse = new DefaultBindableReferenceCombo();
         cboSR = new DefaultBindableReferenceCombo();
         lblErfassungsdatumDesc = new javax.swing.JLabel();
         scpBemSR = new javax.swing.JScrollPane();
         txtBemSR = new javax.swing.JTextArea();
         lblBemSR = new javax.swing.JLabel();
         lblBemWD = new javax.swing.JLabel();
         scpBemWD = new javax.swing.JScrollPane();
         txtBemWD = new javax.swing.JTextArea();
         cboWD = new DefaultBindableReferenceCombo();
         jSeparator1 = new javax.swing.JSeparator();
         jSeparator2 = new javax.swing.JSeparator();
         txtBearbeitetDurch = new javax.swing.JTextField();
         txtErfassungsdatum = new javax.swing.JTextField();
         lblErfassungsdatumDesc1 = new javax.swing.JLabel();
         txtVeranlagungSR = new javax.swing.JTextField();
         lblVeranlagungWD = new javax.swing.JLabel();
         txtVeranlagungWD = new javax.swing.JTextField();
         jLabel1 = new javax.swing.JLabel();
         jLabel2 = new javax.swing.JLabel();
         labLageSR = new javax.swing.JLabel();
         cboLageWD = new javax.swing.JComboBox();
         cboLageSR = new javax.swing.JComboBox();
         labLageWD = new javax.swing.JLabel();
         jLabel5 = new javax.swing.JLabel();
         cboWDFromLage = new javax.swing.JComboBox();
         cboSRFromLage = new javax.swing.JComboBox();
         lblQuerverweise = new javax.swing.JLabel();
         scpQuer = new javax.swing.JScrollPane();
         edtQuer = new javax.swing.JEditorPane();
         jSeparator3 = new javax.swing.JSeparator();
         jPanel1 = new javax.swing.JPanel();
 
         setLayout(new java.awt.BorderLayout());
 
         bpanWdsrDetails.setOpaque(false);
         bpanWdsrDetails.setLayout(new java.awt.GridBagLayout());
 
         lblNummer.setText(org.openide.util.NbBundle.getMessage(
                 WDSRDetailsPanel.class,
                 "WDSRDetailsPanel.lblNummer.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(2, 7, 2, 2);
         bpanWdsrDetails.add(lblNummer, gridBagConstraints);
 
         org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 this,
                 org.jdesktop.beansbinding.ELProperty.create("${cidsBean.nummer}"),
                 txtNummer,
                 org.jdesktop.beansbinding.BeanProperty.create("text"),
                 FrontPropertyConstants.PROP__NUMMER);
         binding.setSourceNullValue("");
         binding.setSourceUnreadableValue("");
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
         bpanWdsrDetails.add(txtNummer, gridBagConstraints);
 
         lblLaengeGR.setText(org.openide.util.NbBundle.getMessage(
                 WDSRDetailsPanel.class,
                 "WDSRDetailsPanel.lblLaengeGR.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(2, 7, 2, 2);
         bpanWdsrDetails.add(lblLaengeGR, gridBagConstraints);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 this,
                 org.jdesktop.beansbinding.ELProperty.create("${cidsBean.frontinfo.laenge_grafik}"),
                 txtLaengeGrafik,
                 org.jdesktop.beansbinding.BeanProperty.create("text"),
                 FrontPropertyConstants.PROP__FRONTINFO
                         + "."
                         + FrontinfoPropertyConstants.PROP__LAENGE_GRAFIK);
         binding.setSourceNullValue("");
         binding.setSourceUnreadableValue("");
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
         bpanWdsrDetails.add(txtLaengeGrafik, gridBagConstraints);
 
         lblLaengeKorr.setText(org.openide.util.NbBundle.getMessage(
                 WDSRDetailsPanel.class,
                 "WDSRDetailsPanel.lblLaengeKorr.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(2, 7, 2, 2);
         bpanWdsrDetails.add(lblLaengeKorr, gridBagConstraints);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 this,
                 org.jdesktop.beansbinding.ELProperty.create("${cidsBean.frontinfo.laenge_korrektur}"),
                 txtLaengeKorrektur,
                 org.jdesktop.beansbinding.BeanProperty.create("text"),
                 FrontPropertyConstants.PROP__FRONTINFO
                         + "."
                         + FrontinfoPropertyConstants.PROP__LAENGE_KORREKTUR);
         binding.setSourceNullValue("");
         binding.setSourceUnreadableValue("");
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
         bpanWdsrDetails.add(txtLaengeKorrektur, gridBagConstraints);
 
         lblLastEditorDescr.setText(org.openide.util.NbBundle.getMessage(
                 WDSRDetailsPanel.class,
                 "WDSRDetailsPanel.lblLastEditorDescr.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(2, 7, 2, 2);
         bpanWdsrDetails.add(lblLastEditorDescr, gridBagConstraints);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 this,
                 org.jdesktop.beansbinding.ELProperty.create("${cidsBean.frontinfo.strasse}"),
                 cboStrasse,
                 org.jdesktop.beansbinding.BeanProperty.create("selectedItem"));
         binding.setSourceNullValue(null);
         binding.setSourceUnreadableValue(null);
         bindingGroup.addBinding(binding);
 
         cboStrasse.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     cboStrasseActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 5;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
         bpanWdsrDetails.add(cboStrasse, gridBagConstraints);
 
         cboSR.setToolTipText(org.openide.util.NbBundle.getMessage(
                 WDSRDetailsPanel.class,
                 "WDSRDetailsPanel.cboSR.toolTipText")); // NOI18N
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 this,
                 org.jdesktop.beansbinding.ELProperty.create("${cidsBean.frontinfo.sr_klasse_or}"),
                 cboSR,
                 org.jdesktop.beansbinding.BeanProperty.create("selectedItem"));
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 8;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
         bpanWdsrDetails.add(cboSR, gridBagConstraints);
 
         lblErfassungsdatumDesc.setText(org.openide.util.NbBundle.getMessage(
                 WDSRDetailsPanel.class,
                 "WDSRDetailsPanel.lblErfassungsdatumDesc.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(2, 7, 2, 2);
         bpanWdsrDetails.add(lblErfassungsdatumDesc, gridBagConstraints);
 
         txtBemSR.setColumns(20);
         txtBemSR.setLineWrap(true);
         txtBemSR.setRows(2);
         txtBemSR.setMinimumSize(new java.awt.Dimension(240, 200));
         txtBemSR.setPreferredSize(new java.awt.Dimension(21, 756));
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 this,
                 org.jdesktop.beansbinding.ELProperty.create("${cidsBean.frontinfo.sr_bem}"),
                 txtBemSR,
                 org.jdesktop.beansbinding.BeanProperty.create("text"));
         binding.setSourceNullValue("");
         binding.setSourceUnreadableValue("");
         bindingGroup.addBinding(binding);
 
         scpBemSR.setViewportView(txtBemSR);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 9;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
         bpanWdsrDetails.add(scpBemSR, gridBagConstraints);
 
         lblBemSR.setText(org.openide.util.NbBundle.getMessage(
                 WDSRDetailsPanel.class,
                 "WDSRDetailsPanel.lblBemSR.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 9;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(2, 7, 2, 2);
         bpanWdsrDetails.add(lblBemSR, gridBagConstraints);
 
         lblBemWD.setText(org.openide.util.NbBundle.getMessage(
                 WDSRDetailsPanel.class,
                 "WDSRDetailsPanel.lblBemWD.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 14;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(2, 7, 2, 2);
         bpanWdsrDetails.add(lblBemWD, gridBagConstraints);
 
         txtBemWD.setColumns(20);
         txtBemWD.setLineWrap(true);
         txtBemWD.setRows(2);
         txtBemWD.setMinimumSize(new java.awt.Dimension(240, 200));
         txtBemWD.setPreferredSize(new java.awt.Dimension(21, 756));
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 this,
                 org.jdesktop.beansbinding.ELProperty.create("${cidsBean.frontinfo.wd_bem}"),
                 txtBemWD,
                 org.jdesktop.beansbinding.BeanProperty.create("text"));
         binding.setSourceNullValue("");
         binding.setSourceUnreadableValue("");
         bindingGroup.addBinding(binding);
 
         scpBemWD.setViewportView(txtBemWD);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 14;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
         bpanWdsrDetails.add(scpBemWD, gridBagConstraints);
 
         cboWD.setToolTipText(org.openide.util.NbBundle.getMessage(
                 WDSRDetailsPanel.class,
                 "WDSRDetailsPanel.cboWD.toolTipText")); // NOI18N
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 this,
                 org.jdesktop.beansbinding.ELProperty.create("${cidsBean.frontinfo.wd_prio_or}"),
                 cboWD,
                 org.jdesktop.beansbinding.BeanProperty.create("selectedItem"));
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 13;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
         bpanWdsrDetails.add(cboWD, gridBagConstraints);
 
         jSeparator1.setMinimumSize(new java.awt.Dimension(0, 10));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 6;
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
         bpanWdsrDetails.add(jSeparator1, gridBagConstraints);
 
         jSeparator2.setMinimumSize(new java.awt.Dimension(0, 10));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 11;
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
         bpanWdsrDetails.add(jSeparator2, gridBagConstraints);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 this,
                 org.jdesktop.beansbinding.ELProperty.create("${cidsBean.bearbeitet_durch}"),
                 txtBearbeitetDurch,
                 org.jdesktop.beansbinding.BeanProperty.create("text"),
                 FrontPropertyConstants.PROP__BEARBEITET_DURCH);
         binding.setSourceNullValue("");
         binding.setSourceUnreadableValue("");
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
         bpanWdsrDetails.add(txtBearbeitetDurch, gridBagConstraints);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 this,
                 org.jdesktop.beansbinding.ELProperty.create("${cidsBean.erfassungsdatum}"),
                 txtErfassungsdatum,
                 org.jdesktop.beansbinding.BeanProperty.create("text"),
                 FrontPropertyConstants.PROP__ERFASSUNGSDATUM);
         binding.setSourceNullValue("");
         binding.setSourceUnreadableValue("");
         binding.setConverter(new SqlDateToStringConverter());
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
         bpanWdsrDetails.add(txtErfassungsdatum, gridBagConstraints);
 
         lblErfassungsdatumDesc1.setText(org.openide.util.NbBundle.getMessage(
                 WDSRDetailsPanel.class,
                 "WDSRDetailsPanel.lblErfassungsdatumDesc1.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 10;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(2, 7, 2, 2);
         bpanWdsrDetails.add(lblErfassungsdatumDesc1, gridBagConstraints);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 this,
                 org.jdesktop.beansbinding.ELProperty.create("${cidsBean.frontinfo.sr_veranlagung}"),
                 txtVeranlagungSR,
                 org.jdesktop.beansbinding.BeanProperty.create("text"),
                 FrontinfoPropertyConstants.PROP__SR_VERANLAGUNG);
         binding.setSourceNullValue("");
         binding.setSourceUnreadableValue("");
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 10;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
         bpanWdsrDetails.add(txtVeranlagungSR, gridBagConstraints);
 
         lblVeranlagungWD.setText(org.openide.util.NbBundle.getMessage(
                 WDSRDetailsPanel.class,
                 "WDSRDetailsPanel.lblVeranlagungWD.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 15;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(2, 7, 2, 2);
         bpanWdsrDetails.add(lblVeranlagungWD, gridBagConstraints);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 this,
                 org.jdesktop.beansbinding.ELProperty.create("${cidsBean.frontinfo.wd_veranlagung}"),
                 txtVeranlagungWD,
                 org.jdesktop.beansbinding.BeanProperty.create("text"),
                 FrontinfoPropertyConstants.PROP__WD_VERANLAGUNG);
         binding.setSourceNullValue("");
         binding.setSourceUnreadableValue("");
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 15;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
         bpanWdsrDetails.add(txtVeranlagungWD, gridBagConstraints);
 
         jLabel1.setText(org.openide.util.NbBundle.getMessage(WDSRDetailsPanel.class, "WDSRDetailsPanel.jLabel1.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 8;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(2, 7, 2, 2);
         bpanWdsrDetails.add(jLabel1, gridBagConstraints);
 
         jLabel2.setText(org.openide.util.NbBundle.getMessage(WDSRDetailsPanel.class, "WDSRDetailsPanel.jLabel2.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 13;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(2, 7, 2, 2);
         bpanWdsrDetails.add(jLabel2, gridBagConstraints);
 
         labLageSR.setText(org.openide.util.NbBundle.getMessage(
                 WDSRDetailsPanel.class,
                 "WDSRDetailsPanel.labLageSR.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 7;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(2, 7, 2, 2);
         bpanWdsrDetails.add(labLageSR, gridBagConstraints);
 
         cboLageWD.setToolTipText(org.openide.util.NbBundle.getMessage(
                 WDSRDetailsPanel.class,
                 "WDSRDetailsPanel.cboLageWD.toolTipText")); // NOI18N
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 this,
                 org.jdesktop.beansbinding.ELProperty.create("${cidsBean.frontinfo.lage_wd}"),
                 cboLageWD,
                 org.jdesktop.beansbinding.BeanProperty.create("selectedItem"));
         bindingGroup.addBinding(binding);
 
         cboLageWD.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     cboLageWDActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 12;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
         bpanWdsrDetails.add(cboLageWD, gridBagConstraints);
 
         cboLageSR.setToolTipText(org.openide.util.NbBundle.getMessage(
                 WDSRDetailsPanel.class,
                 "WDSRDetailsPanel.cboLageSR.toolTipText")); // NOI18N
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 this,
                 org.jdesktop.beansbinding.ELProperty.create("${cidsBean.frontinfo.lage_sr}"),
                 cboLageSR,
                 org.jdesktop.beansbinding.BeanProperty.create("selectedItem"));
         bindingGroup.addBinding(binding);
 
         cboLageSR.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     cboLageSRActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 7;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
         bpanWdsrDetails.add(cboLageSR, gridBagConstraints);
 
         labLageWD.setText(org.openide.util.NbBundle.getMessage(
                 WDSRDetailsPanel.class,
                 "WDSRDetailsPanel.labLageWD.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 12;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(2, 7, 2, 2);
         bpanWdsrDetails.add(labLageWD, gridBagConstraints);
 
         jLabel5.setText(org.openide.util.NbBundle.getMessage(WDSRDetailsPanel.class, "WDSRDetailsPanel.jLabel5.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 5;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(2, 7, 2, 2);
         bpanWdsrDetails.add(jLabel5, gridBagConstraints);
 
         cboWDFromLage.setToolTipText(org.openide.util.NbBundle.getMessage(
                 WDSRDetailsPanel.class,
                 "WDSRDetailsPanel.cboWDFromLage.toolTipText")); // NOI18N
         cboWDFromLage.setEnabled(false);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 13;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
         bpanWdsrDetails.add(cboWDFromLage, gridBagConstraints);
         cboWDFromLage.setVisible(false);
 
         cboSRFromLage.setToolTipText(org.openide.util.NbBundle.getMessage(
                 WDSRDetailsPanel.class,
                 "WDSRDetailsPanel.cboSRFromLage.toolTipText")); // NOI18N
         cboSRFromLage.setEnabled(false);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 8;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
         bpanWdsrDetails.add(cboSRFromLage, gridBagConstraints);
         cboSRFromLage.setVisible(false);
 
         lblQuerverweise.setText(org.openide.util.NbBundle.getMessage(
                 WDSRDetailsPanel.class,
                 "WDSRDetailsPanel.lblQuerverweise.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 17;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(2, 8, 2, 5);
         bpanWdsrDetails.add(lblQuerverweise, gridBagConstraints);
 
         scpQuer.setOpaque(false);
 
         edtQuer.setEditable(false);
         edtQuer.setContentType(org.openide.util.NbBundle.getMessage(
                 WDSRDetailsPanel.class,
                 "WDSRDetailsPanel.edtQuer.contentType")); // NOI18N
         edtQuer.setOpaque(false);
         scpQuer.setViewportView(edtQuer);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 17;
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(2, 3, 2, 2);
         bpanWdsrDetails.add(scpQuer, gridBagConstraints);
 
         jSeparator3.setMinimumSize(new java.awt.Dimension(0, 10));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 16;
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
         bpanWdsrDetails.add(jSeparator3, gridBagConstraints);
 
         jPanel1.setOpaque(false);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 20;
         gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
         gridBagConstraints.weighty = 0.01;
         bpanWdsrDetails.add(jPanel1, gridBagConstraints);
 
         add(bpanWdsrDetails, java.awt.BorderLayout.CENTER);
 
         bindingGroup.bind();
     } // </editor-fold>//GEN-END:initComponents
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void cboStrasseActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cboStrasseActionPerformed
         CidsBean strasseBean = null;
         if (frontBean != null) {
             strasseBean = (CidsBean)frontBean.getProperty(FrontPropertyConstants.PROP__FRONTINFO + "."
                             + FrontinfoPropertyConstants.PROP__STRASSE);
         }
         if (strasseBean != null) {
             final int strasseId = (Integer)strasseBean.getProperty(StrassePropertyConstants.PROP__ID);
 
             final String tabSatzung = VerdisMetaClassConstants.MC_SATZUNG;
             final String tabWinterdienst = VerdisMetaClassConstants.MC_WINTERDIENST;
             final String tabStrassenreinigung = VerdisMetaClassConstants.MC_STRASSENREINIGUNG;
             final String tabStrasse = VerdisMetaClassConstants.MC_STRASSE;
             final String fldId = FrontinfoPropertyConstants.PROP__ID;
             final String fldStrasse = "strasse";
             final String fldSrKlasse = "sr_klasse";
             final String fldWdPrio = "wd_prio";
 
             CismetThreadPool.execute(new SatzungsComboModelWorker(
                     cboLageSR,
                     "SELECT "
                             + CidsAppBackend.getInstance().getVerdisMetaClass(tabSatzung).getId()
                             + ", "
                             + tabSatzung
                             + ".id "
                             + "FROM "
                             + tabStrasse
                             + ", "
                             + tabSatzung
                             + ", "
                             + tabStrassenreinigung
                             + " "
                             + "WHERE "
                             + tabStrasse
                             + "."
                             + fldId
                             + " = "
                             + strasseId
                             + " AND "
                             + tabSatzung
                             + "."
                             + fldStrasse
                             + " = "
                             + tabStrasse
                             + "."
                             + fldId
                             + " AND "
                             + tabStrassenreinigung
                             + "."
                             + fldId
                             + " = "
                             + tabSatzung
                             + "."
                             + fldSrKlasse
                             + ";",
                     (CidsBean)getCidsBean().getProperty(
                         FrontPropertyConstants.PROP__FRONTINFO
                                 + "."
                                 + FrontinfoPropertyConstants.PROP__LAGE_SR)));
             CismetThreadPool.execute(new SatzungsComboModelWorker(
                     cboLageWD,
                     "SELECT "
                             + CidsAppBackend.getInstance().getVerdisMetaClass(tabSatzung).getId()
                             + ", "
                             + tabSatzung
                             + ".id "
                             + "FROM "
                             + tabStrasse
                             + ", "
                             + tabSatzung
                             + ", "
                             + tabWinterdienst
                             + " "
                             + "WHERE "
                             + tabStrasse
                             + "."
                             + fldId
                             + " = "
                             + strasseId
                             + " AND "
                             + tabSatzung
                             + "."
                             + fldStrasse
                             + " = "
                             + tabStrasse
                             + "."
                             + fldId
                             + " AND "
                             + tabWinterdienst
                             + "."
                             + fldId
                             + " = "
                             + tabSatzung
                             + "."
                             + fldWdPrio
                             + ";",
                     (CidsBean)getCidsBean().getProperty(
                         FrontPropertyConstants.PROP__FRONTINFO
                                 + "."
                                 + FrontinfoPropertyConstants.PROP__LAGE_WD)));
         }
     } //GEN-LAST:event_cboStrasseActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void cboLageSRActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cboLageSRActionPerformed
         updateLageDependendCbos();
     }                                                                             //GEN-LAST:event_cboLageSRActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void cboLageWDActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cboLageWDActionPerformed
         updateLageDependendCbos();
     }                                                                             //GEN-LAST:event_cboLageWDActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public Geometry getGeometry() {
         return getGeometry(getCidsBean());
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   frontBean  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static Geometry getGeometry(final CidsBean frontBean) {
         if ((frontBean != null)
                     && (frontBean.getProperty(
                             FrontPropertyConstants.PROP__FRONTINFO
                             + "."
                             + FrontinfoPropertyConstants.PROP__GEOMETRIE) != null)) {
             return (Geometry)frontBean.getProperty(FrontPropertyConstants.PROP__FRONTINFO + "."
                             + FrontinfoPropertyConstants.PROP__GEOMETRIE + "."
                             + GeomPropertyConstants.PROP__GEO_FIELD);
         } else {
             return null;
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   geom      DOCUMENT ME!
      * @param   cidsBean  DOCUMENT ME!
      *
      * @throws  Exception  DOCUMENT ME!
      */
     public static void setGeometry(final Geometry geom, final CidsBean cidsBean) throws Exception {
         Main.transformToDefaultCrsNeeded(geom);
         if (
             cidsBean.getProperty(
                         FrontPropertyConstants.PROP__FRONTINFO
                         + "."
                         + FrontinfoPropertyConstants.PROP__GEOMETRIE)
                     == null) {
             final CidsBean emptyGeoBean = CidsAppBackend.getInstance()
                         .getVerdisMetaClass(VerdisMetaClassConstants.MC_GEOM)
                         .getEmptyInstance()
                         .getBean();
             cidsBean.setProperty(FrontPropertyConstants.PROP__FRONTINFO + "."
                         + FrontinfoPropertyConstants.PROP__GEOMETRIE,
                 emptyGeoBean);
         }
         cidsBean.setProperty(FrontPropertyConstants.PROP__FRONTINFO + "." + FrontinfoPropertyConstants.PROP__GEOMETRIE
                     + "." + GeomPropertyConstants.PROP__GEO_FIELD,
             geom);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     @Override
     public CidsBean getCidsBean() {
         return frontBean;
     }
 
     @Override
     public void setCidsBean(final CidsBean cidsBean) {
         frontBean = cidsBean;
         setEnabled(CidsAppBackend.getInstance().isEditable() && (cidsBean != null));
 //        DefaultCustomObjectEditor.setMetaClassInformationToMetaClassStoreComponentsInBindingGroup(bindingGroup, cidsBean);
         bindingGroup.unbind();
         bindingGroup.bind();
 
         try {
             if ((cidsBean != null)
                         && (cidsBean.getProperty(
                                 FrontPropertyConstants.PROP__FRONTINFO
                                 + "."
                                 + FrontinfoPropertyConstants.PROP__GEOMETRIE) != null)) {
                 bpanWdsrDetails.setBackgroundEnabled(true);
             } else {
                 bpanWdsrDetails.setBackgroundEnabled(false);
             }
             if ((cidsBean == null)
                         || (cidsBean.getProperty(
                                 FrontPropertyConstants.PROP__FRONTINFO
                                 + "."
                                 + FrontinfoPropertyConstants.PROP__STRASSE) == null)) {
                 cboLageSR.setSelectedItem(null);
                 cboLageWD.setSelectedItem(null);
             }
         } catch (Exception e) {
             LOG.warn("problem when trying to set background enabled (or not). will turn the background off", e);
             bpanWdsrDetails.setBackgroundEnabled(false);
         }
         updateLageDependendCbos();
         updateCrossReferences();
         attachBeanValidators();
     }
 
     /**
      * DOCUMENT ME!
      */
     public synchronized void updateCrossReferences() {
         if (frontBean != null) {
             new SwingWorker<String, Void>() {
 
                     @Override
                     protected String doInBackground() throws Exception {
                         final Collection crossReference = (Collection)CidsAppBackend.getInstance()
                                     .getFrontenCrossReferencesFor(frontBean.getMetaObject().getID());
 
                         if (crossReference != null) {
                             String html = "<html><body><center>";
                             for (final Object o : crossReference) {
                                 final String link = o.toString();
                                 html += "<a href=\"" + link + "\"><font size=\"-2\">" + link + "</font></a><br>";
                             }
                             html += "</center></body></html>";
                             return html;
                         }
 
                         return null;
                     }
 
                     @Override
                     protected void done() {
                         try {
                             final String html = get();
                             if (html != null) {
                                 lblQuerverweise.setVisible(true);
                                 edtQuer.setVisible(true);
                                 scpQuer.setVisible(true);
                                 edtQuer.setText(html);
                                 edtQuer.setCaretPosition(0);
                             } else {
                                 edtQuer.setText("");
                                 lblQuerverweise.setVisible(false);
                                 edtQuer.setVisible(false);
                                 scpQuer.setVisible(false);
                             }
                         } catch (InterruptedException ex) {
                             Exceptions.printStackTrace(ex);
                         } catch (ExecutionException ex) {
                             Exceptions.printStackTrace(ex);
                         }
                     }
                 }.execute();
         } else {
             edtQuer.setText("");
             lblQuerverweise.setVisible(false);
             edtQuer.setVisible(false);
             scpQuer.setVisible(false);
         }
     }
 
     /**
      * DOCUMENT ME!
      */
     private void updateLageDependendCbos() {
         if (getCidsBean() != null) {
             final CidsBean lageWDBean = (CidsBean)getCidsBean().getProperty(FrontPropertyConstants.PROP__FRONTINFO + "."
                             + FrontinfoPropertyConstants.PROP__LAGE_WD);
             if (lageWDBean != null) {
                 String wdname = "kein Winterdienst";
                 if (lageWDBean.getProperty("wd_prio") != null) {
                     wdname = (String)lageWDBean.getProperty("wd_prio.name");
                 }
                 cboWDFromLage.setModel(new DefaultComboBoxModel(new String[] { wdname }));
             }
             cboWD.setVisible(lageWDBean == null);
             cboWDFromLage.setVisible(lageWDBean != null);
 
             final CidsBean lageSRBean = (CidsBean)getCidsBean().getProperty(FrontPropertyConstants.PROP__FRONTINFO + "."
                             + FrontinfoPropertyConstants.PROP__LAGE_SR);
             if (lageSRBean != null) {
                 String srname = "keine Straßenreinigung";
                 if (lageSRBean.getProperty("sr_klasse") != null) {
                     srname = (String)lageSRBean.getProperty("sr_klasse.name");
                 }
                 cboSRFromLage.setModel(new DefaultComboBoxModel(new String[] { srname }));
             }
             cboSR.setVisible(lageSRBean == null);
             cboSRFromLage.setVisible(lageSRBean != null);
         }
     }
 
     @Override
     public void editModeChanged() {
         setEnabled(CidsAppBackend.getInstance().isEditable() && (getCidsBean() != null));
     }
 
     @Override
     public void setEnabled(final boolean bln) {
         super.setEnabled(bln);
         txtBearbeitetDurch.setEditable(bln);
         txtBemSR.setEditable(bln);
         txtBemWD.setEditable(bln);
         txtErfassungsdatum.setEditable(bln);
         txtLaengeGrafik.setEditable(bln);
         txtLaengeKorrektur.setEditable(bln);
         txtNummer.setEditable(bln);
         txtVeranlagungSR.setEditable(bln);
         txtVeranlagungWD.setEditable(bln);
         cboSR.setEnabled(bln);
         cboStrasse.setEnabled(bln);
         cboWD.setEnabled(bln);
         cboLageWD.setEnabled(bln);
         cboLageSR.setEnabled(bln);
 
         txtBearbeitetDurch.setOpaque(bln);
         txtBemSR.setOpaque(bln);
         txtBemWD.setOpaque(bln);
         txtErfassungsdatum.setOpaque(bln);
         txtLaengeGrafik.setOpaque(bln);
         txtLaengeKorrektur.setOpaque(bln);
         txtNummer.setOpaque(bln);
         txtVeranlagungSR.setOpaque(bln);
         txtVeranlagungWD.setOpaque(bln);
         cboSR.setOpaque(bln);
         cboStrasse.setOpaque(bln);
         cboWD.setOpaque(bln);
         cboLageWD.setOpaque(bln);
         cboLageSR.setOpaque(bln);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  pCanvas  DOCUMENT ME!
      */
     public void setBackgroundPCanvas(final PCanvas pCanvas) {
         pCanvas.setBackground(getBackground());
         bpanWdsrDetails.setPCanvas(pCanvas);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   frontBean  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static Validator getValidatorNummer(final CidsBean frontBean) {
         return new CidsBeanValidator(
                 frontBean,
                 FrontPropertyConstants.PROP__NUMMER) {
 
                 @Override
                 public ValidatorState performValidation() {
                     final CidsBean cidsBean = getCidsBean();
                     if (cidsBean == null) {
                         return null;
                     }
 
 //                final String bezeichnung = (String) cidsBean.getProperty(PROP__"flaechenbezeichnung");
 //                final int art = (cidsBean.getProperty(PROP__"flaecheninfo.flaechenart.id") == null) ? 0 : (Integer) cidsBean.getProperty(PROP__"flaecheninfo.flaechenart.id");
 //
 //                final Action action = new AbstractAction() {
 //
 //                    @Override
 //                    public void actionPerformed(ActionEvent e) {
 //                        final int answer = JOptionPane.showConfirmDialog(
 //                                Main.THIS,
 //                                "Soll die n\u00E4chste freie Bezeichnung gew\u00E4hlt werden?",
 //                                "Bezeichnung automatisch setzen",
 //                                JOptionPane.YES_NO_OPTION);
 //                        if (answer == JOptionPane.YES_OPTION) {
 //                            final CidsBean cidsBean = getCidsBean();
 //                            int art;
 //                            try {
 //                                art = (Integer) cidsBean.getProperty(PROP__"flaecheninfo.flaechenart.id");
 //                            } catch (final NumberFormatException ex) {
 //                                art = 0;
 //                            }
 //                            final String newValue = Main.THIS.getRegenFlaechenTabellenPanel().getValidFlaechenname(art);
 //                            try {
 //                                cidsBean.setProperty(PROP__"flaechenbezeichnung", newValue);
 //                            } catch (Exception ex) {
 //                                if (log.isDebugEnabled()) {
 //                                    log.debug("error while setting flaechenbezeichnung", ex);
 //                                }
 //                            }
 //                        }
 //                    }
 //                };
 //                boolean numerisch = false;
 //                Integer tester = null;
 //                try {
 //                    tester = Integer.parseInt(bezeichnung);
 //                    numerisch = true;
 //                } catch (final Exception ex) {
 //                    numerisch = false;
 //                }
 //
 //                if ((art == Main.PROPVAL_ART_DACH) || (art == Main.PROPVAL_ART_GRUENDACH)) {
 //                    if (!numerisch) {
 //                        return new ValidatorStateImpl(ValidatorState.ERROR, "Fl\u00E4chenbezeichnung muss eine Zahl sein.", action);
 //                    } else {
 //                        if ((tester.intValue() > 1000) || (tester.intValue() < 0)) {
 //                            return new ValidatorStateImpl(ValidatorState.ERROR, "Fl\u00E4chenbezeichnung muss zwischen 0 und 1000 liegen.", action);
 //                        }
 //                    }
 //                } else {
 //                    if (bezeichnung != null) {
 //                        final int len = bezeichnung.length();
 //                        if (numerisch || ((len > 3) || ((len == 3) && (bezeichnung.compareTo("BBB") > 0)))) {
 //                            return new ValidatorStateImpl(ValidatorState.ERROR, "Fl\u00E4chenbezeichnung muss zwischen A und BBB liegen.", action);
 //                        }
 //                    }
 //                }
                     return new ValidatorStateImpl(ValidatorState.Type.VALID);
                 }
             };
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   frontBean  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static Validator getValidatorLaengeGrafik(final CidsBean frontBean) {
         return new CidsBeanValidator(
                 frontBean,
                 FrontPropertyConstants.PROP__FRONTINFO
                         + "."
                         + FrontinfoPropertyConstants.PROP__LAENGE_GRAFIK) {
 
                 @Override
                 public ValidatorState performValidation() {
                     final CidsBean cidsBean = getCidsBean();
                     if (cidsBean == null) {
                         return null;
                     }
 
                     final Integer laenge_grafik = (Integer)cidsBean.getProperty(
                             FrontPropertyConstants.PROP__FRONTINFO
                                     + "."
                                     + FrontinfoPropertyConstants.PROP__LAENGE_GRAFIK);
                     final Geometry geom = WDSRDetailsPanel.getGeometry(cidsBean);
                     final Action action = new AbstractAction() {
 
                             @Override
                             public void actionPerformed(final ActionEvent event) {
                                 final CidsBean cidsBean = getCidsBean();
                                 final Geometry geom = WDSRDetailsPanel.getGeometry(cidsBean);
 
                                 if (Main.getCurrentInstance().isInEditMode()) {
                                     if (geom != null) {
                                         final int answer = JOptionPane.showConfirmDialog(
                                                 Main.getCurrentInstance(),
                                                 "Soll die Länge aus der Grafik \u00FCbernommen werden?",
                                                 "Länge automatisch setzen",
                                                 JOptionPane.YES_NO_OPTION);
                                         if (answer == JOptionPane.YES_OPTION) {
                                             try {
                                                 final int laenge_grafik = (int)Math.abs(geom.getLength());
                                                 cidsBean.setProperty(
                                                     FrontPropertyConstants.PROP__FRONTINFO
                                                             + "."
                                                             + FrontinfoPropertyConstants.PROP__LAENGE_GRAFIK,
                                                     laenge_grafik);
                                             } catch (final Exception ex) {
                                                 if (LOG.isDebugEnabled()) {
                                                     LOG.debug("error while setting laenge_grafik", ex);
                                                 }
                                             }
                                         }
                                     }
                                 }
                             }
                         };
 
                     if (laenge_grafik == null) {
                         return new ValidatorStateImpl(ValidatorState.Type.ERROR, "Wert ist leer", action);
                     }
 
                     if ((geom != null) && (laenge_grafik != (int)Math.abs(geom.getLength()))) {
                         return new ValidatorStateImpl(
                                 ValidatorState.Type.WARNING,
                                 "L\u00E4nge der Geometrie stimmt nicht \u00FCberein ("
                                         + ((int)(geom.getLength()))
                                         + ")",
                                 action);
                     }
                     return new ValidatorStateImpl(ValidatorState.Type.VALID);
                 }
             };
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   frontBean  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static Validator getValidatorLaengeKorrektur(final CidsBean frontBean) {
         return new CidsBeanValidator(
                 frontBean,
                 FrontPropertyConstants.PROP__FRONTINFO
                         + "."
                         + FrontinfoPropertyConstants.PROP__LAENGE_KORREKTUR) {
 
                 @Override
                 public ValidatorState performValidation() {
                     final CidsBean cidsBean = getCidsBean();
                     if (cidsBean == null) {
                         return null;
                     }
 
                     Integer laenge_grafik = (Integer)cidsBean.getProperty(
                             FrontPropertyConstants.PROP__FRONTINFO
                                     + "."
                                     + FrontinfoPropertyConstants.PROP__LAENGE_GRAFIK);
                     final Integer laenge_korrektur = (Integer)cidsBean.getProperty(
                             FrontPropertyConstants.PROP__FRONTINFO
                                     + "."
                                     + FrontinfoPropertyConstants.PROP__LAENGE_KORREKTUR);
                     final Action action = new AbstractAction() {
 
                             @Override
                             public void actionPerformed(final ActionEvent event) {
                                 final CidsBean cidsBean = getCidsBean();
                                 final Geometry geom = WDSRDetailsPanel.getGeometry(cidsBean);
 
                                 if (Main.getCurrentInstance().isInEditMode()) {
                                     if (geom != null) {
                                         final int answer = JOptionPane.showConfirmDialog(
                                                 Main.getCurrentInstance(),
                                                 "Soll die Länge aus der Grafik \u00FCbernommen werden?",
                                                 "Länge automatisch setzen",
                                                 JOptionPane.YES_NO_OPTION);
                                         if (answer == JOptionPane.YES_OPTION) {
                                             try {
                                                 final Integer laenge_grafik = (Integer)cidsBean.getProperty(
                                                         FrontPropertyConstants.PROP__FRONTINFO
                                                                 + "."
                                                                 + FrontinfoPropertyConstants.PROP__LAENGE_GRAFIK);
                                                 cidsBean.setProperty(
                                                     FrontPropertyConstants.PROP__FRONTINFO
                                                             + "."
                                                             + FrontinfoPropertyConstants.PROP__LAENGE_KORREKTUR,
                                                     laenge_grafik);
                                             } catch (final Exception ex) {
                                                 if (LOG.isDebugEnabled()) {
                                                     LOG.debug("error while setting laenge_korrektur", ex);
                                                 }
                                             }
                                         }
                                     }
                                 }
                             }
                         };
 
                     if (laenge_korrektur != null) {
                         if (laenge_grafik == null) {
                             laenge_grafik = 0;
                         }
                         final int diff = laenge_korrektur - laenge_grafik;
                         if (Math.abs(diff) > 20) {
                             return new ValidatorStateImpl(
                                     ValidatorState.Type.WARNING,
                                     "Differenz zwischen Korrekturwert und Länge > 20m.",
                                     action);
                         }
                     }
                     return new ValidatorStateImpl(ValidatorState.Type.VALID);
                 }
             };
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   frontBean  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static Validator getValidatorDatumErfassung(final CidsBean frontBean) {
         return new CidsBeanValidator(
                 frontBean,
                 FrontPropertyConstants.PROP__ERFASSUNGSDATUM) {
 
                 @Override
                 public ValidatorState performValidation() {
                     final CidsBean cidsBean = getCidsBean();
                     if (cidsBean == null) {
                         return null;
                     }
 
                     // jedes gültige Datum ist valide
                     return new ValidatorStateImpl(ValidatorState.Type.VALID);
                 }
             };
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   frontBean  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static Validator getValidatorVeranlagungSR(final CidsBean frontBean) {
         return new CidsBeanValidator(
                 frontBean,
                 FrontPropertyConstants.PROP__FRONTINFO
                         + "."
                         + FrontinfoPropertyConstants.PROP__SR_VERANLAGUNG) {
 
                 @Override
                 public ValidatorState performValidation() {
                     final CidsBean cidsBean = getCidsBean();
                     if (cidsBean == null) {
                         return null;
                     }
 
                     final String veranlagungsdatum = (String)cidsBean.getProperty(
                             FrontPropertyConstants.PROP__FRONTINFO
                                     + "."
                                     + FrontinfoPropertyConstants.PROP__SR_VERANLAGUNG);
 
                     if (veranlagungsdatum != null) {
                         final boolean matches = Pattern.matches(
                                 "\\d\\d/(01|02|03|04|05|06|07|08|09|10|11|12)",
                                 veranlagungsdatum);
                         if (!matches) {
                             return new ValidatorStateImpl(
                                     ValidatorState.Type.ERROR,
                                     "Veranlagungsdatum muss im Format JJ/MM eingegeben werden.");
                         }
                     }
                     return new ValidatorStateImpl(ValidatorState.Type.VALID);
                 }
             };
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   frontBean  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static Validator getValidatorVeranlagungWD(final CidsBean frontBean) {
         return new CidsBeanValidator(
                 frontBean,
                 FrontPropertyConstants.PROP__FRONTINFO
                         + "."
                         + FrontinfoPropertyConstants.PROP__WD_VERANLAGUNG) {
 
                 @Override
                 public ValidatorState performValidation() {
                     final CidsBean cidsBean = getCidsBean();
                     if (cidsBean == null) {
                         return null;
                     }
 
                     final String veranlagungsdatum = (String)cidsBean.getProperty(
                             FrontPropertyConstants.PROP__FRONTINFO
                                     + "."
                                     + FrontinfoPropertyConstants.PROP__WD_VERANLAGUNG);
 
                     if (veranlagungsdatum != null) {
                         final boolean matches = Pattern.matches(
                                 "\\d\\d/(01|02|03|04|05|06|07|08|09|10|11|12)",
                                 veranlagungsdatum);
                         if (!matches) {
                             return new ValidatorStateImpl(
                                     ValidatorState.Type.ERROR,
                                     "Veranlagungsdatum muss im Format JJ/MM eingegeben werden.");
                         }
                     }
                     return new ValidatorStateImpl(ValidatorState.Type.VALID);
                 }
             };
     }
 
     @Override
     public void hyperlinkUpdate(final HyperlinkEvent he) {
         final Thread t = new Thread() {
 
                 @Override
                 public void run() {
                     if (he.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                         try {
                             Main.getCurrentInstance().getKzPanel().gotoKassenzeichen(he.getDescription());
                         } catch (Exception ex) {
                             LOG.error("Fehler im Hyperlinken", ex);
                         }
                     }
                 }
             };
         t.setPriority(Thread.NORM_PRIORITY);
         t.start();
     }
 
     //~ Inner Classes ----------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     class SatzungsComboModelWorker extends SwingWorker<ComboBoxModel, Void> {
 
         //~ Instance fields ----------------------------------------------------
 
         private final JComboBox cb;
         private final String query;
         private final CidsBean selected;
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new SatzungsComboModelWorker object.
          *
          * @param  cb        DOCUMENT ME!
          * @param  query     DOCUMENT ME!
          * @param  selected  DOCUMENT ME!
          */
         public SatzungsComboModelWorker(final JComboBox cb,
                 final String query,
                 final CidsBean selected) {
             this.cb = cb;
             this.query = query;
             this.selected = selected;
         }
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         protected ComboBoxModel doInBackground() throws Exception {
             cb.setModel(WAIT_MODEL);
             cb.setEnabled(false);
 
             final Collection<CidsBean> cbs = CidsAppBackend.getInstance().getBeansByQuery(query);
 
             final DefaultComboBoxModel dcm = new DefaultComboBoxModel();
             dcm.addElement(null);
 
             for (final CidsBean cb : cbs) {
                 dcm.addElement(cb);
             }
             return dcm;
         }
 
         @Override
         protected void done() {
             try {
                 cb.setModel(get());
                 cb.setSelectedItem(selected);
             } catch (Exception ex) {
                 LOG.error(ex, ex);
             } finally {
                 cb.setEnabled(isEnabled() && (getCidsBean() != null));
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     class SatzungsConverter extends Converter<CidsBean, MetaObject> {
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         public MetaObject convertForward(final CidsBean t) {
             if (t == null) {
                 return null;
             }
             return t.getMetaObject();
         }
 
         @Override
         public CidsBean convertReverse(final MetaObject s) {
             if (s == null) {
                 return null;
             }
             return s.getBean();
         }
     }
 }
