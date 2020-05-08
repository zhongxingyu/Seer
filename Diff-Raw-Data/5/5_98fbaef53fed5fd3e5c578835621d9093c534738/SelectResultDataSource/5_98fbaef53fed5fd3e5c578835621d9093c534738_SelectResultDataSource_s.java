 package org.linkedgov.questions.model;
 
 import java.util.List;
 
 import org.apache.tapestry5.grid.GridDataSource;
 import org.apache.tapestry5.grid.SortConstraint;
 import org.linkedgov.questions.services.QueryDataService;
 
 /**
  * TODO: improve with pagination that goes all the way back to 4store.
  * 
  * Basic implementation of {@Link GridDataSource} for a Sparql query.
  * 
  * @author Luke Wilson-Mawer <a href="http://viscri.co.uk/">Viscri</a> and 
  * @author <a href="http://mmt.me.uk/foaf.rdf#mischa">Mischa Tuffield</a> for LinkedGov
  * 
  */
 public class SelectResultDataSource implements GridDataSource {
     
     /**
      * The results of the query.
      */
     private List<Triple> currentPage;
     
     /**
      * The query.
      */
     private final Query query;
     
     /**
      * QueryDataService.
      */
     private final QueryDataService queryDataService;
     
     /**
      * The start index.
      */
     private int startIndex = 0;
     
     /**
      * The end index.
      */
     @SuppressWarnings("unused")
     private int endIndex = 0;
     
     /**
      * Constructs a new SelectResultDataSource.
      * 
      * @param query - the query to get the data.
      * @param queryService - the service to send the query to to get the data.
      */
     public SelectResultDataSource(Query query, QueryDataService queryDataService) {
         this.queryDataService = queryDataService;
         this.query = query;
     }
     
     public void prepare(int startIndex, int endIndex,
             List<SortConstraint> sortConstraints) {
         this.endIndex = endIndex;
         this.startIndex = startIndex;
        currentPage = queryDataService.executeQuery(query, endIndex-startIndex, startIndex, null);
     }
 
     public int getAvailableRows() { 
         return queryDataService.executeCountForQuery(query);
     }
     
     public Object getRowValue(int index) {
        return currentPage.get(index - startIndex -1);
     } 
 
     public Class<?> getRowType() {
         return Triple.class;
     }
     
     public List<Triple> getResults() {
         //TODO: this breaks stuff
         return null;    
     }
 
 }
