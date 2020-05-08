 package com.github.barcodescanner.core;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class BCGenerator{
 	private List<Integer> unNormalized = null;
 	
 	public BCGenerator(){}
 	
 	/**
 	 * Generates a new array repesenting the length of each part in the barcode
 	 * 
 	 * @param line segment of 0s and 1s containing a barcode found in BCLocate
 	 * @return the new array
 	 */
 	public List<Integer> generate(List<Integer> line){
 		List<Integer> unNormalized = new ArrayList<Integer>();
 		int count = 1;
 		
 		for (int i = 0; i < line.size() - 1 ; i++){ 
 			if(line.get(i)==line.get(i+1)){
 				count++;
 			}
 			else{
 				unNormalized.add(count);
 				count = 1;
 			}
 		}
 		unNormalized.add(count);
 		this.unNormalized = unNormalized;
 		return unNormalized;
 	}
 	
 	/**
 	 * Normalizes the array given in the generate method
 	 * 
 	 * @return a string containing the normalized data, this string is used as key in the database
 	 */
 	public String normalize(){
<<<<<<< HEAD
		StringBuffer s = null;
=======
 		StringBuffer s = new StringBuffer();
>>>>>>> 909c9d780dd0a5139650f3a79bac148ca4d2e59e
 		if (unNormalized==null){
 			return "No unnormalized data is set";
 		}
 		else {
 			List<Integer> normalized;
 			int lengths = unNormalized.get(0) + unNormalized.get(1) + unNormalized.get(unNormalized.size()-1) + unNormalized.get(unNormalized.size()-2);
 			float division = lengths / 4;
 			
 			for (int i = 0; i < unNormalized.size(); i++){
 				int value = Math.round(unNormalized.get(i)/division);
 				s.append(value);
 			}
 			return s.toString();
 		}
 	}
 	
 	/**
 	 * compares two strings and decides if this is the same product
 	 * 
 	 * 
 	 * @param one
 	 * @param two
 	 * @param threshhold
 	 * @return
 	 */
 	public boolean compare(String one, String two, int threshhold){
 		if (one.length() == two.length()){
 		int distance = 0;
 			int current;
 			for (int i = 0; i < one.length();i++){
 				current = Integer.parseInt(one.charAt(i)+"") - Integer.parseInt(two.charAt(i)+"");
 				distance += Math.abs(current);
 			}
 			return distance <= threshhold;
 		}else {
 			return false;
 		}
 	}
 	
 }
