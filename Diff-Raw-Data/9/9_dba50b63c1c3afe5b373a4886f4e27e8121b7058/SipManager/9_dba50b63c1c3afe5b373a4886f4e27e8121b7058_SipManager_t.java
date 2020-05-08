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
 
 import java.net.*;
 import java.text.*;
 import java.util.*;
 import javax.sip.*;
 import javax.sip.address.*;
 import javax.sip.header.*;
 import javax.sip.message.*;
 import com.sun.mc.softphone.SipCommunicator;
 import com.sun.mc.softphone.common.*;
 import com.sun.mc.softphone.media.MediaManager;
 import com.sun.mc.softphone.sip.event.*;
 import com.sun.mc.softphone.sip.security.*;
 
 import java.net.DatagramSocket;
 import java.net.InetSocketAddress;
 import java.net.Socket;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.io.IOException;
 
 import com.sun.stun.NetworkAddressManager;
 
 import com.sun.voip.Logger;
 
 /**
  * The SipManager provides wrapping of the underlying stack's functionalities.
  * It also implements the SipListener interface and handles incoming
  * SIP messages.
  *
  * @author Emil Ivov
  * @version 1.0
  */
 public class SipManager
     implements SipListener
 {
     /**
      * Specifies the number of retries that should be attempted when deleting
      * a sipProvider
      */
     private static final int  RETRY_OBJECT_DELETES       = 10;
     /**
      * Specifies the time to wait before retrying delete of a sipProvider.
      */
     private static final long RETRY_OBJECT_DELETES_AFTER = 500;
 
 
     private static final Console console = Console.getConsole(SipManager.class);
     private static final String DEFAULT_TRANSPORT = "udp";
     //jain-sip objects - package accessibility as they should be
     //available for XxxProcessing classes
     /**
      * The SipFactory instance used to create the SipStack and the Address
      * Message and Header Factories.
      */
     SipFactory sipFactory;
 
     /**
      * The AddressFactory used to create URLs ans Address objects.
      */
     AddressFactory addressFactory;
 
     /**
      * The HeaderFactory used to create SIP message headers.
      */
     HeaderFactory headerFactory;
 
     /**
      * The Message Factory used to create SIP messages.
      */
     MessageFactory messageFactory;
 
     /**
      * The sipStack instance that handles SIP communications.
      */
     SipStack sipStack;
 
     /**
      * The default (and currently the only) SIP listening point of the
      * application.
      */
     ListeningPoint listeningPoint;
 
     /**
      * The JAIN SIP SipProvider instance.
      */
     SipProvider sipProvider;
 
     /**
      * An instance used to provide user credentials
      */
     private SecurityAuthority securityAuthority = null;
 
 
     /**
      * Used for the contact header to provide firewall support.
      */
     private InetSocketAddress publicIsa = null;
 
     private DelayedInviteProcessor delayedInviteProcessor = null;
 
     //properties
     private String sipStackPath = "gov.nist";
 
     protected String currentlyUsedURI = null;
 
     private String displayName = null;
     private String transport = DEFAULT_TRANSPORT;
     private int localPort = -1;
     private int registrationsExpiration = -1;
     private String registrarTransport = null;
 
     //mandatory stack properties
     private String stackAddress = null;
 
     //Prebuilt Message headers
     private FromHeader fromHeader = null;
     private ContactHeader contactHeader = null;
     private ArrayList viaHeaders = null;
     private static final int  MAX_FORWARDS = 70;
     private MaxForwardsHeader maxForwardsHeader = null;
     private long registrationTransaction = -1;
     private ArrayList listeners = new ArrayList();
 
     private String registrarAddress = null;
     private int registrarPort = 5060;
     private boolean registrarIsStunServer;
 
     //XxxProcessing managers
     /**
      * The instance that handles all registration associated activity such as
      * registering, unregistering and keeping track of expirations.
      */
     RegisterProcessing registerProcessing = null;
 
     /**
      * The instance that handles all call associated activity such as
      * establishing, managing, and terminating calls.
      */
     CallProcessing callProcessing = null;
 
     /**
      * The instance that handles incoming/outgoing REFER requests.
      */
     TransferProcessing transferProcessing = null;
 
     /**
      * Authentication manager.
      */
     SipSecurityManager sipSecurityManager = null;
 
     private boolean isStarted = false;
 
     private DatagramSocket socket;
 
     private boolean started;
 
     private SipCommunicator sipCommunicator;
 
     private MediaManager mediaManager;
 
     private static int stackInstance = 0;
 
     private static Object stackNameLock = new Object();
 
     public SipManager(SipCommunicator sipCommunicator, 
 	    MediaManager mediaManager, String registrarAddress) {
 
 	this.sipCommunicator = sipCommunicator;
 	this.mediaManager = mediaManager;
 	this.registrarAddress = registrarAddress;
 
 	String stackName;
 
 	synchronized (stackNameLock) {
 	    stackName = "sip-Communicator." + stackInstance++;
 	}
 
 	System.setProperty("javax.sip.STACK_NAME", stackName);
 
 	Logger.forcePrintln("Stack name is " + stackName + " IP_ADDRESS "
 	    + System.getProperty("javax.sip.IP_ADDRESS"));
 
 	initRegistrarAddress();
 
         registerProcessing  = new RegisterProcessing(this);
         callProcessing      = new CallProcessing(this);
         sipSecurityManager  = new SipSecurityManager();
     }
 
     private void initRegistrarAddress() {
 	if (registrarAddress == null || registrarAddress.length() == 0) {
             registrarAddress = Utils.getPreference(
                 "com.sun.mc.softphone.sip.REGISTRAR_ADDRESS");
 
             if (registrarAddress == null || registrarAddress.length() == 0) {
 	        noStunRegistrar();
 	        return;
 	    }
 	}
 
         String registrarPort = Utils.getPreference(
             "com.sun.mc.softphone.sip.REGISTRAR_UDP_PORT");
 
         if (registrarPort != null && registrarPort.length() > 0) {
 	    try {
 		this.registrarPort = Integer.parseInt(registrarPort);
 	    } catch (NumberFormatException e) {
 		Logger.println("Invalid registrar port " + registrarPort
 		    + " defaulting to " + this.registrarPort);
 	    }
 	}
 
 	String s = registrarAddress;
 
         int ix = s.indexOf(";sip-stun");
 
 	if (ix < 0) {
 	    noStunRegistrar();
 	    return;
 	}
 
 	registrarIsStunServer = true;
 
 	s = s.substring(0, ix);
 
 	try {
             registrarAddress = InetAddress.getByName(s).getHostAddress();
         } catch (UnknownHostException e) {
             Logger.println("No Registrar:  Unable to resolve host " + registrarAddress);
 	    noStunRegistrar();
 	    return;
         }
 
 	if ((ix = s.indexOf(":")) >= 0) {
 	    s = s.substring(ix + 1);
 
 	    try {
 		this.registrarPort = Integer.parseInt(s);
 	    } catch (NumberFormatException e) {
 		Logger.println("Invalid registrar port " + s
 		    + " defaulting to " + this.registrarPort);
 	    }
 	}
     }
 
     private void noStunRegistrar() {
 	Logger.println("No STUN Registrar specified!");
         Logger.println("IF YOU ARE BEHIND A FIREWALL OR NAT,");
         Logger.println(
             "YOU MUST APPEND ;sip-stun TO THE SIP REGISTRAR ADDRESS ");
         Logger.println(
             "AND THE REGISTRAR MUST ALSO BE A STUN SERVER!");
     }
 
     public String getRegistrarAddress() {
 	return registrarAddress;
     }
 
     public boolean isRegistrarStunServer() {
 	return registrarIsStunServer;
     }
 
     public int getRegistrarPort() {
 	return registrarPort;
     }
 
     public void reRegister(String registrarAddress,
 	    String registrarPort) throws CommunicationsException {
 
 	endAllCalls();
 
 	String ipAddress;
 
 	String s = registrarAddress;
 
 	int ix = s.indexOf(";");
 
 	if (ix >= 0) {
 	    s = s.substring(0, ix);
 	}
 
 	try {
 	    InetAddress ia = InetAddress.getByName(s);
 	    ipAddress = ia.getHostAddress();
 	} catch (UnknownHostException e) {
 	    Logger.println("reRegister unable to get registrar:  "
 		+ e.getMessage());
 	    return;
 	}
 
 	System.setProperty("com.sun.mc.softphone.sip.REGISTRAR_ADDRESS",
             ipAddress);
 
         System.setProperty("com.sun.mc.softphone.sip.REGISTRAR_UDP_PORT",
             registrarPort);
 
         Logger.println("Setting registrar to " + ipAddress + ":"
             + registrarPort);
 
         registerProcessing = new RegisterProcessing(this);
 
         register(userName);
     }
 
     public void setRemoteSdpDescription(String sdp) {
 	mediaManager.setRemoteSdpData(sdp);
     }
 
     public String getAuthenticationUserName() {
 	return sipCommunicator.getAuthenticationUserName();
     }
 
     /**
      * Creates and initializes JAIN SIP objects (factories, stack, listening
      * point and provider). Once this method is called the application is ready
      * to handle (incoming and outgoing) sip messages.
      *
      * @throws CommunicationsException if an axception should occur during the
      * initialization process
      */
     public void start() throws CommunicationsException
     {
         try {
             console.logEntry();
 	
 	    localPort = findFreePort(
 		Utils.getPreference("com.sun.mc.softphone.sip.PREFERRED_LOCAL_PORT"));
 
             if (console.isDebugEnabled()) {
                 console.debug("preferred local port=" + localPort);
             }
 
             this.sipFactory = SipFactory.getInstance();
             sipFactory.setPathName(sipStackPath);
 
             try {
                 addressFactory = sipFactory.createAddressFactory();
                 headerFactory = sipFactory.createHeaderFactory();
                 messageFactory = sipFactory.createMessageFactory();
             }
             catch (PeerUnavailableException ex) {
                 console.error("Could not could not create factories!", ex);
                 throw new CommunicationsException(
                     "Could not create factories!",
                     ex
                     );
             }
 
             try {
                 sipStack = sipFactory.createSipStack(System.getProperties());
 
 	        Logger.forcePrintln("SipStack is " + sipStack);
             }
             catch (PeerUnavailableException ex) {
                 console.error("Could not could not create SipStack!", ex);
                 throw new CommunicationsException(
                     "Could not create SipStack!\n"
                     +
                     "A possible reason is an incorrect OUTBOUND_PROXY property\n"
                     + "(Syntax:<proxy_address:port/transport>)",
                     ex
                     );
             }
             try {
                 boolean successfullyBound = false;
                 while (!successfullyBound) {
                     try {
 			InetAddress localHost = 
 			    NetworkAddressManager.getPrivateLocalHost();
 
                         listeningPoint = sipStack.createListeningPoint(
                             localHost.getHostAddress(), localPort, transport);
 
 			publicIsa = listeningPoint.getPublicAddress();
 
 			Logger.println("private address is " 
 			    + localHost.getHostAddress() + ":" + localPort
 			    + " publicIsa is " + publicIsa);
                     }
                     catch (InvalidArgumentException ex) {
                         //choose another port between 1024 and 65000
                         Logger.println("error binding stack to port " + localPort 
 			    + ". Will try another port. " + ex.getMessage());
                         Logger.exception("Error assinging address", ex);
                         
                         localPort = (int) ( (65000 - 1024) * Math.random()) +
                             1024;
                         continue;
                     }
 		    catch (IOException e) {
 			Logger.println("Unable to get public address:  " 
 			    + e.getMessage());
 		    }
                     successfullyBound = true;
                 }
             }
             catch (TransportNotSupportedException ex) {
                 console.error(
                     "Transport " + transport
                     +
                     " is not suppported by the stack!\n Try specifying another"
                     + " transport in SipCommunicator property files.\n",
                     ex);
                 throw new CommunicationsException(
                     "Transport " + transport
                     +
                     " is not suppported by the stack!\n Try specifying another"
                     + " transport in SipCommunicator property files.\n",
                     ex);
             }
             try {
                 sipProvider = sipStack.createSipProvider(listeningPoint);
             }
             catch (ObjectInUseException ex) {
                 console.error("Could not could not create factories!\n", ex);
                 throw new CommunicationsException(
                     "Could not could not create factories!\n", ex);
             }
             try {
                 sipProvider.addSipListener(this);
             }
             catch (TooManyListenersException exc) {
                 console.error(
                     "Could not register SipManager as a sip listener!", exc);
                 throw new CommunicationsException(
                     "Could not register SipManager as a sip listener!", exc);
             }
 
             // we should have a security authority to be able to handle
             // authentication
             if(sipSecurityManager.getSecurityAuthority() == null)
             {
                 throw new CommunicationsException(
                     "No SecurityAuthority was provided to SipManager!");
             }
             sipSecurityManager.setHeaderFactory(headerFactory);
             sipSecurityManager.setTransactionCreator(sipProvider);
             sipSecurityManager.setSipManCallback(this);
 
 
             //Make sure prebuilt headers are nulled so that they get reinited
             //if this is a restart
             contactHeader = null;
             fromHeader = null;
             viaHeaders = null;
             maxForwardsHeader = null;
             isStarted = true;
         }
         finally {
             console.logExit();
         }
     }
 
     /**
      * Unregisters listening points, deletes sip providers, and generally
      * prepares the stack for a re-start(). This method is meant to be used
      * when properties are changed and should be reread by the stack.
      * @throws CommunicationsException
      */
     public void stop() throws CommunicationsException
     {
         try
         {
             console.logEntry();
 
 	    if (sipStack == null) {
 		return;
 	    }
 
 	    int tries;
 
 if (false) {
 	    /*
 	     * This seems to timeout for some reason
 	     * It's not really necessary since we are exiting anyway.
 	     */
 
             //Delete SipProvider
             for (tries = 0; tries < RETRY_OBJECT_DELETES; tries++) {
                 try {
                     sipStack.deleteSipProvider(sipProvider);
                 }
                 catch (ObjectInUseException ex) {
                     // Logger.println("Retrying delete of riSipProvider!");
                     sleep(RETRY_OBJECT_DELETES_AFTER);
                     continue;
                 }
                 break;
             }
             if (tries >= RETRY_OBJECT_DELETES)
                 throw new CommunicationsException(
                     "Failed to delete the sipProvider!");
 }
 
             //Delete RI ListeningPoint
             for (tries = 0; tries < RETRY_OBJECT_DELETES; tries++) {
                 try {
                     sipStack.deleteListeningPoint(listeningPoint);
                 }
                 catch (ObjectInUseException ex) {
                     //Logger.println("Retrying delete of riListeningPoint!");
                     sleep(RETRY_OBJECT_DELETES_AFTER);
                     continue;
                 }
 		catch (Exception e) {
 		}
                 break;
             }
             if (tries >= RETRY_OBJECT_DELETES)
                 throw new CommunicationsException(
                     "Failed to delete a listeningPoint!");
 
             sipProvider = null;
             listeningPoint = null;
             addressFactory = null;
             messageFactory = null;
             headerFactory = null;
             sipStack = null;
 
             viaHeaders = null;
             contactHeader = null;
             fromHeader = null;
         }finally
         {
             console.logExit();
         }
     }
 
     /**
      * Waits during _no_less_ than sleepFor milliseconds.
      * Had to implement it on top of Thread.sleep() to guarantee minimum
      * sleep time.
      *
      * @param sleepFor the number of miliseconds to wait
      */
     private static void sleep(long sleepFor)
     {
         try
         {
             console.logEntry();
 
             long startTime = System.currentTimeMillis();
             long haveBeenSleeping = 0;
             while (haveBeenSleeping < sleepFor) {
                 try {
                     Thread.sleep(sleepFor - haveBeenSleeping);
                 }
                 catch (InterruptedException ex) {
                     //we-ll have to wait again!
                 }
                 haveBeenSleeping = (System.currentTimeMillis() - startTime);
             }
         }finally
         {
             console.logExit();
         }
 
     }
 
     public void setCurrentlyUsedURI(String uri)
     {
         this.currentlyUsedURI = uri;
         this.contactHeader = null;
     }
 
     /**
      * Causes the RegisterProcessing object to send a registration request
      * to the registrar defined in
      * com.sun.mc.softphone.sip.REGISTRAR_ADDRESS and to register with
      * the address defined in the com.sun.mc.softphone.sip.PUBLIC_ADDRESS
      * property
      *
      * @throws CommunicationsException if an exception is thrown by the
      * underlying stack. The exception that caused this CommunicationsException
      * may be extracted with CommunicationsException.getCause()
      */
     public String register() throws CommunicationsException
     {
         return register(currentlyUsedURI);
     }
 
     private Integer addressLock = new Integer(0);
 
     public String getPublicAddress() {
 	synchronized(addressLock) {
 	    if (currentlyUsedURI == null) {
 		try {
 		    addressLock.wait();
 		} catch (InterruptedException e) {
 		}
 	    }
 	}
         return currentlyUsedURI;
     }
 
     private String userName;
 
     private String register(String userName) throws CommunicationsException {
 	this.userName = userName;
 
 	String publicAddress;
 
         try {
             console.logEntry();
 
             //Handle default domain name (i.e. transform 1234 -> 1234@sip.com
             String domain = Utils.getPreference("com.sun.mc.softphone.sip.DOMAIN_NAME");
 
 	    String host = null;
 
 	    if (domain == null || domain.equals("")) {
 	        host = publicIsa.getAddress().getHostAddress();
 	    }
 
 	    int publicPort = publicIsa.getPort();
 
 	    if (userName.indexOf("@") >= 0) {
 		publicAddress = "sip:" + userName + ":" + publicPort;
 	    } else {
 	        if (host == null) {
 	            publicAddress = "sip:" + userName + "@" + domain + ":" + publicPort;
 	        } else {
 	            publicAddress = "sip:" + userName + "@" + host + ":" + publicPort;
 	        }
 	    }
 
             this.currentlyUsedURI = publicAddress;
 
 	    synchronized(addressLock) {
 		addressLock.notifyAll();
 	    }
 
 	    //Logger.println("userName " + userName + " host " 
 	    //	+ host + " domain " + domain);
 	    //Logger.println("publicAddress " + publicAddress);
 
 	    Utils.setPreference("com.sun.mc.softphone.sip.PUBLIC_ADDRESS", publicAddress);
 
 	    /*
 	     * WARNING:  This message is read by the Meeting Central application.
 	     * Do not change this message without making the corresponding change
 	     * to MC.
 	     * 
 	     * This must be System.out.println and not Logger.println so that output
 	     * goes to stdout where the MC console is expecting to read this message.
 	     */
 	    System.out.println("SipCommunicator Public Address is '" + publicAddress + "'");
 
 	    Logger.writeFile("SipCommunicator Public Address is '" + publicAddress + "'");
 
 	    registrarAddress = Utils.getPreference("com.sun.mc.softphone.sip.REGISTRAR_ADDRESS");
 
 	    registrarPort = Integer.parseInt(
 		Utils.getPreference("com.sun.mc.softphone.sip.REGISTRAR_UDP_PORT"));
 	    registrarTransport = Utils.getPreference("com.sun.mc.softphone.sip.REGISTRAR_TRANSPORT");
 
 	    if (Logger.logLevel >= Logger.LOG_MOREINFO) {
 	        Logger.println("Registrar port " + registrarPort);
 	    }
 
 	    registrationsExpiration = 
 		Integer.parseInt(Utils.getPreference("com.sun.mc.softphone.sip.WAIT_UNREGISTGRATION_FOR"));
 
 	    if (registrarAddress != null && registrarAddress.length() > 0) {
 		int ix = registrarAddress.indexOf(";sip-stun");
 
 		if (ix >= 0) {
 		    registrarAddress = registrarAddress.substring(0, ix);
 		}
 
 		Logger.println("Registering with registrar " + registrarAddress + ":"
 		    + registrarPort + " expiration " + registrationsExpiration);
 
                 registerProcessing.register(registrarAddress, registrarPort,
                    registrarTransport, registrationsExpiration);
 	    } else {
 		/*
 		 * Even though we didn't register, notify everybody of our address
 		 */
 		try {
                     fireRegistered((SipURI) addressFactory.createURI(publicAddress));
 		} catch (ParseException e) {
 		    Logger.println("Invalid publicAddress " + publicAddress);
 		}
 	    }
         }
         finally {
             console.logExit();
         }
 	return publicAddress;
     }
 
     public String startRegisterProcess() throws CommunicationsException
     {
 	String publicAddress = null;
 
         try {
             console.logEntry();
             checkIfStarted();
 
 	    String userName = sipCommunicator.getUserName();
 	    String authenticationUserName = sipCommunicator.getAuthenticationUserName();
 
             UserCredentials defaultCredentials = new UserCredentials();
 
 	    defaultCredentials.setUserName(userName);
 	    defaultCredentials.setAuthenticationUserName(authenticationUserName);
             defaultCredentials.setPassword(new char[0]);
 
             String realm = 
 	 	Utils.getPreference("com.sun.mc.softphone.sip.DEFAULT_AUTHENTICATION_REALM");
 
 	    if (realm == null) {
 		realm = "";
 	    }
 
             UserCredentials initialCredentials = securityAuthority.obtainCredentials(realm,
                 defaultCredentials);
 
             publicAddress = register(initialCredentials.getUserName());
 
             //at this point a simple register request has been sent and the global
             //from  header in SipManager has been set to a valid value by the RegisterProcesing
             //class. Use it to extract the valid user name that needs to be cached by
             //the security manager together with the user provided password.
             initialCredentials.setUserName(((SipURI)getFromHeader().getAddress().getURI()).getUser());
 
             cacheCredentials(realm, initialCredentials);
         }
         finally {
             console.logExit();
         }
 	return publicAddress;
     }
 
     /**
      * Causes the RegisterProcessing object to send a registration request with
      * a 0 "expires" interval to the registrar defined in
      * com.sun.mc.softphone.sip.REGISTRAR_ADDRESS.
      *
      * @throws CommunicationsException if an exception is thrown by the
      * underlying stack. The exception that caused this CommunicationsException
      * may be extracted with CommunicationsException.getCause()
      */
     public void unregister() throws CommunicationsException
     {
         try {
             console.logEntry();
             if (!isRegistered()) {
                 return;
             }
             checkIfStarted();
             registerProcessing.unregister();
 
         }
         finally {
             console.logExit();
         }
     }
 
     /**
      * Queries the RegisterProcessing object whether the application is registered
      * with a registrar.
      * @return true if the application is registered with a registrar.
      */
     public boolean isRegistered()
     {
         return (registerProcessing != null && registerProcessing.isRegistered());
     }
 
     public InetSocketAddress getPublicIsa() {
 	return publicIsa;
     }
 
     /**
      * Determines whether the SipManager was start()ed.
      * @return true if the SipManager was start()ed.
      */
     public boolean isStarted()
     {
         return isStarted;
     }
 
 //============================ COMMUNICATION FUNCTIONALITIES =========================
     /**
      * Causes the CallProcessing object to send  an INVITE request to the
      * URI specified by <code>callee</code>
      * setting sdpContent as message body. The method generates a Call object
      * that will represent the resulting call and will be used for later
      * references to the same call.
      *
      * @param callee the URI to send the INVITE to.
      * @param sdpContent the sdp session offer.
      * @return the Call object that will represent the call resulting
      *                  from invoking this method.
      * @throws CommunicationsException if an exception occurs while sending and
      * parsing.
      */
     public Call establishCall(CallListener callListener, 
 	    String callee, String sdpContent) throws CommunicationsException {
 
         try {
             console.logEntry();
             checkIfStarted();
             return callProcessing.invite(callListener, callee, sdpContent);
         }
         finally {
             console.logExit();
         }
     } //CALL
 
     //------------------ hang up on
     /**
      * Causes the CallProcessing object to send a terminating request (CANCEL,
      * BUSY_HERE or BYE) and thus terminate that call with id <code>callID</code>.
      * @param callID the id of the call to terminate.
      * @throws CommunicationsException if an exception occurs while invoking this
      * method.
      */
     public void endCall(int callID) throws CommunicationsException
     {
         try {
             console.logEntry();
             checkIfStarted();
             callProcessing.endCall(callID);
         }
         finally {
             console.logExit();
         }
     }
 
     /**
      * Calls endCall for all currently active calls.
      * @throws CommunicationsException if an exception occurs while
      */
     public void endAllCalls() throws CommunicationsException
     {
         try {
             console.logEntry();
             if (callProcessing == null) {
                 return;
             }
             Object[] keys = callProcessing.getCallDispatcher().getAllCalls();
             for (int i = 0; i < keys.length; i++) {
                 endCall( ( (Integer) keys[i]).intValue());
             }
         }
         finally {
             console.logExit();
         }
     }
 
 
     /**
      * Causes CallProcessing to send a 200 OK response, with the specified
      * sdp description, to the specified call's remote party.
      * @param callID the id of the call that is to be answered.
      * @param sdpContent this party's media description (as defined by SDP).
      * @throws CommunicationsException if an axeption occurs while invoking this
      * method.
      */
     public void answerCall(int callID, String sdpContent) throws
         CommunicationsException
     {
         try {
             console.logEntry();
             checkIfStarted();
             callProcessing.sayOK(callID, sdpContent);
         }
         finally {
             console.logExit();
         }
     } //answer to
 
     /**
      * Sends a NOT_IMPLEMENTED response through the specified transaction.
      * @param serverTransaction the transaction to send the response through.
      * @param request the request that is being answered.
      */
     void sendNotImplemented(ServerTransaction serverTransaction,
                             Request request)
     {
         try {
             console.logEntry();
             Response notImplemented = null;
             try {
                 notImplemented =
                     messageFactory.createResponse(Response.NOT_IMPLEMENTED,
                                                   request);
 
 		if (serverTransaction.getDialog() != null) {
 		    // XXX JP getDialog() sometimes returns null
                     attachToTag(notImplemented, serverTransaction.getDialog());
 		}
             }
             catch (ParseException ex) {
                 fireCommunicationsError(
                     new CommunicationsException(
                     "Failed to create a NOT_IMPLEMENTED response to a "
                     + request.getMethod()
                     + " request!",
                     ex)
                     );
                 return;
             }
             try {
                 serverTransaction.sendResponse(notImplemented);
             }
             catch (Exception ex) {
                 fireCommunicationsError(
                     new CommunicationsException(
                     "Failed to create a NOT_IMPLEMENTED response to a "
                     + request.getMethod()
                     + " request!",
                     ex)
                     );
             }
         }
         finally {
             console.logExit();
         }
     }
 
 //============================= Utility Methods ==================================
     /**
      * Initialises SipManager's fromHeader field in accordance with
      * com.sun.mc.softphone.sip.PUBLIC_ADDRESS
      * com.sun.mc.softphone.sip.DISPLAY_NAME
      * com.sun.mc.softphone.sip.TRANSPORT
      * com.sun.mc.softphone.sip.PREFERRED_LOCAL_PORT and returns a
      * reference to it.
      * @return a reference to SipManager's fromHeader field.
      * @throws CommunicationsException if a ParseException occurs while
      * initially composing the FromHeader.
      */
     FromHeader getFromHeader() throws CommunicationsException
     {
         try {
             console.logEntry();
             if (fromHeader != null) {
                 return fromHeader;
             }
             try {
                 SipURI fromURI = (SipURI) addressFactory.createURI(
                     currentlyUsedURI);
                 //Unnecessary test (report by Willem Romijn)
                 //if (console.isDebugEnabled())
                 fromURI.setTransportParam(listeningPoint.getTransport());
 
                 fromURI.setPort(publicIsa.getPort());
                 Address fromAddress = addressFactory.createAddress(fromURI);
                 if (displayName != null && displayName.trim().length() > 0) {
                     fromAddress.setDisplayName(displayName);
                 } else {
                     fromAddress.setDisplayName(userName);
 		}
                 fromHeader = headerFactory.createFromHeader(fromAddress,
                     Integer.toString(hashCode()));
                 console.debug("Generated from header: " + fromHeader);
             } catch (ParseException ex) {
                 console.error(
                     "A ParseException occurred while creating From Header!", ex);
                 throw new CommunicationsException(
                     "A ParseException occurred while creating From Header!", ex);
             }
             return fromHeader;
         }
         finally {
             console.logExit();
         }
     }
 
     /**
      * Same as calling getContactHeader(true)
      *
      * @return the result of getContactHeader(true)
      * @throws CommunicationsException if an exception is thrown while calling
      * getContactHeader(false)
      */
     ContactHeader getContactHeader() throws CommunicationsException
     {
         return getContactHeader(true);
     }
 
     /**
      * Same as calling getContactHeader(true).
      * @return the result of calling getContactHeader(true).
      * @throws CommunicationsException if an exception occurs while executing
      * getContactHeader(true).
      */
     ContactHeader getRegistrationContactHeader() throws CommunicationsException
     {
         return getContactHeader(true);
     }
 
     /**
      * Initialises SipManager's contactHeader field in accordance with
      * javax.sip.IP_ADDRESS
      * com.sun.mc.softphone.sip.DISPLAY_NAME
      * com.sun.mc.softphone.sip.TRANSPORT
      * com.sun.mc.softphone.sip.PREFERRED_LOCAL_PORT and returns a
      * reference to it.
      * @param useLocalHostAddress specifies whether the SipURI in the contact
      * header should contain the value of javax.sip.IP_ADDRESS (true) or that of
      * com.sun.mc.softphone.sip.PUBLIC_ADDRESS (false).
      * @return a reference to SipManager's contactHeader field.
      * @throws CommunicationsException if a ParseException occurs while
      * initially composing the FromHeader.
      */
     ContactHeader getContactHeader(boolean useLocalHostAddress) throws
         CommunicationsException
     {
         try {
             console.logEntry();
             if (contactHeader != null) {
                 return contactHeader;
             }
             try {
 
                 SipURI contactURI;
                 if (useLocalHostAddress) {
 		    String user = 
 			((SipURI) getFromHeader().getAddress().getURI()).getUser();
 
                     contactURI = (SipURI) addressFactory.createSipURI(user,
                         publicIsa.getAddress().getHostAddress());
                 }
                 else {
                     contactURI = (SipURI) addressFactory.createURI(
                         currentlyUsedURI);
                 }
                 contactURI.setTransportParam(listeningPoint.getTransport());
                 contactURI.setPort(publicIsa.getPort());
                 Address contactAddress = addressFactory.createAddress(
                     contactURI);
                 if (displayName != null && displayName.trim().length() > 0) {
                     contactAddress.setDisplayName(displayName);
                 }
                 contactHeader = headerFactory.createContactHeader(
                     contactAddress);
                 if (console.isDebugEnabled()) {
                     console.debug("generated contactHeader:" + contactHeader);
                 }
             }
             catch (ParseException ex) {
                 console.error(
                     "A ParseException occurred while creating From Header!", ex);
                 throw new CommunicationsException(
                     "A ParseException occurred while creating From Header!", ex);
             }
             return contactHeader;
         }
         finally {
             console.logExit();
         }
     }
 
     /**
      * Initializes (if null) and returns an ArrayList with a single ViaHeader
      * containing localhost's address. This ArrayList may be used when sending
      * requests.
      * @return ViaHeader-s list to be used when sending requests.
      * @throws CommunicationsException if a ParseException is to occur while
      * initializing the array list.
      */
     ArrayList getLocalViaHeaders() throws CommunicationsException
     {
         try {
             console.logEntry();
             /*
              * We can't keep a cached copy because the callers
              * of this method change the viaHeaders.  In particular
              * a branch may be added which causes INVITES to fail.
              */
             //if (viaHeaders != null) {
             //    return viaHeaders;
             //}
             ListeningPoint lp = sipProvider.getListeningPoint();
             viaHeaders = new ArrayList();
             try {
 		String addr = lp.getIPAddress();
 
                 ViaHeader viaHeader = headerFactory.createViaHeader(
                     addr,
                     lp.getPort(),
                     lp.getTransport(),
                     null
                     );
 
 		viaHeader.setRPort();
 
                 viaHeaders.add(viaHeader);
                 if (console.isDebugEnabled()) {
                     console.debug("generated via headers:" + viaHeader);
                 }
                 return viaHeaders;
             }
             catch (ParseException ex) {
                 console.error(
                     "A ParseException occurred while creating Via Headers!");
                 throw new CommunicationsException(
                     "A ParseException occurred while creating Via Headers!");
             }
             catch (InvalidArgumentException ex) {
                 console.error(
                     "Unable to create a via header for port " + lp.getPort(),
                     ex);
                 throw new CommunicationsException(
                     "Unable to create a via header for port " + lp.getPort(),
                     ex);
             }
         }
         finally {
             console.logExit();
         }
     }
 
     /**
      * Initializes and returns SipManager's maxForwardsHeader field using the
      * value specified by MAX_FORWARDS.
      * @return an instance of a MaxForwardsHeader that can be used when
      * sending requests
      * @throws CommunicationsException if MAX_FORWARDS has an invalid value.
      */
     MaxForwardsHeader getMaxForwardsHeader() throws CommunicationsException
     {
         try {
             console.logEntry();
             if (maxForwardsHeader != null) {
                 return maxForwardsHeader;
             }
             try {
                 maxForwardsHeader = headerFactory.createMaxForwardsHeader(MAX_FORWARDS);
                 if (console.isDebugEnabled()) {
                     console.debug("generate max forwards: "
                                   + maxForwardsHeader.toString());
                 }
                 return maxForwardsHeader;
             }
             catch (InvalidArgumentException ex) {
                 throw new CommunicationsException(
                     "A problem occurred while creating MaxForwardsHeader", ex);
             }
         }
         finally {
             console.logExit();
         }
     }
 
     /**
      * Returns the user used to create the From Header URI.
      * @return the user used to create the From Header URI.
      */
     String getLocalUser()
     {
         try {
             console.logEntry();
             return ( (SipURI) getFromHeader().getAddress().getURI()).getUser();
         }
         catch (CommunicationsException ex) {
             return "";
         }
         finally {
             console.logExit();
         }
     }
 
     /**
      * Generates a ToTag (the conainingDialog's hashCode())and attaches it to
      * response's ToHeader.
      * @param response the response that is to get the ToTag.
      * @param containingDialog the Dialog instance that is to extract a unique
      * Tag value (containingDialog.hashCode())
      */
     void attachToTag(Response response, Dialog containingDialog)
     {
         try {
             console.logEntry();
             ToHeader to = (ToHeader) response.getHeader(ToHeader.NAME);
 
             if (to == null) {
                 fireCommunicationsError(
                     new CommunicationsException(
                     "No TO header found in, attaching a to tag is therefore impossible"));
             }
             try {
                 if (to.getTag() == null || to.getTag().trim().length() == 0) {
                     if (console.isDebugEnabled()) {
                         console.debug("generated to tag: " +
                                       containingDialog.hashCode());
                     }
                     to.setTag(Integer.toString(containingDialog.hashCode()));
                 }
             }
             catch (ParseException ex) {
                 fireCommunicationsError(
                     new CommunicationsException(
                     "Failed to attach a TO tag to an outgoing response"));
             }
         }
         finally {
             console.logExit();
         }
     }
 
     private int findFreePort(String portString) {
	int port = 5060;

	try {
	    port = Integer.parseInt(portString);
	} catch (NumberFormatException e) {
	    Logger.println("Invalid preferred port '" + portString 
		+ "'.  Defaulting to " + port);
	}
 	
         //
         // Make sure port is available.  If not, start with 5060 until we
         // find a useable port.
         //  
         boolean firstTime = true;
 
         while (true) {
             try {
 		//Logger.println("trying port " + port);
                 DatagramSocket socket = new DatagramSocket(port);
                 socket.close();
                 return port;
             } catch (SocketException e) {
                 /*
                  * port number is in use, try again.
                  */
                 if (firstTime && port != 5060) {
                     firstTime = false;
                     port = 5060;
                 } else {
                     port += 2;
                 }
             } catch (Exception e) {
 		break;
 	    }
         }
 
 	Logger.println("FATAL:  no local port available!");
 	return -1;
     }
 
 
 //============================     SECURITY     ================================
     /**
      * Sets the SecurityAuthority instance that should be consulted later on for
      * user credentials.
      *
      * @param authority the SecurityAuthority instance that should be consulted
      * later on for user credentials.
      */
     public void setSecurityAuthority(SecurityAuthority authority)
     {
         //keep a copty
         this.securityAuthority = authority;
         sipSecurityManager.setSecurityAuthority(authority);
     }
 
     /**
      * Adds the specified credentials to the security manager's credentials cache
      * so that they get tried next time they're needed.
      *
      * @param realm the realm these credentials should apply for.
      * @param credentials a set of credentials (username and pass)
      */
     public void cacheCredentials(String realm, UserCredentials credentials )
     {
         sipSecurityManager.cacheCredentials(realm, credentials);
     }
 //============================ EVENT DISPATHING ================================
     /**
      * Adds a CommunicationsListener to SipManager.
      * @param listener The CommunicationsListener to be added.
      */
     public void addCommunicationsListener(CommunicationsListener listener)
     {
         try {
             console.logEntry();
             listeners.add(listener);
         }
         finally {
             console.logExit();
         }
     }
 
     //------------ call received dispatch
     void fireCallReceived(Call call)
     {
         try {
             console.logEntry();
             if (console.isDebugEnabled()) {
                 console.debug("received call" + call);
             }
             CallEvent evt = new CallEvent(call);
             for (int i = listeners.size() - 1; i >= 0; i--) {
                 ( (CommunicationsListener) listeners.get(i)).callReceived(evt);
             }
         }
         finally {
             console.logExit();
         }
     } //call received
 
     //------------ call received dispatch
     void fireMessageReceived(Request message)
     {
         try {
             console.logEntry();
             if (console.isDebugEnabled()) {
                 console.debug("receied instant message=" + message);
             }
             MessageEvent evt = new MessageEvent(message);
             for (int i = listeners.size() - 1; i >= 0; i--) {
                 ( (CommunicationsListener) listeners.get(i)).messageReceived(
                     evt);
             }
         }
         finally {
             console.logExit();
         }
     } //call received
 
     //------------ registerred
     void fireRegistered(SipURI fromURI)
     {
 	String address = "sip:" + fromURI.getUser() + "@"
 	    + fromURI.getHost() + ":" + fromURI.getPort();
 
         try {
             console.logEntry();
             if (console.isDebugEnabled()) {
                 console.debug("registered with address = " + address);
             }
             RegistrationEvent evt = new RegistrationEvent(fromURI.toString());
             for (int i = listeners.size() - 1; i >= 0; i--) {
                 ( (CommunicationsListener) listeners.get(i)).registered(evt);
             }
 
 	    setCurrentlyUsedURI(address);
 
 	    publicIsa = new InetSocketAddress(fromURI.getHost(), 
 		fromURI.getPort());
 
 	    Utils.setPreference("com.sun.mc.softphone.sip.PUBLIC_ADDRESS", 
 		address);
 
             //NetworkAddressManager.setPublicControlAddress(publicIsa);
             
 	    if (Logger.logLevel >= Logger.LOG_MOREINFO) {
 	        Logger.println("Softphone registered as " + address);
 	    }
         }
 
         finally {
             console.logExit();
         }
     } //call received
 
     //------------ registering
     void fireRegistering(String address)
     {
         try {
             console.logEntry();
             if (console.isDebugEnabled()) {
                 console.debug("registering with address=" + address);
             }
             RegistrationEvent evt = new RegistrationEvent(address);
             for (int i = listeners.size() - 1; i >= 0; i--) {
                 ( (CommunicationsListener) listeners.get(i)).registering(evt);
             }
         }
         finally {
             console.logExit();
         }
     } //call received
 
     //------------ unregistered
     void fireUnregistered(String address)
     {
 	Logger.println("Unable to register address " + address);
 
         try {
             console.logEntry();
             if (console.isDebugEnabled()) {
                 console.debug("unregistered, address is " + address);
             }
             RegistrationEvent evt = new RegistrationEvent(address);
             for (int i = listeners.size() - 1; i >= 0; i--) {
                 ( (CommunicationsListener) listeners.get(i)).unregistered(evt);
             }
         }
         finally {
             console.logExit();
         }
     } //call received
 
     void fireUnregistering(String address)
     {
         try {
             console.logEntry();
             if (console.isDebugEnabled()) {
                 console.debug("unregistering, address is " + address);
             }
             RegistrationEvent evt = new RegistrationEvent(address);
             for (int i = listeners.size() - 1; i >= 0; i--) {
                 ( (CommunicationsListener) listeners.get(i)).unregistering(evt);
             }
         }
         finally {
             console.logExit();
         }
     } //call received
 
 
     //---------------- received unknown message
     void fireUnknownMessageReceived(Message message)
     {
         try {
             console.logEntry();
             if (console.isDebugEnabled()) {
                 console.debug("unknown message=" + message);
             }
             UnknownMessageEvent evt = new UnknownMessageEvent(message);
             for (int i = listeners.size() - 1; i >= 0; i--) {
                 ( (CommunicationsListener) listeners.get(i)).
                     receivedUnknownMessage(
                     evt);
             }
         }
         finally {
             console.logExit();
         }
     } //unknown message
 
     //---------------- rejected a call
     void fireCallRejectedLocally(String reason, Message invite)
     {
         try {
             console.logEntry();
             if (console.isDebugEnabled()) {
                 console.debug("locally rejected call. reason="
                               + reason
                               + "\ninvite message=" + invite);
             }
             CallRejectedEvent evt = new CallRejectedEvent(reason, invite);
             for (int i = listeners.size() - 1; i >= 0; i--) {
                 ( (CommunicationsListener) listeners.get(i)).
                     callRejectedLocally(
                     evt);
             }
         }
         finally {
             console.logExit();
         }
     }
 
     void fireCallRejectedRemotely(String reason, Message invite)
     {
         try {
             console.logEntry();
             if (console.isDebugEnabled()) {
                 console.debug("call rejected remotely. reason="
                               + reason
                               + "\ninvite message=" + invite);
             }
             CallRejectedEvent evt = new CallRejectedEvent(reason, invite);
             for (int i = listeners.size() - 1; i >= 0; i--) {
                 ( (CommunicationsListener) listeners.get(i)).
                     callRejectedRemotely(
                     evt);
             }
         }
         finally {
             console.logExit();
         }
     }
 
     //call rejected
     //---------------- error occurred
     void fireCommunicationsError(Throwable throwable)
     {
         try {
             console.logEntry();
             console.error(throwable);
             CommunicationsErrorEvent evt = new CommunicationsErrorEvent(
                 throwable);
             for (int i = listeners.size() - 1; i >= 0; i--) {
                 ( (CommunicationsListener) listeners.get(i)).
                     communicationsErrorOccurred(evt);
             }
         }
         finally {
             console.logExit();
         }
     } //error occurred
 
 //============================= SIP LISTENER METHODS ==============================
     public void processRequest(RequestEvent requestReceivedEvent)
     {
         try {
             console.logEntry();
             if (console.isDebugEnabled()) {
                 console.debug("received request=" + requestReceivedEvent);
             }
             ServerTransaction serverTransaction = requestReceivedEvent.
                 getServerTransaction();
             Request request = requestReceivedEvent.getRequest();
             String method = ( (CSeqHeader) request.getHeader(CSeqHeader.NAME)).
                 getMethod();
             if (serverTransaction == null) {
                 try {
                     serverTransaction = sipProvider.getNewServerTransaction(
                         request);
                 }
                 catch (TransactionAlreadyExistsException ex) {
                     /*fireCommunicationsError(
                         new CommunicationsException(
                         "Failed to create a new server"
                         + "transaction for an incoming request\n"
                         + "(Next message contains the request)",
                         ex));
                     fireUnknownMessageReceived(request);*/
                     //let's not scare the user
                     console.trace("Failed to create a new server"
                         + "transaction for an incoming request");
 
                     return;
                 }
                 catch (TransactionUnavailableException ex) {
                     /**
                     fireCommunicationsError(
                         new CommunicationsException(
                         "Failed to create a new server"
                         + "transaction for an incoming request\n"
                         + "(Next message contains the request)",
                         ex));
                     fireUnknownMessageReceived(request);*/
                     //let's not scare the user
                     console.trace("Failed to create a new server " 
 		    	+ "transaction for an incoming request");
                     return;
                 }
             }
             Dialog dialog = serverTransaction.getDialog();
             Request requestClone = (Request) request.clone();
             //INVITE
             if (request.getMethod().equals(Request.INVITE)) {
                 console.debug("received INVITE");
 
 		if (currentlyUsedURI == null) {
 		    synchronized(registerProcessing) {
 		        if (delayedInviteProcessor != null) {
 			    return;
 		        }
 
 		        Logger.println("Delay processing INVITE request "
 			    + "until softphone has its local address");
 
 		        delayedInviteProcessor = new DelayedInviteProcessor(
 			    serverTransaction, request);
 		        return;
 		    }
 		}
 
                 if (serverTransaction.getDialog().getState() == null) {
                     if(console.isDebugEnabled())
                         console.debug("request is an INVITE. Dialog state="
                                       +serverTransaction.getDialog().getState());
                     callProcessing.processInvite(serverTransaction, request);
                 } else {
                     console.debug("request is a reINVITE. Dialog state="
                                       +serverTransaction.getDialog().getState());
                     callProcessing.processReInvite(serverTransaction, request);
                 }
             }
             //ACK
             else if (request.getMethod().equals(Request.ACK)) {
                 if (serverTransaction != null
                     && serverTransaction.getDialog().getFirstTransaction().
                     getRequest().getMethod().equals(Request.INVITE)) {
                     callProcessing.processAck(serverTransaction, request);
                 }
                 else {
                     // just ignore
                     console.debug("ignoring ack");
                 }
             }
             //BYE
             else if (request.getMethod().equals(Request.BYE)) {
                 if (dialog.getFirstTransaction().getRequest().getMethod().
                     equals(
                     Request.INVITE)) {
                     callProcessing.processBye(serverTransaction, request);
                 }
             }
             //CANCEL
             else if (request.getMethod().equals(Request.CANCEL)) {
                 if (dialog.getFirstTransaction().getRequest().getMethod().
                     equals(
                     Request.INVITE)) {
                     callProcessing.processCancel(serverTransaction, request);
                 }
                 else {
                     sendNotImplemented(serverTransaction, request);
 
                     fireUnknownMessageReceived(requestReceivedEvent.getRequest());
                 }
             }
             //REFER
             else if (request.getMethod().equals(Request.REFER)) {
                 console.debug("Received REFER request");
                 transferProcessing.processRefer(serverTransaction, request);
             }
             else if (request.getMethod().equals(Request.INFO)) {
                 /** @todo add proper request handling */
                 sendNotImplemented(serverTransaction, request);
                 fireUnknownMessageReceived(requestReceivedEvent.getRequest());
             }
             else if (request.getMethod().equals(Request.MESSAGE)) {
                 fireMessageReceived(request);
             }
             else if (request.getMethod().equals(Request.NOTIFY)) {
                 /** @todo add proper request handling */
                 sendNotImplemented(serverTransaction, request);
                 fireUnknownMessageReceived(requestReceivedEvent.getRequest());
             }
             else if (request.getMethod().equals(Request.OPTIONS)) {
                 /** @todo add proper request handling */
                 sendNotImplemented(serverTransaction, request);
                 fireUnknownMessageReceived(requestReceivedEvent.getRequest());
             }
             else if (request.getMethod().equals(Request.PRACK)) {
                 /** @todo add proper request handling */
                 sendNotImplemented(serverTransaction, request);
                 fireUnknownMessageReceived(requestReceivedEvent.getRequest());
             }
             else if (request.getMethod().equals(Request.REGISTER)) {
                 /** @todo add proper request handling */
                 sendNotImplemented(serverTransaction, request);
                 fireUnknownMessageReceived(requestReceivedEvent.getRequest());
             }
             else if (request.getMethod().equals(Request.SUBSCRIBE)) {
                 /** @todo add proper request handling */
                 sendNotImplemented(serverTransaction, request);
                 fireUnknownMessageReceived(requestReceivedEvent.getRequest());
             }
             else if (request.getMethod().equals(Request.UPDATE)) {
                 /** @todo add proper request handling */
                 sendNotImplemented(serverTransaction, request);
                 fireUnknownMessageReceived(requestReceivedEvent.getRequest());
             }
             else {
                 //We couldn't recognise the message
                 sendNotImplemented(serverTransaction, request);
 
                 fireUnknownMessageReceived(requestReceivedEvent.getRequest());
             }
         }
         finally {
             console.logExit();
         }
 
 	if (Logger.logLevel >= Logger.LOG_MOREINFO) {
 	    Logger.println("Process request returning... " 
                 + requestReceivedEvent.getRequest().getMethod());
 	}
     }
 
     public void processTimeout(TimeoutEvent transactionTimeOutEvent)
     {
         if (transactionTimeOutEvent == null) {
 	    Logger.println("processTimeout:  transactionTimeOutEvent is null");
 	    return;
 	}
 
         try {
             console.logEntry();
             if (console.isDebugEnabled()) {
                 console.debug("received time out event: "
                               + transactionTimeOutEvent);
             }
             Transaction transaction;
             if (transactionTimeOutEvent.isServerTransaction()) {
                 transaction = transactionTimeOutEvent.getServerTransaction();
             }
             else {
                 transaction = transactionTimeOutEvent.getClientTransaction();
             }
             Request request =
                 transaction.getRequest();
             if (request.getMethod().equals(Request.REGISTER)) {
                 registerProcessing.processTimeout(transaction, request);
             }
             else if (request.getMethod().equals(Request.INVITE)) {
                 callProcessing.processTimeout(transaction, request);
             }
             else {
                 //Just show an error for now
                 //Console.showError("TimeOut Error!",
                 //    "Received a TimeoutEvent while waiting on a message"
                 //    + "\n(Check Details to see the message that caused it)",
                 //    request.toString()
                 //    );
 		// if (MediaManager.isStarted()) {
 		if (Logger.logLevel >= Logger.LOG_MOREINFO) {
 		    Console.showError(
 			"Timeout error while waiting for a response to "
 		        + request.getMethod());
 		}
 		// }
             }
         }
         finally {
             console.logExit();
         }
     }
 
     private ServerTransaction serverTransaction;
     private Request request;
 
     //-------------------- PROCESS RESPONSE
     public void processResponse(ResponseEvent responseReceivedEvent)
     {
         try {
             console.logEntry();
             if (console.isDebugEnabled()) {
                 console.debug("received response=" + responseReceivedEvent);
             }
             ClientTransaction clientTransaction = responseReceivedEvent.
                 getClientTransaction();
             if (clientTransaction == null) {
                 console.debug("ignoring a transactionless response");
                 return;
             }
             Response response = responseReceivedEvent.getResponse();
 
             Dialog dialog = clientTransaction.getDialog();
             String method = ( (CSeqHeader) response.getHeader(CSeqHeader.NAME)).
                 getMethod();
             Response responseClone = (Response) response.clone();
             //OK
 
 	    if (Logger.logLevel >= Logger.LOG_MOREINFO) {
 	        Logger.println("Got response code " + response.getStatusCode());
 	    }
 
             if (response.getStatusCode() == Response.OK) {
                 //REGISTER
                 if (method.equals(Request.REGISTER)) {
                     registerProcessing.processOK(clientTransaction, response);
                 }//INVITE
                 else if (method.equals(Request.INVITE)) {
                     callProcessing.processInviteOK(clientTransaction, response);
                 }//BYE
                 else if (method.equals(Request.BYE)) {
                     callProcessing.processByeOK(clientTransaction, response);
                 }//CANCEL
                 else if (method.equals(Request.CANCEL)) {
                     callProcessing.processCancelOK(clientTransaction, response);
                 }
             }
 
 	    //SESSION_PROGRESS
             else if (response.getStatusCode() == Response.SESSION_PROGRESS) {
 		/*
 		 * This is a workaround for a problem which occurs when calling
 		 * some AT&T phone numbers.  AT&T does not appear to always send
 		 * a connect message so we never get an OK.  
 		 * For now we treat SESSION_PROGRESS as OK.
 		 */
                 if (method.equals(Request.INVITE)) {
             	    ToHeader to = (ToHeader) response.getHeader(ToHeader.NAME);
 
             	    Address address = to.getAddress();
 
             	    String toNumber = address.getURI().toString();
 
 		    /*
 		     * This is a hack for AT&T numbers which never send OK.
 		     * One in particular is 866-839-8145.
 		     * We look for the prefix 9, 1, and if the number
 		     * starts with 8, we'll treat it specially.
 		     */
 		    String s = Utils.getPreference("com.sun.mc.softphone.sip.SESSION_PROGRESS_IS_OK");
 
 		    if (s == null || s.equalsIgnoreCase("true")) {
 		        /*
 		         * Delay process of session progress.
 		         * If an OK happens to come in before the timer expires
 		         * there's nothing to do.  Otherwise, treat the session progress
 		         * as OK.
 		         */
 			if (Logger.logLevel >= Logger.LOG_INFO) {
 			    Logger.println(
 				"SipManager:  Treating SESSION_PROGRESS as OK");
 			}
 		        new SessionProgressTimer(
 			    clientTransaction, callProcessing, response);
 		    }
 		} else {
                     fireUnknownMessageReceived(response);
 		}
             }
 
             //TRYING
             else if (response.getStatusCode() == Response.TRYING
                      //process all provisional responses here
                      //reported by Les Roger Davis
                      || response.getStatusCode() / 100 == 1) {
                 if (method.equals(Request.INVITE)) {
                     callProcessing.processTrying(clientTransaction, response);
                 }
                 //We could also receive a TRYING response to a REGISTER req
                 //bug reports by
                 //Steven Lass <sltemp at comcast.net>
                 //Luis Vazquez <luis at teledata.com.uy>
                 else if(method.equals(Request.REGISTER))
                 {
                     //do nothing
                 }
                 else {
                     fireUnknownMessageReceived(response);
                 }
             }
             //RINGING
             else if (response.getStatusCode() == Response.RINGING) {
                 if (method.equals(Request.INVITE)) {
                     callProcessing.processRinging(clientTransaction, response);
                 }
                 else {
                     fireUnknownMessageReceived(response);
                 }
             }
             //NOT_FOUND
             else if (response.getStatusCode() == Response.NOT_FOUND) {
                 if (method.equals(Request.INVITE)) {
                     callProcessing.processNotFound(clientTransaction, response);
                 }
                 else {
                     fireUnknownMessageReceived(response);
                 }
             }
             //NOT_IMPLEMENTED
             else if (response.getStatusCode() == Response.NOT_IMPLEMENTED) {
                 if (method.equals(Request.INVITE)) {
                     registerProcessing.processNotImplemented(clientTransaction,
                         response);
                 }
                 else if (method.equals(Request.REGISTER)) {
                     callProcessing.processNotImplemented(clientTransaction,
                         response);
                 }
                 else {
                     fireUnknownMessageReceived(response);
                 }
             }
             //REQUEST_TERMINATED
             else if (response.getStatusCode() == Response.REQUEST_TERMINATED) {
                 callProcessing.processRequestTerminated(clientTransaction,
                     response);
             }
             //BUSY_HERE
             else if (response.getStatusCode() == Response.BUSY_HERE) {
                 if (method.equals(Request.INVITE)) {
                     callProcessing.processBusyHere(clientTransaction, response);
                 }
                 else {
                     fireUnknownMessageReceived(response);
                 }
             }
             //401 UNAUTHORIZED
             else if (response.getStatusCode() == Response.UNAUTHORIZED
                      || response.getStatusCode() == Response.PROXY_AUTHENTICATION_REQUIRED) {
                 if(method.equals(Request.INVITE))
                     callProcessing.processAuthenticationChallenge(clientTransaction, response);
                 else if(method.equals(Request.REGISTER))
                     registerProcessing.processAuthenticationChallenge(clientTransaction, response);
                 else
                     fireUnknownMessageReceived(response);
             }
             //Other Errors
             else if ( //We'll handle all errors the same way so no individual handling
                      //is needed
                      //response.getStatusCode() == Response.NOT_ACCEPTABLE
                      //|| response.getStatusCode() == Response.SESSION_NOT_ACCEPTABLE
                      response.getStatusCode() / 100 == 4
                      )
             {
                if (method.equals(Request.INVITE)) {
                    callProcessing.processCallError(clientTransaction, response);
                }
                else {
                    fireUnknownMessageReceived(response);
                }
             }
             else if (response.getStatusCode() == Response.ACCEPTED) {
                 /** @todo add proper request handling */
                 fireUnknownMessageReceived(response);
             }
             else if (response.getStatusCode() == Response.ADDRESS_INCOMPLETE) {
                 /** @todo add proper request handling */
                 fireUnknownMessageReceived(response);
             }
             else if (response.getStatusCode() == Response.ALTERNATIVE_SERVICE) {
                 /** @todo add proper request handling */
                 fireUnknownMessageReceived(response);
             }
             else if (response.getStatusCode() == Response.AMBIGUOUS) {
                 /** @todo add proper request handling */
                 fireUnknownMessageReceived(response);
             }
             else if (response.getStatusCode() == Response.BAD_EVENT) {
                 /** @todo add proper request handling */
                 fireUnknownMessageReceived(response);
             }
             else if (response.getStatusCode() == Response.BAD_EXTENSION) {
                 /** @todo add proper request handling */
                 fireUnknownMessageReceived(response);
             }
             else if (response.getStatusCode() == Response.BAD_GATEWAY) {
                 /** @todo add proper request handling */
                 fireUnknownMessageReceived(response);
             }
             else if (response.getStatusCode() == Response.BAD_REQUEST) {
                 /** @todo add proper request handling */
                 fireUnknownMessageReceived(response);
             }
             else if (response.getStatusCode() == Response.BUSY_EVERYWHERE) {
                 /** @todo add proper request handling */
                 fireUnknownMessageReceived(response);
             }
             else if (response.getStatusCode() ==
                      Response.CALL_IS_BEING_FORWARDED) {
                 /** @todo add proper request handling */
                 fireUnknownMessageReceived(response);
             }
             else if (response.getStatusCode() ==
                      Response.CALL_OR_TRANSACTION_DOES_NOT_EXIST) {
                 /** @todo add proper request handling */
                 fireUnknownMessageReceived(response);
             }
             else if (response.getStatusCode() == Response.DECLINE) {
                 /** @todo add proper request handling */
                 fireUnknownMessageReceived(response);
             }
             else if (response.getStatusCode() ==
                      Response.DOES_NOT_EXIST_ANYWHERE) {
                 /** @todo add proper request handling */
                 fireUnknownMessageReceived(response);
             }
             else if (response.getStatusCode() == Response.EXTENSION_REQUIRED) {
                 /** @todo add proper request handling */
                 fireUnknownMessageReceived(response);
             }
             else if (response.getStatusCode() == Response.FORBIDDEN) {
                 /** @todo add proper request handling */
                 fireUnknownMessageReceived(response);
             }
             else if (response.getStatusCode() == Response.GONE) {
                 /** @todo add proper request handling */
                 fireUnknownMessageReceived(response);
             }
             else if (response.getStatusCode() == Response.INTERVAL_TOO_BRIEF) {
                 /** @todo add proper request handling */
                 fireUnknownMessageReceived(response);
             }
             else if (response.getStatusCode() == Response.LOOP_DETECTED) {
                 /** @todo add proper request handling */
                 fireUnknownMessageReceived(response);
             }
             else if (response.getStatusCode() == Response.MESSAGE_TOO_LARGE) {
                 /** @todo add proper request handling */
                 fireUnknownMessageReceived(response);
             }
             else if (response.getStatusCode() == Response.METHOD_NOT_ALLOWED) {
                 /** @todo add proper request handling */
                 fireUnknownMessageReceived(response);
             }
             else if (response.getStatusCode() == Response.MOVED_PERMANENTLY) {
                 /** @todo add proper request handling */
                 fireUnknownMessageReceived(response);
             }
             else if (response.getStatusCode() == Response.MOVED_TEMPORARILY) {
                 /** @todo add proper request handling */
                 fireUnknownMessageReceived(response);
             }
             else if (response.getStatusCode() == Response.MULTIPLE_CHOICES) {
                 /** @todo add proper request handling */
                 fireUnknownMessageReceived(response);
             }
             else if (response.getStatusCode() == Response.NOT_ACCEPTABLE_HERE) {
                 /** @todo add proper request handling */
                 fireUnknownMessageReceived(response);
             }
             else if (response.getStatusCode() == Response.PAYMENT_REQUIRED) {
                 /** @todo add proper request handling */
                 fireUnknownMessageReceived(response);
             }
             else if (response.getStatusCode() == Response.QUEUED) {
                 /** @todo add proper request handling */
                 fireUnknownMessageReceived(response);
             }
             else if (response.getStatusCode() ==
                      Response.REQUEST_ENTITY_TOO_LARGE) {
                 /** @todo add proper request handling */
                 fireUnknownMessageReceived(response);
             }
             else if (response.getStatusCode() == Response.REQUEST_PENDING) {
                 /** @todo add proper request handling */
                 fireUnknownMessageReceived(response);
             }
             else if (response.getStatusCode() == Response.REQUEST_TIMEOUT) {
                 /** @todo add proper request handling */
                 fireUnknownMessageReceived(response);
             }
             else if (response.getStatusCode() == Response.REQUEST_URI_TOO_LONG) {
                 /** @todo add proper request handling */
                 fireUnknownMessageReceived(response);
             }
             else if (response.getStatusCode() == Response.SERVER_INTERNAL_ERROR) {
                 if (method.equals(Request.INVITE)) {
                     callProcessing.processRemoteFatalError(clientTransaction, response);
                 } else {
                     fireUnknownMessageReceived(response);
 		}
             }
             else if (response.getStatusCode() == Response.SERVER_TIMEOUT) {
                 if (method.equals(Request.INVITE)) {
                     callProcessing.processRemoteFatalError(clientTransaction, response);
 		} else {
                     fireUnknownMessageReceived(response);
 		}
             }
             else if (response.getStatusCode() == Response.SERVICE_UNAVAILABLE) {
                 if (method.equals(Request.INVITE)) {
                     callProcessing.processRemoteFatalError(clientTransaction, response);
 		} else {
                     fireUnknownMessageReceived(response);
 		}
             }
             else if (response.getStatusCode() ==
                      Response.SESSION_NOT_ACCEPTABLE) {
                 /** @todo add proper request handling */
                 fireUnknownMessageReceived(response);
             }
             else if (response.getStatusCode() ==
                      Response.TEMPORARILY_UNAVAILABLE) {
 
                 if (method.equals(Request.INVITE)) {
                     callProcessing.processRemoteFatalError(clientTransaction, response);
 		} else {
                     fireUnknownMessageReceived(response);
 		}
             }
             else if (response.getStatusCode() == Response.TOO_MANY_HOPS) {
                 /** @todo add proper request handling */
                 fireUnknownMessageReceived(response);
             }
             else if (response.getStatusCode() == Response.UNDECIPHERABLE) {
                 /** @todo add proper request handling */
                 fireUnknownMessageReceived(response);
             }
             else if (response.getStatusCode() ==
                      Response.UNSUPPORTED_MEDIA_TYPE) {
                 /** @todo add proper request handling */
                 fireUnknownMessageReceived(response);
             }
             else if (response.getStatusCode() ==
                      Response.UNSUPPORTED_URI_SCHEME) {
                 /** @todo add proper request handling */
                 fireUnknownMessageReceived(response);
             }
             else if (response.getStatusCode() == Response.USE_PROXY) {
                 /** @todo add proper request handling */
                 fireUnknownMessageReceived(response);
             }
             else if (response.getStatusCode() == Response.VERSION_NOT_SUPPORTED) {
                 /** @todo add proper request handling */
                 fireUnknownMessageReceived(response);
             } else if (response.getStatusCode() > 400) {
                if (method.equals(Request.INVITE)) {
                    callProcessing.processCallError(clientTransaction, response);
                }
                else {
                    fireUnknownMessageReceived(response);
                }
 	    }
             else { //We couldn't recognise the message
                 fireUnknownMessageReceived(response);
             }
         }
         finally {
             console.logExit();
         }
     } //process response
 
     public void processDialogTerminated(DialogTerminatedEvent event) {
     }
 
     public void processTransactionTerminated(TransactionTerminatedEvent event) {
     }
 
     public void processIOException(IOExceptionEvent event) {
 	Logger.println("ProcessIOException event:  " + event);
     }
 
     private void checkIfStarted() throws CommunicationsException
     {
         if (!isStarted) {
             console.error("attempt to use the stack while not started");
             throw new CommunicationsException(
                 "The underlying SIP Stack had not been"
                 + "properly initialised! Impossible to continue");
         }
     }
 
     public void sendServerInternalError(int callID) throws
         CommunicationsException
     {
         try {
             console.logEntry();
             checkIfStarted();
             callProcessing.sayInternalError(callID);
         }
         finally {
             console.logExit();
         }
     }
 
     class SessionProgressTimer extends Thread {
 
 	ClientTransaction clientTransaction;
 	CallProcessing callProcessing;
 	Response response;
 
 	public SessionProgressTimer(ClientTransaction clientTransaction,
 	    CallProcessing callProcessing, Response response) {
 
 	    this.clientTransaction = clientTransaction;
 	    this.callProcessing = callProcessing;
 	    this.response = response;
 
 	    start();
 	}
 
 	public void run() {
 	    try {
 		Thread.sleep(1000);	// wait a second
 	    } catch (InterruptedException e) {
 	    }
 
             callProcessing.processInviteSessionProgress(clientTransaction, response);
 	}
     }
 
     class DelayedInviteProcessor extends Thread {
 
 	ServerTransaction serverTransaction;
 	Request request;
 
 	public DelayedInviteProcessor(ServerTransaction serverTransAction,
 	    Request request) {
 
 	    this.serverTransaction = serverTransaction;
 	    this.request = request;
 
 	    start();
 	}
 
 	public void run() {
 	    getPublicAddress();	// wait until we have our public address
 
 	    Logger.println("Processing delayed invite");
 
 	    if (serverTransaction.getDialog().getState() == null) {
                 callProcessing.processInvite(serverTransaction, request);
             } else {
                 callProcessing.processReInvite(serverTransaction, request);
             }
 
 	    synchronized (registerProcessing) {
 	        delayedInviteProcessor = null;
 	    }
 	}
     }
 
 }
