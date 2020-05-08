 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package melt.Model;
 
 import javax.xml.bind.annotation.XmlElement;
 
 /**
  *
  * @author Maria
  */
 public class Essay extends Question {
 
    private Integer wordLimit;
     private String studentEssay;
     
      /**
      * Empty constructor
      */
     public Essay() {
         
     }
 
     /**
      * Essay constructor with a word limit
      * 
      * @param id
      * @param questionText
      * @param mark
      * @param wordLimit 
      */
     public Essay(int id, String questionText, double mark, int wordLimit) {
         super(id, questionText, mark);
         this.wordLimit = wordLimit;
     }
     
     /**
      * Essay constructor without word limit
      * 
      * @param id
      * @param questionText
      * @param mark 
      */
     public Essay(int id, String questionText, double mark) {
         super(id, questionText, mark);
         this.wordLimit = -1;
     }
 
     /**
      * Check if an answer is correct
      *
      * @return the result of the check
      */
 
     @Override
     public boolean checkAnswer() {
         return true;
     }
 
     public int getWordLimit() {
        return wordLimit.intValue();
     }
 
     public String getStudentEssay() {
         return studentEssay;
     }
 
     @XmlElement
     public void setWordLimit(int wordLimit) {
         this.wordLimit = wordLimit;
     }
 
     public void setStudentEssay(String studentEssay) {
         this.studentEssay = studentEssay;
     }   
 }
