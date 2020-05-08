 /* ====================================================================
  * The Apache Software License, Version 1.1
  *
  * Copyright (c) 2000 The Apache Software Foundation.  All rights
  * reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  * 1. Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer.
  *
  * 2. Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in
  *    the documentation and/or other materials provided with the
  *    distribution.
  *
  * 3. The end-user documentation included with the redistribution,
  *    if any, must include the following acknowledgment:
  *       "This product includes software developed by the
  *        Apache Software Foundation (http://www.apache.org/)."
  *    Alternately, this acknowledgment may appear in the software itself,
  *    if and wherever such third-party acknowledgments normally appear.
  *
  * 4. The names "Apache" and "Apache Software Foundation" must
  *    not be used to endorse or promote products derived from this
  *    software without prior written permission. For written
  *    permission, please contact apache@apache.org.
  *
  * 5. Products derived from this software may not be called "Apache",
  *    nor may "Apache" appear in their name, without prior written
  *    permission of the Apache Software Foundation.
  *
  * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
  * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
  * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
  * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
  * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
  * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
  * SUCH DAMAGE.
  * ====================================================================
  *
  * This software consists of voluntary contributions made by many
  * individuals on behalf of the Apache Software Foundation.  For more
  * information on the Apache Software Foundation, please see
  * <http://www.apache.org/>.
  *
  * Portions of this software are based upon public domain software
  * originally written at the National Center for Supercomputing Applications,
  * University of Illinois, Urbana-Champaign.
  *
  * Copyright 2007 Sun Microsystems, Inc. 
  */
 package com.sun.mc.softphone.sip;
 
 import java.io.IOException;
 import java.text.*;
 import java.util.*;
 import javax.sip.*;
 import javax.sip.address.*;
 import javax.sip.header.*;
 import javax.sip.message.*;
 import com.sun.mc.softphone.common.*;
 import com.sun.mc.softphone.sip.security.SipSecurityException;
 
 import com.sun.voip.Logger;
 
 /**
  * <p>Title: SIP COMMUNICATOR-1.1</p>
  * <p>Description: JAIN-SIP-1.1 Audio/Video Phone Application</p>
  * <p>Copyright: Copyright (c) 2003</p>
  * <p>Organisation: LSIIT Laboratory (http://lsiit.u-strasbg.fr)</p>
  * <p>Network Research Team (http://www-r2.u-strasbg.fr))</p>
  * <p>Louis Pasteur University - Strasbourg - France</p>
  * @author Emil Ivov
  * @version 1.1
  */
 class RegisterProcessing
 {
     private static final Console console =
         Console.getConsole(RegisterProcessing.class);
     private SipManager sipManCallback = null;
     private Request registerRequest = null;
     private boolean isRegistered = false;
 
     private Timer reRegisterTimer = new Timer();
 
     RegisterProcessing(SipManager sipManCallback)
     {
         this.sipManCallback = sipManCallback;
     }
 
     void setSipManagerCallBack(SipManager sipManCallback)
     {
         this.sipManCallback = sipManCallback;
     }
 
     void processOK(ClientTransaction clientTransaction, Response response)
     {
         try {
             console.logEntry();
             isRegistered = true;
 	    if (Logger.logLevel >= Logger.LOG_MOREINFO) {
 	        Logger.println("Softphone is registered with registrar");
 	    }
             FromHeader fromHeader =
                 ( (FromHeader) response.getHeader(FromHeader.NAME));
             Address address = fromHeader.getAddress();
             
             // XXX for VPN, the server may suggest a different IP address
             // we should use this other IP address if it works XXX
             Header altHeader = response.getHeader("AlternateAddress");
             if (altHeader != null) {
                 SipURI uri = (SipURI) address.getURI();
                 try {
                     SipFactory sf = SipFactory.getInstance();
                     AddressFactory af = sf.createAddressFactory();
                     String val = ((ExtensionHeader) altHeader).getValue();
                     
                     // remove "<" and ">"
                     if (val.startsWith("<")) {
                         val = val.substring(1);
                     }
                     if (val.endsWith(">")) {
                         val = val.substring(0, val.length() - 1);
 
                     }
                                 
                     Logger.println("Value is: " + val);                    
                     uri = (SipURI) af.createURI(val);
                     
                     Logger.println("Using SipURI: " + uri);
                 } catch (PeerUnavailableException ex) {
                     Logger.println("Error creating address factory: " + ex);
                 } catch (ParseException ex) {
                     Logger.println("Error parsing Sip URI: " + ex);
                 }
                 
                 address.setURI(uri);
                 
                 Logger.println("Using alternate Address: " 
 		    + altHeader.toString());
             }
             
             ExpiresHeader expires = response.getExpires();
             //expires may be null
             //fix by Luca Bincoletto <Luca.Bincoletto@tilab.com>
             if (expires != null && expires.getExpires() == 0) {
                 sipManCallback.fireUnregistered(address.toString());
             } else {
                 sipManCallback.fireRegistered((SipURI) address.getURI());
             }
 	    //
 	    // Tell Meeting Central our address
 	    // XXX
         }
         finally {
             console.logExit();
         }
     }
 
     void processTimeout(Transaction transaction, Request request)
     {
         try {
             console.logEntry();
             isRegistered = true;
             FromHeader fromHeader =
                 ( (FromHeader) request.getHeader(FromHeader.NAME));
             Address address = fromHeader.getAddress();
             sipManCallback.fireUnregistered("Request timeout for: " +
                                             address.toString());

	    Console.showErrorUI("Softphone Error", 
		"Unable to register.  The softphone will not work!", 
		"The softphone was unable to register with the server.\n"
		+ "This could be a network problem such as being unable\n"
		+ "to send and receive UDP packets through a firewall,\n"
		+ "or perhaps the server is not running.\n"
		+ "What follows is the SIP REGISTER request.\n\n\n" + request);
         }
         finally {
             console.logExit();
         }
 
     }
 
     void processNotImplemented(ClientTransaction transaction, Response response)
     {
         try {
             console.logEntry();
             isRegistered = true;
             FromHeader fromHeader =
                 ( (FromHeader) response.getHeader(FromHeader.NAME));
             Address address = fromHeader.getAddress();
             sipManCallback.fireUnregistered("Server returned NOT_IMPLEMENTED. "
                                             + address.toString());
         }
         finally {
             console.logExit();
         }
 
     }
 
     /**
      * Attempts to re-ogenerate the corresponding request with the proper
      * credentials and terminates the call if it fails.
      *
      * @param clientTransaction the corresponding transaction
      * @param response the challenge
      */
     void processAuthenticationChallenge(ClientTransaction clientTransaction,
                                         Response response)
     {
         try {
             console.logEntry();
 
             Request register = clientTransaction.getRequest();
 
             Request reoriginatedRequest = null;
 
             ClientTransaction retryTran = 
 		sipManCallback.sipSecurityManager.handleChallenge(response,
                     clientTransaction.getBranchId(), register);
 
             //Dialog dialog = clientTransaction.getDialog();
 
             retryTran.sendRequest();
 
         }
         catch (SipSecurityException exc) {
             sipManCallback.fireCommunicationsError(
                 new CommunicationsException("Authorization failed!", exc));
         }
         catch (Exception exc) {
             sipManCallback.fireCommunicationsError(
                 new CommunicationsException("Failed to resend a request "
                                             + "after a security challenge!",
                                             exc)
                 );
         }
         finally {
             //tell the others we couldn't register
             Request register = clientTransaction.getRequest();
 
             sipManCallback.
                 fireUnregistered(
                     ( (FromHeader) register.getHeader(FromHeader.NAME)).
                         getAddress().toString());
             console.logExit();
         }
     }
 
 
     synchronized void register(String registrarAddress, int registrarPort,
                   String registrarTransport, int expires) throws
         CommunicationsException
     {
 	if (Logger.logLevel >= Logger.LOG_MOREINFO) {
 	    Logger.println("Registering with " + registrarAddress + ":"
 	        + registrarPort);
 	}
 
         try
         {
             console.logEntry();
 
             //From
             FromHeader fromHeader = sipManCallback.getFromHeader();
             Address fromAddress = fromHeader.getAddress();
             sipManCallback.fireRegistering(fromAddress.toString());
             //Request URI
             SipURI requestURI = null;
             try {
                 requestURI = sipManCallback.addressFactory.createSipURI(null,
                     registrarAddress);
             }
             catch (ParseException ex) {
                 console.error("Bad registrar address:" + registrarAddress, ex);
                 throw new CommunicationsException(
                     "Bad registrar address:"
                     + registrarAddress,
                     ex);
             }
             catch (NullPointerException ex) {
             //Do not throw an exc, we should rather silently notify the user
             //	throw new CommunicationsException(
             //		"A registrar address was not specified!", ex);
                 sipManCallback.fireUnregistered(fromAddress.getURI().toString() +
                                                 " (registrar not specified)");
                 return;
             }
             requestURI.setPort(registrarPort);
             try {
                 requestURI.setTransportParam(registrarTransport);
             }
             catch (ParseException ex) {
                 console.error(registrarTransport
                               + " is not a valid transport!", ex);
                 throw new CommunicationsException(
                     registrarTransport + " is not a valid transport!", ex);
             }
             //Call ID Header
             CallIdHeader callIdHeader = sipManCallback.sipProvider.getNewCallId();
             //CSeq Header
             CSeqHeader cSeqHeader = null;
             try {
                 cSeqHeader = sipManCallback.headerFactory.createCSeqHeader(1,
                     Request.REGISTER);
             }
             catch (ParseException ex) {
                 //Should never happen
                 console.error("Corrupt Sip Stack");
                 Console.showError("Corrupt Sip Stack");
             }
             catch (InvalidArgumentException ex) {
                 //Should never happen
                 console.error("The application is corrupt");
                 Console.showError("The application is corrupt!");
             }
             //To Header
             ToHeader toHeader = null;
             try {
                 String proxyWorkAround =
                     Utils.getProperty("com.sun.mc.softphone.REGISTRAR_WORKAROUND");
 
                 if (proxyWorkAround != null &&
                                 proxyWorkAround.toUpperCase().equals("TRUE")) {
 
                     SipURI toURI = (SipURI)(requestURI.clone());
                     toURI.setUser(Utils.getProperty("user.name"));
 
                     toHeader = sipManCallback.headerFactory.createToHeader(sipManCallback.addressFactory.createAddress(toURI), null);
                 } else {
                     toHeader = sipManCallback.headerFactory.createToHeader(fromAddress, null);
                 }
             }
             catch (ParseException ex) {
                 console.error("Could not create a To header for address:"
                               + fromHeader.getAddress(),
                               ex);
                 //throw was missing - reported by Eero Vaarnas
                 throw new CommunicationsException("Could not create a To header "
                                             + "for address:"
                                             + fromHeader.getAddress(),
                                             ex);
             }
             //Via Headers
             ArrayList viaHeaders = sipManCallback.getLocalViaHeaders();
             //MaxForwardsHeader
             MaxForwardsHeader maxForwardsHeader = sipManCallback.
                 getMaxForwardsHeader();
             //Request
             Request request = null;
             try {
                 request = sipManCallback.messageFactory.createRequest(requestURI,
                     Request.REGISTER,
                     callIdHeader,
                     cSeqHeader, fromHeader, toHeader,
                     viaHeaders,
                     maxForwardsHeader);
             }
             catch (ParseException ex) {
                 console.error("Could not create the register request!", ex);
                 //throw was missing - reported by Eero Vaarnas
                 throw new CommunicationsException(
                     "Could not create the register request!",
                     ex);
             }
             //Expires Header
             ExpiresHeader expHeader = null;
             for (int retry = 0; retry < 2; retry++) {
                 try {
                     expHeader = sipManCallback.headerFactory.createExpiresHeader(
                         expires);
                 }
                 catch (InvalidArgumentException ex) {
                     if (retry == 0) {
                         expires = 3600;
                         continue;
                     }
                     console.error(
                         "Invalid registrations expiration parameter - "
                         + expires,
                         ex);
                     throw new CommunicationsException(
                         "Invalid registrations expiration parameter - "
                         + expires,
                         ex);
                 }
             }
             request.addHeader(expHeader);
             //Contact Header should contain IP - bug report - Eero Vaarnas
             ContactHeader contactHeader = sipManCallback.
                 getRegistrationContactHeader();
             request.addHeader(contactHeader);
             //Transaction
             ClientTransaction regTrans = null;
             try {
                 regTrans = sipManCallback.sipProvider.getNewClientTransaction(
                     request);
             }
             catch (TransactionUnavailableException ex) {
                 console.error("Could not create a register transaction!\n"
                               + "Check that the Registrar address is correct!",
                               ex);
                 //throw was missing - reported by Eero Vaarnas
                 throw new CommunicationsException(
                     "Could not create a register transaction!\n"
                     + "Check that the Registrar address is correct!");
             }
             try {
                 regTrans.sendRequest();
                 if( console.isDebugEnabled() )
                     console.debug("sent request= " + request);
                 //[issue 2] Schedule re registrations
                 //bug reported by LynlvL@netscape.com
                 scheduleReRegistration( registrarAddress, registrarPort,
                             registrarTransport, expires);
 
             }
             //we sometimes get a null pointer exception here so catch them all
             catch (Exception ex) {
                 console.error("Could not send out the register request!", ex);
                 //throw was missing - reported by Eero Vaarnas
                 throw new CommunicationsException(
                     "Could not send out the register request!", ex);
             }
             this.registerRequest = request;
         }
         finally
         {
             console.logExit();
         }
 
     }
 
     /**
      * Synchronize because of timer tasks
      * @throws CommunicationsException
      */
     synchronized void unregister() throws CommunicationsException
     {
         try
         {
             console.logEntry();
 
             if (!isRegistered) {
                 return;
             }
 
             cancelPendingRegistrations();
             isRegistered = false;
 
             Request registerRequest = getRegisterRequest();
             if (this.registerRequest == null) {
                 console.error("Couldn't find the initial register request");
                 throw new CommunicationsException(
                     "Couldn't find the initial register request");
             }
             Request unregisterRequest = (Request) registerRequest.clone();
             try {
                 unregisterRequest.getExpires().setExpires(0);
                 CSeqHeader cSeqHeader =
                     (CSeqHeader)unregisterRequest.getHeader(CSeqHeader.NAME);
                 //[issue 1] - increment registration cseq number
                 //reported by - Roberto Tealdi <roby.tea@tin.it>
                 cSeqHeader.setSequenceNumber(cSeqHeader.getSequenceNumber()+1);
 
             }
             catch (InvalidArgumentException ex) {
                 console.error("Unable to set Expires Header", ex);
                 //Shouldn't happen
                 throw new CommunicationsException("Unable to set Expires Header",
                                                   ex);
             }
             ClientTransaction unregisterTransaction = null;
             try {
                 unregisterTransaction =
                     sipManCallback.sipProvider.getNewClientTransaction(
                     unregisterRequest);
             }
             catch (TransactionUnavailableException ex) {
                 console.error("Unable to create a unregister transaction", ex);
                 throw new CommunicationsException(
                     "Unable to create a unregister transaction", ex);
             }
             try {
                 unregisterTransaction.sendRequest();
                 if( console.isDebugEnabled() )
                     console.debug("sent request: " + unregisterRequest);
             	sipManCallback.fireUnregistering(sipManCallback.currentlyUsedURI);
             }
             catch (SipException ex) {
                 console.error("Faied to send unregister request", ex);
                 throw new CommunicationsException(
                     "Faied to send unregister request", ex);
             }
         }
         finally
         {
             console.logExit();
         }
 
     }
 
     boolean isRegistered()
     {
         return isRegistered;
     }
 
     private Request getRegisterRequest()
     {
         return registerRequest;
     }
 
     private class ReRegisterTask
         extends TimerTask
     {
         String registrarAddress = null;
         int registrarPort = -1;
         String transport = null;
         int expires = 0;
         public ReRegisterTask(String registrarAddress, int registrarPort,
                               String registrarTransport, int expires)
         {
             this.registrarAddress = registrarAddress;
             this.registrarPort = registrarPort;
 
             //don't do this.transport = transport  ;)
             //bug report and fix by Willem Romijn (romijn at lucent.com)
             this.transport = registrarTransport;
             this.expires = expires;
         }
 
         public void run()
         {
             try {
                 console.logEntry();
 
                 try {
                     if (isRegistered()) {
                         register(registrarAddress, registrarPort, transport,
                              expires);
 		    }
                 }
                 catch (CommunicationsException ex) {
                     console.error("Failed to reRegister", ex);
                     sipManCallback.fireCommunicationsError(
                         new CommunicationsException("Failed to reRegister", ex)
                         );
                 }
             }
             finally {
                 console.logExit();
             }
 
         }
     }
 
     private void cancelPendingRegistrations()
     {
         try {
             console.logEntry();
 
             reRegisterTimer.cancel();
             reRegisterTimer = null;
 
             reRegisterTimer = new Timer();
         }
         finally {
             console.logExit();
         }
     }
 
     private void scheduleReRegistration(String registrarAddress,
                                         int registrarPort,
                                         String registrarTransport, int expires)
     {
         try
         {
             console.logEntry();
 
             ReRegisterTask reRegisterTask = new ReRegisterTask(
                          registrarAddress, registrarPort,
                          registrarTransport, expires);
 
             //java.util.Timer thinks in miliseconds
             //bug report and fix by Willem Romijn (romijn at lucent.com)
             reRegisterTimer.schedule(reRegisterTask, expires*1000);
         }
         finally
         {
             console.logExit();
         }
     }
 
 }
