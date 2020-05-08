 /*-
  * Copyright (c) 2010, Derek Konigsberg
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  * 1. Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer. 
  * 2. Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in the
  *    documentation and/or other materials provided with the distribution. 
  * 3. Neither the name of the project nor the names of its
  *    contributors may be used to endorse or promote products derived
  *    from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
  * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
  * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
  * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
  * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
  * OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package org.logicprobe.LogicMail.util;
 
 import net.rim.device.api.util.Arrays;
 
 import j2meunit.framework.Test;
 import j2meunit.framework.TestCase;
 import j2meunit.framework.TestMethod;
 import j2meunit.framework.TestSuite;
 
 public class ConnectionTest extends TestCase {
     
     public ConnectionTest() {
     }
     
     public ConnectionTest(String testName, TestMethod testMethod) {
         super(testName, testMethod);
     }
     
     public void setUp() {
     }
     
     public void tearDown() {
     }
 
     public void testCreateConnection() throws Throwable {
         StubSocketConnection socket = new StubSocketConnection();
         Connection connection = new Connection(socket);
         
         assertEquals(socket.getLocalAddress(), connection.getLocalAddress());
         assertTrue(connection.isConnected());
         assertEquals(0, connection.getBytesSent());
         assertEquals(0, connection.getBytesReceived());
     }
     
     public void testCloseConnection() throws Throwable {
         StubSocketConnection socket = new StubSocketConnection();
         Connection connection = new Connection(socket);
         connection.close();
         
         assertTrue(!connection.isConnected());
         assertTrue(socket.isClosed());
     }
     
     public void testSendCommand() throws Throwable {
         StubSocketConnection socket = new StubSocketConnection();
         Connection connection = new Connection(socket);
         int expectedSent = 0;
         
         byte[] expected = "TheCommand\r\n".getBytes();
         connection.sendCommand("TheCommand");
         assertTrue("Normal command", Arrays.equals(expected, socket.getSentBytes()));
         socket.resetSentBytes();
         expectedSent += expected.length;
         
         expected = "\r\n".getBytes();
         connection.sendCommand("");
         assertTrue("Empty command", Arrays.equals(expected, socket.getSentBytes()));
         socket.resetSentBytes();
         expectedSent += expected.length;
         
         expected = "\r\n".getBytes();
         connection.sendCommand(null);
         assertTrue("Null command", Arrays.equals(expected, socket.getSentBytes()));
         socket.resetSentBytes();
         expectedSent += expected.length;
         
         assertEquals("Bytes sent", expectedSent, connection.getBytesSent());
         
         connection.close();
     }
     
     public void testSendRaw() throws Throwable {
         StubSocketConnection socket = new StubSocketConnection();
         Connection connection = new Connection(socket);
         int expectedSent = 0;
         
         byte[] expected = new byte[0];
         connection.sendRaw("");
         assertTrue("Empty send", Arrays.equals(expected, socket.getSentBytes()));
         socket.resetSentBytes();
         expectedSent += expected.length;
         
         expected = "QWERTY".getBytes();
         connection.sendRaw("QWERTY");
         assertTrue("Normal send", Arrays.equals(expected, socket.getSentBytes()));
         socket.resetSentBytes();
         expectedSent += expected.length;
         
         expected = "1234567890".getBytes();
         connection.sendRaw("1234");
         connection.sendRaw("56");
         connection.sendRaw("789");
         connection.sendRaw("0");
         assertTrue("Multiple sends", Arrays.equals(expected, socket.getSentBytes()));
         socket.resetSentBytes();
         expectedSent += expected.length;
         
         assertEquals(expectedSent, connection.getBytesSent());
         
         connection.close();
     }
     
     public void testReceiveLine() throws Throwable {
         Connection connection = new Connection(new StubSocketConnection("Hello\r\n".getBytes()));
         byte[] expected = "Hello".getBytes();
         byte[] actual = connection.receive();
         assertTrue("Single line", Arrays.equals(expected, actual));
         connection.close();
         
         connection = new Connection(new StubSocketConnection("Hello\n".getBytes()));
         expected = "Hello".getBytes();
         actual = connection.receive();
         assertTrue("Single line LF", Arrays.equals(expected, actual));
         connection.close();        
         
         connection = new Connection(new StubSocketConnection("Hello\r\nWorld\r\n".getBytes()));
         expected = "Hello".getBytes();
         actual = connection.receive();
         assertTrue("Multiple lines", Arrays.equals(expected, actual));
         expected = "World".getBytes();
         actual = connection.receive();
         assertTrue("Multiple lines", Arrays.equals(expected, actual));
         connection.close();
         
         connection = new Connection(new StubSocketConnection("Hello\nWorld\n".getBytes()));
         expected = "Hello".getBytes();
         actual = connection.receive();
         assertTrue("Multiple lines LF", Arrays.equals(expected, actual));
         expected = "World".getBytes();
         actual = connection.receive();
         assertTrue("Multiple lines LF", Arrays.equals(expected, actual));
         connection.close();
     }
     
     public void testReceiveCustomDelimiter() throws Throwable {
         ConnectionResponseTester responseTester = new ConnectionResponseTester() {
             public int checkForCompleteResponse(byte[] buf, int len) {
                 int delimiterIndex = Arrays.getIndex(buf, (byte)'.');
                 if(delimiterIndex != -1) { delimiterIndex++; }
                 return delimiterIndex;
             }
             public int trimCount() {
                 return 1;
             }
         };
         
         Connection connection = new Connection(new StubSocketConnection("Hello.".getBytes()));
         byte[] expected = "Hello".getBytes();
         byte[] actual = connection.receive(responseTester);
         assertTrue("Single line", Arrays.equals(expected, actual));
         connection.close();
         
         connection = new Connection(new StubSocketConnection("Hello.World.".getBytes()));
         expected = "Hello".getBytes();
         actual = connection.receive(responseTester);
         assertTrue("Multiple lines", Arrays.equals(expected, actual));
         expected = "World".getBytes();
         actual = connection.receive(responseTester);
         assertTrue("Multiple lines", Arrays.equals(expected, actual));
         connection.close();
     }
     
     public Test suite() {
         TestSuite suite = new TestSuite("ConnectionTest");
         
         suite.addTest(new ConnectionTest("createConnection", new TestMethod()
         { public void run(TestCase tc) throws Throwable {((ConnectionTest)tc).testCreateConnection(); } }));
         suite.addTest(new ConnectionTest("closeConnection", new TestMethod()
         { public void run(TestCase tc) throws Throwable {((ConnectionTest)tc).testCloseConnection(); } }));
         suite.addTest(new ConnectionTest("sendCommand", new TestMethod()
         { public void run(TestCase tc) throws Throwable {((ConnectionTest)tc).testSendCommand(); } }));
         suite.addTest(new ConnectionTest("sendRaw", new TestMethod()
         { public void run(TestCase tc) throws Throwable {((ConnectionTest)tc).testSendRaw(); } }));
         suite.addTest(new ConnectionTest("receiveLine", new TestMethod()
         { public void run(TestCase tc) throws Throwable {((ConnectionTest)tc).testReceiveLine(); } }));
         suite.addTest(new ConnectionTest("receiveCustomDelimiter", new TestMethod()
         { public void run(TestCase tc) throws Throwable {((ConnectionTest)tc).testReceiveCustomDelimiter(); } }));
         
         return suite;
     }
 }
