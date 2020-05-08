 package me.Jaryl.FoundBoxx.SQLwrapper.Threads;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.URL;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 
 import me.Jaryl.FoundBoxx.FoundBoxx;
 
 public class SQLLoad extends Thread {
 	private FoundBoxx plugin;
 	
 	private String url;
 	private int port;
 	private String database;
 	private String prefix;
 	private String user;
 	private String pass;
 	
 	public SQLLoad(FoundBoxx plugin, String url, int port, String database, String prefix, String user, String pass)
 	{
 		this.plugin = plugin;
 		this.url = url;
 		this.port = port;
 		this.database = database;
 		this.prefix = prefix;
 		this.user = user;
 		this.pass = pass;
 	}
 	
 	public void run()
 	{
 		try {
 			plugin.sql.Stop();
 
 			DriverManager.setLoginTimeout(0);
 			
 			if (plugin.useSQL.equalsIgnoreCase("h2"))
 			{
 				if (!checkLibrary()) {
 					System.out.println("[" + plugin.getDescription().getFullName() + "] Downloading H2 library file...");
 					downloadLibrary();
 					
 					return;
 				}
 				
 				try {
 					Class.forName("org.h2.Driver");
 				} catch (ClassNotFoundException e) {
 					if (checkLibrary())
 					{
 						File file = new File("lib/h2.jar");
 						file.delete();
 						
 						System.out.println("[" +plugin.getDescription().getFullName() + "] Corrupted H2 library file, attempting to redownload.");
 						downloadLibrary();
 						return;
 					}
 				}
 				
 				plugin.sql.conn = DriverManager.getConnection("jdbc:h2:" + plugin.getDataFolder() + File.separator + prefix + "-log;IGNORECASE=TRUE");
 			}
 			else
 			{
 				plugin.sql.conn = DriverManager.getConnection("jdbc:mysql://" + url + ":" + port + "/" + database, user, pass);
 			}
 			
 			plugin.sql.conn.setAutoCommit(true);
 			
 			if (plugin.sql.conn != null)
 			{
 				plugin.sql.dataQuery("CREATE TABLE IF NOT EXISTS `" + prefix + "-log` (`date` datetime NOT NULL, `player` longtext NOT NULL, `block_id` smallint NOT NULL, `x` int NOT NULL, `y` int NOT NULL, `z` int NOT NULL)");
 				plugin.sql.dataQuery("CREATE TABLE IF NOT EXISTS `" + prefix + "-placed` (`x` int NOT NULL, `y` int NOT NULL, `z` int NOT NULL)");
 			}
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
		
		this.stop();
 	}
 	
 	private boolean checkLibrary() {
 		File file = new File("lib/h2.jar");
 		return file.exists() && !file.isDirectory();
 	}
 	private void downloadLibrary() {
 		plugin.needRestart = true;
 		
 		File dir = new File("lib");
 		if (!dir.exists())
 			dir.mkdir();
 
 		File file = new File(dir, "h2.jar");
 		BufferedInputStream input = null;
 		FileOutputStream output = null;
 
 		try {
 			URL url = new URL("http://www.h2database.com/automated/h2-latest.jar");
 			input = new BufferedInputStream(url.openStream());
 			output = new FileOutputStream(file);
 
 			byte data[] = new byte[1024];
 			int count;
 
 			while ((count = input.read(data)) != -1)
 				output.write(data, 0, count);
 		} catch (IOException e) {
 			System.out.println("[" + plugin.getDescription().getFullName() + "] Could not downloaded H2 library file! Trying again...");
 			downloadLibrary();
 		} finally {
 			try {
 				if (input != null)
 					input.close();
 			} catch (IOException e) {}
 
 			try {
 				if (output != null)
 					output.close();
 			} catch (IOException e) {}
 		}
 		
 		System.out.println("[" +plugin.getDescription().getFullName() + "] H2 download complete! Restart server to complete process!");
 	}
 }
