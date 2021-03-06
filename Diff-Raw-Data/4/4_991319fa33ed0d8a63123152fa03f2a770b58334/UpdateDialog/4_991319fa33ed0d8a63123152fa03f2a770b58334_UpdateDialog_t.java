 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 /*
  * UpdateDialog.java
  *
  * Created on 23.3.2009, 21:06:38
  */
 
 package esmska.gui;
 
 import esmska.Context;
 import esmska.data.Icons;
 import esmska.data.Log;
 import esmska.data.Tuple3;
 import esmska.data.event.ValuedEvent;
 import esmska.data.event.ValuedListener;
 import esmska.gui.JHtmlLabel.Events;
 import esmska.update.HttpDownloader;
 import esmska.update.OperatorUpdateInfo;
 import esmska.update.UpdateChecker;
 import esmska.utils.L10N;
 import esmska.data.Links;
 import java.awt.Component;
 import java.awt.Font;
 import java.awt.Image;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.awt.event.KeyEvent;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.text.Collator;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.ResourceBundle;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.BoxLayout;
 import javax.swing.GroupLayout;
 import javax.swing.GroupLayout.Alignment;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JProgressBar;
 import javax.swing.JScrollPane;
 import javax.swing.KeyStroke;
 import javax.swing.LayoutStyle.ComponentPlacement;
 import javax.swing.SwingWorker;
 import javax.swing.WindowConstants;
 import javax.swing.border.Border;
 import javax.swing.border.EmptyBorder;
 import org.apache.commons.lang.ObjectUtils;
 import org.openide.awt.Mnemonics;
 
 /** Dialog for downloading and installing operator updates.
  *
  * @author ripper
  */
 public class UpdateDialog extends javax.swing.JDialog {
 
     private static final String RES = "/esmska/resources/";
     private static final Logger logger = Logger.getLogger(ContactPanel.class.getName());
     private static final ResourceBundle l10n = L10N.l10nBundle;
     private static final Log log = Log.getInstance();
     private UpdateChecker updateChecker;
     /** listener for update checker */
     private UpdateListener listener = new UpdateListener();
     /** list of available updates */
     private ArrayList<OperatorUpdateInfo> updates = new ArrayList<OperatorUpdateInfo>();
     private Border gwScrollPaneBorder; //borders to look better
     private Border emptyBorder = new EmptyBorder(1, 1, 1, 1);
     /** current update downloader */
     private Downloader downloader;
     /** current form state */
     private FormState formState = FormState.IDLE;
 
     private final ImageIcon updateIcon = new ImageIcon(getClass().getResource(RES + "update-22.png"));
     private final ImageIcon updateManagerIcon = new ImageIcon(getClass().getResource(RES + "updateManager-22.png"));
 
     /** Set of possible form states */
     private enum FormState {
         /** check for updates was not performed */
         IDLE,
         /** checking for updates */
         CHECKING,
         /** updates found */
         READY_TO_UPDATE,
         /** downloading and installing updates */
         DOWNLOADING,
         /** no updates found */
         NO_UPDATE
     }
 
     /** Creates new form UpdateDialog
      * @param checker Update checker, may be null
      */
     public UpdateDialog(java.awt.Frame parent, boolean modal, UpdateChecker checker) {
         super(parent, modal);
         updateChecker = (UpdateChecker) ObjectUtils.defaultIfNull(checker, new UpdateChecker());
         initComponents();
         gwScrollPaneBorder = gwScrollPane.getBorder();
 
         //set window images
         ArrayList<Image> images = new ArrayList<Image>();
         images.add(new ImageIcon(getClass().getResource(RES + "updateManager-16.png")).getImage());
         images.add(new ImageIcon(getClass().getResource(RES + "updateManager-22.png")).getImage());
         images.add(new ImageIcon(getClass().getResource(RES + "updateManager-32.png")).getImage());
         images.add(new ImageIcon(getClass().getResource(RES + "updateManager-48.png")).getImage());
         setIconImages(images);
 
         //close on Ctrl+W
         String command = "close";
         getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(
                 KeyEvent.VK_W, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), command);
         getRootPane().getActionMap().put(command, new AbstractAction() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 closeButtonActionPerformed(e);
             }
         });
 
         //set focus
         checkButton.requestFocusInWindow();
 
         //add listener if update checker still running
         if (updateChecker.isRunning()) {
             updateChecker.addActionListener(listener);
         }
 
         //set appropriate state
         if (updateChecker.isOperatorUpdateAvailable()) {
             listener.extractUpdates();
             setFormState(FormState.READY_TO_UPDATE);
         } else {
             setFormState(FormState.IDLE);
         }
     }
 
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
 
         checkButton = new JButton();
         closeButton = new JButton();
         progressBar = new JProgressBar();
         topLabel = new JLabel();
         gwScrollPane = new JScrollPane();
         gwPanel = new JPanel();
         infoLabel = new JHtmlLabel();
 
         setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
 
         checkButton.setIcon(new ImageIcon(getClass().getResource("/esmska/resources/updateManager-22.png"))); // NOI18N
         Mnemonics.setLocalizedText(checkButton, l10n.getString("UpdateDialog.checkButton.text"));
         checkButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                 checkButtonActionPerformed(evt);
             }
         });
 
         closeButton.setIcon(new ImageIcon(getClass().getResource("/esmska/resources/close-22.png"))); // NOI18N
         Mnemonics.setLocalizedText(closeButton, l10n.getString("Close_"));
         closeButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                 closeButtonActionPerformed(evt);
             }
         });
 
         topLabel.setIcon(new ImageIcon(getClass().getResource("/esmska/resources/updateManager-48.png"))); // NOI18N
         Mnemonics.setLocalizedText(topLabel, l10n.getString("UpdateDialog.topLabel.text"));
         gwPanel.setLayout(new BoxLayout(gwPanel, BoxLayout.PAGE_AXIS));
         gwScrollPane.setViewportView(gwPanel);
 
         infoLabel.setFont(infoLabel.getFont().deriveFont((infoLabel.getFont().getStyle() | Font.ITALIC)));
         infoLabel.addValuedListener(new ValuedListener<JHtmlLabel.Events, String>() {
             @Override
             public void eventOccured(ValuedEvent<Events, String> e) {
                 if (e.getEvent() == Events.LINK_CLICKED) {
                     Action browseAction = Actions.getBrowseAction(e.getValue());
                     browseAction.actionPerformed(null);
                 }
             }
         });
 
         GroupLayout layout = new GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(Alignment.LEADING)
             .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(Alignment.TRAILING)
                     .addComponent(gwScrollPane, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 466, Short.MAX_VALUE)
                     .addComponent(topLabel, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 466, Short.MAX_VALUE)
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(closeButton)
                         .addPreferredGap(ComponentPlacement.RELATED)
                         .addComponent(checkButton))
                     .addComponent(progressBar, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 466, Short.MAX_VALUE)
                     .addComponent(infoLabel, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 466, Short.MAX_VALUE))
                 .addContainerGap())
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(Alignment.LEADING)
             .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(topLabel)
                 .addPreferredGap(ComponentPlacement.RELATED)
                 .addComponent(gwScrollPane, GroupLayout.DEFAULT_SIZE, 154, Short.MAX_VALUE)
                 .addPreferredGap(ComponentPlacement.RELATED)
                 .addComponent(infoLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(ComponentPlacement.RELATED)
                 .addComponent(progressBar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                     .addComponent(checkButton)
                     .addComponent(closeButton))
                 .addContainerGap())
         );
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     private void checkButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_checkButtonActionPerformed
         switch (formState) {
             case IDLE:
             case NO_UPDATE:
                 if (updateChecker.isRunning()) {
                     //if some connection is somehow "stuck", allow check again
                     updateChecker.removeActionListener(listener);
                     updateChecker = new UpdateChecker();
                 }
                 setFormState(FormState.CHECKING);
                 updateChecker.addActionListener(listener);
                 updateChecker.checkForUpdates();
                 break;
             case READY_TO_UPDATE:
                 updateGws();
                 break;
         }
 }//GEN-LAST:event_checkButtonActionPerformed
 
     private void closeButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
         if (downloader != null && !downloader.isDone()) {
             //cancel downloading of updates
             logger.finer("Cancelling downloading of updates...");
             downloader.cancel(false);
         }
         setVisible(false);
         dispose();
 }//GEN-LAST:event_closeButtonActionPerformed
 
     /** switch the form into different state */
     private void setFormState(FormState state) {
         formState = state;
 
         checkButton.setEnabled(state != FormState.CHECKING && state != FormState.DOWNLOADING);
         checkButton.setIcon((state == FormState.READY_TO_UPDATE || state == FormState.DOWNLOADING ?
             updateIcon : updateManagerIcon));
         progressBar.setIndeterminate(state == FormState.CHECKING);
         progressBar.setStringPainted(state == FormState.CHECKING || state == FormState.DOWNLOADING);
         gwScrollPane.setBorder(updates.isEmpty() ? emptyBorder : gwScrollPaneBorder);
         updateInfoLabel();
 
         switch (state) {
             case IDLE:
                 Mnemonics.setLocalizedText(checkButton, l10n.getString("UpdateDialog.checkButton.text"));
                 topLabel.setText(l10n.getString("UpdateDialog.topLabel.text"));
                 checkButton.requestFocusInWindow();
                 break;
             case CHECKING:
                 logger.finer("Checking for updates...");
                 Mnemonics.setLocalizedText(checkButton, l10n.getString("UpdateDialog.checkButton.text"));
                 progressBar.setString(l10n.getString("Update.checking"));
                 break;
             case READY_TO_UPDATE:
                 logger.finer(updates.size() + " updates found");
                 Mnemonics.setLocalizedText(checkButton, l10n.getString("Update.update_"));
                 topLabel.setText(l10n.getString("Update.updatesFound"));
                 populateGwList();
                 checkButton.requestFocusInWindow();
                 updateCheckButton();
                 break;
             case NO_UPDATE:
                 logger.finer("No updates found");
                 Mnemonics.setLocalizedText(checkButton, l10n.getString("UpdateDialog.checkButton.text"));
                 topLabel.setText(l10n.getString("Update.noUpdates"));
                 closeButton.requestFocusInWindow();
                 break;
             case DOWNLOADING:
                 logger.finer("Downloading updates...");
                 Mnemonics.setLocalizedText(checkButton, l10n.getString("Update.update_"));
                 topLabel.setText(l10n.getString("Update.updatesFound"));
                 progressBar.setString(l10n.getString("Update.downloading"));
                 //disable checkboxes
                 for (Component c : gwPanel.getComponents()) {
                     c.setEnabled(false);
                 }
                 break;
         }
     }
 
     /** Add checkboxes according to available updates */
     private void populateGwList() {
         gwPanel.removeAll();
 
         for (OperatorUpdateInfo info : updates) {
             JCheckBox checkbox = new JCheckBox();
             if (info.canBeUsed()) {
                 checkbox.setText(info.getName());
             } else {
                 checkbox.setText(MessageFormat.format(l10n.getString("Update.invalidGw"),
                         info.getName(), info.getMinProgramVersion()));
             }
             checkbox.setToolTipText(MessageFormat.format(l10n.getString("Update.gwTooltip"),
                     info.getVersion()));
             //by default select all that can be updated
             checkbox.setSelected(info.canBeUsed());
             checkbox.setEnabled(info.canBeUsed());
             //add listener when checkbox is (de)selected to update checkbutton
             checkbox.addItemListener(new ItemListener() {
                 @Override
                 public void itemStateChanged(ItemEvent e) {
                     updateCheckButton();
                 }
             });
             //add to panel
             gwPanel.add(checkbox);
         }

        //fix redrawing issues on multiplatform laf
        gwPanel.setVisible(false);
        gwPanel.setVisible(true);
     }
 
     /** Count selected checkbox and enable/disable update button */
     private void updateCheckButton() {
         boolean enabled = false;
         for (Component comp : gwPanel.getComponents()) {
             JCheckBox checkbox = (JCheckBox) comp;
             if (checkbox.isSelected()) {
                 enabled = true;
                 break;
             }
         }
         checkButton.setEnabled(enabled);
     }
 
     /** Download all selected gateways asynchronously and run their installation */
     private void updateGws() {
         setFormState(FormState.DOWNLOADING);
 
         //compute which updates to download
         ArrayList<OperatorUpdateInfo> infos = new ArrayList<OperatorUpdateInfo>();
         for (int i = 0; i < updates.size(); i++) {
             OperatorUpdateInfo info = updates.get(i);
             JCheckBox checkbox = (JCheckBox) gwPanel.getComponents()[i];
             if (checkbox.isSelected()) {
                 infos.add(info);
             }
         }
 
         if (infos.isEmpty()) {
             //nothing to download, go back to previous state
             setFormState(FormState.READY_TO_UPDATE);
             return;
         }
 
         progressBar.setValue(0);
         progressBar.setMaximum(infos.size());
 
         //run downloader
         final Downloader dl = new Downloader(infos);
         downloader = dl;
         dl.addPropertyChangeListener(new PropertyChangeListener() {
             @Override
             public void propertyChange(PropertyChangeEvent evt) {
                 if (evt.getPropertyName().equals("state") &&
                         evt.getNewValue() == SwingWorker.StateValue.DONE) {
                     installGws(dl);
                 }
             }
         });
         dl.execute();
     }
 
     /** install downloaded updates */
     private void installGws(Downloader dl) {
         assert dl.isDone() : "Downloader should have finished";
         boolean dlOk = dl.isFinishedOk();
         boolean installOk = true;
 
         if (dl.isCancelled()) {
             //if downloading was cancelled do nothing
             setFormState(FormState.READY_TO_UPDATE);
             return;
         }
 
         ArrayList<Tuple3<OperatorUpdateInfo, String, byte[]>> scripts =
                 new ArrayList<Tuple3<OperatorUpdateInfo, String, byte[]>>();
 
         //retrieve downloaded data
         try {
             if (dl.get() != null) {
                 scripts = dl.get();
             } else {
                 dlOk = false;
             }
         } catch (Exception ex) {
             logger.log(Level.SEVERE, "Could not retrieve downloaded gateway information", ex);
             dlOk = false;
         }
 
         //save the data to harddisk
         logger.finer("Saving " + scripts.size() + " updates to disk");
         for (Tuple3<OperatorUpdateInfo, String, byte[]> script : scripts) {
             try {
                 Context.persistenceManager.saveOperator(
                         script.get1().getFileName(), script.get2(), script.get3());
                 //don't forget to remove it from downloaded updates
                 updates.remove(script.get1());
                 log.addRecord(new Log.Record(
                         MessageFormat.format(l10n.getString("Update.gwUpdated"), script.get1().getName()),
                         null, Icons.STATUS_UPDATE));
             } catch (Exception ex) {
                 logger.log(Level.WARNING, "Could not save operator", ex);
                 installOk = false;
             }
         }
 
         //reload all operators
         logger.finer("Reloading operators...");
         try {
             Context.persistenceManager.loadOperators();
         } catch (Exception ex) {
             logger.log(Level.SEVERE, "Could not reload operators", ex);
             installOk = false;
         }
 
         //update the checkboxes
         populateGwList();
 
         //set new state
         if (updates.size() > 0) {
             setFormState(FormState.READY_TO_UPDATE);
         } else {
             setFormState(FormState.NO_UPDATE);
             infoLabel.setIcon(Icons.STATUS_INFO);
             infoLabel.setText(l10n.getString("Update.allUpdated"));
         }
 
         //inform if something went wrong
         if (!dlOk || !installOk) {
             infoLabel.setIcon(Icons.STATUS_ERROR);
             infoLabel.setText(!installOk ? l10n.getString("Update.installFailed") :
                 l10n.getString("Update.downloadFailed"));
         }
 
         //don't leave old entries in update checker
         updateChecker.refreshUpdatedOperators();
     }
 
     /** set relevant info in the info label */
     private void updateInfoLabel() {
         if (formState == FormState.CHECKING || formState == FormState.DOWNLOADING) {
             //display nothing if just doing something
             infoLabel.setText(null);
             infoLabel.setIcon(null);
             return;
         }
 
         boolean programUpdate = updateChecker.isProgramUpdateAvailable();
         boolean insufficientVersion = false;
         for (OperatorUpdateInfo info : updates) {
             insufficientVersion = insufficientVersion || !info.canBeUsed();
         }
 
         if (insufficientVersion) {
             //there is a gateway which can't be installed
             infoLabel.setText(MessageFormat.format(l10n.getString("Update.notLatest"), Links.DOWNLOAD));
             infoLabel.setIcon(Icons.STATUS_UPDATE_IMPORTANT);
         } else if (programUpdate) {
             //there is a program update available
             infoLabel.setText(MessageFormat.format(l10n.getString("Update.programUpdateAvailable"),
                     updateChecker.getLatestProgramVersion(), Links.DOWNLOAD));
             infoLabel.setIcon(Icons.STATUS_UPDATE_IMPORTANT);
         }
     }
 
     /** Listens for events from update checker */
     private class UpdateListener implements ActionListener {
         @Override
         public void actionPerformed(ActionEvent e) {
             switch (e.getID()) {
                 case UpdateChecker.ACTION_PROGRAM_AND_OPERATOR_UPDATE_AVAILABLE:
                 case UpdateChecker.ACTION_OPERATOR_UPDATE_AVAILABLE:
                     //gateway updates available
                     extractUpdates();
                     setFormState(FormState.READY_TO_UPDATE);
                     break;
                 case UpdateChecker.ACTION_PROGRAM_UPDATE_AVAILABLE:
                 case UpdateChecker.ACTION_NO_UPDATE_AVAILABLE:
                     //no gateway updates available
                     updates.clear();
                     setFormState(FormState.NO_UPDATE);
                     break;
                 case UpdateChecker.ACTION_CHECK_FAILED:
                     updates.clear();
                     setFormState(FormState.NO_UPDATE);
                     infoLabel.setIcon(Icons.STATUS_ERROR);
                     infoLabel.setText(l10n.getString("Update.checkFailed"));
                     break;
             }
         }
         /** take downloaded info, retrieve updates information, sort it by operator name */
         public void extractUpdates() {
             updates = new ArrayList<OperatorUpdateInfo>(updateChecker.getOperatorUpdates());
             //sort by name
             Collections.sort(updates, new Comparator<OperatorUpdateInfo>() {
                 private final Collator collator = Collator.getInstance();
                 @Override
                 public int compare(OperatorUpdateInfo o1, OperatorUpdateInfo o2) {
                     return collator.compare(o1.getName(), o2.getName());
                 }
             });
         }
     }
 
     /** Download all requested updates.
      * Returns collection of [update info;script contents;icon]. */
     private class Downloader extends SwingWorker<ArrayList<Tuple3<OperatorUpdateInfo,String,byte[]>>, Integer> {
         private boolean finishedOk;
         private Collection<OperatorUpdateInfo> infos;
         private ArrayList<Tuple3<OperatorUpdateInfo,String,byte[]>> scripts = new ArrayList<Tuple3<OperatorUpdateInfo,String, byte[]>>();
         int downloaded = 0;
 
         /** Constructor.
          * @param infos collection of infos for which to retrieve files
          */
         public Downloader(Collection<OperatorUpdateInfo> infos) {
             this.infos = infos;
         }
         @Override
         protected ArrayList<Tuple3<OperatorUpdateInfo,String,byte[]>> doInBackground() throws Exception {
             logger.finer("Downloading " + infos.size() + " updates");
             for (OperatorUpdateInfo info : infos) {
                 try {
                     //download script
                     HttpDownloader dl = new HttpDownloader(info.getDownloadUrl().toString(), false);
                     dl.execute();
                     String script = (String) dl.get();
                     if (!dl.isFinishedOk()) {
                         //if script is not downloaded, don't download the icon
                         continue;
                     }
                     byte[] icon = null;
                     if (info.getIconUrl() != null) {
                         //download icon
                         dl = new HttpDownloader(info.getIconUrl().toString(), true);
                         dl.execute();
                         icon = (byte[]) dl.get();
                         if (!dl.isFinishedOk()) {
                             continue;
                         }
                     }
                     Tuple3<OperatorUpdateInfo,String,byte[]> tuple =
                             new Tuple3<OperatorUpdateInfo,String,byte[]>(info, script, icon);
                     scripts.add(tuple);
                 } finally {
                     //publish how many updates are downloaded up to now
                     publish(++downloaded);
                 }
                 if (isCancelled()) {
                     logger.fine("Updates downloading cancelled");
                     finishedOk = false;
                     return null;
                 }
             }
             finishedOk = (infos.size() == scripts.size());
             if (!finishedOk) {
                 logger.warning("Could not download all operator updates");
             }
             return scripts;
         }
         @Override
         protected void process(List<Integer> chunks) {
             //set progress
             progressBar.setValue(chunks.get(chunks.size()-1));
         }
         /** Returns whether all requested updates were downloaded ok. */
         public boolean isFinishedOk() {
             return finishedOk;
         }
     }
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private JButton checkButton;
     private JButton closeButton;
     private JPanel gwPanel;
     private JScrollPane gwScrollPane;
     private JHtmlLabel infoLabel;
     private JProgressBar progressBar;
     private JLabel topLabel;
     // End of variables declaration//GEN-END:variables
 
 }
