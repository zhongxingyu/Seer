 /*******************************************************************************
  * Copyright (c) 2012 Cesar Yeep.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the BSD 3-Clause License
  * ("New BSD" or "BSD Simplified") which accompanies this distribution,
  * and is available at
  * http://opensource.org/licenses/BSD-3-Clause
  * 
  * Contributors:
  *     Cesar Yeep - initial API and implementation
  ******************************************************************************/
 package tests;
 
 import static org.junit.Assert.*;
 import org.junit.Test;
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 
 import beaver.Parser;
 
 import java.io.StringReader;
 import java.io.Reader;
 import java.io.BufferedReader;
 import java.io.IOException;
 
 import testFramework.TestUtilities;
 
 /** This class contains a set of test cases for the CleanJava parser.
  *  @author ceyeep
  */
 public class ParserTest {
 
 	private static TestUtilities testUtilities;
 
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@BeforeClass
 	public static void setUpClass() throws Exception {
 		testUtilities = new TestUtilities();
 		testUtilities.createTempDirectory();
 	}
 	
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@AfterClass
 	public static void tearDownClass() throws Exception {
 		testUtilities.deleteTempDirectory();
 	}
  
 	//tests
  
 	/** Test using this keyword. */
 	@Test
 	public void testParser01() {
 		assertParseOk("//@[this.x := 3]");
 	}
 	
 	/** Test single line notation assignment. */
 	@Test
 	public void testParser02() {
 		assertParseOk("//@[ x := 5 ]");
 	}
 	
 	/** Test object field assignment. */
 	@Test
 	public void testParser03() {
 		assertParseOk("//@[object.x := 3]");
 	}
 	
 	/** Test using simple addition expression. */
 	@Test
 	public void testParser04() {
 		assertParseOk("//@[ x :=  + 4 ]");
 	}
 	
 	/** Test using simple addition expression. */
 	@Test
 	public void testParser05() {
 		assertParseOk("//@[ x := x + 4 ]");
 	}
 	
 	/** Test multiple assignments. */
 	@Test
 	public void testParser06() {
 		assertParseOk("//@[ zero, z  := zero, zero +1 ]");
 	}
 	
 	/** Test constructor declaration. */
 	@Test
 	public void testParser07() {
 		assertParseOk("//@[ x := 3 ]","public Test(){ x = 3; }");
 	}
 	
 	/** Test instance initializer. */
 	@Test
 	public void testParser08() {
 		assertParseOk("//@[ x := 3 ]","{ x = 3; }");
 	}
 	
 	/** Test static initializer. */
 	@Test
 	public void testParser09() {
 		assertParseOk("//@[ x := 3 ]","static { x = 3; }");
 	}
 	
 	/** Test instance initializer. */
 	@Test
 	public void testParser10() {
 		assertParseOk("//@[ x := 3 ]","{ x = 3; }");
 	}
 	
 	/** Test intended function with multiple lines. */
 	@Test
 	public void testParser11() {
 		assertParseOk("/*@ [ x, y := \n 3, 4 ] #*/","{ x = 3; }");
 	}
 		
 	/** Test simple assignment (no blank spaces). */
 	@Test
 	public void testParser12() {
 		assertParseOk("//@[x:=5]");
 	}
 	
 	/** Test multiple line notation. */
 	@Test
 	public void testParser13() {
 		assertParseOk("/*@[ x := 5 ] @*/");
 	}
 	
 	/** Test multiple line notation. */
 	@Test
 	public void testParser14() {
 		assertParseOk("/*@[ x := 5 ]\n"+
 		"@*/");
 	}
 	
 	/** Test comments inside of CJ annotations. */
 	@Test
 	public void testParser15() {
 		assertParseOk("/*@[ x := 5 ]\n" +
 						"//a comment\n"+
 						"@*/");
 	}
 	
 	/** Test comments inside of CJ annotations. */
 	@Test
 	public void testParser16() {
 		assertParseOk("/*@[ x := 5 ]\n" +
 						"//a comment\n"+
 						"@*/");
 	}
 	
 	/** Test anything keyword. */
 	@Test
 	public void testParser17() {
 		assertParseOk("//@[ x := \\anything ]");
 	}
 	
 	/** Test informal notation. */
 	@Test
 	public void testParser18() {
 		assertParseOk("//@[ x := (* informal notaiton *) ]");
 	}
 	
 	/** Test conditional concurrent assignment. */
 	@Test
 	public void testParser19() {
 		assertParseOk("//@[ x > 0 -> x := 3 ]");
 	}
 	
 	/** Test conditional concurrent assignment with else statement. */
 	@Test
 	public void testParser20() {
 		assertParseOk("/*@[ x > 0 -> x := 3 ]\n" +
 						"\\else x := 4]\n"+
 						"@*/");
 	}
 	
 	/** Test conditional concurrent assignment with else statement
 		and I literal. */
 	@Test
 	public void testParser21() {
 		assertParseOk("/*@[ x > 0 -> x := 3 ]\n" +
 						"\\else \\I]\n"+
 						"@*/");
 	}
 	
 	/** Test CleanJava result. */
 	@Test
 	public void testParser22() {
 		String testCase = "//@ \\result := 3 ]";
 		String code = "public double foo2(){ return 3; }";
 		assertParseOk(testCase,code);
 	}
 	
 	//TEST PARSER FAILS
 	
 	/** Test missing value in the right side of the assignment expression. */
 	@Test
 	public void testParserFail01() {
 		assertParseError("//@[x := ]");
 	}
 	
 	/** Test incorrect assingment symbol. */
 	@Test
 	public void testParserFail02() {
 		assertParseError("//@[x : 3]");
 	}
 	
 	/** Test unclosed square bracket. */
 	@Test
 	public void testParserFail03() {
 		assertParseError("//@[x := 3");
 	}
 	
 	/** Test right-side side-effects (++). */
 	@Test
 	public void testParserFail04() {
 		assertParseError("//@[x := x++]");
 	}
 	
 	/** Test right-side side-effects (=). */
 	@Test
 	public void testParserFail05() {
 		String setUp = " int x; ";
 		String testCase = "//@[x := x = 2]";
		String errorMessage = "Syntactic Error: unexpected token \"=\"";
 		assertEquals(errorMessage,testUtilities.runChecker(setUp,testCase));
 	}
 	
 	/** Test right-side side-effects (+=). */
 	@Test
 	public void testParserFail06() {
 		String setUp = " int x; ";
 		String testCase = "//@[x := x += 2]";
		String errorMessage = "Syntactic Error: unexpected token \"=\"";
 		assertEquals(errorMessage,testUtilities.runChecker(setUp,testCase));
 	}
 		
 	//Utility methods
 	
 	/** Check if there are no parsing errors for a given testcase. */
 	protected static void assertParseOk(String intendedFunction) {
 		try {
 			parse(createStub(intendedFunction));
 		} catch(Parser.Exception e) {
 			System.out.println("Parser error");
 			fail(e.getMessage());
 		}
 	}
 	
 	/** Check if there are no parsing errors for a given testcase
 	    (specify chunk of code under intended function). */
 	protected static void assertParseOk(String intendedFunction, String code) {
 		try {
 			parse(createStub(intendedFunction, code));
 		} catch(Parser.Exception e) {
 			System.out.println("Parser error");
 			fail(e.getMessage());
 		}
 	}
 		
 	/** Checks that the parser returns an error for the given testcase. */
 	protected static void assertParseError(String intendedFunction) {
 		try {
 			parse(createStub(intendedFunction));
 		} catch(Parser.Exception e) {
 			return;
 		}
 		fail("Expected to find parse error in " + intendedFunction);
 	}
 	
 	/** Checks that the parser returns an error for the given testcase
 	    (specify chunk of code under intended function). */
 	protected static void assertParseError(String intendedFunction, String code) {
 		try {
 			parse(createStub(intendedFunction, code));
 		} catch(Parser.Exception e) {
 			return;
 		}
 		fail("Expected to find parse error in " + intendedFunction+"\n"+code);
 	}
 	
 	/** Creates a method stub for the given testcase. */
 	protected static String createStub(String testCase)
 	{
 		String stub = "public class Test{\n";
 		stub+=testCase;
 		stub+="\npublic void foo(){}\n}";
 		return stub;
 	}
 	
 	/** Creates a class stub for the given testcase (specific annotated coded). */
 	protected static String createStub(String intendedFunction, String code)
 	{
 		String stub = "public class Test{\n";
 		stub+=intendedFunction+"\n";
 		stub+=code;
 		stub+="\n\n}";
 		return stub;
 	}
 	
 	/** Creates a new Scanner and Parser and parses the given testcase. */
 	protected static void parse(String s) throws beaver.Parser.Exception{
 		try(Reader reader = new StringReader(s)){
 			scanner.JavaScanner scanner = new scanner.JavaScanner(new BufferedReader(reader));
 			new parser.JavaParser().parse(scanner);
 		}catch (IOException e) {
 			fail(e.getMessage());	
 		}
 	}
 }
