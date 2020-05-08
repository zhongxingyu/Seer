 package info.plagiatsjaeger;
 
 import info.plagiatsjaeger.enums.ErrorCode;
 import info.plagiatsjaeger.enums.FileType;
 import info.plagiatsjaeger.types.CompareResult;
 import info.plagiatsjaeger.types.Settings;
 
 import java.io.UnsupportedEncodingException;
 import java.nio.charset.Charset;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.text.DecimalFormat;
 import java.text.DecimalFormatSymbols;
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.Locale;
 
 import org.apache.log4j.Logger;
 import org.lorecraft.phparser.SerializedPhpParser;
 
 
 /**
  * Stellt Methoden zur Kommunikation mit der MySqlDatenbank zur Verfuegung.
  * 
  * @author Michael
  */
 public class MySqlDatabaseHelper
 {
 	private static final String	SERVERNAME		= ConfigReader.getPropertyString("SERVERNAME");			// "jdbc:mysql://localhost/plagiatsjaeger?useUnicode=true&characterEncoding=utf-8";
 	private static final String	DBDRIVER		= ConfigReader.getPropertyString("DBDRIVER");				// "com.mysql.jdbc.Driver";
 	private static final String	USER			= ConfigReader.getPropertyString("USER");
 	private static final String	PASSWORDFILE	= ConfigReader.getPropertyString("PASSWORDPATH");
 
 	private Connection			_connection		= null;
 	private Statement			_statement		= null;
 	public static final Logger	_logger			= Logger.getLogger(MySqlDatabaseHelper.class.getName());
 
 	/**
 	 * Konstruktor wird noch nicht verwendet
 	 */
 	public MySqlDatabaseHelper()
 	{
 	}
 
 	/**
 	 * Stellt eine Verbindung mit der SQL Datenbank her.
 	 * 
 	 * @throws ClassNotFoundException
 	 * @throws SQLException
 	 */
 	private void connect() throws ClassNotFoundException, SQLException
 	{
 		try
 		{
 			_connection = null;
 			_statement = null;
 			// Laedt den MySQL Treiber
 			Class.forName(DBDRIVER);
 			// Verbindung mit DB herstellen
 			String strPassword = this.readPassword();
 			_connection = DriverManager.getConnection(SERVERNAME, USER, strPassword);
 			// Statements erlauben SQL Abfragen
 			_statement = _connection.createStatement();
 		}
 		catch (ClassNotFoundException e)
 		{
 			_logger.fatal(e.getMessage(), e);
 			e.printStackTrace();
 		}
 		catch (SQLException e)
 		{
 			_logger.fatal(e.getMessage(), e);
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Trennt eine bestehende Verbindung mit der Datenbank
 	 * 
 	 * @throws SQLException
 	 */
 	private void disconnect()
 	{
 		try
 		{
 			if (!_statement.isClosed()) _statement.close();
 			if (!_connection.isClosed()) _connection.close();
 		}
 		catch (SQLException e)
 		{
 			_logger.fatal(e.getMessage(), e);
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Ermittelt das Passwort aus dem Passwordfile
 	 * 
 	 * @return result Passwort als String
 	 */
 	public String readPassword()
 	{
 		String result = "";
 		result = SourceLoader.loadFile(PASSWORDFILE).trim();
 		return result;
 	}
 
 	/**
 	 * Fuegt eine Liste von Suchergebnissen, die in einem lokalen Dokument
 	 * gefunden wurden, zu einem Report ein.
 	 * 
 	 * @param compareResults
 	 *            Gefundene Resultate
 	 * @param dID
 	 *            Zu Resultat gehoerende DocId
 	 */
 	public void insertCompareResults(ArrayList<CompareResult> compareResults, int dID)
 	{
 		try
 		{
 			String strStatement = "";
 			connect();
 			DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.GERMAN);
 			otherSymbols.setDecimalSeparator('.');
 			DecimalFormat df = new DecimalFormat("###.##", otherSymbols);
 			for (CompareResult result : compareResults)
 			{
				String text = new String(Charset.forName("UTF-8").encode(result.getSourceText()).array(), "CP1252").replaceAll("'", "\\'");
 				strStatement = "INSERT INTO result VALUES(DEFAULT, '" + text + "' , '" + "' , '" + dID + "' , '" + result.getCheckStart() + "' , '" + result.getCheckEnd() + "' , '" + df.format(result.getSimilarity() * 100) + "' , '" + (result.getIsInSources() ? 1 : 0) + "' , '" + result.getReportID() + "' )";
 				_statement.executeUpdate(strStatement);
 			}
 			disconnect();
 		}
 		catch (ClassNotFoundException e)
 		{
 			_logger.fatal(e.getMessage(), e);
 			e.printStackTrace();
 		}
 		catch (SQLException e)
 		{
 			_logger.fatal(e.getMessage(), e);
 			e.printStackTrace();
 		}
 		catch (UnsupportedEncodingException e)
 		{
 			_logger.fatal(e.getMessage(), e);
 			e.printStackTrace();
 		}
 
 	}
 
 	/**
 	 * Fuegt eine Liste von Suchergebnissen, die auf einer Internetseite
 	 * gefunden wurden, zu einem Report ein.
 	 * 
 	 * @param compareResults
 	 *            gefundene Resultate
 	 * @param sourceLink
 	 *            zu Resultaten gehoerender Link
 	 */
 	public void insertCompareResults(ArrayList<CompareResult> compareResults, String sourceLink)
 	{
 		try
 		{
 			String strStatement = "";
 			connect();
 			DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.GERMAN);
 			otherSymbols.setDecimalSeparator('.');
 			DecimalFormat df = new DecimalFormat("###.##", otherSymbols);
 			for (CompareResult result : compareResults)
 			{
				String text = new String(Charset.forName("UTF-8").encode(result.getSourceText()).array(), "CP1252").replaceAll("'", "\\'");
 
 				_logger.info("Text: " + result.getSourceText());
 
 				strStatement = "INSERT INTO result VALUES(DEFAULT, '" + text + "','" + sourceLink + "' , " + null + " , '" + result.getCheckStart() + "' , '" + result.getCheckEnd() + "','" + df.format(result.getSimilarity() * 100) + "' , '" + (result.getIsInSources() ? 1 : 0) + "' , '" + result.getReportID() + "' )";
 				_statement.executeUpdate(strStatement);
 			}
 		}
 		catch (ClassNotFoundException e)
 		{
 			_logger.fatal(e.getMessage(), e);
 			e.printStackTrace();
 		}
 		catch (SQLException e)
 		{
 			_logger.fatal(e.getMessage(), e);
 			e.printStackTrace();
 		}
 		catch (UnsupportedEncodingException e)
 		{
 			_logger.fatal(e.getMessage(), e);
 			e.printStackTrace();
 		}
 		finally
 		{
 			disconnect();
 		}
 	}
 
 	/**
 	 * Liest die DocumentId aus einem Report aus.
 	 * 
 	 * @param rID
 	 * @return return
 	 */
 	public int getDocumentID(int rID)
 	{
 		int result = 0;
 
 		String strStatement = "SELECT dID FROM report WHERE rID = " + rID;
 		try
 		{
 			ResultSet rstResultSet = startQuery(strStatement);
 			if (rstResultSet.next())
 			{
 				result = rstResultSet.getInt("dID");
 			}
 		}
 		catch (ClassNotFoundException e)
 		{
 			_logger.fatal(e.getMessage());
 			e.printStackTrace();
 		}
 		catch (SQLException e)
 		{
 			_logger.fatal(e.getMessage());
 			e.printStackTrace();
 		}
 		finally
 		{
 			disconnect();
 		}
 
 		return result;
 	}
 
 	/**
 	 * @param query
 	 * @return return
 	 * @throws SQLException
 	 * @throws ClassNotFoundException
 	 */
 	public ResultSet startQuery(String query) throws ClassNotFoundException, SQLException
 	{
 		ResultSet rstResultSet = null;
 		try
 		{
 			connect();
 			rstResultSet = _statement.executeQuery(query);
 		}
 		catch (SQLException e)
 		{
 			_logger.fatal(e.getMessage());
 			e.printStackTrace();
 		}
 		catch (ClassNotFoundException e)
 		{
 			_logger.fatal(e.getMessage());
 			e.printStackTrace();
 		}
 		return rstResultSet;
 	}
 
 	/**
 	 * Laedt die Einstellungen zu einem Report aus der Datenbank.
 	 * 
 	 * @param rId
 	 *            DocumentId des aktuellen Reports
 	 * @return result Gibt Settingsobjekt zurueck
 	 */
 	public Settings getSettings(int rId)
 	{
 		Settings result = null;
 		String strStatement = "SELECT r.rThreshold, sl.slSearchSentenceLength, sl.slSearchJumpLength, sl.slSearchNumLinks, sl.slCompareSentenceLength, sl.slCompareJumpLength, r.rCheckWWW, r.rCheckIDs, se.seName, se.seURL , se.seURLSearchArg, se.seURLAuthArg, se.seURLArgs FROM report AS r LEFT JOIN settinglevel AS sl ON r.slID = sl.slID LEFT JOIN searchengine AS se ON r.seID = se.seID WHERE r.rID = " + rId;
 		ResultSet rstResultSet = null;
 		try
 		{
 			rstResultSet = startQuery(strStatement);
 			if (rstResultSet.next())
 			{
 				result = Settings.getInstance();
 				String input = rstResultSet.getString("r.rCheckIDs");
 				ArrayList<Integer> localFiles = new ArrayList<Integer>();
 				if (input.length() > 0)
 				{
 					SerializedPhpParser serializedPhpParser = new SerializedPhpParser(input);
 					LinkedHashMap<Integer, String> fileHashMap = (LinkedHashMap<Integer, String>) serializedPhpParser.parse();
 					StringBuilder sb = new StringBuilder();
 
 					if (!fileHashMap.isEmpty())
 					{
 						for (Integer key : fileHashMap.keySet())
 						{
 							if (fileHashMap.get(key).length() == 0)
 							{
 								_logger.fatal("There is an Error for " + key);
 							}
 							else
 							{
 								if (sb.length() != 0)
 								{
 									sb.append(",");
 								}
 								sb.append(fileHashMap.get(key));
 							}
 						}
 
 						String statementFile = "SELECT dID from document as d WHERE fID IN (" + sb.toString() + ")";
 						ResultSet rstResultSetFile = startQuery(statementFile);
 						while (rstResultSetFile.next())
 						{
 							localFiles.add(rstResultSetFile.getInt("d.dID"));
 						}
 					}
 				}
 				result.putSettings(rstResultSet.getInt("r.rThreshold"), rstResultSet.getInt("sl.slSearchSentenceLength"), rstResultSet.getInt("sl.slSearchJumpLength"), rstResultSet.getInt("sl.slCompareSentenceLength"), rstResultSet.getInt("sl.slCompareJumpLength"), rstResultSet.getBoolean("r.rCheckWWW"), localFiles, rstResultSet.getString("se.seURL"), rstResultSet.getString("se.seURLSearchArg"), rstResultSet.getString("se.seURLAuthArg"), rstResultSet.getString("se.seUrlArgs"), rstResultSet.getInt("sl.slSearchNumLinks"));
 			}
 		}
 		catch (SQLException e)
 		{
 			_logger.fatal(e.getMessage(), e);
 			e.printStackTrace();
 		}
 		catch (Exception e)
 		{
 			_logger.fatal(e.getMessage(), e);
 			e.printStackTrace();
 		}
 		finally
 		{
 			disconnect();
 		}
 
 		return result;
 	}
 
 	/**
 	 * 
 	 */
 	public void setReportState(int rId, ErrorCode state)
 	{
 		try
 		{
 			connect();
 			String strStatement = "UPDATE report SET rErrorCode=" + state.value() + " WHERE rId=" + rId;
 			_statement.executeUpdate(strStatement);
 			_logger.info("State changed for: " + rId + "to " + state.value());
 		}
 		catch (ClassNotFoundException e)
 		{
 			_logger.fatal(e.getMessage(), e);
 			e.printStackTrace();
 		}
 		catch (SQLException e)
 		{
 			_logger.fatal(e.getMessage(), e);
 			e.printStackTrace();
 		}
 		finally
 		{
 			disconnect();
 		}
 	}
 
 	/**
 	 * Setzt ein Document als fertig geparst.
 	 * 
 	 * @param docId
 	 *            Dokumentnummer passend zum Report
 	 */
 	public void setDocumentAsParsed(int docId)
 	{
 		try
 		{
 			connect();
 			String strStatement = "UPDATE document SET dIsParsed=1 WHERE dID=" + docId;
 			_statement.executeUpdate(strStatement);
 			_logger.info("Setting document parsed in DB: " + docId);
 		}
 		catch (ClassNotFoundException e)
 		{
 			_logger.fatal(e.getMessage(), e);
 			e.printStackTrace();
 		}
 		catch (SQLException e)
 		{
 			_logger.fatal(e.getMessage(), e);
 			e.printStackTrace();
 		}
 		finally
 		{
 			disconnect();
 		}
 	}
 
 	/**
 	 * Gibt zurueck ob Dokument schon geparsed wurde.
 	 * 
 	 * @param docId
 	 *            Dokumentnummp passend zum Report
 	 * @return TRUE wenn geparsed / FALSE wenn nicht geparsed
 	 */
 	public boolean getDocumentParseState(int docId)
 	{
 		boolean isparsed = false;
 		String strStatement = "SELECT dIsParsed FROM document AS d WHERE d.dID = " + docId;
 		ResultSet rstResultSet = null;
 		try
 		{
 			rstResultSet = startQuery(strStatement);
 			if (rstResultSet.next())
 			{
 				isparsed = rstResultSet.getBoolean("d.dIsParsed");
 			}
 		}
 		catch (SQLException e)
 		{
 			_logger.fatal(e.getMessage(), e);
 			e.printStackTrace();
 		}
 		catch (Exception e)
 		{
 			_logger.fatal(e.getMessage(), e);
 			e.printStackTrace();
 		}
 		finally
 		{
 			disconnect();
 		}
 		return isparsed;
 	}
 
 	/**
 	 * Gibt den Namen/Url fuer eine DocId zurueck.
 	 * 
 	 * @param docId
 	 *            Dokumentnummer fuer passenden Report.
 	 * @return Gibt Name/Url als String zurueck.
 	 */
 	public String loadDocumentURL(int docId)
 	{
 		String docurl = "";
 		String strStatement = "SELECT dOriginalName FROM document AS d WHERE d.dID = " + docId;
 		ResultSet rstResultSet = null;
 		try
 		{
 			rstResultSet = startQuery(strStatement);
 			if (rstResultSet.next())
 			{
 				docurl = rstResultSet.getString("d.dOriginalName");
 			}
 		}
 		catch (SQLException e)
 		{
 			_logger.fatal(e.getMessage(), e);
 			e.printStackTrace();
 		}
 		catch (Exception e)
 		{
 			_logger.fatal(e.getMessage(), e);
 			e.printStackTrace();
 		}
 		finally
 		{
 			disconnect();
 		}
 		_logger.info("URL Loaded From DB: " + docurl);
 		return docurl;
 	}
 
 	/**
 	 * Sschreibt Ergebnisse des Reports in die DB.
 	 * 
 	 * @param rId
 	 *            Report ID
 	 * @param similarity
 	 *            Plagiatsverdachtswert
 	 * @param endTime
 	 *            Abschlusszeit
 	 */
 	public void finishReport(int rId, double similarity, String endTime)
 	{
 		try
 		{
 			String strStatement = "";
 			connect();
 			DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.GERMAN);
 			otherSymbols.setDecimalSeparator('.');
 			DecimalFormat df = new DecimalFormat("###.##", otherSymbols);
 			strStatement = "UPDATE report SET rEndTime ='" + endTime + "', rSimilarity = '" + df.format(similarity * 100) + "' WHERE rID =" + rId;
 			_statement.executeUpdate(strStatement);
 		}
 		catch (ClassNotFoundException e)
 		{
 			_logger.fatal(e.getMessage(), e);
 			e.printStackTrace();
 		}
 		catch (SQLException e)
 		{
 			_logger.fatal(e.getMessage(), e);
 			e.printStackTrace();
 		}
 		finally
 		{
 			disconnect();
 		}
 	}
 
 	public FileType loadFileType(int dId) throws Exception
 	{
 		FileType result = null;
 		String strStatement = "SELECT dFileExtension FROM document AS d WHERE d.dID = " + dId;
 		ResultSet rstResultSet = null;
 		try
 		{
 			rstResultSet = startQuery(strStatement);
 			if (rstResultSet.next())
 			{
 				String filetype = rstResultSet.getString("d.dFileExtension");
 				filetype = filetype.replace(".", "").toUpperCase();
 				result = FileType.valueOf(filetype);
 			}
 		}
 		catch (SQLException e)
 		{
 			_logger.fatal(e.getMessage(), e);
 			e.printStackTrace();
 		}
 		// catch (Exception e)
 		// {
 		// _logger.fatal(e.getMessage(), e); e.printStackTrace();
 		// }
 		finally
 		{
 			disconnect();
 		}
 		return result;
 	}
 }
