package veeju.tests.parser;
 
 import org.junit.*;
 import static org.junit.Assert.*;
 import veeju.parser.*;
 
 public final class InvalidSyntaxExceptionTest {
    protected InvalidSyntaxException e, e2;
 
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
        e2 = new InvalidSyntaxException("asdf(", 5);
     }
 
     @Test
     public void testGetText() {
         assertEquals("test\nabc := .def\ntest", e.getText());
        assertEquals("asdf(", e2.getText());
     }
 
     @Test
     public void testGetOffset() {
         assertEquals(12, e.getOffset());
        assertEquals(5, e2.getOffset());
     }
 
     @Test
     public void testGetLine() {
         assertEquals(1, e.getLine());
        assertEquals(0, e2.getLine());
     }
 
     @Test
     public void testGetColumn() {
         assertEquals(7, e.getColumn());
        assertEquals(4, e2.getColumn());
     }
 
     @Test
     public void testGetLineString() {
         assertEquals("abc := .def", e.getLineString());
        assertEquals("asdf(", e2.getLineString());
     }
 }
 
