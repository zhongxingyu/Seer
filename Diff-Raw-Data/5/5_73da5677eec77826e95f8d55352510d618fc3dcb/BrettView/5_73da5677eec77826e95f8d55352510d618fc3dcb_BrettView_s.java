 package ui.spiel.brett;
 
 import java.awt.Dimension;
 import java.awt.Point;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Observable;
 import java.util.Observer;
 import java.util.Vector;
 
 import javax.swing.JPanel;
 
 import pd.brett.Feld;
 import pd.spieler.Figur;
 import pd.spieler.Spieler;
 import ui.GUIController;
 import ui.ressourcen.BrettLader;
 
 /**
  * JPanel, Graphische Darstellung des Spielbrettes.
  */
 public class BrettView extends JPanel implements Observer{
 	private Map<Feld, Feld2d> felder = new HashMap<Feld, Feld2d>();
 	private Map<Figur, Figur2d> figuren = new HashMap<Figur, Figur2d>();
 	private Vector<SpielerView> spielerViews;
 
 	public BrettView(GUIController controller) {
 		setLayout(null);
 		setPreferredSize(new Dimension(600, 600));
 		setMinimumSize(new Dimension(600, 600));
 
 		Map<Integer, Point> koordinaten = null;
 		try {
 			koordinaten = BrettLader.ladeXML("bin/ui/ressourcen/brett.xml");
 		} catch (Exception e) {
 			// Checked Exception in unchecked umwandeln
 			throw new RuntimeException(e);
 		}
 
 		FeldMouseAdapter mouseAdapter = new FeldMouseAdapter(this, controller);
 
 		for (Feld feld : controller.getSpiel().getBrett().getAlleFelder()) {
 			Feld2d feld2d;
 			if (feld.istBank()) {
 				feld2d = new BankFeld2d(koordinaten.get(feld.getNummer()),
 						feld, mouseAdapter);
 			} else {
 				feld2d = new NormalesFeld2d(koordinaten.get(feld.getNummer()),
 						feld, mouseAdapter);
 			}
 			felder.put(feld, feld2d);
 			this.add(feld2d);
 
 			if (feld.istBesetzt()) {
 				Figur figur = feld.getFigur();
 				Figur2d figur2d = new Figur2d(figur, this);
 				this.setComponentZOrder(figur2d, 0);
 				figuren.put(figur, figur2d);
 			}
 		}
 		
 		Vector<Point> spielerViewPos = new Vector<Point>();
 		spielerViewPos.add(new Point(460, 20));
 		spielerViewPos.add(new Point(60, 20));
 		spielerViewPos.add(new Point(60, 560));
 		spielerViewPos.add(new Point(460, 560));
 		
 		spielerViews = new Vector<SpielerView>();
 		
 		for (Spieler spieler : controller.getSpiel().getSpieler()) {
 			SpielerView sv = new SpielerView(controller, spieler.getName(), controller
 					.getSpieler(spieler).getFarbe(), spielerViewPos.get(spieler.getNummer()));
 			spielerViews.add(sv);
 			add(sv);
 		}
 		BrettMouseAdapter brettAdapter = new BrettMouseAdapter(this, controller);
 		add(new SpielBrett2d( brettAdapter ));
 	}
 
 	public Feld2d getFeld2d(Feld feld) {
 		return felder.get(feld);
 	}
 
 	public Figur2d getFigur2d(Figur figur) {
 		return figuren.get(figur);
 	}
 
 	public void update(Observable arg0, Object arg) {
 		// TODO Beim Controller noch einfügen
 		for(SpielerView sv : spielerViews){
 			if (sv == (SpielerView)arg){
 				sv.setFont(new java.awt.Font("Tahoma", 1, 11));
 			}
 		}
 		
 	}
 }
