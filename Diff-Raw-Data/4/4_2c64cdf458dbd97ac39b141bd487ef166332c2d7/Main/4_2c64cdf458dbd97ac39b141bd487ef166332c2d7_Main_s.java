 /*
  * Copyright 2012 Roland Gisler, GISLER iNFORMATiK, Switzerland.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package ch.gitik.ecos;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.Socket;
 import java.net.UnknownHostException;
 
 /**
  * Test-Main Klasse.
  * @author Roland Gisler
  */
 public final class Main {
 
    private static final int PORT = 15471;
 
    /**
     * Konstruktur.
     */
    private Main() {
    }
 
    /**
     * Test-Main.
     * @param args
     *           String Array mit Parametern.
     * @throws IOException IOExcption.
     */
    public static void main(final String[] args) throws IOException {
 
       Socket echoSocket = null;
       PrintWriter out = null;
       BufferedReader in = null;
 
       try {
         echoSocket = new Socket("192.168.0.120", PORT);
          out = new PrintWriter(echoSocket.getOutputStream(), true);
          in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
          out.println("request(1, view)");
          out.flush();
          out.println("get(1, info)");
          out.flush();
          out.println("queryObjects(10, name, addr)");
          out.flush();
          out.println("queryObjects(11, name1, name2, name3, addr)");
          out.flush();
       } catch (UnknownHostException e) {
          System.err.println("Don't know about host.");
          System.exit(1);
       } catch (IOException e) {
          System.err.println("Couldn't get I/O for " + "the connection to host.");
          System.exit(1);
       }
 
       while (true) {
          System.out.println(in.readLine());
       }
    }
 }
