 package careercup.linkedin;
 
 import java.util.*;
 
 /**
  * Created by Sobercheg on 12/18/13.
  */
// http://www.careercup.com/question?id=12179920
// Given a large document and a short pattern consisting of a few words (eg. W1 W2 W3), find the shortest string that has all the words in any order (for eg. W2 foo bar dog W1 cat W3 -- is a valid pattern)

 public class WordPatternMatcher {
 
     public String getShortestString(String input, List<String> patterns) {
         // Idea: keep latest positions of every pattern word.
         // If an input word matches a pattern word:
         //  - update its position
         //  - recalculate min distance as: currentPosition - min(pattern word positions)
         //  - remember start and end positions
 
         String[] inputTokens = input.split("[\\s]+");
         int minDistance = Integer.MAX_VALUE;
         int startPosition = 0;
         int endPosition = 0;
         String matchingSubstring = "";
         Map<String, Integer> wordPositionMap = new HashMap<String, Integer>();
 
         for (int i = 0; i < inputTokens.length; i++) {
             String inputWord = inputTokens[i];
             if (patterns.contains(inputWord)) {
                 wordPositionMap.put(inputWord, i);
 
                 if (wordPositionMap.size() == patterns.size()) {
                     int minPosition = Collections.min(wordPositionMap.values());
                     int currentMinDistance = wordPositionMap.get(inputWord) - minPosition;
                     if (currentMinDistance < minDistance) {
                         minDistance = currentMinDistance;
                         startPosition = minPosition;
                         endPosition = wordPositionMap.get(inputWord);
                         matchingSubstring = "";
                         for (int j = startPosition; j <= endPosition; j++) {
                             matchingSubstring += inputTokens[j] + " ";
                         }
                     }
                 }
             }
         }
         if (wordPositionMap.size() < patterns.size())
             throw new IllegalArgumentException("Cannot find a substring matching pattern");
 
         return matchingSubstring;
 
     }
 
     public static void main(String[] args) {
         WordPatternMatcher matcher = new WordPatternMatcher();
         System.out.println(matcher.getShortestString("abc W1 ddd dk W2 df W3 kj W2 W1 d d", Arrays.asList("W1", "W2", "W3")));
     }
 }
