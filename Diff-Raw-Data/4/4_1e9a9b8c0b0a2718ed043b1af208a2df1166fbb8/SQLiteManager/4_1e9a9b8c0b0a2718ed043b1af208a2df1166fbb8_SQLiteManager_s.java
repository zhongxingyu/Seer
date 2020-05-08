 package net.shiroumi.central.Databases;
 
 import java.io.File;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.sql.SQLException;
 
 import net.shiroumi.central.CentralCore;
 import net.shiroumi.central.Util.Util;
 
 public class SQLiteManager extends DatabaseManager {
 
 	/**
 	 * Connect SQLite Database
 	 * @param par2File Database File
 	 * @throws SQLException
 	 */
 	public static void connect(String par2File) throws SQLException {
 		loadFile();
 		connect(SQLType.SQLite, par2File, null, null, null);
 	}
 
 	private static void loadFile() {
 		File f = new File(CentralCore.getInstance().getDataFolder(), "sqlite.jar");
 		if(!f.exists()) {
 			Util.copyFileFromJar(f, "sqlite.jar", true);
 		}
 		try {
 			URL[] jarURL = {f.toURI().toURL()};
 			URLClassLoader classloader = URLClassLoader.newInstance(jarURL);
 			classloader.loadClass(SQLType.SQLite.getDriver());
		} catch (MalformedURLException | ClassNotFoundException e) {
 			e.printStackTrace();
 		}
 	}
 }
