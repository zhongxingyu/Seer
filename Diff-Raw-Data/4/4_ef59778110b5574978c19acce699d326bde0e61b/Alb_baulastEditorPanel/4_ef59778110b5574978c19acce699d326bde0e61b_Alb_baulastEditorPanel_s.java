 /***************************************************
  *
  * cismet GmbH, Saarbruecken, Germany
  *
  *              ... and it just works.
  *
  ****************************************************/
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 /*
  * Alb_baulastEditorPanel.java
  *
  * Created on 27.11.2009, 14:20:31
  */
 package de.cismet.cids.custom.objecteditors.wunda_blau;
 
 import Sirius.navigator.ui.ComponentRegistry;
 
 import Sirius.server.middleware.types.AbstractAttributeRepresentationFormater;
 import Sirius.server.middleware.types.LightweightMetaObject;
 import Sirius.server.middleware.types.MetaObject;
 
 import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 import java.util.concurrent.ExecutionException;
 
 import javax.swing.ComboBoxModel;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 import javax.swing.SwingWorker;
 
 import de.cismet.cids.custom.objectrenderer.utils.AlphanumComparator;
 import de.cismet.cids.custom.objectrenderer.utils.CidsBeanSupport;
 import de.cismet.cids.custom.objectrenderer.utils.ObjectRendererUtils;
 
 import de.cismet.cids.dynamics.CidsBean;
 import de.cismet.cids.dynamics.DisposableCidsBeanStore;
 
 import de.cismet.cids.editors.DefaultBindableDateChooser;
 
 import de.cismet.tools.CismetThreadPool;
 
 import de.cismet.tools.collections.TypeSafeCollections;
 import java.sql.Date;
 import java.text.DateFormat;
 import java.util.ArrayList;
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import org.jdesktop.beansbinding.Converter;
 
 /**
  * DOCUMENT ME!
  *
  * @author   srichter
  * @version  $Revision$, $Date$
  */
 public class Alb_baulastEditorPanel extends javax.swing.JPanel implements DisposableCidsBeanStore {
 
     //~ Static fields/initializers ---------------------------------------------
     static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Alb_baulastEditorPanel.class);
     // End of variables declaration                   
     private static final ComboBoxModel waitModel = new DefaultComboBoxModel(new String[]{"Wird geladen..."});
     private static final Converter<java.sql.Date, String> DATE_TO_STRING = new Converter<Date, String>() {
 
         @Override
         public String convertForward(Date value) {
             if (value != null) {
                 return DateFormat.getDateInstance().format(value);
             } else {
                 return "-";
             }
         }
 
         @Override
         public Date convertReverse(String value) {
             throw new UnsupportedOperationException("Not supported yet.");
         }
     };
    private final Icon STATUS_OK = new ImageIcon(getClass().getResource("/de/cismet/cids/custom/objecteditors/wunda_blau/status.png"));
    private final Icon STATUS_FAIL = new ImageIcon(getClass().getResource("/de/cismet/cids/custom/objecteditors/wunda_blau/status-busy.png"));
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton btnAddArt;
     private javax.swing.JButton btnAddBeguenstigt;
     private javax.swing.JButton btnAddBelastet;
     private javax.swing.JButton btnMenAbort1;
     private javax.swing.JButton btnMenOk1;
     private javax.swing.JButton btnRemoveArt;
     private javax.swing.JButton btnRemoveBeguenstigt;
     private javax.swing.JButton btnRemoveBelastet;
     private javax.swing.JComboBox cbBaulastArt;
     private javax.swing.JCheckBox chkGeprueft;
     private de.cismet.cids.editors.DefaultBindableDateChooser defaultBindableDateChooser1;
     private de.cismet.cids.editors.DefaultBindableDateChooser defaultBindableDateChooser2;
     private de.cismet.cids.editors.DefaultBindableDateChooser defaultBindableDateChooser3;
     private de.cismet.cids.editors.DefaultBindableDateChooser defaultBindableDateChooser4;
     private javax.swing.JDialog dlgAddBaulastArt;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JLabel lblDescBaulastart;
     private javax.swing.JLabel lblDescBefristungsdatum;
     private javax.swing.JLabel lblDescEintragungsdatum;
     private javax.swing.JLabel lblDescGeschlossenAm;
     private javax.swing.JLabel lblDescLageplan;
     private javax.swing.JLabel lblDescLaufendeNr;
     private javax.swing.JLabel lblDescLoeschungsdatum;
     private javax.swing.JLabel lblDescTextblatt;
     private javax.swing.JLabel lblGeprueft;
     private javax.swing.JLabel lblHeadBegFlurstuecke;
     private javax.swing.JLabel lblHeadBelFlurstuecke;
     private javax.swing.JLabel lblHeadInfo;
     private javax.swing.JLabel lblLastInMap;
     private javax.swing.JLabel lblLetzteAenderung;
     private javax.swing.JLabel lblSuchwortEingeben1;
     private javax.swing.JLabel lblTxtGeprueft;
     private javax.swing.JList lstBaulastArt;
     private javax.swing.JList lstFlurstueckeBeguenstigt;
     private javax.swing.JList lstFlurstueckeBelastet;
     private javax.swing.JPanel panAddBaulastArt;
     private javax.swing.JPanel panArtControls;
     private javax.swing.JPanel panControlsFSBeg;
     private javax.swing.JPanel panControlsFSBel;
     private javax.swing.JPanel panMain;
     private javax.swing.JPanel panMenButtons1;
     private de.cismet.tools.gui.RoundedPanel rpFSBeguenstigt;
     private de.cismet.tools.gui.RoundedPanel rpFSBelastet;
     private de.cismet.tools.gui.SemiRoundedPanel rpHeadInfo;
     private de.cismet.tools.gui.RoundedPanel rpInfo;
     private javax.swing.JScrollPane scpBaulastart;
     private javax.swing.JScrollPane scpFlurstueckeBeguenstigt;
     private javax.swing.JScrollPane scpFlurstueckeBelastet;
     private de.cismet.tools.gui.SemiRoundedPanel semiRoundedPanel1;
     private de.cismet.tools.gui.SemiRoundedPanel semiRoundedPanel2;
     private de.cismet.cids.editors.converters.SqlDateToUtilDateConverter sqlDateToUtilDateConverter;
     private javax.swing.JTextField txtLageplan;
     private javax.swing.JTextField txtLaufendeNr;
     private javax.swing.JTextField txtTextblatt;
     private org.jdesktop.beansbinding.BindingGroup bindingGroup;
     // End of variables declaration//GEN-END:variables
 
     /**
      * Creates new form Alb_baulastEditorPanel.
      */
     public Alb_baulastEditorPanel() {
         this(true);
     }
 
     /**
      * Creates new form Alb_baulastEditorPanel.
      *
      * @param  editable  DOCUMENT ME!
      */
     public Alb_baulastEditorPanel(final boolean editable) {
         this.editable = editable;
         this.editableComponents = TypeSafeCollections.newArrayList();
         initComponents();
         initEditableComponents();
         fsDialoge = new FlurstueckSelectionDialoge();
         fsDialoge.pack();
         fsDialoge.setLocationRelativeTo(this);
         dlgAddBaulastArt.pack();
         dlgAddBaulastArt.setLocationRelativeTo(this);
         AutoCompleteDecorator.decorate(cbBaulastArt);
     }
 
     /**
      * DOCUMENT ME!
      */
     private void initEditableComponents() {
         editableComponents.add(txtLageplan);
         editableComponents.add(txtLaufendeNr);
         editableComponents.add(txtTextblatt);
         editableComponents.add(defaultBindableDateChooser1);
         editableComponents.add(defaultBindableDateChooser2);
         editableComponents.add(defaultBindableDateChooser3);
         editableComponents.add(defaultBindableDateChooser4);
 //        editableComponents.add(chkGeprueft);
 //        editableComponents.add(lstFlurstueckeBeguenstigt);
 //        editableComponents.add(lstFlurstueckeBelastet);
         for (final JComponent editableComponent : editableComponents) {
             editableComponent.setOpaque(editable);
             if (!editable) {
                 rpInfo.remove(chkGeprueft);
                 chkGeprueft.setEnabled(false);
                 java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
                 gridBagConstraints.gridx = 4;
                 gridBagConstraints.gridy = 1;
                 gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
                 gridBagConstraints.insets = new java.awt.Insets(10, 5, 5, 5);
                 rpInfo.add(lblGeprueft, gridBagConstraints);
                 editableComponent.setBorder(null);
                 if (editableComponent instanceof JTextField) {
                     ((JTextField) editableComponent).setEditable(false);
                 } else if (editableComponent instanceof DefaultBindableDateChooser) {
                     final DefaultBindableDateChooser dateChooser = (DefaultBindableDateChooser) editableComponent;
 //                    dateChooser.setEditable(false);
                     dateChooser.setEnabled(false);
                     dateChooser.getEditor().setDisabledTextColor(Color.BLACK);
                     dateChooser.getEditor().setOpaque(false);
                     dateChooser.getEditor().setBorder(null);
                 }
 //                else if (editableComponent instanceof JList) {
 //                    JList listEC = ((JList) editableComponent);
 //                    listEC.setEnabled(false);
 //                }
                 panControlsFSBeg.setVisible(false);
                 panControlsFSBel.setVisible(false);
                 panArtControls.setVisible(false);
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
         bindingGroup = new org.jdesktop.beansbinding.BindingGroup();
 
         sqlDateToUtilDateConverter = new de.cismet.cids.editors.converters.SqlDateToUtilDateConverter();
         dlgAddBaulastArt = new javax.swing.JDialog();
         panAddBaulastArt = new javax.swing.JPanel();
         lblSuchwortEingeben1 = new javax.swing.JLabel();
         cbBaulastArt = new javax.swing.JComboBox();
         panMenButtons1 = new javax.swing.JPanel();
         btnMenAbort1 = new javax.swing.JButton();
         btnMenOk1 = new javax.swing.JButton();
         lblGeprueft = new javax.swing.JLabel();
         panMain = new javax.swing.JPanel();
         rpFSBeguenstigt = new de.cismet.tools.gui.RoundedPanel();
         scpFlurstueckeBeguenstigt = new ColorJScrollpane(new Color(255, 255, 0));
         lstFlurstueckeBeguenstigt = new javax.swing.JList();
         semiRoundedPanel1 = new de.cismet.tools.gui.SemiRoundedPanel();
         lblHeadBegFlurstuecke = new javax.swing.JLabel();
         panControlsFSBeg = new javax.swing.JPanel();
         btnAddBeguenstigt = new javax.swing.JButton();
         btnRemoveBeguenstigt = new javax.swing.JButton();
         rpFSBelastet = new de.cismet.tools.gui.RoundedPanel();
         scpFlurstueckeBelastet = new ColorJScrollpane(new Color(0, 255, 0));
         lstFlurstueckeBelastet = new javax.swing.JList();
         semiRoundedPanel2 = new de.cismet.tools.gui.SemiRoundedPanel();
         lblHeadBelFlurstuecke = new javax.swing.JLabel();
         panControlsFSBel = new javax.swing.JPanel();
         btnAddBelastet = new javax.swing.JButton();
         btnRemoveBelastet = new javax.swing.JButton();
         rpInfo = new de.cismet.tools.gui.RoundedPanel();
         lblDescLaufendeNr = new javax.swing.JLabel();
         lblDescEintragungsdatum = new javax.swing.JLabel();
         lblDescBefristungsdatum = new javax.swing.JLabel();
         lblDescGeschlossenAm = new javax.swing.JLabel();
         lblDescLoeschungsdatum = new javax.swing.JLabel();
         lblDescTextblatt = new javax.swing.JLabel();
         txtTextblatt = new javax.swing.JTextField();
         txtLaufendeNr = new javax.swing.JTextField();
         lblDescLageplan = new javax.swing.JLabel();
         txtLageplan = new javax.swing.JTextField();
         defaultBindableDateChooser4 = new de.cismet.cids.editors.DefaultBindableDateChooser();
         defaultBindableDateChooser1 = new de.cismet.cids.editors.DefaultBindableDateChooser();
         defaultBindableDateChooser2 = new de.cismet.cids.editors.DefaultBindableDateChooser();
         defaultBindableDateChooser3 = new de.cismet.cids.editors.DefaultBindableDateChooser();
         rpHeadInfo = new de.cismet.tools.gui.SemiRoundedPanel();
         lblHeadInfo = new javax.swing.JLabel();
         lblLastInMap = new javax.swing.JLabel();
         lblDescBaulastart = new javax.swing.JLabel();
         scpBaulastart = new javax.swing.JScrollPane();
         lstBaulastArt = new javax.swing.JList();
         panArtControls = new javax.swing.JPanel();
         btnAddArt = new javax.swing.JButton();
         btnRemoveArt = new javax.swing.JButton();
         chkGeprueft = new javax.swing.JCheckBox();
         lblTxtGeprueft = new javax.swing.JLabel();
         lblLetzteAenderung = new javax.swing.JLabel();
         jLabel1 = new javax.swing.JLabel();
 
         dlgAddBaulastArt.setTitle("Art hinzufügen");
         dlgAddBaulastArt.setMinimumSize(new java.awt.Dimension(300, 120));
         dlgAddBaulastArt.setModal(true);
 
         panAddBaulastArt.setMaximumSize(new java.awt.Dimension(300, 120));
         panAddBaulastArt.setMinimumSize(new java.awt.Dimension(300, 120));
         panAddBaulastArt.setPreferredSize(new java.awt.Dimension(300, 120));
         panAddBaulastArt.setLayout(new java.awt.GridBagLayout());
 
         lblSuchwortEingeben1.setFont(new java.awt.Font("Tahoma", 1, 11));
         lblSuchwortEingeben1.setText("Bitte Art auswählen:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
         panAddBaulastArt.add(lblSuchwortEingeben1, gridBagConstraints);
 
         cbBaulastArt.setMaximumSize(new java.awt.Dimension(250, 20));
         cbBaulastArt.setMinimumSize(new java.awt.Dimension(250, 20));
         cbBaulastArt.setPreferredSize(new java.awt.Dimension(250, 20));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panAddBaulastArt.add(cbBaulastArt, gridBagConstraints);
 
         panMenButtons1.setLayout(new java.awt.GridBagLayout());
 
         btnMenAbort1.setText("Abbrechen");
         btnMenAbort1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnMenAbort1ActionPerformed(evt);
             }
         });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panMenButtons1.add(btnMenAbort1, gridBagConstraints);
 
         btnMenOk1.setText("Ok");
         btnMenOk1.setMaximumSize(new java.awt.Dimension(85, 23));
         btnMenOk1.setMinimumSize(new java.awt.Dimension(85, 23));
         btnMenOk1.setPreferredSize(new java.awt.Dimension(85, 23));
         btnMenOk1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnMenOk1ActionPerformed(evt);
             }
         });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panMenButtons1.add(btnMenOk1, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panAddBaulastArt.add(panMenButtons1, gridBagConstraints);
 
         dlgAddBaulastArt.getContentPane().add(panAddBaulastArt, java.awt.BorderLayout.CENTER);
 
         setOpaque(false);
         setLayout(new java.awt.BorderLayout());
 
         panMain.setOpaque(false);
         panMain.setLayout(new java.awt.GridBagLayout());
 
         rpFSBeguenstigt.setMaximumSize(new java.awt.Dimension(270, 195));
 
         scpFlurstueckeBeguenstigt.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5), javax.swing.BorderFactory.createEtchedBorder()));
         scpFlurstueckeBeguenstigt.setMaximumSize(new java.awt.Dimension(270, 142));
         scpFlurstueckeBeguenstigt.setMinimumSize(new java.awt.Dimension(270, 142));
         scpFlurstueckeBeguenstigt.setOpaque(false);
 
         lstFlurstueckeBeguenstigt.setFixedCellWidth(270);
 
         org.jdesktop.beansbinding.ELProperty eLProperty = org.jdesktop.beansbinding.ELProperty.create("${cidsBean.flurstuecke_beguenstigt}");
         org.jdesktop.swingbinding.JListBinding jListBinding = org.jdesktop.swingbinding.SwingBindings.createJListBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, eLProperty, lstFlurstueckeBeguenstigt);
         bindingGroup.addBinding(jListBinding);
 
         lstFlurstueckeBeguenstigt.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 lstFlurstueckeBeguenstigtMouseClicked(evt);
             }
         });
         scpFlurstueckeBeguenstigt.setViewportView(lstFlurstueckeBeguenstigt);
 
         rpFSBeguenstigt.add(scpFlurstueckeBeguenstigt, java.awt.BorderLayout.CENTER);
 
         semiRoundedPanel1.setBackground(java.awt.Color.darkGray);
         semiRoundedPanel1.setLayout(new java.awt.GridBagLayout());
 
         lblHeadBegFlurstuecke.setForeground(new java.awt.Color(255, 255, 255));
         lblHeadBegFlurstuecke.setText("Begünstigte Flurstücke");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         semiRoundedPanel1.add(lblHeadBegFlurstuecke, gridBagConstraints);
 
         rpFSBeguenstigt.add(semiRoundedPanel1, java.awt.BorderLayout.NORTH);
 
         panControlsFSBeg.setOpaque(false);
         panControlsFSBeg.setLayout(new java.awt.GridBagLayout());
 
         btnAddBeguenstigt.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cids/custom/objecteditors/wunda_blau/edit_add_mini.png"))); // NOI18N
         btnAddBeguenstigt.setMaximumSize(new java.awt.Dimension(43, 25));
         btnAddBeguenstigt.setMinimumSize(new java.awt.Dimension(43, 25));
         btnAddBeguenstigt.setPreferredSize(new java.awt.Dimension(43, 25));
         btnAddBeguenstigt.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnAddBeguenstigtActionPerformed(evt);
             }
         });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panControlsFSBeg.add(btnAddBeguenstigt, gridBagConstraints);
 
         btnRemoveBeguenstigt.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cids/custom/objecteditors/wunda_blau/edit_remove_mini.png"))); // NOI18N
         btnRemoveBeguenstigt.setMaximumSize(new java.awt.Dimension(43, 25));
         btnRemoveBeguenstigt.setMinimumSize(new java.awt.Dimension(43, 25));
         btnRemoveBeguenstigt.setPreferredSize(new java.awt.Dimension(43, 25));
         btnRemoveBeguenstigt.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnRemoveBeguenstigtActionPerformed(evt);
             }
         });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panControlsFSBeg.add(btnRemoveBeguenstigt, gridBagConstraints);
 
         rpFSBeguenstigt.add(panControlsFSBeg, java.awt.BorderLayout.SOUTH);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 0.5;
         gridBagConstraints.weighty = 0.5;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
         panMain.add(rpFSBeguenstigt, gridBagConstraints);
 
         rpFSBelastet.setMaximumSize(new java.awt.Dimension(270, 195));
 
         scpFlurstueckeBelastet.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5), javax.swing.BorderFactory.createEtchedBorder()));
         scpFlurstueckeBelastet.setMaximumSize(new java.awt.Dimension(270, 142));
         scpFlurstueckeBelastet.setMinimumSize(new java.awt.Dimension(270, 142));
         scpFlurstueckeBelastet.setOpaque(false);
 
         lstFlurstueckeBelastet.setFixedCellWidth(270);
 
         eLProperty = org.jdesktop.beansbinding.ELProperty.create("${cidsBean.flurstuecke_belastet}");
         jListBinding = org.jdesktop.swingbinding.SwingBindings.createJListBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, eLProperty, lstFlurstueckeBelastet);
         bindingGroup.addBinding(jListBinding);
 
         lstFlurstueckeBelastet.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 lstFlurstueckeBelastetMouseClicked(evt);
             }
         });
         scpFlurstueckeBelastet.setViewportView(lstFlurstueckeBelastet);
 
         rpFSBelastet.add(scpFlurstueckeBelastet, java.awt.BorderLayout.CENTER);
 
         semiRoundedPanel2.setBackground(java.awt.Color.darkGray);
         semiRoundedPanel2.setLayout(new java.awt.GridBagLayout());
 
         lblHeadBelFlurstuecke.setForeground(new java.awt.Color(255, 255, 255));
         lblHeadBelFlurstuecke.setText("Belastete Flurstücke");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         semiRoundedPanel2.add(lblHeadBelFlurstuecke, gridBagConstraints);
 
         rpFSBelastet.add(semiRoundedPanel2, java.awt.BorderLayout.NORTH);
 
         panControlsFSBel.setOpaque(false);
         panControlsFSBel.setLayout(new java.awt.GridBagLayout());
 
         btnAddBelastet.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cids/custom/objecteditors/wunda_blau/edit_add_mini.png"))); // NOI18N
         btnAddBelastet.setMaximumSize(new java.awt.Dimension(43, 25));
         btnAddBelastet.setMinimumSize(new java.awt.Dimension(43, 25));
         btnAddBelastet.setPreferredSize(new java.awt.Dimension(43, 25));
         btnAddBelastet.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnAddBelastetActionPerformed(evt);
             }
         });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panControlsFSBel.add(btnAddBelastet, gridBagConstraints);
 
         btnRemoveBelastet.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cids/custom/objecteditors/wunda_blau/edit_remove_mini.png"))); // NOI18N
         btnRemoveBelastet.setMaximumSize(new java.awt.Dimension(43, 25));
         btnRemoveBelastet.setMinimumSize(new java.awt.Dimension(43, 25));
         btnRemoveBelastet.setPreferredSize(new java.awt.Dimension(43, 25));
         btnRemoveBelastet.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnRemoveBelastetActionPerformed(evt);
             }
         });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panControlsFSBel.add(btnRemoveBelastet, gridBagConstraints);
 
         rpFSBelastet.add(panControlsFSBel, java.awt.BorderLayout.SOUTH);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 0.5;
         gridBagConstraints.weighty = 0.5;
         gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
         panMain.add(rpFSBelastet, gridBagConstraints);
 
         rpInfo.setLayout(new java.awt.GridBagLayout());
 
         lblDescLaufendeNr.setText("Laufende Nummer:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(10, 5, 5, 5);
         rpInfo.add(lblDescLaufendeNr, gridBagConstraints);
 
         lblDescEintragungsdatum.setText("Eintragungsdatum:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         rpInfo.add(lblDescEintragungsdatum, gridBagConstraints);
 
         lblDescBefristungsdatum.setText("Befristungsdatum:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         rpInfo.add(lblDescBefristungsdatum, gridBagConstraints);
 
         lblDescGeschlossenAm.setText("Geschlossen am:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         rpInfo.add(lblDescGeschlossenAm, gridBagConstraints);
 
         lblDescLoeschungsdatum.setText("Löschungsdatum:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 5;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         rpInfo.add(lblDescLoeschungsdatum, gridBagConstraints);
 
         lblDescTextblatt.setText("Textblatt:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 6;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         rpInfo.add(lblDescTextblatt, gridBagConstraints);
 
         org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${cidsBean.textblatt}"), txtTextblatt, org.jdesktop.beansbinding.BeanProperty.create("text"));
         binding.setSourceNullValue("nicht verfügbar");
         binding.setSourceUnreadableValue("");
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 6;
         gridBagConstraints.gridwidth = 7;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
         rpInfo.add(txtTextblatt, gridBagConstraints);
 
         txtLaufendeNr.setMaximumSize(new java.awt.Dimension(125, 20));
         txtLaufendeNr.setMinimumSize(new java.awt.Dimension(125, 20));
         txtLaufendeNr.setPreferredSize(new java.awt.Dimension(125, 20));
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${cidsBean.laufende_nummer}"), txtLaufendeNr, org.jdesktop.beansbinding.BeanProperty.create("text"));
         binding.setSourceNullValue("nicht verfügbar");
         binding.setSourceUnreadableValue("");
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(11, 6, 6, 6);
         rpInfo.add(txtLaufendeNr, gridBagConstraints);
 
         lblDescLageplan.setText("Lageplan:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 7;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         rpInfo.add(lblDescLageplan, gridBagConstraints);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${cidsBean.lageplan}"), txtLageplan, org.jdesktop.beansbinding.BeanProperty.create("text"));
         binding.setSourceNullValue("nicht verfügbar");
         binding.setSourceUnreadableValue("");
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 7;
         gridBagConstraints.gridwidth = 7;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         rpInfo.add(txtLageplan, gridBagConstraints);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${cidsBean.loeschungsdatum}"), defaultBindableDateChooser4, org.jdesktop.beansbinding.BeanProperty.create("date"));
         binding.setSourceNullValue(null);
         binding.setSourceUnreadableValue(null);
         binding.setConverter(sqlDateToUtilDateConverter);
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 5;
         gridBagConstraints.gridwidth = 7;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         rpInfo.add(defaultBindableDateChooser4, gridBagConstraints);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${cidsBean.eintragungsdatum}"), defaultBindableDateChooser1, org.jdesktop.beansbinding.BeanProperty.create("date"));
         binding.setSourceNullValue(null);
         binding.setSourceUnreadableValue(null);
         binding.setConverter(sqlDateToUtilDateConverter);
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.gridwidth = 7;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         rpInfo.add(defaultBindableDateChooser1, gridBagConstraints);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${cidsBean.befristungsdatum}"), defaultBindableDateChooser2, org.jdesktop.beansbinding.BeanProperty.create("date"));
         binding.setSourceNullValue(null);
         binding.setSourceUnreadableValue(null);
         binding.setConverter(sqlDateToUtilDateConverter);
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.gridwidth = 7;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         rpInfo.add(defaultBindableDateChooser2, gridBagConstraints);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${cidsBean.geschlossen_am}"), defaultBindableDateChooser3, org.jdesktop.beansbinding.BeanProperty.create("date"));
         binding.setSourceNullValue(null);
         binding.setSourceUnreadableValue(null);
         binding.setConverter(sqlDateToUtilDateConverter);
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.gridwidth = 7;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         rpInfo.add(defaultBindableDateChooser3, gridBagConstraints);
 
         rpHeadInfo.setBackground(java.awt.Color.darkGray);
         rpHeadInfo.setLayout(new java.awt.GridBagLayout());
 
         lblHeadInfo.setForeground(new java.awt.Color(255, 255, 255));
         lblHeadInfo.setText("Info");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(5, 26, 5, 0);
         rpHeadInfo.add(lblHeadInfo, gridBagConstraints);
 
         lblLastInMap.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cids/custom/wunda_blau/res/zoom-best-fit.png"))); // NOI18N
         lblLastInMap.setToolTipText("Flurstücke der laufenden Nummer in Karte anzeigen");
         lblLastInMap.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
         lblLastInMap.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 lblLastInMapMouseClicked(evt);
             }
         });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
         gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
         rpHeadInfo.add(lblLastInMap, gridBagConstraints);
         lblLastInMap.getAccessibleContext().setAccessibleDescription("");
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.gridwidth = 8;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
         rpInfo.add(rpHeadInfo, gridBagConstraints);
 
         lblDescBaulastart.setText("Arten:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 8;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         rpInfo.add(lblDescBaulastart, gridBagConstraints);
 
         scpBaulastart.setMaximumSize(new java.awt.Dimension(1500, 500));
         scpBaulastart.setMinimumSize(new java.awt.Dimension(150, 75));
         scpBaulastart.setPreferredSize(new java.awt.Dimension(150, 75));
 
         eLProperty = org.jdesktop.beansbinding.ELProperty.create("${cidsBean.art}");
         jListBinding = org.jdesktop.swingbinding.SwingBindings.createJListBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, eLProperty, lstBaulastArt);
         bindingGroup.addBinding(jListBinding);
 
         scpBaulastart.setViewportView(lstBaulastArt);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 8;
         gridBagConstraints.gridwidth = 6;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 0.1;
         gridBagConstraints.weighty = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         rpInfo.add(scpBaulastart, gridBagConstraints);
 
         panArtControls.setOpaque(false);
         panArtControls.setLayout(new java.awt.GridBagLayout());
 
         btnAddArt.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cids/custom/objecteditors/wunda_blau/edit_add_mini.png"))); // NOI18N
         btnAddArt.setMaximumSize(new java.awt.Dimension(43, 25));
         btnAddArt.setMinimumSize(new java.awt.Dimension(43, 25));
         btnAddArt.setPreferredSize(new java.awt.Dimension(43, 25));
         btnAddArt.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnAddArtActionPerformed(evt);
             }
         });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 2;
         gridBagConstraints.gridy = 8;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panArtControls.add(btnAddArt, gridBagConstraints);
 
         btnRemoveArt.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cids/custom/objecteditors/wunda_blau/edit_remove_mini.png"))); // NOI18N
         btnRemoveArt.setMaximumSize(new java.awt.Dimension(43, 25));
         btnRemoveArt.setMinimumSize(new java.awt.Dimension(43, 25));
         btnRemoveArt.setPreferredSize(new java.awt.Dimension(43, 25));
         btnRemoveArt.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnRemoveArtActionPerformed(evt);
             }
         });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 2;
         gridBagConstraints.gridy = 9;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panArtControls.add(btnRemoveArt, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 7;
         gridBagConstraints.gridy = 8;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         rpInfo.add(panArtControls, gridBagConstraints);
 
         chkGeprueft.setOpaque(false);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${cidsBean.geprueft}"), chkGeprueft, org.jdesktop.beansbinding.BeanProperty.create("selected"));
         binding.setSourceNullValue(false);
         binding.setSourceUnreadableValue(false);
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 4;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(10, 5, 5, 5);
         rpInfo.add(chkGeprueft, gridBagConstraints);
 
         lblTxtGeprueft.setText("Übereinstimmung mit Akten:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 3;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(10, 5, 5, 5);
         rpInfo.add(lblTxtGeprueft, gridBagConstraints);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${cidsBean.pruefdatum}"), lblLetzteAenderung, org.jdesktop.beansbinding.BeanProperty.create("text"));
         binding.setSourceNullValue("-");
         binding.setSourceUnreadableValue("-");
         binding.setConverter(DATE_TO_STRING);
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 6;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(11, 6, 6, 6);
         rpInfo.add(lblLetzteAenderung, gridBagConstraints);
 
         jLabel1.setText("zuletzt festgestellt am:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 5;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(10, 5, 5, 5);
         rpInfo.add(jLabel1, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 0.5;
         gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
         panMain.add(rpInfo, gridBagConstraints);
 
         add(panMain, java.awt.BorderLayout.CENTER);
 
         bindingGroup.bind();
     }// </editor-fold>//GEN-END:initComponents
     //~ Instance fields --------------------------------------------------------
     private CidsBean cidsBean;
     private Collection<MetaObject> allSelectedObjects;
     private final boolean editable;
     private final Collection<JComponent> editableComponents;
 //    private boolean landParcelListInitialized = false;
     private boolean baulastArtenListInitialized = false;
     private final FlurstueckSelectionDialoge fsDialoge;
 
     //~ Methods ----------------------------------------------------------------
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private MetaObject[] getLWBaulastarten() {
         return ObjectRendererUtils.getLightweightMetaObjectsForQuery(
                 "alb_baulast_art",
                 "select id,baulast_art from alb_baulast_art order by baulast_art",
                 new String[]{"baulast_art"},
                 new AbstractAttributeRepresentationFormater() {
 
                     @Override
                     public String getRepresentation() {
                         return String.valueOf(getAttribute("baulast_art"));
                     }
                 });
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnAddBelastetActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddBelastetActionPerformed
         fsDialoge.setCurrentListToAdd(CidsBeanSupport.getBeanCollectionFromProperty(cidsBean, "flurstuecke_belastet"));
         handleAddFlurstueck(true);
     }//GEN-LAST:event_btnAddBelastetActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnAddBeguenstigtActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddBeguenstigtActionPerformed
         fsDialoge.setCurrentListToAdd(CidsBeanSupport.getBeanCollectionFromProperty(
                 cidsBean,
                 "flurstuecke_beguenstigt"));
         handleAddFlurstueck(false);
     }//GEN-LAST:event_btnAddBeguenstigtActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  belastet  DOCUMENT ME!
      */
     private void handleAddFlurstueck(final boolean belastet) {
         if (belastet) {
             fsDialoge.setTitle("Belastetes Flurstück hinzufügen");
         } else {
             fsDialoge.setTitle("Begünstigtes Flurstück hinzufügen");
         }
         fsDialoge.setVisible(true);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnRemoveBeguenstigtActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveBeguenstigtActionPerformed
         final Object[] selection = lstFlurstueckeBeguenstigt.getSelectedValues();
         if ((selection != null) && (selection.length > 0)) {
             final int answer = JOptionPane.showConfirmDialog(
                     this,
                     "Soll das Flurstück wirklich gelöscht werden?",
                     "Begünstigtes Flurstück entfernen",
                     JOptionPane.YES_NO_OPTION);
             if (answer == JOptionPane.YES_OPTION) {
                 final Collection flurstueckCol = CidsBeanSupport.getBeanCollectionFromProperty(
                         cidsBean,
                         "flurstuecke_beguenstigt");
                 if (flurstueckCol != null) {
                     for (final Object cur : selection) {
                         try {
                             flurstueckCol.remove(cur);
                         } catch (Exception e) {
                             ObjectRendererUtils.showExceptionWindowToUser("Fehler beim Löschen", e, this);
                         }
                     }
                 }
             }
         }
     }//GEN-LAST:event_btnRemoveBeguenstigtActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnRemoveBelastetActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveBelastetActionPerformed
         final Object[] selection = lstFlurstueckeBelastet.getSelectedValues();
         if ((selection != null) && (selection.length > 0)) {
             final int answer = JOptionPane.showConfirmDialog(
                     this,
                     "Soll das Flurstück wirklich gelöscht werden?",
                     "Belastetes Flurstück entfernen",
                     JOptionPane.YES_NO_OPTION);
             if (answer == JOptionPane.YES_OPTION) {
                 final Collection flurstueckCol = CidsBeanSupport.getBeanCollectionFromProperty(
                         cidsBean,
                         "flurstuecke_belastet");
                 if (flurstueckCol != null) {
                     for (final Object cur : selection) {
                         try {
                             flurstueckCol.remove(cur);
                         } catch (Exception e) {
                             ObjectRendererUtils.showExceptionWindowToUser("Fehler beim Löschen", e, this);
                         }
                     }
                 }
             }
         }
     }//GEN-LAST:event_btnRemoveBelastetActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnAddArtActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddArtActionPerformed
         if (!baulastArtenListInitialized) {
             CismetThreadPool.execute(new BaulastArtenComboModelWorker());
         }
         dlgAddBaulastArt.setVisible(true);
     }//GEN-LAST:event_btnAddArtActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnRemoveArtActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveArtActionPerformed
         final Object[] selection = lstBaulastArt.getSelectedValues();
         if ((selection != null) && (selection.length > 0)) {
             final int answer = JOptionPane.showConfirmDialog(
                     this,
                     "Soll die Art wirklich gelöscht werden?",
                     "Art entfernen",
                     JOptionPane.YES_NO_OPTION);
             if (answer == JOptionPane.YES_OPTION) {
                 final Collection artCol = CidsBeanSupport.getBeanCollectionFromProperty(cidsBean, "art");
                 if (artCol != null) {
                     for (final Object cur : selection) {
                         try {
                             artCol.remove(cur);
                         } catch (Exception e) {
                             ObjectRendererUtils.showExceptionWindowToUser("Fehler beim Löschen", e, this);
                         }
                     }
                 }
             }
         }
     }//GEN-LAST:event_btnRemoveArtActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnMenAbort1ActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMenAbort1ActionPerformed
         dlgAddBaulastArt.setVisible(false);
     }//GEN-LAST:event_btnMenAbort1ActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnMenOk1ActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMenOk1ActionPerformed
         final Object selection = cbBaulastArt.getSelectedItem();
         if (selection instanceof LightweightMetaObject) {
             final CidsBean selectedBean = ((LightweightMetaObject) selection).getBean();
             final Collection<CidsBean> colToAdd = CidsBeanSupport.getBeanCollectionFromProperty(cidsBean, "art");
             if (colToAdd != null) {
                 if (!colToAdd.contains(selectedBean)) {
                     colToAdd.add(selectedBean);
                 }
             }
         }
         dlgAddBaulastArt.setVisible(false);
     }//GEN-LAST:event_btnMenOk1ActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void lstFlurstueckeBelastetMouseClicked(final java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lstFlurstueckeBelastetMouseClicked
         if (evt.getClickCount() > 1) {
             handleJumpToListeSelectionBean(lstFlurstueckeBelastet);
         }
     }//GEN-LAST:event_lstFlurstueckeBelastetMouseClicked
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void lstFlurstueckeBeguenstigtMouseClicked(final java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lstFlurstueckeBeguenstigtMouseClicked
         if (evt.getClickCount() > 1) {
             handleJumpToListeSelectionBean(lstFlurstueckeBeguenstigt);
         }
     }//GEN-LAST:event_lstFlurstueckeBeguenstigtMouseClicked
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void lblLastInMapMouseClicked(final java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblLastInMapMouseClicked
         ObjectRendererUtils.switchToCismapMap();
         ObjectRendererUtils.addBeanGeomsAsFeaturesToCismapMap(allSelectedObjects, editable);
     }//GEN-LAST:event_lblLastInMapMouseClicked
 
     /**
      * DOCUMENT ME!
      *
      * @param  list  DOCUMENT ME!
      */
     private void handleJumpToListeSelectionBean(final JList list) {
         final Object selectedObj = list.getSelectedValue();
         if (selectedObj instanceof CidsBean) {
             final Object realFSBean = ((CidsBean) selectedObj).getProperty("fs_referenz");
             if (realFSBean instanceof CidsBean) {
                 final MetaObject selMO = ((CidsBean) realFSBean).getMetaObject();
                 ComponentRegistry.getRegistry().getDescriptionPane().gotoMetaObject(selMO, "");
             }
         }
     }
 
     @Override
     public CidsBean getCidsBean() {
         return cidsBean;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  selection  DOCUMENT ME!
      */
     public void setAllSelectedMetaObjects(final Collection<MetaObject> selection) {
         this.allSelectedObjects = selection;
     }
 
     @Override
     public void setCidsBean(final CidsBean cidsBean) {
         try {
             bindingGroup.unbind();
             if (cidsBean != null) {
                 final int[] belIdx = lstFlurstueckeBelastet.getSelectedIndices();
                 final int[] begIdx = lstFlurstueckeBeguenstigt.getSelectedIndices();
                 final int[] artenIdx = lstBaulastArt.getSelectedIndices();
                 final Collection<MetaObject> selObj = new ArrayList<MetaObject>(1);
                 selObj.add(cidsBean.getMetaObject());
                 setAllSelectedMetaObjects(selObj);
                 this.cidsBean = cidsBean;
                 List<CidsBean> landParcelCol = CidsBeanSupport.getBeanCollectionFromProperty(
                         cidsBean,
                         "flurstuecke_belastet");
                 Collections.sort(landParcelCol, AlphanumComparator.getInstance());
                 landParcelCol = CidsBeanSupport.getBeanCollectionFromProperty(cidsBean, "flurstuecke_beguenstigt");
                 Collections.sort(landParcelCol, AlphanumComparator.getInstance());
 
                 if (!editable) {
                     Object geprueftObj = cidsBean.getProperty("geprueft");
                     if (geprueftObj instanceof Boolean && ((Boolean) geprueftObj)) {
                         lblGeprueft.setIcon(STATUS_OK);
 //                        lblGeprueft.setText("ja");
 //                        lblGeprueft.setForeground(Color.GREEN);
                     } else {
                         lblGeprueft.setIcon(STATUS_FAIL);
 //                        lblGeprueft.setText("nein");
 //                        lblGeprueft.setForeground(Color.RED);
                     }
 
                 }
 
 
                 bindingGroup.bind();
                 lstFlurstueckeBelastet.setSelectedIndices(belIdx);
                 lstFlurstueckeBeguenstigt.setSelectedIndices(begIdx);
                 lstBaulastArt.setSelectedIndices(artenIdx);
             }
         } catch (Exception x) {
             log.error(x, x);
         }
     }
 
     @Override
     public void dispose() {
         dlgAddBaulastArt.dispose();
         fsDialoge.dispose();
         bindingGroup.unbind();
     }
 
     //~ Inner Classes ----------------------------------------------------------
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     class BaulastArtenComboModelWorker extends SwingWorker<ComboBoxModel, Void> {
 
         //~ Constructors -------------------------------------------------------
         /**
          * Creates a new BaulastArtenComboModelWorker object.
          */
         public BaulastArtenComboModelWorker() {
             cbBaulastArt.setModel(waitModel);
             cbBaulastArt.setEnabled(false);
             btnMenOk1.setEnabled(false);
         }
 
         //~ Methods ------------------------------------------------------------
         @Override
         protected ComboBoxModel doInBackground() throws Exception {
             return new DefaultComboBoxModel(getLWBaulastarten());
         }
 
         @Override
         protected void done() {
             try {
                 cbBaulastArt.setModel(get());
                 baulastArtenListInitialized = true;
             } catch (InterruptedException ex) {
                 if (log.isDebugEnabled()) {
                     log.debug(ex, ex);
                 }
             } catch (ExecutionException ex) {
                 log.error(ex, ex);
             } finally {
                 cbBaulastArt.setEnabled(true);
                 btnMenOk1.setEnabled(true);
             }
         }
     }
 
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
             box.setModel(waitModel);
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
                 if (log.isDebugEnabled()) {
                     log.debug(ex, ex);
                 }
             } catch (ExecutionException ex) {
                 log.error(ex, ex);
             } finally {
                 box.setEnabled(true);
                 ObjectRendererUtils.selectAllTextInEditableCombobox(box);
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     static final class ColorJScrollpane extends JScrollPane {
 
         //~ Static fields/initializers -----------------------------------------
         private static final int STRIPE_THICKNESS = 5;
         //~ Instance fields ----------------------------------------------------
         private final Color stripeColor;
 
         //~ Constructors -------------------------------------------------------
         /**
          * Creates a new ColorJScrollpane object.
          */
         public ColorJScrollpane() {
             this.stripeColor = Color.LIGHT_GRAY;
         }
 
         /**
          * Creates a new ColorJScrollpane object.
          *
          * @param  stripeColor  DOCUMENT ME!
          */
         public ColorJScrollpane(final Color stripeColor) {
             this.stripeColor = stripeColor;
         }
 
         //~ Methods ------------------------------------------------------------
         @Override
         public void paint(final Graphics g) {
             final Graphics2D g2d = (Graphics2D) g;
             final Color backupCol = g2d.getColor();
             g2d.setColor(stripeColor);
             g2d.fillRect(0, STRIPE_THICKNESS, STRIPE_THICKNESS, getHeight() - (2 * STRIPE_THICKNESS));
             g2d.setColor(backupCol);
             super.paint(g);
         }
     }
 }
