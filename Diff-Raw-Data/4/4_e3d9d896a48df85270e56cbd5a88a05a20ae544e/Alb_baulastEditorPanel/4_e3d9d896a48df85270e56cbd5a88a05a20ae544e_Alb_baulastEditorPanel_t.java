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
 import de.cismet.cids.custom.objectrenderer.utils.AlphanumComparator;
 import de.cismet.cids.custom.objectrenderer.utils.CidsBeanSupport;
 import de.cismet.cids.custom.objectrenderer.utils.FlurstueckFinder;
 import de.cismet.cids.custom.objectrenderer.utils.ObjectRendererUtils;
 import de.cismet.cids.dynamics.CidsBean;
 import de.cismet.cids.editors.DefaultBindableDateChooser;
 import de.cismet.tools.CismetThreadPool;
 import de.cismet.tools.collections.TypeSafeCollections;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
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
 import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
 
 /**
  *
  * @author srichter
  */
 public class Alb_baulastEditorPanel extends javax.swing.JPanel {
 
     static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Alb_baulastEditorPanel.class);
     private CidsBean cidsBean;
     private final DefaultComboBoxModel NO_SELECTION_MODEL = new DefaultComboBoxModel(new Object[]{});
     private final boolean editable;
     private final Collection<JComponent> editableComponents;
     private List<CidsBean> currentListToAdd;
 //    private boolean landParcelListInitialized = false;
     private boolean baulastArtenListInitialized = false;
 
     /** Creates new form Alb_baulastEditorPanel */
     public Alb_baulastEditorPanel(boolean editable) {
         this.editable = editable;
         this.editableComponents = TypeSafeCollections.newArrayList();
         initComponents();
         initEditableComponents();
         currentListToAdd = null;
         dlgAddLandParcelDiv.pack();
         dlgAddLandParcelDiv.setLocationRelativeTo(this);
         dlgAddBaulastArt.pack();
         dlgAddBaulastArt.setLocationRelativeTo(this);
         AutoCompleteDecorator.decorate(cbBaulastArt);
         CismetThreadPool.execute(new AbstractFlurstueckComboModelWorker(cbParcels1, true) {
 
             @Override
             protected ComboBoxModel doInBackground() throws Exception {
                 return new DefaultComboBoxModel(FlurstueckFinder.getLWGemarkungen());
             }
 
             @Override
             protected void done() {
                 super.done();
 //                cbParcels1.actionPerformed(null);
                 cbParcels1.setSelectedIndex(0);
                 cbParcels1.requestFocusInWindow();
                 ObjectRendererUtils.selectAllTextInEditableCombobox(cbParcels1);
             }
         });
     }
 
     private final void initEditableComponents() {
         editableComponents.add(txtLageplan);
         editableComponents.add(txtLaufendeNr);
         editableComponents.add(txtTextblatt);
         editableComponents.add(defaultBindableDateChooser1);
         editableComponents.add(defaultBindableDateChooser2);
         editableComponents.add(defaultBindableDateChooser3);
         editableComponents.add(defaultBindableDateChooser4);
 //        editableComponents.add(lstFlurstueckeBeguenstigt);
 //        editableComponents.add(lstFlurstueckeBelastet);
         for (final JComponent editableComponent : editableComponents) {
             editableComponent.setOpaque(editable);
             if (!editable) {
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
 
     /** Creates new form Alb_baulastEditorPanel */
     public Alb_baulastEditorPanel() {
         this(true);
     }
 
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
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
         dlgAddLandParcelDiv = new javax.swing.JDialog();
         panAddLandParcel1 = new javax.swing.JPanel();
         lblFlurstueckAuswaehlen = new javax.swing.JLabel();
         cbParcels1 = new javax.swing.JComboBox();
         panMenButtons2 = new javax.swing.JPanel();
         btnFlurstueckAddMenCancel = new javax.swing.JButton();
         btnFlurstueckAddMenOk = new javax.swing.JButton();
         cbParcels2 = new javax.swing.JComboBox(NO_SELECTION_MODEL);
         cbParcels3 = new javax.swing.JComboBox(NO_SELECTION_MODEL);
         lblGemarkung = new javax.swing.JLabel();
         lblFlur = new javax.swing.JLabel();
         lblFlurstueck = new javax.swing.JLabel();
         lblGemarkungsname = new javax.swing.JLabel();
         jSeparator1 = new javax.swing.JSeparator();
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
         lblDescBaulastart = new javax.swing.JLabel();
         scpBaulastart = new javax.swing.JScrollPane();
         lstBaulastArt = new javax.swing.JList();
         panArtControls = new javax.swing.JPanel();
         btnAddArt = new javax.swing.JButton();
         btnRemoveArt = new javax.swing.JButton();
 
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
 
         dlgAddLandParcelDiv.setTitle("Flurstück hinzufügen");
         dlgAddLandParcelDiv.setMinimumSize(new java.awt.Dimension(380, 120));
         dlgAddLandParcelDiv.setModal(true);
 
         panAddLandParcel1.setMaximumSize(new java.awt.Dimension(180, 180));
         panAddLandParcel1.setMinimumSize(new java.awt.Dimension(180, 180));
         panAddLandParcel1.setPreferredSize(new java.awt.Dimension(180, 180));
         panAddLandParcel1.setLayout(new java.awt.GridBagLayout());
 
         lblFlurstueckAuswaehlen.setFont(new java.awt.Font("Tahoma", 1, 11));
         lblFlurstueckAuswaehlen.setText("Bitte Flurstück auswählen:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridwidth = 4;
         gridBagConstraints.insets = new java.awt.Insets(15, 10, 20, 10);
         panAddLandParcel1.add(lblFlurstueckAuswaehlen, gridBagConstraints);
 
         cbParcels1.setEditable(true);
         cbParcels1.setMaximumSize(new java.awt.Dimension(100, 18));
         cbParcels1.setMinimumSize(new java.awt.Dimension(100, 18));
         cbParcels1.setPreferredSize(new java.awt.Dimension(100, 18));
         cbParcels1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cbParcels1ActionPerformed(evt);
             }
         });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.weightx = 0.33;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panAddLandParcel1.add(cbParcels1, gridBagConstraints);
 
         panMenButtons2.setLayout(new java.awt.GridBagLayout());
 
         btnFlurstueckAddMenCancel.setText("Abbrechen");
         btnFlurstueckAddMenCancel.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnFlurstueckAddMenCancelActionPerformed(evt);
             }
         });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panMenButtons2.add(btnFlurstueckAddMenCancel, gridBagConstraints);
 
         btnFlurstueckAddMenOk.setText("Ok");
         btnFlurstueckAddMenOk.setMaximumSize(new java.awt.Dimension(85, 23));
         btnFlurstueckAddMenOk.setMinimumSize(new java.awt.Dimension(85, 23));
         btnFlurstueckAddMenOk.setPreferredSize(new java.awt.Dimension(85, 23));
         btnFlurstueckAddMenOk.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnFlurstueckAddMenOkActionPerformed(evt);
             }
         });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panMenButtons2.add(btnFlurstueckAddMenOk, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 5;
         gridBagConstraints.gridwidth = 4;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panAddLandParcel1.add(panMenButtons2, gridBagConstraints);
 
         cbParcels2.setEditable(true);
         cbParcels2.setEnabled(false);
         cbParcels2.setMaximumSize(new java.awt.Dimension(100, 18));
         cbParcels2.setMinimumSize(new java.awt.Dimension(100, 18));
         cbParcels2.setPreferredSize(new java.awt.Dimension(100, 18));
         cbParcels2.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cbParcels2ActionPerformed(evt);
             }
         });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.weightx = 0.33;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panAddLandParcel1.add(cbParcels2, gridBagConstraints);
 
         cbParcels3.setEditable(true);
         cbParcels3.setEnabled(false);
         cbParcels3.setMaximumSize(new java.awt.Dimension(100, 18));
         cbParcels3.setMinimumSize(new java.awt.Dimension(100, 18));
         cbParcels3.setPreferredSize(new java.awt.Dimension(100, 18));
         cbParcels3.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cbParcels3ActionPerformed(evt);
             }
         });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 2;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.weightx = 0.33;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panAddLandParcel1.add(cbParcels3, gridBagConstraints);
 
         lblGemarkung.setText("Gemarkung");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panAddLandParcel1.add(lblGemarkung, gridBagConstraints);
 
         lblFlur.setText("Flur");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panAddLandParcel1.add(lblFlur, gridBagConstraints);
 
         lblFlurstueck.setText("Flurstück");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 2;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panAddLandParcel1.add(lblFlurstueck, gridBagConstraints);
 
         lblGemarkungsname.setText(" ");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panAddLandParcel1.add(lblGemarkungsname, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.gridwidth = 4;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
         panAddLandParcel1.add(jSeparator1, gridBagConstraints);
 
         dlgAddLandParcelDiv.getContentPane().add(panAddLandParcel1, java.awt.BorderLayout.CENTER);
 
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
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
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
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
         rpInfo.add(txtTextblatt, gridBagConstraints);
 
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
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
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
         gridBagConstraints.gridwidth = 2;
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
         gridBagConstraints.gridwidth = 2;
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
         gridBagConstraints.gridwidth = 2;
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
         gridBagConstraints.gridwidth = 2;
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
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         rpInfo.add(defaultBindableDateChooser3, gridBagConstraints);
 
         rpHeadInfo.setBackground(java.awt.Color.darkGray);
         rpHeadInfo.setLayout(new java.awt.GridBagLayout());
 
         lblHeadInfo.setForeground(new java.awt.Color(255, 255, 255));
         lblHeadInfo.setText("Info");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         rpHeadInfo.add(lblHeadInfo, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.gridwidth = 3;
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
         gridBagConstraints.gridx = 2;
         gridBagConstraints.gridy = 8;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         rpInfo.add(panArtControls, gridBagConstraints);
 
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
 
     private final MetaObject[] getLWBaulastarten() {
         return ObjectRendererUtils.getLightweightMetaObjectsForQuery("alb_baulast_art", "select id,baulast_art from alb_baulast_art order by baulast_art", new String[]{"baulast_art"}, new AbstractAttributeRepresentationFormater() {
 
             @Override
             public String getRepresentation() {
                 return String.valueOf(getAttribute("baulast_art"));
             }
         });
     }
 
     private void btnAddBelastetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddBelastetActionPerformed
         currentListToAdd = CidsBeanSupport.getBeanCollectionFromProperty(cidsBean, "flurstuecke_belastet");
         handleAddFlurstueck();
     }//GEN-LAST:event_btnAddBelastetActionPerformed
 
     private void btnAddBeguenstigtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddBeguenstigtActionPerformed
         currentListToAdd = CidsBeanSupport.getBeanCollectionFromProperty(cidsBean, "flurstuecke_beguenstigt");
         handleAddFlurstueck();
     }//GEN-LAST:event_btnAddBeguenstigtActionPerformed
 
     private final void handleAddFlurstueck() {
         btnFlurstueckAddMenOk.setEnabled(false);
         dlgAddLandParcelDiv.setVisible(true);
     }
 
     private void btnRemoveBeguenstigtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveBeguenstigtActionPerformed
         final Object[] selection = lstFlurstueckeBeguenstigt.getSelectedValues();
         if (selection != null && selection.length > 0) {
             final int answer = JOptionPane.showConfirmDialog(this, "Soll das Flurstück wirklich gelöscht werden?", "Begünstigtes Flurstück entfernen", JOptionPane.YES_NO_OPTION);
             if (answer == JOptionPane.YES_OPTION) {
                 final Collection flurstueckCol = CidsBeanSupport.getBeanCollectionFromProperty(cidsBean, "flurstuecke_beguenstigt");
                 if (flurstueckCol != null) {
                     for (Object cur : selection) {
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
 
     private void btnRemoveBelastetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveBelastetActionPerformed
         final Object[] selection = lstFlurstueckeBelastet.getSelectedValues();
         if (selection != null && selection.length > 0) {
             final int answer = JOptionPane.showConfirmDialog(this, "Soll das Flurstück wirklich gelöscht werden?", "Belastetes Flurstück entfernen", JOptionPane.YES_NO_OPTION);
             if (answer == JOptionPane.YES_OPTION) {
                 final Collection flurstueckCol = CidsBeanSupport.getBeanCollectionFromProperty(cidsBean, "flurstuecke_belastet");
                 if (flurstueckCol != null) {
                     for (Object cur : selection) {
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
 
     private void btnAddArtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddArtActionPerformed
         if (!baulastArtenListInitialized) {
             CismetThreadPool.execute(new BaulastArtenComboModelWorker());
         }
         dlgAddBaulastArt.setVisible(true);
     }//GEN-LAST:event_btnAddArtActionPerformed
 
     private void btnRemoveArtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveArtActionPerformed
         final Object[] selection = lstBaulastArt.getSelectedValues();
         if (selection != null && selection.length > 0) {
             final int answer = JOptionPane.showConfirmDialog(this, "Soll die Art wirklich gelöscht werden?", "Art entfernen", JOptionPane.YES_NO_OPTION);
             if (answer == JOptionPane.YES_OPTION) {
                 final Collection artCol = CidsBeanSupport.getBeanCollectionFromProperty(cidsBean, "art");
                 if (artCol != null) {
                     for (Object cur : selection) {
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
 
     private void btnMenAbort1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMenAbort1ActionPerformed
         dlgAddBaulastArt.setVisible(false);
     }//GEN-LAST:event_btnMenAbort1ActionPerformed
 
     private void btnMenOk1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMenOk1ActionPerformed
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
 
     private void lstFlurstueckeBelastetMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lstFlurstueckeBelastetMouseClicked
         if (evt.getClickCount() > 1) {
             handleJumpToListeSelectionBean(lstFlurstueckeBelastet);
         }
     }//GEN-LAST:event_lstFlurstueckeBelastetMouseClicked
 
     private void btnFlurstueckAddMenCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFlurstueckAddMenCancelActionPerformed
         dlgAddLandParcelDiv.setVisible(false);
     }//GEN-LAST:event_btnFlurstueckAddMenCancelActionPerformed
 
     private void btnFlurstueckAddMenOkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFlurstueckAddMenOkActionPerformed
         final Object selection = cbParcels3.getSelectedItem();
         if (selection instanceof LightweightMetaObject) {
             final CidsBean selectedBean = ((LightweightMetaObject) selection).getBean();
             if (currentListToAdd != null) {
                 int position = Collections.binarySearch(currentListToAdd, selectedBean, AlphanumComparator.getInstance());
                 if (position < 0) {
                     currentListToAdd.add(-position - 1, selectedBean);
                 }
             }
         } else if (selection instanceof String) {
             int result = JOptionPane.showConfirmDialog(this, "Das Flurstück befindet sich nicht im Datenbestand der aktuellen Flurstücke. Soll es als historisch angelegt werden?", "Historisches Flurstück anlegen", JOptionPane.YES_NO_OPTION);
             if (result == JOptionPane.YES_OPTION) {
                 CidsBean beanToAdd = landParcelBeanFromComboBoxes(selection.toString());
                 if (beanToAdd != null) {
                     int position = Collections.binarySearch(currentListToAdd, beanToAdd, AlphanumComparator.getInstance());
                     if (position < 0) {
                         try {
                             if (MetaObject.NEW == beanToAdd.getMetaObject().getStatus()) {
                                 beanToAdd = beanToAdd.persist();
                             }
                             currentListToAdd.add(-position - 1, beanToAdd);
                         } catch (Exception ex) {
                             log.error(ex, ex);
                         }
                     }
                 }
             }
             currentListToAdd = null;
         }
         dlgAddLandParcelDiv.setVisible(false);
     }//GEN-LAST:event_btnFlurstueckAddMenOkActionPerformed
 //    private final Map<String, CidsBean> unpersistedHistoricLandparcels = TypeSafeCollections.newHashMap();
 
     private CidsBean landParcelBeanFromComboBoxes(String zaehlerNenner) {
         int result = JOptionPane.YES_OPTION;
         try {
             final Map<String, Object> newLandParcelProperties = TypeSafeCollections.newHashMap();
             final String gemarkung = String.valueOf(cbParcels1.getSelectedItem());
             final String flur = String.valueOf(cbParcels2.getSelectedItem());
             if (flur.length() != 3) {
                 result = JOptionPane.showConfirmDialog(this, "Das neue Flurstück entspricht nicht der Namenskonvention: Flur sollte dreistellig sein (mit führenden Nullen, z.B. 007). Datensatz trotzdem abspeichern?", "Warnung: Format", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
             }
             if (result == JOptionPane.YES_OPTION) {
                 final String[] zaehlerNennerTiles = zaehlerNenner.split("/");
                 final String zaehler = zaehlerNennerTiles[0];
                 newLandParcelProperties.put(FlurstueckFinder.FLURSTUECK_GEMARKUNG, Integer.valueOf(gemarkung));
                 newLandParcelProperties.put(FlurstueckFinder.FLURSTUECK_FLUR, flur);
                 newLandParcelProperties.put(FlurstueckFinder.FLURSTUECK_ZAEHLER, zaehler);
                 String nenner = "0";
                 if (zaehlerNennerTiles.length == 2) {
                     nenner = zaehlerNennerTiles[1];
                 }
                 newLandParcelProperties.put(FlurstueckFinder.FLURSTUECK_NENNER, nenner);
                 //the following code tries to avoid the creation of multiple entries for the same landparcel.
                 //however, there *might* be a chance that a historic landparcel is created multiple times when more then
                 //one client creates the same parcel at the "same time".
                 MetaObject[] searchResult = FlurstueckFinder.getLWLandparcel(gemarkung, flur, zaehler, nenner);
                 if (searchResult != null && searchResult.length > 0) {
                     return searchResult[0].getBean();
                 } else {
 //                    final String compountParcelData = gemarkung + "-" + flur + "-" + zaehler + "/" + nenner;
 //                    CidsBean newBean = unpersistedHistoricLandparcels.get(compountParcelData);
 //                    if (newBean == null) {
                     CidsBean newBean = CidsBeanSupport.createNewCidsBeanFromTableName(FlurstueckFinder.FLURSTUECK_TABLE_NAME, newLandParcelProperties);
 //                        unpersistedHistoricLandparcels.put(compountParcelData, newBean);
 //                    }
                     return newBean;
                 }
             }
         } catch (Exception ex) {
             log.error(ex, ex);
         }
         return null;
     }
     private static final String CB_EDITED_ACTION_COMMAND = "comboBoxEdited";
 
     private void cbParcels1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbParcels1ActionPerformed
         Object selection = cbParcels1.getSelectedItem();
         cbParcels3.setEnabled(false);
         btnFlurstueckAddMenOk.setEnabled(false);
         if (selection instanceof LightweightMetaObject) {
             final LightweightMetaObject lwmo = (LightweightMetaObject) selection;
             final String selGemarkungsNr = String.valueOf(selection);
             CismetThreadPool.execute(new AbstractFlurstueckComboModelWorker(cbParcels2, CB_EDITED_ACTION_COMMAND.equals(evt.getActionCommand())) {
 
                 @Override
                 protected ComboBoxModel doInBackground() throws Exception {
                     return new DefaultComboBoxModel(FlurstueckFinder.getLWFlure(selGemarkungsNr));
                 }
             });
             String gemarkungsname = String.valueOf(lwmo.getLWAttribute(FlurstueckFinder.GEMARKUNG_NAME));
             lblGemarkungsname.setText("(" + gemarkungsname + ")");
             cbParcels1.getEditor().getEditorComponent().setBackground(Color.WHITE);
         } else {
             final int foundBeanIndex = ObjectRendererUtils.findComboBoxItemForString(cbParcels1, String.valueOf(selection));
             if (foundBeanIndex < 0) {
                 cbParcels2.setModel(new DefaultComboBoxModel());
                 try {
                     Integer.parseInt(String.valueOf(selection));
                     cbParcels1.getEditor().getEditorComponent().setBackground(Color.YELLOW);
                     cbParcels2.setEnabled(true);
                     if (CB_EDITED_ACTION_COMMAND.equals(evt.getActionCommand())) {
                         cbParcels2.requestFocusInWindow();
                     }
                 } catch (Exception notANumberEx) {
                     log.debug(selection + " is not a number!", notANumberEx);
                     cbParcels2.setEnabled(false);
                     cbParcels1.getEditor().getEditorComponent().setBackground(Color.RED);
                     lblGemarkungsname.setText("(Ist keine Zahl)");
                 }
                 lblGemarkungsname.setText(" ");
             } else {
                 cbParcels1.setSelectedIndex(foundBeanIndex);
                 cbParcels2.getEditor().getEditorComponent().setBackground(Color.WHITE);
                 cbParcels3.getEditor().getEditorComponent().setBackground(Color.WHITE);
             }
         }
     }//GEN-LAST:event_cbParcels1ActionPerformed
 
     private void cbParcels2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbParcels2ActionPerformed
         Object selection = cbParcels2.getSelectedItem();
         if (selection instanceof MetaObject) {
             final String selGem = String.valueOf(cbParcels1.getSelectedItem());
             final StringBuffer selFlurNr = new StringBuffer(String.valueOf(cbParcels2.getSelectedItem()));
             btnFlurstueckAddMenOk.setEnabled(false);
             cbParcels2.getEditor().getEditorComponent().setBackground(Color.WHITE);
             CismetThreadPool.execute(new AbstractFlurstueckComboModelWorker(cbParcels3, CB_EDITED_ACTION_COMMAND.equals(evt.getActionCommand())) {
 
                 @Override
                 protected ComboBoxModel doInBackground() throws Exception {
                     return new DefaultComboBoxModel(FlurstueckFinder.getLWFurstuecksZaehlerNenner(selGem, selFlurNr.toString()));
                 }
             });
         } else {
             final int foundBeanIndex = ObjectRendererUtils.findComboBoxItemForString(cbParcels2, String.valueOf(selection));
             if (foundBeanIndex < 0) {
                 cbParcels2.getEditor().getEditorComponent().setBackground(Color.YELLOW);
                 cbParcels3.setModel(new DefaultComboBoxModel());
                 cbParcels3.setEnabled(true);
                 if (CB_EDITED_ACTION_COMMAND.equals(evt.getActionCommand())) {
                     cbParcels3.requestFocusInWindow();
                 }
             } else {
                 cbParcels2.setSelectedIndex(foundBeanIndex);
                 cbParcels3.getEditor().getEditorComponent().setBackground(Color.WHITE);
             }
         }
     }//GEN-LAST:event_cbParcels2ActionPerformed
 
     private boolean checkFlurstueckSelectionComplete() {
         if (cbParcels2.isEnabled() && cbParcels3.isEnabled()) {
             Object sel2 = cbParcels2.getSelectedItem();
             Object sel3 = cbParcels3.getSelectedItem();
             if (sel2 != null && sel3 != null) {
                 if (sel2.toString().length() > 0 && sel3.toString().length() > 0) {
                     return true;
                 }
             }
         }
         return false;
     }
 
     private void cbParcels3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbParcels3ActionPerformed
         btnFlurstueckAddMenOk.setEnabled(checkFlurstueckSelectionComplete());
         if (CB_EDITED_ACTION_COMMAND.equals(evt.getActionCommand())) {
             btnFlurstueckAddMenOk.requestFocusInWindow();
         }
         Component editor = cbParcels3.getEditor().getEditorComponent();
         if (cbParcels3.getSelectedItem() instanceof MetaObject) {
             editor.setBackground(Color.WHITE);
         } else {
             String parcelNo = String.valueOf(cbParcels3.getSelectedItem());
             if (!parcelNo.contains("/")) {
                 parcelNo += "/0";
                 if (editor instanceof JTextField) {
                     JTextField textEditor = (JTextField) editor;
                     textEditor.setText(parcelNo);
                 }
             }
             final int foundBeanIndex = ObjectRendererUtils.findComboBoxItemForString(cbParcels3, parcelNo);
             if (foundBeanIndex < 0) {
                 cbParcels3.getEditor().getEditorComponent().setBackground(Color.YELLOW);
             } else {
                 cbParcels3.setSelectedIndex(foundBeanIndex);
             }
         }
     }//GEN-LAST:event_cbParcels3ActionPerformed
 
     private void lstFlurstueckeBeguenstigtMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lstFlurstueckeBeguenstigtMouseClicked
         if (evt.getClickCount() > 1) {
             handleJumpToListeSelectionBean(lstFlurstueckeBeguenstigt);
         }
 }//GEN-LAST:event_lstFlurstueckeBeguenstigtMouseClicked
 
     private final void handleJumpToListeSelectionBean(JList list) {
         final Object selectedObj = list.getSelectedValue();
         if (selectedObj instanceof CidsBean) {
             Object realFSBean = ((CidsBean) selectedObj).getProperty("fs_referenz");
             if (realFSBean instanceof CidsBean) {
                 final MetaObject selMO = ((CidsBean) realFSBean).getMetaObject();
                 ComponentRegistry.getRegistry().getDescriptionPane().gotoMetaObject(selMO, "");
             }
         }
     }
 
     public CidsBean getCidsBean() {
         return cidsBean;
     }
 
     public void setCidsBean(CidsBean cidsBean) {
         if (cidsBean != null) {
             this.cidsBean = cidsBean;
             List<CidsBean> landParcelCol = CidsBeanSupport.getBeanCollectionFromProperty(cidsBean, "flurstuecke_belastet");
             Collections.sort(landParcelCol, AlphanumComparator.getInstance());
             landParcelCol = CidsBeanSupport.getBeanCollectionFromProperty(cidsBean, "flurstuecke_beguenstigt");
             Collections.sort(landParcelCol, AlphanumComparator.getInstance());
             bindingGroup.unbind();
             bindingGroup.bind();
         }
     }
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton btnAddArt;
     private javax.swing.JButton btnAddBeguenstigt;
     private javax.swing.JButton btnAddBelastet;
     private javax.swing.JButton btnFlurstueckAddMenCancel;
     private javax.swing.JButton btnFlurstueckAddMenOk;
     private javax.swing.JButton btnMenAbort1;
     private javax.swing.JButton btnMenOk1;
     private javax.swing.JButton btnRemoveArt;
     private javax.swing.JButton btnRemoveBeguenstigt;
     private javax.swing.JButton btnRemoveBelastet;
     private javax.swing.JComboBox cbBaulastArt;
     private javax.swing.JComboBox cbParcels1;
     private javax.swing.JComboBox cbParcels2;
     private javax.swing.JComboBox cbParcels3;
     private de.cismet.cids.editors.DefaultBindableDateChooser defaultBindableDateChooser1;
     private de.cismet.cids.editors.DefaultBindableDateChooser defaultBindableDateChooser2;
     private de.cismet.cids.editors.DefaultBindableDateChooser defaultBindableDateChooser3;
     private de.cismet.cids.editors.DefaultBindableDateChooser defaultBindableDateChooser4;
     private javax.swing.JDialog dlgAddBaulastArt;
     private javax.swing.JDialog dlgAddLandParcelDiv;
     private javax.swing.JSeparator jSeparator1;
     private javax.swing.JLabel lblDescBaulastart;
     private javax.swing.JLabel lblDescBefristungsdatum;
     private javax.swing.JLabel lblDescEintragungsdatum;
     private javax.swing.JLabel lblDescGeschlossenAm;
     private javax.swing.JLabel lblDescLageplan;
     private javax.swing.JLabel lblDescLaufendeNr;
     private javax.swing.JLabel lblDescLoeschungsdatum;
     private javax.swing.JLabel lblDescTextblatt;
     private javax.swing.JLabel lblFlur;
     private javax.swing.JLabel lblFlurstueck;
     private javax.swing.JLabel lblFlurstueckAuswaehlen;
     private javax.swing.JLabel lblGemarkung;
     private javax.swing.JLabel lblGemarkungsname;
     private javax.swing.JLabel lblHeadBegFlurstuecke;
     private javax.swing.JLabel lblHeadBelFlurstuecke;
     private javax.swing.JLabel lblHeadInfo;
     private javax.swing.JLabel lblSuchwortEingeben1;
     private javax.swing.JList lstBaulastArt;
     private javax.swing.JList lstFlurstueckeBeguenstigt;
     private javax.swing.JList lstFlurstueckeBelastet;
     private javax.swing.JPanel panAddBaulastArt;
     private javax.swing.JPanel panAddLandParcel1;
     private javax.swing.JPanel panArtControls;
     private javax.swing.JPanel panControlsFSBeg;
     private javax.swing.JPanel panControlsFSBel;
     private javax.swing.JPanel panMain;
     private javax.swing.JPanel panMenButtons1;
     private javax.swing.JPanel panMenButtons2;
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
     private static final ComboBoxModel waitModel = new DefaultComboBoxModel(new String[]{"Wird geladen..."});
 
 //    class FlurstueckComboModelWorker extends SwingWorker<ComboBoxModel, Void> {
 //
 //        public FlurstueckComboModelWorker() {
 //            cbLandParcels.setModel(waitModel);
 //            cbLandParcels.setEnabled(false);
 //            btnMenOk.setEnabled(false);
 //        }
 //
 //        @Override
 //        protected ComboBoxModel doInBackground() throws Exception {
 //            return new DefaultComboBoxModel(getLWLandparcels());
 //        }
 //
 //        @Override
 //        protected void done() {
 //            try {
 //                cbLandParcels.setModel(get());
 ////                landParcelListInitialized = true;
 //            } catch (InterruptedException ex) {
 //                log.debug(ex, ex);
 //            } catch (ExecutionException ex) {
 //                log.error(ex, ex);
 //            } finally {
 //                cbLandParcels.setEnabled(true);
 //                btnMenOk.setEnabled(true);
 //            }
 //        }
 //    }
     class BaulastArtenComboModelWorker extends SwingWorker<ComboBoxModel, Void> {
 
         public BaulastArtenComboModelWorker() {
             cbBaulastArt.setModel(waitModel);
             cbBaulastArt.setEnabled(false);
             btnMenOk1.setEnabled(false);
         }
 
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
                 log.debug(ex, ex);
             } catch (ExecutionException ex) {
                 log.error(ex, ex);
             } finally {
                 cbBaulastArt.setEnabled(true);
                 btnMenOk1.setEnabled(true);
             }
         }
     }
 
     abstract class AbstractFlurstueckComboModelWorker extends SwingWorker<ComboBoxModel, Void> {
 
         private final JComboBox box;
         private final boolean switchToBox;
 
         public AbstractFlurstueckComboModelWorker(JComboBox box, boolean switchToBox) {
             this.box = box;
             this.switchToBox = switchToBox;
             box.setVisible(true);
             box.setEnabled(false);
             box.setModel(waitModel);
         }
 
         @Override
         protected void done() {
             try {
                 box.setModel(get());
                 if (switchToBox) {
                     box.requestFocus();
                 }
             } catch (InterruptedException ex) {
                 log.debug(ex, ex);
             } catch (ExecutionException ex) {
                 log.error(ex, ex);
             } finally {
                 box.setEnabled(true);
                 ObjectRendererUtils.selectAllTextInEditableCombobox(box);
             }
         }
     }
 
     static final class ColorJScrollpane extends JScrollPane {
 
         private static final int STRIPE_THICKNESS = 5;
 
         public ColorJScrollpane() {
             this.stripeColor = Color.LIGHT_GRAY;
         }
 
         public ColorJScrollpane(Color stripeColor) {
             this.stripeColor = stripeColor;
         }
         private final Color stripeColor;
 
         @Override
         public void paint(Graphics g) {
             final Graphics2D g2d = (Graphics2D) g;
             final Color backupCol = g2d.getColor();
             g2d.setColor(stripeColor);
             g2d.fillRect(0, STRIPE_THICKNESS, STRIPE_THICKNESS, getHeight() - 2 * STRIPE_THICKNESS);
             g2d.setColor(backupCol);
             super.paint(g);
         }
     }
 }
