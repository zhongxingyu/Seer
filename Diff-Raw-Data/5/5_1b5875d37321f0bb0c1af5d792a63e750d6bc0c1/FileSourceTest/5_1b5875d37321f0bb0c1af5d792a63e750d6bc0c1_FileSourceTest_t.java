 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.peterlavalle.degen.extractors;
 
 import edu.emory.mathcs.backport.java.util.Arrays;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipOutputStream;
 import junit.framework.TestCase;
 
 /**
  *
  * @author peter
  */
 public class FileSourceTest extends TestCase {
 
 	public void testParseFileSourceRecipe_full() throws MalformedURLException {
 		final String test = "file://something @h/a @h/i/zip ~foo. =bar";
 
 		final FileSource.Recipe recipe = new FileSource.Recipe(test);
 
 		assertEquals("file://something", recipe.getUrl().toString());
 		assertEquals(2, recipe.getZipList().size());
 		assertEquals("h/a", recipe.getZipList().get(0));
 		assertEquals("h/i/zip", recipe.getZipList().get(1));
 		assertEquals("^foo.$", recipe.getExpression());
 		assertEquals("bar", recipe.getReplacement());
 	}
 
 	public void testParseFileSourceString_simple() throws MalformedURLException {
 		final String test = "       file://something.else   ";
 
 		final FileSource.Recipe recipe = new FileSource.Recipe(test);
 
 		assertEquals("file://something.else", recipe.getUrl().toString());
 		assertEquals(0, recipe.getZipList().size());
 		assertEquals("^.*$", recipe.getExpression());
 		assertEquals("$0", recipe.getReplacement());
 	}
 
 	public void testParseFileSourceString_simpleSelect() throws MalformedURLException {
 		final String test = "       file://something.el2se  ~bar  ";
 
 		final FileSource.Recipe recipe = new FileSource.Recipe(test);
 
 		assertEquals("file://something.el2se", recipe.getUrl().toString());
 		assertEquals(0, recipe.getZipList().size());
 		assertEquals("^bar$", recipe.getExpression());
 		assertEquals("$0", recipe.getReplacement());
 	}
 
 	public void testParseFileSourceString_simpleRewrite() throws MalformedURLException {
 		final String test = "       file://something.el2se  ~bar =foo. ";
 
 		final FileSource.Recipe recipe = new FileSource.Recipe(test);
 
 		assertEquals("file://something.el2se", recipe.getUrl().toString());
 		assertEquals(0, recipe.getZipList().size());
 		assertEquals("^bar$", recipe.getExpression());
 		assertEquals("foo.", recipe.getReplacement());
 	}
 	public void testParseFileSourceString_simpleReplace() throws MalformedURLException {
 		final String test = "       file://something.el2se   =foo.$0 ";
 
 		final FileSource.Recipe recipe = new FileSource.Recipe(test);
 
 		assertEquals("file://something.el2se", recipe.getUrl().toString());
 		assertEquals(0, recipe.getZipList().size());
 		assertEquals("^.*$", recipe.getExpression());
 		assertEquals("foo.$0", recipe.getReplacement());
 	}
 
 	public void testFinalName0() throws IOException {
 		final FileSource source = new FileSource(new FileSource.Recipe("file:///???? ~(b.)r =$1-da"));
 
 		assertEquals("ba-da", source.getFinalName("bar"));
 	}
 
 	public void testFinalName1() throws IOException {
 		final FileSource source = new FileSource(new FileSource.Recipe("file:///???? ~(b.)r =$1-da"));
 
 
 		assertEquals("bu-da", source.getFinalName("bur"));
 	}
 
 	public void testFinalName2() throws IOException {
 		final FileSource source = new FileSource(new FileSource.Recipe("file:///???? ~(b.)r =$1-da"));
 
 
 		assertEquals("bi-da", source.getFinalName("bir"));
 	}
 
 	public void testFinalName3() throws IOException {
 		final FileSource source = new FileSource(new FileSource.Recipe("file:///???? ~(b.)r =$1-da"));
 
 
 		assertEquals("b.-da", source.getFinalName("b.r"));
 	}
 
 	public void testFinalName4() throws IOException {
 		final FileSource source = new FileSource(new FileSource.Recipe("file:///???? ~(b.)r =$1-da"));
 
 
 		assertEquals("bo-da", source.getFinalName("bor"));
 	}
 
 	public void testFinalName5() throws IOException {
 		final FileSource source = new FileSource(new FileSource.Recipe("file:///???? ~(b.)r =$1-da"));
 
 
 		assertEquals(null, source.getFinalName("baa"));
 	}
 
 	public void testFinalName6() throws IOException {
 		final FileSource source = new FileSource(new FileSource.Recipe("file:///???? ~(b.)r =$1-da"));
 
 
 		assertEquals(null, source.getFinalName("foo"));
 	}
 
 	public void testFinalName7() throws IOException {
 		final FileSource source = new FileSource(new FileSource.Recipe("file:///???? ~(b.)r =$1-da"));
 
 		assertEquals(null, source.getFinalName("Bar"));
 	}
 	
 	public static final String[] TEMP_FILE_CONTENTS = new String[]{"foo", "bar", "fbar", "bur", "bir", "fir", "Bir", "baa", "baar"};
 
 	public void testWithFakeArchive() throws IOException {
 		final File tempFile = File.createTempFile("foo", "bar");
 		tempFile.deleteOnExit();
 
 		assertTrue(tempFile.canWrite());
 		assertTrue(tempFile.canRead());
 
 		// create the zip
 		{
 			final ZipOutputStream outputStream = new ZipOutputStream(new FileOutputStream(tempFile));
 
 			for (final String s : TEMP_FILE_CONTENTS) {
 				outputStream.putNextEntry(new ZipEntry(s));
 				outputStream.write(s.getBytes(), 0, s.length());
 			}
 
 			outputStream.close();
 		}
 
 		// create the recipe
 		final FileSource.Recipe recipe = new FileSource.Recipe("file://" + tempFile.getAbsolutePath() + " ~(b.)r =$1-da");
 
 		// create the object (yay!)
 		final FileSource source = new FileSource(recipe);
 
 		// check that we have the right names
 		assertEquals(3, source.getOriginalNames());
 		assertEquals("bar", source.getOriginalNames().get(0));
 		assertEquals("bur", source.getOriginalNames().get(1));
 		assertEquals("bir", source.getOriginalNames().get(2));
 
 		// check that the renames work
 		assertEquals("ba-da", source.getFinalName(source.getOriginalNames().get(0)));
 		assertEquals("bu-da", source.getFinalName(source.getOriginalNames().get(1)));
 		assertEquals("bi-da", source.getFinalName(source.getOriginalNames().get(2)));
 
 		// check that the reads work (when they should)
 		assertEquals("bar".getBytes(), source.getBytes((source.getOriginalNames().get(0))));
 		assertEquals("bur".getBytes(), source.getBytes((source.getOriginalNames().get(1))));
 		assertEquals("bir".getBytes(), source.getBytes((source.getOriginalNames().get(2))));
 
 		// check that the reads don't work (when they should not)
 		assertNull(source.getBytes(source.getOriginalNames().get(0)));
 		assertNull(source.getBytes(source.getOriginalNames().get(1)));
 		assertNull(source.getBytes(source.getOriginalNames().get(2)));
 	}
	
	
	public void testExtractor() throws IOException {
		fail("There is no test of the extractor ready");
	}
 }
