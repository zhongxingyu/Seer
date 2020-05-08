 /* 1.3 Check if one string is a permutation of another,
  * e.g., "dog" is a permutation of "god".
  * Be more specific, let's assume the strings are case sensitive, 
  * and the encoding is known(e.g., ASCII).
  * */
 
 public class P0103 {
    public static boolean isPermutation(String s, String t) {
       if (s == null && t == null) return false;
      if (s.length() != t.length()) return false;
 
       int len = s.length();
       int[] letters = new int[256];
       int numOfUniqueChars = 0;
       int numOfCheckedChars = 0;
 
       for (int i = 0; i < len; i++) {
          char c = s.charAt(i);
          if (letters[c] == 0) numOfUniqueChars++;
          ++letters[c];
       }
 
       for (int i = 0; i < len; i++) {
          char c = t.charAt(i);
          // precondition
          if (letters[c] == 0) return false;
          letters[c]--;
          // postcondition
          if (letters[c] == 0) {
             numOfCheckedChars++;
          }
       }
 
       return numOfCheckedChars == numOfUniqueChars ? true : false;
    }
 
    public static void main(String[] args) {
       System.out.println(P0103.isPermutation("abcdef", "absedf"));
       System.out.println(P0103.isPermutation("abcabc", "aabbcc"));
       System.out.println(P0103.isPermutation("abcabc", "abc"));
      System.out.println(P0103.isPermutation(null, null));
    }
 }
