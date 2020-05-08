package java.org.boncey.jsphinx;
 
 
 import org.apache.log4j.Logger;
 import org.sphx.api.SphinxClient;
 import org.sphx.api.SphinxException;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 
 /**
  * Example service for searching Sphinx.
  * 
  * @author Darren Greaves
  * @version Copyright (c) 2011 Darren Greaves.
  */
 public class DemoSearchService extends SearchService
 {
 
     /**
      * Sphinx field for text.
      */
     private static final String TEXT = "text";
 
 
     /**
      * Sphinx field for title.
      */
     private static final String TITLE = "title";
 
 
     /**
      * Sphinx field for tags.
      */
     private static final String TAGS = "tags";
 
 
     /**
      * The delta Sphinx index name.
      */
     private static final String INDEX_DELTA = "demo_delta";
 
 
     /**
      * The maximum matches Sphinx can return.
      */
     public static final int MAX_MATCHES = 1000;
 
 
     /**
      * Logger for log4j.
      */
     @SuppressWarnings("unused")
     private static Logger _log = Logger.getLogger(DemoSearchService.class);
 
 
     /**
      * Default constructor.
      * 
      * @param props
      */
     public DemoSearchService(Properties props)
     {
 
         super(props);
     }
 
 
     /**
      * Create a Map of field weightings.
      * 
      * @return
      */
     @Override
     @SuppressWarnings("boxing")
     protected Map<String, Integer> createFieldWeightings()
     {
 
         Map<String, Integer> weightings = new HashMap<String, Integer>();
 
         weightings.put(TAGS, 20);
         weightings.put(TITLE, 10);
         weightings.put(TEXT, 5);
 
         return weightings;
     }
 
 
     /**
      * Get the field to sort by.
      * 
      * @return
      */
     @Override
     protected String getSortField()
     {
 
         return "updated";
     }
 
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void addFilters(SearchCommand searchCommand, SphinxClient sphinx) throws SphinxException
     {
 
         // Example of filtering by tags
 
         // List<Tag> tags = blogSearchCommand.getTags();
         //
         // if (tags.size() > 0)
         // {
         // long[] ids = new long[tags.size()];
         //
         // for (int i = 0; i < tags.size(); i++)
         // {
         // ids[i] = tags.get(i).getId();
         // }
         // sphinx.SetFilter(TAG_ID_FILTER, ids, false);
         // }
     }
 
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected String getDeltaIndexName()
     {
 
         return INDEX_DELTA;
     }
 
 }
