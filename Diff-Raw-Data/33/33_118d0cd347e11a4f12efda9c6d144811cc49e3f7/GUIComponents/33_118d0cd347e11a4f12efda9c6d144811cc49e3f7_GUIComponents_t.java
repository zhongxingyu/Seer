 package gui;
 
 import aniAdd.misc.Misc;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Toolkit;
 import java.awt.datatransfer.Clipboard;
 import java.awt.datatransfer.DataFlavor;
 import java.awt.datatransfer.StringSelection;
 import java.awt.datatransfer.Transferable;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.TreeMap;
 import javax.swing.JComponent;
 import javax.swing.JFileChooser;
 import javax.swing.JList;
 import javax.swing.JTree;
 import javax.swing.TransferHandler;
 import javax.swing.filechooser.FileFilter;
 import javax.swing.tree.DefaultMutableTreeNode;
 import processing.Mod_EpProcessing;
 import processing.TagSystem;
 
 /**
  *
  * @author Arokh
  */
 public abstract class GUIComponents extends javax.swing.JPanel {
 
     /** Creates new form MainUI */
     public GUIComponents() {
         initComponents();
         lbl_ImportantEvent.setVisible(false);
         pnl_AddFolderOnStartup.setVisible(false);
         chck_ShowEditboxes.setVisible(false);
         chck_ShowFileInfoPane.setVisible(false);
     }
 
     protected abstract void SaveToMem(String key, Object value);
     protected abstract Object LoadFromMem(String key, Object defVal);
 
     Component tagSystem;
     protected void UIInit(){
         InitDefaults();
         ToggleFileInfoPane();
         ToggleEditBoxes();
         ToggleFileMoving();
         ToggleFolderAutoAdd();
         TagSystemCodeChange();
 
         /*for (int i = 0; i < tbctrl_Main.getTabCount() && tagSystem == null; i++) {
             System.out.print(tbctrl_Main.getTabComponentAt(i));
             if(tbctrl_Main.getTabComponentAt(i).getName().equals(jPanel7.getName())){
                 tagSystem = tbctrl_Main.getTabComponentAt(i);
                 System.out.print("bladd");
             }
         }*/
         tagSystem = tbctrl_Main.getComponentAt(3);
         tbctrl_Main.remove(3);
 
         attachDragAndDrop(this);
     }
     protected void InitDefaults(){
         chck_MarkWatched.setSelected((Boolean)LoadFromMem("SetWatched", true));
         chck_AddToML.setSelected((Boolean)LoadFromMem("AddToMyList", true));
         chck_RenameMoveFiles.setSelected((Boolean)LoadFromMem("RenameFiles", false));
         cmb_Storage.setSelectedIndex((Integer)LoadFromMem("SetStorageType", 1));
 
         chck_AddFolderOnStartup.setSelected((Boolean)LoadFromMem("AddOnLoad", false));
         txt_FoldersToAdd.setText((String)LoadFromMem("FolderToAddOnLoad", ""));
 
         chck_EnableFileMoving.setSelected((Boolean)LoadFromMem("EnableFileMove", false));
 
         ptn_MoveToFolder.setSelected((Boolean)LoadFromMem("MoveTypeUseFolder", true));
         ptn_UseTaggingSystemFolder.setSelected(!(Boolean)LoadFromMem("MoveTypeUseFolder", true));
         txt_MoveToFolder.setText((String)LoadFromMem("MoveToFolder", ""));
         chck_AppendAnimeTitle.setSelected((Boolean)LoadFromMem("AppendAnimeTitle", true));
         cmb_AnimeTitleType.setSelectedIndex((Integer)LoadFromMem("AppendAnimeTitleType", 1));
 
         ptn_UseAniDBFN.setSelected((Boolean)LoadFromMem("RenameTypeAniDBFileName", true));
         ptn_UseTaggingSystemFile.setSelected(!(Boolean)LoadFromMem("RenameTypeAniDBFileName", true));
 
         chck_ShowFileInfoPane.setSelected((Boolean)LoadFromMem("ShowFileInfoPane", false));
         chck_ShowEditboxes.setSelected((Boolean)LoadFromMem("ShowSrcStrOtEditBoxes", false));
 
         txt_CodeBox.setText((String)LoadFromMem("TagSystemCode", txt_CodeBox.getText()));
     }
     protected void ToggleFileInfoPane(){
         SaveToMem("ShowFileInfoPane", chck_ShowFileInfoPane.isSelected());
         if(chck_ShowFileInfoPane.isSelected()){
             spnl_FileAdd.setDividerLocation(spnl_FileAdd.getDividerLocation() - 100);
             pnl_FileAdd_FileInfo.setVisible(true);
         } else {
             pnl_FileAdd_FileInfo.setVisible(false);
             spnl_FileAdd.setDividerLocation(-1*pnl_FileAdd_Ctrls.getY());
         }
     }
     protected void ToggleEditBoxes(){
         SaveToMem("ShowSrcStrOtEditBoxes", chck_ShowEditboxes.isSelected());
         pnl_EditBoxes.setVisible(chck_ShowEditboxes.isSelected());
     }
     protected void ToggleFileMoving(){
         SaveToMem("EnableFileMove", chck_EnableFileMoving.isSelected());
         ptn_MoveToFolder.setEnabled(chck_EnableFileMoving.isSelected());
         ptn_UseTaggingSystemFolder.setEnabled(chck_EnableFileMoving.isSelected());
         txt_MoveToFolder.setEnabled(chck_EnableFileMoving.isSelected());
         chck_AppendAnimeTitle.setEnabled(chck_EnableFileMoving.isSelected());
         cmb_AnimeTitleType.setEnabled(chck_EnableFileMoving.isSelected());
         chck_RenameMoveFiles.setText(chck_EnableFileMoving.isSelected()?"Move/Rename Files":"Rename Files");
     }
     protected void ToggleFolderAutoAdd(){
         SaveToMem("AddOnLoad", chck_AddFolderOnStartup.isSelected());
         txt_FoldersToAdd.setVisible(chck_AddFolderOnStartup.isSelected());
         txt_FoldersToAdd.setEnabled(chck_AddFolderOnStartup.isSelected());
     }
     protected void TagSystemCodeChange(){
         TagSystem ts = new TagSystem();
         TreeMap<String, String> tags = new TreeMap<String, String>();
         tags.put("ATr", txt_AT_Romaji.getText());
         tags.put("ATe", txt_AT_English.getText());
         tags.put("ATk", txt_AT_Kanji.getText());
         tags.put("ATs", txt_AT_Synomymn.getText());
         tags.put("ATo", txt_AT_Other.getText());
 
         tags.put("ETr", txt_ET_Romaji.getText());
         tags.put("ETe", txt_ET_English.getText());
         tags.put("ETk", txt_ET_Kanji.getText());
 
         tags.put("GTs", txt_GT_Short.getText());
         tags.put("GTl", txt_GT_Long.getText());
 
         tags.put("EpNo", updown_EpNo.getValue().toString());
         tags.put("EpHiNo", updown_EpHiNo.getValue().toString());
         tags.put("EpCount", updown_EpCount.getValue().toString());
 
         tags.put("Source", cmb_Source.getSelectedItem().toString());
         tags.put("Type", cmb_Type.getSelectedItem().toString());
 
         tags.put("Depr", chck_IsDeprecated.isSelected()?"1":"");
         tags.put("Cen", chck_IsCensored.isSelected()?"1":"");
 
         try {
             ts.parseAndTransform(txt_CodeBox.getText(), tags);
             lbl_FileNameStr.setText(tags.get("FileName"));
             lbl_DirNameStr.setText(tags.get("PathName"));
         } catch (Exception ex) {
             lbl_FileNameStr.setText("Error");
             lbl_DirNameStr.setText("Error");
         }
     }
 
     protected void AddPaths(boolean File) {
         JFileChooser FC = new javax.swing.JFileChooser();
         if (File) FC.setFileSelectionMode(JFileChooser.FILES_ONLY);  else  FC.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
 
         String lastDirectory = (String)LoadFromMem("LastDirectory", null);
         if(lastDirectory!=null) FC.setCurrentDirectory(new File(lastDirectory));
 
         FC.setMultiSelectionEnabled(true);
         FC.addChoosableFileFilter(new FileFilter() {
             public boolean accept(File file) {return file.isDirectory() || Misc.isVideoFile(file, Mod_EpProcessing.supportedFiles);}
             public String getDescription() {return "Video Files and Directories";}
         });
 
         if (FC.showOpenDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION) {
             ArrayList<File> files = new ArrayList<File>();
             
            for(File sf:FC.getSelectedFiles()){
                lastDirectory = sf.getParentFile().getAbsolutePath();
                files.addAll(Misc.getFiles(sf, Mod_EpProcessing.supportedFiles));
            }
            AddFiles(files);
            SaveToMem("LastDirectory", lastDirectory);
         }
 
         UpdateStatusLabels();
         UpdateProgressBars();
         tbl_Files.updateUI();
      }
     protected void DisplayEvent(String msg, Color color){
         lbl_ImportantEvent.setVisible(true);
         if(msg!=null) lbl_ImportantEvent.setText(msg);
         lbl_ImportantEvent.setOpaque(true);
         lbl_ImportantEvent.setBackground(color!=null?color:getBackground());
     }
     protected void UpdateProgressBars(double fileProg, double totalProg){
         prg_File.setValue((int)(fileProg*1000));
         prg_Total.setValue((int)(totalProg*1000));
     }
     protected void UpdateStatusLabels(long totalBytes, long totalProcessedBytes, long elapsed, long eta){
         lbl_MBProcessed.setText((totalProcessedBytes/1024/1024) + " of " + (totalBytes/1024/1024) + "MB");
         lbl_TimeElapsed.setText("Time elapsed: " + Misc.longToTime(elapsed));
         lbl_TimeRemaining.setText("Time remaining: " + Misc.longToTime(eta));
     }
     
     protected void ProcessingDone(){
         btn_Start.setText("Start");
     }
 
     private void attachDragAndDrop(JComponent c) {
        if(c instanceof JList) ((JList)c).setDragEnabled(false);
         c.setTransferHandler(new FSTransfer());
     }
     private static List<File> transferableToFileList(Transferable t){
         try			{
             List<File> files=new LinkedList<File>();
             if(t.isDataFlavorSupported(DataFlavor.stringFlavor)){
                 String data = (String)t.getTransferData(DataFlavor.stringFlavor);
                 BufferedReader buf=new BufferedReader(new StringReader(data));
                 String line;
                 while((line=buf.readLine())!=null){
                     if(line.startsWith("file:/")){
                         line=line.substring("file:/".length());
                         files.add(new File(line));
                     } else
                         System.out.println("Not sure how to read: "+line);
                 }
                 return files;
             } else if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)){
                 List data = (List)t.getTransferData(DataFlavor.javaFileListFlavor);
                 Iterator i = data.iterator();
                 while (i.hasNext())
                     files.add((File)i.next());
                 return files;
             }
             return null;
         } catch (Exception e){
             e.printStackTrace();
         }
         return null;
     }
     private class FSTransfer extends TransferHandler {
         static final long serialVersionUID=0;
         public boolean importData(JComponent comp, Transferable t){
             final List<File> files=transferableToFileList(t);
            final ArrayList<File> allFiles = new ArrayList<File>();
             if(files!=null){
                 new Runnable() {
                     public void run(){
                        for(File sf:files) allFiles.addAll(Misc.getFiles(sf, Mod_EpProcessing.supportedFiles));
                     }
                 }.run();
                AddFiles(allFiles);
                 UpdateStatusLabels();
                 UpdateProgressBars();
                 tbl_Files.updateUI();
                 return true;
             } else {
                 return false;
             }
         }
 
         public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
             return true;
         }
     }
 
     private void CopyTree(JTree tree){
         Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
         String cmdTree = CopyTree((DefaultMutableTreeNode)tree.getModel().getRoot(), 0);
         
         cb.setContents(new StringSelection(cmdTree), null);
     }
     private String CopyTree(DefaultMutableTreeNode node, int depth){
         String treeStr="";
         do {
             for(int i=0; i<depth;i++) treeStr += "  ";
             treeStr += (String)node.getUserObject() + "\n";
             if(node.getChildCount()!=0) treeStr += CopyTree((DefaultMutableTreeNode)node.getChildAt(0), depth+1);
         } while((node = node.getNextSibling()) != null);
         return treeStr;
     }
 
 
 
     protected abstract void UpdateProgressBars();
     protected abstract void UpdateStatusLabels();    
     protected abstract void AddFiles(ArrayList<File> files);
     
     protected abstract void ToggleMLCmd(boolean doAction);
     protected abstract void ToggleFileRename(boolean doAction);
     protected abstract void ToggleWatched(boolean doAction);
     protected abstract void ToggleStorageType(int type);
     
     protected abstract void ToggleProcessing(String type);
 
     protected abstract void CopyEvents();
 
 
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         ptngrp_Renaming = new javax.swing.ButtonGroup();
         ptngrp_Moving = new javax.swing.ButtonGroup();
         lbl_ImportantEvent = new javax.swing.JLabel();
         tbctrl_Main = new javax.swing.JTabbedPane();
         tp_FileAdd = new javax.swing.JPanel();
         spnl_FileAdd = new javax.swing.JSplitPane();
         pnl_FileAdd_Lstvw = new javax.swing.JPanel();
         scr_tbl_Files = new javax.swing.JScrollPane();
         tbl_Files = new gui.components.JExtTable();
         pnl_FileAdd_Cont = new javax.swing.JPanel();
         pnl_FileAdd_Status = new javax.swing.JPanel();
         prg_File = new javax.swing.JProgressBar();
         prg_Total = new javax.swing.JProgressBar();
         lbl_MBProcessed = new javax.swing.JLabel();
         lbl_TimeElapsed = new javax.swing.JLabel();
         lbl_TimeRemaining = new javax.swing.JLabel();
         pnl_FileAdd_FileInfo = new javax.swing.JPanel();
         pnl_FileAdd_Ctrls = new javax.swing.JPanel();
         cmb_Storage = new javax.swing.JComboBox();
         chck_RenameMoveFiles = new javax.swing.JCheckBox();
         chck_MarkWatched = new javax.swing.JCheckBox();
         chck_AddToML = new javax.swing.JCheckBox();
         pnl_EditBoxes = new javax.swing.JPanel();
         scrl_txt_Source = new javax.swing.JScrollPane();
         txt_Source = new gui.components.JHintTextArea();
         scrl_txt_Storage = new javax.swing.JScrollPane();
         txt_Storage = new gui.components.JHintTextArea();
         scrl_txt_Other = new javax.swing.JScrollPane();
         txt_Other = new gui.components.JHintTextArea();
         btn_AddFiles = new javax.swing.JButton();
         btn_AddFolders = new javax.swing.JButton();
         btn_Start = new javax.swing.JButton();
         tp_Logs = new javax.swing.JPanel();
         spnl_Logs = new javax.swing.JSplitPane();
         scrpn_trvw_Cmd = new javax.swing.JScrollPane();
         trvw_Cmd = new javax.swing.JTree();
         scrpn_trvw_Event = new javax.swing.JScrollPane();
         trvw_Event = new javax.swing.JTree();
         pnl_Logs_Ctrls = new javax.swing.JPanel();
         btn_CopyCmdTree = new javax.swing.JButton();
         btn_CopyLogTree = new javax.swing.JButton();
         btn_CopyDebugMsgs = new javax.swing.JButton();
         btn_ClearLogs = new javax.swing.JButton();
         tp_Options = new javax.swing.JPanel();
         chck_ShowFileInfoPane = new javax.swing.JCheckBox();
         chck_ShowEditboxes = new javax.swing.JCheckBox();
         pnl_AddFolderOnStartup = new javax.swing.JPanel();
         chck_AddFolderOnStartup = new javax.swing.JCheckBox();
         scrl_txt_FoldersToAdd = new javax.swing.JScrollPane();
         txt_FoldersToAdd = new javax.swing.JTextArea();
         pnl_FileRenamingMoving = new javax.swing.JPanel();
         chck_EnableFileMoving = new javax.swing.JCheckBox();
         pnl_FileMove = new javax.swing.JPanel();
         ptn_MoveToFolder = new javax.swing.JRadioButton();
         ptn_UseTaggingSystemFolder = new javax.swing.JRadioButton();
         txt_MoveToFolder = new javax.swing.JTextField();
         chck_AppendAnimeTitle = new javax.swing.JCheckBox();
         cmb_AnimeTitleType = new javax.swing.JComboBox();
         pnl_FileRename = new javax.swing.JPanel();
         lbl_Renaming = new javax.swing.JLabel();
         ptn_UseAniDBFN = new javax.swing.JRadioButton();
         ptn_UseTaggingSystemFile = new javax.swing.JRadioButton();
         btn_EditTagsystem = new javax.swing.JButton();
         tp_TagSystem = new javax.swing.JPanel();
         pnl_AnimeTitles = new javax.swing.JPanel();
         lbl_AnimeTitles = new javax.swing.JLabel();
         lbl_AT_English = new javax.swing.JLabel();
         txt_AT_English = new javax.swing.JTextField();
         lbl_AT_Romaji = new javax.swing.JLabel();
         txt_AT_Romaji = new javax.swing.JTextField();
         lbl_AT_Kanji = new javax.swing.JLabel();
         txt_AT_Kanji = new javax.swing.JTextField();
         lbl_AT_Other = new javax.swing.JLabel();
         txt_AT_Other = new javax.swing.JTextField();
         lbl_AT_Synomymn = new javax.swing.JLabel();
         txt_AT_Synomymn = new javax.swing.JTextField();
         pnl_GroupNames = new javax.swing.JPanel();
         lbl_GroupNames = new javax.swing.JLabel();
         lbl_GT_Short = new javax.swing.JLabel();
         txt_GT_Short = new javax.swing.JTextField();
         lbl_GT_Long = new javax.swing.JLabel();
         txt_GT_Long = new javax.swing.JTextField();
         pnl_EpTitles = new javax.swing.JPanel();
         lbl_EpTitles = new javax.swing.JLabel();
         lbl_ET_English = new javax.swing.JLabel();
         txt_ET_English = new javax.swing.JTextField();
         lbl_ET_Romaji = new javax.swing.JLabel();
         txt_ET_Romaji = new javax.swing.JTextField();
         lbl_ET_Kanji = new javax.swing.JLabel();
         txt_ET_Kanji = new javax.swing.JTextField();
         scrl_txt_CodeBox = new javax.swing.JScrollPane();
         txt_CodeBox = new javax.swing.JTextArea();
         lbl_FileName = new javax.swing.JLabel();
         lbl_DirName = new javax.swing.JLabel();
         lbl_DirNameStr = new javax.swing.JLabel();
         lbl_FileNameStr = new javax.swing.JLabel();
         pnl_Misc = new javax.swing.JPanel();
         lbl_Misc = new javax.swing.JLabel();
         chck_IsCensored = new javax.swing.JCheckBox();
         chck_IsDeprecated = new javax.swing.JCheckBox();
         lbl_EpNo = new javax.swing.JLabel();
         updown_EpNo = new javax.swing.JSpinner();
         lbl_EpHiNo = new javax.swing.JLabel();
         updown_EpHiNo = new javax.swing.JSpinner();
         lbl_EpCount = new javax.swing.JLabel();
         updown_EpCount = new javax.swing.JSpinner();
         lbl_Type = new javax.swing.JLabel();
         cmb_Type = new javax.swing.JComboBox();
         lbl_Source = new javax.swing.JLabel();
         cmb_Source = new javax.swing.JComboBox();
         tp_Help = new javax.swing.JPanel();
         lbl_Icons = new javax.swing.JLabel();
         ctrl_IconHelp = new gui.IconHelp();
 
         lbl_ImportantEvent.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         lbl_ImportantEvent.setText("EVENTS");
 
         tbctrl_Main.addChangeListener(new javax.swing.event.ChangeListener() {
             public void stateChanged(javax.swing.event.ChangeEvent evt) {
                 tbctrl_MainStateChanged(evt);
             }
         });
         tbctrl_Main.addAncestorListener(new javax.swing.event.AncestorListener() {
             public void ancestorMoved(javax.swing.event.AncestorEvent evt) {
             }
             public void ancestorAdded(javax.swing.event.AncestorEvent evt) {
                 tbctrl_MainAncestorAdded(evt);
             }
             public void ancestorRemoved(javax.swing.event.AncestorEvent evt) {
             }
         });
 
         tp_FileAdd.setOpaque(false);
 
         spnl_FileAdd.setDividerLocation(340);
         spnl_FileAdd.setDividerSize(2);
         spnl_FileAdd.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
         spnl_FileAdd.setResizeWeight(1.0);
         spnl_FileAdd.setOpaque(false);
 
         pnl_FileAdd_Lstvw.setBorder(javax.swing.BorderFactory.createEtchedBorder());
         pnl_FileAdd_Lstvw.setOpaque(false);
 
         tbl_Files.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
 
             },
             new String [] {
 
             }
         ));
         scr_tbl_Files.setViewportView(tbl_Files);
 
         javax.swing.GroupLayout pnl_FileAdd_LstvwLayout = new javax.swing.GroupLayout(pnl_FileAdd_Lstvw);
         pnl_FileAdd_Lstvw.setLayout(pnl_FileAdd_LstvwLayout);
         pnl_FileAdd_LstvwLayout.setHorizontalGroup(
             pnl_FileAdd_LstvwLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(scr_tbl_Files, javax.swing.GroupLayout.DEFAULT_SIZE, 789, Short.MAX_VALUE)
         );
         pnl_FileAdd_LstvwLayout.setVerticalGroup(
             pnl_FileAdd_LstvwLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(scr_tbl_Files, javax.swing.GroupLayout.DEFAULT_SIZE, 335, Short.MAX_VALUE)
         );
 
         spnl_FileAdd.setLeftComponent(pnl_FileAdd_Lstvw);
 
         pnl_FileAdd_Cont.setOpaque(false);
 
         pnl_FileAdd_Status.setOpaque(false);
 
         prg_File.setMaximum(1000);
 
         prg_Total.setMaximum(1000);
 
         lbl_MBProcessed.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         lbl_MBProcessed.setText("0 of 0 MB processed");
 
         lbl_TimeElapsed.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         lbl_TimeElapsed.setText("Time elapsed: 0:00:00");
 
         lbl_TimeRemaining.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         lbl_TimeRemaining.setText("Time remaining: 0:00:00");
 
         javax.swing.GroupLayout pnl_FileAdd_StatusLayout = new javax.swing.GroupLayout(pnl_FileAdd_Status);
         pnl_FileAdd_Status.setLayout(pnl_FileAdd_StatusLayout);
         pnl_FileAdd_StatusLayout.setHorizontalGroup(
             pnl_FileAdd_StatusLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(prg_File, javax.swing.GroupLayout.DEFAULT_SIZE, 793, Short.MAX_VALUE)
             .addComponent(prg_Total, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 793, Short.MAX_VALUE)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnl_FileAdd_StatusLayout.createSequentialGroup()
                 .addComponent(lbl_MBProcessed, javax.swing.GroupLayout.PREFERRED_SIZE, 274, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(lbl_TimeElapsed, javax.swing.GroupLayout.DEFAULT_SIZE, 203, Short.MAX_VALUE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(lbl_TimeRemaining, javax.swing.GroupLayout.PREFERRED_SIZE, 304, javax.swing.GroupLayout.PREFERRED_SIZE))
         );
         pnl_FileAdd_StatusLayout.setVerticalGroup(
             pnl_FileAdd_StatusLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnl_FileAdd_StatusLayout.createSequentialGroup()
                 .addComponent(prg_File, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(0, 0, 0)
                 .addComponent(prg_Total, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(0, 0, 0)
                 .addGroup(pnl_FileAdd_StatusLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(lbl_MBProcessed, javax.swing.GroupLayout.DEFAULT_SIZE, 15, Short.MAX_VALUE)
                     .addComponent(lbl_TimeElapsed, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(lbl_TimeRemaining)))
         );
 
         pnl_FileAdd_FileInfo.setBorder(javax.swing.BorderFactory.createTitledBorder("File Information"));
         pnl_FileAdd_FileInfo.setOpaque(false);
 
         javax.swing.GroupLayout pnl_FileAdd_FileInfoLayout = new javax.swing.GroupLayout(pnl_FileAdd_FileInfo);
         pnl_FileAdd_FileInfo.setLayout(pnl_FileAdd_FileInfoLayout);
         pnl_FileAdd_FileInfoLayout.setHorizontalGroup(
             pnl_FileAdd_FileInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 781, Short.MAX_VALUE)
         );
         pnl_FileAdd_FileInfoLayout.setVerticalGroup(
             pnl_FileAdd_FileInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 95, Short.MAX_VALUE)
         );
 
         pnl_FileAdd_Ctrls.setBorder(javax.swing.BorderFactory.createEtchedBorder());
         pnl_FileAdd_Ctrls.setOpaque(false);
 
         cmb_Storage.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Unknown", "Internal (HDD)", "External (CD/DVD)", "Deleted" }));
         cmb_Storage.setSelectedIndex(1);
         cmb_Storage.addItemListener(new java.awt.event.ItemListener() {
             public void itemStateChanged(java.awt.event.ItemEvent evt) {
                 cmb_StorageItemStateChanged(evt);
             }
         });
 
         chck_RenameMoveFiles.setText("Move/Rename Files");
         chck_RenameMoveFiles.setOpaque(false);
         chck_RenameMoveFiles.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 chck_RenameMoveFilesActionPerformed(evt);
             }
         });
 
         chck_MarkWatched.setText("Mark Watched");
         chck_MarkWatched.setOpaque(false);
         chck_MarkWatched.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 chck_MarkWatchedActionPerformed(evt);
             }
         });
 
         chck_AddToML.setSelected(true);
         chck_AddToML.setText("Add File to MyList");
         chck_AddToML.setOpaque(false);
         chck_AddToML.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 chck_AddToMLActionPerformed(evt);
             }
         });
 
         pnl_EditBoxes.setOpaque(false);
 
         txt_Source.setColumns(8);
         txt_Source.setRows(3);
         txt_Source.setFont(new java.awt.Font("Tahoma", 0, 10));
         txt_Source.setTextHint("Source");
         scrl_txt_Source.setViewportView(txt_Source);
 
         txt_Storage.setColumns(8);
         txt_Storage.setRows(3);
         txt_Storage.setFont(new java.awt.Font("Tahoma", 0, 10));
         txt_Storage.setTextHint("Storage");
         scrl_txt_Storage.setViewportView(txt_Storage);
 
         txt_Other.setColumns(8);
         txt_Other.setRows(3);
         txt_Other.setFont(new java.awt.Font("Tahoma", 0, 10));
         txt_Other.setTextHint("Other");
         scrl_txt_Other.setViewportView(txt_Other);
 
         javax.swing.GroupLayout pnl_EditBoxesLayout = new javax.swing.GroupLayout(pnl_EditBoxes);
         pnl_EditBoxes.setLayout(pnl_EditBoxesLayout);
         pnl_EditBoxesLayout.setHorizontalGroup(
             pnl_EditBoxesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnl_EditBoxesLayout.createSequentialGroup()
                 .addComponent(scrl_txt_Source, javax.swing.GroupLayout.DEFAULT_SIZE, 98, Short.MAX_VALUE)
                 .addGap(0, 0, 0)
                 .addComponent(scrl_txt_Storage, javax.swing.GroupLayout.DEFAULT_SIZE, 108, Short.MAX_VALUE)
                 .addGap(0, 0, 0)
                 .addComponent(scrl_txt_Other, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE))
         );
         pnl_EditBoxesLayout.setVerticalGroup(
             pnl_EditBoxesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(scrl_txt_Source, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 46, Short.MAX_VALUE)
             .addComponent(scrl_txt_Other, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 46, Short.MAX_VALUE)
             .addComponent(scrl_txt_Storage, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 46, Short.MAX_VALUE)
         );
 
         btn_AddFiles.setText("Add Files");
         btn_AddFiles.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btn_AddFilesActionPerformed(evt);
             }
         });
 
         btn_AddFolders.setText("Add Folders");
         btn_AddFolders.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btn_AddFoldersActionPerformed(evt);
             }
         });
 
         btn_Start.setText("Start");
         btn_Start.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btn_StartActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout pnl_FileAdd_CtrlsLayout = new javax.swing.GroupLayout(pnl_FileAdd_Ctrls);
         pnl_FileAdd_Ctrls.setLayout(pnl_FileAdd_CtrlsLayout);
         pnl_FileAdd_CtrlsLayout.setHorizontalGroup(
             pnl_FileAdd_CtrlsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnl_FileAdd_CtrlsLayout.createSequentialGroup()
                 .addGroup(pnl_FileAdd_CtrlsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(cmb_Storage, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addGroup(pnl_FileAdd_CtrlsLayout.createSequentialGroup()
                         .addGap(6, 6, 6)
                         .addComponent(chck_AddToML, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(pnl_FileAdd_CtrlsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(chck_MarkWatched, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(chck_RenameMoveFiles, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(0, 0, 0)
                 .addComponent(pnl_EditBoxes, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addGap(0, 0, 0)
                 .addGroup(pnl_FileAdd_CtrlsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(btn_AddFiles, javax.swing.GroupLayout.DEFAULT_SIZE, 145, Short.MAX_VALUE)
                     .addComponent(btn_AddFolders, javax.swing.GroupLayout.DEFAULT_SIZE, 145, Short.MAX_VALUE))
                 .addGap(0, 0, 0)
                 .addComponent(btn_Start))
         );
         pnl_FileAdd_CtrlsLayout.setVerticalGroup(
             pnl_FileAdd_CtrlsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnl_FileAdd_CtrlsLayout.createSequentialGroup()
                 .addGroup(pnl_FileAdd_CtrlsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(cmb_Storage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(chck_MarkWatched))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(pnl_FileAdd_CtrlsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(chck_AddToML)
                     .addComponent(chck_RenameMoveFiles)))
             .addGroup(pnl_FileAdd_CtrlsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                 .addComponent(pnl_EditBoxes, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addComponent(btn_Start, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pnl_FileAdd_CtrlsLayout.createSequentialGroup()
                     .addComponent(btn_AddFolders)
                     .addGap(0, 0, 0)
                     .addComponent(btn_AddFiles)))
         );
 
         javax.swing.GroupLayout pnl_FileAdd_ContLayout = new javax.swing.GroupLayout(pnl_FileAdd_Cont);
         pnl_FileAdd_Cont.setLayout(pnl_FileAdd_ContLayout);
         pnl_FileAdd_ContLayout.setHorizontalGroup(
             pnl_FileAdd_ContLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(pnl_FileAdd_FileInfo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
             .addComponent(pnl_FileAdd_Status, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
             .addComponent(pnl_FileAdd_Ctrls, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         );
         pnl_FileAdd_ContLayout.setVerticalGroup(
             pnl_FileAdd_ContLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnl_FileAdd_ContLayout.createSequentialGroup()
                 .addComponent(pnl_FileAdd_FileInfo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addGap(0, 0, 0)
                 .addComponent(pnl_FileAdd_Status, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(0, 0, 0)
                 .addComponent(pnl_FileAdd_Ctrls, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
         );
 
         spnl_FileAdd.setRightComponent(pnl_FileAdd_Cont);
 
         javax.swing.GroupLayout tp_FileAddLayout = new javax.swing.GroupLayout(tp_FileAdd);
         tp_FileAdd.setLayout(tp_FileAddLayout);
         tp_FileAddLayout.setHorizontalGroup(
             tp_FileAddLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(spnl_FileAdd, javax.swing.GroupLayout.DEFAULT_SIZE, 795, Short.MAX_VALUE)
         );
         tp_FileAddLayout.setVerticalGroup(
             tp_FileAddLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(spnl_FileAdd, javax.swing.GroupLayout.DEFAULT_SIZE, 558, Short.MAX_VALUE)
         );
 
         tbctrl_Main.addTab("File Add", tp_FileAdd);
 
         tp_Logs.setOpaque(false);
 
         spnl_Logs.setDividerLocation(200);
         spnl_Logs.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
         spnl_Logs.setResizeWeight(0.5);
 
         javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("Cmds");
         trvw_Cmd.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
         scrpn_trvw_Cmd.setViewportView(trvw_Cmd);
 
         spnl_Logs.setTopComponent(scrpn_trvw_Cmd);
 
         treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("Event Log");
         trvw_Event.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
         scrpn_trvw_Event.setViewportView(trvw_Event);
 
         spnl_Logs.setRightComponent(scrpn_trvw_Event);
 
         pnl_Logs_Ctrls.setOpaque(false);
 
         btn_CopyCmdTree.setText("Copy Cmd Log Tree");
         btn_CopyCmdTree.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btn_CopyCmdTreeActionPerformed(evt);
             }
         });
 
         btn_CopyLogTree.setText("Copy Event Log Tree");
         btn_CopyLogTree.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btn_CopyLogTreeActionPerformed(evt);
             }
         });
 
         btn_CopyDebugMsgs.setText("Copy Debug Messages");
         btn_CopyDebugMsgs.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btn_CopyDebugMsgsActionPerformed(evt);
             }
         });
 
         btn_ClearLogs.setText("Clear Logs");
 
         javax.swing.GroupLayout pnl_Logs_CtrlsLayout = new javax.swing.GroupLayout(pnl_Logs_Ctrls);
         pnl_Logs_Ctrls.setLayout(pnl_Logs_CtrlsLayout);
         pnl_Logs_CtrlsLayout.setHorizontalGroup(
             pnl_Logs_CtrlsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnl_Logs_CtrlsLayout.createSequentialGroup()
                 .addComponent(btn_CopyCmdTree)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(btn_CopyLogTree)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(btn_CopyDebugMsgs)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 299, Short.MAX_VALUE)
                 .addComponent(btn_ClearLogs))
         );
         pnl_Logs_CtrlsLayout.setVerticalGroup(
             pnl_Logs_CtrlsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnl_Logs_CtrlsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                 .addComponent(btn_CopyCmdTree)
                 .addComponent(btn_CopyLogTree)
                 .addComponent(btn_CopyDebugMsgs)
                 .addComponent(btn_ClearLogs))
         );
 
         javax.swing.GroupLayout tp_LogsLayout = new javax.swing.GroupLayout(tp_Logs);
         tp_Logs.setLayout(tp_LogsLayout);
         tp_LogsLayout.setHorizontalGroup(
             tp_LogsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(pnl_Logs_Ctrls, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
             .addComponent(spnl_Logs, javax.swing.GroupLayout.DEFAULT_SIZE, 795, Short.MAX_VALUE)
         );
         tp_LogsLayout.setVerticalGroup(
             tp_LogsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, tp_LogsLayout.createSequentialGroup()
                 .addComponent(spnl_Logs, javax.swing.GroupLayout.DEFAULT_SIZE, 529, Short.MAX_VALUE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(pnl_Logs_Ctrls, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
         );
 
         tbctrl_Main.addTab("Logs", tp_Logs);
 
         tp_Options.setOpaque(false);
 
         chck_ShowFileInfoPane.setLabel("Show Fileinfo Pane");
         chck_ShowFileInfoPane.setOpaque(false);
         chck_ShowFileInfoPane.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 chck_ShowFileInfoPaneActionPerformed(evt);
             }
         });
 
         chck_ShowEditboxes.setText("Show Storage/Sorce/Other Editboxes");
         chck_ShowEditboxes.setOpaque(false);
         chck_ShowEditboxes.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 chck_ShowEditboxesActionPerformed(evt);
             }
         });
 
         pnl_AddFolderOnStartup.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
         pnl_AddFolderOnStartup.setOpaque(false);
 
         chck_AddFolderOnStartup.setText("Add following folders on Startup:");
         chck_AddFolderOnStartup.setOpaque(false);
         chck_AddFolderOnStartup.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 chck_AddFolderOnStartupActionPerformed(evt);
             }
         });
 
         txt_FoldersToAdd.setColumns(20);
         txt_FoldersToAdd.setFont(new java.awt.Font("Tahoma", 0, 10));
         txt_FoldersToAdd.setRows(5);
         txt_FoldersToAdd.setEnabled(false);
         txt_FoldersToAdd.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyReleased(java.awt.event.KeyEvent evt) {
                 txt_FoldersToAddKeyReleased(evt);
             }
         });
         scrl_txt_FoldersToAdd.setViewportView(txt_FoldersToAdd);
 
         javax.swing.GroupLayout pnl_AddFolderOnStartupLayout = new javax.swing.GroupLayout(pnl_AddFolderOnStartup);
         pnl_AddFolderOnStartup.setLayout(pnl_AddFolderOnStartupLayout);
         pnl_AddFolderOnStartupLayout.setHorizontalGroup(
             pnl_AddFolderOnStartupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnl_AddFolderOnStartupLayout.createSequentialGroup()
                 .addComponent(chck_AddFolderOnStartup)
                 .addContainerGap(88, Short.MAX_VALUE))
             .addComponent(scrl_txt_FoldersToAdd, javax.swing.GroupLayout.DEFAULT_SIZE, 271, Short.MAX_VALUE)
         );
         pnl_AddFolderOnStartupLayout.setVerticalGroup(
             pnl_AddFolderOnStartupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnl_AddFolderOnStartupLayout.createSequentialGroup()
                 .addComponent(chck_AddFolderOnStartup)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addComponent(scrl_txt_FoldersToAdd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
         );
 
         pnl_FileRenamingMoving.setBorder(javax.swing.BorderFactory.createTitledBorder("File Renaming/Moving"));
         pnl_FileRenamingMoving.setOpaque(false);
 
         chck_EnableFileMoving.setText("Enable Filemoving");
         chck_EnableFileMoving.setOpaque(false);
         chck_EnableFileMoving.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 chck_EnableFileMovingActionPerformed(evt);
             }
         });
 
         pnl_FileMove.setOpaque(false);
 
         ptngrp_Moving.add(ptn_MoveToFolder);
         ptn_MoveToFolder.setSelected(true);
         ptn_MoveToFolder.setText("Move to Folder");
         ptn_MoveToFolder.setEnabled(false);
         ptn_MoveToFolder.setOpaque(false);
         ptn_MoveToFolder.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 ptn_MoveToFolderActionPerformed(evt);
             }
         });
 
         ptngrp_Moving.add(ptn_UseTaggingSystemFolder);
         ptn_UseTaggingSystemFolder.setText("Use Tagging System");
         ptn_UseTaggingSystemFolder.setEnabled(false);
         ptn_UseTaggingSystemFolder.setOpaque(false);
         ptn_UseTaggingSystemFolder.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 ptn_UseTaggingSystemFolderActionPerformed(evt);
             }
         });
 
         txt_MoveToFolder.setEnabled(false);
         txt_MoveToFolder.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyReleased(java.awt.event.KeyEvent evt) {
                 txt_MoveToFolderKeyReleased(evt);
             }
         });
 
         chck_AppendAnimeTitle.setText("Append Anime Title");
         chck_AppendAnimeTitle.setEnabled(false);
         chck_AppendAnimeTitle.setOpaque(false);
         chck_AppendAnimeTitle.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 chck_AppendAnimeTitleActionPerformed(evt);
             }
         });
 
         cmb_AnimeTitleType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "English", "Romaji", "Kanji" }));
         cmb_AnimeTitleType.setSelectedIndex(1);
         cmb_AnimeTitleType.setEnabled(false);
         cmb_AnimeTitleType.addItemListener(new java.awt.event.ItemListener() {
             public void itemStateChanged(java.awt.event.ItemEvent evt) {
                 cmb_AnimeTitleTypeItemStateChanged(evt);
             }
         });
 
         javax.swing.GroupLayout pnl_FileMoveLayout = new javax.swing.GroupLayout(pnl_FileMove);
         pnl_FileMove.setLayout(pnl_FileMoveLayout);
         pnl_FileMoveLayout.setHorizontalGroup(
             pnl_FileMoveLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnl_FileMoveLayout.createSequentialGroup()
                 .addGap(15, 15, 15)
                 .addGroup(pnl_FileMoveLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(ptn_UseTaggingSystemFolder)
                     .addGroup(pnl_FileMoveLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                         .addGroup(pnl_FileMoveLayout.createSequentialGroup()
                             .addComponent(ptn_MoveToFolder)
                             .addGap(149, 149, 149))
                         .addGroup(pnl_FileMoveLayout.createSequentialGroup()
                             .addGap(21, 21, 21)
                             .addGroup(pnl_FileMoveLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                 .addGroup(pnl_FileMoveLayout.createSequentialGroup()
                                     .addComponent(chck_AppendAnimeTitle)
                                     .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                     .addComponent(cmb_AnimeTitleType, 0, 106, Short.MAX_VALUE))
                                 .addComponent(txt_MoveToFolder)))))
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
         pnl_FileMoveLayout.setVerticalGroup(
             pnl_FileMoveLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnl_FileMoveLayout.createSequentialGroup()
                 .addComponent(ptn_MoveToFolder)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(txt_MoveToFolder, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(pnl_FileMoveLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(chck_AppendAnimeTitle)
                     .addComponent(cmb_AnimeTitleType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(ptn_UseTaggingSystemFolder)
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         pnl_FileRename.setOpaque(false);
 
         lbl_Renaming.setText("Renaming:");
 
         ptngrp_Renaming.add(ptn_UseAniDBFN);
         ptn_UseAniDBFN.setSelected(true);
         ptn_UseAniDBFN.setText("Use AniDB Filename");
         ptn_UseAniDBFN.setOpaque(false);
         ptn_UseAniDBFN.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 ptn_UseAniDBFNActionPerformed(evt);
             }
         });
 
         ptngrp_Renaming.add(ptn_UseTaggingSystemFile);
         ptn_UseTaggingSystemFile.setText("Use Tagging System");
         ptn_UseTaggingSystemFile.setOpaque(false);
         ptn_UseTaggingSystemFile.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 ptn_UseTaggingSystemFileActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout pnl_FileRenameLayout = new javax.swing.GroupLayout(pnl_FileRename);
         pnl_FileRename.setLayout(pnl_FileRenameLayout);
         pnl_FileRenameLayout.setHorizontalGroup(
             pnl_FileRenameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnl_FileRenameLayout.createSequentialGroup()
                 .addGap(10, 10, 10)
                 .addComponent(ptn_UseAniDBFN)
                 .addGap(18, 18, 18)
                 .addComponent(ptn_UseTaggingSystemFile))
             .addComponent(lbl_Renaming)
         );
         pnl_FileRenameLayout.setVerticalGroup(
             pnl_FileRenameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnl_FileRenameLayout.createSequentialGroup()
                 .addComponent(lbl_Renaming)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(pnl_FileRenameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(ptn_UseAniDBFN)
                     .addComponent(ptn_UseTaggingSystemFile)))
         );
 
         btn_EditTagsystem.setText("Edit Tagging System");
         btn_EditTagsystem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btn_EditTagsystemActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout pnl_FileRenamingMovingLayout = new javax.swing.GroupLayout(pnl_FileRenamingMoving);
         pnl_FileRenamingMoving.setLayout(pnl_FileRenamingMovingLayout);
         pnl_FileRenamingMovingLayout.setHorizontalGroup(
             pnl_FileRenamingMovingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(chck_EnableFileMoving)
             .addComponent(pnl_FileMove, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
             .addGroup(pnl_FileRenamingMovingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                 .addComponent(btn_EditTagsystem, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addComponent(pnl_FileRename, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
         pnl_FileRenamingMovingLayout.setVerticalGroup(
             pnl_FileRenamingMovingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnl_FileRenamingMovingLayout.createSequentialGroup()
                 .addComponent(chck_EnableFileMoving)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(pnl_FileMove, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(pnl_FileRename, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(btn_EditTagsystem, javax.swing.GroupLayout.DEFAULT_SIZE, 34, Short.MAX_VALUE))
         );
 
         javax.swing.GroupLayout tp_OptionsLayout = new javax.swing.GroupLayout(tp_Options);
         tp_Options.setLayout(tp_OptionsLayout);
         tp_OptionsLayout.setHorizontalGroup(
             tp_OptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(tp_OptionsLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(tp_OptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                     .addComponent(pnl_AddFolderOnStartup, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(pnl_FileRenamingMoving, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(tp_OptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(chck_ShowEditboxes)
                     .addComponent(chck_ShowFileInfoPane))
                 .addContainerGap(291, Short.MAX_VALUE))
         );
         tp_OptionsLayout.setVerticalGroup(
             tp_OptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(tp_OptionsLayout.createSequentialGroup()
                 .addGroup(tp_OptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(tp_OptionsLayout.createSequentialGroup()
                         .addComponent(pnl_AddFolderOnStartup, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(pnl_FileRenamingMoving, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addGroup(tp_OptionsLayout.createSequentialGroup()
                         .addComponent(chck_ShowFileInfoPane)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(chck_ShowEditboxes)))
                 .addContainerGap(203, Short.MAX_VALUE))
         );
 
         tbctrl_Main.addTab("Options", tp_Options);
 
         tp_TagSystem.setOpaque(false);
 
         pnl_AnimeTitles.setOpaque(false);
 
         lbl_AnimeTitles.setText("Anime Titles:");
 
         lbl_AT_English.setText("English");
 
         txt_AT_English.setText("The Melancholy of Haruhi Suzumiya (2009)");
         txt_AT_English.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyReleased(java.awt.event.KeyEvent evt) {
                 TxtChange(evt);
             }
         });
 
         lbl_AT_Romaji.setText("Romaji");
 
         txt_AT_Romaji.setText("Suzumiya Haruhi no Yuuutsu (2009)");
         txt_AT_Romaji.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyReleased(java.awt.event.KeyEvent evt) {
                 TxtChange(evt);
             }
         });
 
         lbl_AT_Kanji.setText("Kanji");
 
         txt_AT_Kanji.setText("涼宮ハルヒの憂鬱 (2009)");
         txt_AT_Kanji.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyReleased(java.awt.event.KeyEvent evt) {
                 TxtChange(evt);
             }
         });
 
         lbl_AT_Other.setText("Other");
 
         txt_AT_Other.setText("haruhi2");
         txt_AT_Other.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyReleased(java.awt.event.KeyEvent evt) {
                 TxtChange(evt);
             }
         });
 
         lbl_AT_Synomymn.setText("Synonymn");
 
         txt_AT_Synomymn.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyReleased(java.awt.event.KeyEvent evt) {
                 TxtChange(evt);
             }
         });
 
         javax.swing.GroupLayout pnl_AnimeTitlesLayout = new javax.swing.GroupLayout(pnl_AnimeTitles);
         pnl_AnimeTitles.setLayout(pnl_AnimeTitlesLayout);
         pnl_AnimeTitlesLayout.setHorizontalGroup(
             pnl_AnimeTitlesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnl_AnimeTitlesLayout.createSequentialGroup()
                 .addComponent(lbl_AnimeTitles)
                 .addContainerGap(318, Short.MAX_VALUE))
             .addGroup(pnl_AnimeTitlesLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(pnl_AnimeTitlesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(lbl_AT_English)
                     .addComponent(lbl_AT_Romaji)
                     .addComponent(lbl_AT_Kanji)
                     .addComponent(lbl_AT_Other)
                     .addComponent(lbl_AT_Synomymn))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(pnl_AnimeTitlesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(txt_AT_Synomymn, javax.swing.GroupLayout.DEFAULT_SIZE, 315, Short.MAX_VALUE)
                     .addComponent(txt_AT_Other, javax.swing.GroupLayout.DEFAULT_SIZE, 315, Short.MAX_VALUE)
                     .addComponent(txt_AT_Kanji, javax.swing.GroupLayout.DEFAULT_SIZE, 315, Short.MAX_VALUE)
                     .addComponent(txt_AT_Romaji, javax.swing.GroupLayout.DEFAULT_SIZE, 315, Short.MAX_VALUE)
                     .addComponent(txt_AT_English, javax.swing.GroupLayout.DEFAULT_SIZE, 315, Short.MAX_VALUE)))
         );
         pnl_AnimeTitlesLayout.setVerticalGroup(
             pnl_AnimeTitlesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnl_AnimeTitlesLayout.createSequentialGroup()
                 .addComponent(lbl_AnimeTitles)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(pnl_AnimeTitlesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(txt_AT_English, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(lbl_AT_English))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(pnl_AnimeTitlesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(txt_AT_Romaji, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(lbl_AT_Romaji))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(pnl_AnimeTitlesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(txt_AT_Kanji, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(lbl_AT_Kanji))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(pnl_AnimeTitlesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(txt_AT_Other, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(lbl_AT_Other))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addGroup(pnl_AnimeTitlesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(txt_AT_Synomymn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(lbl_AT_Synomymn)))
         );
 
         pnl_GroupNames.setOpaque(false);
 
         lbl_GroupNames.setText("Group Names:");
 
         lbl_GT_Short.setText("Short");
 
         txt_GT_Short.setText("a.f.k.");
         txt_GT_Short.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyReleased(java.awt.event.KeyEvent evt) {
                 TxtChange(evt);
             }
         });
 
         lbl_GT_Long.setText("Long");
 
         txt_GT_Long.setText("a.f.k. (Long)");
         txt_GT_Long.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyReleased(java.awt.event.KeyEvent evt) {
                 TxtChange(evt);
             }
         });
 
         javax.swing.GroupLayout pnl_GroupNamesLayout = new javax.swing.GroupLayout(pnl_GroupNames);
         pnl_GroupNames.setLayout(pnl_GroupNamesLayout);
         pnl_GroupNamesLayout.setHorizontalGroup(
             pnl_GroupNamesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnl_GroupNamesLayout.createSequentialGroup()
                 .addComponent(lbl_GroupNames)
                 .addContainerGap(311, Short.MAX_VALUE))
             .addGroup(pnl_GroupNamesLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(pnl_GroupNamesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(lbl_GT_Short)
                     .addComponent(lbl_GT_Long))
                 .addGap(21, 21, 21)
                 .addGroup(pnl_GroupNamesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(txt_GT_Long, javax.swing.GroupLayout.DEFAULT_SIZE, 322, Short.MAX_VALUE)
                     .addComponent(txt_GT_Short, javax.swing.GroupLayout.DEFAULT_SIZE, 322, Short.MAX_VALUE)))
         );
         pnl_GroupNamesLayout.setVerticalGroup(
             pnl_GroupNamesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnl_GroupNamesLayout.createSequentialGroup()
                 .addComponent(lbl_GroupNames)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(pnl_GroupNamesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(txt_GT_Short, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(lbl_GT_Short))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(pnl_GroupNamesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(txt_GT_Long, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(lbl_GT_Long)))
         );
 
         pnl_EpTitles.setOpaque(false);
 
         lbl_EpTitles.setText("Episode Titles:");
 
         lbl_ET_English.setText("English");
 
         txt_ET_English.setText("Bamboo Leaf Rhapsody");
         txt_ET_English.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyReleased(java.awt.event.KeyEvent evt) {
                 TxtChange(evt);
             }
         });
 
         lbl_ET_Romaji.setText("Romaji");
 
         txt_ET_Romaji.setText("Sasa no Ha Rhapsody");
         txt_ET_Romaji.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyReleased(java.awt.event.KeyEvent evt) {
                 TxtChange(evt);
             }
         });
 
         lbl_ET_Kanji.setText("Kanji");
 
         txt_ET_Kanji.setText("笹の葉ラプソディ");
         txt_ET_Kanji.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyReleased(java.awt.event.KeyEvent evt) {
                 TxtChange(evt);
             }
         });
 
         javax.swing.GroupLayout pnl_EpTitlesLayout = new javax.swing.GroupLayout(pnl_EpTitles);
         pnl_EpTitles.setLayout(pnl_EpTitlesLayout);
         pnl_EpTitlesLayout.setHorizontalGroup(
             pnl_EpTitlesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnl_EpTitlesLayout.createSequentialGroup()
                 .addComponent(lbl_EpTitles)
                 .addContainerGap(337, Short.MAX_VALUE))
             .addGroup(pnl_EpTitlesLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(pnl_EpTitlesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(lbl_ET_English)
                     .addComponent(lbl_ET_Romaji)
                     .addComponent(lbl_ET_Kanji))
                 .addGap(21, 21, 21)
                 .addGroup(pnl_EpTitlesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(txt_ET_Kanji, javax.swing.GroupLayout.DEFAULT_SIZE, 342, Short.MAX_VALUE)
                     .addComponent(txt_ET_Romaji, javax.swing.GroupLayout.DEFAULT_SIZE, 342, Short.MAX_VALUE)
                     .addComponent(txt_ET_English, javax.swing.GroupLayout.DEFAULT_SIZE, 342, Short.MAX_VALUE)))
         );
         pnl_EpTitlesLayout.setVerticalGroup(
             pnl_EpTitlesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnl_EpTitlesLayout.createSequentialGroup()
                 .addComponent(lbl_EpTitles)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(pnl_EpTitlesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(txt_ET_English, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(lbl_ET_English))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(pnl_EpTitlesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(txt_ET_Romaji, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(lbl_ET_Romaji))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(pnl_EpTitlesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(txt_ET_Kanji, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(lbl_ET_Kanji)))
         );
 
         txt_CodeBox.setColumns(20);
         txt_CodeBox.setFont(new java.awt.Font("Consolas", 0, 10));
         txt_CodeBox.setRows(5);
         txt_CodeBox.setText("AT:=[%ATr%,%ATe%,%ATk%]\nET:=[%ETe%,%ETr%,%ETk%]\nGT:=[%GTs%,%GTl%]\nEpNoPad:=$pad(%EpNo%,$max($len(%EpHiNo%),$len(%EpCount%)),\"0\")\nSrcStyled:=\"[\"%Source%\"]\"\nisMovieType:={%Type%=\"Movie\"?\"True\":\"False\"}\nisDepr:={%Depr%?\"[Depr]\":\"\"}\nisCen:={%Cen%?\"[Cen]\":\"\"}\n\nFileName:=%AT%\" \"%EpNoPad%\" - \"%ET%\" \"%GT% %isDepr% %isCen% %SrcStyled%\nPathName:=\"E:\\Anime\\!Processed\\\"%AT%");
         txt_CodeBox.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyReleased(java.awt.event.KeyEvent evt) {
                 TxtChange(evt);
             }
         });
         scrl_txt_CodeBox.setViewportView(txt_CodeBox);
 
         lbl_FileName.setText("Filename:");
 
         lbl_DirName.setText("Directoryname:");
 
         lbl_DirNameStr.setText(" ");
 
         lbl_FileNameStr.setText(" ");
 
         pnl_Misc.setOpaque(false);
 
         lbl_Misc.setText("Misc Options:");
 
         chck_IsCensored.setText("Is Censored");
         chck_IsCensored.setOpaque(false);
         chck_IsCensored.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 CheckedChange(evt);
             }
         });
 
         chck_IsDeprecated.setText("Is Deprecated");
         chck_IsDeprecated.setOpaque(false);
         chck_IsDeprecated.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 CheckedChange(evt);
             }
         });
 
         lbl_EpNo.setText("Episode Number:");
 
         updown_EpNo.addChangeListener(new javax.swing.event.ChangeListener() {
             public void stateChanged(javax.swing.event.ChangeEvent evt) {
                 SpinnerChange(evt);
             }
         });
 
         lbl_EpHiNo.setText("Highest Ep Number:");
 
         updown_EpHiNo.addChangeListener(new javax.swing.event.ChangeListener() {
             public void stateChanged(javax.swing.event.ChangeEvent evt) {
                 SpinnerChange(evt);
             }
         });
 
         lbl_EpCount.setText("Episode Count:");
 
         updown_EpCount.addChangeListener(new javax.swing.event.ChangeListener() {
             public void stateChanged(javax.swing.event.ChangeEvent evt) {
                 SpinnerChange(evt);
             }
         });
 
         lbl_Type.setText("Type:");
 
         cmb_Type.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "TV Series", "Movie", "OVA" }));
         cmb_Type.addItemListener(new java.awt.event.ItemListener() {
             public void itemStateChanged(java.awt.event.ItemEvent evt) {
                 DropDownChange(evt);
             }
         });
 
         lbl_Source.setText("Source:");
 
         cmb_Source.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Web", "DTV", "HDTV", "DVD", "B-R" }));
         cmb_Source.setSelectedIndex(2);
         cmb_Source.addItemListener(new java.awt.event.ItemListener() {
             public void itemStateChanged(java.awt.event.ItemEvent evt) {
                 DropDownChange(evt);
             }
         });
 
         javax.swing.GroupLayout pnl_MiscLayout = new javax.swing.GroupLayout(pnl_Misc);
         pnl_Misc.setLayout(pnl_MiscLayout);
         pnl_MiscLayout.setHorizontalGroup(
             pnl_MiscLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnl_MiscLayout.createSequentialGroup()
                 .addGroup(pnl_MiscLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(lbl_Misc)
                     .addGroup(pnl_MiscLayout.createSequentialGroup()
                         .addContainerGap()
                         .addGroup(pnl_MiscLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(chck_IsCensored)
                             .addComponent(chck_IsDeprecated))
                         .addGroup(pnl_MiscLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                             .addGroup(pnl_MiscLayout.createSequentialGroup()
                                 .addGap(8, 8, 8)
                                 .addComponent(lbl_Type)
                                 .addGap(18, 18, 18)
                                 .addComponent(cmb_Type, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE))
                             .addGroup(pnl_MiscLayout.createSequentialGroup()
                                 .addGap(6, 6, 6)
                                 .addComponent(lbl_Source)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                 .addComponent(cmb_Source, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addGroup(pnl_MiscLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnl_MiscLayout.createSequentialGroup()
                                 .addGroup(pnl_MiscLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                     .addComponent(lbl_EpHiNo)
                                     .addComponent(lbl_EpNo))
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                             .addGroup(pnl_MiscLayout.createSequentialGroup()
                                 .addComponent(lbl_EpCount)
                                 .addGap(26, 26, 26)))
                         .addGroup(pnl_MiscLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                             .addComponent(updown_EpCount, javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(updown_EpHiNo)
                             .addComponent(updown_EpNo, javax.swing.GroupLayout.DEFAULT_SIZE, 53, Short.MAX_VALUE))))
                 .addGap(10, 10, 10))
         );
         pnl_MiscLayout.setVerticalGroup(
             pnl_MiscLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnl_MiscLayout.createSequentialGroup()
                 .addGroup(pnl_MiscLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(pnl_MiscLayout.createSequentialGroup()
                         .addComponent(lbl_Misc)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addGroup(pnl_MiscLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(chck_IsCensored)
                             .addComponent(cmb_Type, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(lbl_Type))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(pnl_MiscLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(chck_IsDeprecated)
                             .addComponent(cmb_Source, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(lbl_Source)))
                     .addGroup(pnl_MiscLayout.createSequentialGroup()
                         .addContainerGap()
                         .addGroup(pnl_MiscLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(updown_EpNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(lbl_EpNo))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(pnl_MiscLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(updown_EpHiNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(lbl_EpHiNo))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(pnl_MiscLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(updown_EpCount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(lbl_EpCount))))
                 .addContainerGap(23, Short.MAX_VALUE))
         );
 
         javax.swing.GroupLayout tp_TagSystemLayout = new javax.swing.GroupLayout(tp_TagSystem);
         tp_TagSystem.setLayout(tp_TagSystemLayout);
         tp_TagSystemLayout.setHorizontalGroup(
             tp_TagSystemLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, tp_TagSystemLayout.createSequentialGroup()
                 .addGroup(tp_TagSystemLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(pnl_GroupNames, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(pnl_AnimeTitles, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(tp_TagSystemLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                     .addComponent(pnl_EpTitles, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(pnl_Misc, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
             .addGroup(tp_TagSystemLayout.createSequentialGroup()
                 .addGap(0, 0, 0)
                 .addGroup(tp_TagSystemLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(lbl_DirName)
                     .addComponent(lbl_FileName))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(tp_TagSystemLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(lbl_DirNameStr, javax.swing.GroupLayout.DEFAULT_SIZE, 713, Short.MAX_VALUE)
                     .addComponent(lbl_FileNameStr, javax.swing.GroupLayout.DEFAULT_SIZE, 713, Short.MAX_VALUE))
                 .addGap(2, 2, 2))
             .addComponent(scrl_txt_CodeBox, javax.swing.GroupLayout.DEFAULT_SIZE, 795, Short.MAX_VALUE)
         );
         tp_TagSystemLayout.setVerticalGroup(
             tp_TagSystemLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(tp_TagSystemLayout.createSequentialGroup()
                 .addGroup(tp_TagSystemLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(tp_TagSystemLayout.createSequentialGroup()
                         .addComponent(pnl_AnimeTitles, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(pnl_GroupNames, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addGroup(tp_TagSystemLayout.createSequentialGroup()
                         .addComponent(pnl_EpTitles, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(pnl_Misc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(tp_TagSystemLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(lbl_FileName)
                     .addComponent(lbl_FileNameStr))
                 .addGap(2, 2, 2)
                 .addGroup(tp_TagSystemLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(lbl_DirName)
                     .addComponent(lbl_DirNameStr))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(scrl_txt_CodeBox, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE))
         );
 
         tbctrl_Main.addTab("TagSystem", tp_TagSystem);
 
         tp_Help.setOpaque(false);
 
         lbl_Icons.setFont(new java.awt.Font("Tahoma", 1, 14));
         lbl_Icons.setText("Icons:");
 
         javax.swing.GroupLayout tp_HelpLayout = new javax.swing.GroupLayout(tp_Help);
         tp_Help.setLayout(tp_HelpLayout);
         tp_HelpLayout.setHorizontalGroup(
             tp_HelpLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(tp_HelpLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(tp_HelpLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(lbl_Icons)
                     .addComponent(ctrl_IconHelp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap(510, Short.MAX_VALUE))
         );
         tp_HelpLayout.setVerticalGroup(
             tp_HelpLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(tp_HelpLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(lbl_Icons)
                 .addGap(2, 2, 2)
                 .addComponent(ctrl_IconHelp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(346, Short.MAX_VALUE))
         );
 
         tbctrl_Main.addTab("Help", tp_Help);
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
         this.setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(lbl_ImportantEvent, javax.swing.GroupLayout.DEFAULT_SIZE, 800, Short.MAX_VALUE)
             .addComponent(tbctrl_Main, javax.swing.GroupLayout.DEFAULT_SIZE, 800, Short.MAX_VALUE)
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addComponent(lbl_ImportantEvent)
                 .addGap(0, 0, 0)
                 .addComponent(tbctrl_Main, javax.swing.GroupLayout.DEFAULT_SIZE, 586, Short.MAX_VALUE))
         );
     }// </editor-fold>//GEN-END:initComponents
 
     private void chck_ShowFileInfoPaneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chck_ShowFileInfoPaneActionPerformed
         ToggleFileInfoPane();
 }//GEN-LAST:event_chck_ShowFileInfoPaneActionPerformed
     private void chck_ShowEditboxesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chck_ShowEditboxesActionPerformed
         ToggleEditBoxes();
 }//GEN-LAST:event_chck_ShowEditboxesActionPerformed
     private void tbctrl_MainAncestorAdded(javax.swing.event.AncestorEvent evt) {//GEN-FIRST:event_tbctrl_MainAncestorAdded
         UIInit();
     }//GEN-LAST:event_tbctrl_MainAncestorAdded
     private void tbctrl_MainStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_tbctrl_MainStateChanged
         if(tbctrl_Main.getTabCount()==5 && tbctrl_Main.getSelectedComponent() != tagSystem){
             tbctrl_Main.remove(tagSystem);
         }
     }//GEN-LAST:event_tbctrl_MainStateChanged
     private void btn_EditTagsystemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_EditTagsystemActionPerformed
         tbctrl_Main.addTab("TagSystem", tagSystem);
         tbctrl_Main.setSelectedComponent(tagSystem);
 }//GEN-LAST:event_btn_EditTagsystemActionPerformed
     private void chck_EnableFileMovingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chck_EnableFileMovingActionPerformed
         ToggleFileMoving();
 }//GEN-LAST:event_chck_EnableFileMovingActionPerformed
     private void chck_AddFolderOnStartupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chck_AddFolderOnStartupActionPerformed
         ToggleFolderAutoAdd();
 }//GEN-LAST:event_chck_AddFolderOnStartupActionPerformed
     private void btn_AddFilesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_AddFilesActionPerformed
         AddPaths(true);
 }//GEN-LAST:event_btn_AddFilesActionPerformed
     private void chck_AddToMLActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chck_AddToMLActionPerformed
         SaveToMem("AddToMyList", chck_AddToML.isSelected());
         ToggleMLCmd(chck_AddToML.isSelected());
         tbl_Files.updateUI();
 }//GEN-LAST:event_chck_AddToMLActionPerformed
     private void chck_RenameMoveFilesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chck_RenameMoveFilesActionPerformed
         SaveToMem("RenameFiles", chck_RenameMoveFiles.isSelected());
         ToggleFileRename(chck_RenameMoveFiles.isSelected());
         tbl_Files.updateUI();
 }//GEN-LAST:event_chck_RenameMoveFilesActionPerformed
     private void TxtChange(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TxtChange
         TagSystemCodeChange();
         SaveToMem("TagSystemCode", txt_CodeBox.getText());
     }//GEN-LAST:event_TxtChange
     private void CheckedChange(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CheckedChange
         TagSystemCodeChange();
     }//GEN-LAST:event_CheckedChange
     private void DropDownChange(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_DropDownChange
         TagSystemCodeChange();
     }//GEN-LAST:event_DropDownChange
     private void SpinnerChange(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_SpinnerChange
         TagSystemCodeChange();
     }//GEN-LAST:event_SpinnerChange
     private void btn_AddFoldersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_AddFoldersActionPerformed
         AddPaths(false);
 }//GEN-LAST:event_btn_AddFoldersActionPerformed
     private void btn_StartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_StartActionPerformed
         ToggleProcessing(btn_Start.getText());
         if(btn_Start.getText().equals("Start")){
             btn_Start.setText("Pause");
         } else if(btn_Start.getText().equals("Pause")) {
             btn_Start.setText("Resume");
         } else if(btn_Start.getText().equals("Resume")) {
             btn_Start.setText("Pause");
         }
 }//GEN-LAST:event_btn_StartActionPerformed
     private void chck_MarkWatchedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chck_MarkWatchedActionPerformed
         SaveToMem("SetWatched", chck_MarkWatched.isSelected());
         ToggleWatched(chck_MarkWatched.isSelected());
 }//GEN-LAST:event_chck_MarkWatchedActionPerformed
     private void cmb_StorageItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cmb_StorageItemStateChanged
         SaveToMem("SetStorageType", cmb_Storage.getSelectedIndex());
         ToggleStorageType(cmb_Storage.getSelectedIndex());
 }//GEN-LAST:event_cmb_StorageItemStateChanged
     private void txt_FoldersToAddKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txt_FoldersToAddKeyReleased
         SaveToMem("FolderToAddOnLoad", txt_FoldersToAdd.getText());
 }//GEN-LAST:event_txt_FoldersToAddKeyReleased
     private void ptn_MoveToFolderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ptn_MoveToFolderActionPerformed
         SaveToMem("MoveTypeUseFolder", ptn_MoveToFolder.isSelected());
 }//GEN-LAST:event_ptn_MoveToFolderActionPerformed
     private void txt_MoveToFolderKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txt_MoveToFolderKeyReleased
         SaveToMem("MoveToFolder", txt_MoveToFolder.getText());
 }//GEN-LAST:event_txt_MoveToFolderKeyReleased
     private void chck_AppendAnimeTitleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chck_AppendAnimeTitleActionPerformed
         SaveToMem("AppendAnimeTitle", chck_AppendAnimeTitle.isSelected());
 }//GEN-LAST:event_chck_AppendAnimeTitleActionPerformed
     private void cmb_AnimeTitleTypeItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cmb_AnimeTitleTypeItemStateChanged
         SaveToMem("AppendAnimeTitleType", cmb_AnimeTitleType.getSelectedIndex());
 }//GEN-LAST:event_cmb_AnimeTitleTypeItemStateChanged
     private void ptn_UseAniDBFNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ptn_UseAniDBFNActionPerformed
         SaveToMem("RenameTypeAniDBFileName", ptn_UseAniDBFN.isSelected());
 }//GEN-LAST:event_ptn_UseAniDBFNActionPerformed
     private void ptn_UseTaggingSystemFolderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ptn_UseTaggingSystemFolderActionPerformed
         SaveToMem("MoveTypeUseFolder", ptn_MoveToFolder.isSelected());
 }//GEN-LAST:event_ptn_UseTaggingSystemFolderActionPerformed
     private void ptn_UseTaggingSystemFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ptn_UseTaggingSystemFileActionPerformed
         SaveToMem("RenameTypeAniDBFileName", ptn_UseAniDBFN.isSelected());
 }//GEN-LAST:event_ptn_UseTaggingSystemFileActionPerformed
     private void btn_CopyCmdTreeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_CopyCmdTreeActionPerformed
         CopyTree(trvw_Cmd);
     }//GEN-LAST:event_btn_CopyCmdTreeActionPerformed
     private void btn_CopyLogTreeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_CopyLogTreeActionPerformed
         CopyTree(trvw_Event);
     }//GEN-LAST:event_btn_CopyLogTreeActionPerformed
 
     private void btn_CopyDebugMsgsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_CopyDebugMsgsActionPerformed
         CopyEvents();
     }//GEN-LAST:event_btn_CopyDebugMsgsActionPerformed
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     protected javax.swing.JButton btn_AddFiles;
     protected javax.swing.JButton btn_AddFolders;
     protected javax.swing.JButton btn_ClearLogs;
     protected javax.swing.JButton btn_CopyCmdTree;
     protected javax.swing.JButton btn_CopyDebugMsgs;
     protected javax.swing.JButton btn_CopyLogTree;
     protected javax.swing.JButton btn_EditTagsystem;
     protected javax.swing.JButton btn_Start;
     protected javax.swing.JCheckBox chck_AddFolderOnStartup;
     protected javax.swing.JCheckBox chck_AddToML;
     protected javax.swing.JCheckBox chck_AppendAnimeTitle;
     protected javax.swing.JCheckBox chck_EnableFileMoving;
     protected javax.swing.JCheckBox chck_IsCensored;
     protected javax.swing.JCheckBox chck_IsDeprecated;
     protected javax.swing.JCheckBox chck_MarkWatched;
     protected javax.swing.JCheckBox chck_RenameMoveFiles;
     protected javax.swing.JCheckBox chck_ShowEditboxes;
     protected javax.swing.JCheckBox chck_ShowFileInfoPane;
     protected javax.swing.JComboBox cmb_AnimeTitleType;
     protected javax.swing.JComboBox cmb_Source;
     protected javax.swing.JComboBox cmb_Storage;
     protected javax.swing.JComboBox cmb_Type;
     protected gui.IconHelp ctrl_IconHelp;
     protected javax.swing.JLabel lbl_AT_English;
     protected javax.swing.JLabel lbl_AT_Kanji;
     protected javax.swing.JLabel lbl_AT_Other;
     protected javax.swing.JLabel lbl_AT_Romaji;
     protected javax.swing.JLabel lbl_AT_Synomymn;
     protected javax.swing.JLabel lbl_AnimeTitles;
     protected javax.swing.JLabel lbl_DirName;
     protected javax.swing.JLabel lbl_DirNameStr;
     protected javax.swing.JLabel lbl_ET_English;
     protected javax.swing.JLabel lbl_ET_Kanji;
     protected javax.swing.JLabel lbl_ET_Romaji;
     protected javax.swing.JLabel lbl_EpCount;
     protected javax.swing.JLabel lbl_EpHiNo;
     protected javax.swing.JLabel lbl_EpNo;
     protected javax.swing.JLabel lbl_EpTitles;
     protected javax.swing.JLabel lbl_FileName;
     protected javax.swing.JLabel lbl_FileNameStr;
     protected javax.swing.JLabel lbl_GT_Long;
     protected javax.swing.JLabel lbl_GT_Short;
     protected javax.swing.JLabel lbl_GroupNames;
     protected javax.swing.JLabel lbl_Icons;
     protected javax.swing.JLabel lbl_ImportantEvent;
     protected javax.swing.JLabel lbl_MBProcessed;
     protected javax.swing.JLabel lbl_Misc;
     protected javax.swing.JLabel lbl_Renaming;
     protected javax.swing.JLabel lbl_Source;
     protected javax.swing.JLabel lbl_TimeElapsed;
     protected javax.swing.JLabel lbl_TimeRemaining;
     protected javax.swing.JLabel lbl_Type;
     protected javax.swing.JPanel pnl_AddFolderOnStartup;
     protected javax.swing.JPanel pnl_AnimeTitles;
     protected javax.swing.JPanel pnl_EditBoxes;
     protected javax.swing.JPanel pnl_EpTitles;
     protected javax.swing.JPanel pnl_FileAdd_Cont;
     protected javax.swing.JPanel pnl_FileAdd_Ctrls;
     protected javax.swing.JPanel pnl_FileAdd_FileInfo;
     protected javax.swing.JPanel pnl_FileAdd_Lstvw;
     protected javax.swing.JPanel pnl_FileAdd_Status;
     protected javax.swing.JPanel pnl_FileMove;
     protected javax.swing.JPanel pnl_FileRename;
     protected javax.swing.JPanel pnl_FileRenamingMoving;
     protected javax.swing.JPanel pnl_GroupNames;
     protected javax.swing.JPanel pnl_Logs_Ctrls;
     protected javax.swing.JPanel pnl_Misc;
     protected javax.swing.JProgressBar prg_File;
     protected javax.swing.JProgressBar prg_Total;
     protected javax.swing.JRadioButton ptn_MoveToFolder;
     protected javax.swing.JRadioButton ptn_UseAniDBFN;
     protected javax.swing.JRadioButton ptn_UseTaggingSystemFile;
     protected javax.swing.JRadioButton ptn_UseTaggingSystemFolder;
     private javax.swing.ButtonGroup ptngrp_Moving;
     private javax.swing.ButtonGroup ptngrp_Renaming;
     protected javax.swing.JScrollPane scr_tbl_Files;
     protected javax.swing.JScrollPane scrl_txt_CodeBox;
     protected javax.swing.JScrollPane scrl_txt_FoldersToAdd;
     protected javax.swing.JScrollPane scrl_txt_Other;
     protected javax.swing.JScrollPane scrl_txt_Source;
     protected javax.swing.JScrollPane scrl_txt_Storage;
     protected javax.swing.JScrollPane scrpn_trvw_Cmd;
     protected javax.swing.JScrollPane scrpn_trvw_Event;
     protected javax.swing.JSplitPane spnl_FileAdd;
     protected javax.swing.JSplitPane spnl_Logs;
     protected javax.swing.JTabbedPane tbctrl_Main;
     protected gui.components.JExtTable tbl_Files;
     protected javax.swing.JPanel tp_FileAdd;
     protected javax.swing.JPanel tp_Help;
     protected javax.swing.JPanel tp_Logs;
     protected javax.swing.JPanel tp_Options;
     protected javax.swing.JPanel tp_TagSystem;
     protected javax.swing.JTree trvw_Cmd;
     protected javax.swing.JTree trvw_Event;
     protected javax.swing.JTextField txt_AT_English;
     protected javax.swing.JTextField txt_AT_Kanji;
     protected javax.swing.JTextField txt_AT_Other;
     protected javax.swing.JTextField txt_AT_Romaji;
     protected javax.swing.JTextField txt_AT_Synomymn;
     protected javax.swing.JTextArea txt_CodeBox;
     protected javax.swing.JTextField txt_ET_English;
     protected javax.swing.JTextField txt_ET_Kanji;
     protected javax.swing.JTextField txt_ET_Romaji;
     protected javax.swing.JTextArea txt_FoldersToAdd;
     protected javax.swing.JTextField txt_GT_Long;
     protected javax.swing.JTextField txt_GT_Short;
     protected javax.swing.JTextField txt_MoveToFolder;
     protected gui.components.JHintTextArea txt_Other;
     protected gui.components.JHintTextArea txt_Source;
     protected gui.components.JHintTextArea txt_Storage;
     protected javax.swing.JSpinner updown_EpCount;
     protected javax.swing.JSpinner updown_EpHiNo;
     protected javax.swing.JSpinner updown_EpNo;
     // End of variables declaration//GEN-END:variables
 
 }
