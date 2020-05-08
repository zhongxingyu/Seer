 /**
  *
  * Copyright 2013 the original author or authors.
  * Copyright 2013 Sorcersoft.com S.A.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package junit.sorcer.util.bdb.objects;
 
 import static org.junit.Assert.assertEquals;
 import static sorcer.co.operator.list;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.rmi.RMISecurityManager;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import sorcer.core.SorcerEnv;
 import sorcer.service.Context;
 import sorcer.service.Exertion;
 import sorcer.util.IOUtils;
 
 import sorcer.util.bdb.objects.UuidKey;
 import sorcer.util.bdb.objects.UuidObject;
 
 import com.sleepycat.collections.StoredMap;
 import com.sleepycat.je.DatabaseException;
 
 /**
  * @author Mike Sobolewski
  */
 
 public class SorcerDatabaseTest {
 
 	private final static Logger logger = Logger
 			.getLogger(SorcerDatabaseTest.class.getName());
 	
 	static {
 		System.setProperty("java.security.policy", System.getenv("SORCER_HOME")
 				+ "/configs/sorcer.policy");
        System.setProperty("java.rmi.server.useCodebaseOnly", "false");
        System.setProperty("java.protocol.handler.pkgs", "net.jini.url|sorcer.util.bdb|org.rioproject.url");
        System.setSecurityManager(new RMISecurityManager());
 		SorcerEnv.setCodeBaseByArtifacts(new String[] {
 				"org.sorcersoft.sorcer:sorcer-api",
 				"org.sorcersoft.sorcer:ju-arithmetic-api" });
 	}
 	
 	private static SorcerDatabaseRunner runner;
 	private static File dbDir;
 	
 	@BeforeClass 
 	public static void setUpOnce() throws IOException, DatabaseException, Exception {
 		dbDir = new File("tmp/ju-sorcer-db");
         IOUtils.deleteDir(dbDir);
         System.out.println("Sorcer DB dir: " + dbDir.getCanonicalPath());
 		dbDir.mkdirs();
 		String homeDir = "tmp/ju-sorcer-db";
 		runner = new SorcerDatabaseRunner(homeDir);
         runner.run();
 	}
 	
 	@AfterClass 
 	public static void cleanup() throws Exception {
 		// delete database home directory and close database
 		IOUtils.deleteDir(dbDir);
 		if (runner != null) {
             try {
                 // Always attempt to close the database cleanly.
                 runner.close();
             } catch (Exception e) {
                 System.err.println("Exception during database close:");
                 e.printStackTrace();
             }
         }
 	}
 	
 	@Test
 	public void storedcContextSetTest() throws Exception {
         // get from the database three contexts persisted   
 		List<String> names = runner.returnContextNames();
 		List<String> ln = list("c1", "c2", "c3");
 		Collections.sort(names);
 		logger.info("names: " + names);
 		
 		assertEquals(names, ln);
 	}
 	
 	@Test
 	public void storedContextMapTest() throws Exception {
 		StoredMap<UuidKey, Context> sm = runner.getViews()
 				.getContextMap();
 		
 		Iterator<Map.Entry<UuidKey, Context>> mei = sm
 				.entrySet().iterator();
 				
 		List<String> names = new ArrayList<String>();
 		Map.Entry<UuidKey, Context> entry = null;
 
 		while (mei.hasNext()) {
 			entry = mei.next();
 			names.add(entry.getValue().getName());
 		}
 		List<String> ln = list("c1", "c2", "c3");
 		Collections.sort(names);
 		logger.info("names: " + names);
 		
 		assertEquals(names, ln);
 	}
 
 	@Test
 	public void storedExertionSetTest() throws Exception {
         // get from the database two exertions persisted twice
 		List<String> names = runner.returnExertionNames();
 		List<String> ln = list("f1", "f4");
 		Collections.sort(names);
 		logger.info("names: " + names);
 		
 		assertEquals(names, ln);
 	}
 	
 	@Test
 	public void storedExertionMapTest() throws Exception {
 		StoredMap<UuidKey, Exertion> sm = runner.getViews()
 				.getExertionMap();
 		
 		Iterator<Map.Entry<UuidKey, Exertion>> it = sm
 				.entrySet().iterator();
 				
 		List<String> names = new ArrayList<String>();
 		Map.Entry<UuidKey, Exertion> entry = null;
 
 		while (it.hasNext()) {
 			entry = it.next();
 			names.add(entry.getValue().getName());
 		}
 		List<String> ln = list("f1", "f4");
 		Collections.sort(names);
 		logger.info("names: " + names);
 		
 		assertEquals(names, ln);
 	}
 
 	@Test
 	public void storedUuidObjectSetTest() throws Exception {
         // get from the database three sessions persisted with three tasks
 		List<String> names = runner.returnUuidObjectNames();
 		List<String> ln = list("Mike", "Sobolewski");
 		Collections.sort(names);
 		logger.info("names: " + names);
 		
 		assertEquals(names, ln);
 	}
 	
 	@Test
 	public void storedUuidObjectMapTest() throws Exception {
 		StoredMap<UuidKey, UuidObject> sm = runner.getViews()
 				.getUuidObjectMap();
 		
 		Iterator<Map.Entry<UuidKey, UuidObject>> it = sm
 				.entrySet().iterator();
 				
 		List<String> names = new ArrayList<String>();
 		Map.Entry<UuidKey, UuidObject> entry = null;
 
 		while (it.hasNext()) {
 			entry = it.next();
 			names.add(entry.getValue().getObject().toString());
 		}
 		List<String> ln = list("Mike", "Sobolewski");
 		Collections.sort(names);
 		logger.info("names: " + names);
 		
 		assertEquals(names, ln);
 	}
 	
 	//@Test
 	public void sdbURL() throws Exception {
 		URL sbdUrl = new URL("sbd://myIterface/name#dataContext=2345");
 		Object obj = sbdUrl.openConnection().getContent();
 	}
 }
