 /*
  * Made by Wannes 'W' De Smet
  * (c) 2011 Wannes De Smet
  * All rights reserved.
  * 
  */
 package net.wgr.xenmaster.api;
 
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 import net.wgr.xenmaster.controller.BadAPICallException;
 
 /**
  * 
  * @created Dec 14, 2011
  * @author double-u
  */
 public class Task extends NamedEntity {
 
     protected Date created, finished;
     protected String residentOn;
    protected double progress;
     protected String result;
     protected Status status;
     protected List<String> errorInfo;
     protected List<String> subtasks;
     protected String type;
     protected boolean forwarded;
     @Fill
     protected Map<String, String> otherConfig;
     
     public Task() {
         
     }
 
     public Task(String ref, boolean autoFill) {
         super(ref, autoFill);
     }
 
     public Task(String ref) {
         super(ref);
     }
 
     public Date getCreated() {
         return created;
     }
 
     public List<String> getErrorInfo() {
         return errorInfo;
     }
 
     public Date getFinished() {
         return finished;
     }
 
    public double getProgress() {
         return progress;
     }
 
     public String getResidentOn() {
         return residentOn;
     }
 
     public String getResult() {
         return result;
     }
 
     public static List<Task> getAll() throws BadAPICallException {
         return getAllEntities(Task.class);
     }
     
     public static enum Status {
          /**
          * The value does not belong to this enumeration
          */
         UNRECOGNIZED,
         /**
          * task is in progress
          */
         PENDING,
         /**
          * task was completed successfully
          */
         SUCCESS,
         /**
          * task has failed
          */
         FAILURE,
         /**
          * task is being cancelled
          */
         CANCELLING,
         /**
          * task has been cancelled
          */
         CANCELLED
     }
 }
