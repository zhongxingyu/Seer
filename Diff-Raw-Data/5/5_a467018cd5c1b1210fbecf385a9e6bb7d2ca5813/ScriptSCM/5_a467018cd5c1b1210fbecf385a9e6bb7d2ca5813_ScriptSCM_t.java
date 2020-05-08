 package com.cwctravel.hudson.plugins.script_scm;
 
 import groovy.lang.GroovyShell;
 import hudson.Extension;
 import hudson.FilePath;
 import hudson.Launcher;
 import hudson.Util;
 import hudson.console.ConsoleNote;
 import hudson.model.BuildListener;
 import hudson.model.Result;
 import hudson.model.TaskListener;
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractProject;
 import hudson.model.Cause;
 import hudson.scm.ChangeLogParser;
 import hudson.scm.PollingResult;
 import hudson.scm.PollingResult.Change;
 import hudson.scm.RepositoryBrowser;
 import hudson.scm.SCMDescriptor;
 import hudson.scm.SCMRevisionState;
 import hudson.scm.SCM;
 import hudson.tasks.Ant;
 import hudson.triggers.SCMTrigger.SCMTriggerCause;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.io.PrintWriter;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import net.sf.json.JSONObject;
 
 import org.kohsuke.stapler.StaplerRequest;
 
 public class ScriptSCM extends SCM {
 	private static class TempBuild<P extends AbstractProject<P, B>, B extends AbstractBuild<P, B>> extends AbstractBuild<P, B> {
 		protected TempBuild(P job, FilePath workspace) {
 			super(job, new GregorianCalendar());
 			setWorkspace(workspace);
 		}
 
 		@Override
 		public void run() {}
 	}
 
 	private static class TempBuildListener implements BuildListener {
 		private static final long serialVersionUID = 8702160265256733186L;
 
 		private final TaskListener listener;
 
 		public TempBuildListener(TaskListener listener) {
 			this.listener = listener;
 		}
 
 		public PrintStream getLogger() {
 			return listener.getLogger();
 		}
 
 		public void annotate(ConsoleNote ann) throws IOException {
 			listener.annotate(ann);
 
 		}
 
 		public void hyperlink(String url, String text) throws IOException {
 			listener.hyperlink(url, text);
 		}
 
 		public PrintWriter error(String msg) {
 			return listener.error(msg);
 		}
 
 		public PrintWriter error(String format, Object... args) {
 			return listener.error(format, args);
 		}
 
 		public PrintWriter fatalError(String msg) {
 			return listener.fatalError(msg);
 		}
 
 		public PrintWriter fatalError(String format, Object... args) {
 			return listener.fatalError(format, args);
 		}
 
 		public void started(List<Cause> causes) {
 
 		}
 
 		public void finished(Result result) {
 
 		}
 
 	}
 
 	public static class PropertiesBuilder {
 		private final Map<String, String> properties = new HashMap<String, String>();
 
 		public PropertiesBuilder put(String key, Object value) {
 			properties.put(key, value != null ? value.toString() : "");
 			return this;
 		}
 
 		public PropertiesBuilder clear() {
 			properties.clear();
 			return this;
 		}
 
 		public PropertiesBuilder remove(String key) {
 			properties.remove(key);
 			return this;
 		}
 
 		@Override
 		public String toString() {
 			StringWriter sW = new StringWriter();
 			Properties props = new Properties();
 			props.putAll(properties);
 			properties.clear();
 			try {
 				props.store(sW, "");
 			}
 			catch(IOException e) {
 
 			}
 
 			return sW.toString();
 		}
 	}
 
 	public static class Utils {
 		public String escapePropertyValue(String str) {
 			if(str != null) {
 				return str.replace("\\", "\\\\").replace("\r", "").replace("\n", "");
 			}
 
 			return str;
 		}
 
 		public AbstractBuild<?, ?> getLastSuccessfulSCMBuild(AbstractProject<?, ?> project) {
 			if(project != null) {
 				AbstractBuild<?, ?> b = project.getLastBuild();
 				// temporary hack till we figure out what's causing this bug
 				while(b != null && (b.isBuilding() || b.getResult() == null || b.getResult().isWorseThan(Result.UNSTABLE)) && (b.getCause(SCMTriggerCause.class) != null)) {
 					b = b.getPreviousBuild();
 				}
 				return b;
 			}
 			return null;
 		}
 	}
 
 	private String groovyScript;
 	private String groovyScriptFile;
 	private String bindings;
 
 	public ScriptSCM(String groovyScript, String groovyScriptFile, String bindings) {
 		this.groovyScript = Util.fixEmpty(groovyScript);
 		this.groovyScriptFile = Util.fixEmpty(groovyScriptFile);
 		this.bindings = Util.fixEmpty(bindings);
 	}
 
 	public void executeAnt(AbstractProject<?, ?> project, FilePath workspace, Launcher launcher, TaskListener listener, String targets,
 			String antName, String antOpts, String buildFile, String properties) throws ScriptTriggerException {
 		Ant ant = new Ant(targets, antName, antOpts, buildFile, properties);
 		try {
 			TempBuild<?, ?> tempBuild = new TempBuild(project, workspace);
 			ant.perform(tempBuild, launcher, listener instanceof BuildListener ? (BuildListener)listener : new TempBuildListener(listener));
 
 		}
 		catch(IOException iE) {
 			throw new ScriptTriggerException("Script Execution failed", iE);
 		}
 		catch(InterruptedException e) {
 			throw new ScriptTriggerException("Script Execution failed", e);
 		}
 		catch(IllegalArgumentException e) {
 			throw new ScriptTriggerException("Script Execution failed", e);
 		}
 		catch(SecurityException e) {
 			throw new ScriptTriggerException("Script Execution failed", e);
 		}
 	}
 
 	public void executeAnt(AbstractBuild<?, ?> build, Launcher launcher, TaskListener listener, String targets, String antName, String antOpts,
 			String buildFile, String properties) throws ScriptTriggerException {
 		Ant ant = new Ant(targets, antName, antOpts, buildFile, properties);
 		try {
 			ant.perform(build, launcher, listener instanceof BuildListener ? (BuildListener)listener : new TempBuildListener(listener));
 		}
 		catch(IOException iE) {
 			throw new ScriptTriggerException("Script Execution failed", iE);
 		}
 		catch(InterruptedException e) {
 			throw new ScriptTriggerException("Script Execution failed", e);
 		}
 		catch(IllegalArgumentException e) {
 			throw new ScriptTriggerException("Script Execution failed", e);
 		}
 		catch(SecurityException e) {
 			throw new ScriptTriggerException("Script Execution failed", e);
 		}
 	}
 
 	private void evaluateGroovyScript(File workspace, Map<String, Object> input) throws IOException {
 		GroovyShell groovyShell = new GroovyShell();
 		if(input != null) {
 			setGroovySystemObjects(input);
 			for(Map.Entry<String, Object> entry: input.entrySet()) {
 				groovyShell.setVariable(entry.getKey(), entry.getValue());
 			}
 			if(groovyScriptFile != null) {
 				File scriptFile = new File(groovyScriptFile);
 				if(!scriptFile.exists()) {
 					scriptFile = new File(workspace, groovyScriptFile);
 				}
 				String groovyScript = Util.loadFile(scriptFile);
 				groovyShell.evaluate(groovyScript);
 			}
 			else {
 				groovyShell.evaluate(groovyScript);
 			}
 		}
 	}
 
 	private void setGroovySystemObjects(Map<String, Object> input) throws IOException {
 		if(input != null) {
 			input.put("propertiesBuilder", new PropertiesBuilder());
 			input.put("utils", new Utils());
 
 			if(bindings != null) {
 				Properties p = new Properties();
 				p.load(new StringReader(bindings));
 				for(Map.Entry<Object, Object> entry: p.entrySet()) {
 					input.put((String)entry.getKey(), entry.getValue());
 				}
 			}
 		}
 	}
 
 	@Override
 	public SCMRevisionState calcRevisionsFromBuild(AbstractBuild<?, ?> build, Launcher launcher, TaskListener listener) throws IOException, InterruptedException {
 		Map<String, Object> input = new HashMap<String, Object>();
 		input.put("action", "calcRevisionsFromBuild");
 		input.put("build", build);
 		input.put("launcher", launcher);
 		input.put("listener", listener);
 		input.put("scm", this);
 		FilePath filePath = build.getWorkspace().createTempFile("revision-state-", "");
 		input.put("revisionStatePath", filePath.getRemote());
 		input.put("workspacePath", build.getWorkspace().getRemote());
 		input.put("rootPath", build.getRootDir().getAbsolutePath());
 
 		ScriptSCMRevisionState result = null;
 		try {
 			evaluateGroovyScript(new File(build.getWorkspace().getRemote()), input);
 			result = new ScriptSCMRevisionState();
 			result.setRevisionState(Util.loadFile(new File(filePath.getRemote())));
 		}
 		finally {
 			filePath.delete();
 		}
 		return result;
 	}
 
 	@Override
 	public boolean checkout(AbstractBuild<?, ?> build, Launcher launcher, FilePath workspace, BuildListener listener, File changelogFile) throws IOException, InterruptedException {
 		Map<String, Object> input = new HashMap<String, Object>();
 		input.put("action", "checkout");
 		input.put("build", build);
 		input.put("launcher", launcher);
 		input.put("listener", listener);
 		input.put("workspace", workspace);
 		input.put("scm", this);
 		input.put("workspacePath", build.getWorkspace().getRemote());
 		input.put("changeLogPath", changelogFile.getParentFile().getAbsolutePath());
 		input.put("changeLogFile", changelogFile.getAbsolutePath());
 		evaluateGroovyScript(new File(workspace.getRemote()), input);
 		return true;
 	}
 
 	@Override
 	protected PollingResult compareRemoteRevisionWith(AbstractProject<?, ?> project, Launcher launcher, FilePath workspace, TaskListener listener,
 			SCMRevisionState baseline) throws IOException, InterruptedException {
 		Map<String, Object> input = new HashMap<String, Object>();
 		input.put("action", "compareRemoteRevisionWith");
 		input.put("project", project);
 		input.put("launcher", launcher);
 		input.put("listener", listener);
 		input.put("workspace", workspace);
 		input.put("scm", this);
 		input.put("baseline", baseline);
 		input.put("workspacePath", workspace.getRemote());
 
 		FilePath changeResultPath = workspace.createTempFile("change-result", "");
 		input.put("changeResultPath", changeResultPath.getRemote());
 
 		FilePath currentRevisionStatePath = workspace.createTempFile("current-revision", "");
 		input.put("currentRevisionStatePath", currentRevisionStatePath.getRemote());
 
 		PollingResult result = null;
 		try {
 			evaluateGroovyScript(new File(workspace.getRemote()), input);
 
 			ScriptSCMRevisionState remoteRevisionState = new ScriptSCMRevisionState();
 			remoteRevisionState.setRevisionState(Util.loadFile(new File(currentRevisionStatePath.getRemote())));
 			result = new PollingResult(baseline, remoteRevisionState, Change.valueOf(Util.loadFile(new File(changeResultPath.getRemote()))));
 		}
 		finally {
 			changeResultPath.delete();
 			currentRevisionStatePath.delete();
 		}
 		return result;
 	}
 
 	@Override
 	public ChangeLogParser createChangeLogParser() {
 		return new ScriptSCMChangeLogParser();
 	}
 
 	@Override
 	public RepositoryBrowser<ScriptSCMChangeLogEntry> getBrowser() {
 		return new ScriptSCMBrowser();
 	}
 
 	@Extension
 	public static final class DescriptorImpl extends SCMDescriptor<ScriptSCM> {
 		public DescriptorImpl() {
 			super(ScriptSCM.class, null);
 			load();
 		}
 
 		@Override
 		public ScriptSCM newInstance(StaplerRequest req, JSONObject formData) throws FormException {
 			String groovyScript = null;
 			String groovyScriptFile = null;
 			String bindings = null;
 
 			JSONObject jsonObject = (JSONObject)formData.get("scriptSource");
 			if(jsonObject != null) {
 				if(jsonObject.getInt("value") == 0) {
					groovyScript = jsonObject.getString("groovyScript");
 				}
 				else if(jsonObject.getInt("value") == 1) {
					groovyScriptFile = jsonObject.getString("groovyScriptFile");
 				}
 			}
 
 			bindings = formData.getString("bindings");
 			return new ScriptSCM(groovyScript, groovyScriptFile, bindings);
 		}
 
 		@Override
 		public String getDisplayName() {
 			return "Script SCM";
 		}
 	}
 
 	public String getGroovyScript() {
 		return groovyScript;
 	}
 
 	public void setGroovyScript(String groovyScript) {
 		this.groovyScript = groovyScript;
 	}
 
 	public String getGroovyScriptFile() {
 		return groovyScriptFile;
 	}
 
 	public void setGroovyScriptFile(String groovyScriptFile) {
 		this.groovyScriptFile = groovyScriptFile;
 	}
 
 	public String getBindings() {
 		return bindings;
 	}
 
 	public void setBindings(String bindings) {
 		this.bindings = bindings;
 	}
 
 }
