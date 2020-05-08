 package com.n11.rovers;
 
 
 import com.n11.rovers.exception.OutOfPlateauException;
 import com.n11.rovers.exception.PlateauInitializationException;
 import com.n11.rovers.listener.ChangeEvent;
 import com.n11.rovers.models.Coordinates;
 import com.n11.rovers.models.Plateau;
 
 import junit.framework.TestCase;
 
 public class PlateauTest extends TestCase
 {
 
     public PlateauTest( String testName )
     {
         super( testName );
     }
 
 
     public void testPlateau() throws PlateauInitializationException
     {
         Coordinates coords = new Coordinates(5, 5);
         Plateau plateau = new Plateau(coords);
 
        assertEquals(5, plateau.getSurface().length);
        assertEquals(5, plateau.getSurface().length);
         
         assertEquals(false, plateau.getSurface()[0][0]);
     }
 
     public void testChange() throws PlateauInitializationException, OutOfPlateauException {
     	ChangeEvent event = new ChangeEvent(null, new Coordinates(2, 2));
     	Plateau plateau = new Plateau(new Coordinates(5, 5));
     	
     	assertEquals(false, plateau.getSurface()[2][2]);
     	plateau.change(event);
     	assertEquals(true, plateau.getSurface()[2][2]);
     }
 }
