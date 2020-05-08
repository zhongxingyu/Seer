 package nu.danielsundberg.persistence.haddock;
 
import nu.danielsundberg.persistence.AbstractPersistenceTest;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.runners.MockitoJUnitRunner;
 
 import static org.hamcrest.Matchers.*;
 import static org.junit.Assert.assertThat;
 
 @RunWith(MockitoJUnitRunner.class)
public class HaddockConstantsTest extends AbstractPersistenceTest {
 
     @Test
     public void testVersion() throws Exception {
          assertThat(HaddockConstants.getPublicVersion(), is(not(nullValue())));
     }
 
     @Test
     public void testPackage() throws Exception {
         assertThat(HaddockConstants.getHaddockPersistence(), is(not(nullValue())));
     }
 
 }
