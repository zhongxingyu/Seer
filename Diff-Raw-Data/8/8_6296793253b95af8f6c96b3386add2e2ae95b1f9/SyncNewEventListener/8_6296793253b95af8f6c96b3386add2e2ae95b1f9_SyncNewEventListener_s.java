 package com.kii.cloud.sync;
 
 import android.content.Context;
 import android.net.Uri;
 
 import com.kii.sync.KiiNewEventListener;
 import com.kii.sync.SyncMsg;
 
 public abstract class SyncNewEventListener implements KiiNewEventListener {
     KiiSyncClient client = null;
    long id = 0;
     protected Context mContext;
 
     public SyncNewEventListener(Context context) {
         mContext = context;
         id = System.currentTimeMillis();
     }
 
     public boolean register() {
         client = KiiSyncClient.getInstance(mContext);
         if (client == null) {
             throw new NullPointerException();
         }
         return client.registerNewEventListener(id, this);
     }
 
     public void unregister() {
         if (id != 0) {
             client.unregisterNewEventListener(id);
         }
     }
     @Override
     public void onLocalChangeSyncedEvent(Uri[] arg0) {
         // TODO Auto-generated method stub
 
     }
 
     @Override
     public void onNewSyncDeleteEvent(Uri[] arg0) {
         // TODO Auto-generated method stub
 
     }
 
     @Override
     public void onNewSyncInsertEvent(Uri[] arg0) {
         // TODO Auto-generated method stub
 
     }
 
     @Override
     public void onNewSyncUpdateEvent(Uri[] arg0) {
         // TODO Auto-generated method stub
 
     }
 
     @Override
     public void onQuotaExceeded(Uri arg0) {
         // TODO Auto-generated method stub
 
     }
 
     @Override
     public void onSyncComplete(SyncMsg arg0) {
         // TODO Auto-generated method stub
 
     }
 
     @Override
     public void onSyncStart(String arg0) {
         // TODO Auto-generated method stub
 
     }
 
     public abstract void onDownloadComplete(Uri[] arg0);
 
 }
