 /**
  * 
  */
 package fr.iutvalence.java.projet.test;
 
 import fr.iutvalence.java.projet.spaceinvaders.BoundingBox;
 import fr.iutvalence.java.projet.spaceinvaders.Coordinates;
 import fr.iutvalence.java.projet.spaceinvaders.NegativeCoordinatesException;
 
 /**
  * @author alexandre
  *
  */
 public class TestBoundingBox
 {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args)
 	{
 		try
 		{
 			BoundingBox b1 = new BoundingBox(new Coordinates(0,0),
 												new Coordinates(10,10));
 			BoundingBox b2 = new BoundingBox(new Coordinates(5,5),
 												new Coordinates(5,5));
			System.out.println("Avant : " + b1 + "\n\t" + b2);
 			
 			b1.moveTo(new Coordinates(10,10));
 			b2.translate(new Coordinates(10,10));
 			
			//b2.reSize(new Coordinates(10,10));
 			
			System.out.println("Apres : " + b1 + "\n\t" + b2);
 			
 			System.out.println("Intersection : " + b1.intersection(b2));
 		}
 		catch (NegativeCoordinatesException e)
 		{
 			
 		}
 
 	}
 
 }
