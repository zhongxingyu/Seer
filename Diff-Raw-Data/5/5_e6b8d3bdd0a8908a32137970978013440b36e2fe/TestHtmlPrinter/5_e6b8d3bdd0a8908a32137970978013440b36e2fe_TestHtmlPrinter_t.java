 package com.acmetelecom;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import java.io.ByteArrayOutputStream;
 import java.io.PrintStream;
 import java.io.UnsupportedEncodingException;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import sun.reflect.generics.tree.ByteSignature;
 
 
 public class TestHtmlPrinter {
 
 	@Before
 	public void setUp() throws Exception {
 	}
 
 	@After
 	public void tearDown() throws Exception {
 	}
 
 	@Test
 	public void testPrintHeading() {
         ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
 		HtmlPrinter htmlPrinter = new HtmlPrinter(new PrintStream(byteOutputStream));
 		htmlPrinter.printHeading("Mondialu", "8989", "50$");
 		System.out.println();
 
         String actual = getOutput(byteOutputStream);
 
         String expected = "<html>\n<head></head>\n<body>\n<h1>\nAcme Telecom\n</h1>\n" +
                             "<h2>Mondialu/8989 - Price Plan: 50$</h2>\n" +
                             "<table border=\"1\">\n<tr><th width=\"160\">Time</th>" +
                             "<th width=\"160\">Number</th><th width=\"160\">Duration</th>" +
                             "<th width=\"160\">Cost</th></tr>\n";
 
 		assertEquals(expected, actual);
 	}
 
 	@Test
 	public void testPrintItem() {
 		ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
 		HtmlPrinter htmlPrinter = new HtmlPrinter(new PrintStream(byteOutputStream));
		htmlPrinter.printItem("09:11:55", new PhoneEntity("666"), "3:07", "$0.23");
 		System.out.println(byteOutputStream.toString());
 
         String actual = getOutput(byteOutputStream);
 
        String expected = "<tr><td>09:11:55</td><td>666</td><td>3:07</td><td>$0.23</td></tr>\n";
 
         assertEquals(expected, actual);
 	}
 
 	@Test
 	public void testPrintTotal() {
 		ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
 		HtmlPrinter htmlPrinter = new HtmlPrinter(new PrintStream(byteOutputStream));
         htmlPrinter.printTotal("$23.52");
 		System.out.println(byteOutputStream.toString());
 
         String actual = getOutput(byteOutputStream);
 
         String expected = "</table>\n" +
                           "<h2>Total: $23.52</h2>\n" +
                           "</body>\n" +
                           "</html>\n";
 
         assertEquals(expected, actual);
 	}
 
     private String getOutput(ByteArrayOutputStream byteOutputStream) {
         return byteOutputStream.toString().replace("\r",""); // Ensuring that test passes on Windows
     }
 
 }
