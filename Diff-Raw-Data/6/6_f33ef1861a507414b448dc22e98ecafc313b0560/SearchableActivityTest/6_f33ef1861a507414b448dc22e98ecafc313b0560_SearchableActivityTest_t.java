 package nl.napauleon.sabber.search;
 
import android.R;
 import nl.napauleon.sabber.CustomTestRunner;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 import static org.junit.Assert.assertNotNull;
 
 @RunWith(CustomTestRunner.class)
 public class SearchableActivityTest {
 
     SearchableActivity activity;
 
     @Before
     public void setUp() throws Exception {
         activity = new SearchableActivity();
     }
 
     @Test
     public void testOnCreate() throws Exception {
         activity.onCreate(null);
        assertNotNull(activity.findViewById(R.id.list));
     }
 }
