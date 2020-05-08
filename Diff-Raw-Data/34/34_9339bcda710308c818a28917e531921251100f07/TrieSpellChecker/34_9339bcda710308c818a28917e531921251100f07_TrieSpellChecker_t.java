 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package docprocessing.util;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author Leandro Ordonez <leandro.ordone.ante@gmail.com>
  */
 public class TrieSpellChecker {
 
     public static final List<String> DICT = new ArrayList<>();
 
     public static void initialize() {
         try {
             InputStream dictStream = TrieSpellChecker.class.getResourceAsStream("/docprocessing/util/american-english");
             BufferedReader br = new BufferedReader(new InputStreamReader(dictStream));
             String line;
             while ((line = br.readLine()) != null) {
                 DICT.add(line);
             }
 //            System.out.println(DICT);
 
         } catch (FileNotFoundException ex) {
             Logger.getLogger(TrieSpellChecker.class.getName()).log(Level.SEVERE, null, ex);
         } catch (IOException ex) {
             Logger.getLogger(TrieSpellChecker.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     public static String compoundSplitter(String concatenatedWord) {
        return compoundSplitter("", concatenatedWord, 0);
     }
 
    private static String compoundSplitter(String previous, String concatenatedWord, int level) {
         if (DICT.isEmpty()) {
             initialize();
         }
        String altConcatenatedWord = new StringBuffer(concatenatedWord).insert(concatenatedWord.length() - 1, "'").toString();
         if (DICT.contains(concatenatedWord) || DICT.contains(concatenatedWord.toLowerCase()) || DICT.contains(altConcatenatedWord) || DICT.contains(altConcatenatedWord.toLowerCase())) {
             return concatenatedWord;
         } else {
             String firstTerm, secondTerm, lastGoodFirst = "#none";
             int i = 2;
             while (i <= concatenatedWord.length()) {
                 firstTerm = concatenatedWord.substring(0, i);
                 secondTerm = concatenatedWord.substring(i);
                if (DICT.contains(firstTerm) || DICT.contains(firstTerm.toLowerCase())) {
                     lastGoodFirst = firstTerm;
                     if (DICT.contains(secondTerm)) {
                        String result = previous + " " + firstTerm + " " + secondTerm;
                        return result.trim();
                     } else {
                         i++;
                     }
                     //return firstTerm + " " + secondTerm;
                } else if (firstTerm.equals(concatenatedWord)) {
                    if (level < 1 && !lastGoodFirst.equals("#none")) {
                        return compoundSplitter(lastGoodFirst, concatenatedWord.substring(lastGoodFirst.length()), level + 1);
                     } else {
                        return previous + concatenatedWord;
                     }
                } else {
                    i++;
                 }
 

             }
 //            System.out.println(lastFirstCorrect);
         }
         return concatenatedWord;
     }
 
     public static void main(String[] args) {
         System.out.println(TrieSpellChecker.compoundSplitter("housenumbernumeric")); // -> house number numeric
         System.out.println(TrieSpellChecker.compoundSplitter("wickedweather"));      // -> wicked weather
         System.out.println(TrieSpellChecker.compoundSplitter("liquidweather"));      // -> liquid weather
         System.out.println(TrieSpellChecker.compoundSplitter("driveourtrucks"));     // -> drive our trucks
         System.out.println(TrieSpellChecker.compoundSplitter("gocompact"));          // -> go compact
         System.out.println(TrieSpellChecker.compoundSplitter("slimprojector"));      // -> slim projector
         System.out.println(TrieSpellChecker.compoundSplitter("orcore"));             // -> or core
         System.out.println(TrieSpellChecker.compoundSplitter("zipcode"));            // -> zip code
         System.out.println(TrieSpellChecker.compoundSplitter("asdkjkeerver"));       // -> asdkjkeerver
     }
 }
