 /*
  * Copyright (c) 2006-2011 Chris Smith, Shane Mc Cormack, Gregory Holmes
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 
 package com.dmdirc.addons.swingdebug;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PipedInputStream;
 import java.io.PipedOutputStream;
 import java.io.PrintStream;
 
 import javax.swing.text.BadLocationException;
 import javax.swing.text.Document;
 
 /**
  * Simple utility class to redirect System streams to the specified Document.
  */
 public class SystemStreamRedirectThread implements Runnable {
 
     /**
      * Enum to identify System streams.
      */
     public enum Stream {
         /** System.out. */
         OUT,
         /** System.in. */
         IN;
     }
 
     /** Is this thread running? */
     private boolean running = false;
     /** Reader to for the system stream. */
     private final BufferedReader rar;
     /** Stream identifier. */
     private final Stream stream;
     /** Document to output stream into. */
     private final Document document;
 
     /**
      * Constructs a new redirection thread.
      *
      * @param stream System stream to redirect
      * @param document Document to redirect stream into
      *
      * @throws IOException On error redirecting stream
      */
     public SystemStreamRedirectThread(final Stream stream,
             final Document document) throws IOException {
         super();
         this.stream = stream;
         this.document = document;
 
         final PipedInputStream in = new PipedInputStream();
         final PipedOutputStream out = new PipedOutputStream(in);
         rar = new BufferedReader(new InputStreamReader(in));
         switch (stream) {
             case OUT:
                 System.setOut(new PrintStream(out));
                 break;
             case IN:
                 System.setErr(new PrintStream(out));
                 break;
             default:
                 throw new IllegalArgumentException("Unknown stream type: "
                         + stream);
         }
     }
 
     /** {@inheritDoc} */
     @Override
     public void run() {
         running = true;
         while (running) {
             try {
                 if (rar.ready()) {
                     document.insertString(document.getLength(), rar.readLine(),
                             null);
                     document.insertString(document.getLength(), "\n", null);
                 }
             } catch (BadLocationException ex) {
                 running = false;
             } catch (IOException ex) {
                 running = false;
             }
         }
     }
 
     /**
      * Starts the thread adding text to the document.
      */
     public void start() {
         final Thread thread = new Thread(this,
                 "System stream redirector (" + stream + ")");
         thread.setDaemon(true);
         thread.start();
     }
 
     /**
      * Cancels the thread adding text to the document.
      */
     public void cancel() {
         running = false;
     }
 
     /**
      * Is this thread running?
      *
      * @return true iif running
      */
     public boolean isRunning() {
         return running;
     }
 }
