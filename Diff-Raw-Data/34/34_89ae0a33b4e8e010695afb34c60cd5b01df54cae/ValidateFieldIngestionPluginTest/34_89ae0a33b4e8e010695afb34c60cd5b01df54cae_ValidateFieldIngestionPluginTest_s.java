 /* LinkcheckIngestionPluginTest.java - created on Mar 20, 2011, Copyright (c) 2011 The European Library, all rights reserved */
 package org.theeuropeanlibrary.uim.check.validate;
 
 import static org.junit.Assert.assertEquals;
 import static org.mockito.Matchers.any;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 import java.io.IOException;
 import java.util.Properties;
 
 import org.junit.Test;
 import org.theeuropeanlibrary.model.common.Link;
 import org.theeuropeanlibrary.model.common.Title;
 import org.theeuropeanlibrary.model.common.qualifier.LinkTarget;
 import org.theeuropeanlibrary.model.tel.ObjectModelRegistry;
 import org.theeuropeanlibrary.model.tel.qualifier.Maturity;
 
 import eu.europeana.uim.api.ActiveExecution;
 import eu.europeana.uim.api.LoggingEngine;
 import eu.europeana.uim.api.LoggingEngineAdapter;
 import eu.europeana.uim.common.TKey;
 import eu.europeana.uim.store.MetaDataRecord;
 import eu.europeana.uim.store.bean.CollectionBean;
 import eu.europeana.uim.store.bean.ExecutionBean;
 import eu.europeana.uim.store.bean.MetaDataRecordBean;
 
 /**
  * Tests the field validation plugin.
  * 
  * @author Andreas Juffinger (andreas.juffinger@kb.nl)
  * @since Mar 20, 2011
  */
 public class ValidateFieldIngestionPluginTest {
     /**
      * Tests a simple runthrough of the field validation plugin against artificial records.
      * 
      * @throws IOException
      */
     @SuppressWarnings({ "unchecked", "rawtypes" })
     @Test
     public void testSimpleValidation() throws IOException {
         FieldCheckIngestionPlugin plugin = new FieldCheckIngestionPlugin();
         plugin.initialize();
 
         CollectionBean collection = new CollectionBean();
         collection.setName("Validation Test");
         ExecutionBean execution = new ExecutionBean(1L);
         execution.setDataSet(collection);
 
         Properties properties = new Properties();
         properties.put(FieldCheckIngestionPlugin.KEY_FIELDSPEC,
                 "./src/test/resources/fieldspec.txt");
 
         LoggingEngine logging = LoggingEngineAdapter.LONG;
 
         ActiveExecution<Long> context = mock(ActiveExecution.class);
         when(context.getProperties()).thenReturn(properties);
         when(context.getExecution()).thenReturn(execution);
         when(context.getLoggingEngine()).thenReturn(logging);
 
         FieldCheckIngestionPlugin.Data data = new FieldCheckIngestionPlugin.Data();
         when(context.getValue((TKey<?, FieldCheckIngestionPlugin.Data>)any())).thenReturn(data);
 
         MetaDataRecord mdr = new MetaDataRecordBean(0l, collection);
         mdr.addValue(ObjectModelRegistry.TITLE, new Title("Validation Test"));
         Enum<?>[] linkTypeQualifier = new Enum<?>[] { LinkTarget.TABLE_OF_CONTENTS };
         mdr.addValue(
                 ObjectModelRegistry.LINK,
                 new Link(
                         "http://www.theeuropeanlibrary.org/exhibition-reading-europe/detail.html?id=103043"),
                 linkTypeQualifier);
         mdr.addValue(
                 ObjectModelRegistry.LINK,
                 new Link(
                         "http://www.theeuropeanlibrary.org/exhibition-reading-europe/detail.html?id=96805"),
                 linkTypeQualifier);
 
         plugin.initialize(context);
         plugin.processRecord(mdr, context);
 
        assertEquals(Maturity.ACCEPT, mdr.getFirstValue(ObjectModelRegistry.MATURITY));
 
         plugin.completed(context);
 
         plugin.shutdown();
     }
 }
