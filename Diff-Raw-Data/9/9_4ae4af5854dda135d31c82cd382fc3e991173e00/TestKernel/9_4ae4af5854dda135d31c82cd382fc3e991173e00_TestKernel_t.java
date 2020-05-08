 package aeroport.sgbag.kernel;
 
 import static org.junit.Assert.*;
 import java.util.ArrayList;
 import java.util.LinkedList;
 
 import lombok.extern.log4j.Log4j;
 
 import org.apache.log4j.PropertyConfigurator;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 @Log4j
 public class TestKernel {
 
 	private Hall hall;
 	private ArrayList<ElementCircuit> simpleList;
 
 	@BeforeClass
 	public static void setUpBeforeClass() throws Exception {
 		PropertyConfigurator.configure("log4j.properties");
 	}
 
 	@Before
 	// Cette méthode s'exécute avant chaque test mais ne sert que pour le test
 	// simple
 	public void setUpBefore() throws Exception {
 
 		// Tapis roulants
 		TapisRoulant tapis1 = new TapisRoulant(200, 5, 30, true);
 
 		// Toboggans
 		Toboggan toboggan1 = new Toboggan();
 		toboggan1.setAutoDeleteBagages(true);
 
 		// Circuit
		final int longeurRails = 150;
 		Rail r1 = new Rail(longeurRails);
 		Rail r2 = new Rail(longeurRails);
 
 		ConnexionCircuit n1 = new ConnexionCircuit(tapis1);
 		LinkedList<Rail> n1sortie = new LinkedList<Rail>();
 		n1sortie.add(r1);
 		n1.setRailsSortie(n1sortie);
 
 		ConnexionCircuit n2 = new ConnexionCircuit(toboggan1);
 
 		Noeud n3 = new Noeud();
 		LinkedList<Rail> n3sortie = new LinkedList<Rail>();
 		n3sortie.add(r2);
 		n3.setRailsSortie(n3sortie);
 
 		r1.setNoeudSuivant(n3);
 		r2.setNoeudSuivant(n2);
 
 		simpleList = new ArrayList<ElementCircuit>();
 		simpleList.add(n1);
 		simpleList.add(n2);
 		simpleList.add(n3);
 
 		simpleList.add(r1);
 		simpleList.add(r2);
 
 		Circuit circuit = new Circuit(simpleList);
 
 		hall = new Hall();
 		hall.addFileBagage(tapis1);
 		hall.addFileBagage(toboggan1);
 		hall.setCircuit(circuit);
 		hall.init();
 
 		LinkedList<ElementCircuit> chemin = null;
 		chemin = circuit.calculChemin(n1, n2);
 		Chariot c = new Chariot(5, n2, chemin);
 		c.setLength(30);
 		n1.registerChariot(c);
 	}
 
 	@Test
 	public void test() {
 		for (int i = 0; i < 1000; i++) {
 			hall.update();
 			try {
 				Thread.sleep(100);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 
 	}
 /*
 	@Test
 	public void testComplique() {
 
 		// Tapis roulants
 		TapisRoulant tapis1 = new TapisRoulant(200, 5, 30, true);
 		TapisRoulant tapis2 = new TapisRoulant(200, 5, 30, true);
 
 		// Toboggans
 		Toboggan toboggan1 = new Toboggan();
 		toboggan1.setAutoDeleteBagages(true);
 		Toboggan toboggan2 = new Toboggan(20, 0, true);
 		toboggan2.setAutoDeleteBagages(true);
 		
 		// Circuit
 
 		final int longeurRails = 200;
 		Rail r1 = new Rail(longeurRails);
 		Rail r2 = new Rail(longeurRails);
 		Rail r3 = new Rail(longeurRails);
 		Rail r4 = new Rail(longeurRails);
 		Rail r5 = new Rail(longeurRails);
 		Rail r6 = new Rail(longeurRails);
 		Rail r7 = new Rail(longeurRails);
 
 		ConnexionCircuit n1 = new ConnexionCircuit(tapis1);
 		tapis1.setConnexionCircuit(n1);
 		LinkedList<Rail> n1sortie = new LinkedList<Rail>();
 		n1sortie.add(r1);
 		n1.setRailsSortie(n1sortie);
 
 		ConnexionCircuit n2 = new ConnexionCircuit(tapis2);
 		tapis2.setConnexionCircuit(n2);
 		LinkedList<Rail> n2sortie = new LinkedList<Rail>();
 		n2sortie.add(r2);
 		n2.setRailsSortie(n2sortie);
 
 		Noeud n3 = new Noeud();
 		LinkedList<Rail> n3sortie = new LinkedList<Rail>();
 		n3sortie.add(r3);
 		n3sortie.add(r7);
 		n3.setRailsSortie(n3sortie);
 
 		ConnexionCircuit n4 = new ConnexionCircuit(toboggan1);
 		toboggan1.setConnexionCircuit(n4);
 		LinkedList<Rail> n4sortie = new LinkedList<Rail>();
 		n4sortie.add(r4);
 		n4.setRailsSortie(n4sortie);
 
 		ConnexionCircuit n5 = new ConnexionCircuit(toboggan2);
 		toboggan2.setConnexionCircuit(n5);
 		LinkedList<Rail> n5sortie = new LinkedList<Rail>();
 		n5sortie.add(r5);
 		n5.setRailsSortie(n5sortie);
 
 		Noeud n6 = new Noeud();
 		LinkedList<Rail> n6sortie = new LinkedList<Rail>();
 		n6sortie.add(r6);
 		n6.setRailsSortie(n6sortie);
 
 		r1.setNoeudSuivant(n2);
 		r2.setNoeudSuivant(n3);
 		r3.setNoeudSuivant(n4);
 		r4.setNoeudSuivant(n5);
 		r5.setNoeudSuivant(n6);
 		r6.setNoeudSuivant(n1);
 		r7.setNoeudSuivant(n6);
 
 		simpleList = new ArrayList<ElementCircuit>();
 		simpleList.add(n1);
 		simpleList.add(n2);
 		simpleList.add(n3);
 		simpleList.add(n4);
 		simpleList.add(n5);
 		simpleList.add(n6);
 
 		simpleList.add(r1);
 		simpleList.add(r2);
 		simpleList.add(r3);
 		simpleList.add(r4);
 		simpleList.add(r5);
 		simpleList.add(r6);
 		simpleList.add(r7);
 
 		Circuit circuit = new Circuit(simpleList);
 
 		hall = new Hall();
 		hall.setCircuit(circuit);
 
 		LinkedList<ElementCircuit> chemin = null;
 		chemin = circuit.calculChemin(n3, n2);
 		Chariot c = new Chariot(5, n2, chemin);
 		n3.registerChariot(c);
 
 		for (int i = 0; i < 200; i++) {
 			hall.update();
 			try {
 				Thread.sleep(200);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 
 		fail("Not yet implemented");
 	}
 */
 }
