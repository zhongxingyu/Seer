 /**
  * 
  */
 package io;
 
 import java.io.*;
 import java.util.Observable;
 import java.util.Timer;
 import java.util.TimerTask;
 import general.Config;
 import general.Debug;
 
 /**
  * @author Michi
  * Ueberwacht einen Pfad, ob eine spezifizierte Datei existiert
  */
 public class FileMonitor extends Observable  {
 	
 	private File filePath;
 	private Timer monitor;
 
 	/**
 	 *  Der TimerTask zum Ueberpruefen, ob eine Serverantwort in Form einer XML-Datei existiert
 	 * @author Michi
 	 *
 	 */
 	class CheckFileTask  extends TimerTask  
 	{
 		/**
 		 * Funktion die periodische nach Starten des Timers aufgerufen wird
 		 */
 	  public void run()  
 	  {
 	    if (filePath != null) {
 			try {
 				if (filePath.canRead()) {
 					Debug.log(3, "Datei wurde gefunden und kann gelesen werden!");
 					ServerResponse serverResponse = new SimpleXMLParser(filePath).parse();
 					if (serverResponse != null) {
 						// alle Oberserver informieren
 						setChanged();
 						notifyObservers(serverResponse);
 						
 						//Stoppe Timer
 						this.cancel();
 						
 						//loesche Datei
 						if (filePath.delete()) {
							Debug.log(3, "Ursprungsdatei erfolgreich geloescht!");
 						}
						else Debug.error("Ursprungsdatei konnte nicht geloescht werden!");
 					}
 				} else {
 					Debug.log(3, "Keine Datei unter angegebenem Pfad gefunden.");
 				}
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		} else {
 			Debug.error("Error: Pfad zur zu ueberwachenden Datei nicht angegeben!");
 		}
 	  }
 	}
 	
 	/**
 	 * Konstrukor
 	 * @param filePath Der Pfad, der ueberwacht werden soll
 	 */
 	public FileMonitor(File filePath) {
 		this.filePath = filePath;
 	}
 
 
 	/**
 	 * Startet die Ueberwachung des Pfades
 	 */
 	public void startMonitoring() {
 		// Timer starten
         monitor = new Timer();
         monitor.schedule(new CheckFileTask(), 0, 1000//Config.TIMERINTERVALL
         		);
         Debug.log(3, "Monitoring gestartet!");
 	}
 	
 	/**
 	 * Stoppt die Ueberwachung des Pfades
 	 */
 	public void stopMonitoring() {
 		if (this.monitor != null) {
 			this.monitor.cancel();
 			Debug.log(3, "Monitoring gestoppt!");
 		}
 	}
 
 
 	public File getFilePath() {
 		return filePath;
 	}
 
 
 	public void setFilePath(File filePath) {
 		this.filePath = filePath;
 		Debug.log(2, "Zu ueberwachenden Pfad geaendert in: " +filePath.toString());
 	}
 	
 	
 	
 	
 }
