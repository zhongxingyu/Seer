 package com.babyduncan.guavaUsage;
 
 /**
  * Hello world!
  */
 public class App {
     public static void main(String[] args) {
         System.out.println("Hello World!");
        if (args.length < 1)
            throw new IllegalArgumentException("Missing required argument");
     }
 }
