 /*
  * Created on Oct 27, 2006
  */
 package edu.duke.cabig.catrip.deid;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import org.apache.commons.cli.BasicParser;
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.Option;
 import org.apache.commons.cli.OptionBuilder;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 
 public class DeIdDbManage
 {
 	private boolean quiet = false;
 	private String dbUrl;
 	private String user;
 	private String password;
 	
 	public DeIdDbManage(String dbUrl, String user, String password) 
 		throws ClassNotFoundException
 	{
 		super();
 		
 		this.dbUrl = dbUrl;
 		this.user = user;
 		this.password = password;
 
 		Class.forName("com.mysql.jdbc.Driver");
 	}
 	
 	public void init() 
 		throws SQLException
 	{
 		Connection con = DriverManager.getConnection(dbUrl, user, password);
 		try {
 			Statement stmt = con.createStatement();
 			String val = null;
 			
 			stmt.executeUpdate("DROP DATABASE IF EXISTS deid");
 			if (! isQuiet()) System.out.println("Dropped database deid");
 
 			stmt.executeUpdate("CREATE DATABASE deid");
 			if (! isQuiet()) System.out.println("Created database deid");
 
 			stmt.executeUpdate("CREATE TABLE deid.users (id INT NOT NULL AUTO_INCREMENT, userName VARCHAR(255), tableName VARCHAR(255), PRIMARY KEY(id), INDEX (userName), INDEX(tableName))");
 			if (! isQuiet()) System.out.println("Created table deid.users");
 		} finally {
 			try { con.close(); } catch (Exception e) { }
 		}
 
 		newTable("deid");
 		newUser(user, null, "deid");
 	}
 	
 	public void newTable(String table) 
 		throws SQLException
 	{
 		Connection con = DriverManager.getConnection(dbUrl, user, password);
 		try {
 			Statement stmt = con.createStatement();
 			String val = null;
 			
 			stmt.executeUpdate("CREATE TABLE deid." + table + " (id INT NOT NULL AUTO_INCREMENT, phi VARCHAR(255), val VARCHAR(255), PRIMARY KEY(id), INDEX (phi), INDEX(val))");
 			if (! isQuiet()) System.out.println("Created table deid." + table);
 		} finally {
 			try { con.close(); } catch (Exception e) { }
 		}
 	}
 	
 	public void newUser(String user, String password, String table) throws SQLException
 	{
 		Connection con = DriverManager.getConnection(this.dbUrl, this.user, this.password);
 		try {
 			Statement stmt = con.createStatement();
 			String val = null;
 			
 			ResultSet rs = stmt.executeQuery("SELECT COUNT(id) FROM deid.users WHERE userName='" + user + "'");
 			rs.next();
 			if (rs.getInt(1) > 0) {
 				if (! isQuiet()) System.out.println("User " + user + " already exists in table deid." + table);
 			} else {
 				stmt.executeUpdate("INSERT INTO deid.users (userName, tableName) VALUES (\"" + user + "\", \"" + table + "\")");
 				if (! isQuiet()) System.out.println("Added user " + user + " to table deid." + table);
 			}
 			
 			if (password != null && ! password.equals("")) {
				stmt.executeUpdate("GRANT SELECT ON deid.users TO '" + user + "'@'localhost' IDENTIFIED BY '" + password + "'");				
 				if (! isQuiet()) System.out.println("Granted privs for user " + user + " to table deid.users");
 
				stmt.executeUpdate("GRANT ALL PRIVILEGES ON deid." + table + " TO '" + user + "'@'localhost' IDENTIFIED BY '" + password + "'");				
 				if (! isQuiet()) System.out.println("Granted privs for user " + user + " to table deid." + table);
 			}
 		} finally {
 			try { con.close(); } catch (Exception e) { }
 		}
 	}
 	
 	/**
 	 * Get the command-line options
 	 */
 	private static Options getOptions()
 	{
 		Option cmd = OptionBuilder.withArgName("cmd")
 			.hasArg()
 			.isRequired(true)
 			.withDescription("init, newtable, newuser")
 			.create("cmd");
 		
 		Option dburl = OptionBuilder.withArgName("dburl")
 			.hasArg()
 			.isRequired(true)
 			.withDescription("the url to the db - e.g. jdbc:mysql://localhost/mysql")
 			.create("dburl");
 
 		Option user = OptionBuilder.withArgName("user")
 			.hasArg()
 			.isRequired(true)
 			.withDescription("the user name")
 			.create("user");
 
 		Option password = OptionBuilder.withArgName("password")
 			.hasArg()
 			.isRequired(true)
 			.withDescription("the user password")
 			.create("password");
 
 		Option newuser = OptionBuilder.withArgName("newuser")
 			.hasArg()
 			.isRequired(false)
 			.withDescription("the new user name to add to a table")
 			.create("newuser");
 
 		Option newpassword = OptionBuilder.withArgName("newpassword")
 			.hasArg()
 			.isRequired(false)
 			.withDescription("the new user password to add - include if you want privs granted")
 			.create("newpassword");
 
 		Option table = OptionBuilder.withArgName("table")
 			.hasArg()
 			.isRequired(false)
 			.withDescription("the table to add a user to or the new table to create")
 			.create("table");
 		
 		Options options = new Options();
 		options.addOption(cmd);		
 		options.addOption(dburl);		
 		options.addOption(user);		
 		options.addOption(password);		
 		options.addOption(table);		
 		options.addOption(newuser);		
 		options.addOption(newpassword);		
 		return options;
 	}
 	
 	private static void invalidArgs(Options options, ParseException e) 
 	{
 		System.out.println("Error parsing arguments: " + e.getMessage());
 		HelpFormatter formatter = new HelpFormatter();
 		formatter.printHelp("DeIdDbManage", options);
 		System.exit(-1);
 	}
 	
 	public static void main(String[] args) 
 		throws Exception
     {
         Options options = getOptions();
         CommandLine cmd = null;
 		try {			
 			cmd = new BasicParser().parse(options, args);
 		} catch(ParseException e) {	
 			invalidArgs(options, e);
 			return;
 		}
 		
 		DeIdDbManage manage = new DeIdDbManage(
 			cmd.getOptionValue("dburl"), cmd.getOptionValue("user"), cmd.getOptionValue("password")
 		);
 		
 		String cmdName = cmd.getOptionValue("cmd");
 		if ("init".equals(cmdName)) {
 			manage.init();
 		} else if ("newtable".equals(cmdName)) {
 			String table = cmd.getOptionValue("table");
 			if (table == null) {
 				invalidArgs(options, new ParseException("missing table"));
 				return;
 			}			
 			manage.newTable(table); 
 		} else if ("newuser".equals(cmdName)) {
 			String table = cmd.getOptionValue("table");
 			String newuser = cmd.getOptionValue("newuser");
 			String newpassword = cmd.getOptionValue("newpassword");
 			if (table == null) {
 				invalidArgs(options, new ParseException("missing table"));
 				return;
 			}			
 			if (newuser == null) {
 				invalidArgs(options, new ParseException("missing newuser"));
 				return;
 			}			
 			if (newpassword == null || newpassword.equals("")) {
 				System.out.println("No password provided: not granting mysql permissions");
 				return;
 			}			
 			manage.newUser(newuser, newpassword, table); 
 		} else {
 			invalidArgs(options, new ParseException("invalid cmd (" + cmdName + ")"));
 			return;
 		}
     }
 
 	public boolean isQuiet()
 	{
 		return quiet;
 	}
 
 	public void setQuiet(boolean quiet)
 	{
 		this.quiet = quiet;
 	}
 
 }
