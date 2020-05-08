 /**
  * $RCSfile$
  * $Revision: $
  * $Date: $
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
 
 import org.jivesoftware.smack.filter.PacketFilter;
 import org.jivesoftware.smack.filter.PacketIDFilter;
 import org.jivesoftware.smack.filter.ReceivedPacketFilter;
 import org.jivesoftware.smack.packet.Bind;
 import org.jivesoftware.smack.packet.IQ;
 import org.jivesoftware.smack.packet.Packet;
 import org.jivesoftware.smack.packet.ReceivedPacket;
 import org.jivesoftware.smack.packet.Session;
 import org.jivesoftware.smack.packet.XMPPError;
 import org.jivesoftware.smack.sasl.*;
 import org.jivesoftware.smack.util.Base64;
 import org.jivesoftware.smack.util.PacketParserUtils;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 
 import org.apache.harmony.javax.security.auth.callback.Callback;
 import org.apache.harmony.javax.security.auth.callback.CallbackHandler;
 import org.apache.harmony.javax.security.auth.callback.NameCallback;
 import org.apache.harmony.javax.security.auth.callback.PasswordCallback;
 import org.apache.harmony.javax.security.auth.callback.UnsupportedCallbackException;
 import org.apache.harmony.javax.security.sasl.RealmCallback;
 import org.apache.harmony.javax.security.sasl.RealmChoiceCallback;
 
 import java.io.IOException;
 import java.util.*;
 
 /**
  * <p>This class is responsible authenticating the user using SASL, binding the resource
  * to the connection and establishing a session with the server.</p>
  *
  * <p>Once TLS has been negotiated (i.e. the connection has been secured) it is possible to
  * register with the server, authenticate using Non-SASL or authenticate using SASL. If the
  * server supports SASL then Smack will first try to authenticate using SASL. But if that
  * fails then Non-SASL will be tried.</p>
  *
  * <p>The server may support many SASL mechanisms to use for authenticating. Out of the box
  * Smack provides several SASL mechanisms, but it is possible to register new SASL Mechanisms. Use
  * {@link #registerSASLMechanism(String, Class)} to register a new mechanisms. A registered
  * mechanism wont be used until {@link #supportSASLMechanism(String, int)} is called. By default,
  * the list of supported SASL mechanisms is determined from the {@link SmackConfiguration}. </p>
  *
  * <p>Once the user has been authenticated with SASL, it is necessary to bind a resource for
  * the connection. If no resource is passed in {@link #authenticate(String, String, String)}
  * then the server will assign a resource for the connection. In case a resource is passed
  * then the server will receive the desired resource but may assign a modified resource for
  * the connection.</p>
  *
  * <p>Once a resource has been binded and if the server supports sessions then Smack will establish
  * a session so that instant messaging and presence functionalities may be used.</p>
  *
  * @see org.jivesoftware.smack.sasl.SASLMechanism
  *
  * @author Gaston Dombiak
  * @author Jay Kline
  */
 public class SASLAuthentication implements UserAuthentication {
 
     private static Map<String, SASLMechanismType.Factory> implementedMechanisms =
         new HashMap<String, SASLMechanismType.Factory>();
     private static List<String> mechanismsPreferences = new ArrayList<String>();
 
     private Connection connection;
     private Collection<String> serverMechanisms = new ArrayList<String>();
     /**
      * Boolean indicating if SASL negotiation has finished and was successful.
      */
     private boolean saslNegotiated = false;
 
     static {
 
         // Register SASL mechanisms supported by Smack
         registerSASLMechanism(new SASLMechanism.Factory("EXTERNAL"));
         registerSASLMechanism(new SASLGSSAPIMechanism.Factory());
         registerSASLMechanism(new SASLMechanism.Factory("DIGEST-MD5"));
         registerSASLMechanism(new SASLMechanism.Factory("CRAM-MD5"));
         registerSASLMechanism(new SASLMechanism.Factory("PLAIN"));
         registerSASLMechanism(new SASLAnonymous.Factory());
 
         supportSASLMechanism("ANONYMOUS", 0);
         supportSASLMechanism("PLAIN", 0);
         supportSASLMechanism("CRAM-MD5", 0);
         supportSASLMechanism("DIGEST-MD5", 0);
         supportSASLMechanism("GSSAPI", 0);
 
     }
 
     /**
      * Registers a new SASL mechanism
      *
      * @param name   common name of the SASL mechanism. E.g.: PLAIN, DIGEST-MD5 or KERBEROS_V4.
      * @param mClass a SASLMechanism subclass.
      */
     public static void registerSASLMechanism(SASLMechanismType.Factory factory) {
         implementedMechanisms.put(factory.getName(), factory);
     }
 
     /**
      * Unregisters an existing SASL mechanism. Once the mechanism has been unregistered it won't
      * be possible to authenticate users using the removed SASL mechanism. It also removes the
      * mechanism from the supported list.
      *
      * @param name common name of the SASL mechanism. E.g.: PLAIN, DIGEST-MD5 or KERBEROS_V4.
      */
     public static void unregisterSASLMechanism(String name) {
         implementedMechanisms.remove(name);
         mechanismsPreferences.remove(name);
     }
 
 
     /**
      * Registers a new SASL mechanism in the specified preference position. The client will try
      * to authenticate using the most prefered SASL mechanism that is also supported by the server.
      * The SASL mechanism must be registered via {@link #registerSASLMechanism(String, Class)}
      *
      * @param name common name of the SASL mechanism. E.g.: PLAIN, DIGEST-MD5 or KERBEROS_V4.
      */
     public static void supportSASLMechanism(String name) {
         mechanismsPreferences.add(0, name);
     }
 
     /**
      * Registers a new SASL mechanism in the specified preference position. The client will try
      * to authenticate using the most prefered SASL mechanism that is also supported by the server.
      * Use the <tt>index</tt> parameter to set the level of preference of the new SASL mechanism.
      * A value of 0 means that the mechanism is the most prefered one. The SASL mechanism must be
      * registered via {@link #registerSASLMechanism(String, Class)}
      *
      * @param name common name of the SASL mechanism. E.g.: PLAIN, DIGEST-MD5 or KERBEROS_V4.
      * @param index preference position amongst all the implemented SASL mechanism. Starts with 0.
      */
     public static void supportSASLMechanism(String name, int index) {
         if(index > mechanismsPreferences.size())
             index = mechanismsPreferences.size();
         mechanismsPreferences.add(index, name);
     }
 
     /**
      * Un-supports an existing SASL mechanism. Once the mechanism has been unregistered it won't
      * be possible to authenticate users using the removed SASL mechanism. Note that the mechanism
      * is still registered, but will just not be used.
      *
      * @param name common name of the SASL mechanism. E.g.: PLAIN, DIGEST-MD5 or KERBEROS_V4.
      */
     public static void unsupportSASLMechanism(String name) {
         mechanismsPreferences.remove(name);
     }
 
     /**
      * Returns the registerd SASLMechanism classes sorted by the level of preference.
      *
      * @return the registerd SASLMechanism classes sorted by the level of preference.
      */
     public static List<SASLMechanismType.Factory> getRegisterSASLMechanisms() {
         List<SASLMechanismType.Factory> answer = new ArrayList<SASLMechanismType.Factory>();
         for (String mechanismsPreference : mechanismsPreferences) {
             answer.add(implementedMechanisms.get(mechanismsPreference));
         }
         return answer;
     }
 
     SASLAuthentication(Connection connection, ReceivedPacket features) {
         super();
         this.connection = connection;
         if(connection == null)
             throw new IllegalArgumentException("connection must not be null");
         if(features == null)
             throw new IllegalArgumentException("features must not be null");
 
         // Record the mechanisms provided in the previous features packet.
         for(Node node: PacketParserUtils.getChildNodes(features.getElement())) {
             if(node.getLocalName().equals("mechanisms")) {
                 // The server is reporting available SASL mechanisms. Store this information
                 // which will be used later while logging (i.e. authenticating) into
                 // the server
                 serverMechanisms = PacketParserUtils.parseMechanisms(node);
             }
         }
     }
 
     /**
      * Performs SASL authentication of the specified user. If SASL authentication was successful
      * then resource binding and session establishment will be performed. This method will return
      * the full JID provided by the server while binding a resource to the connection.<p>
      *
      * The server may assign a full JID with a username or resource different than the requested
      * by this method.
      *
      * @param username the username that is authenticating with the server.
      * @param password the password to send to the server.
      * @param resource the desired resource.
      * @return the full JID provided by the server while binding a resource to the connection.
      * @throws XMPPException if an error occures while authenticating.
      */
     public String authenticate(String username, String password, String resource)
             throws XMPPException {
         CallbackHandler cbh = new DefaultCallbackHandler(username, password);
         return authenticate(username, resource, cbh);
     }
 
     static class DefaultCallbackHandler implements CallbackHandler {
         final String username;
         final String password;
 
         DefaultCallbackHandler(String username, String password) {
             this.username = username;
             this.password = password;
         }
 
         public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
             for (int i = 0; i < callbacks.length; i++) {
                 if (callbacks[i] instanceof NameCallback) {
                     NameCallback ncb = (NameCallback)callbacks[i];
                     ncb.setName(username);
                 } else if(callbacks[i] instanceof PasswordCallback) {
                     PasswordCallback pcb = (PasswordCallback)callbacks[i];
                     pcb.setPassword(password.toCharArray());
                 } else if(callbacks[i] instanceof RealmCallback) {
                     // Use the default realm provided by the server.
                     RealmCallback rcb = (RealmCallback)callbacks[i];
                     rcb.setText(rcb.getDefaultText());
                 } else if(callbacks[i] instanceof RealmChoiceCallback){
                     //unused
                     //RealmChoiceCallback rccb = (RealmChoiceCallback)callbacks[i];
                 } else {
                    throw new UnsupportedCallbackException(callbacks[i]);
                 }
             }
         }
     };
 
     private String authenticateUsingMechanism(String username, CallbackHandler cbh, String resource,
             SASLMechanismType.Factory mechanismFactory)
             throws XMPPException, SASLMechanismType.MechanismNotSupported
     {
         if (saslNegotiated)
             throw new XMPPException("Already authenticated");
 
         saslNegotiated = false;
 
         // A SASL mechanism was found. Authenticate using the selected mechanism and then
         // proceed to bind a resource
         SASLMechanismType currentMechanism = mechanismFactory.create();
 
         // Trigger SASL authentication with the selected mechanism. We use
         // connection.getHost() since GSAPI requires the FQDN of the server, which
         // may not match the XMPP domain.
         PacketFilter filter = new ReceivedPacketFilter(null, "urn:ietf:params:xml:ns:xmpp-sasl");
         PacketCollector coll = connection.createPacketCollector(filter);
         try {
             /* Start authentication. */
             byte[] authText = currentMechanism.authenticate(username, connection.getServiceName(), cbh);
 
             // Send the initial packet.
             connection.sendPacket(new AuthMechanism(currentMechanism.getName(), authText));
 
             while(true) {
                 Packet packet = coll.nextResult(SmackConfiguration.getPacketReplyTimeout());
                 if(packet == null)
                     throw new XMPPException("SASL authentication timed out", XMPPError.Condition.request_timeout);
                 ReceivedPacket receivedPacket = (ReceivedPacket) packet;
                 Element element = receivedPacket.getElement();
 
                 if(element.getLocalName().equals("success")) {
                     byte[] successData = null;
                    String content = PacketParserUtils.getTextContent(element);
                     if(content != null && content.length() > 0) {
                         if(content.equals("="))
                             successData = new byte[0];
                         else
                             successData = Base64.decode(content);
                     }
 
                     currentMechanism.successReceived(successData);
 
                     saslNegotiated = true;
                     break;
                 }
 
                 if(element.getLocalName().equals("failure")) {
                     String errorCondition = null;
                     Node firstChild = element.getFirstChild();
                     if(firstChild != null)
                         errorCondition = firstChild.getLocalName();
 
                     if (errorCondition != null) {
                         throw new XMPPException("SASL authentication " + currentMechanism.getName() + " failed: " + errorCondition,
                                 XMPPError.fromErrorCondition(errorCondition));
                     }
                     else {
                         throw new XMPPException("SASL authentication " + currentMechanism.getName() + " failed");
                     }
                 }
 
                 if(element.getLocalName().equals("challenge")) {
                     /**
                      * The server is challenging the SASL authentication we just sent. Forward the challenge
                      * to the current SASLMechanism we are using. The SASLMechanism will send a response to
                      * the server. The length of the challenge-response sequence varies according to the
                      * SASLMechanism in use.
                      */
                     // Decode the challenge.
                     byte[] challengeData;
                    if(PacketParserUtils.getTextContent(element) != null)
                        challengeData = Base64.decode(PacketParserUtils.getTextContent(element));
                     else
                         challengeData = new byte[0];
 
                     // Ask the mechanism for the response.
                     byte[] response = currentMechanism.challengeReceived(challengeData);
 
                     // Encode the response.
                     Packet responseStanza;
                     if (response == null)
                         responseStanza = new Response();
                     else
                         responseStanza = new Response(Base64.encodeBytes(response,Base64.DONT_BREAK_LINES));
 
                     // Send the response to the server.
                     connection.sendPacket(responseStanza);
                 }
             }
         } finally {
             connection.removePacketCollector(coll);
         }
 
         PacketCollector featuresCollector = connection.createPacketCollector(
                 new ReceivedPacketFilter("features", "http://etherx.jabber.org/streams"));
 
         boolean foundBind = false;
         boolean sessionSupported = false;
         try {
             // After successful authentication, the stream must be reset.  This will trigger
             // the next <features/>, which will enable binding.
             connection.streamReset();
 
             ReceivedPacket features = (ReceivedPacket) featuresCollector.nextResult(SmackConfiguration.getPacketReplyTimeout());
             if(features == null)
                 throw new XMPPException("Timed out waiting for post-SASL features");
 
             // Ensure that we've received the "bind" feature.
             for(Node node: PacketParserUtils.getChildNodes(features.getElement())) {
                 if(node.getLocalName().equals("bind") &&
                         node.getNamespaceURI().equals("urn:ietf:params:xml:ns:xmpp-bind"))
                     foundBind = true;
                 if(node.getLocalName().equals("session") &&
                         node.getNamespaceURI().equals("urn:ietf:params:xml:ns:xmpp-session"))
                     sessionSupported = true;
             }
 
             if(!foundBind)
                 throw new XMPPException("Authentication successful, but no <bind> feature received");
         } finally {
             featuresCollector.cancel();
         }
 
         // Bind a resource for this connection.
         String JID = bindResource(resource);
 
         // If sessions are supported, establish a session.  XXX: This is obsolete
         // and removed in RFC6121.  See if this can be removed.
         if(sessionSupported)
             establishSession();
 
         return JID;
     }
 
     /**
      * Performs SASL authentication of the specified user. If SASL authentication was successful
      * then resource binding and session establishment will be performed. This method will return
      * the full JID provided by the server while binding a resource to the connection.<p>
      *
      * The server may assign a full JID with a username or resource different than the requested
      * by this method.
      *
      * @param username the username to authenticate, or null to login anonymously
      * @param resource the desired resource.
      * @param cbh the CallbackHandler used to get information from the user
      * @return the full JID provided by the server while binding a resource to the connection.
      * @throws XMPPException if an error occures while authenticating.
      */
     public String authenticate(String username, String resource, CallbackHandler cbh) 
             throws XMPPException
     {
         List<String> mechanismsToUse = mechanismsPreferences;
         if (username == null) {
             mechanismsToUse = new Vector<String>();
             mechanismsToUse.add("ANONYMOUS");
         }
         
         // Try each available SASL mechanism in order of preference until we try one
         // that works, or the server closes the connection.
         XMPPException error = null;
         for (String mechanism: mechanismsToUse) {
             if (!implementedMechanisms.containsKey(mechanism) || !serverMechanisms.contains(mechanism))
                 continue;
 
             SASLMechanismType.Factory factory = implementedMechanisms.get(mechanism);
             try {
                 return authenticateUsingMechanism(username, cbh, resource, factory);
             }
             catch (SASLMechanismType.MechanismNotSupported e) {
                 // The mechanism isn't supported by the local system.  Keep looking.
             }
             catch (XMPPException e) {
                 // The mechanism was supported, but failed.  If it failed due to a timeout,
                 // stop trying and rethrow the exception.
                 XMPPError xmppError = e.getXMPPError();
                 if(xmppError != null && xmppError.getCondition().equals("request-timeout"))
                     throw e;
 
                 // We've found a shared mechanism, and it failed to log in.  Stop looking.
                 // We could keep trying other mechanisms, which the spec allows (but doesn't
                 // require), but unless it's to work around buggy servers there seems to be
                 // no point in doing so.  It would lower security by making us attempt PLAIN
                 // when we don't need to, and it would cause password callbacks to be run
                 // repeatedly.
                 error = e;
                 break;
             }
         }
 
         // If any supported SASL methods were attempted and failed, rethrow the error.
         if(error != null)
             throw error;
 
         throw new XMPPException("No supported SASL methods found");
     }
 
     private String bindResource(String resource) throws XMPPException {
         Bind bindResource = new Bind();
         bindResource.setResource(resource);
 
         PacketCollector<Bind> collector = connection.createPacketCollector(new PacketIDFilter(bindResource), Bind.class);
         connection.sendPacket(bindResource);
         try {
             Bind response = collector.getResult(0);
             response.throwIfError();
             return response.getJid();
         } finally {
             collector.cancel();
         }
     }
 
     private void establishSession() throws XMPPException {
         Session session = new Session();
         PacketCollector<IQ> collector = connection.createPacketCollector(new PacketIDFilter(session), IQ.class);
         connection.sendPacket(session);
         try {
             collector.getResult(0).throwIfError();
         } finally {
             collector.cancel();
         }
     }
 
     /**
      * Returns true if the user was able to authenticate with the server usins SASL.
      *
      * @return true if the user was able to authenticate with the server usins SASL.
      */
     public boolean isAuthenticated() {
         return saslNegotiated;
     }
 
     /**
      * Initiating SASL authentication by select a mechanism.
      */
     public static class AuthMechanism extends Packet {
         final private String name;
         final private String authenticationText;
 
         public AuthMechanism(String name, byte[] initialResponse) {
             if (name == null) {
                 throw new NullPointerException("SASL mechanism name shouldn't be null.");
             }
             this.name = name;
 
             if(initialResponse != null) {
                 if(initialResponse.length == 0)
                     this.authenticationText = "=";
                 else
                     this.authenticationText = Base64.encodeBytes(initialResponse, Base64.DONT_BREAK_LINES);
             }
             else
                 this.authenticationText = null;
         }
 
         public String toXML() {
             StringBuilder stanza = new StringBuilder();
             stanza.append("<auth mechanism=\"").append(name);
             stanza.append("\" xmlns=\"urn:ietf:params:xml:ns:xmpp-sasl\">");
             if (authenticationText != null &&
                     authenticationText.trim().length() > 0) {
                 stanza.append(authenticationText);
             }
             stanza.append("</auth>");
             return stanza.toString();
         }
     }
 
     /**
      * A SASL response stanza.
      */
     public static class Response extends Packet {
         final private String authenticationText;
 
         public Response() {
             authenticationText = null;
         }
 
         public Response(String authenticationText) {
             if (authenticationText == null || authenticationText.trim().length() == 0) {
                 this.authenticationText = null;
             }
             else {
                 this.authenticationText = authenticationText;
             }
         }
 
         public String toXML() {
             StringBuilder stanza = new StringBuilder();
             stanza.append("<response xmlns=\"urn:ietf:params:xml:ns:xmpp-sasl\">");
             if (authenticationText != null) {
                 stanza.append(authenticationText);
             }
             stanza.append("</response>");
             return stanza.toString();
         }
     }
 }
