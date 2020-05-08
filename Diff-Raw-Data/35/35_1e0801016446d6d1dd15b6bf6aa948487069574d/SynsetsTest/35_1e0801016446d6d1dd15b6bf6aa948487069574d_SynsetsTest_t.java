 package synsets;
 
 
 import org.junit.Test;
 
 import java.io.IOException;
 import java.nio.file.Paths;
 
 import static junit.framework.Assert.*;
 import static org.junit.Assert.assertArrayEquals;
 
 /**
  * @author A.Sirenko
  * Date: 8/29/13
  */
 public class SynsetsTest {
 
 	@Test
 	public void wordNetLoadingTest() {
 		try {
			Synsets syns = Loader.read(Paths.get(SynsetsTest.class.getResource("/princeton_wp_synsets").getPath()));
 			assertNotNull(syns);
 			assertEquals(94857, syns.getCountOfLemms());
 
 			String[] arr = syns.getSyms("Not_Existed");
 			assertNotNull(arr);
 			assertTrue(arr.length == 0);
 
 			arr = syns.getSyms("emergent");
 			assertNotNull(arr);
 			assertArrayEquals(arr, new String[] {"emerging", "emergent"});
 		} catch (IOException e) {
 			fail();
 		}
 	}
 }
