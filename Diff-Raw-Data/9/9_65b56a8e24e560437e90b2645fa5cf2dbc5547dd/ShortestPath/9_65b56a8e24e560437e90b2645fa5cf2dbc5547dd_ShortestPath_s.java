 package com.jadekler.app;
 
 import java.util.PriorityQueue;
 
 public class ShortestPath {
     int[][] adjancecyList;
     int[][] edges;
     int[] vertexDistances;
     int[] previous;
     boolean[] visited;
 
     public static void main(String args[]) {
         int[][] adjancecyList = {{1,2,5},{0,2,3},{0,1,3,5},{1,2,4},{3,5},{0,4}};
         int[][] edges = {{7,9,14},{7,10,15},{9,10,11,2},{15,11,6},{6,9},{14,9}};
 
         ShortestPath sp = new ShortestPath();
         sp.djikstrasAlgorithm(adjancecyList, edges, 0, 4);
     }
 
     public void init(int[][] adjancecyList, int[][] edges) {
         if (this.checkConsistency(adjancecyList, edges)) {
             this.visited = new boolean[adjancecyList.length];
             this.previous = new int[adjancecyList.length];
             this.vertexDistances = new int[adjancecyList.length];
 
             for (int i = 0; i < adjancecyList.length; i++) {
                 this.visited[i] = false;
                 this.vertexDistances[i] = Integer.MAX_VALUE;
             }
 
             this.adjancecyList = adjancecyList;
             this.edges = edges;
         } else {
             System.out.println("Vertices and edges don't match");
             System.exit(0);
         }
     }
 
     public boolean checkConsistency(int[][] arr1, int[][] arr2) {
         if (arr1.length != arr2.length) {
             return false;
         }
 
         for  (int i = 0; i < arr1.length; i++) {
             if (arr1[i].length != arr2[i].length) {
                 return false;
             }
         }
 
         return true;
     }
 
     public void djikstrasAlgorithm(int[][] vertices, int[][] edges, int start, int end) {
         this.init(vertices, edges);
         this.djikstrasAlgorithm(start, end);
     }
 
     public void djikstrasAlgorithm(int start, int target) {
         this.visited[start] = true;
         this.vertexDistances[start] = 0;
 
         PriorityQueue<Integer> queue = new PriorityQueue<Integer>();
         queue.offer(start);
 
         while (!queue.isEmpty()) {
             int curItem = queue.poll();
 
             this.visited[curItem] = true;
 
             // Go through adjacent items
             for (int i = 0; i < this.adjancecyList[curItem].length; i++) {
                 int adjancentItem = this.adjancecyList[curItem][i];
                int distance = this.vertexDistances[curItem] + this.edges[curItem][adjancentItem];
 
                 if (distance < this.vertexDistances[adjancentItem]) {
                     this.vertexDistances[adjancentItem] = distance;
                     this.previous[adjancentItem] = curItem;
 
                     if (!this.visited[adjancentItem]) {
                         this.visited[adjancentItem] = true;
                         queue.offer(adjancentItem);
                     }
                 }
             }
         }
 
         int curItem = target;
 
        do {
             System.out.println(curItem);
             curItem = this.previous[curItem];
        } while (curItem != start);
     }
 }
