 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 /*
  * To change this license header, choose License Headers in Project Properties.
  * To change this template file, choose Tools | Templates
  * and open the template in the editor.
  */
 package de.cismet.cids.custom.nas;
 
 import Sirius.navigator.connection.SessionManager;
 
 import Sirius.server.newuser.User;
 
 import org.apache.log4j.Logger;
 
 import java.awt.CardLayout;
 import java.awt.Component;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 
 import java.io.IOException;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Properties;
 import java.util.TimerTask;
 import java.util.concurrent.ExecutionException;
 
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JCheckBox;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.ListCellRenderer;
 import javax.swing.SwingWorker;
 import javax.swing.UIManager;
 import javax.swing.border.EmptyBorder;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.DefaultCaret;
 import javax.swing.text.JTextComponent;
 
 import de.cismet.cids.custom.objecteditors.utils.RendererTools;
 import de.cismet.cids.custom.utils.BusyLoggingTextPane;
 import de.cismet.cids.custom.utils.pointnumberreservation.PointNumberReservation;
 import de.cismet.cids.custom.utils.pointnumberreservation.PointNumberReservationRequest;
 import de.cismet.cids.custom.wunda_blau.search.actions.PointNumberReserverationServerAction;
 import de.cismet.cids.custom.wunda_blau.search.server.VermessungsStellenNummerSearch;
 
 import de.cismet.cids.server.actions.ServerActionParameter;
 import de.cismet.cids.server.search.CidsServerSearch;
 
 import de.cismet.cismap.commons.interaction.CismapBroker;
 
 import de.cismet.tools.gui.StaticSwingTools;
 import de.cismet.tools.gui.downloadmanager.DownloadManager;
 import de.cismet.tools.gui.downloadmanager.DownloadManagerDialog;
 
 /**
  * DOCUMENT ME!
  *
  * @author   daniel
  * @version  $Revision$, $Date$
  */
 public class PointNumberDialog extends javax.swing.JDialog {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final Logger LOG = Logger.getLogger(PointNumberDialog.class);
     private static final String SEVER_ACTION = "pointNumberReservation";
 
     //~ Instance fields --------------------------------------------------------
 
     private String wuppVnr;
     private PointNumberReservationRequest result;
     private boolean hasFreigabeAccess = false;
     private AllAntragsnummernLoadWorker allAnrLoadWorker = new AllAntragsnummernLoadWorker();
     private FreigebenWorker freigebenWorker = new FreigebenWorker();
     private PointNumberLoadWorker pnrLoadWorker = new PointNumberLoadWorker();
     private boolean useAutoCompleteDecorator = false;
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton btnCancel;
     private javax.swing.JButton btnDeSelectAll;
     private javax.swing.JButton btnDone;
     private javax.swing.JButton btnDownload;
     private javax.swing.JButton btnFreigeben;
     private javax.swing.JButton btnSelectAll;
     private javax.swing.JComboBox cbAntragPrefix;
     private javax.swing.JComboBox cbAntragsNummer;
     private javax.swing.Box.Filler filler1;
     private javax.swing.Box.Filler filler2;
     private javax.swing.JScrollPane jScrollPane1;
     private org.jdesktop.swingx.JXBusyLabel jxFreigebenWaitLabel;
     private javax.swing.JLabel lblAnrSeperator;
     private javax.swing.JLabel lblAntragsnummer;
     private javax.swing.JLabel lblFreigebenError;
     private javax.swing.JLabel lblProtokoll;
     private javax.swing.JLabel lblPunktNummern;
     private javax.swing.JLabel lblTabErgaenzen;
     private javax.swing.JLabel lblTabFreigeben;
     private javax.swing.JLabel lblTabReservieren;
     private javax.swing.JPanel pnlAntragsnummer;
     private javax.swing.JPanel pnlControls;
     private de.cismet.cids.custom.nas.PointNumberReservationPanel pnlErgaenzen;
     private javax.swing.JPanel pnlFreigabeListControls;
     private javax.swing.JPanel pnlFreigeben;
     private javax.swing.JPanel pnlFreigebenCard;
     private javax.swing.JPanel pnlFreigebenError;
     private javax.swing.JPanel pnlLeft;
     private de.cismet.cids.custom.nas.PointNumberReservationPanel pnlReservieren;
     private javax.swing.JPanel pnlRight;
     private javax.swing.JPanel pnlTabErgaenzen;
     private javax.swing.JPanel pnlTabFreigeben;
     private javax.swing.JPanel pnlTabReservieren;
     private javax.swing.JPanel pnlWait;
     private de.cismet.cids.custom.utils.BusyLoggingTextPane protokollPane;
     private javax.swing.JList punktNummernList;
     private javax.swing.JScrollPane scpPunktNummernList;
     private javax.swing.JTabbedPane tbpModus;
     // End of variables declaration//GEN-END:variables
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates new form PointNumberDialog.
      *
      * @param  parent  DOCUMENT ME!
      * @param  modal   DOCUMENT ME!
      */
     public PointNumberDialog(final java.awt.Frame parent, final boolean modal) {
         super(parent, modal);
         this.setTitle(org.openide.util.NbBundle.getMessage(
                 PointNumberDialog.class,
                 "PointNumberDialog.title"));
         initComponents();
         final Properties props = new Properties();
         try {
             props.load(PointNumberReservationPanel.class.getResourceAsStream("pointNumberSettings.properties"));
             wuppVnr = props.getProperty("wuppVnr"); // NOI18N
             useAutoCompleteDecorator = Boolean.getBoolean(props.getProperty("autoCompletion"));
         } catch (IOException e) {
             LOG.error("Could not read pointnumberSettings.properties", e);
         }
 
         tbpModus.setTabComponentAt(0, pnlTabReservieren);
         tbpModus.setTabComponentAt(1, lblTabErgaenzen);
         tbpModus.setTabComponentAt(2, lblTabFreigeben);
 
         tbpModus.addChangeListener(new ChangeListener() {
 
                 @Override
                 public void stateChanged(final ChangeEvent e) {
                     if (tbpModus.getSelectedIndex() == 2) {
                         loadPointNumbers();
                     } else {
                         final PointNumberReservationPanel pnl = (PointNumberReservationPanel)
                             tbpModus.getSelectedComponent();
                         pnl.checkNummerierungsbezirke();
                     }
                 }
             });
         tbpModus.addChangeListener(new ChangeListener() {
 
                 @Override
                 public void stateChanged(final ChangeEvent e) {
                     final DefaultComboBoxModel model = (DefaultComboBoxModel)cbAntragsNummer.getModel();
                     if (tbpModus.getSelectedIndex() == 0) {
                         model.setSelectedItem("");
                         cbAntragsNummer.setEditable(true);
                     } else {
                         final int pos = model.getIndexOf(cbAntragsNummer.getSelectedItem());
                         if (pos > 0) {
                             cbAntragsNummer.setSelectedIndex(pos);
                         } else {
                             cbAntragsNummer.setSelectedIndex(0);
                         }
                         cbAntragsNummer.setEditable(false);
                     }
                 }
             });
         punktNummernList.setCellRenderer(new PointNumberListRenderer());
         punktNummernList.addMouseListener(new MouseAdapter() {
 
                 @Override
                 public void mouseClicked(final MouseEvent event) {
                     final JList list = (JList)event.getSource();
                     final int index = list.locationToIndex(event.getPoint());
                     final CheckListItem item = (CheckListItem)list.getModel().getElementAt(index);
                     item.setSelected(!item.isSelected());
                     list.repaint(list.getCellBounds(index, index));
                 }
             });
 
         final JTextComponent textComp = (JTextComponent)cbAntragsNummer.getEditor().getEditorComponent();
 //        textComp.getDocument().addDocumentListener(this);
         final DefaultCaret caret = (DefaultCaret)protokollPane.getCaret();
         caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
         try {
             // if user does not have the right to do freigaben, remove the tab
             hasFreigabeAccess = SessionManager.getConnection()
                         .getConfigAttr(SessionManager.getSession().getUser(), "custom.nas.punktNummernFreigabe")
                         != null;
             if (!hasFreigabeAccess) {
                 tbpModus.remove(2);
             }
             final User user = SessionManager.getSession().getUser();
             CidsServerSearch search = new VermessungsStellenNummerSearch(user.getName());
             Collection res = SessionManager.getProxy()
                         .customServerSearch(SessionManager.getSession().getUser(), search);
             ArrayList<String> tmp;
             if ((res == null) || res.isEmpty()) {
                 tmp = new ArrayList<String>();
                 tmp.add(wuppVnr);
                 search = new VermessungsStellenNummerSearch("%");
                 res = SessionManager.getProxy().customServerSearch(SessionManager.getSession().getUser(), search);
                 tmp.addAll(res);
                 cbAntragPrefix.setModel(new DefaultComboBoxModel(tmp.toArray()));
             } else {
                 tmp = (ArrayList<String>)res;
                 final String vermessungstellenNr = tmp.get(0);
                 if (vermessungstellenNr != null) {
                     cbAntragPrefix.setModel(new DefaultComboBoxModel(tmp.toArray()));
                     RendererTools.makeReadOnly(cbAntragPrefix);
                 }
             }
         } catch (Exception e) {
             LOG.error("Error during determination of vermessungstellennummer", e);
         }
         if (useAutoCompleteDecorator) {
             StaticSwingTools.decorateWithFixedAutoCompleteDecorator(cbAntragsNummer);
         }
         ((DefaultComboBoxModel)cbAntragsNummer.getModel()).addElement("");
         loadAllAntragsNummern();
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      */
     private void loadPointNumbers() {
         // load the point numbers for the given anr..
         if (pnrLoadWorker.getState() == SwingWorker.StateValue.STARTED) {
             try {
                 pnrLoadWorker.cancel(true);
             } catch (Exception e) {
                 if (LOG.isDebugEnabled()) {
                     LOG.debug("Exception on cancellation point number load worker", e);
                 }
             }
         }
         pnrLoadWorker = new PointNumberLoadWorker();
         final CardLayout cl = (CardLayout)(pnlFreigeben.getLayout());
         cl.show(pnlFreigeben, "card1");
         jxFreigebenWaitLabel.setBusy(true);
         pnrLoadWorker.execute();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public String getAnr() {
         final String anr;
         if (cbAntragsNummer.isEditable()) {
             final String tmp;
             tmp = ((JTextComponent)cbAntragsNummer.getEditor().getEditorComponent()).getText();
             if ((tmp == null) || tmp.isEmpty()) {
                 anr = cbAntragsNummer.getSelectedItem().toString();
             } else {
                 anr = tmp;
             }
         } else {
             anr = cbAntragsNummer.getSelectedItem().toString();
         }
         return anr;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public String getAnrPrefix() {
         return (String)cbAntragPrefix.getSelectedItem();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public boolean isErgaenzenMode() {
         return tbpModus.getSelectedIndex() == 1;
     }
 
     /**
      * DOCUMENT ME!
      */
     private void showFreigabeError() {
 //        tfAntragsnummer.getDocument().addDocumentListener(this);
         jxFreigebenWaitLabel.setBusy(false);
         final CardLayout cl = (CardLayout)(pnlFreigeben.getLayout());
         cl.show(pnlFreigeben, "card3");
         jxFreigebenWaitLabel.setBusy(true);
     }
 
     /**
      * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
      * content of this method is always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
         java.awt.GridBagConstraints gridBagConstraints;
 
         pnlTabReservieren = new javax.swing.JPanel();
         lblTabReservieren = new javax.swing.JLabel();
         pnlTabErgaenzen = new javax.swing.JPanel();
         lblTabErgaenzen = new javax.swing.JLabel();
         pnlTabFreigeben = new javax.swing.JPanel();
         lblTabFreigeben = new javax.swing.JLabel();
         pnlLeft = new javax.swing.JPanel();
         lblAntragsnummer = new javax.swing.JLabel();
         tbpModus = new javax.swing.JTabbedPane();
         pnlReservieren = new PointNumberReservationPanel(this);
         pnlErgaenzen = new PointNumberReservationPanel(this);
         pnlFreigeben = new javax.swing.JPanel();
         pnlWait = new javax.swing.JPanel();
         jxFreigebenWaitLabel = new org.jdesktop.swingx.JXBusyLabel();
         pnlFreigebenCard = new javax.swing.JPanel();
         lblPunktNummern = new javax.swing.JLabel();
         scpPunktNummernList = new javax.swing.JScrollPane();
         punktNummernList = new javax.swing.JList();
         btnFreigeben = new javax.swing.JButton();
         pnlFreigabeListControls = new javax.swing.JPanel();
         btnSelectAll = new javax.swing.JButton();
         btnDeSelectAll = new javax.swing.JButton();
         filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0),
                 new java.awt.Dimension(0, 0),
                 new java.awt.Dimension(32767, 0));
         pnlFreigebenError = new javax.swing.JPanel();
         lblFreigebenError = new javax.swing.JLabel();
         pnlAntragsnummer = new javax.swing.JPanel();
         cbAntragPrefix = new javax.swing.JComboBox();
         lblAnrSeperator = new javax.swing.JLabel();
         cbAntragsNummer = new javax.swing.JComboBox();
         pnlRight = new javax.swing.JPanel();
         lblProtokoll = new javax.swing.JLabel();
         jScrollPane1 = new javax.swing.JScrollPane();
         protokollPane = new BusyLoggingTextPane(50, 50);
         btnDownload = new javax.swing.JButton();
         pnlControls = new javax.swing.JPanel();
         filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0),
                 new java.awt.Dimension(0, 0),
                 new java.awt.Dimension(32767, 0));
         btnCancel = new javax.swing.JButton();
         btnDone = new javax.swing.JButton();
 
         pnlTabReservieren.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
         pnlTabReservieren.setOpaque(false);
 
         lblTabReservieren.setIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cids/custom/nas/glyphicons_153_unchecked.png")));                    // NOI18N
         org.openide.awt.Mnemonics.setLocalizedText(
             lblTabReservieren,
             org.openide.util.NbBundle.getMessage(PointNumberDialog.class, "PointNumberDialog.lblTabReservieren.text")); // NOI18N
         pnlTabReservieren.add(lblTabReservieren);
 
         pnlTabErgaenzen.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
         pnlTabErgaenzen.setOpaque(false);
 
         lblTabErgaenzen.setIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cids/custom/nas/glyphicons_154_more_windows.png")));               // NOI18N
         org.openide.awt.Mnemonics.setLocalizedText(
             lblTabErgaenzen,
             org.openide.util.NbBundle.getMessage(PointNumberDialog.class, "PointNumberDialog.lblTabErgaenzen.text")); // NOI18N
         pnlTabErgaenzen.add(lblTabErgaenzen);
 
         pnlTabFreigeben.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
         pnlTabFreigeben.setOpaque(false);
 
         lblTabFreigeben.setIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cids/custom/nas/glyphicons_151_new_window.png")));                 // NOI18N
         org.openide.awt.Mnemonics.setLocalizedText(
             lblTabFreigeben,
             org.openide.util.NbBundle.getMessage(PointNumberDialog.class, "PointNumberDialog.lblTabFreigeben.text")); // NOI18N
         pnlTabFreigeben.add(lblTabFreigeben);
 
         setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
         getContentPane().setLayout(new java.awt.GridBagLayout());
 
         pnlLeft.setMinimumSize(new java.awt.Dimension(400, 27));
         pnlLeft.setPreferredSize(new java.awt.Dimension(500, 378));
         pnlLeft.setLayout(new java.awt.GridBagLayout());
 
         org.openide.awt.Mnemonics.setLocalizedText(
             lblAntragsnummer,
             org.openide.util.NbBundle.getMessage(PointNumberDialog.class, "PointNumberDialog.lblAntragsnummer.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         pnlLeft.add(lblAntragsnummer, gridBagConstraints);
 
         tbpModus.addTab(org.openide.util.NbBundle.getMessage(
                 PointNumberDialog.class,
                 "PointNumberDialog.pnlReservieren.TabConstraints.tabTitle"),
             null,
             pnlReservieren,
             org.openide.util.NbBundle.getMessage(
                 PointNumberDialog.class,
                 "PointNumberDialog.pnlReservieren.TabConstraints.tabToolTip")); // NOI18N
         tbpModus.addTab(org.openide.util.NbBundle.getMessage(
                 PointNumberDialog.class,
                 "PointNumberDialog.pnlErgaenzen.TabConstraints.tabTitle"),
             pnlErgaenzen);                                                      // NOI18N
 
         pnlFreigeben.setLayout(new java.awt.CardLayout());
 
         pnlWait.setLayout(new java.awt.GridBagLayout());
 
         org.openide.awt.Mnemonics.setLocalizedText(
             jxFreigebenWaitLabel,
             org.openide.util.NbBundle.getMessage(
                 PointNumberDialog.class,
                 "PointNumberDialog.jxFreigebenWaitLabel.text")); // NOI18N
         pnlWait.add(jxFreigebenWaitLabel, new java.awt.GridBagConstraints());
 
         pnlFreigeben.add(pnlWait, "card1");
 
         pnlFreigebenCard.setLayout(new java.awt.GridBagLayout());
 
         org.openide.awt.Mnemonics.setLocalizedText(
             lblPunktNummern,
             org.openide.util.NbBundle.getMessage(PointNumberDialog.class, "PointNumberDialog.lblPunktNummern.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
         pnlFreigebenCard.add(lblPunktNummern, gridBagConstraints);
 
         scpPunktNummernList.setViewportView(punktNummernList);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
         pnlFreigebenCard.add(scpPunktNummernList, gridBagConstraints);
 
         org.openide.awt.Mnemonics.setLocalizedText(
             btnFreigeben,
             org.openide.util.NbBundle.getMessage(PointNumberDialog.class, "PointNumberDialog.btnFreigeben.text")); // NOI18N
         btnFreigeben.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     btnFreigebenActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
         pnlFreigebenCard.add(btnFreigeben, gridBagConstraints);
 
         pnlFreigabeListControls.setLayout(new java.awt.GridBagLayout());
 
         btnSelectAll.setIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cids/custom/nas/icon-selectionadd.png")));                      // NOI18N
         org.openide.awt.Mnemonics.setLocalizedText(
             btnSelectAll,
             org.openide.util.NbBundle.getMessage(PointNumberDialog.class, "PointNumberDialog.btnSelectAll.text")); // NOI18N
         btnSelectAll.setToolTipText(org.openide.util.NbBundle.getMessage(
                 PointNumberDialog.class,
                 "PointNumberDialog.btnSelectAll.toolTipText"));                                                    // NOI18N
         btnSelectAll.setPreferredSize(new java.awt.Dimension(30, 28));
         btnSelectAll.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     btnSelectAllActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         pnlFreigabeListControls.add(btnSelectAll, gridBagConstraints);
 
         btnDeSelectAll.setIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cids/custom/nas/icon-selectionremove.png")));                     // NOI18N
         org.openide.awt.Mnemonics.setLocalizedText(
             btnDeSelectAll,
             org.openide.util.NbBundle.getMessage(PointNumberDialog.class, "PointNumberDialog.btnDeSelectAll.text")); // NOI18N
         btnDeSelectAll.setToolTipText(org.openide.util.NbBundle.getMessage(
                 PointNumberDialog.class,
                 "PointNumberDialog.btnDeSelectAll.toolTipText"));                                                    // NOI18N
         btnDeSelectAll.setPreferredSize(new java.awt.Dimension(30, 28));
         btnDeSelectAll.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     btnDeSelectAllActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
         pnlFreigabeListControls.add(btnDeSelectAll, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 2;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.weightx = 1.0;
         pnlFreigabeListControls.add(filler2, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(0, 10, 5, 0);
         pnlFreigebenCard.add(pnlFreigabeListControls, gridBagConstraints);
 
         pnlFreigeben.add(pnlFreigebenCard, "card2");
 
         pnlFreigebenError.setLayout(new java.awt.GridBagLayout());
 
         org.openide.awt.Mnemonics.setLocalizedText(
             lblFreigebenError,
             org.openide.util.NbBundle.getMessage(PointNumberDialog.class, "PointNumberDialog.lblFreigebenError.text")); // NOI18N
         pnlFreigebenError.add(lblFreigebenError, new java.awt.GridBagConstraints());
 
         pnlFreigeben.add(pnlFreigebenError, "card3");
         pnlFreigebenError.getAccessibleContext()
                 .setAccessibleName(org.openide.util.NbBundle.getMessage(
                         PointNumberDialog.class,
                         "PointNumberDialog.pnlFreigebenError.AccessibleContext.accessibleName")); // NOI18N
 
         tbpModus.addTab(org.openide.util.NbBundle.getMessage(
                 PointNumberDialog.class,
                 "PointNumberDialog.pnlFreigeben.TabConstraints.tabTitle"),
             pnlFreigeben); // NOI18N
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.gridwidth = 3;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
         pnlLeft.add(tbpModus, gridBagConstraints);
 
         pnlAntragsnummer.setLayout(new java.awt.GridBagLayout());
 
         cbAntragPrefix.setMinimumSize(new java.awt.Dimension(60, 27));
         cbAntragPrefix.setPreferredSize(new java.awt.Dimension(60, 27));
         cbAntragPrefix.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     cbAntragPrefixActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         pnlAntragsnummer.add(cbAntragPrefix, gridBagConstraints);
 
         org.openide.awt.Mnemonics.setLocalizedText(
             lblAnrSeperator,
             org.openide.util.NbBundle.getMessage(PointNumberDialog.class, "PointNumberDialog.lblAnrSeperator.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
         gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
         pnlAntragsnummer.add(lblAnrSeperator, gridBagConstraints);
 
         cbAntragsNummer.setEditable(true);
         cbAntragsNummer.setMinimumSize(new java.awt.Dimension(209, 29));
         cbAntragsNummer.setPreferredSize(new java.awt.Dimension(209, 29));
         cbAntragsNummer.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     cbAntragsNummerActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.weightx = 1.0;
         pnlAntragsnummer.add(cbAntragsNummer, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.weightx = 1.0;
         pnlLeft.add(pnlAntragsnummer, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
         getContentPane().add(pnlLeft, gridBagConstraints);
 
         pnlRight.setBackground(new java.awt.Color(254, 254, 254));
         pnlRight.setBorder(javax.swing.BorderFactory.createEtchedBorder());
         pnlRight.setMinimumSize(new java.awt.Dimension(400, 0));
         pnlRight.setPreferredSize(new java.awt.Dimension(400, 0));
         pnlRight.setLayout(new java.awt.GridBagLayout());
 
         lblProtokoll.setFont(new java.awt.Font("FreeMono", 1, 20));                                                // NOI18N
         org.openide.awt.Mnemonics.setLocalizedText(
             lblProtokoll,
             org.openide.util.NbBundle.getMessage(PointNumberDialog.class, "PointNumberDialog.lblProtokoll.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(15, 10, 10, 10);
         pnlRight.add(lblProtokoll, gridBagConstraints);
 
         protokollPane.setEditable(false);
         jScrollPane1.setViewportView(protokollPane);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
         pnlRight.add(jScrollPane1, gridBagConstraints);
 
         btnDownload.setIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cids/custom/nas/icon-download-alt.png")));                     // NOI18N
         org.openide.awt.Mnemonics.setLocalizedText(
             btnDownload,
             org.openide.util.NbBundle.getMessage(PointNumberDialog.class, "PointNumberDialog.btnDownload.text")); // NOI18N
         btnDownload.setToolTipText(org.openide.util.NbBundle.getMessage(
                 PointNumberDialog.class,
                 "PointNumberDialog.btnDownload.toolTipText"));                                                    // NOI18N
         btnDownload.setBorderPainted(false);
         btnDownload.setContentAreaFilled(false);
         btnDownload.setFocusPainted(false);
         btnDownload.setPreferredSize(new java.awt.Dimension(32, 32));
         btnDownload.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     btnDownloadActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
         pnlRight.add(btnDownload, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
         gridBagConstraints.weighty = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
         getContentPane().add(pnlRight, gridBagConstraints);
 
         pnlControls.setLayout(new java.awt.GridBagLayout());
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.weightx = 1.0;
         pnlControls.add(filler1, gridBagConstraints);
 
         org.openide.awt.Mnemonics.setLocalizedText(
             btnCancel,
             org.openide.util.NbBundle.getMessage(PointNumberDialog.class, "PointNumberDialog.btnCancel.text")); // NOI18N
         btnCancel.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     btnCancelActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.insets = new java.awt.Insets(5, 0, 10, 0);
         pnlControls.add(btnCancel, gridBagConstraints);
 
         org.openide.awt.Mnemonics.setLocalizedText(
             btnDone,
             org.openide.util.NbBundle.getMessage(PointNumberDialog.class, "PointNumberDialog.btnDone.text")); // NOI18N
         btnDone.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     btnDoneActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
         pnlControls.add(btnDone, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.weightx = 1.0;
         getContentPane().add(pnlControls, gridBagConstraints);
 
         pack();
     } // </editor-fold>//GEN-END:initComponents
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnDoneActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnDoneActionPerformed
         this.dispose();
     }                                                                           //GEN-LAST:event_btnDoneActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnCancelActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnCancelActionPerformed
         this.dispose();
     }                                                                             //GEN-LAST:event_btnCancelActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnFreigebenActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnFreigebenActionPerformed
         freigebenWorker = new FreigebenWorker();
 
         try {
             protokollPane.getDocument().remove(0, protokollPane.getDocument().getLength());
         } catch (BadLocationException ex) {
             LOG.error("Could not clear Protokoll Pane", ex);
         }
         protokollPane.setBusy(true);
         protokollPane.addMessage("Sende Freigabeauftrag.", BusyLoggingTextPane.Styles.INFO);
 
         freigebenWorker.execute();
     } //GEN-LAST:event_btnFreigebenActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnDownloadActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnDownloadActionPerformed
         if (result == null) {
             return;
         }
         PointNumberDownload download;
         if (DownloadManagerDialog.showAskingForUserTitle(
                         CismapBroker.getInstance().getMappingComponent())) {
             final String jobname = (!DownloadManagerDialog.getJobname().equals("")) ? DownloadManagerDialog
                             .getJobname() : null;
             download = new PointNumberDownload(
                     result,
                     "Punktnummer Download",
                     jobname,
                     getAnrPrefix()
                             + "_"
                             + getAnr());
         } else {
             download = new PointNumberDownload(result, "Punktnummer Download", "", getAnrPrefix() + "_" + getAnr());
         }
 
         DownloadManager.instance().add(download);
     } //GEN-LAST:event_btnDownloadActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void cbAntragPrefixActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cbAntragPrefixActionPerformed
         loadAllAntragsNummern();
     }                                                                                  //GEN-LAST:event_cbAntragPrefixActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnDeSelectAllActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnDeSelectAllActionPerformed
         for (int i = 0; i < punktNummernList.getModel().getSize(); i++) {
             final CheckListItem item = (CheckListItem)punktNummernList.getModel().getElementAt(i);
             item.setSelected(false);
         }
         punktNummernList.repaint();
     }                                                                                  //GEN-LAST:event_btnDeSelectAllActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnSelectAllActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnSelectAllActionPerformed
         for (int i = 0; i < punktNummernList.getModel().getSize(); i++) {
             final CheckListItem item = (CheckListItem)punktNummernList.getModel().getElementAt(i);
             item.setSelected(true);
         }
         punktNummernList.repaint();
     }                                                                                //GEN-LAST:event_btnSelectAllActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void cbAntragsNummerActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cbAntragsNummerActionPerformed
         // since the combobox in the freigeben tab is  not editable no document events are fired when changing the
         // selected item
         if (!cbAntragsNummer.isEditable() && (tbpModus.getSelectedIndex() == 2)) {
             loadPointNumbers();
         }
     } //GEN-LAST:event_cbAntragsNummerActionPerformed
 
     /**
      * DOCUMENT ME!
      */
     private void showError() {
         if (protokollPane != null) {
             protokollPane.addMessage(
                 "Während der Bearbeitung des Auftrags trat ein Fehler auf!",
                 BusyLoggingTextPane.Styles.ERROR);
             protokollPane.setBusy(false);
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  args  the command line arguments
      */
     public static void main(final String[] args) {
         /* Set the Nimbus look and feel */
         //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
         /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
          * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
          */
         try {
             for (final javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                 if ("Nimbus".equals(info.getName())) {
                     javax.swing.UIManager.setLookAndFeel(info.getClassName());
                     break;
                 }
             }
         } catch (ClassNotFoundException ex) {
             java.util.logging.Logger.getLogger(PointNumberDialog.class.getName())
                     .log(java.util.logging.Level.SEVERE, null, ex);
         } catch (InstantiationException ex) {
             java.util.logging.Logger.getLogger(PointNumberDialog.class.getName())
                     .log(java.util.logging.Level.SEVERE, null, ex);
         } catch (IllegalAccessException ex) {
             java.util.logging.Logger.getLogger(PointNumberDialog.class.getName())
                     .log(java.util.logging.Level.SEVERE, null, ex);
         } catch (javax.swing.UnsupportedLookAndFeelException ex) {
             java.util.logging.Logger.getLogger(PointNumberDialog.class.getName())
                     .log(java.util.logging.Level.SEVERE, null, ex);
         }
         //</editor-fold>
 
         /* Create and display the dialog */
         java.awt.EventQueue.invokeLater(new Runnable() {
 
                 @Override
                 public void run() {
                     final PointNumberDialog dialog = new PointNumberDialog(new javax.swing.JFrame(), true);
                     dialog.addWindowListener(new java.awt.event.WindowAdapter() {
 
                             @Override
                             public void windowClosing(final java.awt.event.WindowEvent e) {
                                 System.exit(0);
                             }
                         });
                     dialog.setVisible(true);
                 }
             });
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public BusyLoggingTextPane getProtokollPane() {
         return protokollPane;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  result  DOCUMENT ME!
      */
     public void setResult(final PointNumberReservationRequest result) {
         this.result = result;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  newAnr  DOCUMENT ME!
      */
     public void addAnr(final String newAnr) {
         final DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>)cbAntragsNummer.getModel();
         final List<String> tmp = new ArrayList<String>();
         for (int i = 0; i < model.getSize(); i++) {
             tmp.add(model.getElementAt(i));
         }
         final int pos = Collections.binarySearch(tmp, newAnr);
         model.insertElementAt(newAnr, Math.abs(pos));
     }
 
     /**
      * DOCUMENT ME!
      */
     public void loadAllAntragsNummern() {
         if (allAnrLoadWorker.getState() == SwingWorker.StateValue.STARTED) {
             try {
                 final boolean cancelled = allAnrLoadWorker.cancel(true);
             } catch (Exception e) {
             }
         }
         allAnrLoadWorker = new AllAntragsnummernLoadWorker();
         final DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>)cbAntragsNummer.getModel();
         final String insertedText = (String)((JTextComponent)cbAntragsNummer.getEditor().getEditorComponent())
                     .getText();
         model.removeAllElements();
         model.addElement("lade Antragsnummern...");
         if (tbpModus.getSelectedIndex() != 0) {
             cbAntragsNummer.setEditable(false);
         } else {
             ((JTextComponent)cbAntragsNummer.getEditor().getEditorComponent()).setText(insertedText);
         }
 
         cbAntragsNummer.repaint();
         this.repaint();
         allAnrLoadWorker.execute();
     }
 
     //~ Inner Classes ----------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     private final class PointNumberLoadWorker extends SwingWorker<Collection<PointNumberReservation>, Void> {
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         protected Collection<PointNumberReservation> doInBackground() throws Exception {
             final String anr = getAnr();
             if ((anr == null) || anr.equals("") || !anr.matches("[a-zA-Z0-9_-]*")) {
                 showFreigabeError();
                 return null;
             }
 
             final String anrPrefix = (getAnrPrefix() == null) ? "3290" : getAnrPrefix();
             final ServerActionParameter prefix = new ServerActionParameter(
                     PointNumberReserverationServerAction.PARAMETER_TYPE.PREFIX.toString(),
                     anrPrefix);
             final ServerActionParameter aNummer = new ServerActionParameter(
                     PointNumberReserverationServerAction.PARAMETER_TYPE.AUFTRAG_NUMMER.toString(),
                     anr);
             final ServerActionParameter action = new ServerActionParameter(
                     PointNumberReserverationServerAction.PARAMETER_TYPE.ACTION.toString(),
                     PointNumberReserverationServerAction.ACTION_TYPE.GET_POINT_NUMBERS);
             final Collection<PointNumberReservation> pointNumbers = (Collection<PointNumberReservation>)SessionManager
                         .getProxy().executeTask(
                         SEVER_ACTION,
                         "WUNDA_BLAU",
                         null,
                         action,
                         prefix,
                         aNummer);
             return pointNumbers;
         }
 
         @Override
         protected void done() {
             try {
                 final Collection<PointNumberReservation> result = get();
                 if ((result == null)
                             || result.isEmpty()) {
                     // ToDo show error
                     showFreigabeError();
                     return;
                 }
                 final CheckListItem[] listModel = new CheckListItem[result.size()];
                 int i = 0;
                 for (final PointNumberReservation pnr : result) {
                     listModel[i] = new CheckListItem(pnr.getPunktnummern());
                     i++;
                 }
                 Arrays.sort(listModel, new CheckBoxItemComparator());
                 punktNummernList.setModel(new javax.swing.AbstractListModel() {
 
                         CheckListItem[] pnrs = listModel;
 
                         @Override
                         public int getSize() {
                             return pnrs.length;
                         }
 
                         @Override
                         public Object getElementAt(final int i) {
                             return pnrs[i];
                         }
                     });
                 jxFreigebenWaitLabel.setBusy(false);
                 final CardLayout cl = (CardLayout)(pnlFreigeben.getLayout());
                 cl.show(pnlFreigeben, "card2");
 //                        tfAntragsnummer.getDocument().removeDocumentListener(PointNumberDialog.this);
             } catch (InterruptedException ex) {
                 LOG.error(
                     "Swing worker that retrieves pointnumbers for antragsnummer was interrupted",
                     ex);
                 showFreigabeError();
             } catch (ExecutionException ex) {
                 LOG.error(
                     "Error in executing worker thread that retrieves pointnumbers for antragsnummer",
                     ex);
                 showFreigabeError();
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     private final class FreigebenWorker extends SwingWorker<PointNumberReservationRequest, Void> {
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         protected PointNumberReservationRequest doInBackground() throws Exception {
             if ((punktNummernList.getModel() == null) || (punktNummernList.getModel().getSize() == 0)) {
                 return null;
             }
 
             final List<String> selectedValues = new ArrayList<String>();
             for (int i = 0; i < punktNummernList.getModel().getSize(); i++) {
                 final CheckListItem item = (CheckListItem)punktNummernList.getModel().getElementAt(i);
                 if (item.isSelected) {
                     selectedValues.add(item.toString());
                 }
             }
             final HashMap<Integer, ArrayList<Long>> pnrIntervals = new HashMap<Integer, ArrayList<Long>>();
             ArrayList<Long> pnrs = new ArrayList<Long>();
             int index = 0;
             // find coherent intervals
             for (final String pnr : selectedValues) {
                 final int currIndex = selectedValues.indexOf(pnr);
                 if (currIndex != (selectedValues.size() - 1)) {
                     final long currPnr = Long.parseLong(pnr);
                     final long nextPnr = Long.parseLong(selectedValues.get(currIndex + 1));
                     pnrs.add(currPnr);
                     if ((currPnr + 1) != nextPnr) {
                         pnrIntervals.put(index, pnrs);
                         index++;
                         pnrs = new ArrayList<Long>();
                     }
                 } else {
                     pnrs.add(Long.parseLong(pnr));
                     pnrIntervals.put(index, pnrs);
                 }
             }
 
             final String anr = getAnr();
             if (!anr.matches("[a-zA-Z0-9_-]*")) {
                 return null;
             }
 
             final String anrPrefix = (getAnrPrefix() == null) ? "3290" : getAnrPrefix();
             final ServerActionParameter prefix = new ServerActionParameter(
                     PointNumberReserverationServerAction.PARAMETER_TYPE.PREFIX.toString(),
                     anrPrefix);
             final ServerActionParameter aNummer = new ServerActionParameter(
                     PointNumberReserverationServerAction.PARAMETER_TYPE.AUFTRAG_NUMMER.toString(),
                     anr);
             final ServerActionParameter action = new ServerActionParameter(
                     PointNumberReserverationServerAction.PARAMETER_TYPE.ACTION.toString(),
                     PointNumberReserverationServerAction.ACTION_TYPE.DO_STORNO);
 
             // do release for each interval...
             final ArrayList<PointNumberReservation> releasedPoints = new ArrayList<PointNumberReservation>();
             final PointNumberReservationRequest res = new PointNumberReservationRequest();
            res.setSuccessful(true);
             for (final ArrayList<Long> interval : pnrIntervals.values()) {
                 final Long s = interval.get(0) % 1000000;
                 final Long e = interval.get(interval.size() - 1) % 1000000;
                 final Integer start = s.intValue();
                 final Integer end = e.intValue();
 
                 final String nummerierungsbezirk = "" + (interval.get(0) / 1000000);
                 final ServerActionParameter on1 = new ServerActionParameter(
                         PointNumberReserverationServerAction.PARAMETER_TYPE.ON1.toString(),
                         start);
                 final ServerActionParameter on2 = new ServerActionParameter(
                         PointNumberReserverationServerAction.PARAMETER_TYPE.ON2.toString(),
                         end);
                 final ServerActionParameter nbz = new ServerActionParameter(
                         PointNumberReserverationServerAction.PARAMETER_TYPE.NBZ.toString(),
                         nummerierungsbezirk);
                 final PointNumberReservationRequest result = (PointNumberReservationRequest)SessionManager
                             .getProxy()
                             .executeTask(
                                     SEVER_ACTION,
                                     "WUNDA_BLAU",
                                     null,
                                     action,
                                     prefix,
                                     aNummer,
                                     nbz,
                                     on1,
                                     on2);
                if ((result != null) && !result.isSuccessfull()) {
                    res.setSuccessful(false);
                    res.setProtokoll(result.getProtokoll());
                }
                 if ((result != null) && (result.getPointNumbers() != null)
                             && !result.getPointNumbers().isEmpty()) {
                     if (res.getAntragsnummer() == null) {
                         res.setAntragsnummer(result.getAntragsnummer());
                     }
                     releasedPoints.addAll(result.getPointNumbers());
                 }
             }
             res.setPointNumbers(releasedPoints);
             return res;
         }
 
         @Override
         protected void done() {
             final java.util.Timer t = new java.util.Timer();
             t.schedule(new TimerTask() {
 
                     @Override
                     public void run() {
                         try {
                             final PointNumberReservationRequest result = get();
                             setResult(result);
                             if ((result == null) || !result.isSuccessfull()) {
                                 protokollPane.addMessage(
                                     "Fehler beim Senden des Auftrags",
                                     BusyLoggingTextPane.Styles.ERROR);
                                 protokollPane.addMessage(
                                     "Die Protokolldatei mit Fehlerinformationen steht zum Download bereit.",
                                     BusyLoggingTextPane.Styles.ERROR);
                                 protokollPane.setBusy(false);
                                 return;
                             }
                             protokollPane.setBusy(false);
                             protokollPane.addMessage(
                                 "Freigabe für Antragsnummer: "
                                         + result.getAntragsnummer()
                                         + " erfolgreich. Folgende Punktnummern wurden freigegeben:",
                                 BusyLoggingTextPane.Styles.SUCCESS);
                             protokollPane.addMessage("", BusyLoggingTextPane.Styles.INFO);
                             for (final PointNumberReservation pnr : result.getPointNumbers()) {
                                 protokollPane.addMessage(
                                     ""
                                             + pnr.getPunktnummern(),
                                     BusyLoggingTextPane.Styles.INFO);
                             }
                             loadPointNumbers();
                         } catch (InterruptedException ex) {
                             LOG.error(
                                 "Swing worker that releases points was interrupted",
                                 ex);
                             showError();
                         } catch (ExecutionException ex) {
                             LOG.error(
                                 "Error in execution of Swing Worker that releases points",
                                 ex);
                             showError();
                         }
                     }
                 }, 50);
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     private final class AllAntragsnummernLoadWorker extends SwingWorker<Collection<String>, Void> {
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         protected Collection<String> doInBackground() throws Exception {
             final ServerActionParameter action = new ServerActionParameter(
                     PointNumberReserverationServerAction.PARAMETER_TYPE.ACTION.toString(),
                     PointNumberReserverationServerAction.ACTION_TYPE.GET_ALL_RESERVATIONS);
             final ServerActionParameter prefix = new ServerActionParameter(
                     PointNumberReserverationServerAction.PARAMETER_TYPE.PREFIX.toString(),
                     getAnrPrefix());
 
             final List<String> result = (List<String>)SessionManager.getProxy()
                         .executeTask(
                                 SEVER_ACTION,
                                 "WUNDA_BLAU",
                                 null,
                                 action,
                                 prefix);
             Collections.sort(result);
             return result;
         }
 
         @Override
         protected void done() {
             try {
                 final Collection<String> result = get();
                 final DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>)cbAntragsNummer.getModel();
                 final String insertedText = (String)((JTextComponent)cbAntragsNummer.getEditor().getEditorComponent())
                             .getText();
                 model.removeAllElements();
                 if ((result == null) || result.isEmpty()) {
                     model.addElement("keine Aufträge gefunden");
                     return;
                 }
 
                 for (final String s : result) {
                     model.addElement(s.substring(5));
                 }
                 if (tbpModus.getSelectedIndex() == 0) {
                     ((JTextComponent)cbAntragsNummer.getEditor().getEditorComponent()).setText(insertedText);
                     cbAntragsNummer.setEditable(true);
                 }
                 cbAntragsNummer.repaint();
                 PointNumberDialog.this.repaint();
             } catch (InterruptedException ex) {
                 LOG.error("Worker Thread that loads all existing Antragsnummern was interrupted", ex);
             } catch (ExecutionException ex) {
                 LOG.error("Error during executing Worker Thread that loads all existing Antragsnummern", ex);
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     private final class PointNumberListRenderer extends JPanel implements ListCellRenderer<Object> {
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         public Component getListCellRendererComponent(final JList<? extends Object> list,
                 final Object value,
                 final int index,
                 final boolean isSelected,
                 final boolean cellHasFocus) {
             final String text = value.toString();
             final JCheckBox checkbox = new JCheckBox(text);
             checkbox.setEnabled(true);
             checkbox.setSelected(((CheckListItem)value).isSelected());
             checkbox.setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
             checkbox.setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
             checkbox.setFont(getFont());
             checkbox.setFocusPainted(false);
             checkbox.setBorderPainted(true);
             checkbox.setBorder(isSelected ? UIManager.getBorder(
                     "List.focusCellHighlightBorder") : new EmptyBorder(1, 1, 1, 1));
             return checkbox;
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     private final class CheckListItem {
 
         //~ Instance fields ----------------------------------------------------
 
         private final String pnr;
         private boolean isSelected = false;
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new CheckListItem object.
          *
          * @param  label  DOCUMENT ME!
          */
         public CheckListItem(final String label) {
             this.pnr = label;
         }
 
         //~ Methods ------------------------------------------------------------
 
         /**
          * DOCUMENT ME!
          *
          * @return  DOCUMENT ME!
          */
         public boolean isSelected() {
             return isSelected;
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param  isSelected  DOCUMENT ME!
          */
         public void setSelected(final boolean isSelected) {
             this.isSelected = isSelected;
         }
 
         @Override
         public String toString() {
             return pnr;
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     private final class CheckBoxItemComparator implements Comparator<CheckListItem> {
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         public int compare(final CheckListItem o1, final CheckListItem o2) {
             return o1.pnr.compareTo(o2.pnr);
         }
     }
 }
