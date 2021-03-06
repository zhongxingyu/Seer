 package hudson.plugins.clearcase;
 
 import hudson.FilePath;
 import hudson.util.ArgumentListBuilder;
 import java.io.File;
 import java.io.IOException;
 
 public class ClearToolSnapshot extends ClearToolExec {
 
     public ClearToolSnapshot(String clearToolExec) {
         super(clearToolExec);
     }
 
     public void setcs(ClearToolLauncher launcher, String viewName, String configSpec) throws IOException,
             InterruptedException {
         FilePath workspace = launcher.getWorkspace();
         FilePath configSpecFile = workspace.createTextTempFile("configspec", ".txt", configSpec);
 
         ArgumentListBuilder cmd = new ArgumentListBuilder();
         cmd.add(clearToolExec);
         cmd.add("setcs");
         cmd.add(".." + File.separatorChar + configSpecFile.getName());
        launcher.run(cmd.toCommandArray(), null, null, workspace.child(viewName));
 
         configSpecFile.delete();
     }
 
     public void mkview(ClearToolLauncher launcher, String viewName) throws IOException, InterruptedException {
 
         ArgumentListBuilder cmd = new ArgumentListBuilder();
         cmd.add(clearToolExec);
         cmd.add("mkview");
         cmd.add("-snapshot");
         cmd.add("-tag");
         cmd.add(viewName);
         cmd.add(viewName);
         launcher.run(cmd.toCommandArray(), null, null, null);
     }
 
     public void rmview(ClearToolLauncher launcher, String viewName) throws IOException, InterruptedException {
         ArgumentListBuilder cmd = new ArgumentListBuilder();
         cmd.add(clearToolExec);
         cmd.add("rmview");
         cmd.add("-force");
         cmd.add(viewName);
         launcher.run(cmd.toCommandArray(), null, null, null);
         FilePath viewFilePath = launcher.getWorkspace().child(viewName);
         if (viewFilePath.exists()) {
             launcher.getListener().getLogger().println(
                     "Removing view folder as it was not removed when the view was removed.");
             viewFilePath.deleteRecursive();
         }
     }
 
     public void update(ClearToolLauncher launcher, String viewName) throws IOException, InterruptedException {
         ArgumentListBuilder cmd = new ArgumentListBuilder();
         cmd.add(clearToolExec);
         cmd.add("update");
         cmd.add("-force");
         cmd.add("-log", "NUL");
         cmd.add(viewName);
         launcher.run(cmd.toCommandArray(), null, null, null);
     }
 
     @Override
     protected FilePath getRootViewPath(ClearToolLauncher launcher) {
         return launcher.getWorkspace();
     }
 }
