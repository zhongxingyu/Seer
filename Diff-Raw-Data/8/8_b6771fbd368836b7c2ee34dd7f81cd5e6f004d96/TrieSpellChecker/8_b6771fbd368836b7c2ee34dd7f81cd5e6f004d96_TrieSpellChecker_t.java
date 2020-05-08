 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.trie.spellchecker;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
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
             BufferedReader br = new BufferedReader(new FileReader("src/org/trie/util/american-english"));
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
         return compoundSplitter(concatenatedWord, 0);
     }
 
     private static String compoundSplitter(String concatenatedWord, int level) {
         if (DICT.isEmpty()) {
             initialize();
         }
        if (DICT.contains(concatenatedWord) || DICT.contains(concatenatedWord.toLowerCase())) {
             return concatenatedWord;
         } else {
             String firstTerm, secondTerm, lastGoodFirst = "#none";
             int i = 2;
             while (i <= concatenatedWord.length()) {
                 firstTerm = concatenatedWord.substring(0, i);
                 secondTerm = concatenatedWord.substring(i);
                 if (DICT.contains(firstTerm)) {
                     lastGoodFirst = firstTerm;
                     if (DICT.contains(secondTerm)) {
                         return firstTerm + " " + secondTerm;
                     } else {
                         i++;
                     }
                     //return firstTerm + " " + secondTerm;
                 } else {
                     if (firstTerm.equals(concatenatedWord)) {
                        if(level < 1 && !lastGoodFirst.equals("#none")){
                             return lastGoodFirst + " " + compoundSplitter(concatenatedWord.substring(lastGoodFirst.length()), level+1);
                         } else {
                            return (level > 0)? "\b" + concatenatedWord : concatenatedWord;
                         }
                     } else {
                         i++;
                     }
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
