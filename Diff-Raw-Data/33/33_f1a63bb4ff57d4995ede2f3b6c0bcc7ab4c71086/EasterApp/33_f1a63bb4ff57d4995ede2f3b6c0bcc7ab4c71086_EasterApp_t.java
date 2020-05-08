 package org.wooddog.woodstub.demo;
 
 import java.util.Date;
 
 /**
  * Hello world!
  *
  */
 public class EasterApp {
 
     public boolean isEaster() {
         int i = 0;
         System.out.println("just test 1" + i++);
         System.out.println("just test 2" + i++);
         return isEaster(new Date());
     }
 
     private boolean isEaster(Date date) {
         System.out.println("im am not stubbed");
         System.out.println("im am not stubbed");
 
         System.out.println("1");
         System.out.println("2");
         System.out.println("3");
         System.out.println("4");
         System.out.println("5");
         System.out.println("6");
         System.out.println("7");
         System.out.println("8");
         System.out.println("9");
         System.out.println("10");
 
         return false;
 
     }
 }
