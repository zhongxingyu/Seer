 /*
  * Copyright 2014 Sorcersoft.com S.A.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package sorcer.junit;
 
 import org.junit.Ignore;
 import org.junit.runner.notification.Failure;
 import org.junit.runner.notification.RunNotifier;
 import org.junit.runners.BlockJUnit4ClassRunner;
 import org.junit.runners.model.FrameworkMethod;
 import org.junit.runners.model.InitializationError;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.slf4j.bridge.SLF4JBridgeHandler;
 import sorcer.core.SorcerConstants;
 import sorcer.core.SorcerEnv;
 import sorcer.core.requestor.ServiceRequestor;
 import sorcer.launcher.Launcher;
 import sorcer.launcher.SorcerLauncher;
 import sorcer.launcher.WaitMode;
 import sorcer.launcher.WaitingListener;
 import sorcer.resolver.Resolver;
 import sorcer.util.IOUtils;
 import sorcer.util.JavaSystemProperties;
 import sorcer.util.bdb.HandlerInstaller;
 
 import java.io.File;
 import java.io.IOException;
 import java.security.Policy;
 import java.util.Arrays;
import java.util.LinkedList;
 
 import static sorcer.core.SorcerConstants.SORCER_HOME;
 import static sorcer.util.JavaSystemProperties.RMI_SERVER_CLASS_LOADER;
 
 /**
  * @author Rafał Krupiński
  */
 public class SorcerRunner extends BlockJUnit4ClassRunner {
     private static File home;
     private static final Logger log;
 
     private SorcerServiceConfiguration configuration;
 
     static {
         String homePath = System.getProperty(SORCER_HOME);
         if (homePath != null)
             home = new File(homePath);
 
         JavaSystemProperties.ensure("logback.configurationFile", new File(home, "configs/logback.groovy").getPath());
         JavaSystemProperties.ensure(JavaSystemProperties.PROTOCOL_HANDLER_PKGS, "net.jini.url|org.rioproject.url");
         JavaSystemProperties.ensure("org.rioproject.resolver.jar", Resolver.resolveAbsolute("org.rioproject.resolver:resolver-aether"));
         JavaSystemProperties.ensure(RMI_SERVER_CLASS_LOADER, "sorcer.rio.rmi.SorcerResolvingLoader");
         JavaSystemProperties.ensure(SorcerConstants.SORCER_WEBSTER_INTERNAL, Boolean.TRUE.toString());
         log = LoggerFactory.getLogger(SorcerRunner.class);
         new HandlerInstaller(null, null);
     }
 
     public SorcerRunner(Class<?> klass) throws InitializationError {
         this(klass, klass.getAnnotation(SorcerServiceConfiguration.class));
     }
 
     /**
      * @throws org.junit.runners.model.InitializationError if the test class is malformed.
      */
     public SorcerRunner(Class<?> klass, SorcerServiceConfiguration configuration) throws InitializationError {
         super(klass);
         if (home == null)
             throw new InitializationError("sorcer.home property is required");
 
         String policyPath = System.getProperty(JavaSystemProperties.SECURITY_POLICY);
         if (policyPath != null) {
             File policy = new File(policyPath);
             IOUtils.checkFileExistsAndIsReadable(policy);
         } else {
             if (System.getSecurityManager() != null)
                 throw new InitializationError("SecurityManager set but no " + JavaSystemProperties.SECURITY_POLICY);
             File policy = new File(home, "configs/sorcer.policy");
             IOUtils.checkFileExistsAndIsReadable(policy);
             System.setProperty(JavaSystemProperties.SECURITY_POLICY, policy.getPath());
             Policy.getPolicy().refresh();
         }
 
         this.configuration = configuration;
         checkAnnotations(klass);
 
         System.setSecurityManager(new SecurityManager());
 
         SLF4JBridgeHandler.removeHandlersForRootLogger();
         SLF4JBridgeHandler.install();
 
         SorcerEnv.debug = true;
         ExportCodebase exportCodebase = klass.getAnnotation(ExportCodebase.class);
         String[] codebase = exportCodebase != null ? exportCodebase.value() : null;
         if (codebase != null && codebase.length > 0) {
             ServiceRequestor.prepareCodebase(codebase);
         }
     }
 
     private void checkAnnotations(Class<?> klass) throws InitializationError {
         if (configuration == null && klass.getAnnotation(SorcerServiceConfigurations.class) != null)
             throw new InitializationError(SorcerServiceConfigurations.class.getName() + " annotation present on class " + klass.getName() + ". Please run with SorcerSuite.");
     }
 
     @Override
     public void run(final RunNotifier notifier) {
         if (configuration == null) {
             super.run(notifier);
             return;
         }
 
         Launcher sorcerLauncher = null;
         String[] configs = configuration.value();
         try {
             sorcerLauncher = startSorcer(configs);
             super.run(notifier);
        } catch (Exception e) {
             notifier.fireTestFailure(new Failure(getDescription(), new Exception("Error while starting SORCER with configs: " + Arrays.toString(configs), e)));
         } finally {
             if (sorcerLauncher != null)
                 sorcerLauncher.stop();
         }
     }
 
     @Override
     protected void runChild(FrameworkMethod method, RunNotifier notifier) {
         if (method.getAnnotation(Ignore.class) == null)
             log.info("Testing {}", method.getMethod());
         super.runChild(method, notifier);
     }
 
     private Launcher startSorcer(String[] serviceConfigPaths) throws IOException {
         WaitingListener listener = new WaitingListener();
 
         Launcher launcher = new SorcerLauncher();
        launcher.setConfigs(new LinkedList<String>(Arrays.asList(serviceConfigPaths)));
         launcher.addSorcerListener(listener);
         launcher.setHome(home);
         File logDir = new File("/tmp/logs");
         logDir.mkdir();
         launcher.setLogDir(logDir);
 
         log.info("Starting SORCER instance for test {}", getDescription());
        launcher.preConfigure();
         launcher.start();
 
         listener.wait(WaitMode.start);
 
         return launcher;
     }
 }
