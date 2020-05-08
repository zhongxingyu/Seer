 package wordforms;
 
import org.jetbrains.annotations.NotNull;
 import org.junit.Test;
 
 import java.io.*;
 import java.util.List;
 import java.util.Set;
 
 import static org.junit.Assert.*;
 
 
 /**
  * @author A.Sirenko
  * Date: 7/27/13
  */
 public class LoaderTest {
 
 	@Test
 	public void wordformLineShouldBeParsed() {
 		String line = " an";
 		List<String> wfs = Loader.extractWFs(line);
 		assertNotNull(wfs);
 		assertEquals(1, wfs.size());
 		assertEquals(line.trim(), wfs.get(0));
 
 		line = "     am, are -> [are], art -> [art], been, being -> [being], is, was, wast, were";
 		wfs = Loader.extractWFs(line);
 		assertNotNull(wfs);
 		assertEquals(9, wfs.size());
 		assertArrayEquals(
 				new String[]{"am", "are", "art", "been", "being", "is", "was", "wast", "were"},
 				wfs.toArray(new String[wfs.size()]));
 	}
 
 	@Test
 	public void testParsingOfLemmas() {
 		try {
			List<Lemma> lemms = Loader.load(
                    new FileInputStream(LoaderTest.class.getResource("/wordforms.raw").getPath())
            );
 			assertNotNull(lemms);
 			assertEquals(9, lemms.size());
 			assertLemmaContent(lemms.get(0), "the", "the");
 			assertLemmaContent(lemms.get(1), "a", "a", "an");
 			assertLemmaContent(lemms.get(2), "and", "and");
 			assertLemmaContent(
 					lemms.get(3), "be", "be", "am", "are", "art", "been", "being", "is", "was", "wast", "were");
 			assertLemmaContent(lemms.get(4), "in", "in", "ins");
 			assertLemmaContent(lemms.get(5), "of", "of");
 			assertLemmaContent(lemms.get(6), "to", "to");
 			assertLemmaContent(lemms.get(7), "for", "for");
 			assertLemmaContent(
 					lemms.get(8),
 					"art", "art", "artful", "artfully", "artfulness", "artless", "artlessly", "artlessness", "arts");
 		} catch (IOException e) {
 			fail(e.toString());
 		}
 	}
 
 	@Test
 	public void testLoadingOfDefaultSet() {
 		try {
 			Set<Lemma> lemmas = Loader.loadDefaultSet();
 		    assertEquals(32638, lemmas.size());
 		} catch (Exception e) {
 			fail();
 		}
 	}
 
 	private void assertLemmaContent(@NotNull Lemma lemma, @NotNull String name, @NotNull String ... wordforms) {
 		assertEquals(name, lemma.getName());
 		Set<WF> wfs = lemma.getWordforms();
 		assertEquals(wordforms.length, wfs.size());
 		for (String wf: wordforms) {
 			if (!wfs.contains(new WF(wf))) {
 				fail("Wordform " + wf + " wasn't find in lemma");
 			}
 		}
 	}
 }
