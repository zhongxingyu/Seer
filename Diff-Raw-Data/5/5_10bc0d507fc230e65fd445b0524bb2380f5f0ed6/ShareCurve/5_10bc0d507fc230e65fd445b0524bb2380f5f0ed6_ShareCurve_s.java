 package de.codeforum.wedabecha.system.draw;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.util.ArrayList;
 
 import javax.swing.JComponent;
 
 import de.codeforum.wedabecha.ui.MainWindow;
 
 /**
  * @author Micron (micron @ users.berlios.de)
  */
 public class ShareCurve extends JComponent {
 	final static long serialVersionUID = 1;
 	
 	private ArrayList werte;
 	private Color farbe;
 
 	private int abstand;
 	private double[] kurse;
 
 	// zur Berechnung der neuen Werte bei Grössenänderungen des Fensters
 	private double multiplikator;
 	// höchster Wert in der ArrayList werte
 	private double max;
 
 	private int hoch; // Tageshöchststand
 	private int tief; // Tagestiefststand
 	private int start; // Tageseingangswert
 	private int ende; // Tagesendwert
 
 	public int dateBeginIndex = 0;
 	public int dateEndIndex = 299;
 
 	private int breite = 700;
 
 	public ShareCurve(ArrayList werte, Color farbe){
 		this.werte = werte;
 		this.farbe = farbe;
 		this.setSize(MainWindow.getWindowWidth(),  MainWindow.getWindowHeight());
 		this.setVisible(false);
 	} // ShareCurve(ArrayList werte, Color farbe)
 
 
	protected void setGroesse(int breite, int hoehe){
 		this.breite = breite;
 		this.setSize(breite, hoehe);
 	}// setGroesse()
 
 
	protected void setVisibility( boolean sichtbar){
 		this.setVisible(sichtbar);
 	} // setVisibility()
 
 
 	/* diese Methode berechnet den höchsten Wert in der ArrayList, damit
 	 * Kurve immer die volle Fensterhöhe ausnutzt
 	 */
 
 	protected void getMax(){
 		double[] tempArray = new double[this.werte.size()];
 		for(int i = 0; i < this.werte.size(); i++){
 			tempArray = (double[])this.werte.get(i);
 			for(int j = 0; j < tempArray.length; j++){
 				this.max = Math.max(this.max, tempArray[j]);
 			} // for
  		} // for
 		this.multiplikator =	(MainWindow.layeredPane.getHeight() - 100) /
 								this.max;
 	} // getMax
 
 
 	public void paintComponent(Graphics kurve){
 		getMax();
 		int zaehler = 25;
 
 		kurve.setColor(farbe);
 
 		this.abstand = 2 * this.breite / 700;
 
 		for(int i = dateBeginIndex; i < dateEndIndex; i++){
 			this.kurse = (double[])this.werte.get(i);
 			this.start = (int)this.kurse[0];
 			this.ende = (int)this.kurse[1];
 			this.hoch = (int)this.kurse[2];
 			this.tief = (int)this.kurse[3];
 
 			kurve.drawLine(zaehler,
 				MainWindow.layeredPane.getHeight() - 25 -
 				(int)(this.multiplikator * this.start),
 				zaehler,
 				MainWindow.layeredPane.getHeight() - 25 -
 				(int)(this.multiplikator * this.hoch)
 			);
 			kurve.drawLine(zaehler,
 				MainWindow.layeredPane.getHeight() - 25 -
 				(int)(this.multiplikator * this.hoch),
 				zaehler,
 				MainWindow.layeredPane.getHeight() - 25 -
 				(int)(this.multiplikator * this.tief)
 			);
 			kurve.drawLine(zaehler,
 				MainWindow.layeredPane.getHeight() - 25 -
 				(int)(this.multiplikator * this.tief),
 				zaehler,
 				MainWindow.layeredPane.getHeight() - 25 -
 				(int)(this.multiplikator * this.ende)
 			);
 			kurve.drawLine(zaehler,
 				MainWindow.layeredPane.getHeight() - 25 -
 				(int)(this.multiplikator * this.ende),
 				zaehler + this.abstand,
 				MainWindow.layeredPane.getHeight() - 25 -
 				(int)(this.multiplikator * this.ende)
 			);
 			zaehler += this.abstand;
 		}//for()
 	}// paintComponent()
 } // public class ShareCurve
