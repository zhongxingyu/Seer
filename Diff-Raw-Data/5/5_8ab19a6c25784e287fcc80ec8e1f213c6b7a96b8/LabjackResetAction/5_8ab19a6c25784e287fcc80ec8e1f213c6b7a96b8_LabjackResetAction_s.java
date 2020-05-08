 /**
  * Rig Client Commons.
  *
  * @license See LICENSE in the top level directory for complete license terms.
  *
  * Copyright (c) 2010, University of Technology, Sydney
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without 
  * modification, are permitted provided that the following conditions are met:
  *
  *  * Redistributions of source code must retain the above copyright notice, 
  *    this list of conditions and the following disclaimer.
  *  * Redistributions in binary form must reproduce the above copyright 
  *    notice, this list of conditions and the following disclaimer in the 
  *    documentation and/or other materials provided with the distribution.
  *  * Neither the name of the University of Technology, Sydney nor the names 
  *    of its contributors may be used to endorse or promote products derived from 
  *    this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE 
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * @author David Knight (davknigh)
  * @date 13th July 2010
  */
 
 package au.edu.labshare.rigclient.action.reset;
 
 import au.edu.labshare.rigclient.internal.labjack.Labjack;
 import au.edu.uts.eng.remotelabs.rigclient.rig.IResetAction;
 import au.edu.uts.eng.remotelabs.rigclient.util.ConfigFactory;
 import au.edu.uts.eng.remotelabs.rigclient.util.ILogger;
 import au.edu.uts.eng.remotelabs.rigclient.util.LoggerFactory;
 
import javax.usb.UsbException;

 
 /**
  * Reset action which sets all built-in digital and analogue ports of a LabJack
  * to off. Does not affect LJTick DACs The configuration required for this 
  * action is:
  * <ul>
  *  <li><tt>Labjack_Address</tt> - The IP address (for TCP connections) or 
  *  serial number (for USB connections) of the Labjack to be reset.</li>
  *  <li><tt>Labjack_Digital_Outputs</tt> - The number of digital outputs 
  *  available on the labjack. Optional: if missing, 23 will be used as 
  *  default.</li>
  *  <li><tt>Labjack_Analogue_Outputs</tt> - The number of analogue outputs 
  *  available on the labjack. Optional: if missing, 2 will be used as 
  *  default.</li>
  * </ul> 
  */
 public class LabjackResetAction implements IResetAction
 {
     /** Labjack Device */
     private Labjack lj;
     
     /** Details of connection - IP address or Serial Number */
     private String address;
     
     /** Reason the previous invocation of reset failed. */
     private String failureReason;
     
     /** Logger. */
     private ILogger logger;
     
     /** Number of Labjack Outputs */
     private int numberOfDigitalOutputs;
     private int numberOfAnalogueOutputs;
     
     
    public LabjackResetAction() throws UsbException
     {
         this.logger = LoggerFactory.getLoggerInstance();
         
         //get configuration
         if ((this.address = ConfigFactory.getInstance().getProperty("Labjack_Address")) == null)
         {
             this.logger.error("Unable to initialise the Labjack connection. The property 'Labjack_Address' was not " +
             		"found. This must be configured with the network address or serial number of the Labjack.");
             throw new IllegalArgumentException();
         }
         else
         {
             this.logger.info("Labjack address is: " + this.address + '.');
         }
         
         try
         {
             numberOfDigitalOutputs = Integer.parseInt(ConfigFactory.getInstance().
                     getProperty("Labjack_Digital_Outputs", "23"));
         }
         catch (NumberFormatException ex)
         {
             numberOfDigitalOutputs = 23;
         }
         try
         {
             numberOfAnalogueOutputs = Integer.parseInt(ConfigFactory.getInstance().
                     getProperty("Labjack_Analogue_Outputs", "2"));
         }
         catch (NumberFormatException ex)
         {
             numberOfAnalogueOutputs = 2;
         }
         
         this.logger.info("Resetting " + numberOfDigitalOutputs + " digital and " + numberOfAnalogueOutputs 
                 + "analogue labjack outputs.");
         
         //create labjack object    
         if (this.address.matches("\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b"))   //Check if the input matches ip address format
         {
             this.logger.info("Labjack address interpreted as ip address.");
             this.lj = new Labjack(this.address);
         }
         else
         {
             int serialNumber = 0;
             
             try
             {
                 serialNumber = Integer.parseInt(this.address);
                 this.logger.info("Labjack address interpreted as a serial number.");
             }
             catch (NumberFormatException ex)
             {
                 this.logger.error("Unable to initialise the Labjack connection. The property 'Labjack_Address' is " +
                 		"not a valid ip address or serial number.");
                 throw new IllegalArgumentException();
             }
             
             /** 
              * Minimum & maximum serial numbers used by labjack - 
              * see http://labjack.com/support/ue9/users-guide/5.2.1
              */
             if (serialNumber >= 268435456 && serialNumber <= 285212671)     
             {
                 this.lj = new Labjack(serialNumber);
             }
             else
             {
                 this.logger.error("Unable to initialise the Labjack connection. The property 'Labjack_Address' is " +
                 "not a valid serial number.");
                 throw new IllegalArgumentException();
             }
         }
     }
 
     @Override
     public boolean reset() 
     {
         try
         {
             this.logger.debug("Labjack Reset Action - Connecting to Labjack '" + this.address + "'.");
             this.lj.connect();
             
             this.logger.debug("Labjack Reset Action - Setting digital outputs to low");
             for (int i = 0; i < numberOfDigitalOutputs; i++)
             {
                 this.lj.writeDigital(i, false);
             }
             
             this.logger.debug("Labjack Reset Action - Setting analogue outputs to 0v");            
             for (int i = 0; i < numberOfAnalogueOutputs; i++)
             {
                 this.lj.writeAnalogue(i, 0.0);
             }
             
             this.lj.disconnect();
             
             this.failureReason = null;
             return true;
         }
         catch (Exception e)
         {
             this.failureReason = "Failed to reset labjack outputs, because of exception '" 
                 + e.getClass().getSimpleName() + "', message '" + e.getMessage() + "'.";
             this.logger.error(this.failureReason);
         }
         return false;
     }
 
     @Override
     public String getActionType() 
     {
         return "Labjack Reset";
     }
 
     @Override
     public String getFailureReason() 
     {
         return this.failureReason;
     }
 
 }
