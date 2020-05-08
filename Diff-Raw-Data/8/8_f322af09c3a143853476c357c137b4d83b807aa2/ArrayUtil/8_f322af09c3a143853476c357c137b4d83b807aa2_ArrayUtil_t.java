 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package infosys.aspiration.libraryutil;
 
 import java.util.Scanner;
 
 /**
  *
  * @author Tintinmj
  */
 public final class ArrayUtil {
     
     private static Scanner input = new Scanner(System.in); 
     
     public static void input1DArray(int []array) {
         
         for (int i = 0; i < array.length; i++) {
             array[i] = input.nextInt();
         }        
     }
     
     public static void input2DArray(int [][]array) {
         
         for (int i = 0; i < array.length; i++) {
             for (int j = 0; j < array[i].length; j++) {
                 array[i][j] = input.nextInt();
             }
         }        
     }
     
     public static boolean contains(String []array, String key) {
         for(String element : array) {
            if(element.equals(key))
                return true;
         }
        
        return false;
     }
     
     public static boolean contains(int []array, int key) {
         String []tempArray = new String[array.length];
         for(int i = 0; i < array.length; i++) {
             tempArray[i] = String.valueOf(array[i]);
         }
         return contains(tempArray, String.valueOf(key));
     }
     
     public static boolean contains(char []array, char key) {
         String []tempArray = new String[array.length];
         for(int i = 0; i < array.length; i++) {
             tempArray[i] = String.valueOf(array[i]);
         }
         return contains(tempArray, String.valueOf(key));
     }
     
     public static boolean contains(Object []array, Object key) {
         String []tempArray = new String[array.length];
         for(int i = 0; i < array.length; i++) {
             tempArray[i] = (array[i]).toString();
         }
         return contains(tempArray, key.toString());
     }
     
     public static boolean contains(double []array, double key) {
         String []tempArray = new String[array.length];
         for(int i = 0; i < array.length; i++) {
             tempArray[i] = String.valueOf(array[i]);
         }
         return contains(tempArray, String.valueOf(key));
     }
 }
