 // Copyright (C) 2000 Paco Gomez
 // Copyright (C) 2000, 2001, 2002, 2003 Philip Aston
 // All rights reserved.
 //
 // This file is part of The Grinder software distribution. Refer to
 // the file LICENSE which is part of The Grinder distribution for
 // licensing details. The Grinder distribution is available on the
 // Internet at http://grinder.sourceforge.net/
 //
 // THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 // "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 // LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 // FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 // REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 // INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 // (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 // SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 // HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 // STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 // ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 // OF THE POSSIBILITY OF SUCH DAMAGE.
 
 package net.grinder;
 
 import java.io.File;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.StringTokenizer;
 
 import net.grinder.common.GrinderBuild;
 import net.grinder.common.GrinderException;
 import net.grinder.common.GrinderProperties;
 import net.grinder.engine.agent.LauncherThread;
 import net.grinder.engine.process.GrinderProcess;
 
 
 /**
  * This is the entry point of The Grinder agent process.
  *
  * @author Paco Gomez
  * @author Philip Aston
  * @version $Revision$
  */
 public final class Grinder {
   /**
    * The Grinder agent process entry point.
    *
    * @param args Command line arguments.
    * @exception GrinderException If an error occurred.
    */
   public static void main(String[] args) throws GrinderException {
     if (args.length > 1) {
       System.err.println("Usage: java " + Grinder.class.getName() +
                          " [alternatePropertiesFilename]");
       System.exit(1);
     }
 
     new Grinder(args.length != 0 ? new File(args[0]) : null).run();
   }
 
   private final File m_alternateFile;
 
   private Grinder(File alternateFile) {
     m_alternateFile = alternateFile;
   }
 
   /**
    * Run the Grinder agent process.
    *
    * @exception GrinderException if an error occurs
    */
   protected void run() throws GrinderException {
     boolean ignoreInitialSignal = false;
 
     while (true) {
       final GrinderProperties properties =
         new GrinderProperties(m_alternateFile);
 
       final ArrayList command = new ArrayList();
 
       command.add(properties.getProperty("grinder.jvm", "java"));
 
       final String jvmArguments =
         properties.getProperty("grinder.jvm.arguments");
 
       if (jvmArguments != null) {
         // Really should allow whitespace to be
         // escaped/quoted.
         final StringTokenizer tokenizer =
           new StringTokenizer(jvmArguments);
 
         while (tokenizer.hasMoreTokens()) {
           command.add(tokenizer.nextToken());
         }
       }
 
       // Pass through any "grinder" system properties.
       final Iterator systemProperties =
         System.getProperties().entrySet().iterator();
 
       while (systemProperties.hasNext()) {
         final Map.Entry entry = (Map.Entry)systemProperties.next();
         final String key = (String)entry.getKey();
         final String value = (String)entry.getValue();
 
         if (key.startsWith("grinder.")) {
           command.add("-D" + key + "=" + value);
         }
       }
 
       final String additionalClasspath =
         properties.getProperty("grinder.jvm.classpath", null);
 
       final String classpath =
        (additionalClasspath != null ? additionalClasspath + ";" : "") +
         System.getProperty("java.class.path");
 
      classpath.replace(';', File.pathSeparatorChar);
      classpath.replace(':', File.pathSeparatorChar);

       if (classpath.length() > 0) {
         command.add("-classpath");
         command.add(classpath);
       }
 
       if (ignoreInitialSignal) {
         command.add(
           "-D" + GrinderProcess.DONT_WAIT_FOR_SIGNAL_PROPERTY_NAME +
           "=true");
       }
 
       command.add(GrinderProcess.class.getName());
 
       final String hostIDString =
         properties.getProperty("grinder.hostID", getHostName());
 
       final int grinderIDIndex = command.size();
       command.add("");    // Place holder for grinder ID.
 
       if (m_alternateFile != null) {
         command.add(m_alternateFile.getPath());
       }
 
       final int numberOfProcesses =
         properties.getInt("grinder.processes", 1);
 
       final LauncherThread[] threads =
         new LauncherThread[numberOfProcesses];
 
       final String[] stringArray = new String[0];
 
       for (int i = 0; i < numberOfProcesses; i++) {
         final String grinderID = hostIDString + "-" + i;
 
         final String[] commandArray =
           (String[])command.toArray(stringArray);
 
         commandArray[grinderIDIndex] = grinderID;
 
         threads[i] = new LauncherThread(grinderID, commandArray);
         threads[i].start();
       }
 
       final String version = GrinderBuild.getVersionString();
 
       System.out.println("The Grinder version " + version + " started");
 
       int combinedExitStatus = 0;
 
       for (int i = 0; i < numberOfProcesses;) {
         try {
           threads[i].join();
 
           final int exitStatus = threads[i].getExitStatus();
 
           if (exitStatus > 0) { // Not an error
             if (combinedExitStatus == 0) {
               combinedExitStatus = exitStatus;
             }
             else if (combinedExitStatus != exitStatus) {
               System.out.println(
                 "WARNING, threads disagree on exit status");
             }
           }
 
           i++;
         }
         catch (InterruptedException e) {
           // Ignore.
         }
       }
 
       System.out.println("The Grinder version " + version + " finished");
 
       if (combinedExitStatus == GrinderProcess.EXIT_START_SIGNAL) {
         System.out.println("Start signal received");
         ignoreInitialSignal = true;
       }
       else if (combinedExitStatus == GrinderProcess.EXIT_RESET_SIGNAL) {
         System.out.println("Reset signal received");
         ignoreInitialSignal = false;
       }
       else {
         break;
       }
 
       System.out.println();
     }
   }
 
   private String getHostName() {
     try {
       return InetAddress.getLocalHost().getHostName();
     }
     catch (UnknownHostException e) {
       return "UNNAMED HOST";
     }
   }
 }
 
