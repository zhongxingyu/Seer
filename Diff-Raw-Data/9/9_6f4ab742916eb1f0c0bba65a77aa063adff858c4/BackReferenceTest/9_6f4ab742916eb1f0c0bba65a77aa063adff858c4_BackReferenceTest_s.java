 package dfh.grammar;
 
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.io.IOException;
 
 import org.junit.Test;
 
 /**
  * Make sure backreferences work as advertised.'
  * <p>
  * <b>Creation date:</b> Mar 29, 2011
  * 
  * @author David Houghton
  * 
  */
 public class BackReferenceTest {
 
 	@Test
 	public void simpleTest() throws GrammarException, IOException {
 		String[] rules = {
 		//
 		"<ROOT> = [ '-' | '_' ] 'foo' 1",//
 		};
 		Grammar g = new Grammar(rules);
 		String s = "-foo- _foo_ -foo_ _foo-";
 		Matcher m = g.find(s);
 		int count = 0;
 		while (m.match() != null)
 			count++;
 		assertTrue("found all foo", count == 2);
 	}
 
 	@Test
 	public void goodTest() throws GrammarException, IOException {
 		String[] rules = {
 				//
 				"<ROOT> = <q> <text> 1",//
 				"<q> = /[\"']/",//
 				"<text> = /\\w++/",//
 		};
 		Grammar g = new Grammar(rules);
 		String s = "'ned'";
 		Matcher m = g.find(s);
 		Match n = m.match();
 		assertNotNull("found ned", n);
 	}
 
 	@Test
 	public void badTest() throws GrammarException, IOException {
 		String[] rules = {
 				//
 				"<ROOT> = <q> <text> 1",//
 				"<q> = /[\"']/",//
 				"<text> = /\\w++/",//
 		};
 		Grammar g = new Grammar(rules);
 		String s = "'ned\"";
 		Matcher m = g.find(s);
 		Match n = m.match();
 		assertNull("didn't find ned", n);
 	}
 
 	@Test
 	public void tooSmallTest() throws GrammarException, IOException {
 		String[] rules = {
 		//
 		"<ROOT> = /[ab]/ 'foo' 0",//
 		};
 		try {
 			new Grammar(rules);
 			fail("should have throw exception");
 		} catch (GrammarException e) {
 			assertTrue(e.getMessage().equals(
 					"back references must be greater than 0"));
 		}
 	}
 
 	@Test
 	public void tooBigTest() throws GrammarException, IOException {
 		String[] rules = {
 		//
 		"<ROOT> = /[ab]/ 'foo' 3",//
 		};
 		try {
 			new Grammar(rules);
 			fail("should have throw exception");
 		} catch (GrammarException e) {
 			assertTrue(e.getMessage().equals("back reference 3 is too big"));
 		}
 	}
 
 	@Test
 	public void groupTest() throws GrammarException, IOException {
 		String[] rules = {
 		//
 		"<ROOT> = [ /[ab]/ 'foo' 1 ]{2,}",//
 		};
 		Grammar g = new Grammar(rules);
 		int count = 0;
 		Matcher m = g.find("afooabfoob afooa foobfoob");
 		while (m.match() != null)
 			count++;
 		assertTrue("back reference in group worked", count == 1);
 	}
 
 	@Test
 	public void groupTooSmallTest() throws GrammarException, IOException {
 		String[] rules = {
 		//
 		"<ROOT> = [ /[ab]/ 'foo' 0 ]{2,}",//
 		};
 		try {
 			new Grammar(rules);
 			fail("should have throw exception");
 		} catch (GrammarException e) {
 		}
 	}
 
 	@Test
 	public void groupTooBigTest() throws GrammarException, IOException {
 		String[] rules = {
 		//
 		"<ROOT> = [ /[ab]/ 'foo' 4 ]{2,}",//
 		};
 		try {
 			new Grammar(rules);
 			fail("should have throw exception");
 		} catch (GrammarException e) {
 		}
 	}
 
 	@Test
 	public void repetitionTest() throws GrammarException, IOException {
 		String[] rules = {
 		//
 		"<ROOT> = /[ab]/ 'foo' 1{2,}",//
 		};
 		try {
 			new Grammar(rules);
 			fail("should have throw exception");
 		} catch (GrammarException e) {
 			assertTrue(e
 					.getMessage()
					.equals("simple back reference cannot be modified with repetition suffix; use uplevel backreference; e.g., 1^"));
 		}
 	}
 
 }
