 package au.com.sequation.sensis.web;
 
 import org.junit.Test;
 
 import static org.hamcrest.CoreMatchers.equalTo;
 import static org.hamcrest.MatcherAssert.assertThat;
 
 /**
  * User: sbegg
  * Date: 28/04/13
  * Time: 11:58 AM
  */
 public class RegionTest
 {
     @Test
     public void toStringConvertsSuburbToRemoveHyphensAndChangeToTileCaseWithStateInUppercase()
     {
        Region region = new Region("south-fitz-kilda", "nsw", 13);
        assertThat(region.toString(), equalTo("South Fitz Kilda, NSW"));
     }
     @Test
     public void toStringConvertsSuburbWithoutHyphensAndChangeToTileCaseWithStateInUppercase()
     {
        Region region = new Region("hampton", "vic", 13);
        assertThat(region.toString(), equalTo("Hampton, VIC"));
     }
 }
