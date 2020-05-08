 import javax.swing.*;
 import java.awt.*;
 
 public class Traceur extends JPanel{
 	
 	private int epaisseur;
 	private int type; //0: point, 1: ligne, 2:rectangle , 3:cercle
 	private Color couleur;
 	private boolean isDown; 
 	private int x_origine;
 	private int y_origine;
 	private int x_arrivee;
 	private int y_arrivee;
 	private int x3;
 	private int y3;
 	private int hauteur;
 	private int largeur;
 	private boolean estRempli; //true : le rectanlge ou cercle sera remplie
 	private int forme; //0: rond il s'agit du curseur
 	
 	
 	/* constructeur */
 	public Traceur(int type, int epaisseur, Color couleur, int x_origine, int y_origine, int x_arrivee, int y_arrivee, int forme){
 		this.epaisseur=epaisseur;
 		this.couleur=couleur;
 		this.x_origine=x_origine;
 		this.y_origine=y_origine;
 		this.x_arrivee=x_arrivee;
 		this.y_arrivee=y_arrivee;
 		this.type=type;
 		this.forme=forme;
 			
 	}
 	
 	/*accesseurs*/
 	public int getEpaisseur(){ return this.epaisseur; }
 	public int getType(){ return this.type; }
 	public Color getColor(){ return this.couleur; }
 	public boolean getIsDown(){ return this.isDown; }
 	public int getXOrigine(){ return this.x_origine; }
 	public int getYOrigine(){ return this.y_origine; }
 	public int getXArrivee(){ return this.x_arrivee; }
 	public int getYArrivee(){ return this.y_arrivee; }
 	public int getX3(){ return this.x3; }
 	public int getY3(){ return this.y3; }
 	public int getHauteur(){ return this.hauteur; }
 	public int getLargeur(){ return this.largeur; }
	public boolean estRempli(){ return this.estRempli();}
 	public int getForme(){ return this.forme; }
 	
 	
 	/*modifieurs*/
 	public void setEpaisseur(int epaisseur){
 		this.epaisseur=epaisseur;
 		}
 	
 	public void setType(int type){
 		this.type=type;
 	}
 	
 	public void setColor(Color couleur){
 		this.couleur=couleur;
 	}
 	
 	public void setIsDown(boolean isDown){
 		this.isDown=isDown;
 	}
 	
 	public void setXOrigine(int x_origine){
 		this.x_origine=x_origine;
 	}
 	
 	public void setYOrigine(int y_origine){
 		this.y_origine=y_origine;
 	}
 	
 	public void setXArrivee(int x_arrivee){
 		this.x_arrivee=x_arrivee;
 	}
 	
 	public void setYArrivee(int y_arrivee){
 		this.y_arrivee=y_arrivee;
 	}
 	public void setX3(int x){
 		this.x3=x;
 	}
 	public void setY3(int y){
		this.x3=y;
 	}
 	public void estRempli(boolean estRempli){
 		this.estRempli=estRempli;
 	}
 	public void setForme(int forme){
 		this.forme=forme;
 	}
 }
