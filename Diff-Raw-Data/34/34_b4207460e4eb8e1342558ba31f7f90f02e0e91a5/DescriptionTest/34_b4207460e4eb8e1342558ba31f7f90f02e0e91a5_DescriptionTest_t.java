 package dfh.grammar;
 
 import static org.junit.Assert.*;
 
 import java.util.regex.Pattern;
 
 import org.junit.Test;
 
 public class DescriptionTest {
 
 	@Test
 	public void basic() {
 		String[] rules = {
 		//
 		"ROOT = 'foo'" };
 		Grammar g = new Grammar(rules);
 		assertTrue(
 				"basic description",
 				Pattern.compile("\\s*+ROOT = \"foo\"\\s*+")
 						.matcher(g.describe()).matches());
 	}
 
 	@Test
 	public void tagAll() {
 		String[] rules = {
 		//
 		"ROOT = [{bar} 'foo']" };
 		Grammar g = new Grammar(rules);
 		assertTrue(
 				"basic description",
 				Pattern.compile(
 						"\\s*+ROOT = \\[\\{bar\\}\\s*+\"foo\"\\s*+\\]\\s*+")
 						.matcher(g.describe()).matches());
 	}
 
 	@Test
 	public void alternation() {
 		String[] rules = {
 		//
 		"ROOT = 'foo' | [{bar} 'quux']" };
 		Grammar g = new Grammar(rules);
 		assertTrue(
 				"basic description",
 				Pattern.compile(
 						"\\s*+ROOT = \"foo\" \\| \\[\\{bar\\} \"quux\" ]\\s*+")
 						.matcher(g.describe()).matches());
 	}
 
 	@Test
 	public void sequence() {
 		String[] rules = {
 		//
 		"ROOT = 'foo' [{bar} 'quux']" };
 		Grammar g = new Grammar(rules);
 		assertTrue(
 				"basic description",
 				Pattern.compile(
 						"\\s*+ROOT = \"foo\" \\[\\{bar\\} \"quux\" ]\\s*+")
 						.matcher(g.describe()).matches());
 	}
 
 	@Test
 	public void repetition() {
 		String[] rules = {
 		//
 		"ROOT = [{bar} 'foo']+" };
 		Grammar g = new Grammar(rules);
 		assertTrue("basic description",
 				Pattern.compile("\\s*+ROOT = \\[\\{bar\\} \"foo\" ]\\+\\s*+")
 						.matcher(g.describe()).matches());
 	}
 
 	@Test
 	public void cyclesAndReversedRule() {
 		Grammar g = new Grammar(
 				new String[] {
 						//
 						"ROOT = <parens> | <square> | <curly> | <angled>",//
 						"parens = not after <escape> '(' [ /(?:\\\\.|[^()\\[\\]{}<>])++/ | <ROOT> ]*+ ')'",//
 						"square = not after <escape> '[' [ /(?:\\\\.|[^()\\[\\]{}<>])++/ | <ROOT> ]*+ ']'",//
 						"curly = not after <escape> '{' [ /(?:\\\\.|[^()\\[\\]{}<>])++/ | <ROOT> ]*+ '}'",//
 						"angled = not after <escape> '<' [ /(?:\\\\.|[^()\\[\\]{}<>])++/ | <ROOT> ]*+ '>'",//
 						"escape = /(?<!\\\\)(?:\\\\)++/r (odd)",//
 				});
 		g.defineCondition("odd", new Condition() {
 			@Override
 			public boolean passes(CharSequence subsequence) {
 				return subsequence.length() % 2 == 1;
 			}
 		});
 		String d = g.describe();
 		for (String s : new String[] { "ROOT", "parens", "square", "curly",
 				"angled", "escape" }) {
 			Pattern pattern = Pattern.compile("^\\s*+" + s, Pattern.MULTILINE);
 			assertTrue("contains rule " + s, pattern.matcher(d).find());
 		}
 	}
 
 	@Test
 	public void benchmarkRegression() {
 		String[] rules = {
 				//
 				"<ROOT> = [ <a> | <b> ]{2} <b>",//
 				"<a> = 'a'{,2}",//
 				"<b> = 'ab'",//
 		};
 		Grammar g = new Grammar(rules);
 		String d = g.describe();
 		for (String s : new String[] { "ROOT", "a", "b" }) {
 			Pattern pattern = Pattern.compile("^\\s*+" + s, Pattern.MULTILINE);
 			assertTrue("contains rule " + s, pattern.matcher(d).find());
 		}
 	}
 
 	@Test
 	public void taggedDDRInAlternation1() {
 		String s = "a = [{b} <c> ] | \"d\"";
 		Grammar g = new Grammar(s);
 		Grammar g2 = new Grammar("c = 'c'");
 		g.defineRule("c", g2);
 		s = g.describe();
 		assertTrue(s.indexOf("b") > -1);
 	}
 
 	@Test
 	public void taggedDDRInAlternation2() {
 		String s = "a = [{b} <c> ] | \"d\"";
 		Grammar g = new Grammar(s);
 		g.defineRule("c", "c");
 		s = g.describe();
 		assertTrue(s.indexOf("b") > -1);
 		int count = 0, index = 0;
 		while ((index = s.indexOf("c =", index) + 1) > 0)
 			count++;
 		assertEquals(1, count);
 	}
 
 	@Test
 	public void taggedDDRInAlternation3() {
 		String s = "a = [{b} <c> ] | \"d\"";
 		Grammar g = new Grammar(s);
 		g.defineRule("c", Pattern.compile("c"));
 		s = g.describe();
 		assertTrue(s.indexOf("b") > -1);
 		int count = 0, index = 0;
 		while ((index = s.indexOf("c =", index) + 1) > 0)
 			count++;
 		assertEquals(1, count);
 	}
 
 	@Test
 	public void taggedDDRInAlternation4() {
 		String s = "a = [{b} <c> ] | \"d\"";
 		Grammar g = new Grammar(s);
 		Grammar g2 = new Grammar("c = 'c'");
 		g.defineRule("c", g2, "e", new Condition() {
 		});
 		s = g.describe();
 		assertTrue(s.indexOf("b") > -1);
 		int count = 0, index = 0;
 		while ((index = s.indexOf("(e)", index) + 1) > 0)
 			count++;
 		assertEquals(1, count);
 	}
 
 	@Test
 	public void taggedDDRInAlternation5() {
 		String s = "a = [{b} <c> ] | \"d\"";
 		Grammar g = new Grammar(s);
 		g.defineRule("c", "c", "e", new Condition() {
 		});
 		s = g.describe();
 		assertTrue(s.indexOf("b") > -1);
 		int count = 0, index = 0;
 		while ((index = s.indexOf("(c)", index) + 1) > 0)
 			count++;
 		assertEquals(1, count);
 	}
 
 	@Test
 	public void taggedDDRInAlternation6() {
 		String s = "a = [{b} <c> ] | \"d\"";
 		Grammar g = new Grammar(s);
 		g.defineRule("c", Pattern.compile("c"), "e", new Condition() {
 		});
 		s = g.describe();
 		assertTrue(s.indexOf("b") > -1);
 		int count = 0, index = 0;
 		while ((index = s.indexOf("(e)", index) + 1) > 0)
 			count++;
 		assertEquals(1, count);
 	}
 
 	@Test
 	public void taggedDDRInAlternation7() {
 		String[] rules = {
 				//
 				"a = [{e} <c> | [{b} \"d\"] ]++", //
 				"c = 'c'",//
 		};
 		Grammar g = new Grammar(rules);
 		String s = g.describe();
 		new Grammar(s);
 	}
 
 	@Test
 	public void taggedDDRInAlternation8() {
 		String s = "a = [{e} <c> | [{b} \"d\"] ]++";
 		Grammar g = new Grammar(s);
 		g.defineRule("c", Pattern.compile("c"));
 		s = g.describe();
 		new Grammar(s);
 	}
 
	@Test
	public void repetition2() {
		String[] rules = {
				//
				"v4 = <byte> [ '.' <byte> ]{3}",//
				"byte = /\\d{1,3}/r",//
		};
		Grammar g = new Grammar(rules);
		String s = g.describe();
		assertTrue(s.indexOf("]{3}") > -1);
	}
 }
