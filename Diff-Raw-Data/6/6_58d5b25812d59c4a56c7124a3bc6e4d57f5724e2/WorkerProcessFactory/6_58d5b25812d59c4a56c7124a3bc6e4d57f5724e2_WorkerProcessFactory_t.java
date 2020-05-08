 // Copyright (C) 2004 Philip Aston
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
 
 package net.grinder.engine.agent;
 
 import java.io.File;
 import java.io.OutputStream;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.StringTokenizer;
 
 import net.grinder.common.GrinderProperties;
 import net.grinder.communication.CommunicationException;
 import net.grinder.communication.FanOutStreamSender;
 import net.grinder.communication.Message;
 import net.grinder.communication.StreamSender;
 import net.grinder.engine.common.EngineException;
 import net.grinder.engine.process.GrinderProcess;
 
 
 /**
  * Class that can start worker processes.
  *
  * @author Philip Aston
  * @version $Revision$
  */
 final class WorkerProcessFactory implements ProcessFactory {
 
   private final FanOutStreamSender m_fanOutStreamSender;
   private final Message m_initialisationMessage;
   private final List m_command;
  private final int m_commandGrinderIDIndex;
   private final String m_hostIDPrefix;
 
   public WorkerProcessFactory(GrinderProperties properties,
                               Properties systemProperties,
                               File alternateFile,
                               FanOutStreamSender fanOutStreamSender,
                               Message initialisationMessage) {
 
     m_fanOutStreamSender = fanOutStreamSender;
     m_initialisationMessage = initialisationMessage;
 
     m_command = new ArrayList();
     m_command.add(properties.getProperty("grinder.jvm", "java"));
 
     final String jvmArguments =
       properties.getProperty("grinder.jvm.arguments");
 
     if (jvmArguments != null) {
       // Really should allow whitespace to be escaped/quoted.
       final StringTokenizer tokenizer = new StringTokenizer(jvmArguments);
 
       while (tokenizer.hasMoreTokens()) {
         m_command.add(tokenizer.nextToken());
       }
     }
 
     // Pass through any "grinder" system properties.
     final Iterator systemPropertiesIterator =
       systemProperties.entrySet().iterator();
 
     while (systemPropertiesIterator.hasNext()) {
       final Map.Entry entry = (Map.Entry)systemPropertiesIterator.next();
       final String key = (String)entry.getKey();
       final String value = (String)entry.getValue();
 
       if (key.startsWith("grinder.")) {
         m_command.add("-D" + key + "=\"" + value + "\"");
       }
     }
 
     final String additionalClasspath =
       properties.getProperty("grinder.jvm.classpath", null);
 
     final String systemClasspath =
       systemProperties.getProperty("java.class.path");
 
     final StringBuffer classpath = new StringBuffer();
 
     if (additionalClasspath != null) {
       classpath.append(additionalClasspath);
     }
 
     if (additionalClasspath != null && systemClasspath != null) {
       classpath.append(File.pathSeparatorChar);
     }
 
     if (systemClasspath != null) {
       classpath.append(systemClasspath);
     }
 
     if (classpath.length() > 0) {
       m_command.add("-classpath");
       m_command.add(classpath.toString());
     }
 
     m_command.add(GrinderProcess.class.getName());
 
    m_commandGrinderIDIndex = m_command.size();
     m_command.add("<grinderID>"); // Place holder for grinder ID.
 
     if (alternateFile != null) {
       m_command.add(alternateFile.getPath());
     }
 
     m_hostIDPrefix = properties.getProperty("grinder.hostID", getHostName());
   }
 
   private String[] getCommandArray(String grinderID) {
    m_command.set(m_commandGrinderIDIndex, grinderID);
     return (String[])m_command.toArray(new String[0]);
   }
 
   /**
    * Package scope for the unit tests.
    */
   List getCommandList() {
     return m_command;
   }
 
   private String getHostName() {
     try {
       return InetAddress.getLocalHost().getHostName();
     }
     catch (UnknownHostException e) {
       return "UNNAMED HOST";
     }
   }
 
   public ChildProcess create(int processIndex,
                              OutputStream outputStream,
                              OutputStream errorStream) throws EngineException {
 
     final String grinderID = m_hostIDPrefix + "-" + processIndex;
 
     final ChildProcess process =
       new ChildProcess(grinderID, getCommandArray(grinderID),
                        outputStream, errorStream);
 
     final OutputStream processStdin = process.getStdinStream();
 
     try {
       new StreamSender(processStdin).send(m_initialisationMessage);
     }
     catch (CommunicationException e) {
       throw new EngineException("Failed to send initialisation message", e);
     }
 
     m_fanOutStreamSender.add(processStdin);
 
     return process;
   }
 
   public String getCommandLine() {
     final String[] commandArray = getCommandArray("<grinderID>");
 
     final StringBuffer buffer = new StringBuffer(commandArray.length * 10);
 
     boolean pastClass = false;
 
     for (int j = 0; j < commandArray.length; ++j) {
       if (j != 0) {
         buffer.append(" ");
       }
 
       final boolean jvmArgumentParameter =
         j > 0 && (commandArray[j - 1].equals("-jar") ||
                   commandArray[j - 1].equals("-classpath") ||
                   commandArray[j - 1].equals("-cp"));
 
       final boolean argument = pastClass || jvmArgumentParameter;
 
       if (argument) {
         buffer.append("'");
       }
 
       buffer.append(commandArray[j]);
 
       if (argument) {
         buffer.append("'");
       }
 
       if (j > 0 && !jvmArgumentParameter && !commandArray[j].startsWith("-")) {
         pastClass = true;
       }
     }
 
     return buffer.toString();
   }
 }
