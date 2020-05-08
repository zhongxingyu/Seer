 /*
  * JBoss, Home of Professional Open Source.
  * Copyright 2009, Red Hat Middleware LLC, and individual contributors
  * as indicated by the @author tags. See the copyright.txt file in the
  * distribution for a full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 
 package org.jboss.stdio;
 
 import java.io.OutputStream;
 import java.io.IOException;
 import java.io.Writer;
 import java.nio.charset.CharsetDecoder;
 import java.nio.charset.Charset;
 import java.nio.charset.CoderResult;
 import java.nio.ByteBuffer;
 import java.nio.CharBuffer;
 import java.nio.charset.CodingErrorAction;
 
 /**
  * An output stream which decodes into a writer.
  */
 public class WriterOutputStream extends OutputStream {
 
     private final Writer writer;
     private final CharsetDecoder decoder;
     private final ByteBuffer inputBuffer;
     private final CharBuffer outputBuffer;
 
     /**
      * Construct a new instance using the default charset.
      *
      * @param writer the writer to write to
      */
     public WriterOutputStream(final Writer writer) {
         this(writer, Charset.defaultCharset());
     }
 
     /**
      * Construct a new instance using the named charset.
      *
      * @param writer the writer to write to
      * @param charsetName the charset name
      */
     public WriterOutputStream(final Writer writer, final String charsetName) {
         this(writer, Charset.forName(charsetName));
     }
 
     /**
      * Construct a new instance using the given charset.
      *
      * @param writer the writer to write to
      * @param charset the charset
      */
     public WriterOutputStream(final Writer writer, final Charset charset) {
         this(writer, charset.newDecoder());
     }
 
     /**
      * Construct a new instance using the given charset decoder.
      *
      * @param writer the writer to write to
      * @param decoder the charset decoder
      */
     public WriterOutputStream(final Writer writer, final CharsetDecoder decoder) {
         this.writer = writer;
         this.decoder = decoder;
         decoder.onMalformedInput(CodingErrorAction.REPLACE);
         decoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
         decoder.replaceWith("?");
         inputBuffer = ByteBuffer.allocate(256);
         outputBuffer = CharBuffer.allocate(256);
     }
 
     /** {@inheritDoc} */
     public void write(final int b) throws IOException {
         synchronized (decoder) {
             final ByteBuffer inputBuffer = this.inputBuffer;

             if (! inputBuffer.hasRemaining()) {
                 finish();
             }
            inputBuffer.put((byte) b);
         }
     }
 
     /** {@inheritDoc} */
     public void write(final byte[] b, int off, int len) throws IOException {
         synchronized (decoder) {
            if (! inputBuffer.hasRemaining()) {
                finish();
            }
             for (;;) {
                 final ByteBuffer inputBuffer = this.inputBuffer;
                 final int rem = inputBuffer.remaining();
                 final int cnt = rem <= len ? rem : len;
                 inputBuffer.put(b, off, cnt);
                 len -= cnt;
                 off += cnt;
                 if (len == 0) {
                     return;
                 }
                 finish();
             }
         }
     }
 
     private void finish() throws IOException {
         final CharBuffer outputBuffer = this.outputBuffer;
         final ByteBuffer inputBuffer = this.inputBuffer;
         inputBuffer.flip();
         try {
             while (inputBuffer.hasRemaining()) {
                 final CoderResult coderResult = decoder.decode(inputBuffer, outputBuffer, false);
                 if (coderResult.isUnderflow() && outputBuffer.position() == 0) {
                     return;
                 }
                 outputBuffer.flip();
                 try {
                     boolean ok = false;
                     try {
                         writer.write(outputBuffer.array(), outputBuffer.arrayOffset(), outputBuffer.remaining());
                         ok = true;
                     } finally {
                         if (! ok) {
                             inputBuffer.clear();
                         }
                     }
                 } finally {
                     outputBuffer.clear();
                 }
             }
         } finally {
             inputBuffer.compact();
         }
     }
 
     public void flush() throws IOException {
         synchronized (decoder) {
             finish();
             writer.flush();
         }
     }
 
     /** {@inheritDoc} */
     public void close() throws IOException {
         flush();
     }
 }
