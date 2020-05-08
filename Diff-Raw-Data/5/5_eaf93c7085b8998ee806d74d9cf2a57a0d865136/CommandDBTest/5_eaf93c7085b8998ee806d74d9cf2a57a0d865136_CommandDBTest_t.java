 package org.paxle.data.db.impl;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 import java.util.concurrent.Semaphore;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.filefilter.WildcardFileFilter;
 import org.jmock.Expectations;
 import org.jmock.integration.junit3.MockObjectTestCase;
 import org.paxle.core.data.IDataSink;
 import org.paxle.core.queue.ICommand;
 import org.paxle.core.queue.ICommandTracker;
 
 public class CommandDBTest extends MockObjectTestCase {
 	private static final String DERBY_CONFIG_FILE = "../DataLayerDerby/src/main/resources/resources/hibernate/derby.cfg.xml";
 	private static final String H2_CONFIG_FILE = "../DataLayerH2/src/main/resources/resources/hibernate/H2.cfg.xml";
 	
 	private static final String DERBY_CONNECTION_URL = "jdbc:derby:target/command-db;create=true";
 	private static final String H2_CONNECTION_URL = "jdbc:h2:target/command-db/cdb";
 	
 	private ICommandTracker cmdTracker;
 	private CommandDB cmdDB;
 	
 	/**
 	 * @return the hibernate mapping files to use
 	 * @throws MalformedURLException 
 	 */
 	private List<URL> getMappingFiles() throws MalformedURLException {
 		final File mappingFilesDir = new File("src/main/resources/resources/hibernate/mapping/command/");
 		assertTrue(mappingFilesDir.exists());
 		
 		final FileFilter mappingFileFilter = new WildcardFileFilter("*.hbm.xml");		
 		File[] mappingFiles = mappingFilesDir.listFiles(mappingFileFilter);
 		assertNotNull(mappingFiles);
 		assertEquals(4, mappingFiles.length);
 		
 		List<URL> mappingFileURLs = new ArrayList<URL>();
 		for (File mappingFile : mappingFiles) {
 			mappingFileURLs.add(mappingFile.toURL());
 		}
 		
 		return mappingFileURLs;
 	}
 		
 	/**
 	 * @return the hibernate config file to use
 	 * @throws MalformedURLException 
 	 */
 	private URL getConfigFile(String configFile) throws MalformedURLException {
 		final File derbyConfigFile = new File(configFile);
 		assertTrue(derbyConfigFile.exists());
 		return derbyConfigFile.toURL();
 	}	
 	
 	/**
 	 * @return additional properties that should be passed to hibernate
 	 */
 	private Properties getExtraProperties(String connectionString) {
 		Properties props = new Properties();
 		props.put("connection.url", connectionString);
 		props.put("hibernate.connection.url", connectionString);
 		return props;
 	}
 	
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		
 		// create a dummy command tracker
 		this.cmdTracker = mock(ICommandTracker.class);
 	}
 	
 	private void setupDB(String hibernateConfigFile, String connectionURL) throws MalformedURLException {		
 		// create and init the command-db
 		this.cmdDB = new CommandDB(
 				this.getConfigFile(hibernateConfigFile),
 				this.getMappingFiles(),
 				this.getExtraProperties(connectionURL),
 				this.cmdTracker		
 		);
 		
 		// startup DB
 		this.cmdDB.start();
 	}
 	
 	@Override
 	protected void tearDown() throws Exception {
 		// close DB
 		if (cmdDB != null) this.cmdDB.close();
 		
 		// delete data directory
 		File dbDir = new File("target/command-db");
 		assertTrue(dbDir.exists());
 		FileUtils.deleteDirectory(dbDir);
 
 		super.tearDown();
 	}
 	
 	/**
 	 * A dummy data-sink which just prints out the data
 	 */
 	private class DummyDataSink implements IDataSink<ICommand> {
 		private final Semaphore semaphore;
 		public DummyDataSink(Semaphore semaphore) {
 			this.semaphore = semaphore;
 		}
 		
 		public void putData(ICommand cmd) throws Exception {
 			System.out.println("New Command enqueued: " + cmd.getLocation().toASCIIString());
 			this.semaphore.release();
 		}
 
 		public int freeCapacity() throws Exception {
 			return -1;
 		}
 
 		public boolean freeCapacitySupported() {
 			return false;
 		}
 
 		public boolean offerData(ICommand cmd) throws Exception {
 			this.putData(cmd);
 			return true;
 		}
 	}
 
 	private void storeUnknownLocation() throws InterruptedException {
 		final int MAX = 10;
 		
 		// command-tracker must be called MAX times
 		checking(new Expectations() {{
 			exactly(MAX).of(cmdTracker).commandCreated(with(equal("org.paxle.data.db.ICommandDB")), with(any(ICommand.class)));
 		}});
 		
 		// store new commands
 		ArrayList<URI> testURI = new ArrayList<URI>();
 		for (int i=0; i < MAX; i++) {
 			testURI.add(URI.create("http://test.paxle.net/" + i));
 		}		
 		int known = this.cmdDB.storeUnknownLocations(0, 1, testURI);
 		assertEquals(0, known);
 
 		// create a dummy data-sink
 		Semaphore s = null;
 		this.cmdDB.setDataSink(new DummyDataSink(s = new Semaphore(-MAX + 1)));		
 		
 		// wait for all commans to be enqueued
 		boolean acquired = s.tryAcquire(3, TimeUnit.SECONDS);
 		assertTrue(acquired);
 	}
 	
	public void testStoreUnknownLocationDerby() throws MalformedURLException, InterruptedException {
 		// setup DB
 		this.setupDB(DERBY_CONFIG_FILE, DERBY_CONNECTION_URL);
 		
 		// start test
 		this.storeUnknownLocation();
 	}
 	
	public void _testStoreUnknownLocationH2() throws MalformedURLException, InterruptedException {
 		// setup DB
 		this.setupDB(H2_CONFIG_FILE, H2_CONNECTION_URL);
 		
 		// start test
 		this.storeUnknownLocation();
 	}	
 }
