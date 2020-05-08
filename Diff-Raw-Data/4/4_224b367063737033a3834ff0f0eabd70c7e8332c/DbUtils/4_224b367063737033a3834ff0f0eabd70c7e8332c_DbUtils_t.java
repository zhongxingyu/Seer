 import java.sql.*;
 
 public class DbUtils {
 	private static Connection connDb;
 	private static String tableWorks;
 	private static String tableOld;
 
 	//
 
 	public int Connect() {
 		int retVal = 0;
 		try {
 			Class.forName("org.sqlite.JDBC");
 			connDb = DriverManager.getConnection("jdbc:sqlite:unimat.db");
 		} catch (ClassNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			retVal = 2;
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			retVal = 1;
 		}
 		return retVal;
 	}
 
 	public int setProjStd(String nomeproj) {
 		int retVal = -1;
 		if (connDb != null) {
 			tableWorks = nomeproj.toUpperCase() + "_new";
 			tableOld = nomeproj.toUpperCase();
 		}
 		return retVal;
 	}
 	public int setProj(String nomeproj) {
 		// ritorna -1 se la connsessione non � aperta
 		int retVal = -1;
 		if (connDb != null) {
 			try {
 				Statement stat = connDb.createStatement();
 				ResultSet rs = stat
 						.executeQuery("SELECT tbl_name FROM sqlite_master WHERE type='table' and tbl_name = '"
 								+ nomeproj.toUpperCase() + "'");
 				if (rs.next()) {
 					stat.executeUpdate("drop table if exists "
 							+ nomeproj.toUpperCase() + "_new;");
 					stat
 							.executeUpdate("CREATE TABLE "
 									+ nomeproj.toUpperCase()
 									+ "_new (rowid INTEGER PRIMARY KEY, Nome TEXT, Bloccante TEXT, Predecessori TEXT, Periodicita TEXT, Utente_Applicativo TEXT, Server TEXT, Nome_Script TEXT, Path_Script TEXT, Parametri TEXT, FileLog TEXT, PathFileLog TEXT, Variabili_UPROC TEXT, Uproc TEXT, Sessione TEXT, Tipo_Shell_Uproc TEXT, Shell_Uproc TEXT, Management_Unit TEXT, Lancio_Sessione TEXT);");
 					tableWorks = nomeproj.toUpperCase() + "_new";
 					tableOld = nomeproj.toUpperCase();
 					retVal = 0;
 				} else {
 					stat.executeUpdate("drop table if exists "
 							+ nomeproj.toUpperCase() + "_new;");
 					stat.executeUpdate("drop table if exists "
 							+ nomeproj.toUpperCase() + ";");
 					stat
 							.executeUpdate("CREATE TABLE "
 									+ nomeproj.toUpperCase()
 									+ " (rowid INTEGER PRIMARY KEY, Nome TEXT, Bloccante TEXT, Predecessori TEXT, Periodicita TEXT, Utente_Applicativo TEXT, Server TEXT, Nome_Script TEXT, Path_Script TEXT, Parametri TEXT, FileLog TEXT, PathFileLog TEXT, Variabili_UPROC TEXT, Uproc TEXT, Sessione TEXT, Tipo_Shell_Uproc TEXT, Shell_Uproc TEXT, Management_Unit TEXT, Lancio_Sessione TEXT);");
 					stat
 							.executeUpdate("CREATE TABLE "
 									+ nomeproj.toUpperCase()
 									+ "_new (rowid INTEGER PRIMARY KEY, Nome TEXT, Bloccante TEXT, Predecessori TEXT, Periodicita TEXT, Utente_Applicativo TEXT, Server TEXT, Nome_Script TEXT, Path_Script TEXT, Parametri TEXT, FileLog TEXT, PathFileLog TEXT, Variabili_UPROC TEXT, Uproc TEXT, Sessione TEXT, Tipo_Shell_Uproc TEXT, Shell_Uproc TEXT, Management_Unit TEXT, Lancio_Sessione TEXT);");
 					tableWorks = nomeproj.toUpperCase() + "_new";
 					tableOld = nomeproj.toUpperCase();
 					retVal = 0;
 				}
 			} catch (SQLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		return retVal;
 	}
 
 	public void consolida() {
 		if (connDb != null) {
 			try {
 				Statement stat = connDb.createStatement();
 				stat.executeUpdate("drop table if exists " + tableOld);
 				stat.executeUpdate("Alter table " + tableWorks + " rename to "
 						+ tableOld + ";");
 				stat
 						.executeUpdate("CREATE TABLE "
 								+ tableWorks
 								+ " (rowid INTEGER PRIMARY KEY, Nome TEXT, Bloccante TEXT, Predecessori TEXT, Periodicita TEXT, Utente_Applicativo TEXT, Server TEXT, Nome_Script TEXT, Path_Script TEXT, Parametri TEXT, FileLog TEXT, PathFileLog TEXT, Variabili_UPROC TEXT, Uproc TEXT, Sessione TEXT, Tipo_Shell_Uproc TEXT, Shell_Uproc TEXT, Management_Unit TEXT, Lancio_Sessione TEXT);");
 			} catch (SQLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 
 	}
 
 	public int storeRow(String[] param) {
 		int retVal = -1;
 		String insSql = "";
 		if (connDb != null) {
 			try {
 				Statement stat = connDb.createStatement();
 				insSql = "insert into " + tableWorks + " values(";
 				for (int i = 0; i < param.length; i++) {
 					insSql += "'" + param[i].replace("'", "") + "',";
 				}
 				insSql = insSql.substring(0, insSql.length() - 1);
 				insSql += ");";
 				// System.out.println(insSql);
 				stat.execute(insSql);
 			} catch (SQLException e) {
 				// TODO Auto-generated catch block
 				System.out.println(insSql);
 				retVal = -2;
 			}
 		}
 		return retVal;
 	}
 
 	public Boolean isNew(String nomeUproc) {
 		Boolean retVal = false;
 		String query = "";
 		if (connDb != null) {
 			try {
 				Statement stat = connDb.createStatement();
 				query = "SELECT uproc FROM " + tableOld + " WHERE uproc = '"
 						+ nomeUproc + "'";
 				ResultSet rs = stat.executeQuery(query);
 				retVal = true;
 				if (rs.next()) {
 
 					retVal = false;
 				}
 				query = "SELECT * FROM " + tableWorks + " WHERE uproc = '"
 						+ nomeUproc + "' execpt " + "SELECT  * FROM "
 						+ tableOld;
 				rs = stat.executeQuery(query);
 				if (rs.next()) {
 					retVal = true;
 				}
 			} catch (SQLException e) {
 				// TODO Auto-generated catch block
 				// System.out.println(query);
 			}
 		}
 
 		return retVal;
 	}
 
 	public ResultSet getNewUprocs() {
 		ResultSet rs = null;
 		String query ="";
 		if (connDb != null) {
 			try {
 				Statement stat = connDb.createStatement();
 				 query = "SELECT   Nome_Script, Path_Script, Parametri, Uproc, Sessione, Lancio_Sessione, Nome FROM "
 						+ tableWorks
 						+ " except SELECT  Nome_Script, Path_Script, Parametri, Uproc, Sessione, Lancio_Sessione, Nome from "
 						+ tableOld;
 				rs = stat.executeQuery(query);
 			} catch (SQLException e) {
 				// TODO Auto-generated catch block
 				System.out.println(query);
 			}
 		}
 		return rs;
 	}
 
 	public String[] getUprocNewParam(String nomeUproc) {
 		String[] param = new String[19];
 		String query = "";
 		if (connDb != null) {
 			try {
 				Statement stat = connDb.createStatement();
 				query = "SELECT * FROM " + tableWorks + " WHERE uproc = '"
 						+ nomeUproc + "'";
 				ResultSet rs = stat.executeQuery(query);
 				//rs = stat.executeQuery(query);
 				if (rs.next()) {
 					for (int i = 0; i < 19; i++) {
 						param[i] = rs.getString(i + 1);
 					}
 				}
 			} catch (SQLException e) {
 				// TODO Auto-generated catch block
 				System.out.println(query);
 			}
 		}
 		return param;
 	}
 	
 	public String[] getSessioni(){
 		String[] sessioni = null;
 		String query = "";
 		if (connDb != null) {
 			try {
 				Statement stat = connDb.createStatement();
 				query = "select count(*) from "+tableOld+" where nome like 'SESSIONE%:%';";
 				ResultSet rs = stat.executeQuery(query);
 				int numRow = rs.getInt(1);
 				sessioni = new String[numRow];
 				query = "select substr(nome,10,10) from "+tableOld+" where nome like 'SESSIONE%:%' order by nome asc;";
 		rs = stat.executeQuery(query);
 		int i = 0;
 		while (rs.next()){
 			sessioni[i] = rs.getString(1).replace(":", "");
 			i++;
 		}
 			
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			System.out.println(query);
 		}
 		
 	}
 		return sessioni;
 	}
 	
 	public String[] getProjects(){
 		String [] projects = null;
 		String query = "";
 		if (connDb != null){
 			try {
 				Statement stat = connDb.createStatement();
 				query = "select count(*) from sqlite_master where name not like '%_new';";
 				ResultSet rs = stat.executeQuery(query);
 				int numRow = rs.getInt(1);
 				projects  = new String[numRow];
 				query = "select name from sqlite_master where name not like '%_new';";
 				rs = stat.executeQuery(query);
 				int i = 0;
 				while (rs.next()){
 					projects[i] = rs.getString(1);
 					i++;
 				}
 				
 			} catch (SQLException e){
 				System.out.println(query);
 			}
 		}
 		return projects;
 	}
 	
 	public String[] getUprocSessione(String sessione){
 		String [] uprocs = null;
 		String query = "";
 		if (connDb != null) {
 			try {
 				Statement stat = connDb.createStatement();
 				query = "Select count(*) from "+tableWorks+" where sessione = '"+sessione+"'";
 				ResultSet rs = stat.executeQuery(query);
 				int numRow = rs.getInt(1);
 				uprocs = new String[numRow];
 				query = "Select Uproc from "+tableWorks+" where sessione = '"+sessione+"' order by rowid";
 				rs = stat.executeQuery(query);
 				int i = 0;
 				while (rs.next()){
 					uprocs[i] = rs.getString(1);
 					i++;
 				}
 			} catch (SQLException e) {
 				// TODO Auto-generated catch block
 				System.out.println(query);
 			}
 		
 		}
 		return uprocs;
 	}
 	
 	
 	
 	
 	public String[] getNewSession(){
 		String [] sessioni = null;
 		String query = "";
 		if (connDb != null) {
 			try {
 				Statement stat = connDb.createStatement();
 				query = "select count(*) from (SELECT nome FROM "+tableWorks+" where nome like 'SESSIONE%:%' except SELECT nome FROM "+tableOld+" where nome like 'SESSIONE%:%')";
 				ResultSet rs = stat.executeQuery(query);
 				int numRow = rs.getInt(1);
 				sessioni = new String[numRow];
 				query = "SELECT nome FROM "+tableWorks+" where nome like 'SESSIONE%:%' except SELECT nome FROM "+tableOld+" where nome like 'SESSIONE%:%'";
 				rs = stat.executeQuery(query);
 				int i = 0;
 				while (rs.next()){
 					sessioni[i] = rs.getString(1);
 					i++;
 				}
 			} catch (SQLException e) {
 				// TODO Auto-generated catch block
 				System.out.println(query);
 			}
 		}
 		return sessioni;
 	}
 	Boolean isSet(){
 		Boolean retVal = false;
 		try{
 		if (tableWorks.length()!=0){
 			retVal = true;
 		}} catch (java.lang.NullPointerException e){
 			retVal = false;
 		}
 		return retVal;
 	}
 	
 	public String getLabelSessione(String sessione){
 		String retVal = "Sessione "+sessione;
 		String query = "";
 		if (connDb != null){
 			try {
 				Statement stat = connDb.createStatement();
 				query = "SELECT Nome from "+tableWorks;
 				query += " where Nome like 'SESSIONE "+sessione+"%";
 				ResultSet rs = stat.executeQuery(query);
 				if (rs.next()){
 					int pos = rs.getString(1).indexOf(':');
 					
 					retVal = rs.getString(1).substring(pos+2);
 				}
 			} catch (SQLException e){
 				System.out.println(query);
 			}
 			}
 		return retVal;
 	}
 	
 	public String getFirstDep(String uproc){
 		//Cerca dip all'interno della sessione
 		String retVal = "";
 		String pred = "";
 		String query = "";
 		int  min = 0;
 		int max = 0;
 		if (connDb != null) {
 			try {
 			query = "Select Predecessori from "+tableWorks;
 			query += " where  Uproc = '"+uproc+"'";
 			Statement stat = connDb.createStatement();
 			ResultSet rs = stat.executeQuery(query);
 			if (rs.next()){
 				pred = rs.getString(1);
 			}
 		if ( pred.length()>0){
 			pred = pred.replaceAll("\"", "");
 			String listaPred[] = pred.split(";");
 			//TODO Sistemare Predecessori
 			
 			query = "select max(rowid), min(rowid) from "+tableWorks+" where sessione in ";
 			query+= "(select sessione from "+tableWorks+" where uproc = '"+uproc+"')";
 			
 			rs = stat.executeQuery(query);
 			if ( rs.next()){
 				max = rs.getInt(1);
 				min = rs.getInt(2);
 				
 			}
 			
			int rowID = -1;
 			for (int i=0;i<listaPred.length;i++){
 				try {
 				int dipTest = Integer.parseInt(listaPred[i]);
 				
 				if ((dipTest<= max) && (dipTest >= min)){
 					rowID = dipTest;
 					break;
 				}
 				} catch (java.lang.NumberFormatException eN) {
 					
 				}
 			}
			
 			query = "Select Uproc from "+tableWorks;
 			query +=" where rowid = '"+rowID+"'";
 			rs = stat.executeQuery(query);
 			if ( rs.next()){
 				retVal = rs.getString(1);
 			}
 		}
 			} catch (SQLException e){
 				System.out.println(query);
 			}
 		}
 		return retVal;
 	}
 	public String getStringLaunch(String sessione){
 		String retVal = "Standard";
 		String query = "";
 		String sespl = sessione.substring(0,2)+"_"+sessione.substring(3);
 		if (connDb != null) {
 			try {
 				Statement stat = connDb.createStatement();
 				query = "SELECT variabili_uproc from "+tableWorks;
 				query += " where uproc = '"+sespl+"'";
 				//System.out.println(query);
 				ResultSet rs = stat.executeQuery(query);
 				if (rs.next()){
 					retVal = rs.getString(1);
 				}
 			
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			System.out.println(query);
 		}
 		}
 		return retVal;
 	
 	}
 	public ResultSet getAllForCsv(){
 		ResultSet rs = null;
 		if (connDb != null) {
 			try {
 				Statement stat = connDb.createStatement();
 				String query = "SELECT * FROM "+ tableWorks;
 				rs = stat.executeQuery(query);
 			} catch (SQLException e) {
 				// TODO Auto-generated catch block
 				// System.out.println(query);
 			}
 		}
 		return rs;
 	}
 }
