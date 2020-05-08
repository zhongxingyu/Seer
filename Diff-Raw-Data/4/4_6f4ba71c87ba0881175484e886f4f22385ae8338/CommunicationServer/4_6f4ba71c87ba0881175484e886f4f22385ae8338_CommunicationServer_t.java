 package utilities.communication;
 
 /**
  * Diese Klasse fungiert zur Kommunikation mit dem Server und der Benachrichtigung der KI.
  * @author Bjoern List
  *
  */
 import java.io.*;
 
 import utilities.Log;
 import utilities.events.*;
 
 public class CommunicationServer {
 	private String agentfilepath;
 	private File agentFile;
 	private long lastchange = 0;
 	private boolean newSet;
 	private char ownRole;
 	private Thread readerthread;
 	private String serverfilepath;
 	private File serverFile;
 	// Singleton Referenz
 	private static CommunicationServer singleton = null;
 	private int timeout;
 	/**
 	 * privater Konstruktor Erzeugung der Singletoninstanz
 	 */
 	private CommunicationServer() {
 	
 	}
 
 	/**
 	 * Beendet die Abfrage der Serverdatei
 	 */
 	public void disableReading() {
 		if (this.readerthread.isAlive()) {
 			this.readerthread.interrupt();
 		}
 		this.readerthread = null;
 	
 	}
 
 	/**
 	 * Startet die Abfrage der Serverdatei in einem neuen Thread. berprft, ob
 	 * die alte, bereits gelesene, Datei noch vorhanden ist und wartet bis diese
 	 * gelscht ist.
 	 */
 	public synchronized void enableReading(boolean Set) {
 		this.newSet = Set;
 	
 		// Puefung, ob noch ein Leserthread luft
 		if (this.readerthread != null) {
 			// alten Leserthread stoppen
 			this.readerthread.interrupt();
 			this.readerthread = null;
 		} // if
 	
 		// neuen Thread starten
 		this.readerthread = new Thread(new ReadServerFileThread());
 		this.readerthread.setDaemon(true);
 		this.readerthread.setName("CommunicationServer Thread");
 		this.readerthread.start();
 	}
 
 	/**
 	 * Diese Methode lst die jeweiligen Events aus und startet deren
 	 * Verarbeitung
 	 * 
 	 * @param type
 	 *            Typ des GameEvents
 	 * @param arguments
 	 *            Argumente, die zustzlich mit dem GameEvent bergeben werden
 	 */
 
 	public void fireGameEvent(final GameEvent.Type type, final String arguments) {
 		Log.getInstance().write("GameEvent gefeuert: " + type.toString());
 
 		GameEvent event = new GameEvent(type, arguments);
 		try {
 			EventDispatcher.getInstance().triggerEvent(event);
 		} catch (Exception e) {
 			Log.getInstance().write(
 					"Fehler: Event konnte nicht geworfen werden!");
 		}
 
 	}
 
 	/**
 	 * Methode - getInstance liefert die Referenz auf den Singleton zurck
 	 * 
 	 * @return CommunicationServer Instanz des Communication Servers
 	 */
 	public static CommunicationServer getInstance() {
 		// Wenn noch kein Objekt besteht, Objekt erzeugen
 
 		if (singleton == null) {
 			singleton = new CommunicationServer();
 		}
 
 		// Objekt zurckliefern
 		return singleton;
 
 	}
 
 	/**
 	 * Liefert den eingestellten Timeoutwert zwischen 2 Zugriffen auf die
 	 * Serverdatei
 	 * 
 	 * @return int timeout
 	 */
 	public int getTimeout() {
 		return timeout;
 	}
 
 	public boolean init(int timeout,String serverFilePath,char role){
 			this.timeout = timeout;
 			this.ownRole = role;
 			
 			//Umwandlung von backslashes im Pfad in normale Slashes
 					if (serverFilePath.contains("\\")) {
 						serverFilePath = serverFilePath.replace("\\", "/");
 					}
 	
 					// Slash am Ende entfernen, falls vorhanden
 					if (serverFilePath.lastIndexOf("/") == serverFilePath.length() - 1) {
 						serverFilePath = serverFilePath.substring(0,
 								serverFilePath.length() - 1);
 					}
 					String testpath = serverFilePath;
 					File test = new File(testpath);
 					// vollstaendige Pfade mit Dateinamen bauen
 					this.serverfilepath = serverFilePath + "/server2spieler" + ownRole
 							+ ".xml";
 					this.serverFile = new File(serverfilepath);
 					
 					if(test.exists()){
 					 this.testConnection(testpath);
 					 return true;	
 					}else{
 						return false;
 					}
 					
 		}
 
 	/**
 		 * Ueberwachung der Serverdatei Meldung an alle Event Listener auslsen
 		 */
 		public void observe() {
 	
 			try {
 				File old = new File(serverfilepath);
 				// neuer Satz
 				if (this.newSet) {
 					if (old.exists()) {
 						ServerMessage msg = XMLParser.getInstance().readXML(old);
 						if (msg.getSetstatus().equals("beendet")) {
 							old.delete();
 						}
 					}
 				} else {
 					while (true) {
 						if (this.readerthread.isInterrupted()) {
 							break;
 						}
 	
 						if (old.exists() && (lastchange == old.lastModified())) {
 							Thread.sleep(300);
 						} else {
 							break;
 						}
 					}
 	
 				}
 	
 			} catch (Exception e) {
 			
 			}
 	
 	
 	//		this.setFiles(this.serverfilepath, this.ownRole);
 			
 			// Auslesen der Datei
 			Log.getInstance().write("Communication Server:Ueberwachen startet");
 			
 			ServerMessage msg;
 				try {
 				do {
 					msg = this.read();
 				} while (msg == null);
 	
 				if (msg != null) {
 					// Auswerten des ServerFiles und werfen der entsprehenden Events
 					if (msg.getRelease().equals("true")) {
 						this.fireGameEvent(GameEvent.Type.OppMove,
 								String.valueOf(msg.getOppmove()));
 						Log.getInstance().write(
 								"Communication Server: Event OppMove gesendet");
 					}
 					// Sieger ist bestimmt
 					if (!msg.getWinner().equals("offen")) {
 						char Winner = msg.getWinner()
 								.substring(msg.getWinner().indexOf(" ") + 1)
 								.charAt(0);
 	
 						this.fireGameEvent(GameEvent.Type.WinnerSet,
 								String.valueOf(Winner));
 						Log.getInstance().write(
 								"Communication Server: WinnerSet Event gesendet "
 										+ Winner);
 					}
 					// Satz ist beendet
 					if (msg.getSetstatus().equals("beendet")) {
 						this.fireGameEvent(GameEvent.Type.EndSet,
 								String.valueOf(msg.getOppmove()));
 						Log.getInstance().write(
 								"Communication Server: Event EndSet gesendet");
 					}
 	
 					lastchange = serverFile.lastModified();
 				}
 			} catch (Exception e) {
 				Log.getInstance().write("Communication Server: Lesefehler.....");
 				e.printStackTrace();
 			}
 	
 			Log.getInstance().write("Communication Server: Ende berwachung");
 	
 		}
 
 	/**
 	 * Lesen des Serverfiles
 	 */
 	
 	private ServerMessage read(){
 		try {
 		// Serverfile auslesen
 		ServerMessage msg = null;
 		// while (msg == null) {
 		while (!serverFile.exists()) {
 			if (this.readerthread.isInterrupted()) {
 				return msg;
 			}
 			try {
 				Thread.sleep(this.timeout);
 			} catch (Exception e) {
 			}
 		}
 		
 			msg = XMLParser.getInstance().readXML(serverFile);
 			return msg;
 		} catch (Exception e) {
 			return null;
 		}
 		
 	}
 
 	/**
 	 * Diese Methode setzt die Variable lastchange zurck, damit eine neue Datei
 	 * gelesen werden kann.
 	 */
 	
 	public void resetLastChange() {
 		this.lastchange = (Long) null;
 	}
 
 	private void testConnection(String testpath){
 		//Datei schreiben
 		long[] time = new long[10];
 		long watermark = 0;
 		
 		long temp;
 		for (int i = 0; i < time.length; i++) {
 			try {
 			temp = System.nanoTime();
 			File x = new File(testpath + "/" + i +".txt");
 			PrintWriter pr = new PrintWriter(x);
 			pr.println(i);
 			pr.flush();
 			pr.close();
 			time[i] = System.nanoTime()-temp;
 			} catch (FileNotFoundException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		} //for
 		
 		//hoechtser Wert
 		for (int i = 0; i < time.length; i++) {
 			if(time[i]>watermark){
 				watermark = time[i];
 			}
 		}
		double watermark_db = watermark/1000000;
 		
		Log.getInstance().write("Der hoechste Wert zum schreiben ist: " + watermark_db);
 		
 	}
 
 	/**
 	 * Schreiben des Spielzuges auf den Server
 	 * 
 	 * @param spalte
 	 *            Nummer der Spalte, in die der naechste STein gelgt wird
 	 */
 	public synchronized void writeMove(byte spalte, String agentFilePath,
 			char role) {
 		if ((spalte > -1 && spalte < 7) && (agentFilePath != null)) {
 			try {
 				Log.getInstance().write(
 						"Zug schreiben im Pfad " + agentFilePath + "in Spalte "
 								+ spalte);
 				// Backslash in slash umwandeln
 				if (agentFilePath.contains("\\")) {
 					agentFilePath = agentFilePath.replace("\\", "/");
 				}
 
 				// Slash am Ende entfernen, falls vorhanden
 				if (agentFilePath.lastIndexOf("/") == agentFilePath.length() - 1) {
 					agentFilePath = agentFilePath.substring(0,
 							agentFilePath.length() - 1);
 				}
 
 				this.agentfilepath = agentFilePath + "/spieler" + role
 						+ "2server.txt";
 
 				this.agentFile = new File(agentfilepath);
 				FileWriter schreiber = new FileWriter(this.agentFile);
 				schreiber.write(Integer.toString(spalte));
 				schreiber.flush();
 				schreiber.close();
 				Log.getInstance().write("Schreiben erfolgreich");
 
 			} catch (Exception e) {
 				// e.printStackTrace();
 				Log.getInstance().write(
 						"Fehler - Move konnte nicht geschrieben werden!");
 			}
 		} else {
 			Log.getInstance()
 					.write("Fehler - falsche Spalte ausgewaehlt oder Pfad nicht gesetzt");
 		}
 
 	}
 
 }
 
 /**
  * Threadklasse zur berwachung des Serverfiles
  * 
  * @author Bjoern List
  * 
  */
 
 class ReadServerFileThread extends Thread {
 	@Override
 	public void run() {
 		Log.getInstance().write("Ueberwachung gestartet");
 		CommunicationServer.getInstance().observe();
 		this.interrupt();
 	}
 
 }
