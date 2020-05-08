 package org.gonevertical.appengineutils.backup;
 
 import java.util.Date;
 import java.util.List;
 import java.util.logging.Logger;
 
import org.gonevertical.appengineutils.backkup.BackupKind;
import org.gonevertical.appengineutils.backkup.BackupKinds;
 import org.gonevertical.appengineutils.util.KindUtils;
 
 import com.google.appengine.api.taskqueue.DeferredTask;
 import com.google.appengine.api.taskqueue.Queue;
 import com.google.appengine.api.taskqueue.QueueFactory;
 import com.google.appengine.api.taskqueue.TaskOptions;
 
 public class BackupKinds implements DeferredTask {
   
   private static final Logger log = Logger.getLogger(BackupKinds.class.getName());
 
   private Date runDate;
   private boolean useGoogleStorage;
   private String bucketName;
   private List<String> excludeList;
 
   public BackupKinds() {
   }
   
   public void setUseGoogleStorage(boolean useGoogleStorage, String bucketName) {
     this.useGoogleStorage = useGoogleStorage;
     this.bucketName = bucketName;
   }
   
   @Override
   public void run() {
     runDate = new Date();
     
     log.info("Backup task running. runDate=" + runDate.toGMTString());
     
     loopKinds();
   }
 
   private void loopKinds() {
     List<String> kinds = KindUtils.getKinds(false);
     
     for (String kind : kinds) {
       processKind(kind);
     }
   }
 
   private boolean excludeKind(String kind) {
     boolean exclude = false;
     if ((excludeList != null && excludeList.isEmpty() == false) && excludeList.contains(kind) == true) {
       exclude = true;
     }
     return exclude;
   }
 
   private void processKind(String kind) {
     if (excludeKind(kind) == true) {
       return;
     }
     
     BackupKind task = new  BackupKind(runDate, kind);
     task.useGoogleStorage(useGoogleStorage, bucketName);
     
     TaskOptions taskOptions = TaskOptions.Builder.withPayload(task);
     Queue queue = QueueFactory.getDefaultQueue();
     queue.add(taskOptions);
   }
 
   public void setExcludeKinds(List<String> excludeList) {
     this.excludeList = excludeList;
   }
 }
