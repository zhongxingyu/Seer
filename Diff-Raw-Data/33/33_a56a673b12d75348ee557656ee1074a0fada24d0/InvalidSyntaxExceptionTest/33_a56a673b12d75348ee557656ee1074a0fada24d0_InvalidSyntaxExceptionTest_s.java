package veeju.tests.forms;
 
 import org.junit.*;
 import static org.junit.Assert.*;
 import veeju.parser.*;
 
 public final class InvalidSyntaxExceptionTest {
    protected InvalidSyntaxException e;
 
     @Test(expected = IllegalArgumentException.class)
     public void testConstruct_negativeOffset() {
         new InvalidSyntaxException("code", -5);
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testConstruct_offsetOverflow() {
         new InvalidSyntaxException("code", 5);
     }
 
     @Before
     public void setUp() {
         e = new InvalidSyntaxException("test\nabc := .def\ntest", 12);
     }
 
     @Test
     public void testGetText() {
         assertEquals("test\nabc := .def\ntest", e.getText());
     }
 
     @Test
     public void testGetOffset() {
         assertEquals(12, e.getOffset());
     }
 
     @Test
     public void testGetLine() {
         assertEquals(1, e.getLine());
     }
 
     @Test
     public void testGetColumn() {
         assertEquals(7, e.getColumn());
     }
 
     @Test
     public void testGetLineString() {
         assertEquals("abc := .def", e.getLineString());
     }
 }
 
