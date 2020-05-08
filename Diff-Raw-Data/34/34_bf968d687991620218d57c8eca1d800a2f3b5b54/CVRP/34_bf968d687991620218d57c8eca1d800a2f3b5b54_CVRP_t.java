 package com.github.daentech;
 
 import com.github.daentech.graphics.RouteVisualiser;
 
 public class CVRP {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		// Run each algorithm and output the result distance
 		
 		// Run the best by default, but allow switching on commandline input
 		
 		// Output a graph file
 		RouteVisualiser rv = new RouteVisualiser();
 		rv.getNodeMap();
		int[][] paths = {{1,3,4,6,7,34,25,11,56,32,1}};
		rv.drawPaths(paths);
		rv.saveImage();
 
 	}
 
 }
