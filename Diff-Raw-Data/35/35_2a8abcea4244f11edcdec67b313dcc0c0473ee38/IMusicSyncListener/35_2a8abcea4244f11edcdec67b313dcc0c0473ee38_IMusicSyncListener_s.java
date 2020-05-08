 package com.we.android.music;
 
 import java.util.List;
 
 public interface IMusicSyncListener {
     public void onFilesMissing(List<String> missingFiles);
     public void onProgressUpdate(int progress);
     public void onSyncFinished();
 }
