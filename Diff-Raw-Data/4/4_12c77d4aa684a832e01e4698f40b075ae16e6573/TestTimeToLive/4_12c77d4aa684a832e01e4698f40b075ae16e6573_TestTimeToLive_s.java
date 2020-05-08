 /*
  * JBoss, Home of Professional Open Source
  * Copyright 2008, Red Hat, Inc., and others contributors as indicated
  * by the @authors tag. All rights reserved.
  * See the copyright.txt in the distribution for a
  * full listing of individual contributors.
  * This copyrighted material is made available to anyone wishing to use,
  * modify, copy, or redistribute it subject to the terms and conditions
  * of the GNU Lesser General Public License, v. 2.1.
  * This program is distributed in the hope that it will be useful, but WITHOUT A
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
  * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
  * You should have received a copy of the GNU Lesser General Public License,
  * v.2.1 along with this distribution; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
  * MA  02110-1301, USA.
  */
 package org.jboss.blacktie.jatmibroker.xatmi;
 
 import java.util.Arrays;
 
 import junit.framework.TestCase;
 
 import org.apache.log4j.LogManager;
 import org.apache.log4j.Logger;
 import org.jboss.blacktie.jatmibroker.RunServer;
 import org.jboss.blacktie.jatmibroker.core.conf.ConfigurationException;
 
 public class TestTimeToLive extends TestCase {
 	private static final Logger log = LogManager.getLogger(TestTimeToLive.class);
 	private RunServer server = new RunServer();
 	private Connection connection;
 
 	public void setUp() throws ConnectionException, ConfigurationException {
 		server.serverinit();
 
 		ConnectionFactory connectionFactory = ConnectionFactory
 				.getConnectionFactory();
 		connection = connectionFactory.getConnection();
 	}
 
 	public void tearDown() throws ConnectionException, ConfigurationException {
 		connection.close();
 		server.serverdone();
 	}
 
 	public void test_call_ttl() {
 		log.info("test_call_ttl");
 		server.tpadvertiseTestTTL();
 
 		try{
 			String toSend = "test_call_ttl_1";
 			int sendlen = toSend.length() + 1;
 			Buffer sendbuf = new Buffer("X_OCTET", null);
 			sendbuf.setData(toSend.getBytes());
 
 			log.info("send first message");
 			Response rcvbuf = connection.tpcall("TTL", sendbuf, sendlen, 0);
 			fail("Expected TPETIME, got a buffer with rval: "
 					+ rcvbuf.getRval());
 		} catch (ConnectionException e) {
 			if (e.getTperrno() != Connection.TPETIME) {
 				fail("Expected TPETIME, got: " + e.getTperrno());
 			}
 		}
 
 		try{
 			String toSend = "test_call_ttl_2";
 			int sendlen = toSend.length() + 1;
 			Buffer sendbuf = new Buffer("X_OCTET", null);
 			sendbuf.setData(toSend.getBytes());
 
 			log.info("send second message");
 			Response rcvbuf = connection.tpcall("TTL", sendbuf, sendlen, 0);
 			fail("Expected TPETIME, got a buffer with rval: "
 					+ rcvbuf.getRval());
 		} catch (ConnectionException e) {
 			if (e.getTperrno() != Connection.TPETIME) {
 				fail("Expected TPETIME, got: " + e.getTperrno());
 			}
 		}
 
 		try {
 			log.info("wait for first message process");
 			Thread.sleep(6 * 1000);
 			String toSend = "counter,TTL,";
 			int sendlen = toSend.length() + 1;
 			Buffer sendbuf = new Buffer("X_OCTET", null);
 			sendbuf.setData(toSend.getBytes());
 
 			Response rcvbuf = connection.tpcall("default_ADMIN_1", sendbuf, sendlen, 0);
 
 			assertTrue(rcvbuf != null);
 			assertTrue(rcvbuf.getBuffer() != null);
 			assertTrue(rcvbuf.getBuffer().getData() != null);
 			byte[] received = rcvbuf.getBuffer().getData();
 
			//assertTrue(received[0] == '1');
			//assertTrue(received[1] == '1');
 			String counter = new String(received, 1, received.length - 1);
 
 			log.info("get message counter of TTL is " + counter);
 		} catch (ConnectionException e) {
 			fail("UnExpected Exception, got: " + e.getTperrno());
 		} catch (Exception e) {
 			fail("Exception, got: " + e);
 		}
 	}
 }
