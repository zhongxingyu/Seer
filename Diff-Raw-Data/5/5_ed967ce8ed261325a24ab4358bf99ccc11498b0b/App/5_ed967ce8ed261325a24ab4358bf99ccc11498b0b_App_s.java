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
        int index, max=Integer.MAX_VALUE;
 
         if (list.length == 0) {
             throw new RuntimeException("Empty list");
         }
 
 
        for (index = 0; index < list.length-1; index++) {
             if (list[index] > max) {
                 max = list[index];
             }
         }
         return max;
     }
 }
