 import org.junit.Test;
 
import static org.junit.Assert.assertTrue;
 
 
 public class GoogleMapsTest extends TestCaseHelper {
 
     @Test
     public void shouldDisplayLondonInUKWhenSearched() {
         String actualTileForLondon = new GoogleMapsPage().searchFor("London, United Kingdom").getTileSource();
        String expectedTileForLondon = "https://mts0.google.com/vt/lyrs=m@219261208&hl=en&src=app&x=512&y=340&z=10";
        assertTrue(actualTileForLondon.contains(expectedTileForLondon));
     }
 }
