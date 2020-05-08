 package hudson.plugins.antexec;
 
 import hudson.EnvVars;
 import hudson.FilePath;
 import hudson.model.*;
 
 import java.io.IOException;
 import java.io.PrintStream;
 
 public class AntExecUtils {
 
     public static FilePath getAntHome(AbstractBuild build, PrintStream logger, EnvVars env, Boolean isUnix, String antHome, Boolean verbose) throws IOException, InterruptedException {
         String envAntHome = env.get("ANT_HOME");
         String useAntHome = null;
         String antExe = isUnix ? "/bin/ant" : "\\bin\\ant.bat";
 
         //Setup ANT_HOME from Environment or job configuration screen
         if (envAntHome != null && envAntHome.length() > 0 && !envAntHome.equals("")) {
             useAntHome = envAntHome;
             if (verbose != null && verbose)
                 logger.println(Messages.AntExec_AntHomeEnvVarFound(useAntHome));
         } else {
             if (verbose != null && verbose) logger.println(Messages.AntExec_AntHomeEnvVarNotFound());
         }
 
         //Forcing configured ANT_HOME in Environment
         if (antHome != null && antHome.length() > 0 && !antHome.equals("")) {
             if (useAntHome != null) {
                 logger.println(Messages._AntExec_AntHomeReplacing(useAntHome, antHome));
             } else {
                 logger.println(Messages._AntExec_AntHomeReplacing("", antHome));
                 if (build.getBuiltOn().createPath(antHome).exists()) {
                     logger.println(build.getBuiltOn().createPath(antHome) + " exists!");
                 }
             }
             useAntHome = antHome;
             //Change ANT_HOME in environment
             env.put("ANT_HOME", useAntHome);
             logger.println(Messages.AntExec_EnvironmentChanged("ANT_HOME", useAntHome));
 
             //Add ANT_HOME/bin into the environment PATH
            String newAntPath = isUnix ? useAntHome + "/bin;" + env.get("PATH") : useAntHome + "\\bin:" + env.get("PATH");
             env.put("PATH", newAntPath);
             logger.println(Messages.AntExec_EnvironmentChanged("PATH", newAntPath));
 
 
             //Add ANT_HOME/lib into the environment LD_LIBRARY_PATH
             if(isUnix) {
                 env.put("LD_LIBRARY_PATH", useAntHome + "/lib:"+env.get("LD_LIBRARY_PATH"));
                 logger.println(Messages.AntExec_EnvironmentAdded("LD_LIBRARY_PATH", useAntHome + "/lib"));
             }
 
             if (env.containsKey("JAVA_HOME")) {
                env.put("PATH", isUnix ? env.get("JAVA_HOME") + "/bin;" + env.get("PATH") : env.get("JAVA_HOME") + "\\bin:" + env.get("PATH"));
                 logger.println(Messages.AntExec_EnvironmentAdded("PATH", isUnix ? env.get("JAVA_HOME") + "/bin" : env.get("JAVA_HOME") + "\\bin"));
             }
         }
 
         if (useAntHome == null) {
             logger.println(Messages.AntExec_AntHomeValidation());
             logger.println("Trying to run ant from PATH ...");
             return build.getBuiltOn().createPath("ant");
         } else {
             if (build.getBuiltOn().createPath(useAntHome + antExe).exists()) {
                 logger.println(build.getBuiltOn().createPath(useAntHome + antExe) + " exists!");
             }
             return build.getBuiltOn().createPath(useAntHome + antExe);
         }
     }
 
 
     static FilePath makeBuildFile(String targetSource, AbstractBuild build) throws IOException, InterruptedException {
         FilePath buildFile = new FilePath(build.getWorkspace(), AntExec.buildXml);
 
         StringBuilder sb = new StringBuilder();
         sb.append("<project default=\"AntExec_Builder\" xmlns:antcontrib=\"antlib:net.sf.antcontrib\" basedir=\".\">\n");
         sb.append("<target name=\"AntExec_Builder\">\n\n");
         sb.append(targetSource);
         sb.append("\n</target>\n");
         sb.append("</project>\n");
 
         buildFile.write(sb.toString(), null);
         return buildFile;
     }
 }
