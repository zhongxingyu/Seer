 package ch.cern.atlas.apvs.server;
 
 import java.beans.PropertyVetoException;
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 
 import javax.sql.DataSource;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ch.cern.atlas.apvs.client.settings.ServerSettings;
 import ch.cern.atlas.apvs.eventbus.shared.RemoteEventBus;
 
 import com.mchange.v2.c3p0.ComboPooledDataSource;
 
 public class DbCallback {
 	private Logger log = LoggerFactory.getLogger(getClass().getName());
 
 	private ComboPooledDataSource datasource;
 	private ExecutorService executorService;
 	private Future<?> connectFuture;
 	private Future<?> disconnectFuture;
 
 	private RemoteEventBus eventBus;
 
 	private final static boolean LOG_DB = false;
 
 	public DbCallback(RemoteEventBus eventBus) {
 		this.eventBus = eventBus;
 		executorService = Executors.newSingleThreadExecutor();		
 	}
 
 	public void dbConnected(DataSource datasource) throws SQLException {
 	}
 
 	public void dbDisconnected() throws SQLException {
 	}
 
 	public void exceptionCaught(Exception e) {
 	}
 
 	public boolean isConnected() {
 		return datasource != null;
 	}
 
 	public boolean checkConnection() {
 		return isConnected();
 	}
 
 	public void connect(final String url) {
 		disconnect();
 
 		connectFuture = executorService.submit(new Runnable() {
 
 			@Override
 			public void run() {
 				try {
 					datasource = new ComboPooledDataSource();
 				
 					datasource
 							.setDriverClass(LOG_DB ? "net.sf.log4jdbc.DriverSpy"
 									: "oracle.jdbc.OracleDriver");
 					int pos = url.indexOf("@//");
 					String pwd = ServerSettingsStorage.getInstance(eventBus)
 							.getPasswords()
 							.get(ServerSettings.Entry.databaseUrl.toString());
 					if (pos >= 0 && pwd != null && !pwd.equals("")) {
 						String shortUrl = url.substring(pos);
 						String user = url.substring(0, pos);
 						System.err.println("Loging in to " + user + " "
 								+ shortUrl + " " + pwd);
 
 						datasource.setJdbcUrl("jdbc:"
 								+ (LOG_DB ? "log4jdbc:" : "") + "oracle:thin:"
 								+ shortUrl);
 						datasource.setUser(user);
 						datasource.setPassword(pwd);
 
 						// FIXME check if this helps...
 						datasource.setMaxStatementsPerConnection(30);
 
 						dbConnected(datasource);
 
 					} else {
 						log.warn("Username 'user@//' not found in " + url);
 					}
 				} catch (SQLException e) {
 					exceptionCaught(e);
 				} catch (PropertyVetoException e) {
 					exceptionCaught(e);
 				}
 			}
 		});
 
 	}
 
 	public void disconnect() {
 		if (connectFuture != null) {
 			connectFuture.cancel(true);
 		}
 
 		if (datasource == null) {
 			return;
 		}
 
 		if (disconnectFuture != null) {
 			return;
 		}
 
 		disconnectFuture = executorService.submit(new Runnable() {
 
 			@Override
 			public void run() {
 				try {
 					datasource.close();
 					datasource = null;
 					dbDisconnected();
 				} catch (SQLException e) {
 					exceptionCaught(e);
 				}
 			}
 		});
 	}
 
 	protected Connection getConnection() throws SQLException {
		if (datasource == null || datasource.getPassword() == null) {
 			throw new SQLException("No Datasource (yet)");
 		}
 		return datasource.getConnection();
 	}
 }
