 package com.sdc.kotlin;
 
 import com.sdc.abstractLanguage.AbstractOperationPrinter;
 
 public class KotlinOperationPrinter extends AbstractOperationPrinter {
     protected static AbstractOperationPrinter ourInstance = new KotlinOperationPrinter();
 
     public static AbstractOperationPrinter getInstance(){
         return ourInstance;
     }
 }
