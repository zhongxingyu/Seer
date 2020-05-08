 package com.shadowolf.plugins.scheduled;
 
 import java.sql.Blob;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.concurrent.ConcurrentSkipListSet;
 
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 import javax.sql.DataSource;
 
 import org.apache.log4j.Logger;
 import org.xml.sax.Attributes;
 
 import com.shadowolf.plugins.ScheduledPlugin;
 import com.shadowolf.tracker.AnnounceException;
 import com.shadowolf.tracker.TrackerResponse;
 import com.shadowolf.tracker.TrackerRequest.Event;
 import com.shadowolf.util.Data;
 
 public class InfoHashEnforcer extends ScheduledPlugin {
 	protected final static String DATABASE_NAME = "java:comp/env/jdbc/database";
 	protected final static Logger LOGGER = Logger.getLogger(InfoHashEnforcer.class);
 	protected final Object hashLock = new Object();
 	private final String column;
 	
 	protected ConcurrentSkipListSet<String> hashes = new ConcurrentSkipListSet<String>();
 	private PreparedStatement stmt;
 	private Connection conn;
 	
 	public InfoHashEnforcer(Attributes attributes) {
 		super(attributes);
 		
 		String table = attributes.getValue("table");
 		this.column = attributes.getValue("info_hash_column");
 		
 		
 		try {
 			DataSource source = (DataSource) (new InitialContext().lookup(DATABASE_NAME));
 			this.conn = source.getConnection();
 			conn.setAutoCommit(false);
 			
 			this.stmt = conn.prepareStatement("SELECT " + column + " FROM " + table);
 		} catch (NamingException n) {
 			LOGGER.error("Unexpected NamingException...");
 		} catch (SQLException e) {
 			LOGGER.error("Unexpected SQLException..." + e.getMessage() + "\t Cause: " + e.getCause().getMessage());
 		}
 		
 		
 	}
 	
 	@Override
 	public int getInitialDelay() {
 		return 0;
 	}
 	
 	@Override
 	public boolean needsAnnounce() {
 		return true;
 	}
 	
 	@Override
 	public void run() {
 		
 		try {
 			this.stmt.execute();
 			ResultSet rs = this.stmt.getResultSet();
 			while(rs.next()) {
 				final Blob b = rs.getBlob(this.column);
 				final byte[] bs = b.getBytes(1l, (int) b.length());
 			
 				hashes.add(Data.byteArrayToHexString(bs));
 				b.free();
 			}
 			
 			rs.close();
 			LOGGER.debug("Read " + this.hashes.size() + " info_hashes");
 		} catch (SQLException e) {
 			LOGGER.error("Unexpected SQLException..." + e.getMessage() + "\t Cause: " + e.getCause().getMessage());
 		}
 		
 		LOGGER.debug("Read " + this.hashes.size());
 	}
 	
 	@Override
 	public void doAnnounce(Event e, long uploaded, long downloaded, String passkey, String infoHash) throws AnnounceException{
 		if(this.hashes.contains(infoHash) == false) {
 			throw new AnnounceException(TrackerResponse.Errors.TORRENT_NOT_REGISTERED.toString());
 		}
 	}
 }
 
