 package com.twu28.biblioteca;
 import java.util.*;
 import java.io.*;
 public class Biblioteca {
    private final static String firstMsg = "Welcome";
    private final static String[] menuOptions = {"Menu1","Menu2","Menu 3"};
     private static String menuSelect;
public void main(String[] Args){
     System.out.println(firstMsg);
     System.out.println("Please Select from the following Menu Options");
     for (String item : menuOptions){
         System.out.print(item + " ");
     }
     System.out.println("Please enter your selection");
     Scanner scan = new Scanner(System.in);
     menuSelect = scan.nextLine();
 
 }
    public static String getMessage() {
         return firstMsg;
     }
 
    public static int getNumberOfOptions() {
         return menuOptions.length;
     }
 
    public static Object getMenuSelect() {
         return menuSelect;  //To change body of created methods use File | Settings | File Templates.
     }
 }
