 package com;
 
 import java.util.ArrayList;
 
 public class s2 {
 	
 	public static void main() {
 		
 	}
 	
 	public static LinkedList polygonize(Double[][] input) {
 		
 		//hershberg is the class of the delete-only datastructure
 		Hershberg poly = new Hershberg(input);
 		ArrayList<Double[]> hull = poly.inspect();
 		
 		LinkedList<Double[]> convex = new LinkedList(hull[0]);
 		LinkedList<Double[]> concave = new LinkedList();
 		
 		//true =clockwise
 		boolean direction = true;
 		//keeps track of chains. First 2 iterations of chains are "unusual"
 		int k = 0;
 		//makes sure the chain is added starting from 1, not 0
 		int f = 1;
 		
		while (hull.size() != 1) {
 			
 			for(int i = f; i < hull.size() - k; i++) {
 				
 				if (direction){
 					convex.append(hull.get(i));
 				}
 				else {
 					concave.append(hull.get(i));
 				}
 				
 				if (i != hull.size() - (k+1)) {
 					poly.delete(hull.get(i));
 				}
 				
 			}
 			
 			//k makes sure first 2 runs are different from the rest in amount of points deleted
 			if (k > 1) {
 				poly.delete(hull.get(hull.size()));
 			}
 			else {
 				k++;
 				//assignment of f is here in order to minimize the number of times it's done unnecessary
				//only the very first time 0 assigned to f is "usuful"
 				f = 0;
 			}
 			direction = !direction;
 			hull = poly.inspect();
 			
 			
 		}
 		
 		return concave.reverse().concatenate(convex);
 	}
 
 }
