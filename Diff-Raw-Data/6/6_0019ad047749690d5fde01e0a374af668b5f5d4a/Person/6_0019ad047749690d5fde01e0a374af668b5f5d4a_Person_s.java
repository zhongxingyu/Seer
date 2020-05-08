 package edu.agh.tunev.model.cellular.agent;
 
 import java.awt.geom.Point2D;
 
 import edu.agh.tunev.model.AbstractPerson;
 
 public final class Person extends AbstractPerson {
 	
 	public enum Orientation{
 		E, NE, N, NW, W, SW, S, SE	
 	}
 
 	public Person(Point2D.Double position) {
 		super(position);
 	}
 	
 	public static Double orientToAngle(Orientation orient) throws WrongOrientationException{	
 		//starting at east
 		Double angle = 0.0;
 		Person.Orientation[] orientValues = Person.Orientation.values();
 		
 		//TODO: more sensible error handling
 		for(int i = 0; i < orientValues.length && orient != orientValues[i]; ++i){
 			angle += 45.0;
 		}
 		
 		if(angle > 315)
 			throw new WrongOrientationException();
 		else 
 			return angle;
 	}
 
 }
