 package de.skuzzle.polly.test.core.parser;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 
 import de.skuzzle.polly.core.parser.PushbackReader;
 
 
 public class PushbackReaderTest {
     
     private PushbackReader obtain(String input) {
         return new PushbackReader(new InputStreamReader(
             new ByteArrayInputStream(input.getBytes())));
     }
     
     
 
     @Test
     public void testReader1() throws IOException {
         final String input = "abc";
         final PushbackReader reader = this.obtain(input);
         
         Assert.assertEquals(0, reader.getPosition());
         Assert.assertEquals('a', reader.read());
         
         Assert.assertEquals(1, reader.getPosition());
         Assert.assertEquals('b', reader.read());
         
         Assert.assertEquals(2, reader.getPosition());
         Assert.assertEquals('c', reader.read());
         
         Assert.assertEquals(3, reader.getPosition());
         Assert.assertEquals(-1, reader.read());
         Assert.assertTrue(reader.eos());
         
         Assert.assertEquals(-1, reader.read());
         Assert.assertEquals(3, reader.getPosition());
         Assert.assertTrue(reader.eos());
     }
     
     
     
     @Test
     public void testReader2() throws IOException {
         final String input = "abc";
         final PushbackReader reader = this.obtain(input);
         
         Assert.assertEquals('a', reader.read());
         Assert.assertEquals(1, reader.getPosition());
         
         reader.pushback('a');
         Assert.assertEquals(0, reader.getPosition());
         Assert.assertEquals('a', reader.read());
         Assert.assertEquals(1, reader.getPosition());
 
         Assert.assertEquals('b', reader.read());
         Assert.assertEquals('c', reader.read());
         Assert.assertEquals(-1, reader.read());
         Assert.assertEquals(3, reader.getPosition());
     }
     
     
     
     @Test
     public void testReader3() throws IOException {
         final String input = "abc";
         final PushbackReader reader = this.obtain(input);
         
         reader.pushbackArtificial('d');
         Assert.assertEquals(0, reader.getPosition());
         Assert.assertEquals('d', reader.read());
         Assert.assertEquals(0, reader.getPosition());
         
         Assert.assertEquals('a', reader.read());
         Assert.assertEquals(1, reader.getPosition());
 
         Assert.assertEquals('b', reader.read());
         Assert.assertEquals(2, reader.getPosition());
     }
     
     
     
     @Test
     public void testReader4() throws IOException {
         final String input = "abc";
         final PushbackReader reader = this.obtain(input);
         
         reader.pushbackArtificial('d');
         reader.pushbackArtificial('e');
         Assert.assertEquals(0, reader.getPosition());
         Assert.assertEquals('d', reader.read());
         Assert.assertEquals(0, reader.getPosition());
         Assert.assertEquals('e', reader.read());
         Assert.assertEquals(0, reader.getPosition());
         
         Assert.assertEquals('a', reader.read());
         Assert.assertEquals(1, reader.getPosition());
 
         Assert.assertEquals('b', reader.read());
         Assert.assertEquals(2, reader.getPosition());
     }
     
     
     
     @Test
     public void testReader5() throws IOException {
         final String input = "abc";
         final PushbackReader reader = this.obtain(input);
         
         Assert.assertEquals('a', reader.read());
         Assert.assertEquals('b', reader.read());
         Assert.assertEquals('c', reader.read());
         Assert.assertEquals(-1, reader.read());
         Assert.assertTrue(reader.eos());
         reader.pushbackArtificial('d');
         Assert.assertFalse(reader.eos());
         Assert.assertEquals(3, reader.getPosition());
     }
     
     
     
     @Test
     public void testReader8() throws IOException {
         final String input = "a";
         final PushbackReader reader = this.obtain(input);
         reader.read();
         reader.read();
         Assert.assertTrue(reader.eos());
         
         reader.pushbackArtificial('a');
         Assert.assertFalse(reader.eos());
         Assert.assertEquals('a', reader.read());
         Assert.assertEquals(-1, reader.read());
         Assert.assertTrue(reader.eos());
     }
     
     
     
     @Test
     public void testReader9() throws IOException {
         final String input = "a";
         final PushbackReader reader = this.obtain(input);
         reader.pushbackArtificial('b');
         reader.read();
         reader.read();
         reader.read();
         Assert.assertTrue(reader.eos());
     }
     
     
     
     @Test
     public void testReader10() throws IOException {
         final String input = "";
         final PushbackReader reader = this.obtain(input);
         Assert.assertEquals(-1, reader.read());
         Assert.assertTrue(reader.eos());
     }
     
     
     
     @Test
     public void testReader11() throws IOException {
         final String input = "";
         final PushbackReader reader = this.obtain(input);
         reader.pushbackArtificial('a');
         Assert.assertEquals('a', reader.read());
         Assert.assertEquals(-1, reader.read());
         Assert.assertTrue(reader.eos());
     }
 }
