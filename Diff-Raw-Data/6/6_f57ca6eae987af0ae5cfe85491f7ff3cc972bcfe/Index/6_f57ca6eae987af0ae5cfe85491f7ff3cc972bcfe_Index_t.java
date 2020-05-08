 package com.preppa.web.pages;
 
 import java.util.Date;
 
 /**
  * Index page.
  *
  * 
  * @author pg
  */
 public class Index {
 
    public String getCurrentTime() {
        Date today = new Date();
        String message = ". Tapestry is too cool!";
        return today + message;
     }
 }
