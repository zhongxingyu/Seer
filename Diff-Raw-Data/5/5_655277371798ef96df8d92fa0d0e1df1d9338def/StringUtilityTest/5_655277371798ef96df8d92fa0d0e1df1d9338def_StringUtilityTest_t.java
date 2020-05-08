 package semanticMarkup.ling.learn;
 
 import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 
 import org.junit.Test;
 
 import semanticMarkup.ling.learn.knowledge.Constant;
 import semanticMarkup.ling.learn.utility.StringUtility;
 
 public class StringUtilityTest {
 
 	@Test
 	public void testStrip() {
 		// Method strip
 		assertEquals("strip", "word1 word2",
 				StringUtility.strip("word1 <abc> word2"));
 		assertEquals("strip", "word1 word2",
 				StringUtility.strip("word1 <?abc?> word2"));
 		assertEquals("strip", "word1 word2",
 				StringUtility.strip("word1 &nbsp; word2"));
 	}
 
 	@Test
 	public void testRemovePunctuation() {
 		// Method removePunctuation
 		assertEquals("removePunctuation", "word word word wo-rd cant Id end", 
 				StringUtility.removePunctuation("word word, word&$% wo-rd can't I'd end.","-"));
 	}
 
 	@Test
 	public void testTrimString() {
 		// Method trimString
 		assertEquals("trimString head", "word", StringUtility.trimString("	 	word"));
 		assertEquals("trimString tail", "word",
 				StringUtility.trimString("word   		 	"));
 		assertEquals("trimString head and tail", "word",
 				StringUtility.trimString("	 	word	 	 		  "));
 	}
 
 	@Test
 	public void testProcessWord() {
 		// Method processWord
 		String word = "<word>word <\\iword>word word</word2>";
 		assertEquals("processWord", "word word word",
 				StringUtility.processWord(word));
 		assertEquals("processWord", "word word word",
 				StringUtility.processWord(" 	 word word word"));
 		assertEquals("processWord", "word word word",
 				StringUtility.processWord("word word word 	 "));
 	}
 
 	@Test
 	public void testRemoveAll() {
 		// Method removeAll
 		assertEquals("removeAll - begin", "word word ",
 				StringUtility.removeAll("   word word ", "^\\s+"));
 		assertEquals("removeAll - end", "word|word", 
 				StringUtility.removeAll("word|word|", "\\|+$"));
 		assertEquals("removeAll - all", "wordword", 
 				StringUtility.removeAll("|word|word|", "\\|"));
 		assertEquals("removeAll - remove beginning", "word", 
 				StringUtility.removeAll("above word","^("+Constant.STOP+"|"+Constant.FORBIDDEN+")\\b\\s*"));
 		assertEquals("removeAll - remove ending 1", "word1 word2", 
 				StringUtility.removeAll("word1 word2 or","\\s*\\b("+Constant.STOP+"|"+Constant.FORBIDDEN+"|\\w+ly)$"));
 		assertEquals("removeAll - remove ending 2", "word1 word2", 
 				StringUtility.removeAll("word1 word2 usually","\\s*\\b("+Constant.STOP+"|"+Constant.FORBIDDEN+"|\\w+ly)$"));
 		assertEquals("removeAll - remove middle pronouns", "word1  word2", 
 				StringUtility.removeAll("word1 each word2","\\b("+Constant.PRONOUN+")\\b"));
 		assertEquals("removeAll - remove beginning and ending", "word", 
 				StringUtility.removeAll(" 	word	 	","(^\\s*|\\s*$)"));
 	}
 
 	@Test
 	public void testIsWord() {
 		// Method isWord
 		assertEquals("isWord - Length not > 1", false, StringUtility.isWord("a"));
 		assertEquals("isWord - not all word characters", false, StringUtility.isWord("%^"));
 		assertEquals("isWord - all word characters", true, StringUtility.isWord("ab"));
 		assertEquals("isWord - STOP word", false, StringUtility.isWord("state"));
 		assertEquals("isWord - STOP word", false, StringUtility.isWord("page"));
 		assertEquals("isWord - STOP word", false, StringUtility.isWord("fig"));
 	}
 	
 	@Test
 	public void testIsMatchedWords() {
 		assertEquals("isMatchedWords", true,
 				StringUtility.isMatchedWords("and", Constant.FORBIDDEN));
 		assertEquals("isMatchedWords", false,
 				StringUtility.isMatchedWords("kahgds", Constant.FORBIDDEN));
 	}
 	
 	@Test
 	public void testRemoveFromWordList() {
 //		assertEquals(
 //				"removeFromWordList",
 //				"ab|ad|bi|deca|dis|di|dodeca|endo|end|e|hemi|hetero|hexa|homo|infra|inter|ir|macro|mega|meso|micro|mid|mono|multi|ob|octo|over|penta|poly|postero|post|ptero|pseudo|quadri|quinque|semi|sub|sur|syn|tetra|tri|uni|un|xero|[a-z0-9]+_",
 //				StringUtility
 //						.removeFromWordList(
 //								"de",
 //								"ab|ad|bi|deca|de|dis|di|dodeca|endo|end|e|hemi|hetero|hexa|homo|infra|inter|ir|macro|mega|meso|micro|mid|mono|multi|ob|octo|over|penta|poly|postero|post|ptero|pseudo|quadri|quinque|semi|sub|sur|syn|tetra|tri|uni|un|xero|[a-z0-9]+_"));
 		assertEquals(
 				"removeFromWordList",
 				"ad|bi|deca|de|dis|di|dodeca|endo|end|e|hemi|hetero|hexa|homo|infra|inter|ir|macro|mega|meso|micro|mid|mono|multi|ob|octo|over|penta|poly|postero|post|ptero|pseudo|quadri|quinque|semi|sub|sur|syn|tetra|tri|uni|un|xero|[a-z0-9]+_",
 				StringUtility
 						.removeFromWordList(
 								"ab",
 								"ab|ad|bi|deca|de|dis|di|dodeca|endo|end|e|hemi|hetero|hexa|homo|infra|inter|ir|macro|mega|meso|micro|mid|mono|multi|ob|octo|over|penta|poly|postero|post|ptero|pseudo|quadri|quinque|semi|sub|sur|syn|tetra|tri|uni|un|xero|[a-z0-9]+_"));
 		assertEquals(
 				"removeFromWordList",
 				"above|across|after|along|around|as|at|before|below|beneath|between|beyond|by|during|for|from|in|into|near|of|off|on|onto|out|outside|over|than|through|throughout|toward|towards|up|upward|with",
 				StringUtility
 						.removeFromWordList(
 								"without",
 								"above|across|after|along|around|as|at|before|below|beneath|between|beyond|by|during|for|from|in|into|near|of|off|on|onto|out|outside|over|than|through|throughout|toward|towards|up|upward|with|without"));
 	}
 
 	/**
 	
 		while($modifier =~ /^($stop|$FORBIDDEN)\b/){
 		$modifier =~ s#^($stop|$FORBIDDEN)\b\s*##g;
 	}
 
 	while($tag =~ /^($stop|$FORBIDDEN)\b/){
 		$tag =~ s#^($stop|$FORBIDDEN)\b\s*##g;
 
 	}
 	
 		#from ending
 	while($modifier =~ /\b($stop|$FORBIDDEN|\w+ly)$/){
 		$modifier =~ s#\s*\b($stop|$FORBIDDEN|\w+ly)$##g;
 	}
 
 	while($tag =~ /\b($stop|$FORBIDDEN|\w+ly)$/){
 		$tag =~ s#\s*\b($stop|$FORBIDDEN|\w+ly)$##g;
 
 	}
 
 	
 	
 	 */
 	
 	@Test
 	public void testRemoveAllRecursive() {
 		assertEquals("removeAllRecursive - beginning", "word", 
 				StringUtility.removeAllRecursive("stop stop word", "^(stop)\\b\\s*"));
 		assertEquals("removeAllRecursive - ending", "word", 
 				StringUtility.removeAllRecursive("word word1ly word2ly", "\\s*\\b\\w+ly$"));
 		
 	}
 	
 //	@Test
 //	public void testEqualsWithNull(){
 //		assertEquals("equalsWithNull - null : null", true, StringUtility.equalsWithNull(null, null));
 //		assertEquals("equalsWithNull - null : not null", false, StringUtility.equalsWithNull(null, "s2"));
 //		assertEquals("equalsWithNull - not null : null", false, StringUtility.equalsWithNull("s1", null));
 //		assertEquals("equalsWithNull - not null : not null - equal", true, StringUtility.equalsWithNull("abc", "abc"));
 //		assertEquals("equalsWithNull - not null : not null - not equal", false, StringUtility.equalsWithNull("s1", "s2"));
 //	}
 	
 	@Test
 	public void testStringArray2String() {
 		assertEquals("stringArray2String", "teeth unicuspid with", StringUtility.stringArray2String(("teeth unicuspid with".split(" "))));
 	}
 	
 	
 	@Test
 	public void testStringArraySplice() {		
 		List<String> target1 = new ArrayList<String>();
 		target1.addAll(Arrays.asList("hyohyoidei muscle".split(" ")));
 		assertEquals("stringArraySplice", target1, StringUtility.stringArraySplice(Arrays.asList("hyohyoidei muscle".split(" ")), 0, 2));
 		
 	}
 	
 	@Test
 	public void testJoinList(){
 		List<String> input = new ArrayList<String>();
 		input.addAll(Arrays.asList("word1 word2 word3".split(" ")));
 		String sep = "+++";
 		assertEquals("stringArraySplice", "word1+++word2+++word3", StringUtility.joinList(sep, input));
 	}
 	
 	@Test
	public void testIsEntireMatchedNullSafe(){
 		assertEquals("not match - pattern null", false, StringUtility.isEntireMatchedNullSafe("[abc]", null));
 		assertEquals("not match - text null", false, StringUtility.isEntireMatchedNullSafe(null, "^\\[.*$"));
		assertEquals("not match - pattern empty", true, StringUtility.isEntireMatchedNullSafe("[abc]", ""));
 		assertEquals("not match - text empty", false, StringUtility.isEntireMatchedNullSafe("", "^\\[.*$"));
 		assertEquals("not match", false, StringUtility.isEntireMatchedNullSafe("abc", "^\\z.*$"));
 		assertEquals("match", true, StringUtility.isEntireMatchedNullSafe("[abc]", "^\\[.*$"));
 	}
 	
 	@Test
 	public void testReplaceAllBackreference(){
 		assertEquals("addHeadTailSpace", "word1 ,    word2 .    word3 !    word4 . ", StringUtility.replaceAllBackreference("word1, word2. word3! word4.", "(\\W)", " $1 "));
 	}
 	
 	@Test
 	public void testSetSub(){
 		Set<String> a = new HashSet<String>();
 		a.add("word1");
 		a.add("word2");
 		a.add("word3");
 		
 		Set<String> b = new HashSet<String>();
 		b.add("word2");
 		b.add("word3");
 		b.add("word4");
 		
 		Set<String> c = new HashSet<String>();
 		c.add("word1");
 		
 		assertEquals("setSub", c, StringUtility.setSubtraction(a, b));
 		assertEquals("setSub", a, StringUtility.setSubtraction(a, null));
 		assertEquals("setSub", null, StringUtility.setSubtraction(null, b));
 	}
 	
 }
