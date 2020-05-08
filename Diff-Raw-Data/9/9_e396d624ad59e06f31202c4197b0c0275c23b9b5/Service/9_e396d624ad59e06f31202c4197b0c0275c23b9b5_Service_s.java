 import java.util.regex.Pattern;
 import java.util.regex.Matcher;
 import java.io.*;
 import java.util.*;
 import com.google.gson.*;
 import com.google.gson.reflect.TypeToken;
 import java.lang.reflect.Type;
 public class Service {
 	
 	private File workingdir;
 	
 	private String id;
 	
 	private Queue<String> stdOut = new LinkedList<String>();
 	private Queue<String> stdErr = new LinkedList<String>();
 	
 	// Whether the service refers to this program
 	private final boolean isMaster;
 	
 	// A list of the commands which can be run for this service
 	private Map<String, Command> commands = new HashMap<String, Command>();
 	private ServiceSettings settings = new ServiceSettings();
 	
 	// A list of all the services
 	private static Map<String, Service> serviceList =  new HashMap<String, Service>();
 	
 	
 	private Service(String id, File workingdir) {
 		this.isMaster = id.equals("services");
 		this.workingdir = workingdir;
 		this.id = id;
 		commands.put("clearlog", new ClearLogCommand(this));
 		commands.put("reloadconfig", new ReloadConfigCommand(this));
 		this.updateFromConfig();
 		
 		// The "services" service (ie this program) is already running
 		if (isMaster) {
 			commands.put("reloadservicelist", new ReloadServiceListCommand(this));
 			commands.put("updatevarnish", new UpdateVarnishCommand(this));
 
 			// For other services, start the start command
 		} else {
 			commands.put("start", new StartCommand(this));
 			commands.put("stop", new StopCommand(this));
 			commands.put("restart", new RestartCommand(this));
 			execCommand("start");
 		}
 	}
 	public void updateFromConfig() {
 		File settingsFile = new File(this.workingdir, Manager.getSetting("service_json", "service.json"));
 		Map<String,String> serviceDirList;
 		try {
 			String json = Manager.readFile(new FileInputStream(settingsFile));
 			Gson gson = new Gson();
 
 			settings = gson.fromJson(json, ServiceSettings.class);
 		} catch (FileNotFoundException e) {
 			logErr("Can't find service settings file: ".concat(settingsFile.getAbsolutePath()));
 			return;
 		} catch (IOException e) {
 			logErr("Can't read service settings file: ".concat(settingsFile.getAbsolutePath()));
 			return;
 		} catch (JsonSyntaxException e) {
 			logErr("Invalid JSON in service settings file: ".concat(settingsFile.getAbsolutePath()));
 			return;
 		} catch (JsonParseException e) {
 			logErr("Invalid format in service settings file: ".concat(settingsFile.getAbsolutePath()));
 			return;
 		}
 		
 		Iterator iter = settings.getCommands().entrySet().iterator();
 		while (iter.hasNext()) {
 			Map.Entry header = (Map.Entry)iter.next();
 			String cmd = (String)header.getValue();
 			String name = (String)header.getKey();
 			String key = name.toLowerCase();
 			
 			if (commands.containsKey(key)) {
 				commands.get(key).update(cmd, name);
 			} else {
 				Command command = new Command(this, cmd, name);
 				commands.put(key, command);
 			}
 		}
 		
 		// TODO: remove any commands which have been removed from JSON
 		
 		if (settings.name == null) logErr("Missing name in settings file: ".concat(settingsFile.getAbsolutePath()));
 		
 	}
 	public int getPort() {
 		return settings.port;
 	}
 	public String getName() {
 		if (settings.name == null) return id;
 		return settings.name;
 	}
 	public File getWorkingDir() {
 		return workingdir;
 	}
 	public void log(String line) {
 		if (isMaster) System.out.println(line);
 		int outputLength;
 		try {
 			outputLength = Integer.parseInt(Manager.getSetting("output_length"));
 		} catch (NumberFormatException e) {
 			outputLength = 10;
 		}
 		stdOut.add(line);
 		while (stdOut.size() > outputLength) stdOut.remove();
 	}
 	public void logErr(String line) {
 		if (isMaster) System.err.println(line);
 		int outputLength;
 		try {
 			outputLength = Integer.parseInt(Manager.getSetting("output_length"));
 		} catch (NumberFormatException e) {
 			outputLength = 10;
 		}
 		stdErr.add(line);
 		while (stdErr.size() > outputLength) stdErr.remove();
 	}
 	public void logErr(Exception e) {
 		if (isMaster) {
 			System.err.println(e);
 			e.printStackTrace(System.err);
 		}
 		Writer writer = new StringWriter();
 		PrintWriter printWriter = new PrintWriter(writer);
 		e.printStackTrace(printWriter);
 		logErr(writer.toString());
 	}
 	void clearLog() {
 		stdErr.clear();
 		stdOut.clear();
 	}
 	public boolean execCommand(String key) {
 		return execCommand(key, false);
 	}
 	public boolean execCommand(String key, boolean keepRunning) {
 		Command command = commands.get(key);
 		if (command == null) return false;
 		command.exec(keepRunning);
 		return true;
 	}
 	public void stopCommand(String key) {
 		Command command = commands.get(key);
 		if (command == null) return;
 		command.kill();
 	}
 	public boolean isRunning() {
 		if (isMaster) return true;
 		if (!commands.containsKey("main")) return false;
 		return commands.get("main").isRunning();
 	}
 	public boolean hasError() {
 		return (stdErr.size() > 0);
 	}
 	public String getId() {
 		return id;
 	}
 	public String getDomain() {
 		return settings.getDomain();
 	}
 	public Template getFullTemplate() throws IOException {
 		Template serviceTemplate = new Template("service");
 		setBasicData(serviceTemplate);
 		setExtendedData(serviceTemplate);
 		return serviceTemplate;
 	}
 	public Template getItemTemplate() throws IOException {
 		Template serviceTemplate = new Template("serviceitem");
 		setBasicData(serviceTemplate);
 		return serviceTemplate;
 	}
 	public Template getVCLBackend() throws IOException {
 		Template backendTemplate = new Template("backend", "vcl");
 		setBasicData(backendTemplate);
 		return backendTemplate;
 	}
 	public Template getVCLRecvHost() throws IOException {
 		Template recvHostTemplate = new Template("recvhost", "vcl");
 		setBasicData(recvHostTemplate);
 		String customvcl = null;
 		if (settings.disablecaching) {
 				customvcl = "return (pass); #turn off caching for now";
 		}
 		recvHostTemplate.setData("custom", customvcl);
 		return recvHostTemplate;
 	}
 	private void setBasicData(Template template) {
 		template.setData("port", this.getPort()+"");
 		template.setData("path", workingdir.getAbsolutePath());
 		template.setData("name", this.getName());
 		template.setData("running", (this.isRunning())?"running":"stopped");
 		template.setData("domain", this.getDomain());
 		template.setData("error", this.hasError()?"error":"");
 		template.setData("url", "/services/"+this.getId());
 		template.setData("id", this.getId());
 	}
 	private void setExtendedData(Template template) throws IOException {
		Iterator<String> outIter = stdOut.iterator();
 		String stdOutContent = "";
 		while (outIter.hasNext()) {
 			stdOutContent += outIter.next();
 			stdOutContent += "\n";
 		}
 		template.setData("stdOut", stdOutContent);
 		
		Iterator<String> errIter = stdErr.iterator();
 		String stdErrContent = "";
 		while (errIter.hasNext()) {
 			stdErrContent += errIter.next();
 			stdErrContent += "\n";
 		}
 		template.setData("stdErr", stdErrContent);
 		
 		
 		Iterator<Map.Entry<String, Command>> commandIter = commands.entrySet().iterator();
 		TemplateGroup commandTemplates = new TemplateGroup("html");
 		while (commandIter.hasNext()) {
 			Map.Entry<String, Command> header = commandIter.next();
 			String key = header.getKey();
 			Command command = header.getValue();
 			Template commandTemplate = new Template("commanditem");
 			
 			// Ignore the main command, as it's wrapped by Start/Stop/Restart
 			if (key.equals("main")) continue;
 			commandTemplate.setData("action", "/services/"+this.getId()+"/"+key);
 			commandTemplate.setData("name", command.getName());
 			commandTemplates.add(commandTemplate);
 		}
 		template.setData("commandlist", commandTemplates);
 		
 	}
 	public static Service getById(String id) {
 		return serviceList.get(id);
 	}
 	public static Template getIndexTemplate() {
 		try {
 			Template indexTemplate = new Template("index");
 			TemplateGroup serviceTemplates = new TemplateGroup("html");
 			Iterator<Service> iter = serviceList.values().iterator();
 			while (iter.hasNext()) {
 				Service service = iter.next();
 				serviceTemplates.add(service.getItemTemplate());
 			}
 			indexTemplate.setData("servicelist", serviceTemplates);
 			return indexTemplate;
 		} catch (IOException e) {
 			Manager.logErr("Problem with index template");
 			Manager.logErr(e);
 			return null;
 		}
 	}
 	public static Template getVCL() {
 		try {
 			Template vclTemplate = new Template("services", "vcl");
 			TemplateGroup backendTemplates = new TemplateGroup("vcl");
 			TemplateGroup recvHostTemplates = new TemplateGroup("vcl");
 			Iterator<Service> iter = serviceList.values().iterator();
 			while (iter.hasNext()) {
 				Service service = iter.next();
 				backendTemplates.add(service.getVCLBackend());
 				recvHostTemplates.add(service.getVCLRecvHost());
 			}
 			vclTemplate.setData("backends", backendTemplates);
 			vclTemplate.setData("recvhosts", recvHostTemplates);
 			return vclTemplate;
 		} catch (IOException e) {
 			Manager.logErr("Problem with index template");
 			Manager.logErr(e);
 			return null;
 		}
 	}
 
 	/**
 	 * Loads the Service for the currently running Service (this is quite meta, but allows for easy logging etc)
 	 *
 	 * @returns Service
 	 */
 	public static Service loadServicesService() {
 		Service services = new Service("services", new File("."));
 		serviceList.put("services", services);
 		return services;
 	}
 
 	/**
 	 * Loads all the services in the service list
 	 *
 	 * @returns void
 	 */
 	public static void loadServiceList() {
 		File serviceListFile = new File (Manager.getSetting("root_path", ""), Manager.getSetting("service_list", "service_list.json"));
 		Map<String,String> serviceDirList;
 		try {
 			String json = Manager.readFile(new FileInputStream(serviceListFile));
 			Type listType = new TypeToken<HashMap<String,String>>(){}.getType();
 			Gson gson = new Gson();
 
 			serviceDirList = gson.fromJson(json, listType);
 		} catch (FileNotFoundException e) {
 			Manager.logErr("Can't find service list file: ".concat(serviceListFile.getAbsolutePath()));
 			return;
 		} catch (IOException e) {
 			Manager.logErr("Can't read service list file: ".concat(serviceListFile.getAbsolutePath()));
 			return;
 		} catch (JsonSyntaxException e) {
 			Manager.logErr("Invalid JSON in service list file: ".concat(serviceListFile.getAbsolutePath()));
 			return;
 		} catch (JsonParseException e) {
 			Manager.logErr("Invalid format in service list file (should be an object of key/value pairs): ".concat(serviceListFile.getAbsolutePath()));
 			return;
 		}
 
 		for (Map.Entry<String,String> entry : serviceDirList.entrySet()) {
 			String serviceKey = entry.getKey();
 			if (serviceList.containsKey(serviceKey)) {
 				serviceList.get(serviceKey).updateFromConfig();
 			} else {
 				File directory = new File(Manager.getSetting("root_path", ""), entry.getValue());
 				Service service = new Service(serviceKey, directory);
 				serviceList.put(serviceKey, service);
 			}
 		}
 		Manager.updateVarnish();
 	}
 	
 	public static Service getAuth() {
 		return serviceList.get("auth");
 	}
 
 	static class ServiceSettings {
 		public int port;
 		public String name;
 		public boolean disablecaching;
 		private Map<String, String> commands;
 		private String subdomain;
 		public Map<String, String> getCommands() {
 			if (commands == null) return new HashMap<String, String>();
 			return commands;
 		}
 		public String getDomain() {
 			String rootdomain = Manager.getSetting("root_domain", "example.com");
 			if (subdomain != null) return subdomain + "." +rootdomain;
 			return rootdomain;
 		}
 	}
 	
 }
