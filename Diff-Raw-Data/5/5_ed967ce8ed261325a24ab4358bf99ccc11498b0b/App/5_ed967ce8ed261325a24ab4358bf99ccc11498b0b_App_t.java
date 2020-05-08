 package org.maventest;
 
 /**
  * Hello world!
  *
  */
 public class App 
 {
     public static void main( String[] args )
     {
         System.out.println( "Hello World!" );
     }
 
     public static int largest(int[] list) {
        int index, max=0;
 
         if (list.length == 0) {
             throw new RuntimeException("Empty list");
         }
 
 
        for (index = 0; index < list.length; index++) {
             if (list[index] > max) {
                 max = list[index];
             }
         }
         return max;
     }
 }
