 package de.uni_hamburg.informatik.sep.zuul.server.befehle;
 
 import java.util.Arrays;
 import java.util.List;
 
 import de.uni_hamburg.informatik.sep.zuul.server.features.Katze;
 import de.uni_hamburg.informatik.sep.zuul.server.inventar.Item;
 import de.uni_hamburg.informatik.sep.zuul.server.npcs.Maus;
 import de.uni_hamburg.informatik.sep.zuul.server.raum.Raum;
 import de.uni_hamburg.informatik.sep.zuul.server.spiel.Spieler;
 import de.uni_hamburg.informatik.sep.zuul.server.util.FancyFunction;
 import de.uni_hamburg.informatik.sep.zuul.server.util.ServerKontext;
 import de.uni_hamburg.informatik.sep.zuul.server.util.TextVerwalter;
 
 class BefehlFuettere implements Befehl
 {
 	/**
 	 * Bestimmt die Richtung, die die Maus empfiehlt abhängig davon, ob der
 	 * Kuchen giftig ist.
 	 */
 	static String bestimmeRichtung(Item kuchen, String richtigeRichtung,
 			String[] moeglicheRichtungen)
 	{
 		if(kuchen == Item.UKuchen || kuchen == Item.IKuchen)
 		{
 			return richtigeRichtung;
 		}
 		if(kuchen == Item.UGiftkuchen || kuchen == Item.IGiftkuchen)
 		{
			List<String> richtungen = Arrays.asList(moeglicheRichtungen);
 
 			richtungen.remove(richtigeRichtung);
 
 			String falscheRichtung = FancyFunction.getRandomEntry(richtungen);
 
 			// Falls der Raum nur einen Ausgang hat.
 			if(falscheRichtung == null)
 				falscheRichtung = richtigeRichtung;
 
 			return falscheRichtung;
 		}
 
 		return null;
 	}
 
 	@Override
 	public boolean vorbedingungErfuellt(ServerKontext kontext, Spieler spieler,
 			Befehlszeile befehlszeile)
 	{
 		// Wenn eine Katze oder eine Maus gefüttert werden könnte
 		Raum raum = kontext.getAktuellenRaumZu(spieler);
 		return ((raum.hasKatze() && !raum.getKatze().isSatt()) || raum
 				.hasMaus()) && spieler.getInventar().hasAnyKuchen();
 	}
 
 	@Override
 	public boolean ausfuehren(ServerKontext kontext, Spieler spieler,
 			Befehlszeile befehlszeile)
 	{
 		// Versuche eine Katze oder eine Maus zu füttern
 
 		Item kuchen = spieler.getInventar().getAnyKuchen();
 		return fuettereTierMit(kontext, spieler, kuchen);
 	}
 
 	static boolean fuettereTierMit(ServerKontext kontext, Spieler spieler,
 			Item kuchen)
 	{
 		Raum raum = kontext.getAktuellenRaumZu(spieler);
 
 		if(raum.hasKatze())
 		{
 			raum.getKatze().fuettere(kontext, spieler, kuchen);
 			return true;
 		}
 		else if(raum.hasMaus())
 		{
 			return fuettereMaus(kontext, spieler, kuchen, raum, raum.getMaus());
 		}
 		return false;
 	}
 
 	static boolean fuettereMaus(ServerKontext kontext, Spieler spieler,
 			Item kuchen, Raum aktuellerRaum, Maus maus)
 	{
 		String richtigeRichtung = maus.getRichtung();
 
 		String[] moeglicheRichtungen = aktuellerRaum.getMoeglicheAusgaenge();
 
 		String richtung = bestimmeRichtung(kuchen, richtigeRichtung,
 				moeglicheRichtungen);
 
 		String richtungsangabe = String.format(
 				TextVerwalter.MAUS_RICHTUNGSANGABE, richtung);
 
 		kontext.schreibeAnSpieler(spieler, richtungsangabe);
 
 		return true;
 	}
 
 	static boolean fuettereKatze(ServerKontext kontext, Spieler spieler,
 			Katze katze, Item kuchen)
 	{
 		katze.fuettere(kontext, spieler, kuchen);
 
 		return true;
 	}
 
 	@Override
 	public void gibFehlerAus(ServerKontext kontext, Spieler spieler,
 			Befehlszeile befehlszeile)
 	{
 		Raum raum = kontext.getAktuellenRaumZu(spieler);
 
 		if(!spieler.getInventar().hasAnyKuchen())
 		{
 			kontext.schreibeAnSpieler(spieler, TextVerwalter.MAUS_KEIN_KRUEMEL);
 		}
 		else if(!raum.hasKatze() && !raum.hasMaus())
 		{
 			kontext.schreibeAnSpieler(spieler,
 					TextVerwalter.BEFEHL_FUETTERE_NICHTS_DA_ZUM_FUETTERN);
 		}
 		else if(raum.hasKatze() && raum.getKatze().isSatt())
 		{
 			kontext.schreibeAnSpieler(spieler,
 					TextVerwalter.KATZE_HAT_KEINEN_HUNGER);
 		}
 	}
 
 	@Override
 	public String[] getBefehlsnamen()
 	{
 		return new String[] { TextVerwalter.BEFEHL_FUETTERE };
 	}
 
 	@Override
 	public String getHilfe()
 	{
 		return TextVerwalter.HILFE_FEED;
 	}
 }
