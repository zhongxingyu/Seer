 package darep.server;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.Properties;
 
 import darep.Command;
 import darep.Command.ActionType;
 import darep.logger.Logger;
 import darep.repos.Repository;
 import darep.repos.RepositoryException;
 
 public class Server {
 	
 	private Repository repository;
 	private Logger serverLogger;
 	private Properties properties = new Properties();
 	private static final String[] propertyNames = {"incoming-directory", "html-overview", "log-file", 
 											"checking-interval-in-seconds", "completeness-checker.class-name" };
 	private final String[] propertyValues;
 	
 	private static final int INCOMING_DIRECTORY = 0;
 	private static final int HTML_OVERVIEW = 1;
 	private static final int LOG_FILE = 2;
 	private static final int CHECKING_INTERVAL_IN_SECONDS = 3;
 	private static final int COMPLETENESS_CHECKER_CLASS_NAME = 4;
 	
 	private boolean running = true;
 	
 	public Server(Repository repository, Command command) throws ServerException {
 		
 		propertyValues = new String[] {
 				null,
 				null,
 				new File(repository.getLocation(), "server.log").toString(),
 				null,
 				null
 		};
 		
 		this.repository = repository;
 		loadPropertiesFile(command.getParams()[0]);
 		repository.getLogger().logSuccess("Data Repository Server successfully started");
 		serverLogger = new ServerLogger(getProperty(LOG_FILE));
 		repository.setLogger(serverLogger);
 	}
 	
 	public void start() {
 		try {
 			int seconds = Integer.parseInt(getProperty(CHECKING_INTERVAL_IN_SECONDS));
 			if(seconds < 0) {
 				throw new ServerException("checking-interval-in-seconds property must be bigger 1 or bigger");
 			}
 			while(running) {
 				File[] files = checkIncomingDirectory();
 				for(File f : files) {
 					try {
 						addFile(f); 
 					} catch (RepositoryException e) {
 						throw new ServerException("server could not add file " + f.getPath());
 					}
 				}
 				try {
 					Thread.sleep(seconds*1000);
 				} catch (InterruptedException e) {
 					throw new ServerException(e);
 				}
 			}
 		} catch (ServerException e) {
 			// TODO the Application need to return 1 in case of failure. Maybe better make one static logger in darep controller?
 			serverLogger.logError(e.getMessage());
 		}
 	}
 	
 	private String getProperty(int propertyKey) {
 		return properties.getProperty(propertyNames[propertyKey], propertyValues[propertyKey]);
 	}
 	
 	private void loadPropertiesFile(String propertiesPath) throws ServerException {
 		try {
			FileInputStream is = new FileInputStream(propertiesPath);
			try {
				properties.load(is);
			} finally {
				is.close();
			}
 			for(int i = 0; i < propertyNames.length; i++) {
 				if(getProperty(i) == null) {
 					throw new ServerException("Missing property " + propertyNames[i] + " in the property file " + propertiesPath);
 				}
 			}
 		} catch (FileNotFoundException e) {
 			throw new ServerException("could not find properties file " + propertiesPath, e);
 		} catch (IOException e) {
 			throw new ServerException("could not read properties file " + propertiesPath, e);
 		}
 	}
 	
 	private File[] checkIncomingDirectory() {
 		File dir = new File(getProperty(INCOMING_DIRECTORY));
 		// TODO implement completeness-checker
 		File[] files = dir.listFiles();
 		if(files != null) {
 			return files;
 		} else {
 			return new File[0];
 		}
 	}
 	
 	private void addFile(File file) throws RepositoryException {
 		Command command = new Command(ActionType.add, new String[] {file.getPath()}, null, new String[] {"m"});
 		this.repository.add(command);
 	}
 }
