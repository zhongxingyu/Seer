 package asl;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.postgresql.jdbc2.optional.PoolingDataSource;
 
 import asl.Persistence.DbPersistence;
 import asl.infrastructure.Bootstrapper2;
 import asl.infrastructure.MemoryLogger;
 import asl.infrastructure.exceptions.PersistenceException;
 
 public class Main {
 	static final MemoryLogger testLogger = new MemoryLogger(false /*output to console*/);
 	static final MemoryLogger applicationLogger = new MemoryLogger(false /*output to console*/);
 
 	public static void main(String[] args) throws Exception {		
 		testLogger.setLevel(Level.ALL);
		applicationLogger.setLevel(Level.WARNING);
 
 		// Read configuration file
 		ServerSettings settings = Bootstrapper2.StrapTheBoot(applicationLogger);
 		
 		PoolingDataSource connectionPool = new PoolingDataSource();
 		connectionPool.setDatabaseName(settings.DB_DATABASE_NAME);
 		connectionPool.setDataSourceName(settings.DB_DATA_SOURCE_NAME);
 		connectionPool.setUser(settings.DB_USERNAME);
 		connectionPool.setServerName(settings.DB_SERVER_NAME);
 		connectionPool.setPassword(settings.DB_PASSWORD);
 		connectionPool.setMaxConnections(settings.DB_MAX_CONNECTIONS);
 		
 		settings = parseArgs(args, settings);
 		settings.logSettings(applicationLogger);
 		
 		try {
 			System.out.println("Starting ThorBang MQ Server");
 
 			final ThorBangMQServer socketServer = ThorBangMQServer.build(settings, applicationLogger);
 			final IntervalLogger intervalLogger = new IntervalLogger(settings.LOG_INTERVAL, testLogger, Level.SEVERE);
 			
 			Thread t = new Thread(new Runnable() {
 				
 				@Override
 				public void run() {
 					socketServer.start();
 					
 				}
 			});
 			Thread intervalLoggerThread = new Thread(intervalLogger);
 			addShutdownHookForSavingLog((MemoryLogger) testLogger, settings.TEST_LOG_PATH);
 			addShutdownHookForSavingLog((MemoryLogger) applicationLogger, settings.APPLICATION_LOG_PATH);
 			
 			t.start();
 			intervalLoggerThread.start();
 			
 			System.out.println("ThorBangMQ Server Started.");
 			System.out.println("Commands:");
 			System.out.println("  d\t\tDump logfiles");
 			System.out.println("  q\t\tQuit");
 			System.out.println("  qd\t\tQuit and dump logfiles");
 			System.out.println("  resetdb\tClear the database");
 			System.out.println("  clearlogs\tClear the logs");
 			
 	        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
 	        String input = null;
 	        System.out.print(": ");
 	        
 			while((input = br.readLine()) != null){
 				if(input.startsWith("d ")){
 					dumpLog(settings.TEST_LOG_PATH, (MemoryLogger)testLogger);
 					dumpLog(settings.APPLICATION_LOG_PATH, (MemoryLogger)applicationLogger);
 				}
 				else if(input.startsWith("qd")){
 					dumpLog(settings.TEST_LOG_PATH, (MemoryLogger)testLogger);
 					dumpLog(settings.APPLICATION_LOG_PATH, (MemoryLogger)applicationLogger);					stopServer(socketServer, intervalLogger, t, intervalLoggerThread);
 					System.out.println("Bye :)");
 					break;
 				}
 				else if(input.equals("q")){
 					stopServer(socketServer, intervalLogger, t, intervalLoggerThread);
 					System.out.println("Bye :)");
 					break;
 				}
 				else if(input.equals("resetdb")){
 					System.out.println("Clearing db...");
 					clearDb(settings);
 					System.out.println("Db clean!");
 				}
 				else if(input.equals("clearlogs")){
 					applicationLogger.clear();
 					testLogger.clear();
 					System.out.println("Logs cleared!");
 					
 				}
 				else{
 					System.out.println("Unkown command '" + input + "'");
 				}
 		        System.out.print(": ");
 			}
 			
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private static ServerSettings parseArgs(String[] args, ServerSettings settings) {
 		if (args.length < 0) {
 			System.out.println("Optional argument: cleardb=true logpath=path");
 			return settings;
 		}
 
 		for(String arg : args)
 			parseArgument(arg, settings);
 		
 		return settings;
 	}
 	
 	private static void parseArgument(String arg, ServerSettings s){
 		if(arg.equals("cleardb=true"))
 		{
 			System.out.println("Clearing db...");
 			clearDb(s);
 			System.out.println("Db clean!");
 		}
 		else{
 			System.out.println("Unkown argument " + arg);
 		}
 	}
 	
 	private static void clearDb(ServerSettings settings){
 		PoolingDataSource connectionPool = (PoolingDataSource)PoolingDataSource.getDataSource(settings.DB_DATA_SOURCE_NAME);
 		if (connectionPool == null) {
 			connectionPool = new PoolingDataSource();
 			connectionPool.setDatabaseName(settings.DB_DATABASE_NAME);
 			connectionPool.setDataSourceName(settings.DB_DATA_SOURCE_NAME);
 			connectionPool.setUser(settings.DB_USERNAME);
 			connectionPool.setServerName(settings.DB_SERVER_NAME);
 			connectionPool.setPassword(settings.DB_PASSWORD);
 			connectionPool.setMaxConnections(settings.DB_MAX_CONNECTIONS);
 		}
 		
 		DbPersistence dbPersistence = new DbPersistence(connectionPool, null);
 		
 		try {
 			System.out.println("Deleting asl schema...");
 			dbPersistence.deleteSchema();
 		} catch (PersistenceException ignore) {
 			
 		}
 		System.out.println("Rebuilding asl schema...");
 		try {
 			dbPersistence.createSchema();
 			dbPersistence.buildSchema();
 		} catch (PersistenceException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private static void stopServer(ThorBangMQServer server, IntervalLogger logger, Thread serverThread, Thread intervalLoggerThread) throws InterruptedException{
 		System.out.println("Shutting interval logger...");
 		logger.stop();
 		
 		System.out.println("Shutting down server...");
 		server.stop();
 		
 		System.out.println("Waiting for server to stop...");
 		serverThread.join();	
 		intervalLoggerThread.join();
 	}
 	
 	private static void dumpLog(String path, MemoryLogger logger) throws FileNotFoundException{
 		((MemoryLogger)logger).dumpToFile(path);
 		System.out.println("Log file dumped to " + System.getProperty("user.dir") + "/" + path);
 	}
 	
 	private static void addShutdownHookForSavingLog(final MemoryLogger logger, final String pathToStoreLog){
 		Runtime.getRuntime().addShutdownHook(new Thread()
         {
             @Override
             public void run()
             {
                 try {
 					logger.dumpToFile(pathToStoreLog);
 				} catch (FileNotFoundException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
             }
         });
 	}
 
 }
