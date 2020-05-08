 /*
  * JBoss, Home of Professional Open Source.
  * Copyright 2011, Red Hat, Inc., and individual contributors
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
 
 package org.jboss.arquillian.container.appscale.remote;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.PrintStream;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.jboss.arquillian.container.common.AppEngineCommonContainer;
 import org.jboss.arquillian.container.spi.client.container.DeploymentException;
 import org.jboss.arquillian.container.spi.client.protocol.metadata.ProtocolMetaData;
 import org.jboss.shrinkwrap.api.Archive;
 import org.jboss.shrinkwrap.api.GenericArchive;
 import org.jboss.shrinkwrap.api.ShrinkWrap;
 
 /**
  * Remote AppScale container.
  * <p/>
  * To execute tests on remote AppScale instance, you must
  * start AppScale service on remote host
  * and enable password-less ssh access to remote host.
  * <p/>
  * Enable password less access to remote host:
  * $ cat ~/.ssh/id_rsa.pub | ssh root@HOST "cat >> /root/.ssh/authorized_keys"
  * <p/>
  * To start AppScale service on AppScale instance run "appscale up"
  * To stop AppScale service on AppScale instance run "appscale down"
  *
  * @author <a href="mailto:mlazar@redhat.com">Matej Lazar</a>
  * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
  */
 public class AppScaleRemoteContainer extends AppEngineCommonContainer<AppScaleRemoteConfiguration> {
     protected final Logger log = Logger.getLogger(getClass().getName());
     protected static final Pattern hostPortPattern = Pattern.compile("(http://[0-9\\.\\:]+)");
 
     private AppScaleRemoteConfiguration configuration;
     private DeploymentInfo deploymentInfo;
 
     @Override
     public Class<AppScaleRemoteConfiguration> getConfigurationClass() {
         return AppScaleRemoteConfiguration.class;
     }
 
     @Override
     public void setup(AppScaleRemoteConfiguration configuration) {
         this.configuration = configuration;
     }
 
     @Override
     protected File export(Archive<?> archive) throws Exception {
         Archive<GenericArchive> appscaleArchive = ShrinkWrap.create(GenericArchive.class);
         appscaleArchive.merge(archive, "war");
         return super.export(appscaleArchive);
     }
 
     @Override
     protected ProtocolMetaData doDeploy(Archive<?> archive) throws DeploymentException {
         List<String> uploadDeploymentCmd = new ArrayList<String>();
         uploadDeploymentCmd.add("scp");
         uploadDeploymentCmd.add("-r");
         uploadDeploymentCmd.add(getAppLocation().getAbsolutePath());
         uploadDeploymentCmd.add("root@" + configuration.getHost() + ":/root/");
 
         List<String> deployCmd = ssh("/usr/local/appscale-tools/bin/appscale-upload-app --email " + configuration.getEmail() + " --file /root/" + getAppLocation().getName());
 
         List<String> responses = new ArrayList<String>();
         try {
             runCmd(uploadDeploymentCmd, "upload", "./", null, configuration.getUploadTimeout());
             runCmd(deployCmd, "deploy", "./", responses, configuration.getDeployTimeout());
         } catch (InterruptedException e) {
             throw new DeploymentException("Cannot deploy to AppScale.", e);
         }
 
         deploymentInfo = parseUrlFromResponse(responses);
 
         if (deploymentInfo.isValid() == false) {
             throw new DeploymentException("Could not deploy, invalid reponse: " + deploymentInfo);
         }
 
         return getProtocolMetaData(deploymentInfo.host, deploymentInfo.port, archive);
     }
 
     @Override
     protected void teardown() throws DeploymentException {
         List<String> undeployCmd = ssh("/usr/local/appscale-tools/bin/appscale-remove-app --confirm --appname " + deploymentInfo.appName);
         List<String> removeDeploymentArchive = ssh("rm -rf /root/" + getAppLocation().getName());
 
         try {
             runCmd(undeployCmd, "undeploy", "./", null, configuration.getUndeployTimeout());
             runCmd(removeDeploymentArchive, "remove", "./", null, configuration.getRemoveTimeout());
         } catch (InterruptedException e) {
             throw new DeploymentException("Cannot undeploy from AppScale.", e);
         }
     }
 
     protected List<String> ssh(String last) {
         return Arrays.asList("ssh", "root@" + configuration.getHost(), last);
     }
 
     void runCmd(List<String> command, String processName, String workingDirectory, List<String> responses, long timeout) throws InterruptedException {
         log.log(Level.FINE, String.format("Process name='%s' command='%s' workingDirectory='%s'", processName, command, workingDirectory));
         final ProcessBuilder builder = new ProcessBuilder(command);
         builder.directory(new File(workingDirectory));
         final Process process;
         try {
             process = builder.start();
         } catch (IOException e) {
             log.log(Level.SEVERE, "Cannot run command: " + command, e);
             return;
         }
 
         final InputStream stderr = process.getErrorStream();
         final InputStream stdout = process.getInputStream();
 
         final Thread stderrThread = new Thread(new ReadTask(stderr, System.err, processName, null));
         stderrThread.setName(String.format("stderr for %s", processName));
         stderrThread.start();
         final Thread stdoutThread = new Thread(new ReadTask(stdout, System.out, processName, responses));
         stdoutThread.setName(String.format("stdout for %s", processName));
         stdoutThread.start();
 
         stdoutThread.join(timeout);
         stderrThread.join(timeout);
     }
 
     private final class ReadTask implements Runnable {
         private final InputStream source;
         private final PrintStream target;
         private final String processName;
         private final List<String> response;
 
         private ReadTask(final InputStream source, final PrintStream target, String processName, List<String> response) {
             this.source = source;
             this.target = target;
             this.processName = processName;
             this.response = response;
         }
 
         @Override
         public void run() {
             final InputStream source = this.source;
             try {
                 final BufferedReader reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(source)));
                 final OutputStreamWriter writer = new OutputStreamWriter(target);
                 String s;
                 while ((s = reader.readLine()) != null) {
                     synchronized (target) {
                         if (response != null) response.add(s);
                         writer.write('[');
                         writer.write(processName);
                         writer.write("] ");
                         writer.write(s);
                         writer.write('\n');
                         writer.flush();
                     }
                 }
                 source.close();
             } catch (IOException e) {
                 log.log(Level.SEVERE, "Cannot start read task.", e);
             } finally {
                 safeClose(source);
             }
         }
     }
 
     DeploymentInfo parseUrlFromResponse(List<String> responses) {
         DeploymentInfo deploymentInfo = new DeploymentInfo();
 
         for (String response : responses) {
             Matcher hostPortMatcher = hostPortPattern.matcher(response);
             if (hostPortMatcher.find()) {
                 String url = hostPortMatcher.group();
                 URI uri = URI.create(url);
                 deploymentInfo.host = uri.getHost();
                 deploymentInfo.port = uri.getPort();
 
             }
             if (response.startsWith("Uploading")) {
                /* Expected responses are:
		 * "Uploading new version of app {appName}"
		 * "Uploading initial version of app {appName}"
		 */
	        deploymentInfo.appName = response.split("app ")[1];
             }
         }
         return deploymentInfo;
     }
 
     static class DeploymentInfo {
 
         String appName;
         String host;
         Integer port;
 
         @Override
         public String toString() {
             return host + ":" + port + " - " + appName;
         }
 
         public boolean isValid() {
             return appName != null && !appName.equals("")
                     && host != null
                     && port != null && port != 0;
         }
     }
 }
