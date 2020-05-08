 /*
 	Copyright (c) 2005 Serban Iordache 
 	
 	All rights reserved. 
 	
 	Permission is hereby granted, free of charge, to any person obtaining a 
 	copy of this software and associated documentation files (the 
 	"Software"), to deal in the Software without restriction, including 
 	without limitation the rights to use, copy, modify, merge, publish, 
 	distribute, and/or sell copies of the Software, and to permit persons 
 	to whom the Software is furnished to do so, provided that the above 
 	copyright notice(s) and this permission notice appear in all copies of 
 	the Software and that both the above copyright notice(s) and this 
 	permission notice appear in supporting documentation. 
 	
 	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS 
 	OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
 	MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT 
 	OF THIRD PARTY RIGHTS. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR 
 	HOLDERS INCLUDED IN THIS NOTICE BE LIABLE FOR ANY CLAIM, OR ANY SPECIAL 
 	INDIRECT OR CONSEQUENTIAL DAMAGES, OR ANY DAMAGES WHATSOEVER RESULTING 
 	FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, 
 	NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION 
 	WITH THE USE OR PERFORMANCE OF THIS SOFTWARE. 
 	
 	Except as contained in this notice, the name of a copyright holder 
 	shall not be used in advertising or otherwise to promote the sale, use 
 	or other dealings in this Software without prior written authorization 
 	of the copyright holder.
 */
 package net.sourceforge.gjtapi.raw.tapi3;
 
 import java.util.Map;
 
 import net.sourceforge.gjtapi.raw.tapi3.logging.Logger;
 
 /**
  * The default implementation of the {@link Tapi3Native} interface
  * @author Serban Iordache
  */
 public class Tapi3NativeImpl implements Tapi3Native {
 	private static Logger logger;
     
     /**
      * The one and only instance of Tapi3NativeImpl
      */
     private static Tapi3NativeImpl instance; // = new Tapi3NativeImpl();
     
     /**
      * The provider registered for this Tapi3NativeImpl
      */
     private Tapi3Provider provider = null;
     
     /**
      * Private constructor to prevent the instantiation
      */
     private Tapi3NativeImpl() {}
 
     /**
      * Return the Tapi3NativeImpl singleton
      * @return The one and only instance of this class
      */
     public static final Tapi3Native getInstance() {
       if(instance == null)
       {
         logger = Tapi3Provider.getLogger();
         instance = new Tapi3NativeImpl();
         try
         {
           //System.loadLibrary("Tapi3Provider");
           System.loadLibrary("Tapi3Provider"); //./src/net/sourceforge/gjtapi/raw/tapi3/native/Debug/Tapi3Provider");
         }
         catch (Throwable t)
         {
           String msg = "Library Tapi3Provider could not be loaded from " + System.getProperty("java.library.path");
           logger.error(msg, t);
           throw new RuntimeException(msg, t);
         }
       }
       return instance;
     }
 
   public static final void releaseInstance()
   {
     logger = null;
     instance = null;
   }
 
     /**
      * Initialize the Tapi3 provider
      * @param props The name value properties map
      * @return Array of addresses or null on error
      */
     public native String[] tapi3Init(Map props);
     
     /**
      * Shut down the TAPI session
      * @return Error code (0=success) 
      */
     public native int tapi3Shutdown();
     
     /**
      * Answer an incoming call
      * @param callID The identifier for the call
      * @return Error code (0=success) 
      */
     public native int tapi3AnswerCall(int callID); 
     
     /**
      * Disconnect a call
      * @param callID The identifier for the call
      * @return Error code (0=success) 
      */
     public native int tapi3DisconnectCall(int callID); 
     
     /**
      * Release a call
      * @param callID The identifier for the call
      * @return Error code (0=success) 
      */
     public native int tapi3ReleaseCall(int callID); 
 
     /**
      * Reserve a callId
      * @param address The address that the call will start on
      * @return The reserved callID or a negative error code
      */
     public native int tapi3ReserveCallId(String address);
 
     /**
      * Create a call
      * @param callID The callId reserved for the call
      * @param address The address to make a call from
      * @param dest The destination address
      * @return The callID or a negative error code
      */
     public native int tapi3CreateCall(int callID, String address, String dest, int mode);
 
     /**
      * Dial a number on an existing call.
      * @param callID
      * @param numberToDial
      * @return 1 or a negative error code
      */
     public native int tapi3Dial(int callID, String numberToDial);
     
     /**
      * Put a call on hold 
      * @param callID The identifier for the call
      * @return Error code (0=success) 
      */
     //* @param address The address that defines the call to hold
     public native int tapi3Hold(int callID);
     
     /**
      * Join one call to another call
      * @param callID1 The identifier for one call
      * @param callID2 The identifier for another call
      * @param mode The mode for joining
      * @return The new callID or a negative error code
      */
     public native int tapi3Join(int callID1, int callID2, String address, String terminal, int mode);
     
     /**
      * Take a call off hold 
      * @param callID The identifier for the call that we want to take off hold
      * @return Error code (0=success) 
      */
     public native int tapi3UnHold(int callID);
     
     /**
      * Play DTMF tones on a terminal
      * @param terminal The terminal to play the DTMF tones to
      * @param digits A set of digits to send
      * @return Error code (0=success)
      */
     public native int tapi3SendSignals(String terminal, String digits);
     
     /**
      * Register a Tapi3 provider 
      * @param provider The Tapi3 provider
      */
     public void registerProvider(Tapi3Provider provider) {
         this.provider = provider;
     }
 
     /**
      * Callback method called by Tapi3Provider.dll. This implementation delegates the call to the callback method of the registered Tapi3 provider.
      * @param methodID The identifier of the method (event) that should be notified. Must be one of the <i>METHOD_XXX</i> values.
      * @param callID The identifier for the call
      * @param address The address that defines the call
      * @param jniCause The event cause. Must be one of the <i>JNI_CAUSE_XXX</i> values.
      * @param callInfo Array of 4 elements used to initialize a {@link Tapi3PrivateData}
      */
     public void callback(int methodID, int callID, String address, int jniCause, String[] callInfo) {
         if(provider != null) {
            provider.callback(methodID, callID, address, jniCause, callInfo);
         } else {
             Tapi3Provider.getLogger().error("Callback " + methodID + " called, but no provider registered.");
         }
     }
 
     /**
      * sends DeviceSpecific commands as defined by the tapi-specification
      * @param callID in case of a call is involved
      * @param address the address from the invoker
      * @param data the command data
      * @return a negative value in case of an error, zero in case of success and a positive value when the command is processed asynchronously
      */
     public native long tapi3LineDevSpecific(int callID, String address, byte[] data);
 
 }
