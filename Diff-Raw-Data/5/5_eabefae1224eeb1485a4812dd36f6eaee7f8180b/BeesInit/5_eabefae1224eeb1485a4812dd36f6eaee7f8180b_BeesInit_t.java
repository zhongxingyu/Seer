 package com.cloudbees.jenkins.plugins;
 
 import com.cloudbees.plugins.credentials.CredentialsProvider;
 import com.cloudbees.plugins.credentials.cloudbees.CloudBeesUser;
 import hudson.EnvVars;
 import hudson.Extension;
 import hudson.FilePath;
 import hudson.Launcher;
 import hudson.LauncherDecorator;
 import hudson.Proc;
 import hudson.Util;
 import hudson.model.AbstractBuild;
 import hudson.model.BuildListener;
 import hudson.model.Descriptor;
 import hudson.model.Item;
 import hudson.model.Node;
 import hudson.model.Run;
 import hudson.tasks.BuildWrapper;
 import hudson.tools.ToolInstallation;
 import hudson.util.ListBoxModel;
 import hudson.util.Secret;
 import jenkins.model.Jenkins;
 import org.apache.commons.io.FileUtils;
 import org.kohsuke.stapler.AncestorInPath;
 import org.kohsuke.stapler.DataBoundConstructor;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 /**
  * @author <a href="mailto:nicolas.deloof@gmail.com">Nicolas De Loof</a>
  */
 public class BeesInit extends BuildWrapper {
 
     private final String sdk;
 
     private final String user;
 
     private final String region;
 
     @DataBoundConstructor
     public BeesInit(String sdk, String user, String region) {
         this.sdk = sdk;
         this.user = user;
         this.region = region;
     }
 
     @Override
     public Environment setUp(final AbstractBuild build, final Launcher launcher, final BuildListener listener) throws IOException, InterruptedException {
 
         final File userConfigFile = writeBeesConfig(getCloudBeesUser(build));
 
         return new Environment() {
 
             @Override
             public void buildEnvVars(Map<String, String> env) {
                 env.put("BEES_REPO", userConfigFile.getParentFile().getAbsolutePath());
             }
 
             @Override
             public boolean tearDown(AbstractBuild build, BuildListener listener) throws IOException, InterruptedException {
                 userConfigFile.delete();
                 return true;
             }
         };
     }
 
     @Override
     public Launcher decorateLauncher(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException, Run.RunnerAbortedException {
         final ToolInstallation bees = getBeesSDK().translate(build, listener);
         return new DecoratedLauncher(launcher) {
             @Override
             public Proc launch(ProcStarter starter) throws IOException {
                 EnvVars vars = toEnvVars(starter.envs());
                 bees.buildEnvVars(vars);
                 return super.launch(starter.envs(Util.mapToEnv(vars)));
             }
 
             private EnvVars toEnvVars(String[] envs) {
                 EnvVars vars = new EnvVars();
                 for (String line : envs) {
                     vars.addLine(line);
                 }
                 return vars;
             }
         };
     }
 
     public BeesSDK getBeesSDK() {
         List<BeesSDK> installations = BeesSDK.installations();
         if (installations.size() == 1) return installations.get(0);
         for (BeesSDK installation : installations) {
             if (installation.getName().equals(sdk)) {
                 return installation;
             }
         }
         throw new IllegalArgumentException("Invalid CloudBees SDK installation " + sdk);
     }
 
     private CloudBeesUser getCloudBeesUser(AbstractBuild build) {
         CloudBeesUser cloudBeesUser = null;
         List<CloudBeesUser> users = CredentialsProvider.lookupCredentials(CloudBeesUser.class, build.getParent());
         for (CloudBeesUser u : users) {
             if (u.getName().equals(user)) {
                 cloudBeesUser = u;
                 break;
             }
         }
         if (cloudBeesUser == null) throw new IllegalArgumentException("Invalid cloudbees user " + user);
         return cloudBeesUser;
     }
 
     private File writeBeesConfig(CloudBeesUser cloudBeesUser) throws IOException {
         File beesHome = Util.createTempDir();
         final File userConfigFile = new File(beesHome, "bees.config");
         Properties properties = new Properties();
         properties.setProperty("bees.api.url.us", "https://api.cloudbees.com/api");
         properties.setProperty("bees.api.url.eu", "https://api-eu.cloudbees.com/api");
        if ("us".equals(region))
            properties.setProperty("bees.api.url", "https://api.cloudbees.com/api");
        else
            properties.setProperty("bees.api.url", "https://api-eu.cloudbees.com/api");
         properties.setProperty("bees.api.key", cloudBeesUser.getAPIKey());
         properties.setProperty("bees.api.secret", Secret.toString(cloudBeesUser.getAPISecret()));
         FileOutputStream os = new FileOutputStream(userConfigFile);
         properties.store(os, "CloudBees SDK config");
         os.close();
         return userConfigFile;
     }
 
     @Extension
     public static class DescriptorImpl extends Descriptor<BuildWrapper> {
 
         @Override
         public String getDisplayName() {
             return "Configure CloudBees SDK";
         }
 
         public ListBoxModel doFillSdkItems() {
             ListBoxModel m = new ListBoxModel();
             for (BeesSDK sdk : BeesSDK.installations()) {
                 m.add(sdk.getName(), sdk.getName());
             }
             return m;
         }
 
         public ListBoxModel doFillUserItems(@AncestorInPath Item item) {
             ListBoxModel m = new ListBoxModel();
             List<CloudBeesUser> users = CredentialsProvider.lookupCredentials(CloudBeesUser.class, item);
             for (CloudBeesUser u : users) {
                 m.add(u.getDisplayName(), u.getName());
             }
             return m;
         }
 
         public ListBoxModel doFillRegionItems() {
             ListBoxModel m = new ListBoxModel();
             m.add("US", "us");
             m.add("Europe", "eu");
             return m;
         }
     }
 }
