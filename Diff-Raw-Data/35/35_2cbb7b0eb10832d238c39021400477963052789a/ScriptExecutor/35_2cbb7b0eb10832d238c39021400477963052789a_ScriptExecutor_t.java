 package kkckkc.jsourcepad.util.io;
 
 import com.google.common.base.Charsets;
 import com.google.common.base.Splitter;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.google.common.io.CharStreams;
 import com.google.common.io.Files;
 import kkckkc.jsourcepad.model.Application;
 import kkckkc.jsourcepad.model.ExecutionSettings;
 import kkckkc.jsourcepad.model.SettingsManager;
 import kkckkc.jsourcepad.util.Cygwin;
 import kkckkc.utils.Os;
 import kkckkc.utils.io.FileUtils;
 
 import java.io.*;
 import java.util.*;
 import java.util.concurrent.*;
 
 public class ScriptExecutor {
 	private static final int DELAY_BEFORE_DELAY_EVENT = 500;
 	private String script;
 	private ExecutorService executorService;
     private File directory;
 
 	public ScriptExecutor(String script, ExecutorService executorService) {
 		this.script = script;
 		this.executorService = executorService;
 	}
 
     public void setDirectory(File directory) {
         this.directory = directory;
     }
 
     public Execution execute(Callback callback, Reader input, Map<String, String> environment) throws IOException {
 		StringWriter sw = new StringWriter();
 		return execute(new Execution(callback, input, sw, sw), environment);
 	}
 
 	public Execution execute(Callback callback, Reader input, Writer stdoutHandler, Map<String, String> environment) throws IOException {
 		return execute(new Execution(callback, input, stdoutHandler, new StringWriter()), environment);
 	}
 	
 	public Execution execute(Callback callback, Reader input, Writer stdoutHandler, Writer stderrHandler, Map<String, String> environment) throws IOException {
 		return execute(new Execution(callback, input, stdoutHandler, stderrHandler), environment);
 	}
 	
 	private Execution execute(final Execution execution, Map<String, String> environment) throws IOException { 
 		final ProcessBuilder pb = getProcess(execution, environment);
         final Process p = pb.start();
 
 		final Future<?> stdoutFuture = this.executorService.submit(
 				new GobblerRunnable(p.getInputStream(), execution.stdout));
 		final Future<?> stderrFuture = this.executorService.submit(
 				new GobblerRunnable(p.getErrorStream(), execution.stderr));
 		execution.stdoutFuture = stdoutFuture;
 		execution.stderrFuture = stderrFuture;
 		execution.processFuture = this.executorService.submit(new Runnable() {
             public void run() {
 	            try {
 	                p.waitFor();
 	                
 	                int exitCode = p.exitValue();
 	                
 	                stdoutFuture.get();
 	                stderrFuture.get();
 	                
 	                if (exitCode == 0) {
 	                	execution.callback.onSuccess(execution);
 	                } else {
 	                	execution.callback.onFailure(execution);
 	                }
 	                
 	                execution.cleanup();
                 } catch (InterruptedException e) {
 	                p.destroy();
                 	execution.callback.onAbort(execution);
                 } catch (ExecutionException e) {
                 	throw new RuntimeException(e);
                 }
             }
 		});
 		
 		Writer stdin = new OutputStreamWriter(p.getOutputStream(), "utf-8");
 		
 		char[] b = new char[8192];  
 		int read;  
 		while ((read = execution.input.read(b)) != -1) {
 			stdin.write(b, 0, read);  
 		}  
 		stdin.flush();
 		stdin.close();
 
 		
 		try {
 	        execution.processFuture.get(DELAY_BEFORE_DELAY_EVENT, TimeUnit.MILLISECONDS);
         } catch (InterruptedException e) {
         	throw new RuntimeException(e);
         } catch (ExecutionException e) {
         	throw new RuntimeException(e);
         } catch (TimeoutException e) {
 	        execution.callback.onDelay(execution);
         }
         
         return execution;
 	}
 	
 
 	protected ProcessBuilder getProcess(Execution execution, Map<String, String> environment) throws IOException {
 		execution.tempScriptFile = FileUtils.newTempFile("jsourcepad", ".sh");
 		execution.tempScriptFile.setExecutable(true);
         execution.script = script;
         Files.write(script, execution.tempScriptFile, Charsets.UTF_8);
 
         String path = execution.tempScriptFile.getPath();
 
         List<String> argList = Lists.newArrayList();
 
         SettingsManager settingsManager = Application.get().getSettingsManager();
         argList = new ArrayList(Arrays.asList(settingsManager.get(ExecutionSettings.class).getArgs()));
 
         if (Os.isWindows()) {
             path = Cygwin.makePath(path);
         }
 
         List<String> lines = Lists.newArrayList();
         Iterables.addAll(lines, Splitter.on("\n").split(script));
         String firstLine = lines.get(0);
         String cygwinPrefix = "cd " + Cygwin.makePath(directory == null ? new File(".").getCanonicalPath() : directory.getPath()) + "; ";
         if (firstLine.startsWith("#!")) {
             // Remove shebang
             firstLine = firstLine.substring(2);
             if (Os.isWindows()) {
                 argList.add(cygwinPrefix + firstLine.trim() + " " + path);
             } else {
                 argList.add(firstLine.trim() + " " + path);
             }
         } else if (lines.size() == 1) {
             if (Os.isWindows()) {
                 argList.add(cygwinPrefix + firstLine.trim());
             } else {
                 argList.add(firstLine.trim());
             }
         } else {
             if (Os.isWindows()) {
                 argList.add(cygwinPrefix + path);
             } else {
                 argList.add(path);
             }
         }
 
 		ProcessBuilder pb = new ProcessBuilder(argList);
 		pb.environment().putAll(environment);
 
        if (directory != null && ! Os.isWindows()) {
             pb.directory(directory);
         }
 
 	    return pb;
     }
 
     
 
 
     public interface Callback {
 		public void onSuccess(Execution execution);
 		public void onAbort(Execution execution);
 		public void onDelay(Execution execution);
 		public void onFailure(Execution execution);
 	}
 	
 	public static class CallbackAdapter implements Callback {
 		public void onSuccess(Execution execution) {}
 		public void onAbort(Execution execution) {}
 		public void onDelay(Execution execution) {}
 		public void onFailure(Execution execution) {}
 	}
 	
 	
 	public static class Execution {
 		public Future<?> stderrFuture;
 		public Future<?> stdoutFuture;
 		private File tempScriptFile;
 		private Writer stdout;
 		private Writer stderr;
 		private Future<?> processFuture;
 		private Callback callback;
 		private Reader input;
 		private boolean cancelled = false;
         private String script;
 
         public Execution(Callback callback, Reader input, Writer stdout, Writer stderr) {
 	        this.callback = callback;
 	        this.stdout = stdout;
 	        this.stderr = stderr;
 	        this.input = input;
         }
 
         public String getScript() {
             return script;
         }
 
 		public boolean isCancelled() {
 	        return cancelled;
         }
 
 		public void cancel() {
 			this.cancelled  = true;
 			processFuture.cancel(true);
 		}
 		
 		public String getStdout() {
 			if (stdout instanceof StringWriter) {
 				return ((StringWriter) stdout).getBuffer().toString();
 			}
 			return null;
 		}
 
 		public String getStderr() {
 			if (stderr instanceof StringWriter) {
 				return ((StringWriter) stderr).getBuffer().toString();
 			}
 			return null;
 		}
 		
 		private void cleanup() {
 			tempScriptFile.delete();
 		}
 
 		public void waitForCompletion() throws InterruptedException, ExecutionException {
 			if (isCancelled()) throw new RuntimeException("Process has been cancelled");
 	        processFuture.get();
 	        stdoutFuture.get();
 	        stderrFuture.get();
         }
 	}
 	
 	static class GobblerRunnable implements Runnable {
 		private InputStream is;
 		private Writer w;
 		
 		public GobblerRunnable(InputStream is, Writer w) {
 			this.is = is;
 			this.w = w;
 		}
 
 		public void run() {
             try {
                 CharStreams.copy(new InputStreamReader(is, "utf-8"), w);
             } catch (IOException e) {
                 throw new RuntimeException(e);
             }
         }
 	}
 
 	public static Reader noInput() {
 	    return new StringReader("");
     }
 
 	public static Map<String, String> noEnvironment() {
 	    return Collections.emptyMap();
     }
 }
