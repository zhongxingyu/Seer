 import java.applet.*;
 import java.awt.*;
 import javax.swing.*;
 
 public class Ball{
 
 	public double pos_x; //Ballposition x-Achse
 	public double pos_y; //Ballposition y-Achse
 	public double x_speed; //horizontale Ballgeschwindigkeit
 	public double y_speed; //vertikale Ballgeschwindigkeit
 	public int ballradius;
 	public int klickflaeche = 200; //Begrenzung des Spielfeldes
 	public int kraftProKlick = 10;
 	public int linkerRand = 0;
 	public int rechterRand = 400;
 	public int obererRand = 0;
 	public double gravitation = 0.02;
 	public int count;
 	Color color;
 
 	public Ball( int pos_x, int pos_y, double x_speed, double y_speed, int ballradius, Color color, int count){
 
 		this.pos_x= pos_x;
 		this.pos_y= pos_y;
 		this.x_speed = x_speed;
 		this.y_speed = y_speed;
 		this.ballradius= ballradius;
 		this.color = color;
 		this.count = count; //bei Spielneustart reset auf 0
 
 
 	}
 	
 	//Ball getroffen?
 	public boolean getroffen(int maus_x, int maus_y){
 		// Bestimmen der Verbindungsvektoren
 		double x = maus_x - pos_x;
 		double y = maus_y - pos_y;
 
 		// Berechnen der Distanz (Skalarprodukt)
 		double distance = Math.sqrt ((x*x) + (y*y));
 
 		// Wenn Distanz kleiner/gleich Ballradius && Klick in der Klickflaeche erfolgt, gilt der Ball als getroffen
 		if ((distance <= ballradius) && (maus_y >= klickflaeche)){
 			count++; //Trefferzaehler
 			return true;
 		}
 		else{
 			return false;
 		}
 	}
 	
 	//Ballbeschleunigung bei erfolgtem Treffer
 	public void beschleunigen(int maus_x, int maus_y){
 		// Bestimmen der Abstaende
 		double x = maus_x - pos_x;
 		double y = maus_y - pos_y;
 		double x_positiv = x;
 		double y_positiv = y;
 		//Positive Werte bilden
 		if (x_positiv<0){
 			x_positiv = x_positiv*-1;
 		}
 		if (y_positiv<0){
 			y_positiv = y_positiv*-1;
 		}
 		//Anteile der Kraft auf beide Achsen bestimmen
 		double kraftAnteile = x_positiv+y_positiv;
 		double x_kraft = (kraftProKlick/kraftAnteile)*x_positiv;
 		double y_kraft = (kraftProKlick/kraftAnteile)*y_positiv;
 		//Kraefte den x und y Geschwindigkeiten des Balles anrechnen 
 		//Klick unten links
 		if(x<=0 && y>=0){
 			x_speed = x_speed + x_kraft;
 			y_speed = y_speed - y_kraft;
 		}
 		//Klick unten rechts
 		else if(x>=0 && y>=0){
 			x_speed = x_speed - x_kraft;
 			y_speed = y_speed - y_kraft;
 		}
 		//Klick oben rechts
 		else if(x>=0 && y<=0){
 			x_speed = x_speed - x_kraft;
 			y_speed = y_speed + y_kraft;
 		}
 		//Klick oben links
 		else{
 			x_speed = x_speed + x_kraft;
 			y_speed = y_speed + y_kraft;
 		}
 	}
 	
 	public void bewegen(){
 		//Abprallen des Balles an linken,rechten,oberen Rand (verliert dabei an Geschwindigkeit)
		if(pos_x<(linkerRand+ballradius)){
			pos_x = linkerRand+ballradius;
			x_speed = 0.7*x_speed*-1;
		}
		if(pos_x>(rechterRand-ballradius){
			pos_x = rechterRand-ballradius;
 			x_speed = 0.7*x_speed*-1;
 		}
 		if(pos_y<(obererRand+ballradius)){
			pos_y = obererRand+ballradius;
 			y_speed = 0.7*y_speed*-1;
 		}
 		//Fallgeschwindigkeit/Zeit erhöhen
 		y_speed = y_speed + gravitation;
 		//Neue Position bestimmen
 		pos_x = pos_x+x_speed;
 		pos_y = pos_y+y_speed;
 	}
 	
 	//hat der Ball den Boden berruehrt?
 	public boolean isOut(){
 		if (pos_y<600-ballradius){
 			return false;
 		}
 		else{
 			return true;
 		}
 	}
 	
 	//Ball zeichnen
 	public void paintBall(Graphics g){
 		g.setColor (color);
 		int x = (int)pos_x - (int)ballradius;
 		int y = (int)pos_y - (int)ballradius;
 		g.fillOval (x, y, 2 * ballradius, 2 * ballradius);
 
 	}
 	
 	//Anzahl der Balltreffer abfragen
 	public int getCount(){
 		return count;
 	}
 }
