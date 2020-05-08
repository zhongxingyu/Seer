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
 
import builder.smartfrog.SmartFrogAction.State;

 import hudson.Extension;
 import hudson.FilePath;
 import hudson.Launcher;
 import hudson.matrix.MatrixConfiguration;

import hudson.model.AbstractBuild;
import hudson.model.Build;
 import hudson.model.BuildListener;
 import hudson.model.Result;
 import hudson.model.Descriptor;
import hudson.model.JDK;
 import hudson.model.Project;

 import hudson.tasks.Builder;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import net.sf.json.JSONObject;
 
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.StaplerRequest;
 
 import builder.smartfrog.SmartFrogAction.State;
 
 /**
  * SmartFrog Hudson/Jenkins plugin main.
  * 
  * @author <a href="mailto:rhusar@redhat.com">Radoslav Husar</a>
  * @author vjuranek
  * 
  */
 public class SmartFrogBuilder extends Builder implements SmartFrogActionListener {
 
     public static final String ENV_SF_HOME = "SFHOME";
     public static final String ENV_SF_USER_HOME = "SFUSERHOME";
     public static final long HEARTBEAT_PERIOD = 10000;
     
     private String smartFrogName;
     private String deployHost;
     private String hosts;
     private String sfUserHome;
     private String sfUserHome2;
     private String sfUserHome3;
     private String sfUserHome4;
     private String sfOpts;
     private String sfIni;
     private boolean useAltIni;
     private String scriptName;
     private String scriptSource;
     private String scriptPath;
     private String scriptContent;
     
     private Project project;
     private String exportMatrixAxes = "";
     private String defaultScriptPath;
     
     private transient BuildListener listener;
     private transient boolean componentTerminated = false;
     private transient boolean terminatedNormally;
 
     @DataBoundConstructor
     public SmartFrogBuilder(String smartFrogName, String deployHost, String hosts, String sfUserHome, String sfUserHome2, 
             String sfUserHome3, String sfUserHome4, String sfOpts, boolean useAltIni, String sfIni, String scriptName,
             String scriptSource, String scriptPath, String scriptContent){
         this.smartFrogName = smartFrogName;
         this.deployHost = deployHost; 
         this.hosts = hosts; 
         this.sfUserHome = sfUserHome;
         this.sfUserHome2 = sfUserHome2;
         this.sfUserHome3 = sfUserHome3;
         this.sfUserHome4 = sfUserHome4;
         this.sfOpts = sfOpts;
         this.useAltIni = useAltIni;
         this.sfIni = sfIni;
         this.scriptName = scriptName;
         this.scriptSource = scriptSource;
         this.scriptPath = scriptPath;
         this.scriptContent = scriptContent;
     }
     
     public String getSmartFrogName() {
         return smartFrogName;
     }
 
     public String getDeployHost() {
         return deployHost;
     }
 
     public String getHosts() {
         return hosts;
     }
 
     public String getSfUserHome() {
         return sfUserHome;
     }
 
     // Additional SFUSERHOMEs, since 1 is used for Support Libs (terminate hooks).
     public String getSfUserHome2() {
         return sfUserHome2;
     }
 
     public String getSfUserHome3() {
         return sfUserHome3;
     }
     
     public String getSfUserHome4() {
         return sfUserHome4;
     }
 
     public String getSfOpts() {
         return sfOpts;
     }
 
     public String getSfIni() {
         return sfIni;
     }
 
     public boolean isUseAltIni() {
         return useAltIni;
     }
 
     public String getScriptName() {
         return scriptName;
     }
 
     public String getScriptSource() {
         return scriptSource;
     }
 
     public String getScriptPath() {
         return scriptPath;
     }
 
     public String getScriptContent() {
         return scriptContent;
     }
 
     @Override
     public boolean perform(AbstractBuild<?,?> build, Launcher launcher, BuildListener listener)
             throws InterruptedException {
 
         componentTerminated = false;
 
         this.listener = listener;
         String[] hostList = hosts.split("[ \t]+");
 
         this.project = (Project)build.getProject();
 
         try {
             if (scriptSource.equals("content")) {
                 createDefaultScriptFile(scriptContent);
             }
         } catch (IOException ioe) {
             listener.getLogger().println("Could not get canonical path to workspace:" + ioe);
             ioe.printStackTrace();
             build.setResult(Result.FAILURE);
             return false;
         }
 
         /**
          * Export Matrix parameters if matrix project block
          * 
          * If problems occur, then blame:
          * 
          * @author rhusar@redhat.com
          * @version 2009-07-08
          */
         if (project instanceof MatrixConfiguration) {
             MatrixConfiguration matrixConfProject = (MatrixConfiguration) project;
 
             // Get the matrix configuration.
             Map<String, String> matrixConfMap = null;
             matrixConfMap = matrixConfProject.getCombination();
 
             exportMatrixAxes = " ";
 
             // Iterate and only "SF_" prefixed variables.
             for (Map.Entry<String, String> entry : matrixConfMap.entrySet()) {
                 if (entry.getKey().startsWith("SF_")) {
                     exportMatrixAxes = exportMatrixAxes + "export " + entry.getKey() + "=" + entry.getValue() + "; ";
                 }
             }
             System.out.println("SFPlugin: from matrix: " + matrixConfMap + "; exporting: " + exportMatrixAxes);
         }
 
         SmartFrogAction[] sfActions = new SmartFrogAction[hostList.length];
         // start daemons
         for (int k = 0; k < hostList.length; k++) {
             String host = hostList[k];
             SmartFrogAction a = new SmartFrogAction(this, host);
             build.addAction(a);
             a.addStateListener(this);
             sfActions[k] = a;
             a.perform(build, launcher);
         }
 
         // wait until all daemons are ready
         synchronized (this) {
             boolean allStarted = false;
             do {
                 allStarted = true;
                 for (SmartFrogAction a : sfActions) {
                     if (a.getState() != SmartFrogAction.State.RUNNING) {
                         if (a.getState() == SmartFrogAction.State.FAILED) {
                             listener.getLogger().println("SmartFrog deamon on host " + a.getHost() + " failed.");
                             build.setResult(Result.FAILURE);
                             for (SmartFrogAction act : sfActions) {
                                 act.interrupt();
                             }
                             return false;
                         }
                         allStarted = false;
                         break;
                     }
                 }
                 if (allStarted) {
                     break;
                 }
                 try {
                     wait();
                 } catch (InterruptedException ioe) {
                     for (SmartFrogAction a : sfActions) {
                         a.interrupt();
                     }
                     build.setResult(Result.FAILURE);
                     return false;
                 }
             } while (allStarted == false);
         }
 
         // deploy terminate hook
         String[] deploySLCl = buildDeployCommandLine(deployHost, getSupportDescPath(), "terminate-hook");
         try {
             int status = launcher.launch(deploySLCl, build.getEnvVars(), listener.getLogger(),
                     build.getParent().getWorkspace()).join();
             if (status != 0) {
                 listener.getLogger().println("Deployment of support component failed.");
                 build.setResult(Result.FAILURE);
                 for (SmartFrogAction act : sfActions) {
                     act.interrupt();
                 }
                 return false;
             }
         } catch (IOException ioe) {
             build.setResult(Result.FAILURE);
             for (SmartFrogAction act : sfActions) {
                 act.interrupt();
             }
             return false;
         }
 
         // deploy script
         String[] deployCl = buildDeployCommandLine(deployHost);
         try {
             int status = launcher.launch(deployCl, build.getEnvVars(), listener.getLogger(),
                     build.getParent().getWorkspace()).join();
             if (status != 0) {
                 listener.getLogger().println("Deployment failed.");
                 build.setResult(Result.FAILURE);
                 for (SmartFrogAction act : sfActions) {
                     act.interrupt();
                 }
                 return false;
             }
         } catch (IOException ioe) {
             build.setResult(Result.FAILURE);
             for (SmartFrogAction act : sfActions) {
                 act.interrupt();
             }
             return false;
         }
 
         // wait for component termination
         synchronized (this) {
             while (!componentTerminated) {
                 try {
                     wait();
                 } catch (InterruptedException ioe) {
                     for (SmartFrogAction a : sfActions) {
                         a.interrupt();
                     }
                     build.setResult(Result.ABORTED);
                     return false;
                 }
             }
         }
 
         for (SmartFrogAction act : sfActions) {
             act.interrupt();
         }
         build.setResult(terminatedNormally ? Result.SUCCESS : Result.FAILURE);
 
         return true;
     }
 
     @Override
     public DescriptorImpl getDescriptor() {
         return (DescriptorImpl) super.getDescriptor();
     }
 
     @Extension
     public static final class DescriptorImpl extends Descriptor<Builder> {
 
         private List<SmartFrogInstance> smartfrogInstances = new ArrayList<SmartFrogInstance>();
 
         public DescriptorImpl() {
             super(SmartFrogBuilder.class);
             load();
         }
 
         public List<SmartFrogInstance> getSmartfrogInstances() {
             return smartfrogInstances;
         }
         
         public String getDisplayName() {
             return "Deploy SmartFrog component";
         }
 
         @Override
         public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
             smartfrogInstances.clear();
             smartfrogInstances.addAll(req.bindJSONToList(SmartFrogInstance.class, json.get("smartfrogInstances")));
             save();
             return super.configure(req, json);
         }
/*
        @Override                                                                                                               
        public SmartFrogBuilder newInstance(StaplerRequest req) throws hudson.model.Descriptor.FormException {                                                                                                                                    
           SmartFrogBuilder sb = new SmartFrogBuilder();                                                                        
           req.bindParameters(sb, "sb.");                                                                                       
                                                                                                                                
           return sb;                                                                                                           
        }     
*/        
     }
 
     protected String[] buildDaemonCommandLine(String host) {
         String iniPath = useAltIni ? sfIni : getSfInstance().getPath() + "/bin/default.ini";
         return new String[] { "/bin/bash", "-xe", getSfInstance().getSupport() + "/runSF.sh", host,
                 getSfInstance().getPath(), sfUserHome, getSupportLibPath(), sfUserHome2, sfUserHome3, sfUserHome4,
                 getWorkspacePath(), getSfOpts(), iniPath, exportMatrixAxes };
     }
 
     protected String[] buildStopDaemonCommandLine(String host) {
         return new String[] { "/bin/bash", "-xe", getSfInstance().getSupport() + "/stopSF.sh", host,
                 getSfInstance().getPath(), sfUserHome };
     }
 
     protected String[] buildDeployCommandLine(String host) {
         String deployPath;
         if ((scriptSource != null) && scriptSource.equals("path")) {
             deployPath = scriptPath;
         } else {
             deployPath = getDefaultScriptFile().getPath();
         }
         return buildDeployCommandLine(host, deployPath);
     }
 
     protected String[] buildDeployCommandLine(String host, String deployPath) {
         return buildDeployCommandLine(host, deployPath, scriptName);
     }
 
     protected String[] buildDeployCommandLine(String host, String deployPath, String componentName) {
         return new String[] { "/bin/bash", "-xe", getSfInstance().getSupport() + "/deploySF.sh", host,
                 getSfInstance().getPath(), sfUserHome, getSupportLibPath(), sfUserHome2, sfUserHome3, sfUserHome4,
                 deployPath, componentName, getWorkspacePath(), exportMatrixAxes };
     }
 
     protected SmartFrogInstance getSfInstance() {
         for (SmartFrogInstance i : getDescriptor().getSmartfrogInstances()) {
             if (i.getName().equals(getSmartFrogName())) {
                 return i;
             }
         }
         return null;
     }
 
     private void createDefaultScriptFile(String command) throws InterruptedException, IOException {
         FilePath path = project.getWorkspace().createTextTempFile("deployScript", ".sf", command, true);
         defaultScriptPath = path.getRemote();
     }
 
     private File getDefaultScriptFile() {
         return new File(defaultScriptPath);
     }
 
  
     public synchronized void stateChanged(SmartFrogAction action, State newState) {
         notifyAll();
         if (newState == SmartFrogAction.State.RUNNING) {
             listener.getLogger().println("SmartFrog deamon on host " + action.getHost() + " is running.");
         }
     }
 
     private String getWorkspacePath() {
         try {
             return new File(project.getWorkspace().toURI()).getCanonicalPath();
         } catch (IOException ioe) {
             ioe.printStackTrace();
         } catch (InterruptedException ie) {
             ie.printStackTrace();
         }
         return null;
     }
 
     private String getSupportLibPath() {
         return getSfInstance().getSupport();
     }
 
     private String getSupportDescPath() {
         return getSfInstance().getSupport() + "/hudson-support.sf";
     }
 
     public synchronized void componentTerminated(boolean normal) {
         this.componentTerminated = true;
         this.terminatedNormally = normal;
         notifyAll();
     }
 }
