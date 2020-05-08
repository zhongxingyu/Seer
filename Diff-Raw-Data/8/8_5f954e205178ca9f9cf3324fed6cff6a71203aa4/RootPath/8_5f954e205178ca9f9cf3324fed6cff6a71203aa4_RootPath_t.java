 /**
  * Created by IntelliJ IDEA.
  * User: david
  * Date: Dec 1, 2010
  * Time: 8:28:15 AM
  * To change this template use File | Settings | File Templates.
  */
package com.dropedit.controller;
 import java.util.Stack;
 
 public class RootPath {
     private static Stack rootStack = new Stack();
 
     public static void addRootPath(String node){
         rootStack.push(node);
     }
 
     public static String getRootPath(){
         if (rootStack.empty()){
             return "";
         }
         else{
             String returnpath = (String)rootStack.pop();
             return returnpath;
         }
     }
 }
