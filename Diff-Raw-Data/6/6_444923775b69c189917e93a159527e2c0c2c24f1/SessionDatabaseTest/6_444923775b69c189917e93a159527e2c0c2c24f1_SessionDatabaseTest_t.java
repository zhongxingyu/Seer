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
 package junit.sorcer.core.provider.exertmonitor;
 
 import static org.junit.Assert.assertEquals;
 import static sorcer.co.operator.list;
 
 import java.io.File;
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
 
 import sorcer.core.provider.exertmonitor.IMonitorSession;
 import sorcer.util.IOUtils;
 import sorcer.util.bdb.objects.UuidKey;
 
 import com.sleepycat.collections.StoredMap;
 
 /**
  * @author Mike Sobolewski
  */
 
 public class SessionDatabaseTest {
 
 	private final static Logger logger = Logger
 			.getLogger(SessionDatabaseTest.class.getName());
 	
 	static {
 		System.setProperty("java.security.policy", System.getenv("SORCER_HOME")
 				+ "/configs/sorcer.policy");
 		System.setSecurityManager(new RMISecurityManager());
 	}
 	
 	private static SessionDatabaseRunner runner;
 	private static File dbDir;
 	
 	@BeforeClass 
 	public static void setUpOnce() throws Exception {

		dbDir = new File(System.getProperty("java.io.tmpdir"), "ju-session-db");
 		dbDir.mkdirs();
		String homeDir = System.getProperty("java.io.tmpdir") + File.separator + "ju-session-db";
 		runner = new SessionDatabaseRunner(homeDir);
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
 	public void sessionDatabaseTest() throws Exception {
         // get from the database three sessions transactionally persisted with three tasks
 		List<String> names = runner.returnExertionNames();
 		List<String> ln = list("t1", "t2");
 		Collections.sort(names);
 		logger.info("names: " + names);
 		
 		assertEquals(names, ln);
 	}
 
     @Test
 	public void storedMapTest() throws Exception {
 		StoredMap<UuidKey, IMonitorSession> sm = runner.getViews()
 				.getSessionMap();
 		
 		Iterator<Map.Entry<UuidKey, IMonitorSession>> mei = sm
 				.entrySet().iterator();
 				
 		List<String> names = new ArrayList<String>();
 		Map.Entry<UuidKey, IMonitorSession> entry = null;
 
 		while (mei.hasNext()) {
 			entry = mei.next();
 			names.add(entry.getValue().getInitialExertion().getName());
 		}
 		List<String> ln = list("t1", "t2");
 		Collections.sort(names);
 		assertEquals(names, ln);
 	}
 }
