 package org.dsf;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.JUnit4;
 
 import static org.junit.Assert.*;
 
 @RunWith(JUnit4.class)
 public class ClassNameMatchesTest {
 	@Test
 	public void emptyPattern() {
 		assertFalse(new ClassName("AClass").matches(""));
 	}
 	
 	@Test
 	public void whitespacePattern() {
 		assertFalse(new ClassName("AClass").matches(" "));
 	}
 	
 	@Test
 	public void nullPattern() {
 		assertFalse(new ClassName("AClass").matches(null));
 	}
 	
 	@Test
 	public void oneCapitalChar() {
 		assertFalse(new ClassName("AClass").matches("T"));
 		assertTrue(new ClassName("T").matches("T"));
 		assertTrue(new ClassName("Test").matches("T"));
 		assertTrue(new ClassName("TestClass").matches("T"));
 	}
 	
 	@Test
 	public void twoCapitalChars() {
 		assertFalse(new ClassName("Test").matches("TC"));
 		assertTrue(new ClassName("TestClass").matches("TC"));
 		assertTrue(new ClassName("TestClassSecond").matches("TC"));
 	}
 	
 	@Test
 	public void specifyingChars() {
 		assertTrue(new ClassName("TestClass").matches("TeCla"));
 		assertTrue(new ClassName("TestClassSecond").matches("TeCla"));
 	}
 	
 	@Test
 	public void specifyingCharsNoMatch() {
 		assertFalse(new ClassName("TestClass").matches("TeCzz"));
 		assertFalse(new ClassName("TestClass").matches("TeClasss"));
 	}
 	
 	@Test
 	public void trailingWhitespace() {
 		assertFalse(new ClassName("TestClassSecond").matches("TC "));
 		assertTrue(new ClassName("TestClass").matches("TC "));
 	}
 	
 	@Test
 	public void multipleTrailingWhitespace() {
 		assertFalse(new ClassName("TestClassSecond").matches("TC   "));
 		assertTrue(new ClassName("TestClass").matches("TC   "));
 	}
 	
 	@Test
 	public void trailingWhitespaceSpecifyingChars() {
 		assertFalse(new ClassName("TestClassSecond").matches("TeCla "));
 		assertFalse(new ClassName("TestClass").matches("TeCz "));
 		assertTrue(new ClassName("TestClass").matches("TeCla "));
 	}
 	
 	@Test
 	public void middleWhitespace() {
 		assertFalse(new ClassName("TestClassSecond").matches("TC BUGA"));
 		assertTrue(new ClassName("TestClass").matches("TC BUGA"));
 	}
 	
 	@Test
 	public void leadingWildcard() {
 		assertFalse(new ClassName("FooFoo").matches("*B"));
 		assertTrue(new ClassName("FooBar").matches("*B"));
 		assertTrue(new ClassName("FooBarBaz").matches("*B"));
 	}
 	
 	@Test
 	public void multipleLeadingWildcard() {
 		assertFalse(new ClassName("FooFoo").matches("**B"));
 		assertTrue(new ClassName("FooBar").matches("**B"));
 		assertTrue(new ClassName("FooBarBaz").matches("***B"));
 	}
 	
 	@Test
 	public void trailingWildcard() {
 		assertFalse(new ClassName("Test").matches("TC*"));
 		assertTrue(new ClassName("TestClass").matches("TC*"));
 		assertTrue(new ClassName("TestClassSecond").matches("TC*"));
 	}
 	
 	@Test
 	public void trailingWildcardAndWhitespace() {
 		assertTrue(new ClassName("TestClass").matches("TC* "));
 		assertTrue(new ClassName("TestClassSecond").matches("TC* "));
 	}
 	
 	@Test
 	public void multipleTrailingWildcard() {
 		assertFalse(new ClassName("Test").matches("TC**"));
 		assertTrue(new ClassName("TestClass").matches("TC**"));
 		assertTrue(new ClassName("TestClassSecond").matches("TC***"));
 	}
 	
 	@Test
 	public void middleWildcard() {
 		assertFalse(new ClassName("MyPrecious").matches("M*SP"));
 		assertTrue(new ClassName("MyPrecious").matches("M*P"));
 		assertTrue(new ClassName("MySuperPrecious").matches("M*P"));
 		assertTrue(new ClassName("MySuperPrecious").matches("M*SP"));
 	}
 	
 	@Test
 	public void middleWildcardSpecifyingChars() {
 		assertFalse(new ClassName("MyPrecious").matches("M*P*z"));
 		assertTrue(new ClassName("MyPrecious").matches("M*P*o"));
 		assertTrue(new ClassName("MySuperPrecious").matches("M*P*o"));
 		assertTrue(new ClassName("MyPrecious").matches("M*Pre*ou"));
 	}
 	
 	@Test
 	public void multipleMiddleWildcard() {
 		assertTrue(new ClassName("MyPrecious").matches("M***P"));
 		assertTrue(new ClassName("MySuperPrecious").matches("M**P"));
 	}
 	
 	@Test
 	public void lowercaseFirstChar() {
 		assertFalse(new ClassName("testClass").matches("eC"));
 		assertTrue(new ClassName("testClass").matches("tC"));
 	}
 	
 	@Test
 	public void lowercaseFirstCharWithSpecifyingChar() {
 		assertFalse(new ClassName("testClass").matches("tsC"));
 		assertFalse(new ClassName("testClass").matches("tsCa"));
 		assertTrue(new ClassName("testClass").matches("teCl"));
 	}
 	
 	@Test
 	public void nonAsciiChars() {
		assertFalse(new ClassName("моя.прелесссть.МойÜberКласс").matches("МÜK")); //K is ASCII here
		assertTrue(new ClassName("моя.прелесссть.МойÜberКласс").matches("МÜК")); //K is cyrillic here
 	}
 	
 	@Test
 	public void nonAlphabeticalCharsChars() {
		assertFalse(new ClassName("my.test.$Proxy2_SomeClass").matches("$Proxy_SC")); //K is ASCII here
		assertTrue(new ClassName("my.test.$Proxy2_SomeClass").matches("$Proxy2_SC")); //K is cyrillic here
 	}
 }
