 public class Solution {
     private boolean [][] buildPaliMap(String s ){
         int len = s.length();
         char [] arr = s.toCharArray();
         boolean [][] palinMap = new boolean[len][len]; 
         
         for(int i=0; i<len; ++i) {
            palinMap[i][i] = true; 
         }
         for(int d=2;d<=len;++d) {
             for( int i=0; i+d-1<len; ++i){
                 int j = i+d-1;
                 if(arr[i] != arr[j]){
                     palinMap[i][j] = false;
                     continue;
                 }
                 if(i+1 > j-1){
                     palinMap[i][j] = true;
                     continue;
                 }
                 palinMap[i][j] = palinMap[i+1][j-1];
             }
         }
         return palinMap;
     }
     public ArrayList<ArrayList<String>> partition(String s) {
         // Start typing your Java solution below
         // DO NOT write main() function
         ArrayList<ArrayList<String>> res = new ArrayList<ArrayList<String>>();
         if( s == null || s.isEmpty()){
             return res;
         }
         boolean[][] palinMap = buildPaliMap(s);
         ArrayList<String> thisPart = new ArrayList<String>();
         dfs(0, s, palinMap, res, thisPart);
         return res;
     }
     private void dfs(int start, String s, boolean[][] connect, 
     ArrayList<ArrayList<String>> res, ArrayList<String> thisPart){
         if(start == s.length()){
             ArrayList<String> copy = new ArrayList<String>(thisPart);
             res.add(copy);
             return;
         }
         for(int i=start; i<s.length(); ++i) {
             if(!connect[start][i]){
                 continue;
             }
            String palinString = s.substring(start,i+1);
            thisPart.add(palinString);
             dfs(i+1, s, connect, res, thisPart);
            thisPart.remove(palinString);
         }
     }
 }
