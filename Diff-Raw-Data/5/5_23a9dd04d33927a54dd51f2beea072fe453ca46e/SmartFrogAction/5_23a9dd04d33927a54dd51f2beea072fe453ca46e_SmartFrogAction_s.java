 /*
  * JBoss, Home of Professional Open Source.
  * Copyright 2012, Red Hat, Inc., and individual contributors
  * as indicated by the @author tags. See the copyright.txt file in the
  * distribution for a full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package builder.smartfrog;
 
 import hudson.Launcher;
 import hudson.Proc;
 import hudson.model.Action;
 import hudson.model.BuildListener;
 import hudson.model.StreamBuildListener;
 import hudson.model.AbstractBuild;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.PrintStream;
 import java.nio.charset.Charset;
 import java.util.Vector;
 
 import org.kohsuke.stapler.StaplerRequest;
 import org.kohsuke.stapler.StaplerResponse;
 import org.kohsuke.stapler.framework.io.LargeText;
 
 import builder.smartfrog.util.Functions;
 import builder.smartfrog.util.LineFilterOutputStream;
 
 /**
  * 
  * @author Dominik Pospisil
  * @author vjuranek
  */
 public class SmartFrogAction implements Action, Runnable {
 
     private static final String NL = System.getProperty("line.separator");
 
     private final String host;
     private State state;
     private AbstractBuild<?, ?> build;
 
     private transient SmartFrogBuilder builder;
     private transient Proc proc;
     private transient Thread execThread;
     private transient Vector<SmartFrogActionListener> listeners = new Vector<SmartFrogActionListener>();
     private transient Launcher launcher;
     private transient BuildListener log;

     public SmartFrogAction(SmartFrogBuilder builder, String host) {
         this.builder = builder;
         this.host = host;
         this.state = State.STARTING;
     }
 
     public String getHost() {
         return host;
     }
 
     public void perform(final AbstractBuild<?, ?> build, final Launcher launcher) throws IOException,
             InterruptedException {
         this.build = build;
         this.launcher = launcher;
 
         String[] cl = builder.buildDaemonCommandLine(host, Functions.convertWsToCanonicalPath(build.getWorkspace()));
         log = new StreamBuildListener(new PrintStream(new SFFilterOutputStream(new FileOutputStream(getLogFile()))),
                 Charset.defaultCharset());
         proc = launcher.launch().cmds(cl).envs(build.getEnvironment(log)).pwd(build.getWorkspace()).start();
         execThread = new Thread(this, "SFDaemon - " + host);
         execThread.start();
     }
 
     public void run() {
        // wait for proccess to finish
         try {
             proc.join();
             setState(State.FINISHED);
         } catch (IOException ex) {
             setState(State.FAILED);
         } catch (InterruptedException ex) {
             setState(State.FAILED);
         } finally {
             //TODO reliable kill here  - JBQA 2006
             log.getLogger().close();
         }
     }
 
     public void interrupt() {
         String[] cl = builder.buildStopDaemonCommandLine(host);
         try {
             launcher.launch().cmds(cl).envs(build.getEnvironment(log)).pwd(build.getWorkspace()).join();
         } catch (IOException e) {
             e.printStackTrace();
             //TODO what else needs to be done?
         } catch (InterruptedException e){
             e.printStackTrace();
             //TODO what else needs to be done?
         }
         //TODO reliable kill here  - JBQA 2006
     }
 
     public State getState() {
         return state;
     }
     
     private void setState(State s) {
         if (this.getState() == s)
             return;
         this.state = s;
         for (SmartFrogActionListener l : listeners)
             l.stateChanged(this, getState());
     }
 
     public void addStateListener(SmartFrogActionListener l) {
         listeners.add(l);
     }
 
     public void doProgressiveLog(StaplerRequest req, StaplerResponse rsp) throws IOException {
         new LargeText(getLogFile(), !isBuilding()).doProgressText(req, rsp);
     }
 
     public File getLogFile() {
         return new File(build.getRootDir(), host + ".log");
     }
     
     public boolean isBuilding() {
         return (state != State.FAILED) && (state != State.FINISHED);
     }
 
     public String getIconFileName() {
         return "/plugin/org.jboss.hudson.smartfrog/icons/smartfrog24.png";
     }
 
     public String getDisplayName() {
         return "sfDaemon - " + host;
     }
 
     public String getUrlName() {
         return "console-" + host;
     }
 
     private class SFFilterOutputStream extends LineFilterOutputStream {
 
         private OutputStreamWriter os;
 
         public SFFilterOutputStream(OutputStream out) {
             super(out);
             os = new OutputStreamWriter(out);
         }
 
         protected void writeLine(String line) {
 
             if (line.startsWith("SmartFrog ready"))
                 setState(State.RUNNING);
 
             int idx = line.indexOf("[TerminateHook]");
             if (idx > -1) {
                 String compName = line.substring(line.indexOf('[', idx + 15) + 1);
                 compName = compName.substring(0, compName.indexOf(']'));
                 if (compName.endsWith(builder.getSfInstance().getName())) {
                     //TODO keep this info locally?
                     builder.componentTerminated(!line.contains("ABNORMAL"));
                 }
             }
 
             try {
                 os.write(line);
                 os.write(NL);
                 os.flush();
             } catch (IOException ioe) {
 
             }
         }
     }
 
     public enum State {
         STARTING, RUNNING, FINISHED, FAILED
     };
 }
