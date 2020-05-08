 /**
  * This file is part of GoldenGate Project (named also GoldenGate or GG).
  * 
  * Copyright 2009, Frederic Bregier, and individual contributors by the @author
  * tags. See the COPYRIGHT.txt in the distribution for a full listing of
  * individual contributors.
  * 
  * All GoldenGate Project is free software: you can redistribute it and/or
  * modify it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or (at your
  * option) any later version.
  * 
  * GoldenGate is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
  * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * GoldenGate . If not, see <http://www.gnu.org/licenses/>.
  */
 package goldengate.snmp;
 
 import goldengate.common.logging.GgSlf4JLoggerFactory;
 import goldengate.snmp.GgPrivateMib.NotificationElements;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.jboss.netty.logging.InternalLoggerFactory;
 import org.snmp4j.event.ResponseEvent;
 import org.snmp4j.event.ResponseListener;
 import org.snmp4j.mp.SnmpConstants;
 
 import ch.qos.logback.classic.Level;
 
 /**
  * Test class for Agent and simple Client
  * 
  * @author Frederic Bregier
  * 
  */
 public class GgTestSnmpClientAgent {
     static GgSnmpAgent agent;
     static GgSimpleSnmpClient client;
     static GgImplPrivateMib test;
 
     public static void main(String[] args) throws Exception {
         InternalLoggerFactory.setDefaultFactory(new GgSlf4JLoggerFactory(
                 Level.ALL));
         setUp(args[0]);
         System.out.println("Test SysDescr");
         verifySysDescr();
         System.out.println("Test Table");
         verifyTableContents();
         Thread.sleep(10000);
         sendNotification();
        Thread.sleep(300000);
         System.out.println("Stopping");
         tearDown();
     }
 
     public static void setUp(String file) throws Exception {
         // Setup the client to use our newly started agent
         client = new GgSimpleSnmpClient("udp:127.0.0.1/2001", 1162);
         // Create a monitor
         GgPrivateMonitor monitor = new GgPrivateMonitor();
         // Create a Mib
         test = new GgImplPrivateMib("GoldenGate Test SNMP", 6666, 66666, 66, "F. Bregier",
                 "GoldenGate Test SNMP V1.0", "Paris, France", 72);
         // Create the agent associated with the monitor and Mib
         agent = new GgSnmpAgent(new File(file), monitor, test);
         agent.start();
     }
 
     public static void tearDown() throws Exception {
         agent.stop();
         client.stop();
     }
 
     private static boolean assertEquals(String a, String b) {
         if (!a.equals(b)) {
             System.err.println("Not Equals! " + a + " and " + b);
             return false;
         }
         System.out.println("Equal: " + a);
         return true;
     }
 
     /**
      * Simply verifies that we get the same sysDescr as we have registered in
      * our agent
      */
     public static void verifySysDescr() throws IOException {
         assertEquals(test.textualSysDecr, client.getAsString(SnmpConstants.sysDescr));
     }
 
     /**
      * Verify that the table contents is ok.
      */
     public static void verifyTableContents() {
         for (GgMOScalar scalar : test.rowInfo.row) {
             try {
                 System.out.println("Read "+scalar.getID()+":"+client.getAsString(scalar.getID()));
             } catch (IOException e) {
                 System.err.println(scalar.getID()+":"+e.getMessage());
                 continue;
             }
         }
     }
     public static void sendNotification() {
         test.notify(NotificationElements.TrapWarning, "Une alerte", "une seconde alerte", 1971);
     }
     static class StringResponseListener implements ResponseListener {
 
         private String value = null;
 
         @Override
         public void onResponse(ResponseEvent event) {
             System.out.println(event.getResponse());
             if (event.getResponse() != null) {
                 value = GgSimpleSnmpClient.extractSingleString(event);
             }
         }
 
         public String getValue() {
             System.out.println(value);
             return value;
         }
 
     }
 
 }
