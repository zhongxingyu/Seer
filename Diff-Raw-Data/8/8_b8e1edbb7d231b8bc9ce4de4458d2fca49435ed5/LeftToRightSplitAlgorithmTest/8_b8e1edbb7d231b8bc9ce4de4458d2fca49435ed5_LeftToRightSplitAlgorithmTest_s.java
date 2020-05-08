 /**
  * Copyright (c) 2010 Jens Haase <je.haase@googlemail.com>
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 
 package de.tudarmstadt.ukp.teaching.uima.nounDecompounding.splitter;
 
 import java.util.List;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 
 import de.tudarmstadt.ukp.teaching.uima.nounDecompounding.dictionary.IDictionary;
 import de.tudarmstadt.ukp.teaching.uima.nounDecompounding.dictionary.LinkingMorphemes;
 import de.tudarmstadt.ukp.teaching.uima.nounDecompounding.dictionary.SimpleDictionary;
 
 public class LeftToRightSplitAlgorithmTest {
 	
 	@Test
 	public void testSplit1() {
 		IDictionary dict = new SimpleDictionary("Akt" ,"ion", "plan", "Aktion", "Aktionsplan");
 		LinkingMorphemes morphemes = new LinkingMorphemes("s");
 		LeftToRightSplitAlgorithm algo = new LeftToRightSplitAlgorithm(dict, morphemes);
 		
 		List<Split> result = algo.split("Aktionsplan").getAllSplits();
 		Assert.assertEquals(6, result.size());
 		Assert.assertEquals("akt+ionsplan", result.get(0).toString());
 		Assert.assertEquals("aktionsplan", result.get(1).toString());
 		Assert.assertEquals("aktion(s)+plan", result.get(2).toString());
 		Assert.assertEquals("akt+ion(s)+plan", result.get(3).toString());
 		Assert.assertEquals("aktion+splan", result.get(4).toString());
 		Assert.assertEquals("akt+ion+splan", result.get(5).toString());
 	}
 	
 	@Test
 	public void testSplit2() {
 		IDictionary dict = new SimpleDictionary("Donau" ,"dampf", "schiff", "fahrt", "dampfschiff", "schifffahrt");
 		LinkingMorphemes morphemes = new LinkingMorphemes("s");
 		LeftToRightSplitAlgorithm algo = new LeftToRightSplitAlgorithm(dict, morphemes);
 		
 		List<Split> result = algo.split("Donaudampfschifffahrt").getAllSplits();
 		Assert.assertEquals(6, result.size());
 	}
 	
 	@Test
 	public void testSplit3() {
 		IDictionary dict = new SimpleDictionary("Super" ,"mann", "anzug", "Supermann", "anzug");
 		LinkingMorphemes morphemes = new LinkingMorphemes("s");
 		LeftToRightSplitAlgorithm algo = new LeftToRightSplitAlgorithm(dict, morphemes);
 		
 		List<Split> result = algo.split("Supermannanzug").getAllSplits();
 		// Super+mann+anzug, Supermann+anzug
 		Assert.assertEquals(4, result.size());
 	}
 	
 	@Test
 	public void testMorphemes1() {
 		IDictionary dict = new SimpleDictionary("alarm" ,"reaktion");
 		LinkingMorphemes morphemes = new LinkingMorphemes("en");
 		LeftToRightSplitAlgorithm algo = new LeftToRightSplitAlgorithm(dict, morphemes);
 		
 		List<Split> result = algo.split("alarmreaktionen").getAllSplits();
 		// Super+mann+anzug, Supermann+anzug
		Assert.assertEquals(4, result.size());
 		Assert.assertEquals("alarm+reaktionen", result.get(0).toString());
		Assert.assertEquals("alarm+reaktion+en", result.get(1).toString());
		Assert.assertEquals("alarmreaktionen", result.get(2).toString());
		Assert.assertEquals("alarm+reaktion(en)", result.get(3).toString());
 	}
 }
