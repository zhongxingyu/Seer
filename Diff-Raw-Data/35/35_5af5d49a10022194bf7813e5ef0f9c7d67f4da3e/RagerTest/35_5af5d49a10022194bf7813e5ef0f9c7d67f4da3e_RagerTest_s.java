 package org.blh.core.formulas.bitterness;
 
 import org.blh.core.ingredients.Hop;
 import org.blh.core.recipe.HopAddition;
 import org.blh.core.units.Percentage;
 import org.blh.core.units.bitterness.IBU;
 import org.blh.core.units.gravity.SpecificGravity;
 import org.blh.core.units.time.Minutes;
 import org.blh.core.units.volume.Liters;
import org.blh.core.units.weight.Grams;
import java.util.Arrays;
import java.util.List;
 import org.blh.core.units.volume.USGallons;
 import org.blh.core.units.weight.Oz;
 import org.junit.Assert;
 import org.junit.Test;
 
 /**
  *
  * @author thinner
  */
 public class RagerTest {
    
     @Test
     public void testCalc() {
         HopAddition addition1 = new HopAddition(new Hop(null, new Percentage(5)), new Minutes(60), new Grams(50));
         HopAddition addition2 = new HopAddition(new Hop(null, new Percentage(7)), new Minutes(25), new Grams(85));
         List<HopAddition> hopAdditions = Arrays.asList(addition1, addition2);
        
         Liters boilVolume = new Liters(18);
         SpecificGravity boilGravity = new SpecificGravity(1.050);
        
         Rager f = new Rager();
         IBU actual = f.calc(hopAdditions, boilVolume, boilGravity);
         IBU expected = new IBU(87.423);
        
        Assert.assertEquals(expected.value(), actual.value());
     }
 
     @Test
     public void testGetIBUsForAddition() {
         Hop hop = new Hop(null, new Percentage(8));
         HopAddition addition = new HopAddition(hop, new Minutes(45), new Oz(1.5).toGrams());
         Liters boilVolume = new USGallons(5).toLiters();
         SpecificGravity boilGravity = new SpecificGravity(1.045);
        
         Rager f = new Rager();
         IBU actual = f.getIBUsFromAddition(addition, boilVolume, boilGravity);
         IBU expected = new IBU(48.355);
        
        Assert.assertEquals(expected.value(), actual.value());
     }
 }
