 package uk.ac.ox.oucs.search.solr.response;
 
 import com.google.common.collect.ForwardingList;
 import org.apache.solr.client.solrj.response.QueryResponse;
 import org.apache.solr.common.SolrDocument;
 import org.apache.solr.common.util.NamedList;
 import org.sakaiproject.search.api.SearchList;
 import org.sakaiproject.search.api.SearchResult;
 import org.sakaiproject.search.api.SearchService;
 import uk.ac.ox.oucs.search.producer.ContentProducerFactory;
 import uk.ac.ox.oucs.search.response.filter.SearchItemFilter;
 
 import java.util.*;
 
 import static uk.ac.ox.oucs.search.solr.response.TermVectorExtractor.TermInfo;
 
 /**
  * @author Colin Hebert
  */
 public class SolrSearchList extends ForwardingList<SearchResult> implements SearchList {
     private final List<SearchResult> solrResults;
     private final QueryResponse rsp;
     private final int start;
 
     public SolrSearchList(QueryResponse rsp, SearchItemFilter filter, ContentProducerFactory contentProducerFactory) {
         this.rsp = rsp;
 
         //Get the 'start' value. If not set, use 0
         String expectedStart = ((NamedList<String>) rsp.getHeader().get("params")).get("start");
         this.start = (expectedStart != null) ? Integer.parseInt(expectedStart) : 0;
 
         solrResults = new ArrayList<SearchResult>(rsp.getResults().size());
 
         //Extract TermVector information from the response
         TermVectorExtractor termVectorExtractor = new TermVectorExtractor(rsp);
         Map<String, Map<String, Map<String, TermInfo>>> termsPerDocument = termVectorExtractor.getTermVectorInfo();
 
         for (SolrDocument document : rsp.getResults()) {
            String id = (String) document.getFieldValue(SearchService.FIELD_ID);
             String reference = (String) document.getFieldValue(SearchService.FIELD_REFERENCE);
 
             SolrResult solrResult = new SolrResult();
             solrResult.setIndex(solrResults.size());
             solrResult.setDocument(document);
            solrResult.setHighlights(rsp.getHighlighting().get(id));
            solrResult.setTerms(termsPerDocument.get(id));
             solrResult.setContentProducer(contentProducerFactory.getContentProducerForElement(reference));
 
             solrResults.add(filter.filter(solrResult));
         }
     }
 
     @Override
     public Iterator<SearchResult> iterator(int startAt) {
         Iterator<SearchResult> iterator = iterator();
         //Skip the fist elements
         for (int i = 0; i < startAt && iterator.hasNext(); i++)
             iterator.next();
         return iterator;
     }
 
     @Override
     public int getFullSize() {
         return (int) rsp.getResults().getNumFound();
     }
 
     @Override
     public int getStart() {
         return start;
     }
 
     @Override
     protected List<SearchResult> delegate() {
         return Collections.unmodifiableList(solrResults);
     }
 }
