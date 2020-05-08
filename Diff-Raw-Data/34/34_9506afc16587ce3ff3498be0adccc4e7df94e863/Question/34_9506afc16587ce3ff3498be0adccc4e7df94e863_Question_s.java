 package com.forum.repository;
 
 public class Question {
     private String id;
     private String question;
     private String time;
 
    public String getUser_name() {
        return user_name;
     }
 
    private String user_name;

    public Question(String id, String question, String time,String user_name ) {
         this.id = id;
         this.question = question;
         this.time = time;
        this.user_name=user_name;
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
