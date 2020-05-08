 package com.theminequest.MineQuest;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStreamWriter;
 import java.io.Writer;
 import java.sql.ResultSet;
 import java.util.NoSuchElementException;
 import java.util.Scanner;
 import java.util.logging.Logger;
 
 import lib.PatPeter.SQLibrary.DatabaseHandler;
 import lib.PatPeter.SQLibrary.H2;
 import lib.PatPeter.SQLibrary.MySQL;
 import lib.PatPeter.SQLibrary.SQLite;
 

 public class SQLExecutor {
 
 	private enum Mode{
 		MySQL, SQlite, H2;
 	}
 	private Mode databasetype;
 	private DatabaseHandler db;
 	private File datafolder;
 
 	public SQLExecutor(){
 		MineQuest.log("Loading and connecting to SQL...");
 		PropertiesFile config = MineQuest.configuration.databaseConfig;
 		String dbtype = config.getString("db_type","h2");
 		if (dbtype.equalsIgnoreCase("mysql"))
 			databasetype = Mode.MySQL;
 		else if (dbtype.equalsIgnoreCase("sqlite"))
 			databasetype = Mode.SQlite;
 		else
 			databasetype = Mode.H2;
 		MineQuest.log("[SQL] Using "+databasetype.name()+" as database.");
 		String hostname = config.getString("db_hostname","localhost");
 		String port = config.getString("db_port","3306");
 		String databasename = config.getString("db_name","minequest");
 		String username = config.getString("db_username","root");
 		String password = config.getString("db_password","toor");
 		if (databasetype == Mode.MySQL)
 			db = new MySQL(Logger.getLogger("Minecraft"),"mq_",hostname,port,databasename,username,password);
 		else if (databasetype == Mode.SQlite)
 			db = new SQLite(Logger.getLogger("Minecraft"),"mq_","minequest",MineQuest.activePlugin.getDataFolder().getAbsolutePath());
 		else
 			db = new H2(Logger.getLogger("Minecraft"),"mq_","minequest",MineQuest.activePlugin.getDataFolder().getAbsolutePath());
 		datafolder = new File(MineQuest.activePlugin.getDataFolder().getAbsolutePath()+File.separator+"sql");
 		checkInitialization();
 	}
 
 	private void checkInitialization() {
 		if (!datafolder.exists()){
 			datafolder.mkdir();
 		}
 		File versionfile = new File(datafolder+File.separator+"version");
 		Scanner s = null;
 		String lastv = null;
 		try {
 			s = new Scanner(versionfile);
 			lastv = s.nextLine();
 		} catch (FileNotFoundException e) {
 			try {
 				versionfile.createNewFile();
 			} catch (IOException e1) {
 				throw new RuntimeException(e1);
 			}
 		}
 		if (lastv==null || lastv.compareTo(MineQuest.getVersion())!=0){
 			if (lastv==null)
 				lastv = "initial";
 			querySQL("update/"+lastv,"");
 			Writer out;
 			try {
 				out = new OutputStreamWriter(new FileOutputStream(versionfile));
 				out.write(MineQuest.getVersion());
 				out.close();
 			} catch (FileNotFoundException e) {
 				// never going to happen
 			} catch (IOException e) {
 				// never going to happen either
 			}
 		}
 
 	}
 
 	public DatabaseHandler getDB(){
 		return db;
 	}
 
 	/**
 	 * Query an SQL and return a {@link java.sql.ResultSet} of the result.
 	 * If the SQL file contains {@code %s} in the query, the parameters
 	 * specified will replace {@code %s} in the query. Remember that if the query
 	 * is not a {@code SELECT} query, this will ALWAYS return null.
 	 * @param queryfilename sql filename to use
 	 * @param params parameters for sql file
 	 * @return ResultSet of SQL query (or null... if there really is nothing good.)
 	 */
 	public ResultSet querySQL(String queryfilename, String ...params) {
		InputStream i = MineQuest.activePlugin.getResource(queryfilename+".sql");
 		if (i==null)
 			throw new NoSuchElementException("No such resource: " + queryfilename + ".sql");
 		String[] filecontents = convertStreamToString(i).split("\n");
 		for (String line : filecontents){
 			// ignore comments
 			if (!line.startsWith("#") || !line.equals("")){
 				if (params!=null && params.length!=0){
 					int paramsposition = 0;
 					while (paramsposition<params.length && line.contains("%s")){
 						line = line.replaceFirst("%s", params[paramsposition]);
 					}
 				}
 				ResultSet result = db.query(line);
 				if (result!=null)
 					return result;
 			}
 		}
 		return null;
 	}
 
 	private String convertStreamToString(InputStream is) {
 		try {
 			return new java.util.Scanner(is).useDelimiter("\\A").next();
 		} catch (java.util.NoSuchElementException e) {
 			return "";
 		}
 	}
 
 }
