 package algorithm;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 public class Palindrome_Partitioning {
 	public ArrayList<ArrayList<String>> partition(String s) {
         // Note: The Solution object is instantiated only once and is reused by each test case.
         HashMap<String, ArrayList<ArrayList<String>>> map = new HashMap<String, ArrayList<ArrayList<String>>>();
         return helper(s,map);
     }
     public ArrayList<ArrayList<String>> helper(String s, HashMap<String, ArrayList<ArrayList<String>>> map){
        //if(map.containsKey(s)){
			//return map.get(s);
		//}
         ArrayList<ArrayList<String>> ret = new ArrayList<ArrayList<String>>();
         if(s.length()==0){
             ret.add(new ArrayList<String>());
             return ret;
         }
         for(int i=0; i<s.length(); i++){
             String str = s.substring(0,i+1);
             if(isP(str)){
                 ArrayList<ArrayList<String>> res = helper(s.substring(i+1),map);
                 for(ArrayList<String> list : res){
                     list.add(0, str);
                     ret.add(list);
                 }
             }
         }
         ArrayList<ArrayList<String>> clone = new ArrayList<ArrayList<String>>();
         for(ArrayList<String> list:ret){
         	clone.add((ArrayList<String>)list.clone());
         }
        //map.put(s, clone);
         return ret;
     }
     public boolean isP(String str){
         int i=0;
         int j=str.length()-1;
         while(i<j){
             if(str.charAt(i)!=str.charAt(j))
                 return false;
             i++;
             j--;
         }
         return true;
     }
     
     public static void main(String[] args){
     	System.out.println(new Palindrome_Partitioning().partition("aab"));
     }
 }
