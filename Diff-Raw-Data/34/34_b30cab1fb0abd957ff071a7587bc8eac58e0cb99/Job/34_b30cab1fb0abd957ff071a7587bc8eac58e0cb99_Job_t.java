 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
package src.scheduler;
 
 /**
  *
  * @author mateusz
  */
 public class Job {
     private int id;
     private int priority;
     
     public Job (int i, int p) {
         this.id = i;
         this.priority = p;
     }
     
     public Job () {
         this.id = -1;
         this.priority = -1;
     }
     
     public int getId () {
         return this.id;
     }
     
     public int getPriority () {
         return this.priority;
     }
     
     public void setId (int id) {
         this.id = id;
     }
     
     public void setPriority (int priority) {
         this.priority = priority;
     }
     
     public void reset () {
         this.id = -1;
         this.priority = -1;
     }
     
     @Override
     public String toString () {
         return "#" + this.id + " p:" + this.priority;
     }
     
     public static void main(String[] args) {
         Job t[] = new Job [10];
         t[0] = new Job();
         System.out.println(t[0]);
     }
 }
