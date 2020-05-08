 /*
  * Copyright (c) 2013 Jean Niklas L'orange. All rights reserved.
  *
  * The use and distribution terms for this software are covered by the
  * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
  * which can be found in the file LICENSE at the root of this distribution.
  *
  * By using this software in any fashion, you are agreeing to be bound by
  * the terms of this license.
  *
  * You must not remove this notice, or any other, from this software.
  */
 
 package com.hypirion.io;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.Reader;
 import java.io.Writer;
 import java.io.StringWriter;
 import java.io.StringReader;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang3.RandomStringUtils;
 
 import com.hypirion.io.ClosingPipe;
 
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 public class ClosingPipeTest {
 
     /**
      * Tests that a ClosingPipe closes an OutputStream after the InputStream is
      * properly consumed.
      */
     @Test(timeout=1000)
     public void testBasicStreamClosingCapabilities() throws Exception {
         String input = RandomStringUtils.random(4023);
         InputStream in = IOUtils.toInputStream(input, "UTF-8");
         ByteArrayOutputStream out = new ByteArrayOutputStream();
         CloseCheckingOutputStream wrapper = new CloseCheckingOutputStream(out);
         Pipe p = new ClosingPipe(in, wrapper);
         p.start();
         p.join();
         in.close();
         String output = out.toString("UTF-8");
         assertEquals(input, output);
        assertTrue(wrapper.isClosed);
     }
 
     /**
      * Tests that a ClosingPipe closes a Writer after the Reader is properly
      * consumed.
      */
     @Test(timeout=1000)
     public void testBasicWriteClosingCapabilities() throws Exception {
         String input = RandomStringUtils.random(4023);
         Reader in = new StringReader(input);
         StringWriter out = new StringWriter();
         CloseCheckingReader wrapper = new CloseCheckingReader(out);
         Pipe p = new ClosingPipe(in, wrapper);
         p.start();
         p.join();
         in.close();
         String output = out.toString();
         assertEquals(input, output);
        assertTrue(wrapper.isClosed);
     }
 
     public static class CloseCheckingOutputStream extends OutputStream {
         volatile boolean isClosed;
         final OutputStream out;
 
         public CloseCheckingOutputStream(OutputStream out) {
             this.out = out;
         }
 
         synchronized public void write(int b) throws IOException {
             out.write(b);
         }
 
         synchronized public void close() throws IOException {
             out.close();
             isClosed = true;
         }
     }
 
     public static class CloseCheckingReader extends Writer {
         volatile boolean isClosed;
         final Writer out;
 
         public CloseCheckingReader(Writer out) {
             this.out = out;
         }
 
         synchronized public void close() throws IOException {
             out.close();
             isClosed = true;
         }
 
         synchronized public void flush() throws IOException {
             out.flush();
         }
 
         synchronized public void write(char[] cbuf, int off, int len)
             throws IOException {
             out.write(cbuf, off, len);
         }
     }
 }
