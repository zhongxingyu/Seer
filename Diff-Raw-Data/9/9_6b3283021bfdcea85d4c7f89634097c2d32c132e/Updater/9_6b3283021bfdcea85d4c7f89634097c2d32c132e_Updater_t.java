 package com.robonobo.core.update;
 
 import static com.robonobo.common.util.FileUtil.*;
 
 import java.io.*;
 import java.lang.reflect.Method;
 import java.sql.*;
 import java.util.regex.Pattern;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.robonobo.common.exceptions.SeekInnerCalmException;
 import com.robonobo.core.service.DbService;
 
 /**
  * Updates the rbnb home dir to the format expected by a new version
  * 
  * @author macavity
  * 
  */
 @SuppressWarnings("unused")
 public class Updater {
 	static final int CURRENT_VERSION = 2;
 	private File homeDir;
 	Log log = LogFactory.getLog(getClass());
 
 	public Updater(File homeDir) {
 		this.homeDir = homeDir;
 	}
 
 	public void runUpdate() throws IOException {
 		int v = getVersion();
 		if (v < CURRENT_VERSION) {
 			while (v < CURRENT_VERSION) {
 				int nextV = v + 1;
 				Method m;
 				try {
 					m = Updater.class.getDeclaredMethod("updateVersion" + v + "ToVersion" + nextV);
 					m.invoke(this);
 				} catch (Exception e) {
 					throw new SeekInnerCalmException(e);
 				}
 				v = nextV;
 			}
 			saveVersion();
 		}
 	}
 
 	private int getVersion() throws IOException {
 		File versionFile = new File(homeDir, "version");
 		if (!versionFile.exists())
 			return 0;
 		FileInputStream fis = new FileInputStream(versionFile);
 		byte[] buf = new byte[16];
 		int numRead = fis.read(buf);
 		String versionStr = new String(buf, 0, numRead);
 		fis.close();
 		return Integer.parseInt(versionStr);
 	}
 
 	private void saveVersion() throws IOException {
 		File versionFile = new File(homeDir, "version");
 		FileOutputStream fos = new FileOutputStream(versionFile);
 		String versionStr = String.valueOf(CURRENT_VERSION);
 		fos.write(versionStr.getBytes());
 		fos.close();
 	}
 
 	private void updateVersion0ToVersion1() {
 		log.info("Updating robohome dir " + homeDir.getAbsolutePath() + " from version 0 to version 1");
 		// Just delete the config and db dirs - a bit lazy, but for version 0 it should be ok
 		File configDir = new File(homeDir, "config");
 		deleteDirectory(configDir);
 		File dbDir = new File(homeDir, "db");
 		deleteDirectory(dbDir);
 		// log4j props file name changed
 		File log4jFile = new File(homeDir, "robonobo-log4j.properties");
 		log4jFile.delete();
 	}
 
 	private void updateVersion1ToVersion2() {
 		log.info("Updating robohome dir " + homeDir.getAbsolutePath() + " from version 1 to version 2");
 		// Remove config setting
 		try {
 			removeConfigSetting("mina", "bidStrategyClass");
 		} catch (IOException e) {
 			log.error("Caught ioexception removing config setting - oh noes!", e);
 		}
 		// Update db
 		String[] sqlArr = { "DROP TABLE playlist_seen_sids", DbService.CREATE_PLAYLIST_SEEN_SIDS_TBL, DbService.CREATE_LIBRARY_TRACKS_TBL,
 				DbService.CREATE_LIBRARY_SEEN_SIDS_TBL, DbService.CREATE_LIBRARY_LAST_CHECKED_TBL };
 		try {
 			updateMetadataDb(sqlArr);
 		} catch (SQLException e) {
 			log.error("Caught sqlexception updating metadata db - oh noes!", e);
 		}
 	}
 
 	private void removeConfigSetting(String cfgName, String settingName) throws IOException {
 		File configDir = new File(homeDir, "config");
 		File cfgFile = new File(configDir, cfgName+".cfg");
 		if(!cfgFile.exists()) {
 			log.info("Not updating config "+cfgName+" - config file does not exist!");
 			return;
 		}
 		log.info("Removing setting "+settingName+" from config "+cfgName);
 		Pattern p = Pattern.compile("^"+settingName+"=.*$");
 		File tmpFile = File.createTempFile("robo", "cfg");
 		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(cfgFile)));
 		PrintWriter out = new PrintWriter(tmpFile);
 		String line;
 		while((line = in.readLine()) != null) {
 			if(!p.matcher(line).matches())
 				out.println(line);
 		}
 		in.close();
 		out.close();
 		cfgFile.delete();
 		tmpFile.renameTo(cfgFile);
 	}
 	
 	private void updateMetadataDb(String[] sqlStatements) throws SQLException {
 		String sep = File.separator;
 		String dbPrefix = homeDir.getAbsolutePath() + sep + "db" + sep + "metadata";
 		try {
 			Class.forName("org.hsqldb.jdbcDriver");
 		} catch (ClassNotFoundException e) {
 			throw new RuntimeException(e);
 		}
 		String dbUrl = "jdbc:hsqldb:file:" + dbPrefix;
 		File dbPropsFile = new File(dbPrefix + ".properties");
 		if (dbPropsFile.exists()) {
 			log.info("Updating metadata db with "+sqlStatements.length+" statements");
 			Connection conn = DriverManager.getConnection(dbUrl, "sa", "");
 			for (String sql : sqlStatements) {
 				log.debug("Running: "+sql);
 				try {
 					Statement st = conn.createStatement();
 					st.executeUpdate(sql);
 					st.close();
 				} catch (SQLException e) {
 					throw new RuntimeException(e);
 				}
 			}
 			Statement st = conn.createStatement();
			st.executeUpdate("SHUTDOWN");
 			st.close();
 		} else
 			log.info("metadata db props does not exist - not updating metadata db");
 	}
 }
