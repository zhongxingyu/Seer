 import org.junit.Test;
 
import static org.junit.Assert.assertEquals;
 
 
 public class GoogleMapsTest extends TestCaseHelper {
 
     @Test
     public void shouldDisplayLondonInUKWhenSearched() {
         String actualTileForLondon = new GoogleMapsPage().searchFor("London, United Kingdom").getTileSource();
        String expectedTileForLondon = "https://mts1.google.com/vt/lyrs=m@218229115&hl=en&src=app&x=511&y=341&z=10&s=Ga";
        assertEquals(expectedTileForLondon, actualTileForLondon);
     }
 }
