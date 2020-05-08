 /**
  * SAHARA Rig Client
  * 
  * Software abstraction of physical rig to provide rig session control
  * and rig device control. Automatically tests rig hardware and reports
  * the rig status to ensure rig goodness.
  *
  * @license See LICENSE in the top level directory for complete license terms.
  *
  * Copyright (c) 2009, University of Technology, Sydney
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
  * @date 20th February 2013
  */
 
 package au.edu.uts.eng.remotelabs.rigclient.rig.transfer;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import javax.activation.DataHandler;
 import javax.activation.FileDataSource;
 
 import au.edu.uts.eng.remotelabs.rigclient.rig.IRig;
 import au.edu.uts.eng.remotelabs.rigclient.status.SchedulingServerProviderStub;
 import au.edu.uts.eng.remotelabs.rigclient.status.StatusUpdater;
 import au.edu.uts.eng.remotelabs.rigclient.status.types.AddSessionFiles;
 import au.edu.uts.eng.remotelabs.rigclient.status.types.AddSessionFilesResponse;
 import au.edu.uts.eng.remotelabs.rigclient.status.types.SessionFile;
 import au.edu.uts.eng.remotelabs.rigclient.status.types.SessionFileTransfer;
 import au.edu.uts.eng.remotelabs.rigclient.status.types.SessionFiles;
 import au.edu.uts.eng.remotelabs.rigclient.util.ConfigFactory;
 import au.edu.uts.eng.remotelabs.rigclient.util.IConfig;
 import au.edu.uts.eng.remotelabs.rigclient.util.ILogger;
 import au.edu.uts.eng.remotelabs.rigclient.util.LoggerFactory;
 
 /**
  * Processes new session files to transfer them to the Scheduling Server.  
  */
 public class DataTransferWatcher extends Thread
 {
     /** Make attachments size after which no more attachments will be added. */
     public static final int TOTAL_ATTACHMENT_SIZE = 5 * 1024 * 1024;
     
     /** Make size file size for attachments to SOAP messages. */
     public static final int MAX_ATTACHMENT_SIZE = 2 * 1024 * 1024;
     
     /** Default data transfer restore file. */
     public static final String DEFAULT_RESTORE_FILE = "./dfrestore";
     
     /** Delimiter for the list of files in the restore file. */
     public static final String RESTORE_FILE_DELIM = ":#:";
     
     /** The rig client. */
     private final IRig rig;
     
     /* Interface to Scheduling Server. */
     private SchedulingServerProviderStub stub;
 
     /** Configured transfer method. */
     private final TransferMethod transferMethod;
 
     /** Local directory of Rig Client data storage. */
     private final String localDirectory;
     
     /** Restore file location. */
     private final String restoreFile;
     
     /** Whether the rig is in session. */
     private boolean inSession;
     
     /** The current session user. */
     private String currentUser;
     
     /** Session files. */
     private final Map<String, Set<File>> sessionFiles;
     
     /** Transferred files. */
     private final Map<String, List<File>> transferredFiles;
     
     /** Whether to clean files after transfer. */
     private final boolean cleanFiles;
     
     /** Whether data transfers are disabled. */
     private boolean isDisabled;
     
     /** Whether to shutdown. */
     private boolean shutdown;
     
     /** Logger. */
     private final ILogger logger;
     
     public DataTransferWatcher(IRig rig)
     {
         this.logger = LoggerFactory.getLoggerInstance();
         this.logger.debug("Creating the session data file watcher service.");
         
         this.setName("Session Data Watcher");
         
         this.rig = rig;
         this.sessionFiles = new HashMap<String, Set<File>>();
         this.transferredFiles = new HashMap<String, List<File>>();
 
         IConfig conf = ConfigFactory.getInstance();
         
         TransferMethod meth;
         try
         {
             meth = TransferMethod.valueOf(conf.getProperty("Data_Transfer_Method", "WEBDAV"));
             this.logger.debug("Using session data transfer method '" + meth + "'.");
         }
         catch (IllegalArgumentException ex)
         {
             this.logger.error("Session data transfer method '" + conf.getProperty("Data_Transfer_Method") + "' is not a valid " +
             		"option, using 'WEBDAV' transfer method.");
             meth = TransferMethod.WEBDAV;
         }
         this.transferMethod = meth;
             
         String tmp = conf.getProperty("Data_Transfer_Local_Directory", "");
         if ("".equals(tmp))
         {
             this.logger.debug("No local directory configured for session data transfer paths. Providing full file paths to the" +
             		"Scheduling Server.");
         }
         else tmp = new File(tmp).getAbsolutePath();
         this.localDirectory = tmp;
         
         this.restoreFile = conf.getProperty("Data_Transfer_Restore_File", DEFAULT_RESTORE_FILE);
         this.logger.debug("Using session data transfer restore file: " + this.restoreFile);
 
         try
         {
             this.stub = new SchedulingServerProviderStub(StatusUpdater.getSchedulingServerEndpoint());
         }
         catch (Exception e)
         {
             this.isDisabled = true;
             this.logger.error("Unable to load Scheduling Server location, disabling session data file transfers.");
         }
         
         if (this.cleanFiles = this.transferMethod != TransferMethod.FILESYSTEM &&
                 "true".equals(conf.getProperty("Delete_Data_Files_After_Transfer")))
         {
             this.logger.info("Data transfer files will be deleted after they are transferred for user access.");
         }
         else
         {
             this.logger.info("Data transfer files will not be deleted after they are transferred for user access. This " +
             		"may be because the configured transfer method is 'FILESYSTEM' or data file deletion is not configured.");
         }
     }
 
     @Override
     public void run()
     {
         /* Some error in configuration. */
         if (this.isDisabled) return;
         
         /* Loading previous persisted data files. */
         this.loadRestoreFile();
         
         while (!this.shutdown)
         {
             synchronized (this)
             {
                 this.transferFiles();
             }
             
             try
             {
                 Thread.sleep(60000);
             }
             catch (InterruptedException e)
             { /* Expected interrupt. */ }
         }
 
         
         /* We are persisting the lists of data files to send so on next load they
          * can be sent. */
         this.storeRestoreFile();
     }
 
     /**
      * Checks if there are files to transfer and if there are, transfers them.
      */
     private void transferFiles()
     {
         if (this.inSession)
         {
             /* Poll for session files. */
             this.sessionFiles.get(this.currentUser).addAll(this.rig.detectSessionFiles());
         }
         
         for (Entry<String, Set<File>> e : this.sessionFiles.entrySet())
         {
             /* Nothing to send. */
             if (e.getValue().size() == 0 || 
                     e.getValue().size() == this.transferredFiles.get(e.getKey()).size()) continue; 
             
             /* Request parameters. */
             AddSessionFiles request = new AddSessionFiles();
             
             SessionFiles filesParam = new SessionFiles();
             filesParam.setName(this.rig.getName());
             filesParam.setUser(e.getKey());
             request.setAddSessionFiles(filesParam);
             
             /* Add the files to request. */
             int attachmentSize = 0;
             List<File> sentFiles = new ArrayList<File>();
             for (File f : e.getValue())
             {
                 /* We don't want to send a SOAP message to the Scheduling Server that is too large
                  * so we limit it here. */
                 if (attachmentSize > TOTAL_ATTACHMENT_SIZE) break;
                 
                 /* Check the file has not already been sent. */
                 f = f.getAbsoluteFile();
                 if (this.transferredFiles.get(e.getKey()).contains(f)) continue;
                 this.transferredFiles.get(e.getKey()).add(f);
                 
                 if (!f.exists())
                 {
                     /* If a file no longer exists, there is no much point trying to send it. */
                     this.logger.warn("Cannot transfer session data file '" + f.getName() + "' because it no longer exists.");
                     continue;
                 }
                 
                 /* File name. */
                 SessionFile fileParam = new SessionFile();
                 fileParam.setName(f.getName());
                 
                 /* File path. */
                 String path = f.getAbsoluteFile().getParent();
                 if (!"".equals(this.localDirectory) && path.startsWith(this.localDirectory))
                 {
                     path = path.substring(this.localDirectory.length());
                 }
                 fileParam.setPath(path);
                 
                 /* File timestamp. */
                 Calendar ts = Calendar.getInstance();
                 ts.setTimeInMillis(f.lastModified());
                 fileParam.setTimestamp(ts);
                 
                 /* Transfer method. */
                 fileParam.setTransfer(SessionFileTransfer.Factory.fromValue(this.transferMethod.toString()));
                 if (this.transferMethod == TransferMethod.ATTACHMENT)
                 {
                         /* Attachment has the file contents appended to the SOAP message. */
                         if (f.length() > MAX_ATTACHMENT_SIZE) 
                         {
                             this.logger.error("File '" + f.getPath() + "' is larger than maximum allowed attachment size " +
                                     "so will not be transferred for user access. Choose a different transfer method (i.e. 'WEBDAV' " +
                                     "to allow a user to view it.");
                             continue;
                         }
                         
                         attachmentSize += f.length();
                         fileParam.setFile(new DataHandler(new FileDataSource(f)));
                 }       
 
                 sentFiles.add(f);
                 filesParam.addFiles(fileParam);
             }
             
             try
             {
                 /* Send the request. */
                  AddSessionFilesResponse resp = this.stub.addSessionFiles(request);
                  if (resp.getAddSessionFilesResponse().getSuccessful())
                  {
                      if (this.transferMethod == TransferMethod.WEBDAV)
                      {
                          /* For WebDAV files, we want to submit them for transfer. */
                          
                      }
                      else if (this.transferMethod == TransferMethod.ATTACHMENT && this.cleanFiles)
                      {
                          this.logger.debug("Cleaning data transfer files after attachment, files: " + sentFiles);
                          for (File f : sentFiles) f.delete();
                      }
                  }
                  else
                  {
                      this.logger.error("Scheduling Server failed to accept session data files with reason: " + 
                              resp.getAddSessionFilesResponse().getErrorReason() + ". Will attempt to send them " +
                              "again at a later time.");
                      for (File f : sentFiles) this.transferredFiles.get(e.getKey()).remove(f);
                  }
             }
             catch (RemoteException ex)
             {
                 this.logger.error("Failed to communicate with the Scheduling Server to send session data files. Error is " + 
                         ex.getClass().getSimpleName() + ": " + ex.getMessage() + ". Will attempt to send them again at a later time.");
                 for (File f : sentFiles) this.transferredFiles.get(e.getKey()).remove(f);
             }
             
             /* Perform cleanup. */
             if (!e.getKey().equals(this.currentUser) && 
                     this.sessionFiles.get(e.getKey()).size() == this.transferredFiles.get(e.getKey()).size())
             {
                 this.sessionFiles.remove(e.getKey());
                 this.transferredFiles.remove(e.getKey());
             }
         }
     }
     
     /**
      * Loads a previously save data transfer restore file.
      */
     private void loadRestoreFile()
     {
         File restore = new File(this.restoreFile);
         if (!restore.exists())
         {
             this.logger.debug("Session data transfer restore file does not exist, nothing to restore.");
             return;
         }
         
         BufferedReader reader = null;
         try
         {
             reader = new BufferedReader(new InputStreamReader(new FileInputStream(restore)));
             
             String line;
             while ((line = reader.readLine()) != null)
             {
                 line = line.trim();
                 
                 /* The format of the line is '<name> <path>:$:<path>:$:...:$:<path>. */
                 int pos = line.indexOf(' ');
                 if (line.length() == 0 || pos < 1) continue;
                 
                 String user = line.substring(0, pos);
                 this.sessionFiles.put(user, new HashSet<File>());
                 this.transferredFiles.put(user, new ArrayList<File>());
                 
                 for (String path : line.substring(pos).split(RESTORE_FILE_DELIM))
                 {
                     File file = new File(path.trim());
                     if (file.exists())
                     {
                         this.logger.warn("Restore session file '" + path + "' for transfer.");
                         this.sessionFiles.get(user).add(file);
                     }
                     else
                     {
                         this.logger.warn("Cannot set session data file '" + path + "' of user '" + user + "' for " +
                         		"transfer for user access as it not longer exists.");
                     }
                 }
                 
                 if (this.sessionFiles.get(user).size() == 0)
                 {
                     /* All data files not valid, removing the user from list. */
                     this.sessionFiles.remove(user);
                     this.transferredFiles.remove(user);
                 }
             }
         }
         catch (IOException ex)
         {
             this.logger.error("Failed to load session data transfer restore file with error '" + ex.getClass().getSimpleName() 
                     + ": " + ex.getMessage() + "'. Attempting to delete restore file.");
         }
         finally
         {
             if (reader != null)
                 try
                 {
                     reader.close();
                 }
                 catch (IOException e)
                 { /* Not much to do. */ }
         }
         
         /* No more need for the restore file after it has been loaded. */
         restore.delete();
     }
     
     /**
      * Writes a restore file to persist untransferred for the next time the Rig
      * Client is run.
      */
     private void storeRestoreFile()
     {
         /* Nothing to restore. */
         if (this.sessionFiles.size() == 0) return;
         
         PrintWriter writer = null;
         try
         {
             writer = new PrintWriter(this.restoreFile);
             
             for (Entry<String, Set<File>> e : this.sessionFiles.entrySet())
             {
                 /* No need to store user if they have no files pending transfer. */
                 if (e.getValue().size() == this.transferredFiles.get(e.getKey()).size()) continue;
                 
                 /* User name. */
                 writer.print(e.getKey());
                 writer.print(' ');
                 
                 /* List of files. */
                 Iterator<File> it = e.getValue().iterator();
                 while (it.hasNext())
                 {
                     File f = it.next();
                     if (this.transferredFiles.get(e.getKey()).contains(f)) continue;
 
                     writer.print(f.getPath());
                     if (it.hasNext()) writer.print(RESTORE_FILE_DELIM);
                 }
                 
                 writer.println();
             }
         }
         catch (IOException ex)
         {
             this.logger.error("Failed to save session data restore restore file with error '" + 
                     ex.getClass().getSimpleName() + ": " + ex.getMessage() + ". Remaining session data files will " +
                     "not be transferred the next time the Rig Client is started.");
         }
         finally
         {
             if (writer != null) writer.close();
         }
     }
         
     /**
      * Notification a user has finished their session.
      * 
      * @param user user who has started their session.
      */
     public synchronized void sessionStarted(String user)
     {
         this.inSession = true;
         this.currentUser = user;
         this.sessionFiles.put(this.currentUser, new HashSet<File>());
         this.transferredFiles.put(this.currentUser, new ArrayList<File>());
     }
     
     /**
      * Notification a session has completed.
      */
     public synchronized void sessionComplete()
     {
         this.sessionFiles.get(this.currentUser).addAll(this.rig.detectSessionFiles());
         
         String releasedUser = this.currentUser;
         this.currentUser = null;
        this.inSession = true;
         
         if (this.sessionFiles.get(releasedUser).size() == this.transferredFiles.get(releasedUser).size())
         {
             this.sessionFiles.remove(releasedUser);
             this.transferredFiles.remove(releasedUser);
         }
         else
         {
             /* Interrupt the thread so it will send through data files. */
             this.interrupt();
         }
     }
 
     public void shutdown()
     {
         this.shutdown = true;
     }
     
     /** List of possible transfer methods. */
     public enum TransferMethod
     {
         /** Attachment to SOAP notification message. */
         ATTACHMENT, /** Shared filesystem with Scheduling Server. */
         FILESYSTEM, /** WebDAV Scheduling Server server. */
         WEBDAV
     }
 }
