 import java.io.*;
 import java.util.*;
 
 public class Main {
 
     public static void main(String[] araishikeiwai) throws Exception {
         DanielIndexer indexer = new DanielIndexer();
 
         indexer.getInput();
         indexer.getOutput();
 
        System.out.println("\nStarting indexing folder...");
         
         indexer.executeIndex();
 
        System.out.println("\nIndexing folder finished...");
     }
 }
