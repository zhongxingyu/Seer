 package com.partkyle.prefix.test;
 
import static org.junit.Assert.*;
 
import org.junit.BeforeClass;
 import org.junit.Test;
 
 import com.partkyle.prefix.InfixToPrefixConverter;
 
 public class InfixToPrefixTest {
 
 	InfixToPrefixConverter converter = new InfixToPrefixConverter();
 
 	@Test
 	public void testSimple() {
 		assertEquals("3", converter.convert("3"));
 	}
 
 	@Test
 	public void testAddition() {
 		assertEquals("(+ 1 1)", converter.convert("1 + 1"));
 	}
 
 	@Test
 	public void testMultiplication() {
 		String result = converter.convert("2 * 5 + 1");
 		if ("(+ 1 (* 2 5))".equals(result)) {
 
 		} else if ("(+ (* 2 5) 1)".equals(result)) {
 
 		} else {
 			fail(String.format("%s was not the correct answer", result));
 		}
 	}
 
 	@Test
 	public void testParenthesis() {
 		String result = converter.convert("2 * ( 5 + 1 )");
 		if ("(* (+ 5 1) 2)".equals(result)) {
 
 		} else if ("(* 2 (+ 5 1))".equals(result)) {
 
 		} else {
 			fail(String.format("%s was not the correct answer", result));
 		}
 	}
 
 	@Test
 	public void testComplex() {
 		assertEquals("(+ (* 3 x) (/ (+ 9 y) 4))", converter.convert("3 * x + ( 9 + y ) / 4"));
 	}
 }
