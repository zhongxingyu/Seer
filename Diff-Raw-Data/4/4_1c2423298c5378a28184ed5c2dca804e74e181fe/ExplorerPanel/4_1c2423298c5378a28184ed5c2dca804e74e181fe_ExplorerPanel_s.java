 /*
  * $Id$
  * (c) Copyright 2000 wingS development team.
  *
  * This file is part of the wingS demo (http://wings.mercatis.de).
  *
  * The wingS demo is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * Please see COPYING for the complete licence.
  */
 
 package explorer;
 
 import java.io.*;
 import java.awt.event.*;
 
 import javax.swing.tree.*;
 import javax.swing.event.*;
 
 import org.wings.*;
 
 /**
  * TODO: documentation
  *
  * @author Rene Thol
  * @version $Revision$
  */
 public class ExplorerPanel
     extends SPanel
 {
     private STree explorerTree;
 
     private final FileTableModel fTableModel = new FileTableModel();
 
     private final STable fileTable = new STable(fTableModel);
 
     public ExplorerPanel(String dir) {
         try {
             setLayout(new STemplateLayout("/tmp/Explorer.thtml"));
         }
         catch ( java.io.IOException e ) {
             setLayout(new SFlowLayout());
         }
 
         initExplorerTree();
         setExplorerBaseDir(dir);
 
         fileTable.setSelectionMode(fileTable.MULTIPLE_SELECTION);
         add(fileTable, "FileTable");
 
         SButton delete = new SButton("delete");
         delete.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 deleteFiles();
             } });
         add(delete, "Delete");
 
         initUploadFile();
     }
 
     /**
      *
      */
     private void deleteFiles() {
         int selected[] = fileTable.getSelectedRows();
 
         for (int i=0; i<selected.length; i++)
             fTableModel.getFileAt(selected[i]).delete();
 
         fTableModel.reset();
     }
 
     /**
      *
      */
     private void initUploadFile() {
         SForm p = new SForm(new SFlowLayout());
         p.setEncodingType("multipart/form-data");
 
         final SFileChooser chooser = new SFileChooser();
         p.add(chooser);
 
         SButton submit = new SButton("upload");
         submit.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                uploadFile(chooser.getSelectedFile(), chooser.getFilename());
             } });
         p.add(submit);
         add(p, "UploadForm");
     }
 
     /**
      *
      */
     private void uploadFile(File file, String fileName) {
         try {
             FileInputStream fin = new FileInputStream(file);
             FileOutputStream fout =
                 new FileOutputStream(fTableModel.getDirectory().getAbsolutePath() +
                                      File.separator + fileName);
             int val;
 
             while ((val = fin.read()) != -1)
                 fout.write(val);
 
             fin.close();
             fout.close();
 
             fTableModel.reset();
         }
         catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     /**
      *
      */
     private void initExplorerTree() {
         explorerTree = new STree(new DefaultTreeModel(new DefaultMutableTreeNode("")));
         explorerTree.addTreeSelectionListener(new TreeSelectionListener() {
             public void valueChanged(TreeSelectionEvent e) {
                 TreePath tpath = e.getNewLeadSelectionPath();
 
                 if ( tpath!=null ) {
                     DefaultMutableTreeNode selectedNode =
                         (DefaultMutableTreeNode)tpath.getLastPathComponent();
 
                     fTableModel.setDirectory((File)selectedNode.getUserObject());
                 }
             } });
 
         explorerTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
 
         add(explorerTree, "DirTree");
     }
 
     /**
      *
      */
     public void setExplorerBaseDir(String baseDir) {
         File start = new File(baseDir);
 
         if (start.isDirectory()) {
 
             fTableModel.setDirectory(start);
             DefaultMutableTreeNode root = new DefaultMutableTreeNode(start);
             appendDirTree(root);
 
             explorerTree.setModel(new DefaultTreeModel(root));
         }
     }
 
     /**
      * Build the directory tree. For simplicity a static model.
      */
     private void appendDirTree(DefaultMutableTreeNode root) {
         File entries[] = ((File)root.getUserObject()).listFiles();
         if (entries == null)
             return;
 
         for ( int i=0; i<entries.length; i++) {
             DefaultMutableTreeNode nextNode = new DefaultMutableTreeNode(entries[i]);
 
             if (entries[i].isDirectory()) {
                 root.add(nextNode);
                 appendDirTree(nextNode);
             }
         }
 
         return;
     }
 }
 
 /*
  * Local variables:
  * c-basic-offset: 4
  * indent-tabs-mode: nil
  * End:
  */
