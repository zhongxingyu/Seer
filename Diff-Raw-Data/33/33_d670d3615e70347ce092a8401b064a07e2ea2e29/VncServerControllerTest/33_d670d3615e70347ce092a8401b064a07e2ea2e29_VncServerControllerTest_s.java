 /*
  * Copyright (C) 2013 Pauli Kauppinen
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 package org.javnce.vnc.server;
 
 import java.net.InetSocketAddress;
 import java.nio.channels.SocketChannel;
 import org.javnce.eventing.EventLoop;
 import org.junit.After;
 import static org.junit.Assert.*;
 import org.junit.Before;
 import org.junit.Test;
 
 public class VncServerControllerTest {
 
     @Before
     public void setUp() throws Exception {
         EventLoop.shutdownAll();
     }
 
     @After
     public void tearDown() throws Exception {
         assertFalse(EventLoop.exists());
     }
 
     @Test
     public void testClient() throws Throwable {
         VncServerController controller = new VncServerController(false);
 
         TestRemoteClientObserver tester = new TestRemoteClientObserver();
         controller.addObserver(tester);
         controller.launch();
         int port = tester.port();
 
         try (SocketChannel ch = SocketChannel.open(new InetSocketAddress(port))) {
             {
                 RemoteClient client = tester.client();
                 assertEquals(RemoteClient.State.PendingConnection, client.state());
                 client.connect();
             }
             {
                 RemoteClient client = tester.client();
                 assertEquals(RemoteClient.State.Connected, client.state());
                 client.disconnect();
             }
             {
                 RemoteClient client = tester.client();
                 assertEquals(RemoteClient.State.Disconnected, client.state());
             }
         }
         controller.removeObserver(tester);
         controller.shutdown();
     }
 
     @Test
     public void testShutdown() throws Throwable {
         VncServerController controller = new VncServerController(false);
         TestRemoteClientObserver tester = new TestRemoteClientObserver();
         controller.addObserver(tester);
         controller.launch();
         int port = tester.port();
 
         try (SocketChannel ch = SocketChannel.open(new InetSocketAddress(port))) {
             RemoteClient client = tester.client();
             assertEquals(RemoteClient.State.PendingConnection, client.state());
             client.connect();
             controller.removeObserver(tester);
            controller.shutdown();
         }
     }
 }
