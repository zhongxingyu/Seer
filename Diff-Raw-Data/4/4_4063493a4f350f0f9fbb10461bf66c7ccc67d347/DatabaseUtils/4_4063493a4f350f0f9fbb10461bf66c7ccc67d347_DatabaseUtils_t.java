 package hu.documaison.dal.database;
 
 import hu.documaison.data.entities.*;
 import hu.documaison.settings.SettingsData;
 import hu.documaison.settings.SettingsManager;
 
 import java.sql.SQLException;
 
 import com.j256.ormlite.jdbc.JdbcConnectionSource;
 import com.j256.ormlite.support.ConnectionSource;
 import com.j256.ormlite.table.TableUtils;
 
 public class DatabaseUtils {
	private static String databaseUrl = "jdbc:sqlite:d:/temp/documaison.sqlite";
	private static final String databaseUrlTemplate = "jdbc:sqlite:%path%";
 
 	static
 	{
 		loadDatabasePath();
 	}
 	
 	private static void loadDatabasePath()
 	{
 		SettingsData settingsData;
 		try {
 			settingsData = SettingsManager.getCurrentSettings();
 			String path = settingsData.getDatabaseFileLocation().replace('\\', '/');
 			databaseUrl = databaseUrlTemplate.replaceFirst("%path%", path);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public static ConnectionSource getConnectionSource() throws SQLException {
 		ConnectionSource connectionSource = new JdbcConnectionSource(
 				databaseUrl);
 		return connectionSource;
 	}
 
 	// public static <X extends Object> Dao<X, String> getDao(ConnectionSource
 	// connectionSource)
 	// {
 	// // instantiate the dao
 	// Dao<X, String> dao =
 	// DaoManager.createDao(connectionSource, X.class);
 	// return dao;
 	// }
 
 	public static void createTables() throws SQLException {
 		ConnectionSource connectionSource = getConnectionSource();
 		try {
 			// the order of creation is important because of the foreign keys!
 			TableUtils.createTable(connectionSource, Tag.class);
 			TableUtils.createTable(connectionSource, Comment.class);
 			TableUtils.createTable(connectionSource, Metadata.class);
 			TableUtils.createTable(connectionSource, DefaultMetadata.class);
 			TableUtils.createTable(connectionSource, DocumentType.class);
 			TableUtils.createTable(connectionSource, Document.class);
 			TableUtils.createTable(connectionSource, DocumentTagConnection.class);
 		} finally {
 			connectionSource.close();
 		}
 	}
 
 	public static void createTablesBestEffort() {
 		// the order of creation is important because of the foreign keys!
 		tryCreateTable(Tag.class);
 		tryCreateTable(Comment.class);
 		tryCreateTable(Metadata.class);
 		tryCreateTable(DefaultMetadata.class);
 		tryCreateTable(DocumentType.class);
 		tryCreateTable(Document.class);
 		tryCreateTable(DocumentTagConnection.class);
 	}
 
 	private static <T> void tryCreateTable(Class<T> tableClass) {
 		ConnectionSource connectionSource = null;
 		try {
 			connectionSource = getConnectionSource();
 			TableUtils.createTable(connectionSource, tableClass);
 		} catch (SQLException e) {
 			// best effort manner
 		} finally {
 			try {
 				if (connectionSource != null) {
 					connectionSource.close();
 				}
 			} catch (SQLException e) {
 				// best effort manner
 			}
 		}
 	}
 }
