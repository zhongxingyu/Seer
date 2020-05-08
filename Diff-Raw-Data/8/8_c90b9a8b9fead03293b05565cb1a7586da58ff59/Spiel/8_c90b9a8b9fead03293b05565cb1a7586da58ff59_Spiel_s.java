 package de.uni_hamburg.informatik.sep.zuul.server.spiel;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Observable;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.swing.SwingUtilities;
 
 import de.uni_hamburg.informatik.sep.zuul.client.ClientPaket;
 import de.uni_hamburg.informatik.sep.zuul.client.ClientVorschauPaket;
 import de.uni_hamburg.informatik.sep.zuul.server.befehle.Befehl;
 import de.uni_hamburg.informatik.sep.zuul.server.befehle.BefehlFactory;
 import de.uni_hamburg.informatik.sep.zuul.server.befehle.BefehlSchauen;
 import de.uni_hamburg.informatik.sep.zuul.server.befehle.Befehlszeile;
 import de.uni_hamburg.informatik.sep.zuul.server.raum.Raum;
 import de.uni_hamburg.informatik.sep.zuul.server.util.ServerKontext;
 import de.uni_hamburg.informatik.sep.zuul.server.util.TextVerwalter;
 
 /**
  * Dies ist die Hauptklasse der Anwendung "Die Welt von Zuul". "Die Welt von
  * Zuul" ist ein sehr einfaches, textbasiertes Adventure-Game. Ein Spieler kann
  * sich in einer Umgebung bewegen, mehr nicht. Das Spiel sollte auf jeden Fall
  * ausgebaut werden, damit es interessanter wird!
  * 
  * Zum Spielen muss eine Instanz dieser Klasse erzeugt werden und an ihr die
  * Methode "spielen" aufgerufen werden.
  * 
  * Diese Instanz erzeugt und initialisiert alle anderen Objekte der Anwendung:
  * Sie legt alle Räume und einen Parser an und startet das Spiel. Sie wertet
  * auch die Befehle aus, die der Parser liefert, und sorgt für ihre Ausführung.
  * 
  * Das Ausgangssystem basiert auf einem Beispielprojekt aus dem Buch
  * "Java lernen mit BlueJ" von D. J. Barnes und M. Kölling.
  */
 public class Spiel extends Observable
 {
 	public static final long ONE_SECOND = 1000;
 	private SpielLogik _logik;
 	private Map<String, Spieler> _spielerMap;
 	//	private Map<Spieler, String> _nachrichtenMap;
 	private boolean _gestartet;
 
 	/**
 	 * Erzeuge ein neues Spiel
 	 * 
 	 * @param server
 	 */
 	public Spiel()
 	{
 		_logik = new SpielLogik();
 		_spielerMap = new HashMap<String, Spieler>();
 		setGestartet(false);
 	}
 
 	/**
 	 * Erzeuge einen neuen Spieler aus dem übergebenen Namen. Sein Inventar ist
 	 * leer und er bekommt die Standardlebensenergie. Siehe auch
 	 * {@link SpielLogik}.
 	 * 
 	 * @param name
 	 *            Name des Spielers
 	 */
 	public void meldeSpielerAn(String name)
 	{
 		Spieler spieler = _logik.erstelleNeuenSpieler(name);
 		_spielerMap.put(name, spieler);
 	}
 
 	/**
 	 * melde den Spieler mit dem Namen name ab.
 	 * 
 	 * @param name
 	 *            Spielername
 	 */
 	public void meldeSpielerAb(String name)
 	{
 		_logik.meldeSpielerAb(name);
 		_spielerMap.remove(name);
 	}
 
 	/**
 	 * Schablonenmethode für Aktionen bei beendetem Spiel.
 	 */
 	public void beendeSpiel()
 	{
 		setGestartet(false);
 	}
 
 	/**
 	 * Führt das Spiel aus.
 	 */
 	public void spielen()
 	{
 		setGestartet(true);
 		zeigeWillkommensText();
 	}
 
 	/**
 	 * Zeige jedem angemeldeten Spieler den Willkommenstext an.
 	 */
 	private void zeigeWillkommensText()
 	{
 		for(Spieler spieler : _spielerMap.values())
 		{
 			_logik.getKontext().schreibeAnSpieler(spieler,
 					TextVerwalter.EINLEITUNGSTEXT);
 		}
 	}
 
 	/**
 	 * Verarbeite die Eingabe eines Spielers.
 	 * 
 	 * @param eingabezeile
 	 * @param eingabe
 	 */
 	public void verarbeiteEingabe(String benutzerName, String eingabe)
 	{
 
 		System.err.println(benutzerName + ": " + eingabe);
 
 		Spieler spieler = _logik.getKontext().getSpielerByName(benutzerName);
 
 		// Spieler von der Karte entfernt?
 		if(spieler == null)
 			return;
 
 		Befehlszeile befehlszeile = new Befehlszeile(eingabe);
 		Befehl befehl = BefehlFactory.gibBefehl(befehlszeile);
 
 		if(befehl != null)
 		{
 			if(!_logik.fuehreBefehlAusgefuehrenListenerAus(spieler, befehl))
 				return;
 
 			Raum alterRaum = _logik.getKontext().getAktuellenRaumZu(spieler);
 
 			boolean result = Spiel.versucheBefehlAusfuehrung(
 					_logik.getKontext(), spieler, befehlszeile, befehl);
 
 			Raum neuerRaum = _logik.getKontext().getAktuellenRaumZu(spieler);
 
 			// Wenn der Befehl erfolgreich ausgeführt wurde, rufe die Listener auf.
 			if(result)
 				_logik.fuehreBefehlAusgefuehrtListenerAus(spieler, befehl,
 						alterRaum != neuerRaum);
 
 			if(befehl instanceof BefehlSchauen)
 			{
 				String richtung = ((BefehlSchauen) befehl)
 						.extrahiereRichtung(befehlszeile);
 				if(alterRaum.getAusgang(richtung) != null)
 				{
 					String[] ar = { spieler.getName(), richtung };
 					setChanged();
 					notifyObservers(ar);
 				}
 			}
 
 			// Entferne tote Spieler von Landkarte
			// TODO wird nicht ausgeführt
 			if(!spieler.lebendig())
 			{
 				_logik.getKontext().entferneSpieler(spieler);
 			}
 		}
 		else
 			_logik.getKontext().schreibeAnSpieler(spieler,
 					TextVerwalter.FALSCHEEINGABE);
 	}
 
 	/**
 	 * Starte das Spiel neu.
 	 * 
 	 * @param level
 	 */
 	protected void restart(String level)
 	{
 		_logik.beendeSpiel();
 		spielen();
 	}
 
 	/**
 	 * Packe das Clientpaket für den Client mit dem Namen name.
 	 * 
 	 * @param name
 	 * @return
 	 */
 	public ClientPaket packePaket(String name)
 	{
 		Spieler spieler = _spielerMap.get(name); //hole den Spieler mit dem namen
 		String nachricht = _logik.getKontext().getNachrichtFuer(spieler); // hole die nachricht für den spieler
 		return new ClientPaket(_logik.getKontext(), spieler, nachricht); //packe
 
 	}
 
 	public ClientPaket packeVorschauPaket(String name, String richtung)
 	{
 		Spieler spieler = _spielerMap.get(name); //hole den Spieler mit dem namen
 		String nachricht = _logik.getKontext().getNachrichtFuer(spieler); // hole die nacricht für den spieler
 		return new ClientVorschauPaket(_logik.getKontext(), spieler, nachricht,
 				richtung); //packe VorschauPacket!
 	}
 
 	public static boolean versucheBefehlAusfuehrung(ServerKontext kontext,
 			Spieler spieler, Befehlszeile befehlszeile, Befehl befehl)
 	{
 		if(befehl.vorbedingungErfuellt(kontext, spieler, befehlszeile))
 		{
 			return befehl.ausfuehren(kontext, spieler, befehlszeile);
 		}
 		else
 		{
 			befehl.gibFehlerAus(kontext, spieler, befehlszeile);
 			return false;
 		}
 	}
 
 	/**
 	 * @return the gestartet
 	 */
 	public boolean isGestartet()
 	{
 		return _gestartet;
 	}
 
 	TimerTask _tickTimer = new TimerTask()
 	{
 
 		@Override
 		public void run()
 		{
 			SwingUtilities.invokeLater(new Runnable()
 			{
 
 				@Override
 				public void run()
 				{
 					_logik.fuehreTickListenerAus();
 					setChanged();
 					notifyObservers(null);
 				}
 			});
 		}
 	};
 
 	/**
 	 * @param gestartet
 	 *            the gestartet to set
 	 */
 	public void setGestartet(boolean gestartet)
 	{
 		if(!gestartet && _gestartet)
 		{
 			_tickTimer.cancel();
 		}
 		else if(gestartet && !_gestartet)
 		{
 			new Timer().schedule(_tickTimer, ONE_SECOND, ONE_SECOND);
 		}
 		_gestartet = gestartet;
 	}
 }
