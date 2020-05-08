 /*
  * MassSieveFrame.java
  *
  * Created on May 21, 2006, 6:16 PM
  */
 package gov.nih.nimh.mass_sieve.gui;
 
 import gov.nih.nimh.mass_sieve.*;
 import gov.nih.nimh.mass_sieve.io.FileInformation;
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InterruptedIOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Properties;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.JFileChooser;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JScrollPane;
 import javax.swing.JTabbedPane;
 import javax.swing.ProgressMonitorInputStream;
 import org.biojava.bio.BioException;
 import org.biojavax.Namespace;
 import org.biojavax.RichObjectFactory;
 import org.biojavax.bio.db.ncbi.GenpeptRichSequenceDB;
 import org.biojavax.bio.seq.RichSequence;
 import org.biojavax.bio.seq.RichSequenceIterator;
 
 /**
  *
  * @author  slotta
  */
 public class MassSieveFrame extends javax.swing.JFrame {
 
     private ExperimentPanel currentExperiment;
     private HashMap<String, ExperimentPanel> expSet;
     private boolean useDigest,  useMultiColumnSort;
     private String digestName;
     private GraphLayoutType glType;
     private PreferencesDialog optDialog;
     private BatchLoadDialog batchLoadDialog;
     private static HashMap<String, ProteinInfo> proteinDB;
     private MSFileFilter msFilter;
     private MSVFileFilter msvFilter;
     private FastaFileFilter fastaFilter;
     private JTabbedPane jTabbedPaneMain;
     private StatusBar statusBar;
 
     private static class StatusBar extends JLabel {
 
         /** Creates a new instance of StatusBar */
         public StatusBar() {
             super();
             super.setPreferredSize(new Dimension(100, 16));
             setMessage("Ready");
         }
 
         public void setMessage(String message) {
             setText(" " + message);
         }
     }
 
     /** Creates new form MassSieveFrame */
     public MassSieveFrame() {
         initComponents();
         jTabbedPaneMain = new JTabbedPane();
         this.setSize(1000, 750);
         getContentPane().add(jTabbedPaneMain, BorderLayout.CENTER);
         jTabbedPaneMain.addChangeListener(new javax.swing.event.ChangeListener() {
 
             public void stateChanged(javax.swing.event.ChangeEvent evt) {
                 jTabbedPaneMainStateChanged(evt);
             }
         });
         statusBar = new StatusBar();
         getContentPane().add(statusBar, BorderLayout.SOUTH);
         proteinDB = new HashMap<String, ProteinInfo>();
         jFileChooserLoad.setMultiSelectionEnabled(true);
         msFilter = new MSFileFilter();
         msvFilter = new MSVFileFilter();
         fastaFilter = new FastaFileFilter();
         jFileChooserLoad.addChoosableFileFilter(msFilter);
         jFileChooserLoad.addChoosableFileFilter(msvFilter);
         jFileChooserLoad.addChoosableFileFilter(fastaFilter);
         jMenuClose.setEnabled(false);
         jMenuClose.setText("Close");
         jMenuSaveExp.setEnabled(false);
         jMenuSaveExp.setText("Save...");
         jMenuSaveExpSet.setEnabled(false);
         jMenuAddSearchResults.setEnabled(false);
         jMenuOpenSeqDB.setEnabled(false);
         jMenuFilterPrefs.setEnabled(false);
         jMenuCompareDiff.setEnabled(false);
         jMenuCompareParsimony.setEnabled(false);
         jMenuExportSeqDB.setEnabled(false);
         jMenuShowSummary.setEnabled(false);
         useDigest = false;
         digestName = "Trypsin";
         useMultiColumnSort = false;
         glType = GraphLayoutType.NODE_LINK_TREE;
         optDialog = new PreferencesDialog(this);
         batchLoadDialog = new BatchLoadDialog(this);
         expSet = new HashMap<String, ExperimentPanel>();
         updateStatusMessage("Please create or load an experiment");
         Logger.getLogger("prefuse").setLevel(Level.WARNING);
         jMenuOpenGenbankDB.setEnabled(false);  // until fully implemented
 
     }
 
     public void updateStatusMessage(String message) {
         long alloc = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024;
         alloc /= 1024;
 
         long max = (Runtime.getRuntime().maxMemory()) / 1024;
         max /= 1024;
         String mem = "Memory Availiable: " + alloc + " of " + max + "MB         ";
 
         statusBar.setMessage(mem + message);
     }
 
     private void jTabbedPaneMainStateChanged(javax.swing.event.ChangeEvent evt) {
         if (jTabbedPaneMain.getSelectedComponent() instanceof ExperimentPanel) {
             if (currentExperiment != null && currentExperiment != (ExperimentPanel) jTabbedPaneMain.getSelectedComponent()) {
                 currentExperiment.saveDockState();
             }
             currentExperiment = (ExperimentPanel) jTabbedPaneMain.getSelectedComponent();
             currentExperiment.loadDockState();
             jMenuSaveExp.setEnabled(true);
             jMenuSaveExp.setText("Save '" + jTabbedPaneMain.getSelectedComponent().getName() + "'");
         } else {
             jMenuSaveExp.setEnabled(false);
         }
         if (jTabbedPaneMain.getSelectedComponent() != null) {
             jMenuClose.setText("Close '" + jTabbedPaneMain.getSelectedComponent().getName() + "'");
         } else {
             jMenuSaveExp.setEnabled(false);
             jMenuSaveExpSet.setEnabled(false);
             jMenuClose.setEnabled(false);
         }
     }
 
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         buttonGroupTreeSource = new javax.swing.ButtonGroup();
         jFileChooserLoad = new javax.swing.JFileChooser();
         jOptionPaneAbout = new javax.swing.JOptionPane();
         jMenuBarMain = new javax.swing.JMenuBar();
         jMenuFile = new javax.swing.JMenu();
         jMenuNewExperiment = new javax.swing.JMenuItem();
         jMenuAddSearchResults = new javax.swing.JMenuItem();
         jMenuBatchLoad = new javax.swing.JMenuItem();
         jSeparator1 = new javax.swing.JSeparator();
         jMenuOpenExp = new javax.swing.JMenuItem();
         jMenuClose = new javax.swing.JMenuItem();
         jSeparator2 = new javax.swing.JSeparator();
         jMenuSaveExp = new javax.swing.JMenuItem();
         jMenuSaveExpSet = new javax.swing.JMenuItem();
         jSeparator3 = new javax.swing.JSeparator();
         jMenuOpenSeqDB = new javax.swing.JMenuItem();
         jMenuOpenGenbankDB = new javax.swing.JMenuItem();
         jMenuExportSeqDB = new javax.swing.JMenuItem();
         jSeparator4 = new javax.swing.JSeparator();
         jMenuQuit = new javax.swing.JMenuItem();
         jMenuTools = new javax.swing.JMenu();
         jMenuFilterPrefs = new javax.swing.JMenuItem();
         jMenuShowSummary = new javax.swing.JMenuItem();
         jSeparatorCompare = new javax.swing.JSeparator();
         jMenuCompareDiff = new javax.swing.JMenuItem();
         jMenuCompareParsimony = new javax.swing.JMenuItem();
         jSeparator5 = new javax.swing.JSeparator();
         jMenuOptions = new javax.swing.JMenuItem();
         jMenuHelp = new javax.swing.JMenu();
         jMenuGarbageCollect = new javax.swing.JMenuItem();
         jMenuResetLayout = new javax.swing.JMenuItem();
         jMenuAbout = new javax.swing.JMenuItem();
 
         jFileChooserLoad.setDialogTitle("Open Files");
 
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("MassSieve v1.03");
         setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
         addWindowListener(new java.awt.event.WindowAdapter() {
             public void windowClosing(java.awt.event.WindowEvent evt) {
                 formWindowClosing(evt);
             }
         });
 
         jMenuFile.setText("File");
 
         jMenuNewExperiment.setText("New Experiment");
         jMenuNewExperiment.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuNewExperimentActionPerformed(evt);
             }
         });
         jMenuFile.add(jMenuNewExperiment);
 
         jMenuAddSearchResults.setText("Add Search Results...");
         jMenuAddSearchResults.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuAddSearchResultsActionPerformed(evt);
             }
         });
         jMenuFile.add(jMenuAddSearchResults);
 
         jMenuBatchLoad.setText("Batch Load Results...");
         jMenuBatchLoad.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuBatchLoadActionPerformed(evt);
             }
         });
         jMenuFile.add(jMenuBatchLoad);
         jMenuFile.add(jSeparator1);
 
         jMenuOpenExp.setText("Open Experiment(s)...");
         jMenuOpenExp.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuOpenExpActionPerformed(evt);
             }
         });
         jMenuFile.add(jMenuOpenExp);
 
         jMenuClose.setText("Close Tab");
         jMenuClose.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuCloseActionPerformed(evt);
             }
         });
         jMenuFile.add(jMenuClose);
         jMenuFile.add(jSeparator2);
 
         jMenuSaveExp.setText("Save Experiment...");
         jMenuSaveExp.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuSaveExpActionPerformed(evt);
             }
         });
         jMenuFile.add(jMenuSaveExp);
 
         jMenuSaveExpSet.setText("Save All Experiments...");
         jMenuSaveExpSet.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuSaveExpSetActionPerformed(evt);
             }
         });
         jMenuFile.add(jMenuSaveExpSet);
         jMenuFile.add(jSeparator3);
 
         jMenuOpenSeqDB.setText("Import Fasta...");
         jMenuOpenSeqDB.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuOpenSeqDBActionPerformed(evt);
             }
         });
         jMenuFile.add(jMenuOpenSeqDB);
 
         jMenuOpenGenbankDB.setText("Update from Genbank");
         jMenuOpenGenbankDB.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuOpenGenbankDBActionPerformed(evt);
             }
         });
         jMenuFile.add(jMenuOpenGenbankDB);
 
         jMenuExportSeqDB.setText("Export Fasta File...");
         jMenuExportSeqDB.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuExportSeqDBActionPerformed(evt);
             }
         });
         jMenuFile.add(jMenuExportSeqDB);
         jMenuFile.add(jSeparator4);
 
         jMenuQuit.setText("Quit");
         jMenuQuit.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuQuitActionPerformed(evt);
             }
         });
         jMenuFile.add(jMenuQuit);
 
         jMenuBarMain.add(jMenuFile);
 
         jMenuTools.setText("Tools");
 
         jMenuFilterPrefs.setText("Change Filter...");
         jMenuFilterPrefs.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuFilterPrefsActionPerformed(evt);
             }
         });
         jMenuTools.add(jMenuFilterPrefs);
 
         jMenuShowSummary.setText("Experiment Summary...");
         jMenuShowSummary.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuShowSummaryActionPerformed(evt);
             }
         });
         jMenuTools.add(jMenuShowSummary);
         jMenuTools.add(jSeparatorCompare);
 
         jMenuCompareDiff.setText("Compare Experiment Differences");
         jMenuCompareDiff.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuCompareDiffActionPerformed(evt);
             }
         });
         jMenuTools.add(jMenuCompareDiff);
 
         jMenuCompareParsimony.setText("Compare Experiments w/Parsimony");
         jMenuCompareParsimony.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuCompareParsimonyActionPerformed(evt);
             }
         });
         jMenuTools.add(jMenuCompareParsimony);
         jMenuTools.add(jSeparator5);
 
         jMenuOptions.setText("Preferences");
         jMenuOptions.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuOptionsActionPerformed(evt);
             }
         });
         jMenuTools.add(jMenuOptions);
 
         jMenuBarMain.add(jMenuTools);
 
         jMenuHelp.setText("Help");
 
         jMenuGarbageCollect.setText("Compact Memory");
         jMenuGarbageCollect.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuGarbageCollectActionPerformed(evt);
             }
         });
         jMenuHelp.add(jMenuGarbageCollect);
 
         jMenuResetLayout.setText("Reset layout");
         jMenuResetLayout.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuResetLayoutActionPerformed(evt);
             }
         });
         jMenuHelp.add(jMenuResetLayout);
 
         jMenuAbout.setText("About");
         jMenuAbout.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuAboutActionPerformed(evt);
             }
         });
         jMenuHelp.add(jMenuAbout);
 
         jMenuBarMain.add(jMenuHelp);
 
         setJMenuBar(jMenuBarMain);
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     private void jMenuOpenGenbankDBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuOpenGenbankDBActionPerformed
         GenpeptRichSequenceDB genbank = new GenpeptRichSequenceDB();
         for (String pName : proteinDB.keySet()) {
             try {
                 RichSequence seq = genbank.getRichSequence(pName);
                 ProteinInfo pInfo = proteinDB.get(pName);
                 pInfo.updateFromRichSequence(seq);
                 System.out.println("Updated protein " + pName);
             } catch (BioException ex) {
                 System.out.println("Unable to find protein " + pName);
             }
             break;
         }
 
     }//GEN-LAST:event_jMenuOpenGenbankDBActionPerformed
 
     private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
         if (currentExperiment != null) {
             currentExperiment.saveDockState();
         }
     }//GEN-LAST:event_formWindowClosing
 
     private void jMenuSaveExpSetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuSaveExpSetActionPerformed
         jFileChooserLoad.setFileFilter(msvFilter);
         int status = jFileChooserLoad.showSaveDialog(this);
         if (status == JFileChooser.APPROVE_OPTION) {
             File selectedFile = jFileChooserLoad.getSelectedFile();
             try {
                 if (!selectedFile.createNewFile()) {
                     status = JOptionPane.showConfirmDialog(this,
                             selectedFile.getName() + " exists, are you sure you wish to overwrite it?",
                             "Overwrite?", JOptionPane.YES_NO_OPTION);
                     if (status != JOptionPane.OK_OPTION) {
                         return;
                     }
                 }
                 FileOutputStream fs = new FileOutputStream(selectedFile);
                 ObjectOutputStream os = new ObjectOutputStream(fs);
                 int tabCount = jTabbedPaneMain.getTabCount();
                 os.writeInt(tabCount);
                 for (int i = 0; i < tabCount; i++) {
                     ExperimentPanel expPanel = (ExperimentPanel) jTabbedPaneMain.getComponentAt(i);
                     expPanel.saveExperiment(os);
                 }
                 os.writeObject(proteinDB);
                 os.close();
             } catch (FileNotFoundException ex) {
                 JOptionPane.showMessageDialog(this, "Unable to save file", "File Error", JOptionPane.ERROR_MESSAGE);
                 ex.printStackTrace();
             } catch (IOException ex) {
                 JOptionPane.showMessageDialog(this, "Unable to save file", "File Error", JOptionPane.ERROR_MESSAGE);
                 ex.printStackTrace();
             }
         }
     }//GEN-LAST:event_jMenuSaveExpSetActionPerformed
 
     private void jMenuOpenExpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuOpenExpActionPerformed
         jFileChooserLoad.setFileFilter(msvFilter);
         int status = jFileChooserLoad.showOpenDialog(this);
         if (status == JFileChooser.APPROVE_OPTION) {
             File selectedFile = jFileChooserLoad.getSelectedFile();
             try {
                 Object obj;
                 FileInputStream fin = new FileInputStream(selectedFile);
                 ObjectInputStream oin = new ObjectInputStream(fin);
                 int expCount = oin.readInt();
                 if (expCount == 1) {
                     System.out.println("File contains " + expCount + " experiment");
                 } else {
                     System.out.println("File contains " + expCount + " experiments");
                 }
                 for (int i = 0; i < expCount; i++) {
                     obj = oin.readObject();
                     Experiment exp = (Experiment) obj;
                     if (this.createExperiment(exp.getName())) {
                         currentExperiment.reloadData(exp);
                     }
                 }
                 obj = oin.readObject();
                 HashMap<String, ProteinInfo> newProteinDB = (HashMap<String, ProteinInfo>) obj;
                 for (ProteinInfo pi : newProteinDB.values()) {
                     MassSieveFrame.addProtein(pi);
                 }
             } catch (FileNotFoundException ex) {
                 JOptionPane.showMessageDialog(this, "Unable to open file", "File Error", JOptionPane.ERROR_MESSAGE);
                 ex.printStackTrace();
             } catch (ClassNotFoundException ex) {
                 JOptionPane.showMessageDialog(this, "File format does not match current MassSieve version", "File Error", JOptionPane.ERROR_MESSAGE);
                 ex.printStackTrace();
             } catch (IOException ex) {
                 JOptionPane.showMessageDialog(this, "Unable to open file", "File Error", JOptionPane.ERROR_MESSAGE);
                 ex.printStackTrace();
             }
         }
     }//GEN-LAST:event_jMenuOpenExpActionPerformed
 
     private void jMenuSaveExpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuSaveExpActionPerformed
         jFileChooserLoad.setFileFilter(msvFilter);
         int status = jFileChooserLoad.showSaveDialog(this);
         if (status == JFileChooser.APPROVE_OPTION) {
             File selectedFile = jFileChooserLoad.getSelectedFile();
             currentExperiment = (ExperimentPanel) jTabbedPaneMain.getSelectedComponent();
             try {
                 selectedFile = addExtension(selectedFile);
                 if (!selectedFile.createNewFile()) {
                     status = JOptionPane.showConfirmDialog(this,
                             selectedFile.getName() + " exists, are you sure you wish to overwrite it?",
                             "Overwrite?", JOptionPane.YES_NO_OPTION);
                     if (status != JOptionPane.OK_OPTION) {
                         return;
                     }
                 }
                 FileOutputStream fs = new FileOutputStream(selectedFile);
                 ObjectOutputStream os = new ObjectOutputStream(fs);
                 os.writeInt(1);
                 currentExperiment.saveExperiment(os);
                 os.writeObject(proteinDB);
                 os.close();
             } catch (FileNotFoundException ex) {
                 JOptionPane.showMessageDialog(this, "Unable to save file", "File Error", JOptionPane.ERROR_MESSAGE);
                 ex.printStackTrace();
             } catch (IOException ex) {
                 JOptionPane.showMessageDialog(this, "Unable to save file", "File Error", JOptionPane.ERROR_MESSAGE);
                 ex.printStackTrace();
             }
         }
     }//GEN-LAST:event_jMenuSaveExpActionPerformed
 
     private File addExtension(File file) throws IOException {
         if (!file.getCanonicalPath().endsWith(".msv")) {
             return new File(file.getCanonicalPath() + ".msv");
         }
         return file;
     }
 
     private void jMenuShowSummaryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuShowSummaryActionPerformed
         currentExperiment = (ExperimentPanel) jTabbedPaneMain.getSelectedComponent();
         currentExperiment.showSummary();
     }//GEN-LAST:event_jMenuShowSummaryActionPerformed
 
     private void jMenuExportSeqDBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuExportSeqDBActionPerformed
         jFileChooserLoad.setFileFilter(fastaFilter);
         int status = jFileChooserLoad.showSaveDialog(this);
         if (status == JFileChooser.APPROVE_OPTION) {
             File selectedFile = jFileChooserLoad.getSelectedFile();
             try {
                 if (!selectedFile.createNewFile()) {
                     status = JOptionPane.showConfirmDialog(this,
                             selectedFile.getName() + " exists, are you sure you wish to overwrite it?",
                             "Overwrite?", JOptionPane.YES_NO_OPTION);
                     if (status != JOptionPane.OK_OPTION) {
                         return;
                     }
                 }
                 System.out.println("Exporting " + selectedFile.getName() + " as a FASTA formated file");
                 FileOutputStream pOut = new FileOutputStream(selectedFile);
                 BufferedOutputStream fos = new BufferedOutputStream(pOut);
                 int seqCount = 0;
                 HashSet<String> minProteins = new HashSet<String>();
                 for (ExperimentPanel exp : expSet.values()) {
                     minProteins.addAll(exp.getProteins().keySet());
                 }
                 for (String prot : minProteins) {
                     RichSequence rs = proteinDB.get(prot).getRichSequence();
                     if (rs != null) {
                         if (rs.length() > 0) {
                             RichSequence.IOTools.writeFasta(fos, rs, null);
                             seqCount++;
                         }
                     }
                 }
                 JOptionPane.showMessageDialog(this, "Exported " + seqCount + " sequences.");
             } catch (FileNotFoundException ex) {
                 JOptionPane.showMessageDialog(this, "Unable to export sequences", "File Error", JOptionPane.ERROR_MESSAGE);
                 ex.printStackTrace();
             } catch (IOException ex) {
                 JOptionPane.showMessageDialog(this, "Unable to export sequences", "File Error", JOptionPane.ERROR_MESSAGE);
                 ex.printStackTrace();
             }
         }
     }//GEN-LAST:event_jMenuExportSeqDBActionPerformed
 
     private void jMenuBatchLoadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuBatchLoadActionPerformed
         batchLoadDialog.setVisible(true);
     }//GEN-LAST:event_jMenuBatchLoadActionPerformed
 
     private void jMenuGarbageCollectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuGarbageCollectActionPerformed
         System.gc();
     }//GEN-LAST:event_jMenuGarbageCollectActionPerformed
 
     private void jMenuOptionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuOptionsActionPerformed
         optDialog.setUseDigestBox(useDigest);
         optDialog.setProteaseCombo(digestName);
         optDialog.setGraphLayout(glType);
         optDialog.setVisible(true);
     }//GEN-LAST:event_jMenuOptionsActionPerformed
 
     private void jMenuCompareParsimonyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuCompareParsimonyActionPerformed
         ArrayList<PeptideHit> allHits = new ArrayList<PeptideHit>();
         ArrayList<FileInformation> fInfos = new ArrayList<FileInformation>();
         Component comps[] = jTabbedPaneMain.getComponents();
         double maxMascot = Double.MIN_VALUE;
         double maxOmssa = Double.MIN_VALUE;
         double maxXtandem = Double.MIN_VALUE;
         for (Component comp : comps) {
             if (comp instanceof ExperimentPanel) {
                 ExperimentPanel exp = (ExperimentPanel) comp;
                 if (exp.getFilterSettings().getMascotCutoff() > maxMascot) {
                     maxMascot = exp.getFilterSettings().getMascotCutoff();
                 }
                 if (exp.getFilterSettings().getOmssaCutoff() > maxOmssa) {
                     maxOmssa = exp.getFilterSettings().getOmssaCutoff();
                 }
                 if (exp.getFilterSettings().getXtandemCutoff() > maxXtandem) {
                     maxXtandem = exp.getFilterSettings().getXtandemCutoff();
                 }
                 allHits.addAll(exp.getPepCollection().getPeptideHits());
                 fInfos.addAll(exp.getFileInfos());
             }
         }
         currentExperiment = new ExperimentPanel(this, "Parsimony Comparison");
         currentExperiment.getFilterSettings().setUseIonIdent(false);
         currentExperiment.getFilterSettings().setMascotCutoff(maxMascot);
         currentExperiment.getFilterSettings().setOmssaCutoff(maxOmssa);
         currentExperiment.getFilterSettings().setXtandemCutoff(maxXtandem);
         currentExperiment.addPeptideHits(allHits);
         currentExperiment.setFileInfos(fInfos);
         expSet.put("Parsimony Comparison", currentExperiment);
         jTabbedPaneMain.add(currentExperiment);
         jTabbedPaneMain.setSelectedComponent(currentExperiment);
     }//GEN-LAST:event_jMenuCompareParsimonyActionPerformed
 
     private void jMenuCompareDiffActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuCompareDiffActionPerformed
         ListPanel cPanel = new ListPanel();
         cPanel.addProteinList(expSet);
         JScrollPane compare = cPanel.createTable();
         compare.setName("Differences Comparison");
         jTabbedPaneMain.add(compare);
         jTabbedPaneMain.setSelectedComponent(compare);
     }//GEN-LAST:event_jMenuCompareDiffActionPerformed
 
     private void jMenuNewExperimentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuNewExperimentActionPerformed
         //javax.swing.JOptionPane optPane = new javax.swing.JOptionPane();
         String s = JOptionPane.showInputDialog(this, "Experiment Name");
         if (s != null && s.length() > 0) {
             createExperiment(s);
             jMenuClose.setEnabled(true);
             jMenuClose.setText("Close '" + jTabbedPaneMain.getSelectedComponent().getName() + "'");
             jMenuSaveExp.setEnabled(true);
             jMenuSaveExp.setText("Save '" + jTabbedPaneMain.getSelectedComponent().getName() + "'");
         }
     }//GEN-LAST:event_jMenuNewExperimentActionPerformed
 
     public boolean createExperiment(String name) {
         if (expSet.containsKey(name)) {
             JOptionPane.showMessageDialog(MassSieveFrame.this, "There is already an experiment named " + name);
             return false;
         } else {
             ExperimentPanel exp = new ExperimentPanel(this, name);
             this.createExperiment(exp);
             return true;
         }
     }
 
     public void createExperiment(ExperimentPanel expPanel) {
         currentExperiment = expPanel;
         jTabbedPaneMain.add(currentExperiment);
         jTabbedPaneMain.setSelectedComponent(currentExperiment);
         jMenuAddSearchResults.setEnabled(true);
         jMenuOpenSeqDB.setEnabled(true);
         jMenuFilterPrefs.setEnabled(true);
         jMenuShowSummary.setEnabled(true);
         jMenuClose.setEnabled(true);
         jMenuClose.setText("Close '" + jTabbedPaneMain.getSelectedComponent().getName() + "'");
         jMenuSaveExp.setEnabled(true);
         jMenuSaveExp.setText("Save '" + jTabbedPaneMain.getSelectedComponent().getName() + "'");
         jMenuSaveExpSet.setEnabled(true);
         expSet.put(expPanel.getName(), currentExperiment);
         if (expSet.size() >= 2) {
             jMenuCompareDiff.setEnabled(true);
             jMenuCompareParsimony.setEnabled(true);
         }
     }
 
     private void jMenuFilterPrefsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuFilterPrefsActionPerformed
         currentExperiment = (ExperimentPanel) jTabbedPaneMain.getSelectedComponent();
         currentExperiment.showPreferences();
     }//GEN-LAST:event_jMenuFilterPrefsActionPerformed
 
     private void jMenuOpenSeqDBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuOpenSeqDBActionPerformed
         jFileChooserLoad.setFileFilter(fastaFilter);
         int status = jFileChooserLoad.showOpenDialog(this);
         if (status == JFileChooser.APPROVE_OPTION) {
             File selectedFiles[] = jFileChooserLoad.getSelectedFiles();
             addSeqDBfiles(selectedFiles);
         }
         jMenuExportSeqDB.setEnabled(true);
     }//GEN-LAST:event_jMenuOpenSeqDBActionPerformed
 
     public void addSeqDBfiles(final File files[]) {
         new Thread(new Runnable() {
 
             public void run() {
                 int seqCount = 0;
                 //int dupCount = 0;
                 for (File f : files) {
                     System.err.println("Parsing " + f.getName() + " as the sequence database");
                     setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                     try {
                         // Not sure if your input is EMBL or Genbank? Load them both here.^M
                         //Class.forName("org.biojavax.bio.seq.io.EMBLFormat");
                         //Class.forName("org.biojavax.bio.seq.io.UniProtFormat");
                         //Class.forName("org.biojavax.bio.seq.io.UniProtXMLFormat");
                         //Class.forName("org.biojavax.bio.seq.io.GenbankFormat");
                         Class.forName("org.biojavax.bio.seq.io.FastaFormat");
 
                         // Now let BioJavaX guess which format you actually should use (using the default namespace)
                         Namespace ns = RichObjectFactory.getDefaultNamespace();
                         ProgressMonitorInputStream pin = new ProgressMonitorInputStream(MassSieveFrame.this, "Loading " + f.getName(), new FileInputStream(f));
                         BufferedInputStream bin = new BufferedInputStream(pin);
                         RichSequenceIterator seqItr = RichSequence.IOTools.readStream(bin, ns);
                         while (seqItr.hasNext()) {
                             try {
                                 RichSequence seq = seqItr.nextRichSequence();
                                 String seqName = seq.getName();
                                 //System.out.println(seqName);
                                 if (proteinDB.containsKey(seqName)) {
                                     ProteinInfo pInfo = proteinDB.get(seqName);
                                     pInfo.updateFromRichSequence(seq);
                                     //RichSequence daSeq = proteinDB.get(seqName);
                                     //if (daSeq != null) {
                                     //    if (daSeq.getInternalSymbolList() != SymbolList.EMPTY_LIST ) dupCount++;
                                     //}
                                     //proteinDB.put(seqName, seq);
                                     System.err.println(seqName);
                                     seqCount++;
                                 }
                             } catch (BioException ex) {
                                 if (ex.getCause() != null) {
                                     throw ex.getCause();
                                 }
                                 ex.printStackTrace();
                             }
                         }
                         System.err.println(f.getName() + " sequence database read complete!");
                     } catch (InterruptedIOException ex) {
                         setCursor(null);
                         System.err.println("Fasta import canceled by user");
                         break;
                     } catch (IOException ex) {
                         setCursor(null);
                         ex.printStackTrace();
                     } catch (ClassNotFoundException ex) {
                         ex.printStackTrace();
                     } catch (Throwable ex) {
                         ex.printStackTrace();
                     }
                     setCursor(null);
                 }
                 //jOptionPaneAbout.showMessageDialog(MassSieveFrame.this, "Imported " + seqCount + " sequences, with " + dupCount + " duplicates.");
                 JOptionPane.showMessageDialog(MassSieveFrame.this, "Imported " + seqCount + " sequences");
             }
         }).start();
     }
 
     public static void addProtein(ProteinInfo pInfo) {
         String pName = pInfo.getName();
         if (!proteinDB.containsKey(pName)) {
             proteinDB.put(pName, pInfo);
         } else {
             ProteinInfo pInfoOld = proteinDB.get(pName);
             pInfoOld.update(pInfo);
         }
     }
 
     public static ProteinInfo getProtein(String pName) {
         return proteinDB.get(pName);
     }
 
     private void jMenuCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuCloseActionPerformed
         Component current = jTabbedPaneMain.getSelectedComponent();
         jTabbedPaneMain.remove(current);
         if (current instanceof ExperimentPanel) {
             expSet.remove(current.getName());
             if (expSet.size() < 2) {
                 jMenuCompareDiff.setEnabled(false);
                 jMenuCompareParsimony.setEnabled(false);
             }
         }
         if (!(jTabbedPaneMain.getSelectedComponent() instanceof ExperimentPanel)) {
             jMenuAddSearchResults.setEnabled(false);
             jMenuOpenSeqDB.setEnabled(false);
             jMenuFilterPrefs.setEnabled(false);
             jMenuShowSummary.setEnabled(false);
         }
         if (jTabbedPaneMain.getTabCount() < 1) {
             jMenuClose.setEnabled(false);
             jMenuClose.setText("Close");
             jMenuSaveExp.setEnabled(false);
             jMenuSaveExp.setText("Save...");
             jMenuSaveExpSet.setEnabled(false);
         } else {
             jMenuClose.setText("Close " + jTabbedPaneMain.getSelectedComponent().getName());
             jMenuSaveExp.setText("Save '" + jTabbedPaneMain.getSelectedComponent().getName() + "'");
         }
     }//GEN-LAST:event_jMenuCloseActionPerformed
 
     private void jMenuAddSearchResultsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuAddSearchResultsActionPerformed
         jFileChooserLoad.setFileFilter(msFilter);
         int status = jFileChooserLoad.showOpenDialog(this);
         if (status == JFileChooser.APPROVE_OPTION) {
             new Thread(new Runnable() {
 
                 public void run() {
                     setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
 
                     File selectedFiles[] = jFileChooserLoad.getSelectedFiles();
                     currentExperiment = (ExperimentPanel) jTabbedPaneMain.getSelectedComponent();
                     currentExperiment.addFiles(selectedFiles);
                     setCursor(null);
                 }
             }).start();
         }
     }//GEN-LAST:event_jMenuAddSearchResultsActionPerformed
 
     public void addExperimentAndFiles(ExperimentPanel defExp, String exp, File files[]) {
         if (expSet.containsKey(exp)) {
             currentExperiment = expSet.get(exp);
         } else {
             createExperiment(exp);
         }
         currentExperiment.getFilterSettings().cloneFilterSettings(defExp.getFilterSettings());
         currentExperiment.addFiles(files);
     }
 
     private void jMenuQuitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuQuitActionPerformed
         if (currentExperiment != null) {
             currentExperiment.saveDockState();
         }
         System.exit(0);
     }//GEN-LAST:event_jMenuQuitActionPerformed
 
     private void jMenuAboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuAboutActionPerformed
         JOptionPane.showMessageDialog(MassSieveFrame.this, this.getTitle() +
                 "\nLNT/NIMH/NIH\nCreated by Douglas J. Slotta\n" +
                 "Mass Spec. proficiency by Melinda A. McFarland\n" +
                 "\n" + checkAllocatedMem() +
                 "\n" + checkAvailMem() +
                 "\n" + checkMaxMem() +
                 "\n\n" + getSystemInfo());
     }//GEN-LAST:event_jMenuAboutActionPerformed
 
     private void jMenuResetLayoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuResetLayoutActionPerformed
         Component current = jTabbedPaneMain.getSelectedComponent();
         if (current instanceof ExperimentPanel) {
             currentExperiment = (ExperimentPanel) current;
             currentExperiment.resetDockModel();
         }
     }//GEN-LAST:event_jMenuResetLayoutActionPerformed
 
     private String getSystemInfo() {
         Properties p = System.getProperties();
         return "VM: " + p.getProperty("java.vendor") + " Java " + p.getProperty("java.version") +
                 "\nOS: " + p.getProperty("os.name") + " " + p.getProperty("os.version") +
                 " running on " + p.getProperty("os.arch");
     }
 
     private String checkAllocatedMem() {
         long val = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024;
         val /= 1024;
         String res = "Memory used: " + val + "MB";
         return res;
     }
 
     private String checkAvailMem() {
         long val = (Runtime.getRuntime().totalMemory()) / 1024;
         val /= 1024;
         String res = "Current memory available: " + val + "MB";
         return res;
     }
 
     private String checkMaxMem() {
         long val = (Runtime.getRuntime().maxMemory()) / 1024;
         val /= 1024;
         String res = "Max memory Availiable: " + val + "MB";
         return res;
     }
 
     public void setUseDigest(boolean b) {
         useDigest = b;
     }
 
     public void setDigestName(String s) {
         digestName = s;
     }
 
     public void setUseMultiColumnSort(boolean b) {
         useMultiColumnSort = b;
     }
 
     public boolean getUseDigest() {
         return useDigest;
     }
 
     public String getDigestName() {
         return digestName;
     }
 
     public boolean getUseMultiColumnSort() {
         return useMultiColumnSort;
     }
 
     public void setGraphLayout(GraphLayoutType glt) {
         glType = glt;
     }
 
     public GraphLayoutType getGraphLayout() {
         return glType;
     }
 
     /**
      * @param args the command line arguments
      */
     public static void main(String args[]) {
         java.awt.EventQueue.invokeLater(new Runnable() {
 
             public void run() {
                 new MassSieveFrame().setVisible(true);
             }
         });
     }
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.ButtonGroup buttonGroupTreeSource;
     private javax.swing.JFileChooser jFileChooserLoad;
     private javax.swing.JMenuItem jMenuAbout;
     private javax.swing.JMenuItem jMenuAddSearchResults;
     private javax.swing.JMenuBar jMenuBarMain;
     private javax.swing.JMenuItem jMenuBatchLoad;
     private javax.swing.JMenuItem jMenuClose;
     private javax.swing.JMenuItem jMenuCompareDiff;
     private javax.swing.JMenuItem jMenuCompareParsimony;
     private javax.swing.JMenuItem jMenuExportSeqDB;
     private javax.swing.JMenu jMenuFile;
     private javax.swing.JMenuItem jMenuFilterPrefs;
     private javax.swing.JMenuItem jMenuGarbageCollect;
     private javax.swing.JMenu jMenuHelp;
     private javax.swing.JMenuItem jMenuNewExperiment;
     private javax.swing.JMenuItem jMenuOpenExp;
     private javax.swing.JMenuItem jMenuOpenGenbankDB;
     private javax.swing.JMenuItem jMenuOpenSeqDB;
     private javax.swing.JMenuItem jMenuOptions;
     private javax.swing.JMenuItem jMenuQuit;
     private javax.swing.JMenuItem jMenuResetLayout;
     private javax.swing.JMenuItem jMenuSaveExp;
     private javax.swing.JMenuItem jMenuSaveExpSet;
     private javax.swing.JMenuItem jMenuShowSummary;
     private javax.swing.JMenu jMenuTools;
     private javax.swing.JOptionPane jOptionPaneAbout;
     private javax.swing.JSeparator jSeparator1;
     private javax.swing.JSeparator jSeparator2;
     private javax.swing.JSeparator jSeparator3;
     private javax.swing.JSeparator jSeparator4;
     private javax.swing.JSeparator jSeparator5;
     private javax.swing.JSeparator jSeparatorCompare;
     // End of variables declaration//GEN-END:variables
 }
