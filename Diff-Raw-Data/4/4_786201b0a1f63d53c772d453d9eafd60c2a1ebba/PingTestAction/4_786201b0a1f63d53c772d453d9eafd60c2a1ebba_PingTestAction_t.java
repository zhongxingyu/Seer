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
  * @author Michael Diponio (mdiponio)
  * @date 19th January 2010
  *
  * Changelog:
  * - 19/01/2010 - mdiponio - Initial file creation.
  */
 package au.edu.uts.eng.remotelabs.rigclient.action.test;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 /**
  * Test action which determines if a host is up by pinging the host.
  * <p />
  * The behavior of ping test is:
  * <ol>
  *  <li>Test run interval - the default is 30 seconds but may be configured
  *  by setting the property 'Ping_Test_Interval' to a value in seconds.</li>
  *  <li>Periodicity - is periodic.</li>
  *  <li>Set interval - ignored, not honoured.</li> 
  *  <li>Light-dark scheduling - disabled.</li>
  * </ol>
  * The configuration properties for PingTestAction are:
  * <ul>
  *  <li><tt>Ping_Test_Host_&lt;n&gt;</tt> - The host names for the ping test
  *  to verify for response, where <tt>'n'</tt> is from 1 to the nth
  *  host to ping test, in order. The <tt>'Ping_Test_Host_1'</tt> property 
  *  is mandatory and each subsequent property is optional.</li>
  *  <li><tt>Ping_Test_Comand</tt> - The command to execute a ping. This is 
  *  optional, with the default being 'ping' which is expected to be in 
  *  $PATH in UNIX and %PATH% in Windows.</li>
  *  <li><tt>Ping_Test_Args</tt> - The arguments to supply to the ping command.
  *  Ideally this should cause the ping command to ping the host once and have
  *  a timeout of a few seconds. The host address is always the last argument.</li>
  *  <li><tt>Ping_Test_Interval</tt> - The time between ping tests in seconds.</li>
  *  <li><tt>Ping_Test_Fail_Threshold</tt> - The number of times ping must
  *  fail before the ping test fails.</li>
  * </ul>
  */
 @Deprecated
 public class PingTestAction extends AbstractTestAction
 {
     /** Default 'ping' command. */
     public static final String DEFAULT_PING = "ping";
     
     /** The hosts and their associated failures. */
     private final Map<String, Integer> hosts;
     
     /** Ping process builder. */
     private final ProcessBuilder pingBuilder;
     
     /** The number of times ping can fail before the test fails. */
     private int failThreshold;
     
     
     public PingTestAction()
     {
         super();
         this.runInterval = 30;
         this.isPeriodic = true;
         this.isSetIntervalHonoured = false;
         this.doLightDarkSchedule = false;
         
         this.hosts = new HashMap<String, Integer>();
         this.pingBuilder = new ProcessBuilder();
     }
     
     @Override
     public void setUp()
     {
         /* Set up command. */
         final List<String> command = new ArrayList<String>();
         String tmp =this.config.getProperty("Ping_Test_Command", PingTestAction.DEFAULT_PING);
         this.logger.info("Ping command is " + tmp + ".");
         command.add(tmp);
         
         tmp = this.config.getProperty("Ping_Test_Args", this.getDefaultPingArgs());
         this.logger.info(tmp.length() > 1 ? "Ping arguments are " + tmp + "." : "Not using any ping arguments.");
         for (String a : tmp.split("\\s"))
         {
             if (a.length() > 0) command.add(a);
         }
         this.pingBuilder.command(command);
 
         /* Load hosts. */
         if ((tmp = this.config.getProperty("Ping_Test_Host_1")) == null)
         {
             this.logger.error("When using ping test, atleast one host must be configured using the property" +
             		" 'Ping_Test_Host_1.");
             return;
         }
         this.logger.info("Going to test host " + tmp + " with ping test.");
         this.hosts.put(tmp, 0);
         
         int c = 2;
         while ((tmp = this.config.getProperty("Ping_Test_Host_" + c)) != null)
         {
             this.logger.info("Going to test host " + tmp + " with ping test.");
             this.hosts.put(tmp, 0);
             ++c;
         }
         
         /* Other configuration. */
         try
         {
             this.runInterval = Integer.parseInt(this.config.getProperty("Ping_Test_Interval", "30"));
             this.logger.info("The ping test interval is " + this.runInterval + " seconds.");
         }
         catch (NumberFormatException ex)
         {
             this.logger.warn("Invalid ping test interval configuration. It should be either undefined or a valid " +
             		" number to specify the test interval in seconds. Using 30 seconds as the default.");
             this.runInterval = 30;
         }
         
         try
         {
             this.failThreshold = Integer.parseInt(this.config.getProperty("Ping_Test_Fail_Threshold", "3"));
             this.logger.info("The ping test fail threshold is " + this.failThreshold + ".");
         }
         catch (NumberFormatException ex)
         {
             this.logger.warn("Invalid ping test fail threshold specified. This should be an integer value specifying " +
             		"the number of ping failures constitute a test failure. Using 3 as the default.");
             this.failThreshold = 3;
         }
     }
     
     @Override
     public void doTest()
     {
         final List<String> command = this.pingBuilder.command();
         for (Entry<String, Integer> host : this.hosts.entrySet())
         {
             if (!this.runTest) return;
             
             command.add(host.getKey());
             try
             {
                 final Process proc = this.pingBuilder.start();
                 if (proc.waitFor() != 0)
                 {
                     host.setValue(host.getValue() + 1);
                 }
                else
                {
                    host.setValue(0);
                }
             }
             catch (IOException e)
             {
                 this.logger.error("IO Exception running ping test for host " + host.getKey() + ". Does the " +
                 		" ping (" + command.get(0) + ") executable exist?");
             }
             catch (InterruptedException e)
             {
                 /* Resetting the interrupt to propagate it to shutdown the test. */
                 Thread.currentThread().interrupt();
                 return;
             }
             command.remove(host.getKey());
         }
     }
 
     @Override
     public void tearDown()
     {
         /* Does nothing. */
     }
 
     @Override
     public String getReason()
     {
         for (Entry<String, Integer> host : this.hosts.entrySet())
         {
             if (host.getValue() > this.failThreshold)
             {
                 return "Host " + host.getKey() + " has not responded to ping " + host.getValue() + " times."; 
             }
         }
         return null;
     }
 
     @Override
     public boolean getStatus()
     {
         for (Entry<String, Integer> host : this.hosts.entrySet())
         {
             if (host.getValue() > this.failThreshold)
             {
                 this.logger.debug("Failing ping test as host " + host.getKey() + " has failed " + host.getValue() +
                         " times (threshold is " + this.failThreshold + ").");
                 return false;
             }
         }
         return true;
     }
 
     @Override
     public String getActionType()
     {
         return "Ping test";
     }
     
     /**
      * Gets the default ping arguments based on the detected operation system.
      * The arguments attempt to send one ping and have a timeout of 5 seconds.
      * 
      * @return default ping argument string
      */
     private String getDefaultPingArgs()
     {
         final String os = System.getProperty("os.name");
 
         if (os.startsWith("Windows"))
         {
             /* Windows - [-n 1] 1 ping
              *         - [-w 5000] 5 second timeout. */
             this.logger.info("Returning Microsoft Windows ping default arguments as '-n 1 -w 5000' to " +
             "send one ping with a timeout of 5 seconds.");
             return "-n 1 -w 5000";
         }
         else if (os.startsWith("Linux"))
         {
             /* Linux - [-c 1] 1 ping
              *       - [-q] Quiet mode
              *       - [-W 5] 5 second timeout */
             this.logger.info("Returning Linux ping default arguments as '-c 1 -q -W 5 to send one ping with a " +
             "timeout of 5 seconds.");
             return "-c 1 -q -W 5";
         }
         else
         {
             this.logger.warn("Unsupported operating system detected (" + os + "), not specifing any ping " +
                     "arguments." + " If you have the need for another operating system, please fill a " +
             "bug report or email mdiponio@eng.uts.edu.au.");
             return "";
         }
     }
 }
