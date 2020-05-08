 package com.hyperactivity.android_app.forum.models;
 
 
 import java.io.Externalizable;
 import java.io.IOException;
 import java.io.ObjectInput;
 import java.io.ObjectOutput;
 
 /**
  * Created with IntelliJ IDEA.
  * User: OMMatte
  * Date: 2013-04-16
  * Time: 13:10
  */
 public class PrivateCategory {
     private int id;
     private int colorCode;
     private Category parentPrivateCategory;
     private Account account;
     private String headLine;
 
     public String getHeadLine() {
         return headLine;
     }
 
     public int getId() {
         return id;
     }
 
     public int getColorCode() {
         return colorCode;
     }
 
     public Category getParentPrivateCategory() {
         return parentPrivateCategory;
     }
 
     public Account getAccount() {
         return account;
     }
 }
