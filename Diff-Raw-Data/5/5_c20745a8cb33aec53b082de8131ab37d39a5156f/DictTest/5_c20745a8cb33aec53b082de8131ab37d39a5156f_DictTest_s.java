 package com.zylman.wwf.server;
 
 import static org.junit.Assert.assertEquals;
 
 import java.util.concurrent.ConcurrentSkipListSet;
 
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import com.zylman.wwf.shared.SolveResult;
 
 public class DictTest {
 	private static Dict dict;
 	private static ConcurrentSkipListSet<SolveResult> results;
 	private static ConcurrentSkipListSet<SolveResult> expected;
 
 	@BeforeClass public static void initDict() {
 		dict = new Dict("war/dict.txt");
 		results = new ConcurrentSkipListSet<SolveResult>();
 		expected = new ConcurrentSkipListSet<SolveResult>();
 	}
 
 	@Before public void clearResults() {
 		results.clear();
 		expected.clear();
 	}
 
 	@Test public void solve_rack_noWildcards() throws Exception {
 		createExpected("ifs", "fas", "if", "fa", "ais", "si", "is", "as", "ai");
 		dict.solve("fias", "", "", "", results);
 		assertEqual();
 	}
 
 	@Test public void solve_rack_start_noWildcards() throws Exception {
 		createExpected("agha", "aha", "aga", "aah", "aid", "ah", "ag", "ad", "ai", "aa");
 		dict.solve("fgadhi", "a", "", "", results);
 		assertEqual();
 	}
 
 	@Test public void solve_rack_contains_noWildcards() throws Exception {
 		createExpected("haji", "jig", "jag", "haj");
 		dict.solve("fgahi", "", "j", "", results);
 		assertEqual();
 	}
 
 	@Test public void solve_rack_end_noWildcards() throws Exception {
 		createExpected("agha", "saiga", "saga", "sha", "aha", "aga", "fa", "ha", "aa");
 		dict.solve("fgadhsij", "", "", "a", results);
 		assertEqual();
 	}
 
 	@Test public void solve_rack() throws Exception {
 		dict.solve("as*", "", "", "", results);
 		assertSize(72);
 	}
 
 	@Test public void solve_rack_start() throws Exception {
 		dict.solve("fgadhi*", "a", "", "", results);
 		assertSize(86);
 	}
 
 	@Test public void solve_rack_contains() throws Exception {
 		dict.solve("fgahi*", "", "j", "", results);
 		assertSize(30);
 	}
 
 	@Test public void solve_rack_end() throws Exception {
 		dict.solve("fgadhsij*", "", "", "a", results);
 		assertSize(102);
 	}
 
 	@Test public void solve_rack_start_end() throws Exception {
 		dict.solve("fgadhsij**", "r", "", "t", results);
 		assertSize(81);
 	}
 
 	@Test public void solve_rack_start_contains_end() throws Exception {
 		dict.solve("fgadhsij**", "r", "s", "t", results);
 		assertSize(44);
 	}
 
 	private void createExpected(String... expectedResults) {
 		for (String expectedResult : expectedResults) {
 			expected.add(new SolveResult(expectedResult, Dict.score(expectedResult)));
 		}
 	}
 
 	@SuppressWarnings("unused")
 	private void printResults() {
 		for (SolveResult result : results) {
 			System.out.println(result.getWord());
 		}
 	}
 
 	@SuppressWarnings("unused")
 	private void printSize() {
 		System.out.println(results.size());
 	}
 
 	private void assertEqual() {
		assertEquals(results,expected);
 	}
 
 	private void assertSize(int size) {
		assertEquals(results.size(),size);
 	}
 }
