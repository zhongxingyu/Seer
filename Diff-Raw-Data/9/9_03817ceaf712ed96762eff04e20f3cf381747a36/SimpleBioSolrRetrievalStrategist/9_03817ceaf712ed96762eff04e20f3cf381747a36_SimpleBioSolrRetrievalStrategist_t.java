 package edu.cmu.lti.f12.hw2.hw2_team17.retrieval;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.solr.common.SolrDocument;
 import org.apache.solr.common.SolrDocumentList;
 
 import edu.cmu.lti.oaqa.framework.data.RetrievalResult;
import edu.cmu.lti.oaqa.openqa.hello.retrieval.SimpleSolrRetrievalStrategist;
 
 public class SimpleBioSolrRetrievalStrategist extends SimpleSolrRetrievalStrategist {
 
   protected List<RetrievalResult> retrieveDocuments(String query) {
     List<RetrievalResult> result = new ArrayList<RetrievalResult>();
     try {
       SolrDocumentList docs = wrapper.runQuery(query, hitListSize);
 
       for (SolrDocument doc : docs) {
 
         RetrievalResult r = new RetrievalResult((String) doc.getFieldValue("id"),
                 (Float) doc.getFieldValue("score"), query);
         result.add(r);
         System.out.println(doc.getFieldValue("id"));
       }
     } catch (Exception e) {
       System.err.println("Error retrieving documents from Solr: " + e);
     }
     return result;
   }
 
 }
