 package test.blh.core.formulas.alcohol;
 
 import blh.core.formulas.alcohol.BYOSimple;
 import blh.core.formulas.alcohol.BrewersFriendSimple;
 import blh.core.formulas.alcohol.Daniels;
 import blh.core.units.alcohol.ABV;
 import blh.core.units.gravity.SpecificGravity;
 import junit.framework.Assert;
 import org.junit.Test;
 
 /**
  * Created by Erik Larkö at 6/28/13 1:26 PM
  */
 public class AlcoholFormulasTest {
 
     private SpecificGravity og = new SpecificGravity(1.040);
     private SpecificGravity fg = new SpecificGravity(1.010);
     private ABV expected = new ABV(3.9);
 
     @Test
     public void simpleBrewersFriendTest() {
         BrewersFriendSimple f = new BrewersFriendSimple();
         ABV actual = f.calc(og, fg);
 
         Assert.assertEquals(actual.value().value(), expected.value().value(), 0.1);
     }
 
     @Test
     public void BYOSimpleTest() {
         BYOSimple f = new BYOSimple();
         ABV actual = f.calc(og, fg);
 
        Assert.assertEquals(expected.value().value(), actual.value().value(), 0.03);
     }
 
     @Test
     public void danielsTest() {
         Daniels f = new Daniels();
         ABV actual = f.calc(og, fg);
 
        Assert.assertEquals(expected.value().value(), actual.value().value(), 0.5);
     }
 }
