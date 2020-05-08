 package org.paxle.data.db.impl;
 
 import java.io.File;
 import java.net.URI;
 import java.net.URL;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Properties;
 
 import org.apache.commons.cache.Cache;
 import org.apache.commons.cache.EvictionPolicy;
 import org.apache.commons.cache.GroupMap;
 import org.apache.commons.cache.LRUEvictionPolicy;
 import org.apache.commons.cache.MemoryStash;
 import org.apache.commons.cache.SimpleCache;
 import org.apache.commons.cache.StashPolicy;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.hibernate.HibernateException;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.Transaction;
 import org.hibernate.cfg.Configuration;
 import org.paxle.core.data.IDataConsumer;
 import org.paxle.core.data.IDataProvider;
 import org.paxle.core.data.IDataSink;
 import org.paxle.core.data.IDataSource;
 import org.paxle.core.queue.Command;
 import org.paxle.core.queue.ICommand;
 
 public class CommandDB implements IDataProvider<ICommand>, IDataConsumer<ICommand> {
 	private static final int MAX_IDLE_SLEEP = 60000;
 
 	private Cache urlExistsCache = null;
 
 	/**
 	 * A {@link IDataSink data-sink} to write the loaded {@link ICommand commands} out
 	 */
 	private IDataSink<ICommand> sink = null;	
 
 	/**
 	 * A {@link IDataSource data-source} to fetch {@link ICommand commands} from
 	 */
 	private IDataSource<ICommand> source = null;	
 
 	/**
 	 * A {@link Thread thread} to read {@link ICommand commands} from the {@link #source data-source}
 	 * and write it into the {@link #db database}. 
 	 */
 	private Reader readerThread = null;
 
 	/**
 	 * A {@link Thread thread} to read {@link ICommand commands} from the {@link #db database}
 	 * and write it into the {@link #sink data-sink}.
 	 */
 	private Writer writerThread = null;
 
 	/**
 	 * The logger
 	 */
 	private Log logger = LogFactory.getLog(this.getClass());
 
 	/**
 	 * The hibernate {@link SessionFactory}
 	 */
 	private SessionFactory sessionFactory;
 
 	/**
 	 * The currently used db configuration
 	 */
 	private Configuration config; 
 
 	private boolean closed = false;
 
 	public CommandDB(URL configURL, List<URL> mappings) {
 		if (configURL == null) throw new NullPointerException("The URL to the hibernate config file is null.");
 		if (mappings == null) throw new NullPointerException("The list of mapping files was null.");
 
 		try {
 			/* ===========================================================================
 			 * Init Hibernate
 			 * =========================================================================== */
 			try {
 				Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
 
 				// Read the hibernate configuration from *.cfg.xml
 				this.logger.info(String.format("Loading DB configuration from URL '%s'.",configURL));
 				this.config = new Configuration().configure(configURL);
 
 				// register an interceptor (required to support our interface-based command model)
 				this.config.setInterceptor(new InterfaceInterceptor());
 
 				// load the various mapping files
 				for (URL mapping : mappings) {
 					if (logger.isDebugEnabled()) this.logger.debug(String.format("Loading mapping file from URL '%s'.",mapping));
 					config.addURL(mapping);
 				}
 
 				// String[] sql = config.generateSchemaCreationScript( new org.hibernate.dialect.MySQLDialect());
 
 				// create the session factory
 				sessionFactory = config.buildSessionFactory();
 			} catch (Throwable ex) {
 				// Make sure you log the exception, as it might be swallowed
 				this.logger.error("Initial SessionFactory creation failed.",ex);
 				throw new ExceptionInInitializerError(ex);
 			}
 			this.manipulateDbSchema();
 			System.out.println(this.size());
 
 			/* ===========================================================================
 			 * Init Reader/Writer Threads
 			 * =========================================================================== */
 			this.writerThread = new Writer();
 			this.readerThread = new Reader();
 
 			/* ===========================================================================
 			 * Init Cache
 			 * =========================================================================== */		  
 			LRUEvictionPolicy ep = new LRUEvictionPolicy();
 			this.urlExistsCache = new SimpleCache(new MemoryStash(100000), (EvictionPolicy)ep, (StashPolicy) null, (GroupMap)null, (File)null);
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new RuntimeException(e);
 		}
 	}	
 	
 	private void manipulateDbSchema() {
 		System.setProperty("derby.language.logQueryPlan", "true");
 		Connection c = null;
 		try {
 			Properties props = this.config.getProperties();
 			String dbDriver = props.getProperty("connection.driver_class");
 			if (dbDriver != null && dbDriver.equals("org.apache.derby.jdbc.EmbeddedDriver")) {
 				c = DriverManager.getConnection(props.getProperty("connection.url"));
 				PreparedStatement p = c.prepareStatement("CREATE INDEX LOCATION_IDX on COMMAND (location)");
 				p.execute();
 				p.close();
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			try { c.close(); } catch (SQLException e) {/* ignore this */}
 		}
 	}
 
 	public void start() {		
 		this.writerThread.start();
 		this.readerThread.start();		
 	}
 
 	/**
 	 * @see IDataConsumer#setDataSource(IDataSource)
 	 */
 	@SuppressWarnings("unchecked")
 	public void setDataSource(IDataSource dataSource) {
 		if (dataSource == null) throw new NullPointerException("The data-source is null-");
 		if (this.source != null) throw new IllegalStateException("The data-source was already set.");
 		synchronized (this.readerThread) {
 			this.source = dataSource;
 			this.readerThread.notify();			
 		}
 	}
 
 	/**
 	 * @see IDataProvider#setDataSink(IDataSink)
 	 */
 	@SuppressWarnings("unchecked")
 	public void setDataSink(IDataSink dataSink) {
 		if (dataSink == null) throw new NullPointerException("The data-sink is null-");
 		if (this.sink != null) throw new IllegalStateException("The data-sink was already set.");
 		synchronized (this.writerThread) {
 			this.sink = dataSink;
 			this.writerThread.notify();			
 		}
 	}
 
 	public boolean isClosed() {
 		return this.closed;
 	}
 
 	public void close() throws InterruptedException {
 		try {
 			// interrupt reader and writer
 			this.readerThread.interrupt();
 			this.writerThread.interrupt();
 
 			// wait for the threads to shutdown
 			this.readerThread.join(2000);
 			this.writerThread.join(2000);
 
 			// close the DB
 			this.sessionFactory.close();
 
 			// shutdown the database
 			try {
 				String dbDriver = this.config.getProperties().getProperty("connection.driver_class");
 				if (dbDriver != null && dbDriver.equals("org.apache.derby.jdbc.EmbeddedDriver")) {
 					DriverManager.getConnection("jdbc:derby:;shutdown=true");
 				}
 			} catch (SQLException e) {
 				String errMsg = e.getMessage();
 				if (!(errMsg != null && errMsg.equals("Derby system shutdown."))) {
 					this.logger.error("Unable to shutdown database.",e);
 				}
 			}
 
 			// flush cache
 			this.urlExistsCache.clear();
 		}finally {
 			this.closed = true;
 		}
 	}
 
 	public boolean isKnown(String location) {
 		if (location == null || location.length() == 0) return false;
 		boolean known = false;
 
 		Session session = sessionFactory.getCurrentSession();
 		Transaction transaction = null;
 		try {
 			transaction = session.beginTransaction();
 
 			Query query = session.createQuery("SELECT count(location) FROM ICommand as cmd WHERE location = ?").setString(0, location);
 			Long result = (Long) query.uniqueResult();
 			known = (result != null && result.longValue() > 0);
 
 			transaction.commit();
 		} catch (HibernateException e) {
 			if (transaction != null && transaction.isActive()) transaction.rollback(); 
 			this.logger.error("Error while testing if location is known.",e);
 		}	
 
 		return known;
 	}
 
 //	/**
 //	* TODO: does not work at the moment
 //	*/
 //	private void commandToXML(ICommand cmd) {
 //	Session session = sessionFactory.getCurrentSession();		
 //	Transaction transaction = null;
 //	try {
 //	transaction = session.beginTransaction();	
 //	Session dom4jSession = session.getSession(EntityMode.DOM4J);
 
 //	dom4jSession.saveOrUpdate(cmd);
 
 //	transaction.commit();
 //	} catch (HibernateException e) {
 //	if (transaction != null && transaction.isActive()) transaction.rollback(); 
 //	this.logger.error("Error while converting command to XML",e);
 //	}	
 //	}
 
 	/**
 	 * TODO: we can speedup this by using cursors or iterators here ... 
 	 */
 	@SuppressWarnings("unchecked")
 	private List<ICommand> fetchNextCommands(int offset, int limit)  {		
 		List<ICommand> result = null;
 		Session session = sessionFactory.getCurrentSession();
 		Transaction transaction = null;
 		try {
 			transaction = session.beginTransaction();
 
 			Query query = session.getNamedQuery("fromCrawlerQueue");
 //			session.createQuery("FROM ICommand as cmd LEFT JOIN cmd.IndexerDocuments as indexerDoc WHERE AND (indexerDoc is null)");
 			query.setFirstResult(offset);
 			query.setMaxResults(limit);
 			result = (List<ICommand>) query.list();
 
 			/* This is a q&d hack to avoid double loading of enqueued commands. */
 			for (ICommand cmd : (List<ICommand>) result) {
 				cmd.setResultText("Enqueued");
 				session.update(cmd);
 				this.urlExistsCache.store(cmd.getLocation(), Boolean.TRUE, null, null);
 			}
 
 			transaction.commit();
 		} catch (HibernateException e) {
 			if (transaction != null && transaction.isActive()) transaction.rollback(); 
 			this.logger.error("Error while querying queue size",e);
 		}	
 
 		return result;
 	}
 
 	public synchronized void storeCommand(ICommand cmd) {
 		Session session = sessionFactory.getCurrentSession();
 		Transaction transaction = null;
 		try {
 			transaction = session.beginTransaction();
 
 			session.saveOrUpdate(cmd);
 			this.urlExistsCache.store(cmd.getLocation(), Boolean.TRUE, null, null);
 
 			transaction.commit();
 
 			// signal writer that a new URL is available
 			this.writerThread.signalNewDbData();			
 		} catch (HibernateException e) {
 			if (transaction != null && transaction.isActive()) transaction.rollback(); 
 			this.logger.error(String.format("Error while writing command with location '%s' to db.", cmd.getLocation()),e);
 		}
 	}
 	
 	/**
 	 * First queries the DB to remove all known locations from the list and then updates
 	 * it with the new list.
 	 * 
 	 * @param locations the locations to add to the DB
 	 * @return the number of known locations in the given list
 	 */
 	int storeUnknownLocations(List<URI> locations) {
 		if (locations == null || locations.size() == 0) return 0;
 		Session session = sessionFactory.getCurrentSession();
 		Transaction transaction = null;
 		try {
 			transaction = session.beginTransaction();
 
 			// check the cache for URL existance
 			Iterator<URI> locationIterator = locations.iterator();
 			while (locationIterator.hasNext()) {
 				if (this.urlExistsCache.contains(locationIterator.next())) {
 					locationIterator.remove();
 				}
 			}
 			if (locations.size() == 0) return 0;
 
 			// check which URLs are already known
 			HashSet<String> knownLocations = new HashSet<String>();
 
 			int chunkSize = 10;
 			if (locations.size() <= chunkSize) {
 				Query query = session.createQuery("SELECT DISTINCT location FROM ICommand as cmd WHERE location in (:locationList)").setParameterList("locationList",locations);
 				knownLocations.addAll(query.list());
 			} else {
 				int i=0,oldI;
 				while (i<(locations.size()-1)) {
 					oldI = i;
 					if ((i+chunkSize)>=locations.size()) {
 						i=(locations.size()-1); 
 					} else {
 						i+=chunkSize;
 					}
 					List<URI> miniLocations = locations.subList(oldI, i);
 					Query query = session.createQuery("SELECT DISTINCT location FROM ICommand as cmd WHERE location in (:locationList)").setParameterList("locationList",miniLocations);
 					knownLocations.addAll(query.list());			
 				}
 			}
 			
 			int known = 0;
 			
 			// add new commands into DB
 			for (URI location : locations) {
 				if (knownLocations.contains(location)) {
 					this.urlExistsCache.store(location, Boolean.TRUE, null, null);
 					known++;
 					continue;
 				}
 				session.saveOrUpdate(Command.createCommand(location));	
 				this.urlExistsCache.store(location, Boolean.TRUE, null, null);
 			}
 
 			transaction.commit();
 
 			// signal writer that a new URL is available
 			this.writerThread.signalNewDbData();
 			return known;
 		} catch (HibernateException e) {
 			if (transaction != null && transaction.isActive()) transaction.rollback(); 
 			this.logger.error(String.format("Unexpected '%s' while writing %d new commands to db.",
 					e.getClass().getName(),
 					Integer.valueOf(locations.size())
 			),e);
 		}
 		return 0;
 	}
 
 	/**
 	 * @return the total size of the command queue
 	 */
 	public long size() {		
 		Long count = Long.valueOf(-1l);
 		Session session = sessionFactory.getCurrentSession();
 		Transaction transaction = null;
 		try {
 			transaction = session.beginTransaction();
 
 			count = (Long) session.createQuery("select count(*) from ICommand").uniqueResult();
 
 			transaction.commit();
 		} catch (HibernateException e) {
 			if (transaction != null && transaction.isActive()) transaction.rollback(); 
 			this.logger.error("Error while querying queue size",e);
 		}		
 
 		return count.longValue();
 	}
 
 	/**
 	 * Resets the command queue
 	 */
 	public void reset() {
 		Session session = sessionFactory.getCurrentSession();
 		Transaction transaction = null;
 		try {
 			transaction = session.beginTransaction();
 
 			session.createQuery("DELETE FROM ICommand").executeUpdate();
 
 			transaction.commit();
 		} catch (HibernateException e) {
 			if (transaction != null && transaction.isActive()) transaction.rollback(); 
 			this.logger.error("Error while reseting queue.",e);
 		}
 	}
 
 	/**
 	 * A {@link Thread} to read {@link ICommand commands} from the {@link CommandDB#db}
 	 * and to write it into the {@link CommandDB#sink data-sink}
 	 */
 	class Writer extends Thread {
 		public Writer() {
 			super("CommandDB.Writer");
 		}
 
 		@Override
 		public void run() {
 			try {
 				synchronized (this) {
 					while (CommandDB.this.sink == null) this.wait();
 				}			
 
 				List<ICommand> commands = null;
 				while(!Thread.currentThread().isInterrupted()) {
 					commands = CommandDB.this.fetchNextCommands(0,10);
 					if (commands != null && commands.size() > 0) {
 						for (ICommand command : commands) {
 //							System.out.println(CommandDB.this.isKnown(command.getLocation()));
 							CommandDB.this.sink.putData(command);
 //							commandToXML(command);
 						} 
 					} else {
 						// sleep for a while
 						synchronized (this) {
 							this.wait(MAX_IDLE_SLEEP);	
 						}						
 					}
 				}
 			} catch (Exception e) {
 				if (!(e instanceof InterruptedException)) {
 					logger.error(String.format("Unexpected '%s' while waiting reading commands from db.",
 							e.getClass().getName()
 					),e);
 				}
 			} finally {
 				logger.info("CommandDB.Writer shutdown finished.");
 			}		
 		}
 
 		public synchronized void signalNewDbData() {
 			this.notify();
 		}
 	}
 
 	/**
 	 * A {@link Thread} to read {@link ICommand commands} from the {@link CommandDB#source data-source}
 	 * and to write it into the {@link CommandDB#db}.
 	 */	
 	class Reader extends Thread {
 		public Reader() {
 			super("CommandDB.Reader");
 		}
 
 		@Override
 		public void run() {
 			try {
 
 				synchronized (this) {
 					while (CommandDB.this.source == null) this.wait();
 				}		
 
 				while(!Thread.currentThread().isInterrupted()) {
 					ICommand command = CommandDB.this.source.getData();
 					if (command != null) {
 						// store data into db
 						CommandDB.this.storeCommand(command);
 //						CommandDB.this.commandToXML(command);						
 					}
 				}				
 			} catch (Exception e) {
 				if (!(e instanceof InterruptedException)) {
 					logger.error(String.format("Unexpected '%s' while waiting for a new command.",
 							e.getClass().getName()
 					),e);
 				}
 			} finally {
 				logger.info("CommandDB.Reader shutdown finished.");
 			}
 		}
 	}
 }
