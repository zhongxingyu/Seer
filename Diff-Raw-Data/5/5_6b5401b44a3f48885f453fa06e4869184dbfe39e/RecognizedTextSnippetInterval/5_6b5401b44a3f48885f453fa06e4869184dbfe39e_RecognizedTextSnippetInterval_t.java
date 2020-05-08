 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ru.urbancamper.audiobookmarker.text;
 
 import java.util.Iterator;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.TreeSet;
 
 /**
  *This class calculates inteval in full text that has min distance wit sub text interval
  * in terms of frequencies vectors distance;
  * @author pozpl
  */
 public class RecognizedTextSnippetInterval {
 
     /**
      * This function calculate two bounds in a full text array in which  a recognized
      * snippet are probably reside.
      * @param fullText full text in integers array form
      * @param subText sub text in integers array form
      * @return Integer start position of recognized interval
      */
     public Integer calculateFullTextBoundsForRecognizedSnippet(
             Integer[] fullText, Integer[] subText){
 
         TreeMap<Integer, Integer> subTextWordsFrequences =
                 this.wordsFrequencesForTextSnippet(subText, 0, subText.length);
 
         TreeMap<Integer, Integer> fullTextSnippetWordsFrequences =
                 this.wordsFrequencesForTextSnippet(fullText, 0, subText.length);
         Integer aggregatedSummForSnipetsDistance =
                 this.aggregatedSummForWorsFrequenciesVectors(
                 fullTextSnippetWordsFrequences, subTextWordsFrequences);
 
        Integer allClustersNumber = fullText.length - subText.length + 1;
         Integer[] snippetsAggregatedSums = new Integer[allClustersNumber];
         snippetsAggregatedSums[0] = aggregatedSummForSnipetsDistance;
         Integer previowsWord = fullText[0];
         Integer previousWordFreq = fullTextSnippetWordsFrequences.get(previowsWord);
         for(int clusterCounter = 1; clusterCounter < allClustersNumber; clusterCounter++){
             Integer addedWord = fullText[clusterCounter + subText.length - 1];
            
             fullTextSnippetWordsFrequences = this.updateFullTextSnippetFrequencies(fullTextSnippetWordsFrequences, previowsWord, addedWord);
 
             aggregatedSummForSnipetsDistance =
                     this.updateAggregatedSum(fullTextSnippetWordsFrequences,
                     subTextWordsFrequences, previowsWord, previousWordFreq,
                     addedWord, aggregatedSummForSnipetsDistance);
             snippetsAggregatedSums[clusterCounter] = aggregatedSummForSnipetsDistance;
             previowsWord = fullText[clusterCounter];
             previousWordFreq = fullTextSnippetWordsFrequences.get(previowsWord);
 
         }
         Integer minDistanceAggregatedSummIndex = this.findSnippetWithMinDistance(snippetsAggregatedSums);
         return minDistanceAggregatedSummIndex;
     }
 
     /**
      * Function returns intex of the cluster with minimal equlidian distance
      * @param snippetsAggregatedSums
      * @return
      */
     private Integer findSnippetWithMinDistance(Integer[] snippetsAggregatedSums){
         Double  minDistance = Math.sqrt(snippetsAggregatedSums[0]);
         Integer minDistanceIndex = 0;
         for(Integer snippetCounter = 0; snippetCounter < snippetsAggregatedSums.length; snippetCounter++){
             Double currentDistance = Math.sqrt(snippetsAggregatedSums[snippetCounter]);
             if(currentDistance < minDistance){
                 minDistance = currentDistance;
                 minDistanceIndex = snippetCounter;
             }
         }
 
         return minDistanceIndex;
     }
 
     /**
      * Update frequencies of words during scanning process. Word to add frequence
      * is reduced to one and frequence of a word to add is increased by 1
      * @param fullTextSnippetFreq Tree map of words frequencies
      * @param wordToRemove word to reduce frequence
      * @param wordToAdd word to increase frequence
      */
     private TreeMap<Integer,Integer> updateFullTextSnippetFrequencies(
             TreeMap<Integer, Integer> fullTextSnippetFreq, Integer wordToRemove,
             Integer wordToAdd){
         Integer wordToRemoveFreq = fullTextSnippetFreq.get(wordToRemove);
         wordToRemoveFreq--;
         if(wordToRemoveFreq <= 0){
             fullTextSnippetFreq.remove(wordToRemove);
         }else{
             fullTextSnippetFreq.put(wordToRemove, wordToRemoveFreq);
         }
         if(fullTextSnippetFreq.containsKey(wordToAdd)){
             Integer wordToAddFreq = fullTextSnippetFreq.get(wordToAdd);
             wordToAddFreq++;
             fullTextSnippetFreq.put(wordToAdd, wordToAddFreq);
         }else{
             fullTextSnippetFreq.put(wordToAdd, Integer.valueOf(1));
         }
         return fullTextSnippetFreq;
     }
 
     /**
      * This function upgrades aggregated sum.
      * @param fullTextSnippetFreqs - full text frequencies
      * @param subTextSnippetFreqs sub text frequencies
      * @param wordToRemove word to remove
      * @param previousWordFreq frequence of word to remove
      * @param wordToAdd word to add
      * @param aggregatedSum aggregated sum value
      * @return
      */
     private Integer updateAggregatedSum(
             TreeMap<Integer, Integer> fullTextSnippetFreqs,
             TreeMap<Integer, Integer> subTextSnippetFreqs,
             Integer wordToRemove,
             Integer previousWordFreq,
             Integer wordToAdd,
             Integer aggregatedSum){
 
         Boolean subTextContainsKey = subTextSnippetFreqs.containsKey(wordToRemove);
         Integer aggregatedRemove = 0;
         if (subTextContainsKey) {
             Integer subWordFreq = subTextSnippetFreqs.get(wordToRemove);
             Integer oldAggregated = (previousWordFreq - subWordFreq) * (previousWordFreq - subWordFreq);
             Integer newAggregated = (previousWordFreq - subWordFreq - 1) * (previousWordFreq - subWordFreq - 1);
             aggregatedRemove = oldAggregated - newAggregated;
         } else {
             Integer oldAggregated = previousWordFreq * previousWordFreq;
             Integer newAggregated = (previousWordFreq - 1) * (previousWordFreq - 1);
             aggregatedRemove = oldAggregated - newAggregated;
         }
 
         Integer updatedAggregatedSum = aggregatedSum - aggregatedRemove;
 
         Integer wordToAddFreq = fullTextSnippetFreqs.get(wordToAdd);
         Boolean subTextContainsWordToAdd = subTextSnippetFreqs.containsKey(wordToAdd);
         Integer aggregatedAdd = 0;
         if (subTextContainsWordToAdd) {
             Integer subWordFreq = subTextSnippetFreqs.get(wordToAdd);
             Integer newAggregated = (wordToAddFreq - subWordFreq) * (wordToAddFreq - subWordFreq);
             Integer oldAggregated = (wordToAddFreq - subWordFreq - 1) * (wordToAddFreq - subWordFreq - 1);
             aggregatedAdd = newAggregated - oldAggregated;
         } else {
             Integer newAggregated = wordToAddFreq * wordToAddFreq;
             Integer oldAggregated = (wordToAddFreq - 1) * (wordToAddFreq - 1);
             aggregatedAdd = newAggregated - oldAggregated;
         }
 
         updatedAggregatedSum += aggregatedAdd;
         return updatedAggregatedSum;
     }
 
 
     /**
      * Calculate equlidian norm for two frequencies vectors
      * @param fullTestSnippetFreqs
      * @param subTestSnippetFreqs
      * @return
      */
     private Integer aggregatedSummForWorsFrequenciesVectors(
             TreeMap<Integer, Integer> fullTestSnippetFreqs,
             TreeMap<Integer, Integer> subTestSnippetFreqs){
         Set<Integer> subTextkeysSet = subTestSnippetFreqs.keySet();
         Set<Integer> fullTextkeysSet = fullTestSnippetFreqs.keySet();
 
         Set<Integer> keysUnion = this.union(subTextkeysSet, fullTextkeysSet);
         Iterator<Integer> keysIterator = keysUnion.iterator();
         Integer aggregatedSumm = 0;
         while(keysIterator.hasNext()){
             Integer key = keysIterator.next();
             Boolean fullTextContainsKey = fullTestSnippetFreqs.containsKey(key);
             Boolean subTextContainsKey =  subTestSnippetFreqs.containsKey(key);
             if(fullTextContainsKey && subTextContainsKey){
                 Integer subWordFreq = subTestSnippetFreqs.get(key);
                 Integer fullWordFreq = fullTestSnippetFreqs.get(key);
                 aggregatedSumm += (fullWordFreq - subWordFreq) * (fullWordFreq - subWordFreq);
             }else if(fullTextContainsKey){
                 Integer fullWordFreq = fullTestSnippetFreqs.get(key);
                 aggregatedSumm += fullWordFreq * fullWordFreq;
             }else if(subTextContainsKey){
                 Integer subWordFreq = subTestSnippetFreqs.get(key);
                 aggregatedSumm += subWordFreq * subWordFreq;
             }
         }
 
         return aggregatedSumm;
     }
 
     private <T> Set<T> union(Set<T> setA, Set<T> setB) {
         Set<T> tmp = new TreeSet<T>(setA);
         tmp.addAll(setB);
         return tmp;
     }
 
     /**
      * Function to calculate for text snippet an array where key will be an word
      * and value will be a count of this word in text snippet
      * @param textArray
      * @param offset - offset in array
      * @param limit - max number of elements in array
      * @return
      */
     private TreeMap<Integer, Integer> wordsFrequencesForTextSnippet(Integer[] textArray,
             Integer offset, Integer limit){
         TreeMap<Integer, Integer> wordsFrequences = new TreeMap<Integer, Integer>();
         for(Integer wordsCounter = offset; wordsCounter < offset + limit; wordsCounter++){
             if(wordsFrequences.containsKey(textArray[wordsCounter])){
                 Integer oldFrequence = wordsFrequences.get(textArray[wordsCounter]);
                 wordsFrequences.put(textArray[wordsCounter], oldFrequence + 1);
             }else{
                 wordsFrequences.put(textArray[wordsCounter], 1);
             }
         }
 
         return wordsFrequences;
     }
 
 }
