 package atto;
 
 import java.io.StringWriter;
 
import atto.Interpreter;
import atto.lang.Function;

 import junit.framework.TestCase;
 
 public class InterpreterTest extends TestCase {
 
     public void testFUN_expr() throws Exception {
         Interpreter i = new Interpreter();
         Object result = i.run("fun x,y -> x+y\n");
         assertTrue(result instanceof Function);
     }
 
     public void testFUN_block() throws Exception {
         Interpreter i = new Interpreter();
         Object result = i.run("fun x,y ->\n  x+y\n");
         assertTrue(result instanceof Function);
     }
 
     public void testCall() throws Exception {
         Interpreter i = new Interpreter();
         Object result = i.run("f=fun x,y->x+y\nf(1,2)\n");
         assertEquals(new Integer(3), result);
     }
 
     public void testPRINT() throws Exception {
         StringWriter writer = new StringWriter();
         Interpreter i = new Interpreter(writer);
         i.run("a = 100\nprint a\n");
        assertEquals("100\n", writer.toString());
     }
 
     public void testOR() throws Exception {
         Interpreter i = new Interpreter();
         assertEquals(Boolean.TRUE, i.run("1 || 1\n"));
         assertEquals(Boolean.TRUE, i.run("1 || 0\n"));
         assertEquals(Boolean.TRUE, i.run("1 || 0\n"));
         assertEquals(Boolean.FALSE, i.run("0 || 0\n"));
     }
 
     public void testAND() throws Exception {
         Interpreter i = new Interpreter();
         assertEquals(Boolean.TRUE, i.run("1 && 1\n"));
         assertEquals(Boolean.FALSE, i.run("1 && 0\n"));
     }
 
     public void testEQ() throws Exception {
         Interpreter i = new Interpreter();
         assertEquals(Boolean.TRUE, i.run("10 == 10\n"));
     }
 
     public void testNE() throws Exception {
         Interpreter i = new Interpreter();
         assertEquals(Boolean.TRUE, i.run("10 != 20\n"));
     }
 
     public void testLT() throws Exception {
         Interpreter i = new Interpreter();
         assertEquals(Boolean.TRUE, i.run("10 < 20\n"));
         assertEquals(Boolean.FALSE, i.run("10 < 10\n"));
         assertEquals(Boolean.FALSE, i.run("20 < 10\n"));
     }
 
     public void testGT() throws Exception {
         Interpreter i = new Interpreter();
         assertEquals(Boolean.FALSE, i.run("10 > 20\n"));
         assertEquals(Boolean.FALSE, i.run("10 > 10\n"));
         assertEquals(Boolean.TRUE, i.run("20 > 10\n"));
     }
 
     public void testLE() throws Exception {
         Interpreter i = new Interpreter();
         assertEquals(Boolean.TRUE, i.run("10 <= 20\n"));
         assertEquals(Boolean.TRUE, i.run("10 <= 10\n"));
         assertEquals(Boolean.FALSE, i.run("20 <= 10\n"));
     }
 
     public void testGE() throws Exception {
         Interpreter i = new Interpreter();
         assertEquals(Boolean.FALSE, i.run("10 >= 20\n"));
         assertEquals(Boolean.TRUE, i.run("10 >= 10\n"));
         assertEquals(Boolean.TRUE, i.run("20 >= 10\n"));
     }
 
     public void testPLUS() throws Exception {
         Interpreter i = new Interpreter();
         assertEquals(new Integer(30), i.run("10 + 20\n"));
     }
 
     public void testMINUS() throws Exception {
         Interpreter i = new Interpreter();
         assertEquals(new Integer(-10), i.run("10 - 20\n"));
     }
 
     public void testMUL() throws Exception {
         Interpreter i = new Interpreter();
         assertEquals(new Integer(200), i.run("10 * 20\n"));
     }
 
     public void testDIV() throws Exception {
         Interpreter i = new Interpreter();
         assertEquals(new Integer(5), i.run("100 / 20\n"));
     }
 
     public void testMOD() throws Exception {
         Interpreter i = new Interpreter();
         assertEquals(new Integer(3), i.run("8 % 5\n"));
     }
 
     public void testNOT() throws Exception {
         Interpreter i = new Interpreter();
         assertEquals(Boolean.TRUE, i.run("!false\n"));
         assertEquals(Boolean.TRUE, i.run("!0\n"));
         assertEquals(Boolean.FALSE, i.run("!true\n"));
         assertEquals(Boolean.FALSE, i.run("!1\n"));
     }
 
     public void testINT() throws Exception {
         Interpreter i = new Interpreter();
         assertEquals(new Integer(10), i.run("10\n"));
     }
 
     public void testSTRING() throws Exception {
         Interpreter i = new Interpreter();
         assertEquals("hoge", i.run("\"hoge\"\n"));
         assertEquals("hoge", i.run("'hoge'\n"));
     }
 
     public void testBOOL() throws Exception {
         Interpreter i = new Interpreter();
         assertEquals(Boolean.TRUE, i.run("true\n"));
         assertEquals(Boolean.FALSE, i.run("false\n"));
     }
 
     public void testNULL() throws Exception {
         Interpreter i = new Interpreter();
         assertNull(i.run("null\n"));
     }
 
     public void testPARENTHESES() throws Exception {
         Interpreter i = new Interpreter();
         assertEquals(Boolean.TRUE, i.run("(true)\n"));
     }
 
 }
