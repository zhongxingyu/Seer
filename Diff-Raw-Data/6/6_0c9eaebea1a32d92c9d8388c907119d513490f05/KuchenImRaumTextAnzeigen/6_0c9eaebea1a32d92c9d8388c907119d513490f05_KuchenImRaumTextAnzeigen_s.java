 package de.uni_hamburg.informatik.sep.zuul.server.features;
 
 import de.uni_hamburg.informatik.sep.zuul.server.befehle.BefehlFactory;
 import de.uni_hamburg.informatik.sep.zuul.server.spiel.Spieler;
 import de.uni_hamburg.informatik.sep.zuul.server.util.ServerKontext;
 import de.uni_hamburg.informatik.sep.zuul.server.util.TextVerwalter;
 
 public final class KuchenImRaumTextAnzeigen implements Feature,
 		BefehlAusgefuehrtListener
 {
 	@Override
 	public boolean befehlAusgefuehrt(ServerKontext kontext, Spieler spieler,
 			boolean hasRoomChanged)
 	{
 		if(hasRoomChanged)
 		{
 			switch (kontext.getAktuellenRaumZu(spieler).getNaechstesItem())
 			{
			case Kuchen:
			case Giftkuchen:
 				BefehlFactory.schreibeNL(kontext, spieler,
 						TextVerwalter.KUCHENIMRAUMTEXT);
 			}
 		}
 
 		return true;
 	}
 }
