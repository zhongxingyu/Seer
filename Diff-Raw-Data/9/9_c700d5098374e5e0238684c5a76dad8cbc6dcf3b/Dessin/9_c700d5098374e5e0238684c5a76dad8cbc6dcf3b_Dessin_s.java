 import java.awt.Color;
 import java.awt.Graphics;

 
 public class Dessin extends JPanel {
 	Curseur curs1 = new Curseur(20, 20, 30, Color.red);
 	
     public void paintComponent (Graphics g) {
     	// x1 et y1 represente le coin en haut a gauche du curseur
     	double x1 = curs1.getX(); 
     	double y1 = curs1.getY();
     	int taille = curs1.getTaille();
     	double rayon = taille/2;
     	double orientationDegree = curs1.getOrientation();
     	double orientationRadian = curs1.convertToRadian(orientationDegree);
     	// centreX et centreY sont les coordonnees du centre du curseur
     	double centreX = x1+(taille/2);
     	double centreY = y1+(taille/2);
     	// x2 et y2 represente le bout de la fleche d'orientation
     	double x2 = curs1.calculCos(orientationRadian)*(rayon);
     	double y2 = curs1.calculSin(orientationRadian)*(rayon);
     	
     	// ces methodes ne fonctionne pas avec des doubles ....
     	// g.setColor(Color.red);
     	// g.fillOval(x1, y1, taille, taille);
     	// g.drawLine(centreX, centreY, x2, y2); 
     }
 }
