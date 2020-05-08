 package edu.hawaii.achriste.ocdutils;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Scanner;
 import java.util.Set;
 
 /**
  * This class contains methods for sorting communities.
  * @author Anthony Christe
  */
 public class Sorter {
     
     /**
      * Sorts communities by size, largest to smallest.
      * @param clustersFile The clusters file to read in.
      * @param sortedClustersFile The output file.
      */
     public static void sortCommunitiesByReverseSize(File clustersFile, File sortedClustersFile) {
         String[] pairs;
         List<Set<String>> list = new LinkedList<>();
         
         try {
             Scanner in = new Scanner(clustersFile);
             
             // Store each line as a set of pairs
             System.out.println("Reading file");
             while(in.hasNextLine()) {
                 pairs = in.nextLine().split(" ");
                list.add(new HashSet<> (Arrays.asList(pairs)));
             }
             
             // Comparator on size of sets to sort in reverse order
             Comparator<Set<String>> comparator = new Comparator<Set<String>>() {
                 @Override
                 public int compare(Set<String> setA, Set<String> setB) {
                     return Integer.valueOf(setB.size()).compareTo(setA.size());
                 }
             };
             
             System.out.println("Sorting");
             
             // Sort the set
             Collections.sort(list, comparator);
             
             BufferedWriter out = new BufferedWriter(new FileWriter(sortedClustersFile));
             String line;
             System.out.println("Writing file");
             for(Set<String> set : list) {
                 line = set.toString();
                 // Remove extra [ and ] from set.toString
                 line = line.substring(1, line.length() - 1);
                
                //Remove extra commas
                line = line.replaceAll(", ", " ");
                 out.append(line + "\n");
             }
             out.close();
             System.out.println("Done");
         }
         catch(FileNotFoundException e) {
             System.err.println("Could not find file " + clustersFile);
         } catch (IOException ex) {
             System.err.println("Could not write file" + sortedClustersFile);
         }
     }
 }
