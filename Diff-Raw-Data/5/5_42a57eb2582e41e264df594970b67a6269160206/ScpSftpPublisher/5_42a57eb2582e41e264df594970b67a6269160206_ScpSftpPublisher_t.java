 package ru.hobbut.hudson;
 
 import hudson.*;
 import hudson.model.*;
 import hudson.tasks.BuildStepDescriptor;
 import hudson.tasks.BuildStepMonitor;
 import hudson.tasks.Notifier;
 import hudson.tasks.Publisher;
 import hudson.util.CopyOnWriteList;
 import hudson.util.FormValidation;
 import net.sf.json.JSONObject;
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.QueryParameter;
 import org.kohsuke.stapler.StaplerRequest;
 import org.kohsuke.stapler.StaplerResponse;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.util.StringUtils;
 import ru.hobbut.hudson.model.Host;
 import ru.hobbut.hudson.model.HostWithEntries;
 import ru.hobbut.hudson.utils.ConnectInfo;
 import ru.hobbut.hudson.utils.PluginException;
 import ru.hobbut.hudson.utils.UploadCallable;
 import ru.hobbut.hudson.utils.Utils;
 
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.*;
 
 /**
  * Created by IntelliJ IDEA.
  * User: hobbut
  * Date: 1/13/11
  * Time: 5:24 PM
  */
public class ScpSftpPublisher extends Publisher {
 
     private static final Logger logger = LoggerFactory.getLogger(ScpSftpPublisher.class);
 
     private List<HostWithEntries> hostsWithEntries;
 
     @DataBoundConstructor
     public ScpSftpPublisher(List<HostWithEntries> hostsWithEntries) {
         if (hostsWithEntries == null) {
             hostsWithEntries = new ArrayList<HostWithEntries>();
         }
         this.hostsWithEntries = hostsWithEntries;
     }
 
     public List<HostWithEntries> getHostsWithEntries() {
         return hostsWithEntries;
     }
 
     public BuildStepMonitor getRequiredMonitorService() {
         return BuildStepMonitor.BUILD;
     }
 
     private Host getHost(String connectUrl) {
         return DESCRIPTOR.getHost(connectUrl);
     }
 
     private Map<Host, List<HostWithEntries>> getEntriesByHost(AbstractBuild build, Launcher launcher, BuildListener listener) {
         Map<Host, List<HostWithEntries>> map = new HashMap<Host, List<HostWithEntries>>();
 
         for (HostWithEntries hostWithEntries : hostsWithEntries) {
             Host host = getHost(hostWithEntries.getConnectUrl());
             if (host == null) {
                 build.setResult(Result.UNSTABLE);
                 logConsole(listener.getLogger(), "Cannot find host:" + hostWithEntries.getConnectUrl());
                 continue;
             }
             List<HostWithEntries> list = map.get(host);
             if (list == null) {
                 list = new ArrayList<HostWithEntries>();
             }
             list.add(hostWithEntries);
             map.put(host, list);
         }
         return map;
     }
 
     @Override
     public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
         if (build.getResult() == Result.FAILURE) {
             // build failed. don't post
             return true;
         }
         Result result = Result.SUCCESS;
 
         Map<String, Host> hostMap = new HashMap<String, Host>();
         Map<Host, List<HostWithEntries>> map = getEntriesByHost(build, launcher, listener);
 
         List<Callable<Map<HostWithEntries, Boolean>>> calls = new ArrayList<Callable<Map<HostWithEntries, Boolean>>>();
 
         for (Host host : map.keySet()) {
             calls.add(new UploadCallable(map.get(host), host, build, listener));
         }
 
         ExecutorService executorService = Executors.newFixedThreadPool(DESCRIPTOR.isConcurrentUpload() ? map.size() : 1);
         try {
             List<Future<Map<HostWithEntries, Boolean>>> list = executorService.invokeAll(calls);
             for (Future<Map<HostWithEntries, Boolean>> future : list) {
                 try {
                     Map<HostWithEntries, Boolean> results = future.get();
                     if (results.containsValue(false)) {
                         result = Result.UNSTABLE;
                         break;
                     }
                 } catch (ExecutionException e) {
                     e.printStackTrace(listener.error("error"));
                     result = Result.UNSTABLE;
                 }
             }
         } catch (InterruptedException e) {
             e.printStackTrace(listener.error("error"));
             result = Result.UNSTABLE;
         } finally {
             executorService.shutdown();
         }
 
         build.setResult(result);
         return true;
     }
 
     @Extension // this marker indicates Hudson that this is an implementation of an extension point.
     public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();
 
     public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {
 
         private final CopyOnWriteList<Host> hosts = new CopyOnWriteList<Host>();
 
         private boolean concurrentUpload;
 
         @Override
         public boolean isApplicable(Class<? extends AbstractProject> aClass) {
             return true;
         }
 
         @Override
         public String getDisplayName() {
             return "SCP SFTP Publisher";
         }
 
         public boolean isConcurrentUpload() {
             return concurrentUpload;
         }
 
         public void setConcurrentUpload(boolean concurrentUpload) {
             this.concurrentUpload = concurrentUpload;
         }
 
         private Host findHost(String connectUrl) {
             if (StringUtils.hasText(connectUrl)) {
                 return null;
             }
 
             for (Host host : hosts) {
                 if (connectUrl.equals(host.getConnectUrl())) {
                     return host;
                 }
             }
             return null;
         }
 
         @Override
        public Publisher newInstance(StaplerRequest req, JSONObject formData) throws FormException {
             return req.bindJSON(ScpSftpPublisher.class, formData);
         }
 
         @Override
         public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
             logger.debug("json " + json.toString());
             logger.debug("parameters " + req.getParameterMap());
             req.bindParameters(this, "adv.");
             hosts.replaceBy(req.bindParametersToList(Host.class, "scp."));
             save();
             return true;
         }
 
         public DescriptorImpl(Class<? extends Notifier> clazz) {
             super(clazz);
         }
 
         public DescriptorImpl() {
             load();
         }
 
         public Host getHost(String connectionUrl) {
             if (!StringUtils.hasText(connectionUrl)) {
                 return null;
             }
             for (Host host : hosts) {
                 if (connectionUrl.equals(host.getConnectUrl())) {
                     return host;
                 }
             }
             return null;
         }
 
         public Host[] getHosts() {
             return hosts.toArray(new Host[hosts.size()]);
         }
 
         public FormValidation doTestConnection(StaplerRequest req, StaplerResponse rsp,
                                                @QueryParameter("scp.connectUrl") String connectUrl,
                                                @QueryParameter("scp.password") String password,
                                                @QueryParameter("scp.keyfilePath") String keyfile) {
             Host host = new Host(connectUrl, password, keyfile, true);
             try {
                 ConnectInfo connectInfo = Utils.getConnectInfo(host);
                 return Utils.checkAuthentication(connectInfo) ? FormValidation.ok("Connection ok") : FormValidation.error("Authentication failed");
             } catch (PluginException e) {
                 return FormValidation.error(e.getMessage(), e);
             }
         }
 
     }
 
     public static void logConsole(PrintStream logger, String message) {
         Utils.logConsole(logger, message);
     }
 }
