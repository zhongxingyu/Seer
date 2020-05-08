 package com.xingcloud.xa.secondaryindex;
 
 /**
  * Created with IntelliJ IDEA.
  * User: hadoop
  * Date: 5/20/13
  * Time: 7:55 AM
  * To change this template use File | Settings | File Templates.
  */
 public class Index {
   private String projectID;
   private String uid;
   private String propertyID;
   private String value;
   private String operation;
   
   public Index(String projectID, String uid, String propertyID , String value, String operation){
     this.projectID = projectID;
     this.uid = uid;
     this.propertyID = propertyID;
     this.value = value;
    this.value = operation;
   }
 
 
   public String getProjectID() {
     return projectID;
   }
 
   public String getUid() {
     return uid;
   }
 
   public String getPropertyID() {
     return propertyID;
   }
 
   public String getValue() {
     return value;
   }
 
   public String getOperation() {
     return value;
   }
   
   @Override
   public int hashCode(){
     return (projectID + "_" + propertyID + "_" + uid).hashCode();  
   }
 
 
   @Override
   public String toString(){
     return operation+"\t"+projectID+"\t"+uid+"\t"+value;
   }
   
   public void setProjectID(String projectID) {
     this.projectID = projectID;
   }
 
   public void setUid(String uid) {
     this.uid = uid;
   }
 
   public void setPropertyID(String propertyID) {
     this.propertyID = propertyID;
   }
 
   public void setValue(String value) {
     this.value = value;
   }
 
   public void setOperation(String operation) {
     this.operation = operation;
   }
 }
