 package org.jcouchdb.db;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.greaterThan;
 import static org.hamcrest.Matchers.is;
 import static org.hamcrest.Matchers.notNullValue;
 import static org.hamcrest.Matchers.nullValue;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.log4j.Logger;
 import org.jcouchdb.document.Attachment;
 import org.jcouchdb.document.BaseDocument;
 import org.jcouchdb.document.DesignDocument;
 import org.jcouchdb.document.Document;
 import org.jcouchdb.document.DocumentInfo;
 import org.jcouchdb.document.ValueAndDocumentRow;
 import org.jcouchdb.document.ValueRow;
 import org.jcouchdb.document.View;
 import org.jcouchdb.document.ViewAndDocumentsResult;
 import org.jcouchdb.document.ViewResult;
 import org.jcouchdb.exception.DataAccessException;
import org.jcouchdb.exception.DocumentValidationException;
 import org.jcouchdb.exception.NotFoundException;
 import org.jcouchdb.exception.UpdateConflictException;
import org.junit.Ignore;
 import org.junit.Test;
 import org.svenson.JSON;
 
 /**
  * Runs tests against a real couchdb database running on localhost
  *
  * @author shelmberger
  */
 public class LocalDatabaseTestCase
 {
     private JSON jsonGenerator = new JSON();
 
     private final static String COUCHDB_HOST = "localhost";
 
     private final static int COUCHDB_PORT = Server.DEFAULT_PORT;
 
     private static final String TESTDB_NAME = "jcouchdb_test";
 
     private static final String MY_FOO_DOC_ID = "myFoo/DocId";
 
     private static final String BY_VALUE_FUNCTION = "function(doc) { if (doc.type == 'foo') { emit(doc.value,doc); }  }";
 
     private static final String COMPLEX_KEY_FUNCTION = "function(doc) { if (doc.type == 'foo') { emit([1,{\"value\":doc.value}],doc); }  }";
 
     protected static Logger log = Logger.getLogger(LocalDatabaseTestCase.class);
 
     public static Database createDatabaseForTest()
     {
         Server server = new ServerImpl(COUCHDB_HOST, COUCHDB_PORT);
         List<String> databases = server.listDatabases();
 
         log.debug("databases = " + databases);
 
         if (!databases.contains(TESTDB_NAME))
         {
             server.createDatabase(TESTDB_NAME);
         }
 
         return new Database(server,TESTDB_NAME);
     }
 
     @Test
     public void recreateTestDatabase()
     {
         try
         {
             Server server = new ServerImpl(COUCHDB_HOST, COUCHDB_PORT);
             List<String> databases = server.listDatabases();
 
             log.debug("databases = " + databases);
 
             if (databases.contains(TESTDB_NAME))
             {
                 server.deleteDatabase(TESTDB_NAME);
             }
 
             server.createDatabase(TESTDB_NAME);
 
         }
         catch (RuntimeException e)
         {
             log.error("", e);
         }
     }
 
     @Test
     public void createTestDocuments()
     {
         Database db = createDatabaseForTest();
 
         FooDocument foo = new FooDocument("bar!");
 
         assertThat(foo.getId(), is(nullValue()));
         assertThat(foo.getRevision(), is(nullValue()));
 
         db.createDocument(foo);
 
         assertThat(foo.getId(), is(notNullValue()));
         assertThat(foo.getRevision(), is(notNullValue()));
 
         foo = new FooDocument("baz!");
         foo.setProperty("baz2", "Some test value");
 
         db.createDocument(foo);
 
         log.debug("-- resetted database ----------------------------------");
     }
 
     @Test
     public void thatMapDocumentsWork()
     {
         Database db = createDatabaseForTest();
 
         Map<String,String> doc = new HashMap<String, String>();
 
         doc.put("foo", "value for the foo attribute");
         doc.put("bar", "value for the bar attribute");
 
         db.createDocument(doc);
 
         final String id = doc.get("_id");
         assertThat(id, is(notNullValue()));
         assertThat(doc.get("_rev"), is(notNullValue()));
 
         doc = db.getDocument(Map.class, id);
 
         assertThat(doc.get("foo"), is("value for the foo attribute"));
         assertThat(doc.get("bar"), is("value for the bar attribute"));
 
     }
 
     @Test
     public void thatCreateNamedDocWorks()
     {
         FooDocument doc = new FooDocument("qux");
         doc.setId(MY_FOO_DOC_ID);
 
         Database db = createDatabaseForTest();
         db.createDocument(doc);
         assertThat(doc.getId(), is(MY_FOO_DOC_ID));
         assertThat(doc.getRevision(), is(notNullValue()));
 
     }
 
     @Test
     public void thatUpdateDocWorks()
     {
         Database db = createDatabaseForTest();
 
         FooDocument doc = db.getDocument(FooDocument.class, MY_FOO_DOC_ID);
         assertThat(doc.getValue(), is("qux"));
 
         doc.setValue("qux!");
         db.updateDocument(doc);
 
         doc = db.getDocument(FooDocument.class, MY_FOO_DOC_ID);
         assertThat(doc.getValue(), is("qux!"));
     }
 
     
     @Test(expected = UpdateConflictException.class)
     public void thatUpdateConflictWorks()
     {
         FooDocument doc = new FooDocument("qux");
         doc.setId(MY_FOO_DOC_ID);
 
         new Database(COUCHDB_HOST, COUCHDB_PORT, TESTDB_NAME).createDocument(doc);
     }
 
     @Test
     public void testGetAll()
     {
         Database db = createDatabaseForTest();
 
         ViewResult<Map> result = db.listDocuments(null,null);
 
         List<ValueRow<Map>> rows = result.getRows();
 
         String json = jsonGenerator.forValue(rows);
         System.out.println("rows = " + json);
 
         assertThat(rows.size(), is(4));
 
     }
 
     @Test
     public void testGetAllBySeq()
     {
         Database db = createDatabaseForTest();
 
         ViewResult<Map> result = db.listDocumentsByUpdateSequence(null,null);
 
         List<ValueRow<Map>> rows = result.getRows();
 
         assertThat(rows.size(), is(4));
         assertThat(rows.get(0).getKey().toString(), is("1"));
         assertThat(rows.get(1).getKey().toString(), is("2"));
         assertThat(rows.get(2).getKey().toString(), is("3"));
         assertThat(rows.get(3).getKey().toString(), is("5"));   // this one was updated once
 
         String json = jsonGenerator.forValue(rows);
         log.debug("rows = " + json);
     }
 
     @Test
     public void testCreateDesignDocument()
     {
         Database db = createDatabaseForTest();
 
         DesignDocument designDocument = new DesignDocument("foo");
         designDocument.addView("byValue", new View(BY_VALUE_FUNCTION));
         designDocument.addView("complex", new View(COMPLEX_KEY_FUNCTION));
         log.debug("DESIGN DOC = " + jsonGenerator.dumpObjectFormatted(designDocument));
 
         db.createDocument(designDocument);
     }
 
     @Test
     public void getDesignDocument()
     {
         Database db = createDatabaseForTest();
         DesignDocument doc = db.getDesignDocument("foo");
         log.debug(jsonGenerator.dumpObjectFormatted(doc));
         assertThat(doc, is(notNullValue()));
         assertThat(doc.getId(), is(DesignDocument.PREFIX + "foo"));
         assertThat(doc.getViews().get("byValue").getMap(), is(BY_VALUE_FUNCTION));
     }
 
     @Test
     public void queryDocuments()
     {
         Database db = createDatabaseForTest();
         ViewResult<FooDocument> result = db.queryView("foo/byValue", FooDocument.class, null, null);
 
         assertThat(result.getRows().size(), is(3));
 
         FooDocument doc = result.getRows().get(0).getValue();
         assertThat(doc, is(notNullValue()));
         assertThat(doc.getValue(), is("bar!"));
 
         doc = result.getRows().get(1).getValue();
         assertThat(doc, is(notNullValue()));
         assertThat(doc.getValue(), is("baz!"));
 
         doc = result.getRows().get(2).getValue();
         assertThat(doc, is(notNullValue()));
         assertThat(doc.getId(), is(MY_FOO_DOC_ID));
         assertThat(doc.getValue(), is("qux!"));
 
     }
 
     @Test
     public void queryViewAndDocuments()
     {
         Database db = createDatabaseForTest();
         ViewAndDocumentsResult<Object,FooDocument> result = db.queryViewAndDocuments("foo/byValue", Object.class, FooDocument.class, null, null);
 
         assertThat(result.getRows().size(), is(3));
 
         FooDocument doc = result.getRows().get(0).getDocument();
         assertThat(doc, is(notNullValue()));
         assertThat(doc.getValue(), is("bar!"));
 
         doc = result.getRows().get(1).getDocument();
         assertThat(doc, is(notNullValue()));
         assertThat(doc.getValue(), is("baz!"));
 
         doc = result.getRows().get(2).getDocument();
         assertThat(doc, is(notNullValue()));
         assertThat(doc.getId(), is(MY_FOO_DOC_ID));
         assertThat(doc.getValue(), is("qux!"));
 
     }
 
     @Test
     public void queryDocumentsWithComplexKey()
     {
         Database db = createDatabaseForTest();
         ViewResult<FooDocument> result = db.queryView("foo/complex", FooDocument.class, null, null);
 
         assertThat(result.getRows().size(), is(3));
 
         ValueRow<FooDocument> row = result.getRows().get(0);
         assertThat(jsonGenerator.forValue(row.getKey()), is("[1,{\"value\":\"bar!\"}]"));
 
     }
 
     @Test
     public void thatGetDocumentWorks()
     {
         Database db = createDatabaseForTest();
         FooDocument doc = db.getDocument(FooDocument.class, MY_FOO_DOC_ID);
         assertThat(doc.getId(), is(MY_FOO_DOC_ID));
         assertThat(doc.getRevision(), is(notNullValue()));
         assertThat(doc.getValue(), is("qux!"));
 
         log.debug(jsonGenerator.dumpObjectFormatted(doc));
 
     }
 
     @Test
     public void thatAdHocViewsWork()
     {
         Database db = createDatabaseForTest();
         ViewResult<FooDocument>  result = db.queryAdHocView(FooDocument.class, "{ \"map\" : \"function(doc) { if (doc.baz2 == 'Some test value') emit(null,doc);  } \" }", null, null);
 
         assertThat(result.getRows().size(), is(1));
 
         FooDocument doc = result.getRows().get(0).getValue();
         assertThat((String)doc.getProperty("baz2"), is("Some test value"));
     }
 
     @Test
     public void thatNonDocumentFetchingWorks()
     {
         Database db = createDatabaseForTest();
         NotADocument doc = db.getDocument(NotADocument.class, MY_FOO_DOC_ID);
         assertThat(doc.getId(), is(MY_FOO_DOC_ID));
         assertThat(doc.getRevision(), is(notNullValue()));
         assertThat((String)doc.getProperty("value"), is("qux!"));
 
         log.debug(jsonGenerator.dumpObjectFormatted(doc));
 
         doc.setProperty("value", "changed");
 
         db.updateDocument(doc);
 
         NotADocument doc2 = db.getDocument(NotADocument.class, MY_FOO_DOC_ID);
         assertThat((String)doc2.getProperty("value"), is("changed"));
 
     }
 
 
     @Test
     public void thatBulkCreationWorks()
     {
         Database db = createDatabaseForTest();
 
         List<Document> docs = new ArrayList<Document>();
 
         docs.add(new FooDocument("doc-1"));
         docs.add(new FooDocument("doc-2"));
         docs.add(new FooDocument("doc-3"));
         List<DocumentInfo> infos = db.bulkCreateDocuments(docs);
 
         assertThat(infos.size(), is(3));
 
     }
 
     @Test
     public void thatBulkCreationWithIdsWorks()
     {
         Database db = createDatabaseForTest();
 
         List<Document> docs = new ArrayList<Document>();
 
         FooDocument fooDocument = new FooDocument("doc-2");
         fooDocument.setId("second-foo-with-id");
         docs.add(new FooDocument("doc-1"));
 
         docs.add(fooDocument);
         FooDocument fd2 = new FooDocument("doc-3");
         fd2.setId(MY_FOO_DOC_ID);
         docs.add(fd2);
 
         List<DocumentInfo> infos = db.bulkCreateDocuments(docs);
 
         assertThat(infos.size(), is(3));
 
         assertThat(infos.get(0).getId().length(), is(greaterThan(0)));
         assertThat(infos.get(1).getId(), is("second-foo-with-id"));
         
         // conflict results in error and reason being set
         assertThat(infos.get(2).getError().length(), is(greaterThan(0)));
         assertThat(infos.get(2).getReason().length(), is(greaterThan(0)));
 
     }
 
     @Test(expected=UpdateConflictException.class)
     public void thatUpdateConflictsWork()
     {
         FooDocument foo = new FooDocument("value foo");
         FooDocument foo2 = new FooDocument("value foo2");
         foo.setId("update_conflict");
         foo2.setId("update_conflict");
 
         Database db = createDatabaseForTest();
         db.createDocument(foo);
         db.createDocument(foo2);
 
     }
 
     @Test
     public void thatDeleteWorks()
     {
         FooDocument foo = new FooDocument("a document");
         Database db = createDatabaseForTest();
         db.createDocument(foo);
 
         assertThat(foo.getId(), is ( notNullValue()));
 
         FooDocument foo2 = db.getDocument(FooDocument.class, foo.getId());
 
         assertThat(foo.getValue(), is(foo2.getValue()));
 
         db.delete(foo);
 
         try
         {
             db.getDocument(FooDocument.class, foo.getId());
             throw new IllegalStateException("document shouldn't be there anymore");
         }
         catch(NotFoundException nfe)
         {
             // yay!
         }
     }
 
     @Test(expected = DataAccessException.class)
     public void thatDeleteFailsIfWrong()
     {
         Database db = createDatabaseForTest();
         db.delete("fakeid", "fakrev");
     }
 
     private int valueCount(ViewResult<FooDocument> viewResult, String value)
     {
         int cnt = 0;
         for (ValueRow<FooDocument> row : viewResult.getRows())
         {
             if (row.getValue().getValue().equals(value))
             {
                 cnt++;
             }
         }
         return cnt;
     }
 
 
 
     @Test
     public void thatAttachmentHandlingWorks() throws UnsupportedEncodingException
     {
         final String attachmentContent = "The quick brown fox jumps over the lazy dog.";
         
         FooDocument fooDocument = new FooDocument("foo with attachment");
         fooDocument.addAttachment("test", new Attachment("text/plain", attachmentContent.getBytes()));
 
         Database db = createDatabaseForTest();
         db.createDocument(fooDocument);
 
         String id = fooDocument.getId();
         // re-read document
         fooDocument = db.getDocument(FooDocument.class, id);
 
         Attachment attachment = fooDocument.getAttachments().get("test");
         assertThat(attachment, is(notNullValue()));
         assertThat(attachment.isStub(), is(true));
         assertThat(attachment.getContentType(), is("text/plain"));
         assertThat(attachment.getLength(), is(44l));
 
         String content = new String(db.getAttachment(id, "test"));
         assertThat(content, is(attachmentContent));
 
         String newRev = db.updateAttachment(fooDocument.getId(), fooDocument.getRevision(), "test", "text/plain", (attachmentContent+"!!").getBytes());
         assertThat(newRev, is(notNullValue()));
         assertThat(newRev.length(), is(greaterThan(0)));
 
         content = new String(db.getAttachment(id, "test"));
         assertThat(content, is(attachmentContent+"!!"));
 
         newRev = db.deleteAttachment(fooDocument.getId(), newRev, "test");
 
         assertThat(newRev, is(notNullValue()));
         assertThat(newRev.length(), is(greaterThan(0)));
 
         try
         {
             content = new String(db.getAttachment(id, "test"));
             throw new IllegalStateException("attachment should be gone by now");
         }
         catch(NotFoundException e)
         {
             // yay!
         }
 
 
         newRev = db.createAttachment(fooDocument.getId(), newRev, "test", "text/plain", "TEST".getBytes());
 
         assertThat(newRev, is(notNullValue()));
         assertThat(newRev.length(), is(greaterThan(0)));
 
         content = new String(db.getAttachment(id, "test"));
         assertThat(content, is("TEST"));
     }
 
 
     @Test
     public void thatViewKeyQueryingFromAllDocsWorks()
     {
         Database db = createDatabaseForTest();
         ViewResult<Map> result = db.queryByKeys(Map.class, Arrays.asList(MY_FOO_DOC_ID,"second-foo-with-id"), null, null);
         assertThat(result.getRows().size(), is(2));
         assertThat(result.getRows().get(0).getId(), is(MY_FOO_DOC_ID));
         assertThat(result.getRows().get(1).getId(), is("second-foo-with-id"));
     }
 
 
     @Test
     public void thatViewKeyQueryingFromAllDocsWorks2()
     {
         Database db = createDatabaseForTest();
         ViewResult<Map> result = db.queryByKeys(Map.class, Arrays.asList(MY_FOO_DOC_ID,"second-foo-with-id"), null, null);
         assertThat(result.getRows().size(), is(2));
         assertThat(result.getRows().get(0).getId(), is(MY_FOO_DOC_ID));
         assertThat(result.getRows().get(1).getId(), is("second-foo-with-id"));
     }
 
     @Test
     public void thatViewKeyQueryingWorks()
     {
         Database db = createDatabaseForTest();
         ViewResult<FooDocument> result = db.queryViewByKeys("foo/byValue", FooDocument.class, Arrays.asList("doc-1","doc-2"), null, null);
 
         assertThat(result.getRows().size(), is(4));
         assertThat( valueCount(result,"doc-1"), is(2));
         assertThat( valueCount(result,"doc-2"), is(2));
 
     }
 
     @Test
     public void thatViewAndDocumentQueryingWorks()
     {
         Database db = createDatabaseForTest();
         ViewAndDocumentsResult<Object,FooDocument> result = db.queryViewAndDocumentsByKeys("foo/byValue", Object.class, FooDocument.class, Arrays.asList("doc-1"), null, null);
         List<ValueAndDocumentRow<Object, FooDocument>> rows = result.getRows();
         assertThat(rows.size(), is(2));
 
         ValueAndDocumentRow<Object, FooDocument> row = rows.get(0);
         assertThat(row.getDocument(), is(notNullValue()));
         assertThat(row.getDocument().getValue(), is("doc-1"));
 
         row = rows.get(1);
         assertThat(row.getDocument(), is(notNullValue()));
         assertThat(row.getDocument().getValue(), is("doc-1"));
 
 
     }
 
     @Test
     public void testPureBaseDocumentAccess()
     {
         Database db = createDatabaseForTest();
 
         BaseDocument newdoc = new BaseDocument();
         final String value = "baz403872349";
         newdoc.setProperty("foo",value); // same as JSON: { foo: "baz..." }
 
         assertThat(newdoc.getId(), is(nullValue()));
         assertThat(newdoc.getRevision(), is(nullValue()));
 
         db.createDocument(newdoc); // auto-generated id given by the database
 
         assertThat(newdoc.getId().length(), is(greaterThan(0)));
         assertThat(newdoc.getRevision().length(), is(greaterThan(0)));
 
         BaseDocument doc = db.getDocument(BaseDocument.class, newdoc.getId());
 
         assertThat((String)doc.getProperty("foo"), is(value));
 
     }
     
     @Test
     public void testAttachmentStreaming() throws IOException
     {
         Database db = createDatabaseForTest();
         
         final String docId = "attachmentStreamingDoc";
         final String content = "Streaming test.";
         final String content2 = "Streaming test 2.";
         byte[] data = content.getBytes();
         String revision = db.createAttachment(docId, null, "test.txt", "text/plain", new ByteArrayInputStream(data), data.length);
         assertThat(revision.length(), is(greaterThan(0)));
         
         Response resp = db.getAttachmentResponse(docId, "test.txt");
         InputStream is = resp.getInputStream();
         assertThat(new String(IOUtils.toByteArray(is)), is(content));
         resp.destroy();
         
         byte[] data2 = content2.getBytes();
         revision = db.updateAttachment(docId, revision, "test.txt", "text/plain", new ByteArrayInputStream(data2), data2.length);
             
         resp = db.getAttachmentResponse(docId, "test.txt");
         is = resp.getInputStream();
         assertThat(new String(IOUtils.toByteArray(is)), is(content2));
         resp.destroy();
     }
     
     @Test
     public void testValidation()
     {
         String fn = 
                 "function(newDoc, oldDoc, userCtx){\n" + 
         		"  if (newDoc.validationTestField && newDoc.validationTestField !== '123') {\n" + 
         		"    throw({'forbidden':'not 123'});\n" + 
         		"  }\n" + 
         		"}";
         DesignDocument designDoc = new DesignDocument("validate_test");
         designDoc.setValidateOnDocUpdate(fn);
         
         Database db = createDatabaseForTest();
         
         assertThat(designDoc.getRevision(), is(nullValue()));
         db.createDocument(designDoc);
         assertThat(designDoc.getRevision(), is(notNullValue()));
         
         BaseDocument doc = new BaseDocument();
         doc.setProperty("validationTestField", "123");
         
         assertThat(doc.getRevision(), is(nullValue()));
         db.createDocument(doc);
         assertThat(doc.getRevision(), is(notNullValue()));
         
         doc.setProperty("validationTestField", "invalid");
         
         DocumentValidationException e = null;
         try
         {
             db.updateDocument(doc);
         }
         catch(DocumentValidationException e2)
         {
             e = e2;
         }
         
         assertThat(e, is(notNullValue()));
         assertThat(e.getReason(), is("not 123"));
         assertThat(e.getError(), is("forbidden"));
     }
     
     @Test
     @Ignore
     public void thatHandlingHugeAttachmentsWorks()
     {
         Database db = createDatabaseForTest();
         
         BaseDocument doc = new BaseDocument();
         db.createDocument(doc);
         
         long length = (long)(Runtime.getRuntime().maxMemory() * 1.1);
         InputStream is = new SizedInputStreamMock((byte)'A', length);
         db.createAttachment(doc.getId(), doc.getRevision(), "hugeAttachment.txt", "text/plain", is, length);
         
         doc = db.getDocument(BaseDocument.class, doc.getId());
         
         Map<String, Attachment> attachments = doc.getAttachments();
         assertThat(attachments.size(), is(1));
         Attachment attachment = attachments.get("hugeAttachment.txt");
         assertThat(attachment, is(notNullValue()));
         assertThat(attachment.getLength(), is(length));
         assertThat(attachment.getContentType(), is("text/plain"));
         assertThat(attachment.isStub(), is(true));
     }
 }
