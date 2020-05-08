 package example;
 
 import org.junit.Test;
 
 import static example.ImperativeStereotype.*;
 import static junit.framework.Assert.fail;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.is;
 
 public class StereotypesTest {
     @Test
    public void valueForNameShouldReturnValidPortalWhenPassedDescription() {
         assertThat(find("a person from new zealand"), is(newzealand));
     }
 
     @Test
    public void valueForNameShouldReturnValidPortalWhenPassedEnumValueItself() {
         assertThat(find("manchester"), is(manchester));
     }
 
     @Test
    public void valueForNameShouldReturnValidPortalWhenPassedAlternativeName() {
         assertThat(find("thief"), is(liverpool));
     }
 
     @Test
     public void valueForNameShouldBeCaseInsensitive() {
         assertThat(find("scally"), is(preston));
         assertThat(find("SCALLY"), is(preston));
         assertThat(find("ScAlLy"), is(preston));
     }
 
     @Test
     public void valueForNameShouldThrowErrorWhenPassedInvalidString() {
         try {
             find("Awesome");
             fail("Did not throw IllegalArgumentException");
         } catch (IllegalArgumentException e) {
             assertThat(e.getMessage(), is(
                     "Invalid Stereotype [Awesome]. Must be one of [a person from australia, a person from liverpool, " +
                             "a person from manchester, a person from new zealand, a person from preston, aussie, " +
                             "australia, fighter, kiwi, legend, liverpool, manc, manchester, mancunian, newzealand, " +
                             "preston, scally, scouser, thief]"));
         }
     }
 }
