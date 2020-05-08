 
 
 import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 
 import lombok.extern.log4j.Log4j;
 
 import org.apache.log4j.PropertyConfigurator;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import aeroport.sgbag.controler.Simulation;
 import aeroport.sgbag.kernel.Chariot;
 import aeroport.sgbag.kernel.ElementCircuit;
 import aeroport.sgbag.kernel.Noeud;
 import aeroport.sgbag.kernel.TapisRoulant;
 import aeroport.sgbag.utils.CircuitGenerator;
 import aeroport.sgbag.views.VueHall;
 import aeroport.sgbag.views.VueRail;
import aeroport.sgbag.views.VueTapisRoulant;
 
 @Log4j
 public class SimulationAndCircuitGenerator {
 
 	@BeforeClass
 	public static void setUpBeforeClass() throws Exception {
 		PropertyConfigurator.configure("log4j.properties");
 	}
 
 	@Test
 	public void test() {
 		Display display = new Display();
 		Shell shell = new Shell(display);
 		shell.setText("VueTest");
 		shell.setLayout(new FillLayout());
 		shell.setSize(800, 800);
 		VueHall vueHall = new VueHall(shell, SWT.NONE);
 		vueHall.setSize(300, 300);
 		Simulation s = new Simulation(vueHall);
 		
 		CircuitGenerator cg = new CircuitGenerator(vueHall);
 
 		Point p1 = new Point(20, 20);
 		Point p2 = new Point(100, 20);
 		Point p3 = new Point(200, 100);
 		Point p4 = new Point(500, 500);
 		Point p5 = new Point(200, 300);
 		Point p6 = new Point(40, 400);
 		
 		cg.createSegment(p1, p2);
 		cg.createSegment(p2, p3);
 		cg.createSegment(p3, p4);
 		cg.createSegment(p4, p5);
 		VueRail vueRail1 = cg.createSegment(p5, p6);
 		cg.createSegment(p6, p1);
 		//cg.createSegment(p3, p5);
 		cg.createExit(p3);
 		
 		TapisRoulant tapis = cg.createEntry(p5, 100, 1, 25, true).getTapisRoulant();
 		cg.addChariot(vueRail1.getRail().getNoeudSuivant(), 10, 80, tapis.getConnexionCircuit(),
 				null,
 				vueHall.getHall().getCircuit().calculChemin(vueRail1.getRail().getNoeudSuivant(), tapis.getConnexionCircuit()));
 		
 		vueHall.getHall().init();
 		
 		shell.open();
 		vueHall.updateView();
 		vueHall.draw();
 		
 		s.init();
 		s.play();
 		
 		while (!shell.isDisposed()) {
 			if (!display.readAndDispatch()) {
 				shell.layout();
 				display.sleep();
 			}
 		}
 	}
 	
 	@Test
 	public void test2() {
 		Display display = new Display();
 		Shell shell = new Shell(display);
 		shell.setText("VueTest");
 		shell.setLayout(new FillLayout());
 		shell.setSize(800, 800);
 		VueHall vueHall = new VueHall(shell, SWT.NONE);
 		vueHall.setSize(300, 300);
 		Simulation s = new Simulation(vueHall);
 		
 		CircuitGenerator cg = new CircuitGenerator(vueHall);
 
 		Point p1 = new Point(50, 50);
 		Point p2 = new Point(100, 50);
 		Point p3 = new Point(200, 100);
 		Point p4 = new Point(500, 500);
 		Point p5 = new Point(300, 500);
 		Point p6 = new Point(100, 600);
 		Point p7 = new Point(50, 600);
 		
 		cg.createSegment(p1, p2);
 		cg.createSegment(p2, p3);
 		cg.createSegment(p3, p4);
 		VueRail vueRail2 = cg.createSegment(p4, p5);
 		VueRail vueRail1 = cg.createSegment(p5, p6);
 		cg.createSegment(p6, p7);
 		cg.createSegment(p7, p1);
 		cg.createExit(p3);
 		
		VueTapisRoulant vueTapis = cg.createEntry(p5, 100, 10, 20, true);
		vueTapis.setAngle(0);
		TapisRoulant tapis = vueTapis.getTapisRoulant();
 		cg.addChariot(vueRail1.getRail().getNoeudSuivant(), 10, 80, tapis.getConnexionCircuit(),
 				null,
 				vueHall.getHall().getCircuit().calculChemin(vueRail1.getRail().getNoeudSuivant(), tapis.getConnexionCircuit()));
 		
 		cg.addChariot(vueRail2.getRail().getNoeudSuivant(), 50, 80, tapis.getConnexionCircuit(),
 				null,
 				vueHall.getHall().getCircuit().calculChemin(vueRail1.getRail().getNoeudSuivant(), tapis.getConnexionCircuit()));
 		
 		
 		vueHall.getHall().init();
 		
 		shell.open();
 		vueHall.updateView();
 		vueHall.draw();
 		
 		s.init();
 		s.play();
 		
 /*		ArrayList<ElementCircuit> list = vueHall.getHall().getCircuit().getElements();
 		Noeud noeud = null;
 		int i=0;
 		while(noeud == null) {
 			if(list.get(i) instanceof Noeud) {
 				noeud = (Noeud) list.get(i);
 			}
 			i++;
 		}
 		LinkedList<ElementCircuit> chemin = new LinkedList<ElementCircuit>();
 		chemin.add(noeud);
 		Chariot c = new Chariot(5, noeud, chemin);
 		noeud.registerChariot(c);
 	*/	
 		
 		while (!shell.isDisposed()) {
 			if (!display.readAndDispatch()) {
 				shell.layout();
 				display.sleep();
 			}
 		}
 	}
 
 }
