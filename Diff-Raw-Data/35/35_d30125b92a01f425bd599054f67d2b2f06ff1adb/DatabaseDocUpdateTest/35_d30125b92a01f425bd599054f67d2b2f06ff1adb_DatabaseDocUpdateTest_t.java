 /*
  * Copyright (c) 2011. Elad Kehat.
  * This software is provided under the MIT License:
  * http://www.opensource.org/licenses/mit-license.php
  */
 
 package com.jzboy.couchdb;
 
 import com.jzboy.couchdb.util.JsonUtils;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.zip.DeflaterOutputStream;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.node.JsonNodeFactory;
 import org.codehaus.jackson.node.ObjectNode;
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  * Tests for the Database class methods that modify documents in the database.
  * These tests run against a CouchDB instance running on localhost and listening on the default port (5984).
  */
 public class DatabaseDocUpdateTest {
 
 	static String dbName;
 	static Database instance;
 	static byte[] attachment;
 
     @BeforeClass
     public static void setUpClass() throws Exception {
		dbName = "jzboy_test_db_" + System.currentTimeMillis();
 		instance = new Database(dbName);
 		instance.create();
 
 		// create a gzipped attachment
 		final String inputString = "blahblahblah??";
 		byte[] input = inputString.getBytes("utf-8");
 		ByteArrayOutputStream os = new ByteArrayOutputStream();
 		DeflaterOutputStream deflater = new DeflaterOutputStream(os);
 		deflater.write(input, 0, input.length);
 		deflater.close();
 		attachment = os.toByteArray();
     }
 
 	@AfterClass
 	public static void tearDownClass() throws Exception {
 		instance.delete();
 	}
 
 	@Test
 	public void testCreateDocumentFromJsonStrWithId() throws Exception {
 		final String uuid = instance.getServer().nextUUID();
 		final String jsonStr = "{\"field1\":\"value1\",\"field2\":2}";
 		Document result = instance.createDocument(uuid, jsonStr, false);
 		assertEquals("New document's UUID doesn't match the value supplied to createDocument",
                 uuid, result.getId());
 		assertTrue("Newly created document has no rev value?", result.hasRev());
 		assertTrue("Result of createDocument wasn't {'ok':true}",
                 result.getJson().get("ok").getBooleanValue());
 		Document doc = instance.getDocument(uuid);
 		assertEquals("New document's content doesn't match the content supplied to createDocument",
                 jsonStr, JsonUtils.serializeJson(doc.getJson()));
 	}
 
 	@Test
 	public void testCreateDocumentFromJsonNodeWithId() throws Exception {
 		final String uuid = instance.getServer().nextUUID();
 		ObjectNode docJson = new ObjectNode(JsonNodeFactory.instance);
 		docJson.put("field1", "value1");
 		docJson.put("field2", 2);
 		Document result = instance.createDocument(uuid, docJson, false);
 		assertEquals("New document's UUID doesn't match the value supplied to createDocument",
                 uuid, result.getId());
 		assertTrue("Newly created document has no rev value?", result.hasRev());
 		assertTrue("Result of createDocument wasn't {'ok':true}",
                 result.getJson().get("ok").getBooleanValue());
 	}
 
 	@Test
 	public void testCreateDocumentInBatch() throws Exception {
 		final String uuid = instance.getServer().nextUUID();
 		final String jsonStr = "{\"field1\":\"value1\",\"field2\":2}";
 		Document result = instance.createDocument(uuid, jsonStr, true);
 		assertEquals("New document's UUID doesn't match the value supplied to batch create",
                 uuid, result.getId());
 		assertTrue("Result of batch createDocument wasn't {'ok':true}",
                 result.getJson().get("ok").getBooleanValue());
 		assertFalse("Document created in batch mode returned a revision", result.hasRev());
 		// wait for 2 seconds, then try to load this document
 		Thread.sleep(2000);
 		Document doc = instance.getDocument(uuid);
 		assertEquals("New document's content doesn't match the content supplied to batch create",
                 jsonStr, JsonUtils.serializeJson(doc.getJson()));
 	}
 
 	@Test
 	public void testCreateDocumentFromDocument() throws Exception {
 		final String uuid = instance.getServer().nextUUID();
 		ObjectNode docJson = new ObjectNode(JsonNodeFactory.instance);
 		docJson.put("field1", "value11");
 		docJson.put("field2", 22);
 		Document newDoc = new Document(uuid, docJson);
 		Document result = instance.createDocument(newDoc, false);
 		assertEquals("New document's UUID doesn't match the value supplied to createDocument",
                 uuid, result.getId());
 		assertTrue("Newly created document has no rev value?", result.hasRev());
 		assertTrue("Result of createDocument wasn't {'ok':true}",
                 result.getJson().get("ok").getBooleanValue());
 
 		Document doc = instance.getDocument(uuid);
 		assertEquals(docJson, doc.getJson());
 	}
 
 	@Test
 	public void testUpdateDocument() throws Exception {
 		final String uuid = instance.getServer().nextUUID();
 		final String jsonStr = "{\"field1\":\"value135\",\"field2\":246}";
 		// create a new document
 		Document result = instance.createDocument(uuid, jsonStr, false);
 		assertTrue("Result of createDocument update wasn't {'ok':true}",
                 result.getJson().get("ok").getBooleanValue());
 		// check the value of 'field1'
 		Document doc = instance.getDocument(uuid);
 		assertEquals("Newly created Document doesn't contain a supplied value",
                 "value135", JsonUtils.getString(doc.getJson(), "field1", null));
 		final String rev1 = doc.getRev();
 		// update 'field1' to a different value
 		ObjectNode node = (ObjectNode) doc.getJson();
 		node.put("field1", "value99");
 		Document upDoc = instance.updateDocument(doc);
 		// ensure that we got a higher revision number
 		assertTrue("Update didn't increment document revision",
                 upDoc.getRev().compareTo(rev1) > 0);
 		// check the value of 'field1'
 		Document docAgain = instance.getDocument(uuid);
 		assertEquals("Update didn't modify the document",
                 "value99", JsonUtils.getString(docAgain.getJson(), "field1", null));
 	}
 
 	@Test
 	public void testDeleteDocument() throws Exception {
 		final String uuid = instance.getServer().nextUUID();
 		final String jsonStr = "{\"field1\":\"value135\",\"field2\":246}";
 		// create a new document
 		Document result = instance.createDocument(uuid, jsonStr, false);
 		assertTrue("Result of createDocument update wasn't {'ok':true}",
                 result.getJson().get("ok").getBooleanValue());
 		// ensure that document exists
 		Document doc = instance.getDocument(uuid);
 		assertEquals("New document's content doesn't match the content in create",
                 jsonStr, JsonUtils.serializeJson(doc.getJson()));
 		// now delete it
 		String rev = instance.deleteDocument(doc);
 		// the delete stub revision should be higher than the original doc revision
 		assertTrue("Deleted Document stub revision wasn't incremented",
                 doc.getRev().compareTo(rev) < 0);
 		// ensure that it doesn't exist anymore
 		try {
 			instance.getDocument(uuid);
 			fail("Document was supposed to have been deleted");
 		} catch (CouchDBException ex) {
 			assertEquals("Attempt to retrieve a deleted document didn't return the expected status code",
                     404, ex.getStatusCode());
 		}
 	}
 
 	private byte[] readInputStream(InputStream is) throws IOException {
 		ByteArrayOutputStream os = new ByteArrayOutputStream();
 		int i;
 		while ((i = is.read()) > -1)
 			os.write(i);
 		return os.toByteArray();
 	}
 
 	@Test
 	public void testSaveAndGetAttachment() throws Exception {
 		final String fileName = "blahblah.txt.gz";
 		final String uuid = instance.getServer().nextUUID();
 		final String jsonStr = "{\"field1\":\"value135\",\"field2\":246}";
 		final String contentType = "application/x-gzip";
 
 		Document doc = new Document(uuid, null, jsonStr);
 		Document resDoc = instance.saveAttachment(doc, fileName, attachment, contentType);
 		assertTrue("Result of saveAttachment update wasn't {'ok':true}",
                 resDoc.getJson().get("ok").getBooleanValue());
 		assertEquals("UUID of Document saved with attachment wasn't the supplied UUID",
                 uuid, resDoc.getId());
 
 		// now get the stored attachment in its raw form
 		HttpResponse httpRes = instance.getAttachmentRaw(uuid, fileName);
 		assertEquals("Request for raw Document attachment didn't return status 200",
                 200, httpRes.getStatusLine().getStatusCode());
 		HttpEntity ent = httpRes.getEntity();
 		assertEquals("Attachment content type doesn't match the type supplied on creation",
                 contentType, ent.getContentType().getValue());
 		byte[] resAttachment = readInputStream(ent.getContent());
 		assertArrayEquals("Contents of attachement retrieved in raw form doesn't match the content supplied on creation",
                 attachment, resAttachment);
 		// get the stored attachment as an input stream
 		resAttachment = instance.getAttachment(uuid, fileName);
 		assertArrayEquals("Contents of attachment retrieved as inputStream doesn't match the content supplied on creation",
                 attachment, resAttachment);
 	}
 
 
 	@Test
 	public void testDeleteAttachment() throws Exception {
 		// save an attachment
 		final String fileName = "blahblah.txt.gz";
 		final String uuid = instance.getServer().nextUUID();
 		final String jsonStr = "{\"field1\":\"value135\",\"field2\":246}";
 		final String contentType = "application/x-gzip";
 		Document doc = new Document(uuid, null, jsonStr);
 		Document resDoc = instance.saveAttachment(doc, fileName, attachment, contentType);
 		assertTrue("Result of saveAttachment update wasn't {'ok':true}",
                 resDoc.getJson().get("ok").getBooleanValue());
 		assertEquals("UUID of Document saved with attachment wasn't the supplied UUID",
                 uuid, resDoc.getId());
 		// ensure it's retrievable
 		byte[] resAttachment = instance.getAttachment(uuid, fileName);
 		assertTrue("Attachment contents wasn't returned by getAttachment",
                 resAttachment.length > 0);
 		// delete it
 		Document del = instance.deleteAttachment(resDoc, fileName); // use the doc that has a correct revision id
 		assertEquals("UUID returned by deleteAttachment doesn't match the original UUID",
                 uuid, del.getId());
 		assertTrue("Rev returned by deleteAttachment wasn't incremented",
                 del.getRev().compareTo(resDoc.getRev()) > 0);
 		// ensure that it's gone
 		try {
 			resAttachment = instance.getAttachment(uuid, fileName);
 			fail("Attachment wasn't deleted");
 		} catch (CouchDBException e) {
 			assertEquals("Attempt to retrieve a deleted attachment didn't return the expected status code",
                     404, e.getStatusCode());
 		}
 	}
 
 	@Test
 	public void testNumDocsPendingBulkUpdate() throws Exception {
 		ArrayList<String> uuids = instance.getServer().nextUUIDs(2);
 		final String json1 = "{\"field1\":\"value1\",\"field2\":2}";
 		final String json2 = "{\"field1\":\"value2\",\"field2\":3}";
 		Document doc1 = new Document(uuids.get(0), null, json1);
 		Document doc2 = new Document(uuids.get(1), null, json2);
 		instance.saveInBulk(doc1);
 		instance.saveInBulk(doc2);
 		int pending = instance.numDocsPendingBulkUpdate();
 		assertEquals("Wrong number of pending docs following saveInBulk calls",
                 2, pending);
 		instance.clearBulkUpdatesCache();
 		pending = instance.numDocsPendingBulkUpdate();
 		assertEquals("Number of pending docs following clearBulkUpdatesCache not zero",
                 0, pending);
 	}
 
 	@Test
 	public void testSaveInBulk() throws Exception {
 		instance.setBulkUpdatesLimit(2);
 		ArrayList<String> uuids = instance.getServer().nextUUIDs(2);
 		final String json1 = "{\"field1\":\"value1\",\"field2\":2}";
 		final String json2 = "{\"field1\":\"value2\",\"field2\":3}";
 		Document doc1 = new Document(uuids.get(0), null, json1);
 		Document doc2 = new Document(uuids.get(1), null, json2);
 		assertEquals("Unexpected documents pending bulk save - cache should be clear",
                 0, instance.numDocsPendingBulkUpdate());
 		instance.saveInBulk(doc1);
 		assertEquals("Wrong number of pending docs following saveInBulk call",
                 1, instance.numDocsPendingBulkUpdate());
 		instance.saveInBulk(doc2);
 		assertEquals("Bulk cache not cleared following number of saves that matched the limit",
                 0, instance.numDocsPendingBulkUpdate());
 
 		Document resDoc1 = instance.getDocument(uuids.get(0));
 		assertEquals("Contents of document saved in bulk and then retrieved don't match the original",
                 json1, JsonUtils.serializeJson(resDoc1.getJson()));
 		Document resDoc2 = instance.getDocument(uuids.get(1));
 		assertEquals("Contents of document saved in bulk and then retrieved don't match the original",
                 json2, JsonUtils.serializeJson(resDoc2.getJson()));
 	}
 
 	@Test
 	public void testSaveInBulkWithoutContent() throws Exception {
 		Document doc = new Document("1", "1-012345"); // id and rev, no JSON
 		try {
 			instance.saveInBulk(doc);
 			fail("Document with no content was allowed to be added to bulk cache");
 		} catch (IllegalArgumentException e) { }
 	}
 
 	@Test
 	public void testDeleteInBulk() throws Exception {
 		instance.setBulkUpdatesLimit(2);
 		ArrayList<String> uuids = instance.getServer().nextUUIDs(2);
 		final String json1 = "{\"field1\":\"value15\",\"field2\":15}";
 		final String json2 = "{\"field1\":\"value25\",\"field2\":25}";
 		Document saved1 = instance.createDocument(uuids.get(0), json1, false);
 		Document saved2 = instance.createDocument(uuids.get(1), json2, false);
 		saved1.setJson(null);
 		saved2.setJson(null);
 		instance.deleteInBulk(saved1);
 		instance.deleteInBulk(saved2);
 
 		assertEquals("Number of documents in bulk cache not as expected",
                 0, instance.numDocsPendingBulkUpdate());
 		try {
 			instance.getDocument(uuids.get(0));
 			fail("Document wasn't deleted");
 		} catch (CouchDBException ex1) {
 			assertEquals("Attempt to retrieve a deleted attachment didn't return the expected status code",
                     404, ex1.getStatusCode());
 		}
 		try {
 			instance.getDocument(uuids.get(1));
 			fail("Document wasn't deleted");
 		} catch (CouchDBException ex2) {
 			assertEquals("Attempt to retrieve a deleted attachment didn't return the expected status code",
                     404, ex2.getStatusCode());
 		}
 	}
 
 	@Test
 	public void testFlushBulkUpdatesCache() throws Exception {
 		instance.setBulkUpdatesLimit(1000);
 		ArrayList<String> uuids = instance.getServer().nextUUIDs(2);
 		final String json1 = "{\"field1\":\"value1\",\"field2\":2}";
 		final String json2 = "{\"field1\":\"value2\",\"field2\":3}";
 		Document doc1 = new Document(uuids.get(0), null, json1);
 		Document doc2 = new Document(uuids.get(1), null, json2);
 		assertEquals("Number of documents in bulk cache not as expected",
                 0, instance.numDocsPendingBulkUpdate());
 		instance.saveInBulk(doc1);
 		instance.saveInBulk(doc2);
 		assertEquals("Number of documents in bulk cache not as expected",
                 2, instance.numDocsPendingBulkUpdate());
 
 		ArrayList<JsonNode> report = instance.flushBulkUpdatesCache(false, true);
 		assertEquals("Documents not flushed from bulk cache following call to flush",
                 0, instance.numDocsPendingBulkUpdate());
 		for (JsonNode resDoc : report) {
 			assertTrue("Results of flushBulkUpdatesCache did not contain a saved document's ID",
                     uuids.contains(resDoc.get("id").getTextValue()));
 		}
 	}
 
 }
