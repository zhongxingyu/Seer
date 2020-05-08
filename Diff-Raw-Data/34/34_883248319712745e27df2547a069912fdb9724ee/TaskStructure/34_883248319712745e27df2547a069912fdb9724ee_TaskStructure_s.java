 package com.calendar.reporter.database.task;
 
 import java.sql.Date;
 import java.text.SimpleDateFormat;
 
 public class TaskStructure {
     public static final String DATE_FORMAT = "yyyy-MM-dd";
     public static final String TABLE_NAME = "tasks";
     public static final String COLUMN_ID = "_id";
     public static final String COLUMN_NAME = "name";
     public static final String COLUMN_DESCRIPTION = "description";
     public static final String COLUMN_TIME = "time";
     public static final String COLUMN_DATE = "date";
     public static final String PROJECT_ID = "project_id";
     public static final String ACTIVITY_ID = "activity_id";
 
     private long id;
     private String name;
     private String description;
     private int time;
     private Date date;
     private long projectID;
     private long activityID;
 
 
     public long getId() {
         return id;
     }
 
     public void setId(long id) {
         this.id = id;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String getDescription() {
         return description;
     }
 
     public void setDescription(String description) {
         this.description = description;
     }
 
     public int getTime() {
         return time;
     }
 
     public void setTime(int time) {
         this.time = time;
     }
 
     public Date getDate() {
         return date;
     }
 
     public void setDate(Date date) {
         this.date = date;
     }
 
     public long getProjectID() {
         return projectID;
     }
 
     public void setProjectID(long projectID) {
         this.projectID = projectID;
     }
 
     public long getActivityID() {
         return activityID;
     }
 
     public void setActivityID(long activityID) {
         this.activityID = activityID;
     }
    @Override
    public String toString(){
        SimpleDateFormat formatter = new SimpleDateFormat(TaskStructure.DATE_FORMAT);
        String dateNow = formatter.format(date.getTime());
 
        return name + "/" + dateNow;
     }
 }
