 package com.freeroom.projectci.beans;
 
 public enum ReportType {
    UserStory(432), FunctionalTesting(100), Document(64),
    PerformanceTesting(40), IntegrationTesting(20),
     OverTime(999), BugFixing(100), Leave(60), Others(999);
 
     private long estimatedEffort;
 
     ReportType(long estimatedDays) {
         this.estimatedEffort = estimatedDays * 8;
     }
 
     public long getEstimatedEffort() {
         return estimatedEffort;
     }
 }
