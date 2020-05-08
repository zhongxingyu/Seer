 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 
 /**
  * 3Sum Given an array S of n integers, are there elements a, b, c in S such
  * that a + b + c = 0? Find all unique triplets in the array which gives the sum
  * of zero. 
  * Note: Elements in a triplet (a,b,c) must be in non-descending order.
 * (ie, a  b  c) The solution set must not contain duplicate triplets. 
  * For example, given array S = [-1,0,1,2,-1,-4], 
  * A solution set is: [ [-1, 0, 1],[-1, -1, 2]] 
  * http://discuss.leetcode.com/questions/15/3sum
  */
 
 /*
  * The solution is O(n^2), no hard part and tricky code need to be take care.
  * Remember using the HashSet first, to avoid duplicate, the duplicate will not
  * only appears on the first elements are the same in the first loop, it also 
  * happens in the inner while loop.
  */
 public class A3Sum {
 	public ArrayList<ArrayList<Integer>> threeSum(int[] num) {
 		// Start typing your Java solution below
 		// DO NOT write main() function
 		HashSet<ArrayList<Integer>> ret = new HashSet<ArrayList<Integer>>();
 		if (num.length < 3) {
 			return new ArrayList<ArrayList<Integer>>(ret);
 		}
 		Arrays.sort(num);
 		int first = 0;
 		while (first < num.length - 2 && num[first] <= 0) {
			if (first != 0 && num[first] == num[first - 1]) {
				first++;
				continue;
			}
 			int second = first + 1;
 			int third = num.length - 1;
 			int need = -num[first];
 			while (second < third) {
 				if (num[second] + num[third] < need) {
 					second++;
 				} else if (num[second] + num[third] > need) {
 					third--;
 				} else {
 					ArrayList<Integer> pair = new ArrayList<Integer>();
 					pair.add(num[first]);
 					pair.add(num[second]);
 					pair.add(num[third]);
 					ret.add(pair);
 					second++;
 					third--;
 				}
 			}
 			first++;
 		}
 		return new ArrayList<ArrayList<Integer>>(ret);
 	}
 }
