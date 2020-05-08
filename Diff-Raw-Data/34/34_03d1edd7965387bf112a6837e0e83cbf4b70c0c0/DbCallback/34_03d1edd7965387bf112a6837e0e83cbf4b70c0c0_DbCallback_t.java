 package ch.cern.atlas.apvs.server;
 
 import java.sql.Connection;
 import java.sql.Driver;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 
 public class DbCallback {
 
 	private Driver driver;
 	private Connection connection;
 	private ExecutorService executorService;
 	private Future<?> connectFuture;
 	private Future<?> disconnectFuture;
	
	private final static boolean LOG_DB = false;
 
 	public DbCallback() {
 		executorService = Executors.newSingleThreadExecutor();
 	}
 
 	public void dbConnected(Connection connection) throws SQLException {
 	}
 
 	public void dbDisconnected() throws SQLException {
 	}
 
 	public void exceptionCaught(Exception e) {
 	}
 
 	public boolean isConnected() {
 		return connection != null;
 	}
 
 	public void connect(final String url) {		
 		disconnect();
 		
 		connectFuture = executorService.submit(new Runnable() {
 			
 			@Override
 			public void run() {
 				try {
 					if (driver == null) {
						driver = LOG_DB ? new net.sf.log4jdbc.DriverSpy() :new oracle.jdbc.OracleDriver();
 						DriverManager.registerDriver(driver);
 					}
					connection = DriverManager.getConnection("jdbc:"+(LOG_DB ? "log4jdbc:" : "")+"oracle:thin:"+url);
 					dbConnected(connection);
 				} catch (SQLException e) {
 					exceptionCaught(e);
 				}
 			}
 		});
 		
 	}
 
 	public void disconnect() {
 		if (connectFuture != null) {
 			connectFuture.cancel(true);
 		}
 		
 		if (connection == null) {
 			return;
 		}
 		
 		if (disconnectFuture != null) {
 			return;
 		}
 		
 		disconnectFuture = executorService.submit(new Runnable() {
 			
 			@Override
 			public void run() {
 				try {
 					connection.close();
 					connection = null;
 					dbDisconnected();
 				} catch (SQLException e) {
 					exceptionCaught(e);
 				}
 			}
 		});
 	}
 
 	protected Connection getConnection() {
 		return connection;
 	}
 }
