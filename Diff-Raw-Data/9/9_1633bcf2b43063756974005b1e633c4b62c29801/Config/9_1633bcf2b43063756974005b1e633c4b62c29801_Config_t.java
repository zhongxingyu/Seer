 package misc;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.NoSuchElementException;
 import java.util.Scanner;
 
 /**
  * Klasse zum einlesen und speichern von Konfigurationen
  * 
  */
 public class Config {
 
 	private static boolean read = false;
 	
 	public static final String KURS_ERSTELLEN_PAGE = "/teacher.html";
 	public static final String KURS_UEBERSICHT_PAGE = "/overview.html";
 	public static final String KURS_WAHL_PAGE = "/vote.html";
 	public static final String SUPER_LEHRER_PAGE = "/admin.html";
 	public static final String WAHL_ERSTELLEN_PAGE = "/create.html";
 	public static final String START_PAGE = "/login.html";
 	public static final String WAHL_ANSWER = "/answer1.html";
 	public static final String KURS_ANSWER = "/answer2.html";
 	public static final String KURS_WAHL_ANSWER = "/answer3.html";
 	public static final String KURS_AUSWERTUNG_ANSWER = "/answer4.html";
 	public static final String LOGOUT_PAGE = "/logout.html";
 
 	public static final int MAXIMAL_KURS_GROESSE = 50;
 	public static final int MAXIMAL_JAHRGANG = 12;
 	public static final int MINIMAL_JAHRGANG = 07;
 
	public static final boolean DEBUGING = true;
 	private static int port;
 	private static String webroot;
 	private static String errorPage;
 
 	private static String[] allowedFiles;
 
 	private static final int DEFAULT_PORT = 80;
 	private static final String DEFAULT_WEBROOT = "web";
 	private static final String DEFAULT_ERROR_PAGE = DEFAULT_WEBROOT
 			+ "/error.html";
 
 	/**
 	 * Liest Konfigurationen aus Configfile. Lässt sich nur einmal zur laufzeit
 	 * des Programms ausführen.
 	 * 
 	 * @param configFile
 	 *            Datei wo die Konfigurationen gespeichert sin
 	 * @throws FileNotFoundException
 	 *             Die Konfigurationsdatei konnte nicht gefunden werden.
 	 */
 	public Config(File configFile, File allowedFilesList) throws FileNotFoundException {
 		if (read) {
 			Print.err("Es wurde schon eine Konfigurationsdatei eingelesen!");
 			return;
 		}
 		read(configFile, allowedFilesList);
 		read = true;
 	}
 
 	// Einlesen der Konfigurationsdatei und der AllowedFiles Liste
 	private void read(File config, File allowedFilesList) throws FileNotFoundException {
 
 		// Lesen der Konfigurationsdatei
 		Scanner file = new Scanner(config);
 		Print.msg("Lese Konfigurationsdatei...");
 		while (true) {
 			try {
 				String[] data = file.nextLine().split("=");
 				if (data[0].equals("port")) {
 					setPort(Integer.valueOf(data[1]));
 					Print.tab("Port: " + port);
 				} else if (data[0].equals("webroot")) {
 					setWebroot(data[1]);
 					Print.tab("Webroot: " + webroot);
 				} else if (data[0].equals("errorpage")) {
 					setErrorPage(data[1]);
 					Print.tab("ErrorPage: " + errorPage);
 				} else {
 					Print.err("Fehler beim Lesen der Konfigurationsdatei");
 				}
 			} catch (NoSuchElementException e) {
 				break;
 			}
 		}
 		file.close();
 
 		// Lesen der AllowedFiles Liste
 		file = new Scanner(allowedFilesList);
 		Print.msg("Lese AllowedFiles Liste...");
 		String[] allowedFiles;
 		int index = 0;
 		while (true) {
 			try {
 				file.nextLine();
 				index++;
 			} catch (NoSuchElementException e) {
 				break;
 			}
 		}
 		allowedFiles = new String[index];
 		index = 0;
 		file.close();
 		file = new Scanner(allowedFilesList);
 		Print.deb("AllowedFiles:");
 		while (true) {
 			try {
 				allowedFiles[index] = file.nextLine();
 				Print.tab(allowedFiles[index]);
 				index++;
 			} catch (NoSuchElementException e) {
 				break;
 			}
 		}
 		file.close();
 		setAllowedFiles(allowedFiles);
 	}
 
 	/**
 	 * Getter vom Port
 	 * 
 	 * @return Gibt verwendeten Port zurück.
 	 */
 	public static int getPort() {
 		if (!read) {
 			Print.msg("Es wurde keine Konfiguration eingelesen! Nutze default Port!");
 			setPort(DEFAULT_PORT);
 		}
 		return port;
 	}
 
 	/**
 	 * Setter vom Port. Der Port muss zwischen 0 und 65535 sein (Default = 80)!
 	 * 
 	 */
 	private static void setPort(int p) {
 		if (p < 1 || p > 65534) {
 			Print.err("Falscher Port angegeben! Benutze default Port!");
 			p = DEFAULT_PORT;
 		}
 		port = p;
 	}
 
 	/**
 	 * Getter vom Webroot Verzeichnis.
 	 * 
 	 * @return Gibt verwendetes Webroot-Verzeichnis zurück.
 	 */
 	public static String getWebroot() {
 		if (!read) {
 			Print.msg("Es wurde keine Konfiguration eingelesen! Nutze default Webroot!");
 			setWebroot(DEFAULT_WEBROOT);
 		}
 		return webroot;
 
 	}
 
 	/**
 	 * Setter des Webroots. Das Webrootverzeichnis muss existieren und darf
 	 * keine Datei sein (Default = default).
 	 * 
 	 */
 	private static void setWebroot(String wr) {
 		File dir = new File(wr);
 		if (!dir.exists()) {
 			if (wr != DEFAULT_WEBROOT)// Zum unterdrücken der doppelten
 										// Fehlermeldung
 				Print.err("Das Webroot Verzeichnis konnte nicht gefunden werden! Es wird das default Webroot Verzeichnis genutzt!");
 
 			wr = DEFAULT_WEBROOT;
 			dir = new File(wr);
 			if (!dir.exists()) {
 				Print.err("Das default Webroot Verzeichnis konnte nicht geöffnet werden!");
 				System.exit(0);
 			} else if (!dir.isDirectory()) {
 				Print.err("Das default Webroot Verzeichnis ist kein Verzeichnis!");
 				System.exit(0);
 			}
 		} else if (!dir.isDirectory()) {
 			if (wr != DEFAULT_WEBROOT)// Zum unterdrücken der doppelten
 										// Fehlermeldung
 				Print.err("Das Webroot Verzeichnis ist kein Verzeichnis! Es wird das default Webroot Verzeichnis genutzt!");
 
 			wr = DEFAULT_WEBROOT;
 			dir = new File(wr);
 			if (!dir.exists()) {
 				Print.err("Das default Webroot Verzeichnis konnte nicht geöffnet werden!");
 				System.exit(0);
 			} else if (!dir.isDirectory()) {
 				Print.err("Das default Webroot Verzeichnis ist kein Verzeichnis!");
 				System.exit(0);
 			}
 		}
 
 		webroot = wr;
 	}
 
 	/**
 	 * Getter von Error Seite.
 	 * 
 	 * @return Gibt verwendete Error Seite zurück.
 	 */
 	public static String getErrorPage() {
 		if (!read) {
 			Print.msg("Es wurde keine Konfiguration eingelesen! Nutze default Error Page!");
 			setErrorPage(DEFAULT_ERROR_PAGE);
 		}
 		return errorPage;
 
 	}
 
 	/**
 	 * Setter der Error Seite. Die Error Seite muss existieren und eine Datei
 	 * sein! (Default = /error.html)
 	 * 
 	 */
 	private static void setErrorPage(String ep) {
 		File file = new File(ep);
 		if (!file.exists()) {
 			if (ep != DEFAULT_ERROR_PAGE)// Zum unterdrücken der doppelten
 											// Fehlermeldung
 				Print.err("Die Error Seite konnte nicht gefunden werden! Es wird die default Error Seite benutzt!");
 
 			ep = DEFAULT_ERROR_PAGE;
 			file = new File(ep);
 			if (!file.exists()) {
 				Print.err("Fehler beim öffnen der default Error Seite!");
 				System.exit(0);
 			} else if (!file.isFile()) {
 				Print.err("Fehler beim öffnen der default Error Seite!");
 				System.exit(0);
 			}
 		} else if (!file.isFile()) {
 			if (ep != DEFAULT_ERROR_PAGE) // Zum unterdrücken der doppelten
 											// Fehlermeldung
 				Print.err("Die Error Seite ist keine Datei! Es wird die default Error Seite benutzt!");
 
 			ep = DEFAULT_ERROR_PAGE;
 			file = new File(ep);
 			if (!file.exists()) {
 				Print.err("Fehler beim öffnen der default Error Seite!");
 				System.exit(0);
 			} else if (!file.isFile()) {
 				Print.err("Fehler beim öffnen der default Error Seite!");
 				System.exit(0);
 			}
 		}
 		errorPage = ep;
 	}
 
 	/**
 	 * @return Eine String Array mit Dateien die per GET-Reqest angefragt werden
 	 *         dürfen
 	 */
 	public static String[] getAllowedFiles() {
 		return allowedFiles;
 	}
 
 	/**
 	 * @param allwedFiles
 	 *            Zum festlegen der String Array mit Dateien die per GET-Reqest
 	 *            angefragt werden dürfen
 	 */
 	private static void setAllowedFiles(String[] allowedFiles) {
 		String[] checked;
 		int checkedIndex = 0;
 		// Index ermitteln
 		for (int i = 0; i < allowedFiles.length; i++) {
 			File f = new File(allowedFiles[i]);
 			if (f.exists() && f.isFile()) {
 				checkedIndex++;
 			} else {
 				Print.err("Datei nicht gefunden: " + allowedFiles[i]);
 			}
 		}
 		checked = new String[checkedIndex];
 		checkedIndex = 0;
 		for (int i = 0; i < allowedFiles.length; i++) {
 			File f = new File(allowedFiles[i]);
 			if (f.exists() && f.isFile()) {
 				checked[checkedIndex] = allowedFiles[i];
 				checkedIndex++;
 			}
 		}
 		Config.allowedFiles = checked;
 	}
 
 }
