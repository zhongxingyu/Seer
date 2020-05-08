 /*
  *   Licensed to the Apache Software Foundation (ASF) under one
  *   or more contributor license agreements.  See the NOTICE file
  *   distributed with this work for additional information
  *   regarding copyright ownership.  The ASF licenses this file
  *   to you under the Apache License, Version 2.0 (the
  *   "License"); you may not use this file except in compliance
  *   with the License.  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  *   Unless required by applicable law or agreed to in writing,
  *   software distributed under the License is distributed on an
  *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  *   KIND, either express or implied.  See the License for the
  *   specific language governing permissions and limitations
  *   under the License.
  *
  */
 package pl.org.olo.krbldap.apacheds.handlers.extended;
 
 import java.net.SocketAddress;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.directory.server.kerberos.protocol.KerberosProtocolHandler;
 import org.apache.directory.server.ldap.ExtendedOperationHandler;
 import org.apache.directory.server.ldap.LdapServer;
 import org.apache.directory.server.ldap.LdapSession;
 import org.apache.directory.shared.kerberos.components.PaData;
 import org.apache.directory.shared.kerberos.messages.AsReq;
 import org.apache.directory.shared.kerberos.messages.KerberosMessage;
 import org.apache.directory.shared.kerberos.messages.KrbError;
 import org.apache.directory.shared.ldap.model.exception.LdapProtocolErrorException;
 import org.apache.directory.shared.ldap.model.message.LdapResult;
 import org.apache.directory.shared.ldap.model.message.ResultCodeEnum;
 import org.apache.mina.core.future.DefaultWriteFuture;
 import org.apache.mina.core.future.WriteFuture;
 import org.apache.mina.core.session.DummySession;
 import org.apache.mina.core.session.IoSession;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import pl.org.olo.krbldap.apacheds.extras.extended.KrbLdapRequest;
 import pl.org.olo.krbldap.apacheds.extras.extended.KrbLdapResponse;
 import sun.security.krb5.KrbException;
 
 /**
  * Handler for the KrbLDAP Authentication Service request (Kerberos' AS-REQ).
  *
  * @author <a href="mailto:aleksander.adamowski@gmail.com">Aleksander Adamowski</a>
  * @see <a href="http://www.ietf.org/rfc/rfc1510.txt">RFC 1510</a>
  */
 public class KrbLdapAuthServiceHandler implements ExtendedOperationHandler<KrbLdapRequest, KrbLdapResponse> {
     // ------------------------------ FIELDS ------------------------------
 
     private static final Set<String> EXTENSION_OIDS;
     private static final Logger LOG = LoggerFactory.getLogger(KrbLdapAuthServiceHandler.class);
 
     protected LdapServer ldapServer;
 
     private KerberosProtocolHandler kerberosProtocolHandler;
 
     // -------------------------- STATIC METHODS --------------------------
 
     static {
         Set<String> set = new HashSet<String>(3);
         set.add(KrbLdapRequest.EXTENSION_OID);
         set.add(KrbLdapResponse.EXTENSION_OID);
         EXTENSION_OIDS = Collections.unmodifiableSet(set);
     }
 
     // --------------------------- CONSTRUCTORS ---------------------------
 
     public KrbLdapAuthServiceHandler() {
 
     }
 
     // --------------------- GETTER / SETTER METHODS ---------------------
 
     public KerberosProtocolHandler getKerberosProtocolHandler() {
         return kerberosProtocolHandler;
     }
 
     public void setKerberosProtocolHandler(KerberosProtocolHandler kerberosProtocolHandler) {
         this.kerberosProtocolHandler = kerberosProtocolHandler;
     }
 
     public void setLdapServer(LdapServer ldapServer) {
         LOG.debug("Setting LDAP Service");
         this.ldapServer = ldapServer;
     }
 
     // ------------------------ INTERFACE METHODS ------------------------
 
 
     // --------------------- Interface ExtendedOperationHandler ---------------------
 
     public String getOid() {
         return KrbLdapResponse.EXTENSION_OID;
     }
 
     public Set<String> getExtensionOids() {
         return EXTENSION_OIDS;
     }
 
     public void handleExtendedOperation(LdapSession session, KrbLdapRequest req) throws Exception {
         LOG.info("Handling KrbLdap AS request.");
         final KerberosMessage kerberosMessage = req.getKerberosMessage();
         if (LOG.isDebugEnabled()) {
             LOG.debug("LdapSession: [" + session.toString() + "]");
             LOG.debug("ExtendedRequest: [" + req.toString() + "]");
             LOG.debug("KerberosMessage contained in ExtendedRequest: " + kerberosMessage);
             LOG.debug("KerberosMessage class: " + kerberosMessage.getClass().getName());
             LOG.debug("ldapServer available: " + this.ldapServer);
         }
         // Clean up zero-type padata entries from the list.
         // TODO: investigate where do zero-type padata entries come from.
         if (kerberosMessage instanceof AsReq) {
             LOG.debug("PaData list contents:");
             final List<PaData> paDataList = ((AsReq) kerberosMessage).getPaData();
             final Iterator<PaData> iterator = paDataList.iterator();
             while (iterator.hasNext()) {
                 PaData paData = iterator.next();
                 LOG.debug("  PaData type value: " + paData.getPaDataType().getValue());
                 LOG.debug("  PaData value: " + paData.getPaDataValue());
                 if (paData.getPaDataType().getValue() == 0) {
                     LOG.warn("    Zero-type PaData found: " + paData);
                     // Workaround for http://mailman.mit.edu/pipermail/krbdev/2012-January/010640.html :
                     LOG.warn("    removing.");
                     iterator.remove();
                 }
             }
         }
         /** TODO: perform message processing similar in behaviour to
          * {@link org.apache.directory.server.kerberos.protocol.KerberosProtocolHandler#messageReceived}
          */
         final IoSession ldapIoSession = session.getIoSession();
         final KrbLdapAuthServiceHandlerIoSession handlerSession = new KrbLdapAuthServiceHandlerIoSession();
         handlerSession.setRemoteAddress(ldapIoSession.getRemoteAddress());
         KerberosMessage kerberosReply = null;
         kerberosProtocolHandler.messageReceived(handlerSession, kerberosMessage);
         kerberosReply = handlerSession.getKerberosMessage();
         if (kerberosReply == null) {
             LOG.warn("kerberosReply in " + KrbLdapAuthServiceHandlerIoSession.class.getName() +
                     " is null, which means that messageReceived didn't set any reply.");
         }
 
         final KrbLdapResponse resultResponse = req.getResultResponse();
 
         if (resultResponse == null) {
             final String message = "Request has no resultResponse!";
             LOG.error(message + " request is: " + req.toString());
             throw new LdapProtocolErrorException(message);
         }
 
         resultResponse.setKerberosReply(kerberosReply);
         final LdapResult ldapResult = resultResponse.getLdapResult();
 
         if (ldapResult == null) {
             final String message = "Response has no ldapResult!";
             LOG.error(message + " response is: " + resultResponse.toString());
             throw new LdapProtocolErrorException(message);
         }
 
         if (resultResponse.getKerberosReply() instanceof KrbError) {
             final ResultCodeEnum resultCode = ResultCodeEnum.PROTOCOL_ERROR;
             LOG.warn("Kerberos reply is a KrbError, setting LDAP result code: " + resultCode);
             ldapResult.setResultCode(resultCode);
            ldapResult.setDiagnosticMessage(((KrbError) resultResponse.getKerberosReply()).getEText());
         } else {
             ldapResult.setResultCode(ResultCodeEnum.SUCCESS);
         }
        resultResponse.setMessageId(req.getMessageId());
         LOG.debug("Setting Kerberos Reply: " + resultResponse.getKerberosReply());
         ldapIoSession.write(resultResponse);
         LOG.debug("Wrote to Ldap IO Session the following response: " + resultResponse.toString());
     }
 
     // -------------------------- INNER CLASSES --------------------------
 
     private class KrbLdapAuthServiceHandlerIoSession extends DummySession {
         // ------------------------------ FIELDS ------------------------------
 
         private SocketAddress remoteAddress;
 
         private KerberosMessage kerberosMessage = null;
 
         // --------------------- GETTER / SETTER METHODS ---------------------
 
         public KerberosMessage getKerberosMessage() {
             return kerberosMessage;
         }
 
         public void setKerberosMessage(KerberosMessage kerberosMessage) {
             this.kerberosMessage = kerberosMessage;
         }
 
         @Override
         public SocketAddress getRemoteAddress() {
             return this.remoteAddress;
         }
 
         public void setRemoteAddress(SocketAddress remoteAddress) {
             this.remoteAddress = remoteAddress;
         }
 
         // ------------------------ INTERFACE METHODS ------------------------
 
 
         // --------------------- Interface IoSession ---------------------
 
         @Override
         public WriteFuture write(Object message) {
             final DefaultWriteFuture writeFuture;
             LOG.debug("message received in session: " + message);
             if (message instanceof KerberosMessage) {
                 writeFuture = (DefaultWriteFuture) DefaultWriteFuture.newWrittenFuture(this);
                 writeFuture.setValue(message);
                 setKerberosMessage((KerberosMessage) message);
             } else {
                 final KrbException krbException =
                         new KrbException("Message written to " + KrbLdapAuthServiceHandlerIoSession.class.getName() +
                                 " is not of class " + KerberosMessage.class.getName() + " but instead of class " +
                                 message.getClass().getName());
                 LOG.error(krbException.getMessage(), krbException);
                 writeFuture = (DefaultWriteFuture) DefaultWriteFuture.newNotWrittenFuture(this, krbException);
             }
             LOG.debug("Kerberos message stored in session: " + getKerberosMessage());
 
             return writeFuture;
         }
     }
 }
