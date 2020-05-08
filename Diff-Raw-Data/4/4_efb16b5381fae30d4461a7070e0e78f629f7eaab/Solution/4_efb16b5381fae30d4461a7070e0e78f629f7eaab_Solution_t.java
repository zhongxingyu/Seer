 //wild card matching
 public class Solution {
     public boolean isMatch(String s, String p) {
         // Start typing your Java solution below
         // DO NOT write main() function
         if(s == null && p == null)
             return true;
         if(s == null)
             return p.isEmpty();
         if(p == null)
             return s.isEmpty();
 
         int []prev = new int[s.length()+1];
         int [] next = null;
         char[] src = s.toCharArray();
         char[] pattern = p.toCharArray();
         prev[0] = 1;
         for(int i = 1;i<=p.length();++i){
             next = new int[s.length()+1];
             for(int j = 0; j<= s.length();++j){
                 if(pattern[i-1] == '*') {
                     if(j > 0 ) {
                         for(int k = 0; k<=j; ++k){
                             if(prev[k] == 1){
                                 next[j] =1;
                                 break;
                             }
                         }
                     }
                     else
                         next[j]=prev[j];
                 }
                 else if(pattern[i-1] == '?'){
                     if(j == 0)
                         continue;
                     next[j]=prev[j-1];
                 }
                 else {
                     if( j == 0)
                         continue;
                     if(prev[j-1] == 1 && pattern[i-1] == src[j-1])
                         next[j] = 1;
                 }
             }
             prev = next;
         }
         return prev[s.length()]==1;
     }
 }
 //Greedy Solution:
 public boolean isMatch(String s, String p) 
 {
     int n=s.length();
     int m=p.length();
 
     int i=0;
     int j=0;
     int star=-1;
    //this is the starting position in src where we can try to match *
     int sp=0;
 
     while(i<n)
     {
         //one * and multiple *, same effect
         while(j<m && p.charAt(j)=='*')
         {
             star=j++;  //* match 0 character
             sp=i;
         }
 
         if(j==m || (p.charAt(j)!=s.charAt(i) && p.charAt(j)!='?'))
         {
             if(star<0)
                 return false;
             else
             {
                 j=star+1;
                //whenever sp cannot match with the remaining, j will just go back to 
                //next right to * and i will use next try with an increamented index in src
                 i=++sp;     //* match 1 character
             }
         }
         else
         {
             i++;
             j++;
         }
     }
 
     while(j<m && p.charAt(j)=='*') j++;
     return j==m;
 }
 
 
 ////regular expression match
 public class Solution {
     public boolean isMatch(String s, String p) {
 		// Start typing your Java solution below
 		// DO NOT write main() function
 		if(s == null && p == null)
 			return true;
 		if(s == null)
 			return p.isEmpty();
 		if(p == null)
 			return s.isEmpty();
 
         /*save space*/
 		int [] [] dp = new int [3][s.length()+1];
 		for(int i = 0; i<3; ++i){
 			dp[i] = new int [s.length()+1];
 		}
 		char[] src = s.toCharArray();
 		char[] pattern = p.toCharArray();
 		dp[0][0] = 1;
 
 		for(int i = 1;i<=p.length();++i){
             /*remember to reset the line you are going to write!!!!!!!!!!*/
 			for(int j = 0 ; j<= s.length();++j)
 				dp[i%3][j] = 0;
 			
 			//in case of '.', everything is same as one step before
 			if(pattern[i-1] == '.'){
                 //only from 1; not from 0
 				for(int j = 1; j<= s.length();++j){
                     //same as j-1, last position
 					dp [i%3][j] = dp[(i-1)%3][j-1];
 				}
 			}
 			else if(pattern[i-1] == '*'){
 				if(i<=1||pattern[i-2] == '*')
 					throw new IllegalArgumentException(String.format("Illegal Argument %s",p));
 				if(pattern[i-2] == '.'){
 					int j = 0;
 					for(j = 0; j<=s.length(); ++j){
 						if(dp[(i-2)%3][j] == 1)
 							break;
 					}
 					while(j<=s.length()){
 						dp[i%3][j++] = 1;
 					}
 				}else {
 					int j = 0;
 					while(j<=s.length()){
 						if(dp[(i-2)%3][j] == 1){
 							int k = j;
                             //watch here!!! the condition is the same as pattern[i-2]
 							for(k = j; ((k==j)||(k<=s.length() && src[k-1] == pattern[i-2])); ++k)
 								dp[i%3][k] = 1;
 							j = k;
 						}else {
 							++j;
 						}
 					}
 				}
 								
 			}//in case of other, has to be before match and this value matches
 			else{
                 //start from 0, same as last position
 				for(int j = 1; j<= s.length();++j){
 					if(dp[(i-1)%3][j-1] == 1 && pattern[i-1] == src[j-1])
 						dp[i%3][j] = 1;
 				}
 			}
 		}
 		return dp[p.length()%3][s.length()] ==1;
 	}
 }
