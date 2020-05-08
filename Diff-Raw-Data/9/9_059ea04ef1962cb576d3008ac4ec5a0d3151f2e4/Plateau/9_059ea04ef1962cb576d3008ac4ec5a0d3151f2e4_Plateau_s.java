 package com.soat.rover.model;
 
 public class Plateau implements IPlateau
 {
 	// fields -----------------
 	private int width; 
 	private int heigth;
 	
 	/**
 	 * Constructor
 	 * 
 	 * @param width <strong>int</strong> width of plateau
 	 * @param heigth <strong>int</strong> height of the plateau
 	 */
 	public Plateau(int width, int heigth)
 	{
 		this.width  = width;
 		this.heigth = heigth;
 	}
 	
 	/**
 	 * Return the width of plateau
 	 * 
 	 * @return <strong>int</strong> width of plateau
 	 */
 	public int getWidth()
 	{
 		return width;
 	}
 	
 	/**
 	 * Sets the width of plateau
 	 * 
 	 * @param width <strong>int</strong> width of plateau
 	 */
 	public void setWidth(int width)
 	{
 		this.width = width;
 	}
 	
 	/**
 	 * Return the height of the plateau
 	 * 
 	 * @return <strong>int</strong> height of the plateau
 	 */
 	public int getHeigth() 
 	{
 		return heigth;
 	}
 	
 	/**
 	 * Sets the height of the plateau
 	 * 
 	 * @param heigth <strong>int</strong> height of the plateau
 	 */
 	public void setHeigth(int heigth) 
 	{
 		this.heigth = heigth;
 	}
 	
 	/**
 	 * Verifie la position d'un objet sur le plateau
 	 * 
 	 * @param point <strong>Point</strong> point representant la position de l'objet
	 * @return <strong>boolean</strong> true si l'objet est positionne dans le plateau,
 	 * 	false autrement
 	 */
 	public boolean check(Point point)
 	{
 		return (point.getX() >= 0) && (point.getX() <= this.width) &&
                (point.getY() >= 0) && (point.getY() <= this.heigth);
 	}
 
 	@Override
 	public String toString() {
 		return "Plateau [width=" + width + ", heigth=" + heigth + "]";
 	}
 }
