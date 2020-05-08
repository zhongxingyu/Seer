 package com.teamdev.projects.test.data;
 
 /**
  * @author Alexander Orlov
  */
 public class TaskData {
 
     private String taskName;
     private String notes;
     private String tags;
     private String assignee;
     private int estimate;
     private String dueTo;
     private String schedule;
 
     public TaskData(String taskName) {
         this.taskName = taskName;
     }
 
     public TaskData(String taskName, String schedule) {
         this.taskName = taskName;
         this.schedule = schedule;
     }
 
     public TaskData(String taskName, int estimate) {
         this.taskName = taskName;
         this.estimate = estimate;
     }
 
    public TaskData(String taskName, String assignee, String notes) {
         this.taskName = taskName;
        this.assignee = assignee;
         this.notes = notes;
     }
 
     public TaskData(String taskName, String notes, String tags, String assignee, int estimate, String dueTo) {
         this.taskName = taskName;
         this.notes = notes;
         this.tags = tags;
         this.assignee = assignee;
         this.estimate = estimate;
         this.dueTo = dueTo;
     }
 
     public TaskData(String taskName, String notes, String tags, String assignee, int estimate, String dueTo, String schedule) {
         this.taskName = taskName;
         this.notes = notes;
         this.tags = tags;
         this.assignee = assignee;
         this.estimate = estimate;
         this.dueTo = dueTo;
         this.schedule = schedule;
     }
 
     public String getTaskName() {
         return taskName;
     }
 
     public String getNotes() {
         return notes;
     }
 
     public String getTags() {
         return tags;
     }
 
     public String getAssignee() {
         return assignee;
     }
 
     public int getEstimate() {
         return estimate;
     }
 
     public String getDueBy() {
         return dueTo;
     }
 
     public String getSchedule() {
         return schedule;
     }
 
     @Override
     public String toString() {
         return "TaskData{" +
                 "taskName='" + taskName + '\'' +
                 ", notes='" + notes + '\'' +
                 ", tags='" + tags + '\'' +
                 ", assignee='" + assignee + '\'' +
                 ", estimate='" + estimate + '\'' +
                 ", dueTo='" + dueTo + '\'' +
                 ", schedule='" + schedule + '\'' +
                 '}';
     }
 }
