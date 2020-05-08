 package com.griddynamics.jagger.jenkins.plugin;
 
 import com.floreysoft.jmte.Engine;
 import hudson.Extension;
 import hudson.Launcher;
 import hudson.model.*;
 import hudson.tasks.BuildStepDescriptor;
 import hudson.tasks.Builder;
 import hudson.util.FormValidation;
 import hudson.util.QuotedStringTokenizer;
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.QueryParameter;
 
 import java.io.*;
 import java.util.*;
 
 
 public class JaggerEasyDeployPlugin extends Builder
 {
 
     //to collect kernel nodes in one field.
     private ArrayList<KernelNode> kernelNodeList = new ArrayList<KernelNode>();
 
     private final MasterNode masterNode;
 
     //the collect nodes to attack in one field.
     private ArrayList<SuT> sutsList = new ArrayList<SuT>();
 
     private final DBOptions dbOptions;
 
     private final AdditionalProperties additionalProperties;
 
     //environment properties file for test suit
     private final String envProperties;
 
     private String envPropertiesActual;
 
     private String deploymentScript;
 
     //path to Jagger Test Suit .zip
     private final String jaggerTestSuitePath;
 
     private String jaggerTestSuitePathActual;
 
     private final String BASE_DIR = "result";
 
     private final String JAGGER_HOME = "runned_jagger" ;
 
     private final boolean multiNodeConfiguration;
 
     private String lineSeparator;
 
     private transient Engine transformEngine;
 
     /**
      * Constructor where fields from *.jelly will be passed
      * @param sutsList
      *                      List of nodes to test
      * @param masterNode  Master
      * @param kernelNodeList  Kernels
      * @param jaggerTestSuitePath test suite path
      * @param dbOptions properties of dataBase
      * @param additionalProperties properties from text area
      * @param envProperties properties for all nodes
      */
     @DataBoundConstructor
     public JaggerEasyDeployPlugin(ArrayList<SuT> sutsList, ArrayList<KernelNode> kernelNodeList, String jaggerTestSuitePath, DBOptions dbOptions,
                                   AdditionalProperties additionalProperties, String envProperties, MasterNode masterNode) {
 
         this.dbOptions = dbOptions;
         this.sutsList = sutsList;
         this.kernelNodeList = kernelNodeList;
         this.jaggerTestSuitePath = jaggerTestSuitePath;
         this.jaggerTestSuitePathActual = jaggerTestSuitePath;
         this.additionalProperties = additionalProperties;
 
         this.envProperties = envProperties;
         this.envPropertiesActual = envProperties;
         this.masterNode = masterNode;
 
         this.lineSeparator = System.getProperty("line.separator");
 
         multiNodeConfiguration = kernelNodeList != null && kernelNodeList.size() > 0;
     }
 
 
     public String getLineSeparator() {
         return lineSeparator;
     }
 
     public Launcher.ProcStarter getProcStarter() {
         return procStarter;
     }
 
     public void setDeploymentScript(String deploymentScript) {
         this.deploymentScript = deploymentScript;
     }
 
     public ArrayList<KernelNode> getKernelNodeList() {
         return kernelNodeList;
     }
 
     public MasterNode getMasterNode() {
         return masterNode;
     }
 
     public boolean isMultiNodeConfiguration() {
         return multiNodeConfiguration;
     }
 
     public String getEnvPropertiesActual() {
         return envPropertiesActual;
     }
 
     public void setEnvPropertiesActual(String envPropertiesActual) {
         this.envPropertiesActual = envPropertiesActual;
     }
 
     public String getEnvProperties() {
         return envProperties;
     }
 
     public String getJaggerTestSuitePathActual() {
         return jaggerTestSuitePathActual;
     }
 
     public void setJaggerTestSuitePathActual(String jaggerTestSuitePathActual) {
         this.jaggerTestSuitePathActual = jaggerTestSuitePathActual;
     }
 
     public DBOptions getDbOptions() {
         return dbOptions;
     }
 
     public AdditionalProperties getAdditionalProperties() {
         return additionalProperties;
     }
 
     public String getJaggerTestSuitePath() {
         return jaggerTestSuitePath;
     }
 
     public ArrayList<SuT> getSutsList() {
         return sutsList;
     }
 
     public String getDeploymentScript() {
         return deploymentScript;
     }
 
     /**
      * Loading EnvVars and create properties_files
      * @param build .
      * @param listener .
      * @return true
      */
     @Override
     public boolean prebuild(Build build, BuildListener listener) {
 
         PrintStream logger = listener.getLogger();
 
         transformEngine = Engine.createDefaultEngine();
         lineSeparator = System.getProperty("line.separator");
 
         try {
             checkUsesOfEnvironmentProperties(build, listener);
         } catch (IOException e) {
             logger.println("EXCEPTION WHILE CHECKING USES OF ENV VARAIBLES");
             logger.println(e);
             return false;
         } catch (InterruptedException e) {
             logger.println("EXCEPTION WHILE CHECKING USES OF ENV VARAIBLES");
             logger.println(e);
             return false;
         }
 
         try {
             setDeploymentScript(generateScript());
         } catch (IOException e) {
             logger.println("Exception while reading script templates" + getLineSeparator() + e);
             return false;
         }
 
         logger.println(getLineSeparator() + "-------------Deployment-Script-------------------" + getLineSeparator());
         logger.println(getDeploymentScript());
         logger.println(getLineSeparator() + "-------------------------------------------------" + getLineSeparator() + getLineSeparator());
 
         return true;
     }
 
     /**
      * main view of script
      * @return implementation of 'main.script' template
      * @throws IOException while reading template file
      *
      * base.directory;deploying;starting.nodes;collecting.results
      */
     private String generateScript() throws IOException {
 
         Map<String,Object> args = new HashMap<String, Object>();
         args.put("base-directory", BASE_DIR);
         args.put("deploying", deployJagger());
         args.put("starting-nodes", startNodes());
         args.put("collecting-results", collectResults());
         args.put("stopping-agents", stopAgents());
 
         return transformEngine.transform(ScriptTemplate.MAIN.getTemplateString(), args);
     }
 
 
     private String stopAgents() throws IOException {
 
         StringBuilder deploy = new StringBuilder();
 
         if(getSutsList()!=null){
             for(SuT node:getSutsList()) {
                 deploy.append(stopAgent(node));
             }
         }
 
         return deploy.toString();
     }
 
 
     private String stopAgent(SuT node) throws IOException {
 
         Map<String,Object> args = new HashMap<String, Object>();
 
         addNodeProperties(args, node);
         args.put("jagger-home", JAGGER_HOME);
 
         return transformEngine.transform(ScriptTemplate.STOP_AGENT.getTemplateString(), args);
     }
 
 
     private String collectResults() throws IOException {
 
         StringBuilder deploy = new StringBuilder();
 
         deploy.append(copyReports(getMasterNode()));
 
         deploy.append(copyLogs(getMasterNode()));
 
         if(getKernelNodeList()!=null){
             for(Node node:getKernelNodeList()) {
                 deploy.append(copyLogs(node));
             }
         }
 
         if(getSutsList()!=null){
             for(SuT node:getSutsList()) {
                 deploy.append(copyLogs(node));
             }
         }
 
         return deploy.toString();
     }
 
 
     private String copyReports(Node node) throws IOException {
 
         Map<String,Object> args = new HashMap<String, Object>();
 
         addNodeProperties(args, node);
         args.put("base-directory", BASE_DIR);
         args.put("jagger-home", JAGGER_HOME);
 
         return transformEngine.transform(ScriptTemplate.COPY_REPORTS.getTemplateString(), args);
     }
 
 
     private String copyLogs(Node node) throws IOException {
 
         Map<String,Object> args = new HashMap<String, Object>();
 
         addNodeProperties(args, node);
         args.put("base-directory", BASE_DIR);
         args.put("jagger-home", JAGGER_HOME);
 
 
         return transformEngine.transform(ScriptTemplate.COPY_LOGS.getTemplateString(), args);
     }
 
 
     private String startNodes() throws IOException {
 
         Map<String,Object> args = new HashMap<String, Object>();
         args.put("starting-master", startMaster());
         args.put("starting-kernels-agents", startKernelsAgents());
         args.put("check-kernels-agents", checkKernelsAgents());
 
         return transformEngine.transform(ScriptTemplate.START_NODES.getTemplateString(), args);
     }
 
 
     private String checkKernelsAgents() throws IOException {
 
         StringBuilder deploy = new StringBuilder();
 
         if(getKernelNodeList()!=null){
             for(Node node:getKernelNodeList()) {
                 deploy.append(checkKernel(node));
             }
         }
 
         if(getSutsList()!=null){
             for(SuT node:getSutsList()) {
                 deploy.append(checkAgent(node));
             }
         }
 
         return deploy.toString();
     }
 
 
     private String checkAgent(SuT node) throws IOException {
 
         Map <String,Object> args = new HashMap<String, Object>();
         addNodeProperties(args, node);
 
         return transformEngine.transform(ScriptTemplate.CHECK_AGENT.getTemplateString(), args);
     }
 
 
     private String checkKernel(Node node) throws IOException {
 
         Map <String,Object> args = new HashMap<String, Object>();
         addNodeProperties(args, node);
 
         return transformEngine.transform(ScriptTemplate.CHECK_KERNEL.getTemplateString(), args);
     }
 
 
     private String startKernelsAgents() throws IOException {
 
         StringBuilder deploy = new StringBuilder();
 
         if(getKernelNodeList()!=null){
             for(Node node:getKernelNodeList()) {
                 deploy.append(startKernel(node));
             }
         }
 
         if(getSutsList()!=null){
             for(SuT node:getSutsList()) {
                 deploy.append(startAgent(node));
             }
         }
 
         return deploy.toString();
     }
 
 
     private String startAgent(SuT node) throws IOException {
 
         Map <String,Object> args = new HashMap<String, Object>();
 
         addNodeProperties(args, node);
         args.put("master-server-address", getMasterNode().getServerAddressActual());
         args.put("additional-properties", addAdditionalProperties());
         args.put("jagger-home", JAGGER_HOME);
         args.put("jmx-enabled", node.isUseJmx());
         if(node.isUseJmx()) {
             args.put("jmx-ports", node.getJmxPortActual().split(","));
         }
 
         return transformEngine.transform(ScriptTemplate.START_AGENT.getTemplateString(), args);
     }
 
 
     private String startKernel(Node node) throws IOException {
 
         Map <String,Object> args = new HashMap<String, Object>();
 
         addNodeProperties(args, node);
         addDBProperties(args);
         args.put("jagger-home", JAGGER_HOME);
         args.put("master-server-address", getMasterNode().getServerAddressActual());
         args.put("additional-properties", addAdditionalProperties());
         args.put("jagger-properties", getEnvPropertiesActual());
 
         return transformEngine.transform(ScriptTemplate.START_KERNEL.getTemplateString(), args);
     }
 
 
     private String startMaster() throws IOException {
 
         Map <String,Object> args = new HashMap<String, Object>();
         args.put("jagger-home", JAGGER_HOME);
         if(!getDbOptions().isUseExternalDB()) {
             args.put("h2-db", "h2");
         }
         args.put("jagger-properties", getEnvPropertiesActual());
         args.put("zookeeper-port", "2181");
 
         addNodeProperties(args, getMasterNode());
 
         addDBProperties(args);
 
         args.put("min-agents", getSutsList()==null? 0 : getSutsList().size());
         args.put("min-kernels", getKernelNodeList()==null? 0 : getKernelNodeList().size());
 
         args.put("additional-properties", addAdditionalProperties());
 
         return transformEngine.transform(ScriptTemplate.START_MASTER.getTemplateString(), args);
     }
 
 
     private String addAdditionalProperties() {
 
         StringBuilder result = new StringBuilder();
         if(getAdditionalProperties().isDeclared()) {
             Iterator iter = getLinesIterator(getAdditionalProperties().getTextFromAreaActual());
             if(iter != null && iter.hasNext()) {
                 do {
                     result.append("-D").append(iter.next());
                     if(iter.hasNext()){
                         result.append(" \\").append(getLineSeparator()).append("\t");
                     }
                 } while (iter.hasNext());
             }
         }
 
         return result.toString();
     }
 
 
      public Iterator getLinesIterator(String text) {
 
         List<String> result = new ArrayList<String>();
         for(String line : text.split(getLineSeparator())) {
             if(!line.trim().isEmpty()) {
                 result.add(line);
             }
         }
 
         return result.iterator();
     }
 
 
     private void addNodeProperties(Map<String,Object> map, Node node) {
 
         map.put("user-name", node.getUserNameActual());
         map.put("server-address", node.getServerAddressActual());
         map.put("ssh-key-path", processKey(node.getSshKeyPathActual()));
         if(node.isSetJavaHome()) {
             map.put("java-home", node.getJavaHomeActual());
         }
         map.put("java-options", node.getJavaOptionsActual());
     }
 
 
     private void addDBProperties(Map<String,Object> map){
 
         if(getDbOptions().isUseExternalDB()) {
             map.put("db-driver",getDbOptions().getRdbDriverActual());
             map.put("db-url",getDbOptions().getRdbClientUrlActual());
             map.put("db-user-name",getDbOptions().getRdbUserNameActual());
             map.put("db-password",getDbOptions().getRdbPassword());
             map.put("db-dialect",getDbOptions().getRdbDialectActual());
         } else {
             map.put("db-driver","org.h2.Driver");
             map.put("db-url","jdbc:h2:tcp://" + getMasterNode().getServerAddressActual() + ":8043/jaggerdb");
             map.put("db-user-name","jagger");
             map.put("db-password","rocks");
             map.put("db-dialect","org.hibernate.dialect.H2Dialect");
         }
     }
 
     /**
      * @return  deploy's part of script
      * @throws IOException   while reading scripts templates
      */
     private String deployJagger() throws IOException {
 
         StringBuilder deploy = new StringBuilder();
 
         deploy.append(deployJagger1(getMasterNode()));
 
         if(isMultiNodeConfiguration()) {
             for(Node node:getKernelNodeList()){
 
                 deploy.append(deployJagger1(node));
             }
         }
 
         if (getSutsList() != null) {
             for(SuT node : getSutsList()){
 
                 deploy.append(deployJagger1(node));
             }
         }
 
         return deploy.toString();
     }
 
     /**
      * implements template 'deploy.script'
      * @param node  Jagger Node
      * @return piece of deploy part of script
      * @throws java.io.IOException while reading template script
      *
      * properties
      * user.name;server.address;jagger.home;ssh.key.path;jagger.test.suite.name;jagger.test.suite.path;
      */
     private String deployJagger1(Node node) throws IOException {
 
         Map <String,Object> args = new HashMap<String, Object>();
         addNodeProperties(args, node);
         args.put("jagger-home", JAGGER_HOME);
         args.put("jagger-test-suite-path", new File(getJaggerTestSuitePathActual()).getAbsolutePath());
         args.put("jagger-test-suite-name", new File(getJaggerTestSuitePathActual()).getName());
 
         return transformEngine.transform(ScriptTemplate.DEPLOYING.getTemplateString(), args);
     }
 
     /**
      *process key
      * @param keyPath string to process;
      * @return  string
      */
     private String processKey(String keyPath) {
         return keyPath.trim().isEmpty() ? "" : " -i " + keyPath.trim();
     }
 
     /**
      * provide ability to use environment properties
      * @param listener /
      * @param build    /
      * @throws java.io.IOException /
      * @throws InterruptedException /
      */
     private void checkUsesOfEnvironmentProperties(Build build, BuildListener listener) throws IOException, InterruptedException {
 
         checkNodesOnBuildVars(build, listener);
         checkAdditionalPropertiesOnBuildVars(build, listener);
         checkJaggerTestSuitOnBuildVars(build, listener);
         checkDBOptionsOnBuildVars(build, listener);
         checkAgentsOnBuildVars(build, listener);
         checkEnvPropertiesOnBuildVars(build, listener);
     }
 
 
     private void checkEnvPropertiesOnBuildVars(Build build, BuildListener listener) throws IOException, InterruptedException {
 
         String temp = getEnvProperties();
         setEnvPropertiesActual(build.getEnvironment(listener).expand(temp));
         if(getEnvPropertiesActual().matches("\\s*")) {
             setEnvPropertiesActual("./configuration/basic/default.environment.properties");
         }
     }
 
 
     private void checkAgentsOnBuildVars(Build build, BuildListener listener) throws IOException, InterruptedException {
 
         if(getSutsList() != null) {
             for(SuT node : getSutsList()) {
                 checkJavaHome(build, listener, node);
                 checkSshNodesServerAddresses(build, listener, node);
                 checkSshNodesSSHKeyPath(build, listener, node);
                 checkSshNodesUserName(build, listener, node);
                 checkJmxPort(build, listener, node);
                 checkJavaOptions(build, listener, node);
             }
         }
     }
 
 
     private void checkJmxPort(Build build, BuildListener listener, SuT node) throws IOException, InterruptedException {
 
         String temp = node.getJmxPort();
         node.setJmxPortActual(build.getEnvironment(listener).expand(temp));
     }
 
 
     private void checkDBOptionsOnBuildVars(Build build, BuildListener listener) throws IOException, InterruptedException {
 
         String temp = dbOptions.getRdbDialect();
         dbOptions.setRdbDialectActual(build.getEnvironment(listener).expand(temp));
 
         temp = dbOptions.getRdbUserName();
         dbOptions.setRdbUserNameActual(build.getEnvironment(listener).expand(temp));
 
         temp = dbOptions.getRdbClientUrl();
         dbOptions.setRdbClientUrlActual(build.getEnvironment(listener).expand(temp));
 
         temp = dbOptions.getRdbDriver();
         dbOptions.setRdbDriverActual(build.getEnvironment(listener).expand(temp));
     }
 
 
     private void checkJaggerTestSuitOnBuildVars(Build build, BuildListener listener) throws IOException, InterruptedException {
 
         String temp = getJaggerTestSuitePath();
         setJaggerTestSuitePathActual(build.getEnvironment(listener).expand(temp));
     }
 
 
     private void checkAdditionalPropertiesOnBuildVars(Build build, BuildListener listener) throws IOException, InterruptedException {
 
         String temp = getAdditionalProperties().getTextFromArea();
         additionalProperties.setTextFromAreaActual(build.getEnvironment(listener).expand(temp));
     }
 
     /**
      * Check if Build Variables contain addresses , or VERSION (of Jagger)
      * @param build  /
      * @param listener /
      * @throws java.io.IOException /
      * @throws InterruptedException /
      */
     private void checkNodesOnBuildVars(Build build, BuildListener listener) throws IOException, InterruptedException {
 
         checkSshNodesServerAddresses(build, listener, masterNode);
         checkSshNodesUserName(build, listener, masterNode);
         checkSshNodesSSHKeyPath(build, listener, masterNode);
         checkJavaHome(build, listener, masterNode);
         checkJavaOptions(build, listener, masterNode);
 
         if(multiNodeConfiguration) {
             for(Node node: kernelNodeList){
 
                 checkSshNodesServerAddresses(build, listener, node);
                 checkSshNodesUserName(build, listener, node);
                 checkSshNodesSSHKeyPath(build, listener, node);
                 checkJavaHome(build, listener, node);
                 checkJavaOptions(build, listener, node);
             }
         }
     }
 
 
     private void checkJavaOptions(Build build, BuildListener listener, SshNode node) throws IOException, InterruptedException {
 
         String temp = node.getJavaOptions();
         node.setJavaOptionsActual(build.getEnvironment(listener).expand(temp));
     }
 
 
     private void checkJavaHome(Build build, BuildListener listener, SshNode node) throws IOException, InterruptedException {
 
         String temp = node.getJavaHome();
         node.setJavaHomeActual(build.getEnvironment(listener).expand(temp));
     }
 
 
     private void checkSshNodesSSHKeyPath(Build build, BuildListener listener, SshNode node) throws IOException, InterruptedException {
 
         String temp = node.getSshKeyPath();
         node.setSshKeyPathActual(build.getEnvironment(listener).expand(temp));
     }
 
 
     private void checkSshNodesUserName(Build build, BuildListener listener, SshNode node) throws IOException, InterruptedException {
 
         String temp = node.getUserName();
         node.setUserNameActual(build.getEnvironment(listener).expand(temp));
     }
 
 
     private void checkSshNodesServerAddresses(Build build, BuildListener listener, SshNode node) throws IOException, InterruptedException {
 
         String temp = node.getServerAddress();
         node.setServerAddressActual(build.getEnvironment(listener).expand(temp));
     }
 
 
     // Start's processes on computer where jenkins run ProcStarter is not serializable
     transient private Launcher.ProcStarter procStarter = null;
 
 
     /**
      * This method will be called in build time (when you build job)
      * @param build   .
      * @param launcher .
      * @param listener .
      * @return boolean : true if build passed, false in other way
      * @throws InterruptedException
      * @throws IOException
      */
     @Override
     public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)  throws InterruptedException, IOException {
 
         PrintStream logger = listener.getLogger();
         logger.println(getLineSeparator() + "______Jagger_Easy_Deploy_Started______" + getLineSeparator());
         String pathToDeploymentScript = build.getWorkspace() + File.separator + "deploy-script.sh";
 
         try{
 
             setUpProcStarter(launcher,build,listener);
 
             createScriptFile(pathToDeploymentScript);
 
             logger.println(getLineSeparator() + "-----------------Deploying--------------------" + getLineSeparator() + getLineSeparator());
 
             int exitCode = getProcStarter().cmds(stringToCmds("./deploy-script.sh")).start().join();
 
             logger.println("exit code : " + exitCode);
 
             logger.println(getLineSeparator() + "----------------------------------------------" + getLineSeparator());
 
             if(exitCode != 0) {
 
                 if(getSutsList() != null) {
 
                         setDeploymentScript(stopAgents());
                         logger.println(getLineSeparator() + getDeploymentScript() + getLineSeparator());
                         getProcStarter().cmds(stringToCmds(getDeploymentScript())).start().join();
                         logger.println(getLineSeparator());
                 }
 
                 return false;
             } else {
 
                 return true;
             }
         } catch (IOException e) {
 
             logger.println("!!!" + getLineSeparator() + "Exception in perform " + e +
                     "can't create script file or run script");
 
             if(new File(pathToDeploymentScript).delete()) {
                 logger.println(pathToDeploymentScript + " has been deleted");
             } else {
                 logger.println(pathToDeploymentScript + " haven't been created");
             }
         }
 
         return true;
     }
 
 
     /**
      * creating script file to execute later
      * @throws IOException  if can't create file  or ru cmds.
      * @param file 5
      */
     private void createScriptFile(String file) throws IOException {
 
         PrintWriter fw = null;
         try{
             fw = new PrintWriter(new FileOutputStream(file));
             fw.write(getDeploymentScript());
 
         } finally {
             if(fw != null){
                 fw.close();
             }
         }
 
         //setting permissions for executing
         procStarter.cmds(stringToCmds("chmod +x " + file)).start();
     }
 
 
    private void setUpProcStarter(Launcher launcher, AbstractBuild<?, ?> build, BuildListener listener) throws IOException, InterruptedException {
 
         procStarter = launcher.launch();
         procStarter.pwd(build.getWorkspace());
         procStarter.stdout(listener);
        procStarter.envs(build.getEnvironment(listener));
     }
 
     /**
      * String to array
      * cd directory >> [cd, directory]
      * @param str commands in ine string
      * @return array of commands
      */
     private String[] stringToCmds(String str){
         return QuotedStringTokenizer.tokenize(str);
     }
 
 
     /**
      * Unnecessary, but recommended for more type safety
      * @return Descriptor of this class
      */
     @Override
     public Descriptor<Builder> getDescriptor() {
         return (DescriptorJEDP)super.getDescriptor();
     }
 
     @Extension
     public static final class DescriptorJEDP  extends BuildStepDescriptor<Builder>
     {
 
         @Override
         public boolean isApplicable(Class<? extends AbstractProject> aClass) {
             return true;
         }
 
         @Override
         public String getDisplayName() {
             //how it names in build step config
             return "Jagger Easy Deploy";
         }
 
         public FormValidation doCheckEnvProperties(@QueryParameter final String value) {
 
             if(value.matches("\\s*")){
                 return FormValidation.ok("default properties will be used");
             }
 
             return FormValidation.ok();
         }
 
         public FormValidation doCheckJaggerTestSuitePath(@QueryParameter final String value) {
 
             if(value.matches("\\s*")){
                 return FormValidation.warning("set path, please");
             }
             if(value.contains("$")) {
                 return FormValidation.ok();
             }
             String temp = value;
             if(value.startsWith("~")){
                 temp = temp.substring(1,temp.length());
                 temp = System.getProperty("user.home") + temp;
             }
             if(!new File(temp).exists()){
                 return FormValidation.warning("file not exists");
             }
 
             return FormValidation.ok();
         }
     }
 }
