 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.colar.netbeans.fan.actions;
 
 import javax.swing.JOptionPane;
 import net.colar.netbeans.fan.FanLanguage;
 import net.colar.netbeans.fan.FanUtilities;
 import net.colar.netbeans.fan.platform.FanPlatform;
 import net.colar.netbeans.fan.platform.FanPlatformSettings;
 import net.colar.netbeans.fan.project.FanBuildFileHelper;
 import net.colar.netbeans.fan.project.FanProject;
 import net.colar.netbeans.fan.project.FanProjectProperties;
 import org.netbeans.api.debugger.jpda.DebuggerStartException;
 import org.netbeans.api.debugger.jpda.JPDADebugger;
 import org.netbeans.api.extexecution.ExecutionDescriptor;
 import org.netbeans.api.project.Project;
 import org.netbeans.api.project.ui.OpenProjects;
 import org.openide.filesystems.FileObject;
 import org.openide.filesystems.FileUtil;
 import org.openide.loaders.DataObject;
 import org.openide.nodes.Node;
 import org.openide.util.Lookup;
 import org.openide.windows.TopComponent;
 
 /**
  * Command / Action Base
  * Copied/modified from Python module
  */
 public abstract class FanAction
 {
 
     public final ExecutionDescriptor descriptor = new ExecutionDescriptor().frontWindow(true).controllable(true).inputVisible(true).showProgress(true).showSuspended(true);
     private final FanProject project;
     private FanJpdaThread jpdaThread = null;
 
     public FanAction(FanProject project)
     {
         this.project = project;
         assert project != null;
     }
 
     public abstract String getCommandId();
 
     public abstract void invokeAction(Lookup context) throws IllegalArgumentException;
 
     public abstract boolean isActionEnabled(Lookup context) throws IllegalArgumentException;
 
     public boolean asyncCallRequired()
     {
         return true;
     }
 
     public boolean saveRequired()
     {
         return true;
     }
 
     public final FanProject getProject()
     {
         return project;
     }
 
     public Node[] getSelectedNodes()
     {
         return TopComponent.getRegistry().getCurrentNodes();
     }
 
     protected void showLaunchError(String message)
     {
         JOptionPane.showMessageDialog(null, message, "Fantom Launch Error", JOptionPane.ERROR_MESSAGE);
 
     }
 
     protected FanExecution buildPodAction(Lookup lookup)
     {
         // run default target (build)
         return buildAction(lookup, "");
     }
 
     protected FanExecution cleanPodAction(Lookup lookup)
     {
         // run default target (build)
         return buildAction(lookup, "clean");
     }
 
     protected FanExecution testPodAction(Lookup lookup)
     {
         // run default target (build)
         return buildAction(lookup, "test");
     }
 
     protected FanExecution customBuildAction(Lookup lookup, String buildTarget)
     {
         // run default target (build)
         return buildAction(lookup, buildTarget);
     }
 
     protected FanExecution runTalesProject(Lookup lookup, boolean debug)
     {
         return runPodAction(lookup, debug, true);
     }
 
     protected FanExecution runPodAction(Lookup lookup, boolean debug)
     {
         return runPodAction(lookup, debug, false);
     }
 
     private FanExecution runPodAction(Lookup lookup, boolean debug, boolean tales)
     {
         FileObject file = findTargetProject(lookup);
         if (file != null)
         {
             String podName = file.getName();
             String path = FileUtil.toFile(file.getParent()).getAbsolutePath();
             // see if user specified custom main method
             String target = podName + "::" + FanProjectProperties.getProperties(project).getMainMethod();
             if (target == null || target.length() == 0)
             {
                 // otherwise use default
                 target = podName + "::" + "Main" + "." + "main";
             }
             FanExecution fanExec = new FanExecution();
             fanExec.setDisplayName((tales ? "Tales " : "") + getProjectName(lookup));
             if (tales)
             {
                 // execute IN project directory
                 fanExec.setWorkingDirectory(FileUtil.toFile(file).getAbsolutePath());
             } else
             {
                 // execute in parent dir.
                 fanExec.setWorkingDirectory(path);
             }
 
             FanPlatform.getInstance().buildFanCall(fanExec, debug);
 
             if (tales)
             {
                 String talesPath = "NO_TALES_PATH";
                 if (FanPlatform.getInstance().isTalesPresent())
                 {
                     talesPath = FanPlatform.getInstance().getTalesHome().getPath();
                 }
                 fanExec.addEnvVar("FAN_ENV", "util::PathEnv");
                 fanExec.addEnvVar("FAN_ENV_PATH", talesPath);
             }
 
             fanExec.addCommandArg(FanPlatform.FAN_CLASS);
             if (tales)
             {
                 fanExec.addCommandArg(FanPlatform.FAN_TALES_POD_NAME);
                fanExec.addCommandArg(FileUtil.toFile(file).getAbsolutePath());
                 fanExec.addCommandArg(FanPlatform.FAN_TALES_RUN_CMD);
             } else
             {
                 fanExec.addCommandArg(target);
             }
             if (debug)
             {
                 if (jpdaThread != null && jpdaThread.isAlive())
                 {
                     jpdaThread.shutdown();
                     jpdaThread.interrupt();
                 }
                 jpdaThread = new FanJpdaThread();
                 jpdaThread.start();
             }
 
             return fanExec;
         }
         return null;
     }
 
     protected FanExecution testFileAction(Lookup lookup, String testClass)
     {
         FileObject file = findTargetProject(lookup);
         if (file != null)
         {
             String podName = file.getName();
             String path = FileUtil.toFile(file.getParent()).getAbsolutePath();
             // see if user specified custom main method
             String target = podName + (testClass == null ? "" : "::" + testClass);
             FanExecution fanExec = new FanExecution();
             fanExec.setDisplayName(getProjectName(lookup));
             fanExec.setWorkingDirectory(path);
             FanPlatform.getInstance().buildFanCall(fanExec, false);
             fanExec.addCommandArg(FanPlatform.FANT_CLASS);
             fanExec.addCommandArg(target);
             return fanExec;
         }
         return null;
     }
 
     public String getProjectName(Lookup lookup)
     {
         return findTargetProject(lookup).getName();
     }
 
     /**
      * Call the action using internal call to Fan java class (not the wrapper scripts).
      * @param lookup
      * @param target
      * @return
      */
     private FanExecution buildAction(Lookup lookup, String target)
     {
         // if default target "", see what user chose in props;
         if (target.equals(""))
         {
             String newTarget = FanProjectProperties.getProperties(project).getBuildTarget();
             if (newTarget != null)
             {
                 target = newTarget;
             }
         }
         FileObject file = findTargetProject(lookup);
         if (file != null)
         {
             FileObject buildFile = file.getFileObject(FanBuildFileHelper.BUILD_FILE);
             if (buildFile != null)
             {
                 String path = FileUtil.toFile(file).getAbsolutePath();
                 FanExecution fanExec = new FanExecution();
                 fanExec.setDisplayName(getProjectName(lookup));
                 fanExec.setWorkingDirectory(path);
                 FanPlatform.getInstance().buildFanCall(fanExec);
                 fanExec.addCommandArg(FanPlatform.FAN_CLASS);
                 fanExec.addCommandArg(FanBuildFileHelper.BUILD_FILE);
                 fanExec.addCommandArg(target);
                 return fanExec;
             }
         }
         return null;
     }
 
     protected FanExecution runFileAction()
     {
         Node[] activatedNodes = getSelectedNodes();
         FileObject file = null;
         DataObject gdo = activatedNodes[0].getLookup().lookup(DataObject.class);
         if (gdo != null && gdo.getPrimaryFile() != null)
         {
             file = gdo.getPrimaryFile();
         }
         if (file != null && file.getMIMEType().equals(FanLanguage.FAN_MIME_TYPE))
         {
             String path = FileUtil.toFile(file.getParent()).getAbsolutePath();
             String script = FileUtil.toFile(file).getAbsolutePath();
 
             FanExecution fanExec = new FanExecution();
             fanExec.setDisplayName(file.getName());
             fanExec.setWorkingDirectory(path);
             FanPlatform.getInstance().buildFanCall(fanExec);
             fanExec.addCommandArg(FanPlatform.FAN_CLASS);
             fanExec.addCommandArg(script);
 
             return fanExec;
         }
         return null;
     }
 
     /**
      * TODO If the user "selects" a project node it will run that one rather
      * than the 'actual' main project (when running main project)\
      * But to avoid that i would have to setup separate actions for running a project from contextual menu
      * vs running it from "run main project" button .... which seems a bit silly.
      * @param lookup
      * @return
      */
     private FileObject findTargetProject(Lookup lookup)
     {
         FileObject file = null;
         Node[] activatedNodes = getSelectedNodes();
         if (activatedNodes != null && activatedNodes.length > 0)
         {
             DataObject gdo = activatedNodes[0].getLookup().lookup(DataObject.class);
             if (gdo != null && gdo.getPrimaryFile() != null)
             {
                 file = gdo.getPrimaryFile();
                 if (FanProject.isProject(file))
                 {
                     return file;
                 }
             }
         }
         // If we get there, current selected item is NOT a project
         // so use the "main" one
         // use "main project", if fan project
         Project prj = OpenProjects.getDefault().getMainProject();
         if (prj != null && FanProject.isProject(prj.getProjectDirectory()))
         {
             return OpenProjects.getDefault().getMainProject().getProjectDirectory();
         }
         // try to fall back to the selected item project folder
         return file == null ? null
                 : FileUtil.toFileObject(FanUtilities.getPodFolderForPath(file.getPath()));
     }
 
     class FanJpdaThread extends Thread implements Runnable
     {
 
         private volatile boolean shutdown = false;
 
         @Override
         public void run()
         {
             // start JPDA
             FanUtilities.GENERIC_LOGGER.info("Starting JPDA");
             String portStr = FanPlatformSettings.getInstance().get(FanPlatformSettings.PREF_DEBUG_PORT, "8000");
             int port = new Integer(portStr).intValue();
             try
             {
                 for (int i = 0; i != 15 && !shutdown; i++)
                 {
                     // TODO: this is kinda ugly - Use JPDASupport instead ??
                     try
                     {
                         JPDADebugger.attach("localhost", port, new Object[0]);
                         // if connected, then we are good
                         return;
                     } catch (DebuggerStartException e)
                     {
                         FanUtilities.GENERIC_LOGGER.debug("Failed connecting to debugger, will try again: " + e);
                         if (i == 14)
                         {
                             FanUtilities.GENERIC_LOGGER.exception("Failed connecting to Debugger", e);
                         }
                     }
                     Thread.yield();
                     Thread.sleep(1000);
                 }
             } catch (Exception e)
             {
                 e.printStackTrace();
             }
         }
 
         private void shutdown()
         {
             shutdown = true;
         }
     }
 }
