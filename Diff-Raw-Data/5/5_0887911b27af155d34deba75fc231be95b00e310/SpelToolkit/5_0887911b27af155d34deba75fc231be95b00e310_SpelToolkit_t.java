 package ambiorix;
 
 import java.util.Vector;
 
 import ambiorix.spelbord.BordPositie;
 import ambiorix.spelbord.Gebied;
 import ambiorix.spelbord.Pion;
 import ambiorix.spelbord.PionTypeVerzameling;
 import ambiorix.spelbord.Spelbord;
 import ambiorix.spelbord.Tegel;
 import ambiorix.spelbord.Terrein;
 import ambiorix.spelers.Antwoord;
 import ambiorix.spelers.Speler;
 
 public class SpelToolkit {
 	private Vector<Speler> spelers;
 	private Spelbord spelbord;
 
 	public SpelToolkit(Vector<Speler> spelers, Spelbord spelbord) {
 		this.spelers = spelers;
 		this.spelbord = spelbord;
 	}
 
 	// van Spel
 	
 	public int getAantalSpelers() {
 		return spelers.size();
 	}
 
 	@Deprecated
 	public Vector<Speler> getSpelers() {
 		return spelers;
 	}	
 
 	public Speler getActieveSpeler() {
 		for(Speler s : spelers) {
 			if(s.isActief())
 				return s;
 		}
 		
 		return null;
 	}
 
 	public void setActieveSpeler(Speler actieveSpeler) {
 		Speler nuActief = getActieveSpeler();
 		nuActief.zetActief(false);
 		actieveSpeler.zetActief(true);
 	}
 
 	// van Spelbord
 	
 	public boolean positieMogelijk(Tegel t, BordPositie p) {
 		return t.kanBuurAccepteren(p.getBuur(), p.getRichting());
 	}
 	
 	// van typeverzamelingen
 	
 	public Pion getPion(String piontype) {
 		return new Pion(0,PionTypeVerzameling.getInstantie().getType(piontype));
 	}
 
 	public void setTegelAantal(String tegelType, int hoeveelheid) {
 		spelbord.setTegelAantal(tegelType, hoeveelheid);
 	}
 	
 	// Van Speler
 	//  1) Input
 	public BordPositie selecteerBordPositie(Speler s) throws InterruptedException {
 		Antwoord a = s.selecteerBordPositie();
 		return a.getPosities().get(0);
 	}
 	
 	public Tegel selecteerSpelerTegel(Speler s) throws InterruptedException {
 		Antwoord a = s.selecteerSpelerTegel();
 		return a.getTegels().get(0);
 	}
 	
 
 	public Terrein selecteerTegelGebied(Speler s) throws InterruptedException {
 		Antwoord a = s.selecteerTegelGebied();
 		return a.getTerreinen().get(0);
 	}
 	
 	public Pion selecteerSpelerPion(Speler s) throws InterruptedException {
 		Antwoord a = s.selecteerSpelerPion();
 		return a.getPionnen().get(0);
 	}
 	
 	//  2) Output
 	public void zetTegel(Speler s, Tegel t, BordPositie p) {
		spelbord.plaatsTegel(t, p);
 		s.zetTegel(t, p);
 	}
 	
 	public void geefSpelerTegel(Tegel t, Speler s) {
 		s.addTegel(t);
 	}
 	
 	public void geefSpelerPion(Pion p, Speler s) {
 		s.addPion(p);
 	}
 	
 	public void neemSpelerTegelAf(Tegel t, Speler s) {
 		s.deleteTegel(t);
 	}
 
 	// 3) spelbord functies ROBIN
 	public Vector<BordPositie> controleerGlobalePlaatsbaarheid(Tegel tegel,
 			boolean stopDirect) {
 		return spelbord.controleerGlobalePlaatsbaarheid(tegel, stopDirect);
 	}
 
 	public boolean controleerPlaatsbaarheid(Pion pion, Terrein terrein) {
 		return spelbord.controleerPlaatsbaarheid(pion, terrein);
 	}
 
 	public boolean controleerPlaatsbaarheid(Tegel tegel, BordPositie positie) {
 		return spelbord.controleerPlaatsbaarheid(tegel, positie);
 	}
 
 	public Gebied getGebied(Terrein start) {
 		return spelbord.getGebied(start);
 	}
 
 	public Tegel getLaatstGeplaatsteTegel() {
 		return spelbord.getLaatstGeplaatsteTegel();
 	}
 
 	public int getTegelAantal(String tegelType) {
 		return spelbord.getTegelAantal(tegelType);
 	}
 
 	public void plaatsPion(Pion pion, Terrein terrein) {
 		spelbord.plaatsPion(pion, terrein);
 	}
 
 	public void plaatsTegel(Tegel tegel, BordPositie positie) {
 		spelbord.plaatsTegel(tegel, positie);
 	}
 
 	public void setBegintegel(Tegel beginTegel) {
 		spelbord.setBegintegel(beginTegel);
		Speler actief = getActieveSpeler();
		actief.zetTegel(beginTegel, new BordPositie(null, null));
 	}
 
 	public void verwijderPion(Pion pion) {
 		spelbord.verwijderPion(pion);
 	}
 
 	public void verwijderPion(Terrein positie) {
 		spelbord.verwijderPion(positie);
 	}
 
 	public void verwijderTegel(Tegel tegel) {
 		spelbord.verwijderTegel(tegel);
 	}
 	
 	public Tegel getVolgendeTegel()
 	{
 		return spelbord.getVolgendeTegel();
 	}
 }
