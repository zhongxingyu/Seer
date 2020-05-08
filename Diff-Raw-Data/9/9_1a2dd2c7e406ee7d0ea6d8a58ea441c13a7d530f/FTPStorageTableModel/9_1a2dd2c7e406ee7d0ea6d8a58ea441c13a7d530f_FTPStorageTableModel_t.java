 package com.scurab.java.ftpleechergui.model;
 
 import com.scurab.java.ftpleechergui.Application;
 import com.scurab.java.ftpleechergui.TextUtils;
 import org.apache.commons.net.ftp.FTPClient;
 import org.apache.commons.net.ftp.FTPFile;
 
 import javax.imageio.ImageIO;
 import javax.swing.*;
 import javax.swing.table.AbstractTableModel;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 
 public class FTPStorageTableModel extends AbstractTableModel {
 
     public interface OnFtpStorageEventListener {
         public void onDataEvent(DataEvent type);
 
         public void onError(Throwable t);
     }
 
     private FTPClient mClient;
 
     private FTPFile[] mData;
 
     private OnFtpStorageEventListener mListener;
 
     private String mCurrentPath;
 
     private Thread mThread;
 
     private boolean mIsRunning = true;
 
     private FTPFile mToLoad = null;
 
     private ImageIcon mFileIcon = new ImageIcon();
 
     private ImageIcon mFolderIcon = new ImageIcon();
 
     public FTPStorageTableModel() {
         this(null);
     }
 
     public FTPStorageTableModel(FTPClient client) {
         mClient = client;
         mThread = new Thread(new Runnable() {
             @Override
             public void run() {
                 threadWorkImpl();
             }
         });
         mThread.setName("FTPStorageModel");
         mThread.start();
         //init in thread
     }
 
     private void init() {
         FTPFile[] newData = null;
         if (mClient != null) {
             newData = getDirs(mToLoad);
         } else {
             newData = new FTPFile[0];
         }
         if (newData != null) {
             mData = newData;
             fireTableDataChanged();
         }
     }
 
 
     private void threadWorkImpl() {
         try {
             mFileIcon.setImage(ImageIO.read(getClass().getResourceAsStream("/assets/file.png")));
             mFolderIcon.setImage(ImageIO.read(getClass().getResourceAsStream("/assets/folder.png")));
         } catch (IOException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
         while (mIsRunning) {
             synchronized (mThread) {
                 try {
                     init();
                     mThread.wait();
                 } catch (InterruptedException e) {
                     e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                 }
             }
         }
     }
 
     private FTPFile[] getDirs(FTPFile root) {
         ArrayList<FTPFile> data = new ArrayList<FTPFile>();
         try {
             onDataEvent(DataEvent.Started);
 
             if (root == null) {
                 mClient.cdup();
             } else {
                 mClient.cwd(root.getName());
             }
 
             mCurrentPath = mClient.printWorkingDirectory();
             data.addAll(Arrays.asList(mClient.listFiles()));
             Collections.sort(data, mComparator);
             //add null to first position to simulate ".." folder
             data.add(0, null);
             mToLoad = null;
         } catch (IOException e) {
             onError(e);
             data = null;
         }
         onDataEvent(DataEvent.Finished);
         if (data != null) {
             return data.toArray(new FTPFile[data.size()]);
         } else {
             return null;
         }
     }
 
     @Override
     public int getRowCount() {
         return mData == null ? 0 : mData.length;
     }
 
     @Override
     public int getColumnCount() {
         return 3;
     }
 
     @Override
     public String getColumnName(int column) {
         if (column == 0) {
             return "";
         } else if (column == 1) {
             return TextUtils.nz(mCurrentPath, Application.getLabels().getString("Dir"));
         } else if (column == 2) {
             return Application.getLabels().getString("Size");
         } else {
             return super.getColumnName(column);
         }
     }
 
 
     @Override
     public Object getValueAt(int rowIndex, int columnIndex) {
         final FTPFile f = getItem(rowIndex);
         if (columnIndex == 0) {
             if (f != null) {
                 return f.isDirectory() ? mFolderIcon : mFileIcon;
             }
         } else if (columnIndex == 1) {
             if (f == null) {
                 return "..";
             }
             String name = TextUtils.nz(f.getName(), f.toString());
             return name;
         } else if (columnIndex == 2) {
             if (f != null && f.isFile()) {
                return TextUtils.getNumberReadable(f.getSize());
             }
         }
         return null;
     }
 
     public FTPFile getItem(int rowIndex) {
         return mData[rowIndex];
     }
 
     public void setRoot(FTPFile r) {
         mToLoad = r;
         synchronized (mThread) {
             mThread.notifyAll();
         }
     }
 
     public void setFTPClient(FTPClient client) {
         mClient = client;
         synchronized (mThread) {
             mThread.notifyAll();
         }
     }
 
     public boolean isLoading() {
         return mThread != null && mThread.getState() != Thread.State.WAITING;
     }
 
     //region notification
 
     private void onError(IOException e) {
         if (mListener != null) {
             mListener.onError(e);
         }
     }
 
     private void onDataEvent(DataEvent event) {
         if (mListener != null) {
             mListener.onDataEvent(event);
         }
     }
 
     public void setOnFtpStorageEventListener(OnFtpStorageEventListener listener) {
         mListener = listener;
     }
 
     //endregion
 
     @Override
     public Class<?> getColumnClass(int columnIndex) {
         if (columnIndex == 0) {
             return ImageIcon.class;
         } else {
             return FTPFile.class;
         }
     }
 
     /**
      * Return current FTP path
      *
      * @return
      */
     public String getCurrentPath() {
         return mCurrentPath;
     }
 
     //region helpers
 
     /**
      * Sort comparator for sorting alphabetically and folders are before files
      */
     private Comparator<? super FTPFile> mComparator = new Comparator<FTPFile>() {
         @Override
         public int compare(FTPFile o1, FTPFile o2) {
             int a = o1.isDirectory() ? 1 : 0;
             int b = o2.isDirectory() ? 1 : 0;
             if (a == b) {
                 return o1.getName().compareTo(o2.getName());
             } else {
                 return b - a;
             }
         }
     };
     //endregion
 }
