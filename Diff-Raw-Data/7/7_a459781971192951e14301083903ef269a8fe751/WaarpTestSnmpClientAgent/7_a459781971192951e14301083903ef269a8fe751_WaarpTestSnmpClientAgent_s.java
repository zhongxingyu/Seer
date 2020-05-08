 /**
  * This file is part of Waarp Project.
  * 
  * Copyright 2009, Frederic Bregier, and individual contributors by the @author
  * tags. See the COPYRIGHT.txt in the distribution for a full listing of
  * individual contributors.
  * 
  * All Waarp Project is free software: you can redistribute it and/or
  * modify it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or (at your
  * option) any later version.
  * 
  * Waarp is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
  * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * Waarp. If not, see <http://www.gnu.org/licenses/>.
  */
 package org.waarp.snmp.test;
 
 
 import java.io.File;
 import java.io.IOException;
 
 import org.jboss.netty.logging.InternalLoggerFactory;
 import org.snmp4j.event.ResponseEvent;
 import org.snmp4j.event.ResponseListener;
 import org.snmp4j.mp.SnmpConstants;
 import org.waarp.common.logging.WaarpSlf4JLoggerFactory;
 import org.waarp.snmp.WaarpMOFactory;
 import org.waarp.snmp.WaarpSnmpAgent;
 import org.waarp.snmp.utils.WaarpMOScalar;
 
 import ch.qos.logback.classic.Level;
 
 /**
  * Test class for Agent and simple Client
  * 
  * @author Frederic Bregier
  * 
  */
 public class WaarpTestSnmpClientAgent {
     static WaarpSnmpAgent agent;
 
     static WaarpSimpleSnmpClient client;
 
     static WaarpImplPrivateMib test;
 
     public static void main(String[] args) throws Exception {
         InternalLoggerFactory.setDefaultFactory(new WaarpSlf4JLoggerFactory(
                 Level.ALL));
         setUp(args[0]);
         System.out.println("Test SysDescr");
         verifySysDescr();
         System.out.println("Test Table");
         verifyTableContents();
         Thread.sleep(10000);
         sendNotification();
         Thread.sleep(30000);
         System.out.println("Stopping");
         tearDown();
     }
 
     public static void setUp(String file) throws Exception {
         // Setup the client to use our newly started agent
         client = new WaarpSimpleSnmpClient("udp:127.0.0.1/2001", 1162);
         // Create a monitor
         WaarpPrivateMonitor monitor = new WaarpPrivateMonitor();
         // Create a Mib
        test = new WaarpImplPrivateMib("GoldenGate Test SNMP", 6666, 66666, 66,
                "F. Bregier", "GoldenGate Test SNMP V1.0", "Paris, France", 72);
         // Set the default VariableFactory
         WaarpMOFactory.factory = new WaarpTestVariableFactory();
         // Create the agent associated with the monitor and Mib
         agent = new WaarpSnmpAgent(new File(file), monitor, test);
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
         assertEquals(test.textualSysDecr,
                 client.getAsString(SnmpConstants.sysDescr));
     }
 
     /**
      * Verify that the table contents is ok.
      */
     public static void verifyTableContents() {
         for (WaarpMOScalar scalar: test.rowInfo.row) {
             try {
                 System.out.println("Read " + scalar.getID() + ":" +
                         client.getAsString(scalar.getID()));
             } catch (IOException e) {
                 System.err.println(scalar.getID() + ":" + e.getMessage());
                 continue;
             }
         }
     }
 
     public static void sendNotification() {
         test.notifyInfo("Une alerte", "un autre texte d'alerte", 1971);
        test.notifyError("Une seconde alerte", "un second texte d'alerte", 42);
     }
 
     static class StringResponseListener implements ResponseListener {
 
         private String value = null;
 
         @Override
         public void onResponse(ResponseEvent event) {
             System.out.println(event.getResponse());
             if (event.getResponse() != null) {
                 value = WaarpSimpleSnmpClient.extractSingleString(event);
             }
         }
 
         public String getValue() {
             System.out.println(value);
             return value;
         }
 
     }
 
 }
