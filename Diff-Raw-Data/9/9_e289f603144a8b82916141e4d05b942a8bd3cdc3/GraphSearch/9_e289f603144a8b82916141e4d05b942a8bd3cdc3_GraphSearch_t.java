 package org.triple_brain.module.search;
 
 import org.apache.solr.client.solrj.SolrQuery;
 import org.apache.solr.client.solrj.SolrServer;
 import org.apache.solr.client.solrj.SolrServerException;
 import org.apache.solr.client.solrj.response.QueryResponse;
 import org.apache.solr.common.SolrDocument;
 import org.apache.solr.core.CoreContainer;
 import org.codehaus.jettison.json.JSONArray;
 import org.codehaus.jettison.json.JSONException;
 import org.codehaus.jettison.json.JSONObject;
 import org.triple_brain.graphmanipulator.jena.graph.JenaGraphManipulator;
 import org.triple_brain.module.model.User;
 
 import java.io.UnsupportedEncodingException;
 import java.util.StringTokenizer;
 
 import static org.triple_brain.module.common_utils.CommonUtils.decodeURL;
import static org.triple_brain.module.model.json.graph.VertexJsonFields.*;
 
 /*
 * Copyright Mozilla Public License 1.1
 */
 public class GraphSearch {
 
     private SearchUtils searchUtils;
 
     public static GraphSearch withCoreContainer(CoreContainer coreContainer){
         return new GraphSearch(coreContainer);
     }
 
     private GraphSearch(CoreContainer coreContainer){
         this.searchUtils = SearchUtils.usingCoreCoreContainer(coreContainer);
     }
 
     public JSONArray searchVerticesForAutoCompletionByLabelAndUser(String label, User user){
         JSONArray vertices = new JSONArray();
         JenaGraphManipulator jenaGraphManipulator = JenaGraphManipulator.withUser(user);
         try{
             SolrServer solrServer = searchUtils.solrServerFromUser(user);
             SolrQuery solrQuery = new SolrQuery();
             String sentenceMinusLastWord = sentenceMinusLastWord(label);
             String lastWord = lastWordOfSentence(label);
             solrQuery.setQuery("label:"+sentenceMinusLastWord +"*");
             solrQuery.addFilterQuery("label:"+sentenceMinusLastWord+"*"+lastWord+"*");
             QueryResponse queryResponse = solrServer.query(solrQuery);
             for(SolrDocument document : queryResponse.getResults()){
                 JSONObject searchResult = new JSONObject()
                         .put(ID, decodeURL((String) document.get("uri")))
                         .put(LABEL, document.get("label"));
                 vertices.put(searchResult);
 
             }
         }catch (SolrServerException | UnsupportedEncodingException | JSONException e){
             throw new RuntimeException(e);
         }
         return vertices;
     }
 
     private String lastWordOfSentence(String sentence){
         StringTokenizer tokenizer = new StringTokenizer(
                 sentence,
                 " "
         );
         String lastWord = "";
         while(tokenizer.hasMoreTokens()){
             lastWord = tokenizer.nextToken();
         }
         return lastWord;
     }
 
     private String sentenceMinusLastWord(String sentence){
         if(sentence.contains(" ")){
             return sentence.substring(0, sentence.lastIndexOf(" "));
         }else{
             return "";
         }
     }
 }
