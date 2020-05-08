 package com.forum.repository;
 
 public class Question {
     private String id;
     private String question;
    private String userName;
     private String time;
 
    public String getUserName() {
        return userName;
     }
 
    public Question(String id, String question, String time, String userName) {
         this.id = id;
         this.question = question;
         this.time = time;
        this.userName = userName;
     }
 
     public String getId() {
         return id;
     }
 
     public String getQuestion() {
         return question;
     }
 
     public String getTime() {
         return time;
     }
 }
