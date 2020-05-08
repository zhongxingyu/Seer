 package home;
 
 import java.util.ArrayList;
 
 public class Recursion {
 
 	public ArrayList<ArrayList<Integer>> GetSubsetCollection(
 			ArrayList<Integer> inputSet) {
 		return this.GetSubsetCollection(inputSet, inputSet.size());
 	}
 
 	private ArrayList<ArrayList<Integer>> GetSubsetCollection(
 			ArrayList<Integer> inputSet, int currentIndex) {
 		ArrayList<ArrayList<Integer>> resultSet;
 
 		if (currentIndex == 0) {
 			resultSet = new ArrayList<ArrayList<Integer>>();
 
 			ArrayList<Integer> emptyList = new ArrayList<Integer>();
 			resultSet.add(emptyList);
 		} else {
 			resultSet = GetSubsetCollection(inputSet, currentIndex - 1);
 
			int currInt = inputSet.get(currentIndex-1);
 
 			ArrayList<ArrayList<Integer>> newSubsets = new ArrayList<ArrayList<Integer>>();
 
 			for (ArrayList<Integer> currSetElement : resultSet) {
 				// Add current element to All previous subsets
 				ArrayList<Integer> newElements = new ArrayList<Integer>();
 				newElements.addAll(currSetElement);
 				newElements.add(currInt);
 
 				newSubsets.add(newElements);
 			}
			
 			resultSet.addAll(newSubsets);
 		}
 		return resultSet;
 
 	}
 
 	public void printSubsetCollection(ArrayList<ArrayList<Integer>> subsets) {
 		for (ArrayList<Integer> subsetElements : subsets) {
 			System.out.println();
 			for (Integer currChar : subsetElements) {
 				System.out.print(currChar + ", ");
 			}
 		}

 	}
 }
