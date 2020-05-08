 package com.scurab.java.ftpleechergui.controller;
 
 import com.scurab.java.ftpleecher.*;
 import com.scurab.java.ftpleechergui.Application;
 import com.scurab.java.ftpleechergui.adapter.FTPMasterTableAdapter;
 import com.scurab.java.ftpleechergui.model.DownloadTableModel;
 import com.scurab.java.ftpleechergui.model.Settings;
 import org.apache.commons.net.ftp.FTPFile;
 
 import javax.swing.*;
 import javax.swing.table.DefaultTableCellRenderer;
 import javax.swing.table.TableColumn;
 import java.awt.*;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 public class DownloadController extends TableController {
 
     private JTable mTable;
 
     private FTPLeechMaster mMaster;
 
     private FTPConnection mConfig;
 
     private List<DownloadTask> mTasks = new ArrayList<DownloadTask>();
 
     private FTPFactory mFactory;
 
     private DownloadTableModel mTableModel;
 
     private FTPMasterTableAdapter mAdapter;
 
     public DownloadController(JTable table, FTPLeechMaster master) {
         super(table);
         mTable = table;
         mTable.setFont(new Font(mTable.getFont().getFontName(), Font.PLAIN, 11));
         mMaster = master;
         mAdapter = new FTPMasterTableAdapter(mMaster);
         mTableModel = new DownloadTableModel(mAdapter);
         mTable.setModel(mTableModel);
         setWidths();
     }
 
     private void setWidths() {
         /*
         0 Index
         1 State
         2 FileName
         3 Part
         4 Size
         5 Downloaded
         6 percents
         7 Speed
         8 Error
      */
         DefaultTableCellRenderer center = new DefaultTableCellRenderer();
         center.setHorizontalAlignment(DefaultTableCellRenderer.CENTER);
 
         int[] widths = new int[]{50, 150, -1, 50, 150, 150, 100, 100, -1};
         DefaultTableCellRenderer[] renderers = new DefaultTableCellRenderer[]{
                 center, center, null, center, center, center, center, center, null
         };
 
         for (int i = 0; i < widths.length; i++) {
             TableColumn tcm = mTable.getColumnModel().getColumn(i);
             int w = widths[i];
             if (w > -1) {
                 tcm.setMaxWidth(w);
             }
             if (renderers[i] != null) {
                 tcm.setCellRenderer(renderers[i]);
             }
         }
     }
 
     @Override
     protected void onInitTable(JTable table) {
         table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         table.setShowGrid(true);
     }
 
     public void onDownloadItem(String ftpFolder, FTPFile file, String destFolder) throws IOException, FatalFTPException {
         String url = ftpFolder + "/" + file.getName();
         DownloadTask task = mFactory.createTask(url, destFolder);
         mTasks.add(task);
         mMaster.enqueue(task);
     }
 
     public FTPConnection getConfig() {
         return mConfig;
     }
 
     public void setConfig(FTPConnection config) {
         mConfig = config;
         mFactory = new FTPFactory(new FTPContext(mConfig).setSettings(transformSettings()));
     }
 
     public void onSettingsChanged(){
        if(mConfig != null){
            mFactory = new FTPFactory(new FTPContext(mConfig).setSettings(transformSettings()));
        }
     }
 
     private static FTPSettings transformSettings(){
         FTPSettings fs = new FTPSettings();
         Settings s = Application.getInstance().getSettings();
         fs.bufferSize = s.bufferSize;
         fs.fileType = s.fileType;
         fs.globalPieceLength = s.globalPieceLength;
         fs.resume = s.resume;
         return fs;
     }
 }
