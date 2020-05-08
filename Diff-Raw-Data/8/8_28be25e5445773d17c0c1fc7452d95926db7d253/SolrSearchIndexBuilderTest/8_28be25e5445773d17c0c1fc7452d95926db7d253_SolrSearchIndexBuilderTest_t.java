 package org.sakaiproject.search.solr;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Mock;
 import org.mockito.MockitoAnnotations;
 import org.sakaiproject.event.api.Event;
 import org.sakaiproject.search.indexing.DefaultTask;
 import org.sakaiproject.search.indexing.Task;
 import org.sakaiproject.search.indexing.TaskMatcher;
 import org.sakaiproject.search.producer.ContentProducerFactory;
 import org.sakaiproject.search.producer.ProducerBuilder;
 import org.sakaiproject.search.queueing.IndexQueueing;
 
 import java.util.Date;
 
 import static org.mockito.Mockito.*;
 
 /**
  * @author Colin Hebert
  */
 public class SolrSearchIndexBuilderTest {
     private SolrSearchIndexBuilder solrSearchIndexBuilder;
     @Mock
     private IndexQueueing mockIndexQueueing;
     private ContentProducerFactory contentProducerFactory;
 
     @Before
     public void setUp() throws Exception {
         MockitoAnnotations.initMocks(this);
         solrSearchIndexBuilder = new SolrSearchIndexBuilder();
         solrSearchIndexBuilder.setIndexQueueing(mockIndexQueueing);
         contentProducerFactory = new ContentProducerFactory();
         solrSearchIndexBuilder.setContentProducerFactory(contentProducerFactory);
     }
 
     @Test
     public void testAddResource() throws Exception {
         Date eventTime = new Date();
         String reference = "reference";
         String eventType = "eventType";
         Event event = mock(Event.class);
         when(event.getEventTime()).thenReturn(eventTime);
         when(event.getResource()).thenReturn(reference);
         when(event.getEvent()).thenReturn(eventType);
         ProducerBuilder producerBuilder = ProducerBuilder.create().addDoc(reference)
                 .addEvent(eventType, ProducerBuilder.ActionType.ADD);
         contentProducerFactory.addContentProducer(producerBuilder.build());
 
         solrSearchIndexBuilder.addResource(null, event);
 
         verify(mockIndexQueueing).addTaskToQueue(
                 argThat(new TaskMatcher(DefaultTask.Type.INDEX_DOCUMENT.getTypeName())));
 
     }
 
     @Test
    public void testUnsupportedEventResource() {
         Date eventTime = new Date();
         String reference = "reference";
         String eventType = "eventType";
         Event event = mock(Event.class);
         when(event.getEventTime()).thenReturn(eventTime);
         when(event.getResource()).thenReturn(reference);
         when(event.getEvent()).thenReturn(eventType);
         ProducerBuilder producerBuilder = ProducerBuilder.create();
         contentProducerFactory.addContentProducer(producerBuilder.build());
 
         solrSearchIndexBuilder.addResource(null, event);
 
         verify(mockIndexQueueing, never()).addTaskToQueue(any(Task.class));
     }
 
     @Test
     public void testRemoveResource() throws Exception {
         Date eventTime = new Date();
         String reference = "reference";
         String eventType = "eventType";
         Event event = mock(Event.class);
         when(event.getEventTime()).thenReturn(eventTime);
         when(event.getResource()).thenReturn(reference);
         when(event.getEvent()).thenReturn(eventType);
         ProducerBuilder producerBuilder = ProducerBuilder.create().addDoc(reference)
                 .addEvent(eventType, ProducerBuilder.ActionType.DELETE);
         contentProducerFactory.addContentProducer(producerBuilder.build());
 
         solrSearchIndexBuilder.addResource(null, event);
 
         verify(mockIndexQueueing).addTaskToQueue(
                 argThat(new TaskMatcher(DefaultTask.Type.REMOVE_DOCUMENT.getTypeName())));
     }
 
     @Test
     public void testRebuildSite() throws Exception {
         String siteId = "siteid";
 
         solrSearchIndexBuilder.rebuildIndex(siteId);
 
         verify(mockIndexQueueing).addTaskToQueue(
                 argThat(new TaskMatcher(DefaultTask.Type.INDEX_SITE.getTypeName())));
     }
 
     @Test
     public void testRefreshSite() throws Exception {
         String siteId = "siteid";
 
         solrSearchIndexBuilder.refreshIndex(siteId);
 
         verify(mockIndexQueueing).addTaskToQueue(
                 argThat(new TaskMatcher(DefaultTask.Type.REFRESH_SITE.getTypeName())));
     }
 
     @Test
     public void testRebuildIndex() throws Exception {
         solrSearchIndexBuilder.rebuildIndex();
 
         verify(mockIndexQueueing).addTaskToQueue(
                 argThat(new TaskMatcher(DefaultTask.Type.INDEX_ALL.getTypeName())));
     }
 
     @Test
     public void testRefreshIndex() throws Exception {
         solrSearchIndexBuilder.refreshIndex();
 
         verify(mockIndexQueueing).addTaskToQueue(
                 argThat(new TaskMatcher(DefaultTask.Type.REFRESH_ALL.getTypeName())));
     }
 }
