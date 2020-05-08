 /*
  * The MIT License
  *
  * Copyright 2013 Jason Unger <entityreborn@gmail.com>.
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
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package me.entityreborn.socbot.core;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.InterruptedIOException;
 
 /**
  *
  * @author Jason Unger <entityreborn@gmail.com>
  */
 public class InputThread extends Thread {
 
     private BufferedReader in;
     private Engine engine;
 
     public InputThread(InputStream i, Engine c) {
         in = new BufferedReader(new InputStreamReader(i));
         engine = c;
     }
 
     @Override
     public void run() {
         while (engine.isConnected() && !Thread.interrupted()) {
             String line = null;
 
             // Try to read the next line from the server.
             try {
                 line = in.readLine();
             } catch (InterruptedIOException e) {
                 // Sometimes happens if we've been idle. Double check we're
                 // still connected.
                 engine.sendPing();
                 
                 continue;
            } catch (IOException e) {
                 // Die quietly.
             }
             
             if (line == null) {
                 break;
             }
 
             try {
                 engine.handleLine(line);
             } catch (Throwable e) {
                 engine.handleException(e);
             }
         }
 
         if (!Thread.interrupted()) {
             engine.disconnect();
         }
     }
 }
