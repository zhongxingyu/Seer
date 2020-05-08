 package amazon;
 
 /* My approach is to generate a map of one list and traversal the other list to see
  * whether there is common integers in both list. If there is, add the the result set;
  * if not, ignore.
  */
 
/* The time complexity of my algorithm is O(MN), where M is the length of the first list
  * and N is the length of the second list. In order to use less memory I choose the smaller
  * list to construct the additional map.
  */
  
 /* Enter your code here. */
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.ArrayList;
 
 public class AmazonQuestion {
 	/**
 	 * Selecting the smaller list to generate the map so as to use as less memory as possible
 	 * @param s smaller list
 	 * @param l larger list
 	 * @return list of intersection without duplicates
 	 */
 	public static List<Integer> find_intersection (List<Integer> s, List<Integer> l) {
 		
         HashMap<Integer, Integer> map_a = new HashMap<Integer, Integer>();
         for (int i = 0; i < s.size(); i++) {
             if (!map_a.containsKey(s.get(i)))
                 map_a.put(s.get(i), 1);
         }
         // result set which automatically eliminate duplicates
         HashSet<Integer> resultset = new HashSet<Integer>(); 
         
         for (int i = 0; i < l.size(); i++) {
             if (map_a.containsKey(l.get(i)))
                 resultset.add(l.get(i));
         }
         // transfer to result list and return
         List<Integer> result = new ArrayList<Integer>();
         for (Integer e : resultset) {
             result.add(e);
         }
         return result;
 		
 	}
 	/**
 	 * core method to find the intersection of two lists
 	 * @param a first list
 	 * @param b second list
 	 * @return list of intersection without duplicates
 	 */
     public static List<Integer> intersection (List<Integer> a, List<Integer> b) {
         if (a == null || b == null)
             return null;
         
         if (a.size() < b.size())
         	return find_intersection(a, b);
         else
         	return find_intersection(b, a);
     }
 }
