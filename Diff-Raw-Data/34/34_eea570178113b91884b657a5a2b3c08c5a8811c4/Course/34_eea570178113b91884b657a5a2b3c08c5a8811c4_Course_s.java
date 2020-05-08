/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
 package com.uah.items;
 
 /**
  *
  * @author Pablo Sicilia
  * @version Mavsel Tool 1.0
  */
 public class Course {
     /**************************************************************************
     *                              ATTRIBUTES
     **************************************************************************/
     private String id;    
     private String fullname;
     private String shortname;
     private String summary;    
     private String configExtraData;
 
 
     /**************************************************************************
     *                              getters & setters
     **************************************************************************/
     public String getFullname() {
         return fullname;
     }
 
     public void setFullname(String fullname) {
         this.fullname = fullname;
     }
 
     public String getShortname() {
         return shortname;
     }
 
     public void setShortname(String shortname) {
         this.shortname = shortname;
     }
 
     public String getSummary() {
         return summary;
     }
 
     public void setSummary(String summary) {
         this.summary = summary;
     }
 
     public String getId() {
         return id;
     }
 
     public void setId(String id) {
         this.id = id;
     }
 
     public String getConfigExtraData() {
         return configExtraData;
     }
 
     public void setConfigExtraData(String configExtraData) {
         this.configExtraData = configExtraData;
     }
     
     
 }
