 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Vehicles;
 
 import Helpers.Vector3f;
 import Main.Container;
 import java.text.ParseException;
 import java.util.Date;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author Gebruiker
  */
 public class TransportVehicleTest {
     
     public TransportVehicleTest() {
     }
     
     @BeforeClass
     public static void setUpClass() {
     }
     
     @AfterClass
     public static void tearDownClass() {
     }
     
     @Before
     public void setUp() {
     }
     
     @After
     public void tearDown() {
     }
 
     /**
      * Test of update method, of class TransportVehicle.
      */
     @Test
     public void testUpdate() throws Exception {
         System.out.println("update");
         float gameTime = 0.0F;
         TransportVehicle instance = null;
         instance.update(gameTime);
         // TODO review the generated test code and remove the default call to fail.
         fail("The test case is a prototype.");
     }
 
     /**
      * Test of MatchesContainer method, of class TransportVehicle.
      */
     @Test
    public void testMatchesContainer() throws ParseException, Exception {
         System.out.println("MatchesContainer");
         
         Date a = Container.df.parse("13 09 28 10:00");
         Date b = Container.df.parse("13 09 28 10:30");
         
         Container container = new Container("XD");
         try{ container.setArrival(a, b, Container.TransportType.trein, "T", new Helpers.Vector3f()); }
         catch(Exception ex) {}
         
         TransportVehicle instance = new TransportVehicle(a, b, null, Vehicle.VehicleType.truck, null, null);
         boolean expResult = false;
         boolean result = instance.MatchesContainer(container);
         assertEquals(expResult, result);
         // TODO review the generated test code and remove the default call to fail.
         fail("The test case is a prototype.");
     }
 }
