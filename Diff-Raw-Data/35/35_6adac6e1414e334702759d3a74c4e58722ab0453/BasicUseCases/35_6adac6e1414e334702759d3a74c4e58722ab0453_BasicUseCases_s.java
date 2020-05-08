 package org.linagora.clients.minefi.dpma.terminologie;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 
 import org.junit.Test;
 
 import com.sun.star.lang.IllegalArgumentException;
 import com.sun.star.lang.Locale;
 import com.sun.star.linguistic2.XMeaning;
 
 public class BasicUseCases extends AbstractAnglicismeThesaurusTest {
 
 
 	@Test
 	public void testGetLocales() {
 
 		Locale[] ls = super.getLocales();
 		assertTrue(ls != null && ls.length > 0);
 	}
 	
 	@Test
 	public void invalideArguments() {
 		try {
 			XMeaning[] results = getSynonyme(null);
 			assertNotNull(results);
 			assertTrue(results.length == 0);
 			results = getSynonyme("");
 			assertNotNull(results);
 			assertTrue(results.length == 0);
 		} catch (IllegalArgumentException e) {
 			fail();
 		} catch (RuntimeException e) {
 			fail();
 		}
 	}
 	
 	@Test
 	public void testGetSynonymeSimple() throws IllegalArgumentException, RuntimeException {
 		XMeaning[] res = getSynonyme("spam");
 		// No entry in dictionnary on 'spam'
 		assertTrue(res != null && res.length == 0);
 	}
 
 	@Test
	public void testPackage() throws IllegalArgumentException, RuntimeException {
 		// 
 		XMeaning[]  www = getSynonyme("web");
		
		
 		XMeaning[]  paquet = getSynonyme("package");
		
		// No entry in dictionnary on 'spam'
//		assertTrue(res != null && res.length == 8);
 	}
 	
 	@Test
 	public void testGetSynonymeWithSpace() throws IllegalArgumentException, RuntimeException {
 		XMeaning[] res = getSynonyme("wild card");
		assertTrue(res != null && res.length == 1 && res[0].querySynonyms().length == 2);
 		// Should do the same
 		res = getSynonyme("wild-card");
		assertTrue(res != null && res.length == 1 && res[0].querySynonyms().length == 2);
 		// An other test		
 		res = getSynonyme("data-base");
		assertTrue(res != null && res.length == 2 && res[0].querySynonyms().length == 1);
 		res = getSynonyme("data base");
		assertTrue(res != null && res.length == 1 && res[0].querySynonyms().length == 1);

 	}
 	
 	@Test
 	public void testGetSynonymeWithHyphen() throws IllegalArgumentException, RuntimeException {
 		XMeaning[] results = getSynonyme("hit-and-run");
 		assertTrue(results != null && results.length > 0);
 	}
 	
 	@Test
 	public void testGetSynonymeWithSeveralOptions() throws IllegalArgumentException, RuntimeException {
 		XMeaning[] results = getSynonyme("e-mail");
 		assertTrue(results != null && results.length >= 2);
 	}
 
 	@Test
 	public void testUpperCaseWord() throws IllegalArgumentException, RuntimeException {
 		XMeaning[] results = getSynonyme("gds");
 		assertTrue(results != null && results.length >= 1);
 		results = getSynonyme("atm");
 		assertTrue(results != null && results.length >= 1);
 	}
 	
 	@Test 
 	public void testExclusionList() throws IllegalArgumentException, IOException, RuntimeException {
         BufferedReader in = new BufferedReader(new FileReader("resources/liste.exclusion.txt"));
         String str;
         boolean fail = false;
         while ((str = in.readLine()) != null) {
         	if ( ! str.startsWith("==")) {
         		XMeaning[] results = getSynonyme(str);
         		//assertTrue(results != null && results.length == 0);				        
         		if (results != null && results.length > 0 ) {
         			System.out.println(str + " is not missing !");
         			fail = true;
         		}
         		else
         			System.out.println(str + " is missing : OK");
         	}
         }
 	    in.close();
         assertFalse(fail);
 	}
 }
