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
 
 import hudson.Extension;
 import hudson.Launcher;
 import hudson.matrix.Combination;
 import hudson.matrix.MatrixConfiguration;
 import hudson.model.BuildListener;
 import hudson.model.Result;
 import hudson.model.AbstractBuild;
 import hudson.model.Descriptor;
 import hudson.tasks.Builder;
 import hudson.util.ListBoxModel;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import net.sf.json.JSONObject;
 
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.StaplerRequest;
 
 import builder.smartfrog.SmartFrogAction.State;
 import builder.smartfrog.util.Functions;
 
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
 
     private SmartFrogInstance sfInstance;
     private String deployHost;
     private String hosts;
     private String sfUserHome;
     private String sfUserHome2;
     private String sfUserHome3;
     private String sfUserHome4;
     private String sfOpts;
     private String sfIni;
     private boolean useAltIni;
     private ScriptSource scriptSource;
 
     // private transient BuildListener listener;
     private transient String exportMatrixAxes = "";
     private transient boolean componentTerminated = false;
     private transient boolean terminatedNormally;
 
     @DataBoundConstructor
     public SmartFrogBuilder(String smartFrogName, String deployHost, String hosts, String sfUserHome,
             String sfUserHome2, String sfUserHome3, String sfUserHome4, String sfOpts, boolean useAltIni, String sfIni,
             ScriptSource scriptSource) {
         this.sfInstance = getDescriptor().getSFInstanceByName(smartFrogName);
         this.deployHost = deployHost;
         this.hosts = hosts;
         this.sfUserHome = sfUserHome;
         this.sfUserHome2 = sfUserHome2;
         this.sfUserHome3 = sfUserHome3;
         this.sfUserHome4 = sfUserHome4;
         this.sfOpts = sfOpts;
         this.useAltIni = useAltIni;
         this.sfIni = sfIni;
         this.scriptSource = scriptSource;
     }
 
     public SmartFrogInstance getSfInstance() {
         return sfInstance;
     }
 
     public String getSmartFrogName() {
         return sfInstance.getName();
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
 
     public ScriptSource getScriptSource() {
         return scriptSource;
     }
 
     @Override
     public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
             throws InterruptedException {
         componentTerminated = false;
 
         // check if SF script exists or create new one
         if (!prepareScript(build, listener))
             return false;
 
         // Export Matrix parameters if matrix project block
         if (build.getProject() instanceof MatrixConfiguration)
             exportMatrixAxes = exportMatrixAxes(build);
 
         // create daemons
         SmartFrogAction[] sfActions = createDaemons(build, launcher);
         // wait until all daemons are ready
         if(!daemonsReady(build, listener, sfActions))
             return false;
         // deploy terminate hook
         if(!deployTerminateHook(build, launcher, listener, sfActions))
             return false;
         // deploy script
         if(!deployScript(build, launcher, listener, sfActions))
             return false;
         // wait for component termination
         if(!waitForCompletion(build, sfActions))
             return false;
         // terminte daemons
         for (SmartFrogAction act : sfActions) {
             act.interrupt();
         }
         
         build.setResult(terminatedNormally ? Result.SUCCESS : Result.FAILURE);
         return true;
     }
 
     private boolean prepareScript(AbstractBuild<?, ?> build, BuildListener listener) throws InterruptedException {
         if (scriptSource instanceof StringScriptSource) {
             try {
                 ((StringScriptSource) scriptSource).createDefaultScriptFile(build);
             } catch (IOException ioe) {
                 listener.getLogger().println("[SmartFrog] FAILURE: Could not get canonical path to workspace:" + ioe);
                 ioe.printStackTrace();
                 build.setResult(Result.FAILURE);
                 return false;
             }
         }
         if (scriptSource instanceof FileScriptSource) {
             File f = new File(((FileScriptSource) scriptSource).getScriptPath());
             if (!f.exists()) {
                 listener.getLogger().println(
                         "[SmartFrog] FAILURE: Script file " + f.getAbsolutePath() + " doesn't exists!");
                 build.setResult(Result.FAILURE);
                 return false;
             }
         }
         return true;
     }
 
     private String exportMatrixAxes(AbstractBuild<?, ?> build) {
         String exportedMatrixAxes = " ";
         MatrixConfiguration matrix = (MatrixConfiguration) build.getProject();
         Combination combinations = matrix.getCombination();
         // Add only "SF_" prefixed variables.
         for (Map.Entry<String, String> entry : combinations.entrySet()) {
             if (entry.getKey().startsWith("SF_")) {
                 exportedMatrixAxes = exportedMatrixAxes + "export " + entry.getKey() + "=" + entry.getValue() + "; ";
             }
         }
         return exportedMatrixAxes;
     }
 
     private SmartFrogAction[] createDaemons(AbstractBuild<?, ?> build, Launcher launcher) {
         String[] hostList = hosts.split("[ \t]+");
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
         return sfActions;
     }
 
     private synchronized boolean daemonsReady(AbstractBuild<?, ?> build, BuildListener listener, SmartFrogAction[] sfActions) {
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
         return true;
     }
     
     private boolean deployTerminateHook(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener, SmartFrogAction[] sfActions) 
         throws InterruptedException {
         
         String[] deploySLCl = buildDeployCommandLine(deployHost, sfInstance.getSupportScriptPath(), "terminate-hook");
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
         return true;
     }
 
     private boolean deployScript(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener, SmartFrogAction[] sfActions) 
         throws InterruptedException {
         String[] deployCl = buildDeployCommandLine(deployHost, scriptSource.getScriptName(),
                 Functions.convertWsToCanonicalPath(build.getWorkspace()));
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
         return true;
     }
     
     private synchronized boolean waitForCompletion(AbstractBuild<?, ?> build, SmartFrogAction[] sfActions){
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
         return true;
     }
     
     protected String[] buildDaemonCommandLine(String host, String workspace) {
         String iniPath = useAltIni ? sfIni : sfInstance.getPath() + "/bin/default.ini";
         return new String[] { "/bin/bash", "-xe", sfInstance.getSupport() + "/runSF.sh", host, sfInstance.getPath(),
                 sfUserHome, sfInstance.getSupport(), sfUserHome2, sfUserHome3, sfUserHome4, workspace, getSfOpts(),
                 iniPath, exportMatrixAxes };
     }
 
     protected String[] buildStopDaemonCommandLine(String host) {
         return new String[] { "/bin/bash", "-xe", sfInstance.getSupport() + "/stopSF.sh", host, sfInstance.getPath(),
                 sfUserHome };
     }
 
     protected String[] buildDeployCommandLine(String host, String componentName, String workspace) {
         String defaultScriptPath = scriptSource != null ? scriptSource.getDefaultScriptPath() : "";
         return new String[] { "/bin/bash", "-xe", sfInstance.getSupport() + "/deploySF.sh", host, sfInstance.getPath(),
                 sfUserHome, sfInstance.getSupport(), sfUserHome2, sfUserHome3, sfUserHome4, defaultScriptPath,
                 componentName, workspace, exportMatrixAxes };
     }
 
     public synchronized void stateChanged(SmartFrogAction action, State newState) {
         notifyAll();
         //if (newState == SmartFrogAction.State.RUNNING) {
         //    listener.getLogger().println("SmartFrog deamon on host " + action.getHost() + " is running.");
         //}
     }
 
     public synchronized void componentTerminated(boolean normal) {
         this.componentTerminated = true;
         this.terminatedNormally = normal;
         notifyAll();
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
 
         public SmartFrogInstance getSFInstanceByName(String name) {
             for (SmartFrogInstance sf : smartfrogInstances) {
                 if (sf.getName().equals(name))
                     return sf;
             }
             return null;
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
 
         public ListBoxModel doFillSmartFrogNameItems() {
             ListBoxModel lb = new ListBoxModel();
             for (SmartFrogInstance sf : smartfrogInstances) {
                 lb.add(sf.getName(), sf.getName());
             }
             return lb;
         }
 
     }
 
 }
