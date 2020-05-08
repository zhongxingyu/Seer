 package org.xezz.reddit;
 
 import java.util.List;
 
 /**
  * Reddit challenge 130
  */
 public class App {
     public static void main(String[] args) {
 
         try {
             System.out.println(getString(DieParser.parse("2d20").rollDice()));
             System.out.println(getString(DieParser.parse("4d6").rollDice()));
         } catch (NumberFormatException e) {
             System.err.println("Invalid format of the input String");
            e.printStackTrace();
         } catch (IllegalArgumentException e) {
             System.err.println("Invalid arguments");
            e.printStackTrace();
         }
     }
 
     private static String getString(List<Integer> rolledTwoDice) {
         StringBuilder stringBuilder = new StringBuilder();
         for (Integer roll : rolledTwoDice) {
             stringBuilder.append(roll).append(" ");
         }
         return stringBuilder.toString().trim();
     }
 }
