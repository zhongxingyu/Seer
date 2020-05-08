 /**
  * A regex object represents a regular expression. A regular expression can be
  * recursively defined in the following way: r = <alphanumeric literal> Matches
  * one copy of the literal given. r = . Matches precisely one character r = r1r2
  * (concatenation of regexes) Matches the r1, then r2, starting from the end of
  * the match of r1. r = r1* Matches zero or more copies of r1. r = r1+ Matches
  * one or more copies of r1 r = [r1|r2] Matches r1 or r2, but not both.
  *
  * Notice that we are pretending that we are only interested in matching
  * alphanumerics; this is to prevent us from having to deal with escape
  * sequences and other ugliness.
  *
  * Here are some example regexes and strings:
  *
  * Regex                String                  Matches?
  * a                    a                       yes
  * a                    b                       no
  * .                    a                       yes
  * .                    b                       yes
  * a*                                           yes
  * a*                   a                       yes
  * a*                   aaaa                    yes
  * a*                   aab                     no
  * a+                                           no
  * a+                   a                       yes
  * a+b                  aaab                    yes
  * a+b                  b                       no
  * .*b                  aaab                    yes
  * [a|b]                a                       yes
  * [a|b]                b                       yes
  * [a|b]                                        no
  *
  * @author Nathan
  */
 import java.text.ParseException;
 
 public class Regex {
 
     private String regex;
     private boolean initialized;
 
     public Regex() {
         regex = null;
     }
 
     public Regex(String regex) throws ParseException {
         initialize(regex);
     }
 
     /**
      * Instantiates a Regex that matches against the regex given in the parameter.
      * Checks to ensure that the syntax is valid, and throws an IllegalArgumentException if not.
      */
     public void initialize(String regex) throws ParseException {
         int offset = isValidSyntax(regex, 0);
         if (offset == -1) {
             this.regex = regex;
             initialized = true;
         } else {
             throw new ParseException("Syntax Error in regex:  "
                     + regex + ".", offset);
         }
     }
 
     /**
      * checkSyntax verifies that the first token of the regular expression, then
      * recursively calls checkSyntax on any unverified part of the expression.
      * This method should return true if and only if the regex it receives as a parameter
      * is a valid representation of a regular expression.
      */
     private int isValidSyntax(String regex, int startPos) {
         if (regex.length() == 0) {
             return -1;
         } else if (Character.isLetterOrDigit(regex.charAt(0))
                 || regex.charAt(0) == '.') {
             if (regex.length() > 1
                     && (regex.charAt(1) == '+' || regex.charAt(1) == '*')) {
                 return isValidSyntax(regex.substring(2), startPos + 2);
             } else {
                 return isValidSyntax(regex.substring(1), startPos + 1);
             }
         } else if (regex.charAt(0) == '[') {
             int off = 0;
             if ( (off = isValidSyntax(regex.substring(1, regex.indexOf('|')), startPos + 1)) >= 0 )
                 return off;
             if ( (off = isValidSyntax(regex.substring(regex.indexOf('|') + 1, regex.lastIndexOf(']')), startPos + regex.indexOf('|') + 1)) >= 0 )
                 return off;
             if ( (off = isValidSyntax("l" + regex.substring(regex.lastIndexOf(']') + 1), startPos + regex.lastIndexOf(']') + 1)) >= 0)
                 return off;
             return -1;
         } else {
             return startPos;
         }
     }
 
     /**
      * Returns true if and only if this Regex matches the string given.  See the class description
      * for examples of matching.
      * @param toMatch the string to match against
      */
     public boolean matches(String toMatch) {
         if (!initialized) return false;
         return matches(this.regex, toMatch);
     }
 
     /**
      * Returns true if the given string matches the given regex.  Works recursively; matching
      * the first token of the regex against the first token of the string to match, then stripping
      * off the first token of the regex and the matched portion of the string and proceeding.
      * @return true iff the regex matches the string toMatch.
      */
     private static boolean matches(String regex, String toMatch) {
         if (regex.length() == 0 && toMatch.length() == 0) {
             // base case; both strings are empty, it's a match.
             return true;
         } else if (regex.length() == 0 && toMatch.length() > 0) {
             // if we have portions of the string left over,
             // and the regex is empty, we failed to match.
             return false;
         } else if (Character.isLetterOrDigit(regex.charAt(0))) {
             // if we have a literal, we first check to see if it's modified by a * or +
             if (regex.length() > 1 && regex.charAt(1) == '*') {
                 if (toMatch.length() > 0 && toMatch.charAt(0) == regex.charAt(0)) {
                 // if there's a star, we check to see if the character matches
                     // if it does, strip it off and leave the star on.
                     return matches(regex, toMatch.substring(1));
                 } else {
                     // if not, strip off the star.
                     return matches(regex.substring(2), toMatch);
                 }
             } else if (regex.length() > 1 && regex.charAt(1) == '+') {
                 // plusses work much like stars, except that we can't have no match
                 if (toMatch.length() == 0) {
                     return false;
                 }
                 if (toMatch.charAt(0) == regex.charAt(0)) {
                     // they're so much like stars that after one match, we replace it with a star.
                     return matches(regex.charAt(0) + "*" + regex.substring(2),
                             toMatch.substring(1));
                 }
             } else {
                 // if we don't have a star or a plus, we just make sure that we match the
                 // current character, then move on.
                 return (toMatch.length() != 0 && regex.charAt(0) == toMatch.charAt(0))
                     && matches(regex.substring(1), toMatch.substring(1));
             }
         } else if (regex.charAt(0) == '.') {
             if (regex.length() > 1) {
                 if (regex.charAt(1) == '*') {
                     // .* matches everything
                     return true;
                 } else if (regex.charAt(1) == '+') {
                     // .+ matches everything, if there's anything left to match
                     return toMatch.length() > 1;
                 }
             } else {
                 // we'll match just a single character this way.
                 return matches(regex.substring(1), toMatch.substring(1));
             }
         } else if (regex.charAt(0) == '[') {
             // split this into two regexes; if it matches the left part OR the right part, match.
             String leftRegex = regex.substring(1, regex.lastIndexOf('|'));
             String rightRegex = regex.substring(regex.lastIndexOf('|') + 1,
                     regex.lastIndexOf(']'));
             String remainderRegex = regex.substring(regex.lastIndexOf(']') + 1);
            if (leftRegex.length() == 0 || rightRegex.length() == 0 && remainderRegex.length() > 0) {
                if (remainderRegex.charAt(0) == '+') {
                    // if we have E+ as one option, convert the plus to star
                    remainderRegex = '*' + remainderRegex.substring(1);
                }
            }
             return matches(leftRegex + remainderRegex, toMatch)
                 || matches(rightRegex + remainderRegex, toMatch);
         }
         // if we got a character we didn't expect in the regex, no match.
         return false;
     }
 
     /**
      * Returns the largest portion of the string, if any, which matches this Regex.
      * @return the substring that matches this regex, or null if no substring matches.
      */
     public String partialMatch(String toMatch) {
         if (!initialized) return null;
         // start from the beginning, and test every substring from beginning to end
         String largestMatch = "";
         boolean match = false;
         for (int i = 0; i < toMatch.length(); i++) {
             for (int j = i; j <= toMatch.length(); j++) {
                 if (matches(toMatch.substring(i, j))) {
                     match = true;
                     if (j - i > largestMatch.length()) {
                         largestMatch = toMatch.substring(i, j);
                     }
                 }
             }
         }
         return (match) ? largestMatch : null;
     }
 
 
     /**
      * Replaces every instance of a match with this regex.
      * Overlapping match areas should be dealt with from left to right; for instance:
      * new Regex("aaa").replaceMatching("aaaaaaa", "b") should return "bba", not "bab" or "abb".
      * @param toMatch the string to find the regex in
      * @param toReplace the text to replace the regex with
      * @return the new string, with the text in toMatch that matches this regex replaced by toReplace.
      */
     public String replaceMatching(String toMatch, String toReplace) {
         if (!initialized) return toReplace;
 
         while (partialMatch(toMatch) != null) {
             String partialMatch = partialMatch(toMatch);
             toMatch = toMatch.replace(partialMatch, toReplace);
         }
         return toMatch;
     }
 }
