 /*
  * Copyright (c) 2011. Elad Kehat.
  * This software is provided under the MIT License:
  * http://www.opensource.org/licenses/mit-license.php
  */
 
 package com.jzboy.couchdb;
 
 import java.util.Map;
 import org.codehaus.jackson.JsonNode;
 import org.junit.Before;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  * Tests for the Database class methods that run on the database itself - not on documents within it.
  * These tests run against a CouchDB instance running on localhost and listening on the default port (5984).
  */
 public class DatabaseAPITest {
 
 	String dbName;
 	Database instance;
 
     @Before
     public void setUp() {
		dbName = "jzboy_test_db_" + System.currentTimeMillis();
 		instance = new Database(dbName);
     }
 
 	@Test
 	public void testCreate() throws Exception {
 		instance.create();
 		try {
 			JsonNode result = instance.info();
 			String resName = result.get("db_name").getTextValue();
 			assertEquals("Name of newly created DB does not match supplied name",
                     dbName, resName);
 		} finally {
 			instance.delete();
 		}
 	}
 
 	@Test
 	public void testInfoAsMap() throws Exception {
 		instance.create();
 		try {
 			Map<String, String> result = instance.infoAsMap();
 			String resName = result.get("db_name");
 			assertEquals("DB name from database info does not match the name given on creation",
                     dbName, resName);
 		} finally {
 			instance.delete();
 		}
 	}
 
 	@Test
 	public void testExists() throws Exception {
 		instance.create();
 		try {
 			assertTrue("Newly created DB does not exist", instance.exists());
 		} finally {
 			instance.delete();
 		}
 
		Database missing = new Database("no_such_database");
 		assertFalse("A presumably non-existent database does exist", missing.exists());
 		// ensure that exceptions are still thrown when the response isn't 404
 		try {
             // only lowercase letters are allowed by couchdb
			Database wrong = new Database("ILLEGAL_DB_NAME");
 			wrong.exists();
 			fail("Expected an exception when calling exists() on a db with an illegal name");
 		} catch (CouchDBException ex) {
 			assertTrue(ex.getStatusCode() != 404);
 		}
 	}
 
 	@Test
 	public void testCreateIfNotExists() throws Exception {
 		instance.create();
 		// run createIfNotExists on an existing database, to ensure it doesn't fail
 		try {
 			instance.createIfNotExists();
 		} finally {
 			instance.delete();
 		}
 		final String newName = dbName + "_new";
 		Database newInstance = new Database(newName);
 		// ensure that this db doesn't exist yet
 		JsonNode result;
 		try {
 			result = newInstance.info();
 			fail("Assumed that database " + newName + " doesn't exist");
 		} catch (CouchDBException e) {
 			assertEquals("Status code 404 expected for non-existent DB info request",
                     404, e.getStatusCode());
 		}
 		// now create the new database through the createIfNotExists method
 		try {
 			newInstance.createIfNotExists();
 			result = newInstance.info();
 			String resName = result.get("db_name").getTextValue();
 			assertEquals("createIfNotExists may not have created a DB with the supplied name",
                     newName, resName);
 		} finally {
 			newInstance.delete();
 		}
 	}
 
 	@Test
 	public void testDelete() throws Exception {
 		instance.create();
 		JsonNode result;
 		try {
 			result = instance.info();
 			String resName = result.get("db_name").getTextValue();
 			assertEquals(dbName, resName);
 		} finally {
 			instance.delete();
 		}
 		try {
 			result = instance.info();
 			fail("Database not deleted");
 		} catch (CouchDBException ex) {
 			assertEquals("Status code 404 expected for just-deleted DB info request",
                     404, ex.getStatusCode());
 		}
 	}
 
 	@Test
 	public void testGetRevsLimit() throws Exception {
 		instance.create();
 		try {
 			int result = instance.getRevsLimit();
 			int expected = 1000; // that's the default value for CouchDB
 			assertEquals("RevsLimit returned a value other than the default",
                     expected, result);
 		} finally {
 			instance.delete();
 		}
 	}
 
 	@Test
 	public void testSetRevsLimit() throws Exception {
 		final int newLimit = 2000;
 		try {
 			instance.create();
 			int currentLimit = instance.getRevsLimit();
 			assertFalse("New limit used for setRevsLimit test is same as current",
                     newLimit == currentLimit);
 
 			instance.setRevsLimit(newLimit);
 			currentLimit = instance.getRevsLimit();
 			assertTrue("New limit not set by setRevsLimit",
                     newLimit == currentLimit);
 		} finally {
 			instance.delete();
 		}
 	}
 
 	@Test
 	public void testCompact() throws Exception {
 		instance.create();
 		try {
 			instance.compact();
 		} catch (CouchDBException e) {
 			fail(e.toString());
 		} finally {
 			instance.delete();
 		}
 	}
 
 }
