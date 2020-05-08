 package hudson.plugins.buckminster.command;
 
 import hudson.FilePath;
 import hudson.Launcher;
 import hudson.model.BuildListener;
 import hudson.model.TaskListener;
 import hudson.model.AbstractBuild;
 import hudson.model.Computer;
 import hudson.model.Hudson;
 import hudson.model.JDK;
 import hudson.model.Node;
 import hudson.plugins.buckminster.BuckminsterInstallation;
 import hudson.plugins.buckminster.EclipseBuckminsterBuilder;
 import hudson.plugins.buckminster.callables.GetDirectorExecutable;
 import hudson.plugins.buckminster.install.BuckminsterInstallable;
 
import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Creates the commandline to invoke to process and writes a textfile containing the 
  * buckminster commands to a file commands.txt in the workspace
  * @author Johannes Utzig
  *
  */
 public class CommandLineBuilder {
 	private BuckminsterInstallation installation;
 	private String commands;
 	private String logLevel;
 	private String additionalParams;
 	private String equinoxLauncherArgs;
 	private FilePath hudsonWorkspaceRoot;
 	private String userWorkspace;
 	private String userTemp;
 	private String userOutput;
 	private String userCommandFile;
 	private String propertyFile;
 	
 
 	public CommandLineBuilder(BuckminsterInstallation installation,
 			String commands, String logLevel, String additionalParams,
 			String userWorkspace, String userTemp, String userOutput,
 			String userCommandFile, String propertyFile, String equinoxLauncherArgs) {
 		this(installation);
 		this.commands = commands;
 		this.logLevel = logLevel;
 		this.additionalParams = additionalParams;
 		this.userWorkspace = userWorkspace;
 		this.userTemp = userTemp;
 		this.userOutput = userOutput;
 		this.userCommandFile = userCommandFile;
 		this.propertyFile = propertyFile;
 		this.equinoxLauncherArgs = equinoxLauncherArgs;
 	}
 	
 	CommandLineBuilder(BuckminsterInstallation installation) {
 		this.installation = installation;
 	}
 
 	public static CommandLineBuilder forInstallation(BuckminsterInstallation installation){
 		return new CommandLineBuilder(installation);
 	}
 	
 	public CommandLineBuilder commands(String commands){
 		this.commands = commands;
 		return this;
 	}
 	
 	public CommandLineBuilder loglevel(String loglevel){
 		this.logLevel = loglevel;
 		return this;
 	}
 	
 	public CommandLineBuilder additionalParams(String additionalParams){
 		this.additionalParams = additionalParams;
 		return this;
 	}
 	
 	public CommandLineBuilder equinoxLauncherArgs(String equinoxLauncherArgs){
 		this.equinoxLauncherArgs = equinoxLauncherArgs;
 		return this;
 	}
 	
 	public CommandLineBuilder userWorkspace(String userWorkspace){
 		this.userWorkspace = userWorkspace;
 		return this;
 	}
 	
 	public CommandLineBuilder userTemp(String userTemp){
 		this.userTemp = userTemp;
 		return this;
 	}
 	
 	public CommandLineBuilder userOutput(String userOutput){
 		this.userOutput = userOutput;
 		return this;
 	}
 	
 	
 	public CommandLineBuilder userCommandFile(String userCommandFile){
 		this.userCommandFile = userCommandFile;
 		return this;
 	}
 	
 	
 	public CommandLineBuilder propertyFile(String propertyFile){
 		this.propertyFile = propertyFile;
 		return this;
 	}
 	
 	/**
 	 * fills an arraylist with all program arguments for buckminster
 	 * 
 	 * @param build
 	 * @param launcher 
 	 * @return the list containing the actual invocation and all parameters
 	 * @throws MalformedURLException
 	 * @throws IOException
 	 * @throws InterruptedException
 	 */
 	public List<String> buildCommands(AbstractBuild<?,?> build, BuildListener listener, Launcher launcher)
 			throws MalformedURLException, IOException, InterruptedException{
 
 		List<String> commandList = new ArrayList<String>();
 		hudsonWorkspaceRoot = build.getWorkspace();
 
 		commandList.add(getJavaExecutable(listener, launcher, build));
 		
 		Map<String, String> properties = new CaseInsensitiveMap(build.getEnvironment(listener));
 		properties.putAll(build.getBuildVariables());
 		
 		addJVMProperties(commandList, properties);
 
 		FilePath commandsPath = getCommandFilePath(build, properties);
 		
 		addStarterParameters(build, commandList, properties);
 
 
 		 commandList.add("--loglevel");
 		 commandList.add(getLogLevel());
 		 
 		 //add a parameter for global properties if defined
 		 if(getPropertyFile()!=null && getPropertyFile().length()>0)
 		 {
 			 commandList.add("-P");
 			 commandList.add(expandProperties(getPropertyFile(), properties));
 		 }
 		// Tell Buckminster about the command file
 		commandList.add("-S");
 		commandList.add(commandsPath.getRemote());
 		//only write out commands if the user did not specify a custom command file
 		if(userCommandFile==null || userCommandFile.length()==0)
 		{
 			writeCommandFile(commandsPath, properties);
 		}
 		return commandList;
 	}
 
 	String getJavaExecutable(BuildListener listener, Launcher launcher, AbstractBuild<?,?> build) throws IOException,
 			InterruptedException {
 
 		
 		JDK jdk = build.getProject().getJDK();
 		if(jdk!=null)
 		{
 	        jdk= jdk.forNode(Computer.currentComputer().getNode(), listener);
 	        jdk = jdk.forEnvironment(build.getEnvironment(listener));
 		}
 		
 		//if none is configured, hope it is in the PATH
 		//otherwise use the configured one
 		if(jdk!=null)
 		{
 			Boolean isWindows = !launcher.isUnix();
 			FilePath javaExecutable;
 			if(isWindows)
 			{ 
 				javaExecutable = Computer.currentComputer().getNode().createPath(jdk.getHome()).child("bin").child("java.exe");
 			}
 			else
 			{
 				javaExecutable = Computer.currentComputer().getNode().createPath(jdk.getHome()).child("bin").child("java");
 			}
 			if(javaExecutable.exists())
 			{
 				return javaExecutable.getRemote();
 			}
 			else
 			{
 				String message = "The configured JDK \"{0}\" points to \"{1}\" but no executable exists. Defaulting to \"java\"";
 				message = MessageFormat.format(message, jdk.getName(),jdk.getHome());
 				listener.error(message);
 			}
 		}
 		return "java";
 	}
 
 	FilePath getCommandFilePath(AbstractBuild<?, ?> build,
 			Map<String, String> properties) {
 		// the file listing all the commands since buckminster doesn't accept
 		// several commands as programm arguments
 		if(userCommandFile==null || userCommandFile.length()==0)
 		{
 			return build.getWorkspace().child("commands.txt");
 		}
 		return build.getWorkspace().child(expandProperties(userCommandFile, properties));
 
 	}
 
 	public BuckminsterInstallation getInstallation() {
 		return installation;
 	}
 
 	public String getCommands() {
 		return commands;
 	}
 
 	public String getLogLevel() {
 		return logLevel;
 	}
 
 	public String getAdditionalParams() {
 		return additionalParams;
 	}
 	
 	public String getEquinoxLauncherArgs() {
 		return equinoxLauncherArgs;
 	}
 	
 	public String getPropertyFile() {
 		return propertyFile;
 	}
 
 	void writeCommandFile(FilePath commandsPath, Map<String, String> properties)
 			throws IOException, InterruptedException {
 		commandsPath.write(expandProperties(getCommands(), properties), "UTF-8");
 	}
 
 	void addStarterParameters(AbstractBuild<?,?> build, List<String> commandList, Map<String, String> properties)
 			throws IOException, InterruptedException {
 		commandList.add("-jar");
 		commandList.add(findEquinoxLauncher());
 
 		//add additional arguments for the equinox launcher
 		addEquinoxLauncherProperties(commandList, properties);
 			
 		
 		// Specify Eclipse Product
 		commandList.add("-application");
 		commandList.add("org.eclipse.buckminster.cmdline.headless");
 
 		// set the workspace to the hudson workspace
 		commandList.add("-data");
 	
 		String workspace = getDataPath(build, properties);
 
 		commandList.add(workspace);
 	}
 
 	void addEquinoxLauncherProperties(List<String> commandList,
 			Map<String, String> properties) {
 		if(getEquinoxLauncherArgs()!=null)
 		{
 			String[] equinoxArgs = getEquinoxLauncherArgs().split("[\n\r]+");
 			for (int i = 0; i < equinoxArgs.length; i++) {
 				if(equinoxArgs[i].trim().length()>0){
 					String parameter = expandProperties(equinoxArgs[i],properties);
 					commandList.add(parameter);
 				}
 			}
 			
 		}
 	}
 
 	String getDataPath(AbstractBuild<?, ?> build,
 			Map<String, String> properties) throws IOException, InterruptedException {
 		if(userWorkspace==null || userWorkspace.length()==0)
 		{
 			return hudsonWorkspaceRoot.getRemote();
 		}
 		return expandProperties(userWorkspace, properties);
 	}
 
 	void addJVMProperties(List<String> commandList, Map<String, String> properties) throws IOException, InterruptedException {
 		//temp and output root
 		commandList.add(MessageFormat.format("-Dbuckminster.output.root={0}",getOutputDir(properties)));
 		commandList.add(MessageFormat.format("-Dbuckminster.temp.root={0}",getTempDir(properties)));
 		String params = getInstallation().getParams();
 		if(params!=null)
 		{
 			String[] globalVMParams = params.split("[\n\r]+");
 
 			for (int i = 0; i < globalVMParams.length; i++) {
 				if(globalVMParams[i].trim().length()>0)
 					commandList.add(expandProperties(globalVMParams[i],properties));
 			}
 			if(globalVMParams.length==0)
 			{
 				commandList.add("-Xmx512m");
 				commandList.add("-XX:PermSize=128m");
 			}
 		}
 		else
 		{
 			commandList.add("-Xmx512m");
 			commandList.add("-XX:PermSize=128m");
 		}
 
 		//job vm setting (properties)
 		String jobParams = getAdditionalParams();
 		if(jobParams!=null){
 			String[] additionalJobParams = jobParams.split("[\n\r]+");
 			for (int i = 0; i < additionalJobParams.length; i++) {
 				if(additionalJobParams[i].trim().length()>0){
 					String parameter = expandProperties(additionalJobParams[i],properties);
 					commandList.add(parameter);
 				}
 			}
 		}
 	}
 
 	Object getTempDir(Map<String, String> properties) {
 		if(userTemp==null || userTemp.length()==0)
 		{
 			return hudsonWorkspaceRoot.child("buckminster.temp").getRemote();
 		}
 		return hudsonWorkspaceRoot.child(expandProperties(userTemp, properties)).getRemote();
 	}
 
 	String getOutputDir(Map<String, String> properties) {
 		if(userOutput==null || userOutput.length()==0)
 		{
 			return hudsonWorkspaceRoot.child("buckminster.output").getRemote();
 		}
 		return hudsonWorkspaceRoot.child(expandProperties(userOutput, properties)).getRemote();
 	}
 
 	String expandProperties(String string, Map<String, String> properties) {
 		Pattern pattern = Pattern.compile("\\$\\{(.*?)\\}",Pattern.CASE_INSENSITIVE);
 		Matcher matcher = pattern.matcher(string);
 		while(matcher.find()){
 			if(matcher.group(1)!=null){
 				if(properties.containsKey(matcher.group(1))){
 					String replacement = null;
 					if(matcher.group(1).equalsIgnoreCase("WORKSPACE")){
 						//special treatment for the workspace variable because the path has to be transformed into a unix path
 						//see: https://hudson.dev.java.net/issues/show_bug.cgi?id=4947
						replacement = new File(properties.get("WORKSPACE")).toURI().getPath();
 					}
 					else{
 						replacement = properties.get(matcher.group(1));
 					}
 					string = string.replace(matcher.group(0), replacement);	
 				}
 			}
 		}
 		
 		return string;
 	}
 	
 	/**
 	 * searches for the eclipse starter jar
 	 * <p>
 	 * The content of the folder $ECLIPSE_HOME/plugins is listed and the first
 	 * file that starts with <code>org.eclipse.equinox.launcher_</code> is
 	 * returned.
 	 * 
 	 * @return the guess for the startup jar, or <code>null</code> if none was
 	 *         found
 	 * @throws IOException 
 	 * @throws InterruptedException 
 	 * @see EclipseBuckminsterBuilder#getEclipseHome()
 	 */
 	String findEquinoxLauncher() throws IOException, InterruptedException {
 		FilePath installationHome =  Computer.currentComputer().getNode().createPath(getInstallation().getHome());
 		FilePath pluginDir = installationHome.child("plugins");
 		if(!pluginDir.exists())
 			throw new FileNotFoundException("No 'plugins' directory has been found in "+installation.getHome());
 		List<FilePath> plugins = pluginDir.list();
 		for (FilePath filePath : plugins) {
 			
 			
 			if (filePath.getName()
 					.startsWith("org.eclipse.equinox.launcher_")) {
 				return filePath.getRemote();
 
 			}
 		}
 		throw new FileNotFoundException("No equinox launcher jar has been found in "+pluginDir.getRemote());
 	}
 	
 
 	public static List<String> createDirectorScript(BuckminsterInstallable installable, FilePath toolDir, Node node, TaskListener log, Set<String> repositories, Set<String> featuresToInstall) throws IOException, InterruptedException
 	{
 		return createDirectorScript(installable, toolDir, node, log, repositories, featuresToInstall, new HashSet<String>());
 	}
 	
 	public static List<String> createDirectorScript(BuckminsterInstallable installable, FilePath toolDir, Node node, TaskListener log, Set<String> repositories, Set<String> featuresToInstall, Set<String> featuresToUninstall) throws IOException, InterruptedException
 	{
 		List<String> commands = new ArrayList<String>();
 		
 		String executableName = toolDir.getChannel().call(new GetDirectorExecutable());
 		String directorInvocation = toolDir.child("director").child(executableName).getRemote();
 		
 		commands.add(directorInvocation);
 		FilePath buckyDir = toolDir.child("buckminster");
 		String buckyDirPath = buckyDir.getRemote();
 		List<JDK> jdks = Hudson.getInstance().getJDKs();
 		if(jdks!=null && jdks.size()>0)
 		{
 			JDK jdk = Hudson.getInstance().getJDKs().get(0);
 			jdk = jdk.forNode(node, log);
 			jdk = jdk.forEnvironment(node.toComputer().getEnvironment());
 			commands.add("-vm");
 			commands.add(node.createPath(jdk.getBinDir().getPath()).child("java").getRemote());
 		}
 		commands.add("-d");
 		commands.add(buckyDirPath);
 		commands.add("-p");
 		commands.add("Buckminster");
 		if(repositories.size()>0)
 		{
 			commands.add("-r");
 			commands.add(toCSV(repositories));
 		}
 		if(featuresToUninstall.size()>0)
 		{
 			commands.add("-uninstallIU");
 			commands.add(toCSV(featuresToUninstall));
 		}
 		if(featuresToInstall.size()>0)
 		{
 			commands.add("-installIU");
 			commands.add(toCSV(featuresToInstall));
 		}
 		
 		return commands;
 	}
 
 	
 	static String toCSV(Collection<String> values) {
 		return toCSV(values,", ");
 	}
 	
 	static String toCSV(Collection<String> values, String separator) {
 		if(values==null)
 			throw new IllegalArgumentException("values is null");
 		if(values.isEmpty())
 			return "";
 		StringBuilder builder = new StringBuilder();
 		for (String value : values) {
 				builder.append(value);
 				builder.append(separator);
 		}
 		builder.setLength(builder.length()-separator.length());
 		return builder.toString();
 	}
 }
