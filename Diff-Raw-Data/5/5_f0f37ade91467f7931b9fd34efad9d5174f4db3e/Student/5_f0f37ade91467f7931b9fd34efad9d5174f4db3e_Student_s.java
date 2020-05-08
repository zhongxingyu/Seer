 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package test;
 
 /**
  *
  * @author Joerg Woditschka
  * @author Nadir Yuldashev
  */
 public class Student {
     private int id;
     private double knowledge=0;
     private double knowledgeIncreasement;
     private final double intelligence = initIntelligence();
     private double tiredness;
     private double motivation;
     
     /**
      * 
      * @param id 
      */
     public void Student(int id){
         this.id=id;
     }
 
     /**
      *
      * @return a double of value between 0 and 40 This functions initializes the
      * value of the tiredness attribute.
      */
     private void initTiredness() {
         this.tiredness=Math.round(Math.random()*40);
     }
 
      /**
      *
      * @return a double of value between 50 and 90 This functions initializes the
      * value of the motivation attribute.
      */
     private void initMotivation() {
         this.motivation=Math.round(50+Math.random()*40);
     }
 
      /**
      *
      * @return a double of value between 1,30 and 2,00 This functions initializes the
      * value of the intelligence attribute. This is only done once for each
      * student.
      */
     private double initIntelligence() {
         double result = Math.round((1.3 + Math.random() % 0.7) * 100);
         result = result / 100;
         return result;
     }
 
     /**
      * This functions updates the knowledge and the knowledgeincreasement values
      * of the student. It should be run once every second.
      */
     void updateKnowledge() {
         this.knowledgeIncreasement = (this.motivation - this.tiredness) * this.intelligence * 0.000375;
         if(this.knowledgeIncreasement<0)
            this.knowledgeIncreasement = 0;
         this.knowledge = this.knowledge + this.knowledgeIncreasement;
     }
 
     /**
      *
      * @return the intelligence attribute of the student is returned
      */
     public double getIntelligence() {
         return this.intelligence;
     }
 }
