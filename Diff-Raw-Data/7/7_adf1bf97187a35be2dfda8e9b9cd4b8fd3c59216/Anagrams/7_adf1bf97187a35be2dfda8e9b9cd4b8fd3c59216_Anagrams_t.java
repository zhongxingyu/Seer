 import java.util.ArrayList;
import java.util.Arrays;
 import java.util.HashMap;
 
 public class Anagrams {
     public ArrayList<String> anagrams(String[] strs) {
         ArrayList<String> result = new ArrayList<String>();
         if (strs.length == 0) return result;
         HashMap<String, Integer> dict = new HashMap<String, Integer>();
         for (int i = 0; i < strs.length; i++){
             String str = sort(strs[i]);
             if (dict.containsKey(str)){
                 if (dict.get(str) >= 0){
                     result.add(strs[dict.get(str)]);
                     dict.put(str, -1);
                 }
                 result.add(strs[i]);
             } else {
                 dict.put(str, i);
             }
         }
         return result;
     }
     
     String sort(String s){
         char[] tmp = s.toCharArray();
         Arrays.sort(tmp);
         return new String(tmp);
     }
 }
