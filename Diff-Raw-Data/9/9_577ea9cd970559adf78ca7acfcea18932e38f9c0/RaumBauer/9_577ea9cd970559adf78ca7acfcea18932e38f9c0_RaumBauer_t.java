 package de.uni_hamburg.informatik.sep.zuul.server.raum;
 
 import java.util.ArrayList;
 import java.util.Map;
 
 import de.uni_hamburg.informatik.sep.zuul.server.npcs.Maus;
 import de.uni_hamburg.informatik.sep.zuul.server.util.FancyFunction;
 import de.uni_hamburg.informatik.sep.zuul.server.util.TextVerwalter;
 
 public class RaumBauer
 {
 	private Raum _startRaum;
 	private Raum _endRaum;
 
 	public RaumBauer(RaumStruktur struktur)
 	{
 		// initialisiereRaeumeHart();
 		initialisiereRaeume(struktur.getConnections());
 	}
 
 	/**
 	 * Verbinde alle Räume.
 	 * 
 	 * @param verbindungen
 	 */
 	private void initialisiereRaeume(Map<Raum, Raum[]> verbindungen)
 	{
 		ArrayList<Raum> kannMausEnthaltenRaum = new ArrayList<Raum>();
 
 		for(Raum raum : verbindungen.keySet())
 		{
 			raum.verbindeZweiRaeume(TextVerwalter.RICHTUNG_NORDEN,
 					verbindungen.get(raum)[0], TextVerwalter.RICHTUNG_SUEDEN);
 			raum.verbindeZweiRaeume(TextVerwalter.RICHTUNG_OSTEN,
 					verbindungen.get(raum)[1], TextVerwalter.RICHTUNG_WESTEN);
 			raum.verbindeZweiRaeume(TextVerwalter.RICHTUNG_SUEDEN,
 					verbindungen.get(raum)[2], TextVerwalter.RICHTUNG_NORDEN);
 			raum.verbindeZweiRaeume(TextVerwalter.RICHTUNG_WESTEN,
 					verbindungen.get(raum)[3], TextVerwalter.RICHTUNG_OSTEN);
 
 			if(raum.getRaumart() == RaumArt.Start)
 			{
 				_startRaum = raum;
 			}
 			if(raum.getRaumart() == RaumArt.Ende)
 			{
 				_endRaum = raum;
 			}
 			if(raum.getRaumart() != RaumArt.Ende
 					&& raum.getRaumart() != RaumArt.Start)
 				kannMausEnthaltenRaum.add(raum);
 		}
 
 		// TODO: Maus anzahl auslesen
 		// Setze 3 Mäuse zufällig.
 
 		if(kannMausEnthaltenRaum.size() > 0)
 			mausInRaumSetzen(kannMausEnthaltenRaum, 3);
 	}
 
 	public void mausInRaumSetzen(ArrayList<Raum> kannMausEnthaltenRaum, int i)
 	{
 		for(; i > 0 && kannMausEnthaltenRaum.size() > 0; --i)
 		{
 			Raum r = mausInRaumSetzen(kannMausEnthaltenRaum);
 			kannMausEnthaltenRaum.remove(r);
 		}
 	}
 
 	/**
 	 * @param kannMausEnthaltenRaum
 	 */
 	public Raum mausInRaumSetzen(ArrayList<Raum> kannMausEnthaltenRaum)
 	{
 
 		Raum mausRaum = FancyFunction.getRandomEntry(kannMausEnthaltenRaum);
		if(mausRaum == null)
 			return null;
 
 		mausRaum.setMaus(new Maus(mausRaum, _endRaum));
 
 		return mausRaum;
 	}
 
 	/**
 	 * Gibt den Startraum zurück, von dem aus der Spieler startet.
 	 * 
 	 * @return Der Startraum
 	 */
 	public Raum getStartRaum()
 	{
 		return _startRaum;
 	}
 }
