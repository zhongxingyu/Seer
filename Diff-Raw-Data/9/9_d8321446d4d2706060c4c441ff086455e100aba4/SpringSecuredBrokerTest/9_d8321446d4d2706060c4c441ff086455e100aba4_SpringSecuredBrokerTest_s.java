 /*
  * Copyright 2005-2006 The Apache Software Foundation.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.apache.servicemix.jbi.security;
 
 import java.io.IOException;
 import java.net.URL;
 
 import javax.jbi.messaging.InOnly;
 import javax.security.auth.Subject;
 import javax.security.auth.callback.Callback;
 import javax.security.auth.callback.CallbackHandler;
 import javax.security.auth.callback.NameCallback;
 import javax.security.auth.callback.PasswordCallback;
 import javax.security.auth.callback.UnsupportedCallbackException;
 import javax.security.auth.login.LoginContext;
 import javax.xml.namespace.QName;
 
 import org.apache.servicemix.client.DefaultServiceMixClient;
 import org.apache.servicemix.client.ServiceMixClient;
 import org.apache.servicemix.jbi.jaxp.StringSource;
 import org.apache.servicemix.tck.Receiver;
import org.apache.servicemix.tck.ReceiverComponent;
 import org.apache.servicemix.tck.SpringTestSupport;
 import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
 import org.springframework.context.support.AbstractXmlApplicationContext;
 
 public class SpringSecuredBrokerTest extends SpringTestSupport {
 
     static {
         String path = System.getProperty("java.security.auth.login.config");
         if (path == null) {
             URL resource = PropertiesLoginModuleTest.class.getResource("login.config");
             if (resource != null) {
                path = resource.getFile();
                 System.setProperty("java.security.auth.login.config", path);
             }
         }
        System.out.println("Path to login config: " + path);
     }
 
     protected Receiver receiver1;
     protected Receiver receiver2;
     protected Receiver receiver3;
     protected ServiceMixClient client;
     
     protected void setUp() throws Exception {
         super.setUp();
         receiver1 = (Receiver) jbi.getBean("receiver1");
         receiver2 = (Receiver) jbi.getBean("receiver2");
         receiver3 = (Receiver) jbi.getBean("receiver3");
         client = new DefaultServiceMixClient(jbi);
     }
     
     protected void tearDown() throws Exception {
         super.tearDown();
     }
 
     protected AbstractXmlApplicationContext createBeanFactory() {
         return new ClassPathXmlApplicationContext("org/apache/servicemix/jbi/security/secure.xml");
     }
     
     protected Subject login(final String username, final String password) throws Exception {
         LoginContext context = new LoginContext("servicemix-domain", new CallbackHandler() {
             public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                 for (int i = 0; i < callbacks.length; i++) {
                     if (callbacks[i] instanceof NameCallback) {
                         ((NameCallback) callbacks[i]).setName(username);
                     } else if (callbacks[i] instanceof PasswordCallback) {
                         ((PasswordCallback) callbacks[i]).setPassword(password.toCharArray());
                     } else {
                         throw new UnsupportedCallbackException(callbacks[i]);
                     }
                 }
             }
         });
         context.login();
         return context.getSubject();
     }
     
     protected void send(String username, String password, QName service) throws Exception {
         Subject subject = login(username, password);
         InOnly me = client.createInOnlyExchange();
         me.setService(service);
         me.getInMessage().setSecuritySubject(subject);
         me.getInMessage().setContent(new StringSource("<hello>world</hello>"));
         client.sendSync(me);
     }
     
     public void testAuthorizationsOnReceiver1() throws Exception {
         QName service = new QName("http://servicemix.org/example/1", "receiver1");
         // receiver1 should be available to: programmers, accounting, testers
         send("first", "secret", service);
         send("second", "password", service);
         send("third", "another", service);
     }
     
     public void testAuthorizationsOnReceiver2() throws Exception {
         QName service = new QName("http://servicemix.org/example/1", "receiver2");
         // receiver2 should be available to: programmers, accounting
         send("first", "secret", service);
         send("second", "password", service);
         try {
             send("third", "another", service);
             fail("receiver2 is not available to testers");
         } catch (SecurityException e) {
         }
     }
     
     public void testAuthorizationsOnReceiver3() throws Exception {
         QName service = new QName("http://servicemix.org/example/2", "receiver1");
         // receiver2 should be available to: programmers
         send("first", "secret", service);
         try {
             send("second", "password", service);
             fail("receiver2 is not available to accounting");
         } catch (SecurityException e) {
         }
         try {
             send("third", "another", service);
             fail("receiver2 is not available to testers");
         } catch (SecurityException e) {
         }
     }
     
 }
