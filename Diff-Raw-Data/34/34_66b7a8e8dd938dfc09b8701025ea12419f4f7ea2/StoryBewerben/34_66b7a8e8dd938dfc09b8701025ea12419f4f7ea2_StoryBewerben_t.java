 package de.openjobs.entities;
 		
 import org.junit.Before;
 import org.junit.Test;
 
 import de.openjobs.entities.TestDriver;
 
 
 
 /**
  * JUnit test case generated from the file StoryBewerben.natspec. 
  *
  * PLEASE do not modify this file. It will be overwritten by any changes
  * in StoryBewerben.natspec. 
  */
 public class StoryBewerben {
 
 	private TestDriver testDriver;
 
 	@Test
 	public void executeScript() throws Exception {
 		// Der Arbeitgeber erstellt ein Stellenangebot. 
 		de.openjobs.entities.Stellenangebot stellenangebot_ = testDriver.createStellenangebot();
 		// Das Ergebnis ist ein Artefakt. 
 		testDriver.istEinArtefakt(stellenangebot_);
		// Die Anzahl der Stellen vom Stellenangebot wird auf 1 begrenzt. 
		testDriver.setzeMaxAnzahlStellen(stellenangebot_, 1);
 		// Das Stellenangebot kann auf 1 Stelle(n) vermittelt werden. 
		testDriver.vermittelbarAufStellen(stellenangebot_, 1);
 		// Der Arbeitssuchende erstellt ein Stellengesuch. 
 		de.openjobs.entities.Stellengesuch stellengesuch_ = testDriver.createStellengesuch();
 		// Das Ergebnis ist ein Artefakt. 
 		testDriver.istEinArtefakt(stellengesuch_);
 		
 	}
 	
 	@Before
 	public void setUp() {
 		testDriver = new TestDriver();
 	}
 }
