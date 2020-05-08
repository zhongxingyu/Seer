 package de.proglabor.aufgabe2;
 
 /**
  * @author SirMonkey
  *
  */
 public class Pflanze implements Comparable<Pflanze> {
 
 	private int x;
 	private int y;
 
 	/**
 	 * Konstruktor
 	 * @param x Koordinate	
 	 * @param y Koordinate
 	 */
 	public Pflanze(int x, int y) {
 		this.x = x;
 		this.y = y;
 	}
 
 	/**
 	 * @return die x-Koordinate
 	 */
 	public int getX() {
 		return x;
 	}
 
 	/**
 	 * setzt x-Koordinate
 	 * @param x 
 	 */
 	public void setX(int x) {
 		this.x = x;
 	}
 
 	/**
 	 * @return y-Koordinate
 	 */
 	public int getY() {
 		return y;
 	}
 
 	/**
 	 * setzt y-Koordinate
 	 * @param y 
 	 */
 	public void setY(int y) {
 		this.y = y;
 	}
 
 	/* (non-Javadoc)
 	 * @see java.lang.Comparable#compareTo(java.lang.Object)
 	 */
 	@Override
 	public int compareTo(Pflanze anderePflanze) {
		if (this.x == anderePflanze.x && this.y == anderePflanze.y) {
 			return 0;
		} else if (this.x < anderePflanze.x ) {
 			return -1;
 		} else {
 			return 1;
 		}
 	}
 	
 }
