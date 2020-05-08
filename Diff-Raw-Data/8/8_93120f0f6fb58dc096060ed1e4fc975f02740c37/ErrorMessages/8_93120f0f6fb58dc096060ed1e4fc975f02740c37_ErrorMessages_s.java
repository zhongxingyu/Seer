 package org.jrask.compiler;
 
 public class ErrorMessages {
 
     public static String expectedClass(String filename, int row, int column) {
         return prefix(filename, row, column) + "expected 'class'";
     }
 
     public static String expectedClassName(String filename, int row, int column) {
         return prefix(filename, row, column) + "expected class name";
     }
 
     public static String fileNotFound(String filename) {
         return filename + ": file not found";
     }
 
     private static String prefix(String filename, int row, int column) {
         return filename + ":" + row + ":" + column + ": ";
     }
 
     public static String classNameMustBeCapitalized(String name, String filename, int row, int column) {
         return prefix(filename, row, column) + "class name '" + name + "' must be capitalized";
     }
 
     public static String methodNameMustNotBeCapitalized(String name, String filename, int row, int column) {
         return prefix(filename, row, column) + "method name '" + name + "' must not be capitalized";
     }
 }
