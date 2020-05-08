 /**
  * 
  */
 package game;
 
 import general.Debug;
 import io.FileMonitor;
 import io.ServerResponse;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Observable;
 import java.util.Observer;
 
 import ai.*;
 
 /**
  * @author Michi
  * Bildet ein Vier-Gewinnt-Spiel mit mehreren Saetzen ab
  */
 public class FourInARowGame implements Observer {
 	
 	//Die Saetze
 	private ArrayList<Set> sets;
 	
 	//Index des aktuellen Satzes
 	private int setIndex = -1;
 	
 	//Die Anzahl der Saetze
 	private int setNumber;
 	
 	//Die Nummer der Gegnergruppe
 	private int enemy;
 	
 	//Der Pfad zum Kommunikationsverzeichnis
 	private String commDirString;
 	private File commDir;
 	
 	//Der Filemonitor zur Ueberwachung
 	private FileMonitor commDirMonitor;
 	
 	/**
 	 * Konstrukor, Erstellt ein neues Spielobjekt
 	 * @param setNumber Die Anzahl an Saetzen
 	 * @param commDirString Die Pfadangabe, unter der mit dem Server kommuniziert werden soll
 	 */
 	public FourInARowGame(int setNumber, String commDirString) {
 		sets = new ArrayList<Set>();
 		this.setNumber = setNumber;
 		this.commDir = new File(commDirString);
 		this.commDirMonitor = new FileMonitor( new File(commDir + "/server_xml.txt")); //TODO Dateinamen dynamisch ermitteln
 		
 		//Wir werden ueber Aenderungen informiert - hier: sobald eine neue Antwort des Servers empfangen wurde
 		this.commDirMonitor.addObserver(this);
 		
 		//Teste Kommunikationspfad
 		if (commDir.canWrite()) Debug.log(2, "Kommunikationspfad erfolgreich eingerichtet.");
 		else Debug.error("Angegebenes Kommunikationsverzeichnes konnte nicht eingerichtet werden!");
 	}
 	
 	/**
 	 * Ermittelt den Spielstand nach Saetzen
 	 * @return String der Form "Anzahl von uns gewonnen Saetze":"Anzahl von Gegner gewonnen Saetze"; z.B. "2:1"
 	 */
 	public String getScore() {
 		return "0:0"; //TODO Spielstand nach Saetzen ermitteln und als String zurckliefern
 	}
 	
 	/**
 	 * Ueberprueft, ob wir bereits das Spiel gewonnen haben
 	 * @return Liefer TRUE zurueck, falls wir nach Saetzen gewonnen haben, FALSE sonst
 	 */
 	public boolean checkWinStatus() {
 		return false; //TODO implementieren
 	}
 	
 	/**
 	 * Setzt eine neues Kommunikationsverzeichnis
 	 * @param commDirString Der Pfad zum Verzeichnis
 	 */
 	public void setCommDir(String commDirString) {
 		this.commDirString = commDirString;
 		
 		this.commDir = new File(commDirString);
		this.commDirMonitor = new FileMonitor( new File(commDir + "server2spielero.xml")); //TODO Dateinamen dynamisch ermitteln
 		
 		//Wir werden ueber Aenderungen informiert - hier: sobald eine neue Antwort des Servers empfangen wurde
 		this.commDirMonitor.addObserver(this);
 		
 		//Teste Kommunikationspfad
 		if (commDir.canWrite()) Debug.log(2, "Kommunikationspfad erfolgreich eingerichtet.");
 		else Debug.error("Angegebenes Kommunikationsverzeichnes konnte nicht eingerichtet werden!");
 	}
 	
 	
 	/**
 	 * Beginnt einen neuen Satz und startet die Ueberwachung des Kommunikationspfades
 	 */
 	public void startNewSet() {
 		Set newSet = new Set(Player.X); //TODO Unseren Spieler von GUI erhalten
 		newSet.setStartTime(new Date());
 		sets.add(newSet); 
 		setIndex++;
 		commDirMonitor.startMonitoring();
 	}
 	
 	/**
 	 * Beendet den akutellen Satz
 	 */
 	public void endSet() {
 		commDirMonitor.stopMonitoring();
 	}
 	
 	/**
 	 * Liefert den gerade aktuellen Satz
 	 * @return Eine Referenz auf den Satz
 	 */
 	public Set getCurrentSet() {
 		return sets.get(setIndex);
 	}
 
 
 	public ArrayList<Set> getSets() {
 		return sets;
 	}
 
 	public int getSetNumber() {
 		return setNumber;
 	}
 
 	/**
 	 * Wird vom FileMonitor aufgerufen, sobald eine neue Datei des Servers gefunden und ausgewertet werden konnte
 	 * @param arg0 Das Objekt, welches das Update ausgeloest hat
 	 * @param arg1 Die Serverantwort, die aus der gefunden XML-Datei gelesen wurde
 	 */
 	@Override
 	public void update(Observable arg0, Object arg1) {
 		//Teste, ob es sich wirklich um eine korrekte Instanz handelt
 		if (arg1 != null && arg1 instanceof ServerResponse) { 
 			
 			//Falls es sich um eine korrekte Serverantwort handelt in eigene Variable casten
 			ServerResponse serverResponse = (ServerResponse) arg1;
 			
 			//TODO Antwort auswerten und reagieren
 			
 			//Falls ein Gegnerzug uebermittelt wurde, muss dieser auch bei uns abgebildet werden
 			if (serverResponse.getGegnerzug() != -1) {
 				this.getCurrentSet().makeDrop(this.getCurrentSet().getEnemyPlayer(), serverResponse.getGegnerzug());
 				this.getCurrentSet().setStartingPlayer(this.getCurrentSet().getEnemyPlayer());
 			} else { // Falls kein Gegnerzug uebermittelt wurde, sind wir in jedem Fall der startende Spieler
 				this.getCurrentSet().setStartingPlayer(this.getCurrentSet().getOurPlayer());
 			}
 			
 			//Falls Freigabe fuer uns erfolgt wird unser naechster Zug berechnet
 			if (serverResponse.getFreigabe()) {
 				IComputerPlayer ki = new RandomKI();
 				Player ourPlayer = this.getCurrentSet().getOurPlayer();
 				Field calculatedField = ki.calcField(ourPlayer, this.getCurrentSet().getBoard());
 				Move calculatedMove = new Move(ourPlayer, calculatedField);
 				
 				this.getCurrentSet().makeMove(calculatedMove);
 				this.getCurrentSet().getBoard().print();
 				//TODO Unseren Zug per FileWriter an den Server uebermitteln
 			}
 			
 			//Falls ein Gewinenr feststeht, Satz beenden
 			Player winner;
 			if ( ( winner = this.getCurrentSet().getBoard().findWinner()) != null) {
 				Debug.log(0, "Satz #" + (setIndex+1) + " wurde von Spieler " + winner + " gewonnen!");
 				this.endSet();
 			}
 			
 		}
 		
 	}
 }
