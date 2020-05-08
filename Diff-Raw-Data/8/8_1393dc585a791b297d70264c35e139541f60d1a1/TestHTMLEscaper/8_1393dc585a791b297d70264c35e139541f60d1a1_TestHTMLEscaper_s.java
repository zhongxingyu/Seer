 package org.webmacro.util;
 
 import junit.framework.TestCase;
 
 public class TestHTMLEscaper extends TestCase
 {
     public TestHTMLEscaper (String name)
     {
         super(name);
     }
 
 
     protected void setUp ()
     {
     }
 
     public void testEscaping () throws Exception
     {
         assertEscape("<B>", "&lt;B&gt;");
         assertEscape("\u00f6", "&ouml;");
         assertEscape("\u00a9", "&copy;");
         assertEscape("\u00a3", "&pound;");
         assertEscape("\u0080", "&euro;");
         assertEscape("This is a test: \u0080\u0080\u0080 is better than" +
                 "\u00a3\u00a3\u00a3!",
                 "This is a test: &euro;&euro;&euro; is better than" +
                 "&pound;&pound;&pound;!" );
     }
 
     public void testEscapingNull() throws Exception
     {
         assertTrue( "Escaping null didn't return null!", HTMLEscaper.escape( null) == null);
     }
 
     private void assertEscape( String text, String expected )
     {
         String res = HTMLEscaper.escape(text);
         if (!res.equals(expected))
         {
             String s = "Escaped /" + text + "/ and expected /" + expected + "/ but got /" + res+"/";
             assertTrue( s, false);
         }
     }
 
 }
 
 
