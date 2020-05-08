 package dummydk.controller;
 
 import dummydk.gui.Frame;
 import dummydk.model.Spieler;
 import dummydk.model.Spielobjekt;
 
 import java.awt.Point;
 import java.awt.event.KeyEvent;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 
 /**
  * Repraesentiert ein Spiel, welches aus mehreren Spielfeldern bestehen kann. Ein
  * Spiel wird aus einer Datei erzeugt und besteht solange, bis es entweder
  * gewonnen oder verloren wurde.
  */
 public class Spiel {
 
 	/**
 	 * Die Anzahl Raeume pro Level. Eleganter waere es, diese Information direkt
 	 * aus der Textdatei zu beziehen. Aber so geht es auch.
 	 */
 	private static final int ANZ_RAUM_PRO_LEVEL = 3;
 
 	/** Der Frame, zu dem dieses Spiel gehoert. */
 	private Frame parentFrame;
 
 	/** Alle Spielfelder fuer das aktuelle Spiel. */
 	private ArrayList<Spielfeld> spielfelder;
 
 	/** Zeiger auf das aktuelle Spielfeld, welches gerade bespielt wird. */
 	private int aktSpielfeld;
 
 	/**
 	 * Eine Liste aller Spieler, die an diesem Spiel teilnehmen. <Spieler>
 	 * bedeutet, dass die ArrayList nur Werte des Typs Spieler aufnehmen kann
 	 * (und keine anderen Datentypen).
 	 */
 	private ArrayList<Spieler> alleSpieler;
 
 	/**
 	 * Erzeugt ein neues Objekt dieser Klasse
 	 * 
 	 * @param frame
 	 *            Der Frame, zu dem dieses Spiel gehoert.
 	 * @param anzLevel
 	 *            Die Anzahl der Level fuer dieses Spiel. Ein Level besteht aus
 	 *            ANZ_RAUM_PRO_LEVEL Raeumen.
 	 * @param textDatei
 	 *            Die Textdatei, die die Raeume enthaelt. Diese sollte die
 	 *            richtige Anzahl von Raeumen enthalten.
 	 */
 	public Spiel(Frame frame, int anzLevel, File textDatei) {
 
 		/* Erzeugt ein Array, welches Spielfelder beinhaltet. */
 		spielfelder = new ArrayList<Spielfeld>();
 		this.erzeugeSpielfelder(textDatei);
 
 		/* Noch gibt es kein Spielfeld.. */
 		this.aktSpielfeld = -1;
 
 		alleSpieler = new ArrayList<Spieler>();
 	}
 
 	/**
 	 * Erzeugt alle Spielfelder auf Basis der eingegebenen Datei.
 	 * 
 	 * @param textDatei
 	 *            Die Textdatei, die die Raeume enthaelt. Diese sollte die
 	 *            richtige Anzahl von Raeumen enthalten.
 	 */
 	private void erzeugeSpielfelder(File textDatei) {
 
 		BufferedReader reader = null;
 
 		try {
 
 			/* Datei oeffnen. */
 			reader = new BufferedReader(new FileReader(textDatei));
 		} catch (IOException e) {
 
 			this.parentFrame.zeigeFehler("Einlesefehler",
 					"Fehler beim oeffnen der Datei");
 		}
 
 		String zeile = null;
 
 		try {
 
 			/* Erste Zeile einlesen. */
 			zeile = reader.readLine();
 		} catch (IOException e) {
 
 			this.parentFrame.zeigeFehler("Einlesefehler",
 					"Aus der Datei konnte nicht gelesen werden");
 		}
 
 		/*
 		 * Die erste Zeile der Datei sollte die Groessenangaben fuer jedes
 		 * Spielfeld beinhalten. Anzahl Zeilen = Hoehe, Anzahl Spalten = Breite
 		 */
 		/* TODO: Validierung */
 		int hoehe = zeile.charAt(0);
 		int breite = zeile.charAt(1);
 
 		try {
 
 			/* Naechste Zeile.. */
 			zeile = reader.readLine();
 		} catch (IOException e) {
 
 			this.parentFrame.zeigeFehler("Einlesefehler",
 					"Aus der Datei konnte nicht gelesen werden");
 		}
 
 		/*
 		 * Speichert, in welcher Zeile DES AKTUELLEN SPIELFELDS wir uns
 		 * befinden. Variablen, die fuer mehrere Schleifendurchlaeufe verwendet
 		 * werden muessen ausserhalb der Schleife deklariert werden.
 		 */
 		int zeileNr = 0;
 		Spielfeld spielfeld = null;
 
 		/* Dateiende erreicht? */
 		while (zeile != null) {
 
 			if (zeileNr == 0) {
 
 				/* Erste Zeile, das heisst es kommt ein neues Spielfeld. */
 				spielfeld = new Spielfeld(hoehe, breite);
 			}
 
 			/* Ueber alle Zeichen in der Zeile iterieren. */
 			/* TODO: Validierung */
 			for (int i = 0; i < zeile.length(); i++) {
 
 				int zeichenWert = (int) zeile.charAt(i);
 
 				/* Haben wir auch das richtige Zeichen gelesen? ;-) */
 				System.out.println("erzeugeSpielfelder: Gelesenes Zeichen: "
 						+ zeichenWert);
 
 				/* Das passende Objekt fuer das gelesene Zeichen suchen.. */
 				Spielobjekt obj;
 				switch (zeichenWert) {
 
 				default:
 					obj = new Spieler();
 				}
 
 				/*
 				 * Darauf achten, dass das Objekt die richtige Position bekommt.
 				 * Hier: Zeile, Spalte.
 				 */
 				spielfeld.setzeObjektAnPos(obj, new Point(zeileNr, i));
 			}
 
 			try {
 
 				/* Naechste Zeile.. */
 				zeile = reader.readLine();
 			} catch (IOException e) {
 
 				this.parentFrame.zeigeFehler("Einlesefehler",
 						"Aus der Datei konnte nicht gelesen werden");
 			}
 
			if (zeile.equals("")) {
 
 				/*
				 * Leerzeile bedeutet, dass wir fuer dieses Spielfeld fertig
 				 * sind.
 				 */
 				spielfelder.add(spielfeld);
 				zeileNr = 0;
 			}
 		}
 	}
 
 	/**
 	 * Fuehrt eine Aktion, die durch einen Tastendruck initiert wurde, auf dem
 	 * Spiel aus.
 	 * 
 	 * @param key
 	 *            Die gedrueckte Taste
 	 */
 	public void aktion(int key) {
 
 		/*
 		 * Hier koennte man zwischen Tasten unterscheiden, die fuer das SPIEL sind
 		 * (z.B. zum Beenden oder Neustarten) und Tasten, die fuer das SPIELFELD
 		 * sind (z.B. Bewegung).
 		 */
 		if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_RIGHT
 				|| key == KeyEvent.VK_UP || key == KeyEvent.VK_DOWN) {
 
 			/*
 			 * TODO: Fuer welchen Spieler?
 			 */
 			spielfelder.get(aktSpielfeld).bewege(alleSpieler.get(0), key);
 
 			/*
 			 * Falls fuer jede Bewegung des Spielers auch noch die Gegner bewegt
 			 * werden sollen...
 			 */
 			spielfelder.get(aktSpielfeld).bewegeAlleGegner();
 		}
 
 		/* else if (...) */
 		/*
 		 * Genau so koennte man hier fuer andere Tasten verfahren. Wird z.B.
 		 * Leertaste gedrueckt, weiss das Spiel, dass das die Taste fuer die Waffe
 		 * fuer den ersten Spieler ist und teilt das dem Spielfeld mit.
 		 */
 		{
 			spielfelder.get(aktSpielfeld).aktion(alleSpieler.get(0), key);
 		}
 	}
 
 	/**
 	 * Gibt das aktuelle Spielfeld zurueck.
 	 * 
 	 * @return Das aktuelle Spielfeld
 	 */
 	public Spielfeld getAktuellesSpielfeld() {
 
 		return spielfelder.get(aktSpielfeld);
 	}
 
 }
