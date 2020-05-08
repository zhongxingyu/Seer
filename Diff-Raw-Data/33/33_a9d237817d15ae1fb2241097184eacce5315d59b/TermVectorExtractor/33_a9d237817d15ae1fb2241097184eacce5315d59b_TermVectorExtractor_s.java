 package uk.ac.ox.oucs.search.solr.response;
 
 import org.apache.solr.client.solrj.response.QueryResponse;
 import org.apache.solr.common.util.NamedList;
 
 import java.util.*;
 
 /**
  * Extract {@link org.apache.solr.handler.component.TermVectorComponent} results from a {@link QueryResponse}
  * <p>
  * Based on <a href="https://issues.apache.org/jira/browse/SOLR-949">SOLR-949</a>, this extractor will get every
  * information provided by  {@link org.apache.solr.handler.component.TermVectorComponent} and put that in a {@link Map}
 * of documents with the documentId as the key.<br />
  * The document map contain itself a {@link Map} of fields available in the document, with the field name as the key.<br />
  * The field map contains itself a {@link Map} of terms used in the field, with the term as the key.<br />
  * The term map associate a term to a {@link TermInfo}.
  * </p>
  * <p/>
  * TODO: Change nested {@link Map} to a custom data-structure able to contain more information?
  *
  * @author Colin Hebert
  */
 public class TermVectorExtractor {
     //Fields found in termVectorComponent
     private static final String TERM_VECTORS = "termVectors";
     private static final String DF = "df";
     private static final String TF = "tf";
     private static final String TF_IDF = "tf-idf";
     private static final String POSITIONS = "positions";
     private static final String OFFSETS = "offsets";
     private static final String UNIQUE_KEY = "uniqueKey";
     private static final String WARNINGS = "warnings";
     private static final String UNIQUE_KEY_FIELD_NAME = "uniqueKeyFieldName";
     /**
      * TermVector data in the form:
      * <pre>
     * "documentId":{
      *     "fieldName":{
      *         "term": {@link TermInfo},
      *         ...
      *     },
      *     ...
      * },
      * ...
      * </pre>
      */
     private Map<String, Map<String, Map<String, TermInfo>>> termVectorInfo = Collections.emptyMap();
 
     public TermVectorExtractor(QueryResponse queryResponse) {
         NamedList<Object> res = (NamedList<Object>) queryResponse.getResponse().get(TERM_VECTORS);
         if (res != null)
             termVectorInfo = extractTermVectorInfo(res);
     }
 
     /**
      * Extract documents from the {@link org.apache.solr.handler.component.TermVectorComponent} result
      *
      * @param termVectorInfosRaw Raw data extracted from the solr query
      * @return A map of document ids associated with a map of fields in these documents
      */
     private Map<String, Map<String, Map<String, TermInfo>>> extractTermVectorInfo(NamedList<Object> termVectorInfosRaw) {
         Map<String, Map<String, Map<String, TermInfo>>> documents = new HashMap<String, Map<String, Map<String, TermInfo>>>(termVectorInfosRaw.size());
         for (Map.Entry<String, Object> termVectorInfoEntryRaw : termVectorInfosRaw) {
             if (UNIQUE_KEY_FIELD_NAME.equals(termVectorInfoEntryRaw.getKey())) {
                 //Ignore unique key field name
             } else if (WARNINGS.equals(termVectorInfoEntryRaw.getKey())) {
                 //Ignore warnings
             } else {
                 //From this point, the entry can be considered as always a document
                 NamedList<Object> documentContentRaw = (NamedList<Object>) termVectorInfoEntryRaw.getValue();
                String documentId = (String) documentContentRaw.get(UNIQUE_KEY);
                 Map<String, Map<String, TermInfo>> fieldTerms = extractDocumentContent(documentContentRaw);
 
                documents.put(documentId, fieldTerms);
             }
         }
         return Collections.unmodifiableMap(documents);
     }
 
     /**
      * Extract fields from a document
      *
      * @param documentContentRaw Raw data extracted from the solr query
      * @return A map of field names associated with a map of terms in these fields
      */
     private Map<String, Map<String, TermInfo>> extractDocumentContent(NamedList<Object> documentContentRaw) {
         Map<String, Map<String, TermInfo>> fields = new HashMap<String, Map<String, TermInfo>>(documentContentRaw.size());
         for (Map.Entry<String, Object> documentContentEntryRaw : documentContentRaw) {
             if (UNIQUE_KEY.equals(documentContentEntryRaw.getKey())) {
                 //Ignore documentId, we already got that earlier
             } else {
                 //From this point, the entry can be considered as always a field in the document
                 NamedList<Object> fieldContentRaw = (NamedList<Object>) documentContentEntryRaw.getValue();
                 String fieldName = documentContentEntryRaw.getKey();
                 Map<String, TermInfo> terms = extractFieldContent(fieldContentRaw);
 
                 fields.put(fieldName, terms);
             }
         }
         return Collections.unmodifiableMap(fields);
     }
 
     /**
      * Extract terms from a field
      *
      * @param fieldContentRaw Raw data extracted from the solr query
      * @return A map of terms associated with various data on these fields, {@link }
      */
     private Map<String, TermInfo> extractFieldContent(NamedList<Object> fieldContentRaw) {
         Map<String, TermInfo> terms = new HashMap<String, TermInfo>(fieldContentRaw.size());
         for (Map.Entry<String, Object> fieldContentEntryRaw : fieldContentRaw) {
             String term = fieldContentEntryRaw.getKey();
             NamedList<Object> termInfoRaw = (NamedList<Object>) fieldContentEntryRaw.getValue();
             TermInfo termInfo = new TermInfo();
             if (termInfoRaw.get(DF) != null)
                 termInfo.setDocumentFrequency(((Number) termInfoRaw.get(DF)).longValue());
             if (termInfoRaw.get(TF) != null)
                 termInfo.setTermFrequency(((Number) termInfoRaw.get(TF)).longValue());
             if (termInfoRaw.get(TF_IDF) != null)
                 termInfo.setTermFrequencyInverseDocumentFrequency(((Number) termInfoRaw.get(TF_IDF)).doubleValue());
             if (termInfoRaw.get(POSITIONS) != null)
                 termInfo.setPositions(extractTermPositions((NamedList<Number>) termInfoRaw.get(POSITIONS)));
             if (termInfoRaw.get(OFFSETS) != null)
                 termInfo.setOffsets(extractTermOffsets((NamedList<Number>) termInfoRaw.get(OFFSETS)));
 
             terms.put(term, termInfo);
         }
         return Collections.unmodifiableMap(terms);
     }
 
     private List<Long> extractTermPositions(NamedList<Number> termPositionsRaw) {
         List<Long> positions = new ArrayList<Long>(termPositionsRaw.size());
         for (Map.Entry<?, Number> position : termPositionsRaw)
             positions.add(position.getValue().longValue());
         return Collections.unmodifiableList(positions);
     }
 
     private List<TermInfo.Offset> extractTermOffsets(NamedList<Number> termOffsetsRaw) {
         List<TermInfo.Offset> offsets = new ArrayList<TermInfo.Offset>(termOffsetsRaw.size() / 2);
         for (Iterator<Map.Entry<String, Number>> iterator = termOffsetsRaw.iterator(); iterator
                 .hasNext(); ) {
             TermInfo.Offset offset = new TermInfo.Offset();
             offset.setStart(iterator.next().getValue().longValue());
             offset.setEnd(iterator.next().getValue().longValue());
             offsets.add(offset);
         }
         return Collections.unmodifiableList(offsets);
     }
 
     public Map<String, Map<String, Map<String, TermInfo>>> getTermVectorInfo() {
         return termVectorInfo;
     }
 
     /**
      * Various data about a term, such as :
      * <ul>
      * <li>document frequency (df), which is the number of documents containing the term</li>
      * <li>term frequency (tf), which is the number of times the term appears in the current document</li>
      * <li><a href="http://en.wikipedia.org/wiki/Tf*idf">term frequency-inverse document frequency</a> (tf-idf)</li>
      * <li>positions</li>
      * <li>offsets</li>
      * </ul>
      */
     public static class TermInfo {
         private Long documentFrequency;
         private Long termFrequency;
         private Double termFrequencyInverseDocumentFrequency;
         private List<Offset> offsets;
         private List<Long> positions;
 
         private TermInfo() {
         }
 
         public Long getDocumentFrequency() {
             return documentFrequency;
         }
 
         private void setDocumentFrequency(Long documentFrequency) {
             this.documentFrequency = documentFrequency;
         }
 
         public Long getTermFrequency() {
             return termFrequency;
         }
 
         private void setTermFrequency(Long termFrequency) {
             this.termFrequency = termFrequency;
         }
 
         public Double getTermFrequencyInverseDocumentFrequency() {
             return termFrequencyInverseDocumentFrequency;
         }
 
         private void setTermFrequencyInverseDocumentFrequency(Double termFrequencyInverseDocumentFrequency) {
             this.termFrequencyInverseDocumentFrequency = termFrequencyInverseDocumentFrequency;
         }
 
         public List<Offset> getOffsets() {
             return offsets;
         }
 
         private void setOffsets(List<Offset> offsets) {
             this.offsets = offsets;
         }
 
         public List<Long> getPositions() {
             return positions;
         }
 
         private void setPositions(List<Long> positions) {
             this.positions = positions;
         }
 
         public static class Offset {
             private Long start;
             private Long end;
 
             private Offset() {
             }
 
             public Long getStart() {
                 return start;
             }
 
             private void setStart(Long start) {
                 this.start = start;
             }
 
             public Long getEnd() {
                 return end;
             }
 
             private void setEnd(Long end) {
                 this.end = end;
             }
         }
     }
 }
