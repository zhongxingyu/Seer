 /**
  * 
  */
 package general;
 
 /**
  * Klasse zum zentralen Ablegen verschiedener Parameter
  */
 public class Config {
 	
 	/**
 	 * Die Zeit in Millisekunden, die zwischen zwei Versuchen eine Serverantwort zu lesen gewartet wird
 	 */
 	public static int TIMERINTERVALL = 300;
 	
 	/**
 	 * Die maxixmale Zeit in Millisekunden, die unsere Zugberechnung brauchen darf.
 	 * Falls diese ueberschritten wird, wird der Server ein Timeout reklamieren
 	 */
 	public static int MAX_CALCTIME = 3000;
 
 	/**
 	 * Die Standardanzahl, mit wie vielen Reihen gespielt werden soll
 	 */
 	public static int ROWS = 6;
 	
 	/**
 	 * Die Standardanzahl, mit wie vielen Spalten gespielt werden soll
 	 */
 	public static int COLS = 7;
 	
 	/**
 	 * Default Kommunikationsverzeichnis fr die Serverdatei
 	 */
 	public static String DEFAULT_COMMDIR = "C:\\4Line";
 	
 	
 	/**
 	 * Anzahl Saetze, die waehrend eines Spiels standardmaessig gepsielt werden
 	 */
 	public static int SETCOUNT = 3;
 	
 	/**
 	 * Dateinamen zur Kommunikation mit dem Server
 	 */
	public static String FILENAME_SPIELER_X2SERVER = "\\spielerx2server.txt";
	public static String FILENAME_SPIELER_O2SERVER = "\\spielero2server.txt";
	public static String FILENAME_SERVER2SPIELER_X = "\\server2spielerx.xml";//TODO revert for windows server
	public static String FILENAME_SERVER2SPIELER_O = "\\server2spielero.xml";
 }
