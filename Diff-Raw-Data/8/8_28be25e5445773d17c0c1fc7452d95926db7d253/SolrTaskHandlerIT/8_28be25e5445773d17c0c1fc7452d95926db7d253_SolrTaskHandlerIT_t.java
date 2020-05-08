 package org.sakaiproject.search.solr.indexing;
 
 import org.apache.solr.client.solrj.SolrQuery;
 import org.apache.solr.client.solrj.SolrServer;
 import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
 import org.apache.solr.client.solrj.response.QueryResponse;
 import org.apache.solr.client.solrj.util.ClientUtils;
 import org.apache.solr.common.SolrDocument;
 import org.apache.solr.common.SolrDocumentList;
 import org.apache.solr.util.AbstractSolrTestCase;
 import org.hamcrest.CoreMatchers;
 import org.joda.time.DateTime;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.sakaiproject.search.api.EntityContentProducer;
 import org.sakaiproject.search.api.SearchService;
 import org.sakaiproject.search.producer.ContentProducerFactory;
 import org.sakaiproject.search.producer.ProducerBuilder;
 import org.sakaiproject.thread_local.api.ThreadLocalManager;
 
 import java.util.Date;
 
 import static org.hamcrest.CoreMatchers.equalTo;
 import static org.hamcrest.CoreMatchers.is;
 import static org.mockito.Mockito.mock;
 
 /**
  * Integration test with Solr.
  *
  * @author Colin Hebert
  */
 @org.apache.lucene.util.LuceneTestCase.SuppressCodecs({"Lucene3x", "Lucene40"})
 public class SolrTaskHandlerIT extends AbstractSolrTestCase {
     public static final Date DATE_1 = new DateTime(2013, 3, 10, 16, 0, 0).toDate();
     public static final Date DATE_2 = new DateTime(2013, 3, 10, 17, 0, 0).toDate();
     private ContentProducerFactory contentProducerFactory;
     private SolrServer solrServer;
     private SolrTaskHandler solrTaskHandler;
 
     @BeforeClass
     public static void beforeClass() throws Exception {
         initCore("org/sakaiproject/search/solr/conf/search/conf/solrconfig.xml",
                 "org/sakaiproject/search/solr/conf/search/conf/schema.xml",
                 "org/sakaiproject/search/solr/conf/search/");
     }
 
     @Before
     @Override
     public void setUp() throws Exception {
         super.setUp();
         solrServer = new EmbeddedSolrServer(h.getCoreContainer(), h.getCore().getName());
         clearIndex();
         assertIndexIsEmpty();
 
         solrTaskHandler = new SolrTaskHandler();
         solrTaskHandler.setSolrServer(solrServer);
         SolrTools solrTools = new SolrTools();
         solrTaskHandler.setSolrTools(solrTools);
         solrTools.setSolrServer(solrServer);
         contentProducerFactory = new ContentProducerFactory();
         solrTools.setContentProducerFactory(contentProducerFactory);
         solrTaskHandler.setThreadLocalManager(mock(ThreadLocalManager.class));
     }
 
     /**
      * Attempts to add a new document to the index.
      * <p>
      * Checks that only one document is available (the index is empty to begin with).<br />
      * Checks that the document is the one created (same properties, same creation date).
      * </p>
      *
      * @throws Exception any exception.
      */
     @Test
     public void testIndexDocument() throws Exception {
         String reference = "testIndexDocument";
         // Add a producer for 'reference'
         contentProducerFactory.addContentProducer(ProducerBuilder.create().addDoc(reference).build());
 
         solrTaskHandler.indexDocument(reference, DATE_1);
 
         SolrDocumentList results = getSolrDocuments();
         // A new documents has been created
         assertThat(results.getNumFound(), is(1L));
         // The document matches the input
         SolrDocument document = results.get(0);
         assertThat(document.getFieldValue(SearchService.FIELD_REFERENCE), CoreMatchers.<Object>equalTo(reference));
         assertDocumentMatches(document, DATE_1);
     }
 
     /**
      * Attempts to add an outdated document to the index.
      * <p>
      * Checks that a same document can't be twice in the index.<br />
      * Checks that an outdated document doesn't overwrite a newer version of that document.<br />
      * Checks that outdated documents fail silently.
      * </p>
      *
      * @throws Exception any exception.
      */
     @Test
     public void testIndexDocumentOutdatedFails() throws Exception {
         String reference = "testIndexDocument";
         ProducerBuilder contentProducerBuilder = ProducerBuilder.create().addDoc(reference);
         contentProducerFactory.addContentProducer(contentProducerBuilder.build());
         addDocumentToIndex(reference, DATE_2);
 
         solrTaskHandler.indexDocument(reference, DATE_1);
 
         SolrDocumentList results = getSolrDocuments();
         assertThat(results.getNumFound(), is(1L));
 
         SolrDocument document = results.get(0);
         assertThat(document.getFieldValue(SearchService.FIELD_REFERENCE), CoreMatchers.<Object>equalTo(reference));
         assertDocumentMatches(document, DATE_2);
     }
 
     /**
      * Attempts to remove a document from the index.
      * <p>
      * Checks that the removal of the only document results in an empty index.
      * </p>
      *
      * @throws Exception any exception.
      */
     @Test
     public void testRemoveDocument() throws Exception {
         String reference = "testRemoveDocument";
         ProducerBuilder contentProducerBuilder = ProducerBuilder.create().addDoc(reference);
         contentProducerFactory.addContentProducer(contentProducerBuilder.build());
         addDocumentToIndex(reference, DATE_1);
 
         solrTaskHandler.removeDocument(reference, DATE_2);
 
         assertIndexIsEmpty();
     }
 
     /**
      * Attempts to remove a document from the index when a newer version of the document is present.
      * <p>
      * Checks that a newer document isn't removed when the removal date is before the document indexation date.<br />
      * Checks that outdated removal fail silently.
      * </p>
      *
      * @throws Exception any exception.
      */
     @Test
     public void testRemoveDocumentOutdatedFails() throws Exception {
         String reference = "testRemoveDocument";
         ProducerBuilder contentProducerBuilder = ProducerBuilder.create().addDoc(reference);
         contentProducerFactory.addContentProducer(contentProducerBuilder.build());
         addDocumentToIndex(reference, DATE_2);
 
         solrTaskHandler.removeDocument(reference, DATE_1);
 
         assertThat(getSolrDocuments().getNumFound(), is(1L));
     }
 
     /**
      * Attempts to index a site containing multiple documents.
      * <p>
      * Checks that the expected number of documents were indexed.
      * Checks that the expected documents are available.
      * </p>
      *
      * @throws Exception any exception.
      */
     @Test
     public void testIndexSiteCreatesRightNumberOfDocuments() throws Exception {
         String siteId = "indexSiteId";
         int numberOfDocs = 7;
         ProducerBuilder contentProducerBuilder = ProducerBuilder.create()
                 .addDocsToSite(siteId, numberOfDocs);
         contentProducerFactory.addContentProducer(contentProducerBuilder.build());
 
         solrTaskHandler.indexSite(siteId, DATE_1);
 
         assertThat(getSolrDocuments().getNumFound(), is((long) numberOfDocs));
         assertSiteDocumentsMatches(siteId, DATE_1);
     }
 
     /**
      * Attempts to index an already indexed site containing multiple documents (and had some documents removed).
      * <p>
      * Checks that the expected number of documents were indexed.<br />
      * Checks that the old/nonexistant documents were removed.<br />
      * </p>
      *
      * @throws Exception any exception.
      */
     @Test
     public void testIndexSiteRemovesOldDocuments() throws Exception {
         String siteId = "indexSiteId";
         int numberOfOldDocs = 7;
         int numberOfNewDocs = 3;
         ProducerBuilder contentProducerBuilder = ProducerBuilder.create().addDocsToSite(siteId, numberOfOldDocs);
         contentProducerFactory.addContentProducer(contentProducerBuilder.build());
         addSiteToIndex(siteId, DATE_1);
         contentProducerBuilder.emptySite(siteId)
                 .addDocsToSite(siteId, numberOfNewDocs);
 
         solrTaskHandler.indexSite(siteId, DATE_2);
 
         assertThat(getSolrDocuments().getNumFound(), is((long) numberOfNewDocs));
         assertSiteDocumentsMatches(siteId, DATE_2);
     }
 
     /**
      * Attempts to refresh a site.
      * <p>
      * Check that the existing documents are indeed refreshed.
      * </p>
      *
      * @throws Exception
      */
     @Test
     public void testRefreshSite() throws Exception {
         String siteId = "refreshSiteId";
         int numberOfDocs = 1;
         ProducerBuilder contentProducerBuilder = ProducerBuilder.create()
                 .addDocsToSite(siteId, numberOfDocs);
         contentProducerFactory.addContentProducer(contentProducerBuilder.build());
         addSiteToIndex(siteId, DATE_1);
         //Load the existing doc and change it, before re-adding it.
         ProducerBuilder.Document doc = contentProducerBuilder.getDocs().iterator().next();
         doc.setTitle("newTitle");
         contentProducerBuilder.addDoc(doc);
 
         solrTaskHandler.refreshSite(siteId, DATE_2);
 
         assertThat(getSolrDocuments().getNumFound(), is((long) numberOfDocs));
         assertSiteDocumentsMatches(siteId, DATE_2);
     }
 
     /**
      * Attempts to refresh a site that didn't have documents in the first place.
      * <p>
      * Check that no documents were added even though there are new documents in the site.
      * </p>
      *
      * @throws Exception
      */
     @Test
     public void testRefreshSiteDoesntCreateDocuments() throws Exception {
         String siteId = "refreshSiteId";
         int numberOfDocs = 7;
         ProducerBuilder contentProducerBuilder = ProducerBuilder.create()
                 .addDocsToSite(siteId, numberOfDocs);
         contentProducerFactory.addContentProducer(contentProducerBuilder.build());
 
         solrTaskHandler.refreshSite(siteId, DATE_1);
 
         assertIndexIsEmpty();
     }
 
     /**
      * Attempts to refresh a site that has deprecated documents.
      * <p>
      * Check that the now deprecated documents are removed.
      * </p>
      *
      * @throws Exception
      */
     @Test
     public void testRefreshSiteRemovesDocuments() throws Exception {
         String siteId = "refreshSiteId";
         int numberOfDocs = 7;
         ProducerBuilder contentProducerBuilder = ProducerBuilder.create()
                 .addDocsToSite(siteId, numberOfDocs);
         contentProducerFactory.addContentProducer(contentProducerBuilder.build());
         addSiteToIndex(siteId, DATE_1);
         contentProducerBuilder.emptySite(siteId);
 
         solrTaskHandler.refreshSite(siteId, DATE_2);
 
         assertIndexIsEmpty();
     }
 
     private void assertIndexIsEmpty() throws Exception {
         assertThat(getSolrDocuments().getNumFound(), is(0L));
     }
 
     private void assertSiteDocumentsMatches(String siteId, Date actionDate) throws Exception {
         QueryResponse response = solrServer.query(
                 new SolrQuery(SearchService.FIELD_SITEID + ":" + ClientUtils.escapeQueryChars(siteId)));
 
         for (SolrDocument document : response.getResults()) {
             assertDocumentMatches(document, actionDate);
         }
     }
 
     private void assertDocumentMatches(SolrDocument document, Date actionDate) {
         assertDocumentMatches(document);
         assertThat((Date) document.getFieldValue(SearchService.DATE_STAMP), equalTo(actionDate));
     }
 
     private void assertDocumentMatches(SolrDocument document) {
         String reference = (String) document.getFieldValue(SearchService.FIELD_REFERENCE);
         EntityContentProducer contentProducer = contentProducerFactory.getContentProducerForElement(reference);
 
         assertThat(document.getFieldValue(SearchService.FIELD_CONTAINER),
                 CoreMatchers.<Object>equalTo(contentProducer.getContainer(reference)));
         assertThat(document.getFieldValue(SearchService.FIELD_TYPE),
                 CoreMatchers.<Object>equalTo(contentProducer.getType(reference)));
         assertThat(document.getFieldValue(SearchService.FIELD_TITLE),
                 CoreMatchers.<Object>equalTo(contentProducer.getTitle(reference)));
         assertThat(document.getFieldValue(SearchService.FIELD_TOOL),
                 CoreMatchers.<Object>equalTo(contentProducer.getTool()));
         assertThat(document.getFieldValue(SearchService.FIELD_URL),
                 CoreMatchers.<Object>equalTo(contentProducer.getUrl(reference)));
         assertThat(document.getFieldValue(SearchService.FIELD_SITEID),
                 CoreMatchers.<Object>equalTo(contentProducer.getSiteId(reference)));
     }
 
     private void addDocumentToIndex(String reference, Date indexationDate) throws Exception {
         solrTaskHandler.indexDocument(reference, indexationDate);
         solrServer.commit();
     }
 
     private void addSiteToIndex(String siteId, Date indexationDate) throws Exception {
         solrTaskHandler.indexSite(siteId, indexationDate);
         solrServer.commit();
     }
 
     private SolrDocumentList getSolrDocuments() throws Exception {
         solrServer.commit();
         SolrQuery query = new SolrQuery("*:*");
         QueryResponse qr = solrServer.query(query);
         return qr.getResults();
     }
 }
