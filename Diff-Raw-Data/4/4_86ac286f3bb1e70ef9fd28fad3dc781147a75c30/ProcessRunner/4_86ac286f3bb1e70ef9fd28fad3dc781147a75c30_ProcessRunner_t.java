 /* $Id: NativeRunner.java 32 2008-01-16 16:08:49Z abr $
  * $Revision: 32 $
  * $Date: 2008-01-16 16:08:49 +0000 (Wed, 16 Jan 2008) $
  * $Author: abr $
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
  * CVS:  $Id: NativeRunner.java 32 2008-01-16 16:08:49Z abr $
  */
 package dk.statsbiblioteket.util.console;
 
 import dk.statsbiblioteket.util.qa.QAInfo;
 
 import java.io.*;
 import java.util.*;
 
 
 /**
  * <p>Native command executor. Based on ProcessBuilder. Incorporates timeout for
  * spawned processes.</p>
  *
  * <p>Give the arguments, enviroment and starting directory when instantiating
  * this class. Then use either {@link #execute} or {@link #executeNoCollect}
  * to spawn the process.</p>
  *
  * <p>{@link #execute} automatically empties the output
  * and error streams, which can then be read after the process have returned.
  * {@link #executeNoCollect} does not, so they might be filled, and block the
  * process, until they are emptied again.</p>
  *
  * <p> This code is not yet entirely thread safe. Be sure to only call a given
  * processRunner from one thread, and do not reuse it. </p>
  */
 @QAInfo(level = QAInfo.Level.NORMAL,
         state = QAInfo.State.IN_DEVELOPMENT,
         author = "abr")
 public class ProcessRunner implements Runnable{
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
 
  //   private final Object locker = new Object();
 
     private long timeout = Long.MAX_VALUE;
 
     private boolean collect = true;
     private int maxOutput = 31000;
     private int maxError = 31000;
     private int return_code;
     private boolean timedOut;
 
     public ProcessRunner(){
         pb = new ProcessBuilder();
     }
 
     public ProcessRunner(String command) {
         this();
         List<String> l = new ArrayList<String>();
         l.add(command);
         setCommand(l);
     }
 
     public ProcessRunner(List<String> commands) {
         this();
         setCommand(commands);
     }
 
             @Deprecated
     public ProcessRunner(List<String> commands, Map<String,String> enviroment) {
         this();
         setCommand(commands);
         setEnviroment(enviroment);
     }
 
                                @Deprecated
     public ProcessRunner(List<String> commands, InputStream processInput ){
         this();
         setCommand(commands);
         setInputStream(processInput);
     }
 
             @Deprecated
     public ProcessRunner(List<String> commands, File startingDir){
         this();
         setCommand(commands);
         setStartingDir(startingDir);
     }
 
             @Deprecated
     public ProcessRunner(List<String> commands, Map<String,String> enviroment,
                          InputStream processInput){
         this();
         setCommand(commands);
         setEnviroment(enviroment);
         setInputStream(processInput);
     }
 
             @Deprecated
     public ProcessRunner(List<String> commands, InputStream processInput,
                          File startingDir){
         this();
         setCommand(commands);
         setInputStream(processInput);
         setStartingDir(startingDir);
     }
 
     /**
      * Run these commands in this directory, with this enviroment.
      * @param commands The commands to run
      * @param enviroment The enviroment the proces lives in
      * @param startingDir The starting dir for the commands
      */
             @Deprecated
     public ProcessRunner(List<String> commands, Map<String,String> enviroment,
                         File startingDir) {
         this();
         setCommand(commands);
         setEnviroment(enviroment);
         setStartingDir(startingDir);
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
             @Deprecated
     public ProcessRunner(List<String> commands, Map<String,String> enviroment,
                         InputStream processInput, File startingDir) {
         this();
         setCommand(commands);
         setEnviroment(enviroment);
         setInputStream(processInput);
         setStartingDir(startingDir);
     }
 
 
     public void setEnviroment(Map<String,String> enviroment){
         if (enviroment != null){
             Map<String,String> env = pb.environment();
             env.putAll(enviroment);
         }
     }
     public void setInputStream(InputStream processInput){
         this.processInput = processInput;
     }
 
     public void setStartingDir(File startingDir){
         pb.directory(startingDir);
     }
 
     public void setCommand(List<String> commands){
         pb.command(commands);
     }
 
     public void setTimeout(long timeout){
         this.timeout = timeout;
     }
 
     public void setCollection(boolean collect){
         this.collect = collect;
     }
 
     public void setErrorCollectionByteSize(int maxError){
         this.maxError = maxError;
     }
 
     public void setOutputCollectionByteSize(int maxOutput){
         this.maxOutput = maxOutput;
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
 
     public int getReturnCode(){
         return return_code;
     }
 
     public boolean isTimedOut() {
         return timedOut;
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
 
 
 
 
 
 
 
 
 
 
 
 
     public void run() {
 
 
         try {
             Process p = pb.start();
 
             if (collect){
                 ByteArrayOutputStream pOut =
                         collectProcessOutput(p.getInputStream(), this.maxOutput);
                 ByteArrayOutputStream pError =
                         collectProcessOutput(p.getErrorStream(), this.maxError);
                 return_code = execute(p);
                 waitForThreads();
                 processOutput = new ByteArrayInputStream(pOut.toByteArray());
                 processError = new ByteArrayInputStream(pError.toByteArray());
 
             } else {
                 processOutput = p.getInputStream();
                 processError = p.getErrorStream();
                 return_code = execute(p);
 
             }
 
         } catch (IOException e) {
             throw new RuntimeException("An io error occurred when running the command",e);
         }
     }
 
     private synchronized int execute(Process p){
         long startTime = System.currentTimeMillis();
         feedProcess(p, processInput);
         int return_value;
 
         while (true){
             //is the thread finished?
             try {
                 //then return
                 return_value =  p.exitValue();
                 break;
             }catch (IllegalThreadStateException e){
                 //not finished
             }
             //is the runtime exceeded?
             if (System.currentTimeMillis()-startTime > timeout){
                 //then return
                 p.destroy();
                 return_value = -1;
                 timedOut = true;
             }
             //else sleep again
             try {
                 wait (POLLING_INTERVAL);
             } catch (InterruptedException e) {
                 //just go on.
             }
 
         }
         return return_value;
 
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
         Thread t = new Thread() {
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
                   /*  // This seems ugly
                     throw new RuntimeException("Couldn't write input to " +
                             "process.", e);*/
                 }
             }
         };
 
         Thread.UncaughtExceptionHandler u =
                 new Thread.UncaughtExceptionHandler() {
                     public void uncaughtException(Thread t, Throwable e) {
                         //Might not be the prettiest solution...
                     }
                 };
         t.setUncaughtExceptionHandler(u);
         t.start();
 
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
         @Deprecated
     public synchronized int execute(int maxOutput,
                                     int maxError) throws Exception {
         setErrorCollectionByteSize(maxError);
         setOutputCollectionByteSize(maxOutput);
         run();
         return return_code;
     }
 
     /**
      * Execute the native commands. Standard and error output will not be
      * collected. It is the responsibility of the caller to empty these
      * OutputStreams, which can be accessed by {@link #getProcessOutput} and
      * {@link #getProcessError}.
      * @return the exit value from the native commands.
      * @throws Exception if execution of the native commands failed.
      */
         @Deprecated
     public synchronized int executeNoCollect() throws Exception {
        setCollection(false);
         run();
        return getReturnCode();
     }
 
 
     /**
      * Execute the native commands while blocking. Standard and error output will not be
      * collected. It is the responsibility of the caller to empty these
      * OutputStreams, which can be accessed by {@link #getProcessOutput} and
      * {@link #getProcessError}.<br>
      * Deprecated. Use {@link #setTimeout} and {@link #executeNoCollect()} instead.
      * @param maxRuntime the maximum number of milliseconds that the native
      *                   commands is allowed to run.
      * @return the exit value from the native commands.
      * @throws Exception if execution of the native commands failed or
      *                   timed out.
      */
     @Deprecated
     public synchronized int executeNoCollect(final long maxRuntime)
             throws Exception {
         setTimeout(maxRuntime);
         setCollection(false);
         run();
         return getReturnCode();
     }
 
 
 
 
     /**
      * Set the parameters of a NativeRunner. Deprecated, use the acesser methods
      * instead.
      * @param commands The commands to run
      * @param enviroment The enviroment the proces lives in
      * @param processInput An inputstream from which the commands to the proces
      * can be read. Will only be read once, so you cannot make something
      * interactive.
      * @param startingDir The starting dir for the commands
      */
     @Deprecated
     public void setParameters(List<String> commands,
                               Map<String,String> enviroment,
                               InputStream processInput, File startingDir) {
         setCommand(commands);
         setEnviroment(enviroment);
         setInputStream(processInput);
         setStartingDir(startingDir);
     }
 
 }
