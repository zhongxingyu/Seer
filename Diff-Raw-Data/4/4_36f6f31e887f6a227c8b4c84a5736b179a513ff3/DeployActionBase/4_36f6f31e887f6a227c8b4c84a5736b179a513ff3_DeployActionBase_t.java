 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package th.co.geniustree.virgo.maven;
 
 import java.awt.EventQueue;
 import th.co.geniustree.virgo.server.api.ServerInstanceProviderUtils;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.List;
 import java.util.concurrent.ExecutionException;
 import java.util.logging.Logger;
 import javax.management.InstanceNotFoundException;
 import javax.management.MBeanException;
 import javax.management.MalformedObjectNameException;
 import javax.management.ReflectionException;
 import javax.swing.SwingWorker;
 import org.apache.maven.project.MavenProject;
 import org.netbeans.api.progress.ProgressHandle;
 import org.netbeans.api.progress.ProgressHandleFactory;
 import org.netbeans.api.project.Project;
 import org.netbeans.api.server.ServerInstance;
 import org.netbeans.modules.maven.api.NbMavenProject;
 import org.netbeans.modules.maven.api.execute.RunConfig;
 import org.netbeans.modules.maven.api.execute.RunUtils;
 import org.netbeans.spi.server.ServerInstanceProvider;
 import org.openide.execution.ExecutorTask;
 import org.openide.util.Exceptions;
 import org.openide.util.Task;
 import org.openide.util.TaskListener;
 import th.co.geniustree.virgo.server.api.Deployer;
 
 /**
  *
  * @author pramoth
  */
 public abstract class DeployActionBase implements ActionListener {
 
     protected final Project context;
 
     public DeployActionBase(Project context) {
         this.context = context;
     }
 
     @Override
     public void actionPerformed(ActionEvent ev) {
         try {
             execute();
         } catch (Exception ex) {
             Exceptions.printStackTrace(ex);
         }
     }
 
     protected void execute() throws IOException, MalformedObjectNameException, InstanceNotFoundException, MBeanException, ReflectionException {
         NbMavenProject nbMavenProject = context.getLookup().lookup(NbMavenProject.class);
         final MavenProject mavenProject = nbMavenProject.getMavenProject();
         final File baseDir = mavenProject.getBasedir();
         String finalFileName = "target/" + mavenProject.getBuild().getFinalName() + ".jar";
         final File finalFile = new File(baseDir, finalFileName);
         RunConfig createRunConfig = RunUtils.createRunConfig(mavenProject.getBasedir(), context, finalFileName, Arrays.asList("package"));
         ExecutorTask task = RunUtils.run(createRunConfig);
         task.addTaskListener(new TaskListener() {
             @Override
             public void taskFinished(Task task) {
                 EventQueue.invokeLater(new Runnable() {
                     @Override
                     public void run() {
                         try {
                             executeDeployTask(mavenProject, finalFile);
                         } catch (IOException ex) {
                             ex.printStackTrace();
                         }
                     }
                 });
             }
         });
     }
 
     public abstract void doOperation(Deployer deployer, File finalFile, String symbolicName, String bundleVersion, boolean recover) throws Exception;
 
     private void executeDeployTask(MavenProject mavenProject, final File finalFile) throws IOException {
         final String symbolicName = BundleUtils.getSymbolicName(mavenProject);
         final String bundleVersion = BundleUtils.getBundleVersion(finalFile);
         ServerInstanceProvider virgoProvider = ServerInstanceProviderUtils.getVirgoServerInstanceProvider();
         if (virgoProvider != null) {
             List<ServerInstance> instances = virgoProvider.getInstances();
             if (instances.isEmpty()) {
                 ServerInstanceProviderUtils.openWizard();
             }
             //User may be cancle wizard.
             if (instances.isEmpty()) {
                 return;
             }
             final Deployer deployer = instances.get(0).getLookup().lookup(Deployer.class);
             try {
                 final ProgressHandle handle = ProgressHandleFactory.createHandle("Deploy " + mavenProject.getBuild().getFinalName());
                 SwingWorker worker = new SwingWorker<Object, Object>() {
                     @Override
                     protected Object doInBackground() throws Exception {
                         handle.start();
                         doOperation(deployer, finalFile, symbolicName, bundleVersion, true);
                         return null;
                     }
 
                     @Override
                     protected void done() {
                         try {
                             get();
                         } catch (InterruptedException ex) {
                             Exceptions.printStackTrace(ex);
                         } catch (ExecutionException ex) {
                             Exceptions.printStackTrace(ex);
                        } finally {
                            handle.finish();
                         }
                     }
                 };
                 worker.execute();
             } catch (Exception ex) {
                 Exceptions.printStackTrace(ex);
             }
         }
     }
 }
