 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package test.blh.core.units.weight;
 
 import blh.core.units.weight.Lbs;
 import blh.core.units.weight.Kilograms;
 import junit.framework.Assert;
 import org.junit.Test;
 
 /**
  *
  * @author thinner
  */
 public class LbsTest {
     
     @Test
     public void testDouble() {
         Lbs actual = new Lbs(2);
         double expected = 2;
         
         Assert.assertEquals(expected, actual.value());
     }
     
     @Test
    public void testKiloLbs() {
         Lbs actual = new Lbs(new Kilograms(2));
         Lbs expected = new Lbs(4.40924524);
         
         Assert.assertEquals(expected.value(), actual.value(), 0.00001);
     }
 
     @Test
    public void testToKiloLbs() {
         Kilograms actual = new Lbs(3).toKilograms();
         Kilograms expected = new Kilograms(1.36077711);
         
         Assert.assertEquals(expected.value(), actual.value(), 0.00001);
     }
 }
