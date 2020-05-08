 package org.sakaiproject.search.solr.indexing;
 
 import com.google.common.collect.Iterators;
 import org.apache.solr.client.solrj.SolrQuery;
 import org.apache.solr.client.solrj.SolrServer;
 import org.apache.solr.client.solrj.SolrServerException;
 import org.apache.solr.client.solrj.util.ClientUtils;
 import org.apache.solr.common.SolrDocument;
 import org.apache.solr.common.SolrDocumentList;
 import org.apache.solr.common.SolrInputDocument;
 import org.apache.solr.common.util.DateUtil;
 import org.apache.solr.common.util.NamedList;
 import org.apache.tika.Tika;
 import org.apache.tika.metadata.Metadata;
 import org.sakaiproject.search.api.EntityContentProducer;
 import org.sakaiproject.search.api.SearchIndexBuilder;
 import org.sakaiproject.search.api.SearchService;
 import org.sakaiproject.search.producer.BinaryEntityContentProducer;
 import org.sakaiproject.search.producer.ContentProducerFactory;
 import org.sakaiproject.search.solr.SolrSearchIndexBuilder;
 import org.sakaiproject.search.solr.util.AdminStatRequest;
 import org.sakaiproject.site.api.Site;
 import org.sakaiproject.site.api.SiteService;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Reader;
 import java.util.*;
 
 /**
  * Set of methods used to facilitate the usage of solr.
  *
  * @author Colin Hebert
  */
 public class SolrTools {
     private static final String PROPERTY_PREFIX = "property_";
     private static final String UPREFIX = PROPERTY_PREFIX + "tika_";
     private static final Logger logger = LoggerFactory.getLogger(SolrTools.class);
     /**
     * Maximum number of characters retrieved in a document parsed by Tika.
      */
     private static final int MAX_STRING_LENGTH = 10000000;
     private SiteService siteService;
     private SearchIndexBuilder searchIndexBuilder;
     private ContentProducerFactory contentProducerFactory;
     private SolrServer solrServer;
     private Tika tika;
 
     /**
      * Initialises tika if needed.
      */
     public void init() {
         tika = new Tika();
         tika.setMaxStringLength(MAX_STRING_LENGTH);
     }
 
     /**
      * Generates a {@link SolrInputDocument} to index the given resource thanks to its {@link EntityContentProducer}.
      *
      * @param reference  resource to index
      * @param actionDate date of creation of the indexation task
      * @return a document ready to be indexed
      */
     public SolrInputDocument toSolrDocument(String reference, Date actionDate) {
         EntityContentProducer contentProducer = contentProducerFactory.getContentProducerForElement(reference);
         if (logger.isDebugEnabled())
             logger.debug("Create a solr document to add '" + reference + "' to the index.");
 
         SolrInputDocument document = new SolrInputDocument();
 
         //The date_stamp field should be automatically set by solr (default="NOW"), if it isn't set here
         document.addField(SearchService.DATE_STAMP, actionDate);
         document.addField(SearchService.FIELD_REFERENCE, reference);
         document.addField(SearchService.FIELD_CONTAINER, contentProducer.getContainer(reference));
         document.addField(SearchService.FIELD_TYPE, contentProducer.getType(reference));
         document.addField(SearchService.FIELD_TITLE, contentProducer.getTitle(reference));
         document.addField(SearchService.FIELD_TOOL, contentProducer.getTool());
         document.addField(SearchService.FIELD_URL, contentProducer.getUrl(reference));
         document.addField(SearchService.FIELD_SITEID, contentProducer.getSiteId(reference));
 
         //Add the custom properties
         Map<String, Collection<String>> properties = extractCustomProperties(reference, contentProducer);
         for (Map.Entry<String, Collection<String>> entry : properties.entrySet()) {
             document.addField(PROPERTY_PREFIX + entry.getKey(), entry.getValue());
         }
 
         //Add the content
         if (contentProducer instanceof BinaryEntityContentProducer) {
             // A tika digested document adds content and metadata to the document.
             setDocumentTikaProperties(reference, document, (BinaryEntityContentProducer) contentProducer);
         } else {
             String content;
             if (contentProducer.isContentFromReader(reference))
                 content = readerToString(contentProducer.getContentReader(reference));
             else
                 content = contentProducer.getContent(reference);
             document.setField(SearchService.FIELD_CONTENTS, stripNonCharCodepoints(content));
         }
 
         return document;
     }
 
     /**
      * Gets the content of a document from a Reader and converts it to a String.
      *
      * @param reader content of the document as a Reader
      * @return the content of the document.
      */
     private String readerToString(Reader reader) {
         StringBuffer sb = new StringBuffer();
         try {
             BufferedReader br = new BufferedReader(reader);
             String tmp;
             while ((tmp = br.readLine()) != null) {
                 sb.append(tmp);
             }
         } catch (IOException e) {
             logger.error("Couldn't extract the content from a reader.", e);
             sb = new StringBuffer();
         }
         return sb.toString();
     }
 
     /**
      * Extracts additional document properties and content through Tika.
      *
      * @param reference             reference of the document to index.
      * @param document              solr document about to be index.
      * @param binaryContentProducer contentProducer for the document.
      */
     private void setDocumentTikaProperties(String reference, SolrInputDocument document,
                                            BinaryEntityContentProducer binaryContentProducer) {
         try {
             Metadata metadata = new Metadata();
             String resourceName = binaryContentProducer.getResourceName(reference);
             String contentType = binaryContentProducer.getContentType(reference);
             InputStream contentStream = binaryContentProducer.getContentStream(reference);
             if (resourceName != null)
                 metadata.add(Metadata.RESOURCE_NAME_KEY, resourceName);
             if (contentType != null)
                 metadata.add(Metadata.CONTENT_TYPE, contentType);
             //Extract the content of the document (and additional properties in metadata)
             if (contentStream != null) {
                 String documentContent = tika.parseToString(contentStream, metadata);
                 document.setField(SearchService.FIELD_CONTENTS, documentContent);
             }
 
             //Add additional properties extracted by Tika to the document
             for (String metadataName : metadata.names())
                 for (String metadataValue : metadata.getValues(metadataName))
                     document.addField(UPREFIX + metadataName, metadataValue);
         } catch (Exception e) {
             logger.warn("Couldn't parse the content of '" + reference + "'", e);
         }
     }
 
     /**
      * Extracts properties from the {@link EntityContentProducer}
      * <p>
      * The {@link EntityContentProducer#getCustomProperties(String)} method returns a map of different kind of elements.
      * To avoid casting and calls to {@code instanceof}, extractCustomProperties does all the work
      * and returns a formatted map containing only {@link Collection<String>}.
      * </p>
      *
      * @param reference       affected resource
      * @param contentProducer producer providing properties for the given resource
      * @return a formatted map of {@link Collection<String>}
      */
     @SuppressWarnings("unchecked")
     private Map<String, Collection<String>> extractCustomProperties(String reference,
                                                                     EntityContentProducer contentProducer) {
         Map<String, ?> m = contentProducer.getCustomProperties(reference);
 
         if (m == null)
             return Collections.emptyMap();
 
         Map<String, Collection<String>> properties = new HashMap<String, Collection<String>>(m.size());
         for (Map.Entry<String, ?> propertyEntry : m.entrySet()) {
             String propertyName = toSolrFieldName(propertyEntry.getKey());
             Object propertyValue = propertyEntry.getValue();
             Collection<String> values;
 
             // Check for basic data type that could be provided by the EntityContentProducer
             // If the data type can't be defined, nothing is stored. The toString method could be called,
             // but some values could be not meant to be indexed.
             if (propertyValue instanceof String)
                 values = Collections.singleton((String) propertyValue);
             else if (propertyValue instanceof String[])
                 values = Arrays.asList((String[]) propertyValue);
             else if (propertyValue instanceof Collection)
                 values = (Collection<String>) propertyValue;
             else {
                 if (propertyValue != null)
                     logger.warn("Couldn't find what the value for '" + propertyName + "' was. It has been ignored.");
                 values = Collections.emptyList();
             }
 
             // If this property was already present there
             // This shouldn't happen, but if it does everything must be stored
             if (properties.containsKey(propertyName)) {
                 logger.warn("Two properties had a really similar name '" + propertyName + "' and were merged. "
                         + "This shouldn't happen!");
                 if (logger.isDebugEnabled())
                     logger.debug("Merged values '" + properties.get(propertyName) + "' with '" + values);
                 values = new ArrayList<String>(values);
                 values.addAll(properties.get(propertyName));
             }
 
             properties.put(propertyName, values);
         }
 
         return properties;
     }
 
     /**
      * Replaces special characters, turn to lower case and avoid repetitive '_'.
      *
      * @param propertyName String to filter.
      * @return a filtered name more appropriate to use with solr.
      */
     private String toSolrFieldName(String propertyName) {
         StringBuilder sb = new StringBuilder(propertyName.length());
         boolean lastUnderscore = false;
         for (Character c : propertyName.toLowerCase().toCharArray()) {
             if ((c < 'a' || c > 'z') && (c < '0' || c > '9'))
                 c = '_';
             if (!lastUnderscore || c != '_')
                 sb.append(c);
             lastUnderscore = (c == '_');
         }
         if (logger.isDebugEnabled())
             logger.debug("Transformed the '" + propertyName + "' property into: '" + sb + "'");
         return sb.toString();
     }
 
     /**
      * Removes non-characters and non-printable characters from a String.
      *
      * @param input content containing non-characters or non-printable characters
     * @return a String stripped from unusable characters.
      */
     private String stripNonCharCodepoints(String input) {
         StringBuilder retVal = new StringBuilder();
         char ch;
 
         for (int i = 0; i < input.length(); i++) {
             ch = input.charAt(i);
             //CHECKSTYLE.OFF: MagicNumber - Characters are full of magic number, there is nothing to check here.
 
             // Strip all non-characters and non-printable control characters except tabulator,
             // new line and carriage return
             // See http://unicode.org/cldr/utility/list-unicodeset.jsp?a=[:Noncharacter_Code_Point=True:]
             if (ch % 0x10000 != 0xffff && // 0xffff - 0x10ffff range step 0x10000
                     ch % 0x10000 != 0xfffe && // 0xfffe - 0x10fffe range
                     (ch <= 0xfdd0 || ch >= 0xfdef) && // 0xfdd0 - 0xfdef
                     (ch > 0x1F || ch == 0x9 || ch == 0xa || ch == 0xd)) {
 
                 retVal.append(ch);
             }
 
             //CHECKSTYLE.ON: MagicNumber
         }
 
         return retVal.toString();
     }
 
     /**
      * Transforms dates to the Solr date format.
      *
      * @param date date to format.
      * @return a String respecting the format used by Solr.
      */
     public String format(Date date) {
         return DateUtil.getThreadLocalDateFormat().format(date);
     }
 
     /**
      * Gets every indexable site.
      * <p>
      * Usually this method is used for heavy operations affecting every site using the search index.
      * </p>
      *
      * @return every site that is considered as indexable.
      */
     public Queue<String> getIndexableSites() {
         Queue<String> refreshedSites = new LinkedList<String>();
         List<Site> sites = siteService.getSites(SiteService.SelectionType.ANY, null, null, null,
                 SiteService.SortType.NONE, null);
         for (Site s : sites) {
             if (isSiteIndexable(s)) {
                 refreshedSites.offer(s.getId());
             }
         }
         return refreshedSites;
     }
 
     /**
      * Gets the references of every indexed document still available for a specific site.
      * <p>
      * This method is most commonly used to refresh a site.<br />
      * A document could be in the index while not being available within sakai, in that case the reference won't be
      * returned.
      * </p>
      *
      * @param siteId site in which the documents are.
      * @return a queue of every references of documents belonging to a site.
      * @throws SolrServerException thrown if the query to get references failed.
      */
     public Queue<String> getValidReferences(String siteId) throws SolrServerException {
         if (logger.isDebugEnabled())
             logger.debug("Obtaining indexed elements for site: '" + siteId + "'");
         SolrQuery query = new SolrQuery()
                 .setQuery(SearchService.FIELD_SITEID + ":" + ClientUtils.escapeQueryChars(siteId))
                         //TODO: Use paging?
                 .setRows(Integer.MAX_VALUE)
                 .addField(SearchService.FIELD_REFERENCE);
 
         SolrDocumentList results = solrServer.query(query).getResults();
         Queue<String> references = new LinkedList<String>();
         for (SolrDocument document : results) {
             String reference = (String) document.getFieldValue(SearchService.FIELD_REFERENCE);
             if (contentProducerFactory.getContentProducerForElement(reference) != null)
                 references.add(reference);
         }
         return references;
     }
 
     /**
      * Gets the reference of every document available (not only the indexed ones) for a specific site.
      * <p>
      * This method gets the documents currently available in a site, not only the indexed ones.<br />
      * This method is most commonly used to reindex a site.
      * </p>
      *
      * @param siteId identifier of the site which contains documents
      * @return a queue of references for every document available within a site.
      */
     public Queue<String> getSiteDocumentsReferences(String siteId) {
         //TODO: Replace by a lazy queuing system
         Queue<String> references = new LinkedList<String>();
 
         for (EntityContentProducer contentProducer : contentProducerFactory.getContentProducers()) {
             Iterators.addAll(references, contentProducer.getSiteContentIterator(siteId));
         }
 
         return references;
     }
 
     /**
      * Checks whether a site should be indexed or not.
      *
      * @param site site to check.
      * @return true if the site is indexable, false otherwise.
      */
     private boolean isSiteIndexable(Site site) {
         return !siteService.isSpecialSite(site.getId()) && isSiteWithToolIndexable(site) && isSiteTypeIndexable(site);
     }
 
     /**
      * Checks if a site is indexable depending if it's a user site.
      *
      * @param site site to check.
      * @return true if the site is indexable based on the site type.
      */
     private boolean isSiteTypeIndexable(Site site) {
         return !(searchIndexBuilder.isExcludeUserSites() && siteService.isUserSite(site.getId()));
     }
 
     /**
      * Checks if the site is indexable based on the presence of search tool.
      *
      * @param site site to check.
      * @return true if the site is indexable based on the site type.
      */
     private boolean isSiteWithToolIndexable(Site site) {
         return !searchIndexBuilder.isOnlyIndexSearchToolSites()
                 || site.getToolForCommonId(SolrSearchIndexBuilder.SEARCH_TOOL_ID) != null;
     }
 
     /**
      * Gets the number of documents currently pending in the solr server.
      *
      * @return the number of documents awaiting indexation in the solr server.
      */
     @SuppressWarnings("unchecked")
     public int getPendingDocuments() {
         try {
             AdminStatRequest adminStatRequest = new AdminStatRequest();
             adminStatRequest.setParam("key", "updateHandler");
             NamedList<Object> result = solrServer.request(adminStatRequest);
             NamedList<Object> mbeans = (NamedList<Object>) result.get("solr-mbeans");
             NamedList<Object> updateHandler = (NamedList<Object>) mbeans.get("UPDATEHANDLER");
             NamedList<Object> updateHandler2 = (NamedList<Object>) updateHandler.get("updateHandler");
             NamedList<Object> stats = (NamedList<Object>) updateHandler2.get("stats");
             return ((Long) stats.get("docsPending")).intValue();
         } catch (SolrServerException e) {
             logger.warn("Couldn't obtain the number of pending documents", e);
             return 0;
         } catch (IOException e) {
             logger.error("Can't contact the search server", e);
             return 0;
         }
     }
 
     public void setSearchIndexBuilder(SearchIndexBuilder searchIndexBuilder) {
         this.searchIndexBuilder = searchIndexBuilder;
     }
 
     public void setSiteService(SiteService siteService) {
         this.siteService = siteService;
     }
 
     public void setContentProducerFactory(ContentProducerFactory contentProducerFactory) {
         this.contentProducerFactory = contentProducerFactory;
     }
 
     public void setSolrServer(SolrServer solrServer) {
         this.solrServer = solrServer;
     }
 }
