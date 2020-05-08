 package com.shadowolf.plugins.scheduled;
 
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 
 import com.shadowolf.plugins.ScheduledDBPlugin;
 import com.shadowolf.user.PeerListFactory;
 
 public class PeerCountsUpdater extends ScheduledDBPlugin {
 	private final static boolean DEBUG = false;
 	protected final static Logger LOGGER = Logger.getLogger(PeerCountsUpdater.class);
 	protected final static String DATABASE_NAME = "java:comp/env/jdbc/database";
 
 	private final String table;
 	private final String seederC;
 	private final String leecherC;
 	private final String infoC;
 
 	public PeerCountsUpdater(final Map<String, String> attributes) {
 		this.table = attributes.get("table");
 		this.seederC = attributes.get("seeder_column");
 		this.leecherC = attributes.get("leecher_column");
 		this.infoC = attributes.get("info_hash_column");
 	}
 
 	@Override
 	public void run() {
 		try {
 			final HashMap<byte[], int[]> updates = PeerListFactory.getPeerCounts();
 			this.prepareStatement("UPDATE " + this.table + " SET " + this.seederC + "=0, " + this.leecherC + "=0").execute();
 
 			final PreparedStatement updateStmt = this.prepareStatement("UPDATE " + this.table + " SET "
 					+ this.seederC + "= ? , " + this.leecherC + "= ? WHERE " + this.infoC + "= ?");
 
 			if(updateStmt == null) {
 				LOGGER.error("Could not prepare statement!");
 				return;
 			}
 
 			try {
 				if(DEBUG) {
 					LOGGER.debug("Pushing stats for " + updates.size() + " torrents.");
 				}
 
 				final Iterator<byte[]> iter = updates.keySet().iterator();
 				while(iter.hasNext()) {
 					final byte[] next = iter.next();
 					if(DEBUG) {
 						LOGGER.debug("Updating torrent with Seeders: " + updates.get(next)[0]);
 						LOGGER.debug("Updating torrent with Leechers: " + updates.get(next)[1]);
 					}
 
 					updateStmt.setInt(1, updates.get(next)[0]);
 					updateStmt.setInt(2, updates.get(next)[1]);
 					updateStmt.setBytes(3, next);
 					updateStmt.execute();
 
 					if(DEBUG) {
 						LOGGER.debug("Updated torrent with Seeders: " + updates.get(next)[0]);
 						LOGGER.debug("Updated torrent with Leechers: " + updates.get(next)[1]);
 					}

					updateStmt.close();
 				}
 
 				this.commit();
 			} finally {
 				updateStmt.close();
 			}
 		} catch (final SQLException e) {
			LOGGER.error("Failed query! " + e.getMessage());
 			this.rollback();
 		}
 	}
 
 }
