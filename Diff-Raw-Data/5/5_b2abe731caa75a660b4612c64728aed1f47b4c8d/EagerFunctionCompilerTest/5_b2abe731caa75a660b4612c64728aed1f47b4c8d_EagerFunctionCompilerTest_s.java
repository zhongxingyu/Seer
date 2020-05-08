 package org.instructionexecutor;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 
 import org.junit.Test;
 import org.suite.SuiteUtil;
 import org.suite.node.Atom;
 import org.suite.node.Int;
 import org.suite.node.Node;
 import org.suite.node.Str;
 import org.suite.node.Tree;
 
 public class EagerFunctionCompilerTest {
 
 	@Test
 	public void testApply() {
 		assertEquals(Int.create(2), eval("" //
 				+ "apply {1} {(a => 2),}"));
 		assertEquals(Int.create(2), eval("" //
 				+ "apply {4} {(a => a + 1), (b => b * 2), (c => c / 5),}"));
 	}
 
 	@Test
 	public void testClosure() {
 		assertEquals(Int.create(7), eval("" //
 				+ "define add = (p => q => p + q) >> add {3} {4}"));
 		assertEquals(Int.create(20), eval("" //
 				+ "define p = (n => n + 1) >> \n" //
 				+ "define q = (n => p {n} * 2) >> \n" //
 				+ "q {9}"));
 	}
 
 	@Test
 	public void testConcat() {
 		assertEquals(SuiteUtil.parse("1, 2, 3, 4, 5, 6, 7, 8,"), eval("" //
 				+ "concat {1, 2, 3, 4,} {5, 6, 7, 8,}"));
 		assertEquals(SuiteUtil.parse("1, 2, 3, 4, 5, 6,"), eval("" //
 				+ "concat-list {(1, 2,), (3, 4,), (5, 6,),}"));
 	}
 
 	@Test
 	public void testContains() {
 		assertEquals(Atom.create("true"), eval("" //
 				+ "contains {9} {7, 8, 9, 10, 11,}"));
 		assertEquals(Atom.create("false"), eval("" //
 				+ "contains {12} {7, 8, 9, 10, 11,}"));
 	}
 
 	@Test
 	public void testCross() {
 		assertEquals(SuiteUtil.parse("" //
 				+ "((7, 1,), (7, 2,),), " //
 				+ "((8, 1,), (8, 2,),), " //
 				+ "((9, 1,), (9, 2,),),") //
 				, eval("cross {a => b => a, b,} {7, 8, 9,} {1, 2,}"));
 
 		assertEquals(
 				SuiteUtil.parse("" //
 						+ "((A, 1,), (A, 2,),), " //
 						+ "((B, 1,), (B, 2,),), " //
 						+ "((C, 1,), (C, 2,),),") //
 				,
 				eval("define list1 as list of one of (A, B, C,) = (A, B, C,) >> \n" //
 						+ "cross {a => b => a, b,} {list1} {1, 2,}"));
 	}
 
 	@Test
 	public void testFibonacci() {
 		assertEquals(Int.create(89), eval("" //
 				+ "define fib = (n => \n" //
 				+ "    if (n > 1) then ( \n" //
 				+ "        fib {n - 1} + fib {n - 2} \n" //
 				+ "    ) \n" //
 				+ "    else 1 \n" //
 				+ ") >> \n" //
 				+ "fib {10}"));
 
 		assertEquals(Int.create(89), eval("" // Pretends co-recursion
 				+ "define fib = (i1 => i2 => dummy => \n" //
 				+ "    i2/(fib {i2} {i1 + i2}) \n" //
 				+ ") >> \n" //
 				+ "define h = (f => head {f {}}) >> \n" //
 				+ "define t = (f => tail {f {}}) >> \n" //
 				+ "apply {fib {0} {1}} {t, t, t, t, t, t, t, t, t, t, h,}"));
 	}
 
 	@Test
 	public void testFilter() {
 		assertEquals(SuiteUtil.parse("4, 6,"), eval("" //
 				+ "filter {n => n % 2 = 0} {3, 4, 5, 6,}"));
 	}
 
 	@Test
 	public void testFold() {
 		assertEquals(Int.create(324), eval("" //
 				+ "fold {a => b => a * b} {2, 3, 6, 9,}"));
 		assertEquals(Int.create(79), eval("" //
 				+ "fold-left {a => b => a - b} {100} {6, 7, 8,}"));
 		assertEquals(Int.create(-93), eval("" //
 				+ "fold-right {a => b => a - b} {100} {6, 7, 8,}"));
 	}
 
 	@Test
 	public void testIf() {
 		assertEquals(Int.create(0), eval("3 > 4 ? 1 | 0"));
 		assertEquals(Int.create(1), eval("3 = 3 ? 1 | 0"));
 	}
 
 	@Test
 	public void testJoin() {
 		assertEquals(Int.create(19), eval("" //
 				+ "define p = (n => n * 2) >> \n" //
 				+ "define q = (n => n + 1) >> \n" //
 				+ "define r = (join {p} {q}) >> \n" //
 				+ "r {9}"));
 	}
 
 	@Test
 	public void testLog() {
 		assertEquals(Int.create(1), eval("" //
 				+ "if (1 = 1) then 1 else (1 / 0)"));
 		assertEquals(Int.create(1), eval("" //
				+ "if (1 = 1) then 1 else log {\"shouldn't appear\"} {1 / 0}"));
 		assertEquals(Int.create(1), eval("" //
				+ "if (1 = 0) then 1 else log {\"should appear\"} {1 / 0}"));
 	}
 
 	@Test
 	public void testMap() {
 		assertEquals(SuiteUtil.parse("5, 6, 7,"), eval("" //
 				+ "map {n => n + 2} {3, 4, 5,}"));
 	}
 
 	@Test
 	public void testOperator() {
 		assertEquals(Atom.create("true"), eval("" //
 				+ "and {1 = 1} {or {1 = 0} {1 = 1}}"));
 	}
 
 	@Test
 	public void testRange() {
 		assertEquals(SuiteUtil.parse("2, 5, 8, 11,"), eval("" //
 				+ "define range = (i => j => inc => \n" //
 				+ "    if (i != j) then ( \n" //
 				+ "        i, range {i + inc} {j} {inc} \n" //
 				+ "    ) \n" //
 				+ "    else () \n" //
 				+ ") >> \n" //
 				+ "range {2} {14} {3}"));
 	}
 
 	@Test
 	public void testSplit() {
 		assertEquals(Int.create(1), eval("" //
 				+ "split {h => l => h} {1, 2,}"));
 	}
 
 	@Test
 	public void testSwitch() {
 		assertEquals(new Str("C"), eval("" //
 				+ "define switch = (p => \n" //
 				+ "    p = 1 ? \"A\" | \n" //
 				+ "    p = 2 ? \"B\" | \n" //
 				+ "    p = 3 ? \"C\" | \n" //
 				+ "    \"D\" \n" //
 				+ ") >> \n" //
 				+ "switch {3}"));
 	}
 
 	@Test
 	public void testSys() {
 		assertNotNull(Tree.decompose(eval("cons {1} {2,}")));
 		assertEquals(Int.create(1), eval("head {1, 2, 3,}"));
 		assertNotNull(Tree.decompose(eval("tail {1, 2, 3,}")));
 	}
 
 	@Test
 	public void testZip() {
 		assertEquals(SuiteUtil.parse("(1, 5), (2, 6), (3, 7),"), eval("" //
 				+ "define zip-up = zip {a => b => a, b} >> \n" //
 				+ "zip-up {1, 2, 3,} {5, 6, 7,}"));
 	}
 
 	private static Node eval(String f) {
 		return SuiteUtil.evaluateFunctional(f, false);
 	}
 
 }
