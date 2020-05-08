 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package model;
 
 import java.util.ArrayList;
 
 /**
  *
  * @author Kasper
  */
 public class SKS_Headline {
     private ArrayList<SKS_question> questions;
     private String headline;
 
     public SKS_Headline(ArrayList<SKS_question> questions, String headline) {
         this.questions = questions;
         this.headline = headline;
     }
 
     public SKS_Headline(String headline) {
         this.headline = headline;
         questions = new ArrayList<>();
     }
 
     public ArrayList<SKS_question> getQuestions() {
         return questions;
     }
 
     public String getHeadline() {
         return headline;
     }
     
     
     public void addQuestion(SKS_question question){
         questions.add(question);
     }

    public void setQuestions(ArrayList<SKS_question> questions) {
        this.questions = questions;
    }
     
 }
