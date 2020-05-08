 package com.pivotal.hamster.cli.parser;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.hadoop.yarn.api.ApplicationConstants;
 import org.apache.hadoop.yarn.api.records.ContainerLaunchContext;
 
 public class HamsterParamBuilder {
     private static final Log LOG = LogFactory.getLog(HamsterParamBuilder.class);
     
     String[] outArgs;
     
     /* parameters for mca <K,V>*/
     Map<String, String> mcaParams;
     
     /* parameters for ompi <K,V> */
     Map<String, String> ompiParams;
     
     /* parameters for hamster cli, like -debug, etc. */
     Map<String, String> cliParams;
     
     /* parameters for addFile/Archieve, etc. */
     List<String> addFiles;
     List<String> addArchives;
     
     /* parameters for user's application */
     List<String> userParams;
     
     /* parameters for user's env */
     Map<String, String> userEnvs;
     
     /* -np # */
     int np = -1;
     
     boolean mpiApp;
     boolean verbose;
     // use valgrind to debug mpirun
     boolean valgrind;
     private int hamsterMemory = -1;
     private int hamsterCPU = -1;
     
     public HamsterParamBuilder() {
       mcaParams = new HashMap<String, String>();
       ompiParams = new HashMap<String, String>();
       cliParams = new HashMap<String, String>();
       addFiles = new ArrayList<String>();
       addArchives = new ArrayList<String>();
       userParams = new ArrayList<String>();
       userEnvs = new HashMap<String, String>();
       verbose = false;
       mpiApp = false;
       valgrind = false;
     }
     
     private static String[] removeEmpty(String[] before) {
       List<String> after = new ArrayList<String>();
       for (String s : before) {
         if (!s.isEmpty()) {
           after.add(s);
         }
       }
       return after.toArray(new String[0]);
     }
     
     private void checkAndThrow(String[] args) throws IOException {
       // do set hamster home, etc. only when execute mpi applications
       if (!mpiApp) {
         LOG.error("you must call \"hamster mpirun ... \", we don't support other application now!");
         throw new IOException("you must call \"hamster mpirun ... \", we don't support other application now!");
       }
       
       // check if we have np specified
       if (np <= 0) {
         LOG.error("you should specify a number-mpi-processes >= 0 in this job (by {-c or -n or --n or -np}.");
         throw new IOException("you should specify a number-mpi-processes >= 0 in this job (by {-c or -n or --n or -np}.");
       }
     }
 
     public String[] parse(String[] args) throws IOException {
       // insanity check
       if (args == null || args.length == 0) {
         throw new IOException("input parameter is empty");
       }
       
       // save inputArgs
       mpiApp = checkIsMpiApp(args);
       
       // we will do nothing when it's not MPI
       if (!mpiApp) {
         LOG.warn("it's not a MPI application");
         outArgs = args;
         return outArgs;
       }
       
       // otherwise, we will do parse
       String[] curArgs = removeEmpty(args);
       CliParser parser;
       
       // parse mpirun args
       parser = new MpirunCliParser();
       curArgs = parser.parse(curArgs, this);
       
       // parse mca parametes
       parser = new McaCliParser();
       curArgs = parser.parse(curArgs, this);
       
       // parse hamster cli parameters
       parser = new HamsterCliParser();
       curArgs = parser.parse(curArgs, this);
       
       outArgs = curArgs;
       
       checkAndThrow(outArgs);
       
       return outArgs;
     }
     
     /**
      * check if it's mpi application
      * @return if it's mpi application
      */
     private static boolean checkIsMpiApp(String[] args) {
       if (args[0].endsWith("mpirun") || args[0].endsWith("orterun")) {
         return true;
       }
       
       return false;
     }
     
     public List<String> getAddFiles() {
       return addFiles;
     }
     
     public List<String> getAddArchives() {
       return addArchives;
     }
     
     public Map<String, String> getUserSpecifiedEnvs() {
       return userEnvs;
     }
     
     public boolean isVerbose() {
       return verbose;
     }
     
     public boolean isValgrind() {
       return valgrind;
     }
     
     public void setMcaParam(String key, String value) {
       mcaParams.put(key, value);
     }
     
     public void unsetMcaParam(String key) {
       if (mcaParams.containsKey(key)) {
         mcaParams.remove(key);
       }
     }
     
     /**
      * get number of processes in this job (-np/-c/--n/-n)
      */
     public int getNp() {
       return np;
     }
     
     public String[] getUserParam(ContainerLaunchContext ctx) {
       if (!mpiApp) {
         List<String> userParams = new ArrayList<String>();
         // append all user's parameters
         for (int i = 0; i < outArgs.length; i++) {
           userParams.add(outArgs[i]);
         }
         // append log output
         userParams.add("1>" + ApplicationConstants.LOG_DIR_EXPANSION_VAR + "/stdout");
         userParams.add("2>" + ApplicationConstants.LOG_DIR_EXPANSION_VAR + "/stderr");
         return userParams.toArray(new String[0]);
       }
       
       // add first one, it should be "mpirun"
       List<String> userParams = new ArrayList<String>();
       userParams.add("$JAVA_HOME/bin/java");
       
       /*
        * we need to specify JVM param for running AM, because we will do a "fork"
        * in AM, so we have to use at most 1/2 mem of total, but luckily, it should
        * be enough for us. :)
        */
       int xmx = 512;
       int xms = 16;
      if (this.hamsterMemory > 0) {
        // hard code specified memory > 64M, because it will be problematic when
        // we have memory less than 32M
        xmx = this.hamsterMemory / 2;
      }
       userParams.add(String.format("-Xmx%dM -Xms%dM", xmx, xms));
       
       // userParams.add("-Xdebug -Xrunjdwp:transport=dt_socket,server=y,address=\"8111\"");
       userParams.add("-cp");
       if (ctx == null) {
         userParams.add("hamster-core.jar");
       } else {
         userParams.add(ctx.getEnvironment().get("CLASSPATH"));
       }
       userParams.add("com.pivotal.hamster.appmaster.HamsterAppMaster");
       if (!valgrind) {
         userParams.add("mpirun");
       } else {
         userParams.add("valgrind");
         userParams.add("--tool=memcheck");
         userParams.add("--track-origins=yes");
         userParams.add("--leak-check=full");
         userParams.add("mpirun");
       }
            
       // add mca params to select modules
       mcaParams.put("ras", "yarn");
       mcaParams.put("plm", "yarn");
       mcaParams.put("odls", "yarn");
       
       // append mca parameters
       for (Entry<String, String> e : mcaParams.entrySet()) {
         userParams.add("-mca");
         userParams.add(e.getKey());
         userParams.add(e.getValue());
       }
       
       // append other user's parameters
       for (int i = 1; i < outArgs.length; i++) {
         userParams.add(outArgs[i]);
       }
       
       // append log output
       userParams.add("1>" + ApplicationConstants.LOG_DIR_EXPANSION_VAR + "/stdout");
       userParams.add("2>" + ApplicationConstants.LOG_DIR_EXPANSION_VAR + "/stderr");
       
       return userParams.toArray(new String[0]);
     }
     
     public boolean isMpiApp() {
       return mpiApp;
     }
     
     public String getUserCli(ContainerLaunchContext ctx) {
       String[] args = getUserParam(ctx);
       return convertArgsToCmd(args);
     }
     
     public static String convertArgsToCmd(String[] args) {
       StringBuilder sb = new StringBuilder();
       for (int i = 0; i < args.length - 1; i++) {
         sb.append(args[i]);
         sb.append(" ");
       }
       sb.append(args[args.length - 1]);
       return sb.toString();
     }
     
     public void setHamsterMemory(int mem) {
     	this.hamsterMemory = mem;
     }
     
     public int getHamsterMemory() {
     	return this.hamsterMemory;
     }
     
     public void setHamsterCPU(int cpu) {
     	this.hamsterCPU = cpu;
     } 
     
     public int getHamsterCPU() {
     	return this.hamsterCPU;
     }
 }
  
