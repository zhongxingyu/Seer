 package com.jim.demo.task;
 
import java.io.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: JimQiao
  * Date: 10/11/13
  * Time: 6:27 AM
  * To change this template use File | Settings | File Templates.
  */
 public class Task1 {
     public static int calc(int n) {
         int sum = 0;
         for (int i = 1; i <= n; i++) {
             sum += i;
         }
         return sum;
     }
 
     public static void main(String[] args) throws Exception {
         BufferedInputStream in = new BufferedInputStream(System.in);
 
        java.io.BufferedReader r = new BufferedReader();
         r.readLine();
     }
 }
