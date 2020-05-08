 package org.inigma.maven;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 public class VersionInformation {
     private String branchName;
     private String version;
     private boolean snapshot;
     private Date timestamp = new Date();
     private String pattern;
 
     public VersionInformation(String pattern) {
         this.pattern = pattern;
     }
 
     public void setTimestamp(Date timestamp) {
         this.timestamp = timestamp;
     }
 
     public void setBranchName(String branchName) {
         this.branchName = branchName;
     }
 
     public void setSnapshot(boolean snapshot) {
         this.snapshot = snapshot;
     }
 
     public boolean isSnapshot() {
         return snapshot;
     }
 
     public void setVersion(String version) {
         this.version = version;
     }
 
     public String getFinalVersion() {
         if (!snapshot) {
             return version;
         }
 
         String snapshotVersion = pattern.replaceAll("\\$\\{scmVersion\\.number\\}", version);
         snapshotVersion = snapshotVersion.replaceAll("\\$\\{scmVersion\\.branch\\}", branchName);
         SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd.hh.mm.ss");
         snapshotVersion = snapshotVersion.replaceAll("\\$\\{scmVersion\\.date\\}", sdf.format(timestamp));
         return snapshotVersion;
     }
 }
