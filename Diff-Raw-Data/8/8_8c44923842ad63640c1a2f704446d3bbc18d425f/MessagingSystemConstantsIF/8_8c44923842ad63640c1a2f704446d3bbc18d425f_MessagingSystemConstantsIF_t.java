 package com.jjw.messagingsystem;
 
 /**
  * Common place to store all of our constants
  * 
  * @author jjwyse
  * 
  */
 public interface MessagingSystemConstantsIF
 {
     // Model attributes
     String MODEL_ERROR = "error";
     String MODEL_MESSAGES = "messages";
 
     // Views
     String VIEW_DISABLED = "disabled";
     String VIEW_INBOX = "inbox";
     String VIEW_REGISTER = "registration";
 
     // Entities
     String ENTITY_USER = "user";
 
     // Groups
     String GROUP_ZE_WORLD = "zeWorld"; // group that represents all users
 
     // Udacity users' entity properties
    String USER_TYPE = "Users";
     String USER_FIRSTNAME = "firstName";
     String USER_LASTNAME = "lastName";
     String USER_USERNAME = "userName";
     String USER_EMAIL = "email";
     String USER_GROUPS = "groups";
     String USER_ENABLED = "enabled";
     String USER_AUTHORITIES = "authorities";
 
     // Udacity messages' entity properties
    String MESSAGE_TYPE = "Messages";
     String MESSAGE_FROM_USERNAME = "fromUserName";
     String MESSAGE_TO_USERNAME = "toUserName";
     String MESSAGE_TO_GROUPNAME = "toGroupName";
     String MESSAGE_CONTENT = "content";
     String MESSAGE_SUBJECT = "subject";
     String MESSAGE_DATE = "date";
 
     // Udacity groups' entity properties
    String GROUP_TYPE = "Groups";
     String GROUP_NAME = "groupName";
 
     // Prefix for Spring MVC
     String REDIRECT = "redirect:";
 }
