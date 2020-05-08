 /* $Id$
  * $Revision$
  * $Date$
  * $Author$
  *
  * The Summa project.
  * Copyright (C) 2005-2007  The State and University Library
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  */
 /*
  * The State and University Library of Denmark
  * CVS:  $Id$
  */
 package dk.statsbiblioteket.util.NativeExecution;
 
 import dk.statsbiblioteket.util.qa.QAInfo;
 
 import java.io.*;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 
 /**
  * Native command executor. Based on ProcessBuilder. Incorporates timeout for
  * spawned processes.
  *
  * <p>Give the arguments, enviroment and starting directory when instantiating
  * this class. Then use either execute or executeNoCollect to spawn the process.
  * Execute automatically empties the output and error streams, which can then be
  * read after the process have returned.<br>
  * ExecuteNoCollect does not, so they might be filled, and block the process, until
  * they are emptied again.
  *
  * <p>Input to the program, it if for example is a 
  *
  *
  */
 @QAInfo(level = QAInfo.Level.NORMAL,
         state = QAInfo.State.IN_DEVELOPMENT,
         author = "te,ab")
 public class NativeRunner  {
     private InputStream processInput =   null;
     private InputStream processOutput = null;
     private InputStream processError =  null;
 
     /**
      * The threads that polls the output from the commands. When a thread is
      * finished, it removes itself from this list.
      */
     private final List<Thread> threads =
             Collections.synchronizedList(new LinkedList<Thread>());
 
     private final int MAXINITIALBUFFER = 1000000;
     private final int THREADTIMEOUT = 1000; // Milliseconds
     private final int POLLING_INTERVAL = 100;//milli
 
     private final ProcessBuilder pb;
 
     /**
      * No argument constructor. Cannot run, use setParameters.
      */
     public NativeRunner(){
         pb = new ProcessBuilder();
     }
 
     /**
      * Just this one command
      * @param command the command to run
      */
     public NativeRunner(String command) {
         this();
         pb.command(command);
     }
 
     /**
      * This list of commands
      * @param commands the list of commands to run
      */
     public NativeRunner(List<String> commands) {
         this();
         pb.command(commands);
     }
 
     /**
      * Run these commands in this directory, with this enviroment.
      * @param commands The commands to run
      * @param enviroment The enviroment the proces lives in
      * @param startingDir The starting dir for the commands
      */
     public NativeRunner(List<String> commands, Map<String,String> enviroment,
                         File startingDir) {
         this();
         setParameters(commands, enviroment, null,startingDir);
     }
 
     /**
      * Run these commands in this directory, with this enviroment.
      * @param commands The commands to run
      * @param enviroment The enviroment the proces lives in
      * @param processInput An inputstream from which the commands to the proces
      * can be read. Will only be read once, so you cannot make something
      * interactive.
      * @param startingDir The starting dir for the commands
      */
     public NativeRunner(List<String> commands, Map<String,String> enviroment,
                         InputStream processInput, File startingDir) {
         this();
         setParameters(commands, enviroment, processInput,startingDir);
     }
 
     public NativeRunner(List<String> commands, Map<String,String> enviroment) {
         this();
         setParameters(commands,enviroment,null,null);
     }
 
     /**
      * Set the parameters of a NativeRunner.
      * @param commands The commands to run
      * @param enviroment The enviroment the proces lives in
      * @param processInput An inputstream from which the commands to the proces
      * can be read. Will only be read once, so you cannot make something
      * interactive.
      * @param startingDir The starting dir for the commands
      */
     public void setParameters(List<String> commands,
                               Map<String,String> enviroment,
                               InputStream processInput, File startingDir) {
         pb.command(commands);
         pb.directory(startingDir);
        Map<String,String> env = pb.environment();

        env.putAll(enviroment);
         this.processInput = processInput;
     }
 
     /**
      * Execute the native commands, while blocking. Standard and error output
      * will be collected
      * and can be retrieved later by {@link #getProcessOutput} and
      * {@link #getProcessError}.
      * @return the exit value from the native commands.
      * @param maxOutput  the maximum number of bytes that should be collected
      *                   from the output of the native commands.
      * @param maxError   the maximum number of bytes that should be collected
      *                   from the error-output of the native commands.
      * @throws Exception if execution of the native commands failed.
      */
     public synchronized int execute(int maxOutput,
                                     int maxError) throws Exception {
         try {
             Process p = pb.start();
             ByteArrayOutputStream pOut =
                     collectProcessOutput(p.getInputStream(), maxOutput);
             ByteArrayOutputStream pError =
                     collectProcessOutput(p.getErrorStream(), maxError);
             feedProcess(p, processInput);
             while (true) {
                 try {
                     p.waitFor();
                     break;
                 } catch (InterruptedException e) {
                     // Ignoring interruptions, we just want to try waiting
                     // again.
                 }
             }
             waitForThreads();
             processOutput = new ByteArrayInputStream(pOut.toByteArray());
             processError = new ByteArrayInputStream(pError.toByteArray());
             return p.exitValue();
         } catch (IOException e) {
             throw new Exception("Failure while running "
                     + stringsToString(
                     (String[])(pb.command().toArray())), e);
         }
     }
 
 
     /**
      * Concatenate a array of strings
      * @param strings The strings to concatenate
      * @return The strings separated by a space
      */
     private String stringsToString(String[] strings) {
         StringWriter sw = new StringWriter(100);
         for (String s: strings) {
             sw.append(s).append(" ");
         }
         return sw.toString();
     }
 
 
 
     /**
      * Wait for the polling threads to finish.
      */
     private void waitForThreads() {
         long endTime = System.currentTimeMillis() + THREADTIMEOUT;
         while (System.currentTimeMillis() < endTime && threads.size() > 0) {
             try {
                 wait(POLLING_INTERVAL);
             } catch (InterruptedException e) {
                 // Ignore, as we are just waiting
             }
         }
     }
 
 
     /**
      * Execute the native commands. Standard and error output will not be
      * collected. It is the responsibility of the caller to empty these
      * OutputStreams, which can be accessed by {@link #getProcessOutput} and
      * {@link #getProcessError}. The maxRuntime is Long.MAX_VALUE.
      * @return the exit value from the native commands.
      * @throws Exception if execution of the native commands failed.
      */
     public int executeNoCollect() throws Exception {
         return executeNoCollect(Long.MAX_VALUE);
     }
 
     /**
      * Execute the native commands while blocking. Standard and error output will not be
      * collected. It is the responsibility of the caller to empty these
      * OutputStreams, which can be accessed by {@link #getProcessOutput} and
      * {@link #getProcessError}.
      * @param maxRuntime the maximum number of milliseconds that the native
      *                   commands is allowed to run.
      * @return the exit value from the native commands.
      * @throws Exception if execution of the native commands failed or
      *                   timed out.
      */
     public synchronized int executeNoCollect(final long maxRuntime) throws Exception {
         try {
             Process p = pb.start();
             processOutput = p.getInputStream();
             processError = p.getErrorStream();
             feedProcess(p, processInput);
             long startTime = System.currentTimeMillis();
 
             while (true){
                 //is the thread finished?
                 try {
                     //then return
                     return p.exitValue();
                 }catch (IllegalThreadStateException e){
                     //not finished
                 }
                 //is the runtime exceeded?
                 if (System.currentTimeMillis()-startTime > maxRuntime){
                     //then return
                     p.destroy();
                     throw new Exception("Process timed out");
                 }
                 //else sleep again
                 wait (POLLING_INTERVAL);
 
             }
 
         } catch (IOException e) {
           throw new Exception("Failure while running "
                     + stringsToString(
                     (String[])(pb.command().toArray())), e);
         }
     }
 
     /**
      * The OutputStream will either be the OutputStream directly from the
      * execution of the native commands with the method {@link #executeNoCollect}
      * or a cache with the output of the execution of the native commands by
      * {@link#execute}.
      * @return the output of the native commands.
      */
     public InputStream getProcessOutput() {
         return processOutput;
     }
     /**
      * The OutputStream will either be the error-OutputStream directly from the
      * execution of the native commands with the method {@link #executeNoCollect}
      * or a cache with the error-output of the execution of the native commands
      * by {@link#execute}.
      * @return the error-output of the native commands.
      */
     public InputStream getProcessError() {
         return processError;
     }
 
     private ByteArrayOutputStream collectProcessOutput(
             final InputStream inputStream, final int maxCollect) {
         final ByteArrayOutputStream stream =
                 new ByteArrayOutputStream(Math.min(MAXINITIALBUFFER,
                         maxCollect));
         Thread t = new Thread() {
             public void run() {
                 try {
                     InputStream reader = null;
                     OutputStream writer = null;
                     try {
                         reader = new BufferedInputStream(inputStream);
                         writer = new BufferedOutputStream(stream);
                         int c;
                         int counter = 0;
                         while ((c = reader.read()) != -1) {
                             counter++;
                             if (counter < maxCollect) {
                                 writer.write(c);
                             }
                         }
                     } finally {
                         if (reader != null) {
                             reader.close();
                         }
                         if (writer != null) {
                             writer.close();
                         }
                     }
                 } catch (IOException e) {
                     // This seems ugly
                     throw new RuntimeException("Couldn't read output from " +
                             "process.", e);
                 }
                 threads.remove(this);
             }
         };
         threads.add(t);
         t.start();
         return stream;
     }
 
     private void feedProcess(final Process process,
                              InputStream processInput) {
         if (processInput == null) {
             // No complaints here - null just means no input
             return;
         }
         final OutputStream pIn = process.getOutputStream();
         final InputStream given = processInput;
         new Thread() {
             public void run() {
                 try {
                     OutputStream writer = null;
                     try {
                         writer = new BufferedOutputStream(pIn);
                         int c;
                         while ((c = given.read()) != -1) {
                             writer.write(c);
                         }
                     } finally {
                         if (writer != null) {
                             writer.close();
                         }
                         pIn.close();
                     }
                 } catch (IOException e) {
                     // This seems ugly
                     throw new RuntimeException("Couldn't write input to " +
                             "process.", e);
                 }
             }
         }.start();
     }
 
     /**
      * Return what was printed on the output channel of a _finished_ process,
      *  as a string, including newlines
      * @return the output as a string
      */
     public String getProcessOutputAsString() {
         return getStringContent(getProcessOutput());
     }
 
     /**
      * Return what was printed on the error channel of a _finished_ process,
      * as a string, including newlines
      * @return the error as a string
      */
     public String getProcessErrorAsString() {
         return getStringContent(getProcessError());
     }
 
     private String getStringContent(InputStream stream) {
         if (stream == null) {
             return null;
         }
         BufferedInputStream in = new BufferedInputStream(stream, 1000);
         StringWriter sw = new StringWriter(1000);
         int c;
         try {
             while ((c = in.read()) != -1) {
                 sw.append((char)c);
             }
             return sw.toString();
         } catch (IOException e) {
             return "Could not transform content of stream to String";
         }
 
     }
 }
