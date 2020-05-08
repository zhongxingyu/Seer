 /**
  * SAHARA Rig Client
  * 
  * Software abstraction of physical rig to provide rig session control
  * and rig device control. Automatically tests rig hardware and reports
  * the rig status to ensure rig goodness.
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
  * @date 27th August 2010
  *
  * Changelog:
  * - 27/08/2010 - davknigh - Initial file creation.
  */
 package au.edu.labshare.rigclient.action.access;
 
 import java.util.ArrayList;
 import java.util.List;
 import au.edu.uts.eng.remotelabs.rigclient.rig.IAccessAction;
 import au.edu.uts.eng.remotelabs.rigclient.util.ConfigFactory;
 import au.edu.uts.eng.remotelabs.rigclient.util.IConfig;
 import au.edu.uts.eng.remotelabs.rigclient.util.ILogger;
 import au.edu.uts.eng.remotelabs.rigclient.util.LoggerFactory;
 
 /**
  * Access action that changes the ownership of specified devices to the 
  * allocated user on allocate, and then back to root on
  * revoke. This action can be used to change ownership of local devices, or
  * devices on a remote host, using ssh. For remote hosts, Host key 
  * authentication must be enabled - if the ssh transaction requires 
  * a password, the rigclient will deadlock. 
  * <br />
  * The required configuration for this action is:
  * <ul>
  *  <li>Device_Ownership_Host_Address - The address to the host where the
  *  device is connected to. This is only needed if the device is not local and
  *  requires a SSH connection to change the device ownership.</li>
  *  <li>Device_Ownership_Path_&lt;n&gt; - The paths of the devices to change the
  *  ownership of. Multiple paths can be configured from 1 to n paths 
  *  where n is the property name suffix.</li>
  * </ul>
  */
 public class DeviceOwnershipAccessAction implements IAccessAction
 {
     /** Logger */
     private ILogger logger;
     
     /** The devices to change ownership of */
     private List<String> devices;
     
     /** The last reason for failure */
     private String failureReason;
 
     /** Builder for operating system process */
     private ProcessBuilder pb;
 
     
     public DeviceOwnershipAccessAction()
     {
         IConfig config = ConfigFactory.getInstance();
         this.logger = LoggerFactory.getLoggerInstance();
         
         if (!"Linux".equals(System.getProperty("os.name")))
         {
             this.logger.error("The '" + this.getActionType() + "' class can only be used on Linux ('" + 
                     System.getProperty("os.name") + "' detected).");
             throw new IllegalStateException(this.getActionType() + " only works on Linux.");
         }
         
         this.devices = new ArrayList<String>();
         
         String hostAddress;
        if ((hostAddress = config.getProperty("Device_Ownership_Host_Address")) != null)
         {
             this.pb.command().add("ssh");
             this.pb.command().add(hostAddress);
             this.logger.debug(this.getActionType() + ": Changing ownership of devices on " + hostAddress + '.');
         }
         else
         {
             this.logger.debug(this.getActionType() + ": Changing ownership of local devices.");
         }
         
         this.pb.command().add("chown");
         
         /* Load configuration for each node. */
         int i = 1;
         String path;
        while ((path = config.getProperty("Device_Ownership_Path_" + i)) != null)
         {
             this.logger.info(this.getActionType() + ": Going to change ownership of device node with path " + path + ".");
             this.devices.add(path);
             i++;
         }
     }
     
     @Override
     public boolean assign(String name)
     {
         for (String device : this.devices)
         {
             this.logger.info(this.getActionType() + ": Changing ownership of device " + device + " to " + name + ".");
             
             if (!(setOwner(device, name)))
             {
                 return false;
             }
         }
         return true;
     }
 
     @Override
     public boolean revoke(String name)
     {
         for (String device : this.devices)
         {
             this.logger.info(this.getActionType() + ": Changing ownership of device " + device + " to root.");
             
             if (!(setOwner(device, "root")))
             {
                 return false;
             }
         }
         return true;
     }
 
     @Override
     public String getActionType()
     {
         return "Unix Device Ownership Access";
     }
 
     @Override
     public String getFailureReason()
     {
         return this.failureReason;
     }
     
     /**
      * Sets owner of specified device to specified user, using chown, and 
      * optionally, ssh, commands.
      * @param device device to change owner of
      * @param owner the new owner for the device
      * @return true on success, false otherwise
      */
     private boolean setOwner(String device, String owner)
     {
         boolean ret = true;
         
         try
         {
             this.pb.command().add(owner);
             this.pb.command().add(device);
             
             Process proc = this.pb.start();
             
             int returnValue;
             if ((returnValue = proc.waitFor()) != 0)
             {
                 this.logger.warn("Attempt to change owner of " + device + " to " + owner 
                         + " failed with return value "  + returnValue + '.');
                 this.failureReason = "Device ownership could not be set.";
                 ret = false;
             }
         }
         catch (Exception ex)
         {
             this.logger.warn("Attempt to change permissions of " + device + " to " + owner
                     + " failed with exception "  + ex.getClass().getName() + " and message " + ex.getMessage() + '.');
             this.failureReason = "Device ownership could not be set.";
             ret = false;
         }
         finally
         {
             this.pb.command().remove(owner);
             this.pb.command().remove(device);
         }
         
     return ret;
     }
 }
