 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package se.wallenius.domain;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Set;
 import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
 import org.springframework.stereotype.Service;
 import se.wallenius.domain.proxyclient.Provider;
 import se.wallenius.domain.proxyclient.ProxyClient;
 
 /**
  *
  * @author fredrik
  */
 @Service
 public class DefaultRelatedSearchService implements RelatedSearchService {
     
     @Autowired
     private ProxyClient proxyClient;
     
     private final static int RESULT_AMOUNT = 16;
 
    @Cacheable("terms")
     @Override
     public List<Suggestion> findRelatedPhrases(String term) {
         
         final List<Provider> providers = proxyClient.requestForTerm(term);
         
         if (providers == null) {
             return emptyList();
         }
         
         makeAllResponsesLowerCase(providers);
         removeDuplicates(providers);
         
        // First pick the words received from several APIs
         Set<String> higestRanked = this.findWordsExistingInSeveral(providers);
         
         // Result object - Linkedset to keep order
         Set<String> finalResult = new LinkedHashSet<String>(higestRanked);
         
        // Take the best from all
         fillUpWithHighestRanked(finalResult, convertProvidersToLists(providers));
         
         return this.createSuggestionList(finalResult);
     }
     
     private List<Suggestion> emptyList() {
         return new ArrayList<Suggestion>();
     }
     
     private void makeAllResponsesLowerCase(List<Provider> providers) {
         for (Provider provider : providers) {
             List<String> lowercaseOnly = new ArrayList<String>();
             for (String word : provider.getResult()) {
                 lowercaseOnly.add(word.toLowerCase());
             }
             provider.setResult(lowercaseOnly);
         }
     }
     
     private void removeDuplicates(List<Provider> providers) {
        // Using LinkedHashSet to keep order
         for (Provider provider : providers) {
             Set<String> noDuplicates = new LinkedHashSet<String>(provider.getResult());
             provider.setResult(new ArrayList<String>(noDuplicates));
         }
     }
     
     private Set<String> findWordsExistingInSeveral(List<Provider> providers) {
         Set<String> allWords = new HashSet<String>();
         Set<String> duplicates = new HashSet<String>();
         
         for (Provider provider : providers) {
             for (String word : provider.getResult()) {
                 if (!allWords.add(word)) {
                     duplicates.add(word);
                 }
             }
         }
        
         return duplicates;
     }
     
     private void fillUpWithHighestRanked(Set<String> finalResult, List<List<String>> allResults) {
          while (finalResult.size() < RESULT_AMOUNT) {
              int sizeBeforeLoop = finalResult.size();
              
              for (List<String> resultFromOneApi : allResults) {
                  if (!resultFromOneApi.isEmpty()) {
                      finalResult.add(resultFromOneApi.remove(0));
                  }
              }
              
              if (finalResult.size() == sizeBeforeLoop) {
                  // Ran out of phrases
                  return;
              }
          }
     }
     
     private List<List<String>> convertProvidersToLists(List<Provider> providers) {
         List<List<String>> result = new ArrayList<List<String>>();
         for (Provider provider : providers) {
             result.add(provider.getResult());
         }
         return result;
     }
 
     private List<Suggestion> createSuggestionList(Set<String> finalResult) {
         List<Suggestion> result = new ArrayList<Suggestion>();
         for (String phrase : finalResult) {
             result.add(new Suggestion(phrase, 1.0));
         }
         return result;
     }
 
 }
