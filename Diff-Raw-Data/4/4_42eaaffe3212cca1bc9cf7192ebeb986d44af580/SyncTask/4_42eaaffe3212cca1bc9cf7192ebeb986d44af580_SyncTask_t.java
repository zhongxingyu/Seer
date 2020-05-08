 package net.jiangyouxin.vnotepad;
 
 import android.content.*;
 
 public class SyncTask {
     // trick: use id in strings.xml for result
     public static int SYNC_SUCCESS_NO_CHANGE = R.string.sync_success_no_change;
 
     public static int SYNC_SUCCESS_DOWNLOAD = R.string.sync_success_download;
     public static int SYNC_SUCCESS_UPLOAD = R.string.sync_success_upload;
     public static int SYNC_SUCCESS_MERGE = R.string.sync_success_merge;
 
     public static int SYNC_FAILED_NETWORK = R.string.sync_failed_network;
     public static int SYNC_FAILED_CONFLICT = R.string.sync_failed_conflict;
 
     private String baseFile;
     private String localFile;
     private String serverFile;
     private SyncClient client;
     private Context context;
 
     public SyncTask(String baseFile, String localFile, String serverFile, Context context) {
         this.baseFile = baseFile;
         this.localFile = localFile;
         this.serverFile = serverFile;
         this.client = new SimpleSyncClient();
         this.context = context;
     }
 
     public int doSync() {
         if (!client.download(context, serverFile))
             return SYNC_FAILED_NETWORK;
         if (isDifferent(serverFile, baseFile)) {
             if (isDifferent(localFile, baseFile)) {
                 return doMerge();
             } else {
                 return doDownload();
             }
         } else if(isDifferent(localFile, baseFile)) {
             return doUpload(SYNC_SUCCESS_UPLOAD);
         } else {
             return SYNC_SUCCESS_NO_CHANGE;
         }
     }
     private int doDownload() {
         copyFile(serverFile, localFile);
         copyFile(serverFile, baseFile);
         return SYNC_SUCCESS_DOWNLOAD;
     }
     private int doUpload(int retIfSuccess) {
         if (!client.upload(context, localFile))
             return SYNC_FAILED_NETWORK;
         copyFile(localFile, baseFile);
         return retIfSuccess;
     }
     private int doMerge() {
         byte []base = FileUtility.readFile(context, baseFile);
         byte []local = FileUtility.readFile(context, localFile);
         byte []server = FileUtility.readFile(context, serverFile);
         FileUtility.writeFile(context, baseFile, server);
 
         byte []result = xdl_merge(
                 base,
                 local,
                 server,
                 0,  // flags
                 0,  // marker_size
                 1,  // level
                 0,  // favor
                 1,  // style
                 "orig",
                 "local",
                 "server");
         
         FileUtility.writeFile(context, localFile, result);
 
         return SYNC_FAILED_CONFLICT;
     }
     private boolean isDifferent(String file1, String file2) {
         byte []buffer1 = FileUtility.readFile(context, file1);
         byte []buffer2 = FileUtility.readFile(context, file2);
         if (buffer1.length != buffer2.length)
             return true;
         for (int i = 0; i < buffer1.length; i++)
             if (buffer1[i] != buffer2[i])
                 return true;
         return false;
     }
     private void copyFile(String file1, String file2) {
         byte []buffer = FileUtility.readFile(context, file1);
         FileUtility.writeFile(context, file2, buffer);
     }
     private native byte[] xdl_merge(
             byte[] orig,
             byte[] mf1,
             byte[] mf2,
             int flags,
             int marker_size,
             int level,
             int favor,
             int style,
             String ancestor,
             String file1,
             String file2);

     static {
        System.loadLibrary("xdiff");
     }
 }
