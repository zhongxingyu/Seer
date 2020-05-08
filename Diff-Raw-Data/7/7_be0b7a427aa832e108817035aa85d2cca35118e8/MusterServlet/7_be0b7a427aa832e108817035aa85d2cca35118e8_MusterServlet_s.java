 /*!
 * Muster v1.8.1
  * https://apps.education.ucsb.edu/redmine/projects/muster
  * 
  * Copyright (c) 2011, Justin Force
  * Licensed under the BSD 3-Clause License
  */
 
 package edu.education.ucsb.muster;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.io.UnsupportedEncodingException;
 import java.sql.Connection;
 import java.sql.Driver;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.util.LinkedList;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.lang.StringEscapeUtils;
 
 import com.google.gson.Gson;
 import com.google.gson.stream.JsonReader;
 import com.sidewaysmilk.justache.Justache;
 import com.sidewaysmilk.justache.JustacheKeyNotFoundException;
 
 import edu.education.ucsb.muster.MusterConfiguration.DatabaseDefinition;
 
 /**
  * Servlet implementation class MusterServlet
  */
 public class MusterServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 
 	private static final String confPath = "/WEB-INF/muster.conf.json";
 
 	private MusterConfiguration conf;
 
 	/**
 	 * If any of the files at these paths change, we should reinitialize the servlet.
 	 */
 	private static LinkedList<String> reloadFilePaths;
 
 	private static Justache<String, String> cache;
 
 	private static ExceptionQueue exceptions;
 
 	public void init() {
 
 		conf = loadConfiguration();
 
 		// Set reload paths
 		reloadFilePaths = new LinkedList<String>();
 		reloadFilePaths.add(confPath);
 		reloadFilePaths.add(conf.reloadFilePath);
 
 		// Keep a queue of exceptions that have occurred so that we can review it
 		exceptions = new ExceptionQueue(conf.exceptionQueueLength);
 
 		// Initialize cache
 		cache = getCache();
 	}
 
 	public void destroy() {
 		cache.die();
 	}
 
 	private Justache<String, String> getCache() {
 		return new Justache<String, String>(conf.cacheTTL, conf.cacheMaxLength);
 	}
 
 	private String testConnectivity(DatabaseDefinition db) {
 
 		// load driver
 		try {
 			DriverManager.getDriver(db.url);
 		} catch (SQLException e) {
 			try {
 				DriverManager.registerDriver((Driver) Class.forName(db.driver).getConstructor()
 						.newInstance((Object[]) null));
 			} catch (Exception e1) {
 				addException(e1, "A driver couldn't be loaded. Check the config file and try again. driver: `"
 						+ db.driver + "`, confPath: `" + confPath + "`");
 				return "FAIL";
 			}
 		}
 
 		// connect and test setReadOnly
 
 		// Add the connection to our list and try setting readOnly to test
 		Connection connection = null;
 		try {
 			connection = DriverManager.getConnection(db.url, db.username, db.password);
 			connection.setReadOnly(true);
 			connection.close();
 		} catch (Exception e) {
 			addException(e, "Setting readonly failed on " + db.url);
 			return e.toString();
 		}
 
 		return "OK";
 	}
 
 	private MusterConfiguration loadConfiguration() {
 
 		Gson gson = new Gson();
 		JsonReader reader = null;
 		MusterConfiguration loadedConf = null;
 
 		try {
 			reader = new JsonReader(new InputStreamReader(getServletContext().getResourceAsStream(confPath), "UTF-8"));
 		} catch (UnsupportedEncodingException e) {
 			addException(e, "Unsupported encoding");
 		} catch (NullPointerException e) {
 			addException(e, "Couldn't open config file `" + confPath + "`");
 		}
 
 		loadedConf = gson.fromJson(reader, MusterConfiguration.class);
 		loadedConf.lastLoaded = System.currentTimeMillis();
 		return loadedConf;
 	}
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 
 		if (reloadFilesHaveChanged()) {
 			log("Reinitializing...");
 			init();
 		}
 
 		// Set headers and get writer
 		response.setCharacterEncoding("UTF-8");
 		PrintWriter writer = response.getWriter();
 		response.setContentType("text/javascript");
 
 		// purge the cache if we're asked to (purge_cache URI is called)
 		if (purgeCacheRequested(request.getRequestURI())) {
 			purgeCache();
 			writer.println("Cache purged");
 			return;
 		}
 
 		String database = request.getParameter("database");
 		String select = request.getParameter("select");
 		String from = request.getParameter("from");
 		String where = request.getParameter("where");
 		String order = request.getParameter("order");
 		String callback = request.getParameter("callback");
 		String nocache = request.getParameter("nocache");
 
 		// output status if requested
 		boolean callbackSet = callback != null; // true if callback is set
 		if (statusRequested(request.getRequestURI(), callbackSet)) {
 			writer.println(getStatus());
 			return;
 		}
 
 		boolean noCache = false;
 
 		if (nocache != null && nocache.toLowerCase().equals("true")) {
 			noCache = true;
 		}
 
 		// Construct query string
 		String query = "SELECT " + select + " FROM " + from + ((where == null) ? "" : " WHERE " + where)
 				+ ((order == null) ? "" : " ORDER BY " + order);
 
 		// Attempt to retrieve query from cache. If it's expired or not present,
 		// perform the query and cache the result.
 		String out = null;
 
 		// Just in case the servlet ever decides that the cache thread should be
 		// killed check to make sure it's there before we get started.
 		try {
 			cache.getThread();
 		} catch (NullPointerException e) {
 			addException(e, "Cache thread died!");
 			cache = getCache();
 		}
 
 		// If nocache is requested, make sure to get a fresh copy of this record
 		if (noCache) {
 			try {
 				cache.remove(query);
 			} catch (JustacheKeyNotFoundException e) {
 				// That's ok. You can request nocache even if nothing is cached.
 			}
 		}
 
 		try {
 			out = cache.get(query);
 		} catch (JustacheKeyNotFoundException e) {
 			try {
 				out = getOutputAsJson(database, query);
 				cache.put(query, out);
 			} catch (SQLException e1) {
 				addException(e1, "SQLException: " + query);
 			} catch (NullPointerException e1) {
 				addException(e1, "NullPointerException: " + query);
 			}
 		}
 
 		// Write response
 		writer.println(callback + '(' + out + ')');
 
 	}
 
 	private void addException(Exception exception, String message) {
 		log(message);
 		exception.printStackTrace();
 		exceptions.push(new ExceptionWrapper(exception, message));
 	}
 
 	private String getExceptionString(ExceptionWrapper e) {
 		StringBuffer out = new StringBuffer();
 		out.append(e.getException().toString());
 		out.append('\n');
 		for (StackTraceElement element : e.getException().getStackTrace()) {
 			out.append(element);
 			out.append('\n');
 		}
 		return out.toString();
 	}
 
 	private String getStatus() {
		StringBuffer out = new StringBuffer("Muster v1.8.1\n\n");
 		for (DatabaseDefinition db : conf.databases) {
 			out.append(db.name + ":\t");
 			out.append(testConnectivity(db));
 			out.append("\n");
 		}
 
 		out.append("\n\nLast " + conf.exceptionQueueLength + " exceptions\n\n");
 
 		for (ExceptionWrapper e : exceptions) {
 			out.append("==================================\n");
 			out.append("-- " + e.getDate().toString() + " --\n");
 			out.append("==================================\n");
 			out.append('\n');
 			out.append(e.getNote() + '\n');
 			out.append('\n');
 			out.append("Stack trace:\n");
 			out.append("--------------------\n");
 			out.append(getExceptionString(e));
 			out.append("\n\n\n");
 		}
 
 		return out.toString();
 	}
 
 	private boolean statusRequested(String uri, boolean callbackSet) {
 		if (uri.matches("^/muster/status$") || (uri.matches("^/muster/$") && !callbackSet)) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	private void purgeCache() {
 		cache.die();
 		cache = getCache();
 	}
 
 	private boolean purgeCacheRequested(String uri) {
 		if (uri.matches("^/muster/purge_cache$")) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	private String getOutputAsJson(String database, String query) throws SQLException {
 
 		// The output string
 		StringBuffer out = new StringBuffer();
 
 		// Cache StringBuffer length as needed
 		int len;
 
 		// Database operations
 		DatabaseDefinition db = conf.getDatabase(database);
 
 		// //register the driver
 		registerDriver(db.driver, db.url);
 
 		// // Connect to the database
 		Connection connection = DriverManager.getConnection(db.url, db.username, db.password);
 
 		// // Perform the query
 		PreparedStatement statement = connection.prepareStatement(query);
 		statement.execute();
 		ResultSet results = statement.getResultSet();
 
 		// Get and write the column names
 		ResultSetMetaData meta = results.getMetaData();
 		int columnCount = meta.getColumnCount();
 		LinkedList<String> columns = new LinkedList<String>();
 		for (int i = 1; i < columnCount + 1; i++) {
 			// We're only dealing with JSON, so the column names should be
 			// JavaScript-friendly.
 			columns.add(StringEscapeUtils.escapeJavaScript(meta.getColumnName(i)));
 		}
 		out.append("{\n  \"columns\" : [ ");
 
 		// Add column names in JSON format
 		for (String column : columns) {
 			out.append('"' + column + "\", ");
 		}
 
 		// remove the trailing ", " and add a line break and close the array
 		len = out.length();
 		out.delete(len - 2, len);
 		out.append(" ],\n");
 
 		// Add column values
 		out.append("  \"results\" : [ \n");
 
 		while (results.next()) {
 			out.append(rowAsJson(results, columns));
 		}
 
 		// remove the trailing ", "
 		len = out.length();
 		out.delete(len - 2, len);
 		out.append("\n  ]\n");
 		out.append("}");
 
 		return out.toString();
 	}
 
 	private String rowAsJson(ResultSet results, LinkedList<String> columns) {
 		StringBuffer out = new StringBuffer("");
 		int len;
 
 		for (String column : columns) {
 			// output "column" : "value". Escape for JavaScript.
 			try {
 				String value = results.getString(column);
 				if (value != null) {
 					out.append(String.format("      \"%s\": \"%s\",\n", column,
 							StringEscapeUtils.escapeJavaScript(value.replaceAll("\r\n|\r", "\n"))));
 				}
 			} catch (SQLException e) {
 				addException(e, "Couldn't get column `" + column + "`");
 			}
 		}
 
 		// remove the trailing ", " and add a line break and close the
 		// object
 		len = out.length();
 		out.delete(len - 2, len);
 
 		return "    {\n" + out + "\n    },\n";
 	}
 
 	private Driver registerDriver(String driver, String url) {
 		try {
 			DriverManager.registerDriver((Driver) Class.forName(driver).getConstructor().newInstance((Object[]) null));
 			return DriverManager.getDriver(url);
 		} catch (Exception e) {
 			addException(e, "Could not load driver `" + driver + "` for url `" + url + "`");
 		}
 		return null;
 	}
 
 	private boolean reloadFilesHaveChanged() {
 
 		long lastLoaded = conf.lastLoaded;
 
 		for (String path : reloadFilePaths) {
 			String realPath = getServletContext().getRealPath(path);
 			long mtime = new File(realPath).lastModified();
 			if (mtime != 0) {
 				// Found a copy in Context. Remember that for the log.
 				path = realPath;
 			} else {
 				// No Context copy. Try for an absolute path copy.
 				mtime = new File(path).lastModified();
 			}
 			if (mtime > lastLoaded) {
 				log(path + " modified.");
 				return true;
 			}
 		}
 		return false;
 	}
 }
