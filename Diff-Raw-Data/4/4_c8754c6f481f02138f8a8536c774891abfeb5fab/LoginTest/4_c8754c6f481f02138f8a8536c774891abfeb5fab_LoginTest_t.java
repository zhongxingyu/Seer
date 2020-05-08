 /**
  * $RCSfile$
  * $Revision$
  * $Date$
  *
  * Copyright 2003-2007 Jive Software.
  *
  * All rights reserved. Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.jivesoftware.smack;
 
 import org.jivesoftware.smack.test.SmackTestCase;
 import org.jivesoftware.smack.util.StringUtils;
 
 /**
  * Includes set of login tests. 
  *
  * @author Gaston Dombiak
  */
 public class LoginTest extends SmackTestCase {
 
     public LoginTest(String arg0) {
         super(arg0);
     }
 
     /**
      * Check that the server is returning the correct error when trying to login using an invalid
      * (i.e. non-existent) user.
      */
     public void testInvalidLogin() throws Exception {
         XMPPConnection connection = createConnection();
         connection.connect();
         try {
             // Login with an invalid user
             connection.login("invaliduser" , "invalidpass");
             connection.disconnect();
             fail("Invalid user was able to log into the server");
         }
         catch (XMPPException e) {
             assertNotNull("XMPPError isn't set", e.getXMPPError());
 
             assertEquals("Incorrect error code while login with an invalid user", 401,
                     e.getXMPPError().getCode());
         }
     }
 
     /**
      * Check that the server handles anonymous users correctly.
      */
     // XXX: This test should not fall back on non-SASL login.
     public void testSASLAnonymousLogin() throws Exception {
         XMPPConnection conn1 = createConnection();
         XMPPConnection conn2 = createConnection();
         conn1.connect();
         conn2.connect();
         try {
             // Try to login anonymously
             conn1.loginAnonymously();
             conn2.loginAnonymously();
 
             assertNotNull("Resource is null", StringUtils.parseResource(conn1.getUser()));
             assertNotNull("Resource is null", StringUtils.parseResource(conn2.getUser()));
 
             assertNotNull("Username is null", StringUtils.parseName(conn1.getUser()));
             assertNotNull("Username is null", StringUtils.parseName(conn2.getUser()));
         }
         finally {
             // Close the connection
             conn1.disconnect();
             conn2.disconnect();
         }
     }
 
     /**
      * Check that the server handles anonymous users correctly.
      */
     public void testNonSASLAnonymousLogin() throws Exception {
         ConnectionConfiguration config = new ConnectionConfiguration(getHost(), getPort());
         config.setSASLAuthenticationEnabled(false);
         XMPPConnection conn1 = new XMPPConnection(config);
         conn1.connect();
 
         config = new ConnectionConfiguration(getHost(), getPort());
         config.setSASLAuthenticationEnabled(false);
         XMPPConnection conn2 = new XMPPConnection(config);
         conn2.connect();
 
         try {
             // Try to login anonymously
             conn1.loginAnonymously();
             conn2.loginAnonymously();
 
             assertNotNull("Resource is null", StringUtils.parseResource(conn1.getUser()));
             assertNotNull("Resource is null", StringUtils.parseResource(conn2.getUser()));
 
             assertNotNull("Username is null", StringUtils.parseName(conn1.getUser()));
             assertNotNull("Username is null", StringUtils.parseName(conn2.getUser()));
         }
         finally {
             // Close the connection
             conn1.disconnect();
             conn2.disconnect();
         }
     }
 
     /**
      * Check server-assigned resources.
      */
     public void testLoginWithNoResource() throws Exception {
         XMPPConnection conn = createConnection();
         conn.connect();
         try {
             conn.getAccountManager().createAccount("user_1", "user_1");
         } catch (XMPPException e) {
             // Do nothing if the account already exists.
             if (!e.getXMPPError().getCondition().equals("conflict")) {
                 throw e;
             }
         }
         conn.login("user_1", "user_1", (String) null);
         if (!conn.isAuthenticated())
             fail("User with no resource was unable to log into the server");
 
         // Check that the server assigned a resource.
         assertNotNull("JID assigned by server is missing", conn.getUser());
        assertTrue("JID assigned by server does not have a resource",
                StringUtils.parseResource(conn.getUser()).length() > 0);
         conn.disconnect();
     }
 
     protected int getMaxConnections() {
         return 0;
     }
 }
