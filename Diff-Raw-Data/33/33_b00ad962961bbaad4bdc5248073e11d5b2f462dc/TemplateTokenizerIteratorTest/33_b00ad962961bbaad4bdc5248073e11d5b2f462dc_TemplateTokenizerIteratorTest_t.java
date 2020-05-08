 package org.libtoil.core;
 import junit.framework.TestCase;
 
 
 public class TemplateTokenizerIteratorTest extends TestCase {
 	public void testText1() {
 		TemplateTokenizer tt = new TemplateTokenizer("The swift brown fox jumped over the lazy dog");
 		String output = "";
 		for(TemplateTokenizer.Token t : tt) {
 			output +=t;
 			output += "-";
 		}
 		assertEquals("The- -swift- -brown- -fox- -jumped- -over- -the- -lazy- -dog-", output);
	}
 
	public void testText2() {
		TemplateTokenizer tt = new TemplateTokenizer("at@@at@ @at@ @@@at@ @ @at@ @at@@ @@@ @at");
		String output = "";
		for(TemplateTokenizer.Token t : tt) {
			output +=t;
			output += "-";
//			System.out.print(t + "-");
		}
		assertEquals("at-@@-at-@- -@at@- -@@-@at@- -@- -@at@- -@at@-@- -@@-@- -@-at-", output);
 	}

 }
