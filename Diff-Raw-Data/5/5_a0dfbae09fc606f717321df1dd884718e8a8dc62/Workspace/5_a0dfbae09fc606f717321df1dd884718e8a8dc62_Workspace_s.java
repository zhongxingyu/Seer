 package at.photoselector;
 
 import java.io.File;
 import java.sql.SQLException;
 import java.util.List;
 
 public class Workspace {
 
 	//################################ STATICS ################################
 
 	public final static int UNPROCESSED = 1;
 	public final static int ACCEPTED = 2;
 	public final static int DECLINED = 4;
 
 	private static Workspace instance;
 
 	public static void open(String path) {
 		instance = new Workspace(path);
 	}
 
 	public static void addPhoto(String path) {
 		File location = new File(path);
 		if (location.isDirectory()) {
 			for (File current : location.listFiles())
 				addPhoto(current.getAbsolutePath());
 		}
 		if (location.getName().matches(".*jpe?g$"))
 			try {
 				instance.db
 						.execute("INSERT INTO photos (path, status) VALUES ('"
 						+ location.getAbsolutePath() + "', " + UNPROCESSED
 						+ ")");
 			} catch (SQLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 	}
 
 	public static List<String> getPhotos(int filter) throws SQLException {
 		String sql = "SELECT path FROM photos WHERE stage IS NULL";
 		String tmp = "";
 		if ((UNPROCESSED & filter) > 0)
 			tmp += "status = " + UNPROCESSED + " OR ";
 		if ((ACCEPTED & filter) > 0)
 			tmp += "status = " + ACCEPTED + " OR ";
 		if ((DECLINED & filter) > 0)
 			tmp += "status = " + DECLINED + " OR ";
 		if (0 < tmp.length())
 			sql += " AND (" + tmp.substring(0, tmp.length() - 4) + ")";
 
 		return instance.db.getStringList(sql);
 	}
 
 	public static boolean accept(String path) {
 		try {
 			instance.db.execute("UPDATE photos SET status=" + ACCEPTED
 					+ " WHERE path='"
 					+ path + "'");
 
 			return isStageCompleted();
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return false;
 		}
 	}
 
 	public static boolean decline(String path) {
 		try {
 			instance.db.execute("UPDATE photos SET status=" + DECLINED
 					+ " WHERE path='"
 					+ path + "'");
 			return isStageCompleted();
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return false;
 		}
 	}
 
 	public static boolean isStageCompleted() throws SQLException {
 		if (0 == instance.db.getIntegerList(
				"SELECT pid FROM photos WHERE stage IS NULL AND status <> "
 						+ UNPROCESSED).size()) {
 			stageCompleted();
 			return true;
 		}
 		return false;
 	}
 
 	public static void stageCompleted() throws SQLException {
 		instance.db.execute("UPDATE photos SET stage="
 				+ Stage.getCurrent().getId() + " WHERE status="
 				+ DECLINED);
 		instance.db.execute("UPDATE photos SET status=" + UNPROCESSED
 				+ " WHERE status=" + ACCEPTED);
 	}
 
 	// ################################ NON-STATICS ################################
 
 	private Database db;
 
 	/**
 	 * Instantiates a new workspace. I. a. create a connection to the new
 	 * database after closing the old one.
 	 * 
 	 * @param path
 	 *            the absolut path to the database file
 	 */
 	private Workspace(String path) {
 		Database.closeConnection();
 		db = new Database(path);
 		Stage.init(db);
 	}
 }
