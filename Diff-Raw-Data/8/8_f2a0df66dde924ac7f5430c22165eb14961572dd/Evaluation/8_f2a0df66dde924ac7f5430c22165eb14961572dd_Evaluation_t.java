 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.util.*;
 import java.util.HashMap;
 import java.text.DecimalFormat;
 
 public class Evaluation {
 
     FileReader queryReader;
     FileReader correctResultReader;
     FileReader resultReader;
     
     HashMap<String, HashSet<String>> relevantHash;
     HashMap<String, HashSet<String>> retrievedHash;
 
     public Evaluation(String queriesFilePath, String correctResultsFilePath, String resultsFilePath) {
         try {
             queryReader         = new FileReader(queriesFilePath);
             correctResultReader = new FileReader(correctResultsFilePath);
             resultReader        = new FileReader(resultsFilePath);
             
             relevantHash  = buildDocumentsHash(correctResultReader);
             retrievedHash = buildDocumentsHash(resultReader);
             
             BufferedReader bufferedQueryReader = new BufferedReader(queryReader);
             
             DecimalFormat df = new DecimalFormat("0.00");
             
             // Print out results for now
             System.out.println("Evaluating results");
             
             String queryLine = null;
             while ((queryLine = bufferedQueryReader.readLine()) != null) {
                 String queryId = queryLine.split(":")[0];
                 // Get relevant and retrieved documents for that query
                 Set<String> relevantDocuments  = relevantHash.get(queryId);
                 Set<String> retrievedDocuments = retrievedHash.get(queryId);
                 
                 // Get their sizes
                 double relevantSize = (relevantDocuments != null) ? relevantDocuments.size() : 0;
                 double retrievedSize = (retrievedDocuments != null) ? retrievedDocuments.size() : 0;
                 
                 // Get intersection size of relevant and retrieved documents
                 int intersection = 0;
                 if (relevantDocuments != null && retrievedDocuments != null) {
                     retrievedDocuments.retainAll(relevantDocuments);
                     intersection = retrievedDocuments.size();
                 }
                 
                 // Calculate precision, recall, f-measure
                 double precision = intersection / retrievedSize;
                 double recall    = intersection / relevantSize;
                 double fMeasure  = 2 * (precision * recall) / (precision + recall);
                 System.out.println(queryId + "\tRecall: " + df.format(recall) + ";\tPrecision: " + 
                                    df.format(precision) + ";\tF-Measure: " + df.format(fMeasure));
             }
             bufferedQueryReader.close();
             
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
     
     // Builds Hash with keys being the query id and values a Set with the paths of the documents
     public HashMap<String, HashSet<String>> buildDocumentsHash(FileReader fReader) {
         HashMap<String, HashSet<String>> relevantHash = new HashMap<String, HashSet<String>>(50);
         
         try {
             BufferedReader bufferedResultReader = new BufferedReader(fReader);
             String resultLine = null;
             while ((resultLine = bufferedResultReader.readLine()) != null) {
                 String[] splitLine = resultLine.split("\\s+");
                 String queryId = splitLine[0];
                 
                if (!relevantHash.containsKey(queryId))
                     relevantHash.put(queryId, new HashSet<String>());
                
                relevantHash.get(queryId).add(splitLine[1]);
             }
             bufferedResultReader.close();
             
         } catch (Exception e) {
             e.printStackTrace();
         }
         
         return relevantHash;
     }
     
 }
