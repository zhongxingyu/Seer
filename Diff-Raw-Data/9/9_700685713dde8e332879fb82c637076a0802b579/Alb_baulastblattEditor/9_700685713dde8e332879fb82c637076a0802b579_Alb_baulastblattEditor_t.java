 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 /*
  * CoolThemaRenderer.java
  *
  * Created on 10. November 3508, 11:56
  */
 package de.cismet.cids.custom.objecteditors.wunda_blau;
 
 import Sirius.navigator.connection.SessionManager;
 import Sirius.navigator.ui.RequestsFullSizeComponent;
 
 import Sirius.server.middleware.types.MetaObject;
 
 import org.jdesktop.beansbinding.Converter;
 
 import org.openide.util.WeakListeners;
 
 import java.awt.CardLayout;
 import java.awt.Component;
 import java.awt.event.MouseListener;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 
 import java.sql.Date;
 
 import java.text.DateFormat;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.swing.JComponent;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.border.Border;
 import javax.swing.border.EmptyBorder;
 
 import de.cismet.cids.annotations.AggregationRenderer;
 
 import de.cismet.cids.custom.objectrenderer.utils.AlphanumComparator;
 import de.cismet.cids.custom.objectrenderer.utils.CidsBeanSupport;
 import de.cismet.cids.custom.objectrenderer.utils.ObjectRendererUtils;
 
 import de.cismet.cids.dynamics.CidsBean;
 import de.cismet.cids.dynamics.DisposableCidsBeanStore;
 
 import de.cismet.cids.editors.DefaultBeanInitializer;
 import de.cismet.cids.editors.EditorBeanInitializerStore;
 import de.cismet.cids.editors.EditorClosedEvent;
 import de.cismet.cids.editors.EditorSaveListener;
 
 import de.cismet.tools.collections.TypeSafeCollections;
 
 import de.cismet.tools.gui.BorderProvider;
 import de.cismet.tools.gui.FooterComponentProvider;
 import de.cismet.tools.gui.StaticSwingTools;
 import de.cismet.tools.gui.TitleComponentProvider;
 
 /**
  * de.cismet.cids.objectrenderer.CoolThemaRenderer.
  *
  * <p>Renderer for the "Thema"-theme</p>
  *
  * @author   srichter
  * @version  $Revision$, $Date$
  */
 @AggregationRenderer
 public class Alb_baulastblattEditor extends JPanel implements DisposableCidsBeanStore,
     TitleComponentProvider,
     FooterComponentProvider,
     BorderProvider,
     RequestsFullSizeComponent,
     EditorSaveListener {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final int BLATT_NUMMER_ANZAHL_ZIFFERN = 6;
     static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Alb_baulastblattEditor.class);
     private static final Comparator<Object> OBJECT_COMPARATOR = new Comparator<Object>() {
 
             @Override
             public int compare(final Object o1, final Object o2) {
                 return AlphanumComparator.getInstance().compare(String.valueOf(o1), String.valueOf(o2));
             }
         };
 
     public static final String TITLE_PREFIX = "Baulastblatt";
     public static final String TITLE_AGR_PREFIX = "Baulastblätter";
     private static final String FLURSTUECKE_BELASTET = "flurstuecke_belastet";
     private static final String FLURSTUECKE_BEGUENSTIGT = "flurstuecke_beguenstigt";
     private static final String ACTION_TAG = "custom.baulast.document@WUNDA_BLAU";
 
     private static final Converter<java.sql.Date, String> DATE_TO_STRING = new Converter<Date, String>() {
 
             @Override
             public String convertForward(final Date value) {
                 if (value != null) {
                     return DateFormat.getDateInstance().format(value);
                 } else {
                     return "";
                 }
             }
 
             @Override
             public Date convertReverse(final String value) {
                 throw new UnsupportedOperationException("Not supported yet.");
             }
         };
 
     //~ Instance fields --------------------------------------------------------
 
 // private static final Validator<String> NUMBER_VALIDATOR = new Validator<String>() {
 //
 // @Override
 // public Result validate(String t) {
 // if (t.length() < BLATT_NUMMER_ANZAHL_ZIFFERN) {
 // return new Result(Result.ERROR, "Blattnummer ist nicht " + BLATT_NUMMER_ANZAHL_ZIFFERN + "-stellig!");
 // } else if (!t.matches("^\\d$")) {
 // return new Result(Result.ERROR, "Blattnummer darf nur aus Ziffern [0-9] bestehen!");
 // }
 // return null;
 // }
 // };
     private final boolean editable;
     private CidsBean cidsBean;
     private final CardLayout cardLayout;
     private Collection<CidsBean> baulastenBeans = null;
 //    private Collection<CidsBean> baulastenBeansToDelete = new ArrayList<CidsBean>();
     private PropertyChangeListener strongReferenceToWeakListener = null;
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private de.cismet.cids.custom.objecteditors.wunda_blau.Alb_picturePanel alb_picturePanel;
     private javax.swing.JButton btnAddLaufendeNummer;
     private javax.swing.JButton btnBack;
     private javax.swing.JButton btnCopyBaulast;
     private javax.swing.JButton btnForward;
     private javax.swing.JButton btnPasteBaulast;
     private javax.swing.JButton btnRemoveLaufendeNummer;
     private javax.swing.Box.Filler filler1;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JLabel jLabel3;
     private javax.swing.JPanel jPanel1;
     private javax.swing.JPanel jPanel2;
     private javax.swing.JPanel jPanel3;
     private javax.swing.JLabel lblBack;
     private javax.swing.JLabel lblBearbeiter;
     private javax.swing.JLabel lblBearbeitetAm;
     private javax.swing.JLabel lblBlattInMap;
     private javax.swing.JLabel lblBlattnummer;
     private javax.swing.JLabel lblDurch;
     private javax.swing.JLabel lblForw;
     private javax.swing.JLabel lblLetzteBearbeitung;
     private javax.swing.JLabel lblTitle;
     private javax.swing.JList lstLaufendeNummern;
     private de.cismet.cids.custom.objecteditors.wunda_blau.Alb_baulastEditorPanel panBaulastEditor;
     private de.cismet.tools.gui.RoundedPanel panBlattNummer;
     private javax.swing.JPanel panButtons;
     private javax.swing.JPanel panControlsLaufendeNummern;
     private javax.swing.JPanel panFooter;
     private javax.swing.JPanel panFooterLeft;
     private javax.swing.JPanel panFooterRight;
     private javax.swing.JPanel panMain;
     private javax.swing.JPanel panTitle;
     private de.cismet.tools.gui.RoundedPanel rpLaufendeNummern;
     private javax.swing.JScrollPane scpLaufendeNummern;
     private de.cismet.tools.gui.SemiRoundedPanel semiRoundedPanel1;
     private de.cismet.tools.gui.SemiRoundedPanel semiRoundedPanel2;
     private de.cismet.cids.editors.converters.SqlDateToUtilDateConverter sqlDateToUtilDateConverter;
     private javax.swing.JTextField txtBlattnummer;
     private org.jdesktop.beansbinding.BindingGroup bindingGroup;
     // End of variables declaration//GEN-END:variables
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates new form CoolThemaRenderer.
      */
     public Alb_baulastblattEditor() {
         this(true);
     }
 
     /**
      * Creates new form CoolThemaRenderer.
      *
      * @param  editable  DOCUMENT ME!
      */
     public Alb_baulastblattEditor(final boolean editable) {
         this.editable = editable;
         initComponents();
         initFooterElements();
         cardLayout = (CardLayout)getLayout();
         if (!editable) {
             panControlsLaufendeNummern.setVisible(false);
             txtBlattnummer.setEditable(false);
             txtBlattnummer.setOpaque(false);
             txtBlattnummer.setBorder(null);
             lblLetzteBearbeitung.setVisible(false);
             lblBearbeiter.setVisible(false);
             lblDurch.setVisible(false);
             lblBearbeitetAm.setVisible(false);
         }
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @param   blattnummer  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private static String correctBlattnummer(String blattnummer) {
         if (blattnummer.matches("^\\d*$")) {
             while (blattnummer.length() < BLATT_NUMMER_ANZAHL_ZIFFERN) {
                 blattnummer = "0" + blattnummer;
             }
             return blattnummer;
         } else {
             return null;
         }
     }
 
     /**
      * DOCUMENT ME!
      */
     private void fillBaulastQualityAttributes() {
         for (final CidsBean baulastBean : baulastenBeans) {
             Alb_baulastEditor.addPruefungsInfoToBean(baulastBean);
         }
     }
 
     /**
      * DOCUMENT ME!
      */
     private void initFooterElements() {
         ObjectRendererUtils.decorateJLabelAndButtonSynced(
             lblForw,
             btnForward,
             ObjectRendererUtils.FORWARD_SELECTED,
             ObjectRendererUtils.FORWARD_PRESSED);
         ObjectRendererUtils.decorateJLabelAndButtonSynced(
             lblBack,
             btnBack,
             ObjectRendererUtils.BACKWARD_SELECTED,
             ObjectRendererUtils.BACKWARD_PRESSED);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     @Override
     public CidsBean getCidsBean() {
         return cidsBean;
     }
 
     /**
      * DOCUMENT ME!
      */
     private void disableSecondPageIfNoPermission() {
         if (!ObjectRendererUtils.checkActionTag(ACTION_TAG)) {
             for (final MouseListener l : lblForw.getMouseListeners()) {
                 lblForw.removeMouseListener(l);
             }
             lblForw.setEnabled(false);
             btnForward.setEnabled(false);
             for (final MouseListener l : lblBack.getMouseListeners()) {
                 lblBack.removeMouseListener(l);
             }
             lblBack.setEnabled(false);
             btnBack.setEnabled(false);
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  cidsBean  DOCUMENT ME!
      */
     @Override
     public void setCidsBean(final CidsBean cidsBean) {
         try {
             if (cidsBean != null) {
                 final int nrIdx = lstLaufendeNummern.getSelectedIndex();
                 bindingGroup.unbind();
                 this.cidsBean = cidsBean;
                 this.baulastenBeans = CidsBeanSupport.getBeanCollectionFromProperty(cidsBean, "baulasten");
                 if (baulastenBeans != null) {
                     Collections.sort((List)baulastenBeans, OBJECT_COMPARATOR);
                 }
                 bindingGroup.bind();
                 if (nrIdx <= 0) {
                     lstLaufendeNummern.setSelectedIndex(0);
                 } else {
                     try {
                         lstLaufendeNummern.setSelectedIndex(nrIdx);
                     } catch (Exception x) {
                         lstLaufendeNummern.setSelectedIndex(0);
                         log.error(x, x);
                     }
                 }
                final Object laufendeNr = ((CidsBean)lstLaufendeNummern.getSelectedValue()).getProperty(
                        "laufende_nummer");
                 final Object blattnummer = cidsBean.getProperty("blattnummer");
                lblTitle.setText("Baulastblatt " + blattnummer + ": lfd. Nummer " + laufendeNr);
                 checkLaufendeNummern();
                 strongReferenceToWeakListener = new PropertyChangeListener() {
 
                         @Override
                         public void propertyChange(final PropertyChangeEvent evt) {
                             if ("blattnummer".equals(evt.getPropertyName())) {
                                 for (final CidsBean bean : baulastenBeans) {
                                     try {
                                         bean.setProperty("blattnummer", evt.getNewValue());
                                     } catch (Exception ex) {
                                         log.warn(ex, ex);
                                     }
                                 }
                             }
                         }
                     };
                 cidsBean.addPropertyChangeListener(WeakListeners.propertyChange(
                         strongReferenceToWeakListener,
                         cidsBean));
             }
             disableSecondPageIfNoPermission();
         } catch (Exception x) {
             log.error(x, x);
         }
     }
 
     /**
      * DOCUMENT ME!
      */
     private void checkLaufendeNummern() {
         if (lstLaufendeNummern.getModel().getSize() > 0) {
             if (!panBaulastEditor.isEnabled()) {
                 setAllSubComponentsEnabled(panBaulastEditor, true);
             }
         } else {
             if (panBaulastEditor.isEnabled()) {
                 setAllSubComponentsEnabled(panBaulastEditor, false);
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  component  DOCUMENT ME!
      * @param  enabled    DOCUMENT ME!
      */
     private void setAllSubComponentsEnabled(final JComponent component, final boolean enabled) {
         component.setEnabled(enabled);
         for (final Component subComponent : component.getComponents()) {
             if (subComponent != null) {
                 if (subComponent instanceof JComponent) {
                     setAllSubComponentsEnabled((JComponent)subComponent, enabled);
                 }
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  true if the fields/boxes are editable
      */
     public boolean isEditable() {
         return editable;
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
         panTitle = new javax.swing.JPanel();
         lblTitle = new javax.swing.JLabel();
         panFooter = new javax.swing.JPanel();
         panButtons = new javax.swing.JPanel();
         jPanel2 = new javax.swing.JPanel();
         jPanel1 = new javax.swing.JPanel();
         filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(20, 0),
                 new java.awt.Dimension(20, 0),
                 new java.awt.Dimension(20, 32767));
         lblLetzteBearbeitung = new javax.swing.JLabel();
         lblBearbeiter = new javax.swing.JLabel();
         lblDurch = new javax.swing.JLabel();
         lblBearbeitetAm = new javax.swing.JLabel();
         panFooterLeft = new javax.swing.JPanel();
         lblBack = new javax.swing.JLabel();
         btnBack = new javax.swing.JButton();
         jPanel3 = new javax.swing.JPanel();
         panFooterRight = new javax.swing.JPanel();
         btnForward = new javax.swing.JButton();
         lblForw = new javax.swing.JLabel();
         panMain = new javax.swing.JPanel();
         panBaulastEditor = new de.cismet.cids.custom.objecteditors.wunda_blau.Alb_baulastEditorPanel(editable);
         rpLaufendeNummern = new de.cismet.tools.gui.RoundedPanel();
         scpLaufendeNummern = new javax.swing.JScrollPane();
         lstLaufendeNummern = new javax.swing.JList();
         semiRoundedPanel1 = new de.cismet.tools.gui.SemiRoundedPanel();
         jLabel1 = new javax.swing.JLabel();
         panControlsLaufendeNummern = new javax.swing.JPanel();
         btnAddLaufendeNummer = new javax.swing.JButton();
         btnRemoveLaufendeNummer = new javax.swing.JButton();
         btnCopyBaulast = new javax.swing.JButton();
         btnPasteBaulast = new javax.swing.JButton();
         panBlattNummer = new de.cismet.tools.gui.RoundedPanel();
         lblBlattnummer = new javax.swing.JLabel();
         txtBlattnummer = new javax.swing.JTextField();
         semiRoundedPanel2 = new de.cismet.tools.gui.SemiRoundedPanel();
         jLabel3 = new javax.swing.JLabel();
         lblBlattInMap = new javax.swing.JLabel();
         alb_picturePanel = new de.cismet.cids.custom.objecteditors.wunda_blau.Alb_picturePanel(!editable);
 
         panTitle.setOpaque(false);
         panTitle.setLayout(new java.awt.GridBagLayout());
 
         lblTitle.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
         lblTitle.setForeground(new java.awt.Color(255, 255, 255));
         lblTitle.setText("Baulastblatt");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panTitle.add(lblTitle, gridBagConstraints);
 
         panFooter.setOpaque(false);
         panFooter.setLayout(new java.awt.BorderLayout());
 
         panButtons.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 6, 0));
         panButtons.setOpaque(false);
         panButtons.setLayout(new java.awt.GridLayout(1, 0));
 
         jPanel2.setOpaque(false);
         jPanel2.setLayout(new java.awt.GridBagLayout());
 
         jPanel1.setOpaque(false);
         jPanel1.add(filler1);
 
         lblLetzteBearbeitung.setForeground(new java.awt.Color(204, 204, 204));
         lblLetzteBearbeitung.setText("Letzte Bearbeitung am");
         jPanel1.add(lblLetzteBearbeitung);
 
         lblBearbeiter.setForeground(new java.awt.Color(204, 204, 204));
 
         org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 panBaulastEditor,
                 org.jdesktop.beansbinding.ELProperty.create("${cidsBean.bearbeitungsdatum}"),
                 lblBearbeiter,
                 org.jdesktop.beansbinding.BeanProperty.create("text"),
                 "bearbeitungsdatum");
         binding.setSourceNullValue("(unbekannt)");
         binding.setSourceUnreadableValue("(unbekannt)");
         binding.setConverter(Alb_baulastblattEditor.DATE_TO_STRING);
         bindingGroup.addBinding(binding);
 
         jPanel1.add(lblBearbeiter);
 
         lblDurch.setForeground(new java.awt.Color(204, 204, 204));
         lblDurch.setText("durch");
         jPanel1.add(lblDurch);
 
         lblBearbeitetAm.setForeground(new java.awt.Color(204, 204, 204));
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 panBaulastEditor,
                 org.jdesktop.beansbinding.ELProperty.create("${cidsBean.bearbeitet_von}"),
                 lblBearbeitetAm,
                 org.jdesktop.beansbinding.BeanProperty.create("text"),
                 "bearbeitet_von");
         binding.setSourceNullValue("(unbekannt)");
         binding.setSourceUnreadableValue("(unbekannt)");
         bindingGroup.addBinding(binding);
 
         jPanel1.add(lblBearbeitetAm);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.weightx = 0.01;
         jPanel2.add(jPanel1, gridBagConstraints);
 
         panFooterLeft.setMaximumSize(new java.awt.Dimension(124, 40));
         panFooterLeft.setMinimumSize(new java.awt.Dimension(124, 40));
         panFooterLeft.setOpaque(false);
         panFooterLeft.setPreferredSize(new java.awt.Dimension(124, 40));
         panFooterLeft.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 10, 5));
 
         lblBack.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
         lblBack.setForeground(new java.awt.Color(255, 255, 255));
         lblBack.setText("Info");
         lblBack.setEnabled(false);
         lblBack.addMouseListener(new java.awt.event.MouseAdapter() {
 
                 @Override
                 public void mouseClicked(final java.awt.event.MouseEvent evt) {
                     lblBackMouseClicked(evt);
                 }
             });
         panFooterLeft.add(lblBack);
 
         btnBack.setIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cids/custom/wunda_blau/res/arrow-left.png"))); // NOI18N
         btnBack.setBorder(null);
         btnBack.setBorderPainted(false);
         btnBack.setContentAreaFilled(false);
         btnBack.setEnabled(false);
         btnBack.setFocusPainted(false);
         btnBack.setMaximumSize(new java.awt.Dimension(30, 30));
         btnBack.setMinimumSize(new java.awt.Dimension(30, 30));
         btnBack.setPreferredSize(new java.awt.Dimension(30, 30));
         btnBack.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     btnBackActionPerformed(evt);
                 }
             });
         panFooterLeft.add(btnBack);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         jPanel2.add(panFooterLeft, gridBagConstraints);
 
         panButtons.add(jPanel2);
 
         jPanel3.setOpaque(false);
         jPanel3.setLayout(new java.awt.GridBagLayout());
 
         panFooterRight.setMaximumSize(new java.awt.Dimension(124, 40));
         panFooterRight.setOpaque(false);
         panFooterRight.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 5));
 
         btnForward.setIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cids/custom/wunda_blau/res/arrow-right.png"))); // NOI18N
         btnForward.setBorder(null);
         btnForward.setBorderPainted(false);
         btnForward.setContentAreaFilled(false);
         btnForward.setFocusPainted(false);
         btnForward.setMaximumSize(new java.awt.Dimension(30, 30));
         btnForward.setMinimumSize(new java.awt.Dimension(30, 30));
         btnForward.setPreferredSize(new java.awt.Dimension(30, 30));
         btnForward.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     btnForwardActionPerformed(evt);
                 }
             });
         panFooterRight.add(btnForward);
 
         lblForw.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
         lblForw.setForeground(new java.awt.Color(255, 255, 255));
         lblForw.setText("Dokumente");
         lblForw.addMouseListener(new java.awt.event.MouseAdapter() {
 
                 @Override
                 public void mouseClicked(final java.awt.event.MouseEvent evt) {
                     lblForwMouseClicked(evt);
                 }
             });
         panFooterRight.add(lblForw);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.weightx = 0.01;
         jPanel3.add(panFooterRight, gridBagConstraints);
 
         panButtons.add(jPanel3);
 
         panFooter.add(panButtons, java.awt.BorderLayout.CENTER);
 
         setLayout(new java.awt.CardLayout());
 
         panMain.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
         panMain.setOpaque(false);
         panMain.setLayout(new java.awt.GridBagLayout());
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.gridheight = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 0.9;
         gridBagConstraints.weighty = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panMain.add(panBaulastEditor, gridBagConstraints);
 
         rpLaufendeNummern.setLayout(new java.awt.GridBagLayout());
 
         lstLaufendeNummern.setFixedCellWidth(75);
 
         final org.jdesktop.beansbinding.ELProperty eLProperty = org.jdesktop.beansbinding.ELProperty.create(
                 "${cidsBean.baulasten}");
         final org.jdesktop.swingbinding.JListBinding jListBinding = org.jdesktop.swingbinding.SwingBindings
                     .createJListBinding(
                         org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                         this,
                         eLProperty,
                         lstLaufendeNummern);
         bindingGroup.addBinding(jListBinding);
 
         lstLaufendeNummern.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
 
                 @Override
                 public void valueChanged(final javax.swing.event.ListSelectionEvent evt) {
                     lstLaufendeNummernValueChanged(evt);
                 }
             });
         scpLaufendeNummern.setViewportView(lstLaufendeNummern);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weighty = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         rpLaufendeNummern.add(scpLaufendeNummern, gridBagConstraints);
 
         semiRoundedPanel1.setBackground(java.awt.Color.darkGray);
         semiRoundedPanel1.setLayout(new java.awt.GridBagLayout());
 
         jLabel1.setForeground(new java.awt.Color(255, 255, 255));
         jLabel1.setText("Laufende Nummern");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         semiRoundedPanel1.add(jLabel1, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         rpLaufendeNummern.add(semiRoundedPanel1, gridBagConstraints);
 
         panControlsLaufendeNummern.setOpaque(false);
         panControlsLaufendeNummern.setLayout(new java.awt.GridBagLayout());
 
         btnAddLaufendeNummer.setIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cids/custom/objecteditors/wunda_blau/edit_add_mini.png"))); // NOI18N
         btnAddLaufendeNummer.setMaximumSize(new java.awt.Dimension(43, 25));
         btnAddLaufendeNummer.setMinimumSize(new java.awt.Dimension(43, 25));
         btnAddLaufendeNummer.setPreferredSize(new java.awt.Dimension(43, 25));
         btnAddLaufendeNummer.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     btnAddLaufendeNummerActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panControlsLaufendeNummern.add(btnAddLaufendeNummer, gridBagConstraints);
 
         btnRemoveLaufendeNummer.setIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cids/custom/objecteditors/wunda_blau/edit_remove_mini.png"))); // NOI18N
         btnRemoveLaufendeNummer.setMaximumSize(new java.awt.Dimension(43, 25));
         btnRemoveLaufendeNummer.setMinimumSize(new java.awt.Dimension(43, 25));
         btnRemoveLaufendeNummer.setPreferredSize(new java.awt.Dimension(43, 25));
         btnRemoveLaufendeNummer.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     btnRemoveLaufendeNummerActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panControlsLaufendeNummern.add(btnRemoveLaufendeNummer, gridBagConstraints);
 
         btnCopyBaulast.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/16/document-copy.png"))); // NOI18N
         btnCopyBaulast.setMaximumSize(new java.awt.Dimension(43, 25));
         btnCopyBaulast.setMinimumSize(new java.awt.Dimension(43, 25));
         btnCopyBaulast.setPreferredSize(new java.awt.Dimension(43, 25));
         btnCopyBaulast.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     btnCopyBaulastActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panControlsLaufendeNummern.add(btnCopyBaulast, gridBagConstraints);
 
         btnPasteBaulast.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/16/clipboard-paste.png"))); // NOI18N
         btnPasteBaulast.setMaximumSize(new java.awt.Dimension(43, 25));
         btnPasteBaulast.setMinimumSize(new java.awt.Dimension(43, 25));
         btnPasteBaulast.setPreferredSize(new java.awt.Dimension(43, 25));
         btnPasteBaulast.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     btnPasteBaulastActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panControlsLaufendeNummern.add(btnPasteBaulast, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         rpLaufendeNummern.add(panControlsLaufendeNummern, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 0.1;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panMain.add(rpLaufendeNummern, gridBagConstraints);
 
         panBlattNummer.setLayout(new java.awt.GridBagLayout());
 
         lblBlattnummer.setText("Blattnummer:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panBlattNummer.add(lblBlattnummer, gridBagConstraints);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 this,
                 org.jdesktop.beansbinding.ELProperty.create("${cidsBean.blattnummer}"),
                 txtBlattnummer,
                 org.jdesktop.beansbinding.BeanProperty.create("text"));
         binding.setSourceNullValue("keine Angabe");
         binding.setSourceUnreadableValue("");
         bindingGroup.addBinding(binding);
 
         txtBlattnummer.addFocusListener(new java.awt.event.FocusAdapter() {
 
                 @Override
                 public void focusLost(final java.awt.event.FocusEvent evt) {
                     txtBlattnummerFocusLost(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panBlattNummer.add(txtBlattnummer, gridBagConstraints);
 
         semiRoundedPanel2.setBackground(java.awt.Color.darkGray);
         semiRoundedPanel2.setLayout(new java.awt.GridBagLayout());
 
         jLabel3.setForeground(new java.awt.Color(255, 255, 255));
         jLabel3.setText("Baulastblatt");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(5, 26, 5, 0);
         semiRoundedPanel2.add(jLabel3, gridBagConstraints);
 
         lblBlattInMap.setIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cids/custom/wunda_blau/res/zoom-best-fit.png"))); // NOI18N
         lblBlattInMap.setToolTipText("Flurstücke des Baulastblattes in Karte anzeigen");
         lblBlattInMap.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
         lblBlattInMap.addMouseListener(new java.awt.event.MouseAdapter() {
 
                 @Override
                 public void mouseClicked(final java.awt.event.MouseEvent evt) {
                     lblBlattInMapMouseClicked(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
         gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
         semiRoundedPanel2.add(lblBlattInMap, gridBagConstraints);
         lblBlattInMap.getAccessibleContext().setAccessibleDescription("");
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         panBlattNummer.add(semiRoundedPanel2, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panMain.add(panBlattNummer, gridBagConstraints);
 
         add(panMain, "card1");
         add(alb_picturePanel, "card2");
 
         bindingGroup.bind();
     } // </editor-fold>//GEN-END:initComponents
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void lstLaufendeNummernValueChanged(final javax.swing.event.ListSelectionEvent evt) { //GEN-FIRST:event_lstLaufendeNummernValueChanged
         final Object selectionObj = lstLaufendeNummern.getSelectedValue();
         if (selectionObj instanceof CidsBean) {
             bindingGroup.getBinding("bearbeitet_von").unbind();
             bindingGroup.getBinding("bearbeitungsdatum").unbind();
             final CidsBean selectedBean = (CidsBean)selectionObj;
             panBaulastEditor.setCidsBean(selectedBean);
             alb_picturePanel.setCidsBean(selectedBean);
             btnPasteBaulast.setEnabled(isPastePossible());
             bindingGroup.getBinding("bearbeitet_von").bind();
             bindingGroup.getBinding("bearbeitungsdatum").bind();
            final Object laufendeNr = selectedBean.getProperty("laufende_nummer");
            final Object blattNummer = selectedBean.getProperty("blattnummer");
            lblTitle.setText("Baulastblatt " + blattNummer + ": lfd. Nummer " + laufendeNr);
         }
         final Object[] selectedValues = lstLaufendeNummern.getSelectedValues();
         final Collection<MetaObject> selectedObjects = TypeSafeCollections.newArrayList();
         for (final Object obj : selectedValues) {
             if (obj instanceof CidsBean) {
                 selectedObjects.add(((CidsBean)obj).getMetaObject());
             }
         }
         panBaulastEditor.setAllSelectedMetaObjects(selectedObjects);
     }                                                                                             //GEN-LAST:event_lstLaufendeNummernValueChanged
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnAddLaufendeNummerActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnAddLaufendeNummerActionPerformed
         try {
             final Collection<CidsBean> baulasten = CidsBeanSupport.getBeanCollectionFromProperty(cidsBean, "baulasten");
             if (baulasten != null) {
                 final String userInput = (String)JOptionPane.showInputDialog(
                         StaticSwingTools.getParentFrame(this),
                         "Bitte die neue Laufende Nummer eingeben:",
                         "Neue Laufende Nummer anlegen",
                         JOptionPane.PLAIN_MESSAGE,
                         null,
                         null,
                         getHighestCurrentLaufendeNummer(baulasten)
                                 + 1);
                 if (userInput != null) {
                     if (isNewLaufendeNummer(userInput)) {
                         final int laufendeNr = Integer.parseInt(userInput);
 
                         final CidsBean newBean = CidsBeanSupport.createNewCidsBeanFromTableName("alb_baulast");
                         newBean.setProperty("laufende_nummer", String.valueOf(laufendeNr));
                         newBean.setProperty("blattnummer", txtBlattnummer.getText());
                         newBean.setProperty("bearbeitet_von", SessionManager.getSession().getUser().getName());
                         newBean.setProperty("bearbeitungsdatum", new Date(System.currentTimeMillis()));
                         baulasten.add(newBean);
                         final int newIndex = lstLaufendeNummern.getModel().getSize();
                         lstLaufendeNummern.setSelectedIndex(newIndex - 1);
                     } else {
                         JOptionPane.showMessageDialog(
                             StaticSwingTools.getParentFrame(this),
                             "Die Nummer "
                                     + userInput
                                     + " kann nicht angelegt werden, weil diese Nummer bereits existiert!");
                     }
                 }
             }
             checkLaufendeNummern();
         } catch (Exception ex) {
             log.error(ex, ex);
             ObjectRendererUtils.showExceptionWindowToUser(
                 "Fehler beim Hinzufügen einer neuen Laufenden Nummer",
                 ex,
                 this);
         }
     } //GEN-LAST:event_btnAddLaufendeNummerActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param   input  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private boolean isNewLaufendeNummer(final String input) {
         final List<CidsBean> lasten = CidsBeanSupport.getBeanCollectionFromProperty(cidsBean, "baulasten");
         for (final CidsBean baulast : lasten) {
             final Object laufendeNr = baulast.getProperty("laufende_nummer");
             if (input.equals(laufendeNr)) {
                 return false;
             }
         }
         return true;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   baulasten  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private int getHighestCurrentLaufendeNummer(final Collection<CidsBean> baulasten) {
         int max = 0;
         if (baulasten != null) {
             for (final CidsBean baulastBean : baulasten) {
                 final Object laufendeNummerObj = baulastBean.getProperty("laufende_nummer");
                 if (laufendeNummerObj != null) {
                     try {
                         final int currentLN = Integer.valueOf(laufendeNummerObj.toString());
                         if (currentLN > max) {
                             max = currentLN;
                         }
                     } catch (Exception ex) {
                         // Number format exception expected
                     }
                 }
             }
         }
         return max;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnRemoveLaufendeNummerActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnRemoveLaufendeNummerActionPerformed
         final Object selection = lstLaufendeNummern.getSelectedValue();
         if (selection instanceof CidsBean) {
             try {
                 final CidsBean selectionBean = (CidsBean)selection;
                 final Object laufendeNummer = selectionBean.getProperty("laufende_nummer");
                 final Collection<CidsBean> baulastenCol = CidsBeanSupport.getBeanCollectionFromProperty(
                         cidsBean,
                         "baulasten");
                 if (baulastenCol != null) {
                     boolean checkOK = true;
                     if ((laufendeNummer == null) || (String.valueOf(laufendeNummer).length() < 1)) {
                         final Set<CidsBean> checkBelastet = TypeSafeCollections.newHashSet();
                         final Set<CidsBean> checkBeguenstigt = TypeSafeCollections.newHashSet();
                         for (final CidsBean otherBaulastenBean : baulastenCol) {
                             if (otherBaulastenBean != selectionBean) {
                                 checkBelastet.addAll(CidsBeanSupport.getBeanCollectionFromProperty(
                                         otherBaulastenBean,
                                         FLURSTUECKE_BELASTET));
                                 checkBeguenstigt.addAll(CidsBeanSupport.getBeanCollectionFromProperty(
                                         otherBaulastenBean,
                                         FLURSTUECKE_BEGUENSTIGT));
                             }
                         }
                         if (
                             checkBelastet.containsAll(
                                         CidsBeanSupport.getBeanCollectionFromProperty(
                                             selectionBean,
                                             FLURSTUECKE_BELASTET))
                                     && checkBeguenstigt.containsAll(
                                         CidsBeanSupport.getBeanCollectionFromProperty(
                                             selectionBean,
                                             FLURSTUECKE_BEGUENSTIGT))) {
                             checkOK = true;
                         } else {
                             checkOK = false;
                         }
                     }
                     final int answer;
                     if (checkOK) {
                         answer = JOptionPane.showConfirmDialog(
                                 StaticSwingTools.getParentFrame(this),
                                 "Soll die Nummer wirklich gelöscht werden?",
                                 "Nummer entfernen",
                                 JOptionPane.YES_NO_OPTION);
                     } else {
                         answer = JOptionPane.showConfirmDialog(
                                 StaticSwingTools.getParentFrame(this),
                                 "Plausibilitätsprüfung fehlgeschlagen. Nicht alle Flurstücke des Platzhalters wurden realen Baulasten zugeordnet. Soll dennoch gelöscht werden?",
                                 "Platzhalter entfernen",
                                 JOptionPane.YES_NO_OPTION,
                                 JOptionPane.WARNING_MESSAGE);
                     }
                     if (answer == JOptionPane.YES_OPTION) {
                         selectionBean.delete();
 //                        baulastenCol.remove(selectionBean);
 //                        baulastenBeansToDelete.add(selectionBean);
                         lstLaufendeNummern.setSelectedIndex(lstLaufendeNummern.getMinSelectionIndex());
                     }
                 }
                 checkLaufendeNummern();
             } catch (Exception e) {
                 log.error(e, e);
                 ObjectRendererUtils.showExceptionWindowToUser("Fehler beim Löschen", e, this);
             }
         }
     } //GEN-LAST:event_btnRemoveLaufendeNummerActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void lblBackMouseClicked(final java.awt.event.MouseEvent evt) { //GEN-FIRST:event_lblBackMouseClicked
         btnBackActionPerformed(null);
     }                                                                       //GEN-LAST:event_lblBackMouseClicked
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnBackActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnBackActionPerformed
         cardLayout.show(this, "card1");
         btnBack.setEnabled(false);
         btnForward.setEnabled(true);
         lblBack.setEnabled(false);
         lblForw.setEnabled(true);
     }                                                                           //GEN-LAST:event_btnBackActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnForwardActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnForwardActionPerformed
         cardLayout.show(this, "card2");
         btnBack.setEnabled(true);
         btnForward.setEnabled(false);
         lblBack.setEnabled(true);
         lblForw.setEnabled(false);
         alb_picturePanel.updateIfPicturePathsChanged();
         final String fileCollisionWarning = alb_picturePanel.getCollisionWarning();
         if (fileCollisionWarning.length() > 0) {
             JOptionPane.showMessageDialog(
                 StaticSwingTools.getParentFrame(this),
                 fileCollisionWarning,
                 "Unterschiedliche Dateiformate",
                 JOptionPane.WARNING_MESSAGE);
         }
         alb_picturePanel.clearCollisionWarning();
 //        alb_picturePanel.zoomToFeatureCollection();
     }                                                                              //GEN-LAST:event_btnForwardActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void lblForwMouseClicked(final java.awt.event.MouseEvent evt) { //GEN-FIRST:event_lblForwMouseClicked
         btnForwardActionPerformed(null);
     }                                                                       //GEN-LAST:event_lblForwMouseClicked
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnPasteBaulastActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnPasteBaulastActionPerformed
         final CidsBean currentBaulastBean = panBaulastEditor.getCidsBean();
         try {
             EditorBeanInitializerStore.getInstance().initialize(currentBaulastBean);
         } catch (Exception ex) {
             log.error(ex, ex);
         }
     }                                                                                   //GEN-LAST:event_btnPasteBaulastActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnCopyBaulastActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnCopyBaulastActionPerformed
         final CidsBean currentBaulastBean = panBaulastEditor.getCidsBean();
         EditorBeanInitializerStore.getInstance()
                 .registerInitializer(currentBaulastBean.getMetaObject().getMetaClass(),
                     new DefaultBeanInitializer(currentBaulastBean) {
 
                         @Override
                         protected void processSimpleProperty(final CidsBean beanToInit,
                                 final String propertyName,
                                 final Object simpleValueToProcess) throws Exception {
                             if ("geprueft".equalsIgnoreCase(propertyName)
                                 || "pruefdatum".equalsIgnoreCase(propertyName)
                                 || "geprueft_von".equalsIgnoreCase(propertyName)
                                 || "pruefkommentar".equalsIgnoreCase(propertyName)) {
                                 return;
                             }
 
                             final Object curVal = beanToInit.getProperty(propertyName);
                             if ((curVal == null) || "".equals(curVal.toString())) {
                                 super.processSimpleProperty(beanToInit, propertyName, simpleValueToProcess);
                             }
                         }
 
                         @Override
                         protected void processArrayProperty(final CidsBean beanToInit,
                                 final String propertyName,
                                 final Collection<CidsBean> arrayValueToProcess) throws Exception {
                             if (!propertyName.endsWith("pages")) {
                                 final Collection<CidsBean> collectionToFill = CidsBeanSupport
                                         .getBeanCollectionFromProperty(beanToInit, propertyName);
                                 collectionToFill.clear();
                                 collectionToFill.addAll(arrayValueToProcess);
                             }
                         }
                     });
         btnPasteBaulast.setEnabled(isPastePossible());
     } //GEN-LAST:event_btnCopyBaulastActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void lblBlattInMapMouseClicked(final java.awt.event.MouseEvent evt) { //GEN-FIRST:event_lblBlattInMapMouseClicked
         ObjectRendererUtils.switchToCismapMap();
         ObjectRendererUtils.addBeanGeomAsFeatureToCismapMap(cidsBean, true);
     }                                                                             //GEN-LAST:event_lblBlattInMapMouseClicked
 
     /**
      * DOCUMENT ME!
      */
     private void correctBlattnummer() {
         final String bnn = correctBlattnummer(txtBlattnummer.getText());
         if ((bnn != null) && !bnn.equals(txtBlattnummer.getText())) {
             txtBlattnummer.setText(bnn);
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void txtBlattnummerFocusLost(final java.awt.event.FocusEvent evt) { //GEN-FIRST:event_txtBlattnummerFocusLost
         correctBlattnummer();
     }                                                                           //GEN-LAST:event_txtBlattnummerFocusLost
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private boolean isPastePossible() {
         final CidsBean blBean = panBaulastEditor.getCidsBean();
         final boolean isNewBean = (blBean != null) && (blBean.getMetaObject().getStatus() == MetaObject.NEW);
         return isNewBean
                     && (EditorBeanInitializerStore.getInstance().getInitializer(blBean.getMetaObject().getMetaClass())
                         != null);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     @Override
     public JComponent getTitleComponent() {
         return panTitle;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     @Override
     public JComponent getFooterComponent() {
         return panFooter;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     @Override
     public Border getTitleBorder() {
         return new EmptyBorder(10, 10, 10, 10);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     @Override
     public Border getFooterBorder() {
         return new EmptyBorder(5, 5, 5, 5);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     @Override
     public Border getCenterrBorder() {
         return new EmptyBorder(0, 5, 0, 5);
     }
 
     /**
      * DOCUMENT ME!
      */
     @Override
     public void dispose() {
         bindingGroup.unbind();
         alb_picturePanel.dispose();
         panBaulastEditor.dispose();
         strongReferenceToWeakListener = null;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  event  DOCUMENT ME!
      */
     @Override
     public void editorClosed(final EditorClosedEvent event) {
 //            log.fatal(status);
 //        if (EditorSaveStatus.SAVE_SUCCESS == status) {
 //            for (CidsBean toDelete : baulastenBeansToDelete) {
 //                log.fatal(toDelete);
 //                try {
 //                    toDelete.delete();
 //                    log.fatal("deleted!");
 //                } catch (Exception ex) {
 //                    log.error("Can not delete Baulast Bean!", ex);
 //                }
 //            }
 //        }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  RuntimeException  DOCUMENT ME!
      */
     @Override
     public boolean prepareForSave() {
         correctBlattnummer();
         try {
             final ArrayList<String> errors = new ArrayList<String>();
             final String blattnummer = txtBlattnummer.getText();
             final boolean unique = Alb_Constraints.checkUniqueBlattNummer(
                     blattnummer,
                     getCidsBean().getMetaObject().getID());
             if (!unique) {
                 errors.add(
                     "Die Blattnummer "
                             + blattnummer
                             + " existiert bereits! Bitte geben Sie eine andere Blattnummer ein.");
             }
             final List<String> alleLastenOhneBelastetesFS = Alb_Constraints
                         .getBaulastenOhneBelastestesFlurstueckFromBlatt(cidsBean);
             if (alleLastenOhneBelastetesFS.size() > 0) {
                 errors.add(
                     "Folgende Baulasten haben kein belastetes Flurstück:\n"
                             + alleLastenOhneBelastetesFS
                             + "\nBitte ordnen Sie diesen laufenden Nummern belastete Flurstücke zu, erst dann kann der Datensatz gespeichert werden.");
             }
             final List<String> incorrectBaulasteDates = Alb_Constraints.getIncorrectBaulastDates(cidsBean);
             if (incorrectBaulasteDates.size() > 0) {
                 errors.add(
                     "Sie haben bei den folgenden laufenden Nummern des aktuell bearbeiteten Baulastblattes unplausible Datumsangaben vorgenommen (Eingabedatum fehlt oder liegt nach dem Lösch- Schließ oder Befristungsdatum):\n"
                             + incorrectBaulasteDates
                             + "\nBitte korrigieren Sie die fehlerhaften Datumsangaben, erst dann kann der Datensatz gespeichert werden.");
             }
             final Set<String> laufendeNummerCheck = new HashSet<String>();
             for (final CidsBean last : baulastenBeans) {
                 final String lastString = String.valueOf(last);
                 if (!laufendeNummerCheck.add(lastString)) {
                     errors.add(
                         "Die laufende Nummer "
                                 + lastString
                                 + " ist mehrfach vergeben. Bitte ordnen Sie jeder Baulaste eine eindeutige laufende Nummer zu.");
                 }
             }
 
             if (errors.size() > 0) {
                 String errorOutput = "";
                 for (final String s : errors) {
                     errorOutput += s + "\n";
                 }
                 errorOutput = errorOutput.substring(0, errorOutput.length() - 1);
                 JOptionPane.showMessageDialog(
                     StaticSwingTools.getParentFrame(this),
                     errorOutput,
                     "Fehler aufgetreten",
                     JOptionPane.WARNING_MESSAGE);
                 return false;
             } else {
                 fillBaulastQualityAttributes();
                 return true;
             }
         } catch (Exception ex) {
             ObjectRendererUtils.showExceptionWindowToUser("Fehler beim Speichern", ex, this);
             throw new RuntimeException(ex);
         }
     }
 }
