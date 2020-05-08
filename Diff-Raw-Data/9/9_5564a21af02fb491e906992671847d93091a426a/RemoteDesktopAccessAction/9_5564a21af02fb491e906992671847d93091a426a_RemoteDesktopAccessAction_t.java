 /**
  * SAHARA Rig Client
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
  * @author Tania Machet (tmachet)
  * @date 19th February 2010
  */
 package au.edu.uts.eng.remotelabs.rigclient.action.access;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import au.edu.uts.eng.remotelabs.rigclient.rig.IAccessAction;
 import au.edu.uts.eng.remotelabs.rigclient.util.ConfigFactory;
 import au.edu.uts.eng.remotelabs.rigclient.util.ILogger;
 import au.edu.uts.eng.remotelabs.rigclient.util.LoggerFactory;
 
 /**
  * Windows Terminal Services access action. Performs the access action by
  * adding and removing users from the configurable Remote Desktop Users group
  * which controls who may remote login to a Windows console using RDP.
  * <p>
  * This action only works for Windows, on other platforms a {@link IllegalStateException} will be thrown on
  * construction.
  * <p>
  * Note that the implementation assumes that the "net" command is in the PATH.
  * <p>
  * The configuration properties for RemoteDesktopAccessAction are:
  * <ul>
  *   <li><tt>Remote_Desktop_Windows_Domain</tt> - specifies the Win NT4 domain the user is part of (i.e their name is
  *   '\\&lt;Windows_Domain&gt;\&lt;name&gt;').</li>
  * </ul>
  * Access is granted with the <code>assign</code> method that adds users to the user group if they do not exist there
  * yet. Access is revoked with the <code>revoke</code> method that removes the user from the group.
  * 
  * @deprecated To be replaced au.edu.labshare.action.access.RemoteDesktopAccessAction 
  *      (au.edu.labshare holds commons library for Sahara Rig Client)
  */
 @Deprecated
 public class RemoteDesktopAccessAction implements IAccessAction
 {
 
     /** Default user group for remote desktop access. */
     public static final String DEFAULT_GROUPNAME = "\"Remote Desktop Users\"";
 
     /** Default command for changing user groups for windows */
     public static final String DEFAULT_COMMAND = "net";
 
     /** Default command for changing user groups for windows */
     public static final String DEFAULT_LOCALGROUP = "localgroup";
 
     /** Domain name. */
     private final String domainName;
 
     /** Reason for failure. */
     private String failureReason;
 
     /** Logger. */
     protected ILogger logger;
 
     /**
      * Constructor.
      */
     public RemoteDesktopAccessAction()
     {
         this.logger = LoggerFactory.getLoggerInstance();
 
         /* RDP access only valid for Windows - check that the OS is windows */
         if (System.getProperty("os.name").startsWith("Windows"))
         {
             /* Get domain if it is configured */
             if (ConfigFactory.getInstance().getProperty("Remote_Desktop_Windows_Domain","").equals(""))
             {
                 this.domainName = null;
             }
             else
             {
                 this.domainName = ConfigFactory.getInstance().getProperty("Remote_Desktop_Windows_Domain");
             }
             this.logger.info("The Remote Desktop Windows Domain has been set to " + this.domainName
                     + " for the Remote Desktop Access Action.");
         }
         else
         {
             this.logger.error("Unable to instantiate the Remote Desktop Action (" + this.getClass().getName() + 
                     ") becuase the detected platform is not Windows. Detected platform is '" + 
                     System.getProperty("os.name") + "'.");
             throw new IllegalStateException("Remote Desktop Action is only valid for a WINDOWS platforms not " + System.getProperty("os.name"));
         }
     }
 
     @Override
     public boolean assign(final String name)
     {
         synchronized (this)
         {
             /*
              * --------------------------------------------------------------------
              * 1. Check whether user is in RDP user group
              * a. sets up net process
              * b. executes process
              * c. check output to see whether user is in the group
              * --------------------------------------------------------------------
              */
             final List<String> commandBase = new ArrayList<String>();
 
             commandBase.add(RemoteDesktopAccessAction.DEFAULT_COMMAND);
             commandBase.add(RemoteDesktopAccessAction.DEFAULT_LOCALGROUP);
             final String groupName = ConfigFactory.getInstance().getProperty("Remote_Desktop_Groupname",
                     RemoteDesktopAccessAction.DEFAULT_GROUPNAME);
             this.logger.debug("The group name read is " + groupName + '.');
             if (groupName.charAt(0) == '"' && groupName.charAt(groupName.length()-1) == '"')
             {
                 commandBase.add(groupName);
             }
             else if (groupName.charAt(0) != '"' && groupName.charAt(groupName.length()-1) == '"')
             {
                 commandBase.add('"' + groupName);
             }
             else if (groupName.charAt(0) == '"' && groupName.charAt(groupName.length()-1) != '"')
             {
                 commandBase.add(groupName + '"');
             }
             else
             {
                 commandBase.add('"' + groupName + '"');
             }
             this.logger.debug("The base command is " + commandBase.toString() + '.');
 
             try
             {
                 Process proc = this.executeCommand(commandBase);
                 if (this.isUserInGroup(proc, name))
                 {
                     return true;
                 }
                 else
                 {
                     this.logger.debug("The user " + name + " is not yet in the user group " + groupName + 
                             " for Remote Desktop Access Acction.");
                     /*--------------------------------------------------------------------
                      * 2. If not, add user to group
                      * a. sets up net add process
                      * b. executes process
                      * -------------------------------------------------------------------- */
 
                     final List<String> commandAdd = new ArrayList<String>();
                     commandAdd.addAll(commandBase);
                     commandAdd.add("/ADD");
                     if (this.domainName != null)
                     {
                         commandAdd.add(this.domainName + "\\" + name);
                     }
                    else
                    {
                        commandAdd.add(name);
                    }
                     this.logger.debug("The command to be executed to add a user to the Remote Desktop Users group is "
                             + commandAdd.toString());
 
                     proc = this.executeCommand(commandAdd);
 
                     /*
                      * --------------------------------------------------------------------
                      * 3. Check whether user is in RDP user group
                      * a. sets up net process
                      * b. executes process
                      * c. check output to see whether user is in the group
                      * --------------------------------------------------------------------
                      */
                     proc = this.executeCommand(commandBase);
                     if (!this.isUserInGroup(proc, name))
                     {
                         this.logger.info("The user " + name + " could not be added to the user group " + groupName
                                 + "for Remote Desktop Access using the command " + commandAdd.toString() + '.');
                         this.failureReason = "User could not be added to Remote Desktop Users group.";
                         return false;
                     }
                     
                     return true;
                 }
             }
             catch (final Exception e)
             {
                 this.logger.info("Executing the command to add user " + name + " to the Remote Desktop users group " 
                         + groupName + " failed with error " + e.getMessage() + '.');
                 this.failureReason = "Command to add user to Remote Desktop Users group has failed.";
                 return false;
             }
         }
     }
 
     @Override
     public boolean revoke(final String name)
     {
         synchronized (this)
         {
             /*--------------------------------------------------------------------
              * 1. End users sessions
              * a. Set up process for qwinsta
              * b. Execute process
              * -------------------------------------------------------------------*/
             final List<String> command = new ArrayList<String>();
             command.add("qwinsta");
             command.add(name);
 
             Process proc;
             try
             {
                 proc = this.executeCommand(command);
                 final InputStream is = proc.getInputStream();
                 final BufferedReader br = new BufferedReader(new InputStreamReader(is));
                 String line = null;
 
                 /*
                  * --------------------------------------------------------------------
                  * c. Check user session exists using qwinsta output
                  * d. for each session, set up process for log-off
                  * e. execute log-off process
                  * --------------------------------------------------------------------
                  */
                 while (br.ready() && (line = br.readLine()) != null)
                 {
                     if (line.contains(name))
                     {
                         final String qwinstaSplit[] = line.split("\\s+");
                         this.logger.debug("The split qwinsta command is: " + Arrays.toString(qwinstaSplit) + '.');
                         String logoffID = null;
                         
                         for (int i=0; i<qwinstaSplit.length-1; i++)
                         {
                             if (qwinstaSplit[i].trim().equals(name))
                             {
                                 logoffID = qwinstaSplit[i+1]; 
                             }
                         }
                         if (logoffID == null) continue;
 
                         final List<String> logoffCommand = new ArrayList<String>();
                         logoffCommand.add("logoff");
                         logoffCommand.add(logoffID);
 
                         Process procLogoff;
                         procLogoff = this.executeCommand(logoffCommand);
                         if (procLogoff.exitValue() != 0)
                         {
                             this.logger.warn("Attempt to log off Remote Desktop session ID " + qwinstaSplit[2] + 
                                     " for user " + name + " returned unexpected result " + procLogoff.exitValue() + '.');
                             this.failureReason = "User session could not be ended - user acces revoke failed.";
                             return false;
                         }
                     }
                 }
                 br.close();
                 
                 /*--------------------------------------------------------------------
                  * 2. Check whether user is in RDP user group
                  * a. sets up net process
                  * b. executes process
                  * c. check output to see whether user is in the group
                  * --------------------------------------------------------------------*/
 
                 final List<String> commandBase = new ArrayList<String>();
                 commandBase.add(RemoteDesktopAccessAction.DEFAULT_COMMAND);
                 commandBase.add(RemoteDesktopAccessAction.DEFAULT_LOCALGROUP);
                 final String groupName = ConfigFactory.getInstance().getProperty("Remote_Desktop_Groupname",
                         RemoteDesktopAccessAction.DEFAULT_GROUPNAME);
                 this.logger.debug("The group name read is " + groupName + '.');
                 if (groupName.charAt(0) == '"' && groupName.charAt(groupName.length()-1) == '"')
                 {
                     commandBase.add(groupName);
                 }
                 else if (groupName.charAt(0) != '"' && groupName.charAt(groupName.length()-1) == '"')
                 {
                     commandBase.add('"' + groupName);
                 }
                 else if (groupName.charAt(0) == '"' && groupName.charAt(groupName.length()-1) != '"')
                 {
                     commandBase.add(groupName + '"');
                 }
                 else
                 {
                     commandBase.add('"' + groupName + '"');
                 }
                 this.logger.debug("The base command is " + commandBase.toString() + '.');
 
                 Process procCheck = this.executeCommand(commandBase);
                 if (this.isUserInGroup(procCheck, name))
                 {
                     /*
                      * --------------------------------------------------------------------
                      * 3. If yes, remove user from group
                      * a. sets up net delete process
                      * b. executes process
                      * --------------------------------------------------------------------
                      */
                     final List<String> commandDelete = new ArrayList<String>();
                     commandDelete.addAll(commandBase);
                     commandDelete.add("/DELETE");
                     if (this.domainName != null)
                     {
                         commandDelete.add(this.domainName + "\\" + name);
                     }
                    else
                    {
                        commandDelete.add(name);
                    }
                     
                     procCheck = this.executeCommand(commandDelete);
                     
                     if (procCheck.exitValue() != 0)
                     {
                         this.failureReason = "User could not be be removed from Remote Desktop Users group.";
                         return false;
                     }
                 }
             }
             catch (final Exception e)
             {
                 this.logger.warn("Revoke action for Remote Desktop Access for user " + name + "failed with exception "
                         + e.getClass().getName() + " and with message " + e.getMessage() + '.');
             }
         }
         return true;
     }
 
     @Override
     public String getActionType()
     {
         return "Windows Remote Desktop Access";
     }
 
     @Override
     public String getFailureReason()
     {
         return this.failureReason;
     }
 
     /**
      * Executes the access action specified using the command and working directory
      * 
      * @param command
      * @return Process
      * @throws Exception
      */
     private Process executeCommand(final List<String> command) throws Exception
     {
         final ProcessBuilder builder = new ProcessBuilder(command);
         Process accessProc = builder.start();
         this.logger.info("The Remote Desktop Access Action has invoked the command: " + command.toString() + '.');
         accessProc.waitFor();
 
         return accessProc;
     }
 
     /**
      * Check that a user belongs to the user group.
      * 
      * @param proc process whose input stream should be searched
      * @param name of user to be checked
      * @return true if user is in group
      * @throws IOException
      */
     private boolean isUserInGroup(final Process proc, final String name) throws IOException
     {
         BufferedReader br = null;
         try
         {
             br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
             String line = null;
     
             while (br.ready() && (line = br.readLine()) != null)
             {
                 if (line.contains(name))
                 {
                     this.logger.debug("The user " + name + " is in the user group.");
                     return true;
                 }
             }
             this.logger.debug("The user " + name + " is NOT in the user group.");
             return false;
         }
         finally
         {
             if (br != null) br.close();
         }
     }
 }
