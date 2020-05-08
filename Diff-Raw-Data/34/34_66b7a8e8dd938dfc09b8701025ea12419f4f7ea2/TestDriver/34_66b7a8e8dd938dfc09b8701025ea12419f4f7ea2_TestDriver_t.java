 package de.openjobs.entities;
 
 import junit.framework.Assert;
 import de.devboost.natspec.annotations.TextSyntax;
 
 public class TestDriver {
 
 	public TestDriver() {
 		super();
 	}
 
 	@TextSyntax("Der Arbeitgeber erstellt ein Stellenangebot.")
 	public Stellenangebot createStellenangebot() {
 		final Arbeitgeber arbeitgeber = new Arbeitgeber();
 		return arbeitgeber.erstelleStellenangebot();
 	}
 
 	@TextSyntax("Das Ergebnis ist ein Artefakt.")
 	public void istEinArtefakt(Stellenangebot ergebnis) {
 		Assert.assertNotNull(ergebnis);
 	}
 
 
	@TextSyntax("Die Anzahl der Stellen vom Stellenangebot wird auf #2 begrenzt.")
 	public void setzeMaxAnzahlStellen(Stellenangebot stellenangebot,Integer anzahl) {
 		stellenangebot.setAnzahlStellen(anzahl);
 	}
 
	@TextSyntax("Das Stellenangebot kann auf #2 Stelle(n) vermittelt werden.")
	public void vermittelbarAufStellen(Stellenangebot stellenangebot,Integer anzahl) {
		final Integer stellen=stellenangebot.getAnzahlStellen();
		Assert.assertEquals(anzahl, stellen);
 	}
 
 	@TextSyntax("Der Arbeitssuchende erstellt ein Stellengesuch.")
 	public Stellengesuch createStellengesuch() {
 		return new Stellengesuch();
 	}
 
 	@TextSyntax("Das Ergebnis ist ein Artefakt.")
 	public void istEinArtefakt(Stellengesuch ergebnis) {
 		Assert.assertNotNull(ergebnis);
 	}
 	
 	@TextSyntax("Der Arbeitssuchende erstellt eine Bewerbung zu einem Stellenangebot.")
	public Bewerbung createBewerbung(Arbeitsuchender arbeitssuchender,Stellenangebot stellenangebot) {
 		return new Bewerbung();
 	}
 
	@TextSyntax("Das Ergebnis ist eine valide Bewerbung.")
	public void istEinArtefakt(Bewerbung bewerbung) {
		Assert.assertNotNull(bewerbung);
	}
	
 	@TextSyntax("Der Arbeitgeber stellt stellt den Arbeitssuchenden der Bewerbung zum Stellenangebot ein.")
 	public void einstellenArbeitssuchenden(Bewerbung bewerbung) {
 		Assert.assertTrue(false);
 	}
 
 
 	@TextSyntax("Als Ergebnis wird kein Fehler erwartet.")
 	public void noException(Object ergebnis) {
 		Assert.assertTrue(false);
 	}
 
 	
 
 }
