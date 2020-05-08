 /*
  * Copyright (c) 2011. Elad Kehat.
  * This software is provided under the MIT License:
  * http://www.opensource.org/licenses/mit-license.php
  */
 
 package com.jzboy.couchdb;
 
 import java.util.ArrayList;
 import org.codehaus.jackson.JsonNode;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  * These tests run against a CouchDB instance running on localhost and listening on the default port (5984).
  */
 public class ServerTest {
 
 	static Server instance;
 
 	@BeforeClass
 	public static void setUpClass() throws Exception {
 		instance = new Server();
 	}
 
 	@Test
 	public void testVersion() throws Exception {
		String re = "^\\d+\\.\\d+\\.\\w+$";
 		String version = instance.version();
		assertTrue("version response format does not match expected format: " + version,
                 version.matches(re));
 	}
 
 	@Test
 	public void testAllDbs() throws Exception {
		final String dbName = "jzboy_server_test_db_" + System.currentTimeMillis();
 		Database db = new Database(instance, dbName);
 		db.create();
 		try {
 			ArrayList<String> result = instance.allDbs();
 			assertTrue("allDbs response does not contain a newly created DB",
                     result.contains(dbName));
 		} finally {
 			db.delete();
 		}
 	}
 
 	@Test
 	public void testConfig() throws Exception {
 		JsonNode result = instance.config();
 		// the result is a big JSON with CouchDB's configuration params
 		// just check that one of them is indeed there
 		String logLevel = result.get("log").get("level").getTextValue();
 		assertNotNull("config response does not contain the log level property", logLevel);
 		assertFalse("config response log level property is empty", logLevel.isEmpty());
 	}
 
 	@Test
 	public void testStats() throws Exception {
 		JsonNode result = instance.stats();
 		// the result is a big JSON with CouchDB's statistics
 		// just check that one of the expected parameters is there
 		int currentOpenDbs = result.get("couchdb").get("open_databases").get("current").getIntValue();
 		assertTrue("stats response does not contain an expected parameter", currentOpenDbs >= 0);
 	}
 
 	@Test
 	public void testNextUUIDs() throws Exception {
 		final int count = 10;
 		ArrayList<String> result = instance.nextUUIDs(count);
 		assertEquals("nextUUIDs did not return the requested number of results",
                 count, result.size());
 		for (String uuid : result)
 			assertNotNull("A UUID returned by nextUUIDs was null", uuid);
 	}
 
 	@Test
 	public void testNextUUID() throws Exception {
 		String result = instance.nextUUID();
 		assertNotNull("The UUID returned by nextUUID was null", result);
 	}
 
 	@Test
 	public void testActiveTasks() throws Exception {
 		// Not much to test here really (unless we want to set up some lengthy task to start running).
 		// So I just call the method and ensure that it returns smoothly w/o any exceptions flying.
 		ArrayList<JsonNode> result = instance.activeTasks();
 		assertNotNull("activeTasks returned a null result", result);
 	}
 
 	@Test
 	public void testToString() {
 		String expResult = "CouchDB @http://127.0.0.1:5984/";
 		String result = instance.toString();
 		assertEquals("toString did not return the expected result", expResult, result);
 	}
 
 }
