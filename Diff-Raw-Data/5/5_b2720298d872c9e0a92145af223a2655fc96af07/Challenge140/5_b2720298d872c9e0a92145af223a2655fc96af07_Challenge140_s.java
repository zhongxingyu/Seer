 package com.drguildo.dailyprogrammer.intermediate;
 
 import java.util.Scanner;
 
 /*
 * In graph theory , an adjacency matrix is a data structure that can represent
  * the edges between nodes for a graph in an N x N matrix. The basic idea is
  * that an edge exists between the elements of a row and column if the entry at
  * that point is set to a valid value. This data structure can also represent
 * either a directed graph or an undirected graph , since you can read the rows
  * as being "source" nodes, and columns as being the "destination" (or
  * vice-versa).
  * 
  * Your goal is to write a program that takes in a list of edge-node
  * relationships, and print a directed adjacency matrix for it. Our convention
  * will follow that rows point to columns. Follow the examples for clarification
  * of this convention.
  * 
  * URL: http://www.reddit.com/r/dailyprogrammer/comments/1t6dlf/121813_challenge_140_intermediate_adjacency_matrix/
  */
 public class Challenge140 {
   public static void main(String[] args) {
     Scanner scanner = new Scanner(System.in);
 
     int n = scanner.nextInt(); // the number of nodes
     int m = scanner.nextInt(); // the number of edges
     scanner.nextLine();
 
     int[][] graph = new int[n][n];
 
     String line, src, dst;
     for (int i = 0; i < m; i++) {
       line = scanner.nextLine();
 
       src = line.split(" -> ")[0];
       dst = line.split(" -> ")[1];
 
       for (String srcNode : src.split(" "))
         for (String dstNode : dst.split(" "))
           graph[Integer.parseInt(srcNode)][Integer.parseInt(dstNode)] = 1;
     }
 
     scanner.close();
 
     for (int i = 0; i < n; i++) {
       for (int j = 0; j < n; j++)
         System.out.print(graph[i][j] + " ");
       System.out.println();
     }
   }
 }
