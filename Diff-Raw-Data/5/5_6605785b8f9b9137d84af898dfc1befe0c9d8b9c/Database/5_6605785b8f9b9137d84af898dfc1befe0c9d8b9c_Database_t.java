 package nl.crafters.chatcensor;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.sql.Connection;
 import java.sql.DatabaseMetaData;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.Date;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 public class Database {
 	
 	private String dbType = "org.sqlite.JDBC";
 	private String dbString = "";
 	private ChatCensor plugin;
 	static String maindirectory = "plugins/ChatCensor/";
 	static File wordFile = new File(maindirectory + "words.txt");
 	
 	Connection connection;
 	public void Log(Player p, String message) {
 		if (p==null) {
 			plugin.AddLog(message);
 		}
 		else {
 			p.sendMessage(plugin.CHATPREFIX + " " + message);
 		}
 	}
 	public boolean PrepareDB() {
 		if (plugin.useMYSQL ) {
 			dbType = "com.mysql.jdbc.Driver";
 		}
 		try 
 		{
 			Class.forName(dbType);
 		} catch (ClassNotFoundException e) {
 			plugin.AddLog("DB Type wrong:" + dbType);
 			return false;
 		}
 		if (plugin.useMYSQL) {
 			dbString = "jdbc:mysql://" + plugin.mysqlHost + ":"+plugin.mysqlPort + "/" + plugin.mysqlDatabase ;
 		}
 		else {
			dbString = "jdbc:sqlite:plugins/ChatCensor/ccensor.db";
 		}
 		return true;
 	}
 	public Database(ChatCensor instance) {
 		plugin = instance;
 		PrepareDB();
 	}
 	public boolean Connect() {
 		try {
 			if (plugin.useMYSQL) {
 				connection = DriverManager.getConnection(dbString,plugin.mysqlUser,plugin.mysqlPassword);
 			}
 			else {
				connection = DriverManager.getConnection("jdbc:sqlite:plugins/ChatCensor/ccensor.db");
 			}
 		} catch (SQLException e) {
 			plugin.AddLog("DB Error:" + e.getMessage());
 			return false;
 		}
 		return true;
 	}
 	public boolean disConnect() {
 		try {
 			connection.close();
 			connection = null;
 		} catch (SQLException e) {
 			plugin.AddLog("DB Error:" + e.getMessage());
 			return false;
 		}
 		return true;
 	}
 	public boolean CheckDatabase() {
 		String strSQL = "";
     	if (! Connect() ) 
     	{
     		plugin.AddLog("Database not connected!");
     	}
         try {
             DatabaseMetaData dbm = connection.getMetaData();
             ResultSet rs = dbm.getTables(null, null, "ccuser", null);
 
             Statement st;
             Boolean tableexist = true;
             if (!rs.next())
                 tableexist = false;
             rs.close();     
             if (!tableexist)
             {
             	st = connection.createStatement();
             	plugin.AddLog("Creating tables (mysql:" + plugin.useMYSQL + ")");
             	
             	if (plugin.useMYSQL)
                 {
             		strSQL = "CREATE TABLE IF NOT EXISTS `ccuser` (`playername` varchar(32) NOT NULL,`TotalFine` decimal(65,2) DEFAULT '0.00',`Total` int(11) DEFAULT '0',`Jailed` int(11) DEFAULT '0',`Kicked` int(11) DEFAULT '0',`Muted` int(11) DEFAULT '0',`CurrentlyMuted` int(1) DEFAULT '0',`LastTime` bigint(20) unsigned DEFAULT '0',PRIMARY KEY (`playername`),KEY `TotalFine` (`TotalFine`),KEY `NumberJailed` (`Jailed`),KEY `Kicked` (`Kicked`)) ENGINE=InnoDB DEFAULT CHARSET=utf8";
 					st.executeUpdate(strSQL);
             		strSQL = "CREATE TABLE IF NOT EXISTS `ccwords` (`word` varchar(32) NOT NULL, `weight` int(11) DEFAULT '1', PRIMARY KEY (`word`)) ENGINE=InnoDB DEFAULT CHARSET=utf8;";
             		st.executeUpdate(strSQL);
                 }
                 else
                 {
                 	strSQL = 	"CREATE TABLE IF NOT EXISTS \"ccuser\"" + 
                 				"(\"playername\" VARCHAR PRIMARY KEY  NOT NULL ," + 
                 				"\"TotalFine\" DOUBLE, "  + 
                 				"\"Total\" INTEGER," + 
                 				"\"Jailed\" INTEGER," + 
                 				"\"Kicked\" INTEGER," +
                 				"\"Muted\" INTEGER," +
                 				"\"CurrentlyMuted\" INTEGER," +
                 				"\"LastTime\" INTEGER);";
                 	st.executeUpdate(strSQL);
                 	strSQL =	"CREATE TABLE IF NOT EXISTS \"ccwords\"" + 
             					"(\"word\" VARCHAR PRIMARY KEY NOT NULL, " +
             					"\"weight\" INTEGER DEFAULT 1 " +
             					");";
                 	st.executeUpdate(strSQL);
                 }
             	st.executeUpdate(strSQL);
 
             	try {
             		connection.commit();
             	}
             	catch (SQLException e) {
             	}
             	importWords(null);
             	
             	st.close();
                 connection.close();
                 
                 plugin.AddLog("Database/tables created");
                 
             }
         } catch (SQLException e) 
         {
             plugin.AddLog("Error while creating tables! - " + e.getMessage() + strSQL);
         }
         return true;
 	}
 	public boolean resetList(){
     	if (! Connect() ) 
     	{
     		plugin.AddLog("Database not connected!");
     	}
     	Statement st;
 		try {
 			st = connection.createStatement();
     	plugin.AddLog("Deleting all words");
    		String strSQL = "DELETE FROM `ccwords`;";
    		st.executeUpdate(strSQL);
    		disConnect();
 		} catch (SQLException e) {
 			disConnect();
 			return false;
 		}
     	return true;
 	}
 	public void importWords(Player p ) {
 		int Success = 0;
 		int Failed = 0;
     	if (! Connect() ) 
     	{
     		plugin.AddLog("Database not connected!");
     	}
         String word = null;
     	if (!wordFile.exists()) {
     		if (p!=null) {
     			Log(p,plugin.CHATPREFIX  + ChatColor.RED + " words.txt not found!");
     		}
     		return;
     	}
         try {
             BufferedReader in = new BufferedReader(new FileReader(wordFile));
             int i = 0;
             while ((word = in.readLine()) != null) {
             	if (!word.equalsIgnoreCase("")) {
             		if (!plugin.c.wordExists(word)) {
             			if (AddWord(word) )
             				Success++;
             			else
             				Failed++;
                 		i++;
             		}
             		else 
             			Failed++;
             	}
             }
             in.close();
             in = null;
             i = 0;
         } catch (Exception e) {
         	plugin.AddLog("Error reading the words.txt" + e.getMessage());
         }
     	Log(p,"Added " + Success + " words from words.txt (" + Failed + " failed)");
     	plugin.c.loadWords();
     	
     	
     	
     	
     	
     }	
 	public void exportWords(Player p ) {
 		int Success = 0;
     	if (! Connect() ) 
     	{
     		plugin.AddLog("Database not connected!");
     	}
     	
     	String list[] = GetWords();
         try {
             BufferedWriter out = new BufferedWriter(new FileWriter(wordFile));
             for (String s : list) {
             	if (!s.equalsIgnoreCase("")) {
             		out.write(s);
             		out.newLine();
             		Success++;
             	}
             }
             out.close();
             out = null;
         } catch (Exception e) {
         	plugin.AddLog("Error writing to words.txt" + e.getMessage());
         }
     	Log(p,"Exported " + Success + " words to words.txt");
     }		
 	public String GetCounters(String playername) {
 		String strOut = "0;0;0;0;0";
 		if (playername.equalsIgnoreCase("")) {
 			return strOut;
 		}
 		if (!Connect()) {
 			return strOut;
 		}
 		String strSQL = "SELECT * from ccuser WHERE playername='" + playername + "';";
 		Statement st;
 		try {
 			st = connection.createStatement();
 			ResultSet rs = st.executeQuery(strSQL);
 			if (rs.next()) {
 				strOut = ((Integer) rs.getInt("Total")).toString();
 				strOut = strOut + ";" + ((Integer) rs.getInt("Jailed")).toString();
 				strOut = strOut + ";" + ((Integer) rs.getInt("Kicked")).toString();
 				strOut = strOut + ";" + ((Integer) rs.getInt("Muted")).toString();
 				strOut = strOut + ";" + ((Integer) rs.getInt("TotalFine")).toString();
 			}
 			rs.close();
 		} catch (SQLException e) {
 			plugin.AddLog("Error in reading stats in database!" + e.getMessage());
 			disConnect();
 			return "";
 		}
 		disConnect();
 		return strOut;
 	}
 	
 	public String GetFilledString(String strIn, Integer cLength, boolean atend) 
 	{
 		return strIn;
 		
 		/*
 		
 		String strOut = strIn;
 		int ol = cLength - strIn.length();
 		for (int i =0;i<ol;i++) {
 			if (atend) {
 				strOut = strOut +"_";
 			}
 			else {
 				strOut = "_" + strOut; 
 			}
 		}
 		
 		return strOut;
 		*/
 		
 		
 	}
 	public void GetList(Player player) {
 		
 		ResultSet rs = null;
 		
 		if (!Connect()) {
 			return ;
 		}
 		String strSQL = "SELECT * FROM ccuser ORDER BY Total DESC LIMIT 0 , 10";
 		Statement st;
 		try {
 			st = connection.createStatement();
 			rs = st.executeQuery(strSQL);
 			Log(player,plugin.CHATPREFIX  + " " + ChatColor.RED + "---- Top List ChatCensor ---- ");
 			Log(player,plugin.CHATPREFIX  + " " + GetFilledString("Player",15,true) + "/" +  
 										   GetFilledString("Jail",4,false) + "/" + 
 										   GetFilledString("Kick",4,false) + "/" + 
 										   GetFilledString("Fine",8,false) + "/" + 
 										   GetFilledString("Total",4,false)  );
 			try {
 				while (rs.next()) {
 					Log(player,plugin.CHATPREFIX  + " " + ChatColor.WHITE + 
 												GetFilledString(rs.getString("playername"),15,true) +"/" +  
 												GetFilledString(rs.getString("Jailed"),4,true)  + "/" + 
 												GetFilledString(rs.getString("Kicked"),4,true) + "/" + 
 												GetFilledString(rs.getString("TotalFine"),8,true)  + "/" + 
 												GetFilledString(rs.getString("Total"),4,true) );
 				}
 				rs.close();
 				disConnect();
 			} catch (SQLException e) {
 				plugin.AddLog("Database error:" + e.getMessage());
 			}
 			
 			
 		} catch (SQLException e) {
 			plugin.AddLog("Error in reading stats in database!");
 		}
 	}
 	public boolean AddWord(String word) {
 		if (word.length() <3) 
 			return false;
 		ResultSet rs = null;
 		if (!Connect()) {
 			return false;
 		}
 		String strSQL = "SELECT * FROM ccwords WHERE word='"  + word + "';";
 		Statement st;
 		try {
 				st = connection.createStatement();
 				rs = st.executeQuery(strSQL);
 				if (rs.next()) {
 					rs.close();
 					disConnect();
 					return false;
 				}
 				rs.close();
 				strSQL = "INSERT INTO ccwords (word) VALUES ('" + word + "');";
 				st.executeUpdate(strSQL);
 				disConnect();
 			} catch (SQLException e) {
 				plugin.AddLog("Database error:" + e.getMessage());
 			}
 		return true;
 	}
 	public boolean WordExists(String word) {
 		ResultSet rs = null;
 		if (!Connect()) {
 			return false;
 		}
 		String strSQL = "SELECT * FROM ccwords WHERE word='"  + word + "';";
 		Statement st;
 		boolean retval=false;
 		try {
 				st = connection.createStatement();
 				rs = st.executeQuery(strSQL);
 				if (rs.next()) {
 					retval = true;
 				}
 				else {
 					retval=false;
 				}
 				rs.close();
 				disConnect();
 			} catch (SQLException e) {
 				plugin.AddLog("Database error:" + e.getMessage());
 			}
 		return retval;
 	}
 	
 	public boolean DeleteWord(String word) {
 		ResultSet rs = null;
 		if (!Connect()) {
 			return false;
 		}
 		String strSQL = "SELECT * FROM ccwords WHERE word='"  + word + "';";
 		Statement st;
 		try {
 				st = connection.createStatement();
 				rs = st.executeQuery(strSQL);
 				if (!rs.next()) {
 					rs.close();
 					disConnect();
 					return false;
 				}
 				rs.close();
 				strSQL = "DELETE FROM ccwords WHERE word='" + word + "';";
 				st.executeUpdate(strSQL);
 				disConnect();
 			} catch (SQLException e) {
 				plugin.AddLog("Database error:" + e.getMessage());
 			}
 		return true;
 	}
 	
 	public String[] GetWords() {
 		ResultSet rs = null;
 		String out = "";
 		if (!Connect()) {
 			return null;
 		}
 		String strSQL = "SELECT * FROM ccwords ORDER BY word";
 		Statement st;
 		try {
 			st = connection.createStatement();
 			rs = st.executeQuery(strSQL);
 			try {
 				while (rs.next()) {
 					if (out=="") {
 						out = rs.getString("word") ;
 					}
 					else {
 						out = out + ";" + rs.getString("word") ;
 					}
 				}
 				rs.close();
 				disConnect();
 			} catch (SQLException e) {
 				plugin.AddLog("Database error:" + e.getMessage());
 			}
 		} catch (SQLException e) {
 			plugin.AddLog("Error in reading stats in database!");
 		}
 		return out.split(";");
 	}
 	public boolean ResetCounter(Player p, String playername) {
 		if (!Connect()) {
 			return false;
 		}
 		String strSQL = "UPDATE ccuser SET Total='0',Jailed='0',Kicked='0',TotalFine='0.0' WHERE playername='" + playername + "';";
 		Statement st;
 		try {
 			st = connection.createStatement();
 			st.executeUpdate(strSQL);
 			st.close();
 			disConnect();
 			st = null;
 		} catch (SQLException e) {
 			plugin.AddLog("Error in updating stats in database!");
 			disConnect();
 			return false;
 		}		
 		return true;
 		
 	}
 	public boolean AddCounter(String playername, String field) {
 		
 		if (!Connect()) {
 			return false;
 		}
 		String strSQL = "SELECT * from ccuser WHERE playername='" + playername + "';";
 		Integer NumberJailed = 0;
 		Integer NumberTotal = 0;
 		Integer NumberKicked = 0;
 		Integer NumberMuted = 0;
 		Double TotalFine = 0.0;
 		long CurrentTime = (new Date()).getTime();
 
 		boolean newrec = true;
 		
 		Statement st;
 		try {
 			st = connection.createStatement();
 			ResultSet rs = st.executeQuery(strSQL);
 			while (rs.next()) {
 				NumberTotal = rs.getInt("Total");
 				NumberJailed = rs.getInt("Jailed");
 				NumberKicked = rs.getInt("Kicked");
 				NumberMuted = rs.getInt("Muted");
 				TotalFine = rs.getDouble("TotalFine");
 				newrec=false;
 			}
 			rs.close();
 		} catch (SQLException e) {
 			plugin.AddLog("Error in writing stats in database!");
 			disConnect();
 			return false;
 		}
 		if(field=="total") {
 			NumberTotal++;
 		}
 		if(field=="jail") {
 			NumberJailed++;
 		}
 		else if (field=="kick") {
 			NumberKicked++;
 		}
 		else if (field=="mute") {
 			NumberMuted++;
 		}
 		else if (field=="fine") {
 			TotalFine = TotalFine + (double) plugin.PlayerFine;
 		}
 		if (newrec) {
 			//INSERT INTO `ccuser` (`playername` ,`TotalFine` ,`Total` ,`Jailed` ,`Kicked` ,`Muted` ,`CurrentlyMuted` ,`LastTime`)
 			//VALUES ('ddj', '10.00', '1', '0', '0', '0', '0', '0');
 
 			strSQL = "INSERT INTO `ccuser` " + 
 					 "(`playername`,`TotalFine`,`Total`,`Jailed`,`Kicked`,`LastTime`,`Muted`,`CurrentlyMuted`) " +
 					 "VALUES (" + 
 					 "'" + playername + "','" +
 					 TotalFine + "','" + 
 					 NumberTotal + "','" +
 					 NumberJailed  + "','" +
 					 NumberKicked + "','" +
 					 CurrentTime + "','" +
 					 NumberMuted + "','0');";
 					 
 		}
 		else {
 			strSQL = "UPDATE ccuser SET " + 
 			 		 "TotalFine='" + TotalFine + "'," + 
 			 		 "Total='" + NumberTotal + "'," +
 			 		 "Jailed='" + NumberJailed + "'," + 
 			 		 "Kicked='" + NumberKicked + "'," + 
 			 		 "LastTime='" + CurrentTime + "'," + 
 			 		 "Muted='" + NumberMuted + 
 			 		 "' WHERE playername='" + playername + "';";
 		}
 		
 		try {
 			st.executeUpdate(strSQL);
 		} catch (SQLException e) {
 			disConnect();
 			plugin.AddLog("Error in updating stats:" + strSQL);
 			return false;
 		}
 		
 		disConnect();
 		return true;
 	}
 	
 	public Integer GetJailTime(Player p, int jailTime) 
 	{
 		
 		if (p==null)
 			return 0;
 		String strList[] = GetCounters(p.getName()).split(";");
 		int total = (Integer.parseInt(strList[0]));
 		int additionaltime = (int) (total*plugin.useIncreaseJailTime);
 		return jailTime + additionaltime;
 	}
 	
 }
