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
 
 package sorcer.launcher;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.Option;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.apache.commons.cli.PosixParser;
 import org.slf4j.LoggerFactory;
 import sorcer.launcher.process.DestroyingListener;
 import sorcer.launcher.process.ProcessDestroyer;
 import sorcer.util.FileUtils;
 import sorcer.util.JavaSystemProperties;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.List;
 
 import static sorcer.core.SorcerConstants.*;
 
 /**
  * @author Rafał Krupiński
  */
 public class Sorcer {
     private static final String WAIT = "w";
     private static final String LOGS = "logs";
     private static final String DEBUG = "debug";
     private static final String PROFILE = "P";
     public static final String MODE = "M";
 
     /**
      * This method calls {@link java.lang.System#exit(int)} before returning, in case of any remaining non-daemon threads running.
      * There is a java API for starting SORCER in in-process or forked modes with WAIT options, so there shouldn't be any need to call this method from java code.
      * <p/>
      * -wait=[no,start,end]
      * -logDir {}
      * -home {}
      * <p/>
      * {}... config files for ServiceStarter
      */
     public static void main(String[] args) {
         try {
             JavaSystemProperties.ensure(SORCER_HOME);
             Options options = buildOptions();
             CommandLineParser parser = new PosixParser();
             CommandLine cmd = parser.parse(options, args);
 
             if (cmd.hasOption('h')) {
                 HelpFormatter helpFormatter = new HelpFormatter();
                helpFormatter.printHelp(160, "sorcer", "Start sorcer", options, null);
                 return;
             }
 
             ILauncher launcher = parseCommandLine(cmd);
 
             launcher.preConfigure();
 
             if (launcher instanceof SorcerLauncher) {
                 SorcerLauncher.installSecurityManager();
             }
 
             launcher.start();
         } catch (Exception x) {
             x.printStackTrace();
             System.exit(-1);
         }
     }
 
     private static Options buildOptions() {
         Options options = new Options();
         Option wait = new Option(WAIT, "wait", true, "Wait style, one of:\n"
                 + "'no' - exit immediately, forces forked mode\n"
                 + "'start' - wait until sorcer starts, then exit, forces forked mode\n"
                 + "'end' - wait until sorcer finishes, stopping launcher also stops SORCER");
         wait.setType(Launcher.WaitMode.class);
         wait.setArgs(1);
         wait.setArgName("wait-mode");
         options.addOption(wait);
 
         Option logs = new Option(LOGS, true, "Directory for logs");
         logs.setType(File.class);
         logs.setArgs(1);
         logs.setArgName("log-dir");
         options.addOption(logs);
 
         Option debug = new Option(DEBUG, true, "Add debug option to JVM");
         debug.setType(Boolean.class);
         debug.setArgs(1);
         debug.setArgName("port");
         options.addOption(debug);
 
         //see sorcer.launcher.Profile
         Option profile = new Option(PROFILE, "profile", true, "Profile, one of [sorcer, rio, mix] or a path");
         profile.setArgs(1);
         profile.setType(String.class);
         profile.setArgName("profile");
         options.addOption(profile);
 
         String modeDesc = "Select start mode, one of:\n"
                 + Mode.preferDirect.paramValue + " - default, prefer current JVM, start in forked if debug is enabled or required environment variable is not set\n"
                 + Mode.forceDirect.paramValue + " - try current JVM, exit on failure\n"
                 + Mode.preferFork.paramValue + " - prefer new process, try in current JVM if cannot run a process (e.g. insufficient privileges)\n"
                 + Mode.forceFork.paramValue + " try new process, exit on failure";
         Option mode = new Option(MODE, "mode", true, modeDesc);
         mode.setType(Boolean.class);
         mode.setArgs(1);
         mode.setArgName("mode");
         options.addOption(mode);
         options.addOption("h", "help", false, "Print this help");
 
         return options;
     }
 
     private static ILauncher parseCommandLine(CommandLine cmd) throws ParseException, IOException {
         Mode mode;
         if (cmd.hasOption(MODE)) {
             String modeValue = cmd.getOptionValue(MODE);
             if (Mode.forceFork.paramValue.equalsIgnoreCase(modeValue))
                 mode = Mode.forceFork;
             else if (Mode.preferFork.paramValue.equalsIgnoreCase(modeValue))
                 mode = Mode.preferFork;
             else if (Mode.forceDirect.paramValue.equalsIgnoreCase(modeValue))
                 mode = Mode.forceDirect;
             else if (Mode.preferDirect.paramValue.equalsIgnoreCase(modeValue))
                 mode = Mode.preferDirect;
             else
                 throw new IllegalAccessError("Illegal mode " + modeValue);
         } else
             mode = Mode.preferDirect;
 
         Integer debugPort = null;
         if (cmd.hasOption(DEBUG)) {
             debugPort = Integer.parseInt(cmd.getOptionValue(DEBUG));
         }
 
         ILauncher launcher = null;
         if (!mode.fork) {
             boolean envOk = SorcerLauncher.checkEnvironment();
             if (envOk && debugPort == null) {
                 launcher = createSorcerLauncher();
             } else {
                 if (debugPort == null)
                     if (mode.force)
                         throw new IllegalArgumentException("Cannot run in force-direct mode");
                     else
                         LoggerFactory.getLogger(Sorcer.class).warn("Cannot run in force-direct mode");
                 else if (mode.force)
                     throw new IllegalArgumentException("Cannot run in force-direct mode with debug");
                 else
                     LoggerFactory.getLogger(Sorcer.class).warn("Cannot run in force-direct mode with debug");
             }
         }
 
         if (launcher == null) {
             IForkingLauncher forkingLauncher = null;
             try {
                 forkingLauncher = createForkingLauncher(debugPort);
             } catch (IllegalStateException e) {
                 if ((mode.fork && mode.force) || (!mode.fork))
                     throw e;
             }
             launcher = forkingLauncher;
 /*
             File outFile = new File(logDir, "output.log");
             File errFile = new File(logDir, "error.log");
             if (SystemUtils.isJavaVersionAtLeast(JavaVersion.JAVA_1_7)) {
                 forkingLauncher.setOutFile(outFile);
                 forkingLauncher.setErrFile(errFile);
             }
             forkingLauncher.setOut(new PrintStream(outFile));
             forkingLauncher.setErr(new PrintStream(errFile));
 */
         }
 
         // called prefer-fork but didn't make it
         // fallback to direct
         if (launcher == null && mode.fork && !mode.force)
             launcher = createSorcerLauncher();
 
         if (launcher == null)
             throw new IllegalStateException("Could not start SORCER");
 
         String homePath = System.getProperty(SORCER_HOME);
         File home = null;
         if (homePath != null) {
             home = new File(homePath).getCanonicalFile();
             launcher.setHome(home);
         }
 
         File logDir = FileUtils.getFile(home, cmd.hasOption(LOGS) ? cmd.getOptionValue(LOGS) : "logs");
 
         launcher.setLogDir(logDir);
 
         try {
             launcher.setWaitMode(cmd.hasOption(WAIT) ? ILauncher.WaitMode.valueOf(cmd.getOptionValue(WAIT)) : ILauncher.WaitMode.start);
         } catch (IllegalArgumentException x) {
             throw new IllegalArgumentException("Illegal wait option " + cmd.getOptionValue(WAIT) + ". Use one of " + Arrays.toString(ILauncher.WaitMode.values()), x);
         }
 
         @SuppressWarnings("unchecked")
         List<String> userConfigFiles = cmd.getArgList();
         launcher.setConfigs(userConfigFiles);
 
         if (cmd.hasOption(PROFILE))
             launcher.setProfile(cmd.getOptionValue(PROFILE));
 
         return launcher;
     }
 
     private static IForkingLauncher createForkingLauncher(Integer debugPort) {
         IForkingLauncher forkingLauncher;
         try {
             forkingLauncher = (IForkingLauncher) Class.forName("sorcer.launcher.process.ForkingLauncher").newInstance();
             if (debugPort != null)
                 forkingLauncher.setDebugPort(debugPort);
 
             forkingLauncher.setSorcerListener(new DestroyingListener(ProcessDestroyer.installShutdownHook()));
         } catch (InstantiationException e) {
             throw new IllegalStateException("Could not instantiate ForkingLauncher", e);
         } catch (IllegalAccessException e) {
             throw new IllegalStateException("Could not access ForkingLauncher", e);
         } catch (ClassNotFoundException e) {
             throw new IllegalStateException("Could not instantiate ForkingLauncher", e);
         }
         return forkingLauncher;
     }
 
     private static ILauncher createSorcerLauncher() {
         ILauncher launcher;
         SorcerLauncher.installLogging();
         launcher = new SorcerLauncher();
         return launcher;
     }
 }
