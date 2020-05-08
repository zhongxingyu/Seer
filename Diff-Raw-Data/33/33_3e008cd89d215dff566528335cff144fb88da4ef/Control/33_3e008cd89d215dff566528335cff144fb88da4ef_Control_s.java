 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package viriussimulator;
 
 import java.util.ArrayList;
 
 /**
  *
  * @author baroba
  */
 public class Control {
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         // TODO code application logic here
         City city = new City(2);
        for (int updates = 0; updates < 10; updates++)
         {
             city.update();
         
         }
     }
 }
