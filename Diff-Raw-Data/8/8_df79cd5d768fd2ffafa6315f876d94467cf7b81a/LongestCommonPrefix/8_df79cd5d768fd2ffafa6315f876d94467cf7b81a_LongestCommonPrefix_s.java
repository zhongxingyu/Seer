 package guibin.zhang.leetcode.string;
 
 /**
  *
  * Write a function to find the longest common prefix string amongst an array of strings.
  * 
  * Question link: http://www.careercup.com/question?id=5162680650301440
  * 
  * @author Gubin Zhang <guibin.beijing@gmail.com>
  */
 public class LongestCommonPrefix {
     
     public String longestCommonPrefix(String[] strs) {
         
        int wordsLength = strs.length;
         // Initialize prefix length as the length of the first word
         int prefixLength = strs[0].length();
         // Iterate each character of the prefix to check if it exists in each word.
         for (int i = 0; i < prefixLength; i ++) {
             char c = strs[0].charAt(i);
             // Check the prefix character in each word
            for (int j = 1; j < wordsLength; j ++) {
                if (i > strs[j].length() - 1 || c != strs[j].charAt(i)) {
                     return strs[0].substring(0, i);
                 }
             }
         }
         
         return strs[0];
     }
     
     public static void main(String[] args) {
         String[] strs = {"abcdef", "abcdxxx", "abcdabcdef", "abyy"};
         LongestCommonPrefix lcp = new LongestCommonPrefix();
         System.out.println(lcp.longestCommonPrefix(strs));
     }
 }
